package com.ibm.guardium.mysql;

import co.elastic.logstash.api.Event;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.ibm.guardium.universalconnector.commons.structures.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


import java.text.ParseException;
import java.time.*;

import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Parser {

    private static final Logger log = LogManager.getLogger(Parser.class);

    public static Record parseRecord(final Event event) throws ParseException {

        Map<String, Object> data = event.getData();

        log.debug(Constants.EVENT_DATA, data);

        Record record = new Record();
        if (data != null) {
            // set default value since we don't have sessionId
            record.setSessionId(Constants.UNKNOWN_STRING);

            // accountId:dbName
            record.setDbName(getAccountIdDBName(data));


            if(null != data.get(Constants.DATABASE_USER_NAME)
                    && !data.get(Constants.DATABASE_USER_NAME).toString().isEmpty()){
                record.setAppUserName(data.get(Constants.DATABASE_USER_NAME).toString());
            }

            record.setTime(getTime(data));

            record.setSessionLocator(getSessionLocator(data));

            record.setAccessor(getAccessor(data));

            setQueryORExceptionRecord(data, record);
        }
        log.debug(Constants.GUARD_RECORD, record);

        return record;

    }

    private static void setQueryORExceptionRecord(Map data, Record record) {
        if(null != data.get(Constants.STATUS_CODE) && null != data.get(Constants.COMMAND)
                && Integer.parseInt(data.get(Constants.STATUS_CODE).toString()) == 0
                && !data.get(Constants.COMMAND).toString().isEmpty()
                && data.get(Constants.COMMAND).toString().equals(Constants.QUERY_CONST)){
            setQueryData(data, record);
        } else if (null != data.get(Constants.COMMAND)
                && !data.get(Constants.COMMAND).toString().isEmpty()
                && data.get(Constants.COMMAND).toString().equals(Constants.FAILED_CONNECT)) {
            setExceptionRecord(data, record);
        } else if (null != data.get(Constants.STATUS_CODE)
                && Integer.parseInt(data.get(Constants.STATUS_CODE).toString()) != 0) {
            setExceptionRecord (data, record);
        }
    }

    private static void setExceptionRecord(Map data, Record record) {
        ExceptionRecord exceptionRecord = new ExceptionRecord();

        String description = Constants.DESCRIPTION_MESSAGE + data.get(Constants.STATUS_CODE).toString();

        exceptionRecord.setExceptionTypeId(Constants.SQL_ERROR);

        exceptionRecord.setDescription(description);

        if(null != data.get(Constants.QUERY) && !data.get(Constants.QUERY).toString().isEmpty()){
            exceptionRecord.setSqlString(data.get(Constants.QUERY).toString());
        } else {
            exceptionRecord.setSqlString(Constants.NOT_AVAILABLE);
        }

        if (null != data.get(Constants.COMMAND_TYPE)
                && !data.get(Constants.COMMAND_TYPE).toString().isEmpty()
                && null != data.get(Constants.ACTION)
                && data.get(Constants.ACTION).toString().equals(Constants.FAILED_CONNECT)) {
            exceptionRecord.setExceptionTypeId(Constants.LOGIN_FAILED);
            description = Constants.CONNECTION_FAILED_DESCRIPTION_MESSAGE + data.get(Constants.STATUS_CODE).toString();
            exceptionRecord.setDescription((description));
            exceptionRecord.setSqlString(Constants.NOT_AVAILABLE);
        }
        record.setException(exceptionRecord);
    }

    private static void setQueryData(Map data, Record record) {
        if (data.get(Constants.QUERY) != null && !data.get(Constants.QUERY).toString().isEmpty()) {

            String rawQuery = data.get(Constants.QUERY).toString();

            Data queryData = new Data();
            queryData.setConstruct(null);
            queryData.setOriginalSqlCommand(rawQuery);

            record.setData(queryData);
        }
    }


    private static Accessor getAccessor(Map data) {
        Accessor accessor = new Accessor();

        if(null != data.get(Constants.DATABASE_USER_NAME)
                && !data.get(Constants.DATABASE_USER_NAME).toString().isEmpty()){
            accessor.setDbUser(data.get(Constants.DATABASE_USER_NAME).toString());
        }

        accessor.setServerType(Constants.SERVER_TYPE);
        accessor.setServerOs(Constants.UNKNOWN_STRING);
        accessor.setClientOs(Constants.UNKNOWN_STRING);
        accessor.setClientHostName(Constants.UNKNOWN_STRING);

        // accountId:dbName
        accessor.setServerHostName(getAccountIdDBName(data));

        accessor.setCommProtocol(Constants.UNKNOWN_STRING);
        accessor.setDbProtocol(Constants.DB_PROTOCOL);
        accessor.setDbProtocolVersion(Constants.UNKNOWN_STRING);
        accessor.setOsUser(Constants.UNKNOWN_STRING);
        accessor.setSourceProgram(null);
        accessor.setClient_mac(Constants.UNKNOWN_STRING);

        // accountId:dbName
        accessor.setServiceName(getAccountIdDBName(data));


        accessor.setLanguage(Constants.DB_PROTOCOL);
        accessor.setDataType(Constants.TEXT);

        return accessor;
    }

    private static String getAccountIdDBName(Map data) {

        String dbNameAccountId = Constants.UNKNOWN_STRING;
        String dbInstanceName = "";



        if(null != data.get(Constants.MESSAGE)
                && !data.get(Constants.MESSAGE).toString().isEmpty()
                && isValidJson(data.get(Constants.MESSAGE).toString())){

           JsonObject messageJSON = getJSON(data.get(Constants.MESSAGE).toString());

            if(null != messageJSON && null != messageJSON.keySet()
                    && messageJSON.keySet().contains(Constants.LOG_GROUP)){
                String logGroup =  messageJSON.get(Constants.LOG_GROUP).toString();
                dbInstanceName = getDBInstanceName(logGroup);
            }
        }


        if(null != data.get(Constants.ACCOUNT_ID)
                && !data.get(Constants.ACCOUNT_ID).toString().isEmpty()
                && null != data.get(Constants.DATABASE_NAME)
                && !data.get(Constants.DATABASE_NAME).toString().isEmpty()){

            String accountId = Constants.UNKNOWN_STRING;
            String database = data.get(Constants.DATABASE_NAME).toString();

            if(data.get(Constants.ACCOUNT_ID) instanceof String){
                accountId = data.get(Constants.ACCOUNT_ID).toString();
            }
            else if (data.get(Constants.ACCOUNT_ID) instanceof List<?>) {
                List<?> rawList = (List<?>) data.get(Constants.ACCOUNT_ID);
                List<Object> arrayList = new ArrayList<>(rawList);

                if(!arrayList.isEmpty()){
                    accountId = String.valueOf(arrayList.get(0));
                }
            }
            if (null != dbInstanceName && !dbInstanceName.isEmpty()){
                dbNameAccountId =  accountId+":"+dbInstanceName+":"+database;
            } else {
                dbNameAccountId = accountId+":"+database;
            }


        }
        return dbNameAccountId;
    }

    private static Time getTime(Map data) {
        if(null != data.get(Constants.TIMESTAMP) && !data.get(Constants.TIMESTAMP).toString().isEmpty()){
           return getEpochTime(data.get(Constants.TIMESTAMP).toString());
        }
        return null;
    }

    private static SessionLocator  getSessionLocator(Map data) {
        String serverIp = getAccountIdDBName(data);
        SessionLocator sessionLocator = new SessionLocator();
        if(null != data.get(Constants.CLIENT_IP)
                && !data.get(Constants.CLIENT_IP).toString().isEmpty()){
            sessionLocator.setClientIp(data.get(Constants.CLIENT_IP).toString());
        }
        sessionLocator.setClientPort(Constants.DEFAULT_PORT);
        sessionLocator.setServerPort(Constants.DEFAULT_PORT);
        sessionLocator.setServerIp(serverIp);
        sessionLocator.setIpv6(false);
        sessionLocator.setClientIpv6(Constants.UNKNOWN_STRING);
        sessionLocator.setServerIpv6(Constants.UNKNOWN_STRING);
        return sessionLocator;
    }

    private static Time getEpochTime(String timeStamp) {
        ZonedDateTime zonedDateTime;

        try {
            // Try to parse as ISO-8601 (with 'Z' and nanoseconds) i.e. 2025-07-22T13:14:45.644605897Z
            Instant instant = Instant.parse(timeStamp);
            zonedDateTime = instant.atZone(ZoneId.systemDefault());
        } catch (DateTimeParseException e) {
            // Fallback: try to parse using the custom format
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd HH:mm:ss");
            LocalDateTime localDateTime = LocalDateTime.parse(timeStamp, formatter);
            zonedDateTime = localDateTime.atZone(ZoneId.systemDefault());
        }

        long millis = zonedDateTime.toInstant().toEpochMilli();
        int minOffset = zonedDateTime.getOffset().getTotalSeconds() / 60;

        return new Time(millis, minOffset, 0);
    }

    private static String getDBInstanceName(String logGroupName) {
        if (logGroupName == null || logGroupName.isEmpty()) {
            return null;
        }

        // Split by "/" and get the element at index 4
        String[] parts = logGroupName.split("/");
        if (parts.length >= 5) {
            return parts[4];
        }
        return null;
    }


    private static JsonObject getJSON(String jsonString) {
        try {
            JsonElement element = JsonParser.parseString(jsonString);

            if (element.isJsonObject()) {
                return element.getAsJsonObject();
            } else {
                throw new IllegalArgumentException("Provided string is not a JSON object");
            }

        } catch (JsonSyntaxException | IllegalArgumentException e) {
            // Handle invalid JSON or wrong type
            log.error("Error parsing JSON: " + e.getMessage());
            return null;
        }
    }

    private static boolean isValidJson(String json) {
        if (json == null || json.trim().isEmpty()) {
            log.warn("JSON string is null or empty");
            return false;
        }

        try {
            JsonElement element = JsonParser.parseString(json);

            if (element == null || element.isJsonNull()) {
                log.warn("Parsed JSON is null: {}", json);
                return false;
            }

            return true;
        } catch (JsonSyntaxException e) {
            log.warn("Invalid JSON syntax: {} , {}", json, e);
            return false;
        }
    }

}
