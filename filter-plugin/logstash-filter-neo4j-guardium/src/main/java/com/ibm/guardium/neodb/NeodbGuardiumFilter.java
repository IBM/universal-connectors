//
// Copyright 2024-2025 IBM Inc. All rights reserved
// SPDX-License-Identifier: Apache2.0
//

package com.ibm.guardium.neodb;

import java.io.File;
import java.text.ParseException;
import java.util.Collection;
import java.util.Collections;
import com.google.gson.JsonElement;
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

@LogstashPlugin(name = "neodb_guardium_filter")
public class NeodbGuardiumFilter implements Filter {

	private static Logger logger = LogManager.getLogger(NeodbGuardiumFilter.class);


	public static final String LOG42_CONF = "log4j2uc.properties";
	static {
		try {
			String uc_etc = System.getenv("UC_ETC");
			LoggerContext context = (LoggerContext) LogManager.getContext(false);
			File file = new File(uc_etc + File.separator + LOG42_CONF);
			context.setConfigLocation(file.toURI());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private String id;
	public static final PluginConfigSpec<String> SOURCE_CONFIG = PluginConfigSpec.stringSetting("source", "message");
	private static Logger log = LogManager.getLogger(NeodbGuardiumFilter.class);
    Parser parser = new Parser();
	public NeodbGuardiumFilter(String id, Configuration config, Context context) {
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

		for (Event e : events) {
			if(logger.isDebugEnabled()){
				logger.debug("Event Now: {}", e.getData());
			}

			try {
				JsonObject inputData = inputData(e);

				if (isNotSystemGeneratedEvent(inputData)) {

					Record record = Parser.parseRecord(inputData);

					final GsonBuilder builder = new GsonBuilder();
					builder.serializeNulls();
					final Gson gson = builder.create();
					e.setField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME, gson.toJson(record));

					matchListener.filterMatched(e);
				} else {
					e.tag(Constants.LOGSTASH_TAG_SKIP_NOT_NEO);
				}

			} catch (ParseException pe) {
				log.error("Neo4j filter: Error parsing neo4j event " + e.getField(Constants.MESSAGE).toString(), pe);
				e.tag(Constants.LOGSTASH_TAG_JSON_PARSE_ERROR);
			} catch (Exception exception) {
				log.error("Neo4j filter: Error parsing neo4j event " + e.getField(Constants.MESSAGE).toString(),
						exception);
				e.tag(Constants.LOGSTASH_TAG_JSON_PARSE_ERROR);
			}
		}
		return events;
	}

	private boolean isNotSystemGeneratedEvent(JsonObject inputData) {
		if (inputData == null || !inputData.has(Constants.QUERY_STATEMENT)) {
			return false;  // Treat as system-generated if missing QUERY_STATEMENT or inputData is null
		}

		JsonElement queryElement = inputData.get(Constants.QUERY_STATEMENT);
		if (queryElement == null || queryElement.isJsonNull()) {
			return false;  // Treat as system-generated if queryElement is null or JsonNull
		}

		String queryStatement = queryElement.getAsString();
		if (queryStatement == null || queryStatement.isEmpty() || queryStatement.startsWith("EXPLAIN")) {
			return false;  // Treat as system-generated if queryStatement is null, empty, or starts with "EXPLAIN"
		}

		return true;  // Otherwise, it is not a system-generated event
	}

	private JsonObject inputData(Event e) {
		JsonObject data = new JsonObject();

		if (e.getField(Constants.CLIENT_IP) != null
				&& !e.getField(Constants.CLIENT_IP).toString().isEmpty()) {
			data.addProperty(Constants.CLIENT_IP, e.getField(Constants.CLIENT_IP).toString());
		}
		if (e.getField(Constants.SERVER_IP) != null
				&& !e.getField(Constants.SERVER_IP).toString().isEmpty()) {
			data.addProperty(Constants.SERVER_IP, e.getField(Constants.SERVER_IP).toString());
		}
		if (e.getField(Constants.DB_PROTOCOL) != null
				&& e.getField(Constants.DB_PROTOCOL).toString().isEmpty()) {
			data.addProperty(Constants.DB_PROTOCOL, e.getField(Constants.DB_PROTOCOL).toString());
		}
		if (e.getField(Constants.TIMESTAMP) != null
				&& !e.getField(Constants.TIMESTAMP).toString().isEmpty()) {
			data.addProperty(Constants.TIMESTAMP, e.getField(Constants.TIMESTAMP).toString());
		}
		if (e.getField(Constants.LOG_LEVEL) != null
				&& !e.getField(Constants.LOG_LEVEL).toString().isEmpty()) {
			data.addProperty(Constants.LOG_LEVEL, e.getField(Constants.LOG_LEVEL).toString());
		}
		if (e.getField(Constants.DB_USER) != null && !e.getField(Constants.DB_USER).toString().isEmpty()) {
			data.addProperty(Constants.DB_USER, e.getField(Constants.DB_USER).toString());
		}
		if (e.getField(Constants.DB_NAME) != null && !e.getField(Constants.DB_NAME).toString().isEmpty()) {
			data.addProperty(Constants.DB_NAME, e.getField(Constants.DB_NAME).toString());
		}
		if (e.getField(Constants.SOURCE_PROGRAM) != null
				&& !e.getField(Constants.SOURCE_PROGRAM).toString().isEmpty()) {
			data.addProperty(Constants.SOURCE_PROGRAM, e.getField(Constants.SOURCE_PROGRAM).toString());
		}
		if (e.getField(Constants.QUERY_STATEMENT) != null
				&& !e.getField(Constants.QUERY_STATEMENT).toString().isEmpty()) {
			data.addProperty(Constants.QUERY_STATEMENT, e.getField(Constants.QUERY_STATEMENT).toString());
		}
		if (e.getField(Constants.SERVER_HOSTNAME) != null
				&& !e.getField(Constants.SERVER_HOSTNAME).toString().isEmpty()) {
			data.addProperty(Constants.SERVER_HOSTNAME, e.getField(Constants.SERVER_HOSTNAME).toString());
		}
		if (e.getField(Constants.MESSAGE) != null && !e.getField(Constants.MESSAGE).toString().isEmpty()) {
			data.addProperty(Constants.MESSAGE, e.getField(Constants.MESSAGE).toString());
		}
		return data;
	}

}