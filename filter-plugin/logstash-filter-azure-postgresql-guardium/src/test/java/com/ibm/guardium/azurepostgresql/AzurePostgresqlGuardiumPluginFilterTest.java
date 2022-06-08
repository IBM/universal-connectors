/*
#Copyright 2020-2021 IBM Inc. All rights reserved
#SPDX-License-Identifier: Apache-2.0
#*/
package com.ibm.guardium.azurepostgresql;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Assert;
import org.junit.Test;
import org.logstash.plugins.ContextImpl;

import com.ibm.guardium.azurepostgresql.Constants;
import com.ibm.guardium.azurepostgresql.AzurePostgresqlGuardiumPluginFilter;
import com.ibm.guardium.univer***REMOVED***lconnector.commons.GuardConstants;
import co.elastic.logstash.api.Context;
import co.elastic.logstash.api.Event;
import co.elastic.logstash.api.FilterMatchListener;

public class AzurePostgresqlGuardiumPluginFilterTest {

	final static Context context = new ContextImpl(null, null);

	final static AzurePostgresqlGuardiumPluginFilter filter = new AzurePostgresqlGuardiumPluginFilter("test-id", null, context);

	Event e = new org.logstash.Event();

	Event intitalizeEventObject() {

		e.setField(Constants.SESSION_ID, "7420");
		e.setField(Constants.TIMESTAMP, "2022-02-23 13:20:29");
		e.setField(Constants.CLIENT_IP, "183.87.117.29");
		e.setField(Constants.USER_NAME, "postgres");
		e.setField(Constants.STATEMENT, "select * from employee;");
		e.setField(Constants.DATABASE_NAME, "postgres");
		e.setField(Constants.SERVER_HOSTNAME,"1234567_mypssqlserver");
		e.setField(Constants.APPLICATION_NAME, "psql");
		e.setField(Constants.SUCCEEDED, "LOG");
		return e;
	}

	/**
	 * To feed Guardium univer***REMOVED***l connector, a "GuardRecord" field must exist.
	 * 
	 * Filter should add field "GuardRecord" to the Event, which Univer***REMOVED***l connector
	 * then inserts into Guardium.
	 */

	@Test
	public void testGuardRecord() {

		Context context = new ContextImpl(null, null);

		AzurePostgresqlGuardiumPluginFilter filter = new AzurePostgresqlGuardiumPluginFilter("test-id", null, context);

		TestMatchListener matchListener = new TestMatchListener();

		Event e = intitalizeEventObject();

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