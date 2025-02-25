package com.ibm.guardium.universalconnector.commons.custom_parsing.parsers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * Engine for parsing LEEF formatted events into a Map structure. <br>
 * <br>
 * LEEF events take this form:
 *
 * <pre>
 *       LEEF:LEEFVersion|Vendor|Product|Version|EventID|fieldOne=valueOne\tfieldTwo=valueTwo\t....
 * </pre>
 *
 * There are currently two supported LEEF format versions:
 *
 * <pre>
 * V1    LEEF:1.0|Microsoft|MSExchange|4.0 SP1|15345|src=192.0.2.0\tdst=172.50.123.1\tsev=4\t....
 * V2    LEEF:2.0|Lancope|StealthWatch|1.0|41|^|src=192.0.2.0^dst=172.50.123.1^sev=5^....
 * </pre>
 *
 * These formats are quite similar, but the salient difference is that V2 allows
 * an extra 'header'
 * field that actually supplies a custom attibute delimiter. Note that this
 * custom-delimiter
 * parameter is optional, and not at all required for V2. In the second example
 * above you can see
 * that the attributes are separated by a carat (^), this works because the
 * 'fifth' header field
 * specifies a carat. This instructs the parser to separate attributes based on
 * this custom
 * delimiter, instead of statically using a tab like V1 does. If the
 * custom-delimiter header field
 * is omitted, the library will just use the default attribute delimiter of tab
 * (\t) <br>
 * <br>
 * Additionally, LEEF V2 can also take in the hex representation of a custom
 * delimiter character,
 * eg:
 *
 * <pre>
 * V2    LEEF:2.0|Lancope|StealthWatch|1.0|41|xa6|src=192.0.2.0¦dst=172.50.123.1¦sev=5¦...
 * </pre>
 *
 * In the above sample, 'xa6' is the hex of a segmented-bar (¦). If a custom
 * delimiter is specified
 * that cannot be converted into a Character directly, the engine will fallback
 * to using the default
 * delimiter (tab) <br>
 * <br>
 * The returned map has an intuitive keying strategy for all 'attribute' fields,
 * ie it just uses
 * whatever the name of the attribute was in the event-text. However, the LEEF
 * format defines some
 * 'header' fields that do not have intrinsic names within the format. The
 * keying names for the
 * header fields are as follows:
 *
 * <pre>
 *     $leefversion$ -&gt; LEEFVersion
 *     $vendor$      -&gt; Vendor
 *     $product$     -&gt; Product
 *     $version$     -&gt; Version
 *     $eventid$     -&gt; EventID
 * </pre>
 */
public class LeefParser implements IParser {
    private static Logger logger = LogManager.getLogger(LeefParser.class);
    private static final String HEADER_START_MARKER = "LEEF:";
    private static final char LEEF_DEFAULT_HEADER_DELIM = '|';
    private static final char LEEF_DEFAULT_BODY_DELIM = '\t';
    private static final String LEEF_V2 = "2.0";

    Map<String, String> extractedProperties;

    @Override
    public String parse(String payload, String key) {
        if (key == null)
            return null;
        return extractedProperties.get(key);
    }

    @Override
    public boolean isPayloadValid(String payload) {
        if (payload == null)
            return false;

        extractedProperties = parsePayload(payload);
        return extractedProperties != null && !extractedProperties.isEmpty();
    }

    /**
     * Creates and returns a Map containing all fields from within the LEEF event
     * structure in the
     * supplied payload String. <br>
     * <br>
     * The returned map has an intuitive keying strategy for all 'attribute' fields,
     * ie it just uses
     * whatever the name of the attribute was in the event-text. However, the LEEF
     * format defines some
     * 'header' fields that do not have intrinsic names within the format. The
     * keying names for the
     * header fields are as follows:
     *
     * <pre>
     *     $leefversion$ -&gt; LEEFVersion
     *     $vendor$      -&gt; Vendor
     *     $product$     -&gt; Product
     *     $version$     -&gt; Version
     *     $eventid$     -&gt; EventID
     * </pre>
     *
     * @param payload The structured event payload
     * @return A Map containing String values of all 'properties' within the event
     */
    public Map<String, String> parsePayload(String payload) {
        Map<String, String> map = new HashMap<>();
        Index i = new Index();
        // Look for the LEEF header, if it isn't present, then the payload we were
        // handed is not LEEF
        int headerIndex = payload.indexOf(HEADER_START_MARKER);
        if (headerIndex == -1) {
            // Prefer to return an empty Map as opposed to null
            return map;
        }

        // Establish our index to begin walking the 'LEEF' part of the String
        i.setIndex(headerIndex + HEADER_START_MARKER.length());

        String leefVersion = this.extractHeaderFields(i, payload, map);
        char bodyDelimiter = LEEF_DEFAULT_BODY_DELIM;
        if (leefVersion.equals(LEEF_V2)) {
            // LEEFV2 allows the message to serialize a custom delimiter
            bodyDelimiter = extractCustomDelimiter(i, payload);
        }

        this.extractBodyFields(bodyDelimiter, i, payload, map);

        return map;
    }

    /**
     * The LEEF format has a few 'header' fields that are not key/value. These need
     * to be handled
     * specially and key'd specially, and that is done within this method. <br>
     * <br>
     * Additionally, this method returns the LEEFVersion attribute from the header
     * as some later
     * parsing decisions depend on which version of the LEEF standard we are dealing
     * with.
     *
     * @param i       An Index representing where the read is currently set in the
     *                supplied payload
     * @param payload The LEEF String
     * @param map     The Map that is accumulating fields from this LEEF event
     * @return The 'version' of LEEF that this event corresponds to
     */
    private String extractHeaderFields(Index i, String payload, Map<String, String> map) {
        String leefVersion = getNextToken(LEEF_DEFAULT_HEADER_DELIM, i, payload);
        map.put("$leefversion$", leefVersion);
        map.put("$vendor$", getNextToken(LEEF_DEFAULT_HEADER_DELIM, i, payload));
        map.put("$product$", getNextToken(LEEF_DEFAULT_HEADER_DELIM, i, payload));
        map.put("$version$", getNextToken(LEEF_DEFAULT_HEADER_DELIM, i, payload));
        map.put("$eventid$", getNextToken(LEEF_DEFAULT_HEADER_DELIM, i, payload));
        return leefVersion;
    }

    /**
     * Extract the name/value pairs of attributes that form the LEEF event's body.
     *
     * @param bodyDelimiter The delimiter that separates one attribute from another
     * @param i             An Index representing where the read is currently set in
     *                      the supplied payload
     * @param payload       The LEEF String
     * @param map           The Map that is accumulating fields from this LEEF event
     */
    private void extractBodyFields(
            char bodyDelimiter, Index i, String payload, Map<String, String> map) {
        while (i.getIndex() < payload.length()) {
            String key = getNextToken('=', i, payload);
            String value = getNextToken(bodyDelimiter, i, payload);
            if (!value.isEmpty() && !value.trim().equals("") && !map.containsKey(key)) {
                map.put(key.trim(), value.trim());
            }
        }
    }

    /**
     * V2 of the LEEF standard allows the payload to supply a custom attribute
     * delimiter. This
     * function attempts to extract that delimiter. The custom delimiter is
     * optional, so it being
     * absent is also possible. <br>
     * <br>
     * The custom delimiter must be either:
     *
     * <pre>
     *     - A single character
     *     - A hex representation of a character (eg ax6 -&gt; ¦)
     * </pre>
     *
     * If the custom-delimiter cannot be interpreted considering the above
     * constraints, it just
     * defaults back to a tab (\t)
     *
     * @param i       An Index representing where the read is currently set in the
     *                supplied payload
     * @param payload The LEEF String
     * @return The character to be used to separate attributes within the LEEF
     *         String's body.
     */
    private char extractCustomDelimiter(Index i, String payload) {
        int tempCurrentPostion = i.getIndex();
        String delimiterCharacter = getNextToken(LEEF_DEFAULT_HEADER_DELIM, i, payload);

        // Check to see if the attribute delimiter is pipe
        if (delimiterCharacter.length() == 0) {
            delimiterCharacter = getNextToken(LEEF_DEFAULT_HEADER_DELIM, i, payload);
            if (delimiterCharacter.length() == 0) {
                delimiterCharacter = String.valueOf(LEEF_DEFAULT_HEADER_DELIM);
            } else {
                // If delimiter wasn't the empty string again, it means that the delimiter field
                // is present in the header, but there's literally nothing in it, eg:
                // LEEF:2.0|Microsoft|MSExchange|4.0 SP1|15345||
                // In this case we just need to increment the temporary position to get the
                // reader past the closing '|' and into the attributes
                tempCurrentPostion++;
            }
        }

        // Check to make sure we have something for the delimiter character since it is
        // an optional
        // header attribute.
        // If we have something for the delimiter character field (even if it is empty)
        // we shouldn't
        // reach the end of the payload.
        if (i.getIndex() != payload.length()) {
            if (delimiterCharacter.length() == 1) {
                return delimiterCharacter.charAt(0);
            }
            delimiterCharacter = delimiterCharacter.toLowerCase();
            if (delimiterCharacter.startsWith("x") || delimiterCharacter.startsWith("0x")) {
                return convertToChar(delimiterCharacter.substring(delimiterCharacter.indexOf('x') + 1));
            }
        } else {
            i.setIndex(tempCurrentPostion);
        }

        return LEEF_DEFAULT_BODY_DELIM;
    }

    /**
     * Returns the substring from the supplied String that is delimited by the
     * supplied delimiter.
     *
     * @param nextDelimiter The character to search for to bound the returned value
     * @param i             An Index representing where the read is currently set in
     *                      the supplied payload
     * @param payload       The String payload to extract substrings from
     * @return A substring of the supplied String that is delimited by the next
     *         occurrence of the
     *         supplied delimiter
     */
    private String getNextToken(char nextDelimiter, Index i, String payload) {
        int startIndex = i.getIndex();
        while (i.getIndex() < payload.length()) {
            char currentChar = payload.charAt(i.getIndex());
            i.setIndex(i.getIndex() + 1);
            if (currentChar == nextDelimiter) {
                break;
            }
        }
        int endIndex = i.getIndex() - 1;
        if (i.getIndex() == payload.length() && payload.charAt(payload.length() - 1) != nextDelimiter) {
            endIndex = i.getIndex();
            return payload.substring(startIndex, endIndex).trim();
        }

        return payload.substring(startIndex, endIndex);
    }

    /**
     * Simple object for passing around the current index in the payload. Without
     * something like this
     * the algorithm in this class is difficult to make thread safe.
     */
    private class Index {
        int current = 0;

        void setIndex(int index) {
            this.current = index;
        }

        int getIndex() {
            return this.current;
        }
    }

    /**
     * Converts the supplied String into its char equivalent. If the supplied String
     * cannot be
     * converted into a char, then LEEF_DEFAULT_BODY_DELIM '\t' is returned.
     *
     * @param delimiterCharacter The String to turn into a char
     * @return The char representation if it had one, '\t' otherwise
     */
    private char convertToChar(String delimiterCharacter) {
        char a = LEEF_DEFAULT_BODY_DELIM;
        try {
            if (delimiterCharacter.length() > 2) {
                int lsb = Integer.parseInt(delimiterCharacter.substring(delimiterCharacter.length() - 2), 16);
                int msb = Integer.parseInt(delimiterCharacter.substring(0, delimiterCharacter.length() - 2), 16);

                a = new String(new byte[] { (byte) msb, (byte) lsb }).charAt(0);
            } else {
                a = (char) Integer.parseInt(delimiterCharacter, 16);
            }
        } catch (Exception e) {
            logger.debug("Unable to determine delimiter character from string '{}'", delimiterCharacter);
        }

        return a;
    }
}
