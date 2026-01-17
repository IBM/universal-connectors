/*
© Copyright IBM Corp. 2021, 2022 All rights reserved.
SPDX-License-Identifier: Apache-2.0
*/
package com.ibm.guardium.documentdb;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.validator.routines.InetAddressValidator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ibm.guardium.universalconnector.commons.GuardConstants;
import com.ibm.guardium.universalconnector.commons.Util;
import com.ibm.guardium.universalconnector.commons.structures.Accessor;
import com.ibm.guardium.universalconnector.commons.structures.Construct;
import com.ibm.guardium.universalconnector.commons.structures.Data;
import com.ibm.guardium.universalconnector.commons.structures.ExceptionRecord;
import com.ibm.guardium.universalconnector.commons.structures.Record;
import com.ibm.guardium.universalconnector.commons.structures.Sentence;
import com.ibm.guardium.universalconnector.commons.structures.SentenceObject;
import com.ibm.guardium.universalconnector.commons.structures.SessionLocator;
import com.ibm.guardium.universalconnector.commons.structures.Time;
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
	private final static String DOCUMENTDB_AUDIT_SIGNAL = "\"atype\"";
	private final static String DOCUMENTDB_PROFILER_SIGNAL = "command";
	private final static String AGGR_KEY="aggregate";
	private final static String COUNT_KEY="count";
	private final static String DELETE_KEY="remove";
	private final static String INSERT_KEY="insert";
	private final static String UPDATE_KEY="update";
	private final static String DISTINCT_KEY="distinct";
	private final static String FIND_KEY="find";
	private final static String FINDANDMODIFY_KEY="findAndModify";
	public static final String LOGSTASH_TAG_JSON_PARSE_ERROR = "_documentdbguardium_json_parse_error";
	private final static Set<String> LOCAL_IP_LIST = new HashSet<>(Arrays.asList("127.0.0.1", "0:0:0:0:0:0:0:1"));
	private final static String DOCUMENT_INTERNAL_API_IP = "(NONE)";
	/*
     * skipping non-relevant log events like
     * "successful authenticate", and other events 
     * with blank db name
     */
    public static final String LOGSTASH_TAG_SKIP = "_documentdbguardium_skip";
	private static final InetAddressValidator inetAddressValidator = InetAddressValidator.getInstance();
	private static Logger log ;
	private String id;

	public DocumentdbGuardiumFilter(String id, Configuration config, Context context) {
		// constructors should validate configuration options
		this.id = id;
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
		if (log == null) {
			// instantiate default logger, as other Context can not coexsit alongside Guardium Universal Connector Logger context.
			log = LogManager.getLogger(DocumentdbGuardiumFilter.class);
		}
		String messageString=null;
		ArrayList<Event> skippedEvents = new ArrayList<>();
		for (Event e : events) {
			if (e.getField("message") instanceof String) {
				messageString = e.getField("message").toString();
				

				if (messageString.contains(DOCUMENTDB_AUDIT_SIGNAL)) {// This is an audit event
					try {
						JsonObject inputJSON = new Gson().fromJson(messageString, JsonObject.class);
						final String atype = inputJSON.get("atype").getAsString();
						final JsonObject param = inputJSON.get("param").getAsJsonObject();
                       if ((atype.equals("authenticate") && param.get("error").getAsString().equals("0") ) ||
				       	(param.has("ns") && param.get("ns").getAsString().isEmpty()))
						{
                            e.tag(LOGSTASH_TAG_SKIP);
							skippedEvents.add(e);
                            continue;
                        }
						Record record = Parser.parseAuditRecord(inputJSON);
						if(e.getField("serverHostnamePrefix") !=null && e.getField("serverHostnamePrefix") instanceof String) {
							record.getAccessor().setServerHostName(e.getField("serverHostnamePrefix").toString()+".aws.com");
							String dbName=record.getDbName();
							record.setDbName(!dbName.isEmpty()?e.getField("serverHostnamePrefix").toString()+":"+dbName:e.getField("serverHostnamePrefix").toString());
						}
						record.getAccessor().setServiceName(record.getDbName());
						if(e.getField("event_id") !=null && e.getField("event_id") instanceof String) {
							record.setSessionId(record.getSessionId()+e.getField("event_id").toString());
						}
						this.correctIPs(e, record);
						final GsonBuilder builder = new GsonBuilder();
						builder.serializeNulls();
						final Gson gson = builder.create();
						e.setField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME, gson.toJson(record));
						matchListener.filterMatched(e); // Flag OK for filter input/parsing/out
					} catch (Exception exception) {
						// don't let event pass filter
						// events.remove(e);
						log.error("DocumentDB filter: Error parsing docDb audit event " + logEvent(e), exception);
						e.tag(LOGSTASH_TAG_JSON_PARSE_ERROR);
					}
				} else if (messageString.contains(DOCUMENTDB_PROFILER_SIGNAL)) {// This is a profiler event
					try {
						if(messageString.contains(AGGR_KEY) || messageString.contains(COUNT_KEY) || messageString.contains(DELETE_KEY) || messageString.contains(DISTINCT_KEY)|| messageString.contains(FIND_KEY)|| messageString.contains(FINDANDMODIFY_KEY)||messageString.contains(INSERT_KEY)||messageString.contains(UPDATE_KEY))
						{
							JsonObject inputJSON = new Gson().fromJson(messageString, JsonObject.class);
							if ((!inputJSON.has("ns")) || (inputJSON.has("ns") && inputJSON.get("ns").getAsString().isEmpty()) )  {
	                            e.tag(LOGSTASH_TAG_SKIP);
								skippedEvents.add(e);
	                            continue;
	                        }
							Record record = Parser.parseProfilerRecord(inputJSON);
							if(e.getField("serverHostnamePrefix") !=null && e.getField("serverHostnamePrefix") instanceof String) {
								record.getAccessor().setServerHostName(e.getField("serverHostnamePrefix").toString()+".aws.com");
								String dbName=record.getDbName();
								record.setDbName(!dbName.isEmpty()?e.getField("serverHostnamePrefix").toString()+":"+dbName:e.getField("serverHostnamePrefix").toString());			
							}
							record.getAccessor().setServiceName(record.getDbName());
							if(e.getField("event_id") !=null && e.getField("event_id") instanceof String) {
								record.setSessionId(record.getSessionId()+e.getField("event_id").toString());
							}
							this.correctIPs(e, record);
							final GsonBuilder builder = new GsonBuilder();
							builder.serializeNulls();
							final Gson gson = builder.disableHtmlEscaping().create();
							e.setField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME, gson.toJson(record));
							if (record.getDbName().equals(Parser.UNKOWN_STRING))  {
	                            e.tag(LOGSTASH_TAG_SKIP);
								skippedEvents.add(e);
	                            continue;
	                        }
							matchListener.filterMatched(e); // Flag OK for filter input/parsing/out
						}
					}catch (Exception exception) {
						// don't let event pass filter
						// events.remove(e);
						log.error("DocumentDB filter: Error parsing docDb profiler event " + logEvent(e), exception);
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
	 * Creates log string to be printed
	 * @param event
	 * @return
	 */
	private static String logEvent(Event event) {
		StringBuffer sb = new StringBuffer();
		try {
			sb.append("{ ");
			boolean first = true;
			for (Map.Entry<String, Object> stringObjectEntry : event.getData().entrySet()) {
				if (!first) {
					sb.append(",");
				}
				sb.append("\"" + stringObjectEntry.getKey() + "\" : \"" + stringObjectEntry.getValue() + "\"");
				first = false;
			}
			sb.append(" }");
			return sb.toString();
		} catch (Exception e) {
			log.error("DocumentDB filter: Failed to create event log string", e);
			return null;
		}
	}

}
