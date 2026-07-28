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
    @Test
    public void testSanitizeMongoBsonLiterals_NullOrEmpty() {
        assertNull(StringUtils.sanitizeMongoBsonLiterals(null));
        assertEquals("", StringUtils.sanitizeMongoBsonLiterals(""));
    }

    @Test
    public void testSanitizeMongoBsonLiterals_NoSlashFastPath() {
        String input = "{\"a\":\"hello\",\"b\":42}";
        assertSame(input, StringUtils.sanitizeMongoBsonLiterals(input));
    }

    @Test
    public void testSanitizeMongoBsonLiterals_NoRegexLiteral() {
        String input = "{\"a\":1,\"b\":\"hello\"}";
        assertEquals(input, StringUtils.sanitizeMongoBsonLiterals(input));
    }

    // ── basic structural positions ─────────────────────────────────────────────

    @Test
    public void testSanitizeMongoBsonLiterals_SimpleRegex() {
        String input = "{\"key\":/^abc/}";
        assertEquals("{\"key\":\"/^abc/\"}", StringUtils.sanitizeMongoBsonLiterals(input));
    }

    @Test
    public void testSanitizeMongoBsonLiterals_RegexFollowedByCommaAndMoreKeys() {
        String input = "{\"a\":{\"b\":/^abc/},\"c\":\"val\"}";
        assertEquals("{\"a\":{\"b\":\"/^abc/\"},\"c\":\"val\"}", StringUtils.sanitizeMongoBsonLiterals(input));
    }

    @Test
    public void testSanitizeMongoBsonLiterals_RegexWithFlags() {
        String input = "{\"key\":/^abc/i}";
        assertEquals("{\"key\":\"/^abc/i\"}", StringUtils.sanitizeMongoBsonLiterals(input));
    }

    @Test
    public void testSanitizeMongoBsonLiterals_NinArrayThenNotRegex() {
        // Array closes before the regex — regex must still be quoted
        String input = "{\"a\":{\"b\":[\"x\",\"y\"],\"c\":/^abc/},\"d\":\"val\"}";
        com.google.gson.JsonObject obj = com.google.gson.JsonParser.parseString(
                StringUtils.sanitizeMongoBsonLiterals(input)).getAsJsonObject();
        assertEquals("/^abc/", obj.getAsJsonObject("a").get("c").getAsString());
        assertEquals("x",      obj.getAsJsonObject("a").getAsJsonArray("b").get(0).getAsString());
    }

    @Test
    public void testSanitizeMongoBsonLiterals_MultipleOperatorsThenNotRegex() {
        // Multiple sibling keys before the regex — all must survive unchanged
        String input = "{\"a\":{\"b\":[\"x\",\"y\"],\"c\":\"z\",\"d\":/^abc/}}";
        com.google.gson.JsonObject obj = com.google.gson.JsonParser.parseString(
                StringUtils.sanitizeMongoBsonLiterals(input)).getAsJsonObject();
        com.google.gson.JsonObject a = obj.getAsJsonObject("a");
        assertEquals("/^abc/", a.get("d").getAsString());
        assertEquals("z",      a.get("c").getAsString());
        assertEquals("x",      a.getAsJsonArray("b").get(0).getAsString());
    }

    // ── regex with backslash escape sequences ─────────────────────────────────

    @Test
    public void testSanitizeMongoBsonLiterals_PipelineMatchWithBackslashRegex() {
        String input = "{\"a\":[{\"b\":{\"c\":/.*\\wsome term.*/i}}]}";
        com.google.gson.JsonObject obj = com.google.gson.JsonParser.parseString(
                StringUtils.sanitizeMongoBsonLiterals(input)).getAsJsonObject();
        String val = obj.getAsJsonArray("a").get(0).getAsJsonObject()
                .getAsJsonObject("b").get("c").getAsString();
        assertTrue(val.contains("some term"));
    }

    @Test
    public void testSanitizeMongoBsonLiterals_PipelineMatchMultipleRegexFields() {
        // Two regex values as siblings — both must be quoted
        String input = "{\"a\":[{\"b\":{"
                + "\"c\":/.*alpha.*/i,"
                + "\"d\":/.*beta.*/i,"
                + "\"e\":{\"f\":[\"x\",\"y\"]}}}]}";
        com.google.gson.JsonObject obj = com.google.gson.JsonParser.parseString(
                StringUtils.sanitizeMongoBsonLiterals(input)).getAsJsonObject();
        com.google.gson.JsonObject b = obj.getAsJsonArray("a").get(0)
                .getAsJsonObject().getAsJsonObject("b");
        assertTrue(b.get("c").getAsString().contains("alpha"));
        assertTrue(b.get("d").getAsString().contains("beta"));
    }

    // ── strings containing slashes must not be touched ─────────────────────────

    @Test
    public void testSanitizeMongoBsonLiterals_Base64SlashInsideStringNotTouched() {
        String input = "{\"key\":\"abc/def==\"}";
        assertEquals(input, StringUtils.sanitizeMongoBsonLiterals(input));
    }

    @Test
    public void testSanitizeMongoBsonLiterals_StringValueStartingWithSlashNotTouched() {
        String input = "{\"key\":\"/a/b/c\"}";
        assertEquals(input, StringUtils.sanitizeMongoBsonLiterals(input));
    }

    @Test
    public void testSanitizeMongoBsonLiterals_Base64SlashAndRegexInSameDocument() {
        // Slash inside a quoted string must not be touched; unquoted regex must be quoted
        String input = "{\"a\":{\"key\":\"abc/def==\"},\"b\":/^abc/}";
        com.google.gson.JsonObject obj = com.google.gson.JsonParser.parseString(
                StringUtils.sanitizeMongoBsonLiterals(input)).getAsJsonObject();
        assertEquals("/^abc/",    obj.get("b").getAsString());
        assertEquals("abc/def==", obj.getAsJsonObject("a").get("key").getAsString());
    }

    // ── patterns with backslash sequences and escaped slashes ─────────────────

    @Test
    public void testSanitizeMongoBsonLiterals_RegexWithBackslashSequences() {
        String input = "{\"key\":/.*\\\\wsome value.*/i}";
        com.google.gson.JsonObject obj = com.google.gson.JsonParser.parseString(
                StringUtils.sanitizeMongoBsonLiterals(input)).getAsJsonObject();
        assertTrue(obj.get("key").getAsString().contains("some value"));
    }

    @Test
    public void testSanitizeMongoBsonLiterals_RegexWithEscapedSlash() {
        String input = "{\"key\":/^a?:\\/\\//i}";
        com.google.gson.JsonObject obj = com.google.gson.JsonParser.parseString(
                StringUtils.sanitizeMongoBsonLiterals(input)).getAsJsonObject();
        assertTrue(obj.get("key").getAsString().startsWith("/^a"));
    }

    // ── nested document with regex ─────────────────────────────────────────────

    @Test
    public void testSanitizeMongoBsonLiterals_RegexInsideAggregatePipeline() {
        String input = "{\"a\":{\"b\":{\"c\":"
                + "[{\"d\":{\"key\":/.*\\\\wtest value.*/i,"
                + "\"e\":{\"f\":[\"x\",\"y\"]}}}],"
                + "\"g\":0}}}";
        com.google.gson.JsonObject obj = com.google.gson.JsonParser.parseString(
                StringUtils.sanitizeMongoBsonLiterals(input)).getAsJsonObject();
        String val = obj.getAsJsonObject("a").getAsJsonObject("b")
                .getAsJsonArray("c").get(0).getAsJsonObject()
                .getAsJsonObject("d").get("key").getAsString();
        assertTrue(val.contains("test value"));
    }

    @Test
    public void testSanitizeMongoBsonLiterals_NoSlashInInput() {
        String input = "{\"a\":{\"b\":{\"c\":true},\"d\":0}}";
        assertSame(input, StringUtils.sanitizeMongoBsonLiterals(input));
    }
}
