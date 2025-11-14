// Copyright 2020-2021 IBM Inc. All rights reserved
// SPDX-License-Identifier: Apache2.0
//

package com.ibm.guardium.singlestore;

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Utility class for parsing SingleStore log messages into key-value pairs
 * instead of relying on array indices.
 */
public class SingleStoreLogFormat {
    private static final Logger log = LogManager.getLogger(SingleStoreLogFormat.class);

    // Field names
    public static final String EVENT_TYPE = "eventType";
    public static final String DB_USER = "dbUser";
    public static final String DB_NAME = "dbName";
    public static final String SERVER_PORT = "serverPort";
    public static final String QUERY = "query";
    public static final String LOGIN_STATUS = "loginStatus";
    public static final String TIMESTAMP_DATE = "timestampDate";
    public static final String TIMESTAMP_TIME = "timestampTime";

    // Known values
    public static final String USER_LOGIN = "USER_LOGIN";
    public static final String LOGIN_FAILURE = "FAILURE: Access denied";

    /**
     * Parse a SingleStore log message into a map of key-value pairs
     *
     * @param logMessage The raw log message string
     * @return Map containing parsed fields
     */
    public static Map<String, String> parseLog(String logMessage) {
        Map<String, String> logMap = new HashMap<>();

        if (logMessage == null || logMessage.isEmpty()) {
            log.warn("Empty log message provided to parser");
            return logMap;
        }

        try {
            // Split on commas that are NOT preceded by backslash
            // This handles escaped commas (\,) in the log format
            String[] values = logMessage.split("(?<!\\\\),");

            // Unescape both comma separators AND backslashes to get original SQL
            for (int i = 0; i < values.length; i++) {
                if (values[i] != null) {
                    // Unescape commas that were used as field separators
                    values[i] = values[i].replace("\\,", ",");
                    // Unescape double backslashes to single backslashes (original SQL format)
                    values[i] = values[i].replace("\\\\", "\\");
                }
            }

            // Basic validation
            if (values.length < 12) {
                log.warn("Log format error: Not enough fields in log event. Expected at least 12, got {}", values.length);
                return logMap;
            }

            // Extract key fields with null checks
            logMap.put(TIMESTAMP_DATE, values[1] != null ? values[1] : "");
            logMap.put(TIMESTAMP_TIME, values[2] != null ? values[2] : "");
            logMap.put(EVENT_TYPE, values[5] != null ? values[5] : "");
            logMap.put(DB_USER, values[7] != null ? values[7] : "");
            logMap.put(DB_NAME, values[8] != null ? values[8] : "");

            // Extract server port from format like "port:1234" with null check
            if (values[3] != null) {
                String serverPortField = values[3];
                String[] portParts = serverPortField.split(":");
                if (portParts.length > 1 && portParts[1] != null) {
                    logMap.put(SERVER_PORT, portParts[1]);
                } else {
                    logMap.put(SERVER_PORT, "");
                }
            } else {
                logMap.put(SERVER_PORT, "");
            }

            // Query is now correctly in values[11] - no need to reassemble
            // Backslashes are preserved as-is from the original log
            if (values.length > 11 && values[11] != null) {
                logMap.put(QUERY, values[11]);
            } else {
                logMap.put(QUERY, "");
            }

            // For login events, extract status
            if (USER_LOGIN.equals(logMap.get(EVENT_TYPE)) && values.length > 11 && values[11] != null) {
                logMap.put(LOGIN_STATUS, values[11]);
            } else {
                logMap.put(LOGIN_STATUS, "");
            }

            return logMap;
        } catch (Exception e) {
            log.error("Error parsing log message: {}", e.getMessage());
            return logMap;
        }
    }
}
