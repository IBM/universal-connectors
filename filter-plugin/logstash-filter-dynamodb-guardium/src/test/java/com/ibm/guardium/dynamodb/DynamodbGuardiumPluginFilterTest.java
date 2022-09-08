//
// Copyright 2020-2021 IBM Inc. All rights reserved
// SPDX-License-Identifier: Apache2.0
//
package com.ibm.guardium.dynamodb;

//import co.elastic.logstash.api.Configuration;
import co.elastic.logstash.api.Context;
import co.elastic.logstash.api.Event;
import co.elastic.logstash.api.FilterMatchListener;
import com.ibm.guardium.univer***REMOVED***lconnector.commons.GuardConstants;
import com.ibm.guardium.univer***REMOVED***lconnector.commons.structures.Record;
import org.junit.Assert;
import org.junit.Test;
//import org.logstash.plugins.ConfigurationImpl;
import org.logstash.plugins.ContextImpl;
import com.ibm.guardium.dynamodb.DynamodbGuardiumPluginFilter;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.gson.Gson;

//import static com.ibm.guardium.mongodb.MongodbGuardiumFilter.LOGSTASH_TAG_SKIP_NOT_MONGODB;

public class DynamodbGuardiumPluginFilterTest {

    final static String mongodString = "<14>Feb 18 08:53:31 qa-db51 mongod: { \"atype\" : \"authCheck\", \"ts\" : { \"$date\" : \"2020-01-16T05:41:30.783-0500\" }, \"local\" : { \"ip\" : \"(NONE)\", \"port\" : 0 }, \"remote\" : { \"ip\" : \"(NONE)\", \"port\" : 0 }, \"users\" : [], \"roles\" : [], \"param\" : { \"command\" : \"find\", \"ns\" : \"config.tran***REMOVED***ctions\", \"args\" : { \"find\" : \"tran***REMOVED***ctions\", \"filter\" : { \"lastWriteDate\" : { \"$lt\" : { \"$date\" : \"2020-01-16T05:11:30.782-0500\" } } }, \"projection\" : { \"_id\" : 1 }, \"sort\" : { \"_id\" : 1 }, \"$db\" : \"config\" } }, \"result\" : 0 }";
    final static Context context = new ContextImpl(null, null);
    final static DynamodbGuardiumPluginFilter filter = new DynamodbGuardiumPluginFilter("test-id", null, context);

    /**
     * To feed Guardium univer***REMOVED***l connector, a "GuardRecord" fields must exist. 
     * 
     * Filter should add field "GuardRecord" to the Event, which Univer***REMOVED***l connector then inserts into Guardium.   
     */
    
    //event string
    @Test
    public void testFieldGuardRecord_dynamodb() {
        final String mongodString2 = "{\"eventVersion\": \"1.08\", \"userIdentity\": {\"type\": \"Root\", \"principalId\": \"083406524166\", \"arn\": \"arn:aws:iam::083406524166:root\", \"accountId\": \"083406524166\", \"accessKeyId\": \"ASIARG23KR4DPKFEC4ZJ\", \"sessionContext\": { \"attributes\": {  \"creationDate\": \"2020-12-28T07:15:10Z\", \"mfaAuthenticated\": \"false\" } } },\"eventTime\": \"2020-12-28T07:35:21Z\",\"eventSource\": \"dynamodb.amazonaws.com\",   \"eventName\": \"CreateTable\",    \"awsRegion\": \"ap-south-1\", \"sourceIPAddress\": \"103.62.17.201\", \"userAgent\": \"console.mazonaws.com\", \"requestParameters\": {\"attributeDefinitions\": [{ \"attributeName\": \"Employee Number\", \"attributeType\": \"N\" }], \"tableName\": \"Employee\", \"keySchema\": [ {\"attributeName\": \"Employee Number\", \"keyType\": \"HASH\" } ],\"billingMode\": \"PROVISIONED\", \"provisionedThroughput\": { \"readCapacityUnits\": 5,  \"writeCapacityUnits\": 5 }, \"sSESpecification\": { \"enabled\": false } }, \"responseElements\": { \"tableDescription\": {  \"attributeDefinitions\": [ {  \"attributeName\": \"Employee Number\",\"attributeType\": \"N\"} ],   \"tableName\": \"Employee\",  \"keySchema\": [{ \"attributeName\": \"Employee Number\", \"keyType\": \"HASH\"  } ], \"tableStatus\": \"CREATING\", \"creationDateTime\": \"Dec 28, 2020, 7:35:21 AM\",  \"provisionedThroughput\": { \"numberOfDecreasesToday\": 0, \"readCapacityUnits\": 5, \"writeCapacityUnits\": 5},\"tableSizeBytes\": 0,\"itemCount\": 0,\"tableArn\": \"arn:aws:dynamodb:ap-south-1:083406524166:table/Employee\",\"tableId\": \"0bbd1d31-624b-4a0a-b804-4a7bc7351e3e\"}},\"requestID\": \"4NR3PQIDKFSKJGELV8EBNN1BNJVV4KQNSO5AEMVJF66Q9ASUAAJG\",\"eventID\": \"79151c71-058e-4031-8f2d-adbac87198a2\",\"readOnly\": false,\"resources\": [{\"accountId\": \"083406524166\",\"type\": \"AWS::DynamoDB::Table\",\"ARN\": \"arn:aws:dynamodb:ap-south-1:083406524166:table/Employee\"}],\"eventType\": \"AwsApiCall\",\"apiVersion\": \"2012-08-10\",\"managementEvent\": true,\"recipientAccountId\": \"083406524166\",\"eventCategory\": \"Management\"}";

        // Configuration config = new ConfigurationImpl(Collections.singletonMap("source", sourceField));
        Context context = new ContextImpl(null, null);
        DynamodbGuardiumPluginFilter filter = new DynamodbGuardiumPluginFilter("test-id", null, context);

        Event e = new org.logstash.Event();
        TestMatchListener matchListener = new TestMatchListener();
        
        e.setField("mes***REMOVED***ge", mongodString2);
        e.setField("account_id", "123456789");
        e.setField("host", "test.com");
        Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);

        Assert.assertEquals(1, results.size());
        Assert.assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
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