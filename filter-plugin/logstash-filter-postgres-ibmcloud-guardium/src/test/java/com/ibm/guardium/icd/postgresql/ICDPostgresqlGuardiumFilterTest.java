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
	public void testcreateTableeventwithCloudLogs() {
		Context context = new ContextImpl(null, null);
		ICDPostgresqlGuardiumFilter filter = new ICDPostgresqlGuardiumFilter("test-id", null, context);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("message", "{\"data\":{\"tag\":\"platform.fd073c9576dd48778ca36548591e77b0.databases-for-postgresql\",\"label\":{\"crn\":\"crn:v1:bluemix:public:databases-for-postgresql:us-south:a/fd073c9576dd48778ca36548591e77b0:6b491b6f-5c7b-4108-95c1-b4344be65cf0::\",\"database\":\"postgresql\",\"ingester\":\"iclr-agent-fluent-bit\",\"member\":\"m-0\",\"region\":\"us-south\"},\"meta.cluster_name\":\"icd-prod-us-south-db-cxz89\",\"stream\":\"stderr\",\"message\":{\"saveServiceCopy\":false,\"message\":\"2025-03-12 07:15:30 UTC [DBeaver 23.3.1 - SQLEditor <Script-49.sql>] [00000] [29700]: [6-1] user=admin,db=ibmclouddb,client=172.30.78.0 LOG:  AUDIT: SESSION,6,1,DDL,CREATE TABLE,,,create table icd_1235 (id int),<not logged>\",\"logSourceCRN\":\"crn:v1:bluemix:public:databases-for-postgresql:us-south:a/fd073c9576dd48778ca36548591e77b0:6b491b6f-5c7b-4108-95c1-b4344be65cf0::\",\"serviceName\":\"databases-for-postgresql\"},\"logtag\":\"F\",\"app\":\"databases-for-postgresql\"},\"labels\":{\"applicationname\":\"ibm-platform-logs\",\"subsystemname\":\"databases-for-postgresql:6b491b6f-5c7b-4108-95c1-b4344be65cf0\",\"computername\":\"\",\"threadid\":\"\",\"ipaddress\":\"\"},\"metadata\":{\"timestamp\":\"2025-03-12T07:15:30.468425\",\"severity\":\"Info\",\"logid\":\"df1308bd-e46f-443f-a858-8f55872da222\",\"priorityclass\":\"high\",\"branchid\":\"694e0ecf-1767-acfd-a1ff-9491d8a246e2\",\"ingressTimestamp\":1741763733276000000}}");
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
		e.setField("message", "{\"_account\":\"86b66c1cfc\",\"_host\":\"loc-a33432b4-cb0e-4681-8c5c-0ae7bc2c923b-0\",\"_label\":{\"region\":\"us-south\",\"member\":\"m-1\",\"database\":\"postgresql\",\"crn\":\"crn:v1:bluemix:public:databases-for-postgresql:us-south:a/dffb515716044c4a8e8b77998b1b540e:fc665749-5844-4335-8096-3af41b3a190c::\",\"ingester\":\"iclr-agent-fluent-bit\"},\"_file\":\"databases-for-postgresql\",\"_line\":\"{\\\"saveServiceCopy\\\": false, \\\"logSourceCRN\\\":\\\"crn:v1:bluemix:public:databases-for-postgresql:us-south:a/dffb515716044c4a8e8b77998b1b540e:fc665749-5844-4335-8096-3af41b3a190c::\\\",\\\"message\\\":\\\"2024-10-21 09:34:07 UTC [DBeaver 23.2.5 - SQLEditor <Script-21.sql>] [00000] [30232]: [17-1] user=admin,db=ibmclouddb,client=172.30.166.192 LOG:  AUDIT: SESSION,17,1,DDL,ALTER,,,ALTER COLLATION french RENAME TO germen1,<not logged>\\\",\\\"serviceName\\\": \\\"databases-for-postgresql\\\"}\",\"_rawline\":null,\"_ts\":1729503247437,\"_app\":\"databases-for-postgresql\",\"_originating_user_agent\":null,\"saveServiceCopy\":false,\"logSourceCRN\":\"crn:v1:bluemix:public:databases-for-postgresql:us-south:a/dffb515716044c4a8e8b77998b1b540e:fc665749-5844-4335-8096-3af41b3a190c::\",\"message\":\"2024-10-25 06:32:55 UTC [DBeaver 24.1.3 - SQLEditor <Script-93.sql>] [00000] [42128]: [13-1] user=admin,db=ibmclouddb,client=172.30.165.0 LOG:  AUDIT: SESSION,11,1,DDL,CREATE TABLE,,,CREATE TABLE ACCOUNT_MASTER_SUPPLIERS_25Oct (SUPPLIERID varchar(5)NOT NULL PRIMARY KEY,NAME VARCHAR(30),PHONE CHAR(8)),<not logged>\",\"serviceName\":\"databases-for-postgresql\",\"_mezmo_line_size\":907}");
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
		e.setField("message", "{\"data\":{\"tag\":\"platform.fd073c9576dd48778ca36548591e77b0.databases-for-postgresql\",\"label\":{\"crn\":\"crn:v1:bluemix:public:databases-for-postgresql:us-south:a/fd073c9576dd48778ca36548591e77b0:6b491b6f-5c7b-4108-95c1-b4344be65cf0::\",\"database\":\"postgresql\",\"ingester\":\"iclr-agent-fluent-bit\",\"member\":\"m-0\",\"region\":\"us-south\"},\"meta.cluster_name\":\"icd-prod-us-south-db-cxz89\",\"stream\":\"stderr\",\"message\":{\"saveServiceCopy\":false,\"message\":\"2025-03-12 07:15:34 UTC [DBeaver 23.3.1 - SQLEditor <Script-49.sql>] [42601] [29700]: [13-1] user=admin,db=ibmclouddb,client=172.30.78.0 STATEMENT:  create tabl  ErrorFinalMultiq (id int)\",\"logSourceCRN\":\"crn:v1:bluemix:public:databases-for-postgresql:us-south:a/fd073c9576dd48778ca36548591e77b0:6b491b6f-5c7b-4108-95c1-b4344be65cf0::\",\"serviceName\":\"databases-for-postgresql\"},\"logtag\":\"F\",\"app\":\"databases-for-postgresql\"},\"labels\":{\"applicationname\":\"ibm-platform-logs\",\"subsystemname\":\"databases-for-postgresql:6b491b6f-5c7b-4108-95c1-b4344be65cf0\",\"computername\":\"\",\"threadid\":\"\",\"ipaddress\":\"\"},\"metadata\":{\"timestamp\":\"2025-03-12T07:15:34.910378\",\"severity\":\"Error\",\"logid\":\"45e9b8e0-bd6f-41eb-99ab-fb6d609b247b\",\"priorityclass\":\"high\",\"branchid\":\"524d4246-9548-c251-7c84-6e0d3e1a812f\",\"ingressTimestamp\":1741763740555000000}}");
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
		e.setField("message","{\"data\":{\"tag\":\"platform.fd073c9576dd48778ca36548591e77b0.databases-for-postgresql\",\"label\":{\"crn\":\"crn:v1:bluemix:public:databases-for-postgresql:us-south:a/fd073c9576dd48778ca36548591e77b0:6b491b6f-5c7b-4108-95c1-b4344be65cf0::\",\"database\":\"postgresql\",\"ingester\":\"iclr-agent-fluent-bit\",\"member\":\"m-0\",\"region\":\"us-south\"},\"meta.cluster_name\":\"icd-prod-us-south-db-cxz89\",\"stream\":\"stderr\",\"message\":{\"saveServiceCopy\":false,\"message\":\"2025-03-11 07:04:39 UTC [DBeaver 23.3.1 - SQLEditor <Script-49.sql>] [00000] [420]: [5-1] user=admin,db=ibmclouddb,client=172.30.78.0 LOG:  AUDIT: SESSION,4,1,DDL,DROP TABLE,,,drop table  icd_1235,<not logged>\",\"logSourceCRN\":\"crn:v1:bluemix:public:databases-for-postgresql:us-south:a/fd073c9576dd48778ca36548591e77b0:6b491b6f-5c7b-4108-95c1-b4344be65cf0::\",\"serviceName\":\"databases-for-postgresql\"},\"logtag\":\"F\",\"app\":\"databases-for-postgresql\"},\"labels\":{\"applicationname\":\"ibm-platform-logs\",\"subsystemname\":\"databases-for-postgresql:6b491b6f-5c7b-4108-95c1-b4344be65cf0\",\"computername\":\"\",\"threadid\":\"\",\"ipaddress\":\"\"},\"metadata\":{\"timestamp\":\"2025-03-11T07:04:39.888825\",\"severity\":\"Info\",\"logid\":\"d49beb36-95e9-4971-aacf-1912eb87da17\",\"priorityclass\":\"high\",\"branchid\":\"694e0ecf-1767-acfd-a1ff-9491d8a246e2\",\"ingressTimestamp\":1741676688230000000}}");
		e.setField("include_account_in_host","false");
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertEquals(1, results.size());
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, matchListener.getMatchCount());
	}

	@Test
	public void testInvalidEvent() {
		Context context = new ContextImpl(null, null);
		ICDPostgresqlGuardiumFilter filter = new ICDPostgresqlGuardiumFilter("test-id", null, context);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("message", "{\"_account\":\"86b66c1cfc\",\"_host\":\"loc-a33432b4-cb0e-4681-8c5c-0ae7bc2c923b-0\",\"_label\":{\"crn\":\"crn:v1:bluemix:public:databases-for-postgresql:us-south:a/dffb515716044c4a8e8b77998b1b540e:fc665749-5844-4335-8096-3af41b3a190c::\",\"region\":\"us-south\",\"database\":\"postgresql\",\"ingester\":\"iclr-agent-fluent-bit\",\"member\":\"m-0\"},\"_file\":\"databases-for-postgresql\",\"_line\":\"{\\\\\"saveServiceCopy\\\\\": false, \\\\\"message\\\\\":\\\\\"2024-10-21 11:36:30,905 INFO: no action. I am (c-fc665749-5844-4335-8096-3af41b3a190c-m-0), a secondary, and following a leader (c-fc665749-5844-4335-8096-3af41b3a190c-m-1)\\\\\",\\\\\"logSourceCRN\\\\\":\\\\\"crn:v1:bluemix:public:databases-for-postgresql:us-south:a/dffb515716044c4a8e8b77998b1b540e:fc665749-5844-4335-8096-3af41b3a190c::\\\\\",\\\\\"serviceName\\\\\": \\\\\"databases-for-postgresql\\\\\"}\",\"_rawline\":null,\"_ts\":1729510590422,\"_app\":\"databases-for-postgresql\",\"_originating_user_agent\":null,\"saveServiceCopy\":false,\"message\":\"2024-10-21 11:36:30,905 INFO: no action. I am (c-fc665749-5844-4335-8096-3af41b3a190c-m-0), a secondary, and following a leader (c-fc665749-5844-4335-8096-3af41b3a190c-m-1)\",\"logSourceCRN\":\"crn:v1:bluemix:public:databases-for-postgresql:us-south:a/dffb515716044c4a8e8b77998b1b540e:fc665749-5844-4335-8096-3af41b3a190c::\",\"serviceName\":\"databases-for-postgresql\",\"_mezmo_line_size\":836}");
		e.setField("include_account_in_host","false");
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertEquals(1, results.size());
		assertNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
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
