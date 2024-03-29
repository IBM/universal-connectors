/*
Copyright IBM Corp. 2024 All rights reserved.
SPDX-License-Identifier: Apache-2.0
*/

package com.ibm.guardium.scylladb;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import com.ibm.guardium.universalconnector.commons.structures.Record;
import co.elastic.logstash.api.Event;


public class ParserTest {

	@Test
	public void testParseRecord() throws Exception {
		Event e = new org.logstash.Event();
		e.setField("username", "\"cassandra\"");
		e.setField("keyspace_name", "\"mykeyspace\"");
		e.setField("server_ip", "\"172.31.57.239\"");
		e.setField("client_ip", "\"9.43.118.110\"");
		e.setField("category", "\"DDL\"");
		e.setField("operation", "\"CREATE TABLE SUBRATO(ID INT PRIMARY KEY);\"");
		e.setField("error", "\"false\"");
		e.setField("timestamp", "2024-01-10T02:35:58.000Z");
		e.setField("logsource", "DWP-5CD212FV5L");
		
		final Record record = Parser.parseRecord(e);
		assertNotNull(record);
		assertEquals("mykeyspace",record.getDbName());
		assertEquals("172.31.57.239",record.getSessionLocator().getServerIp());
		assertEquals("9.43.118.110",record.getSessionLocator().getClientIp());
		assertEquals(Long.parseLong("1704854158000"),record.getTime().getTimstamp());
		assertEquals("DWP-5CD212FV5L",record.getAccessor().getServerHostName());
		assertEquals("CREATE TABLE SUBRATO(ID INT PRIMARY KEY);", record.getData().getOriginalSqlCommand());
	}
	
	@Test
	public void testParsesql() throws Exception {
		Event e = new org.logstash.Event();
		e.setField("username", "\"cassandra\"");
		e.setField("keyspace_name", "\"mykeyspace\"");
		e.setField("server_ip", "\"172.31.57.239\"");
		e.setField("category", "\"DML\"");
		e.setField("operation", "\"USE mykeyspace;\"");
		e.setField("error", "\"false\"");
		e.setField("timestamp", "2024-01-10T02:35:58.000Z");
		e.setField("logsource", "DWP-5CD212FV5L");
		final Record record = Parser.parseRecord(e);
		assertNotNull(record);
		assertEquals("USE mykeyspace;", record.getData().getOriginalSqlCommand());
	}
	
	@Test
	public void testParseuser() throws Exception {
		Event e = new org.logstash.Event();
		e.setField("username", "\"alice1\"");
		e.setField("keyspace_name", "\"mykeyspace\"");
		e.setField("server_ip", "\"172.31.57.239\"");
		e.setField("category", "\"DCL\"");
		e.setField("operation", "\"ALTER ROLE alice1 WITH PASSWORD = '***' AND SUPERUSER = false;\"");
		e.setField("error", "\"false\"");
		e.setField("timestamp", "2024-01-10T02:35:58.000Z");
		e.setField("logsource", "DWP-5CD212FV5L");
		final Record record = Parser.parseRecord(e);
		assertNotNull(record);
		assertEquals("alice1", record.getAccessor().getDbUser());
	}
	
	@Test
	public void testParseLogin_success() throws Exception {
		Event e = new org.logstash.Event();
		e.setField("username", "\"cassandra\"");
		e.setField("keyspace_name", "\"\"");
		e.setField("server_ip", "\"172.31.57.239\"");
		e.setField("category", "\"AUTH\"");
		e.setField("operation", "\"\"");
		e.setField("error", "\"false\"");
		e.setField("timestamp", "2024-01-10T02:35:58.000Z");
		e.setField("logsource", "DWP-5CD212FV5L");
		final Record record = Parser.parseRecord(e);
		assertNotNull(record);
		assertEquals("LOGIN", record.getData().getOriginalSqlCommand());
	}
	
	@Test
	public void testParseLogin_fail() throws Exception {
		Event e = new org.logstash.Event();
		e.setField("username", "\"cassandra\"");
		e.setField("keyspace_name", "\"\"");
		e.setField("server_ip", "\"172.31.57.239\"");
		e.setField("category", "\"AUTH\"");
		e.setField("operation", "\"\"");
		e.setField("error", "\"true\"");
		e.setField("timestamp", "2024-01-10T02:35:58.000Z");
		e.setField("logsource", "DWP-5CD212FV5L");
		final Record record = Parser.parseRecord(e);
		assertNotNull(record);
		assertEquals("", record.getException().getSqlString());
	}
	
	@Test
	public void testParseErrorsql() throws Exception {
		Event e = new org.logstash.Event();
		e.setField("username", "\"alice1\"");
		e.setField("keyspace_name", "\"mykeyspace\"");
		e.setField("server_ip", "\"172.31.57.239\"");
		e.setField("category", "\"DCL\"");
		e.setField("operation", "\"ALTER ROLE alice1 WITH PASSWORD = '***' AND SUPERUSER = false;\"");
		e.setField("error", "\"true\"");
		e.setField("timestamp", "2024-01-10T02:35:58.000Z");
		e.setField("logsource", "DWP-5CD212FV5L");
		final Record record = Parser.parseRecord(e);
		assertNotNull(record);
		assertEquals("DWP-5CD212FV5L",record.getAccessor().getServerHostName());
		assertEquals("Error Occurred", record.getException().getDescription());
		assertEquals("ALTER ROLE alice1 WITH PASSWORD = '***' AND SUPERUSER = false;", record.getException().getSqlString());
	}
	
	@Test
	public void testParseMultilineSql() throws Exception {
		Event e = new org.logstash.Event();
		e.setField("message", "");
		e.setField("username", "\"cassandra\"");
		e.setField("keyspace_name", "\"mykeyspace\"");
		e.setField("server_ip", "\"172.31.57.239\"");
		e.setField("category", "\"DDL\"");
		e.setField("operation", "\"CREATE TABLE DEVTESTING1 (#012    a int,#012    b int,#012    c int,#012    PRIMARY KEY (a, b, c)#012);\"");
		e.setField("error", "\"false\"");
		e.setField("timestamp", "2024-01-10T02:35:58.000Z");
		e.setField("logsource", "DWP-5CD212FV5L");
		final Record record = Parser.parseRecord(e);
		assertNotNull(record);
		assertEquals("CREATE TABLE DEVTESTING1 ( a int, b int, c int, PRIMARY KEY (a, b, c) );", record.getData().getOriginalSqlCommand());
	}

} 
