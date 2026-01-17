//
// Copyright 2020- IBM Inc. All rights reserved
// SPDX-License-Identifier: Apache2.0
//
package com.ibm.guardium.azureSQL;

import java.text.ParseException;

import org.junit.Assert;
import org.junit.Test;

import com.ibm.guardium.azureSQL.Constants;
import com.ibm.guardium.azureSQL.Parser;
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
    	
    	e.setField(Constants.Session_ID, "77");
		e.setField(Constants.TIMESTAMP, "1644335283875860800");
		e.setField(Constants.Client_IP, "194.2.127.16");
		e.setField(Constants.User_Name, "dbadmin");
		e.setField(Constants.STATEMENT, "select * from employee;");
		e.setField(Constants.DATABASE_NAME,"AzureDB");
		e.setField(Constants.APPLICATION_NAME,"SQL SERVER");
		e.setField(Constants.CLIENT_HOST_NAME,"DESKTOP-KJ3D16L");
		e.setField(Constants.Server_Hostname,"test-server-azuresql");
		e.setField(Constants.SUCCEEDED,"true");
		return e;
   } 
   
    @Test 
    public void createQuery() throws ParseException {
   
    	Event e=intitalizeEventObject();
		e.setField(Constants.STATEMENT, "create table emp1(id int);");
		
        final Record record = Parser.parseRecord(e);
        Assert.assertEquals(record.getData().getConstruct(), null);
    }
    
    @Test 
    public void insertQuery() throws ParseException {
        
    	Event e=intitalizeEventObject();		
    	e.setField(Constants.STATEMENT, "INSERT INTO Employee (EmployeeNo, FirstName, LastName, DOB, JoinedDate, DepartmentNo )"
				+ "VALUES ( 101, 'Mike', 'James', '1980-01-05', '2005-03-27', 01);");
    	
        final Record record = Parser.parseRecord(e);
        Assert.assertEquals(record.getData().getConstruct(), null);
    }
    
    @Test 
    public void selectQuery() throws ParseException {
        
    	Event e=intitalizeEventObject();		
    	e.setField(Constants.STATEMENT, "SELECT A.EmployeeNo, A.DepartmentNo, B.NetPay FROM  Employee A "
    			+ "INNER JOIN Salary B ON (A.EmployeeNo = B. EmployeeNo);");
    	
        final Record record = Parser.parseRecord(e);
        Assert.assertEquals(record.getData().getConstruct(), null);
    }
   
    
    @Test 
    public void updateQuery() throws ParseException {
        
    	Event e=intitalizeEventObject();		
    	e.setField(Constants.STATEMENT, "UPDATE Employee SET DepartmentNo = 03 WHERE EmployeeNo = 101;");
    	
        final Record record = Parser.parseRecord(e);
        Assert.assertEquals(record.getData().getConstruct(), null);
    }
    
    
    @Test 
    public void deleteQuery() throws ParseException {
        
    	Event e=intitalizeEventObject();		
    	e.setField(Constants.STATEMENT, "DELETE FROM Employee WHERE EmployeeNo = 101;");
    	
        final Record record = Parser.parseRecord(e);
        Assert.assertEquals(record.getData().getConstruct(), null);
    }
        
    @Test
    public void testParseSessionLocator() throws ParseException {
    	Event e=intitalizeEventObject();
    	SessionLocator sessionLocator = Parser.parseSessionLocator(e);
    	
        Assert.assertEquals("194.2.127.16", sessionLocator.getClientIp());
        Assert.assertEquals(-1, sessionLocator.getClientPort());
        Assert.assertEquals(false, sessionLocator.isIpv6());
    } 
    
    @Test
    public void testParseAccessor() throws ParseException {
    	Event e=intitalizeEventObject();
    	Accessor accessor = Parser.parseAccessor(e);
    	
        Assert.assertEquals(Constants.DATA_PROTOCOL_STRING, accessor.getDbProtocol());
        Assert.assertEquals(Constants.SERVER_TYPE_STRING, accessor.getServerType());
        Assert.assertEquals(Constants.LANGUAGE, accessor.getLanguage());
    }
    
    @Test
    public void testParseTimestamp() throws ParseException {
    	
    	Event e=intitalizeEventObject();
    	Time time = Parser.parseTimestamp(e);
		Assert.assertEquals(1644335283875L,time.getTimstamp());
    }   
   
    @Test 
    public void testErrors() throws ParseException {
    	Event e=intitalizeEventObject();
    	
    	e.setField(Constants.SUCCEEDED, "false");
    	e.setField(Constants.STATEMENT, "select * from emp;");
 		e.setField(Constants.ADDITIONAL_INFORMATION, "Invalid object name");
    	
    	final Record record = Parser.parseRecord(e);

        Assert.assertEquals(Constants.SQL_ERROR,record.getException().getExceptionTypeId());
        Assert.assertEquals("Invalid object name"
        		,record.getException().getDescription());
    }
}