/*
Copyright IBM Corp. 2021, 2023 All rights reserved.
SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.guardium.databricks;

import co.elastic.logstash.api.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.ibm.guardium.universalconnector.commons.GuardConstants;
import com.ibm.guardium.universalconnector.commons.structures.Record;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import static com.ibm.guardium.databricks.Constants.*;

@LogstashPlugin(name = "databricks_guardium_filter")
public class DatabricksGuardiumFilter implements Filter {

    private static Logger logger = LogManager.getLogger(DatabricksGuardiumFilter.class);

    private String id;
    private String filter;
    private Parser parser;
    public static final PluginConfigSpec<String> SOURCE_CONFIG = PluginConfigSpec.stringSetting("source", "message");

    public DatabricksGuardiumFilter(String id, String filter, Configuration config, Context context) {
        this(id, config, context);
        this.filter = filter;
    }

    public DatabricksGuardiumFilter(String id, Configuration config, Context context) {
        this.id = id;
        this.parser = new Parser();
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
     * Filters the received events by skipping the invalid ones and normalizing them
     * by parsing the provided payloads into Guardium Generic Records.
     *
     * @param events              A list of received events
     * @param filterMatchListener The listener for this plugin
     * @return A list of normalized events
     */
    public Collection<Event> filter(Collection<Event> events, FilterMatchListener filterMatchListener) {
        ArrayList<Event> skippedEvents = new ArrayList<>();

        for (Event e : events) {
            if (logger.isDebugEnabled()) {
                logger.debug("Event Now: {}", e.getData());
            }
            if (!(e.getField(MESSAGE) instanceof String)
                    || (filter != null && !String.valueOf(e.getField(MESSAGE)).contains(filter))) {
                e.tag(INVALID_MSG);
            } else {
                try {
                    JsonObject input = new Gson().fromJson(e.getField(MESSAGE).toString(), JsonObject.class);
                    Record record = this.parser.parseRecord(input);
                    final Gson gson = new GsonBuilder().disableHtmlEscaping().serializeNulls().create();
                    e.setField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME, gson.toJson(record));
                    filterMatchListener.filterMatched(e);
                    logger.debug("Record pardsed: {}", record.toString());

                } catch (Exception ex) {
                    logger.error("Exception occurred while parsing record: ", e.getField(MESSAGE));
                }
            }
        }
        events.removeAll(skippedEvents);
        return events;

    }

}