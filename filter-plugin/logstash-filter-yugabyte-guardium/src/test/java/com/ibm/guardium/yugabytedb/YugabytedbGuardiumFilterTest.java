//
// Copyright 2021-2022 IBM Inc. All rights reserved
// SPDX-License-Identifier: Apache2.0
//

package com.ibm.guardium.yugabytedb;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import com.ibm.guardium.yugabytedb.Constants;
import com.ibm.guardium.yugabytedb.YugabytedbGuardiumFilter;
import org.junit.Assert;
import org.junit.Test;
import org.logstash.plugins.ContextImpl;

import com.ibm.guardium.universalconnector.commons.GuardConstants;

import co.elastic.logstash.api.Context;
import co.elastic.logstash.api.Event;
import co.elastic.logstash.api.FilterMatchListener;

public class YugabytedbGuardiumFilterTest {
	
	
    final static Context context = new ContextImpl(null, null);
    final static YugabytedbGuardiumFilter filter = new YugabytedbGuardiumFilter("test-id", null, context);
    

    /**
     * To feed Guardium universal connector, a "GuardRecord" field must exist.
     * 
     * Filter should add field "GuardRecord" to the Event, which Universal connector then inserts into Guardium.   
     */
    @Test
    public void testFieldGuardRecord() {

        Context context = new ContextImpl(null, null);
        YugabytedbGuardiumFilter filter = new YugabytedbGuardiumFilter("test-id", null, context);

        Event e = ParserTest.getSuccessEvent();
        TestMatchListener matchListener = new TestMatchListener();

        Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);

        Assert.assertEquals(1, results.size());
        Assert.assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
        Assert.assertEquals(1, matchListener.getMatchCount());
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
