package com.ibm.guardium.mongodb;

import co.elastic.logstash.api.Event;
import co.elastic.logstash.api.FilterMatchListener;

import java.util.concurrent.atomic.AtomicInteger;

public class TestMatchListener implements FilterMatchListener {

    private AtomicInteger matchCount = new AtomicInteger(0);

    @Override
    public void filterMatched(Event event) {
        matchCount.incrementAndGet();
    }

    public int getMatchCount() {
        return matchCount.get();
    }
}
