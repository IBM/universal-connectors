package org.logstashplugins;

import co.elastic.logstash.api.Configuration;
import co.elastic.logstash.api.Context;
import co.elastic.logstash.api.Event;
import co.elastic.logstash.api.FilterMatchListener;
import org.junit.Assert;
import org.junit.Test;
import org.logstash.plugins.ConfigurationImpl;
import org.logstash.plugins.ContextImpl;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;
import com.ibm.guardium.universalconnector.commons.GuardConstants;



public class MySqlFilterGuardiumTest {

    @Test
    public void testParseMySqlSyslog() {
        final String mysql_message = "mysql_audit_log: { \"timestamp\": \"2020-08-26 17:31:22\", \"id\": 1, \"class\": \"general\", \"event\": \"status\", \"connection_id\": 42, \"account\": { \"user\": \"guardium_qa\", \"host\": \"\" }, \"login\": { \"user\": \"guardium_qa\", \"os\": \"\", \"ip\": \"9.80.196.128\", \"proxy\": \"\" }, \"general_data\": { \"command\": \"Query\", \"sql_command\": \"select\", \"query\": \"select * from pet\nLIMIT 0, 200\", \"status\": 0 } },";
        Configuration config = new ConfigurationImpl(Collections.singletonMap("source", "message"));
        Context context = new ContextImpl(null, null);
        MySqlFilterGuardium filter = new MySqlFilterGuardium("test-id", config, context);

        Event e = new org.logstash.Event();
        TestMatchListener matchListener = new TestMatchListener();

        e.setField("message", mysql_message);
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
