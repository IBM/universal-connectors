/*
© Copyright IBM Corp. 2021, 2022 All rights reserved.
SPDX-License-Identifier: Apache-2.0
*/

package com.ibm.guardium.mariadb;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.logstash.Event;

import com.ibm.guardium.mariadb.constant.ApplicationConstant;
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
		event.setField(ApplicationConstant.CONNECTIONID_KEY, "7");
		event.setField(ApplicationConstant.DATABASE_KEY, "mariadb");
		event.setField(ApplicationConstant.USERNAME_KEY, "root");
		event.setField(ApplicationConstantTest.RETCODE_KEY, "0");
		event.setField(ApplicationConstantTest.OBJECT_KEY, "'show tables'");
		event.setField(ApplicationConstantTest.HOSTNAME_KEY, "localhost");
		event.setField(ApplicationConstantTest.SERVERHOST_KEY, "LP-5CD1184J8J");
		event.setField(ApplicationConstantTest.OPERATION_KEY, "QUERY");
		event.setField("timestamp", "20220204 15:47:44");
		event.setField("totalOffset", "-330");
		Record actualResponse = ParserHelper.parseRecord(event);
		assertNotNull(actualResponse);
		assertEquals("-105977793", actualResponse.getSessionId());
		assertEquals("mariadb", actualResponse.getDbName());
		assertEquals("root", actualResponse.getAppUserName());
		assertEquals(1644009464000L, actualResponse.getTime().getTimstamp());
		assertEquals("localhost", actualResponse.getAccessor().getClientHostName());
		assertEquals("root", actualResponse.getAccessor().getDbUser());
		assertEquals("show tables", actualResponse.getData().getOriginalSqlCommand());

	}

	@Test
	public void parseRecordForElseConditionTest() throws Exception {
		Event event = new org.logstash.Event();
		event.setField(ApplicationConstant.CONNECTIONID_KEY, "7");
		event.setField(ApplicationConstant.DATABASE_KEY, "mariadb");
		event.setField(ApplicationConstant.USERNAME_KEY, "root");
		event.setField(ApplicationConstantTest.RETCODE_KEY, "1064");
		event.setField(ApplicationConstantTest.OBJECT_KEY, "'show tabes'");
		event.setField(ApplicationConstantTest.HOSTNAME_KEY, "localhost");
		event.setField(ApplicationConstantTest.SERVERHOST_KEY, "LP-5CD1184J8J");
		event.setField(ApplicationConstantTest.OPERATION_KEY, "QUERY");
		event.setField("timestamp", "20220204 15:47:44");
		event.setField("totalOffset", "-330");
		Record actualResponse = ParserHelper.parseRecord(event);
		assertNotNull(actualResponse);
		assertEquals("-105977793", actualResponse.getSessionId());
		assertEquals("mariadb", actualResponse.getDbName());
		assertEquals("root", actualResponse.getAppUserName());
		assertEquals("Error (1064)", actualResponse.getException().getDescription());
		assertEquals("SQL_ERROR", actualResponse.getException().getExceptionTypeId());
		assertEquals("'show tabes'", actualResponse.getException().getSqlString());
	}

	@Test
	public void parseRecordForElseCondition1Test() throws Exception {
		Event event = new org.logstash.Event();
		event.setField(ApplicationConstant.CONNECTIONID_KEY, null);
		event.setField(ApplicationConstant.DATABASE_KEY, null);
		event.setField(ApplicationConstant.USERNAME_KEY, null);
		event.setField(ApplicationConstantTest.RETCODE_KEY, "0");
		event.setField(ApplicationConstantTest.OBJECT_KEY, "'show globl variables like \\\\'server-audit%\\\\''");
		event.setField(ApplicationConstantTest.HOSTNAME_KEY, "localhost");
		event.setField(ApplicationConstantTest.SERVERHOST_KEY, "LP-5CD1184J8J");
		event.setField(ApplicationConstantTest.OPERATION_KEY, "QUERY");
		event.setField("timestamp", "20220204 15:47:44");
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
	public void parseAccessorTest() throws Exception {
		Event event = new org.logstash.Event();
		event.setField(ApplicationConstant.HOSTNAME_KEY, ApplicationConstantTest.HOSTNAME_VALUE);
		event.setField(ApplicationConstant.SERVERHOST_KEY, ApplicationConstantTest.SERVERHOST_VALUE);
		event.setField(ApplicationConstant.USERNAME_KEY, ApplicationConstantTest.USERNAME_VALUE);
		event.setField(ApplicationConstant.SOURCEPROGRAM_KEY, ApplicationConstant.SOURCEPROGRAM_VALUE);
		event.setField(ApplicationConstant.SERVER_TYPE_STRING, "MariaDB");
		event.setField(ApplicationConstant.DBPROTOCAL_STRING, "MariaDB");
		Accessor actualResponse = ParserHelper.parseAccessor(event);
		assertNotNull(actualResponse);
		assertEquals(ApplicationConstantTest.HOSTNAME_VALUE, actualResponse.getClientHostName());
		assertEquals(ApplicationConstantTest.SERVERHOST_VALUE, actualResponse.getServerHostName());
		assertEquals(ApplicationConstantTest.USERNAME_VALUE, actualResponse.getDbUser());

	}

	@Test
	public void parseAccessor1Test() throws Exception {
		Event event = new org.logstash.Event();
		event.setField(ApplicationConstant.HOSTNAME_KEY, null);
		event.setField(ApplicationConstant.SERVERHOST_KEY, null);
		event.setField(ApplicationConstant.USERNAME_KEY, null);
		Accessor actualResponse = ParserHelper.parseAccessor(event);
		assertNotNull(actualResponse);
		assertEquals("", actualResponse.getClientHostName());
		assertEquals("", actualResponse.getServerHostName());
		assertEquals("NA", actualResponse.getDbUser());
	}

	@Test
	public void parseAccessorExceptionTest() throws Exception {
		try {
			Accessor accessorexception = ParserHelper.parseAccessor(null);

		} catch (Exception e) {
		}
	}

	@Test
	public void parseSessionLocatorTest() {
		Event event = new org.logstash.Event();
		event.setField(ApplicationConstant.CLIENT_IP, ApplicationConstant.CLIENT_IP_STRING);
		event.setField(ApplicationConstant.SERVER_IP, ApplicationConstant.SERVER_IP_STRING);
		SessionLocator actualResponse = ParserHelper.parseSessionLocator(event);
		assertNotNull(actualResponse);
		assertEquals(ApplicationConstant.CLIENT_IP_STRING, actualResponse.getClientIp());
		assertEquals(ApplicationConstant.SERVER_IP_STRING, actualResponse.getServerIp());

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
		event.setField(ApplicationConstant.RETCODE_KEY, "1064");
		event.setField(ApplicationConstant.OBJECT_KEY, "'USE DATABASES'");
		ExceptionRecord actualResponse = ParserHelper.parseExceptionRecord(event);
		assertNotNull(actualResponse);
		assertEquals("Error (1064)", actualResponse.getDescription());
		assertEquals("SQL_ERROR", actualResponse.getExceptionTypeId());
		assertEquals("'USE DATABASES'", actualResponse.getSqlString());

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
	public void parseAuthorizationTest() throws Exception {
		Event event = new org.logstash.Event();
		event.setField(ApplicationConstant.RETCODE_KEY, "1064");
		event.setField(ApplicationConstant.OBJECT_KEY, null);
		final ExceptionRecord actualResponse = ParserHelper.parseExceptionRecord(event);
		assertNotNull(actualResponse);
		assertEquals("Error (1064)", actualResponse.getDescription());
		assertEquals("SQL_ERROR", actualResponse.getExceptionTypeId());
		assertEquals("NA", actualResponse.getSqlString());

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
		event.setField(ApplicationConstantTest.OBJECT_KEY,
				"'CREATE   USER \\'myusersha\\'@\\'localhost\\' IDENTIFIED BY *****'");
		Data actualResponse = ParserHelper.parseData(event);
		assertNotNull(actualResponse);
		assertEquals("CREATE   USER 'myusersha'@'localhost' IDENTIFIED BY xxxxx",
				actualResponse.getOriginalSqlCommand());

	}

	@Test
	public void parseDataExceptionTest() throws Exception {
		try {
			Data dataexception = ParserHelper.parseData(null);

		} catch (Exception e) {
		}
	}

	@Test
	public void getTimeTest() throws Exception {
		Event event = new Event();
		event.setField(ApplicationConstantTest.TIMESTAMP_KEY, "20211222 12:53:23");
		event.setField("totalOffset", "-330");
		Time actualResponse = ParserHelper.getTime(event);
		assertNotNull(actualResponse);
	}

	@Test
	public void parseTimeExceptionTest() throws Exception {
		try {
			Time timeexception = ParserHelper.getTime(null);

		} catch (Exception e) {
		}
	}

}
