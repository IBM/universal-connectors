/*
Copyright 2022-2023 IBM Inc. All rights reserved
SPDX-License-Identifier: Apache-2.0
*/
package com.ibm.guardium.documentdb;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.validator.routines.InetAddressValidator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.ibm.guardium.universalconnector.commons.GuardConstants;
import com.ibm.guardium.universalconnector.commons.Util;
import com.ibm.guardium.universalconnector.commons.structures.Record;
import com.ibm.guardium.universalconnector.commons.structures.SessionLocator;

import co.elastic.logstash.api.Configuration;
import co.elastic.logstash.api.Context;
import co.elastic.logstash.api.Event;
import co.elastic.logstash.api.Filter;
import co.elastic.logstash.api.FilterMatchListener;
import co.elastic.logstash.api.LogstashPlugin;
import co.elastic.logstash.api.PluginConfigSpec;

@LogstashPlugin(name = "documentdb_guardium_filter")
public class DocumentdbGuardiumFilter implements Filter {

	public static final PluginConfigSpec<String> SOURCE_CONFIG = PluginConfigSpec.stringSetting("source", "message");
	public static final String LOGSTASH_TAG_JSON_PARSE_ERROR = "_documentdbguardium_json_parse_error";
	public static final String LOGSTASH_TAG_JSON_DEPTH_ERROR = "_documentdbguardium_json_depth_error";
	/*
	 * skipping non-relevant log events like
	 * "successful authenticate", and other events
	 * with blank db name
	 */
	public static final String LOGSTASH_TAG_SKIP = "_documentdbguardium_skip";
	// Reuse Gson instances to avoid creating new ones for every event (Performance Optimization)
	private static final Gson GSON_PARSER = new Gson();
	private static final Gson GSON_SERIALIZER = new GsonBuilder().serializeNulls().create();
	private static final Gson GSON_SERIALIZER_NO_ESCAPE =
			new GsonBuilder().serializeNulls().disableHtmlEscaping().create();
	private static final String DOCUMENTDB_AUDIT_SIGNAL = "\"atype\"";
	private static final String DOCUMENTDB_PROFILER_SIGNAL = "command";
	private static final String AGGR_KEY = "aggregate";
	private static final String COUNT_KEY = "count";
	private static final String DELETE_KEY = "remove";
	private static final String INSERT_KEY = "insert";
	private static final String UPDATE_KEY = "update";
	private static final String DISTINCT_KEY = "distinct";
	private static final String FIND_KEY = "find";
	private static final String FINDANDMODIFY_KEY = "findAndModify";
	// Set for efficient profiler key checking (Performance Optimization)
	private static final Set<String> PROFILER_KEYS =
			new HashSet<>(
					Arrays.asList(
							AGGR_KEY,
							COUNT_KEY,
							DELETE_KEY,
							INSERT_KEY,
							UPDATE_KEY,
							DISTINCT_KEY,
							FIND_KEY,
							FINDANDMODIFY_KEY));
	private static final Set<String> LOCAL_IP_LIST =
			new HashSet<>(Arrays.asList("127.0.0.1", "0:0:0:0:0:0:0:1"));
	private static final String DOCUMENT_INTERNAL_API_IP = "(NONE)";
	private static final InetAddressValidator inetAddressValidator = InetAddressValidator.getInstance();
	private static Logger log = LogManager.getLogger(DocumentdbGuardiumFilter.class);
	Parser parser;
	private String id;

	public DocumentdbGuardiumFilter(String id, Configuration config, Context context) {
		// constructors should validate configuration options
		this.id = id;
		this.parser = new Parser();
	}

	@Override
	public Collection<PluginConfigSpec<?>> configSchema() {
		// should return a list of all configuration options for this plugin
		return Collections.singletonList(SOURCE_CONFIG);
	}

	@Override
	public String getId() {
		return this.id;
	}

	/**
	 * Filter event to create Guard record object(s) for each Adit/Profiler event
	 */
	@Override
	public Collection<Event> filter(Collection<Event> events, FilterMatchListener matchListener) {
		String messageString=null;
		ArrayList<Event> skippedEvents = new ArrayList<>();
		for (Event e : events) {
			if (e.getField("message") instanceof String) {
				messageString = e.getField("message").toString();
				

				if (!isProperlyClosedJson(messageString)) {
					log.error("DocumentDB filter: JSON validation failed (truncated or too large)");
					e.tag(LOGSTASH_TAG_JSON_DEPTH_ERROR);
					continue;
				}
				if (messageString.contains(DOCUMENTDB_AUDIT_SIGNAL)) {// This is an audit event
					try {
						JsonObject inputJSON = GSON_PARSER.fromJson(messageString, JsonObject.class);
						final String atype = inputJSON.get("atype").getAsString();
						final JsonObject param = inputJSON.get("param").getAsJsonObject();
						if ((atype.equals("authenticate") && param.get("error").getAsString().equals("0") ) ||
								(param.has("ns") && param.get("ns").getAsString().isEmpty()))
						{
							e.tag(LOGSTASH_TAG_SKIP);
							skippedEvents.add(e);
							continue;
						}
						Record record = parser.parseAuditRecord(inputJSON);
						if(e.getField("serverHostnamePrefix") !=null && e.getField("serverHostnamePrefix") instanceof String) {
							record.getAccessor().setServerHostName(e.getField("serverHostnamePrefix").toString()+".aws.com");
							String dbName=record.getDbName();
							record.setDbName(!dbName.isEmpty()?e.getField("serverHostnamePrefix").toString()+":"+dbName:e.getField("serverHostnamePrefix").toString());
						}
						record.getAccessor().setServiceName(record.getDbName());

						this.correctIPs(e, record);
						if(record.getSessionId().isEmpty()) {
							record.getSessionLocator().setClientPort(SessionLocator.PORT_DEFAULT);
						}
						e.setField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME, GSON_SERIALIZER.toJson(record));
						matchListener.filterMatched(e); // Flag OK for filter input/parsing/out

					} catch (StackOverflowError soe) {
						log.error(
								"DocumentDB filter: JSON nesting too deep (StackOverflow), skipping event {}",
								logEvent(e));
						e.tag(LOGSTASH_TAG_JSON_DEPTH_ERROR);
					} catch (OutOfMemoryError oom) {
						log.error(
								"DocumentDB filter: Insufficient memory to process event, skipping {}",
								logEvent(e));
						e.tag(LOGSTASH_TAG_JSON_PARSE_ERROR);
						System.gc(); // Suggest garbage collection
					} catch (JsonSyntaxException jse) {
						log.error(
								"DocumentDB filter: Error parsing docDb audit event {} \n {} ",
								logEvent(e),
								formatJsonSyntaxException(jse));
						e.tag(LOGSTASH_TAG_JSON_PARSE_ERROR);
					} catch (Exception exception) {
						// don't let event pass filter
						// events.remove(e);
						log.error(
								"DocumentDB filter: Error parsing docDb audit event {}", logEvent(e), exception);
						e.tag(LOGSTASH_TAG_JSON_PARSE_ERROR);
					}
				} else if (messageString.contains(DOCUMENTDB_PROFILER_SIGNAL)) {// This is a profiler event
					try {
						if (containsAnyProfilerKey(messageString)) {
							JsonObject inputJSON = GSON_PARSER.fromJson(messageString, JsonObject.class);
							if ((!inputJSON.has("ns")) || (inputJSON.has("ns") && inputJSON.get("ns").getAsString().isEmpty()) )  {
								e.tag(LOGSTASH_TAG_SKIP);
								skippedEvents.add(e);
								continue;
							}
							Record record = parser.parseProfilerRecord(inputJSON);
							if (e.getField("serverHostnamePrefix") instanceof String) {
								record.getAccessor().setServerHostName(e.getField("serverHostnamePrefix").toString()+".aws.com");
								String dbName=record.getDbName();
								record.setDbName(!dbName.isEmpty()?e.getField("serverHostnamePrefix").toString()+":"+dbName:e.getField("serverHostnamePrefix").toString());
							}
							record.getAccessor().setServiceName(record.getDbName());

							this.correctIPs(e, record);
							if(record.getSessionId().isEmpty()) {
								record.getSessionLocator().setClientPort(SessionLocator.PORT_DEFAULT);
							}
							if (record.getDbName().equals(Parser.UNKOWN_STRING))  {
	                            e.tag(LOGSTASH_TAG_SKIP);
								skippedEvents.add(e);
	                            continue;
	                        }
							e.setField(
									GuardConstants.GUARDIUM_RECORD_FIELD_NAME,
									GSON_SERIALIZER_NO_ESCAPE.toJson(record));
							matchListener.filterMatched(e); // Flag OK for filter input/parsing/out
						}
					} catch (StackOverflowError soe) {
						log.error(
								"DocumentDB filter: JSON nesting too deep (StackOverflow), skipping event {} ",
								logEvent(e));
						e.tag(LOGSTASH_TAG_JSON_DEPTH_ERROR);
					} catch (OutOfMemoryError oom) {
						log.error(
								"DocumentDB filter: Insufficient memory to process event, skipping {} ",
								logEvent(e));
						e.tag(LOGSTASH_TAG_JSON_PARSE_ERROR);
						System.gc(); // Suggest garbage collection
					} catch (JsonSyntaxException jse) {
						log.error(
								"DocumentDB filter: Error parsing docDb profiler event {} \n {} ",
								logEvent(e),
								formatJsonSyntaxException(jse));
						e.tag(LOGSTASH_TAG_JSON_PARSE_ERROR);
					} catch (Exception exception) {
						// don't let event pass filter
						// events.remove(e);
						log.error(
								"DocumentDB filter: Error parsing docDb profiler event {} ",
								logEvent(e),
								exception);
						e.tag(LOGSTASH_TAG_JSON_PARSE_ERROR);
					}
				}
			}
		}
		events.removeAll(skippedEvents);
		return events;
	}

	/**
	 * Overrides DocumentDB local/remote IP 127.0.0.1, if Logstash Event contains
	 * "server_ip".
	 * Override "(NONE)" IP, if not filterd, as it's internal command by DocumentDB.
	 * Note: IP needs to be in ipv4/ipv6 format
	 * 
	 * @param e      - Logstash Event
	 * @param record - Record after parsing.
	 */
	private void correctIPs(Event e, Record record) {
		// Override "(NONE)" IP, if not filterd, as it's internal command by DocumentDB.
		// Note: IP needs to be in ipv4/ipv6 format
		SessionLocator sessionLocator = record.getSessionLocator();
		String sessionServerIp = sessionLocator.getServerIp();

		if (isDocumentInternalCommandIp(sessionServerIp)) {
			String ip = getValidatedEventServerIp(e);
			if (ip != null) {
				if (Util.isIPv6(ip)) {
					sessionLocator.setServerIpv6(ip);
					sessionLocator.setIpv6(true);
				} else {
					sessionLocator.setServerIp(ip);
					sessionLocator.setIpv6(false);
				}
			} else if (sessionServerIp.equalsIgnoreCase(DOCUMENT_INTERNAL_API_IP)) {
				sessionLocator.setServerIp("0.0.0.0");
			}
		}

		if (isDocumentInternalCommandIp(sessionLocator.getClientIp())) {
			if (sessionLocator.isIpv6()) {
				sessionLocator.setClientIpv6(sessionLocator.getServerIpv6());
			} else {
				sessionLocator.setClientIp(sessionLocator.getServerIp());
			}
		}
	}

	/**
	 * Validates server IP
	 * @param e
	 * @return
	 */
	private String getValidatedEventServerIp(Event e) {
		if (e.getField("server_ip") instanceof String) {
			String ip = e.getField("server_ip").toString();
			if (ip != null && inetAddressValidator.isValid(ip)) {
				return ip;
			}
		}
		return null;
	}

	/**
	 * Checks if the IP address is local or remote, returns true/false in case of local address
	 * @param ip
	 * @return
	 */
	private boolean isDocumentInternalCommandIp(String ip) {
		return ip != null && (LOCAL_IP_LIST.contains(ip) || ip.trim().equalsIgnoreCase(DOCUMENT_INTERNAL_API_IP));
	}

	/**
	 * Helper method to check if message contains any profiler key More efficient than multiple
	 * contains() calls
	 */
	private static boolean containsAnyProfilerKey(String message) {
		for (String key : PROFILER_KEYS) {
			if (message.contains(key)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Creates log string to be printed (optimized with StringBuilder)
	 *
	 * @param event
	 * @return
	 */
	private static String logEvent(Event event) {
		StringBuilder sb = new StringBuilder(256); // Pre-allocate reasonable size
		try {
			sb.append("{ ");
			boolean first = true;
			for (Map.Entry<String, Object> stringObjectEntry : event.getData().entrySet()) {
				if (!first) {
					sb.append(", ");
				}
				sb.append('"')
						.append(stringObjectEntry.getKey())
						.append("\": \"")
						.append(stringObjectEntry.getValue())
						.append('"');
				first = false;
			}
			sb.append(" }");
			return sb.toString();
		} catch (Exception e) {
			log.error("DocumentDB filter: Failed to create event log string", e);
			return "{ error: failed to serialize event }";
		}
	}

	private static boolean isProperlyClosedJson(String json) {
		if (json == null || json.isEmpty()) {
			return false;
		}

		// Trim whitespace
		String trimmed = json.trim();
		if (trimmed.isEmpty()) {
			return false;
		}

		char first = trimmed.charAt(0);
		char last = trimmed.charAt(trimmed.length() - 1);

		// Check if starts with { or [ and ends with matching } or ]
		if (first == '{' && last == '}') {
			return true;
		}
		if (first == '[' && last == ']') {
			return true;
		}

		// Not properly closed or not valid JSON structure
		return false;
	}

	/**
	 * Formats JsonSyntaxException message, truncating everything from the troubleshooting link
	 * onwards
	 *
	 * @param jse The JsonSyntaxException to format
	 * @return Formatted exception message (truncated before troubleshooting link if found, otherwise
	 *     just the exception message without stack trace)
	 */
	private static String formatJsonSyntaxException(JsonSyntaxException jse) {
		String message = jse.toString();
		int linkIndex =
				message.indexOf("See https://github.com/google/gson/blob/main/Troubleshooting.md");
		if (linkIndex != -1) {
			// Troubleshooting link found - truncate everything from this point onwards
			return message.substring(0, linkIndex).trim();
		}
		// No troubleshooting link - just return the exception message without full stack trace
		return message;
	}
}
