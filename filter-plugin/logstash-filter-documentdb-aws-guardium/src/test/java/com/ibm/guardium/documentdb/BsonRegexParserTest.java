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

    /** find with bare /regex/ as $not value. */
    @Test
    public void testFindWithNotRegex() {
        String message =
            "{\"atype\":\"authCheck\",\"ts\":1700000000000," +
            "\"timestamp_utc\":\"2023-11-14 22:13:20.000\"," +
            "\"remote_ip\":\"127.0.0.1:11111\"," +
            "\"users\":[{\"user\":\"testuser\",\"db\":\"testdb\"}]," +
            "\"param\":{\"command\":\"find\",\"ns\":\"testdb.items\"," +
            "\"args\":{\"find\":\"items\"," +
            "\"filter\":{\"STATUS\":{\"$ne\":\"N\"}," +
            "\"CODE\":{\"$not\":/^XYZ/}," +
            "\"LANG\":\"en\"}," +
            "\"skip\":0,\"startTransaction\":false}," +
            "\"result\":0}}";

        Record record = runFilter(message);

        assertNotNull(record.getData(),                "data must not be null");
        assertNotNull(record.getData().getConstruct(), "construct must not be null");
        assertNull(record.getException(),              "no exception expected");
        assertEquals("find", record.getData().getConstruct().sentences.get(0).getVerb(),
                "sentence verb must equal param.command");
    }

    /** find with $nin and $not:/regex/ as sibling operators on the same field. */
    @Test
    public void testFindWithNinAndNotRegex() {
        String message =
            "{\"atype\":\"authCheck\",\"ts\":1700000001000," +
            "\"timestamp_utc\":\"2023-11-14 22:13:21.000\"," +
            "\"remote_ip\":\"127.0.0.1:22222\"," +
            "\"users\":[{\"user\":\"testuser\",\"db\":\"testdb\"}]," +
            "\"param\":{\"command\":\"find\",\"ns\":\"testdb.items\"," +
            "\"args\":{\"find\":\"items\"," +
            "\"filter\":{\"STATUS\":{\"$ne\":\"N\"}," +
            "\"CODE\":{\"$nin\":[\"AA\",\"BB\"],\"$not\":/^XYZ/}," +
            "\"LANG\":\"en\"}," +
            "\"skip\":0,\"startTransaction\":false}," +
            "\"result\":0}}";

        Record record = runFilter(message);

        assertNotNull(record.getData());
        assertNotNull(record.getData().getConstruct());
        assertNull(record.getException(),
                "no exception expected — $nin + $not with regex literal must parse");
    }

    /** find with $and, multi-value $nin, and $not:/regex/ combined. */
    @Test
    public void testFindWithAndNinNotRegex() {
        String message =
            "{\"atype\":\"authCheck\",\"ts\":1700000002000," +
            "\"timestamp_utc\":\"2023-11-14 22:13:22.000\"," +
            "\"remote_ip\":\"127.0.0.1:33333\"," +
            "\"users\":[{\"user\":\"testuser\",\"db\":\"testdb\"}]," +
            "\"param\":{\"command\":\"find\",\"ns\":\"testdb.items\"," +
            "\"args\":{\"find\":\"items\"," +
            "\"filter\":{\"$and\":[{\"TYPE\":{\"$nin\":[\"T1\"]}},{\"STATUS\":{\"$nin\":[\"PIPE\"]}}]," +
            "\"CODE\":{\"$nin\":[\"AA\",\"BB\",\"CC\"],\"$not\":/^XYZ/}," +
            "\"LANG\":\"en\"}," +
            "\"skip\":0,\"startTransaction\":false}," +
            "\"result\":0}}";

        Record record = runFilter(message);

        assertNotNull(record.getData());
        assertNotNull(record.getData().getConstruct());
        assertNull(record.getException(),
                "no exception expected — $and + long $nin + $not:/regex/ must all parse");
    }

    /** aggregate $match with a quotemeta regex: /.*\Q...\E.*\/i */
    @Test
    public void testAggregateWithQuotemetaRegex() {
        String message =
            "{\"atype\":\"authCheck\",\"ts\":1700000003000," +
            "\"timestamp_utc\":\"2023-11-14 22:13:23.000\"," +
            "\"remote_ip\":\"127.0.0.1:44444\"," +
            "\"users\":[{\"user\":\"testuser\",\"db\":\"testdb\"}]," +
            "\"param\":{\"command\":\"aggregate\",\"ns\":\"testdb.items\"," +
            "\"args\":{\"aggregate\":\"items\",\"allowDiskUse\":false," +
            "\"cursor\":{\"batchSize\":256},\"explain\":false," +
            "\"pipeline\":[" +
            "{\"$match\":{\"NAME\":/.*\\Qsome term\\E.*/i,\"STATUS\":{\"$in\":[\"A\",\"C\"]}}}," +
            "{\"$sort\":{\"NAME\":1}},{\"$skip\":0},{\"$limit\":100}]," +
            "\"startTransaction\":false},\"result\":0}}";

        Record record = runFilter(message);

        assertNotNull(record.getData());
        assertNotNull(record.getData().getConstruct());
        assertNull(record.getException(),
                "no exception expected — aggregate $match with quotemeta regex must parse");
    }

    /** fullSql must contain a single \Q, not double-escaped \\Q (unescapeJava regression). */
    @Test
    public void testFullSqlContainsSingleBackslashQ() {
        String message =
            "{\"atype\":\"authCheck\",\"ts\":1700000004000," +
            "\"timestamp_utc\":\"2023-11-14 22:13:24.000\"," +
            "\"remote_ip\":\"127.0.0.1:55555\"," +
            "\"users\":[{\"user\":\"testuser\",\"db\":\"testdb\"}]," +
            "\"param\":{\"command\":\"aggregate\",\"ns\":\"testdb.items\"," +
            "\"args\":{\"aggregate\":\"items\",\"allowDiskUse\":false," +
            "\"cursor\":{\"batchSize\":256},\"explain\":false," +
            "\"pipeline\":[" +
            "{\"$match\":{\"NAME\":/.*\\Qsome term\\E.*/i,\"STATUS\":{\"$in\":[\"A\"]}}}]," +
            "\"startTransaction\":false},\"result\":0}}";

        Record record = runFilter(message);

        String fullSql = record.getData().getConstruct().getFullSql();
        assertNotNull(fullSql, "fullSql must not be null");
        assertTrue(fullSql.contains("\\Q"),   "fullSql must contain \\Q");
        assertFalse(fullSql.contains("\\\\Q"), "fullSql must NOT contain \\\\Q");
    }
}
