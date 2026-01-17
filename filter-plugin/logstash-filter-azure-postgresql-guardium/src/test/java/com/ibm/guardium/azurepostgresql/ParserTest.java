
/*
#Copyright 2020-2021 IBM Inc. All rights reserved
#SPDX-License-Identifier: Apache-2.0
#*/
package com.ibm.guardium.azurepostgresql;

import java.text.ParseException;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.ibm.guardium.azurepostgresql.Constants;
import com.ibm.guardium.azurepostgresql.Parser;
import com.ibm.guardium.universalconnector.commons.structures.Accessor;
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
import com.ibm.guardium.universalconnector.commons.structures.Time;
import co.elastic.logstash.api.Event;


public class ParserTest {

	Parser parser = new Parser();
	
    Event e = new org.logstash.Event();

	Event intitalizeEventObject() {

		e.setField(Constants.SESSION_ID, "7420");
		e.setField(Constants.TIMESTAMP, "2022-03-02 14:06:56");
		e.setField(Constants.CLIENT_IP, "183.87.117.29");
		e.setField(Constants.CLIENT_PORT, "10176");
		e.setField(Constants.USER_NAME, "postgres");
		e.setField(Constants.STATEMENT, "select * from employee;");
		e.setField(Constants.DATABASE_NAME, "postgres");
		e.setField(Constants.SERVER_HOSTNAME,"1234567_mypssqlserver");
		e.setField(Constants.APPLICATION_NAME, "psql");
		e.setField(Constants.SUCCEEDED, "LOG");
		return e;
	}

	@Test
	public void createQuery() throws ParseException {

		Event e = intitalizeEventObject();

		e.setField(Constants.STATEMENT, "create table emp1(id int);");
		final Record record = Parser.parseRecord(e);
		Assert.assertEquals(record.getData().getOriginalSqlCommand(),"create table emp1(id int);");
	}

	@Test
	public void insertQuery() throws ParseException {

		Event e = intitalizeEventObject();
		
		e.setField(Constants.STATEMENT,
				"INSERT INTO Employee (EmployeeNo, FirstName, LastName, DOB, JoinedDate, DepartmentNo )"
						+ "VALUES ( 101, 'sss', 'shinde','1980-01-05', '2005-03-27', 01);");
		final Record record = Parser.parseRecord(e);
		Assert.assertEquals(record.getData().getOriginalSqlCommand(), "INSERT INTO Employee (EmployeeNo, FirstName, LastName, DOB, JoinedDate, DepartmentNo )"
				+ "VALUES ( 101, 'sss', 'shinde','1980-01-05', '2005-03-27', 01);");
	}

	@Test
	public void selectQuery() throws ParseException {

		Event e = intitalizeEventObject();
		
		e.setField(Constants.STATEMENT, "Select * from employee;");
		final Record record = Parser.parseRecord(e);
		Assert.assertEquals(record.getData().getOriginalSqlCommand(),"Select * from employee;" );
	}

	@Test
	public void selectQueryNew() throws ParseException {

		Event e = intitalizeEventObject();

		e.setField(Constants.STATEMENT, "\"SELECT \n   first_name || ' ' || last_name \"\"Full Name\"\"\nFROM \n   AutomationEdge\"");
		final Record record = Parser.parseRecord(e);
		Assert.assertEquals(record.getData().getOriginalSqlCommand(),"SELECT \n" +
				"   first_name || ' ' || last_name \"Full Name\"\n" +
				"FROM \n" +
				"   AutomationEdge" );
	}

	@Test
	public void updateQuery() throws ParseException {

		Event e = intitalizeEventObject();
		
		e.setField(Constants.STATEMENT, "UPDATE Employee SET DepartmentNo = 03 WHERE EmployeeNo = 101;");
		final Record record = Parser.parseRecord(e);
		Assert.assertEquals(record.getData().getOriginalSqlCommand(),"UPDATE Employee SET DepartmentNo = 03 WHERE EmployeeNo = 101;");
	}

	@Test
	public void deleteQuery() throws ParseException {

		Event e = intitalizeEventObject();
		
		e.setField(Constants.STATEMENT, "DELETE FROM Employee WHERE EmployeeNo = 101;");
		 Record record = Parser.parseRecord(e);
		Assert.assertEquals(record.getData().getOriginalSqlCommand(),"DELETE FROM Employee WHERE EmployeeNo = 101;");
	}

	@Test
	public void testParseSessionLocator() throws ParseException {
		Event e = intitalizeEventObject();
		
		SessionLocator sessionLocator = Parser.parseSessionLocator(e);
		Assert.assertEquals("183.87.117.29", sessionLocator.getClientIp());
		Assert.assertEquals(10176, sessionLocator.getClientPort());
		Assert.assertEquals(false, sessionLocator.isIpv6());
	}

	@Test
	public void testParseAccessor() throws ParseException {
		Event e = intitalizeEventObject();
		
		Accessor accessor = Parser.parseAccessor(e);
		Assert.assertEquals(Constants.DATA_PROTOCOL_STRING, accessor.getDbProtocol());
		Assert.assertEquals(Constants.SERVER_TYPE_STRING, accessor.getServerType());
		Assert.assertEquals(Constants.LANGUAGE, accessor.getLanguage());
	}

	@Test
	public void testErrors() throws ParseException {
		Event e = intitalizeEventObject();
	
		
		e.setField(Constants.SUCCEEDED, "ERROR");
		e.setField(Constants.MESSAGE, "relation \"dept\" already exists");
		final Record record = Parser.parseRecord(e);
		Assert.assertEquals(Constants.SQL_ERROR, record.getException().getExceptionTypeId());
		Assert.assertEquals("relation \"dept\" already exists", record.getException().getDescription());
	}

	@Test
	public void testAuth() throws ParseException {
		Event e = intitalizeEventObject();
		
		e.setField(Constants.SUCCEEDED, "FATAL" );
		e.setField(Constants.SQL_STATE,"28P01");
		e.setField(Constants.PREFIX,"28P01");
		e.setField(Constants.MESSAGE, "password authentication failed for user \"postgres\"");
		final Record record = Parser.parseRecord(e);
		Assert.assertEquals(Constants.LOGIN_ERROR, record.getException().getExceptionTypeId());
		Assert.assertEquals("password authentication failed for user \"postgres\"",
				record.getException().getDescription());
	}
	
	
	
	
	 
	
}