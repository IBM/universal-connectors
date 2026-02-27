//
// Copyright 2020-2021 IBM Inc. All rights reserved
// SPDX-License-Identifier: Apache2.0
//
package com.ibm.guardium.cassandra;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.apache.commons.collections.map.HashedMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ibm.guardium.universalconnector.commons.GuardConstants;
import com.ibm.guardium.universalconnector.commons.structures.Record;

import co.elastic.logstash.api.Configuration;
import co.elastic.logstash.api.Context;
import co.elastic.logstash.api.Event;
import co.elastic.logstash.api.Filter;
import co.elastic.logstash.api.FilterMatchListener;
import co.elastic.logstash.api.LogstashPlugin;
import co.elastic.logstash.api.PluginConfigSpec;

//class name must match plugin name
@LogstashPlugin(name = "cassandra_guardium_plugin_filter")
public class CassandraGuardiumPluginFilter implements Filter {

	public static final String LOG42_CONF = "log4j2uc.properties";
	static {
		try {
			String uc_etc = System.getenv("UC_ETC");
			LoggerContext context = (LoggerContext) LogManager.getContext(false);
			File file = new File(uc_etc + File.separator + LOG42_CONF);
			context.setConfigLocation(file.toURI());
		} catch (Exception e) {
			System.err.println("Failed to load log4j configuration " + e.getMessage());
			e.printStackTrace();
		}
	}

	private String id;
	private Parser parser;
	public static final PluginConfigSpec<String> SOURCE_CONFIG = PluginConfigSpec.stringSetting("source", "message");
	private static Logger log = LogManager.getLogger(CassandraGuardiumPluginFilter.class);

	public CassandraGuardiumPluginFilter(String id, Configuration config, Context context) {
		// constructors should validate configuration options
		this.id = id;
		this.parser = new Parser();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Collection<Event> filter(Collection<Event> events, FilterMatchListener matchListener) {
		for (Event e : events) {
			if (e.getField(Constants.MESSAGE) instanceof String) {
				String input = e.getField(Constants.MESSAGE).toString();
				try {
					Map<String, String> dataMap = new HashedMap();

					dataMap.put(Constants.SERVER_IP, e.getField(Constants.SERVER_IP).toString());
					dataMap.put(Constants.SERVER_HOSTNAME, e.getField(Constants.SERVER_HOSTNAME).toString());
					String[] intermediate_input = input.split(Constants.INPUT_SPLIT1);
					String[] secondary_input = intermediate_input[1].split(Constants.INPUT_SPLIT2);

					for (String input_value : secondary_input) {

						String[] keyValue = input_value.split(Constants.INPUT_SPLIT3,Constants.limit);
						dataMap.put(keyValue[0], keyValue[1]);
					}
					Record record = parser.parseRecord(dataMap);
					final GsonBuilder builder = new GsonBuilder();
					builder.serializeNulls();
					final Gson gson = builder.create();
					e.setField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME, gson.toJson(record));
					matchListener.filterMatched(e);
				} catch (Exception exception) {
					log.error("Cassandradb Filter: Error Parsing Event " + exception);
					e.tag(Constants.LOGSTASH_TAG_JSON_PARSE_ERROR);
				}
			} else {
				log.error("Given Event Is Not An Instance Of String " + e.getField(Constants.MESSAGE));
				e.tag(Constants.LOGSTASH_TAG_SKIP_NOT_CASSANDRA_DB);
			}
		}
		return events;
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
}
