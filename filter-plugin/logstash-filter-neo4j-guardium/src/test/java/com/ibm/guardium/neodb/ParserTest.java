//
// Copyright 2020-2021 IBM Inc. All rights reserved
// SPDX-License-Identifier: Apache2.0
//

package com.ibm.guardium.neodb;

import java.util.Map;

import org.aicer.grok.dictionary.GrokDictionary;
import org.aicer.grok.util.Grok;
import org.junit.Assert;
import org.junit.Test;

import com.google.gson.JsonObject;
import com.ibm.guardium.univer***REMOVED***lconnector.commons.structures.Accessor;
import com.ibm.guardium.univer***REMOVED***lconnector.commons.structures.Construct;
import com.ibm.guardium.univer***REMOVED***lconnector.commons.structures.ExceptionRecord;
import com.ibm.guardium.univer***REMOVED***lconnector.commons.structures.Sentence;
import com.ibm.guardium.univer***REMOVED***lconnector.commons.structures.SessionLocator;
import com.ibm.guardium.univer***REMOVED***lconnector.commons.structures.Time;

import co.elastic.logstash.api.Event;

public class ParserTest {

	    Parser parser = new Parser();
	    final String neoSuccessString = "2021-08-06 17:09:40.008+0000 INFO  9 ms: (planning: 1, waiting: 0) - 0 B - 4 page hits, 0 page faults - bolt-session	bolt	neo4j-browser/v4.3.1		client/127.0.0.1:51372	server/127.0.0.1:11004>	neo4j - neo4j - MATCH (Ishant:player {name: \"Ishant Sharma\", YOB: 1988, POB: \"Delhi\"}) DETACH DELETE Ishant - {} - runtime=slotted - {type: 'user-direct', app: 'neo4j-browser_v4.3.1'}";
	    
	    final String neoErrorString = "2021-01-25 11:17:09.099+0000 INFO  106 ms: (planning: 61, waiting: 0) - 0 B - 1 page hits, 0 page faults - bolt-session	bolt	neo4j-browser/v4.2.1		client/127.0.0.1:53010	server/127.0.0.1:7687>	<none> - neo4j - EXPLAIN MATCH(n) - {} - runtime=null - {type: 'user-action', app: 'neo4j-browser_v4.2.1'} - Query cannot conclude with MATCH (must be RETURN or an update clause) (line 1, column 9 (offset: 8))";
	    
	    final String skipString = "2021-08-06 14:09:07.781+0000 INFO  71 ms: (planning: 69, waiting: 0) - 64 B - 3 page hits, 0 page faults - bolt-session   bolt    neo4j-javascript/0.0.0-dev              client/127.0.0.1:53828  server/127.0.0.1:11004> guardiumdb - neo4j - MATCH ()-->() RETURN count(*); - {} - runtime=pipelined - {}";
	    
	    @Test
	    public void testParseAsConstruct_Match() {
	    	
	    	Event e = getParsedEvent(neoSuccessString);
	    	
	    	JsonObject inputData = inputData(e);
	        
	        final Construct result = Parser.parseAsConstruct(inputData);
	        
	        final Sentence sentence = result.sentences.get(0);
	        
	        Assert.assertEquals("MATCH", sentence.getVerb().trim());
	        Assert.assertEquals("player", sentence.getObjects().get(0).name.trim());
	        Assert.assertEquals("graph", sentence.getObjects().get(0).type);
	    }
	    
	    @Test
	    public void testParseAsConstruct_Create() {
	        String neoString = "2021-08-06 15:57:11.502+0000 INFO  2 ms: (planning: 1, waiting: 0) - 1704 B - 0 page hits, 0 page faults - bolt-session	bolt	neo4j-browser/v4.3.1		client/127.0.0.1:54356	server/127.0.0.1:11004>	neo4j - neo4j - CREATE (friend:Person {name: 'Mark'})   RETURN friend - {} - runtime=slotted - {type: 'user-direct', app: 'neo4j-browser_v4.3.1'}";
	    	Event e = getParsedEvent(neoString);
	    	
	    	JsonObject inputData = inputData(e);
	        
	        final Construct result = Parser.parseAsConstruct(inputData);
	        
	        final Sentence sentence = result.sentences.get(0);
	    
	        Assert.assertEquals("CREATE", sentence.getVerb().trim());
	        Assert.assertEquals("Person", sentence.getObjects().get(0).name.trim());
	        Assert.assertEquals("graph", sentence.getObjects().get(0).type);
	    }
	    
	    @Test
	    public void testParseAsConstruct_Merge() {
	        String neoString = "2021-08-06 15:56:12.097+0000 INFO  4 ms: (planning: 1, waiting: 0) - 0 B - 6 page hits, 0 page faults - bolt-session	bolt	neo4j-browser/v4.3.1		client/127.0.0.1:54356	server/127.0.0.1:11004>	neo4j - neo4j - MERGE (mark:Person {name: 'Mark'})   RETURN mark - {} - runtime=slotted - {type: 'user-direct', app: 'neo4j-browser_v4.3.1'}";
	    	Event e = getParsedEvent(neoString);
	    	
	    	JsonObject inputData = inputData(e);
	        
	        final Construct result = Parser.parseAsConstruct(inputData);
	        
	        final Sentence sentence = result.sentences.get(0);
	    
	        Assert.assertEquals("MERGE", sentence.getVerb().trim());
	        Assert.assertEquals("Person", sentence.getObjects().get(0).name.trim());
	        Assert.assertEquals("graph", sentence.getObjects().get(0).type);
	    }
	    
	    @Test
	    public void testParseException_DELETE() {
	        String neoString = "2021-03-03 08:49:32.367+0000 ERROR 7 ms: (planning: 7, waiting: 0) - 0 B - 0 page hits, 0 page faults - bolt-session	bolt	neo4j-browser/v4.2.1		client/127.0.0.1:62845	server/127.0.0.1:7687>	<none> - neo4j - DETACH DELETE node - {} - runtime=null - {type: 'user-action', app: 'neo4j-browser_v4.2.1'} - Variable `node` not defined (line 1, column 23 (offset: 22))";
	    	Event e = getParsedEvent(neoString);

	    	JsonObject inputData = inputData(e);
	        
	        final ExceptionRecord exceptionRecord = Parser.parseException(inputData);

	        Assert.assertEquals("Variable `node` not defined (line 1, column 23 (offset: 22))", exceptionRecord.getDescription().trim());
	        Assert.assertEquals("DETACH DELETE node - {} - runtime=null - {type: 'user-action', app: 'neo4j-browser_v4.2.1", exceptionRecord.getSqlString().trim());
	        
	    }
	    
	    @Test
	    public void testParseAccessor() {

	    	Event e = getParsedEvent(neoSuccessString);
	    	
	    	JsonObject inputData = inputData(e);
	        
	        final Accessor accessor = Parser.parseAccessor(inputData);
	        
	        Assert.assertEquals("Bolt database protocol", accessor.getDbProtocol().toString().trim());
	        Assert.assertEquals("NEO4J", accessor.getServerType().toString().trim());
	        Assert.assertEquals("neo4j", accessor.getDbUser().toString().trim());
	        Assert.assertEquals("FREE_TEXT", accessor.getLanguage().toString().trim());
	        
	        
	    }
	    
	    @Test
	    public void testParseSessionLocator() {

	    	Event e = getParsedEvent(neoSuccessString);
	    	
	    	JsonObject inputData = inputData(e);
	        
	        final SessionLocator sessionLocator = Parser.parseSessionLocator(inputData);
	        
	        Assert.assertEquals("127.0.0.1", sessionLocator.getClientIp().toString().trim());
	        Assert.assertEquals(51372, sessionLocator.getClientPort());
	        Assert.assertEquals("127.0.0.1", sessionLocator.getServerIp().toString().trim());
	        Assert.assertEquals(11004, sessionLocator.getServerPort());
	        
	    }
	    
	    @Test
	    public void testParseTimestamp() {

	    	Event e = getParsedEvent(neoSuccessString);
	    	e.setField(Constants.TIMESTAMP, "2021-01-25 11:17:09.099+0000");
	    	JsonObject inputData = inputData(e);
	    	
	        final String timestamp = Parser.parseTimestamp(inputData);
	        
	        Assert.assertEquals("2021-01-25 11:17:09.099+0000", timestamp);

	    }
	    
	    @Test
	    public void testGetTime() {

	    	String dateString = "2021-01-25 11:17:09.099+0000";
	    	
	        final Time time = Parser.getTime(dateString);
	        
	        Assert.assertEquals(0, time.getMinDst());
	        Assert.assertEquals(0, time.getMinOffsetFromGMT());
	        Assert.assertEquals(1611573429099L, time.getTimstamp());

	    }
	    
	    @Test
	    public void testParseSentence() {

	    	Event e = getParsedEvent(neoSuccessString);
	    	
	    	JsonObject inputData = inputData(e);
	        
	        final Sentence sentence = Parser.parseSentence(inputData);
	        
	        Assert.assertEquals("MATCH", sentence.getVerb());
	        Assert.assertEquals("player", sentence.getObjects().get(0).name.trim());
	        Assert.assertEquals("graph", sentence.getObjects().get(0).type);
	        
	    }
	    
	    @Test
	    public void testParseRedactedSensitiveDataSql() {
	    	
	    	Event e = getParsedEvent(neoSuccessString);
	    	
	    	JsonObject inputData = inputData(e);
	        
	    	final String redacted = Parser.parseRedactedSensitiveDataSql(inputData);
	    	
	        Assert.assertEquals("2021-08-06 17:09:40.008+0000 INFO  9 ms: (planning: 1, waiting: 0) - 0 B - 4 page hits, 0 page faults - bolt-session	bolt	neo4j-browser/v4.3.1		client/127.0.0.1:51372	server/127.0.0.1:11004>	neo4j - neo4j - MATCH (Ishant:player {name: \"Ishant Sharma\", YOB: 1988, POB: \"Delhi\"}) DETACH DELETE Ishant - {} - runtime=slotted - {type: 'user-direct', app: 'neo4j-browser_v4.3.1'}", redacted);

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
	    
	    public static Event getParsedEvent(String logEvent){
	    	
	    	Map<String, String> results = getGrokParsedEvent(logEvent);
	    	
	    	Event e = new org.logstash.Event();
	    	e.setField("mes***REMOVED***ge", logEvent);
	    	
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
	    
	    private static Map<String, String> getGrokParsedEvent(String logEvent){
	    	
			final String expression = "(?<ts>[^[A-Z]]*) %{LOGLEVEL:log_level}\\s(?<metadata1>[^\\\t]*)\t(?<protocol>[^ \\s]*)\\s(?<driverVersion>[^ \t]*)\t\t(?<client_ip>[^ \t]*)\t(?<server_ip>[^ >]*)\\>\t(?<dbname>[^-]*)\\-\\s(?<dbuser>[^-]*)\\- %{GREEDYDATA:queryStatement}";
			final GrokDictionary dictionary = new GrokDictionary();
			
			// Load the built-in dictionaries
			dictionary.addBuiltInDictionaries();
			
			// Resolve all expressions loaded
			dictionary.bind();
			
			Grok compiledPattern = dictionary.compileExpression(expression);
			
			return compiledPattern.extractNamedGroups(logEvent);
	  }

}
