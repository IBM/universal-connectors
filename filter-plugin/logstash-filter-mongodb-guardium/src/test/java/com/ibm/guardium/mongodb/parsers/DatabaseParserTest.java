package com.ibm.guardium.mongodb.parsers;

import co.elastic.logstash.api.Context;
import co.elastic.logstash.api.Event;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.ibm.guardium.mongodb.MongodbGuardiumFilter;
import com.ibm.guardium.mongodb.TestMatchListener;
import com.ibm.guardium.mongodb.parsersbytype.BaseParser;
import com.ibm.guardium.univer***REMOVED***lconnector.commons.GuardConstants;
import com.ibm.guardium.univer***REMOVED***lconnector.commons.structures.Accessor;
import com.ibm.guardium.univer***REMOVED***lconnector.commons.structures.Record;
import org.junit.Assert;
import org.junit.Test;
import org.logstash.plugins.ContextImpl;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;

public class DatabaseParserTest {

    final static Context context = new ContextImpl(null, null);
    final static MongodbGuardiumFilter filter = new MongodbGuardiumFilter("test-id", null, context);


    public static String reatTestCaseInput(String fileName) throws URISyntaxException, IOException {
        Path resourceDirectory = Paths.get("src","test","resources", "database", fileName);
        return new String(Files.readAllBytes(resourceDirectory));
    }


    public static String buildTestCaseMes***REMOVED***ge(String input) throws URISyntaxException, IOException {
        StringBuffer sb = new StringBuffer();
        sb.append(MongodbGuardiumFilter.MONGOD_AUDIT_START_SIGNAL).append(" ").append(input);
        return sb.toString();
    }


    private void validateSessionDetails(JsonObject source, Record record) {
        Assert.assertEquals("serverIp", source.get("local").getAsJsonObject().get("ip").getAsString(), record.getSessionLocator().getServerIp());
        Assert.assertEquals("serverPort", source.get("local").getAsJsonObject().get("port").getAsInt(), record.getSessionLocator().getServerPort());

        Assert.assertEquals("clientIp", source.get("remote").getAsJsonObject().get("ip").getAsString(), record.getSessionLocator().getClientIp());
        Assert.assertEquals("clientPort", source.get("remote").getAsJsonObject().get("port").getAsInt(), record.getSessionLocator().getClientPort());

        Assert.assertEquals("isIpv6", false, record.getSessionLocator().isIpv6());
    }


    private void validateAccessor(JsonObject source, Record record) {
        Assert.assertEquals("user", source.get("users").getAsJsonArray().get(0).getAsJsonObject().get("user").getAsString(), record.getAccessor().getDbUser());
        String dbName = source.get("param").getAsJsonObject().get("ns") !=null ?
                            source.get("param").getAsJsonObject().get("ns").getAsString() :
                            source.get("param").getAsJsonObject().get("db").getAsString();

        Assert.assertEquals("serviceName", dbName, record.getAccessor().getServiceName());
        Assert.assertEquals("serverType", BaseParser.SERVER_TYPE_STRING, record.getAccessor().getServerType());
        Assert.assertEquals("language", Accessor.LANGUAGE_FREE_TEXT_STRING, record.getAccessor().getLanguage());
        Assert.assertEquals("dbProtocol", Accessor.DATA_TYPE_GUARDIUM_SHOULD_NOT_PARSE_SQL, record.getAccessor().getDataType());
    }



    @Test
    public void test_createDatabase() throws Exception {

        Event e = new org.logstash.Event();
        TestMatchListener matchListener = new TestMatchListener();

        String inputMsg = reatTestCaseInput("database_create.txt");
        String testCaseMes***REMOVED***ge = buildTestCaseMes***REMOVED***ge(inputMsg);
        e.setField("mes***REMOVED***ge", testCaseMes***REMOVED***ge);

        Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);

        Assert.assertEquals(1, results.size());
        String recordString = e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME).toString();
        Record record = (new Gson()).fromJson(recordString, Record.class);
        JsonObject source = (new Gson()).fromJson(inputMsg, JsonObject.class);

        Assert.assertNotNull(record);
        // validate session
        validateSessionDetails(source, record);
        // validate access
        validateAccessor(source, record);
        // validate object details
        Assert.assertEquals("database name", source.get("param").getAsJsonObject().get("ns").getAsString(), record.getDbName());
        Assert.assertEquals("sentence verb", "createDatabase" /*source.get("atype").getAsString()*/, record.getData().getConstruct().getSentences().get(0).getVerb());
        Assert.assertEquals("object type", "database", record.getData().getConstruct().getSentences().get(0).getObjects().get(0).getType());
        Assert.assertEquals("object name", source.get("param").getAsJsonObject().get("ns").getAsString(), record.getData().getConstruct().getSentences().get(0).getObjects().get(0).getName());
        Assert.assertEquals("schema name", source.get("param").getAsJsonObject().get("ns").getAsString(), record.getData().getConstruct().getSentences().get(0).getObjects().get(0).getSchema());


    }

    @Test
    public void test_dropDatabase() throws Exception {

        Event e = new org.logstash.Event();
        TestMatchListener matchListener = new TestMatchListener();

        String inputMsg = reatTestCaseInput("database_drop.txt");
        String testCaseMes***REMOVED***ge = buildTestCaseMes***REMOVED***ge(inputMsg);
        e.setField("mes***REMOVED***ge", testCaseMes***REMOVED***ge);

        Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);

        Assert.assertEquals(1, results.size());
        String recordString = e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME).toString();
        Record record = (new Gson()).fromJson(recordString, Record.class);
        JsonObject source = (new Gson()).fromJson(inputMsg, JsonObject.class);

        Assert.assertNotNull(record);
        // validate session
        validateSessionDetails(source, record);
        // validate access
        validateAccessor(source, record);
        // validate object details
        Assert.assertEquals("database name", source.get("param").getAsJsonObject().get("ns").getAsString(), record.getDbName());
        Assert.assertEquals("sentence verb", "dropDatabase" /*source.get("atype").getAsString()*/, record.getData().getConstruct().getSentences().get(0).getVerb());
        Assert.assertEquals("object type", "database", record.getData().getConstruct().getSentences().get(0).getObjects().get(0).getType());
        Assert.assertEquals("object name", source.get("param").getAsJsonObject().get("ns").getAsString(), record.getData().getConstruct().getSentences().get(0).getObjects().get(0).getName());
        Assert.assertEquals("schema name", source.get("param").getAsJsonObject().get("ns").getAsString(), record.getData().getConstruct().getSentences().get(0).getObjects().get(0).getSchema());


    }

    @Test
    public void testParseMongoSyslog_dropAllRolesOnDatabase() throws Exception {

        Event e = new org.logstash.Event();
        TestMatchListener matchListener = new TestMatchListener();

        String inputMsg = reatTestCaseInput("database_drop_roles.txt");
        String testCaseMes***REMOVED***ge = buildTestCaseMes***REMOVED***ge(inputMsg);
        e.setField("mes***REMOVED***ge", testCaseMes***REMOVED***ge);

        Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);

        Assert.assertEquals(1, results.size());
        String recordString = e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME).toString();
        Record record = (new Gson()).fromJson(recordString, Record.class);
        JsonObject source = (new Gson()).fromJson(inputMsg, JsonObject.class);

        Assert.assertNotNull(record);
        // validate session
        validateSessionDetails(source, record);
        // validate access
        validateAccessor(source, record);
        // validate object details
        Assert.assertEquals("database name", source.get("param").getAsJsonObject().get("db").getAsString(), record.getDbName());
        Assert.assertEquals("sentence verb", "dropAllRolesFromDatabase" /*source.get("atype").getAsString()*/, record.getData().getConstruct().getSentences().get(0).getVerb());
        Assert.assertEquals("object type", "database", record.getData().getConstruct().getSentences().get(0).getObjects().get(0).getType());
        Assert.assertEquals("object name", source.get("param").getAsJsonObject().get("db").getAsString(), record.getData().getConstruct().getSentences().get(0).getObjects().get(0).getName());
        Assert.assertEquals("schema name", source.get("param").getAsJsonObject().get("db").getAsString(), record.getData().getConstruct().getSentences().get(0).getObjects().get(0).getSchema());
    }
}
