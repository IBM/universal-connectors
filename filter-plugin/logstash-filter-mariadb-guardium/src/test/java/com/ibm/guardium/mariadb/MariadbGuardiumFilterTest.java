/*
© Copyright IBM Corp. 2021, 2022 All rights reserved.
SPDX-License-Identifier: Apache-2.0
*/

package com.ibm.guardium.mariadb;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.logstash.plugins.ConfigurationImpl;

import co.elastic.logstash.api.Configuration;
import co.elastic.logstash.api.Event;
import co.elastic.logstash.api.FilterMatchListener;

public class MariadbGuardiumFilterTest {
	FilterMatchListener matchListener = new TestMatchListener();
	Configuration config = new ConfigurationImpl(
			Collections.singletonMap(ApplicationConstantTest.CONFIG_KEY, ApplicationConstantTest.CONFIG_VALUE));
	MariadbGuardiumFilter mariadbGuardiumFilter = new MariadbGuardiumFilter(ApplicationConstantTest.ID, config, null);

	@Test
	public void filterTest() {
		Event event = new org.logstash.Event();
		event.setField(ApplicationConstantTest.LOG_MESSAGE_KEY, ApplicationConstantTest.LOG_MESSAGE);
		event.setField(ApplicationConstantTest.CONNECTIONID_KEY, ApplicationConstantTest.CONNECTIONID_VALUE);
		event.setField(ApplicationConstantTest.USERNAME_KEY, ApplicationConstantTest.USERNAME_VALUE);
		event.setField(ApplicationConstantTest.DATABASE_KEY, ApplicationConstantTest.DATABASE_VALUE);
		event.setField(ApplicationConstantTest.RETCODE_KEY, ApplicationConstantTest.RETCODE_VALUE);
		event.setField(ApplicationConstantTest.OBJECT_KEY, ApplicationConstantTest.OBJECT_VALUE);
		event.setField(ApplicationConstantTest.HOSTNAME_KEY, ApplicationConstantTest.HOSTNAME_VALUE);
		event.setField(ApplicationConstantTest.SERVERHOST_KEY, ApplicationConstantTest.SERVERHOST_VALUE);
		event.setField(ApplicationConstantTest.OPERATION_KEY, ApplicationConstantTest.OPERATION_VALUE);
		event.setField("timestamp", "20220107 09:11:51");
		Collection<Event> actualResponse = mariadbGuardiumFilter.filter(Collections.singletonList(event),
				matchListener);
		assertNotNull(actualResponse);
		assertEquals(ApplicationConstantTest.CONNECTIONID_VALUE, actualResponse.toArray(new org.logstash.Event[] {})[0]
				.getField(ApplicationConstantTest.CONNECTIONID_KEY));
		assertEquals(ApplicationConstantTest.SERVERHOST_VALUE, actualResponse.toArray(new org.logstash.Event[] {})[0]
				.getField(ApplicationConstantTest.SERVERHOST_KEY));
		assertEquals(ApplicationConstantTest.USERNAME_VALUE,
				actualResponse.toArray(new org.logstash.Event[] {})[0].getField(ApplicationConstantTest.USERNAME_KEY));
		assertEquals(ApplicationConstantTest.HOSTNAME_VALUE,
				actualResponse.toArray(new org.logstash.Event[] {})[0].getField(ApplicationConstantTest.HOSTNAME_KEY));
		assertEquals(ApplicationConstantTest.DATABASE_VALUE,
				actualResponse.toArray(new org.logstash.Event[] {})[0].getField(ApplicationConstantTest.DATABASE_KEY));
		assertEquals(ApplicationConstantTest.RETCODE_VALUE,
				actualResponse.toArray(new org.logstash.Event[] {})[0].getField(ApplicationConstantTest.RETCODE_KEY));
		assertEquals(ApplicationConstantTest.OBJECT_VALUE,
				actualResponse.toArray(new org.logstash.Event[] {})[0].getField(ApplicationConstantTest.OBJECT_KEY));

	}

	@Test
	public void CONNECTIONEVENTTest() {
		final String auditString = "20220204 15:47:44,LP-5CD1184J8J,root,localhost,13,0,CONNECT,,,0";
		Event event = new org.logstash.Event();
		ArrayList<Event> list = new ArrayList<>();
		list.add(event);
		event.setField(ApplicationConstantTest.LOG_MESSAGE_KEY, auditString);
		event.setField(ApplicationConstantTest.CONNECTIONID_KEY, "13");
		event.setField(ApplicationConstantTest.USERNAME_KEY, "root");
		event.setField(ApplicationConstantTest.DATABASE_KEY, "");
		event.setField(ApplicationConstantTest.RETCODE_KEY, "0");
		event.setField(ApplicationConstantTest.OBJECT_KEY, "");
		event.setField(ApplicationConstantTest.HOSTNAME_KEY, "localhost");
		event.setField(ApplicationConstantTest.SERVERHOST_KEY, "LP-5CD1184J8J");
		event.setField(ApplicationConstantTest.OPERATION_KEY, "CONNECT");
		event.setField("timestamp", "20220204 15:47:44");
		Collection<Event> actualResponse = mariadbGuardiumFilter.filter(list, matchListener);
		assertNotNull(actualResponse);
	}

	@Test
	public void QUERYEVENTTest() {
		final String auditString = "20220204 15:47:53,LP-5CD1184J8J,root,localhost,13,66,QUERY,,'select * from maths_report',1046";
		Event event = new org.logstash.Event();
		ArrayList<Event> list = new ArrayList<>();
		list.add(event);
		event.setField(ApplicationConstantTest.LOG_MESSAGE_KEY, auditString);
		event.setField(ApplicationConstantTest.CONNECTIONID_KEY, "13");
		event.setField(ApplicationConstantTest.USERNAME_KEY, "root");
		event.setField(ApplicationConstantTest.DATABASE_KEY, "");
		event.setField(ApplicationConstantTest.RETCODE_KEY, "1046");
		event.setField(ApplicationConstantTest.OBJECT_KEY, "'select * from maths_report'");
		event.setField(ApplicationConstantTest.HOSTNAME_KEY, "localhost");
		event.setField(ApplicationConstantTest.SERVERHOST_KEY, "LP-5CD1184J8J");
		event.setField(ApplicationConstantTest.OPERATION_KEY, "QUERY");
		event.setField("timestamp", "20220204 15:47:53");
		Collection<Event> actualResponse = mariadbGuardiumFilter.filter(list, matchListener);
		assertNotNull(actualResponse);
	}

	@Test
	public void TABLEEVENTTest() {
		final String auditString = "20220204 13:49:42,LP-5CD1184J8J,root,localhost,5,25,READ,auditlog,total***REMOVED***le,";
		Event event = new org.logstash.Event();
		ArrayList<Event> list = new ArrayList<>();
		list.add(event);
		event.setField(ApplicationConstantTest.LOG_MESSAGE_KEY, auditString);
		event.setField(ApplicationConstantTest.CONNECTIONID_KEY, "5");
		event.setField(ApplicationConstantTest.USERNAME_KEY, "root");
		event.setField(ApplicationConstantTest.DATABASE_KEY, "auditlog");
		event.setField(ApplicationConstantTest.RETCODE_KEY, "");
		event.setField(ApplicationConstantTest.OBJECT_KEY, "total***REMOVED***le");
		event.setField(ApplicationConstantTest.HOSTNAME_KEY, "localhost");
		event.setField(ApplicationConstantTest.SERVERHOST_KEY, "LP-5CD1184J8J");
		event.setField(ApplicationConstantTest.OPERATION_KEY, "READ");
		event.setField("timestamp", "20220204 13:49:42");
		Collection<Event> actualResponse = mariadbGuardiumFilter.filter(list, matchListener);
		assertNotNull(actualResponse);

	}

	@Test
	public void QUERYEVENTTest1() {
		final String auditString = "20220204 14:00:47,LP-5CD1184J8J,root,localhost,7,33,QUERY,,'create database maxcMG',0";
		Event event = new org.logstash.Event();
		ArrayList<Event> list = new ArrayList<>();
		list.add(event);
		event.setField(ApplicationConstantTest.LOG_MESSAGE_KEY, auditString);
		event.setField(ApplicationConstantTest.CONNECTIONID_KEY, "7");
		event.setField(ApplicationConstantTest.USERNAME_KEY, "root");
		event.setField(ApplicationConstantTest.DATABASE_KEY, "");
		event.setField(ApplicationConstantTest.RETCODE_KEY, "0");
		event.setField(ApplicationConstantTest.OBJECT_KEY, "'create database maxcMG'");
		event.setField(ApplicationConstantTest.HOSTNAME_KEY, "localhost");
		event.setField(ApplicationConstantTest.SERVERHOST_KEY, "LP-5CD1184J8J");
		event.setField(ApplicationConstantTest.OPERATION_KEY, "QUERY");
		event.setField("timestamp", "20220204 14:00:47");
		Collection<Event> actualResponse = mariadbGuardiumFilter.filter(list, matchListener);
		assertNotNull(actualResponse);
	}

	@Test
	public void QUERYEVENTTest2() {
		final String auditString = "20220201 14:05:38,LP-5CD1184J8J,root,localhost,4,15,QUERY,auditlog,'insert into auditlog.total***REMOVED***le(SalepersonName,SoldData,ProductName)values(\\'ash\\',CURRENT_DATE(),\\'MariaDBvmm\\')',0";
		Event event = new org.logstash.Event();
		ArrayList<Event> list = new ArrayList<>();
		list.add(event);
		event.setField(ApplicationConstantTest.LOG_MESSAGE_KEY, auditString);
		event.setField(ApplicationConstantTest.CONNECTIONID_KEY, "4");
		event.setField(ApplicationConstantTest.USERNAME_KEY, "root");
		event.setField(ApplicationConstantTest.DATABASE_KEY, "auditlog");
		event.setField(ApplicationConstantTest.RETCODE_KEY, "0");
		event.setField(ApplicationConstantTest.OBJECT_KEY,
				"'insert into auditlog.total***REMOVED***le(SalepersonName,SoldData,ProductName)values(\\\\'ash\\\\',CURRENT_DATE(),\\\\'MariaDBvmm\\\\')'");
		event.setField(ApplicationConstantTest.HOSTNAME_KEY, "localhost");
		event.setField(ApplicationConstantTest.SERVERHOST_KEY, "LP-5CD1184J8J");
		event.setField(ApplicationConstantTest.OPERATION_KEY, "QUERY");
		event.setField("timestamp", "20220201 14:05:38");
		Collection<Event> actualResponse = mariadbGuardiumFilter.filter(list, matchListener);
		assertNotNull(actualResponse);

	}

	@Test
	public void QUERYEVENTTest3() {
		final String auditString = "20220201 14:06:32,LP-5CD1184J8J,root,localhost,4,16,QUERY,auditlog,'insert into auditlog.total***REMOVED***le(SalepersonName,SoldData,ProductName)values(\"ashr\",CURRENT_DATE(),\"MariaDBvmms\")',0";
		Event event = new org.logstash.Event();
		ArrayList<Event> list = new ArrayList<>();
		list.add(event);
		event.setField(ApplicationConstantTest.LOG_MESSAGE_KEY, auditString);
		event.setField(ApplicationConstantTest.CONNECTIONID_KEY, "4");
		event.setField(ApplicationConstantTest.USERNAME_KEY, "root");
		event.setField(ApplicationConstantTest.DATABASE_KEY, "auditlog");
		event.setField(ApplicationConstantTest.RETCODE_KEY, "0");
		event.setField(ApplicationConstantTest.OBJECT_KEY,
				"'insert into auditlog.total***REMOVED***le(SalepersonName,SoldData,ProductName)values(\\\"ashr\\\",CURRENT_DATE(),\\\"MariaDBvmms\\\")'");
		event.setField(ApplicationConstantTest.HOSTNAME_KEY, "localhost");
		event.setField(ApplicationConstantTest.SERVERHOST_KEY, "LP-5CD1184J8J");
		event.setField(ApplicationConstantTest.OPERATION_KEY, "QUERY");
		event.setField("timestamp", "20220201 14:06:32");
		Collection<Event> actualResponse = mariadbGuardiumFilter.filter(list, matchListener);
		assertNotNull(actualResponse);

	}

	@Test
	public void QUERYEVENTTest4() {
		final String auditString = "20220201 14:13:17,LP-5CD1184J8J,root,localhost,4,17,QUERY,auditlog,'create databse testdb',1064";
		Event event = new org.logstash.Event();
		ArrayList<Event> list = new ArrayList<>();
		list.add(event);
		event.setField(ApplicationConstantTest.LOG_MESSAGE_KEY, auditString);
		event.setField(ApplicationConstantTest.CONNECTIONID_KEY, "4");
		event.setField(ApplicationConstantTest.USERNAME_KEY, "root");
		event.setField(ApplicationConstantTest.DATABASE_KEY, "auditlog");
		event.setField(ApplicationConstantTest.RETCODE_KEY, "1064");
		event.setField(ApplicationConstantTest.OBJECT_KEY, "'create databse testdb'");
		event.setField(ApplicationConstantTest.HOSTNAME_KEY, "localhost");
		event.setField(ApplicationConstantTest.SERVERHOST_KEY, "LP-5CD1184J8J");
		event.setField(ApplicationConstantTest.OPERATION_KEY, "QUERY");
		event.setField("timestamp", "20220201 14:13:17");
		Collection<Event> actualResponse = mariadbGuardiumFilter.filter(list, matchListener);
		assertNotNull(actualResponse);
	}

	@Test
	public void QUERYEVENTTest5() {
		final String auditString = "20220201 22:50:44,LP-5CD1184J8J,root,localhost,5,27,QUERY,auditlog,'SELECT DATABASE()',0";
		Event event = new org.logstash.Event();
		ArrayList<Event> list = new ArrayList<>();
		list.add(event);
		event.setField(ApplicationConstantTest.LOG_MESSAGE_KEY, auditString);
		event.setField(ApplicationConstantTest.CONNECTIONID_KEY, "5");
		event.setField(ApplicationConstantTest.USERNAME_KEY, "root");
		event.setField(ApplicationConstantTest.DATABASE_KEY, "auditlog");
		event.setField(ApplicationConstantTest.RETCODE_KEY, "0");
		event.setField(ApplicationConstantTest.OBJECT_KEY, "'SELECT DATABASE()'");
		event.setField(ApplicationConstantTest.HOSTNAME_KEY, "localhost");
		event.setField(ApplicationConstantTest.SERVERHOST_KEY, "LP-5CD1184J8J");
		event.setField(ApplicationConstantTest.OPERATION_KEY, "QUERY");
		event.setField("timestamp", "20220201 22:50:44");
		Collection<Event> actualResponse = mariadbGuardiumFilter.filter(list, matchListener);
		assertNotNull(actualResponse);
	}

	@Test
	public void QUERYEVENTTest6() {
		final String auditString = "20220201 22:57:00,LP-5CD1184J8J,root,localhost,5,29,QUERY,auditlog,'show databases',0";
		Event event = new org.logstash.Event();
		ArrayList<Event> list = new ArrayList<>();
		list.add(event);
		event.setField(ApplicationConstantTest.LOG_MESSAGE_KEY, auditString);
		event.setField(ApplicationConstantTest.CONNECTIONID_KEY, "5");
		event.setField(ApplicationConstantTest.USERNAME_KEY, "root");
		event.setField(ApplicationConstantTest.DATABASE_KEY, "auditlog");
		event.setField(ApplicationConstantTest.RETCODE_KEY, "0");
		event.setField(ApplicationConstantTest.OBJECT_KEY, "'show databases'");
		event.setField(ApplicationConstantTest.HOSTNAME_KEY, "localhost");
		event.setField(ApplicationConstantTest.SERVERHOST_KEY, "LP-5CD1184J8J");
		event.setField(ApplicationConstantTest.OPERATION_KEY, "QUERY");
		event.setField("timestamp", "20220201 22:57:00");
		Collection<Event> actualResponse = mariadbGuardiumFilter.filter(list, matchListener);
		assertNotNull(actualResponse);
	}

	@Test
	public void DISCONNECTEVENTTest() {
		final String auditString = "20220201 23:00:16,LP-5CD1184J8J,root,localhost,5,0,DISCONNECT,auditlog,,0";
		Event event = new org.logstash.Event();
		ArrayList<Event> list = new ArrayList<>();
		list.add(event);
		event.setField(ApplicationConstantTest.LOG_MESSAGE_KEY, auditString);
		event.setField(ApplicationConstantTest.CONNECTIONID_KEY, "5");
		event.setField(ApplicationConstantTest.USERNAME_KEY, "root");
		event.setField(ApplicationConstantTest.DATABASE_KEY, "auditlog");
		event.setField(ApplicationConstantTest.RETCODE_KEY, "0");
		event.setField(ApplicationConstantTest.OBJECT_KEY, "");
		event.setField(ApplicationConstantTest.HOSTNAME_KEY, "localhost");
		event.setField(ApplicationConstantTest.SERVERHOST_KEY, "LP-5CD1184J8J");
		event.setField(ApplicationConstantTest.OPERATION_KEY, "DISCONNECT");
		event.setField("timestamp", "20220201 23:00:16");
		Collection<Event> actualResponse = mariadbGuardiumFilter.filter(list, matchListener);
		assertNotNull(actualResponse);
	}

	@Test
	public void QUERYEVENTTest7() {
		final String auditString = "20220202 11:01:13,LP-5CD1184J8J,root,localhost,6,34,QUERY,,'show global variables like \\'server-audit%\\'',0";
		Event event = new org.logstash.Event();
		ArrayList<Event> list = new ArrayList<>();
		list.add(event);
		event.setField(ApplicationConstantTest.LOG_MESSAGE_KEY, auditString);
		event.setField(ApplicationConstantTest.CONNECTIONID_KEY, "5");
		event.setField(ApplicationConstantTest.USERNAME_KEY, "root");
		event.setField(ApplicationConstantTest.DATABASE_KEY, "");
		event.setField(ApplicationConstantTest.RETCODE_KEY, "0");
		event.setField(ApplicationConstantTest.OBJECT_KEY, "'show global variables like \\\\'server-audit%\\\\''");
		event.setField(ApplicationConstantTest.HOSTNAME_KEY, "localhost");
		event.setField(ApplicationConstantTest.SERVERHOST_KEY, "LP-5CD1184J8J");
		event.setField(ApplicationConstantTest.OPERATION_KEY, "QUERY");
		event.setField("timestamp", "20220202 11:01:13");
		Collection<Event> actualResponse = mariadbGuardiumFilter.filter(list, matchListener);
		assertNotNull(actualResponse);
	}

	@Test
	public void LOGIN_FAILEDTest() {
		final String auditString = "20220204 15:47:06,LP-5CD1184J8J,root,localhost,11,0,FAILED_CONNECT,,,1045";
		Event event = new org.logstash.Event();
		ArrayList<Event> list = new ArrayList<>();
		list.add(event);
		event.setField(ApplicationConstantTest.LOG_MESSAGE_KEY, auditString);
		event.setField(ApplicationConstantTest.CONNECTIONID_KEY, "11");
		event.setField(ApplicationConstantTest.USERNAME_KEY, "root");
		event.setField(ApplicationConstantTest.DATABASE_KEY, "");
		event.setField(ApplicationConstantTest.RETCODE_KEY, "1045");
		event.setField(ApplicationConstantTest.OBJECT_KEY, "");
		event.setField(ApplicationConstantTest.HOSTNAME_KEY, "localhost");
		event.setField(ApplicationConstantTest.SERVERHOST_KEY, "LP-5CD1184J8J");
		event.setField(ApplicationConstantTest.OPERATION_KEY, "FAILED_CONNECT");
		event.setField("timestamp", "20220204 15:47:06");
		Collection<Event> actualResponse = mariadbGuardiumFilter.filter(list, matchListener);
		assertNotNull(actualResponse);
	}

	@Test
	public void CREATE_USERTest() {
		final String auditString = "20220329 18:38:42,LP-5CD1184J8J,root,localhost,6,9,QUERY,mysql,'CREATE   USER \\'myusersha\\'@\\'localhost\\' IDENTIFIED BY *****',0";
		Event event = new org.logstash.Event();
		ArrayList<Event> list = new ArrayList<>();
		list.add(event);
		event.setField(ApplicationConstantTest.LOG_MESSAGE_KEY, auditString);
		event.setField(ApplicationConstantTest.CONNECTIONID_KEY, "6");
		event.setField(ApplicationConstantTest.USERNAME_KEY, "root");
		event.setField(ApplicationConstantTest.DATABASE_KEY, "mysql");
		event.setField(ApplicationConstantTest.RETCODE_KEY, "0");
		event.setField(ApplicationConstantTest.OBJECT_KEY,
				"'CREATE   USER \\\\'myusersha\\\\'@\\\\'localhost\\\\' IDENTIFIED BY *****'");
		event.setField(ApplicationConstantTest.HOSTNAME_KEY, "localhost");
		event.setField(ApplicationConstantTest.SERVERHOST_KEY, "LP-5CD1184J8J");
		event.setField(ApplicationConstantTest.OPERATION_KEY, "QUERY");
		event.setField("timestamp", "20220329 18:38:42");
		Collection<Event> actualResponse = mariadbGuardiumFilter.filter(list, matchListener);
		assertNotNull(actualResponse);
	}

	@Test
	public void CREATE_FUNCTIONTest() {
		final String auditString = "20220430 10:37:42,LP-5CD1184J8J,root,localhost,8,106,QUERY,mariadb,'CREATE FUNCTION counter () RETURNS INT\\n  BEGIN\\n    UPDATE counter SET c = c + 1;\\n    RETURN (SELECT c FROM counter LIMIT 1);\\n  END',0";
		Event event = new org.logstash.Event();
		ArrayList<Event> list = new ArrayList<>();
		list.add(event);
		event.setField(ApplicationConstantTest.LOG_MESSAGE_KEY, auditString);
		event.setField(ApplicationConstantTest.CONNECTIONID_KEY, "8");
		event.setField(ApplicationConstantTest.USERNAME_KEY, "root");
		event.setField(ApplicationConstantTest.DATABASE_KEY, "mariadb");
		event.setField(ApplicationConstantTest.RETCODE_KEY, "0");
		event.setField(ApplicationConstantTest.OBJECT_KEY,
				"'CREATE FUNCTION counter () RETURNS INT\\n  BEGIN\\n    UPDATE counter SET c = c + 1;\\n    RETURN (SELECT c FROM counter LIMIT 1);\\n  END'");
		event.setField(ApplicationConstantTest.HOSTNAME_KEY, "localhost");
		event.setField(ApplicationConstantTest.SERVERHOST_KEY, "LP-5CD1184J8J");
		event.setField(ApplicationConstantTest.OPERATION_KEY, "QUERY");
		event.setField("timestamp", "20220430 10:37:42");
		Collection<Event> actualResponse = mariadbGuardiumFilter.filter(list, matchListener);
		assertNotNull(actualResponse);
	}

	@Test
	public void JSON_QUERYest() {
		final String auditString = "20220430 09:01:20,LP-5CD1184J8J,root,localhost,8,89,QUERY,mariadb,'SET @json=\\'{\\n            \"A\": [0,\\n                  [1, 2, 3],\\n                  [4, 5, 6],\\n                  \"seven\",\\n                   0.8,\\n                   true,\\n                   false,\\n                   \"eleven\",\\n                  [12, [13, 14], {\"key1\":\"value1\"},[15]],\\n                  true],\\n            \"B\": {\"C\": 1},\\n            \"D\": 2\\n           }\\'',0";
		Event event = new org.logstash.Event();
		ArrayList<Event> list = new ArrayList<>();
		list.add(event);
		event.setField(ApplicationConstantTest.LOG_MESSAGE_KEY, auditString);
		event.setField(ApplicationConstantTest.CONNECTIONID_KEY, "8");
		event.setField(ApplicationConstantTest.USERNAME_KEY, "root");
		event.setField(ApplicationConstantTest.DATABASE_KEY, "mariadb");
		event.setField(ApplicationConstantTest.RETCODE_KEY, "0");
		event.setField(ApplicationConstantTest.OBJECT_KEY,
				"'SET @json=\\\\'{\\\\n            \\\"A\\\": [0,\\\\n                  [1, 2, 3],\\\\n                  [4, 5, 6],\\\\n                  \\\"seven\\\",\\\\n                   0.8,\\\\n                   true,\\\\n                   false,\\\\n                   \\\"eleven\\\",\\\\n                  [12, [13, 14], {\\\"key1\\\":\\\"value1\\\"},[15]],\\\\n                  true],\\\\n            \\\"B\\\": {\\\"C\\\": 1},\\\\n            \\\"D\\\": 2\\\\n           }\\\\''");
		event.setField(ApplicationConstantTest.HOSTNAME_KEY, "localhost");
		event.setField(ApplicationConstantTest.SERVERHOST_KEY, "LP-5CD1184J8J");
		event.setField(ApplicationConstantTest.OPERATION_KEY, "QUERY");
		event.setField("timestamp", "20220430 09:01:20");
		Collection<Event> actualResponse = mariadbGuardiumFilter.filter(list, matchListener);
		assertNotNull(actualResponse);
	}

	@Test
	public void JSON_EXTRACTest() {
		final String auditString = "20220430 09:01:34,LP-5CD1184J8J,root,localhost,8,90,QUERY,mariadb,'SELECT JSON_EXTRACT(@json, \\'$.A[-8][1]\\')',0";
		Event event = new org.logstash.Event();
		ArrayList<Event> list = new ArrayList<>();
		list.add(event);
		event.setField(ApplicationConstantTest.LOG_MESSAGE_KEY, auditString);
		event.setField(ApplicationConstantTest.CONNECTIONID_KEY, "8");
		event.setField(ApplicationConstantTest.USERNAME_KEY, "root");
		event.setField(ApplicationConstantTest.DATABASE_KEY, "mariadb");
		event.setField(ApplicationConstantTest.RETCODE_KEY, "0");
		event.setField(ApplicationConstantTest.OBJECT_KEY, "'SELECT JSON_EXTRACT(@json, \\'$.A[-8][1]\\')'");
		event.setField(ApplicationConstantTest.HOSTNAME_KEY, "localhost");
		event.setField(ApplicationConstantTest.SERVERHOST_KEY, "LP-5CD1184J8J");
		event.setField(ApplicationConstantTest.OPERATION_KEY, "QUERY");
		event.setField("timestamp", "20220430 09:01:34");
		Collection<Event> actualResponse = mariadbGuardiumFilter.filter(list, matchListener);
		assertNotNull(actualResponse);
	}

	@Test
	public void GROUP_BYTest() {
		final String auditString = "20220430 18:20:35,LP-5CD1184J8J,root,localhost,12,179,QUERY,mariadb,'select product_id, sum(retail_price * quantity) as gross_revenue\\n  from ***REMOVED***les\\n  group by product_id',0";
		Event event = new org.logstash.Event();
		ArrayList<Event> list = new ArrayList<>();
		list.add(event);
		event.setField(ApplicationConstantTest.LOG_MESSAGE_KEY, auditString);
		event.setField(ApplicationConstantTest.CONNECTIONID_KEY, "12");
		event.setField(ApplicationConstantTest.USERNAME_KEY, "root");
		event.setField(ApplicationConstantTest.DATABASE_KEY, "mariadb");
		event.setField(ApplicationConstantTest.RETCODE_KEY, "0");
		event.setField(ApplicationConstantTest.OBJECT_KEY,
				"'select product_id, sum(retail_price * quantity) as gross_revenue\\\\n  from ***REMOVED***les\\\\n  group by product_id'");
		event.setField(ApplicationConstantTest.HOSTNAME_KEY, "localhost");
		event.setField(ApplicationConstantTest.SERVERHOST_KEY, "LP-5CD1184J8J");
		event.setField(ApplicationConstantTest.OPERATION_KEY, "QUERY");
		event.setField("timestamp", "20220430 18:20:35");
		Collection<Event> actualResponse = mariadbGuardiumFilter.filter(list, matchListener);
		assertNotNull(actualResponse);
	}

	@Test
	public void INSERTTest() {
		final String auditString = "20220430 19:01:33,LP-5CD1184J8J,root,localhost,12,188,QUERY,mariadb,'INSERT INTO birthday(name, date) VALUES(\\'Alex\\', \\'2001-11-24\\')',0";

		Event event = new org.logstash.Event();
		ArrayList<Event> list = new ArrayList<>();
		list.add(event);
		event.setField(ApplicationConstantTest.LOG_MESSAGE_KEY, auditString);
		event.setField(ApplicationConstantTest.CONNECTIONID_KEY, "12");
		event.setField(ApplicationConstantTest.USERNAME_KEY, "root");
		event.setField(ApplicationConstantTest.DATABASE_KEY, "mariadb");
		event.setField(ApplicationConstantTest.RETCODE_KEY, "0");
		event.setField(ApplicationConstantTest.OBJECT_KEY,
				"'INSERT INTO birthday(name, date) VALUES(\\\\'Alex\\\\', \\\\'2001-11-24\\\\')'");
		event.setField(ApplicationConstantTest.HOSTNAME_KEY, "localhost");
		event.setField(ApplicationConstantTest.SERVERHOST_KEY, "LP-5CD1184J8J");
		event.setField(ApplicationConstantTest.OPERATION_KEY, "QUERY");
		event.setField("timestamp", "20220430 19:01:33");
		Collection<Event> actualResponse = mariadbGuardiumFilter.filter(list, matchListener);
		assertNotNull(actualResponse);
	}

	@Test
	public void DROPTest() {
		final String auditString = "20220430 19:25:48,LP-5CD1184J8J,root,localhost,13,207,QUERY,mysql,'drop database testdb',0";

		Event event = new org.logstash.Event();
		ArrayList<Event> list = new ArrayList<>();
		list.add(event);
		event.setField(ApplicationConstantTest.LOG_MESSAGE_KEY, auditString);
		event.setField(ApplicationConstantTest.CONNECTIONID_KEY, "13");
		event.setField(ApplicationConstantTest.USERNAME_KEY, "root");
		event.setField(ApplicationConstantTest.DATABASE_KEY, "mysql");
		event.setField(ApplicationConstantTest.RETCODE_KEY, "0");
		event.setField(ApplicationConstantTest.OBJECT_KEY, "'drop database testdb'");
		event.setField(ApplicationConstantTest.HOSTNAME_KEY, "localhost");
		event.setField(ApplicationConstantTest.SERVERHOST_KEY, "LP-5CD1184J8J");
		event.setField(ApplicationConstantTest.OPERATION_KEY, "QUERY");
		event.setField("timestamp", "20220430 19:25:48");
		Collection<Event> actualResponse = mariadbGuardiumFilter.filter(list, matchListener);
		assertNotNull(actualResponse);
	}

	@Test
	public void CREATE_SCHEMATest() {
		final String auditString = "20220430 19:32:05,LP-5CD1184J8J,root,localhost,13,211,QUERY,mysql,'create schema my_schema comment=\\'this is comment1\\'',0";

		Event event = new org.logstash.Event();
		ArrayList<Event> list = new ArrayList<>();
		list.add(event);
		event.setField(ApplicationConstantTest.LOG_MESSAGE_KEY, auditString);
		event.setField(ApplicationConstantTest.CONNECTIONID_KEY, "13");
		event.setField(ApplicationConstantTest.USERNAME_KEY, "root");
		event.setField(ApplicationConstantTest.DATABASE_KEY, "mysql");
		event.setField(ApplicationConstantTest.RETCODE_KEY, "0");
		event.setField(ApplicationConstantTest.OBJECT_KEY,
				"'create schema my_schema comment=\\\\'this is comment1\\\\''");
		event.setField(ApplicationConstantTest.HOSTNAME_KEY, "localhost");
		event.setField(ApplicationConstantTest.SERVERHOST_KEY, "LP-5CD1184J8J");
		event.setField(ApplicationConstantTest.OPERATION_KEY, "QUERY");
		event.setField("timestamp", "20220430 19:32:05");
		Collection<Event> actualResponse = mariadbGuardiumFilter.filter(list, matchListener);
		assertNotNull(actualResponse);
	}

	@Test
	public void DATETest() {
		final String auditString = "20220430 19:01:52,LP-5CD1184J8J,root,localhost,12,189,QUERY,mariadb,'SELECT name, YEAR(date), MONTH(date), DAY(date) FROM birthday',0";

		Event event = new org.logstash.Event();
		ArrayList<Event> list = new ArrayList<>();
		list.add(event);
		event.setField(ApplicationConstantTest.LOG_MESSAGE_KEY, auditString);
		event.setField(ApplicationConstantTest.CONNECTIONID_KEY, "12");
		event.setField(ApplicationConstantTest.USERNAME_KEY, "root");
		event.setField(ApplicationConstantTest.DATABASE_KEY, "mariadb");
		event.setField(ApplicationConstantTest.RETCODE_KEY, "0");
		event.setField(ApplicationConstantTest.OBJECT_KEY,
				"'SELECT name, YEAR(date), MONTH(date), DAY(date) FROM birthday'");
		event.setField(ApplicationConstantTest.HOSTNAME_KEY, "localhost");
		event.setField(ApplicationConstantTest.SERVERHOST_KEY, "LP-5CD1184J8J");
		event.setField(ApplicationConstantTest.OPERATION_KEY, "QUERY");
		event.setField("timestamp", "20220430 19:01:52");
		Collection<Event> actualResponse = mariadbGuardiumFilter.filter(list, matchListener);
		assertNotNull(actualResponse);
	}

	@Test
	public void JSON_EXPRESSIONTest() {
		final String auditString = "20220430 19:01:52,LP-5CD1184J8J,root,localhost,12,189,QUERY,mariadb,'SELECT name, YEAR(date), MONTH(date), DAY(date) FROM birthday',0";

		Event event = new org.logstash.Event();
		ArrayList<Event> list = new ArrayList<>();
		list.add(event);
		event.setField(ApplicationConstantTest.LOG_MESSAGE_KEY, auditString);
		event.setField(ApplicationConstantTest.CONNECTIONID_KEY, "12");
		event.setField(ApplicationConstantTest.USERNAME_KEY, "root");
		event.setField(ApplicationConstantTest.DATABASE_KEY, "mariadb");
		event.setField(ApplicationConstantTest.RETCODE_KEY, "0");
		event.setField(ApplicationConstantTest.OBJECT_KEY,
				"'SELECT name, YEAR(date), MONTH(date), DAY(date) FROM birthday'");
		event.setField(ApplicationConstantTest.HOSTNAME_KEY, "localhost");
		event.setField(ApplicationConstantTest.SERVERHOST_KEY, "LP-5CD1184J8J");
		event.setField(ApplicationConstantTest.OPERATION_KEY, "QUERY");
		event.setField("timestamp", "20220430 19:01:52");
		Collection<Event> actualResponse = mariadbGuardiumFilter.filter(list, matchListener);
		assertNotNull(actualResponse);
	}

	@Test
	public void RENAMETest() {
		final String auditString = "20220430 10:59:41,LP-5CD1184J8J,root,localhost,10,131,QUERY,mariadb,'RENAME TABLE connector_details TO orders_table,\\n    t2 TO t1,\\n    orders_table TO t2',0";

		Event event = new org.logstash.Event();
		ArrayList<Event> list = new ArrayList<>();
		list.add(event);
		event.setField(ApplicationConstantTest.LOG_MESSAGE_KEY, auditString);
		event.setField(ApplicationConstantTest.CONNECTIONID_KEY, "10");
		event.setField(ApplicationConstantTest.USERNAME_KEY, "root");
		event.setField(ApplicationConstantTest.DATABASE_KEY, "mariadb");
		event.setField(ApplicationConstantTest.RETCODE_KEY, "0");
		event.setField(ApplicationConstantTest.OBJECT_KEY,
				"'RENAME TABLE connector_details TO orders_table,\\\\n    t2 TO t1,\\\\n    orders_table TO t2'");
		event.setField(ApplicationConstantTest.HOSTNAME_KEY, "localhost");
		event.setField(ApplicationConstantTest.SERVERHOST_KEY, "LP-5CD1184J8J");
		event.setField(ApplicationConstantTest.OPERATION_KEY, "QUERY");
		event.setField("timestamp", "20220430 10:59:41");
		Collection<Event> actualResponse = mariadbGuardiumFilter.filter(list, matchListener);
		assertNotNull(actualResponse);
	}

	@Test
	public void ORDER_BYTest() {
		final String auditString = "20220501 00:15:53,LP-5CD1184J8J,root,localhost,13,240,QUERY,mariadb,'UPDATE seq SET x=\\'z\\' WHERE x=\\'b\\' ORDER BY i DESC LIMIT 1',0";

		Event event = new org.logstash.Event();
		ArrayList<Event> list = new ArrayList<>();
		list.add(event);
		event.setField(ApplicationConstantTest.LOG_MESSAGE_KEY, auditString);
		event.setField(ApplicationConstantTest.CONNECTIONID_KEY, "13");
		event.setField(ApplicationConstantTest.USERNAME_KEY, "root");
		event.setField(ApplicationConstantTest.DATABASE_KEY, "mariadb");
		event.setField(ApplicationConstantTest.RETCODE_KEY, "0");
		event.setField(ApplicationConstantTest.OBJECT_KEY,
				"'UPDATE seq SET x=\\\\'z\\\\' WHERE x=\\\\'b\\\\' ORDER BY i DESC LIMIT 1'");
		event.setField(ApplicationConstantTest.HOSTNAME_KEY, "localhost");
		event.setField(ApplicationConstantTest.SERVERHOST_KEY, "LP-5CD1184J8J");
		event.setField(ApplicationConstantTest.OPERATION_KEY, "QUERY");
		event.setField("timestamp", "20220501 00:15:53");
		Collection<Event> actualResponse = mariadbGuardiumFilter.filter(list, matchListener);
		assertNotNull(actualResponse);
	}

	@Test
	public void CREATE_PROCEDURETest() {
		final String auditString = "20220207 12:43:54,LP-5CD1184J8J,root,localhost,24,174,QUERY,auditlog,'CREATE PROCEDURE SelectAllConnector()\\r\\n\\r\\nBegin\\r\\nSELECT * FROM Customers;\\r\\nEND',0";

		Event event = new org.logstash.Event();
		ArrayList<Event> list = new ArrayList<>();
		list.add(event);
		event.setField(ApplicationConstantTest.LOG_MESSAGE_KEY, auditString);
		event.setField(ApplicationConstantTest.CONNECTIONID_KEY, "24");
		event.setField(ApplicationConstantTest.USERNAME_KEY, "root");
		event.setField(ApplicationConstantTest.DATABASE_KEY, "auditlog");
		event.setField(ApplicationConstantTest.RETCODE_KEY, "0");
		event.setField(ApplicationConstantTest.OBJECT_KEY,
				"'CREATE PROCEDURE SelectAllConnector()\\\\r\\\\n\\\\r\\\\nBegin\\\\r\\\\nSELECT * FROM Customers;\\\\r\\\\nEND'");
		event.setField(ApplicationConstantTest.HOSTNAME_KEY, "localhost");
		event.setField(ApplicationConstantTest.SERVERHOST_KEY, "LP-5CD1184J8J");
		event.setField(ApplicationConstantTest.OPERATION_KEY, "QUERY");
		event.setField("timestamp", "20220207 12:43:54");
		Collection<Event> actualResponse = mariadbGuardiumFilter.filter(list, matchListener);
		assertNotNull(actualResponse);
	}

	@Test
	public void HAVINGTest() {
		final String auditString = "20220501 01:01:53,LP-5CD1184J8J,root,localhost,13,247,QUERY,mariadb,'SELECT genre, SUM(***REMOVED***les) AS \"Total\"\\nFROM book***REMOVED***les\\nWHERE ***REMOVED***les < 100\\nGROUP BY country\\nHAVING SUM(***REMOVED***les) > 500',0";

		Event event = new org.logstash.Event();
		ArrayList<Event> list = new ArrayList<>();
		list.add(event);
		event.setField(ApplicationConstantTest.LOG_MESSAGE_KEY, auditString);
		event.setField(ApplicationConstantTest.CONNECTIONID_KEY, "13");
		event.setField(ApplicationConstantTest.USERNAME_KEY, "root");
		event.setField(ApplicationConstantTest.DATABASE_KEY, "mariadb");
		event.setField(ApplicationConstantTest.RETCODE_KEY, "0");
		event.setField(ApplicationConstantTest.OBJECT_KEY,
				"'SELECT genre, SUM(***REMOVED***les) AS \\\"Total\\\"\\\\nFROM book***REMOVED***les\\\\nWHERE ***REMOVED***les < 100\\\\nGROUP BY country\\\\nHAVING SUM(***REMOVED***les) > 500'");
		event.setField(ApplicationConstantTest.HOSTNAME_KEY, "localhost");
		event.setField(ApplicationConstantTest.SERVERHOST_KEY, "LP-5CD1184J8J");
		event.setField(ApplicationConstantTest.OPERATION_KEY, "QUERY");
		event.setField("timestamp", "20220501 01:01:53");
		Collection<Event> actualResponse = mariadbGuardiumFilter.filter(list, matchListener);
		assertNotNull(actualResponse);
	}

	@Test
	public void CREATE_VIEWTest() {
		final String auditString = "20220501 00:35:20,LP-5CD1184J8J,root,localhost,13,244,QUERY,mariadb,'CREATE VIEW Customers\\nAS\\nSELECT *  \\nFROM t1',0";

		Event event = new org.logstash.Event();
		ArrayList<Event> list = new ArrayList<>();
		list.add(event);
		event.setField(ApplicationConstantTest.LOG_MESSAGE_KEY, auditString);
		event.setField(ApplicationConstantTest.CONNECTIONID_KEY, "13");
		event.setField(ApplicationConstantTest.USERNAME_KEY, "root");
		event.setField(ApplicationConstantTest.DATABASE_KEY, "mariadb");
		event.setField(ApplicationConstantTest.RETCODE_KEY, "0");
		event.setField(ApplicationConstantTest.OBJECT_KEY, "'CREATE VIEW Customers\\\\nAS\\\\nSELECT *  \\\\nFROM t1'");
		event.setField(ApplicationConstantTest.HOSTNAME_KEY, "localhost");
		event.setField(ApplicationConstantTest.SERVERHOST_KEY, "LP-5CD1184J8J");
		event.setField(ApplicationConstantTest.OPERATION_KEY, "QUERY");
		event.setField("timestamp", "20220501 00:35:20");
		Collection<Event> actualResponse = mariadbGuardiumFilter.filter(list, matchListener);
		assertNotNull(actualResponse);
	}

	@Test
	public void RENAME_STMTTest() {
		final String auditString = "20220430 10:59:41,LP-5CD1184J8J,root,localhost,10,131,QUERY,mariadb,'RENAME TABLE connector_details TO orders_table,\\n    t2 TO t1,\\n    orders_table TO t2',0";

		Event event = new org.logstash.Event();
		ArrayList<Event> list = new ArrayList<>();
		list.add(event);
		event.setField(ApplicationConstantTest.LOG_MESSAGE_KEY, auditString);
		event.setField(ApplicationConstantTest.CONNECTIONID_KEY, "10");
		event.setField(ApplicationConstantTest.USERNAME_KEY, "root");
		event.setField(ApplicationConstantTest.DATABASE_KEY, "mariadb");
		event.setField(ApplicationConstantTest.RETCODE_KEY, "0");
		event.setField(ApplicationConstantTest.OBJECT_KEY,
				"'RENAME TABLE connector_details TO orders_table,\\\\n    t2 TO t1,\\\\n    orders_table TO t2'");
		event.setField(ApplicationConstantTest.HOSTNAME_KEY, "localhost");
		event.setField(ApplicationConstantTest.SERVERHOST_KEY, "LP-5CD1184J8J");
		event.setField(ApplicationConstantTest.OPERATION_KEY, "QUERY");
		event.setField("timestamp", "20220430 10:59:41");
		Collection<Event> actualResponse = mariadbGuardiumFilter.filter(list, matchListener);
		assertNotNull(actualResponse);
	}

	@Test
	public void GRANT_STMTTest() {
		final String auditString = "20211205 17:18:47,LP-5CD1184J8J,root,localhost,5,42,QUERY,mysql,'grant replication slave on *.* to cjar_user@localhost',0";

		Event event = new org.logstash.Event();
		ArrayList<Event> list = new ArrayList<>();
		list.add(event);
		event.setField(ApplicationConstantTest.LOG_MESSAGE_KEY, auditString);
		event.setField(ApplicationConstantTest.CONNECTIONID_KEY, "5");
		event.setField(ApplicationConstantTest.USERNAME_KEY, "root");
		event.setField(ApplicationConstantTest.DATABASE_KEY, "mysql");
		event.setField(ApplicationConstantTest.RETCODE_KEY, "0");
		event.setField(ApplicationConstantTest.OBJECT_KEY, "'grant replication slave on *.* to cjar_user@localhost'");
		event.setField(ApplicationConstantTest.HOSTNAME_KEY, "localhost");
		event.setField(ApplicationConstantTest.SERVERHOST_KEY, "LP-5CD1184J8J");
		event.setField(ApplicationConstantTest.OPERATION_KEY, "QUERY");
		event.setField("timestamp", "20211205 17:18:47");
		Collection<Event> actualResponse = mariadbGuardiumFilter.filter(list, matchListener);
		assertNotNull(actualResponse);
	}

	@Test
	public void TRUNCATE_STMTTest() {
		final String auditString = "20220501 02:02:24,LP-5CD1184J8J,root,localhost,13,270,QUERY,auditlog,'Truncate table a',0";

		Event event = new org.logstash.Event();
		ArrayList<Event> list = new ArrayList<>();
		list.add(event);
		event.setField(ApplicationConstantTest.LOG_MESSAGE_KEY, auditString);
		event.setField(ApplicationConstantTest.CONNECTIONID_KEY, "13");
		event.setField(ApplicationConstantTest.USERNAME_KEY, "root");
		event.setField(ApplicationConstantTest.DATABASE_KEY, "auditlog");
		event.setField(ApplicationConstantTest.RETCODE_KEY, "0");
		event.setField(ApplicationConstantTest.OBJECT_KEY, "'Truncate table a'");
		event.setField(ApplicationConstantTest.HOSTNAME_KEY, "localhost");
		event.setField(ApplicationConstantTest.SERVERHOST_KEY, "LP-5CD1184J8J");
		event.setField(ApplicationConstantTest.OPERATION_KEY, "QUERY");
		event.setField("timestamp", "20220501 02:02:24");
		Collection<Event> actualResponse = mariadbGuardiumFilter.filter(list, matchListener);
		assertNotNull(actualResponse);
	}

	@Test
	public void DROP_USERTest() {
		final String auditString = "20211205 17:20:25,LP-5CD1184J8J,root,localhost,5,47,QUERY,mysql,'drop user cjar_user@localhost',0";

		Event event = new org.logstash.Event();
		ArrayList<Event> list = new ArrayList<>();
		list.add(event);
		event.setField(ApplicationConstantTest.LOG_MESSAGE_KEY, auditString);
		event.setField(ApplicationConstantTest.CONNECTIONID_KEY, "5");
		event.setField(ApplicationConstantTest.USERNAME_KEY, "root");
		event.setField(ApplicationConstantTest.DATABASE_KEY, "mysql");
		event.setField(ApplicationConstantTest.RETCODE_KEY, "0");
		event.setField(ApplicationConstantTest.OBJECT_KEY, "'drop user cjar_user@localhost'");
		event.setField(ApplicationConstantTest.HOSTNAME_KEY, "localhost");
		event.setField(ApplicationConstantTest.SERVERHOST_KEY, "LP-5CD1184J8J");
		event.setField(ApplicationConstantTest.OPERATION_KEY, "QUERY");
		event.setField("timestamp", "20211205 17:20:25");
		Collection<Event> actualResponse = mariadbGuardiumFilter.filter(list, matchListener);
		assertNotNull(actualResponse);
	}

	@Test
	public void ALTER_USERTest() {
		final String auditString = "20220430 05:01:48,LP-5CD1184J8J,root,localhost,5,24,QUERY,mysql,'ALTER USER foo2@test IDENTIFIED BY *****',0";

		Event event = new org.logstash.Event();
		ArrayList<Event> list = new ArrayList<>();
		list.add(event);
		event.setField(ApplicationConstantTest.LOG_MESSAGE_KEY, auditString);
		event.setField(ApplicationConstantTest.CONNECTIONID_KEY, "5");
		event.setField(ApplicationConstantTest.USERNAME_KEY, "root");
		event.setField(ApplicationConstantTest.DATABASE_KEY, "mysql");
		event.setField(ApplicationConstantTest.RETCODE_KEY, "0");
		event.setField(ApplicationConstantTest.OBJECT_KEY, "'ALTER USER foo2@test IDENTIFIED BY *****'");
		event.setField(ApplicationConstantTest.HOSTNAME_KEY, "localhost");
		event.setField(ApplicationConstantTest.SERVERHOST_KEY, "LP-5CD1184J8J");
		event.setField(ApplicationConstantTest.OPERATION_KEY, "QUERY");
		event.setField("timestamp", "20220430 05:01:48");
		Collection<Event> actualResponse = mariadbGuardiumFilter.filter(list, matchListener);
		assertNotNull(actualResponse);
	}

	@Test
	public void DELETETest() {
		final String auditString = "20211205 17:20:26,LP-5CD1184J8J,root,localhost,84,1040,QUERY,university,'DELETE from students where RollNo = \\'20\\'',0";

		Event event = new org.logstash.Event();
		ArrayList<Event> list = new ArrayList<>();
		list.add(event);
		event.setField(ApplicationConstantTest.LOG_MESSAGE_KEY, auditString);
		event.setField(ApplicationConstantTest.CONNECTIONID_KEY, "84");
		event.setField(ApplicationConstantTest.USERNAME_KEY, "root");
		event.setField(ApplicationConstantTest.DATABASE_KEY, "university");
		event.setField(ApplicationConstantTest.RETCODE_KEY, "0");
		event.setField(ApplicationConstantTest.OBJECT_KEY, "'DELETE from students where RollNo = \\\\'20\\\\''");
		event.setField(ApplicationConstantTest.HOSTNAME_KEY, "localhost");
		event.setField(ApplicationConstantTest.SERVERHOST_KEY, "LP-5CD1184J8J");
		event.setField(ApplicationConstantTest.OPERATION_KEY, "QUERY");
		event.setField("timestamp", "20211205 17:20:26");
		Collection<Event> actualResponse = mariadbGuardiumFilter.filter(list, matchListener);
		assertNotNull(actualResponse);
	}

	@Test
	public void UPDATETest() {
		final String auditString = "20211205 17:20:27,LP-5CD1184J8J,root,localhost,84,1048,QUERY,university,'UPDATE EMP SET AGE = AGE + 1',0";

		Event event = new org.logstash.Event();
		ArrayList<Event> list = new ArrayList<>();
		list.add(event);
		event.setField(ApplicationConstantTest.LOG_MESSAGE_KEY, auditString);
		event.setField(ApplicationConstantTest.CONNECTIONID_KEY, "84");
		event.setField(ApplicationConstantTest.USERNAME_KEY, "root");
		event.setField(ApplicationConstantTest.DATABASE_KEY, "university");
		event.setField(ApplicationConstantTest.RETCODE_KEY, "0");
		event.setField(ApplicationConstantTest.OBJECT_KEY, "'UPDATE EMP SET AGE = AGE + 1'");
		event.setField(ApplicationConstantTest.HOSTNAME_KEY, "localhost");
		event.setField(ApplicationConstantTest.SERVERHOST_KEY, "LP-5CD1184J8J");
		event.setField(ApplicationConstantTest.OPERATION_KEY, "QUERY");
		event.setField("timestamp", "20211205 17:20:27");
		Collection<Event> actualResponse = mariadbGuardiumFilter.filter(list, matchListener);
		assertNotNull(actualResponse);
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
