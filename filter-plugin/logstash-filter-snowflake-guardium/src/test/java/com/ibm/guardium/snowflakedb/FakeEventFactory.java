//
// Copyright 2021-2023 IBM Inc. All rights reserved
// SPDX-License-Identifier: Apache2.0
//

package com.ibm.guardium.snowflakedb;

import com.ibm.guardium.snowflakedb.utils.Constants;
import org.logstash.Event;

public class FakeEventFactory {
    public static Event getSuccessEvent(){
        Event event = new Event();

        event.setField(Constants.LOGIN_SUCCESS, "YES");
        event.setField(Constants.SESSION_ID, "28004041468657710");
        event.setField(Constants.QUERY_EXECUTION_STATUS, "SUCCESS");
        event.setField(Constants.QUERY_TIMESTAMP, "2023-05-17T18:30:41.016");
        event.setField(Constants.CLIENT_IP,"129.41.59.5");
        event.setField(Constants.SERVER_IP, "103.81.94.233");
        event.setField(Constants.USER_NAME, "FOO_USER");
        String clientEnv = "{\"tracing\":\"INFO\",\"OS\":\"Mac OS X\",\"OCSP_MODE\":\"FAIL_OPEN\",\"JAVA_VM\":\"OpenJDK " +
                "64-Bit Server VM\",\"warehouse\":\"COMPUTE_WH\",\"password\":\"****\"," +
                "\"database\":\"SNOWFLAKE_SAMPLE_DATA\",\"OS_VERSION\":\"13.3.1\"," +
                "\"serverURL\":\"https://qaybzea-gib95623.snowflakecomputing.com:443/\",\"JAVA_VERSION\":\"17\"," +
                "\"user\":\"chiragsoni\",\"account\":\"qaybzea-gib95623\"," +
                "\"JAVA_RUNTIME\":\"OpenJDK Runtime Environment\"}";

        event.setField(Constants.CLIENT_ENVIRONMENT, clientEnv);
        event.setField(Constants.SERVER_HOST_NAME, "qaybzea-gib95623.aws_us_east_1.aws.snowflakecomputing.com");
        event.setField(Constants.CLIENT_APPLICATION_ID, "JDBC 3.13.6");
        event.setField(Constants.WAREHOUSE_NAME, "COMPUTE_WH");
        event.setField(Constants.DATABASE_NAME, "TESTDB");
        event.setField(Constants.QUERY_ID, "01ac58cc-0504-85a9-0063-7d870004518e");
        event.setField(Constants.QUERY_TEXT, "SELECT CURRENT_DATABASE(), CURRENT_SCHEMA()");

        return event;
    }

    public static Event getSQLErrorEvent(){
        Event event = new Event();

        event.setField(Constants.LOGIN_SUCCESS, "YES");
        event.setField(Constants.SESSION_ID, "28004041468657710");
        event.setField(Constants.QUERY_EXECUTION_STATUS, "FAIL");
        event.setField(Constants.QUERY_TIMESTAMP, "2023-05-17T18:30:41.016");
        event.setField(Constants.CLIENT_IP,"129.41.59.5");
        event.setField(Constants.SERVER_IP, "103.81.94.233");
        event.setField(Constants.USER_NAME, "FOO_USER");
        String clientEnv = "{\"tracing\":\"INFO\",\"OS\":\"Mac OS X\",\"OCSP_MODE\":\"FAIL_OPEN\",\"JAVA_VM\":\"OpenJDK " +
                "64-Bit Server VM\",\"warehouse\":\"COMPUTE_WH\",\"password\":\"****\"," +
                "\"database\":\"SNOWFLAKE_SAMPLE_DATA\",\"OS_VERSION\":\"13.3.1\"," +
                "\"serverURL\":\"https://qaybzea-gib95623.snowflakecomputing.com:443/\",\"JAVA_VERSION\":\"17\"," +
                "\"user\":\"chiragsoni\",\"account\":\"qaybzea-gib95623\"," +
                "\"JAVA_RUNTIME\":\"OpenJDK Runtime Environment\"}";

        event.setField(Constants.CLIENT_ENVIRONMENT, clientEnv);
        event.setField(Constants.SERVER_HOST_NAME, "qaybzea-gib95623.aws_us_east_1.aws.snowflakecomputing.com");
        event.setField(Constants.CLIENT_APPLICATION_ID, "JDBC 3.13.6");
        event.setField(Constants.WAREHOUSE_NAME, "COMPUTE_WH");
        event.setField(Constants.DATABASE_NAME, "TESTDB");
        event.setField(Constants.QUERY_ID, "01ac58cc-0504-85a9-0063-7d870004518e");
        event.setField(Constants.QUERY_TEXT, "SELECT CURRENT_DATABASE(),");
        event.setField(Constants.QUERY_ERROR_MESSAGE, "Syntax Error");
        event.setField(Constants.QUERY_ERROR_CODE, "1234");

        return event;
    }

    public static Event getAuthErrorEvent(){
        Event event = new Event();

        event.setField(Constants.LOGIN_SUCCESS, "NO");
        event.setField(Constants.LOGIN_TIMESTAMP, "2023-05-17T18:30:41.016");
        event.setField(Constants.CLIENT_IP,"129.41.59.5");
        event.setField(Constants.SERVER_IP, "103.81.94.233");
        event.setField(Constants.USER_NAME, "FOO_USER");

        event.setField(Constants.SERVER_HOST_NAME, "qaybzea-gib95623.aws_us_east_1.aws.snowflakecomputing.com");
        event.setField(Constants.CLIENT_APPLICATION_ID, "JDBC_DRIVER 3.13.6");
        event.setField(Constants.LOGIN_ERROR_CODE, "390100");
        event.setField(Constants.LOGIN_ERROR_MESSAGE, "INCORRECT_USERNAME_PASSWORD");
        event.setField(Constants.QUERY_ID, "01ac58cc-0504-85a9-0063-7d870004518e");

        return event;
    }

    public static String getClientEnvWithoutOsUser(){
        return "{\"tracing\":\"INFO\",\"OS\":\"Mac OS X\",\"OCSP_MODE\":\"FAIL_OPEN\",\"JAVA_VM\":\"OpenJDK " +
                "64-Bit Server VM\",\"warehouse\":\"COMPUTE_WH\",\"password\":\"****\"," +
                "\"database\":\"SNOWFLAKE_SAMPLE_DATA\",\"OS_VERSION\":\"13.3.1\"," +
                "\"serverURL\":\"https://qaybzea-gib95623.snowflakecomputing.com:443/\",\"JAVA_VERSION\":\"17\"," +
                "\"account\":\"qaybzea-gib95623\"," +
                "\"JAVA_RUNTIME\":\"OpenJDK Runtime Environment\"}";
    }
}
