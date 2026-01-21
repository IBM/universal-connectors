/*
Copyright IBM Corp. 2024 All rights reserved.
SPDX-License-Identifier: Apache-2.0
*/
package com.ibm.guardium.DATASOURCE_PLACEHOLDER;

import static org.junit.Assert.*;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;
import org.logstash.plugins.ContextImpl;

import com.ibm.guardium.universalconnector.commons.GuardConstants;

import co.elastic.logstash.api.Context;
import co.elastic.logstash.api.Event;
import co.elastic.logstash.api.FilterMatchListener;

/**
 * Unit tests for DataSourceGuardiumFilter
 * 
 * INSTRUCTIONS:
 * 1. Replace DATASOURCE_PLACEHOLDER with your data source name
 * 2. Replace DataSourceGuardiumFilter with your actual filter class name
 * 3. Add test cases for different log types from your data source
 * 4. Include tests for success cases, error cases, and edge cases
 * 5. Use actual sample logs from your data source
 */
public class DataSourceGuardiumFilterTest {
	
	final static Context context = new ContextImpl(null, null);
	final static DataSourceGuardiumFilter filter = new DataSourceGuardiumFilter("test-id", null, context);

	/**
	 * Test helper class to count matched events
	 */
	static class TestMatchListener implements FilterMatchListener {
		private AtomicInteger matchCount = new AtomicInteger(0);

		@Override
		public void filterMatched(Event event) {
			matchCount.incrementAndGet();
		}

		public int getMatchCount() {
			return matchCount.get();
		}
	}

	/**
	 * Test successful SELECT query
	 * TODO: Replace with actual log from your data source
	 */
	@Test
	public void testSuccessfulSelectQuery() {
		// TODO: Replace with actual JSON or text log from your data source
		final String auditLog = "{ \"timestamp\": \"2024-01-01T12:00:00Z\", \"user\": \"testuser\", \"database\": \"testdb\", \"operation\": \"SELECT\", \"query\": \"SELECT * FROM users\", \"status\": \"SUCCESS\" }";
		
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("message", auditLog);
		
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		
		assertEquals(1, results.size());
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, matchListener.getMatchCount());
	}

	/**
	 * Test successful INSERT operation
	 * TODO: Replace with actual log from your data source
	 */
	@Test
	public void testSuccessfulInsert() {
		final String auditLog = "{ \"timestamp\": \"2024-01-01T12:00:00Z\", \"user\": \"testuser\", \"database\": \"testdb\", \"operation\": \"INSERT\", \"query\": \"INSERT INTO users VALUES (1, 'John')\", \"status\": \"SUCCESS\" }";
		
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("message", auditLog);
		
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		
		assertEquals(1, results.size());
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, matchListener.getMatchCount());
	}

	/**
	 * Test successful UPDATE operation
	 * TODO: Replace with actual log from your data source
	 */
	@Test
	public void testSuccessfulUpdate() {
		final String auditLog = "{ \"timestamp\": \"2024-01-01T12:00:00Z\", \"user\": \"testuser\", \"database\": \"testdb\", \"operation\": \"UPDATE\", \"query\": \"UPDATE users SET name='Jane' WHERE id=1\", \"status\": \"SUCCESS\" }";
		
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("message", auditLog);
		
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		
		assertEquals(1, results.size());
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, matchListener.getMatchCount());
	}

	/**
	 * Test successful DELETE operation
	 * TODO: Replace with actual log from your data source
	 */
	@Test
	public void testSuccessfulDelete() {
		final String auditLog = "{ \"timestamp\": \"2024-01-01T12:00:00Z\", \"user\": \"testuser\", \"database\": \"testdb\", \"operation\": \"DELETE\", \"query\": \"DELETE FROM users WHERE id=1\", \"status\": \"SUCCESS\" }";
		
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("message", auditLog);
		
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		
		assertEquals(1, results.size());
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, matchListener.getMatchCount());
	}

	/**
	 * Test failed operation (error case)
	 * TODO: Replace with actual error log from your data source
	 */
	@Test
	public void testFailedOperation() {
		final String auditLog = "{ \"timestamp\": \"2024-01-01T12:00:00Z\", \"user\": \"testuser\", \"database\": \"testdb\", \"operation\": \"SELECT\", \"query\": \"SELECT * FROM nonexistent\", \"status\": \"ERROR\", \"errorMessage\": \"Table not found\" }";
		
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("message", auditLog);
		
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		
		assertEquals(1, results.size());
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, matchListener.getMatchCount());
	}

	/**
	 * Test DDL operation (CREATE TABLE)
	 * TODO: Replace with actual DDL log from your data source
	 */
	@Test
	public void testCreateTable() {
		final String auditLog = "{ \"timestamp\": \"2024-01-01T12:00:00Z\", \"user\": \"admin\", \"database\": \"testdb\", \"operation\": \"CREATE\", \"query\": \"CREATE TABLE users (id INT, name VARCHAR(100))\", \"status\": \"SUCCESS\" }";
		
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("message", auditLog);
		
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		
		assertEquals(1, results.size());
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, matchListener.getMatchCount());
	}

	/**
	 * Test invalid JSON
	 */
	@Test
	public void testInvalidJson() {
		final String auditLog = "This is not valid JSON";
		
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("message", auditLog);
		
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		
		assertEquals(1, results.size());
		// Should be tagged with error
		assertTrue(e.getTags().contains(DataSourceGuardiumFilter.LOGSTASH_TAG_JSON_PARSE_ERROR));
		assertEquals(0, matchListener.getMatchCount());
	}

	/**
	 * Test null message
	 */
	@Test
	public void testNullMessage() {
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("message", null);
		
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		
		assertEquals(1, results.size());
		assertEquals(0, matchListener.getMatchCount());
	}

	/**
	 * Test empty message
	 */
	@Test
	public void testEmptyMessage() {
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("message", "");
		
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		
		assertEquals(1, results.size());
		assertEquals(0, matchListener.getMatchCount());
	}

	/**
	 * Test with missing required fields
	 * TODO: Customize based on your required fields
	 */
	@Test
	public void testMissingRequiredFields() {
		final String auditLog = "{ \"timestamp\": \"2024-01-01T12:00:00Z\" }"; // Missing other required fields
		
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("message", auditLog);
		
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		
		assertEquals(1, results.size());
		// Depending on your implementation, this might be tagged with an error
	}

	// TODO: Add more test cases specific to your data source:
	// - Different user types (admin, regular user, service account)
	// - Different operation types specific to your data source
	// - Edge cases (very long queries, special characters, etc.)
	// - Different timestamp formats if applicable
	// - IPv4 and IPv6 addresses
	// - Multiple databases/schemas
	// - Transactions
	// - Stored procedures/functions
	// - etc.
}

// Made with Bob
