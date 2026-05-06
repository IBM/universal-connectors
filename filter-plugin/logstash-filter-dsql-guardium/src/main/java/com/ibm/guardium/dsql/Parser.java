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

            // Set basic record fields
            record.setAppUserName(Constants.APP_USER_NAME);
            record.setAccessor(parseAccessor(event));
            record.setSessionLocator(parseSessionLocator(event));
            record.setDbName(parseDbName(event));
            record.setTime(parseTimestamp(event));
            
            // Set session ID
            Object sessionIdObj = event.getField(Constants.SESSION_ID);
            if (sessionIdObj != null) {
                record.setSessionId(sessionIdObj.toString());
            } else {
                record.setSessionId(Constants.UNKNOWN_STRING);
            }

            // Parse SQL statement or exception based on exit code
            Object exitCodeObj = event.getField(Constants.EXIT_CODE);
            String exitCode = exitCodeObj != null ? exitCodeObj.toString() : null;
            
            if (exitCode != null && !exitCode.equals(Constants.EXIT_CODE_SUCCESS)) {
                // This is an error/exception
                record.setException(parseException(event));
            } else {
                // This is a successful SQL statement
                Object statementText = event.getField(Constants.STATEMENT_TEXT);
                if (statementText != null && !statementText.toString().isEmpty()) {
                    record.setData(parseData(event));
                }
            }

        } catch (Exception e) {
            log.error("Error parsing DSQL record: {}", e.getMessage(), e);
            throw new ParseException("Failed to parse DSQL record: " + e.getMessage(), 0);
        }

        return record;
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
        accessor.setClientMac(Constants.UNKNOWN_STRING);
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
                
                // Parse ISO 8601 format timestamp
                ZonedDateTime parsedTime = ZonedDateTime.parse(dateString, DateTimeFormatter.ISO_DATE_TIME);
                millis = parsedTime.toInstant().toEpochMilli();
            }
        } catch (Exception e) {
            log.error("Failed to parse timestamp: {}", e.getMessage(), e);
        }

        return new Time(millis, 0, 0);
    }

    /**
     * Parse SQL data from successful statements
     */
    private static Data parseData(Event event) {
        Data data = new Data();

        Object statementTextObj = event.getField(Constants.STATEMENT_TEXT);
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
     */
    private static ExceptionRecord parseException(Event event) {
        ExceptionRecord exception = new ExceptionRecord();

        // Get error message
        Object errorMessageObj = event.getField(Constants.ERROR_MESSAGE);
        String errorMessage = errorMessageObj != null ? 
            errorMessageObj.toString() : Constants.UNKNOWN_STRING;

        // Determine exception type based on error
        if (errorMessage.toLowerCase().contains("authentication") || 
            errorMessage.toLowerCase().contains("login") ||
            errorMessage.toLowerCase().contains("password")) {
            exception.setExceptionTypeId(Constants.LOGIN_FAILED);
        } else {
            exception.setExceptionTypeId(Constants.SQL_ERROR);
        }

        exception.setDescription(errorMessage);

        // Set SQL string if available
        Object statementTextObj = event.getField(Constants.STATEMENT_TEXT);
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
