package com.ibm.guardium.documentdb;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.concurrent.TimeUnit;

/** Direct performance test to measure actual execution time of problematic operations */
@Disabled
class PerformanceTest {

  @Test
  @Timeout(value = 2, unit = TimeUnit.SECONDS)
  void testSplitPerformance() {
    // Create string with 100,000 dots
    StringBuilder sb = new StringBuilder("db");
    for (int i = 0; i < 100000; i++) {
      sb.append(".collection").append(i);
    }
    String ns = sb.toString();

    long start = System.currentTimeMillis();
    String[] parts = ns.split("\\.");
    long end = System.currentTimeMillis();

    System.out.println("Split with 100,000 dots took: " + (end - start) + "ms");
    System.out.println("Array length: " + parts.length);
    System.out.println("First element: " + parts[0]);
  }

  @Test
  @Timeout(value = 2, unit = TimeUnit.SECONDS)
  void testReplaceAllPerformance() {
    // Create string with 500,000 whitespace characters
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < 500000; i++) {
      sb.append(" \t\n\r");
    }
    String whitespace = sb.toString();

    long start = System.currentTimeMillis();
    String result = whitespace.trim().replaceAll("\\s", "");
    long end = System.currentTimeMillis();

    System.out.println("ReplaceAll with 2,000,000 whitespace chars took: " + (end - start) + "ms");
    System.out.println("Result length: " + result.length());
  }

  @Test
  @Timeout(value = 2, unit = TimeUnit.SECONDS)
  void testSplitColonPerformance() {
    // Create IP string with many colons
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < 100000; i++) {
      sb.append(i).append(":");
    }
    String ip = sb.toString();

    long start = System.currentTimeMillis();
    String[] parts = ip.split(":");
    long end = System.currentTimeMillis();

    System.out.println("Split colon with 100,000 colons took: " + (end - start) + "ms");
    System.out.println("Array length: " + parts.length);
  }
}
