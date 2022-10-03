/*
© Copyright IBM Corp. 2021, 2022 All rights reserved.
SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.guardium.greenplumdb;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.logstash.plugins.ConfigurationImpl;

import com.ibm.guardium.universalconnector.commons.GuardConstants;

import co.elastic.logstash.api.Configuration;
import co.elastic.logstash.api.Event;
import co.elastic.logstash.api.FilterMatchListener;

public class GreenplumdbGuardiumFilterTest {

	private static final Configuration CONFIG = new ConfigurationImpl(
			Collections.singletonMap(ApplicationConstantTest.CONFIG_KEY, ApplicationConstantTest.CONFIG_VALUE));

	private static final GreenplumdbGuardiumFilter FILTER = new GreenplumdbGuardiumFilter(ApplicationConstantTest.ID,
			CONFIG, null);

	private TestMatchListener matchListener;

	@BeforeEach
	public void beforeEach() {

		matchListener = new TestMatchListener();

	}

	@Test
	public void filterTest1() {

		final String message = "2022-08-05 06:32:41.527966 UTC,ec2-user,template1,p1713,th1225320576,172.31.7.158,56116,2022-08-05 06:32:41 UTC,0,con7,cmd1,seg0,,dx4,,sx1,LOG,0,\"statement: CREATE TABLE products5(name varchar(40),prod_id integer,supplier_id integer)DISTRIBUTED BY (prod_id);\",,,,,,\"CREATE TABLE products5(name varchar(40),prod_id integer,supplier_id integer)DISTRIBUTED BY (prod_id);\",0,,postgres.c,1243";
		List<Event> events = new ArrayList<>();
		Event event = new org.logstash.Event();
		events.add(event);
		event.setField(ApplicationConstantTest.MESSAGE, message);
		event.setField(ApplicationConstantTest.TIMESTAMP, "2022-08-05 06:32:41.527966 UTC");
		event.setField(ApplicationConstantTest.REMOTE_HOST, "172.31.7.158");
		event.setField(ApplicationConstantTest.REMOTE_PORT, "56116");
		event.setField(ApplicationConstantTest.USER_NAME, "ec2-user");
		event.setField(ApplicationConstantTest.DATABASE_NAME, "template1");
		event.setField(ApplicationConstantTest.PROCESS_ID, "p1713");
		event.setField(ApplicationConstantTest.CONNECTION_ID, "con7");
		event.setField(ApplicationConstant.TRANSACTION_ID, "0");
		event.setField(ApplicationConstantTest.QUERY,
				"CREATE TABLE products5(name varchar(40),prod_id integer,supplier_id integer)DISTRIBUTED BY (prod_id);");
		event.setField(ApplicationConstantTest.SERVER_HOSTNAME, "ip-172-31-7-158.ap-south-1.compute.internal");
		event.setField(ApplicationConstant.EVENT_SEVERITY, "LOG");
		Collection<Event> actaulResponse = FILTER.filter(events, matchListener);
		assertNotNull(actaulResponse);
		assertEquals(1, actaulResponse.size());
		assertEquals(1, matchListener.getMatchCount());
		assertNotNull(event.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));

	}

	@Test
	public void filterTest2() {

		final String message = "2022-08-05 06:32:41.527966 IST,ec2-user,test1,p1713,th1225320576,172.31.7.158,56116,2022-08-05 06:32:41 IST,0,con7,cmd1,seg0,,dx4,,sx1,LOG,0,statement: DELETE FROM products5 where prod_id = 10;,,,,,,DELETE FROM products5 where prod_id = 10;,0,,postgres.c,1243";
		List<Event> events = new ArrayList<>();
		Event event = new org.logstash.Event();
		events.add(event);
		event.setField(ApplicationConstantTest.MESSAGE, message);
		event.setField(ApplicationConstantTest.TIMESTAMP, "2022-08-05 06:32:41.527966 IST");
		event.setField(ApplicationConstantTest.REMOTE_HOST, "172.31.7.158");
		event.setField(ApplicationConstantTest.REMOTE_PORT, "56116");
		event.setField(ApplicationConstantTest.USER_NAME, "ec2-user");
		event.setField(ApplicationConstantTest.DATABASE_NAME, "test1");
		event.setField(ApplicationConstantTest.PROCESS_ID, "p1713");
		event.setField(ApplicationConstantTest.CONNECTION_ID, "con7");
		event.setField(ApplicationConstant.TRANSACTION_ID, "0");
		event.setField(ApplicationConstantTest.QUERY, "\"delete from products5;\"");
		event.setField(ApplicationConstantTest.SERVER_HOSTNAME, "ip-172-31-7-158.ap-south-1.compute.internal");
		event.setField(ApplicationConstant.EVENT_SEVERITY, "LOG");
		Collection<Event> actaulResponse = FILTER.filter(events, matchListener);
		assertNotNull(actaulResponse);
		assertEquals(1, actaulResponse.size());
		assertEquals(1, matchListener.getMatchCount());
		assertNotNull(event.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));

	}

	@Test
	public void filterTest3() {

		final String message = "2022-08-05 06:32:41.527966 IST,ec2-user,test1,p1713,th1225320576,172.31.7.158,56116,2022-08-05 06:32:41 IST,0,con7,cmd1,seg0,,dx4,,sx1,LOG,0,statement: DELETE FROM products5 where prod_id = 10;,,,,,,DELETE FROM products5 where prod_id = 10;,0,,postmaster.c,1243";
		List<Event> events = new ArrayList<>();
		Event event = new org.logstash.Event();
		events.add(event);
		event.setField(ApplicationConstantTest.MESSAGE, message);
		event.setField(ApplicationConstantTest.TIMESTAMP, "2022-08-05 06:32:41.527966 IST");
		event.setField(ApplicationConstantTest.REMOTE_HOST, "172.31.7.158");
		event.setField(ApplicationConstantTest.REMOTE_PORT, "56116");
		event.setField(ApplicationConstantTest.USER_NAME, "ec2-user");
		event.setField(ApplicationConstantTest.DATABASE_NAME, "test1");
		event.setField(ApplicationConstantTest.PROCESS_ID, "p1713");
		event.setField(ApplicationConstantTest.CONNECTION_ID, "con7");
		event.setField(ApplicationConstantTest.QUERY, "\"delete from products;\"");
		event.setField(ApplicationConstantTest.SERVER_HOSTNAME, "ip-172-31-7-158.ap-south-1.compute.internal");
		event.setField(ApplicationConstant.EVENT_SEVERITY, "LOG");
		Collection<Event> actaulResponse = FILTER.filter(events, matchListener);
		assertNotNull(actaulResponse);
		assertEquals(0, actaulResponse.size());
		assertEquals(0, matchListener.getMatchCount());
		assertNull(event.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));

	}

	@Test
	public void filterTest4() {

		final String message = "2022-08-05 06:32:41.527966,ec2-user,test1,p1713,th1225320576,172.31.7.158,56116,2022-08-05 06:32:41 IST,0,con7,cmd1,seg0,,dx4,,sx1,LOG,0,statement: DELETE FROM products5 where prod_id = 10;,,,,,,DELETE FROM products5 where prod_id = 10;,0,,postgres.c,1243";
		List<Event> events = new ArrayList<>();
		Event event = new org.logstash.Event();
		events.add(event);
		event.setField(ApplicationConstantTest.MESSAGE, message);
		event.setField(ApplicationConstantTest.TIMESTAMP, "2022-08-05 06:32:41.527966");
		event.setField(ApplicationConstantTest.REMOTE_HOST, "172.31.7.158");
		event.setField(ApplicationConstantTest.REMOTE_PORT, "56116");
		event.setField(ApplicationConstantTest.USER_NAME, "ec2-user");
		event.setField(ApplicationConstantTest.DATABASE_NAME, "test1");
		event.setField(ApplicationConstantTest.PROCESS_ID, "p1713");
		event.setField(ApplicationConstantTest.CONNECTION_ID, "con7");
		event.setField(ApplicationConstantTest.QUERY, "\"delete from products5;\"");
		event.setField(ApplicationConstantTest.SERVER_HOSTNAME, "ip-172-31-7-158.ap-south-1.compute.internal");
		event.setField(ApplicationConstant.EVENT_SEVERITY, "LOG");
		Collection<Event> actaulResponse = FILTER.filter(events, matchListener);
		assertNotNull(actaulResponse);
		assertEquals(0, actaulResponse.size());
		assertEquals(0, matchListener.getMatchCount());
		assertNull(event.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));

	}

	@Test
	public void filterTest5() {

		final String message = "2022-08-05 06:32:41.527966 IST,ec2-user,test1,p1713,th1225320576,172.31.7.158,56116,2022-08-05 06:32:41 IST,0,con7,cmd1,seg0,,dx4,,sx1,LOG,0,statement:,,,,,,,0,,postgres.c,1243";
		List<Event> events = new ArrayList<>();
		Event event = new org.logstash.Event();
		events.add(event);
		event.setField(ApplicationConstantTest.MESSAGE, message);
		event.setField(ApplicationConstantTest.TIMESTAMP, "2022-08-05 06:32:41.527966 IST");
		event.setField(ApplicationConstantTest.REMOTE_HOST, "172.31.7.158");
		event.setField(ApplicationConstantTest.REMOTE_PORT, "56116");
		event.setField(ApplicationConstantTest.USER_NAME, "ec2-user");
		event.setField(ApplicationConstantTest.DATABASE_NAME, "test1");
		event.setField(ApplicationConstantTest.PROCESS_ID, "p1713");
		event.setField(ApplicationConstantTest.CONNECTION_ID, "con7");
		event.setField(ApplicationConstantTest.QUERY, "");
		event.setField(ApplicationConstantTest.SERVER_HOSTNAME, "ip-172-31-7-158.ap-south-1.compute.internal");
		event.setField(ApplicationConstant.EVENT_SEVERITY, "LOG");
		Collection<Event> actaulResponse = FILTER.filter(events, matchListener);
		assertNotNull(actaulResponse);
		assertEquals(0, actaulResponse.size());
		assertEquals(0, matchListener.getMatchCount());
		assertNull(event.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));

	}

	@Test
	public void filterTest6() {

		final String message = "2022-08-05 06:32:41.527966 IST,ec2-user,test1,p1713,th1225320576,2001:0db8:85a3:0000:0000:8a2e:0370:7334,56116,2022-08-05 06:32:41 IST,0,con7,cmd1,seg0,,dx4,,sx1,LOG,0,statement: DELETE FROM products5 where prod_id = 10;,,,,,,DELETE FROM products5 where prod_id = 10;,0,,postgres.c,1243";
		List<Event> events = new ArrayList<>();
		Event event = new org.logstash.Event();
		events.add(event);
		event.setField(ApplicationConstantTest.MESSAGE, message);
		event.setField(ApplicationConstantTest.TIMESTAMP, "2022-08-05 06:32:41.527966 IST");
		event.setField(ApplicationConstantTest.REMOTE_HOST, "2001:0db8:85a3:0000:0000:8a2e:0370:7334");
		event.setField(ApplicationConstantTest.REMOTE_PORT, "56116");
		event.setField(ApplicationConstantTest.USER_NAME, "ec2-user");
		event.setField(ApplicationConstantTest.DATABASE_NAME, "test1");
		event.setField(ApplicationConstantTest.PROCESS_ID, "p1713");
		event.setField(ApplicationConstantTest.CONNECTION_ID, "con7");
		event.setField(ApplicationConstant.TRANSACTION_ID, "0");
		event.setField(ApplicationConstantTest.QUERY, "delete from products5;");
		event.setField(ApplicationConstantTest.SERVER_HOSTNAME, "ip-172-31-7-158.ap-south-1.compute.internal");
		Collection<Event> actaulResponse = FILTER.filter(events, matchListener);
		event.setField(ApplicationConstant.EVENT_SEVERITY, "LOG");
		assertNotNull(actaulResponse);
		assertEquals(1, actaulResponse.size());
		assertEquals(1, matchListener.getMatchCount());
		assertNotNull(event.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));

	}

	@Test
	public void filterTest7() {

		final String message = "2022-08-05 06:50:03.128588 UTC,\"ec2-user\",\"template1\",p2244,th-719484800,\"172.31.7.158\",\"56224\",2022-08-05 06:50:03 UTC,0,con7,cmd2,seg0,slice1,,,sx1,\"LOG\",\"0\",\"statement: select * from products;\",,,,,,\"select * from products;\",0,,\"postgres.c\",1243,";
		List<Event> events = new ArrayList<>();
		Event event = new org.logstash.Event();
		events.add(event);
		event.setField(ApplicationConstantTest.MESSAGE, message);
		event.setField(ApplicationConstantTest.TIMESTAMP, "2022-08-05 06:50:03.128588 UTC");
		event.setField(ApplicationConstantTest.REMOTE_HOST, "172.31.7.158");
		event.setField(ApplicationConstantTest.REMOTE_PORT, "56224");
		event.setField(ApplicationConstantTest.USER_NAME, "ec2-user");
		event.setField(ApplicationConstantTest.DATABASE_NAME, "template1");
		event.setField(ApplicationConstantTest.PROCESS_ID, "p1713");
		event.setField(ApplicationConstantTest.CONNECTION_ID, "con7");
		event.setField(ApplicationConstant.TRANSACTION_ID, "0");
		event.setField(ApplicationConstant.SLICE_ID, "slice1");
		event.setField(ApplicationConstantTest.QUERY, "select * from products;");
		event.setField(ApplicationConstantTest.SERVER_HOSTNAME, "ip-172-31-7-158.ap-south-1.compute.internal");
		event.setField(ApplicationConstant.EVENT_SEVERITY, "LOG");
		Collection<Event> actaulResponse = FILTER.filter(events, matchListener);
		assertNotNull(actaulResponse);
		assertEquals(1, actaulResponse.size());
		assertEquals(1, matchListener.getMatchCount());
		assertNotNull(event.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));

	}

	@Test
	public void filterTest8() {

		final String message = "2022-08-05 06:50:03.128588 UTC,\"ec2-user\",\"template1\",p2244,th-719484800,\"172.31.7.158\",\"56224\",2022-08-05 06:50:03 UTC,0,con7,cmd2,seg0,slice2,,,sx1,\"LOG\",\"0\",\"statement: select * from products;\",,,,,,\"select * from products;\",0,,\"postgres.c\",1243,";
		List<Event> events = new ArrayList<>();
		Event event = new org.logstash.Event();
		events.add(event);
		event.setField(ApplicationConstantTest.MESSAGE, message);
		event.setField(ApplicationConstantTest.TIMESTAMP, "2022-08-05 06:50:03.128588 UTC");
		event.setField(ApplicationConstantTest.REMOTE_HOST, "172.31.7.158");
		event.setField(ApplicationConstantTest.REMOTE_PORT, "56224");
		event.setField(ApplicationConstantTest.USER_NAME, "ec2-user");
		event.setField(ApplicationConstantTest.DATABASE_NAME, "template1");
		event.setField(ApplicationConstantTest.PROCESS_ID, "p1713");
		event.setField(ApplicationConstantTest.CONNECTION_ID, "con7");
		event.setField(ApplicationConstant.TRANSACTION_ID, "0");
		event.setField(ApplicationConstant.SLICE_ID, "slice2");
		event.setField(ApplicationConstantTest.QUERY, "select * from products;");
		event.setField(ApplicationConstantTest.SERVER_HOSTNAME, "ip-172-31-7-158.ap-south-1.compute.internal");
		event.setField(ApplicationConstant.EVENT_SEVERITY, "LOG");
		Collection<Event> actaulResponse = FILTER.filter(events, matchListener);
		assertNotNull(actaulResponse);
		assertEquals(0, actaulResponse.size());
		assertEquals(0, matchListener.getMatchCount());
		assertNull(event.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));

	}

	@Test
	public void filterTest9() {

		final String message = "2022-08-05 06:32:41.527966 UTC,\"ec2-user\",\"template1\",p1713,th1225320576,\"[local]\",,2022-08-05 06:32:41 UTC,0,con7,cmd1,seg0,,dx4,,sx1,\"LOG\",\"0\",\"statement: CREATE TABLE products5(name varchar(40),prod_id integer,supplier_id integer)DISTRIBUTED BY (prod_id);\",,,,,,\"CREATE TABLE products5(name varchar(40),prod_id integer,supplier_id integer)DISTRIBUTED BY (prod_id);\",0,,\"postgres.c\",1243";
		List<Event> events = new ArrayList<>();
		Event event = new org.logstash.Event();
		events.add(event);
		event.setField(ApplicationConstantTest.MESSAGE, message);
		event.setField(ApplicationConstantTest.TIMESTAMP, "2022-08-05 06:32:41.527966 UTC");
		event.setField(ApplicationConstant.SERVER_IP, "172.31.7.160");
		event.setField(ApplicationConstantTest.REMOTE_HOST, "[local]");
		event.setField(ApplicationConstantTest.USER_NAME, "ec2-user");
		event.setField(ApplicationConstantTest.DATABASE_NAME, "template1");
		event.setField(ApplicationConstantTest.PROCESS_ID, "p1713");
		event.setField(ApplicationConstantTest.CONNECTION_ID, "con7");
		event.setField(ApplicationConstant.TRANSACTION_ID, "0");
		event.setField(ApplicationConstantTest.QUERY,
				"CREATE TABLE products5(name varchar(40),prod_id integer,supplier_id integer)DISTRIBUTED BY (prod_id);");
		event.setField(ApplicationConstantTest.SERVER_HOSTNAME, "ip-172-31-7-158.ap-south-1.compute.internal");
		event.setField(ApplicationConstant.EVENT_SEVERITY, "LOG");
		Collection<Event> actaulResponse = FILTER.filter(events, matchListener);
		assertNotNull(actaulResponse);
		assertEquals(1, actaulResponse.size());
		assertEquals(1, matchListener.getMatchCount());
		assertNotNull(event.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));

	}

	@Test
	public void filterTest10() {

		final String message = "2022-08-05 06:32:41.527966 UTC,\"ec2-user\",\"template1\",p1713,th1225320576,\"172.31.7.158\",\"56224\",2022-08-05 06:32:41 UTC,0,con7,cmd1,seg0,,dx4,,sx1,\"LOG\",\"0\",\"statement: CREATE TABLE products5(name varchar(40),prod_id integer,supplier_id integer)DISTRIBUTED BY (prod_id);\",,,,,,\"CREATE TABLE products5(name varchar(40),prod_id integer,supplier_id integer)DISTRIBUTED BY (prod_id);\",0,,\"postgres.c\",1243";
		List<Event> events = new ArrayList<>();
		Event event = new org.logstash.Event();
		events.add(event);
		event.setField(ApplicationConstantTest.MESSAGE, message);
		event.setField(ApplicationConstantTest.TIMESTAMP, "2022-08-05 06:32:41.527966 UTC");
		event.setField(ApplicationConstant.REMOTE_PORT, "\"56224\"");
		event.setField(ApplicationConstant.SERVER_IP, "172.31.7.160");
		event.setField(ApplicationConstantTest.REMOTE_HOST, "172.31.7.158");
		event.setField(ApplicationConstantTest.USER_NAME, "ec2-user");
		event.setField(ApplicationConstantTest.DATABASE_NAME, "template1");
		event.setField(ApplicationConstantTest.PROCESS_ID, "p1713");
		event.setField(ApplicationConstantTest.CONNECTION_ID, "con7");
		event.setField(ApplicationConstant.TRANSACTION_ID, "0");
		event.setField(ApplicationConstantTest.QUERY,
				"CREATE TABLE products5(name varchar(40),prod_id integer,supplier_id integer)DISTRIBUTED BY (prod_id);");
		event.setField(ApplicationConstantTest.SERVER_HOSTNAME, "ip-172-31-7-158.ap-south-1.compute.internal");
		event.setField(ApplicationConstant.EVENT_SEVERITY, "LOG");
		Collection<Event> actaulResponse = FILTER.filter(events, matchListener);
		assertNotNull(actaulResponse);
		assertEquals(1, actaulResponse.size());
		assertEquals(1, matchListener.getMatchCount());
		assertNotNull(event.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));

	}

	@Test
	public void filterTest11() {

		final String message = "2022-08-05 06:32:41.527966 UTC,\"ec2-user\",\"template1\",p1713,th1225320576,\"172.31.7.158\",\"56224\",2022-08-05 06:32:41 UTC,0,con7,cmd1,seg0,,dx4,,sx1,\"LOG\",\"0\",\"statement: create database \"\"db_cricket\"\";\",,,,,,\"create database \"\"db_cricket\"\"\",0,,\"postgres.c\",1243";
		List<Event> events = new ArrayList<>();
		Event event = new org.logstash.Event();
		events.add(event);
		event.setField(ApplicationConstantTest.MESSAGE, message);
		event.setField(ApplicationConstantTest.TIMESTAMP, "2022-08-05 06:32:41.527966 UTC");
		event.setField(ApplicationConstant.REMOTE_PORT, "\"56224\"");
		event.setField(ApplicationConstant.SERVER_IP, "172.31.7.160");
		event.setField(ApplicationConstantTest.REMOTE_HOST, "172.31.7.158");
		event.setField(ApplicationConstantTest.USER_NAME, "ec2-user");
		event.setField(ApplicationConstantTest.DATABASE_NAME, "template1");
		event.setField(ApplicationConstantTest.PROCESS_ID, "p1713");
		event.setField(ApplicationConstantTest.CONNECTION_ID, "con7");
		event.setField(ApplicationConstant.TRANSACTION_ID, "0");
		event.setField(ApplicationConstantTest.QUERY, "\"create database \"\"db_cricket\"\";\"");
		event.setField(ApplicationConstantTest.SERVER_HOSTNAME, "ip-172-31-7-158.ap-south-1.compute.internal");
		event.setField(ApplicationConstant.EVENT_SEVERITY, "LOG");
		Collection<Event> actaulResponse = FILTER.filter(events, matchListener);
		assertNotNull(actaulResponse);
		assertEquals(1, actaulResponse.size());
		assertEquals(1, matchListener.getMatchCount());
		assertNotNull(event.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));

	}

	@Test
	public void filterAuthorizationExceptionTest() {

		final String message = "2022-08-29 08:19:13.744998 UTC,\"ec2-user\",\"automation_testing\",p1646,th893405312,\"172.31.5.213\",\"38380\",2022-08-29 08:19:13 UTC,916,con12,cmd2,seg0,,dx6,x916,sx1,\"LOG\",\"23505\",\"duplicate key value violates unique constraint \"\"emp_pkey\"\"\",\"Key (id)=(B6717) already exists.\",,,,,\"insert into emp values('B6717', 'Adinarayabna', 'Potta', 200110, '2019-12-10','test');\r\n"
				+ "update emp set surname = 'Adinarayana' where surname = 'Adinarayabna';\",0,,\"elog.c\",500,";
		List<Event> events = new ArrayList<>();
		Event event = new org.logstash.Event();
		events.add(event);
		event.setField(ApplicationConstantTest.MESSAGE, message);
		event.setField(ApplicationConstantTest.TIMESTAMP, "2022-08-29 08:19:13.744998 UTC");
		event.setField(ApplicationConstantTest.REMOTE_HOST, "172.31.5.213");
		event.setField(ApplicationConstantTest.REMOTE_PORT, "38380");
		event.setField(ApplicationConstantTest.USER_NAME, "ec2-user");
		event.setField(ApplicationConstantTest.DATABASE_NAME, "automation_testing");
		event.setField(ApplicationConstantTest.PROCESS_ID, "p1646");
		event.setField(ApplicationConstantTest.CONNECTION_ID, "con12");
		event.setField(ApplicationConstantTest.QUERY,
				"insert into emp values('B6717', 'Adinarayabna', 'Potta', 200110, '2019-12-10','test');\r\n"
						+ "update emp set surname = 'Adinarayana' where surname = 'Adinarayabna';");
		event.setField(ApplicationConstantTest.SERVER_HOSTNAME, "ip-172-31-7-158.ap-south-1.compute.internal");
		event.setField(ApplicationConstant.EVENT_SEVERITY, "LOG");
		event.setField(ApplicationConstant.FILE_NAME, "elog.c");
		event.setField(ApplicationConstant.STATE_CODE, "23505");
		event.setField(ApplicationConstant.EVENT_MESSAGE,
				"duplicate key value violates unique constraint \"emp_pkey\",\"Key (id)=(B6717) already exists.");
		Collection<Event> actaulResponse = FILTER.filter(events, matchListener);
		assertNotNull(actaulResponse);
		assertEquals(1, actaulResponse.size());
		assertEquals(1, matchListener.getMatchCount());
		assertNotNull(event.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));

	}

	@Test
	public void filterAuthenticationExceptionTest() {

		final String message = "2022-08-29 08:19:13.744998 UTC,\"ec2-user\",\"automation_testing\",p1646,th893405312,\"172.31.5.213\",\"38380\",2022-08-29 08:19:13 UTC,916,con12,cmd2,seg0,,dx6,x916,sx1,\"LOG\",\"23505\",\"no pg_hba.conf entry for host \"[local]\", user \"ankur1500\", database \"testdb\", SSL off\",\"Key (id)=(B6717) already exists.\",,,,,,0,,\"auth.c\",500,";
		List<Event> events = new ArrayList<>();
		Event event = new org.logstash.Event();
		events.add(event);
		event.setField(ApplicationConstantTest.MESSAGE, message);
		event.setField(ApplicationConstantTest.TIMESTAMP, "2022-08-29 08:19:13.744998 UTC");
		event.setField(ApplicationConstantTest.REMOTE_HOST, "172.31.5.213");
		event.setField(ApplicationConstantTest.REMOTE_PORT, "38380");
		event.setField(ApplicationConstantTest.USER_NAME, "ec2-user");
		event.setField(ApplicationConstantTest.DATABASE_NAME, "automation_testing");
		event.setField(ApplicationConstantTest.PROCESS_ID, "p1646");
		event.setField(ApplicationConstantTest.CONNECTION_ID, "con12");
		event.setField(ApplicationConstantTest.QUERY, "");
		event.setField(ApplicationConstantTest.SERVER_HOSTNAME, "ip-172-31-7-158.ap-south-1.compute.internal");
		event.setField(ApplicationConstant.EVENT_SEVERITY, "LOG");
		event.setField(ApplicationConstant.FILE_NAME, "auth.c");
		event.setField(ApplicationConstant.STATE_CODE, "23505");
		event.setField(ApplicationConstant.EVENT_MESSAGE,
				"duplicate key value violates unique constraint \"emp_pkey\",\"Key (id)=(B6717) already exists.");
		Collection<Event> actaulResponse = FILTER.filter(events, matchListener);
		assertNotNull(actaulResponse);
		assertEquals(1, actaulResponse.size());
		assertEquals(1, matchListener.getMatchCount());
		assertNotNull(event.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));

	}

}

class TestMatchListener implements FilterMatchListener {

	private AtomicInteger matchCount = new AtomicInteger(0);

	@Override
	public void filterMatched(Event event) {
		matchCount.incrementAndGet();
	}

	public int getMatchCount() {
		return matchCount.get();
	}
}
