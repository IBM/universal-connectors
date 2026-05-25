/*
 * Copyright 2024 IBM Inc. All rights reserved
 * SPDX-License-Identifier: Apache2.0
 */
package com.ibm.guardium.universal.datasources.snowflake;

import co.elastic.logstash.api.Event;
import com.ibm.guardium.universal.parser.AbstractGuardiumParser;
import com.ibm.guardium.universalconnector.commons.structures.Record;

import java.util.Optional;

/**
 * Thin connector for Snowflake audit log parsing.
 *
 * <p><b>Migration note:</b> This class delegates to event-type parsers that
 * currently live in {@code logstash-filter-snowflake-guardium}. As part of the
 * full migration, the following packages should be moved into this plugin:
 * <ul>
 *   <li>{@code com.ibm.guardium.snowflakedb.parser.*}</li>
 *   <li>{@code com.ibm.guardium.snowflakedb.utils.*}</li>
 *   <li>{@code com.ibm.guardium.snowflakedb.exceptions.*}</li>
 * </ul>
 *
 * <p>All logic previously in {@code GuardiumSnowflakeFilter} is consolidated here.
 */
public class SnowflakeParser extends AbstractGuardiumParser {

    // TODO (migration): move com.ibm.guardium.snowflakedb.* into this plugin
    private static final String EVENT_TYPE_FIELD = com.ibm.guardium.snowflakedb.utils.Constants.EVENT_TYPE;

    @Override
    public Record parseRecord(Event event) throws Exception {
        String eventType = Optional.ofNullable(event.getField(EVENT_TYPE_FIELD))
                .map(Object::toString).orElse(null);
        if (eventType == null) return null;

        com.ibm.guardium.snowflakedb.parser.Parser parser;
        switch (eventType.toUpperCase()) {
            case com.ibm.guardium.snowflakedb.utils.Constants.SQL_ERROR:
                parser = new com.ibm.guardium.snowflakedb.parser.SQLErrorEventParser();
                break;
            case com.ibm.guardium.snowflakedb.utils.Constants.LOGIN_FAILED:
                parser = new com.ibm.guardium.snowflakedb.parser.AuthFailedEventParser();
                break;
            case com.ibm.guardium.snowflakedb.utils.Constants.SUCCESS:
                parser = new com.ibm.guardium.snowflakedb.parser.SuccessEventParser();
                break;
            default:
                return null;
        }

        Record record = parser.parseRecord(event.toMap());

        // Skip events with no resolved user (same guard as original filter)
        String dbUser = record.getAccessor().getDbUser();
        if (dbUser == null || dbUser.isEmpty()) return null;

        return record;
    }
}
