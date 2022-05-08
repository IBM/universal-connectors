//
// Copyright 2020-2021 IBM Inc. All rights reserved
// SPDX-License-Identifier: Apache2.0
//
package com.ibm.guardium.generic;

//import co.elastic.logstash.api.Configuration;
import co.elastic.logstash.api.Context;
import co.elastic.logstash.api.Event;
import co.elastic.logstash.api.FilterMatchListener;

import com.ibm.guardium.generic.Constants;
import com.ibm.guardium.generic.GenericGuardiumFilter;
import com.ibm.guardium.universalconnector.commons.GuardConstants;
import com.ibm.guardium.universalconnector.commons.structures.Record;
import org.junit.Assert;
import org.junit.Test;
//import org.logstash.plugins.ConfigurationImpl;
import org.logstash.plugins.ContextImpl;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.gson.Gson;

public class GenericGuardiumFilterTest {

	final static String postgresString = "2020-10-22 11:53:26 UTC:183.87.237.49(63661):postgres@testDB:[17796]:LOG:  00000: AUDIT: SESSION,27,1,WRITE,DELETE,,,DELETE FROM employee WHERE emp_id  = 101,<none>";
	final static Context context = new ContextImpl(null, null);
	final static GenericGuardiumFilter filter = new GenericGuardiumFilter("test-id", null, context);

	/**
	 * To feed Guardium universal connector, a "GuardRecord" fields must exist.
	 * 
	 * Filter should add field "GuardRecord" to the Event, which Universal connector
	 * then inserts into Guardium.
	 */
	@Test
	public void testFieldMessage_rds() {
		final String postgresString2 = "2020-10-22 11:53:26 UTC:183.87.237.49(63661):postgres@testDB:[17796]:LOG:  00000: AUDIT: SESSION,27,1,WRITE,DELETE,,,DELETE FROM employee WHERE emp_id  = 101,<none>";

		// Configuration config = new ConfigurationImpl(Collections.singletonMap("source", sourceField));
		
		Context context = new ContextImpl(null, null);
		GenericGuardiumFilter filter = new GenericGuardiumFilter("test-id", null, context);

		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();

		e.setField("message", postgresString2);
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);

		Assert.assertEquals(1, results.size());

	}

	
	 @Test public void testFieldGuardRecord_rds() { 
		 
	 final String rdsString = "2020-10-22 11:53:26 UTC:183.87.237.49(63661):postgres@testDB:[17796]:LOG:  00000: AUDIT: SESSION,27,1,WRITE,DELETE,,,DELETE FROM employee WHERE emp_id  = 101,<none>\\r\\n2020-10-22 11:53:26 UTC:183.87.237.49(63661):postgres@testDB:[17796]:LOCATION:  log_audit_event, pgaudit.c:760";
	  
	  
	  // Configuration config = new ConfigurationImpl(Collections.singletonMap("source", sourceField)); 
	 
	  Context context = new ContextImpl(null, null); 
	  GenericGuardiumFilter filter = new GenericGuardiumFilter("test-id", null, context);
	  
	  Event e = new org.logstash.Event(); 
	  TestMatchListener matchListener = new TestMatchListener();
	  
	  e.setField("message", rdsString); 
	  e.setField(Constants.TIMESTAMP, "2020-10-22 11:53:26 UTC");
	  e.setField(Constants.CLIENTIP, "183.87.237.49");
	  e.setField(Constants.SESSIONID, "1234");
	  e.setField(Constants.CLIENTPORT, "63661");
	  e.setField(Constants.APPUSERNAME, "NA");
	  e.setField(Constants.ORIGINALSQLCOMMAND, "select * from emp");
	  e.setField(Constants.DBNAME, "testDB");
	  e.setField(Constants.DBUSER, "postgres");
		
	  Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
	  
	  Assert.assertEquals(1, results.size());
	  Assert.assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME)); 
	  
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