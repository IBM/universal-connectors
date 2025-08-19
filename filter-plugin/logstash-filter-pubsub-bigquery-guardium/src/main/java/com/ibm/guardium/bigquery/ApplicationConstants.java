/*
Copyright IBM Corp. 2021, 2025 All rights reserved.
SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.guardium.bigquery;

import org.apache.commons.lang3.StringUtils;

public class ApplicationConstants {

	public static final String LOGSTASH_TAG_SKIP_NOT_GCP = "_GCPguardium_skip_not_GCP";
	public static final String LOGSTASH_TAG_JSON_PARSE_ERROR = "_bigqueryguardium_json_parse_error";
	public static final String DATA_PROTOCOL_STRING = "BigQuery(GCP)";
	public static final String SERVER_TYPE_STRING = "BigQuery";
	public static final String UNKOWN_STRING = StringUtils.EMPTY;
	public static final String COMPOUND_OBJECT_STRING = "[json-object]";
	public static final String EXCEPTION_TYPE_STRING = "SQL_ERROR";

	public static final String DEFAULT_IPV6 = "0000:0000:0000:0000:0000:FFFF:0000:0000";
	public static final String MESSAGE_CONTAINS = "bigquery.googleapis.com";
	public static final String SERVER_HOST_NAME_STRING = "serverHostname";
	public static final String STATEMENT_TYPE = "statementType";
	public static final String COLLECTION = "collection";
	public static final String UNPARSEABLE = "unparseable";
	public static final String EVENT_MESSAGE = "message";
	public static final String PROTO_PAYLOAD = "protoPayload";

	public static final String METADATA = "metadata";
	public static final String REQUEST_METADATA = "requestMetadata";
	public static final String CALLER_IP = "callerIp";

	public static final String SERVICE_NAME = "serviceName";
	public static final String AT_TYPE = "@type";
	public static final String REQUEST = "request";
	public static final String DATABASE = "database";

	public static final String RESPONSE = "response";

	public static final String AUTHENTICATION_INFO = "authenticationInfo";
	public static final String PRINCIPAL_EMAIL = "principalEmail";

	public static final String SEVERITY = "severity";
	public static final String TIMESTAMP = "timestamp";
	public static final String INSERTID = "insertId";

	public static final String CREATE_STATEMENT = "createStatement";
	public static final String STATEMENTS = "statements";
	public static final String SQL = "sql";

	public static final String ERROR = "error";
	public static final String ERRORS = "errors";
	public static final String CODE = "code";
	public static final String MESSAGE = "message";
	public static final String ERROR_RESULT = "errorResult";

	public static final String SERVICE_DATA = "serviceData";

	public static final String JOB_INSERT_REQUEST = "jobInsertRequest";
	public static final String RESOURCE = "resource";

	public static final String JOB_CONFIGURATION = "jobConfiguration";
	public static final String QUERY = "query";

	public static final String JOB_INSERT_RESPONSE = "jobInsertResponse";

	public static final String STATUS = "status";
	public static final String JOB_STATUS = "jobStatus";
	public static final String JOB_COMPLETED_EVENT = "jobCompletedEvent";
	public static final String JOB = "job";

	public static final String JOB_QUERY_RESPONSE = "jobQueryResponse";
	public static final String JOB_GET_QUERY_RESULT_REQUEST = "jobGetQueryResultsRequest";
	public static final String JOB_GET_QUERY_RESULT_RESPONSE = "jobGetQueryResultsResponse";
	public static final String JOB_QUERY_DONE_RESPONSE = "jobQueryDoneResponse";

	public static final String HTTP_REQUEST = "httpRequest";
	public static final String SERVER_IP = "serverIp";
	public static final String DEFAULT_IP = "0.0.0.0";
	public static final String TABLE_CREATION = "tableCreation";
	public static final String TABLE_CHANGE = "tableChange";
	public static final String TABLE_DATA_READ = "tableDataRead";

	public static final String TABLE = "table";
	public static final String TABLE_NAME = "tableName";
	public static final String SCHEMA_JSON = "schemaJson";
	public static final String FIELDS = "fields";
	public static final String PROJECT_ID = "project_id";

	public static final String NAME = "name";
	public static final String TYPE = "type";

	public static final String JOB_INSERTION = "jobInsertion";
	public static final String JOB_CHANGE = "jobChange";
	public static final String JOB_CONFIG = "jobConfig";
	public static final String QUERY_CONFIG = "queryConfig";

	public static final String VIEW = "view";

	public static final String TABLE_INSERT_REQUEST = "tableInsertRequest";
	public static final String TABLE_UPDATE_REQUEST = "tableUpdateRequest";
	public static final String TABLE_INSERT_RESPONSE = "tableInsertResponse";
	public static final String TABLE_UPDATE_RESPONSE = "tableUpdateResponse";

	public static final String LABELS = "labels";
	public static final String DATASET_ID = "dataset_id";

	public static final String DATASETID = "datasetId";

	public static final String DATA_TYPE = "MYSQL";

	public static final String VERBS = "verbs";
	public static final String OBJECTS = "objects";
	public static final String CREATE = "create";
	public static final String TRUNCATE = "truncate";
	public static final String AST = "ast";
	public static final Object KEYWORD = "keyword";
	public static final Object TYPE_BIGQUERY = "bigquery";
	public static final String SELECT = "select";
	public static final String PARSE = "parse";
	public static final String TABLE_LIST = "tableList";
	public static final String DROP = "drop";
	public static final String ALTER = "alter";
	public static final String FUCNTION = "function";
	public static final String PROCEDURE = "procedure";
	public static final String COLUMN_LIST = "columnList";
	public static final String SUBSTITUTE_WITH_QUESTION_MARK = "?";
	public static final String SUBSTITUTE_WITH_INTERSECT = " INTERSECT";
	public static final String DELETE = "delete";
	public static final String FROM_DELETE = "FROM,DELETE";
	public static final String INTO_INSERT = "INTO,INSERT";
	public static final String INSERT = "insert";
	public static final String UPDATE = "update";
	public static final String UNNEST = "unnest";
	public static final String BRACKET = "(";
	public static final String SPACE = " ";
	public static final String DOT = ".";
	public static final String CSV = ",";
	public static final String BACKTICK = "`";
	public static final String ALL_DDL = "ASSIGNMENT,CAPACITY,RESERVATION,VIEW,PROCEDURE,FUNCTION,MATERIALIZED VIEW,SNAPSHOT TABLE,SCHEMA,ON,TABLE";
	public static final String DATASET_CREATION = "datasetCreation";
	public static final String DATASET_DELETION = "datasetDeletion";
	public static final String TABLE_DELETION = "tableDeletion";
	public static final String REASON = "reason";
	
	public static final String OPENB = "(";
	public static final String GRANT = "GRANT";
	public static final String REVOKE = "REVOKE";
	public static final String MERGE = "MERGE";
	
	public static final String CREATE_SCHEMA = "create schema";
	public static final String CREATE_TABlE = "create table";
	public static final String DROP_SCHEMA = "drop schema";
	public static final String DROP_TABLE = "drop table";
	public static final String TABLEINSERT_REQUEST = "table_insert_request";
	public static final String TABLEDELETE_REQUEST = "table_delete_request";
	public static final String RESOURCE_NAME = "resourceName";
	public static final String IMPORT = "IMPORT";
	public static final String LOAD_CONFIG = "loadConfig";
	public static final String DESTINATION_TABLE = "destinationTable";
	public static final String PEG_PARSER = "PegParser";
	
	
	
	private ApplicationConstants() {
	}
}
