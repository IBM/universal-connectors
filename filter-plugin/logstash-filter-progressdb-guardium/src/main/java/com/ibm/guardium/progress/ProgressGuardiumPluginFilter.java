//
// Copyright 2023 IBM All Rights Reserved.
// SPDX-License-Identifier: Apache2.0
//

package com.ibm.guardium.progress;

import java.io.File;
import java.util.Collection;
import java.util.Collections;

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

@LogstashPlugin(name = "progress_guardium_plugin_filter")
public class ProgressGuardiumPluginFilter implements Filter {

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
    private static Logger log = LogManager.getLogger(ProgressGuardiumPluginFilter.class);

    public ProgressGuardiumPluginFilter(String id, Configuration config, Context context) {
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

            try {
                JsonObject inputData = inputData(e);

                    Record record = Parser.parseRecord(inputData);

                    final GsonBuilder builder = new GsonBuilder();
                    builder.serializeNulls();
                    final Gson gson = builder.create();
                    e.setField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME, gson.toJson(record).toString());
                    matchListener.filterMatched(e);
              
            } catch (Exception exception) {
                log.error("Progress filter: Skip event " + e.getField(Constants.LOGSTASH_TAG_SKIP_NOT_PROGRESS), exception);
                e.tag(Constants.LOGSTASH_TAG_SKIP_NOT_PROGRESS);

            }
        }
        return events;
    }


    private JsonObject inputData(Event e) {
        JsonObject data = new JsonObject();

        if (e.getField(Constants.CLIENT_SESSION_ID) != null) {
            data.addProperty(Constants.CLIENT_SESSION_ID, e.getField(Constants.CLIENT_SESSION_ID).toString());
        }
        if (e.getField(Constants.USER_ID) != null) {
            data.addProperty(Constants.USER_ID, e.getField(Constants.USER_ID).toString());
        }
        if (e.getField(Constants.SERVER_IP) != null
                && !e.getField(Constants.SERVER_IP).toString().isEmpty()) {
            data.addProperty(Constants.SERVER_IP, e.getField(Constants.SERVER_IP).toString());
        }
        if (e.getField(Constants.CLIENT_IP) != null
                && !e.getField(Constants.CLIENT_IP).toString().isEmpty()) {
            data.addProperty(Constants.CLIENT_IP, e.getField(Constants.CLIENT_IP).toString());
        }
        if (e.getField(Constants.DB_PROTOCOL) != null
                && e.getField(Constants.DB_PROTOCOL).toString().isEmpty()) {
            data.addProperty(Constants.DB_PROTOCOL, e.getField(Constants.DB_PROTOCOL).toString());
        }
        if (e.getField(Constants.TIMESTAMP) != null
                && !e.getField(Constants.TIMESTAMP).toString().isEmpty()) {
            data.addProperty(Constants.TIMESTAMP, e.getField(Constants.TIMESTAMP).toString());
        }

        if (e.getField(Constants.DATABASE_NAME) != null && !e.getField(Constants.DATABASE_NAME).toString().isEmpty()) {
            data.addProperty(Constants.DATABASE_NAME, e.getField(Constants.DATABASE_NAME).toString());
        }
        if (e.getField(Constants.EVENT_NAME) != null
                && !e.getField(Constants.EVENT_NAME).toString().isEmpty()) {
            data.addProperty(Constants.EVENT_NAME, e.getField(Constants.EVENT_NAME).toString());
        }
        if (e.getField(Constants.EVENT_CONTEXT) != null
                && !e.getField(Constants.EVENT_CONTEXT).toString().isEmpty()) {
            data.addProperty(Constants.EVENT_CONTEXT, e.getField(Constants.EVENT_CONTEXT).toString());
        }
        if (e.getField(Constants.LOGIN_FAILED) != null
                && !e.getField(Constants.LOGIN_FAILED).toString().isEmpty()) {
            data.addProperty(Constants.LOGIN_FAILED, e.getField(Constants.LOGIN_FAILED).toString());
        }
        if (e.getField(Constants.SERVER_HOST) != null
                && !e.getField(Constants.SERVER_HOST).toString().isEmpty()) {
            data.addProperty(Constants.SERVER_HOST, e.getField(Constants.SERVER_HOST).toString());
        }
        if (e.getField(Constants.SERVER_PORT) != null
                && !e.getField(Constants.SERVER_PORT).toString().isEmpty()) {
            data.addProperty(Constants.SERVER_PORT, e.getField(Constants.SERVER_PORT).toString());
        }
        if (e.getField(Constants.CLIENT_PORT) != null
                && !e.getField(Constants.CLIENT_PORT).toString().isEmpty()) {
            data.addProperty(Constants.CLIENT_PORT, e.getField(Constants.CLIENT_PORT).toString());
        }


        return data;
    }

}

