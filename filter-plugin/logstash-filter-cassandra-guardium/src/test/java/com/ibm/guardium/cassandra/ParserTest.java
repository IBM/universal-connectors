//
// Copyright 2020-2021 IBM Inc. All rights reserved
// SPDX-License-Identifier: Apache2.0
//
package com.ibm.guardium.cassandra;

import java.text.ParseException;
import java.util.Map;
import org.apache.commons.collections.map.HashedMap;
import org.junit.Assert;
import org.junit.Test;

import com.ibm.guardium.universalconnector.commons.structures.Record;

public class ParserTest {

	Parser parser = new Parser();

	@SuppressWarnings("unchecked")
	Map<String, String> data = new HashedMap();

	Map<String, String> intializeMap() {

		data.put("ks", "company");
		data.put("timestamp", "1643267542035");
		data.put("source", "/127.0.0.1");
		data.put("port", "39632");
		data.put("host", "localhost/127.0.0.1:7000");
		data.put("user", "anonymous");
		data.put("type", "REQUEST_FAILURE");
		data.put("operation", "Delete firstname from company.employee where empid =101;");
		data.put("category", "DML");
		return data;
	}

	@Test
	public void testParseRecord() throws ParseException {

		final Record record = parser.parseRecord(intializeMap());

		Assert.assertEquals(Constants.TEXT, record.getAccessor().getDataType());
		Assert.assertEquals(null, record.getException());
		Assert.assertNotNull(record.getData());
		Assert.assertEquals(record.getDbName(),record.getAccessor().getServiceName());
		Assert.assertEquals(Constants.UNKNOWN_STRING, record.getSessionId());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testParseRecordError() throws ParseException {
		Map<String, String> intrimData = new HashedMap();

		intrimData = intializeMap();
		intrimData.put("type", "REQUEST_FAILURE");
		intrimData.put("operation",
				"Select * from employee;; No keyspace has been specified. USE a keyspace, or explicitly specify keyspace.tablename");
		intrimData.put("category", "ERROR");

		final Record record = parser.parseRecord(intrimData);

		Assert.assertEquals(Constants.TEXT, record.getAccessor().getDataType());
		Assert.assertEquals("SQL_ERROR", record.getException().getExceptionTypeId());
		Assert.assertNull(record.getData());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testParseRecordAuthSuccess() throws ParseException {

		Map<String, String> intrimData = new HashedMap();

		intrimData = intializeMap();

		intrimData.put("type", "LOGIN_SUCCESS");
		intrimData.put("operation", "LOGIN SUCCESSFUL");
		intrimData.put("category", "AUTH");

		final Record record = parser.parseRecord(intrimData);

		Assert.assertEquals(Constants.TEXT, record.getAccessor().getDataType());
		Assert.assertNull(record.getException());
		Assert.assertNotNull(record.getData());

	}

	@Test
	public void testParseRecordAuthFail() throws ParseException {
		@SuppressWarnings({ "unchecked", "unused" })
		Map<String, String> intrimData = new <String, String>  HashedMap();

		intrimData = intializeMap();

		intrimData.put("type", "UNAUTHORIZED_ATTEMPT");
		intrimData.put("operation",
				"CREATE USER test WITH PASSWORD *******; User test does not have sufficient privileges to perform the requested operation");
		intrimData.put("category", "AUTH");

		final Record record = parser.parseRecord(intrimData);

		Assert.assertEquals(Constants.TEXT, record.getAccessor().getDataType());
		Assert.assertNotNull(record.getException());
		Assert.assertNull(record.getData());

	}

}