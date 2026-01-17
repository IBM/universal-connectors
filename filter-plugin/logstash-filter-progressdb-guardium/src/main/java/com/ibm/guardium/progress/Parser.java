//
// Copyright 2023 IBM All Rights Reserved.
// SPDX-License-Identifier: Apache2.0
//
package com.ibm.guardium.progress;

import com.google.gson.JsonObject;
import com.ibm.guardium.universalconnector.commons.structures.Record;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;



public class Parser {

    private static final Logger log = LogManager.getLogger(Parser.class);

    public static Record parseRecord(final JsonObject data) throws ParseException {
        Record record = new Record();
        if (data != null) {

            //database name
            if (data.has(Constants.DATABASE_NAME) && !data.get(Constants.DATABASE_NAME).isJsonNull() && !data.get(Constants.DATABASE_NAME).getAsString().isEmpty())
                record.setDbName(data.get(Constants.DATABASE_NAME).getAsString());
            else record.setDbName(Constants.UNKNOWN_STRING);


            record.setAppUserName(Constants.NOT_AVAILABLE);
            record.setSessionLocator(parseSessionLocator(data));
            record.setAccessor(parseAccessor(data));
            record.setTime(parseTimestamp(data));
            record.setData(parseData(data));
            record.setException(parseException(data));



            if (data.get(Constants.CLIENT_SESSION_ID).getAsString().isEmpty()) {
                Integer hashCode = (record.getSessionLocator().getClientIp() + record.getSessionLocator().getClientPort() + record.getDbName()).hashCode();
                record.setSessionId(hashCode.toString());

            } else {
                record.setSessionId(data.get(Constants.CLIENT_SESSION_ID).getAsString());

            }

        }
        return record;

    }

    // ----------- TIME


    public static Time parseTimestamp(final JsonObject data) throws ParseException {
        if (data.has(Constants.TIMESTAMP) && data.get(Constants.TIMESTAMP) != null) {
            String dateString = data.get(Constants.TIMESTAMP).getAsString();
            if (dateString != null) {
                ZonedDateTime zonedDateTime = LocalDateTime.parse(dateString,
                        DateTimeFormatter.ofPattern(Constants.DATE_TIME_FORMAT)).atZone(ZoneId.of(Constants.UTC));
                long epoch = zonedDateTime.toInstant().toEpochMilli();
                int minOffset = 0;
                Time t = new Time(epoch, minOffset, 0);
                return t;
            }
        }
        return null;
    }

    // ----------- Session Locator


    public static SessionLocator parseSessionLocator(JsonObject data) {
        SessionLocator sessionLocator = new SessionLocator();
        sessionLocator.setIpv6(false);

        if (data.has(Constants.CLIENT_IP) && !data.get(Constants.CLIENT_IP).isJsonNull())
            sessionLocator.setClientIp(data.get(Constants.CLIENT_IP).getAsString());
        else sessionLocator.setClientIp(Constants.DEFAULT_IP);

        if (data.has(Constants.CLIENT_PORT) && !data.get(Constants.CLIENT_PORT).isJsonNull())
        sessionLocator.setClientPort(data.get(Constants.CLIENT_PORT).getAsInt());
        else sessionLocator.setClientPort(Constants.DEFAULT_PORT);


        if (data.has(Constants.SERVER_PORT) && !data.get(Constants.SERVER_PORT).isJsonNull())
            sessionLocator.setServerPort(data.get(Constants.SERVER_PORT).getAsInt());
        else sessionLocator.setServerPort(Constants.DEFAULT_PORT);


        if (data.has(Constants.SERVER_IP) && !data.get(Constants.SERVER_IP).isJsonNull())
            sessionLocator.setServerIp(data.get(Constants.SERVER_IP).getAsString());
        else sessionLocator.setServerIp(Constants.DEFAULT_IP);


        sessionLocator.setClientIpv6(Constants.UNKNOWN_STRING);
        sessionLocator.setServerIpv6(Constants.UNKNOWN_STRING);

        return sessionLocator;
    }


    // ---------- Accessor


    public static Accessor parseAccessor(JsonObject data) {
        Accessor accessor = new Accessor();

        accessor.setDbProtocol(Constants.DB_PROTOCOL);
        accessor.setServerType(Constants.SERVER_TYPE);

        if (data.has(Constants.USER_ID) && !data.get(Constants.USER_ID).isJsonNull() && !data.get(Constants.USER_ID).getAsString().isEmpty()) //dbuser
            accessor.setDbUser(data.get(Constants.USER_ID).getAsString());
        else accessor.setDbUser(Constants.NOT_AVAILABLE);

        if (data.has(Constants.SERVER_HOST) && !data.get(Constants.SERVER_HOST).isJsonNull())
            accessor.setServerHostName(data.get(Constants.SERVER_HOST).getAsString());
        else accessor.setServerHostName(Constants.NOT_AVAILABLE);


        if (data.has(Constants.CLIENT_HOST) && !data.get(Constants.CLIENT_HOST).isJsonNull())
            accessor.setClientHostName(data.get(Constants.CLIENT_HOST).getAsString());
        else accessor.setClientHostName(Constants.NOT_AVAILABLE);
        
           if (data.has(Constants.DATABASE_NAME) && !data.get(Constants.DATABASE_NAME).isJsonNull())
            accessor.setServiceName(data.get(Constants.DATABASE_NAME).getAsString());
        else accessor.setServiceName(Constants.NOT_AVAILABLE);


        accessor.setLanguage(Accessor.LANGUAGE_FREE_TEXT_STRING);
        accessor.setDataType(Accessor.DATA_TYPE_GUARDIUM_SHOULD_NOT_PARSE_SQL);
        accessor.setClient_mac(Constants.UNKNOWN_STRING);
        accessor.setClientOs(Constants.UNKNOWN_STRING);
        accessor.setCommProtocol(Constants.UNKNOWN_STRING);
        accessor.setDbProtocolVersion(Constants.UNKNOWN_STRING);
        accessor.setOsUser(Constants.UNKNOWN_STRING);
        accessor.setServerDescription(Constants.UNKNOWN_STRING);
        accessor.setServerOs(Constants.UNKNOWN_STRING);
        accessor.setSourceProgram(Constants.NOT_AVAILABLE);


        return accessor;
    }


//	-------------------------Data Construct----------------

    public static Data parseData(JsonObject inputJSON) {
        Data data = new Data();
        Construct construct;
        try {
            construct = parseAsConstruct(inputJSON);
            if (construct != null) {
                data.setConstruct(construct);
                //data.setOriginalSqlCommand(Constants.NOT_AVAILABLE);

                if (construct.getFullSql() == null || construct.getFullSql().isEmpty()) {
                    construct.setFullSql(inputJSON.toString());
                }
            }
        } catch (Exception e) {
            log.error("Progress filter: Error parsing Json " + inputJSON, e);
            throw e;
        }

        return data;
    }

    private static Construct parseAsConstruct(final JsonObject data) {
        try {
            final Sentence sentence = Parser.parseSentence(data);

            final Construct construct = new Construct();
            construct.sentences.add(sentence);
            construct.setFullSql(data.toString());
            //construct.setRedactedSensitiveDataSql(Constants.NOT_AVAILABLE);

            return construct;


        } catch (final Exception e) {
            throw e;
        }
    }

    protected static Sentence parseSentence(final JsonObject data) {
        Sentence sentence = null;
        String verb;
        String obj = Constants.EVENT_CONTEXT;
        if (data.has(Constants.EVENT_NAME)) {
            String eventName = data.get(Constants.EVENT_NAME).getAsString();

            if (eventName != null || !eventName.isEmpty()) {
                verb = eventName;
                sentence = new Sentence(verb);

                if (data.has(Constants.EVENT_CONTEXT.toString()) && data.get(Constants.EVENT_CONTEXT.toString()) != null && !(data.get(Constants.EVENT_CONTEXT.toString()).isJsonNull())) {
                    String eventcontext = data.get(Constants.EVENT_CONTEXT).getAsString();

                    if (eventcontext != null || !eventcontext.isEmpty())
                        obj = eventcontext;
                }
                SentenceObject so = new SentenceObject(obj);
                sentence.getObjects().add(so);

            }

        }

        return sentence;

    }

    // ------ Error


    public static ExceptionRecord parseException(JsonObject data) {
        ExceptionRecord exceptionRecord = new ExceptionRecord();
        String description = data.get(Constants.EVENT_NAME).getAsString();

        exceptionRecord.setExceptionTypeId(Constants.NOT_AVAILABLE);
        exceptionRecord.setDescription(Constants.NOT_AVAILABLE);
        exceptionRecord.setSqlString(Constants.NOT_AVAILABLE);


        if (data.get(Constants.CLIENT_SESSION_ID).getAsString().isEmpty()) {
            if (data.has(Constants.EVENT_NAME) && !data.get(Constants.EVENT_NAME).isJsonNull()) {

                if (data.get(Constants.EVENT_NAME).getAsString().contains("fail")) {
                    exceptionRecord.setExceptionTypeId(Constants.LOGIN_FAILED);
                    exceptionRecord.setDescription((description));
                    exceptionRecord.setSqlString(Constants.NOT_AVAILABLE);

                }

            }
        }

        return exceptionRecord;
    }


}
