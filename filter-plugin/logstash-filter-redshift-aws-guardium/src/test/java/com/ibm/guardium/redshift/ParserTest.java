package com.ibm.guardium.redshift;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.text.ParseException;

import org.junit.jupiter.api.Test;

import com.ibm.guardium.univer***REMOVED***lconnector.commons.structures.Accessor;
import com.ibm.guardium.univer***REMOVED***lconnector.commons.structures.Data;
import com.ibm.guardium.univer***REMOVED***lconnector.commons.structures.ExceptionRecord;
import com.ibm.guardium.univer***REMOVED***lconnector.commons.structures.Record;
import com.ibm.guardium.univer***REMOVED***lconnector.commons.structures.SessionLocator;
import com.ibm.guardium.univer***REMOVED***lconnector.commons.structures.Time;

import co.elastic.logstash.api.Event;

class ParserTest {
	@Test
	public void testparseAccessor() {
		Record record = new Record();
		record.setDbName("979326520502_guardiumredshift:dev");
		Event e = new org.logstash.Event();
		e.setField("username", "awsuser");
		e.setField("os_version", "Linux 4.14.262-200.489.amzn2.x86_64 amd64");
		final Accessor accessor = Parser.parseAccessor(e, record);
		assertNotNull(accessor);
		assertEquals("awsuser", accessor.getDbUser());
		assertEquals("Linux 4.14.262-200.489.amzn2.x86_64 amd64", accessor.getOsUser());
	}

	@Test
	public void testparsesessionLocator1() {
		Event e = new org.logstash.Event();
		e.setField(RedShiftTags.REMOTEHOST, "223.233.72.13");
		e.setField(RedShiftTags.REMOTEPORT, "31370");
		final SessionLocator sessionLocator = Parser.parseSessionLocator(e);
		assertNotNull(sessionLocator);
		assertEquals("223.233.72.13", sessionLocator.getClientIp());
		assertEquals(31370, sessionLocator.getClientPort());
	}

	@Test
	public void testparsesessionLocator2() {
		Event e = new org.logstash.Event();
		e.setField("remotehost", "::1");
		e.setField("remoteport", "31370");
		final SessionLocator sessionLocator = Parser.parseSessionLocator(e);
		assertNotNull(sessionLocator);
		assertEquals("::1", sessionLocator.getClientIpv6());
		assertEquals(31370, sessionLocator.getClientPort());
	}

	@Test
	public void testParseData() {
		Event e = new org.logstash.Event();
		e.setField(RedShiftTags.SQLQUERY, "SELECT d.datname as Name");
		final Data data = Parser.parseData(e);
		assertNotNull(data);
		assertEquals("SELECT d.datname as Name", data.getOriginalSqlCommand());
	}

	@Test
	public void testparsegetConnTime() {
		Event e = new org.logstash.Event();
		e.setField("day", "Mon");
		e.setField("month", "Mar");
		e.setField("md", "16");
		e.setField("year", "2022");
		e.setField("time", "04:58:51:641");
		final Time contime = Parser.getConnTime(e);
		assertNotNull(contime);
		assertEquals(1647386931641L, contime.getTimstamp());
	}

	@Test
	public void testparsegetTime() {
		Event e = new org.logstash.Event();
		e.setField("timestamp", "2022-03-17T07:34:29.118Z");
		final Time time = Parser.getTime(e);
		assertNotNull(time);
		assertEquals(1647502469118L, time.getTimstamp());
	}

	@Test
	public void testparseException() {
		Event e = new org.logstash.Event();
		e.setField("action", "authentication failure");
		final ExceptionRecord exceptionRecord = Parser.parseException(e);
		assertNotNull(exceptionRecord);
		assertEquals("LOGIN_FAILED", exceptionRecord.getExceptionTypeId());
		assertEquals("LOGIN_FAILED", exceptionRecord.getDescription());

	}

	@Test
	public void testparseRecord_ConnectionLog() throws ParseException {
		Event e = new org.logstash.Event();
		e.setField("pid", "796");
		e.setField("dbname", "dev");
		e.setField("dbprefix", "979326520502_guardiumredshift");
		e.setField("username", "awsuser");
		e.setField("day", "Mon");
		e.setField("month", "Mar");
		e.setField("md", "16");
		e.setField("year", "2022");
		e.setField("time", "04:58:51:641");
		e.setField("action", "authentication failure");
		final Record record = Parser.parseRecord(e);
		assertNotNull(record);
		assertEquals("796", record.getSessionId());
		assertEquals("979326520502_guardiumredshift:dev", record.getDbName());
		assertEquals("awsuser", record.getAppUserName());
		assertEquals(1647386931641L, record.getTime().getTimstamp());
		assertEquals("LOGIN_FAILED", record.getException().getExceptionTypeId());
		assertEquals("LOGIN_FAILED", record.getException().getDescription());

	}

	@Test
	public void testparseRecord_UserActivityLog() throws ParseException {
		Event e = new org.logstash.Event();
		e.setField("pid", "1073979586");
		e.setField("dbname", "dev");
		e.setField("dbprefix", "979326520502_guardiumredshift");
		e.setField("user", "awsuser");
		e.setField("timestamp", "2022-03-17T07:34:29.118Z");
		e.setField("sql_query", "SELECT d.datname as Name");
		final Record record = Parser.parseRecord(e);
		assertNotNull(record);
		assertEquals("1073979586", record.getSessionId());
		assertEquals("979326520502_guardiumredshift:dev", record.getDbName());
		assertEquals("awsuser", record.getAppUserName());
		assertEquals(1647502469118L, record.getTime().getTimstamp());
		assertEquals("SELECT d.datname as Name", record.getData().getOriginalSqlCommand());
	}

	@Test
	public void testparseRecord_NA() throws ParseException {
		Event e = new org.logstash.Event();
		e.setField("pid", "1073979586");
		e.setField("timestamp", "2022-03-17T07:34:29.118Z");
		e.setField("sql_query", "SELECT d.datname as Name");
		final Record record = Parser.parseRecord(e);
		assertNotNull(record);
		assertEquals("1073979586", record.getSessionId());
		assertEquals(1647502469118L, record.getTime().getTimstamp());
		assertEquals("SELECT d.datname as Name", record.getData().getOriginalSqlCommand());
	}

	@Test
	public void testIsipv6() throws ParseException {
		Event e = new org.logstash.Event();
		e.setField("remotehost", "::ffff:136.226.255.29");
		e.setField("remoteport", "31370");
		final SessionLocator sessionLocator = Parser.parseSessionLocator(e);
		assertNotNull(sessionLocator);
		assertEquals("::ffff:136.226.255.29", sessionLocator.getClientIpv6());
		assertEquals(31370, sessionLocator.getClientPort());
	}

}
