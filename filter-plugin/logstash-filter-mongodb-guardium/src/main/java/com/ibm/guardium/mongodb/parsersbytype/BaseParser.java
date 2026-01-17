package com.ibm.guardium.mongodb.parsersbytype;

import com.google.gson.*;
import com.ibm.guardium.mongodb.Parser;
import com.ibm.guardium.universalconnector.commons.Util;
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

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

//https://www.mongodb.com/docs/manual/reference/audit-message/

public abstract class BaseParser {


    private static Logger log = LogManager.getLogger(BaseParser.class);

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

    public String getAType(JsonObject data) {
        String atype = data.get("atype").getAsString();
        return atype;
    }

    public boolean validate(JsonObject data){
//        final String atype = getAType(data);
//        final JsonArray users = data.getAsJsonArray("users");
//
//        if ( (!atype.equals("authCheck") && !atype.equals("authenticate")) // filter handles only authCheck message template & authentication error,
//                || ( atype.equals("authenticate") && data.get("result").getAsString().equals("0")) // not auth success,
//                || ( users.size() == 0 && !atype.equals("authenticate")) )  { // nor messages with empty users array, as it's an internal command (except authenticate, which states in param.user)
//            return false;
//        }
        return true;
    }

    public Record parseRecord(final JsonObject data){

        if (!validate(data)){
            return null;
        }

        Record record = new Record();

        final JsonObject param = data.get("param").getAsJsonObject();
        final JsonObject args = param==null ? null : param.getAsJsonObject("args");

        String sessionId = parseSessionId(args);
        record.setSessionId(sessionId);

        String dbName = parseDatabaseName(param, args, data);
        record.setDbName(dbName);

        String userName = parseApplicationUser(data);
        record.setAppUserName(userName);

        record.setSessionLocator(parseSessionLocator(data));
        record.setAccessor(parseAccessor(data));

        String result = data.get("result").getAsString(); // 0 success; 13/18 errors
        if (result.equals("0")) {
            record.setData(parseData(data));
        } else { // 13, 18
            record.setException(parseException(data, result));
        }

        // post populate fields:
        record.getAccessor().setServiceName(dbName); // For other data sources, service name might be different.

        // set timestamp
        String dateString = parseTimestamp(data);
        Time time = getTime(dateString);
        record.setTime(time);


        return record;
    }


    protected String sessionLocatorToString(SessionLocator sessionLocator) {
        StringBuffer sb = new StringBuffer();
        sb.append(sessionLocator.getServerIp())
                .append(sessionLocator.getClientIp())
                .append(sessionLocator.getClientPort())
                .append(sessionLocator.getServerPort())
                .append(sessionLocator.getServerIpv6())
                .append(sessionLocator.getClientIpv6());
        return sb.toString();
    }

    protected String accessorToString(Accessor accessor){
        StringBuffer sb = new StringBuffer();
        sb.append(accessor.getDbUser())
                .append(accessor.getClientOs())
                .append(accessor.getServerOs())
                .append(accessor.getClient_mac())
                .append(accessor.getServerType())
                .append(accessor.getDataType())
                .append(accessor.getLanguage())
                .append(accessor.getServiceName())
                .append(accessor.getDbProtocol())
                .append(accessor.getOsUser())
                .append(accessor.getSourceProgram());
        return sb.toString();
    }

    protected String parseSessionId(JsonObject args){
        // Session Id is kept as empty string by default
        // If details of SessionId is available from the event, it will be used.
        // Else sniffer will generate the session Id.
        //
        String sessionId = UNKOWN_STRING;
        if (args != null && args.has("lsid")) {
            JsonObject lsid = args.getAsJsonObject("lsid");
            sessionId = lsid.getAsJsonObject("id").get("$binary").getAsString();
        }
        return sessionId;
    }

    protected String parseDatabaseName(JsonObject param, JsonObject args, JsonObject wholeData) {
        String dbName = UNKOWN_STRING;
        if (args != null && args.has("$db")) {
            dbName = args.get("$db").getAsString();
        } else {
            if (param != null && param.has("db")) { // in "authenticate" error message
                dbName = param.get("db").getAsString();
            } else if (param != null && param.has("ns")) {
                final String ns = param.get("ns").getAsString();
                dbName = ns.split("\\.")[0]; // sometimes contains "."; fallback OK.
            }
        }
        return dbName;
    }

    protected String parseApplicationUser(JsonObject data){
        String userStr = UNKOWN_STRING;
        JsonArray users = data.has("users") ? data.getAsJsonArray("users") : null;
        if (users!=null && users.size()>0){
            StringBuffer sb = new StringBuffer();
            for (JsonElement user : users) {
                if (sb.length()>0){
                    sb.append(" ");
                }
                sb.append(user.getAsJsonObject().get("user").getAsString());
            }
            userStr = sb.toString();
        }
        return userStr;
    }

    /**
     * Parses a MongoDB native audit sent over syslog. Format looks as the
     * database.profiler.
     *
     * @param data
     * @return
     */
    public String parse(final JsonObject data) {

        try {

            final Construct construct = parseAsConstruct(data);

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
    public Construct parseAsConstruct(final JsonObject data) {
        final Sentence sentence = parseSentence(data);

        final Construct construct = new Construct();
        construct.getSentences().add(sentence);

        construct.setFullSql(data.toString());

        redactWithExceptions(data); // Warning: overwrites data.param.args

        construct.setRedactedSensitiveDataSql(data.toString());
        return construct;
    }

    protected abstract Sentence parseSentence(final JsonObject data);


    /**
     * Creates an ExceptionRecord to be used in Record, instead of Data.
     * @param data
     * @param resultCode
     * @return
     */
    private static ExceptionRecord parseException(JsonObject data, String resultCode) {
        ExceptionRecord exceptionRecord = new ExceptionRecord();
        if (resultCode.equals("13")) {
            exceptionRecord.setExceptionTypeId(EXCEPTION_TYPE_AUTHORIZATION_STRING);
            exceptionRecord.setDescription("Unauthorized to perform the operation (13)");
            //  exceptionRecord.setSqlString(); DEFER

        } else if (resultCode.equals("18")) {
            exceptionRecord.setExceptionTypeId(EXCEPTION_TYPE_AUTHENTICATION_STRING);
            exceptionRecord.setDescription("Authentication Failed (18)");
        } else if(resultCode.equals("11")) {
            exceptionRecord.setExceptionTypeId(Parser.EXCEPTION_TYPE_AUTHENTICATION_STRING);
            exceptionRecord.setDescription("User Not Found (11)");
        } else { // prep for unknown error code
            exceptionRecord.setExceptionTypeId(EXCEPTION_TYPE_AUTHORIZATION_STRING);
            exceptionRecord.setDescription("Error (" + resultCode + ")"); // let Guardium handle, if you'd like
        }

        exceptionRecord.setSqlString(data.toString()); // NOTE: no redaction
        // exceptionRecord.setTimestamp() is called later, as optimization
        return exceptionRecord;
    }

    public Accessor parseAccessor(JsonObject data) {
        Accessor accessor = new Accessor();

        accessor.setDbProtocol(DATA_PROTOCOL_STRING);
        accessor.setServerType(SERVER_TYPE_STRING);

        String dbUsers = UNKOWN_STRING;
        if (data.has("users")) {
            JsonArray users = data.getAsJsonArray("users");
            StringBuffer dbUsersSb = new StringBuffer();
            if (users.size() > 0) {
                for (JsonElement user : users) {
                    if (dbUsersSb.length()>0){
                        dbUsersSb.append(" ");
                    }
                    dbUsersSb.append(user.getAsJsonObject().get("user").getAsString());
                }
                dbUsers = dbUsersSb.toString();
            }
        }
        if (dbUsers.trim().length()==0 && data.has("param")) { // users array is empty in "authenticate" exception; fetch from param.user:
            final JsonObject param = data.get("param").getAsJsonObject();
            if (param.has("user")) { // in authenticate event
                dbUsers = param.get("user").getAsString();
            }
        }

        accessor.setDbUser(dbUsers);

        accessor.setServerHostName(UNKOWN_STRING); // populated from Event, later
        accessor.setSourceProgram(UNKOWN_STRING); // populated from Event, later

        accessor.setLanguage(Accessor.LANGUAGE_FREE_TEXT_STRING);
        accessor.setDataType(Accessor.DATA_TYPE_GUARDIUM_SHOULD_NOT_PARSE_SQL);

        accessor.setClient_mac(UNKOWN_STRING);
        accessor.setClientHostName(UNKOWN_STRING);
        accessor.setClientOs(UNKOWN_STRING);
        accessor.setCommProtocol(UNKOWN_STRING);
        accessor.setDbProtocolVersion(UNKOWN_STRING);
        accessor.setOsUser(UNKOWN_STRING);
        accessor.setServerDescription(UNKOWN_STRING);
        accessor.setServerOs(UNKOWN_STRING);
        accessor.setServiceName(UNKOWN_STRING);

        return accessor;
    }

    private static SessionLocator parseSessionLocator(JsonObject data) {
        SessionLocator sessionLocator = new SessionLocator();
        sessionLocator.setIpv6(false);

        sessionLocator.setClientIp(UNKOWN_STRING);
        sessionLocator.setClientPort(SessionLocator.PORT_DEFAULT);
        sessionLocator.setClientIpv6(UNKOWN_STRING);

        if (data.has("remote")) {
            JsonObject remote = data.getAsJsonObject("remote");
            String address = remote.get("ip").getAsString();
            int port = remote.get("port").getAsInt();
            if (Util.isIPv6(address)) {
                sessionLocator.setIpv6(true);
                sessionLocator.setClientIpv6(address);
                sessionLocator.setClientPort(port);
                sessionLocator.setClientIp(UNKOWN_STRING);
            } else { // ipv4 
                sessionLocator.setClientIp(address);
                sessionLocator.setClientPort(port);
                sessionLocator.setClientIpv6(UNKOWN_STRING);
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
                sessionLocator.setServerIp(UNKOWN_STRING);
            } else { // IPv4
                sessionLocator.setServerIp(address);
                sessionLocator.setServerPort(port);
                sessionLocator.setServerIpv6(UNKOWN_STRING);
            }
        }
        return sessionLocator;
    }

    /**
     * Parses the query and returns a Data instance. Note: Setting timestamp
     * deferred, to be set by parseRecord().
     *
     * @param inputJSON
     * @return
     *
     * @see Data
     */
    public Data parseData(JsonObject inputJSON) {
        Data data = new Data();
        try {
            Construct construct = parseAsConstruct(inputJSON);
            if (construct != null) {
                data.setConstruct(construct);

                if (construct.getFullSql() == null) {
                    construct.setFullSql(UNKOWN_STRING);
                }
                if (construct.getRedactedSensitiveDataSql() == null) {
                    construct.setRedactedSensitiveDataSql(UNKOWN_STRING);
                }
            }
        } catch (Exception e) {
            log.error("MongoDB filter: Error parsing JSon " + inputJSON, e);
            throw e;
        }
        return data;
    }

    public String parseTimestamp(final JsonObject data) {
        String dateString = null;
        if (data.has("ts")) {
            dateString = data.getAsJsonObject("ts").get("$date").getAsString();
        }
        return dateString;
    }

    public Time getTime(String dateString){
        ZonedDateTime date = ZonedDateTime.parse(dateString, DATE_TIME_FORMATTER);
        long millis = date.toInstant().toEpochMilli();
        int  minOffset = date.getOffset().getTotalSeconds()/60;
        //int  minDst = date.getOffset().getRules().isDaylightSavings(date.toInstant()) ? 60 : 0;
        return new Time(millis, minOffset, 0);
    }

    /**
     * Redact except values of objects and verbs
     */
    public JsonElement redactWithExceptions(JsonObject data) {
        return data;
    }

    /**
     * Redact/Sanitize sensitive information. For example all field values.
     * The result can be seen in reports like "Hourly access details" or "Long running queries". 
     * Note: data is transformed/changed, so use only after you don't need the data anymore, 
     * for example, after populating as String in full_sql.
     * @param data
     * @return
     */
    public JsonElement redact(JsonElement data) {
        // if final-leaf value (string, number) return "?"
        // else {
        // if reserved word: Redact valueRedact 
        // }
        if (data.isJsonPrimitive()) {
            return new JsonPrimitive(MASK_STRING);
        }

        else if (data.isJsonArray()) {
            JsonArray array = data.getAsJsonArray();
            for (int i=0; i<array.size(); i++) {
                JsonElement redactedElement = redact(array.get(i));
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
                    JsonElement redactedValue = redact(object.get(key));
                    object.remove(key);
                    object.add(key, redactedValue);
                }
            }
        }

        return /* changed */ data;
    }
}
