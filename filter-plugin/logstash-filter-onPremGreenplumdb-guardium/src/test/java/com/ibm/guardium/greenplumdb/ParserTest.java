/*
Copyright IBM Corp. 2021, 2022 All rights reserved.
SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.guardium.greenplumdb;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

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

import co.elastic.logstash.api.Event;

/**
 * 
 * @author shivam_agarwal
 *
 */
public class ParserTest {

	/**
	 * 
	 * @throws Exception
	 */
	@Test
	public void parserRecordTest1() throws Exception {

		Event event = new org.logstash.Event();
		event.setField(ApplicationConstantTest.TIMESTAMP, "2022-08-05 06:32:41.527966 IST");
		event.setField(ApplicationConstantTest.MIN_OFFSET,"+05:00");
		event.setField(ApplicationConstantTest.REMOTE_HOST, "172.31.7.158");
		event.setField(ApplicationConstantTest.REMOTE_PORT, "56116");
		event.setField(ApplicationConstantTest.USER_NAME, "ec2-user");
		event.setField(ApplicationConstantTest.DATABASE_NAME, "test1");
		event.setField(ApplicationConstantTest.PROCESS_ID, "p1713");
		event.setField(ApplicationConstantTest.CONNECTION_ID, "con7");
		event.setField(ApplicationConstantTest.QUERY, "DELETE FROM products5 where prod_id = 10;");
		event.setField(ApplicationConstantTest.SERVER_HOSTNAME, "ip-172-31-7-158.ap-south-1.compute.internal");
		event.setField(ApplicationConstant.EVENT_SEVERITY, "LOG");
		Record actualResponse = Parser.parseRecord(event);
		assertNotNull(actualResponse);
		assertNotNull(actualResponse.getTime());
		assertNotNull(actualResponse.getSessionLocator());
		assertNotNull(actualResponse.getAccessor());
		assertNotNull(actualResponse.getData());

	}

	@Test
	public void parseRecordTest2() throws Exception {
		Event event = new org.logstash.Event();
		event.setField(ApplicationConstantTest.TIMESTAMP, "2022-08-05 06:32:41.527966 IST");
		event.setField(ApplicationConstant.MIN_OFFSET,"-04:00");
		event.setField(ApplicationConstantTest.REMOTE_HOST, "172.31.7.158");
		event.setField(ApplicationConstantTest.REMOTE_PORT, "56116");
		event.setField(ApplicationConstantTest.USER_NAME, "ec2-user");
		event.setField(ApplicationConstantTest.QUERY, "DELETE FROM products5 where prod_id = 10;");
		event.setField(ApplicationConstantTest.SERVER_HOSTNAME, "ip-172-31-7-158.ap-south-1.compute.internal");
		event.setField(ApplicationConstant.EVENT_SEVERITY, "LOG");
		Record record = Parser.parseRecord(event);
		assertNotNull(record);
		assertEquals(ApplicationConstant.EMPTY, record.getDbName());
	}

	/**
	 * 
	 * @throws Exception
	 */
	@Test
	public void parseTimeTest() throws Exception {
		Event event = new org.logstash.Event();
		event.setField(ApplicationConstant.TIMESTAMP, "2022-08-05 06:08:31.399413 UTC");
		Time actualResponse = Parser.parseTime(event);
		assertNotNull(actualResponse);

	}

	@Test
	public void parseAccessorTest() throws Exception {
		Event event = new org.logstash.Event();
		Record record = new Record();
		record.setDbName("test1");
		Accessor actual = Parser.parseAccessor(event, record);
		assertNotNull(actual);
		assertEquals(ApplicationConstant.NOT_AVAILABLE, actual.getDbUser());
		assertEquals(ApplicationConstant.EMPTY, actual.getServerHostName());
	}

	@Test
	public void parseAccessorExceptionTest() {
		Event event = new org.logstash.Event();
		event.setField(ApplicationConstantTest.USER_NAME, "ec2-user");
		event.setField(ApplicationConstantTest.SERVER_HOSTNAME, "ip-172-31-7-158.ap-south-1.compute.internal");
		Accessor actual = null;
		try {
			actual = Parser.parseAccessor(event, null);
		} catch (Exception e) {

			assertNull(actual);
		}

	}

	@Test
	public void parseDataTest() {

		Event event = new org.logstash.Event();
		Data data = null;
		try {
			data = Parser.parseData(event);
		} catch (Exception e) {
			assertNull(data);
		}

	}

	@Test
	public void parseSessionLocatorTest1() throws Exception {
		Event event = new org.logstash.Event();

		event.setField(ApplicationConstant.REMOTE_PORT, ApplicationConstant.EMPTY);
		SessionLocator sessionLocator = Parser.parseSessionLocator(event);
		assertNotNull(sessionLocator);
		assertEquals(ApplicationConstant.DEFAULT_IPV4, sessionLocator.getServerIp());
		assertEquals(Integer.parseInt(ApplicationConstant.DEFAULT_PORT), sessionLocator.getClientPort());

	}

	@Test
	public void parseSessionLocatorTest2() throws Exception {
		Event event = new org.logstash.Event();

		event.setField(ApplicationConstantTest.REMOTE_HOST, ApplicationConstant.EMPTY);
		event.setField(ApplicationConstantTest.REMOTE_PORT, ApplicationConstant.EMPTY);
		SessionLocator sessionLocator = Parser.parseSessionLocator(event);

		assertNotNull(sessionLocator);
		assertEquals(ApplicationConstant.DEFAULT_IPV4, sessionLocator.getServerIp());
		assertEquals(Integer.parseInt(ApplicationConstant.DEFAULT_PORT), sessionLocator.getClientPort());

	}

}
