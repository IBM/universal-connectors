//
// Copyright 2020- IBM Inc. All rights reserved
// SPDX-License-Identifier: Apache2.0
//
package com.ibm.guardium.saphana;

//import co.elastic.logstash.api.Configuration;
import co.elastic.logstash.api.Context;
import co.elastic.logstash.api.Event;
import co.elastic.logstash.api.FilterMatchListener;
import com.ibm.guardium.universalconnector.commons.GuardConstants;
import com.ibm.guardium.universalconnector.commons.structures.UCRecord;
import org.junit.Assert;
import org.junit.Test;
//import org.logstash.plugins.ConfigurationImpl;
import org.logstash.plugins.ContextImpl;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.gson.Gson;

public class SapHanaGuardiumPluginFilterTest {

	final static Context context = new ContextImpl(null, null);
	final static SapHanaGuardiumPluginFilter filter = new SapHanaGuardiumPluginFilter("test-id", null, context);

	/**
	 * To feed Guardium universal connector, a "GuardRecord" fields must exist.
	 * 
	 * Filter should add field "GuardRecord" to the Event, which Universal connector
	 * then inserts into Guardium.
	 */

	// event string
	@Test
	public void testFieldGuardRecord_saphana() {

		final String s3 = "2021-07-06 07:48:40;indexserver;0b5725d05852;HXE;90;39040;192.168.56.1;DESKTOP-KJ3D16L;7164;56956;policy6;INFO;CONNECT;;;;;;;JOHNNY;UNSUCCESSFUL;;;;;authentication failed at ptime/query/catalog/catalog_authmgr.cc:927;;200131;AVT6J3744;;;;;;UNKNOWN;;;;;;;;;;;;;;;";
		final String s2 = "\\\"34e5478895e9de27c12bf14bd6a945faf4ee3536\\\"";
		Context context = new ContextImpl(null, null);
		SapHanaGuardiumPluginFilter filter = new SapHanaGuardiumPluginFilter("test-id", null, context);

		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();

		e.setField("message", s3);
		e.setField("fingerprint", s2);
		e.setField("ts", "2021-07-06 07:48:40");
		e.setField("Client_IP", "192.168.56.1");
		e.setField("Session_ID", "1234");
		e.setField("Action_Status", "UNSUCCESSFUL");
		e.setField("Client_Port_Number", "5001");
		e.setField("Application_username", "Laxmikant");
		e.setField(Constants.AUDIT_ACTION, "CONNECT");
		e.setField("Executed_Statement", "select * from emp");
		e.setField(Constants.SERVICE_NAME, "indexserver");
		e.setField(Constants.SERVER_HOST, "573ebafe99f7");
		e.setField(Constants.CLIENT_HOST, "DESKTOP-KJ3D16L");
		e.setField(Constants.SOURCE_PROGRAM, "HDBSQL");
		e.setField(Constants.SERVER_PORT, "3901");
		e.setField(Constants.SCHEMA_NAME, "SYSTEM");
		e.setField(Constants.DB_USER, "ABC");
		e.setField(Constants.OFFSET, "-330");
		e.setField(Constants.MIN_OFF,"+05:10");



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