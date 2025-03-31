package com.ibm.guardium.trino;

import co.elastic.logstash.api.Event;
import co.elastic.logstash.api.FilterMatchListener;
import co.elastic.logstash.api.Configuration;
import co.elastic.logstash.api.Context;
import org.logstash.plugins.ConfigurationImpl;
import org.logstash.plugins.ContextImpl;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TrinoGuardiumFilterTest {

    FilterMatchListener matchListener = new TestMatchListener();

    String id = "1";
    Configuration config = new ConfigurationImpl(Collections.singletonMap("source", ""));
    Context context = new ContextImpl(null, null);
    TrinoGuardiumFilter filter = new TrinoGuardiumFilter(id, config, context);

    @Test
    void test() {
        String payload = "logstash trino-logstash[-]: 2025-03-10T15:42:10.438Z 172.29.0.7 LEEF:1.0|Trino|Trino Server|466|FINISHED|CatalogName=hive\tDatabaseName=test_db\tTableName=test_table\tTime=2025-03-10T15:42:10.438Z\tClientIP=127.0.0.1\tServerIP=172.29.0.7\tServerPort=8080\tUser=trino\tSQLCommand=\\\"SELECT * FROM hive.test_db.test_table LIMIT 10\\\"\tQueryId=20250310_154210_00000_5858r\tError=null";
        Event event = new org.logstash.Event();
        event.setField("message", payload);
        Collection<Event> actualResponse = filter.filter(Collections.singletonList(event), matchListener);

        assertNotNull(actualResponse.toArray(new Event[0])[0].getField("GuardRecord"));
        assertTrue(((TestMatchListener) matchListener).getMatchCount() > 0);
    }

    class TestMatchListener implements FilterMatchListener {
        private AtomicInteger matchCount = new AtomicInteger(0);

        public int getMatchCount() {
            return matchCount.get();
        }

        @Override
        public void filterMatched(co.elastic.logstash.api.Event arg0) {
            matchCount.incrementAndGet();

        }
    }
}
