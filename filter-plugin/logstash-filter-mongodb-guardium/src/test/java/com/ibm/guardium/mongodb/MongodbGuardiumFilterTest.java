//
// Copyright 2020- IBM Inc. All rights reserved
// SPDX-License-Identifier: Apache2.0
//
package com.ibm.guardium.mongodb;

//import co.elastic.logstash.api.Configuration;
import co.elastic.logstash.api.Context;
import co.elastic.logstash.api.Event;
import co.elastic.logstash.api.FilterMatchListener;
import com.ibm.guardium.universalconnector.commons.GuardConstants;
import com.ibm.guardium.universalconnector.commons.structures.Accessor;
import com.ibm.guardium.universalconnector.commons.structures.Construct;
import com.ibm.guardium.universalconnector.commons.structures.Data;
import com.ibm.guardium.universalconnector.commons.structures.ExceptionRecord;
import com.ibm.guardium.universalconnector.commons.structures.Record;
import com.ibm.guardium.universalconnector.commons.structures.Sentence;
import com.ibm.guardium.universalconnector.commons.structures.SentenceObject;
import com.ibm.guardium.universalconnector.commons.structures.SessionLocator;
import com.ibm.guardium.universalconnector.commons.structures.Time;
import org.junit.Assert;
import org.junit.Test;
//import org.logstash.plugins.ConfigurationImpl;
import org.logstash.plugins.ContextImpl;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.gson.Gson;

import static com.ibm.guardium.mongodb.MongodbGuardiumFilter.LOGSTASH_TAG_SKIP_NOT_MONGODB;

public class MongodbGuardiumFilterTest {

    final static String mongodString = "<14>Feb 18 08:53:31 qa-db51 mongod: { \"atype\" : \"authCheck\", \"ts\" : { \"$date\" : \"2020-01-16T05:41:30.783-0500\" }, \"local\" : { \"ip\" : \"(NONE)\", \"port\" : 0 }, \"remote\" : { \"ip\" : \"(NONE)\", \"port\" : 0 }, \"users\" : [], \"roles\" : [], \"param\" : { \"command\" : \"find\", \"ns\" : \"config.transactions\", \"args\" : { \"find\" : \"transactions\", \"filter\" : { \"lastWriteDate\" : { \"$lt\" : { \"$date\" : \"2020-01-16T05:11:30.782-0500\" } } }, \"projection\" : { \"_id\" : 1 }, \"sort\" : { \"_id\" : 1 }, \"$db\" : \"config\" } }, \"result\" : 0 }";
    final static Context context = new ContextImpl(null, null);
    final static MongodbGuardiumFilter filter = new MongodbGuardiumFilter("test-id", null, context);

    /**
     * To feed Guardium universal connector, a "GuardRecord" fields must exist. 
     *
     * Filter should add field "GuardRecord" to the Event, which Universal connector then inserts into Guardium.   
     */
    @Test
    public void testFieldGuardRecord_mongod() {
        final String mongodString2 = "mongod: { \"atype\" : \"authCheck\", \"ts\" : { \"$date\" : \"2020-08-19T11:52:27.495-0400\" }, \"local\" : { \"ip\" : \"127.0.0.1\", \"port\" : 27017 }, \"remote\" : { \"ip\" : \"127.0.0.1\", \"port\" : 10688 }, \"users\" : [ { \"user\" : \"realAdmin\", \"db\" : \"admin\" } ], \"roles\" : [ { \"role\" : \"readWriteAnyDatabase\", \"db\" : \"admin\" }, { \"role\" : \"readWrite\", \"db\" : \"newDB02\" }, { \"role\" : \"userAdminAnyDatabase\", \"db\" : \"admin\" } ], \"param\" : { \"command\" : \"getCmdLineOpts\", \"ns\" : \"admin\", \"args\" : { \"getCmdLineOpts\" : 1, \"lsid\" : { \"id\" : { \"$binary\" : \"9d5Ezoo2ROy/uYNsTpGJww==\", \"$type\" : \"04\" } }, \"$db\" : \"admin\" } }, \"result\" : 13 }";

        // Configuration config = new ConfigurationImpl(Collections.singletonMap("source", sourceField));
        Context context = new ContextImpl(null, null);
        MongodbGuardiumFilter filter = new MongodbGuardiumFilter("test-id", null, context);

        Event e = new org.logstash.Event();
        TestMatchListener matchListener = new TestMatchListener();

        e.setField("message", mongodString2);
        Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);

        Assert.assertEquals(1, results.size());
        Assert.assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
        Assert.assertEquals(1, matchListener.getMatchCount());
    }

    @Test
    public void testParseICDMongo_nomessage() {
        Context context = new ContextImpl(null, null);
        MongodbGuardiumFilter filter = new MongodbGuardiumFilter("test-id", null, context);

        Event e = new org.logstash.Event();
        TestMatchListener matchListener = new TestMatchListener();
        Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
        Assert.assertNotNull(results);
    }

    @Test
    public void testParseICDMongo_dbname_prefix_Empty() {
        final String mongosString ="2023-02-06T14:24:55.125865418Z stdout F  mongod: { 'atype' : 'createCollection', 'ts' : { '$date' : '2022-12-15T06:19:23.284+00:00' }, 'local' : { 'ip' : '172.30.23.154', 'port' : 31040 }, 'remote' : { 'ip' : '172.30.175.28', 'port' : 55068 }, 'users' : [ { 'user' : 'admin', 'db' : 'admin' } ], 'roles' : [ { 'role' : 'dbAdminAnyDatabase', 'db' : 'admin' }, { 'role' : 'readWriteAnyDatabase', 'db' : 'admin' }, { 'role' : 'userAdminAnyDatabase', 'db' : 'admin' } ], 'param' : { 'ns' : 'testdatabase.testcollection1' }, 'result' : 0 }";
        Context context = new ContextImpl(null, null);
        MongodbGuardiumFilter filter = new MongodbGuardiumFilter("test-id", null, context);

        Event e = new org.logstash.Event();
        TestMatchListener matchListener = new TestMatchListener();

        e.setField("message", mongosString);
        e.setField("dbname_prefix","");
        Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);

        Assert.assertEquals(1, results.size());
        String recordString = e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME).toString();
        Record record = (new Gson()).fromJson(recordString, Record.class);
        Assert.assertNotNull(record);
        Assert.assertEquals("testdatabase",record.getDbName());
    }

    @Test
    public void testParseICDMongo_DbName_NotAvailable() {
        final String mongosString ="2023-02-06T14:10:39.305086948Z stdout F  mongod: { 'atype' : 'createUser', 'ts' : { '$date' : '2022-12-16T07:07:48.710+00:00' }, 'local' : { 'ip' : '172.30.93.219', 'port' : 32298 }, 'remote' : { 'ip' : '172.30.28.157', 'port' : 49350 }, 'users' : [ { 'user' : 'admin', 'db' : 'admin' } ], 'roles' : [ { 'role' : 'readWriteAnyDatabase', 'db' : 'admin' }, { 'role' : 'dbAdminAnyDatabase', 'db' : 'admin' }, { 'role' : 'userAdminAnyDatabase', 'db' : 'admin' } ], 'param' : { 'user' : 'myadmin1', 'db' : '', 'roles' : [ { 'role' : 'readWrite', 'db' : 'config' }, { 'role' : 'read', 'db' : 'orders' }, { 'role' : 'read', 'db' : 'test' } ] }, 'result' : 0 }";
        Context context = new ContextImpl(null, null);
        MongodbGuardiumFilter filter = new MongodbGuardiumFilter("test-id", null, context);

        Event e = new org.logstash.Event();
        TestMatchListener matchListener = new TestMatchListener();

        e.setField("message", mongosString);
        e.setField("server_hostname","testid44505a917e8fcb952c4ce-test75b-776c-486e-8ee7-f6a0ddd5a1cb.ibm.com");
        e.setField("dbname_prefix","testid44505a917e8fcb952c4ce-test75b-776c-486e-8ee7-f6a0ddd5a1cb");
        Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);

        Assert.assertEquals(1, results.size());
        String recordString = e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME).toString();
        Record record = (new Gson()).fromJson(recordString, Record.class);
        Assert.assertNotNull(record);
        Assert.assertEquals("testid44505a917e8fcb952c4ce-test75b-776c-486e-8ee7-f6a0ddd5a1cb",record.getDbName() );
    }

    @Test
    public void testParseICDMongo_authenticate_LoginFailedLog() {
        final String mongosString = "2023-02-07T09:09:58.256243217Z stdout F  mongod: { 'atype' : 'authenticate', 'ts' : { '$date' : '2023-03-30T13:33:43.676+00:00' }, 'local' : { 'ip' : '172.30.135.140', 'port' : 30137 }, 'remote' : { 'ip' : '172.30.3.142', 'port' : 50710 }, 'users' : [ { 'user' : 'admin', 'db' : 'admin' }, { 'user' : 'IBMuser', 'db' : 'ec2db' } ], 'roles' : [ { 'role' : 'dbAdminAnyDatabase', 'db' : 'admin' }, { 'role' : 'readWriteAnyDatabase', 'db' : 'admin' }, { 'role' : 'userAdminAnyDatabase', 'db' : 'admin' } ], 'param' : { 'user' : 'IBMuser', 'db' : 'ec2db', 'mechanism' : 'SCRAM-SHA-256' }, 'result' : 18 }";
        Context context = new ContextImpl(null, null);
        MongodbGuardiumFilter filter = new MongodbGuardiumFilter("test-id", null, context);

        Event e = new org.logstash.Event();
        TestMatchListener matchListener = new TestMatchListener();

        e.setField("message", mongosString);
        e.setField("server_hostname","testid44505a917e8fcb952c4ce-test75b-776c-486e-8ee7-f6a0ddd5a1cb.ibm.com");
        e.setField("dbname_prefix","testid44505a917e8fcb952c4ce-test75b-776c-486e-8ee7-f6a0ddd5a1cb");
        Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);

        Assert.assertEquals(1, results.size());
        String recordString = e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME).toString();
        Record record = (new Gson()).fromJson(recordString, Record.class);
        Assert.assertNotNull(record);
        Assert.assertEquals(Parser.EXCEPTION_TYPE_AUTHENTICATION_STRING, record.getException().getExceptionTypeId());
        Assert.assertEquals("admin IBMuser",record.getAccessor().getDbUser());
    }

    @Test
    public void testParseICDMongo_IPV6() {
        final String mongosString ="2023-02-06T14:12:33.410775871Z stdout F  mongod: { 'atype' : 'createRole', 'ts' : { '$date' : '2022-12-16T06:38:59.589+00:00' }, 'local' : { 'ip' : '2001:0db8:85a3:0000:0000:8a2e:0370:7334', 'port' : 32298 }, 'remote' : { 'ip' : '2001:0db8:85a3:0000:0000:8a2e:0370:7337', 'port' : 49350 }, 'users' : [ { 'user' : 'admin', 'db' : 'admin' } ], 'roles' : [ { 'role' : 'userAdminAnyDatabase', 'db' : 'admin' }, { 'role' : 'dbAdminAnyDatabase', 'db' : 'admin' }, { 'role' : 'readWriteAnyDatabase', 'db' : 'admin' } ], 'param' : { 'role' : 'newroll', 'db' : 'admin', 'roles' : [ { 'role' : 'readWrite', 'db' : 'config' }, { 'role' : 'read', 'db' : 'orders' }, { 'role' : 'read', 'db' : 'test' } ], 'privileges' : [ { 'resource' : { 'cluster' : true }, 'actions' : [ 'addShard' ] }, { 'resource' : { 'db' : 'config', 'collection' : '' }, 'actions' : [ 'find', 'insert', 'remove', 'update' ] }, { 'resource' : { 'db' : 'users', 'collection' : 'usersCollection' }, 'actions' : [ 'insert', 'remove', 'update' ] }, { 'resource' : { 'db' : 'admin', 'collection' : '' }, 'actions' : [ 'find' ] } ] }, 'result' : 0 }";
        Context context = new ContextImpl(null, null);
        MongodbGuardiumFilter filter = new MongodbGuardiumFilter("test-id", null, context);

        Event e = new org.logstash.Event();
        TestMatchListener matchListener = new TestMatchListener();

        e.setField("message", mongosString);
        e.setField("server_hostname","testid44505a917e8fcb952c4ce-test75b-776c-486e-8ee7-f6a0ddd5a1cb.ibm.com");
        e.setField("dbname_prefix","testid44505a917e8fcb952c4ce-test75b-776c-486e-8ee7-f6a0ddd5a1cb");
        Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);

        Assert.assertEquals(1, results.size());
        String recordString = e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME).toString();
        Record record = (new Gson()).fromJson(recordString, Record.class);
        Assert.assertNotNull(record);
        Assert.assertEquals(true,record.getSessionLocator().isIpv6());
    }

    @Test
    public void testParseICDMongo_authenticate_UnknownErrorLog() {
        final String mongosString ="2023-02-07T09:09:58.256243217Z stdout F  mongod: { 'atype' : 'authenticate', 'ts' : { '$date' : '2022-12-16T08:47:36.788+00:00' }, 'local' : { 'ip' : '172.30.93.219', 'port' : 32298 }, 'remote' : { 'ip' : '172.30.28.157', 'port' : 45730 }, 'users' : [], 'roles' : [], 'param' : { 'user' : 'admin', 'db' : 'admin', 'mechanism' : 'SCRAM-SHA-256' }, 'result' : 20 }";
        Context context = new ContextImpl(null, null);
        MongodbGuardiumFilter filter = new MongodbGuardiumFilter("test-id", null, context);

        Event e = new org.logstash.Event();
        TestMatchListener matchListener = new TestMatchListener();

        e.setField("message", mongosString);
        e.setField("server_hostname","testid44505a917e8fcb952c4ce-test75b-776c-486e-8ee7-f6a0ddd5a1cb.ibm.com");
        e.setField("dbname_prefix","testid44505a917e8fcb952c4ce-test75b-776c-486e-8ee7-f6a0ddd5a1cb");
        Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);

        Assert.assertEquals(1, results.size());
        String recordString = e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME).toString();
        Record record = (new Gson()).fromJson(recordString, Record.class);
        Assert.assertNotNull(record);
        Assert.assertEquals(Parser.EXCEPTION_TYPE_AUTHORIZATION_STRING, record.getException().getExceptionTypeId());
        Assert.assertEquals("Error (20)", record.getException().getDescription());
    }

    @Test
    public void testParseICDMongo_IPNotAvailable() {
        final String mongosString ="2023-02-07T09:09:58.256243217Z stdout F  mongod: { 'atype' : 'authenticate', 'ts' : { '$date' : '2022-12-16T08:47:36.788+00:00' }, 'users' : [], 'roles' : [], 'param' : { 'user' : 'admin', 'db' : 'admin', 'mechanism' : 'SCRAM-SHA-256' }, 'result' : 18 }";
        Context context = new ContextImpl(null, null);
        MongodbGuardiumFilter filter = new MongodbGuardiumFilter("test-id", null, context);

        Event e = new org.logstash.Event();
        TestMatchListener matchListener = new TestMatchListener();

        e.setField("message", mongosString);
        e.setField("server_hostname","testid44505a917e8fcb952c4ce-test75b-776c-486e-8ee7-f6a0ddd5a1cb.ibm.com");
        e.setField("dbname_prefix","testid44505a917e8fcb952c4ce-test75b-776c-486e-8ee7-f6a0ddd5a1cb");
        Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);

        Assert.assertEquals(1, results.size());
        String recordString = e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME).toString();
        Record record = (new Gson()).fromJson(recordString, Record.class);
        Assert.assertNotNull(record);
        Assert.assertEquals("", record.getSessionLocator().getClientIp());
        Assert.assertEquals(-1, record.getSessionLocator().getClientPort());
    }

    @Test
    public void testFieldGuardRecord_mongos() {
        final String mongosString = "mongos: { \"atype\" : \"authCheck\", \"ts\" : { \"$date\" : \"2020-10-08T13:58:00.222+0300\" }, \"local\" : { \"ip\" : \"127.0.0.1\", \"port\" : 28017 }, \"remote\" : { \"ip\" : \"127.0.0.1\", \"port\" : 45824 }, \"users\" : [ { \"user\" : \"admin\", \"db\" : \"admin\" } ], \"roles\" : [ { \"role\" : \"root\", \"db\" : \"admin\" } ], \"param\" : { \"command\" : \"replSetGetStatus\", \"ns\" : \"admin\", \"args\" : { \"replSetGetStatus\" : 1, \"forShell\" : 1, \"$clusterTime\" : { \"clusterTime\" : { \"$timestamp\" : { \"t\" : 1602154677, \"i\" : 1 } }, \"signature\" : { \"hash\" : { \"$binary\" : \"LYKHSxxbXvcIDvX3FAhpam1SdYk=\", \"$type\" : \"00\" }, \"keyId\" : { \"$numberLong\" : \"6880241122304589825\" } } }, \"$db\" : \"admin\" } }, \"result\" : 0 }";
        //"mongod: { \"atype\" : \"authCheck\", \"ts\" : { \"$date\" : \"2020-08-19T11:52:27.495-0400\" }, \"local\" : { \"ip\" : \"127.0.0.1\", \"port\" : 27017 }, \"remote\" : { \"ip\" : \"127.0.0.1\", \"port\" : 10688 }, \"users\" : [ { \"user\" : \"realAdmin\", \"db\" : \"admin\" } ], \"roles\" : [ { \"role\" : \"readWriteAnyDatabase\", \"db\" : \"admin\" }, { \"role\" : \"readWrite\", \"db\" : \"newDB02\" }, { \"role\" : \"userAdminAnyDatabase\", \"db\" : \"admin\" } ], \"param\" : { \"command\" : \"getCmdLineOpts\", \"ns\" : \"admin\", \"args\" : { \"getCmdLineOpts\" : 1, \"lsid\" : { \"id\" : { \"$binary\" : \"9d5Ezoo2ROy/uYNsTpGJww==\", \"$type\" : \"04\" } }, \"$db\" : \"admin\" } }, \"result\" : 13 }";

        // Configuration config = new ConfigurationImpl(Collections.singletonMap("source", sourceField));
        Context context = new ContextImpl(null, null);
        MongodbGuardiumFilter filter = new MongodbGuardiumFilter("test-id", null, context);

        Event e = new org.logstash.Event();
        TestMatchListener matchListener = new TestMatchListener();

        e.setField("message", mongosString);
        Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);

        Assert.assertEquals(1, results.size());
        Assert.assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
        Assert.assertEquals(1, matchListener.getMatchCount());
    }

    @Test
    public void testFieldGuardRecord_auditMsgNotFound() {
        final String mongodString2 = "mongodBlaBla: { \"atype\" : \"authCheck\", \"ts\" : { \"$date\" : \"2020-08-19T11:52:27.495-0400\" }, \"local\" : { \"ip\" : \"127.0.0.1\", \"port\" : 27017 }, \"remote\" : { \"ip\" : \"127.0.0.1\", \"port\" : 10688 }, \"users\" : [ { \"user\" : \"realAdmin\", \"db\" : \"admin\" } ], \"roles\" : [ { \"role\" : \"readWriteAnyDatabase\", \"db\" : \"admin\" }, { \"role\" : \"readWrite\", \"db\" : \"newDB02\" }, { \"role\" : \"userAdminAnyDatabase\", \"db\" : \"admin\" } ], \"param\" : { \"command\" : \"getCmdLineOpts\", \"ns\" : \"admin\", \"args\" : { \"getCmdLineOpts\" : 1, \"lsid\" : { \"id\" : { \"$binary\" : \"9d5Ezoo2ROy/uYNsTpGJww==\", \"$type\" : \"04\" } }, \"$db\" : \"admin\" } }, \"result\" : 13 }";

        // Configuration config = new ConfigurationImpl(Collections.singletonMap("source", sourceField));
        Context context = new ContextImpl(null, null);
        MongodbGuardiumFilter filter = new MongodbGuardiumFilter("test-id", null, context);

        Event e = new org.logstash.Event();
        TestMatchListener matchListener = new TestMatchListener();

        e.setField("message", mongodString2);
        Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);

        Assert.assertEquals(1, results.size());
        Set<String> tags = new HashSet<>((ArrayList)e.getField("tags"));
        Assert.assertEquals(1, tags.size());
        Assert.assertTrue(tags.contains(LOGSTASH_TAG_SKIP_NOT_MONGODB));
    }

    @Test
    public void testParseMongoSyslog() {
        final String mongodString = "<14>Feb 18 08:53:31 qa-db51 mongod: { \"atype\" : \"authCheck\", \"ts\" : { \"$date\" : \"2020-06-11T09:44:11.070-0400\" }, \"local\" : { \"ip\" : \"9.70.147.59\", \"port\" : 27017 }, \"remote\" : { \"ip\" : \"9.148.202.94\", \"port\" : 60185 }, \"users\" : [ { \"user\" : \"realAdmin\", \"db\" : \"admin\" } ], \"roles\" : [ { \"role\" : \"readWriteAnyDatabase\", \"db\" : \"admin\" }, { \"role\" : \"userAdminAnyDatabase\", \"db\" : \"admin\" } ], \"param\" : { \"command\" : \"find\", \"ns\" : \"admin.USERS\", \"args\" : { \"find\" : \"USERS\", \"filter\" : {}, \"lsid\" : { \"id\" : { \"$binary\" : \"mV20eHvvRha2ELTeqJxQJg==\", \"$type\" : \"04\" } }, \"$db\" : \"admin\", \"$readPreference\" : { \"mode\" : \"primaryPreferred\" } } }, \"result\" : 0 }";
        Context context = new ContextImpl(null, null);
        MongodbGuardiumFilter filter = new MongodbGuardiumFilter("test-id", null, context);

        Event e = new org.logstash.Event();
        TestMatchListener matchListener = new TestMatchListener();

        e.setField("message", mongodString);
        Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);

        Assert.assertEquals(1, results.size());
        Assert.assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
        Assert.assertEquals(1, matchListener.getMatchCount());
    }

    @Test
    public void testParseOtherSyslog() {
        String syslogString = "<7>Feb 18 08:55:14 qa-db51 kernel: IPv6 addrconf: prefix with wrong length 96";
        Context context = new ContextImpl(null, null);
        MongodbGuardiumFilter filter = new MongodbGuardiumFilter("test-id", null, context);

        Event e = new org.logstash.Event();
        TestMatchListener matchListener = new TestMatchListener();

        e.setField("message", syslogString);
        Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);

        Assert.assertEquals(1, results.size());
        Assert.assertEquals(true,
                e.getField("tags").toString().contains("_mongoguardium_skip"));
        //Assert.assertNull(e.getField("Construct"));
        Assert.assertEquals(0, matchListener.getMatchCount());
    }

    /**
     * Tests that messages are skipped & removed if atype != "authCheck"
     */
    @Test
    public void testParseMongo_skip_remove_atype_unknownType() {
        String messageString = "<14>Feb 18 08:53:31 qa-db51 mongod: { \"atype\" : \"unknownType\", \"ts\" : { \"$date\" : \"2020-06-03T03:40:30.888-0400\" }, \"local\" : { \"ip\" : \"127.0.0.1\", \"port\" : 27017 }, \"remote\" : { \"ip\" : \"127.0.0.1\", \"port\" : 40426 }, \"users\" : [ { \"user\" : \"realAdmin\", \"db\" : \"admin\" } ], \"roles\" : [ { \"role\" : \"readWriteAnyDatabase\", \"db\" : \"admin\" }, { \"role\" : \"userAdminAnyDatabase\", \"db\" : \"admin\" } ], \"param\" : { \"ns\" : \"newDB01.newCollection01\" }, \"result\" : 0 }";
        Context context = new ContextImpl(null, null);
        MongodbGuardiumFilter filter = new MongodbGuardiumFilter("test-id", null, context);

        Event e = new org.logstash.Event();
        TestMatchListener matchListener = new TestMatchListener();
        ArrayList<Event> events = new ArrayList<>();
        e.setField("message", messageString);
        events.add(e);

        Collection<Event> results = filter.filter(events, matchListener);

        Assert.assertEquals(1, results.size());
        Assert.assertEquals(0, matchListener.getMatchCount());
    }

    /**
     * Tests that messages without identified users are skipped & removed
     */
    @Test
    public void testParseMongo_skip_remove_empty_users() {
        String messageString = "<14>Feb 18 08:53:31 qa-db51 mongod: { \"atype\" : \"authCheck\", \"ts\" : { \"$date\" : \"2020-06-11T09:50:21.485-0400\" }, \"local\" : { \"ip\" : \"9.70.147.59\", \"port\" : 27017 }, \"remote\" : { \"ip\" : \"9.148.202.94\", \"port\" : 60241 }, \"users\" : [], \"roles\" : [], \"param\" : { \"command\" : \"isMaster\", \"ns\" : \"admin\", \"args\" : { \"ismaster\" : 1, \"client\" : { \"driver\" : { \"name\" : \"PyMongo\", \"version\" : \"3.10.1\" }, \"os\" : { \"type\" : \"Darwin\", \"name\" : \"Darwin\", \"architecture\" : \"x86_64\", \"version\" : \"10.14.6\" }, \"platform\" : \"CPython 2.7.15.final.0\" }, \"$db\" : \"admin\" } }, \"result\" : 0 }";
        Context context = new ContextImpl(null, null);
        MongodbGuardiumFilter filter = new MongodbGuardiumFilter("test-id", null, context);

        Event e = new org.logstash.Event();
        TestMatchListener matchListener = new TestMatchListener();
        ArrayList<Event> events = new ArrayList<>();
        e.setField("message", messageString);
        events.add(e);

        Collection<Event> results = filter.filter(events, matchListener);

        Assert.assertEquals(0, results.size());
        Assert.assertEquals(0, matchListener.getMatchCount());
    }

    /**
     * Tests that atype="authenticate" messages are skipped & removed.
     *
     * Unsuccessful messages are handled as reported as Exception (Failed login in Guardium).
     */
    //@Test
    public void testParseMongo_skip_remove_atype_authenticate_successful() {
        String messageString = "<14>Feb 18 08:53:31 qa-db51 mongod: { \"atype\" : \"authenticate\", \"ts\" : { \"$date\" : \"2020-06-09T08:34:12.424-0400\" }, \"local\" : { \"ip\" : \"9.70.147.59\", \"port\" : 27017 }, \"remote\" : { \"ip\" : \"9.148.206.148\", \"port\" : 49712 }, \"users\" : [ { \"user\" : \"realAdmin\", \"db\" : \"admin\" } ], \"roles\" : [ { \"role\" : \"readWriteAnyDatabase\", \"db\" : \"admin\" }, { \"role\" : \"userAdminAnyDatabase\", \"db\" : \"admin\" } ], \"param\" : { \"user\" : \"realAdmin\", \"db\" : \"admin\", \"mechanism\" : \"SCRAM-SHA-256\" }, \"result\" : 0 }";
        Context context = new ContextImpl(null, null);
        MongodbGuardiumFilter filter = new MongodbGuardiumFilter("test-id", null, context);

        Event e = new org.logstash.Event();
        TestMatchListener matchListener = new TestMatchListener();
        ArrayList<Event> events = new ArrayList<>();
        e.setField("message", messageString);
        events.add(e);

        Collection<Event> results = filter.filter(events, matchListener);

        Assert.assertEquals(0, results.size());
        Assert.assertEquals(0, matchListener.getMatchCount());
    }

    /**
     * Test integrity of events collection, after skipped events were removed from it.
     *
     * Tests also that authentication failure is handled and not removed, even though empty users[].
     */
    @Test
    public void testParseMongo_eventsCollectionIntegrity() {
        String messageStringOK = "<14>Feb 18 08:53:31 qa-db51 mongod: { \"atype\" : \"authCheck\", \"ts\" : { \"$date\" : \"2020-06-11T09:44:11.070-0400\" }, \"local\" : { \"ip\" : \"9.70.147.59\", \"port\" : 27017 }, \"remote\" : { \"ip\" : \"9.148.202.94\", \"port\" : 60185 }, \"users\" : [ { \"user\" : \"realAdmin\", \"db\" : \"admin\" } ], \"roles\" : [ { \"role\" : \"readWriteAnyDatabase\", \"db\" : \"admin\" }, { \"role\" : \"userAdminAnyDatabase\", \"db\" : \"admin\" } ], \"param\" : { \"command\" : \"find\", \"ns\" : \"admin.USERS\", \"args\" : { \"find\" : \"USERS\", \"filter\" : {}, \"lsid\" : { \"id\" : { \"$binary\" : \"mV20eHvvRha2ELTeqJxQJg==\", \"$type\" : \"04\" } }, \"$db\" : \"admin\", \"$readPreference\" : { \"mode\" : \"primaryPreferred\" } } }, \"result\" : 0 }";
        String messageStringOK2 = "<14>Feb 18 08:53:32 qa-db51 mongod: { \"atype\" : \"createCollection\", \"ts\" : { \"$date\" : \"2020-06-03T03:40:30.888-0400\" }, \"local\" : { \"ip\" : \"127.0.0.1\", \"port\" : 27017 }, \"remote\" : { \"ip\" : \"127.0.0.1\", \"port\" : 40426 }, \"users\" : [ { \"user\" : \"realAdmin\", \"db\" : \"admin\" } ], \"roles\" : [ { \"role\" : \"readWriteAnyDatabase\", \"db\" : \"admin\" }, { \"role\" : \"userAdminAnyDatabase\", \"db\" : \"admin\" } ], \"param\" : { \"ns\" : \"newDB01.newCollection01\" }, \"result\" : 0 }";
        String messageStringSkip = "<14>Feb 18 08:53:32 qa-db51 mongod: { \"atype\" : \"unsupportedTypeCollection\", \"ts\" : { \"$date\" : \"2020-06-03T03:40:30.888-0400\" }, \"local\" : { \"ip\" : \"127.0.0.1\", \"port\" : 27017 }, \"remote\" : { \"ip\" : \"127.0.0.1\", \"port\" : 40426 }, \"users\" : [ { \"user\" : \"realAdmin\", \"db\" : \"admin\" } ], \"roles\" : [ { \"role\" : \"readWriteAnyDatabase\", \"db\" : \"admin\" }, { \"role\" : \"userAdminAnyDatabase\", \"db\" : \"admin\" } ], \"param\" : { \"ns\" : \"newDB01.newCollection01\" }, \"result\" : 0 }";
        String messageStringAuthOK = "<14>Feb 18 08:53:33 qa-db51 mongod: { \"atype\" : \"authenticate\", \"ts\" : { \"$date\" : \"2020-05-17T11:37:30.421-0400\" }, \"local\" : { \"ip\" : \"127.0.0.1\", \"port\" : 27017 }, \"remote\" : { \"ip\" : \"127.0.0.1\", \"port\" : 29398 }, \"users\" : [], \"roles\" : [], \"param\" : { \"user\" : \"readerUser\", \"db\" : \"admin\", \"mechanism\" : \"SCRAM-SHA-256\" }, \"result\" : 18 }";
        Context context = new ContextImpl(null, null);
        MongodbGuardiumFilter filter = new MongodbGuardiumFilter("test-id", null, context);

        TestMatchListener matchListener = new TestMatchListener();
        ArrayList<Event> inputEvents = new ArrayList<>();
        Event e = new org.logstash.Event();
        e.setField("message", messageStringOK);
        Event eCollection = new org.logstash.Event();
        eCollection.setField("message", messageStringOK2);
        Event eSkip = new org.logstash.Event();
        eSkip.setField("message", messageStringSkip);
        Event eAuth = new org.logstash.Event();
        eAuth.setField("message", messageStringAuthOK);
        inputEvents.add(e);
        inputEvents.add(eCollection);
        inputEvents.add(eSkip);
        inputEvents.add(eAuth);

        Collection<Event> results = filter.filter(inputEvents, matchListener);

        Assert.assertEquals(4, results.size());
        Assert.assertEquals(true, e.getField("tags") == null );
        Assert.assertEquals(true, eAuth.getField("tags") == null);
        Assert.assertEquals(3, matchListener.getMatchCount());
    }

    /**
     * Tests that localhost IPs stay that way, if no "server_ip" field exist in logstash event.
     *
     * NOTE: Events with "(NONE)" have no users[], so they are removed from events.
     */
    @Test
    public void testParseMongoSyslog_localhostIPs_remain() {
        final String mongodString = "<14>Feb 18 08:53:31 qa-db51 mongod: { \"atype\" : \"authCheck\", \"ts\" : { \"$date\" : \"2020-01-16T05:41:30.783-0500\" }, \"local\" : { \"ip\" : \"127.0.0.1\", \"port\" : 10400 }, \"remote\" : { \"ip\" : \"127.0.0.1\", \"port\" : 27017 }, \"users\" : [ { \"user\" : \"realAdmin\", \"db\" : \"admin\" } ], \"roles\" : [ { \"role\" : \"readWriteAnyDatabase\", \"db\" : \"admin\" }, { \"role\" : \"userAdminAnyDatabase\", \"db\" : \"admin\" } ], \"param\" : { \"command\" : \"find\", \"ns\" : \"config.transactions\", \"args\" : { \"find\" : \"transactions\", \"filter\" : { \"lastWriteDate\" : { \"$lt\" : { \"$date\" : \"2020-01-16T05:11:30.782-0500\" } } }, \"projection\" : { \"_id\" : 1 }, \"sort\" : { \"_id\" : 1 }, \"$db\" : \"config\" } }, \"result\" : 0 }";
        final String expectedIP = "127.0.0.1";

        Event e = new org.logstash.Event();
        TestMatchListener matchListener = new TestMatchListener();

        e.setField("message", mongodString); // with "(NONE)" local/remote IPs
        // no server_ip field

        Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);

        Assert.assertEquals(1, results.size());
        String recordString = e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME).toString();
        Record record = (new Gson()).fromJson(recordString, Record.class);
        Assert.assertNotNull(record);

        Assert.assertEquals(
                "When no server_ip field, MongoDB remote IP should not be overriden.",
                expectedIP, record.getSessionLocator().getClientIp());
        Assert.assertEquals(
                "When no server_ip field, MongoDB local IP should not be overriden.",
                expectedIP, record.getSessionLocator().getServerIp());
    }
    /**
     * Tests that localhost IPs are overriden with "server_ip", if exists in logstash Event.

     * On events with local IP, convert to server IP, if sent thru logstash event by Filebeat/syslog. 
     * NOTE: Events with "(NONE)" have no users[], so they are removed from events.
     */
    @Test
    public void testParseMongoSyslog_localhostIP_override() {
        final String mongodString = "<14>Feb 18 08:53:31 qa-db51 mongod: { \"atype\" : \"authCheck\", \"ts\" : { \"$date\" : \"2020-01-16T05:41:30.783-0500\" }, \"local\" : { \"ip\" : \"127.0.0.1\", \"port\" : 10400 }, \"remote\" : { \"ip\" : \"127.0.0.1\", \"port\" : 27017 }, \"users\" : [ { \"user\" : \"realAdmin\", \"db\" : \"admin\" } ], \"roles\" : [ { \"role\" : \"readWriteAnyDatabase\", \"db\" : \"admin\" }, { \"role\" : \"userAdminAnyDatabase\", \"db\" : \"admin\" } ], \"param\" : { \"command\" : \"find\", \"ns\" : \"config.transactions\", \"args\" : { \"find\" : \"transactions\", \"filter\" : { \"lastWriteDate\" : { \"$lt\" : { \"$date\" : \"2020-01-16T05:11:30.782-0500\" } } }, \"projection\" : { \"_id\" : 1 }, \"sort\" : { \"_id\" : 1 }, \"$db\" : \"config\" } }, \"result\" : 0 }";
        final String expectedIP = "20.30.40.50";

        Event e = new org.logstash.Event();
        TestMatchListener matchListener = new TestMatchListener();

        e.setField("message", mongodString); // with "(NONE)" local/remote IPs
        e.setField("server_ip", expectedIP);

        Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);

        Assert.assertEquals(1, results.size());
        String recordString = e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME).toString();
        Record record = (new Gson()).fromJson(recordString, Record.class);
        Assert.assertNotNull(record);

        Assert.assertEquals(
                "server_ip field should be used, if exists, when MongoDB remote IP points to localhost.",
                expectedIP, record.getSessionLocator().getClientIp());
        Assert.assertEquals(
                "server_ip field should be used, if exists, when MongoDB local IP points to localhost.",
                expectedIP, record.getSessionLocator().getServerIp());
    }

    @Test
    public void testParseMongoSyslog_doNotInjectHost() {
        // syslog message uses different IPs for local/remote:
        final String mongodString = "<14>Feb 18 08:53:31 qa-db51 mongod: { \"atype\" : \"authCheck\", \"ts\" : { \"$date\" : \"2020-01-16T05:41:30.783-0500\" }, \"local\" : { \"ip\" : \"1.2.3.456\", \"port\" : 0 }, \"remote\" : { \"ip\" : \"1.2.3.123\", \"port\" : 0 }, \"users\" : [ { \"user\" : \"realAdmin\", \"db\" : \"admin\" } ], \"roles\" : [ { \"role\" : \"readWriteAnyDatabase\", \"db\" : \"admin\" }, { \"role\" : \"userAdminAnyDatabase\", \"db\" : \"admin\" } ], \"roles\" : [], \"param\" : { \"command\" : \"find\", \"ns\" : \"config.transactions\", \"args\" : { \"find\" : \"transactions\", \"filter\" : { \"lastWriteDate\" : { \"$lt\" : { \"$date\" : \"2020-01-16T05:11:30.782-0500\" } } }, \"projection\" : { \"_id\" : 1 }, \"sort\" : { \"_id\" : 1 }, \"$db\" : \"config\" } }, \"result\" : 0 }";
        final String hostString = "9.9.9.9";

        Event e = new org.logstash.Event();
        TestMatchListener matchListener = new TestMatchListener();

        e.setField("message", mongodString);
        e.setField("host", hostString);

        Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);

        Assert.assertEquals(1, results.size());
        String recordString = e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME).toString();
        Record record = (new Gson()).fromJson(recordString, Record.class);
        Assert.assertNotNull(record);

        Assert.assertEquals(
                "host should not override client IP when native audit shows different IPs for local & remote",
                "1.2.3.123", record.getSessionLocator().getClientIp());
        Assert.assertEquals(
                "host should not override server IP when native audit shows different IPs for local & remote",
                "1.2.3.456", record.getSessionLocator().getServerIp());
    }


    /**
     * Simulates cut-off towards end of Syslog message, and tests proper JSON parse error tag is attached 
     * */
    @Test
    public void testTagParseError() {
        String messageString = "<14>Feb 18 08:53:31 qa-db51 mongod: { \"atype\" : \"authCheck\", \"ts\" : { \"$date\" : \"2020-06-11T09:44:11.070-0400\" }, \"local\" : { \"ip\" : \"9.70.147.59\", \"port\" : 27017 }, \"remote\" : { \"ip\" : \"9.148.202.94\", \"port\" : 60185 }, \"users\" : [ { \"user\" : \"realAdmin\", \"db\" : \"admin\" } ], \"roles\" : [ { \"role\" : \"readWriteAnyDatabase\", \"db\" : \"admin\" }, { \"role\" : \"userAdminAnyDatabase\", \"db\" : \"admin\" } ], \"param\" : { \"command\" : \"find\", \"ns\" : \"admin.USERS\", \"args\" : { \"find\" : \"USERS\", \"filter\" : {}, \"lsid\" : { \"id\" : { \"$binary\" : \"mV20eHvvRha2ELTeqJxQJg==\", \"$type\" : \"04\" } }, \"$db\" : \"admin\", \"$readPreference\" : { \"mode\" : \"primaryPreferred\" } } }, \"res";
        Context context = new ContextImpl(null, null);
        MongodbGuardiumFilter filter = new MongodbGuardiumFilter("test-id", null, context);

        Event e = new org.logstash.Event();
        TestMatchListener matchListener = new TestMatchListener();

        e.setField("message", messageString);
        ArrayList<Event> events = new ArrayList<>(Arrays.asList(new Event[]{e}));
        Collection<Event> results = filter.filter(events, matchListener);

        Assert.assertEquals(1, results.size());
        Assert.assertEquals(true, e.getField("tags").toString().contains(
                MongodbGuardiumFilter.LOGSTASH_TAG_JSON_PARSE_ERROR));
        Assert.assertEquals(0, matchListener.getMatchCount()); // just sigals as not a match, so no further tags will be added, in pipeline.
    }

    @Test
    public void testIcdLog() {
        String messageString = "%{otherdetail} mongod: { \\\"atype\\\" : \\\"createIndex\\\", \\\"ts\\\" : { \\\"$date\\\" : \\\"2025-03-25T04:20:54.349+00:00\\\" }, \\\"uuid\\\" : { \\\"$binary\\\" : \\\"1Abh27aUQFaZG4bAMTCe1g==\\\", \\\"$type\\\" : \\\"04\\\" }, \\\"local\\\" : { \\\"ip\\\" : \\\"172.30.241.136\\\", \\\"port\\\" : 31519 }, \\\"remote\\\" : { \\\"ip\\\" : \\\"172.30.22.74\\\", \\\"port\\\" : 50034 }, \\\"users\\\" : [ { \\\"user\\\" : \\\"admin\\\", \\\"db\\\" : \\\"admin\\\" } ], \\\"roles\\\" : [ { \\\"role\\\" : \\\"dbAdminAnyDatabase\\\", \\\"db\\\" : \\\"admin\\\" }, { \\\"role\\\" : \\\"readWriteAnyDatabase\\\", \\\"db\\\" : \\\"admin\\\" }, { \\\"role\\\" : \\\"userAdminAnyDatabase\\\", \\\"db\\\" : \\\"admin\\\" } ], \\\"param\\\" : { \\\"ns\\\" : \\\"guardium_dev.testtttt2503_0950\\\", \\\"indexName\\\" : \\\"_id_\\\", \\\"indexSpec\\\" : { \\\"v\\\" : 2, \\\"key\\\" : { \\\"_id\\\" : 1 }, \\\"name\\\" : \\\"_id_\\\" }, \\\"indexBuildState\\\" : \\\"IndexBuildSucceeded\\\" }, \\\"result\\\" : 0 }";
        Context context = new ContextImpl(null, null);
        MongodbGuardiumFilter filter = new MongodbGuardiumFilter("test-id", null, context);

        Event e = new org.logstash.Event();
        TestMatchListener matchListener = new TestMatchListener();

        e.setField("message", messageString);
        ArrayList<Event> events = new ArrayList<>(Arrays.asList(new Event[]{e}));
        Collection<Event> results = filter.filter(events, matchListener);

        Assert.assertEquals(1, results.size());
        Assert.assertEquals(0, matchListener.getMatchCount());
    }
}

