//
// Copyright 2020- IBM Inc. All rights reserved
// SPDX-License-Identifier: Apache2.0
//
package com.ibm.guardium.azureSQL;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;
import org.junit.Test;
//import org.logstash.plugins.ConfigurationImpl;
import org.logstash.plugins.ContextImpl;

import com.ibm.guardium.azureSQL.Constants;
import com.ibm.guardium.azureSQL.AzureSQLGuardiumPluginFilter;
import com.ibm.guardium.universalconnector.commons.GuardConstants;

//import co.elastic.logstash.api.Configuration;
import co.elastic.logstash.api.Context;
import co.elastic.logstash.api.Event;
import co.elastic.logstash.api.FilterMatchListener;


public class AzureSQLGuardiumPluginFilterTest {

	final static Context context = new ContextImpl(null, null);
	final static AzureSQLGuardiumPluginFilter filter = new AzureSQLGuardiumPluginFilter("test-id", null, context);
	Event e = new org.logstash.Event();
	
	Event intitalizeEventObject() {	
    	
	    e.setField(Constants.Session_ID, "77");
		e.setField(Constants.TIMESTAMP, "1644335283875860800");
		e.setField(Constants.Client_IP, "194.2.127.16");
		e.setField(Constants.User_Name, "dbadmin");
		e.setField(Constants.STATEMENT, "select * from employee;");
		e.setField(Constants.DATABASE_NAME,"AzureDB");
		e.setField(Constants.APPLICATION_NAME,"SQL SERVER");
		e.setField(Constants.CLIENT_HOST_NAME,"DESKTOP-KJ3D16L");
		e.setField(Constants.Server_Hostname,"test-server-azuresql");
		e.setField(Constants.SUCCEEDED,"true");
		return e;
   }
	
	/**
	 * To feed Guardium universal connector, a "GuardRecord" field must exist.
	 * 
	 * Filter should add field "GuardRecord" to the Event, which Universal connector
	 * then inserts into Guardium.
	 */

	@Test
	public void testGuardRecord() {
		
		Context context = new ContextImpl(null, null);
		AzureSQLGuardiumPluginFilter filter = new AzureSQLGuardiumPluginFilter("test-id", null, context);

		
		TestMatchListener matchListener = new TestMatchListener();

		Event e=intitalizeEventObject();
		
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