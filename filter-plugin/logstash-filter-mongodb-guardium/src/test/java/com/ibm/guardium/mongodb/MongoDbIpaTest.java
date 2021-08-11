//
// Copyright 2020- IBM Inc. All rights reserved
// SPDX-License-Identifier: Apache2.0
//
package com.ibm.guardium.mongodb;

import co.elastic.logstash.api.Context;
import co.elastic.logstash.api.Event;
import com.google.gson.Gson;
import com.ibm.guardium.universalconnector.commons.GuardConstants;
import com.ibm.guardium.universalconnector.commons.structures.Record;
import org.junit.Assert;
import org.junit.Test;
import org.logstash.plugins.ContextImpl;

import java.util.Collection;
import java.util.Collections;

public class MongoDbIpaTest {

    final static Context context = new ContextImpl(null, null);
    final static MongodbGuardiumFilter filter = new MongodbGuardiumFilter("test-id", null, context);
    final static String messageStrForIPTests = "\"mongod: {\n" +
            "    \"atype\": \"authCheck\",\n" +
            "    \"ts\": {\n" +
            "      \"$date\": \"2020-10-13T05:34:58.768-0400\"\n" +
            "    },\n" +
            "    \"local\": {\n" +
            "      \"ip\": %s,\n" +
            "      \"port\": 27017\n" +
            "    },\n" +
            "    \"remote\": {\n" +
            "      \"ip\": %s,\n" +
            "      \"port\": 59056\n" +
            "    },\n" +
            "    \"users\": [\n" +
            "      {\n" +
            "        \"user\": \"admin\",\n" +
            "        \"db\": \"admin\"\n" +
            "      }\n" +
            "    ],\n" +
            "    \"roles\": [\n" +
            "      {\n" +
            "        \"role\": \"read\",\n" +
            "        \"db\": \"admin\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"role\": \"readWrite\",\n" +
            "        \"db\": \"admin\"\n" +
            "      }\n" +
            "    ],\n" +
            "    \"param\": {\n" +
            "      \"command\": \"create\",\n" +
            "      \"ns\": \"admin.shefali123\",\n" +
            "      \"args\": {\n" +
            "        \"create\": \"shefali123\",\n" +
            "        \"lsid\": {\n" +
            "          \"id\": {\n" +
            "            \"$binary\": \"gnlGNvRNRLaLBFH0cC6Y2Q==\",\n" +
            "            \"$type\": \"04\"\n" +
            "          }\n" +
            "        },\n" +
            "        \"$db\": \"admin\"\n" +
            "      }\n" +
            "    },\n" +
            "    \"result\": 0\n" +
            "  }";


    //---------------------test_local_ip_replacement_server_has_IPV6_address
    @Test
    public void testFieldGuardRecord_ip_local_filebeatIpV6() {
        String clientIp = "127.0.0.1";
        String serverIp = "127.0.0.1";
        String serverIpFromFilebeat = "2620:1f7:853:a000:a700::5";

        Event e = new org.logstash.Event();
        TestMatchListener matchListener = new TestMatchListener();

        String messageUnderTest = String.format(messageStrForIPTests, serverIp, clientIp);
        e.setField("message", messageUnderTest);
        e.setField("server_ip", serverIpFromFilebeat);
        Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);

        Assert.assertEquals(1, results.size());
        Assert.assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
        String recordStr = e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME).toString();
        Record record = new Gson().fromJson(recordStr, Record.class);

        Assert.assertTrue(record.getSessionLocator().getClientIpv6().equals(serverIpFromFilebeat));
        Assert.assertTrue(record.getSessionLocator().getServerIpv6().equals(serverIpFromFilebeat));
        Assert.assertTrue(record.getSessionLocator().isIpv6());
        Assert.assertEquals(1, matchListener.getMatchCount());

    }

    //---------------------test_local_ip_replacement_server_has_IPV4_address
    @Test
    public void testFieldGuardRecord_ip_local_filebeatIpV4() {
        String clientIp = "127.0.0.1";
        String serverIp = "127.0.0.1";
        String serverIpFromFilebeat = "1.2.3.4";

        Event e = new org.logstash.Event();
        TestMatchListener matchListener = new TestMatchListener();

        String messageUnderTest = String.format(messageStrForIPTests, serverIp, clientIp);
        e.setField("message", messageUnderTest);
        e.setField("server_ip", serverIpFromFilebeat);
        Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);

        Assert.assertEquals(1, results.size());
        Assert.assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
        String recordStr = e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME).toString();
        Record record = new Gson().fromJson(recordStr, Record.class);

        Assert.assertTrue(record.getSessionLocator().getClientIp().equals(serverIpFromFilebeat));
        Assert.assertTrue(record.getSessionLocator().getServerIp().equals(serverIpFromFilebeat));
        Assert.assertFalse(record.getSessionLocator().isIpv6());
        Assert.assertEquals(1, matchListener.getMatchCount());

    }

    //---------------------test_local_ip_NONE_replacement_server_has_IPV4_address
    @Test
    public void testFieldGuardRecord_ip_NONE_filebeatIpV4() {
        String clientIp = "(NONE)";
        String serverIp = "127.0.0.1";
        String serverIpFromFilebeat = "1.2.3.4";

        Event e = new org.logstash.Event();
        TestMatchListener matchListener = new TestMatchListener();

        String messageUnderTest = String.format(messageStrForIPTests, serverIp, clientIp);
        e.setField("message", messageUnderTest);
        e.setField("server_ip", serverIpFromFilebeat);
        Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);

        Assert.assertEquals(1, results.size());
        Assert.assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
        String recordStr = e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME).toString();
        Record record = new Gson().fromJson(recordStr, Record.class);

        Assert.assertTrue(record.getSessionLocator().getClientIp().equals(serverIpFromFilebeat));
        Assert.assertTrue(record.getSessionLocator().getServerIp().equals(serverIpFromFilebeat));
        Assert.assertFalse(record.getSessionLocator().isIpv6());
        Assert.assertEquals(1, matchListener.getMatchCount());
    }

    //---------------------test_local_ip_NONE_replacement_server_has_IPV4_address
    @Test
    public void testFieldGuardRecord_ip_NONE_filebeatIpV6() {
        String clientIp = "(NONE)";
        String serverIp = "(NONE)";
        String serverIpFromFilebeat = "1.2.3.4";

        Event e = new org.logstash.Event();
        TestMatchListener matchListener = new TestMatchListener();

        String messageUnderTest = String.format(messageStrForIPTests, serverIp, clientIp);
        e.setField("message", messageUnderTest);
        e.setField("server_ip", serverIpFromFilebeat);
        Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);

        Assert.assertEquals(1, results.size());
        Assert.assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
        String recordStr = e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME).toString();
        Record record = new Gson().fromJson(recordStr, Record.class);

        Assert.assertTrue(record.getSessionLocator().getClientIp().equals(serverIpFromFilebeat));
        Assert.assertTrue(record.getSessionLocator().getServerIp().equals(serverIpFromFilebeat));
        Assert.assertFalse(record.getSessionLocator().isIpv6());
        Assert.assertEquals(1, matchListener.getMatchCount());

    }

    //---------------------test_local_ip_NONE_replacement_server_has_IPV4_address
    @Test
    public void testFieldGuardRecord_ip_NONE_filebeatInvalid() {
        String clientIp = "(NONE)";
        String serverIp = "(NONE)";
        String serverIpFromFilebeat = "1";

        Event e = new org.logstash.Event();
        TestMatchListener matchListener = new TestMatchListener();

        String messageUnderTest = String.format(messageStrForIPTests, serverIp, clientIp);
        e.setField("message", messageUnderTest);
        e.setField("server_ip", serverIpFromFilebeat);
        Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);

        Assert.assertEquals(1, results.size());
        Assert.assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
        String recordStr = e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME).toString();
        Record record = new Gson().fromJson(recordStr, Record.class);

        Assert.assertTrue(record.getSessionLocator().getClientIp().equals("0.0.0.0"));
        Assert.assertTrue(record.getSessionLocator().getServerIp().equals("0.0.0.0"));
        Assert.assertFalse(record.getSessionLocator().isIpv6());
        Assert.assertEquals(1, matchListener.getMatchCount());

    }

    //---------------------test_local_ip_NONE_replacement_server_has_IPV4_address
    @Test
    public void testFieldGuardRecord_ip_local_filebeatInvalid() {
        String clientIp = "127.0.0.1";
        String serverIp = "127.0.0.1";
        String serverIpFromFilebeat = "1";

        Event e = new org.logstash.Event();
        TestMatchListener matchListener = new TestMatchListener();

        String messageUnderTest = String.format(messageStrForIPTests, serverIp, clientIp);
        e.setField("message", messageUnderTest);
        e.setField("server_ip", serverIpFromFilebeat);
        Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);

        Assert.assertEquals(1, results.size());
        Assert.assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
        String recordStr = e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME).toString();
        Record record = new Gson().fromJson(recordStr, Record.class);

        Assert.assertTrue(record.getSessionLocator().getClientIp().equals("127.0.0.1"));
        Assert.assertTrue(record.getSessionLocator().getServerIp().equals("127.0.0.1"));
        Assert.assertFalse(record.getSessionLocator().isIpv6());
        Assert.assertEquals(1, matchListener.getMatchCount());
    }

    @Test
    //---------------------test_local_ip_replacement_event_does_not_have_sessionIp
    public void testFieldGuardRecord_ip_local_no_filebeat_ip() {
        //{ "@timestamp" : "2020-10-13T09:35:06.173Z","message" : "mongod: { "atype" : "authCheck", "ts" : { "$date" : "2020-10-13T05:34:58.768-0400" }, "local" : { "ip" : "127.0.0.1", "port" : 27017 }, "remote" : { "ip" : "127.0.0.1", "port" : 59056 }, "users" : [ { "user" : "admin", "db" : "admin" } ], "roles" : [ { "role" : "read", "db" : "admin" }, { "role" : "readWrite", "db" : "admin" } ], "param" : { "command" : "create", "ns" : "admin.shefali123", "args" : { "create" : "shefali123", "lsid" : { "id" : { "$binary" : "gnlGNvRNRLaLBFH0cC6Y2Q==", "$type" : "04" } }, "$db" : "admin" } }, "result" : 0 }","host" : "{mac=ConvertedList{delegate=[00:50:56:b4:77:7f]}, id=f6da45a8-225a-46c9-958a-ea7bca345534, ip=ConvertedList{delegate=[2620:1f7:853:a000:a700::5, fe80::953b:3ae2:ab60:d65, 9.42.101.19]}, os={kernel=10.0.17763.1457 (WinBuild.160101.0800), family=windows, version=10.0, name=Windows Server 2019 Standard, build=17763.1457, platform=windows}, name=sys-win2019db03, hostname=sys-win2019db03, architecture=x86_64}","agent" : "{id=90467cc3-4af0-4311-a999-ce9fb9ef80a4, type=filebeat, name=sys-win2019db03, version=7.8.1, hostname=sys-win2019db03, ephemeral_id=84bca17c-1919-4793-a6e9-0ecb748967c6}","server_ip" : "2620:1f7:853:a000:a700::5","tags" : "ConvertedList{delegate=[beats_input_codec_plain_applied]}","server_hostname" : "sys-win2019db03","input" : "{type=log}","log" : "{file={path=C:\Deploy\auditLog.json}, offset=1647316}","ecs" : "{version=1.5.0}","type" : "filebeat-mongodb-preset","source_program" : "mongod","@version" : "1","client_hostname" : "sys-win2019db03" }

        // Configuration config = new ConfigurationImpl(Collections.singletonMap("source", sourceField));
        Context context = new ContextImpl(null, null);
        MongodbGuardiumFilter filter = new MongodbGuardiumFilter("test-id", null, context);

        String clientIp = "127.0.0.1";
        String serverIp = "127.0.0.1";

        Event e = new org.logstash.Event();
        TestMatchListener matchListener = new TestMatchListener();

        String messageUnderTest = String.format(messageStrForIPTests, serverIp, clientIp);
        e.setField("message", messageUnderTest);
        Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);

        Assert.assertEquals(1, results.size());
        Assert.assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
        String recordStr = e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME).toString();
        Record record = new Gson().fromJson(recordStr, Record.class);

        Assert.assertTrue(record.getSessionLocator().getClientIp().equals("127.0.0.1"));
        Assert.assertTrue(record.getSessionLocator().getServerIp().equals("127.0.0.1"));
        Assert.assertFalse(record.getSessionLocator().isIpv6());
        Assert.assertEquals(1, matchListener.getMatchCount());

    }

    @Test
    //---------------------test_local_ip_replacement_event_does_not_have_sessionIp
    public void testFieldGuardRecord_ip_NONE_no_filebeat_ip() {
        //{ "@timestamp" : "2020-10-13T09:35:06.173Z","message" : "mongod: { "atype" : "authCheck", "ts" : { "$date" : "2020-10-13T05:34:58.768-0400" }, "local" : { "ip" : "127.0.0.1", "port" : 27017 }, "remote" : { "ip" : "127.0.0.1", "port" : 59056 }, "users" : [ { "user" : "admin", "db" : "admin" } ], "roles" : [ { "role" : "read", "db" : "admin" }, { "role" : "readWrite", "db" : "admin" } ], "param" : { "command" : "create", "ns" : "admin.shefali123", "args" : { "create" : "shefali123", "lsid" : { "id" : { "$binary" : "gnlGNvRNRLaLBFH0cC6Y2Q==", "$type" : "04" } }, "$db" : "admin" } }, "result" : 0 }","host" : "{mac=ConvertedList{delegate=[00:50:56:b4:77:7f]}, id=f6da45a8-225a-46c9-958a-ea7bca345534, ip=ConvertedList{delegate=[2620:1f7:853:a000:a700::5, fe80::953b:3ae2:ab60:d65, 9.42.101.19]}, os={kernel=10.0.17763.1457 (WinBuild.160101.0800), family=windows, version=10.0, name=Windows Server 2019 Standard, build=17763.1457, platform=windows}, name=sys-win2019db03, hostname=sys-win2019db03, architecture=x86_64}","agent" : "{id=90467cc3-4af0-4311-a999-ce9fb9ef80a4, type=filebeat, name=sys-win2019db03, version=7.8.1, hostname=sys-win2019db03, ephemeral_id=84bca17c-1919-4793-a6e9-0ecb748967c6}","server_ip" : "2620:1f7:853:a000:a700::5","tags" : "ConvertedList{delegate=[beats_input_codec_plain_applied]}","server_hostname" : "sys-win2019db03","input" : "{type=log}","log" : "{file={path=C:\Deploy\auditLog.json}, offset=1647316}","ecs" : "{version=1.5.0}","type" : "filebeat-mongodb-preset","source_program" : "mongod","@version" : "1","client_hostname" : "sys-win2019db03" }

        // Configuration config = new ConfigurationImpl(Collections.singletonMap("source", sourceField));
        Context context = new ContextImpl(null, null);
        MongodbGuardiumFilter filter = new MongodbGuardiumFilter("test-id", null, context);

        String clientIp = "(NONE)";
        String serverIp = "(NONE)";

        Event e = new org.logstash.Event();
        TestMatchListener matchListener = new TestMatchListener();

        String messageUnderTest = String.format(messageStrForIPTests, serverIp, clientIp);
        e.setField("message", messageUnderTest);
        Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);

        Assert.assertEquals(1, results.size());
        Assert.assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
        String recordStr = e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME).toString();
        Record record = new Gson().fromJson(recordStr, Record.class);

        Assert.assertTrue(record.getSessionLocator().getClientIp().equals("0.0.0.0"));
        Assert.assertTrue(record.getSessionLocator().getServerIp().equals("0.0.0.0"));
        Assert.assertFalse(record.getSessionLocator().isIpv6());
        Assert.assertEquals(1, matchListener.getMatchCount());

    }
}
