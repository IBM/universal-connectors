//
// Copyright 2020- IBM Inc. All rights reserved
// SPDX-License-Identifier: Apache2.0
//
package com.ibm.guardium.couchbasedb;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;
import org.junit.Test;
//import org.logstash.plugins.ConfigurationImpl;
import org.logstash.plugins.ContextImpl;

import com.ibm.guardium.univer***REMOVED***lconnector.commons.GuardConstants;

//import co.elastic.logstash.api.Configuration;
import co.elastic.logstash.api.Context;
import co.elastic.logstash.api.Event;
import co.elastic.logstash.api.FilterMatchListener;


public class CouchbasedbGuardiumPluginFilterTest {

	final static Context context = new ContextImpl(null, null);
	final static CouchbasedbGuardiumPluginFilter filter = new CouchbasedbGuardiumPluginFilter("test-id", null, context);

	/**
	 * To feed Guardium univer***REMOVED***l connector, a "GuardRecord" field must exist.
	 * 
	 * Filter should add field "GuardRecord" to the Event, which Univer***REMOVED***l connector
	 * then inserts into Guardium.
	 */

	// N1QL Audit log fired thru WebConsole
	@Test
	public void testGuardRecord() {
		final String couchbaseAuditLog = "{\"clientContextId\":\"5c5476f0-e46d-4a6a-a19b-982a5fea056f\",\"description\":\"A N1QL SELECT statement was executed\",\"id\":28672,\"isAdHoc\":true,\"metrics\":{\"elapsedTime\":\"29.1335ms\",\"executionTime\":\"29.0167ms\",\"resultCount\":3,\"resultSize\":217,\"sortCount\":3},\"name\":\"SELECT statement\",\"node\":\"127.0.0.1:8091\",\"real_userid\":{\"domain\":\"builtin\",\"user\":\"Administrator\"},\"remote\":{\"ip\":\"127.0.0.1\",\"port\":40389},\"requestId\":\"f5592288-044f-453b-bbba-dc7d49e4aeb4\",\"statement\":\"SELECT name,phone FROM `travel-***REMOVED***mple` WHERE type=\\\"hotel\\\" AND city=\\\"Manchester\\\" and directions IS NOT MISSING ORDER BY name LIMIT 10;\",\"status\":\"success\",\"timestamp\":\"2021-06-22T12:00:21.444Z\",\"userAgent\":\"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/90.0.4430.212 Safari/537.36 (Couchbase Query Workbench (6.6.2-9588-enterprise))\"}";

		// Configuration config = new
		// ConfigurationImpl(Collections.singletonMap("source", sourceField));
		Context context = new ContextImpl(null, null);
		CouchbasedbGuardiumPluginFilter filter = new CouchbasedbGuardiumPluginFilter("test-id", null, context);

		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();

		e.setField("mes***REMOVED***ge", couchbaseAuditLog);
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