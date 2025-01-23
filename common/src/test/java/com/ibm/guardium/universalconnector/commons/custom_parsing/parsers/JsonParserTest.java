package com.ibm.guardium.universalconnector.commons.custom_parsing.parsers;

import static junit.framework.TestCase.assertNull;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.ibm.guardium.universalconnector.commons.custom_parsing.parsers.json_parser.JsonUtil;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JsonParserTest {
    private JsonUtil js = new JsonUtil();

    private static final String JSON_SAMPLE_1 = "{\n"
            + "\t\"id\": \"0001\",\n"
            + "\t\"type\": \"donut\",\n"
            + "\t\"name\": \"Cake\",\n"
            + "\t\"ppu\": 0.55,\n"
            + "\t\"batters\":\n"
            + "\t\t{\n"
            + "\t\t\t\"batter\":\n"
            + "\t\t\t\t[\n"
            + "\t\t\t\t\t{ \"id\": \"1001\", \"type\": \"Regular\" },\n"
            + "\t\t\t\t\t{ \"id\": \"1002\", \"type\": \"Chocolate\" },\n"
            + "\t\t\t\t\t{ \"id\": \"1003\", \"type\": \"Blueberry\" },\n"
            + "\t\t\t\t\t{ \"id\": \"1004\", \"type\": \"Devil's Food\" }\n"
            + "\t\t\t\t]\n"
            + "\t\t},\n"
            + "\t\"topping\":\n"
            + "\t\t[\n"
            + "\t\t\t{ \"id\": \"5001\", \"type\": \"None\" },\n"
            + "\t\t\t{ \"id\": \"5002\", \"type\": \"Glazed\" },\n"
            + "\t\t\t{ \"id\": \"5005\", \"type\": \"Sugar\" },\n"
            + "\t\t\t{ \"id\": \"5007\", \"type\": \"Powdered Sugar\" },\n"
            + "\t\t\t{ \"id\": \"5006\", \"type\": \"Chocolate with Sprinkles\" },\n"
            + "\t\t\t{ \"id\": \"5003\", \"type\": \"Chocolate\" },\n"
            + "\t\t\t{ \"id\": \"5004\", \"type\": \"Maple\" }\n"
            + "\t\t]\n"
            + "}";

    @Test
    public void testJsonParse() {
        Map<String, String> map = js.getMap("{\"field1\":\"value1\"}");
        assertEquals(1, map.size());
        assertEquals("value1", map.get("/\"field1\""));

        map = js.getMap(SINGLE_JSON);

        assertEquals(12, map.size());
        assertEquals("0", map.get("/\"severity\""));
        assertEquals("Successfully sent 6 of 6 logs to Kinesis", map.get("/\"message\""));
        assertEquals("33C9A321-088E-43FE-83D7-4C6A4D134937", map.get("/\"decorations\"/\"host_uuid\""));

        map = js.getMap(JSON_SAMPLE_1);
        assertEquals(28, map.size());
        assertEquals("0001", map.get("/\"id\""));
        assertEquals("0.55", map.get("/\"ppu\""));
        assertEquals("Chocolate with Sprinkles", map.get("/\"topping\"[4]/\"type\""));

        // Testing null. Json Util will use the string "null" if a null is found when
        // parsing.
        map = js.getMap(JSON_null);
        assertEquals("null", map.get("/\"hostIdentifier\""));
    }

    private static final String JSON_null = "{\"hostIdentifier\": null}";

    @Test
    public void testJsonParser() {
        JsonParser jp = new JsonParser();

        jp.isPayloadValid("{\"field1\":\"value1\"}");
        Map<String, String> map = jp.extractedProperties;

        assertEquals(1, map.size());
        assertEquals("value1", map.get("/\"field1\""));

        jp.isPayloadValid(SINGLE_JSON);
        map = jp.extractedProperties;
        assertEquals(12, map.size());
        assertEquals("0", map.get("/\"severity\""));
        assertEquals("Successfully sent 6 of 6 logs to Kinesis", map.get("/\"message\""));
        assertEquals("33C9A321-088E-43FE-83D7-4C6A4D134937", map.get("/\"decorations\"/\"host_uuid\""));

        jp.isPayloadValid(JSON_SAMPLE_1);
        map = jp.extractedProperties;
        assertEquals(28, map.size());
        assertEquals("0001", map.get("/\"id\""));
        assertEquals("0.55", map.get("/\"ppu\""));
        assertEquals("Chocolate with Sprinkles", map.get("/\"topping\"[4]/\"type\""));
    }

    private static final String SINGLE_JSON = "{\"hostIdentifier\":\"7d058e67620c\",\"calendarTime\":\"Sun Apr 14 16:17:14 2019 UTC\","
            + "\"unixTime\":\"1555258634\","
            + "\"severity\":\"0\",\"filename\":\"aws_kinesis.cpp\",\"line\":\"142\",\"message\":\"Successfully sent 6 of 6 logs to Kinesis\","
            + "\"version\":\"2.7.0\",\"decorations\":{\"customer_id\":\"1\",\"host_uuid\":\"33C9A321-088E-43FE-83D7-4C6A4D134937\",\"hostname\""
            + ":\"7d058e67620c\"},\"log_type\":\"status\"}";

    private static final String SINGLE_JSON_TRAILING = SINGLE_JSON + " random non json text";
    private static final String DOUBLE_JSON_TEXT = SINGLE_JSON_TRAILING + SINGLE_JSON;
    private static final String SINGLE_JSON_ARRAY = "[" + SINGLE_JSON + "]";

    private static final String JSON_CONTROL_CHARACTERS_IN_QUOTES = "{\"field1}\":\"value{\"}";

    @Test
    public void testJsonWalk() {
        // Simple test, give it a purely JSON string and expect to get the same string
        // back
        assertEquals(SINGLE_JSON, js.walkJson('{', '}', SINGLE_JSON));
        // Simple test with a pure JSON array string
        assertEquals(SINGLE_JSON_ARRAY, js.walkJson('[', ']', SINGLE_JSON_ARRAY));

        // Test a case where there's non-json trailing text
        assertEquals(SINGLE_JSON, js.walkJson('{', '}', SINGLE_JSON_TRAILING));
        // Trailing text and another JSON object
        assertEquals(SINGLE_JSON, js.walkJson('{', '}', DOUBLE_JSON_TEXT));

        // Ensure that control characters embedded within quotes are not considered
        assertEquals(JSON_CONTROL_CHARACTERS_IN_QUOTES, js.walkJson('{', '}', JSON_CONTROL_CHARACTERS_IN_QUOTES));

        assertNull(js.walkJson('{', '}', ""));
        assertNull(js.walkJson('{', '}', "{"));
        assertNull(js.walkJson('{', '}', "{hhhhhaajnnnn[][]"));
    }

    @Test
    public void testJsonIdentification() {
        // Simple and pure JSON object
        List<String> jsonSubstrings = js.getJsonCandidates(SINGLE_JSON);
        assertEquals(1, jsonSubstrings.size());
        assertEquals(SINGLE_JSON, jsonSubstrings.get(0));

        // Simple and pure JSON array
        jsonSubstrings = js.getJsonCandidates(SINGLE_JSON_ARRAY);
        assertEquals(1, jsonSubstrings.size());
        assertEquals(SINGLE_JSON_ARRAY, jsonSubstrings.get(0));

        // Single JSON Object with leading and trailing text
        jsonSubstrings = js.getJsonCandidates("<43> Jun 05 05:22:33 testhost " + SINGLE_JSON + "end");
        assertEquals(1, jsonSubstrings.size());
        assertEquals(SINGLE_JSON, jsonSubstrings.get(0));

        // Single JSON array with leading and trailing text
        jsonSubstrings = js.getJsonCandidates("<43> Jun 05 05:22:33 testhost " + SINGLE_JSON_ARRAY + "end");
        assertEquals(1, jsonSubstrings.size());
        assertEquals(SINGLE_JSON_ARRAY, jsonSubstrings.get(0));

        // Two separate JSON objects back-to-back with leading and trailing text
        jsonSubstrings = js.getJsonCandidates("<43> Jun 05 05:22:33 testhost " + SINGLE_JSON + SINGLE_JSON + "end");
        assertEquals(2, jsonSubstrings.size());
        assertEquals(SINGLE_JSON, jsonSubstrings.get(0));
        assertEquals(SINGLE_JSON, jsonSubstrings.get(1));

        // JSON object and Array back-to-back
        jsonSubstrings = js
                .getJsonCandidates("<43> Jun 05 05:22:33 testhost " + SINGLE_JSON + SINGLE_JSON_ARRAY + "end");
        assertEquals(2, jsonSubstrings.size());
        assertEquals(SINGLE_JSON, jsonSubstrings.get(0));
        assertEquals(SINGLE_JSON_ARRAY, jsonSubstrings.get(1));

        // Multiple JSON objects along with some invalid junk that looks like JSON
        jsonSubstrings = js.getJsonCandidates("<43> Jun 05 05:22:33 testhost processor[123]" + SINGLE_JSON
                + "{test}" + SINGLE_JSON_ARRAY + "end" + SINGLE_JSON);
        assertEquals(5, jsonSubstrings.size());
        assertEquals("[123]", jsonSubstrings.get(0));
        assertEquals(SINGLE_JSON, jsonSubstrings.get(1));
        assertEquals("{test}", jsonSubstrings.get(2));
        assertEquals(SINGLE_JSON_ARRAY, jsonSubstrings.get(3));
        assertEquals(SINGLE_JSON, jsonSubstrings.get(4));
    }

    @Test
    public void testMapAdditions() {
        Map<String, JsonElement> map = new HashMap<>();

        js.addToMap("key123", new JsonPrimitive("test123"), map);
        assertEquals("\"test123\"", map.get("key123").toString());

        // Add the same thing again, should be automatically re-keyed to fit in the map
        js.addToMap("key123", new JsonPrimitive("test123"), map);
        assertEquals("\"test123\"", map.get("1key123").toString());

        // Add the same thing again, should be automatically re-keyed to fit in the map
        js.addToMap("key123", new JsonPrimitive("test123"), map);
        assertEquals("\"test123\"", map.get("2key123").toString());
    }

    @Test
    public void testGetMembersAsMap() {
        // Test some random junk and stuff that vaguely looks like JSON. Should return
        // an empty map
        assertTrue(js.getAllMembersAsMap("").isEmpty());
        assertTrue(js.getAllMembersAsMap("hashdashdhsa").isEmpty());
        assertTrue(js.getAllMembersAsMap("{hfhghhg}").isEmpty());

        // This string looks invalid, but it's technically valid JSON
        Map<String, JsonElement> map = js.getAllMembersAsMap("[dsfsdf, asd]");
        assertEquals(3, map.size());
        assertEquals("[\"dsfsdf\",\"asd\"]", map.get("/[]").toString());
        assertEquals("dsfsdf", map.get("/[0]").getAsString());
        assertEquals("asd", map.get("/[1]").getAsString());

        // Test a single valid JSON event
        map = js.getAllMembersAsMap(SINGLE_JSON);
        assertEquals(12, map.size());

        // Add a JSON array to the end and validate that the size increases by two
        map = js.getAllMembersAsMap(SINGLE_JSON + "text [123]");
        assertEquals(14, map.size());

        // Add an invalid JSON object and validate that it doesn't increase the map size
        map = js.getAllMembersAsMap(SINGLE_JSON + "text {123}");
        assertEquals(12, map.size());

        // Duplicate the objects, we should be able to key all fields, so the map should
        // double in size
        map = js.getAllMembersAsMap(SINGLE_JSON + "text " + SINGLE_JSON);
        assertEquals(24, map.size());
        assertEquals("\"7d058e67620c\"", map.get("/\"hostIdentifier\"").toString());
        // Second occurrences should have indexes on their keys
        assertEquals("\"7d058e67620c\"", map.get("1/\"hostIdentifier\"").toString());

        // Try a similar test to the prior one with JSON arrays
        map = js.getAllMembersAsMap(SINGLE_JSON_ARRAY + "text " + SINGLE_JSON_ARRAY);
        assertEquals(26, map.size());
        assertEquals(SINGLE_JSON_ARRAY, map.get("/[]").toString());
        // Second occurrences should have indexes on their keys
        assertEquals(SINGLE_JSON_ARRAY, map.get("1/[]").toString());
        // Test an individual field as well
        assertEquals("\"7d058e67620c\"", map.get("/[0]/\"hostIdentifier\"").toString());
        // Second occurrences should have indexes on their keys
        assertEquals("\"7d058e67620c\"", map.get("1/[0]/\"hostIdentifier\"").toString());
    }

}