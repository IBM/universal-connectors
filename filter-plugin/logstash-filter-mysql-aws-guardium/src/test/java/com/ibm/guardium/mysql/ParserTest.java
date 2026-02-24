package com.ibm.guardium.mysql;

import co.elastic.logstash.api.Event;
import com.ibm.guardium.universalconnector.commons.structures.Record;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.text.ParseException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class ParserTest {

    @Test
    void testParseRecord_withValidInput_shouldReturnValidRecord() throws ParseException {
        // Mock the Event
        Event event = Mockito.mock(Event.class);

        // Prepare data for the mocked Event
        Map<String, Object> mockData = new HashMap<>();

        // Nested cloudwatch data as a Map
        Map<String, Object> logEvents = new HashMap<>();
        logEvents.put("id", "12345678901234567890123456789012345678901234567890123456");
        logEvents.put("timestamp", 1753323600000L);

        Map<String, Object> cloudwatch = new HashMap<>();
        cloudwatch.put("logEvents", logEvents);
        cloudwatch.put("logStream", "perfmysqlsv");
        cloudwatch.put("messageType", "DATA_MESSAGE");

        List<String> subscriptionFiltersList  = new ArrayList();
        subscriptionFiltersList.add("PostgresCloudWatchLogstoS3");
        Map<String, List<String>> subscriptionFiltersMap = new HashMap();
        subscriptionFiltersMap.put("delegate",subscriptionFiltersList);

        cloudwatch.put("subscriptionFilters",subscriptionFiltersMap);

        cloudwatch.put("owner", "123456789012");
        cloudwatch.put("logGroup", "/aws/rds/instance/sample-mysql-instance/audit");

        mockData.put("cloudwatch", cloudwatch);
        mockData.put("query_timestamp", "20250724 02:20:00");
        mockData.put("client_port", 734);
        mockData.put("GuardRecord_did_not_exist", true);
        mockData.put("logStream", "sample-mysql-instance");
        mockData.put("db_user", "unknown");
        mockData.put("client_ip", "192.168.1.100");
        mockData.put("fileKey", "mysql-logs/2025/07/24/CloudWatchLogstoS3-22-2025-07-24-02-19-30-sample-file-key.gz");
        mockData.put("command_type", "UNKNOWN");
        mockData.put("bucketName", "sample-mysql-audit-bucket");
        mockData.put("command", "QUERY");
        mockData.put("@timestamp", "2025-07-24T02:20:35.622185651Z");
        mockData.put("user", "admin");
        mockData.put("timestamp", "2025-07-24T02:20:35.622126440Z");
        mockData.put("database", "testdb");
        mockData.put("host", "sample-host-01");
        mockData.put("connection_id", 366361);
        mockData.put("@version", 1);
        mockData.put("type", "MYSQL_S3SQS");
        mockData.put("status_code", 0);

        List<String> accountId = new ArrayList<String>();
        accountId.add("123456789012");
        accountId.add("123456789012");

        mockData.put("account_id", accountId);
        mockData.put("query", "/* ApplicationName=DBeaver 25.0.3 - SQLEditor <Script-23.sql> */ CREATE TABLE employees (\n" +
                "    id INT AUTO_INCREMENT PRIMARY KEY,\n" +
                "    first_name VARCHAR(50),\n" +
                "    last_name VARCHAR(50),\n" +
                "    email VARCHAR(100),\n" +
                "    hire_date DATE\n)");

        // Embed the cloudwatch object as JSON string under "message" for getAccountIdDBName
        mockData.put("message", new com.google.gson.Gson().toJson(cloudwatch));

        // Set mock behavior
        Mockito.when(event.getData()).thenReturn(mockData);

        // Act
        Record record = Parser.parseRecord(event);

        // Assert
        assertNotNull(record);
        assertNotNull(record.getAccessor());
        assertNotNull(record.getSessionLocator());
        assertEquals("admin", record.getAccessor().getDbUser());
        assertNotNull(record.getData());
        assertTrue(record.getData().getOriginalSqlCommand().startsWith("/* ApplicationName=DBeaver"));

        // Validate db name format
        assertTrue(record.getDbName().contains("123456789012:sample-mysql-instance:testdb"));
    }

    @Test
    void testParseRecord_withQuery() throws ParseException {
        // Mock the Event
        Event event = Mockito.mock(Event.class);

        // Prepare data for the mocked Event
        Map<String, Object> mockData = new HashMap<>();

        // Nested cloudwatch data as a Map
        Map<String, Object> logEvents = new HashMap<>();
        logEvents.put("id", "12345678901234567890123456789012345678901234567890123456");
        logEvents.put("timestamp", 1753323600000L);

        Map<String, Object> cloudwatch = new HashMap<>();
        cloudwatch.put("logEvents", logEvents);
        cloudwatch.put("logStream", "perfmysqlsv");
        cloudwatch.put("messageType", "DATA_MESSAGE");

        List<String> subscriptionFiltersList  = new ArrayList();
        subscriptionFiltersList.add("PostgresCloudWatchLogstoS3");
        Map<String, List<String>> subscriptionFiltersMap = new HashMap();
        subscriptionFiltersMap.put("delegate",subscriptionFiltersList);

        cloudwatch.put("subscriptionFilters",subscriptionFiltersMap);

        cloudwatch.put("owner", "123456789012");
        cloudwatch.put("logGroup", "/aws/rds/instance/sample-mysql-instance/audit");

        mockData.put("cloudwatch", cloudwatch);
        mockData.put("query_timestamp", "20250724 02:20:00");
        mockData.put("client_port", 734);
        mockData.put("GuardRecord_did_not_exist", true);
        mockData.put("logStream", "sample-mysql-instance");
        mockData.put("db_user", "unknown");
        mockData.put("client_ip", "192.168.1.100");
        mockData.put("fileKey", "mysql-logs/2025/07/24/CloudWatchLogstoS3-22-2025-07-24-02-19-30-sample-file-key.gz");
        mockData.put("command_type", "UNKNOWN");
        mockData.put("bucketName", "sample-mysql-audit-bucket");
        mockData.put("command", "QUERY");
        mockData.put("@timestamp", "2025-07-24T02:20:35.622185651Z");
        mockData.put("user", "admin");
        mockData.put("timestamp", "2025-07-24T02:20:35.622126440Z");
        mockData.put("database", "testdb");
        mockData.put("host", "sample-host-01");
        mockData.put("connection_id", 366361);
        mockData.put("@version", 1);
        mockData.put("type", "MYSQL_S3SQS");
        mockData.put("status_code", 0);

        List<String> accountId = new ArrayList<String>();
        accountId.add("123456789012");
        accountId.add("123456789012");

        mockData.put("account_id", accountId);
        mockData.put("query", "/* ApplicationName=DBeaver 25.0.3 - SQLEditor <Script-23.sql> */ INSERT INTO Star123 (first_name, last_name, email, hire_date) VALUES(\\'John\\', \\'Doe\\', \\'john.doe@example.com\\', \\'2022-01-15\\')");

        // Embed the cloudwatch object as JSON string under "message" for getAccountIdDBName
        mockData.put("message", new com.google.gson.Gson().toJson(cloudwatch));

        // Set mock behavior
        Mockito.when(event.getData()).thenReturn(mockData);

        // Act
        Record record = Parser.parseRecord(event);

        // Assert
        assertNotNull(record);
        assertNotNull(record.getAccessor());
        assertNotNull(record.getSessionLocator());
        assertEquals("admin", record.getAccessor().getDbUser());
        assertNotNull(record.getData());
        assertTrue(record.getData().getOriginalSqlCommand().startsWith("/* ApplicationName=DBeaver"));

        // Validate db name format
        assertTrue(record.getDbName().contains("123456789012:sample-mysql-instance:testdb"));
    }


    @Test
    void testParseRecordSQLSyntaxError() throws ParseException {
        // Mock the Event
        Event event = Mockito.mock(Event.class);

        // Prepare data for the mocked Event
        Map<String, Object> mockData = new HashMap<>();

        // Nested cloudwatch data as a Map
        Map<String, Object> logEvents = new HashMap<>();
        logEvents.put("id", "98765432109876543210987654321098765432109876543210987654");
        logEvents.put("timestamp", 1753342566000L);

        Map<String, Object> cloudwatch = new HashMap<>();
        cloudwatch.put("logEvents", logEvents);
        cloudwatch.put("logStream", "perfmysqlsv");
        cloudwatch.put("messageType", "DATA_MESSAGE");

        List<String> subscriptionFiltersList = new ArrayList<>();
        subscriptionFiltersList.add("PostgresCloudWatchLogstoS3");

        Map<String, List<String>> subscriptionFiltersMap = new HashMap<>();
        subscriptionFiltersMap.put("delegate", subscriptionFiltersList);

        cloudwatch.put("subscriptionFilters", subscriptionFiltersMap);
        cloudwatch.put("owner", "123456789012");
        cloudwatch.put("logGroup", "/aws/rds/instance/sample-mysql-instance/audit");

        mockData.put("cloudwatch", cloudwatch);
        mockData.put("query_timestamp", "20250724 07:36:06");
        mockData.put("client_port", 734);
        mockData.put("GuardRecord_did_not_exist", true);
        mockData.put("logStream", "sample-mysql-instance");
        mockData.put("db_user", "unknown");
        mockData.put("client_ip", "192.168.1.100");
        mockData.put("fileKey", "mysql-logs/2025/07/24/CloudWatchLogstoS3-22-2025-07-24-07-35-25-sample-file-key.gz");
        mockData.put("command_type", "UNKNOWN");
        mockData.put("bucketName", "sample-mysql-audit-bucket");
        mockData.put("command", "QUERY");
        mockData.put("@timestamp", "2025-07-24T07:36:30.492230782Z");
        mockData.put("timestamp", "2025-07-24T07:36:30.492191791Z");
        mockData.put("user", "admin");
        mockData.put("host", "sample-host-01");
        mockData.put("connection_id", 417016);
        mockData.put("database", "testdb");
        mockData.put("@version", 1);
        mockData.put("type", "S3SQS__MYSQL");
        mockData.put("status_code", 1064);

        List<String> accountId = new ArrayList<>();
        accountId.add("123456789012");
        accountId.add("123456789012");
        mockData.put("account_id", accountId);

        mockData.put("query", "/* ApplicationName=DBeaver 25.0.3 - SQLEditor <Script-23.sql> */ DRGOP TABLE 24July2025_05");

        // Embed the cloudwatch object as JSON string under "message"
        mockData.put("message", new com.google.gson.Gson().toJson(cloudwatch));

        // Set mock behavior
        Mockito.when(event.getData()).thenReturn(mockData);

        // Act
        Record record = Parser.parseRecord(event);

        // Assert
        assertNotNull(record);
        assertNotNull(record.getAccessor());
        assertNotNull(record.getSessionLocator());
        assertEquals("admin", record.getAccessor().getDbUser());
        assertNotNull(record.getException().getDescription());

        assertTrue(record.getDbName().contains("123456789012:sample-mysql-instance:testdb"));
    }

    @Test
    void testParseRecordFailedConnect() throws ParseException {
        // Mock the Event
        Event event = Mockito.mock(Event.class);

        // Prepare data for the mocked Event
        Map<String, Object> mockData = new HashMap<>();

        // Nested cloudwatch data as a Map
        Map<String, Object> logEvents = new HashMap<>();
        logEvents.put("id", "11111111111111111111111111111111111111111111111111111111");
        logEvents.put("timestamp", 1753346386000L);

        Map<String, Object> cloudwatch = new HashMap<>();
        cloudwatch.put("logEvents", logEvents);
        cloudwatch.put("logStream", "perfmysqlsv");
        cloudwatch.put("messageType", "DATA_MESSAGE");

        List<String> subscriptionFiltersList = new ArrayList<>();
        subscriptionFiltersList.add("PostgresCloudWatchLogstoS3");

        Map<String, List<String>> subscriptionFiltersMap = new HashMap<>();
        subscriptionFiltersMap.put("delegate", subscriptionFiltersList);

        cloudwatch.put("subscriptionFilters", subscriptionFiltersMap);
        cloudwatch.put("owner", "123456789012");
        cloudwatch.put("logGroup", "/aws/rds/instance/sample-mysql-instance/audit");

        mockData.put("cloudwatch", cloudwatch);
        mockData.put("client_port", 876);
        mockData.put("GuardRecord_did_not_exist", true);
        mockData.put("logStream", "sample-mysql-instance");
        mockData.put("db_user", "sample_user");
        mockData.put("client_ip", "192.168.1.100");
        mockData.put("fileKey", "mysql-logs/2025/07/24/CloudWatchLogstoS3-22-2025-07-24-08-39-00-sample-file-key.gz");
        mockData.put("command_type", "UNKNOWN");
        mockData.put("bucketName", "sample-mysql-audit-bucket");
        mockData.put("action", "FAILED_CONNECT");
        mockData.put("@timestamp", "2025-07-24T08:40:05.632510305Z");
        mockData.put("timestamp", "2025-07-24T08:40:05.632260750Z");
        mockData.put("connection_type", "TCP/IP");
        mockData.put("database", "unknown");
        mockData.put("status_code", 1045);
        mockData.put("type", "S3SQS__MYSQL");
        mockData.put("@version", 1);
        mockData.put("error_timestamp", "20250724 08:39:46");
        mockData.put("db_instance", "sample-host-01");

        List<String> accountIdList = new ArrayList<>();
        accountIdList.add("123456789012");
        accountIdList.add("123456789012");
        mockData.put("account_id", accountIdList);

        // Embed the cloudwatch object as JSON string under "message"
        mockData.put("message", new com.google.gson.Gson().toJson(cloudwatch));

        // Set mock behavior
        Mockito.when(event.getData()).thenReturn(mockData);

        // Act
        Record record = Parser.parseRecord(event);

        // Assert
        assertNotNull(record);
        assertNotNull(record.getAccessor());
        assertNotNull(record.getSessionLocator());

        assertEquals("192.168.1.100", record.getSessionLocator().getClientIp());

        assertTrue(record.getDbName().contains("123456789012:sample-mysql-instance:unknown"));
        assertNotNull(record.getException());
        assertEquals("LOGIN_FAILED", record.getException().getExceptionTypeId());
    }

}
