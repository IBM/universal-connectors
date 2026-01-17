//
// Copyright 2023 IBM All Rights Reserved.
// SPDX-License-Identifier: Apache2.0
//
package com.ibm.guardium.progress;

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
import org.junit.Test;
import java.text.ParseException;
import static com.ibm.guardium.progress.Constants.minOffset;
import static org.junit.Assert.assertEquals;

public class ParserTest {

    Parser parser = new Parser();


    // parseRecord verified
    @Test
    public void testParseRecord() throws ParseException {


        JsonObject data = new JsonObject();
        data.addProperty(Constants.CLIENT_IP, "0.0.0.0");
        data.addProperty(Constants.TIMESTAMP, "2023-03-01T23:16:47.956Z");
        data.addProperty(Constants.USER_ID, "ABC");
        data.addProperty(Constants.DATABASE_NAME, "Demo");
        data.addProperty(Constants.CLIENT_SESSION_ID, "t+3ZR8SvMLygFMida3OP4w");
        data.addProperty(Constants.CLIENT_PORT, "-1");
        data.addProperty(Constants.SERVER_PORT, "5555");
        data.addProperty(Constants.CLIENT_HOST, "NA");
        data.addProperty(Constants.SERVER_HOST,"DatabaseM");
        data.addProperty(Constants.SOURCE_PROGRAM, "NA");
        data.addProperty(Constants.EVENT_NAME, "_sys.tbl.create");
        data.addProperty(Constants.EVENT_CONTEXT, "PUB._fileSTUDENTPUB");


        final Record record = Parser.parseRecord(data);
        assertEquals("Demo", record.getDbName());
        assertEquals("t+3ZR8SvMLygFMida3OP4w", record.getSessionId());

    }

    // Accessor values verified.
    @Test
    public void testParseAccessor() throws ParseException {

        JsonObject data = new JsonObject();
        data.addProperty(Constants.CLIENT_IP, "0.0.0.0");
        data.addProperty(Constants.TIMESTAMP, "2023-02-01T23:11:17.956Z");
        data.addProperty(Constants.USER_ID, "Apurva");
        data.addProperty(Constants.DATABASE_NAME, "Demo");
        data.addProperty(Constants.CLIENT_SESSION_ID, "+t3ZR8SvMLygFMida3OP4w");
        data.addProperty(Constants.CLIENT_PORT, -1);
        data.addProperty(Constants.SERVER_PORT, -1);
        data.addProperty(Constants.SERVER_HOST,"DatabaseM");
        data.addProperty(Constants.CLIENT_HOST, "NA");
        data.addProperty(Constants.SOURCE_PROGRAM, "NA");
        data.addProperty(Constants.EVENT_NAME, "_sys.fld.create");
        data.addProperty(Constants.EVENT_CONTEXT, "PUB._Field4273_Db-guid");


        Record record = Parser.parseRecord(data);
        Accessor actual = record.getAccessor();

        assertEquals(Constants.DB_PROTOCOL, actual.getDbProtocol());
        assertEquals(Constants.SERVER_TYPE, actual.getServerType());
        assertEquals("FREE_TEXT", actual.getLanguage());
        assertEquals("CONSTRUCT", actual.getDataType());
        assertEquals("Apurva", actual.getDbUser());
    }

    //Session Locator values verified.
    @Test
    public void testParseSessionLocator() throws ParseException {

        JsonObject data = new JsonObject();
        data.addProperty(Constants.CLIENT_IP, "0.0.0.0");
        data.addProperty(Constants.TIMESTAMP, "2023-02-28T23:16:44.956Z");
        data.addProperty(Constants.USER_ID, "Apurva");
        data.addProperty(Constants.DATABASE_NAME, "Demo");
        data.addProperty(Constants.CLIENT_SESSION_ID, "+t3ZR8SvMLygFMida3OP4w");
        data.addProperty(Constants.SERVER_PORT, 5555);
        data.addProperty(Constants.SERVER_HOST,"DatabaseM");
        data.addProperty(Constants.CLIENT_HOST, "NA");
        data.addProperty(Constants.SOURCE_PROGRAM, "NA");
        data.addProperty(Constants.EVENT_NAME, "_sys.tbl.update");
        data.addProperty(Constants.EVENT_CONTEXT, "PUB._fileSTUDENTPUB");
        data.addProperty(Constants.CLIENT_IP, "0.0.0.0");
        data.addProperty(Constants.SERVER_IP, "0.0.0.0");

        Record record = Parser.parseRecord(data);
        SessionLocator session = record.getSessionLocator();

       assertEquals(-1, session.getClientPort());
        assertEquals(5555, session.getServerPort());
        assertEquals("0.0.0.0", session.getServerIp());
    }


    @Test
    public void testParseTimestamp() throws ParseException {
        JsonObject data = new JsonObject();
        data.addProperty(Constants.CLIENT_IP, "0.0.0.0");
        data.addProperty(Constants.TIMESTAMP, "2023-03-01T23:16:47.956Z");
        data.addProperty(Constants.USER_ID, "xyz");
        data.addProperty(Constants.DATABASE_NAME, "Demo");
        data.addProperty(Constants.CLIENT_SESSION_ID, "+t3ZR8SvMLygFMida3OP4w");
        data.addProperty(Constants.CLIENT_PORT, "-1");
        data.addProperty(Constants.SERVER_PORT, "-1");
       data.addProperty(Constants.SERVER_HOST, "MSSQL DB Machine");
        data.addProperty(Constants.CLIENT_HOST, "NA");
        data.addProperty(Constants.SOURCE_PROGRAM, "NA");
        data.addProperty(Constants.EVENT_NAME, "_sys.index.update");
        data.addProperty(Constants.EVENT_CONTEXT, "PUB._index14189_Default");

        Time date = Parser.parseTimestamp(data);
        Long epoch = date.getTimstamp();
        Long l = 1677712607956L;
        assertEquals(0, minOffset);
        assertEquals(l, epoch);

    }

    // Exception handling
    @Test
    public void testParseRecord_Error() throws ParseException {
        JsonObject data = new JsonObject();
        data.addProperty(Constants.CLIENT_SESSION_ID, "");
        data.addProperty(Constants.CLIENT_IP, "0.0.0.0");
        data.addProperty(Constants.TIMESTAMP, "2023-02-26T23:16:17.956Z");
        data.addProperty(Constants.USER_ID, "");
        data.addProperty(Constants.DATABASE_NAME, "Demo");
        data.addProperty(Constants.CLIENT_PORT, -1);
        data.addProperty(Constants.SERVER_PORT, -1);
        data.addProperty(Constants.SERVER_HOST,"DatabaseM");
        data.addProperty(Constants.CLIENT_HOST, "NA");
        data.addProperty(Constants.SOURCE_PROGRAM, "NA");
        data.addProperty(Constants.EVENT_NAME, "_sql.user.login.fail");
        data.addProperty(Constants.EVENT_CONTEXT, "SQL USER LOGIN");

        ExceptionRecord e1 = Parser.parseException(data);

        assertEquals("LOGIN_FAILED", e1.getExceptionTypeId());
        assertEquals("_sql.user.login.fail", e1.getDescription());

    }

}


