package com.ibm.guardium.universalconnector.commons.custom_parsing;

import java.util.*;

import static com.ibm.guardium.universalconnector.commons.custom_parsing.PropertyConstant.*;

public class SqlParser {
    static List<String> validParsers = new ArrayList<>(Arrays.asList("REGEX", "SNIFFER"));
    static Map<String, String> validSnifferParsers;

    static {
        Map<String, String> map = new HashMap<>();
        map.put("MSSQL", "MS_SQL_SERVER");
        map.put("SYB", "SYBASE");
        map.put("ORACLE", "ORACLE");
        map.put("DB2", "DB2");
        map.put("INFX", "IBM_INFORMIX");
        map.put("MYSQL", "MYSQL");
        map.put("TRD", "TERADATA");
        map.put("PGRS", "POSTGRESQL");
        map.put("MONGO", "MONGODB");
        map.put("CASS", "CASSANDRA");
        map.put("ASTER", "ASTER");
        map.put("GPLUM", "GPLUM");
        map.put("COUCH", "COUCHBASE");
        map.put("MARIADB", "MARIADB");
        map.put("HIVE", "HIVE");
        map.put("IMPALA", "IMPALA");
        map.put("VRTC", "VERTICA");
        map.put("MEMSQL", "MEMSQL");
        map.put("MYSQL_X", "MYSQL_X");
        map.put("COUCHDB", "COUCHDB");
        map.put("REDIS", "REDIS");
        map.put("COCKROACH", "COCKROACH");
        map.put("SNOWFLAKE", "SNOWFLAKE");
        validSnifferParsers = Collections.unmodifiableMap(map);
    }

    public static String getServerType(String language) {
        return validSnifferParsers.get(language);
    }

    public static ValidityCase isValid(Map<String, String> properties) {
        boolean active = hasSqlParsing(properties);
        if (!active)
            return ValidityCase.VALID;

        String parsingType = properties.get(PARSING_TYPE);
        if (parsingType == null || !validParsers.contains(parsingType))
            return ValidityCase.INVALID_PARSING_TYPE;

        if (isSnifferParsing(parsingType)) {
            String snifferParser = properties.get(SNIFFER_PARSER);
            if (snifferParser == null || !validSnifferParsers.containsKey(snifferParser))
                return ValidityCase.INVALID_SNIFFER_PARSER;
        } else {
            String object = properties.get(OBJECT);
            if (object == null || object.isEmpty())
                return ValidityCase.NULL_OBJECT;

            String verb = properties.get(VERB);
            if (verb == null || verb.isEmpty())
                return ValidityCase.NULL_VERB;
        }

        return ValidityCase.VALID;
    }

    public static boolean hasSqlParsing(Map<String, String> properties) {
        return Boolean.parseBoolean(properties.get(SQL_PARSING_ACTIVE));
    }

    public static boolean isSnifferParsing(String parsingType) {
        return parsingType.equalsIgnoreCase("SNIFFER");
    }

    public enum ValidityCase {
        VALID("The SQL Parsing is valid"),
        INVALID_PARSING_TYPE("Parsing type can only be REGEX or SNIFFER"),
        INVALID_SNIFFER_PARSER("Sniffer Parser is invalid."),
        NULL_OBJECT("The object field cannot be null."),
        NULL_VERB("The verb field cannot be null.");

        public final String description;

        ValidityCase(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}
