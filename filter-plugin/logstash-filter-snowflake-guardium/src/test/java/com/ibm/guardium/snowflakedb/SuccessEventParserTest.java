//
// Copyright 2021-2023 IBM Inc. All rights reserved
// SPDX-License-Identifier: Apache2.0
//

package com.ibm.guardium.snowflakedb;

import com.google.gson.Gson;
import com.ibm.guardium.snowflakedb.exceptions.ParseException;
import com.ibm.guardium.snowflakedb.parser.Parser;
import com.ibm.guardium.snowflakedb.parser.SuccessEventParser;
import com.ibm.guardium.snowflakedb.utils.Constants;
import com.ibm.guardium.universalconnector.commons.structures.Record;
import org.junit.Assert;
import org.junit.Test;
import org.logstash.Event;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;

public class SuccessEventParserTest {

    @Test
    public void testSuccessEvent(){
        Event e = FakeEventFactory.getSuccessEvent();
        Parser parser = new SuccessEventParser();
        Map<String,Object> event = e.toMap();

        try{
            Record record = parser.parseRecord(event);

            Assert.assertEquals(record.getSessionId(), event.get(Constants.SESSION_ID).toString());
            Assert.assertEquals(record.getDbName(), event.get(Constants.DATABASE_NAME).toString());
            Assert.assertEquals(record.getAppUserName(), Constants.NOT_AVAILABLE);
            Assert.assertEquals(record.getTime().getMinDst(), 0);
            Assert.assertEquals(record.getTime().getMinOffsetFromGMT(), 0);

            String qts = event.get(Constants.QUERY_TIMESTAMP).toString();
            LocalDateTime date = LocalDateTime.parse(qts,Parser.DATE_TIME_FORMATTER);
            long ts = date.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

            Assert.assertEquals(record.getTime().getTimstamp(), ts);

            Assert.assertEquals(record.getAccessor().getDbUser(), event.get(Constants.USER_NAME).toString());
            Assert.assertEquals(record.getAccessor().getServerType(), Constants.SERVER_TYPE);
            Assert.assertEquals(record.getAccessor().getServerOs(), Constants.NOT_AVAILABLE);

            String env = event.get(Constants.CLIENT_ENVIRONMENT).toString();
            Gson gson = new Gson();
            Map<String,String> clientEnv = gson.fromJson(env, Map.class);
            String clientOs = clientEnv.get(Constants.CLIENT_OS) + ", "
                    + clientEnv.get(Constants.CLIENT_OS_VERSION);
            Assert.assertEquals(record.getAccessor().getClientOs(), clientOs);

            Assert.assertEquals(record.getAccessor().getClientHostName(), Constants.UNKNOWN_STRING);
            Assert.assertEquals(record.getAccessor().getServerHostName(),
                    event.get(Constants.SERVER_HOST_NAME).toString());
            Assert.assertEquals(record.getAccessor().getCommProtocol(), Constants.UNKNOWN_STRING);
            Assert.assertEquals(record.getAccessor().getDbProtocol(), Constants.DB_PROTOCOL);
            Assert.assertEquals(record.getAccessor().getDbProtocolVersion(), Constants.UNKNOWN_STRING);
            Assert.assertEquals(record.getAccessor().getOsUser(),
                    clientEnv.get(Constants.CLIENT_OS_USER));
            Assert.assertEquals(record.getAccessor().getSourceProgram(),
                    event.get(Constants.CLIENT_APPLICATION_ID).toString());
            Assert.assertEquals(record.getAccessor().getClient_mac(), Constants.UNKNOWN_STRING);
            Assert.assertEquals(record.getAccessor().getServerDescription(), Constants.UNKNOWN_STRING);
            Assert.assertEquals(record.getAccessor().getServiceName(), event.get(Constants.DATABASE_NAME).toString());
            Assert.assertEquals(record.getAccessor().getLanguage(), Constants.LANGUAGE_SNOWFLAKE);
            Assert.assertEquals(record.getAccessor().getDataType(),Constants.TEXT);


            Assert.assertEquals(record.getSessionLocator().getClientIp(),event.get(Constants.CLIENT_IP).toString());
            Assert.assertEquals(record.getSessionLocator().getServerIp(),event.get(Constants.SERVER_IP).toString());
            Assert.assertEquals(Long.valueOf(record.getSessionLocator().getServerPort()),
                    Long.valueOf(Constants.SERVER_PORT));

            Assert.assertEquals(record.getData().getOriginalSqlCommand(),event.get(Constants.QUERY_TEXT).toString());
            Assert.assertEquals(record.getDbName(), record.getAccessor().getServiceName());
        } catch (ParseException ex){
            Assert.fail(ex.getMessage());
            ex.printStackTrace();
        }
    }

    @Test
    public void testGetTime(){
        Event e = FakeEventFactory.getSuccessEvent();
        SuccessEventParser parser = new SuccessEventParser();
        Map<String,Object> event = e.toMap();

        try{
            Record record = parser.parseRecord(event);
            System.out.println(record.getTime().getTimstamp());
        }catch (ParseException ex){
            Assert.fail(ex.getMessage());
            ex.printStackTrace();
        }

    }

    @Test
    public void  testAccessorWithoutClientOSUser(){
        Event e = FakeEventFactory.getSuccessEvent();
        e.setField(Constants.CLIENT_ENVIRONMENT,FakeEventFactory.getClientEnvWithoutOsUser());
        SuccessEventParser parser = new SuccessEventParser();
        Map<String,Object> event = e.toMap();

        try{
            Record record = parser.parseRecord(event);
            Assert.assertEquals(Constants.NOT_AVAILABLE,record.getAccessor().getOsUser());
        }catch (ParseException ex){
            Assert.fail(ex.getMessage());
            ex.printStackTrace();
        }
    }
}
