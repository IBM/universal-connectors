
/*
#Copyright 2020-2021 IBM Inc. All rights reserved
#SPDX-License-Identifier: Apache-2.0
#*/
package com.ibm.guardium.s3sqspostgresql;

import java.io.File;
import java.util.Collection;
import java.util.Collections;


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
@LogstashPlugin(name = "s3sqs_postgresql_guardium_plugin_filter")

public class S3SQSPostgresqlGuardiumPluginFilter implements Filter {

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
	
	public static final PluginConfigSpec<String> SOURCE_CONFIG = PluginConfigSpec.stringSetting("source", "message");
	
	private static Logger log = LogManager.getLogger(S3SQSPostgresqlGuardiumPluginFilter.class);

	public S3SQSPostgresqlGuardiumPluginFilter(String id, Configuration config, Context context) {

		// constructors should validate configuration options

		this.id = id;
	}

	@Override

	public Collection<Event> filter(Collection<Event> events, FilterMatchListener matchListener) {

		for (Event e : events) {
			if(null != e && null != e.getData()){
				try {
					log.debug("Event Now: {} " + e.getData());

					Record record = Parser.parseRecord(e);

					final GsonBuilder builder = new GsonBuilder();

					builder.serializeNulls();

					final Gson gson = builder.create();

					e.setField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME, gson.toJson(record));

					matchListener.filterMatched(e);

				} catch (Exception exception) {
					log.warn("Failed event {}" , e.getData());
				}
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
