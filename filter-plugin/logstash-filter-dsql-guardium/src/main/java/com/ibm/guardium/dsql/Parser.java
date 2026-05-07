/*
#Copyright 2020-2021 IBM Inc. All rights reserved
#SPDX-License-Identifier: Apache-2.0
#*/
package com.ibm.guardium.dsql;

import co.elastic.logstash.api.Event;
import com.ibm.guardium.universalconnector.commons.structures.Accessor;
import com.ibm.guardium.universalconnector.commons.structures.Data;
import com.ibm.guardium.universalconnector.commons.structures.ExceptionRecord;
import com.ibm.guardium.universalconnector.commons.structures.Record;
import com.ibm.guardium.universalconnector.commons.structures.SessionLocator;
import com.ibm.guardium.universalconnector.commons.structures.Time;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.ParseException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Parser {

    private static Logger log = LogManager.getLogger(Parser.class);

    /**
     * Parse the DSQL Database Activity Stream event into a Guardium Record
     * The event is expected to have already been parsed by Logstash's json filter
     * Supports both flat format and nested DatabaseActivityMonitoringRecord format
     *
     * @param event The Logstash event containing parsed DSQL audit log data
     * @return Record object containing parsed audit information
     * @throws ParseException if parsing fails
     */
    public static Record parseRecord(final Event event) throws ParseException {
        Record record = new Record();

        try {
            // The message should already be parsed as a Map by Logstash's json filter
            // Access the parsed fields directly from the event
            Map<String, Object> eventData = event.getData();
            
            if (eventData == null || eventData.isEmpty()) {
                throw new ParseException("Event data is null or empty", 0);
            }

            // Check if this is a nested DatabaseActivityMonitoringRecord format
            Event processedEvent = event;
            if (isNestedFormat(event)) {
                log.debug("Detected nested DatabaseActivityMonitoringRecord format");
                processedEvent = extractNestedEvent(event);
                if (processedEvent == null) {
                    throw new ParseException("Failed to extract nested event from DatabaseActivityMonitoringRecord", 0);
                }
            }

            // Validate that this is a PostgreSQL record
            Object dbProtocolObj = processedEvent.getField(Constants.DB_PROTOCOL);
            if (dbProtocolObj != null) {
                String dbProtocol = dbProtocolObj.toString();
                if (!dbProtocol.equalsIgnoreCase("POSTGRESQL") && !dbProtocol.equalsIgnoreCase("POSTGRES")) {
                    throw new ParseException("Unsupported database protocol: " + dbProtocol + ". Only POSTGRESQL is supported.", 0);
                }
            }

            // Set basic record fields
            record.setAppUserName(Constants.APP_USER_NAME);
            record.setAccessor(parseAccessor(processedEvent));
            record.setSessionLocator(parseSessionLocator(processedEvent));
            record.setDbName(parseDbName(processedEvent));
            record.setTime(parseTimestamp(processedEvent));
            
            // Set session ID
            Object sessionIdObj = getFieldValue(processedEvent, Constants.SESSION_ID);
            if (sessionIdObj != null) {
                record.setSessionId(sessionIdObj.toString());
            } else {
                record.setSessionId(Constants.UNKNOWN_STRING);
            }

            // Parse SQL statement or exception based on exit code
            Object exitCodeObj = getFieldValue(processedEvent, Constants.EXIT_CODE);
            String exitCode = exitCodeObj != null ? exitCodeObj.toString() : null;
            
            if (exitCode != null && !exitCode.equals(Constants.EXIT_CODE_SUCCESS)) {
                // This is an error/exception
                record.setException(parseException(processedEvent));
            } else {
                // This is a successful SQL statement
                Object statementText = getStatementText(processedEvent);
                if (statementText != null && !statementText.toString().isEmpty()) {
                    record.setData(parseData(processedEvent));
                }
            }

        } catch (Exception e) {
            log.error("Error parsing DSQL record: {}", e.getMessage(), e);
            throw new ParseException("Failed to parse DSQL record: " + e.getMessage(), 0);
        }

        return record;
    }

    /**
     * Check if the event is in nested DatabaseActivityMonitoringRecord format
     */
    private static boolean isNestedFormat(Event event) {
        Object typeObj = event.getField(Constants.TYPE);
        Object eventListObj = event.getField(Constants.DATABASE_ACTIVITY_EVENT_LIST);
        
        return typeObj != null &&
               typeObj.toString().equals(Constants.DB_ACTIVITY_MONITORING_RECORD) &&
               eventListObj != null;
    }

    /**
     * Extract the first event from the nested databaseActivityEventList array
     * Creates a new Event object with flattened fields for easier processing
     */
    private static Event extractNestedEvent(Event parentEvent) {
        try {
            Object eventListObj = parentEvent.getField(Constants.DATABASE_ACTIVITY_EVENT_LIST);
            
            if (eventListObj instanceof List<?>) {
                List<?> eventList = (List<?>) eventListObj;
                
                if (!eventList.isEmpty() && eventList.get(0) instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> nestedEventData = (Map<String, Object>) eventList.get(0);
                    
                    // Create a new event with the nested data
                    Event flatEvent = new org.logstash.Event();
                    
                    // Copy all fields from nested event
                    for (Map.Entry<String, Object> entry : nestedEventData.entrySet()) {
                        flatEvent.setField(entry.getKey(), entry.getValue());
                    }
                    
                    // Also preserve parent-level fields that might be needed
                    Object instanceId = parentEvent.getField(Constants.INSTANCE_ID);
                    if (instanceId != null) {
                        flatEvent.setField(Constants.INSTANCE_ID, instanceId);
                    }
                    
                    Object clusterId = parentEvent.getField(Constants.CLUSTER_ID);
                    if (clusterId != null) {
                        flatEvent.setField(Constants.CLUSTER_ID, clusterId);
                    }
                    
                    // Preserve account_id and instance_name if they exist at parent level
                    Object accountId = parentEvent.getField(Constants.ACCOUNT_ID);
                    if (accountId != null) {
                        flatEvent.setField(Constants.ACCOUNT_ID, accountId);
                    }
                    
                    Object instanceName = parentEvent.getField(Constants.INSTANCE_NAME);
                    if (instanceName != null) {
                        flatEvent.setField(Constants.INSTANCE_NAME, instanceName);
                    }
                    
                    return flatEvent;
                }
            }
        } catch (Exception e) {
            log.error("Error extracting nested event: {}", e.getMessage(), e);
        }
        
        return null;
    }

    /**
     * Get field value, checking both statementText and commandText fields
     */
    private static Object getStatementText(Event event) {
        Object statementText = event.getField(Constants.STATEMENT_TEXT);
        if (statementText == null || statementText.toString().isEmpty()) {
            statementText = event.getField(Constants.COMMAND_TEXT);
        }
        return statementText;
    }

    /**
     * Get field value with fallback support for nested format field names
     */
    private static Object getFieldValue(Event event, String fieldName) {
        return event.getField(fieldName);
    }

    /**
     * Parse accessor information from the audit event
     */
    private static Accessor parseAccessor(Event event) {
        Accessor accessor = new Accessor();

        // Set database user
        Object dbUserObj = event.getField(Constants.DB_USER_NAME);
        if (dbUserObj != null && !dbUserObj.toString().isEmpty()) {
            accessor.setDbUser(dbUserObj.toString());
        } else {
            accessor.setDbUser(Constants.UNKNOWN_STRING);
        }

        // Set server type and protocol
        accessor.setServerType(Constants.SERVER_TYPE_STRING);
        accessor.setDbProtocol(Constants.DATA_PROTOCOL_STRING);
        accessor.setLanguage(Constants.LANGUAGE);
        accessor.setCommProtocol(Constants.COMM_PROTOCOL);

        // Set server hostname
        String accountId = getAccountId(event);
        String instanceName = getInstanceName(event);
        String serverHostName = accountId + ":" + instanceName;
        accessor.setServerHostName(serverHostName);

        // Set service name (same as dbName)
        String dbName = parseDbName(event);
        accessor.setServiceName(dbName);

        // Set client application if available
        Object clientAppObj = event.getField(Constants.CLIENT_APPLICATION);
        if (clientAppObj != null && !clientAppObj.toString().isEmpty()) {
            accessor.setSourceProgram(clientAppObj.toString());
        } else {
            accessor.setSourceProgram(Constants.UNKNOWN_STRING);
        }

        // Set other fields
        accessor.setServerOs(Constants.UNKNOWN_STRING);
        accessor.setClientOs(Constants.UNKNOWN_STRING);
        accessor.setClientHostName(Constants.UNKNOWN_STRING);
        accessor.setDbProtocolVersion(Constants.UNKNOWN_STRING);
        accessor.setOsUser(Constants.UNKNOWN_STRING);
        accessor.setClient_mac(Constants.UNKNOWN_STRING);
        accessor.setServerDescription(Constants.UNKNOWN_STRING);
        accessor.setDataType(Accessor.DATA_TYPE_GUARDIUM_SHOULD_PARSE_SQL);

        return accessor;
    }

    /**
     * Parse session locator information
     */
    private static SessionLocator parseSessionLocator(Event event) {
        SessionLocator sessionLocator = new SessionLocator();

        // Parse client IP and port
        String clientIp = Constants.DEFAULT_IP;
        int clientPort = Constants.DEFAULT_PORT;

        Object remoteHostObj = event.getField(Constants.REMOTE_HOST);
        if (remoteHostObj != null && !remoteHostObj.toString().isEmpty()) {
            clientIp = remoteHostObj.toString();
        }

        Object remotePortObj = event.getField(Constants.REMOTE_PORT);
        if (remotePortObj != null) {
            try {
                clientPort = Integer.parseInt(remotePortObj.toString());
            } catch (NumberFormatException e) {
                log.warn("Failed to parse remote port: {}", remotePortObj);
            }
        }

        sessionLocator.setClientIp(clientIp);
        sessionLocator.setClientPort(clientPort);

        // Set server IP (using account:instance:dbname format)
        String accountId = getAccountId(event);
        String instanceName = getInstanceName(event);
        
        Object dbNameObj = event.getField(Constants.DATABASE_NAME);
        String dbName = dbNameObj != null ? dbNameObj.toString() : Constants.UNKNOWN_STRING;
        
        String serverIp = accountId + ":" + instanceName + ":" + dbName;
        sessionLocator.setServerIp(serverIp);
        sessionLocator.setServerPort(Constants.DEFAULT_PORT);

        // Set IPv6 fields
        sessionLocator.setIpv6(false);
        sessionLocator.setClientIpv6(Constants.UNKNOWN_STRING);
        sessionLocator.setServerIpv6(Constants.UNKNOWN_STRING);

        return sessionLocator;
    }

    /**
     * Parse database name
     */
    private static String parseDbName(Event event) {
        String accountId = getAccountId(event);
        String instanceName = getInstanceName(event);
        String dbName = Constants.UNKNOWN_STRING;

        Object dbNameObj = event.getField(Constants.DATABASE_NAME);
        if (dbNameObj != null && !dbNameObj.toString().isEmpty()) {
            dbName = dbNameObj.toString();
        }

        if (dbName.isEmpty() || dbName.equals(Constants.UNKNOWN_STRING)) {
            return Constants.NA;
        }

        return accountId + ":" + instanceName + ":" + dbName;
    }

    /**
     * Parse timestamp from the audit event
     * Supports multiple timestamp formats including ISO 8601 and SQL Server format
     */
    private static Time parseTimestamp(Event event) {
        long millis = 0;

        try {
            // Try logTime first, then startTime
            Object timeField = event.getField(Constants.LOG_TIME);
            
            if (timeField == null) {
                timeField = event.getField(Constants.START_TIME);
            }

            if (timeField != null) {
                String dateString = timeField.toString();
                
                // Try multiple timestamp formats
                millis = parseTimestampString(dateString);
            }
        } catch (Exception e) {
            log.error("Failed to parse timestamp: {}", e.getMessage(), e);
        }

        return new Time(millis, 0, 0);
    }

    /**
     * Parse timestamp string with support for multiple formats
     * Supports:
     * - ISO 8601: 2022-10-06T21:34:42.711Z
     * - SQL Server format: 2022-10-06 21:44:38.4120677+00
     * - Other common formats
     */
    private static long parseTimestampString(String dateString) {
        if (dateString == null || dateString.isEmpty()) {
            return 0;
        }

        try {
            // First, try ISO 8601 format (most common)
            try {
                ZonedDateTime parsedTime = ZonedDateTime.parse(dateString, DateTimeFormatter.ISO_DATE_TIME);
                return parsedTime.toInstant().toEpochMilli();
            } catch (Exception e) {
                // Not ISO 8601, try other formats
            }

            // Try SQL Server format: "2022-10-06 21:44:38.4120677+00"
            // Replace space with 'T' and normalize timezone
            String normalizedDate = dateString;
            
            // If it has a space instead of 'T', replace it
            if (normalizedDate.contains(" ") && !normalizedDate.contains("T")) {
                normalizedDate = normalizedDate.replace(" ", "T");
            }
            
            // Handle timezone format: +00 -> +00:00
            if (normalizedDate.matches(".*[+-]\\d{2}$")) {
                normalizedDate = normalizedDate + ":00";
            }
            
            // Try parsing the normalized format
            try {
                ZonedDateTime parsedTime = ZonedDateTime.parse(normalizedDate, DateTimeFormatter.ISO_DATE_TIME);
                return parsedTime.toInstant().toEpochMilli();
            } catch (Exception e) {
                // Still failed, try with offset format
            }

            // Try with explicit offset format
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSSSXXX");
                ZonedDateTime parsedTime = ZonedDateTime.parse(normalizedDate, formatter);
                return parsedTime.toInstant().toEpochMilli();
            } catch (Exception e) {
                // Try without microseconds
            }

            // Try simpler format without microseconds
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
                ZonedDateTime parsedTime = ZonedDateTime.parse(normalizedDate, formatter);
                return parsedTime.toInstant().toEpochMilli();
            } catch (Exception e) {
                log.warn("Could not parse timestamp with any known format: {}", dateString);
            }

        } catch (Exception e) {
            log.error("Unexpected error parsing timestamp: {}", e.getMessage(), e);
        }

        return 0;
    }

    /**
     * Parse SQL data from successful statements
     * Supports both statementText and commandText fields
     */
    private static Data parseData(Event event) {
        Data data = new Data();

        // Try statementText first, then commandText (for nested format)
        Object statementTextObj = getStatementText(event);
        if (statementTextObj != null && !statementTextObj.toString().isEmpty()) {
            String sqlText = statementTextObj.toString();
            data.setOriginalSqlCommand(sqlText);
        } else {
            data.setOriginalSqlCommand(Constants.UNKNOWN_STRING);
        }

        return data;
    }

    /**
     * Parse exception information from failed statements
     * Supports both statementText and commandText fields
     */
    private static ExceptionRecord parseException(Event event) {
        ExceptionRecord exception = new ExceptionRecord();

        // Get error message
        Object errorMessageObj = event.getField(Constants.ERROR_MESSAGE);
        String errorMessage = errorMessageObj != null ?
            errorMessageObj.toString() : Constants.UNKNOWN_STRING;

        // Check class field for LOGIN events (nested format)
        Object classObj = event.getField(Constants.CLASS);
        String eventClass = classObj != null ? classObj.toString() : "";

        // Determine exception type based on error message or class
        if (errorMessage.toLowerCase().contains("authentication") ||
            errorMessage.toLowerCase().contains("login") ||
            errorMessage.toLowerCase().contains("password") ||
            eventClass.equalsIgnoreCase("LOGIN")) {
            exception.setExceptionTypeId(Constants.LOGIN_FAILED);
        } else {
            exception.setExceptionTypeId(Constants.SQL_ERROR);
        }

        exception.setDescription(errorMessage);

        // Set SQL string if available - try both field names
        Object statementTextObj = getStatementText(event);
        if (statementTextObj != null && !statementTextObj.toString().isEmpty()) {
            exception.setSqlString(statementTextObj.toString());
        } else {
            exception.setSqlString(Constants.UNKNOWN_STRING);
        }

        return exception;
    }

    /**
     * Get account ID from event
     */
    private static String getAccountId(Event event) {
        String accountId = Constants.UNKNOWN_STRING;
        
        Object accountIdObj = event.getField(Constants.ACCOUNT_ID);
        if (accountIdObj instanceof String) {
            accountId = accountIdObj.toString();
        } else if (accountIdObj instanceof List<?>) {
            List<?> rawList = (List<?>) accountIdObj;
            List<Object> arrayList = new ArrayList<>(rawList);
            
            if (!arrayList.isEmpty()) {
                accountId = String.valueOf(arrayList.get(0));
            }
        }
        
        return accountId;
    }

    /**
     * Get instance name from event
     */
    private static String getInstanceName(Event event) {
        String instanceName = Constants.UNKNOWN_STRING;
        
        Object instanceNameObj = event.getField(Constants.INSTANCE_NAME);
        if (instanceNameObj instanceof String) {
            instanceName = instanceNameObj.toString();
        } else if (instanceNameObj instanceof List<?>) {
            List<?> rawList = (List<?>) instanceNameObj;
            List<Object> arrayList = new ArrayList<>(rawList);
            
            if (!arrayList.isEmpty()) {
                instanceName = String.valueOf(arrayList.get(0));
            }
        }
        
        return instanceName;
    }
}

// Made with Bob
