/*
Copyright 2022-2023 IBM Inc. All rights reserved
SPDX-License-Identifier: Apache-2.0
*/
package com.ibm.guardium.documentdb;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Central repository for all constants used in DocumentDB Guardium filter plugin.
 * This class consolidates magic strings, numbers, and configuration values to improve
 * maintainability and reduce duplication.
 */
public final class Constants {
    
    // Prevent instantiation
    private Constants() {
        throw new AssertionError("Constants class should not be instantiated");
    }
    
    // Protocol and Server Type
    public static final String DATA_PROTOCOL = "DocumentDB";
    public static final String SERVER_TYPE = "DocumentDB";
    
    // Default Values
    public static final String UNKNOWN_STRING = "";
    public static final String NOT_AVAILABLE = "N.A.";
    public static final String DEFAULT_IP = "0.0.0.0";
    public static final String DEFAULT_IPV6 = "0000:0000:0000:0000:0000:FFFF:0000:0000";
    public static final String COMPOUND_OBJECT = "[json-object]";
    public static final String DOCUMENT_INTERNAL_API_IP = "(NONE)";
    
    // Exception Types
    public static final String EXCEPTION_TYPE_AUTHORIZATION = "SQL_ERROR";
    public static final String EXCEPTION_TYPE_AUTHENTICATION = "LOGIN_FAILED";
    public static final String UC_PARSER_ERROR = "UC_PARSER_ERROR";
    public static final String UC_AUDIT_ERROR = "UC_AUDIT_ERROR";
    
    // Logstash Tags
    public static final String LOGSTASH_TAG_JSON_PARSE_ERROR = "_documentdbguardium_json_parse_error";
    public static final String LOGSTASH_TAG_JSON_DEPTH_ERROR = "_documentdbguardium_json_depth_error";
    public static final String LOGSTASH_TAG_SKIP = "_documentdbguardium_skip";
    
    // DocumentDB Event Signals
    public static final String DOCUMENTDB_AUDIT_SIGNAL = "\"atype\"";
    public static final String DOCUMENTDB_PROFILER_SIGNAL = "command";
    
    // DocumentDB Operation Keys
    public static final String AGGR_KEY = "aggregate";
    public static final String COUNT_KEY = "count";
    public static final String DELETE_KEY = "remove";
    public static final String INSERT_KEY = "insert";
    public static final String UPDATE_KEY = "update";
    public static final String DISTINCT_KEY = "distinct";
    public static final String FIND_KEY = "find";
    public static final String FINDANDMODIFY_KEY = "findAndModify";
    
    // Profiler Keys Set for efficient checking (immutable)
    public static final Set<String> PROFILER_KEYS = Collections.unmodifiableSet(new HashSet<>(
        Arrays.asList(
            AGGR_KEY,
            COUNT_KEY,
            DELETE_KEY,
            INSERT_KEY,
            UPDATE_KEY,
            DISTINCT_KEY,
            FIND_KEY,
            FINDANDMODIFY_KEY
        )
    ));
    
    // Local IP addresses (immutable)
    public static final Set<String> LOCAL_IP_LIST = Collections.unmodifiableSet(new HashSet<>(
        Arrays.asList("127.0.0.1", "0:0:0:0:0:0:0:1")
    ));
    
    // Redaction ignore strings - arguments that won't be redacted (immutable)
    public static final Set<String> REDACTION_IGNORE_STRINGS = Collections.unmodifiableSet(new HashSet<>(
        Arrays.asList("from", "localField", "foreignField", "as", "connectFromField", "connectToField")
    ));
    
    // JSON Field Names
    public static final String FIELD_ATYPE = "atype";
    public static final String FIELD_PARAM = "param";
    public static final String FIELD_NS = "ns";
    public static final String FIELD_USER = "user";
    public static final String FIELD_ERROR = "error";
    public static final String FIELD_MESSAGE = "message";
    public static final String FIELD_COMMAND = "command";
    public static final String FIELD_TS = "ts";
    public static final String FIELD_CLIENT = "client";
    public static final String FIELD_REMOTE_IP = "remote_ip";
    public static final String FIELD_APP_NAME = "appName";
    public static final String FIELD_OP = "op";
    
    // Event Field Names
    public static final String EVENT_FIELD_MESSAGE = "message";
    public static final String EVENT_FIELD_SERVER_IP = "server_ip";
    public static final String EVENT_FIELD_SERVER_HOSTNAME_PREFIX = "serverHostnamePrefix";
    
    // Authentication Types
    public static final String AUTH_TYPE_AUTHENTICATE = "authenticate";
    public static final String AUTH_TYPE_AUTHCHECK = "authCheck";
    public static final String AUTH_TYPE_CREATE_ROLE = "createRole";
    public static final String AUTH_TYPE_DROP_ROLE = "dropRole";
    
    // Error Codes
    public static final String ERROR_CODE_SUCCESS = "0";
    public static final String ERROR_CODE_UNAUTHORIZED = "13";
    public static final String ERROR_CODE_AUTH_FAILED = "18";
    
    // Server Configuration
    public static final String SERVER_HOSTNAME_SUFFIX = ".aws.com";
    public static final String DEFAULT_SERVER_HOSTNAME = "documentdb.amazonaws.com";
    
    // Limits and Thresholds
    public static final int MAX_SQL_STRING_LENGTH = 10000;
    public static final int STRING_BUILDER_INITIAL_CAPACITY = 256;
    
    // Error Messages
    public static final String ERROR_JSON_VALIDATION_FAILED = "DocumentDB filter: JSON validation failed (truncated or too large)";
    public static final String ERROR_INVALID_AUTHENTICATE_LOG = "DocumentDB filter: Invalid authenticate log";
    public static final String ERROR_JSON_NESTING_TOO_DEEP = "DocumentDB filter: JSON nesting too deep (StackOverflow), skipping event";
    public static final String ERROR_INSUFFICIENT_MEMORY = "DocumentDB filter: Insufficient memory to process event, skipping";
    public static final String ERROR_PARSING_AUDIT_EVENT = "DocumentDB filter: Error parsing docDb audit event";
    public static final String ERROR_PARSING_PROFILER_EVENT = "DocumentDB filter: Error parsing docDb profiler event";
    public static final String ERROR_MISSING_DB_NAME = "DocumentDB filter: Missing DB name";
    public static final String ERROR_FAILED_TO_SERIALIZE_EVENT = "{ error: failed to serialize event }";
}
