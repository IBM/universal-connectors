/*
* � Copyright IBM Corp. 2021, 2022 All rights reserved.
* SPDX-License-Identifier: Apache-2.0
*/

package com.ibm.neptune.connector;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.logstash.plugins.ConfigurationImpl;

import com.ibm.guardium.univer***REMOVED***lconnector.commons.GuardConstants;
import com.ibm.neptune.connector.constant.ApplicationConstantTest;

import co.elastic.logstash.api.Configuration;
import co.elastic.logstash.api.Event;
import co.elastic.logstash.api.FilterMatchListener;

public class NeptuneGuardiumFilterTest {

	private static Configuration CONFIG = new ConfigurationImpl(
			Collections.singletonMap(ApplicationConstantTest.CONFIG_KEY, ApplicationConstantTest.CONFIG_VALUE));

	private static NeptuneGuardiumFilter FILTER = new NeptuneGuardiumFilter(ApplicationConstantTest.ID, CONFIG, null);

	private TestMatchListener matchListener;

	/**
	 * this method invoke before the each test cases and create new
	 * TestMatchListener object every time before invoke each test cases.
	 */
	@BeforeEach
	public void beforeEach() {

		matchListener = new TestMatchListener();

	}

	/**
	 * this method for test the filter, when log data is of staus type.
	 */
	@Test
	public void filterTestForStatus1() {

		Event event = new org.logstash.Event();
		List<Event> events = new ArrayList<>();
		events.add(event);
		event.setField(ApplicationConstantTest.LOG_MESSAGE_KEY, ApplicationConstantTest.LOG_MESSAGE);
		event.setField(ApplicationConstantTest.TIME_STAMP_KEY, ApplicationConstantTest.TIME_STAMP);
		event.setField(ApplicationConstantTest.SERVER_HOST_KEY, ApplicationConstantTest.SERVER_HOST);
		event.setField(ApplicationConstantTest.CLIENT_HOST_KEY, ApplicationConstantTest.CLIENT_HOST);
		event.setField(ApplicationConstantTest.HTTP_HEADERS_KEY, ApplicationConstantTest.HTTP_HEADERS);
		event.setField(ApplicationConstantTest.PAYLOAD_KEY, ApplicationConstantTest.PAYLOAD);
		event.setField(ApplicationConstantTest.CALLERIAM_KEY, ApplicationConstantTest.CALLERIAM);
		event.setField(ApplicationConstantTest.SERVER_HOSTNAME_PREFIX_KEY,
				ApplicationConstantTest.SERVER_HOSTNAME_PREFIX);
		event.setField(ApplicationConstantTest.DBNAME_PREFIX_KEY, ApplicationConstantTest.DBNAME_PREFIX);

		Collection<Event> actualResponse = FILTER.filter(events, matchListener);

		assertNotNull(actualResponse);
		assertEquals(ApplicationConstantTest.TIME_STAMP, event.getField(ApplicationConstantTest.TIME_STAMP_KEY));
		assertEquals(ApplicationConstantTest.SERVER_HOST, event.getField(ApplicationConstantTest.SERVER_HOST_KEY));
		assertEquals(ApplicationConstantTest.CLIENT_HOST, event.getField(ApplicationConstantTest.CLIENT_HOST_KEY));
		assertEquals(ApplicationConstantTest.HTTP_HEADERS, event.getField(ApplicationConstantTest.HTTP_HEADERS_KEY));
		assertEquals(ApplicationConstantTest.PAYLOAD, event.getField(ApplicationConstantTest.PAYLOAD_KEY));
		assertEquals(0, actualResponse.size());
		assertEquals(0, matchListener.getMatchCount());

		assertNull(event.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));

	}

	/**
	 * this method for test the filter, when log data is of status type.
	 */
	@Test
	public void filterTestForStatus2() {

		final String auditString = "1629182102678, 172.31.20.251:47148, [unknown], HTTP_GET, [unknown], [unknown], \"HttpObjectAggregator$AggregatedFullHttpRequest(decodeResult: success, version: HTTP/1.1, content: CompositeByteBuf(ridx: 0, widx: 0, cap: 0, components=0)) GET /status HTTP/1.1 Host: database2-neptune.cluster-cllxmp6vknzx.ap-south-1.neptune.amazonaws.com:8182 Accept-Encoding: identity User-Agent: python-urllib3/1.26.5 content-length: 0\", /status";

		Event event = new org.logstash.Event();
		List<Event> events = new ArrayList<>();
		events.add(event);
		event.setField(ApplicationConstantTest.LOG_MESSAGE_KEY, auditString);
		event.setField(ApplicationConstantTest.CLIENT_HOST_KEY, "172.31.20.251:47148");
		event.setField(ApplicationConstantTest.SERVER_HOST_KEY, "[unknown]");
		event.setField(ApplicationConstantTest.TIME_STAMP_KEY, "1629182102678");
		event.setField(ApplicationConstantTest.HTTP_HEADERS_KEY,
				"HttpObjectAggregator$AggregatedFullHttpRequest(decodeResult: success, version: HTTP/1.1, content: CompositeByteBuf(ridx: 0, widx: 0, cap: 0, components=0)) GET /status HTTP/1.1 Host: database2-neptune.cluster-cllxmp6vknzx.ap-south-1.neptune.amazonaws.com:8182 Accept-Encoding: identity User-Agent: python-urllib3/1.26.5 content-length: 0");
		event.setField(ApplicationConstantTest.PAYLOAD_KEY, "/status");
		event.setField(ApplicationConstantTest.CALLERIAM_KEY, "[unknown]");
		event.setField(ApplicationConstantTest.SERVER_HOSTNAME_PREFIX_KEY,
				ApplicationConstantTest.SERVER_HOSTNAME_PREFIX);
		event.setField(ApplicationConstantTest.DBNAME_PREFIX_KEY, ApplicationConstantTest.DBNAME_PREFIX);

		Collection<Event> response = FILTER.filter(events, matchListener);

		assertNull(event.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(0, response.size());
		assertEquals(0, matchListener.getMatchCount());

	}

	/**
	 * this method for test the filter, when log data is of gremlin type which does
	 * not containing HttpHeader.
	 */
	@Test
	public void filterTestForGremlin3() {

		final String auditString = "1629182128935, 172.31.20.251:47158, 172.31.29.87:8182, Websocket, [unknown], [unknown], [unknown], \"RequestMes***REMOVED***ge{, requestId=bd265eaa-32cf-4c11-8901-a8000272ad92, op='eval', processor='', args={gremlin= g.V().valueMap().limit(10) , aliases={g=g}}}\"";

		Event event = new org.logstash.Event();
		List<Event> events = new ArrayList<>();
		events.add(event);
		event.setField(ApplicationConstantTest.LOG_MESSAGE_KEY, auditString);
		event.setField(ApplicationConstantTest.CLIENT_HOST_KEY, "172.31.20.251:47158");
		event.setField(ApplicationConstantTest.SERVER_HOST_KEY, "172.31.29.87:8182");
		event.setField(ApplicationConstantTest.TIME_STAMP_KEY, "1629182126435");
		event.setField(ApplicationConstantTest.HTTP_HEADERS_KEY, "[unknown]");
		event.setField(ApplicationConstantTest.PAYLOAD_KEY,
				"RequestMes***REMOVED***ge{, requestId=bd265eaa-32cf-4c11-8901-a8000272ad92, op='eval', processor='', args={gremlin= g.V().valueMap().limit(10) , aliases={g=g}}}");

		event.setField(ApplicationConstantTest.CALLERIAM_KEY, "[unknown]");
		event.setField(ApplicationConstantTest.SERVER_HOSTNAME_PREFIX_KEY,
				ApplicationConstantTest.SERVER_HOSTNAME_PREFIX);
		event.setField(ApplicationConstantTest.DBNAME_PREFIX_KEY, ApplicationConstantTest.DBNAME_PREFIX);

		Collection<Event> response = FILTER.filter(events, matchListener);

		assertNotNull(event.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, response.size());
		assertEquals(1, matchListener.getMatchCount());

	}

	@Test
	public void filterTestForGremlin6() {

		final String auditString = "1629182126440, 172.31.20.251:47156, 172.31.29.87:8182, Websocket, [unknown], [unknown], [unknown], \"RequestMes***REMOVED***ge{, requestId=ddab9942-c7f9-406b-b26a-7cd02f3d06f1, op='eval', processor='', args={gremlin= g.V('10').property('name','marko') , aliases={g=g}}}\"";

		Event event = new org.logstash.Event();
		List<Event> events = new ArrayList<>();
		events.add(event);
		event.setField(ApplicationConstantTest.LOG_MESSAGE_KEY, auditString);
		event.setField(ApplicationConstantTest.CLIENT_HOST_KEY, "172.31.20.251:47156");
		event.setField(ApplicationConstantTest.SERVER_HOST_KEY, "172.31.29.87:8182");
		event.setField(ApplicationConstantTest.TIME_STAMP_KEY, "1629182126440");
		event.setField(ApplicationConstantTest.HTTP_HEADERS_KEY, "[unknown]");
		event.setField(ApplicationConstantTest.PAYLOAD_KEY,
				"RequestMes***REMOVED***ge{, requestId=ddab9942-c7f9-406b-b26a-7cd02f3d06f1, op='eval', processor='', args={gremlin= g.V('10').property('name','marko') , aliases={g=g}}}");
		event.setField(ApplicationConstantTest.CALLERIAM_KEY, "[unknown]");
		event.setField(ApplicationConstantTest.SERVER_HOSTNAME_PREFIX_KEY,
				ApplicationConstantTest.SERVER_HOSTNAME_PREFIX);
		event.setField(ApplicationConstantTest.DBNAME_PREFIX_KEY, ApplicationConstantTest.DBNAME_PREFIX);

		Collection<Event> response = FILTER.filter(events, matchListener);

		assertNotNull(event.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, response.size());
		assertEquals(1, matchListener.getMatchCount());

	}

	@Test
	public void filterTestForGremlin7() {

		final String auditString = "1629182126440, 172.31.20.251:47156, 172.31.29.87:8182, Websocket, [unknown], [unknown], [unknown], \"RequestMes***REMOVED***ge{, requestId=ddab9942-c7f9-406b-b26a-7cd02f3d06f1, op='eval', processor='', args={gremlin= g.V().drop() , aliases={g=g}}}\"";

		Event event = new org.logstash.Event();
		List<Event> events = new ArrayList<>();
		events.add(event);
		event.setField(ApplicationConstantTest.LOG_MESSAGE_KEY, auditString);
		event.setField(ApplicationConstantTest.CLIENT_HOST_KEY, "172.31.20.251:47156");
		event.setField(ApplicationConstantTest.SERVER_HOST_KEY, "172.31.29.87:8182");
		event.setField(ApplicationConstantTest.TIME_STAMP_KEY, "1629182126440");
		event.setField(ApplicationConstantTest.HTTP_HEADERS_KEY, "[unknown]");
		event.setField(ApplicationConstantTest.PAYLOAD_KEY,
				"RequestMes***REMOVED***ge{, requestId=ddab9942-c7f9-406b-b26a-7cd02f3d06f1, op='eval', processor='', args={gremlin= g.V().drop() , aliases={g=g}}}");
		event.setField(ApplicationConstantTest.CALLERIAM_KEY, "[unknown]");
		event.setField(ApplicationConstantTest.SERVER_HOSTNAME_PREFIX_KEY,
				ApplicationConstantTest.SERVER_HOSTNAME_PREFIX);
		event.setField(ApplicationConstantTest.DBNAME_PREFIX_KEY, ApplicationConstantTest.DBNAME_PREFIX);

		Collection<Event> response = FILTER.filter(events, matchListener);

		assertNotNull(event.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, response.size());
		assertEquals(1, matchListener.getMatchCount());

	}

	@Test
	public void filterTestForGremlin8() {

		final String auditString = "1629182126440, 172.31.20.251:47156, 172.31.29.87:8182, Websocket, [unknown], [unknown], [unknown], \"RequestMes***REMOVED***ge{, requestId=ddab9942-c7f9-406b-b26a-7cd02f3d06f1, op='eval', processor='', args={gremlin= g.addE('knows').from(V('1')).to(V('2')) , aliases={g=g}}}\"";

		Event event = new org.logstash.Event();
		List<Event> events = new ArrayList<>();
		events.add(event);
		event.setField(ApplicationConstantTest.LOG_MESSAGE_KEY, auditString);
		event.setField(ApplicationConstantTest.CLIENT_HOST_KEY, "172.31.20.251:47156");
		event.setField(ApplicationConstantTest.SERVER_HOST_KEY, "172.31.29.87:8182");
		event.setField(ApplicationConstantTest.TIME_STAMP_KEY, "1629182126440");
		event.setField(ApplicationConstantTest.HTTP_HEADERS_KEY, "[unknown]");
		event.setField(ApplicationConstantTest.PAYLOAD_KEY,
				"RequestMes***REMOVED***ge{, requestId=ddab9942-c7f9-406b-b26a-7cd02f3d06f1, op='eval', processor='', args={gremlin= g.addE('knows').from(V('1')).to(V('2')) , aliases={g=g}}}");
		event.setField(ApplicationConstantTest.CALLERIAM_KEY, "[unknown]");
		event.setField(ApplicationConstantTest.SERVER_HOSTNAME_PREFIX_KEY,
				ApplicationConstantTest.SERVER_HOSTNAME_PREFIX);
		event.setField(ApplicationConstantTest.DBNAME_PREFIX_KEY, ApplicationConstantTest.DBNAME_PREFIX);

		Collection<Event> response = FILTER.filter(events, matchListener);

		assertNotNull(event.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, response.size());
		assertEquals(1, matchListener.getMatchCount());

	}

	@Test
	public void filterTestForGremlin9() {

		final String auditString = "1629182126440, 172.31.20.251:47156, 172.31.29.87:8182, Websocket, [unknown], [unknown], [unknown], \"RequestMes***REMOVED***ge{, requestId=ddab9942-c7f9-406b-b26a-7cd02f3d06f1, op='eval', processor='', args={gremlin= g.V().hasLabel('Person') , aliases={g=g}}}\"";

		Event event = new org.logstash.Event();
		List<Event> events = new ArrayList<>();
		events.add(event);
		event.setField(ApplicationConstantTest.LOG_MESSAGE_KEY, auditString);
		event.setField(ApplicationConstantTest.CLIENT_HOST_KEY, "172.31.20.251:47156");
		event.setField(ApplicationConstantTest.SERVER_HOST_KEY, "172.31.29.87:8182");
		event.setField(ApplicationConstantTest.TIME_STAMP_KEY, "1629182126440");
		event.setField(ApplicationConstantTest.HTTP_HEADERS_KEY, "[unknown]");
		event.setField(ApplicationConstantTest.PAYLOAD_KEY,
				"RequestMes***REMOVED***ge{, requestId=ddab9942-c7f9-406b-b26a-7cd02f3d06f1, op='eval', processor='', args={gremlin= g.V().hasLabel('Person') , aliases={g=g}}}");
		event.setField(ApplicationConstantTest.CALLERIAM_KEY, "[unknown]");
		event.setField(ApplicationConstantTest.SERVER_HOSTNAME_PREFIX_KEY,
				ApplicationConstantTest.SERVER_HOSTNAME_PREFIX);
		event.setField(ApplicationConstantTest.DBNAME_PREFIX_KEY, ApplicationConstantTest.DBNAME_PREFIX);

		Collection<Event> response = FILTER.filter(events, matchListener);

		assertNotNull(event.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, response.size());
		assertEquals(1, matchListener.getMatchCount());

	}

	@Test
	public void filterTestForGremlin10() {

		final String auditString = "1629182126440, 172.31.20.251:47156, 172.31.29.87:8182, Websocket, [unknown], [unknown], [unknown], \"RequestMes***REMOVED***ge{, requestId=ddab9942-c7f9-406b-b26a-7cd02f3d06f1, op='eval', processor='', args={gremlin= g.V('10').count() , aliases={g=g}}}\"";

		Event event = new org.logstash.Event();
		List<Event> events = new ArrayList<>();
		events.add(event);
		event.setField(ApplicationConstantTest.LOG_MESSAGE_KEY, auditString);
		event.setField(ApplicationConstantTest.CLIENT_HOST_KEY, "172.31.20.251:47156");
		event.setField(ApplicationConstantTest.SERVER_HOST_KEY, "172.31.29.87:8182");
		event.setField(ApplicationConstantTest.TIME_STAMP_KEY, "1629182126440");
		event.setField(ApplicationConstantTest.HTTP_HEADERS_KEY, "[unknown]");
		event.setField(ApplicationConstantTest.PAYLOAD_KEY,
				"RequestMes***REMOVED***ge{, requestId=ddab9942-c7f9-406b-b26a-7cd02f3d06f1, op='eval', processor='', args={gremlin= g.V('10').count() , aliases={g=g}}}");
		event.setField(ApplicationConstantTest.CALLERIAM_KEY, "[unknown]");
		event.setField(ApplicationConstantTest.SERVER_HOSTNAME_PREFIX_KEY,
				ApplicationConstantTest.SERVER_HOSTNAME_PREFIX);
		event.setField(ApplicationConstantTest.DBNAME_PREFIX_KEY, ApplicationConstantTest.DBNAME_PREFIX);

		Collection<Event> response = FILTER.filter(events, matchListener);

		assertNotNull(event.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, response.size());
		assertEquals(1, matchListener.getMatchCount());

	}

	@Test
	public void filterTestForGremlin11() {

		final String auditString = "1629182126440, 172.31.20.251:47156, 172.31.29.87:8182, Websocket, [unknown], [unknown], [unknown], \"RequestMes***REMOVED***ge{, requestId=ddab9942-c7f9-406b-b26a-7cd02f3d06f1, op='eval', processor='', args={gremlin= g.V().hasId('10') , aliases={g=g}}}\"";

		Event event = new org.logstash.Event();
		List<Event> events = new ArrayList<>();
		events.add(event);
		event.setField(ApplicationConstantTest.LOG_MESSAGE_KEY, auditString);
		event.setField(ApplicationConstantTest.CLIENT_HOST_KEY, "172.31.20.251:47156");
		event.setField(ApplicationConstantTest.SERVER_HOST_KEY, "172.31.29.87:8182");
		event.setField(ApplicationConstantTest.TIME_STAMP_KEY, "1629182126440");
		event.setField(ApplicationConstantTest.HTTP_HEADERS_KEY, "[unknown]");
		event.setField(ApplicationConstantTest.PAYLOAD_KEY,
				"RequestMes***REMOVED***ge{, requestId=ddab9942-c7f9-406b-b26a-7cd02f3d06f1, op='eval', processor='', args={gremlin= g.V().hasId('10') , aliases={g=g}}}");
		event.setField(ApplicationConstantTest.CALLERIAM_KEY, "[unknown]");
		event.setField(ApplicationConstantTest.SERVER_HOSTNAME_PREFIX_KEY,
				ApplicationConstantTest.SERVER_HOSTNAME_PREFIX);
		event.setField(ApplicationConstantTest.DBNAME_PREFIX_KEY, ApplicationConstantTest.DBNAME_PREFIX);

		Collection<Event> response = FILTER.filter(events, matchListener);

		assertNotNull(event.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, response.size());
		assertEquals(1, matchListener.getMatchCount());

	}

	@Test
	public void filterTestForGremlin12() {

		final String auditString = "1629182126440, 172.31.20.251:47156, 172.31.29.87:8182, Websocket, [unknown], [unknown], [unknown], \"RequestMes***REMOVED***ge{, requestId=ddab9942-c7f9-406b-b26a-7cd02f3d06f1, op='eval', processor='', args={gremlin= g.addV('Person') , aliases={g=g}}}\"";

		Event event = new org.logstash.Event();
		List<Event> events = new ArrayList<>();
		events.add(event);
		event.setField(ApplicationConstantTest.LOG_MESSAGE_KEY, auditString);
		event.setField(ApplicationConstantTest.CLIENT_HOST_KEY, "172.31.20.251:47156");
		event.setField(ApplicationConstantTest.SERVER_HOST_KEY, "172.31.29.87:8182");
		event.setField(ApplicationConstantTest.TIME_STAMP_KEY, "1629182126440");
		event.setField(ApplicationConstantTest.HTTP_HEADERS_KEY, "[unknown]");
		event.setField(ApplicationConstantTest.PAYLOAD_KEY,
				"RequestMes***REMOVED***ge{, requestId=ddab9942-c7f9-406b-b26a-7cd02f3d06f1, op='eval', processor='', args={gremlin= g.addV('Person') , aliases={g=g}}}");
		event.setField(ApplicationConstantTest.CALLERIAM_KEY, "[unknown]");
		event.setField(ApplicationConstantTest.SERVER_HOSTNAME_PREFIX_KEY,
				ApplicationConstantTest.SERVER_HOSTNAME_PREFIX);
		event.setField(ApplicationConstantTest.DBNAME_PREFIX_KEY, ApplicationConstantTest.DBNAME_PREFIX);

		Collection<Event> response = FILTER.filter(events, matchListener);

		assertNotNull(event.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, response.size());
		assertEquals(1, matchListener.getMatchCount());

	}

	@Test
	public void filterTestForGremlin13() {

		final String auditString = "1629182126440, 172.31.20.251:47156, 172.31.29.87:8182, Websocket, [unknown], [unknown], [unknown], \"RequestMes***REMOVED***ge{, requestId=ddab9942-c7f9-406b-b26a-7cd02f3d06f1, op='eval', processor='', args={gremlin= g.addE('knows').from(V('1')).to(V('4')).property('weight', 1.0) , aliases={g=g}}}\"";

		Event event = new org.logstash.Event();
		List<Event> events = new ArrayList<>();
		events.add(event);
		event.setField(ApplicationConstantTest.LOG_MESSAGE_KEY, auditString);
		event.setField(ApplicationConstantTest.CLIENT_HOST_KEY, "172.31.20.251:47156");
		event.setField(ApplicationConstantTest.SERVER_HOST_KEY, "172.31.29.87:8182");
		event.setField(ApplicationConstantTest.TIME_STAMP_KEY, "1629182126440");
		event.setField(ApplicationConstantTest.HTTP_HEADERS_KEY, "[unknown]");
		event.setField(ApplicationConstantTest.PAYLOAD_KEY,
				"RequestMes***REMOVED***ge{, requestId=ddab9942-c7f9-406b-b26a-7cd02f3d06f1, op='eval', processor='', args={gremlin= g.addE('knows').from(V('1')).to(V('4')).property('weight', 1.0) , aliases={g=g}}}");
		event.setField(ApplicationConstantTest.CALLERIAM_KEY, "[unknown]");
		event.setField(ApplicationConstantTest.SERVER_HOSTNAME_PREFIX_KEY,
				ApplicationConstantTest.SERVER_HOSTNAME_PREFIX);
		event.setField(ApplicationConstantTest.DBNAME_PREFIX_KEY, ApplicationConstantTest.DBNAME_PREFIX);

		Collection<Event> response = FILTER.filter(events, matchListener);

		assertNotNull(event.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, response.size());
		assertEquals(1, matchListener.getMatchCount());

	}

	@Test
	public void filterTestForGremlin14() {

		final String auditString = "1629182126440, 172.31.20.251:47156, 172.31.29.87:8182, Websocket, [unknown], [unknown], [unknown], \"RequestMes***REMOVED***ge{, requestId=ddab9942-c7f9-406b-b26a-7cd02f3d06f1, op='eval', processor='', args={gremlin= g.V('1','2','3','4') , aliases={g=g}}}\"";

		Event event = new org.logstash.Event();
		List<Event> events = new ArrayList<>();
		events.add(event);
		event.setField(ApplicationConstantTest.LOG_MESSAGE_KEY, auditString);
		event.setField(ApplicationConstantTest.CLIENT_HOST_KEY, "172.31.20.251:47156");
		event.setField(ApplicationConstantTest.SERVER_HOST_KEY, "172.31.29.87:8182");
		event.setField(ApplicationConstantTest.TIME_STAMP_KEY, "1629182126440");
		event.setField(ApplicationConstantTest.HTTP_HEADERS_KEY, "[unknown]");
		event.setField(ApplicationConstantTest.PAYLOAD_KEY,
				"RequestMes***REMOVED***ge{, requestId=ddab9942-c7f9-406b-b26a-7cd02f3d06f1, op='eval', processor='', args={gremlin= g.V('1','2','3','4') , aliases={g=g}}}");
		event.setField(ApplicationConstantTest.CALLERIAM_KEY, "[unknown]");
		event.setField(ApplicationConstantTest.SERVER_HOSTNAME_PREFIX_KEY,
				ApplicationConstantTest.SERVER_HOSTNAME_PREFIX);
		event.setField(ApplicationConstantTest.DBNAME_PREFIX_KEY, ApplicationConstantTest.DBNAME_PREFIX);

		Collection<Event> response = FILTER.filter(events, matchListener);

		assertNotNull(event.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, response.size());
		assertEquals(1, matchListener.getMatchCount());

	}

	@Test
	public void filterTestForGremlin15() {

		final String auditString = "1629182126440, 172.31.20.251:47156, 172.31.29.87:8182, Websocket, [unknown], [unknown], [unknown], \"RequestMes***REMOVED***ge{, requestId=5ff3ff67-0f76-45fd-aa36-7165f96a3ebf, op='eval', processor='', args={gremlin= g.V().hasLabel('User').has('name', 'Terry').as('user')   .bothE('FRIEND')         .has('strength', P.gt(1)).otherV()     .aggregate('friends')   .bothE('FRIEND')     .has('strength', P.gt(1)).otherV()     .where(P.neq('user')).where(P.without('friends'))   .groupCount().by('name')   .order(Scope.local).by(values, Order.decr)   .next() , aliases={g=g}}}\"";

		Event event = new org.logstash.Event();
		List<Event> events = new ArrayList<>();
		events.add(event);
		event.setField(ApplicationConstantTest.LOG_MESSAGE_KEY, auditString);
		event.setField(ApplicationConstantTest.CLIENT_HOST_KEY, "172.31.20.251:47156");
		event.setField(ApplicationConstantTest.SERVER_HOST_KEY, "172.31.29.87:8182");
		event.setField(ApplicationConstantTest.TIME_STAMP_KEY, "1629182126440");
		event.setField(ApplicationConstantTest.HTTP_HEADERS_KEY, "[unknown]");
		event.setField(ApplicationConstantTest.PAYLOAD_KEY,
				"RequestMes***REMOVED***ge{, requestId=5ff3ff67-0f76-45fd-aa36-7165f96a3ebf, op='eval', processor='', args={gremlin= g.V().hasLabel('User').has('name', 'Terry').as('user')   .bothE('FRIEND')         .has('strength', P.gt(1)).otherV()     .aggregate('friends')   .bothE('FRIEND')     .has('strength', P.gt(1)).otherV()     .where(P.neq('user')).where(P.without('friends'))   .groupCount().by('name')   .order(Scope.local).by(values, Order.decr)   .next() , aliases={g=g}}}");
		event.setField(ApplicationConstantTest.CALLERIAM_KEY, "[unknown]");
		event.setField(ApplicationConstantTest.SERVER_HOSTNAME_PREFIX_KEY,
				ApplicationConstantTest.SERVER_HOSTNAME_PREFIX);
		event.setField(ApplicationConstantTest.DBNAME_PREFIX_KEY, ApplicationConstantTest.DBNAME_PREFIX);

		Collection<Event> response = FILTER.filter(events, matchListener);

		assertNotNull(event.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, response.size());
		assertEquals(1, matchListener.getMatchCount());

	}

	@Test
	public void filterTestForGremlin16() {

		final String auditString = "1629182126440, 172.31.20.251:47156, 172.31.29.87:8182, Websocket, [unknown], [unknown], [unknown], \"RequestMes***REMOVED***ge{, requestId=2c694a3c-5ccd-4806-854a-681183763d7d, op='eval', processor='', args={gremlin= g.V().hasLabel('User').has('name', 'Terry').as('user'). both('FRIEND').aggregate('friends'). both('FRIEND').where(P.neq('user')).where(P.without('friends')).   groupCount().by('name').     order(Scope.local).by(values, Order.decr).   next() , aliases={g=g}}}\"";

		Event event = new org.logstash.Event();
		List<Event> events = new ArrayList<>();
		events.add(event);
		event.setField(ApplicationConstantTest.LOG_MESSAGE_KEY, auditString);
		event.setField(ApplicationConstantTest.CLIENT_HOST_KEY, "172.31.20.251:47156");
		event.setField(ApplicationConstantTest.SERVER_HOST_KEY, "172.31.29.87:8182");
		event.setField(ApplicationConstantTest.TIME_STAMP_KEY, "1629182126440");
		event.setField(ApplicationConstantTest.HTTP_HEADERS_KEY, "[unknown]");
		event.setField(ApplicationConstantTest.PAYLOAD_KEY,
				"RequestMes***REMOVED***ge{, requestId=2c694a3c-5ccd-4806-854a-681183763d7d, op='eval', processor='', args={gremlin= g.V().hasLabel('User').has('name', 'Terry').as('user'). both('FRIEND').aggregate('friends'). both('FRIEND').where(P.neq('user')).where(P.without('friends')).   groupCount().by('name').     order(Scope.local).by(values, Order.decr).   next() , aliases={g=g}}}");
		event.setField(ApplicationConstantTest.CALLERIAM_KEY, "[unknown]");
		event.setField(ApplicationConstantTest.SERVER_HOSTNAME_PREFIX_KEY,
				ApplicationConstantTest.SERVER_HOSTNAME_PREFIX);
		event.setField(ApplicationConstantTest.DBNAME_PREFIX_KEY, ApplicationConstantTest.DBNAME_PREFIX);

		Collection<Event> response = FILTER.filter(events, matchListener);

		assertNotNull(event.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, response.size());
		assertEquals(1, matchListener.getMatchCount());

	}

	@Test
	public void filterTestForGremlin17() {

		final String auditString = "1629182126440, 172.31.20.251:47156, 172.31.29.87:8182, Websocket, [unknown], [unknown], [unknown], \"RequestMes***REMOVED***ge{, requestId=4e81b2cc-fa2c-4e13-b8cf-dda3699426a2, op='eval', processor='', args={gremlin=g.V().hasLabel('Team').   group().     by('founded').     by('name').   order(local).     by(keys).   unfold() , aliases={g=g}}}\"";

		Event event = new org.logstash.Event();
		List<Event> events = new ArrayList<>();
		events.add(event);
		event.setField(ApplicationConstantTest.LOG_MESSAGE_KEY, auditString);
		event.setField(ApplicationConstantTest.CLIENT_HOST_KEY, "172.31.20.251:47156");
		event.setField(ApplicationConstantTest.SERVER_HOST_KEY, "172.31.29.87:8182");
		event.setField(ApplicationConstantTest.TIME_STAMP_KEY, "1629182126440");
		event.setField(ApplicationConstantTest.HTTP_HEADERS_KEY, "[unknown]");
		event.setField(ApplicationConstantTest.PAYLOAD_KEY,
				"RequestMes***REMOVED***ge{, requestId=4e81b2cc-fa2c-4e13-b8cf-dda3699426a2, op='eval', processor='', args={gremlin=g.V().hasLabel('Team').   group().     by('founded').     by('name').   order(local).     by(keys).   unfold() , aliases={g=g}}}");
		event.setField(ApplicationConstantTest.CALLERIAM_KEY, "[unknown]");
		event.setField(ApplicationConstantTest.SERVER_HOSTNAME_PREFIX_KEY,
				ApplicationConstantTest.SERVER_HOSTNAME_PREFIX);
		event.setField(ApplicationConstantTest.DBNAME_PREFIX_KEY, ApplicationConstantTest.DBNAME_PREFIX);
		Collection<Event> response = FILTER.filter(events, matchListener);

		assertNotNull(event.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, response.size());
		assertEquals(1, matchListener.getMatchCount());

	}

	@Test
	public void filterTestForGremlin18() {

		final String auditString = "1629182126440, 172.31.20.251:47156, 172.31.29.87:8182, Websocket, [unknown], [unknown], [unknown], \"RequestMes***REMOVED***ge{, requestId=c7647fd5-b97a-480c-a943-14b98a3037a2, op='eval', processor='', args={gremlin=g.V().hasLabel('Stadium').   order().     by('capacity',desc).   valueMap('name','capacity').     by(unfold()) , aliases={g=g}}}\"";

		Event event = new org.logstash.Event();
		List<Event> events = new ArrayList<>();
		events.add(event);
		event.setField(ApplicationConstantTest.LOG_MESSAGE_KEY, auditString);
		event.setField(ApplicationConstantTest.CLIENT_HOST_KEY, "172.31.20.251:47156");
		event.setField(ApplicationConstantTest.SERVER_HOST_KEY, "172.31.29.87:8182");
		event.setField(ApplicationConstantTest.TIME_STAMP_KEY, "1629182126440");
		event.setField(ApplicationConstantTest.HTTP_HEADERS_KEY, "[unknown]");
		event.setField(ApplicationConstantTest.PAYLOAD_KEY,
				"RequestMes***REMOVED***ge{, requestId=c7647fd5-b97a-480c-a943-14b98a3037a2, op='eval', processor='', args={gremlin=g.V().hasLabel('Stadium').   order().     by('capacity',desc).   valueMap('name','capacity').     by(unfold()) , aliases={g=g}}}");
		event.setField(ApplicationConstantTest.CALLERIAM_KEY, "[unknown]");
		event.setField(ApplicationConstantTest.SERVER_HOSTNAME_PREFIX_KEY,
				ApplicationConstantTest.SERVER_HOSTNAME_PREFIX);
		event.setField(ApplicationConstantTest.DBNAME_PREFIX_KEY, ApplicationConstantTest.DBNAME_PREFIX);

		Collection<Event> response = FILTER.filter(events, matchListener);

		assertNotNull(event.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, response.size());
		assertEquals(1, matchListener.getMatchCount());

	}

	@Test
	public void filterTestForGremlin19() {

		final String auditString = "1629182126440, 172.31.20.251:47156, 172.31.29.87:8182, Websocket, [unknown], [unknown], [unknown], \"RequestMes***REMOVED***ge{, requestId=63caece9-be34-4fd3-b573-c24327b43365, op='eval', processor='', args={gremlin= g.V().hasLabel('User').has('name', 'Terry').as('user')   .bothE('FRIEND')         .has('strength', P.gt(1)).otherV()     .aggregate('friends')   .bothE('FRIEND')     .has('strength', P.gt(1)).otherV()     .where(P.neq('user')).where(P.without('friends'))   .groupCount().by('name')   .order(Scope.local).by(values, decr)   .next() , aliases={g=g}}}\"";

		Event event = new org.logstash.Event();
		List<Event> events = new ArrayList<>();
		events.add(event);
		event.setField(ApplicationConstantTest.LOG_MESSAGE_KEY, auditString);
		event.setField(ApplicationConstantTest.CLIENT_HOST_KEY, "172.31.20.251:47156");
		event.setField(ApplicationConstantTest.SERVER_HOST_KEY, "172.31.29.87:8182");
		event.setField(ApplicationConstantTest.TIME_STAMP_KEY, "1629182126440");
		event.setField(ApplicationConstantTest.HTTP_HEADERS_KEY, "[unknown]");
		event.setField(ApplicationConstantTest.PAYLOAD_KEY,
				"RequestMes***REMOVED***ge{, requestId=63caece9-be34-4fd3-b573-c24327b43365, op='eval', processor='', args={gremlin= g.V().hasLabel('User').has('name', 'Terry').as('user')   .bothE('FRIEND')         .has('strength', P.gt(1)).otherV()     .aggregate('friends')   .bothE('FRIEND')     .has('strength', P.gt(1)).otherV()     .where(P.neq('user')).where(P.without('friends'))   .groupCount().by('name')   .order(Scope.local).by(values, decr)   .next() , aliases={g=g}}}");
		event.setField(ApplicationConstantTest.CALLERIAM_KEY, "[unknown]");
		event.setField(ApplicationConstantTest.SERVER_HOSTNAME_PREFIX_KEY,
				ApplicationConstantTest.SERVER_HOSTNAME_PREFIX);
		event.setField(ApplicationConstantTest.DBNAME_PREFIX_KEY, ApplicationConstantTest.DBNAME_PREFIX);
		Collection<Event> response = FILTER.filter(events, matchListener);

		assertNotNull(event.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, response.size());
		assertEquals(1, matchListener.getMatchCount());

	}

	@Test
	public void filterTestForGremlin20() {

		final String auditString = "1629182126440, 172.31.20.251:47156, 172.31.29.87:8182, Websocket, [unknown], [unknown], [unknown], \"RequestMes***REMOVED***ge{, requestId=0d762b5e-0401-4e6e-927b-7c1229c0bf75, op='eval', processor='', args={gremlin= g.addV(\"\"person\"\").property(\"\"name\"\", 'dan')  .addV('person').property('name', 'mike')  .addV('person').property('name', '***REMOVED***ikiran') , aliases={g=g}}}\"";

		Event event = new org.logstash.Event();
		List<Event> events = new ArrayList<>();
		events.add(event);
		event.setField(ApplicationConstantTest.LOG_MESSAGE_KEY, auditString);
		event.setField(ApplicationConstantTest.CLIENT_HOST_KEY, "172.31.20.251:47156");
		event.setField(ApplicationConstantTest.SERVER_HOST_KEY, "172.31.29.87:8182");
		event.setField(ApplicationConstantTest.TIME_STAMP_KEY, "1629182126440");
		event.setField(ApplicationConstantTest.HTTP_HEADERS_KEY, "[unknown]");
		event.setField(ApplicationConstantTest.PAYLOAD_KEY,
				"RequestMes***REMOVED***ge{, requestId=0d762b5e-0401-4e6e-927b-7c1229c0bf75, op='eval', processor='', args={gremlin= g.addV(\"\"person\"\").property(\"\"name\"\", 'dan')  .addV('person').property('name', 'mike')  .addV('person').property('name', '***REMOVED***ikiran') , aliases={g=g}}}");
		event.setField(ApplicationConstantTest.CALLERIAM_KEY, "[unknown]");
		event.setField(ApplicationConstantTest.SERVER_HOSTNAME_PREFIX_KEY,
				ApplicationConstantTest.SERVER_HOSTNAME_PREFIX);
		event.setField(ApplicationConstantTest.DBNAME_PREFIX_KEY, ApplicationConstantTest.DBNAME_PREFIX);

		Collection<Event> response = FILTER.filter(events, matchListener);

		assertNotNull(event.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, response.size());
		assertEquals(1, matchListener.getMatchCount());

	}

	@Test
	public void filterTestForGremlin21() {

		final String auditString = "1629182126440, 172.31.20.251:47156, 172.31.29.87:8182, Websocket, [unknown], [unknown], [unknown], \"RequestMes***REMOVED***ge{, requestId=d74e4a24-292e-4284-94f3-142538326d91, op='eval', processor='', args={gremlin= g.V().hasLabel(\"\"person\"\").has(\"\"name\"\",\"\"shivam\"\").drop().iterate() , aliases={g=g}}}\"";

		Event event = new org.logstash.Event();
		List<Event> events = new ArrayList<>();
		events.add(event);
		event.setField(ApplicationConstantTest.LOG_MESSAGE_KEY, auditString);
		event.setField(ApplicationConstantTest.CLIENT_HOST_KEY, "172.31.20.251:47156");
		event.setField(ApplicationConstantTest.SERVER_HOST_KEY, "172.31.29.87:8182");
		event.setField(ApplicationConstantTest.TIME_STAMP_KEY, "1629182126440");
		event.setField(ApplicationConstantTest.HTTP_HEADERS_KEY, "[unknown]");
		event.setField(ApplicationConstantTest.PAYLOAD_KEY,
				"RequestMes***REMOVED***ge{, requestId=d74e4a24-292e-4284-94f3-142538326d91, op='eval', processor='', args={gremlin= g.V().hasLabel(\"\"person\"\").has(\"\"name\"\",\"\"shivam\"\").drop().iterate() , aliases={g=g}}}");
		event.setField(ApplicationConstantTest.CALLERIAM_KEY, "[unknown]");
		event.setField(ApplicationConstantTest.SERVER_HOSTNAME_PREFIX_KEY,
				ApplicationConstantTest.SERVER_HOSTNAME_PREFIX);
		event.setField(ApplicationConstantTest.DBNAME_PREFIX_KEY, ApplicationConstantTest.DBNAME_PREFIX);

		Collection<Event> response = FILTER.filter(events, matchListener);

		assertNotNull(event.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, response.size());
		assertEquals(1, matchListener.getMatchCount());

	}

	@Test
	public void filterTestForGremlin22() {

		final String auditString = "1629182126440, 172.31.20.251:47156, 172.31.29.87:8182, Websocket, [unknown], [unknown], [unknown], \"RequestMes***REMOVED***ge{, requestId=d74e4a24-292e-4284-94f3-142538326d91, op='eval', processor='', args={gremlin= g.addV('person').property(id, '2').property('name', 'vadas').property('age', 27).next()\r\n"
				+ "g.addV('software').property(id, '3').property('name', 'lop').property('lang', 'java').next()\r\n"
				+ "g.addV('person').property(id, '4').property('name', 'josh').property('age', 32).next()\r\n"
				+ "g.addV('software').property(id, '5').property('name', 'ripple').property('ripple', 'java').next()\r\n"
				+ "g.addV('person').property(id, '6').property('name', 'peter').property('age', 35), aliases={g=g}}}\"";

		Event event = new org.logstash.Event();
		List<Event> events = new ArrayList<>();
		events.add(event);
		event.setField(ApplicationConstantTest.LOG_MESSAGE_KEY, auditString);
		event.setField(ApplicationConstantTest.CLIENT_HOST_KEY, "172.31.20.251:47156");
		event.setField(ApplicationConstantTest.SERVER_HOST_KEY, "172.31.29.87:8182");
		event.setField(ApplicationConstantTest.TIME_STAMP_KEY, "1629182126440");
		event.setField(ApplicationConstantTest.HTTP_HEADERS_KEY, "[unknown]");
		event.setField(ApplicationConstantTest.PAYLOAD_KEY,
				"RequestMes***REMOVED***ge{, requestId=d74e4a24-292e-4284-94f3-142538326d91, op='eval', processor='', args={gremlin= g.addV('person').property(id, '2').property('name', 'vadas').property('age', 27).next()\r\n"
						+ "g.addV('software').property(id, '3').property('name', 'lop').property('lang', 'java').next()\r\n"
						+ "g.addV('person').property(id, '4').property('name', 'josh').property('age', 32).next()\r\n"
						+ "g.addV('software').property(id, '5').property('name', 'ripple').property('ripple', 'java').next()\r\n"
						+ "g.addV('person').property(id, '6').property('name', 'peter').property('age', 35) , aliases={g=g}}}");
		event.setField(ApplicationConstantTest.CALLERIAM_KEY, "[unknown]");
		event.setField(ApplicationConstantTest.SERVER_HOSTNAME_PREFIX_KEY,
				ApplicationConstantTest.SERVER_HOSTNAME_PREFIX);
		event.setField(ApplicationConstantTest.DBNAME_PREFIX_KEY, ApplicationConstantTest.DBNAME_PREFIX);

		Collection<Event> response = FILTER.filter(events, matchListener);

		assertNotNull(event.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, response.size());
		assertEquals(1, matchListener.getMatchCount());

	}

	@Test
	public void filterTestForGremlin23() {

		final String auditString = "1629182126440, 172.31.20.251:47156, 172.31.29.87:8182, Websocket, [unknown], [unknown], [unknown], \"RequestMes***REMOVED***ge{, requestId=d74e4a24-292e-4284-94f3-142538326d91, op='eval', processor='', args={gremlin= g.addV('person::software::company') , aliases={g=g}}}\"";

		Event event = new org.logstash.Event();
		List<Event> events = new ArrayList<>();
		events.add(event);
		event.setField(ApplicationConstantTest.LOG_MESSAGE_KEY, auditString);
		event.setField(ApplicationConstantTest.CLIENT_HOST_KEY, "172.31.20.251:47156");
		event.setField(ApplicationConstantTest.SERVER_HOST_KEY, "172.31.29.87:8182");
		event.setField(ApplicationConstantTest.TIME_STAMP_KEY, "1629182126440");
		event.setField(ApplicationConstantTest.HTTP_HEADERS_KEY, "[unknown]");
		event.setField(ApplicationConstantTest.PAYLOAD_KEY,
				"RequestMes***REMOVED***ge{, requestId=d74e4a24-292e-4284-94f3-142538326d91, op='eval', processor='', args={gremlin= g.addV('person::software::company') , aliases={g=g}}}");
		event.setField(ApplicationConstantTest.CALLERIAM_KEY, "[unknown]");
		event.setField(ApplicationConstantTest.SERVER_HOSTNAME_PREFIX_KEY,
				ApplicationConstantTest.SERVER_HOSTNAME_PREFIX);
		event.setField(ApplicationConstantTest.DBNAME_PREFIX_KEY, ApplicationConstantTest.DBNAME_PREFIX);

		Collection<Event> response = FILTER.filter(events, matchListener);

		assertNotNull(event.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, response.size());
		assertEquals(1, matchListener.getMatchCount());

	}

	@Test
	public void filterTestForGremlin24() {

		final String auditString = "1629182126440, 172.31.20.251:47156, 172.31.29.87:8182, Websocket, [unknown], [unknown], [unknown], \"RequestMes***REMOVED***ge{, requestId=d74e4a24-292e-4284-94f3-142538326d91, op='eval', processor='', args={gremlin= g.V().hasLabel('person').outE() , aliases={g=g}}}\"";

		Event event = new org.logstash.Event();
		List<Event> events = new ArrayList<>();
		events.add(event);
		event.setField(ApplicationConstantTest.LOG_MESSAGE_KEY, auditString);
		event.setField(ApplicationConstantTest.CLIENT_HOST_KEY, "172.31.20.251:47156");
		event.setField(ApplicationConstantTest.SERVER_HOST_KEY, "172.31.29.87:8182");
		event.setField(ApplicationConstantTest.TIME_STAMP_KEY, "1629182126440");
		event.setField(ApplicationConstantTest.HTTP_HEADERS_KEY, "[unknown]");
		event.setField(ApplicationConstantTest.PAYLOAD_KEY,
				"RequestMes***REMOVED***ge{, requestId=d74e4a24-292e-4284-94f3-142538326d91, op='eval', processor='', args={gremlin= g.V().hasLabel('person').outE() , aliases={g=g}}}");
		event.setField(ApplicationConstantTest.CALLERIAM_KEY, "[unknown]");
		event.setField(ApplicationConstantTest.SERVER_HOSTNAME_PREFIX_KEY,
				ApplicationConstantTest.SERVER_HOSTNAME_PREFIX);
		event.setField(ApplicationConstantTest.DBNAME_PREFIX_KEY, ApplicationConstantTest.DBNAME_PREFIX);

		Collection<Event> response = FILTER.filter(events, matchListener);

		assertNotNull(event.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, response.size());
		assertEquals(1, matchListener.getMatchCount());

	}

	@Test
	public void filterTestForGremlin25() {

		final String auditString = "1629182126440, 172.31.20.251:47156, 172.31.29.87:8182, Websocket, [unknown], [unknown], [unknown], \"RequestMes***REMOVED***ge{, requestId=d74e4a24-292e-4284-94f3-142538326d91, op='eval', processor='', args={gremlin= g.E().hasLabel('knows').outV() , aliases={g=g}}}\"";

		Event event = new org.logstash.Event();
		List<Event> events = new ArrayList<>();
		events.add(event);
		event.setField(ApplicationConstantTest.LOG_MESSAGE_KEY, auditString);
		event.setField(ApplicationConstantTest.CLIENT_HOST_KEY, "172.31.20.251:47156");
		event.setField(ApplicationConstantTest.SERVER_HOST_KEY, "172.31.29.87:8182");
		event.setField(ApplicationConstantTest.TIME_STAMP_KEY, "1629182126440");
		event.setField(ApplicationConstantTest.HTTP_HEADERS_KEY, "[unknown]");
		event.setField(ApplicationConstantTest.PAYLOAD_KEY,
				"RequestMes***REMOVED***ge{, requestId=d74e4a24-292e-4284-94f3-142538326d91, op='eval', processor='', args={gremlin= g.E().hasLabel('knows').outV() , aliases={g=g}}}");
		event.setField(ApplicationConstantTest.CALLERIAM_KEY, "[unknown]");
		event.setField(ApplicationConstantTest.SERVER_HOSTNAME_PREFIX_KEY,
				ApplicationConstantTest.SERVER_HOSTNAME_PREFIX);
		event.setField(ApplicationConstantTest.DBNAME_PREFIX_KEY, ApplicationConstantTest.DBNAME_PREFIX);

		Collection<Event> response = FILTER.filter(events, matchListener);

		assertNotNull(event.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, response.size());
		assertEquals(1, matchListener.getMatchCount());

	}

	@Test
	public void filterTestForGremlin26() {

		final String auditString = "1629182126440, 172.31.20.251:47156, 172.31.29.87:8182, Websocket, [unknown], [unknown], [unknown], \"RequestMes***REMOVED***ge{, requestId=d74e4a24-292e-4284-94f3-142538326d91, op='eval', processor='', args={gremlin= g.addE('knows').from('p1').to('p2').addE('knows').from('p3').to('p4') , aliases={g=g}}}\"";

		Event event = new org.logstash.Event();
		List<Event> events = new ArrayList<>();
		events.add(event);
		event.setField(ApplicationConstantTest.LOG_MESSAGE_KEY, auditString);
		event.setField(ApplicationConstantTest.CLIENT_HOST_KEY, "172.31.20.251:47156");
		event.setField(ApplicationConstantTest.SERVER_HOST_KEY, "172.31.29.87:8182");
		event.setField(ApplicationConstantTest.TIME_STAMP_KEY, "1629182126440");
		event.setField(ApplicationConstantTest.HTTP_HEADERS_KEY, "[unknown]");
		event.setField(ApplicationConstantTest.PAYLOAD_KEY,
				"RequestMes***REMOVED***ge{, requestId=d74e4a24-292e-4284-94f3-142538326d91, op='eval', processor='', args={gremlin= g.addE('knows').from('p1').to('p2').addE('knows').from('p3').to('p4') , aliases={g=g}}}");
		event.setField(ApplicationConstantTest.CALLERIAM_KEY, "[unknown]");
		event.setField(ApplicationConstantTest.SERVER_HOSTNAME_PREFIX_KEY,
				ApplicationConstantTest.SERVER_HOSTNAME_PREFIX);
		event.setField(ApplicationConstantTest.DBNAME_PREFIX_KEY, ApplicationConstantTest.DBNAME_PREFIX);

		Collection<Event> response = FILTER.filter(events, matchListener);

		assertNotNull(event.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, response.size());
		assertEquals(1, matchListener.getMatchCount());

	}

	@Test
	public void filterTestForGremlin27() {

		final String auditString = "1629182126440, 172.31.20.251:47156, 172.31.29.87:8182, Websocket, [unknown], [unknown], [unknown], \"RequestMes***REMOVED***ge{, requestId=d74e4a24-292e-4284-94f3-142538326d91, op='eval', processor='', args={gremlin= g.E('10').property('name','xyz').V('10').property('name','abc') , aliases={g=g}}}\"";

		Event event = new org.logstash.Event();
		List<Event> events = new ArrayList<>();
		events.add(event);
		event.setField(ApplicationConstantTest.LOG_MESSAGE_KEY, auditString);
		event.setField(ApplicationConstantTest.CLIENT_HOST_KEY, "172.31.20.251:47156");
		event.setField(ApplicationConstantTest.SERVER_HOST_KEY, "172.31.29.87:8182");
		event.setField(ApplicationConstantTest.TIME_STAMP_KEY, "1629182126440");
		event.setField(ApplicationConstantTest.HTTP_HEADERS_KEY, "[unknown]");
		event.setField(ApplicationConstantTest.PAYLOAD_KEY,
				"RequestMes***REMOVED***ge{, requestId=d74e4a24-292e-4284-94f3-142538326d91, op='eval', processor='', args={gremlin= g.E('10').property('name','xyz').V('10').property('name','abc') , aliases={g=g}}}");
		event.setField(ApplicationConstantTest.CALLERIAM_KEY, "[unknown]");
		event.setField(ApplicationConstantTest.SERVER_HOSTNAME_PREFIX_KEY,
				ApplicationConstantTest.SERVER_HOSTNAME_PREFIX);
		event.setField(ApplicationConstantTest.DBNAME_PREFIX_KEY, ApplicationConstantTest.DBNAME_PREFIX);

		Collection<Event> response = FILTER.filter(events, matchListener);

		assertNotNull(event.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, response.size());
		assertEquals(1, matchListener.getMatchCount());

	}

	@Test
	public void filterTestForGremlin28() {

		final String auditString = "1629182126440, 172.31.20.251:47156, 172.31.29.87:8182, Websocket, [unknown], [unknown], [unknown], \"RequestMes***REMOVED***ge{, requestId=d74e4a24-292e-4284-94f3-142538326d91, op='eval', processor='', args={gremlin= g.E('10').drop().V('10').drop() , aliases={g=g}}}\"";

		Event event = new org.logstash.Event();
		List<Event> events = new ArrayList<>();
		events.add(event);
		event.setField(ApplicationConstantTest.LOG_MESSAGE_KEY, auditString);
		event.setField(ApplicationConstantTest.CLIENT_HOST_KEY, "172.31.20.251:47156");
		event.setField(ApplicationConstantTest.SERVER_HOST_KEY, "172.31.29.87:8182");
		event.setField(ApplicationConstantTest.TIME_STAMP_KEY, "1629182126440");
		event.setField(ApplicationConstantTest.HTTP_HEADERS_KEY, "[unknown]");
		event.setField(ApplicationConstantTest.PAYLOAD_KEY,
				"RequestMes***REMOVED***ge{, requestId=d74e4a24-292e-4284-94f3-142538326d91, op='eval', processor='', args={gremlin= g.E('10').drop().V('10').drop() , aliases={g=g}}}");
		event.setField(ApplicationConstantTest.CALLERIAM_KEY, "[unknown]");
		event.setField(ApplicationConstantTest.SERVER_HOSTNAME_PREFIX_KEY,
				ApplicationConstantTest.SERVER_HOSTNAME_PREFIX);
		event.setField(ApplicationConstantTest.DBNAME_PREFIX_KEY, ApplicationConstantTest.DBNAME_PREFIX);

		Collection<Event> response = FILTER.filter(events, matchListener);

		assertNotNull(event.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, response.size());
		assertEquals(1, matchListener.getMatchCount());

	}

	@Test
	public void filterTestForGremlin29() {

		final String auditString = "1629182126440, 172.31.20.251:47156, 172.31.29.87:8182, Websocket, [unknown], [unknown], [unknown], \"RequestMes***REMOVED***ge{, requestId=d74e4a24-292e-4284-94f3-142538326d91, op='eval', processor='', args={gremlin= g.E().hasLabel('knows').valueMap() , aliases={g=g}}}\"";

		Event event = new org.logstash.Event();
		List<Event> events = new ArrayList<>();
		events.add(event);
		event.setField(ApplicationConstantTest.LOG_MESSAGE_KEY, auditString);
		event.setField(ApplicationConstantTest.CLIENT_HOST_KEY, "172.31.20.251:47156");
		event.setField(ApplicationConstantTest.SERVER_HOST_KEY, "172.31.29.87:8182");
		event.setField(ApplicationConstantTest.TIME_STAMP_KEY, "1629182126440");
		event.setField(ApplicationConstantTest.HTTP_HEADERS_KEY, "[unknown]");
		event.setField(ApplicationConstantTest.PAYLOAD_KEY,
				"RequestMes***REMOVED***ge{, requestId=d74e4a24-292e-4284-94f3-142538326d91, op='eval', processor='', args={gremlin= g.E().hasLabel('knows').valueMap() , aliases={g=g}}}");
		event.setField(ApplicationConstantTest.CALLERIAM_KEY, "[unknown]");
		event.setField(ApplicationConstantTest.SERVER_HOSTNAME_PREFIX_KEY,
				ApplicationConstantTest.SERVER_HOSTNAME_PREFIX);
		event.setField(ApplicationConstantTest.DBNAME_PREFIX_KEY, ApplicationConstantTest.DBNAME_PREFIX);

		Collection<Event> response = FILTER.filter(events, matchListener);

		assertNotNull(event.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, response.size());
		assertEquals(1, matchListener.getMatchCount());

	}

	@Test
	public void filterTestForGremlin30() {

		final String auditString = "1629182126440, 172.31.20.251:47156, 172.31.29.87:8182, Websocket, [unknown], [unknown], [unknown], \"RequestMes***REMOVED***ge{, requestId=d74e4a24-292e-4284-94f3-142538326d91, op='eval', processor='', args={gremlin= g.V().hasLabel('person').out('knows').drop() , aliases={g=g}}}\"";

		Event event = new org.logstash.Event();
		List<Event> events = new ArrayList<>();
		events.add(event);
		event.setField(ApplicationConstantTest.LOG_MESSAGE_KEY, auditString);
		event.setField(ApplicationConstantTest.CLIENT_HOST_KEY, "172.31.20.251:47156");
		event.setField(ApplicationConstantTest.SERVER_HOST_KEY, "172.31.29.87:8182");
		event.setField(ApplicationConstantTest.TIME_STAMP_KEY, "1629182126440");
		event.setField(ApplicationConstantTest.HTTP_HEADERS_KEY, "[unknown]");
		event.setField(ApplicationConstantTest.PAYLOAD_KEY,
				"RequestMes***REMOVED***ge{, requestId=d74e4a24-292e-4284-94f3-142538326d91, op='eval', processor='', args={gremlin= g.V().hasLabel('person').out('knows').drop() , aliases={g=g}}}");
		event.setField(ApplicationConstantTest.CALLERIAM_KEY, "[unknown]");
		event.setField(ApplicationConstantTest.SERVER_HOSTNAME_PREFIX_KEY,
				ApplicationConstantTest.SERVER_HOSTNAME_PREFIX);
		event.setField(ApplicationConstantTest.DBNAME_PREFIX_KEY, ApplicationConstantTest.DBNAME_PREFIX);

		Collection<Event> response = FILTER.filter(events, matchListener);

		assertNotNull(event.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, response.size());
		assertEquals(1, matchListener.getMatchCount());

	}

	@Test
	public void filterTestForGremlin31() {

		final String auditString = "1629182126440, 172.31.20.251:47156, 172.31.29.87:8182, Websocket, [unknown], [unknown], [unknown], \"RequestMes***REMOVED***ge{, requestId=d74e4a24-292e-4284-94f3-142538326d91, op='eval', processor='', args={gremlin= g.E().valueMap().V().valueMap() , aliases={g=g}}}\"";

		Event event = new org.logstash.Event();
		List<Event> events = new ArrayList<>();
		events.add(event);
		event.setField(ApplicationConstantTest.LOG_MESSAGE_KEY, auditString);
		event.setField(ApplicationConstantTest.CLIENT_HOST_KEY, "172.31.20.251:47156");
		event.setField(ApplicationConstantTest.SERVER_HOST_KEY, "172.31.29.87:8182");
		event.setField(ApplicationConstantTest.TIME_STAMP_KEY, "1629182126440");
		event.setField(ApplicationConstantTest.HTTP_HEADERS_KEY, "[unknown]");
		event.setField(ApplicationConstantTest.PAYLOAD_KEY,
				"RequestMes***REMOVED***ge{, requestId=d74e4a24-292e-4284-94f3-142538326d91, op='eval', processor='', args={gremlin= g.E().valueMap().V().valueMap() , aliases={g=g}}}");
		event.setField(ApplicationConstantTest.CALLERIAM_KEY, "[unknown]");
		event.setField(ApplicationConstantTest.SERVER_HOSTNAME_PREFIX_KEY,
				ApplicationConstantTest.SERVER_HOSTNAME_PREFIX);
		event.setField(ApplicationConstantTest.DBNAME_PREFIX_KEY, ApplicationConstantTest.DBNAME_PREFIX);

		Collection<Event> response = FILTER.filter(events, matchListener);

		assertNotNull(event.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, response.size());
		assertEquals(1, matchListener.getMatchCount());

	}

	@Test
	public void filterTestForGremlin32() {

		final String auditString = "1629182126440, 172.31.20.251:47156, 172.31.29.87:8182, Websocket, [unknown], [unknown], [unknown], \"RequestMes***REMOVED***ge{, requestId=d74e4a24-292e-4284-94f3-142538326d91, op='eval', processor='', args={gremlin= g.V().hasLabel('person').outE('knows').outV().has('person','name','shivam').drop() , aliases={g=g}}}\"";

		Event event = new org.logstash.Event();
		List<Event> events = new ArrayList<>();
		events.add(event);
		event.setField(ApplicationConstantTest.LOG_MESSAGE_KEY, auditString);
		event.setField(ApplicationConstantTest.CLIENT_HOST_KEY, "172.31.20.251:47156");
		event.setField(ApplicationConstantTest.SERVER_HOST_KEY, "172.31.29.87:8182");
		event.setField(ApplicationConstantTest.TIME_STAMP_KEY, "1629182126440");
		event.setField(ApplicationConstantTest.HTTP_HEADERS_KEY, "[unknown]");
		event.setField(ApplicationConstantTest.PAYLOAD_KEY,
				"RequestMes***REMOVED***ge{, requestId=d74e4a24-292e-4284-94f3-142538326d91, op='eval', processor='', args={gremlin= g.V().hasLabel('person').outE('knows').outV().has('person','name','shivam').drop() , aliases={g=g}}}");
		event.setField(ApplicationConstantTest.CALLERIAM_KEY, "[unknown]");
		event.setField(ApplicationConstantTest.SERVER_HOSTNAME_PREFIX_KEY,
				ApplicationConstantTest.SERVER_HOSTNAME_PREFIX);
		event.setField(ApplicationConstantTest.DBNAME_PREFIX_KEY, ApplicationConstantTest.DBNAME_PREFIX);

		Collection<Event> response = FILTER.filter(events, matchListener);

		assertNotNull(event.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, response.size());
		assertEquals(1, matchListener.getMatchCount());

	}

	/**
	 * this method for test the filter, when log data is of sparql type in which
	 * serverhost containing some additional information.
	 */
	@Test
	public void filterTestForsparql1() {

		final String auditString = "1629192925919, 172.31.20.251:47148, ip-172-31-29-87.ap-south-1.compute.internal/172.31.29.87:8182, HTTP_POST, null, [unknown], \"HttpObjectAggregator$AggregatedFullHttpRequest(decodeResult: success, version: HTTP/1.1, content: CompositeByteBuf(ridx: 0, widx: 68, cap: 68, components=1)) POST /sparql HTTP/1.1 Host: database2-neptune.cluster-cllxmp6vknzx.ap-south-1.neptune.amazonaws.com:8182 Accept-Encoding: identity Accept: application/sparql-results+json content-type: application/x-www-form-urlencoded Content-Length: 68 User-Agent: python-urllib3/1.26.5\", query=%0ASELECT+%2A+WHERE+%7B%0A++++%3Fs+%3Fp+%3Fo%0A%7D+LIMIT+40%0A";

		Event event = new org.logstash.Event();
		List<Event> events = new ArrayList<>();
		events.add(event);
		event.setField(ApplicationConstantTest.LOG_MESSAGE_KEY, auditString);
		event.setField(ApplicationConstantTest.CLIENT_HOST_KEY, "172.31.20.251:47148");
		event.setField(ApplicationConstantTest.SERVER_HOST_KEY,
				"ip-172-31-29-87.ap-south-1.compute.internal/172.31.29.87:8182");
		event.setField(ApplicationConstantTest.TIME_STAMP_KEY, "1629192925919");
		event.setField(ApplicationConstantTest.HTTP_HEADERS_KEY,
				"HttpObjectAggregator$AggregatedFullHttpRequest(decodeResult: success, version: HTTP/1.1, content: CompositeByteBuf(ridx: 0, widx: 68, cap: 68, components=1)) POST /sparql HTTP/1.1 Host: database2-neptune.cluster-cllxmp6vknzx.ap-south-1.neptune.amazonaws.com:8182 Accept-Encoding: identity Accept: application/sparql-results+json content-type: application/x-www-form-urlencoded Content-Length: 68 User-Agent: python-urllib3/1.26.5");
		event.setField(ApplicationConstantTest.PAYLOAD_KEY,
				"query=%0ASELECT+%2A+WHERE+%7B%0A++++%3Fs+%3Fp+%3Fo%0A%7D+LIMIT+40%0A");
		event.setField(ApplicationConstantTest.CALLERIAM_KEY, null);
		event.setField(ApplicationConstantTest.SERVER_HOSTNAME_PREFIX_KEY,
				ApplicationConstantTest.SERVER_HOSTNAME_PREFIX);
		event.setField(ApplicationConstantTest.DBNAME_PREFIX_KEY, ApplicationConstantTest.DBNAME_PREFIX);

		Collection<Event> response = FILTER.filter(events, matchListener);

		assertNotNull(event.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, response.size());
		assertEquals(1, matchListener.getMatchCount());

	}

	/**
	 * this method for test the filter, when log data is of sparql type in which
	 * serverhost does not containing some additional information, only containing
	 * serverIP and serverPort.
	 */
	@Test
	public void filterTestForsparql2() {

		final String auditString = "1629182105171, 172.31.20.251:47148, 172.31.29.87:8182, HTTP_POST, [unknown], [unknown], \"HttpObjectAggregator$AggregatedFullHttpRequest(decodeResult: success, version: HTTP/1.1, content: CompositeByteBuf(ridx: 0, widx: 1707, cap: 1707, components=1)) POST /sparql HTTP/1.1 Host: database2-neptune.cluster-cllxmp6vknzx.ap-south-1.neptune.amazonaws.com:8182 Accept-Encoding: identity content-type: application/x-www-form-urlencoded Content-Length: 1707 User-Agent: python-urllib3/1.26.5\", update=INSERT+DATA+%7B%0A%3Chttp%3A%2F%2Fs-1000-1%3E+%3Chttp%3A%2F%2FDummy-1%3E+%3Chttp%3A%2F%2Fo-1%3E+.%0A%3Chttp%3A%2F%2Fs-1000-2%3E+%3Chttp%3A%2F%2FDummy-2%3E+%3Chttp%3A%2F%2Fo-2%3E+.%0A%3Chttp%3A%2F%2Fs-1000-3%3E+%3Chttp%3A%2F%2FDummy-3%3E+%3Chttp%3A%2F%2Fo-3%3E+.%0A%7D%0A%0A%0A%0A%0A";

		Event event = new org.logstash.Event();
		List<Event> events = new ArrayList<>();
		events.add(event);
		event.setField(ApplicationConstantTest.LOG_MESSAGE_KEY, auditString);
		event.setField(ApplicationConstantTest.CLIENT_HOST_KEY, "172.31.20.251:47148");
		event.setField(ApplicationConstantTest.SERVER_HOST_KEY, "172.31.29.87:8182");
		event.setField(ApplicationConstantTest.TIME_STAMP_KEY, "1629182105171");
		event.setField(ApplicationConstantTest.HTTP_HEADERS_KEY,
				"HttpObjectAggregator$AggregatedFullHttpRequest(decodeResult: success, version: HTTP/1.1, content: CompositeByteBuf(ridx: 0, widx: 68, cap: 68, components=1)) POST /sparql HTTP/1.1 Host: database2-neptune.cluster-cllxmp6vknzx.ap-south-1.neptune.amazonaws.com:8182 Accept-Encoding: identity Accept: application/sparql-results+json content-type: application/x-www-form-urlencoded Content-Length: 68 User-Agent: python-urllib3/1.26.5");
		event.setField(ApplicationConstantTest.PAYLOAD_KEY,
				"update=INSERT+DATA+%7B%0A%3Chttp%3A%2F%2Fs-1000-1%3E+%3Chttp%3A%2F%2FDummy-1%3E+%3Chttp%3A%2F%2Fo-1%3E+.%0A%3Chttp%3A%2F%2Fs-1000-2%3E+%3Chttp%3A%2F%2FDummy-2%3E+%3Chttp%3A%2F%2Fo-2%3E+.%0A%3Chttp%3A%2F%2Fs-1000-3%3E+%3Chttp%3A%2F%2FDummy-3%3E+%3Chttp%3A%2F%2Fo-3%3E+.%0A%7D%0A%0A%0A%0A%0A");
		event.setField(ApplicationConstantTest.CALLERIAM_KEY, "[unknown]");
		event.setField(ApplicationConstantTest.SERVER_HOSTNAME_PREFIX_KEY,
				ApplicationConstantTest.SERVER_HOSTNAME_PREFIX);
		event.setField(ApplicationConstantTest.DBNAME_PREFIX_KEY, ApplicationConstantTest.DBNAME_PREFIX);

		Collection<Event> response = FILTER.filter(events, matchListener);

		assertNotNull(event.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, response.size());
		assertEquals(1, matchListener.getMatchCount());

	}

	/**
	 * this method for test filter, when log data is of sparql type.
	 */
	@Test
	public void filterTestForSparql3() {

		final String auditString = "1629192925919, 172.31.20.251:47148, [unknown], HTTP_POST, [unknown], [unknown], \"HttpObjectAggregator$AggregatedFullHttpRequest(decodeResult: success, version: HTTP/1.1, content: CompositeByteBuf(ridx: 0, widx: 68, cap: 68, components=1)) POST /sparql HTTP/1.1 Host: database2-neptune.cluster-cllxmp6vknzx.ap-south-1.neptune.amazonaws.com:8182 Accept-Encoding: identity Accept: application/sparql-results+json content-type: application/x-www-form-urlencoded Content-Length: 68 User-Agent: python-urllib3/1.26.5\", query=%0ASELECT+%2A+WHERE+%7B%0A++++%3Fs+%3Fp+%3Fo%0A%7D+LIMIT+40%0A";

		Event event = new org.logstash.Event();
		List<Event> events = new ArrayList<>();
		events.add(event);
		event.setField(ApplicationConstantTest.LOG_MESSAGE_KEY, auditString);
		event.setField(ApplicationConstantTest.CLIENT_HOST_KEY, "172.31.20.251:47148");
		event.setField(ApplicationConstantTest.SERVER_HOST_KEY, "[unknown]");
		event.setField(ApplicationConstantTest.TIME_STAMP_KEY, "1629192925919");
		event.setField(ApplicationConstantTest.HTTP_HEADERS_KEY,
				"HttpObjectAggregator$AggregatedFullHttpRequest(decodeResult: success, version: HTTP/1.1, content: CompositeByteBuf(ridx: 0, widx: 68, cap: 68, components=1)) POST /sparql HTTP/1.1 Host: database2-neptune.cluster-cllxmp6vknzx.ap-south-1.neptune.amazonaws.com:8182 Accept-Encoding: identity Accept: application/sparql-results+json content-type: application/x-www-form-urlencoded Content-Length: 68 User-Agent: python-urllib3/1.26.5");
		event.setField(ApplicationConstantTest.PAYLOAD_KEY,
				"query=%0ASELECT+%2A+WHERE+%7B%0A++++%3Fs+%3Fp+%3Fo%0A%7D+LIMIT+40%0A");
		event.setField(ApplicationConstantTest.CALLERIAM_KEY, "[unknown]");
		event.setField(ApplicationConstantTest.SERVER_HOSTNAME_PREFIX_KEY,
				ApplicationConstantTest.SERVER_HOSTNAME_PREFIX);
		event.setField(ApplicationConstantTest.DBNAME_PREFIX_KEY, ApplicationConstantTest.DBNAME_PREFIX);

		Collection<Event> response = FILTER.filter(events, matchListener);

		assertNotNull(event.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, response.size());
		assertEquals(1, matchListener.getMatchCount());

	}

	/**
	 * this method for test the filter, when log data is of sparql type in which
	 * payload does not containing value(i.e [unknown]).
	 */

	@Test
	public void filterTestForSparql4() {

		final String auditString = "1629192925919, 172.31.20.251:47148, 172.31.29.87:8182, HTTP_POST, [unknown], [unknown], \"HttpObjectAggregator$AggregatedFullHttpRequest(decodeResult: success, version: HTTP/1.1, content: CompositeByteBuf(ridx: 0, widx: 68, cap: 68, components=1)) POST /sparql HTTP/1.1 Host: database2-neptune.cluster-cllxmp6vknzx.ap-south-1.neptune.amazonaws.com:8182 Accept-Encoding: identity Accept: application/sparql-results+json content-type: application/x-www-form-urlencoded Content-Length: 68 User-Agent: python-urllib3/1.26.5\", query=%0ASELECT+%2A+WHERE+%7B%0A++++%3Fs+%3Fp+%3Fo%0A%7D+LIMIT+40%0A";

		Event event = new org.logstash.Event();

		List<Event> events = new ArrayList<>();
		events.add(event);

		event.setField(ApplicationConstantTest.LOG_MESSAGE_KEY, auditString);
		event.setField(ApplicationConstantTest.CLIENT_HOST_KEY, "172.31.20.251:47148");
		event.setField(ApplicationConstantTest.SERVER_HOST_KEY, "172.31.29.87:8182");
		event.setField(ApplicationConstantTest.TIME_STAMP_KEY, "1629192925919");
		event.setField(ApplicationConstantTest.HTTP_HEADERS_KEY,
				"HttpObjectAggregator$AggregatedFullHttpRequest(decodeResult: success, version: HTTP/1.1, content: CompositeByteBuf(ridx: 0, widx: 68, cap: 68, components=1)) POST /sparql HTTP/1.1 Host: database2-neptune.cluster-cllxmp6vknzx.ap-south-1.neptune.amazonaws.com:8182 Accept-Encoding: identity Accept: application/sparql-results+json content-type: application/x-www-form-urlencoded Content-Length: 68 User-Agent: python-urllib3/1.26.5");
		event.setField(ApplicationConstantTest.PAYLOAD_KEY,
				"query=%0ASELECT+%2A+WHERE+%7B%0A++++%3Fs+%3Fp+%3Fo%0A%7D+LIMIT+40%0A");
		event.setField(ApplicationConstantTest.CALLERIAM_KEY, "[unknown]");
		event.setField(ApplicationConstantTest.SERVER_HOSTNAME_PREFIX_KEY,
				ApplicationConstantTest.SERVER_HOSTNAME_PREFIX);
		event.setField(ApplicationConstantTest.DBNAME_PREFIX_KEY, ApplicationConstantTest.DBNAME_PREFIX);

		Collection<Event> response = FILTER.filter(events, matchListener);

		assertNotNull(event.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, response.size());
		assertEquals(1, matchListener.getMatchCount());

	}

	@Test
	public void filterTestForSparql5() {

		final String auditString = "1629192925919, 172.31.20.251:47148, 172.31.29.87:8182, HTTP_POST, [unknown], [unknown], \"HttpObjectAggregator$AggregatedFullHttpRequest(decodeResult: success, version: HTTP/1.1, content: CompositeByteBuf(ridx: 0, widx: 68, cap: 68, components=1)) POST /sparql HTTP/1.1 Host: database2-neptune.cluster-cllxmp6vknzx.ap-south-1.neptune.amazonaws.com:8182 Accept-Encoding: identity Accept: application/sparql-results+json content-type: application/x-www-form-urlencoded Content-Length: 68 User-Agent: python-urllib3/1.26.5\", query=DESCRIBE+%3Chttp%3A%2F%2Fwww.example.com%2Fsoccer%2Fontology%2F%3E%0A";

		Event event = new org.logstash.Event();

		List<Event> events = new ArrayList<>();
		events.add(event);

		event.setField(ApplicationConstantTest.LOG_MESSAGE_KEY, auditString);
		event.setField(ApplicationConstantTest.CLIENT_HOST_KEY, "172.31.20.251:47148");
		event.setField(ApplicationConstantTest.SERVER_HOST_KEY, "172.31.29.87:8182");
		event.setField(ApplicationConstantTest.TIME_STAMP_KEY, "1629192925919");
		event.setField(ApplicationConstantTest.HTTP_HEADERS_KEY,
				"HttpObjectAggregator$AggregatedFullHttpRequest(decodeResult: success, version: HTTP/1.1, content: CompositeByteBuf(ridx: 0, widx: 68, cap: 68, components=1)) POST /sparql HTTP/1.1 Host: database2-neptune.cluster-cllxmp6vknzx.ap-south-1.neptune.amazonaws.com:8182 Accept-Encoding: identity Accept: application/sparql-results+json content-type: application/x-www-form-urlencoded Content-Length: 68 User-Agent: python-urllib3/1.26.5");
		event.setField(ApplicationConstantTest.PAYLOAD_KEY,
				"query=DESCRIBE+%3Chttp%3A%2F%2Fwww.example.com%2Fsoccer%2Fontology%2F%3E%0A");
		event.setField(ApplicationConstantTest.CALLERIAM_KEY, "[unknown]");
		event.setField(ApplicationConstantTest.SERVER_HOSTNAME_PREFIX_KEY,
				ApplicationConstantTest.SERVER_HOSTNAME_PREFIX);
		event.setField(ApplicationConstantTest.DBNAME_PREFIX_KEY, ApplicationConstantTest.DBNAME_PREFIX);

		Collection<Event> response = FILTER.filter(events, matchListener);

		assertNotNull(event.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, response.size());
		assertEquals(1, matchListener.getMatchCount());

	}

	@Test
	public void filterTestForSparql6() {

		final String auditString = "1629192925919, 172.31.20.251:47148, 172.31.29.87:8182, HTTP_POST, [unknown], [unknown], \"HttpObjectAggregator$AggregatedFullHttpRequest(decodeResult: success, version: HTTP/1.1, content: CompositeByteBuf(ridx: 0, widx: 68, cap: 68, components=1)) POST /sparql HTTP/1.1 Host: database2-neptune.cluster-cllxmp6vknzx.ap-south-1.neptune.amazonaws.com:8182 Accept-Encoding: identity Accept: application/sparql-results+json content-type: application/x-www-form-urlencoded Content-Length: 68 User-Agent: python-urllib3/1.26.5\", query=PREFIX+foaf%3A++++%3Chttp%3A%2F%2Fxmlns.com%2Ffoaf%2F0.1%2F%3E%0ASELECT+%3FnameX+%3FnameY+%3FnickY%0AWHERE%0A++%7B+%3Fx+foaf%3Aknows+%3Fy+%3B%0A+++++++foaf%3Aname+%3FnameX+.%0A++++%3Fy+foaf%3Aname+%3FnameY+.%0A++++OPTIONAL+%7B+%3Fy+foaf%3Anick+%3FnickY+%7D%0A++%7D%0A";

		Event event = new org.logstash.Event();

		List<Event> events = new ArrayList<>();
		events.add(event);

		event.setField(ApplicationConstantTest.LOG_MESSAGE_KEY, auditString);
		event.setField(ApplicationConstantTest.CLIENT_HOST_KEY, "172.31.20.251:47148");
		event.setField(ApplicationConstantTest.SERVER_HOST_KEY, "172.31.29.87:8182");
		event.setField(ApplicationConstantTest.TIME_STAMP_KEY, "1629192925919");
		event.setField(ApplicationConstantTest.HTTP_HEADERS_KEY,
				"HttpObjectAggregator$AggregatedFullHttpRequest(decodeResult: success, version: HTTP/1.1, content: CompositeByteBuf(ridx: 0, widx: 68, cap: 68, components=1)) POST /sparql HTTP/1.1 Host: database2-neptune.cluster-cllxmp6vknzx.ap-south-1.neptune.amazonaws.com:8182 Accept-Encoding: identity Accept: application/sparql-results+json content-type: application/x-www-form-urlencoded Content-Length: 68 User-Agent: python-urllib3/1.26.5");
		event.setField(ApplicationConstantTest.PAYLOAD_KEY,
				"query=PREFIX+foaf%3A++++%3Chttp%3A%2F%2Fxmlns.com%2Ffoaf%2F0.1%2F%3E%0ASELECT+%3FnameX+%3FnameY+%3FnickY%0AWHERE%0A++%7B+%3Fx+foaf%3Aknows+%3Fy+%3B%0A+++++++foaf%3Aname+%3FnameX+.%0A++++%3Fy+foaf%3Aname+%3FnameY+.%0A++++OPTIONAL+%7B+%3Fy+foaf%3Anick+%3FnickY+%7D%0A++%7D%0A");
		event.setField(ApplicationConstantTest.CALLERIAM_KEY, "[unknown]");
		event.setField(ApplicationConstantTest.SERVER_HOSTNAME_PREFIX_KEY,
				ApplicationConstantTest.SERVER_HOSTNAME_PREFIX);
		event.setField(ApplicationConstantTest.DBNAME_PREFIX_KEY, ApplicationConstantTest.DBNAME_PREFIX);

		Collection<Event> response = FILTER.filter(events, matchListener);

		assertNotNull(event.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, response.size());
		assertEquals(1, matchListener.getMatchCount());

	}

	@Test
	public void filterTestForSparql7() {

		final String auditString = "1629192925919, 172.31.20.251:47148, 172.31.29.87:8182, HTTP_POST, [unknown], [unknown], \"HttpObjectAggregator$AggregatedFullHttpRequest(decodeResult: success, version: HTTP/1.1, content: CompositeByteBuf(ridx: 0, widx: 68, cap: 68, components=1)) POST /sparql HTTP/1.1 Host: database2-neptune.cluster-cllxmp6vknzx.ap-south-1.neptune.amazonaws.com:8182 Accept-Encoding: identity Accept: application/sparql-results+json content-type: application/x-www-form-urlencoded Content-Length: 68 User-Agent: python-urllib3/1.26.5\", update=PREFIX+foaf%3A++%3Chttp%3A%2F%2Fxmlns.com%2Ffoaf%2F0.1%2F%3E%0A%0AWITH+%3Chttp%3A%2F%2Fexample%2Faddresses%3E%0ADELETE+%7B+%3Fperson+foaf%3AgivenName+%27Bill%27+%7D%0AINSERT+%7B+%3Fperson+foaf%3AgivenName+%27William%27+%7D%0AWHERE%0A++%7B+%3Fperson+foaf%3AgivenName+%27Bill%27%0A++%7D+%0A";

		Event event = new org.logstash.Event();

		List<Event> events = new ArrayList<>();
		events.add(event);

		event.setField(ApplicationConstantTest.LOG_MESSAGE_KEY, auditString);
		event.setField(ApplicationConstantTest.CLIENT_HOST_KEY, "172.31.20.251:47148");
		event.setField(ApplicationConstantTest.SERVER_HOST_KEY, "172.31.29.87:8182");
		event.setField(ApplicationConstantTest.TIME_STAMP_KEY, "1629192925919");
		event.setField(ApplicationConstantTest.HTTP_HEADERS_KEY,
				"HttpObjectAggregator$AggregatedFullHttpRequest(decodeResult: success, version: HTTP/1.1, content: CompositeByteBuf(ridx: 0, widx: 68, cap: 68, components=1)) POST /sparql HTTP/1.1 Host: database2-neptune.cluster-cllxmp6vknzx.ap-south-1.neptune.amazonaws.com:8182 Accept-Encoding: identity Accept: application/sparql-results+json content-type: application/x-www-form-urlencoded Content-Length: 68 User-Agent: python-urllib3/1.26.5");
		event.setField(ApplicationConstantTest.PAYLOAD_KEY,
				"update=PREFIX+foaf%3A++%3Chttp%3A%2F%2Fxmlns.com%2Ffoaf%2F0.1%2F%3E%0A%0AWITH+%3Chttp%3A%2F%2Fexample%2Faddresses%3E%0ADELETE+%7B+%3Fperson+foaf%3AgivenName+%27Bill%27+%7D%0AINSERT+%7B+%3Fperson+foaf%3AgivenName+%27William%27+%7D%0AWHERE%0A++%7B+%3Fperson+foaf%3AgivenName+%27Bill%27%0A++%7D+%0A");
		event.setField(ApplicationConstantTest.CALLERIAM_KEY, "[unknown]");
		event.setField(ApplicationConstantTest.SERVER_HOSTNAME_PREFIX_KEY,
				ApplicationConstantTest.SERVER_HOSTNAME_PREFIX);
		event.setField(ApplicationConstantTest.DBNAME_PREFIX_KEY, ApplicationConstantTest.DBNAME_PREFIX);

		Collection<Event> response = FILTER.filter(events, matchListener);

		assertNotNull(event.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, response.size());
		assertEquals(1, matchListener.getMatchCount());

	}

	@Test
	public void filterTestForSparql8() {

		final String auditString = "1629192925919, 172.31.20.251:47148, 172.31.29.87:8182, HTTP_POST, [unknown], [unknown], \"HttpObjectAggregator$AggregatedFullHttpRequest(decodeResult: success, version: HTTP/1.1, content: CompositeByteBuf(ridx: 0, widx: 68, cap: 68, components=1)) POST /sparql HTTP/1.1 Host: database2-neptune.cluster-cllxmp6vknzx.ap-south-1.neptune.amazonaws.com:8182 Accept-Encoding: identity Accept: application/sparql-results+json content-type: application/x-www-form-urlencoded Content-Length: 68 User-Agent: python-urllib3/1.26.5\", update=CREATE+GRAPH+%3Chttp%3A%2F%2Fxmlns.com%2Ffoaf%2F0.1%2F%3E%0A";

		Event event = new org.logstash.Event();

		List<Event> events = new ArrayList<>();
		events.add(event);

		event.setField(ApplicationConstantTest.LOG_MESSAGE_KEY, auditString);
		event.setField(ApplicationConstantTest.CLIENT_HOST_KEY, "172.31.20.251:47148");
		event.setField(ApplicationConstantTest.SERVER_HOST_KEY, "172.31.29.87:8182");
		event.setField(ApplicationConstantTest.TIME_STAMP_KEY, "1629192925919");
		event.setField(ApplicationConstantTest.HTTP_HEADERS_KEY,
				"HttpObjectAggregator$AggregatedFullHttpRequest(decodeResult: success, version: HTTP/1.1, content: CompositeByteBuf(ridx: 0, widx: 68, cap: 68, components=1)) POST /sparql HTTP/1.1 Host: database2-neptune.cluster-cllxmp6vknzx.ap-south-1.neptune.amazonaws.com:8182 Accept-Encoding: identity Accept: application/sparql-results+json content-type: application/x-www-form-urlencoded Content-Length: 68 User-Agent: python-urllib3/1.26.5");
		event.setField(ApplicationConstantTest.PAYLOAD_KEY,
				"update=CREATE+GRAPH+%3Chttp%3A%2F%2Fxmlns.com%2Ffoaf%2F0.1%2F%3E%0A");
		event.setField(ApplicationConstantTest.CALLERIAM_KEY, "[unknown]");
		event.setField(ApplicationConstantTest.SERVER_HOSTNAME_PREFIX_KEY,
				ApplicationConstantTest.SERVER_HOSTNAME_PREFIX);
		event.setField(ApplicationConstantTest.DBNAME_PREFIX_KEY, ApplicationConstantTest.DBNAME_PREFIX);

		Collection<Event> response = FILTER.filter(events, matchListener);

		assertNotNull(event.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, response.size());
		assertEquals(1, matchListener.getMatchCount());

	}

	@Test
	public void filterTestForSparql9() {

		final String auditString = "1629192925919, 172.31.20.251:47148, 172.31.29.87:8182, HTTP_POST, [unknown], [unknown], \"HttpObjectAggregator$AggregatedFullHttpRequest(decodeResult: success, version: HTTP/1.1, content: CompositeByteBuf(ridx: 0, widx: 68, cap: 68, components=1)) POST /sparql HTTP/1.1 Host: database2-neptune.cluster-cllxmp6vknzx.ap-south-1.neptune.amazonaws.com:8182 Accept-Encoding: identity Accept: application/sparql-results+json content-type: application/x-www-form-urlencoded Content-Length: 68 User-Agent: python-urllib3/1.26.5\", update=PREFIX+dc%3A+%3Chttp%3A%2F%2Fpurl.org%2Fdc%2Felements%2F1.1%2F%3E%0A%0ADELETE+DATA%0A%7B%0A++%3Chttp%3A%2F%2Fexample%2Fbook2%3E+dc%3Atitle+%22David+Copperfield%22+%3B%0A+++++++++++++++++++++++++dc%3Acreator+%22Edmund+Wells%22+.%0A%7D%0A";

		Event event = new org.logstash.Event();

		List<Event> events = new ArrayList<>();
		events.add(event);

		event.setField(ApplicationConstantTest.LOG_MESSAGE_KEY, auditString);
		event.setField(ApplicationConstantTest.CLIENT_HOST_KEY, "172.31.20.251:47148");
		event.setField(ApplicationConstantTest.SERVER_HOST_KEY, "172.31.29.87:8182");
		event.setField(ApplicationConstantTest.TIME_STAMP_KEY, "1629192925919");
		event.setField(ApplicationConstantTest.HTTP_HEADERS_KEY,
				"HttpObjectAggregator$AggregatedFullHttpRequest(decodeResult: success, version: HTTP/1.1, content: CompositeByteBuf(ridx: 0, widx: 68, cap: 68, components=1)) POST /sparql HTTP/1.1 Host: database2-neptune.cluster-cllxmp6vknzx.ap-south-1.neptune.amazonaws.com:8182 Accept-Encoding: identity Accept: application/sparql-results+json content-type: application/x-www-form-urlencoded Content-Length: 68 User-Agent: python-urllib3/1.26.5");
		event.setField(ApplicationConstantTest.PAYLOAD_KEY,
				"update=PREFIX+dc%3A+%3Chttp%3A%2F%2Fpurl.org%2Fdc%2Felements%2F1.1%2F%3E%0A%0ADELETE+DATA%0A%7B%0A++%3Chttp%3A%2F%2Fexample%2Fbook2%3E+dc%3Atitle+%22David+Copperfield%22+%3B%0A+++++++++++++++++++++++++dc%3Acreator+%22Edmund+Wells%22+.%0A%7D%0A");
		event.setField(ApplicationConstantTest.CALLERIAM_KEY, "[unknown]");
		event.setField(ApplicationConstantTest.SERVER_HOSTNAME_PREFIX_KEY,
				ApplicationConstantTest.SERVER_HOSTNAME_PREFIX);
		event.setField(ApplicationConstantTest.DBNAME_PREFIX_KEY, ApplicationConstantTest.DBNAME_PREFIX);

		Collection<Event> response = FILTER.filter(events, matchListener);

		assertNotNull(event.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, response.size());
		assertEquals(1, matchListener.getMatchCount());

	}

	@Test
	public void filterTestForSparql10() {

		final String auditString = "1629192925919, 172.31.20.251:47148, 172.31.29.87:8182, HTTP_POST, [unknown], [unknown], \"HttpObjectAggregator$AggregatedFullHttpRequest(decodeResult: success, version: HTTP/1.1, content: CompositeByteBuf(ridx: 0, widx: 68, cap: 68, components=1)) POST /sparql HTTP/1.1 Host: database2-neptune.cluster-cllxmp6vknzx.ap-south-1.neptune.amazonaws.com:8182 Accept-Encoding: identity Accept: application/sparql-results+json content-type: application/x-www-form-urlencoded Content-Length: 68 User-Agent: python-urllib3/1.26.5\", update=ADD+GRAPH%3Chttp%3A%2F%2Fxmlns.com%2Ffoaf%2F0.1%2F%3E+TO+GRAPH%3Chttp%3A%2F%2Fexample.org%2Fnamed%3E%0A";

		Event event = new org.logstash.Event();

		List<Event> events = new ArrayList<>();
		events.add(event);

		event.setField(ApplicationConstantTest.LOG_MESSAGE_KEY, auditString);
		event.setField(ApplicationConstantTest.CLIENT_HOST_KEY, "172.31.20.251:47148");
		event.setField(ApplicationConstantTest.SERVER_HOST_KEY, "172.31.29.87:8182");
		event.setField(ApplicationConstantTest.TIME_STAMP_KEY, "1629192925919");
		event.setField(ApplicationConstantTest.HTTP_HEADERS_KEY,
				"HttpObjectAggregator$AggregatedFullHttpRequest(decodeResult: success, version: HTTP/1.1, content: CompositeByteBuf(ridx: 0, widx: 68, cap: 68, components=1)) POST /sparql HTTP/1.1 Host: database2-neptune.cluster-cllxmp6vknzx.ap-south-1.neptune.amazonaws.com:8182 Accept-Encoding: identity Accept: application/sparql-results+json content-type: application/x-www-form-urlencoded Content-Length: 68 User-Agent: python-urllib3/1.26.5");
		event.setField(ApplicationConstantTest.PAYLOAD_KEY,
				"update=ADD+GRAPH%3Chttp%3A%2F%2Fxmlns.com%2Ffoaf%2F0.1%2F%3E+TO+GRAPH%3Chttp%3A%2F%2Fexample.org%2Fnamed%3E%0A");
		event.setField(ApplicationConstantTest.CALLERIAM_KEY, "[unknown]");
		event.setField(ApplicationConstantTest.SERVER_HOSTNAME_PREFIX_KEY,
				ApplicationConstantTest.SERVER_HOSTNAME_PREFIX);
		event.setField(ApplicationConstantTest.DBNAME_PREFIX_KEY, ApplicationConstantTest.DBNAME_PREFIX);

		Collection<Event> response = FILTER.filter(events, matchListener);

		assertNotNull(event.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, response.size());
		assertEquals(1, matchListener.getMatchCount());

	}

	@Test
	public void filterTestForSparql11() {

		final String auditString = "1629192925919, 172.31.20.251:47148, 172.31.29.87:8182, HTTP_POST, [unknown], [unknown], \"HttpObjectAggregator$AggregatedFullHttpRequest(decodeResult: success, version: HTTP/1.1, content: CompositeByteBuf(ridx: 0, widx: 68, cap: 68, components=1)) POST /sparql HTTP/1.1 Host: database2-neptune.cluster-cllxmp6vknzx.ap-south-1.neptune.amazonaws.com:8182 Accept-Encoding: identity Accept: application/sparql-results+json content-type: application/x-www-form-urlencoded Content-Length: 68 User-Agent: python-urllib3/1.26.5\", update=ADD+GRAPH%3Chttp%3A%2F%2Fxmlns.com%2Ffoaf%2F0.1%2F%3E+TO+DEFAULT%0A";

		Event event = new org.logstash.Event();

		List<Event> events = new ArrayList<>();
		events.add(event);

		event.setField(ApplicationConstantTest.LOG_MESSAGE_KEY, auditString);
		event.setField(ApplicationConstantTest.CLIENT_HOST_KEY, "172.31.20.251:47148");
		event.setField(ApplicationConstantTest.SERVER_HOST_KEY, "172.31.29.87:8182");
		event.setField(ApplicationConstantTest.TIME_STAMP_KEY, "1629192925919");
		event.setField(ApplicationConstantTest.HTTP_HEADERS_KEY,
				"HttpObjectAggregator$AggregatedFullHttpRequest(decodeResult: success, version: HTTP/1.1, content: CompositeByteBuf(ridx: 0, widx: 68, cap: 68, components=1)) POST /sparql HTTP/1.1 Host: database2-neptune.cluster-cllxmp6vknzx.ap-south-1.neptune.amazonaws.com:8182 Accept-Encoding: identity Accept: application/sparql-results+json content-type: application/x-www-form-urlencoded Content-Length: 68 User-Agent: python-urllib3/1.26.5");
		event.setField(ApplicationConstantTest.PAYLOAD_KEY,
				"update=ADD+GRAPH%3Chttp%3A%2F%2Fxmlns.com%2Ffoaf%2F0.1%2F%3E+TO+DEFAULT%0A");
		event.setField(ApplicationConstantTest.CALLERIAM_KEY, "[unknown]");
		event.setField(ApplicationConstantTest.SERVER_HOSTNAME_PREFIX_KEY,
				ApplicationConstantTest.SERVER_HOSTNAME_PREFIX);
		event.setField(ApplicationConstantTest.DBNAME_PREFIX_KEY, ApplicationConstantTest.DBNAME_PREFIX);

		Collection<Event> response = FILTER.filter(events, matchListener);

		assertNotNull(event.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, response.size());
		assertEquals(1, matchListener.getMatchCount());

	}

	@Test
	public void filterTestForSparql12() {

		final String auditString = "1629192925919, 172.31.20.251:47148, 172.31.29.87:8182, HTTP_POST, [unknown], [unknown], \"HttpObjectAggregator$AggregatedFullHttpRequest(decodeResult: success, version: HTTP/1.1, content: CompositeByteBuf(ridx: 0, widx: 68, cap: 68, components=1)) POST /sparql HTTP/1.1 Host: database2-neptune.cluster-cllxmp6vknzx.ap-south-1.neptune.amazonaws.com:8182 Accept-Encoding: identity Accept: application/sparql-results+json content-type: application/x-www-form-urlencoded Content-Length: 68 User-Agent: python-urllib3/1.26.5\", update=COPY+GRAPH%3Chttp%3A%2F%2Fxmlns.com%2Ffoaf%2F0.1%2F%3E+TO+GRAPH%3Chttp%3A%2F%2Fexample.com%2Fnames%3E%0A";

		Event event = new org.logstash.Event();

		List<Event> events = new ArrayList<>();
		events.add(event);

		event.setField(ApplicationConstantTest.LOG_MESSAGE_KEY, auditString);
		event.setField(ApplicationConstantTest.CLIENT_HOST_KEY, "172.31.20.251:47148");
		event.setField(ApplicationConstantTest.SERVER_HOST_KEY, "172.31.29.87:8182");
		event.setField(ApplicationConstantTest.TIME_STAMP_KEY, "1629192925919");
		event.setField(ApplicationConstantTest.HTTP_HEADERS_KEY,
				"HttpObjectAggregator$AggregatedFullHttpRequest(decodeResult: success, version: HTTP/1.1, content: CompositeByteBuf(ridx: 0, widx: 68, cap: 68, components=1)) POST /sparql HTTP/1.1 Host: database2-neptune.cluster-cllxmp6vknzx.ap-south-1.neptune.amazonaws.com:8182 Accept-Encoding: identity Accept: application/sparql-results+json content-type: application/x-www-form-urlencoded Content-Length: 68 User-Agent: python-urllib3/1.26.5");
		event.setField(ApplicationConstantTest.PAYLOAD_KEY,
				"update=COPY+GRAPH%3Chttp%3A%2F%2Fxmlns.com%2Ffoaf%2F0.1%2F%3E+TO+GRAPH%3Chttp%3A%2F%2Fexample.com%2Fnames%3E%0A");
		event.setField(ApplicationConstantTest.CALLERIAM_KEY, "[unknown]");
		event.setField(ApplicationConstantTest.SERVER_HOSTNAME_PREFIX_KEY,
				ApplicationConstantTest.SERVER_HOSTNAME_PREFIX);
		event.setField(ApplicationConstantTest.DBNAME_PREFIX_KEY, ApplicationConstantTest.DBNAME_PREFIX);
		Collection<Event> response = FILTER.filter(events, matchListener);

		assertNotNull(event.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, response.size());
		assertEquals(1, matchListener.getMatchCount());

	}

	@Test
	public void filterTestForSparql13() {

		final String auditString = "1629192925919, 172.31.20.251:47148, 172.31.29.87:8182, HTTP_POST, [unknown], [unknown], \"HttpObjectAggregator$AggregatedFullHttpRequest(decodeResult: success, version: HTTP/1.1, content: CompositeByteBuf(ridx: 0, widx: 68, cap: 68, components=1)) POST /sparql HTTP/1.1 Host: database2-neptune.cluster-cllxmp6vknzx.ap-south-1.neptune.amazonaws.com:8182 Accept-Encoding: identity Accept: application/sparql-results+json content-type: application/x-www-form-urlencoded Content-Length: 68 User-Agent: python-urllib3/1.26.5\", update=COPY+GRAPH%3Chttp%3A%2F%2Fxmlns.com%2Ffoaf%2F0.1%2F%3E+TO+DEFAULT%0A";

		Event event = new org.logstash.Event();

		List<Event> events = new ArrayList<>();
		events.add(event);

		event.setField(ApplicationConstantTest.LOG_MESSAGE_KEY, auditString);
		event.setField(ApplicationConstantTest.CLIENT_HOST_KEY, "172.31.20.251:47148");
		event.setField(ApplicationConstantTest.SERVER_HOST_KEY, "172.31.29.87:8182");
		event.setField(ApplicationConstantTest.TIME_STAMP_KEY, "1629192925919");
		event.setField(ApplicationConstantTest.HTTP_HEADERS_KEY,
				"HttpObjectAggregator$AggregatedFullHttpRequest(decodeResult: success, version: HTTP/1.1, content: CompositeByteBuf(ridx: 0, widx: 68, cap: 68, components=1)) POST /sparql HTTP/1.1 Host: database2-neptune.cluster-cllxmp6vknzx.ap-south-1.neptune.amazonaws.com:8182 Accept-Encoding: identity Accept: application/sparql-results+json content-type: application/x-www-form-urlencoded Content-Length: 68 User-Agent: python-urllib3/1.26.5");
		event.setField(ApplicationConstantTest.PAYLOAD_KEY,
				"update=COPY+GRAPH%3Chttp%3A%2F%2Fxmlns.com%2Ffoaf%2F0.1%2F%3E+TO+DEFAULT%0A");
		event.setField(ApplicationConstantTest.CALLERIAM_KEY, "[unknown]");
		event.setField(ApplicationConstantTest.SERVER_HOSTNAME_PREFIX_KEY,
				ApplicationConstantTest.SERVER_HOSTNAME_PREFIX);
		event.setField(ApplicationConstantTest.DBNAME_PREFIX_KEY, ApplicationConstantTest.DBNAME_PREFIX);

		Collection<Event> response = FILTER.filter(events, matchListener);

		assertNotNull(event.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, response.size());
		assertEquals(1, matchListener.getMatchCount());

	}

	@Test
	public void filterTestForSparql14() {

		final String auditString = "1629192925919, 172.31.20.251:47148, 172.31.29.87:8182, HTTP_POST, [unknown], [unknown], \"HttpObjectAggregator$AggregatedFullHttpRequest(decodeResult: success, version: HTTP/1.1, content: CompositeByteBuf(ridx: 0, widx: 68, cap: 68, components=1)) POST /sparql HTTP/1.1 Host: database2-neptune.cluster-cllxmp6vknzx.ap-south-1.neptune.amazonaws.com:8182 Accept-Encoding: identity Accept: application/sparql-results+json content-type: application/x-www-form-urlencoded Content-Length: 68 User-Agent: python-urllib3/1.26.5\", update=MOVE+GRAPH%3Chttp%3A%2F%2Fxmlns.com%2Ffoaf%2F0.1%2F%3E+TO+GRAPH%3Chttp%3A%2F%2Fexample.org%2Fnamed%3E%0A";

		Event event = new org.logstash.Event();

		List<Event> events = new ArrayList<>();
		events.add(event);

		event.setField(ApplicationConstantTest.LOG_MESSAGE_KEY, auditString);
		event.setField(ApplicationConstantTest.CLIENT_HOST_KEY, "172.31.20.251:47148");
		event.setField(ApplicationConstantTest.SERVER_HOST_KEY, "172.31.29.87:8182");
		event.setField(ApplicationConstantTest.TIME_STAMP_KEY, "1629192925919");
		event.setField(ApplicationConstantTest.HTTP_HEADERS_KEY,
				"HttpObjectAggregator$AggregatedFullHttpRequest(decodeResult: success, version: HTTP/1.1, content: CompositeByteBuf(ridx: 0, widx: 68, cap: 68, components=1)) POST /sparql HTTP/1.1 Host: database2-neptune.cluster-cllxmp6vknzx.ap-south-1.neptune.amazonaws.com:8182 Accept-Encoding: identity Accept: application/sparql-results+json content-type: application/x-www-form-urlencoded Content-Length: 68 User-Agent: python-urllib3/1.26.5");
		event.setField(ApplicationConstantTest.PAYLOAD_KEY,
				"update=MOVE+GRAPH%3Chttp%3A%2F%2Fxmlns.com%2Ffoaf%2F0.1%2F%3E+TO+GRAPH%3Chttp%3A%2F%2Fexample.org%2Fnamed%3E%0A");
		event.setField(ApplicationConstantTest.CALLERIAM_KEY, "[unknown]");
		event.setField(ApplicationConstantTest.SERVER_HOSTNAME_PREFIX_KEY,
				ApplicationConstantTest.SERVER_HOSTNAME_PREFIX);
		event.setField(ApplicationConstantTest.DBNAME_PREFIX_KEY, ApplicationConstantTest.DBNAME_PREFIX);

		Collection<Event> response = FILTER.filter(events, matchListener);

		assertNotNull(event.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, response.size());
		assertEquals(1, matchListener.getMatchCount());

	}

	@Test
	public void filterTestForSparql15() {

		final String auditString = "1629192925919, 172.31.20.251:47148, 172.31.29.87:8182, HTTP_POST, [unknown], [unknown], \"HttpObjectAggregator$AggregatedFullHttpRequest(decodeResult: success, version: HTTP/1.1, content: CompositeByteBuf(ridx: 0, widx: 68, cap: 68, components=1)) POST /sparql HTTP/1.1 Host: database2-neptune.cluster-cllxmp6vknzx.ap-south-1.neptune.amazonaws.com:8182 Accept-Encoding: identity Accept: application/sparql-results+json content-type: application/x-www-form-urlencoded Content-Length: 68 User-Agent: python-urllib3/1.26.5\", update=MOVE+GRAPH%3Chttp%3A%2F%2Fxmlns.com%2Ffoaf%2F0.1%2F%3E+TO+DEFAULT%0A";

		Event event = new org.logstash.Event();

		List<Event> events = new ArrayList<>();
		events.add(event);

		event.setField(ApplicationConstantTest.LOG_MESSAGE_KEY, auditString);
		event.setField(ApplicationConstantTest.CLIENT_HOST_KEY, "172.31.20.251:47148");
		event.setField(ApplicationConstantTest.SERVER_HOST_KEY, "172.31.29.87:8182");
		event.setField(ApplicationConstantTest.TIME_STAMP_KEY, "1629192925919");
		event.setField(ApplicationConstantTest.HTTP_HEADERS_KEY,
				"HttpObjectAggregator$AggregatedFullHttpRequest(decodeResult: success, version: HTTP/1.1, content: CompositeByteBuf(ridx: 0, widx: 68, cap: 68, components=1)) POST /sparql HTTP/1.1 Host: database2-neptune.cluster-cllxmp6vknzx.ap-south-1.neptune.amazonaws.com:8182 Accept-Encoding: identity Accept: application/sparql-results+json content-type: application/x-www-form-urlencoded Content-Length: 68 User-Agent: python-urllib3/1.26.5");
		event.setField(ApplicationConstantTest.PAYLOAD_KEY,
				"update=MOVE+GRAPH%3Chttp%3A%2F%2Fxmlns.com%2Ffoaf%2F0.1%2F%3E+TO+DEFAULT%0A");
		event.setField(ApplicationConstantTest.CALLERIAM_KEY, "[unknown]");
		event.setField(ApplicationConstantTest.SERVER_HOSTNAME_PREFIX_KEY,
				ApplicationConstantTest.SERVER_HOSTNAME_PREFIX);
		event.setField(ApplicationConstantTest.DBNAME_PREFIX_KEY, ApplicationConstantTest.DBNAME_PREFIX);
		Collection<Event> response = FILTER.filter(events, matchListener);

		assertNotNull(event.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, response.size());
		assertEquals(1, matchListener.getMatchCount());

	}

	@Test
	public void filterTestForSparql16() {

		final String auditString = "1629192925919, 172.31.20.251:47148, 172.31.29.87:8182, HTTP_POST, [unknown], [unknown], \"HttpObjectAggregator$AggregatedFullHttpRequest(decodeResult: success, version: HTTP/1.1, content: CompositeByteBuf(ridx: 0, widx: 68, cap: 68, components=1)) POST /sparql HTTP/1.1 Host: database2-neptune.cluster-cllxmp6vknzx.ap-south-1.neptune.amazonaws.com:8182 Accept-Encoding: identity Accept: application/sparql-results+json content-type: application/x-www-form-urlencoded Content-Length: 68 User-Agent: python-urllib3/1.26.5\", update=DROP+GRAPH%3Chttp%3A%2F%2Fxmlns.com%2Ffoaf%2F0.1%2F%3E%0A";

		Event event = new org.logstash.Event();

		List<Event> events = new ArrayList<>();
		events.add(event);

		event.setField(ApplicationConstantTest.LOG_MESSAGE_KEY, auditString);
		event.setField(ApplicationConstantTest.CLIENT_HOST_KEY, "172.31.20.251:47148");
		event.setField(ApplicationConstantTest.SERVER_HOST_KEY, "172.31.29.87:8182");
		event.setField(ApplicationConstantTest.TIME_STAMP_KEY, "1629192925919");
		event.setField(ApplicationConstantTest.HTTP_HEADERS_KEY,
				"HttpObjectAggregator$AggregatedFullHttpRequest(decodeResult: success, version: HTTP/1.1, content: CompositeByteBuf(ridx: 0, widx: 68, cap: 68, components=1)) POST /sparql HTTP/1.1 Host: database2-neptune.cluster-cllxmp6vknzx.ap-south-1.neptune.amazonaws.com:8182 Accept-Encoding: identity Accept: application/sparql-results+json content-type: application/x-www-form-urlencoded Content-Length: 68 User-Agent: python-urllib3/1.26.5");
		event.setField(ApplicationConstantTest.PAYLOAD_KEY,
				"update=DROP+GRAPH%3Chttp%3A%2F%2Fxmlns.com%2Ffoaf%2F0.1%2F%3E%0A");
		event.setField(ApplicationConstantTest.CALLERIAM_KEY, "[unknown]");
		event.setField(ApplicationConstantTest.SERVER_HOSTNAME_PREFIX_KEY,
				ApplicationConstantTest.SERVER_HOSTNAME_PREFIX);
		event.setField(ApplicationConstantTest.DBNAME_PREFIX_KEY, ApplicationConstantTest.DBNAME_PREFIX);

		Collection<Event> response = FILTER.filter(events, matchListener);

		assertNotNull(event.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, response.size());
		assertEquals(1, matchListener.getMatchCount());

	}

	@Test
	public void filterTestForSparql17() {

		final String auditString = "1629192925919, 172.31.20.251:47148, 172.31.29.87:8182, HTTP_POST, [unknown], [unknown], \"HttpObjectAggregator$AggregatedFullHttpRequest(decodeResult: success, version: HTTP/1.1, content: CompositeByteBuf(ridx: 0, widx: 68, cap: 68, components=1)) POST /sparql HTTP/1.1 Host: database2-neptune.cluster-cllxmp6vknzx.ap-south-1.neptune.amazonaws.com:8182 Accept-Encoding: identity Accept: application/sparql-results+json content-type: application/x-www-form-urlencoded Content-Length: 68 User-Agent: python-urllib3/1.26.5\", update=DROP+ALL%0A";

		Event event = new org.logstash.Event();

		List<Event> events = new ArrayList<>();
		events.add(event);

		event.setField(ApplicationConstantTest.LOG_MESSAGE_KEY, auditString);
		event.setField(ApplicationConstantTest.CLIENT_HOST_KEY, "172.31.20.251:47148");
		event.setField(ApplicationConstantTest.SERVER_HOST_KEY, "172.31.29.87:8182");
		event.setField(ApplicationConstantTest.TIME_STAMP_KEY, "1629192925919");
		event.setField(ApplicationConstantTest.HTTP_HEADERS_KEY,
				"HttpObjectAggregator$AggregatedFullHttpRequest(decodeResult: success, version: HTTP/1.1, content: CompositeByteBuf(ridx: 0, widx: 68, cap: 68, components=1)) POST /sparql HTTP/1.1 Host: database2-neptune.cluster-cllxmp6vknzx.ap-south-1.neptune.amazonaws.com:8182 Accept-Encoding: identity Accept: application/sparql-results+json content-type: application/x-www-form-urlencoded Content-Length: 68 User-Agent: python-urllib3/1.26.5");
		event.setField(ApplicationConstantTest.PAYLOAD_KEY, "update=DROP+ALL%0A");
		event.setField(ApplicationConstantTest.CALLERIAM_KEY, "[unknown]");
		event.setField(ApplicationConstantTest.SERVER_HOSTNAME_PREFIX_KEY,
				ApplicationConstantTest.SERVER_HOSTNAME_PREFIX);
		event.setField(ApplicationConstantTest.DBNAME_PREFIX_KEY, ApplicationConstantTest.DBNAME_PREFIX);

		Collection<Event> response = FILTER.filter(events, matchListener);

		assertNotNull(event.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, response.size());
		assertEquals(1, matchListener.getMatchCount());

	}

	@Test
	public void filterTestForSparql18() {

		final String auditString = "1629192925919, 172.31.20.251:47148, 172.31.29.87:8182, HTTP_POST, [unknown], [unknown], \"HttpObjectAggregator$AggregatedFullHttpRequest(decodeResult: success, version: HTTP/1.1, content: CompositeByteBuf(ridx: 0, widx: 68, cap: 68, components=1)) POST /sparql HTTP/1.1 Host: database2-neptune.cluster-cllxmp6vknzx.ap-south-1.neptune.amazonaws.com:8182 Accept-Encoding: identity Accept: application/sparql-results+json content-type: application/x-www-form-urlencoded Content-Length: 68 User-Agent: python-urllib3/1.26.5\", update=LOAD++%3Chttp%3A%2F%2Fexample.com%2Fnames%3E+INTO+GRAPH+%3Chttp%3A%2F%2Fexample.com%2Faddresses%3E%0A";

		Event event = new org.logstash.Event();

		List<Event> events = new ArrayList<>();
		events.add(event);

		event.setField(ApplicationConstantTest.LOG_MESSAGE_KEY, auditString);
		event.setField(ApplicationConstantTest.CLIENT_HOST_KEY, "172.31.20.251:47148");
		event.setField(ApplicationConstantTest.SERVER_HOST_KEY, "172.31.29.87:8182");
		event.setField(ApplicationConstantTest.TIME_STAMP_KEY, "1629192925919");
		event.setField(ApplicationConstantTest.HTTP_HEADERS_KEY,
				"HttpObjectAggregator$AggregatedFullHttpRequest(decodeResult: success, version: HTTP/1.1, content: CompositeByteBuf(ridx: 0, widx: 68, cap: 68, components=1)) POST /sparql HTTP/1.1 Host: database2-neptune.cluster-cllxmp6vknzx.ap-south-1.neptune.amazonaws.com:8182 Accept-Encoding: identity Accept: application/sparql-results+json content-type: application/x-www-form-urlencoded Content-Length: 68 User-Agent: python-urllib3/1.26.5");
		event.setField(ApplicationConstantTest.PAYLOAD_KEY,
				"update=LOAD++%3Chttp%3A%2F%2Fexample.com%2Fnames%3E+INTO+GRAPH+%3Chttp%3A%2F%2Fexample.com%2Faddresses%3E%0A");
		event.setField(ApplicationConstantTest.CALLERIAM_KEY, "[unknown]");
		event.setField(ApplicationConstantTest.SERVER_HOSTNAME_PREFIX_KEY,
				ApplicationConstantTest.SERVER_HOSTNAME_PREFIX);
		event.setField(ApplicationConstantTest.DBNAME_PREFIX_KEY, ApplicationConstantTest.DBNAME_PREFIX);
		Collection<Event> response = FILTER.filter(events, matchListener);

		assertNotNull(event.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, response.size());
		assertEquals(1, matchListener.getMatchCount());

	}

	@Test
	public void filterTestForSparql20() {

		final String auditString = "1629192925919, 172.31.20.251:47148, 172.31.29.87:8182, HTTP_POST, [unknown], [unknown], \"HttpObjectAggregator$AggregatedFullHttpRequest(decodeResult: success, version: HTTP/1.1, content: CompositeByteBuf(ridx: 0, widx: 68, cap: 68, components=1)) POST /sparql HTTP/1.1 Host: database2-neptune.cluster-cllxmp6vknzx.ap-south-1.neptune.amazonaws.com:8182 Accept-Encoding: identity Accept: application/sparql-results+json content-type: application/x-www-form-urlencoded Content-Length: 68 User-Agent: python-urllib3/1.26.5\", query=PREFIX+dc10%3A++%3Chttp%3A%2F%2Fpurl.org%2Fdc%2Felements%2F1.0%2F%3E%0APREFIX+dc11%3A++%3Chttp%3A%2F%2Fpurl.org%2Fdc%2Felements%2F1.1%2F%3E%0ASELECT+%3Ftitle+%3Fauthor%0AWHERE++%7B+%7B+%3Fbook+dc10%3Atitle+%3Ftitle+.++%3Fbook+dc10%3Acreator+%3Fauthor+%7D%0A+++++++++UNION%0A+++++++++%7B+%3Fbook+dc11%3Atitle+%3Ftitle+.++%3Fbook+dc11%3Acreator+%3Fauthor+%7D%0A+++++++%7D%0A";

		Event event = new org.logstash.Event();

		List<Event> events = new ArrayList<>();
		events.add(event);

		event.setField(ApplicationConstantTest.LOG_MESSAGE_KEY, auditString);
		event.setField(ApplicationConstantTest.CLIENT_HOST_KEY, "172.31.20.251:47148");
		event.setField(ApplicationConstantTest.SERVER_HOST_KEY, "172.31.29.87:8182");
		event.setField(ApplicationConstantTest.TIME_STAMP_KEY, "1629192925919");
		event.setField(ApplicationConstantTest.HTTP_HEADERS_KEY,
				"HttpObjectAggregator$AggregatedFullHttpRequest(decodeResult: success, version: HTTP/1.1, content: CompositeByteBuf(ridx: 0, widx: 68, cap: 68, components=1)) POST /sparql HTTP/1.1 Host: database2-neptune.cluster-cllxmp6vknzx.ap-south-1.neptune.amazonaws.com:8182 Accept-Encoding: identity Accept: application/sparql-results+json content-type: application/x-www-form-urlencoded Content-Length: 68 User-Agent: python-urllib3/1.26.5");
		event.setField(ApplicationConstantTest.PAYLOAD_KEY,
				"query=PREFIX+dc10%3A++%3Chttp%3A%2F%2Fpurl.org%2Fdc%2Felements%2F1.0%2F%3E%0APREFIX+dc11%3A++%3Chttp%3A%2F%2Fpurl.org%2Fdc%2Felements%2F1.1%2F%3E%0ASELECT+%3Ftitle+%3Fauthor%0AWHERE++%7B+%7B+%3Fbook+dc10%3Atitle+%3Ftitle+.++%3Fbook+dc10%3Acreator+%3Fauthor+%7D%0A+++++++++UNION%0A+++++++++%7B+%3Fbook+dc11%3Atitle+%3Ftitle+.++%3Fbook+dc11%3Acreator+%3Fauthor+%7D%0A+++++++%7D%0A");
		event.setField(ApplicationConstantTest.CALLERIAM_KEY, "[unknown]");
		event.setField(ApplicationConstantTest.SERVER_HOSTNAME_PREFIX_KEY,
				ApplicationConstantTest.SERVER_HOSTNAME_PREFIX);
		event.setField(ApplicationConstantTest.DBNAME_PREFIX_KEY, ApplicationConstantTest.DBNAME_PREFIX);
		Collection<Event> response = FILTER.filter(events, matchListener);

		assertNotNull(event.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, response.size());
		assertEquals(1, matchListener.getMatchCount());

	}

	@Test
	public void filterTestForSparql21() {

		final String auditString = "1629192925919, 172.31.20.251:47148, 172.31.29.87:8182, HTTP_POST, [unknown], [unknown], \"HttpObjectAggregator$AggregatedFullHttpRequest(decodeResult: success, version: HTTP/1.1, content: CompositeByteBuf(ridx: 0, widx: 68, cap: 68, components=1)) POST /sparql HTTP/1.1 Host: database2-neptune.cluster-cllxmp6vknzx.ap-south-1.neptune.amazonaws.com:8182 Accept-Encoding: identity Accept: application/sparql-results+json content-type: application/x-www-form-urlencoded Content-Length: 68 User-Agent: python-urllib3/1.26.5\", query=PREFIX+dc10%3A++%3Chttp%3A%2F%2Fpurl.org%2Fdc%2Felements%2F1.0%2F%3E%0APREFIX+dc11%3A++%3Chttp%3A%2F%2Fpurl.org%2Fdc%2Felements%2F1.1%2F%3E%0ASELECT+%3Fx+%3Fy%0AWHERE++%7B+%7B+%3Fbook+dc10%3Atitle+%3Fx+%7D+UNION+%7B+%3Fbook+dc11%3Atitle++%3Fy+%7D+%7D%0A";

		Event event = new org.logstash.Event();

		List<Event> events = new ArrayList<>();
		events.add(event);

		event.setField(ApplicationConstantTest.LOG_MESSAGE_KEY, auditString);
		event.setField(ApplicationConstantTest.CLIENT_HOST_KEY, "172.31.20.251:47148");
		event.setField(ApplicationConstantTest.SERVER_HOST_KEY, "172.31.29.87:8182");
		event.setField(ApplicationConstantTest.TIME_STAMP_KEY, "1629192925919");
		event.setField(ApplicationConstantTest.HTTP_HEADERS_KEY,
				"HttpObjectAggregator$AggregatedFullHttpRequest(decodeResult: success, version: HTTP/1.1, content: CompositeByteBuf(ridx: 0, widx: 68, cap: 68, components=1)) POST /sparql HTTP/1.1 Host: database2-neptune.cluster-cllxmp6vknzx.ap-south-1.neptune.amazonaws.com:8182 Accept-Encoding: identity Accept: application/sparql-results+json content-type: application/x-www-form-urlencoded Content-Length: 68 User-Agent: python-urllib3/1.26.5");
		event.setField(ApplicationConstantTest.PAYLOAD_KEY,
				"query=PREFIX+dc10%3A++%3Chttp%3A%2F%2Fpurl.org%2Fdc%2Felements%2F1.0%2F%3E%0APREFIX+dc11%3A++%3Chttp%3A%2F%2Fpurl.org%2Fdc%2Felements%2F1.1%2F%3E%0ASELECT+%3Fx+%3Fy%0AWHERE++%7B+%7B+%3Fbook+dc10%3Atitle+%3Fx+%7D+UNION+%7B+%3Fbook+dc11%3Atitle++%3Fy+%7D+%7D%0A");
		event.setField(ApplicationConstantTest.CALLERIAM_KEY, "[unknown]");
		event.setField(ApplicationConstantTest.SERVER_HOSTNAME_PREFIX_KEY,
				ApplicationConstantTest.SERVER_HOSTNAME_PREFIX);
		event.setField(ApplicationConstantTest.DBNAME_PREFIX_KEY, ApplicationConstantTest.DBNAME_PREFIX);

		Collection<Event> response = FILTER.filter(events, matchListener);

		assertNotNull(event.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, response.size());
		assertEquals(1, matchListener.getMatchCount());

	}

	@Test
	public void filterTestForSparql22() {

		final String auditString = "1629192925919, 172.31.20.251:47148, 172.31.29.87:8182, HTTP_POST, [unknown], [unknown], \"HttpObjectAggregator$AggregatedFullHttpRequest(decodeResult: success, version: HTTP/1.1, content: CompositeByteBuf(ridx: 0, widx: 68, cap: 68, components=1)) POST /sparql HTTP/1.1 Host: database2-neptune.cluster-cllxmp6vknzx.ap-south-1.neptune.amazonaws.com:8182 Accept-Encoding: identity Accept: application/sparql-results+json content-type: application/x-www-form-urlencoded Content-Length: 68 User-Agent: python-urllib3/1.26.5\", update=PREFIX+dc%3A+%3Chttp%3A%2F%2Fpurl.org%2Fdc%2Felements%2F1.1%2F%3E%0ADELETE+DATA%0A%7B+GRAPH+%3Chttp%3A%2F%2Fexample%2FbookStore%3E+%7B+%3Chttp%3A%2F%2Fexample%2Fbook1%3E++dc%3Atitle++%22Fundamentals+of+Compiler+Desing%22+%7D+%7D+%3B%0A%0APREFIX+dc%3A+%3Chttp%3A%2F%2Fpurl.org%2Fdc%2Felements%2F1.1%2F%3E%0AINSERT+DATA%0A%7B+GRAPH+%3Chttp%3A%2F%2Fexample%2FbookStore%3E+%7B+%3Chttp%3A%2F%2Fexample%2Fbook1%3E++dc%3Atitle++%22Fundamentals+of+Compiler+Design%22+%7D+%7D%0A";

		Event event = new org.logstash.Event();

		List<Event> events = new ArrayList<>();
		events.add(event);

		event.setField(ApplicationConstantTest.LOG_MESSAGE_KEY, auditString);
		event.setField(ApplicationConstantTest.CLIENT_HOST_KEY, "172.31.20.251:47148");
		event.setField(ApplicationConstantTest.SERVER_HOST_KEY, "172.31.29.87:8182");
		event.setField(ApplicationConstantTest.TIME_STAMP_KEY, "1629192925919");
		event.setField(ApplicationConstantTest.HTTP_HEADERS_KEY,
				"HttpObjectAggregator$AggregatedFullHttpRequest(decodeResult: success, version: HTTP/1.1, content: CompositeByteBuf(ridx: 0, widx: 68, cap: 68, components=1)) POST /sparql HTTP/1.1 Host: database2-neptune.cluster-cllxmp6vknzx.ap-south-1.neptune.amazonaws.com:8182 Accept-Encoding: identity Accept: application/sparql-results+json content-type: application/x-www-form-urlencoded Content-Length: 68 User-Agent: python-urllib3/1.26.5");
		event.setField(ApplicationConstantTest.PAYLOAD_KEY,
				"update=PREFIX+dc%3A+%3Chttp%3A%2F%2Fpurl.org%2Fdc%2Felements%2F1.1%2F%3E%0ADELETE+DATA%0A%7B+GRAPH+%3Chttp%3A%2F%2Fexample%2FbookStore%3E+%7B+%3Chttp%3A%2F%2Fexample%2Fbook1%3E++dc%3Atitle++%22Fundamentals+of+Compiler+Desing%22+%7D+%7D+%3B%0A%0APREFIX+dc%3A+%3Chttp%3A%2F%2Fpurl.org%2Fdc%2Felements%2F1.1%2F%3E%0AINSERT+DATA%0A%7B+GRAPH+%3Chttp%3A%2F%2Fexample%2FbookStore%3E+%7B+%3Chttp%3A%2F%2Fexample%2Fbook1%3E++dc%3Atitle++%22Fundamentals+of+Compiler+Design%22+%7D+%7D%0A");
		event.setField(ApplicationConstantTest.CALLERIAM_KEY, "[unknown]");
		event.setField(ApplicationConstantTest.SERVER_HOSTNAME_PREFIX_KEY,
				ApplicationConstantTest.SERVER_HOSTNAME_PREFIX);
		event.setField(ApplicationConstantTest.DBNAME_PREFIX_KEY, ApplicationConstantTest.DBNAME_PREFIX);

		Collection<Event> response = FILTER.filter(events, matchListener);

		assertNotNull(event.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, response.size());
		assertEquals(1, matchListener.getMatchCount());

	}

	@Test
	public void filterTestForSparql23() {

		final String auditString = "1653045538286, 172.31.3.205:35612, 172.31.28.105:8182, HTTP_GET, [unknown], [unknown], \"HttpObjectAggregator$AggregatedFullHttpRequest(decodeResult: success, version: HTTP/1.1, content: CompositeByteBuf(ridx: 0, widx: 0, cap: 0, components=0)) GET /sparql?query=SELECT+*+WHERE+%7B+%3Fs+%3Fp+%3Fo+%7D+limit+1 HTTP/1.1 Accept: text/csv;q=0.8, application/x-sparqlstar-results+json;q=0.8, application/sparql-results+json;q=0.8, application/json;q=0.8, application/sparql-results+xml, application/xml, text/tab-separated-values;q=0.8, application/x-sparqlstar-results+xml;q=0.8, text/x-tab-separated-values-star;q=0.8, application/x-sparqlstar-results+tsv;q=0.8, application/x-binary-rdf-results-table;q=0.8 Host: localhost:8182 Connection: Keep-Alive User-Agent: Apache-HttpClient/4.5.13 (Java/12.0.2) Accept-Encoding: gzip,deflate content-length: 0\", /sparql?query=SELECT+*+WHERE+%7B+%3Fs+%3Fp+%3Fo+%7D+limit+1";

		Event event = new org.logstash.Event();

		List<Event> events = new ArrayList<>();
		events.add(event);

		event.setField(ApplicationConstantTest.LOG_MESSAGE_KEY, auditString);
		event.setField(ApplicationConstantTest.CLIENT_HOST_KEY, "172.31.3.205:35612");
		event.setField(ApplicationConstantTest.SERVER_HOST_KEY, "172.31.28.105:8182");
		event.setField(ApplicationConstantTest.TIME_STAMP_KEY, "1653045538286");
		event.setField(ApplicationConstantTest.HTTP_HEADERS_KEY,
				"HttpObjectAggregator$AggregatedFullHttpRequest(decodeResult: success, version: HTTP/1.1, content: CompositeByteBuf(ridx: 0, widx: 0, cap: 0, components=0)) GET /sparql?query=SELECT+*+WHERE+%7B+%3Fs+%3Fp+%3Fo+%7D+limit+1 HTTP/1.1 Accept: text/csv;q=0.8, application/x-sparqlstar-results+json;q=0.8, application/sparql-results+json;q=0.8, application/json;q=0.8, application/sparql-results+xml, application/xml, text/tab-separated-values;q=0.8, application/x-sparqlstar-results+xml;q=0.8, text/x-tab-separated-values-star;q=0.8, application/x-sparqlstar-results+tsv;q=0.8, application/x-binary-rdf-results-table;q=0.8 Host: localhost:8182 Connection: Keep-Alive User-Agent: Apache-HttpClient/4.5.13 (Java/12.0.2) Accept-Encoding: gzip,deflate content-length: 0");
		event.setField(ApplicationConstantTest.PAYLOAD_KEY,
				"/sparql?query=SELECT+*+WHERE+%7B+%3Fs+%3Fp+%3Fo+%7D+limit+1");
		event.setField(ApplicationConstantTest.CALLERIAM_KEY, "[unknown]");
		event.setField(ApplicationConstantTest.SERVER_HOSTNAME_PREFIX_KEY,
				ApplicationConstantTest.SERVER_HOSTNAME_PREFIX);
		event.setField(ApplicationConstantTest.DBNAME_PREFIX_KEY, ApplicationConstantTest.DBNAME_PREFIX);

		Collection<Event> response = FILTER.filter(events, matchListener);

		assertNotNull(event.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, response.size());
		assertEquals(1, matchListener.getMatchCount());

	}

	@Test
	public void filterTestForSparql24() {

		final String auditString = "1653045538286, 172.31.3.205:35612, 172.31.28.105:8182, HTTP_GET, [unknown], [unknown], \"HttpObjectAggregator$AggregatedFullHttpRequest(decodeResult: success, version: HTTP/1.1, content: CompositeByteBuf(ridx: 0, widx: 0, cap: 0, components=0)) GET /sparql HTTP/1.1 Accept: text/csv;q=0.8, application/x-sparqlstar-results+json;q=0.8, application/sparql-results+json;q=0.8, application/json;q=0.8, application/sparql-results+xml, application/xml, text/tab-separated-values;q=0.8, application/x-sparqlstar-results+xml;q=0.8, text/x-tab-separated-values-star;q=0.8, application/x-sparqlstar-results+tsv;q=0.8, application/x-binary-rdf-results-table;q=0.8 Host: localhost:8182 Connection: Keep-Alive User-Agent: Apache-HttpClient/4.5.13 (Java/12.0.2) Accept-Encoding: gzip,deflate content-length: 0\", query=PREFIX+foaf%3A++++%3Chttp%3A%2F%2Fxmlns.com%2Ffoaf%2F0.1%2F%3E%0APREFIX+vcard%3A+++%3Chttp%3A%2F%2Fwww.w3.org%2F2001%2Fvcard-rdf%2F3.0%23%3E%0A%0ACONSTRUCT+%7B+%3Fx++vcard%3AN+_%3Av+.%0A++++++++++++_%3Av+vcard%3AgivenName+%3Fgname+.%0A++++++++++++_%3Av+vcard%3AfamilyName+%3Ffname+%7D%0AWHERE%0A+%7B%0A++++%7B+%3Fx+foaf%3Afirstname+%3Fgname+%7D+UNION++%7B+%3Fx+foaf%3Agivenname+++%3Fgname+%7D+.%0A++++%7B+%3Fx+foaf%3Asurname+++%3Ffname+%7D+UNION++%7B+%3Fx+foaf%3Afamily_name+%3Ffname+%7D+.%0A+%7D%0A";

		Event event = new org.logstash.Event();

		List<Event> events = new ArrayList<>();
		events.add(event);

		event.setField(ApplicationConstantTest.LOG_MESSAGE_KEY, auditString);
		event.setField(ApplicationConstantTest.CLIENT_HOST_KEY, "172.31.3.205:35612");
		event.setField(ApplicationConstantTest.SERVER_HOST_KEY, "172.31.28.105:8182");
		event.setField(ApplicationConstantTest.TIME_STAMP_KEY, "1653045538286");
		event.setField(ApplicationConstantTest.HTTP_HEADERS_KEY,
				"HttpObjectAggregator$AggregatedFullHttpRequest(decodeResult: success, version: HTTP/1.1, content: CompositeByteBuf(ridx: 0, widx: 0, cap: 0, components=0)) GET /sparql HTTP/1.1 Accept: text/csv;q=0.8, application/x-sparqlstar-results+json;q=0.8, application/sparql-results+json;q=0.8, application/json;q=0.8, application/sparql-results+xml, application/xml, text/tab-separated-values;q=0.8, application/x-sparqlstar-results+xml;q=0.8, text/x-tab-separated-values-star;q=0.8, application/x-sparqlstar-results+tsv;q=0.8, application/x-binary-rdf-results-table;q=0.8 Host: localhost:8182 Connection: Keep-Alive User-Agent: Apache-HttpClient/4.5.13 (Java/12.0.2) Accept-Encoding: gzip,deflate content-length: 0");
		event.setField(ApplicationConstantTest.PAYLOAD_KEY,
				"query=PREFIX+foaf%3A++++%3Chttp%3A%2F%2Fxmlns.com%2Ffoaf%2F0.1%2F%3E%0APREFIX+vcard%3A+++%3Chttp%3A%2F%2Fwww.w3.org%2F2001%2Fvcard-rdf%2F3.0%23%3E%0A%0ACONSTRUCT+%7B+%3Fx++vcard%3AN+_%3Av+.%0A++++++++++++_%3Av+vcard%3AgivenName+%3Fgname+.%0A++++++++++++_%3Av+vcard%3AfamilyName+%3Ffname+%7D%0AWHERE%0A+%7B%0A++++%7B+%3Fx+foaf%3Afirstname+%3Fgname+%7D+UNION++%7B+%3Fx+foaf%3Agivenname+++%3Fgname+%7D+.%0A++++%7B+%3Fx+foaf%3Asurname+++%3Ffname+%7D+UNION++%7B+%3Fx+foaf%3Afamily_name+%3Ffname+%7D+.%0A+%7D%0A");
		event.setField(ApplicationConstantTest.CALLERIAM_KEY, "[unknown]");
		event.setField(ApplicationConstantTest.SERVER_HOSTNAME_PREFIX_KEY,
				ApplicationConstantTest.SERVER_HOSTNAME_PREFIX);
		event.setField(ApplicationConstantTest.DBNAME_PREFIX_KEY, ApplicationConstantTest.DBNAME_PREFIX);

		Collection<Event> response = FILTER.filter(events, matchListener);

		assertNotNull(event.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, response.size());
		assertEquals(1, matchListener.getMatchCount());

	}

	@Test
	public void filterTestForSparql25() {

		final String auditString = "1653045538286, 172.31.3.205:35612, 172.31.28.105:8182, HTTP_GET, [unknown], [unknown], \"HttpObjectAggregator$AggregatedFullHttpRequest(decodeResult: success, version: HTTP/1.1, content: CompositeByteBuf(ridx: 0, widx: 0, cap: 0, components=0)) GET /sparql HTTP/1.1 Accept: text/csv;q=0.8, application/x-sparqlstar-results+json;q=0.8, application/sparql-results+json;q=0.8, application/json;q=0.8, application/sparql-results+xml, application/xml, text/tab-separated-values;q=0.8, application/x-sparqlstar-results+xml;q=0.8, text/x-tab-separated-values-star;q=0.8, application/x-sparqlstar-results+tsv;q=0.8, application/x-binary-rdf-results-table;q=0.8 Host: localhost:8182 Connection: Keep-Alive User-Agent: Apache-HttpClient/4.5.13 (Java/12.0.2) Accept-Encoding: gzip,deflate content-length: 0\", update=ADD+GRAPH+%3Chttp%3A%2F%2Fexample.com%2Fnames%3E+TO+DEFAULT";

		Event event = new org.logstash.Event();

		List<Event> events = new ArrayList<>();
		events.add(event);

		event.setField(ApplicationConstantTest.LOG_MESSAGE_KEY, auditString);
		event.setField(ApplicationConstantTest.CLIENT_HOST_KEY, "172.31.3.205:35612");
		event.setField(ApplicationConstantTest.SERVER_HOST_KEY, "172.31.28.105:8182");
		event.setField(ApplicationConstantTest.TIME_STAMP_KEY, "1653045538286");
		event.setField(ApplicationConstantTest.HTTP_HEADERS_KEY,
				"HttpObjectAggregator$AggregatedFullHttpRequest(decodeResult: success, version: HTTP/1.1, content: CompositeByteBuf(ridx: 0, widx: 0, cap: 0, components=0)) GET /sparql HTTP/1.1 Accept: text/csv;q=0.8, application/x-sparqlstar-results+json;q=0.8, application/sparql-results+json;q=0.8, application/json;q=0.8, application/sparql-results+xml, application/xml, text/tab-separated-values;q=0.8, application/x-sparqlstar-results+xml;q=0.8, text/x-tab-separated-values-star;q=0.8, application/x-sparqlstar-results+tsv;q=0.8, application/x-binary-rdf-results-table;q=0.8 Host: localhost:8182 Connection: Keep-Alive User-Agent: Apache-HttpClient/4.5.13 (Java/12.0.2) Accept-Encoding: gzip,deflate content-length: 0");
		event.setField(ApplicationConstantTest.PAYLOAD_KEY,
				"update=ADD+GRAPH+%3Chttp%3A%2F%2Fexample.com%2Fnames%3E+TO+DEFAULT");
		event.setField(ApplicationConstantTest.CALLERIAM_KEY, "[unknown]");
		event.setField(ApplicationConstantTest.SERVER_HOSTNAME_PREFIX_KEY,
				ApplicationConstantTest.SERVER_HOSTNAME_PREFIX);
		event.setField(ApplicationConstantTest.DBNAME_PREFIX_KEY, ApplicationConstantTest.DBNAME_PREFIX);

		Collection<Event> response = FILTER.filter(events, matchListener);

		assertNotNull(event.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, response.size());
		assertEquals(1, matchListener.getMatchCount());

	}

	@Test
	public void filterTestForSparql26() {

		final String auditString = "1653045538286, 172.31.3.205:35612, 172.31.28.105:8182, HTTP_GET, [unknown], [unknown], \"HttpObjectAggregator$AggregatedFullHttpRequest(decodeResult: success, version: HTTP/1.1, content: CompositeByteBuf(ridx: 0, widx: 0, cap: 0, components=0)) GET /sparql HTTP/1.1 Accept: text/csv;q=0.8, application/x-sparqlstar-results+json;q=0.8, application/sparql-results+json;q=0.8, application/json;q=0.8, application/sparql-results+xml, application/xml, text/tab-separated-values;q=0.8, application/x-sparqlstar-results+xml;q=0.8, text/x-tab-separated-values-star;q=0.8, application/x-sparqlstar-results+tsv;q=0.8, application/x-binary-rdf-results-table;q=0.8 Host: localhost:8182 Connection: Keep-Alive User-Agent: Apache-HttpClient/4.5.13 (Java/12.0.2) Accept-Encoding: gzip,deflate content-length: 0\", update=ADD+DEFAULT+TO+GRAPH+%3Chttp%3A%2F%2Fexample.com%2Faddresses%3E";

		Event event = new org.logstash.Event();

		List<Event> events = new ArrayList<>();
		events.add(event);

		event.setField(ApplicationConstantTest.LOG_MESSAGE_KEY, auditString);
		event.setField(ApplicationConstantTest.CLIENT_HOST_KEY, "172.31.3.205:35612");
		event.setField(ApplicationConstantTest.SERVER_HOST_KEY, "172.31.28.105:8182");
		event.setField(ApplicationConstantTest.TIME_STAMP_KEY, "1653045538286");
		event.setField(ApplicationConstantTest.HTTP_HEADERS_KEY,
				"HttpObjectAggregator$AggregatedFullHttpRequest(decodeResult: success, version: HTTP/1.1, content: CompositeByteBuf(ridx: 0, widx: 0, cap: 0, components=0)) GET /sparql HTTP/1.1 Accept: text/csv;q=0.8, application/x-sparqlstar-results+json;q=0.8, application/sparql-results+json;q=0.8, application/json;q=0.8, application/sparql-results+xml, application/xml, text/tab-separated-values;q=0.8, application/x-sparqlstar-results+xml;q=0.8, text/x-tab-separated-values-star;q=0.8, application/x-sparqlstar-results+tsv;q=0.8, application/x-binary-rdf-results-table;q=0.8 Host: localhost:8182 Connection: Keep-Alive User-Agent: Apache-HttpClient/4.5.13 (Java/12.0.2) Accept-Encoding: gzip,deflate content-length: 0");
		event.setField(ApplicationConstantTest.PAYLOAD_KEY,
				"update=ADD+DEFAULT+TO+GRAPH+%3Chttp%3A%2F%2Fexample.com%2Faddresses%3E");
		event.setField(ApplicationConstantTest.CALLERIAM_KEY, "[unknown]");
		event.setField(ApplicationConstantTest.SERVER_HOSTNAME_PREFIX_KEY,
				ApplicationConstantTest.SERVER_HOSTNAME_PREFIX);
		event.setField(ApplicationConstantTest.DBNAME_PREFIX_KEY, ApplicationConstantTest.DBNAME_PREFIX);

		Collection<Event> response = FILTER.filter(events, matchListener);

		assertNotNull(event.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, response.size());
		assertEquals(1, matchListener.getMatchCount());

	}

	@Test
	public void filterTestForSparql27() {

		final String auditString = "1653045538286, 172.31.3.205:35612, 172.31.28.105:8182, HTTP_GET, [unknown], [unknown], \"HttpObjectAggregator$AggregatedFullHttpRequest(decodeResult: success, version: HTTP/1.1, content: CompositeByteBuf(ridx: 0, widx: 0, cap: 0, components=0)) GET /sparql HTTP/1.1 Accept: text/csv;q=0.8, application/x-sparqlstar-results+json;q=0.8, application/sparql-results+json;q=0.8, application/json;q=0.8, application/sparql-results+xml, application/xml, text/tab-separated-values;q=0.8, application/x-sparqlstar-results+xml;q=0.8, text/x-tab-separated-values-star;q=0.8, application/x-sparqlstar-results+tsv;q=0.8, application/x-binary-rdf-results-table;q=0.8 Host: localhost:8182 Connection: Keep-Alive User-Agent: Apache-HttpClient/4.5.13 (Java/12.0.2) Accept-Encoding: gzip,deflate content-length: 0\", update=MOVE+GRAPH+%3Chttp%3A%2F%2Fexample.com%2Fnames%3E+TO+DEFAULT";

		Event event = new org.logstash.Event();

		List<Event> events = new ArrayList<>();
		events.add(event);

		event.setField(ApplicationConstantTest.LOG_MESSAGE_KEY, auditString);
		event.setField(ApplicationConstantTest.CLIENT_HOST_KEY, "172.31.3.205:35612");
		event.setField(ApplicationConstantTest.SERVER_HOST_KEY, "172.31.28.105:8182");
		event.setField(ApplicationConstantTest.TIME_STAMP_KEY, "1653045538286");
		event.setField(ApplicationConstantTest.HTTP_HEADERS_KEY,
				"HttpObjectAggregator$AggregatedFullHttpRequest(decodeResult: success, version: HTTP/1.1, content: CompositeByteBuf(ridx: 0, widx: 0, cap: 0, components=0)) GET /sparql HTTP/1.1 Accept: text/csv;q=0.8, application/x-sparqlstar-results+json;q=0.8, application/sparql-results+json;q=0.8, application/json;q=0.8, application/sparql-results+xml, application/xml, text/tab-separated-values;q=0.8, application/x-sparqlstar-results+xml;q=0.8, text/x-tab-separated-values-star;q=0.8, application/x-sparqlstar-results+tsv;q=0.8, application/x-binary-rdf-results-table;q=0.8 Host: localhost:8182 Connection: Keep-Alive User-Agent: Apache-HttpClient/4.5.13 (Java/12.0.2) Accept-Encoding: gzip,deflate content-length: 0");
		event.setField(ApplicationConstantTest.PAYLOAD_KEY,
				"update=MOVE+GRAPH+%3Chttp%3A%2F%2Fexample.com%2Fnames%3E+TO+DEFAULT");
		event.setField(ApplicationConstantTest.CALLERIAM_KEY, "[unknown]");
		event.setField(ApplicationConstantTest.SERVER_HOSTNAME_PREFIX_KEY,
				ApplicationConstantTest.SERVER_HOSTNAME_PREFIX);
		event.setField(ApplicationConstantTest.DBNAME_PREFIX_KEY, ApplicationConstantTest.DBNAME_PREFIX);

		Collection<Event> response = FILTER.filter(events, matchListener);

		assertNotNull(event.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, response.size());
		assertEquals(1, matchListener.getMatchCount());

	}

	@Test
	public void filterTestForSparql28() {

		final String auditString = "1653045538286, 172.31.3.205:35612, 172.31.28.105:8182, HTTP_GET, [unknown], [unknown], \"HttpObjectAggregator$AggregatedFullHttpRequest(decodeResult: success, version: HTTP/1.1, content: CompositeByteBuf(ridx: 0, widx: 0, cap: 0, components=0)) GET /sparql HTTP/1.1 Accept: text/csv;q=0.8, application/x-sparqlstar-results+json;q=0.8, application/sparql-results+json;q=0.8, application/json;q=0.8, application/sparql-results+xml, application/xml, text/tab-separated-values;q=0.8, application/x-sparqlstar-results+xml;q=0.8, text/x-tab-separated-values-star;q=0.8, application/x-sparqlstar-results+tsv;q=0.8, application/x-binary-rdf-results-table;q=0.8 Host: localhost:8182 Connection: Keep-Alive User-Agent: Apache-HttpClient/4.5.13 (Java/12.0.2) Accept-Encoding: gzip,deflate content-length: 0\", update=MOVE+DEFAULT+TO+GRAPH+%3Chttp%3A%2F%2Fexample.com%2Faddresses%3E";

		Event event = new org.logstash.Event();

		List<Event> events = new ArrayList<>();
		events.add(event);

		event.setField(ApplicationConstantTest.LOG_MESSAGE_KEY, auditString);
		event.setField(ApplicationConstantTest.CLIENT_HOST_KEY, "172.31.3.205:35612");
		event.setField(ApplicationConstantTest.SERVER_HOST_KEY, "172.31.28.105:8182");
		event.setField(ApplicationConstantTest.TIME_STAMP_KEY, "1653045538286");
		event.setField(ApplicationConstantTest.HTTP_HEADERS_KEY,
				"HttpObjectAggregator$AggregatedFullHttpRequest(decodeResult: success, version: HTTP/1.1, content: CompositeByteBuf(ridx: 0, widx: 0, cap: 0, components=0)) GET /sparql HTTP/1.1 Accept: text/csv;q=0.8, application/x-sparqlstar-results+json;q=0.8, application/sparql-results+json;q=0.8, application/json;q=0.8, application/sparql-results+xml, application/xml, text/tab-separated-values;q=0.8, application/x-sparqlstar-results+xml;q=0.8, text/x-tab-separated-values-star;q=0.8, application/x-sparqlstar-results+tsv;q=0.8, application/x-binary-rdf-results-table;q=0.8 Host: localhost:8182 Connection: Keep-Alive User-Agent: Apache-HttpClient/4.5.13 (Java/12.0.2) Accept-Encoding: gzip,deflate content-length: 0");
		event.setField(ApplicationConstantTest.PAYLOAD_KEY,
				"update=MOVE+DEFAULT+TO+GRAPH+%3Chttp%3A%2F%2Fexample.com%2Faddresses%3E");
		event.setField(ApplicationConstantTest.CALLERIAM_KEY, "[unknown]");
		event.setField(ApplicationConstantTest.SERVER_HOSTNAME_PREFIX_KEY,
				ApplicationConstantTest.SERVER_HOSTNAME_PREFIX);
		event.setField(ApplicationConstantTest.DBNAME_PREFIX_KEY, ApplicationConstantTest.DBNAME_PREFIX);

		Collection<Event> response = FILTER.filter(events, matchListener);

		assertNotNull(event.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, response.size());
		assertEquals(1, matchListener.getMatchCount());

	}

	@Test
	public void filterTestForSparql29() {

		final String auditString = "1653045538286, 172.31.3.205:35612, 172.31.28.105:8182, HTTP_GET, [unknown], [unknown], \"HttpObjectAggregator$AggregatedFullHttpRequest(decodeResult: success, version: HTTP/1.1, content: CompositeByteBuf(ridx: 0, widx: 0, cap: 0, components=0)) GET /sparql HTTP/1.1 Accept: text/csv;q=0.8, application/x-sparqlstar-results+json;q=0.8, application/sparql-results+json;q=0.8, application/json;q=0.8, application/sparql-results+xml, application/xml, text/tab-separated-values;q=0.8, application/x-sparqlstar-results+xml;q=0.8, text/x-tab-separated-values-star;q=0.8, application/x-sparqlstar-results+tsv;q=0.8, application/x-binary-rdf-results-table;q=0.8 Host: localhost:8182 Connection: Keep-Alive User-Agent: Apache-HttpClient/4.5.13 (Java/12.0.2) Accept-Encoding: gzip,deflate content-length: 0\", update=COPY+GRAPH+%3Chttp%3A%2F%2Fexample.com%2Fnames%3E+TO+DEFAULT";

		Event event = new org.logstash.Event();

		List<Event> events = new ArrayList<>();
		events.add(event);

		event.setField(ApplicationConstantTest.LOG_MESSAGE_KEY, auditString);
		event.setField(ApplicationConstantTest.CLIENT_HOST_KEY, "172.31.3.205:35612");
		event.setField(ApplicationConstantTest.SERVER_HOST_KEY, "172.31.28.105:8182");
		event.setField(ApplicationConstantTest.TIME_STAMP_KEY, "1653045538286");
		event.setField(ApplicationConstantTest.HTTP_HEADERS_KEY,
				"HttpObjectAggregator$AggregatedFullHttpRequest(decodeResult: success, version: HTTP/1.1, content: CompositeByteBuf(ridx: 0, widx: 0, cap: 0, components=0)) GET /sparql HTTP/1.1 Accept: text/csv;q=0.8, application/x-sparqlstar-results+json;q=0.8, application/sparql-results+json;q=0.8, application/json;q=0.8, application/sparql-results+xml, application/xml, text/tab-separated-values;q=0.8, application/x-sparqlstar-results+xml;q=0.8, text/x-tab-separated-values-star;q=0.8, application/x-sparqlstar-results+tsv;q=0.8, application/x-binary-rdf-results-table;q=0.8 Host: localhost:8182 Connection: Keep-Alive User-Agent: Apache-HttpClient/4.5.13 (Java/12.0.2) Accept-Encoding: gzip,deflate content-length: 0");
		event.setField(ApplicationConstantTest.PAYLOAD_KEY,
				"update=COPY+GRAPH+%3Chttp%3A%2F%2Fexample.com%2Fnames%3E+TO+DEFAULT");
		event.setField(ApplicationConstantTest.CALLERIAM_KEY, "[unknown]");
		event.setField(ApplicationConstantTest.SERVER_HOSTNAME_PREFIX_KEY,
				ApplicationConstantTest.SERVER_HOSTNAME_PREFIX);
		event.setField(ApplicationConstantTest.DBNAME_PREFIX_KEY, ApplicationConstantTest.DBNAME_PREFIX);

		Collection<Event> response = FILTER.filter(events, matchListener);

		assertNotNull(event.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, response.size());
		assertEquals(1, matchListener.getMatchCount());

	}

	@Test
	public void filterTestForSparql30() {

		final String auditString = "1653045538286, 172.31.3.205:35612, 172.31.28.105:8182, HTTP_GET, [unknown], [unknown], \"HttpObjectAggregator$AggregatedFullHttpRequest(decodeResult: success, version: HTTP/1.1, content: CompositeByteBuf(ridx: 0, widx: 0, cap: 0, components=0)) GET /sparql HTTP/1.1 Accept: text/csv;q=0.8, application/x-sparqlstar-results+json;q=0.8, application/sparql-results+json;q=0.8, application/json;q=0.8, application/sparql-results+xml, application/xml, text/tab-separated-values;q=0.8, application/x-sparqlstar-results+xml;q=0.8, text/x-tab-separated-values-star;q=0.8, application/x-sparqlstar-results+tsv;q=0.8, application/x-binary-rdf-results-table;q=0.8 Host: localhost:8182 Connection: Keep-Alive User-Agent: Apache-HttpClient/4.5.13 (Java/12.0.2) Accept-Encoding: gzip,deflate content-length: 0\", update=COPY+DEFAULT+TO+GRAPH+%3Chttp%3A%2F%2Fexample.com%2Faddresses%3E";

		Event event = new org.logstash.Event();

		List<Event> events = new ArrayList<>();
		events.add(event);

		event.setField(ApplicationConstantTest.LOG_MESSAGE_KEY, auditString);
		event.setField(ApplicationConstantTest.CLIENT_HOST_KEY, "172.31.3.205:35612");
		event.setField(ApplicationConstantTest.SERVER_HOST_KEY, "172.31.28.105:8182");
		event.setField(ApplicationConstantTest.TIME_STAMP_KEY, "1653045538286");
		event.setField(ApplicationConstantTest.HTTP_HEADERS_KEY,
				"HttpObjectAggregator$AggregatedFullHttpRequest(decodeResult: success, version: HTTP/1.1, content: CompositeByteBuf(ridx: 0, widx: 0, cap: 0, components=0)) GET /sparql HTTP/1.1 Accept: text/csv;q=0.8, application/x-sparqlstar-results+json;q=0.8, application/sparql-results+json;q=0.8, application/json;q=0.8, application/sparql-results+xml, application/xml, text/tab-separated-values;q=0.8, application/x-sparqlstar-results+xml;q=0.8, text/x-tab-separated-values-star;q=0.8, application/x-sparqlstar-results+tsv;q=0.8, application/x-binary-rdf-results-table;q=0.8 Host: localhost:8182 Connection: Keep-Alive User-Agent: Apache-HttpClient/4.5.13 (Java/12.0.2) Accept-Encoding: gzip,deflate content-length: 0");
		event.setField(ApplicationConstantTest.PAYLOAD_KEY,
				"update=COPY+DEFAULT+TO+GRAPH+%3Chttp%3A%2F%2Fexample.com%2Faddresses%3E");
		event.setField(ApplicationConstantTest.CALLERIAM_KEY, "[unknown]");
		event.setField(ApplicationConstantTest.SERVER_HOSTNAME_PREFIX_KEY,
				ApplicationConstantTest.SERVER_HOSTNAME_PREFIX);
		event.setField(ApplicationConstantTest.DBNAME_PREFIX_KEY, ApplicationConstantTest.DBNAME_PREFIX);

		Collection<Event> response = FILTER.filter(events, matchListener);

		assertNotNull(event.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, response.size());
		assertEquals(1, matchListener.getMatchCount());

	}

	@Test
	public void filterTestForSparql31() {

		final String auditString = "1653045538286, 172.31.3.205:35612, 172.31.28.105:8182, HTTP_GET, [unknown], [unknown], \"HttpObjectAggregator$AggregatedFullHttpRequest(decodeResult: success, version: HTTP/1.1, content: CompositeByteBuf(ridx: 0, widx: 0, cap: 0, components=0)) GET /sparql HTTP/1.1 Accept: text/csv;q=0.8, application/x-sparqlstar-results+json;q=0.8, application/sparql-results+json;q=0.8, application/json;q=0.8, application/sparql-results+xml, application/xml, text/tab-separated-values;q=0.8, application/x-sparqlstar-results+xml;q=0.8, text/x-tab-separated-values-star;q=0.8, application/x-sparqlstar-results+tsv;q=0.8, application/x-binary-rdf-results-table;q=0.8 Host: localhost:8182 Connection: Keep-Alive User-Agent: Apache-HttpClient/4.5.13 (Java/12.0.2) Accept-Encoding: gzip,deflate content-length: 0\", query=PREFIX+%3A+++%3Chttp%3A%2F%2Fexample%2F%3E%0D%0ASELECT+*+%0D%0A%7B++%3Fs+%3Aitem%2F%3Aprice+%3Fx+.+%7D";

		Event event = new org.logstash.Event();

		List<Event> events = new ArrayList<>();
		events.add(event);

		event.setField(ApplicationConstantTest.LOG_MESSAGE_KEY, auditString);
		event.setField(ApplicationConstantTest.CLIENT_HOST_KEY, "172.31.3.205:35612");
		event.setField(ApplicationConstantTest.SERVER_HOST_KEY, "172.31.28.105:8182");
		event.setField(ApplicationConstantTest.TIME_STAMP_KEY, "1653045538286");
		event.setField(ApplicationConstantTest.HTTP_HEADERS_KEY,
				"HttpObjectAggregator$AggregatedFullHttpRequest(decodeResult: success, version: HTTP/1.1, content: CompositeByteBuf(ridx: 0, widx: 0, cap: 0, components=0)) GET /sparql HTTP/1.1 Accept: text/csv;q=0.8, application/x-sparqlstar-results+json;q=0.8, application/sparql-results+json;q=0.8, application/json;q=0.8, application/sparql-results+xml, application/xml, text/tab-separated-values;q=0.8, application/x-sparqlstar-results+xml;q=0.8, text/x-tab-separated-values-star;q=0.8, application/x-sparqlstar-results+tsv;q=0.8, application/x-binary-rdf-results-table;q=0.8 Host: localhost:8182 Connection: Keep-Alive User-Agent: Apache-HttpClient/4.5.13 (Java/12.0.2) Accept-Encoding: gzip,deflate content-length: 0");
		event.setField(ApplicationConstantTest.PAYLOAD_KEY,
				"query=PREFIX+%3A+++%3Chttp%3A%2F%2Fexample%2F%3E%0D%0ASELECT+*+%0D%0A%7B++%3Fs+%3Aitem%2F%3Aprice+%3Fx+.+%7D");
		event.setField(ApplicationConstantTest.CALLERIAM_KEY, "[unknown]");
		event.setField(ApplicationConstantTest.SERVER_HOSTNAME_PREFIX_KEY,
				ApplicationConstantTest.SERVER_HOSTNAME_PREFIX);
		event.setField(ApplicationConstantTest.DBNAME_PREFIX_KEY, ApplicationConstantTest.DBNAME_PREFIX);

		Collection<Event> response = FILTER.filter(events, matchListener);

		assertNotNull(event.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, response.size());
		assertEquals(1, matchListener.getMatchCount());

	}

	@Test
	public void filterTestForSparql32() {

		final String auditString = "1653045538286, 172.31.3.205:35612, 172.31.28.105:8182, HTTP_GET, [unknown], [unknown], \"HttpObjectAggregator$AggregatedFullHttpRequest(decodeResult: success, version: HTTP/1.1, content: CompositeByteBuf(ridx: 0, widx: 0, cap: 0, components=0)) GET /sparql HTTP/1.1 Accept: text/csv;q=0.8, application/x-sparqlstar-results+json;q=0.8, application/sparql-results+json;q=0.8, application/json;q=0.8, application/sparql-results+xml, application/xml, text/tab-separated-values;q=0.8, application/x-sparqlstar-results+xml;q=0.8, text/x-tab-separated-values-star;q=0.8, application/x-sparqlstar-results+tsv;q=0.8, application/x-binary-rdf-results-table;q=0.8 Host: localhost:8182 Connection: Keep-Alive User-Agent: Apache-HttpClient/4.5.13 (Java/12.0.2) Accept-Encoding: gzip,deflate content-length: 0\", query=PREFIX+foaf%3A+++%3Chttp%3A%2F%2Fxmlns.com%2Ffoaf%2F0.1%2F%3E%0D%0ADESCRIBE+%3Fx+%3Fy+%3Chttp%3A%2F%2Fexample.org%2F%3E%0D%0AWHERE++++%7B%3Fx+foaf%3Aknows+%3Fy%7D";

		Event event = new org.logstash.Event();

		List<Event> events = new ArrayList<>();
		events.add(event);

		event.setField(ApplicationConstantTest.LOG_MESSAGE_KEY, auditString);
		event.setField(ApplicationConstantTest.CLIENT_HOST_KEY, "172.31.3.205:35612");
		event.setField(ApplicationConstantTest.SERVER_HOST_KEY, "172.31.28.105:8182");
		event.setField(ApplicationConstantTest.TIME_STAMP_KEY, "1653045538286");
		event.setField(ApplicationConstantTest.HTTP_HEADERS_KEY,
				"HttpObjectAggregator$AggregatedFullHttpRequest(decodeResult: success, version: HTTP/1.1, content: CompositeByteBuf(ridx: 0, widx: 0, cap: 0, components=0)) GET /sparql HTTP/1.1 Accept: text/csv;q=0.8, application/x-sparqlstar-results+json;q=0.8, application/sparql-results+json;q=0.8, application/json;q=0.8, application/sparql-results+xml, application/xml, text/tab-separated-values;q=0.8, application/x-sparqlstar-results+xml;q=0.8, text/x-tab-separated-values-star;q=0.8, application/x-sparqlstar-results+tsv;q=0.8, application/x-binary-rdf-results-table;q=0.8 Host: localhost:8182 Connection: Keep-Alive User-Agent: Apache-HttpClient/4.5.13 (Java/12.0.2) Accept-Encoding: gzip,deflate content-length: 0");
		event.setField(ApplicationConstantTest.PAYLOAD_KEY,
				"query=PREFIX+foaf%3A+++%3Chttp%3A%2F%2Fxmlns.com%2Ffoaf%2F0.1%2F%3E%0D%0ADESCRIBE+%3Fx+%3Fy+%3Chttp%3A%2F%2Fexample.org%2F%3E%0D%0AWHERE++++%7B%3Fx+foaf%3Aknows+%3Fy%7D");
		event.setField(ApplicationConstantTest.CALLERIAM_KEY, "[unknown]");
		event.setField(ApplicationConstantTest.SERVER_HOSTNAME_PREFIX_KEY,
				ApplicationConstantTest.SERVER_HOSTNAME_PREFIX);
		event.setField(ApplicationConstantTest.DBNAME_PREFIX_KEY, ApplicationConstantTest.DBNAME_PREFIX);

		Collection<Event> response = FILTER.filter(events, matchListener);

		assertNotNull(event.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, response.size());
		assertEquals(1, matchListener.getMatchCount());

	}

	@Test
	public void filterTestForSparql33() {

		final String auditString = "1653045538286, 172.31.3.205:35612, 172.31.28.105:8182, HTTP_GET, [unknown], [unknown], \"HttpObjectAggregator$AggregatedFullHttpRequest(decodeResult: success, version: HTTP/1.1, content: CompositeByteBuf(ridx: 0, widx: 0, cap: 0, components=0)) GET /sparql HTTP/1.1 Accept: text/csv;q=0.8, application/x-sparqlstar-results+json;q=0.8, application/sparql-results+json;q=0.8, application/json;q=0.8, application/sparql-results+xml, application/xml, text/tab-separated-values;q=0.8, application/x-sparqlstar-results+xml;q=0.8, text/x-tab-separated-values-star;q=0.8, application/x-sparqlstar-results+tsv;q=0.8, application/x-binary-rdf-results-table;q=0.8 Host: localhost:8182 Connection: Keep-Alive User-Agent: Apache-HttpClient/4.5.13 (Java/12.0.2) Accept-Encoding: gzip,deflate content-length: 0\", query=PREFIX+foaf%3A+%3Chttp%3A%2F%2Fxmlns.com%2Ffoaf%2F0.1%2F%3E%0D%0ASELECT++%3Fname%0D%0AFROM++++%3Chttp%3A%2F%2Fexample.org%2Ffoaf%2FaliceFoaf%3E%0D%0AWHERE+++%7B+%3Fx+foaf%3Aname+%3Fname+%7D\r\n";

		Event event = new org.logstash.Event();

		List<Event> events = new ArrayList<>();
		events.add(event);

		event.setField(ApplicationConstantTest.LOG_MESSAGE_KEY, auditString);
		event.setField(ApplicationConstantTest.CLIENT_HOST_KEY, "172.31.3.205:35612");
		event.setField(ApplicationConstantTest.SERVER_HOST_KEY, "172.31.28.105:8182");
		event.setField(ApplicationConstantTest.TIME_STAMP_KEY, "1653045538286");
		event.setField(ApplicationConstantTest.HTTP_HEADERS_KEY,
				"HttpObjectAggregator$AggregatedFullHttpRequest(decodeResult: success, version: HTTP/1.1, content: CompositeByteBuf(ridx: 0, widx: 0, cap: 0, components=0)) GET /sparql HTTP/1.1 Accept: text/csv;q=0.8, application/x-sparqlstar-results+json;q=0.8, application/sparql-results+json;q=0.8, application/json;q=0.8, application/sparql-results+xml, application/xml, text/tab-separated-values;q=0.8, application/x-sparqlstar-results+xml;q=0.8, text/x-tab-separated-values-star;q=0.8, application/x-sparqlstar-results+tsv;q=0.8, application/x-binary-rdf-results-table;q=0.8 Host: localhost:8182 Connection: Keep-Alive User-Agent: Apache-HttpClient/4.5.13 (Java/12.0.2) Accept-Encoding: gzip,deflate content-length: 0");
		event.setField(ApplicationConstantTest.PAYLOAD_KEY,
				"query=PREFIX+foaf%3A+%3Chttp%3A%2F%2Fxmlns.com%2Ffoaf%2F0.1%2F%3E%0D%0ASELECT++%3Fname%0D%0AFROM++++%3Chttp%3A%2F%2Fexample.org%2Ffoaf%2FaliceFoaf%3E%0D%0AWHERE+++%7B+%3Fx+foaf%3Aname+%3Fname+%7D\r\n");
		event.setField(ApplicationConstantTest.CALLERIAM_KEY, "[unknown]");
		event.setField(ApplicationConstantTest.SERVER_HOSTNAME_PREFIX_KEY,
				ApplicationConstantTest.SERVER_HOSTNAME_PREFIX);
		event.setField(ApplicationConstantTest.DBNAME_PREFIX_KEY, ApplicationConstantTest.DBNAME_PREFIX);

		Collection<Event> response = FILTER.filter(events, matchListener);

		assertNotNull(event.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, response.size());
		assertEquals(1, matchListener.getMatchCount());

	}

	@Test
	public void filterTestForSparql34() {

		final String auditString = "1653045538286, 172.31.3.205:35612, 172.31.28.105:8182, HTTP_GET, [unknown], [unknown], \"HttpObjectAggregator$AggregatedFullHttpRequest(decodeResult: success, version: HTTP/1.1, content: CompositeByteBuf(ridx: 0, widx: 0, cap: 0, components=0)) GET /sparql HTTP/1.1 Accept: text/csv;q=0.8, application/x-sparqlstar-results+json;q=0.8, application/sparql-results+json;q=0.8, application/json;q=0.8, application/sparql-results+xml, application/xml, text/tab-separated-values;q=0.8, application/x-sparqlstar-results+xml;q=0.8, text/x-tab-separated-values-star;q=0.8, application/x-sparqlstar-results+tsv;q=0.8, application/x-binary-rdf-results-table;q=0.8 Host: localhost:8182 Connection: Keep-Alive User-Agent: Apache-HttpClient/4.5.13 (Java/12.0.2) Accept-Encoding: gzip,deflate content-length: 0\", query=PREFIX+foaf%3A+%3Chttp%3A%2F%2Fxmlns.com%2Ffoaf%2F0.1%2F%3E%0D%0APREFIX+dc%3A+%3Chttp%3A%2F%2Fpurl.org%2Fdc%2Felements%2F1.1%2F%3E%0D%0A%0D%0ASELECT+%3Fwho+%3Fg+%3Fmbox%0D%0AFROM+%3Chttp%3A%2F%2Fexample.org%2Fdft.ttl%3E%0D%0AFROM+NAMED+%3Chttp%3A%2F%2Fexample.org%2Falice%3E%0D%0AFROM+NAMED+%3Chttp%3A%2F%2Fexample.org%2Fbob%3E%0D%0AWHERE%0D%0A%7B%0D%0A+++%3Fg+dc%3Apublisher+%3Fwho+.%0D%0A+++GRAPH+%3Fg+%7B+%3Fx+foaf%3Ambox+%3Fmbox+%7D%0D%0A%7D";

		Event event = new org.logstash.Event();

		List<Event> events = new ArrayList<>();
		events.add(event);

		event.setField(ApplicationConstantTest.LOG_MESSAGE_KEY, auditString);
		event.setField(ApplicationConstantTest.CLIENT_HOST_KEY, "172.31.3.205:35612");
		event.setField(ApplicationConstantTest.SERVER_HOST_KEY, "172.31.28.105:8182");
		event.setField(ApplicationConstantTest.TIME_STAMP_KEY, "1653045538286");
		event.setField(ApplicationConstantTest.HTTP_HEADERS_KEY,
				"HttpObjectAggregator$AggregatedFullHttpRequest(decodeResult: success, version: HTTP/1.1, content: CompositeByteBuf(ridx: 0, widx: 0, cap: 0, components=0)) GET /sparql HTTP/1.1 Accept: text/csv;q=0.8, application/x-sparqlstar-results+json;q=0.8, application/sparql-results+json;q=0.8, application/json;q=0.8, application/sparql-results+xml, application/xml, text/tab-separated-values;q=0.8, application/x-sparqlstar-results+xml;q=0.8, text/x-tab-separated-values-star;q=0.8, application/x-sparqlstar-results+tsv;q=0.8, application/x-binary-rdf-results-table;q=0.8 Host: localhost:8182 Connection: Keep-Alive User-Agent: Apache-HttpClient/4.5.13 (Java/12.0.2) Accept-Encoding: gzip,deflate content-length: 0");
		event.setField(ApplicationConstantTest.PAYLOAD_KEY,
				"query=PREFIX+foaf%3A+%3Chttp%3A%2F%2Fxmlns.com%2Ffoaf%2F0.1%2F%3E%0D%0APREFIX+dc%3A+%3Chttp%3A%2F%2Fpurl.org%2Fdc%2Felements%2F1.1%2F%3E%0D%0A%0D%0ASELECT+%3Fwho+%3Fg+%3Fmbox%0D%0AFROM+%3Chttp%3A%2F%2Fexample.org%2Fdft.ttl%3E%0D%0AFROM+NAMED+%3Chttp%3A%2F%2Fexample.org%2Falice%3E%0D%0AFROM+NAMED+%3Chttp%3A%2F%2Fexample.org%2Fbob%3E%0D%0AWHERE%0D%0A%7B%0D%0A+++%3Fg+dc%3Apublisher+%3Fwho+.%0D%0A+++GRAPH+%3Fg+%7B+%3Fx+foaf%3Ambox+%3Fmbox+%7D%0D%0A%7D");
		event.setField(ApplicationConstantTest.CALLERIAM_KEY, "[unknown]");
		event.setField(ApplicationConstantTest.SERVER_HOSTNAME_PREFIX_KEY,
				ApplicationConstantTest.SERVER_HOSTNAME_PREFIX);
		event.setField(ApplicationConstantTest.DBNAME_PREFIX_KEY, ApplicationConstantTest.DBNAME_PREFIX);

		Collection<Event> response = FILTER.filter(events, matchListener);

		assertNotNull(event.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, response.size());
		assertEquals(1, matchListener.getMatchCount());

	}

	@Test
	public void filterTestForSparql35() {

		final String auditString = "1629192925919, 172.31.20.251:47148, 172.31.29.87:8182, HTTP_POST, [unknown], [unknown], \"HttpObjectAggregator$AggregatedFullHttpRequest(decodeResult: success, version: HTTP/1.1, content: CompositeByteBuf(ridx: 0, widx: 68, cap: 68, components=1)) POST /sparql HTTP/1.1 Host: database2-neptune.cluster-cllxmp6vknzx.ap-south-1.neptune.amazonaws.com:8182 Accept-Encoding: identity Accept: application/sparql-results+json content-type: application/x-www-form-urlencoded Content-Length: 68 User-Agent: python-urllib3/1.26.5\", update=CLEAR++GRAPH+%3Chttp%3A%2F%2Fexample.com%2Fnames%3E\r\n";

		Event event = new org.logstash.Event();

		List<Event> events = new ArrayList<>();
		events.add(event);

		event.setField(ApplicationConstantTest.LOG_MESSAGE_KEY, auditString);
		event.setField(ApplicationConstantTest.CLIENT_HOST_KEY, "172.31.20.251:47148");
		event.setField(ApplicationConstantTest.SERVER_HOST_KEY, "172.31.29.87:8182");
		event.setField(ApplicationConstantTest.TIME_STAMP_KEY, "1629192925919");
		event.setField(ApplicationConstantTest.HTTP_HEADERS_KEY,
				"HttpObjectAggregator$AggregatedFullHttpRequest(decodeResult: success, version: HTTP/1.1, content: CompositeByteBuf(ridx: 0, widx: 68, cap: 68, components=1)) POST /sparql HTTP/1.1 Host: database2-neptune.cluster-cllxmp6vknzx.ap-south-1.neptune.amazonaws.com:8182 Accept-Encoding: identity Accept: application/sparql-results+json content-type: application/x-www-form-urlencoded Content-Length: 68 User-Agent: python-urllib3/1.26.5");
		event.setField(ApplicationConstantTest.PAYLOAD_KEY,
				"update=CLEAR++GRAPH+%3Chttp%3A%2F%2Fexample.com%2Fnames%3E\r\n");
		event.setField(ApplicationConstantTest.CALLERIAM_KEY, "[unknown]");
		event.setField(ApplicationConstantTest.SERVER_HOSTNAME_PREFIX_KEY,
				ApplicationConstantTest.SERVER_HOSTNAME_PREFIX);
		event.setField(ApplicationConstantTest.DBNAME_PREFIX_KEY, ApplicationConstantTest.DBNAME_PREFIX);

		Collection<Event> response = FILTER.filter(events, matchListener);

		assertNotNull(event.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, response.size());
		assertEquals(1, matchListener.getMatchCount());

	}

	@Test
	public void filterTestForSparql36() {

		final String auditString = "1629192925919, 172.31.20.251:47148, 172.31.29.87:8182, HTTP_POST, [unknown], [unknown], \"HttpObjectAggregator$AggregatedFullHttpRequest(decodeResult: success, version: HTTP/1.1, content: CompositeByteBuf(ridx: 0, widx: 68, cap: 68, components=1)) POST /sparql HTTP/1.1 Host: database2-neptune.cluster-cllxmp6vknzx.ap-south-1.neptune.amazonaws.com:8182 Accept-Encoding: identity Accept: application/sparql-results+json content-type: application/x-www-form-urlencoded Content-Length: 68 User-Agent: python-urllib3/1.26.5\", update=PREFIX+dc%3A++%3Chttp%3A%2F%2Fpurl.org%2Fdc%2Felements%2F1.1%2F%3E%0D%0APREFIX+dcmitype%3A+%3Chttp%3A%2F%2Fpurl.org%2Fdc%2Fdcmitype%2F%3E%0D%0APREFIX+xsd%3A+%3Chttp%3A%2F%2Fwww.w3.org%2F2001%2FXMLSchema%23%3E%0D%0A%0D%0AINSERT%0D%0A++%7B+GRAPH+%3Chttp%3A%2F%2Fexample%2FbookStore2%3E+%7B+%3Fbook+%3Fp+%3Fv+%7D+%7D%0D%0AWHERE%0D%0A++%7B+GRAPH++%3Chttp%3A%2F%2Fexample%2FbookStore%3E%0D%0A+++++%7B+%3Fbook+dc%3Adate+%3Fdate+.+%0D%0A+++++++FILTER+%28+%3Fdate+%3C+%222000-01-01T00%3A00%3A00-02%3A00%22%5E%5Exsd%3AdateTime+%29%0D%0A+++++++%3Fbook+%3Fp+%3Fv%0D%0A+++++%7D%0D%0A++%7D+%3B%0D%0A%0D%0AWITH+%3Chttp%3A%2F%2Fexample%2FbookStore%3E%0D%0ADELETE%0D%0A+%7B+%3Fbook+%3Fp+%3Fv+%7D%0D%0AWHERE%0D%0A+%7B+%3Fbook+dc%3Adate+%3Fdate+%3B%0D%0A+++++++++dc%3Atype+dcmitype%3APhysicalObject+.%0D%0A+++FILTER+%28+%3Fdate+%3C+%222000-01-01T00%3A00%3A00-02%3A00%22%5E%5Exsd%3AdateTime+%29+%0D%0A+++%3Fbook+%3Fp+%3Fv%0D%0A+%7D+\r\n";

		Event event = new org.logstash.Event();

		List<Event> events = new ArrayList<>();
		events.add(event);

		event.setField(ApplicationConstantTest.LOG_MESSAGE_KEY, auditString);
		event.setField(ApplicationConstantTest.CLIENT_HOST_KEY, "172.31.20.251:47148");
		event.setField(ApplicationConstantTest.SERVER_HOST_KEY, "172.31.29.87:8182");
		event.setField(ApplicationConstantTest.TIME_STAMP_KEY, "1629192925919");
		event.setField(ApplicationConstantTest.HTTP_HEADERS_KEY,
				"HttpObjectAggregator$AggregatedFullHttpRequest(decodeResult: success, version: HTTP/1.1, content: CompositeByteBuf(ridx: 0, widx: 68, cap: 68, components=1)) POST /sparql HTTP/1.1 Host: database2-neptune.cluster-cllxmp6vknzx.ap-south-1.neptune.amazonaws.com:8182 Accept-Encoding: identity Accept: application/sparql-results+json content-type: application/x-www-form-urlencoded Content-Length: 68 User-Agent: python-urllib3/1.26.5");
		event.setField(ApplicationConstantTest.PAYLOAD_KEY,
				"update=PREFIX+dc%3A++%3Chttp%3A%2F%2Fpurl.org%2Fdc%2Felements%2F1.1%2F%3E%0D%0APREFIX+dcmitype%3A+%3Chttp%3A%2F%2Fpurl.org%2Fdc%2Fdcmitype%2F%3E%0D%0APREFIX+xsd%3A+%3Chttp%3A%2F%2Fwww.w3.org%2F2001%2FXMLSchema%23%3E%0D%0A%0D%0AINSERT%0D%0A++%7B+GRAPH+%3Chttp%3A%2F%2Fexample%2FbookStore2%3E+%7B+%3Fbook+%3Fp+%3Fv+%7D+%7D%0D%0AWHERE%0D%0A++%7B+GRAPH++%3Chttp%3A%2F%2Fexample%2FbookStore%3E%0D%0A+++++%7B+%3Fbook+dc%3Adate+%3Fdate+.+%0D%0A+++++++FILTER+%28+%3Fdate+%3C+%222000-01-01T00%3A00%3A00-02%3A00%22%5E%5Exsd%3AdateTime+%29%0D%0A+++++++%3Fbook+%3Fp+%3Fv%0D%0A+++++%7D%0D%0A++%7D+%3B%0D%0A%0D%0AWITH+%3Chttp%3A%2F%2Fexample%2FbookStore%3E%0D%0ADELETE%0D%0A+%7B+%3Fbook+%3Fp+%3Fv+%7D%0D%0AWHERE%0D%0A+%7B+%3Fbook+dc%3Adate+%3Fdate+%3B%0D%0A+++++++++dc%3Atype+dcmitype%3APhysicalObject+.%0D%0A+++FILTER+%28+%3Fdate+%3C+%222000-01-01T00%3A00%3A00-02%3A00%22%5E%5Exsd%3AdateTime+%29+%0D%0A+++%3Fbook+%3Fp+%3Fv%0D%0A+%7D+\r\n");
		event.setField(ApplicationConstantTest.CALLERIAM_KEY, "[unknown]");
		event.setField(ApplicationConstantTest.SERVER_HOSTNAME_PREFIX_KEY,
				ApplicationConstantTest.SERVER_HOSTNAME_PREFIX);
		event.setField(ApplicationConstantTest.DBNAME_PREFIX_KEY, ApplicationConstantTest.DBNAME_PREFIX);

		Collection<Event> response = FILTER.filter(events, matchListener);

		assertNotNull(event.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, response.size());
		assertEquals(1, matchListener.getMatchCount());

	}

	@Test
	public void notNeptuneLogTest() {

		final String auditString = "1629192925919, 172.31.20.251:47148, 172.31.29.87:8182, HTTP_POST, [unknown], [unknown], \"HttpObjectAggregator$AggregatedFullHttpRequest(decodeResult: success, version: HTTP/1.1, content: CompositeByteBuf(ridx: 0, widx: 68, cap: 68, components=1)) POST  HTTP/1.1 Host: database2-neptune.cluster-cllxmp6vknzx.ap-south-1.neptune.amazonaws.com:8182 Accept-Encoding: identity Accept: application/-results+json content-type: application/x-www-form-urlencoded Content-Length: 68 User-Agent: python-urllib3/1.26.5\", query=%0ASELECT+%2A+WHERE+%7B%0A++++%3Fs+%3Fp+%3Fo%0A%7D+LIMIT+40%0A";

		Event event = new org.logstash.Event();

		List<Event> events = new ArrayList<>();
		events.add(event);

		event.setField(ApplicationConstantTest.LOG_MESSAGE_KEY, auditString);
		event.setField(ApplicationConstantTest.CLIENT_HOST_KEY, "172.31.20.251:47148");
		event.setField(ApplicationConstantTest.SERVER_HOST_KEY, "172.31.29.87:8182");
		event.setField(ApplicationConstantTest.TIME_STAMP_KEY, "1629192925919");
		event.setField(ApplicationConstantTest.HTTP_HEADERS_KEY,
				"HttpObjectAggregator$AggregatedFullHttpRequest(decodeResult: success, version: HTTP/1.1, content: CompositeByteBuf(ridx: 0, widx: 68, cap: 68, components=1)) POST / HTTP/1.1 Host: database2-neptune.cluster-cllxmp6vknzx.ap-south-1.neptune.amazonaws.com:8182 Accept-Encoding: identity Accept: application/sparql-results+json content-type: application/x-www-form-urlencoded Content-Length: 68 User-Agent: python-urllib3/1.26.5");
		event.setField(ApplicationConstantTest.PAYLOAD_KEY,
				"query=%0ASELECT+%2A+WHERE+%7B%0A++++%3Fs+%3Fp+%3Fo%0A%7D+LIMIT+40%0A");
		event.setField(ApplicationConstantTest.CALLERIAM_KEY, "[unknown]");
		event.setField(ApplicationConstantTest.SERVER_HOSTNAME_PREFIX_KEY,
				ApplicationConstantTest.SERVER_HOSTNAME_PREFIX);
		event.setField(ApplicationConstantTest.DBNAME_PREFIX_KEY, ApplicationConstantTest.DBNAME_PREFIX);
		Collection<Event> response = FILTER.filter(events, matchListener);

		assertEquals(0, response.size());
		assertEquals(0, matchListener.getMatchCount());

	}

	@Test
	public void TestException() {

		final String auditString = "1629192925919, 172.31.20.251:47148, 172.31.29.87:8182, HTTP_POST, [unknown], [unknown], \\\"HttpObjectAggregator$AggregatedFullHttpRequest(decodeResult: success, version: HTTP/1.1, content: CompositeByteBuf(ridx: 0, widx: 68, cap: 68, components=1)) POST /sparql HTTP/1.1 Host: database2-neptune.cluster-cllxmp6vknzx.ap-south-1.neptune.amazonaws.com:8182 Accept-Encoding: identity Accept: application/sparql-results+json content-type: application/x-www-form-urlencoded Content-Length: 68 User-Agent: python-urllib3/1.26.5\\\", %0ASELECT+%2A+WHERE+%7B%0A++++%3Fs+%3Fp+%3Fo%0A%7D+LIMIT+40%0A";

		Event event = new org.logstash.Event();

		List<Event> events = new ArrayList<>();
		events.add(event);

		event.setField(ApplicationConstantTest.LOG_MESSAGE_KEY, auditString);
		event.setField(ApplicationConstantTest.CLIENT_HOST_KEY, "172.31.20.251:47148");
		event.setField(ApplicationConstantTest.SERVER_HOST_KEY, "172.31.29.87:8182");
		event.setField(ApplicationConstantTest.TIME_STAMP_KEY, "1629192925919");
		event.setField(ApplicationConstantTest.HTTP_HEADERS_KEY,
				"HttpObjectAggregator$AggregatedFullHttpRequest(decodeResult: success, version: HTTP/1.1, content: CompositeByteBuf(ridx: 0, widx: 68, cap: 68, components=1)) POST / HTTP/1.1 Host: database2-neptune.cluster-cllxmp6vknzx.ap-south-1.neptune.amazonaws.com:8182 Accept-Encoding: identity Accept: application/sparql-results+json content-type: application/x-www-form-urlencoded Content-Length: 68 User-Agent: python-urllib3/1.26.5");
		event.setField(ApplicationConstantTest.PAYLOAD_KEY,
				"%0ASELECT+%2A+WHERE+%7B%0A++++%3Fs+%3Fp+%3Fo%0A%7D+LIMIT+40%0A");
		event.setField(ApplicationConstantTest.CALLERIAM_KEY, "[unknown]");
		event.setField(ApplicationConstantTest.SERVER_HOSTNAME_PREFIX_KEY,
				ApplicationConstantTest.SERVER_HOSTNAME_PREFIX);
		event.setField(ApplicationConstantTest.DBNAME_PREFIX_KEY, ApplicationConstantTest.DBNAME_PREFIX);

		Collection<Event> response = FILTER.filter(events, matchListener);

		assertEquals(0, response.size());
		assertEquals(0, matchListener.getMatchCount());

	}

	@Test
	public void filterTestForSparqlInvalidQuery() {

		final String auditString = "1629192925919, 172.31.20.251:47148, 172.31.29.87:8182, HTTP_POST, [unknown], [unknown], \"HttpObjectAggregator$AggregatedFullHttpRequest(decodeResult: success, version: HTTP/1.1, content: CompositeByteBuf(ridx: 0, widx: 68, cap: 68, components=1)) POST /sparql HTTP/1.1 Host: database2-neptune.cluster-cllxmp6vknzx.ap-south-1.neptune.amazonaws.com:8182 Accept-Encoding: identity Accept: application/sparql-results+json content-type: application/x-www-form-urlencoded Content-Length: 68 User-Agent: python-urllib3/1.26.5\", update=dc%3A++%3Chttp%3A%2F%2Fpurl.org%2Fdc%2Felements%2F1.1%2F%3E%0D%0APREFIX+dcmitype%3A+%3Chttp%3A%2F%2Fpurl.org%2Fdc%2Fdcmitype%2F%3E%0D%0APREFIX+xsd%3A+%3Chttp%3A%2F%2Fwww.w3.org%2F2001%2FXMLSchema%23%3E%0D%0A%0D%0AINSERT%0D%0A++%7B+GRAPH+%3Chttp%3A%2F%2Fexample%2FbookStore2%3E+%7B+%3Fbook+%3Fp+%3Fv+%7D+%7D%0D%0AWHERE%0D%0A++%7B+GRAPH++%3Chttp%3A%2F%2Fexample%2FbookStore%3E%0D%0A+++++%7B+%3Fbook+dc%3Adate+%3Fdate+.+%0D%0A+++++++FILTER+%28+%3Fdate+%3C+%222000-01-01T00%3A00%3A00-02%3A00%22%5E%5Exsd%3AdateTime+%29%0D%0A+++++++%3Fbook+%3Fp+%3Fv%0D%0A+++++%7D%0D%0A++%7D+%3B%0D%0A%0D%0AWITH+%3Chttp%3A%2F%2Fexample%2FbookStore%3E%0D%0ADELETE%0D%0A+%7B+%3Fbook+%3Fp+%3Fv+%7D%0D%0AWHERE%0D%0A+%7B+%3Fbook+dc%3Adate+%3Fdate+%3B%0D%0A+++++++++dc%3Atype+dcmitype%3APhysicalObject+.%0D%0A+++FILTER+%28+%3Fdate+%3C+%222000-01-01T00%3A00%3A00-02%3A00%22%5E%5Exsd%3AdateTime+%29+%0D%0A+++%3Fbook+%3Fp+%3Fv%0D%0A+%7D+\r\n";

		Event event = new org.logstash.Event();

		List<Event> events = new ArrayList<>();
		events.add(event);

		event.setField(ApplicationConstantTest.LOG_MESSAGE_KEY, auditString);
		event.setField(ApplicationConstantTest.CLIENT_HOST_KEY, "172.31.20.251:47148");
		event.setField(ApplicationConstantTest.SERVER_HOST_KEY, "172.31.29.87:8182");
		event.setField(ApplicationConstantTest.TIME_STAMP_KEY, "1629192925919");
		event.setField(ApplicationConstantTest.HTTP_HEADERS_KEY,
				"HttpObjectAggregator$AggregatedFullHttpRequest(decodeResult: success, version: HTTP/1.1, content: CompositeByteBuf(ridx: 0, widx: 68, cap: 68, components=1)) POST /sparql HTTP/1.1 Host: database2-neptune.cluster-cllxmp6vknzx.ap-south-1.neptune.amazonaws.com:8182 Accept-Encoding: identity Accept: application/sparql-results+json content-type: application/x-www-form-urlencoded Content-Length: 68 User-Agent: python-urllib3/1.26.5");
		event.setField(ApplicationConstantTest.PAYLOAD_KEY,
				"update=dc%3A++%3Chttp%3A%2F%2Fpurl.org%2Fdc%2Felements%2F1.1%2F%3E%0D%0APREFIX+dcmitype%3A+%3Chttp%3A%2F%2Fpurl.org%2Fdc%2Fdcmitype%2F%3E%0D%0APREFIX+xsd%3A+%3Chttp%3A%2F%2Fwww.w3.org%2F2001%2FXMLSchema%23%3E%0D%0A%0D%0AINSERT%0D%0A++%7B+GRAPH+%3Chttp%3A%2F%2Fexample%2FbookStore2%3E+%7B+%3Fbook+%3Fp+%3Fv+%7D+%7D%0D%0AWHERE%0D%0A++%7B+GRAPH++%3Chttp%3A%2F%2Fexample%2FbookStore%3E%0D%0A+++++%7B+%3Fbook+dc%3Adate+%3Fdate+.+%0D%0A+++++++FILTER+%28+%3Fdate+%3C+%222000-01-01T00%3A00%3A00-02%3A00%22%5E%5Exsd%3AdateTime+%29%0D%0A+++++++%3Fbook+%3Fp+%3Fv%0D%0A+++++%7D%0D%0A++%7D+%3B%0D%0A%0D%0AWITH+%3Chttp%3A%2F%2Fexample%2FbookStore%3E%0D%0ADELETE%0D%0A+%7B+%3Fbook+%3Fp+%3Fv+%7D%0D%0AWHERE%0D%0A+%7B+%3Fbook+dc%3Adate+%3Fdate+%3B%0D%0A+++++++++dc%3Atype+dcmitype%3APhysicalObject+.%0D%0A+++FILTER+%28+%3Fdate+%3C+%222000-01-01T00%3A00%3A00-02%3A00%22%5E%5Exsd%3AdateTime+%29+%0D%0A+++%3Fbook+%3Fp+%3Fv%0D%0A+%7D+\r\n");
		event.setField(ApplicationConstantTest.CALLERIAM_KEY, "[unknown]");
		event.setField(ApplicationConstantTest.SERVER_HOSTNAME_PREFIX_KEY,
				ApplicationConstantTest.SERVER_HOSTNAME_PREFIX);
		event.setField(ApplicationConstantTest.DBNAME_PREFIX_KEY, ApplicationConstantTest.DBNAME_PREFIX);

		Collection<Event> response = FILTER.filter(events, matchListener);

		assertNull(event.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(0, response.size());
		assertEquals(0, matchListener.getMatchCount());

	}

	@Test
	public void filterTestForGremlinInvalidQuery() {

		final String auditString = "1629182126440, 172.31.20.251:47156, 172.31.29.87:8182, Websocket, [unknown], [unknown], [unknown], \"RequestMes***REMOVED***ge{, requestId=d74e4a24-292e-4284-94f3-142538326d91, op='eval', processor='', args={gremlin= g.E.valueMap().V().valueMap() , aliases={g=g}}}\"";

		Event event = new org.logstash.Event();
		List<Event> events = new ArrayList<>();
		events.add(event);
		event.setField(ApplicationConstantTest.LOG_MESSAGE_KEY, auditString);
		event.setField(ApplicationConstantTest.CLIENT_HOST_KEY, "172.31.20.251:47156");
		event.setField(ApplicationConstantTest.SERVER_HOST_KEY, "172.31.29.87:8182");
		event.setField(ApplicationConstantTest.TIME_STAMP_KEY, "1629182126440");
		event.setField(ApplicationConstantTest.HTTP_HEADERS_KEY, "[unknown]");
		event.setField(ApplicationConstantTest.PAYLOAD_KEY,
				"RequestMes***REMOVED***ge{, requestId=d74e4a24-292e-4284-94f3-142538326d91, op='eval', processor='', args={gremlin= g.E.valueMap().V().valueMap() , aliases={g=g}}}");
		event.setField(ApplicationConstantTest.CALLERIAM_KEY, "[unknown]");
		event.setField(ApplicationConstantTest.SERVER_HOSTNAME_PREFIX_KEY,
				ApplicationConstantTest.SERVER_HOSTNAME_PREFIX);
		event.setField(ApplicationConstantTest.DBNAME_PREFIX_KEY, ApplicationConstantTest.DBNAME_PREFIX);

		Collection<Event> response = FILTER.filter(events, matchListener);

		assertNull(event.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(0, response.size());
		assertEquals(0, matchListener.getMatchCount());

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
