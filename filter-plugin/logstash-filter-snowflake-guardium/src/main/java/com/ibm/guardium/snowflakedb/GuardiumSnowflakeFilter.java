//
// Copyright 2021-2023 IBM Inc. All rights reserved
// SPDX-License-Identifier: Apache2.0
//

package com.ibm.guardium.snowflakedb;

import co.elastic.logstash.api.Configuration;
import co.elastic.logstash.api.Context;
import co.elastic.logstash.api.Event;
import co.elastic.logstash.api.Filter;
import co.elastic.logstash.api.FilterMatchListener;
import co.elastic.logstash.api.LogstashPlugin;
import co.elastic.logstash.api.PluginConfigSpec;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import com.ibm.guardium.snowflakedb.exceptions.ParseException;
import com.ibm.guardium.snowflakedb.parser.AuthFailedEventParser;
import com.ibm.guardium.snowflakedb.parser.ErrorRecordBuilder;
import com.ibm.guardium.snowflakedb.parser.Parser;
import com.ibm.guardium.snowflakedb.parser.SQLErrorEventParser;
import com.ibm.guardium.snowflakedb.parser.SuccessEventParser;
import com.ibm.guardium.snowflakedb.utils.Constants;
import com.ibm.guardium.universalconnector.commons.GuardConstants;
import com.ibm.guardium.universalconnector.commons.structures.Record;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.google.gson.*;


// class name must match plugin name
@LogstashPlugin(name = "guardium_snowflake_filter")
public class GuardiumSnowflakeFilter implements Filter {

    private static Logger log = LogManager.getLogger(GuardiumSnowflakeFilter.class);

    public static final PluginConfigSpec<String> SOURCE_CONFIG = PluginConfigSpec.stringSetting("message");

    public GuardiumSnowflakeFilter(String id, Configuration config, Context context) {
        this.id = id;
    }

    private String id;

    @Override
    public Collection<Event> filter(Collection<Event> events, FilterMatchListener matchListener) {
        final GsonBuilder builder = new GsonBuilder();
        builder.serializeNulls();
        final Gson gson = builder.create();

        Parser parser = null;

        ArrayList<Event> skippedEvents = new ArrayList<>();
        for (Event event : events) {
            Record partialRecord = null;
            try {
                Optional<String> optEventType = Optional.ofNullable(
                        event.getField(Constants.EVENT_TYPE)
                ).map(Object::toString);

                if(optEventType.isPresent()){
                    String eventType = optEventType.get();
                    if(log.isDebugEnabled()){
                        log.info("Snowflake Filter - Event Type: {}", eventType);
                    }

                    switch (eventType.toUpperCase()){
                        case Constants.SQL_ERROR:
                            parser = new SQLErrorEventParser();
                            break;
                        case Constants.LOGIN_FAILED:
                            parser = new AuthFailedEventParser();
                            break;
                        case Constants.SUCCESS:
                            parser = new SuccessEventParser();
                            break;
                    }

                    if(parser != null){
                        Record rec = parser.parseRecord(event.toMap());
                        if(rec.getAccessor().getDbUser() == null || rec.getAccessor().getDbUser().equals("")){
                            event.tag(Constants.LOGSTASH_TAG_SKIP_NOT_SNOWFLAKE);
                            skippedEvents.add(event);
                        }
                        else{
                            event.setField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME, gson.toJson(rec));
                        }
                    } else {
                        // Unknown event_type - this is an audit/configuration error
                        throw new Exception("Unknown event_type - parser not initialized.");
                    }
                }
            } catch (ParseException parseException) {
                // Handle parser errors
                // ParseException is thrown during parsing, so we may have partial data
                log.error("Snowflake filter: Parser error - {}", parseException.getMessage());
                Record errorRecord = ErrorRecordBuilder.parseRecordException(event, partialRecord, parseException.getMessage());
                event.setField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME, gson.toJson(errorRecord));
                event.tag(Constants.LOGSTASH_TAG_JSON_PARSE_ERROR);
            } catch (Exception exception) {
                // Handle generic errors
                // Generic exceptions may occur at any point, check if we have partial data
                log.error("Snowflake filter: Error parsing event {}", exception);
                Record errorRecord = ErrorRecordBuilder.parseRecordException(event, partialRecord, exception.getMessage());
                event.setField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME, gson.toJson(errorRecord));
                event.tag(Constants.LOGSTASH_TAG_JSON_PARSE_ERROR);
            }
        }
        events.removeAll(skippedEvents);
        return events;
    }

    @Override
    public Collection<PluginConfigSpec<?>> configSchema() {
        return Collections.singletonList(SOURCE_CONFIG);
    }

    @Override
    public String getId() {
        return id;
    }

}
