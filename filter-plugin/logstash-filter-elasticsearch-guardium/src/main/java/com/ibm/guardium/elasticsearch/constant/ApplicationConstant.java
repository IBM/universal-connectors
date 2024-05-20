/*
Copyright IBM Corp. 2024 All rights reserved.
SPDX-License-Identifier: Apache-2.0
*/
package com.ibm.guardium.elasticsearch.constant;

public class ApplicationConstant {

	public static final String TIMESTAMP_KEY = "timestamp";
    public static final String HOSTNAME_KEY = "host.name";
	public static final String HOSTIP_KEY = "host.ip";
	public static final String USERNAME_KEY = "user.name";
	public static final String EVENTTYPE_KEY = "event.type";
	public static final String EVENTACTION_KEY = "event.action";
	public static final String ORIGINADDRESS_KEY = "origin.address";
	public static final String URLPATH_KEY = "url.path";
	public static final String URLQUERY_KEY = "url.query";
	public static final String REQUESTMETHOD_KEY = "request.method";
	public static final String REQUESTBODY_KEY = "request.body";
	public static final String REQUESTID_KEY = "request.id";
	public static final String TYPE_KEY = "type";
	public static final String CLUSTERID_KEY = "cluster.uuid";
	public static final String NODENAME_KEY = "node.name";
	public static final String NODEID_KEY = "node.id";
	public static final String SERVER_TYPE_STRING = "Elasticsearch";
	public static final String DBPROTOCAL_STRING = "EL_SEARCH";
	public static final String LANGUAGE_STRING = "EL_SEARCH";
    public static final String DEFAULT_IP= "0.0.0.0";
	public static final int DEFAULT_PORT = -1;
	public static final String UNKNOWN_STRING = "";
	public static final String NOT_AVAILABLE = "NA";
	public static final String AUTHENTICATION_SUCCESS_STRING = "authentication_success";
	public static final String EXCEPTION_TYPE_AUTHENTICATION_STRING = "LOGIN_FAILED";
	public static final String EXCEPTION_DESCRIPTION_AUTHENTICATION_STRING = "authentication failed";
	public static final String ERROR_TRACE_STRING = "error_trace";
	public static final String APP_USER_NAME_STRING = "app_user_name";
	public static final String SERVER_HOST_STRING = "server_host";
	public static final String PRETTY_STRING = "pretty";
}
