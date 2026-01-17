/*
* Copyright IBM Corp. 2021, 2022 All rights reserved.
* SPDX-License-Identifier: Apache-2.0
*/

package com.ibm.neptune.connector;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.ibm.guardium.universalconnector.commons.structures.Accessor;
import com.ibm.guardium.universalconnector.commons.structures.Data;
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
import com.ibm.neptune.connector.constant.ApplicationConstantTest;

import co.elastic.logstash.api.Event;

public class ParserHelperTest {

	private Record record;

	@BeforeEach
	public void beforeEachTest() {
		record = new Record();
		record.setDbName(ApplicationConstantTest.DBNAME_PREFIX.split(ApplicationConstantTest.DOT)[0]);
	}

	@Test
	public void parseSessionLocatorTest() throws Exception {

		SessionLocator actualResponse = ParserHelper.parseSessionLocator(ApplicationConstantTest.CLIENT_HOST,
				ApplicationConstantTest.SERVER_HOST);
		assertNotNull(actualResponse);
		assertNotNull(actualResponse);
		assertEquals(ApplicationConstantTest.SERVER_IP, actualResponse.getServerIp());
		assertEquals(ApplicationConstantTest.CLIENT_IP, actualResponse.getClientIp());

	}

	@Test
	public void parseAccessorTest() throws Exception {

		Event event = new org.logstash.Event();
		event.setField(ApplicationConstantTest.CALLERIAM_KEY, ApplicationConstantTest.CALLERIAM);
		event.setField(ApplicationConstantTest.SERVER_HOSTNAME_PREFIX_KEY,
				ApplicationConstantTest.SERVER_HOSTNAME_PREFIX);
		Accessor actualResponse = ParserHelper.parseAccessor(event, record);
		assertNotNull(actualResponse);
		assertNotNull(actualResponse.getDbProtocol());
		assertNotNull(actualResponse.getDbProtocolVersion());
		assertNotNull(actualResponse.getSourceProgram());
		assertNotNull(actualResponse.getServerHostName());
		assertEquals(record.getDbName(), actualResponse.getServiceName());
	}

	@Test
	public void parseTimeTest() throws Exception {

		Time actualResponse = ParserHelper.parseTime(ApplicationConstantTest.TIME_STAMP);
		assertNotNull(actualResponse);
		assertEquals(Long.parseLong(ApplicationConstantTest.TIME_STAMP), actualResponse.getTimstamp());

	}

	@Test
	public void parseDataTest() throws Exception {

		Event event = new org.logstash.Event();
		event.setField(ApplicationConstantTest.LOG_MESSAGE_KEY, ApplicationConstantTest.LOG_MESSAGE);
		event.setField(ApplicationConstantTest.PAYLOAD_KEY, ApplicationConstantTest.PAYLOAD);

		Data actualResponse = ParserHelper.parseData(event, record);
		assertNotNull(actualResponse);
		assertEquals(ApplicationConstantTest.PAYLOAD, actualResponse.getConstruct().getFullSql());
	}

	@Test
	public void parseRecordTest1() throws Exception {
		Event event = new org.logstash.Event();
		event.setField(ApplicationConstantTest.TIME_STAMP_KEY, ApplicationConstantTest.TIME_STAMP);
		event.setField(ApplicationConstantTest.SERVER_HOST_KEY, ApplicationConstantTest.SERVER_HOST);
		event.setField(ApplicationConstantTest.CLIENT_HOST_KEY, ApplicationConstantTest.CLIENT_HOST);
		event.setField(ApplicationConstantTest.HTTP_HEADERS_KEY, ApplicationConstantTest.HTTP_HEADERS);
		event.setField(ApplicationConstantTest.LOG_MESSAGE_KEY, ApplicationConstantTest.LOG_MESSAGE);
		event.setField(ApplicationConstantTest.PAYLOAD_KEY, ApplicationConstantTest.PAYLOAD);
		event.setField(ApplicationConstantTest.CALLERIAM_KEY, ApplicationConstantTest.CALLERIAM);
		event.setField(ApplicationConstantTest.SERVER_HOSTNAME_PREFIX_KEY,
				ApplicationConstantTest.SERVER_HOSTNAME_PREFIX);
		event.setField(ApplicationConstantTest.DBNAME_PREFIX_KEY, ApplicationConstantTest.DBNAME_PREFIX);

		Record actualResponse = ParserHelper.parseRecord(event, record);

		assertNotNull(actualResponse);
		assertEquals(record.getDbName(), actualResponse.getAccessor().getServiceName());
		assertEquals(
				event.getField(ApplicationConstantTest.SERVER_HOSTNAME_PREFIX_KEY).toString()
						.split(ApplicationConstantTest.DOT)[0] + ".aws.com",
				actualResponse.getAccessor().getServerHostName());
		assertEquals(Long.parseLong(ApplicationConstantTest.TIME_STAMP), actualResponse.getTime().getTimstamp());
		assertEquals(ApplicationConstantTest.SERVER_IP, actualResponse.getSessionLocator().getServerIp());
		assertEquals(Integer.parseInt(ApplicationConstantTest.SERVER_PORT),
				actualResponse.getSessionLocator().getServerPort());
		assertEquals(ApplicationConstantTest.CLIENT_IP, actualResponse.getSessionLocator().getClientIp());
		assertEquals(Integer.parseInt(ApplicationConstantTest.CLIENT_PORT),
				actualResponse.getSessionLocator().getClientPort());
		assertEquals(ApplicationConstantTest.PAYLOAD, actualResponse.getData().getConstruct().getFullSql());

	}

	@Test
	public void parseRecordTest2() throws Exception {
		Event event = new org.logstash.Event();
		event.setField(ApplicationConstantTest.TIME_STAMP_KEY, ApplicationConstantTest.TIME_STAMP);
		event.setField(ApplicationConstantTest.SERVER_HOST_KEY, ApplicationConstantTest.SERVER_HOST);
		event.setField(ApplicationConstantTest.CLIENT_HOST_KEY, ApplicationConstantTest.CLIENT_HOST1);
		event.setField(ApplicationConstantTest.HTTP_HEADERS_KEY, ApplicationConstantTest.HTTP_HEADERS);
		event.setField(ApplicationConstantTest.LOG_MESSAGE_KEY, ApplicationConstantTest.LOG_MESSAGE1);
		event.setField(ApplicationConstantTest.PAYLOAD_KEY, ApplicationConstantTest.ACTUAL_PAYLOAD);
		event.setField(ApplicationConstantTest.CALLERIAM_KEY, ApplicationConstantTest.CALLERIAM);
		event.setField(ApplicationConstantTest.SERVER_HOSTNAME_PREFIX_KEY,
				ApplicationConstantTest.SERVER_HOSTNAME_PREFIX);
		event.setField(ApplicationConstantTest.DBNAME_PREFIX_KEY, ApplicationConstantTest.DBNAME_PREFIX);

		Record actualResponse = ParserHelper.parseRecord(event, record);

		assertNotNull(actualResponse);
		assertEquals(record.getDbName(), actualResponse.getAccessor().getServiceName());
		assertEquals(
				event.getField(ApplicationConstantTest.SERVER_HOSTNAME_PREFIX_KEY).toString()
						.split(ApplicationConstantTest.DOT)[0] + ".aws.com",
				actualResponse.getAccessor().getServerHostName());
		assertEquals(Long.parseLong(ApplicationConstantTest.TIME_STAMP), actualResponse.getTime().getTimstamp());
		assertEquals(ApplicationConstantTest.SERVER_IP, actualResponse.getSessionLocator().getServerIp());
		assertEquals(Integer.parseInt(ApplicationConstantTest.SERVER_PORT),
				actualResponse.getSessionLocator().getServerPort());
		assertEquals(ApplicationConstantTest.CLIENT_IP, actualResponse.getSessionLocator().getClientIp());
		assertEquals(Integer.parseInt(ApplicationConstantTest.CLIENT_PORT),
				actualResponse.getSessionLocator().getClientPort());

	}

	@Test
	public void decodeContenTest() {
		String query = null;

		String actual = ParserHelper.decodeContent(query);
		assertEquals(null, actual);

	}

	@Test
	public void prepareGuardRecordDataTest() {

		Event event = new org.logstash.Event();
		Record actual = null;
		try {
			actual = ParserHelper.parseRecord(event, record);
		} catch (Exception e) {

			assertNull(actual);
		}

	}

}
