//
// Copyright 2021-2023 IBM Inc. All rights reserved
// SPDX-License-Identifier: Apache2.0
//

package com.ibm.guardium.snowflakedb;

import com.ibm.guardium.snowflakedb.exceptions.ParseException;
import com.ibm.guardium.snowflakedb.parser.AuthFailedEventParser;
import com.ibm.guardium.snowflakedb.parser.Parser;
import com.ibm.guardium.snowflakedb.utils.Constants;
import com.ibm.guardium.universalconnector.commons.structures.Accessor;
import com.ibm.guardium.universalconnector.commons.structures.Construct;
import com.ibm.guardium.universalconnector.commons.structures.Data;
import com.ibm.guardium.universalconnector.commons.structures.ExceptionRecord;
import com.ibm.guardium.universalconnector.commons.structures.Record;
import com.ibm.guardium.universalconnector.commons.structures.Sentence;
import com.ibm.guardium.universalconnector.commons.structures.SentenceObject;
import com.ibm.guardium.universalconnector.commons.structures.SessionLocator;
import com.ibm.guardium.universalconnector.commons.structures.Time;
import com.ibm.guardium.universalconnector.commons.structures.SessionLocator;
import org.junit.Assert;
import org.junit.Test;
import org.logstash.Event;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;

public class AuthFailedEventParserTest {
    @Test
    public void testAuthError(){
        Event e = FakeEventFactory.getAuthErrorEvent();
        Parser parser = new AuthFailedEventParser();
        Map<String,Object> event = e.toMap();

        try{
            Record record = parser.parseRecord(event);

            Integer hashCode = (e.getField(Constants.CLIENT_IP).toString() + SessionLocator.PORT_DEFAULT
                    + e.getField(Constants.SERVER_IP) + Constants.SERVER_PORT).hashCode();

            Assert.assertEquals(hashCode.toString(), record.getSessionId());
            Assert.assertEquals(record.getAppUserName(), event.get(Constants.USER_NAME).toString());
            Assert.assertEquals(record.getTime().getMinDst(), 0);
            Assert.assertEquals(record.getTime().getMinOffsetFromGMT(), 0);

            String qts = event.get(Constants.LOGIN_TIMESTAMP).toString();
            LocalDateTime date = Parser.parseTime(qts);
            long ts = date.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

            Assert.assertEquals(record.getTime().getTimstamp(), ts);

            Assert.assertEquals(record.getAccessor().getDbUser(), event.get(Constants.USER_NAME).toString());
            Assert.assertEquals(record.getAccessor().getServerType(), Constants.SERVER_TYPE);
            Assert.assertEquals(record.getAccessor().getServerOs(), Constants.NOT_AVAILABLE);

            Assert.assertEquals(record.getAccessor().getClientHostName(), Constants.UNKNOWN_STRING);
            Assert.assertEquals(record.getAccessor().getServerHostName(),
                    event.get(Constants.SERVER_HOST_NAME).toString());
            Assert.assertEquals(record.getAccessor().getCommProtocol(), Constants.UNKNOWN_STRING);
            Assert.assertEquals(record.getAccessor().getDbProtocol(), Constants.DB_PROTOCOL);
            Assert.assertEquals(record.getAccessor().getDbProtocolVersion(), Constants.UNKNOWN_STRING);

            Assert.assertEquals(record.getAccessor().getSourceProgram(),
                    event.get(Constants.CLIENT_APPLICATION_ID).toString());
            Assert.assertEquals(record.getAccessor().getClient_mac(), Constants.UNKNOWN_STRING);
            Assert.assertEquals(record.getAccessor().getServerDescription(), Constants.UNKNOWN_STRING);
            Assert.assertEquals(record.getAccessor().getServiceName(), Constants.NOT_AVAILABLE);
            Assert.assertEquals(record.getAccessor().getLanguage(), Constants.LANGUAGE_SNOWFLAKE);
            Assert.assertEquals(record.getAccessor().getDataType(),Constants.TEXT);


            Assert.assertEquals(record.getSessionLocator().getClientIp(),event.get(Constants.CLIENT_IP).toString());
            Assert.assertEquals(record.getSessionLocator().getServerIp(),event.get(Constants.SERVER_IP).toString());
            Assert.assertEquals(Long.valueOf(record.getSessionLocator().getServerPort()),
                    Long.valueOf(Constants.SERVER_PORT));

            Assert.assertEquals(null,record.getData());
            Assert.assertEquals(event.get(Constants.LOGIN_ERROR_CODE) + ": " +
                    event.get(Constants.LOGIN_ERROR_MESSAGE),
                    record.getException().getDescription());
            Assert.assertEquals(record.getException().getExceptionTypeId(),Constants.LOGIN_FAILED);
            Assert.assertEquals(record.getDbName(), record.getAccessor().getServiceName());
        } catch (ParseException ex){
            Assert.fail(ex.getMessage());
            ex.printStackTrace();
        }
    }
}
