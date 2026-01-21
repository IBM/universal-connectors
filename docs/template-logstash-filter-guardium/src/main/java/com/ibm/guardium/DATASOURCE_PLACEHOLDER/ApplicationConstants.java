/*
Copyright IBM Corp. 2024 All rights reserved.
SPDX-License-Identifier: Apache-2.0
*/
package com.ibm.guardium.DATASOURCE_PLACEHOLDER;

/**
 * ApplicationConstants class contains all constant values used throughout the plugin.
 * 
 * INSTRUCTIONS:
 * 1. Replace DATASOURCE_PLACEHOLDER with your data source name
 * 2. Update field name constants to match your audit log structure
 * 3. Add any additional constants specific to your data source
 * 4. Update default values as needed
 */
public class ApplicationConstants {
	
	// ========== Field Names ==========
	// TODO: Update these constants to match your audit log field names
	
	/**
	 * Message field name in Logstash event
	 */
	public static final String MESSAGE = "message";
	
	/**
	 * Timestamp field name in audit log
	 * Common alternatives: "time", "eventTime", "@timestamp", "timestamp"
	 */
	public static final String TIMESTAMP = "timestamp";
	
	/**
	 * User field name in audit log
	 * Common alternatives: "user", "username", "dbUser", "principal", "userId"
	 */
	public static final String USER = "user";
	
	/**
	 * Database name field in audit log
	 * Common alternatives: "database", "db", "schema", "databaseName"
	 */
	public static final String DATABASE = "database";
	
	/**
	 * Table/Collection name field in audit log
	 * Common alternatives: "table", "collection", "tableName", "object"
	 */
	public static final String TABLE = "table";
	
	/**
	 * Operation/Action field in audit log
	 * Common alternatives: "operation", "action", "command", "verb", "operationType"
	 */
	public static final String OPERATION = "operation";
	
	/**
	 * Query/Statement field in audit log
	 * Common alternatives: "query", "statement", "sql", "command", "queryText"
	 */
	public static final String QUERY = "query";
	
	/**
	 * Client IP address field in audit log
	 * Common alternatives: "clientIp", "client_ip", "sourceIp", "remoteAddress"
	 */
	public static final String CLIENT_IP = "clientIp";
	
	/**
	 * Server hostname field in audit log
	 * Common alternatives: "serverHost", "server", "hostname", "host"
	 */
	public static final String SERVER_HOST = "serverHost";
	
	/**
	 * Status code field in audit log
	 * Common alternatives: "statusCode", "status", "resultCode", "returnCode"
	 */
	public static final String STATUS_CODE = "statusCode";
	
	/**
	 * Error message field in audit log
	 * Common alternatives: "errorMessage", "error", "message", "errorDescription"
	 */
	public static final String ERROR_MESSAGE = "errorMessage";
	
	/**
	 * Session ID field in audit log
	 * Common alternatives: "sessionId", "session", "connectionId", "activityId"
	 */
	public static final String SESSION_ID = "sessionId";
	
	// ========== Default Values ==========
	
	/**
	 * Default IPv4 address when not available
	 */
	public static final String DEFAULT_IP = "0.0.0.0";
	
	/**
	 * Default IPv6 address when not available
	 */
	public static final String DEFAULT_IPV6 = "0000:0000:0000:0000:0000:FFFF:0000:0000";

	/**
	 * Default PORT when not available
	 */
	public static final String DEFAULT_IP = "0";

	/**
	 * Unknown string value
	 */
	public static final String UNKNOWN_STRING = "";
	
	/**
	 * Not available string value
	 */
	public static final String NOT_AVAILABLE = "N.A.";
	
	// ========== Server and Protocol Information ==========
	
	/**
	 * Server type identifier
	 * TODO: Update with your data source name (e.g., "MongoDB", "PostgreSQL", "MySQL")
	 */
	public static final String SERVER_TYPE = "[DATASOURCE_NAME]";
	
	/**
	 * Data protocol identifier
	 * TODO: Update with your data source protocol (e.g., "MongoDB", "PostgreSQL", "MySQL")
	 */
	public static final String DATA_PROTOCOL = "[DATASOURCE_NAME]";
	
	// ========== Object Types ==========
	
	/**
	 * Collection/Table object type
	 */
	public static final String COLLECTION = "collection";
	
	/**
	 * Database object type
	 */
	public static final String DATABASE_TYPE = "database";
	
	/**
	 * Schema object type
	 */
	public static final String SCHEMA = "schema";
	
	/**
	 * Table object type
	 */
	public static final String TABLE_TYPE = "table";
	
	// ========== Exception Types ==========
	
	/**
	 * Authorization exception type
	 */
	public static final String EXCEPTION_TYPE_AUTHORIZATION_STRING = "SQL_ERROR";
	
	/**
	 * Authentication exception type
	 */
	public static final String EXCEPTION_TYPE_AUTHENTICATION_STRING = "LOGIN_FAILED";
	
	/**
	 * SQL error exception type
	 */
	public static final String SQL_ERROR = "SQL_ERROR";
	
	// ========== Logstash Tags ==========
	
	/**
	 * Tag for JSON parsing errors
	 */
	public static final String LOGSTASH_TAG_JSON_PARSE_ERROR = "DATASOURCE_PLACEHOLDER_json_parse_error";
	
	/**
	 * Tag for events that are not from this data source
	 */
	public static final String LOGSTASH_TAG_SKIP_NOT_DATASOURCE = "DATASOURCE_PLACEHOLDER_skip_not_datasource";
	
	/**
	 * Tag for invalid events
	 */
	public static final String LOGSTASH_TAG_INVALID_EVENT = "DATASOURCE_PLACEHOLDER_invalid_event";
	
	// ========== Additional Constants ==========
	// TODO: Add any additional constants specific to your data source
	
	/**
	 * Example: Minimum supported version
	 */
	// public static final String MIN_VERSION = "1.0.0";
	
	/**
	 * Example: Maximum log size
	 */
	// public static final int MAX_LOG_SIZE = 10000;
	
	/**
	 * Example: Specific operation types
	 */
	// public static final String OPERATION_SELECT = "SELECT";
	// public static final String OPERATION_INSERT = "INSERT";
	// public static final String OPERATION_UPDATE = "UPDATE";
	// public static final String OPERATION_DELETE = "DELETE";
	
	/**
	 * Example: Log categories
	 */
	// public static final String CATEGORY_DATA_ACCESS = "DATA_ACCESS";
	// public static final String CATEGORY_ADMIN = "ADMIN";
	// public static final String CATEGORY_DDL = "DDL";
	
	/**
	 * Private constructor to prevent instantiation
	 */
	private ApplicationConstants() {
		throw new IllegalStateException("Constants class");
	}
}

