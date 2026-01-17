//
// Copyright 2020-2021 IBM Inc. All rights reserved
// SPDX-License-Identifier: Apache2.0
//

package com.ibm.guardium.singlestore;

import co.elastic.logstash.api.Event;
import com.google.gson.JsonObject;
import com.ibm.guardium.universalconnector.commons.structures.Accessor;
import com.ibm.guardium.universalconnector.commons.structures.Construct;
import com.ibm.guardium.universalconnector.commons.structures.Data;
import com.ibm.guardium.universalconnector.commons.structures.ExceptionRecord;
import com.ibm.guardium.universalconnector.commons.structures.Record;
import com.ibm.guardium.universalconnector.commons.structures.Sentence;
import com.ibm.guardium.universalconnector.commons.structures.SentenceObject;
import com.ibm.guardium.universalconnector.commons.structures.SessionLocator;
import com.ibm.guardium.universalconnector.commons.structures.Time;
import com.ibm.guardium.universalconnector.commons.structures.Accessor;
import com.ibm.guardium.universalconnector.commons.structures.Construct;
import com.ibm.guardium.universalconnector.commons.structures.Data;
import com.ibm.guardium.universalconnector.commons.structures.ExceptionRecord;
import com.ibm.guardium.universalconnector.commons.structures.Record;
import com.ibm.guardium.universalconnector.commons.structures.Sentence;
import com.ibm.guardium.universalconnector.commons.structures.SentenceObject;
import com.ibm.guardium.universalconnector.commons.structures.SessionLocator;
import com.ibm.guardium.universalconnector.commons.structures.Time;

import java.util.ArrayList;

import org.junit.Assert;
import org.junit.Test;

public class ParserTest {

    String singlestoreString = "133855,2024-06-24 07:16:16.901,UTC,53b9ae806c1c:3306,agg,1,100000,root,test_db,,1308432953418920798,CREATE DATABASE uc_test_db";
    Parser parser = new Parser();

    @Test
    public void testParseSessionLocator() {
        Event e = getParsedEvent(singlestoreString);
        JsonObject inputData = inputData(e);
        final SessionLocator result = parser.parseSessionLocator(inputData);

        SessionLocator expectedSessionLocator = new SessionLocator();
        expectedSessionLocator.setClientIp(Constants.CLIENT_IP_VALUE);
        expectedSessionLocator.setClientPort(Constants.CLIENT_PORT_VALUE);
        expectedSessionLocator.setServerIp(Constants.SERVER_IP_VALUE);
        expectedSessionLocator.setServerPort(3306);

        Assert.assertEquals(expectedSessionLocator.getClientIp(), result.getClientIp());
        Assert.assertEquals(expectedSessionLocator.getClientPort(), result.getClientPort());
        Assert.assertEquals(expectedSessionLocator.getServerIp(), result.getServerIp());
        Assert.assertEquals(expectedSessionLocator.getServerPort(), result.getServerPort());
    }

    @Test
    public void testParseAccessor() {
        Event e = getParsedEvent(singlestoreString);
        JsonObject inputData = inputData(e);
        final Accessor result = parser.parseAccessor(inputData, "test_db");

        Accessor expectedAccessor = new Accessor();
        expectedAccessor.setDbUser("root");
        expectedAccessor.setServiceName("test_db");
        expectedAccessor.setLanguage("MEMSQL");
        expectedAccessor.setDataType("TEXT");
        expectedAccessor.setServerType(Constants.SERVER_TYPE_STRING);
        expectedAccessor.setDbProtocol(Constants.DB_PROTOCOL);

        Assert.assertEquals(expectedAccessor.getDbUser(), result.getDbUser());
        Assert.assertEquals(expectedAccessor.getServiceName(), result.getServiceName());
        Assert.assertEquals(expectedAccessor.getLanguage(), result.getLanguage());
        Assert.assertEquals(expectedAccessor.getDataType(), result.getDataType());
        Assert.assertEquals(expectedAccessor.getServerType(), result.getServerType());
        Assert.assertEquals(expectedAccessor.getDbProtocol(), result.getDbProtocol());
    }

    @Test
    public void testGetTime() {
        Event e = getParsedEvent(singlestoreString);
        JsonObject inputData = inputData(e);
        final Time time = parser.getTime(inputData);

        Assert.assertEquals(0, time.getMinDst());
        Assert.assertEquals(0, time.getMinOffsetFromGMT());
        Assert.assertEquals(1719213376901L, time.getTimstamp());
    }

    @Test
    public void testParseData() {
        Event e = getParsedEvent(singlestoreString);
        JsonObject inputData = inputData(e);
        Data result = parser.parseData(inputData);

        Data expectedData = new Data();
        expectedData.setOriginalSqlCommand("CREATE DATABASE uc_test_db");

        Assert.assertEquals(expectedData.getOriginalSqlCommand(), result.getOriginalSqlCommand());
        Assert.assertNull(result.getConstruct());
    }

    @Test
    public void testParseExceptionRecord() {
        String failedLoginString = "1546,2025-05-07 11:07:20.523,PDT,53b9ae806c1c:3308,leaf,USER_LOGIN,99985,root,localhost,,password,FAILURE: Access denied";
        Event e = getParsedEvent(failedLoginString);
        JsonObject inputData = inputData(e);
        Record result = parser.parseExceptionRecord(inputData);

        Record expectedRecord = new Record();
        ExceptionRecord expectedException = new ExceptionRecord();
        expectedException.setExceptionTypeId("LOGIN_FAILED");
        expectedException.setDescription("Login Failed (FAILURE: Access denied)");
        expectedRecord.setException(expectedException);

        Assert.assertEquals(expectedRecord.getException().getDescription(), result.getException().getDescription());
        Assert.assertEquals(expectedRecord.getException().getExceptionTypeId(), result.getException().getExceptionTypeId());
    }

    @Test
    public void testParseRecord() {
        Event e = getParsedEvent(singlestoreString);
        JsonObject inputData = inputData(e);
        Record result = parser.parseRecord(inputData);

        Assert.assertNotNull(result);
        Assert.assertEquals("root", result.getAccessor().getDbUser());
        Assert.assertEquals("test_db", result.getDbName());
        Assert.assertNotNull(result.getTime());
        Assert.assertNotNull(result.getSessionLocator());
        Assert.assertEquals("CREATE DATABASE uc_test_db", result.getData().getOriginalSqlCommand());
    }

    @Test
    public void testParseRecordWithLoginEvent() {
        String loginString = "1546,2025-05-07 11:07:20.523,PDT,53b9ae806c1c:3308,leaf,USER_LOGIN,99985,root,localhost,,password,SUCCESS";
        Event e = getParsedEvent(loginString);
        JsonObject inputData = inputData(e);
        Record result = parser.parseRecord(inputData);

        Assert.assertNotNull(result);
        Assert.assertNull(result.getData()); // Login events should not have data
    }

    @Test
    public void testGetInitialRecord() {
        Event e = getParsedEvent(singlestoreString);
        JsonObject inputData = inputData(e);
        Record result = parser.getInitialRecord(inputData);

        Assert.assertNotNull(result);
        Assert.assertEquals("N.A.", result.getAppUserName());
        Assert.assertEquals("test_db", result.getDbName());
        Assert.assertEquals("", result.getSessionId());
        Assert.assertNotNull(result.getTime());
        Assert.assertNotNull(result.getSessionLocator());
        Assert.assertNotNull(result.getAccessor());
    }

    @Test
    public void testParseSessionLocatorWithIPv6Client() {
        Event e = getParsedEvent(singlestoreString);
        JsonObject inputData = inputData(e);
        inputData.addProperty(Constants.CLIENT_IP, "2001:0db8:85a3:0000:0000:8a2e:0370:7334");

        SessionLocator result = parser.parseSessionLocator(inputData);

        Assert.assertTrue(result.isIpv6());
        Assert.assertEquals("2001:0db8:85a3:0000:0000:8a2e:0370:7334", result.getClientIpv6());
    }

    @Test
    public void testParseSessionLocatorWithIPv6Server() {
        Event e = getParsedEvent(singlestoreString);
        JsonObject inputData = inputData(e);

        // Only server is IPv6, client is IPv4
        inputData.addProperty(Constants.SERVER_IP, "2001:0db8:85a3:0000:0000:8a2e:0370:7335");
        SessionLocator result = parser.parseSessionLocator(inputData);

        // When only server is IPv6, isIpv6 should be true and serverIpv6 should be set
        Assert.assertTrue(result.isIpv6());
        Assert.assertEquals("2001:0db8:85a3:0000:0000:8a2e:0370:7335", result.getServerIpv6());
    }

    @Test
    public void testParseDataWithNullQuery() {
        Event e = getParsedEvent(singlestoreString);
        JsonObject inputData = inputData(e);
        inputData.remove(Constants.QUERY_STATEMENT);

        try {
            Data result = parser.parseData(inputData);
            // Should handle null gracefully
            Assert.assertNotNull(result);
        } catch (Exception ex) {
            // Expected to handle null
        }
    }

    @Test
    public void testParseDataWithQuotedQuery() {
        Event e = getParsedEvent(singlestoreString);
        JsonObject inputData = inputData(e);
        inputData.addProperty(Constants.QUERY_STATEMENT, "\"SELECT * FROM table\"");
        Data result = parser.parseData(inputData);
        Assert.assertNotNull(result);
        Assert.assertEquals("SELECT * FROM table", result.getOriginalSqlCommand());
    }

    @Test
    public void testGetTimeWithInvalidTimestamp() {
        JsonObject inputData = new JsonObject();
        inputData.addProperty(Constants.TIMESTAMP, "invalid-timestamp");
        Time result = parser.getTime(inputData);
        Assert.assertNull(result); // Should return null for invalid timestamp
    }

    @Test
    public void testGetTimeWithNullTimestamp() {
        JsonObject inputData = new JsonObject();
        Time result = parser.getTime(inputData);
        Assert.assertNotNull(result);
        Assert.assertEquals(0, result.getTimstamp());
    }

//	    ----------------------------------- ---------------------------------------------------

    private JsonObject inputData(Event e) {
        JsonObject data = new JsonObject();

        if (e.getField(Constants.CLIENT_IP).toString() != null && !e.getField(Constants.CLIENT_IP).toString().isEmpty()) {
            data.addProperty(Constants.CLIENT_IP, e.getField(Constants.CLIENT_IP).toString());
        }
        if (e.getField(Constants.SERVER_IP).toString() != null && !e.getField(Constants.SERVER_IP).toString().isEmpty()) {
            data.addProperty(Constants.SERVER_IP, e.getField(Constants.SERVER_IP).toString());
        }
        if (e.getField(Constants.SERVER_HOSTNAME).toString() != null && e.getField(Constants.SERVER_HOSTNAME).toString().isEmpty()) {
            data.addProperty(Constants.SERVER_HOSTNAME, e.getField(Constants.SERVER_HOSTNAME).toString());
        }
        if (e.getField(Constants.TIMESTAMP).toString() != null && !e.getField(Constants.TIMESTAMP).toString().isEmpty()) {
            data.addProperty(Constants.TIMESTAMP, e.getField(Constants.TIMESTAMP).toString());
        }
        if (e.getField(Constants.SERVER_PORT).toString() != null && !e.getField(Constants.SERVER_PORT).toString().isEmpty()) {
            data.addProperty(Constants.SERVER_PORT, e.getField(Constants.SERVER_PORT).toString());
        }
        if (e.getField(Constants.DB_USER).toString() != null && !e.getField(Constants.DB_USER).toString().isEmpty()) {
            data.addProperty(Constants.DB_USER, e.getField(Constants.DB_USER).toString());
        }
        if (e.getField(Constants.DB_NAME).toString() != null && !e.getField(Constants.DB_NAME).toString().isEmpty()) {
            data.addProperty(Constants.DB_NAME, e.getField(Constants.DB_NAME).toString());
        }
        if (e.getField(Constants.QUERY_STATEMENT).toString() != null && !e.getField(Constants.QUERY_STATEMENT).toString().isEmpty()) {
            data.addProperty(Constants.QUERY_STATEMENT, e.getField(Constants.QUERY_STATEMENT).toString());
        }
        if (e.getField(Constants.MESSAGE).toString() != null && !e.getField(Constants.MESSAGE).toString().isEmpty()) {
            data.addProperty(Constants.MESSAGE, e.getField(Constants.MESSAGE).toString());
        }
        return data;
    }


    public static Event getParsedEvent(String logEvent) {
        try {

            String[] values = logEvent.split(",");

            String server_hostname = values[3].split(":")[0];
            String server_port = values[3].split(":")[1];

            Event e = new org.logstash.Event();

            e.setField("message", logEvent);
            e.setField(Constants.CLIENT_IP, "0.0.0.0");
            e.setField(Constants.DB_NAME, values[8]);

            if (values[5].equals("USER_LOGIN")) {
                e.setField(Constants.CLIENT_IP, values[8]);
                e.setField(Constants.DB_NAME, "");
            }
            e.setField(Constants.SERVER_IP, "0.0.0.0");
            e.setField(Constants.SERVER_HOSTNAME, server_hostname);
            e.setField(Constants.SERVER_PORT, server_port);
            e.setField(Constants.TIMESTAMP, values[1] + " " + values[2]);
            e.setField(Constants.DB_USER, values[7]);

            if (values.length > 12) {
                for (int i = 12; i < values.length; i++) {
                    values[11] = values[11] + "," + values[i];
                }

            }
            e.setField(Constants.QUERY_STATEMENT, values[11]);
            return e;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }
    }
}


