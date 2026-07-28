package com.ibm.guardium.documentdb;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Collection;
import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.logstash.plugins.ContextImpl;

import com.google.gson.Gson;
import com.ibm.guardium.universalconnector.commons.GuardConstants;
import com.ibm.guardium.universalconnector.commons.structures.Record;

import co.elastic.logstash.api.Context;
import co.elastic.logstash.api.Event;
import co.elastic.logstash.api.FilterMatchListener;

/** End-to-end tests for audit logs containing BSON regex literals. All message strings are synthetic. */
public class BsonRegexParserTest {

    private static final Context context = new ContextImpl(null, null);
    private static final Gson gson = new Gson();

    // ── helpers ───────────────────────────────────────────────────────────────

    private static final DocumentdbGuardiumFilter filter =
            new DocumentdbGuardiumFilter("test-id", null, context);

    private static class CapturingMatchListener implements FilterMatchListener {
        int count = 0;
        @Override public void filterMatched(Event e) { count++; }
    }

    private Record runFilter(String message) {
        Event e = new org.logstash.Event();
        e.setField("message", message);
        e.setField("serverHostnamePrefix", "test-cluster");
        e.setField("event_id",
                "0000000000000000000000000000000000000000000000000000000000000001");

        CapturingMatchListener listener = new CapturingMatchListener();
        Collection<Event> results = filter.filter(Collections.singletonList(e), listener);

        assertEquals(1, results.size(), "filter should return exactly one event");
        assertEquals(1, listener.count,  "event should be matched");
        Object raw = e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME);
        assertNotNull(raw, "GuardRecord field must be populated");
        return gson.fromJson(raw.toString(), Record.class);
    }

    // ── test cases ────────────────────────────────────────────────────────────

    /** find: bare regex as a field operator value. */
    @Test
    public void testFindWithNotRegex() {
        String message =
            "{\"atype\":\"authCheck\",\"ts\":1700000000000," +
            "\"timestamp_utc\":\"2023-11-14 22:13:20.000\"," +
            "\"remote_ip\":\"127.0.0.1:11111\"," +
            "\"users\":[{\"user\":\"testuser\",\"db\":\"testdb\"}]," +
            "\"param\":{\"command\":\"find\",\"ns\":\"testdb.col\"," +
            "\"args\":{\"find\":\"col\"," +
            "\"filter\":{\"a\":{\"b\":\"v\"}," +
            "\"c\":{\"d\":/^abc/}," +
            "\"e\":\"x\"}," +
            "\"skip\":0,\"startTransaction\":false}," +
            "\"result\":0}}";

        Record record = runFilter(message);

        assertNotNull(record.getData(),                "data must not be null");
        assertNotNull(record.getData().getConstruct(), "construct must not be null");
        assertNull(record.getException(),              "no exception expected");
        assertEquals("find", record.getData().getConstruct().sentences.get(0).getVerb(),
                "sentence verb must equal param.command");
    }

    /** find: array operator and regex as siblings on the same field. */
    @Test
    public void testFindWithNinAndNotRegex() {
        String message =
            "{\"atype\":\"authCheck\",\"ts\":1700000001000," +
            "\"timestamp_utc\":\"2023-11-14 22:13:21.000\"," +
            "\"remote_ip\":\"127.0.0.1:22222\"," +
            "\"users\":[{\"user\":\"testuser\",\"db\":\"testdb\"}]," +
            "\"param\":{\"command\":\"find\",\"ns\":\"testdb.col\"," +
            "\"args\":{\"find\":\"col\"," +
            "\"filter\":{\"a\":{\"b\":\"v\"}," +
            "\"c\":{\"d\":[\"p\",\"q\"],\"e\":/^abc/}," +
            "\"f\":\"x\"}," +
            "\"skip\":0,\"startTransaction\":false}," +
            "\"result\":0}}";

        Record record = runFilter(message);

        assertNotNull(record.getData());
        assertNotNull(record.getData().getConstruct());
        assertNull(record.getException(), "no exception expected");
    }

    /** find: nested array operators and regex combined in one filter. */
    @Test
    public void testFindWithAndNinNotRegex() {
        String message =
            "{\"atype\":\"authCheck\",\"ts\":1700000002000," +
            "\"timestamp_utc\":\"2023-11-14 22:13:22.000\"," +
            "\"remote_ip\":\"127.0.0.1:33333\"," +
            "\"users\":[{\"user\":\"testuser\",\"db\":\"testdb\"}]," +
            "\"param\":{\"command\":\"find\",\"ns\":\"testdb.col\"," +
            "\"args\":{\"find\":\"col\"," +
            "\"filter\":{\"a\":[{\"b\":{\"c\":[\"p\"]}},{\"d\":{\"e\":[\"q\"]}}]," +
            "\"f\":{\"g\":[\"r\",\"s\",\"t\"],\"h\":/^abc/}," +
            "\"i\":\"x\"}," +
            "\"skip\":0,\"startTransaction\":false}," +
            "\"result\":0}}";

        Record record = runFilter(message);

        assertNotNull(record.getData());
        assertNotNull(record.getData().getConstruct());
        assertNull(record.getException(), "no exception expected");
    }

    /** aggregate: regex with backslash escape sequence in a pipeline stage. */
    @Test
    public void testAggregateWithBackslashRegex() {
        String message =
            "{\"atype\":\"authCheck\",\"ts\":1700000003000," +
            "\"timestamp_utc\":\"2023-11-14 22:13:23.000\"," +
            "\"remote_ip\":\"127.0.0.1:44444\"," +
            "\"users\":[{\"user\":\"testuser\",\"db\":\"testdb\"}]," +
            "\"param\":{\"command\":\"aggregate\",\"ns\":\"testdb.col\"," +
            "\"args\":{\"aggregate\":\"col\",\"allowDiskUse\":false," +
            "\"cursor\":{\"batchSize\":256},\"explain\":false," +
            "\"pipeline\":[" +
            "{\"$match\":{\"a\":/.*\\wsome term.*/i,\"b\":{\"c\":[\"x\",\"y\"]}}}," +
            "{\"$sort\":{\"a\":1}},{\"$skip\":0},{\"$limit\":100}]," +
            "\"startTransaction\":false},\"result\":0}}";

        Record record = runFilter(message);

        assertNotNull(record.getData());
        assertNotNull(record.getData().getConstruct());
        assertNull(record.getException(), "no exception expected");
    }

    /** fullSql must preserve a backslash escape sequence, not double-escape it (unescapeJava regression). */
    @Test
    public void testFullSqlPreservesBackslashEscape() {
        String message =
            "{\"atype\":\"authCheck\",\"ts\":1700000004000," +
            "\"timestamp_utc\":\"2023-11-14 22:13:24.000\"," +
            "\"remote_ip\":\"127.0.0.1:55555\"," +
            "\"users\":[{\"user\":\"testuser\",\"db\":\"testdb\"}]," +
            "\"param\":{\"command\":\"aggregate\",\"ns\":\"testdb.col\"," +
            "\"args\":{\"aggregate\":\"col\",\"allowDiskUse\":false," +
            "\"cursor\":{\"batchSize\":256},\"explain\":false," +
            "\"pipeline\":[" +
            "{\"$match\":{\"a\":/.*\\wsome term.*/i,\"b\":{\"c\":[\"x\"]}}}]," +
            "\"startTransaction\":false},\"result\":0}}";

        Record record = runFilter(message);

        String fullSql = record.getData().getConstruct().getFullSql();
        assertNotNull(fullSql, "fullSql must not be null");
        assertTrue(fullSql.contains("\\w"),    "fullSql must contain \\w");
        assertFalse(fullSql.contains("\\\\w"), "fullSql must NOT contain \\\\w");
    }
}
