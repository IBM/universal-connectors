//
// Copyright 2020- IBM Inc. All rights reserved
// SPDX-License-Identifier: Apache2.0
//
package com.ibm.guardium.mongodb; // be specific; this will prevent clashes with classes of other plugins

import java.text.ParseException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.ibm.guardium.universalconnector.commons.Util;
import com.ibm.guardium.universalconnector.commons.structures.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Parser {

    private static Logger log = LogManager.getLogger(Parser.class);

    public static final String DATA_PROTOCOL_STRING = "MongoDB native audit";
    public static final String UNKOWN_STRING = "";
    public static final String SERVER_TYPE_STRING = "MongoDB";
    private static final String MASK_STRING = "?";
    public static final String EXCEPTION_TYPE_AUTHORIZATION_STRING = "SQL_ERROR";
    public static final String EXCEPTION_TYPE_AUTHENTICATION_STRING = "LOGIN_FAILED";
    public static final String COMPOUND_OBJECT_STRING = "[json-object]";
    /**
     * These arguments will not be redacted, as they only contain 
     * collection/field names rather than sensitive values.
     */
    public static Set<String> REDACTION_IGNORE_STRINGS = new HashSet<>(
            Arrays.asList("from", "localField", "foreignField", "as", "connectFromField", "connectToField"));

    private static final DateTimeFormatterBuilder dateTimeFormatterBuilder = new DateTimeFormatterBuilder()
            .append(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS[[XXX][X]]"));

    private static final DateTimeFormatter DATE_TIME_FORMATTER = dateTimeFormatterBuilder.toFormatter();

    /**
     * Parses a MongoDB native audit sent over syslog. Format looks as the
     * database.profiler.
     * 
     * @param data
     * @return
     */
    public static String Parse(final JsonObject data) {

        try {

            final Construct construct = Parser.parseAsConstruct(data);

            final GsonBuilder builder = new GsonBuilder();
            builder.setPrettyPrinting().serializeNulls();
            final Gson gson = builder.create();
            return gson.toJson(construct);

        } catch (final Exception e) {
            log.error("MongoDB filter: Error parsing data", e);
            throw e;
        }

    }

    /**
     * Parses a MongoDB native audit sent over syslog.
     * 
     * JSON format appreas after "mongod: " string and expected format is the same
     * as mongodb database.profiler. For example: { "atype": "authCheck", "ts": {
     * "$date": "2020-01-14T10:46:02.431-0500" }, "local": { "ip": "127.0.0.1",
     * "port": 27017 }, "remote": { "ip": "127.0.0.1", "port": 33708 }, "users": [],
     * "roles": [], "param": { "command": "find", "ns": "test.bios", "args": {
     * "find": "bios", "filter": {}, "lsid": { "id": { "$binary":
     * "hg6ugx4ASiGWKSPiDRlEFw==", "$type": "04" } }, "$db": "test" } }, "result": 0
     * },
     * 
     * @return Construct Used by Parse and for easier testing, as well
     * @author Tal Daniel
     */
    public static Construct parseAsConstruct(final JsonObject data) {
        try {
            final Sentence sentence = Parser.parseSentence(data);
            
            final Construct construct = new Construct();
            construct.sentences.add(sentence);
            
            construct.setFullSql(data.toString());
            
            if (data.get("atype").getAsString().equals("authCheck")) {
                // redact, though docs state args may be already redacted.
                Parser.RedactWithExceptions(data); // Warning: overwrites data.param.args
            }
            
            construct.setRedactedSensitiveDataSql(data.toString());
            return construct;
        } catch (final Exception e) {
            throw e;
        }
    }
    
    protected static Sentence parseSentence(final JsonObject data) {
        
        Sentence sentence = null;
        
        final String atype = data.get("atype").getAsString();
        final JsonObject param = data.get("param").getAsJsonObject();
        
        switch (atype) {
            case "authCheck":
                final String command = param.get("command").getAsString();
                final JsonObject args = param.getAsJsonObject("args");

                // + main object
                sentence = new Sentence(command);
                if (args.has(command)) {
                    sentence.getObjects().add(parseSentenceObject(args, command));
                } else if (args.has(command.toLowerCase())) { // 2 word commands are changed to lowercase in args, sometimes(?), like mapReduce, resetErrors
                    sentence.getObjects().add(parseSentenceObject(args, command.toLowerCase()));
                }

                

                switch (command) {
                    case "aggregate":
                        /*
                         * Assumes no inner-lookups; only sequential stages in pipeline.
                         */
                        final JsonArray pipeline = args.getAsJsonArray("pipeline");
                        if (pipeline != null && pipeline.size() > 0) {
                            for (final JsonElement stage : pipeline) {
                                // handle * lookups
                                // + object if stage has $lookup or $graphLookup: { from: obj2 }
                                JsonObject lookupStage = null;

                                if (stage.getAsJsonObject().has("$lookup")) {
                                    lookupStage = stage.getAsJsonObject().getAsJsonObject("$lookup");
                                } else if (stage.getAsJsonObject().has("$graphLookup")) {
                                    lookupStage = stage.getAsJsonObject().getAsJsonObject("$graphLookup");
                                }

                                if (lookupStage != null && lookupStage.has("from")) {
                                    final SentenceObject lookupStageObject = new SentenceObject(
                                            lookupStage.get("from").getAsString());
                                    // + object
                                    sentence.getObjects().add(lookupStageObject);
                                }
                            }
                        }
                    default: // find, insert, delete, update, ...
                        break; // already done before switch
                }
                break;
            /* case "createCollection":
            case "dropCollection":
                final String ns = param.get("ns").getAsString();
                final String[] nsArray = ns.split("\\.");
                final String db = nsArray[0];
                final String collection = nsArray[1];
                sentence = new Sentence(atype); // atype is command
                final SentenceObject sentenceObject = new SentenceObject(collection, db);
                    sentence.objects.add(sentenceObject);
                break; */
            default:
                return null; // NOTE: not parsed
        }

        return sentence;
    }

    protected static SentenceObject parseSentenceObject(JsonObject args, String command) {
        SentenceObject sentenceObject = null;
        if (args.get(command).isJsonPrimitive()) {
            sentenceObject = new SentenceObject(args.get(command).getAsString());
            sentenceObject.setType("collection"); // this used to be default value, but since sentence is defined in common package, "collection" as default value was removed
        } else {
            sentenceObject = new SentenceObject(COMPOUND_OBJECT_STRING);
        }
        return sentenceObject;
    }


    public static Record parseRecord(final JsonObject data) throws ParseException {
        Record record = new Record();

        final JsonObject param = data.get("param").getAsJsonObject();
        final JsonObject args = param.getAsJsonObject("args");
        final String result = data.get("result").getAsString(); // 0 success; 13/18 errors

        String sessionId = Parser.UNKOWN_STRING;
        if (args != null && args.has("lsid")) {
            final JsonObject lsid = args.getAsJsonObject("lsid");
            sessionId = lsid.getAsJsonObject("id").get("$binary").getAsString();
        }
        record.setSessionId(sessionId);

        String dbName = Parser.UNKOWN_STRING;
        if (args != null && args.has("$db")) {
            dbName = args.get("$db").getAsString();
        } else if (param != null && param.has("db")) { // in "authenticate" error message 
            dbName = param.get("db").getAsString();
        } else if (param != null && param.has("ns")) {
            final String ns = param.get("ns").getAsString(); 
            dbName = ns.split("\\.")[0]; // sometimes contains "."; fallback OK.
        }
        record.setDbName(dbName);
        record.setAppUserName(Parser.UNKOWN_STRING);

        record.setSessionLocator(Parser.parseSessionLocator(data));
        record.setAccessor(Parser.parseAccessor(data));

        if (result.equals("0")) {
            record.setData(Parser.parseData(data));
        } else { // 13, 18
            record.setException(Parser.parseException(data, result));
        }

        // post populate fields:
        record.getAccessor().setServiceName(dbName); // For other data sources, service name might be different.

        // set timestamp
        String dateString = Parser.parseTimestamp(data);
        Time time = Parser.getTime(dateString);
        record.setTime(time);

        return record;
    }

    /**
     * Creates an ExceptionRecord to be used in Record, instead of Data.
     * @param data
     * @param resultCode
     * @return
     */
    private static ExceptionRecord parseException(JsonObject data, String resultCode) {
        ExceptionRecord exceptionRecord = new ExceptionRecord();
        if (resultCode.equals("13")) {
            exceptionRecord.setExceptionTypeId(Parser.EXCEPTION_TYPE_AUTHORIZATION_STRING);
            exceptionRecord.setDescription("Unauthorized to perform the operation (13)");
            //  exceptionRecord.setSqlString(); DEFER

        } else if (resultCode.equals("18")) {
            exceptionRecord.setExceptionTypeId(Parser.EXCEPTION_TYPE_AUTHENTICATION_STRING);
            exceptionRecord.setDescription("Authentication Failed (18)");
        } else { // prep for unknown error code
            exceptionRecord.setExceptionTypeId(Parser.EXCEPTION_TYPE_AUTHORIZATION_STRING);
            exceptionRecord.setDescription("Error (" + resultCode + ")"); // let Guardium handle, if you'd like
        }

        exceptionRecord.setSqlString(data.toString()); // NOTE: no redaction
        // exceptionRecord.setTimestamp() is called later, as optimization
        return exceptionRecord;
    }

    public static Accessor parseAccessor(JsonObject data) {
        Accessor accessor = new Accessor();

        accessor.setDbProtocol(Parser.DATA_PROTOCOL_STRING);
        accessor.setServerType(Parser.SERVER_TYPE_STRING);

        String dbUsers = Parser.UNKOWN_STRING;
        if (data.has("users")) {
            JsonArray users = data.getAsJsonArray("users");
            dbUsers = "";
            if (users.size() > 0) {
                for (JsonElement user : users) {
                    dbUsers += user.getAsJsonObject().get("user").getAsString() + " ";
                }
            } else if (data.has("param")) { // users array is empty in "authenticate" exception; fetch from param.user:
                final JsonObject param = data.get("param").getAsJsonObject();
                if (param.has("user")) { // in authenticate event
                    dbUsers = param.get("user").getAsString();
                }
            }
        } 
        
        accessor.setDbUser(dbUsers);

        accessor.setServerHostName(Parser.UNKOWN_STRING); // populated from Event, later
        accessor.setSourceProgram(Parser.UNKOWN_STRING); // populated from Event, later

        accessor.setLanguage(Accessor.LANGUAGE_FREE_TEXT_STRING);
        accessor.setDataType(Accessor.DATA_TYPE_GUARDIUM_SHOULD_NOT_PARSE_SQL);

        accessor.setClient_mac(Parser.UNKOWN_STRING);
        accessor.setClientHostName(Parser.UNKOWN_STRING);
        accessor.setClientOs(Parser.UNKOWN_STRING);
        accessor.setCommProtocol(Parser.UNKOWN_STRING);
        accessor.setDbProtocolVersion(Parser.UNKOWN_STRING);
        accessor.setOsUser(Parser.UNKOWN_STRING);
        accessor.setServerDescription(Parser.UNKOWN_STRING);
        accessor.setServerOs(Parser.UNKOWN_STRING);
        accessor.setServiceName(Parser.UNKOWN_STRING);

        return accessor;
    }

    private static SessionLocator parseSessionLocator(JsonObject data) {
        SessionLocator sessionLocator = new SessionLocator();
        sessionLocator.setIpv6(false);

        sessionLocator.setClientIp(Parser.UNKOWN_STRING);
        sessionLocator.setClientPort(SessionLocator.PORT_DEFAULT);
        sessionLocator.setClientIpv6(Parser.UNKOWN_STRING);

        if (data.has("remote")) {
            JsonObject remote = data.getAsJsonObject("remote");
            String address = remote.get("ip").getAsString();
            int port = remote.get("port").getAsInt();
            if (Util.isIPv6(address)) {
                sessionLocator.setIpv6(true);
                sessionLocator.setClientIpv6(address);
                sessionLocator.setClientPort(port);
                sessionLocator.setClientIp(Parser.UNKOWN_STRING);
            } else { // ipv4 
                sessionLocator.setClientIp(address);
                sessionLocator.setClientPort(port);
                sessionLocator.setClientIpv6(Parser.UNKOWN_STRING);
            }
        }
        if (data.has("local")) {
            JsonObject local = data.getAsJsonObject("local");
            String address = local.get("ip").getAsString();
            int port = local.get("port").getAsInt();
            if (Util.isIPv6(address)) {
                sessionLocator.setIpv6(true);
                sessionLocator.setServerIpv6(address);
                sessionLocator.setServerPort(port);
                sessionLocator.setServerIp(Parser.UNKOWN_STRING);
            } else { // IPv4
                sessionLocator.setServerIp(address);
                sessionLocator.setServerPort(port);
                sessionLocator.setServerIpv6(Parser.UNKOWN_STRING);
            }
        }
        return sessionLocator;
    }

    /**
     * Parses the query and returns a Data instance. Note: Setting timestamp
     * deferred, to be set by Parser.parseRecord().
     * 
     * @param inputJSON
     * @return
     * 
     * @see Data
     */
    public static Data parseData(JsonObject inputJSON) {
        Data data = new Data();
        try {
            Construct construct = parseAsConstruct(inputJSON);
            if (construct != null) {
                data.setConstruct(construct);

                if (construct.getFullSql() == null) {
                    construct.setFullSql(Parser.UNKOWN_STRING);
                }
                if (construct.getRedactedSensitiveDataSql() == null) {
                    construct.setRedactedSensitiveDataSql(Parser.UNKOWN_STRING);
                }
            }
        } catch (Exception e) {
            log.error("MongoDB filter: Error parsing JSon " + inputJSON, e);
            throw e;
        }
        return data;
    }

    public static String parseTimestamp(final JsonObject data) {
        String dateString = null;
        if (data.has("ts")) {
            dateString = data.getAsJsonObject("ts").get("$date").getAsString();
        }
        return dateString;
    }

    public static Time getTime(String dateString){
        ZonedDateTime date = ZonedDateTime.parse(dateString, DATE_TIME_FORMATTER);
        long millis = date.toInstant().toEpochMilli();
        int  minOffset = date.getOffset().getTotalSeconds()/60;
        //int  minDst = date.getOffset().getRules().isDaylightSavings(date.toInstant()) ? 60 : 0;
        return new Time(millis, minOffset, 0);
    }

    /**
     * Redact except values of objects and verbs
     */
    static JsonElement RedactWithExceptions(JsonObject data) {

        final JsonObject param = data.get("param").getAsJsonObject();
        final String command = param.get("command").getAsString();
        final JsonObject args = param.getAsJsonObject("args");
        
        final JsonElement originalCollection = args.get(command);
        final JsonElement originalDB = args.get("$db");
        
        final JsonElement redactedArgs = Parser.Redact(args);
        
        // restore common field values not to redact
        args.remove(command);
        args.add(command, originalCollection);
        args.remove("$db");
        args.add("$db", originalDB);

        return redactedArgs;
    }

    /**
     * Redact/Sanitize sensitive information. For example all field values.
     * The result can be seen in reports like "Hourly access details" or "Long running queries". 
     * Note: data is transformed/changed, so use only after you don't need the data anymore, 
     * for example, after populating as String in full_sql.
     * @param data
     * @return
     */
    static JsonElement Redact(JsonElement data) {
        // if final-leaf value (string, number) return "?"
        // else {
            // if reserved word: Redact valueRedact 
            // }
            if (data.isJsonPrimitive()) {
                return new JsonPrimitive(Parser.MASK_STRING);
            }

            else if (data.isJsonArray()) { 
                JsonArray array = data.getAsJsonArray(); 
                    for (int i=0; i<array.size(); i++) {
                        JsonElement redactedElement = Parser.Redact(array.get(i));
                        array.set(i, redactedElement);
                    }
            } 
            else if (data.isJsonObject()) {
                JsonObject object = data.getAsJsonObject();
                final Set<String> keys = object.keySet();
                final Set<String> keysCopy = new HashSet<>(); // make a copy, as keys changes on every remove/add, below  
                for (String key : keys) {
                    keysCopy.add(key);
                }
                for (String key : keysCopy) { 
                    if (!REDACTION_IGNORE_STRINGS.contains(key)) {
                        JsonElement redactedValue = Redact(object.get(key));
                        object.remove(key);
                        object.add(key, redactedValue); 
                    } 
                }
            }

            return /* changed */ data;
    }
}