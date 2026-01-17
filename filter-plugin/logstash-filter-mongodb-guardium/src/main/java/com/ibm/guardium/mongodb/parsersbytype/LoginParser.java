package com.ibm.guardium.mongodb.parsersbytype;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Optional;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.ibm.guardium.mongodb.AType;
import com.ibm.guardium.universalconnector.commons.structures.Accessor;
import com.ibm.guardium.universalconnector.commons.structures.Construct;
import com.ibm.guardium.universalconnector.commons.structures.Data;
import com.ibm.guardium.universalconnector.commons.structures.Accessor;
import com.ibm.guardium.universalconnector.commons.structures.Construct;
import com.ibm.guardium.universalconnector.commons.structures.Data;
import com.ibm.guardium.universalconnector.commons.structures.ExceptionRecord;
import com.ibm.guardium.universalconnector.commons.structures.Record;
import com.ibm.guardium.universalconnector.commons.structures.Sentence;
import com.ibm.guardium.universalconnector.commons.structures.SentenceObject;
import com.ibm.guardium.universalconnector.commons.structures.SessionLocator;
import com.ibm.guardium.universalconnector.commons.structures.Time;
import com.ibm.guardium.universalconnector.commons.structures.Sentence;
import com.ibm.guardium.universalconnector.commons.structures.SentenceObject;
import com.ibm.guardium.universalconnector.commons.structures.SessionLocator;
import com.ibm.guardium.universalconnector.commons.structures.Time;

public class LoginParser extends BaseParser {

    private static final String PARAM = "param";
    private static final String UNKNOWN = "";
    private static final String UUID = "uuid";
    private static final String NOT_AVAILABLE = "N.A.";

    @Override
    public boolean validate(JsonObject data) {

        Optional<JsonElement> optParam = Optional.ofNullable(data.get(PARAM));

        JsonObject param = optParam.orElseGet(JsonObject::new).getAsJsonObject();

        return param.size() > 0;
    }

    @Override
    public Record parseRecord(JsonObject data) {
        Record record = new Record();

        String sessionId = getSessionID(data);
        String dbName = getInitialUser(data).get("db").getAsString();
        String appUserName = getInitialUser(data).get("user").getAsString();

        Time time = getTime(data);

        SessionLocator sessionLocator = getSessionLocator(data);

        Accessor accessor = getAccessor(data);

        Data d = getData(data);

        record.setSessionId(sessionId);
        record.setDbName(dbName);
        record.setAppUserName(appUserName);
        record.setTime(time);
        record.setSessionLocator(sessionLocator);
        record.setAccessor(accessor);
        record.setData(d);
        record.setException(null);

        return record;
    }

    private Data getData(JsonObject data) {
        Data d = new Data();

        Construct construct = new Construct();
        Sentence sentence = new Sentence("logout");

        SentenceObject obj = new SentenceObject(getInitialUser(data).get("user").getAsString());
        obj.setType("user");
        obj.setSchema(getInitialUser(data).get("db").getAsString());

        ArrayList<SentenceObject> listSentanceObject = new ArrayList<>(1);
        listSentanceObject.add(obj);

        sentence.setObjects(listSentanceObject);

        ArrayList<Sentence> sentances = new ArrayList<>();
        sentances.add(sentence);

        construct.setSentences(sentances);
        construct.setFullSql(data.toString());
        construct.setRedactedSensitiveDataSql(data.toString());

        d.setConstruct(construct);
        d.setOriginalSqlCommand(null);

        return d;
    }

    private Accessor getAccessor(JsonObject data) {
        Accessor accesor = new Accessor();

        String dbUser = getInitialUser(data).get("user").getAsString();
        accesor.setDbUser(dbUser);
        accesor.setServerType("MongoDB");
        accesor.setDbProtocol("MongoDB native audit");

        String serviceName = getInitialUser(data).get("db").getAsString();
        accesor.setServiceName(serviceName);

        accesor.setLanguage(Accessor.LANGUAGE_FREE_TEXT_STRING);
        accesor.setDataType(Accessor.DATA_TYPE_GUARDIUM_SHOULD_NOT_PARSE_SQL);


        Optional<JsonElement> optSourceProgram = Optional.ofNullable(data.get("source_program"));
        String sourceProgram = UNKNOWN;

        if(optSourceProgram.isPresent()) {
            sourceProgram = optSourceProgram.get().getAsString();
        }

        accesor.setSourceProgram(sourceProgram);

        return accesor;
    }

    private SessionLocator getSessionLocator(JsonObject data) {
        SessionLocator sessionLocator = new SessionLocator();

        Optional<JsonElement> optLocal = Optional.ofNullable(data.get("local"));

        JsonObject server = optLocal.orElseGet(JsonObject::new).getAsJsonObject();

        Optional<JsonElement> optServerIP = Optional.ofNullable(server.get("ip"));
        Optional<JsonElement> optServerPort = Optional.ofNullable(server.get("port"));

        String serverIp = UNKNOWN;
        if(optServerIP.isPresent()) {
            serverIp = optServerIP.get().getAsString();
        }

        Integer serverPort = 0;
        if(optServerPort.isPresent()) {
            serverPort = optServerPort.get().getAsInt();
        }


        sessionLocator.setServerIp(serverIp);
        sessionLocator.setServerPort(serverPort);

        Optional<JsonElement> optRemote = Optional.ofNullable(data.get("remote"));

        JsonObject client = optRemote.orElseGet(JsonObject::new).getAsJsonObject();

        Optional<JsonElement> optClientIP = Optional.ofNullable(client.get("ip"));
        Optional<JsonElement> optClientPort = Optional.ofNullable(client.get("port"));

        String clientIp = UNKNOWN;
        if(optClientIP.isPresent()){
            clientIp = optClientIP.get().getAsString();
        }

        Integer clientPort = 0;
        if(optClientPort.isPresent()) {
            clientPort = optClientPort.get().getAsInt();
        }

        sessionLocator.setClientIp(clientIp);
        sessionLocator.setClientPort(clientPort);

        return sessionLocator;
    }

    private Time getTime(JsonObject data) {

        Optional<JsonElement> optTimestamp = Optional.ofNullable(data.get("ts"));

        JsonObject ts = optTimestamp.orElseGet(JsonObject::new).getAsJsonObject();

        Optional<JsonElement> optDate = Optional.ofNullable(ts.get("$date"));

        String date = UNKNOWN;
        if(optDate.isPresent()) {
            date = optDate.get().getAsString();
        }

        ZonedDateTime dateTime = ZonedDateTime.parse(date);
        long epochTime = dateTime.toInstant().toEpochMilli();

        Time t = new Time();
        t.setTimstamp(epochTime);
        t.setMinDst(0);
        t.setMinOffsetFromGMT(0);

        return t;
    }

    private JsonObject getInitialUser(JsonObject data) {

        Optional<JsonElement> optParam = Optional.ofNullable(data.get(PARAM));

        JsonObject param = optParam.orElseGet(JsonObject::new).getAsJsonObject();

        Optional<JsonElement> optInitialUsers = Optional.ofNullable(param.get("initialUsers"));
        JsonArray initialUsers = optInitialUsers.orElseGet(JsonArray::new).getAsJsonArray();

        JsonObject initialUser = initialUsers.get(0).getAsJsonObject();

        return initialUser;
    }

    private String getSessionID(JsonObject data) {
        Optional<JsonElement> optuuid = Optional.ofNullable(data.get(UUID));

        JsonObject uuid = optuuid.orElseGet(JsonObject::new).getAsJsonObject();

        Optional<JsonElement> optBinary = Optional.ofNullable(uuid.get("$binary"));
        String sessionId = UNKNOWN;
        if(optBinary.isPresent()) {
            sessionId = optBinary.get().getAsString();
        }

        return sessionId;
    }

    @Override
    protected Sentence parseSentence(JsonObject data) {
        Sentence sentence = new Sentence(AType.LOGOUT.getPropertyValue());
        return sentence;
    }

}

