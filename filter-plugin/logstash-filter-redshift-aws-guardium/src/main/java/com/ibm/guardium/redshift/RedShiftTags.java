package com.ibm.guardium.redshift;

/**
 * This interface contains all the constants which is used across the
 * application.
 */
public interface RedShiftTags {
	public static final String UNKNOWN_STRING = "";
	public static final String ID = "pid";
	public static final String DB = "dbname";
	public static final String TIMESTAMP = "timestamp";
	public static final String U_IDENTIFIER = "user";
	public static final String USER_NAME = "username";
	public static final String REMOTEHOST = "remotehost";
	public static final String REMOTEPORT = "remoteport";
	public static final String OSVERSION = "os_version";
	public static final String DAY = "day";
	public static final String MONTH = "month";
	public static final String YEAR = "year";
	public static final String MD = "md";
	public static final String TIME = "time";
	public static final String SQLQUERY = "sql_query";
	public static final String LOGSTASH_TAG_JSON_PARSE_ERROR = "redshift-parse-failure";
	public static final String REDSHIFT_STRING = "Redshift";
	public static final String DB_PROTOCOL = "Redshift(AWS)";
	public static final String NA_STRING = "NA";
	public static final String DEFAULT_IP = "0.0.0.0";
	public static final String DEFAULT_IPV6 = "0000:0000:0000:0000:0000:FFFF:0000:0000";
	public static final String LANGUAGE = "PGRS";
	public static final Integer DEFAULT_PORT = -1;
	public static final String EXCEPTION_TYPE_AUTHENTICATION_STRING = "LOGIN_FAILED";
	public static final String STATUS = "action";
	public static final String DBPREFIX = "dbprefix";
	public static final String SERVERHOSTNAME_PREFIX = "serverHostnamePrefix";
	public static final String UNAUTHORISED = "authentication failure";
	public static final String COLLECTION = "collection";
}
