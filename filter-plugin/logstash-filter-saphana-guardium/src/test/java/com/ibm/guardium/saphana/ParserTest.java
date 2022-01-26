//
// Copyright 2020- IBM Inc. All rights reserved
// SPDX-License-Identifier: Apache2.0
//
package com.ibm.guardium.***REMOVED***phana;

import java.text.ParseException;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ibm.guardium.***REMOVED***phana.Constants;
import com.ibm.guardium.***REMOVED***phana.Parser;
import com.ibm.guardium.univer***REMOVED***lconnector.commons.structures.*;
import com.ibm.guardium.univer***REMOVED***lconnector.commons.structures.Record;

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

		final Record record = Parser.parseRecord(data);

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
		data.addProperty(Constants.DB_USER, "JOHNNY");
		data.addProperty(Constants.SCHEMA_NAME, "SYSTEM");
		final Record record = Parser.parseRecord(data);

		Assert.assertEquals("CONNECT", record.getException().getExceptionTypeId());
		Assert.assertEquals("authentication failed", record.getException().getDescription());
	}

	// Date is not Parsed, only time is parsed.
	@Test
	public void testTimeParing() throws ParseException {
		String dateStr = "2020-12-28 07:35:21";
		Time time = Parser.getTime(dateStr);
		Assert.assertTrue("Failed to parse date, time is " + time.getTimstamp(), 1609140921000L == time.getTimstamp());

	}

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
		data.addProperty(Constants.DB_USER, "JOHNNY");
		data.addProperty(Constants.SCHEMA_NAME, "SYSTEM");

		Record record = Parser.parseRecord(data);
		Accessor actual = record.getAccessor();

		Assert.assertEquals(Constants.DB_PROTOCOL, actual.getDbProtocol());
		Assert.assertEquals(Constants.SERVER_TYPE, actual.getServerType());

		Assert.assertEquals("JOHNNY", actual.getDbUser());

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
		data.addProperty(Constants.DB_USER, "JOHNNY");
		data.addProperty(Constants.SCHEMA_NAME, "SYSTEM");

		String date = Parser.parseTimestamp(data);
		Assert.assertEquals("2020-12-28 07:35:21", date);
	}

	// Timestamp parsing
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
		data.addProperty(Constants.DB_USER, "JOHNNY");
		data.addProperty(Constants.SCHEMA_NAME, "SYSTEM");
		data.addProperty(Constants.OFFSET, "-300");

		String dateString = Parser.parseTimestamp(data);
		long time = Parser.getTime(dateString).getTimstamp();
		Assert.assertEquals(time, 1322907330000L);
	}

}