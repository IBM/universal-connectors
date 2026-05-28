//
// Copyright 2021-2023 IBM Inc. All rights reserved
// SPDX-License-Identifier: Apache2.0
//

package com.ibm.guardium.snowflakedb.parser;

import co.elastic.logstash.api.Event;
import com.ibm.guardium.snowflakedb.utils.Constants;
import com.ibm.guardium.snowflakedb.utils.DefaultGuardRecordBuilder;
import com.ibm.guardium.universalconnector.commons.structures.Accessor;
import com.ibm.guardium.universalconnector.commons.structures.ExceptionRecord;
import com.ibm.guardium.universalconnector.commons.structures.Record;
import com.ibm.guardium.universalconnector.commons.structures.SessionLocator;
import com.ibm.guardium.universalconnector.commons.structures.Time;

/**
 * Utility class for building error records (UC_PARSER_ERRORS and UC_AUDIT_ERRORS)
 * Extends DefaultGuardRecordBuilder to reuse default value builders
 */
public class ErrorRecordBuilder extends DefaultGuardRecordBuilder {

    /**
     * Create a UC_PARSER_ERROR record when parser fails
     *
     * @param event The event that failed to parse
     * @param errorMessage The error message describing the failure
     * @return A Record with UC_PARSER_ERROR exception
     */
    public static Record createParserErrorRecord(Event event, String errorMessage) {
        return createErrorRecord(event, null, errorMessage, Constants.UC_PARSER_ERROR);
    }

    /**
     * Create a UC_AUDIT_ERROR record when audit processing fails
     *
     * @param event The event that failed to process
     * @param errorMessage The error message describing the failure
     * @return A Record with UC_AUDIT_ERROR exception
     */
    public static Record createAuditErrorRecord(Event event, String errorMessage) {
        return createErrorRecord(event, null, errorMessage, Constants.UC_AUDIT_ERROR);
    }

    /**
     * Parse and create an error record based on parsing state.
     * Similar to DocumentDB's parseRecordException approach: if we successfully parsed Data
     * with SQL command, it's a UC_PARSER_ERROR (data validation issue). Otherwise, it's a
     * UC_AUDIT_ERROR (parsing failure).
     *
     * @param event The event that failed
     * @param partialRecord Partially parsed record (may be null)
     * @param errorMessage The error message describing the failure
     * @return A Record with appropriate exception type
     */
    public static Record parseRecordException(Event event, Record partialRecord, String errorMessage) {
        // Determine error type based on whether we successfully parsed Data with SQL
        String exceptionType;
        if (partialRecord != null && partialRecord.getData() != null
                && partialRecord.getData().getOriginalSqlCommand() != null
                && !partialRecord.getData().getOriginalSqlCommand().isEmpty()) {
            // We successfully parsed the event and extracted SQL - this is a data validation issue
            exceptionType = Constants.UC_PARSER_ERROR;
        } else {
            // We couldn't parse the event properly - this is an audit/parsing failure
            exceptionType = Constants.UC_AUDIT_ERROR;
        }
        return createErrorRecord(event, partialRecord, errorMessage, exceptionType);
    }

    /**
     * Common method to create error records
     *
     * @param event The event that failed
     * @param partialRecord Partially parsed record (may be null), used to extract already-parsed data
     * @param errorMessage The error message
     * @param exceptionTypeId The exception type (UC_PARSER_ERROR or UC_AUDIT_ERROR)
     * @return A Record with exception information and populated fields from event
     */
    private static Record createErrorRecord(Event event, Record partialRecord, String errorMessage, String exceptionTypeId) {
        ErrorRecordBuilder builder = new ErrorRecordBuilder();
        Record errorRecord = new Record();

        // Set exception details
        ExceptionRecord exceptionRecord = builder.buildDefaultExceptionRecord();
        exceptionRecord.setExceptionTypeId(exceptionTypeId);

        // Add appropriate prefix based on error type
        String prefix = exceptionTypeId.equals(Constants.UC_PARSER_ERROR) ? "Parser Error: " : "Audit Error: ";
        String description = prefix + (errorMessage != null ? errorMessage : "");
        exceptionRecord.setDescription(description);

        // Set SQL string - prefer from partialRecord if available, otherwise use full event
        if (partialRecord != null && partialRecord.getData() != null
                && partialRecord.getData().getOriginalSqlCommand() != null
                && !partialRecord.getData().getOriginalSqlCommand().isEmpty()) {
            exceptionRecord.setSqlString(partialRecord.getData().getOriginalSqlCommand());
        } else {
            exceptionRecord.setSqlString(getEventAsString(event));
        }
        errorRecord.setException(exceptionRecord);

        // Set time - use from partialRecord if available, otherwise current time
        if (partialRecord != null && partialRecord.getTime() != null) {
            errorRecord.setTime(partialRecord.getTime());
        } else {
            errorRecord.setTime(new Time(System.currentTimeMillis(), 0, 0));
        }

        // Build session locator - use from partialRecord if available, otherwise build from event
        SessionLocator sessionLocator;
        if (partialRecord != null && partialRecord.getSessionLocator() != null) {
            sessionLocator = partialRecord.getSessionLocator();
        } else {
            sessionLocator = builder.buildDefaultSessionLocator();
            String clientIp = getFieldAsString(event, Constants.CLIENT_IP, null);
            if (clientIp != null) {
                sessionLocator.setClientIp(clientIp);
            }
            String serverIp = getFieldAsString(event, Constants.SERVER_IP, null);
            if (serverIp != null) {
                sessionLocator.setServerIp(serverIp);
            }
            sessionLocator.setServerPort(Constants.SERVER_PORT);
            sessionLocator.setClientPort(-1);
        }
        errorRecord.setSessionLocator(sessionLocator);

        // Build accessor - use from partialRecord if available, otherwise build from event
        Accessor accessor;
        if (partialRecord != null && partialRecord.getAccessor() != null) {
            accessor = partialRecord.getAccessor();
        } else {
            accessor = builder.buildDefaultAccessor();
            String serverHostName = getFieldAsString(event, Constants.SERVER_HOST_NAME, null);
            if (serverHostName != null) {
                accessor.setServerHostName(serverHostName);
            }
            String dbUser = getFieldAsString(event, Constants.USER_NAME, null);
            if (dbUser != null) {
                accessor.setDbUser(dbUser);
            }
            String sourceProgram = getFieldAsString(event, Constants.CLIENT_APPLICATION_ID, null);
            if (sourceProgram != null) {
                accessor.setSourceProgram(sourceProgram);
            }
            String serviceName = getFieldAsString(event, Constants.DATABASE_NAME, null);
            if (serviceName != null) {
                accessor.setServiceName(serviceName);
            }
        }
        errorRecord.setAccessor(accessor);

        // Set session ID and database name - use from partialRecord if available
        if (partialRecord != null && partialRecord.getSessionId() != null) {
            errorRecord.setSessionId(partialRecord.getSessionId());
        } else {
            errorRecord.setSessionId(getFieldAsString(event, Constants.SESSION_ID, Constants.UNKNOWN_STRING));
        }

        if (partialRecord != null && partialRecord.getDbName() != null) {
            errorRecord.setDbName(partialRecord.getDbName());
        } else {
            errorRecord.setDbName(getFieldAsString(event, Constants.DATABASE_NAME, Constants.NOT_AVAILABLE));
        }

        errorRecord.setAppUserName(Constants.NOT_AVAILABLE);

        return errorRecord;
    }

    /**
     * Get field value from event as string, with default if not present
     *
     * @param event The event
     * @param fieldName The field name to retrieve
     * @param defaultValue Default value if field is not present or null
     * @return Field value as string or default value
     */
    private static String getFieldAsString(Event event, String fieldName, String defaultValue) {
        try {
            Object value = event.getField(fieldName);
            if (value != null) {
                return value.toString();
            }
        } catch (Exception e) {
            // Field not present or error accessing it
        }
        return defaultValue;
    }

    /**
     * Convert event to string for error logging
     *
     * @param event The event to convert
     * @return String representation of the event, or error message if conversion fails
     */
    private static String getEventAsString(Event event) {
        try {
            return event.toMap().toString();
        } catch (Exception e) {
            return "Unable to convert event to string";
        }
    }
}