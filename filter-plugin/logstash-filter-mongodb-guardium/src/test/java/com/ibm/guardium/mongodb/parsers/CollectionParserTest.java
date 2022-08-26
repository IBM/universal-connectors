package com.ibm.guardium.mongodb.parsers;

import co.elastic.logstash.api.Context;
import co.elastic.logstash.api.Event;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.ibm.guardium.mongodb.MongodbGuardiumFilter;
import com.ibm.guardium.mongodb.TestMatchListener;
import com.ibm.guardium.universalconnector.commons.GuardConstants;
import com.ibm.guardium.universalconnector.commons.structures.Record;
import org.junit.Assert;
import org.junit.Test;
import org.logstash.plugins.ContextImpl;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;

public class CollectionParserTest {

    final static Context context = new ContextImpl(null, null);
    final static MongodbGuardiumFilter filter = new MongodbGuardiumFilter("test-id", null, context);


    public static String reatTestCaseInput(String fileName) throws Exception {
        Path resourceDirectory = Paths.get("src","test","resources", "collection", fileName);
        return new String(Files.readAllBytes(resourceDirectory));
    }


    public static String buildTestCaseMessage(String input){
        StringBuffer sb = new StringBuffer();
        sb.append(MongodbGuardiumFilter.MONGOD_AUDIT_START_SIGNAL).append(" ").append(input);
        return sb.toString();
    }


    @Test
    public void test_createCollection() throws Exception {

        Event e = new org.logstash.Event();
        TestMatchListener matchListener = new TestMatchListener();

        String inputMsg = reatTestCaseInput("collection_create.txt");
        String testCaseMessage = buildTestCaseMessage(inputMsg);
        e.setField("message", testCaseMessage);

        Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);

        Assert.assertEquals(1, results.size());
        String recordString = e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME).toString();
        Record record = (new Gson()).fromJson(recordString, Record.class);
        JsonObject source = (new Gson()).fromJson(inputMsg, JsonObject.class);

        Assert.assertNotNull(record);

        // validate object details
        String[] parts = source.get("param").getAsJsonObject().get("ns").getAsString().split("\\.");
        String sourceCollection = parts[1];
        String sourceDb = parts[0];

        Assert.assertEquals("database name", sourceDb, record.getDbName());
        Assert.assertEquals("sentence verb", "createCollection" /*source.get("atype").getAsString()*/, record.getData().getConstruct().getSentences().get(0).getVerb());
        Assert.assertEquals("object type", "collection", record.getData().getConstruct().getSentences().get(0).getObjects().get(0).getType());
        Assert.assertEquals("object name", sourceCollection, record.getData().getConstruct().getSentences().get(0).getObjects().get(0).getName());
        Assert.assertEquals("schema name", sourceDb, record.getData().getConstruct().getSentences().get(0).getObjects().get(0).getSchema());


    }

    @Test
    public void test_dropCollection() throws Exception {

        Event e = new org.logstash.Event();
        TestMatchListener matchListener = new TestMatchListener();

        String inputMsg = reatTestCaseInput("collection_drop.txt");
        String testCaseMessage = buildTestCaseMessage(inputMsg);
        e.setField("message", testCaseMessage);

        Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);

        Assert.assertEquals(1, results.size());
        String recordString = e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME).toString();
        Record record = (new Gson()).fromJson(recordString, Record.class);
        JsonObject source = (new Gson()).fromJson(inputMsg, JsonObject.class);

        Assert.assertNotNull(record);

        // validate object details
        String[] parts = getDbAndCollectionFromParam(source);
        String sourceCollection = parts[1];
        String sourceDb = parts[0];

        Assert.assertEquals("database name", sourceDb, record.getDbName());
        Assert.assertEquals("sentence verb", "dropCollection" /*source.get("atype").getAsString()*/, record.getData().getConstruct().getSentences().get(0).getVerb());
        Assert.assertEquals("object type", "collection", record.getData().getConstruct().getSentences().get(0).getObjects().get(0).getType());
        Assert.assertEquals("object name", sourceCollection, record.getData().getConstruct().getSentences().get(0).getObjects().get(0).getName());
        Assert.assertEquals("schema name", sourceDb, record.getData().getConstruct().getSentences().get(0).getObjects().get(0).getSchema());
    }

    @Test
    public void test_renameCollection() throws Exception {

        Event e = new org.logstash.Event();
        TestMatchListener matchListener = new TestMatchListener();

        String inputMsg = reatTestCaseInput("collection_rename.txt");
        String testCaseMessage = buildTestCaseMessage(inputMsg);
        e.setField("message", testCaseMessage);

        Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);

        Assert.assertEquals(1, results.size());
        String recordString = e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME).toString();
        Record record = (new Gson()).fromJson(recordString, Record.class);
        JsonObject source = (new Gson()).fromJson(inputMsg, JsonObject.class);

        Assert.assertNotNull(record);

        // validate object details
        String sourceDb = "dbgbdi1";

        Assert.assertEquals("database name", sourceDb, record.getDbName());
        Assert.assertEquals("sentence verb", "renameCollection" /*source.get("atype").getAsString()*/, record.getData().getConstruct().getSentences().get(0).getVerb());

        Assert.assertEquals("object type", "collection", record.getData().getConstruct().getSentences().get(0).getObjects().get(0).getType());
        Assert.assertEquals("object name", "collgbdi1", record.getData().getConstruct().getSentences().get(0).getObjects().get(0).getName());
        Assert.assertEquals("schema name", sourceDb, record.getData().getConstruct().getSentences().get(0).getObjects().get(0).getSchema());

        Assert.assertEquals("object type", "collection", record.getData().getConstruct().getSentences().get(0).getObjects().get(1).getType());
        Assert.assertEquals("object name", "collgbdi2", record.getData().getConstruct().getSentences().get(0).getObjects().get(1).getName());
        Assert.assertEquals("schema name", sourceDb, record.getData().getConstruct().getSentences().get(0).getObjects().get(1).getSchema());
    }

    public static String[] getDbAndCollectionFromParam(JsonObject source){
        return source.get("param").getAsJsonObject().get("ns").getAsString().split("\\.");
    }
}
