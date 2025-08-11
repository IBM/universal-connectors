package com.ibm.guardium.aws.opensearch;

import co.elastic.logstash.api.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibm.guardium.universalconnector.commons.custom_parsing.ParserFactory;
import com.ibm.guardium.universalconnector.commons.structures.Time;
import org.junit.Before;
import org.junit.jupiter.api.Test;
import org.logstash.plugins.ConfigurationImpl;
import org.logstash.plugins.ContextImpl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class OpensearchGuardiumFilterTest {

    FilterMatchListener matchListener = new TestMatchListener();

    String id = "1";
    Configuration config = new ConfigurationImpl(Collections.singletonMap("source", ""));
    Context context = new ContextImpl(null, null);
    OpensearchGuardiumFilter filter = new OpensearchGuardiumFilter(id, config, context);

    private Parser parser;
    private ObjectMapper objectMapper;

    @Before
    public void setUp() throws IOException {
        parser = new Parser(ParserFactory.ParserType.json);
        objectMapper = new ObjectMapper();
    }

    @Test
    void testRestLayer() {
        String payload = "{\n" +
                "    \"audit_cluster_name\": \"1245451225:myopensearchpk\",\n" +
                "    \"audit_transport_headers\": {\n" +
                "        \"X-Opaque-Id\": \"d6c4f099-4663-4633-baef-98eb0581f020\"\n" +
                "    },\n" +
                "    \"audit_node_name\": \"3c5bbacf33948a9a2c26426eb5a55c63\",\n" +
                "    \"audit_trace_task_id\": \"4dW-p_qGTr6MT1q9RqXThA:2669096\",\n" +
                "    \"audit_transport_request_type\": \"GetMappingsRequest\",\n" +
                "    \"audit_category\": \"INDEX_EVENT\",\n" +
                "    \"audit_request_origin\": \"REST\",\n" +
                "    \"audit_node_id\": \"4dW-p_qGTr6MT1q9RqXThA\",\n" +
                "    \"audit_request_layer\": \"TRANSPORT\",\n" +
                "    \"@timestamp\": \"2025-04-21T17:32:22.227+00:00\",\n" +
                "    \"audit_format_version\": 4,\n" +
                "    \"audit_request_remote_address\": \"216.58.113.178\",\n" +
                "    \"audit_request_privilege\": \"indices:admin/mappings/get\",\n" +
                "    \"audit_request_effective_user\": \"userpk\",\n" +
                "    \"audit_trace_resolved_indices\": [\n" +
                "        \".kibana_1\",\n" +
                "        \"school\",\n" +
                "        \".opendistro_security\",\n" +
                "        \"test_index\",\n" +
                "        \".opensearch-observability\"\n" +
                "    ]\n" +
                "}";
        Event event = new org.logstash.Event();
        event.setField("message", payload);
        Collection<Event> actualResponse = filter.filter(Collections.singletonList(event), matchListener);

        assertNotNull(actualResponse.toArray(new Event[0])[0].getField("GuardRecord"));
    }

    @Test
    void testArrayValuesFromIndex() {
        String payload = "{\n" +
                "            \"audit_compliance_operation\": \"CREATE\",\n" +
                "            \"audit_cluster_name\": \"1245451225:myopensearchpk\",\n" +
                "            \"audit_node_name\": \"076ba9bbd1cfeb6c80e1d15a405869ed\",\n" +
                "            \"audit_category\": \"COMPLIANCE_DOC_WRITE\",\n" +
                "            \"audit_request_origin\": \"REST\",\n" +
                "            \"audit_compliance_doc_version\": 1,\n" +
                "            \"audit_request_body\": \"{\\n  \\\"student_id\\\": \\\"101\\\",\\n  \\\"name\\\": \\\"John Doe\\\",\\n  \\\"age\\\": 15,\\n  \\\"grade\\\": \\\"10th\\\",\\n  \\\"subjects\\\": [\\\"Math\\\", \\\"Science\\\", \\\"English\\\"]\\n}\\n\",\n" +
                "            \"audit_node_id\": \"rgz9dKmNT9C9a1iSBM8yjg\",\n" +
                "            \"@timestamp\": \"2025-03-06T18:43:13.968+00:00\",\n" +
                "            \"audit_format_version\": 4,\n" +
                "            \"audit_request_remote_address\": \"69.171.141.155\",\n" +
                "            \"audit_trace_doc_id\": \"1\",\n" +
                "            \"audit_request_effective_user\": \"admin\",\n" +
                "            \"audit_trace_shard_id\": 0,\n" +
                "            \"audit_trace_indices\": [\n" +
                "                \"school-2025\"\n" +
                "            ],\n" +
                "            \"audit_trace_resolved_indices\": [\n" +
                "                \"school-2025\"\n" +
                "            ]\n" +
                "        }";
        Event event = new org.logstash.Event();
        event.setField("message", payload);
        Collection<Event> actualResponse = filter.filter(Collections.singletonList(event), matchListener);

        assertNotNull(actualResponse.toArray(new Event[0])[0].getField("GuardRecord"));
    }

    @Test
    void testFailedLogin() {
        String payload = "{\n" +
                "    \"audit_cluster_name\": \"1245451225:myopensearchpk\",\n" +
                "    \"audit_node_name\": \"3c5bbacf33948a9a2c26426eb5a55c63\",\n" +
                "    \"audit_rest_request_method\": \"GET\",\n" +
                "    \"audit_category\": \"FAILED_LOGIN\",\n" +
                "    \"audit_request_origin\": \"REST\",\n" +
                "    \"audit_node_id\": \"4dW-p_qGTr6MT1q9RqXThA\",\n" +
                "    \"audit_request_layer\": \"REST\",\n" +
                "    \"audit_rest_request_path\": \"/_plugins/_security/authinfo\",\n" +
                "    \"@timestamp\": \"2025-04-22T15:10:29.763+00:00\",\n" +
                "    \"audit_request_effective_user_is_admin\": false,\n" +
                "    \"audit_format_version\": 4,\n" +
                "    \"audit_request_remote_address\": \"216.58.113.178\",\n" +
                "    \"audit_rest_request_headers\": {\n" +
                "        \"x-opensearch-product-origin\": [\n" +
                "            \"opensearch-dashboards\"\n" +
                "        ],\n" +
                "        \"Connection\": [\n" +
                "            \"keep-alive\"\n" +
                "        ],\n" +
                "        \"x-opaque-id\": [\n" +
                "            \"56e3fd95-d79f-4077-a674-85fa22fed9e9\"\n" +
                "        ],\n" +
                "        \"Host\": [\n" +
                "            \"localhost:9200\"\n" +
                "        ],\n" +
                "        \"Content-Length\": [\n" +
                "            \"0\"\n" +
                "        ],\n" +
                "        \"NO_REDACT\": [\n" +
                "            \"false\"\n" +
                "        ]\n" +
                "    },\n" +
                "    \"audit_request_effective_user\": \"userpk\"\n" +
                "}";
        Event event = new org.logstash.Event();
        event.setField("message", payload);
        Collection<Event> actualResponse = filter.filter(Collections.singletonList(event), matchListener);

        assertNotNull(actualResponse.toArray(new Event[0])[0].getField("GuardRecord"));
    }

    @Test
    void testBadHeader() {
        String payload = "{\n" +
                "            \"audit_cluster_name\": \"1245451225:myopensearchpk\",\n" +
                "            \"audit_rest_request_params\": {\n" +
                "                \"t\": \"1\",\n" +
                "                \"index\": \"teorema505\"\n" +
                "            },\n" +
                "            \"audit_node_name\": \"076ba9bbd1eb6c80e1d15a405869ed\",\n" +
                "            \"audit_rest_request_method\": \"GET\",\n" +
                "            \"audit_category\": \"BAD_HEADERS\",\n" +
                "            \"audit_request_origin\": \"REST\",\n" +
                "            \"audit_node_id\": \"rgz9dKmNT9C91iSBM8yjg\",\n" +
                "            \"audit_request_layer\": \"REST\",\n" +
                "            \"audit_rest_request_path\": \"/teorema505\",\n" +
                "            \"@timestamp\": \"2025-03-06T18:39:54.550+00:00\",\n" +
                "            \"audit_request_effective_user_is_admin\": false,\n" +
                "            \"audit_format_version\": 4,\n" +
                "            \"audit_request_remote_address\": \"161.35.66.151\",\n" +
                "            \"audit_rest_request_headers\": {\n" +
                "                \"content-length\": [\n" +
                "                    \"0\"\n" +
                "                ],\n" +
                "                \"NO_REDACT\": [\n" +
                "                    \"false\"\n" +
                "                ],\n" +
                "                \"user-agent\": [\n" +
                "                    \"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/108.0.0.0 Safari/537.36\"\n" +
                "                ],\n" +
                "                \"accept\": [\n" +
                "                    \"*/*\"\n" +
                "                ]\n" +
                "            },\n" +
                "            \"audit_request_effective_user\": \"<NONE>\"\n" +
                "        }";
        Event event = new org.logstash.Event();
        event.setField("message", payload);
        Collection<Event> actualResponse = filter.filter(Collections.singletonList(event), matchListener);

        assertNotNull(actualResponse.toArray(new Event[0])[0].getField("GuardRecord"));
    }

    @Test
    void testTransportLayer() {
        String payload = "{\n" +
                "            \"audit_cluster_name\": \"1245451225:myopensearchpk\",\n" +
                "            \"audit_transport_headers\": {\n" +
                "                \"X-Opaque-Id\": \"f0075eb2-569f-4fd3-bfuu-2219571dfd9b\"\n" +
                "            },\n" +
                "            \"audit_node_name\": \"076ba9bbd1cfeb6c1d15a405869ed\",\n" +
                "            \"audit_trace_task_id\": \"rgz9dKmNT9C9a1iSBM8yjg:34312\",\n" +
                "            \"audit_transport_request_type\": \"GetAliasesRequest\",\n" +
                "            \"audit_category\": \"INDEX_EVENT\",\n" +
                "            \"audit_request_origin\": \"REST\",\n" +
                "            \"audit_node_id\": \"rgz9dKmNTC9a1iSBM8yjg\",\n" +
                "            \"audit_request_layer\": \"TRANSPORT\",\n" +
                "            \"@timestamp\": \"2025-03-06T16:56:39.103+00:00\",\n" +
                "            \"audit_format_version\": 4,\n" +
                "            \"audit_request_remote_address\": \"69.171.141.155\",\n" +
                "            \"audit_request_privilege\": \"indices:admin/aliases/get\",\n" +
                "            \"audit_request_effective_user\": \"admin\",\n" +
                "            \"audit_trace_resolved_indices\": [\n" +
                "                \".opendistro-reports-instances\",\n" +
                "                \".ql-datasources\",\n" +
                "                \".opendistro_security\",\n" +
                "                \".plugins-ml-config\",\n" +
                "                \"opensearch_dashboards_sample_data_flights\",\n" +
                "                \"school-2025\",\n" +
                "                \".opendistro-reports-definitions\",\n" +
                "                \".kibana_92668751_admin_1\",\n" +
                "                \".opensearch-observability\",\n" +
                "                \"opensearch_dashboards_sample_data_logs\",\n" +
                "                \".kibana_1\"\n" +
                "            ]\n" +
                "        }";
        Event event = new org.logstash.Event();
        event.setField("message", payload);
        Collection<Event> actualResponse = filter.filter(Collections.singletonList(event), matchListener);

        assertNotNull(actualResponse.toArray(new Event[0])[0].getField("GuardRecord"));
    }

    @Test
    void testInvalidJsonHandling() {
        // Arrange
        String invalidJsonPayload = "{ \"@timestamp\": \"2025-03-06 15:39:02.724\", \"@message\": { \"audit_cluster_name\": \"346824953529:myopensearchpk\" ";
        Event event = new org.logstash.Event();
        event.setField("message", invalidJsonPayload);

        List<Event> events = new ArrayList<>();
        events.add(event);

        Collection<Event> actualResponse = filter.filter(events, matchListener);

        assertEquals(0, actualResponse.size());
    }

    @Test
    void testParse() {
        String payload = "{ \"field1\": \"value1\", \"field2\": [\"value2\", \"value3\"] }";
        Parser parser = new Parser(ParserFactory.ParserType.json);

        String result = parser.parse(payload, "field1");
        assertEquals("value1", result);

        result = parser.parse(payload, "field2[1]");
        assertEquals("value3", result);

        result = parser.parse(payload, "nonexistent");
        assertEquals("", result);

        result = parser.parse(payload, "field2[5]");
        assertEquals("", result);

        result = parser.parse(payload, null);
        assertNull(result);

        result = parser.parse(payload, "");
        assertNull(result);
    }

    @Test
    void testGetValueFromPayload() {
        String payload = "{ \"field1\": \"value1\", \"field2\": [\"value2\", \"value3\"] }";
        Parser parser = new Parser(ParserFactory.ParserType.json);

        String result = parser.getValueFromPayload(payload, "field1");
        assertEquals("value1", result);

        result = parser.getValueFromPayload(payload, "nonexistent");
        assertEquals("", result);

        result = parser.getValueFromPayload(payload, "field2[1]");
        assertEquals("value3", result);

        result = parser.getValueFromPayload(payload, "field2[5]");
        assertEquals("", result);

        result = parser.getValueFromPayload(payload, null);
        assertNull(result);

        result = parser.getValueFromPayload(payload, "");
        assertNull(result);
    }

    @Test
    void testNormalizeAuditCategory() {
        Parser parser = new Parser(ParserFactory.ParserType.json);
        ObjectMapper objectMapper = new ObjectMapper();

        String restPayload = "{\"audit_category\": \"FAILED_LOGIN\", \"audit_request_layer\": \"REST\"}";
        String normalizedRest = parser.normalizeAuditCategory(restPayload);
        try {
            JsonNode restNode = objectMapper.readTree(normalizedRest);
            assertEquals("REST_FAILED_LOGIN", restNode.path("audit_category").asText());
        } catch (Exception e) {
            fail("Exception occurred while parsing JSON: " + e.getMessage());
        }

        String transportPayload = "{\"audit_category\": \"FAILED_LOGIN\", \"audit_request_layer\": \"TRANSPORT\"}";
        String normalizedTransport = parser.normalizeAuditCategory(transportPayload);
        try {
            JsonNode transportNode = objectMapper.readTree(normalizedTransport);
            assertEquals("TRANSPORT_FAILED_LOGIN", transportNode.path("audit_category").asText());
        } catch (Exception e) {
            fail("Exception occurred while parsing JSON: " + e.getMessage());
        }

        String standardPayload = "{\"audit_category\": \"INDEX_EVENT\", \"audit_request_layer\": \"REST\"}";
        String normalizedStandard = parser.normalizeAuditCategory(standardPayload);
        try {
            JsonNode standardNode = objectMapper.readTree(normalizedStandard);
            assertEquals("INDEX_EVENT", standardNode.path("audit_category").asText());
        } catch (Exception e) {
            fail("Exception occurred while parsing JSON: " + e.getMessage());
        }

        String malformedPayload = "{\"audit_category\": \"FAILED_LOGIN\"}";
        String normalizedMalformed = parser.normalizeAuditCategory(malformedPayload);
        assertEquals(malformedPayload, normalizedMalformed);
    }

    @Test
    void testConfigSchema() {
        OpensearchGuardiumFilter filter = new OpensearchGuardiumFilter("testId", null, null);
        Collection<PluginConfigSpec<?>> configSchema = filter.configSchema();
        assertNotNull(configSchema);
        assertEquals(1, configSchema.size());
        assertTrue(configSchema.contains(OpensearchGuardiumFilter.SOURCE_CONFIG));
    }

    @Test
    void testGetId() {
        String expectedId = "testId";
        OpensearchGuardiumFilter filter = new OpensearchGuardiumFilter(expectedId, null, null);
        String actualId = filter.getId();
        assertEquals(expectedId, actualId);
    }

    @Test
    void testParseTimestamp() {
        String isoTimestamp = "2023-10-01T12:34:56.789Z";
        Time time = Parser.parseTimestamp(isoTimestamp);
        assertNotNull(time);

        String invalidTimestamp = "invalid-timestamp";
        assertThrows(IllegalArgumentException.class, () -> Parser.parseTimestamp(invalidTimestamp));
        assertThrows(IllegalArgumentException.class, () -> Parser.parseTimestamp(null));
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
    @Test
    public void testNormalizeReservedKeyword() {
        assertEquals("_user", Parser.normalizeReservedKeyword("user"));
        assertEquals("_get", Parser.normalizeReservedKeyword("get"));
        assertEquals("school", Parser.normalizeReservedKeyword("school"));
        assertNull(Parser.normalizeReservedKeyword(null));
    }

    @Test
    public void testCheckURIPath_basic() {
        assertEquals("/students", Parser.checkURIPath("students"));
        assertEquals("/_user", Parser.checkURIPath("user"));
        assertEquals("/_get/_template", Parser.checkURIPath("get/template"));
    }

    @Test
    public void testCheckURIPath_encodedAndInvalid() {
        assertEquals("/invalid/xml_input", Parser.checkURIPath("<?xml version=\"1.0\"?>"));
        assertEquals("/_mappings", Parser.checkURIPath("indices:mappings"));
    }
}
