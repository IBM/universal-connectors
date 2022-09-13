/*
© Copyright IBM Corp. 2021, 2022 All rights reserved.
SPDX-License-Identifier: Apache-2.0
*/
package com.ibm.guardium.apachesolrdb;

import org.apache.commons.lang3.StringUtils;

public class ApplicationConstant {

	public static final String LOGSTASH_TAG_JSON_PARSE_ERROR = "Parser error";
	public static final String INSERT_ID = "-1";
	public static final String UNKNOWN_STRING = StringUtils.EMPTY;
	public static final String DATA_PROTOCOL = "ApacheSolrAzure";
	public static final String DEFAULT_IP = "0.0.0.0";
	public static final String SERVER_TYPE = "SolrDB";
	public static final String NOT_AVAILABLE = "NA";
	public static final String LOGSTASH_TAG_SKIP_NOT_SOLR = "_solrguardium_skip_not_solr";
	public static final String SOLR_QTP_LOG_UPDATE = "o.a.s.u.p.LogUpdateProcessorFactory";
	public static final String SOLR_QTP_ERROR_STRING = "o.a.s.h.RequestHandlerBase";
	public static final String SOLR_QTP_OVERSEER_STRING = "o.a.s.c.a.c.OverseerCollectionMes***REMOVED***geHandler";
	public static final String SOLR_QTP_REQUEST = "o.a.s.c.S.Request";
	public static final String HTTP_SOLR_CALL = "o.a.s.s.HttpSolrCall";
	public static final String INFO_EVENT = "INFO";
	public static final String ERROR = "ERROR";
	public static final String EXCEPTION_DESCRIPTION_STRING = "SolrException";

	// Input parameters
	public static final String USER_NAME = "username";
	public static final String TIMESTAMP = "timestamp";
	public static final String QUERY_STRING = "query_value";
	public static final String SERVER_HOSTNAME = "serverHostName";
	public static final String SERVER_IP = "server_ip";
	public static final String SERVER_OS = "server_os";
	public static final String REQUEST_TYPE = "request_value";
	public static final String COLLECTION = "collection";
	public static final String ERROR_MSG = "error_value";
	public static final String LOG_TYPE = "event_type";
	public static final String VERB = "path_value";
	public static final String CLASS = "class_name";
	public static final String WEB_APP = "app_value";
	public static final String EXCEPTION_TYPE_AUTHORIZATION_STRING = "SQL_ERROR";

	// Operations
	public static final String SPLIT_BY_SPACE = " ";
	public static final String SPLIT_BY_EQUAL = "=";
	public static final String CORE_CHECK = "x:";
	public static final String COLLECTION_CHECK = "c:";
	public static final String PATH = "/select";
	public static final String ADD = "add=";
	public static final String DELETE = "delete";
	public static final String CREATE_CORE = "action=CREATE";
	public static final String UPDATE_CORE = "action=RENAME";
	public static final String DELETE_CORE = "action=UNLOAD";
	public static final String DB_KEY_CHECK = "ACTION";
	public static final String CORE_KEY_CHECK = "COREVALUE";
	public static final String FULLSQL_CHECK = "SQL";
	public static final String MASK_STRING = "?";
	public static final String STATEMENT = "stmt=select";
	public static final String KEY_ACTION = "action";
	public static final String KEY_CORE = "core";
	public static final String KEY_NAME = "name";
	public static final String KEY_TARGET = "target";
	public static final String KEY_OTHER = "other";
	public static final String KEY_EQUAL_COLLECTION = "collection=";
	public static final String KEY_EQUAL_TARGET = "target=";
	public static final String DELETE_COLLECTION = "action=DELETE";
	public static final String KEY_EQUAL_CORE = "core=";
	public static final String LIST_COLLECTION = "action=LIST";
	public static final String COLLECTION_PROP = "action=COLLECTIONPROP";
	public static final String COL_STATUS = "action=COLSTATUS";
	public static final String MODIFY_COLLECTION = "action=MODIFYCOLLECTION";
	public static final String RELOAD = "action=RELOAD";
	public static final String MIGRATE_COLLECTION = "action=MIGRATE";
	public static final String REINDEX = "action=REINDEXCOLLECTION";
	public static final String BACKUP = "action=BACKUP";
	public static final String RESTORE = "action=RESTORE";
	public static final String REBALANCELEADERS = "action=REBALANCELEADERS";
	public static final String KEY_EQUAL_NAME = "name=";
	public static final String KEY_EQUAL_OTHER = "other=";
	public static final String CLUSTERSTATUS = "action=CLUSTERSTATUS";
	public static final String CLUSTERPROP = "action=CLUSTERPROP";
	public static final String BALANCESHARDUNIQUE = "action=BALANCESHARDUNIQUE";
	public static final String ADDROLE = "action=ADDROLE";
	public static final String OVERSEERSTATUS = "action=OVERSEERSTATUS";
	public static final String MIGRATESTATEFORMAT = "action=MIGRATESTATEFORMAT";
	public static final String SPLITSHARD = "action=SPLITSHARD";
	public static final String DELETESHARD = "action=DELETESHARD";
	public static final String FORCELEADER = "action=FORCELEADER";
	public static final String CREATESHARD = "action=CREATESHARD";
	public static final String ADDREPLICA = "action=ADDREPLICA";
	public static final String MOVEREPLICA = "action=MOVEREPLICA";
	public static final String DELETEREPLICA = "action=DELETEREPLICA";
	public static final String ADDREPLICAPROP = "action=ADDREPLICAPROP";
	public static final String DELETEREPLICAPROP = "action=DELETEREPLICAPROP";
	public static final String CREATEALIAS = "action=CREATEALIAS";
	public static final String LISTALIASES = "action=LISTALIASES";
	public static final String ALIASPROP = "action=ALIASPROP";
	public static final String DELETEALIAS = "action=DELETEALIAS";
	public static final String UTILIZENODE = "action=UTILIZENODE";
	public static final String REPLACENODE = "action=REPLACENODE";
	public static final String DELETENODE = "action=DELETENODE";
	public static final String REMOVEROLE = "action=REMOVEROLE";
	public static final String REQUESTSTATUS = "action=REQUESTSTATUS";
	public static final String SWAP_CORE = "action=SWAP";
	public static final String CORE_STATUS = "action=STATUS";
	public static final String SPLIT_CORE = "action=SPLIT";
	public static final String REQUESTRECOVERY = "action=REQUESTRECOVERY";
	public static final String MERGEINDEXES = "action=MERGEINDEXES";
	public static final String STATUS_CHECK = "status=0";
	public static final String SPELL = "/spell";
	public static final String SOLR_SQL_ERROR_STRING = "o.a.s.c.s.i.s.ExceptionStream";
	public static final String DEFAULT_IPV6 = "0000:0000:0000:0000:0000:FFFF:0000:0000";
	public static final String PARAMS = "query_type";
	public static final String PATH_FOR_FULLSQL = "path_type";
	public static final String WEBAPP_FOR_FULLSQL = "app_type";
	public static final String CORE_FOR_FULLSQL = "core_value";
	public static final String CORE_COLLECTION = "core_collection";
	public static final String SQL_ERROR_DESCRIPTION = "o.a.s.c.s.i.s.ExceptionStream java.io.IOException: Failed to execute sqlQuery";
	public static final String LEX_ERROR_DESCRIPTION = "o.a.s.h.RequestHandlerBase org.apache.solr.common.SolrException: org.apache.solr.search.SyntaxError: Cannot parse";
}
