//
// Copyright 2023 IBM All Rights Reserved.
// SPDX-License-Identifier: Apache2.0
//
package com.ibm.guardium.progress;

import co.elastic.logstash.api.Context;
import co.elastic.logstash.api.Event;
import co.elastic.logstash.api.FilterMatchListener;
import com.ibm.guardium.univer***REMOVED***lconnector.commons.GuardConstants;
import org.junit.Assert;
import org.junit.Test;
import org.logstash.plugins.ContextImpl;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;


public class ProgressGuardiumPluginFilterTest {

    final static Context context = new ContextImpl(null, null);
    final static ProgressGuardiumPluginFilter filter = new ProgressGuardiumPluginFilter("test-id", null, context);

    /**
     * To feed Guardium univer***REMOVED***l connector, a "GuardRecord" fields must exist.
     * <p>
     * Filter should add field "GuardRecord" to the Event, which Univer***REMOVED***l connector
     * then inserts into Guardium.
     */

    @Test
    public void testGuardRecord() {

        TestMatchListener matchListener = new TestMatchListener();

        Event e = new org.logstash.Event();

        e.setField(Constants.CLIENT_SESSION_ID, "jt3ZR8SvMLygFMida3OP4w");
        e.setField(Constants.EVENT_NAME, "_sys.index.update");
        e.setField(Constants.EVENT_CONTEXT, "PUB._index14189_Default");
        e.setField(Constants.CLIENT_IP, "0.0.0.0");
        e.setField(Constants.TIMESTAMP, "2023-02-15T23:16:57.956Z");
        e.setField(Constants.USER_ID, "Apurva");
        e.setField(Constants.DATABASE_NAME, "Demo");
        e.setField(Constants.SERVER_PORT, -1);
        e.setField(Constants.SERVER_HOST, "DB Machine");
        e.setField(Constants.CLIENT_HOST, "NA");
        e.setField(Constants.SOURCE_PROGRAM, "NA");
        e.setField(Constants.CLIENT_IP, "0.0.0.0");
        e.setField(Constants.CLIENT_PORT, "-1");
        e.setField(Constants.SERVER_PORT, "-1");
        e.setField(Constants.SERVER_IP, "0.0.0.0");

        Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);

        Assert.assertEquals(1, results.size());
        Assert.assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
        Assert.assertEquals(1, matchListener.getMatchCount());
    }

    @Test
    public void testExceptionRecord() {

        TestMatchListener matchListener = new TestMatchListener();

        Event e = new org.logstash.Event();

        e.setField(Constants.CLIENT_SESSION_ID, "");
        e.setField(Constants.EVENT_NAME, "_sql.user.login.fail");
        e.setField(Constants.EVENT_CONTEXT, "SQL USER LOGIN FAILED");
        e.setField(Constants.TIMESTAMP, "2023-02-17T11:16:47.956Z");
        e.setField(Constants.USER_ID, "");
        e.setField(Constants.DATABASE_NAME, "Demo");
        e.setField(Constants.CLIENT_HOST, "NA");
        e.setField(Constants.SOURCE_PROGRAM, "NA");
        e.setField(Constants.CLIENT_IP, "0.0.0.0");
        e.setField(Constants.CLIENT_PORT, "-1");
        e.setField(Constants.SERVER_PORT, "-1");
        e.setField(Constants.SERVER_HOST, "DB Machine");
        e.setField(Constants.SERVER_IP, "0.0.0.0");

        Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);

        Assert.assertEquals(1, results.size());
        Assert.assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
        Assert.assertEquals(1, matchListener.getMatchCount());


    }

    @Test
    public void testExceptionRecord1() {

        TestMatchListener matchListener = new TestMatchListener();

        Event e = new org.logstash.Event();

        e.setField(Constants.CLIENT_SESSION_ID, "");
        e.setField(Constants.EVENT_NAME, "_sql.db.connect");
        e.setField(Constants.EVENT_CONTEXT, "SQL DATABASE CONNECTED");
        e.setField(Constants.TIMESTAMP, "2023-03-01T23:16:47.956Z");
        e.setField(Constants.USER_ID, "");
        e.setField(Constants.DATABASE_NAME, "Demo");
        e.setField(Constants.CLIENT_HOST, "NA");
        e.setField(Constants.SOURCE_PROGRAM, "NA");
        e.setField(Constants.CLIENT_IP, "0.0.0.0");
        e.setField(Constants.CLIENT_PORT, "-1");
        e.setField(Constants.SERVER_PORT, "-1");
        e.setField(Constants.SERVER_HOST, "DB Machine");
        e.setField(Constants.SERVER_IP, "0.0.0.0");

        Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);

        Assert.assertEquals(1, results.size());
        Assert.assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
        Assert.assertEquals(1, matchListener.getMatchCount());;


    }

    class TestMatchListener implements FilterMatchListener {

        private final AtomicInteger matchCount = new AtomicInteger(0);

        @Override
        public void filterMatched(Event event) {
            matchCount.incrementAndGet();
        }

        public int getMatchCount() {
            return matchCount.get();
        }
    }
}
