//
// Copyright 2021-2023 IBM Inc. All rights reserved
// SPDX-License-Identifier: Apache2.0
//

package com.ibm.guardium.snowflakedb.utils;

import org.apache.commons.lang3.StringUtils;

public class Constants {
    public static final String NOT_AVAILABLE = "NA";

    public static final String DEFAULT_IP = "0.0.0.0";

    public static final String UNKNOWN_STRING = StringUtils.EMPTY;

    public static final String LANGUAGE_SNOWFLAKE= "SNOWFLAKE";

    public static final String DB_PROTOCOL = "SNOWFLAKE";
    public static final String SERVER_TYPE = "SNOWFLAKE";

    public static final Integer SERVER_PORT = 443;

    public static final String DATABASE_NAME = "database_name";
    public static final String QUERY_ID = "query_id";
    public static final String SESSION_ID = "session_id";

    public static final String QUERY_TIMESTAMP= "query_timestamp";

    public static final String CLIENT_IP= "client_ip";

    public static final String SERVER_IP= "server_ip";

    public static final String USER_NAME= "user_name";

    public static final String CLIENT_ENVIRONMENT= "client_environment";

    public static final String QUERY_TEXT= "query_text";
    public static final String CLIENT_APPLICATION_ID= "client_application_id";

    public static final String WAREHOUSE_NAME= "warehouse_name";

    public static final String QUERY_ERROR_CODE= "query_error_code";

    public static final String QUERY_ERROR_MESSAGE= "query_error_message";

    public static final String QUERY_EXECUTION_STATUS= "query_execution_status";

    public static final String LOGIN_SUCCESS= "login_success";

    public static final String LOGIN_ERROR_CODE= "login_error_code";

    public static final String LOGIN_ERROR_MESSAGE= "login_error_message";

    public static final String LOGIN_TIMESTAMP= "login_timestamp";

    public static final String SERVER_HOST_NAME="server_host_name";
    public static final String EVENT_TYPE="event_type";

    public static final String CLIENT_OS = "OS";

    public static final String CLIENT_OS_USER = "user";

    public static final String CLIENT_OS_VERSION = "OS_VERSION";

    public static final String SUCCESS = "SUCCESS";
    public static final String SQL_ERROR = "SQL_ERROR";
    public static final String  LOGIN_FAILED = "LOGIN_FAILED";

    public static final String TEXT = "TEXT";

    public static final String LOGSTASH_TAG_SKIP_NOT_SNOWFLAKE = "_not_snowflake_or_malformed";
    public static final String LOGSTASH_TAG_JSON_PARSE_ERROR = "_snowflakeguardium_json_parse_error";


}
