/*
Copyright IBM Corp. 2021, 2024 All rights reserved.
SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.guardium.cockroachdb;

import co.elastic.logstash.api.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.ibm.guardium.universalconnector.commons.GuardConstants;
import com.ibm.guardium.universalconnector.commons.structures.Record;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import static com.ibm.guardium.cockroachdb.Constants.*;

/**
 * CockroachDB Guardium Filter Plugin
 * <p>
 * This filter processes CockroachDB audit logs received via syslog and converts them
 * into Guardium universal connector format.
 */
@LogstashPlugin(name = "cockroachdb_guardium_filter")
public class CockroachdbGuardiumFilter implements Filter {

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

    private static Logger log = LogManager.getLogger(CockroachdbGuardiumFilter.class);

    private String id;
    private Parser parser;

    public static final PluginConfigSpec<String> SOURCE_CONFIG =
            PluginConfigSpec.stringSetting("source", COCKROACHDB);

    public CockroachdbGuardiumFilter(String id, Configuration config, Context context) {
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
     * Filters the received events by skipping the invalid ones and normalizing them by parsing the
     * provided payloads into Guardium Generic Records.
     *
     * @param events              A list of received events
     * @param filterMatchListener The listener for this plugin
     * @return A list of normalized events
     */
    @Override
    public Collection<Event> filter(Collection<Event> events, FilterMatchListener filterMatchListener) {
        ArrayList<Event> skippedEvents = new ArrayList<>();

        for (Event e : events) {
            if (log.isDebugEnabled()) {
                log.debug("Event Now: {}", e.getData());
            }
            try {
                // Check if the event has the cockroachdb field
                Object cockroachdbField = e.getField(COCKROACHDB);

                if (cockroachdbField == null) {
                    log.error("Event does not contain cockroachdb field, tagging as invalid");
                    e.tag(INVALID_MSG);
                    skippedEvents.add(e);
                    continue;
                }

                // Convert the cockroachdb field to JsonObject
                JsonObject inputJSON = new Gson().toJsonTree(cockroachdbField).getAsJsonObject();

                // Parse the record (filtering is handled by Logstash Grok filter)
                Record record = this.parser.parseRecord(inputJSON);

                // Convert to JSON and add to event
                final GsonBuilder builder = new GsonBuilder();
                builder.serializeNulls();
                final Gson gson = builder.create();
                e.setField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME, gson.toJson(record));

                filterMatchListener.filterMatched(e);
                log.info("==========>Final JSON to be send to Guardium: {}", gson.toJson(record));

            } catch (Exception exception) {
                log.error("CockroachDB Filter: Error parsing CockroachDB event: " + exception.getMessage(), exception);
                e.tag(INVALID_MSG);
                skippedEvents.add(e);
            }
        }

        events.removeAll(skippedEvents);
        return events;
    }
}