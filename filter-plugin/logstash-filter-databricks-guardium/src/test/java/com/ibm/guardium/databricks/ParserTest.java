package com.ibm.guardium.databricks;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ibm.guardium.universalconnector.commons.structures.Record;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ParserTest {

    @Test
    void testParseGetTime() {
        final String DatabricksString = "{ \"resourceId\": \"/SUBSCRIPTIONS/5C0C81D4-656F-415D-8599-DCD86F2F665E/RESOURCEGROUPS/DATABRICKSTEST/PROVIDERS/MICROSOFT.DATABRICKS/WORKSPACES/DATABRICK-TEST\", \"operationVersion\": \"1.0.0\", \"identity\": \"{\\\"email\\\":\\\"abc.h@abc.com\\\",\\\"subjectName\\\":null}\", \"operationName\": \"Microsoft.Databricks/accounts/tokenLogin\", \"time\": \"2025-06-05T13:55:56Z\", \"category\": \"accounts\", \"properties\": {\"sourceIPAddress\":\"20.193.136.102\",\"logId\":\"87e1a69e-444a-434e-a0be-648b1797e6d9\",\"serviceName\":\"accounts\",\"userAgent\":\"Apache-HttpClient/4.5.14 (Java/17.0.13) Databricks-Service/driver DBHttpClient/v2RawClient\",\"response\":\"{\\\"statusCode\\\":200}\",\"sessionId\":null,\"actionName\":\"tokenLogin\",\"requestId\":\"0018d88c-2f52-4cf0-86a4-8d1dc416ab10\",\"requestParams\":\"{\\\"user\\\":\\\"abc@abc.com\\\",\\\"tokenId\\\":\\\"kfhjgjfhdgjkdh39284783297423943hejhfkdsfh39\\\",\\\"authenticationMethod\\\":\\\"API_INT_PAT_TOKEN\\\"}\"}, \"Host\": \"1234-123456-ab1c2d3e-12-123-12-1\"}";
        final JsonObject DatabricksJson = JsonParser.parseString(DatabricksString).getAsJsonObject();
        Record record = Parser.parseRecord(DatabricksJson);
        assertEquals(1749131756000L, record.getTime().getTimstamp());
        assertNotNull(record);
    }

    @Test
    void testParseServiceName() {
        final String DatabricksString = "{ \"resourceId\": \"/SUBSCRIPTIONS/5C0C81D4-656F-415D-8599-DCD86F2F665E/RESOURCEGROUPS/DATABRICKSTEST/PROVIDERS/MICROSOFT.DATABRICKS/WORKSPACES/DATABRICK-TEST\", \"operationVersion\": \"1.0.0\", \"identity\": \"{\\\"email\\\":\\\"abc.h@abc.com\\\",\\\"subjectName\\\":null}\", \"operationName\": \"Microsoft.Databricks/accounts/tokenLogin\", \"time\": \"2025-05-01T13:43:28Z\", \"category\": \"accounts\", \"properties\": {\"sourceIPAddress\":\"20.193.136.102\",\"logId\":\"87e1a69e-444a-434e-a0be-648b1797e6d9\",\"serviceName\":\"accounts\",\"userAgent\":\"Apache-HttpClient/4.5.14 (Java/17.0.13) Databricks-Service/driver DBHttpClient/v2RawClient\",\"response\":\"{\\\"statusCode\\\":200}\",\"sessionId\":null,\"actionName\":\"tokenLogin\",\"requestId\":\"0018d88c-2f52-4cf0-86a4-8d1dc416ab10\",\"requestParams\":\"{\\\"user\\\":\\\"abc@abc.com\\\",\\\"tokenId\\\":\\\"kfhjgjfhdgjkdh39284783297423943hejhfkdsfh39\\\",\\\"authenticationMethod\\\":\\\"API_INT_PAT_TOKEN\\\"}\"}, \"Host\": \"1234-123456-ab1c2d3e-12-123-12-1\"}";
        final JsonObject DatabricksJson = JsonParser.parseString(DatabricksString).getAsJsonObject();
        Record record = Parser.parseRecord(DatabricksJson);
        assertEquals("accounts", record.getAccessor().getServiceName());

    }

    @Test
    void testParseClientIp() {
        final String DatabricksString = "{ \"resourceId\": \"/SUBSCRIPTIONS/5C0C81D4-656F-415D-8599-DCD86F2F665E/RESOURCEGROUPS/DATABRICKSTEST/PROVIDERS/MICROSOFT.DATABRICKS/WORKSPACES/DATABRICK-TEST\", \"operationVersion\": \"1.0.0\", \"identity\": \"{\\\"email\\\":\\\"abc.h@abc.com\\\",\\\"subjectName\\\":null}\", \"operationName\": \"Microsoft.Databricks/accounts/tokenLogin\", \"time\": \"2025-05-01T13:43:28Z\", \"category\": \"accounts\", \"properties\": {\"sourceIPAddress\":\"20.193.136.102\",\"logId\":\"87e1a69e-444a-434e-a0be-648b1797e6d9\",\"serviceName\":\"accounts\",\"userAgent\":\"Apache-HttpClient/4.5.14 (Java/17.0.13) Databricks-Service/driver DBHttpClient/v2RawClient\",\"response\":\"{\\\"statusCode\\\":200}\",\"sessionId\":null,\"actionName\":\"tokenLogin\",\"requestId\":\"0018d88c-2f52-4cf0-86a4-8d1dc416ab10\",\"requestParams\":\"{\\\"user\\\":\\\"abc@abc.com\\\",\\\"tokenId\\\":\\\"kfhjgjfhdgjkdh39284783297423943hejhfkdsfh39\\\",\\\"authenticationMethod\\\":\\\"API_INT_PAT_TOKEN\\\"}\"}, \"Host\": \"1234-123456-ab1c2d3e-12-123-12-1\"}";
        final JsonObject DatabricksJson = JsonParser.parseString(DatabricksString).getAsJsonObject();
        Record record = Parser.parseRecord(DatabricksJson);
        assertEquals("20.193.136.102", record.getSessionLocator().getClientIp());

    }

    @Test
    void testLogin() {
        final String DatabricksString = "{ \"resourceId\": \"/SUBSCRIPTIONS/5C0C81D4-656F-415D-8599-DCD86F2F665E/RESOURCEGROUPS/DATABRICKSTEST/PROVIDERS/MICROSOFT.DATABRICKS/WORKSPACES/DATABRICK-TEST\", \"operationVersion\": \"1.0.0\", \"identity\": \"{\\\"email\\\":\\\"abc.h@abc.com\\\",\\\"subjectName\\\":null}\", \"operationName\": \"Microsoft.Databricks/accounts/tokenLogin\", \"time\": \"2025-05-01T13:43:28Z\", \"category\": \"accounts\", \"properties\": {\"sourceIPAddress\":\"20.193.136.102\",\"logId\":\"87e1a69e-444a-434e-a0be-648b1797e6d9\",\"serviceName\":\"accounts\",\"userAgent\":\"Apache-HttpClient/4.5.14 (Java/17.0.13) Databricks-Service/driver DBHttpClient/v2RawClient\",\"response\":\"{\\\"statusCode\\\":200}\",\"sessionId\":null,\"actionName\":\"tokenLogin\",\"requestId\":\"0018d88c-2f52-4cf0-86a4-8d1dc416ab10\",\"requestParams\":\"{\\\"user\\\":\\\"abc@abc.com\\\",\\\"tokenId\\\":\\\"kfhjgjfhdgjkdh39284783297423943hejhfkdsfh39\\\",\\\"authenticationMethod\\\":\\\"API_INT_PAT_TOKEN\\\"}\"}, \"Host\": \"1234-123456-ab1c2d3e-12-123-12-1\"}";
        final JsonObject DatabricksJson = JsonParser.parseString(DatabricksString).getAsJsonObject();
        Record record = Parser.parseRecord(DatabricksJson);
        assertEquals("tokenLogin", record.getData().getOriginalSqlCommand());

    }

    @Test
    void testDBUser() {
        final String DatabricksString = "{ \"resourceId\": \"/SUBSCRIPTIONS/5C0C81D4-656F-415D-8599-DCD86F2F665E/RESOURCEGROUPS/DATABRICKSTEST/PROVIDERS/MICROSOFT.DATABRICKS/WORKSPACES/DATABRICK-TEST\", \"operationVersion\": \"1.0.0\", \"identity\": \"{\\\"email\\\":\\\"abc.h@abc.com\\\",\\\"subjectName\\\":null}\", \"operationName\": \"Microsoft.Databricks/accounts/tokenLogin\", \"time\": \"2025-05-01T13:43:28Z\", \"category\": \"accounts\", \"properties\": {\"sourceIPAddress\":\"20.193.136.102\",\"logId\":\"87e1a69e-444a-434e-a0be-648b1797e6d9\",\"serviceName\":\"accounts\",\"userAgent\":\"Apache-HttpClient/4.5.14 (Java/17.0.13) Databricks-Service/driver DBHttpClient/v2RawClient\",\"response\":\"{\\\"statusCode\\\":200}\",\"sessionId\":null,\"actionName\":\"tokenLogin\",\"requestId\":\"0018d88c-2f52-4cf0-86a4-8d1dc416ab10\",\"requestParams\":\"{\\\"user\\\":\\\"abc@abc.com\\\",\\\"tokenId\\\":\\\"kfhjgjfhdgjkdh39284783297423943hejhfkdsfh39\\\",\\\"authenticationMethod\\\":\\\"API_INT_PAT_TOKEN\\\"}\"}, \"Host\": \"1234-123456-ab1c2d3e-12-123-12-1\"}";
        final JsonObject DatabricksJson = JsonParser.parseString(DatabricksString).getAsJsonObject();
        Record record = Parser.parseRecord(DatabricksJson);
        assertEquals("abc.h@abc.com", record.getAccessor().getDbUser());

    }

    @Test
    public void testQueryINSERT() {
        final String DatabricksString = "{ \"resourceId\": \"/SUBSCRIPTIONS/5C0C81D4-656F-415D-8599-DCD86F2F665E/RESOURCEGROUPS/NEXUS/PROVIDERS/MICROSOFT.DATABRICKS/WORKSPACES/DATABRICK-TEST\", \"operationVersion\": \"1.0.0\", \"identity\": \"{\\\"email\\\":\\\"abc.h@abc.com\\\",\\\"subjectName\\\":null}\", \"operationName\": \"Microsoft.Databricks/databrickssql/commandSubmit\", \"time\": \"2025-05-20T18:02:06Z\", \"category\": \"databrickssql\", \"properties\": {\"sourceIPAddress\":null,\"logId\":\"65ca7d4e-410d-3051-b646-8159f9249d2b\",\"serviceName\":\"databrickssql\",\"userAgent\":\"\",\"response\":null,\"sessionId\":\"01f035a4-8aaf-10a6-a7e0-6a4bdc6dbdee\",\"actionName\":\"commandSubmit\",\"requestId\":\"bcc13087-c08c-46e7-91d2-542c8e70d455\",\"requestParams\":\"{\\\"warehouseId\\\":\\\"98f94533c2374cfa\\\",\\\"commandId\\\":\\\"01f035a4-8ac8-1b9b-a9f4-d60c6c929805\\\",\\\"commandText\\\":\\\"INSERT INTO sql_test_a (ID, FIRST_NAME, LAST_NAME) VALUES ('5', 'Steve', 'Jobs')\\\"}\"}, \"Host\": \"0520-070249-lol2uedj-10-139-96-0\"}";
        final JsonObject DatabricksJson = JsonParser.parseString(DatabricksString).getAsJsonObject();
        Record record = Parser.parseRecord(DatabricksJson);
        assertEquals("INSERT", record.getData().getConstruct().getSentences().get(0).getVerb());
        assertEquals("sql_test_a", record.getData().getConstruct().getSentences().get(0).getObjects().get(0).name);
    }

    @Test
    public void testQuerySELECT() {
        final String DatabricksString = "{ \"resourceId\": \"/SUBSCRIPTIONS/6C0A81D4-656F-415D-8599-DCD86F2F665E/RESOURCEGROUPS/NEXUS/PROVIDERS/MICROSOFT.DATABRICKS/WORKSPACES/DATABRICK-TEST\", \"operationVersion\": \"1.0.0\", \"identity\": \"{\\\"email\\\":\\\"abc@abc.com\\\",\\\"subjectName\\\":null}\", \"operationName\": \"Microsoft.Databricks/databrickssql/commandSubmit\", \"time\": \"2025-05-26T15:52:46Z\", \"category\": \"databrickssql\", \"properties\": {\"sourceIPAddress\":null,\"logId\":\"564590ed-bb0e-3004-8316-bd9449497e93\",\"serviceName\":\"databrickssql\",\"userAgent\":\"\",\"response\":null,\"sessionId\":\"01f03a49-77e8-12ce-a914-5ad6d3cf3b47\",\"actionName\":\"commandSubmit\",\"requestId\":\"ebbfdd8d-5c7c-4e45-88a6-bee1de75243f\",\"requestParams\":\"{\\\"warehouseId\\\":\\\"75f94533c2374cfa\\\",\\\"commandId\\\":\\\"01f03a49-77ec-1901-9623-18fcec1c5e2c\\\",\\\"commandText\\\":\\\"SELECT * FROM test\\\"}\"}, \"Host\": \"0526-070308-ald41zyz-10-139-74-1\"}";
        final JsonObject DatabricksJson = JsonParser.parseString(DatabricksString).getAsJsonObject();
        Record record = Parser.parseRecord(DatabricksJson);
        assertEquals("SELECT", record.getData().getConstruct().getSentences().get(0).getVerb());
        assertEquals("test", record.getData().getConstruct().getSentences().get(0).getObjects().get(0).name);
    }

    @Test
    public void testQueryCREATEVIEW() {
        final String DatabricksString = "{ \"resourceId\": \"/SUBSCRIPTIONS/8JWEHRI-656F-415D-8599-DCD86F2F665E/RESOURCEGROUPS/NEXUS/PROVIDERS/MICROSOFT.DATABRICKS/WORKSPACES/DATABRICK-TEST\", \"operationVersion\": \"1.0.0\", \"identity\": \"{\\\"email\\\":\\\"abc@abc.com\\\",\\\"subjectName\\\":null}\", \"operationName\": \"Microsoft.Databricks/databrickssql/commandSubmit\", \"time\": \"2025-05-26T15:44:04Z\", \"category\": \"databrickssql\", \"properties\": {\"sourceIPAddress\":null,\"logId\":\"86328842-d81b-3648-8466-f57bbc266f22\",\"serviceName\":\"databrickssql\",\"userAgent\":\"\",\"response\":null,\"sessionId\":\"01f03a48-4064-1013-9da7-a6f7b1bb7f0b\",\"actionName\":\"commandSubmit\",\"requestId\":\"31b1fa55-ca89-4b99-add8-bc3d1267e671\",\"requestParams\":\"{\\\"warehouseId\\\":\\\"75f94533c2374cfa\\\",\\\"commandId\\\":\\\"01f03a48-406e-17df-9a82-7335f9404a69\\\",\\\"commandText\\\":\\\"CREATE TEMPORARY VIEW test(id, name) AS\\\\n  VALUES ( 1, 'Lisa'),\\\\n         ( 2, 'Mary'),\\\\n         ( 3, 'Evan'),\\\\n         ( 4, 'Fred'),\\\\n         ( 7, 'Lily')\\\"}\"}, \"Host\": \"0526-070308-ald41zyz-10-139-74-1\"}";
        final JsonObject DatabricksJson = JsonParser.parseString(DatabricksString).getAsJsonObject();
        Record record = Parser.parseRecord(DatabricksJson);
        assertEquals("CREATE TEMPORARY VIEW", record.getData().getConstruct().getSentences().get(0).getVerb());
        assertEquals("test", record.getData().getConstruct().getSentences().get(0).getObjects().get(0).name);
    }

    @Test
    public void testQuerySHOW() {
        final String DatabricksString = "{ \"resourceId\": \"/SUBSCRIPTIONS/8JWEHRI-656F-415D-8599-DCD86F2F665E/RESOURCEGROUPS/NEXUS/PROVIDERS/MICROSOFT.DATABRICKS/WORKSPACES/DATABRICK-TEST\", \"operationVersion\": \"1.0.0\", \"identity\": \"{\\\"email\\\":\\\"abc@abc.com\\\",\\\"subjectName\\\":null}\", \"operationName\": \"Microsoft.Databricks/databrickssql/commandSubmit\", \"time\": \"2025-05-26T15:44:04Z\", \"category\": \"databrickssql\", \"properties\": {\"sourceIPAddress\":null,\"logId\":\"86328842-d81b-3648-8466-f57bbc266f22\",\"serviceName\":\"databrickssql\",\"userAgent\":\"\",\"response\":null,\"sessionId\":\"01f03a48-4064-1013-9da7-a6f7b1bb7f0b\",\"actionName\":\"commandSubmit\",\"requestId\":\"31b1fa55-ca89-4b99-add8-bc3d1267e671\",\"requestParams\":\"{\\\"warehouseId\\\":\\\"75f94533c2374cfa\\\",\\\"commandId\\\":\\\"01f03a48-406e-17df-9a82-7335f9404a69\\\",\\\"commandText\\\":\\\"SHOW FUNCTIONS LIKE 't*'\\\"}\"}, \"Host\": \"0526-070308-ald41zyz-10-139-74-1\"}";
        final JsonObject DatabricksJson = JsonParser.parseString(DatabricksString).getAsJsonObject();
        Record record = Parser.parseRecord(DatabricksJson);
        assertEquals("SHOW FUNCTIONS", record.getData().getConstruct().getSentences().get(0).getVerb());
        assertEquals("'t*'", record.getData().getConstruct().getSentences().get(0).getObjects().get(0).name);
    }

    @Test
    public void testQuerySYS() {
        final String DatabricksString = "{ \"resourceId\": \"/SUBSCRIPTIONS/5C0C81D4-656F-415D-8599-DCD86F2F665E/RESOURCEGROUPS/NEXUS/PROVIDERS/MICROSOFT.DATABRICKS/WORKSPACES/DATABRICK-TEST\", \"operationVersion\": \"1.0.0\", \"identity\": \"{\\\"email\\\":\\\"abc@abc.com\\\",\\\"subjectName\\\":null}\", \"operationName\": \"Microsoft.Databricks/databrickssql/commandSubmit\", \"time\": \"2025-05-29T18:24:40Z\", \"category\": \"databrickssql\", \"properties\": {\"sourceIPAddress\":null,\"logId\":\"f5981ae6-3e04-30ac-9a2d-92c5dc5c1bf3\",\"serviceName\":\"databrickssql\",\"userAgent\":\"\",\"response\":null,\"sessionId\":\"01f03cba-2f8c-1fb3-8e12-2a688100edcc\",\"actionName\":\"commandSubmit\",\"requestId\":\"872c8c31-e344-44ce-a90b-9a3b21cfed50\",\"requestParams\":\"{\\\"warehouseId\\\":\\\"75f94533c2374cfa\\\",\\\"commandId\\\":\\\"01f03cba-2f93-1c70-b76d-325984240046\\\",\\\"commandText\\\":\\\"-- This is a system generated query from sql editor\\\\n"
                + //
                "show tables in `hive_metastore`.`default`;\\\"}\"}, \"Host\": \"0529-070248-plvfgds0-10-139-74-0\"}";
        final JsonObject DatabricksJson = JsonParser.parseString(DatabricksString).getAsJsonObject();
        Record record = Parser.parseRecord(DatabricksJson);
        assertEquals("SHOW TABLES", record.getData().getConstruct().getSentences().get(0).getVerb());
        assertEquals("`hive_metastore`.`default`", record.getData().getConstruct().getSentences().get(0).getObjects().get(0).name);
    }
    @Test
    public void testQueryDESCIBE() {
        final String DatabricksString = "{ \"resourceId\": \"/SUBSCRIPTIONS/5C0C81D4-656F-415D-8599-DCD86F2F665E/RESOURCEGROUPS/NEXUS/PROVIDERS/MICROSOFT.DATABRICKS/WORKSPACES/DATABRICK-TEST\", \"operationVersion\": \"1.0.0\", \"identity\": \"{\\\"email\\\":\\\"abc@abc.com\\\",\\\"subjectName\\\":null}\", \"operationName\": \"Microsoft.Databricks/databrickssql/commandSubmit\", \"time\": \"2025-05-29T18:24:40Z\", \"category\": \"databrickssql\", \"properties\": {\"sourceIPAddress\":null,\"logId\":\"f5981ae6-3e04-30ac-9a2d-92c5dc5c1bf3\",\"serviceName\":\"databrickssql\",\"userAgent\":\"\",\"response\":null,\"sessionId\":\"01f03cba-2f8c-1fb3-8e12-2a688100edcc\",\"actionName\":\"commandSubmit\",\"requestId\":\"872c8c31-e344-44ce-a90b-9a3b21cfed50\",\"requestParams\":\"{\\\"warehouseId\\\":\\\"75f94533c2374cfa\\\",\\\"commandId\\\":\\\"01f03cba-2f93-1c70-b76d-325984240046\\\",\\\"commandText\\\":\\\"-- This is a system generated query from sql editor\\\\n"
                + //
                "describe table extended `hive_metastore`.`default`.`hive_table`;\\\"}\"}, \"Host\": \"0529-070248-plvfgds0-10-139-74-0\"}";
        final JsonObject DatabricksJson = JsonParser.parseString(DatabricksString).getAsJsonObject();
        Record record = Parser.parseRecord(DatabricksJson);
        assertEquals("DESCRIBE", record.getData().getConstruct().getSentences().get(0).getVerb());
        assertEquals("`hive_metastore`.`default`.`hive_table`", record.getData().getConstruct().getSentences().get(0).getObjects().get(0).name);
    }

    @Test
    public void testQuerySHOWFUNCTIONS() {
        final String DatabricksString = "{ \"resourceId\": \"/SUBSCRIPTIONS/5C0C81D4-656F-415D-8599-DCD86F2F665E/RESOURCEGROUPS/NEXUS/PROVIDERS/MICROSOFT.DATABRICKS/WORKSPACES/DATABRICK-TEST\", \"operationVersion\": \"1.0.0\", \"identity\": \"{\\\"email\\\":\\\"abc@abc.com\\\",\\\"subjectName\\\":null}\", \"operationName\": \"Microsoft.Databricks/databrickssql/commandSubmit\", \"time\": \"2025-05-29T18:24:40Z\", \"category\": \"databrickssql\", \"properties\": {\"sourceIPAddress\":null,\"logId\":\"f5981ae6-3e04-30ac-9a2d-92c5dc5c1bf3\",\"serviceName\":\"databrickssql\",\"userAgent\":\"\",\"response\":null,\"sessionId\":\"01f03cba-2f8c-1fb3-8e12-2a688100edcc\",\"actionName\":\"commandSubmit\",\"requestId\":\"872c8c31-e344-44ce-a90b-9a3b21cfed50\",\"requestParams\":\"{\\\"warehouseId\\\":\\\"75f94533c2374cfa\\\",\\\"commandId\\\":\\\"01f03cba-2f93-1c70-b76d-325984240046\\\",\\\"commandText\\\":\\\"-- This is a system generated query from sql editor\\\\n"
                + //
                "-- This is a system generated query from sql editor\nshow functions;\\\"}\"}, \"Host\": \"0529-070248-plvfgds0-10-139-74-0\"}";
        final JsonObject DatabricksJson = JsonParser.parseString(DatabricksString).getAsJsonObject();
        Record record = Parser.parseRecord(DatabricksJson);
        assertEquals("SHOW FUNCTIONS", record.getData().getConstruct().getSentences().get(0).getVerb());
    }

    @Test
    public void testParseException() {
        final String DatabricksString = "{ \"resourceId\": \"/SUBSCRIPTIONS/5C0C81D4-656F-415D-8599-DCD86F2F665E/RESOURCEGROUPS/NEXUS/PROVIDERS/MICROSOFT.DATABRICKS/WORKSPACES/DATABRICK-TEST\", \"operationVersion\": \"1.0.0\", \"identity\": \"{\\\"email\\\":\\\"System-User\\\",\\\"subjectName\\\":null}\", \"operationName\": \"Microsoft.Databricks/databrickssql/commandFinish\", \"time\": \"2025-05-20T17:59:01Z\", \"category\": \"databrickssql\", \"properties\": {\"sourceIPAddress\":null,\"logId\":\"ed28a166-6bac-342b-b8d0-237037250589\",\"serviceName\":\"databrickssql\",\"userAgent\":\"\",\"response\":\"{\\\"statusCode\\\":400,\\\"errorMessage\\\":\\\"[USER_RAISED_EXCEPTION] This is a custom error message SQLSTATE: P0001\\\"}\",\"sessionId\":\"01f035a4-18f0-1667-ac2c-aabd587fbc36\",\"actionName\":\"commandFinish\",\"requestId\":\"317ff959-7147-42ba-9009-b5f7b5279094\",\"requestParams\":\"{\\\"warehouseId\\\":\\\"75f94533c2374cfa\\\",\\\"commandId\\\":\\\"01f035a4-191c-13ae-8b5a-7ffa329cbb4c\\\"}\"}, \"Host\": \"0520-070249-lol2uedj-10-139-96-0\"}";
        final JsonObject DatabricksJson = JsonParser.parseString(DatabricksString).getAsJsonObject();
        Record record = Parser.parseRecord(DatabricksJson);
        assertEquals("Error:400 Action: databrickssql/commandFinish Message: [USER_RAISED_EXCEPTION] This is a custom error message SQLSTATE: P0001",
                record.getException().getDescription());
        assertEquals("SQL_ERROR", record.getException().getExceptionTypeId());
    }

    @Test
    public void testParseExceptionNoErrMessage() {
        final String DatabricksString = "{ \"resourceId\": \"/SUBSCRIPTIONS/5C0C81D4-656F-415D-8599-DCD86F2F665E/RESOURCEGROUPS/NEXUS/PROVIDERS/MICROSOFT.DATABRICKS/WORKSPACES/DATABRICK-TEST\", \"operationVersion\": \"1.0.0\", \"identity\": \"{\\\"email\\\":\\\"System-User\\\",\\\"subjectName\\\":null}\", \"operationName\": \"Microsoft.Databricks/databrickssql/commandFinish\", \"time\": \"2025-05-20T17:59:01Z\", \"category\": \"databrickssql\", \"properties\": {\"sourceIPAddress\":null,\"logId\":\"ed28a166-6bac-342b-b8d0-237037250589\",\"serviceName\":\"databrickssql\",\"userAgent\":\"\",\"response\":\"{\\\"statusCode\\\":400}\", \"sessionId\":\"01f035a4-18f0-1667-ac2c-aabd587fbc36\",\"actionName\":\"commandFinish\",\"requestId\":\"317ff959-7147-42ba-9009-b5f7b5279094\",\"requestParams\":\"{\\\"warehouseId\\\":\\\"75f94533c2374cfa\\\",\\\"commandId\\\":\\\"01f035a4-191c-13ae-8b5a-7ffa329cbb4c\\\"}\"}, \"Host\": \"0520-070249-lol2uedj-10-139-96-0\"}";
        final JsonObject DatabricksJson = JsonParser.parseString(DatabricksString).getAsJsonObject();
        Record record = Parser.parseRecord(DatabricksJson);
        assertEquals("Error:400 Action: databrickssql/commandFinish Message: {\"statusCode\":400}",
                record.getException().getDescription());
        assertEquals("SQL_ERROR", record.getException().getExceptionTypeId());
    }

    @Test
    public void testParseExceptionStatu() {
        final String DatabricksString = "{ \"resourceId\": \"/SUBSCRIPTIONS/5C0C81D4-656F-415D-8599-DCD86F2F665E/RESOURCEGROUPS/NEXUS/PROVIDERS/MICROSOFT.DATABRICKS/WORKSPACES/AZUREMTSERVER\", \"operationVersion\": \"1.0.0\", \"identity\": \"{\\\"email\\\":\\\"abc@abc.com\\\",\\\"subjectName\\\":null}\", \"operationName\": \"Microsoft.Databricks/notebook/runCommand\", \"time\": \"2025-06-19T14:38:01Z\", \"category\": \"notebook\", \"properties\": {\"sourceIPAddress\":\"0.1.87.5\",\"logId\":\"2b9419c3-9cac-31f4-bb9c-073dfa658974\",\"serviceName\":\"notebook\",\"userAgent\":\"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/137.0.0.0 Safari/537.36\",\"response\":\"{\\\"statusCode\\\":1,\\\"errorMessage\\\":\\\"InnerExecutionException: null\\\"}\",\"sessionId\":null,\"actionName\":\"runCommand\",\"requestId\":\"da7cc2fa-0f08-45e6-b241-54b20acc4eb7\",\"requestParams\":\"{\\\"notebookId\\\":\\\"3116387834167860\\\",\\\"clusterId\\\":\\\"0611-191046-ba0xnptx\\\",\\\"executionTime\\\":\\\"0.289\\\",\\\"status\\\":\\\"failed\\\",\\\"commandLanguage\\\":\\\"python\\\",\\\"commandId\\\":\\\"7254928687120170\\\",\\\"commandText\\\":\\\"SELECT * FROM hive_metastore.default.my_table1;\\\"}\"}, \"Host\": \"0619-070308-hw7n9ftr-10-139-92-0\"}";
        final JsonObject DatabricksJson = JsonParser.parseString(DatabricksString).getAsJsonObject();
        Record record = Parser.parseRecord(DatabricksJson);
        assertEquals("Error:1 Action: notebook/runCommand Message: InnerExecutionException: null",
                record.getException().getDescription());
        assertEquals("SQL_ERROR", record.getException().getExceptionTypeId());
        assertEquals("SELECT * FROM hive_metastore.default.my_table1;", record.getException().getSqlString());

    }

}
