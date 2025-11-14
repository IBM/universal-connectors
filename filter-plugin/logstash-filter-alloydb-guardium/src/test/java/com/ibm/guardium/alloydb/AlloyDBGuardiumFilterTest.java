package com.ibm.guardium.alloydb;

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

class AlloyDBGuardiumFilterTest {

  FilterMatchListener matchListener = new TestMatchListener();

  String id = "1";
  Configuration config = new ConfigurationImpl(Collections.singletonMap("source", ""));
  Context context = new ContextImpl(null, null);
  AlloyDBGuardiumFilter filter = new AlloyDBGuardiumFilter(id, config, context);

  @Test
  void test() {
    String payload =
        "{\n"
            + "    \"textPayload\": \"2025-05-05 19:10:44.336 UTC [70374]: [1-1] db=alloydb_postgresql,user=postgres LOG: [postgres.c:1187] statement: SELECT * FROM customers\",\n"
            + "    \"insertId\": \"s=21bfbbc9ae684df79c81633b63e1fb33;i=18d4f1;b=488acbc40da24f1c85d08b22a1969f2b;m=3cf02fe71;t=6346841bbd621;x=1f898f7d8bd2cd3-0-0@a1\",\n"
            + "    \"resource\": {\n"
            + "        \"type\": \"alloydb.googleapis.com/Instance\",\n"
            + "        \"labels\": {\n"
            + "            \"location\": \"us-central1\",\n"
            + "            \"resource_container\": \"projects/485533885456\",\n"
            + "            \"cluster_id\": \"alloydb-postgresql\",\n"
            + "            \"instance_id\": \"alloydb-postgresql-instance\"\n"
            + "        }\n"
            + "    },\n"
            + "    \"timestamp\": \"2025-05-05T19:10:44.336161Z\",\n"
            + "    \"severity\": \"INFO\",\n"
            + "    \"labels\": {\n"
            + "        \"DATABASE_VERSION\": \"POSTGRES_15\",\n"
            + "        \"CONSUMER_PROJECT_NUMBER\": \"485533885456\",\n"
            + "        \"CONSUMER_PROJECT\": \"project-name\",\n"
            + "        \"NODE_ID\": \"kvqd\"\n"
            + "    },\n"
            + "    \"logName\": \"projects/project-name/logs/alloydb.googleapis.com%2Fpostgres.log\",\n"
            + "    \"receiveTimestamp\": \"2025-05-05T19:11:05.292741654Z\"\n"
            + "}";

    Event event = new org.logstash.Event();
    event.setField("message", payload);
    Collection<Event> actualResponse =
        filter.filter(Collections.singletonList(event), matchListener);

    assertNotNull(actualResponse.toArray(new Event[0])[0].getField("GuardRecord"));
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
