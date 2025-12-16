/*
#Copyright 2020-2021 IBM Inc. All rights reserved
#SPDX-License-Identifier: Apache-2.0
#*/
package com.ibm.guardium.s3sqspostgresql;

public interface Constants {

	public static final String CONNECTION_FROM =  "connection_from";

	public static final String APP_USER_NAME = "AWSService";

	public static final String TIMESTAMP = "timestamp";

	public static final String STATEMENT = "statement";

	public static final String CLIENT_IP = "client_ip";

	public static final String CLIENT_PORT = "port";

	public static final String SUCCEEDED = "e_level";

	public static final String DATABASE_NAME = "database_name";

	public static final String PARSED_MESSAGE = "parsed_message";

	public static final String USER_NAME = "user_name";

	public static final String DB_USER = "db_user";

	public static final String SESSION_ID = "session_id";

	public static final String FULL_SQL_QUERY = "full_sql_query";

	public static final String ERROR_MESSAGE = "error_message";

	public static final String SQL_STATE_CODE = "sql_state_code";

	public static final String SQL_STATE_CODE_SUCCESS = "00000";

	public static final String DEFAULT_IP = "0.0.0.0";

	public static final int DEFAULT_PORT = -1;

	public static final String UNKNOWN_STRING = "";

	public static final String SERVER_TYPE_STRING = "POSTGRESQL";

	public static final String DATA_PROTOCOL_STRING = "POSTGRESQL";

	public static final String LANGUAGE = "PGRS";

	public static final String SQL_ERROR = "SQL_ERROR";

	public static final String LOGIN_FAILED = "LOGIN_FAILED";

	public static final String COMM_PROTOCOL = "AWSApiCall";

	public static final String MESSAGE = "message";

	public static final String APPLICATION_NAME = "application_name";
	
	public static final String NA = "N.A.";
	
	public static final String RECORDS = "records";

	public static final String PREFIX = "pre_fix";

	public static final String ACCOUNT_ID = "account_id";

	public static final String ERROR_SEVERITY = "error_severity";

	public static final String ERROR = "ERROR";

	public static final String FATAL = "FATAL";

	public static final String QUERY = "query";

	public static final String LOG_GROUP = "logGroup";

	public static final String DB_NAME = "db_name";

	public static final String LOG_LEVEL = "log_level";

	public static final String SERVER_HOST_NAME = "server_hostname";

	public static final String INSTANCE_NAME = "instance_name";

	public static final String DURATION = "duration";
}