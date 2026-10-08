//
// Copyright 2020-2021 IBM Inc. All rights reserved
// SPDX-License-Identifier: Apache2.0
//

package com.ibm.guardium.singlestore;

import co.elastic.logstash.api.Context;
import co.elastic.logstash.api.Event;
import co.elastic.logstash.api.FilterMatchListener;
import org.apache.commons.lang3.StringEscapeUtils;
import org.junit.Assert;
import org.junit.Test;
import org.logstash.plugins.ContextImpl;

import com.ibm.guardium.universalconnector.commons.GuardConstants;
import com.ibm.guardium.universalconnector.commons.structures.Record;
import com.google.gson.Gson;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class SingleStoredbGuardiumFilterTest {


    /**
     * To feed Guardium universal connector, a "GuardRecord" fields must exist.
     * Filter should add field "GuardRecord" to the Event, which Universal connector then inserts into Guardium.
     */
    @Test
    public void testFieldGuardRecord_singlestoredb() {
        System.out.println("                                        ================================");
        System.out.println("========================================||testFieldGuardRecord_singlestoredb||========================================");
        System.out.println("                                        ================================");

        String singlestoreString = "133855,2024-06-24 07:16:16.901,UTC,singlestore.node1:3306,agg,1,100000,root,vector_db,,1308432953418920798,CREATE DATABASE `uc_vector_db`";
        Context context = new ContextImpl(null, null);
        SingleStoredbGuardiumFilter filter = new SingleStoredbGuardiumFilter("test-id", null, context);

        Event e = ParserTest.getParsedEvent(singlestoreString);

        TestMatchListener matchListener = new TestMatchListener();

        if (e != null) {
            e.setField(Constants.SERVER_IP, "10.0.0.1");
            e.setField(Constants.SERVER_HOSTNAME, "singlestore.server.com");
            Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
            Assert.assertEquals(1, results.size());
            Assert.assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
        }

    }

    @Test
    public void testShortLogLineIsSkippedSilently() {
        // 4-field internal audit entries like "126245,C,SUCCESS,0" should not produce
        // an error record — they must be silently skipped without tagging or crashing.
        String shortLog = "126245,C,SUCCESS,0";

        Context context = new ContextImpl(null, null);
        SingleStoredbGuardiumFilter filter = new SingleStoredbGuardiumFilter("test-id", null, context);

        Event e = new org.logstash.Event();
        e.setField("message", shortLog);
        e.setField(Constants.SERVER_IP, "10.0.0.1");
        e.setField(Constants.SERVER_HOSTNAME, "singlestore.server.com");

        TestMatchListener matchListener = new TestMatchListener();
        Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);

        Assert.assertEquals(1, results.size());
        // GuardRecord must NOT be set — the event was skipped
        Assert.assertNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
    }

    @Test
    public void TestCleanQuery() {
        String originalQuery = "/* ApplicationName=DBeaver 25.1.0 - SQLEditor <Script-13.sql> */ SELECT * FROM customers WHERE city = \"Pune\"";
        String expectedCleanedQuery = "SELECT * FROM customers WHERE city = \"Pune\"";

        Parser parser = new Parser();
        String actualCleanedQuery = parser.cleanQuery(originalQuery);

        Assert.assertEquals(expectedCleanedQuery, actualCleanedQuery);
    }

    @Test
    public void testServerHostnameFromEventMetadataTakesPrecedence() {
        String logMessage = "133855,2024-06-24 07:16:16.901,UTC,singlestore.node1:3306,agg,1,100000,root,vector_db,,1308432953418920798,CREATE DATABASE `uc_vector_db`";

        Context context = new ContextImpl(null, null);
        SingleStoredbGuardiumFilter filter = new SingleStoredbGuardiumFilter("test-id", null, context);

        Event e = new org.logstash.Event();
        e.setField("message", logMessage);
        e.setField("serverHostname", "filebeat.override.hostname.com");
        e.setField("serverIP", "10.0.0.1");

        TestMatchListener matchListener = new TestMatchListener();
        Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);

        Assert.assertEquals(1, results.size());
        Assert.assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));

        Record record = new Gson().fromJson(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME).toString(), Record.class);
        Assert.assertEquals("filebeat.override.hostname.com", record.getAccessor().getServerHostName());
    }

    @Test
    public void testServerHostnameFallsBackToAuditLogWhenMetadataMissing() {
        String logMessage = "133855,2024-06-24 07:16:16.901,UTC,singlestore.node1:3306,agg,1,100000,root,vector_db,,1308432953418920798,CREATE DATABASE `uc_vector_db`";

        Context context = new ContextImpl(null, null);
        SingleStoredbGuardiumFilter filter = new SingleStoredbGuardiumFilter("test-id", null, context);

        Event e = new org.logstash.Event();
        e.setField("message", logMessage);
        // Do NOT set serverHostname on the event — simulate absence of Filebeat metadata
        e.setField("serverIP", "10.0.0.1");

        TestMatchListener matchListener = new TestMatchListener();
        Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);

        Assert.assertEquals(1, results.size());
        Assert.assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));

        Record record = new Gson().fromJson(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME).toString(), Record.class);
        Assert.assertEquals("singlestore.node1", record.getAccessor().getServerHostName());
    }

    @Test
    public void testDMLInsertQuery() {
        String logMessage = "133894,2024-06-24 07:20:50.842,UTC,singlestore.node1:3306,agg,1,100000,root,uc_vector_db,temp_1_31066_5,17413739249988095469,INSERT INTO employees VALUES (1\\, 'John Doe'\\, 'Engineer'\\, 75000.00)";

        Context context = new ContextImpl(null, null);
        SingleStoredbGuardiumFilter filter = new SingleStoredbGuardiumFilter("test-id", null, context);

        Event e = new org.logstash.Event();
        e.setField("message", logMessage);
        e.setField(Constants.SERVER_IP, "10.0.0.1");

        TestMatchListener matchListener = new TestMatchListener();
        Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);

        Assert.assertEquals(1, results.size());
        Assert.assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));

        Record record = new Gson().fromJson(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME).toString(), Record.class);
        Assert.assertEquals("INSERT INTO employees VALUES (1, 'John Doe', 'Engineer', 75000.00)", record.getData().getOriginalSqlCommand());
        Assert.assertEquals("root", record.getAccessor().getDbUser());
        Assert.assertEquals("uc_vector_db", record.getDbName());
    }

    @Test
    public void testDMLUpdateQuery() {
        String logMessage = "133894,2024-06-24 07:20:50.842,UTC,singlestore.node1:3306,agg,1,100000,root,uc_vector_db,temp_1_31066_5,17413739249988095469,UPDATE employees SET salary = 80000.00 WHERE id = 1";

        Context context = new ContextImpl(null, null);
        SingleStoredbGuardiumFilter filter = new SingleStoredbGuardiumFilter("test-id", null, context);

        Event e = new org.logstash.Event();
        e.setField("message", logMessage);
        e.setField(Constants.SERVER_IP, "10.0.0.1");

        TestMatchListener matchListener = new TestMatchListener();
        Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);

        Assert.assertEquals(1, results.size());
        Assert.assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));

        Record record = new Gson().fromJson(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME).toString(), Record.class);
        Assert.assertEquals("UPDATE employees SET salary = 80000.00 WHERE id = 1", record.getData().getOriginalSqlCommand());
    }

    @Test
    public void testDMLDeleteQuery() {
        String logMessage = "133894,2024-06-24 07:20:50.842,UTC,singlestore.node1:3306,agg,1,100000,root,uc_vector_db,temp_1_31066_5,17413739249988095469,DELETE FROM employees WHERE id = 3";

        Context context = new ContextImpl(null, null);
        SingleStoredbGuardiumFilter filter = new SingleStoredbGuardiumFilter("test-id", null, context);

        Event e = new org.logstash.Event();
        e.setField("message", logMessage);
        e.setField(Constants.SERVER_IP, "10.0.0.1");

        TestMatchListener matchListener = new TestMatchListener();
        Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);

        Assert.assertEquals(1, results.size());
        Assert.assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));

        Record record = new Gson().fromJson(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME).toString(), Record.class);
        Assert.assertEquals("DELETE FROM employees WHERE id = 3", record.getData().getOriginalSqlCommand());
    }

    @Test
    public void testEscapedCommasInQuery() {
        String logMessage = "21596,2024-06-10 10:42:43.236,UTC,singlestore.node1:3306,agg,1,99995,root,information_schema,temp_1_5015_0,694459544968825767,SELECT IP_ADDR\\, PORT\\, MEMSQL_DIR FROM information_schema.mv_disk_usage";

        Context context = new ContextImpl(null, null);
        SingleStoredbGuardiumFilter filter = new SingleStoredbGuardiumFilter("test-id", null, context);

        Event e = new org.logstash.Event();
        e.setField("message", logMessage);
        e.setField(Constants.SERVER_IP, "10.0.0.1");

        TestMatchListener matchListener = new TestMatchListener();
        Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);

        Assert.assertEquals(1, results.size());
        Assert.assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));

        Record record = new Gson().fromJson(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME).toString(), Record.class);
        Assert.assertEquals("SELECT IP_ADDR, PORT, MEMSQL_DIR FROM information_schema.mv_disk_usage", record.getData().getOriginalSqlCommand());
    }

    @Test
    public void testUserLoginSuccessEvent() {
        String logMessage = "21622,2024-06-10 10:47:43.235,UTC,singlestore.node1:3306,agg,USER_LOGIN,99995,root,localhost,root@%,password,SUCCESS";

        Context context = new ContextImpl(null, null);
        SingleStoredbGuardiumFilter filter = new SingleStoredbGuardiumFilter("test-id", null, context);

        Event e = new org.logstash.Event();
        e.setField("message", logMessage);
        e.setField(Constants.SERVER_IP, "10.0.0.1");

        TestMatchListener matchListener = new TestMatchListener();
        Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);

        Assert.assertEquals(1, results.size());
        Assert.assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));

        Record record = new Gson().fromJson(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME).toString(), Record.class);
        Assert.assertEquals("SET @event = 'USER_LOGIN'", record.getData().getOriginalSqlCommand());
        Assert.assertEquals("root", record.getAccessor().getDbUser());
    }

    @Test
    public void testUserLoginFailureExceptionRecord() {
        String logMessage = "152310,2024-06-26 13:54:39.421,UTC,singlestore.node1:3306,agg,USER_LOGIN,99997,testuser,192.0.2.1,,authentication_none,FAILURE: Access denied";

        Context context = new ContextImpl(null, null);
        SingleStoredbGuardiumFilter filter = new SingleStoredbGuardiumFilter("test-id", null, context);

        Event e = new org.logstash.Event();
        e.setField("message", logMessage);
        e.setField(Constants.SERVER_IP, "10.0.0.1");

        TestMatchListener matchListener = new TestMatchListener();
        Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);

        Assert.assertEquals(1, results.size());
        Assert.assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));

        Record record = new Gson().fromJson(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME).toString(), Record.class);
        Assert.assertNotNull(record.getException());
        Assert.assertEquals("LOGIN_FAILED", record.getException().getExceptionTypeId());
        Assert.assertEquals("Login Failed (FAILURE: Access denied)", record.getException().getDescription());
    }

    @Test
    public void testComplexQueryWithOptimizerHints() {
        String logMessage = "21866,2024-06-10 11:29:58.831,UTC,singlestore.node1:3306,agg,1,99993,root,information_schema,temp_1_5077_3,4952243803313514788,/*!90621 OBJECT()*/ SELECT `TABLE_NAME` FROM `_MV_QUERY_PROSPECTIVE_HISTOGRAMS`";

        Context context = new ContextImpl(null, null);
        SingleStoredbGuardiumFilter filter = new SingleStoredbGuardiumFilter("test-id", null, context);

        Event e = new org.logstash.Event();
        e.setField("message", logMessage);
        e.setField(Constants.SERVER_IP, "10.0.0.1");

        TestMatchListener matchListener = new TestMatchListener();
        Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);

        Assert.assertEquals(1, results.size());
        Assert.assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));

        Record record = new Gson().fromJson(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME).toString(), Record.class);
        Assert.assertEquals("SELECT TABLE_NAME FROM _MV_QUERY_PROSPECTIVE_HISTOGRAMS", record.getData().getOriginalSqlCommand());
    }

}

class TestMatchListener implements FilterMatchListener {

    private final AtomicInteger matchCount = new AtomicInteger(0);

    @Override
    public void filterMatched(Event event) {
        matchCount.incrementAndGet();
    }
}
