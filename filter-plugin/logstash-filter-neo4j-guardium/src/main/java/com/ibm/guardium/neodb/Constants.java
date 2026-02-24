//
// Copyright 2020-2021 IBM Inc. All rights reserved
// SPDX-License-Identifier: Apache2.0
//

package com.ibm.guardium.neodb;

public class Constants {

	public static final String UNKNOWN_STRING = "";
	public static final String NOT_AVAILABLE = "NA";
	public static final String DATA_PROTOCOL_STRING = "BoltDB";
	public static final String SERVER_TYPE_STRING = "NEO4J";
	public static final String COMM_PROTOCOL = "Neo4JApiCall";
	public static final String LOGSTASH_TAG_SKIP_NOT_NEO = "_neoguardium_skip_not_neo";

	public static final String LOGSTASH_TAG_JSON_PARSE_ERROR = "_neoguardium_json_parse_error";
	public static final String SERVER_HOSTNAME = "serverHostName";
	public static final String SQL_ERROR = "SQL_ERROR";

	

	// Input parameters
	public static final String CLIENT_IP = "client_ip";
	public static final String SERVER_IP = "server_ip";
	public static final String DB_PROTOCOL = "protocol";
	public static final String TIMESTAMP = "ts";
	public static final String MIN_OFF = "minOff";
	public static final String LOG_LEVEL = "log_level";
	public static final String DB_USER = "dbuser";
	public static final String DB_NAME = "dbname";
	public static final String SOURCE_PROGRAM = "driverVersion";
	public static final String QUERY_STATEMENT = "queryStatement";
	public static final String MESSAGE = "message";
	public static final String TYPE = "graph";

	// Operations
	public static final String MATCH = "MATCH";
	public static final String CREATE = "CREATE";
	public static final String ON_CREATE_SET = "ON CREATE SET";
	public static final String ON_MATCH_SET = "ON MATCH SET";
	public static final String MERGE = "MERGE";
	public static final String DELETE = "DELETE";
	public static final String DETACH_DELETE = "DETACH DELETE";
	public static final String REMOVE = "REMOVE";
	public static final String SET = "SET";
	public static final String RETURN = "RETURN";
	public static final String WHERE = "WHERE";
	public static final String FOREACH = "FOREACH";
	public static final String ON_CREAT_ST = "ON_CREAT_ST";
	public static final String ON_MATC_ST = "ON_MATC_ST";
	public static final String DETACH_DELET = "DETACH_DELET";
	public static final String EXPLAIN = "EXPLAIN";

	public static final String EVERYTHING = "everything";
}
