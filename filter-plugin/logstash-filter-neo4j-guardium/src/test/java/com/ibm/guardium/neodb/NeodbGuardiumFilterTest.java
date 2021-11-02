//
// Copyright 2020-2021 IBM Inc. All rights reserved
// SPDX-License-Identifier: Apache2.0
//

package com.ibm.guardium.neodb;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;
import org.junit.Test;
import org.logstash.plugins.ContextImpl;

import com.ibm.guardium.universalconnector.commons.GuardConstants;

import co.elastic.logstash.api.Context;
import co.elastic.logstash.api.Event;
import co.elastic.logstash.api.FilterMatchListener;

public class NeodbGuardiumFilterTest {
	
	
    final static Context context = new ContextImpl(null, null);
    final static NeodbGuardiumFilter filter = new NeodbGuardiumFilter("test-id", null, context);
    

    /**
     * To feed Guardium universal connector, a "GuardRecord" fields must exist. 
     * 
     * Filter should add field "GuardRecord" to the Event, which Universal connector then inserts into Guardium.   
     */
    @Test
    public void testFieldGuardRecord_neodb() {
    	
    	String neoString = "2021-08-06 14:40:25.744+0000 INFO  275 ms: (planning: 1, waiting: 0) - 1792 B - 0 page hits, 0 page faults - bolt-session	bolt	neo4j-browser/v4.3.1		client/127.0.0.1:49321	server/127.0.0.1:11004>	neo4j - neo4j - CREATE (Dhawan:player{name: \"Shikar Dhawan\", YOB: 1985, POB: \"Delhi\"}) RETURN Dhawan - {} - runtime=slotted - {type: 'user-direct', app: 'neo4j-browser_v4.3.1'}";

        Context context = new ContextImpl(null, null);
        NeodbGuardiumFilter filter = new NeodbGuardiumFilter("test-id", null, context);

        Event e = ParserTest.getParsedEvent(neoString);
        TestMatchListener matchListener = new TestMatchListener();

        Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);

        Assert.assertEquals(1, results.size());
        Assert.assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
        Assert.assertEquals(1, matchListener.getMatchCount());
    }
    
    @Test
    public void testFieldSkippedRecord_neodb() {
    	
	    String skipString = "2021-08-06 14:40:24.484+0000 INFO  67 ms: (planning: 63, waiting: 0) - 0 B - 1 page hits, 0 page faults - bolt-session	bolt	neo4j-browser/v4.3.1		client/127.0.0.1:49321	server/127.0.0.1:11004>	neo4j - neo4j - EXPLAIN CREATE (Dhawan:player{name: \"Shikar Dhawan\", YOB: 1985, POB: \"Delhi\"}) RETURN Dhawan - {} - runtime=slotted - {type: 'user-action', app: 'neo4j-browser_v4.3.1'}";

        Context context = new ContextImpl(null, null);
        NeodbGuardiumFilter filter = new NeodbGuardiumFilter("test-id", null, context);

        Event e = ParserTest.getParsedEvent(skipString);
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

    public int getMatchCount() {
        return matchCount.get();
    }
}
