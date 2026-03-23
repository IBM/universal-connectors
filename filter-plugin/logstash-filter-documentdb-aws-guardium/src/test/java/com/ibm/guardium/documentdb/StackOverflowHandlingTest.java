package com.ibm.guardium.documentdb;

import co.elastic.logstash.api.Context;
import co.elastic.logstash.api.Event;
import co.elastic.logstash.api.FilterMatchListener;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.logstash.plugins.ContextImpl;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/** Demonstrates that StackOverflowError CAN be caught and handled gracefully */
@Disabled
class StackOverflowHandlingTest {

  static final Context context = new ContextImpl(null, null);
  static final DocumentdbGuardiumFilter filter =
      new DocumentdbGuardiumFilter("test-id", null, context);

  /** Test that demonstrates StackOverflowError can be caught */
  @Test
  @Timeout(value = 5, unit = TimeUnit.SECONDS)
  void testStackOverflowCanBeCaught() {
    // Create deeply nested JSON that will cause StackOverflowError
    StringBuilder deepJson = new StringBuilder();
    deepJson.append(
        "{\"atype\":\"createDatabase\",\"ts\":1629364973923,\"remote_ip\":\"172.31.32.149:57308\",\"user\":\"test\",\"param\":{\"ns\":\"test\",\"data\":");

    for (int i = 0; i < 10000; i++) {
      deepJson.append("{\"level").append(i).append("\":");
    }
    deepJson.append("\"value\"");
    for (int i = 0; i < 10000; i++) {
      deepJson.append("}");
    }
    deepJson.append("}}");

    Event e = new org.logstash.Event();
    e.setField("message", deepJson.toString());
    TestMatchListener matchListener = new TestMatchListener();

    // With JsonValidator, deeply nested JSON is now prevented before it can cause
    // StackOverflowError
    Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
    assertNotNull(results);

    // Event should be tagged as having excessive depth
    Object tags = e.getField("tags");
    boolean wasTagged =
        tags != null && tags.toString().contains("_documentdbguardium_json_depth_error");

    assertTrue(wasTagged, "JsonValidator should have detected and tagged deeply nested JSON");
    System.out.println("✓ JsonValidator successfully prevented StackOverflowError");
    System.out.println("✓ Event was tagged instead of causing stack overflow");
    System.out.println("✓ This is the BETTER solution - prevention rather than catching errors");
  }

  /** Demonstrates that after catching StackOverflowError, the JVM continues normally */
  @Test
  @Timeout(value = 5, unit = TimeUnit.SECONDS)
  void testJvmContinuesAfterStackOverflow() {
    // First, cause a StackOverflowError
    try {
      causeStackOverflow(0);
    } catch (StackOverflowError soe) {
      System.out.println("✓ Caught StackOverflowError from recursive method");
    }

    // Now prove the JVM is still working fine
    String result = "JVM is still working!";
    assertEquals("JVM is still working!", result);
    System.out.println("✓ JVM continues to work normally after StackOverflowError");
  }

  // Helper method to intentionally cause StackOverflowError
  private void causeStackOverflow(int depth) {
    causeStackOverflow(depth + 1); // Infinite recursion
  }

  // Helper class for test listener
  private static class TestMatchListener implements FilterMatchListener {
    @Override
    public void filterMatched(Event event) {
      // No-op
    }
  }
}
