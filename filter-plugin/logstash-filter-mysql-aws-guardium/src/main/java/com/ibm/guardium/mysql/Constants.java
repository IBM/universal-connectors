package com.ibm.guardium.mysql;


public interface Constants {


    String NOT_AVAILABLE = "NA";



    String LOGSTASH_TAG_SKIP_NOT_PROGRESS = "LOGSTASH_TAG_SKIP_NOT_PROGRESS";



    String SERVER_IP = "server_ip";



    String SERVER_PORT = "portNum";




    String SERVER_HOST = "host"; //db machine

    String CLIENT_HOST = "Client_Name";

    String CLIENT_SESSION_ID = "clientSessionId";









    String SOURCE_PROGRAM = "SOURCE_PROGRAM";

    String USER_ID = "user";





    String EVENT_CONTEXT = "eventContext";



    String DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

    String LOGIN_FAILED = "LOGIN_FAILED";



    String SQL_TEXT = "sql_text";



    String EVENT_NOW_MYSQL = "MySQL data {} ";


    String DATABASE_NAME = "database";

    String DATABASE_USER_NAME = "user";


    String TIMESTAMP = "timestamp";

    int MIN_OFFSET_FROM_GMT = 0;

    int MIN_DST = 0;

    String CLIENT_IP = "client_ip";

    int DEFAULT_PORT = -1;

    String DEFAULT_IP = "0.0.0.0";

    String UNKNOWN_STRING = "";

    String SERVER_TYPE = "MySQL";

    String ACCOUNT_ID = "account_id";

    String DB_PROTOCOL = "MYSQL";

    String TEXT = "TEXT";

    String QUERY = "query";

    String QUERY_CONST = "QUERY";

    String  STATUS_CODE = "status_code";

    String COMMAND_TYPE = "command_type";

    String FAILED_CONNECT = "FAILED_CONNECT";

    String ACTION = "action";

    String DESCRIPTION_MESSAGE = "The Query has failed with Error code ";

    String CONNECTION_FAILED_DESCRIPTION_MESSAGE = "Login Connection request failed with Error code ";

    String GUARD_RECORD = "GuardRecord {}";

    String EVENT_DATA = "Event Data {}";

    String RECORDS = "records";

    String TIMESTAMP_ERROR = "Invalid timestamp format: {}";

    String COMMAND_UNKNOWN = "UNKNOWN";

    String COMMAND = "command";

    String RDS_ADMIN = "rdsadmin";

    String LOG_GROUP = "logGroup";

    String MESSAGE = "message";

    String SQL_ERROR = "SQL_ERROR";
}

