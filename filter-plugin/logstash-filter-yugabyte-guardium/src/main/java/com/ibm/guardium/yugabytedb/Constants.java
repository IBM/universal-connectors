//
// Copyright 2021-2022 IBM Inc. All rights reserved
// SPDX-License-Identifier: Apache2.0
//

package com.ibm.guardium.yugabytedb;

import org.apache.commons.lang3.StringUtils;

public class Constants {

	public static final String NOT_AVAILABLE= "NA";
	public static final String UNKNOWN_STRING = StringUtils.EMPTY;
	public static final String LOGSTASH_TAG_JSON_PARSE_ERROR = "_yugabyteguardium_json_parse_error";

	// Input parameters
	public static final String TIMESTAMP = "timestamp";
	public static final String CLIENT_IP = "client_ip";
	public static final String CLIENT_PORT = "client_port";
	public static final String PROCESS_ID = "process_id";
	public static final String APPLICATION_NAME = "application_name";
	public static final String USERNAME = "username";
	public static final String DB_NAME = "db_name";
	public static final String SESSION_ID = "session_id";
	public static final String TRANSACTION_ID = "transaction_id";
	public static final String AUDIT_TYPE = "audit_type";
	public static final String STATEMENT_ID = "statement_id";
	public static final String SUB_STATEMENT_ID = "sub_statement_id";
	public static final String STATEMENT_CLASS = "statement_class";
	public static final String COMMAND = "command";
	public static final String QUERY = "query";
	public static final String PARAMETERS = "parameters";
	public static final String EVENT_TYPE = "event_type";

	public static final String EVENT_CATEGORY = "event_category";
	public static final String SERVER_HOST = "serverHost";
	public static final String SERVER_OS = "serverOS";
	public static final String SERVER_IP = "serverIP";
	public static final String SQL_ERROR = "SQL_ERROR";

	public static final String LOGIN_FAILED = "LOGIN_FAILED";

	public static final String ERROR_DESCRIPTION = "error_description";

	public static final String SERVER_TYPE_PG = "POSTGRE";

	public static final String SERVER_TYPE_CASSANDRA = "CASSANDRA";

	public static final String LANGUAGE_PG = "PGRS";

	public static final String LANGUAGE_CASSANDRA = "CASS";

	public static final String DB_PROTOCOL_PG = "POSTGRESQL";

	public static final String DB_PROTOCOL_CASSANDRA = "CASSANDRA";
	public static final String MESSAGE = "message";

	public static final String TYPE = "log_type";

	public static final String SQL = "sql";
	public static final String AUTH_ERROR = "AUTH_ERROR";
	public static final String[] UNKNOWN_AS_VALUE = {"UNKNOWN", "[UNKNOWN]"};
	public static final String CATEGORY_AUTH = "AUTH";
	public static final String LOGIN_ERROR = "LOGIN_ERROR";
	public static final String CATEGORY_ERROR = "ERROR";
	public static final String POSTGRES = "postgres";
	public static final String CASSANDRA = "cassandra";

}
