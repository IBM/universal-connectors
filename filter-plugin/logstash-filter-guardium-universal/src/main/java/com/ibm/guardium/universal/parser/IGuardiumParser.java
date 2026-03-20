/*
 * Copyright 2024 IBM Inc. All rights reserved
 * SPDX-License-Identifier: Apache2.0
 */
package com.ibm.guardium.universal.parser;

import co.elastic.logstash.api.Event;
import com.ibm.guardium.universalconnector.commons.structures.Record;

/**
 * Contract for all datasource-specific Guardium parsers.
 *
 * <p>Implementations contain ONLY the data-source-specific parsing logic.
 * All Logstash plugin boilerplate (registration, event loop, GSON, Log4j,
 * error tagging) is handled once in {@link com.ibm.guardium.universal.GuardiumUniversalFilter}.
 *
 * <h2>Adding a new datasource</h2>
 * <ol>
 *   <li>Create a class that implements this interface.</li>
 *   <li>Register it in {@link ParserRegistry} with a one-liner.</li>
 *   <li>Ship a {@code filter.conf} that uses
 *       {@code guardium_universal_filter { datasource => "your-name" }}.</li>
 * </ol>
 */
public interface IGuardiumParser {

    /**
     * Parse a Logstash event into a Guardium {@link Record}.
     *
     * @param event the Logstash event containing raw log data
     * @return a fully populated Record, or {@code null} to silently skip the event
     * @throws Exception if the event is malformed; the generic filter will tag it
     *                   as {@code _guardium_parse_error_<datasource>}
     */
    Record parseRecord(Event event) throws Exception;
}
