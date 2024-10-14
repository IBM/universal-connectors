package com.ibm.guardium.mongodb.parsers;

import co.elastic.logstash.api.Context;
import co.elastic.logstash.api.Event;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.ibm.guardium.mongodb.MongodbGuardiumFilter;
import com.ibm.guardium.mongodb.TestMatchListener;
import com.ibm.guardium.mongodb.parsersbytype.BaseParser;
import com.ibm.guardium.mongodb.parsersbytype.IndexParser;
import com.ibm.guardium.universalconnector.commons.GuardConstants;
import com.ibm.guardium.universalconnector.commons.structures.Accessor;
import com.ibm.guardium.universalconnector.commons.structures.UCRecord;
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

public class IndexParserTest {

    final static Context context = new ContextImpl(null, null);
    final static MongodbGuardiumFilter filter = new MongodbGuardiumFilter("test-id", null, context);


    public static String reatTestCaseInput(String fileName) throws URISyntaxException, IOException {
        Path resourceDirectory = Paths.get("src","test","resources", "index", fileName);
        return new String(Files.readAllBytes(resourceDirectory));
    }


    public static String buildTestCaseMessage(String input) throws URISyntaxException, IOException {
        StringBuffer sb = new StringBuffer();
        sb.append(MongodbGuardiumFilter.MONGOD_AUDIT_START_SIGNAL).append(" ").append(input);
        return sb.toString();
    }


    private void validateSessionDetails(JsonObject source, UCRecord record) {
        Assert.assertEquals("serverIp", source.get("local").getAsJsonObject().get("ip").getAsString(), record.getSessionLocator().getServerIp());
        Assert.assertEquals("serverPort", source.get("local").getAsJsonObject().get("port").getAsInt(), record.getSessionLocator().getServerPort());

        Assert.assertEquals("clientIp", source.get("remote").getAsJsonObject().get("ip").getAsString(), record.getSessionLocator().getClientIp());
        Assert.assertEquals("clientPort", source.get("remote").getAsJsonObject().get("port").getAsInt(), record.getSessionLocator().getClientPort());

        Assert.assertEquals("isIpv6", false, record.getSessionLocator().isIpv6());
    }


    private void validateAccessor(JsonObject source, UCRecord record) {
        String user = source.get("users")!=null && source.get("users").getAsJsonArray().size()>0 ?
                source.get("users").getAsJsonArray().get(0).getAsJsonObject().get("user").getAsString() :
                IndexParser.USER_NOT_AVAILABLE;
        Assert.assertEquals("user", user, record.getAccessor().getDbUser());
        String[] parts = CollectionParserTest.getDbAndCollectionFromParam(source);
        String dbName = parts[0];

        Assert.assertEquals("serviceName", dbName, record.getAccessor().getServiceName());
        Assert.assertEquals("serverType", BaseParser.SERVER_TYPE_STRING, record.getAccessor().getServerType());
        Assert.assertEquals("language", Accessor.LANGUAGE_FREE_TEXT_STRING, record.getAccessor().getLanguage());
        Assert.assertEquals("dbProtocol", Accessor.DATA_TYPE_GUARDIUM_SHOULD_NOT_PARSE_SQL, record.getAccessor().getDataType());
    }


    @Test
    public void test_createIndex() throws Exception {

        Event e = new org.logstash.Event();
        TestMatchListener matchListener = new TestMatchListener();

        String inputMsg = reatTestCaseInput("index_create.txt");
        String testCaseMessage = buildTestCaseMessage(inputMsg);
        e.setField("message", testCaseMessage);

        Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);

        Assert.assertEquals(1, results.size());
        String recordString = e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME).toString();
        UCRecord record = (new Gson()).fromJson(recordString, UCRecord.class);
        JsonObject source = (new Gson()).fromJson(inputMsg, JsonObject.class);

        Assert.assertNotNull(record);
        // validate session
        validateSessionDetails(source, record);
        // validate access
        validateAccessor(source, record);
        // validate object details
        String[] parts = CollectionParserTest.getDbAndCollectionFromParam(source);
        String sourceCollection = parts[1];
        String sourceDb = parts[0];

        Assert.assertEquals("database name", sourceDb, record.getDbName());
        Assert.assertEquals("sentence verb", "createIndex" /*source.get("atype").getAsString()*/, record.getData().getConstruct().getSentences().get(0).getVerb());
        Assert.assertEquals("object type", "index", record.getData().getConstruct().getSentences().get(0).getObjects().get(0).getType());
        Assert.assertEquals("object name", source.get("param").getAsJsonObject().get("indexName").getAsString(), record.getData().getConstruct().getSentences().get(0).getObjects().get(0).getName());

    }

     @Test
    public void test_createIndex2() throws Exception {

        Event e = new org.logstash.Event();
        TestMatchListener matchListener = new TestMatchListener();

        String inputMsg = reatTestCaseInput("index_create_2.txt");
        String testCaseMessage = buildTestCaseMessage(inputMsg);
        e.setField("message", testCaseMessage);

        Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);

        Assert.assertEquals(1, results.size());
        String recordString = e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME).toString();
        UCRecord record = (new Gson()).fromJson(recordString, UCRecord.class);
        JsonObject source = (new Gson()).fromJson(inputMsg, JsonObject.class);

        Assert.assertNotNull(record);
        // validate session
        validateSessionDetails(source, record);
        // validate access
        validateAccessor(source, record);
        // validate object details
        String[] parts = CollectionParserTest.getDbAndCollectionFromParam(source);
        String sourceCollection = parts[1];
        String sourceDb = parts[0];

        Assert.assertEquals("database name", sourceDb, record.getDbName());
        Assert.assertEquals("sentence verb", "createIndex" /*source.get("atype").getAsString()*/, record.getData().getConstruct().getSentences().get(0).getVerb());
        Assert.assertEquals("object type", "index", record.getData().getConstruct().getSentences().get(0).getObjects().get(0).getType());
        Assert.assertEquals("object name", source.get("param").getAsJsonObject().get("indexName").getAsString(), record.getData().getConstruct().getSentences().get(0).getObjects().get(0).getName());

    }

    @Test
    public void test_dropIndex() throws Exception {

        Event e = new org.logstash.Event();
        TestMatchListener matchListener = new TestMatchListener();

        String inputMsg = reatTestCaseInput("index_drop.txt");
        String testCaseMessage = buildTestCaseMessage(inputMsg);
        e.setField("message", testCaseMessage);

        Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);

        Assert.assertEquals(1, results.size());
        String recordString = e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME).toString();
        UCRecord record = (new Gson()).fromJson(recordString, UCRecord.class);
        JsonObject source = (new Gson()).fromJson(inputMsg, JsonObject.class);

        Assert.assertNotNull(record);
        // validate session
        validateSessionDetails(source, record);
        // validate access
        validateAccessor(source, record);
        // validate object details
        String[] parts = CollectionParserTest.getDbAndCollectionFromParam(source);
        String sourceCollection = parts[1];
        String sourceDb = parts[0];

        Assert.assertEquals("database name", sourceDb, record.getDbName());
        Assert.assertEquals("sentence verb", "dropIndex" /*source.get("atype").getAsString()*/, record.getData().getConstruct().getSentences().get(0).getVerb());
        Assert.assertEquals("object type", "index", record.getData().getConstruct().getSentences().get(0).getObjects().get(0).getType());
        Assert.assertEquals("object name", source.get("param").getAsJsonObject().get("indexName").getAsString(), record.getData().getConstruct().getSentences().get(0).getObjects().get(0).getName());
        Assert.assertEquals("schema name", sourceDb, record.getData().getConstruct().getSentences().get(0).getObjects().get(0).getSchema());
    }

}
