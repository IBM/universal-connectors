//
// Copyright 2020- IBM Inc. All rights reserved
// SPDX-License-Identifier: Apache2.0
//
package com.ibm.guardium.couchbasedb;

import java.text.ParseException;

import org.junit.Assert;
import org.junit.Test;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ibm.guardium.universalconnector.commons.structures.Accessor;
import com.ibm.guardium.universalconnector.commons.structures.Construct;
import com.ibm.guardium.universalconnector.commons.structures.Record;
import com.ibm.guardium.universalconnector.commons.structures.Sentence;
import com.ibm.guardium.universalconnector.commons.structures.SessionLocator;

public class ParserTest {

    Parser parser = new Parser();
    final String couchbaseString = "{\"clientContextId\":\"5c5476f0-e46d-4a6a-a19b-982a5fea056f\",\"description\":\"A N1QL SELECT statement was executed\",\"id\":28672,\"isAdHoc\":true,\"metrics\":{\"elapsedTime\":\"29.1335ms\",\"executionTime\":\"29.0167ms\",\"resultCount\":3,\"resultSize\":217,\"sortCount\":3},\"name\":\"SELECT statement\",\"node\":\"127.0.0.1:8091\",\"real_userid\":{\"domain\":\"builtin\",\"user\":\"Administrator\"},\"remote\":{\"ip\":\"127.0.0.1\",\"port\":40389},\"requestId\":\"f5592288-044f-453b-bbba-dc7d49e4aeb4\",\"statement\":\"SELECT name,phone FROM `travel-sample` WHERE type=\\\"hotel\\\" AND city=\\\"Manchester\\\" and directions IS NOT MISSING ORDER BY name LIMIT 10;\",\"status\":\"success\",\"timestamp\":\"2021-05-20T14:41:21.444Z\",\"userAgent\":\"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/90.0.4430.212 Safari/537.36 (Couchbase Query Workbench (6.6.2-9588-enterprise))\"}";
    final JsonObject couchbaseJson = JsonParser.parseString(couchbaseString).getAsJsonObject();
      
    //ADMIN REST API
    @Test 
    public void testForAdminRestAPI() throws ParseException {
        final String couchbaseString = "{\"description\":\"An HTTP request was made to the API at /admin/config.\",\"httpMethod\":\"GET\",\"httpResultCode\":200,\"id\":28698,\"name\":\"/admin/config API request\",\"real_userid\":{\"domain\":\"builtin\",\"user\":\"Administrator\"},\"remote\":{\"ip\":\"172.17.0.1\",\"port\":49480},\"timestamp\":\"2021-05-26T10:00:46.406Z\"}";
        final JsonObject couchbaseJson = JsonParser.parseString(couchbaseString).getAsJsonObject();
        final Record record = Parser.parseRecord(couchbaseJson);

        final Construct construct = record.getData().getConstruct();
        final Sentence sentence = construct.sentences.get(0);
        
        Assert.assertEquals("GET", sentence.getVerb());
        Assert.assertEquals("/admin/config API request", sentence.getObjects().get(0).name);

    }
    
    @Test 
    public void testForN1QLRestAPI() throws ParseException {
        final String couchbaseString = "{\"description\":\"A N1QL SELECT statement was executed\",\"id\":28672,\"isAdHoc\":true,\"metrics\":{\"elapsedTime\":\"68.6619ms\",\"executionTime\":\"68.5632ms\",\"resultCount\":5,\"resultSize\":82},\"name\":\"SELECT statement\",\"node\":\"127.0.0.1:8091\",\"real_userid\":{\"domain\":\"local\",\"user\":\"Administrator\"},\"remote\":{\"ip\":\"172.17.0.1\",\"port\":58438},\"requestId\":\"3e7e21ab-5530-4a99-8738-c8f9d3d00485\",\"statement\":\"SELECT distinct city FROM `travel-sample` LIMIT 5\",\"status\":\"success\",\"timestamp\":\"2021-05-25T15:05:59.334Z\",\"userAgent\":\"curl/7.55.1\"}";
        final JsonObject couchbaseJson = JsonParser.parseString(couchbaseString).getAsJsonObject();
        
        final Record record = Parser.parseRecord(couchbaseJson);

        Assert.assertEquals(Constants.TEXT, record.getAccessor().getDataType());
        Assert.assertEquals(null, record.getException());
        Assert.assertNotNull(record.getData());
    }
    
    //Exception handling - Syntactically wrong query
    @Test 
    public void testForErrorHandling() throws ParseException {
        final String couchbaseString = "{\"clientContextId\":\"79f105ab-075b-41ff-b9d9-2f898ee2be31\",\"description\":\"An unrecognized statement was received by the N1QL query engine\",\"id\":28687,\"isAdHoc\":true,\"metrics\":{\"elapsedTime\":\"1.378ms\",\"errorCount\":1,\"executionTime\":\"1.1121ms\",\"resultCount\":0,\"resultSize\":0},\"name\":\"UNRECOGNIZED statement\",\"node\":\"127.0.0.1:8091\",\"real_userid\":{\"domain\":\"builtin\",\"user\":\"Administrator\"},\"remote\":{\"ip\":\"127.0.0.1\",\"port\":35563},\"requestId\":\"142d2fce-c4fd-48df-99f7-be91232a8e7b\",\"statement\":\"SELECT * FROM beer-sample` limit 5;\",\"status\":\"fatal\",\"timestamp\":\"2021-06-21T12:13:43.492Z\",\"userAgent\":\"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.106 Safari/537.36 (Couchbase Query Workbench (6.6.2-9600-enterprise))\"}";
        final JsonObject couchbaseJson = JsonParser.parseString(couchbaseString).getAsJsonObject();
        
        final Record record = Parser.parseRecord(couchbaseJson);
        
        Assert.assertEquals("28687", record.getException().getExceptionTypeId());
        Assert.assertEquals("An unrecognized statement was received by the N1QL query engine", record.getException().getDescription());
    }
    
  //Exception handling - When same key is tried to insert again
    @Test 
    public void testForErrorHandling2() throws ParseException {
        final String couchbaseString = "{\"clientContextId\":\"50b52e66-5627-4fc5-a54a-89f02075b8a5\",\"description\":\"A N1QL INSERT statement was executed\",\"id\":28676,\"isAdHoc\":true,\"metrics\":{\"elapsedTime\":\"10.2109ms\",\"errorCount\":1,\"executionTime\":\"10.066ms\",\"resultCount\":0,\"resultSize\":0},\"name\":\"INSERT statement\",\"node\":\"127.0.0.1:8091\",\"real_userid\":{\"domain\":\"builtin\",\"user\":\"Administrator\"},\"remote\":{\"ip\":\"127.0.0.1\",\"port\":48977},\"requestId\":\"4daa776c-37cb-473a-9fc2-f6f65709d06d\",\"statement\":\"INSERT INTO `travel-sample` (KEY, VALUE) VALUES ( \\\"airline::432\\\", { \\\"callsign\\\": \\\"\\\", \\\"country\\\" : \\\"USA\\\", \\\"type\\\" : \\\"airline\\\"} ) RETURNING META().id as docid;\",\"status\":\"errors\",\"timestamp\":\"2021-06-21T12:21:00.427Z\",\"userAgent\":\"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.106 Safari/537.36 (Couchbase Query Workbench (6.6.2-9600-enterprise))\"}";
        final JsonObject couchbaseJson = JsonParser.parseString(couchbaseString).getAsJsonObject();
        final Record record = Parser.parseRecord(couchbaseJson);
        
        Assert.assertEquals("28676", record.getException().getExceptionTypeId());
        Assert.assertEquals("A N1QL INSERT statement was executed", record.getException().getDescription());
    }
    
    
    //Session Locator values verified.
    @Test
    public void testForSessionLocator() throws ParseException {
    	SessionLocator sessionLocator = Parser.parseSessionLocator(couchbaseJson);
    	
        Assert.assertEquals(40389, sessionLocator.getClientPort());
        Assert.assertEquals(8091, sessionLocator.getServerPort());
        Assert.assertEquals(false, sessionLocator.isIpv6());

    } 
    
  //Accessor values verified.
    @Test
    public void testForAccessor() throws ParseException {
    	Accessor accessor = Parser.parseAccessor(couchbaseJson);
        
        Assert.assertEquals(Constants.DATA_PROTOCOL_STRING, accessor.getDbProtocol());
        Assert.assertEquals(Constants.SERVER_TYPE_STRING, accessor.getServerType());
        Assert.assertEquals(Constants.COUCHB_LANGUAGE, accessor.getLanguage());

    }
    
  //test sessionID
    @Test 
    public void testForSessionID() throws ParseException {
    	
    	final String testLog="{\"description\":\"User session has ended due to a timeout\",\"id\":8258,\"name\":\"session timeout\",\"real_userid\":{\"domain\":\"builtin\",\"user\":\"Administrator\"},\"sessionid\":\"83c0783df26e53ba47ad9b21f86e7c8eefb0f7fc\",\"timestamp\":\"2021-06-24T08:16:11.927Z\"}";
        final JsonObject testJson = JsonParser.parseString(testLog).getAsJsonObject();
        final String sessionID = Parser.parseSessionID(testJson);        
	
        Assert.assertEquals("83c0783df26e53ba47ad9b21f86e7c8eefb0f7fc",sessionID);
    }
    
    @Test
    public void testParseTimestamp() throws ParseException {
    	long time1 = Parser.parseTimestamp(couchbaseJson).getTimstamp();
        final String testString = "{\"description\":\"A N1QL SELECT statement was executed\",\"id\":28672,\"isAdHoc\":true,\"metrics\":{\"elapsedTime\":\"68.6619ms\",\"executionTime\":\"68.5632ms\",\"resultCount\":5,\"resultSize\":82},\"name\":\"SELECT statement\",\"node\":\"127.0.0.1:8091\",\"real_userid\":{\"domain\":\"local\",\"user\":\"Administrator\"},\"remote\":{\"ip\":\"172.17.0.1\",\"port\":58438},\"requestId\":\"3e7e21ab-5530-4a99-8738-c8f9d3d00485\",\"statement\":\"SELECT distinct city FROM `travel-sample` LIMIT 5\",\"status\":\"success\",\"timestamp\":\"2021-05-25T15:05:59.334Z\",\"userAgent\":\"curl/7.55.1\"}";
        final JsonObject testJson = JsonParser.parseString(testString).getAsJsonObject();
        long time2 = Parser.parseTimestamp(testJson).getTimstamp();
        Assert.assertNotEquals(time1, time2);
    }
    
    @Test 
    public void testForUIGeneratedFrequentLogs() throws ParseException {
    	
    	//frequency very high
    	final String dummy="{\"clientContextId\":\"INTERNAL-6220dcc1-6e1b-49a0-a368-3d30b806a1cc\",\"description\":\"A N1QL SELECT statement was executed\",\"id\":28672,\"isAdHoc\":true,\"metrics\":{\"elapsedTime\":\"19.5885ms\",\"executionTime\":\"19.5122ms\",\"resultCount\":1,\"resultSize\":42},\"name\":\"SELECT statement\",\"node\":\"127.0.0.1:8091\",\"real_userid\":{\"domain\":\"builtin\",\"user\":\"Administrator\"},\"remote\":{\"ip\":\"127.0.0.1\",\"port\":39923},\"requestId\":\"341f2c0f-4024-4f20-82e1-154006b5dd7c\",\"statement\":\"select raw {\\\"beer-sample\\\" : (select raw count(*) from `beer-sample`)[0],\\\"travel-sample\\\" : (select raw count(*) from `travel-sample`)[0]}\",\"status\":\"success\",\"timestamp\":\"2021-06-24T08:49:39.430Z\",\"userAgent\":\"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.114 Safari/537.36 (Couchbase Query Workbench (6.6.2-9600-enterprise))\"}";
        final JsonObject dummyJson = JsonParser.parseString(dummy).getAsJsonObject();
        final Boolean b = Parser.checkForUIGeneratedQueries(dummyJson);
        Assert.assertEquals(true,b);        
        
    }
    
    @Test 
    public void nullCheck() throws ParseException {
        
        final String testString = "{\"clientContextId\":\"INTERNAL-99da26de-9a02-4555-a203-b75ae74d51fb\",\"description\":\"A N1QL SELECT statement was executed\",\"id\":28672,\"isAdHoc\":true,\"metrics\":{\"elapsedTime\":\"23.2187ms\",\"executionTime\":\"23.1437ms\",\"resultCount\":1,\"resultSize\":42},\"name\":\"SELECT statement\",\"node\":null,\"real_userid\":{\"domain\":\"builtin\",\"user\":\"Administrator\"},\"remote\":{\"ip\":\"127.0.0.1\",\"port\":56585},\"requestId\":\"42b4fe03-9de9-4a71-89a7-473e86a40072\",\"statement\":\"select raw {\\\"beer-sample\\\" : (select raw count(*) from `beer-sample`)[0],\\\"travel-sample\\\" : (select raw count(*) from `travel-sample`)[0]}\",\"status\":\"success\",\"timestamp\":\"2021-06-24T08:48:49.426Z\",\"userAgent\":\"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.114 Safari/537.36 (Couchbase Query Workbench (6.6.2-9600-enterprise))\"}";
        final JsonObject testCouchbaseJson = JsonParser.parseString(testString).getAsJsonObject();
        final SessionLocator sessionLocator = Parser.parseSessionLocator(testCouchbaseJson);

        Assert.assertEquals(sessionLocator.getClientIp(),"127.0.0.1");
        Assert.assertEquals(sessionLocator.getClientPort(),56585);
    }
}