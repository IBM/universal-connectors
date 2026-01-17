/*
Copyright IBM Corp. 2021, 2023 All rights reserved.
SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.guardium.milvus;

import com.ibm.guardium.universalconnector.commons.custom_parsing.CustomParser;
import com.ibm.guardium.universalconnector.commons.custom_parsing.ParserFactory;
import com.ibm.guardium.universalconnector.commons.structures.Record;
import com.ibm.guardium.universalconnector.commons.structures.Record;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.regex.*;

import static com.ibm.guardium.milvus.Constants.*;
import static com.ibm.guardium.universalconnector.commons.custom_parsing.PropertyConstant.*;

/**
 * Parser Class will perform operation on parsing events and messages from the
 * Milvus audit logs into a Guardium record instance Guardium records include
 * the accessor, the sessionLocator, data, and exceptions. If there are no
 * errors, the data contains details about the query "construct"
 *
 * @className @Parser
 */
public class Parser extends CustomParser {
    private static final Logger logger = LogManager.getLogger(Parser.class);
    private static final String TIME_FORMAT = "yyyy/MM/dd HH:mm:ss.SSS xxx";
    private static final String CLIENT_REGEX = "^([a-zA-Z0-9.-]+)-([0-9a-fA-F:.]+):(\\d+)$";

    private Client client;

    public Parser(ParserFactory.ParserType parserType) {
        super(parserType);
    }

    @Override
    public Record parseRecord(String payload) {
        if (!isValid(payload))
            return null;

        client = getClient(getValue(payload, USER_ADDR)); // DB_PROTOCOL ???
        return extractRecord(payload);
    }

    @Override
    public String getConfigFileContent() {
        return "{\n" +
                "    \"db_name\": \"databaseName\",\n" +
                "    \"db_user\": \"userName\",\n" +
                "    \"db_protocol\": \"MILVUS GRPC\",\n" +
                "    \"app_user_name\": \"userName\",\n" +
                "    \"client\": \"userAddress\",\n" +
                "    \"exception_type_id\": \"errorCode\",\n" +
                "    \"exception_desc\": \"errorMessage\",\n" +
                "    \"collection_name\": \"collectionName\",\n" +
                "    \"partition\": \"partitionName\",\n" +
                "    \"query_expression\": \"queryExpression\",\n" +
                "    \"trace_id\": \"traceId\",\n" +
                "    \"response_size\": \"responseSize\",\n" +
                "    \"time_cost\": \"timeCost\",\n" +
                "    \"time_start\": \"timeStart\",\n" +
                "    \"time_end\": \"timeEnd\",\n" +
                "    \"sdk_version\": \"sdkVersion\",\n" +
                "    \"timestamp\": \"devTime\",\n" +
                "    \"method_name\": \"methodName\",\n" +
                "    \"method_status\": \"methodStatus\",\n" +
                "    \"server_port\": \"{0}\",\n" +
                "    \"server_type\": \"{Milvus}\",\n" +
                "    \"session_id\": \"\",\n" +
                "    \"event_id\": \"$eventid$\",\n" +
                "    \"sniffer_parser\": \"FREE_TEXT\"\n" +
                "}";

    }

    @Override
    protected String getExceptionTypeId(String payload) {
        String value = getValue(payload, EXCEPTION_TYPE_ID);
        if (value == null || value.equals("0"))
            return DEFAULT_STRING;
        
        // Check if this is a login failure
        String eventId = getValue(payload, EVENT_ID);
        String methodStatus = getValue(payload, METHOD_STATUS);
        
        // Identify login failures by checking multiple indicators
        boolean isLoginFailure = (eventId != null && eventId.contains("Connect-GrpcUnauthenticated"))
        || (methodStatus != null && methodStatus.equals("GrpcUnauthenticated"));

        return isLoginFailure ? LOGIN_FAILED : SQL_ERROR;
    }

    @Override
    protected String getExceptionDescription(String payload) {
        String value = getValue(payload, ERROR_MSG);
        return value != null ? value : DEFAULT_STRING;
    }

    @Override
    protected String getSqlString(String payload) {
        String value = getValue(payload, EVENT_ID);
        return value != null ? value : DEFAULT_STRING; // Set the SQL command that caused the exception
    }

    @Override
    protected Time getTimestamp(String payload) {
        String value = getValue(payload, TIMESTAMP);
        if (value != null) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(TIME_FORMAT);

                OffsetDateTime dateTime = OffsetDateTime.parse(value, formatter);
                long millis = dateTime.toInstant().toEpochMilli();
                int minOffsetFromGMT = dateTime.getOffset().getTotalSeconds() / 60;
                return new Time(millis, minOffsetFromGMT, 0);
            } catch (DateTimeParseException e) {
                logger.error("Time {} is invalid.", value, e);
            }
        }
        return new Time(0L, 0, 0);
    }
    @Override
    protected String getDbProtocol(String payload) {
        return client.protocol;
    }

    @Override
    protected Integer getClientPort(String payload) {
        return client.port;
    }
    @Override
    protected String getClientIpv6(String payload) {
        return client.ip;
    }
    @Override
    protected String getClientIp(String payload) {
        return client.ip;
    }

    Client getClient(String clientInfo) {
        if (clientInfo == null)
            return new Client();

        Pattern pattern = Pattern.compile(CLIENT_REGEX);
        Matcher matcher = pattern.matcher(clientInfo);
        if (matcher.matches()) {
            return new Client(matcher.group(1), matcher.group(2), matcher.group(3));
        }

        return new Client();
    }

    @Override
    protected Data getData(String payload, String sqlString) {
        Data data = new Data();

        try {
            // Convert parsed data to JSON format with correct separators
            sqlString = getMlvsGrpcMessage(payload);
        } catch (Exception e) {
            logger.error("Error in parsing LEEF log: ",e.getMessage());
        }

        // Set the final MLVS JSON string
        data.setOriginalSqlCommand(sqlString);

        return data;
    }

    @Override
    protected String getLanguage(String payload) {
        return LANGUAGE;
    }

    @Override
    protected String getDataType(String payload) {
        return DATA_TYPE;
    }

    @Override
    protected Record extractRecord(String payload) {
        Record record = new Record();

        record.setSessionId(getSessionId(payload));
        record.setDbName(getDbName(payload));
        record.setAppUserName(getAppUserName(payload));
        String sqlString = getSqlString(payload);
        record.setException(getException(payload, sqlString));
        record.setAccessor(getAccessor(payload));
        record.setSessionLocator(getSessionLocator(payload));
        record.setTime(getTimestamp(payload));

        record.setData(getData(payload, sqlString));

        return record;
    }
    
    @Override
    protected String getServiceName(String payload) {
        return getDbName(payload);
    }

    private String getMlvsGrpcMessage(String payload) throws Exception {

        StringBuilder sb = new StringBuilder();

        sb.append("__MLVS { ");
        sb.append("\"identifier\":\"").append(getTraceId(payload)).append("\",\n");
        sb.append("\"collection_name\":=\'").append(getCollectionName(payload)).append("\',\n");//object
        sb.append("\"action\":\"").append(getMethodName(payload)).append("\",\n"); //verb
        sb.append("\"query_expression\":\"").append(getQueryExpression(payload)).append("\",\n");
        sb.append("\"dev_time\":\"").append(this.getTimestamp(payload).toString()).append("\",\n");
        sb.append("\"partition_name\":\"").append(getPartitionName(payload)).append("\",\n");
        sb.append("\"trace_id\":\"").append(getTraceId(payload)).append("\",\n");
        sb.append("\"responseSize\":\"").append(getResponseSize(payload)).append("\",\n");
        sb.append("\"time_cost\":\"").append(getTimeCost(payload)).append("\",\n");
        sb.append("\"time_start\":\"").append(getTimeStart(payload)).append("\",\n");
        sb.append("\"time_end\":\"").append(getTimeEnd(payload)).append("\",\n");
        sb.append("\"sdk_version\":\"").append(getSDKVersion(payload)).append("\"\n");
        sb.append("}");

        return sb.toString();
    }

    private String getSDKVersion(String payload) {
        String value = this.getValue(payload,"sdk_version");
        return value != null ? value : "";
    }

    private String getTimeEnd(String payload) {
        String value = this.getValue(payload,"time_end");
        return value != null ? value : "";
    }

    private String getTimeStart(String payload) {
        String value = this.getValue(payload,"time_start");
        return value != null ? value : "";
    }

    private String getTimeCost(String payload) {
        String value = this.getValue(payload,"time_cost");
        return value != null ? value : "";
    }

    private String getResponseSize(String payload) {
        String value = this.getValue(payload,"response_size");
        return value != null ? value : "";
    }

    private String getPartitionName(String payload) {
        String value = this.getValue(payload,"partition_name");
        return value != null ? value : "";
    }

    private String getQueryExpression(String payload) {
        String value = this.getValue(payload,"method_expr");
        return value != null ? value : "";
    }

    private String getMethodName(String payload) {
        String value = this.getValue(payload, "method_name");
        return value != null ? value : "";
    }

    private String getTraceId(String payload) {
        String value = this.getValue(payload, "trace_id");
        return value != null ? value : "";
    }

    private String getCollectionName(String payload) {
        String value = this.getValue(payload, "collection_name");
        return value != null ? value : "";
    }


    static class Client {
        private String protocol = Constants.DB_PROTOCOL;
        private String ip = EMPTY;
        private int port = 0;

        Client(String protocol, String ip, String port) {
            // Always use the constant instead of the extracted protocol
            this.protocol = Constants.DB_PROTOCOL;
            this.ip = ip;
            try {
                this.port = Integer.parseInt(port);
            } catch (NumberFormatException e) {
                this.port = -1;
            }
        }

        Client() {
        }
    }
}