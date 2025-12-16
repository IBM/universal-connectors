/*
#Copyright 2020-2021 IBM Inc. All rights reserved
#SPDX-License-Identifier: Apache-2.0
#*/
package com.ibm.guardium.s3sqspostgresql;

import co.elastic.logstash.api.Event;
import com.ibm.guardium.universalconnector.commons.structures.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Parser {

    private static Logger log = LogManager.getLogger(Parser.class);

    public static Record parseRecord(final Event e) throws ParseException {

        Record record = new Record();

        record.setAccessor(Parser.parseAccessor(e));

        if (e.getField(Constants.PARSED_MESSAGE) != null) {
            Object parsedMessageObj = e.getField(Constants.PARSED_MESSAGE);
            String dbName = Constants.UNKNOWN_STRING;
            if (parsedMessageObj instanceof Map) {
                Map<?, ?> parsedMessage = (Map<?, ?>) parsedMessageObj;

                String accountId = getAccountId(e);

                if (!parsedMessage.isEmpty() && parsedMessage.get(Constants.DATABASE_NAME) != null
                        && !accountId.isEmpty()) {
                    dbName = parsedMessage.get(Constants.DATABASE_NAME).toString();
                }
                // Set dbName for Amazon Data Firehose method
                else if (null != e.getData().get(Constants.DB_NAME)
                        && !e.getData().get(Constants.DB_NAME).toString().isEmpty()) {
                    dbName = e.getData().get(Constants.DB_NAME).toString();
                }
                if (dbName.isEmpty()) {
                    record.setDbName(Constants.NA);
                } else {
                    record.setDbName(accountId + ":" + getInstanceName(e) + ":" + dbName);
                }

                if (parsedMessage.get(Constants.CONNECTION_FROM) != null) {

                    setSeesionLocator(e, parsedMessage, record, dbName);
                }
                // Set SessionLocator for Amazon Data Firehose method
                else if (null != e.getData().get(Constants.CLIENT_IP)
                        && null != e.getData().get(Constants.CLIENT_PORT)
                        && !e.getData().get(Constants.CLIENT_IP).toString().isEmpty()
                        && !e.getData().get(Constants.CLIENT_PORT).toString().isEmpty()) {
                    String clientIP = e.getData().get(Constants.CLIENT_IP).toString();
                    String clientPort = e.getData().get(Constants.CLIENT_PORT).toString();
                    record.setSessionLocator(Parser.parseSessionLocator(e, clientIP, clientPort, dbName));

                } else {
                    setDefaultSessionLocator(record);
                }
                record.setSessionId(Constants.UNKNOWN_STRING);
                setOriginalSqlCommand(e, (Map<?, ?>) parsedMessageObj, record);
            }
        }

        record.setAppUserName(Constants.APP_USER_NAME);

        record.setTime(Parser.parseTimestamp(e));
        
        // Parse and set response time from duration field
        // Duration is expected in milliseconds and needs to be converted to microseconds
        if (e.includes(Constants.DURATION) && (null != e.getField(Constants.DURATION))) {
            try {
                String durationStr = e.getField(Constants.DURATION).toString();
                // Parse duration value (e.g., "0.919" from "0.919 ms")
                double durationMs = Double.parseDouble(durationStr);
                // Convert milliseconds to microseconds as per protobuf definition
                int durationMicros = (int) (durationMs * 1000);
                record.setResponseTime(durationMicros);
            } catch (NumberFormatException ex) {
                log.debug("Failed to parse duration: {}", e.getField(Constants.DURATION), ex);
            }
        }
        
        return record;
    }

    private static String getAccountId(Event e) {
        String accountId = "";
        if (e.getField(Constants.ACCOUNT_ID) instanceof String) {
            accountId = e.getField(Constants.ACCOUNT_ID).toString();
        } else if (e.getField(Constants.ACCOUNT_ID) instanceof List<?>) {
            List<?> rawList = (List<?>) e.getField(Constants.ACCOUNT_ID);
            List<Object> arrayList = new ArrayList<>(rawList);

            if (!arrayList.isEmpty()) {
                accountId = String.valueOf(arrayList.get(0));
            }
        }
        return accountId;
    }

    public static String getInstanceName(Event e) {
        String res = "";
        if (e.getField(Constants.INSTANCE_NAME) instanceof String) {
            res = e.getField(Constants.INSTANCE_NAME).toString();
        } else if (e.getField(Constants.INSTANCE_NAME) instanceof List<?>) {
            List<?> rawList = (List<?>) e.getField(Constants.INSTANCE_NAME);
            List<Object> arrayList = new ArrayList<>(rawList);

            if (!arrayList.isEmpty()) {
                res = String.valueOf(arrayList.get(0));
            }
        }
        return res;
    }

    private static void setOriginalSqlCommand(Event e, Map parsedMessageObj, Record record) {
        if (parsedMessageObj.get(Constants.SQL_STATE_CODE) != null
                && !parsedMessageObj.get(Constants.SQL_STATE_CODE).toString().isEmpty()
                && parsedMessageObj.get(Constants.SQL_STATE_CODE).toString().equals(Constants.SQL_STATE_CODE_SUCCESS)) {
            Data data = new Data();
            setSQL(e, record, data);
        }
        // Set SQL for Amazon Data Firehose method
        else if (null != parsedMessageObj.get(Constants.LOG_GROUP)
                && null != e.getData().get(Constants.LOG_LEVEL)
                && null != e.getField(Constants.FULL_SQL_QUERY)
                && !parsedMessageObj.get(Constants.LOG_GROUP).toString().isEmpty()
                && !e.getData().get(Constants.LOG_LEVEL).toString().isEmpty()
                && !e.getField(Constants.FULL_SQL_QUERY).toString().isEmpty()) {
            Data data = new Data();
            setSQL(e, record, data);
        } else {
            // Error message for Amazon Data Firehose method
            if (null != e.getData().get(Constants.LOG_LEVEL)
                    && null != e.getData().get(Constants.ERROR_MESSAGE)
                    && !e.getData().get(Constants.LOG_LEVEL).toString().isEmpty()
                    && (e.getData().get(Constants.LOG_LEVEL).toString().equals(Constants.FATAL)
                    || !e.getData().get(Constants.ERROR_MESSAGE).toString().isEmpty())) {

                ExceptionRecord exceptionRecord = new ExceptionRecord();
                exceptionRecord.setExceptionTypeId(Constants.LOGIN_FAILED);
                if (null != e.getData().get(Constants.ERROR_MESSAGE)
                        && e.getData().get(Constants.ERROR_MESSAGE).toString().isEmpty()) {
                    String message = e.getData().get(Constants.ERROR_MESSAGE).toString();
                    exceptionRecord.setDescription(message);
                }
                exceptionRecord.setSqlString(Constants.UNKNOWN_STRING);
                record.setSessionLocator(setDefaultSessionLocator(record));
                record.setException(exceptionRecord);
            } else if (parsedMessageObj.get(Constants.ERROR_SEVERITY) != null &&
                    (parsedMessageObj.get(Constants.ERROR_SEVERITY).equals(Constants.ERROR))) {

                ExceptionRecord exceptionRecord = new ExceptionRecord();
                exceptionRecord.setExceptionTypeId(Constants.SQL_ERROR);

                if (null != parsedMessageObj.get(Constants.MESSAGE) &&
                        !parsedMessageObj.get(Constants.MESSAGE).toString().isEmpty()) {
                    String message = parsedMessageObj.get(Constants.MESSAGE).toString();
                    message = message.replaceAll("\"(\\w+)\"", "$1");
                    exceptionRecord.setDescription(message);
                }
                exceptionRecord.setSqlString(Constants.UNKNOWN_STRING);
                record.setSessionLocator(setDefaultSessionLocator(record));
                record.setException(exceptionRecord);
            } else {
                ExceptionRecord exceptionRecord = new ExceptionRecord();
                exceptionRecord.setExceptionTypeId(Constants.LOGIN_FAILED);
                if (null != parsedMessageObj.get(Constants.MESSAGE) &&
                        !parsedMessageObj.get(Constants.MESSAGE).toString().isEmpty()) {
                    String message = parsedMessageObj.get(Constants.MESSAGE).toString();
                    message = message.replaceAll("\"(\\w+)\"", "$1");
                    exceptionRecord.setDescription(message);
                }
                exceptionRecord.setSqlString(Constants.UNKNOWN_STRING);
                record.setSessionLocator(setDefaultSessionLocator(record));
                record.setException(exceptionRecord);
            }
        }
    }

    private static void setSQL(Event e, Record record, Data data) {
        if (e.getField(Constants.FULL_SQL_QUERY) != null) {
            String sqlQuery = e.getField(Constants.FULL_SQL_QUERY).toString();
            if (sqlQuery.contains("\"\"")) {
                sqlQuery = sqlQuery.replaceAll("\"\"([^\"]*)\"\"", "\"$1\"");
            }
            data.setOriginalSqlCommand(sqlQuery);
        } else {
            data.setOriginalSqlCommand(Constants.NA);
        }
        record.setData(data);
    }

    // Set Default SessionLocator in case of LOGIN_FAILED or ERROR
    private static SessionLocator setDefaultSessionLocator(Record record) {
        String serverIp = Constants.DEFAULT_IP;
        SessionLocator sessionLocator = new SessionLocator();
        sessionLocator.setClientIp(Constants.DEFAULT_IP);
        sessionLocator.setClientPort(Constants.DEFAULT_PORT);
        sessionLocator.setServerIp(serverIp);
        sessionLocator.setServerPort(Constants.DEFAULT_PORT);
        sessionLocator.setIpv6(false);
        sessionLocator.setClientIpv6(Constants.UNKNOWN_STRING);
        sessionLocator.setServerIpv6(Constants.UNKNOWN_STRING);
        return sessionLocator;
    }

    private static void setSeesionLocator(Event e, Map<?, ?> parsedMessage, Record record, String dbName) {
        Object connectionFrom = parsedMessage.get(Constants.CONNECTION_FROM);
        String[] connectionArr = connectionFrom.toString().split(":");

        String clientIP = Constants.UNKNOWN_STRING;
        String clientPort = Constants.UNKNOWN_STRING;

        if (connectionArr[0] != null && !connectionArr[0].isEmpty() &&
                connectionArr[1] != null && !connectionArr[1].isEmpty()) {
            clientIP = connectionArr[0];
            clientPort = connectionArr[1];
        }

        record.setSessionLocator(Parser.parseSessionLocator(e, clientIP, clientPort, dbName));
    }

    public static Time parseTimestamp(final Event e) {
        long millis = 0;
        try {
            Object field = e.getField(Constants.TIMESTAMP);

            String dateString;

            if (field instanceof Object[]) {
                Object[] arr = (Object[]) field;
                if (arr.length > 0) {
                    dateString = arr[0].toString();
                } else {
                    dateString = "";
                }
            } else {
                dateString = field.toString();
                // Handle case where string looks like "[ts1, ts2]"
                if (dateString.startsWith("[") && dateString.endsWith("]")) {
                    String[] parts = dateString.substring(1, dateString.length() - 1).split(",");
                    if (parts.length > 0) {
                        dateString = parts[0].trim();
                    }
                }
            }
            // Try ISO first
            try {
                ZonedDateTime parsedTime = ZonedDateTime.parse(dateString, DateTimeFormatter.ISO_DATE_TIME);
                millis = parsedTime.toInstant().toEpochMilli();
            } catch (DateTimeParseException isoEx) {
                // Fallback to local date time format
                log.debug("ISO parse failed, trying fallback format", isoEx);
                LocalDateTime localDateTime = LocalDateTime.parse(dateString, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                ZonedDateTime zonedDateTime = localDateTime.atZone(ZoneId.systemDefault());
                millis = zonedDateTime.toInstant().toEpochMilli();
            }

        } catch (Exception exe) {
            log.error("parseTimestamp final failure: {}", exe);
        }
        return new Time(millis, 0, 0);
    }

    public static SessionLocator parseSessionLocator(Event e, String clientIP, String clientPort, String dbName) {

        String accountId = getAccountId(e);
        String serverIp = accountId + ":" + dbName;

        SessionLocator sessionLocator = new SessionLocator();
        sessionLocator.setClientIp(clientIP);
        sessionLocator.setClientPort(Integer.parseInt(clientPort));
        sessionLocator.setServerIp(serverIp);
        sessionLocator.setServerPort(Constants.DEFAULT_PORT);
        sessionLocator.setIpv6(false);
        sessionLocator.setClientIpv6(Constants.UNKNOWN_STRING);
        sessionLocator.setServerIpv6(Constants.UNKNOWN_STRING);
        return sessionLocator;
    }


    public static Accessor parseAccessor(final Event e) {
        Accessor accessor = new Accessor();
        accessor.setDataType(Accessor.DATA_TYPE_GUARDIUM_SHOULD_PARSE_SQL);
        accessor.setLanguage(Constants.LANGUAGE);
        accessor.setClientHostName(Constants.NA);
        accessor.setClientOs(Constants.UNKNOWN_STRING);
        accessor.setServerOs(Constants.UNKNOWN_STRING);

        String accId = getAccountId(e);
        String instanceName = getInstanceName(e);

        if (e.getField(Constants.PARSED_MESSAGE) != null) {
            Object parsedMessageObj = e.getField(Constants.PARSED_MESSAGE);
            if (parsedMessageObj instanceof Map) {
                Map<?, ?> parsedMessage = (Map<?, ?>) parsedMessageObj;

                String dbUser = Constants.UNKNOWN_STRING;
                if (!parsedMessage.isEmpty() && parsedMessage.get(Constants.USER_NAME) != null) {
                    dbUser = parsedMessage.get(Constants.USER_NAME).toString();
                }
                // Set DBUser for Amazon Data Firehose method
                else if (null != e.getData()
                        && null != e.getData().get(Constants.DB_USER)
                        && !e.getData().get(Constants.DB_USER).toString().isEmpty()) {
                    dbUser = e.getData().get(Constants.DB_USER).toString();
                }
                if (dbUser.isEmpty()) {
                    accessor.setDbUser(Constants.NA);
                } else {
                    accessor.setDbUser(dbUser);
                }

                accessor.setOsUser(Constants.UNKNOWN_STRING);
                String accountId = getAccountId(e);
                if (parsedMessage.get(Constants.DATABASE_NAME) != null
                        && !accountId.isEmpty()) {
                    accessor.setServiceName(accountId + ":" + instanceName + ":" + parsedMessage.get(Constants.DATABASE_NAME).toString());
                }
                // Set ServiceName for Amazon Data Firehose method
                else if (null != e.getData()
                        && null != e.getData().get(Constants.DB_NAME)
                        && !e.getData().get(Constants.DB_NAME).toString().isEmpty()) {
                    accessor.setServiceName(accountId + ":" + instanceName + ":" + e.getData().get(Constants.DB_NAME).toString());
                } else {
                    accessor.setServiceName(Constants.NA);
                }

                if (parsedMessage.get(Constants.APPLICATION_NAME) != null) {
                    accessor.setSourceProgram(parsedMessage.get(Constants.APPLICATION_NAME).toString());
                } else {
                    accessor.setSourceProgram(Constants.UNKNOWN_STRING);
                }
            }
        }

        accessor.setServerType(Constants.SERVER_TYPE_STRING);
        accessor.setCommProtocol(Constants.COMM_PROTOCOL);
        accessor.setDbProtocol(Constants.DATA_PROTOCOL_STRING);
        accessor.setDbProtocolVersion(Constants.UNKNOWN_STRING);
        accessor.setClient_mac(Constants.UNKNOWN_STRING);
        accessor.setServerDescription(Constants.UNKNOWN_STRING);
        accessor.setServerHostName(accId + ":" + instanceName);

        return accessor;
    }

}