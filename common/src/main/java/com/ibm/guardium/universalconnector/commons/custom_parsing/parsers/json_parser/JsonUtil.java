package com.ibm.guardium.universalconnector.commons.custom_parsing.parsers.json_parser;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Engine for parsing abstract JSON Strings into a Map of keys/values. <br>
 * <br>
 * Capable of handling multiple distinct JSON objects/arrays within a single
 * String. <br>
 * <br>
 * The keying strategy is as follows: <br>
 * - /"object1"/"object2"/"property" -- A forward slash is at the root of the
 * object, all referenced
 * fields/objects used to reach the desired property are also delimited by
 * forward slashes, and all
 * fieldnames are encapsulated in quotes <br>
 * - /"object1"/"array"[] -- Keys the entire JSONArray named 'array'<br>
 * - /"object1"/"array"[0] -- Keys the first index of the JSONArray named
 * 'array'<br>
 * - /"object1"/"array"[0]/"property1" -- Keys a field called 'property1' which
 * is within the
 * JSONObject at the first index of the JSONArray named 'array'<br>
 * <br>
 * Because this algorithm allows for multiple distinct JSON objects/arrays to be
 * decomposed from a
 * single payload, it also needs a re-keying strategy to deal with instances
 * where two distinct JSON
 * objects in a single event have overlapping fields. Eg consider this sample
 * event payload: <br>
 *
 * <pre>
 *  Aug 3 05:15:22 testhost {"field1":"value1", "field2":"value2"} text123 {"field1":"value3", "field2":"value4"}
 * </pre>
 *
 * This sample contains two duplicate JSON objects. In order to facilitate
 * parsing of all possible
 * information, if a key already exists in the 'map' we're using to accumulate
 * fields, it will
 * numerically prefix the 'duplicate' such that it can be uniquely key'd in the
 * map. <br>
 * <br>
 * In our example, this would result in the following keys being available in
 * the resulting map:<br>
 *
 * <pre>
 * /"field1"
 * /"field2"
 * 1/"field1"
 * 1/"field2"
 * </pre>
 *
 * This has no set upper bound, so the numerical prefix could grow depending on
 * how many duplicate
 * fields there are in the supplied String.
 */
public class JsonUtil {
    private static final Logger logger = LogManager.getLogger(JsonUtil.class);

    public JsonUtil() {
        // Clients should use the JsonParser to access this class' functionality
    }

    /**
     *
     *
     * <pre>
     * Creates a Map containing the JSON values. The supplied String doesn't need to exclusively
     * be JSON. This function will attempt to isolate the JSON object within the String and operate
     * on it. If there are multiple distinct JSON objects within the supplied String, only one
     * will be parsed. Generally it will be the first occurring JSON object within the String.
     *
     * The keying strategy for the Map is just the full path of the relevant property, eg:
     *  - /"object1"/"object2"/"property"
     *
     * Also JSON arrays are supported by returning a single value for the entire array itself, and
     * allows indexing to extract a specific value from a JSON array:
     *  - /"object1"/"array1"[]
     *  - /"object1"/"array1"[0]
     * </pre>
     *
     * @param json A String containing JSON structure (does not have to exclusively
     *             be JSON, this
     *             function will attempt to find JSON within a String)
     * @return A Map of the the fields within the JSON
     */
    public Map<String, String> getMap(String json) {
        Map<String, String> jsonMap = new HashMap<>();
        Map<String, JsonElement> jsonElementMap = getAllMembersAsMap(json);

        for (Map.Entry<String, JsonElement> elementEntry : jsonElementMap.entrySet()) {
            String keypath = elementEntry.getKey();
            JsonElement element = elementEntry.getValue();

            if (element.isJsonArray()) {
                jsonMap.put(keypath, element.toString());
            } else if (element.isJsonNull()) {
                jsonMap.put(keypath, "null");
            } else if (element.isJsonPrimitive()) {
                jsonMap.put(keypath, element.getAsString());
            }
        }
        return jsonMap;
    }

    /**
     *
     *
     * <pre>
     * Returns a map representing the members of the supplied json String. The map
     * is String to JsonElement. This allows the caller to decide how they want handle
     * each member of the map, JsonElement gives methods to check the type and handle
     * type conversions.
     * </pre>
     *
     * @param jsonString jsonString The jsonString
     * @return A map of String to JsonElement
     */
    public Map<String, JsonElement> getAllMembersAsMap(String jsonString) {
        Map<String, JsonElement> members = new LinkedHashMap<>();
        List<String> jsonCandidates = this.getJsonCandidates(jsonString);
        for (String jsonCandidate : jsonCandidates) {
            JsonElement element;
            try {
                element = new JsonParser().parse(jsonCandidate);
            } catch (JsonParseException e) {
                // It's possible that we've been given a snippet that was determined as a
                // candidate
                // for being valid JSON, but actually just appeared to be JSON. This should
                // happen
                // infrequently
                logger.debug("Supplied JSON snippet is not valid: {}", jsonCandidate, e);
                continue;
            }

            // The root element really needs to be either a map or an array to be valid json
            // since forward slash is enough to indicate the absolute starting point,
            // so below we only need to provide an empty string (or a forward slash for
            // array)
            // as the parent path.
            if (element.isJsonObject()) {
                this.parseMap(element.getAsJsonObject(), "", members);
            } else if (element.isJsonArray()) {
                this.parseList(element.getAsJsonArray(), "/", members);
            }
        }
        return members;
    }

    /**
     *
     *
     * <pre>
     * Recursively search the tree, finding keys and values. The name of a key is
     * denoted by the search-tree path to the key.
     *
     * For example, a key named 'keyName' that is nested under two objects named
     * 'object1' and 'object 2' would have the final name of '/object1/object2/keyName'.
     *
     * The goal here is to get the absolute name of each attribute in the json structure,
     * relative names can be duplicated in nested objects, so we want the full path
     * </pre>
     *
     * @param map  The map (really a tree structure) to recurse through
     * @param root The name of the root object in this
     */
    private void parseMap(JsonObject obj, String root, Map<String, JsonElement> map) {
        for (Map.Entry<String, JsonElement> entry : obj.entrySet()) {
            String name = root + "/\"" + entry.getKey() + "\"";
            JsonElement element = entry.getValue();

            if (element.isJsonObject()) {
                this.parseMap(element.getAsJsonObject(), name, map);
            } else if (element.isJsonArray()) {
                this.parseList(element.getAsJsonArray(), name, map);
            } else {
                addToMap(name, element, map);
            }
        }
    }

    /**
     *
     *
     * <pre>
     * Iterates through a list finding keys and values. The name of a key is
     * denoted by the search-tree path to the key. Recurses when finding
     * another list, or a map
     *
     * For example, a key named 'keyName' that is nested under two objects named
     * 'object1' and 'object 2' would have the final name of '/object1/object2/keyName'.
     *
     * The goal here is to get the absolute name of each attribute in the json structure,
     * relative names can be duplicated in nested objects, so we want the full path
     * </pre>
     *
     * @param list The list to iterate through
     * @param root The name of the root object in this
     */
    private void parseList(JsonArray list, String root, Map<String, JsonElement> map) {
        addToMap(root + "[]", list, map);
        int i = 0;
        for (JsonElement element : list) {
            // We need some way of identifying each member of the list, easiest to just tag
            // them with their index within the list
            String name = root + "[" + i + "]";

            if (element.isJsonObject()) {
                this.parseMap(element.getAsJsonObject(), name, map);
            } else if (element.isJsonArray()) {
                this.parseList(element.getAsJsonArray(), name, map);
            } else {
                addToMap(name, element, map);
            }

            i++;
        }
    }

    /**
     * Adds to the accumulating map of JSON fields. Allows duplicate keys to be
     * added to the map by
     * rekeying them with numerical prefixes. <br>
     * <br>
     * For example, if key /"key123" already exists in the map, and another addition
     * is attempted to
     * be made with key "key123", this function will rekey it as 1/"key123". A
     * subsequent addition of
     * /"key123" would be rekey'd as 2/"key123". <br>
     * <br>
     * This should be something of an edge case, as this should functionally only
     * happen when a JSON
     * String we're given contains multiple distinct JSON objects/arrays within it,
     * and the keys
     * within those distinct models have some clash. Generally we'll be getting a
     * single JSON object
     * within an event and this backup strategy that rekey's entries.
     *
     * @param key     The key to add to the Map
     * @param element The Element to be mapped to the Key
     * @param map     The Map itself
     */
    public void addToMap(String key, JsonElement element, Map<String, JsonElement> map) {
        if (map.containsKey(key)) {
            // If the map already has this key, it means that a prior JSON object/array in
            // the
            // originally submitted String has already been processed, and there are
            // duplicate
            // keys between these two (or more) JSON structures. In order to not drop the
            // 'duplicates'
            // on the floor, we can numerically index them and then add them to the map
            int i = 1;
            String newKey = i + key;
            while (map.containsKey(newKey)) {
                i++;
                newKey = i + key;
            }

            // Rename our key with the first indexed value that didn't appear in the map
            key = newKey;
        }

        map.put(key, element);
    }

    private static final char OPEN_CURLY = '{';
    private static final char CLOSE_CURLY = '}';
    private static final char OPEN_SQUARE = '[';
    private static final char CLOSE_SQUARE = ']';

    /**
     * Scans the supplied 'json' String looking for distinct instances of
     * valid-looking JSON. <br>
     * <br>
     * This function is capable of extracting multiple unrelated JSON objects/arrays
     * from text. This
     * is not a perfect process and the substrings returned by this method are not
     * guaranteed to be
     * valid JSON. <br>
     * <br>
     * The real purpose of this function is to very quickly identify what
     * subsections of the supplied
     * event string will be consumable as JSON for property extraction. If this
     * function occasionally
     * returns some invalid JSON substring (eg '{value}'), that's the cost of doing
     * this quickly in
     * general and not spending tons of time trying to exhaustively validate the
     * strings we're
     * returning.
     *
     * @param json A String that likely contains one or more JSON objects/arrays
     *             within it.
     * @return A List of Strings that appear to be JSON snippets from the supplied
     *         'json'
     */
    public List<String> getJsonCandidates(String json) {
        List<String> results = new ArrayList<>();

        int curly = json.indexOf(OPEN_CURLY);
        int square = json.indexOf(OPEN_SQUARE);
        while (curly >= 0 || square >= 0) {
            String jsonSub;
            int offset;

            // The first occurrence of either is what's important, if we found a '{' first,
            // we'll
            // walk the payload looking for when the corresponding '}' is found, and
            // vice-versa for '[]'
            if (curly >= 0 && ((curly < square) || square < 0)) {
                jsonSub = this.walkJson(OPEN_CURLY, CLOSE_CURLY, json.substring(curly));
                offset = curly;
            } else {
                jsonSub = this.walkJson(OPEN_SQUARE, CLOSE_SQUARE, json.substring(square));
                offset = square;
            }

            if (jsonSub != null) {
                results.add(jsonSub);

                // Reset for the next iteration of the loop
                curly = json.indexOf(OPEN_CURLY, offset + jsonSub.length());
                square = json.indexOf(OPEN_SQUARE, offset + jsonSub.length());
            } else {
                // If the jsonSub was null, it means the last call to walkJson didn't find any
                // way
                // to return a json sub-string. This would have walked the entire payload, and
                // likely
                // means that the String we were handed doesn't contain valid JSON
                break;
            }
        }

        return results;
    }

    /**
     * Attempts to walk a subsection of the supplied 'json' String attempting to
     * find the bounding
     * text that represents a JSON object of some kind. <br>
     * <br>
     * This function should be called with a 'json' String that begins with the
     * corresponding
     * 'incrementor' character. If the String is not in this state, the function
     * behaves incorrectly.
     * <br>
     * <br>
     * The algorithm here is just to increment/decrement a counter as we encounter
     * corresponding
     * control structure characters ('{}' or '[]') such that when our counter hits
     * zero, we know the
     * control structure is well balanced, and therefore we have a good chance at
     * having captured a
     * valid JSON substring. <br>
     * <br>
     * This is not a 100% test that the substring identified is valid JSON, it is a
     * quick test to
     * isolate well structured JSON objects within an encompassing String. If the
     * 'json' is
     * sufficiently malformed this function will not perform 'correctly', and the
     * substrings returned
     * may not be valid. <br>
     * <br>
     * In this context these edge cases are fine, because the returned JSON
     * substrings will actually
     * be processed upstream and if they are not totally valid exceptions will be
     * thrown to
     * communicate the problem.
     *
     * @param incrementor The character to use to increment our counter
     * @param decrementor The character to use to decrement our counter
     * @param json        A String potentially containing a JSON substring beginning
     *                    with the 'incrementor'
     *                    character
     * @return A substring of the supplied String that appears to be a valid JSON
     *         object. Returns null
     *         in some circumstances where the function is certain it did not detect
     *         any valid JSON
     */
    public String walkJson(char incrementor, char decrementor, String json) {
        int balance = 0;
        int quoted = 0;
        int subStringLength = 0;
        for (char c : json.toCharArray()) {
            /*
             * We avoid 'counting' control characters if they are within quotes, anything
             * within quotes
             * in JSON is fair game and should be ignored as key/field text and not control
             * structure
             */
            if (c == incrementor && (quoted % 2 == 0)) {
                balance++;
            } else if (c == decrementor && (quoted % 2 == 0)) {
                balance--;
            } else if (c == '"') {
                quoted++;
            }

            subStringLength++;

            if (balance == 0) {
                return json.substring(0, subStringLength);
            }
        }

        // If we get to here then the JSON control characters never balanced out and
        // there
        // definitely is not a JSON substring fitting the description of the parameters
        // handed to us
        return null;
    }
}
