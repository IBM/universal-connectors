/*

Copyright IBM Corp. 2021, 2022 All rights reserved.
SPDX-License-Identifier: Apache-2.0

*/
package com.ibm.guardium.firestore;

import org.apache.commons.lang3.StringUtils;

public class ApplicationConstants {

	public static final String DATA_PROTOCOL_STRING = "Firestore(GCP)";
	public static final String SERVER_TYPE_STRING = "Firestore";
	public static final String UNKOWN_STRING = StringUtils.EMPTY;
	public static final String COMPOUND_OBJECT_STRING = "[json-object]";

	public static final String EVENT_MESSAGE = "message";
	public static final String PROTO_PAYLOAD = "protoPayload";

	public static final String REQUEST_METADATA = "requestMetadata";
	public static final String CALLER_IP = "callerIp";

	public static final String SERVICE_NAME = "serviceName";

	public static final String AUTHENTICATION_INFO = "authenticationInfo";
	public static final String PRINCIPAL_EMAIL = "principalEmail";

	public static final String SEVERITY = "severity";
	public static final String TIMESTAMP = "timestamp";
	public static final String INSERTID = "insertId";

	public static final String ERROR = "error";
	public static final String CODE = "code";
	public static final String MESSAGE = "message";

	public static final String STATUS = "status";

	public static final String HTTP_REQUEST = "httpRequest";
	public static final String SERVER_IP = "serverIp";
	public static final String RESOURCE_NAME = "resourceName";

	
	public static final CharSequence FIRE_STORE_SERVICE = "firestore.googleapis.com";
	public static final String REQUEST = "request";
	public static final String ADD_TARGET = "addTarget";
	public static final String QUERY = "query";
	public static final String STRUCTURED_QUERY = "structuredQuery";
	public static final String DOCUMENTS = "documents";
	public static final String WRITES = "writes";
	public static final String MASK = "mask";
	public static final String SERVER_HOST_NAME_STRING = "firestore.googleapis.com";
	public static final String OPERATION = "operation";
	public static final String PRODUCER = "producer";
	public static final String TYPE = "@type";
	public static final String COLLECTIONS = "collection";
	public static final String RESOURCE = "resource";
	public static final String LABELS = "labels";
	public static final String PROJECT_ID = "project_id";
	public static final String CALLER_SUPPLIED_USER_AGENT = "callerSuppliedUserAgent";
	public static final String FROM = "from";
	public static final String COLLECTIONID = "collectionId";
	public static final String UPDATE = "update";
	public static final String NAME = "name";
	public static final String EXCEPTION_TYPE_STRING = "SQL_ERROR";
	public static final String REDACTED_KEY = "value";
	public static final String REDACTED_MASK = "?";
	public static final String DELETE = "delete";
	public static final String DEFAULT_IPV6 = "0000:0000:0000:0000:0000:FFFF:0000:0000";
	public static final String DEFAULT_IPV4 = "0.0.0.0";
	public static final String INDEX = "index";
	public static final String FIELD = "field";
	

	private ApplicationConstants() {
	}
}
