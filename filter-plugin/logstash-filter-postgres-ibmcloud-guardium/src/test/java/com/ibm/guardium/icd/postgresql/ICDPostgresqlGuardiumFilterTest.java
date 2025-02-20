/*
Copyright IBM Corp. 2023, 2025 All rights reserved.
SPDX-License-Identifier: Apache-2.0
*/

package com.ibm.guardium.icd.postgresql;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.logstash.plugins.ContextImpl;

import com.ibm.guardium.icd.postgresql.ICDPostgresqlGuardiumFilter;
import com.ibm.guardium.universalconnector.commons.GuardConstants;
import co.elastic.logstash.api.Context;
import co.elastic.logstash.api.Event;
import co.elastic.logstash.api.FilterMatchListener;

public class ICDPostgresqlGuardiumFilterTest {
	final static Context context = new ContextImpl(null, null);
	final static ICDPostgresqlGuardiumFilter filter = new ICDPostgresqlGuardiumFilter("test-id", null, context);

	@Test
	public void testcreateevent() {
		Context context = new ContextImpl(null, null);
		ICDPostgresqlGuardiumFilter filter = new ICDPostgresqlGuardiumFilter("test-id", null, context);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("message", "{\"_account\":\"86b66c1cfc\",\"_host\":\"loc-a33432b4-cb0e-4681-8c5c-0ae7bc2c923b-0\",\"_label\":{\"region\":\"us-south\",\"member\":\"m-1\",\"database\":\"postgresql\",\"crn\":\"crn:v1:bluemix:public:databases-for-postgresql:us-south:a/dffb515716044c4a8e8b77998b1b540e:fc665749-5844-4335-8096-3af41b3a190c::\",\"ingester\":\"iclr-agent-fluent-bit\"},\"_file\":\"databases-for-postgresql\",\"_line\":\"{\\\"saveServiceCopy\\\": false, \\\"logSourceCRN\\\":\\\"crn:v1:bluemix:public:databases-for-postgresql:us-south:a/dffb515716044c4a8e8b77998b1b540e:fc665749-5844-4335-8096-3af41b3a190c::\\\",\\\"message\\\":\\\"2024-10-21 09:34:07 UTC [DBeaver 23.2.5 - SQLEditor <Script-21.sql>] [00000] [30232]: [17-1] user=admin,db=ibmclouddb,client=172.30.166.192 LOG:  AUDIT: SESSION,17,1,DDL,CREATE TABLE,,,create table pratikshaSingleLine2110 (id int),<not logged>\\\",\\\"serviceName\\\": \\\"databases-for-postgresql\\\"}\",\"_rawline\":null,\"_ts\":1729503247437,\"_app\":\"databases-for-postgresql\",\"_originating_user_agent\":null,\"saveServiceCopy\":false,\"logSourceCRN\":\"crn:v1:bluemix:public:databases-for-postgresql:us-south:a/dffb515716044c4a8e8b77998b1b540e:fc665749-5844-4335-8096-3af41b3a190c::\",\"message\":\"2024-10-21 09:34:07 UTC [DBeaver 23.2.5 - SQLEditor <Script-21.sql>] [00000] [30232]: [17-1] user=admin,db=ibmclouddb,client=172.30.166.192 LOG:  AUDIT: SESSION,17,1,DDL,CREATE TABLE,,,create table pratikshaSingleLine2110 (id int),<not logged>\",\"serviceName\":\"databases-for-postgresql\",\"_mezmo_line_size\":907}");
		e.setField("include_account_in_host","false");
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertEquals(1, results.size());
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, matchListener.getMatchCount());
	}

	@Test
	public void testfetchevent() {
		Context context = new ContextImpl(null, null);
		ICDPostgresqlGuardiumFilter filter = new ICDPostgresqlGuardiumFilter("test-id", null, context);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("message", "{\"_account\":\"86b66c1cfc\",\"_host\":\"loc-a33432b4-cb0e-4681-8c5c-0ae7bc2c923b-0\",\"_label\":{\"region\":\"us-south\",\"member\":\"m-1\",\"database\":\"postgresql\",\"crn\":\"crn:v1:bluemix:public:databases-for-postgresql:us-south:a/dffb515716044c4a8e8b77998b1b540e:fc665749-5844-4335-8096-3af41b3a190c::\",\"ingester\":\"iclr-agent-fluent-bit\"},\"_file\":\"databases-for-postgresql\",\"_line\":\"{\\\"saveServiceCopy\\\": false, \\\"logSourceCRN\\\":\\\"crn:v1:bluemix:public:databases-for-postgresql:us-south:a/dffb515716044c4a8e8b77998b1b540e:fc665749-5844-4335-8096-3af41b3a190c::\\\",\\\"message\\\":\\\"2024-10-21 09:34:07 UTC [DBeaver 23.2.5 - SQLEditor <Script-21.sql>] [00000] [30232]: [17-1] user=admin,db=ibmclouddb,client=172.30.166.192 LOG:  AUDIT: SESSION,17,1,DDL,CREATE TABLE,,,create table pratikshaSingleLine2110 (id int),<not logged>\\\",\\\"serviceName\\\": \\\"databases-for-postgresql\\\"}\",\"_rawline\":null,\"_ts\":1729503247437,\"_app\":\"databases-for-postgresql\",\"_originating_user_agent\":null,\"saveServiceCopy\":false,\"logSourceCRN\":\"crn:v1:bluemix:public:databases-for-postgresql:us-south:a/dffb515716044c4a8e8b77998b1b540e:fc665749-5844-4335-8096-3af41b3a190c::\",\"message\":\"2024-10-25 04:55:04 UTC [DBeaver 23.2.5 - SQLEditor <Script-21.sql>] [00000] [58633]: [6-1] user=admin,db=ibmclouddb,client=172.30.152.192 LOG:  AUDIT: SESSION,6,1,DDL,CREATE TABLE,,,\\\"CREATE TABLE \\\"\\\"table_with_special_chars!@#$%^&*()\\\"\\\" (id SERIAL PRIMARY KEY)\\\",<not logged>\",\"serviceName\":\"databases-for-postgresql\",\"_mezmo_line_size\":907}");
		e.setField("include_account_in_host","false");
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertEquals(1, results.size());
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, matchListener.getMatchCount());
	}

	@Test
	public void testaltercollationevent() {
		Context context = new ContextImpl(null, null);
		ICDPostgresqlGuardiumFilter filter = new ICDPostgresqlGuardiumFilter("test-id", null, context);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("message", "{\"_account\":\"86b66c1cfc\",\"_host\":\"loc-a33432b4-cb0e-4681-8c5c-0ae7bc2c923b-0\",\"_label\":{\"region\":\"us-south\",\"member\":\"m-1\",\"database\":\"postgresql\",\"crn\":\"crn:v1:bluemix:public:databases-for-postgresql:us-south:a/dffb515716044c4a8e8b77998b1b540e:fc665749-5844-4335-8096-3af41b3a190c::\",\"ingester\":\"iclr-agent-fluent-bit\"},\"_file\":\"databases-for-postgresql\",\"_line\":\"{\\\"saveServiceCopy\\\": false, \\\"logSourceCRN\\\":\\\"crn:v1:bluemix:public:databases-for-postgresql:us-south:a/dffb515716044c4a8e8b77998b1b540e:fc665749-5844-4335-8096-3af41b3a190c::\\\",\\\"message\\\":\\\"2024-10-21 09:34:07 UTC [DBeaver 23.2.5 - SQLEditor <Script-21.sql>] [00000] [30232]: [17-1] user=admin,db=ibmclouddb,client=172.30.166.192 LOG:  AUDIT: SESSION,17,1,DDL,ALTER,,,ALTER COLLATION french RENAME TO germen1;,<not logged>\\\",\\\"serviceName\\\": \\\"databases-for-postgresql\\\"}\",\"_rawline\":null,\"_ts\":1729503247437,\"_app\":\"databases-for-postgresql\",\"_originating_user_agent\":null,\"saveServiceCopy\":false,\"logSourceCRN\":\"crn:v1:bluemix:public:databases-for-postgresql:us-south:a/dffb515716044c4a8e8b77998b1b540e:fc665749-5844-4335-8096-3af41b3a190c::\",\"message\":\"2024-10-25 06:32:55 UTC [DBeaver 24.1.3 - SQLEditor <Script-93.sql>] [00000] [42128]: [13-1] user=admin,db=ibmclouddb,client=172.30.165.0 LOG:  AUDIT: SESSION,11,1,DDL,CREATE TABLE,,,\\\"CREATE TABLE ACCOUNT_MASTER_SUPPLIERS_25Oct (SUPPLIERID varchar(5)NOT NULL PRIMARY KEY,NAME VARCHAR(30),PHONE CHAR(8))\\\",<not logged>\",\"serviceName\":\"databases-for-postgresql\",\"_mezmo_line_size\":907}");
		e.setField("include_account_in_host","false");
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertEquals(1, results.size());
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, matchListener.getMatchCount());
	}

	@Test
	public void testcreatecollationevent1() {
		Context context = new ContextImpl(null, null);
		ICDPostgresqlGuardiumFilter filter = new ICDPostgresqlGuardiumFilter("test-id", null, context);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("message", "{\"_account\":\"86b66c1cfc\",\"_host\":\"loc-a33432b4-cb0e-4681-8c5c-0ae7bc2c923b-0\",\"_label\":{\"region\":\"us-south\",\"member\":\"m-1\",\"database\":\"postgresql\",\"crn\":\"crn:v1:bluemix:public:databases-for-postgresql:us-south:a/dffb515716044c4a8e8b77998b1b540e:fc665749-5844-4335-8096-3af41b3a190c::\",\"ingester\":\"iclr-agent-fluent-bit\"},\"_file\":\"databases-for-postgresql\",\"_line\":\"{\\\"saveServiceCopy\\\": false, \\\"logSourceCRN\\\":\\\"crn:v1:bluemix:public:databases-for-postgresql:us-south:a/dffb515716044c4a8e8b77998b1b540e:fc665749-5844-4335-8096-3af41b3a190c::\\\",\\\"message\\\":\\\"2024-10-21 09:34:07 UTC [DBeaver 23.2.5 - SQLEditor <Script-21.sql>] [00000] [30232]: [17-1] user=admin,db=ibmclouddb,client=172.30.166.192 LOG:  AUDIT: SESSION,17,1,DDL,CREATE,,,CREATE COLLATION german FROM german_phonebook,<not logged>\\\",\\\"serviceName\\\": \\\"databases-for-postgresql\\\"}\",\"_rawline\":null,\"_ts\":1729503247437,\"_app\":\"databases-for-postgresql\",\"_originating_user_agent\":null,\"saveServiceCopy\":false,\"logSourceCRN\":\"crn:v1:bluemix:public:databases-for-postgresql:us-south:a/dffb515716044c4a8e8b77998b1b540e:fc665749-5844-4335-8096-3af41b3a190c::\",\"message\":\"2024-10-21 09:34:07 UTC [DBeaver 23.2.5 - SQLEditor <Script-21.sql>] [00000] [30232]: [17-1] user=admin,db=ibmclouddb,client=172.30.166.192 LOG:  AUDIT: SESSION,17,1,DDL,CREATE,,,CREATE COLLATION german FROM german_phonebook,<not logged>\",\"serviceName\":\"databases-for-postgresql\",\"_mezmo_line_size\":907}");
		e.setField("include_account_in_host","false");
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertEquals(1, results.size());
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, matchListener.getMatchCount());
	}


	@Test
	public void testerrors() {
		Context context = new ContextImpl(null, null);
		ICDPostgresqlGuardiumFilter filter = new ICDPostgresqlGuardiumFilter("test-id", null, context);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("message", "{\"_account\":\"86b66c1cfc\",\"_host\":\"loc-a33432b4-cb0e-4681-8c5c-0ae7bc2c923b-0\",\"_label\":{\"region\":\"us-south\",\"member\":\"m-1\",\"database\":\"postgresql\",\"crn\":\"crn:v1:bluemix:public:databases-for-postgresql:us-south:a/dffb515716044c4a8e8b77998b1b540e:fc665749-5844-4335-8096-3af41b3a190c::\",\"ingester\":\"iclr-agent-fluent-bit\"},\"_file\":\"databases-for-postgresql\",\"_line\":\"{\\\"saveServiceCopy\\\": false, \\\"logSourceCRN\\\":\\\"crn:v1:bluemix:public:databases-for-postgresql:us-south:a/dffb515716044c4a8e8b77998b1b540e:fc665749-5844-4335-8096-3af41b3a190c::\\\",\\\"message\\\":\\\"2024-10-21 09:34:07 UTC [DBeaver 23.2.5 - SQLEditor <Script-21.sql>] [42601] [65998]: [78-1] user=admin,db=ibmclouddb,client=172.30.173.0 STATEMENT:  create tabl  ErrorFinalMultiq (id int)\\\",\\\"serviceName\\\": \\\"databases-for-postgresql\\\"}\",\"_rawline\":null,\"_ts\":1729503247437,\"_app\":\"databases-for-postgresql\",\"_originating_user_agent\":null,\"saveServiceCopy\":false,\"logSourceCRN\":\"crn:v1:bluemix:public:databases-for-postgresql:us-south:a/dffb515716044c4a8e8b77998b1b540e:fc665749-5844-4335-8096-3af41b3a190c::\",\"message\":\"2024-10-21 09:34:07 UTC [DBeaver 23.2.5 - SQLEditor <Script-21.sql>] [42601] [65998]: [78-1] user=admin,db=ibmclouddb,client=172.30.173.0 STATEMENT:  create tabl  ErrorFinalMultiq (id int)\",\"serviceName\":\"databases-for-postgresql\",\"_mezmo_line_size\":907}");
		e.setField("include_account_in_host","false");
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertEquals(1, results.size());
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, matchListener.getMatchCount());
	}

	@Test
	public void testdroptableevent() {
		Context context = new ContextImpl(null, null);
		ICDPostgresqlGuardiumFilter filter = new ICDPostgresqlGuardiumFilter("test-id", null, context);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("message", "{\"_account\":\"86b66c1cfc\",\"_host\":\"loc-a33432b4-cb0e-4681-8c5c-0ae7bc2c923b-0\",\"_label\":{\"region\":\"us-south\",\"member\":\"m-1\",\"database\":\"postgresql\",\"crn\":\"crn:v1:bluemix:public:databases-for-postgresql:us-south:a/dffb515716044c4a8e8b77998b1b540e:fc665749-5844-4335-8096-3af41b3a190c::\",\"ingester\":\"iclr-agent-fluent-bit\"},\"_file\":\"databases-for-postgresql\",\"_line\":\"{\\\"saveServiceCopy\\\": false, \\\"logSourceCRN\\\":\\\"crn:v1:bluemix:public:databases-for-postgresql:us-south:a/dffb515716044c4a8e8b77998b1b540e:fc665749-5844-4335-8096-3af41b3a190c::\\\",\\\"message\\\":\\\"2024-10-21 09:34:07 UTC [DBeaver 23.2.5 - SQLEditor <Script-21.sql>] [00000] [30232]: [17-1] user=admin,db=ibmclouddb,client=172.30.166.192 LOG:  AUDIT: SESSION,17,1,DDL,DROP,,,DROP TABLE films,<not logged>\\\",\\\"serviceName\\\": \\\"databases-for-postgresql\\\"}\",\"_rawline\":null,\"_ts\":1729503247437,\"_app\":\"databases-for-postgresql\",\"_originating_user_agent\":null,\"saveServiceCopy\":false,\"logSourceCRN\":\"crn:v1:bluemix:public:databases-for-postgresql:us-south:a/dffb515716044c4a8e8b77998b1b540e:fc665749-5844-4335-8096-3af41b3a190c::\",\"message\":\"2024-10-21 09:34:07 UTC [DBeaver 23.2.5 - SQLEditor <Script-21.sql>] [00000] [30232]: [17-1] user=admin,db=ibmclouddb,client=172.30.166.192 LOG:  AUDIT: SESSION,17,1,DDL,DROP,,,DROP TABLE films,<not logged>\",\"serviceName\":\"databases-for-postgresql\",\"_mezmo_line_size\":907}");
		e.setField("include_account_in_host","false");
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertEquals(1, results.size());
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, matchListener.getMatchCount());
	}

	@Test
	public void testSinglelineE() {
		Context context = new ContextImpl(null, null);
		ICDPostgresqlGuardiumFilter filter = new ICDPostgresqlGuardiumFilter("test-id", null, context);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		//e.setField("message",
		//		"{\"_account\":\"f25cd67829\",\"_host\":\"ibm-cloud-databases-prod\",\"_label\":{\"database\":\"postgresql\",\"member\":\"m-0\",\"crn\":\"crn:v1:bluemix:public:databases-for-postgresql:us-south:a/2212c4a700f44505a917e8fcb952c4ce:dc13b788-ce60-4fab-bb6b-3021c6d30e97::\",\"region\":\"us-south\"},\"_mac\":\"06:02:03:16:cf:8f\",\"_file\":\"crn:v1:bluemix:public:databases-for-postgresql:us-south:a/2212c4a700f44505a917e8fcb952c4ce:dc13b788-ce60-4fab-bb6b-3021c6d30e97::\",\"_line\":\"{\\\"message\\\":\\\"Oct 14 21:33:42 loc-a33432b4-cb0e-4681-8c5c-0ae7bc2c923b-1 databases-for-postgresql 2023-09-04 10:32:12 UTC [psql] [00000] [4261]: [30-1] user=admin,db=testdb,client=172.30.88.128 LOG:  AUDIT: SESSION,22,1,DDL,DROP TABLE,,,\\\\\\\"Drop \\\",\\\"saveServiceCopy\\\":true,\\\"logSourceCRN\\\":\\\"crn:v1:bluemix:public:databases-for-postgresql:us-south:a/2212c4a700f44505a917e8fcb952c4ce:dc13b788-ce60-4fab-bb6b-3021c6d30e97::\\\"}\",\"_rawline\":null,\"_ts\":1693823533016,\"_platform\":\"ibm-cloud-databases-prod\",\"_app\":\"crn:v1:bluemix:public:databases-for-postgresql:us-south:a/2212c4a700f44505a917e8fcb952c4ce:dc13b788-ce60-4fab-bb6b-3021c6d30e97::\",\"_ipremote\":\"166.9.85.89\",\"message\":\"Oct 14 21:33:42 loc-a33432b4-cb0e-4681-8c5c-0ae7bc2c923b-1 databases-for-postgresql 2023-09-04 10:32:12 UTC [psql] [00000] [4261]: [30-1] user=admin,db=testdb,client=172.30.88.128 LOG:  AUDIT: SESSION,22,1,DDL,DROP TABLE,,,\\\"Drop \",\"saveServiceCopy\":true,\"logSourceCRN\":\"crn:v1:bluemix:public:databases-for-postgresql:us-south:a/2212c4a700f44505a917e8fcb952c4ce:dc13b788-ce60-4fab-bb6b-3021c6d30e97::\",\"_mezmo_line_size\":1074}");
		e.setField("message", "{\\\"_account\\\":\\\"86b66c1cfc\\\",\\\"_host\\\":\\\"loc-a33432b4-cb0e-4681-8c5c-0ae7bc2c923b-0\\\",\\\"_label\\\":{\\\"crn\\\":\\\"crn:v1:bluemix:public:databases-for-postgresql:us-south:a/dffb515716044c4a8e8b77998b1b540e:fc665749-5844-4335-8096-3af41b3a190c::\\\",\\\"region\\\":\\\"us-south\\\",\\\"database\\\":\\\"postgresql\\\",\\\"ingester\\\":\\\"iclr-agent-fluent-bit\\\",\\\"member\\\":\\\"m-0\\\"},\\\"_file\\\":\\\"databases-for-postgresql\\\",\\\"_line\\\":\\\"{\\\\\\\"saveServiceCopy\\\\\\\": false, \\\\\\\"message\\\\\\\":\\\\\\\"2024-10-21 11:36:30,905 INFO: no action. I am (c-fc665749-5844-4335-8096-3af41b3a190c-m-0), a secondary, and following a leader (c-fc665749-5844-4335-8096-3af41b3a190c-m-1)\\\\\\\",\\\\\\\"logSourceCRN\\\\\\\":\\\\\\\"crn:v1:bluemix:public:databases-for-postgresql:us-south:a/dffb515716044c4a8e8b77998b1b540e:fc665749-5844-4335-8096-3af41b3a190c::\\\\\\\",\\\\\\\"serviceName\\\\\\\": \\\\\\\"databases-for-postgresql\\\\\\\"}\\\",\\\"_rawline\\\":null,\\\"_ts\\\":1729510590422,\\\"_app\\\":\\\"databases-for-postgresql\\\",\\\"_originating_user_agent\\\":null,\\\"saveServiceCopy\\\":false,\\\"message\\\":\\\"2024-10-21 11:36:30,905 INFO: no action. I am (c-fc665749-5844-4335-8096-3af41b3a190c-m-0), a secondary, and following a leader (c-fc665749-5844-4335-8096-3af41b3a190c-m-1)\\\",\\\"logSourceCRN\\\":\\\"crn:v1:bluemix:public:databases-for-postgresql:us-south:a/dffb515716044c4a8e8b77998b1b540e:fc665749-5844-4335-8096-3af41b3a190c::\\\",\\\"serviceName\\\":\\\"databases-for-postgresql\\\",\\\"_mezmo_line_size\\\":836}");
		e.setField("include_account_in_host","false");
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertEquals(1, results.size());
	}
	@Test
	public void testSingleline() {
		Context context = new ContextImpl(null, null);
		ICDPostgresqlGuardiumFilter filter = new ICDPostgresqlGuardiumFilter("test-id", null, context);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		//e.setField("message",
		//		"{\"_account\":\"f25cd67829\",\"_host\":\"ibm-cloud-databases-prod\",\"_label\":{\"database\":\"postgresql\",\"member\":\"m-0\",\"crn\":\"crn:v1:bluemix:public:databases-for-postgresql:us-south:a/2212c4a700f44505a917e8fcb952c4ce:dc13b788-ce60-4fab-bb6b-3021c6d30e97::\",\"region\":\"us-south\"},\"_mac\":\"06:02:03:16:cf:8f\",\"_file\":\"crn:v1:bluemix:public:databases-for-postgresql:us-south:a/2212c4a700f44505a917e8fcb952c4ce:dc13b788-ce60-4fab-bb6b-3021c6d30e97::\",\"_line\":\"{\\\"message\\\":\\\"Oct 14 21:33:42 loc-a33432b4-cb0e-4681-8c5c-0ae7bc2c923b-1 databases-for-postgresql 2023-09-04 10:32:12 UTC [psql] [00000] [4261]: [30-1] user=admin,db=testdb,client=172.30.88.128 LOG:  AUDIT: SESSION,22,1,DDL,DROP TABLE,,,\\\\\\\"Drop \\\",\\\"saveServiceCopy\\\":true,\\\"logSourceCRN\\\":\\\"crn:v1:bluemix:public:databases-for-postgresql:us-south:a/2212c4a700f44505a917e8fcb952c4ce:dc13b788-ce60-4fab-bb6b-3021c6d30e97::\\\"}\",\"_rawline\":null,\"_ts\":1693823533016,\"_platform\":\"ibm-cloud-databases-prod\",\"_app\":\"crn:v1:bluemix:public:databases-for-postgresql:us-south:a/2212c4a700f44505a917e8fcb952c4ce:dc13b788-ce60-4fab-bb6b-3021c6d30e97::\",\"_ipremote\":\"166.9.85.89\",\"message\":\"Oct 14 21:33:42 loc-a33432b4-cb0e-4681-8c5c-0ae7bc2c923b-1 databases-for-postgresql 2023-09-04 10:32:12 UTC [psql] [00000] [4261]: [30-1] user=admin,db=testdb,client=172.30.88.128 LOG:  AUDIT: SESSION,22,1,DDL,DROP TABLE,,,\\\"Drop \",\"saveServiceCopy\":true,\"logSourceCRN\":\"crn:v1:bluemix:public:databases-for-postgresql:us-south:a/2212c4a700f44505a917e8fcb952c4ce:dc13b788-ce60-4fab-bb6b-3021c6d30e97::\",\"_mezmo_line_size\":1074}");
		e.setField("message", "{\"_account\":\"73d8998281\",\"_host\":\"ibm-cloud-databases-prod\",\"_label\":{\"crn\":\"crn:v1:bluemix:public:databases-for-postgresql:us-south:a/dffb515716044c4a8e8b77998b1b540e:e4f4b810-b583-478c-86a2-cbdfe1c0c3f4::\",\"region\":\"us-south\",\"ingester\":\"iclr-agent-fluent-bit\",\"database\":\"postgresql\",\"member\":\"m-1\"},\"_file\":\"crn:v1:bluemix:public:databases-for-postgresql:us-south:a/dffb515716044c4a8e8b77998b1b540e:e4f4b810-b583-478c-86a2-cbdfe1c0c3f4::\",\"_line\":\"{\\\"saveServiceCopy\\\": false, \\\"message\\\":\\\"2024-12-12 11:37:43 UTC [DBeaver 23.3.0 - SQLEditor <Script-28.sql>] [00000] [202540]: [23-1] user=admin,db=ibmclouddb,client=172.30.97.192 LOG:  AUDIT: SESSION,19,1,DDL,CREATE TABLE,,,create table piyush12Dec (id int),<not logged>\\\",\\\"logSourceCRN\\\":\\\"crn:v1:bluemix:public:databases-for-postgresql:us-south:a/dffb515716044c4a8e8b77998b1b540e:e4f4b810-b583-478c-86a2-cbdfe1c0c3f4::\\\",\\\"serviceName\\\": \\\"databases-for-postgresql\\\"}\",\"_rawline\":null,\"_ts\":1734003463616,\"_platform\":\"ibm-cloud-databases-prod\",\"_app\":\"crn:v1:bluemix:public:databases-for-postgresql:us-south:a/dffb515716044c4a8e8b77998b1b540e:e4f4b810-b583-478c-86a2-cbdfe1c0c3f4::\",\"_originating_user_agent\":null,\"saveServiceCopy\":false,\"message\":\"2024-12-12 11:37:43 UTC [DBeaver 23.3.0 - SQLEditor <Script-28.sql>] [00000] [202540]: [23-1] user=admin,db=ibmclouddb,client=172.30.97.192 LOG:  AUDIT: SESSION,19,1,DDL,CREATE TABLE,,,create table piyush12Dec (id int),<not logged>\",\"logSourceCRN\":\"crn:v1:bluemix:public:databases-for-postgresql:us-south:a/dffb515716044c4a8e8b77998b1b540e:e4f4b810-b583-478c-86a2-cbdfe1c0c3f4::\",\"serviceName\":\"databases-for-postgresql\",\"_mezmo_line_size\":1126}");
		e.setField("include_account_in_host","false");
		e.setField("logSource", "crn:v1:bluemix:public:databases-for-postgresql:us-south:a/dffb515716044c4a8e8b77998b1b540e:e4f4b810-b583-478c-86a2-cbdfe1c0c3f4::");
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertEquals(1, results.size());
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
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
