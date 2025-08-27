//
// Copyright 2020-2021 IBM Inc. All rights reserved
// SPDX-License-Identifier: Apache2.0
//

package com.ibm.guardium.neodb;

import co.elastic.logstash.api.Event;
import com.google.gson.JsonObject;
import com.ibm.guardium.universalconnector.commons.structures.*;
import com.ibm.guardium.universalconnector.commons.structures.Record;
import org.junit.Assert;
import org.junit.Test;
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParserTest {

	String neoSuccessString = "2021-08-06 17:09:40.008+0000 INFO  9 ms: (planning: 1, waiting: 0) - 0 B - 4 page hits, 0 page faults - bolt-session	bolt	neo4j-browser/v4.3.1		client/127.0.0.1:51372	server/127.0.0.1:11004>	neo4j - neo4j - MATCH (Ishant:player {name: 'Ishant Sharma', YOB: 1988, POB: 'Delhi'}) DETACH DELETE Ishant - {} - runtime=slotted - {type: 'user-direct', app: 'neo4j-browser_v4.3.1'}";
	String neoSuccessString_grokOutput = "{\n" +
			"    \"ts\": \"\\\"2021-08-06 17:09:40.008+0000\",\n" +
			"    \"log_level\": \"INFO\",\n" +
			"    \"metadata1\": \" 9 ms: (planning: 1, waiting: 0) - 0 B - 4 page hits, 0 page faults - bolt-session\",\n" +
			"    \"protocol\": \"bolt\",\n" +
			"    \"driverVersion\": \"neo4j-browser/v4.3.1\",\n" +
			"    \"client_ip\": \"client/127.0.0.1:51372\",\n" +
			"    \"server_ip\": \"server/127.0.0.1:11004\",\n" +
			"    \"dbname\": \"neo4j \",\n" +
			"    \"dbuser\": \"neo4j \",\n" +
			"    \"queryStatement\": \"MATCH (Ishant:player {name: 'Ishant Sharma', YOB: 1988, POB: 'Delhi'}) DETACH DELETE Ishant - {} - runtime=slotted - {type: 'user-direct', app: 'neo4j-browser_v4.3.1'}\\\";\"\n" +
			"  }";

	    @Test
	    public void testParseAsConstruct_Match() {

	    	Event e = getParsedEvent(neoSuccessString_grokOutput, neoSuccessString);

	    	JsonObject inputData = inputData(e);
			Parser parser = new Parser();
			final Construct result = parser.parseAsConstruct(inputData);

	        final Sentence sentence = result.sentences.get(0);

	        Assert.assertEquals("MATCH", sentence.getVerb().trim());
	        Assert.assertEquals("player", sentence.getObjects().get(0).name.trim());
	        Assert.assertEquals("graph", sentence.getObjects().get(0).type);
	    }

	    @Test
	    public void testParseAsConstruct_Create() {
	        String neoString = "2021-08-06 15:57:11.502+0000 INFO  2 ms: (planning: 1, waiting: 0) - 1704 B - 0 page hits, 0 page faults - bolt-session	bolt	neo4j-browser/v4.3.1		client/127.0.0.1:54356	server/127.0.0.1:11004>	neo4j - neo4j - CREATE (friend:Person {name: 'Mark'})   RETURN friend - {} - runtime=slotted - {type: 'user-direct', app: 'neo4j-browser_v4.3.1'}";
			String neoString_grokOutput = "{\n" +
					"    \"ts\": \"2021-08-06 15:57:11.502+0000\",\n" +
					"    \"log_level\": \"INFO\",\n" +
					"    \"metadata1\": \" 2 ms: (planning: 1, waiting: 0) - 1704 B - 0 page hits, 0 page faults - bolt-session\",\n" +
					"    \"protocol\": \"bolt\",\n" +
					"    \"driverVersion\": \"neo4j-browser/v4.3.1\",\n" +
					"    \"client_ip\": \"client/127.0.0.1:54356\",\n" +
					"    \"server_ip\": \"server/127.0.0.1:11004\",\n" +
					"    \"dbname\": \"neo4j \",\n" +
					"    \"dbuser\": \"neo4j \",\n" +
					"    \"queryStatement\": \"CREATE (friend:Person {name: 'Mark'})   RETURN friend - {} - runtime=slotted - {type: 'user-direct', app: 'neo4j-browser_v4.3.1'}\"\n" +
					"  }";
	    	Event e = getParsedEvent(neoString_grokOutput,neoString);

	    	JsonObject inputData = inputData(e);

			Parser parser = new Parser();
			final Construct result = parser.parseAsConstruct(inputData);

	        final Sentence sentence = result.sentences.get(0);

	        Assert.assertEquals("CREATE", sentence.getVerb().trim());
	        Assert.assertEquals("Person", sentence.getObjects().get(0).name.trim());
	        Assert.assertEquals("graph", sentence.getObjects().get(0).type);
	    }

	    @Test
	    public void testParseAsConstruct_Merge() {
	        String neoString = "2021-08-06 15:56:12.097+0000 INFO  4 ms: (planning: 1, waiting: 0) - 0 B - 6 page hits, 0 page faults - bolt-session	bolt	neo4j-browser/v4.3.1		client/127.0.0.1:54356	server/127.0.0.1:11004>	neo4j - neo4j - MERGE (mark:Person {name: 'Mark'})   RETURN mark - {} - runtime=slotted - {type: 'user-direct', app: 'neo4j-browser_v4.3.1'}";
			String neoString_grokOutput = "{\n" +
					"    \"ts\": \"2021-08-06 15:56:12.097+0000\",\n" +
					"    \"log_level\": \"INFO\",\n" +
					"    \"metadata1\": \" 4 ms: (planning: 1, waiting: 0) - 0 B - 6 page hits, 0 page faults - bolt-session\",\n" +
					"    \"protocol\": \"bolt\",\n" +
					"    \"driverVersion\": \"neo4j-browser/v4.3.1\",\n" +
					"    \"client_ip\": \"client/127.0.0.1:54356\",\n" +
					"    \"server_ip\": \"server/127.0.0.1:11004\",\n" +
					"    \"dbname\": \"neo4j \",\n" +
					"    \"dbuser\": \"neo4j \",\n" +
					"    \"queryStatement\": \"MERGE (mark:Person {name: 'Mark'})   RETURN mark - {} - runtime=slotted - {type: 'user-direct', app: 'neo4j-browser_v4.3.1'}\"\n" +
					"  }";
	    	Event e = getParsedEvent(neoString_grokOutput,neoString);
			e.setField("minoff", "+04:00");
			JsonObject inputData = inputData(e);

			Parser parser = new Parser();
	        final Construct result = parser.parseAsConstruct(inputData);

	        final Sentence sentence = result.sentences.get(0);

	        Assert.assertEquals("MERGE", sentence.getVerb().trim());
	        Assert.assertEquals("Person", sentence.getObjects().get(0).name.trim());
	        Assert.assertEquals("graph", sentence.getObjects().get(0).type);
	    }

	    @Test
	    public void testParseException_DELETE() {
	        String neoString = "2021-03-03 08:49:32.367+0000 ERROR 7 ms: (planning: 7, waiting: 0) - 0 B - 0 page hits, 0 page faults - bolt-session	bolt	neo4j-browser/v4.2.1		client/127.0.0.1:62845	server/127.0.0.1:7687>	<none> - neo4j - DETACH DELETE node - {} - runtime=null - {type: 'user-action', app: 'neo4j-browser_v4.2.1'} - Variable `node` not defined (line 1, column 23 (offset: 22))";
			String neoString_grokOutput = "{\n" +
					"    \"ts\": \"2021-03-03 08:49:32.367+0000\",\n" +
					"    \"log_level\": \"ERROR\",\n" +
					"    \"metadata1\": \"7 ms: (planning: 7, waiting: 0) - 0 B - 0 page hits, 0 page faults - bolt-session\",\n" +
					"    \"protocol\": \"bolt\",\n" +
					"    \"driverVersion\": \"neo4j-browser/v4.2.1\",\n" +
					"    \"client_ip\": \"client/127.0.0.1:62845\",\n" +
					"    \"server_ip\": \"server/127.0.0.1:7687\",\n" +
					"    \"dbname\": \"<none> \",\n" +
					"    \"dbuser\": \"neo4j \",\n" +
					"    \"queryStatement\": \"DETACH DELETE node - {} - runtime=null - {type: 'user-action', app: 'neo4j-browser_v4.2.1'} - Variable `node` not defined (line 1, column 23 (offset: 22))\\\";\"\n" +
					"  }";
			Event e = getParsedEvent(neoString_grokOutput, neoString);
			e.setField("minoff", "+07:00");
			JsonObject inputData = inputData(e);

			Parser parser = new Parser();
	        final ExceptionRecord exceptionRecord = parser.parseException(inputData);

	        Assert.assertEquals("Variable `node` not defined (line 1, column 23 (offset: 22))", exceptionRecord.getDescription().trim());
	        Assert.assertEquals("DETACH DELETE node - {} - runtime=null - {type: 'user-action', app: 'neo4j-browser_v4.2.1", exceptionRecord.getSqlString().trim());
	    }

	    @Test
	    public void testParseAccessor() {

	    	Event e = getParsedEvent(neoSuccessString_grokOutput,neoSuccessString);
	    	JsonObject inputData = inputData(e);

			Parser parser = new Parser();
	        final Accessor accessor = parser.parseAccessor(inputData);

	        Assert.assertEquals("Bolt database protocol", accessor.getDbProtocol().toString().trim());
	        Assert.assertEquals("NEO4J", accessor.getServerType().toString().trim());
	        Assert.assertEquals("neo4j", accessor.getDbUser().toString().trim());
	        Assert.assertEquals("FREE_TEXT", accessor.getLanguage().toString().trim());
	    }

	    @Test
	    public void testParseSessionLocator() {

	    	Event e = getParsedEvent(neoSuccessString_grokOutput, neoSuccessString);

	    	JsonObject inputData = inputData(e);

			Parser parser = new Parser();
			final SessionLocator sessionLocator = parser.parseSessionLocator(inputData);

	        Assert.assertEquals("127.0.0.1", sessionLocator.getClientIp().toString().trim());
	        Assert.assertEquals(51372, sessionLocator.getClientPort());
	        Assert.assertEquals("127.0.0.1", sessionLocator.getServerIp().toString().trim());
	        Assert.assertEquals(11004, sessionLocator.getServerPort());

	    }

	    @Test
	    public void testParseTimestamp() {

	    	Event e = getParsedEvent(neoSuccessString_grokOutput, neoSuccessString);
	    	e.setField(Constants.TIMESTAMP, "2021-01-25 11:17:09.099+0000");
	    	JsonObject inputData = inputData(e);

			Parser parser = new Parser();
			final String timestamp = parser.parseTimestamp(inputData);

	        Assert.assertEquals("2021-01-25 11:17:09.099+0000", timestamp);

	    }

	@Test
	public void testGetTime() {

		String dateString = "2021-01-25 11:17:09.099+0000";
		String timeZone = "-04:00";

		Parser parser = new Parser();
		final Time time = parser.getTime(dateString, timeZone);

		Assert.assertEquals(0, time.getMinDst());
		Assert.assertEquals(-240, time.getMinOffsetFromGMT());
		Assert.assertEquals(1611587829099L, time.getTimstamp());

	}

	    @Test
	    public void testParseSentence() {

	    	Event e = getParsedEvent(neoSuccessString_grokOutput, neoSuccessString);

	    	JsonObject inputData = inputData(e);

			Parser parser = new Parser();
	        final Sentence sentence = parser.parseSentence(inputData);

	        Assert.assertEquals("MATCH", sentence.getVerb());
	        Assert.assertEquals("player", sentence.getObjects().get(0).name.trim());
	        Assert.assertEquals("graph", sentence.getObjects().get(0).type);

	    }

	    @Test
	    public void testParseRedactedSensitiveDataSql() {

	    	Event e = getParsedEvent(neoSuccessString_grokOutput, neoSuccessString);

	    	JsonObject inputData = inputData(e);

			Parser parser = new Parser();
	    	final String redacted = parser.parseRedactedSensitiveDataSql(inputData);

	        Assert.assertEquals("2021-08-06 17:09:40.008+0000 INFO  9 ms: (planning: 1, waiting: 0) - 0 B - 4 page hits, 0 page faults - bolt-session	bolt	neo4j-browser/v4.3.1		client/127.0.0.1:51372	server/127.0.0.1:11004>	neo4j - neo4j - MATCH (Ishant:player {name: 'Ishant Sharma', YOB: 1988, POB: 'Delhi'}) DETACH DELETE Ishant - {} - runtime=slotted - {type: 'user-direct', app: 'neo4j-browser_v4.3.1'}", redacted);

	    }

		@Test
		public void testEmptyOperations() {
			LinkedHashMap<String, ArrayList<String>> operations = new LinkedHashMap<String, ArrayList<String>>();
			List<String> alKeys = new ArrayList<String>(operations.keySet());
			Assert.assertEquals(true, alKeys.isEmpty());
		}

//	    ----------------------------------- ---------------------------------------------------

	    private JsonObject inputData(Event e){
			JsonObject data = new JsonObject();

			if(e.getField(Constants.CLIENT_IP).toString() != null && !e.getField(Constants.CLIENT_IP).toString().isEmpty()){
				data.addProperty(Constants.CLIENT_IP, e.getField(Constants.CLIENT_IP).toString());
			}
			if(e.getField(Constants.SERVER_IP).toString() != null && !e.getField(Constants.SERVER_IP).toString().isEmpty()){
				data.addProperty(Constants.SERVER_IP, e.getField(Constants.SERVER_IP).toString());
			}
			if(e.getField(Constants.DB_PROTOCOL).toString() != null && e.getField(Constants.DB_PROTOCOL).toString().isEmpty()){
				data.addProperty(Constants.DB_PROTOCOL, e.getField(Constants.DB_PROTOCOL).toString());
			}
			if(e.getField(Constants.TIMESTAMP).toString() != null && !e.getField(Constants.TIMESTAMP).toString().isEmpty()){
				data.addProperty(Constants.TIMESTAMP, e.getField(Constants.TIMESTAMP).toString());
			}
			if(e.getField(Constants.LOG_LEVEL).toString() != null && !e.getField(Constants.LOG_LEVEL).toString().isEmpty()){
				data.addProperty(Constants.LOG_LEVEL, e.getField(Constants.LOG_LEVEL).toString());
			}
			if(e.getField(Constants.DB_USER).toString() != null && !e.getField(Constants.DB_USER).toString().isEmpty()){
				data.addProperty(Constants.DB_USER, e.getField(Constants.DB_USER).toString());
			}
			if(e.getField(Constants.DB_NAME).toString() != null && !e.getField(Constants.DB_NAME).toString().isEmpty()){
				data.addProperty(Constants.DB_NAME, e.getField(Constants.DB_NAME).toString());
			}
			if(e.getField(Constants.SOURCE_PROGRAM).toString() != null && !e.getField(Constants.SOURCE_PROGRAM).toString().isEmpty()){
				data.addProperty(Constants.SOURCE_PROGRAM, e.getField(Constants.SOURCE_PROGRAM).toString());
			}
			if(e.getField(Constants.QUERY_STATEMENT).toString() != null && !e.getField(Constants.QUERY_STATEMENT).toString().isEmpty()){
				data.addProperty(Constants.QUERY_STATEMENT, e.getField(Constants.QUERY_STATEMENT).toString());
			}
			if(e.getField(Constants.MESSAGE).toString() != null && !e.getField(Constants.MESSAGE).toString().isEmpty()){
				data.addProperty(Constants.MESSAGE, e.getField(Constants.MESSAGE).toString());
			}
			return data;
		}


	public static Event getParsedEvent(String logEvent_json , String logEvent) {

		Map<String,String> results = parseJson(logEvent_json);
		Event e = new org.logstash.Event();
		e.setField("message", logEvent);

		e.setField(Constants.CLIENT_IP, results.get(Constants.CLIENT_IP));
		e.setField(Constants.SERVER_IP, results.get(Constants.SERVER_IP));
		e.setField(Constants.DB_PROTOCOL, results.get(Constants.DB_PROTOCOL));
		e.setField(Constants.TIMESTAMP, results.get(Constants.TIMESTAMP));
		e.setField(Constants.LOG_LEVEL, results.get(Constants.LOG_LEVEL));
		e.setField(Constants.DB_USER, results.get(Constants.DB_USER));
		e.setField(Constants.DB_NAME, results.get(Constants.DB_NAME));
		e.setField(Constants.SOURCE_PROGRAM, results.get(Constants.SOURCE_PROGRAM));
		e.setField(Constants.QUERY_STATEMENT, results.get(Constants.QUERY_STATEMENT));

		return e;
	}


	public static Map<String, String> parseJson(String jsonString) {
		Map<String, String> map = new HashMap<>();

		// Use a regular expression to match each key-value pair in the JSON string
		String pattern = "\"(.*?)\":\\s*\"(.*?)\"";
		Pattern r = Pattern.compile(pattern);
		Matcher m = r.matcher(jsonString);

		while (m.find()) {
			// Add each key-value pair to the map
			String key = m.group(1);
			String value = m.group(2).replaceAll("(?<!\\\\)\\\\(?!\\\\)", "");
			map.put(key, value);
		}
		return map;
	}

}


