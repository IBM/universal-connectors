/*
Copyright 2022-2023 IBM Inc. All rights reserved
SPDX-License-Identifier: Apache-2.0
*/
package com.ibm.guardium.documentdb;

import org.apache.commons.validator.routines.InetAddressValidator;

/**
 * Utility class for validation operations in DocumentDB Guardium filter.
 * Provides methods for JSON validation, IP address validation, and other common checks.
 */
public final class ValidationUtils {
    
    private static final InetAddressValidator INET_ADDRESS_VALIDATOR = InetAddressValidator.getInstance();
    
    // Prevent instantiation
    private ValidationUtils() {
        throw new AssertionError("ValidationUtils class should not be instantiated");
    }
    
    /**
     * Checks if a JSON string is properly closed with matching braces/brackets.
     * 
     * @param json The JSON string to validate
     * @return true if the JSON has matching opening and closing braces/brackets, false otherwise
     */
    public static boolean isProperlyClosedJson(String json) {
        if (json == null || json.isEmpty()) {
            return false;
        }
        
        String trimmed = json.trim();
        if (trimmed.isEmpty()) {
            return false;
        }
        
        char first = trimmed.charAt(0);
        char last = trimmed.charAt(trimmed.length() - 1);
        
        // Check if starts with { or [ and ends with matching } or ]
        return (first == '{' && last == '}') || (first == '[' && last == ']');
    }
    
    /**
     * Validates if a string is a valid IP address (IPv4 or IPv6).
     * 
     * @param ip The IP address string to validate
     * @return true if valid IP address, false otherwise
     */
    public static boolean isValidIpAddress(String ip) {
        return ip != null && INET_ADDRESS_VALIDATOR.isValid(ip);
    }
    
    /**
     * Checks if the IP address is a DocumentDB internal command IP.
     * This includes local IPs (127.0.0.1, ::1) and the special "(NONE)" marker.
     * 
     * @param ip The IP address to check
     * @return true if it's an internal/local IP, false otherwise
     */
    public static boolean isDocumentInternalCommandIp(String ip) {
        return ip != null && 
               (Constants.LOCAL_IP_LIST.contains(ip) || 
                ip.trim().equalsIgnoreCase(Constants.DOCUMENT_INTERNAL_API_IP));
    }
    
    /**
     * Checks if a string is null or empty.
     * 
     * @param str The string to check
     * @return true if null or empty, false otherwise
     */
    public static boolean isNullOrEmpty(String str) {
        return str == null || str.isEmpty();
    }
    
    /**
     * Returns the provided value if not null/empty, otherwise returns the default value.
     * 
     * @param value The value to check
     * @param defaultValue The default value to return if value is null/empty
     * @return The value or default value
     */
    public static String getValueOrDefault(String value, String defaultValue) {
        return (value != null && !value.isEmpty()) ? value : defaultValue;
    }
}
