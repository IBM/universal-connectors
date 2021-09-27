//
// Copyright 2020-2021 IBM Inc. All rights reserved
// SPDX-License-Identifier: Apache2.0
//
package com.ibm.guardium.dynamodb;

import co.elastic.logstash.api.Configuration;
import co.elastic.logstash.api.Context;
import co.elastic.logstash.api.Event;
import co.elastic.logstash.api.Filter;
import co.elastic.logstash.api.FilterMatchListener;
import co.elastic.logstash.api.LogstashPlugin;
import co.elastic.logstash.api.PluginConfigSpec;
import com.google.gson.*;
import com.ibm.guardium.universalconnector.commons.GuardConstants;
import com.ibm.guardium.universalconnector.commons.Util;
import com.ibm.guardium.universalconnector.commons.structures.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;

import java.io.File;
import java.util.*;

//class name must match plugin name
@LogstashPlugin(name = "dynamodb_guardium_plugin_filter")
public class DynamodbGuardiumPluginFilter implements Filter{

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
    private static Logger log = LogManager.getLogger(DynamodbGuardiumPluginFilter.class);

    public DynamodbGuardiumPluginFilter(String id, Configuration config, Context context) {
        // constructors should validate configuration options
        this.id = id;
    }

	@Override
	public Collection<Event> filter(Collection<Event> events, FilterMatchListener matchListener) {

		for (Event e : events) {
			if (e.getField("message") instanceof String) {
				String input = e.getField("message").toString();
	            try {
	            	JsonObject inputJSON = (JsonObject) JsonParser.parseString(input);
	            	Record record = Parser.parseRecord(inputJSON);
	            	final GsonBuilder builder = new GsonBuilder();
	            	builder.serializeNulls();
	            	final Gson gson = builder.create();
	            	e.setField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME, gson.toJson(record));

	            	matchListener.filterMatched(e); 
	            }
	            catch (Exception exception) {
	            	log.error("Dynamodb filter: Error parsing dynamo event "+ exception);
	            	e.tag(Constants.LOGSTASH_TAG_JSON_PARSE_ERROR);
	            }
	        }
			else {
				e.tag(Constants.LOGSTASH_TAG_SKIP_NOT_DYNAMO);
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
