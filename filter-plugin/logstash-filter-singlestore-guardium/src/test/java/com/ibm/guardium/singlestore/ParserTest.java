//
// Copyright 2020-2021 IBM Inc. All rights reserved
// SPDX-License-Identifier: Apache2.0
//

package com.ibm.guardium.singlestore;

import co.elastic.logstash.api.Event;
import com.google.gson.JsonObject;
import com.ibm.guardium.universalconnector.commons.structures.*;
import com.ibm.guardium.universalconnector.commons.structures.Record;

import java.util.ArrayList;

import org.junit.Assert;
import org.junit.Test;

public class ParserTest {

	String singlestoreString = "133855,2024-06-24 07:16:16.901,UTC,53b9ae806c1c:3306,agg,1,100000,root,vector_db,,1308432953418920798,CREATE DATABASE uc_vector_db";

		@Test
	    public void testParseSessionLocator() {
	    	Event e = getParsedEvent(singlestoreString);
	    	JsonObject inputData = inputData(e);
			final SessionLocator result = Parser.parseSessionLocator(inputData);

			SessionLocator expectedSessionLocator = new SessionLocator();
			expectedSessionLocator.setClientIp(Constants.CLIENT_IP_VALUE); 
			expectedSessionLocator.setClientPort(Constants.CLIENT_PORT_VALUE);
			expectedSessionLocator.setServerIp(Constants.SERVER_IP_VALUE);
			expectedSessionLocator.setServerPort(3306);

			Assert.assertEquals(expectedSessionLocator.getClientIp(), result.getClientIp());
			Assert.assertEquals(expectedSessionLocator.getClientPort(), result.getClientPort());
			Assert.assertEquals(expectedSessionLocator.getServerIp(), result.getServerIp());
			Assert.assertEquals(expectedSessionLocator.getServerPort(), result.getServerPort());
	    }

		@Test
	    public void testParseAccessor() {
	    	Event e = getParsedEvent(singlestoreString);
	    	JsonObject inputData = inputData(e);
			final Accessor result = Parser.parseAccessor(inputData, "vector_db");

			Accessor expectedAccessor = new Accessor();
			expectedAccessor.setDbUser("root");
			expectedAccessor.setServiceName("vector_db");
			expectedAccessor.setLanguage("FREE_TEXT");
			expectedAccessor.setDataType("CONSTRUCT");
			expectedAccessor.setServerType(Constants.SERVER_TYPE_STRING);
			expectedAccessor.setDbProtocol(Constants.DB_PROTOCOL);

			Assert.assertEquals(expectedAccessor.getDbUser(), result.getDbUser());
			Assert.assertEquals(expectedAccessor.getServiceName(), result.getServiceName());
			Assert.assertEquals(expectedAccessor.getLanguage(), result.getLanguage());
			Assert.assertEquals(expectedAccessor.getDataType(), result.getDataType());
			Assert.assertEquals(expectedAccessor.getServerType(), result.getServerType());
			Assert.assertEquals(expectedAccessor.getDbProtocol(), result.getDbProtocol());
	    }
	    
	    @Test
	    public void testGetTime() {
     		Event e = getParsedEvent(singlestoreString);
	    	JsonObject inputData = inputData(e);
	        final Time time = Parser.getTime(inputData);

			Assert.assertEquals(0, time.getMinDst());
			Assert.assertEquals(0, time.getMinOffsetFromGMT());
			Assert.assertEquals(1719213376901L, time.getTimstamp());
	    }
	    
		@Test 
		public void testParseQuery() {
			String testQuery = "INSERT INTO test_table";
			Sentence result = Parser.parseQuery(testQuery);
			Sentence expected = new Sentence("INSERT INTO");
  			SentenceObject sentenceObject = new SentenceObject("test_table");
			sentenceObject.setType(Constants.TYPE);
			expected.getObjects().add(sentenceObject);

			Assert.assertEquals(expected.getVerb(), result.getVerb());
			Assert.assertEquals(expected.getObjects().get(0).getName(), result.getObjects().get(0).getName());
			Assert.assertEquals(expected.getObjects().get(0).getType(), result.getObjects().get(0).getType());
		}

		@Test
		public void testParseData() {
  			Event e = getParsedEvent(singlestoreString);
	    	JsonObject inputData = inputData(e);
			Data result = Parser.parseData(inputData);

			Data expectedData = new Data();
			Construct expectedConstruct = new Construct();
			expectedConstruct.setFullSql("CREATE DATABASE uc_vector_db");
			expectedData.setConstruct(expectedConstruct);
			expectedData.setConstruct(expectedConstruct);
			Sentence expectedSentence = new Sentence("CREATE DATABASE");

			ArrayList<Sentence> expectedSentences = new ArrayList<Sentence>();
			expectedSentences.add(expectedSentence);
			expectedConstruct.setSentences(expectedSentences);

			Assert.assertEquals(expectedData.getConstruct().getFullSql(), result.getConstruct().getFullSql());
		}

		@Test 
		public void testParseExceptionRecord() { 
			String failedLoginString = "1546,2025-05-07 11:07:20.523,PDT,53b9ae806c1c:3308,leaf,USER_LOGIN,99985,root,localhost,,password,FAILURE: Access denied";
			Event e = getParsedEvent(failedLoginString);
	    	JsonObject inputData = inputData(e);
			Record result = Parser.parseExceptionRecord(inputData);
			
			Record expectedRecord = new Record();
			ExceptionRecord expectedException = new ExceptionRecord(); 
			expectedException.setExceptionTypeId("LOGIN_FAILED");
			expectedException.setDescription("Login Failed (FAILURE: Access denied)");
			expectedRecord.setException(expectedException);

			Assert.assertEquals(expectedRecord.getException().getDescription(), result.getException().getDescription());
			Assert.assertEquals(expectedRecord.getException().getExceptionTypeId(), result.getException().getExceptionTypeId());
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


