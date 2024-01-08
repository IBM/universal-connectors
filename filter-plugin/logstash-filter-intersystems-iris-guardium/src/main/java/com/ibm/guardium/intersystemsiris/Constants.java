/*
Copyright IBM Corp. 2023 All rights reserved.
SPDX-License-Identifier: Apache-2.0
*/
package com.ibm.guardium.intersystemsiris;

public interface Constants {

	public static final String UNKNOWN_STRING = "";
	public static final String TIMESTAMP = "mytimestamp";
	public static final String DB_NAME = "namespace";
	public static final String DB_USER = "username";
	public static final String SERVER_HOSTNAME = "systemid";
	public static final String CLIENT_IP = "clientipaddress";
	public static final String SERVER_IP ="Server_ip";
	public static final String DEFAULT_IP = "0.0.0.0";
	public static final int DEFAULT_PORT = -1;
	public static final String OS_USER = "osusername";
	public static final String EVENT_TYPE = "event";
	public static final String EVENT_DATA = "eventdata";
	public static final String SERVER_TYPE = "IRIS";
	public static final String DB_PROTOCOL = "IRIS";
	public static final String EXCEPTION_TYPE_AUTHORIZATION_STRING = "LOGIN_FAILED";
	public static final String NOT_AVAILABLE = "NA";
	public static final Object VERB = "verb";
	public static final Object OBJECTS = "objects";
	public static final Object DESCENDANTS = "descs";
	public static final String COLLECTION = "collection";
	public static final String DEFAULT_IPV6 = "0000:0000:0000:0000:0000:FFFF:0000:0000";
	public static final String DESCRIPTION = "description";
	public static final String PATTERN_ipv4 = "^((0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)\\.){3}(0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)$";
	public static final String PATTERN_ipv6 = "(([0-9a-fA-F]{1,4}:){7,7}[0-9a-fA-F]{1,4}|([0-9a-fA-F]{1,4}:){1,7}:|([0-9a-fA-F]{1,4}:){1,6}:[0-9a-fA-F]{1,4}|([0-9a-fA-F]{1,4}:){1,5}(:[0-9a-fA-F]{1,4}){1,2}|([0-9a-fA-F]{1,4}:){1,4}(:[0-9a-fA-F]{1,4}){1,3}|([0-9a-fA-F]{1,4}:){1,3}(:[0-9a-fA-F]{1,4}){1,4}|([0-9a-fA-F]{1,4}:){1,2}(:[0-9a-fA-F]{1,4}){1,5}|[0-9a-fA-F]{1,4}:((:[0-9a-fA-F]{1,4}){1,6})|:((:[0-9a-fA-F]{1,4}){1,7}|:)|fe80:(:[0-9a-fA-F]{0,4}){0,4}%[0-9a-zA-Z]{1,}|::(ffff(:0{1,4}){0,1}:){0,1}((25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])\\.){3,3}(25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])|([0-9a-fA-F]{1,4}:){1,4}:((25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])\\.){3,3}(25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9]))";

}
