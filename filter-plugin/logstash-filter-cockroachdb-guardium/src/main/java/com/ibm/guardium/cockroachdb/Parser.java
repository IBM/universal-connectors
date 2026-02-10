/*
Copyright IBM Corp. 2021, 2024 All rights reserved.
SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.guardium.cockroachdb;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ibm.guardium.universalconnector.commons.structures.*;
import org.apache.commons.validator.routines.InetAddressValidator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import static com.ibm.guardium.cockroachdb.Constants.*;
import static com.ibm.guardium.universalconnector.commons.structures.Accessor.LANGUAGE_FREE_TEXT_STRING;

/**
 * Parser Class will perform operation on parsing events and messages from the CockroachDB audit logs into
 * a Guardium record instance. Guardium records include the accessor, the sessionLocator, data, and
 * exceptions. If there are no errors, the data contains details about the query "construct"
 *
 * @className Parser
 */
public class Parser {

    private static Logger logger = LogManager.getLogger(Parser.class);
    private static final InetAddressValidator inetAddressValidator = InetAddressValidator.getInstance();

    public Parser() {
    }

    /**
     * Parse the CockroachDB event into a Guardium Record
     *
     * @param data JsonObject containing the CockroachDB audit log data
     * @return Record object containing parsed data
     */
    public Record parseRecord(JsonObject data) {
        logger.debug("=== Starting parseRecord ===");
        logger.debug("Input data: {}", data.toString());

        Record record = new Record();

        record.setSessionId(UNKNOWN_STRING);
        String dbName = data.has(DATABASE_NAME) && !data.get(DATABASE_NAME).isJsonNull()
                ? data.get(DATABASE_NAME).getAsString()
                : extractDatabaseNameFromTableName(data);
        record.setDbName(dbName);

        // Extract and clean username
        String rawUser = data.has(USER) && !data.get(USER).isJsonNull()
                ? data.get(USER).getAsString()
                : NOT_AVAILABLE;
        record.setAppUserName(cleanExtraChars(rawUser));

        String sqlString = data.has(STATEMENT) && !data.get(STATEMENT).isJsonNull()
                ? data.get(STATEMENT).getAsString()
                : UNKNOWN_STRING;

        record.setAccessor(getAccessor(data, dbName));
        record.setSessionLocator(getSessionLocator(data));
        record.setTime(getTimestamp(data));

        // Check for authentication failures by EventType
        if (data.has(EVENT_TYPE) && !data.get(EVENT_TYPE).isJsonNull()
                && CLIENT_AUTH_FAILED.equals(data.get(EVENT_TYPE).getAsString())) {
            // Failed authentication - create Exception record only
            record.setData(null);
            record.setException(getLoginFailedException(data));
        }
        // Check for SQL errors
        else if (data.has(ERROR_TEXT) && !data.get(ERROR_TEXT).isJsonNull()) {
            // SQL ERROR - Query with error, create Exception record only
            record.setData(null);
            record.setException(getException(data, sqlString));
        } // Regular query execution
        else {
            record.setData(getData(sqlString));
        }

        return record;
    }

    /**
     * Get Accessor object with database user and service information
     */
    protected Accessor getAccessor(JsonObject data, String dbName) {
        Accessor accessor = new Accessor();

        accessor.setServiceName(dbName);

        // Extract and clean username
        String rawUser = data.has(USER) && !data.get(USER).isJsonNull()
                ? data.get(USER).getAsString()
                : NOT_AVAILABLE;
        accessor.setDbUser(cleanExtraChars(rawUser));
        accessor.setDbProtocolVersion(UNKNOWN_STRING);

        // Set DB protocol and server type
        accessor.setDbProtocol(DB_PROTOCOL);
        accessor.setServerType(SERVER_TYPE);

        accessor.setServerOs(UNKNOWN_STRING);
        accessor.setServerDescription(UNKNOWN_STRING);

        // Set server hostname - only if ServerHost is NOT an IP address
        // If it's an IP, set to N.A. since we don't have the actual hostname
        String serverHostName = NOT_AVAILABLE;
        if (data.has(SERVER_HOST) && !data.get(SERVER_HOST).isJsonNull()) {
            String serverHost = data.get(SERVER_HOST).getAsString();
            // Only set as hostname if it's NOT a valid IP address
            if (!inetAddressValidator.isValid(serverHost)) {
                serverHostName = serverHost;
            }
            // else: it's an IP, keep serverHostName as NOT_AVAILABLE
        }

        accessor.setServerHostName(serverHostName);
        accessor.setClientHostName(UNKNOWN_STRING);
        accessor.setClient_mac(UNKNOWN_STRING);
        accessor.setClientOs(UNKNOWN_STRING);
        accessor.setCommProtocol(UNKNOWN_STRING);
        accessor.setOsUser(UNKNOWN_STRING);
        accessor.setSourceProgram(data.has(APPLICATION_NAME) && !data.get(APPLICATION_NAME).isJsonNull()
                ? data.get(APPLICATION_NAME).getAsString()
                : UNKNOWN_STRING);
        accessor.setLanguage(LANGUAGE_COCKROACHDB);
        accessor.setDataType(Accessor.DATA_TYPE_GUARDIUM_SHOULD_PARSE_SQL);

        return accessor;
    }

    /**
     * Get SessionLocator with client and server IP/port information
     */
    protected SessionLocator getSessionLocator(JsonObject data) {
        SessionLocator sessionLocator = new SessionLocator();

        sessionLocator.setIpv6(false);
        sessionLocator.setClientIp(data.has(CLIENT_IP) && !data.get(CLIENT_IP).isJsonNull()
                ? data.get(CLIENT_IP).getAsString()
                : DEFAULT_IP);
        sessionLocator.setClientPort(data.has(CLIENT_PORT) && !data.get(CLIENT_PORT).isJsonNull()
                ? Integer.parseInt(data.get(CLIENT_PORT).getAsString())
                : DEFAULT_PORT);

        // Set server IP only if ServerHost field contains a valid IP address
        String serverIp = DEFAULT_IP;
        if (data.has(SERVER_HOST) && !data.get(SERVER_HOST).isJsonNull()) {
            String serverHost = data.get(SERVER_HOST).getAsString();
            // Only set as IP if it's a valid IP address
            if (inetAddressValidator.isValid(serverHost)) {
                serverIp = serverHost;
            }
        }
        sessionLocator.setServerIp(serverIp);

        sessionLocator.setServerPort(DEFAULT_PORT);
        sessionLocator.setClientIpv6(DEFAULT_IPV6);
        sessionLocator.setServerIpv6(DEFAULT_IPV6);

        return sessionLocator;
    }

    /**
     * Get timestamp from the CockroachDB event
     */
    protected Time getTimestamp(JsonObject data) {
        if (data.has(TIMESTAMP) && !data.get(TIMESTAMP).isJsonNull()) {
            try {
                long timestampNs = data.get(TIMESTAMP).getAsLong();
                long timestampMs = timestampNs / 1_000_000;
                return new Time(timestampMs, 0, 0);
            } catch (Exception e) {
                logger.error("Error parsing timestamp: {}", e.getMessage());
            }
        }
        // Fallback to current time if timestamp is missing or invalid
        return new Time(System.currentTimeMillis(), 0, 0);
    }

    /**
     * Get Data object containing SQL statement
     */
    protected Data getData(String sqlString) {
        Data data = new Data();
        data.setOriginalSqlCommand(cleanExtraChars(sqlString));
        return data;
    }

    /**
     * Get ExceptionRecord for error events
     */
    protected ExceptionRecord getException(JsonObject data, String sqlString) {
        ExceptionRecord exception = new ExceptionRecord();

        String sqlState = data.has(SQLSTATE) && !data.get(SQLSTATE).isJsonNull()
                ? data.get(SQLSTATE).getAsString()
                : UNKNOWN_STRING;

        String errorText = data.has(ERROR_TEXT) && !data.get(ERROR_TEXT).isJsonNull()
                ? data.get(ERROR_TEXT).getAsString()
                : UNKNOWN_STRING;

        // Build description with SQLSTATE if available
        String description = errorText;
        if (sqlState != null && !sqlState.isEmpty()) {
            description = String.format("[SQLSTATE: %s] %s", sqlState, errorText);
        }

        exception.setExceptionTypeId(EXCEPTION_TYPE_SQL_ERROR_STRING);
        exception.setDescription(cleanExtraChars(description));
        exception.setSqlString(UNKNOWN_STRING.equals(sqlString) ? NOT_AVAILABLE : cleanExtraChars(sqlString));

        return exception;
    }

    /**
     * Get ExceptionRecord for failed login attempts
     */
    protected ExceptionRecord getLoginFailedException(JsonObject data) {
        ExceptionRecord exception = new ExceptionRecord();

        // Extract and clean username
        String rawUser = data.has(USER) && !data.get(USER).isJsonNull()
                ? data.get(USER).getAsString()
                : NOT_AVAILABLE;
        String user = cleanExtraChars(rawUser);

        String reason = data.has(REASON) && !data.get(REASON).isJsonNull()
                ? data.get(REASON).getAsString()
                : UNKNOWN_STRING;

        String description = String.format("Login failed for user '%s': %s", user, reason);

        exception.setExceptionTypeId(EXCEPTION_TYPE_LOGIN_FAILED_STRING);
        exception.setDescription(cleanExtraChars(description));
        exception.setSqlString(UNKNOWN_STRING);

        return exception;
    }

    /**
     * Extract database name from TableName field.
     * For DDL operations (create_table, alter_table, drop_table, create_index, etc.),
     * the database name is embedded in the TableName field in format: database.schema.table
     * CockroachDB uses format: database.schema.table or database.table
     *
     * @param data The JsonObject containing the event data
     * @return Database name if found, otherwise NOT_AVAILABLE
     */
    private String extractDatabaseNameFromTableName(JsonObject data) {
        // Check if TableName exists at the top level
        if (data.has(TABLE_NAME) && !data.get(TABLE_NAME).isJsonNull()) {
            String tableName = data.get(TABLE_NAME).getAsString();

            if (tableName == null || tableName.isEmpty()) {
                return NOT_AVAILABLE;
            }

            // Clean any special characters first
            tableName = cleanExtraChars(tableName);

            // Split by dot and take the first part as database name
            String[] parts = tableName.split("\\.");
            if (parts.length > 0 && !parts[0].isEmpty()) {
                return parts[0];
            }
        }

        return NOT_AVAILABLE;
    }

    /**
     * Remove CockroachDB's special Unicode quote characters (‹ and ›)
     * that appear as pairs wrapping values. This handles extra characters
     * like ‹value› by removing only the quote characters while preserving
     * the content between them.
     *
     * @param value The string to clean
     * @return Cleaned string without special quotes, or original value if null/empty
     */
    private String cleanExtraChars(String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        // Remove paired Unicode quotes (‹content›) by replacing them with just the content
        // This regex matches the opening quote, captures any content, and the closing quote
        return value.replaceAll("‹([^›]*)›", "$1");
    }
}