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
    // --- sanitizeMongoBsonLiterals ---

    @Test
    public void testSanitizeMongoBsonLiterals_NullOrEmpty() {
        assertNull(StringUtils.sanitizeMongoBsonLiterals(null));
        assertEquals("", StringUtils.sanitizeMongoBsonLiterals(""));
    }

    @Test
    public void testSanitizeMongoBsonLiterals_NoSlashFastPath() {
        // No slash at all — original reference returned unchanged (fast-path)
        String input = "{\"a\":\"hello\",\"b\":42}";
        assertSame(input, StringUtils.sanitizeMongoBsonLiterals(input));
    }

    @Test
    public void testSanitizeMongoBsonLiterals_NoRegexLiteral() {
        // Has slashes but all inside string values — nothing changes
        String input = "{\"a\":1,\"b\":\"hello\"}";
        assertEquals(input, StringUtils.sanitizeMongoBsonLiterals(input));
    }

    @Test
    public void testSanitizeMongoBsonLiterals_SimpleRegex() {
        // "$not":/^AQA/  — the core production failure pattern
        String input = "{\"$not\":/^AQA/}";
        assertEquals("{\"$not\":\"/^AQA/\"}", StringUtils.sanitizeMongoBsonLiterals(input));
    }

    @Test
    public void testSanitizeMongoBsonLiterals_RegexFollowedByCommaAndMoreKeys() {
        // Regex is followed by },"nextKey" — surrounding structure must be preserved exactly
        String input = "{\"MARSHA_CODE\":{\"$not\":/^AQA/},\"TR_LANGUAGE\":\"en_US\"}";
        String result = StringUtils.sanitizeMongoBsonLiterals(input);
        assertEquals("{\"MARSHA_CODE\":{\"$not\":\"/^AQA/\"},\"TR_LANGUAGE\":\"en_US\"}", result);
    }

    @Test
    public void testSanitizeMongoBsonLiterals_RegexWithFlags() {
        String input = "{\"$regex\":/^abc/i}";
        assertEquals("{\"$regex\":\"/^abc/i\"}", StringUtils.sanitizeMongoBsonLiterals(input));
    }

    @Test
    public void testSanitizeMongoBsonLiterals_NinArrayThenNotRegex() {
        // Production error 1: $nin:[...], "$not":/^AQA/  in same object
        String input = "{\"MARSHA_CODE\":{\"$nin\":[\"ANURO\",\"BGIAH\"],\"$not\":/^AQA/},\"TR_LANGUAGE\":\"en_US\"}";
        com.google.gson.JsonObject obj = com.google.gson.JsonParser.parseString(
                StringUtils.sanitizeMongoBsonLiterals(input)).getAsJsonObject();
        assertEquals("/^AQA/", obj.getAsJsonObject("MARSHA_CODE").get("$not").getAsString());
        assertEquals("ANURO",  obj.getAsJsonObject("MARSHA_CODE").getAsJsonArray("$nin").get(0).getAsString());
    }

    @Test
    public void testSanitizeMongoBsonLiterals_Base64SlashInsideStringNotTouched() {
        // A '/' inside a quoted $binary value must never be treated as a regex literal
        String input = "{\"$binary\":\"4tf92TTyQ/SD0uOgHZLbBA==\"}";
        assertEquals(input, StringUtils.sanitizeMongoBsonLiterals(input));
    }

    @Test
    public void testSanitizeMongoBsonLiterals_StringValueStartingWithSlashNotTouched() {
        // A string value that starts with '/' must not be double-quoted —
        // the ':' precedes the opening '"', not the '/' directly
        String input = "{\"path\":\"/some/route\"}";
        assertEquals(input, StringUtils.sanitizeMongoBsonLiterals(input));
    }

    @Test
    public void testSanitizeMongoBsonLiterals_Base64SlashAndRegexInSameDocument() {
        // $binary with slash inside string alongside a BSON regex literal —
        // only the regex should be quoted
        String input = "{\"lsid\":{\"$binary\":\"4tf92TTyQ/SD==\"},\"$not\":/^AQA/}";
        com.google.gson.JsonObject obj = com.google.gson.JsonParser.parseString(
                StringUtils.sanitizeMongoBsonLiterals(input)).getAsJsonObject();
        assertEquals("/^AQA/", obj.get("$not").getAsString());
        assertEquals("4tf92TTyQ/SD==", obj.getAsJsonObject("lsid").get("$binary").getAsString());
    }

    @Test
    public void testSanitizeMongoBsonLiterals_RegexWithBackslashSequences() {
        // \Q...\E quotemeta with spaces — the production aggregate pipeline error
        String input = "{\"name\":/.*\\\\QSANDERS TRAVEL\\\\E.*/i}";
        com.google.gson.JsonObject obj = com.google.gson.JsonParser.parseString(
                StringUtils.sanitizeMongoBsonLiterals(input)).getAsJsonObject();
        assertTrue(obj.get("name").getAsString().contains("SANDERS TRAVEL"));
    }

    @Test
    public void testSanitizeMongoBsonLiterals_RegexWithEscapedSlash() {
        // Escaped forward slash inside pattern: /^https?:\/\//i
        String input = "{\"url\":/^https?:\\/\\//i}";
        com.google.gson.JsonObject obj = com.google.gson.JsonParser.parseString(
                StringUtils.sanitizeMongoBsonLiterals(input)).getAsJsonObject();
        assertTrue(obj.get("url").getAsString().startsWith("/^https"));
    }

    @Test
    public void testSanitizeMongoBsonLiterals_ProductionAggregatePipeline() {
        // Exact message field from the aggregate pipeline production error
        String input = "{\"atype\":\"authCheck\",\"param\":{\"command\":\"aggregate\","
                + "\"ns\":\"corp_sales.ctac_agency_profile\",\"args\":{\"pipeline\":"
                + "[{\"$match\":{\"name\":/.*\\\\QSANDERS TRAVEL\\\\E.*/i,"
                + "\"statusIndicator\":{\"$in\":[\"A\",\"C\"]}}}],"
                + "\"result\":0}}}";
        com.google.gson.JsonObject obj = com.google.gson.JsonParser.parseString(
                StringUtils.sanitizeMongoBsonLiterals(input)).getAsJsonObject();
        String name = obj.getAsJsonObject("param").getAsJsonObject("args")
                .getAsJsonArray("pipeline").get(0).getAsJsonObject()
                .getAsJsonObject("$match").get("name").getAsString();
        assertTrue(name.contains("SANDERS TRAVEL"));
    }

    @Test
    public void testSanitizeMongoBsonLiterals_TruncatedJsonNotModified() {
        // DocumentDB args-truncation ("upsert":tru...) — sanitizeMongoBsonLiterals does not
        // touch it (no '/' in input, fast-path returns original reference unchanged).
        // Gson parses "tru..." as an unquoted string value; the real failure happens
        // when Gson hits a genuinely malformed structure in the full production message.
        String input = "{\"atype\":\"authCheck\",\"param\":{\"args\":{\"upsert\":tru...},\"result\":0}}";
        assertSame(input, StringUtils.sanitizeMongoBsonLiterals(input));
    }
}
