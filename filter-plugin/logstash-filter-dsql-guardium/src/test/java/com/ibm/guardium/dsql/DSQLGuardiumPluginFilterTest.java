package com.ibm.guardium.dsql;

import co.elastic.logstash.api.Event;
import com.ibm.guardium.universalconnector.commons.structures.Record;
import org.junit.Test;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class DSQLGuardiumPluginFilterTest {

    @Test
    public void testParseRecordWithSuccessfulStatement() throws ParseException {
        Event mockEvent = mock(Event.class);

        // Mock event data with DSQL audit log fields
        Map<String, Object> eventData = new HashMap<>();
        eventData.put(Constants.TYPE, "record");
        eventData.put(Constants.DATABASE_NAME, "testdb");
        eventData.put(Constants.DB_USER_NAME, "testuser");
        eventData.put(Constants.REMOTE_HOST, "10.0.0.1");
        eventData.put(Constants.REMOTE_PORT, "5432");
        eventData.put(Constants.SESSION_ID, "session123");
        eventData.put(Constants.STATEMENT_TEXT, "SELECT * FROM users WHERE id = 1;");
        eventData.put(Constants.EXIT_CODE, "0");
        eventData.put(Constants.LOG_TIME, "2023-11-10T10:15:30Z");
        eventData.put(Constants.CLIENT_APPLICATION, "psql");

        when(mockEvent.getData()).thenReturn(eventData);
        when(mockEvent.getField(Constants.DATABASE_NAME)).thenReturn("testdb");
        when(mockEvent.getField(Constants.DB_USER_NAME)).thenReturn("testuser");
        when(mockEvent.getField(Constants.REMOTE_HOST)).thenReturn("10.0.0.1");
        when(mockEvent.getField(Constants.REMOTE_PORT)).thenReturn("5432");
        when(mockEvent.getField(Constants.SESSION_ID)).thenReturn("session123");
        when(mockEvent.getField(Constants.STATEMENT_TEXT)).thenReturn("SELECT * FROM users WHERE id = 1;");
        when(mockEvent.getField(Constants.EXIT_CODE)).thenReturn("0");
        when(mockEvent.getField(Constants.LOG_TIME)).thenReturn("2023-11-10T10:15:30Z");
        when(mockEvent.getField(Constants.CLIENT_APPLICATION)).thenReturn("psql");
        when(mockEvent.getField(Constants.ACCOUNT_ID)).thenReturn("123456789012");
        when(mockEvent.getField(Constants.INSTANCE_NAME)).thenReturn("dsql-cluster-1");

        Record record = Parser.parseRecord(mockEvent);

        assertNotNull(record);
        assertNotNull(record.getData());
        assertEquals("SELECT * FROM users WHERE id = 1;", record.getData().getOriginalSqlCommand());
        assertEquals("testuser", record.getAccessor().getDbUser());
        assertEquals("10.0.0.1", record.getSessionLocator().getClientIp());
        assertEquals(5432, record.getSessionLocator().getClientPort());
        assertEquals("session123", record.getSessionId());
        assertEquals(Constants.SERVER_TYPE_STRING, record.getAccessor().getServerType());
        assertEquals(Constants.DATA_PROTOCOL_STRING, record.getAccessor().getDbProtocol());
        assertEquals(Constants.LANGUAGE, record.getAccessor().getLanguage());
        assertEquals("psql", record.getAccessor().getSourceProgram());
    }

    @Test
    public void testParseRecordWithError() throws ParseException {
        Event mockEvent = mock(Event.class);

        Map<String, Object> eventData = new HashMap<>();
        eventData.put(Constants.TYPE, "record");
        eventData.put(Constants.DATABASE_NAME, "testdb");
        eventData.put(Constants.DB_USER_NAME, "testuser");
        eventData.put(Constants.REMOTE_HOST, "10.0.0.1");
        eventData.put(Constants.REMOTE_PORT, "5432");
        eventData.put(Constants.SESSION_ID, "session123");
        eventData.put(Constants.STATEMENT_TEXT, "SELECT * FROM nonexistent_table;");
        eventData.put(Constants.EXIT_CODE, "1");
        eventData.put(Constants.ERROR_MESSAGE, "relation \"nonexistent_table\" does not exist");
        eventData.put(Constants.LOG_TIME, "2023-11-10T10:15:30Z");

        when(mockEvent.getData()).thenReturn(eventData);
        when(mockEvent.getField(Constants.DATABASE_NAME)).thenReturn("testdb");
        when(mockEvent.getField(Constants.DB_USER_NAME)).thenReturn("testuser");
        when(mockEvent.getField(Constants.REMOTE_HOST)).thenReturn("10.0.0.1");
        when(mockEvent.getField(Constants.REMOTE_PORT)).thenReturn("5432");
        when(mockEvent.getField(Constants.SESSION_ID)).thenReturn("session123");
        when(mockEvent.getField(Constants.STATEMENT_TEXT)).thenReturn("SELECT * FROM nonexistent_table;");
        when(mockEvent.getField(Constants.EXIT_CODE)).thenReturn("1");
        when(mockEvent.getField(Constants.ERROR_MESSAGE)).thenReturn("relation \"nonexistent_table\" does not exist");
        when(mockEvent.getField(Constants.LOG_TIME)).thenReturn("2023-11-10T10:15:30Z");
        when(mockEvent.getField(Constants.ACCOUNT_ID)).thenReturn("123456789012");
        when(mockEvent.getField(Constants.INSTANCE_NAME)).thenReturn("dsql-cluster-1");

        Record record = Parser.parseRecord(mockEvent);

        assertNotNull(record);
        assertNotNull(record.getException());
        assertEquals(Constants.SQL_ERROR, record.getException().getExceptionTypeId());
        assertEquals("relation \"nonexistent_table\" does not exist", record.getException().getDescription());
        assertEquals("SELECT * FROM nonexistent_table;", record.getException().getSqlString());
    }

    @Test
    public void testParseRecordWithLoginFailure() throws ParseException {
        Event mockEvent = mock(Event.class);

        Map<String, Object> eventData = new HashMap<>();
        eventData.put(Constants.TYPE, "record");
        eventData.put(Constants.DATABASE_NAME, "testdb");
        eventData.put(Constants.DB_USER_NAME, "testuser");
        eventData.put(Constants.REMOTE_HOST, "10.0.0.1");
        eventData.put(Constants.REMOTE_PORT, "5432");
        eventData.put(Constants.SESSION_ID, "session123");
        eventData.put(Constants.EXIT_CODE, "1");
        eventData.put(Constants.ERROR_MESSAGE, "password authentication failed for user \"testuser\"");
        eventData.put(Constants.LOG_TIME, "2023-11-10T10:15:30Z");

        when(mockEvent.getData()).thenReturn(eventData);
        when(mockEvent.getField(Constants.DATABASE_NAME)).thenReturn("testdb");
        when(mockEvent.getField(Constants.DB_USER_NAME)).thenReturn("testuser");
        when(mockEvent.getField(Constants.REMOTE_HOST)).thenReturn("10.0.0.1");
        when(mockEvent.getField(Constants.REMOTE_PORT)).thenReturn("5432");
        when(mockEvent.getField(Constants.SESSION_ID)).thenReturn("session123");
        when(mockEvent.getField(Constants.EXIT_CODE)).thenReturn("1");
        when(mockEvent.getField(Constants.ERROR_MESSAGE)).thenReturn("password authentication failed for user \"testuser\"");
        when(mockEvent.getField(Constants.LOG_TIME)).thenReturn("2023-11-10T10:15:30Z");
        when(mockEvent.getField(Constants.ACCOUNT_ID)).thenReturn("123456789012");
        when(mockEvent.getField(Constants.INSTANCE_NAME)).thenReturn("dsql-cluster-1");

        Record record = Parser.parseRecord(mockEvent);

        assertNotNull(record);
        assertNotNull(record.getException());
        assertEquals(Constants.LOGIN_FAILED, record.getException().getExceptionTypeId());
        assertTrue(record.getException().getDescription().contains("password authentication failed"));
    }

    @Test
    public void testParseTimestampValid() throws ParseException {
        Event mockEvent = mock(Event.class);

        Map<String, Object> eventData = new HashMap<>();
        eventData.put(Constants.LOG_TIME, "2023-11-10T10:15:30Z");
        eventData.put(Constants.DATABASE_NAME, "testdb");
        eventData.put(Constants.DB_USER_NAME, "testuser");
        eventData.put(Constants.EXIT_CODE, "0");
        eventData.put(Constants.STATEMENT_TEXT, "SELECT 1;");

        when(mockEvent.getData()).thenReturn(eventData);
        when(mockEvent.getField(Constants.LOG_TIME)).thenReturn("2023-11-10T10:15:30Z");
        when(mockEvent.getField(Constants.DATABASE_NAME)).thenReturn("testdb");
        when(mockEvent.getField(Constants.DB_USER_NAME)).thenReturn("testuser");
        when(mockEvent.getField(Constants.EXIT_CODE)).thenReturn("0");
        when(mockEvent.getField(Constants.STATEMENT_TEXT)).thenReturn("SELECT 1;");
        when(mockEvent.getField(Constants.ACCOUNT_ID)).thenReturn("123456789012");
        when(mockEvent.getField(Constants.INSTANCE_NAME)).thenReturn("dsql-cluster-1");

        Record record = Parser.parseRecord(mockEvent);

        assertNotNull(record.getTime());
        assertTrue(record.getTime().getTimstamp() > 0);
    }

    @Test
    public void testParseDbName() throws ParseException {
        Event mockEvent = mock(Event.class);

        Map<String, Object> eventData = new HashMap<>();
        eventData.put(Constants.DATABASE_NAME, "production_db");
        eventData.put(Constants.DB_USER_NAME, "admin");
        eventData.put(Constants.EXIT_CODE, "0");
        eventData.put(Constants.STATEMENT_TEXT, "SELECT 1;");
        eventData.put(Constants.LOG_TIME, "2023-11-10T10:15:30Z");

        when(mockEvent.getData()).thenReturn(eventData);
        when(mockEvent.getField(Constants.DATABASE_NAME)).thenReturn("production_db");
        when(mockEvent.getField(Constants.DB_USER_NAME)).thenReturn("admin");
        when(mockEvent.getField(Constants.EXIT_CODE)).thenReturn("0");
        when(mockEvent.getField(Constants.STATEMENT_TEXT)).thenReturn("SELECT 1;");
        when(mockEvent.getField(Constants.LOG_TIME)).thenReturn("2023-11-10T10:15:30Z");
        when(mockEvent.getField(Constants.ACCOUNT_ID)).thenReturn("123456789012");
        when(mockEvent.getField(Constants.INSTANCE_NAME)).thenReturn("dsql-cluster-1");

        Record record = Parser.parseRecord(mockEvent);

        assertNotNull(record.getDbName());
        assertEquals("123456789012:dsql-cluster-1:production_db", record.getDbName());
        assertEquals("123456789012:dsql-cluster-1:production_db", record.getAccessor().getServiceName());
    }

    @Test
    public void testParseSessionLocator() throws ParseException {
        Event mockEvent = mock(Event.class);

        Map<String, Object> eventData = new HashMap<>();
        eventData.put(Constants.DATABASE_NAME, "testdb");
        eventData.put(Constants.DB_USER_NAME, "testuser");
        eventData.put(Constants.REMOTE_HOST, "192.168.1.100");
        eventData.put(Constants.REMOTE_PORT, "54321");
        eventData.put(Constants.EXIT_CODE, "0");
        eventData.put(Constants.STATEMENT_TEXT, "SELECT 1;");
        eventData.put(Constants.LOG_TIME, "2023-11-10T10:15:30Z");

        when(mockEvent.getData()).thenReturn(eventData);
        when(mockEvent.getField(Constants.DATABASE_NAME)).thenReturn("testdb");
        when(mockEvent.getField(Constants.DB_USER_NAME)).thenReturn("testuser");
        when(mockEvent.getField(Constants.REMOTE_HOST)).thenReturn("192.168.1.100");
        when(mockEvent.getField(Constants.REMOTE_PORT)).thenReturn("54321");
        when(mockEvent.getField(Constants.EXIT_CODE)).thenReturn("0");
        when(mockEvent.getField(Constants.STATEMENT_TEXT)).thenReturn("SELECT 1;");
        when(mockEvent.getField(Constants.LOG_TIME)).thenReturn("2023-11-10T10:15:30Z");
        when(mockEvent.getField(Constants.ACCOUNT_ID)).thenReturn("123456789012");
        when(mockEvent.getField(Constants.INSTANCE_NAME)).thenReturn("dsql-cluster-1");

        Record record = Parser.parseRecord(mockEvent);

        assertNotNull(record.getSessionLocator());
        assertEquals("192.168.1.100", record.getSessionLocator().getClientIp());
        assertEquals(54321, record.getSessionLocator().getClientPort());
        assertEquals("123456789012:dsql-cluster-1:testdb", record.getSessionLocator().getServerIp());
        assertFalse(record.getSessionLocator().isIpv6());
    }
}

// Made with Bob
