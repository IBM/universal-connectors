/*
#Copyright 2020-2021 IBM Inc. All rights reserved
#SPDX-License-Identifier: Apache-2.0
#*/
package com.ibm.guardium.dsql;

public interface Constants {

	public static final String TYPE = "type";

	public static final String CLASS = "class";

	public static final String CLIENT_APPLICATION = "clientApplication";

	public static final String DATABASE_NAME = "databaseName";

	public static final String DB_PROTOCOL = "dbProtocol";

	public static final String DB_USER_NAME = "dbUserName";

	public static final String ERROR_MESSAGE = "errorMessage";

	public static final String EXIT_CODE = "exitCode";

	public static final String REMOTE_HOST = "remoteHost";

	public static final String REMOTE_PORT = "remotePort";

	public static final String SERVER_HOST = "serverHost";

	public static final String SESSION_ID = "sessionId";

	public static final String START_TIME = "startTime";

	public static final String STATEMENT_TEXT = "statementText";

	public static final String COMMAND_TAG = "commandTag";

	public static final String PARAMETER_LIST = "parameterList";

	public static final String LOG_TIME = "logTime";

	public static final String ENGINE_NATIVE_AUDIT_FIELDS = "engineNativeAuditFields";

	public static final String DEFAULT_IP = "0.0.0.0";

	public static final int DEFAULT_PORT = -1;

	public static final String UNKNOWN_STRING = "";

	public static final String SERVER_TYPE_STRING = "POSTGRESQL";

	public static final String DATA_PROTOCOL_STRING = "POSTGRESQL";

	public static final String LANGUAGE = "PGRS";

	public static final String SQL_ERROR = "SQL_ERROR";

	public static final String LOGIN_FAILED = "LOGIN_FAILED";

	public static final String COMM_PROTOCOL = "AWSApiCall";

	public static final String APP_USER_NAME = "AWSService";

	public static final String NA = "N.A.";

	public static final String ACCOUNT_ID = "account_id";

	public static final String INSTANCE_NAME = "instance_name";

	public static final String SERVER_HOST_NAME = "server_hostname";

	public static final String MESSAGE = "message";

	public static final String RECORDS = "Records";

	public static final String BODY = "body";

	public static final String LOG_EVENTS = "logEvents";

	public static final String EXIT_CODE_SUCCESS = "0";

	// Constants for nested DatabaseActivityMonitoringRecord format
	public static final String DATABASE_ACTIVITY_EVENT_LIST = "databaseActivityEventList";

	public static final String COMMAND_TEXT = "commandText";

	public static final String COMMAND = "command";

	public static final String CLUSTER_ID = "clusterId";

	public static final String INSTANCE_ID = "instanceId";

	public static final String DB_ACTIVITY_MONITORING_RECORD = "DatabaseActivityMonitoringRecord";

}