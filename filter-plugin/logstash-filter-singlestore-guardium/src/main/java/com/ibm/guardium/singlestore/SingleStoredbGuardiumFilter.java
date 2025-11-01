// Copyright 2020-2021 IBM Inc. All rights reserved
// SPDX-License-Identifier: Apache2.0
//

package com.ibm.guardium.singlestore;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.ibm.guardium.universalconnector.commons.GuardConstants;
import com.ibm.guardium.universalconnector.commons.structures.Record;
import co.elastic.logstash.api.Configuration;
import co.elastic.logstash.api.Context;
import co.elastic.logstash.api.Event;
import co.elastic.logstash.api.Filter;
import co.elastic.logstash.api.FilterMatchListener;
import co.elastic.logstash.api.LogstashPlugin;
import co.elastic.logstash.api.PluginConfigSpec;

@LogstashPlugin(name = "singlestoredb_guardium_filter")
public class SingleStoredbGuardiumFilter implements Filter {

	public static final String LOG42_CONF = "log4j2uc.properties";
	static {
		try {
			String uc_etc = System.getenv("UC_ETC");
			if (uc_etc != null) {
				LoggerContext context = (LoggerContext) LogManager.getContext(false);
				File file = new File(uc_etc + File.separator + LOG42_CONF);
				context.setConfigLocation(file.toURI());
			}
		} catch (Exception e) {
			System.out.println("====: " + e.getMessage());
		}
	}

	final private String id;
	public static final PluginConfigSpec<String> SOURCE_CONFIG = PluginConfigSpec.stringSetting("source", "message");
	final private static Logger log = LogManager.getLogger(SingleStoredbGuardiumFilter.class);

	public SingleStoredbGuardiumFilter(String id, Configuration config, Context context) {
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

	@Override
	public Collection<Event> filter(Collection<Event> events, FilterMatchListener matchListener) {
		if (events == null || matchListener == null) {
			log.error("Null events or matchListener provided to filter");
			return events;
		}

		for (Event e : events) {
			if (e == null) {
				continue;
			}

			if (log.isDebugEnabled()) {
				log.debug("Event Now: {}", e.getData());
			}

			try {
				Record record;

				final GsonBuilder builder = new GsonBuilder();
				final Gson gson = builder.create();
				builder.serializeNulls();

				// Check if message field exists
				if (e.getField("message") == null) {
					log.error("Event missing 'message' field");
					e.tag(Constants.LOGSTASH_TAG_JSON_PARSE_ERROR);
					continue;
				}

				getParsedEvent(e.getField("message").toString(), e);

				JsonObject inputData = inputData(e);
				if (inputData == null) {
					log.error("Failed to create inputData from event");
					e.tag(Constants.LOGSTASH_TAG_JSON_PARSE_ERROR);
					continue;
				}

				if (isFailedLogin(inputData)) {
					record = Parser.parseExceptionRecord(inputData);
				} else {
					record = Parser.parseRecord(inputData);
				}

				if (record != null) {
					e.setField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME, gson.toJson(record));
					matchListener.filterMatched(e);
					log.info("==========>Final JSON to be send to Guardium: {}", gson.toJson(record));
					System.out.println("==========>Final JSON to be send to Guardium: " + gson.toJson(record));
				} else {
					log.error("Failed to create record from event");
					e.tag(Constants.LOGSTASH_TAG_JSON_PARSE_ERROR);
				}
			} catch (Exception exception) {
				log.error("SingleStore filter: Error parsing SingleStore event", exception);
				e.tag(Constants.LOGSTASH_TAG_JSON_PARSE_ERROR);
			}
		}
		return events;
	}

	private boolean isFailedLogin(JsonObject inputData) {
		if (inputData == null || !inputData.has("message") || inputData.get("message").isJsonNull()) {
			return false;
		}

		try {
			String message = inputData.get("message").getAsString();
			Map<String, String> logMap = SingleStoreLogFormat.parseLog(message);

			String eventType = logMap.get(SingleStoreLogFormat.EVENT_TYPE);
			String loginStatus = logMap.get(SingleStoreLogFormat.LOGIN_STATUS);

			return eventType != null && loginStatus != null &&
					SingleStoreLogFormat.USER_LOGIN.equals(eventType) &&
					SingleStoreLogFormat.LOGIN_FAILURE.equals(loginStatus);
		} catch (Exception e) {
			log.error("Error checking for failed login: {}", e.getMessage());
			return false;
		}
	}

	private JsonObject inputData(Event e) {
		if (e == null) {
			log.error("Null event provided to inputData");
			return null;
		}

		JsonObject data = new JsonObject();

		try {
			addPropertyIfNotNull(data, Constants.TIMESTAMP, e, Constants.TIMESTAMP, true);
			addPropertyIfNotNull(data, Constants.CLIENT_IP, e, Constants.CLIENT_IP, false);
			addPropertyIfNotNull(data, Constants.SERVER_IP, e, Constants.SERVER_IP, false);
			addPropertyIfNotNull(data, Constants.SERVER_HOSTNAME, e, Constants.SERVER_HOSTNAME, false);
			addPropertyIfNotNull(data, Constants.SERVER_PORT, e, Constants.SERVER_PORT, false);
			addPropertyIfNotNull(data, Constants.DB_USER, e, Constants.DB_USER, false);
			addPropertyIfNotNull(data, Constants.DB_NAME, e, Constants.DB_NAME, false);
			addPropertyIfNotNull(data, Constants.QUERY_STATEMENT, e, Constants.QUERY_STATEMENT, false);
			addPropertyIfNotNull(data, Constants.MESSAGE, e, Constants.MESSAGE, false);
		} catch (Exception ex) {
			log.error("Error creating inputData: {}", ex.getMessage());
		}

		return data;
	}

	// Helper method to add property if not null
	private void addPropertyIfNotNull(JsonObject data, String propertyName, Event e, String fieldName, boolean isTimestamp) {
		if (data == null || propertyName == null || e == null || fieldName == null) {
			return;
		}

		try {
			Object fieldValue = e.getField(fieldName);
			if (fieldValue != null) {
				String value = fieldValue.toString();
				if (value != null && !value.isEmpty()) {
					if (isTimestamp) {
						value = value.replace("UTC", "Z");
					}
					data.addProperty(propertyName, value);
				}
			}
		} catch (Exception ex) {
			log.error("Error adding property {}: {}", propertyName, ex.getMessage());
		}
	}

	public static void getParsedEvent(String logEvent, Event event) {
		if (logEvent == null || event == null) {
			log.error("Null logEvent or event provided to getParsedEvent");
			return;
		}

		try {
			Map<String, String> logMap = SingleStoreLogFormat.parseLog(logEvent);

			if (logMap.isEmpty()) {
				log.error("Failed to parse log event: {}", logEvent);
				return;
			}

			event.setField("message", logEvent);

			// Set fields with null checks
			String dbName = logMap.get(SingleStoreLogFormat.DB_NAME);
			if (dbName != null) {
				event.setField(Constants.DB_NAME, dbName);
			} else {
				event.setField(Constants.DB_NAME, "");
			}

			event.setField(Constants.CLIENT_IP, Constants.CLIENT_IP_VALUE);

			String eventType = logMap.get(SingleStoreLogFormat.EVENT_TYPE);
			if (eventType != null && SingleStoreLogFormat.USER_LOGIN.equals(eventType)) {
				if (dbName != null) {
					event.setField(Constants.CLIENT_IP, dbName);
				}
				event.setField(Constants.DB_NAME, "");
			}

			if (event.getData() != null) {
				if (event.getData().containsKey("serverIP")) {
					Object serverIP = event.getField("serverIP");
					if (serverIP != null) {
						event.setField(Constants.SERVER_IP, serverIP.toString());
					}
				}

				if (event.getData().containsKey("serverHostname")) {
					Object serverHostname = event.getField("serverHostname");
					if (serverHostname != null) {
						event.setField(Constants.SERVER_HOSTNAME, serverHostname.toString());
					}
				}
			}

			String serverPort = logMap.get(SingleStoreLogFormat.SERVER_PORT);
			if (serverPort != null) {
				event.setField(Constants.SERVER_PORT, serverPort);
			}

			String timestampDate = logMap.get(SingleStoreLogFormat.TIMESTAMP_DATE);
			String timestampTime = logMap.get(SingleStoreLogFormat.TIMESTAMP_TIME);
			if (timestampDate != null && timestampTime != null) {
				event.setField(Constants.TIMESTAMP, timestampDate + " " + timestampTime);
			}

			String dbUser = logMap.get(SingleStoreLogFormat.DB_USER);
			if (dbUser != null) {
				event.setField(Constants.DB_USER, dbUser);
			}

			String query = logMap.get(SingleStoreLogFormat.QUERY);
			if (query != null) {
				event.setField(Constants.QUERY_STATEMENT, query);
			}
		} catch (Exception e) {
			log.error("getParsedEvent Function {}", e.getMessage());
		}
	}
}