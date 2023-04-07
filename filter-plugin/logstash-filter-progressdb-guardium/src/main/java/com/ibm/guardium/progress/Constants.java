//
// Copyright 2023 IBM All Rights Reserved.
// SPDX-License-Identifier: Apache2.0
//

package com.ibm.guardium.progress;


public interface Constants {


    String NOT_AVAILABLE = "NA";

    String UNKNOWN_STRING = "";

    String LOGSTASH_TAG_SKIP_NOT_PROGRESS = "LOGSTASH_TAG_SKIP_NOT_PROGRESS";


    String CLIENT_IP = "clientIP";

    String SERVER_IP = "serverIP";

    String DEFAULT_IP = "0.0.0.0";

    String CLIENT_PORT = "clientPortNum";
    String SERVER_PORT = "portNum";

    int DEFAULT_PORT = -1;

    int minOffset = 0;

    String SERVER_HOST = "hostName"; //db machine

    String CLIENT_HOST = "Client_Name";

    String CLIENT_SESSION_ID = "clientSessionId";

    String TIMESTAMP = "timeStamp";
    String DATABASE_NAME = "databaseName";

    String DB_PROTOCOL = "Progress";

    String SERVER_TYPE = "Progress";

    String SOURCE_PROGRAM = "SOURCE_PROGRAM";

    String USER_ID = "userid";

    String EVENT_NAME = "eventName";

    String EVENT_CONTEXT = "eventContext";

    String LOGIN_FAILED = "LOGIN_FAILED";

    String DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

    String UTC = "UTC";


}

