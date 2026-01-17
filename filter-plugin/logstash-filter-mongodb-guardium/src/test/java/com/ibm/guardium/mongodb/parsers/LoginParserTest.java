package com.ibm.guardium.mongodb.parsers;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ibm.guardium.mongodb.parsersbytype.BaseParser;
import com.ibm.guardium.mongodb.parsersbytype.LoginParser;
import com.ibm.guardium.universalconnector.commons.structures.Accessor;
import com.ibm.guardium.universalconnector.commons.structures.Construct;
import com.ibm.guardium.universalconnector.commons.structures.Data;
import com.ibm.guardium.universalconnector.commons.structures.ExceptionRecord;
import com.ibm.guardium.universalconnector.commons.structures.Record;
import com.ibm.guardium.universalconnector.commons.structures.Sentence;
import com.ibm.guardium.universalconnector.commons.structures.SentenceObject;
import com.ibm.guardium.universalconnector.commons.structures.SessionLocator;
import com.ibm.guardium.universalconnector.commons.structures.Time;

import org.junit.Assert;
import org.junit.Test;

import java.time.ZonedDateTime;

/**
 * LoginParserTest
 */
public class LoginParserTest {

    public String eventString = "{\"atype\":\"logout\",\"ts\":{\"$date\":\"2024-05-03T05:11:34.475+00:00\"},"+
            "\"uuid\":{\"$binary\":\"yYg9ePcSR4Cogc6p+2h8RA==\",\"$type\":\"04\"},\"local\":"+
            "{\"ip\":\"192.168.254.5\",\"port\":27017},\"remote\":"+
            "{\"ip\":\"103.232.239.145\",\"port\":49949},\"users\":[],\"roles\":[{\"role\":\"backup\","+
            "\"db\":\"admin\"},{\"role\":\"dbAdminAnyDatabase\",\"db\":\"admin\"},"+
            "{\"role\":\"enableSharding\",\"db\":\"admin\"},{\"role\":\"clusterMonitor\",\"db\":"+
            "\"admin\"},{\"role\":\"readWriteAnyDatabase\",\"db\":\"admin\"},{\"role\":\"atlasAdmin\","+
            "\"db\":\"admin\"}],\"param\":{\"reason\":\"Client has disconnected\",\"initialUsers\":"+
            "[{\"user\":\"userAdmin\",\"db\":\"admin\"}],\"updatedUsers\":[]},\"result\":0}";


    @Test
    public void tesParseRecord() {
        JsonObject event = JsonParser.parseString(eventString).getAsJsonObject();
        LoginParser parser = new LoginParser();

        Record record = parser.parseRecord(event);
        Time time = record.getTime();
        Accessor accessor = record.getAccessor();
        SessionLocator locator = record.getSessionLocator();
        Data data = record.getData();

        String date = event.get("ts").getAsJsonObject().get("$date").getAsString();
        ZonedDateTime t = ZonedDateTime.parse(date);
        Assert.assertEquals(time.getTimstamp(), t.toInstant().toEpochMilli());
        Assert.assertEquals(time.getMinDst(), 0);
        Assert.assertEquals(time.getMinOffsetFromGMT(), 0);

        String serverIP = event.get("local").getAsJsonObject().get("ip").getAsString();
        int serverPort = event.get("local").getAsJsonObject().get("port").getAsInt();
        String clientIP = event.get("remote").getAsJsonObject().get("ip").getAsString();
        int clientPort = event.get("remote").getAsJsonObject().get("port").getAsInt();

        Assert.assertEquals(locator.getClientIp(), clientIP);
        Assert.assertEquals(locator.getClientPort(), clientPort);
        Assert.assertEquals(locator.getServerIp(), serverIP);
        Assert.assertEquals(locator.getServerPort(), serverPort);

        JsonObject initialUser = event.get("param").getAsJsonObject().get("initialUsers")
                .getAsJsonArray().get(0)
                .getAsJsonObject();
        String dbUser = initialUser.get("user").getAsString();
        String dbName = initialUser.get("db").getAsString();

        Assert.assertEquals(accessor.getDbUser(), dbUser);
        Assert.assertEquals(accessor.getServiceName(), dbName);
        Assert.assertEquals(accessor.getServerType(), "MongoDB");
        Assert.assertEquals(accessor.getDbProtocol(), "MongoDB native audit");
        Assert.assertEquals(accessor.getLanguage(), "FREE_TEXT");
        Assert.assertEquals(accessor.getDataType(), "CONSTRUCT");

        String verb = event.get("atype").getAsString();
        Assert.assertEquals(data.getConstruct()
                .getSentences().get(0)
                .getObjects().get(0)
                .getName(), dbUser);

        Assert.assertEquals(data.getConstruct()
                .getSentences().get(0)
                .getObjects().get(0)
                .getType(), "user");

        Assert.assertEquals(data.getConstruct()
                .getSentences().get(0)
                .getVerb(), verb);

        String sessionId = event.get("uuid").getAsJsonObject().get("$binary").getAsString();
        System.out.println(sessionId);
        Assert.assertEquals(record.getSessionId().toString(), sessionId.toString());
        Assert.assertEquals(record.getDbName(), dbName);
        Assert.assertEquals(record.getAppUserName(), dbUser);

    }
}