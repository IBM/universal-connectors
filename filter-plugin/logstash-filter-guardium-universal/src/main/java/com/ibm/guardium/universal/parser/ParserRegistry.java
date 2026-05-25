/*
 * Copyright 2024 IBM Inc. All rights reserved
 * SPDX-License-Identifier: Apache2.0
 */
package com.ibm.guardium.universal.parser;

import com.ibm.guardium.universal.datasources.mongodb.MongoDbParser;
import com.ibm.guardium.universal.datasources.mysql.MySqlParser;
import com.ibm.guardium.universal.datasources.snowflake.SnowflakeParser;

import java.util.Map;
import java.util.TreeMap;

/**
 * Central registry mapping datasource names to their {@link IGuardiumParser} implementations.
 *
 * <p>Adding a new datasource requires exactly one line here plus the parser class itself.
 * No Logstash plugin scaffolding, build files, or gem packaging are needed.
 */
public class ParserRegistry {

    // Case-insensitive map so "MySQL", "mysql", and "MYSQL" all resolve.
    private static final Map<String, IGuardiumParser> PARSERS =
            new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    static {
        // ---- Relational ----
        register("mysql",       new MySqlParser());
        register("mysql-aws",   new MySqlParser());   // re-uses same parser; same log format
        register("mysql-azure", new MySqlParser());

        // ---- Document / NoSQL ----
        register("mongodb",     new MongoDbParser());

        // ---- Cloud DW ----
        register("snowflake",   new SnowflakeParser());

        // TODO: register remaining datasources following the same pattern:
        // register("postgres",     new PostgresParser());
        // register("mssql",        new MsSqlParser());
        // register("cassandra",    new CassandraParser());
        // ... (one line per datasource)
    }

    /**
     * Returns the parser for the given datasource name.
     *
     * @throws IllegalArgumentException if no parser is registered for the name
     */
    public static IGuardiumParser getParser(String datasource) {
        if (datasource == null || datasource.isBlank()) {
            throw new IllegalArgumentException(
                    "guardium_universal_filter requires a 'datasource' parameter. "
                    + "Available: " + PARSERS.keySet());
        }
        IGuardiumParser parser = PARSERS.get(datasource);
        if (parser == null) {
            throw new IllegalArgumentException(
                    "No parser registered for datasource '" + datasource + "'. "
                    + "Available: " + PARSERS.keySet());
        }
        return parser;
    }

    /**
     * Registers a parser. Intended for testing and runtime extensions.
     */
    public static void register(String name, IGuardiumParser parser) {
        PARSERS.put(name, parser);
    }
}
