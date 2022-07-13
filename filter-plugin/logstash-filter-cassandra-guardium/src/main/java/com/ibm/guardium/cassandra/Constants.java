//
// Copyright 2020-2021 IBM Inc. All rights reserved
// SPDX-License-Identifier: Apache2.0
//
package com.ibm.guardium.cassandra;

public interface Constants {
	public static final String INPUT_SPLIT1=" - ";
	public static final String INPUT_SPLIT2="\\|";
	public static final String INPUT_SPLIT3=":";
	public static final String OPERATION_SPLIT1=";";
	public static final String OPERATION_SPLIT2=";;";
	public static final String KEYSPACE="ks";
	
	public static final String UNKNOWN_STRING = "";
	public static final String MESSAGE = "message";
	public static final String LOGSTASH_TAG_JSON_PARSE_ERROR = "_cassandraguardium_json_parse_error";
	public static final String LOGSTASH_TAG_SKIP_NOT_CASSANDRA_DB = "_cassandraguardium_skip_not_cassandradb";
	public static final String CLIENT_IP_VALUE = "0.0.0.0";
	public static final int CLIENT_PORT_VALUE = -1;
	public static final String SERVER_IP_VALUE = "0.0.0.0";
	public static final int SERVER_PORT_VALUE = -1;
	public static final String CLIENT_IP="source";
	public static final String CLIENT_PORT="port";
	public static final String SERVER_IP ="serverIP";
	
	public static final String TIMESTAMP = "timestamp";
	public static final String USER = "user";
	public static final String SERVER_TYPE_STRING = "CASSANDRA";
	public static final String SERVER_HOSTNAME = "serverHostname";
	public static final String DATA_PROTOCOL_STRING = "CASSANDRA";
	public static final String CASS_LANGUAGE="CASS";
	public static final String OPERATION = "operation";
	public static final String ERROR = "ERROR";
	public static final String TYPE = "type";
	public static final String LOGIN_FAILED = "LOGIN_FAILED";
	public static final String SQL_ERROR = "SQL_ERROR";
	public static final String TEXT = "TEXT";
	public static final String CATEGORY = "category";
	public static final String AUTH = "AUTH";
	public static final String LOGIN_SUCCESS = "LOGIN_SUCCESS";
	public static final int limit = 2;
}