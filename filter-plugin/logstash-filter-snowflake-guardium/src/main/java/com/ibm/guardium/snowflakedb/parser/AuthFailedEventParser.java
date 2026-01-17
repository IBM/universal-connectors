//
// Copyright 2021-2023 IBM Inc. All rights reserved
// SPDX-License-Identifier: Apache2.0
//

package com.ibm.guardium.snowflakedb.parser;

import com.ibm.guardium.snowflakedb.utils.Constants;
import com.ibm.guardium.snowflakedb.utils.DefaultGuardRecordBuilder;
import com.ibm.guardium.snowflakedb.exceptions.ParseException;
import com.ibm.guardium.universalconnector.commons.structures.ExceptionRecord;
import com.ibm.guardium.universalconnector.commons.structures.Accessor;
import com.ibm.guardium.universalconnector.commons.structures.Construct;
import com.ibm.guardium.universalconnector.commons.structures.Data;
import com.ibm.guardium.universalconnector.commons.structures.ExceptionRecord;
import com.ibm.guardium.universalconnector.commons.structures.Record;
import com.ibm.guardium.universalconnector.commons.structures.Sentence;
import com.ibm.guardium.universalconnector.commons.structures.SentenceObject;
import com.ibm.guardium.universalconnector.commons.structures.SessionLocator;
import com.ibm.guardium.universalconnector.commons.structures.Time;
import com.ibm.guardium.universalconnector.commons.structures.SessionLocator;
import com.ibm.guardium.universalconnector.commons.structures.Time;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class AuthFailedEventParser implements Parser{
    private static Logger log = LogManager.getLogger(SuccessEventParser.class);
    private Map<String, Object> eventMap;
    private Record guardRecord;

    public AuthFailedEventParser() {
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

        String dbUser = getStringValueOf(Constants.USER_NAME);
        guardRecord.getAccessor().setDbUser(dbUser);
        guardRecord.getAccessor().setOsUser(Constants.NOT_AVAILABLE);
        guardRecord.getAccessor().setSourceProgram(getStringValueOf(Constants.CLIENT_APPLICATION_ID));
        guardRecord.getAccessor().setServiceName(Constants.NOT_AVAILABLE);
        guardRecord.getAccessor().setClientOs(Constants.NOT_AVAILABLE);
        guardRecord.getAccessor().setServerHostName(getStringValueOf(Constants.SERVER_HOST_NAME));

        ExceptionRecord exceptionRecord = setExceptionRecord();
        guardRecord.setException(exceptionRecord);
        guardRecord.setData(null);

        Integer hashCode = (sessionLocator.getClientIp() + sessionLocator.getClientPort()
                + sessionLocator.getServerIp() + sessionLocator.getServerPort()).hashCode();
        guardRecord.setSessionId(hashCode.toString());
        guardRecord.setDbName(Constants.NOT_AVAILABLE);
        guardRecord.setAppUserName(dbUser);

        return guardRecord;
    }

    private ExceptionRecord setExceptionRecord() {
        ExceptionRecord exceptionRecord = guardRecord.getException();

        try{
            exceptionRecord.setExceptionTypeId(Constants.LOGIN_FAILED);
            String description = getStringValueOf(Constants.LOGIN_ERROR_CODE) + ": " +getStringValueOf(Constants.LOGIN_ERROR_MESSAGE);
            exceptionRecord.setDescription(description);
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
        String ts = getStringValueOf(Constants.LOGIN_TIMESTAMP);
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
