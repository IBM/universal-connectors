/*
 Copyright IBM Corp. 2021, 2022 All rights reserved.
SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.guardium.awsmariadb;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.logstash.Event;

import com.ibm.guardium.awsmariadb.ParserHelper;
import com.ibm.guardium.awsmariadb.constant.ApplicationConstant;
import com.ibm.guardium.universalconnector.commons.structures.Accessor;
import com.ibm.guardium.universalconnector.commons.structures.Data;
import com.ibm.guardium.universalconnector.commons.structures.ExceptionRecord;
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

public class ParserHelperTest {
	@Test
	public void parseRecordTest() throws Exception {
		Event event = new org.logstash.Event();
		event.setField(ApplicationConstant.CONNECTIONID_KEY, "231");
		event.setField(ApplicationConstant.DBNAMEPREFIX_KEY, "979326520502:database-mariadbtest:Connector");
		event.setField(ApplicationConstant.USERNAME_KEY, "admin");
		event.setField(ApplicationConstantTest.RETCODE_KEY, "0");
		event.setField(ApplicationConstantTest.OBJECT_KEY, "show tables");
		event.setField(ApplicationConstantTest.HOSTNAME_KEY, "localhost");
		event.setField(ApplicationConstant.SERVERHOSTNAMEPREFIX_KEY, "979326520502-database-mariadbtest");
		event.setField(ApplicationConstantTest.OPERATION_KEY, "QUERY");
		event.setField("timestamp", "20220204 15:47:44");
		Record actualResponse = ParserHelper.parseRecord(event);
		assertNotNull(actualResponse);
//		assertEquals("231", actualResponse.getSessionId());
		assertEquals("331346110", actualResponse.getSessionId());
		assertEquals("979326520502:database-mariadbtest:Connector", actualResponse.getDbName());
		assertEquals("admin", actualResponse.getAppUserName());
		assertEquals("localhost", actualResponse.getAccessor().getClientHostName());
		assertEquals("admin", actualResponse.getAccessor().getDbUser());
		assertEquals("show tables", actualResponse.getData().getOriginalSqlCommand());

	}

	@Test
	public void parseRecordForElseConditionTest() throws Exception {
		Event event = new org.logstash.Event();
		event.setField(ApplicationConstant.CONNECTIONID_KEY, "7");
		event.setField(ApplicationConstant.DBNAMEPREFIX_KEY, "979326520502:database-mariadbtest:Connector");
		event.setField(ApplicationConstant.USERNAME_KEY, "admin");
		event.setField(ApplicationConstantTest.RETCODE_KEY, "1146");
		event.setField(ApplicationConstantTest.OBJECT_KEY, "'show tabes'");
		event.setField(ApplicationConstantTest.HOSTNAME_KEY, "localhost");
		event.setField(ApplicationConstant.SERVERHOSTNAMEPREFIX_KEY, "979326520502-database-mariadbtest");
		event.setField(ApplicationConstantTest.OPERATION_KEY, "QUERY");
		event.setField("timestamp", "20220524 03:22:54");
		event.setField("totalOffset", "-330");
		Record actualResponse = ParserHelper.parseRecord(event);
		assertNotNull(actualResponse);
//		assertEquals("7", actualResponse.getSessionId());
		assertEquals("-1157520155", actualResponse.getSessionId());
		assertEquals("979326520502:database-mariadbtest:Connector", actualResponse.getDbName());
		assertEquals("admin", actualResponse.getAppUserName());
		assertEquals("Error (1146)", actualResponse.getException().getDescription());
		assertEquals("SQL_ERROR", actualResponse.getException().getExceptionTypeId());
		assertEquals("'show tabes'", actualResponse.getException().getSqlString());
	}

	@Test
	public void parseRecordForElseCondition1Test() throws Exception {
		Event event = new org.logstash.Event();
		event.setField(ApplicationConstant.CONNECTIONID_KEY, null);
		event.setField(ApplicationConstant.DATABASE_KEY, null);
		event.setField(ApplicationConstant.USERNAME_KEY, null);
		event.setField(ApplicationConstantTest.RETCODE_KEY, null);
		event.setField(ApplicationConstantTest.OBJECT_KEY, "'show globl variables like \\\\'server-audit%\\\\''");
		event.setField(ApplicationConstantTest.HOSTNAME_KEY, "localhost");
		event.setField(ApplicationConstant.SERVERHOSTNAMEPREFIX_KEY, "979326520502-database-mariadbtest");
		event.setField(ApplicationConstantTest.OPERATION_KEY, "QUERY");
		event.setField("timestamp", "20220524 03:22:54");
		event.setField("totalOffset", "-330");
		Record actualResponse = ParserHelper.parseRecord(event);
		assertNotNull(actualResponse);

	}

	@Test
	public void parseRecordExceptionTest() throws Exception {
		try {
			Record accessorexception = ParserHelper.parseRecord(null);

		} catch (Exception e) {
		}
	}

	@Test
	public void parseAccessorTest() {
		Event event = new org.logstash.Event();
		Record record = new Record();
		event.setField(ApplicationConstant.HOSTNAME_KEY, ApplicationConstantTest.HOSTNAME_VALUE);
		event.setField(ApplicationConstant.SERVERHOSTNAMEPREFIX_KEY, "979326520502-database-mariadbtest");
		event.setField(ApplicationConstant.USERNAME_KEY, ApplicationConstantTest.USERNAME_VALUE);
		event.setField(ApplicationConstant.SOURCEPROGRAM_KEY, ApplicationConstant.SOURCEPROGRAM_VALUE);
		event.setField(ApplicationConstant.DBNAMEPREFIX_KEY, "979326520502:database-mariadbtest:Connector");
		Accessor actualResponse = ParserHelper.parseAccessor(event, record);
		assertNotNull(actualResponse);
		assertEquals("", actualResponse.getClientHostName());
		assertEquals("979326520502-database-mariadbtest.aws.com", actualResponse.getServerHostName());
		assertEquals(ApplicationConstantTest.USERNAME_VALUE, actualResponse.getDbUser());
		assertEquals("", actualResponse.getSourceProgram());

	}

	@Test
	public void parseAccessor1Test() {
		Event event = new org.logstash.Event();
		Record record = new Record();
		event.setField(ApplicationConstant.HOSTNAME_KEY, null);
		event.setField(ApplicationConstant.SERVERHOST_KEY, null);
		event.setField(ApplicationConstant.USERNAME_KEY, null);
		Accessor actualResponse = ParserHelper.parseAccessor(event, record);
		assertNotNull(actualResponse);
		assertEquals("", actualResponse.getClientHostName());
		assertEquals("mariaDB.aws.com", actualResponse.getServerHostName());
		assertEquals("NA", actualResponse.getDbUser());
	}

	@Test
	public void parseAccessorExceptionTest() throws Exception {
		Record record = new Record();
		try {
			Accessor accessorexception = ParserHelper.parseAccessor(null, record);

		} catch (Exception e) {
		}
	}

	@Test
	public void parseSessionLocatorTest() {
		Event event = new org.logstash.Event();
		event.setField(ApplicationConstant.CLIENT_IP, ApplicationConstant.DEFAULT_IP);
		event.setField(ApplicationConstant.SERVER_IP, ApplicationConstant.DEFAULT_IP);
		SessionLocator actualResponse = ParserHelper.parseSessionLocator(event);
		assertNotNull(actualResponse);
		assertEquals(ApplicationConstant.DEFAULT_IP, actualResponse.getClientIp());
		assertEquals(ApplicationConstant.DEFAULT_IP, actualResponse.getServerIp());

	}

	@Test
	public void parseSessionLocatorExceptionTest() throws Exception {
		try {
			SessionLocator actualResponse = ParserHelper.parseSessionLocator(null);

		} catch (Exception e) {
		}
	}

	@Test
	public void parseExceptionTest() throws Exception {
		Event event = new org.logstash.Event();
		event.setField(ApplicationConstant.RETCODE_KEY, "1050");
		event.setField(ApplicationConstant.OBJECT_KEY, "'USE DTABASES'");
		ExceptionRecord actualResponse = ParserHelper.parseExceptionRecord(event);
		assertNotNull(actualResponse);
		assertEquals("Error (1050)", actualResponse.getDescription());
		assertEquals("SQL_ERROR", actualResponse.getExceptionTypeId());
		assertEquals("'USE DTABASES'", actualResponse.getSqlString());

	}

	@Test
	public void testAuthenticationFailedError() throws Exception {
		Event event = new org.logstash.Event();
		event.setField(ApplicationConstant.RETCODE_KEY, "1045");
		event.setField(ApplicationConstant.OBJECT_KEY, "");
		final ExceptionRecord actualResponse = ParserHelper.parseExceptionRecord(event);
		assertNotNull(actualResponse);
		assertEquals("LOGIN_FAILED", actualResponse.getExceptionTypeId());
		assertEquals("Authentication Failed (1045)", actualResponse.getDescription());
		assertEquals("", actualResponse.getSqlString());

	}

	@Test
	public void testAuthenticationFailed1Error() throws Exception {
		Event event = new org.logstash.Event();
		event.setField(ApplicationConstant.RETCODE_KEY, "1043");
		event.setField(ApplicationConstant.OBJECT_KEY, "");
		final ExceptionRecord actualResponse = ParserHelper.parseExceptionRecord(event);
		assertNotNull(actualResponse);
		assertEquals("LOGIN_FAILED", actualResponse.getExceptionTypeId());
		assertEquals("Authentication Failed (1043)", actualResponse.getDescription());
		assertEquals("", actualResponse.getSqlString());

	}

	@Test
	public void parseAuthorizationTest() throws Exception {
		Event event = new org.logstash.Event();
		event.setField(ApplicationConstant.RETCODE_KEY, "1146");
		event.setField(ApplicationConstant.OBJECT_KEY, "'show databas'");
		final ExceptionRecord actualResponse = ParserHelper.parseExceptionRecord(event);
		assertNotNull(actualResponse);
		assertEquals("Error (1146)", actualResponse.getDescription());
		assertEquals("SQL_ERROR", actualResponse.getExceptionTypeId());
		assertEquals("'show databas'", actualResponse.getSqlString());

	}

	@Test
	public void parseExceptionRecordTestException() throws Exception {
		try {
			ExceptionRecord exceptionrecord = ParserHelper.parseExceptionRecord(null);

		} catch (Exception e) {

		}
	}

	@Test
	public void parseDataTest() throws Exception {
		Event event = new org.logstash.Event();
		event.setField(ApplicationConstantTest.OBJECT_KEY, "CREATE USER foo2@test1 IDENTIFIED BY *****");
		Data actualResponse = ParserHelper.parseData(event);
		assertNotNull(actualResponse);
		assertEquals("CREATE USER foo2@test1 IDENTIFIED BY xxxxx", actualResponse.getOriginalSqlCommand());

	}

	@Test
	public void parseDataExceptionTest() throws Exception {
		try {
			Data dataexception = ParserHelper.parseData(null);

		} catch (Exception e) {
		}
	}

	@Test
	public void parseTimeTest() throws Exception {
		Event event = new Event();
		event.setField(ApplicationConstantTest.TIMESTAMP_KEY, "20211222 12:53:23");
		Time actualResponse = ParserHelper.parseTime(event);
		assertNotNull(actualResponse);
	}

	@Test
	public void parseTimeExceptionTest() throws Exception {
		try {
			Time timeexception = ParserHelper.parseTime(null);

		} catch (Exception e) {
		}
	}

}
