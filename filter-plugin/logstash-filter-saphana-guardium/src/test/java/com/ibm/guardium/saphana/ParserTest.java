//
// Copyright 2020- IBM Inc. All rights reserved
// SPDX-License-Identifier: Apache2.0
//
package com.ibm.guardium.saphana;

import java.text.ParseException;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ibm.guardium.saphana.Constants;
import com.ibm.guardium.saphana.Parser;
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

import org.junit.Assert;
import org.junit.Test;

public class ParserTest {

	Parser parser = new Parser();

	// CreateTable
	@Test
	public void testParseRecord_CreateTable() throws ParseException {

		JsonObject data = new JsonObject();
		data.addProperty(Constants.CLIENT_IP, "0.0.0.0");
		data.addProperty(Constants.TIMESTAMP, "2020-12-28 07:35:21");
		data.addProperty(Constants.APP_USER, "Laxmikant");
		data.addProperty(Constants.SESSION_ID, "1234");
		data.addProperty(Constants.ACTION_STATUS, "SUCCESSFUL");
		data.addProperty(Constants.EXEC_STATEMENT,
				"CREATE TABLE Orders (OrderID int NOT NULL,OrderNumber int NOT NULL,PersonID int,PRIMARY KEY (OrderID))");
		data.addProperty(Constants.CLIENT_PORT, "5400");
		data.addProperty(Constants.AUDIT_ACTION, "Create Table");
		data.addProperty(Constants.SERVICE_NAME, "indexserver");
		data.addProperty(Constants.SERVER_HOST, "docker");
		data.addProperty(Constants.CLIENT_HOST, "desktop4141");
		data.addProperty(Constants.SOURCE_PROGRAM, "HDBsql");
		data.addProperty(Constants.SERVER_PORT, "3900");
		data.addProperty(Constants.DB_USER, "JOHNNY");
		data.addProperty(Constants.SCHEMA_NAME, "SYSTEM");
		data.addProperty(Constants.MIN_OFF,"+04:00");

		final Record record = Parser.parseRecord(data);

		Assert.assertEquals(record.getDbName(), record.getAccessor().getServiceName());

		Assert.assertEquals(
				"CREATE TABLE Orders (OrderID int NOT NULL,OrderNumber int NOT NULL,PersonID int,PRIMARY KEY (OrderID))",
				record.getData().getOriginalSqlCommand());

	}

	// Exception handling
	@Test
	public void testParseRecord_Error() throws ParseException {
		JsonObject data = new JsonObject();
		data.addProperty(Constants.CLIENT_IP, "0.0.0.0");
		data.addProperty(Constants.TIMESTAMP, "2020-12-28 07:35:21");
		data.addProperty(Constants.APP_USER, "Laxmikant");
		data.addProperty(Constants.SESSION_ID, "1234");
		data.addProperty(Constants.ACTION_STATUS, "UNSUCCESSFUL");
		data.addProperty(Constants.CLIENT_PORT, "5400");
		data.addProperty(Constants.AUDIT_ACTION, "CONNECT");
		data.addProperty(Constants.SERVICE_NAME, "indexserver");
		data.addProperty(Constants.SERVER_HOST, "docker");
		data.addProperty(Constants.CLIENT_HOST, "desktop4141");
		data.addProperty(Constants.SOURCE_PROGRAM, "HDBsql");
		data.addProperty(Constants.SERVER_PORT, "3900");
		data.addProperty(Constants.DB_USER, "PQR");
		data.addProperty(Constants.SCHEMA_NAME, "SYSTEM");
		data.addProperty(Constants.MIN_OFF,"+06:00");

		final Record record = Parser.parseRecord(data);

		Assert.assertEquals("LOGIN_FAILED", record.getException().getExceptionTypeId());
		Assert.assertEquals("authentication failed", record.getException().getDescription());
	}

/*
	Date is not Parsed, only time is parsed.
	@Test
	public void testTimeParing() throws ParseException {
		String dateStr = "2020-12-28 07:35:21";
		Time time = Parser.getTime(dateStr);
		Assert.assertTrue("Failed to parse date, time is " + time.getTimstamp(), 1609166121000L == time.getTimstamp());

	}
*/

	// Accessor values verified.
	@Test
	public void testParseAccessor() throws ParseException {

		JsonObject data = new JsonObject();
		data.addProperty(Constants.CLIENT_IP, "0.0.0.0");
		data.addProperty(Constants.TIMESTAMP, "2020-12-28 07:35:21");
		data.addProperty(Constants.APP_USER, "Laxmikant");
		data.addProperty(Constants.SESSION_ID, "1234");
		data.addProperty(Constants.ACTION_STATUS, "SUCCESSFUL");
		data.addProperty(Constants.EXEC_STATEMENT,
				"CREATE TABLE Orders (OrderID int NOT NULL,OrderNumber int NOT NULL,PersonID int,PRIMARY KEY (OrderID))");
		data.addProperty(Constants.CLIENT_PORT, "5400");
		data.addProperty(Constants.AUDIT_ACTION, "Create Table");
		data.addProperty(Constants.SERVICE_NAME, "indexserver");
		data.addProperty(Constants.SERVER_HOST, "docker");
		data.addProperty(Constants.CLIENT_HOST, "desktop4141");
		data.addProperty(Constants.SOURCE_PROGRAM, "HDBsql");
		data.addProperty(Constants.SERVER_PORT, "3900");
		data.addProperty(Constants.DB_USER, "RFV");
		data.addProperty(Constants.SCHEMA_NAME, "SYSTEM");
		data.addProperty(Constants.MIN_OFF,"+09:00");

		Record record = Parser.parseRecord(data);
		Accessor actual = record.getAccessor();

		Assert.assertEquals(Constants.DB_PROTOCOL, actual.getDbProtocol());
		Assert.assertEquals(Constants.SERVER_TYPE, actual.getServerType());

		Assert.assertEquals("RFV", actual.getDbUser());

	}

	@Test
	public void testParseTimestamp() {
		JsonObject data = new JsonObject();
		data.addProperty(Constants.CLIENT_IP, "0.0.0.0");
		data.addProperty(Constants.TIMESTAMP, "2021-12-07 05:53:02");
		data.addProperty(Constants.APP_USER, "Laxmikant");
		data.addProperty(Constants.SESSION_ID, "1234");
		data.addProperty(Constants.ACTION_STATUS, "SUCCESSFUL");
		data.addProperty(Constants.EXEC_STATEMENT,
				"CREATE TABLE Orders (OrderID int NOT NULL,OrderNumber int NOT NULL,PersonID int,PRIMARY KEY (OrderID))");
		data.addProperty(Constants.CLIENT_PORT, "5400");
		data.addProperty(Constants.AUDIT_ACTION, "Create Table");
		data.addProperty(Constants.SERVICE_NAME, "indexserver");
		data.addProperty(Constants.SERVER_HOST, "docker");
		data.addProperty(Constants.CLIENT_HOST, "desktop4141");
		data.addProperty(Constants.SOURCE_PROGRAM, "HDBsql");
		data.addProperty(Constants.SERVER_PORT, "3900");
		data.addProperty(Constants.DB_USER, "IJB");
		data.addProperty(Constants.SCHEMA_NAME, "SYSTEM");
		data.addProperty(Constants.MIN_OFF,"-02:00");

		Time date = Parser.parseTimestamp(data);
		Assert.assertEquals(1638863582000L, date.getTimstamp());
	}

	@Test
	public void testGetTime() throws ParseException {
		JsonObject data = new JsonObject();
		data.addProperty(Constants.CLIENT_IP, "0.0.0.0");
		data.addProperty(Constants.TIMESTAMP, "2011-12-03 10:15:30");
		data.addProperty(Constants.APP_USER, "Laxmikant");
		data.addProperty(Constants.SESSION_ID, "1234");
		data.addProperty(Constants.ACTION_STATUS, "SUCCESSFUL");
		data.addProperty(Constants.EXEC_STATEMENT,
				"CREATE TABLE Orders (OrderID int NOT NULL,OrderNumber int NOT NULL,PersonID int,PRIMARY KEY (OrderID))");
		data.addProperty(Constants.CLIENT_PORT, "5400");
		data.addProperty(Constants.AUDIT_ACTION, "Create Table");
		data.addProperty(Constants.SERVICE_NAME, "indexserver");
		data.addProperty(Constants.SERVER_HOST, "docker");
		data.addProperty(Constants.CLIENT_HOST, "desktop4141");
		data.addProperty(Constants.SOURCE_PROGRAM, "HDBsql");
		data.addProperty(Constants.SERVER_PORT, "3900");
		data.addProperty(Constants.DB_USER, "OLK");
		data.addProperty(Constants.SCHEMA_NAME, "SYSTEM");
		data.addProperty(Constants.OFFSET, "-300");
		data.addProperty(Constants.MIN_OFF,"-04:00");


		Time time = Parser.parseTimestamp(data);
		Assert.assertEquals(time.getTimstamp(), 1322939730000L);
	}

	// Timestamp parsing
	@Test
	public void testTimestamp() throws ParseException {
		JsonObject data = new JsonObject();
		data.addProperty(Constants.CLIENT_IP, "0.0.0.0");
		data.addProperty(Constants.TIMESTAMP, "1709544248000");
		data.addProperty(Constants.APP_USER, "Laxmikant");
		data.addProperty(Constants.SESSION_ID, "1234");
		data.addProperty(Constants.ACTION_STATUS, "SUCCESSFUL");
		data.addProperty(Constants.EXEC_STATEMENT,
				"CREATE TABLE Orders (OrderID int NOT NULL,OrderNumber int NOT NULL,PersonID int,PRIMARY KEY (OrderID))");
		data.addProperty(Constants.CLIENT_PORT, "5400");
		data.addProperty(Constants.AUDIT_ACTION, "Create Table");
		data.addProperty(Constants.SERVICE_NAME, "indexserver");
		data.addProperty(Constants.SERVER_HOST, "docker");
		data.addProperty(Constants.CLIENT_HOST, "desktop4141");
		data.addProperty(Constants.SOURCE_PROGRAM, "HDBsql");
		data.addProperty(Constants.SERVER_PORT, "3900");
		data.addProperty(Constants.DB_USER, "OLK");
		data.addProperty(Constants.SCHEMA_NAME, "SYSTEM");

		Time time = Parser.parseTimestamp(data);
		Assert.assertEquals(time.getTimstamp(), 1709544248000L);
	}
}