/*
 * Copyright 2024 IBM Inc. All rights reserved
 * SPDX-License-Identifier: Apache2.0
 */
package com.ibm.guardium.universal;

import co.elastic.logstash.api.Configuration;
import co.elastic.logstash.api.Context;
import co.elastic.logstash.api.Event;
import co.elastic.logstash.api.Filter;
import co.elastic.logstash.api.FilterMatchListener;
import co.elastic.logstash.api.LogstashPlugin;
import co.elastic.logstash.api.PluginConfigSpec;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ibm.guardium.universal.parser.IGuardiumParser;
import com.ibm.guardium.universal.parser.ParserRegistry;
import com.ibm.guardium.universalconnector.commons.GuardConstants;
import com.ibm.guardium.universalconnector.commons.structures.Record;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Single generic Logstash filter plugin that replaces all individual
 * {@code logstash-filter-*-guardium} plugins.
 *
 * <p>Usage in {@code filter.conf}:
 * <pre>{@code
 * filter {
 *   guardium_universal_filter {
 *     datasource => "mysql"   # or "mongodb", "snowflake", "postgres", ...
 *   }
 * }
 * }</pre>
 *
 * <p>This class contains ZERO datasource-specific logic. All parsing is
 * delegated to the {@link IGuardiumParser} registered for the chosen datasource.
 * Adding a new datasource means writing one parser class and one line in
 * {@link ParserRegistry} — no new Logstash plugin, no new gem, no new build.
 */
@LogstashPlugin(name = "guardium_universal_filter")
public class GuardiumUniversalFilter implements Filter {

    // ---- Config specs --------------------------------------------------------

    /** Which datasource this pipeline handles, e.g. "mysql", "mongodb". */
    public static final PluginConfigSpec<String> DATASOURCE_CONFIG =
            PluginConfigSpec.stringSetting("datasource", "");

    /** Logstash field that carries the raw log message. Defaults to "message". */
    public static final PluginConfigSpec<String> SOURCE_CONFIG =
            PluginConfigSpec.stringSetting("source", "message");

    // ---- Static init ---------------------------------------------------------

    static {
        try {
            String ucEtc = System.getenv("UC_ETC");
            LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
            ctx.setConfigLocation(
                    new File(ucEtc + File.separator + "log4j2uc.properties").toURI());
        } catch (Exception e) {
            System.err.println("Failed to load log4j configuration: " + e.getMessage());
        }
    }

    private static final Logger log = LogManager.getLogger(GuardiumUniversalFilter.class);
    private static final Gson GSON = new GsonBuilder().serializeNulls().create();

    // ---- Instance fields -----------------------------------------------------

    private final String id;
    private final String datasource;
    private final IGuardiumParser parser;

    // ---- Constructor ---------------------------------------------------------

    public GuardiumUniversalFilter(String id, Configuration config, Context context) {
        this.id = id;
        this.datasource = config.get(DATASOURCE_CONFIG);
        this.parser = ParserRegistry.getParser(this.datasource);
        log.info("GuardiumUniversalFilter initialised for datasource '{}'", datasource);
    }

    // ---- Filter logic --------------------------------------------------------

    @Override
    public Collection<Event> filter(Collection<Event> events, FilterMatchListener matchListener) {
        List<Event> skipped = new ArrayList<>();

        for (Event event : events) {
            try {
                Record record = parser.parseRecord(event);

                if (record == null) {
                    // Parser says: skip this event (not relevant / internal / empty)
                    event.tag("_guardium_skip_" + datasource);
                    skipped.add(event);
                } else {
                    event.setField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME, GSON.toJson(record));
                    matchListener.filterMatched(event);
                }

            } catch (Exception e) {
                log.error("[{}] Error parsing event: {}", datasource, e.getMessage(), e);
                event.tag("_guardium_parse_error_" + datasource);
            }
        }

        events.removeAll(skipped);
        return events;
    }

    // ---- Plugin metadata -----------------------------------------------------

    @Override
    public Collection<PluginConfigSpec<?>> configSchema() {
        return Arrays.asList(DATASOURCE_CONFIG, SOURCE_CONFIG);
    }

    @Override
    public String getId() {
        return id;
    }
}
