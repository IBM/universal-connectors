/*
Copyright IBM Corp. 2021, 2024 All rights reserved.
SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.guardium.cockroachdb;

public class Constants {
    static final String COCKROACHDB = "cockroachdb";
    static final String INVALID_MSG = "EVENT_IS_INVALID";
    static final String SERVER_TYPE = "COCKROACHDB";
    static final String DB_PROTOCOL = "COCKROACHDB";
    static final String UNKNOWN_STRING = "";
    static final String NOT_AVAILABLE = "N.A.";
    static final String DEFAULT_IP = "0.0.0.0";
    static final String DEFAULT_IPV6 = "0000:0000:0000:0000:0000:FFFF:0000:0000";
    static final int DEFAULT_PORT = -1;
    static final String LANGUAGE_COCKROACHDB = "COCKROACH";

    // Event types
    static final String CLIENT_AUTH_FAILED = "client_authentication_failed";
    static final String LOGIN_FAILED = "LoginFailed";
    static final String ERROR_TEXT = "ErrorText";
    static final String SQLSTATE = "SQLSTATE";
    static final String EXCEPTION_TYPE_SQL_ERROR_STRING = "SQL_ERROR";
    static final String EXCEPTION_TYPE_LOGIN_FAILED_STRING = "LOGIN_FAILED";

    // CockroachDB JSON field names
    static final String TIMESTAMP = "Timestamp";
    static final String USER = "User";
    static final String STATEMENT = "Statement";
    static final String APPLICATION_NAME = "ApplicationName";
    static final String DATABASE_NAME = "DatabaseName";
    static final String CLIENT_IP = "ClientIP";
    static final String CLIENT_PORT = "ClientPort";
    static final String SERVER_HOSTNAME = "ServerHostname";
    static final String EVENT_TYPE = "EventType";
    static final String REASON = "Reason";
    static final String TABLE_NAME = "TableName";
}
