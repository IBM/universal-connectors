/*
Copyright IBM Corp. 2024 All rights reserved.
SPDX-License-Identifier: Apache-2.0
*/
package com.ibm.guardium.elasticsearch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;
import org.logstash.plugins.ContextImpl;

import com.ibm.guardium.universalconnector.commons.GuardConstants;

import co.elastic.logstash.api.Context;
import co.elastic.logstash.api.Event;
import co.elastic.logstash.api.FilterMatchListener;

public class ElasticsearchGuardiumFilterTest {

	final static Context context = new ContextImpl(null, null);
	final static ElasticsearchGuardiumFilter filter = new ElasticsearchGuardiumFilter("test-id", null, context);

	@Test
	public void filterTestSQLRestApi() {
		final String elasticString="{\"type\":\"audit\", \"timestamp\":\"2023-09-26T11:01:19,220+0000\", \"cluster.uuid\":\"mEtWYmxQSdGY27FoeGubIQ\", \"node.name\":\"node-1\", \"node.id\":\"RkqF7ZGKRuSlu_1EFdGXqQ\", \"host.name\":\"127.0.0.1\", \"host.ip\":\"127.0.0.1\", \"event.type\":\"rest\", \"event.action\":\"authentication_success\", \"authentication.type\":\"REALM\", \"user.name\":\"elastic\", \"user.realm\":\"reserved\", \"origin.type\":\"rest\", \"origin.address\":\"117.98.4.93:1811\", \"realm\":\"reserved\", \"url.path\":\"/_sql\", \"url.query\":\"format=txt&pretty\", \"request.method\":\"POST\", \"request.body\":\"\\n{\\n  \\\"query\\\": \\\"SELECT * FROM hcltesting1\\\"\\n}\\n\", \"request.id\":\"Ol0gXgOtTO6cyIjF1Vx3aA\"}";
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("message", elasticString);
		e.setField("totalOffset", null);
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertEquals(1, results.size());
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, matchListener.getMatchCount());
	}
	@Test
	public void filterTestSQLCli() {
		final String elasticString="{\"type\":\"audit\", \"timestamp\":\"2023-10-03T10:19:02,913+0000\", \"cluster.uuid\":\"mEtWYmxQSdGY27FoeGubIQ\", \"node.name\":\"node-1\", \"node.id\":\"RkqF7ZGKRuSlu_1EFdGXqQ\", \"host.name\":\"127.0.0.1\", \"host.ip\":\"127.0.0.1\", \"event.type\":\"rest\", \"event.action\":\"authentication_success\", \"authentication.type\":\"REALM\", \"user.name\":\"elastic\", \"user.realm\":\"reserved\", \"origin.type\":\"rest\", \"origin.address\":\"127.0.0.1:51844\", \"realm\":\"reserved\", \"url.path\":\"/_sql\", \"url.query\":\"error_trace\", \"request.method\":\"POST\", \"request.body\":\"{\\\"query\\\":\\\"select * from emp\\\",\\\"mode\\\":\\\"cli\\\",\\\"version\\\":\\\"8.9.1\\\",\\\"time_zone\\\":\\\"Z\\\",\\\"request_timeout\\\":\\\"90000ms\\\",\\\"page_timeout\\\":\\\"45000ms\\\",\\\"columnar\\\":false,\\\"binary_format\\\":true,\\\"keep_alive\\\":\\\"5d\\\"}\", \"request.id\":\"UAoVQmBHS46yzkz2g-vkrw\"}";
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("message", elasticString);
		e.setField("totalOffset", "+330");
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertEquals(1, results.size());
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, matchListener.getMatchCount());
	}
	@Test
	public void filterTestDSL() {
		final String elasticString="{\"type\":\"audit\", \"timestamp\":\"2023-10-03T10:24:44,452-0700\", \"cluster.uuid\":\"toy-DTcNR--DrEFRvgZ-FA\", \"node.name\":\"lit4elastic1\", \"node.id\":\"lZRWOJerQq2eGMBfp5XLfA\", \"host.name\":\"lit4elastic1.fyre.ibm.com\", \"host.ip\":\"10.11.75.48\", \"event.type\":\"rest\", \"event.action\":\"authentication_success\", \"authentication.type\":\"REALM\", \"user.name\":\"elastic\", \"user.realm\":\"reserved\", \"origin.type\":\"rest\", \"origin.address\":\"9.43.11.20:50088\", \"realm\":\"reserved\", \"url.path\":\"/_search\", \"request.method\":\"GET\", \"request.body\":\"{\\r\\n  \\\"query\\\": { \\r\\n    \\\"bool\\\": { \\r\\n      \\\"must\\\": [\\r\\n        { \\\"match\\\": { \\\"title\\\":   \\\"Search\\\"        }},\\r\\n        { \\\"match\\\": { \\\"content\\\": \\\"Elasticsearch\\\" }}\\r\\n      ],\\r\\n      \\\"filter\\\": [ \\r\\n        { \\\"term\\\":  { \\\"status\\\": \\\"published\\\" }},\\r\\n        { \\\"range\\\": { \\\"publish_date\\\": { \\\"gte\\\": \\\"2015-01-01\\\" }}}\\r\\n      ]\\r\\n    }\\r\\n  }\\r\\n}\", \"request.id\":\"823lZkbrSGKzIPwhHKPZjA\"}";
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("message", elasticString);
		e.setField("totalOffset", "");
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertEquals(1, results.size());
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, matchListener.getMatchCount());
	}
	@Test
	public void filterTestEQL() {
		final String elasticString="{\"type\":\"audit\", \"timestamp\":\"2023-10-03T11:58:20,320+0000\", \"cluster.uuid\":\"mEtWYmxQSdGY27FoeGubIQ\", \"node.name\":\"node-1\", \"node.id\":\"RkqF7ZGKRuSlu_1EFdGXqQ\", \"host.name\":\"127.0.0.1\", \"host.ip\":\"127.0.0.1\", \"event.type\":\"rest\", \"event.action\":\"authentication_success\", \"authentication.type\":\"REALM\", \"user.name\":\"elastic\", \"user.realm\":\"reserved\", \"origin.type\":\"rest\", \"origin.address\":\"[::1]:49110\", \"realm\":\"reserved\", \"url.path\":\"/my-data-stream/_eql/search\", \"url.query\":\"pretty\", \"request.method\":\"GET\", \"request.body\":\"\\n{\\n  \\\"query\\\": \\\"process where process.name == \\\\\\\"regsvr32.exe\\\\\\\"\\\"\\n}\\n\", \"request.id\":\"dC0OPVcCRVemNRwstfdkMw\"}";
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("message", elasticString);
		e.setField("totalOffset", "+330");
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertEquals(1, results.size());
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, matchListener.getMatchCount());
	}
	@Test
	public void filterTestAPIs() {
		final String elasticString="{\"type\":\"audit\", \"timestamp\":\"2023-10-03T11:49:21,181+0000\", \"cluster.uuid\":\"mEtWYmxQSdGY27FoeGubIQ\", \"node.name\":\"node-1\", \"node.id\":\"RkqF7ZGKRuSlu_1EFdGXqQ\", \"host.name\":\"127.0.0.1\", \"host.ip\":\"127.0.0.1\", \"event.type\":\"rest\", \"event.action\":\"authentication_success\", \"authentication.type\":\"REALM\", \"user.name\":\"elastic\", \"user.realm\":\"reserved\", \"origin.type\":\"rest\", \"origin.address\":\"[::1]:55008\", \"realm\":\"reserved\", \"url.path\":\"/clicklogs/_graph/explore\", \"url.query\":\"pretty\", \"request.method\":\"POST\", \"request.body\":\"\\n{\\n  \\\"query\\\": {\\n    \\\"match\\\": {\\n      \\\"query.raw\\\": \\\"midi\\\"\\n    }\\n  },\\n  \\\"controls\\\": {\\n    \\\"use_significance\\\": false,        \\n    \\\"sample_size\\\": 2000,              \\n    \\\"timeout\\\": 2000,                  \\n    \\\"sample_diversity\\\": {             \\n      \\\"field\\\": \\\"category.raw\\\",\\n      \\\"max_docs_per_value\\\": 500\\n    }\\n  },\\n  \\\"vertices\\\": [\\n    {\\n      \\\"field\\\": \\\"product\\\",\\n      \\\"size\\\": 5,                      \\n      \\\"min_doc_count\\\": 10,            \\n      \\\"shard_min_doc_count\\\": 3        \\n    }\\n  ],\\n  \\\"connections\\\": {\\n    \\\"query\\\": {                        \\n      \\\"bool\\\": {\\n        \\\"filter\\\": [\\n          {\\n            \\\"range\\\": {\\n              \\\"query_time\\\": {\\n                \\\"gte\\\": \\\"2015-10-01 00:00:00\\\"\\n              }\\n            }\\n          }\\n        ]\\n      }\\n    },\\n    \\\"vertices\\\": [\\n      {\\n        \\\"field\\\": \\\"query.raw\\\",\\n        \\\"size\\\": 5,\\n        \\\"min_doc_count\\\": 10,\\n        \\\"shard_min_doc_count\\\": 3\\n      }\\n    ]\\n  }\\n}\\n\", \"request.id\":\"d06EuroPRHif7l1vd8oZxQ\"}";
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("message", elasticString);
		e.setField("totalOffset", "+330");
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertEquals(1, results.size());
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, matchListener.getMatchCount());
	}
	@Test
	public void filterTestNullMessage() {
		final String elasticString=null;
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("message", elasticString);
		e.setField("totalOffset", "+330");
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertEquals(1, results.size());
		assertEquals(0, matchListener.getMatchCount());
	}

	class TestMatchListener implements FilterMatchListener {
		private AtomicInteger matchCount = new AtomicInteger(0);

		public int getMatchCount() {
			return matchCount.get();
		}

		@Override
		public void filterMatched(co.elastic.logstash.api.Event arg0) {
			matchCount.incrementAndGet();

		}
	}
}
