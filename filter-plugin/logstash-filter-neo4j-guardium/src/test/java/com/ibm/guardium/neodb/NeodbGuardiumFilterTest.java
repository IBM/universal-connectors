//
// Copyright 2020-2021 IBM Inc. All rights reserved
// SPDX-License-Identifier: Apache2.0
//

package com.ibm.guardium.neodb;

import co.elastic.logstash.api.Context;
import co.elastic.logstash.api.Event;
import co.elastic.logstash.api.FilterMatchListener;
import org.junit.Assert;
import org.junit.Test;
import org.logstash.plugins.ContextImpl;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class NeodbGuardiumFilterTest {
	
	
    final static Context context = new ContextImpl(null, null);
    

    /**
     * To feed Guardium univer***REMOVED***l connector, a "GuardRecord" fields must exist. 
     * 
     * Filter should add field "GuardRecord" to the Event, which Univer***REMOVED***l connector then inserts into Guardium.   
     */
    @Test
    public void testFieldGuardRecord_neodb() {
    	
    	String neoString = "2021-08-06 14:40:25.744+0000 INFO  275 ms: (planning: 1, waiting: 0) - 1792 B - 0 page hits, 0 page faults - bolt-session	bolt	neo4j-browser/v4.3.1		client/127.0.0.1:49321	server/127.0.0.1:11004>	neo4j - neo4j - CREATE (Dhawan:player{name: \"Shikar Dhawan\", YOB: 1985, POB: \"Delhi\"}) RETURN Dhawan - {} - runtime=slotted - {type: 'user-direct', app: 'neo4j-browser_v4.3.1'}";
        String neoString_grokOutput = "{\n" +
                "    \"ts\": \"2021-08-06 14:40:25.744+0000\",\n" +
                "    \"log_level\": \"INFO\",\n" +
                "    \"metadata1\": \" 275 ms: (planning: 1, waiting: 0) - 1792 B - 0 page hits, 0 page faults - bolt-session\",\n" +
                "    \"protocol\": \"bolt\",\n" +
                "    \"driverVersion\": \"neo4j-browser/v4.3.1\",\n" +
                "    \"client_ip\": \"client/127.0.0.1:49321\",\n" +
                "    \"server_ip\": \"server/127.0.0.1:11004\",\n" +
                "    \"dbname\": \"neo4j \",\n" +
                "    \"dbuser\": \"neo4j \",\n" +
                "    \"queryStatement\": \"CREATE (Dhawan:player{name: \\\\\\\"Shikar Dhawan\\\\\\\", YOB: 1985, POB: \\\\\\\"Delhi\\\\\\\"}) RETURN Dhawan - {} - runtime=slotted - {type: 'user-direct', app: 'neo4j-browser_v4.3.1'}\\\";\"\n" +
                "  }";
        Context context = new ContextImpl(null, null);
        NeodbGuardiumFilter filter = new NeodbGuardiumFilter("test-id", null, context);

        Event e = ParserTest.getParsedEvent(neoString_grokOutput, neoString);
        TestMatchListener matchListener = new TestMatchListener();

        Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);

        Assert.assertEquals(1, results.size());
    }
    
    @Test
    public void testFieldSkippedRecord_neodb() {
    	
	    String skipString = "2021-08-06 14:40:24.484+0000 INFO  67 ms: (planning: 63, waiting: 0) - 0 B - 1 page hits, 0 page faults - bolt-session	bolt	neo4j-browser/v4.3.1		client/127.0.0.1:49321	server/127.0.0.1:11004>	neo4j - neo4j - EXPLAIN CREATE (Dhawan:player{name: \"Shikar Dhawan\", YOB: 1985, POB: \"Delhi\"}) RETURN Dhawan - {} - runtime=slotted - {type: 'user-action', app: 'neo4j-browser_v4.3.1'}";
        String skipString_grokOutput = "{\n" +
                "    \"ts\": \"2021-08-06 14:40:24.484+0000\",\n" +
                "    \"log_level\": \"INFO\",\n" +
                "    \"metadata1\": \" 67 ms: (planning: 63, waiting: 0) - 0 B - 1 page hits, 0 page faults - bolt-session\",\n" +
                "    \"protocol\": \"bolt\",\n" +
                "    \"driverVersion\": \"neo4j-browser/v4.3.1\",\n" +
                "    \"client_ip\": \"client/127.0.0.1:49321\",\n" +
                "    \"server_ip\": \"server/127.0.0.1:11004\",\n" +
                "    \"dbname\": \"neo4j \",\n" +
                "    \"dbuser\": \"neo4j \",\n" +
                "    \"queryStatement\": \"EXPLAIN CREATE (Dhawan:player{name: \\\\\\\"Shikar Dhawan\\\\\\\", YOB: 1985, POB: \\\\\\\"Delhi\\\\\\\"}) RETURN Dhawan - {} - runtime=slotted - {type: 'user-action', app: 'neo4j-browser_v4.3.1'}\\\";\"\n" +
                "  }";
        Context context = new ContextImpl(null, null);
        NeodbGuardiumFilter filter = new NeodbGuardiumFilter("test-id", null, context);

        Event e = ParserTest.getParsedEvent(skipString_grokOutput, skipString);
        TestMatchListener matchListener = new TestMatchListener();

        Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);

        Assert.assertEquals(1, results.size());
        Set<String> tags = new HashSet<>((ArrayList)e.getField("tags"));
        Assert.assertEquals(1, tags.size());
        Assert.assertTrue(tags.contains(Constants.LOGSTASH_TAG_SKIP_NOT_NEO));
    }
}

class TestMatchListener implements FilterMatchListener {

    private AtomicInteger matchCount = new AtomicInteger(0);

    @Override
    public void filterMatched(Event event) {
        matchCount.incrementAndGet();
    }
}
