package com.ibm.guardium.documentdb;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.concurrent.TimeUnit;

/** Demonstrates the difference between StackOverflowError and OutOfMemoryError */
@Disabled
class MemoryIssuesTest {

  /**
   * StackOverflowError: Caused by deep recursion (stack depth) Stack memory is limited (typically
   * 1-2MB per thread)
   */
  @Test
  @Timeout(value = 5, unit = TimeUnit.SECONDS)
  void testStackOverflowError() {
    System.out.println("\n=== StackOverflowError Test ===");

    // Create deeply nested JSON (10,000 levels)
    StringBuilder deepJson = new StringBuilder("{");
    for (int i = 0; i < 10000; i++) {
      deepJson.append("\"level\":").append("{");
    }
    deepJson.append("\"value\":1");
    for (int i = 0; i < 10000; i++) {
      deepJson.append("}");
    }
    deepJson.append("}");

    System.out.println("JSON string size: " + deepJson.length() + " bytes");
    System.out.println("Nesting depth: 10,000 levels");

    try {
      JsonObject obj = new Gson().fromJson(deepJson.toString(), JsonObject.class);
      System.out.println("✗ Unexpectedly succeeded");
    } catch (StackOverflowError soe) {
      System.out.println("✓ StackOverflowError caught (as expected)");
      System.out.println("  Cause: Recursive parsing exceeded stack depth");
      System.out.println("  Stack memory exhausted, but heap memory is fine");
    }
  }

  /**
   * OutOfMemoryError: Caused by too much data (heap exhaustion) Heap memory is much larger (GBs
   * available)
   */
  @Test
  @Timeout(value = 10, unit = TimeUnit.SECONDS)
  void testPotentialOutOfMemoryScenario() {
    System.out.println("\n=== Potential OutOfMemoryError Scenario ===");

    // Create JSON with MANY fields (not deeply nested, but wide)
    // This consumes heap memory, not stack
    StringBuilder wideJson = new StringBuilder("{");
    int fieldCount = 100000; // 100K fields

    for (int i = 0; i < fieldCount; i++) {
      if (i > 0) wideJson.append(",");
      wideJson.append("\"field").append(i).append("\":\"value").append(i).append("\"");
    }
    wideJson.append("}");

    System.out.println("JSON string size: " + (wideJson.length() / 1024) + " KB");
    System.out.println("Field count: " + fieldCount);
    System.out.println("Nesting depth: 1 (shallow)");

    try {
      // Get memory before parsing
      Runtime runtime = Runtime.getRuntime();
      long memBefore = runtime.totalMemory() - runtime.freeMemory();

      JsonObject obj = new Gson().fromJson(wideJson.toString(), JsonObject.class);

      long memAfter = runtime.totalMemory() - runtime.freeMemory();
      long memUsed = (memAfter - memBefore) / 1024 / 1024; // MB

      System.out.println("✓ Parsing succeeded");
      System.out.println("  Memory used: ~" + memUsed + " MB");
      System.out.println("  This uses HEAP memory, not stack");
      System.out.println("  With millions of fields, could cause OOM");
    } catch (OutOfMemoryError oom) {
      System.out.println("✗ OutOfMemoryError occurred");
      System.out.println("  Heap memory exhausted");
    } catch (Exception e) {
      System.out.println("Other exception: " + e.getMessage());
    }
  }

  /** Demonstrates the difference in memory usage */
  @Test
  void testMemoryDifference() {
    System.out.println("\n=== Memory Usage Comparison ===");

    // Stack memory per thread (JVM default)
    System.out.println("Stack memory per thread: ~1-2 MB (JVM default -Xss)");
    System.out.println("  - Used for method calls, local variables");
    System.out.println("  - StackOverflowError when exceeded");
    System.out.println();

    // Heap memory
    Runtime runtime = Runtime.getRuntime();
    long maxHeap = runtime.maxMemory() / 1024 / 1024; // MB
    long totalHeap = runtime.totalMemory() / 1024 / 1024; // MB
    long freeHeap = runtime.freeMemory() / 1024 / 1024; // MB

    System.out.println("Heap memory:");
    System.out.println("  - Max heap: " + maxHeap + " MB (JVM -Xmx)");
    System.out.println("  - Total heap: " + totalHeap + " MB");
    System.out.println("  - Free heap: " + freeHeap + " MB");
    System.out.println("  - Used for objects, arrays, data structures");
    System.out.println("  - OutOfMemoryError when exceeded");
    System.out.println();

    System.out.println("Key Difference:");
    System.out.println("  - Deep nesting (10K levels) → StackOverflowError");
    System.out.println("  - Wide data (millions of fields) → OutOfMemoryError");
    System.out.println("  - Both are security concerns!");
  }
}
