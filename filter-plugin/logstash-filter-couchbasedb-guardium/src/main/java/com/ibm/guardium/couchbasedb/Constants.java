//
// Copyright 2020- IBM Inc. All rights reserved
// SPDX-License-Identifier: Apache2.0
//

package com.ibm.guardium.couchbasedb;

public interface Constants {
	
	public static final String LOGSTASH_TAG_SKIP_NOT_COUCHBASE_DB = "_couchbaseguardium_skip_not_couchbasedb";
	public static final String LOGSTASH_TAG_SKIP = "_couchbaseguardium_skip";
	public static final String LOGSTASH_TAG_JSON_PARSE_ERROR = "_couchbaseguardium_json_parse_error";
	public static final String MESSAGE = "message";
	public static final String SERVER_IP = "serverIP";
	public static final String SERVER_HOSTNAME = "serverHostname";
	public static final String NOT_AVAILABLE = "N.A.";
	public static final String DEFAULT_IP = "0.0.0.0";
	public static final String LOOPBACK_ADDRESS = "127.0.0.1";
	public static final int DEFAULT_SERVER_PORT = -1;
	public static final int DEFAULT_CLIENT_PORT = -1;
	public static final String UNKNOWN_STRING = "";
	public static final String SERVER_TYPE_STRING = "COUCHBASE";
	public static final String DATA_PROTOCOL_STRING = "COUCHBASE";
	public static final String REQUEST_ID = "requestId";
	public static final String CLIENT_CONTEXT_ID = "clientContextId";
	public static final String INTERNAL="INTERNAL-";
	public static final String SESSION_ID = "sessionid";
	public static final String DESCRIPTION = "description";
	public static final String SUCCESS = "success";
	public static final String N1QL = "N1QL";
	public static final String ERROR_MESSAGE = "errorMessage";
	public static final String STATUS = "status";
	public static final String FATAL = "fatal";
	public static final String ERRORS = "errors";
	public static final String TEXT = "TEXT";
	public static final String CONSTRUCT = "CONSTRUCT";
	public static final String ID = "id";
	public static final String REAL_USERID = "real_userid";
	public static final String USER = "user";
	public static final String USER_AGENT = "userAgent";
	public static final String REMOTE = "remote";
	public static final String NODE = "node";
	public static final String IP = "ip";
	public static final String PORT = "port";
	public static final String STATEMENT = "statement";
	public static final String TIMESTAMP = "timestamp";
	public static final String MIN_OFF = "Offset";
	public static final String OBJECT_TYPE = "bucket";
	public static final String NAME = "name";
	public static final String BUCKET = "bucket_name";
	public static final String PREPENDED_STRING="__CB POST /#statement=";
	public static final String COUCHB_LANGUAGE="COUCHB";
	public static final String FREE_TEXT="FREE_TEXT";
	public static final String HTTP_METHOD="httpMethod";
	public static final String SQL_ERROR="SQL_ERROR";
	public static final String LOGIN_FAILED="LOGIN_FAILED";
	public static final String GROUP_NAME = "group_name";
	public static final String FULL_NAME = "full_name";
	public static final String IDENTITY = "identity";

	public static final String BUCKET_NAME = "bucket_name";
	public static final String ROLES = "roles";
	public static final String SCOPE_NAME = "scope_name";
	public static final String DOC_ID = "doc_id";
	public static final String LOGIN_SUCCESS = "login success";
	public static final String LOGOUT_SUCCESS = "logout success";
	public static final String SESSION_TIMEOUT = "session timeout";
}