package com.ibm.guardium.test.s3sqspostgresql;

import co.elastic.logstash.api.Event;
import com.ibm.guardium.s3sqspostgresql.Constants;
import com.ibm.guardium.s3sqspostgresql.Parser;
import com.ibm.guardium.universalconnector.commons.structures.*;
import org.junit.Test;
import sun.security.krb5.internal.PAEncTSEnc;

import java.text.ParseException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.ibm.guardium.s3sqspostgresql.Parser.getInstanceName;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class S3SQSPostgresqlGuardiumPluginFilterTest {

    @Test
    public void testParseRecordWithSuccessSql() throws ParseException {
        Event mockEvent = mock(Event.class);

        Map<String, Object> parsedMsg = new HashMap<>();
        parsedMsg.put(Constants.DATABASE_NAME, "testdb");
        parsedMsg.put(Constants.USER_NAME, "user1");
        parsedMsg.put(Constants.CONNECTION_FROM, "10.0.0.1:1234");
        parsedMsg.put(Constants.SQL_STATE_CODE, Constants.SQL_STATE_CODE_SUCCESS);

        when(mockEvent.getField(Constants.PARSED_MESSAGE)).thenReturn(parsedMsg);
        when(mockEvent.getField(Constants.SESSION_ID)).thenReturn("11");
        when(mockEvent.getField(Constants.FULL_SQL_QUERY)).thenReturn("SELECT * FROM table;");
        when(mockEvent.getField(Constants.TIMESTAMP)).thenReturn("2023-11-10T10:15:30Z");
        when(mockEvent.getField(Constants.ACCOUNT_ID)).thenReturn("123456");
        when(mockEvent.getField(Constants.INSTANCE_NAME)).thenReturn("postgres");
        when(mockEvent.getField(Constants.SERVER_HOST_NAME)).thenReturn("123456:postgres");

        Record record = Parser.parseRecord(mockEvent);

        assertNotNull(record.getData());
        assertEquals("SELECT * FROM table;", record.getData().getOriginalSqlCommand());
        assertEquals("user1", record.getAccessor().getDbUser());
        assertEquals("10.0.0.1", record.getSessionLocator().getClientIp());
        assertEquals("123456:postgres", record.getAccessor().getServerHostName());
        assertEquals("123456:postgres:testdb", record.getAccessor().getServiceName());
        assertEquals("123456:postgres:testdb", record.getDbName());
    }

    @Test
    public void testSQLQueryWithDoubleQuote() throws ParseException {
        Event mockEvent = mock(Event.class);

        Map<String, Object> parsedMsg = new HashMap<>();
        parsedMsg.put(Constants.DATABASE_NAME, "testdb");
        parsedMsg.put(Constants.USER_NAME, "user1");
        parsedMsg.put(Constants.CONNECTION_FROM, "10.0.0.1:1234");
        parsedMsg.put(Constants.SQL_STATE_CODE, Constants.SQL_STATE_CODE_SUCCESS);

        when(mockEvent.getField(Constants.PARSED_MESSAGE)).thenReturn(parsedMsg);
        when(mockEvent.getField(Constants.SESSION_ID)).thenReturn("11");
        when(mockEvent.getField(Constants.FULL_SQL_QUERY)).thenReturn("SELECT COUNT(*) AS \"\"RECORDS\"\" from \"table\";");
        when(mockEvent.getField(Constants.TIMESTAMP)).thenReturn("2023-11-10T10:15:30Z");
        when(mockEvent.getField(Constants.ACCOUNT_ID)).thenReturn("123456");
        when(mockEvent.getField(Constants.INSTANCE_NAME)).thenReturn("postgres");
        when(mockEvent.getField(Constants.SERVER_HOST_NAME)).thenReturn("123456:postgres");

        Record record = Parser.parseRecord(mockEvent);

        assertNotNull(record.getData());
        assertEquals("SELECT COUNT(*) AS \"RECORDS\" from \"table\";", record.getData().getOriginalSqlCommand());
        assertEquals("user1", record.getAccessor().getDbUser());
        assertEquals("10.0.0.1", record.getSessionLocator().getClientIp());
        assertEquals("123456:postgres", record.getAccessor().getServerHostName());
        assertEquals("123456:postgres:testdb", record.getAccessor().getServiceName());
        assertEquals("123456:postgres:testdb", record.getDbName());
    }

    @Test
    public void testParseTimestampValid() {
        Event mockEvent = mock(Event.class);
        when(mockEvent.getField(Constants.TIMESTAMP)).thenReturn("2023-11-10T10:15:30Z");

        Time time = Parser.parseTimestamp(mockEvent);

        assertTrue(time.getTimstamp() > 0);
    }

    @Test
    public void testParseTimestampInvalid() {
        Event mockEvent = mock(Event.class);
        when(mockEvent.getField(Constants.TIMESTAMP)).thenReturn("invalid-date");

        Time time = Parser.parseTimestamp(mockEvent);

        assertEquals(0, time.getTimstamp());
    }

    @Test
    public void testParseAccessorWithDefaults() {
        Event mockEvent = mock(Event.class);
        when(mockEvent.getField(Constants.PARSED_MESSAGE)).thenReturn(null);
        when(mockEvent.getField(Constants.ACCOUNT_ID)).thenReturn("123456");
        when(mockEvent.getField(Constants.INSTANCE_NAME)).thenReturn("postgres");
        when(mockEvent.getField(Constants.SERVER_HOST_NAME)).thenReturn("123456:postgres");

        Accessor accessor = Parser.parseAccessor(mockEvent);

        assertEquals(Constants.NA, accessor.getClientHostName());
        assertEquals(Constants.SERVER_TYPE_STRING, accessor.getServerType());
        assertEquals("123456:postgres", accessor.getServerHostName());
    }

    @Test
    public void testParseAccessorWithParsedMessage() {
        Event mockEvent = mock(Event.class);
        Map<String, Object> parsedMsg = new HashMap<>();
        parsedMsg.put(Constants.USER_NAME, "dbuser");
        parsedMsg.put(Constants.DATABASE_NAME, "service_db");
        parsedMsg.put(Constants.APPLICATION_NAME, "app1");

        when(mockEvent.getField(Constants.PARSED_MESSAGE)).thenReturn(parsedMsg);
        when(mockEvent.getField(Constants.ACCOUNT_ID)).thenReturn("123456");
        when(mockEvent.getField(Constants.INSTANCE_NAME)).thenReturn("postgres");
        when(mockEvent.getField(Constants.SERVER_HOST_NAME)).thenReturn("123456:postgres");

        Accessor accessor = Parser.parseAccessor(mockEvent);

        assertEquals("dbuser", accessor.getDbUser());
        //assertEquals("service_db", accessor.getServiceName());
        assertEquals("app1", accessor.getSourceProgram());
        assertEquals("123456:postgres", accessor.getServerHostName());

    }

    @Test
    public void ErrorTestCaseCheck() throws Exception {
        Event event = mock(Event.class);

        Map<String, Object> parsedMessage = new HashMap<>();
        parsedMessage.put("backend_type", "client backend");
        parsedMessage.put("database_name", "mydb");
        parsedMessage.put("internal_query", "");
        parsedMessage.put("detail", "");
        parsedMessage.put("log_time", "2025-07-21 08:22:21.726+00");
        parsedMessage.put("query_id", "0");
        parsedMessage.put("virtual_transaction_id", "5/633");
        parsedMessage.put("session_start_time", "2025-07-21 05:03:18+00");
        parsedMessage.put("process_id", "1510");
        parsedMessage.put("application_name", "DBeaver 25.0.3 - SQLEditor <Script-21.sql>");
        parsedMessage.put("leader_pid", "");
        parsedMessage.put("hint", "");
        parsedMessage.put("query_pos", "1");
        parsedMessage.put("message", "syntax error at or near \"drogp\"");
        parsedMessage.put("connection_from", "223.233.87.228:22408");
        parsedMessage.put("session_line_num", "458");
        parsedMessage.put("location", "");
        parsedMessage.put("transaction_id", "0");
        parsedMessage.put("user_name", "Admin123");
        parsedMessage.put("session_id", "687dca16.5e6");
        parsedMessage.put("sql_state_code", "42601");
        parsedMessage.put("error_severity", "ERROR");
        parsedMessage.put("internal_query_pos", "");
        parsedMessage.put("context", "");
        parsedMessage.put("query", "drogp table PANY");
        parsedMessage.put("command_tag", "PARSE");

        when(event.getField("parsed_message")).thenReturn(parsedMessage);
        when(event.getField("account_id")).thenReturn("123456");
        when(event.getField("timestamp")).thenReturn("2025-07-21T08:23:01.903324202Z");
        when(event.getField("session_id")).thenReturn("687dca16.5e6");
        when(event.getField("message")).thenReturn("syntax error at or near \"drogp\"");
        when(event.getField("succeeded")).thenReturn("ERROR");
        when(event.getField("prefix")).thenReturn("42601");
        when(event.getField("instance_name")).thenReturn("postgres");
        when(event.getField("server_hostname")).thenReturn("123456:postgres");

        Record record = Parser.parseRecord(event);

        assertNotNull(record);
        assertEquals("123456:postgres:mydb", record.getDbName());

        assertNotNull(record.getAccessor());
        assertEquals("Admin123", record.getAccessor().getDbUser());
        assertEquals("123456:postgres:mydb", record.getAccessor().getServiceName());
        assertEquals("DBeaver 25.0.3 - SQLEditor <Script-21.sql>", record.getAccessor().getSourceProgram());

        assertNotNull(record.getSessionLocator());
        assertEquals(Constants.DEFAULT_IP, record.getSessionLocator().getClientIp());
        assertEquals(Constants.DEFAULT_PORT, record.getSessionLocator().getClientPort());
        assertEquals("123456:postgres", record.getAccessor().getServerHostName());

        assertNotNull(record.getTime());
        assertTrue(record.getTime().getTimstamp() > 0);

        assertEquals("", record.getSessionId());

        assertNotNull(record.getException());
    }

    @Test
    public void testParseRecordFromSampleEventNow() throws Exception {
        Event event = mock(Event.class);

        Map<String, Object> parsedMessage = new HashMap<>();
        parsedMessage.put(Constants.DATABASE_NAME, "mypgdb");
        parsedMessage.put(Constants.USER_NAME, "postgresadmin");
        parsedMessage.put(Constants.CONNECTION_FROM, "223.233.87.243:31637");
        parsedMessage.put(Constants.SQL_STATE_CODE, Constants.SQL_STATE_CODE_SUCCESS);
        parsedMessage.put(Constants.APPLICATION_NAME, "NA");
        parsedMessage.put(Constants.MESSAGE, "INSERT INTO test123 (first_name, last_name, department, salary) VALUES ('Alice', 'Johnson', 'HR', 55000.00)");

        when(event.getField(Constants.PARSED_MESSAGE)).thenReturn(parsedMessage);
        when(event.getField(Constants.SESSION_ID)).thenReturn("1433");
        when(event.getField(Constants.FULL_SQL_QUERY)).thenReturn("INSERT INTO test123 (first_name, last_name, department, salary) VALUES ('Alice', 'Johnson', 'HR', 55000.00)");
        when(event.getField(Constants.TIMESTAMP)).thenReturn("2025-08-22T09:57:21Z");
        when(event.getField(Constants.ACCOUNT_ID)).thenReturn("123456");
        when(event.getField(Constants.INSTANCE_NAME)).thenReturn("postgres");
        when(event.getField(Constants.SERVER_HOST_NAME)).thenReturn("123456:postgres");

        Record record = Parser.parseRecord(event);

        assertNotNull(record);
        assertEquals("123456:postgres:mypgdb", record.getDbName());

        assertNotNull(record.getAccessor());
        assertEquals("postgresadmin", record.getAccessor().getDbUser());
        assertEquals("123456:postgres:mypgdb", record.getAccessor().getServiceName());

        assertNotNull(record.getSessionLocator());
        assertEquals("223.233.87.243", record.getSessionLocator().getClientIp());
        assertEquals(31637, record.getSessionLocator().getClientPort());
        assertEquals("123456:mypgdb", record.getSessionLocator().getServerIp());
        assertEquals("123456:postgres", record.getAccessor().getServerHostName());

        assertNotNull(record.getTime());
        assertTrue(record.getTime().getTimstamp() > 0);

        assertEquals("", record.getSessionId());

        assertNotNull(record.getData());
        assertEquals("INSERT INTO test123 (first_name, last_name, department, salary) VALUES ('Alice', 'Johnson', 'HR', 55000.00)",
                record.getData().getOriginalSqlCommand());
    }

    @Test
    public void testParseRecordFromEventNowEFG789() throws Exception {
        Event event = mock(Event.class);

        Map<String, Object> parsedMessage = new HashMap<>();
        parsedMessage.put(Constants.DATABASE_NAME, "mypgdb");
        parsedMessage.put(Constants.USER_NAME, "postgresadmin");
        parsedMessage.put(Constants.CONNECTION_FROM, "223.233.87.243:27930");
        parsedMessage.put(Constants.SQL_STATE_CODE, Constants.SQL_STATE_CODE_SUCCESS);
        parsedMessage.put(Constants.APPLICATION_NAME, "NA");
        parsedMessage.put(Constants.MESSAGE, "INSERT INTO EFG789 (first_name, last_name, department, salary) VALUES ('Bob', 'Smith', 'IT', 65000.00)");

        when(event.getField(Constants.PARSED_MESSAGE)).thenReturn(parsedMessage);
        when(event.getField(Constants.SESSION_ID)).thenReturn("23");
        when(event.getField(Constants.FULL_SQL_QUERY)).thenReturn("INSERT INTO EFG789 (first_name, last_name, department, salary) VALUES ('Bob', 'Smith', 'IT', 65000.00)");
        when(event.getField(Constants.TIMESTAMP)).thenReturn("2025-08-23T07:30:48.706Z");
        when(event.getField(Constants.ACCOUNT_ID)).thenReturn("123456");
        when(event.getField(Constants.INSTANCE_NAME)).thenReturn("postgres");
        when(event.getField(Constants.SERVER_HOST_NAME)).thenReturn("123456:postgres");

        Record record = Parser.parseRecord(event);

        // Validate main record fields
        assertNotNull(record);
        assertEquals("123456:postgres:mypgdb", record.getDbName());
        assertEquals("", record.getSessionId());

        // Validate accessor
        assertNotNull(record.getAccessor());
        assertEquals("postgresadmin", record.getAccessor().getDbUser());
        assertEquals("123456:postgres:mypgdb", record.getAccessor().getServiceName());
        assertEquals("NA", record.getAccessor().getSourceProgram());
        assertEquals("123456:postgres", record.getAccessor().getServerHostName());

        // Validate session locator
        assertNotNull(record.getSessionLocator());
        assertEquals("223.233.87.243", record.getSessionLocator().getClientIp());
        assertEquals(27930, record.getSessionLocator().getClientPort());
        assertEquals("123456:mypgdb", record.getSessionLocator().getServerIp());

        // Validate time
        assertNotNull(record.getTime());
        assertTrue(record.getTime().getTimstamp() > 0);

        // Validate SQL command
        assertNotNull(record.getData());
        assertEquals("INSERT INTO EFG789 (first_name, last_name, department, salary) VALUES ('Bob', 'Smith', 'IT', 65000.00)",
                record.getData().getOriginalSqlCommand());
    }

    @Test
    public void testParseRecordFromJHTV123InsertEvent() throws Exception {
        Event event = mock(Event.class);

        // Build parsed message as per the event log
        Map<String, Object> parsedMessage = new HashMap<>();
        parsedMessage.put(Constants.DATABASE_NAME, "mypgdb");
        parsedMessage.put(Constants.USER_NAME, "postgresadmin");
        parsedMessage.put(Constants.CONNECTION_FROM, "223.233.87.243:29370");
        parsedMessage.put(Constants.SQL_STATE_CODE, Constants.SQL_STATE_CODE_SUCCESS);
        parsedMessage.put(Constants.APPLICATION_NAME, "NA");
        parsedMessage.put(Constants.MESSAGE, "INSERT INTO JHTV123 (first_name, last_name, department, salary) VALUES ('Bob', 'Smith', 'IT', 65000.00)");

        // Mock the event fields
        when(event.getField(Constants.PARSED_MESSAGE)).thenReturn(parsedMessage);
        when(event.getField(Constants.SESSION_ID)).thenReturn("");
        when(event.getField(Constants.FULL_SQL_QUERY)).thenReturn("INSERT INTO JHTV123 (first_name, last_name, department, salary) VALUES ('Bob', 'Smith', 'IT', 65000.00)");
        when(event.getField(Constants.TIMESTAMP)).thenReturn("2025-08-23T08:55:48Z");
        when(event.getField(Constants.ACCOUNT_ID)).thenReturn("123456");
        when(event.getField(Constants.INSTANCE_NAME)).thenReturn("postgres");
        when(event.getField(Constants.SERVER_HOST_NAME)).thenReturn("123456:postgres");

        // Parse the record
        Record record = Parser.parseRecord(event);

        // Validate main record fields
        assertNotNull(record);
        assertEquals("123456:postgres:mypgdb", record.getDbName());
        assertEquals("", record.getSessionId());

        // Validate accessor
        assertNotNull(record.getAccessor());
        assertEquals("postgresadmin", record.getAccessor().getDbUser());
        assertEquals("123456:postgres:mypgdb", record.getAccessor().getServiceName());
        assertEquals("NA", record.getAccessor().getSourceProgram());
        assertEquals("123456:postgres", record.getAccessor().getServerHostName());

        // Validate session locator
        assertNotNull(record.getSessionLocator());
        assertEquals("223.233.87.243", record.getSessionLocator().getClientIp());
        assertEquals(29370, record.getSessionLocator().getClientPort());
        assertEquals("123456:mypgdb", record.getSessionLocator().getServerIp());

        // Validate time
        assertNotNull(record.getTime());
        assertTrue(record.getTime().getTimstamp() > 0);

        // Validate SQL command
        assertNotNull(record.getData());
        assertEquals(
                "INSERT INTO JHTV123 (first_name, last_name, department, salary) VALUES ('Bob', 'Smith', 'IT', 65000.00)",
                record.getData().getOriginalSqlCommand()
        );
    }
    @Test
    public void testParseServerHostName() throws ParseException {
        Event mockEvent = mock(Event.class);

        Map<String, Object> parsedMsg = new HashMap<>();
        parsedMsg.put(Constants.DATABASE_NAME, "testdb");
        parsedMsg.put(Constants.USER_NAME, "user1");
        parsedMsg.put(Constants.CONNECTION_FROM, "10.0.0.1:1234");
        parsedMsg.put(Constants.SQL_STATE_CODE, Constants.SQL_STATE_CODE_SUCCESS);

        when(mockEvent.getField(Constants.PARSED_MESSAGE)).thenReturn(parsedMsg);
        when(mockEvent.getField(Constants.ACCOUNT_ID)).thenReturn("123456");
        when(mockEvent.getField(Constants.INSTANCE_NAME)).thenReturn("postgres");

        Record record = Parser.parseRecord(mockEvent);
        assertEquals("123456:postgres", record.getAccessor().getServerHostName());
    }

    @Test
    public void testGetInstanceName_withString() throws Exception{
        Event mockEvent = mock(Event.class);
        when(mockEvent.getField(Constants.INSTANCE_NAME)).thenReturn("Postgres");

        String result = Parser.getInstanceName(mockEvent);
        assertEquals("Postgres", result);
    }

    @Test
    public void testGetInstanceName_withListContainingValue() throws Exception{
            Event mockEvent = mock(Event.class);
            List<String> instanceList = Arrays.asList("Postgres", "OtherValue");
            when(mockEvent.getField(Constants.INSTANCE_NAME)).thenReturn(instanceList);

            String result = Parser.getInstanceName(mockEvent);
            assertEquals("Postgres", result);
    }
}
