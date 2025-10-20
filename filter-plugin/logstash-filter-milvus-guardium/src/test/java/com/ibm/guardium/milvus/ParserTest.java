package com.ibm.guardium.milvus;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibm.guardium.universalconnector.commons.custom_parsing.ParserFactory;
import com.ibm.guardium.universalconnector.commons.structures.Record;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ParserTest {
    Parser parser = new Parser(ParserFactory.ParserType.leef);

    @Test
    void test1() {
        String payload = "LEEF:1.0|Zilliz|Milvus|1.0|Connect-Successful|devTime=2025/04/02 23:14:21.017 +00:00\tdevTimeFormat=yyyy/MM/dd HH:mm:ss.SSS xxx\tuserName=Unknown\tuserAddress=tcp-172.17.0.1:35050\tdatabaseName=Unknown\tcollectionName=Unknown\tpartitionName=Unknown\tqueryExpression=Unknown\terrorCode=0\terrorMessage=\ttraceId=cf03d862397e1fc9339f717865ae31f0\tresponseSize=105\ttimeCost=2.737959ms\ttimeStart=2025/04/02 23:14:21.014 +00:00\ttimeEnd=2025/04/02 23:14:21.017 +00:00\tsdkVersion=Python-2.4.3\tmethodName=Connect\tmethodStatus=Successful";
        Record record = parser.parseRecord(payload);
        assertNotNull(record);

        assertEquals("Unknown", record.getDbName());
        assertEquals(35050, record.getSessionLocator().getClientPort());
        assertEquals("172.17.0.1", record.getSessionLocator().getClientIp());
        assertEquals("tcp", record.getAccessor().getDbProtocol());
        assertEquals("Unknown", record.getAccessor().getDbUser());
        assertEquals("Milvus", record.getAccessor().getServerType());
        assertEquals("Time{timstamp=1743635661017, minOffsetFromGMT=0, minDst=0}", record.getTime().toString());
        try {
            String expected = new String(Files.readAllBytes(Paths.get("src/test/resources/milvusGRPCMessage_test1.txt")));
            assertEquals(expected, record.getData().getOriginalSqlCommand());
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        assertNull(record.getException());
    }

    @Test
    void test2() {
        String payload = "LEEF:1.0|Zilliz|Milvus|1.0|Connect-GrpcUnauthenticated|devTime=2025/02/06 21:20:05.353 +00:00\tdevTimeFormat=yyyy/MM/dd HH:mm:ss.SSS xxx\tuserName=Unknown\tuserAddress=tcp-172.17.0.1:46936\tdatabaseName=TestDB\tqueryExpression=Unknown\terrorCode=65535\terrorMessage=rpc error: code = Unauthenticated desc = auth check failure, please check api key is correct";
        Record record = parser.parseRecord(payload);
        assertNotNull(record);

        assertEquals("TestDB", record.getDbName());
        assertEquals(46936, record.getSessionLocator().getClientPort());
        assertEquals("172.17.0.1", record.getSessionLocator().getClientIp());
        assertEquals("tcp", record.getAccessor().getDbProtocol());
        assertEquals("Unknown", record.getAccessor().getDbUser());
        assertEquals("Milvus", record.getAccessor().getServerType());
        assertEquals("Time{timstamp=1738876805353, minOffsetFromGMT=0, minDst=0}", record.getTime().toString());
        assertEquals("SQL_ERROR", record.getException().getExceptionTypeId());
        try {
            String expected = new String(Files.readAllBytes(Paths.get("src/test/resources/milvusGRPCMessage_test2.txt")));
            assertEquals(expected, record.getData().getOriginalSqlCommand());
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        assertEquals("rpc error: code = Unauthenticated desc = auth check failure, please check api key is correct",
                record.getException().getDescription());
    }

    @Test
    void test3() {
        String payload = "LEEF:1.0|Zilliz|Milvus|1.0|DropAlias-GrpcPermissionDenied|devTime=2025/02/06 21:38:53.557 +00:00\tdevTimeFormat=yyyy/MM/dd HH:mm:ss.SSS xxx\tuserName=zilliz\tuserAddress=tcp-172.17.0.1:47286\tdatabaseName=default\tqueryExpression=Unknown\terrorCode=65535\terrorMessage=rpc error: code = PermissionDenied desc = PrivilegeDropAlias: permission deny to zilliz in the `default` database";
        Record record = parser.parseRecord(payload);
        assertNotNull(record);

        assertEquals("default", record.getDbName());
        assertEquals(47286, record.getSessionLocator().getClientPort());
        assertEquals("172.17.0.1", record.getSessionLocator().getClientIp());
        assertEquals("tcp", record.getAccessor().getDbProtocol());
        assertEquals("zilliz", record.getAccessor().getDbUser());
        assertEquals("Milvus", record.getAccessor().getServerType());
        assertEquals("Time{timstamp=1738877933557, minOffsetFromGMT=0, minDst=0}", record.getTime().toString());
        assertEquals("SQL_ERROR", record.getException().getExceptionTypeId());
        assertEquals(
                "rpc error: code = PermissionDenied desc = PrivilegeDropAlias: permission deny to zilliz in the `default` database",
                record.getException().getDescription());
    }

    @Test
    void testMoreLogs() {
        String filePath = "src/test/resources/logs.txt";
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String payload;
            while ((payload = reader.readLine()) != null) {
                Record record = parser.parseRecord(payload);

                assertNotEquals("", record.getDbName());
                assertNotEquals(-1, record.getSessionLocator().getClientPort());
                assertNotEquals("0.0.0.0", record.getSessionLocator().getClientIp());
                assertNotEquals("", record.getAccessor().getDbProtocol());
                assertNotEquals("", record.getAccessor().getDbUser());
                assertEquals("Milvus", record.getAccessor().getServerType());
                assertNotEquals("", record.getTime().toString());
                if (!payload.contains("errorCode=0")) {
                    assertEquals("SQL_ERROR", record.getException().getExceptionTypeId());
                    assertNotEquals("", record.getException().getSqlString());
                    assertNotEquals("", record.getException().getDescription());
                } else {
                    assertNull(record.getException());
                    assertNotEquals("", record.getData().getOriginalSqlCommand());
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }
    }

    private void assertJsonEquals(String expectedJson, String actualJson) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> expectedMap = objectMapper.readValue(expectedJson, Map.class);
        Map<String, Object> actualMap = objectMapper.readValue(actualJson, Map.class);

        assertEquals(expectedMap, actualMap, "JSON content does not match!");
    }

}
