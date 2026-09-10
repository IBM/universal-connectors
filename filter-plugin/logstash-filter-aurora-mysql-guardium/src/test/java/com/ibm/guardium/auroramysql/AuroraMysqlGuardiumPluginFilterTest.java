//
// Copyright 2020- IBM Inc. All rights reserved
// SPDX-License-Identifier: Apache2.0
//
package com.ibm.guardium.auroramysql;

//import co.elastic.logstash.api.Configuration;

import co.elastic.logstash.api.Context;
import co.elastic.logstash.api.Event;
import co.elastic.logstash.api.FilterMatchListener;
import com.ibm.guardium.universalconnector.commons.GuardConstants;
import org.junit.Assert;
import org.junit.Test;
import org.logstash.plugins.ContextImpl;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;


public class AuroraMysqlGuardiumPluginFilterTest {

	final static Context context = new ContextImpl(null, null);
	final static AuroraMysqlGuardiumPluginFilter filter = new AuroraMysqlGuardiumPluginFilter("test-id", null, context);

	/**
	 * To feed Guardium universal connector, a "GuardRecord" fields must exist.
	 * 
	 * Filter should add field "GuardRecord" to the Event, which Universal connector
	 * then inserts into Guardium.
	 */

	// event string
	@Test
	public void testFieldGuardRecord_aurora_mysql() {

		final String s1 = "1638806583399975,testauroracluster-instance-1,admin,49.36.47.163,2955,11590217,QUERY,,'SELECT current_user()',0";
		
		Context context = new ContextImpl(null, null);
		AuroraMysqlGuardiumPluginFilter filter = new AuroraMysqlGuardiumPluginFilter("test-id", null, context);

		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();

		e.setField("message", s1);
		e.setField(Constants.TIMESTAMP, "1636962255474405");
		e.setField(Constants.CLIENT_IP, "192.168.56.1");
		e.setField(Constants.SESSION_ID, "1234");
		e.setField(Constants.ACTION_STATUS, "0");
		e.setField(Constants.EXEC_STATEMENT, "'SELECT CONVERT(DATE_FORMAT(joining_date,\\\"%Y-%m-%d-%H:%i:00\\\"),DATETIME) FROM Employee LIMIT 0, 1000'");
		e.setField(Constants.DB_NAME, "music");
		e.setField(Constants.SERVER_INSTANCE, "testauroracluster-instance-1");
		e.setField(Constants.DB_USER, "admin");
		e.setField(Constants.AUDIT_ACTION, "FAILED_CONNECT");
		e.setField(Constants.SERVERHOSTNAME, "serverHostName");

		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);

		Assert.assertEquals(1, results.size());
		Assert.assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		Assert.assertEquals(1, matchListener.getMatchCount());
	}

	/**
		* Verify that an event with empty dbName but a fully-qualified SQL statement
		* (schema.table) is processed successfully — replicates the customer scenario
		* where Aurora audit logs emit an empty dbName when no USE statement was issued.
		*/
	@Test
	public void testFieldGuardRecord_emptyDbName_fullyQualifiedSQL() {

		Context context = new ContextImpl(null, null);
		AuroraMysqlGuardiumPluginFilter filter = new AuroraMysqlGuardiumPluginFilter("test-id", null, context);

		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();

		// Simulates the event after grok has run: dbName is empty, schema is
		// embedded in the SQL as a fully-qualified reference (schema.`table`)
		e.setField("message", "1638806583399975,test-aurora-mysql-instance-1,testuser,192.0.2.10,2343,184395,QUERY,,'CREATE TABLE testschema.`test_table` (id INT NOT NULL PRIMARY KEY) ENGINE=InnoDB',0");
		e.setField(Constants.TIMESTAMP, "1638806583399975");
		e.setField(Constants.CLIENT_IP, "192.0.2.10");
		e.setField(Constants.SESSION_ID, "2343");
		e.setField(Constants.ACTION_STATUS, "0");
		e.setField(Constants.EXEC_STATEMENT, "'CREATE TABLE testschema.`test_table` (id INT NOT NULL PRIMARY KEY) ENGINE=InnoDB'");
		e.setField(Constants.DB_NAME, "testschema");   // as resolved by the config grok fallback
		e.setField(Constants.SERVER_INSTANCE, "test-aurora-mysql-instance-1");
		e.setField(Constants.DB_USER, "testuser");
		e.setField(Constants.AUDIT_ACTION, "QUERY");
		e.setField(Constants.SERVERHOSTNAME, "sampleaccountid_test-aurora-mysql-instance-1");

		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);

		Assert.assertEquals(1, results.size());
		Assert.assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		Assert.assertEquals(1, matchListener.getMatchCount());
	}

	/**
		* Event with empty dbName but fully-qualified schema.table in SQL.
		* Conf grok extracts schema from SQL (e.g. testschema from
		* CREATE TABLE testschema.`test_table`).
		* Verifies plugin parses the event and produces a GuardRecord.
		*/
	@Test
	public void testFieldGuardRecord_emptyDbName_fullyQualifiedSQL_confGrokExtract() {

		Context context = new ContextImpl(null, null);
		AuroraMysqlGuardiumPluginFilter filter = new AuroraMysqlGuardiumPluginFilter("test-id", null, context);

		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();

		// Simulates event after conf-level grok extracts dbName from SQL:
		// Original audit log: ...,QUERY,,'CREATE TABLE testschema.`test_table`...',0  (empty dbName)
		// Conf grok sets dbName = "testschema" before invoking plugin
		e.setField("message", "1638806583399975,test-aurora-mysql-instance-1,testuser,192.0.2.10,2343,184395,QUERY,,'CREATE TABLE testschema.`test_table` (id INT NOT NULL PRIMARY KEY) ENGINE=InnoDB',0");
		e.setField(Constants.TIMESTAMP, "1638806583399975");
		e.setField(Constants.CLIENT_IP, "192.0.2.10");
		e.setField(Constants.SESSION_ID, "2343");
		e.setField(Constants.ACTION_STATUS, "0");
		e.setField(Constants.EXEC_STATEMENT, "'CREATE TABLE testschema.`test_table` (id INT NOT NULL PRIMARY KEY) ENGINE=InnoDB'");
		e.setField(Constants.DB_NAME, "testschema");
		e.setField(Constants.SERVER_INSTANCE, "test-aurora-mysql-instance-1");
		e.setField(Constants.DB_USER, "testuser");
		e.setField(Constants.AUDIT_ACTION, "QUERY");
		e.setField(Constants.SERVERHOSTNAME, "sampleaccountid_test-aurora-mysql-instance-1");

		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);

		Assert.assertEquals(1, results.size());
		Assert.assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		Assert.assertEquals(1, matchListener.getMatchCount());
	}

	/**
		* Event with empty dbName and no schema extractable from SQL (e.g. select @@version_comment).
		* Conf sets dbName = "NA" as fallback before invoking plugin.
		* Verifies plugin parses the event and produces a GuardRecord (not dropped).
		*/
	@Test
	public void testFieldGuardRecord_emptyDbName_noSchemaInSQL() {

		Context context = new ContextImpl(null, null);
		AuroraMysqlGuardiumPluginFilter filter = new AuroraMysqlGuardiumPluginFilter("test-id", null, context);

		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();

		// Simulates: ...,QUERY,,'select @@version_comment limit 1',0  (empty dbName, no schema in SQL)
		// Conf sets dbName = "NA" fallback before invoking plugin
		e.setField("message", "1638806583399975,test-aurora-mysql-instance-1,testuser,192.0.2.10,2343,184395,QUERY,,'select @@version_comment limit 1',0");
		e.setField(Constants.TIMESTAMP, "1638806583399975");
		e.setField(Constants.CLIENT_IP, "192.0.2.10");
		e.setField(Constants.SESSION_ID, "2343");
		e.setField(Constants.ACTION_STATUS, "0");
		e.setField(Constants.EXEC_STATEMENT, "'select @@version_comment limit 1'");
		e.setField(Constants.DB_NAME, "NA");   // NA fallback set by conf when no schema found in SQL
		e.setField(Constants.SERVER_INSTANCE, "test-aurora-mysql-instance-1");
		e.setField(Constants.DB_USER, "testuser");
		e.setField(Constants.AUDIT_ACTION, "QUERY");
		e.setField(Constants.SERVERHOSTNAME, "sampleaccountid_test-aurora-mysql-instance-1");

		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);

		Assert.assertEquals(1, results.size());
		Assert.assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		Assert.assertEquals(1, matchListener.getMatchCount());
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