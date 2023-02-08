/*
© Copyright IBM Corp. 2021, 2022, 2023 All rights reserved.

SPDX-License-Identifier: Apache-2.0
*/
package com.ibm.guardium.spanner;

import org.apache.commons.lang3.StringUtils;

public class ApplicationConstants {

	public static final String DATA_PROTOCOL = "Spanner";
	public static final String SERVER_TYPE = "SpannerDB";
	public static final String UNKOWN_STRING = StringUtils.EMPTY;

	public static final String PROTO_PAYLOAD = "protoPayload";

	public static final String REQUEST_METADATA = "requestMetadata";
	public static final String CALLER_IP = "callerIp";
	public static final String REQUEST = "request";
	public static final String DATABASE = "database";

	public static final String RESPONSE = "response";

	public static final String SERVICE_NAME = "serviceName";

	public static final String AUTHENTICATION_INFO = "authenticationInfo";
	public static final String AUTHORIZATION_INFO = "authorizationInfo";
	public static final String RESOURCE_ATTRIBUTES = "resourceAttributes";
	public static final String DATABASE_TYPE = "spanner.databases";

	public static final String PRINCIPAL_EMAIL = "principalEmail";

	public static final String TIMESTAMP = "timestamp";
	public static final String INSERTID = "insertId";

	public static final String CREATE_STATEMENT = "createStatement";
	public static final String STATEMENTS = "statements";
	public static final String SQL = "sql";

	public static final String NAME = "name";
	public static final String TYPE = "type";

	public static final String MESSAGE = "mes***REMOVED***ge";
	public static final String SPANNER_SERVICE = "spanner.googleapis.com";

	public static final String DEFAULT_IP = "0.0.0.0";

	public static final String COLLECTION = "collection";
	public static final String LABELS = "labels";
	public static final String INSTANCE_ID = "instance_id";
	public static final String PROJECT_ID = "project_id";

	public static final String RESOURCES = "resource";

	public static final String VERBS = "verbs";

	public static final String OBJECTS = "objects";

	public static final String PARSE = "parse";
	public static final String TABLE_LIST = "tableList";
	public static final String COLUMN_LIST = "columnList";
	public static final String AST = "ast";
	public static final String KEYWORD = "keyword";
	public static final String SELECT = "select";
	public static final String TYPE_BIGQUERY = "bigquery";

	public static final String UNPARSEABLE = "unparseable";
	public static final String CREATE = "create";
	public static final String DROP = "drop";
	public static final String ALTER = "alter";

	public static final String TABLE = "table";
	public static final String INDEX = "index";
	public static final String VIEW = "view";

	public static final String SUBSTITUTE_WITH_QUESTION_MARK = "?";
	public static final String SUBSTITUTE_WITH_INTERSECT = " INTERSECT";
	public static final String SUBSTITUTE_WITH_SPACE = " ";
	public static final String COLON = ":";
	public static final String UNDERSCORE = "_";
	public static final String DEFAULT_IPV6 = "0000:0000:0000:0000:0000:FFFF:0000:0000";
	public static final String DESCENDANTS = "descs";
	public static final String VERB = "verb";
	public static final String WITH = "WITH";


	private ApplicationConstants() {
	}
}
