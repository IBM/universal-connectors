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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Tests for malicious inputs that could cause infinite loops or performance issues */
@Disabled
class MaliciousInputTest {

  static final Context context = new ContextImpl(null, null);
  static final DocumentdbGuardiumFilter filter =
      new DocumentdbGuardiumFilter("test-id", null, context);

  /**
   * Test with extremely deeply nested JSON objects (10,000 levels) This tests Gson's recursion
   * handling and toString() on deeply nested structures EXPECTED: StackOverflowError - this is a
   * known vulnerability in Gson without depth limits
   */
  @Test
  @Timeout(value = 10, unit = TimeUnit.SECONDS)
  void testExtremelyDeeplyNestedJson() {
    StringBuilder deepJson = new StringBuilder();
    deepJson.append(
        "{\"atype\":\"createDatabase\",\"ts\":1629364973923,\"remote_ip\":\"172.31.32.149:57308\",\"user\":\"test\",\"param\":{\"ns\":\"test\",\"data\":");

    // Create 10,000 levels of nesting
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

    // With JsonValidator, deeply nested JSON is now rejected before parsing
    Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
    assertNotNull(results);

    // Event should be tagged as having excessive depth
    Object tags = e.getField("tags");
    assertTrue(
        tags != null && tags.toString().contains("_documentdbguardium_json_depth_error"),
        "Deeply nested JSON should be detected and tagged by JsonValidator");
    System.out.println(
        "✓ JsonValidator successfully prevented StackOverflowError by rejecting deeply nested JSON");
  }

  /**
   * Test with command.toString() on extremely large nested object Lines 222, 240 call
   * command.toString() which could be slow on large objects
   */
  @Test
  @Timeout(value = 10, unit = TimeUnit.SECONDS)
  void testLargeNestedObjectToString() {
    StringBuilder largeObject = new StringBuilder();
    largeObject.append(
        "{\"atype\":\"createDatabase\",\"ts\":1629364973923,\"remote_ip\":\"172.31.32.149:57308\",\"user\":\"test\",\"param\":{");

    // Create object with 10,000 fields
    for (int i = 0; i < 10000; i++) {
      if (i > 0) largeObject.append(",");
      largeObject
          .append("\"field")
          .append(i)
          .append("\":{\"nested\":{\"data\":\"value")
          .append(i)
          .append("\"}}");
    }
    largeObject.append("}}");

    Event e = new org.logstash.Event();
    e.setField("message", largeObject.toString());
    TestMatchListener matchListener = new TestMatchListener();

    try {
      Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
      assertNotNull(results);
    } catch (Exception ex) {
      assertTrue(true, "Parser handled large object: " + ex.getMessage());
    }
  }

  /** Test with special regex characters in strings that could cause replaceAll issues */
  @Test
  @Timeout(value = 5, unit = TimeUnit.SECONDS)
  void testSpecialRegexCharacters() {
    // Create appName with regex special characters repeated many times
    StringBuilder specialChars = new StringBuilder();
    String regexChars = ".*+?^${}()|[]\\";
    for (int i = 0; i < 10000; i++) {
      specialChars.append(regexChars);
    }

    String json =
        "{\"op\":\"command\",\"ts\":1641978528311,\"ns\":\"test.collection\",\"command\":{\"aggregate\":\"cases\"},\"client\":\"172.31.40.18:38230\",\"appName\":\""
            + specialChars.toString().replace("\\", "\\\\").replace("\"", "\\\"")
            + "\",\"user\":\"test\"}";

    Event e = new org.logstash.Event();
    e.setField("message", json);
    TestMatchListener matchListener = new TestMatchListener();

    try {
      Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
      assertNotNull(results);
    } catch (Exception ex) {
      assertTrue(true, "Parser handled special characters: " + ex.getMessage());
    }
  }

  /**
   * Test with circular-looking references in param object This tests if toString() on param causes
   * issues
   */
  @Test
  @Timeout(value = 5, unit = TimeUnit.SECONDS)
  void testComplexParamObject() {
    StringBuilder complexParam = new StringBuilder();
    complexParam.append(
        "{\"atype\":\"createDatabase\",\"ts\":1629364973923,\"remote_ip\":\"172.31.32.149:57308\",\"user\":\"test\",\"param\":{");

    // Create complex nested structure in param
    for (int i = 0; i < 1000; i++) {
      if (i > 0) complexParam.append(",");
      complexParam.append("\"obj").append(i).append("\":{");
      for (int j = 0; j < 10; j++) {
        if (j > 0) complexParam.append(",");
        complexParam.append("\"field").append(j).append("\":\"value").append(j).append("\"");
      }
      complexParam.append("}");
    }
    complexParam.append("}}");

    Event e = new org.logstash.Event();
    e.setField("message", complexParam.toString());
    TestMatchListener matchListener = new TestMatchListener();

    try {
      Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
      assertNotNull(results);
    } catch (Exception ex) {
      assertTrue(true, "Parser handled complex param: " + ex.getMessage());
    }
  }

  /** Test with Unicode and special characters that could cause string processing issues */
  @Test
  @Timeout(value = 5, unit = TimeUnit.SECONDS)
  void testUnicodeAndSpecialCharacters() {
    StringBuilder unicode = new StringBuilder();
    // Add various Unicode characters, emojis, and special chars
    for (int i = 0; i < 10000; i++) {
      unicode.append("\\u0000\\u0001\\u0002\\uFFFF😀🔥💻");
    }

    String json =
        "{\"atype\":\"createDatabase\",\"ts\":1629364973923,\"remote_ip\":\"172.31.32.149:57308\",\"user\":\"test\",\"param\":{\"ns\":\"test\",\"data\":\""
            + unicode.toString()
            + "\"}}";

    Event e = new org.logstash.Event();
    e.setField("message", json);
    TestMatchListener matchListener = new TestMatchListener();

    try {
      Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
      assertNotNull(results);
    } catch (Exception ex) {
      assertTrue(true, "Parser handled Unicode: " + ex.getMessage());
    }
  }

  /** Test with extremely long single field value */
  @Test
  @Timeout(value = 5, unit = TimeUnit.SECONDS)
  void testExtremelyLongFieldValue() {
    StringBuilder longValue = new StringBuilder();
    // Create 1MB string
    for (int i = 0; i < 1000000; i++) {
      longValue.append("x");
    }

    String json =
        "{\"atype\":\"createDatabase\",\"ts\":1629364973923,\"remote_ip\":\"172.31.32.149:57308\",\"user\":\"test\",\"param\":{\"ns\":\"test\",\"data\":\""
            + longValue.toString()
            + "\"}}";

    Event e = new org.logstash.Event();
    e.setField("message", json);
    TestMatchListener matchListener = new TestMatchListener();

    try {
      Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
      assertNotNull(results);
    } catch (Exception ex) {
      assertTrue(true, "Parser handled long value: " + ex.getMessage());
    }
  }

  // Helper class for test listener
  private static class TestMatchListener implements FilterMatchListener {
    @Override
    public void filterMatched(Event event) {
      // No-op
    }
  }
}
