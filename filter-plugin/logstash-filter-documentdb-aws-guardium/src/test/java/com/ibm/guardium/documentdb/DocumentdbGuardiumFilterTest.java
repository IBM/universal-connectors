package com.ibm.guardium.documentdb;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;

 
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;
 
import org.logstash.plugins.ContextImpl;
import com.google.gson.Gson;
import com.ibm.guardium.univer***REMOVED***lconnector.commons.GuardConstants;
import com.ibm.guardium.univer***REMOVED***lconnector.commons.structures.Record;

import co.elastic.logstash.api.Context;
import co.elastic.logstash.api.Event;
import co.elastic.logstash.api.FilterMatchListener;
import co.elastic.logstash.api.PluginConfigSpec;

import static org.junit.jupiter.api.Assertions.*;

//import static org.junit.Assert.*;

import static org.junit.jupiter.api.Assertions.*;

//import static org.junit.Assert.*;

public class DocumentdbGuardiumFilterTest {



	final static Context context = new ContextImpl(null, null);
	final static  DocumentdbGuardiumFilter filter = new DocumentdbGuardiumFilter("test-id", null, context);

	//Test for No Mes***REMOVED***ge string in log
	@Test
	public void testNoMes***REMOVED***ge() {
		final String DbString = "{\"atype\": \"authenticate\", \"ts\": 1629364658655, \"remote_ip\": \"127.0.0.0:57308\", \"user\": \"\", "
				+ "\"param\": { \"user\": \"serviceadmin\", \"mechanism\": \"SCRAM-SHA-1\", \"success\": true, \"mes***REMOVED***ge\": \"\", \"error\": 0 }}";
		DocumentdbGuardiumFilter filter = getGuardiumFilterConnector(DbString);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertNotNull(results);		
	}

	//Audit log into GuardRecord
	@Test
	public void testAudit() {
		final String DbString = "{ \"atype\": \"createDatabase\", \"ts\": 1629364973923, \"remote_ip\": \"172.31.32.149:57308\", \"user\": \"documentdbMaster\", \"param\": { \"ns\": \"test\" } }";
		DocumentdbGuardiumFilter filter = getGuardiumFilterConnector(DbString);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("mes***REMOVED***ge", DbString);
		e.setField("serverHostnamePrefix","LP-123455556");
		e.setField("event_id", "1234567");
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertEquals(1, results.size());
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, matchListener.getMatchCount());
	}
	@Test
	public void testCreateUser() {
		final String DbString = "{\"atype\":\"createUser\",\"ts\":1658838066630,\"remote_ip\":\"172.31.43.39:54086\",\"user\":\"QAtest\",\"param\":{\"userName\":\"Test20\",\"roles\":[]}}";
		DocumentdbGuardiumFilter filter = getGuardiumFilterConnector(DbString);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("mes***REMOVED***ge", DbString);
		e.setField("serverHostnamePrefix","LP-123455556");
		e.setField("event_id", "1234567");
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertEquals(1, results.size());
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, matchListener.getMatchCount());
	}
	@Test
	public void testinsert() {
		final String DbString = "{ \"op\": \"insert\", \"ts\": 1660112876973, \"ns\": \"testvb.uccollection\", \"command\": { \"insert\": \"uccollection\", \"ordered\": true, \"$db\": \"testvb\", \"lsid\": { \"id\": { \"$binary\": \"z7uTEHISSGifEUqWokGB6w==\", \"$type\": \"4\" } }, \"txnNumber\": 3 }, \"nInserted\": 1, \"protocol\": \"op_query\", \"millis\": 881, \"client\": \"172.31.93.131:36636\", \"user\": \"shyeotik\" }";
		DocumentdbGuardiumFilter filter = getGuardiumFilterConnector(DbString);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("mes***REMOVED***ge", DbString);
		e.setField("serverHostnamePrefix","LP-123455556");
		e.setField("event_id", "1234567");
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertEquals(1, results.size());
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, matchListener.getMatchCount());
	}
	@Test
	public void testfind() {
		final String DbString = "{\"op\":\"query\",\"ts\":1660792890340,\"ns\":\"SandeepTest.cases\",\"command\":{\"find\":\"cases\",\"filter\":{\"firstName\":\"QA\"},\"$db\":\"SandeepTest\",\"lsid\":{\"id\":{\"$binary\":\"2pEWwQ6zQ5ucpeqDnE7c3w==\",\"$type\":\"4\"}},\"$readPreference\":{\"mode\":\"primaryPreferred\"}},\"cursorExhausted\":true,\"nreturned\":1,\"responseLength\":365,\"protocol\":\"op_query\",\"millis\":570,\"planSummary\":\"COLLSCAN\",\"execStats\":{\"stage\":\"COLLSCAN\",\"nReturned\":\"1\",\"executionTimeMillisEstimate\":\"568.830\"},\"client\":\"172.31.40.165:40670\",\"user\":\"***REMOVED***ndeep\"}";
		DocumentdbGuardiumFilter filter = getGuardiumFilterConnector(DbString);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("mes***REMOVED***ge", DbString);
		e.setField("serverHostnamePrefix","LP-123455556");
		e.setField("event_id", "1234567");
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertEquals(1, results.size());
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, matchListener.getMatchCount());
	}
	@Test
	public void testRemove() {
		final String DbString = "{\"op\":\"remove\",\"ts\":1660803667048,\"ns\":\"SandeepTest.cases\",\"command\":{\"q\":{\"lastName\":\"Test\"},\"limit\":0},\"nRemoved\":1,\"protocol\":\"op_query\",\"millis\":106,\"planSummary\":\"COLLSCAN\",\"execStats\":{\"stage\":\"DELETE\",\"nReturned\":\"0\",\"executionTimeMillisEstimate\":\"102.257\",\"inputStage\":{\"stage\":\"COLLSCAN\",\"nReturned\":\"1\",\"executionTimeMillisEstimate\":\"102.208\"}},\"client\":\"172.31.40.165:43460\",\"user\":\"***REMOVED***ndeep\"}";
		DocumentdbGuardiumFilter filter = getGuardiumFilterConnector(DbString);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("mes***REMOVED***ge", DbString);
		e.setField("serverHostnamePrefix","LP-123455556");
		e.setField("event_id", "1234567");
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertEquals(1, results.size());
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, matchListener.getMatchCount());
	}
	@Test
	public void testcount() {
		final String DbString = "{\"op\":\"command\",\"ts\":1660803665486,\"ns\":\"SandeepTest.cases\",\"command\":{\"count\":\"cases\",\"query\":{\"firstName\":\"QA\"},\"$db\":\"SandeepTest\",\"lsid\":{\"id\":{\"$binary\":\"iR4WATpmQFKJq1hw30i1hQ==\",\"$type\":\"4\"}},\"$readPreference\":{\"mode\":\"primaryPreferred\"}},\"protocol\":\"op_query\",\"millis\":76,\"planSummary\":\"COLLSCAN\",\"execStats\":{\"stage\":\"AGGREGATE\",\"nReturned\":\"1\",\"executionTimeMillisEstimate\":\"76.069\",\"inputStage\":{\"stage\":\"COLLSCAN\",\"nReturned\":\"1\",\"executionTimeMillisEstimate\":\"76.060\"}},\"client\":\"172.31.40.165:43460\",\"user\":\"***REMOVED***ndeep\"}";
		DocumentdbGuardiumFilter filter = getGuardiumFilterConnector(DbString);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("mes***REMOVED***ge", DbString);
		e.setField("serverHostnamePrefix","LP-123455556");
		e.setField("event_id", "1234567");
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertEquals(1, results.size());
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, matchListener.getMatchCount());
	}
	@Test
	public void testSkip() {
		final String DbString = "{\"op\":\"command\",\"ts\":1660803665486,\"ns\":\"SandeepTest.\",\"command\":{\"count\":\"cases\",\"query\":{\"firstName\":\"QA\"},\"$db\":\"SandeepTest\",\"lsid\":{\"id\":{\"$binary\":\"iR4WATpmQFKJq1hw30i1hQ==\",\"$type\":\"4\"}},\"$readPreference\":{\"mode\":\"primaryPreferred\"}},\"protocol\":\"op_query\",\"millis\":76,\"planSummary\":\"COLLSCAN\",\"execStats\":{\"stage\":\"AGGREGATE\",\"nReturned\":\"1\",\"executionTimeMillisEstimate\":\"76.069\",\"inputStage\":{\"stage\":\"COLLSCAN\",\"nReturned\":\"1\",\"executionTimeMillisEstimate\":\"76.060\"}},\"client\":\"172.31.40.165:43460\",\"user\":\"***REMOVED***ndeep\"}";
		DocumentdbGuardiumFilter filter = getGuardiumFilterConnector(DbString);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("mes***REMOVED***ge", DbString);
		e.setField("serverHostnamePrefix","LP-123455556");
		e.setField("event_id", "1234567");
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertEquals(1, results.size());				
	}
	@Test
	public void testskip() {
		final String DbString = "{\"atype\": \"authenticate\", \"ts\": 1629364658655, \"remote_ip\": \"127.0.0.0:57308\", \"user\": \"\", "
				+ "\"param\": { \"user\": \"serviceadmin\", \"mechanism\": \"SCRAM-SHA-1\", \"success\": true, \"mes***REMOVED***ge\": \"\", \"error\": 0 }}";
		DocumentdbGuardiumFilter filter = getGuardiumFilterConnector(DbString);
		Event e = new org.logstash.Event();
		ArrayList<Event> events = new ArrayList<>();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("mes***REMOVED***ge","DbString");
		events.add(e);
		Collection<Event> results = filter.filter(events, matchListener);
		assertEquals(1, results.size());

	}
	//Audit log into GuardRecord
		@Test
		public void testmes***REMOVED***ge() {
			final String DbString = "{ \"atype\": \"createDatabase\", \"ts\": 1629364973923, \"remote_ip\": \"172.31.32.149:57308\", \"user\": \"documentdbMaster\", \"param\": { \"ns\": \"\" } }";
			DocumentdbGuardiumFilter filter = getGuardiumFilterConnector(DbString);
			Event e = new org.logstash.Event();
			ArrayList<Event> events = new ArrayList<>();
			TestMatchListener matchListener = new TestMatchListener();
			e.setField("mes***REMOVED***ge", DbString);
			e.setField("serverHostnamePrefix","LP-123455556");
			e.setField("event_id", "1234567");
			events.add(e);
			Collection<Event> results = filter.filter(events, matchListener);
			assertEquals(0, results.size());
		}
		@Test
		public void testmes***REMOVED***ge2() {
			final String DbString = "{\"atype\":\"createUser\",\"ts\":1658838066630,\"remote_ip\":\"172.31.43.39:54086\",\"user\":\"QAtest\",\"param\":{\"userName\":\"Test20\",\"roles\":[]}}";
			DocumentdbGuardiumFilter filter = getGuardiumFilterConnector(DbString);
			Event e = new org.logstash.Event();
			ArrayList<Event> events = new ArrayList<>();
			TestMatchListener matchListener = new TestMatchListener();
			e.setField("mes***REMOVED***ge", DbString);
			e.setField("serverHostnamePrefix","LP-123455556");
			e.setField("event_id", "1234567");
			events.add(e);
			Collection<Event> results = filter.filter(events, matchListener);
			assertEquals(1, results.size());
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
		e.setField("mes***REMOVED***ge", DbString);
		e.setField("serverHostnamePrefix","LP-123455556");
		e.setField("event_id", "1234567");
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertEquals(1, results.size());
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, matchListener.getMatchCount());
	}
		@Test
		public void testProfiler1() {
			final String DbString = "{\"op\":\"command\",\"ts\":1641978528311,\"ns\":\"\",\"command\":{\"aggregate\":\"cases\","
					+ "\"pipeline\":[{\"$match\":{\"Province_State\":\"Washington\",\"Admin2\":\"King\",\"Case_Type\":\"Deaths\"}},"
					+ "{\"$group\":{\"_id\":null,\"sum\":{\"$sum\":\"$Difference\"}}}],\"cursor\":{},\"lsid\":{\"id\":{\"$binary\":\"qIAmkDmpSX2SxQ39xh2lkA==\","
					+ "\"$type\":\"4\"}},\"$db\":\"case\"},\"cursorExhausted\":true,\"nreturned\":1,\"responseLength\":22,\"protocol\":\"op_query\",\"millis\":576,"
					+ "\"planSummary\":\"COLLSCAN\",\"execStats\":{\"stage\":\"SORT_AGGREGATE\",\"nReturned\":\"1\",\"executionTimeMillisEstimate\":\"575.288\","
					+ "\"inputStage\":{\"stage\":\"COLLSCAN\",\"nReturned\":\"70\",\"executionTimeMillisEstimate\":\"575.062\"}},\"client\":\"172.31.40.18:38230\","
					+ "\"appName\":\"MongoDB Shell\",\"user\":\"kirtimalhotra\"}";
			DocumentdbGuardiumFilter filter = getGuardiumFilterConnector(DbString);
			Event e = new org.logstash.Event();
			ArrayList<Event> events = new ArrayList<>();
			TestMatchListener matchListener = new TestMatchListener();
			e.setField("mes***REMOVED***ge", DbString);
			e.setField("serverHostnamePrefix","LP-123455556");
			e.setField("event_id", "1234567");
			events.add(e);
			Collection<Event> results = filter.filter(events, matchListener);
			assertEquals(0, results.size());
		}
	//Audit log with dbName
	@Test
	public void testAuditWithDbName() {
		final String DbString = "{\"atype\":\"createDatabase\",\"ts\":1641456822978,\"remote_ip\":\"172.31.39.255:49548\",\"user\":\"qadbdivya\","
				+ "\"param\":{\"ns\":\"case\"}}";
		DocumentdbGuardiumFilter filter = getGuardiumFilterConnector(DbString);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("mes***REMOVED***ge", DbString);
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertEquals(1, results.size());
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, matchListener.getMatchCount());
	}
		@Test
		public void testAudit2() {
			final String DbString = "{\r\n"
					+ "    \"atype\": \"authenticate\",\r\n"
					+ "    \"ts\": 1643364584563,\r\n"
					+ "    \"remote_ip\": \"172.31.45.103:44418\",\r\n"
					+ "    \"user\": \"\",\r\n"
					+ "    \"param\": {\r\n"
					+ "        \"user\": \"kirtimalhotraa\",\r\n"
					+ "        \"mechanism\": \"SCRAM-SHA-1\",\r\n"
					+ "        \"success\": false,\r\n"
					+ "        \"mes***REMOVED***ge\": \"User does not exist\",\r\n"
					+ "        \"error\": 18\r\n"
					+ "    }\r\n"
					+ "}";
			DocumentdbGuardiumFilter filter = getGuardiumFilterConnector(DbString);
			Event e = new org.logstash.Event();
			TestMatchListener matchListener = new TestMatchListener();
			e.setField("mes***REMOVED***ge", DbString);
			Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
			assertEquals(1, results.size());
			assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
			assertEquals(1, matchListener.getMatchCount());
		}

	
	//Audit log with Unauthorized exception
	@Test
	public void testAuditWithUnauthorizedException() {
		final String DbString = "{\"atype\":\"authenticate\",\"ts\":1641456822926,\"remote_ip\":\"172.31.39.255:49548\",\"user\":\"\","
				+ "\"param\":{\"user\":\"qadbdivya\",\"mechanism\":\"SCRAM-SHA-1\",\"success\":false,\"mes***REMOVED***ge\":\"Unauthorized\",\"error\":13}}";
		DocumentdbGuardiumFilter filter = getGuardiumFilterConnector(DbString);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("mes***REMOVED***ge", DbString);
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertEquals(1, results.size());
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, matchListener.getMatchCount());
	}

	//Audit log with Unauthenticated exception
	@Test
	public void testAuditWithUnauthenticatedException() {
		final String DbString = "{\"atype\":\"authenticate\",\"ts\":1641456822926,\"remote_ip\":\"172.31.39.255:49548\",\"user\":\"\","
				+ "\"param\":{\"user\":\"qadbdivya\",\"mechanism\":\"SCRAM-SHA-1\",\"success\":false,\"mes***REMOVED***ge\":\"Unauthorized\",\"error\":18}}";
		DocumentdbGuardiumFilter filter = getGuardiumFilterConnector(DbString);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("mes***REMOVED***ge", DbString);
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);		
		assertEquals(1, results.size());
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, matchListener.getMatchCount());
	}

	//Audit log with other exception
	@Test
	public void testAuditWithOtherException() {
		final String DbString = "{\"atype\":\"authenticate\",\"ts\":1641456822926,\"remote_ip\":\"172.31.39.255:49548\",\"user\":\"\","
				+ "\"param\":{\"user\":\"qadbdivya\",\"mechanism\":\"SCRAM-SHA-1\",\"success\":false,\"mes***REMOVED***ge\":\"Unauthorized\",\"error\":20}}";
		DocumentdbGuardiumFilter filter = getGuardiumFilterConnector(DbString);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("mes***REMOVED***ge", DbString);
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertEquals(1, results.size());
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, matchListener.getMatchCount());
	}

	//Audit log with other exception
	@Test
	public void testAuditWithServiceAdminLog() {
		final String DbString = "{\"atype\":\"createDatabase\",\"ts\":1641456822978,\"remote_ip\":\"172.31.39.255:49548\",\"user\":\"qadbdivya\","
				+ "\"param\":{\"ns\":\"case.auto\"}}";
		DocumentdbGuardiumFilter filter = getGuardiumFilterConnector(DbString);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("mes***REMOVED***ge", DbString);
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
		final String DbString = "{ \"atype\": \"createDatabase\", \"ts\": 1629364973923, \"remote_ip\": \"172.31.32.149:57308\", \"user\": \"documentdbMaster\", \"param\": { \"ns\": \"test\" } }";
		DocumentdbGuardiumFilter filter = getGuardiumFilterConnector(DbString);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("mes***REMOVED***ge", DbString);
		e.setField("serverHostnamePrefix","LP-123455556");
		e.setField("event_id", "1234567");
		filter.filter(Collections.singletonList(e), matchListener);
		String recordString = e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME).toString();
		Record record = (new Gson()).fromJson(recordString, Record.class);
		assertNotNull(record.getAccessor().getServiceName());
		assertEquals("LP-123455556:test", record.getAccessor().getServiceName());
	}

	//Test DBProtocol
	@Test
	public void testDbProtocol() {
		final String DbString = "{ \"atype\": \"createDatabase\", \"ts\": 1629364973923, \"remote_ip\": \"172.31.32.149:57308\", \"user\": \"documentdbMaster\", \"param\": { \"ns\": \"test\" } }";
		DocumentdbGuardiumFilter filter = getGuardiumFilterConnector(DbString);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("mes***REMOVED***ge", DbString);
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
			e.setField("mes***REMOVED***ge", DbString);
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
