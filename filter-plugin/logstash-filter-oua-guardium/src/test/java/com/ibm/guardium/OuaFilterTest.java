package org.logstashplugins;

//import co.elastic.logstash.api.Configuration;
import co.elastic.logstash.api.Context;
import co.elastic.logstash.api.Event;
import co.elastic.logstash.api.FilterMatchListener;
import com.ibm.guardium.univer***REMOVED***lconnector.commons.structures.Record;
import com.ibm.guardium.univer***REMOVED***lconnector.commons.GuardConstants;
import org.junit.Assert;
import org.junit.Test;
//import org.logstash.plugins.ConfigurationImpl;
import org.logstash.plugins.ContextImpl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.gson.Gson;

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

        Context context = new ContextImpl(null, null);
        OuaFilter filter = new OuaFilter("test-id", null, context);
        TestMatchListener matchListener = new TestMatchListener();
        Event e = new org.logstash.Event();
        
        e.setField("mes***REMOVED***ge", msg);
        e.setField(OuaFilter.SESSION_ID_TAG, session_id);
        e.setField(OuaFilter.DB_NAME_TAG, db_name);
        e.setField(OuaFilter.DB_USER_TAG, db_user);
        e.setField(OuaFilter.OS_USER_TAG, os_user);
        e.setField(OuaFilter.CLIENT_IP_TAG, client_ip);
        e.setField(OuaFilter.SERVER_IP_TAG, server_ip);
        e.setField(OuaFilter.CLIENT_HOST_TAG, client_host);
        e.setField(OuaFilter.SERVER_HOST_TAG, server_host);
        e.setField(OuaFilter.SOURCE_PROGRAM_TAG, source_program);
        e.setField(OuaFilter.TIMESTAMP_TAG, timestamp);
        e.setField(OuaFilter.SQL_TAG, sql_text);

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
