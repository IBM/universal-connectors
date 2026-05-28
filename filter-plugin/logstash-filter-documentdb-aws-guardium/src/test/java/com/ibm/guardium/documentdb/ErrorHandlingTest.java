/*
 * Copyright 2022-2023 IBM Inc. All rights reserved
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.guardium.documentdb;

import com.ibm.guardium.universalconnector.commons.structures.Construct;
import com.ibm.guardium.universalconnector.commons.structures.Data;
import com.ibm.guardium.universalconnector.commons.structures.Record;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for error handling in DocumentDB parser.
 * Tests UC_AUDIT_ERROR and UC_PARSER_ERROR scenarios.
 */
public class ErrorHandlingTest {

    private final Parser parser = new Parser();

    @Test
    public void testUCAuditError_WithNullRecord() {
        // Test UC_AUDIT_ERROR when record is null
        String error = Constants.ERROR_PARSING_AUDIT_EVENT;
        String jsonString = "{\"atype\":\"authCheck\",\"ts\":\"invalid_timestamp\",\"remote_ip\":\"203.0.113.10:10862\",\"users\":[{\"user\":\"testuser\",\"db\":\"admin\"}],\"param\":{\"command\":\"find\",\"ns\":\"admin.test_collection\"}}";
        
        Record result = parser.parseRecordException(null, error, jsonString);
        
        // Verify it's UC_AUDIT_ERROR
        assertNotNull(result, "Result should not be null");
        assertNotNull(result.getException(), "Exception should not be null");
        assertEquals(Constants.UC_AUDIT_ERROR, result.getException().getExceptionTypeId(), "Should be UC_AUDIT_ERROR");
        assertTrue(result.getException().getDescription().contains(error), "Description should contain error message");
        assertNotNull(result.getException().getSqlString(), "SQL string should not be null");
        
        // Verify partial information was extracted
        assertEquals("admin", result.getDbName(), "DB name should be extracted");
        assertEquals("testuser", result.getAppUserName(), "App user name should be extracted");
        assertEquals("203.0.113.10", result.getSessionLocator().getClientIp(), "Client IP should be extracted");
        assertEquals(10862, result.getSessionLocator().getClientPort(), "Client port should be extracted");
        
        // Verify accessor fields are not null
        assertNotNull(result.getAccessor(), "Accessor should not be null");
        assertEquals("testuser", result.getAccessor().getDbUser(), "DB user should be extracted");
        assertEquals("admin", result.getAccessor().getServiceName(), "Service name should be extracted");
        assertEquals(Constants.UNKNOWN_STRING, result.getAccessor().getServerOs(), "Server OS should be empty string");
        assertEquals(Constants.UNKNOWN_STRING, result.getAccessor().getClientOs(), "Client OS should be empty string");
        assertEquals(Constants.UNKNOWN_STRING, result.getAccessor().getSourceProgram(), "Source program should be empty string");
    }

    @Test
    public void testUCAuditError_WithMalformedJSON() {
        // Test UC_AUDIT_ERROR with completely malformed JSON
        String error = Constants.ERROR_PARSING_AUDIT_EVENT;
        String jsonString = "{\"atype\":\"authCheck\",\"malformed_structure\":}";
        
        Record result = parser.parseRecordException(null, error, jsonString);
        
        // Verify it's UC_AUDIT_ERROR
        assertNotNull(result, "Result should not be null");
        assertNotNull(result.getException(), "Exception should not be null");
        assertEquals(Constants.UC_AUDIT_ERROR, result.getException().getExceptionTypeId(), "Should be UC_AUDIT_ERROR");
        
        // Verify default values are used when extraction fails
        assertEquals(Constants.NOT_AVAILABLE, result.getDbName(), "DB name should be N.A.");
        assertEquals(Constants.NOT_AVAILABLE, result.getAppUserName(), "App user name should be N.A.");
        assertEquals(Constants.DEFAULT_IP, result.getSessionLocator().getClientIp(), "Client IP should be default");
        
        // Verify accessor fields are not null
        assertNotNull(result.getAccessor(), "Accessor should not be null");
        assertEquals(Constants.UNKNOWN_STRING, result.getAccessor().getServerOs(), "Server OS should be empty string");
        assertEquals(Constants.UNKNOWN_STRING, result.getAccessor().getClientOs(), "Client OS should be empty string");
    }

    @Test
    public void testUCParserError_WithDataSet() {
        // Test UC_PARSER_ERROR when record has data with originalSqlCommand
        Record record = new Record();
        Data data = new Data();
        Construct construct = new Construct();
        construct.setFullSql("test sql command");
        data.setConstruct(construct);
        data.setOriginalSqlCommand("test sql command");
        record.setData(data);
        
        String error = "Test parser error";
        String jsonString = "{\"atype\":\"authCheck\",\"ts\":\"123456789\",\"remote_ip\":\"192.168.1.1:5000\",\"users\":[{\"user\":\"parseruser\",\"db\":\"testdb\"}],\"param\":{\"command\":\"update\",\"ns\":\"testdb.collection\"}}";
        
        Record result = parser.parseRecordException(record, error, jsonString);
        
        // Verify it's UC_PARSER_ERROR
        assertNotNull(result, "Result should not be null");
        assertNotNull(result.getException(), "Exception should not be null");
        assertEquals(Constants.UC_PARSER_ERROR, result.getException().getExceptionTypeId(), "Should be UC_PARSER_ERROR");
        assertEquals("test sql command", result.getException().getSqlString(), "SQL string should be from original data");
        assertTrue(result.getException().getDescription().contains(error), "Description should contain error message");
        
        // Verify partial information was still extracted from JSON
        assertEquals("testdb", result.getDbName(), "DB name should be extracted");
        assertEquals("parseruser", result.getAppUserName(), "App user name should be extracted");
        assertEquals("192.168.1.1", result.getSessionLocator().getClientIp(), "Client IP should be extracted");
        assertEquals(5000, result.getSessionLocator().getClientPort(), "Client port should be extracted");
        
        // Verify accessor fields are not null
        assertNotNull(result.getAccessor(), "Accessor should not be null");
        assertEquals("parseruser", result.getAccessor().getDbUser(), "DB user should be extracted");
        assertEquals("testdb", result.getAccessor().getServiceName(), "Service name should be extracted");
        assertEquals(Constants.UNKNOWN_STRING, result.getAccessor().getServerOs(), "Server OS should be empty string");
        assertEquals(Constants.UNKNOWN_STRING, result.getAccessor().getClientOs(), "Client OS should be empty string");
    }

    @Test
    public void testUCParserError_WithEmptyOriginalSqlCommand() {
        // Test that empty originalSqlCommand results in UC_AUDIT_ERROR
        Record record = new Record();
        Data data = new Data();
        data.setOriginalSqlCommand("");
        record.setData(data);
        
        String error = "Test error";
        String jsonString = "{\"atype\":\"authCheck\"}";
        
        Record result = parser.parseRecordException(record, error, jsonString);
        
        // Should be UC_AUDIT_ERROR because originalSqlCommand is empty
        assertEquals(Constants.UC_AUDIT_ERROR, result.getException().getExceptionTypeId(), "Should be UC_AUDIT_ERROR");
    }

    @Test
    public void testErrorDescription_ContainsOriginalError() {
        // Test that error description contains the original error message
        String error = "Custom error message for testing";
        String jsonString = "{\"atype\":\"authCheck\"}";
        
        Record result = parser.parseRecordException(null, error, jsonString);
        
        assertNotNull(result.getException(), "Exception should not be null");
        assertTrue(result.getException().getDescription().contains(error), "Description should contain original error");
    }

    @Test
    public void testPartialExtraction_AuthCheckEvent() {
        // Test partial extraction for authCheck event with various fields
        String error = "Test error";
        String jsonString = "{\"atype\":\"authCheck\",\"ts\":\"invalid\",\"remote_ip\":\"10.0.0.1:8080\",\"users\":[{\"user\":\"admin\",\"db\":\"mydb\"}],\"param\":{\"command\":\"delete\",\"ns\":\"mydb.mycollection\"},\"appName\":\"MyApp\"}";
        
        Record result = parser.parseRecordException(null, error, jsonString);
        
        // Verify all extractable fields were extracted
        assertEquals("mydb", result.getDbName(), "DB name");
        assertEquals("admin", result.getAppUserName(), "App user name");
        assertEquals("10.0.0.1", result.getSessionLocator().getClientIp(), "Client IP");
        assertEquals(8080, result.getSessionLocator().getClientPort(), "Client port");
        assertEquals("admin", result.getAccessor().getDbUser(), "DB user");
        assertEquals("mydb", result.getAccessor().getServiceName(), "Service name");
        assertEquals("MyApp", result.getAccessor().getSourceProgram(), "Source program");
    }

    @Test
    public void testPartialExtraction_AuditEvent() {
        // Test partial extraction for regular audit event
        String error = "Test error";
        String jsonString = "{\"atype\":\"createCollection\",\"ts\":\"invalid\",\"user\":\"dbadmin\",\"param\":{\"ns\":\"production.orders\"}}";
        
        Record result = parser.parseRecordException(null, error, jsonString);
        
        // Verify extraction for audit event
        assertEquals("production", result.getDbName(), "DB name");
        assertNotNull(result.getAccessor(), "Accessor should not be null");
        assertEquals("dbadmin", result.getAccessor().getDbUser(), "DB user");
    }

    @Test
    public void testPartialExtraction_ProfilerEvent() {
        // Test partial extraction for profiler event
        String error = "Test error";
        String jsonString = "{\"command\":{\"find\":\"users\"},\"ns\":\"appdb.users\",\"user\":\"appuser\"}";
        
        Record result = parser.parseRecordException(null, error, jsonString);
        
        // Verify extraction for profiler event
        assertEquals("appdb", result.getDbName(), "DB name");
        assertNotNull(result.getAccessor(), "Accessor should not be null");
    }
}

// Made with Bob
