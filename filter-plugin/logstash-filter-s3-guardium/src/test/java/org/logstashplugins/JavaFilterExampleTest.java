/*
 *
 * Copyright 2020-2021 IBM Inc. All rights reserved
 * SPDX-License-Identifier: Apache2.0
 *
 */
package org.logstashplugins;

//import co.elastic.logstash.api.Configuration;
import co.elastic.logstash.api.Configuration;
import co.elastic.logstash.api.Context;
import co.elastic.logstash.api.Event;
import co.elastic.logstash.api.FilterMatchListener;
import com.ibm.guardium.s3.Parser;
import com.ibm.guardium.universalconnector.commons.GuardConstants;
import com.ibm.guardium.universalconnector.commons.structures.Time;
import org.junit.Assert;
import org.junit.Test;
import org.logstash.plugins.ConfigurationImpl;
import org.logstash.plugins.ContextImpl;
//import org.logstash.plugins.ConfigurationImpl;

import java.text.ParseException;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;

public class JavaFilterExampleTest {

//    final static String mongodString = "<14>Feb 18 08:53:31 qa-db51 mongod: { \"atype\" : \"authCheck\", \"ts\" : { \"$date\" : \"2020-01-16T05:41:30.783-0500\" }, \"local\" : { \"ip\" : \"(NONE)\", \"port\" : 0 }, \"remote\" : { \"ip\" : \"(NONE)\", \"port\" : 0 }, \"users\" : [], \"roles\" : [], \"param\" : { \"command\" : \"find\", \"ns\" : \"config.transactions\", \"args\" : { \"find\" : \"transactions\", \"filter\" : { \"lastWriteDate\" : { \"$lt\" : { \"$date\" : \"2020-01-16T05:11:30.782-0500\" } } }, \"projection\" : { \"_id\" : 1 }, \"sort\" : { \"_id\" : 1 }, \"$db\" : \"config\" } }, \"result\" : 0 }";
//    final static Context context = new ContextImpl(null, null);
//    final static JavaS3ToGuardiumFilter filter = new JavaS3ToGuardiumFilter("test-id", null, context);
    @Test
    public void testDateParing() throws ParseException {
        String dateStr = "2020-07-14T04:03:57Z";
        Time time = Parser.getTime(dateStr);
        Assert.assertTrue("Failed to parse date, time is "+time, 1594699437000L==time.getTimstamp());
    }

    @Test
    public void testJavaExampleFilter() {

        String sourceField = "foo";
        Configuration config = new ConfigurationImpl(Collections.singletonMap("source", sourceField));
        Context context = new ContextImpl(null, null);
        LogstashFilterS3Guardium filter = new LogstashFilterS3Guardium("test-id", config, context);

        TestMatchListener matchListener = new TestMatchListener();
        String cwlog = "{\n" +
                "      \"@version\" => \"1\",\n" +
                "     \"@timestamp\" => 2020-07-14T04:19:00.629Z,\n" +
                "  \"cloudwatch_logs\" => {\n" +
                "       \"event_id\" => \"35563005964377320956022514424007384299921152149562654906\",\n" +
                "    \"ingestion_time\" => 2020-07-14T04:19:00.720Z,\n" +
                "       \"log_group\" => \"uc_doc_group\",\n" +
                "      \"log_stream\" => \"987076625343_CloudTrail_us-east-1\"\n" +
                "  },\n" +
                "      \"message\" => \"{\"eventVersion\":\"1.05\",\"userIdentity\":{\"type\":\"AssumedRole\",\"principalId\":\"AROAJYWCH33XZXHIQAPR2:TrustedAdvisor_987076625343_152bf7d3-3289-497e-a68b-60210b555cc4\",\"arn\":\"arn:aws:sts::987076625343:assumed-role/AWSServiceRoleForTrustedAdvisor/TrustedAdvisor_987076625343_152bf7d3-3289-497e-a68b-60210b555cc4\",\"accountId\":\"987076625343\",\"accessKeyId\":\"ASIA6LUS2AO7WSAQPC7G\",\"sessionContext\":{\"attributes\":{\"mfaAuthenticated\":\"false\",\"creationDate\":\"2020-07-14T04:03:56Z\"},\"sessionIssuer\":{\"type\":\"Role\",\"principalId\":\"AROAJYWCH33XZXHIQAPR2\",\"arn\":\"arn:aws:iam::987076625343:role/aws-service-role/trustedadvisor.amazonaws.com/AWSServiceRoleForTrustedAdvisor\",\"accountId\":\"987076625343\",\"userName\":\"AWSServiceRoleForTrustedAdvisor\"}},\"invokedBy\":\"AWS Internal\"},\"eventTime\":\"2020-07-14T04:03:57Z\",\"eventSource\":\"s3.amazonaws.com\",\"eventName\":\"GetBucketLocation\",\"awsRegion\":\"us-east-1\",\"sourceIPAddress\":\"10.246.238.2\",\"userAgent\":\"[AWS-Support-TrustedAdvisor, aws-internal/3 aws-sdk-java/1.11.728 Linux/4.9.184-0.1.ac.235.83.329.metal1.x86_64 OpenJDK_64-Bit_Server_VM/25.242-b08 java/1.8.0_242 vendor/Oracle_Corporation]\",\"requestParameters\":{\"host\":[\"s3.us-east-1.amazonaws.com\"],\"bucketName\":\"guardium01\",\"location\":[\"\"],\"aws-account\":[\"987076625343\"]},\"responseElements\":null,\"additionalEventData\":{\"SignatureVersion\":\"SigV4\",\"CipherSuite\":\"ECDHE-RSA-AES128-SHA\",\"AuthenticationMethod\":\"AuthHeader\",\"vpcEndpointId\":\"vpce-00dc1369\"},\"requestID\":\"271E9E0DC890C561\",\"eventID\":\"f7a85761-d85a-430f-ac55-a71e1b82a660\",\"eventType\":\"AwsApiCall\",\"recipientAccountId\":\"987076625343\",\"vpcEndpointId\":\"vpce-00dc1369\"}\"\n" +
                "}";

        Event e = new org.logstash.Event();
        e.setField("cloudwatch_logs", "{\n" +
                "       \"event_id\" => \"35563005964377320956022514424007384299921152149562654906\",\n" +
                "    \"ingestion_time\" => 2020-07-14T04:19:00.720Z,\n" +
                "       \"log_group\" => \"uc_doc_group\",\n" +
                "      \"log_stream\" => \"987076625343_CloudTrail_us-east-1\"\n" +
                "  }");
        e.setField("message", "{\"eventVersion\":\"1.05\",\"userIdentity\":{\"type\":\"AssumedRole\",\"principalId\":\"AROAJYWCH33XZXHIQAPR2:TrustedAdvisor_987076625343_152bf7d3-3289-497e-a68b-60210b555cc4\",\"arn\":\"arn:aws:sts::987076625343:assumed-role/AWSServiceRoleForTrustedAdvisor/TrustedAdvisor_987076625343_152bf7d3-3289-497e-a68b-60210b555cc4\",\"accountId\":\"987076625343\",\"accessKeyId\":\"ASIA6LUS2AO7WSAQPC7G\",\"sessionContext\":{\"attributes\":{\"mfaAuthenticated\":\"false\",\"creationDate\":\"2020-07-14T04:03:56Z\"},\"sessionIssuer\":{\"type\":\"Role\",\"principalId\":\"AROAJYWCH33XZXHIQAPR2\",\"arn\":\"arn:aws:iam::987076625343:role/aws-service-role/trustedadvisor.amazonaws.com/AWSServiceRoleForTrustedAdvisor\",\"accountId\":\"987076625343\",\"userName\":\"AWSServiceRoleForTrustedAdvisor\"}},\"invokedBy\":\"AWS Internal\"},\"eventTime\":\"2020-07-14T04:03:57Z\",\"eventSource\":\"s3.amazonaws.com\",\"eventName\":\"GetBucketLocation\",\"awsRegion\":\"us-east-1\",\"sourceIPAddress\":\"10.246.238.2\",\"userAgent\":\"[AWS-Support-TrustedAdvisor, aws-internal/3 aws-sdk-java/1.11.728 Linux/4.9.184-0.1.ac.235.83.329.metal1.x86_64 OpenJDK_64-Bit_Server_VM/25.242-b08 java/1.8.0_242 vendor/Oracle_Corporation]\",\"requestParameters\":{\"host\":[\"s3.us-east-1.amazonaws.com\"],\"bucketName\":\"guardium01\",\"location\":[\"\"],\"aws-account\":[\"987076625343\"]},\"responseElements\":null,\"additionalEventData\":{\"SignatureVersion\":\"SigV4\",\"CipherSuite\":\"ECDHE-RSA-AES128-SHA\",\"AuthenticationMethod\":\"AuthHeader\",\"vpcEndpointId\":\"vpce-00dc1369\"},\"requestID\":\"271E9E0DC890C561\",\"eventID\":\"f7a85761-d85a-430f-ac55-a71e1b82a660\",\"eventType\":\"AwsApiCall\",\"recipientAccountId\":\"987076625343\",\"vpcEndpointId\":\"vpce-00dc1369\"}");
        
        Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
        Assert.assertEquals(1, results.size());

        String recordStr = e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME).toString();
        Assert.assertTrue(recordStr!=null && recordStr.length()>0);

//      ip resolution s3-1.amazonaws.com may give different ips, no need to test only relevant properties and think about worthiness of ip resolution here ...
//        Record record = new Gson().fromJson(recordStr, Record.class);
//        Record recordExpected = new Gson().fromJson("{\"sessionId\":\"{\\\"type\\\":\\\"AssumedRole\\\",\\\"principalId\\\":\\\"AROAJYWCH33XZXHIQAPR2:TrustedAdvisor_987076625343_152bf7d3-3289-497e-a68b-60210b555cc4\\\",\\\"arn\\\":\\\"arn:aws:sts::987076625343:assumed-role/AWSServiceRoleForTrustedAdvisor/TrustedAdvisor_987076625343_152bf7d3-3289-497e-a68b-60210b555cc4\\\",\\\"accountId\\\":\\\"987076625343\\\",\\\"accessKeyId\\\":\\\"ASIA6LUS2AO7WSAQPC7G\\\",\\\"sessionContext\\\":{\\\"attributes\\\":{\\\"mfaAuthenticated\\\":\\\"false\\\",\\\"creationDate\\\":\\\"2020-07-14T04:03:56Z\\\"},\\\"sessionIssuer\\\":{\\\"type\\\":\\\"Role\\\",\\\"principalId\\\":\\\"AROAJYWCH33XZXHIQAPR2\\\",\\\"arn\\\":\\\"arn:aws:iam::987076625343:role/aws-service-role/trustedadvisor.amazonaws.com/AWSServiceRoleForTrustedAdvisor\\\",\\\"accountId\\\":\\\"987076625343\\\",\\\"userName\\\":\\\"AWSServiceRoleForTrustedAdvisor\\\"}},\\\"invokedBy\\\":\\\"AWS Internal\\\"}\",\"dbName\":\"guardium01\",\"appUserName\":\"AWSServiceRoleForTrustedAdvisor\",\"time\":1594688637000,\"gmtOffsetInMin\":0,\"dstOffsetInMin\":0,\"sessionLocator\":{\"clientIp\":\"10.246.238.2\",\"clientPort\":0,\"serverIp\":\"52.216.133.245\",\"serverPort\":0,\"isIpv6\":false,\"clientIpv6\":null,\"serverIpv6\":null},\"accessor\":{\"dbUser\":\"AWSServiceRoleForTrustedAdvisor\",\"serverType\":\"S3\",\"serverOs\":\"\",\"clientOs\":\"\",\"clientHostName\":\"10.246.238.2\",\"serverHostName\":\"s3.amazonaws.com\",\"commProtocol\":\"AwsApiCall\",\"dbProtocol\":\"S3\",\"dbProtocolVersion\":\"1.05\",\"osUser\":\"\",\"sourceProgram\":\"AWS-Support-TrustedAdvisor, aws-internal\",\"client_mac\":\"\",\"serverDescription\":\"us-east-1\",\"serviceName\":\"s3.amazonaws.com\",\"language\":\"\",\"type\":\"AwsApiCall\"},\"data\":{\"construct\":{\"sentences\":[{\"verb\":\"GetBucketLocation\",\"objects\":[{\"name\":\"\",\"type\":null,\"fields\":[],\"schema\":\"\"}],\"descendants\":[],\"fields\":[]}],\"fullSql\":\"{\\\"eventVersion\\\":\\\"1.05\\\",\\\"userIdentity\\\":{\\\"type\\\":\\\"AssumedRole\\\",\\\"principalId\\\":\\\"AROAJYWCH33XZXHIQAPR2:TrustedAdvisor_987076625343_152bf7d3-3289-497e-a68b-60210b555cc4\\\",\\\"arn\\\":\\\"arn:aws:sts::987076625343:assumed-role/AWSServiceRoleForTrustedAdvisor/TrustedAdvisor_987076625343_152bf7d3-3289-497e-a68b-60210b555cc4\\\",\\\"accountId\\\":\\\"987076625343\\\",\\\"accessKeyId\\\":\\\"ASIA6LUS2AO7WSAQPC7G\\\",\\\"sessionContext\\\":{\\\"attributes\\\":{\\\"mfaAuthenticated\\\":\\\"false\\\",\\\"creationDate\\\":\\\"2020-07-14T04:03:56Z\\\"},\\\"sessionIssuer\\\":{\\\"type\\\":\\\"Role\\\",\\\"principalId\\\":\\\"AROAJYWCH33XZXHIQAPR2\\\",\\\"arn\\\":\\\"arn:aws:iam::987076625343:role/aws-service-role/trustedadvisor.amazonaws.com/AWSServiceRoleForTrustedAdvisor\\\",\\\"accountId\\\":\\\"987076625343\\\",\\\"userName\\\":\\\"AWSServiceRoleForTrustedAdvisor\\\"}},\\\"invokedBy\\\":\\\"AWS Internal\\\"},\\\"eventTime\\\":\\\"2020-07-14T04:03:57Z\\\",\\\"eventSource\\\":\\\"s3.amazonaws.com\\\",\\\"eventName\\\":\\\"GetBucketLocation\\\",\\\"awsRegion\\\":\\\"us-east-1\\\",\\\"sourceIPAddress\\\":\\\"10.246.238.2\\\",\\\"userAgent\\\":\\\"[AWS-Support-TrustedAdvisor, aws-internal/3 aws-sdk-java/1.11.728 Linux/4.9.184-0.1.ac.235.83.329.metal1.x86_64 OpenJDK_64-Bit_Server_VM/25.242-b08 java/1.8.0_242 vendor/Oracle_Corporation]\\\",\\\"requestParameters\\\":{\\\"host\\\":[\\\"s3.us-east-1.amazonaws.com\\\"],\\\"bucketName\\\":\\\"guardium01\\\",\\\"location\\\":[\\\"\\\"],\\\"aws-account\\\":[\\\"987076625343\\\"]},\\\"additionalEventData\\\":{\\\"SignatureVersion\\\":\\\"SigV4\\\",\\\"CipherSuite\\\":\\\"ECDHE-RSA-AES128-SHA\\\",\\\"AuthenticationMethod\\\":\\\"AuthHeader\\\",\\\"vpcEndpointId\\\":\\\"vpce-00dc1369\\\"},\\\"requestID\\\":\\\"271E9E0DC890C561\\\",\\\"eventID\\\":\\\"f7a85761-d85a-430f-ac55-a71e1b82a660\\\",\\\"eventType\\\":\\\"AwsApiCall\\\",\\\"recipientAccountId\\\":\\\"987076625343\\\",\\\"vpcEndpointId\\\":\\\"vpce-00dc1369\\\"}\",\"redactedSensitiveDataSql\":\"{\\\"eventVersion\\\":\\\"1.05\\\",\\\"userIdentity\\\":{\\\"type\\\":\\\"AssumedRole\\\",\\\"principalId\\\":\\\"AROAJYWCH33XZXHIQAPR2:TrustedAdvisor_987076625343_152bf7d3-3289-497e-a68b-60210b555cc4\\\",\\\"arn\\\":\\\"arn:aws:sts::987076625343:assumed-role/AWSServiceRoleForTrustedAdvisor/TrustedAdvisor_987076625343_152bf7d3-3289-497e-a68b-60210b555cc4\\\",\\\"accountId\\\":\\\"987076625343\\\",\\\"accessKeyId\\\":\\\"ASIA6LUS2AO7WSAQPC7G\\\",\\\"sessionContext\\\":{\\\"attributes\\\":{\\\"mfaAuthenticated\\\":\\\"false\\\",\\\"creationDate\\\":\\\"2020-07-14T04:03:56Z\\\"},\\\"sessionIssuer\\\":{\\\"type\\\":\\\"Role\\\",\\\"principalId\\\":\\\"AROAJYWCH33XZXHIQAPR2\\\",\\\"arn\\\":\\\"arn:aws:iam::987076625343:role/aws-service-role/trustedadvisor.amazonaws.com/AWSServiceRoleForTrustedAdvisor\\\",\\\"accountId\\\":\\\"987076625343\\\",\\\"userName\\\":\\\"AWSServiceRoleForTrustedAdvisor\\\"}},\\\"invokedBy\\\":\\\"AWS Internal\\\"},\\\"eventTime\\\":\\\"2020-07-14T04:03:57Z\\\",\\\"eventSource\\\":\\\"s3.amazonaws.com\\\",\\\"eventName\\\":\\\"GetBucketLocation\\\",\\\"awsRegion\\\":\\\"us-east-1\\\",\\\"sourceIPAddress\\\":\\\"10.246.238.2\\\",\\\"userAgent\\\":\\\"[AWS-Support-TrustedAdvisor, aws-internal/3 aws-sdk-java/1.11.728 Linux/4.9.184-0.1.ac.235.83.329.metal1.x86_64 OpenJDK_64-Bit_Server_VM/25.242-b08 java/1.8.0_242 vendor/Oracle_Corporation]\\\",\\\"requestParameters\\\":{\\\"host\\\":[\\\"s3.us-east-1.amazonaws.com\\\"],\\\"bucketName\\\":\\\"guardium01\\\",\\\"location\\\":[\\\"\\\"],\\\"aws-account\\\":[\\\"987076625343\\\"]},\\\"additionalEventData\\\":{\\\"SignatureVersion\\\":\\\"SigV4\\\",\\\"CipherSuite\\\":\\\"ECDHE-RSA-AES128-SHA\\\",\\\"AuthenticationMethod\\\":\\\"AuthHeader\\\",\\\"vpcEndpointId\\\":\\\"vpce-00dc1369\\\"},\\\"requestID\\\":\\\"271E9E0DC890C561\\\",\\\"eventID\\\":\\\"f7a85761-d85a-430f-ac55-a71e1b82a660\\\",\\\"eventType\\\":\\\"AwsApiCall\\\",\\\"recipientAccountId\\\":\\\"987076625343\\\",\\\"vpcEndpointId\\\":\\\"vpce-00dc1369\\\"}\"},\"originalSqlCommand\":\"{\\\"eventVersion\\\":\\\"1.05\\\",\\\"userIdentity\\\":{\\\"type\\\":\\\"AssumedRole\\\",\\\"principalId\\\":\\\"AROAJYWCH33XZXHIQAPR2:TrustedAdvisor_987076625343_152bf7d3-3289-497e-a68b-60210b555cc4\\\",\\\"arn\\\":\\\"arn:aws:sts::987076625343:assumed-role/AWSServiceRoleForTrustedAdvisor/TrustedAdvisor_987076625343_152bf7d3-3289-497e-a68b-60210b555cc4\\\",\\\"accountId\\\":\\\"987076625343\\\",\\\"accessKeyId\\\":\\\"ASIA6LUS2AO7WSAQPC7G\\\",\\\"sessionContext\\\":{\\\"attributes\\\":{\\\"mfaAuthenticated\\\":\\\"false\\\",\\\"creationDate\\\":\\\"2020-07-14T04:03:56Z\\\"},\\\"sessionIssuer\\\":{\\\"type\\\":\\\"Role\\\",\\\"principalId\\\":\\\"AROAJYWCH33XZXHIQAPR2\\\",\\\"arn\\\":\\\"arn:aws:iam::987076625343:role/aws-service-role/trustedadvisor.amazonaws.com/AWSServiceRoleForTrustedAdvisor\\\",\\\"accountId\\\":\\\"987076625343\\\",\\\"userName\\\":\\\"AWSServiceRoleForTrustedAdvisor\\\"}},\\\"invokedBy\\\":\\\"AWS Internal\\\"},\\\"eventTime\\\":\\\"2020-07-14T04:03:57Z\\\",\\\"eventSource\\\":\\\"s3.amazonaws.com\\\",\\\"eventName\\\":\\\"GetBucketLocation\\\",\\\"awsRegion\\\":\\\"us-east-1\\\",\\\"sourceIPAddress\\\":\\\"10.246.238.2\\\",\\\"userAgent\\\":\\\"[AWS-Support-TrustedAdvisor, aws-internal/3 aws-sdk-java/1.11.728 Linux/4.9.184-0.1.ac.235.83.329.metal1.x86_64 OpenJDK_64-Bit_Server_VM/25.242-b08 java/1.8.0_242 vendor/Oracle_Corporation]\\\",\\\"requestParameters\\\":{\\\"host\\\":[\\\"s3.us-east-1.amazonaws.com\\\"],\\\"bucketName\\\":\\\"guardium01\\\",\\\"location\\\":[\\\"\\\"],\\\"aws-account\\\":[\\\"987076625343\\\"]},\\\"additionalEventData\\\":{\\\"SignatureVersion\\\":\\\"SigV4\\\",\\\"CipherSuite\\\":\\\"ECDHE-RSA-AES128-SHA\\\",\\\"AuthenticationMethod\\\":\\\"AuthHeader\\\",\\\"vpcEndpointId\\\":\\\"vpce-00dc1369\\\"},\\\"requestID\\\":\\\"271E9E0DC890C561\\\",\\\"eventID\\\":\\\"f7a85761-d85a-430f-ac55-a71e1b82a660\\\",\\\"eventType\\\":\\\"AwsApiCall\\\",\\\"recipientAccountId\\\":\\\"987076625343\\\",\\\"vpcEndpointId\\\":\\\"vpce-00dc1369\\\"}\",\"useConstruct\":true},\"exception\":null}", Record.class);
//        Assert.assertEquals(recordExpected,record);

    }
//
//    @Test
//    public void testParseMongoSyslog() {
//        final String mongodString = "<14>Feb 18 08:53:31 qa-db51 mongod: { \"atype\" : \"authCheck\", \"ts\" : { \"$date\" : \"2020-06-11T09:44:11.070-0400\" }, \"local\" : { \"ip\" : \"9.70.147.59\", \"port\" : 27017 }, \"remote\" : { \"ip\" : \"9.148.202.94\", \"port\" : 60185 }, \"users\" : [ { \"user\" : \"realAdmin\", \"db\" : \"admin\" } ], \"roles\" : [ { \"role\" : \"readWriteAnyDatabase\", \"db\" : \"admin\" }, { \"role\" : \"userAdminAnyDatabase\", \"db\" : \"admin\" } ], \"param\" : { \"command\" : \"find\", \"ns\" : \"admin.USERS\", \"args\" : { \"find\" : \"USERS\", \"filter\" : {}, \"lsid\" : { \"id\" : { \"$binary\" : \"mV20eHvvRha2ELTeqJxQJg==\", \"$type\" : \"04\" } }, \"$db\" : \"admin\", \"$readPreference\" : { \"mode\" : \"primaryPreferred\" } } }, \"result\" : 0 }";
//        Context context = new ContextImpl(null, null);
//        JavaS3ToGuardiumFilter filter = new JavaS3ToGuardiumFilter("test-id", null, context);
//
//        Event e = new org.logstash.Event();
//        TestMatchListener matchListener = new TestMatchListener();
//
//        e.setField("message", mongodString);
//        Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
//
//        Assert.assertEquals(1, results.size());
//        Assert.assertNotNull(e.getField("Record"));
//        Assert.assertEquals(1, matchListener.getMatchCount());
//    }
//
//
//    @Test
//    public void testParseOtherSyslog() {
//        String syslogString = "<7>Feb 18 08:55:14 qa-db51 kernel: IPv6 addrconf: prefix with wrong length 96";
//        Context context = new ContextImpl(null, null);
//        JavaS3ToGuardiumFilter filter = new JavaS3ToGuardiumFilter("test-id", null, context);
//
//        Event e = new org.logstash.Event();
//        TestMatchListener matchListener = new TestMatchListener();
//
//        e.setField("message", syslogString);
//        Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
//
//        Assert.assertEquals(1, results.size());
//        Assert.assertEquals(true,
//            e.getField("tags").toString().contains("_mongoguardium_skip"));
//        //Assert.assertNull(e.getField("Construct"));
//        Assert.assertEquals(0, matchListener.getMatchCount());
//    }
//
//    /**
//     * Tests that messages are skipped & removed if atype != "authCheck"
//     */
//    @Test
//    public void testParseMongo_skip_remove_atype_createCollection() {
//        String messageString = "<14>Feb 18 08:53:31 qa-db51 mongod: { \"atype\" : \"createCollection\", \"ts\" : { \"$date\" : \"2020-06-03T03:40:30.888-0400\" }, \"local\" : { \"ip\" : \"127.0.0.1\", \"port\" : 27017 }, \"remote\" : { \"ip\" : \"127.0.0.1\", \"port\" : 40426 }, \"users\" : [ { \"user\" : \"realAdmin\", \"db\" : \"admin\" } ], \"roles\" : [ { \"role\" : \"readWriteAnyDatabase\", \"db\" : \"admin\" }, { \"role\" : \"userAdminAnyDatabase\", \"db\" : \"admin\" } ], \"param\" : { \"ns\" : \"newDB01.newCollection01\" }, \"result\" : 0 }";
//        Context context = new ContextImpl(null, null);
//        JavaS3ToGuardiumFilter filter = new JavaS3ToGuardiumFilter("test-id", null, context);
//
//        Event e = new org.logstash.Event();
//        TestMatchListener matchListener = new TestMatchListener();
//        ArrayList<Event> events = new ArrayList<>();
//        e.setField("message", messageString);
//        events.add(e);
//
//        Collection<Event> results = filter.filter(events, matchListener);
//
//        Assert.assertEquals(0, results.size());
//        Assert.assertEquals(0, matchListener.getMatchCount());
//    }
//
//    /**
//     * Tests that messages without identified users are skipped & removed
//     */
//    @Test
//    public void testParseMongo_skip_remove_empty_users() {
//        String messageString = "<14>Feb 18 08:53:31 qa-db51 mongod: { \"atype\" : \"authCheck\", \"ts\" : { \"$date\" : \"2020-06-11T09:50:21.485-0400\" }, \"local\" : { \"ip\" : \"9.70.147.59\", \"port\" : 27017 }, \"remote\" : { \"ip\" : \"9.148.202.94\", \"port\" : 60241 }, \"users\" : [], \"roles\" : [], \"param\" : { \"command\" : \"isMaster\", \"ns\" : \"admin\", \"args\" : { \"ismaster\" : 1, \"client\" : { \"driver\" : { \"name\" : \"PyMongo\", \"version\" : \"3.10.1\" }, \"os\" : { \"type\" : \"Darwin\", \"name\" : \"Darwin\", \"architecture\" : \"x86_64\", \"version\" : \"10.14.6\" }, \"platform\" : \"CPython 2.7.15.final.0\" }, \"$db\" : \"admin\" } }, \"result\" : 0 }";
//        Context context = new ContextImpl(null, null);
//        JavaS3ToGuardiumFilter filter = new JavaS3ToGuardiumFilter("test-id", null, context);
//
//        Event e = new org.logstash.Event();
//        TestMatchListener matchListener = new TestMatchListener();
//        ArrayList<Event> events = new ArrayList<>();
//        e.setField("message", messageString);
//        events.add(e);
//
//        Collection<Event> results = filter.filter(events, matchListener);
//
//        Assert.assertEquals(0, results.size());
//        Assert.assertEquals(0, matchListener.getMatchCount());
//    }
//
//    /**
//     * Tests that atype="authenticate" messages are skipped & removed.
//     *
//     * Unsuccessful messages are handled as reported as Exception (Failed login in Guardium).
//     */
//    @Test
//    public void testParseMongo_skip_remove_atype_authenticate_successful() {
//        String messageString = "<14>Feb 18 08:53:31 qa-db51 mongod: { \"atype\" : \"authenticate\", \"ts\" : { \"$date\" : \"2020-06-09T08:34:12.424-0400\" }, \"local\" : { \"ip\" : \"9.70.147.59\", \"port\" : 27017 }, \"remote\" : { \"ip\" : \"9.148.206.148\", \"port\" : 49712 }, \"users\" : [ { \"user\" : \"realAdmin\", \"db\" : \"admin\" } ], \"roles\" : [ { \"role\" : \"readWriteAnyDatabase\", \"db\" : \"admin\" }, { \"role\" : \"userAdminAnyDatabase\", \"db\" : \"admin\" } ], \"param\" : { \"user\" : \"realAdmin\", \"db\" : \"admin\", \"mechanism\" : \"SCRAM-SHA-256\" }, \"result\" : 0 }";
//        Context context = new ContextImpl(null, null);
//        JavaS3ToGuardiumFilter filter = new JavaS3ToGuardiumFilter("test-id", null, context);
//
//        Event e = new org.logstash.Event();
//        TestMatchListener matchListener = new TestMatchListener();
//        ArrayList<Event> events = new ArrayList<>();
//        e.setField("message", messageString);
//        events.add(e);
//
//        Collection<Event> results = filter.filter(events, matchListener);
//
//        Assert.assertEquals(0, results.size());
//        Assert.assertEquals(0, matchListener.getMatchCount());
//    }
//
//    /**
//     * Test integrity of events collection, after skipped events were removed from it.
//     *
//     * Tests also that authentication failure is handled and not removed, even though empty users[].
//     */
//    @Test
//    public void testParseMongo_eventsCollectionIntegrity() {
//        String messageStringOK = "<14>Feb 18 08:53:31 qa-db51 mongod: { \"atype\" : \"authCheck\", \"ts\" : { \"$date\" : \"2020-06-11T09:44:11.070-0400\" }, \"local\" : { \"ip\" : \"9.70.147.59\", \"port\" : 27017 }, \"remote\" : { \"ip\" : \"9.148.202.94\", \"port\" : 60185 }, \"users\" : [ { \"user\" : \"realAdmin\", \"db\" : \"admin\" } ], \"roles\" : [ { \"role\" : \"readWriteAnyDatabase\", \"db\" : \"admin\" }, { \"role\" : \"userAdminAnyDatabase\", \"db\" : \"admin\" } ], \"param\" : { \"command\" : \"find\", \"ns\" : \"admin.USERS\", \"args\" : { \"find\" : \"USERS\", \"filter\" : {}, \"lsid\" : { \"id\" : { \"$binary\" : \"mV20eHvvRha2ELTeqJxQJg==\", \"$type\" : \"04\" } }, \"$db\" : \"admin\", \"$readPreference\" : { \"mode\" : \"primaryPreferred\" } } }, \"result\" : 0 }";
//        String messageStringSkip = "<14>Feb 18 08:53:32 qa-db51 mongod: { \"atype\" : \"createCollection\", \"ts\" : { \"$date\" : \"2020-06-03T03:40:30.888-0400\" }, \"local\" : { \"ip\" : \"127.0.0.1\", \"port\" : 27017 }, \"remote\" : { \"ip\" : \"127.0.0.1\", \"port\" : 40426 }, \"users\" : [ { \"user\" : \"realAdmin\", \"db\" : \"admin\" } ], \"roles\" : [ { \"role\" : \"readWriteAnyDatabase\", \"db\" : \"admin\" }, { \"role\" : \"userAdminAnyDatabase\", \"db\" : \"admin\" } ], \"param\" : { \"ns\" : \"newDB01.newCollection01\" }, \"result\" : 0 }";
//        String messageStringAuthOK = "<14>Feb 18 08:53:33 qa-db51 mongod: { \"atype\" : \"authenticate\", \"ts\" : { \"$date\" : \"2020-05-17T11:37:30.421-0400\" }, \"local\" : { \"ip\" : \"127.0.0.1\", \"port\" : 27017 }, \"remote\" : { \"ip\" : \"127.0.0.1\", \"port\" : 29398 }, \"users\" : [], \"roles\" : [], \"param\" : { \"user\" : \"readerUser\", \"db\" : \"admin\", \"mechanism\" : \"SCRAM-SHA-256\" }, \"result\" : 18 }";
//        Context context = new ContextImpl(null, null);
//        JavaS3ToGuardiumFilter filter = new JavaS3ToGuardiumFilter("test-id", null, context);
//
//        TestMatchListener matchListener = new TestMatchListener();
//        ArrayList<Event> inputEvents = new ArrayList<>();
//        Event e = new org.logstash.Event();
//        e.setField("message", messageStringOK);
//        Event eSkip = new org.logstash.Event();
//        eSkip.setField("message", messageStringSkip);
//        Event eAuth = new org.logstash.Event();
//        eAuth.setField("message", messageStringAuthOK);
//        inputEvents.add(e);
//        inputEvents.add(eSkip);
//        inputEvents.add(eAuth);
//
//        Collection<Event> results = filter.filter(inputEvents, matchListener);
//
//        Assert.assertEquals(2, results.size());
//        Assert.assertEquals(true, e.getField("tags") == null );
//        Assert.assertEquals(true, eAuth.getField("tags") == null);
//        Assert.assertEquals(2, matchListener.getMatchCount());
//    }
//
//    @Test
//    public void testParseMongoSyslog_doNotInjectHost() {
//        // syslog message uses different IPs for local/remote:
//        final String mongodString = "<14>Feb 18 08:53:31 qa-db51 mongod: { \"atype\" : \"authCheck\", \"ts\" : { \"$date\" : \"2020-01-16T05:41:30.783-0500\" }, \"local\" : { \"ip\" : \"1.2.3.456\", \"port\" : 0 }, \"remote\" : { \"ip\" : \"1.2.3.123\", \"port\" : 0 }, \"users\" : [ { \"user\" : \"realAdmin\", \"db\" : \"admin\" } ], \"roles\" : [ { \"role\" : \"readWriteAnyDatabase\", \"db\" : \"admin\" }, { \"role\" : \"userAdminAnyDatabase\", \"db\" : \"admin\" } ], \"roles\" : [], \"param\" : { \"command\" : \"find\", \"ns\" : \"config.transactions\", \"args\" : { \"find\" : \"transactions\", \"filter\" : { \"lastWriteDate\" : { \"$lt\" : { \"$date\" : \"2020-01-16T05:11:30.782-0500\" } } }, \"projection\" : { \"_id\" : 1 }, \"sort\" : { \"_id\" : 1 }, \"$db\" : \"config\" } }, \"result\" : 0 }";
//        final String hostString = "9.9.9.9";
//
//        Event e = new org.logstash.Event();
//        TestMatchListener matchListener = new TestMatchListener();
//
//        e.setField("message", mongodString);
//        e.setField("host", hostString);
//
//        Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
//
//        Assert.assertEquals(1, results.size());
//        String recordString = e.getField("Record").toString();
//        Record record = (new Gson()).fromJson(recordString, Record.class);
//        Assert.assertNotNull(record);
//
//        Assert.assertEquals(
//                "host should not override client IP when native audit shows different IPs for local & remote",
//                "1.2.3.123", record.getSessionLocator().getClientIp());
//        Assert.assertEquals(
//                "host should not override server IP when native audit shows different IPs for local & remote",
//                "1.2.3.456", record.getSessionLocator().getServerIp());
//    }

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