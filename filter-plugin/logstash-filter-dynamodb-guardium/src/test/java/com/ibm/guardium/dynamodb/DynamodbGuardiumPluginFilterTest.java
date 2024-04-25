//
// Copyright 2021-2024 IBM Inc. All rights reserved
// SPDX-License-Identifier: Apache2.0
//
package com.ibm.guardium.dynamodb;

import co.elastic.logstash.api.Context;
import co.elastic.logstash.api.Event;
import co.elastic.logstash.api.FilterMatchListener;
import com.ibm.guardium.universalconnector.commons.GuardConstants;
import org.junit.Assert;
import org.junit.Test;
import org.logstash.plugins.ContextImpl;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;


public class DynamodbGuardiumPluginFilterTest {
    final static Context context = new ContextImpl(null, null);
    final static DynamodbGuardiumPluginFilter filter = new DynamodbGuardiumPluginFilter("test-id", null, context);

    /**
     * To feed Guardium universal connector, a "GuardRecord" fields must exist. 
     * 
     * Filter should add field "GuardRecord" to the Event, which Universal connector then inserts into Guardium.   
     */
    
    //event string
    @Test
    public void testFieldGuardRecord_dynamodb() {
        final String mongodString2 = "{\"eventVersion\": \"1.08\", \"userIdentity\": {\"type\": \"Root\", \"principalId\": \"<dummy_principal_id>\", \"arn\": \"arn:aws:iam::083406524166:root\", \"accountId\": \"083406524166\", \"accessKeyId\": \"<dummy_access_key_id>\", \"sessionContext\": { \"attributes\": {  \"creationDate\": \"2020-12-28T07:15:10Z\", \"mfaAuthenticated\": \"false\" } } },\"eventTime\": \"2020-12-28T07:35:21Z\",\"eventSource\": \"dynamodb.amazonaws.com\",   \"eventName\": \"CreateTable\",    \"awsRegion\": \"ap-south-1\", \"sourceIPAddress\": \"103.62.17.201\", \"userAgent\": \"console.mazonaws.com\", \"requestParameters\": {\"attributeDefinitions\": [{ \"attributeName\": \"Employee Number\", \"attributeType\": \"N\" }], \"tableName\": \"Employee\", \"keySchema\": [ {\"attributeName\": \"Employee Number\", \"keyType\": \"HASH\" } ],\"billingMode\": \"PROVISIONED\", \"provisionedThroughput\": { \"readCapacityUnits\": 5,  \"writeCapacityUnits\": 5 }, \"sSESpecification\": { \"enabled\": false } }, \"responseElements\": { \"tableDescription\": {  \"attributeDefinitions\": [ {  \"attributeName\": \"Employee Number\",\"attributeType\": \"N\"} ],   \"tableName\": \"Employee\",  \"keySchema\": [{ \"attributeName\": \"Employee Number\", \"keyType\": \"HASH\"  } ], \"tableStatus\": \"CREATING\", \"creationDateTime\": \"Dec 28, 2020, 7:35:21 AM\",  \"provisionedThroughput\": { \"numberOfDecreasesToday\": 0, \"readCapacityUnits\": 5, \"writeCapacityUnits\": 5},\"tableSizeBytes\": 0,\"itemCount\": 0,\"tableArn\": \"arn:aws:dynamodb:ap-south-1:083406524166:table/Employee\",\"tableId\": \"0bbd1d31-624b-4a0a-b804-4a7bc7351e3e\"}},\"requestID\": \"4NR3PQIDKFSKJGELV8EBNN1BNJVV4KQNSO5AEMVJF66Q9ASUAAJG\",\"eventID\": \"79151c71-058e-4031-8f2d-adbac87198a2\",\"readOnly\": false,\"resources\": [{\"accountId\": \"083406524166\",\"type\": \"AWS::DynamoDB::Table\",\"ARN\": \"arn:aws:dynamodb:ap-south-1:083406524166:table/Employee\"}],\"eventType\": \"AwsApiCall\",\"apiVersion\": \"2012-08-10\",\"managementEvent\": true,\"recipientAccountId\": \"083406524166\",\"eventCategory\": \"Management\"}";

        // Configuration config = new ConfigurationImpl(Collections.singletonMap("source", sourceField));
        Context context = new ContextImpl(null, null);
        DynamodbGuardiumPluginFilter filter = new DynamodbGuardiumPluginFilter("test-id", null, context);

        Event e = new org.logstash.Event();
        TestMatchListener matchListener = new TestMatchListener();
        
        e.setField("message", mongodString2);
        e.setField("account_id", "ABCD");
        Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);

        Assert.assertEquals(1, results.size());
        Assert.assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
        Assert.assertEquals(1, matchListener.getMatchCount());
    }

    @Test
    public void testFieldGuardRecord_dynamodb1() {
        final String mongodString2 = "[{\"awsRegion\":\"us-east-1\",\"requestParameters\":{\"select\":\"ALL_ATTRIBUTES\",\"consistentRead\":false,\"tableName\":\"Pratiksha\",\"limit\":50,\"returnConsumedCapacity\":\"TOTAL\"},\"eventID\":\"02763b6b-71c0-4a12-a525-2ec46a944eb1\",\"readOnly\":true,\"recipientAccountId\":\"346824953529\",\"eventCategory\":\"Data\",\"eventName\":\"Scan\",\"responseElements\":null,\"eventVersion\":\"1.08\",\"sourceIPAddress\":\"129.41.59.3\",\"resources\":[{\"type\":\"AWS::DynamoDB::Table\",\"accountId\":\"346824953529\",\"ARN\":\"arn:aws:dynamodb:us-east-1:346824953529:table/Pratiksha\"}],\"apiVersion\":\"2012-08-10\",\"tlsDetails\":{\"clientProvidedHostHeader\":\"dynamodb.us-east-1.amazonaws.com\",\"tlsVersion\":\"TLSv1.3\",\"cipherSuite\":\"TLS_AES_128_GCM_SHA256\"},\"eventSource\":\"dynamodb.amazonaws.com\",\"requestID\":\"1HGD3CI5SO9JPGFIOHPHO16167VV4KQNSO5AEMVJF66Q9ASUAAJG\",\"userAgent\":\"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36\",\"managementEvent\":false,\"eventTime\":\"2024-03-12T06:22:01Z\",\"eventType\":\"AwsApiCall\",\"userIdentity\":{\"type\":\"AssumedRole\",\"arn\":\"arn:aws:sts::346824953529:assumed-role/aws_gi_dev/Pratiksha.Sonawane@ibm.com\",\"accessKeyId\":\"ASIAVBQDAZ24VBGUDK5Z\",\"sessionContext\":{\"attributes\":{\"mfaAuthenticated\":\"false\",\"creationDate\":\"2024-03-12T06:21:33Z\"},\"sessionIssuer\":{\"userName\":\"aws_gi_dev\",\"type\":\"Role\",\"arn\":\"arn:aws:iam::346824953529:role/aws_gi_dev\",\"accountId\":\"346824953529\",\"principalId\":\"AROAVBQDAZ24Z7RFEJOTC\"}},\"accountId\":\"346824953529\",\"principalId\":\"AROAVBQDAZ24Z7RFEJOTC:Pratiksha.Sonawane@ibm.com\"},\"sessionCredentialFromConsole\":\"true\"},{\"awsRegion\":\"us-east-1\",\"requestParameters\":{\"key\":{\"name\":\"Dehradun\",\"id\":\"7\"},\"tableName\":\"Pratiksha\"},\"eventID\":\"61253d6e-76fd-4bf1-8d77-878e601480d8\",\"readOnly\":false,\"recipientAccountId\":\"346824953529\",\"eventCategory\":\"Data\",\"eventName\":\"DeleteItem\",\"responseElements\":null,\"eventVersion\":\"1.08\",\"sourceIPAddress\":\"129.41.59.3\",\"resources\":[{\"type\":\"AWS::DynamoDB::Table\",\"accountId\":\"346824953529\",\"ARN\":\"arn:aws:dynamodb:us-east-1:346824953529:table/Pratiksha\"}],\"apiVersion\":\"2012-08-10\",\"tlsDetails\":{\"clientProvidedHostHeader\":\"dynamodb.us-east-1.amazonaws.com\",\"tlsVersion\":\"TLSv1.3\",\"cipherSuite\":\"TLS_AES_128_GCM_SHA256\"},\"eventSource\":\"dynamodb.amazonaws.com\",\"requestID\":\"8Q6U0HL3AKG0GVL39I7JRHFBBVVV4KQNSO5AEMVJF66Q9ASUAAJG\",\"userAgent\":\"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36\",\"managementEvent\":false,\"eventTime\":\"2024-03-12T06:22:12Z\",\"eventType\":\"AwsApiCall\",\"userIdentity\":{\"type\":\"AssumedRole\",\"arn\":\"arn:aws:sts::346824953529:assumed-role/aws_gi_dev/Pratiksha.Sonawane@ibm.com\",\"accessKeyId\":\"ASIAVBQDAZ242R4WQQML\",\"sessionContext\":{\"attributes\":{\"mfaAuthenticated\":\"false\",\"creationDate\":\"2024-03-12T06:21:33Z\"},\"sessionIssuer\":{\"userName\":\"aws_gi_dev\",\"type\":\"Role\",\"arn\":\"arn:aws:iam::346824953529:role/aws_gi_dev\",\"accountId\":\"346824953529\",\"principalId\":\"AROAVBQDAZ24Z7RFEJOTC\"}},\"accountId\":\"346824953529\",\"principalId\":\"AROAVBQDAZ24Z7RFEJOTC:Pratiksha.Sonawane@ibm.com\"},\"sessionCredentialFromConsole\":\"true\"}]";

        // Configuration config = new ConfigurationImpl(Collections.singletonMap("source", sourceField));
        Context context = new ContextImpl(null, null);
        DynamodbGuardiumPluginFilter filter = new DynamodbGuardiumPluginFilter("test-id", null, context);

        Event e = new org.logstash.Event();
        TestMatchListener matchListener = new TestMatchListener();

        e.setField("message", mongodString2);
        e.setField("account_id", "ABCD");
        Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);

        Assert.assertEquals(2, results.size());
        Assert.assertEquals(1, matchListener.getMatchCount());
    }
}

class TestMatchListener implements FilterMatchListener {

    private AtomicInteger matchCount = new AtomicInteger(0);

    @Override
    public void filterMatched(Event event) {
        matchCount.incrementAndGet();
    }

    public int getMatchCount() {
        return matchCount.get();
    }
}