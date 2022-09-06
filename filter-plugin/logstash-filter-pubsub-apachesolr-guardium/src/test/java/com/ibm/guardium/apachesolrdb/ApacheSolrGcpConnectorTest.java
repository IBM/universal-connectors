/*
© Copyright IBM Corp. 2021, 2022 All rights reserved.
SPDX-License-Identifier: Apache-2.0
*/
package com.ibm.guardium.apachesolrdb;

import static com.ibm.guardium.apachesolrdb.ApacheSolrGcpConnector.LOGSTASH_TAG_SKIP_NOT_SOLR;
import static com.ibm.guardium.apachesolrdb.ApacheSolrGcpConnector.LOGSTASH_TAG_SOLR_PARSE_ERROR;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.logstash.plugins.ConfigurationImpl;
import org.logstash.plugins.ContextImpl;

import com.ibm.guardium.univer***REMOVED***lconnector.commons.GuardConstants;

import co.elastic.logstash.api.Configuration;
import co.elastic.logstash.api.Context;
import co.elastic.logstash.api.Event;
import co.elastic.logstash.api.FilterMatchListener;
import co.elastic.logstash.api.PluginConfigSpec;

/**
 * this class contains all the testing method and covers the ParserService
 * class.
 * 
 *
 */
public class ApacheSolrGcpConnectorTest {

	@Test
	public void configSchemaTest() {
		final String qtp = "";
		ApacheSolrGcpConnector filter = getApacheSolrConnector(qtp);
		Collection<PluginConfigSpec<?>> events = filter.configSchema();
		assertNotNull(events);
	}

	@Test
	public void commonUtilsTest() {
		CommonUtils commonUtils = new CommonUtils();
		assertNotNull(commonUtils);
	}

	@Test
	public void getIdTest() {
		final String qtp = "";
		ApacheSolrGcpConnector filter = getApacheSolrConnector(qtp);
		String id = filter.getId();
		assertNotNull(id);
	}

	@Test
	public void parseQtpRecordTest() {
		final String qtp = "{\"insertId\":\"bx***REMOVED***hwfao05y1\",\"jsonPayload\":{\"mes***REMOVED***ge\":\"2022-05-30 09:29:24.851 INFO  (qtp464887938-227) [c:tech2 s:shard2 r:core_node7 x:tech2_shard2_replica_n4] o.a.s.u.p.LogUpdateProcessorFactory [tech2_shard2_replica_n4]  webapp=\\/solr path=\\/update\\/json\\/docs params={}{add=[0060248025 (1734242955259019264), 0679805273]} 0 13\"},\"resource\":{\"type\":\"gce_instance\",\"labels\":{\"project_id\":\"project-sccd\",\"zone\":\"asia-south2-a\",\"instance_id\":\"4329029748489862071\"}},\"timestamp\":\"2022-05-19T11:35:34.049526902Z\",\"labels\":{\"compute.googleapis.com\\/resource_name\":\"solr-qa\"},\"logName\":\"projects\\/project-sccd\\/logs\\/files\",\"receiveTimestamp\":\"2022-05-19T11:35:34.523632435Z\"}";
		ApacheSolrGcpConnector filter = getApacheSolrConnector(qtp);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("mes***REMOVED***ge", qtp);
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertNotNull(results);
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, matchListener.getMatchCount());
	}

	@Test
	public void parseQtpSWAPCoretest() {
		final String qtp = "{\"insertId\":\"1lzqgspg20hyho1\",\"jsonPayload\":{\"mes***REMOVED***ge\":\"2022-06-03 11:27:38.004 INFO  (qtp2005169944-19) [   ] o.a.s.s.HttpSolrCall [admin] webapp=null path=\\/admin\\/cores params={core=testnew&other=TestCore&action=SWAP} status=0 QTime=207\"},\"resource\":{\"type\":\"gce_instance\",\"labels\":{\"zone\":\"asia-south2-a\",\"instance_id\":\"6290282768426383618\",\"project_id\":\"project-sccd\"}},\"timestamp\":\"2022-05-30T08:50:36.941752806Z\",\"labels\":{\"compute.googleapis.com\\/resource_name\":\"apachesolrqa\"},\"logName\":\"projects\\/project-sccd\\/logs\\/files\",\"receiveTimestamp\":\"2022-05-30T08:50:37.641620487Z\"}";
		ApacheSolrGcpConnector filter = getApacheSolrConnector(qtp);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("mes***REMOVED***ge", qtp);
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertNotNull(results);
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, matchListener.getMatchCount());
	}

	@Test
	public void parseQtpRecordTestPost() {
		final String qtp = "{\"insertId\":\"epzzqmg1c6000v\",\"jsonPayload\":{\"mes***REMOVED***ge\":\"2022-05-13 07:58:02.622 INFO  (qtp527804008-21) [c:techproducts s:shard1 r:core_node5 x:techproducts_shard1_replica_n3] o.a.s.u.p.LogUpdateProcessorFactory [techproducts_shard1_replica_n3]  webapp=\\/solr path=\\/update params={update.distrib=FROMLEADER&distrib.from=http://10.190.0.10:7574/solr/techproducts_shard1_replica_n1/&wt=javabin&version=2}{add=[EN7800GTX/2DHTV/256M (1732697058276540416)]} 0 17\"},\"resource\":{\"type\":\"gce_instance\",\"labels\":{\"project_id\":\"project-sccd\",\"zone\":\"asia-south2-a\",\"instance_id\":\"2639899388249855782\"}},\"timestamp\":\"2022-02-10T12:42:05.050746475Z\",\"logName\":\"projects\\/project-sccd\\/logs\\/files\",\"receiveTimestamp\":\"2022-02-10T12:42:05.438339552Z\"}";
		ApacheSolrGcpConnector filter = getApacheSolrConnector(qtp);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("mes***REMOVED***ge", qtp);
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertNotNull(results);
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, matchListener.getMatchCount());
	}

	@Test
	public void parseQtpRecordSkipTest() {
		final String qtp = "{ \"insertId\": \"g3ulzsfq8d4ho\", \"jsonPayload\": { \"mes***REMOVED***ge\": \"2022-05-12 11:23:55.167 INFO (qtp464887938-109) [c:test123 s:shard1 r:core_node5 x:test123_shard1_replica_n2] o.a.s.c.S.Request [test123_shard1_replica_n2] webapp=/solr path=/select params={df=_text_&distrib=false&_stateVer_=test123:18&_facet_={}&fl=id&fl=score&shards.purpose=1064964&start=0&fsv=true&shard.url=http://192.168.43.197:8983/solr/test123_shard1_replica_n2/|http://192.168.43.197:8983/solr/test123_shard1_replica_n1/&rows=0&rid=-7&version=2&q=*:*&json.facet={}&omitHeader=false&NOW=1652354635039&isShard=true&wt=javabin} hits=2 status=0 QTime=59\" }, \"resource\": { \"type\": \"gce_instance\", \"labels\": { \"instance_id\": \"2639899388249855782\", \"project_id\": \"project-sccd\", \"zone\": \"asia-south2-a\" } }";
		ApacheSolrGcpConnector filter = getApacheSolrConnector(qtp);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("mes***REMOVED***ge", qtp);
		List<Event> ev = new ArrayList<Event>();
		ev.add(e);
		Collection<Event> results = filter.filter(ev, matchListener);
		assertEquals(0, results.size());
		Set<String> tags = new HashSet<>((ArrayList) e.getField("tags"));
		assertEquals(1, tags.size());
		assertTrue(tags.contains(LOGSTASH_TAG_SKIP_NOT_SOLR));
	}

	@Test
	public void parseQtpRecordTestPost2() {
		final String qtp = "{\"insertId\":\"epzzqmg1c6000v\",\"jsonPayload\":{\"mes***REMOVED***ge\":\"2022-05-11 11:33:25.264 INFO  (qtp527804008-20) [c:firstdemo s:shard2 r:core_node8 x:firstdemo_shard2_replica_n6] o.a.s.u.p.LogUpdateProcessorFactory [firstdemo_shard2_replica_n6]  webapp=\\/solr path=\\/update params={commitWithin=1000&overwrite=true&wt=json&_=1652266680431}{add=[666]} 0 651\"},\"resource\":{\"type\":\"gce_instance\",\"labels\":{\"project_id\":\"project-sccd\",\"zone\":\"asia-south2-a\",\"instance_id\":\"2639899388249855782\"}},\"timestamp\":\"2022-02-10T12:42:05.050746475Z\",\"logName\":\"projects\\/project-sccd\\/logs\\/files\",\"receiveTimestamp\":\"2022-02-10T12:42:05.438339552Z\"}";
		ApacheSolrGcpConnector filter = getApacheSolrConnector(qtp);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("mes***REMOVED***ge", qtp);
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertNotNull(results);
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, matchListener.getMatchCount());
	}

	@Test
	public void parseQtpRenameCollection() {
		final String qtp = "{\"insertId\":\"g3ulzsfq8d4ho\",\"jsonPayload\":{\"mes***REMOVED***ge\":\"2022-05-13 08:46:23.290 INFO  (qtp527804008-21) [   ] o.a.s.s.HttpSolrCall [admin] webapp=null path=\\/admin\\/collections params={name=films&action=RENAME&target=rfilms} status=0 QTime=16\"},\"resource\":{\"type\":\"gce_instance\",\"labels\":{\"instance_id\":\"2639899388249855782\",\"project_id\":\"project-sccd\",\"zone\":\"asia-south2-a\"}},\"timestamp\":\"2022-03-29T04:23:32.970132401Z\",\"logName\":\"projects\\/project-sccd\\/logs\\/files\",\"receiveTimestamp\":\"2022-03-29T04:23:33.320652100Z\"}";
		ApacheSolrGcpConnector filter = getApacheSolrConnector(qtp);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("mes***REMOVED***ge", qtp);
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertNotNull(results);
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, matchListener.getMatchCount());
	}

	@Test
	public void parseQtpDeleteDocumentById() {
		final String qtp = "{\"insertId\":\"1dyfl8lg1468qv1\",\"jsonPayload\":{\"mes***REMOVED***ge\":\"2022-04-21 06:49:27.772 INFO  (qtp1661210650-54) [   x:first] o.a.s.u.p.LogUpdateProcessorFactory [first]  webapp=\\/solr path=\\/update params={commitWithin=1000&overwrite=true&wt=json&_=1650516033791}{delete=[Solr1001 (-1730699610313195520)]} 0 2\"},\"resource\":{\"type\":\"gce_instance\",\"labels\":{\"instance_id\":\"2639899388249855782\",\"project_id\":\"project-sccd\",\"zone\":\"asia-south2-a\"}},\"timestamp\":\"2022-04-21T06:49:27.772796954Z\",\"logName\":\"projects\\/project-sccd\\/logs\\/files\",\"receiveTimestamp\":\"2022-04-21T06:49:28.346427694Z\"}";
		ApacheSolrGcpConnector filter = getApacheSolrConnector(qtp);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("mes***REMOVED***ge", qtp);
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertNotNull(results);
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, matchListener.getMatchCount());
	}

	@Test
	public void InvalidRecord() {
		final String qtp = "{\"insertId\":\"epzzqmg1c6000v\",\"jsonPayload\":{\"mes***REMOVED***ge\":\"2022-02-10 12:42:05.050 INFO  (qtp672313607-16) [   x:first_core] o.a.s.u.p.LogUpdateProcessorFactory [first_core]  webapp=\\/solr path=\\/update params={commitWithin=1000&overwrite=true&wt=json&_=1644496890256} 0 6\"},\"resource\":{\"type\":\"gce_instance\",\"labels\":{\"project_id\":\"project-sccd\",\"zone\":\"asia-south2-a\",\"instance_id\":\"2639899388249855782\"}},\"timestamp\":\"2022-02-10T12:42:05.050746475Z\",\"logName\":\"projects\\/project-sccd\\/logs\\/files\",\"receiveTimestamp\":\"2022-02-10T12:42:05.438339552Z\"}";
		ApacheSolrGcpConnector filter = getApacheSolrConnector(qtp);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("mes***REMOVED***ge", qtp);
		List<Event> ev = new ArrayList<Event>();
		ev.add(e);
		Collection<Event> results = filter.filter(ev, matchListener);
		assertEquals(0, results.size());
		Set<String> tags = new HashSet<>((ArrayList) e.getField("tags"));
		assertEquals(1, tags.size());
		assertTrue(tags.contains(LOGSTASH_TAG_SKIP_NOT_SOLR));
	}

	@Test
	public void parseSqlError() {
		final String qtp = "{\"insertId\":\"bx***REMOVED***hwfao05y3\",\"jsonPayload\":{\"mes***REMOVED***ge\":\"2022-06-10 13:26:51.659 ERROR (qtp464887938-261) [c:new_collection s:shard1 r:core_node5 x:new_collection_shard1_replica_n2] o.a.s.c.s.i.s.ExceptionStream java.io.IOException: Failed to execute sqlQuery 'Select name,title from new_collection limit 1000' against JDBC connection 'jdbc:calcitesolr:'.\"},\"resource\":{\"type\":\"gce_instance\",\"labels\":{\"project_id\":\"project-sccd\",\"instance_id\":\"4329029748489862071\",\"zone\":\"asia-south2-a\"}},\"timestamp\":\"2022-05-19T11:35:34.145484201Z\",\"labels\":{\"compute.googleapis.com\\/resource_name\":\"solr-qa\"},\"logName\":\"projects\\/project-sccd\\/logs\\/files\",\"receiveTimestamp\":\"2022-05-19T11:35:34.523632435Z\"}";
		ApacheSolrGcpConnector filter = getApacheSolrConnector(qtp);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("mes***REMOVED***ge", qtp);
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertNotNull(results);
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, matchListener.getMatchCount());
	}

	@Test
	public void parseLexicalError() {
		final String qtp = "{\"insertId\":\"1bg3kr9fltr2na\",\"jsonPayload\":{\"mes***REMOVED***ge\":\"2022-06-22 05:12:11.971 ERROR (qtp527804008-19) [   x:first] o.a.s.h.RequestHandlerBase org.apache.solr.common.SolrException: org.apache.solr.search.SyntaxError: Cannot parse '!@#$%^&*()_+': Lexical error at line 1, column 7.  Encountered: \\\"&\\\" (38), after : \\\"\\\"\"},\"resource\":{\"type\":\"gce_instance\",\"labels\":{\"instance_id\":\"6290282768426383618\",\"project_id\":\"project-sccd\",\"zone\":\"asia-south2-a\"}},\"timestamp\":\"2022-06-22T05:12:11.975271603Z\",\"labels\":{\"compute.googleapis.com\\/resource_name\":\"apachesolrqa\"},\"logName\":\"projects\\/project-sccd\\/logs\\/files\",\"receiveTimestamp\":\"2022-06-22T05:12:12.482594362Z\"}";
		ApacheSolrGcpConnector filter = getApacheSolrConnector(qtp);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("mes***REMOVED***ge", qtp);
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertNotNull(results);
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, matchListener.getMatchCount());
	}

	@Test
	public void parseQtpSpellCheck() {
		final String qtp = "{\"insertId\":\"8c9wgpfmbprxg\",\"jsonPayload\":{\"mes***REMOVED***ge\":\"2022-06-13 05:58:04.153 INFO  (qtp527804008-14) [c:firstcoll s:shard1 r:core_node2 x:firstcoll_shard1_replica_n1] o.a.s.c.S.Request [firstcoll_shard1_replica_n1]  webapp=\\/solr path=\\/spell params={df=text&spellcheck.q=delll+ultra+sharp&spellcheck.collateParam.q.op=AND&spellcheck=true&wt=xml} hits=0 status=0 QTime=12\"},\"resource\":{\"type\":\"gce_instance\",\"labels\":{\"zone\":\"asia-south2-a\",\"instance_id\":\"6290282768426383618\",\"project_id\":\"project-sccd\"}},\"timestamp\":\"2022-06-13T05:58:04.155117226Z\",\"labels\":{\"compute.googleapis.com\\/resource_name\":\"apachesolrqa\"},\"logName\":\"projects\\/project-sccd\\/logs\\/files\",\"receiveTimestamp\":\"2022-06-13T05:58:04.618124555Z\"}";
		ApacheSolrGcpConnector filter = getApacheSolrConnector(qtp);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("mes***REMOVED***ge", qtp);
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertNotNull(results);
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, matchListener.getMatchCount());
	}

	@Test
	public void parseQtpQueryHandler() {
		final String qtp = "{\"insertId\":\"m7a8q2febiyvo\",\"jsonPayload\":{\"mes***REMOVED***ge\":\"2022-07-26 08:07:25.824 INFO  (qtp527804008-20) [   x:firstcore] o.a.s.c.S.Request [firstcore]  webapp=\\/solr path=\\/query params={q=*:*} hits=1000 status=0 QTime=1\"},\"resource\":{\"type\":\"gce_instance\",\"labels\":{\"instance_id\":\"7322797003770403197\",\"zone\":\"us-central1-a\",\"project_id\":\"charged-mind-281913\"}},\"timestamp\":\"2022-07-26T08:07:25.829710264Z\",\"labels\":{\"compute.googleapis.com\\/resource_name\":\"apache-solr-vm-1\"},\"logName\":\"projects\\/charged-mind-281913\\/logs\\/files\",\"receiveTimestamp\":\"2022-07-26T08:07:26.415762700Z\"}";
		ApacheSolrGcpConnector filter = getApacheSolrConnector(qtp);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("mes***REMOVED***ge", qtp);
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertNotNull(results);
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, matchListener.getMatchCount());
	}

	@Test
	public void parseQtpTermsHandler() {
		final String qtp = "{\"insertId\":\"1jrrgrff5z77jj\",\"jsonPayload\":{\"mes***REMOVED***ge\":\"2022-07-27 06:36:56.324 INFO  (qtp464887938-86) [c:two s:shard1 r:core_node2 x:two_shard1_replica_n1] o.a.s.c.S.Request [two_shard1_replica_n1]  webapp=\\/solr path=\\/terms params={terms.fl=name&wt=xml} status=0 QTime=46\"},\"resource\":{\"type\":\"gce_instance\",\"labels\":{\"project_id\":\"project-sccd\",\"instance_id\":\"6290282768426383618\",\"zone\":\"asia-south2-a\"}},\"timestamp\":\"2022-07-06T05:44:08.559008733Z\",\"labels\":{\"compute.googleapis.com\\/resource_name\":\"apachesolrqa\"},\"logName\":\"projects\\/project-sccd\\/logs\\/files\",\"receiveTimestamp\":\"2022-07-06T05:44:09.532328235Z\"}";
		ApacheSolrGcpConnector filter = getApacheSolrConnector(qtp);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("mes***REMOVED***ge", qtp);
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertNotNull(results);
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, matchListener.getMatchCount());
	}

	@Test
	public void parseQtpGetHandler() {
		final String qtp = "{\"insertId\":\"1jrrgrff5z77jj\",\"jsonPayload\":{\"mes***REMOVED***ge\":\"2022-07-27 06:16:36.171 INFO  (qtp464887938-86) [c:two s:shard1 r:core_node2 x:two_shard1_replica_n1] o.a.s.c.S.Request [two_shard1_replica_n1]  webapp=\\/solr path=\\/get params={q=*:*} status=0 QTime=29\"},\"resource\":{\"type\":\"gce_instance\",\"labels\":{\"project_id\":\"project-sccd\",\"instance_id\":\"6290282768426383618\",\"zone\":\"asia-south2-a\"}},\"timestamp\":\"2022-07-06T05:44:08.559008733Z\",\"labels\":{\"compute.googleapis.com\\/resource_name\":\"apachesolrqa\"},\"logName\":\"projects\\/project-sccd\\/logs\\/files\",\"receiveTimestamp\":\"2022-07-06T05:44:09.532328235Z\"}";
		ApacheSolrGcpConnector filter = getApacheSolrConnector(qtp);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("mes***REMOVED***ge", qtp);
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertNotNull(results);
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, matchListener.getMatchCount());
	}

	@Test
	public void parseQtpSkipRecord() {
		final String qtp = "{\"insertId\":\"hrfdz8fqfvn9m\",\"jsonPayload\":{\"mes***REMOVED***ge\":\"2022-03-29 04:21:40.759 INFO  (qtp1661210650-40) [   ] o.a.s.s.HttpSolrCall [admin] webapp=null path=\\/admin\\/cores params={indexInfo=false&wt=json&_=1648464924176} status=0 QTime=11\"},\"resource\":{\"type\":\"gce_instance\",\"labels\":{\"zone\":\"asia-south2-a\",\"instance_id\":\"2639899388249855782\",\"project_id\":\"project-sccd\"}},\"timestamp\":\"2022-03-29T04:21:40.761324287Z\",\"logName\":\"projects\\/project-sccd\\/logs\\/files\",\"receiveTimestamp\":\"2022-03-29T04:21:41.324482638Z\"}";
		ApacheSolrGcpConnector filter = getApacheSolrConnector(qtp);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("mes***REMOVED***ge", qtp);
		List<Event> ev = new ArrayList<Event>();
		ev.add(e);
		Collection<Event> results = filter.filter(ev, matchListener);
		assertEquals(0, results.size());
		Set<String> tags = new HashSet<>((ArrayList) e.getField("tags"));
		assertEquals(1, tags.size());
		assertTrue(tags.contains(LOGSTASH_TAG_SKIP_NOT_SOLR));
	}

	@Test
	public void parseQtpInvalidResourceTest() {
		final String qtp = "{\"insertId\":\"epzzqmg1c6000v\",\"jsonPayload\":{\"mes***REMOVED***ge\":\"2022-02-10 12:42:05.050 INFO  (qtp672313607-16) [   x:first_core] o.a.s.u.p.LogUpdateProcessorFactory [first_core]  webapp=\\/solr path=\\/update params={commitWithin=1000&overwrite=true&wt=json&_=1644496890256}{add=[7698 (1724380007675985920)]} 0 6\"},\"timestamp\":\"2022-02-10T12:42:05.050746475Z\",\"logName\":\"projects\\/project-sccd\\/logs\\/files\",\"receiveTimestamp\":\"2022-02-10T12:42:05.438339552Z\"}";
		ApacheSolrGcpConnector filter = getApacheSolrConnector(qtp);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("mes***REMOVED***ge", qtp);
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertNotNull(results);
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, matchListener.getMatchCount());
	}

	@Test
	public void parseQtpInvalidParamsTest() {
		final String qtp = "{\"insertId\":\"epzzqmg1c6000v\",\"jsonPayload\":{\"mes***REMOVED***ge\":\"2022-02-10 12:42:05.050 INFO  (qtp672313607-16) [   x:first_core] o.a.s.u.p.LogUpdateProcessorFactory [first_core]  webapp=\\/solr path=\\/update  0 6\"},\"resource\":{\"type\":\"gce_instance\",\"labels\":{\"project_id\":\"project-sccd\",\"zone\":\"asia-south2-a\",\"instance_id\":\"2639899388249855782\"}},\"timestamp\":\"2022-02-10T12:42:05.050746475Z\",\"logName\":\"projects\\/project-sccd\\/logs\\/files\",\"receiveTimestamp\":\"2022-02-10T12:42:05.438339552Z\"}";
		ApacheSolrGcpConnector filter = getApacheSolrConnector(qtp);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("mes***REMOVED***ge", qtp);
		List<Event> ev = new ArrayList<Event>();
		ev.add(e);
		Collection<Event> results = filter.filter(ev, matchListener);
		assertEquals(0, results.size());
		Set<String> tags = new HashSet<>((ArrayList) e.getField("tags"));
		assertEquals(1, tags.size());
		assertTrue(tags.contains(LOGSTASH_TAG_SKIP_NOT_SOLR));
	}

	@Test
	public void parseServiceNameNegTest() {
		final String qtp = "{\"insertId\":\"epzzqmg1c6dwlg\",\"jsonPayload\":{\"mes***REMOVED***ge\":\"2022-02-10 12:44:20.243 INFO  (qtp672313607-18) [   x:first_core] o.a.s.c.S.Request [first_core]  webapp=\\/solr path=\\/select params={q=id:7698&_=1644497065596} hits=1 status=0 QTime=1\"},\"resource\":{\"type\":\"gce_instance\",\"labels\":{\"project_id\":\"project-sccd\",\"zone\":\"asia-south2-a\"}},\"timestamp\":\"2022-02-10T12:44:20.244030006Z\",\"logName\":\"projects\\/project-sccd\\/logs\\/files\",\"receiveTimestamp\":\"2022-02-10T12:44:20.439608402Z\"}";
		ApacheSolrGcpConnector filter = getApacheSolrConnector(qtp);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("mes***REMOVED***ge", qtp);
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertNotNull(results);
	}

	@Test
	public void parseServiceNameNegTest1() {
		final String qtp = "{\"insertId\":\"epzzqmg1c6dwlg\",\"jsonPayload\":{\"mes***REMOVED***ge\":\"2022-02-10 12:44:20.243 INFO  (qtp672313607-18) [   x:first_core] o.a.s.c.S.Request [first_core]  webapp=\\/solr path=\\/select params={q=id:7698&_=1644497065596} hits=1 status=0 QTime=1\"},\"resource\":{\"type\":\"gce_instance\",\"labels\":{\"project_id\":\"project-sccd\",\"instance_id\":\"2639899388249855782\"}},\"timestamp\":\"2022-02-10T12:44:20.244030006Z\",\"logName\":\"projects\\/project-sccd\\/logs\\/files\",\"receiveTimestamp\":\"2022-02-10T12:44:20.439608402Z\"}";
		ApacheSolrGcpConnector filter = getApacheSolrConnector(qtp);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("mes***REMOVED***ge", qtp);
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertNotNull(results);
	}

	@Test
	public void parseServiceNameNegTest2() {
		final String qtp = "{\"insertId\":\"epzzqmg1c6dwlg\",\"jsonPayload\":{\"mes***REMOVED***ge\":\"2022-02-10 12:44:20.243 INFO  (qtp672313607-18) [   x:first_core] o.a.s.c.S.Request [first_core]  webapp=\\/solr path=\\/select params={q=id:7698&_=1644497065596} hits=1 status=0 QTime=1\"},\"resource\":{\"labels\":{\"project_id\":\"project-sccd\",\"instance_id\":\"2639899388249855782\",\"zone\":\"asia-south2-a\"}},\"timestamp\":\"2022-02-10T12:44:20.244030006Z\",\"logName\":\"projects\\/project-sccd\\/logs\\/files\",\"receiveTimestamp\":\"2022-02-10T12:44:20.439608402Z\"}";
		ApacheSolrGcpConnector filter = getApacheSolrConnector(qtp);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("mes***REMOVED***ge", qtp);
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertNotNull(results);
	}

	@Test
	public void parseEmptyJSONTest() {
		final String qtp = "";
		ApacheSolrGcpConnector filter = getApacheSolrConnector(qtp);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("mes***REMOVED***ge", qtp);
		List<Event> ev = new ArrayList<Event>();
		ev.add(e);
		Collection<Event> results = filter.filter(ev, matchListener);
		assertEquals(0, results.size());
		Set<String> tags = new HashSet<>((ArrayList) e.getField("tags"));
		assertEquals(1, tags.size());
		assertTrue(tags.contains(LOGSTASH_TAG_SKIP_NOT_SOLR));
	}

	@Test
	public void InvalidSolrCallTest() {
		final String qtp = "{\"insertId\":\"hrfdz8fqfvn9m\",\"jsonPayload\":{\"mes***REMOVED***ge\":\"2022-03-29 04:21:40.759 INFO  (qtp1661210650-40) [   ] o.a.s.s.HttpSolrCall [admin] webapp=null path=\\/admin\\/cores params={indexInfo=false&wt=json&_=1648464924176} status=0 QTime=11\"},\"resource\":{\"type\":\"gce_instance\",\"labels\":{\"zone\":\"asia-south2-a\",\"instance_id\":\"2639899388249855782\",\"project_id\":\"project-sccd\"}},\"timestamp\":\"2022-03-29T04:21:40.761324287Z\",\"logName\":\"projects\\/project-sccd\\/logs\\/files\",\"receiveTimestamp\":\"2022-03-29T04:21:41.324482638Z\"}";
		ApacheSolrGcpConnector filter = getApacheSolrConnector(qtp);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("mes***REMOVED***ge", qtp);
		List<Event> ev = new ArrayList<Event>();
		ev.add(e);
		Collection<Event> results = filter.filter(ev, matchListener);
		assertEquals(0, results.size());
		Set<String> tags = new HashSet<>((ArrayList) e.getField("tags"));
		assertEquals(1, tags.size());
		assertTrue(tags.contains(LOGSTASH_TAG_SKIP_NOT_SOLR));
	}

	@Test
	public void invalidJsonTest() {
		final String qtp = "\"insertId\":\"epzzqmg1c6dwlg\",\"jsonPayload\":{\"mes***REMOVED***ge\":\"2022-02-10 12:44:20.243 INFO  (qtp672313607-18) [   x:first_core] o.a.s.c.S.Request [first_core]  webapp=\\/solr path=\\/select params={q=id:7698&_=1644497065596} hits=1 status=0 QTime=1\"},\"resource\":{\"type\":\"gce_instance\",\"labels\":{\"project_id\":\"project-sccd\",\"instance_id\":\"2639899388249855782\",\"zone\":\"asia-south2-a\"}},\"timestamp\":\"2022-02-10T12:44:20.244030006Z\",\"logName\":\"projects\\/project-sccd\\/logs\\/files\",\"receiveTimestamp\":\"2022-02-10T12:44:20.439608402Z\"";
		ApacheSolrGcpConnector filter = getApacheSolrConnector(qtp);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("mes***REMOVED***ge", qtp);
		List<Event> ev = new ArrayList<Event>();
		ev.add(e);
		Collection<Event> results = filter.filter(ev, matchListener);
		assertEquals(0, results.size());
		Set<String> tags = new HashSet<>((ArrayList) e.getField("tags"));
		assertEquals(1, tags.size());
		assertTrue(tags.contains(LOGSTASH_TAG_SKIP_NOT_SOLR));
	}

	@Test
	public void timeFormatExceptionTest() {
		final String qtp = "{\"insertId\":\"epzzqmg1c6dwlg\",\"jsonPayload\":{\"mes***REMOVED***ge\":\"2022-02-10  INFO  (qtp672313607-18) [   x:first_core] o.a.s.c.S.Request [first_core]  webapp=\\/solr path=\\/select params={q=id:7698&_=1644497065596} hits=1 status=0 QTime=1\"},\"resource\":{\"type\":\"gce_instance\",\"labels\":{\"project_id\":\"project-sccd\",\"instance_id\":\"2639899388249855782\",\"zone\":\"asia-south2-a\"}},\"timestamp\":\"2022-02-10T12:44:20.244030006Z\",\"logName\":\"projects\\/project-sccd\\/logs\\/files\",\"receiveTimestamp\":\"2022-02-10T12:44:20.439608402Z\"}";
		ApacheSolrGcpConnector filter = getApacheSolrConnector(qtp);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("mes***REMOVED***ge", qtp);
		List<Event> ev = new ArrayList<Event>();
		ev.add(e);
		Collection<Event> results = filter.filter(ev, matchListener);
		assertEquals(1, results.size());
		Set<String> tags = new HashSet<>((ArrayList) e.getField("tags"));
		assertEquals(1, tags.size());
		assertTrue(tags.contains(LOGSTASH_TAG_SOLR_PARSE_ERROR));
	}

	@Test
	public void parseQtpDeleteTest() {
		final String qtp = "{\"insertId\":\"epzzqmg1c6000v\",\"jsonPayload\":{\"mes***REMOVED***ge\":\"2022-02-10 12:42:05.050 INFO  (qtp672313607-16) [   x:first_core] o.a.s.u.p.LogUpdateProcessorFactory [first_core]  webapp=\\/solr path=\\/update params={commitWithin=1000&overwrite=true&wt=json&_=1648123036274}{delete=[1234 (-1728273303680843776)]} 0 49\"},\"resource\":{\"type\":\"gce_instance\",\"labels\":{\"project_id\":\"project-sccd\",\"zone\":\"asia-south2-a\",\"instance_id\":\"2639899388249855782\"}},\"timestamp\":\"2022-02-10T12:42:05.050746475Z\",\"logName\":\"projects\\/project-sccd\\/logs\\/files\",\"receiveTimestamp\":\"2022-02-10T12:42:05.438339552Z\"}";
		ApacheSolrGcpConnector filter = getApacheSolrConnector(qtp);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("mes***REMOVED***ge", qtp);
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertNotNull(results);
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, matchListener.getMatchCount());
	}

	// CoreCreation
	@Test
	public void parseQtpCreateCoreTest() {
		final String qtp = "{\"insertId\":\"hrfdz8fqfvn9m\",\"jsonPayload\":{\"mes***REMOVED***ge\":\"2022-03-29 04:21:40.759 INFO  (qtp1661210650-40) [   ] o.a.s.s.HttpSolrCall [admin] webapp=null path=\\/admin\\/cores params={name=Solr_***REMOVED***mple_core&action=CREATE&instanceDir=Solr_***REMOVED***mple_core&wt=json} status=0 QTime=11\"},\"resource\":{\"type\":\"gce_instance\",\"labels\":{\"zone\":\"asia-south2-a\",\"instance_id\":\"2639899388249855782\",\"project_id\":\"project-sccd\"}},\"timestamp\":\"2022-03-29T04:21:40.761324287Z\",\"logName\":\"projects\\/project-sccd\\/logs\\/files\",\"receiveTimestamp\":\"2022-03-29T04:21:41.324482638Z\"}";
		ApacheSolrGcpConnector filter = getApacheSolrConnector(qtp);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("mes***REMOVED***ge", qtp);
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertNotNull(results);
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, matchListener.getMatchCount());
	}

	@Test
	public void parseQtpCreateCoreTest2() {
		final String qtp = "{\"insertId\":\"hrfdz8fqfvn9m\",\"jsonPayload\":{\"mes***REMOVED***ge\":\"2022-03-29 04:21:40.759 INFO  (qtp1661210650-40) [   ] o.a.s.s.HttpSolrCall [admin] webapp=null path=\\/admin\\/cores params={name=Solr_***REMOVED***mple_core&action=CREATE&instanceDir=Solr_***REMOVED***mple_core&wt=&ab=t} status=0 QTime=11\"},\"resource\":{\"type\":\"gce_instance\",\"labels\":{\"zone\":\"asia-south2-a\",\"instance_id\":\"2639899388249855782\"}},\"timestamp\":\"2022-03-29T04:21:40.761324287Z\",\"logName\":\"projects\\/project-sccd\\/logs\\/files\",\"receiveTimestamp\":\"2022-03-29T04:21:41.324482638Z\"}";
		ApacheSolrGcpConnector filter = getApacheSolrConnector(qtp);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("mes***REMOVED***ge", qtp);
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertNotNull(results);
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, matchListener.getMatchCount());
	}

	@Test
	public void parseQtpCreateCollectionTest() {
		final String qtp = "{\"insertId\":\"bx***REMOVED***hwfao05y1\",\"jsonPayload\":{\"mes***REMOVED***ge\":\"2022-05-18 06:21:41.002 INFO  (qtp464887938-18) [   ] o.a.s.s.HttpSolrCall [admin] webapp=null path=\\/admin\\/collections params={replicationFactor=3&maxShardsPerNode=-1&collection.configName=Test1&name=Test1&action=CREATE&numShards=1&wt=json} status=0 QTime=2313\"},\"resource\":{\"type\":\"gce_instance\",\"labels\":{\"project_id\":\"project-sccd\",\"zone\":\"asia-south2-a\",\"instance_id\":\"4329029748489862071\"}},\"timestamp\":\"2022-05-19T11:35:34.049526902Z\",\"labels\":{\"compute.googleapis.com\\/resource_name\":\"solr-qa\"},\"logName\":\"projects\\/project-sccd\\/logs\\/files\",\"receiveTimestamp\":\"2022-05-19T11:35:34.523632435Z\"}";
		ApacheSolrGcpConnector filter = getApacheSolrConnector(qtp);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("mes***REMOVED***ge", qtp);
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertNotNull(results);
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, matchListener.getMatchCount());
	}

	// core updation
	@Test
	public void parseQtpUpdateCoreTest() {
		final String qtp = "{\"insertId\":\"hrfdz8fqfvn9m\",\"jsonPayload\":{\"mes***REMOVED***ge\":\"2022-03-29 04:21:40.759 INFO  (qtp1661210650-40) [   ] o.a.s.s.HttpSolrCall [admin] webapp=null path=\\/admin\\/cores params={core=Solr_***REMOVED***mple_core&other=Sample&action=RENAME} status=0 QTime=11\"},\"resource\":{\"type\":\"gce_instance\",\"labels\":{\"zone\":\"asia-south2-a\",\"instance_id\":\"2639899388249855782\",\"project_id\":\"project-sccd\"}},\"timestamp\":\"2022-03-29T04:21:40.761324287Z\",\"logName\":\"projects\\/project-sccd\\/logs\\/files\",\"receiveTimestamp\":\"2022-03-29T04:21:41.324482638Z\"}";
		ApacheSolrGcpConnector filter = getApacheSolrConnector(qtp);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("mes***REMOVED***ge", qtp);
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertNotNull(results);
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, matchListener.getMatchCount());
	}

	// core deletion
	@Test
	public void parseQtpDeleteCoreTest() {
		final String qtp = "{\"insertId\":\"w6z71mg14acmf5\",\"jsonPayload\":{\"mes***REMOVED***ge\":\"2022-04-21 07:24:25.627 INFO  (qtp1661210650-48) [   ] o.a.s.s.HttpSolrCall [admin] webapp=null path=\\/admin\\/cores params={deleteInstanceDir=true&core=TestingSample&deleteDataDir=true&action=UNLOAD&wt=json&deleteIndex=true} status=0 QTime=38\"},\"resource\":{\"type\":\"gce_instance\",\"labels\":{\"zone\":\"asia-south2-a\",\"instance_id\":\"2639899388249855782\",\"project_id\":\"project-sccd\"}},\"timestamp\":\"2022-04-21T07:24:25.627978460Z\",\"logName\":\"projects\\/project-sccd\\/logs\\/files\",\"receiveTimestamp\":\"2022-04-21T07:24:26.354786764Z\"}";
		ApacheSolrGcpConnector filter = getApacheSolrConnector(qtp);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("mes***REMOVED***ge", qtp);
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertNotNull(results);
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, matchListener.getMatchCount());
	}

	@Test
	public void parseQtpDeleteCollectionTest() {
		final String qtp = "{\"insertId\":\"g3ulzsfq8d4ho\",\"jsonPayload\":{\"mes***REMOVED***ge\":\"2022-05-18 05:18:29.421 INFO  (qtp464887938-17) [   ] o.a.s.s.HttpSolrCall [admin] webapp=null path=\\/admin\\/collections params={name=test&action=DELETE} status=0 QTime=1001\"},\"resource\":{\"type\":\"gce_instance\",\"labels\":{\"instance_id\":\"2639899388249855782\",\"project_id\":\"project-sccd\",\"zone\":\"asia-south2-a\"}},\"timestamp\":\"2022-03-29T04:23:32.970132401Z\",\"logName\":\"projects\\/project-sccd\\/logs\\/files\",\"receiveTimestamp\":\"2022-03-29T04:23:33.320652100Z\"}";
		ApacheSolrGcpConnector filter = getApacheSolrConnector(qtp);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("mes***REMOVED***ge", qtp);
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertNotNull(results);
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, matchListener.getMatchCount());
	}

	@Test
	public void parseQtpCreateCoreInvalidTest() {
		final String qtp = "{\"insertId\":\"hrfdz8fqfvn9m\",\"jsonPayload\":{\"mes***REMOVED***ge\":\"2022-03-29 04:21:40.759 INFO  (qtp1661210650-40) [   ] o.a.s.s.HttpSolrCall [admin] webapp=null path=\\/admin\\/cores params={indexInfo=false&wt=json&_=1648464924176} status=0 QTime=11\"},\"resource\":{\"type\":\"gce_instance\",\"labels\":{\"zone\":\"asia-south2-a\",\"instance_id\":\"2639899388249855782\",\"project_id\":\"project-sccd\"}},\"timestamp\":\"2022-03-29T04:21:40.761324287Z\",\"logName\":\"projects\\/project-sccd\\/logs\\/files\",\"receiveTimestamp\":\"2022-03-29T04:21:41.324482638Z\"}";
		ApacheSolrGcpConnector filter = getApacheSolrConnector(qtp);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("mes***REMOVED***ge", qtp);
		List<Event> ev = new ArrayList<Event>();
		ev.add(e);
		Collection<Event> results = filter.filter(ev, matchListener);
		assertEquals(0, results.size());
		Set<String> tags = new HashSet<>((ArrayList) e.getField("tags"));
		assertEquals(1, tags.size());
		assertTrue(tags.contains(LOGSTASH_TAG_SKIP_NOT_SOLR));
	}

	@Test
	public void parseQtpRequestRecordTest() {
		final String qtp = "{\"insertId\":\"epzzqmg1c6dwlg\",\"jsonPayload\":{\"mes***REMOVED***ge\":\"2022-02-10 12:44:20.243 INFO  (qtp672313607-18) [   x:first_core] o.a.s.c.S.Request [first_core]  webapp=\\/solr path=\\/select params={q=id:7698&_=1644497065596} hits=1 status=0 QTime=1\"},\"resource\":{\"type\":\"gce_instance\",\"labels\":{\"project_id\":\"project-sccd\",\"instance_id\":\"2639899388249855782\",\"zone\":\"asia-south2-a\"}},\"timestamp\":\"2022-02-10T12:44:20.244030006Z\",\"logName\":\"projects\\/project-sccd\\/logs\\/files\",\"receiveTimestamp\":\"2022-02-10T12:44:20.439608402Z\"}";
		ApacheSolrGcpConnector filter = getApacheSolrConnector(qtp);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("mes***REMOVED***ge", qtp);
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertNotNull(results);
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, matchListener.getMatchCount());
	}

	@Test
	public void CoreCreationTest() {
		final String qtp = "{\"insertId\":\"hrfdz8fqfvn9m\",\"jsonPayload\":{\"mes***REMOVED***ge\":\"2022-03-29 04:21:40.759 INFO  (qtp1661210650-40) [   ] o.a.s.s.HttpSolrCall [admin] webapp=null path=\\/admin\\/cores params={action=CREATE} status=0 QTime=11\"},\"resource\":{\"type\":\"gce_instance\",\"labels\":{\"zone\":\"asia-south2-a\",\"instance_id\":\"2639899388249855782\",\"project_id\":\"project-sccd\"}},\"timestamp\":\"2022-03-29T04:21:40.761324287Z\",\"logName\":\"projects\\/project-sccd\\/logs\\/files\",\"receiveTimestamp\":\"2022-03-29T04:21:41.324482638Z\"}";
		ApacheSolrGcpConnector filter = getApacheSolrConnector(qtp);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("mes***REMOVED***ge", qtp);
		List<Event> ev = new ArrayList<Event>();
		ev.add(e);
		Collection<Event> results = filter.filter(ev, matchListener);
		assertNotNull(results);
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, matchListener.getMatchCount());
	}

	@Test
	public void parseQtpErrorRecordTest() {
		final String qtp = "{\"insertId\":\"17u90yhf9nmdte\",\"jsonPayload\":{\"mes***REMOVED***ge\":\"2022-02-10 14:04:38.542 ERROR  (qtp672313607-17) [   x:first_core] o.a.s.h.RequestHandlerBase org.apache.solr.common.SolrException: Cannot parse provided JSON: Unexpected EOF: char=(EOF),position=20 AFTER=''\"},\"resource\":{\"type\":\"gce_instance\",\"labels\":{\"instance_id\":\"2639899388249855782\",\"zone\":\"asia-south2-a\",\"project_id\":\"project-sccd\"}},\"timestamp\":\"2022-02-10T14:04:38.543314031Z\",\"logName\":\"projects\\/project-sccd\\/logs\\/files\",\"receiveTimestamp\":\"2022-02-10T14:04:39.447195015Z\"}";
		ApacheSolrGcpConnector filter = getApacheSolrConnector(qtp);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("mes***REMOVED***ge", qtp);
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertNotNull(results);
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, matchListener.getMatchCount());
	}

	@Test
	public void parseQtpErrorRecordCollectionTest() {
		final String qtp = "{\"insertId\":\"17u90yhf9nmdte\",\"jsonPayload\":{\"mes***REMOVED***ge\":\"2022-05-19 12:03:09.995 ERROR (OverseerThreadFactory-126-thread-5-processing-n:192.168.43.197:8983_solr) [   ] o.a.s.c.a.c.OverseerCollectionMes***REMOVED***geHandler Collection: renamedTest3 operation: delete failed => org.apache.solr.common.SolrException: Collection : renamedTest3 is part of aliases: [Test3], remove or modify the aliases before removing this collection.\"},\"resource\":{\"type\":\"gce_instance\",\"labels\":{\"instance_id\":\"2639899388249855782\",\"zone\":\"asia-south2-a\",\"project_id\":\"project-sccd\"}},\"timestamp\":\"2022-02-10T14:04:38.543314031Z\",\"logName\":\"projects\\/project-sccd\\/logs\\/files\",\"receiveTimestamp\":\"2022-02-10T14:04:39.447195015Z\"}";
		ApacheSolrGcpConnector filter = getApacheSolrConnector(qtp);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("mes***REMOVED***ge", qtp);
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertNotNull(results);
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, matchListener.getMatchCount());
	}

	@Test
	public void parseQtpCollectionPropTest() {
		final String qtp = "{\"insertId\":\"bx***REMOVED***hwfao05y0\",\"jsonPayload\":{\"mes***REMOVED***ge\":\"2022-05-20 04:08:20.907 INFO  (qtp464887938-26) [   ] o.a.s.s.HttpSolrCall [admin] webapp=null path=\\/admin\\/collections params={propertyName=name&name=proptest&action=COLLECTIONPROP&propertyValue=testprop1} status=0 QTime=8\"},\"resource\":{\"type\":\"gce_instance\",\"labels\":{\"instance_id\":\"4329029748489862071\",\"zone\":\"asia-south2-a\",\"project_id\":\"project-sccd\"}},\"timestamp\":\"2022-05-19T11:35:34.036037173Z\",\"labels\":{\"compute.googleapis.com\\/resource_name\":\"solr-qa\"},\"logName\":\"projects\\/project-sccd\\/logs\\/files\",\"receiveTimestamp\":\"2022-05-19T11:35:34.523632435Z\"}";
		ApacheSolrGcpConnector filter = getApacheSolrConnector(qtp);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("mes***REMOVED***ge", qtp);
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertNotNull(results);
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, matchListener.getMatchCount());
	}

	@Test
	public void parseQtpCollectionStatusTest() {
		final String qtp = "{\"insertId\":\"bx***REMOVED***hwfao05y0\",\"jsonPayload\":{\"mes***REMOVED***ge\":\"2022-05-20 04:14:18.125 INFO  (qtp464887938-25) [c:proptest   ] o.a.s.s.HttpSolrCall [admin] webapp=null path=\\/admin\\/collections params={action=COLSTATUS&sizeInfo=true&collection=proptest&fieldInfo=true&coreInfo=true&segments=true} status=0 QTime=287\"},\"resource\":{\"type\":\"gce_instance\",\"labels\":{\"instance_id\":\"4329029748489862071\",\"zone\":\"asia-south2-a\",\"project_id\":\"project-sccd\"}},\"timestamp\":\"2022-05-19T11:35:34.036037173Z\",\"labels\":{\"compute.googleapis.com\\/resource_name\":\"solr-qa\"},\"logName\":\"projects\\/project-sccd\\/logs\\/files\",\"receiveTimestamp\":\"2022-05-19T11:35:34.523632435Z\"}";
		ApacheSolrGcpConnector filter = getApacheSolrConnector(qtp);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("mes***REMOVED***ge", qtp);
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertNotNull(results);
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, matchListener.getMatchCount());
	}

	@Test
	public void parseQtpModifyCollectionTest() {
		final String qtp = "{\"insertId\":\"bx***REMOVED***hwfao05y0\",\"jsonPayload\":{\"mes***REMOVED***ge\":\"2022-05-20 04:34:09.392 INFO  (qtp464887938-22) [c:proptest   ] o.a.s.s.HttpSolrCall [admin] webapp=null path=\\/admin\\/collections params={replicationFactor=1&action=MODIFYCOLLECTION&numShards=2&collection=proptest} status=0 QTime=132\"},\"resource\":{\"type\":\"gce_instance\",\"labels\":{\"instance_id\":\"4329029748489862071\",\"zone\":\"asia-south2-a\",\"project_id\":\"project-sccd\"}},\"timestamp\":\"2022-05-19T11:35:34.036037173Z\",\"labels\":{\"compute.googleapis.com\\/resource_name\":\"solr-qa\"},\"logName\":\"projects\\/project-sccd\\/logs\\/files\",\"receiveTimestamp\":\"2022-05-19T11:35:34.523632435Z\"}";
		ApacheSolrGcpConnector filter = getApacheSolrConnector(qtp);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("mes***REMOVED***ge", qtp);
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertNotNull(results);
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, matchListener.getMatchCount());
	}

	@Test
	public void parseQtpReloadCollectionTest() {
		final String qtp = "{\"insertId\":\"bx***REMOVED***hwfao05y0\",\"jsonPayload\":{\"mes***REMOVED***ge\":\"2022-05-20 04:37:04.911 INFO  (qtp464887938-263) [   ] o.a.s.s.HttpSolrCall [admin] webapp=null path=\\/admin\\/collections params={name=proptest&action=RELOAD} status=0 QTime=2516\"},\"resource\":{\"type\":\"gce_instance\",\"labels\":{\"instance_id\":\"4329029748489862071\",\"zone\":\"asia-south2-a\",\"project_id\":\"project-sccd\"}},\"timestamp\":\"2022-05-19T11:35:34.036037173Z\",\"labels\":{\"compute.googleapis.com\\/resource_name\":\"solr-qa\"},\"logName\":\"projects\\/project-sccd\\/logs\\/files\",\"receiveTimestamp\":\"2022-05-19T11:35:34.523632435Z\"}";
		ApacheSolrGcpConnector filter = getApacheSolrConnector(qtp);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("mes***REMOVED***ge", qtp);
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertNotNull(results);
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, matchListener.getMatchCount());
	}

	@Test
	public void parseQtpSkipReloadTest() {
		final String qtp = "{\"insertId\":\"bx***REMOVED***hwfao05y0\",\"jsonPayload\":{\"mes***REMOVED***ge\":\"2022-05-20 04:37:04.698 INFO  (qtp464887938-260) [   ] o.a.s.s.HttpSolrCall [admin] webapp=null path=\\/admin\\/cores params={core=proptest_shard1_replica_n2&qt=\\/admin\\/cores&action=RELOAD&wt=javabin&version=2} status=0 QTime=2264\"},\"resource\":{\"type\":\"gce_instance\",\"labels\":{\"instance_id\":\"4329029748489862071\",\"zone\":\"asia-south2-a\",\"project_id\":\"project-sccd\"}},\"timestamp\":\"2022-05-19T11:35:34.036037173Z\",\"labels\":{\"compute.googleapis.com\\/resource_name\":\"solr-qa\"},\"logName\":\"projects\\/project-sccd\\/logs\\/files\",\"receiveTimestamp\":\"2022-05-19T11:35:34.523632435Z\"}";
		ApacheSolrGcpConnector filter = getApacheSolrConnector(qtp);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("mes***REMOVED***ge", qtp);
		List<Event> ev = new ArrayList<Event>();
		ev.add(e);
		Collection<Event> results = filter.filter(ev, matchListener);
		assertEquals(0, results.size());
		Set<String> tags = new HashSet<>((ArrayList) e.getField("tags"));
		assertEquals(1, tags.size());
		assertTrue(tags.contains(LOGSTASH_TAG_SKIP_NOT_SOLR));
	}

	@Test
	public void parseQtpMigrateCollectionTest() {
		final String qtp = "{\"insertId\":\"bx***REMOVED***hwfao05y0\",\"jsonPayload\":{\"mes***REMOVED***ge\":\"2022-05-20 05:25:33.231 INFO  (qtp464887938-26) [c:proptest   ] o.a.s.s.HttpSolrCall [admin] webapp=null path=\\/admin\\/collections params={split.key=a!&target.collection=Test111&action=MIGRATE&collection=proptest} status=0 QTime=6785\"},\"resource\":{\"type\":\"gce_instance\",\"labels\":{\"instance_id\":\"4329029748489862071\",\"zone\":\"asia-south2-a\",\"project_id\":\"project-sccd\"}},\"timestamp\":\"2022-05-19T11:35:34.036037173Z\",\"labels\":{\"compute.googleapis.com\\/resource_name\":\"solr-qa\"},\"logName\":\"projects\\/project-sccd\\/logs\\/files\",\"receiveTimestamp\":\"2022-05-19T11:35:34.523632435Z\"}";
		ApacheSolrGcpConnector filter = getApacheSolrConnector(qtp);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("mes***REMOVED***ge", qtp);
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertNotNull(results);
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, matchListener.getMatchCount());
	}

	@Test
	public void parseQtpReIndexCollectionTest() {
		final String qtp = "{\"insertId\":\"bx***REMOVED***hwfao05y0\",\"jsonPayload\":{\"mes***REMOVED***ge\":\"2022-05-20 05:38:24.165 INFO  (qtp464887938-293) [   ] o.a.s.s.HttpSolrCall [admin] webapp=null path=\\/admin\\/collections params={name=Test111&action=REINDEXCOLLECTION} status=0 QTime=8873\"},\"resource\":{\"type\":\"gce_instance\",\"labels\":{\"instance_id\":\"4329029748489862071\",\"zone\":\"asia-south2-a\",\"project_id\":\"project-sccd\"}},\"timestamp\":\"2022-05-19T11:35:34.036037173Z\",\"labels\":{\"compute.googleapis.com\\/resource_name\":\"solr-qa\"},\"logName\":\"projects\\/project-sccd\\/logs\\/files\",\"receiveTimestamp\":\"2022-05-19T11:35:34.523632435Z\"}";
		ApacheSolrGcpConnector filter = getApacheSolrConnector(qtp);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("mes***REMOVED***ge", qtp);
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertNotNull(results);
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, matchListener.getMatchCount());
	}

	@Test
	public void parseQtpSkipBackUpTest() {
		final String qtp = "{\"insertId\":\"bx***REMOVED***hwfao05y0\",\"jsonPayload\":{\"mes***REMOVED***ge\":\"2022-05-20 11:34:39.277 INFO  (qtp464887938-26) [   ] o.a.s.s.HttpSolrCall [admin] webapp=null path=\\/admin\\/cores params={core=Test111_shard1_replica_n1&qt=\\/admin\\/cores&name=shard1&shardBackupId=md_shard1_0&action=BACKUPCORE&location=file:\\/\\/\\/C:\\/SolrCloud\\/solr-8.11.1\\/example\\/cloud\\/node1\\/solr\\/myBackupName2\\/Test111&incremental=true&wt=javabin&version=2} status=0 QTime=753\"},\"resource\":{\"type\":\"gce_instance\",\"labels\":{\"instance_id\":\"4329029748489862071\",\"zone\":\"asia-south2-a\",\"project_id\":\"project-sccd\"}},\"timestamp\":\"2022-05-19T11:35:34.036037173Z\",\"labels\":{\"compute.googleapis.com\\/resource_name\":\"solr-qa\"},\"logName\":\"projects\\/project-sccd\\/logs\\/files\",\"receiveTimestamp\":\"2022-05-19T11:35:34.523632435Z\"}";
		ApacheSolrGcpConnector filter = getApacheSolrConnector(qtp);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("mes***REMOVED***ge", qtp);
		List<Event> ev = new ArrayList<Event>();
		ev.add(e);
		Collection<Event> results = filter.filter(ev, matchListener);
		assertEquals(0, results.size());
		Set<String> tags = new HashSet<>((ArrayList) e.getField("tags"));
		assertEquals(1, tags.size());
		assertTrue(tags.contains(LOGSTASH_TAG_SKIP_NOT_SOLR));
	}

	@Test
	public void parseQtpBackUpCollectionTest() {
		final String qtp = "{\"insertId\":\"bx***REMOVED***hwfao05y0\",\"jsonPayload\":{\"mes***REMOVED***ge\":\"2022-05-20 11:34:39.907 INFO  (qtp464887938-23) [c:Test111   ] o.a.s.s.HttpSolrCall [admin] webapp=null path=\\/admin\\/collections params={name=myBackupName2&action=BACKUP&location=\\/opt\\/solr-8.6.0\\/example\\/cloud\\/node1\\/solr&collection=Test111} status=0 QTime=1678\"},\"resource\":{\"type\":\"gce_instance\",\"labels\":{\"instance_id\":\"4329029748489862071\",\"zone\":\"asia-south2-a\",\"project_id\":\"project-sccd\"}},\"timestamp\":\"2022-05-19T11:35:34.036037173Z\",\"labels\":{\"compute.googleapis.com\\/resource_name\":\"solr-qa\"},\"logName\":\"projects\\/project-sccd\\/logs\\/files\",\"receiveTimestamp\":\"2022-05-19T11:35:34.523632435Z\"}";
		ApacheSolrGcpConnector filter = getApacheSolrConnector(qtp);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("mes***REMOVED***ge", qtp);
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertNotNull(results);
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, matchListener.getMatchCount());
	}

	@Test
	public void parseQtpRestoreCollectionTest() {
		final String qtp = "{\"insertId\":\"bx***REMOVED***hwfao05y0\",\"jsonPayload\":{\"mes***REMOVED***ge\":\"2022-05-20 11:40:13.111 INFO  (qtp464887938-687) [c:myRestoredCollectionName   ] o.a.s.s.HttpSolrCall [admin] webapp=null path=\\/admin\\/collections params={name=myBackupName2&action=RESTORE&location=\\/opt\\/solr-8.6.0\\/example\\/cloud\\/node1\\/solr&collection=myRestoredCollectionName} status=0 QTime=3994\"},\"resource\":{\"type\":\"gce_instance\",\"labels\":{\"instance_id\":\"4329029748489862071\",\"zone\":\"asia-south2-a\",\"project_id\":\"project-sccd\"}},\"timestamp\":\"2022-05-19T11:35:34.036037173Z\",\"labels\":{\"compute.googleapis.com\\/resource_name\":\"solr-qa\"},\"logName\":\"projects\\/project-sccd\\/logs\\/files\",\"receiveTimestamp\":\"2022-05-19T11:35:34.523632435Z\"}";
		ApacheSolrGcpConnector filter = getApacheSolrConnector(qtp);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("mes***REMOVED***ge", qtp);
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertNotNull(results);
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, matchListener.getMatchCount());
	}

	@Test
	public void parseQtpRebalanceLeaderTest() {
		final String qtp = "{\"insertId\":\"bx***REMOVED***hwfao05y0\",\"jsonPayload\":{\"mes***REMOVED***ge\":\"2022-05-21 06:10:34.793 INFO  (qtp464887938-293) [c:proptest   ] o.a.s.s.HttpSolrCall [admin] webapp=null path=\\/admin\\/collections params={action=REBALANCELEADERS&collection=proptest&wt=json} status=0 QTime=37\"},\"resource\":{\"type\":\"gce_instance\",\"labels\":{\"instance_id\":\"4329029748489862071\",\"zone\":\"asia-south2-a\",\"project_id\":\"project-sccd\"}},\"timestamp\":\"2022-05-19T11:35:34.036037173Z\",\"labels\":{\"compute.googleapis.com\\/resource_name\":\"solr-qa\"},\"logName\":\"projects\\/project-sccd\\/logs\\/files\",\"receiveTimestamp\":\"2022-05-19T11:35:34.523632435Z\"}";
		ApacheSolrGcpConnector filter = getApacheSolrConnector(qtp);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("mes***REMOVED***ge", qtp);
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertNotNull(results);
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, matchListener.getMatchCount());
	}

	@Test
	public void parseQtpListCollectionTest() {
		final String qtp = "{\"insertId\":\"g3ulzsfq8d4ho\",\"jsonPayload\":{\"mes***REMOVED***ge\":\"2022-05-19 16:04:32.563 INFO  (qtp464887938-17) [   ] o.a.s.s.HttpSolrCall [admin] webapp=null path=\\/admin\\/collections params={action=LIST} status=0 QTime=0\"},\"resource\":{\"type\":\"gce_instance\",\"labels\":{\"instance_id\":\"2639899388249855782\",\"project_id\":\"project-sccd\",\"zone\":\"asia-south2-a\"}},\"timestamp\":\"2022-03-29T04:23:32.970132401Z\",\"logName\":\"projects\\/project-sccd\\/logs\\/files\",\"receiveTimestamp\":\"2022-03-29T04:23:33.320652100Z\"}";
		ApacheSolrGcpConnector filter = getApacheSolrConnector(qtp);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("mes***REMOVED***ge", qtp);
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertNotNull(results);
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, matchListener.getMatchCount());
	}

	@Test
	public void parseQtpListSkipTest() {
		final String qtp = "{\"insertId\":\"bx***REMOVED***hwfao05y0\",\"jsonPayload\":{\"mes***REMOVED***ge\":\"2022-05-19 16:01:50.137 INFO  (qtp464887938-17) [   ] o.a.s.s.HttpSolrCall [admin] webapp=null path=\\/admin\\/collections params={action=LIST&wt=json&_=1652976104291} status=0 QTime=0\"},\"resource\":{\"type\":\"gce_instance\",\"labels\":{\"instance_id\":\"4329029748489862071\",\"zone\":\"asia-south2-a\",\"project_id\":\"project-sccd\"}},\"timestamp\":\"2022-05-19T11:35:34.036037173Z\",\"labels\":{\"compute.googleapis.com\\/resource_name\":\"solr-qa\"},\"logName\":\"projects\\/project-sccd\\/logs\\/files\",\"receiveTimestamp\":\"2022-05-19T11:35:34.523632435Z\"}";
		ApacheSolrGcpConnector filter = getApacheSolrConnector(qtp);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("mes***REMOVED***ge", qtp);
		List<Event> ev = new ArrayList<Event>();
		ev.add(e);
		Collection<Event> results = filter.filter(ev, matchListener);
		assertEquals(0, results.size());
		Set<String> tags = new HashSet<>((ArrayList) e.getField("tags"));
		assertEquals(1, tags.size());
		assertTrue(tags.contains(LOGSTASH_TAG_SKIP_NOT_SOLR));
	}

	@Test
	public void parseQtpClusterStatusTest() {
		final String qtp = "{\"insertId\":\"1gg7n63g3kehbcm\",\"jsonPayload\":{\"mes***REMOVED***ge\":\"2022-05-21 06:30:04.518 INFO  (qtp464887938-688) [   ] o.a.s.s.HttpSolrCall [admin] webapp=null path=\\/admin\\/collections params={action=CLUSTERSTATUS} status=0 QTime=41\"},\"resource\":{\"type\":\"gce_instance\",\"labels\":{\"instance_id\":\"4329029748489862071\",\"project_id\":\"project-sccd\",\"zone\":\"asia-south2-a\"}},\"timestamp\":\"2022-05-20T13:13:00.599651185Z\",\"labels\":{\"compute.googleapis.com\\/resource_name\":\"solr-qa\"},\"logName\":\"projects\\/project-sccd\\/logs\\/files\",\"receiveTimestamp\":\"2022-05-20T13:13:01.522528107Z\"}";
		ApacheSolrGcpConnector filter = getApacheSolrConnector(qtp);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("mes***REMOVED***ge", qtp);
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertNotNull(results);
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, matchListener.getMatchCount());
	}

	@Test
	public void parseQtpClusterPropTest() {
		final String qtp = "{\"insertId\":\"1gg7n63g3kehbcm\",\"jsonPayload\":{\"mes***REMOVED***ge\":\"2022-05-23 04:09:06.029 INFO  (qtp464887938-903) [   ] o.a.s.s.HttpSolrCall [admin] webapp=null path=\\/admin\\/collections params={val=https&name=urlScheme&action=CLUSTERPROP&wt=xml} status=0 QTime=4\"},\"resource\":{\"type\":\"gce_instance\",\"labels\":{\"instance_id\":\"4329029748489862071\",\"project_id\":\"project-sccd\",\"zone\":\"asia-south2-a\"}},\"timestamp\":\"2022-05-20T13:13:00.599651185Z\",\"labels\":{\"compute.googleapis.com\\/resource_name\":\"solr-qa\"},\"logName\":\"projects\\/project-sccd\\/logs\\/files\",\"receiveTimestamp\":\"2022-05-20T13:13:01.522528107Z\"}";
		ApacheSolrGcpConnector filter = getApacheSolrConnector(qtp);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("mes***REMOVED***ge", qtp);
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertNotNull(results);
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, matchListener.getMatchCount());
	}

	@Test
	public void parseQtpBalanceShardUniqueTest() {
		final String qtp = "{\"insertId\":\"1gg7n63g3kehbcm\",\"jsonPayload\":{\"mes***REMOVED***ge\":\"2022-05-23 04:13:59.677 INFO  (qtp464887938-569) [c:proptest   ] o.a.s.s.HttpSolrCall [admin] webapp=null path=\\/admin\\/collections params={property=preferredLeader&action=BALANCESHARDUNIQUE&collection=proptest&wt=xml} status=0 QTime=168\"},\"resource\":{\"type\":\"gce_instance\",\"labels\":{\"instance_id\":\"4329029748489862071\",\"project_id\":\"project-sccd\",\"zone\":\"asia-south2-a\"}},\"timestamp\":\"2022-05-20T13:13:00.599651185Z\",\"labels\":{\"compute.googleapis.com\\/resource_name\":\"solr-qa\"},\"logName\":\"projects\\/project-sccd\\/logs\\/files\",\"receiveTimestamp\":\"2022-05-20T13:13:01.522528107Z\"}";
		ApacheSolrGcpConnector filter = getApacheSolrConnector(qtp);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("mes***REMOVED***ge", qtp);
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertNotNull(results);
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, matchListener.getMatchCount());
	}

	@Test
	public void parseQtpUtilizeNodeTest() {
		final String qtp = "{\"insertId\":\"bx***REMOVED***hwfao05y1\",\"jsonPayload\":{\"mes***REMOVED***ge\":\"2022-05-25 03:47:54.011 INFO  (qtp464887938-23) [   ] o.a.s.s.HttpSolrCall [admin] webapp=null path=\\/admin\\/collections params={node=192.168.43.197:8983_solr&action=UTILIZENODE} status=0 QTime=804\"},\"resource\":{\"type\":\"gce_instance\",\"labels\":{\"project_id\":\"project-sccd\",\"zone\":\"asia-south2-a\",\"instance_id\":\"4329029748489862071\"}},\"timestamp\":\"2022-05-19T11:35:34.049526902Z\",\"labels\":{\"compute.googleapis.com\\/resource_name\":\"solr-qa\"},\"logName\":\"projects\\/project-sccd\\/logs\\/files\",\"receiveTimestamp\":\"2022-05-19T11:35:34.523632435Z\"}";
		ApacheSolrGcpConnector filter = getApacheSolrConnector(qtp);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("mes***REMOVED***ge", qtp);
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertNotNull(results);
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, matchListener.getMatchCount());
	}

	@Test
	public void parseQtpReplaceNodeTest() {
		final String qtp = "{\"insertId\":\"bx***REMOVED***hwfao05y1\",\"jsonPayload\":{\"mes***REMOVED***ge\":\"2022-05-25 03:49:42.686 INFO  (qtp464887938-201) [   ] o.a.s.s.HttpSolrCall [admin] webapp=null path=\\/admin\\/collections params={sourceNode=192.168.43.197:8983_solr&action=REPLACENODE&targetNode=192.168.43.197:8983_solr} status=400 QTime=105\"},\"resource\":{\"type\":\"gce_instance\",\"labels\":{\"project_id\":\"project-sccd\",\"zone\":\"asia-south2-a\",\"instance_id\":\"4329029748489862071\"}},\"timestamp\":\"2022-05-19T11:35:34.049526902Z\",\"labels\":{\"compute.googleapis.com\\/resource_name\":\"solr-qa\"},\"logName\":\"projects\\/project-sccd\\/logs\\/files\",\"receiveTimestamp\":\"2022-05-19T11:35:34.523632435Z\"}";
		ApacheSolrGcpConnector filter = getApacheSolrConnector(qtp);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("mes***REMOVED***ge", qtp);
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertNotNull(results);
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, matchListener.getMatchCount());
	}

	@Test
	public void parseQtpDeleteNodeTest() {
		final String qtp = "{\"insertId\":\"bx***REMOVED***hwfao05y1\",\"jsonPayload\":{\"mes***REMOVED***ge\":\"2022-05-25 03:54:56.755 INFO  (qtp464887938-204) [   ] o.a.s.s.HttpSolrCall [admin] webapp=null path=\\/admin\\/collections params={node=192.168.43.197:8983_solr&action=DELETENODE} status=0 QTime=50\"},\"resource\":{\"type\":\"gce_instance\",\"labels\":{\"project_id\":\"project-sccd\",\"zone\":\"asia-south2-a\",\"instance_id\":\"4329029748489862071\"}},\"timestamp\":\"2022-05-19T11:35:34.049526902Z\",\"labels\":{\"compute.googleapis.com\\/resource_name\":\"solr-qa\"},\"logName\":\"projects\\/project-sccd\\/logs\\/files\",\"receiveTimestamp\":\"2022-05-19T11:35:34.523632435Z\"}";
		ApacheSolrGcpConnector filter = getApacheSolrConnector(qtp);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("mes***REMOVED***ge", qtp);
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertNotNull(results);
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, matchListener.getMatchCount());
	}

	@Test
	public void parseQtpRemoveRoleTest() {
		final String qtp = "{\"insertId\":\"bx***REMOVED***hwfao05y1\",\"jsonPayload\":{\"mes***REMOVED***ge\":\"2022-05-25 03:53:15.219 INFO  (qtp464887938-171) [   ] o.a.s.s.HttpSolrCall [admin] webapp=null path=\\/admin\\/collections params={node=192.168.43.197:8983_solr&role=overseer&action=REMOVEROLE} status=0 QTime=53\"},\"resource\":{\"type\":\"gce_instance\",\"labels\":{\"project_id\":\"project-sccd\",\"zone\":\"asia-south2-a\",\"instance_id\":\"4329029748489862071\"}},\"timestamp\":\"2022-05-19T11:35:34.049526902Z\",\"labels\":{\"compute.googleapis.com\\/resource_name\":\"solr-qa\"},\"logName\":\"projects\\/project-sccd\\/logs\\/files\",\"receiveTimestamp\":\"2022-05-19T11:35:34.523632435Z\"}";
		ApacheSolrGcpConnector filter = getApacheSolrConnector(qtp);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("mes***REMOVED***ge", qtp);
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertNotNull(results);
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, matchListener.getMatchCount());
	}

	@Test
	public void parseQtpAddRoleTest() {
		final String qtp = "{\"insertId\":\"1gg7n63g3kehbcm\",\"jsonPayload\":{\"mes***REMOVED***ge\":\"2022-05-23 04:40:29.923 INFO  (qtp464887938-1234) [   ] o.a.s.s.HttpSolrCall [admin] webapp=null path=\\/admin\\/collections params={node=node2&role=overseer&action=ADDROLE} status=0 QTime=61\"},\"resource\":{\"type\":\"gce_instance\",\"labels\":{\"instance_id\":\"4329029748489862071\",\"project_id\":\"project-sccd\",\"zone\":\"asia-south2-a\"}},\"timestamp\":\"2022-05-20T13:13:00.599651185Z\",\"labels\":{\"compute.googleapis.com\\/resource_name\":\"solr-qa\"},\"logName\":\"projects\\/project-sccd\\/logs\\/files\",\"receiveTimestamp\":\"2022-05-20T13:13:01.522528107Z\"}";
		ApacheSolrGcpConnector filter = getApacheSolrConnector(qtp);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("mes***REMOVED***ge", qtp);
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertNotNull(results);
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, matchListener.getMatchCount());
	}

	@Test
	public void parseQtpOVERSEERSTATUSTest() {
		final String qtp = "{\"insertId\":\"1gg7n63g3kehbcm\",\"jsonPayload\":{\"mes***REMOVED***ge\":\"2022-05-23 04:42:52.826 INFO  (qtp464887938-903) [   ] o.a.s.s.HttpSolrCall [admin] webapp=null path=\\/admin\\/collections params={action=OVERSEERSTATUS} status=0 QTime=163\"},\"resource\":{\"type\":\"gce_instance\",\"labels\":{\"instance_id\":\"4329029748489862071\",\"project_id\":\"project-sccd\",\"zone\":\"asia-south2-a\"}},\"timestamp\":\"2022-05-20T13:13:00.599651185Z\",\"labels\":{\"compute.googleapis.com\\/resource_name\":\"solr-qa\"},\"logName\":\"projects\\/project-sccd\\/logs\\/files\",\"receiveTimestamp\":\"2022-05-20T13:13:01.522528107Z\"}";
		ApacheSolrGcpConnector filter = getApacheSolrConnector(qtp);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("mes***REMOVED***ge", qtp);
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertNotNull(results);
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, matchListener.getMatchCount());
	}

	@Test
	public void parseQtpMIGRATESTATEFORMATTest() {
		final String qtp = "{\"insertId\":\"1gg7n63g3kehbcm\",\"jsonPayload\":{\"mes***REMOVED***ge\":\"2022-05-23 07:08:11.865 INFO  (qtp464887938-1231) [c:proptest   ] o.a.s.s.HttpSolrCall [admin] webapp=null path=\\/admin\\/collections params={action=MIGRATESTATEFORMAT&collection=proptest} status=0 QTime=107\"},\"resource\":{\"type\":\"gce_instance\",\"labels\":{\"instance_id\":\"4329029748489862071\",\"project_id\":\"project-sccd\",\"zone\":\"asia-south2-a\"}},\"timestamp\":\"2022-05-20T13:13:00.599651185Z\",\"labels\":{\"compute.googleapis.com\\/resource_name\":\"solr-qa\"},\"logName\":\"projects\\/project-sccd\\/logs\\/files\",\"receiveTimestamp\":\"2022-05-20T13:13:01.522528107Z\"}";
		ApacheSolrGcpConnector filter = getApacheSolrConnector(qtp);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("mes***REMOVED***ge", qtp);
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertNotNull(results);
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, matchListener.getMatchCount());
	}

	@Test
	public void parseQtpStatusCoreTest() {
		final String qtp = "{\"insertId\":\"3bvrk9fqomcqe\",\"jsonPayload\":{\"mes***REMOVED***ge\":\"2022-06-07 15:53:35.254 INFO  (qtp527804008-19) [   ] o.a.s.s.HttpSolrCall [admin] webapp=null path=\\/admin\\/cores params={action=STATUS} status=0 QTime=2\"},\"resource\":{\"type\":\"gce_instance\",\"labels\":{\"zone\":\"asia-south2-a\",\"instance_id\":\"6290282768426383618\",\"project_id\":\"project-sccd\"}},\"timestamp\":\"2022-06-07T15:53:35.255274747Z\",\"labels\":{\"compute.googleapis.com\\/resource_name\":\"apachesolrqa\"},\"logName\":\"projects\\/project-sccd\\/logs\\/files\",\"receiveTimestamp\":\"2022-06-07T15:53:35.618039128Z\"}";
		ApacheSolrGcpConnector filter = getApacheSolrConnector(qtp);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("mes***REMOVED***ge", qtp);
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertNotNull(results);
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, matchListener.getMatchCount());
	}

	@Test
	public void parseQtpStatusCoreTest2() {
		final String qtp = "{\"insertId\":\"3bvrk9fqomcqe\",\"jsonPayload\":{\"mes***REMOVED***ge\":\"2022-06-08 05:35:27.907 INFO  (qtp2005169944-26) [   ] o.a.s.s.HttpSolrCall [admin] webapp=null path=\\/admin\\/cores params={core=Product&action=STATUS} status=0 QTime=70\"},\"resource\":{\"type\":\"gce_instance\",\"labels\":{\"zone\":\"asia-south2-a\",\"instance_id\":\"6290282768426383618\",\"project_id\":\"project-sccd\"}},\"timestamp\":\"2022-06-07T15:53:35.255274747Z\",\"labels\":{\"compute.googleapis.com\\/resource_name\":\"apachesolrqa\"},\"logName\":\"projects\\/project-sccd\\/logs\\/files\",\"receiveTimestamp\":\"2022-06-07T15:53:35.618039128Z\"}";
		ApacheSolrGcpConnector filter = getApacheSolrConnector(qtp);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("mes***REMOVED***ge", qtp);
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertNotNull(results);
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, matchListener.getMatchCount());
	}

	@Test
	public void parseQtpSplitCoreTest() {
		final String qtp = "{\"insertId\":\"3bvrk9fqomcqe\",\"jsonPayload\":{\"mes***REMOVED***ge\":\"2022-06-08 05:42:41.420 INFO  (qtp2005169944-56) [   ] o.a.s.s.HttpSolrCall [admin] webapp=null path=\\/admin\\/cores params={core=testnew&action=SPLIT&targetCore=second_collection} status=0 QTime=493\"},\"resource\":{\"type\":\"gce_instance\",\"labels\":{\"zone\":\"asia-south2-a\",\"instance_id\":\"6290282768426383618\",\"project_id\":\"project-sccd\"}},\"timestamp\":\"2022-06-07T15:53:35.255274747Z\",\"labels\":{\"compute.googleapis.com\\/resource_name\":\"apachesolrqa\"},\"logName\":\"projects\\/project-sccd\\/logs\\/files\",\"receiveTimestamp\":\"2022-06-07T15:53:35.618039128Z\"}";
		ApacheSolrGcpConnector filter = getApacheSolrConnector(qtp);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("mes***REMOVED***ge", qtp);
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertNotNull(results);
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, matchListener.getMatchCount());
	}

	// Shard Management----
	@Test
	public void parseQtpSPLITSHARDTest() {
		final String qtp = "{\"insertId\":\"g3ulzsfq8d4ho\",\"jsonPayload\":{\"mes***REMOVED***ge\":\"2022-05-23 08:59:46.112 INFO  (qtp1939990953-24) [c:demo   ] o.a.s.s.HttpSolrCall [admin] webapp=null path=\\/admin\\/collections params={action=SPLITSHARD&collection=demo&shard=shard1&wt=xml} status=0 QTime=7715\"},\"resource\":{\"type\":\"gce_instance\",\"labels\":{\"instance_id\":\"2639899388249855782\",\"project_id\":\"project-sccd\",\"zone\":\"asia-south2-a\"}},\"timestamp\":\"2022-03-29T04:23:32.970132401Z\",\"logName\":\"projects\\/project-sccd\\/logs\\/files\",\"receiveTimestamp\":\"2022-03-29T04:23:33.320652100Z\"}";
		ApacheSolrGcpConnector filter = getApacheSolrConnector(qtp);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("mes***REMOVED***ge", qtp);
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertNotNull(results);
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, matchListener.getMatchCount());
	}

	@Test
	public void parseQtpDELETESHARDTest() {
		final String qtp = "{\"insertId\":\"g3ulzsfq8d4ho\",\"jsonPayload\":{\"mes***REMOVED***ge\":\"2022-05-23 09:30:20.360 INFO  (qtp1939990953-19) [c:demo   ] o.a.s.s.HttpSolrCall [admin] webapp=null path=\\/admin\\/collections params={action=DELETESHARD&collection=demo&shard=shard1&wt=xml} status=0 QTime=108\"},\"resource\":{\"type\":\"gce_instance\",\"labels\":{\"instance_id\":\"2639899388249855782\",\"project_id\":\"project-sccd\",\"zone\":\"asia-south2-a\"}},\"timestamp\":\"2022-03-29T04:23:32.970132401Z\",\"logName\":\"projects\\/project-sccd\\/logs\\/files\",\"receiveTimestamp\":\"2022-03-29T04:23:33.320652100Z\"}";
		ApacheSolrGcpConnector filter = getApacheSolrConnector(qtp);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("mes***REMOVED***ge", qtp);
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertNotNull(results);
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, matchListener.getMatchCount());
	}

	@Test
	public void parseQtpFORCELEADERTest() {
		final String qtp = "{\"insertId\":\"bx***REMOVED***hwfao05y0\",\"jsonPayload\":{\"mes***REMOVED***ge\":\"2022-05-24 13:46:31.022 INFO  (qtp1939990953-306) [c:demo1   ] o.a.s.s.HttpSolrCall [admin] webapp=null path=\\/admin\\/collections params={action=FORCELEADER&collection=demo1&shard=shard1} status=500 QTime=210\"},\"resource\":{\"type\":\"gce_instance\",\"labels\":{\"instance_id\":\"4329029748489862071\",\"zone\":\"asia-south2-a\",\"project_id\":\"project-sccd\"}},\"timestamp\":\"2022-05-19T11:35:34.036037173Z\",\"labels\":{\"compute.googleapis.com\\/resource_name\":\"solr-qa\"},\"logName\":\"projects\\/project-sccd\\/logs\\/files\",\"receiveTimestamp\":\"2022-05-19T11:35:34.523632435Z\"}";
		ApacheSolrGcpConnector filter = getApacheSolrConnector(qtp);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("mes***REMOVED***ge", qtp);
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertNotNull(results);
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, matchListener.getMatchCount());
	}

	@Test
	public void parseQtpCREATESHARDTest() {
		final String qtp = "{\"insertId\":\"bx***REMOVED***hwfao05y0\",\"jsonPayload\":{\"mes***REMOVED***ge\":\"2022-05-24 13:57:04.083 INFO  (qtp1939990953-266) [c:demo1   ] o.a.s.s.HttpSolrCall [admin] webapp=null path=\\/admin\\/collections params={action=CREATESHARD&shard=shardName&collection=demo1} status=400 QTime=10\"},\"resource\":{\"type\":\"gce_instance\",\"labels\":{\"instance_id\":\"4329029748489862071\",\"zone\":\"asia-south2-a\",\"project_id\":\"project-sccd\"}},\"timestamp\":\"2022-05-19T11:35:34.036037173Z\",\"labels\":{\"compute.googleapis.com\\/resource_name\":\"solr-qa\"},\"logName\":\"projects\\/project-sccd\\/logs\\/files\",\"receiveTimestamp\":\"2022-05-19T11:35:34.523632435Z\"}";
		ApacheSolrGcpConnector filter = getApacheSolrConnector(qtp);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("mes***REMOVED***ge", qtp);
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertNotNull(results);
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, matchListener.getMatchCount());
	}

	@Test
	public void parseQtpREQUESTSTATUSTest() {
		final String qtp = "{\"insertId\":\"1gg7n63g3kehbcm\",\"jsonPayload\":{\"mes***REMOVED***ge\":\"2022-05-31 06:55:50.613 INFO  (qtp464887938-1139) [   ] o.a.s.s.HttpSolrCall [admin] webapp=null path=\\/admin\\/collections params={requestid=1000&action=REQUESTSTATUS} status=0 QTime=4\"},\"resource\":{\"type\":\"gce_instance\",\"labels\":{\"instance_id\":\"4329029748489862071\",\"project_id\":\"project-sccd\",\"zone\":\"asia-south2-a\"}},\"timestamp\":\"2022-05-20T13:13:00.599651185Z\",\"labels\":{\"compute.googleapis.com\\/resource_name\":\"solr-qa\"},\"logName\":\"projects\\/project-sccd\\/logs\\/files\",\"receiveTimestamp\":\"2022-05-20T13:13:01.522528107Z\"}";
		ApacheSolrGcpConnector filter = getApacheSolrConnector(qtp);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("mes***REMOVED***ge", qtp);
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertNotNull(results);
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, matchListener.getMatchCount());
	}

	// Replica Management-----

	@Test
	public void parseQtpAddReplicaTest() {
		final String qtp = "{\"insertId\":\"bx***REMOVED***hwfao05y1\",\"jsonPayload\":{\"mes***REMOVED***ge\":\"2022-05-25 03:09:32.734 INFO  (qtp464887938-204) [c:films   ] o.a.s.s.HttpSolrCall [admin] webapp=null path=\\/admin\\/collections params={node=192.168.43.197:8983_solr&action=ADDREPLICA&collection=films&shard=shard1&wt=xml} status=0 QTime=2160\"},\"resource\":{\"type\":\"gce_instance\",\"labels\":{\"project_id\":\"project-sccd\",\"zone\":\"asia-south2-a\",\"instance_id\":\"4329029748489862071\"}},\"timestamp\":\"2022-05-19T11:35:34.049526902Z\",\"labels\":{\"compute.googleapis.com\\/resource_name\":\"solr-qa\"},\"logName\":\"projects\\/project-sccd\\/logs\\/files\",\"receiveTimestamp\":\"2022-05-19T11:35:34.523632435Z\"}";
		ApacheSolrGcpConnector filter = getApacheSolrConnector(qtp);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("mes***REMOVED***ge", qtp);
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertNotNull(results);
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, matchListener.getMatchCount());
	}

	@Test
	public void parseQtpMoveReplicaTest() {
		final String qtp = "{\"insertId\":\"bx***REMOVED***hwfao05y1\",\"jsonPayload\":{\"mes***REMOVED***ge\":\"2022-05-25 03:29:51.504 INFO  (qtp464887938-23) [c:films   ] o.a.s.s.HttpSolrCall [admin] webapp=null path=\\/admin\\/collections params={sourceNode=192.168.43.197:8983_solr&replica=core_node7&action=MOVEREPLICA&collection=films&shard=shard1&targetNode=192.168.43.197:8983_solr} status=0 QTime=1861\"},\"resource\":{\"type\":\"gce_instance\",\"labels\":{\"project_id\":\"project-sccd\",\"zone\":\"asia-south2-a\",\"instance_id\":\"4329029748489862071\"}},\"timestamp\":\"2022-05-19T11:35:34.049526902Z\",\"labels\":{\"compute.googleapis.com\\/resource_name\":\"solr-qa\"},\"logName\":\"projects\\/project-sccd\\/logs\\/files\",\"receiveTimestamp\":\"2022-05-19T11:35:34.523632435Z\"}";
		ApacheSolrGcpConnector filter = getApacheSolrConnector(qtp);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("mes***REMOVED***ge", qtp);
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertNotNull(results);
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, matchListener.getMatchCount());
	}

	@Test
	public void parseQtpDeleteReplicaTest() {
		final String qtp = "{\"insertId\":\"bx***REMOVED***hwfao05y1\",\"jsonPayload\":{\"mes***REMOVED***ge\":\"2022-05-23 09:26:13.658 INFO  (qtp1939990953-23) [c:demo   ] o.a.s.s.HttpSolrCall [admin] webapp=null path=\\/admin\\/collections params={replica=core_node2&action=DELETEREPLICA&collection=demo&shard=shard1&wt=xml} status=0 QTime=496\"},\"resource\":{\"type\":\"gce_instance\",\"labels\":{\"project_id\":\"project-sccd\",\"zone\":\"asia-south2-a\",\"instance_id\":\"4329029748489862071\"}},\"timestamp\":\"2022-05-19T11:35:34.049526902Z\",\"labels\":{\"compute.googleapis.com\\/resource_name\":\"solr-qa\"},\"logName\":\"projects\\/project-sccd\\/logs\\/files\",\"receiveTimestamp\":\"2022-05-19T11:35:34.523632435Z\"}";
		ApacheSolrGcpConnector filter = getApacheSolrConnector(qtp);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("mes***REMOVED***ge", qtp);
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertNotNull(results);
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, matchListener.getMatchCount());
	}

	@Test
	public void parseQtpAddReplicaPropTest() {
		final String qtp = "{\"insertId\":\"bx***REMOVED***hwfao05y1\",\"jsonPayload\":{\"mes***REMOVED***ge\":\"2022-05-25 03:35:09.780 INFO  (qtp464887938-171) [c:films   ] o.a.s.s.HttpSolrCall [admin] webapp=null path=\\/admin\\/collections params={replica=core_node2&property.value=special&property=special&action=ADDREPLICAPROP&collection=films&shard=shard1} status=0 QTime=90\"},\"resource\":{\"type\":\"gce_instance\",\"labels\":{\"project_id\":\"project-sccd\",\"zone\":\"asia-south2-a\",\"instance_id\":\"4329029748489862071\"}},\"timestamp\":\"2022-05-19T11:35:34.049526902Z\",\"labels\":{\"compute.googleapis.com\\/resource_name\":\"solr-qa\"},\"logName\":\"projects\\/project-sccd\\/logs\\/files\",\"receiveTimestamp\":\"2022-05-19T11:35:34.523632435Z\"}";
		ApacheSolrGcpConnector filter = getApacheSolrConnector(qtp);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("mes***REMOVED***ge", qtp);
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertNotNull(results);
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, matchListener.getMatchCount());
	}

	@Test
	public void parseQtpDeleteReplicaPropTest() {
		final String qtp = "{\"insertId\":\"bx***REMOVED***hwfao05y1\",\"jsonPayload\":{\"mes***REMOVED***ge\":\"2022-05-25 03:40:30.171 INFO  (qtp464887938-23) [c:films   ] o.a.s.s.HttpSolrCall [admin] webapp=null path=\\/admin\\/collections params={replica=core_node2&property=preferredLeader&action=DELETEREPLICAPROP&shard=shard1&collection=films&wt=xml} status=0 QTime=51\"},\"resource\":{\"type\":\"gce_instance\",\"labels\":{\"project_id\":\"project-sccd\",\"zone\":\"asia-south2-a\",\"instance_id\":\"4329029748489862071\"}},\"timestamp\":\"2022-05-19T11:35:34.049526902Z\",\"labels\":{\"compute.googleapis.com\\/resource_name\":\"solr-qa\"},\"logName\":\"projects\\/project-sccd\\/logs\\/files\",\"receiveTimestamp\":\"2022-05-19T11:35:34.523632435Z\"}";
		ApacheSolrGcpConnector filter = getApacheSolrConnector(qtp);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("mes***REMOVED***ge", qtp);
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertNotNull(results);
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, matchListener.getMatchCount());
	}

	// Collection Alias Management----
	@Test
	public void parseQtpCreateAliasTest() {
		final String qtp = "{\"insertId\":\"bx***REMOVED***hwfao05y1\",\"jsonPayload\":{\"mes***REMOVED***ge\":\"2022-05-23 09:53:16.443 INFO  (qtp1939990953-22) [   ] o.a.s.s.HttpSolrCall [admin] webapp=null path=\\/admin\\/collections params={collections=demo,demo1&name=testalias&action=CREATEALIAS&wt=xml} status=0 QTime=195\"},\"resource\":{\"type\":\"gce_instance\",\"labels\":{\"project_id\":\"project-sccd\",\"zone\":\"asia-south2-a\",\"instance_id\":\"4329029748489862071\"}},\"timestamp\":\"2022-05-19T11:35:34.049526902Z\",\"labels\":{\"compute.googleapis.com\\/resource_name\":\"solr-qa\"},\"logName\":\"projects\\/project-sccd\\/logs\\/files\",\"receiveTimestamp\":\"2022-05-19T11:35:34.523632435Z\"}";
		ApacheSolrGcpConnector filter = getApacheSolrConnector(qtp);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("mes***REMOVED***ge", qtp);
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertNotNull(results);
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, matchListener.getMatchCount());
	}

	@Test
	public void parseQtpListAliasTest() {
		final String qtp = "{\"insertId\":\"bx***REMOVED***hwfao05y1\",\"jsonPayload\":{\"mes***REMOVED***ge\":\"2022-05-23 09:57:36.136 INFO  (qtp1939990953-113) [   ] o.a.s.s.HttpSolrCall [admin] webapp=null path=\\/admin\\/collections params={action=LISTALIASES&wt=xml} status=0 QTime=14\"},\"resource\":{\"type\":\"gce_instance\",\"labels\":{\"project_id\":\"project-sccd\",\"zone\":\"asia-south2-a\",\"instance_id\":\"4329029748489862071\"}},\"timestamp\":\"2022-05-19T11:35:34.049526902Z\",\"labels\":{\"compute.googleapis.com\\/resource_name\":\"solr-qa\"},\"logName\":\"projects\\/project-sccd\\/logs\\/files\",\"receiveTimestamp\":\"2022-05-19T11:35:34.523632435Z\"}";
		ApacheSolrGcpConnector filter = getApacheSolrConnector(qtp);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("mes***REMOVED***ge", qtp);
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertNotNull(results);
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, matchListener.getMatchCount());
	}

	@Test
	public void parseQtpAliasPropTest() {
		final String qtp = "{\"insertId\":\"bx***REMOVED***hwfao05y1\",\"jsonPayload\":{\"mes***REMOVED***ge\":\"2022-05-23 10:03:43.969 INFO  (qtp1939990953-113) [   ] o.a.s.s.HttpSolrCall [admin] webapp=null path=\\/admin\\/collections params={name=testalias&action=ALIASPROP&property.otherKey=otherValue&wt=xml&property.someKey=someValue} status=0 QTime=237\"},\"resource\":{\"type\":\"gce_instance\",\"labels\":{\"project_id\":\"project-sccd\",\"zone\":\"asia-south2-a\",\"instance_id\":\"4329029748489862071\"}},\"timestamp\":\"2022-05-19T11:35:34.049526902Z\",\"labels\":{\"compute.googleapis.com\\/resource_name\":\"solr-qa\"},\"logName\":\"projects\\/project-sccd\\/logs\\/files\",\"receiveTimestamp\":\"2022-05-19T11:35:34.523632435Z\"}";
		ApacheSolrGcpConnector filter = getApacheSolrConnector(qtp);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("mes***REMOVED***ge", qtp);
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertNotNull(results);
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, matchListener.getMatchCount());
	}

	@Test
	public void parseQtpDeleteAliasTest() {
		final String qtp = "{\"insertId\":\"2s4s1xg18cz4ws\",\"jsonPayload\":{\"mes***REMOVED***ge\":\"2022-05-31 06:22:53.921 INFO  (qtp1077072774-21) [   ] o.a.s.s.HttpSolrCall [admin] webapp=null path=\\/admin\\/collections params={flush=true&action=DELETESTATUS&wt=xml} status=0 QTime=6\"},\"resource\":{\"type\":\"gce_instance\",\"labels\":{\"instance_id\":\"6290282768426383618\",\"zone\":\"asia-south2-a\",\"project_id\":\"project-sccd\"}},\"timestamp\":\"2022-05-31T06:22:53.921152976Z\",\"labels\":{\"compute.googleapis.com\\/resource_name\":\"apachesolrqa\"},\"logName\":\"projects\\/project-sccd\\/logs\\/files\",\"receiveTimestamp\":\"2022-05-31T06:22:54.640401665Z\"}";
		ApacheSolrGcpConnector filter = getApacheSolrConnector(qtp);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("mes***REMOVED***ge", qtp);
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertNotNull(results);
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, matchListener.getMatchCount());
	}

	@Test
	public void parseQtpErrorRecordTest2() {
		final String qtp = "{\"insertId\":\"1987nofg10esctd\",\"jsonPayload\":{\"mes***REMOVED***ge\":\"2022-04-13 10:57:30.617 ERROR (qtp672313607-18) [   x:Testing] o.a.s.h.RequestHandlerBase org.apache.solr.common.SolrException: Expected: OBJECT_START but got STRING at [2]\"},\"resource\":{\"type\":\"gce_instance\",\"labels\":{\"project_id\":\"project-sccd\",\"zone\":\"asia-south2-a\",\"instance_id\":\"2639899388249855782\"}},\"timestamp\":\"2022-04-13T10:57:30.618757Z\",\"logName\":\"projects\\/project-sccd\\/logs\\/files\",\"receiveTimestamp\":\"2022-04-13T10:57:31.450095866Z\"}";
		ApacheSolrGcpConnector filter = getApacheSolrConnector(qtp);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("mes***REMOVED***ge", qtp);
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertNotNull(results);
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, matchListener.getMatchCount());
	}

	private ApacheSolrGcpConnector getApacheSolrConnector(String jsonString) {
		Configuration config = new ConfigurationImpl(Collections.singletonMap("source", jsonString));
		Context context = new ContextImpl(null, null);
		ApacheSolrGcpConnector filter = new ApacheSolrGcpConnector("solr-id", config, context);
		return filter;
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
}
