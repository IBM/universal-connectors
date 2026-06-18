package com.ibm.guardium.documentdb;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for StringUtils class.
 */
public class StringUtilsTest {

    @Test
    public void testRemoveWhitespace_WithSpaces() {
        assertEquals("HelloWorld", StringUtils.removeWhitespace("Hello World"));
        assertEquals("test", StringUtils.removeWhitespace("  test  "));
        assertEquals("abc", StringUtils.removeWhitespace("a b c"));
    }

    @Test
    public void testRemoveWhitespace_WithTabs() {
        assertEquals("HelloWorld", StringUtils.removeWhitespace("Hello\tWorld"));
        assertEquals("test", StringUtils.removeWhitespace("\ttest\t"));
    }

    @Test
    public void testRemoveWhitespace_WithNewlines() {
        assertEquals("HelloWorld", StringUtils.removeWhitespace("Hello\nWorld"));
        assertEquals("HelloWorld", StringUtils.removeWhitespace("Hello\r\nWorld"));
    }

    @Test
    public void testRemoveWhitespace_NullOrEmpty() {
        assertNull(StringUtils.removeWhitespace(null));
        assertEquals("", StringUtils.removeWhitespace(""));
    }

    @Test
    public void testRemoveWhitespace_NoWhitespace() {
        assertEquals("HelloWorld", StringUtils.removeWhitespace("HelloWorld"));
    }

    @Test
    public void testExtractDbNameFromNs_Valid() {
        assertEquals("testdb", StringUtils.extractDbNameFromNs("testdb.collection"));
        assertEquals("mydb", StringUtils.extractDbNameFromNs("mydb.users"));
        assertEquals("db", StringUtils.extractDbNameFromNs("db.col"));
    }

    @Test
    public void testExtractDbNameFromNs_NoDot() {
        assertEquals("testdb", StringUtils.extractDbNameFromNs("testdb"));
        assertEquals("single", StringUtils.extractDbNameFromNs("single"));
    }

    @Test
    public void testExtractDbNameFromNs_MultipleDots() {
        assertEquals("db", StringUtils.extractDbNameFromNs("db.collection.subcollection"));
    }

    @Test
    public void testExtractDbNameFromNs_NullOrEmpty() {
        assertEquals(Constants.UNKNOWN_STRING, StringUtils.extractDbNameFromNs(null));
        assertEquals(Constants.UNKNOWN_STRING, StringUtils.extractDbNameFromNs(""));
    }

    @Test
    public void testExtractCollectionFromNs_Valid() {
        assertEquals("collection", StringUtils.extractCollectionFromNs("testdb.collection"));
        assertEquals("users", StringUtils.extractCollectionFromNs("mydb.users"));
        assertEquals("col", StringUtils.extractCollectionFromNs("db.col"));
    }

    @Test
    public void testExtractCollectionFromNs_NoDot() {
        assertEquals("testdb", StringUtils.extractCollectionFromNs("testdb"));
        assertEquals("single", StringUtils.extractCollectionFromNs("single"));
    }

    @Test
    public void testExtractCollectionFromNs_MultipleDots() {
        assertEquals("collection.subcollection", 
            StringUtils.extractCollectionFromNs("db.collection.subcollection"));
    }

    @Test
    public void testExtractCollectionFromNs_NullOrEmpty() {
        assertNull(StringUtils.extractCollectionFromNs(null));
        assertEquals("", StringUtils.extractCollectionFromNs(""));
    }

    @Test
    public void testTruncate_ExceedsMaxLength() {
        String longString = "This is a very long string that needs to be truncated";
        String result = StringUtils.truncate(longString, 20, "...");
        assertEquals("This is a very long ...".length(), result.length());
        assertTrue(result.endsWith("..."));
        assertEquals("This is a very long ...", result);
    }

    @Test
    public void testTruncate_WithinMaxLength() {
        String shortString = "Short";
        String result = StringUtils.truncate(shortString, 20, "...");
        assertEquals("Short", result);
    }

    @Test
    public void testTruncate_ExactMaxLength() {
        String exactString = "12345678901234567890";
        String result = StringUtils.truncate(exactString, 20, "...");
        assertEquals("12345678901234567890", result);
    }

    @Test
    public void testTruncate_NullString() {
        assertNull(StringUtils.truncate(null, 10, "..."));
    }

    @Test
    public void testContainsAnyProfilerKey_WithAggregateKey() {
        assertTrue(StringUtils.containsAnyProfilerKey("{ \"aggregate\": \"collection\" }"));
    }

    @Test
    public void testContainsAnyProfilerKey_WithCountKey() {
        assertTrue(StringUtils.containsAnyProfilerKey("{ \"count\": \"collection\" }"));
    }

    @Test
    public void testContainsAnyProfilerKey_WithInsertKey() {
        assertTrue(StringUtils.containsAnyProfilerKey("{ \"insert\": \"collection\" }"));
    }

    @Test
    public void testContainsAnyProfilerKey_WithUpdateKey() {
        assertTrue(StringUtils.containsAnyProfilerKey("{ \"update\": \"collection\" }"));
    }

    @Test
    public void testContainsAnyProfilerKey_WithRemoveKey() {
        assertTrue(StringUtils.containsAnyProfilerKey("{ \"remove\": \"collection\" }"));
    }

    @Test
    public void testContainsAnyProfilerKey_WithFindKey() {
        assertTrue(StringUtils.containsAnyProfilerKey("{ \"find\": \"collection\" }"));
    }

    @Test
    public void testContainsAnyProfilerKey_WithDistinctKey() {
        assertTrue(StringUtils.containsAnyProfilerKey("{ \"distinct\": \"collection\" }"));
    }

    @Test
    public void testContainsAnyProfilerKey_WithFindAndModifyKey() {
        assertTrue(StringUtils.containsAnyProfilerKey("{ \"findAndModify\": \"collection\" }"));
    }

    @Test
    public void testContainsAnyProfilerKey_NoProfilerKey() {
        assertFalse(StringUtils.containsAnyProfilerKey("{ \"other\": \"value\" }"));
        assertFalse(StringUtils.containsAnyProfilerKey("{ \"atype\": \"authenticate\" }"));
    }

    @Test
    public void testContainsAnyProfilerKey_NullOrEmpty() {
        assertFalse(StringUtils.containsAnyProfilerKey(null));
        assertFalse(StringUtils.containsAnyProfilerKey(""));
    }

    @Test
    public void testContainsAnyProfilerKey_MultipleKeys() {
        assertTrue(StringUtils.containsAnyProfilerKey("{ \"insert\": \"col\", \"update\": \"col\" }"));
    }
}

// Made with Bob
