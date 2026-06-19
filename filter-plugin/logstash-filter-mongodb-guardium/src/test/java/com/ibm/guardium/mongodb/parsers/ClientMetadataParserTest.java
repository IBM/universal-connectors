package com.ibm.guardium.mongodb.parsers;

import co.elastic.logstash.api.Context;
import co.elastic.logstash.api.Event;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.ibm.guardium.mongodb.MongodbGuardiumFilter;
import com.ibm.guardium.mongodb.TestMatchListener;
import com.ibm.guardium.universalconnector.commons.GuardConstants;
import com.ibm.guardium.universalconnector.commons.structures.Record;
import com.ibm.guardium.universalconnector.commons.structures.Sentence;
import org.junit.Assert;
import org.junit.Test;
import org.logstash.plugins.ContextImpl;

import java.util.Collection;
import java.util.Collections;

public class ClientMetadataParserTest {

    final static Context context = new ContextImpl(null, null);
    final static MongodbGuardiumFilter filter = new MongodbGuardiumFilter("test-id", null, context);

    public static String buildTestCaseMessage(String input) {
        StringBuffer sb = new StringBuffer();
        sb.append(MongodbGuardiumFilter.MONGOD_AUDIT_START_SIGNAL).append(" ").append(input);
        return sb.toString();
    }

    @Test
    public void test_clientMetadata_withFullDetails() throws Exception {
        String inputMsg = "{ \"atype\": \"clientMetadata\", \"ts\": { \"$date\": \"2024-01-15T10:30:45.123-0500\" }, " +
                "\"local\": { \"ip\": \"127.0.0.1\", \"port\": 27017 }, " +
                "\"remote\": { \"ip\": \"192.168.1.100\", \"port\": 54321 }, " +
                "\"users\": [ { \"user\": \"testUser\", \"db\": \"admin\" } ], " +
                "\"roles\": [ { \"role\": \"readWrite\", \"db\": \"admin\" } ], " +
                "\"param\": { " +
                "  \"clientMetadata\": { " +
                "    \"driver\": { \"name\": \"mongo-java-driver\", \"version\": \"4.5.0\" }, " +
                "    \"os\": { \"type\": \"Linux\", \"name\": \"Ubuntu\", \"architecture\": \"x86_64\", \"version\": \"20.04\" }, "
                +
                "    \"platform\": \"Java/OpenJDK/11.0.16\", " +
                "    \"application\": { \"name\": \"MyApp\" } " +
                "  } " +
                "}, " +
                "\"result\": 0 }";

        Event e = new org.logstash.Event();
        TestMatchListener matchListener = new TestMatchListener();

        String testCaseMessage = buildTestCaseMessage(inputMsg);
        e.setField("message", testCaseMessage);

        Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);

        Assert.assertEquals(1, results.size());
        String recordString = e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME).toString();
        Record record = (new Gson()).fromJson(recordString, Record.class);

        Assert.assertNotNull(record);

        // Validate sentence details
        Sentence sentence = record.getData().getConstruct().getSentences().get(0);
        Assert.assertEquals("sentence verb", "clientMetadata", sentence.getVerb());

        // Validate application object
        Assert.assertTrue("should have at least one object", sentence.getObjects().size() > 0);
        Assert.assertEquals("object name", "MyApp", sentence.getObjects().get(0).getName());
        Assert.assertEquals("object type", "application", sentence.getObjects().get(0).getType());

        // Validate fields contain driver, os, and platform information
        Assert.assertTrue("should contain driver field",
                sentence.getFields().stream().anyMatch(f -> f.startsWith("driver:")));
        Assert.assertTrue("should contain os field",
                sentence.getFields().stream().anyMatch(f -> f.startsWith("os:")));
        Assert.assertTrue("should contain platform field",
                sentence.getFields().stream().anyMatch(f -> f.startsWith("platform:")));
    }

    @Test
    public void test_clientMetadata_withLocalEndpoint() throws Exception {
        String inputMsg = "{ \"atype\": \"clientMetadata\", \"ts\": { \"$date\": \"2024-01-15T10:30:45.123-0500\" }, " +
                "\"local\": { \"ip\": \"10.0.0.5\", \"port\": 27017 }, " +
                "\"remote\": { \"ip\": \"10.0.0.10\", \"port\": 45678 }, " +
                "\"users\": [ { \"user\": \"admin\", \"db\": \"admin\" } ], " +
                "\"roles\": [ { \"role\": \"root\", \"db\": \"admin\" } ], " +
                "\"param\": { " +
                "  \"localEndpoint\": { \"ip\": \"10.0.0.5\", \"port\": 27017 }, " +
                "  \"clientMetadata\": { " +
                "    \"driver\": { \"name\": \"pymongo\", \"version\": \"4.0.1\" }, " +
                "    \"os\": { \"type\": \"Darwin\", \"name\": \"macOS\", \"architecture\": \"arm64\", \"version\": \"13.0\" }, "
                +
                "    \"platform\": \"Python/3.9.7\" " +
                "  } " +
                "}, " +
                "\"result\": 0 }";

        Event e = new org.logstash.Event();
        TestMatchListener matchListener = new TestMatchListener();

        String testCaseMessage = buildTestCaseMessage(inputMsg);
        e.setField("message", testCaseMessage);

        Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);

        Assert.assertEquals(1, results.size());
        String recordString = e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME).toString();
        Record record = (new Gson()).fromJson(recordString, Record.class);

        Assert.assertNotNull(record);

        // Validate sentence
        Sentence sentence = record.getData().getConstruct().getSentences().get(0);
        Assert.assertEquals("sentence verb", "clientMetadata", sentence.getVerb());

        // Should have a default client object when no application name is provided
        Assert.assertTrue("should have at least one object", sentence.getObjects().size() > 0);
    }

    @Test
    public void test_clientMetadata_withUnixSocket() throws Exception {
        String inputMsg = "{ \"atype\": \"clientMetadata\", \"ts\": { \"$date\": \"2024-01-15T10:30:45.123-0500\" }, " +
                "\"local\": { \"ip\": \"127.0.0.1\", \"port\": 27017 }, " +
                "\"remote\": { \"ip\": \"127.0.0.1\", \"port\": 0 }, " +
                "\"users\": [ { \"user\": \"localUser\", \"db\": \"test\" } ], " +
                "\"roles\": [ { \"role\": \"readWrite\", \"db\": \"test\" } ], " +
                "\"param\": { " +
                "  \"localEndpoint\": { \"unix\": \"/tmp/mongodb-27017.sock\" }, " +
                "  \"clientMetadata\": { " +
                "    \"driver\": { \"name\": \"nodejs\", \"version\": \"5.0.0\" }, " +
                "    \"os\": { \"type\": \"Linux\", \"name\": \"Debian\", \"architecture\": \"x86_64\", \"version\": \"11\" }, "
                +
                "    \"platform\": \"Node.js v18.12.0, LE\" " +
                "  } " +
                "}, " +
                "\"result\": 0 }";

        Event e = new org.logstash.Event();
        TestMatchListener matchListener = new TestMatchListener();

        String testCaseMessage = buildTestCaseMessage(inputMsg);
        e.setField("message", testCaseMessage);

        Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);

        Assert.assertEquals(1, results.size());
        String recordString = e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME).toString();
        Record record = (new Gson()).fromJson(recordString, Record.class);

        Assert.assertNotNull(record);

        // Validate basic structure
        Sentence sentence = record.getData().getConstruct().getSentences().get(0);
        Assert.assertEquals("sentence verb", "clientMetadata", sentence.getVerb());
        Assert.assertTrue("should have objects", sentence.getObjects().size() > 0);
    }

    @Test
    public void test_clientMetadata_mongosh() throws Exception {
        // Test with actual mongosh clientMetadata log format
        String inputMsg = "{ \"atype\" : \"clientMetadata\", \"ts\" : { \"$date\" : \"2026-02-11T08:50:12.869-08:00\" }, "
                +
                "\"uuid\" : { \"$binary\" : \"8bcf4258Tn+9URFLF07DLw==\", \"$type\" : \"04\" }, " +
                "\"local\" : { \"ip\" : \"127.0.0.1\", \"port\" : 27017 }, " +
                "\"remote\" : { \"ip\" : \"127.0.0.1\", \"port\" : 37004 }, " +
                "\"users\" : [], \"roles\" : [], " +
                "\"param\" : { " +
                "  \"localEndpoint\" : { \"ip\" : \"127.0.0.1\", \"port\" : 27017 }, " +
                "  \"clientMetadata\" : { " +
                "    \"application\" : { \"name\" : \"mongosh 2.6.0\" }, " +
                "    \"driver\" : { \"name\" : \"nodejs|mongosh\", \"version\" : \"6.19.0|2.6.0\" }, " +
                "    \"platform\" : \"Node.js v20.20.0, LE\", " +
                "    \"os\" : { \"name\" : \"linux\", \"architecture\" : \"x64\", \"version\" : \"3.10.0-327.22.2.el7.x86_64\", \"type\" : \"Linux\" } "
                +
                "  } " +
                "}, " +
                "\"result\" : 0 }";

        Event e = new org.logstash.Event();
        TestMatchListener matchListener = new TestMatchListener();

        String testCaseMessage = buildTestCaseMessage(inputMsg);
        e.setField("message", testCaseMessage);

        Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);

        Assert.assertEquals(1, results.size());
        String recordString = e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME).toString();
        Record record = (new Gson()).fromJson(recordString, Record.class);

        Assert.assertNotNull(record);

        // Validate sentence details
        Sentence sentence = record.getData().getConstruct().getSentences().get(0);
        Assert.assertEquals("sentence verb", "clientMetadata", sentence.getVerb());

        // Validate application object
        Assert.assertTrue("should have at least one object", sentence.getObjects().size() > 0);
        Assert.assertEquals("object name", "mongosh 2.6.0", sentence.getObjects().get(0).getName());
        Assert.assertEquals("object type", "application", sentence.getObjects().get(0).getType());

        // Validate fields contain driver, os, and platform information
        Assert.assertTrue("should contain driver field",
                sentence.getFields().stream().anyMatch(f -> f.contains("nodejs|mongosh")));
        Assert.assertTrue("should contain os field with architecture and version",
                sentence.getFields().stream()
                        .anyMatch(f -> f.contains("Linux") && f.contains("linux") && f.contains("x64")));
        Assert.assertTrue("should contain platform field",
                sentence.getFields().stream().anyMatch(f -> f.contains("Node.js v20.20.0")));

        // Validate session locator
        Assert.assertEquals("client IP", "127.0.0.1", record.getSessionLocator().getClientIp());
        Assert.assertEquals("client port", 37004, record.getSessionLocator().getClientPort());
        Assert.assertEquals("server IP", "127.0.0.1", record.getSessionLocator().getServerIp());
        Assert.assertEquals("server port", 27017, record.getSessionLocator().getServerPort());
    }
}