/*
Copyright IBM Corp. 2024 All rights reserved.
SPDX-License-Identifier: Apache-2.0
*/

package com.ibm.guardium.scylladb;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;
import com.ibm.guardium.universalconnector.commons.GuardConstants;
import org.junit.jupiter.api.Test;
import org.logstash.plugins.ContextImpl;
import co.elastic.logstash.api.Context;
import co.elastic.logstash.api.Event;
import co.elastic.logstash.api.FilterMatchListener;

public class ScyllaDbGuardiumFilterTest {

	final static Context context = new ContextImpl(null, null);
	final static ScyllaDbGuardiumFilter filter = new ScyllaDbGuardiumFilter("test-id", null, context);

	@Test
	public void testcreateevent() {
		Context context = new ContextImpl(null, null);
		ScyllaDbGuardiumFilter filter = new ScyllaDbGuardiumFilter("test-id", null, context);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("message", "");
		e.setField("username", "\"cassandra\"");
		e.setField("keyspace_name", "\"mykeyspace\"");
		e.setField("server_ip", "\"172.31.57.239\"");
		e.setField("client_ip", "\"9.43.118.110\"");
		e.setField("category", "\"DDL\"");
		e.setField("operation", "\"CREATE#012TABLE SUBRATO(ID INT PRIMARY KEY);\"");
		e.setField("error", "\"false\"");
		e.setField("timestamp", "2024-01-10T02:35:58.000Z");
		e.setField("logsource", "DWP-5CD212FV5L");
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertEquals(1, results.size());
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, matchListener.getMatchCount());
	}
	
	@Test
	public void testuseevent() {
		Context context = new ContextImpl(null, null);
		ScyllaDbGuardiumFilter filter = new ScyllaDbGuardiumFilter("test-id", null, context);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("message", "");
		e.setField("username", "\"cassandra\"");
		e.setField("keyspace_name", "\"mykeyspace\"");
		e.setField("server_ip", "\"172.31.57.239\"");
		e.setField("category", "\"DML\"");
		e.setField("operation", "\"USE mykeyspace;\"");
		e.setField("error", "\"false\"");
		e.setField("timestamp", "2024-01-10T02:35:58.000Z");
		e.setField("logsource", "DWP-5CD212FV5L");

		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertEquals(1, results.size());
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, matchListener.getMatchCount());
	}

	@Test
	public void testalterroleevent() {
		Context context = new ContextImpl(null, null);
		ScyllaDbGuardiumFilter filter = new ScyllaDbGuardiumFilter("test-id", null, context);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("message", "");
		e.setField("username", "\"alice1\"");
		e.setField("keyspace_name", "\"mykeyspace\"");
		e.setField("server_ip", "\"172.31.57.239\"");
		e.setField("category", "\"DCL\"");
		e.setField("operation", "\"ALTER ROLE alice1 WITH PASSWORD = '***' AND SUPERUSER = false;\"");
		e.setField("error", "\"false\"");
		e.setField("timestamp", "2024-01-10T02:35:58.000Z");
		e.setField("logsource", "DWP-5CD212FV5L");

		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertEquals(1, results.size());
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, matchListener.getMatchCount());
	}

	@Test
	public void testLoginSuccess() {
		Context context = new ContextImpl(null, null);
		ScyllaDbGuardiumFilter filter = new ScyllaDbGuardiumFilter("test-id", null, context);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("message", "");
		e.setField("username", "\"cassandra\"");
		e.setField("keyspace_name", "\"\"");
		e.setField("server_ip", "\"172.31.57.239\"");
		e.setField("category", "\"AUTH\"");
		e.setField("operation", "\"\"");
		e.setField("error", "\"false\"");
		e.setField("timestamp", "2024-01-10T02:35:58.000Z");
		e.setField("logsource", "DWP-5CD212FV5L");

		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertEquals(1, results.size());
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, matchListener.getMatchCount());
	}
	
	@Test
	public void testLoginFail() {
		Context context = new ContextImpl(null, null);
		ScyllaDbGuardiumFilter filter = new ScyllaDbGuardiumFilter("test-id", null, context);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("message", "");
		e.setField("username", "\"cassandra\"");
		e.setField("keyspace_name", "\"\"");
		e.setField("server_ip", "\"172.31.57.239\"");
		e.setField("category", "\"AUTH\"");
		e.setField("operation", "\"\"");
		e.setField("error", "\"true\"");
		e.setField("timestamp", "2024-01-10T02:35:58.000Z");
		e.setField("logsource", "DWP-5CD212FV5L");

		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertEquals(1, results.size());
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, matchListener.getMatchCount());
	}
	
	@Test
	public void testAdminevent() {
		Context context = new ContextImpl(null, null);
		ScyllaDbGuardiumFilter filter = new ScyllaDbGuardiumFilter("test-id", null, context);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("message", "");
		e.setField("username", "\"cassandra\"");
		e.setField("keyspace_name", "\"\"");
		e.setField("server_ip", "\"172.31.57.239\"");
		e.setField("category", "\"ADMIN\"");
		e.setField("operation", "\"CREATE SERVICE_LEVEL IF NOT EXISTS OLAP WITH SHARES = 100;\"");
		e.setField("error", "\"false\"");
		e.setField("timestamp", "2024-01-10T02:35:58.000Z");
		e.setField("logsource", "DWP-5CD212FV5L");

		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertEquals(1, results.size());
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, matchListener.getMatchCount());
	}
	
	
	@Test
	public void testerrorevent() {
		Context context = new ContextImpl(null, null);
		ScyllaDbGuardiumFilter filter = new ScyllaDbGuardiumFilter("test-id", null, context);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("message", "");
		e.setField("username", "\"alice1\"");
		e.setField("keyspace_name", "\"mykeyspace\"");
		e.setField("server_ip", "\"172.31.57.239\"");
		e.setField("category", "\"DCL\"");
		e.setField("operation", "\"ALTER ROLE alice1 WITH PASSWORD = '***' AND SUPERUSER = false;\"");
		e.setField("error", "\"true\"");
		e.setField("timestamp", "2024-01-10T02:35:58.000Z");
		e.setField("logsource", "DWP-5CD212FV5L");

		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertEquals(1, results.size());
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, matchListener.getMatchCount());
	}
	
	@Test
	public void testjsonparse() {
		Context context = new ContextImpl(null, null);
		ScyllaDbGuardiumFilter filter = new ScyllaDbGuardiumFilter("test-id", null, context);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("message", " ");

		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertEquals(1, results.size());
	}
	
	@Test
	public void testMultiline() {
		Context context = new ContextImpl(null, null);
		ScyllaDbGuardiumFilter filter = new ScyllaDbGuardiumFilter("test-id", null, context);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("message", "");
		e.setField("username", "\"cassandra\"");
		e.setField("keyspace_name", "\"mykeyspace\"");
		e.setField("server_ip", "\"172.31.57.239\"");
		e.setField("category", "\"DDL\"");
		e.setField("operation", "\"CREATE TABLE DEVTESTING1 (#012    a int,#012    b int,#012    c int,#012    PRIMARY KEY (a, b, c)#012);\"");
		e.setField("error", "\"false\"");
		e.setField("timestamp", "2024-01-10T02:35:58.000Z");
		e.setField("logsource", "DWP-5CD212FV5L");
		
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertEquals(1, results.size());
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, matchListener.getMatchCount());
	}
	
	@Test
	public void testnullfield() {
		Context context = new ContextImpl(null, null);
		ScyllaDbGuardiumFilter filter = new ScyllaDbGuardiumFilter("test-id", null, context);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("message", "");
		e.setField("username", null);
		e.setField("keyspace_name", null);
		e.setField("server_ip", null);
		e.setField("category", "\"DDL\""); 
		e.setField("operation", "\"CREATE TABLE DEVTESTING1 (#012    a int,#012    b int,#012    c int,#012    PRIMARY KEY (a, b, c)#012);\"");
		e.setField("error", "\"false\"");
		e.setField("timestamp", "2024-01-10T02:35:58.000Z");
		e.setField("logsource", "DWP-5CD212FV5L");
		
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertEquals(1, results.size());
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, matchListener.getMatchCount());
	}
	
	@Test
	public void testnullkeyspace() {
		Context context = new ContextImpl(null, null);
		ScyllaDbGuardiumFilter filter = new ScyllaDbGuardiumFilter("test-id", null, context);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("message", "");
		e.setField("username", null);
		e.setField("keyspace_name", null);
		e.setField("server_ip", "\"172.31.57.239\"");
		e.setField("category", "\"DDL\"");
		e.setField("operation", "\"CREATE TABLE DEVTESTING1 (#012    a int,#012    b int,#012    c int,#012    PRIMARY KEY (a, b, c)#012);\"");
		e.setField("error", "\"false\"");
		e.setField("timestamp", "2024-01-10T02:35:58.000Z");
		e.setField("logsource", "DWP-5CD212FV5L");
		
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertEquals(1, results.size());
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, matchListener.getMatchCount());
	}
	
	@Test
	public void testNouser() {
		Context context = new ContextImpl(null, null);
		ScyllaDbGuardiumFilter filter = new ScyllaDbGuardiumFilter("test-id", null, context);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("message", "");
		e.setField("username", "\"\"");
		e.setField("keyspace_name", "\"mykeyspace\"");
		e.setField("server_ip", "\"172.31.57.239\"");
		e.setField("category", "\"DML\"");
		e.setField("operation", "DELETE birth_year FROM Users3    #012    WHERE username IN ('jywf', 'wjh');");
		e.setField("error", "\"false\"");
		e.setField("timestamp", "2024-01-10T02:35:58.000Z");
		e.setField("logsource", "DWP-5CD212FV5L");

		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertEquals(1, results.size());
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, matchListener.getMatchCount());
	}
	
	@Test
	public void testNullCategory() {
		Context context = new ContextImpl(null, null);
		ScyllaDbGuardiumFilter filter = new ScyllaDbGuardiumFilter("test-id", null, context);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("message", "");
		e.setField("username", "\"cassandra\"");
		e.setField("keyspace_name", "\"\"");
		e.setField("server_ip", "\"172.31.57.239\"");
		e.setField("category", null);
		e.setField("operation", "\"\"");
		e.setField("error", "\"false\"");
		e.setField("timestamp", "2024-01-10T02:35:58.000Z");
		e.setField("logsource", "DWP-5CD212FV5L");

		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertEquals(1, results.size());
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, matchListener.getMatchCount());
	}
	
	@Test
	public void testNullSql() {
		Context context = new ContextImpl(null, null);
		ScyllaDbGuardiumFilter filter = new ScyllaDbGuardiumFilter("test-id", null, context);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("message", "");
		e.setField("username", "\"\"");
		e.setField("keyspace_name", "\"mykeyspace\"");
		e.setField("server_ip", "\"172.31.57.239\"");
		e.setField("category", "\"DML\"");
		e.setField("operation", null);
		e.setField("error", "\"false\"");
		e.setField("timestamp", "2024-01-10T02:35:58.000Z");
		e.setField("logsource", "DWP-5CD212FV5L");

		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertEquals(1, results.size());
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, matchListener.getMatchCount());
	}
	
	@Test
	public void testNullLoginFail() {
		Context context = new ContextImpl(null, null);
		ScyllaDbGuardiumFilter filter = new ScyllaDbGuardiumFilter("test-id", null, context);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("message", "");
		e.setField("username", "\"cassandra\"");
		e.setField("keyspace_name", "\"\"");
		e.setField("server_ip", "\"172.31.57.239\"");
		e.setField("category", null);
		e.setField("operation", "\"\"");
		e.setField("error", null);
		e.setField("timestamp", "2024-01-10T02:35:58.000Z");
		e.setField("logsource", "DWP-5CD212FV5L");

		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertEquals(1, results.size());
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, matchListener.getMatchCount());
	}
	
	@Test
	public void testIpv6() {
		Context context = new ContextImpl(null, null);
		ScyllaDbGuardiumFilter filter = new ScyllaDbGuardiumFilter("test-id", null, context);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("message", "");
		e.setField("username", "\"cassandra\"");
		e.setField("keyspace_name", "\"mykeyspace\"");
		e.setField("server_ip", "\"0000:0000:0000:0000:0000:FFFF:0000:0000\"");
		e.setField("category", "\"DDL\"");
		e.setField("operation", "\"CREATE TABLE SUBRATO(ID INT PRIMARY KEY);\"");
		e.setField("error", "\"false\"");
		e.setField("timestamp", "2024-01-10T02:35:58.000Z");
		e.setField("logsource", "DWP-5CD212FV5L");
		
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertEquals(1, results.size());
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, matchListener.getMatchCount());
	}
	
	@Test
	public void testNullcategoryevent() {
		Context context = new ContextImpl(null, null);
		ScyllaDbGuardiumFilter filter = new ScyllaDbGuardiumFilter("test-id", null, context);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("message", "");
		e.setField("username", "\"alice1\"");
		e.setField("keyspace_name", "\"mykeyspace\"");
		e.setField("server_ip", "\"172.31.57.239\"");
		e.setField("category", null);
		e.setField("operation", "\"ALTER ROLE alice1 WITH PASSWORD = '***' AND SUPERUSER = false;\"");
		e.setField("error", "\"true\"");
		e.setField("timestamp", "2024-01-10T02:35:58.000Z");
		e.setField("logsource", "DWP-5CD212FV5L");

		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertEquals(1, results.size());
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, matchListener.getMatchCount());
	}
	
	@Test
	public void testNullsqlevent() {
		Context context = new ContextImpl(null, null);
		ScyllaDbGuardiumFilter filter = new ScyllaDbGuardiumFilter("test-id", null, context);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("message", "");
		e.setField("username", "\"alice1\"");
		e.setField("keyspace_name", "\"mykeyspace\"");
		e.setField("server_ip", "\"172.31.57.239\"");
		e.setField("category", "\"DCL\"");
		e.setField("operation", null);
		e.setField("error", "\"true\"");
		e.setField("timestamp", "2024-01-10T02:35:58.000Z");
		e.setField("logsource", "DWP-5CD212FV5L");

		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertEquals(1, results.size());
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, matchListener.getMatchCount());
	}
	
	@Test
	public void testNulltimeevent() {
		Context context = new ContextImpl(null, null);
		ScyllaDbGuardiumFilter filter = new ScyllaDbGuardiumFilter("test-id", null, context);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("message", "");
		e.setField("username", "\"alice1\"");
		e.setField("keyspace_name", "\"mykeyspace\"");
		e.setField("server_ip", "\"172.31.57.239\"");
		e.setField("category", "\"DCL\"");
		e.setField("operation", null);
		e.setField("error", "\"true\"");
		e.setField("timestamp", null);
		e.setField("logsource", "DWP-5CD212FV5L");

		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertEquals(1, results.size());
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, matchListener.getMatchCount());
	}
	
	@Test
	public void testemptytimeevent() {
		Context context = new ContextImpl(null, null);
		ScyllaDbGuardiumFilter filter = new ScyllaDbGuardiumFilter("test-id", null, context);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("message", "");
		e.setField("username", "\"alice1\"");
		e.setField("keyspace_name", "\"mykeyspace\"");
		e.setField("server_ip", "\"172.31.57.239\"");
		e.setField("category", "\"DCL\"");
		e.setField("operation", null);
		e.setField("error", "\"true\"");
		e.setField("timestamp", "");
		e.setField("logsource", "DWP-5CD212FV5L");

		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertEquals(1, results.size());
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, matchListener.getMatchCount());
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
