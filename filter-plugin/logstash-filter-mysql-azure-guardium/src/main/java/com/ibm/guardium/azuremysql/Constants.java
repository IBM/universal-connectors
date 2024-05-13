/*
Copyright IBM Corp. 2023 All rights reserved.
SPDX-License-Identifier: Apache-2.0
*/
package com.ibm.guardium.azuremysql;

public interface Constants {

	public static final String UNKNOWN_STRING = "";
	public static final String TIMESTAMP = "event_time";
	public static final String DB_USER = "user";
	public static final String CLIENT_IP = "ip";
	public static final int CLIENT_PORT = -1;
	public static final int DEFAULT_PORT = -1;
	public static final String RECORDS = "records";
	public static final String PROPERTIES = "properties";
	public static final String SESSION_ID = "connection_Id";
	public static final String RESOURCE_ID = "resourceId";
	public static final String EVENT_CATEGORY = "event_class";
	public static final String EVENT_SUBCATEGORY = "event_subclass";
	public static final String DB_NAME = "db";
	public static final String DB_PROTOCOL = "MYSQL";
	public static final String SQL_TEXT = "sql_text";
	public static final String ERROR_CODE = "error_code";
	public static final String SERVER_TYPE = "MySql";
	public static final String Language = "MYSQL";
	public static final String DEFAULT_IP = "0.0.0.0";
	public static final String DEFAULT_IPV6 = "0000:0000:0000:0000:0000:FFFF:0000:0000";
	public static final String EXCEPTION_TYPE_AUTHORIZATION_STRING = "SQL_ERROR";
	public static final String NOT_AVAILABLE = "N.A.";
}
