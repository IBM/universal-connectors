//
// Copyright 2021-2023 IBM Inc. All rights reserved
// SPDX-License-Identifier: Apache2.0
//

package com.ibm.guardium.snowflakedb.parser;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.google.gson.*;
import com.ibm.guardium.snowflakedb.utils.Constants;
import com.ibm.guardium.snowflakedb.utils.DefaultGuardRecordBuilder;
import com.ibm.guardium.snowflakedb.exceptions.ParseException;
import com.ibm.guardium.universalconnector.commons.structures.Accessor;
import com.ibm.guardium.universalconnector.commons.structures.Construct;
import com.ibm.guardium.universalconnector.commons.structures.Data;
import com.ibm.guardium.universalconnector.commons.structures.ExceptionRecord;
import com.ibm.guardium.universalconnector.commons.structures.Record;
import com.ibm.guardium.universalconnector.commons.structures.Sentence;
import com.ibm.guardium.universalconnector.commons.structures.SentenceObject;
import com.ibm.guardium.universalconnector.commons.structures.SessionLocator;
import com.ibm.guardium.universalconnector.commons.structures.Time;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SuccessEventParser implements Parser{
    private static Logger log = LogManager.getLogger(SuccessEventParser.class);

    private Map<String, Object> eventMap;
    private Record guardRecord;

    public SuccessEventParser() {
        DefaultGuardRecordBuilder builder = new DefaultGuardRecordBuilder();
        guardRecord = builder.buildGuardRecordWithDefaultValues();
        eventMap = new HashMap<>();
	}


	/**
     * Parses a Snowflake event sent via JDBC input plugin query.
     * 
     * @param event A logstash event containing Snowflake key/value pairs
     * @return
     */
    @Override
    public Record parseRecord(final Map<String, Object> event) throws ParseException {
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


        Data dataObj = setData();
        guardRecord.setData(dataObj);
        guardRecord.setException(null);

        guardRecord.setSessionId(this.getStringValueOf(Constants.SESSION_ID));
        guardRecord.setDbName(this.getStringValueOf(Constants.DATABASE_NAME));

        return this.guardRecord;
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

    private Data setData() {
        Data dataObj = guardRecord.getData();
        try{
            dataObj.setOriginalSqlCommand(getStringValueOf(Constants.QUERY_TEXT));
        } catch (Exception e) {
            log.error("Snowflake filter: Error occurred while parsing Data object: " + eventMap, e);
            throw e;
        }

        return dataObj;
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
                } else {
                    accessor.setOsUser(Constants.NOT_AVAILABLE);
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
