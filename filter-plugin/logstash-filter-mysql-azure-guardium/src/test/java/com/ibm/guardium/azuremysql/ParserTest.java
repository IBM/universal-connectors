/*
Copyright IBM Corp. 2023 All rights reserved.
SPDX-License-Identifier: Apache-2.0
*/
package com.ibm.guardium.azuremysql;

import static org.junit.Assert.*;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ibm.guardium.universalconnector.commons.structures.Record;
import org.junit.Test;

public class ParserTest {
	@Test
	public void testparseCreatetable() throws Exception {
		final String mysqlString = "{\"ServerType\":\"MySQL\",\"resourceId\":\"/SUBSCRIPTIONS/083DE1FB-CD2D-4B7C-895A-2B5AF1D091E8/RESOURCEGROUPS/NEWRESOURCEGUARDIUM/PROVIDERS/MICROSOFT.DBFORMYSQL/FLEXIBLESERVERS/MYSQL-TEST-GUARDIUM\",\"category\":\"MySqlAuditLogs\",\"properties\":{\"event_subclass\":\"LOG\",\"replication_set_role\":\"single\",\"thread_id\":27,\"ip\":\"20.219.48.223\",\"host\":\"\",\"event_class\":\"general_log\",\"error_code\":0,\"category\":\"MySqlAuditLogs\",\"user\":\"test[test] @  [20.219.48.223]\",\"sql_text\":\"create table subratotable55(ID int, phone int)\",\"event_time\":\"2023-04-17T09:59:26Z\"}}";
		final JsonObject mysqlJson = JsonParser.parseString(mysqlString).getAsJsonObject();
		Record record = Parser.parseRecord(mysqlJson);
		assertEquals("create table subratotable55(ID int, phone int)", record.getData().getOriginalSqlCommand());
		assertNotNull(record);
	}

	@Test
	public void testparseInsert() throws Exception {
		final String mysqlString = "{\"ServerType\":\"MySQL\",\"resourceId\":\"/SUBSCRIPTIONS/083DE1FB-CD2D-4B7C-895A-2B5AF1D091E8/RESOURCEGROUPS/NEWRESOURCEGUARDIUM/PROVIDERS/MICROSOFT.DBFORMYSQL/FLEXIBLESERVERS/MYSQL-TEST-GUARDIUM\",\"category\":\"MySqlAuditLogs\",\"properties\":{\"event_subclass\":\"LOG\",\"replication_set_role\":\"single\",\"thread_id\":21,\"ip\":\"20.204.110.141\",\"host\":\"\",\"event_class\":\"general_log\",\"error_code\":0,\"category\":\"MySqlAuditLogs\",\"user\":\"test[test] @  [20.204.110.141]\",\"sql_text\":\"insert into resourcetable(id) values(22)\",\"event_time\":\"2023-04-17T07:37:31Z\"}}";
		final JsonObject mysqlJson = JsonParser.parseString(mysqlString).getAsJsonObject();
		Record record = Parser.parseRecord(mysqlJson);
		assertEquals("insert into resourcetable(id) values(22)", record.getData().getOriginalSqlCommand());
		assertNotNull(record);
	}

	@Test
	public void testparseShowdatabase() throws Exception {
		final String mysqlString = "{\"ServerType\":\"MySQL\",\"resourceId\":\"/SUBSCRIPTIONS/083DE1FB-CD2D-4B7C-895A-2B5AF1D091E8/RESOURCEGROUPS/NEWRESOURCEGUARDIUM/PROVIDERS/MICROSOFT.DBFORMYSQL/FLEXIBLESERVERS/MYSQL-TEST-GUARDIUM\",\"category\":\"MySqlAuditLogs\",\"properties\":{\"event_subclass\":\"LOG\",\"replication_set_role\":\"single\",\"thread_id\":21,\"ip\":\"20.204.110.141\",\"host\":\"\",\"event_class\":\"general_log\",\"error_code\":0,\"category\":\"MySqlAuditLogs\",\"user\":\"test[test] @  [20.204.110.141]\",\"sql_text\":\"show databases\",\"event_time\":\"2023-04-17T07:37:31Z\"}}";
		final JsonObject mysqlJson = JsonParser.parseString(mysqlString).getAsJsonObject();
		Record record = Parser.parseRecord(mysqlJson);
		assertEquals("show databases", record.getData().getOriginalSqlCommand());
		assertNotNull(record);
	}
	
	@Test
	public void testparseShowtable() throws Exception {
		final String mysqlString = "{\"ServerType\":\"MySQL\",\"resourceId\":\"/SUBSCRIPTIONS/083DE1FB-CD2D-4B7C-895A-2B5AF1D091E8/RESOURCEGROUPS/NEWRESOURCEGUARDIUM/PROVIDERS/MICROSOFT.DBFORMYSQL/FLEXIBLESERVERS/MYSQL-TEST-GUARDIUM\",\"category\":\"MySqlAuditLogs\",\"properties\":{\"event_subclass\":\"LOG\",\"replication_set_role\":\"single\",\"thread_id\":21,\"ip\":\"20.204.110.141\",\"host\":\"\",\"event_class\":\"general_log\",\"error_code\":0,\"category\":\"MySqlAuditLogs\",\"user\":\"test[test] @  [20.204.110.141]\",\"sql_text\":\"show tables\",\"event_time\":\"2023-04-17T07:37:31Z\"}}";
		final JsonObject mysqlJson = JsonParser.parseString(mysqlString).getAsJsonObject();
		Record record = Parser.parseRecord(mysqlJson);
		assertEquals("show tables", record.getData().getOriginalSqlCommand());
		assertNotNull(record);
	}

	@Test
	public void testparseResourcetable() throws Exception {
		final String mysqlString = "{\"ServerType\":\"MySQL\",\"resourceId\":\"/SUBSCRIPTIONS/083DE1FB-CD2D-4B7C-895A-2B5AF1D091E8/RESOURCEGROUPS/NEWRESOURCEGUARDIUM/PROVIDERS/MICROSOFT.DBFORMYSQL/FLEXIBLESERVERS/MYSQL-TEST-GUARDIUM\",\"category\":\"MySqlAuditLogs\",\"properties\":{\"event_subclass\":\"LOG\",\"replication_set_role\":\"single\",\"thread_id\":21,\"ip\":\"20.204.110.141\",\"host\":\"\",\"event_class\":\"general_log\",\"error_code\":0,\"category\":\"MySqlAuditLogs\",\"user\":\"test[test] @  [20.204.110.141]\",\"sql_text\":\"select * from resourcetable\",\"event_time\":\"2023-04-17T08:13:46Z\"}}";
		final JsonObject mysqlJson = JsonParser.parseString(mysqlString).getAsJsonObject();
		Record record = Parser.parseRecord(mysqlJson);
		assertEquals("select * from resourcetable", record.getData().getOriginalSqlCommand());
		assertNotNull(record);
	}
	
	@Test
	public void testparseincorrectusername() throws Exception {
		final String mysqlString = "{\"ServerType\":\"MySQL\",\"resourceId\":\"/SUBSCRIPTIONS/083DE1FB-CD2D-4B7C-895A-2B5AF1D091E8/RESOURCEGROUPS/NEWRESOURCEGUARDIUM/PROVIDERS/MICROSOFT.DBFORMYSQL/FLEXIBLESERVERS/MYSQL-TEST-GUARDIUM\",\"category\":\"MySqlAuditLogs\",\"properties\":{\"event_subclass\":\"LOG\",\"replication_set_role\":\"single\",\"thread_id\":21,\"ip\":\"20.204.110.141\",\"host\":\"\",\"event_class\":\"general_log\",\"error_code\":0,\"category\":\"MySqlAuditLogs\",\"user\":\"test@  [20.204.110.141]\",\"sql_text\":\"select * from resourcetable\",\"event_time\":\"2023-04-17T08:13:46Z\"}}";
		final JsonObject mysqlJson = JsonParser.parseString(mysqlString).getAsJsonObject();
		Record record = Parser.parseRecord(mysqlJson);
		assertEquals("", record.getAccessor().getDbUser());
		assertNotNull(record);
	}

	@Test
	public void testparseAltertable() throws Exception {
		final String mysqlString = "{\"ServerType\":\"MySQL\",\"resourceId\":\"/SUBSCRIPTIONS/083DE1FB-CD2D-4B7C-895A-2B5AF1D091E8/RESOURCEGROUPS/NEWRESOURCEGUARDIUM/PROVIDERS/MICROSOFT.DBFORMYSQL/FLEXIBLESERVERS/MYSQL-TEST-GUARDIUM\",\"category\":\"MySqlAuditLogs\",\"properties\":{\"event_subclass\":\"LOG\",\"replication_set_role\":\"single\",\"thread_id\":21,\"ip\":\"20.204.110.141\",\"host\":\"\",\"event_class\":\"general_log\",\"error_code\":0,\"category\":\"MySqlAuditLogs\",\"user\":\"test[test] @  [20.204.110.141]\",\"sql_text\":\"Alter table subratotable1 add (Name varchar(20))\",\"event_time\":\"2023-04-17T08:13:46Z\"}}";
		final JsonObject mysqlJson = JsonParser.parseString(mysqlString).getAsJsonObject();
		Record record = Parser.parseRecord(mysqlJson);
		assertEquals("Alter table subratotable1 add (Name varchar(20))", record.getData().getOriginalSqlCommand());
		assertEquals("20.204.110.141", record.getSessionLocator().getClientIp());
		assertNotNull(record);
	}

	@Test
	public void testparseSelect() throws Exception {
		final String mysqlString = "{\"ServerType\":\"MySQL\",\"resourceId\":\"/SUBSCRIPTIONS/083DE1FB-CD2D-4B7C-895A-2B5AF1D091E8/RESOURCEGROUPS/NEWRESOURCEGUARDIUM/PROVIDERS/MICROSOFT.DBFORMYSQL/FLEXIBLESERVERS/MYSQL-TEST-GUARDIUM\",\"category\":\"MySqlAuditLogs\",\"properties\":{\"event_subclass\":\"LOG\",\"replication_set_role\":\"single\",\"thread_id\":21,\"ip\":\"20.204.110.141\",\"host\":\"\",\"event_class\":\"general_log\",\"error_code\":0,\"category\":\"MySqlAuditLogs\",\"user\":\"test[test] @  [20.204.110.141]\",\"sql_text\":\"select * from subratotable\",\"event_time\":\"2023-04-17T07:37:47Z\"}}";
		final JsonObject mysqlJson = JsonParser.parseString(mysqlString).getAsJsonObject();
		Record record = Parser.parseRecord(mysqlJson);
		assertEquals("select * from subratotable", record.getData().getOriginalSqlCommand());
		assertEquals("1669962420", record.getSessionId());
		assertNotNull(record);
	}

	@Test
	public void testparseDroptable() throws Exception {
		final String mysqlString = "{\"ServerType\":\"MySQL\",\"resourceId\":\"/SUBSCRIPTIONS/083DE1FB-CD2D-4B7C-895A-2B5AF1D091E8/RESOURCEGROUPS/NEWRESOURCEGUARDIUM/PROVIDERS/MICROSOFT.DBFORMYSQL/FLEXIBLESERVERS/MYSQL-TEST-GUARDIUM\",\"category\":\"MySqlAuditLogs\",\"properties\":{\"event_subclass\":\"LOG\",\"replication_set_role\":\"single\",\"thread_id\":21,\"ip\":\"20.204.110.141\",\"host\":\"\",\"event_class\":\"general_log\",\"error_code\":0,\"category\":\"MySqlAuditLogs\",\"user\":\"test[test] @  [20.204.110.141]\",\"sql_text\":\"drop table subratotable\",\"event_time\":\"2023-04-17T08:13:46Z\"}}";
		final JsonObject mysqlJson = JsonParser.parseString(mysqlString).getAsJsonObject();
		Record record = Parser.parseRecord(mysqlJson);
		assertEquals("drop table subratotable", record.getData().getOriginalSqlCommand());
		assertEquals("test", record.getAccessor().getDbUser());
		assertNotNull(record);
	}

	@Test
	public void testparseComment() throws Exception {
		final String mysqlString = "{\"ServerType\":\"MySQL\",\"resourceId\":\"/SUBSCRIPTIONS/083DE1FB-CD2D-4B7C-895A-2B5AF1D091E8/RESOURCEGROUPS/NEWRESOURCEGUARDIUM/PROVIDERS/MICROSOFT.DBFORMYSQL/FLEXIBLESERVERS/MYSQL-TEST-GUARDIUM\",\"category\":\"MySqlAuditLogs\",\"properties\":{\"event_subclass\":\"LOG\",\"replication_set_role\":\"single\",\"thread_id\":21,\"ip\":\"20.204.110.141\",\"host\":\"\",\"event_class\":\"general_log\",\"error_code\":0,\"category\":\"MySqlAuditLogs\",\"user\":\"test[test] @  [20.204.110.141]\",\"sql_text\":\"select @@version_comment limit 1\",\"event_time\":\"2023-04-17T08:13:46Z\"}}";
		final JsonObject mysqlJson = JsonParser.parseString(mysqlString).getAsJsonObject();
		Record record = Parser.parseRecord(mysqlJson);
		assertEquals("select @@version_comment limit 1", record.getData().getOriginalSqlCommand());
		assertNotNull(record);
	}

	@Test
	public void testparseDatabase() throws Exception {
		final String mysqlString = "{\"ServerType\":\"MySQL\",\"resourceId\":\"/SUBSCRIPTIONS/083DE1FB-CD2D-4B7C-895A-2B5AF1D091E8/RESOURCEGROUPS/NEWRESOURCEGUARDIUM/PROVIDERS/MICROSOFT.DBFORMYSQL/FLEXIBLESERVERS/MYSQL-TEST-GUARDIUM\",\"category\":\"MySqlAuditLogs\",\"properties\":{\"event_subclass\":\"LOG\",\"replication_set_role\":\"single\",\"thread_id\":21,\"ip\":\"20.204.110.141\",\"host\":\"\",\"event_class\":\"general_log\",\"error_code\":0,\"category\":\"MySqlAuditLogs\",\"user\":\"test[test] @  [20.204.110.141]\",\"sql_text\":\"SELECT DATABASE()\",\"event_time\":\"2023-04-17T08:13:46Z\"}}";
		final JsonObject mysqlJson = JsonParser.parseString(mysqlString).getAsJsonObject();
		Record record = Parser.parseRecord(mysqlJson);
		assertEquals("SELECT DATABASE()", record.getData().getOriginalSqlCommand());
		assertNotNull(record);
	}

	@Test
	public void testparseError() throws Exception {
		final String mysqlString = "{\"ServerType\":\"MySQL\",\"resourceId\":\"/SUBSCRIPTIONS/083DE1FB-CD2D-4B7C-895A-2B5AF1D091E8/RESOURCEGROUPS/NEWRESOURCEGUARDIUM/PROVIDERS/MICROSOFT.DBFORMYSQL/FLEXIBLESERVERS/MYSQL-TEST-GUARDIUM\",\"category\":\"MySqlAuditLogs\",\"properties\":{\"event_subclass\":\"ERROR\",\"replication_set_role\":\"single\",\"thread_id\":27,\"ip\":\"20.219.48.223\",\"host\":\"\",\"event_class\":\"general_log\",\"error_code\":1146,\"category\":\"MySqlAuditLogs\",\"user\":\"test[test] @  [20.219.48.223]\",\"sql_text\":\"select * from subrato\",\"event_time\":\"2023-04-17T09:27:22Z\"}}";
		final JsonObject mysqlJson = JsonParser.parseString(mysqlString).getAsJsonObject();
		Record record = Parser.parseRecord(mysqlJson);
		assertEquals("SQL_ERROR", record.getException().getExceptionTypeId());
		assertEquals("Error Occured (1146)", record.getException().getDescription());
		assertNotNull(record);
	}
	
	@Test
	public void testparseErrorWithoutErrorcode() throws Exception {
		final String mysqlString = "{\"ServerType\":\"MySQL\",\"resourceId\":\"/SUBSCRIPTIONS/083DE1FB-CD2D-4B7C-895A-2B5AF1D091E8/RESOURCEGROUPS/NEWRESOURCEGUARDIUM/PROVIDERS/MICROSOFT.DBFORMYSQL/FLEXIBLESERVERS/MYSQL-TEST-GUARDIUM\",\"category\":\"MySqlAuditLogs\",\"properties\":{\"event_subclass\":\"ERROR\",\"replication_set_role\":\"single\",\"thread_id\":27,\"ip\":\"20.219.48.223\",\"host\":\"\",\"event_class\":\"general_log\",\"category\":\"MySqlAuditLogs\",\"user\":\"test[test] @  [20.219.48.223]\",\"sql_text\":\"select * from subrato\",\"event_time\":\"2023-04-17T09:27:22Z\"}}";
		final JsonObject mysqlJson = JsonParser.parseString(mysqlString).getAsJsonObject();
		Record record = Parser.parseRecord(mysqlJson);
		assertEquals("SQL_ERROR", record.getException().getExceptionTypeId());
		assertEquals("Error Occured ()", record.getException().getDescription());
		assertNotNull(record);
	}
	
	@Test
	public void testparseErrorWithoutSqltext() throws Exception {
		final String mysqlString = "{\"ServerType\":\"MySQL\",\"resourceId\":\"/SUBSCRIPTIONS/083DE1FB-CD2D-4B7C-895A-2B5AF1D091E8/RESOURCEGROUPS/NEWRESOURCEGUARDIUM/PROVIDERS/MICROSOFT.DBFORMYSQL/FLEXIBLESERVERS/MYSQL-TEST-GUARDIUM\",\"category\":\"MySqlAuditLogs\",\"properties\":{\"event_subclass\":\"ERROR\",\"replication_set_role\":\"single\",\"thread_id\":27,\"ip\":\"20.219.48.223\",\"host\":\"\",\"event_class\":\"general_log\",\"error_code\":1146,\"category\":\"MySqlAuditLogs\",\"user\":\"test[test] @  [20.219.48.223]\",\"event_time\":\"2023-04-17T09:27:22Z\"}}";
		final JsonObject mysqlJson = JsonParser.parseString(mysqlString).getAsJsonObject();
		Record record = Parser.parseRecord(mysqlJson);
		assertEquals("SQL_ERROR", record.getException().getExceptionTypeId());
		assertEquals("Error Occured (1146)", record.getException().getDescription());
		assertEquals("", record.getException().getSqlString());
		assertNotNull(record);
	}

	@Test
	public void testparseDBname() throws Exception {
		final String mysqlString = "{\"ServerType\":\"MySQL\",\"resourceId\":\"/SUBSCRIPTIONS/083DE1FB-CD2D-4B7C-895A-2B5AF1D091E8/RESOURCEGROUPS/NEWRESOURCEGUARDIUM/PROVIDERS/MICROSOFT.DBFORMYSQL/FLEXIBLESERVERS/MYSQL-TEST-GUARDIUM\",\"category\":\"MySqlAuditLogs\",\"properties\":{\"event_subclass\":\"LOG\",\"replication_set_role\":\"single\",\"thread_id\":27,\"ip\":\"20.219.48.223\",\"host\":\"\",\"event_class\":\"general_log\",\"error_code\":0,\"category\":\"MySqlAuditLogs\",\"user\":\"test[test] @  [20.219.48.223]\",\"sql_text\":\"select * from sub\",\"event_time\":\"2023-04-17T09:25:28Z\"}}";
		final JsonObject mysqlJson = JsonParser.parseString(mysqlString).getAsJsonObject();
		Record record = Parser.parseRecord(mysqlJson);
		assertEquals("083DE1FB-CD2D-4B7C-895A-2B5AF1D091E8:MYSQL-TEST-GUARDIUM", record.getDbName());
		assertNotNull(record);
	}

	@Test
	public void testparseServiceName() throws Exception {
		final String mysqlString = "{\"ServerType\":\"MySQL\",\"resourceId\":\"/SUBSCRIPTIONS/083DE1FB-CD2D-4B7C-895A-2B5AF1D091E8/RESOURCEGROUPS/NEWRESOURCEGUARDIUM/PROVIDERS/MICROSOFT.DBFORMYSQL/FLEXIBLESERVERS/MYSQL-TEST-GUARDIUM\",\"category\":\"MySqlAuditLogs\",\"properties\":{\"event_subclass\":\"ERROR\",\"replication_set_role\":\"single\",\"thread_id\":27,\"ip\":\"20.219.48.223\",\"host\":\"\",\"event_class\":\"general_log\",\"error_code\":1146,\"category\":\"MySqlAuditLogs\",\"user\":\"test[test] @  [20.219.48.223]\",\"sql_text\":\"select * from subrato\",\"event_time\":\"2023-04-17T09:27:22Z\"}}";
		final JsonObject mysqlJson = JsonParser.parseString(mysqlString).getAsJsonObject();
		Record record = Parser.parseRecord(mysqlJson);
		assertEquals("083DE1FB-CD2D-4B7C-895A-2B5AF1D091E8:MYSQL-TEST-GUARDIUM", record.getAccessor().getServiceName());
		assertNotNull(record);
	}

	@Test
	public void testparseDbuser() throws Exception {
		final String mysqlString = "{\"ServerType\":\"MySQL\",\"resourceId\":\"/SUBSCRIPTIONS/083DE1FB-CD2D-4B7C-895A-2B5AF1D091E8/RESOURCEGROUPS/NEWRESOURCEGUARDIUM/PROVIDERS/MICROSOFT.DBFORMYSQL/FLEXIBLESERVERS/MYSQL-TEST-GUARDIUM\",\"category\":\"MySqlAuditLogs\",\"properties\":{\"event_subclass\":\"ERROR\",\"replication_set_role\":\"single\",\"thread_id\":27,\"ip\":\"20.219.48.223\",\"host\":\"\",\"event_class\":\"general_log\",\"error_code\":1146,\"category\":\"MySqlAuditLogs\",\"user\":\"test[test] @  [20.219.48.223]\",\"sql_text\":\"select * from subrato\",\"event_time\":\"2023-04-17T09:27:22Z\"}}";
		final JsonObject mysqlJson = JsonParser.parseString(mysqlString).getAsJsonObject();
		Record record = Parser.parseRecord(mysqlJson);
		assertEquals("test", record.getAccessor().getDbUser());
		assertNotNull(record);
	}

	@Test
	public void testparseServerHostName() throws Exception {
		final String mysqlString = "{\"ServerType\":\"MySQL\",\"resourceId\":\"/SUBSCRIPTIONS/083DE1FB-CD2D-4B7C-895A-2B5AF1D091E8/RESOURCEGROUPS/NEWRESOURCEGUARDIUM/PROVIDERS/MICROSOFT.DBFORMYSQL/FLEXIBLESERVERS/MYSQL-TEST-GUARDIUM\",\"category\":\"MySqlAuditLogs\",\"properties\":{\"event_subclass\":\"ERROR\",\"replication_set_role\":\"single\",\"thread_id\":27,\"ip\":\"20.219.48.223\",\"host\":\"\",\"event_class\":\"general_log\",\"error_code\":1146,\"category\":\"MySqlAuditLogs\",\"user\":\"test[test] @  [20.219.48.223]\",\"sql_text\":\"select * from subrato\",\"event_time\":\"2023-04-17T09:27:22Z\"}}";
		final JsonObject mysqlJson = JsonParser.parseString(mysqlString).getAsJsonObject();
		Record record = Parser.parseRecord(mysqlJson);
		assertEquals("083DE1FB-CD2D-4B7C-895A-2B5AF1D091E8-MYSQL-TEST-GUARDIUM.azure.com",
				record.getAccessor().getServerHostName());
		assertNotNull(record);
	}

	@Test
	public void testparseDbprotocol() throws Exception {
		final String mysqlString = "{\"ServerType\":\"MySQL\",\"resourceId\":\"/SUBSCRIPTIONS/083DE1FB-CD2D-4B7C-895A-2B5AF1D091E8/RESOURCEGROUPS/NEWRESOURCEGUARDIUM/PROVIDERS/MICROSOFT.DBFORMYSQL/FLEXIBLESERVERS/MYSQL-TEST-GUARDIUM\",\"category\":\"MySqlAuditLogs\",\"properties\":{\"event_subclass\":\"ERROR\",\"replication_set_role\":\"single\",\"thread_id\":27,\"ip\":\"20.219.48.223\",\"host\":\"\",\"event_class\":\"general_log\",\"error_code\":1146,\"category\":\"MySqlAuditLogs\",\"user\":\"test[test] @  [20.219.48.223]\",\"sql_text\":\"select * from subrato\",\"event_time\":\"2023-04-17T09:27:22Z\"}}";
		final JsonObject mysqlJson = JsonParser.parseString(mysqlString).getAsJsonObject();
		Record record = Parser.parseRecord(mysqlJson);
		assertEquals("MYSQL", record.getAccessor().getDbProtocol());
		assertEquals("MYSQL", record.getAccessor().getServerType());
		assertNotNull(record);
	}

	@Test
	public void testparseDatatype() throws Exception {
		final String mysqlString = "{\"ServerType\":\"MySQL\",\"resourceId\":\"/SUBSCRIPTIONS/083DE1FB-CD2D-4B7C-895A-2B5AF1D091E8/RESOURCEGROUPS/NEWRESOURCEGUARDIUM/PROVIDERS/MICROSOFT.DBFORMYSQL/FLEXIBLESERVERS/MYSQL-TEST-GUARDIUM\",\"category\":\"MySqlAuditLogs\",\"properties\":{\"event_subclass\":\"ERROR\",\"replication_set_role\":\"single\",\"thread_id\":27,\"ip\":\"20.219.48.223\",\"host\":\"\",\"event_class\":\"general_log\",\"error_code\":1146,\"category\":\"MySqlAuditLogs\",\"user\":\"test[test] @  [20.219.48.223]\",\"sql_text\":\"select * from subrato\",\"event_time\":\"2023-04-17T09:27:22Z\"}}";
		final JsonObject mysqlJson = JsonParser.parseString(mysqlString).getAsJsonObject();
		Record record = Parser.parseRecord(mysqlJson);
		assertEquals("TEXT", record.getAccessor().getDataType());
		assertEquals("MYSQL", record.getAccessor().getLanguage());
		assertNotNull(record);
	}

	@Test
	public void testparseSessionid() throws Exception {
		final String mysqlString = "{\"ServerType\":\"MySQL\",\"resourceId\":\"/SUBSCRIPTIONS/083DE1FB-CD2D-4B7C-895A-2B5AF1D091E8/RESOURCEGROUPS/NEWRESOURCEGUARDIUM/PROVIDERS/MICROSOFT.DBFORMYSQL/FLEXIBLESERVERS/MYSQL-TEST-GUARDIUM\",\"category\":\"MySqlAuditLogs\",\"properties\":{\"event_subclass\":\"ERROR\",\"replication_set_role\":\"single\",\"thread_id\":27,\"ip\":\"20.219.48.223\",\"host\":\"\",\"event_class\":\"general_log\",\"error_code\":1146,\"category\":\"MySqlAuditLogs\",\"user\":\"test[test] @  [20.219.48.223]\",\"sql_text\":\"select * from subrato\",\"event_time\":\"2023-04-17T09:27:22Z\"}}";
		final JsonObject mysqlJson = JsonParser.parseString(mysqlString).getAsJsonObject();
		Record record = Parser.parseRecord(mysqlJson);
		assertEquals("1359540437", record.getSessionId());
		assertNotNull(record);
	}

	@Test
	public void testparsegetIP() throws Exception {
		final String mysqlString = "{\"ServerType\":\"MySQL\",\"resourceId\":\"/SUBSCRIPTIONS/083DE1FB-CD2D-4B7C-895A-2B5AF1D091E8/RESOURCEGROUPS/NEWRESOURCEGUARDIUM/PROVIDERS/MICROSOFT.DBFORMYSQL/FLEXIBLESERVERS/MYSQL-TEST-GUARDIUM\",\"category\":\"MySqlAuditLogs\",\"properties\":{\"event_subclass\":\"ERROR\",\"replication_set_role\":\"single\",\"thread_id\":27,\"ip\":\"20.219.48.223\",\"host\":\"\",\"event_class\":\"general_log\",\"error_code\":1146,\"category\":\"MySqlAuditLogs\",\"user\":\"test[test] @  [20.219.48.223]\",\"sql_text\":\"select * from subrato\",\"event_time\":\"2023-04-17T09:27:22Z\"}}";
		final JsonObject mysqlJson = JsonParser.parseString(mysqlString).getAsJsonObject();
		Record record = Parser.parseRecord(mysqlJson);
		assertEquals("20.219.48.223", record.getSessionLocator().getClientIp());
		assertEquals("0.0.0.0", record.getSessionLocator().getServerIp());
		assertNotNull(record);
	}

	@Test
	public void testparsegetIPV6() throws Exception {
		final String mysqlString = "{\"ServerType\":\"MySQL\",\"resourceId\":\"/SUBSCRIPTIONS/083DE1FB-CD2D-4B7C-895A-2B5AF1D091E8/RESOURCEGROUPS/NEWRESOURCEGUARDIUM/PROVIDERS/MICROSOFT.DBFORMYSQL/FLEXIBLESERVERS/MYSQL-TEST-GUARDIUM\",\"category\":\"MySqlAuditLogs\",\"properties\":{\"event_subclass\":\"ERROR\",\"replication_set_role\":\"single\",\"thread_id\":27,\"ip\":\"2001:0db8:85a3:0000:0000:8a2e:0370:7334\",\"host\":\"\",\"event_class\":\"general_log\",\"error_code\":1146,\"category\":\"MySqlAuditLogs\",\"user\":\"test[test] @  [20.219.48.223]\",\"sql_text\":\"select * from subrato\",\"event_time\":\"2023-04-17T09:27:22Z\"}}";
		final JsonObject mysqlJson = JsonParser.parseString(mysqlString).getAsJsonObject();
		Record record = Parser.parseRecord(mysqlJson);
		assertEquals("2001:0db8:85a3:0000:0000:8a2e:0370:7334", record.getSessionLocator().getClientIpv6());
		assertEquals("0000:0000:0000:0000:0000:FFFF:0000:0000", record.getSessionLocator().getServerIpv6());
		assertNotNull(record);
	}
	
	@Test
	public void testparsegetNoIP() throws Exception {
		final String mysqlString = "{\"ServerType\":\"MySQL\",\"resourceId\":\"/SUBSCRIPTIONS/083DE1FB-CD2D-4B7C-895A-2B5AF1D091E8/RESOURCEGROUPS/NEWRESOURCEGUARDIUM/PROVIDERS/MICROSOFT.DBFORMYSQL/FLEXIBLESERVERS/MYSQL-TEST-GUARDIUM\",\"category\":\"MySqlAuditLogs\",\"properties\":{\"event_subclass\":\"ERROR\",\"replication_set_role\":\"single\",\"thread_id\":27,\"host\":\"\",\"event_class\":\"general_log\",\"error_code\":1146,\"category\":\"MySqlAuditLogs\",\"user\":\"test[test] @  [20.219.48.223]\",\"sql_text\":\"select * from subrato\",\"event_time\":\"2023-04-17T09:27:22Z\"}}";
		final JsonObject mysqlJson = JsonParser.parseString(mysqlString).getAsJsonObject();
		Record record = Parser.parseRecord(mysqlJson);
		assertEquals("0.0.0.0", record.getSessionLocator().getClientIp());
		assertEquals("0.0.0.0", record.getSessionLocator().getServerIp());
		assertNotNull(record);
	}
	
	@Test
	public void testparsegetPort() throws Exception {
		final String mysqlString = "{\"ServerType\":\"MySQL\",\"resourceId\":\"/SUBSCRIPTIONS/083DE1FB-CD2D-4B7C-895A-2B5AF1D091E8/RESOURCEGROUPS/NEWRESOURCEGUARDIUM/PROVIDERS/MICROSOFT.DBFORMYSQL/FLEXIBLESERVERS/MYSQL-TEST-GUARDIUM\",\"category\":\"MySqlAuditLogs\",\"properties\":{\"event_subclass\":\"ERROR\",\"replication_set_role\":\"single\",\"thread_id\":27,\"ip\":\"20.219.48.223\",\"host\":\"\",\"event_class\":\"general_log\",\"error_code\":1146,\"category\":\"MySqlAuditLogs\",\"user\":\"test[test] @  [20.219.48.223]\",\"sql_text\":\"select * from subrato\",\"event_time\":\"2023-04-17T09:27:22Z\"}}";
		final JsonObject mysqlJson = JsonParser.parseString(mysqlString).getAsJsonObject();
		Record record = Parser.parseRecord(mysqlJson);
		assertEquals(-1, record.getSessionLocator().getClientPort());
		assertEquals(-1, record.getSessionLocator().getServerPort());
		assertNotNull(record);
	}

	@Test
	public void testparseTimestamp() throws Exception {
		final String mysqlString = "{\"ServerType\":\"MySQL\",\"resourceId\":\"/SUBSCRIPTIONS/083DE1FB-CD2D-4B7C-895A-2B5AF1D091E8/RESOURCEGROUPS/NEWRESOURCEGUARDIUM/PROVIDERS/MICROSOFT.DBFORMYSQL/FLEXIBLESERVERS/MYSQL-TEST-GUARDIUM\",\"category\":\"MySqlAuditLogs\",\"properties\":{\"event_subclass\":\"LOG\",\"replication_set_role\":\"single\",\"thread_id\":21,\"ip\":\"20.204.110.141\",\"host\":\"\",\"event_class\":\"general_log\",\"error_code\":0,\"category\":\"MySqlAuditLogs\",\"user\":\"test[test] @  [20.204.110.141]\",\"sql_text\":\"show databases\",\"event_time\":\"2023-04-17T07:37:31Z\"}}";
		final JsonObject mysqlJson = JsonParser.parseString(mysqlString).getAsJsonObject();
		Record record = Parser.parseRecord(mysqlJson);
		assertEquals(Long.parseLong("1681717051000"), record.getTime().getTimstamp());
		assertEquals(0, record.getTime().getMinDst());
		assertEquals(0, record.getTime().getMinOffsetFromGMT());
		assertNotNull(record);
	}

	@Test
	public void testparseConnect() throws Exception {
		final String mysqlString = "{\"ServerType\":\"MySQL\",\"resourceId\":\"/SUBSCRIPTIONS/083DE1FB-CD2D-4B7C-895A-2B5AF1D091E8/RESOURCEGROUPS/NEWRESOURCEGUARDIUM/PROVIDERS/MICROSOFT.DBFORMYSQL/FLEXIBLESERVERS/MYSQL-TEST-GUARDIUM\",\"category\":\"MySqlAuditLogs\",\"properties\":{\"replication_set_role\":\"single\",\"event_subclass\":\"CONNECT\",\"connection_id\":41,\"ip\":\"192.8.218.211\",\"host\":\"\",\"event_class\":\"connection_log\",\"category\":\"MySqlAuditLogs\",\"user\":\"test\",\"db\":\"\"}}";
		final JsonObject mysqlJson = JsonParser.parseString(mysqlString).getAsJsonObject();
		Record record = Parser.parseRecord(mysqlJson);
		assertEquals("083DE1FB-CD2D-4B7C-895A-2B5AF1D091E8:MYSQL-TEST-GUARDIUM", record.getDbName());
		assertEquals("CONNECT", record.getData().getOriginalSqlCommand());
		assertNotNull(record);
	}

	@Test
	public void testparsedbnameConnect() throws Exception {
		final String mysqlString = "{\"ServerType\":\"MySQL\",\"resourceId\":\"/SUBSCRIPTIONS/083DE1FB-CD2D-4B7C-895A-2B5AF1D091E8/RESOURCEGROUPS/NEWRESOURCEGUARDIUM/PROVIDERS/MICROSOFT.DBFORMYSQL/FLEXIBLESERVERS/MYSQL-TEST-GUARDIUM\",\"category\":\"MySqlAuditLogs\",\"properties\":{\"replication_set_role\":\"single\",\"event_subclass\":\"CONNECT\",\"connection_id\":41,\"ip\":\"192.8.218.211\",\"host\":\"\",\"event_class\":\"connection_log\",\"category\":\"MySqlAuditLogs\",\"user\":\"test\",\"db\":\"demodb\"}}";
		final JsonObject mysqlJson = JsonParser.parseString(mysqlString).getAsJsonObject();
		Record record = Parser.parseRecord(mysqlJson);
		assertEquals("083DE1FB-CD2D-4B7C-895A-2B5AF1D091E8:MYSQL-TEST-GUARDIUM:demodb", record.getDbName());
		assertEquals("CONNECT", record.getData().getOriginalSqlCommand());
		assertNotNull(record);
	}
	
	@Test
	public void testparsedbnameDisConnect() throws Exception {
		final String mysqlString = "{\"ServerType\":\"MySQL\",\"resourceId\":\"/SUBSCRIPTIONS/083DE1FB-CD2D-4B7C-895A-2B5AF1D091E8/RESOURCEGROUPS/NEWRESOURCEGUARDIUM/PROVIDERS/MICROSOFT.DBFORMYSQL/FLEXIBLESERVERS/MYSQL-TEST-GUARDIUM\",\"category\":\"MySqlAuditLogs\",\"properties\":{\"replication_set_role\":\"single\",\"event_subclass\":\"DISCONNECT\",\"connection_id\":41,\"ip\":\"192.8.218.211\",\"host\":\"\",\"event_class\":\"connection_log\",\"category\":\"MySqlAuditLogs\",\"user\":\"test\",\"db\":\"demodatabase\"}}";
		final JsonObject mysqlJson = JsonParser.parseString(mysqlString).getAsJsonObject();
		Record record = Parser.parseRecord(mysqlJson);
		assertEquals("DISCONNECT", record.getData().getOriginalSqlCommand());
		assertEquals("083DE1FB-CD2D-4B7C-895A-2B5AF1D091E8:MYSQL-TEST-GUARDIUM:demodatabase", record.getDbName());
		assertNotNull(record);
	}

	@Test
	public void testparseSessionLocatorIpv6() throws Exception {
		final String mysqlString = "{\"ServerType\":\"MySQL\",\"resourceId\":\"/SUBSCRIPTIONS/083DE1FB-CD2D-4B7C-895A-2B5AF1D091E8/RESOURCEGROUPS/NEWRESOURCEGUARDIUM/PROVIDERS/MICROSOFT.DBFORMYSQL/FLEXIBLESERVERS/MYSQL-TEST-GUARDIUM\",\"category\":\"MySqlAuditLogs\",\"properties\":{\"event_subclass\":\"LOG\",\"replication_set_role\":\"single\",\"thread_id\":27,\"ip\":\"20.219.48.223\",\"host\":\"\",\"event_class\":\"general_log\",\"error_code\":0,\"category\":\"MySqlAuditLogs\",\"user\":\"test[test] @  [20.219.48.223]\",\"sql_text\":\"SELECT DATABASE()\",\"event_time\":\"2023-04-17T09:25:41Z\"}}";
		final JsonObject mysqlJson = JsonParser.parseString(mysqlString).getAsJsonObject();
		Record record = Parser.parseRecord(mysqlJson);
		assertEquals(false, record.getSessionLocator().isIpv6());
		assertNotNull(record);
	}

}
