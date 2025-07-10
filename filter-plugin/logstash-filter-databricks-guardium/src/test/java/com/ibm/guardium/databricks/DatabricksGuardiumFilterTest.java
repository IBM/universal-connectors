package com.ibm.guardium.databricks;

import co.elastic.logstash.api.Event;
import co.elastic.logstash.api.FilterMatchListener;
import co.elastic.logstash.api.Context;
import org.logstash.plugins.ContextImpl;

import com.ibm.guardium.universalconnector.commons.GuardConstants;

import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class DatabricksGuardiumFilterTest {

    Context context = new ContextImpl(null, null);
    DatabricksGuardiumFilter filter = new DatabricksGuardiumFilter("test-id", null, context);

    @Test
    public void testAccounts() {
        final String DatabricksString = "{ \"resourceId\": \"/SUBSCRIPTIONS/5C0C81D4-656F-415D-8599-DCD86F2F665E/RESOURCEGROUPS/DATABRICKSTEST/PROVIDERS/MICROSOFT.DATABRICKS/WORKSPACES/DATABRICK-TEST\", \"operationVersion\": \"1.0.0\", \"identity\": \"{\\\"email\\\":\\\"abc.h@abc.com\\\",\\\"subjectName\\\":null}\", \"operationName\": \"Microsoft.Databricks/accounts/tokenLogin\", \"time\": \"2025-05-01T13:43:28Z\", \"category\": \"accounts\", \"properties\": {\"sourceIPAddress\":\"20.193.136.102\",\"logId\":\"87e1a69e-444a-434e-a0be-648b1797e6d9\",\"serviceName\":\"accounts\",\"userAgent\":\"Apache-HttpClient/4.5.14 (Java/17.0.13) Databricks-Service/driver DBHttpClient/v2RawClient\",\"response\":\"{\\\"statusCode\\\":200}\",\"sessionId\":null,\"actionName\":\"tokenLogin\",\"requestId\":\"0018d88c-2f52-4cf0-86a4-8d1dc416ab10\",\"requestParams\":\"{\\\"user\\\":\\\"abc@abc.com\\\",\\\"tokenId\\\":\\\"kfhjgjfhdgjkdh39284783297423943hejhfkdsfh39\\\",\\\"authenticationMethod\\\":\\\"API_INT_PAT_TOKEN\\\"}\"}, \"Host\": \"1234-123456-ab1c2d3e-12-123-12-1\"}";
        Event e = new org.logstash.Event();
        TestMatchListener matchListener = new TestMatchListener();
        e.setField("message", DatabricksString);
        Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
        assertEquals(1, results.size());
        assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
        assertEquals(1, matchListener.getMatchCount());
    }

    @Test
    public void testEmpty() {
        final String DatabricksString = "";
        Event e = new org.logstash.Event();
        TestMatchListener matchListener = new TestMatchListener();
        e.setField("message", DatabricksString);
        Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
        assertEquals(1, results.size());
    }

    @Test
    public void testMessage() {
        final String DatabricksString = "{ \"resourceId\": \"/SUBSCRIPTIONS/5C0C81D4-656F-415D-8599-DCD86F2F665E/RESOURCEGROUPS/DATABRICKSTEST/PROVIDERS/MICROSOFT.DATABRICKS/WORKSPACES/DATABRICK-TEST\", \"operationVersion\": \"1.0.0\", \"identity\": \"{\\\"email\\\":\\\"abc.h@abc.com\\\",\\\"subjectName\\\":null}\", \"operationName\": \"Microsoft.Databricks/accounts/tokenLogin\", \"time\": \"2025-05-01T13:43:28Z\", \"category\": \"accounts\", \"properties\": {\"sourceIPAddress\":\"20.193.136.102\",\"logId\":\"87e1a69e-444a-434e-a0be-648b1797e6d9\",\"serviceName\":\"accounts\",\"userAgent\":\"Apache-HttpClient/4.5.14 (Java/17.0.13) Databricks-Service/driver DBHttpClient/v2RawClient\",\"response\":\"{\\\"statusCode\\\":200}\",\"sessionId\":null,\"actionName\":\"tokenLogin\",\"requestId\":\"0018d88c-2f52-4cf0-86a4-8d1dc416ab10\",\"requestParams\":\"{\\\"user\\\":\\\"abc@abc.com\\\",\\\"tokenId\\\":\\\"kfhjgjfhdgjkdh39284783297423943hejhfkdsfh39\\\",\\\"authenticationMethod\\\":\\\"API_INT_PAT_TOKEN\\\"}\"}, \"Host\": \"1234-123456-ab1c2d3e-12-123-12-1\"}";
        Event e = new org.logstash.Event();
        TestMatchListener matchListener = new TestMatchListener();
        e.setField("message", DatabricksString);
        Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
        assertEquals(1, results.size());
        assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
        assertEquals(1, matchListener.getMatchCount());
    }

    @Test
    public void testCategory() {
        final String DatabricksString = "{ \"resourceId\": \"/SUBSCRIPTIONS/5C0C81D4-656F-415D-8599-DCD86F2F665E/RESOURCEGROUPS/DATABRICKSTEST/PROVIDERS/MICROSOFT.DATABRICKS/WORKSPACES/DATABRICK-TEST\", \"operationVersion\": \"1.0.0\", \"identity\": \"{\\\"email\\\":\\\"abc.h@abc.com\\\",\\\"subjectName\\\":null}\", \"operationName\": \"Microsoft.Databricks/accounts/tokenLogin\", \"time\": \"2025-05-01T13:43:28Z\", \"category\": \"accounts\", \"properties\": {\"sourceIPAddress\":\"20.193.136.102\",\"logId\":\"87e1a69e-444a-434e-a0be-648b1797e6d9\",\"serviceName\":\"accounts\",\"userAgent\":\"Apache-HttpClient/4.5.14 (Java/17.0.13) Databricks-Service/driver DBHttpClient/v2RawClient\",\"response\":\"{\\\"statusCode\\\":200}\",\"sessionId\":null,\"actionName\":\"tokenLogin\",\"requestId\":\"0018d88c-2f52-4cf0-86a4-8d1dc416ab10\",\"requestParams\":\"{\\\"user\\\":\\\"abc@abc.com\\\",\\\"tokenId\\\":\\\"kfhjgjfhdgjkdh39284783297423943hejhfkdsfh39\\\",\\\"authenticationMethod\\\":\\\"API_INT_PAT_TOKEN\\\"}\"}, \"Host\": \"1234-123456-ab1c2d3e-12-123-12-1\"}";
        Event e = new org.logstash.Event();
        TestMatchListener matchListener = new TestMatchListener();
        e.setField("message", DatabricksString);
        Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
        assertEquals(1, results.size());
        assertNotNull(e.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
        assertEquals(1, matchListener.getMatchCount());
    }

    @Test
    public void testInvalidRecords() {
        final String DatabricksString = "{\"records\":\"\"}";
        Event e = new org.logstash.Event();
        TestMatchListener matchListener = new TestMatchListener();
        e.setField("message", DatabricksString);
        Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);
        assertEquals(1, results.size());
    }

    @Test
    public void getIdTest() {
        final String DatabricksString = "";
        Event e = new org.logstash.Event();
        e.setField("message", DatabricksString);
        String id = filter.getId();
    }

}

class TestMatchListener implements FilterMatchListener {
    private AtomicInteger matchCount = new AtomicInteger(0);

    public int getMatchCount() {
        return matchCount.get();
    }

    @Override
    public void filterMatched(co.elastic.logstash.api.Event arg0) {
        matchCount.incrementAndGet();

    }
}
