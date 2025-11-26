//
// Copyright 2020- IBM Inc. All rights reserved
// SPDX-License-Identifier: Apache2.0
//
package com.ibm.guardium.teradatadb;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;
import org.junit.Test;
//import org.logstash.plugins.ConfigurationImpl;
import org.logstash.plugins.ContextImpl;

import com.ibm.guardium.universalconnector.commons.GuardConstants;

//import co.elastic.logstash.api.Configuration;
import co.elastic.logstash.api.Context;
import co.elastic.logstash.api.Event;
import co.elastic.logstash.api.FilterMatchListener;


public class TeradatadbGuardiumPluginFilterTest {

	final static Context context = new ContextImpl(null, null);
	final static TeradatadbGuardiumPluginFilter filter = new TeradatadbGuardiumPluginFilter("test-id", null, context);
	Event e = new org.logstash.Event();
	
	Event intitalizeEventObject() {
	   	
	    e.setField(Constants.SESSION_ID, "6968");
		e.setField(Constants.TIME_FIELD, "2021-11-16T07:49:41.220Z");
		e.setField(Constants.CLIENT_IP, "194.2.127.16");
		e.setField(Constants.SERVER_IP, "10.0.0.1");
		e.setField(Constants.USER_NAME, "SYSDBA");
		e.setField(Constants.SERVER_HOSTNAME, "server.com");
		e.setField(Constants.SQL_TEXT_INFO, "select * from employee;");
		e.setField(Constants.ERROR_TEXT, null);
		e.setField(Constants.LOGON_SOURCE, "(TCP/IP) c089 194.2.127.16 DBS-TERADATA1620.COM;DB-TERA CID=2D39989 "
				+ "AVT666744 JDBC17.10.00.14;1.8.0_202 01 LSS");
		e.setField(Constants.OS_USER, "TESTUSER");
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
		TeradatadbGuardiumPluginFilter filter = new TeradatadbGuardiumPluginFilter("test-id", null, context);

		
		TestMatchListener matchListener = new TestMatchListener();

		Event e=intitalizeEventObject();
		
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);

		Assert.assertEquals(1, results.size());
		Assert.assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		Assert.assertEquals(1, matchListener.getMatchCount());
	}
	
	/**
	 * Test to verify that Server IP is properly parsed from the event
	 * and included in the SessionLocator of the GuardRecord.
	 * This test validates the ServerIPAddrByServer field retrieval
	 * from DBC.QryLogClientAttrV view as per Teradata support recommendation.
	 */
	@Test
	public void testServerIPParsing() throws Exception {
		Event e = intitalizeEventObject();
		
		// Parse the record using the Parser
		com.ibm.guardium.universalconnector.commons.structures.Record record = Parser.parseRecord(e);
		
		// Verify SessionLocator contains the correct Server IP
		Assert.assertNotNull("SessionLocator should not be null", record.getSessionLocator());
		Assert.assertEquals("Server IP should match the value from ServerIPAddrByServer field",
							"10.0.0.1",
							record.getSessionLocator().getServerIp());
		
		// Verify Client IP is also correctly set
		Assert.assertEquals("Client IP should match the value from ClientIPAddrByClient field",
							"194.2.127.16",
							record.getSessionLocator().getClientIp());
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