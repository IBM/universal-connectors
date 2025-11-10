package com.ibm.guardium.auroramysql;

public interface Constants {
	
	public static final String NOT_AVAILABLE = "NA";
	public static final int PORT = -1;
	public static final String UNKNOWN_STRING = "";
    public static final String LOGSTASH_TAG_JSON_PARSE_ERROR = "_auroraMysql_guardium_json_parse_error";
    public static final String TIMESTAMP = "ts";
    public static final String SERVER_INSTANCE="serverInstance";
    public static final String DB_USER="userName";
    public static final String CLIENT_IP = "clientIp";
    public static final String SESSION_ID = "connectionId";
    public static final String AUDIT_ACTION ="operation";
    public static final String DB_NAME="dbName";
    public static final String EXEC_STATEMENT = "originalSQL";
	public static final String ACTION_STATUS = "retcode";
	public static final String DB_PROTOCOL = "MYSQL";
	public static final String SERVER_TYPE = "MySql";
	public static final String Language = "MYSQL";
	public static final String APP_USER = "AWSService";
	public static final String IP = "0.0.0.0";
	public static final String SERVERHOSTNAME = "Server_Hostname";
	public static final String LOGIN_FAILED = "LOGIN_FAILED";
	public static final String SQL_ERROR = "SQL_ERROR";
	public static final String TAGS = "tags";
	public static final String GROK_PARSE_FAILURE = "_grokparsefailure";
	public static final String MESSAGE = "message";
	public static final String SQL_ERROR_CODE_MY_010914 = "MY-010914";
}
