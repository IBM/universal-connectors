//
// Copyright 2020-2021 IBM Inc. All rights reserved
// SPDX-License-Identifier: Apache2.0
//

package com.ibm.guardium.singlestore;

import co.elastic.logstash.api.Event;
import com.google.gson.JsonObject;
import com.ibm.guardium.singlestore.Constants;
import com.ibm.guardium.singlestore.Parser;
import com.ibm.guardium.universalconnector.commons.structures.*;
import org.junit.Assert;
import org.junit.Test;

public class ParserTest {

	String singlestoreString = "133855,2024-06-24 07:16:16.901,UTC,53b9ae806c1c:3306,agg,1,100000,root,vector_db,,1308432953418920798,CREATE DATABASE uc_vector_db";

	@Test
	    public void testParseAsConstruct_Match() {
	    	
	    	Event e = getParsedEvent(singlestoreString);
	    	
	    	JsonObject inputData = inputData(e);
		//final Construct result = Parser.parseAsConstruct(inputData);

		//final Sentence sentence = result.sentences.get(0);

		//Assert.assertEquals("MATCH", sentence.getVerb().trim());
		//Assert.assertEquals("player", sentence.getObjects().get(0).name.trim());
		//Assert.assertEquals("graph", sentence.getObjects().get(0).type);
	    }
	    
	    @Test
	    public void testParseAsConstruct_Create() {

			Event e = getParsedEvent(singlestoreString);
	    	
	    	JsonObject inputData = inputData(e);

			//final Construct result = Parser.parseAsConstruct(inputData);

			//final Sentence sentence = result.sentences.get(0);

			//Assert.assertEquals("CREATE", sentence.getVerb().trim());
			//Assert.assertEquals("Person", sentence.getObjects().get(0).name.trim());
			//Assert.assertEquals("graph", sentence.getObjects().get(0).type);
	    }
	    
	    @Test
	    public void testParseAsConstruct_Merge() {

			Event e = getParsedEvent(singlestoreString);
	    	
	    	JsonObject inputData = inputData(e);

			//final Construct result = Parser.parseAsConstruct(inputData);

			//final Sentence sentence = result.sentences.get(0);

			//Assert.assertEquals("MERGE", sentence.getVerb().trim());
			//Assert.assertEquals("Person", sentence.getObjects().get(0).name.trim());
			//Assert.assertEquals("graph", sentence.getObjects().get(0).type);
	    }
	    
	    @Test
	    public void testParseException_DELETE() {
	        String singlestoreString = "2021-03-03 08:49:32.367+0000 ERROR 7 ms: (planning: 7, waiting: 0) - 0 B - 0 page hits, 0 page faults - bolt-session	bolt	neo4j-browser/v4.2.1		client/127.0.0.1:62845	server/127.0.0.1:7687>	<none> - neo4j - DETACH DELETE node - {} - runtime=null - {type: 'user-action', app: 'neo4j-browser_v4.2.1'} - Variable `node` not defined (line 1, column 23 (offset: 22))";
			Event e = getParsedEvent(singlestoreString);
	    	JsonObject inputData = inputData(e);

			//final ExceptionRecord exceptionRecord = Parser.parseException(inputData);

			// Assert.assertEquals("Variable `node` not defined (line 1, column 23 (offset: 22))", exceptionRecord.getDescription().trim());
			//Assert.assertEquals("DETACH DELETE node - {} - runtime=null - {type: 'user-action', app: 'neo4j-browser_v4.2.1", exceptionRecord.getSqlString().trim());
	    }
	    
	    @Test
	    public void testParseAccessor() {

	    	Event e = getParsedEvent(singlestoreString);
	    	
	    	JsonObject inputData = inputData(e);

			//final Accessor accessor = Parser.parseAccessor(inputData);

			//Assert.assertEquals("Bolt database protocol", accessor.getDbProtocol().toString().trim());
			//Assert.assertEquals("NEO4J", accessor.getServerType().toString().trim());
			//Assert.assertEquals("neo4j", accessor.getDbUser().toString().trim());
			//Assert.assertEquals("FREE_TEXT", accessor.getLanguage().toString().trim());
	    }
	    
	    @Test
	    public void testParseSessionLocator() {

	    	Event e = getParsedEvent(singlestoreString);
	    	
	    	JsonObject inputData = inputData(e);
	        
	        //final SessionLocator sessionLocator = Parser.parseSessionLocator(inputData);
	        
	        //Assert.assertEquals("127.0.0.1", sessionLocator.getClientIp().toString().trim());
	        //Assert.assertEquals(51372, sessionLocator.getClientPort());
	        //Assert.assertEquals("127.0.0.1", sessionLocator.getServerIp().toString().trim());
	        //Assert.assertEquals(11004, sessionLocator.getServerPort());
	        
	    }
	    
	    @Test
	    public void testParseTimestamp() {

	    	Event e = getParsedEvent(singlestoreString);
	    	JsonObject inputData = inputData(e);
	    	
	        //final String timestamp = Parser.parseTimestamp(inputData);
	        
	        //Assert.assertEquals("2021-01-25 11:17:09.099+0000", timestamp);

	    }
	    
	    @Test
	    public void testGetTime() {

	    	String dateString = "2021-01-25 11:17:09.099+0000";
	    	
	        //final Time time = Parser.getTime(dateString);

			//Assert.assertEquals(0, time.getMinDst());
			//Assert.assertEquals(0, time.getMinOffsetFromGMT());
			//Assert.assertEquals(1611573429099L, time.getTimstamp());

	    }

	    @Test
	    public void testParseSentence() {

	    	Event e = getParsedEvent(singlestoreString);

	    	JsonObject inputData = inputData(e);

	        //final Sentence sentence = Parser.parseSentence(inputData);

			//Assert.assertEquals("MATCH", sentence.getVerb());
			//Assert.assertEquals("player", sentence.getObjects().get(0).name.trim());
			//Assert.assertEquals("graph", sentence.getObjects().get(0).type);

	    }
	    
	    @Test
	    public void testParseRedactedSensitiveDataSql() {
	    	
	    	Event e = getParsedEvent(singlestoreString);
	    	
	    	JsonObject inputData = inputData(e);
	        
	    	//final String redacted = Parser.parseRedactedSensitiveDataSql(inputData);

			//Assert.assertEquals("2021-08-06 17:09:40.008+0000 INFO  9 ms: (planning: 1, waiting: 0) - 0 B - 4 page hits, 0 page faults - bolt-session	bolt	neo4j-browser/v4.3.1		client/127.0.0.1:51372	server/127.0.0.1:11004>	neo4j - neo4j - MATCH (Ishant:player {name: 'Ishant Sharma', YOB: 1988, POB: 'Delhi'}) DETACH DELETE Ishant - {} - runtime=slotted - {type: 'user-direct', app: 'neo4j-browser_v4.3.1'}", redacted);

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
			if(e.getField(Constants.SERVER_HOSTNAME).toString() != null && e.getField(Constants.SERVER_HOSTNAME).toString().isEmpty()){
				data.addProperty(Constants.SERVER_HOSTNAME, e.getField(Constants.SERVER_HOSTNAME).toString());
			}
			if(e.getField(Constants.TIMESTAMP).toString() != null && !e.getField(Constants.TIMESTAMP).toString().isEmpty()){
				data.addProperty(Constants.TIMESTAMP, e.getField(Constants.TIMESTAMP).toString());
			}
			if(e.getField(Constants.SERVER_PORT).toString() != null && !e.getField(Constants.SERVER_PORT).toString().isEmpty()){
				data.addProperty(Constants.SERVER_PORT, e.getField(Constants.SERVER_PORT).toString());
			}
			if(e.getField(Constants.DB_USER).toString() != null && !e.getField(Constants.DB_USER).toString().isEmpty()){
				data.addProperty(Constants.DB_USER, e.getField(Constants.DB_USER).toString());
			}
			if(e.getField(Constants.DB_NAME).toString() != null && !e.getField(Constants.DB_NAME).toString().isEmpty()){
				data.addProperty(Constants.DB_NAME, e.getField(Constants.DB_NAME).toString());
			}
			if(e.getField(Constants.QUERY_STATEMENT).toString() != null && !e.getField(Constants.QUERY_STATEMENT).toString().isEmpty()){
				data.addProperty(Constants.QUERY_STATEMENT, e.getField(Constants.QUERY_STATEMENT).toString());
			}
			if(e.getField(Constants.MESSAGE).toString() != null && !e.getField(Constants.MESSAGE).toString().isEmpty()){
				data.addProperty(Constants.MESSAGE, e.getField(Constants.MESSAGE).toString());
			}
			return data;
		}


	public static Event getParsedEvent(String logEvent) {
		try {

			String[] values=logEvent.split(",");

			String server_hostname=values[3].split(":")[0];
			String server_port=values[3].split(":")[1];

			Event e = new org.logstash.Event();

			e.setField("message", logEvent);
			e.setField(Constants.CLIENT_IP, "0.0.0.0");
			e.setField(Constants.DB_NAME, values[8]);

			if(values[5].equals("USER_LOGIN")){
				e.setField(Constants.CLIENT_IP, values[8]);
				e.setField(Constants.DB_NAME, "");
			}
			e.setField(Constants.SERVER_IP, "0.0.0.0");
			e.setField(Constants.SERVER_HOSTNAME,server_hostname);
			e.setField(Constants.SERVER_PORT,server_port);
			e.setField(Constants.TIMESTAMP, values[1]+values[2]);
			e.setField(Constants.DB_USER, values[7]);

			if(values.length>12){
				for (int i=12;i<values.length;i++){
					values[11]=values[11]+","+values[i];
				}

			}
			e.setField(Constants.QUERY_STATEMENT, values[11]);
			return e;
		}
		catch (Exception e){
			System.out.println(e.getMessage());
			return null;
		}

	}
}


