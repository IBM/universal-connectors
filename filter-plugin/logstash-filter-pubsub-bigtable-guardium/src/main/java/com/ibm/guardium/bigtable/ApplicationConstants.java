/*
Copyright IBM Corp. 2021, 2024 All rights reserved.
SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.guardium.bigtable;

import org.apache.commons.lang3.StringUtils;

public class ApplicationConstants {
    public static final String LOGSTASH_TAG_SKIP_NOT_GCP = "_GCPguardium_skip_not_GCP";
    public static final String LOGSTASH_TAG_JSON_PARSE_ERROR = "_bigtableguardium_json_parse_error";
    public static final String LOGSTASH_TAG_INVALID_BIGTABLE = "_bigtableguardium_invalid_event";
    public static final String DATA_PROTOCOL_STRING = "BigTable(GCP)";
    public static final String SERVER_TYPE_STRING = "BigTable";
    public static final String UNKOWN_STRING = StringUtils.EMPTY;
    public static final String DEFAULT_IPV6 = "0000:0000:0000:0000:0000:FFFF:0000:0000";
    public static final String BIGTABLE_API = "bigtable.googleapis.com";
    public static final String BIGTABLE_ADMIN_API = "bigtableadmin.googleapis.com";
    public static final String PROTO_PAYLOAD = "protoPayload";
    public static final String REQUEST_METADATA = "requestMetadata";
    public static final String CALLER_IP = "callerIp";
    public static final String SERVICE_NAME = "serviceName";
    public static final String REQUEST = "request";
    public static final String INSERT_ID = "insertId";
    public static final String RESPONSE = "response";
    public static final String AUTHENTICATION_INFO = "authenticationInfo";
    public static final String AUTHORIZATION_INFO = "authorizationInfo";
    public static final String RESOURCE_ATTRIBUTES = "resourceAttributes";
    public static final String PERMISSION = "permission";
    public static final String TYPE = "type";
    public static final String AT_TYPE = "@type";
    public static final String NAME = "name";
    public static final String PRINCIPAL_EMAIL = "principalEmail";
    public static final String SEVERITY = "severity";
    public static final String TIMESTAMP = "timestamp";
    public static final String ERROR = "error";
    public static final String SYNTAX_ERROR = "Syntax";
    public static final String CRITICAL = "critical";
    public static final String CODE = "code";
    public static final String MESSAGE = "message";
    public static final String METHOD_NAME = "methodName";
    public static final String RESOURCE = "resource";
    public static final String STATUS = "status";
    public static final String DEFAULT_IP = "0.0.0.0";
    public static final String TABLE = "table";
    public static final String INSTANCE = "instance";
    public static final String PROJECT = "project";
    public static final String PROJECT_ID = "project_id";
    public static final String INSTANCE_ID = "instance_id";
    public static final String TABLE_ID = "table_id";
    public static final String LABELS = "labels";
    public static final String DOT = ".";
    public static final String SLASH = "/";
    public static final String SEMICOLON = "; ";
    public static final String COLON = ":";
    public static final String UNDERLINE = "_";
    public static final String ON = " ON ";
    public static final String STATUS_CODE = "Status Code: ";
    public static final String DESCRIPTION = ", Description: ";
    public static final String PROJECTS = "projects";
    public static final String INSTANCES = "instances";
    public static final String TABLES = "tables";
    public static final String CONNECTION_ID_KEY = "connectionid";
    public static final String USERNAME_KEY = "username";
    public static final String DATABASE_KEY = "database";
    public static final String RETCODE_KEY = "retcode";
    public static final String HOSTNAME_KEY = "hostname";
    public static final String SERVERHOST_KEY = "serverhost";
    public static final String OPERATION_KEY = "operation";
    public static final String RESOURCE_NAME = "resourceName";
    public static final String EXECUTE_QUERY = "executeQuery";
    public static final String UNKNOWN_STRING = "";

    // status codes
    public static final String STATUS_OK = "OK";
    public static final String STATUS_CANCELLED = "CANCELLED";
    public static final String STATUS_UNKNOWN = "UNKNOWN";
    public static final String STATUS_INVALID_ARGUMENT = "INVALID_ARGUMENT";
    public static final String STATUS_DEADLINE_EXCEEDED = "DEADLINE_EXCEEDED";
    public static final String STATUS_NOT_FOUND = "NOT_FOUND";
    public static final String STATUS_ALREADY_EXISTS = "ALREADY_EXISTS";
    public static final String STATUS_PERMISSION_DENIED = "PERMISSION_DENIED";
    public static final String STATUS_RESOURCE_EXHAUSTED = "RESOURCE_EXHAUSTED";
    public static final String STATUS_FAILED_PRECONDITION = "FAILED PRECONDITION";
    public static final String STATUS_ABORTED = "ABORTED";
    public static final String STATUS_OUT_OF_RANGE = "OUT_OF_RANGE";
    public static final String STATUS_UNIMPLEMENTED = "UNIMPLEMENTED";
    public static final String STATUS_INTERNAL = "INTERNAL";
    public static final String STATUS_UNAVAILABLE = "UNAVAILABLE";
    public static final String STATUS_RESOURCE_NOT_FOUND = "RESOURCE_NOT_FOUND";
    public static final String STATUS_INTERNAL_SERVER_ERROR = "INTERNAL_SERVER_ERROR";
    public static final String STATUS_NOT_AUTHORIZED = "NOT_AUTHORIZED";
    public static final String STATUS_RESOURCE_ALREADY_EXIST = "RESOURCE_ALREADY_EXIST";
    public static final String STATUS_RATE_LIMIT_EXCEEDED = "RATE_LIMIT_EXCEEDED";
    public static final String STATUS_GENERAL_EXCEPTION = "GENERAL_EXCEPTION";
    public static final String SQL_ERROR = "SQL_ERROR";

    private ApplicationConstants() {
    }
}
