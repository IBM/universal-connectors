package com.ibm.guardium.mongodb.parsers;

import co.elastic.logstash.api.Context;
import co.elastic.logstash.api.Event;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.ibm.guardium.mongodb.MongodbGuardiumFilter;
import com.ibm.guardium.mongodb.TestMatchListener;
import com.ibm.guardium.mongodb.parsersbytype.RoleParser;
import com.ibm.guardium.universalconnector.commons.GuardConstants;
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
import org.junit.Assert;
import org.junit.Test;
import org.logstash.plugins.ContextImpl;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class RoleParserTest {

    final static Context context = new ContextImpl(null, null);
    final static MongodbGuardiumFilter filter = new MongodbGuardiumFilter("test-id", null, context);


    public static String reatTestCaseInput(String fileName) throws Exception {
        Path resourceDirectory = Paths.get("src","test","resources", "role", fileName);
        return new String(Files.readAllBytes(resourceDirectory));
    }


    public static String buildTestCaseMessage(String input){
        StringBuffer sb = new StringBuffer();
        sb.append(MongodbGuardiumFilter.MONGOD_AUDIT_START_SIGNAL).append(" ").append(input);
        return sb.toString();
    }


    @Test
    public void test_createRole_db() throws Exception {

        Event e = new org.logstash.Event();
        TestMatchListener matchListener = new TestMatchListener();

        String inputMsg = reatTestCaseInput("role_create.txt");
        String testCaseMessage = buildTestCaseMessage(inputMsg);
        e.setField("message", testCaseMessage);

        Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);

        Assert.assertEquals(1, results.size());
        String recordString = e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME).toString();
        Record record = (new Gson()).fromJson(recordString, Record.class);
        JsonObject source = (new Gson()).fromJson(inputMsg, JsonObject.class);

        Assert.assertNotNull(record);

        // validate object details
        Assert.assertEquals("database name", source.get("param").getAsJsonObject().get("db").getAsString(), record.getDbName());
        Assert.assertEquals("sentence verb", "createRole", record.getData().getConstruct().getSentences().get(0).getVerb());
        Assert.assertEquals("object type", "role", record.getData().getConstruct().getSentences().get(0).getObjects().get(0).getType());
        Assert.assertEquals("object name", source.get("param").getAsJsonObject().get("role").getAsString(), record.getData().getConstruct().getSentences().get(0).getObjects().get(0).getName());
        Assert.assertEquals("schema name", source.get("param").getAsJsonObject().get("db").getAsString(), record.getData().getConstruct().getSentences().get(0).getObjects().get(0).getSchema());

        // check roles given to the role
        validateRoles( source,  record, true);

        // check priviliges
        validatePrivileges(record, "dbgbdi1", "collgbdi1", "collection", Arrays.asList(new String[]{"insert", "remove", "update"}), true);
    }

    @Test
    public void test_createRole_cluster() throws Exception {

        Event e = new org.logstash.Event();
        TestMatchListener matchListener = new TestMatchListener();

        String inputMsg = reatTestCaseInput("role_create_cluster.txt");
        String testCaseMessage = buildTestCaseMessage(inputMsg);
        e.setField("message", testCaseMessage);

        Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);

        Assert.assertEquals(1, results.size());
        String recordString = e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME).toString();
        Record record = (new Gson()).fromJson(recordString, Record.class);
        JsonObject source = (new Gson()).fromJson(inputMsg, JsonObject.class);

        Assert.assertNotNull(record);

        // validate object details
        Assert.assertEquals("database name", source.get("param").getAsJsonObject().get("db").getAsString(), record.getDbName());
        Assert.assertEquals("sentence verb", "createRole", record.getData().getConstruct().getSentences().get(0).getVerb());
        Assert.assertEquals("object type", "role", record.getData().getConstruct().getSentences().get(0).getObjects().get(0).getType());
        Assert.assertEquals("object name", source.get("param").getAsJsonObject().get("role").getAsString(), record.getData().getConstruct().getSentences().get(0).getObjects().get(0).getName());
        Assert.assertEquals("schema name", source.get("param").getAsJsonObject().get("db").getAsString(), record.getData().getConstruct().getSentences().get(0).getObjects().get(0).getSchema());

        // check roles given to the role
        validateRoles( source,  record, true);

        // check priviliges
        validatePrivileges(record, "cluster", "cluster", "cluster", Arrays.asList(new String[]{"addShard"}), true);
    }

    @Test
    public void test_dropRole() throws Exception {

        Event e = new org.logstash.Event();
        TestMatchListener matchListener = new TestMatchListener();

        String inputMsg = reatTestCaseInput("role_drop.txt");
        String testCaseMessage = buildTestCaseMessage(inputMsg);
        e.setField("message", testCaseMessage);

        Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);

        Assert.assertEquals(1, results.size());
        String recordString = e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME).toString();
        Record record = (new Gson()).fromJson(recordString, Record.class);
        JsonObject source = (new Gson()).fromJson(inputMsg, JsonObject.class);

        Assert.assertNotNull(record);

        // validate object details
        Assert.assertEquals("database name", source.get("param").getAsJsonObject().get("db").getAsString(), record.getDbName());
        Assert.assertEquals("sentence verb", "dropRole", record.getData().getConstruct().getSentences().get(0).getVerb());
        Assert.assertEquals("object type", "role", record.getData().getConstruct().getSentences().get(0).getObjects().get(0).getType());
        Assert.assertEquals("object name", source.get("param").getAsJsonObject().get("role").getAsString(), record.getData().getConstruct().getSentences().get(0).getObjects().get(0).getName());
        Assert.assertEquals("schema name", source.get("param").getAsJsonObject().get("db").getAsString(), record.getData().getConstruct().getSentences().get(0).getObjects().get(0).getSchema());

    }

    @Test
    public void test_revokeRole() throws Exception {

        Event e = new org.logstash.Event();
        TestMatchListener matchListener = new TestMatchListener();

        String inputMsg = reatTestCaseInput("role_revokePriviliges.txt");
        String testCaseMessage = buildTestCaseMessage(inputMsg);
        e.setField("message", testCaseMessage);

        Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);

        Assert.assertEquals(1, results.size());
        String recordString = e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME).toString();
        Record record = (new Gson()).fromJson(recordString, Record.class);
        JsonObject source = (new Gson()).fromJson(inputMsg, JsonObject.class);

        Assert.assertNotNull(record);

        // validate object details
        Assert.assertEquals("database name", source.get("param").getAsJsonObject().get("db").getAsString(), record.getDbName());
        Assert.assertEquals("sentence verb", "revokePrivilegesFromRole", record.getData().getConstruct().getSentences().get(0).getVerb());
        Assert.assertEquals("object type", "role", record.getData().getConstruct().getSentences().get(0).getObjects().get(0).getType());
        Assert.assertEquals("object name", source.get("param").getAsJsonObject().get("role").getAsString(), record.getData().getConstruct().getSentences().get(0).getObjects().get(0).getName());
        Assert.assertEquals("schema name", source.get("param").getAsJsonObject().get("db").getAsString(), record.getData().getConstruct().getSentences().get(0).getObjects().get(0).getSchema());

        // check priviliges
        validatePrivileges(record, "dbgbdi1", "collgbdiT1", "collection", Arrays.asList(new String[]{"insert"}), true);
    }

    @Test
    public void updateRole() throws Exception {

        Event e = new org.logstash.Event();
        TestMatchListener matchListener = new TestMatchListener();

        String inputMsg = reatTestCaseInput("role_update.txt");
        String testCaseMessage = buildTestCaseMessage(inputMsg);
        e.setField("message", testCaseMessage);

        Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);

        Assert.assertEquals(1, results.size());
        String recordString = e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME).toString();
        Record record = (new Gson()).fromJson(recordString, Record.class);
        JsonObject source = (new Gson()).fromJson(inputMsg, JsonObject.class);

        Assert.assertNotNull(record);

        // validate object details
        Assert.assertEquals("database name", source.get("param").getAsJsonObject().get("db").getAsString(), record.getDbName());
        Assert.assertEquals("sentence verb", "updateRole", record.getData().getConstruct().getSentences().get(0).getVerb());
        Assert.assertEquals("object type", "role", record.getData().getConstruct().getSentences().get(0).getObjects().get(0).getType());
        Assert.assertEquals("object name", source.get("param").getAsJsonObject().get("role").getAsString(), record.getData().getConstruct().getSentences().get(0).getObjects().get(0).getName());
        Assert.assertEquals("schema name", source.get("param").getAsJsonObject().get("db").getAsString(), record.getData().getConstruct().getSentences().get(0).getObjects().get(0).getSchema());

        validateRoles( source,  record, false);

        validatePrivileges(record, "dbgbdi1", " ", "collection", Arrays.asList(new String[]{"createCollection", "createIndex"}), true);

    }

    private void validateRoles(JsonObject source, Record record, boolean isRequired){
        ArrayList<Sentence> allSentences = record.getData().getConstruct().getSentences().get(0).getDescendants();
        Sentence rolesSentence = null;
        for (Sentence sentence : allSentences) {
            if (sentence.getVerb().equals(RoleParser.ROLES_COMMAND)){
                rolesSentence = sentence;
                break;
            }
        }
        if (isRequired){
            Assert.assertNotNull(rolesSentence);
        }
        if (rolesSentence==null){
            return;
        }

        ArrayList<SentenceObject> recordRoles = rolesSentence.getObjects();
        HashSet<String> allRecordRolesSet = new HashSet<>();
        for (SentenceObject recordRole : recordRoles) {
            allRecordRolesSet.add(recordRole.name);
        }
        JsonArray allRoles = source.get("param").getAsJsonObject().get("roles").getAsJsonArray();
        for(int i=0; i<allRoles.size(); i++){
            JsonObject role = allRoles.get(i).getAsJsonObject();
            String roleName = role.get("role").getAsString();
            Assert.assertTrue("assigned role "+roleName, allRecordRolesSet.contains(roleName));
        }
    }

    private void validatePrivileges(Record record, String db, String collection, String type, Collection<String> actions, boolean isRequired){
        ArrayList<Sentence> allSentences = record.getData().getConstruct().getSentences().get(0).getDescendants();
        Sentence privilegesSentence = null;
        for (Sentence sentence : allSentences) {
            if (sentence.getVerb().equals(RoleParser.PRIVILEGES_COMMAND)){
                privilegesSentence = sentence;
                break;
            }
        }
        if (isRequired){
            Assert.assertNotNull(privilegesSentence);
        }
        if (privilegesSentence==null){
            return;
        }

        ArrayList<SentenceObject> recordPrivileges = privilegesSentence.getObjects();
        HashSet<String> allRecordPrivilegesSet = new HashSet<>();
        for (SentenceObject recordPrivilege : recordPrivileges) {
            Assert.assertEquals("db name", db, recordPrivilege.getSchema());
            Assert.assertEquals("collection name", collection, recordPrivilege.getName());
            Assert.assertEquals("object type", type, recordPrivilege.getType());

            allRecordPrivilegesSet.addAll(Arrays.asList(recordPrivilege.getFields()));
        }
        for (String action : actions){
            Assert.assertTrue(action, allRecordPrivilegesSet.contains(action));
        }
    }
}
