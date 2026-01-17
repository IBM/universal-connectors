//
// Copyright 2020- IBM Inc. All rights reserved
// SPDX-License-Identifier: Apache2.0
//
package com.ibm.guardium.auroramysql;

import java.text.ParseException;
import com.google.gson.JsonObject;
import com.ibm.guardium.auroramysql.Constants;
import com.ibm.guardium.auroramysql.Parser;
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
		
		data.addProperty(Constants.CLIENT_IP, "2600:1f16:6a0:1801:f292:6f15:a643:6128");
		data.addProperty(Constants.TIMESTAMP, "1638806583399975");
		data.addProperty(Constants.APP_USER, "Laxmikant");
		data.addProperty(Constants.SESSION_ID, "1234");
		data.addProperty(Constants.ACTION_STATUS, "0");
		data.addProperty(Constants.EXEC_STATEMENT,
				"'CREATE TABLE Orders (OrderID int NOT NULL,OrderNumber int NOT NULL,PersonID int,PRIMARY KEY (OrderID))'");
		data.addProperty(Constants.AUDIT_ACTION, "Create Table");
		data.addProperty(Constants.DB_USER, "admin");
		data.addProperty(Constants.DB_NAME, "music");
		data.addProperty(Constants.SERVERHOSTNAME, "serverHostName");
		final Record record = Parser.parseRecord(data);

		Assert.assertEquals(
				"CREATE TABLE Orders (OrderID int NOT NULL,OrderNumber int NOT NULL,PersonID int,PRIMARY KEY (OrderID))",
				record.getData().getOriginalSqlCommand());
		Assert.assertEquals(record.getDbName(), record.getAccessor().getServiceName());
	}

	// Exception handling
	@Test
	public void testParseRecord_Error () throws ParseException {
		JsonObject data = new JsonObject();
		data.addProperty(Constants.CLIENT_IP, "0.0.0.0");
		data.addProperty(Constants.TIMESTAMP, "1638806583399975");
		data.addProperty(Constants.APP_USER, "Laxmikant");
		data.addProperty(Constants.SESSION_ID, "1234");
		data.addProperty(Constants.ACTION_STATUS, "1100");
		data.addProperty(Constants.AUDIT_ACTION, "CONNECT");
		data.addProperty(Constants.DB_USER, "JOHNNY");
		data.addProperty(Constants.DB_NAME, "music");
		data.addProperty(Constants.SERVERHOSTNAME, "serverHostName");
		final Record record = Parser.parseRecord(data);
		Assert.assertEquals("LOGIN_FAILED", record.getException().getExceptionTypeId());
		Assert.assertEquals("CONNECT", record.getException().getDescription());
	}


	// Accessor values verified.
	@Test
	public void testParseAccessor() throws ParseException {

		JsonObject data = new JsonObject();
		data.addProperty(Constants.CLIENT_IP, "localhost");
		data.addProperty(Constants.TIMESTAMP, "1638806583399975");
		data.addProperty(Constants.APP_USER, "Laxmikant");
		data.addProperty(Constants.SESSION_ID, "1234");
		data.addProperty(Constants.ACTION_STATUS, "0");
		data.addProperty(Constants.EXEC_STATEMENT,
				"CREATE TABLE Orders (OrderID int NOT NULL,OrderNumber int NOT NULL,PersonID int,PRIMARY KEY (OrderID))");
		data.addProperty(Constants.AUDIT_ACTION, "Create Table");
		data.addProperty(Constants.DB_USER, "JOHNNY");
		data.addProperty(Constants.DB_NAME, "MUSIC");
		data.addProperty(Constants.SERVERHOSTNAME, "serverHostName");
		Record record = Parser.parseRecord(data);
		Accessor actual = record.getAccessor();

		Assert.assertEquals(Constants.DB_PROTOCOL, actual.getDbProtocol());
		Assert.assertEquals(Constants.SERVER_TYPE, actual.getServerType());

		Assert.assertEquals("JOHNNY", actual.getDbUser());

	}

	@Test
	public void testParseTimestamp() {
		JsonObject data = new JsonObject();
		data.addProperty(Constants.TIMESTAMP, "1638806583399975");
		long date = Parser.parseTimestamp(data);
		Assert.assertEquals(1638806583399975L, date);
	}

	// Timestamp parsing
	@Test
	public void testGetTime() throws ParseException {
		JsonObject data = new JsonObject();
		data.addProperty(Constants.TIMESTAMP, "1638806583399975");
		long dateString = Parser.parseTimestamp(data);
		long time = Parser.getTime(dateString).getTimstamp();
		Assert.assertEquals(time, 1638806583399L);
	}

}