/*
© Copyright IBM Corp. 2021, 2022 All rights reserved.
SPDX-License-Identifier: Apache-2.0
*/
package com.ibm.guardium.documentdb;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.Test;
import org.logstash.plugins.ContextImpl;
import com.google.gson.Gson;
import com.ibm.guardium.universalconnector.commons.GuardConstants;
import com.ibm.guardium.universalconnector.commons.structures.Record;

import co.elastic.logstash.api.Context;
import co.elastic.logstash.api.Event;
import co.elastic.logstash.api.FilterMatchListener;

public class DocumentdbGuardiumFilterTest {



	final static Context context = new ContextImpl(null, null);
	final static  DocumentdbGuardiumFilter filter = new DocumentdbGuardiumFilter("test-id", null, context);

	//Test for No Message string in log
	@Test
	public void testNoMessage() {
		final String DbString = "{\"atype\": \"authenticate\", \"ts\": 1629364658655, \"remote_ip\": \"127.0.0.0:57308\", \"user\": \"\", "
				+ "\"param\": { \"user\": \"serviceadmin\", \"mechanism\": \"SCRAM-SHA-1\", \"success\": true, \"message\": \"\", \"error\": 0 }}";
		DocumentdbGuardiumFilter filter = getGuardiumFilterConnector(DbString);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		//e.setField("message", DbString);
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertEquals(1, results.size());
		assertNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(0, matchListener.getMatchCount());
	}

	//Audit log into GuardRecord
	@Test
	public void testAudit() {
		final String DbString = "{\"atype\": \"authenticate\", \"ts\": 1629364658655, \"remote_ip\": \"127.0.0.0:57308\", \"user\": \"\", "
				+ "\"param\": { \"user\": \"serviceadmin\", \"mechanism\": \"SCRAM-SHA-1\", \"success\": true, \"message\": \"\", \"error\": 0 }}";
		DocumentdbGuardiumFilter filter = getGuardiumFilterConnector(DbString);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("message", DbString);
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertEquals(1, results.size());
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, matchListener.getMatchCount());
	}

	//Profiler log into GuardRecord
	@Test
	public void testProfiler() {
		final String DbString = "{\"op\":\"command\",\"ts\":1641978528311,\"ns\":\"case.cases\",\"command\":{\"aggregate\":\"cases\","
				+ "\"pipeline\":[{\"$match\":{\"Province_State\":\"Washington\",\"Admin2\":\"King\",\"Case_Type\":\"Deaths\"}},"
				+ "{\"$group\":{\"_id\":null,\"sum\":{\"$sum\":\"$Difference\"}}}],\"cursor\":{},\"lsid\":{\"id\":{\"$binary\":\"qIAmkDmpSX2SxQ39xh2lkA==\","
				+ "\"$type\":\"4\"}},\"$db\":\"case\"},\"cursorExhausted\":true,\"nreturned\":1,\"responseLength\":22,\"protocol\":\"op_query\",\"millis\":576,"
				+ "\"planSummary\":\"COLLSCAN\",\"execStats\":{\"stage\":\"SORT_AGGREGATE\",\"nReturned\":\"1\",\"executionTimeMillisEstimate\":\"575.288\","
				+ "\"inputStage\":{\"stage\":\"COLLSCAN\",\"nReturned\":\"70\",\"executionTimeMillisEstimate\":\"575.062\"}},\"client\":\"172.31.40.18:38230\","
				+ "\"appName\":\"MongoDB Shell\",\"user\":\"kirtimalhotra\"}";
		DocumentdbGuardiumFilter filter = getGuardiumFilterConnector(DbString);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("message", DbString);
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertEquals(1, results.size());
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, matchListener.getMatchCount());
	}

	//Audit log with dbName
	@Test
	public void testAuditWithDbName() {
		final String DbString = "{\"atype\":\"createDatabase\",\"ts\":1641456822978,\"remote_ip\":\"172.31.39.255:49548\",\"user\":\"qadbdivya\","
				+ "\"param\":{\"ns\":\"case\"}}";
		DocumentdbGuardiumFilter filter = getGuardiumFilterConnector(DbString);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("message", DbString);
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertEquals(1, results.size());
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, matchListener.getMatchCount());
	}

	//Audit log with Unauthorized exception
	@Test
	public void testAuditWithUnauthorizedException() {
		final String DbString = "{\"atype\":\"authenticate\",\"ts\":1641456822926,\"remote_ip\":\"172.31.39.255:49548\",\"user\":\"\","
				+ "\"param\":{\"user\":\"qadbdivya\",\"mechanism\":\"SCRAM-SHA-1\",\"success\":false,\"message\":\"Unauthorized\",\"error\":13}}";
		DocumentdbGuardiumFilter filter = getGuardiumFilterConnector(DbString);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("message", DbString);
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertEquals(1, results.size());
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, matchListener.getMatchCount());
	}

	//Audit log with Unauthenticated exception
	@Test
	public void testAuditWithUnauthenticatedException() {
		final String DbString = "{\"atype\":\"authenticate\",\"ts\":1641456822926,\"remote_ip\":\"172.31.39.255:49548\",\"user\":\"\","
				+ "\"param\":{\"user\":\"qadbdivya\",\"mechanism\":\"SCRAM-SHA-1\",\"success\":false,\"message\":\"Unauthorized\",\"error\":18}}";
		DocumentdbGuardiumFilter filter = getGuardiumFilterConnector(DbString);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("message", DbString);
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertEquals(1, results.size());
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, matchListener.getMatchCount());
	}

	//Audit log with other exception
	@Test
	public void testAuditWithOtherException() {
		final String DbString = "{\"atype\":\"authenticate\",\"ts\":1641456822926,\"remote_ip\":\"172.31.39.255:49548\",\"user\":\"\","
				+ "\"param\":{\"user\":\"qadbdivya\",\"mechanism\":\"SCRAM-SHA-1\",\"success\":false,\"message\":\"Unauthorized\",\"error\":20}}";
		DocumentdbGuardiumFilter filter = getGuardiumFilterConnector(DbString);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("message", DbString);
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertEquals(1, results.size());
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		//		String jsonInString = new Gson().toJson(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		//		JsonObject jsonObject = new JsonParser().parse(jsonInString).getAsJsonObject();
		assertEquals(1, matchListener.getMatchCount());
	}

	//Audit log with other exception
	@Test
	public void testAuditWithServiceAdminLog() {
		final String DbString = "{\"atype\":\"authenticate\",\"ts\":1641456404670,\"remote_ip\":\"unix_domain_socket\",\"user\":\"\","
				+ "\"param\":{\"user\":\"serviceadmin\",\"mechanism\":\"SCRAM-SHA-1\",\"success\":true,\"message\":\"\",\"error\":0}}";
		DocumentdbGuardiumFilter filter = getGuardiumFilterConnector(DbString);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("message", DbString);
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertEquals(1, results.size());
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		String recordString = e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME).toString();
		Record record = (new Gson()).fromJson(recordString, Record.class);
		assertNotNull(record);
		assertEquals(1, matchListener.getMatchCount());
	}


	//Test Service name
	@Test
	public void testServiceName() {
		final String DbString = "{\"atype\":\"authenticate\",\"ts\":1641456404670,\"remote_ip\":\"unix_domain_socket\",\"user\":\"\","
				+ "\"param\":{\"user\":\"serviceadmin\",\"mechanism\":\"SCRAM-SHA-1\",\"success\":true,\"message\":\"\",\"error\":0}}";
		DocumentdbGuardiumFilter filter = getGuardiumFilterConnector(DbString);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("message", DbString);
		filter.filter(Collections.singletonList(e), matchListener);
		String recordString = e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME).toString();
		Record record = (new Gson()).fromJson(recordString, Record.class);
		assertNotNull(record.getAccessor().getServiceName());
		assertEquals(Parser.SERVER_TYPE_STRING, record.getAccessor().getServiceName());
	}

	//Test DBProtocol
	@Test
	public void testDbProtocol() {
		final String DbString = "{\"atype\":\"authenticate\",\"ts\":1642760998400,\"remote_ip\":\"172.31.45.103:40528\",\"user\":\"\",\"param\":{\"user\":\"kirtimalhotra\",\"mechanism\":\"SCRAM-SHA-1\",\"success\":true,\"message\":\"\",\"error\":0}}";
		DocumentdbGuardiumFilter filter = getGuardiumFilterConnector(DbString);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("message", DbString);
		filter.filter(Collections.singletonList(e), matchListener);
		String recordString = e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME).toString();
		Record record = (new Gson()).fromJson(recordString, Record.class);
		assertNotNull(record.getAccessor().getDbProtocol());
		assertEquals(Parser.DATA_PROTOCOL_STRING, record.getAccessor().getDbProtocol());
	}
	
	//Test SourceProgram with aggregate query
		@Test
		public void testSourceProgram() {
			final String DbString = "{\"op\":\"command\",\"ts\":1642680123143,\"ns\":\"case.cases\",\"command\":{\"aggregate\":\"cases\","
					+ "\"pipeline\":[{\"$match\":{\"Province_State\":\"Washington\",\"Admin2\":\"King\",\"Case_Type\":\"Deaths\"}},"
					+ "{\"$group\":{\"_id\":null,\"sum\":{\"$sum\":\"$Difference\"}}}],\"cursor\":{},\"lsid\":{\"id\":{\"$binary\":\"ZZ8cs9YlSeGif+YRtNuAXQ==\","
					+ "\"$type\":\"4\"}},\"$db\":\"case\"},\"cursorExhausted\":true,\"nreturned\":1,\"responseLength\":22,\"protocol\":\"op_query\","
					+ "\"millis\":577,\"planSummary\":\"COLLSCAN\",\"execStats\":{\"stage\":\"SORT_AGGREGATE\",\"nReturned\":\"1\","
					+ "\"executionTimeMillisEstimate\":\"576.632\",\"inputStage\":{\"stage\":\"COLLSCAN\",\"nReturned\":\"70\","
					+ "\"executionTimeMillisEstimate\":\"576.327\"}},\"client\":\"172.31.45.103:43834\",\"appName\":\"MongoDB Shell\",\"user\":\"kirtimalhotra\"}";
			DocumentdbGuardiumFilter filter = getGuardiumFilterConnector(DbString);
			Event e = new org.logstash.Event();
			TestMatchListener matchListener = new TestMatchListener();
			e.setField("message", DbString);
			filter.filter(Collections.singletonList(e), matchListener);
			String recordString = e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME).toString();
			Record record = (new Gson()).fromJson(recordString, Record.class);
			assertNotNull(record.getAccessor().getSourceProgram());
		}

	private DocumentdbGuardiumFilter getGuardiumFilterConnector(String jsonString) {
		Context context = new ContextImpl(null, null);
		DocumentdbGuardiumFilter filter = new DocumentdbGuardiumFilter("test-id", null, context);
		return filter;
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
