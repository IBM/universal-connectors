//
// Copyright 2021-2023 IBM Inc. All rights reserved
// SPDX-License-Identifier: Apache2.0
//

package com.ibm.guardium.snowflakedb;

import co.elastic.logstash.api.Configuration;
import co.elastic.logstash.api.Context;
import co.elastic.logstash.api.Event;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.ibm.guardium.snowflakedb.utils.Constants;
import com.ibm.guardium.universalconnector.commons.GuardConstants;
import org.junit.Assert;
import org.junit.Test;
import org.logstash.plugins.ConfigurationImpl;

import java.util.*;

public class GuardiumSnowflakeFilterTest {

    private final static Context context = null;
    private final static Configuration config = new ConfigurationImpl(Collections.emptyMap());

    @Test
    public void testUnknownEventTypeCreatesAuditError() {
        // Test that unknown event_type creates UC_AUDIT_ERROR
        GuardiumSnowflakeFilter filter = new GuardiumSnowflakeFilter("test-id", config, context);
        
        Event event = new org.logstash.Event();
        event.setField(Constants.EVENT_TYPE, "UNKNOWN_TYPE");
        event.setField(Constants.USER_NAME, "test_user");
        event.setField(Constants.QUERY_TIMESTAMP, "2024-01-01T10:00:00.000Z");
        event.setField(Constants.CLIENT_IP, "192.168.1.1");
        event.setField(Constants.SERVER_IP, "10.0.0.1");
        event.setField(Constants.SERVER_HOST_NAME, "test.snowflakecomputing.com");
        event.setField(Constants.DATABASE_NAME, "test_db");
        event.setField(Constants.QUERY_TEXT, "SELECT 1");
        
        Collection<Event> results = filter.filter(Collections.singletonList(event), null);
        
        Assert.assertEquals("Should return 1 event", 1, results.size());
        Event resultEvent = results.iterator().next();
        
        Object guardRecordObj = resultEvent.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME);
        Assert.assertNotNull("GuardRecord should be present", guardRecordObj);
        
        Gson gson = new Gson();
        JsonObject guardRecord = gson.fromJson(guardRecordObj.toString(), JsonObject.class);
        JsonObject exception = guardRecord.getAsJsonObject("exception");
        
        Assert.assertNotNull("Exception should be present", exception);
        Assert.assertEquals("Should be UC_AUDIT_ERROR", 
                Constants.UC_AUDIT_ERROR, 
                exception.get("exceptionTypeId").getAsString());
        Assert.assertTrue("Description should mention unknown event_type",
                exception.get("description").getAsString().toLowerCase().contains("unknown"));
    }

    @Test
    public void testInvalidTimestampCreatesAuditError() {
        // Test that invalid timestamp creates UC_AUDIT_ERROR
        // timestamp parsing fails early, before SQL extraction,
        // so no partial record with Data exists -> UC_AUDIT_ERROR
        GuardiumSnowflakeFilter filter = new GuardiumSnowflakeFilter("test-id", config, context);
        
        Event event = new org.logstash.Event();
        event.setField(Constants.EVENT_TYPE, Constants.SUCCESS);
        event.setField(Constants.USER_NAME, "test_user");
        event.setField(Constants.QUERY_TIMESTAMP, "INVALID_TIMESTAMP");
        event.setField(Constants.CLIENT_IP, "192.168.1.1");
        event.setField(Constants.SERVER_IP, "10.0.0.1");
        event.setField(Constants.SERVER_HOST_NAME, "test.snowflakecomputing.com");
        event.setField(Constants.DATABASE_NAME, "test_db");
        event.setField(Constants.QUERY_TEXT, "SELECT 1");
        event.setField(Constants.SESSION_ID, "session123");
        event.setField(Constants.QUERY_ID, "query123");
        
        Collection<Event> results = filter.filter(Collections.singletonList(event), null);
        
        Assert.assertEquals("Should return 1 event", 1, results.size());
        Event resultEvent = results.iterator().next();
        
        Object guardRecordObj = resultEvent.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME);
        Assert.assertNotNull("GuardRecord should be present", guardRecordObj);
        
        Gson gson = new Gson();
        JsonObject guardRecord = gson.fromJson(guardRecordObj.toString(), JsonObject.class);
        JsonObject exception = guardRecord.getAsJsonObject("exception");
        
        Assert.assertNotNull("Exception should be present", exception);
        Assert.assertEquals("Should be UC_AUDIT_ERROR (parsing failed before SQL extraction)",
                Constants.UC_AUDIT_ERROR,
                exception.get("exceptionTypeId").getAsString());
    }

    @Test
    public void testValidSuccessEventProcessedCorrectly() {
        // Test that valid SUCCESS event is processed without errors
        GuardiumSnowflakeFilter filter = new GuardiumSnowflakeFilter("test-id", config, context);
        
        Event event = FakeEventFactory.getSuccessEvent();
        event.setField(Constants.EVENT_TYPE, Constants.SUCCESS);
        
        Collection<Event> results = filter.filter(Collections.singletonList(event), null);
        
        Assert.assertEquals("Should return 1 event", 1, results.size());
        Event resultEvent = results.iterator().next();
        
        Object guardRecordObj = resultEvent.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME);
        Assert.assertNotNull("GuardRecord should be present", guardRecordObj);
        
        Gson gson = new Gson();
        JsonObject guardRecord = gson.fromJson(guardRecordObj.toString(), JsonObject.class);
        
        // Should not have UC error exception for valid event
        if (guardRecord.has("exception") && !guardRecord.get("exception").isJsonNull()) {
            JsonObject exception = guardRecord.getAsJsonObject("exception");
            if (exception.has("exceptionTypeId")) {
                String exceptionType = exception.get("exceptionTypeId").getAsString();
                Assert.assertNotEquals("Should not be UC_PARSER_ERROR",
                        Constants.UC_PARSER_ERROR, exceptionType);
                Assert.assertNotEquals("Should not be UC_AUDIT_ERROR",
                        Constants.UC_AUDIT_ERROR, exceptionType);
            }
        }
    }

    @Test
    public void testValidSQLErrorEventProcessedCorrectly() {
        // Test that valid SQL_ERROR event is processed without UC errors
        GuardiumSnowflakeFilter filter = new GuardiumSnowflakeFilter("test-id", config, context);
        
        Event event = FakeEventFactory.getSQLErrorEvent();
        event.setField(Constants.EVENT_TYPE, Constants.SQL_ERROR);
        
        Collection<Event> results = filter.filter(Collections.singletonList(event), null);
        
        Assert.assertEquals("Should return 1 event", 1, results.size());
        Event resultEvent = results.iterator().next();
        
        Object guardRecordObj = resultEvent.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME);
        Assert.assertNotNull("GuardRecord should be present", guardRecordObj);
        
        Gson gson = new Gson();
        JsonObject guardRecord = gson.fromJson(guardRecordObj.toString(), JsonObject.class);
        JsonObject exception = guardRecord.getAsJsonObject("exception");
        
        Assert.assertNotNull("Exception should be present for SQL error", exception);
        // Should have SQL_ERROR, not UC_PARSER_ERROR or UC_AUDIT_ERROR
        String exceptionType = exception.get("exceptionTypeId").getAsString();
        Assert.assertNotEquals("Should not be UC_PARSER_ERROR", 
                Constants.UC_PARSER_ERROR, exceptionType);
        Assert.assertNotEquals("Should not be UC_AUDIT_ERROR", 
                Constants.UC_AUDIT_ERROR, exceptionType);
    }

    @Test
    public void testValidLoginFailedEventProcessedCorrectly() {
        // Test that valid LOGIN_FAILED event is processed without UC errors
        GuardiumSnowflakeFilter filter = new GuardiumSnowflakeFilter("test-id", config, context);
        
        Event event = FakeEventFactory.getAuthErrorEvent();
        event.setField(Constants.EVENT_TYPE, Constants.LOGIN_FAILED);
        
        Collection<Event> results = filter.filter(Collections.singletonList(event), null);
        
        Assert.assertEquals("Should return 1 event", 1, results.size());
        Event resultEvent = results.iterator().next();
        
        Object guardRecordObj = resultEvent.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME);
        Assert.assertNotNull("GuardRecord should be present", guardRecordObj);
        
        Gson gson = new Gson();
        JsonObject guardRecord = gson.fromJson(guardRecordObj.toString(), JsonObject.class);
        JsonObject exception = guardRecord.getAsJsonObject("exception");
        
        Assert.assertNotNull("Exception should be present for login failure", exception);
        // Should have LOGIN_FAILED, not UC_PARSER_ERROR or UC_AUDIT_ERROR
        String exceptionType = exception.get("exceptionTypeId").getAsString();
        Assert.assertNotEquals("Should not be UC_PARSER_ERROR", 
                Constants.UC_PARSER_ERROR, exceptionType);
        Assert.assertNotEquals("Should not be UC_AUDIT_ERROR", 
                Constants.UC_AUDIT_ERROR, exceptionType);
    }

    @Test
    public void testEmptyEventTypeCreatesAuditError() {
        // Test that empty event_type creates UC_AUDIT_ERROR
        GuardiumSnowflakeFilter filter = new GuardiumSnowflakeFilter("test-id", config, context);
        
        Event event = new org.logstash.Event();
        event.setField(Constants.EVENT_TYPE, "");
        event.setField(Constants.USER_NAME, "test_user");
        
        Collection<Event> results = filter.filter(Collections.singletonList(event), null);
        
        Assert.assertEquals("Should return 1 event", 1, results.size());
        Event resultEvent = results.iterator().next();
        
        Object guardRecordObj = resultEvent.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME);
        Assert.assertNotNull("GuardRecord should be present", guardRecordObj);
        
        Gson gson = new Gson();
        JsonObject guardRecord = gson.fromJson(guardRecordObj.toString(), JsonObject.class);
        JsonObject exception = guardRecord.getAsJsonObject("exception");
        
        Assert.assertNotNull("Exception should be present", exception);
        Assert.assertEquals("Should be UC_AUDIT_ERROR", 
                Constants.UC_AUDIT_ERROR, 
                exception.get("exceptionTypeId").getAsString());
    }

    @Test
    public void testCaseInsensitiveEventType() {
        // Test that event_type is case-insensitive (SUCCESS, success, SuCcEsS should all work)
        GuardiumSnowflakeFilter filter = new GuardiumSnowflakeFilter("test-id", config, context);
        
        Event event = FakeEventFactory.getSuccessEvent();
        event.setField(Constants.EVENT_TYPE, "success"); // lowercase
        
        Collection<Event> results = filter.filter(Collections.singletonList(event), null);
        
        Assert.assertEquals("Should return 1 event", 1, results.size());
        Event resultEvent = results.iterator().next();
        
        Object guardRecordObj = resultEvent.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME);
        Assert.assertNotNull("GuardRecord should be present", guardRecordObj);
        
        Gson gson = new Gson();
        JsonObject guardRecord = gson.fromJson(guardRecordObj.toString(), JsonObject.class);
        
        // Should process successfully without UC errors
        if (guardRecord.has("exception") && !guardRecord.get("exception").isJsonNull()) {
            JsonObject exception = guardRecord.getAsJsonObject("exception");
            if (exception.has("exceptionTypeId")) {
                String exceptionType = exception.get("exceptionTypeId").getAsString();
                Assert.assertNotEquals("Should not be UC_PARSER_ERROR",
                        Constants.UC_PARSER_ERROR, exceptionType);
                Assert.assertNotEquals("Should not be UC_AUDIT_ERROR",
                        Constants.UC_AUDIT_ERROR, exceptionType);
            }
        }
    }
}