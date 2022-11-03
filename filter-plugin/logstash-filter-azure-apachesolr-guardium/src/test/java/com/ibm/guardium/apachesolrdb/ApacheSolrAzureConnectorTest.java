/*
 Copyright IBM Corp. 2021, 2022 All rights reserved.
 SPDX-License-Identifier: Apache-2.0
*/
package com.ibm.guardium.apachesolrdb;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.logstash.plugins.ContextImpl;
import static com.ibm.guardium.apachesolrdb.ApacheSolrAzureConnector.LOGSTASH_TAG_SKIP_NOT_SOLR;
import static com.ibm.guardium.apachesolrdb.ApacheSolrAzureConnector.LOGSTASH_TAG_SOLR_PARSE_ERROR;

import com.ibm.guardium.univer***REMOVED***lconnector.commons.GuardConstants;
import co.elastic.logstash.api.Context;
import org.logstash.plugins.ConfigurationImpl;
import co.elastic.logstash.api.Configuration;
import co.elastic.logstash.api.Event;
import co.elastic.logstash.api.FilterMatchListener;
import co.elastic.logstash.api.PluginConfigSpec;

public class ApacheSolrAzureConnectorTest {

	final static Context context = new ContextImpl(null, null);
	final static ApacheSolrAzureConnector filter = new ApacheSolrAzureConnector("test-id", null, context);

	/**
	 * To feed Guardium univer***REMOVED***l connector, a "GuardRecord" fields must exist.
	 * 
	 * Filter should add field "GuardRecord" to the Event, which Univer***REMOVED***l connector
	 * then inserts into Guardium.
	 */

	@Test
	public void configSchemaTest() {
		final String qtp = "";
		ApacheSolrAzureConnector filter = getApacheSolrConnector(qtp);
		Collection<PluginConfigSpec<?>> events = filter.configSchema();
		Assert.assertNotNull(events);
	}

	@Test
	public void commonUtilsTest() {
		CommonUtils commonUtils = new CommonUtils();
		Assert.assertNotNull(commonUtils);
	}

	@Test
	public void getIdTest() {
		String id = filter.getId();
		Assert.assertNotNull(id);
	}

	// while executing query
	@Test
	public void parseRecordQtpExecuteTest() {

		final String qtp = "2022-02-09 06:49:31.070 INFO  (qtp234250762-22) [   x:demo_collection] o.a.s.c.S.Request [demo_collection]  webapp=/solr path=/select params={q=id:401&_=1644230860222} hits=1 status=0 QTime=0";
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("mes***REMOVED***ge", qtp);
		e.setField(ApplicationConstant.TIMESTAMP, ApplicationConstantTest.TIMESTAMP_VALUE);
		e.setField(ApplicationConstant.QUERY_STRING, ApplicationConstantTest.QUERYSTRING_SELECT_VALUE);
		e.setField(ApplicationConstant.COLLECTION, ApplicationConstantTest.COLLECTION_VALUE);
		e.setField(ApplicationConstant.VERB, ApplicationConstantTest.PATH_SELECT_VALUE);
		e.setField(ApplicationConstant.SERVER_HOSTNAME, ApplicationConstantTest.SERVERHOSTNAME_VALUE);
		e.setField(ApplicationConstant.SERVER_IP, ApplicationConstantTest.SERVERIP_VALUE);
		e.setField(ApplicationConstant.SERVER_OS, ApplicationConstantTest.SERVEROS_VALUE);
		e.setField(ApplicationConstant.LOG_TYPE, ApplicationConstantTest.LOG_INFO_VALUE);
		e.setField(ApplicationConstant.CLASS, ApplicationConstantTest.SOLR_QTP_REQUEST_VALUE);
		e.setField(ApplicationConstant.USER_NAME, ApplicationConstantTest.USERNAME_VALUE);
		e.setField(ApplicationConstant.WEB_APP, ApplicationConstantTest.WEBAPP_VALUE);
		e.setField(ApplicationConstant.KEY_CORE, ApplicationConstantTest.KEY_CORE_VALUE);
		e.setField(ApplicationConstant.WEBAPP_FOR_FULLSQL, ApplicationConstantTest.WEBAPP_FOR_FULLSQL_VALUE);
		e.setField(ApplicationConstant.PATH_FOR_FULLSQL, ApplicationConstantTest.PATH_FOR_FULLSQL_VALUE);
		e.setField(ApplicationConstant.PARAMS, ApplicationConstantTest.PARAMS_VALUE);

		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);

		Assert.assertEquals(1, results.size());
		Assert.assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		Assert.assertEquals(1, matchListener.getMatchCount());
	}
	
	
	
	@Test
	public void parseRecordQtpExecuteTest2() {

		final String qtp = "2022-02-09 06:49:31.070 INFO  (qtp234250762-22) [   x:demo_collection] o.a.s.c.S.Request [demo_collection]  webapp=/solr path=/select params={q=id:401&_=1644230860222} hits=1 status=0 QTime=0";
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("mes***REMOVED***ge", qtp);
		e.setField(ApplicationConstant.TIMESTAMP, ApplicationConstantTest.TIMESTAMP_VALUE);
		e.setField(ApplicationConstant.QUERY_STRING, ApplicationConstantTest.QUERYSTRING_SELECT_VALUE);
		e.setField(ApplicationConstant.COLLECTION, null);
		e.setField(ApplicationConstant.VERB, ApplicationConstantTest.PATH_SELECT_VALUE);
		e.setField(ApplicationConstant.SERVER_HOSTNAME, ApplicationConstantTest.SERVERHOSTNAME_VALUE);
		e.setField(ApplicationConstant.SERVER_IP, ApplicationConstantTest.SERVERIP_VALUE);
		e.setField(ApplicationConstant.SERVER_OS, ApplicationConstantTest.SERVEROS_VALUE);
		e.setField(ApplicationConstant.LOG_TYPE, ApplicationConstantTest.LOG_INFO_VALUE);
		e.setField(ApplicationConstant.CLASS, ApplicationConstantTest.SOLR_QTP_REQUEST_VALUE);
		e.setField(ApplicationConstant.USER_NAME, ApplicationConstantTest.USERNAME_VALUE);
		e.setField(ApplicationConstant.WEB_APP, ApplicationConstantTest.WEBAPP_VALUE);
		e.setField(ApplicationConstant.KEY_CORE, ApplicationConstantTest.KEY_CORE_VALUE);
		e.setField(ApplicationConstant.WEBAPP_FOR_FULLSQL, ApplicationConstantTest.WEBAPP_FOR_FULLSQL_VALUE);
		e.setField(ApplicationConstant.PATH_FOR_FULLSQL, ApplicationConstantTest.PATH_FOR_FULLSQL_VALUE);
		e.setField(ApplicationConstant.PARAMS, ApplicationConstantTest.PARAMS_VALUE);

		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);

		Assert.assertEquals(1, results.size());
		Assert.assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		Assert.assertEquals(1, matchListener.getMatchCount());
	}

	@Test
	public void parseRecordQtpExecuteTestWithServerIp() {

		final String qtp = "2022-02-09 06:49:31.070 INFO  (qtp234250762-22) [   x:demo_collection] o.a.s.c.S.Request [demo_collection]  webapp=/solr path=/select params={q=id:401&_=1644230860222} hits=1 status=0 QTime=0";
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("mes***REMOVED***ge", qtp);
		e.setField(ApplicationConstant.TIMESTAMP, ApplicationConstantTest.TIMESTAMP_VALUE);
		e.setField(ApplicationConstant.QUERY_STRING, ApplicationConstantTest.QUERYSTRING_SELECT_VALUE);
		e.setField(ApplicationConstant.COLLECTION, ApplicationConstantTest.COLLECTION_VALUE);
		e.setField(ApplicationConstant.VERB, ApplicationConstantTest.PATH_SELECT_VALUE);
		e.setField(ApplicationConstant.SERVER_HOSTNAME, ApplicationConstantTest.SERVERHOSTNAME_VALUE);
		e.setField(ApplicationConstant.SERVER_IP, ApplicationConstantTest.SERVER_IPv6_VALUE);
		e.setField(ApplicationConstant.SERVER_OS, ApplicationConstantTest.SERVEROS_VALUE);
		e.setField(ApplicationConstant.LOG_TYPE, ApplicationConstantTest.LOG_INFO_VALUE);
		e.setField(ApplicationConstant.CLASS, ApplicationConstantTest.SOLR_QTP_REQUEST_VALUE);
		e.setField(ApplicationConstant.USER_NAME, ApplicationConstantTest.USERNAME_VALUE);
		e.setField(ApplicationConstant.WEB_APP, ApplicationConstantTest.WEBAPP_VALUE);
		e.setField(ApplicationConstant.KEY_CORE, ApplicationConstantTest.KEY_CORE_VALUE);
		e.setField(ApplicationConstant.WEBAPP_FOR_FULLSQL, ApplicationConstantTest.WEBAPP_FOR_FULLSQL_VALUE);
		e.setField(ApplicationConstant.PATH_FOR_FULLSQL, ApplicationConstantTest.PATH_FOR_FULLSQL_VALUE);
		e.setField(ApplicationConstant.PARAMS, ApplicationConstantTest.PARAMS_VALUE);

		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);

		Assert.assertEquals(1, results.size());
		Assert.assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		Assert.assertEquals(1, matchListener.getMatchCount());
	}

	@Test
	public void CreateCoreTest() {

		final String qtp = "2022-03-24 11:56:22.486 INFO  (qtp2005169944-57) [   ] o.a.s.s.HttpSolrCall [admin] webapp=null path=/admin/cores params={action=CREATE} status=0 QTime=2167";

		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("mes***REMOVED***ge", qtp);
		e.setField(ApplicationConstant.TIMESTAMP, ApplicationConstantTest.TIMESTAMP_VALUE);
		e.setField(ApplicationConstant.QUERY_STRING, ApplicationConstantTest.QUERYSTRING_CORECREATE_VALUE);
		e.setField(ApplicationConstant.CLASS, ApplicationConstantTest.HTTP_SOLR_CALL_VALUE);
		e.setField(ApplicationConstant.VERB, ApplicationConstantTest.PATH_CREATE_VALUE);
		e.setField(ApplicationConstant.SERVER_HOSTNAME, ApplicationConstantTest.SERVERHOSTNAME_VALUE);
		e.setField(ApplicationConstant.SERVER_IP, ApplicationConstantTest.SERVERIP_VALUE);
		e.setField(ApplicationConstant.SERVER_OS, ApplicationConstantTest.SERVEROS_VALUE);
		e.setField(ApplicationConstant.LOG_TYPE, ApplicationConstantTest.LOG_INFO_VALUE);
		e.setField(ApplicationConstant.USER_NAME, ApplicationConstantTest.USERNAME_VALUE);
		e.setField(ApplicationConstant.KEY_CORE, ApplicationConstantTest.KEY_CORE_VALUE);
		e.setField(ApplicationConstant.WEBAPP_FOR_FULLSQL, ApplicationConstantTest.WEBAPP_FOR_FULLSQL_VALUE);
		e.setField(ApplicationConstant.PATH_FOR_FULLSQL, ApplicationConstantTest.PATH_FOR_FULLSQL_VALUE);
		e.setField(ApplicationConstant.PARAMS, ApplicationConstantTest.PARAMS_VALUE);

		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);

		Assert.assertEquals(1, results.size());
		Assert.assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		Assert.assertEquals(1, matchListener.getMatchCount());
	}

	@Test
	public void CreateCollectionTest() {

		final String qtp = "2022-05-13 07:43:00.378 INFO  (qtp527804008-20) [   ] o.a.s.s.HttpSolrCall [admin] webapp=null path=/admin/collections params={replicationFactor=2&maxShardsPerNode=-1&collection.configName=techproducts&name=techproducts&action=CREATE&numShards=2&wt=json} status=0 QTime=4005";
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("mes***REMOVED***ge", qtp);
		e.setField(ApplicationConstant.TIMESTAMP, ApplicationConstantTest.TIMESTAMP_VALUE);
		e.setField(ApplicationConstant.QUERY_STRING, ApplicationConstantTest.QUERYSTRING_CREATECOLLECTION_VALUE);
		e.setField(ApplicationConstant.CLASS, ApplicationConstantTest.HTTP_SOLR_CALL_VALUE);
		e.setField(ApplicationConstant.VERB, ApplicationConstantTest.ADMIN_COLLECTION_VALUE);
		e.setField(ApplicationConstant.SERVER_HOSTNAME, ApplicationConstantTest.SERVERHOSTNAME_VALUE);
		e.setField(ApplicationConstant.SERVER_IP, ApplicationConstantTest.SERVERIP_VALUE);
		e.setField(ApplicationConstant.SERVER_OS, ApplicationConstantTest.SERVEROS_VALUE);
		e.setField(ApplicationConstant.LOG_TYPE, ApplicationConstantTest.LOG_INFO_VALUE);
		e.setField(ApplicationConstant.USER_NAME, ApplicationConstantTest.USERNAME_VALUE);
		e.setField(ApplicationConstant.KEY_CORE, ApplicationConstantTest.KEY_CORE_VALUE);
		e.setField(ApplicationConstant.WEBAPP_FOR_FULLSQL, ApplicationConstantTest.WEBAPP_FOR_FULLSQL_VALUE);
		e.setField(ApplicationConstant.PATH_FOR_FULLSQL, ApplicationConstantTest.PATH_FOR_FULLSQL_VALUE);
		e.setField(ApplicationConstant.PARAMS, ApplicationConstantTest.PARAMS_VALUE);

		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);

		Assert.assertEquals(1, results.size());
		Assert.assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		Assert.assertEquals(1, matchListener.getMatchCount());
	}

	@Test
	public void RenameCollectionTest() {

		final String qtp = "2022-05-13 08:46:23.290 INFO  (qtp527804008-21) [   ] o.a.s.s.HttpSolrCall [admin] webapp=null path=/admin/collections params={name=films&action=RENAME&target=rfilms} status=0 QTime=16";
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("mes***REMOVED***ge", qtp);
		e.setField(ApplicationConstant.TIMESTAMP, ApplicationConstantTest.TIMESTAMP_VALUE);
		e.setField(ApplicationConstant.QUERY_STRING, ApplicationConstantTest.QUERYSTRING_RENAMECOLLECTION_VALUE);
		e.setField(ApplicationConstant.CLASS, ApplicationConstantTest.HTTP_SOLR_CALL_VALUE);
		e.setField(ApplicationConstant.VERB, ApplicationConstantTest.ADMIN_COLLECTION_VALUE);
		e.setField(ApplicationConstant.SERVER_HOSTNAME, ApplicationConstantTest.SERVERHOSTNAME_VALUE);
		e.setField(ApplicationConstant.SERVER_IP, ApplicationConstantTest.SERVERIP_VALUE);
		e.setField(ApplicationConstant.SERVER_OS, ApplicationConstantTest.SERVEROS_VALUE);
		e.setField(ApplicationConstant.LOG_TYPE, ApplicationConstantTest.LOG_INFO_VALUE);
		e.setField(ApplicationConstant.USER_NAME, ApplicationConstantTest.USERNAME_VALUE);
		e.setField(ApplicationConstant.KEY_CORE, ApplicationConstantTest.KEY_CORE_VALUE);
		e.setField(ApplicationConstant.WEBAPP_FOR_FULLSQL, ApplicationConstantTest.WEBAPP_FOR_FULLSQL_VALUE);
		e.setField(ApplicationConstant.PATH_FOR_FULLSQL, ApplicationConstantTest.PATH_FOR_FULLSQL_VALUE);
		e.setField(ApplicationConstant.PARAMS, ApplicationConstantTest.PARAMS_VALUE);

		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);

		Assert.assertEquals(1, results.size());
		Assert.assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		Assert.assertEquals(1, matchListener.getMatchCount());
	}

	@Test
	public void DeleteCollectionTest() {

		final String qtp = "2022-05-18 10:58:06.126 INFO  (qtp527804008-16) [   ] o.a.s.s.HttpSolrCall [admin] webapp=null path=/admin/collections params={name=Test3&action=DELETE} status=0 QTime=325";
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("mes***REMOVED***ge", qtp);
		e.setField(ApplicationConstant.TIMESTAMP, ApplicationConstantTest.TIMESTAMP_VALUE);
		e.setField(ApplicationConstant.QUERY_STRING, ApplicationConstantTest.QUERYSTRING_DELETECOLLECTION_VALUE);
		e.setField(ApplicationConstant.CLASS, ApplicationConstantTest.HTTP_SOLR_CALL_VALUE);
		e.setField(ApplicationConstant.VERB, ApplicationConstantTest.ADMIN_COLLECTION_VALUE);
		e.setField(ApplicationConstant.SERVER_HOSTNAME, ApplicationConstantTest.SERVERHOSTNAME_VALUE);
		e.setField(ApplicationConstant.SERVER_IP, ApplicationConstantTest.SERVERIP_VALUE);
		e.setField(ApplicationConstant.SERVER_OS, ApplicationConstantTest.SERVEROS_VALUE);
		e.setField(ApplicationConstant.LOG_TYPE, ApplicationConstantTest.LOG_INFO_VALUE);
		e.setField(ApplicationConstant.USER_NAME, ApplicationConstantTest.USERNAME_VALUE);
		e.setField(ApplicationConstant.KEY_CORE, ApplicationConstantTest.KEY_CORE_VALUE);
		e.setField(ApplicationConstant.WEBAPP_FOR_FULLSQL, ApplicationConstantTest.WEBAPP_FOR_FULLSQL_VALUE);
		e.setField(ApplicationConstant.PATH_FOR_FULLSQL, ApplicationConstantTest.PATH_FOR_FULLSQL_VALUE);
		e.setField(ApplicationConstant.PARAMS, ApplicationConstantTest.PARAMS_VALUE);

		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);

		Assert.assertEquals(1, results.size());
		Assert.assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		Assert.assertEquals(1, matchListener.getMatchCount());
	}

	@Test
	public void parseQtpListCollectionTest() {

		final String qtp = "2022-05-19 16:04:32.563 INFO  (qtp464887938-17) [   ] o.a.s.s.HttpSolrCall [admin] webapp=null path=/admin/collections params={action=LIST} status=0 QTime=0";
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("mes***REMOVED***ge", qtp);
		e.setField(ApplicationConstant.TIMESTAMP, ApplicationConstantTest.TIMESTAMP_VALUE);
		e.setField(ApplicationConstant.QUERY_STRING, ApplicationConstantTest.QUERYSTRING_LISTCOLLECTION_VALUE);
		e.setField(ApplicationConstant.CLASS, ApplicationConstantTest.HTTP_SOLR_CALL_VALUE);
		e.setField(ApplicationConstant.VERB, ApplicationConstantTest.ADMIN_COLLECTION_VALUE);
		e.setField(ApplicationConstant.SERVER_HOSTNAME, ApplicationConstantTest.SERVERHOSTNAME_VALUE);
		e.setField(ApplicationConstant.SERVER_IP, ApplicationConstantTest.SERVERIP_VALUE);
		e.setField(ApplicationConstant.SERVER_OS, ApplicationConstantTest.SERVEROS_VALUE);
		e.setField(ApplicationConstant.LOG_TYPE, ApplicationConstantTest.LOG_INFO_VALUE);
		e.setField(ApplicationConstant.USER_NAME, ApplicationConstantTest.USERNAME_VALUE);
		e.setField(ApplicationConstant.KEY_CORE, ApplicationConstantTest.KEY_CORE_VALUE);
		e.setField(ApplicationConstant.WEBAPP_FOR_FULLSQL, ApplicationConstantTest.WEBAPP_FOR_FULLSQL_VALUE);
		e.setField(ApplicationConstant.PATH_FOR_FULLSQL, ApplicationConstantTest.PATH_FOR_FULLSQL_VALUE);
		e.setField(ApplicationConstant.PARAMS, ApplicationConstantTest.PARAMS_VALUE);
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);

		Assert.assertEquals(1, results.size());
		Assert.assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		Assert.assertEquals(1, matchListener.getMatchCount());
	}

	@Test
	public void parseQtpListSkipTest() {

		final String qtp = "2022-05-18 06:55:24.168 INFO  (qtp527804008-23) [   ] o.a.s.s.HttpSolrCall [admin] webapp=null path=/admin/collections params={action=LIST&wt=json&_=1652856928210} status=0 QTime=0";
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("mes***REMOVED***ge", qtp);
		e.setField(ApplicationConstant.TIMESTAMP, ApplicationConstantTest.TIMESTAMP_VALUE);
		e.setField(ApplicationConstant.QUERY_STRING, ApplicationConstantTest.QUERYSTRING_SKIP_LISTCOLLECTION_VALUE);
		e.setField(ApplicationConstant.CLASS, ApplicationConstantTest.HTTP_SOLR_CALL_VALUE);
		e.setField(ApplicationConstant.VERB, ApplicationConstantTest.ADMIN_COLLECTION_VALUE);
		e.setField(ApplicationConstant.SERVER_HOSTNAME, ApplicationConstantTest.SERVERHOSTNAME_VALUE);
		e.setField(ApplicationConstant.SERVER_IP, ApplicationConstantTest.SERVERIP_VALUE);
		e.setField(ApplicationConstant.SERVER_OS, ApplicationConstantTest.SERVEROS_VALUE);
		e.setField(ApplicationConstant.LOG_TYPE, ApplicationConstantTest.LOG_INFO_VALUE);
		e.setField(ApplicationConstant.USER_NAME, ApplicationConstantTest.USERNAME_VALUE);
		e.setField(ApplicationConstant.KEY_CORE, ApplicationConstantTest.KEY_CORE_VALUE);
		e.setField(ApplicationConstant.WEBAPP_FOR_FULLSQL, ApplicationConstantTest.WEBAPP_FOR_FULLSQL_VALUE);
		e.setField(ApplicationConstant.PATH_FOR_FULLSQL, ApplicationConstantTest.PATH_FOR_FULLSQL_VALUE);
		e.setField(ApplicationConstant.PARAMS, ApplicationConstantTest.PARAMS_VALUE);
		List<Event> ev = new ArrayList<Event>();
		ev.add(e);
		Collection<Event> results = filter.filter(ev, matchListener);
		Assert.assertEquals(0, results.size());
		Set<String> tags = new HashSet<>((ArrayList) e.getField("tags"));
		Assert.assertEquals(1, tags.size());
		Assert.assertTrue(tags.contains(LOGSTASH_TAG_SKIP_NOT_SOLR));
	}

	@Test
	public void parseQtpCollectionPropTest() {
		final String qtp = "2022-05-20 04:08:20.907 INFO  (qtp464887938-26) [   ] o.a.s.s.HttpSolrCall [admin] webapp=null path=/admin/collections params={propertyName=name&name=proptest&action=COLLECTIONPROP&propertyValue=testprop1} status=0 QTime=8";
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("mes***REMOVED***ge", qtp);
		e.setField(ApplicationConstant.TIMESTAMP, ApplicationConstantTest.TIMESTAMP_VALUE);
		e.setField(ApplicationConstant.QUERY_STRING, ApplicationConstantTest.QUERYSTRING_COLLECTION_PROP_VALUE);
		e.setField(ApplicationConstant.CLASS, ApplicationConstantTest.HTTP_SOLR_CALL_VALUE);
		e.setField(ApplicationConstant.VERB, ApplicationConstantTest.ADMIN_COLLECTION_VALUE);
		e.setField(ApplicationConstant.SERVER_HOSTNAME, ApplicationConstantTest.SERVERHOSTNAME_VALUE);
		e.setField(ApplicationConstant.SERVER_IP, ApplicationConstantTest.SERVERIP_VALUE);
		e.setField(ApplicationConstant.SERVER_OS, ApplicationConstantTest.SERVEROS_VALUE);
		e.setField(ApplicationConstant.LOG_TYPE, ApplicationConstantTest.LOG_INFO_VALUE);
		e.setField(ApplicationConstant.USER_NAME, ApplicationConstantTest.USERNAME_VALUE);
		e.setField(ApplicationConstant.COLLECTION, null);
		e.setField(ApplicationConstant.KEY_CORE, ApplicationConstantTest.KEY_CORE_VALUE);
		e.setField(ApplicationConstant.WEBAPP_FOR_FULLSQL, ApplicationConstantTest.WEBAPP_FOR_FULLSQL_VALUE);
		e.setField(ApplicationConstant.PATH_FOR_FULLSQL, ApplicationConstantTest.PATH_FOR_FULLSQL_VALUE);
		e.setField(ApplicationConstant.PARAMS, ApplicationConstantTest.PARAMS_VALUE);
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);

		Assert.assertEquals(1, results.size());
		Assert.assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		Assert.assertEquals(1, matchListener.getMatchCount());
	}

	@Test
	public void parseQtpCollectionStatusTest() {
		final String qtp = "2022-05-20 04:14:18.125 INFO  (qtp464887938-25) [c:proptest   ] o.a.s.s.HttpSolrCall [admin] webapp=null path=/admin/collections params={action=COLSTATUS&sizeInfo=true&collection=proptest&fieldInfo=true&coreInfo=true&segments=true} status=0 QTime=287";
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("mes***REMOVED***ge", qtp);
		e.setField(ApplicationConstant.TIMESTAMP, ApplicationConstantTest.TIMESTAMP_VALUE);
		e.setField(ApplicationConstant.QUERY_STRING, ApplicationConstantTest.QUERYSTRING_COLLECTION_STATUS_VALUE);
		e.setField(ApplicationConstant.CLASS, ApplicationConstantTest.HTTP_SOLR_CALL_VALUE);
		e.setField(ApplicationConstant.VERB, ApplicationConstantTest.ADMIN_COLLECTION_VALUE);
		e.setField(ApplicationConstant.SERVER_HOSTNAME, ApplicationConstantTest.SERVERHOSTNAME_VALUE);
		e.setField(ApplicationConstant.SERVER_IP, ApplicationConstantTest.SERVERIP_VALUE);
		e.setField(ApplicationConstant.SERVER_OS, ApplicationConstantTest.SERVEROS_VALUE);
		e.setField(ApplicationConstant.LOG_TYPE, ApplicationConstantTest.LOG_INFO_VALUE);
		e.setField(ApplicationConstant.USER_NAME, ApplicationConstantTest.USERNAME_VALUE);
		e.setField(ApplicationConstant.KEY_CORE, ApplicationConstantTest.KEY_CORE_VALUE);
		e.setField(ApplicationConstant.WEBAPP_FOR_FULLSQL, ApplicationConstantTest.WEBAPP_FOR_FULLSQL_VALUE);
		e.setField(ApplicationConstant.PATH_FOR_FULLSQL, ApplicationConstantTest.PATH_FOR_FULLSQL_VALUE);
		e.setField(ApplicationConstant.PARAMS, ApplicationConstantTest.PARAMS_VALUE);
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);

		Assert.assertEquals(1, results.size());
		Assert.assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		Assert.assertEquals(1, matchListener.getMatchCount());
	}

	@Test
	public void parseQtpMigrateTest() {
		final String qtp = "2022-05-20 04:14:18.125 INFO  (qtp464887938-25) [c:proptest   ] o.a.s.s.HttpSolrCall [admin] webapp=null path=/admin/collections params={action=COLSTATUS&sizeInfo=true&collection=proptest&fieldInfo=true&coreInfo=true&segments=true} status=0 QTime=287";
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("mes***REMOVED***ge", qtp);
		e.setField(ApplicationConstant.TIMESTAMP, ApplicationConstantTest.TIMESTAMP_VALUE);
		e.setField(ApplicationConstant.QUERY_STRING, ApplicationConstantTest.QUERYSTRING_COLLECTION_MIGRATE_VALUE);
		e.setField(ApplicationConstant.CLASS, ApplicationConstantTest.HTTP_SOLR_CALL_VALUE);
		e.setField(ApplicationConstant.VERB, ApplicationConstantTest.ADMIN_COLLECTION_VALUE);
		e.setField(ApplicationConstant.SERVER_HOSTNAME, ApplicationConstantTest.SERVERHOSTNAME_VALUE);
		e.setField(ApplicationConstant.SERVER_IP, ApplicationConstantTest.SERVERIP_VALUE);
		e.setField(ApplicationConstant.SERVER_OS, ApplicationConstantTest.SERVEROS_VALUE);
		e.setField(ApplicationConstant.LOG_TYPE, ApplicationConstantTest.LOG_INFO_VALUE);
		e.setField(ApplicationConstant.USER_NAME, ApplicationConstantTest.USERNAME_VALUE);
		e.setField(ApplicationConstant.KEY_CORE, ApplicationConstantTest.KEY_CORE_VALUE);
		e.setField(ApplicationConstant.WEBAPP_FOR_FULLSQL, ApplicationConstantTest.WEBAPP_FOR_FULLSQL_VALUE);
		e.setField(ApplicationConstant.PATH_FOR_FULLSQL, ApplicationConstantTest.PATH_FOR_FULLSQL_VALUE);
		e.setField(ApplicationConstant.PARAMS, ApplicationConstantTest.PARAMS_VALUE);
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);

		Assert.assertEquals(1, results.size());
		Assert.assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		Assert.assertEquals(1, matchListener.getMatchCount());
	}

	@Test
	public void parseQtpReIndexTest() {
		final String qtp = "2022-05-20 04:14:18.125 INFO  (qtp464887938-25) [c:proptest   ] o.a.s.s.HttpSolrCall [admin] webapp=null path=/admin/collections params={action=COLSTATUS&sizeInfo=true&collection=proptest&fieldInfo=true&coreInfo=true&segments=true} status=0 QTime=287";
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("mes***REMOVED***ge", qtp);
		e.setField(ApplicationConstant.TIMESTAMP, ApplicationConstantTest.TIMESTAMP_VALUE);
		e.setField(ApplicationConstant.QUERY_STRING, ApplicationConstantTest.QUERYSTRING_COLLECTION_REINDEX_VALUE);
		e.setField(ApplicationConstant.CLASS, ApplicationConstantTest.HTTP_SOLR_CALL_VALUE);
		e.setField(ApplicationConstant.VERB, ApplicationConstantTest.ADMIN_COLLECTION_VALUE);
		e.setField(ApplicationConstant.SERVER_HOSTNAME, ApplicationConstantTest.SERVERHOSTNAME_VALUE);
		e.setField(ApplicationConstant.SERVER_IP, ApplicationConstantTest.SERVERIP_VALUE);
		e.setField(ApplicationConstant.SERVER_OS, ApplicationConstantTest.SERVEROS_VALUE);
		e.setField(ApplicationConstant.LOG_TYPE, ApplicationConstantTest.LOG_INFO_VALUE);
		e.setField(ApplicationConstant.USER_NAME, ApplicationConstantTest.USERNAME_VALUE);
		e.setField(ApplicationConstant.KEY_CORE, ApplicationConstantTest.KEY_CORE_VALUE);
		e.setField(ApplicationConstant.WEBAPP_FOR_FULLSQL, ApplicationConstantTest.WEBAPP_FOR_FULLSQL_VALUE);
		e.setField(ApplicationConstant.PATH_FOR_FULLSQL, ApplicationConstantTest.PATH_FOR_FULLSQL_VALUE);
		e.setField(ApplicationConstant.PARAMS, ApplicationConstantTest.PARAMS_VALUE);
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);

		Assert.assertEquals(1, results.size());
		Assert.assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		Assert.assertEquals(1, matchListener.getMatchCount());
	}

	@Test
	public void parseQtpBackUpCollectionTest() {
		final String qtp = "2022-05-20 04:14:18.125 INFO  (qtp464887938-25) [c:proptest   ] o.a.s.s.HttpSolrCall [admin] webapp=null path=/admin/collections params={action=COLSTATUS&sizeInfo=true&collection=proptest&fieldInfo=true&coreInfo=true&segments=true} status=0 QTime=287";
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("mes***REMOVED***ge", qtp);
		e.setField(ApplicationConstant.TIMESTAMP, ApplicationConstantTest.TIMESTAMP_VALUE);
		e.setField(ApplicationConstant.QUERY_STRING, ApplicationConstantTest.QUERYSTRING_COLLECTION_BACKUP_VALUE);
		e.setField(ApplicationConstant.CLASS, ApplicationConstantTest.HTTP_SOLR_CALL_VALUE);
		e.setField(ApplicationConstant.VERB, ApplicationConstantTest.ADMIN_COLLECTION_VALUE);
		e.setField(ApplicationConstant.SERVER_HOSTNAME, ApplicationConstantTest.SERVERHOSTNAME_VALUE);
		e.setField(ApplicationConstant.SERVER_IP, ApplicationConstantTest.SERVERIP_VALUE);
		e.setField(ApplicationConstant.SERVER_OS, ApplicationConstantTest.SERVEROS_VALUE);
		e.setField(ApplicationConstant.LOG_TYPE, ApplicationConstantTest.LOG_INFO_VALUE);
		e.setField(ApplicationConstant.USER_NAME, ApplicationConstantTest.USERNAME_VALUE);
		e.setField(ApplicationConstant.KEY_CORE, ApplicationConstantTest.KEY_CORE_VALUE);
		e.setField(ApplicationConstant.WEBAPP_FOR_FULLSQL, ApplicationConstantTest.WEBAPP_FOR_FULLSQL_VALUE);
		e.setField(ApplicationConstant.PATH_FOR_FULLSQL, ApplicationConstantTest.PATH_FOR_FULLSQL_VALUE);
		e.setField(ApplicationConstant.PARAMS, ApplicationConstantTest.PARAMS_VALUE);
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);

		Assert.assertEquals(1, results.size());
		Assert.assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		Assert.assertEquals(1, matchListener.getMatchCount());
	}

	@Test
	public void parseQtpRestoreCollectionTest() {
		final String qtp = "2022-05-20 04:14:18.125 INFO  (qtp464887938-25) [c:proptest   ] o.a.s.s.HttpSolrCall [admin] webapp=null path=/admin/collections params={action=COLSTATUS&sizeInfo=true&collection=proptest&fieldInfo=true&coreInfo=true&segments=true} status=0 QTime=287";
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("mes***REMOVED***ge", qtp);
		e.setField(ApplicationConstant.TIMESTAMP, ApplicationConstantTest.TIMESTAMP_VALUE);
		e.setField(ApplicationConstant.QUERY_STRING, ApplicationConstantTest.QUERYSTRING_COLLECTION_RESTORE_VALUE);
		e.setField(ApplicationConstant.CLASS, ApplicationConstantTest.HTTP_SOLR_CALL_VALUE);
		e.setField(ApplicationConstant.VERB, ApplicationConstantTest.ADMIN_COLLECTION_VALUE);
		e.setField(ApplicationConstant.SERVER_HOSTNAME, ApplicationConstantTest.SERVERHOSTNAME_VALUE);
		e.setField(ApplicationConstant.SERVER_IP, ApplicationConstantTest.SERVERIP_VALUE);
		e.setField(ApplicationConstant.SERVER_OS, ApplicationConstantTest.SERVEROS_VALUE);
		e.setField(ApplicationConstant.LOG_TYPE, ApplicationConstantTest.LOG_INFO_VALUE);
		e.setField(ApplicationConstant.USER_NAME, ApplicationConstantTest.USERNAME_VALUE);
		e.setField(ApplicationConstant.KEY_CORE, ApplicationConstantTest.KEY_CORE_VALUE);
		e.setField(ApplicationConstant.WEBAPP_FOR_FULLSQL, ApplicationConstantTest.WEBAPP_FOR_FULLSQL_VALUE);
		e.setField(ApplicationConstant.PATH_FOR_FULLSQL, ApplicationConstantTest.PATH_FOR_FULLSQL_VALUE);
		e.setField(ApplicationConstant.PARAMS, ApplicationConstantTest.PARAMS_VALUE);
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);

		Assert.assertEquals(1, results.size());
		Assert.assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		Assert.assertEquals(1, matchListener.getMatchCount());
	}

	@Test
	public void parseQtpREBALANCELEADERSTest() {
		final String qtp = "2022-05-20 04:14:18.125 INFO  (qtp464887938-25) [c:proptest   ] o.a.s.s.HttpSolrCall [admin] webapp=null path=/admin/collections params={action=COLSTATUS&sizeInfo=true&collection=proptest&fieldInfo=true&coreInfo=true&segments=true} status=0 QTime=287";
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("mes***REMOVED***ge", qtp);
		e.setField(ApplicationConstant.TIMESTAMP, ApplicationConstantTest.TIMESTAMP_VALUE);
		e.setField(ApplicationConstant.QUERY_STRING,
				ApplicationConstantTest.QUERYSTRING_COLLECTION_REBALANCELEADERS_VALUE);
		e.setField(ApplicationConstant.CLASS, ApplicationConstantTest.HTTP_SOLR_CALL_VALUE);
		e.setField(ApplicationConstant.VERB, ApplicationConstantTest.ADMIN_COLLECTION_VALUE);
		e.setField(ApplicationConstant.SERVER_HOSTNAME, ApplicationConstantTest.SERVERHOSTNAME_VALUE);
		e.setField(ApplicationConstant.SERVER_IP, ApplicationConstantTest.SERVERIP_VALUE);
		e.setField(ApplicationConstant.SERVER_OS, ApplicationConstantTest.SERVEROS_VALUE);
		e.setField(ApplicationConstant.LOG_TYPE, ApplicationConstantTest.LOG_INFO_VALUE);
		e.setField(ApplicationConstant.USER_NAME, ApplicationConstantTest.USERNAME_VALUE);
		e.setField(ApplicationConstant.KEY_CORE, ApplicationConstantTest.KEY_CORE_VALUE);
		e.setField(ApplicationConstant.WEBAPP_FOR_FULLSQL, ApplicationConstantTest.WEBAPP_FOR_FULLSQL_VALUE);
		e.setField(ApplicationConstant.PATH_FOR_FULLSQL, ApplicationConstantTest.PATH_FOR_FULLSQL_VALUE);
		e.setField(ApplicationConstant.PARAMS, ApplicationConstantTest.PARAMS_VALUE);
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);

		Assert.assertEquals(1, results.size());
		Assert.assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		Assert.assertEquals(1, matchListener.getMatchCount());
	}

	@Test
	public void parseQtpModifyCollectionTest() {

		final String qtp = "2022-05-20 04:34:09.392 INFO  (qtp464887938-22) [c:proptest   ] o.a.s.s.HttpSolrCall [admin] webapp=null path=/admin/collections params={replicationFactor=1&action=MODIFYCOLLECTION&numShards=2&collection=proptest} status=0 QTime=132";
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("mes***REMOVED***ge", qtp);
		e.setField(ApplicationConstant.TIMESTAMP, ApplicationConstantTest.TIMESTAMP_VALUE);
		e.setField(ApplicationConstant.QUERY_STRING, ApplicationConstantTest.QUERYSTRING_MODIFY_COLLECTION_VALUE);
		e.setField(ApplicationConstant.CLASS, ApplicationConstantTest.HTTP_SOLR_CALL_VALUE);
		e.setField(ApplicationConstant.VERB, ApplicationConstantTest.ADMIN_COLLECTION_VALUE);
		e.setField(ApplicationConstant.SERVER_HOSTNAME, ApplicationConstantTest.SERVERHOSTNAME_VALUE);
		e.setField(ApplicationConstant.SERVER_IP, ApplicationConstantTest.SERVERIP_VALUE);
		e.setField(ApplicationConstant.SERVER_OS, ApplicationConstantTest.SERVEROS_VALUE);
		e.setField(ApplicationConstant.LOG_TYPE, ApplicationConstantTest.LOG_INFO_VALUE);
		e.setField(ApplicationConstant.USER_NAME, ApplicationConstantTest.USERNAME_VALUE);
		e.setField(ApplicationConstant.KEY_CORE, ApplicationConstantTest.KEY_CORE_VALUE);
		e.setField(ApplicationConstant.WEBAPP_FOR_FULLSQL, ApplicationConstantTest.WEBAPP_FOR_FULLSQL_VALUE);
		e.setField(ApplicationConstant.PATH_FOR_FULLSQL, ApplicationConstantTest.PATH_FOR_FULLSQL_VALUE);
		e.setField(ApplicationConstant.PARAMS, ApplicationConstantTest.PARAMS_VALUE);
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);

		Assert.assertEquals(1, results.size());
		Assert.assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		Assert.assertEquals(1, matchListener.getMatchCount());
	}

	@Test
	public void parseQtpReloadCollectionTest() {

		final String qtp = "2022-05-20 04:37:04.911 INFO  (qtp464887938-263) [   ] o.a.s.s.HttpSolrCall [admin] webapp=null path=/admin/collections params={name=proptest&action=RELOAD} status=0 QTime=2516";
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("mes***REMOVED***ge", qtp);
		e.setField(ApplicationConstant.TIMESTAMP, ApplicationConstantTest.TIMESTAMP_VALUE);
		e.setField(ApplicationConstant.QUERY_STRING, ApplicationConstantTest.QUERYSTRING_RELOAD_COLLECTION_VALUE);
		e.setField(ApplicationConstant.CLASS, ApplicationConstantTest.HTTP_SOLR_CALL_VALUE);
		e.setField(ApplicationConstant.VERB, ApplicationConstantTest.ADMIN_COLLECTION_VALUE);
		e.setField(ApplicationConstant.SERVER_HOSTNAME, ApplicationConstantTest.SERVERHOSTNAME_VALUE);
		e.setField(ApplicationConstant.SERVER_IP, ApplicationConstantTest.SERVERIP_VALUE);
		e.setField(ApplicationConstant.SERVER_OS, ApplicationConstantTest.SERVEROS_VALUE);
		e.setField(ApplicationConstant.LOG_TYPE, ApplicationConstantTest.LOG_INFO_VALUE);
		e.setField(ApplicationConstant.USER_NAME, ApplicationConstantTest.USERNAME_VALUE);
		e.setField(ApplicationConstant.KEY_CORE, ApplicationConstantTest.KEY_CORE_VALUE);
		e.setField(ApplicationConstant.WEBAPP_FOR_FULLSQL, ApplicationConstantTest.WEBAPP_FOR_FULLSQL_VALUE);
		e.setField(ApplicationConstant.PATH_FOR_FULLSQL, ApplicationConstantTest.PATH_FOR_FULLSQL_VALUE);
		e.setField(ApplicationConstant.PARAMS, ApplicationConstantTest.PARAMS_VALUE);
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);

		Assert.assertEquals(1, results.size());
		Assert.assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		Assert.assertEquals(1, matchListener.getMatchCount());
	}

	// Manage Shards-------
	@Test
	public void parseQtpSPLITSHARDTest() {

		final String qtp = "2022-05-23 08:59:46.112 INFO  (qtp1939990953-24) [c:demo   ] o.a.s.s.HttpSolrCall [admin] webapp=null path=/admin/collections params={action=SPLITSHARD&collection=demo&shard=shard1&wt=xml} status=0 QTime=7715";
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("mes***REMOVED***ge", qtp);
		e.setField(ApplicationConstant.TIMESTAMP, ApplicationConstantTest.TIMESTAMP_VALUE);
		e.setField(ApplicationConstant.QUERY_STRING, ApplicationConstantTest.QUERYSTRING_SPLIT_SHARD_VALUE);
		e.setField(ApplicationConstant.CLASS, ApplicationConstantTest.HTTP_SOLR_CALL_VALUE);
		e.setField(ApplicationConstant.VERB, ApplicationConstantTest.ADMIN_COLLECTION_VALUE);
		e.setField(ApplicationConstant.SERVER_HOSTNAME, ApplicationConstantTest.SERVERHOSTNAME_VALUE);
		e.setField(ApplicationConstant.SERVER_IP, ApplicationConstantTest.SERVERIP_VALUE);
		e.setField(ApplicationConstant.SERVER_OS, ApplicationConstantTest.SERVEROS_VALUE);
		e.setField(ApplicationConstant.LOG_TYPE, ApplicationConstantTest.LOG_INFO_VALUE);
		e.setField(ApplicationConstant.USER_NAME, ApplicationConstantTest.USERNAME_VALUE);
		e.setField(ApplicationConstant.KEY_CORE, ApplicationConstantTest.KEY_CORE_VALUE);
		e.setField(ApplicationConstant.WEBAPP_FOR_FULLSQL, ApplicationConstantTest.WEBAPP_FOR_FULLSQL_VALUE);
		e.setField(ApplicationConstant.PATH_FOR_FULLSQL, ApplicationConstantTest.PATH_FOR_FULLSQL_VALUE);
		e.setField(ApplicationConstant.PARAMS, ApplicationConstantTest.PARAMS_VALUE);
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);

		Assert.assertEquals(1, results.size());
		Assert.assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		Assert.assertEquals(1, matchListener.getMatchCount());
	}

	@Test
	public void parseQtpDELETESHARDTest() {

		final String qtp = "2022-05-23 09:30:20.360 INFO  (qtp1939990953-19) [c:demo   ] o.a.s.s.HttpSolrCall [admin] webapp=null path=/admin/collections params={action=DELETESHARD&collection=demo&shard=shard1&wt=xml} status=0 QTime=108";
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("mes***REMOVED***ge", qtp);
		e.setField(ApplicationConstant.TIMESTAMP, ApplicationConstantTest.TIMESTAMP_VALUE);
		e.setField(ApplicationConstant.QUERY_STRING, ApplicationConstantTest.QUERYSTRING_DELETE_SHARD_VALUE);
		e.setField(ApplicationConstant.CLASS, ApplicationConstantTest.HTTP_SOLR_CALL_VALUE);
		e.setField(ApplicationConstant.VERB, ApplicationConstantTest.ADMIN_COLLECTION_VALUE);
		e.setField(ApplicationConstant.SERVER_HOSTNAME, ApplicationConstantTest.SERVERHOSTNAME_VALUE);
		e.setField(ApplicationConstant.SERVER_IP, ApplicationConstantTest.SERVERIP_VALUE);
		e.setField(ApplicationConstant.SERVER_OS, ApplicationConstantTest.SERVEROS_VALUE);
		e.setField(ApplicationConstant.LOG_TYPE, ApplicationConstantTest.LOG_INFO_VALUE);
		e.setField(ApplicationConstant.USER_NAME, ApplicationConstantTest.USERNAME_VALUE);
		e.setField(ApplicationConstant.KEY_CORE, ApplicationConstantTest.KEY_CORE_VALUE);
		e.setField(ApplicationConstant.WEBAPP_FOR_FULLSQL, ApplicationConstantTest.WEBAPP_FOR_FULLSQL_VALUE);
		e.setField(ApplicationConstant.PATH_FOR_FULLSQL, ApplicationConstantTest.PATH_FOR_FULLSQL_VALUE);
		e.setField(ApplicationConstant.PARAMS, ApplicationConstantTest.PARAMS_VALUE);
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);

		Assert.assertEquals(1, results.size());
		Assert.assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		Assert.assertEquals(1, matchListener.getMatchCount());
	}

	@Test
	public void parseQtpFORCELEADERTest() {

		final String qtp = "2022-05-24 13:46:31.022 INFO  (qtp1939990953-306) [c:demo1   ] o.a.s.s.HttpSolrCall [admin] webapp=null path=/admin/collections params={action=FORCELEADER&collection=demo1&shard=shard1} status=0 QTime=210";
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("mes***REMOVED***ge", qtp);
		e.setField(ApplicationConstant.TIMESTAMP, ApplicationConstantTest.TIMESTAMP_VALUE);
		e.setField(ApplicationConstant.QUERY_STRING, ApplicationConstantTest.QUERYSTRING_FORCE_LEADER_VALUE);
		e.setField(ApplicationConstant.CLASS, ApplicationConstantTest.HTTP_SOLR_CALL_VALUE);
		e.setField(ApplicationConstant.VERB, ApplicationConstantTest.ADMIN_COLLECTION_VALUE);
		e.setField(ApplicationConstant.SERVER_HOSTNAME, ApplicationConstantTest.SERVERHOSTNAME_VALUE);
		e.setField(ApplicationConstant.SERVER_IP, ApplicationConstantTest.SERVERIP_VALUE);
		e.setField(ApplicationConstant.SERVER_OS, ApplicationConstantTest.SERVEROS_VALUE);
		e.setField(ApplicationConstant.LOG_TYPE, ApplicationConstantTest.LOG_INFO_VALUE);
		e.setField(ApplicationConstant.USER_NAME, ApplicationConstantTest.USERNAME_VALUE);
		e.setField(ApplicationConstant.KEY_CORE, ApplicationConstantTest.KEY_CORE_VALUE);
		e.setField(ApplicationConstant.WEBAPP_FOR_FULLSQL, ApplicationConstantTest.WEBAPP_FOR_FULLSQL_VALUE);
		e.setField(ApplicationConstant.PATH_FOR_FULLSQL, ApplicationConstantTest.PATH_FOR_FULLSQL_VALUE);
		e.setField(ApplicationConstant.PARAMS, ApplicationConstantTest.PARAMS_VALUE);
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);

		Assert.assertEquals(1, results.size());
		Assert.assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		Assert.assertEquals(1, matchListener.getMatchCount());
	}

	@Test
	public void parseQtpCREATESHARDTest() {

		final String qtp = "2022-05-20 04:37:04.911 INFO  (qtp464887938-263) [   ] o.a.s.s.HttpSolrCall [admin] webapp=null path=/admin/collections params={name=proptest&action=RELOAD} status=0 QTime=2516";
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("mes***REMOVED***ge", qtp);
		e.setField(ApplicationConstant.TIMESTAMP, ApplicationConstantTest.TIMESTAMP_VALUE);
		e.setField(ApplicationConstant.QUERY_STRING, ApplicationConstantTest.QUERYSTRING_CREATE_SHARD_VALUE);
		e.setField(ApplicationConstant.CLASS, ApplicationConstantTest.HTTP_SOLR_CALL_VALUE);
		e.setField(ApplicationConstant.VERB, ApplicationConstantTest.ADMIN_COLLECTION_VALUE);
		e.setField(ApplicationConstant.SERVER_HOSTNAME, ApplicationConstantTest.SERVERHOSTNAME_VALUE);
		e.setField(ApplicationConstant.SERVER_IP, ApplicationConstantTest.SERVERIP_VALUE);
		e.setField(ApplicationConstant.SERVER_OS, ApplicationConstantTest.SERVEROS_VALUE);
		e.setField(ApplicationConstant.LOG_TYPE, ApplicationConstantTest.LOG_INFO_VALUE);
		e.setField(ApplicationConstant.USER_NAME, ApplicationConstantTest.USERNAME_VALUE);
		e.setField(ApplicationConstant.KEY_CORE, ApplicationConstantTest.KEY_CORE_VALUE);
		e.setField(ApplicationConstant.WEBAPP_FOR_FULLSQL, ApplicationConstantTest.WEBAPP_FOR_FULLSQL_VALUE);
		e.setField(ApplicationConstant.PATH_FOR_FULLSQL, ApplicationConstantTest.PATH_FOR_FULLSQL_VALUE);
		e.setField(ApplicationConstant.PARAMS, ApplicationConstantTest.PARAMS_VALUE);
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);

		Assert.assertEquals(1, results.size());
		Assert.assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		Assert.assertEquals(1, matchListener.getMatchCount());
	}

	@Test
	public void parseQtpREQUESTSTATUSTest() {

		final String qtp = "2022-05-31 06:55:50.613 INFO  (qtp464887938-1139) [   ] o.a.s.s.HttpSolrCall [admin] webapp=null path=/admin/collections params={requestid=1000&action=REQUESTSTATUS} status=0 QTime=4";
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("mes***REMOVED***ge", qtp);
		e.setField(ApplicationConstant.TIMESTAMP, ApplicationConstantTest.TIMESTAMP_VALUE);
		e.setField(ApplicationConstant.QUERY_STRING, ApplicationConstantTest.QUERYSTRING_REQUEST_STATUS_VALUE);
		e.setField(ApplicationConstant.CLASS, ApplicationConstantTest.HTTP_SOLR_CALL_VALUE);
		e.setField(ApplicationConstant.VERB, ApplicationConstantTest.ADMIN_COLLECTION_VALUE);
		e.setField(ApplicationConstant.SERVER_HOSTNAME, ApplicationConstantTest.SERVERHOSTNAME_VALUE);
		e.setField(ApplicationConstant.SERVER_IP, ApplicationConstantTest.SERVERIP_VALUE);
		e.setField(ApplicationConstant.SERVER_OS, ApplicationConstantTest.SERVEROS_VALUE);
		e.setField(ApplicationConstant.LOG_TYPE, ApplicationConstantTest.LOG_INFO_VALUE);
		e.setField(ApplicationConstant.USER_NAME, ApplicationConstantTest.USERNAME_VALUE);
		e.setField(ApplicationConstant.KEY_CORE, ApplicationConstantTest.KEY_CORE_VALUE);
		e.setField(ApplicationConstant.WEBAPP_FOR_FULLSQL, ApplicationConstantTest.WEBAPP_FOR_FULLSQL_VALUE);
		e.setField(ApplicationConstant.PATH_FOR_FULLSQL, ApplicationConstantTest.PATH_FOR_FULLSQL_VALUE);
		e.setField(ApplicationConstant.PARAMS, ApplicationConstantTest.PARAMS_VALUE);
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);

		Assert.assertEquals(1, results.size());
		Assert.assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		Assert.assertEquals(1, matchListener.getMatchCount());
	}

	// Manage Replica--------
	@Test
	public void parseQtpAddReplicaTest() {

		final String qtp = "2022-05-25 03:09:32.734 INFO  (qtp464887938-204) [c:films   ] o.a.s.s.HttpSolrCall [admin] webapp=null path=/admin/collections params={node=192.168.43.197:8983_solr&action=ADDREPLICA&collection=films&shard=shard1&wt=xml} status=0 QTime=2160";
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("mes***REMOVED***ge", qtp);
		e.setField(ApplicationConstant.TIMESTAMP, ApplicationConstantTest.TIMESTAMP_VALUE);
		e.setField(ApplicationConstant.QUERY_STRING, ApplicationConstantTest.QUERYSTRING_ADD_REPLICA_VALUE);
		e.setField(ApplicationConstant.CLASS, ApplicationConstantTest.HTTP_SOLR_CALL_VALUE);
		e.setField(ApplicationConstant.VERB, ApplicationConstantTest.ADMIN_COLLECTION_VALUE);
		e.setField(ApplicationConstant.SERVER_HOSTNAME, ApplicationConstantTest.SERVERHOSTNAME_VALUE);
		e.setField(ApplicationConstant.SERVER_IP, ApplicationConstantTest.SERVERIP_VALUE);
		e.setField(ApplicationConstant.SERVER_OS, ApplicationConstantTest.SERVEROS_VALUE);
		e.setField(ApplicationConstant.LOG_TYPE, ApplicationConstantTest.LOG_INFO_VALUE);
		e.setField(ApplicationConstant.USER_NAME, ApplicationConstantTest.USERNAME_VALUE);
		e.setField(ApplicationConstant.KEY_CORE, ApplicationConstantTest.KEY_CORE_VALUE);
		e.setField(ApplicationConstant.WEBAPP_FOR_FULLSQL, ApplicationConstantTest.WEBAPP_FOR_FULLSQL_VALUE);
		e.setField(ApplicationConstant.PATH_FOR_FULLSQL, ApplicationConstantTest.PATH_FOR_FULLSQL_VALUE);
		e.setField(ApplicationConstant.PARAMS, ApplicationConstantTest.PARAMS_VALUE);
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);

		Assert.assertEquals(1, results.size());
		Assert.assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		Assert.assertEquals(1, matchListener.getMatchCount());
	}

	@Test
	public void parseQtpMoveReplicaTest() {

		final String qtp = "2022-05-25 03:29:51.504 INFO  (qtp464887938-23) [c:films   ] o.a.s.s.HttpSolrCall [admin] webapp=null path=/admin/collections params={sourceNode=192.168.43.197:8983_solr&replica=core_node7&action=MOVEREPLICA&collection=films&shard=shard1&targetNode=192.168.43.197:8983_solr} status=0 QTime=1861";
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("mes***REMOVED***ge", qtp);
		e.setField(ApplicationConstant.TIMESTAMP, ApplicationConstantTest.TIMESTAMP_VALUE);
		e.setField(ApplicationConstant.QUERY_STRING, ApplicationConstantTest.QUERYSTRING_MOVE_REPLICA_VALUE);
		e.setField(ApplicationConstant.CLASS, ApplicationConstantTest.HTTP_SOLR_CALL_VALUE);
		e.setField(ApplicationConstant.VERB, ApplicationConstantTest.ADMIN_COLLECTION_VALUE);
		e.setField(ApplicationConstant.SERVER_HOSTNAME, ApplicationConstantTest.SERVERHOSTNAME_VALUE);
		e.setField(ApplicationConstant.SERVER_IP, ApplicationConstantTest.SERVERIP_VALUE);
		e.setField(ApplicationConstant.SERVER_OS, ApplicationConstantTest.SERVEROS_VALUE);
		e.setField(ApplicationConstant.LOG_TYPE, ApplicationConstantTest.LOG_INFO_VALUE);
		e.setField(ApplicationConstant.USER_NAME, ApplicationConstantTest.USERNAME_VALUE);
		e.setField(ApplicationConstant.KEY_CORE, ApplicationConstantTest.KEY_CORE_VALUE);
		e.setField(ApplicationConstant.WEBAPP_FOR_FULLSQL, ApplicationConstantTest.WEBAPP_FOR_FULLSQL_VALUE);
		e.setField(ApplicationConstant.PATH_FOR_FULLSQL, ApplicationConstantTest.PATH_FOR_FULLSQL_VALUE);
		e.setField(ApplicationConstant.PARAMS, ApplicationConstantTest.PARAMS_VALUE);
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);

		Assert.assertEquals(1, results.size());
		Assert.assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		Assert.assertEquals(1, matchListener.getMatchCount());
	}

	@Test
	public void parseQtpDeleteReplicaTest() {

		final String qtp = "2022-05-23 09:26:13.658 INFO  (qtp1939990953-23) [c:demo   ] o.a.s.s.HttpSolrCall [admin] webapp=null path=/admin/collections params={replica=core_node2&action=DELETEREPLICA&collection=demo&shard=shard1&wt=xml} status=0 QTime=496";
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("mes***REMOVED***ge", qtp);
		e.setField(ApplicationConstant.TIMESTAMP, ApplicationConstantTest.TIMESTAMP_VALUE);
		e.setField(ApplicationConstant.QUERY_STRING, ApplicationConstantTest.QUERYSTRING_DELETE_REPLICA_VALUE);
		e.setField(ApplicationConstant.CLASS, ApplicationConstantTest.HTTP_SOLR_CALL_VALUE);
		e.setField(ApplicationConstant.VERB, ApplicationConstantTest.ADMIN_COLLECTION_VALUE);
		e.setField(ApplicationConstant.SERVER_HOSTNAME, ApplicationConstantTest.SERVERHOSTNAME_VALUE);
		e.setField(ApplicationConstant.SERVER_IP, ApplicationConstantTest.SERVERIP_VALUE);
		e.setField(ApplicationConstant.SERVER_OS, ApplicationConstantTest.SERVEROS_VALUE);
		e.setField(ApplicationConstant.LOG_TYPE, ApplicationConstantTest.LOG_INFO_VALUE);
		e.setField(ApplicationConstant.USER_NAME, ApplicationConstantTest.USERNAME_VALUE);
		e.setField(ApplicationConstant.KEY_CORE, ApplicationConstantTest.KEY_CORE_VALUE);
		e.setField(ApplicationConstant.WEBAPP_FOR_FULLSQL, ApplicationConstantTest.WEBAPP_FOR_FULLSQL_VALUE);
		e.setField(ApplicationConstant.PATH_FOR_FULLSQL, ApplicationConstantTest.PATH_FOR_FULLSQL_VALUE);
		e.setField(ApplicationConstant.PARAMS, ApplicationConstantTest.PARAMS_VALUE);
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);

		Assert.assertEquals(1, results.size());
		Assert.assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		Assert.assertEquals(1, matchListener.getMatchCount());
	}

	@Test
	public void parseQtpAddReplicaPropTest() {

		final String qtp = "2022-05-25 03:35:09.780 INFO  (qtp464887938-171) [c:films   ] o.a.s.s.HttpSolrCall [admin] webapp=null path=/admin/collections params={replica=core_node2&property.value=special&property=special&action=ADDREPLICAPROP&collection=films&shard=shard1} status=0 QTime=90";
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("mes***REMOVED***ge", qtp);
		e.setField(ApplicationConstant.TIMESTAMP, ApplicationConstantTest.TIMESTAMP_VALUE);
		e.setField(ApplicationConstant.QUERY_STRING, ApplicationConstantTest.QUERYSTRING_ADD_REPLICAPROP_VALUE);
		e.setField(ApplicationConstant.CLASS, ApplicationConstantTest.HTTP_SOLR_CALL_VALUE);
		e.setField(ApplicationConstant.VERB, ApplicationConstantTest.ADMIN_COLLECTION_VALUE);
		e.setField(ApplicationConstant.SERVER_HOSTNAME, ApplicationConstantTest.SERVERHOSTNAME_VALUE);
		e.setField(ApplicationConstant.SERVER_IP, ApplicationConstantTest.SERVERIP_VALUE);
		e.setField(ApplicationConstant.SERVER_OS, ApplicationConstantTest.SERVEROS_VALUE);
		e.setField(ApplicationConstant.LOG_TYPE, ApplicationConstantTest.LOG_INFO_VALUE);
		e.setField(ApplicationConstant.USER_NAME, ApplicationConstantTest.USERNAME_VALUE);
		e.setField(ApplicationConstant.KEY_CORE, ApplicationConstantTest.KEY_CORE_VALUE);
		e.setField(ApplicationConstant.WEBAPP_FOR_FULLSQL, ApplicationConstantTest.WEBAPP_FOR_FULLSQL_VALUE);
		e.setField(ApplicationConstant.PATH_FOR_FULLSQL, ApplicationConstantTest.PATH_FOR_FULLSQL_VALUE);
		e.setField(ApplicationConstant.PARAMS, ApplicationConstantTest.PARAMS_VALUE);
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);

		Assert.assertEquals(1, results.size());
		Assert.assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		Assert.assertEquals(1, matchListener.getMatchCount());
	}

	@Test
	public void parseQtpDeleteReplicaPropTest() {
		final String qtp = "2022-05-25 03:40:30.171 INFO  (qtp464887938-23) [c:films   ] o.a.s.s.HttpSolrCall [admin] webapp=null path=/admin/collections params={replica=core_node2&property=preferredLeader&action=DELETEREPLICAPROP&shard=shard1&collection=films&wt=xml} status=0 QTime=51";
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("mes***REMOVED***ge", qtp);
		e.setField(ApplicationConstant.TIMESTAMP, ApplicationConstantTest.TIMESTAMP_VALUE);
		e.setField(ApplicationConstant.QUERY_STRING, ApplicationConstantTest.QUERYSTRING_DELETE_REPLICAPROP_VALUE);
		e.setField(ApplicationConstant.CLASS, ApplicationConstantTest.HTTP_SOLR_CALL_VALUE);
		e.setField(ApplicationConstant.VERB, ApplicationConstantTest.ADMIN_COLLECTION_VALUE);
		e.setField(ApplicationConstant.SERVER_HOSTNAME, ApplicationConstantTest.SERVERHOSTNAME_VALUE);
		e.setField(ApplicationConstant.SERVER_IP, ApplicationConstantTest.SERVERIP_VALUE);
		e.setField(ApplicationConstant.SERVER_OS, ApplicationConstantTest.SERVEROS_VALUE);
		e.setField(ApplicationConstant.LOG_TYPE, ApplicationConstantTest.LOG_INFO_VALUE);
		e.setField(ApplicationConstant.USER_NAME, ApplicationConstantTest.USERNAME_VALUE);
		e.setField(ApplicationConstant.KEY_CORE, ApplicationConstantTest.KEY_CORE_VALUE);
		e.setField(ApplicationConstant.WEBAPP_FOR_FULLSQL, ApplicationConstantTest.WEBAPP_FOR_FULLSQL_VALUE);
		e.setField(ApplicationConstant.PATH_FOR_FULLSQL, ApplicationConstantTest.PATH_FOR_FULLSQL_VALUE);
		e.setField(ApplicationConstant.PARAMS, ApplicationConstantTest.PARAMS_VALUE);
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);

		Assert.assertEquals(1, results.size());
		Assert.assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		Assert.assertEquals(1, matchListener.getMatchCount());
	}

	// Manage Alias--------
	@Test
	public void parseQtpCreateAliasTest() {
		final String qtp = "2022-05-23 09:53:16.443 INFO  (qtp1939990953-22) [   ] o.a.s.s.HttpSolrCall [admin] webapp=null path=/admin/collections params={collections=demo,demo1&name=testalias&action=CREATEALIAS&wt=xml} status=0 QTime=195";
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("mes***REMOVED***ge", qtp);
		e.setField(ApplicationConstant.TIMESTAMP, ApplicationConstantTest.TIMESTAMP_VALUE);
		e.setField(ApplicationConstant.QUERY_STRING, ApplicationConstantTest.QUERYSTRING_CREATE_ALIAS_VALUE);
		e.setField(ApplicationConstant.CLASS, ApplicationConstantTest.HTTP_SOLR_CALL_VALUE);
		e.setField(ApplicationConstant.VERB, ApplicationConstantTest.ADMIN_COLLECTION_VALUE);
		e.setField(ApplicationConstant.SERVER_HOSTNAME, ApplicationConstantTest.SERVERHOSTNAME_VALUE);
		e.setField(ApplicationConstant.SERVER_IP, ApplicationConstantTest.SERVERIP_VALUE);
		e.setField(ApplicationConstant.SERVER_OS, ApplicationConstantTest.SERVEROS_VALUE);
		e.setField(ApplicationConstant.LOG_TYPE, ApplicationConstantTest.LOG_INFO_VALUE);
		e.setField(ApplicationConstant.USER_NAME, ApplicationConstantTest.USERNAME_VALUE);
		e.setField(ApplicationConstant.KEY_CORE, ApplicationConstantTest.KEY_CORE_VALUE);
		e.setField(ApplicationConstant.WEBAPP_FOR_FULLSQL, ApplicationConstantTest.WEBAPP_FOR_FULLSQL_VALUE);
		e.setField(ApplicationConstant.PATH_FOR_FULLSQL, ApplicationConstantTest.PATH_FOR_FULLSQL_VALUE);
		e.setField(ApplicationConstant.PARAMS, ApplicationConstantTest.PARAMS_VALUE);
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);

		Assert.assertEquals(1, results.size());
		Assert.assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		Assert.assertEquals(1, matchListener.getMatchCount());
	}

	@Test
	public void parseQtpListAliasTest() {
		final String qtp = "2022-05-23 09:57:36.136 INFO  (qtp1939990953-113) [   ] o.a.s.s.HttpSolrCall [admin] webapp=null path=/admin/collections params={action=LISTALIASES&wt=xml} status=0 QTime=14";
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("mes***REMOVED***ge", qtp);
		e.setField(ApplicationConstant.TIMESTAMP, ApplicationConstantTest.TIMESTAMP_VALUE);
		e.setField(ApplicationConstant.QUERY_STRING, ApplicationConstantTest.QUERYSTRING_LIST_ALIAS_VALUE);
		e.setField(ApplicationConstant.CLASS, ApplicationConstantTest.HTTP_SOLR_CALL_VALUE);
		e.setField(ApplicationConstant.VERB, ApplicationConstantTest.ADMIN_COLLECTION_VALUE);
		e.setField(ApplicationConstant.SERVER_HOSTNAME, ApplicationConstantTest.SERVERHOSTNAME_VALUE);
		e.setField(ApplicationConstant.SERVER_IP, ApplicationConstantTest.SERVERIP_VALUE);
		e.setField(ApplicationConstant.SERVER_OS, ApplicationConstantTest.SERVEROS_VALUE);
		e.setField(ApplicationConstant.LOG_TYPE, ApplicationConstantTest.LOG_INFO_VALUE);
		e.setField(ApplicationConstant.USER_NAME, ApplicationConstantTest.USERNAME_VALUE);
		e.setField(ApplicationConstant.KEY_CORE, ApplicationConstantTest.KEY_CORE_VALUE);
		e.setField(ApplicationConstant.WEBAPP_FOR_FULLSQL, ApplicationConstantTest.WEBAPP_FOR_FULLSQL_VALUE);
		e.setField(ApplicationConstant.PATH_FOR_FULLSQL, ApplicationConstantTest.PATH_FOR_FULLSQL_VALUE);
		e.setField(ApplicationConstant.PARAMS, ApplicationConstantTest.PARAMS_VALUE);
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);

		Assert.assertEquals(1, results.size());
		Assert.assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		Assert.assertEquals(1, matchListener.getMatchCount());
	}

	@Test
	public void parseQtpSkipListAliasTest() {
		final String qtp = "2022-05-23 09:57:36.136 INFO  (qtp1939990953-113) [   ] o.a.s.s.HttpSolrCall [admin] webapp=null path=/admin/collections params={action=LISTALIASES&wt=xml} status=0 QTime=14";
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("mes***REMOVED***ge", qtp);
		e.setField(ApplicationConstant.TIMESTAMP, ApplicationConstantTest.TIMESTAMP_VALUE);
		e.setField(ApplicationConstant.QUERY_STRING, ApplicationConstantTest.QUERYSTRING_SKIP_LIST_ALIAS_VALUE);
		e.setField(ApplicationConstant.CLASS, ApplicationConstantTest.HTTP_SOLR_CALL_VALUE);
		e.setField(ApplicationConstant.VERB, ApplicationConstantTest.ADMIN_COLLECTION_VALUE);
		e.setField(ApplicationConstant.SERVER_HOSTNAME, ApplicationConstantTest.SERVERHOSTNAME_VALUE);
		e.setField(ApplicationConstant.SERVER_IP, ApplicationConstantTest.SERVERIP_VALUE);
		e.setField(ApplicationConstant.SERVER_OS, ApplicationConstantTest.SERVEROS_VALUE);
		e.setField(ApplicationConstant.LOG_TYPE, ApplicationConstantTest.LOG_INFO_VALUE);
		e.setField(ApplicationConstant.USER_NAME, ApplicationConstantTest.USERNAME_VALUE);
		e.setField(ApplicationConstant.KEY_CORE, ApplicationConstantTest.KEY_CORE_VALUE);
		e.setField(ApplicationConstant.WEBAPP_FOR_FULLSQL, ApplicationConstantTest.WEBAPP_FOR_FULLSQL_VALUE);
		e.setField(ApplicationConstant.PATH_FOR_FULLSQL, ApplicationConstantTest.PATH_FOR_FULLSQL_VALUE);
		e.setField(ApplicationConstant.PARAMS, ApplicationConstantTest.PARAMS_VALUE);
		List<Event> ev = new ArrayList<Event>();
		ev.add(e);
		Collection<Event> results = filter.filter(ev, matchListener);
		Assert.assertEquals(0, results.size());
		Set<String> tags = new HashSet<>((ArrayList) e.getField("tags"));
		Assert.assertEquals(1, tags.size());
		Assert.assertTrue(tags.contains(LOGSTASH_TAG_SKIP_NOT_SOLR));
	}

	@Test
	public void parseQtpAliasPropTest() {
		final String qtp = "2022-05-23 10:03:43.969 INFO  (qtp1939990953-113) [   ] o.a.s.s.HttpSolrCall [admin] webapp=null path=/admin/collections params={name=testalias&action=ALIASPROP&property.otherKey=otherValue&wt=xml&property.someKey=someValue} status=0 QTime=237";
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("mes***REMOVED***ge", qtp);
		e.setField(ApplicationConstant.TIMESTAMP, ApplicationConstantTest.TIMESTAMP_VALUE);
		e.setField(ApplicationConstant.QUERY_STRING, ApplicationConstantTest.QUERYSTRING_ALIAS_PROP_VALUE);
		e.setField(ApplicationConstant.CLASS, ApplicationConstantTest.HTTP_SOLR_CALL_VALUE);
		e.setField(ApplicationConstant.VERB, ApplicationConstantTest.ADMIN_COLLECTION_VALUE);
		e.setField(ApplicationConstant.SERVER_HOSTNAME, ApplicationConstantTest.SERVERHOSTNAME_VALUE);
		e.setField(ApplicationConstant.SERVER_IP, ApplicationConstantTest.SERVERIP_VALUE);
		e.setField(ApplicationConstant.SERVER_OS, ApplicationConstantTest.SERVEROS_VALUE);
		e.setField(ApplicationConstant.LOG_TYPE, ApplicationConstantTest.LOG_INFO_VALUE);
		e.setField(ApplicationConstant.USER_NAME, ApplicationConstantTest.USERNAME_VALUE);
		e.setField(ApplicationConstant.KEY_CORE, ApplicationConstantTest.KEY_CORE_VALUE);
		e.setField(ApplicationConstant.WEBAPP_FOR_FULLSQL, ApplicationConstantTest.WEBAPP_FOR_FULLSQL_VALUE);
		e.setField(ApplicationConstant.PATH_FOR_FULLSQL, ApplicationConstantTest.PATH_FOR_FULLSQL_VALUE);
		e.setField(ApplicationConstant.PARAMS, ApplicationConstantTest.PARAMS_VALUE);
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);

		Assert.assertEquals(1, results.size());
		Assert.assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		Assert.assertEquals(1, matchListener.getMatchCount());
	}

	@Test
	public void parseQtpDeleteAliasTest() {
		final String qtp = "2022-05-25 03:43:01.555 INFO  (qtp464887938-204) [   ] o.a.s.s.HttpSolrCall [admin] webapp=null path=/admin/collections params={name=testalias&action=DELETEALIAS&wt=xml} status=0 QTime=63";
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("mes***REMOVED***ge", qtp);
		e.setField(ApplicationConstant.TIMESTAMP, ApplicationConstantTest.TIMESTAMP_VALUE);
		e.setField(ApplicationConstant.QUERY_STRING, ApplicationConstantTest.QUERYSTRING_DELETE_ALIAS_VALUE);
		e.setField(ApplicationConstant.CLASS, ApplicationConstantTest.HTTP_SOLR_CALL_VALUE);
		e.setField(ApplicationConstant.VERB, ApplicationConstantTest.ADMIN_COLLECTION_VALUE);
		e.setField(ApplicationConstant.SERVER_HOSTNAME, ApplicationConstantTest.SERVERHOSTNAME_VALUE);
		e.setField(ApplicationConstant.SERVER_IP, ApplicationConstantTest.SERVERIP_VALUE);
		e.setField(ApplicationConstant.SERVER_OS, ApplicationConstantTest.SERVEROS_VALUE);
		e.setField(ApplicationConstant.LOG_TYPE, ApplicationConstantTest.LOG_INFO_VALUE);
		e.setField(ApplicationConstant.USER_NAME, ApplicationConstantTest.USERNAME_VALUE);
		e.setField(ApplicationConstant.KEY_CORE, ApplicationConstantTest.KEY_CORE_VALUE);
		e.setField(ApplicationConstant.WEBAPP_FOR_FULLSQL, ApplicationConstantTest.WEBAPP_FOR_FULLSQL_VALUE);
		e.setField(ApplicationConstant.PATH_FOR_FULLSQL, ApplicationConstantTest.PATH_FOR_FULLSQL_VALUE);
		e.setField(ApplicationConstant.PARAMS, ApplicationConstantTest.PARAMS_VALUE);
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);

		Assert.assertEquals(1, results.size());
		Assert.assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		Assert.assertEquals(1, matchListener.getMatchCount());
	}

	// Manage Cluster and node-----

	@Test
	public void parseQtpClusterStatusTest() {
		final String qtp = "2022-05-21 06:30:04.518 INFO  (qtp464887938-688) [   ] o.a.s.s.HttpSolrCall [admin] webapp=null path=/admin/collections params={action=CLUSTERSTATUS} status=0 QTime=41";
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("mes***REMOVED***ge", qtp);
		e.setField(ApplicationConstant.TIMESTAMP, ApplicationConstantTest.TIMESTAMP_VALUE);
		e.setField(ApplicationConstant.QUERY_STRING, ApplicationConstantTest.QUERYSTRING_CLUSTER_STATUS_VALUE);
		e.setField(ApplicationConstant.CLASS, ApplicationConstantTest.HTTP_SOLR_CALL_VALUE);
		e.setField(ApplicationConstant.VERB, ApplicationConstantTest.ADMIN_COLLECTION_VALUE);
		e.setField(ApplicationConstant.SERVER_HOSTNAME, ApplicationConstantTest.SERVERHOSTNAME_VALUE);
		e.setField(ApplicationConstant.SERVER_IP, ApplicationConstantTest.SERVERIP_VALUE);
		e.setField(ApplicationConstant.SERVER_OS, ApplicationConstantTest.SERVEROS_VALUE);
		e.setField(ApplicationConstant.LOG_TYPE, ApplicationConstantTest.LOG_INFO_VALUE);
		e.setField(ApplicationConstant.USER_NAME, ApplicationConstantTest.USERNAME_VALUE);
		e.setField(ApplicationConstant.KEY_CORE, ApplicationConstantTest.KEY_CORE_VALUE);
		e.setField(ApplicationConstant.WEBAPP_FOR_FULLSQL, ApplicationConstantTest.WEBAPP_FOR_FULLSQL_VALUE);
		e.setField(ApplicationConstant.PATH_FOR_FULLSQL, ApplicationConstantTest.PATH_FOR_FULLSQL_VALUE);
		e.setField(ApplicationConstant.PARAMS, ApplicationConstantTest.PARAMS_VALUE);
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);

		Assert.assertEquals(1, results.size());
		Assert.assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		Assert.assertEquals(1, matchListener.getMatchCount());
	}

	@Test
	public void parseQtpClusterPropTest() {
		final String qtp = "2022-05-23 04:09:06.029 INFO  (qtp464887938-903) [   ] o.a.s.s.HttpSolrCall [admin] webapp=null path=/admin/collections params={val=https&name=urlScheme&action=CLUSTERPROP&wt=xml} status=0 QTime=4";
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("mes***REMOVED***ge", qtp);
		e.setField(ApplicationConstant.TIMESTAMP, ApplicationConstantTest.TIMESTAMP_VALUE);
		e.setField(ApplicationConstant.QUERY_STRING, ApplicationConstantTest.QUERYSTRING_CLUSTER_PROP_VALUE);
		e.setField(ApplicationConstant.CLASS, ApplicationConstantTest.HTTP_SOLR_CALL_VALUE);
		e.setField(ApplicationConstant.VERB, ApplicationConstantTest.ADMIN_COLLECTION_VALUE);
		e.setField(ApplicationConstant.SERVER_HOSTNAME, ApplicationConstantTest.SERVERHOSTNAME_VALUE);
		e.setField(ApplicationConstant.SERVER_IP, ApplicationConstantTest.SERVERIP_VALUE);
		e.setField(ApplicationConstant.SERVER_OS, ApplicationConstantTest.SERVEROS_VALUE);
		e.setField(ApplicationConstant.LOG_TYPE, ApplicationConstantTest.LOG_INFO_VALUE);
		e.setField(ApplicationConstant.USER_NAME, ApplicationConstantTest.USERNAME_VALUE);
		e.setField(ApplicationConstant.KEY_CORE, ApplicationConstantTest.KEY_CORE_VALUE);
		e.setField(ApplicationConstant.WEBAPP_FOR_FULLSQL, ApplicationConstantTest.WEBAPP_FOR_FULLSQL_VALUE);
		e.setField(ApplicationConstant.PATH_FOR_FULLSQL, ApplicationConstantTest.PATH_FOR_FULLSQL_VALUE);
		e.setField(ApplicationConstant.PARAMS, ApplicationConstantTest.PARAMS_VALUE);
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);

		Assert.assertEquals(1, results.size());
		Assert.assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		Assert.assertEquals(1, matchListener.getMatchCount());
	}

	@Test
	public void parseQtpBalanceShardUniqueTest() {
		final String qtp = "2022-05-23 04:13:59.677 INFO  (qtp464887938-569) [c:proptest   ] o.a.s.s.HttpSolrCall [admin] webapp=null path=/admin/collections params={property=preferredLeader&action=BALANCESHARDUNIQUE&collection=proptest&wt=xml} status=0 QTime=168";
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("mes***REMOVED***ge", qtp);
		e.setField(ApplicationConstant.TIMESTAMP, ApplicationConstantTest.TIMESTAMP_VALUE);
		e.setField(ApplicationConstant.QUERY_STRING, ApplicationConstantTest.QUERYSTRING_BALANCE_SHARD_UNIQUE_VALUE);
		e.setField(ApplicationConstant.CLASS, ApplicationConstantTest.HTTP_SOLR_CALL_VALUE);
		e.setField(ApplicationConstant.VERB, ApplicationConstantTest.ADMIN_COLLECTION_VALUE);
		e.setField(ApplicationConstant.SERVER_HOSTNAME, ApplicationConstantTest.SERVERHOSTNAME_VALUE);
		e.setField(ApplicationConstant.SERVER_IP, ApplicationConstantTest.SERVERIP_VALUE);
		e.setField(ApplicationConstant.SERVER_OS, ApplicationConstantTest.SERVEROS_VALUE);
		e.setField(ApplicationConstant.LOG_TYPE, ApplicationConstantTest.LOG_INFO_VALUE);
		e.setField(ApplicationConstant.USER_NAME, ApplicationConstantTest.USERNAME_VALUE);
		e.setField(ApplicationConstant.KEY_CORE, ApplicationConstantTest.KEY_CORE_VALUE);
		e.setField(ApplicationConstant.WEBAPP_FOR_FULLSQL, ApplicationConstantTest.WEBAPP_FOR_FULLSQL_VALUE);
		e.setField(ApplicationConstant.PATH_FOR_FULLSQL, ApplicationConstantTest.PATH_FOR_FULLSQL_VALUE);
		e.setField(ApplicationConstant.PARAMS, ApplicationConstantTest.PARAMS_VALUE);
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);

		Assert.assertEquals(1, results.size());
		Assert.assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		Assert.assertEquals(1, matchListener.getMatchCount());
	}

	@Test
	public void parseQtpUtilizeNodeTest() {
		final String qtp = "2022-05-25 03:47:54.011 INFO  (qtp464887938-23) [   ] o.a.s.s.HttpSolrCall [admin] webapp=null path=/admin/collections params={node=192.168.43.197:8983_solr&action=UTILIZENODE} status=0 QTime=804";
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("mes***REMOVED***ge", qtp);
		e.setField(ApplicationConstant.TIMESTAMP, ApplicationConstantTest.TIMESTAMP_VALUE);
		e.setField(ApplicationConstant.QUERY_STRING, ApplicationConstantTest.QUERYSTRING_UTILIZE_NODE_VALUE);
		e.setField(ApplicationConstant.CLASS, ApplicationConstantTest.HTTP_SOLR_CALL_VALUE);
		e.setField(ApplicationConstant.VERB, ApplicationConstantTest.ADMIN_COLLECTION_VALUE);
		e.setField(ApplicationConstant.SERVER_HOSTNAME, ApplicationConstantTest.SERVERHOSTNAME_VALUE);
		e.setField(ApplicationConstant.SERVER_IP, ApplicationConstantTest.SERVERIP_VALUE);
		e.setField(ApplicationConstant.SERVER_OS, ApplicationConstantTest.SERVEROS_VALUE);
		e.setField(ApplicationConstant.LOG_TYPE, ApplicationConstantTest.LOG_INFO_VALUE);
		e.setField(ApplicationConstant.USER_NAME, ApplicationConstantTest.USERNAME_VALUE);
		e.setField(ApplicationConstant.KEY_CORE, ApplicationConstantTest.KEY_CORE_VALUE);
		e.setField(ApplicationConstant.WEBAPP_FOR_FULLSQL, ApplicationConstantTest.WEBAPP_FOR_FULLSQL_VALUE);
		e.setField(ApplicationConstant.PATH_FOR_FULLSQL, ApplicationConstantTest.PATH_FOR_FULLSQL_VALUE);
		e.setField(ApplicationConstant.PARAMS, ApplicationConstantTest.PARAMS_VALUE);
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);

		Assert.assertEquals(1, results.size());
		Assert.assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		Assert.assertEquals(1, matchListener.getMatchCount());
	}

	@Test
	public void parseQtpReplaceNodeTest() {
		final String qtp = "2022-05-25 03:49:42.686 INFO  (qtp464887938-201) [   ] o.a.s.s.HttpSolrCall [admin] webapp=null path=/admin/collections params={sourceNode=192.168.43.197:8983_solr&action=REPLACENODE&targetNode=192.168.43.197:8983_solr} status=0 QTime=105";
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("mes***REMOVED***ge", qtp);
		e.setField(ApplicationConstant.TIMESTAMP, ApplicationConstantTest.TIMESTAMP_VALUE);
		e.setField(ApplicationConstant.QUERY_STRING, ApplicationConstantTest.QUERYSTRING_REPLACE_NODE_VALUE);
		e.setField(ApplicationConstant.CLASS, ApplicationConstantTest.HTTP_SOLR_CALL_VALUE);
		e.setField(ApplicationConstant.VERB, ApplicationConstantTest.ADMIN_COLLECTION_VALUE);
		e.setField(ApplicationConstant.SERVER_HOSTNAME, ApplicationConstantTest.SERVERHOSTNAME_VALUE);
		e.setField(ApplicationConstant.SERVER_IP, ApplicationConstantTest.SERVERIP_VALUE);
		e.setField(ApplicationConstant.SERVER_OS, ApplicationConstantTest.SERVEROS_VALUE);
		e.setField(ApplicationConstant.LOG_TYPE, ApplicationConstantTest.LOG_INFO_VALUE);
		e.setField(ApplicationConstant.USER_NAME, ApplicationConstantTest.USERNAME_VALUE);
		e.setField(ApplicationConstant.KEY_CORE, ApplicationConstantTest.KEY_CORE_VALUE);
		e.setField(ApplicationConstant.WEBAPP_FOR_FULLSQL, ApplicationConstantTest.WEBAPP_FOR_FULLSQL_VALUE);
		e.setField(ApplicationConstant.PATH_FOR_FULLSQL, ApplicationConstantTest.PATH_FOR_FULLSQL_VALUE);
		e.setField(ApplicationConstant.PARAMS, ApplicationConstantTest.PARAMS_VALUE);
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);

		Assert.assertEquals(1, results.size());
		Assert.assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		Assert.assertEquals(1, matchListener.getMatchCount());
	}

	@Test
	public void parseQtpDeleteNodeTest() {
		final String qtp = "2022-05-25 03:54:56.755 INFO  (qtp464887938-204) [   ] o.a.s.s.HttpSolrCall [admin] webapp=null path=/admin/collections params={node=192.168.43.197:8983_solr&action=DELETENODE} status=0 QTime=50";
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("mes***REMOVED***ge", qtp);
		e.setField(ApplicationConstant.TIMESTAMP, ApplicationConstantTest.TIMESTAMP_VALUE);
		e.setField(ApplicationConstant.QUERY_STRING, ApplicationConstantTest.QUERYSTRING_DELETE_NODE_VALUE);
		e.setField(ApplicationConstant.CLASS, ApplicationConstantTest.HTTP_SOLR_CALL_VALUE);
		e.setField(ApplicationConstant.VERB, ApplicationConstantTest.ADMIN_COLLECTION_VALUE);
		e.setField(ApplicationConstant.SERVER_HOSTNAME, ApplicationConstantTest.SERVERHOSTNAME_VALUE);
		e.setField(ApplicationConstant.SERVER_IP, ApplicationConstantTest.SERVERIP_VALUE);
		e.setField(ApplicationConstant.SERVER_OS, ApplicationConstantTest.SERVEROS_VALUE);
		e.setField(ApplicationConstant.LOG_TYPE, ApplicationConstantTest.LOG_INFO_VALUE);
		e.setField(ApplicationConstant.USER_NAME, ApplicationConstantTest.USERNAME_VALUE);
		e.setField(ApplicationConstant.KEY_CORE, ApplicationConstantTest.KEY_CORE_VALUE);
		e.setField(ApplicationConstant.WEBAPP_FOR_FULLSQL, ApplicationConstantTest.WEBAPP_FOR_FULLSQL_VALUE);
		e.setField(ApplicationConstant.PATH_FOR_FULLSQL, ApplicationConstantTest.PATH_FOR_FULLSQL_VALUE);
		e.setField(ApplicationConstant.PARAMS, ApplicationConstantTest.PARAMS_VALUE);
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);

		Assert.assertEquals(1, results.size());
		Assert.assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		Assert.assertEquals(1, matchListener.getMatchCount());
	}

	@Test
	public void parseQtpRemoveRoleTest() {
		final String qtp = "2022-05-25 03:53:15.219 INFO  (qtp464887938-171) [   ] o.a.s.s.HttpSolrCall [admin] webapp=null path=/admin/collections params={node=192.168.43.197:8983_solr&role=overseer&action=REMOVEROLE} status=0 QTime=53";
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("mes***REMOVED***ge", qtp);
		e.setField(ApplicationConstant.TIMESTAMP, ApplicationConstantTest.TIMESTAMP_VALUE);
		e.setField(ApplicationConstant.QUERY_STRING, ApplicationConstantTest.QUERYSTRING_REMOVE_ROLE_VALUE);
		e.setField(ApplicationConstant.CLASS, ApplicationConstantTest.HTTP_SOLR_CALL_VALUE);
		e.setField(ApplicationConstant.VERB, ApplicationConstantTest.ADMIN_COLLECTION_VALUE);
		e.setField(ApplicationConstant.SERVER_HOSTNAME, ApplicationConstantTest.SERVERHOSTNAME_VALUE);
		e.setField(ApplicationConstant.SERVER_IP, ApplicationConstantTest.SERVERIP_VALUE);
		e.setField(ApplicationConstant.SERVER_OS, ApplicationConstantTest.SERVEROS_VALUE);
		e.setField(ApplicationConstant.LOG_TYPE, ApplicationConstantTest.LOG_INFO_VALUE);
		e.setField(ApplicationConstant.USER_NAME, ApplicationConstantTest.USERNAME_VALUE);
		e.setField(ApplicationConstant.KEY_CORE, ApplicationConstantTest.KEY_CORE_VALUE);
		e.setField(ApplicationConstant.WEBAPP_FOR_FULLSQL, ApplicationConstantTest.WEBAPP_FOR_FULLSQL_VALUE);
		e.setField(ApplicationConstant.PATH_FOR_FULLSQL, ApplicationConstantTest.PATH_FOR_FULLSQL_VALUE);
		e.setField(ApplicationConstant.PARAMS, ApplicationConstantTest.PARAMS_VALUE);
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);

		Assert.assertEquals(1, results.size());
		Assert.assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		Assert.assertEquals(1, matchListener.getMatchCount());
	}

	@Test
	public void parseQtpAddRoleTest() {
		final String qtp = "2022-05-23 04:40:29.923 INFO  (qtp464887938-1234) [   ] o.a.s.s.HttpSolrCall [admin] webapp=null path=/admin/collections params={node=node2&role=overseer&action=ADDROLE} status=0 QTime=61";
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("mes***REMOVED***ge", qtp);
		e.setField(ApplicationConstant.TIMESTAMP, ApplicationConstantTest.TIMESTAMP_VALUE);
		e.setField(ApplicationConstant.QUERY_STRING, ApplicationConstantTest.QUERYSTRING_ADD_ROLE_VALUE);
		e.setField(ApplicationConstant.CLASS, ApplicationConstantTest.HTTP_SOLR_CALL_VALUE);
		e.setField(ApplicationConstant.VERB, ApplicationConstantTest.ADMIN_COLLECTION_VALUE);
		e.setField(ApplicationConstant.SERVER_HOSTNAME, ApplicationConstantTest.SERVERHOSTNAME_VALUE);
		e.setField(ApplicationConstant.SERVER_IP, ApplicationConstantTest.SERVERIP_VALUE);
		e.setField(ApplicationConstant.SERVER_OS, ApplicationConstantTest.SERVEROS_VALUE);
		e.setField(ApplicationConstant.LOG_TYPE, ApplicationConstantTest.LOG_INFO_VALUE);
		e.setField(ApplicationConstant.USER_NAME, ApplicationConstantTest.USERNAME_VALUE);
		e.setField(ApplicationConstant.KEY_CORE, ApplicationConstantTest.KEY_CORE_VALUE);
		e.setField(ApplicationConstant.WEBAPP_FOR_FULLSQL, ApplicationConstantTest.WEBAPP_FOR_FULLSQL_VALUE);
		e.setField(ApplicationConstant.PATH_FOR_FULLSQL, ApplicationConstantTest.PATH_FOR_FULLSQL_VALUE);
		e.setField(ApplicationConstant.PARAMS, ApplicationConstantTest.PARAMS_VALUE);
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);

		Assert.assertEquals(1, results.size());
		Assert.assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		Assert.assertEquals(1, matchListener.getMatchCount());
	}

	@Test
	public void parseQtpOVERSEERSTATUSTest() {
		final String qtp = "2022-05-23 04:42:52.826 INFO  (qtp464887938-903) [   ] o.a.s.s.HttpSolrCall [admin] webapp=null path=/admin/collections params={action=OVERSEERSTATUS} status=0 QTime=163";
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("mes***REMOVED***ge", qtp);
		e.setField(ApplicationConstant.TIMESTAMP, ApplicationConstantTest.TIMESTAMP_VALUE);
		e.setField(ApplicationConstant.QUERY_STRING, ApplicationConstantTest.QUERYSTRING_OVERSEERSTATUS_VALUE);
		e.setField(ApplicationConstant.CLASS, ApplicationConstantTest.HTTP_SOLR_CALL_VALUE);
		e.setField(ApplicationConstant.VERB, ApplicationConstantTest.ADMIN_COLLECTION_VALUE);
		e.setField(ApplicationConstant.SERVER_HOSTNAME, ApplicationConstantTest.SERVERHOSTNAME_VALUE);
		e.setField(ApplicationConstant.SERVER_IP, ApplicationConstantTest.SERVERIP_VALUE);
		e.setField(ApplicationConstant.SERVER_OS, ApplicationConstantTest.SERVEROS_VALUE);
		e.setField(ApplicationConstant.LOG_TYPE, ApplicationConstantTest.LOG_INFO_VALUE);
		e.setField(ApplicationConstant.USER_NAME, ApplicationConstantTest.USERNAME_VALUE);
		e.setField(ApplicationConstant.KEY_CORE, ApplicationConstantTest.KEY_CORE_VALUE);
		e.setField(ApplicationConstant.WEBAPP_FOR_FULLSQL, ApplicationConstantTest.WEBAPP_FOR_FULLSQL_VALUE);
		e.setField(ApplicationConstant.PATH_FOR_FULLSQL, ApplicationConstantTest.PATH_FOR_FULLSQL_VALUE);
		e.setField(ApplicationConstant.PARAMS, ApplicationConstantTest.PARAMS_VALUE);
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);

		Assert.assertEquals(1, results.size());
		Assert.assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		Assert.assertEquals(1, matchListener.getMatchCount());
	}

	@Test
	public void parseQtpMIGRATESTATEFORMATTest() {
		final String qtp = "2022-05-23 07:08:11.865 INFO  (qtp464887938-1231) [c:proptest   ] o.a.s.s.HttpSolrCall [admin] webapp=null path=/admin/collections params={action=MIGRATESTATEFORMAT&collection=proptest} status=0 QTime=107";
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("mes***REMOVED***ge", qtp);
		e.setField(ApplicationConstant.TIMESTAMP, ApplicationConstantTest.TIMESTAMP_VALUE);
		e.setField(ApplicationConstant.QUERY_STRING, ApplicationConstantTest.QUERYSTRING_MIGRATESTATEFORMAT_VALUE);
		e.setField(ApplicationConstant.CLASS, ApplicationConstantTest.HTTP_SOLR_CALL_VALUE);
		e.setField(ApplicationConstant.VERB, ApplicationConstantTest.ADMIN_COLLECTION_VALUE);
		e.setField(ApplicationConstant.SERVER_HOSTNAME, ApplicationConstantTest.SERVERHOSTNAME_VALUE);
		e.setField(ApplicationConstant.SERVER_IP, ApplicationConstantTest.SERVERIP_VALUE);
		e.setField(ApplicationConstant.SERVER_OS, ApplicationConstantTest.SERVEROS_VALUE);
		e.setField(ApplicationConstant.LOG_TYPE, ApplicationConstantTest.LOG_INFO_VALUE);
		e.setField(ApplicationConstant.USER_NAME, ApplicationConstantTest.USERNAME_VALUE);
		e.setField(ApplicationConstant.KEY_CORE, ApplicationConstantTest.KEY_CORE_VALUE);
		e.setField(ApplicationConstant.WEBAPP_FOR_FULLSQL, ApplicationConstantTest.WEBAPP_FOR_FULLSQL_VALUE);
		e.setField(ApplicationConstant.PATH_FOR_FULLSQL, ApplicationConstantTest.PATH_FOR_FULLSQL_VALUE);
		e.setField(ApplicationConstant.PARAMS, ApplicationConstantTest.PARAMS_VALUE);
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);

		Assert.assertEquals(1, results.size());
		Assert.assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		Assert.assertEquals(1, matchListener.getMatchCount());
	}

	@Test
	public void parseQtpStatusCoreTest() {
		final String qtp = "2022-06-08 05:35:27.907 INFO  (qtp2005169944-26) [   ] o.a.s.s.HttpSolrCall [admin] webapp=null path=/admin/cores params={core=Product&action=STATUS} status=0 QTime=70";
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("mes***REMOVED***ge", qtp);
		e.setField(ApplicationConstant.TIMESTAMP, ApplicationConstantTest.TIMESTAMP_VALUE);
		e.setField(ApplicationConstant.QUERY_STRING, ApplicationConstantTest.QUERYSTRING_CORE_STATUS_VALUE);
		e.setField(ApplicationConstant.CLASS, ApplicationConstantTest.HTTP_SOLR_CALL_VALUE);
		e.setField(ApplicationConstant.VERB, ApplicationConstantTest.ADMIN_COLLECTION_VALUE);
		e.setField(ApplicationConstant.SERVER_HOSTNAME, ApplicationConstantTest.SERVERHOSTNAME_VALUE);
		e.setField(ApplicationConstant.SERVER_IP, ApplicationConstantTest.SERVERIP_VALUE);
		e.setField(ApplicationConstant.SERVER_OS, ApplicationConstantTest.SERVEROS_VALUE);
		e.setField(ApplicationConstant.LOG_TYPE, ApplicationConstantTest.LOG_INFO_VALUE);
		e.setField(ApplicationConstant.USER_NAME, ApplicationConstantTest.USERNAME_VALUE);
		e.setField(ApplicationConstant.KEY_CORE, ApplicationConstantTest.KEY_CORE_VALUE);
		e.setField(ApplicationConstant.WEBAPP_FOR_FULLSQL, ApplicationConstantTest.WEBAPP_FOR_FULLSQL_VALUE);
		e.setField(ApplicationConstant.PATH_FOR_FULLSQL, ApplicationConstantTest.PATH_FOR_FULLSQL_VALUE);
		e.setField(ApplicationConstant.PARAMS, ApplicationConstantTest.PARAMS_VALUE);
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);

		Assert.assertEquals(1, results.size());
		Assert.assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		Assert.assertEquals(1, matchListener.getMatchCount());
	}

	@Test
	public void parseQtpSplitCoreTest() {
		final String qtp = "2022-06-08 05:42:41.420 INFO  (qtp2005169944-56) [   ] o.a.s.s.HttpSolrCall [admin] webapp=null path=/admin/cores params={core=testnew&action=SPLIT&targetCore=second_collection} status=0 QTime=493";
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("mes***REMOVED***ge", qtp);
		e.setField(ApplicationConstant.TIMESTAMP, ApplicationConstantTest.TIMESTAMP_VALUE);
		e.setField(ApplicationConstant.QUERY_STRING, ApplicationConstantTest.QUERYSTRING_SPLIT_CORE_VALUE);
		e.setField(ApplicationConstant.CLASS, ApplicationConstantTest.HTTP_SOLR_CALL_VALUE);
		e.setField(ApplicationConstant.VERB, ApplicationConstantTest.ADMIN_COLLECTION_VALUE);
		e.setField(ApplicationConstant.SERVER_HOSTNAME, ApplicationConstantTest.SERVERHOSTNAME_VALUE);
		e.setField(ApplicationConstant.SERVER_IP, ApplicationConstantTest.SERVERIP_VALUE);
		e.setField(ApplicationConstant.SERVER_OS, ApplicationConstantTest.SERVEROS_VALUE);
		e.setField(ApplicationConstant.LOG_TYPE, ApplicationConstantTest.LOG_INFO_VALUE);
		e.setField(ApplicationConstant.USER_NAME, ApplicationConstantTest.USERNAME_VALUE);
		e.setField(ApplicationConstant.KEY_CORE, ApplicationConstantTest.KEY_CORE_VALUE);
		e.setField(ApplicationConstant.WEBAPP_FOR_FULLSQL, ApplicationConstantTest.WEBAPP_FOR_FULLSQL_VALUE);
		e.setField(ApplicationConstant.PATH_FOR_FULLSQL, ApplicationConstantTest.PATH_FOR_FULLSQL_VALUE);
		e.setField(ApplicationConstant.PARAMS, ApplicationConstantTest.PARAMS_VALUE);
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);

		Assert.assertEquals(1, results.size());
		Assert.assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		Assert.assertEquals(1, matchListener.getMatchCount());
	}

	@Test
	public void parseQtpSWAPCoretest() {
		final String qtp = "2022-06-03 11:27:38.004 INFO  (qtp2005169944-19) [   ] o.a.s.s.HttpSolrCall [admin] webapp=null path=/admin/cores params={core=testnew&other=TestCore&action=SWAP} status=0 QTime=207";
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("mes***REMOVED***ge", qtp);
		e.setField(ApplicationConstant.TIMESTAMP, ApplicationConstantTest.TIMESTAMP_VALUE);
		e.setField(ApplicationConstant.QUERY_STRING, ApplicationConstantTest.QUERYSTRING_SWAP_CORE_VALUE);
		e.setField(ApplicationConstant.CLASS, ApplicationConstantTest.HTTP_SOLR_CALL_VALUE);
		e.setField(ApplicationConstant.VERB, ApplicationConstantTest.ADMIN_COLLECTION_VALUE);
		e.setField(ApplicationConstant.SERVER_HOSTNAME, ApplicationConstantTest.SERVERHOSTNAME_VALUE);
		e.setField(ApplicationConstant.SERVER_IP, ApplicationConstantTest.SERVERIP_VALUE);
		e.setField(ApplicationConstant.SERVER_OS, ApplicationConstantTest.SERVEROS_VALUE);
		e.setField(ApplicationConstant.LOG_TYPE, ApplicationConstantTest.LOG_INFO_VALUE);
		e.setField(ApplicationConstant.USER_NAME, ApplicationConstantTest.USERNAME_VALUE);
		e.setField(ApplicationConstant.KEY_CORE, ApplicationConstantTest.KEY_CORE_VALUE);
		e.setField(ApplicationConstant.WEBAPP_FOR_FULLSQL, ApplicationConstantTest.WEBAPP_FOR_FULLSQL_VALUE);
		e.setField(ApplicationConstant.PATH_FOR_FULLSQL, ApplicationConstantTest.PATH_FOR_FULLSQL_VALUE);
		e.setField(ApplicationConstant.PARAMS, ApplicationConstantTest.PARAMS_VALUE);
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);

		Assert.assertEquals(1, results.size());
		Assert.assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		Assert.assertEquals(1, matchListener.getMatchCount());
	}

	@Test
	public void parseQtpSpelltest() {
		final String qtp = "2022-06-13 05:59:17.750 INFO  (qtp527804008-22) [c:firstcoll s:shard1 r:core_node2 x:firstcoll_shard1_replica_n1] o.a.s.c.S.Request [firstcoll_shard1_replica_n1]  webapp=/solr path=/spell params={df=text&spellcheck.q=dell+ultra+sharp&spellcheck.collateParam.q.op=AND&spellcheck=true&wt=xml} hits=0 status=0 QTime=10";
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("mes***REMOVED***ge", qtp);
		e.setField(ApplicationConstant.TIMESTAMP, ApplicationConstantTest.TIMESTAMP_VALUE);
		e.setField(ApplicationConstant.QUERY_STRING, ApplicationConstantTest.QUERYSTRING_SPELL_VALUE);
		e.setField(ApplicationConstant.CLASS, ApplicationConstantTest.SOLR_QTP_REQUEST_VALUE);
		e.setField(ApplicationConstant.COLLECTION, ApplicationConstantTest.COLLECTION_SHARD_VALUE);
		e.setField(ApplicationConstant.VERB, ApplicationConstant.SPELL);
		e.setField(ApplicationConstant.SERVER_HOSTNAME, ApplicationConstantTest.SERVERHOSTNAME_VALUE);
		e.setField(ApplicationConstant.SERVER_IP, ApplicationConstantTest.SERVERIP_VALUE);
		e.setField(ApplicationConstant.SERVER_OS, ApplicationConstantTest.SERVEROS_VALUE);
		e.setField(ApplicationConstant.LOG_TYPE, ApplicationConstantTest.LOG_INFO_VALUE);
		e.setField(ApplicationConstant.USER_NAME, ApplicationConstantTest.USERNAME_VALUE);
		e.setField(ApplicationConstant.KEY_CORE, ApplicationConstantTest.KEY_CORE_VALUE);
		e.setField(ApplicationConstant.WEBAPP_FOR_FULLSQL, ApplicationConstantTest.WEBAPP_FOR_FULLSQL_VALUE);
		e.setField(ApplicationConstant.PATH_FOR_FULLSQL, ApplicationConstantTest.PATH_FOR_FULLSQL_VALUE);
		e.setField(ApplicationConstant.PARAMS, ApplicationConstantTest.PARAMS_VALUE);
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);

		Assert.assertEquals(1, results.size());
		Assert.assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		Assert.assertEquals(1, matchListener.getMatchCount());
	}

	// check for info logs in case of error event
	@Test
	public void parseInfoQtpErrorTest() {

		final String qtp = "2022-02-09 06:44:21.089 INFO  (qtp234250762-19) [   x:demo_collection] o.a.s.u.p.LogUpdateProcessorFactory [demo_collection]  webapp=/solr path=/update params={commitWithin=1000&overwrite=true&wt=json&_=1644230869164}{} 0 0";

		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("mes***REMOVED***ge", qtp);
		e.setField(ApplicationConstant.TIMESTAMP, ApplicationConstantTest.TIMESTAMP_VALUE);
		e.setField(ApplicationConstant.QUERY_STRING, ApplicationConstantTest.QUERYSTRING_VALUE);
		e.setField(ApplicationConstant.COLLECTION, ApplicationConstantTest.COLLECTION_VALUE);
		e.setField(ApplicationConstant.SERVER_HOSTNAME, ApplicationConstantTest.SERVERHOSTNAME_VALUE);
		e.setField(ApplicationConstant.SERVER_IP, ApplicationConstantTest.SERVERIP_VALUE);
		e.setField(ApplicationConstant.SERVER_OS, ApplicationConstantTest.SERVEROS_VALUE);
		e.setField(ApplicationConstant.LOG_TYPE, ApplicationConstantTest.LOG_INFO_VALUE);
		e.setField(ApplicationConstant.KEY_CORE, ApplicationConstantTest.KEY_CORE_VALUE);
		e.setField(ApplicationConstant.WEBAPP_FOR_FULLSQL, ApplicationConstantTest.WEBAPP_FOR_FULLSQL_VALUE);
		e.setField(ApplicationConstant.PATH_FOR_FULLSQL, ApplicationConstantTest.PATH_FOR_FULLSQL_VALUE);
		e.setField(ApplicationConstant.PARAMS, ApplicationConstantTest.PARAMS_VALUE);
		List<Event> ev = new ArrayList<Event>();
		ev.add(e);
		Collection<Event> results = filter.filter(ev, matchListener);
		Assert.assertEquals(0, results.size());
		Set<String> tags = new HashSet<>((ArrayList) e.getField("tags"));
		Assert.assertEquals(1, tags.size());
		Assert.assertTrue(tags.contains(LOGSTASH_TAG_SKIP_NOT_SOLR));
	}

	// for qtp error logs

	@Test
	public void parseRecordQtpErrorTest() {

		final String qtp = "2022-02-08 08:07:10.210 ERROR (qtp2005169944-46) [   x:testnew] o.a.s.h.RequestHandlerBase org.apache.solr.common.SolrException: Cannot parse provided JSON: Expected key,value separator ':': char=;,position=7 AFTER='[{ \"id\";' BEFORE='yyashi, \"name\":\"ashi\"}]'";

		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("mes***REMOVED***ge", qtp);
		e.setField(ApplicationConstant.TIMESTAMP, ApplicationConstantTest.TIMESTAMP_VALUE);
		e.setField(ApplicationConstant.ERROR_MSG,ApplicationConstantTest.LEXICAL_ERRORMSG_VALUE);
		e.setField(ApplicationConstant.COLLECTION, ApplicationConstantTest.COLLECTION_VALUE);
		e.setField(ApplicationConstant.SERVER_HOSTNAME, ApplicationConstantTest.SERVERHOSTNAME_VALUE);
		e.setField(ApplicationConstant.SERVER_IP, ApplicationConstantTest.SERVERIP_VALUE);
		e.setField(ApplicationConstant.SERVER_OS, ApplicationConstantTest.SERVEROS_VALUE);
		e.setField(ApplicationConstant.LOG_TYPE, ApplicationConstantTest.LOG_ERROR_VALUE);
		e.setField(ApplicationConstant.KEY_CORE, ApplicationConstantTest.KEY_CORE_VALUE);
		e.setField(ApplicationConstant.WEBAPP_FOR_FULLSQL, ApplicationConstantTest.WEBAPP_FOR_FULLSQL_VALUE);
		e.setField(ApplicationConstant.PATH_FOR_FULLSQL, ApplicationConstantTest.PATH_FOR_FULLSQL_VALUE);
		e.setField(ApplicationConstant.PARAMS, ApplicationConstantTest.PARAMS_VALUE);

		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);

		Assert.assertEquals(1, results.size());
		Assert.assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		Assert.assertEquals(1, matchListener.getMatchCount());
	}

	// qtp error exception
	@Test
	public void parseRecordQtpErrorExceptionTest() {

		final String qtp = "2022-02-08 08:07:10.210 ERROR (qtp2005169944-46) [   x:testnew] o.a.s.h.RequestHandlerBase org.apache.solr.common.SolrException: Cannot parse provided JSON: Expected key,value separator ':': char=;,position=7 AFTER='[{ \"id\";' BEFORE='yyashi, \"name\":\"ashi\"}]'";

		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("mes***REMOVED***ge", qtp);
		e.setField(ApplicationConstant.TIMESTAMP, ApplicationConstantTest.TIMESTAMP_VALUE);
		e.setField(ApplicationConstant.COLLECTION, ApplicationConstantTest.COLLECTION_VALUE);
		e.setField(ApplicationConstant.SERVER_HOSTNAME, ApplicationConstantTest.SERVERHOSTNAME_VALUE);
		e.setField(ApplicationConstant.SERVER_IP, ApplicationConstantTest.SERVERIP_VALUE);
		e.setField(ApplicationConstant.SERVER_OS, ApplicationConstantTest.SERVEROS_VALUE);
		e.setField(ApplicationConstant.LOG_TYPE, ApplicationConstantTest.LOG_ERROR_VALUE);
		e.setField(ApplicationConstant.KEY_CORE, ApplicationConstantTest.KEY_CORE_VALUE);
		e.setField(ApplicationConstant.WEBAPP_FOR_FULLSQL, ApplicationConstantTest.WEBAPP_FOR_FULLSQL_VALUE);
		e.setField(ApplicationConstant.PATH_FOR_FULLSQL, ApplicationConstantTest.PATH_FOR_FULLSQL_VALUE);
		e.setField(ApplicationConstant.ERROR_MSG, "o.a.s.h.RequestHandlerBase org.apache.solr.common.SolrException: Cannot parse provided JSON: Expected key,value separator ':': char=;,position=7 AFTER='[{ \\\"id\\\";' BEFORE='yyashi, \\\"name\\\":\\\"ashi\\\"}]'");
		
		e.setField(ApplicationConstant.KEY_CORE, ApplicationConstantTest.KEY_CORE_VALUE);
		e.setField(ApplicationConstant.WEBAPP_FOR_FULLSQL, ApplicationConstantTest.WEBAPP_FOR_FULLSQL_VALUE);
		e.setField(ApplicationConstant.PATH_FOR_FULLSQL, ApplicationConstantTest.PATH_FOR_FULLSQL_VALUE);
		e.setField(ApplicationConstant.PARAMS, ApplicationConstantTest.PARAMS_VALUE);

		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);

		Assert.assertEquals(1, results.size());
		Assert.assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		Assert.assertEquals(1, matchListener.getMatchCount());
	}

	@Test
	public void parseQtpSqlErrorTest() {

		final String qtp = "2022-06-10 13:26:51.659 ERROR (qtp464887938-261) [c:new_collection s:shard1 r:core_node5 x:new_collection_shard1_replica_n2] o.a.s.c.s.i.s.ExceptionStream java.io.IOException: Failed to execute sqlQuery 'Select name,title from new_collection limit 1000' against JDBC connection 'jdbc:calcitesolr:'.";
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("mes***REMOVED***ge", qtp);
		e.setField(ApplicationConstant.TIMESTAMP, ApplicationConstantTest.TIMESTAMP_VALUE);
		e.setField(ApplicationConstant.ERROR_MSG, ApplicationConstantTest.EXCEPTIONSTREAM_VALUE);
		e.setField(ApplicationConstant.COLLECTION, ApplicationConstantTest.COLLECTION_VALUE);
		e.setField(ApplicationConstant.SERVER_HOSTNAME, ApplicationConstantTest.SERVERHOSTNAME_VALUE);
		e.setField(ApplicationConstant.SERVER_IP, ApplicationConstantTest.SERVERIP_VALUE);
		e.setField(ApplicationConstant.SERVER_OS, ApplicationConstantTest.SERVEROS_VALUE);
		e.setField(ApplicationConstant.LOG_TYPE, ApplicationConstantTest.LOG_ERROR_VALUE);
		e.setField(ApplicationConstant.KEY_CORE, ApplicationConstantTest.KEY_CORE_VALUE);
		e.setField(ApplicationConstant.WEBAPP_FOR_FULLSQL, ApplicationConstantTest.WEBAPP_FOR_FULLSQL_VALUE);
		e.setField(ApplicationConstant.PATH_FOR_FULLSQL, ApplicationConstantTest.PATH_FOR_FULLSQL_VALUE);
		e.setField(ApplicationConstant.PARAMS, ApplicationConstantTest.PARAMS_VALUE);

		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);

		Assert.assertEquals(1, results.size());
		Assert.assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		Assert.assertEquals(1, matchListener.getMatchCount());
	}

	// while adding document
	@Test
	public void parseRecordQtpDocumentTest() {

		final String qtp = "2022-02-08 07:30:09.930 INFO  (qtp2005169944-26) [   x:testnew] o.a.s.u.p.LogUpdateProcessorFactory [testnew]  webapp=/solr path=/update params={commitWithin=1000&overwrite=true&wt=json&_=1643965367564}{add=[yashi (1724179189492350976)]} 0 30";
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("mes***REMOVED***ge", qtp);
		e.setField(ApplicationConstant.TIMESTAMP, ApplicationConstantTest.TIMESTAMP_VALUE);
		e.setField(ApplicationConstant.QUERY_STRING, ApplicationConstantTest.QUERYSTRING_ADD_VALUE);
		e.setField(ApplicationConstant.COLLECTION, ApplicationConstantTest.COLLECTION_VALUE);
		e.setField(ApplicationConstant.VERB, ApplicationConstantTest.PATH_UPDATE_VALUE);
		e.setField(ApplicationConstant.SERVER_HOSTNAME, ApplicationConstantTest.SERVERHOSTNAME_VALUE);
		e.setField(ApplicationConstant.SERVER_IP, ApplicationConstantTest.SERVERIP_VALUE);
		e.setField(ApplicationConstant.SERVER_OS, ApplicationConstantTest.SERVEROS_VALUE);
		e.setField(ApplicationConstant.LOG_TYPE, ApplicationConstantTest.LOG_INFO_VALUE);
		e.setField(ApplicationConstant.CLASS, ApplicationConstantTest.UPDATE_CLASS_VALUE);
		e.setField(ApplicationConstant.USER_NAME, ApplicationConstantTest.USERNAME_VALUE);
		e.setField(ApplicationConstant.WEB_APP, ApplicationConstantTest.WEBAPP_VALUE);

		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);

		Assert.assertEquals(1, results.size());
		Assert.assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		Assert.assertEquals(1, matchListener.getMatchCount());
	}

	@Test
	public void parseQtpRecordTestPost2() {
		final String qtp = "2022-02-08 07:30:09.930 INFO  (qtp2005169944-26) [c:techproducts s:shard2 r:core_node8 x:techproducts_shard2_replica_n6] o.a.s.u.p.LogUpdateProcessorFactory [techproducts_shard2_replica_n6]  webapp=/solr path=/update params={}{add=[adata, apple, asus, ati, belkin, canon, cor***REMOVED***ir, dell, maxtor, ***REMOVED***msung, ... (11 adds)]} 0 30";

		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("mes***REMOVED***ge", qtp);
		e.setField(ApplicationConstant.TIMESTAMP, ApplicationConstantTest.TIMESTAMP_VALUE);
		e.setField(ApplicationConstant.QUERY_STRING, ApplicationConstantTest.QUERYSTRING_SHARD_ADD_VALUE);
		e.setField(ApplicationConstant.COLLECTION, ApplicationConstantTest.COLLECTION_SHARD_VALUE);
		e.setField(ApplicationConstant.VERB, ApplicationConstantTest.PATH_UPDATE_VALUE);
		e.setField(ApplicationConstant.SERVER_HOSTNAME, ApplicationConstantTest.SERVERHOSTNAME_VALUE);
		e.setField(ApplicationConstant.SERVER_IP, ApplicationConstantTest.SERVERIP_VALUE);
		e.setField(ApplicationConstant.SERVER_OS, ApplicationConstantTest.SERVEROS_VALUE);
		e.setField(ApplicationConstant.LOG_TYPE, ApplicationConstantTest.LOG_INFO_VALUE);
		e.setField(ApplicationConstant.CLASS, ApplicationConstantTest.UPDATE_CLASS_VALUE);
		e.setField(ApplicationConstant.USER_NAME, ApplicationConstantTest.USERNAME_VALUE);
		e.setField(ApplicationConstant.WEB_APP, ApplicationConstantTest.WEBAPP_VALUE);
		
		e.setField(ApplicationConstant.KEY_CORE, ApplicationConstantTest.KEY_CORE_VALUE);
		e.setField(ApplicationConstant.WEBAPP_FOR_FULLSQL, ApplicationConstantTest.WEBAPP_FOR_FULLSQL_VALUE);
		e.setField(ApplicationConstant.PATH_FOR_FULLSQL, ApplicationConstantTest.PATH_FOR_FULLSQL_VALUE);
		e.setField(ApplicationConstant.PARAMS, ApplicationConstantTest.PARAMS_VALUE);
		
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);

		Assert.assertEquals(1, results.size());
		Assert.assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		Assert.assertEquals(1, matchListener.getMatchCount());
	}

	@Test
	public void parseQtpRenameCollection() {
		final String qtp = "2022-05-13 08:46:23.290 INFO  (qtp527804008-21) [   ] o.a.s.s.HttpSolrCall [admin] webapp=null path=/admin/collections params={name=films&action=RENAME&target=rfilms} status=0 QTime=16";
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("mes***REMOVED***ge", qtp);
		e.setField(ApplicationConstant.TIMESTAMP, ApplicationConstantTest.TIMESTAMP_VALUE);
		e.setField(ApplicationConstant.QUERY_STRING, ApplicationConstantTest.QUERYSTRING_COLLECTIONUPDATE_VALUE);
		e.setField(ApplicationConstant.CLASS, ApplicationConstantTest.HTTP_SOLR_CALL_VALUE);
		e.setField(ApplicationConstant.VERB, ApplicationConstantTest.PATH_RENAME_VALUE);
		e.setField(ApplicationConstant.SERVER_HOSTNAME, ApplicationConstantTest.SERVERHOSTNAME_VALUE);
		e.setField(ApplicationConstant.SERVER_IP, ApplicationConstantTest.SERVERIP_VALUE);
		e.setField(ApplicationConstant.SERVER_OS, ApplicationConstantTest.SERVEROS_VALUE);
		e.setField(ApplicationConstant.LOG_TYPE, ApplicationConstantTest.LOG_INFO_VALUE);
		e.setField(ApplicationConstant.CLASS, ApplicationConstantTest.HTTP_SOLR_CALL_VALUE);
		e.setField(ApplicationConstant.USER_NAME, ApplicationConstantTest.USERNAME_VALUE);
		e.setField(ApplicationConstant.WEB_APP, ApplicationConstantTest.WEBAPP_VALUE_NULL);

		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);

		Assert.assertEquals(1, results.size());
		Assert.assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		Assert.assertEquals(1, matchListener.getMatchCount());
	}

	@Test
	public void parseQtpErrorRecordCollectionTest() {
		final String qtp = "2022-05-19 12:03:09.995 ERROR (OverseerThreadFactory-126-thread-5-processing-n:192.168.43.197:8983_solr) [   ] o.a.s.c.a.c.OverseerCollectionMes***REMOVED***geHandler Collection: renamedTest3 operation: delete failed => org.apache.solr.common.SolrException: Collection : renamedTest3 is part of aliases: [Test3], remove or modify the aliases before removing this collection.";
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("mes***REMOVED***ge", qtp);
		e.setField(ApplicationConstant.TIMESTAMP, ApplicationConstantTest.TIMESTAMP_VALUE);
		e.setField(ApplicationConstant.ERROR_MSG, ApplicationConstantTest.COLLECTION_ERRORMSG_VALUE);
		e.setField(ApplicationConstant.SERVER_HOSTNAME, ApplicationConstantTest.SERVERHOSTNAME_VALUE);
		e.setField(ApplicationConstant.SERVER_IP, ApplicationConstantTest.SERVERIP_VALUE);
		e.setField(ApplicationConstant.SERVER_OS, ApplicationConstantTest.SERVEROS_VALUE);
		e.setField(ApplicationConstant.LOG_TYPE, ApplicationConstantTest.LOG_ERROR_VALUE);

		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);

		Assert.assertEquals(1, results.size());
		Assert.assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		Assert.assertEquals(1, matchListener.getMatchCount());
	}

	@Test
	public void InvalidSessionTest() {

		final String qtp = "2022-02-08 07:30:09.930 INFO  (qtp2005169944-26) [   x:testnew] o.a.s.u.p.LogUpdateProcessorFactory [testnew]  webapp=/solr path=/update params={commitWithin=1000&overwrite=true&wt=json&_=1643965367564}{add=[yashi (1724179189492350976)]} 0 30";

		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("mes***REMOVED***ge", qtp);
		e.setField(ApplicationConstant.QUERY_STRING, ApplicationConstantTest.QUERYSTRING_ADD_VALUE);
		e.setField(ApplicationConstant.COLLECTION, ApplicationConstantTest.COLLECTION_VALUE);
		e.setField(ApplicationConstant.VERB, ApplicationConstantTest.PATH_UPDATE_VALUE);
		e.setField(ApplicationConstant.SERVER_HOSTNAME, ApplicationConstantTest.SERVERHOSTNAME_VALUE);
		e.setField(ApplicationConstant.SERVER_IP, ApplicationConstantTest.SERVERIP_VALUE);
		e.setField(ApplicationConstant.SERVER_OS, ApplicationConstantTest.SERVEROS_VALUE);
		e.setField(ApplicationConstant.LOG_TYPE, ApplicationConstantTest.LOG_INFO_VALUE);
		e.setField(ApplicationConstant.CLASS, ApplicationConstantTest.UPDATE_CLASS_VALUE);
		e.setField(ApplicationConstant.USER_NAME, ApplicationConstantTest.USERNAME_VALUE);
		e.setField(ApplicationConstant.WEB_APP, ApplicationConstantTest.WEBAPP_VALUE);
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);

		Assert.assertEquals(1, results.size());
		Assert.assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		Assert.assertEquals(1, matchListener.getMatchCount());
	}

	// while deleting document
	@Test
	public void parseRecordQtpDeleteTest() {

		final String qtp = "2022-03-25 12:04:21.483 INFO  (qtp2005169944-64) [   x:test123] o.a.s.u.p.LogUpdateProcessorFactory [test123]  webapp=/solr path=/update params={commitWithin=1000&overwrite=true&wt=json&_=1648123036274}{delete=[1234 (-1728273303680843776)]} 0 49";

		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("mes***REMOVED***ge", qtp);
		e.setField(ApplicationConstant.TIMESTAMP, ApplicationConstantTest.TIMESTAMP_VALUE);
		e.setField(ApplicationConstant.QUERY_STRING, ApplicationConstantTest.QUERYSTRING_DELETE_VALUE);
		e.setField(ApplicationConstant.COLLECTION, ApplicationConstantTest.COLLECTION_VALUE);
		e.setField(ApplicationConstant.VERB, ApplicationConstantTest.PATH_UPDATE_VALUE);
		e.setField(ApplicationConstant.SERVER_HOSTNAME, ApplicationConstantTest.SERVERHOSTNAME_VALUE);
		e.setField(ApplicationConstant.SERVER_IP, ApplicationConstantTest.SERVERIP_VALUE);
		e.setField(ApplicationConstant.SERVER_OS, ApplicationConstantTest.SERVEROS_VALUE);
		e.setField(ApplicationConstant.LOG_TYPE, ApplicationConstantTest.LOG_INFO_VALUE);
		e.setField(ApplicationConstant.CLASS, ApplicationConstantTest.UPDATE_CLASS_VALUE);
		e.setField(ApplicationConstant.USER_NAME, ApplicationConstantTest.USERNAME_VALUE);
		e.setField(ApplicationConstant.WEB_APP, ApplicationConstantTest.WEBAPP_VALUE);

		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);

		Assert.assertEquals(1, results.size());
		Assert.assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		Assert.assertEquals(1, matchListener.getMatchCount());
	}

	// core creation
	@Test
	public void parseQtpCreateCoreTest() {

		final String qtp = "2022-03-24 11:56:22.486 INFO  (qtp2005169944-57) [   ] o.a.s.s.HttpSolrCall [admin] webapp=null path=/admin/cores params={name=Solr_***REMOVED***mple_core&action=CREATE&instanceDir=Solr_***REMOVED***mple_core&wt=json} status=0 QTime=2167";

		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("mes***REMOVED***ge", qtp);
		e.setField(ApplicationConstant.TIMESTAMP, ApplicationConstantTest.TIMESTAMP_VALUE);
		e.setField(ApplicationConstant.QUERY_STRING, ApplicationConstantTest.QUERYSTRING_CORECREATE_VALUE);
		e.setField(ApplicationConstant.CLASS, ApplicationConstantTest.HTTP_SOLR_CALL_VALUE);
		e.setField(ApplicationConstant.VERB, ApplicationConstantTest.PATH_CREATE_VALUE);
		e.setField(ApplicationConstant.SERVER_HOSTNAME, ApplicationConstantTest.SERVERHOSTNAME_VALUE);
		e.setField(ApplicationConstant.SERVER_IP, ApplicationConstantTest.SERVERIP_VALUE);
		e.setField(ApplicationConstant.SERVER_OS, ApplicationConstantTest.SERVEROS_VALUE);
		e.setField(ApplicationConstant.LOG_TYPE, ApplicationConstantTest.LOG_INFO_VALUE);
		e.setField(ApplicationConstant.CLASS, ApplicationConstantTest.HTTP_SOLR_CALL_VALUE);
		e.setField(ApplicationConstant.USER_NAME, ApplicationConstantTest.USERNAME_VALUE);
		e.setField(ApplicationConstant.WEB_APP, ApplicationConstantTest.WEBAPP_VALUE_NULL);

		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);

		Assert.assertEquals(1, results.size());
		Assert.assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		Assert.assertEquals(1, matchListener.getMatchCount());
	}

	// core updation
	@Test
	public void parseQtpUpdateCoreTest() {

		final String qtp = "2022-03-25 12:25:26.240 INFO  (qtp2005169944-56) [   ] o.a.s.s.HttpSolrCall [admin] webapp=null path=/admin/cores params={core=Solr_***REMOVED***mple_core&other=Sample&action=RENAME} status=0 QTime=237";

		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("mes***REMOVED***ge", qtp);
		e.setField(ApplicationConstant.TIMESTAMP, ApplicationConstantTest.TIMESTAMP_VALUE);
		e.setField(ApplicationConstant.QUERY_STRING, ApplicationConstantTest.QUERYSTRING_COREUPDATE_VALUE);
		e.setField(ApplicationConstant.CLASS, ApplicationConstantTest.HTTP_SOLR_CALL_VALUE);
		e.setField(ApplicationConstant.VERB, ApplicationConstantTest.PATH_RENAME_VALUE);
		e.setField(ApplicationConstant.SERVER_HOSTNAME, ApplicationConstantTest.SERVERHOSTNAME_VALUE);
		e.setField(ApplicationConstant.SERVER_IP, ApplicationConstantTest.SERVERIP_VALUE);
		e.setField(ApplicationConstant.SERVER_OS, ApplicationConstantTest.SERVEROS_VALUE);
		e.setField(ApplicationConstant.LOG_TYPE, ApplicationConstantTest.LOG_INFO_VALUE);
		e.setField(ApplicationConstant.CLASS, ApplicationConstantTest.HTTP_SOLR_CALL_VALUE);
		e.setField(ApplicationConstant.USER_NAME, ApplicationConstantTest.USERNAME_VALUE);
		e.setField(ApplicationConstant.WEB_APP, ApplicationConstantTest.WEBAPP_VALUE_NULL);

		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);

		Assert.assertEquals(1, results.size());
		Assert.assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		Assert.assertEquals(1, matchListener.getMatchCount());
	}

	// core deletion
	@Test
	public void parseQtpDeleteCoreTest() {

		final String qtp = "2022-03-25 04:49:23.148 INFO  (qtp2005169944-48) [   ] o.a.s.s.HttpSolrCall [admin] webapp=null path=/admin/cores params={deleteInstanceDir=true&core=Solr_***REMOVED***mple_core&deleteDataDir=true&action=UNLOAD&wt=json&deleteIndex=true} status=0 QTime=690";

		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("mes***REMOVED***ge", qtp);
		e.setField(ApplicationConstant.TIMESTAMP, ApplicationConstantTest.TIMESTAMP_VALUE);
		e.setField(ApplicationConstant.QUERY_STRING, ApplicationConstantTest.QUERYSTRING_COREDELETE_VALUE);
		e.setField(ApplicationConstant.CLASS, ApplicationConstantTest.HTTP_SOLR_CALL_VALUE);
		e.setField(ApplicationConstant.VERB, ApplicationConstantTest.PATH_DELETE_VALUE);
		e.setField(ApplicationConstant.SERVER_HOSTNAME, ApplicationConstantTest.SERVERHOSTNAME_VALUE);
		e.setField(ApplicationConstant.SERVER_IP, ApplicationConstantTest.SERVERIP_VALUE);
		e.setField(ApplicationConstant.SERVER_OS, ApplicationConstantTest.SERVEROS_VALUE);
		e.setField(ApplicationConstant.LOG_TYPE, ApplicationConstantTest.LOG_INFO_VALUE);
		e.setField(ApplicationConstant.CLASS, ApplicationConstantTest.HTTP_SOLR_CALL_VALUE);
		e.setField(ApplicationConstant.USER_NAME, ApplicationConstantTest.USERNAME_VALUE);
		e.setField(ApplicationConstant.WEB_APP, ApplicationConstantTest.WEBAPP_VALUE_NULL);

		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);

		Assert.assertEquals(1, results.size());
		Assert.assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		Assert.assertEquals(1, matchListener.getMatchCount());
	}

	@Test
	public void TimeFormatException() {

		final String qtp = "2022-02-0906:49:31.070 INFO  (qtp234250762-22) [   x:demo_collection] o.a.s.c.S.Request [demo_collection]  webapp=/solr path=/select params={q=id:401&_=1644230860222} hits=1 status=0 QTime=0";
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("mes***REMOVED***ge", qtp);
		e.setField(ApplicationConstant.TIMESTAMP, ApplicationConstantTest.INCORRECT_TIMESTAMP_VALUE);
		e.setField(ApplicationConstant.QUERY_STRING, ApplicationConstantTest.QUERYSTRING_SELECT_VALUE);
		e.setField(ApplicationConstant.COLLECTION, ApplicationConstantTest.COLLECTION_VALUE);
		e.setField(ApplicationConstant.VERB, ApplicationConstantTest.PATH_SELECT_VALUE);
		e.setField(ApplicationConstant.SERVER_HOSTNAME, ApplicationConstantTest.SERVERHOSTNAME_VALUE);
		e.setField(ApplicationConstant.SERVER_IP, ApplicationConstantTest.SERVERIP_VALUE);
		e.setField(ApplicationConstant.SERVER_OS, ApplicationConstantTest.SERVEROS_VALUE);
		e.setField(ApplicationConstant.LOG_TYPE, ApplicationConstantTest.LOG_INFO_VALUE);
		e.setField(ApplicationConstant.CLASS, ApplicationConstantTest.SOLR_QTP_REQUEST_VALUE);
		e.setField(ApplicationConstant.USER_NAME, ApplicationConstantTest.USERNAME_VALUE);
		e.setField(ApplicationConstant.WEB_APP, ApplicationConstantTest.WEBAPP_VALUE);
		List<Event> ev = new ArrayList<Event>();
		ev.add(e);
		Collection<Event> results = filter.filter(ev, matchListener);
		Assert.assertEquals(1, results.size());
		Set<String> tags = new HashSet<>((ArrayList) e.getField("tags"));
		Assert.assertEquals(1, tags.size());
		Assert.assertTrue(tags.contains(LOGSTASH_TAG_SOLR_PARSE_ERROR));
	}

	@Test
	public void EmptyRecordTest() {
		final String qtp = "";
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("mes***REMOVED***ge", qtp);
		List<Event> ev = new ArrayList<Event>();
		ev.add(e);
		Collection<Event> results = filter.filter(ev, matchListener);
		Assert.assertEquals(0, results.size());
		Set<String> tags = new HashSet<>((ArrayList) e.getField("tags"));
		Assert.assertEquals(1, tags.size());
		Assert.assertTrue(tags.contains(LOGSTASH_TAG_SKIP_NOT_SOLR));
	}

	// if not getting the solr qtp logs
	@Test
	public void InvalidRecordTest() {
		final String qtp = "2022-02-09 06:49:31.070 INFO  (qtp234250762-22) [   x:demo_collection]  [demo_collection]  webapp=/solr path=/select params={q=id:401&_=1644230860222} hits=1 status=0 QTime=0";
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("mes***REMOVED***ge", qtp);
		List<Event> ev = new ArrayList<Event>();
		ev.add(e);
		Collection<Event> results = filter.filter(ev, matchListener);
		Assert.assertEquals(0, results.size());
		Set<String> tags = new HashSet<>((ArrayList) e.getField("tags"));
		Assert.assertEquals(1, tags.size());
		Assert.assertTrue(tags.contains(LOGSTASH_TAG_SKIP_NOT_SOLR));
	}

	@Test
	public void parseServiceNameTest() {

		final String qtp = "2022-02-09 06:49:31.070 INFO  (qtp234250762-22) [   x:demo_collection] o.a.s.c.S.Request [demo_collection]  webapp=/solr path=/select params={q=id:401&_=1644230860222} hits=1 status=0 QTime=0";
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("mes***REMOVED***ge", qtp);
		e.setField(ApplicationConstant.USER_NAME, ApplicationConstantTest.USERNAME_VALUE);
		e.setField(ApplicationConstant.TIMESTAMP, ApplicationConstantTest.TIMESTAMP_VALUE);
		e.setField(ApplicationConstant.QUERY_STRING, ApplicationConstantTest.QUERYSTRING_SELECT_VALUE);
		e.setField(ApplicationConstant.COLLECTION, ApplicationConstantTest.COLLECTION_VALUE);
		e.setField(ApplicationConstant.VERB, ApplicationConstantTest.PATH_SELECT_VALUE);
		e.setField(ApplicationConstant.SERVER_HOSTNAME, ApplicationConstantTest.SERVERHOSTNAME_VALUE);
		e.setField(ApplicationConstant.SERVER_IP, ApplicationConstantTest.SERVERIP_VALUE);
		e.setField(ApplicationConstant.SERVER_OS, ApplicationConstantTest.SERVEROS_VALUE);
		e.setField(ApplicationConstant.REQUEST_TYPE, ApplicationConstantTest.REQUEST_SEARCH_VALUE);
		e.setField(ApplicationConstant.LOG_TYPE, ApplicationConstantTest.LOG_INFO_VALUE);
		e.setField(ApplicationConstant.CLASS, ApplicationConstantTest.SOLR_QTP_REQUEST_VALUE);
		e.setField(ApplicationConstant.WEB_APP, ApplicationConstantTest.WEBAPP_VALUE);

		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);

		Assert.assertEquals(1, results.size());
		Assert.assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		Assert.assertEquals(1, matchListener.getMatchCount());
	}

	@Test
	public void SolrServiceTest() {

		final String qtp = "2022-02-09 06:49:31.070 INFO  (qtp234250762-22) [   x:demo_collection] o.a.s.c.S.Request [demo_collection]  webapp=/solr path=/select params={q=id:401&_=1644230860222} hits=1 status=0 QTime=0";
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("mes***REMOVED***ge", qtp);
		e.setField(ApplicationConstant.USER_NAME, ApplicationConstantTest.USERNAME_VALUE);
		e.setField(ApplicationConstant.TIMESTAMP, ApplicationConstantTest.TIMESTAMP_VALUE);
		e.setField(ApplicationConstant.QUERY_STRING, ApplicationConstantTest.QUERYSTRING_SELECT_VALUE);
		e.setField(ApplicationConstant.COLLECTION, ApplicationConstantTest.COLLECTION_VALUE);
		e.setField(ApplicationConstant.VERB, ApplicationConstantTest.PATH_SELECT_VALUE);
		e.setField(ApplicationConstant.SERVER_HOSTNAME, ApplicationConstantTest.SERVERHOSTNAME_VALUE);
		e.setField(ApplicationConstant.SERVER_IP, ApplicationConstantTest.SERVERIP_VALUE);
		e.setField(ApplicationConstant.SERVER_OS, ApplicationConstantTest.SERVEROS_VALUE);
		e.setField(ApplicationConstant.REQUEST_TYPE, ApplicationConstantTest.REQUEST_SEARCH_VALUE);
		e.setField(ApplicationConstant.LOG_TYPE, ApplicationConstantTest.LOG_INFO_VALUE);
		e.setField(ApplicationConstant.CLASS, ApplicationConstantTest.SOLR_QTP_REQUEST_VALUE);
		e.setField(ApplicationConstant.WEB_APP, ApplicationConstantTest.WEBAPP_VALUE);

		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);

		Assert.assertEquals(1, results.size());
		Assert.assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		Assert.assertEquals(1, matchListener.getMatchCount());
	}

	@Test
	public void ServerIpTest() {

		final String qtp = "2022-02-09 06:49:31.070 INFO  (qtp234250762-22) [   x:demo_collection] o.a.s.c.S.Request [demo_collection]  webapp=/solr path=/select params={q=id:401&_=1644230860222} hits=1 status=0 QTime=0";
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("mes***REMOVED***ge", qtp);
		e.setField(ApplicationConstant.USER_NAME, ApplicationConstantTest.USERNAME_VALUE);
		e.setField(ApplicationConstant.TIMESTAMP, ApplicationConstantTest.TIMESTAMP_VALUE);
		e.setField(ApplicationConstant.QUERY_STRING, ApplicationConstantTest.QUERYSTRING_SELECT_VALUE);
		e.setField(ApplicationConstant.COLLECTION, ApplicationConstantTest.COLLECTION_VALUE);
		e.setField(ApplicationConstant.VERB, ApplicationConstantTest.PATH_SELECT_VALUE);
		e.setField(ApplicationConstant.SERVER_HOSTNAME, ApplicationConstantTest.SERVERHOSTNAME_VALUE);
		e.setField(ApplicationConstant.SERVER_OS, ApplicationConstantTest.SERVEROS_VALUE);
		e.setField(ApplicationConstant.REQUEST_TYPE, ApplicationConstantTest.REQUEST_SEARCH_VALUE);
		e.setField(ApplicationConstant.LOG_TYPE, ApplicationConstantTest.LOG_INFO_VALUE);
		e.setField(ApplicationConstant.SERVER_IP, ApplicationConstantTest.SERVERIP_VALUE);
		e.setField(ApplicationConstant.CLASS, ApplicationConstantTest.SOLR_QTP_REQUEST_VALUE);
		e.setField(ApplicationConstant.WEB_APP, ApplicationConstantTest.WEBAPP_VALUE);

		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);

		Assert.assertEquals(1, results.size());
		Assert.assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		Assert.assertEquals(1, matchListener.getMatchCount());
	}

	@Test
	public void parseServiceNameNegTest() {
		final String qtp = "2022-02-09 06:49:31.070 INFO  (qtp234250762-22) [   x:demo_collection] o.a.s.c.S.Request [demo_collection]  webapp=/solr path=/select params={q=id:401&_=1644230860222} hits=1 status=0 QTime=0";
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("mes***REMOVED***ge", qtp);
		e.setField(ApplicationConstant.USER_NAME, ApplicationConstantTest.USERNAME_VALUE);
		e.setField(ApplicationConstant.TIMESTAMP, ApplicationConstantTest.TIMESTAMP_VALUE);
		e.setField(ApplicationConstant.QUERY_STRING, ApplicationConstantTest.QUERYSTRING_SELECT_VALUE);
		e.setField(ApplicationConstant.COLLECTION, ApplicationConstantTest.COLLECTION_VALUE);
		e.setField(ApplicationConstant.VERB, ApplicationConstantTest.PATH_SELECT_VALUE);
		e.setField(ApplicationConstant.SERVER_HOSTNAME, ApplicationConstantTest.SERVERHOSTNAME_VALUE);
		e.setField(ApplicationConstant.SERVER_IP, ApplicationConstantTest.SERVERIP_VALUE);
		e.setField(ApplicationConstant.SERVER_OS, ApplicationConstantTest.SERVEROS_VALUE);
		e.setField(ApplicationConstant.REQUEST_TYPE, ApplicationConstantTest.REQUEST_SEARCH_VALUE);
		e.setField(ApplicationConstant.LOG_TYPE, ApplicationConstantTest.LOG_INFO_VALUE);
		e.setField(ApplicationConstant.CLASS, ApplicationConstantTest.SOLR_QTP_REQUEST_VALUE);
		e.setField(ApplicationConstant.WEB_APP, ApplicationConstantTest.WEBAPP_VALUE);

		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);

		Assert.assertEquals(1, results.size());
		Assert.assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		Assert.assertEquals(1, matchListener.getMatchCount());
	}

	private ApacheSolrAzureConnector getApacheSolrConnector(String jsonString) {
		Configuration config = new ConfigurationImpl(Collections.singletonMap("source", jsonString));
		Context context = new ContextImpl(null, null);
		ApacheSolrAzureConnector filter = new ApacheSolrAzureConnector("solr-id", config, context);
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
