package com.ibm.guardium.documentdb;

import static org.junit.jupiter.api.Assertions.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.logstash.plugins.ContextImpl;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ibm.guardium.universalconnector.commons.GuardConstants;
import com.ibm.guardium.universalconnector.commons.structures.Record;

import co.elastic.logstash.api.Context;
import co.elastic.logstash.api.Event;
import co.elastic.logstash.api.FilterMatchListener;

/**
 * Test class for authCheck audit log parsing in DocumentDB Guardium filter.
 * Tests the parsing of authCheck events from CSV file containing 200 real audit logs.
 */
public class AuthCheckParserTest {

    private static final Context context = new ContextImpl(null, null);
    private static final Gson gson = new Gson();

    /**
     * Helper method to read authCheck audit logs from CSV file
     */
    private List<String> readAuditLogsFromCSV() throws IOException {
        List<String> auditLogs = new ArrayList<>();
        
        try (InputStream is = getClass().getClassLoader()
                .getResourceAsStream("documentdb/authcheck_audit_logs.csv");
             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            
            // Skip header line
            reader.readLine();
            
            String line;
            while ((line = reader.readLine()) != null) {
                // Split by comma, but handle the JSON message field which contains commas
                int firstComma = line.indexOf(',');
                if (firstComma > 0) {
                    String message = line.substring(firstComma + 1);
                    // Remove surrounding quotes if present
                    if (message.startsWith("\"") && message.endsWith("\"")) {
                        message = message.substring(1, message.length() - 1);
                    }
                    // Unescape double quotes
                    message = message.replace("\"\"", "\"");
                    auditLogs.add(message);
                }
            }
        }
        
        return auditLogs;
    }

    /**
     * Helper method to parse a specific record by index
     */
    private Record parseRecordByIndex(int index) throws Exception {
        List<String> auditLogs = readAuditLogsFromCSV();
        assertTrue(index > 0 && index <= auditLogs.size(), 
            "Index " + index + " out of range. Total records: " + auditLogs.size());
        
        String auditLog = auditLogs.get(index - 1); // Convert to 0-based index
        
        DocumentdbGuardiumFilter filter = new DocumentdbGuardiumFilter("test-id", null, context);
        Event e = new org.logstash.Event();
        TestMatchListener matchListener = new TestMatchListener();
        e.setField("message", auditLog);
        e.setField("serverHostnamePrefix", "test-cluster");
        
        Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
        
        assertEquals(1, results.size(), "Should have 1 result");
        assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME), 
            "GuardRecord should not be null");
        
        String recordString = e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME).toString();
        return gson.fromJson(recordString, Record.class);
    }

    /**
     * Main test: Parse all 200 authCheck audit logs from CSV
     */
    @Test
    public void testParseMultipleAuthCheckAuditLogs() throws Exception {
        List<String> auditLogs = readAuditLogsFromCSV();
        
        assertEquals(200, auditLogs.size(), "Should have 200 audit logs in CSV");
        
        DocumentdbGuardiumFilter filter = new DocumentdbGuardiumFilter("test-id", null, context);
        TestMatchListener matchListener = new TestMatchListener();
        
        int successCount = 0;
        List<String> errors = new ArrayList<>();
        
        for (int i = 0; i < auditLogs.size(); i++) {
            String auditLog = auditLogs.get(i);
            try {
                Event e = new org.logstash.Event();
                e.setField("message", auditLog);
                e.setField("serverHostnamePrefix", "test-cluster");
                
                Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
                
                assertNotNull(results, "Results should not be null for record " + (i + 1));
                assertEquals(1, results.size(), "Should have 1 result for record " + (i + 1));
                
                Object guardRecord = e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME);
                assertNotNull(guardRecord, "GuardRecord should not be null for record " + (i + 1));
                
                // Verify it's valid JSON
                String recordString = guardRecord.toString();
                Record record = gson.fromJson(recordString, Record.class);
                assertNotNull(record, "Parsed record should not be null for record " + (i + 1));
                
                successCount++;
            } catch (Exception ex) {
                errors.add("Record " + (i + 1) + ": " + ex.getMessage());
            }
        }
        
        if (!errors.isEmpty()) {
            fail("Failed to parse " + errors.size() + " records:\n" + String.join("\n", errors));
        }
        
        assertEquals(200, successCount, "All 200 records should parse successfully");
        assertEquals(200, matchListener.getMatchCount(), "All 200 records should match");
    }

    /**
     * Test parsing of record #7 - validates specific field values
     */
    @Test
    public void testAuthCheckRecord7() throws Exception {
        Record record = parseRecordByIndex(7);
        
        assertNotNull(record);
        assertEquals("readwrite", record.getAccessor().getDbUser());
        // dbName includes serverHostnamePrefix in Logstash version
        assertEquals("test-cluster:admin", record.getDbName());
        assertNotNull(record.getSessionLocator());
        assertEquals("203.0.113.10", record.getSessionLocator().getClientIp());
        // Verify port was parsed (actual value may vary per record)
        assertTrue(record.getSessionLocator().getClientPort() > 0,
            "Client port should be parsed and positive");
    }

    /**
     * Test parsing of record #20
     */
    @Test
    public void testAuthCheckRecord20() throws Exception {
        Record record = parseRecordByIndex(20);
        
        assertNotNull(record);
        assertEquals("readwrite", record.getAccessor().getDbUser());
        assertNotNull(record.getData());
        assertNotNull(record.getData().getConstruct());
    }

    /**
     * Test parsing of record #25
     */
    @Test
    public void testAuthCheckRecord25() throws Exception {
        Record record = parseRecordByIndex(25);
        
        assertNotNull(record);
        assertEquals("readwrite", record.getAccessor().getDbUser());
        assertEquals("203.0.113.10", record.getSessionLocator().getClientIp());
    }

    /**
     * Test parsing of record #30
     */
    @Test
    public void testAuthCheckRecord30() throws Exception {
        Record record = parseRecordByIndex(30);
        
        assertNotNull(record);
        assertNotNull(record.getAccessor());
        assertNotNull(record.getSessionLocator());
    }

    /**
     * Test parsing of record #54
     */
    @Test
    public void testAuthCheckRecord54() throws Exception {
        Record record = parseRecordByIndex(54);
        
        assertNotNull(record);
        assertEquals("readwrite", record.getAccessor().getDbUser());
    }

    /**
     * Test parsing of record #167
     */
    @Test
    public void testAuthCheckRecord167() throws Exception {
        Record record = parseRecordByIndex(167);
        
        assertNotNull(record);
        assertNotNull(record.getData());
    }

    /**
     * Test parsing of record #200 (last record)
     */
    @Test
    public void testAuthCheckRecord200() throws Exception {
        Record record = parseRecordByIndex(200);
        
        assertNotNull(record);
        assertEquals("readwrite", record.getAccessor().getDbUser());
        assertNotNull(record.getSessionLocator());
    }

    /**
     * Test that authCheck events are not skipped
     */
    @Test
    public void testAuthCheckNotSkipped() throws Exception {
        String authCheckLog = "{\"atype\":\"authCheck\",\"ts\":1779145197084," +
            "\"timestamp_utc\":\"2026-05-18 22:59:57.084\",\"remote_ip\":\"203.0.113.10:10862\"," +
            "\"users\":[{\"user\":\"readwrite\",\"db\":\"admin\"}]," +
            "\"param\":{\"command\":\"find\",\"ns\":\"admin.test\"," +
            "\"args\":{\"find\":\"test\"},\"result\":0}}";
        
        DocumentdbGuardiumFilter filter = new DocumentdbGuardiumFilter("test-id", null, context);
        Event e = new org.logstash.Event();
        TestMatchListener matchListener = new TestMatchListener();
        e.setField("message", authCheckLog);
        
        Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
        
        assertEquals(1, results.size(), "authCheck event should not be skipped");
        assertEquals(1, matchListener.getMatchCount(), "authCheck event should match");
    }

    /**
     * Test client port parsing from remote_ip field
     */
    @Test
    public void testClientPortParsing() throws Exception {
        Record record = parseRecordByIndex(1);
        
        assertNotNull(record);
        assertNotNull(record.getSessionLocator());
        // Verify port was parsed correctly from remote_ip
        assertTrue(record.getSessionLocator().getClientPort() > 0, 
            "Client port should be parsed from remote_ip");
    }

    /**
     * Test that users array is correctly parsed for app user name
     */
    @Test
    public void testUsersArrayParsing() throws Exception {
        Record record = parseRecordByIndex(1);
        
        assertNotNull(record);
        assertEquals("readwrite", record.getAccessor().getDbUser(), 
            "User should be extracted from users array");
    }

    /**
     * Test database name extraction from param.ns
     */
    @Test
    public void testDatabaseNameExtraction() throws Exception {
        Record record = parseRecordByIndex(1);
        
        assertNotNull(record);
        // dbName includes serverHostnamePrefix in Logstash version
        assertEquals("test-cluster:admin", record.getDbName(),
            "Database name should be extracted from param.ns and prefixed with serverHostnamePrefix");
    }

    /**
     * Test that Data construct is properly created
     */
    @Test
    public void testDataConstructCreation() throws Exception {
        Record record = parseRecordByIndex(1);
        
        assertNotNull(record);
        assertNotNull(record.getData(), "Data should not be null");
        assertNotNull(record.getData().getConstruct(), "Construct should not be null");
        assertNotNull(record.getData().getConstruct().getFullSql(), 
            "FullSql should not be null");
    }

    /**
     * Test that timestamp is correctly parsed
     */
    @Test
    public void testTimestampParsing() throws Exception {
        Record record = parseRecordByIndex(1);
        
        assertNotNull(record);
        assertNotNull(record.getTime(), "Time should not be null");
        assertTrue(record.getTime().getTimstamp() > 0, "Timestamp should be positive");
    }

    /**
     * Inner class for test match listener
     */
    private static class TestMatchListener implements FilterMatchListener {
        private int matchCount = 0;

        @Override
        public void filterMatched(Event event) {
            matchCount++;
        }

        public int getMatchCount() {
            return matchCount;
        }
    }
}

// Made with Bob
