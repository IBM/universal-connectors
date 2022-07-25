/*
© Copyright IBM Corp. 2021, 2022 All rights reserved.

SPDX-License-Identifier: Apache-2.0
*/

package com.ibm.guardium.couchdb;

import static org.junit.jupiter.api.Assertions.*;

import java.text.ParseException;

import org.junit.jupiter.api.Test;

import com.ibm.guardium.univer***REMOVED***lconnector.commons.structures.Accessor;
import com.ibm.guardium.univer***REMOVED***lconnector.commons.structures.Construct;
import com.ibm.guardium.univer***REMOVED***lconnector.commons.structures.Data;
import com.ibm.guardium.univer***REMOVED***lconnector.commons.structures.ExceptionRecord;
import com.ibm.guardium.univer***REMOVED***lconnector.commons.structures.Record;
import com.ibm.guardium.univer***REMOVED***lconnector.commons.structures.Sentence;
import com.ibm.guardium.univer***REMOVED***lconnector.commons.structures.SessionLocator;
import com.ibm.guardium.univer***REMOVED***lconnector.commons.structures.Time;

import co.elastic.logstash.api.Event;

class ParserTest {
	
	@Test
	public void testParseRecord() throws Exception {
		Event event = new org.logstash.Event();
		event.setField(ApplicationConstant.USER_NAME, "sravanthi");
		event.setField(ApplicationConstant.VERB, "GET");
		event.setField(ApplicationConstant.ID,"36fa3c0487");
		event.setField(ApplicationConstant.DB_NAME, "fruits");
		event.setField(ApplicationConstant.LOGMESSAGE, "");
		event.setField(ApplicationConstant.TIMESTAMP, "2022-02-21T07:03:10.759000Z");
		event.setField(ApplicationConstant.STATUS, "200");
		event.setField(ApplicationConstant.DESCRIPTION, "ok");
		event.setField(ApplicationConstant.TIME_INTERVAL, "30");
		final Record record = Parser.parseRecord(event);
		assertNotNull(record);
		assertEquals("", record.getAppUserName());
		assertEquals("fruits", record.getDbName());
		assertEquals("36fa3c0487",record.getSessionId());
		assertEquals(1645426990759L,record.getTime().getTimstamp());
	}
	
	@Test
	public void testParseRecordForElseCondition() throws Exception {
		Event event = new org.logstash.Event();
		event.setField(ApplicationConstant.VERB, "GET");
		event.setField(ApplicationConstant.DB_NAME, "fruits");
		event.setField(ApplicationConstant.STATUS, "404");
		event.setField(ApplicationConstant.TIMESTAMP, "2022-02-21T07:03:10.759000Z");
		final Record record = Parser.parseRecord(event);
		assertNotNull(record);
		assertEquals("fruits", record.getDbName());
	}
	
	@Test
	public void testParseSenssionLocator() {
		Event event = new org.logstash.Event();
		event.setField(ApplicationConstant.CLIENT_IP, "127.0.0.1");
		event.setField(ApplicationConstant.CLIENT_PORT, "5984");
		event.setField(ApplicationConstant.SERVER_IP, "172.31.12.122");
		final SessionLocator session = Parser.parseSessionLocator(event);
		assertNotNull(session);
		assertEquals("127.0.0.1", session.getClientIp());
		assertEquals(-1,session.getClientPort());
		assertEquals("172.31.12.122", session.getServerIp());
	}

	@Test
	public void testParseAccessor() {
		Event event = new org.logstash.Event();
		event.setField(ApplicationConstant.USER_NAME, "sravanthi");
		event.setField(ApplicationConstant.DB_NAME, "fruits");
		event.setField(Parser.SERVER_TYPE_STRING, "CouchDB");
		event.setField(Parser.DATA_PROTOCOL, "CouchDB");
		event.setField(ApplicationConstant.SERVER_IP, "172.31.12.122");
		event.setField(ApplicationConstant.SERVER_HOSTNAME, "ip-172-31-12-122");
		final Accessor accessor = Parser.parseAccessor(event);
		assertNotNull(accessor);
		assertEquals("sravanthi", accessor.getDbUser());
		assertEquals("CouchDB", accessor.getServiceName());
		assertEquals("CouchDB", accessor.getServerType());
		assertEquals("CouchDB", accessor.getDbProtocol());
		assertEquals("ip-172-31-12-122", accessor.getServerHostName());
	}
	@Test
	public void testAuthenticationFailedError() {
	Event event = new org.logstash.Event();
	event.setField(ApplicationConstant.VERB, "GET");
	event.setField(ApplicationConstant.DB_NAME,"fruits");
	event.setField(ApplicationConstant.STATUS, "401");
	final ExceptionRecord exception = Parser.parseExceptionRecord(event);
	assertNotNull(exception);
	assertEquals("LOGIN_FAILED", exception.getExceptionTypeId());
	assertEquals("Authentication Failed (401)", exception.getDescription());
	assertEquals("GET fruits", exception.getSqlString());
	}

	@Test
	public void testAuthorizationFailedError() {
	Event event = new org.logstash.Event();
	event.setField(ApplicationConstant.VERB, "GET");
	event.setField(ApplicationConstant.DB_NAME,"fruits");
	event.setField(ApplicationConstant.STATUS, "103");
	final ExceptionRecord exception = Parser.parseExceptionRecord(event);
	assertNotNull(exception);
	assertEquals("SQL_ERROR", exception.getExceptionTypeId());
	assertEquals("Error (" + event.getField(ApplicationConstant.STATUS) + ")", exception.getDescription());
	assertEquals("GET fruits", exception.getSqlString());
	}		
	
	@Test
	public void testGetTime() throws Exception {
		Event event = new org.logstash.Event();
		event.setField(ApplicationConstant.TIMESTAMP, "2022-02-21T07:03:10.759000Z");
		final Time time = Parser.getTime(event);
		assertNotNull(time);
		assertEquals(1645426990759L,time.getTimstamp());
	}
	
}
