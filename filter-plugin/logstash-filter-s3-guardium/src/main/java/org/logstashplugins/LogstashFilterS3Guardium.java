/*
 *
 * Copyright 2020-2021 IBM Inc. All rights reserved
 * SPDX-License-Identifier: Apache2.0
 *
 */
package org.logstashplugins;

import co.elastic.logstash.api.Configuration;
import co.elastic.logstash.api.Context;
import co.elastic.logstash.api.Event;
import co.elastic.logstash.api.Filter;
import co.elastic.logstash.api.FilterMatchListener;
import co.elastic.logstash.api.LogstashPlugin;
import co.elastic.logstash.api.PluginConfigSpec;
import com.google.gson.*;
import com.ibm.guardium.s3.Parser;
import com.ibm.guardium.universalconnector.commons.GuardConstants;
import com.ibm.guardium.universalconnector.commons.structures.Record;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

// class name must match plugin name
@LogstashPlugin(name = "logstash_filter_s3_guardium")
public class LogstashFilterS3Guardium implements Filter {

    public static final String LOG42_CONF="log4j2uc.properties";

    static {
        try {
            String uc_etc = System.getenv("UC_ETC");
            LoggerContext context = (LoggerContext) LogManager.getContext(false);
            File file = new File(uc_etc +File.separator+LOG42_CONF);
            context.setConfigLocation(file.toURI());
        } catch (Exception e){
            System.err.println("Failed to load log4j configuration "+e.getMessage());
            e.printStackTrace();
        }
    }
    private static Logger log = LogManager.getLogger(LogstashFilterS3Guardium.class);

    public static final PluginConfigSpec<String> SOURCE_CONFIG = PluginConfigSpec.stringSetting("source", "message");
    public static final String LOGSTASH_TAG_S3_JSON_PARSE_ERROR = "_s3_json_parse_error";

    private String id;
    private Gson gson;

    public LogstashFilterS3Guardium(String id, Configuration config, Context context) {
        // constructors should validate configuration options
        // init log properties
        log.debug("Finished JavaOutputToGuardium constructor");
        this.id = id;
        final GsonBuilder builder = new GsonBuilder();
        builder.serializeNulls();
        gson = builder.create();

    }

    @Override
    public Collection<Event> filter(Collection<Event> events, FilterMatchListener matchListener) {
        ArrayList<Event> skippedEvents = new ArrayList<>();
        for (Event e : events) {
            try {
                if (log.isDebugEnabled()) {
                    log.debug("Event: "+logEvent(e));
                }
                JsonElement inputJSON = null;
                if (e.getField("detail") !=null ) {

                    String jsonDetailsProp = gson.toJson(e.getField("detail"));
                    if (log.isDebugEnabled()) { log.debug("sqs event 'detail' property value " + jsonDetailsProp); }
                    inputJSON = JsonParser.parseString(jsonDetailsProp);

                } else if (e.getField("cloudwatch_logs") !=null ) {

                    inputJSON = JsonParser.parseString(e.getField("message").toString());
                    if (log.isDebugEnabled()) { log.debug("cloudwatch_logs event 'message' property value " + inputJSON); }

                } else {

                    throw new Exception("Invalid event, no relevant for s3 parser properties found");
                }

                Record record = Parser.buildRecord(inputJSON);
                if (record==null){
                    log.warn("Failed to parse event "+logEvent(e));
                    continue;
                }

                e.setField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME, gson.toJson(record));

                matchListener.filterMatched(e); // Flag OK for filter input/parsing/out
                        
            } catch (Exception ex) {
                log.error("Error parsing jsonDetailsEvent  "+logEvent(e), ex);
                e.tag(LOGSTASH_TAG_S3_JSON_PARSE_ERROR);
            }
        }

        // Remove skipped mongodb events from reaching output
        // FIXME log which events skipped
        events.removeAll(skippedEvents);
        return events;
    }

    private static String logEvent(Event event){
        try {
            StringBuffer sb = new StringBuffer();
            sb.append("{ ");
            boolean first = true;
            for (Map.Entry<String, Object> stringObjectEntry : event.getData().entrySet()) {
                if (!first){
                    sb.append(",");
                }
                sb.append("\""+stringObjectEntry.getKey()+"\" : \""+stringObjectEntry.getValue()+"\"");
                first = false;
            }
            sb.append(" }");
            return sb.toString();
        } catch (Exception e){
            log.error("Failed to create event log string", e);
            return null;
        }
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
