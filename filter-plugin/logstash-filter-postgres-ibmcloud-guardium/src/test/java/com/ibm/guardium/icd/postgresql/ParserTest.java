/*
Copyright IBM Corp. 2023 All rights reserved.
SPDX-License-Identifier: Apache-2.0
*/

package com.ibm.guardium.icd.postgresql;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.ibm.guardium.icd.postgresql.Parser;
import com.ibm.guardium.universalconnector.commons.structures.Record;

import co.elastic.logstash.api.Event;

class ParserTest {

	@Test
	public void testParseRecord() throws Exception {
		Event e = new org.logstash.Event();
		e.setField("user", "admin");
		e.setField("timestamp", "2022-09-05 07:22:23");
		e.setField("timezone", "UTC");
		e.setField("accountId", "2212c4a700f44505a917e8fcb952c4ce");
		e.setField("dbname", "ibmclouddb");
		e.setField("clientIP", "172.30.216.75");
		e.setField("detail", ":f90dcc7d-68aa-4c85-8740-21929b237bfc");
		e.setField("sqlquery", "\\\"create table sst20_report(Roll_No int,Name varchar(20),Marks int);\\\"");
		final Record record = Parser.parseRecord(e);
		assertNotNull(record);
		assertEquals("admin", record.getAppUserName());
		assertEquals("2212c4a700f44505a917e8fcb952c4ce:f90dcc7d-68aa-4c85-8740-21929b237bfc:ibmclouddb",
				record.getDbName());
		assertEquals(1662362543000L, record.getTime().getTimstamp());
	}	
	
	@Test
	public void testParsesql() throws Exception {
		Event e = new org.logstash.Event();
		e.setField("user", "admin");
		e.setField("timestamp", "2022-09-05 07:22:23");
		e.setField("timezone", "UTC");
		e.setField("accountId", "2212c4a700f44505a917e8fcb952c4ce");
		e.setField("dbname", "ibmclouddb");
		e.setField("clientIP", "172.30.216.75");
		e.setField("detail", ":f90dcc7d-68aa-4c85-8740-21929b237bfc");
		e.setField("sqlquery", "\\\"create table sst20_report(Roll_No int,Name varchar(20),Marks int);\\\"");
		final Record record = Parser.parseRecord(e);
		assertNotNull(record);
		assertEquals("create table sst20_report(Roll_No int,Name varchar(20),Marks int);",
				record.getData().getOriginalSqlCommand());
	}

	@Test
	public void testParseNouser() throws Exception {
		Event e = new org.logstash.Event();
		e.setField("user", "");
		e.setField("timestamp", "2022-09-05 07:22:23");
		e.setField("timezone", "UTC");
		e.setField("accountId", "2212c4a700f44505a917e8fcb952c4ce");
		e.setField("dbname", "ibmclouddb");
		e.setField("clientIP", "172.30.216.75");
		e.setField("detail", ":f90dcc7d-68aa-4c85-8740-21929b237bfc");
		e.setField("sqlquery", "\\\"create table sst20_report(Roll_No int,Name varchar(20),Marks int);\\\"");
		final Record record = Parser.parseRecord(e);
		assertNotNull(record);
		assertEquals("", record.getAccessor().getDbUser());
	}

	@Test
	public void testParsenoSQLError() throws Exception {
		Event e = new org.logstash.Event();
		e.setField("user", "admin");
		e.setField("timestamp", "2022-09-06 09:42:18");
		e.setField("timezone", "UTC");
		e.setField("accountId", "2212c4a700f44505a917e8fcb952c4ce");
		e.setField("dbname", "ibmclouddb");
		e.setField("clientIP", "172.30.148.111");
		e.setField("status", null);
		e.setField("statement", "STATEMENT");
		e.setField("detail", ":f90dcc7d-68aa-4c85-8740-21929b237bfc");
		e.setField("ID1", "48p01");
		final Record record = Parser.parseRecord(e);
		assertNotNull(record);
		assertEquals("48p01", record.getException().getDescription());
		assertEquals("SQL_ERROR", record.getException().getExceptionTypeId());
		assertEquals("", record.getException().getSqlString());
	}

	@Test
	public void testParseSQLError() throws Exception {
		Event e = new org.logstash.Event();
		e.setField("user", "admin");
		e.setField("timestamp", "2022-09-06 09:42:18");
		e.setField("timezone", "UTC");
		e.setField("accountId", "2212c4a700f44505a917e8fcb952c4ce");
		e.setField("dbname", "ibmclouddb");
		e.setField("clientIP", "172.30.148.111");
		e.setField("status", "CREATE USER Ankita111 PASSWORD 'Agent0806';");
		e.setField("statement", "STATEMENT");
		e.setField("detail", ":f90dcc7d-68aa-4c85-8740-21929b237bfc");
		e.setField("ID1", "48p01");
		final Record record = Parser.parseRecord(e);
		assertNotNull(record);
		assertEquals("48p01", record.getException().getDescription());
		assertEquals("SQL_ERROR", record.getException().getExceptionTypeId());
		assertEquals("CREATE USER Ankita111 PASSWORD 'Agent0806';", record.getException().getSqlString());
	}

	@Test
	public void testParsenull() throws Exception {
		Event e = new org.logstash.Event();
		e.setField("user", null);
		e.setField("timestamp", "2022-09-06 09:42:18");
		e.setField("timezone", "UTC");
		e.setField("accountId", null);
		e.setField("dbname", null);
		e.setField("clientIP", null);
		e.setField("sqlquery", "drop table sst_report;");
		e.setField("detail", ":f90dcc7d-68aa-4c85-8740-21929b237bfc");
		final Record record = Parser.parseRecord(e);
		assertNotNull(record);
		assertEquals("NA", record.getAccessor().getDbUser());
		assertEquals("", record.getAccessor().getServerHostName());
		assertEquals("PGRS", record.getAccessor().getLanguage());
	}

	@Test
	public void testParsothertime() throws Exception {
		Event e = new org.logstash.Event();
		e.setField("user", "admin");
		e.setField("timestamp", "2023-08-28T08:49:38.200313002Z");
		e.setField("timezone", "UTC");
		e.setField("accountId", "2212c4a700f44505a917e8fcb952c4ce");
		e.setField("dbname", "ibmclouddb");
		e.setField("clientIP", "172.30.216.75");
		e.setField("detail", ":f90dcc7d-68aa-4c85-8740-21929b237bfc");
		e.setField("sqlquery", "\\\"create table sst20_report(Roll_No int,Name varchar(20),Marks int);\\\"");
		final Record record = Parser.parseRecord(e);
		assertNotNull(record);
		assertEquals("create table sst20_report(Roll_No int,Name varchar(20),Marks int);",
				record.getData().getOriginalSqlCommand());
	}

}
