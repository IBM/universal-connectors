//
// Copyright 2021-2024 IBM Inc. All rights reserved
// SPDX-License-Identifier: Apache2.0
//
package com.ibm.guardium.dynamodb;

public interface Constants {
	
	public static final String NOT_AVAILABLE = "N.A.";
	public static final String DATA_PROTOCOL_STRING = "AMAZON DYNAMODB";
	public static final String UNKNOWN_STRING = "";
	public static final String SERVER_TYPE_STRING = "DYNAMODB";
    public static final String LOGSTASH_TAG_JSON_PARSE_ERROR = "_dynamoguardium_json_parse_error";

	
	public static final String REQUEST_ID = "requestID";
	public static final String ERROR_MESSAGE = "errorMessage";
	public static final String ERROR_CODE = "errorCode";
	public static final String USER_IDENTITY = "userIdentity";
	public static final String USERNAME = "userName";
	public static final String SESSION_CONTEXT= "sessionContext";
	public static final String SESSION_ISSUER = "sessionIssuer";
	public static final String USER_AGENT = "userAgent";
	public static final String SOURCE_IP_ADDRESS = "sourceIPAddress";
	public static final String TABLE_NAME = "tableName";
	public static final String TABLE_ARN = "tableArn";
	public static final String EVENT_NAME = "eventName";
	public static final String EVENT_TIME = "eventTime";
	public static final String REQUEST_PARAMETERS = "requestParameters";
	public static final String OBJECT_TYPE = "table"; //for dynamodb
	public static final String PRINCIPAL_ID = "principalId";
	public static final String ACCOUNT_ID = "accountId";
	public static final String ACCESS_KEY_ID = "accessKeyId";
	public static final String EVENT_ID = "eventId";
	public static final String EVENT_SOURCE = "eventSource";
	public static final String ARN = "arn";
	public static final String RESOURCES = "resources";
	public static final String KEY = "key";
	public static final String CONDITION_EXPRESSION = "conditionExpression";

	//constants
	public static final int CLIENT_PORT = -1;
	public static final String SERVER_IP = "0.0.0.0";
	public static final int SERVER_PORT = -1;
	public static final String LOGSTASH_TAG_SKIP_NOT_DYNAMO = "_dynamoguardium_skip_not_dynamodb";
	public static final String MASK_STRING = "?";
	public static final String SERVER_HOSTNAME = "dynamodb.amazonaws.com";
	public static final String SQL_ERROR = "SQL_ERROR";

	public static final String LOGIN_ERROR = "LOGIN_FAILED";

	public static final String ERROR_CODE_ACCESS_DENIED = "AccessDenied";

	
}