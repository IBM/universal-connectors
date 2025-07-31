package com.ibm.guardium.redshift;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.ibm.guardium.universalconnector.commons.structures.Record;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.logstash.plugins.ContextImpl;

import com.ibm.guardium.universalconnector.commons.GuardConstants;

import co.elastic.logstash.api.Context;
import co.elastic.logstash.api.Event;
import co.elastic.logstash.api.FilterMatchListener;
import co.elastic.logstash.api.PluginConfigSpec;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RedShiftGuardiumConnectorTest {

	final static Context context = new ContextImpl(null, null);
	final static RedShiftGuardiumConnector Filter = new RedShiftGuardiumConnector("test-id", null, context);

	@Test
	public void testConfigSchema() {
		RedShiftGuardiumConnector filter = new RedShiftGuardiumConnector("rds", null, context);
		Collection<PluginConfigSpec<?>> events = filter.configSchema();
		Assert.assertNotNull(events);
	}

	@Test
	public void testgetId() {
		RedShiftGuardiumConnector filter = new RedShiftGuardiumConnector("rds", null, context);
		String id = filter.getId();
		Assert.assertNotNull(id);
	}

	@Test
	public void testgetSourceField() {
		RedShiftGuardiumConnector filter = new RedShiftGuardiumConnector("rds", null, context);
		String id = filter.getSourceField();
	}
	@Test
	public void testparseDDLRecord() {
		Context context = new ContextImpl(null, null);
		RedShiftGuardiumConnector filter = new RedShiftGuardiumConnector("rds", null, context);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("pid", "796");
		e.setField("db", "dev");
		e.setField("user", "awsuser");
		e.setField("timestamp", "2021-08-23T16:28:14Z");
		e.setField("dbprefix", "979326520502_guardiumredshift");
		e.setField("serverHostnamePrefix", "979326520502_guardiumredshift");
		e.setField("sql_query",
				"LOG: ALTER DATASHARE salesshare ADD TABLE public.tickit_sales_redshift;");
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		Assert.assertEquals(1, results.size());
		Assert.assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		Assert.assertEquals(1, matchListener.getMatchCount());
	}
	@Test
	public void testparselibrary() {
		Context context = new ContextImpl(null, null);
		RedShiftGuardiumConnector filter = new RedShiftGuardiumConnector("rds", null, context);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("pid", "796");
		e.setField("db", "dev");
		e.setField("user", "awsuser");
		e.setField("timestamp", "2021-08-23T16:28:14Z");
		e.setField("dbprefix", "979326520502_guardiumredshift");
		e.setField("serverHostnamePrefix", "979326520502_guardiumredshift");
		e.setField("sql_query",
				"LOG: create library f_urlparse1\r\n"
						+ "language plpythonu\r\n"
						+ "from 's3://guardiumredshift2/geomatry.zip'\r\n"
						+ "credentials 'aws_iam_role=arn:aws:iam::979326520502:role/myspectrum_role'\r\n"
						+ "region as 'us-east-1';");
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		Assert.assertEquals(1, results.size());
		Assert.assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		Assert.assertEquals(1, matchListener.getMatchCount());
	}

	@Test
	public void testparseCreate() {
		Context context = new ContextImpl(null, null);
		RedShiftGuardiumConnector filter = new RedShiftGuardiumConnector("rds", null, context);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("pid", "796");
		e.setField("db", "dev");
		e.setField("user", "awsuser");
		e.setField("timestamp", "2021-08-23T16:28:14Z");
		e.setField("dbprefix", "979326520502_guardiumredshift");
		e.setField("serverHostnamePrefix", "979326520502_guardiumredshift");
		e.setField("sql_query",
				"LOG: create external table spectrum_schema.spectrum_table(c1 int) stored as parque location 's3://guardiumredshift/myfolder/';");
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		Assert.assertEquals(1, results.size());
		Assert.assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		Assert.assertEquals(1, matchListener.getMatchCount());
	}
	@Test
	public void testparseMINUS() {
		Context context = new ContextImpl(null, null);
		RedShiftGuardiumConnector filter = new RedShiftGuardiumConnector("rds", null, context);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("pid", "796");
		e.setField("db", "dev");
		e.setField("user", "awsuser");
		e.setField("timestamp", "2021-08-23T16:28:14Z");
		e.setField("dbprefix", "979326520502_guardiumredshift");
		e.setField("serverHostnamePrefix", "979326520502_guardiumredshift");
		e.setField("sql_query",
				"LOG: select * from sales1\r\n "
						+ "MINUs\r\n "
						+ "select * from sales2;\r\n");
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		Assert.assertEquals(1, results.size());
		Assert.assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		Assert.assertEquals(1, matchListener.getMatchCount());
	}
	@Test
	public void testUnload() {
		Context context = new ContextImpl(null, null);
		RedShiftGuardiumConnector filter = new RedShiftGuardiumConnector("rds", null, context);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("pid", "796");
		e.setField("db", "dev");
		e.setField("user", "awsuser");
		e.setField("timestamp", "2021-08-23T16:28:14Z");
		e.setField("dbprefix", "979326520502_guardiumredshift");
		e.setField("serverHostnamePrefix", "979326520502_guardiumredshift");
		e.setField("sql_query",
				"LOG: unload ('select * from venue') to 's3://mybucket/unload/' iam_role 'arn:aws:iam::0123456789012:role/MyRedshiftRole';");
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		Assert.assertEquals(1, results.size());
		Assert.assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		Assert.assertEquals(1, matchListener.getMatchCount());
	}
	@Test
	public void testparseVacuum() {
		Context context = new ContextImpl(null, null);
		RedShiftGuardiumConnector filter = new RedShiftGuardiumConnector("rds", null, context);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("pid", "796");
		e.setField("db", "dev");
		e.setField("user", "awsuser");
		e.setField("timestamp", "2021-08-23T16:28:14Z");
		e.setField("dbprefix", "979326520502_guardiumredshift");
		e.setField("serverHostnamePrefix", "979326520502_guardiumredshift");
		e.setField("sql_query",
				"LOG: vacuum sort only sales to 75 percent");
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		Assert.assertEquals(1, results.size());
		Assert.assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		Assert.assertEquals(1, matchListener.getMatchCount());
	}

	@Test
	public void testCopy() {
		Context context = new ContextImpl(null, null);
		RedShiftGuardiumConnector filter = new RedShiftGuardiumConnector("rds", null, context);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("pid", "796");
		e.setField("db", "dev");
		e.setField("user", "awsuser");
		e.setField("timestamp", "2021-08-23T16:28:14Z");
		e.setField("dbprefix", "979326520502_guardiumredshift");
		e.setField("serverHostnamePrefix", "979326520502_guardiumredshift");
		e.setField("sql_query",
				"LOG: copy category "
						+ "from 's3://mybucket/custdata' \r\n"
						+ "iam_role 'arn:aws:iam::0123456789012:role/MyRedshiftRole'");
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		Assert.assertEquals(1, results.size());
		Assert.assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		Assert.assertEquals(1, matchListener.getMatchCount());
	}
	@Test
	public void testSelectTop() {
		Context context = new ContextImpl(null, null);
		RedShiftGuardiumConnector filter = new RedShiftGuardiumConnector("rds", null, context);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("pid", "796");
		e.setField("db", "dev");
		e.setField("user", "awsuser");
		e.setField("timestamp", "2021-08-23T16:28:14Z");
		e.setField("dbprefix", "979326520502_guardiumredshift");
		e.setField("serverHostnamePrefix", "979326520502_guardiumredshift");
		e.setField("sql_query",
				"LOG: select top 10 * from sales;");
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		Assert.assertEquals(1, results.size());
		Assert.assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		Assert.assertEquals(1, matchListener.getMatchCount());
	}
	@Test
	public void testCancel() {
		Context context = new ContextImpl(null, null);
		RedShiftGuardiumConnector filter = new RedShiftGuardiumConnector("rds", null, context);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("pid", "796");
		e.setField("db", "dev");
		e.setField("user", "awsuser");
		e.setField("timestamp", "2021-08-23T16:28:14Z");
		e.setField("dbprefix", "979326520502_guardiumredshift");
		e.setField("serverHostnamePrefix", "979326520502_guardiumredshift");
		e.setField("sql_query",
				"LOG: cancel 802");
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		Assert.assertEquals(1, results.size());
		Assert.assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		Assert.assertEquals(1, matchListener.getMatchCount());
	}
	@Test
	public void testparse_connectionLog() {
		Context context = new ContextImpl(null, null);
		RedShiftGuardiumConnector filter = new RedShiftGuardiumConnector("rds", null, context);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("pid", "796");
		e.setField("action", "authentication failure");
		e.setField("dbname", "dev");
		e.setField("dbprefix", "979326520502:guardiumredshift");
		e.setField("username", "awsuser");
		e.setField("remotehost", "::1");
		e.setField("remoteport", "31370");
		e.setField("day", "Mon");
		e.setField("month", "Aug");
		e.setField("md", "16");
		e.setField("year", "2021");
		e.setField("time", "06:02:38:217");
		e.setField("os_version", "Linux 4.14.262-200.489.amzn2.x86_64 amd64");
		e.setField("serverHostnamePrefix", "979326520502-guardiumredshift");

		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		Assert.assertEquals(1, results.size());
		Assert.assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		Assert.assertEquals(1, matchListener.getMatchCount());
	}


	@Test
	public void testparse_connectionLogFail() {
		Context context = new ContextImpl(null, null);
		RedShiftGuardiumConnector filter = new RedShiftGuardiumConnector("rds", null, context);
		List<Event> events = new ArrayList<>();
		Event e = new org.logstash.Event();
		events.add(e);
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("pid", "796");
		e.setField("action", "authenticated");
		e.setField("dbname", "dev");
		e.setField("dbprefix", "979326520502_guardiumredshift");
		e.setField("username", "awsuser");
		e.setField("remotehost", "::1");
		e.setField("remoteport", "31370");
		e.setField("day", "Mon");
		e.setField("month", "Aug");
		e.setField("md", "16");
		e.setField("year", "2021");
		e.setField("time", "06:02:38:217");
		e.setField("os_version", "Linux 4.14.262-200.489.amzn2.x86_64 amd64");
		e.setField("serverHostnamePrefix", "979326520502_guardiumredshift");

		Collection<Event> results = filter.filter(events, matchListener);
		Assert.assertEquals(0, results.size());
		Assert.assertNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		Assert.assertEquals(0, matchListener.getMatchCount());
	}

	@Test
	public void testparse_clientIP() {
		Context context = new ContextImpl(null, null);
		RedShiftGuardiumConnector filter = new RedShiftGuardiumConnector("rds", null, context);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("pid", "796");
		e.setField("dbname", "dev");
		e.setField("action", "authentication failure");
		e.setField("dbprefix", "979326520502_guardiumredshift");
		e.setField("username", "awsuser");
		e.setField("remotehost", "223.233.72.13");
		e.setField("remoteport", "31370");
		e.setField("day", "Mon");
		e.setField("month", "Aug");
		e.setField("md", "16");
		e.setField("year", "2021");
		e.setField("time", "06:02:38:217");
		e.setField("os_version", "Linux 4.14.262-200.489.amzn2.x86_64 amd64");
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		Assert.assertEquals(1, results.size());
		Assert.assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		Assert.assertEquals(1, matchListener.getMatchCount());
	}

	@Test
	public void testparseRecord() {
		Context context = new ContextImpl(null, null);
		RedShiftGuardiumConnector filter = new RedShiftGuardiumConnector("rds", null, context);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("pid", "796");
		e.setField("db", "dev");
		e.setField("user", "awsuser");
		e.setField("timestamp", "2021-08-23T16:28:14Z");
		e.setField("dbprefix", "979326520502_guardiumredshift");
		e.setField("serverHostnamePrefix", "979326520502_guardiumredshift");
		e.setField("sql_query",
				"LOG: CREATE TABLE ABC (\r\n" + "PersonID int,\r\n" + "LastName varchar(255),\r\n"
						+ "FirstName varchar(255),\r\n" + "Address varchar(255),\r\n" + "TeamName varchar(255)\r\n"
						+ ");");
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		Assert.assertEquals(1, results.size());
		Assert.assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		Assert.assertEquals(1, matchListener.getMatchCount());
	}
	@Test
	public void testparseRecord1() {
		Context context = new ContextImpl(null, null);
		RedShiftGuardiumConnector filter = new RedShiftGuardiumConnector("rds", null, context);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("pid", "796");
		e.setField("db", "dev");
		e.setField("user", "awsuser");
		e.setField("timestamp", "2021-08-23T16:28:14Z");
		e.setField("dbprefix", "979326520502_guardiumredshift");
		e.setField("serverHostnamePrefix", "979326520502_guardiumredshift");
		e.setField("sql_query",
				"LOG: CREATE TABLE T1 ( ��col1 Varchar(20) distkey sortkey );");
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		Assert.assertEquals(1, results.size());
		Assert.assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		Assert.assertEquals(1, matchListener.getMatchCount());
	}



	@Test
	public void testparseRecord_Username() {
		Context context = new ContextImpl(null, null);
		RedShiftGuardiumConnector filter = new RedShiftGuardiumConnector("rds", null, context);
		ArrayList<Event> events = new ArrayList<>();
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("pid", "796");
		e.setField("dbname", "dev");
		e.setField("dbprefix", "979326520502_guardiumredshift");
		e.setField("user", "rdsdb");
		e.setField("timestamp", "2021-08-23T16:28:14Z");
		e.setField("sql_query", "LOG: SELECT d.datname as Name");
		events.add(e);
		Collection<Event> results = filter.filter(events, matchListener);
		Assert.assertNotNull(results);
	}

	@Test
	public void testparseSessionID_Invalid() {
		Context context = new ContextImpl(null, null);
		RedShiftGuardiumConnector filter = new RedShiftGuardiumConnector("rds", null, context);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("pid", "796");
		e.setField("dbname", "dev");
		e.setField("dbprefix", "979326520502_guardiumredshift");
		e.setField("user", "awsuser");
		e.setField("timestamp", "2021-08-23T16:28:14Z");
		e.setField("sql_query", "LOG: SELECT d.datname as Name");
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		Assert.assertEquals(1, results.size());
		Assert.assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		Assert.assertEquals(1, matchListener.getMatchCount());
	}

	@Test
	public void testparseUsername_invalid() {
		Context context = new ContextImpl(null, null);
		RedShiftGuardiumConnector filter = new RedShiftGuardiumConnector("rds", null, context);
		ArrayList<Event> events = new ArrayList<>();
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("pid", "796");
		e.setField("dbname", "dev");
		e.setField("dbprefix", "979326520502_guardiumredshift");
		e.setField("timestamp", "2021-08-23T16:28:14Z");
		e.setField("sql_query", "LOG: SELECT d.datname as Name");
		events.add(e);
		Collection<Event> results = filter.filter(events, matchListener);
		Assert.assertEquals(0, results.size());
	}

	@Test
	public void testparseException() {
		Context context = new ContextImpl(null, null);
		RedShiftGuardiumConnector filter = new RedShiftGuardiumConnector("rds", null, context);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("dbname", "dev");
		e.setField("dbprefix", "979326520502_guardiumredshift");
		e.setField("username", "awsuser");
		e.setField("day", "Mon");
		e.setField("month", "Mar");
		e.setField("md", "15");
		e.setField("year", "2022");
		e.setField("time", "08:18:14:766");
		e.setField("action", "authentication failure");
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		Assert.assertEquals(1, results.size());
	}

	@Test
	public void testparse_noMessage() {
		Context context = new ContextImpl(null, null);
		RedShiftGuardiumConnector filter = new RedShiftGuardiumConnector("rds", null, context);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("dbname", "dev");
		e.setField("action", "authentication failure");
		e.setField("dbprefix", "979326520502_guardiumredshift");
		e.setField("username", "awsuser");
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		Assert.assertEquals(1, results.size());
	}

	@Test
	public void testIsipv6() {
		Context context = new ContextImpl(null, null);
		RedShiftGuardiumConnector filter = new RedShiftGuardiumConnector("rds", null, context);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("pid", "796");
		e.setField("dbname", "dev");
		e.setField("action", "authentication failure");
		e.setField("dbprefix", "979326520502_guardiumredshift");
		e.setField("username", "awsuser");
		e.setField("day", "Mon");
		e.setField("month", "Aug");
		e.setField("md", "16");
		e.setField("year", "2021");
		e.setField("time", "06:02:38:217");
		e.setField("os_version", "Linux 4.14.262-200.489.amzn2.x86_64 amd64");
		e.setField("serverHostnamePrefix", "979326520502_guardiumredshift");
		e.setField("remotehost", "::ffff:136.226.255.29");
		e.setField("remoteport", "31370");
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		Assert.assertEquals(1, results.size());
		Assert.assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		Assert.assertEquals(1, matchListener.getMatchCount());
	}
	@Test
	public void testSelectPIVOT() {
		Context context = new ContextImpl(null, null);
		RedShiftGuardiumConnector filter = new RedShiftGuardiumConnector("rds", null, context);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("pid", "796");
		e.setField("dbname", "dev");
		e.setField("user", "awsuser");
		e.setField("timestamp", "2021-08-23T16:28:14Z");
		e.setField("dbprefix", "979326520502_guardiumredshift");
		e.setField("serverHostnamePrefix", "979326520502_guardiumredshift");
		e.setField("sql_query",
				"LOG: SELECT *\r\n"
						+ "FROM (SELECT partname, price FROM part) PIVOT (\r\n"
						+ "    AVG(price) FOR partname IN ('P1', 'P2', 'P3')\r\n"
						+ ");");
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		Assert.assertEquals(1, results.size());
		Assert.assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		Assert.assertEquals(1, matchListener.getMatchCount());
	}
	@Test
	public void testSelectUNPIVOT() {
		Context context = new ContextImpl(null, null);
		RedShiftGuardiumConnector filter = new RedShiftGuardiumConnector("rds", null, context);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("pid", "796");
		e.setField("dbname", "dev");
		e.setField("user", "awsuser");
		e.setField("timestamp", "2021-08-23T16:28:14Z");
		e.setField("dbprefix", "979326520502_guardiumredshift");
		e.setField("serverHostnamePrefix", "979326520502_guardiumredshift");
		e.setField("sql_query",
				"LOG: SELECT * \r\n "
						+ "FROM (SELECT red, green, blue FROM count_by_color) UNPIVOT (\r\n"
						+ "    cnt FOR color IN (red, green, blue)\r\n"
						+ ");");
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		Assert.assertEquals(1, results.size());
		Assert.assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		Assert.assertEquals(1, matchListener.getMatchCount());
	}

	@Test
	public void testifExists() {
		Context context = new ContextImpl(null, null);
		RedShiftGuardiumConnector filter = new RedShiftGuardiumConnector("rds", null, context);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("pid", "796");
		e.setField("db", "dev");
		e.setField("user", "awsuser");
		e.setField("timestamp", "2021-08-23T16:28:14Z");
		e.setField("dbprefix", "979326520502_guardiumredshift");
		e.setField("serverHostnamePrefix", "979326520502_guardiumredshift");
		e.setField("sql_query",
				"LOG: create  table if exists cities( cityid integer not null, city varchar(100) not null,\r\n"
						+ "state char(2) not null);");
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		Assert.assertEquals(1, results.size());
		Assert.assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		Assert.assertEquals(1, matchListener.getMatchCount());
	}
	@Test
	public void testifExists1() {
		Context context = new ContextImpl(null, null);
		RedShiftGuardiumConnector filter = new RedShiftGuardiumConnector("rds", null, context);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("pid", "796");
		e.setField("db", "dev");
		e.setField("user", "awsuser");
		e.setField("timestamp", "2021-08-23T16:28:14Z");
		e.setField("dbprefix", "979326520502_guardiumredshift");
		e.setField("serverHostnamePrefix", "979326520502_guardiumredshift");
		e.setField("sql_query",
				"LOG: DROP MODEL IF EXISTS remote_customer_churn;");
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		Assert.assertEquals(1, results.size());
		Assert.assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		Assert.assertEquals(1, matchListener.getMatchCount());
	}
	@Test
	public void testCreateFunction() {
		Context context = new ContextImpl(null, null);
		RedShiftGuardiumConnector filter = new RedShiftGuardiumConnector("rds", null, context);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("pid", "796");
		e.setField("db", "dev");
		e.setField("user", "awsuser");
		e.setField("timestamp", "2021-08-23T16:28:14Z");
		e.setField("dbprefix", "979326520502_guardiumredshift");
		e.setField("serverHostnamePrefix", "979326520502_guardiumredshift");
		e.setField("sql_query",
				"LOG: create function f_sqlTest (a int, b int )\r\n"
						+ "returns int\r\n"
						+ "stable\r\n"
						+ "as $$\r\n"
						+ "select 1\r\n"
						+ "$$ language sql");
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		Assert.assertEquals(1, results.size());
		Assert.assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		Assert.assertEquals(1, matchListener.getMatchCount());
	}
	@Test
	public void testCreateExternalSchema() {
		Context context = new ContextImpl(null, null);
		RedShiftGuardiumConnector filter = new RedShiftGuardiumConnector("rds", null, context);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("pid", "796");
		e.setField("db", "dev");
		e.setField("user", "awsuser");
		e.setField("timestamp", "2021-08-23T16:28:14Z");
		e.setField("dbprefix", "979326520502_guardiumredshift");
		e.setField("serverHostnamePrefix", "979326520502_guardiumredshift");
		e.setField("sql_query",
				"LOG: CREATE EXTERNAL SCHEMA example_schema FROM"
						+ "DATA CATALOG\r\n "
						+ "DATABASE 'dev'\r\n"
						+ "REGION 'us-east-1'\r\n"
						+ "IAM_ROLE 'arn:aws:iam::979326520502:role/myspectrum_role'\r\n"
						+ "CREATE EXTERNAL DATABASE IF NOT EXISTS;");
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		Assert.assertEquals(1, results.size());
		Assert.assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		Assert.assertEquals(1, matchListener.getMatchCount());
	}
	@Test
	public void testCreateExternalSchema1() {
		Context context = new ContextImpl(null, null);
		RedShiftGuardiumConnector filter = new RedShiftGuardiumConnector("rds", null, context);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("pid", "796");
		e.setField("db", "dev");
		e.setField("user", "awsuser");
		e.setField("timestamp", "2021-08-23T16:28:14Z");
		e.setField("dbprefix", "979326520502_guardiumredshift");
		e.setField("serverHostnamePrefix", "979326520502_guardiumredshift");
		e.setField("sql_query",
				"LOG: CREATE EXTERNAL TABLE external_schema.external_table (\r\n"
						+ "  col1 SMALLINT,\r\n"
						+ "  col2 CHAR(1)\r\n"
						+ ")");
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		Assert.assertEquals(1, results.size());
		Assert.assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		Assert.assertEquals(1, matchListener.getMatchCount());
	}
	@Test
	public void testAlterDatabase() {
		Context context = new ContextImpl(null, null);
		RedShiftGuardiumConnector filter = new RedShiftGuardiumConnector("rds", null, context);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("pid", "796");
		e.setField("db", "dev");
		e.setField("user", "awsuser");
		e.setField("timestamp", "2021-08-23T16:28:14Z");
		e.setField("dbprefix", "979326520502:guardiumredshift");
		e.setField("serverHostnamePrefix", "979326520502-guardiumredshift");
		e.setField("sql_query",
				"LOG: alter database tickit_sandbox rename to tickit_test;");
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		Assert.assertEquals(1, results.size());
		Assert.assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		Assert.assertEquals(1, matchListener.getMatchCount());
	}
	@Test
	public void testAlterDatabase2() {
		Context context = new ContextImpl(null, null);
		RedShiftGuardiumConnector filter = new RedShiftGuardiumConnector("rds", null, context);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("pid", "796");
		e.setField("db", "dev");
		e.setField("user", "awsuser");
		e.setField("timestamp", "2021-08-23T16:28:14Z");
		e.setField("dbprefix", "979326520502:guardiumredshift");
		e.setField("serverHostnamePrefix", "979326520502-guardiumredshift");
		e.setField("sql_query",
				"LOG: ALTER DATABASE sampledb ISOLATION LEVEL SNAPSHOT;");
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		Assert.assertEquals(1, results.size());
		Assert.assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		Assert.assertEquals(1, matchListener.getMatchCount());
	}
	@Test
	public void testCreateMaterializedView() {
		Context context = new ContextImpl(null, null);
		RedShiftGuardiumConnector filter = new RedShiftGuardiumConnector("rds", null, context);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("pid", "796");
		e.setField("db", "dev");
		e.setField("user", "awsuser");
		e.setField("timestamp", "2021-08-23T16:28:14Z");
		e.setField("dbprefix", "979326520502:guardiumredshift");
		e.setField("serverHostnamePrefix", "979326520502-guardiumredshift");
		e.setField("sql_query",
				"LOG: CREATE MATERIALIZED VIEW tickets_mv AS\r\n"
						+ "    select   catgroup,\r\n"
						+ "    sum(qtysold) as sold\r\n"
						+ "    from     category c, event e, sales s\r\n"
						+ "    where    c.catid = e.catid\r\n"
						+ "    and      e.eventid = s.eventid\r\n"
						+ "    group by catgroup;");
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		Assert.assertEquals(1, results.size());
		Assert.assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		Assert.assertEquals(1, matchListener.getMatchCount());
	}

	@Test
	public void testCreateMaterializedView2() {
		Context context = new ContextImpl(null, null);
		RedShiftGuardiumConnector filter = new RedShiftGuardiumConnector("rds", null, context);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("pid", "796");
		e.setField("db", "dev");
		e.setField("user", "awsuser");
		e.setField("timestamp", "2021-08-23T16:28:14Z");
		e.setField("dbprefix", "979326520502:guardiumredshift");
		e.setField("serverHostnamePrefix", "979326520502-guardiumredshift");
		e.setField("sql_query",
				"LOG: CREATE MATERIALIZED VIEW mv_sales_vw as\r\n"
						+ "select salesid, qtysold, pricepaid, commission, saletime from public.sales\r\n"
						+ "union all\r\n"
						+ "select salesid, qtysold, pricepaid, commission, saletime from spectrum.sales;");
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		Assert.assertEquals(1, results.size());
		Assert.assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		Assert.assertEquals(1, matchListener.getMatchCount());
	}
	@Test
	public void testCreateMaterializedView3() {
		Context context = new ContextImpl(null, null);
		RedShiftGuardiumConnector filter = new RedShiftGuardiumConnector("rds", null, context);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("pid", "796");
		e.setField("db", "dev");
		e.setField("user", "awsuser");
		e.setField("timestamp", "2021-08-23T16:28:14Z");
		e.setField("dbprefix", "979326520502:guardiumredshift");
		e.setField("serverHostnamePrefix", "979326520502-guardiumredshift");
		e.setField("sql_query",
				"LOG: create materialized view mv_sales_vw as select a from t;");
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		Assert.assertEquals(1, results.size());
		Assert.assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		Assert.assertEquals(1, matchListener.getMatchCount());
	}

	@Test
	public void testDropMaterializedView() {
		Context context = new ContextImpl(null, null);
		RedShiftGuardiumConnector filter = new RedShiftGuardiumConnector("rds", null, context);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("pid", "796");
		e.setField("db", "dev");
		e.setField("user", "awsuser");
		e.setField("timestamp", "2021-08-23T16:28:14Z");
		e.setField("dbprefix", "979326520502:guardiumredshift");
		e.setField("serverHostnamePrefix", "979326520502-guardiumredshift");
		e.setField("sql_query",
				"LOG: DROP MATERIALIZED VIEW  IF EXISTS  mv_name;");
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		Assert.assertEquals(1, results.size());
		Assert.assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		Assert.assertEquals(1, matchListener.getMatchCount());
	}
	@Test
	public void testRefreshMaterializedView() {
		Context context = new ContextImpl(null, null);
		RedShiftGuardiumConnector filter = new RedShiftGuardiumConnector("rds", null, context);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("pid", "796");
		e.setField("db", "dev");
		e.setField("user", "awsuser");
		e.setField("timestamp", "2021-08-23T16:28:14Z");
		e.setField("dbprefix", "979326520502:guardiumredshift");
		e.setField("serverHostnamePrefix", "979326520502-guardiumredshift");
		e.setField("sql_query",
				"LOG: REFRESH MATERIALIZED VIEW tickets_mv;");
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		Assert.assertEquals(1, results.size());
		Assert.assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		Assert.assertEquals(1, matchListener.getMatchCount());
	}
	@Test
	public void testAlterMaterializedView() {
		Context context = new ContextImpl(null, null);
		RedShiftGuardiumConnector filter = new RedShiftGuardiumConnector("rds", null, context);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("pid", "796");
		e.setField("db", "dev");
		e.setField("user", "awsuser");
		e.setField("timestamp", "2021-08-23T16:28:14Z");
		e.setField("dbprefix", "979326520502:guardiumredshift");
		e.setField("serverHostnamePrefix", "979326520502-guardiumredshift");
		e.setField("sql_query",
				"LOG: ALTER MATERIALIZED VIEW tickets_mv AUTO REFRESH YES");
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		Assert.assertEquals(1, results.size());
		Assert.assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		Assert.assertEquals(1, matchListener.getMatchCount());
	}
	@Test
	public void testCreateRole() {
		Context context = new ContextImpl(null, null);
		RedShiftGuardiumConnector filter = new RedShiftGuardiumConnector("rds", null, context);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("pid", "796");
		e.setField("db", "dev");
		e.setField("user", "awsuser");
		e.setField("timestamp", "2021-08-23T16:28:14Z");
		e.setField("dbprefix", "979326520502:guardiumredshift");
		e.setField("serverHostnamePrefix", "979326520502-guardiumredshift");
		e.setField("sql_query",
				"LOG: CREATE ROLE sample_role1;");
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		Assert.assertEquals(1, results.size());
		Assert.assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		Assert.assertEquals(1, matchListener.getMatchCount());
	}
	@Test
	public void testCreateRole2() {
		Context context = new ContextImpl(null, null);
		RedShiftGuardiumConnector filter = new RedShiftGuardiumConnector("rds", null, context);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("pid", "796");
		e.setField("db", "dev");
		e.setField("user", "awsuser");
		e.setField("timestamp", "2021-08-23T16:28:14Z");
		e.setField("dbprefix", "979326520502:guardiumredshift");
		e.setField("serverHostnamePrefix", "979326520502-guardiumredshift");
		e.setField("sql_query",
				"LOG: CREATE ROLE sample_role1 EXTERNALID \"ABC123\";");
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		Assert.assertEquals(1, results.size());
		Assert.assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		Assert.assertEquals(1, matchListener.getMatchCount());
	}
	@Test
	public void testDropRole() {
		Context context = new ContextImpl(null, null);
		RedShiftGuardiumConnector filter = new RedShiftGuardiumConnector("rds", null, context);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("pid", "796");
		e.setField("db", "dev");
		e.setField("user", "awsuser");
		e.setField("timestamp", "2021-08-23T16:28:14Z");
		e.setField("dbprefix", "979326520502:guardiumredshift");
		e.setField("serverHostnamePrefix", "979326520502-guardiumredshift");
		e.setField("sql_query",
				"LOG: DROP ROLE sample_role FORCE;");
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		Assert.assertEquals(1, results.size());
		Assert.assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		Assert.assertEquals(1, matchListener.getMatchCount());
	}
	@Test
	public void testAlterRole1() {
		Context context = new ContextImpl(null, null);
		RedShiftGuardiumConnector filter = new RedShiftGuardiumConnector("rds", null, context);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("pid", "796");
		e.setField("db", "dev");
		e.setField("user", "awsuser");
		e.setField("timestamp", "2021-08-23T16:28:14Z");
		e.setField("dbprefix", "979326520502:guardiumredshift");
		e.setField("serverHostnamePrefix", "979326520502-guardiumredshift");
		e.setField("sql_query",
				"LOG: ALTER ROLE sample_role1 WITH RENAME TO sample_role2;");
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		Assert.assertEquals(1, results.size());
		Assert.assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		Assert.assertEquals(1, matchListener.getMatchCount());
	}
	@Test
	public void testAlterRole2() {
		Context context = new ContextImpl(null, null);
		RedShiftGuardiumConnector filter = new RedShiftGuardiumConnector("rds", null, context);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("pid", "796");
		e.setField("db", "dev");
		e.setField("user", "awsuser");
		e.setField("timestamp", "2021-08-23T16:28:14Z");
		e.setField("dbprefix", "979326520502:guardiumredshift");
		e.setField("serverHostnamePrefix", "979326520502-guardiumredshift");
		e.setField("sql_query",
				"LOG: ALTER ROLE sample_role1 EXTERNALID TO \"XYZ456\";");
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		Assert.assertEquals(1, results.size());
		Assert.assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		Assert.assertEquals(1, matchListener.getMatchCount());
	}
	@Test
	public void testCreateFunction1() {
		Context context = new ContextImpl(null, null);
		RedShiftGuardiumConnector filter = new RedShiftGuardiumConnector("rds", null, context);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("pid", "796");
		e.setField("db", "dev");
		e.setField("user", "awsuser");
		e.setField("timestamp", "2021-08-23T16:28:14Z");
		e.setField("dbprefix", "979326520502:guardiumredshift");
		e.setField("serverHostnamePrefix", "979326520502-guardiumredshift");
		e.setField("sql_query",
				"LOG: CREATE OR REPLACE function f_sql_commission (float, float )\r\n"
						+ "  returns float\r\n"
						+ "stable\r\n"
						+ "as $$\r\n"
						+ "  select f_sql_greater ($1, $2)  \r\n"
						+ "$$ language sql;");
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		Assert.assertEquals(1, results.size());
		Assert.assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		Assert.assertEquals(1, matchListener.getMatchCount());
	}
	@Test
	public void testCreateFunction2() {
		Context context = new ContextImpl(null, null);
		RedShiftGuardiumConnector filter = new RedShiftGuardiumConnector("rds", null, context);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("pid", "796");
		e.setField("db", "dev");
		e.setField("user", "awsuser");
		e.setField("timestamp", "2021-08-23T16:28:14Z");
		e.setField("dbprefix", "979326520502:guardiumredshift");
		e.setField("serverHostnamePrefix", "979326520502-guardiumredshift");
		e.setField("sql_query",
				"LOG: create function f_sql_greater (float, float)\r\n"
						+ "  returns float\r\n"
						+ "stable\r\n"
						+ "as $$\r\n"
						+ "  select case when $1 > $2 then $1\r\n"
						+ "    else $2\r\n"
						+ "  end\r\n"
						+ "$$ language sql;");
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		Assert.assertEquals(1, results.size());
		Assert.assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		Assert.assertEquals(1, matchListener.getMatchCount());
	}
	@Test
	public void testDropFunction1() {
		Context context = new ContextImpl(null, null);
		RedShiftGuardiumConnector filter = new RedShiftGuardiumConnector("rds", null, context);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("pid", "796");
		e.setField("db", "dev");
		e.setField("user", "awsuser");
		e.setField("timestamp", "2021-08-23T16:28:14Z");
		e.setField("dbprefix", "979326520502:guardiumredshift");
		e.setField("serverHostnamePrefix", "979326520502-guardiumredshift");
		e.setField("sql_query",
				"LOG: drop function f_sqrt(int);");
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		Assert.assertEquals(1, results.size());
		Assert.assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		Assert.assertEquals(1, matchListener.getMatchCount());
	}
	@Test
	public void testDropFunction2() {
		Context context = new ContextImpl(null, null);
		RedShiftGuardiumConnector filter = new RedShiftGuardiumConnector("rds", null, context);
		Event e = new org.logstash.Event();
		TestMatchListener matchListener = new TestMatchListener();
		e.setField("pid", "796");
		e.setField("db", "dev");
		e.setField("user", "awsuser");
		e.setField("timestamp", "2021-08-23T16:28:14Z");
		e.setField("dbprefix", "979326520502:guardiumredshift");
		e.setField("serverHostnamePrefix", "979326520502-guardiumredshift");
		e.setField("sql_query",
				"LOG: drop function f_sqrt(int)restrict;");
		Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
		Assert.assertEquals(1, results.size());
		Assert.assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
		Assert.assertEquals(1, matchListener.getMatchCount());
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