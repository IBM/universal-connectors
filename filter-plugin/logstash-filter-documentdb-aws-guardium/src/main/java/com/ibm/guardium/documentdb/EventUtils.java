/*
Copyright 2022-2023 IBM Inc. All rights reserved
SPDX-License-Identifier: Apache-2.0
*/
package com.ibm.guardium.documentdb;

import co.elastic.logstash.api.Event;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

/**
 * Utility class for Event-related operations in DocumentDB Guardium filter.
 * Provides methods for event logging, field extraction, and manipulation.
 */
public final class EventUtils {
    
    private static final Logger log = LogManager.getLogger(EventUtils.class);
    
    // Reuse Gson instance for performance (only used in stdin case)
    private static final com.google.gson.Gson GSON_RECONSTRUCTOR = new com.google.gson.Gson();
    
    // Prevent instantiation
    private EventUtils() {
        throw new AssertionError("EventUtils class should not be instantiated");
    }
    
    /**
     * Creates a log string representation of an event (optimized with StringBuilder).
     * 
     * @param event The event to log
     * @return String representation of the event
     */
    public static String logEvent(Event event) {
        StringBuilder sb = new StringBuilder(Constants.STRING_BUILDER_INITIAL_CAPACITY);
        try {
            sb.append("{ ");
            boolean first = true;
            for (Map.Entry<String, Object> entry : event.getData().entrySet()) {
                if (!first) {
                    sb.append(", ");
                }
                sb.append('"')
                    .append(entry.getKey())
                    .append("\": \"")
                    .append(entry.getValue())
                    .append('"');
                first = false;
            }
            sb.append(" }");
            return sb.toString();
        } catch (Exception e) {
            log.error("DocumentDB filter: Failed to create event log string", e);
            return Constants.ERROR_FAILED_TO_SERIALIZE_EVENT;
        }
    }
    
    /**
     * Safely extracts a string field from an event.
     *
     * @param event The event
     * @param fieldName The field name to extract
     * @return The field value as string, or null if not found or fieldName is null
     */
    public static String getStringField(Event event, String fieldName) {
        if (fieldName == null) {
            return null;
        }
        Object field = event.getField(fieldName);
        return (field != null) ? field.toString() : null;
    }
    
    /**
     * Validates and extracts server IP from event.
     * 
     * @param event The event containing server_ip field
     * @return Valid IP address or null if invalid
     */
    public static String getValidatedEventServerIp(Event event) {
        String ip = getStringField(event, Constants.EVENT_FIELD_SERVER_IP);
        return ValidationUtils.isValidIpAddress(ip) ? ip : null;
    }
    
    /**
     * Extracts the message field from an event.
     * Handles two cases:
     * 1. CloudWatch logs: message field contains JSON string (fast path - 99% of cases)
     * 2. stdin with json codec: JSON already parsed into event fields (slow path - testing only)
     *
     * @param event The event
     * @return The message string, or reconstructed JSON if already parsed
     */
    public static String getMessageField(Event event) {
        // Fast path: CloudWatch logs with message field (99% of cases)
        String message = getStringField(event, Constants.EVENT_FIELD_MESSAGE);
        if (message != null) {
            return message;
        }
        
        // Slow path: stdin with json codec - only for testing
        // Check if JSON is already parsed by looking for DocumentDB audit signal
        Object atype = event.getField(Constants.FIELD_ATYPE);
        if (atype != null) {
            // JSON already parsed, reconstruct it manually to avoid Gson reflection issues
            try {
                return reconstructJsonFromEvent(event);
            } catch (Exception e) {
                return null;
            }
        }
        
        return null;
    }
    
    /**
     * Reconstructs JSON string from parsed event fields.
     * Only includes DocumentDB-relevant fields, avoiding Logstash internal fields.
     * This is only used for stdin testing, not production CloudWatch logs.
     *
     * @param event The event with parsed JSON fields
     * @return JSON string representation
     */
    private static String reconstructJsonFromEvent(Event event) {
        com.google.gson.JsonObject json = new com.google.gson.JsonObject();

        
        // Add all simple fields from the event, skipping Logstash internals
        for (Map.Entry<String, Object> entry : event.getData().entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            
            // Skip Logstash internal fields
            if (key.startsWith("@") || key.equals("host") || key.equals("event")) {
                continue;
            }
            
            try {

                // Convert value to JsonElement
                if (value == null) {
                    json.add(key, com.google.gson.JsonNull.INSTANCE);
                } else if (value instanceof String) {
                    json.addProperty(key, (String) value);
                } else if (value instanceof Number) {
                    json.addProperty(key, (Number) value);
                } else if (value instanceof Boolean) {
                    json.addProperty(key, (Boolean) value);
                } else if (value instanceof Map || value instanceof java.util.List) {
                    // For complex objects, try Gson first, fall back to string representation if it fails
                    try {
                        json.add(key, GSON_RECONSTRUCTOR.toJsonTree(value));
                    } catch (com.google.gson.JsonIOException jioEx) {
                        // Gson reflection failed (Java module restrictions)
                        // Convert to string and try to parse as JSON
                        try {
                            String valueStr = value.toString();
                            // Try to parse it as JSON
                            json.add(key, com.google.gson.JsonParser.parseString(valueStr));
                        } catch (Exception parseEx) {
                            // If parsing fails, just add as string
                            json.addProperty(key, value.toString());
                        }
                    }
                } else {
                    // For other types, convert to string
                    json.addProperty(key, value.toString());
                }
            } catch (Exception e) {
            }
        }
        
        String result = GSON_RECONSTRUCTOR.toJson(json);
        return result;
    }
    
    /**
     * Extracts the server hostname prefix from an event.
     * 
     * @param event The event
     * @return The server hostname prefix, or null if not found
     */
    public static String getServerHostnamePrefix(Event event) {
        return getStringField(event, Constants.EVENT_FIELD_SERVER_HOSTNAME_PREFIX);
    }
}
