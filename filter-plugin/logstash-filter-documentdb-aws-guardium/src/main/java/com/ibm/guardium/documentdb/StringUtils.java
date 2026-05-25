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
}
