//
// Copyright 2020-2021 IBM Inc. All rights reserved
// SPDX-License-Identifier: Apache2.0
//
package com.ibm.guardium.generic;

import java.text.ParseException;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ibm.guardium.generic.Constants;
import com.ibm.guardium.generic.Parser;
import com.ibm.guardium.universalconnector.commons.structures.Record;

import org.junit.Assert;
import org.junit.Test;

public class ParserTest {

	Parser parser = new Parser();
	final String rdsString = "2020-10-22 11:53:26 UTC:183.87.237.49(63661):postgres@testDB:[17796]:LOG:  00000: AUDIT: SESSION,27,1,WRITE,DELETE,,,DELETE FROM employee WHERE emp_id  = 101,<none>\\r\\n2020-10-22 11:53:26 UTC:183.87.237.49(63661):postgres@testDB:[17796]:LOCATION:  log_audit_event, pgaudit.c:760";


	@Test public void testParseRecord() throws ParseException {

		JsonObject data = new JsonObject();
		data.addProperty(Constants.APPUSERNAME, "NA");
		data.addProperty(Constants.DBNAME, "testDB");
		data.addProperty(Constants.SESSIONID, "17796");

		Record record = Parser.parseRecord(data);

		Assert.assertEquals("17796", record.getSessionId());
		Assert.assertEquals("testDB", record.getDbName());
		Assert.assertEquals(Constants.NOT_AVAILABLE, record.getAppUserName());
	}

	@Test public void testParseTime() throws ParseException {

		JsonObject data = new JsonObject();
		data.addProperty(Constants.APPUSERNAME, "NA");
		data.addProperty(Constants.DBNAME, "testDB");
		data.addProperty(Constants.SESSIONID, "17796");
		data.addProperty(Constants.TIMESTAMP, "2022-08-22 13:56:59 GMT");

		Time time = Parser.parseTimestamp(data);

		Assert.assertEquals(1661176619000L, time.getTimstamp());
		Assert.assertEquals(0, time.getMinDst());
		Assert.assertEquals(0, time.getMinOffsetFromGMT());
	}


	@Test public void testParseSessionLocator() throws ParseException {

		JsonObject data = new JsonObject();

		data.addProperty(Constants.CLIENTIP, "183.87.237.49");
		data.addProperty(Constants.CLIENTPORT, 63661);
		data.addProperty(Constants.SERVERIP, "0.0.0.0");
		data.addProperty(Constants.SERVERPORT, -1);

		SessionLocator actual = Parser.parseSessionLocator(data);

		Assert.assertEquals(Constants.UNKNOWNSERVERIP, actual.getServerIp());
		Assert.assertEquals(-1,actual.getServerPort());
		Assert.assertEquals("183.87.237.49", actual.getClientIp());
		Assert.assertEquals(63661, actual.getClientPort());

	}


	@Test
	public void testParseAccessor() throws ParseException {
		JsonObject data = new JsonObject();
		data.addProperty(Constants.CLIENTIP, "183.87.237.49");
		data.addProperty(Constants.TIMESTAMP, "2022-08-22 13:56:59 GMT");
		data.addProperty(Constants.APPUSERNAME, "NA");
		data.addProperty(Constants.SESSIONID, "1234");
		data.addProperty(Constants.ORIGINALSQLCOMMAND,
				"CREATE TABLE Orders (OrderID int NOT NULL,OrderNumber int NOT NULL,PersonID int,PRIMARY KEY (OrderID))");
		data.addProperty(Constants.CLIENTPORT, "63661");
		data.addProperty(Constants.DBUSER, "postgres");
		data.addProperty(Constants.DBPROTOCOL, "Postgre AWS Native Audit");
		data.addProperty(Constants.SERVERTYPE, "Postgre");

		Record record = Parser.parseRecord(data);
		Accessor actual = record.getAccessor();

		Assert.assertEquals("Postgre AWS Native Audit", actual.getDbProtocol());
		Assert.assertEquals("Postgre", actual.getServerType());

		Assert.assertEquals("postgres", actual.getDbUser());

	}

}