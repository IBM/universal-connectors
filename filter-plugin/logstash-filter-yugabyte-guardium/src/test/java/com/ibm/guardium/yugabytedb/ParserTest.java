//
// Copyright 2021-2022 IBM Inc. All rights reserved
// SPDX-License-Identifier: Apache2.0
//

package com.ibm.guardium.yugabytedb;

import co.elastic.logstash.api.Event;
import com.ibm.guardium.universalconnector.commons.structures.Accessor;
import com.ibm.guardium.universalconnector.commons.structures.ExceptionRecord;
import com.ibm.guardium.universalconnector.commons.structures.SessionLocator;
import com.ibm.guardium.universalconnector.commons.structures.Time;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ParserTest {
    Event e;

    @Before
    public void grabSuccessEvent() {
        e = getSuccessEvent();
    }

    @Test
    public void testParseAccessor() {

        System.out.println(e);
        final Accessor accessor = Parser.parseAccessor(e.toMap());
        Assert.assertEquals(e.getField(Constants.USERNAME), accessor.getDbUser().trim());
        Assert.assertEquals(Constants.SERVER_TYPE_PG, accessor.getServerType().trim());
        Assert.assertEquals(e.getField(Constants.SERVER_OS), accessor.getServerOs().trim());
        String expectedServerHostName = Constants.POSTGRES + "_" + e.getField(Constants.SERVER_HOST);
        Assert.assertEquals(expectedServerHostName, accessor.getServerHostName().trim());
        Assert.assertEquals(Constants.DB_PROTOCOL_PG, accessor.getDbProtocol().trim());
        Assert.assertEquals(e.getField(Constants.APPLICATION_NAME), accessor.getSourceProgram().trim());
        Assert.assertEquals(e.getField(Constants.DB_NAME), accessor.getServiceName().trim());
        Assert.assertEquals(Constants.LANGUAGE_PG, accessor.getLanguage().trim());
        Assert.assertEquals(Accessor.DATA_TYPE_GUARDIUM_SHOULD_PARSE_SQL, accessor.getDataType().trim());
    }

    @Test
    public void testParseSessionLocator() {

        final SessionLocator sessionLocator = Parser.parseSessionLocator(e.toMap());

        Assert.assertEquals(e.getField(Constants.CLIENT_IP), sessionLocator.getClientIp().trim());

        int clientPort = Integer.valueOf(e.getField(Constants.CLIENT_PORT).toString());
        Assert.assertEquals(clientPort, sessionLocator.getClientPort());
        Assert.assertEquals(e.getField(Constants.SERVER_IP), sessionLocator.getServerIp().trim());
    }

    @Test
    public void testGetTime() {

        Double epochTimestamp = Double.valueOf(e.getField(Constants.TIMESTAMP).toString());
        long t =(long)(epochTimestamp * 1000);
        Time time = Parser.getTime(t);

        Assert.assertEquals(t, time.getTimstamp());
        Assert.assertEquals(0, time.getMinDst());
        Assert.assertEquals(0, time.getMinOffsetFromGMT());
    }

    public static Event getSuccessEvent() {

        String logEvent = "1669791875.091 172.17.0.1(64400) [488] ysqlsh yugabyte yugabyte 6387005b.1e8 0 LOG:  " +
                "AUDIT: SESSION,1,1,DDL,CREATE DATABASE,,,create database foo;,<none>";

        Event e = new org.logstash.Event();

        e.setField("message", logEvent);

        e.setField(Constants.TIMESTAMP, 1669791875.091);
        e.setField(Constants.CLIENT_IP, "172.17.0.1");
        e.setField(Constants.CLIENT_PORT, 64400);
        e.setField(Constants.PROCESS_ID, 488);
        e.setField(Constants.APPLICATION_NAME, "ysqlsh");
        e.setField(Constants.USERNAME, "yugabyte");
        e.setField(Constants.DB_NAME, "yugabyte");
        e.setField(Constants.SESSION_ID, "6387005b.1e8");
        e.setField(Constants.SERVER_OS, "linux");
        e.setField(Constants.SERVER_HOST, "thismachine");
        e.setField(Constants.SERVER_IP, "0.0.0.0");
        e.setField(Constants.TRANSACTION_ID, 0);
        e.setField(Constants.EVENT_TYPE, "LOG");
        e.setField(Constants.AUDIT_TYPE, "SESSION");
        e.setField(Constants.STATEMENT_ID, 1);
        e.setField(Constants.SUB_STATEMENT_ID, 1);
        e.setField(Constants.STATEMENT_CLASS, "DDL");
        e.setField(Constants.COMMAND, "CREATE");
        e.setField(Constants.QUERY, "create database foo");
        e.setField(Constants.PARAMETERS, "<none>");
        e.setField(Constants.TYPE, "sql");
        return e;

    }

    @Test
    public void testException(){
        e = getErrorEvent();

        final ExceptionRecord exceptionRecord = Parser.parseException(e.toMap());

        Assert.assertEquals(Constants.SQL_ERROR, exceptionRecord.getExceptionTypeId());
        Assert.assertEquals(e.getField(Constants.QUERY), exceptionRecord.getSqlString());
        Assert.assertEquals(e.getField(Constants.ERROR_DESCRIPTION), exceptionRecord.getDescription());
    }

    public static Event getErrorEvent() {
        String logEvent = "1670253284.711 172.17.0.1(57860) [357] ysqlsh yugabyte yugabyte 638e0acd.165 0 ERROR:  " +
                "relation \"account\" already exists 1670253284.711 172.17.0.1(57860) [357] ysqlsh yugabyte yugabyte " +
                "638e0acd.165 0 STATEMENT:  create table account (id int, name text, password text, description text);";

        Event e = new org.logstash.Event();

        e.setField("message", logEvent);

        e.setField(Constants.TIMESTAMP, 1669791875.091);
        e.setField(Constants.CLIENT_IP, "172.17.0.1");
        e.setField(Constants.CLIENT_PORT, 57860);
        e.setField(Constants.PROCESS_ID, 357);
        e.setField(Constants.APPLICATION_NAME, "ysqlsh");
        e.setField(Constants.USERNAME, "yugabyte");
        e.setField(Constants.DB_NAME, "yugabyte");
        e.setField(Constants.SESSION_ID, "638e0acd.165");
        e.setField(Constants.TRANSACTION_ID, 0);
        e.setField(Constants.EVENT_TYPE, "ERROR");
        e.setField(Constants.SERVER_OS, "linux");
        e.setField(Constants.SERVER_HOST, "postgres_thismachine");
        e.setField(Constants.SERVER_IP, "0.0.0.0");
        e.setField(Constants.ERROR_DESCRIPTION, "relation \"account\" already exists ");
        e.setField(Constants.QUERY, "create table account (id int, name text, password text, description text)");
        e.setField(Constants.TYPE, "sql");
        return e;

    }
}
