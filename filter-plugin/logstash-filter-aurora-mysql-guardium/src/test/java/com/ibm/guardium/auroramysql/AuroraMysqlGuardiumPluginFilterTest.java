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

import java.util.ArrayList;
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
	 * To check Message contains SQL Error code or if there is any "_grokparsefailure" present in tags
	 **/
	@Test
	public void testSQLError_aurora_mysql() {

		final String message = "2024-11-15T15:36:05.640887Z 3486 [Note] [MY-010914] [Server] Got packets out of order";

		Context context = new ContextImpl(null, null);
		AuroraMysqlGuardiumPluginFilter filter = new AuroraMysqlGuardiumPluginFilter("test-id", null, context);

		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();

		ArrayList<String> tags = new ArrayList<String>();
		tags.add(Constants.GROK_PARSE_FAILURE);

		e.setField(Constants.MESSAGE, message);
		e.setField(Constants.TAGS, tags);
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
		Assert.assertNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));

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