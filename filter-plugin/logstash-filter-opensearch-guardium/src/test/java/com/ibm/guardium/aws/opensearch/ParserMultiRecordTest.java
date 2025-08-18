package com.ibm.guardium.aws.opensearch;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibm.guardium.universalconnector.commons.custom_parsing.ParserFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ParserMultiRecordTest {

    @Test
    void testSingleRecordJsonFormat() throws Exception {
        // Test with a single record
        String payload = "{\n" +
                "    \"audit_category\": \"COMPLIANCE_DOC_WRITE\",\n" +
                "    \"audit_request_body\": \"{\\n  \\\"student_id\\\": \\\"S022\\\",\\n  \\\"name\\\": \\\"William Brooks\\\",\\n  \\\"grade\\\": 10,\\n  \\\"class\\\": \\\"10B\\\",\\n  \\\"subjects\\\": [\\\"Math\\\", \\\"Physics\\\"],\\n  \\\"admission_date\\\": \\\"2021-09-20\\\",\\n  \\\"address\\\": {\\n    \\\"street\\\": \\\"123 Oakwood Dr\\\",\\n    \\\"city\\\": \\\"Springfield\\\",\\n    \\\"zip\\\": \\\"12357\\\"\\n  }\\n}\\n\"\n" +
                "}";

        Parser parser = new Parser(ParserFactory.ParserType.json);
        String sqlString = parser.getSqlString(payload);

        // Verify the result contains valid JSON
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(sqlString.substring(sqlString.indexOf("#") + 1));

        // Check that _query field exists and is valid JSON
        assertTrue(jsonNode.has("_query"));
        JsonNode queryNode = jsonNode.get("_query");
        assertTrue(queryNode.isObject());
        assertEquals("S022", queryNode.get("student_id").asText());
    }

    @Test
    void testMultipleRecordsJsonFormat() throws Exception {
        // Test with multiple records (bulk operation)
        String payload = "{\n" +
                "    \"audit_category\": \"COMPLIANCE_DOC_WRITE\",\n" +
                "    \"audit_request_body\": \"{ \\\"index\\\": { \\\"_index\\\": \\\"school\\\", \\\"_id\\\": 1 } }\\n" +
                "{ \\\"student_id\\\": \\\"S001\\\", \\\"name\\\": \\\"Alice Johnson\\\", \\\"grade\\\": 10, \\\"class\\\": \\\"10A\\\", \\\"subjects\\\": [\\\"Math\\\", \\\"Science\\\"], \\\"admission_date\\\": \\\"2021-06-15\\\", \\\"address\\\": { \\\"street\\\": \\\"123 Main St\\\", \\\"city\\\": \\\"Springfield\\\", \\\"zip\\\": \\\"12345\\\" } }\\n" +
                "{ \\\"index\\\": { \\\"_index\\\": \\\"school\\\", \\\"_id\\\": 2 } }\\n" +
                "{ \\\"student_id\\\": \\\"S002\\\", \\\"name\\\": \\\"Bob Smith\\\", \\\"grade\\\": 9, \\\"class\\\": \\\"9B\\\", \\\"subjects\\\": [\\\"English\\\", \\\"History\\\"], \\\"admission_date\\\": \\\"2022-07-01\\\", \\\"address\\\": { \\\"street\\\": \\\"456 Oak Ave\\\", \\\"city\\\": \\\"Springfield\\\", \\\"zip\\\": \\\"12346\\\" } }\\n" +
                "{ \\\"index\\\": { \\\"_index\\\": \\\"school\\\", \\\"_id\\\": 3 } }\\n" +
                "{ \\\"student_id\\\": \\\"S003\\\", \\\"name\\\": \\\"Charlie Davis\\\", \\\"grade\\\": 11, \\\"class\\\": \\\"11C\\\", \\\"subjects\\\": [\\\"Physics\\\", \\\"Chemistry\\\"], \\\"admission_date\\\": \\\"2020-09-10\\\", \\\"address\\\": { \\\"street\\\": \\\"789 Pine Rd\\\", \\\"city\\\": \\\"Shelbyville\\\", \\\"zip\\\": \\\"54321\\\" } }\\n\"\n" +
                "}";

        Parser parser = new Parser(ParserFactory.ParserType.json);
        String sqlString = parser.getSqlString(payload);

        // Verify the result contains valid JSON
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(sqlString.substring(sqlString.indexOf("#") + 1));

        // Check that _query field exists and is a JSON array
        assertTrue(jsonNode.has("_query"));
        JsonNode queryNode = jsonNode.get("_query");
        assertTrue(queryNode.isArray());

        // Verify we have 6 elements in the array (3 index operations + 3 documents)
        assertEquals(6, queryNode.size());

        // Check first document content
        JsonNode firstDoc = queryNode.get(1);
        assertEquals("S001", firstDoc.get("student_id").asText());

        // Check second document content
        JsonNode secondDoc = queryNode.get(3);
        assertEquals("S002", secondDoc.get("student_id").asText());

        // Check third document content
        JsonNode thirdDoc = queryNode.get(5);
        assertEquals("S003", thirdDoc.get("student_id").asText());
    }
}

// Made with Bob
