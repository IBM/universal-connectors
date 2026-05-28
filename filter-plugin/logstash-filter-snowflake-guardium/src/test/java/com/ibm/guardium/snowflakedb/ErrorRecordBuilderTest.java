//
// Copyright 2021-2023 IBM Inc. All rights reserved
// SPDX-License-Identifier: Apache2.0
//

package com.ibm.guardium.snowflakedb;

import com.ibm.guardium.snowflakedb.parser.ErrorRecordBuilder;
import com.ibm.guardium.snowflakedb.utils.Constants;
import com.ibm.guardium.universalconnector.commons.structures.Accessor;
import com.ibm.guardium.universalconnector.commons.structures.Record;
import org.junit.Assert;
import org.junit.Test;
import org.logstash.Event;

import java.util.HashMap;
import java.util.Map;

public class ErrorRecordBuilderTest {

    @Test
    public void testCreateParserErrorRecord() {
        // Create a mock event
        Event event = new Event();
        event.setField("test_field", "test_value");
        event.setField(Constants.QUERY_TEXT, "SELECT * FROM invalid_table");

        String errorMessage = "Failed to parse query syntax";

        // Create parser error record
        Record errorRecord = ErrorRecordBuilder.createParserErrorRecord(event, errorMessage);

        // Verify exception details
        Assert.assertNotNull("Exception should not be null", errorRecord.getException());
        Assert.assertEquals("Exception type should be UC_PARSER_ERROR",
                Constants.UC_PARSER_ERROR, errorRecord.getException().getExceptionTypeId());
        Assert.assertTrue("Description should contain error message",
                errorRecord.getException().getDescription().contains(errorMessage));
        Assert.assertTrue("Description should have 'Parser Error:' prefix",
                errorRecord.getException().getDescription().startsWith("Parser Error:"));
        Assert.assertNotNull("SQL string should not be null",
                errorRecord.getException().getSqlString());

        // Verify time is set
        Assert.assertNotNull("Time should not be null", errorRecord.getTime());
        Assert.assertTrue("Timestamp should be greater than 0",
                errorRecord.getTime().getTimstamp() > 0);

        // Verify session locator is set
        Assert.assertNotNull("SessionLocator should not be null", errorRecord.getSessionLocator());

        // Verify accessor details
        Assert.assertNotNull("Accessor should not be null", errorRecord.getAccessor());
        Assert.assertEquals("DB Protocol should be SNOWFLAKE",
                Constants.DB_PROTOCOL, errorRecord.getAccessor().getDbProtocol());
        Assert.assertEquals("Server Type should be SNOWFLAKE",
                Constants.SERVER_TYPE, errorRecord.getAccessor().getServerType());
        Assert.assertEquals("Language should be SNOWFLAKE",
                Constants.LANGUAGE_SNOWFLAKE, errorRecord.getAccessor().getLanguage());
        Assert.assertEquals("Data type should be TEXT",
                Accessor.DATA_TYPE_GUARDIUM_SHOULD_PARSE_SQL, errorRecord.getAccessor().getDataType());

        // Verify database name
        Assert.assertEquals("DB name should be NA",
                Constants.NOT_AVAILABLE, errorRecord.getDbName());
    }

    @Test
    public void testCreateAuditErrorRecord() {
        // Create a mock event
        Event event = new Event();
        event.setField("test_field", "test_value");
        event.setField(Constants.USER_NAME, "test_user");

        String errorMessage = "Failed to process audit log";

        // Create audit error record
        Record errorRecord = ErrorRecordBuilder.createAuditErrorRecord(event, errorMessage);

        // Verify exception details
        Assert.assertNotNull("Exception should not be null", errorRecord.getException());
        Assert.assertEquals("Exception type should be UC_AUDIT_ERROR",
                Constants.UC_AUDIT_ERROR, errorRecord.getException().getExceptionTypeId());
        Assert.assertTrue("Description should contain error message",
                errorRecord.getException().getDescription().contains(errorMessage));
        Assert.assertTrue("Description should have 'Audit Error:' prefix",
                errorRecord.getException().getDescription().startsWith("Audit Error:"));
        Assert.assertNotNull("SQL string should not be null",
                errorRecord.getException().getSqlString());

        // Verify time is set
        Assert.assertNotNull("Time should not be null", errorRecord.getTime());
        Assert.assertTrue("Timestamp should be greater than 0",
                errorRecord.getTime().getTimstamp() > 0);

        // Verify session locator is set
        Assert.assertNotNull("SessionLocator should not be null", errorRecord.getSessionLocator());

        // Verify accessor details
        Assert.assertNotNull("Accessor should not be null", errorRecord.getAccessor());
        Assert.assertEquals("DB Protocol should be SNOWFLAKE",
                Constants.DB_PROTOCOL, errorRecord.getAccessor().getDbProtocol());
        Assert.assertEquals("Server Type should be SNOWFLAKE",
                Constants.SERVER_TYPE, errorRecord.getAccessor().getServerType());
        Assert.assertEquals("Language should be SNOWFLAKE",
                Constants.LANGUAGE_SNOWFLAKE, errorRecord.getAccessor().getLanguage());
        Assert.assertEquals("Data type should be TEXT",
                Accessor.DATA_TYPE_GUARDIUM_SHOULD_PARSE_SQL, errorRecord.getAccessor().getDataType());

        // Verify database name
        Assert.assertEquals("DB name should be NA",
                Constants.NOT_AVAILABLE, errorRecord.getDbName());
    }

    @Test
    public void testParserErrorWithNullErrorMessage() {
        Event event = new Event();
        event.setField("test_field", "test_value");

        // Create parser error record with null error message
        Record errorRecord = ErrorRecordBuilder.createParserErrorRecord(event, null);

        // Verify it handles null gracefully
        Assert.assertNotNull("Exception should not be null", errorRecord.getException());
        Assert.assertEquals("Exception type should be UC_PARSER_ERROR",
                Constants.UC_PARSER_ERROR, errorRecord.getException().getExceptionTypeId());
        Assert.assertTrue("Description should have 'Parser Error:' prefix",
                errorRecord.getException().getDescription().startsWith("Parser Error:"));
    }

    @Test
    public void testAuditErrorWithEmptyErrorMessage() {
        Event event = new Event();
        event.setField("test_field", "test_value");

        // Create audit error record with empty error message
        Record errorRecord = ErrorRecordBuilder.createAuditErrorRecord(event, "");

        // Verify it handles empty string gracefully
        Assert.assertNotNull("Exception should not be null", errorRecord.getException());
        Assert.assertEquals("Exception type should be UC_AUDIT_ERROR",
                Constants.UC_AUDIT_ERROR, errorRecord.getException().getExceptionTypeId());
        Assert.assertTrue("Description should have 'Audit Error:' prefix",
                errorRecord.getException().getDescription().startsWith("Audit Error:"));
    }

    @Test
    public void testErrorRecordContainsEventData() {
        Event event = new Event();
        event.setField(Constants.QUERY_TEXT, "SELECT * FROM test_table");
        event.setField(Constants.USER_NAME, "test_user");
        event.setField(Constants.DATABASE_NAME, "test_db");

        String errorMessage = "Test error";

        // Create parser error record
        Record errorRecord = ErrorRecordBuilder.createParserErrorRecord(event, errorMessage);

        // Verify SQL string contains event data
        String sqlString = errorRecord.getException().getSqlString();
        Assert.assertNotNull("SQL string should not be null", sqlString);
        Assert.assertTrue("SQL string should contain event data", sqlString.length() > 0);
    }

    @Test
    public void testErrorRecordPopulatesFieldsFromEvent() {
        // Create event with complete data
        Event event = new Event();
        event.setField(Constants.USER_NAME, "test_user");
        event.setField(Constants.DATABASE_NAME, "test_db");
        event.setField(Constants.SERVER_HOST_NAME, "test.snowflakecomputing.com");
        event.setField(Constants.CLIENT_IP, "192.168.1.100");
        event.setField(Constants.SERVER_IP, "10.0.0.1");
        event.setField(Constants.SESSION_ID, "session123");
        event.setField(Constants.CLIENT_APPLICATION_ID, "JDBC 3.13.6");

        String errorMessage = "Test error with complete data";

        // Create parser error record
        Record errorRecord = ErrorRecordBuilder.createParserErrorRecord(event, errorMessage);

        // Verify accessor fields are populated from event
        Assert.assertEquals("DB user should be from event",
                "test_user", errorRecord.getAccessor().getDbUser());
        Assert.assertEquals("Server hostname should be from event",
                "test.snowflakecomputing.com", errorRecord.getAccessor().getServerHostName());
        Assert.assertEquals("Service name should be from event",
                "test_db", errorRecord.getAccessor().getServiceName());
        Assert.assertEquals("Source program should be from event",
                "JDBC 3.13.6", errorRecord.getAccessor().getSourceProgram());

        // Verify session locator fields are populated from event
        Assert.assertEquals("Client IP should be from event",
                "192.168.1.100", errorRecord.getSessionLocator().getClientIp());
        Assert.assertEquals("Server IP should be from event",
                "10.0.0.1", errorRecord.getSessionLocator().getServerIp());
        Assert.assertEquals("Server port should be set",
                Constants.SERVER_PORT, Integer.valueOf(errorRecord.getSessionLocator().getServerPort()));

        // Verify session ID and database name
        Assert.assertEquals("Session ID should be from event",
                "session123", errorRecord.getSessionId());
        Assert.assertEquals("DB name should be from event",
                "test_db", errorRecord.getDbName());
    }

    @Test
    public void testErrorRecordWithMissingFields() {
        // Create event with minimal data
        Event event = new Event();
        event.setField(Constants.USER_NAME, "test_user");

        String errorMessage = "Test error with missing fields";

        // Create audit error record
        Record errorRecord = ErrorRecordBuilder.createAuditErrorRecord(event, errorMessage);

        // Verify default values are used for missing fields
        Assert.assertEquals("Server hostname should use default",
                Constants.UNKNOWN_STRING, errorRecord.getAccessor().getServerHostName());
        Assert.assertEquals("Client IP should use default",
                Constants.DEFAULT_IP, errorRecord.getSessionLocator().getClientIp());
        Assert.assertEquals("Server IP should use default",
                Constants.DEFAULT_IP, errorRecord.getSessionLocator().getServerIp());
        Assert.assertEquals("DB name should use default",
                Constants.NOT_AVAILABLE, errorRecord.getDbName());
        Assert.assertEquals("Session ID should use default",
                Constants.UNKNOWN_STRING, errorRecord.getSessionId());

        // Verify user name is still populated
        Assert.assertEquals("DB user should be from event",
                "test_user", errorRecord.getAccessor().getDbUser());
    }
}