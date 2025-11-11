//
// Copyright 2020- IBM Inc. All rights reserved
// SPDX-License-Identifier: Apache2.0
//
package com.ibm.guardium.teradatadb;

import java.text.ParseException;

import org.junit.Assert;
import org.junit.Test;

import com.ibm.guardium.universalconnector.commons.structures.Accessor;
import com.ibm.guardium.universalconnector.commons.structures.Record;
import com.ibm.guardium.universalconnector.commons.structures.SessionLocator;
import com.ibm.guardium.universalconnector.commons.structures.Time;

import co.elastic.logstash.api.Event;

public class ParserTest {

    Parser parser = new Parser();
    Event e = new org.logstash.Event();
    
    Event intitalizeEventObject() {	
    	
	    e.setField(Constants.SESSION_ID, "6968");
		e.setField(Constants.TIME_FIELD, "2021-11-16T07:49:41.220Z");
		e.setField(Constants.CLIENT_IP, "9.211.127.160");
        e.setField(Constants.SERVER_IP, "9.211.127.161");
		e.setField(Constants.USER_NAME, "SYSDBA");
		e.setField(Constants.SERVER_HOSTNAME, "1.1.1.1");
		e.setField(Constants.SQL_TEXT_INFO, "select * from employee;");
		e.setField(Constants.ERROR_TEXT, null);
		e.setField(Constants.LOGON_SOURCE, "(TCP/IP) c089 194.2.127.16 DBS-TERADATA1620.COM;DB-TERA CID=2D39989 "
				+ "AVT666744 JDBC17.10.00.14;1.8.0_202 01 LSS");
        e.setField(Constants.OS_USER, "TESTUSER");
		return e;
   } 
   
    @Test 
    public void createQuery() throws ParseException {
   
    	Event e=intitalizeEventObject();
		e.setField(Constants.SQL_TEXT_INFO, "CREATE SET TABLE EMPLOYEE,FALLBACK ( EmployeeNo INTEGER, FirstName VARCHAR(30), "
				+ "LastName VARCHAR(30), DOB DATE FORMAT 'YYYY-MM-DD', JoinedDate DATE FORMAT 'YYYY-MM-DD', "
				+ "DepartmentNo BYTEINT ) UNIQUE PRIMARY INDEX ( EmployeeNo );");
		
        final Record record = Parser.parseRecord(e);
        Assert.assertEquals(record.getData().getConstruct(), null);
    }
    
    @Test 
    public void insertQuery() throws ParseException {
        
    	Event e=intitalizeEventObject();		
    	e.setField(Constants.SQL_TEXT_INFO, "INSERT INTO Employee (EmployeeNo, FirstName, LastName, DOB, JoinedDate, DepartmentNo )"
				+ "VALUES ( 101, 'Mike', 'James', '1980-01-05', '2005-03-27', 01);");
    	
        final Record record = Parser.parseRecord(e);
        Assert.assertEquals(record.getData().getConstruct(), null);
    }
    
    @Test 
    public void selectQuery() throws ParseException {
        
    	Event e=intitalizeEventObject();		
    	e.setField(Constants.SQL_TEXT_INFO, "SELECT A.EmployeeNo, A.DepartmentNo, B.NetPay FROM  Employee A "
    			+ "INNER JOIN Salary B ON (A.EmployeeNo = B. EmployeeNo);");
    	
        final Record record = Parser.parseRecord(e);
        Assert.assertEquals(record.getData().getConstruct(), null);
        Assert.assertEquals(record.getDbName(), record.getAccessor().getServiceName());
    }
   
    
    @Test 
    public void updateQuery() throws ParseException {
        
    	Event e=intitalizeEventObject();		
    	e.setField(Constants.SQL_TEXT_INFO, "UPDATE Employee SET DepartmentNo = 03 WHERE EmployeeNo = 101;");
    	
        final Record record = Parser.parseRecord(e);
        Assert.assertEquals(record.getData().getConstruct(), null);
    }
    
    
    @Test 
    public void deleteQuery() throws ParseException {
        
    	Event e=intitalizeEventObject();		
    	e.setField(Constants.SQL_TEXT_INFO, "DELETE FROM Employee WHERE EmployeeNo = 101;");
    	
        final Record record = Parser.parseRecord(e);
        Assert.assertEquals(record.getData().getConstruct(), null);
    }
        
    @Test
    public void testParseSessionLocator() throws ParseException {
    	Event e=intitalizeEventObject();
    	SessionLocator sessionLocator = Parser.parseSessionLocator(e);
    	
        Assert.assertEquals("9.211.127.160", sessionLocator.getClientIp());
        Assert.assertEquals(-1, sessionLocator.getClientPort());
        Assert.assertEquals(false, sessionLocator.isIpv6());
    } 
    
    @Test
    public void testParseAccessor() throws ParseException {
    	Event e=intitalizeEventObject();
    	Accessor accessor = Parser.parseAccessor(e);
    	
        Assert.assertEquals(Constants.DATA_PROTOCOL_STRING, accessor.getDbProtocol());
        Assert.assertEquals(Constants.SERVER_TYPE_STRING, accessor.getServerType());
        Assert.assertEquals(Constants.TERADATA_LANGUAGE, accessor.getLanguage());
        Assert.assertEquals("TESTUSER", accessor.getOsUser());
    }
    
    @Test
    public void testParseTimestamp() throws ParseException {
    	
    	Event e=intitalizeEventObject();
    	Time time = Parser.parseTimestamp(e);
		Assert.assertEquals(1637048981220L,time.getTimstamp());
    }   
   
    @Test 
    public void testErrors() throws ParseException {
    	Event e=intitalizeEventObject();

    	e.setField(Constants.SQL_TEXT_INFO, "select * from DBC.QryLog;");
 		e.setField(Constants.ERROR_TEXT, "The user does not have SELECT access to DBC.QryLog.");
    	
    	final Record record = Parser.parseRecord(e);

        Assert.assertEquals(Constants.SQL_ERROR,record.getException().getExceptionTypeId());
        Assert.assertEquals("The user does not have SELECT access to DBC.QryLog."
        		,record.getException().getDescription());
    }
}