/*
Copyright IBM Corp. 2021, 2024 All rights reserved.
SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.guardium.bigtable;

import co.elastic.logstash.api.Configuration;
import co.elastic.logstash.api.Event;
import co.elastic.logstash.api.FilterMatchListener;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.ibm.guardium.universalconnector.commons.GuardConstants;
import org.junit.jupiter.api.Test;
import org.logstash.plugins.ConfigurationImpl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class BigTableGuardiumFilterTest {

    FilterMatchListener matchListener = new TestMatchListener();

    String id = "Big1";
    Configuration config = new ConfigurationImpl(
            Collections.singletonMap("Source", "Message"));
    BigTableGuardiumFilter bigTableGuardiumFilter = new BigTableGuardiumFilter(id, config, null);

    @Test
    void testBigTableGuardiumFilter() {
        String id = "Big1";
        assertEquals("Big1", id);
    }

    @Test
    void filterTest() {
        Event event = new org.logstash.Event();
        event.setField(ApplicationConstants.MESSAGE, ApplicationConstants.MESSAGE);
        event.setField(ApplicationConstants.CONNECTION_ID_KEY, "4");
        event.setField(ApplicationConstants.USERNAME_KEY, "testName");
        event.setField(ApplicationConstants.DATABASE_KEY, "db2");
        event.setField(ApplicationConstants.RETCODE_KEY, "001");
        event.setField(ApplicationConstants.HOSTNAME_KEY, "localhost");
        event.setField(ApplicationConstants.SERVERHOST_KEY, "LP-5CD1184J8J");
        event.setField(ApplicationConstants.OPERATION_KEY, "QUERY");
        event.setField("timestamp", "20220107 09:11:51");
        Collection<Event> actualResponse = bigTableGuardiumFilter.filter(Collections.singletonList(event),
                matchListener);
        assertNotNull(actualResponse);
        assertEquals("4", actualResponse.toArray(new Event[0])[0]
                .getField(ApplicationConstants.CONNECTION_ID_KEY));
        assertEquals("LP-5CD1184J8J", actualResponse.toArray(new Event[0])[0]
                .getField(ApplicationConstants.SERVERHOST_KEY));
        assertEquals("testName",
                actualResponse.toArray(new Event[0])[0].getField(ApplicationConstants.USERNAME_KEY));
        assertEquals("localhost",
                actualResponse.toArray(new Event[0])[0].getField(ApplicationConstants.HOSTNAME_KEY));
        assertEquals("db2",
                actualResponse.toArray(new Event[0])[0].getField(ApplicationConstants.DATABASE_KEY));
        assertEquals("001",
                actualResponse.toArray(new Event[0])[0].getField(ApplicationConstants.RETCODE_KEY));
    }

    @Test
    void filterTestNotGCP() {
        Event event = new org.logstash.Event();
        event.setField(ApplicationConstants.MESSAGE, "message");
        event.setField(ApplicationConstants.CONNECTION_ID_KEY, "4");

        Collection<Event> actualResponse = bigTableGuardiumFilter.filter(Collections.singletonList(event), matchListener);

        assertNotNull(actualResponse);
        Event filteredEvent = actualResponse.iterator().next();
        assertNull(filteredEvent.getField(ApplicationConstants.LOGSTASH_TAG_SKIP_NOT_GCP));
    }

    @Test
    void filterTestInvalidJSON() {
        Event event = new org.logstash.Event();
        event.setField(ApplicationConstants.MESSAGE, "message.bigtable.googleapis.com");

        Collection<Event> events = new ArrayList<>();
        events.add(event);

        Collection<Event> actualResponse = bigTableGuardiumFilter.filter(events, matchListener);
        assertTrue(actualResponse.isEmpty());
    }

    @Test
    void filterTestNullRecord() {
        String validJsonWithNullRecord = "{\"message\":\"bigtable.googleapis.com\"}";

        Event event = new org.logstash.Event();
        event.setField(ApplicationConstants.MESSAGE, validJsonWithNullRecord);

        Collection<Event> events = new ArrayList<>();
        events.add(event);

        Collection<Event> actualResponse = bigTableGuardiumFilter.filter(events, matchListener);
        assertTrue(actualResponse.isEmpty());
    }

    @Test
    void filterTestValidRecord() throws IOException {

        String payload = String.valueOf(readJsonFileAsJson("src/test/resources/createTable.json"));

        Event event = new org.logstash.Event();
        event.setField(ApplicationConstants.MESSAGE, payload);
        event.setField(ApplicationConstants.BIGTABLE_API, "bigtable.googleapis.com");

        Collection<Event> events = new ArrayList<>();
        events.add(event);

        Collection<Event> actualResponse = bigTableGuardiumFilter.filter(events, matchListener);
        assertNotNull(actualResponse);
        assertFalse(actualResponse.isEmpty());

        Event filteredEvent = actualResponse.iterator().next();
        assertNotNull(filteredEvent.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
    }

    @Test
    void filterTestNotSkippedEvent() {
        String jsonWithException = "{\"key\":}";

        Event event = new org.logstash.Event();
        event.setField(ApplicationConstants.MESSAGE, jsonWithException);
        event.setField(ApplicationConstants.BIGTABLE_API, "bigtable.googleapis.com");

        Collection<Event> events = new ArrayList<>();
        events.add(event);

        Collection<Event> actualResponse = bigTableGuardiumFilter.filter(events, matchListener);
        assertFalse(actualResponse.isEmpty());
    }

    @Test
    void filterTestValidRecordMultipleEvents() throws IOException {
        String payload = String.valueOf(readJsonFileAsJson("src/test/resources/createTable.json"));
        String payload2 = String.valueOf(readJsonFileAsJson("src/test/resources/writeRecord.json"));

        Event event1 = new org.logstash.Event();
        event1.setField(ApplicationConstants.MESSAGE, payload);
        event1.setField(ApplicationConstants.BIGTABLE_API, "bigtable.googleapis.com");

        Event event2 = new org.logstash.Event();
        event2.setField(ApplicationConstants.MESSAGE, payload2);
        event2.setField(ApplicationConstants.BIGTABLE_API, "bigtable.googleapis.com");

        Collection<Event> events = new ArrayList<>();
        events.add(event1);
        events.add(event2);

        Collection<Event> actualResponse = bigTableGuardiumFilter.filter(events, matchListener);
        assertNotNull(actualResponse);
        assertFalse(actualResponse.isEmpty());

        Event filteredEvent = actualResponse.iterator().next();
        assertNotNull(filteredEvent.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
    }

    @Test
    void filterTestGetTableEvent() throws IOException {
        String payload = String.valueOf(readJsonFileAsJson("src/test/resources/getTable.json"));

        Event event = new org.logstash.Event();
        event.setField(ApplicationConstants.MESSAGE, payload);
        event.setField(ApplicationConstants.BIGTABLE_API, "bigtable.googleapis.com");


        Collection<Event> events = new ArrayList<>();
        events.add(event);

        Collection<Event> actualResponse = bigTableGuardiumFilter.filter(events, matchListener);
        assertNotNull(actualResponse);
        assertFalse(actualResponse.isEmpty());

        Event filteredEvent = actualResponse.iterator().next();
        assertNotNull(filteredEvent.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));

        var data = filteredEvent.getData();
        var record = data.get("GuardRecord");

        assertNotNull(record);

    }

    @Test
    void filterTestValidandInvalidEvents() throws IOException {
        String payload = String.valueOf(readJsonFileAsJson("src/test/resources/createTable.json"));
        String payload2 = "{\"key\":}";

        Event event1 = new org.logstash.Event();
        event1.setField(ApplicationConstants.MESSAGE, payload);
        event1.setField(ApplicationConstants.BIGTABLE_API, "bigtable.googleapis.com");

        Event event2 = new org.logstash.Event();
        event2.setField(ApplicationConstants.MESSAGE, payload2);
        event2.setField(ApplicationConstants.BIGTABLE_API, "bigtable.googleapis.com");

        Collection<Event> events = new ArrayList<>();
        events.add(event1);
        events.add(event2);

        Collection<Event> actualResponse = bigTableGuardiumFilter.filter(events, matchListener);
        assertNotNull(actualResponse);
        assertFalse(actualResponse.isEmpty());

        Event filteredEvent = actualResponse.iterator().next();
        assertNotNull(filteredEvent.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
    }

    @Test
    void testNonBigtableLog() throws IOException {
        String payload = String.valueOf(readJsonFileAsJson("src/test/resources/notBigtable.json"));

        Event event = new org.logstash.Event();
        event.setField(ApplicationConstants.MESSAGE, payload);

        Collection<Event> events = new ArrayList<>();
        events.add(event);

        Collection<Event> actualResponse = bigTableGuardiumFilter.filter(events, matchListener);
        assertTrue(actualResponse.isEmpty());

    }

    private JsonObject readJsonFileAsJson(String fileName) throws IOException {
        return new Gson().fromJson(readJsonFile(fileName), JsonObject.class);
    }

    private String readJsonFile(String fileName) throws IOException {
        return new String(Files.readAllBytes(Paths.get(fileName)));
    }

    class TestMatchListener implements FilterMatchListener {

        private AtomicInteger matchCount = new AtomicInteger(0);

        @Override
        public void filterMatched(Event event) {
            matchCount.incrementAndGet();
        }

        public int getMatchCount() {
            return matchCount.get();
        }
    }



}