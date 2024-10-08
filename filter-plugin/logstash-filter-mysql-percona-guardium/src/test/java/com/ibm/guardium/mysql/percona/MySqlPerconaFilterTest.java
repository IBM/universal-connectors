package com.ibm.guardium.mysql.percona;

import co.elastic.logstash.api.Configuration;
import co.elastic.logstash.api.Context;
import co.elastic.logstash.api.Event;
import co.elastic.logstash.api.FilterMatchListener;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ibm.guardium.universalconnector.commons.GuardConstants;
import com.ibm.guardium.universalconnector.commons.structures.Record;
import org.junit.Assert;
import org.junit.Test;
import org.logstash.plugins.ConfigurationImpl;
import org.logstash.plugins.ContextImpl;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;

public class MySqlPerconaFilterTest {

    @Test
    public void testParseMySqlPercona_Data(){
        // syslog
        String mysql_message = "<14>Feb 12 07:18:30 dbqa09 percona-audit: {\"audit_record\":{\"name\":\"Query\",\"record\":\"106_1970-01-01T00:00:00\",\"timestamp\":\"2021-02-12T12:18:30 UTC\",\"command_class\":\"select\",\"connection_id\":\"12\",\"status\":0,\"sqltext\":\"select * from Products limit 99999\",\"user\":\"root[root] @ localhost []\",\"host\":\"localhost\",\"os_user\":\"\",\"ip\":\"\",\"db\":\"\"}}";
        Configuration config = new ConfigurationImpl(Collections.singletonMap("log_level", "debug"));
        Context context = new ContextImpl(null, null);
        MySqlPerconaFilter filter = new MySqlPerconaFilter("test-id", config, context);

        Event e = new org.logstash.Event();
        TestMatchListener matchListener = new TestMatchListener();

        e.setField("message", mysql_message);
        Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);

        Assert.assertEquals(1, results.size());
        Assert.assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
        Assert.assertEquals(1, matchListener.getMatchCount());
        System.out.println(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
    }

    @Test
    public void testParseTime(){
        DateTimeFormatterBuilder dateTimeFormatterBuilder = new DateTimeFormatterBuilder()
                //.append(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS[[XXX][X]]"));
                .append(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss z"));
        DateTimeFormatter DATE_TIME_FORMATTER = dateTimeFormatterBuilder.toFormatter();

        String dateString = "2021-02-03T18:55:57 UTC";
        ZonedDateTime date = ZonedDateTime.parse(dateString, DATE_TIME_FORMATTER);
    }

    @Test
    public void testDbNameExtraction(){
        String result = MySqlPerconaFilter.processDbUser(MySqlPerconaFilter.UNKNOWN_STRING);
        Assert.assertEquals("Invalid empty name","",result);

        result = MySqlPerconaFilter.processDbUser("abc[def] @ localhost []");
        Assert.assertEquals("Invalid simple case","abc",result);

        result = MySqlPerconaFilter.processDbUser("abc[def][ghk] @ localhost []");
        Assert.assertEquals("Invalid double[]","abc[def]",result);

        result = MySqlPerconaFilter.processDbUser(" abc [ghk] @ localhost []");
        Assert.assertEquals("Invalid - not trimmed name","abc",result);

    }

    @Test
    public void testParseMySqlPercona_Data1(){
        // filebeat
        String mysql_message = "percona-audit: {\"audit_record\":{\"name\":\"Query\",\n" +
                "\t\t\t\t\t\t\t\t\t\t\t\t   \"record\":\"19775_2021-01-21T17:46:23\",\n" +
                "\t\t\t\t\t\t\t\t\t\t\t\t\"timestamp\":\"2021-01-27T17:46:02 UTC\",\n" +
                "\t\t\t\t\t\t\t\t\t\t\t\t\"command_class\":\"select\",\n" +
                "\t\t\t\t\t\t\t\t\t\t\t\t\"connection_id\":\"11\",\n" +
                "\t\t\t\t\t\t\t\t\t\t\t\t\"status\":0,\n" +
                "\t\t\t\t\t\t\t\t\t\t\t\t\"sqltext\":\"select * from books\",\n" +
                "\t\t\t\t\t\t\t\t\t\t\t\t\"user\":\"rootAAAA[rootKKKKKK] @ localhost []\",\n" +
                "\t\t\t\t\t\t\t\t\t\t\t\t\"host\":\"localhost\",\n" +
                "\t\t\t\t\t\t\t\t\t\t\t\t\"os_user\":\"\",\n" +
                "\t\t\t\t\t\t\t\t\t\t\t\t\"ip\":\"\",\n" +
                "\t\t\t\t\t\t\t\t\t\t\t\t\"db\":\"gdb\"}\n" +
                "\t\t\t\t\t\t\t\t\t}";
        String minOff = "+07:00";


        Configuration config = new ConfigurationImpl(Collections.singletonMap("log_level", "debug"));
        Context context = new ContextImpl(null, null);
        MySqlPerconaFilter filter = new MySqlPerconaFilter("test-id", config, context);

        Event e = new org.logstash.Event();
        TestMatchListener matchListener = new TestMatchListener();

        e.setField("minOff", minOff);
        e.setField("message", mysql_message);
        Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);

        Assert.assertEquals(1, results.size());
        Assert.assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
        Assert.assertEquals(1, matchListener.getMatchCount());
        System.out.println(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
        Record record = new Gson().fromJson((String)e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME), Record.class);
        Assert.assertEquals(record.getDbName(), record.getAccessor().getServiceName());

    }

    @Test
    public void testParseMySqlPercona_Error(){
        String error_message = "percona-audit: {\"audit_record\":{\"name\":\"Query\",\"record\":\"39_2021-02-03T18:54:45\",\"timestamp\":\"2021-02-03T18:55:57 UTC\",\"command_class\":\"select\",\"connection_id\":\"2\",\"status\":1146,\"sqltext\":\"select * from users\",\"user\":\"root[root] @ localhost []\",\"host\":\"localhost\",\"os_user\":\"\",\"ip\":\"\",\"db\":\"mysql\"}}";
        String minOff = "+04:00";

        Configuration config = new ConfigurationImpl(Collections.singletonMap("source", "message"));
        Context context = new ContextImpl(null, null);
        MySqlPerconaFilter filter = new MySqlPerconaFilter("test-id", config, context);

        Event e = new org.logstash.Event();
        TestMatchListener matchListener = new TestMatchListener();

        e.setField("message", error_message);
        e.setField("minOff", minOff);
        Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);

        Assert.assertEquals(1, results.size());
        Assert.assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
        Assert.assertEquals(1, matchListener.getMatchCount());
        System.out.println(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
    }

    @Test
    public void testParseMySqlPercona_AccessDenied(){
        String access_denied = "percona-audit: {\"audit_record\": {\"name\": \"Connect\",\"record\": \"8420_2022-02-10T10:37:59\",\"timestamp\": \"2022-02-10T13:24:28 UTC\",\"connection_id\": \"10\",\"status\": 1045,\"user\": \"root\",\"priv_user\": \"root\",\"os_login\": \"\",\"proxy_user\": \"\",\"host\": \"localhost\",\"ip\": \"\",\"db\": \"\"}}";
        String minOff = "+04:00";

        Configuration config = new ConfigurationImpl(Collections.singletonMap("source", "message"));
        Context context = new ContextImpl(null, null);
        MySqlPerconaFilter filter = new MySqlPerconaFilter("test-id", config, context);

        Event e = new org.logstash.Event();
        TestMatchListener matchListener = new TestMatchListener();

        e.setField("message", access_denied);
        e.setField("minOff", minOff);
        Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
        Assert.assertEquals(1, results.size());
        Assert.assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
        Assert.assertEquals(1, matchListener.getMatchCount());
        System.out.println(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
    }

    @Test
    public void testParseMySqlPercona_Data2(){
        String mysql_message = "<14>Feb 12 07:18:30 dbqa09 percona-audit: {\"audit_record\":{\"name\":\"Query\",\"record\":\"106_1970-01-01T00:00:00\",\"timestamp\":\"2021-02-12T12:18:30Z\",\"command_class\":\"select\",\"connection_id\":\"12\",\"status\":0,\"sqltext\":\"select * from Products limit 99999\",\"user\":\"root[root] @ localhost []\",\"host\":\"localhost\",\"os_user\":\"\",\"ip\":\"\",\"db\":\"\"}}";
        Configuration config = new ConfigurationImpl(Collections.singletonMap("log_level", "debug"));
        Context context = new ContextImpl(null, null);
        MySqlPerconaFilter filter = new MySqlPerconaFilter("test-id", config, context);

        Event e = new org.logstash.Event();
        TestMatchListener matchListener = new TestMatchListener();

        e.setField("message", mysql_message);
        Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);

        Assert.assertEquals(1, results.size());
        Assert.assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
        Assert.assertEquals(1, matchListener.getMatchCount());
        System.out.println(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
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
