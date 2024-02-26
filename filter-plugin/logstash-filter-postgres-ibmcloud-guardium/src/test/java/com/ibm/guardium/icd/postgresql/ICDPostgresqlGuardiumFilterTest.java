/*
Copyright IBM Corp. 2023 All rights reserved.
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
		e.setField("message",
				"\"2022-09-05T07:22:23.428548901Z stderr F 2022-09-05 07:22:23 UTC [psql] [00000] [2572]: [3-1] user=admin,db=ibmclouddb,client=172.30.44.128 LOG:  AUDIT: SESSION,2,1,DDL,CREATE TABLE,,,\"create table maths_report(Roll_No int,Name varchar(20),Marks int);\",<not logged>\",\"saveServiceCopy\":true,\"logSourceCRN\":\"crn:v1:bluemix:public:databases-for-postgresql:us-south:a/2212c4a700f44505a917e8fcb952c4ce:f90dcc7d-68aa-4c85-8740-21929b237bfc::\"");
		e.setField("user", "admin");
		e.setField("timestamp", "2022-09-05 07:22:23");
		e.setField("timezone", "UTC");
		e.setField("accountId", "2212c4a700f44505a917e8fcb952c4ce");
		e.setField("dbname", "ibmclouddb");
		e.setField("clientIP", "172.30.216.75");
		e.setField("detail", ":f90dcc7d-68aa-4c85-8740-21929b237bfc");
		e.setField("sqlquery", "create table sst20_report(Roll_No int,Name varchar(20),Marks int);");

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
		e.setField("message", "");
		e.setField("user", "admin");
		e.setField("timestamp", "2022-09-05 07:22:23");
		e.setField("timezone", "UTC");
		e.setField("accountId", "2212c4a700f44505a917e8fcb952c4ce");
		e.setField("dbname", "ibmclouddb");
		e.setField("clientIP", "172.30.216.75");
		e.setField("detail", ":f90dcc7d-68aa-4c85-8740-21929b237bfc");
		e.setField("sqlquery", "\\\"FETCH PRIOR FROM \\\"\\\"mycursors123\\\"\\\";\\\"");

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
		e.setField("message", "");
		e.setField("user", "admin");
		e.setField("timestamp", "2022-09-05 07:22:23");
		e.setField("timezone", "UTC");
		e.setField("accountId", "2212c4a700f44505a917e8fcb952c4ce");
		e.setField("dbname", "ibmclouddb");
		e.setField("clientIP", "172.30.216.75");
		e.setField("detail", ":f90dcc7d-68aa-4c85-8740-21929b237bfc");
		e.setField("sqlquery", "\\\"ALTER COLLATION \\\"\\\"french\\\"\\\" RENAME TO germen1;\\\"");

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
		e.setField("message", "");
		e.setField("user", "admin");
		e.setField("timestamp", "2022-09-05 07:22:23");
		e.setField("timezone", "UTC");
		e.setField("accountId", "2212c4a700f44505a917e8fcb952c4ce");
		e.setField("dbname", "ibmclouddb");
		e.setField("clientIP", "172.30.216.75");
		e.setField("detail", ":f90dcc7d-68aa-4c85-8740-21929b237bfc");
		e.setField("sqlquery", "\\\"CREATE COLLATION german FROM \\\"\\\"german_phonebook\\\"\\\";\\\"");

		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertEquals(1, results.size());
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, matchListener.getMatchCount());
	}
	
	@Test
	public void testerroraltercollationevent1() {
		Context context = new ContextImpl(null, null);
		ICDPostgresqlGuardiumFilter filter = new ICDPostgresqlGuardiumFilter("test-id", null, context);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("message", "");
		e.setField("user", "admin");
		e.setField("timestamp", "2022-09-05 07:22:23");
		e.setField("timezone", "UTC");
		e.setField("accountId", "2212c4a700f44505a917e8fcb952c4ce");
		e.setField("dbname", "ibmclouddb");
		e.setField("clientIP", "172.30.216.75");
		e.setField("detail", ":f90dcc7d-68aa-4c85-8740-21929b237bfc");
		e.setField("status", " ALTER COLLATION \\\"french\\\" RENAME TO germen1;");
		e.setField("statement", "STATEMENT");
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertEquals(1, results.size());
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, matchListener.getMatchCount());
	}
	
	@Test
	public void testerrorspecial() {
		Context context = new ContextImpl(null, null);
		ICDPostgresqlGuardiumFilter filter = new ICDPostgresqlGuardiumFilter("test-id", null, context);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("message", "");
		e.setField("user", "admin");
		e.setField("timestamp", "2022-09-05 07:22:23");
		e.setField("timezone", "UTC");
		e.setField("accountId", "2212c4a700f44505a917e8fcb952c4ce");
		e.setField("dbname", "ibmclouddb");
		e.setField("clientIP", "172.30.216.75");
		e.setField("detail", ":f90dcc7d-68aa-4c85-8740-21929b237bfc");
		e.setField("status", "ALTER INDEX coord_idx ALTER COLUMN 3 SET STATISTICS 1000; INFO: archive-push command begin 2.43: [pg_wal/000000010000000000000014] --archive-async --compress-level=1 --compress-type=lz4 --config=/conf/postgresql/pgbackrest.conf --db-timeout=86400 --exec-id=5022-0a6d6c89 --log-level-console=info --log-level-stderr=info --pg1-path=/data/postgresql/15 --process-max=2 --protocol-timeout=86450 --repo1-path=/postgresql_backup_4d6d4913-a3d2-4224-b5a5-6fb298d871fa --repo1-s3-bucket=4d6d4913-a3d2-4224-b5a5-6fb298d871fa --repo1-s3-endpoint=s3.private.us.cloud-object-storage.appdomain.cloud --repo1-s3-key=<redacted> --repo1-s3-key-secret=<redacted> --repo1-s3-region=us-standard --repo1-s3-uri-style=path --repo1-type=s3 --spool-path=/data/postgresql/spool --stanza=formation INFO: pushed WAL file '000000010000000000000014' to the archive asynchronously INFO: archive-push command end: completed successfully (2015ms);");
		e.setField("statement", "STATEMENT");
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
		e.setField("message", "");
		e.setField("user", "admin");
		e.setField("timestamp", "2022-09-05 07:22:23");
		e.setField("timezone", "UTC");
		e.setField("accountId", "2212c4a700f44505a917e8fcb952c4ce");
		e.setField("dbname", "ibmclouddb");
		e.setField("clientIP", "172.30.216.75");
		e.setField("detail", ":f90dcc7d-68aa-4c85-8740-21929b237bfc");
		e.setField("status", " ALTER COLLATION \\\"french\\\" RENAME TO germen1;");
		e.setField("statement", "STATEMENT");
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
		e.setField("message", "");
		e.setField("user", "admin");
		e.setField("timestamp", "2022-09-05 07:22:23");
		e.setField("timezone", "UTC");
		e.setField("accountId", "2212c4a700f44505a917e8fcb952c4ce");
		e.setField("dbname", "ibmclouddb");
		e.setField("clientIP", "172.30.216.75");
		e.setField("detail", ":f90dcc7d-68aa-4c85-8740-21929b237bfc");
		e.setField("sqlquery", "\\\"DROP TABLE films, distributors;\\\"");

		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertEquals(1, results.size());
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, matchListener.getMatchCount());
	}

	@Test
	public void testalterroutineevent() {
		Context context = new ContextImpl(null, null);
		ICDPostgresqlGuardiumFilter filter = new ICDPostgresqlGuardiumFilter("test-id", null, context);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("message", "");
		e.setField("user", "admin");
		e.setField("timestamp", "2022-09-05 07:22:23");
		e.setField("timezone", "UTC");
		e.setField("accountId", "2212c4a700f44505a917e8fcb952c4ce");
		e.setField("dbname", "ibmclouddb");
		e.setField("clientIP", "172.30.216.75");
		e.setField("detail", ":f90dcc7d-68aa-4c85-8740-21929b237bfc");
		e.setField("sqlquery", "\\\"ALTER ROUTINE add(integer, integer) RENAME TO add_record;\\\"");

		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertEquals(1, results.size());
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, matchListener.getMatchCount());
	}

	@Test
	public void testdropoperarorevent() {
		Context context = new ContextImpl(null, null);
		ICDPostgresqlGuardiumFilter filter = new ICDPostgresqlGuardiumFilter("test-id", null, context);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("message", "");
		e.setField("user", "admin");
		e.setField("timestamp", "2022-09-05 07:22:23");
		e.setField("timezone", "UTC");
		e.setField("accountId", "2212c4a700f44505a917e8fcb952c4ce");
		e.setField("dbname", "ibmclouddb");
		e.setField("clientIP", "172.30.216.75");
		e.setField("detail", ":f90dcc7d-68aa-4c85-8740-21929b237bfc");
		e.setField("sqlquery", "\\\"DROP OPERATOR +@+ (numeric, numeric);\\\"");

		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertEquals(1, results.size());
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, matchListener.getMatchCount());
	}

	@Test
	public void testalteroperarorevent() {
		Context context = new ContextImpl(null, null);
		ICDPostgresqlGuardiumFilter filter = new ICDPostgresqlGuardiumFilter("test-id", null, context);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("message", "");
		e.setField("user", "admin");
		e.setField("timestamp", "2022-09-05 07:22:23");
		e.setField("timezone", "UTC");
		e.setField("accountId", "2212c4a700f44505a917e8fcb952c4ce");
		e.setField("dbname", "ibmclouddb");
		e.setField("clientIP", "172.30.216.75");
		e.setField("detail", ":f90dcc7d-68aa-4c85-8740-21929b237bfc");
		e.setField("sqlquery", "\\\"ALTER OPERATOR +@+ (numeric, numeric) OWNER TO admin;\\\"");

		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertEquals(1, results.size());
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, matchListener.getMatchCount());
	}

	@Test
	public void testcallevent() {
		Context context = new ContextImpl(null, null);
		ICDPostgresqlGuardiumFilter filter = new ICDPostgresqlGuardiumFilter("test-id", null, context);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("message", "");
		e.setField("user", "admin");
		e.setField("timestamp", "2022-09-05 07:22:23");
		e.setField("timezone", "UTC");
		e.setField("accountId", "2212c4a700f44505a917e8fcb952c4ce");
		e.setField("dbname", "ibmclouddb");
		e.setField("clientIP", "172.30.216.75");
		e.setField("detail", ":f90dcc7d-68aa-4c85-8740-21929b237bfc");
		e.setField("sqlquery", "\\\"call insert_data(12,13);\\\"");

		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertEquals(1, results.size());
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, matchListener.getMatchCount());
	}

	@Test
	public void testaltergroupevent() {
		Context context = new ContextImpl(null, null);
		ICDPostgresqlGuardiumFilter filter = new ICDPostgresqlGuardiumFilter("test-id", null, context);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("message", "");
		e.setField("user", "admin");
		e.setField("timestamp", "2022-09-05 07:22:23");
		e.setField("timezone", "UTC");
		e.setField("accountId", "2212c4a700f44505a917e8fcb952c4ce");
		e.setField("dbname", "ibmclouddb");
		e.setField("clientIP", "172.30.216.75");
		e.setField("detail", ":f90dcc7d-68aa-4c85-8740-21929b237bfc");
		e.setField("sqlquery", "\\\"ALTER GROUP ibmtest ADD USER karl, john;\\\"");

		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertEquals(1, results.size());
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, matchListener.getMatchCount());
	}

	@Test
	public void testcreatecollationevent() {
		Context context = new ContextImpl(null, null);
		ICDPostgresqlGuardiumFilter filter = new ICDPostgresqlGuardiumFilter("test-id", null, context);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("message", "");
		e.setField("user", "admin");
		e.setField("timestamp", "2022-09-05 07:22:23");
		e.setField("timezone", "UTC");
		e.setField("accountId", "2212c4a700f44505a917e8fcb952c4ce");
		e.setField("dbname", "ibmclouddb");
		e.setField("clientIP", "172.30.216.75");
		e.setField("detail", ":f90dcc7d-68aa-4c85-8740-21929b237bfc");
		e.setField("sqlquery",
				"\\\"CREATE COLLATION german_phonebook1 (provider = icu, locale = 'de-u-co-phonebk');\\\"");

		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertEquals(1, results.size());
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, matchListener.getMatchCount());
	}

	@Test
	public void testalteronlyevent() {
		Context context = new ContextImpl(null, null);
		ICDPostgresqlGuardiumFilter filter = new ICDPostgresqlGuardiumFilter("test-id", null, context);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("message", "");
		e.setField("user", "admin");
		e.setField("timestamp", "2022-09-05 07:22:23");
		e.setField("timezone", "UTC");
		e.setField("accountId", "2212c4a700f44505a917e8fcb952c4ce");
		e.setField("dbname", "ibmclouddb");
		e.setField("clientIP", "172.30.216.75");
		e.setField("detail", ":f90dcc7d-68aa-4c85-8740-21929b237bfc");
		e.setField("sqlquery", "\\\"ALTER TABLE ONLY Demo ADD UNIQUE (Name, Age);\\\"");

		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertEquals(1, results.size());
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, matchListener.getMatchCount());
	}

	@Test
	public void testsetevent() {
		Context context = new ContextImpl(null, null);
		ICDPostgresqlGuardiumFilter filter = new ICDPostgresqlGuardiumFilter("test-id", null, context);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("message", "");
		e.setField("user", "admin");
		e.setField("timestamp", "2022-09-05 07:22:23");
		e.setField("timezone", "UTC");
		e.setField("accountId", "2212c4a700f44505a917e8fcb952c4ce");
		e.setField("dbname", "ibmclouddb");
		e.setField("clientIP", "172.30.216.75");
		e.setField("detail", ":f90dcc7d-68aa-4c85-8740-21929b237bfc");
		e.setField("sqlquery", "\\\"SET search_path TO myschema,public;\\\"");

		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertEquals(1, results.size());
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, matchListener.getMatchCount());
	}

	@Test
	public void testcreatevent() {
		Context context = new ContextImpl(null, null);
		ICDPostgresqlGuardiumFilter filter = new ICDPostgresqlGuardiumFilter("test-id", null, context);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("message", "");
		e.setField("user", "admin");
		e.setField("timestamp", "2022-09-05 07:22:23");
		e.setField("timezone", "UTC");
		e.setField("accountId", "2212c4a700f44505a917e8fcb952c4ce");
		e.setField("dbname", "ibmclouddb");
		e.setField("clientIP", "172.30.216.75");
		e.setField("detail", ":f90dcc7d-68aa-4c85-8740-21929b237bfc");
		e.setField("sqlquery", "\\\"CREATE TABLE SUBSHAGNIK(NAME varchar(40),Age int,City varchar(40));\\\"");

		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertEquals(1, results.size());
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, matchListener.getMatchCount());
	}

	@Test
	public void testcreatetypeevent() {
		Context context = new ContextImpl(null, null);
		ICDPostgresqlGuardiumFilter filter = new ICDPostgresqlGuardiumFilter("test-id", null, context);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("message", "");
		e.setField("user", "admin");
		e.setField("timestamp", "2022-09-05 07:22:23");
		e.setField("timezone", "UTC");
		e.setField("accountId", "2212c4a700f44505a917e8fcb952c4ce");
		e.setField("dbname", "ibmclouddb");
		e.setField("clientIP", "172.30.216.75");
		e.setField("detail", ":f90dcc7d-68aa-4c85-8740-21929b237bfc");
		e.setField("sqlquery", "\\\"CREATE TYPE myrowtype3 AS (f1 int, f2 text, f3 numeric);\\\"");
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertEquals(1, results.size());
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, matchListener.getMatchCount());
	}

	@Test
	public void testmergeevent() {
		Context context = new ContextImpl(null, null);
		ICDPostgresqlGuardiumFilter filter = new ICDPostgresqlGuardiumFilter("test-id", null, context);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("message", "");
		e.setField("user", "admin");
		e.setField("timestamp", "2022-09-05 07:22:23");
		e.setField("timezone", "UTC");
		e.setField("accountId", "2212c4a700f44505a917e8fcb952c4ce");
		e.setField("dbname", "ibmclouddb");
		e.setField("clientIP", "172.30.216.75");
		e.setField("detail", ":f90dcc7d-68aa-4c85-8740-21929b237bfc");
		e.setField("sqlquery",
				",MISC,???,,,MERGE into b using a on a.id = b.id when matched then update set x = b.x + 1 when not matched then insert (id,x,status) values (a.id,a.x,a.status);");
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertEquals(1, results.size());
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, matchListener.getMatchCount());
	}

	@Test
	public void testdropevent() {
		Context context = new ContextImpl(null, null);
		ICDPostgresqlGuardiumFilter filter = new ICDPostgresqlGuardiumFilter("test-id", null, context);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("message", "");
		e.setField("user", "admin");
		e.setField("timestamp", "2022-09-06 09:42:18");
		e.setField("timezone", "UTC");
		e.setField("accountId", "2212c4a700f44505a917e8fcb952c4ce");
		e.setField("dbname", "ibmclouddb");
		e.setField("clientIP", "172.30.148.111");
		e.setField("sqlquery", "drop table sst_report;");
		e.setField("detail", ":f90dcc7d-68aa-4c85-8740-21929b237bfc");
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertEquals(1, results.size());
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, matchListener.getMatchCount());
	}

	@Test
	public void testerrorevent() {
		Context context = new ContextImpl(null, null);
		ICDPostgresqlGuardiumFilter filter = new ICDPostgresqlGuardiumFilter("test-id", null, context);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("message",
				"\"2022-09-08T10:48:55.751244055Z stderr F 2022-09-08 10:48:55 UTC [psql] [42710] [4900]: [5-1] user=admin,db=ibmclouddb,client=172.30.216.75 STATEMENT:  CREATE USER Ankita111 PASSWORD 'Agent0806';\",\"saveServiceCopy\":true,\"logSourceCRN\":\"crn:v1:bluemix:public:databases-for-postgresql:us-south:a/2212c4a700f44505a917e8fcb952c4ce:c4f36d93-655a-4ae8-87cb-d098a6168b23::\"");
		e.setField("user", "admin");
		e.setField("timestamp", "2022-09-06 09:42:18");
		e.setField("timezone", "UTC");
		e.setField("accountId", "2212c4a700f44505a917e8fcb952c4ce");
		e.setField("dbname", "ibmclouddb");
		e.setField("clientIP", "172.30.148.111");
		e.setField("status", "CREATE USER Ankita111 PASSWORD 'Agent0806';");
		e.setField("statement", "STATEMENT");
		e.setField("detail", ":f90dcc7d-68aa-4c85-8740-21929b237bfc");
		e.setField("ID1", "4h001");
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertEquals(1, results.size());
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, matchListener.getMatchCount());
	}

	@Test
	public void testaccountId() {
		Context context = new ContextImpl(null, null);
		ICDPostgresqlGuardiumFilter filter = new ICDPostgresqlGuardiumFilter("test-id", null, context);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("message",
				"\"2022-09-05T07:22:23.428548901Z stderr F 2022-09-05 07:22:23 UTC [psql] [00000] [2572]: [3-1] user=admin,db=ibmclouddb,client=172.30.44.128 LOG:  AUDIT: SESSION,2,1,DDL,CREATE TABLE,,,\"create table maths_report(Roll_No int,Name varchar(20),Marks int);\",<not logged>\",\"saveServiceCopy\":true,\"logSourceCRN\":\"crn:v1:bluemix:public:databases-for-postgresql:us-south:a/2212c4a700f44505a917e8fcb952c4ce:f90dcc7d-68aa-4c85-8740-21929b237bfc::\"");
		e.setField("user", "admin");
		e.setField("timestamp", "2022-09-05 07:22:23");
		e.setField("timezone", "UTC");
		e.setField("accountId", null);
		e.setField("dbname", "ibmclouddb");
		e.setField("clientIP", "172.30.216.75");
		e.setField("sqlquery", "\"create table sst20_report(Roll_No int,Name varchar(20),Marks int);\"");
		e.setField("detail", ":f90dcc7d-68aa-4c85-8740-21929b237bfc");

		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertEquals(1, results.size());
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, matchListener.getMatchCount());
	}

	@Test
	public void testtimestamp() {
		Context context = new ContextImpl(null, null);
		ICDPostgresqlGuardiumFilter filter = new ICDPostgresqlGuardiumFilter("test-id", null, context);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("message",
				"\"2022-09-06T09:42:18.974754781Z stderr F 2022-09-06 09:42:18 UTC [psql] [00000] [4483]: [4-1] user=admin,db=ibmclouddb,client=172.30.148.111 LOG:  AUDIT: SESSION,2,1,DDL,DROP TABLE,,,drop table sst_report;,<not logged>\",\"saveServiceCopy\":true,\"logSourceCRN\":\"crn:v1:bluemix:public:databases-for-postgresql:us-south:a/2212c4a700f44505a917e8fcb952c4ce:57ea80ca-f9e0-496a-a452-1856078ce139::\"");
		e.setField("user", null);
		e.setField("timestamp", null);
		e.setField("timezone", null);
		e.setField("accountId", null);
		e.setField("dbname", null);
		e.setField("clientIP", null);
		e.setField("sqlquery", "drop table sst_report;");
		e.setField("detail", ":f90dcc7d-68aa-4c85-8740-21929b237bfc");
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertEquals(1, results.size());
	}

	@Test
	public void teststatusevent() {
		Context context = new ContextImpl(null, null);
		ICDPostgresqlGuardiumFilter filter = new ICDPostgresqlGuardiumFilter("test-id", null, context);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("message",
				"\"2022-09-08T10:48:55.751244055Z stderr F 2022-09-08 10:48:55 UTC [psql] [42710] [4900]: [5-1] user=admin,db=ibmclouddb,client=172.30.216.75 STATEMENT:  CREATE USER Ankita111 PASSWORD 'Agent0806';\",\"saveServiceCopy\":true,\"logSourceCRN\":\"crn:v1:bluemix:public:databases-for-postgresql:us-south:a/2212c4a700f44505a917e8fcb952c4ce:c4f36d93-655a-4ae8-87cb-d098a6168b23::\"");
		e.setField("user", "admin");
		e.setField("timestamp", "2022-09-06 09:42:18");
		e.setField("timezone", "UTC");
		e.setField("accountId", "2212c4a700f44505a917e8fcb952c4ce");
		e.setField("dbname", "ibmclouddb");
		e.setField("clientIP", "172.30.148.111");
		e.setField("status", null);
		e.setField("statement", "STATEMENT");
		e.setField("detail", ":f90dcc7d-68aa-4c85-8740-21929b237bfc");

		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertEquals(1, results.size());
	}

	@Test
	public void testloginfailevent() {
		Context context = new ContextImpl(null, null);
		ICDPostgresqlGuardiumFilter filter = new ICDPostgresqlGuardiumFilter("test-id", null, context);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("message",
				"\"2023-09-06T05:07:28.68501142Z stderr F 2023-09-06 05:07:28 UTC [[unknown]] [28P01] [598]: [1-1] user=admin,db=ibmclouddb,client=172.30.9.0 FATAL:  password authentication failed for user \"admin\"\",\"saveServiceCopy\":true,\"logSourceCRN\":\"crn:v1:bluemix:public:databases-for-postgresql:us-south:a/2212c4a700f44505a917e8fcb952c4ce:c4f36d93-655a-4ae8-87cb-d098a6168b23::\"");
		e.setField("user", "admin");
		e.setField("timestamp", "2022-09-06 09:42:18");
		e.setField("timezone", "UTC");
		e.setField("accountId", "2212c4a700f44505a917e8fcb952c4ce");
		e.setField("dbname", "ibmclouddb");
		e.setField("clientIP", "172.30.148.111");
		e.setField("status", "password authentication failed for user \\\"admin\\\"");
		e.setField("statement", "FATAL");
		e.setField("detail", ":f90dcc7d-68aa-4c85-8740-21929b237bfc");
		e.setField("ID1", "28p01");

		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertEquals(1, results.size());
	}

	@Test
	public void testLogstatusevent() {
		Context context = new ContextImpl(null, null);
		ICDPostgresqlGuardiumFilter filter = new ICDPostgresqlGuardiumFilter("test-id", null, context);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("message",
				"\"2022-09-08T10:48:55.751244055Z stderr F 2022-09-08 10:48:55 UTC [psql] [42710] [4900]: [5-1] user=admin,db=ibmclouddb,client=172.30.216.75 STATEMENT:  CREATE USER Ankita111 PASSWORD 'Agent0806';\",\"saveServiceCopy\":true,\"logSourceCRN\":\"crn:v1:bluemix:public:databases-for-postgresql:us-south:a/2212c4a700f44505a917e8fcb952c4ce:c4f36d93-655a-4ae8-87cb-d098a6168b23::\"");
		e.setField("user", "admin");
		e.setField("timestamp", "2022-09-06 09:42:18");
		e.setField("timezone", "UTC");
		e.setField("accountId", "2212c4a700f44505a917e8fcb952c4ce");
		e.setField("dbname", "ibmclouddb");
		e.setField("clientIP", "172.30.148.111");
		e.setField("status", null);
		e.setField("statement", "LOG");
		e.setField("detail", ":f90dcc7d-68aa-4c85-8740-21929b237bfc");

		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertEquals(1, results.size());
	}

	@Test
	public void teststatementevent() {
		Context context = new ContextImpl(null, null);
		ICDPostgresqlGuardiumFilter filter = new ICDPostgresqlGuardiumFilter("test-id", null, context);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("message",
				"\"2022-09-08T10:48:55.751244055Z stderr F 2022-09-08 10:48:55 UTC [psql] [42710] [4900]: [5-1] user=admin,db=ibmclouddb,client=172.30.216.75 STATEMENT:  CREATE USER Ankita111 PASSWORD 'Agent0806';\",\"saveServiceCopy\":true,\"logSourceCRN\":\"crn:v1:bluemix:public:databases-for-postgresql:us-south:a/2212c4a700f44505a917e8fcb952c4ce:c4f36d93-655a-4ae8-87cb-d098a6168b23::\"");
		e.setField("user", "admin");
		e.setField("timestamp", "2022-09-06 09:42:18");
		e.setField("timezone", "UTC");
		e.setField("accountId", "2212c4a700f44505a917e8fcb952c4ce");
		e.setField("dbname", "ibmclouddb");
		e.setField("clientIP", "172.30.148.111");
		e.setField("status", "CREATE USER Ankita111 PASSWORD 'Agent0806';");
		e.setField("statement", null);
		e.setField("detail", ":f90dcc7d-68aa-4c85-8740-21929b237bfc");

		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertEquals(1, results.size());
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, matchListener.getMatchCount());
	}

	@Test
	public void testjsonparse() {
		Context context = new ContextImpl(null, null);
		ICDPostgresqlGuardiumFilter filter = new ICDPostgresqlGuardiumFilter("test-id", null, context);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("message", " ");

		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertEquals(1, results.size());
	}

	@Test
	public void testclientIpevent() {
		Context context = new ContextImpl(null, null);
		ICDPostgresqlGuardiumFilter filter = new ICDPostgresqlGuardiumFilter("test-id", null, context);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("message",
				"\"2022-09-05T07:22:23.428548901Z stderr F 2022-09-05 07:22:23 UTC [psql] [00000] [2572]: [3-1] user=admin,db=ibmclouddb,client=172.30.44.128 LOG:  AUDIT: SESSION,2,1,DDL,CREATE TABLE,,,\"create table maths_report(Roll_No int,Name varchar(20),Marks int);\",<not logged>\",\"saveServiceCopy\":true,\"logSourceCRN\":\"crn:v1:bluemix:public:databases-for-postgresql:us-south:a/2212c4a700f44505a917e8fcb952c4ce:f90dcc7d-68aa-4c85-8740-21929b237bfc::\"");
		e.setField("user", "admin");
		e.setField("timestamp", "2022-09-05 07:22:23");
		e.setField("timezone", "UTC");
		e.setField("accountId", "2212c4a700f44505a917e8fcb952c4ce");
		e.setField("dbname", "ibmclouddb");
		e.setField("clientIP", null);
		e.setField("sqlquery", "\"create table sst20_report(Roll_No int,Name varchar(20),Marks int);\"");
		e.setField("detail", ":f90dcc7d-68aa-4c85-8740-21929b237bfc");

		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertEquals(1, results.size());
	}

	@Test
	public void testclientIpv6event() {
		Context context = new ContextImpl(null, null);
		ICDPostgresqlGuardiumFilter filter = new ICDPostgresqlGuardiumFilter("test-id", null, context);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("message",
				"\"2022-09-05T07:22:23.428548901Z stderr F 2022-09-05 07:22:23 UTC [psql] [00000] [2572]: [3-1] user=admin,db=ibmclouddb,client=172.30.44.128 LOG:  AUDIT: SESSION,2,1,DDL,CREATE TABLE,,,\"create table maths_report(Roll_No int,Name varchar(20),Marks int);\",<not logged>\",\"saveServiceCopy\":true,\"logSourceCRN\":\"crn:v1:bluemix:public:databases-for-postgresql:us-south:a/2212c4a700f44505a917e8fcb952c4ce:f90dcc7d-68aa-4c85-8740-21929b237bfc::\"");
		e.setField("user", "admin");
		e.setField("timestamp", "2022-09-05 07:22:23");
		e.setField("timezone", "UTC");
		e.setField("accountId", "2212c4a700f44505a917e8fcb952c4ce");
		e.setField("dbname", "ibmclouddb");
		e.setField("clientIP", "0000:0000:0000:0000:0000:FFFF:0000:0000");
		e.setField("sqlquery", "\"create table sst20_report(Roll_No int,Name varchar(20),Marks int);\"");
		e.setField("detail", ":f90dcc7d-68aa-4c85-8740-21929b237bfc");

		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertEquals(1, results.size());
		assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		assertEquals(1, matchListener.getMatchCount());
	}

	@Test
	public void testIncorrectTimeevent() {
		Context context = new ContextImpl(null, null);
		ICDPostgresqlGuardiumFilter filter = new ICDPostgresqlGuardiumFilter("test-id", null, context);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("message",
				"\"2022-09-05T07:22:23.428548901Z stderr F 2022-09-05 07:22:23 UTC [psql] [00000] [2572]: [3-1] user=admin,db=ibmclouddb,client=172.30.44.128 LOG:  AUDIT: SESSION,2,1,DDL,CREATE TABLE,,,\"create table maths_report(Roll_No int,Name varchar(20),Marks int);\",<not logged>\",\"saveServiceCopy\":true,\"logSourceCRN\":\"crn:v1:bluemix:public:databases-for-postgresql:us-south:a/2212c4a700f44505a917e8fcb952c4ce:f90dcc7d-68aa-4c85-8740-21929b237bfc::\"");
		e.setField("user", "admin");
		e.setField("timestamp", "20-2022-05T07:22:23.428548901Z");
		e.setField("timezone", "UTC");
		e.setField("accountId", "2212c4a700f44505a917e8fcb952c4ce");
		e.setField("dbname", "ibmclouddb");
		e.setField("clientIP", "172.30.216.75");
		e.setField("sqlquery", "\"create table sst20_report(Roll_No int,Name varchar(20),Marks int);\"");
		e.setField("detail", ":f90dcc7d-68aa-4c85-8740-21929b237bfc");

		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertEquals(1, results.size());
	}

	@Test
	public void testMultiline() {
		Context context = new ContextImpl(null, null);
		ICDPostgresqlGuardiumFilter filter = new ICDPostgresqlGuardiumFilter("test-id", null, context);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("message",
				"[{\"_account\":\"60e93e7dfd\",\"_host\":\"ibm-cloud-databases-prod\",\"_label\":{\"database\":\"postgresql\",\"member\":\"m-0\",\"crn\":\"crn:v1:bluemix:public:databases-for-postgresql:us-south:a\\/2212c4a700f44505a917e8fcb952c4ce:905c0a21-f65e-42e7-848f-963d18eb0954::\",\"region\":\"us-south\"},\"_mac\":\"b2:fd:1b:75:45:c8\",\"_file\":\"crn:v1:bluemix:public:databases-for-postgresql:us-south:a\\/2212c4a700f44505a917e8fcb952c4ce:905c0a21-f65e-42e7-848f-963d18eb0954::\",\"_line\":\"{\\\"message\\\":\\\"2023-08-29T08:01:44.532645876Z stderr F 2023-08-29 08:01:44 UTC [psql] [00000] [328]: [1-1] user=admin,db=ibmclouddb,client=10.36.46.158 LOG:  AUDIT: SESSION,1,1,DDL,CREATE TABLE,,,\\\\\\\"CREATE \\\",\\\"saveServiceCopy\\\":true,\\\"logSourceCRN\\\":\\\"crn:v1:bluemix:public:databases-for-postgresql:us-south:a\\/2212c4a700f44505a917e8fcb952c4ce:905c0a21-f65e-42e7-848f-963d18eb0954::\\\"}\",\"_rawline\":null,\"_ts\":1693296105050,\"_platform\":\"ibm-cloud-databases-prod\",\"_app\":\"crn:v1:bluemix:public:databases-for-postgresql:us-south:a\\/2212c4a700f44505a917e8fcb952c4ce:905c0a21-f65e-42e7-848f-963d18eb0954::\",\"_ipremote\":\"166.9.61.79\",\"message\":\"2023-08-29T08:01:44.532645876Z stderr F 2023-08-29 08:01:44 UTC [psql] [00000] [328]: [1-1] user=admin,db=ibmclouddb,client=10.36.46.158 LOG:  AUDIT: SESSION,1,1,DDL,CREATE TABLE,,,\\\"CREATE \",\"saveServiceCopy\":true,\"logSourceCRN\":\"crn:v1:bluemix:public:databases-for-postgresql:us-south:a\\/2212c4a700f44505a917e8fcb952c4ce:905c0a21-f65e-42e7-848f-963d18eb0954::\",\"_mezmo_line_size\":1059},{\"_account\":\"60e93e7dfd\",\"_host\":\"ibm-cloud-databases-prod\",\"_label\":{\"database\":\"postgresql\",\"member\":\"m-0\",\"crn\":\"crn:v1:bluemix:public:databases-for-postgresql:us-south:a\\/2212c4a700f44505a917e8fcb952c4ce:905c0a21-f65e-42e7-848f-963d18eb0954::\",\"region\":\"us-south\"},\"_mac\":\"b2:fd:1b:75:45:c8\",\"_file\":\"crn:v1:bluemix:public:databases-for-postgresql:us-south:a\\/2212c4a700f44505a917e8fcb952c4ce:905c0a21-f65e-42e7-848f-963d18eb0954::\",\"_line\":\"{\\\"message\\\":\\\"2023-08-29T08:01:44.532875843Z stderr F \\\\tTABLE\\\",\\\"saveServiceCopy\\\":true,\\\"logSourceCRN\\\":\\\"crn:v1:bluemix:public:databases-for-postgresql:us-south:a\\/2212c4a700f44505a917e8fcb952c4ce:905c0a21-f65e-42e7-848f-963d18eb0954::\\\"}\",\"_rawline\":null,\"_ts\":1693296105050,\"_platform\":\"ibm-cloud-databases-prod\",\"_app\":\"crn:v1:bluemix:public:databases-for-postgresql:us-south:a\\/2212c4a700f44505a917e8fcb952c4ce:905c0a21-f65e-42e7-848f-963d18eb0954::\",\"_ipremote\":\"166.9.61.79\",\"message\":\"2023-08-29T08:01:44.532875843Z stderr F \\tTABLE\",\"saveServiceCopy\":true,\"logSourceCRN\":\"crn:v1:bluemix:public:databases-for-postgresql:us-south:a\\/2212c4a700f44505a917e8fcb952c4ce:905c0a21-f65e-42e7-848f-963d18eb0954::\",\"_mezmo_line_size\":916},{\"_account\":\"60e93e7dfd\",\"_host\":\"ibm-cloud-databases-prod\",\"_label\":{\"database\":\"postgresql\",\"member\":\"m-0\",\"crn\":\"crn:v1:bluemix:public:databases-for-postgresql:us-south:a\\/2212c4a700f44505a917e8fcb952c4ce:905c0a21-f65e-42e7-848f-963d18eb0954::\",\"region\":\"us-south\"},\"_mac\":\"b2:fd:1b:75:45:c8\",\"_file\":\"crn:v1:bluemix:public:databases-for-postgresql:us-south:a\\/2212c4a700f44505a917e8fcb952c4ce:905c0a21-f65e-42e7-848f-963d18eb0954::\",\"_line\":\"{\\\"message\\\":\\\"2023-08-29T08:01:44.533048365Z stderr F \\\\tMULTILINE(student_id INT,\\\",\\\"saveServiceCopy\\\":true,\\\"logSourceCRN\\\":\\\"crn:v1:bluemix:public:databases-for-postgresql:us-south:a\\/2212c4a700f44505a917e8fcb952c4ce:905c0a21-f65e-42e7-848f-963d18eb0954::\\\"}\",\"_rawline\":null,\"_ts\":1693296105050,\"_platform\":\"ibm-cloud-databases-prod\",\"_app\":\"crn:v1:bluemix:public:databases-for-postgresql:us-south:a\\/2212c4a700f44505a917e8fcb952c4ce:905c0a21-f65e-42e7-848f-963d18eb0954::\",\"_ipremote\":\"166.9.61.79\",\"message\":\"2023-08-29T08:01:44.533048365Z stderr F \\tMULTILINE(student_id INT,\",\"saveServiceCopy\":true,\"logSourceCRN\":\"crn:v1:bluemix:public:databases-for-postgresql:us-south:a\\/2212c4a700f44505a917e8fcb952c4ce:905c0a21-f65e-42e7-848f-963d18eb0954::\",\"_mezmo_line_size\":936},{\"_account\":\"60e93e7dfd\",\"_host\":\"ibm-cloud-databases-prod\",\"_label\":{\"database\":\"postgresql\",\"member\":\"m-0\",\"crn\":\"crn:v1:bluemix:public:databases-for-postgresql:us-south:a\\/2212c4a700f44505a917e8fcb952c4ce:905c0a21-f65e-42e7-848f-963d18eb0954::\",\"region\":\"us-south\"},\"_mac\":\"b2:fd:1b:75:45:c8\",\"_file\":\"crn:v1:bluemix:public:databases-for-postgresql:us-south:a\\/2212c4a700f44505a917e8fcb952c4ce:905c0a21-f65e-42e7-848f-963d18eb0954::\",\"_line\":\"{\\\"message\\\":\\\"2023-08-29T08:01:44.533141921Z stderr F \\\\tstudent_name VARCHAR(30));\\\\\\\",<not logged>\\\",\\\"saveServiceCopy\\\":true,\\\"logSourceCRN\\\":\\\"crn:v1:bluemix:public:databases-for-postgresql:us-south:a\\/2212c4a700f44505a917e8fcb952c4ce:905c0a21-f65e-42e7-848f-963d18eb0954::\\\"}\",\"_rawline\":null,\"_ts\":1693296105050,\"_platform\":\"ibm-cloud-databases-prod\",\"_app\":\"crn:v1:bluemix:public:databases-for-postgresql:us-south:a\\/2212c4a700f44505a917e8fcb952c4ce:905c0a21-f65e-42e7-848f-963d18eb0954::\",\"_ipremote\":\"166.9.61.79\",\"message\":\"2023-08-29T08:01:44.533141921Z stderr F \\tstudent_name VARCHAR(30));\\\",<not logged>\",\"saveServiceCopy\":true,\"logSourceCRN\":\"crn:v1:bluemix:public:databases-for-postgresql:us-south:a\\/2212c4a700f44505a917e8fcb952c4ce:905c0a21-f65e-42e7-848f-963d18eb0954::\",\"_mezmo_line_size\":952}]");
		ArrayList<String> stringlist = new ArrayList<>(Arrays.asList("multiline"));
		e.setField("tags", stringlist);
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertEquals(1, results.size());
	}

	@Test
	public void testthirdpartyMultiline() {
		Context context = new ContextImpl(null, null);
		ICDPostgresqlGuardiumFilter filter = new ICDPostgresqlGuardiumFilter("test-id", null, context);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("message",
				"[{\"_account\":\"f25cd67829\",\"_host\":\"ibm-cloud-databases-prod\",\"_label\":{\"database\":\"postgresql\",\"member\":\"m-0\",\"crn\":\"crn:v1:bluemix:public:databases-for-postgresql:us-south:a/2212c4a700f44505a917e8fcb952c4ce:dc13b788-ce60-4fab-bb6b-3021c6d30e97::\",\"region\":\"us-south\"},\"_mac\":\"06:02:03:16:cf:8f\",\"_file\":\"crn:v1:bluemix:public:databases-for-postgresql:us-south:a/2212c4a700f44505a917e8fcb952c4ce:dc13b788-ce60-4fab-bb6b-3021c6d30e97::\",\"_line\":\"{\\\"message\\\":\\\"2023-09-04T10:32:12.755702432Z stderr F 2023-09-04 10:32:12 UTC [psql] [00000] [4261]: [30-1] user=admin,db=testdb,client=172.30.88.128 LOG:  AUDIT: SESSION,22,1,DDL,DROP TABLE,,,\\\\\\\"Drop \\\",\\\"saveServiceCopy\\\":true,\\\"logSourceCRN\\\":\\\"crn:v1:bluemix:public:databases-for-postgresql:us-south:a/2212c4a700f44505a917e8fcb952c4ce:dc13b788-ce60-4fab-bb6b-3021c6d30e97::\\\"}\",\"_rawline\":null,\"_ts\":1693823533016,\"_platform\":\"ibm-cloud-databases-prod\",\"_app\":\"crn:v1:bluemix:public:databases-for-postgresql:us-south:a/2212c4a700f44505a917e8fcb952c4ce:dc13b788-ce60-4fab-bb6b-3021c6d30e97::\",\"_ipremote\":\"166.9.85.89\",\"message\":\"2023-09-04T10:32:12.755702432Z stderr F 2023-09-04 10:32:12 UTC [psql] [00000] [4261]: [30-1] user=admin,db=testdb,client=172.30.88.128 LOG:  AUDIT: SESSION,22,1,DDL,DROP TABLE,,,\\\"Drop \",\"saveServiceCopy\":true,\"logSourceCRN\":\"crn:v1:bluemix:public:databases-for-postgresql:us-south:a/2212c4a700f44505a917e8fcb952c4ce:dc13b788-ce60-4fab-bb6b-3021c6d30e97::\",\"_mezmo_line_size\":1074}, {\"_account\":\"f25cd67829\",\"_host\":\"ibm-cloud-databases-prod\",\"_label\":{\"database\":\"postgresql\",\"member\":\"m-0\",\"crn\":\"crn:v1:bluemix:public:databases-for-postgresql:us-south:a/2212c4a700f44505a917e8fcb952c4ce:dc13b788-ce60-4fab-bb6b-3021c6d30e97::\",\"region\":\"us-south\"},\"_mac\":\"06:02:03:16:cf:8f\",\"_file\":\"crn:v1:bluemix:public:databases-for-postgresql:us-south:a/2212c4a700f44505a917e8fcb952c4ce:dc13b788-ce60-4fab-bb6b-3021c6d30e97::\",\"_line\":\"{\\\"message\\\":\\\"2023-09-04T10:32:12.755769851Z stderr F \\\\ttable \\\",\\\"saveServiceCopy\\\":true,\\\"logSourceCRN\\\":\\\"crn:v1:bluemix:public:databases-for-postgresql:us-south:a/2212c4a700f44505a917e8fcb952c4ce:dc13b788-ce60-4fab-bb6b-3021c6d30e97::\\\"}\",\"_rawline\":null,\"_ts\":1693823533016,\"_platform\":\"ibm-cloud-databases-prod\",\"_app\":\"crn:v1:bluemix:public:databases-for-postgresql:us-south:a/2212c4a700f44505a917e8fcb952c4ce:dc13b788-ce60-4fab-bb6b-3021c6d30e97::\",\"_ipremote\":\"166.9.85.89\",\"message\":\"2023-09-04T10:32:12.755769851Z stderr F \\ttable \",\"saveServiceCopy\":true,\"logSourceCRN\":\"crn:v1:bluemix:public:databases-for-postgresql:us-south:a/2212c4a700f44505a917e8fcb952c4ce:dc13b788-ce60-4fab-bb6b-3021c6d30e97::\",\"_mezmo_line_size\":917}, {\"_account\":\"f25cd67829\",\"_host\":\"ibm-cloud-databases-prod\",\"_label\":{\"database\":\"postgresql\",\"member\":\"m-0\",\"crn\":\"crn:v1:bluemix:public:databases-for-postgresql:us-south:a/2212c4a700f44505a917e8fcb952c4ce:dc13b788-ce60-4fab-bb6b-3021c6d30e97::\",\"region\":\"us-south\"},\"_mac\":\"06:02:03:16:cf:8f\",\"_file\":\"crn:v1:bluemix:public:databases-for-postgresql:us-south:a/2212c4a700f44505a917e8fcb952c4ce:dc13b788-ce60-4fab-bb6b-3021c6d30e97::\",\"_line\":\"{\\\"message\\\":\\\"2023-09-04T10:32:12.755789108Z stderr F \\\\tThirdparttool1;\\\",\\\"saveServiceCopy\\\":true,\\\"logSourceCRN\\\":\\\"crn:v1:bluemix:public:databases-for-postgresql:us-south:a/2212c4a700f44505a917e8fcb952c4ce:dc13b788-ce60-4fab-bb6b-3021c6d30e97::\\\"}\",\"_rawline\":null,\"_ts\":1693823533016,\"_platform\":\"ibm-cloud-databases-prod\",\"_app\":\"crn:v1:bluemix:public:databases-for-postgresql:us-south:a/2212c4a700f44505a917e8fcb952c4ce:dc13b788-ce60-4fab-bb6b-3021c6d30e97::\",\"_ipremote\":\"166.9.85.89\",\"message\":\"2023-09-04T10:32:12.755789108Z stderr F \\tThirdparttool1;\",\"saveServiceCopy\":true,\"logSourceCRN\":\"crn:v1:bluemix:public:databases-for-postgresql:us-south:a/2212c4a700f44505a917e8fcb952c4ce:dc13b788-ce60-4fab-bb6b-3021c6d30e97::\",\"_mezmo_line_size\":926}, {\"_account\":\"f25cd67829\",\"_host\":\"ibm-cloud-databases-prod\",\"_label\":{\"database\":\"postgresql\",\"member\":\"m-0\",\"crn\":\"crn:v1:bluemix:public:databases-for-postgresql:us-south:a/2212c4a700f44505a917e8fcb952c4ce:dc13b788-ce60-4fab-bb6b-3021c6d30e97::\",\"region\":\"us-south\"},\"_mac\":\"06:02:03:16:cf:8f\",\"_file\":\"crn:v1:bluemix:public:databases-for-postgresql:us-south:a/2212c4a700f44505a917e8fcb952c4ce:dc13b788-ce60-4fab-bb6b-3021c6d30e97::\",\"_line\":\"{\\\"message\\\":\\\"2023-09-04T10:32:12.755824111Z stderr F \\\\t\\\\\\\",<not logged>\\\",\\\"saveServiceCopy\\\":true,\\\"logSourceCRN\\\":\\\"crn:v1:bluemix:public:databases-for-postgresql:us-south:a/2212c4a700f44505a917e8fcb952c4ce:dc13b788-ce60-4fab-bb6b-3021c6d30e97::\\\"}\",\"_rawline\":null,\"_ts\":1693823533016,\"_platform\":\"ibm-cloud-databases-prod\",\"_app\":\"crn:v1:bluemix:public:databases-for-postgresql:us-south:a/2212c4a700f44505a917e8fcb952c4ce:dc13b788-ce60-4fab-bb6b-3021c6d30e97::\",\"_ipremote\":\"166.9.85.89\",\"message\":\"2023-09-04T10:32:12.755824111Z stderr F \\t\\\",<not logged>\",\"saveServiceCopy\":true,\"logSourceCRN\":\"crn:v1:bluemix:public:databases-for-postgresql:us-south:a/2212c4a700f44505a917e8fcb952c4ce:dc13b788-ce60-4fab-bb6b-3021c6d30e97::\",\"_mezmo_line_size\":926}]");
		ArrayList<String> stringlist = new ArrayList<>(Arrays.asList("multiline"));
		e.setField("tags", stringlist);
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertEquals(1, results.size());
	}

	@Test
	public void testErrorMultiline() {
		Context context = new ContextImpl(null, null);
		ICDPostgresqlGuardiumFilter filter = new ICDPostgresqlGuardiumFilter("test-id", null, context);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("message",
				"[{\"_account\":\"b0b64dcdd2\",\"_host\":\"ibm-cloud-databases-prod\",\"_label\":{\"database\":\"postgresql\",\"member\":\"m-0\",\"crn\":\"crn:v1:bluemix:public:databases-for-postgresql:us-south:a\\/2212c4a700f44505a917e8fcb952c4ce:3720bc70-4a8a-4945-84d6-1efecd3f956e::\",\"region\":\"us-south\"},\"_mac\":\"72:20:fd:54:5c:53\",\"_file\":\"crn:v1:bluemix:public:databases-for-postgresql:us-south:a\\/2212c4a700f44505a917e8fcb952c4ce:3720bc70-4a8a-4945-84d6-1efecd3f956e::\",\"_line\":\"{\\\"message\\\":\\\"2023-08-30T07:44:16.544550563Z stderr F 2023-08-30 07:44:16 UTC [psql] [42601] [4060]: [2-1] user=admin,db=ibmclouddb,client=172.30.88.128 STATEMENT:  CREATE TABL Products(  \\\",\\\"saveServiceCopy\\\":true,\\\"logSourceCRN\\\":\\\"crn:v1:bluemix:public:databases-for-postgresql:us-south:a\\/2212c4a700f44505a917e8fcb952c4ce:3720bc70-4a8a-4945-84d6-1efecd3f956e::\\\"}\",\"_rawline\":null,\"_ts\":1693381456997,\"_platform\":\"ibm-cloud-databases-prod\",\"_app\":\"crn:v1:bluemix:public:databases-for-postgresql:us-south:a\\/2212c4a700f44505a917e8fcb952c4ce:3720bc70-4a8a-4945-84d6-1efecd3f956e::\",\"_ipremote\":\"166.9.90.41\",\"message\":\"2023-08-30T07:44:16.544550563Z stderr F 2023-08-30 07:44:16 UTC [psql] [42601] [4060]: [2-1] user=admin,db=ibmclouddb,client=172.30.88.128 STATEMENT:  CREATE TABL Products(  \",\"saveServiceCopy\":true,\"logSourceCRN\":\"crn:v1:bluemix:public:databases-for-postgresql:us-south:a\\/2212c4a700f44505a917e8fcb952c4ce:3720bc70-4a8a-4945-84d6-1efecd3f956e::\",\"_mezmo_line_size\":1042},{\"_account\":\"b0b64dcdd2\",\"_host\":\"ibm-cloud-databases-prod\",\"_label\":{\"database\":\"postgresql\",\"member\":\"m-0\",\"crn\":\"crn:v1:bluemix:public:databases-for-postgresql:us-south:a\\/2212c4a700f44505a917e8fcb952c4ce:3720bc70-4a8a-4945-84d6-1efecd3f956e::\",\"region\":\"us-south\"},\"_mac\":\"72:20:fd:54:5c:53\",\"_file\":\"crn:v1:bluemix:public:databases-for-postgresql:us-south:a\\/2212c4a700f44505a917e8fcb952c4ce:3720bc70-4a8a-4945-84d6-1efecd3f956e::\",\"_line\":\"{\\\"message\\\":\\\"2023-08-30T07:44:16.544563656Z stderr F \\\\t    ID INT,  \\\",\\\"saveServiceCopy\\\":true,\\\"logSourceCRN\\\":\\\"crn:v1:bluemix:public:databases-for-postgresql:us-south:a\\/2212c4a700f44505a917e8fcb952c4ce:3720bc70-4a8a-4945-84d6-1efecd3f956e::\\\"}\",\"_rawline\":null,\"_ts\":1693381456997,\"_platform\":\"ibm-cloud-databases-prod\",\"_app\":\"crn:v1:bluemix:public:databases-for-postgresql:us-south:a\\/2212c4a700f44505a917e8fcb952c4ce:3720bc70-4a8a-4945-84d6-1efecd3f956e::\",\"_ipremote\":\"166.9.90.41\",\"message\":\"2023-08-30T07:44:16.544563656Z stderr F \\t    ID INT,  \",\"saveServiceCopy\":true,\"logSourceCRN\":\"crn:v1:bluemix:public:databases-for-postgresql:us-south:a\\/2212c4a700f44505a917e8fcb952c4ce:3720bc70-4a8a-4945-84d6-1efecd3f956e::\",\"_mezmo_line_size\":923},{\"_account\":\"b0b64dcdd2\",\"_host\":\"ibm-cloud-databases-prod\",\"_label\":{\"database\":\"postgresql\",\"member\":\"m-0\",\"crn\":\"crn:v1:bluemix:public:databases-for-postgresql:us-south:a\\/2212c4a700f44505a917e8fcb952c4ce:3720bc70-4a8a-4945-84d6-1efecd3f956e::\",\"region\":\"us-south\"},\"_mac\":\"72:20:fd:54:5c:53\",\"_file\":\"crn:v1:bluemix:public:databases-for-postgresql:us-south:a\\/2212c4a700f44505a917e8fcb952c4ce:3720bc70-4a8a-4945-84d6-1efecd3f956e::\",\"_line\":\"{\\\"message\\\":\\\"2023-08-30T07:44:16.544571168Z stderr F \\\\t    Product_Name VARCHAR(65),  \\\",\\\"saveServiceCopy\\\":true,\\\"logSourceCRN\\\":\\\"crn:v1:bluemix:public:databases-for-postgresql:us-south:a\\/2212c4a700f44505a917e8fcb952c4ce:3720bc70-4a8a-4945-84d6-1efecd3f956e::\\\"}\",\"_rawline\":null,\"_ts\":1693381456997,\"_platform\":\"ibm-cloud-databases-prod\",\"_app\":\"crn:v1:bluemix:public:databases-for-postgresql:us-south:a\\/2212c4a700f44505a917e8fcb952c4ce:3720bc70-4a8a-4945-84d6-1efecd3f956e::\",\"_ipremote\":\"166.9.90.41\",\"message\":\"2023-08-30T07:44:16.544571168Z stderr F \\t    Product_Name VARCHAR(65),  \",\"saveServiceCopy\":true,\"logSourceCRN\":\"crn:v1:bluemix:public:databases-for-postgresql:us-south:a\\/2212c4a700f44505a917e8fcb952c4ce:3720bc70-4a8a-4945-84d6-1efecd3f956e::\",\"_mezmo_line_size\":941},{\"_account\":\"b0b64dcdd2\",\"_host\":\"ibm-cloud-databases-prod\",\"_label\":{\"database\":\"postgresql\",\"member\":\"m-0\",\"crn\":\"crn:v1:bluemix:public:databases-for-postgresql:us-south:a\\/2212c4a700f44505a917e8fcb952c4ce:3720bc70-4a8a-4945-84d6-1efecd3f956e::\",\"region\":\"us-south\"},\"_mac\":\"72:20:fd:54:5c:53\",\"_file\":\"crn:v1:bluemix:public:databases-for-postgresql:us-south:a\\/2212c4a700f44505a917e8fcb952c4ce:3720bc70-4a8a-4945-84d6-1efecd3f956e::\",\"_line\":\"{\\\"message\\\":\\\"2023-08-30T07:44:16.544577765Z stderr F \\\\t    Price DECIMAL(9,2)  \\\",\\\"saveServiceCopy\\\":true,\\\"logSourceCRN\\\":\\\"crn:v1:bluemix:public:databases-for-postgresql:us-south:a\\/2212c4a700f44505a917e8fcb952c4ce:3720bc70-4a8a-4945-84d6-1efecd3f956e::\\\"}\",\"_rawline\":null,\"_ts\":1693381456997,\"_platform\":\"ibm-cloud-databases-prod\",\"_app\":\"crn:v1:bluemix:public:databases-for-postgresql:us-south:a\\/2212c4a700f44505a917e8fcb952c4ce:3720bc70-4a8a-4945-84d6-1efecd3f956e::\",\"_ipremote\":\"166.9.90.41\",\"message\":\"2023-08-30T07:44:16.544577765Z stderr F \\t    Price DECIMAL(9,2)  \",\"saveServiceCopy\":true,\"logSourceCRN\":\"crn:v1:bluemix:public:databases-for-postgresql:us-south:a\\/2212c4a700f44505a917e8fcb952c4ce:3720bc70-4a8a-4945-84d6-1efecd3f956e::\",\"_mezmo_line_size\":934},{\"_account\":\"b0b64dcdd2\",\"_host\":\"ibm-cloud-databases-prod\",\"_label\":{\"database\":\"postgresql\",\"member\":\"m-0\",\"crn\":\"crn:v1:bluemix:public:databases-for-postgresql:us-south:a\\/2212c4a700f44505a917e8fcb952c4ce:3720bc70-4a8a-4945-84d6-1efecd3f956e::\",\"region\":\"us-south\"},\"_mac\":\"72:20:fd:54:5c:53\",\"_file\":\"crn:v1:bluemix:public:databases-for-postgresql:us-south:a\\/2212c4a700f44505a917e8fcb952c4ce:3720bc70-4a8a-4945-84d6-1efecd3f956e::\",\"_line\":\"{\\\"message\\\":\\\"2023-08-30T07:44:16.544589207Z stderr F \\\\t);\\\",\\\"saveServiceCopy\\\":true,\\\"logSourceCRN\\\":\\\"crn:v1:bluemix:public:databases-for-postgresql:us-south:a\\/2212c4a700f44505a917e8fcb952c4ce:3720bc70-4a8a-4945-84d6-1efecd3f956e::\\\"}\",\"_rawline\":null,\"_ts\":1693381456997,\"_platform\":\"ibm-cloud-databases-prod\",\"_app\":\"crn:v1:bluemix:public:databases-for-postgresql:us-south:a\\/2212c4a700f44505a917e8fcb952c4ce:3720bc70-4a8a-4945-84d6-1efecd3f956e::\",\"_ipremote\":\"166.9.90.41\",\"message\":\"2023-08-30T07:44:16.544589207Z stderr F \\t);\",\"saveServiceCopy\":true,\"logSourceCRN\":\"crn:v1:bluemix:public:databases-for-postgresql:us-south:a\\/2212c4a700f44505a917e8fcb952c4ce:3720bc70-4a8a-4945-84d6-1efecd3f956e::\",\"_mezmo_line_size\":912}]");
		ArrayList<String> stringlist = new ArrayList<>(Arrays.asList("multiline"));
		e.setField("tags", stringlist);
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertEquals(1, results.size());
	}

	@Test
	public void testThirdpartyalreadyexisterrorMultiline() {
		Context context = new ContextImpl(null, null);
		ICDPostgresqlGuardiumFilter filter = new ICDPostgresqlGuardiumFilter("test-id", null, context);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("message",
				"[{\"_account\":\"16a669035f\",\"_host\":\"ibm-cloud-databases-prod\",\"_label\":{\"database\":\"postgresql\",\"member\":\"m-0\",\"crn\":\"crn:v1:bluemix:public:databases-for-postgresql:us-south:a\\/2212c4a700f44505a917e8fcb952c4ce:d03fe2ec-709b-46ae-9a21-d0705b1feca0::\",\"region\":\"us-south\"},\"_mac\":\"56:1a:2b:d5:0e:f6\",\"_file\":\"crn:v1:bluemix:public:databases-for-postgresql:us-south:a\\/2212c4a700f44505a917e8fcb952c4ce:d03fe2ec-709b-46ae-9a21-d0705b1feca0::\",\"_line\":\"{\\\"message\\\":\\\"2023-09-05T06:39:41.161173741Z stderr F 2023-09-05 06:39:41 UTC [psql] [42P07] [583]: [33-1] user=admin,db=test1,client=172.30.175.0 STATEMENT:  CREATE \\\",\\\"saveServiceCopy\\\":true,\\\"logSourceCRN\\\":\\\"crn:v1:bluemix:public:databases-for-postgresql:us-south:a\\/2212c4a700f44505a917e8fcb952c4ce:d03fe2ec-709b-46ae-9a21-d0705b1feca0::\\\"}\",\"_rawline\":null,\"_ts\":1693895982041,\"_platform\":\"ibm-cloud-databases-prod\",\"_app\":\"crn:v1:bluemix:public:databases-for-postgresql:us-south:a\\/2212c4a700f44505a917e8fcb952c4ce:d03fe2ec-709b-46ae-9a21-d0705b1feca0::\",\"_ipremote\":\"166.9.61.79\",\"message\":\"2023-09-05T06:39:41.161173741Z stderr F 2023-09-05 06:39:41 UTC [psql] [42P07] [583]: [33-1] user=admin,db=test1,client=172.30.175.0 STATEMENT:  CREATE \",\"saveServiceCopy\":true,\"logSourceCRN\":\"crn:v1:bluemix:public:databases-for-postgresql:us-south:a\\/2212c4a700f44505a917e8fcb952c4ce:d03fe2ec-709b-46ae-9a21-d0705b1feca0::\",\"_mezmo_line_size\":1038},{\"_account\":\"16a669035f\",\"_host\":\"ibm-cloud-databases-prod\",\"_label\":{\"database\":\"postgresql\",\"member\":\"m-0\",\"crn\":\"crn:v1:bluemix:public:databases-for-postgresql:us-south:a\\/2212c4a700f44505a917e8fcb952c4ce:d03fe2ec-709b-46ae-9a21-d0705b1feca0::\",\"region\":\"us-south\"},\"_mac\":\"56:1a:2b:d5:0e:f6\",\"_file\":\"crn:v1:bluemix:public:databases-for-postgresql:us-south:a\\/2212c4a700f44505a917e8fcb952c4ce:d03fe2ec-709b-46ae-9a21-d0705b1feca0::\",\"_line\":\"{\\\"message\\\":\\\"2023-09-05T06:39:41.161204499Z stderr F \\\\tTABLE\\\",\\\"saveServiceCopy\\\":true,\\\"logSourceCRN\\\":\\\"crn:v1:bluemix:public:databases-for-postgresql:us-south:a\\/2212c4a700f44505a917e8fcb952c4ce:d03fe2ec-709b-46ae-9a21-d0705b1feca0::\\\"}\",\"_rawline\":null,\"_ts\":1693895982041,\"_platform\":\"ibm-cloud-databases-prod\",\"_app\":\"crn:v1:bluemix:public:databases-for-postgresql:us-south:a\\/2212c4a700f44505a917e8fcb952c4ce:d03fe2ec-709b-46ae-9a21-d0705b1feca0::\",\"_ipremote\":\"166.9.61.79\",\"message\":\"2023-09-05T06:39:41.161204499Z stderr F \\tTABLE\",\"saveServiceCopy\":true,\"logSourceCRN\":\"crn:v1:bluemix:public:databases-for-postgresql:us-south:a\\/2212c4a700f44505a917e8fcb952c4ce:d03fe2ec-709b-46ae-9a21-d0705b1feca0::\",\"_mezmo_line_size\":913},{\"_account\":\"16a669035f\",\"_host\":\"ibm-cloud-databases-prod\",\"_label\":{\"database\":\"postgresql\",\"member\":\"m-0\",\"crn\":\"crn:v1:bluemix:public:databases-for-postgresql:us-south:a\\/2212c4a700f44505a917e8fcb952c4ce:d03fe2ec-709b-46ae-9a21-d0705b1feca0::\",\"region\":\"us-south\"},\"_mac\":\"56:1a:2b:d5:0e:f6\",\"_file\":\"crn:v1:bluemix:public:databases-for-postgresql:us-south:a\\/2212c4a700f44505a917e8fcb952c4ce:d03fe2ec-709b-46ae-9a21-d0705b1feca0::\",\"_line\":\"{\\\"message\\\":\\\"2023-09-05T06:39:41.161243672Z stderr F \\\\tMULTILINE\\\",\\\"saveServiceCopy\\\":true,\\\"logSourceCRN\\\":\\\"crn:v1:bluemix:public:databases-for-postgresql:us-south:a\\/2212c4a700f44505a917e8fcb952c4ce:d03fe2ec-709b-46ae-9a21-d0705b1feca0::\\\"}\",\"_rawline\":null,\"_ts\":1693895982041,\"_platform\":\"ibm-cloud-databases-prod\",\"_app\":\"crn:v1:bluemix:public:databases-for-postgresql:us-south:a\\/2212c4a700f44505a917e8fcb952c4ce:d03fe2ec-709b-46ae-9a21-d0705b1feca0::\",\"_ipremote\":\"166.9.61.79\",\"message\":\"2023-09-05T06:39:41.161243672Z stderr F \\tMULTILINE\",\"saveServiceCopy\":true,\"logSourceCRN\":\"crn:v1:bluemix:public:databases-for-postgresql:us-south:a\\/2212c4a700f44505a917e8fcb952c4ce:d03fe2ec-709b-46ae-9a21-d0705b1feca0::\",\"_mezmo_line_size\":917},{\"_account\":\"16a669035f\",\"_host\":\"ibm-cloud-databases-prod\",\"_label\":{\"database\":\"postgresql\",\"member\":\"m-0\",\"crn\":\"crn:v1:bluemix:public:databases-for-postgresql:us-south:a\\/2212c4a700f44505a917e8fcb952c4ce:d03fe2ec-709b-46ae-9a21-d0705b1feca0::\",\"region\":\"us-south\"},\"_mac\":\"56:1a:2b:d5:0e:f6\",\"_file\":\"crn:v1:bluemix:public:databases-for-postgresql:us-south:a\\/2212c4a700f44505a917e8fcb952c4ce:d03fe2ec-709b-46ae-9a21-d0705b1feca0::\",\"_line\":\"{\\\"message\\\":\\\"2023-09-05T06:39:41.161272082Z stderr F \\\\t(Name \\\",\\\"saveServiceCopy\\\":true,\\\"logSourceCRN\\\":\\\"crn:v1:bluemix:public:databases-for-postgresql:us-south:a\\/2212c4a700f44505a917e8fcb952c4ce:d03fe2ec-709b-46ae-9a21-d0705b1feca0::\\\"}\",\"_rawline\":null,\"_ts\":1693895982041,\"_platform\":\"ibm-cloud-databases-prod\",\"_app\":\"crn:v1:bluemix:public:databases-for-postgresql:us-south:a\\/2212c4a700f44505a917e8fcb952c4ce:d03fe2ec-709b-46ae-9a21-d0705b1feca0::\",\"_ipremote\":\"166.9.61.79\",\"message\":\"2023-09-05T06:39:41.161272082Z stderr F \\t(Name \",\"saveServiceCopy\":true,\"logSourceCRN\":\"crn:v1:bluemix:public:databases-for-postgresql:us-south:a\\/2212c4a700f44505a917e8fcb952c4ce:d03fe2ec-709b-46ae-9a21-d0705b1feca0::\",\"_mezmo_line_size\":914},{\"_account\":\"16a669035f\",\"_host\":\"ibm-cloud-databases-prod\",\"_label\":{\"database\":\"postgresql\",\"member\":\"m-0\",\"crn\":\"crn:v1:bluemix:public:databases-for-postgresql:us-south:a\\/2212c4a700f44505a917e8fcb952c4ce:d03fe2ec-709b-46ae-9a21-d0705b1feca0::\",\"region\":\"us-south\"},\"_mac\":\"56:1a:2b:d5:0e:f6\",\"_file\":\"crn:v1:bluemix:public:databases-for-postgresql:us-south:a\\/2212c4a700f44505a917e8fcb952c4ce:d03fe2ec-709b-46ae-9a21-d0705b1feca0::\",\"_line\":\"{\\\"message\\\":\\\"2023-09-05T06:39:41.161292463Z stderr F \\\\t varchar(40));\\\",\\\"saveServiceCopy\\\":true,\\\"logSourceCRN\\\":\\\"crn:v1:bluemix:public:databases-for-postgresql:us-south:a\\/2212c4a700f44505a917e8fcb952c4ce:d03fe2ec-709b-46ae-9a21-d0705b1feca0::\\\"}\",\"_rawline\":null,\"_ts\":1693895982041,\"_platform\":\"ibm-cloud-databases-prod\",\"_app\":\"crn:v1:bluemix:public:databases-for-postgresql:us-south:a\\/2212c4a700f44505a917e8fcb952c4ce:d03fe2ec-709b-46ae-9a21-d0705b1feca0::\",\"_ipremote\":\"166.9.61.79\",\"message\":\"2023-09-05T06:39:41.161292463Z stderr F \\t varchar(40));\",\"saveServiceCopy\":true,\"logSourceCRN\":\"crn:v1:bluemix:public:databases-for-postgresql:us-south:a\\/2212c4a700f44505a917e8fcb952c4ce:d03fe2ec-709b-46ae-9a21-d0705b1feca0::\",\"_mezmo_line_size\":922}]");
		ArrayList<String> stringlist = new ArrayList<>(Arrays.asList("multiline"));
		e.setField("tags", stringlist);
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertEquals(1, results.size());
	}

	@Test
	public void testThirdpartysysntexerrorMultiline() {
		Context context = new ContextImpl(null, null);
		ICDPostgresqlGuardiumFilter filter = new ICDPostgresqlGuardiumFilter("test-id", null, context);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("message",
				"[{\"_account\":\"16a669035f\",\"_host\":\"ibm-cloud-databases-prod\",\"_label\":{\"database\":\"postgresql\",\"member\":\"m-0\",\"crn\":\"crn:v1:bluemix:public:databases-for-postgresql:us-south:a/2212c4a700f44505a917e8fcb952c4ce:d03fe2ec-709b-46ae-9a21-d0705b1feca0::\",\"region\":\"us-south\"},\"_mac\":\"56:1a:2b:d5:0e:f6\",\"_file\":\"crn:v1:bluemix:public:databases-for-postgresql:us-south:a/2212c4a700f44505a917e8fcb952c4ce:d03fe2ec-709b-46ae-9a21-d0705b1feca0::\",\"_line\":\"{\\\"message\\\":\\\"2023-09-05T07:19:31.836061647Z stderr F 2023-09-05 07:19:31 UTC [psql] [42601] [2544]: [5-1] user=admin,db=test1,client=172.30.73.0 STATEMENT:  CREATE \\\",\\\"saveServiceCopy\\\":true,\\\"logSourceCRN\\\":\\\"crn:v1:bluemix:public:databases-for-postgresql:us-south:a/2212c4a700f44505a917e8fcb952c4ce:d03fe2ec-709b-46ae-9a21-d0705b1feca0::\\\"}\",\"_rawline\":null,\"_ts\":1693898372351,\"_platform\":\"ibm-cloud-databases-prod\",\"_app\":\"crn:v1:bluemix:public:databases-for-postgresql:us-south:a/2212c4a700f44505a917e8fcb952c4ce:d03fe2ec-709b-46ae-9a21-d0705b1feca0::\",\"_ipremote\":\"166.9.61.79\",\"message\":\"2023-09-05T07:19:31.836061647Z stderr F 2023-09-05 07:19:31 UTC [psql] [42601] [2544]: [5-1] user=admin,db=test1,client=172.30.73.0 STATEMENT:  CREATE \",\"saveServiceCopy\":true,\"logSourceCRN\":\"crn:v1:bluemix:public:databases-for-postgresql:us-south:a/2212c4a700f44505a917e8fcb952c4ce:d03fe2ec-709b-46ae-9a21-d0705b1feca0::\",\"_mezmo_line_size\":1037}, {\"_account\":\"16a669035f\",\"_host\":\"ibm-cloud-databases-prod\",\"_label\":{\"database\":\"postgresql\",\"member\":\"m-0\",\"crn\":\"crn:v1:bluemix:public:databases-for-postgresql:us-south:a/2212c4a700f44505a917e8fcb952c4ce:d03fe2ec-709b-46ae-9a21-d0705b1feca0::\",\"region\":\"us-south\"},\"_mac\":\"56:1a:2b:d5:0e:f6\",\"_file\":\"crn:v1:bluemix:public:databases-for-postgresql:us-south:a/2212c4a700f44505a917e8fcb952c4ce:d03fe2ec-709b-46ae-9a21-d0705b1feca0::\",\"_line\":\"{\\\"message\\\":\\\"2023-09-05T07:19:31.836077984Z stderr F \\\\tTABL\\\",\\\"saveServiceCopy\\\":true,\\\"logSourceCRN\\\":\\\"crn:v1:bluemix:public:databases-for-postgresql:us-south:a/2212c4a700f44505a917e8fcb952c4ce:d03fe2ec-709b-46ae-9a21-d0705b1feca0::\\\"}\",\"_rawline\":null,\"_ts\":1693898372351,\"_platform\":\"ibm-cloud-databases-prod\",\"_app\":\"crn:v1:bluemix:public:databases-for-postgresql:us-south:a/2212c4a700f44505a917e8fcb952c4ce:d03fe2ec-709b-46ae-9a21-d0705b1feca0::\",\"_ipremote\":\"166.9.61.79\",\"message\":\"2023-09-05T07:19:31.836077984Z stderr F \\tTABL\",\"saveServiceCopy\":true,\"logSourceCRN\":\"crn:v1:bluemix:public:databases-for-postgresql:us-south:a/2212c4a700f44505a917e8fcb952c4ce:d03fe2ec-709b-46ae-9a21-d0705b1feca0::\",\"_mezmo_line_size\":912}, {\"_account\":\"16a669035f\",\"_host\":\"ibm-cloud-databases-prod\",\"_label\":{\"database\":\"postgresql\",\"member\":\"m-0\",\"crn\":\"crn:v1:bluemix:public:databases-for-postgresql:us-south:a/2212c4a700f44505a917e8fcb952c4ce:d03fe2ec-709b-46ae-9a21-d0705b1feca0::\",\"region\":\"us-south\"},\"_mac\":\"56:1a:2b:d5:0e:f6\",\"_file\":\"crn:v1:bluemix:public:databases-for-postgresql:us-south:a/2212c4a700f44505a917e8fcb952c4ce:d03fe2ec-709b-46ae-9a21-d0705b1feca0::\",\"_line\":\"{\\\"message\\\":\\\"2023-09-05T07:19:31.836086167Z stderr F \\\\tMULTILINE\\\",\\\"saveServiceCopy\\\":true,\\\"logSourceCRN\\\":\\\"crn:v1:bluemix:public:databases-for-postgresql:us-south:a/2212c4a700f44505a917e8fcb952c4ce:d03fe2ec-709b-46ae-9a21-d0705b1feca0::\\\"}\",\"_rawline\":null,\"_ts\":1693898372351,\"_platform\":\"ibm-cloud-databases-prod\",\"_app\":\"crn:v1:bluemix:public:databases-for-postgresql:us-south:a/2212c4a700f44505a917e8fcb952c4ce:d03fe2ec-709b-46ae-9a21-d0705b1feca0::\",\"_ipremote\":\"166.9.61.79\",\"message\":\"2023-09-05T07:19:31.836086167Z stderr F \\tMULTILINE\",\"saveServiceCopy\":true,\"logSourceCRN\":\"crn:v1:bluemix:public:databases-for-postgresql:us-south:a/2212c4a700f44505a917e8fcb952c4ce:d03fe2ec-709b-46ae-9a21-d0705b1feca0::\",\"_mezmo_line_size\":917}, {\"_account\":\"16a669035f\",\"_host\":\"ibm-cloud-databases-prod\",\"_label\":{\"database\":\"postgresql\",\"member\":\"m-0\",\"crn\":\"crn:v1:bluemix:public:databases-for-postgresql:us-south:a/2212c4a700f44505a917e8fcb952c4ce:d03fe2ec-709b-46ae-9a21-d0705b1feca0::\",\"region\":\"us-south\"},\"_mac\":\"56:1a:2b:d5:0e:f6\",\"_file\":\"crn:v1:bluemix:public:databases-for-postgresql:us-south:a/2212c4a700f44505a917e8fcb952c4ce:d03fe2ec-709b-46ae-9a21-d0705b1feca0::\",\"_line\":\"{\\\"message\\\":\\\"2023-09-05T07:19:31.836095188Z stderr F \\\\t(Name \\\",\\\"saveServiceCopy\\\":true,\\\"logSourceCRN\\\":\\\"crn:v1:bluemix:public:databases-for-postgresql:us-south:a/2212c4a700f44505a917e8fcb952c4ce:d03fe2ec-709b-46ae-9a21-d0705b1feca0::\\\"}\",\"_rawline\":null,\"_ts\":1693898372351,\"_platform\":\"ibm-cloud-databases-prod\",\"_app\":\"crn:v1:bluemix:public:databases-for-postgresql:us-south:a/2212c4a700f44505a917e8fcb952c4ce:d03fe2ec-709b-46ae-9a21-d0705b1feca0::\",\"_ipremote\":\"166.9.61.79\",\"message\":\"2023-09-05T07:19:31.836095188Z stderr F \\t(Name \",\"saveServiceCopy\":true,\"logSourceCRN\":\"crn:v1:bluemix:public:databases-for-postgresql:us-south:a/2212c4a700f44505a917e8fcb952c4ce:d03fe2ec-709b-46ae-9a21-d0705b1feca0::\",\"_mezmo_line_size\":914}, {\"_account\":\"16a669035f\",\"_host\":\"ibm-cloud-databases-prod\",\"_label\":{\"database\":\"postgresql\",\"member\":\"m-0\",\"crn\":\"crn:v1:bluemix:public:databases-for-postgresql:us-south:a/2212c4a700f44505a917e8fcb952c4ce:d03fe2ec-709b-46ae-9a21-d0705b1feca0::\",\"region\":\"us-south\"},\"_mac\":\"56:1a:2b:d5:0e:f6\",\"_file\":\"crn:v1:bluemix:public:databases-for-postgresql:us-south:a/2212c4a700f44505a917e8fcb952c4ce:d03fe2ec-709b-46ae-9a21-d0705b1feca0::\",\"_line\":\"{\\\"message\\\":\\\"2023-09-05T07:19:31.836104753Z stderr F \\\\t varchar(40));\\\",\\\"saveServiceCopy\\\":true,\\\"logSourceCRN\\\":\\\"crn:v1:bluemix:public:databases-for-postgresql:us-south:a/2212c4a700f44505a917e8fcb952c4ce:d03fe2ec-709b-46ae-9a21-d0705b1feca0::\\\"}\",\"_rawline\":null,\"_ts\":1693898372351,\"_platform\":\"ibm-cloud-databases-prod\",\"_app\":\"crn:v1:bluemix:public:databases-for-postgresql:us-south:a/2212c4a700f44505a917e8fcb952c4ce:d03fe2ec-709b-46ae-9a21-d0705b1feca0::\",\"_ipremote\":\"166.9.61.79\",\"message\":\"2023-09-05T07:19:31.836104753Z stderr F \\t varchar(40));\",\"saveServiceCopy\":true,\"logSourceCRN\":\"crn:v1:bluemix:public:databases-for-postgresql:us-south:a/2212c4a700f44505a917e8fcb952c4ce:d03fe2ec-709b-46ae-9a21-d0705b1feca0::\",\"_mezmo_line_size\":922}]");
		ArrayList<String> stringlist = new ArrayList<>(Arrays.asList("multiline"));
		e.setField("tags", stringlist);
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		assertEquals(1, results.size());
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
