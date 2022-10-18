/*
* � Copyright IBM Corp. 2021, 2022 All rights reserved.
*SPDX-License-Identifier: Apache-2.0
*/

package com.ibm.neptune.connector.constant;

public interface ApplicationConstants {

	public static final String SOURCE = "source";

	public static final String QUERY_TYPE_SPARQL = "sparql";
	public static final String QUERY_TYPE_GREMLIN = "gremlin";
	public static final String QUERY_TYPE_STATUS = "status";

	public static final String GREMLINQUERYIDENTIFIER = "/gremlin";
	public static final String SPARQLQUERYIDENTIFIER = "/sparql";
	public static final String STATUSQUERYIDENTIFIER = "/status";
	public static final String GREMLIN_PROFILE = "/gremlin/profile";
	public static final String GREMLIN_EXPLAIN = "/gremlin/explain";
	public static final String SERVERHOSTIDENTIFIER = "neptune.amazonaws.com";

	public static final String NEPTUNE_AWS = "Neptune(AWS)";

	public static final String CLIENT_HOST = "clienthost";
	public static final String SERVER_HOST = "serverhost";
	public static final String REQUEST_HEADERS = "httpheaders";
	public static final String UNKNOWN = "unknown";
	public static final String TIMESTAMP = "timestamp";
	public static final String MESSAGE = "mes***REMOVED***ge";
	public static final String VERSION = "version";
	public static final String PAYLOAD = "payload";
	public static final String ARGS = "args";
	public static final String SERVER_HOSTNAME_PREFIX = "serverHostnamePrefix";
	public static final String DBNAME_PREFIX = "dbnamePrefix";
	public static final String CALLER_IAM = "callerIAM";

	public static final String DOT = "\\.";
	public static final String FORWARDSLASH = "/";
	public static final String BACKWARDSLASH = "\\";
	public static final String COLON = ":";
	public static final String DOUBLE_COLON = "::";
	public static final String EQUAL = "=";
	public static final String SPACE = " ";
	public static final String COLON_SPACE = ": ";
	public static final String QUOMA_SPACE = ", ";
	public static final String UNKNOWN_STRING = "";

	public static final String NOT_AVAILABLE = "NA";
	public static final String TEXT = "TEXT";
	public static final String NEPTUNE = "Neptune";
	public static final String NOTEBOOK = "NoteBook";
	public static final String DEFAULT_IP = "0.0.0.0";
	public static final String DEFAULT_PORT = "-1";
	public static final String UTF = "UTF-8";
	public static final String VERB = "verb";
	public static final String OBJECT = "object";
	public static final String QUERY = "query";
	public static final String BASE_URL = "http%3A%2F%2Fexample.org%2F";
	public static final String EXPLAIN = "explain=dynamic";

	// operations
	public static final String SELECT = "select";
	public static final String CREATE = "create";
	public static final String INSERT = "insert";
	public static final String DROP = "drop";
	public static final String CLEAR = "clear";
	public static final String DELETE = "delete";
	public static final String TYPE = "graph";
	public static final String ADD = "add";
	public static final String UPDATE = "update";
	public static final String VERTICES = "vertices";
	public static final String EDGES = "edges";
	public static final String NODE_TYPE = "type";
	public static final String IDs = "id";
	public static final String ALL = "[All]";
	public static final String VERTEX = "Vertex";
	public static final String ADD_VERTEX = "AddVertex";
	public static final String DBNAME = "dbname";
	public static final String DATA = "data";
	public static final String MOVE = "move";
	public static final String COPY = "copy";
	public static final String LOAD = "load";
	public static final String DESCRIBE = "describe";
	public static final String CONST = "_const_";
	public static final String ANON = "_anon_";
	public static final String DESCRB = "_describe_";

}
