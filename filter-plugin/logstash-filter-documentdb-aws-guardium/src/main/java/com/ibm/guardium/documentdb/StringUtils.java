/*
Copyright 2022-2023 IBM Inc. All rights reserved
SPDX-License-Identifier: Apache-2.0
*/
package com.ibm.guardium.documentdb;

/**
 * Utility class for string manipulation operations in DocumentDB Guardium filter.
 * Provides efficient methods for common string operations.
 */
public final class StringUtils {
    
    // Prevent instantiation
    private StringUtils() {
        throw new AssertionError("StringUtils class should not be instantiated");
    }
    
    /**
     * Removes all whitespace from a string.
     * More efficient than regex replaceAll for simple character removal.
     * 
     * @param str The string to process
     * @return String with all whitespace removed, or original if null/empty
     */
    public static String removeWhitespace(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        
        StringBuilder sb = new StringBuilder(str.length());
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (!Character.isWhitespace(c)) {
                sb.append(c);
            }
        }
        return sb.toString();
    }
    
    /**
     * Extracts database name from namespace string (e.g., "db.collection" -> "db").
     * Uses indexOf instead of regex split for better performance.
     * 
     * @param ns The namespace string
     * @return The database name, or empty string if invalid
     */
    public static String extractDbNameFromNs(String ns) {
        if (ns == null || ns.isEmpty()) {
            return Constants.UNKNOWN_STRING;
        }
        int dotIndex = ns.indexOf('.');
        return dotIndex > 0 ? ns.substring(0, dotIndex) : ns;
    }
    
    /**
     * Extracts collection name from namespace string (e.g., "db.collection" -> "collection").
     * Uses indexOf instead of regex split for better performance.
     * 
     * @param ns The namespace string
     * @return The collection name, or original string if no dot found
     */
    public static String extractCollectionFromNs(String ns) {
        if (ns == null || ns.isEmpty()) {
            return ns;
        }
        int dotIndex = ns.indexOf('.');
        return dotIndex > 0 ? ns.substring(dotIndex + 1) : ns;
    }
    
    /**
     * Truncates a string to the specified maximum length and appends a suffix.
     * 
     * @param str The string to truncate
     * @param maxLength The maximum length
     * @param suffix The suffix to append (e.g., "... [truncated]")
     * @return The truncated string with suffix, or original if shorter than maxLength
     */
    public static String truncate(String str, int maxLength, String suffix) {
        if (str == null || str.length() <= maxLength) {
            return str;
        }
        return str.substring(0, maxLength) + suffix;
    }
    
    /**
     * Checks if a message contains any of the profiler keys.
     * More efficient than multiple contains() calls.
     * 
     * @param message The message to check
     * @return true if any profiler key is found, false otherwise
     */
    public static boolean containsAnyProfilerKey(String message) {
        if (message == null || message.isEmpty()) {
            return false;
        }
        for (String key : Constants.PROFILER_KEYS) {
            if (message.contains(key)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Replaces BSON regex literals (e.g. {@code /^foo/i}) with a JSON-safe quoted
     * representation (e.g. {@code "/^foo/i"}) so that the resulting string can be parsed by a
     * strict JSON parser such as Gson.
     *
     * <p>DocumentDB audit logs may contain BSON Extended JSON v1 regex literals of the form
     * {@code /pattern/flags} as values (e.g. {@code "field":{"$not":/^foo/}}). These are
     * valid in MongoDB shell syntax but not in strict JSON.
     *
     * <p>Uses a character-by-character state machine to track quoted-string boundaries so that a
     * {@code /} character inside a string value (e.g. inside a base64 {@code $binary} value) is
     * never mistaken for the start of a BSON regex literal.
     *
     * @param json The raw DocumentDB message string
     * @return The string with BSON regex literals quoted, or the original if no {@code /} present
     */
    public static String sanitizeMongoBsonLiterals(String json) {
        if (json == null || json.isEmpty() || !json.contains("/")) {
            return json;
        }

        StringBuilder out = new StringBuilder(json.length() + 16);
        boolean inString = false;  // currently inside a JSON quoted string
        boolean escaped  = false;  // previous char was an unescaped backslash
        // True when the last non-whitespace token outside a string was ':', ',' or '[' —
        // i.e. a position where a JSON value is expected next.
        boolean afterValueSeparator = false;

        int i = 0;
        while (i < json.length()) {
            char c = json.charAt(i);

            // ── inside a quoted string ──────────────────────────────────────────
            if (inString) {
                out.append(c);
                if (escaped) {
                    escaped = false;
                } else if (c == '\\') {
                    escaped = true;
                } else if (c == '"') {
                    inString = false;
                }
                i++;
                continue;
            }

            // ── outside a quoted string ─────────────────────────────────────────

            if (c == '"') {
                inString = true;
                afterValueSeparator = false;
                out.append(c);
                i++;
                continue;
            }

            if (c == ':' || c == ',' || c == '[') {
                afterValueSeparator = true;
                out.append(c);
                i++;
                continue;
            }

            // '{' opens an object — not a scalar-value position
            if (c == '{' || c == '}' || c == ']') {
                afterValueSeparator = false;
                out.append(c);
                i++;
                continue;
            }

            // Whitespace between a separator and the next value: preserve the flag
            if (Character.isWhitespace(c)) {
                out.append(c);
                i++;
                continue;
            }

            // ── BSON regex literal: /pattern/flags ──────────────────────────────
            // Only recognised immediately after ':', ',', or '[' (value-expected positions).
            // The closing '/' is found by scanning character-by-character, skipping over
            // escaped slashes (\/) inside the pattern. This handles:
            //   /^foo/                    simple pattern
            //   /.*\Qsome text\E.*/i      \Q...\E quotemeta, spaces, flags
            //   /^https?:\/\//i           escaped slashes inside pattern
            //   /\bword\b/                backslash sequences
            if (c == '/' && afterValueSeparator) {
                int closingSlash = findRegexClosingSlash(json, i + 1);
                int newline = json.indexOf('\n', i + 1);
                // Reject if no closing slash found, or a newline appears before it
                boolean validLiteral = closingSlash != -1
                        && (newline == -1 || closingSlash < newline);
                if (validLiteral) {
                    // Consume optional flags (letters/digits) after the closing slash
                    int flagsEnd = closingSlash + 1;
                    while (flagsEnd < json.length()
                            && Character.isLetterOrDigit(json.charAt(flagsEnd))) {
                        flagsEnd++;
                    }
                    // Emit the literal wrapped in double quotes.
                    // Escape characters that are invalid inside a JSON string:
                    //   \  ->  \\   (backslash)
                    //   "  ->  \"   (double quote inside pattern)
                    out.append('"');
                    for (int j = i; j < flagsEnd; j++) {
                        char p = json.charAt(j);
                        if (p == '\\' || p == '"') out.append('\\');
                        out.append(p);
                    }
                    out.append('"');
                    afterValueSeparator = false;
                    i = flagsEnd;
                    continue;
                }
            }

            // Any other non-whitespace character outside a string resets the separator flag
            afterValueSeparator = false;
            out.append(c);
            i++;
        }

        return out.toString();
    }

    /**
     * Finds the closing {@code /} of a BSON regex literal, starting the search at {@code from}.
     * Skips over backslash-escaped characters (e.g. {@code \/}) so that an escaped forward slash
     * inside the pattern is not mistaken for the closing delimiter.
     *
     * @param json The source string
     * @param from The index to start searching from (one past the opening {@code /})
     * @return The index of the closing {@code /}, or {@code -1} if not found before end-of-line
     */
    private static int findRegexClosingSlash(String json, int from) {
        int j = from;
        while (j < json.length()) {
            char c = json.charAt(j);
            if (c == '\n') return -1;   // regex literals do not span lines
            if (c == '\\') {
                j += 2;                 // skip the escaped character
                continue;
            }
            if (c == '/') return j;
            j++;
        }
        return -1;
    }
}
