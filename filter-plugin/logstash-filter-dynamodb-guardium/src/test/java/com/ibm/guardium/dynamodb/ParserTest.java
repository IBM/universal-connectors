//
// Copyright 2020-2021 IBM Inc. All rights reserved
// SPDX-License-Identifier: Apache2.0
//
package com.ibm.guardium.dynamodb;

import java.text.ParseException;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ibm.guardium.univer***REMOVED***lconnector.commons.structures.*;

import org.junit.Assert;
import org.junit.Test;

public class ParserTest {

    Parser parser = new Parser();
    final String dynamoString = "{\"eventVersion\": \"1.08\", \"userIdentity\": {\"type\": \"Root\", \"principalId\": \"083406524166\", \"arn\": \"arn:aws:iam::083406524166:root\", \"accountId\": \"083406524166\", \"accessKeyId\": \"ASIARG23KR4DPKFEC4ZJ\", \"sessionContext\": { \"attributes\": {  \"creationDate\": \"2020-12-28T07:15:10Z\", \"mfaAuthenticated\": \"false\" } } },\"eventTime\": \"2020-12-28T07:35:21Z\",\"eventSource\": \"dynamodb.amazonaws.com\",   \"eventName\": \"CreateTable\",    \"awsRegion\": \"ap-south-1\", \"sourceIPAddress\": \"103.62.17.201\", \"userAgent\": \"console.mazonaws.com\", \"requestParameters\": {\"attributeDefinitions\": [{ \"attributeName\": \"Employee Number\", \"attributeType\": \"N\" }], \"tableName\": \"Employee\", \"keySchema\": [ {\"attributeName\": \"Employee Number\", \"keyType\": \"HASH\" } ],\"billingMode\": \"PROVISIONED\", \"provisionedThroughput\": { \"readCapacityUnits\": 5,  \"writeCapacityUnits\": 5 }, \"sSESpecification\": { \"enabled\": false } }, \"responseElements\": { \"tableDescription\": {  \"attributeDefinitions\": [ {  \"attributeName\": \"Employee Number\",\"attributeType\": \"N\"} ],   \"tableName\": \"Employee\",  \"keySchema\": [{ \"attributeName\": \"Employee Number\", \"keyType\": \"HASH\"  } ], \"tableStatus\": \"CREATING\", \"creationDateTime\": \"Dec 28, 2020, 7:35:21 AM\",  \"provisionedThroughput\": { \"numberOfDecreasesToday\": 0, \"readCapacityUnits\": 5, \"writeCapacityUnits\": 5},\"tableSizeBytes\": 0,\"itemCount\": 0,\"tableArn\": \"arn:aws:dynamodb:ap-south-1:083406524166:table/Employee\",\"tableId\": \"0bbd1d31-624b-4a0a-b804-4a7bc7351e3e\"}},\"requestID\": \"4NR3PQIDKFSKJGELV8EBNN1BNJVV4KQNSO5AEMVJF66Q9ASUAAJG\",\"eventID\": \"79151c71-058e-4031-8f2d-adbac87198a2\",\"readOnly\": false,\"resources\": [{\"accountId\": \"083406524166\",\"type\": \"AWS::DynamoDB::Table\",\"ARN\": \"arn:aws:dynamodb:ap-south-1:083406524166:table/Employee\"}],\"eventType\": \"AwsApiCall\",\"apiVersion\": \"2012-08-10\",\"managementEvent\": true,\"recipientAccountId\": \"083406524166\",\"eventCategory\": \"Management\"}";
    final JsonObject dynamoJson = JsonParser.parseString(dynamoString).getAsJsonObject();

    //CreateTable
    @Test
    public void testParseRecord_CreateTable() throws ParseException {
        final String dynamoString = "{\"eventVersion\": \"1.08\", \"userIdentity\": {\"type\": \"Root\", \"principalId\": \"083406524166\", \"arn\": \"arn:aws:iam::083406524166:root\", \"accountId\": \"083406524166\", \"accessKeyId\": \"ASIARG23KR4DPKFEC4ZJ\", \"sessionContext\": { \"attributes\": {  \"creationDate\": \"2020-12-28T07:15:10Z\", \"mfaAuthenticated\": \"false\" } } },\"eventTime\": \"2020-12-28T07:35:21Z\",\"eventSource\": \"dynamodb.amazonaws.com\",   \"eventName\": \"CreateTable\",    \"awsRegion\": \"ap-south-1\", \"sourceIPAddress\": \"103.62.17.201\", \"userAgent\": \"console.mazonaws.com\", \"requestParameters\": {\"attributeDefinitions\": [{ \"attributeName\": \"Employee Number\", \"attributeType\": \"N\" }], \"tableName\": \"Employee\", \"keySchema\": [ {\"attributeName\": \"Employee Number\", \"keyType\": \"HASH\" } ],\"billingMode\": \"PROVISIONED\", \"provisionedThroughput\": { \"readCapacityUnits\": 5,  \"writeCapacityUnits\": 5 }, \"sSESpecification\": { \"enabled\": false } }, \"responseElements\": { \"tableDescription\": {  \"attributeDefinitions\": [ {  \"attributeName\": \"Employee Number\",\"attributeType\": \"N\"} ],   \"tableName\": \"Employee\",  \"keySchema\": [{ \"attributeName\": \"Employee Number\", \"keyType\": \"HASH\"  } ], \"tableStatus\": \"CREATING\", \"creationDateTime\": \"Dec 28, 2020, 7:35:21 AM\",  \"provisionedThroughput\": { \"numberOfDecreasesToday\": 0, \"readCapacityUnits\": 5, \"writeCapacityUnits\": 5},\"tableSizeBytes\": 0,\"itemCount\": 0,\"tableArn\": \"arn:aws:dynamodb:ap-south-1:083406524166:table/Employee\",\"tableId\": \"0bbd1d31-624b-4a0a-b804-4a7bc7351e3e\"}},\"requestID\": \"4NR3PQIDKFSKJGELV8EBNN1BNJVV4KQNSO5AEMVJF66Q9ASUAAJG\",\"eventID\": \"79151c71-058e-4031-8f2d-adbac87198a2\",\"readOnly\": false,\"resources\": [{\"accountId\": \"083406524166\",\"type\": \"AWS::DynamoDB::Table\",\"ARN\": \"arn:aws:dynamodb:ap-south-1:083406524166:table/Employee\"}],\"eventType\": \"AwsApiCall\",\"apiVersion\": \"2012-08-10\",\"managementEvent\": true,\"recipientAccountId\": \"083406524166\",\"eventCategory\": \"Management\"}";
        final JsonObject dynamoJson = JsonParser.parseString(dynamoString).getAsJsonObject();

        final Record record = Parser.parseRecord(dynamoJson);

        final Construct construct = record.getData().getConstruct();
        final Sentence sentence = construct.sentences.get(0);

        Assert.assertEquals("CreateTable", sentence.getVerb());
        Assert.assertEquals("Employee", sentence.getObjects().get(0).name);

    }

    //Exception handling
    @Test
    public void testParseRecord_Error() throws ParseException {
        final String dynamoString = "{ \"eventVersion\": \"1.08\", \"userIdentity\": { \"type\": \"IAMUser\", \"principalId\": \"AIDAVBQDAZ24R743XVEUH\", \"arn\": \"arn:aws:iam::346824953529:user/rasika.shete\", \"accountId\": \"346824953529\", \"accessKeyId\": \"AKIAVBQDAZ24XZ4CMVXK\", \"userName\": \"rasika.shete\" }, \"eventTime\": \"2021-01-11T18:03:56Z\", \"eventSource\": \"dynamodb.amazonaws.com\", \"eventName\": \"UpdateTable\", \"awsRegion\": \"ap-south-1\", \"sourceIPAddress\": \"103.62.16.216\", \"userAgent\": \"aws-cli/2.1.15 Python/3.7.9 Windows/10 exe/AMD64 prompt/off command/dynamodb.update-table\", \"errorCode\": \"ResourceNotFoundException\", \"errorMes***REMOVED***ge\": \"Requested resource not found: Table: Music12 not found\", \"requestParameters\": { \"tableName\": \"Music12\", \"provisionedThroughput\": {  \"readCapacityUnits\": 20,  \"writeCapacityUnits\": 10 } }, \"responseElements\": null, \"requestID\": \"LBQJMQUKQNU73MVN8GHAGNBDDNVV4KQNSO5AEMVJF66Q9ASUAAJG\", \"eventID\": \"e1325551-1a43-453d-a188-5f91ba136c6a\", \"readOnly\": false, \"resources\": [ {  \"accountId\": \"346824953529\",  \"type\": \"AWS::DynamoDB::Table\",  \"ARN\": \"arn:aws:dynamodb:ap-south-1:346824953529:table/Music12\" } ], \"eventType\": \"AwsApiCall\", \"apiVersion\": \"2012-08-10\", \"managementEvent\": true, \"recipientAccountId\": \"346824953529\", \"eventCategory\": \"Management\"}";
        final JsonObject dynamoJson = JsonParser.parseString(dynamoString).getAsJsonObject();

        final Record record = Parser.parseRecord(dynamoJson);
        Assert.assertEquals("SQL_ERROR", record.getException().getExceptionTypeId());
        Assert.assertEquals("Requested resource not found: Table: Music12 not found", record.getException().getDescription());
    }

    //Date is not Parsed, only time is parsed.
    @Test
    public void testTimeParing() throws ParseException {
        String dateStr = "2020-12-28T07:35:21Z";
        Time time = Parser.getTime(dateStr);
        Assert.assertTrue("Failed to parse date, time is "+time.getTimstamp(), 1609140921000L==time.getTimstamp());

    }

    @Test
    public void testParseAsConstruct_CreateTable() {
        final String dynamoString = "{\"eventVersion\": \"1.08\", \"userIdentity\": {\"type\": \"Root\", \"principalId\": \"083406524166\", \"arn\": \"arn:aws:iam::083406524166:root\", \"accountId\": \"083406524166\", \"accessKeyId\": \"ASIARG23KR4DPKFEC4ZJ\", \"sessionContext\": { \"attributes\": {  \"creationDate\": \"2020-12-28T07:15:10Z\", \"mfaAuthenticated\": \"false\" } } },\"eventTime\": \"2020-12-28T07:35:21Z\",\"eventSource\": \"dynamodb.amazonaws.com\",   \"eventName\": \"CreateTable\",    \"awsRegion\": \"ap-south-1\", \"sourceIPAddress\": \"103.62.17.201\", \"userAgent\": \"console.mazonaws.com\", \"requestParameters\": {\"attributeDefinitions\": [{ \"attributeName\": \"Employee Number\", \"attributeType\": \"N\" }], \"tableName\": \"Employee\", \"keySchema\": [ {\"attributeName\": \"Employee Number\", \"keyType\": \"HASH\" } ],\"billingMode\": \"PROVISIONED\", \"provisionedThroughput\": { \"readCapacityUnits\": 5,  \"writeCapacityUnits\": 5 }, \"sSESpecification\": { \"enabled\": false } }, \"responseElements\": { \"tableDescription\": {  \"attributeDefinitions\": [ {  \"attributeName\": \"Employee Number\",\"attributeType\": \"N\"} ],   \"tableName\": \"Employee\",  \"keySchema\": [{ \"attributeName\": \"Employee Number\", \"keyType\": \"HASH\"  } ], \"tableStatus\": \"CREATING\", \"creationDateTime\": \"Dec 28, 2020, 7:35:21 AM\",  \"provisionedThroughput\": { \"numberOfDecreasesToday\": 0, \"readCapacityUnits\": 5, \"writeCapacityUnits\": 5},\"tableSizeBytes\": 0,\"itemCount\": 0,\"tableArn\": \"arn:aws:dynamodb:ap-south-1:083406524166:table/Employee\",\"tableId\": \"0bbd1d31-624b-4a0a-b804-4a7bc7351e3e\"}},\"requestID\": \"4NR3PQIDKFSKJGELV8EBNN1BNJVV4KQNSO5AEMVJF66Q9ASUAAJG\",\"eventID\": \"79151c71-058e-4031-8f2d-adbac87198a2\",\"readOnly\": false,\"resources\": [{\"accountId\": \"083406524166\",\"type\": \"AWS::DynamoDB::Table\",\"ARN\": \"arn:aws:dynamodb:ap-south-1:083406524166:table/Employee\"}],\"eventType\": \"AwsApiCall\",\"apiVersion\": \"2012-08-10\",\"managementEvent\": true,\"recipientAccountId\": \"083406524166\",\"eventCategory\": \"Management\"}";

        final JsonObject dynamoJson = JsonParser.parseString(dynamoString).getAsJsonObject();
        final Construct result = Parser.parseAsConstruct(dynamoJson);

        final Sentence sentence = result.sentences.get(0);
        Assert.assertEquals("CreateTable", sentence.getVerb());
        Assert.assertEquals("Employee", sentence.getObjects().get(0).name);
        Assert.assertEquals("table", sentence.getObjects().get(0).type);
    }

    @Test
    public void testParseAsConstruct_updateTable() {
        final String dynamoString = "{\"eventVersion\": \"1.08\", \"userIdentity\": { \"type\": \"IAMUser\", \"principalId\": \"AIDAVBQDAZ24R743XVEUH\", \"arn\": \"arn:aws:iam::346824953529:user/rasika.shete\", \"accountId\": \"346824953529\", \"accessKeyId\": \"AKIAVBQDAZ24XZ4CMVXK\", \"userName\": \"rasika.shete\" }, \"eventTime\": \"2021-01-11T09:53:19Z\", \"eventSource\": \"dynamodb.amazonaws.com\", \"eventName\": \"UpdateTable\", \"awsRegion\": \"ap-south-1\", \"sourceIPAddress\": \"103.62.16.216\", \"userAgent\": \"aws-cli/2.1.15 Python/3.7.9 Windows/10 exe/AMD64 prompt/off command/dynamodb.update-table\", \"requestParameters\": { \"tableName\": \"Music\", \"provisionedThroughput\": { \"readCapacityUnits\": 20, \"writeCapacityUnits\": 10 } }, \"responseElements\": { \"tableDescription\": { \"attributeDefinitions\": [ {  \"attributeName\": \"Artist\",  \"attributeType\": \"S\" }, {  \"attributeName\": \"SongTitle\",  \"attributeType\": \"S\" } ], \"tableName\": \"Music\", \"keySchema\": [ {  \"attributeName\": \"Artist\",  \"keyType\": \"HASH\" }, {  \"attributeName\": \"SongTitle\",  \"keyType\": \"RANGE\" } ], \"tableStatus\": \"UPDATING\", \"creationDateTime\": \"Jan 5, 2021, 7:15:21 AM\", \"provisionedThroughput\": { \"lastIncreaseDateTime\": \"Jan 11, 2021, 9:53:19 AM\", \"numberOfDecreasesToday\": 0, \"readCapacityUnits\": 10, \"writeCapacityUnits\": 5 }, \"tableSizeBytes\": 0, \"itemCount\": 0, \"tableArn\": \"arn:aws:dynamodb:ap-south-1:346824953529:table/Music\", \"tableId\": \"09d11b16-4faf-4038-a028-29726272fcf4\" } }, \"requestID\": \"8PT4I4IJNC2HJLMLBMF5QUMT9NVV4KQNSO5AEMVJF66Q9ASUAAJG\", \"eventID\": \"f268cc2a-ba9d-4cb1-95d5-dee903e18a8b\", \"readOnly\": false, \"resources\": [ { \"accountId\": \"346824953529\", \"type\": \"AWS::DynamoDB::Table\", \"ARN\": \"arn:aws:dynamodb:ap-south-1:346824953529:table/Music\" } ], \"eventType\": \"AwsApiCall\", \"apiVersion\": \"2012-08-10\", \"managementEvent\": true, \"recipientAccountId\": \"346824953529\", \"eventCategory\": \"Management\"}";
        final JsonObject dynamoJson = JsonParser.parseString(dynamoString).getAsJsonObject();
        final Construct result = Parser.parseAsConstruct(dynamoJson);

        final Sentence sentence = result.sentences.get(0);
        Assert.assertEquals("UpdateTable", sentence.getVerb());
        Assert.assertEquals("Music", sentence.getObjects().get(0).name);
    }

    @Test
    public void testParseAsConstruct_deleteTable() {
        final String dynamoString = "{\"eventVersion\": \"1.08\", \"userIdentity\": { \"type\": \"IAMUser\", \"principalId\": \"AIDAVBQDAZ24R743XVEUH\", \"arn\": \"arn:aws:iam::346824953529:user/rasika.shete\", \"accountId\": \"346824953529\", \"accessKeyId\": \"ASIAVBQDAZ247J7EYCZ5\", \"userName\": \"rasika.shete\", \"sessionContext\": {   \"attributes\": {   \"creationDate\": \"2020-12-28T06:58:12Z\",   \"mfaAuthenticated\": \"false\"   } } }, \"eventTime\": \"2020-12-28T08:18:45Z\", \"eventSource\": \"dynamodb.amazonaws.com\", \"eventName\": \"DeleteTable\", \"awsRegion\": \"ap-south-1\", \"sourceIPAddress\": \"103.62.17.201\", \"userAgent\": \"console.amazonaws.com\", \"requestParameters\": { \"tableName\": \"Students\" }, \"responseElements\": { \"tableDescription\": {   \"tableId\": \"97916744-b891-4756-a966-ca73720eaa73\",   \"provisionedThroughput\": {   \"readCapacityUnits\": 5,   \"writeCapacityUnits\": 5,   \"numberOfDecreasesToday\": 0   },   \"tableName\": \"Students\",   \"tableSizeBytes\": 0,   \"tableStatus\": \"DELETING\",   \"tableArn\": \"arn:aws:dynamodb:ap-south-1:346824953529:table/Students\",   \"itemCount\": 0 } }, \"requestID\": \"QKK5BIUO639AHVDAR1NIFO2UJ3VV4KQNSO5AEMVJF66Q9ASUAAJG\", \"eventID\": \"420dc42a-369a-46f4-b9ea-d7b8867bd86b\", \"readOnly\": false, \"resources\": [ {   \"accountId\": \"346824953529\",   \"type\": \"AWS::DynamoDB::Table\",   \"ARN\": \"arn:aws:dynamodb:ap-south-1:346824953529:table/Students\" } ], \"eventType\": \"AwsApiCall\", \"apiVersion\": \"2012-08-10\", \"managementEvent\": true, \"recipientAccountId\": \"346824953529\", \"eventCategory\": \"Management\"}";
        final JsonObject dynamoJson = JsonParser.parseString(dynamoString).getAsJsonObject();
        final Construct result = Parser.parseAsConstruct(dynamoJson);

        final Sentence sentence = result.sentences.get(0);
        Assert.assertEquals("DeleteTable", sentence.getVerb());
        Assert.assertEquals("Students", sentence.getObjects().get(0).name);
    }


	@Test
    public void testSessionLocator_validIP() throws ParseException{
        final String dynamoString = "{\"eventVersion\": \"1.08\", \"userIdentity\": { \"type\": \"IAMUser\", \"principalId\": \"AIDAVBQDAZ24R743XVEUH\", \"arn\": \"arn:aws:iam::346824953529:user/rasika.shete\", \"accountId\": \"346824953529\", \"accessKeyId\": \"ASIAVBQDAZ247J7EYCZ5\", \"userName\": \"rasika.shete\", \"sessionContext\": {   \"attributes\": {   \"creationDate\": \"2020-12-28T06:58:12Z\",   \"mfaAuthenticated\": \"false\"   } } }, \"eventTime\": \"2020-12-28T08:18:45Z\", \"eventSource\": \"dynamodb.amazonaws.com\", \"eventName\": \"DeleteTable\", \"awsRegion\": \"ap-south-1\", \"sourceIPAddress\": \"103.62.17.201\", \"userAgent\": \"console.amazonaws.com\", \"requestParameters\": { \"tableName\": \"Students\" }, \"responseElements\": { \"tableDescription\": {   \"tableId\": \"97916744-b891-4756-a966-ca73720eaa73\",   \"provisionedThroughput\": {   \"readCapacityUnits\": 5,   \"writeCapacityUnits\": 5,   \"numberOfDecreasesToday\": 0   },   \"tableName\": \"Students\",   \"tableSizeBytes\": 0,   \"tableStatus\": \"DELETING\",   \"tableArn\": \"arn:aws:dynamodb:ap-south-1:346824953529:table/Students\",   \"itemCount\": 0 } }, \"requestID\": \"QKK5BIUO639AHVDAR1NIFO2UJ3VV4KQNSO5AEMVJF66Q9ASUAAJG\", \"eventID\": \"420dc42a-369a-46f4-b9ea-d7b8867bd86b\", \"readOnly\": false, \"resources\": [ {   \"accountId\": \"346824953529\",   \"type\": \"AWS::DynamoDB::Table\",   \"ARN\": \"arn:aws:dynamodb:ap-south-1:346824953529:table/Students\" } ], \"eventType\": \"AwsApiCall\", \"apiVersion\": \"2012-08-10\", \"managementEvent\": true, \"recipientAccountId\": \"346824953529\", \"eventCategory\": \"Management\"}";
        final JsonObject dynamoJson = JsonParser.parseString(dynamoString).getAsJsonObject();
        final Record record = Parser.parseRecord(dynamoJson);
        final SessionLocator result = record.getSessionLocator();

        Assert.assertEquals("103.62.17.201", result.getClientIp());
    }

    @Test
    public void testSessionLocator_invalidIP() throws ParseException {
        final String dynamoString = "{\"eventVersion\": \"1.08\", \"userIdentity\": { \"type\": \"IAMUser\", \"principalId\": \"AIDAVBQDAZ24R743XVEUH\", \"arn\": \"arn:aws:iam::346824953529:user/rasika.shete\", \"accountId\": \"346824953529\", \"accessKeyId\": \"ASIAVBQDAZ247J7EYCZ5\", \"userName\": \"rasika.shete\", \"sessionContext\": {   \"attributes\": {   \"creationDate\": \"2020-12-28T06:58:12Z\",   \"mfaAuthenticated\": \"false\"   } } }, \"eventTime\": \"2020-12-28T08:18:45Z\", \"eventSource\": \"dynamodb.amazonaws.com\", \"eventName\": \"DeleteTable\", \"awsRegion\": \"ap-south-1\", \"sourceIPAddress\": \"dynamodb.application-autoscaling.amazonaws.com\", \"userAgent\": \"console.amazonaws.com\", \"requestParameters\": { \"tableName\": \"Students\" }, \"responseElements\": { \"tableDescription\": {   \"tableId\": \"97916744-b891-4756-a966-ca73720eaa73\",   \"provisionedThroughput\": {   \"readCapacityUnits\": 5,   \"writeCapacityUnits\": 5,   \"numberOfDecreasesToday\": 0   },   \"tableName\": \"Students\",   \"tableSizeBytes\": 0,   \"tableStatus\": \"DELETING\",   \"tableArn\": \"arn:aws:dynamodb:ap-south-1:346824953529:table/Students\",   \"itemCount\": 0 } }, \"requestID\": \"QKK5BIUO639AHVDAR1NIFO2UJ3VV4KQNSO5AEMVJF66Q9ASUAAJG\", \"eventID\": \"420dc42a-369a-46f4-b9ea-d7b8867bd86b\", \"readOnly\": false, \"resources\": [ {   \"accountId\": \"346824953529\",   \"type\": \"AWS::DynamoDB::Table\",   \"ARN\": \"arn:aws:dynamodb:ap-south-1:346824953529:table/Students\" } ], \"eventType\": \"AwsApiCall\", \"apiVersion\": \"2012-08-10\", \"managementEvent\": true, \"recipientAccountId\": \"346824953529\", \"eventCategory\": \"Management\"}";
        final JsonObject dynamoJson = JsonParser.parseString(dynamoString).getAsJsonObject();
        final Record record = Parser.parseRecord(dynamoJson);
        final SessionLocator result = record.getSessionLocator();

        Assert.assertEquals("0.0.0.0", result.getClientIp());
    }

    //Accessor values verified.
    @Test
    public void testParseAccessor() throws ParseException {
        Record record = Parser.parseRecord(dynamoJson);
        Accessor actual = record.getAccessor();

        Assert.assertEquals(Constants.DATA_PROTOCOL_STRING, actual.getDbProtocol());
        Assert.assertEquals(Constants.SERVER_TYPE_STRING, actual.getServerType());

        Assert.assertEquals("083406524166:root", actual.getDbUser());

    }

    @Test
    public void testParseTimestamp() {
        String date = Parser.parseTimestamp(dynamoJson);
        Assert.assertEquals("2020-12-28T07:35:21Z", date);
    }

    @Test
    public void testGetTime() throws ParseException {
        String dateString = Parser.parseTimestamp(dynamoJson);
        long time = Parser.getTime(dateString).getTimstamp();

        final String testString = "{\"eventVersion\": \"1.08\", \"userIdentity\": {\"type\": \"Root\", \"principalId\": \"083406524166\", \"arn\": \"arn:aws:iam::083406524166:root\", \"accountId\": \"083406524166\", \"accessKeyId\": \"ASIARG23KR4DPKFEC4ZJ\", \"sessionContext\": { \"attributes\": {  \"creationDate\": \"2020-12-28T07:15:10Z\", \"mfaAuthenticated\": \"false\" } } },\"eventTime\": \"2020-12-28T09:35:21Z\",\"eventSource\": \"dynamodb.amazonaws.com\",   \"eventName\": \"CreateTable\",    \"awsRegion\": \"ap-south-1\", \"sourceIPAddress\": \"103.62.17.201\", \"userAgent\": \"console.mazonaws.com\", \"requestParameters\": {\"attributeDefinitions\": [{ \"attributeName\": \"Employee Number\", \"attributeType\": \"N\" }], \"tableName\": \"Employee\", \"keySchema\": [ {\"attributeName\": \"Employee Number\", \"keyType\": \"HASH\" } ],\"billingMode\": \"PROVISIONED\", \"provisionedThroughput\": { \"readCapacityUnits\": 5,  \"writeCapacityUnits\": 5 }, \"sSESpecification\": { \"enabled\": false } }, \"responseElements\": { \"tableDescription\": {  \"attributeDefinitions\": [ {  \"attributeName\": \"Employee Number\",\"attributeType\": \"N\"} ],   \"tableName\": \"Employee\",  \"keySchema\": [{ \"attributeName\": \"Employee Number\", \"keyType\": \"HASH\"  } ], \"tableStatus\": \"CREATING\", \"creationDateTime\": \"Dec 28, 2020, 7:35:21 AM\",  \"provisionedThroughput\": { \"numberOfDecreasesToday\": 0, \"readCapacityUnits\": 5, \"writeCapacityUnits\": 5},\"tableSizeBytes\": 0,\"itemCount\": 0,\"tableArn\": \"arn:aws:dynamodb:ap-south-1:083406524166:table/Employee\",\"tableId\": \"0bbd1d31-624b-4a0a-b804-4a7bc7351e3e\"}},\"requestID\": \"4NR3PQIDKFSKJGELV8EBNN1BNJVV4KQNSO5AEMVJF66Q9ASUAAJG\",\"eventID\": \"79151c71-058e-4031-8f2d-adbac87198a2\",\"readOnly\": false,\"resources\": [{\"accountId\": \"083406524166\",\"type\": \"AWS::DynamoDB::Table\",\"ARN\": \"arn:aws:dynamodb:ap-south-1:083406524166:table/Employee\"}],\"eventType\": \"AwsApiCall\",\"apiVersion\": \"2012-08-10\",\"managementEvent\": true,\"recipientAccountId\": \"083406524166\",\"eventCategory\": \"Management\"}";
        final JsonObject testJson = JsonParser.parseString(testString).getAsJsonObject();
        String dateString2 = Parser.parseTimestamp(testJson);
        long time2 = Parser.getTime(dateString2).getTimstamp();

        Assert.assertNotEquals(time, time2);
    }


}