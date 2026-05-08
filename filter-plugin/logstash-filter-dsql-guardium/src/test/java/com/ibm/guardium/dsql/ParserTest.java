package com.ibm.guardium.dsql;

import co.elastic.logstash.api.Event;
import com.ibm.guardium.universalconnector.commons.structures.Record;
import org.junit.Test;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class ParserTest {

    @Test
    public void testParseNestedPostgreSQLLoginFailure() throws ParseException {
        // Create a nested DatabaseActivityMonitoringRecord format event
        Event event = new org.logstash.Event();
        
        event.setField(Constants.TYPE, "DatabaseActivityMonitoringRecord");
        event.setField(Constants.CLUSTER_ID, "");
        event.setField(Constants.INSTANCE_ID, "db-4JCWQLUZVFYP7DIWP6JVQ77O3Q");
        event.setField(Constants.ACCOUNT_ID, "123456789012");
        event.setField(Constants.INSTANCE_NAME, "postgres-cluster-1");
        
        // Create the nested event list
        List<Map<String, Object>> eventList = new ArrayList<>();
        Map<String, Object> nestedEvent = new HashMap<>();
        
        nestedEvent.put(Constants.CLASS, "LOGIN");
        nestedEvent.put(Constants.CLIENT_APPLICATION, "psql");
        nestedEvent.put(Constants.COMMAND, "LOGIN FAILED");
        nestedEvent.put(Constants.COMMAND_TEXT, "Login failed for user 'test'. Reason: Password did not match that for the login provided.");
        nestedEvent.put(Constants.DATABASE_NAME, "testdb");
        nestedEvent.put(Constants.DB_PROTOCOL, "POSTGRESQL");
        nestedEvent.put(Constants.DB_USER_NAME, "test");
        nestedEvent.put(Constants.ERROR_MESSAGE, "password authentication failed for user \"test\"");
        nestedEvent.put(Constants.EXIT_CODE, 1);
        nestedEvent.put(Constants.LOG_TIME, "2022-10-06T21:34:42.711Z");
        nestedEvent.put(Constants.REMOTE_HOST, "10.0.1.100");
        nestedEvent.put(Constants.REMOTE_PORT, 5432);
        nestedEvent.put(Constants.SERVER_HOST, "172.31.30.159");
        nestedEvent.put(Constants.SESSION_ID, "session-123");
        nestedEvent.put(Constants.START_TIME, null);
        nestedEvent.put(Constants.TYPE, "record");
        
        eventList.add(nestedEvent);
        event.setField(Constants.DATABASE_ACTIVITY_EVENT_LIST, eventList);
        
        // Parse the record
        Record record = Parser.parseRecord(event);
        
        // Verify the record was parsed correctly
        assertNotNull(record);
        assertNotNull(record.getException());
        assertEquals(Constants.LOGIN_FAILED, record.getException().getExceptionTypeId());
        assertTrue(record.getException().getDescription().contains("password authentication failed"));
        assertEquals("test", record.getAccessor().getDbUser());
        assertEquals("10.0.1.100", record.getSessionLocator().getClientIp());
        assertEquals(5432, record.getSessionLocator().getClientPort());
        assertEquals("session-123", record.getSessionId());
        assertEquals(Constants.SERVER_TYPE_STRING, record.getAccessor().getServerType());
        assertEquals(Constants.DATA_PROTOCOL_STRING, record.getAccessor().getDbProtocol());
    }

    @Test
    public void testParseNestedPostgreSQLSuccessfulQuery() throws ParseException {
        // Create a nested DatabaseActivityMonitoringRecord format event
        Event event = new org.logstash.Event();
        
        event.setField(Constants.TYPE, "DatabaseActivityMonitoringRecord");
        event.setField(Constants.CLUSTER_ID, "");
        event.setField(Constants.INSTANCE_ID, "db-4JCWQLUZVFYP7DIWP6JVQ77O3Q");
        event.setField(Constants.ACCOUNT_ID, "123456789012");
        event.setField(Constants.INSTANCE_NAME, "postgres-cluster-1");
        
        // Create the nested event list
        List<Map<String, Object>> eventList = new ArrayList<>();
        Map<String, Object> nestedEvent = new HashMap<>();
        
        nestedEvent.put(Constants.CLASS, "READ");
        nestedEvent.put(Constants.CLIENT_APPLICATION, "psql");
        nestedEvent.put(Constants.COMMAND, "SELECT");
        nestedEvent.put(Constants.COMMAND_TEXT, "SELECT * FROM users WHERE id = 1;");
        nestedEvent.put(Constants.DATABASE_NAME, "testdb");
        nestedEvent.put(Constants.DB_PROTOCOL, "POSTGRESQL");
        nestedEvent.put(Constants.DB_USER_NAME, "postgres");
        nestedEvent.put(Constants.EXIT_CODE, 0);
        nestedEvent.put(Constants.LOG_TIME, "2022-10-06T21:34:42.711Z");
        nestedEvent.put(Constants.REMOTE_HOST, "10.0.1.100");
        nestedEvent.put(Constants.REMOTE_PORT, 5432);
        nestedEvent.put(Constants.SERVER_HOST, "172.31.30.159");
        nestedEvent.put(Constants.SESSION_ID, "session-456");
        nestedEvent.put(Constants.TYPE, "record");
        
        eventList.add(nestedEvent);
        event.setField(Constants.DATABASE_ACTIVITY_EVENT_LIST, eventList);
        
        // Parse the record
        Record record = Parser.parseRecord(event);
        
        // Verify the record was parsed correctly
        assertNotNull(record);
        assertNotNull(record.getData());
        assertEquals("SELECT * FROM users WHERE id = 1;", record.getData().getOriginalSqlCommand());
        assertEquals("postgres", record.getAccessor().getDbUser());
        assertEquals("10.0.1.100", record.getSessionLocator().getClientIp());
        assertEquals(5432, record.getSessionLocator().getClientPort());
        assertEquals("session-456", record.getSessionId());
    }

    @Test
    public void testAcceptSQLServerRecord() throws ParseException {
        // Create a nested event with SQL Server protocol
        Event event = new org.logstash.Event();
        
        event.setField(Constants.TYPE, "DatabaseActivityMonitoringRecord");
        event.setField(Constants.INSTANCE_ID, "db-test");
        event.setField(Constants.ACCOUNT_ID, "123456789012");
        event.setField(Constants.INSTANCE_NAME, "sqlserver-instance");
        
        List<Map<String, Object>> eventList = new ArrayList<>();
        Map<String, Object> nestedEvent = new HashMap<>();
        
        nestedEvent.put(Constants.DB_PROTOCOL, "SQLSERVER");
        nestedEvent.put(Constants.DB_USER_NAME, "test");
        nestedEvent.put(Constants.DATABASE_NAME, "testdb");
        nestedEvent.put(Constants.EXIT_CODE, 0);
        nestedEvent.put(Constants.STATEMENT_TEXT, "SELECT 1");
        nestedEvent.put(Constants.LOG_TIME, "2023-11-10T10:15:30Z");
        nestedEvent.put(Constants.REMOTE_HOST, "10.0.0.1");
        nestedEvent.put(Constants.REMOTE_PORT, 1433);
        nestedEvent.put(Constants.SESSION_ID, "session-789");
        
        eventList.add(nestedEvent);
        event.setField(Constants.DATABASE_ACTIVITY_EVENT_LIST, eventList);
        
        // This should now be accepted even though it's SQL Server
        Record record = Parser.parseRecord(event);
        
        assertNotNull(record);
        assertEquals("test", record.getAccessor().getDbUser());
        assertNotNull(record.getData());
    }

    @Test
    public void testFlatFormatStillWorks() throws ParseException {
        // Verify that the original flat format still works
        Event event = new org.logstash.Event();
        
        event.setField(Constants.TYPE, "record");
        event.setField(Constants.DATABASE_NAME, "testdb");
        event.setField(Constants.DB_USER_NAME, "postgres");
        event.setField(Constants.DB_PROTOCOL, "POSTGRESQL");
        event.setField(Constants.REMOTE_HOST, "10.0.0.1");
        event.setField(Constants.REMOTE_PORT, 5432);
        event.setField(Constants.SESSION_ID, "session789");
        event.setField(Constants.STATEMENT_TEXT, "SELECT 1;");
        event.setField(Constants.EXIT_CODE, 0);
        event.setField(Constants.LOG_TIME, "2023-11-10T10:15:30Z");
        event.setField(Constants.ACCOUNT_ID, "123456789012");
        event.setField(Constants.INSTANCE_NAME, "dsql-cluster-1");
        
        Record record = Parser.parseRecord(event);
        
        assertNotNull(record);
        assertNotNull(record.getData());
        assertEquals("SELECT 1;", record.getData().getOriginalSqlCommand());
        assertEquals("postgres", record.getAccessor().getDbUser());
    }

    @Test
    public void testParseSQLServerTimestampFormat() throws ParseException {
        // Test SQL Server timestamp format with 7 decimal places: "2022-10-06 21:34:42.7113072+00"
        Event event = new org.logstash.Event();
        
        event.setField(Constants.TYPE, "DatabaseActivityMonitoringRecord");
        event.setField(Constants.INSTANCE_ID, "db-4JCWQLUZVFYP7DIWP6JVQ77O3Q");
        event.setField(Constants.ACCOUNT_ID, "123456789012");
        event.setField(Constants.INSTANCE_NAME, "postgres-cluster-1");
        
        List<Map<String, Object>> eventList = new ArrayList<>();
        Map<String, Object> nestedEvent = new HashMap<>();
        
        nestedEvent.put(Constants.CLASS, "LOGIN");
        nestedEvent.put(Constants.CLIENT_APPLICATION, "psql");
        nestedEvent.put(Constants.COMMAND, "LOGIN FAILED");
        nestedEvent.put(Constants.COMMAND_TEXT, "Login failed for user 'test'.");
        nestedEvent.put(Constants.DATABASE_NAME, "testdb");
        nestedEvent.put(Constants.DB_PROTOCOL, "POSTGRESQL");
        nestedEvent.put(Constants.DB_USER_NAME, "test");
        nestedEvent.put(Constants.ERROR_MESSAGE, "password authentication failed");
        nestedEvent.put(Constants.EXIT_CODE, 1);
        // SQL Server timestamp format with space and 7 decimal places
        nestedEvent.put(Constants.LOG_TIME, "2022-10-06 21:34:42.7113072+00");
        nestedEvent.put(Constants.REMOTE_HOST, "10.0.1.100");
        nestedEvent.put(Constants.REMOTE_PORT, 5432);
        nestedEvent.put(Constants.SESSION_ID, "session-123");
        nestedEvent.put(Constants.TYPE, "record");
        
        eventList.add(nestedEvent);
        event.setField(Constants.DATABASE_ACTIVITY_EVENT_LIST, eventList);
        
        Record record = Parser.parseRecord(event);
        
        assertNotNull(record);
        assertNotNull(record.getTime());
        // Verify timestamp was parsed (should be > 0)
        assertTrue(record.getTime().getTimstamp() > 0);
        // Verify it's approximately correct (October 2022)
        assertTrue(record.getTime().getTimstamp() > 1665000000000L);
        assertTrue(record.getTime().getTimstamp() < 1666000000000L);
    }

    @Test
    public void testParseNestedDDLStatement() throws ParseException {
        // Test DDL statement (CREATE TABLE) with error message (creates exception)
        Event event = new org.logstash.Event();
        
        event.setField(Constants.TYPE, "DatabaseActivityMonitoringRecord");
        event.setField(Constants.CLUSTER_ID, "");
        event.setField(Constants.INSTANCE_ID, "db-4JCWQLUZVFYP7DIWP6JVQ77O3Q");
        event.setField(Constants.ACCOUNT_ID, "123456789012");
        event.setField(Constants.INSTANCE_NAME, "postgres-cluster-1");
        
        List<Map<String, Object>> eventList = new ArrayList<>();
        Map<String, Object> nestedEvent = new HashMap<>();
        
        nestedEvent.put(Constants.CLASS, "SCHEMA");
        nestedEvent.put(Constants.CLIENT_APPLICATION, "pgAdmin");
        nestedEvent.put(Constants.COMMAND, "CREATE");
        nestedEvent.put(Constants.COMMAND_TEXT, "CREATE TABLE testDB.public.TestTable2(textA varchar(6000), textB varchar(6000))");
        nestedEvent.put(Constants.DATABASE_NAME, "testDB");
        nestedEvent.put(Constants.DB_PROTOCOL, "POSTGRESQL");
        nestedEvent.put(Constants.DB_USER_NAME, "test");
        nestedEvent.put(Constants.ERROR_MESSAGE, "permission denied for schema public");
        nestedEvent.put(Constants.EXIT_CODE, 1);
        nestedEvent.put(Constants.LOG_TIME, "2022-10-06 21:44:38.4120677+00");
        nestedEvent.put(Constants.REMOTE_HOST, "10.0.1.100");
        nestedEvent.put(Constants.REMOTE_PORT, 5432);
        nestedEvent.put(Constants.SERVER_HOST, "172.31.30.159");
        nestedEvent.put(Constants.SESSION_ID, 84);
        nestedEvent.put(Constants.TYPE, "record");
        
        eventList.add(nestedEvent);
        event.setField(Constants.DATABASE_ACTIVITY_EVENT_LIST, eventList);
        
        Record record = Parser.parseRecord(event);
        
        assertNotNull(record);
        // Since exitCode is 1, should have exception not data
        assertNotNull(record.getException());
        assertEquals(Constants.SQL_ERROR, record.getException().getExceptionTypeId());
        assertEquals("permission denied for schema public", record.getException().getDescription());
        assertEquals("CREATE TABLE testDB.public.TestTable2(textA varchar(6000), textB varchar(6000))",
                     record.getException().getSqlString());
        assertEquals("test", record.getAccessor().getDbUser());
        assertEquals("10.0.1.100", record.getSessionLocator().getClientIp());
        assertEquals(5432, record.getSessionLocator().getClientPort());
    }

    @Test
    public void testParseSelectWithExitCode1NoError() throws ParseException {
        // Test SELECT statement with exitCode 1 but no error message (should create Data, not Exception)
        Event event = new org.logstash.Event();
        
        event.setField(Constants.TYPE, "DatabaseActivityMonitoringRecord");
        event.setField(Constants.CLUSTER_ID, "");
        event.setField(Constants.INSTANCE_ID, "db-4JCWQLUZVFYP7DIWP6JVQ77O3Q");
        event.setField(Constants.ACCOUNT_ID, "123456789012");
        event.setField(Constants.INSTANCE_NAME, "postgres-cluster-1");
        
        List<Map<String, Object>> eventList = new ArrayList<>();
        Map<String, Object> nestedEvent = new HashMap<>();
        
        nestedEvent.put(Constants.CLASS, "TABLE");
        nestedEvent.put(Constants.CLIENT_APPLICATION, "psql");
        nestedEvent.put(Constants.COMMAND, "SELECT");
        nestedEvent.put(Constants.COMMAND_TEXT, "select * from testDB.public.TestTable");
        nestedEvent.put(Constants.DATABASE_NAME, "testDB");
        nestedEvent.put(Constants.DB_PROTOCOL, "POSTGRESQL");
        nestedEvent.put(Constants.DB_USER_NAME, "test");
        nestedEvent.put(Constants.ERROR_MESSAGE, null);  // No error message
        nestedEvent.put(Constants.EXIT_CODE, 1);  // exitCode 1 but no error
        nestedEvent.put(Constants.LOG_TIME, "2022-10-06 21:24:59.9422268+00");
        nestedEvent.put(Constants.REMOTE_HOST, "10.0.1.100");
        nestedEvent.put(Constants.REMOTE_PORT, 5432);
        nestedEvent.put(Constants.SERVER_HOST, "172.31.30.159");
        nestedEvent.put(Constants.SESSION_ID, 62);
        nestedEvent.put(Constants.TYPE, "record");
        
        eventList.add(nestedEvent);
        event.setField(Constants.DATABASE_ACTIVITY_EVENT_LIST, eventList);
        
        Record record = Parser.parseRecord(event);
        
        assertNotNull(record);
        // Since errorMessage is null, should create Data record (not Exception)
        assertNotNull(record.getData());
        assertEquals("select * from testDB.public.TestTable", record.getData().getOriginalSqlCommand());
        assertEquals("test", record.getAccessor().getDbUser());
        assertEquals("10.0.1.100", record.getSessionLocator().getClientIp());
        assertEquals(5432, record.getSessionLocator().getClientPort());
    }

    @Test
    public void testParseNestedDDLStatementSuccess() throws ParseException {
        // Test successful DDL statement (CREATE TABLE) with no error message
        Event event = new org.logstash.Event();
        
        event.setField(Constants.TYPE, "DatabaseActivityMonitoringRecord");
        event.setField(Constants.INSTANCE_ID, "db-4JCWQLUZVFYP7DIWP6JVQ77O3Q");
        event.setField(Constants.ACCOUNT_ID, "123456789012");
        event.setField(Constants.INSTANCE_NAME, "postgres-cluster-1");
        
        List<Map<String, Object>> eventList = new ArrayList<>();
        Map<String, Object> nestedEvent = new HashMap<>();
        
        nestedEvent.put(Constants.CLASS, "SCHEMA");
        nestedEvent.put(Constants.CLIENT_APPLICATION, "psql");
        nestedEvent.put(Constants.COMMAND, "CREATE");
        nestedEvent.put(Constants.COMMAND_TEXT, "CREATE TABLE users(id serial PRIMARY KEY, name varchar(100))");
        nestedEvent.put(Constants.DATABASE_NAME, "mydb");
        nestedEvent.put(Constants.DB_PROTOCOL, "POSTGRESQL");
        nestedEvent.put(Constants.DB_USER_NAME, "postgres");
        nestedEvent.put(Constants.EXIT_CODE, 0);
        nestedEvent.put(Constants.LOG_TIME, "2022-10-06T21:44:38.412Z");
        nestedEvent.put(Constants.REMOTE_HOST, "10.0.1.100");
        nestedEvent.put(Constants.REMOTE_PORT, 5432);
        nestedEvent.put(Constants.SESSION_ID, "session-789");
        nestedEvent.put(Constants.TYPE, "record");
        
        eventList.add(nestedEvent);
        event.setField(Constants.DATABASE_ACTIVITY_EVENT_LIST, eventList);
        
        Record record = Parser.parseRecord(event);
        
        assertNotNull(record);
        // Since exitCode is 0, should have data not exception
        assertNotNull(record.getData());
        assertEquals("CREATE TABLE users(id serial PRIMARY KEY, name varchar(100))",
                     record.getData().getOriginalSqlCommand());
        assertEquals("postgres", record.getAccessor().getDbUser());
    }

    @Test
    public void testParseWithNullRemotePort() throws ParseException {
        // Test handling of null remotePort (common in SQL Server logs)
        Event event = new org.logstash.Event();
        
        event.setField(Constants.TYPE, "DatabaseActivityMonitoringRecord");
        event.setField(Constants.INSTANCE_ID, "db-test");
        event.setField(Constants.ACCOUNT_ID, "123456789012");
        event.setField(Constants.INSTANCE_NAME, "postgres-cluster-1");
        
        List<Map<String, Object>> eventList = new ArrayList<>();
        Map<String, Object> nestedEvent = new HashMap<>();
        
        nestedEvent.put(Constants.DB_PROTOCOL, "POSTGRESQL");
        nestedEvent.put(Constants.DB_USER_NAME, "test");
        nestedEvent.put(Constants.DATABASE_NAME, "testdb");
        nestedEvent.put(Constants.REMOTE_HOST, "local machine");
        nestedEvent.put(Constants.REMOTE_PORT, null);  // null port
        nestedEvent.put(Constants.EXIT_CODE, 0);
        nestedEvent.put(Constants.STATEMENT_TEXT, "SELECT 1");
        nestedEvent.put(Constants.LOG_TIME, "2023-11-10T10:15:30Z");
        nestedEvent.put(Constants.SESSION_ID, "session-456");
        nestedEvent.put(Constants.TYPE, "record");
        
        eventList.add(nestedEvent);
        event.setField(Constants.DATABASE_ACTIVITY_EVENT_LIST, eventList);
        
        Record record = Parser.parseRecord(event);
        
        assertNotNull(record);
        assertEquals("local machine", record.getSessionLocator().getClientIp());
        // Should use default port when null
        assertEquals(Constants.DEFAULT_PORT, record.getSessionLocator().getClientPort());
    }

    @Test
    public void testParseWithEmptyDatabaseName() throws ParseException {
        // Test handling of empty database name (common in login failures)
        Event event = new org.logstash.Event();
        
        event.setField(Constants.TYPE, "DatabaseActivityMonitoringRecord");
        event.setField(Constants.INSTANCE_ID, "db-test");
        event.setField(Constants.ACCOUNT_ID, "123456789012");
        event.setField(Constants.INSTANCE_NAME, "postgres-cluster-1");
        
        List<Map<String, Object>> eventList = new ArrayList<>();
        Map<String, Object> nestedEvent = new HashMap<>();
        
        nestedEvent.put(Constants.DB_PROTOCOL, "POSTGRESQL");
        nestedEvent.put(Constants.DB_USER_NAME, "test");
        nestedEvent.put(Constants.DATABASE_NAME, "");  // Empty database name
        nestedEvent.put(Constants.REMOTE_HOST, "10.0.0.1");
        nestedEvent.put(Constants.REMOTE_PORT, 5432);
        nestedEvent.put(Constants.EXIT_CODE, 1);
        nestedEvent.put(Constants.ERROR_MESSAGE, "authentication failed");
        nestedEvent.put(Constants.LOG_TIME, "2023-11-10T10:15:30Z");
        nestedEvent.put(Constants.SESSION_ID, 0);
        nestedEvent.put(Constants.TYPE, "record");
        
        eventList.add(nestedEvent);
        event.setField(Constants.DATABASE_ACTIVITY_EVENT_LIST, eventList);
        
        Record record = Parser.parseRecord(event);
        
        assertNotNull(record);
        // Empty database name should result in N.A.
        assertEquals(Constants.NA, record.getDbName());
    }

    @Test
    public void testParseMultilineCommandText() throws ParseException {
        // Test handling of multi-line SQL statements (common in DDL)
        Event event = new org.logstash.Event();
        
        event.setField(Constants.TYPE, "DatabaseActivityMonitoringRecord");
        event.setField(Constants.INSTANCE_ID, "db-test");
        event.setField(Constants.ACCOUNT_ID, "123456789012");
        event.setField(Constants.INSTANCE_NAME, "postgres-cluster-1");
        
        List<Map<String, Object>> eventList = new ArrayList<>();
        Map<String, Object> nestedEvent = new HashMap<>();
        
        String multilineSQL = "CREATE TABLE testDB.dbo.TestTable2(\r\n" +
                             "textA varchar(6000),\r\n" +
                             "    textB varchar(6000)\r\n" +
                             ")";
        
        nestedEvent.put(Constants.DB_PROTOCOL, "POSTGRESQL");
        nestedEvent.put(Constants.DB_USER_NAME, "postgres");
        nestedEvent.put(Constants.DATABASE_NAME, "testDB");
        nestedEvent.put(Constants.COMMAND_TEXT, multilineSQL);
        nestedEvent.put(Constants.REMOTE_HOST, "10.0.0.1");
        nestedEvent.put(Constants.REMOTE_PORT, 5432);
        nestedEvent.put(Constants.EXIT_CODE, 0);
        nestedEvent.put(Constants.LOG_TIME, "2023-11-10T10:15:30Z");
        nestedEvent.put(Constants.SESSION_ID, "session-123");
        nestedEvent.put(Constants.TYPE, "record");
        
        eventList.add(nestedEvent);
        event.setField(Constants.DATABASE_ACTIVITY_EVENT_LIST, eventList);
        
        Record record = Parser.parseRecord(event);
        
        assertNotNull(record);
        assertNotNull(record.getData());
        // Verify multi-line SQL is preserved
        assertTrue(record.getData().getOriginalSqlCommand().contains("\r\n"));
        assertTrue(record.getData().getOriginalSqlCommand().contains("textA varchar(6000)"));
    }

    @Test
    public void testParseSQLServerLoginFailureWithNullErrorMessage() throws ParseException {
        // Test SQL Server login failure where errorMessage is null but command indicates failure
        Event event = new org.logstash.Event();
        
        event.setField(Constants.TYPE, "DatabaseActivityMonitoringRecord");
        event.setField(Constants.CLUSTER_ID, "");
        event.setField(Constants.INSTANCE_ID, "db-4JCWQLUZVFYP7DIWP6JVQ77O3Q");
        event.setField(Constants.ACCOUNT_ID, "123456789012");
        event.setField(Constants.INSTANCE_NAME, "sqlserver-cluster-1");
        
        List<Map<String, Object>> eventList = new ArrayList<>();
        Map<String, Object> nestedEvent = new HashMap<>();
        
        nestedEvent.put(Constants.CLASS, "LOGIN");
        nestedEvent.put(Constants.CLIENT_APPLICATION, "Microsoft SQL Server Management Studio");
        nestedEvent.put(Constants.COMMAND, "LOGIN FAILED");
        nestedEvent.put(Constants.COMMAND_TEXT, "Login failed for user 'test'. Reason: Password did not match that for the login provided. [CLIENT: local-machine]");
        nestedEvent.put(Constants.DATABASE_NAME, "");
        nestedEvent.put(Constants.DB_PROTOCOL, "SQLSERVER");
        nestedEvent.put(Constants.DB_USER_NAME, "test");
        nestedEvent.put(Constants.ERROR_MESSAGE, null);  // null error message
        nestedEvent.put(Constants.EXIT_CODE, 0);
        nestedEvent.put(Constants.LOG_TIME, "2022-10-06 21:34:42.7113072+00");
        nestedEvent.put(Constants.REMOTE_HOST, "local machine");
        nestedEvent.put(Constants.REMOTE_PORT, null);
        nestedEvent.put(Constants.SERVER_HOST, "172.31.30.159");
        nestedEvent.put(Constants.SESSION_ID, 0);
        nestedEvent.put(Constants.TYPE, "record");
        
        eventList.add(nestedEvent);
        event.setField(Constants.DATABASE_ACTIVITY_EVENT_LIST, eventList);
        
        Record record = Parser.parseRecord(event);
        
        // Verify this is treated as an exception (login failure)
        assertNotNull(record);
        assertNotNull(record.getException());
        assertEquals(Constants.LOGIN_FAILED, record.getException().getExceptionTypeId());
        assertTrue(record.getException().getDescription().contains("Login failed for user 'test'"));
        assertEquals("test", record.getAccessor().getDbUser());
        assertEquals("local machine", record.getSessionLocator().getClientIp());
        assertEquals(Constants.DEFAULT_PORT, record.getSessionLocator().getClientPort());
        assertEquals("0", record.getSessionId());
        assertEquals(Constants.NA, record.getDbName());  // Empty database name
    }
}