//
// Copyright 2021-2023 IBM Inc. All rights reserved
// SPDX-License-Identifier: Apache2.0
//

package com.ibm.guardium.snowflakedb.parser;

import com.google.gson.Gson;
import com.ibm.guardium.snowflakedb.utils.Constants;
import com.ibm.guardium.snowflakedb.utils.DefaultGuardRecordBuilder;
import com.ibm.guardium.snowflakedb.exceptions.ParseException;
import com.ibm.guardium.universalconnector.commons.structures.Record;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class SQLErrorEventParser implements Parser{

    private static Logger log = LogManager.getLogger(SuccessEventParser.class);
    private Map<String, Object> eventMap;
    private Record guardRecord;

    public SQLErrorEventParser(){
        DefaultGuardRecordBuilder builder = new DefaultGuardRecordBuilder();
        guardRecord = builder.buildGuardRecordWithDefaultValues();
        eventMap = new HashMap<>();
    }
    @Override
    public Record parseRecord(Map<String, Object> event) throws ParseException {

        if(event == null){
            ParseException e = new ParseException("Snowflake filter: Event object is null.");
            log.error(e);
            throw e;
        }

        if(log.isDebugEnabled()){
            log.debug("Event Now: ",eventMap);
        }

        eventMap = event;

        Time time = getTime();
        guardRecord.setTime(time);

        SessionLocator sessionLocator = setSessionLocator();
        guardRecord.setSessionLocator(sessionLocator);

        Accessor accessor = setAccessor();
        guardRecord.setAccessor(accessor);

        ExceptionRecord exceptionRecord = setExceptionRecord();
        guardRecord.setException(exceptionRecord);
        guardRecord.setData(null);

        guardRecord.setSessionId(this.getStringValueOf(Constants.SESSION_ID));
        guardRecord.setDbName(this.getStringValueOf(Constants.DATABASE_NAME));

        return guardRecord;
    }

    private Accessor setAccessor() {
        Accessor accessor = guardRecord.getAccessor();

        try {

            accessor.setDbUser(getStringValueOf(Constants.USER_NAME));
            accessor.setSourceProgram(getStringValueOf(Constants.CLIENT_APPLICATION_ID));
            accessor.setServiceName(getStringValueOf(Constants.DATABASE_NAME));
            Optional<String> optClientEnv = Optional.ofNullable(
                    eventMap.get(Constants.CLIENT_ENVIRONMENT)
            ).map(Object::toString);

            if(optClientEnv.isPresent() && !optClientEnv.get().isEmpty()){
                Gson gson = new Gson();
                Map<String, String> clientEnv = gson.fromJson(optClientEnv.get(), Map.class);
                String clientOS = getClientOS(clientEnv);
                accessor.setClientOs(clientOS);

                Optional<String> optOsUSer = Optional.ofNullable(
                        clientEnv.get(Constants.CLIENT_OS_USER)
                ).map(Object::toString);

                if(optOsUSer.isPresent()){
                    accessor.setOsUser(optOsUSer.get());
                }
            }

            accessor.setServerHostName(getStringValueOf(Constants.SERVER_HOST_NAME));
            accessor.setDbProtocol(Constants.DB_PROTOCOL);
        } catch (Exception e) {
            log.error("Snowflake filter: Error occurred while parsing Accessor object: " + eventMap, e);
            throw e;
        }

        return accessor;
    }

    private ExceptionRecord setExceptionRecord() {
        ExceptionRecord exceptionRecord = guardRecord.getException();

        try{
            exceptionRecord.setExceptionTypeId(Constants.SQL_ERROR);
            String description = getStringValueOf(Constants.QUERY_ERROR_CODE) + ": "+ getStringValueOf(Constants.QUERY_ERROR_MESSAGE);
            exceptionRecord.setDescription(description);
            exceptionRecord.setSqlString(getStringValueOf(Constants.QUERY_TEXT));
        } catch (Exception e) {
            log.error("Snowflake filter: Error occurred while parsing Exception object: " + eventMap, e);
            throw e;
        }
        return exceptionRecord;
    }

    private SessionLocator setSessionLocator() {
        SessionLocator sessionLocator = guardRecord.getSessionLocator();
        try {

            sessionLocator.setClientIp(getStringValueOf(Constants.CLIENT_IP));
            sessionLocator.setServerIp(getStringValueOf(Constants.SERVER_IP));
            sessionLocator.setServerPort(Constants.SERVER_PORT);

        } catch (Exception e) {
            log.error("Snowflake filter: Error occurred while parsing session locator object: " + eventMap, e);
            throw e;
        }
        return sessionLocator;
    }

    private String getStringValueOf(String fieldName){
        String value = Constants.NOT_AVAILABLE;
        Optional<String> opt = Optional.ofNullable(
                eventMap.get(fieldName)
        ).map(Object::toString);

        if(opt.isPresent()){
            value = opt.get();
        }
        return  value;
    }

    private Time getTime(){
        String ts = getStringValueOf(Constants.QUERY_TIMESTAMP);
        Time t = guardRecord.getTime();
        try {
            LocalDateTime date = Parser.parseTime(ts);

            t.setTimstamp(date.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()); //Snowflake supplies the date in UTC
            t.setMinOffsetFromGMT(0);
            t.setMinDst(0);
        } catch (Exception e){
            log.error("Snowflake filter: Error occurred while parsing Time object: " + eventMap, e);
            throw e;
        }

        return t;
    }
}
