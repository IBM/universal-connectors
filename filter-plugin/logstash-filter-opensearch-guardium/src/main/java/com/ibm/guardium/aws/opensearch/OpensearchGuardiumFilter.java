/*
Copyright IBM Corp. 2021, 2023 All rights reserved.
SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.guardium.aws.opensearch;

import co.elastic.logstash.api.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.ibm.guardium.universalconnector.commons.GuardConstants;
import com.ibm.guardium.universalconnector.commons.custom_parsing.ParserFactory;
import com.ibm.guardium.universalconnector.commons.structures.Record;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import static com.ibm.guardium.aws.opensearch.Constants.*;

@LogstashPlugin(name = "opensearch_guardium_filter")
public class OpensearchGuardiumFilter implements Filter {
    private static Logger logger = LogManager.getLogger(OpensearchGuardiumFilter.class);
    public static final PluginConfigSpec<String> SOURCE_CONFIG = PluginConfigSpec.stringSetting("source", "message");
    private String id;
    private Parser parser;

    public OpensearchGuardiumFilter(String id, Configuration config, Context context) {
        this.id = id;
        this.parser = new Parser(ParserFactory.ParserType.json);
    }

    @Override
    public Collection<PluginConfigSpec<?>> configSchema() {
        return Collections.singletonList(SOURCE_CONFIG);
    }

    /**
     * Returns the id
     *
     * @return id
     */
    @Override
    public String getId() {
        return this.id;
    }

    /**
     * Filters the received events by skipping the invalid ones and normalizing them by parsing the provided payloads into Guardium Generic Records.
     *
     * @param events              A list of received events
     * @param filterMatchListener The listener for this plugin
     * @return A list of normalized events
     */
    public Collection<Event> filter(Collection<Event> events, FilterMatchListener filterMatchListener) {
        ArrayList<Event> skippedEvents = new ArrayList<>();
        for (Event event : events) {
            if (logger.isDebugEnabled()) {
                logger.debug("Received event: {}", event.getData());
            }

            Object messageField = event.getField(MESSAGE);
            String messageString = messageField.toString();

            if (!CommonUtils.isJSONValid(messageString)) {
                event.tag(INVALID_MSG_OPENSEARCH);
                skippedEvents.add(event);
                continue;
            }
            try {
                JsonObject inputJSON = new Gson().fromJson(messageString, JsonObject.class);
                Record record = parser.parseRecord(String.valueOf(inputJSON));
                if (record == null) {
                    event.tag(INVALID_MSG_OPENSEARCH);
                    skippedEvents.add(event);
                    continue;
                }
                Gson gson = new GsonBuilder()
                        .disableHtmlEscaping()
                        .serializeNulls()
                        .create();

                event.setField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME, gson.toJson(record));
                filterMatchListener.filterMatched(event);
            } catch (Exception ex) {
                logger.error("Exception in parsing message: {}", event.getData(),
                        ex);
                event.tag(LOGSTASH_TAG_JSON_PARSE_ERROR);
            }

        }
        events.removeAll(skippedEvents);
        return events;
    }
}