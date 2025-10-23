package com.ibm.guardium;

import co.elastic.logstash.api.Context;
import co.elastic.logstash.api.Event;
import co.elastic.logstash.api.FilterMatchListener;
import com.google.gson.Gson;
import com.ibm.guardium.universalconnector.commons.GuardConstants;
import com.ibm.guardium.universalconnector.commons.structures.Record;
import org.junit.Assert;
import org.junit.Test;
import org.logstash.plugins.ContextImpl;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;

public class OuaFilterTest {
    @Test
    public void testParse() {
		final String msg = "{\"sessionid\":373114511,\"proxy_sessionid\":0,\"instance_id\":1,\"instance_name\":\"on8crh7u\",\"authentication_type\":\"(TYPE=(DATABASE));(CLIENT ADDRESS=((ADDRESS=(PROTOCOL=tcp)(HOST=9.70.157.206)(PORT=46511))));\",\"userid\":\"SYSTEM\",\"os_user\":\"oracle18\",\"current_user\":\"SYSTEM\",\"client_host_ip\":\"9.70.157.206\",\"client_host_name\":\"rh7u1x64t-ktap.guard.swg.usma.ibm.com\",\"server_host_ip\":\"9.70.157.206\",\"server_host_name\":\"rh7u1x64t-ktap.guard.swg.usma.ibm.com\",\"client_program_name\":\"sqlplus@rh7u1x64t-ktap.guard.swg.usma.ibm.com (\",\"terminal\":\"pts/1\",\"os_process\":\"15767\",\"dbid\":2287733986,\"dbname\":\"ON8CRH7U\",\"statement_id\":14,\"return_code\":0,\"server_nls_characterset_id\":873,\"server_nls_nchar_characterset_id\":2000,\"event_timestamp\":\"+000017794 22:56:57.111795\",\"sql_text\":\"  alter user QA_TEST default role ALL\"}";
		final long session_id = 373114511;
		final String db_name = "ON8CRH7U";
		final String db_user = "SYSTEM";
		final String os_user = "oracle18";
		final String client_ip = "9.70.157.206";
		final String server_ip = "9.70.157.206";
		final String client_host = "rh7u1x64t-ktap.guard.swg.usma.ibm.com";
		final String server_host = "rh7u1x64t-ktap.guard.swg.usma.ibm.com";
		final String source_program = "sqlplus@rh7u1x64t-ktap.guard.swg.usma.ibm.com (";
		final String timestamp = "+000017794 22:56:57.111795";
		final String sql_text = "  alter user QA_TEST default role ALL";
        final int server_port = 1521;
        final int return_code =0;



        Context context = new ContextImpl(null, null);
        OuaFilter filter = new OuaFilter("test-id", null, context);
        TestMatchListener matchListener = new TestMatchListener();
        Event e = new org.logstash.Event();
        
        e.setField(OuaFilter.MESSAGE, msg);
        e.setField(OuaFilter.SESSION_ID_TAG, session_id);
        e.setField(OuaFilter.DB_NAME_TAG, db_name);
        e.setField(OuaFilter.DB_USER_TAG, db_user);
        e.setField(OuaFilter.OS_USER_TAG, os_user);
        e.setField(OuaFilter.CLIENT_IP_TAG, client_ip);
        e.setField(OuaFilter.SERVER_IP_TAG, server_ip);
        e.setField(OuaFilter.CLIENT_HOST_TAG, client_host);
        e.setField(OuaFilter.SERVER_HOST_TAG, server_host);
        e.setField(OuaFilter.SERVER_HOST_PORT, server_port);
        e.setField(OuaFilter.SOURCE_PROGRAM_TAG, source_program);
        e.setField(OuaFilter.TIMESTAMP_TAG, timestamp);
        e.setField(OuaFilter.SQL_TAG, sql_text);
        e.setField(OuaFilter.RETURN_CODE_TAG,return_code);

        Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);

        Assert.assertEquals(1, results.size());
        Assert.assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));

        Record record = new Gson().fromJson((String)e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME), Record.class);
        Assert.assertEquals(record.getDbName(), record.getAccessor().getServiceName());
        
        Assert.assertEquals(1, matchListener.getMatchCount());
        Assert.assertEquals("ON8CRH7U", record.getDbName());
        Assert.assertEquals("ON8CRH7U", record.getAccessor().getServiceName());

    }

    @Test
    public void testParseCloud() {
        final String msg = "{\"sessionid\":373114511,\"proxy_sessionid\":0,\"instance_id\":1,\"instance_name\":\"on8crh7u\",\"authentication_type\":\"(TYPE=(DATABASE));(CLIENT ADDRESS=((ADDRESS=(PROTOCOL=tcp)(HOST=9.70.157.206)(PORT=46511))));\",\"userid\":\"SYSTEM\",\"os_user\":\"oracle18\",\"current_user\":\"SYSTEM\",\"client_host_ip\":\"9.70.157.206\",\"client_host_name\":\"rh7u1x64t-ktap.guard.swg.usma.ibm.com\",\"server_host_ip\":\"9.70.157.206\",\"server_host_name\":\"rh7u1x64t-ktap.guard.swg.usma.ibm.com\",\"client_program_name\":\"sqlplus@rh7u1x64t-ktap.guard.swg.usma.ibm.com (\",\"terminal\":\"pts/1\",\"os_process\":\"15767\",\"dbid\":2287733986,\"dbname\":\"testdb\",\"statement_id\":14,\"return_code\":0,\"server_nls_characterset_id\":873,\"server_nls_nchar_characterset_id\":2000,\"event_timestamp\":\"+000017794 22:56:57.111795\",\"sql_text\":\"  alter user QA_TEST default role ALL\"}";

        final long session_id = 373114511;
        final String db_name = "testdb";
        final String db_user = "SYSTEM";
        final String os_user = "oracle18";
        final String client_ip = "9.70.157.206";
        final String server_ip = "9.70.157.206";
        final String client_host = "rh7u1x64t-ktap.guard.swg.usma.ibm.com";
        final String server_host = "rh7u1x64t-ktap.guard.swg.usma.ibm.com";
        final String source_program = "sqlplus@rh7u1x64t-ktap.guard.swg.usma.ibm.com (";
        final String timestamp = "+000017794 22:56:57.111795";
        final String sql_text = "  alter user QA_TEST default role ALL";
        final int server_port = 1521;
        final String account_id = "346824953529";
        final int return_code =0;



        Context context = new ContextImpl(null, null);
        OuaFilter filter = new OuaFilter("test-id", null, context);
        TestMatchListener matchListener = new TestMatchListener();
        Event e = new org.logstash.Event();

        e.setField("message", msg);
        e.setField(OuaFilter.SESSION_ID_TAG, session_id);
        e.setField(OuaFilter.DB_NAME_TAG, db_name);
        e.setField(OuaFilter.DB_USER_TAG, db_user);
        e.setField(OuaFilter.OS_USER_TAG, os_user);
        e.setField(OuaFilter.CLIENT_IP_TAG, client_ip);
        e.setField(OuaFilter.SERVER_IP_TAG, server_ip);
        e.setField(OuaFilter.CLIENT_HOST_TAG, client_host);
        e.setField(OuaFilter.SERVER_HOST_TAG, server_host);
        e.setField(OuaFilter.SERVER_HOST_PORT, server_port);
        e.setField(OuaFilter.SOURCE_PROGRAM_TAG, source_program);
        e.setField(OuaFilter.TIMESTAMP_TAG, timestamp);
        e.setField(OuaFilter.SQL_TAG, sql_text);
        e.setField(OuaFilter.SERVER_ACCOUNT_ID,account_id);
        e.setField(OuaFilter.RETURN_CODE_TAG,return_code);

        Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);

        Assert.assertEquals(1, results.size());
        Assert.assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));

        Record record = new Gson().fromJson((String)e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME), Record.class);
        Assert.assertEquals(record.getDbName(), record.getAccessor().getServiceName());

        Assert.assertEquals(1, matchListener.getMatchCount());
        Assert.assertEquals("346824953529:testdb", record.getDbName());
        Assert.assertEquals("346824953529:testdb", record.getAccessor().getServiceName());

    }
    @Test
    public void testParseJdbcConnect() {
        final String msg = "{\"payload\":{\"CLIENT_HOST_IP\":\"9.147.163.45\",\"CLIENT_HOST_NAME\":\"dors-mbp.givatayim.il.ibm.com\",\"CLIENT_PROGRAM_NAME\":\"DBeaver 24?1?0 ? SQLEditor ?Script?16?sql?\",\"CON_NAME\":\"ON9PRH8X\",\"DBID\":\"1440598309\",\"DBNAME\":\"ON9CRH8X\",\"EVENT_TIMESTAMP\":\"+000019898 12:18:46.755743\",\"EVENT_TIMESTAMP_UTC\":1719231526755,\"OS_USER\":\"il017920\",\"RETURN_CODE\":\"0\",\"SERVER_HOST_IP\":\"9.46.226.158\",\"SESSIONID\":\"782118933\",\"SQL_TEXT\":\"INSERT INTO DOR_TEST VALUES ('97', 'aa', 'aa', 'aa', 'aa')\\u0000\",\"USERID\":\"SCOTT\"},\"schema\":{\"fields\":[{\"field\":\"EVENT_TIMESTAMP_UTC\",\"name\":\"org.apache.kafka.connect.data.Timestamp\",\"optional\":true,\"type\":\"int64\",\"version\":1},{\"field\":\"EVENT_TIMESTAMP\",\"optional\":true,\"type\":\"string\"},{\"field\":\"SESSIONID\",\"optional\":true,\"type\":\"string\"},{\"field\":\"USERID\",\"optional\":true,\"type\":\"string\"},{\"field\":\"CON_NAME\",\"optional\":true,\"type\":\"string\"},{\"field\":\"OS_USER\",\"optional\":true,\"type\":\"string\"},{\"field\":\"CLIENT_HOST_IP\",\"optional\":true,\"type\":\"string\"},{\"field\":\"CLIENT_HOST_NAME\",\"optional\":true,\"type\":\"string\"},{\"field\":\"SERVER_HOST_IP\",\"optional\":true,\"type\":\"string\"},{\"field\":\"CLIENT_PROGRAM_NAME\",\"optional\":true,\"type\":\"string\"},{\"field\":\"DBID\",\"optional\":true,\"type\":\"string\"},{\"field\":\"DBNAME\",\"optional\":true,\"type\":\"string\"},{\"field\":\"RETURN_CODE\",\"optional\":true,\"type\":\"string\"},{\"field\":\"SQL_TEXT\",\"optional\":true,\"type\":\"string\"}],\"optional\":false,\"type\":\"struct\"}}";

        Context context = new ContextImpl(null, null);
        OuaFilter filter = new OuaFilter("test-id", null, context);
        TestMatchListener matchListener = new TestMatchListener();
        Event e = new org.logstash.Event();

        e.setField(OuaFilter.SERVER_ADDRESS_TAG, "auto-oua-01.cxmxwdil903v.us-west-1.rds.amazonaws.com");
        e.setField(OuaFilter.SERVER_HOST_PORT, 1521L);
        e.setField("message", msg);

        Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);

        Assert.assertEquals(1, results.size());
        Assert.assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));

        Record record = new Gson().fromJson((String)e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME), Record.class);
        Assert.assertEquals(record.getDbName(), record.getAccessor().getServiceName());

        // verify \u0000 removed from sql text
        Assert.assertEquals("INSERT INTO DOR_TEST VALUES ('97', 'aa', 'aa', 'aa', 'aa')", record.getData().getOriginalSqlCommand());

        //verify time
        Assert.assertEquals(1719231526755L, record.getTime().getTimstamp());

        Assert.assertEquals(1521L, e.getField(OuaFilter.SERVER_HOST_PORT));
        Assert.assertEquals("auto-oua-01.cxmxwdil903v.us-west-1.rds.amazonaws.com", e.getField(OuaFilter.SERVER_HOST_TAG));

        Assert.assertEquals(1, matchListener.getMatchCount());
        Assert.assertEquals("ON9CRH8X", record.getDbName());
        Assert.assertEquals("ON9CRH8X", record.getAccessor().getServiceName());
    }

    @Test
    public void testParseJdbcConnect_userid_null() {
        final String msg = "{\"payload\":{\"CLIENT_HOST_IP\":\"9.147.163.45\",\"CLIENT_HOST_NAME\":\"dors-mbp.givatayim.il.ibm.com\",\"CLIENT_PROGRAM_NAME\":\"DBeaver 24?1?0 ? SQLEditor ?Script?16?sql?\",\"CON_NAME\":\"ON9PRH8X\",\"DBID\":\"1440598309\",\"DBNAME\":\"ON9CRH8X\",\"EVENT_TIMESTAMP\":\"+000019898 12:18:46.755743\",\"EVENT_TIMESTAMP_UTC\":1719231526755,\"OS_USER\":\"il017920\",\"RETURN_CODE\":\"0\",\"SERVER_HOST_IP\":\"9.46.226.158\",\"SESSIONID\":\"782118933\",\"SQL_TEXT\":\"INSERT INTO DOR_TEST VALUES ('97', 'aa', 'aa', 'aa', 'aa')\\u0000\",\"USERID\":null},\"schema\":{\"fields\":[{\"field\":\"EVENT_TIMESTAMP_UTC\",\"name\":\"org.apache.kafka.connect.data.Timestamp\",\"optional\":true,\"type\":\"int64\",\"version\":1},{\"field\":\"EVENT_TIMESTAMP\",\"optional\":true,\"type\":\"string\"},{\"field\":\"SESSIONID\",\"optional\":true,\"type\":\"string\"},{\"field\":\"USERID\",\"optional\":true,\"type\":\"string\"},{\"field\":\"CON_NAME\",\"optional\":true,\"type\":\"string\"},{\"field\":\"OS_USER\",\"optional\":true,\"type\":\"string\"},{\"field\":\"CLIENT_HOST_IP\",\"optional\":true,\"type\":\"string\"},{\"field\":\"CLIENT_HOST_NAME\",\"optional\":true,\"type\":\"string\"},{\"field\":\"SERVER_HOST_IP\",\"optional\":true,\"type\":\"string\"},{\"field\":\"CLIENT_PROGRAM_NAME\",\"optional\":true,\"type\":\"string\"},{\"field\":\"DBID\",\"optional\":true,\"type\":\"string\"},{\"field\":\"DBNAME\",\"optional\":true,\"type\":\"string\"},{\"field\":\"RETURN_CODE\",\"optional\":true,\"type\":\"string\"},{\"field\":\"SQL_TEXT\",\"optional\":true,\"type\":\"string\"}],\"optional\":false,\"type\":\"struct\"}}";

        Context context = new ContextImpl(null, null);
        OuaFilter filter = new OuaFilter("test-id", null, context);
        TestMatchListener matchListener = new TestMatchListener();
        Event e = new org.logstash.Event();

        // init
        e.setField(OuaFilter.SERVER_ADDRESS_TAG, "auto-oua-01.cxmxwdil903v.us-west-1.rds.amazonaws.com");
        e.setField(OuaFilter.SERVER_HOST_PORT, 1521L);
        e.setField("message", msg);

        // action
        Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);

        Assert.assertEquals(1, results.size());
        Assert.assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));

        Record record = new Gson().fromJson((String)e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME), Record.class);
        Assert.assertEquals(record.getDbName(), record.getAccessor().getServiceName());

        // result
        Assert.assertEquals("",record.getAccessor().getDbUser()); // verify jsonNull not throw exception
    }

    @Test
    public void testParseJdbcConnectNoSchema() {
        String msg ="{\"EVENT_TIMESTAMP_UTC\":1733051171821,\"SESSIONID\":\"1276239794\",\"USERID\":\"SCOTT\",\"CON_NAME\":\"ON9PDBQA\",\"OS_USER\":\"il017920\",\"CLIENT_HOST_IP\":\"9.147.173.67\",\"CLIENT_HOST_NAME\":\"dors-mbp.givatayim.il.ibm.com\",\"SERVER_HOST_IP\":\"9.46.92.230\",\"CLIENT_PROGRAM_NAME\":\"SQL Developer\",\"DBID\":\"2505005351\",\"DBNAME\":\"ON9CDBQA\",\"RETURN_CODE\":\"0\",\"SQL_TEXT\":\"insert into persons values ('first3', 'last3')\\u0000\"}";

        Context context = new ContextImpl(null, null);
        OuaFilter filter = new OuaFilter("test-id", null, context);
        TestMatchListener matchListener = new TestMatchListener();
        Event e = new org.logstash.Event();

        e.setField(OuaFilter.SERVER_ADDRESS_TAG, "uc.test.demo-server.ibm.com");
        e.setField(OuaFilter.SERVER_HOST_PORT, 1521L);
        e.setField("message", msg);
        e.setField("source_system", "kafka-connect");

        Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);

        Assert.assertEquals(1, results.size());
        Assert.assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));

        Record record = new Gson().fromJson((String)e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME), Record.class);
        Assert.assertEquals(record.getDbName(), record.getAccessor().getServiceName());

        // verify \u0000 removed from sql text
        Assert.assertEquals("insert into persons values ('first3', 'last3')", record.getData().getOriginalSqlCommand());

        //verify time
        Assert.assertEquals(1733051171821L, record.getTime().getTimstamp());

        Assert.assertEquals(1521L, e.getField(OuaFilter.SERVER_HOST_PORT));
        Assert.assertEquals("uc.test.demo-server.ibm.com", e.getField(OuaFilter.SERVER_HOST_TAG));

        Assert.assertEquals(1, matchListener.getMatchCount());
        Assert.assertEquals("ON9CDBQA", record.getDbName());
        Assert.assertEquals("ON9CDBQA", record.getAccessor().getServiceName());
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
