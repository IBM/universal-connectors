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
     * Quotes any BSON regex literal ({@code /pattern/flags}) found outside a JSON string so
     * the result can be parsed by a strict JSON parser such as Gson.
     *
     * <p>DocumentDB audit logs serialise MongoDB query args in MongoDB shell syntax, where
     * regex values are written as bare {@code /pattern/flags} instead of quoted strings.
     * A bare {@code /} has no meaning in JSON outside a string, so every unquoted {@code /}
     * that has a matching closing {@code /} on the same line is safely treated as a regex
     * literal. Slashes inside JSON string values (e.g. base64, URLs) are never touched
     * because the scanner tracks quoted-string boundaries.
     *
     * @param json raw DocumentDB audit message
     * @return input with all BSON regex literals wrapped in double quotes; original reference
     *         returned unchanged if the input contains no {@code /}
     */
    public static String sanitizeMongoBsonLiterals(String json) {
        if (json == null || json.isEmpty() || !json.contains("/")) {
            return json;
        }

        StringBuilder out = new StringBuilder(json.length() + 16);
        boolean inString = false;
        boolean escaped  = false;

        int i = 0;
        while (i < json.length()) {
            char c = json.charAt(i);

            if (inString) {
                out.append(c);
                if (escaped)       { escaped = false; }
                else if (c == '\\') { escaped = true;  }
                else if (c == '"')  { inString = false; }
                i++;
                continue;
            }

            if (c == '"') {
                inString = true;
                out.append(c);
                i++;
                continue;
            }

            // Any unquoted '/' is a BSON regex literal — find its closing '/'
            if (c == '/') {
                int closingSlash = findRegexClosingSlash(json, i + 1);
                int newline = json.indexOf('\n', i + 1);
                if (closingSlash != -1 && (newline == -1 || closingSlash < newline)) {
                    // Consume optional flags after the closing slash
                    int flagsEnd = closingSlash + 1;
                    while (flagsEnd < json.length()
                            && Character.isLetterOrDigit(json.charAt(flagsEnd))) {
                        flagsEnd++;
                    }
                    // Wrap in quotes, escaping '\' and '"' inside the pattern
                    out.append('"');
                    for (int j = i; j < flagsEnd; j++) {
                        char p = json.charAt(j);
                        if (p == '\\' || p == '"') out.append('\\');
                        out.append(p);
                    }
                    out.append('"');
                    i = flagsEnd;
                    continue;
                }
            }

            out.append(c);
            i++;
        }

        return out.toString();
    }

    /**
     * Returns the index of the closing {@code /} of a BSON regex literal, starting at
     * {@code from} (one past the opening {@code /}). Backslash-escaped characters are
     * skipped so an escaped {@code \/} inside the pattern is not treated as the delimiter.
     * Returns {@code -1} if the end of the line is reached before a closing {@code /}.
     */
    private static int findRegexClosingSlash(String json, int from) {
        int j = from;
        while (j < json.length()) {
            char c = json.charAt(j);
            if (c == '\n') return -1;
            if (c == '\\') { j += 2; continue; }
            if (c == '/') return j;
            j++;
        }
        return -1;
    }
}
