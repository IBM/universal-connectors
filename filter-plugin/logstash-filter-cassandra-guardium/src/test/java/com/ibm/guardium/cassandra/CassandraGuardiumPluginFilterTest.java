//
// Copyright 2020-2021 IBM Inc. All rights reserved
// SPDX-License-Identifier: Apache2.0
//
package com.ibm.guardium.cassandra;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Assert;
import org.junit.Test;
import org.logstash.plugins.ContextImpl;
import com.ibm.guardium.universalconnector.commons.GuardConstants;
import co.elastic.logstash.api.Context;
import co.elastic.logstash.api.Event;
import co.elastic.logstash.api.FilterMatchListener;

public class CassandraGuardiumPluginFilterTest {

	final static Context context = new ContextImpl(null, null);

	/**
	 * To feed Guardium universal connector, a "GuardRecord" field must exist.
	 * 
	 * Filter should add field "GuardRecord" to the Event, which Universal
	 * connector then inserts into Guardium.
	 */

	@SuppressWarnings("unused")
	@Test
	public void testGuardRecord() {
		final String cassandraAuditLog = "INFO  [Native-Transport-Requests-1] 2022-01-31 13:17:17,901 FileAuditLogger.java:51 - user:test|host:localhost/127.0.0.1:7000|source:/127.0.0.1|port:47844|timestamp:1643615237901|type:UNAUTHORIZED_ATTEMPT|category:AUTH|operation:CREATE USER test WITH PASSWORD *******; User test does not have sufficient privileges to perform the requested operation";
	
		
		String serverIP = "1.1.1.1";	
			String serverHostname = "mypc";
		CassandraGuardiumPluginFilter filter = new CassandraGuardiumPluginFilter("test-id", null, context);

		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("message", cassandraAuditLog);
		e.setField(Constants.SERVER_IP, "1.1.1.1");
		e.setField(Constants.SERVER_HOSTNAME, "mypc");
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