/*
 * Copyright 2024 IBM Inc. All rights reserved
 * SPDX-License-Identifier: Apache2.0
 */
package com.ibm.guardium.universal.datasources.mongodb;

import co.elastic.logstash.api.Event;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ibm.guardium.universal.parser.AbstractGuardiumParser;
import com.ibm.guardium.universalconnector.commons.structures.Record;

import java.util.Optional;

/**
 * Thin connector for MongoDB audit log parsing.
 *
 * <p><b>Migration note:</b> This class delegates to the parser hierarchy that
 * currently lives in {@code logstash-filter-mongodb-guardium}. As part of the
 * full migration to the universal plugin, the following packages should be moved
 * into this plugin (they have no Logstash dependency — only common + Gson):
 * <ul>
 *   <li>{@code com.ibm.guardium.mongodb.ParserFactory}</li>
 *   <li>{@code com.ibm.guardium.mongodb.AType}</li>
 *   <li>{@code com.ibm.guardium.mongodb.Parser} (constants)</li>
 *   <li>{@code com.ibm.guardium.mongodb.parsersbytype.*} (10 parser classes)</li>
 * </ul>
 *
 * <p>Until those packages are moved, include the mongodb plugin's JAR as a
 * compile-time dependency in {@code build.gradle}.
 *
 * <p>All enrichment logic previously scattered across {@code MongodbGuardiumFilter}
 * (server_hostname, source_program, dbname_prefix, server port, IP correction)
 * is consolidated here.
 */
public class MongoDbParser extends AbstractGuardiumParser {

    private static final String MONGOD_SIGNAL = "mongod: ";
    private static final String MONGOS_SIGNAL = "mongos: ";

    @Override
    public Record parseRecord(Event event) throws Exception {
        if (!(event.getField("message") instanceof String)) return null;

        String message = event.getField("message").toString();

        // Skip internal MongoDB system events
        if (message.contains("__system") || message.contains("\"c\":\"CONTROL\"")) {
            return null;
        }

        // Locate the JSON audit payload after "mongod: " or "mongos: "
        int idx = message.indexOf(MONGOD_SIGNAL);
        if (idx == -1) idx = message.indexOf(MONGOS_SIGNAL);
        if (idx == -1) return null;

        String json = message.substring(idx + MONGOD_SIGNAL.length());
        JsonObject data = JsonParser.parseString(json).getAsJsonObject();

        // --- Delegate to the existing MongoDB parser factory ---
        // TODO (migration): move com.ibm.guardium.mongodb.* into this plugin
        com.ibm.guardium.mongodb.parsersbytype.BaseParser mongoParser =
                com.ibm.guardium.mongodb.ParserFactory.getParser(data);
        Record record = mongoParser.parseRecord(data);
        if (record == null) return null;

        // Enrich with Logstash event fields (previously in MongodbGuardiumFilter)
        if (event.getField("server_hostname") instanceof String) {
            record.getAccessor().setServerHostName(
                    event.getField("server_hostname").toString());
        }
        Optional.ofNullable(event.getField("source_program"))
                .map(Object::toString)
                .ifPresent(sp -> record.getAccessor().setSourceProgram(sp));

        if (event.getField("icd_default_serverport") instanceof String) {
            record.getSessionLocator().setServerPort(
                    Integer.parseInt(event.getField("icd_default_serverport").toString()));
        }

        if (event.getField("dbname_prefix") instanceof String) {
            String prefix = event.getField("dbname_prefix").toString();
            if (!prefix.isEmpty()) {
                String db = record.getDbName();
                String combined = db.isEmpty() ? prefix : prefix + ":" + db;
                record.setDbName(combined);
                record.getAccessor().setServiceName(combined);
            }
        }

        correctIPs(event, record);
        return record;
    }
}
