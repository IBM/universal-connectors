/*
Copyright IBM Corp. 2023 All rights reserved.
SPDX-License-Identifier: Apache-2.0
*/
package com.ibm.guardium.azuremysql;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;
import org.logstash.plugins.ContextImpl;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ibm.guardium.universalconnector.commons.GuardConstants;
import com.ibm.guardium.universalconnector.commons.structures.Record;

import co.elastic.logstash.api.Context;
import co.elastic.logstash.api.Event;
import co.elastic.logstash.api.FilterMatchListener;

public class AzureMysqlGuardiumFilterTest {
	final static Context context = new ContextImpl(null, null);
	final static AzureMysqlGuardiumFilter filter = new AzureMysqlGuardiumFilter("test-id", null, context);

	@Test
	public void testMysql_CreateTable() {
		final String mysqlString = "{\"ServerType\":\"MySQL\",\"resourceId\":\"/SUBSCRIPTIONS/083DE1FB-CD2D-4B7C-895A-2B5AF1D091E8/RESOURCEGROUPS/NEWRESOURCEGUARDIUM/PROVIDERS/MICROSOFT.DBFORMYSQL/FLEXIBLESERVERS/MYSQL-TEST-GUARDIUM\",\"category\":\"MySqlAuditLogs\",\"properties\":{\"event_subclass\":\"LOG\",\"replication_set_role\":\"single\",\"thread_id\":27,\"ip\":\"20.219.48.223\",\"host\":\"\",\"event_class\":\"general_log\",\"error_code\":0,\"category\":\"MySqlAuditLogs\",\"user\":\"test[test] @  [20.219.48.223]\",\"sql_text\":\"create table subratotable55(ID int, phone int)\",\"event_time\":\"2023-04-17T09:59:26Z\"}}";
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("message", mysqlString);
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertEquals(1, results.size());
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, matchListener.getMatchCount());
	}

	@Test
	public void testMysql_Insert() {
		final String mysqlString = "{\"ServerType\":\"MySQL\",\"resourceId\":\"/SUBSCRIPTIONS/083DE1FB-CD2D-4B7C-895A-2B5AF1D091E8/RESOURCEGROUPS/NEWRESOURCEGUARDIUM/PROVIDERS/MICROSOFT.DBFORMYSQL/FLEXIBLESERVERS/MYSQL-TEST-GUARDIUM\",\"category\":\"MySqlAuditLogs\",\"properties\":{\"event_subclass\":\"LOG\",\"replication_set_role\":\"single\",\"thread_id\":21,\"ip\":\"20.204.110.141\",\"host\":\"\",\"event_class\":\"general_log\",\"error_code\":0,\"category\":\"MySqlAuditLogs\",\"user\":\"test[test] @  [20.204.110.141]\",\"sql_text\":\"insert into resourcetable(id) values(22)\",\"event_time\":\"2023-04-17T07:37:31Z\"}}";
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("message", mysqlString);
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertEquals(1, results.size());
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, matchListener.getMatchCount());
	}

	@Test
	public void testMysql_Showdatabase() {
		final String mysqlString = "{\"ServerType\":\"MySQL\",\"resourceId\":\"/SUBSCRIPTIONS/083DE1FB-CD2D-4B7C-895A-2B5AF1D091E8/RESOURCEGROUPS/NEWRESOURCEGUARDIUM/PROVIDERS/MICROSOFT.DBFORMYSQL/FLEXIBLESERVERS/MYSQL-TEST-GUARDIUM\",\"category\":\"MySqlAuditLogs\",\"properties\":{\"event_subclass\":\"LOG\",\"replication_set_role\":\"single\",\"thread_id\":21,\"ip\":\"20.204.110.141\",\"host\":\"\",\"event_class\":\"general_log\",\"error_code\":0,\"category\":\"MySqlAuditLogs\",\"user\":\"test[test] @  [20.204.110.141]\",\"sql_text\":\"show databases\",\"event_time\":\"2023-04-17T07:37:31Z\"}}";
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("message", mysqlString);
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertEquals(1, results.size());
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, matchListener.getMatchCount());
	}

	@Test
	public void testMysql_Showtable() {
		final String mysqlString = "{\"ServerType\":\"MySQL\",\"resourceId\":\"/SUBSCRIPTIONS/083DE1FB-CD2D-4B7C-895A-2B5AF1D091E8/RESOURCEGROUPS/NEWRESOURCEGUARDIUM/PROVIDERS/MICROSOFT.DBFORMYSQL/FLEXIBLESERVERS/MYSQL-TEST-GUARDIUM\",\"category\":\"MySqlAuditLogs\",\"properties\":{\"event_subclass\":\"LOG\",\"replication_set_role\":\"single\",\"thread_id\":21,\"ip\":\"20.204.110.141\",\"host\":\"\",\"event_class\":\"general_log\",\"error_code\":0,\"category\":\"MySqlAuditLogs\",\"user\":\"test[test] @  [20.204.110.141]\",\"sql_text\":\"show tables\",\"event_time\":\"2023-04-17T07:37:31Z\"}}";
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("message", mysqlString);
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertEquals(1, results.size());
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, matchListener.getMatchCount());
	}

	@Test
	public void testMysql_Altertable() {
		final String mysqlString = "{\"ServerType\":\"MySQL\",\"resourceId\":\"/SUBSCRIPTIONS/083DE1FB-CD2D-4B7C-895A-2B5AF1D091E8/RESOURCEGROUPS/NEWRESOURCEGUARDIUM/PROVIDERS/MICROSOFT.DBFORMYSQL/FLEXIBLESERVERS/MYSQL-TEST-GUARDIUM\",\"category\":\"MySqlAuditLogs\",\"properties\":{\"event_subclass\":\"LOG\",\"replication_set_role\":\"single\",\"thread_id\":21,\"ip\":\"20.204.110.141\",\"host\":\"\",\"event_class\":\"general_log\",\"error_code\":0,\"category\":\"MySqlAuditLogs\",\"user\":\"test[test] @  [20.204.110.141]\",\"sql_text\":\"Alter table subratotable1 add (Name varchar(20))\",\"event_time\":\"2023-04-17T08:13:46Z\"}}";
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("message", mysqlString);
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertEquals(1, results.size());
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, matchListener.getMatchCount());
	}

	@Test
	public void testMysql_Select() {
		final String mysqlString = "{\"ServerType\":\"MySQL\",\"resourceId\":\"/SUBSCRIPTIONS/083DE1FB-CD2D-4B7C-895A-2B5AF1D091E8/RESOURCEGROUPS/NEWRESOURCEGUARDIUM/PROVIDERS/MICROSOFT.DBFORMYSQL/FLEXIBLESERVERS/MYSQL-TEST-GUARDIUM\",\"category\":\"MySqlAuditLogs\",\"properties\":{\"event_subclass\":\"LOG\",\"replication_set_role\":\"single\",\"thread_id\":21,\"ip\":\"20.204.110.141\",\"host\":\"\",\"event_class\":\"general_log\",\"error_code\":0,\"category\":\"MySqlAuditLogs\",\"user\":\"test[test] @  [20.204.110.141]\",\"sql_text\":\"select * from subratotable\",\"event_time\":\"2023-04-17T07:37:47Z\"}}";
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("message", mysqlString);
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertEquals(1, results.size());
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, matchListener.getMatchCount());
	}

	@Test
	public void testMysql_Droptable() {
		final String mysqlString = "{\"ServerType\":\"MySQL\",\"resourceId\":\"/SUBSCRIPTIONS/083DE1FB-CD2D-4B7C-895A-2B5AF1D091E8/RESOURCEGROUPS/NEWRESOURCEGUARDIUM/PROVIDERS/MICROSOFT.DBFORMYSQL/FLEXIBLESERVERS/MYSQL-TEST-GUARDIUM\",\"category\":\"MySqlAuditLogs\",\"properties\":{\"event_subclass\":\"LOG\",\"replication_set_role\":\"single\",\"thread_id\":21,\"ip\":\"20.204.110.141\",\"host\":\"\",\"event_class\":\"general_log\",\"error_code\":0,\"category\":\"MySqlAuditLogs\",\"user\":\"test[test] @  [20.204.110.141]\",\"sql_text\":\"drop table subratotable\",\"event_time\":\"2023-04-17T08:13:46Z\"}}";
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("message", mysqlString);
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertEquals(1, results.size());
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, matchListener.getMatchCount());
	}

	@Test
	public void testMysql_Resourcetable() {
		final String mysqlString = "{\"ServerType\":\"MySQL\",\"resourceId\":\"/SUBSCRIPTIONS/083DE1FB-CD2D-4B7C-895A-2B5AF1D091E8/RESOURCEGROUPS/NEWRESOURCEGUARDIUM/PROVIDERS/MICROSOFT.DBFORMYSQL/FLEXIBLESERVERS/MYSQL-TEST-GUARDIUM\",\"category\":\"MySqlAuditLogs\",\"properties\":{\"event_subclass\":\"LOG\",\"replication_set_role\":\"single\",\"thread_id\":21,\"ip\":\"20.204.110.141\",\"host\":\"\",\"event_class\":\"general_log\",\"error_code\":0,\"category\":\"MySqlAuditLogs\",\"user\":\"test[test] @  [20.204.110.141]\",\"sql_text\":\"select * from resourcetable\",\"event_time\":\"2023-04-17T08:13:46Z\"}}";
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("message", mysqlString);
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertEquals(1, results.size());
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, matchListener.getMatchCount());
	}

	@Test
	public void testMysql_Database() throws Exception {
		final String mysqlString = "{\"ServerType\":\"MySQL\",\"resourceId\":\"/SUBSCRIPTIONS/083DE1FB-CD2D-4B7C-895A-2B5AF1D091E8/RESOURCEGROUPS/NEWRESOURCEGUARDIUM/PROVIDERS/MICROSOFT.DBFORMYSQL/FLEXIBLESERVERS/MYSQL-TEST-GUARDIUM\",\"category\":\"MySqlAuditLogs\",\"properties\":{\"event_subclass\":\"LOG\",\"replication_set_role\":\"single\",\"thread_id\":21,\"ip\":\"20.204.110.141\",\"host\":\"\",\"event_class\":\"general_log\",\"error_code\":0,\"category\":\"MySqlAuditLogs\",\"user\":\"test[test] @  [20.204.110.141]\",\"sql_text\":\"SELECT DATABASE()\",\"event_time\":\"2023-04-17T08:13:46Z\"}}";
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("message", mysqlString);
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertEquals(1, results.size());
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, matchListener.getMatchCount());
	}

	@Test
	public void testMysql_Error() {
		final String mysqlString = "{\"ServerType\":\"MySQL\",\"resourceId\":\"/SUBSCRIPTIONS/083DE1FB-CD2D-4B7C-895A-2B5AF1D091E8/RESOURCEGROUPS/NEWRESOURCEGUARDIUM/PROVIDERS/MICROSOFT.DBFORMYSQL/FLEXIBLESERVERS/MYSQL-TEST-GUARDIUM\",\"category\":\"MySqlAuditLogs\",\"properties\":{\"event_subclass\":\"ERROR\",\"replication_set_role\":\"single\",\"thread_id\":27,\"ip\":\"20.219.48.223\",\"host\":\"\",\"event_class\":\"general_log\",\"error_code\":1146,\"category\":\"MySqlAuditLogs\",\"user\":\"test[test] @  [20.219.48.223]\",\"sql_text\":\"select * from subrato\",\"event_time\":\"2023-04-17T09:27:22Z\"}}";
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("message", mysqlString);
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertEquals(1, results.size());
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, matchListener.getMatchCount());
	}

	@Test
	public void testMysql_Empty() throws Exception {
		final String mysqlString = "";
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("message", mysqlString);
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertEquals(1, results.size());
	}

	@Test
	public void testMysql_DB() {
		final String mysqlString = "{\"ServerType\":\"MySQL\",\"resourceId\":\"/SUBSCRIPTIONS/083DE1FB-CD2D-4B7C-895A-2B5AF1D091E8/RESOURCEGROUPS/NEWRESOURCEGUARDIUM/PROVIDERS/MICROSOFT.DBFORMYSQL/FLEXIBLESERVERS/MYSQL-TEST-GUARDIUM\",\"category\":\"MySqlAuditLogs\",\"properties\":{\"event_subclass\":\"DISCONNECT\",\"replication_set_role\":\"single\",\"connection_id\":27,\"ip\":\"20.219.48.223\",\"host\":\"\",\"event_class\":\"connection_log\",\"category\":\"MySqlAuditLogs\",\"user\":\"test\",\"db\":\"\"}}";
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("message", mysqlString);
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertEquals(1, results.size());
	}

	@Test
	public void testMysql_Sql() {
		final String mysqlString = "{\"ServerType\":\"MySQL\",\"resourceId\":\"/SUBSCRIPTIONS/083DE1FB-CD2D-4B7C-895A-2B5AF1D091E8/RESOURCEGROUPS/NEWRESOURCEGUARDIUM/PROVIDERS/MICROSOFT.DBFORMYSQL/FLEXIBLESERVERS/MYSQL-TEST-GUARDIUM\",\"category\":\"MySqlAuditLogs\",\"properties\":{\"event_subclass\":\"LOG\",\"replication_set_role\":\"single\",\"thread_id\":21,\"ip\":\"20.204.110.141\",\"host\":\"\",\"event_class\":\"general_log\",\"error_code\":0,\"category\":\"MySqlAuditLogs\",\"user\":\"test[test] @  [20.204.110.141]\",\"event_time\":\"2023-04-17T08:13:46Z\"}}";
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("message", mysqlString);
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertEquals(1, results.size());
	}

	@Test
	public void testMysql_NoUser() {
		final String mysqlString = "{\"ServerType\":\"MySQL\",\"resourceId\":\"/SUBSCRIPTIONS/083DE1FB-CD2D-4B7C-895A-2B5AF1D091E8/RESOURCEGROUPS/NEWRESOURCEGUARDIUM/PROVIDERS/MICROSOFT.DBFORMYSQL/FLEXIBLESERVERS/MYSQL-TEST-GUARDIUM\",\"category\":\"MySqlAuditLogs\",\"properties\":{\"event_subclass\":\"LOG\",\"replication_set_role\":\"single\",\"thread_id\":21,\"ip\":\"20.204.110.141\",\"host\":\"\",\"event_class\":\"general_log\",\"error_code\":0,\"category\":\"MySqlAuditLogs\",\"event_time\":\"2023-04-17T08:13:46Z\"}}";
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("message", mysqlString);
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertEquals(1, results.size());
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, matchListener.getMatchCount());
	}

	@Test
	public void testMysql_NoMessage() {
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertEquals(1, results.size());
	}

	@Test
	public void testInvalidRecords() {
		final String mysqlString = "{\"ServerType\":\"MySQL\",\"resourceId\":\"/SUBSCRIPTIONS/083DE1FB-CD2D-4B7C-895A-2B5AF1D091E8/RESOURCEGROUPS/NEWRESOURCEGUARDIUM/PROVIDERS/MICROSOFT.DBFORMYSQL/FLEXIBLESERVERS/MYSQL-TEST-GUARDIUM\",\"category\":\"MySqlAuditLogs\",\"properties\":{}}";
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("message", mysqlString);
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertEquals(1, results.size());

	}

	@Test
	public void testConnection() {
		final String mysqlString = "{\"ServerType\":\"MySQL\",\"resourceId\":\"/SUBSCRIPTIONS/083DE1FB-CD2D-4B7C-895A-2B5AF1D091E8/RESOURCEGROUPS/NEWRESOURCEGUARDIUM/PROVIDERS/MICROSOFT.DBFORMYSQL/FLEXIBLESERVERS/MYSQL-TEST-GUARDIUM\",\"category\":\"MySqlAuditLogs\",\"properties\":{\"replication_set_role\":\"single\",\"event_subclass\":\"CONNECT\",\"connection_id\":41,\"ip\":\"192.8.218.211\",\"host\":\"\",\"event_class\":\"connection_log\",\"category\":\"MySqlAuditLogs\",\"user\":\"test\",\"db\":\"demodatabase\"}}";
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("message", mysqlString);
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertEquals(1, results.size());

	}

	@Test
	public void testDissConnection() {
		final String mysqlString = "{\"ServerType\":\"MySQL\",\"resourceId\":\"/SUBSCRIPTIONS/083DE1FB-CD2D-4B7C-895A-2B5AF1D091E8/RESOURCEGROUPS/NEWRESOURCEGUARDIUM/PROVIDERS/MICROSOFT.DBFORMYSQL/FLEXIBLESERVERS/MYSQL-TEST-GUARDIUM\",\"category\":\"MySqlAuditLogs\",\"properties\":{\"replication_set_role\":\"single\",\"event_subclass\":\"DISCONNECT\",\"connection_id\":41,\"ip\":\"192.8.218.211\",\"host\":\"\",\"event_class\":\"connection_log\",\"category\":\"MySqlAuditLogs\",\"user\":\"test\",\"db\":\"demodatabase\"}}";
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("message", mysqlString);
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertEquals(1, results.size());

	}

	@Test
	public void getIdTest() {
		final String mysqlString = "";
		Event e = new org.logstash.Event();
		e.setField("message", mysqlString);
		String id = filter.getId();
	}

	class TestMatchListener implements FilterMatchListener {
		private AtomicInteger matchCount = new AtomicInteger(0);

		public int getMatchCount() {
			return matchCount.get();
		}

		@Override
		public void filterMatched(co.elastic.logstash.api.Event arg0) {
			matchCount.incrementAndGet();

		}
	}

}
