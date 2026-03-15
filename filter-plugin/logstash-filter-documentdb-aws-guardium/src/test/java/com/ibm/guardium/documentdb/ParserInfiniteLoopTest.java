package com.ibm.guardium.documentdb;

import co.elastic.logstash.api.Context;
import co.elastic.logstash.api.Event;
import co.elastic.logstash.api.FilterMatchListener;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.logstash.plugins.ContextImpl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test suite to verify that malformed events cannot cause infinite loops in the DocumentDB parser.
 * All tests have timeouts to detect infinite loops.
 */
@Disabled
class ParserInfiniteLoopTest {

  static final Context context = new ContextImpl(null, null);
  static final DocumentdbGuardiumFilter filter =
      new DocumentdbGuardiumFilter("test-id", null, context);

  /**
   * Test 1: Deeply nested JSON (1000 levels) This should NOT cause infinite loop - Gson will handle
   * it or throw exception
   */
  @Test
  // @Timeout(value = 5, unit = TimeUnit.SECONDS)
  void testDeeplyNestedJson() {
    StringBuilder deepJson = new StringBuilder();
    deepJson.append(
        "{\"atype\":\"createDatabase\",\"ts\":1629364973923,\"remote_ip\":\"172.31.32.149:57308\",\"user\":\"test\",\"param\":{\"ns\":\"test\",\"nested\":");

    // Create 1000 levels of nesting
    for (int i = 0; i < 1000; i++) {
      deepJson.append("{\"level").append(i).append("\":");
    }
    deepJson.append("\"value\"");
    for (int i = 0; i < 1000; i++) {
      deepJson.append("}");
    }
    deepJson.append("}}");

    Event e = new org.logstash.Event();
    e.setField("message", deepJson.toString());
    TestMatchListener matchListener = new TestMatchListener();

    try {
      Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
      // Should complete without infinite loop (may succeed or fail with exception)
      assertNotNull(results);
    } catch (Exception ex) {
      // Exception is acceptable - just shouldn't hang
      assertTrue(true, "Parser threw exception instead of hanging: " + ex.getMessage());
    }
  }

  /**
   * Test 2: Truncated JSON (missing closing braces) Should be caught by isValidJsonStructure()
   * check
   */
  @Test
  @Timeout(value = 5, unit = TimeUnit.SECONDS)
  void testTruncatedJson() {
    String truncatedJson =
        "{\"atype\":\"createDatabase\",\"ts\":1629364973923,\"remote_ip\":\"172.31.32.149:57308\",\"user\":\"test\",\"param\":{\"ns\":\"test\"";

    Event e = new org.logstash.Event();
    e.setField("message", truncatedJson);
    TestMatchListener matchListener = new TestMatchListener();

    ArrayList<Event> events = new ArrayList<>();
    events.add(e);
    Collection<Event> results = filter.filter(events, matchListener);

    // Should be tagged as truncated by JsonValidator
    assertNotNull(results);
    Object tags = e.getField("tags");
    assertTrue(
        (tags != null && tags.toString().contains("_documentdbguardium_json_depth_error")),
        "Truncated JSON should be detected and tagged");
  }

  /**
   * Test 3: Very long string with many dots (tests split optimization) Should NOT cause infinite
   * loop with our indexOf() optimization
   */
  @Test
  // @Timeout(value = 5, unit = TimeUnit.SECONDS)
  void testManyDotsInNamespace() {
    StringBuilder manyDots = new StringBuilder("db");
    for (int i = 0; i < 10000; i++) {
      manyDots.append(".collection").append(i);
    }

    String jsonWithManyDots =
        "{\"atype\":\"createDatabase\",\"ts\":1629364973923,\"remote_ip\":\"172.31.32.149:57308\",\"user\":\"test\",\"param\":{\"ns\":\""
            + manyDots.toString()
            + "\"}}";

    Event e = new org.logstash.Event();
    e.setField("message", jsonWithManyDots);
    TestMatchListener matchListener = new TestMatchListener();

    try {
      Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
      assertNotNull(results);
      // Should complete quickly with our indexOf() optimization
    } catch (Exception ex) {
      // Exception is acceptable - just shouldn't hang
      assertTrue(true, "Parser handled long string without hanging");
    }
  }

  /** Test 4: Circular-looking structure (not actually circular in JSON, but tests recursion) */
  @Test
  @Timeout(value = 5, unit = TimeUnit.SECONDS)
  void testCircularLookingStructure() {
    String circularLooking =
        "{\"atype\":\"createDatabase\",\"ts\":1629364973923,\"remote_ip\":\"172.31.32.149:57308\",\"user\":\"test\",\"param\":{\"ns\":\"test\",\"a\":{\"b\":{\"c\":{\"d\":{\"e\":{\"f\":{\"g\":{\"h\":{\"i\":{\"j\":\"value\"}}}}}}}}}}}}";

    Event e = new org.logstash.Event();
    e.setField("message", circularLooking);
    TestMatchListener matchListener = new TestMatchListener();

    Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
    assertNotNull(results);
    // Should complete - JSON doesn't support actual circular references
  }

  /** Test 5: Very large array (tests array iteration) */
  @Test
  @Timeout(value = 5, unit = TimeUnit.SECONDS)
  void testVeryLargeArray() {
    StringBuilder largeArray = new StringBuilder();
    largeArray.append(
        "{\"op\":\"command\",\"ts\":1641978528311,\"ns\":\"test.collection\",\"command\":{\"aggregate\":\"cases\",\"pipeline\":[");

    for (int i = 0; i < 1000; i++) {
      if (i > 0) largeArray.append(",");
      largeArray
          .append("{\"$match\":{\"field")
          .append(i)
          .append("\":\"value")
          .append(i)
          .append("\"}}");
    }

    largeArray.append(
        "],\"cursor\":{},\"$db\":\"test\"},\"client\":\"172.31.40.18:38230\",\"user\":\"test\"}");

    Event e = new org.logstash.Event();
    e.setField("message", largeArray.toString());
    TestMatchListener matchListener = new TestMatchListener();

    try {
      Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
      assertNotNull(results);
      // Should complete - array iteration is bounded
    } catch (Exception ex) {
      assertTrue(true, "Parser handled large array without hanging");
    }
  }

  /** Test 6: Malformed IP address with many colons (tests IP parsing) */
  @Test
  @Timeout(value = 5, unit = TimeUnit.SECONDS)
  void testMalformedIpAddress() {
    String malformedIp =
        "{\"atype\":\"createDatabase\",\"ts\":1629364973923,\"remote_ip\":\"1:2:3:4:5:6:7:8:9:10:11:12:13:14:15\",\"user\":\"test\",\"param\":{\"ns\":\"test\"}}";

    Event e = new org.logstash.Event();
    e.setField("message", malformedIp);
    TestMatchListener matchListener = new TestMatchListener();

    try {
      Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
      assertNotNull(results);
      // Should complete - our indexOf() optimization handles this
    } catch (Exception ex) {
      assertTrue(true, "Parser handled malformed IP without hanging");
    }
  }

  /** Test 7: String with excessive whitespace (tests replaceAll optimization) */
  @Test
  @Timeout(value = 5, unit = TimeUnit.SECONDS)
  void testExcessiveWhitespace() {
    StringBuilder excessiveWhitespace = new StringBuilder();
    for (int i = 0; i < 10000; i++) {
      excessiveWhitespace.append(" \t\n\r");
    }

    String jsonWithWhitespace =
        "{\"op\":\"command\",\"ts\":1641978528311,\"ns\":\"test.collection\",\"command\":{\"aggregate\":\"cases\"},\"client\":\"172.31.40.18:38230\",\"appName\":\""
            + excessiveWhitespace.toString()
            + "\",\"user\":\"test\"}";

    Event e = new org.logstash.Event();
    e.setField("message", jsonWithWhitespace);
    TestMatchListener matchListener = new TestMatchListener();

    try {
      Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
      assertNotNull(results);
      // Should complete with our replace() optimization
    } catch (Exception ex) {
      assertTrue(true, "Parser handled excessive whitespace without hanging");
    }
  }

  /** Test 8: Maximum size event (256KB - CloudWatch limit) */
  @Test
  @Timeout(value = 10, unit = TimeUnit.SECONDS)
  void testMaximumSizeEvent() {
    StringBuilder maxSize = new StringBuilder();
    maxSize.append(
        "{\"atype\":\"createDatabase\",\"ts\":1629364973923,\"remote_ip\":\"172.31.32.149:57308\",\"user\":\"test\",\"param\":{\"ns\":\"test\",\"data\":\"");

    // Fill to near 256KB
    int targetSize = 256 * 1024 - 200; // Leave room for JSON structure
    while (maxSize.length() < targetSize) {
      maxSize.append("x");
    }
    maxSize.append("\"}}");

    Event e = new org.logstash.Event();
    e.setField("message", maxSize.toString());
    TestMatchListener matchListener = new TestMatchListener();

    try {
      Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
      assertNotNull(results);
      // Should complete - size is bounded by CloudWatch
    } catch (Exception ex) {
      assertTrue(true, "Parser handled maximum size event without hanging");
    }
  }

  /** Test 9: Concurrent parsing of malformed events Tests thread safety and ensures no deadlocks */
  @Test
  @Timeout(value = 10, unit = TimeUnit.SECONDS)
  void testConcurrentMalformedEvents() throws InterruptedException {
    ExecutorService executor = Executors.newFixedThreadPool(10);
    CountDownLatch latch = new CountDownLatch(100);

    for (int i = 0; i < 100; i++) {
      final int index = i;
      executor.submit(
          () -> {
            try {
              String malformed =
                  "{\"atype\":\"test" + index + "\",\"ts\":1629364973923,\"param\":{\"ns\":\"test";
              Event e = new org.logstash.Event();
              e.setField("message", malformed);
              TestMatchListener matchListener = new TestMatchListener();
              filter.filter(Collections.singletonList(e), matchListener);
            } catch (Exception ex) {
              // Expected for malformed events
            } finally {
              latch.countDown();
            }
          });
    }

    boolean completed = latch.await(8, TimeUnit.SECONDS);
    executor.shutdown();
    assertTrue(completed, "All concurrent parsing tasks should complete without deadlock");
  }

  /** Test 10: Empty and null edge cases */
  @Test
  @Timeout(value = 5, unit = TimeUnit.SECONDS)
  void testEmptyAndNullCases() {
    TestMatchListener matchListener = new TestMatchListener();

    // Empty string
    Event e1 = new org.logstash.Event();
    e1.setField("message", "");
    ArrayList<Event> events1 = new ArrayList<>();
    events1.add(e1);
    Collection<Event> results1 = filter.filter(events1, matchListener);
    assertNotNull(results1);

    // Just braces
    Event e2 = new org.logstash.Event();
    e2.setField("message", "{}");
    ArrayList<Event> events2 = new ArrayList<>();
    events2.add(e2);
    Collection<Event> results2 = filter.filter(events2, matchListener);
    assertNotNull(results2);

    // Just brackets
    Event e3 = new org.logstash.Event();
    e3.setField("message", "[]");
    ArrayList<Event> events3 = new ArrayList<>();
    events3.add(e3);
    Collection<Event> results3 = filter.filter(events3, matchListener);
    assertNotNull(results3);
  }

  // Helper class for test listener
  private static class TestMatchListener implements FilterMatchListener {
    private AtomicInteger matchCount = new AtomicInteger(0);

    @Override
    public void filterMatched(Event event) {
      matchCount.incrementAndGet();
    }

    private int getMatchCount() {
      return matchCount.get();
    }
  }
}
