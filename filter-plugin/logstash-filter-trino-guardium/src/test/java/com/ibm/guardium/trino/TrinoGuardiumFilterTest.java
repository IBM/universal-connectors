package com.ibm.guardium.trino;

import co.elastic.logstash.api.Event;
import co.elastic.logstash.api.FilterMatchListener;
import co.elastic.logstash.api.Context;
import com.google.gson.Gson;
import com.ibm.guardium.universalconnector.commons.GuardConstants;
import org.logstash.plugins.ContextImpl;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static com.ibm.guardium.trino.Constants.*;
import static org.junit.jupiter.api.Assertions.*;

class TrinoGuardiumFilterTest {
  String payload =
      "{\"createTime\":\"2025-06-05T19:50:09.402Z\",\"context\":{\"user\":\"trino\",\"originalUser\":\"trino\",\"principal\":\"trino\",\"enabledRoles\":[],\"groups\":[],\"remoteClientAddress\":\"127.0.0.1\",\"userAgent\":\"trino-cli\",\"clientTags\":[],\"clientCapabilities\":[\"PATH\",\"PARAMETRIC_DATETIME\",\"SESSION_AUTHORIZATION\"],\"source\":\"trino-cli\",\"timezone\":\"UTC\",\"resourceGroupId\":[\"global\"],\"sessionProperties\":{},\"resourceEstimates\":{},\"serverAddress\":\"172.19.0.2\",\"serverVersion\":\"466\",\"environment\":\"production\",\"queryType\":\"SELECT\",\"retryPolicy\":\"NONE\"},\"metadata\":{\"queryId\":\"20250605_195009_00010_34b3y\",\"transactionId\":\"b69e0f30-de9a-4dd3-b05f-f75566f1d6d1\",\"query\":\"SELECT * FROM hive.test_db.test_table LIMIT 10\",\"queryState\":\"QUEUED\",\"tables\":[],\"routines\":[],\"uri\":\"http://172.19.0.2:8080/v1/query/20250605_195009_00010_34b3y\"}}";
  TrinoGuardiumFilter filter = getGuardiumFilterConnector(payload);

  @Test
  public void TestNoMessage() {
    Event e = new org.logstash.Event();
    TrinoGuardiumFilterTest.TestMatchListener matchListener =
        new TrinoGuardiumFilterTest.TestMatchListener();
    Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
    assertNotNull(results);
  }

  @Test
  public void TestNullPointerException() {
    Event e = new org.logstash.Event();
    e.setField(MESSAGE, payload);
    e.setField(QueryId, "20250605_195009_00010_34b3y");
    TrinoGuardiumFilterTest.TestMatchListener matchListener =
        new TrinoGuardiumFilterTest.TestMatchListener();
    assertThrows(
        NullPointerException.class,
        () -> {
          Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
        });
  }

  @Test
  public void TestQueuedMessage() {
    Event e = new org.logstash.Event();
    e.setField(QueryState, "QUEUED");
    TrinoGuardiumFilterTest.TestMatchListener matchListener =
        new TrinoGuardiumFilterTest.TestMatchListener();
    Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
    assertNotNull(results);
  }

  @Test
  public void TestAudit() {
    Map<String, Object> payload1 = new Gson().fromJson(payload, Map.class);

    Event e = new org.logstash.Event();
    TrinoGuardiumFilterTest.TestMatchListener matchListener =
        new TrinoGuardiumFilterTest.TestMatchListener();
    e.setField(MESSAGE, payload1);
    e.setField(QueryState, "FINISHED");
    e.setField(QueryId, "20250605_195009_00010_34b3y");

    Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
    assertEquals(1, results.size());
    assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
    assertEquals(1, matchListener.getMatchCount());
    assertNotNull(results.iterator().next().getField("GuardRecord"));
  }

  @Test
  void testFilter() {
    TrinoGuardiumFilter filter = getGuardiumFilterConnector("");
    Event e = new org.logstash.Event();
    Map<String, Object> payload1 = new Gson().fromJson(payload, Map.class);
    TrinoGuardiumFilterTest.TestMatchListener matchListener =
        new TrinoGuardiumFilterTest.TestMatchListener();
    e.setField(MESSAGE, payload1);
    e.setField(QueryState, "FINISHED");
    e.setField(Time, "2025-04-24T15:09:29.097Z");
    e.setField(ClientIP, "127.0.0.1");
    e.setField(ServerIP, "172.19.0.6");
    e.setField(DBUser, "trino");
    e.setField(SQLCommand, "SELECT * FROM hive.test_db.test_table LIMIT 10");
    e.setField(QueryId, "20250424_150929_00004_cfqae");
    Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);

    assertNotNull(results.iterator().next().getField("GuardRecord"));
    assertTrue((matchListener).getMatchCount() > 0);
  }

  @Test
  void testGetId() {
    String expectedId = "testId";
    TrinoGuardiumFilter filter = new TrinoGuardiumFilter(expectedId, null, null);
    String actualId = filter.getId();
    assertEquals(expectedId, actualId);
  }

  @Test
  void testFilterStringMatch() {
    String filterValue = "150929";
    String payload =
        "{\"filterValue\":\"150929\",\"DatabaseName\":\"db_name\",\"createTime\":\"2025-06-05T19:50:09.402Z\",\"context\":{\"user\":\"trino\",\"originalUser\":\"trino\",\"principal\":\"trino\",\"enabledRoles\":[],\"groups\":[],\"remoteClientAddress\":\"127.0.0.1\",\"userAgent\":\"trino-cli\",\"clientTags\":[],\"clientCapabilities\":[\"PATH\",\"PARAMETRIC_DATETIME\",\"SESSION_AUTHORIZATION\"],\"source\":\"trino-cli\",\"timezone\":\"UTC\",\"resourceGroupId\":[\"global\"],\"sessionProperties\":{},\"resourceEstimates\":{},\"serverAddress\":\"172.19.0.2\",\"serverVersion\":\"466\",\"environment\":\"production\",\"queryType\":\"SELECT\",\"retryPolicy\":\"NONE\"},\"metadata\":{\"queryId\":\"20250605_195009_00010_34b3y\",\"transactionId\":\"b69e0f30-de9a-4dd3-b05f-f75566f1d6d1\",\"query\":\"SELECT * FROM hive.test_db.test_table LIMIT 10\",\"queryState\":\"QUEUED\",\"tables\":[],\"routines\":[],\"uri\":\"http://172.19.0.2:8080/v1/query/20250605_195009_00010_34b3y\"}}";
    Map<String, Object> payload1 = new Gson().fromJson(payload, Map.class);

    TrinoGuardiumFilter filterWithKeyword =
        new TrinoGuardiumFilter("test-id", filterValue, null, new ContextImpl(null, null));

    Event e = new org.logstash.Event();
    TrinoGuardiumFilterTest.TestMatchListener matchListener =
        new TrinoGuardiumFilterTest.TestMatchListener();
    e.setField("message", payload1);
    e.setField(QueryState, "FINISHED");
    e.setField(QueryId, "20250605_195009_00010_34b3y");

    Collection<Event> result =
        filterWithKeyword.filter(Collections.singletonList(e), matchListener);
    assertNotNull(result.iterator().next().getField("GuardRecord"));
  }

  private TrinoGuardiumFilter getGuardiumFilterConnector(String jsonString) {
    Context context = new ContextImpl(null, null);
    TrinoGuardiumFilter filter = new TrinoGuardiumFilter("test-id", null, context);
    return filter;
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
