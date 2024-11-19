/*
Copyright IBM Corp. 2021, 2024 All rights reserved.
SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.guardium.bigtable;

import co.elastic.logstash.api.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.ibm.guardium.universalconnector.commons.GuardConstants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

@LogstashPlugin(name = "big_table_guardium_filter")
public class BigTableGuardiumFilter implements Filter {

    public static final PluginConfigSpec<String> SOURCE_CONFIG = PluginConfigSpec.stringSetting("source", "message");
    private static Logger logger = LogManager.getLogger(BigTableGuardiumFilter.class);
    private String id;
    private String sourceField;

    /**
     * BigTableGuardiumFilter() method used to set SOURCE_CONFIG and context for
     * Events
     *
     * @param config
     * @param context
     * @return
     * @methodName SpannerFilterConnector
     */
    public BigTableGuardiumFilter(String id, Configuration config, Context context) {
        this.id = id;
        this.sourceField = config.get(SOURCE_CONFIG);
    }

    /**
     * configSchema() method will get the iD
     *
     * @return Collection<PluginConfigSpec < ?>>
     * @methodName @configSchema
     */
    @Override
    public Collection<PluginConfigSpec<?>> configSchema() {
        return Collections.singletonList(SOURCE_CONFIG);
    }

    /**
     * getId() method will get the iD
     *
     * @return String
     * @methodName @getId
     */
    @Override
    public String getId() {
        return this.id;
    }

    /**
     * filter() method will perform operation on input, get data and some
     * validations performed to check the input parameter and then call method which
     * convert the input as a JSON and and convert json object into Map for Guardium
     * Object and return response as per the requirement.
     *
     * @param events
     * @param filterMatchListener
     * @return Collection<Event>
     * @methodName @filter
     */
    @Override
    public Collection<Event> filter(Collection<Event> events, FilterMatchListener filterMatchListener) {
        ArrayList<Event> skippedEvents = new ArrayList<>();

        for (var e : events) {
            if (logger.isDebugEnabled()) {
                logger.debug("Event Now: {}", e.getData());
            }

            var messageField = e.getField(ApplicationConstants.MESSAGE);

            if (isValidMessage(messageField)) {
                String messageString = messageField.toString();

                if (!CommonUtils.isJSONValid(messageString)) {
                    e.tag(ApplicationConstants.LOGSTASH_TAG_SKIP_NOT_GCP);
                    skippedEvents.add(e);
                    continue;
                }

                try {
                    var inputJSON = new Gson().fromJson(messageString, JsonObject.class);

                    if (isBigTableMessage(inputJSON)) {

                        var record = Parser.parseRecord(inputJSON);
                        if (record == null) {
                            e.tag(ApplicationConstants.LOGSTASH_TAG_INVALID_BIGTABLE);
                            skippedEvents.add(e);
                            continue;
                        }

                        var gson = new GsonBuilder()
                                .disableHtmlEscaping()
                                .serializeNulls()
                                .create();

                        e.setField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME, gson.toJson(record));
                        filterMatchListener.filterMatched(e);
                    } else {
                        e.tag(ApplicationConstants.LOGSTASH_TAG_SKIP_NOT_GCP);
                        skippedEvents.add(e);
                    }

                } catch (Exception ex) {
                    logger.error("Found exception in parsing JSON and event that caused exception: {}", e.getData(),
                            ex);
                    e.tag(ApplicationConstants.LOGSTASH_TAG_JSON_PARSE_ERROR);
                }

            } else {
                e.tag(ApplicationConstants.LOGSTASH_TAG_SKIP_NOT_GCP);
            }
        }

        events.removeAll(skippedEvents);
        return events;
    }

    private boolean isBigTableMessage(JsonObject inputJSON) {
        if (inputJSON.has(ApplicationConstants.PROTO_PAYLOAD)) {
            var protoPayload = inputJSON.getAsJsonObject(ApplicationConstants.PROTO_PAYLOAD);
            if (protoPayload != null && protoPayload.has(ApplicationConstants.SERVICE_NAME)) {
                String serviceName = protoPayload.get(ApplicationConstants.SERVICE_NAME).getAsString();
                if (serviceName.contains(ApplicationConstants.BIGTABLE_API)
                        || serviceName.contains(ApplicationConstants.BIGTABLE_ADMIN_API)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isValidMessage(Object messageField) {
        return messageField instanceof String
                && (String.valueOf(messageField).contains(ApplicationConstants.BIGTABLE_API)
                        || String.valueOf(messageField).contains(ApplicationConstants.BIGTABLE_ADMIN_API));
    }
}
