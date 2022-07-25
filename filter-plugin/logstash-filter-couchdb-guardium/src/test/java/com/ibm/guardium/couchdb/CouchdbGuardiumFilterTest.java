/*
 Copyright IBM Corp. 2021, 2022 All rights reserved.

SPDX-License-Identifier: Apache-2.0
*/

package com.ibm.guardium.couchdb;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.logstash.plugins.ContextImpl;
import com.ibm.guardium.univer***REMOVED***lconnector.commons.GuardConstants;
import co.elastic.logstash.api.Context;
import co.elastic.logstash.api.Event;
import co.elastic.logstash.api.FilterMatchListener;
import co.elastic.logstash.api.PluginConfigSpec;

public class CouchdbGuardiumFilterTest {

	final static Context context = new ContextImpl(null, null);
	final static CouchdbGuardiumFilter filter = new CouchdbGuardiumFilter("test-id", null, context);

	@Test
	public void testMes***REMOVED***ge() {
		Context context = new ContextImpl(null, null);
		CouchdbGuardiumFilter filter = new CouchdbGuardiumFilter("test-id", null, context);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("application","couchdb");
		e.setField("timestamp","2022-02-21T07:03:10.759000Z");
		e.setField("client_ip","127.0.0.1");
		e.setField("client_port","5984");
		e.setField("server_ip","172.31.12.122");
		e.setField("server_host","ip-172-31-12-122");
		e.setField("description","ok");
		e.setField("username","balram");
		e.setField("verb","GET");
		e.setField("status","200");
		e.setField("timeinterval","30");
		e.setField("logmes***REMOVED***ge","/_all_dbs?startkey=%22%22&endkey=%22%E9%A6%99%22&limit=30");
		e.setField("db_name","_all_dbs");

		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertEquals(1, results.size());
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, matchListener.getMatchCount());

	}
	@Test
	public void testLoginFailed() {
		Context context = new ContextImpl(null, null);
		CouchdbGuardiumFilter filter = new CouchdbGuardiumFilter("test-id", null, context);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("application","couchdb");
		e.setField("timestamp","2022-02-21T07:03:10.759000Z");
		e.setField("client_ip","127.0.0.1");
		e.setField("client_port","5984");
		e.setField("description","ok");
		e.setField("username","undefined");
		e.setField("status","401");
		e.setField("verb","GET");
		e.setField("timeinterval","30");
		e.setField("logmes***REMOVED***ge","recepies");
		e.setField("db_name","recepies");

		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertEquals(1, results.size());
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
	}
	
	@Test
	public void testMes***REMOVED***ge1() {
		Context context = new ContextImpl(null, null);
		CouchdbGuardiumFilter filter = new CouchdbGuardiumFilter("test-id", null, context);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("application","couchdb");
		e.setField("timestamp","2022-02-21T07:03:10.759000Z");
		e.setField("client_ip","127.0.0.1");
		e.setField("client_port","5984");
		e.setField("server_ip","172.31.12.122");
		e.setField("server_host","ip-172-31-12-122");
		e.setField("description","ok");
		e.setField("username","balram");
		e.setField("verb","GET");
		e.setField("status","401");
		e.setField("timeinterval","30");
		e.setField("db_name","_session");

		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertEquals(1, results.size());
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, matchListener.getMatchCount());

	}

	@Test
	public void testClientIp() {
		Context context = new ContextImpl(null, null);
		CouchdbGuardiumFilter filter = new CouchdbGuardiumFilter("test-id", null, context);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("application","couchdb");
		e.setField("timestamp","2022-02-21T07:03:10.759000Z");
		e.setField("description","ok");
		e.setField("username","balram");
		e.setField("verb","GET");
		e.setField("status","401");
		e.setField("timeinterval","30");
		e.setField("db_name","_all_dbs");
		
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertEquals(1, results.size());
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));

	}
	@Test
	public void testmes***REMOVED***ge2() {
		Context context = new ContextImpl(null, null);
		CouchdbGuardiumFilter filter = new CouchdbGuardiumFilter("test-id", null, context);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("application","couchdb");
		e.setField("timestamp","2022-02-21T07:03:10.759000Z");
		e.setField("description","ok");
		e.setField("username","balram");
		e.setField("id","36fa3c0487");
		e.setField("verb",null);
		e.setField("status","401");
		e.setField("timeinterval","30");
		e.setField("db_name","_all_dbs");
		
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertEquals(1, results.size());
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));

	}
	@Test
	public void testStatus() {
		Context context = new ContextImpl(null, null);
		CouchdbGuardiumFilter filter = new CouchdbGuardiumFilter("test-id", null, context);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("application","couchdb");
		e.setField("timestamp","2022-02-21T07:03:10.759000Z");
		e.setField("description","ok");
		e.setField("username","balram");
		e.setField("verb","GET");
		e.setField("server_ip",null);
		e.setField("id",null);
		e.setField("status",null);
		e.setField("timeinterval","30");
		e.setField("db_name","_all_dbs");
		
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertEquals(1, results.size());

	}
	@Test
	public void testTimeStamp() {
		Context context = new ContextImpl(null, null);
		CouchdbGuardiumFilter filter = new CouchdbGuardiumFilter("test-id", null, context);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("application","couchdb");
		e.setField("timestamp",null);
		e.setField("description","ok");
		e.setField("username","balram");
		e.setField("verb","GET");
		e.setField("status","200");
		e.setField("timeinterval","30");
		e.setField("db_name","_all_dbs");
		
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertEquals(1, results.size());
	}
	@Test
	public void testVerb() {
		Context context = new ContextImpl(null, null);
		CouchdbGuardiumFilter filter = new CouchdbGuardiumFilter("test-id", null, context);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("application","couchdb");
		e.setField("timestamp","2022-02-21T07:03:10.759000Z");
		e.setField("description","ok");
		e.setField("username","balram");
		e.setField("verb",null);
		e.setField("status","200");
		e.setField("timeinterval","30");
		e.setField("db_name","_all_dbs");
		
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertEquals(1, results.size());
	}
	@Test
	public void testEmptyMes***REMOVED***ge() {
		Context context = new ContextImpl(null, null);
		CouchdbGuardiumFilter filter = new CouchdbGuardiumFilter("test-id", null, context);
		 ArrayList<Event> events = new ArrayList<>();
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("application","couchdb");
		e.setField("verb",null);
		e.setField("db_name",null);
		events.add(e);
		
		Collection<Event> results = filter.filter(events, matchListener);
		assertEquals(0, results.size());

	}	
	@Test
	public void testVerb1() {
	Context context = new ContextImpl(null, null);
	CouchdbGuardiumFilter filter = new CouchdbGuardiumFilter("test-id", null, context);
	Event e = new org.logstash.Event();
	TestMatchListener matchListener = new TestMatchListener();
	e.setField("application","couchdb");
	e.setField("timestamp","2022-02-21T07:03:10.759000Z");
	e.setField("description","ok");
	e.setField("username","balram");
	e.setField("verb",null);
	e.setField("status","null");
	e.setField("timeinterval","30");
	e.setField("db_name","_all_dbs");

	Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
	assertEquals(1, results.size());
	}
	
	@Test
	public void testVerb2() {
	Context context = new ContextImpl(null, null);
	CouchdbGuardiumFilter filter = new CouchdbGuardiumFilter("test-id", null, context);
	Event e = new org.logstash.Event();
	TestMatchListener matchListener = new TestMatchListener();
	e.setField("application","couchdb");
	e.setField("timestamp","2022-02-21T07:03:10.759000Z");
	e.setField("description","ok");
	e.setField("username","balram");
	e.setField("verb",null);
	e.setField("status","300");
	e.setField("timeinterval","30");
	e.setField("db_name","_all_dbs");

	Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
	assertEquals(1, results.size());
	}	
	
	 @Test 
	    public void testSkippedEvent() {
	        Context context = new ContextImpl(null, null);
	        CouchdbGuardiumFilter filter = new CouchdbGuardiumFilter("test-id", null, context);
	        Event e = new org.logstash.Event();
	        TestMatchListener matchListener = new TestMatchListener();
	        ArrayList<Event> events = new ArrayList<>();
	        e.setField("application","couchdb");
	        e.setField("dbName","_session");
	        e.setField("status", "200");	        
	        events.add(e);
	        
	        Collection<Event> results = filter.filter(events, matchListener);
	        	        
	        assertEquals(0, results.size());

	    }
	 
	 @Test
	 public void testMes***REMOVED***ge2() {
	 Context context = new ContextImpl(null, null);
	 CouchdbGuardiumFilter filter = new CouchdbGuardiumFilter("test-id", null, context);
	 Event e = new org.logstash.Event();
	 ArrayList<Event> events = new ArrayList<>();
	 TestMatchListener matchListener = new TestMatchListener();
	 e.setField("application","couchdb");
	 e.setField("timestamp","2022-02-21T07:03:10.759000Z");
	 e.setField("client_ip","127.0.0.1");
	 e.setField("client_port","5984");
	 e.setField("server_ip","172.31.12.122");
	 e.setField("server_host","ip-172-31-12-122");
	 e.setField("description","ok");
	 e.setField("username","balram");
	 e.setField("verb","GET");
	 e.setField("status","200");
	 e.setField("timeinterval","30");
	 e.setField("db_name","_utils");
	 events.add(e);
	 Collection<Event> results = filter.filter(events, matchListener);
	 assertEquals(0, results.size());



	 }
	 @Test
	 public void testMes***REMOVED***ge3() {
	 Context context = new ContextImpl(null, null);
	 CouchdbGuardiumFilter filter = new CouchdbGuardiumFilter("test-id", null, context);
	 Event e = new org.logstash.Event();
	 ArrayList<Event> events = new ArrayList<>();
	 TestMatchListener matchListener = new TestMatchListener();
	 e.setField("application","couchdb");
	 e.setField("timestamp","2022-02-21T07:03:10.759000Z");
	 e.setField("client_ip","127.0.0.1");
	 e.setField("client_port","5984");
	 e.setField("server_ip","172.31.12.122");
	 e.setField("server_host","ip-172-31-12-122");
	 e.setField("description","ok");
	 e.setField("username","balram");
	 e.setField("verb","GET");
	 e.setField("status","200");
	 e.setField("timeinterval","30");
	 e.setField("db_name","verifytestdb");
	 events.add(e);
	 Collection<Event> results = filter.filter(events, matchListener);
	 assertEquals(0, results.size());



	 }
	 @Test
	 public void testMes***REMOVED***ge4() {
	 Context context = new ContextImpl(null, null);
	 CouchdbGuardiumFilter filter = new CouchdbGuardiumFilter("test-id", null, context);
	 Event e = new org.logstash.Event();
	 ArrayList<Event> events = new ArrayList<>();
	 TestMatchListener matchListener = new TestMatchListener();
	 e.setField("application","couchdb");
	 e.setField("timestamp","2022-02-21T07:03:10.759000Z");
	 e.setField("client_ip","127.0.0.1");
	 e.setField("client_port","5984");
	 e.setField("server_ip","172.31.12.122");
	 e.setField("server_host","ip-172-31-12-122");
	 e.setField("description","ok");
	 e.setField("username","balram");
	 e.setField("verb","GET");
	 e.setField("status","200");
	 e.setField("timeinterval","30");
	 e.setField("db_name","_session");
	 events.add(e);
	 Collection<Event> results = filter.filter(events, matchListener);
	 assertEquals(0, results.size());



	 }
	 @Test
	 public void testMes***REMOVED***ge5() {
	 Context context = new ContextImpl(null, null);
	 CouchdbGuardiumFilter filter = new CouchdbGuardiumFilter("test-id", null, context);
	 Event e = new org.logstash.Event();
	 ArrayList<Event> events = new ArrayList<>();
	 TestMatchListener matchListener = new TestMatchListener();
	 e.setField("application","couchdb");
	 e.setField("timestamp","2022-02-21T07:03:10.759000Z");
	 e.setField("client_ip","127.0.0.1");
	 e.setField("client_port","5984");
	 e.setField("server_ip","172.31.12.122");
	 e.setField("server_host","ip-172-31-12-122");
	 e.setField("description","ok");
	 e.setField("username","balram");
	 e.setField("verb","GET");
	 e.setField("status","200");
	 e.setField("timeinterval","30");
	 e.setField("db_name","verifytestdb_replicate");
	 events.add(e);
	 Collection<Event> results = filter.filter(events, matchListener);
	 assertEquals(0, results.size());



	 }
	 @Test
	 public void testMes***REMOVED***ge6() {
	 Context context = new ContextImpl(null, null);
	 CouchdbGuardiumFilter filter = new CouchdbGuardiumFilter("test-id", null, context);
	 Event e = new org.logstash.Event();
	 ArrayList<Event> events = new ArrayList<>();
	 TestMatchListener matchListener = new TestMatchListener();
	 e.setField("application","couchdb");
	 e.setField("timestamp","2022-02-21T07:03:10.759000Z");
	 e.setField("client_ip","127.0.0.1");
	 e.setField("client_port","5984");
	 e.setField("server_ip","172.31.12.122");
	 e.setField("server_host","ip-172-31-12-122");
	 e.setField("description","ok");
	 e.setField("username","balram");
	 e.setField("verb","GET");
	 e.setField("status","200");
	 e.setField("timeinterval","30");
	 e.setField("db_name","favicon.ico");
	 events.add(e);
	 Collection<Event> results = filter.filter(events, matchListener);
	 assertEquals(0, results.size());



	 }
	 
	 
	 @Test
		public void getIdTest() {
			String id = filter.getId();
			assertNotNull(id);
		}
	 @Test
		public void configSchemaTest() {
			Collection<PluginConfigSpec<?>> events = filter.configSchema();
			assertNotNull(events);
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
