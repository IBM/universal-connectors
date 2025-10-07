package com.ibm.guardium.alloydb;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibm.guardium.universalconnector.commons.custom_parsing.ParserFactory;
import com.ibm.guardium.universalconnector.commons.structures.Record;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ParserTest {

  Parser parser = new Parser(ParserFactory.ParserType.json);

  @Test
  void testSqlExecution() {
    String payload =
        "{\n"
            + "    \"textPayload\": \"2025-05-05 19:10:44.336 UTC [70374]: [1-1] db=alloydb_postgresql,user=postgres LOG: [postgres.c:1187] statement: SELECT * FROM customers\",\n"
            + "    \"insertId\": \"s=21bfbbc9ae684df79c81633b63e1fb33;i=18d4f1;b=488acbc40da24f1c85d08b22a1969f2b;m=3cf02fe71;t=6346841bbd621;x=1f898f7d8bd2cd3-0-0@a1\",\n"
            + "    \"resource\": {\n"
            + "        \"type\": \"alloydb.googleapis.com/Instance\",\n"
            + "        \"labels\": {\n"
            + "            \"location\": \"us-central1\",\n"
            + "            \"resource_container\": \"projects/485533885456\",\n"
            + "            \"cluster_id\": \"alloydb-postgresql\",\n"
            + "            \"instance_id\": \"alloydb-postgresql-instance\"\n"
            + "        }\n"
            + "    },\n"
            + "    \"timestamp\": \"2025-05-05T19:10:44.336161Z\",\n"
            + "    \"severity\": \"INFO\",\n"
            + "    \"labels\": {\n"
            + "        \"DATABASE_VERSION\": \"POSTGRES_15\",\n"
            + "        \"CONSUMER_PROJECT_NUMBER\": \"485533885456\",\n"
            + "        \"CONSUMER_PROJECT\": \"charged-mind-281913\",\n"
            + "        \"NODE_ID\": \"kvqd\"\n"
            + "    },\n"
            + "    \"logName\": \"projects/charged-mind-281913/logs/alloydb.googleapis.com%2Fpostgres.log\",\n"
            + "    \"receiveTimestamp\": \"2025-05-05T19:11:05.292741654Z\"\n"
            + "}";
    Record record = parser.parseRecord(payload);
    assertNotNull(record);
    assertEquals("alloydb_postgresql", record.getDbName());
    assertEquals(-1, record.getSessionLocator().getClientPort());
    assertEquals("0.0.0.0", record.getSessionLocator().getClientIp());
    assertEquals(-1, record.getSessionLocator().getServerPort());
    assertEquals("0.0.0.0", record.getSessionLocator().getServerIp());
    assertEquals("ALLOYDB", record.getAccessor().getDbProtocol());
    assertEquals("postgres", record.getAccessor().getDbUser());
    assertEquals("ALLOYDB", record.getAccessor().getServerType());
    assertEquals(
        "alloydb_postgresql",
        record.getAccessor().getServiceName());
    assertEquals(
        "Time{timstamp=1746472244336, minOffsetFromGMT=0, minDst=0}", record.getTime().toString());
    assertNull(record.getException());
    assertEquals("SELECT * FROM customers", record.getData().getOriginalSqlCommand());
    assertEquals("PGRS", record.getAccessor().getLanguage());
  }

  @Test
  void testError() {

    String payload =
        "{\n"
            + "    \"textPayload\": \"2025-05-05 16:38:52.621 UTC [32230]: [1-1] db=alloydb_postgresql,user=postgres ERROR:[parse_relation.c:1395]relation \\\"cars5\\\" does not exist at character 15\",\n"
            + "    \"insertId\": \"s=21bfbbc9ae684df79c81633b63e1fb33;i=b0a8c;b=488acbc40da24f1c85d08b22a1969f2b;m=1afe942f8;t=6346622a21aa8;x=588441c25ef80d7f-0-0@a1\",\n"
            + "    \"resource\": {\n"
            + "        \"type\": \"alloydb.googleapis.com/Instance\",\n"
            + "        \"labels\": {\n"
            + "            \"cluster_id\": \"alloydb-postgresql\",\n"
            + "            \"location\": \"us-central1\",\n"
            + "            \"instance_id\": \"alloydb-postgresql\",\n"
            + "            \"resource_container\": \"projects/485533885456\"\n"
            + "        }\n"
            + "    },\n"
            + "    \"timestamp\": \"2025-05-05T16:38:52.621480Z\",\n"
            + "    \"severity\": \"ERROR\",\n"
            + "    \"labels\": {\n"
            + "        \"CONSUMER_PROJECT_NUMBER\": \"485533885456\",\n"
            + "        \"CONSUMER_PROJECT\": \"charged-mind-281913\",\n"
            + "        \"DATABASE_VERSION\": \"POSTGRES_15\",\n"
            + "        \"NODE_ID\": \"kvqd\"\n"
            + "    },\n"
            + "    \"logName\": \"projects/charged-mind-281913/logs/alloydb.googleapis.com%2Fpostgres.log\",\n"
            + "    \"receiveTimestamp\": \"2025-05-05T16:38:53.360493534Z\"\n"
            + "}";
    Record record = parser.parseRecord(payload);
    assertNotNull(record);
    assertEquals("alloydb_postgresql", record.getDbName());
    assertEquals(-1, record.getSessionLocator().getClientPort());
    assertEquals("0.0.0.0", record.getSessionLocator().getClientIp());
    assertEquals(-1, record.getSessionLocator().getServerPort());
    assertEquals("0.0.0.0", record.getSessionLocator().getServerIp());
    assertFalse(record.getSessionLocator().isIpv6());
    assertEquals("ALLOYDB", record.getAccessor().getDbProtocol());
    assertEquals("postgres", record.getAccessor().getDbUser());
    assertEquals("ALLOYDB", record.getAccessor().getServerType());
    assertEquals(
        "Time{timstamp=1746463132621, minOffsetFromGMT=0, minDst=0}", record.getTime().toString());
    assertNotNull(record.getException());
    assertEquals("SQL_ERROR", record.getException().getExceptionTypeId());
    assertEquals(
        "[parse_relation.c:1395]relation \"cars5\" does not exist at character 15",
        record.getException().getDescription());
    assertEquals(
        "alloydb_postgresql",
        record.getAccessor().getServiceName());
  }

  @Test
  void testLoginFailed() {
    String payload =
        "{\n"
            + "    \"textPayload\": \"2025-06-19 16:40:44.090 UTC [324511]: [1-1] db=postgres,user=postgres FATAL:[auth.c:372] password authentication failed for user \\\"postgres\\\"\",\n"
            + "    \"insertId\": \"s=81da2c12023949fd8a8f279760f1aa98;i=7a5314;b=f0731cb0ca664583b8609af6ac5059d7;m=136726d1ac;t=637ef6822df2a;x=7b81f3739ae5686b-0-0@a1\",\n"
            + "    \"resource\": {\n"
            + "        \"type\": \"alloydb.googleapis.com/Instance\",\n"
            + "        \"labels\": {\n"
            + "            \"cluster_id\": \"alloydb-test\",\n"
            + "            \"instance_id\": \"alloydb-test-primary\",\n"
            + "            \"location\": \"us-central1\",\n"
            + "            \"resource_container\": \"projects/485533885456\"\n"
            + "        }\n"
            + "    },\n"
            + "    \"timestamp\": \"2025-06-19T16:40:44.091178Z\",\n"
            + "    \"severity\": \"ALERT\",\n"
            + "    \"labels\": {\n"
            + "        \"CONSUMER_PROJECT\": \"charged-mind-281913\",\n"
            + "        \"NODE_ID\": \"m3df\",\n"
            + "        \"CONSUMER_PROJECT_NUMBER\": \"485533885456\",\n"
            + "        \"DATABASE_VERSION\": \"POSTGRES_16\"\n"
            + "    },\n"
            + "    \"logName\": \"projects/charged-mind-281913/logs/alloydb.googleapis.com%2Fpostgres.log\",\n"
            + "    \"receiveTimestamp\": \"2025-06-19T16:40:45.240192087Z\"\n"
            + "}";

    Record record = parser.parseRecord(payload);
    assertNotNull(record);
    assertNotNull(record.getException());
    assertEquals("LOGIN_FAILED", record.getException().getExceptionTypeId());
    assertEquals(
        "[auth.c:372] password authentication failed for user \"postgres\"",
        record.getException().getDescription());
  }

  @Test
  void testFailedSQL() {
    String payload =
        "{\n"
            + "  \"textPayload\": \"2025-09-17 18:32:35.153 UTC [2918167]: [3-1] db=postgres,user=postgres STATEMENT:  select * from employs;\",\n"
            + "  \"insertId\": \"s=e839ffda58874d68b3330617daee3ed8;i=4f01bd8;b=e9292a297c8c447ea7efe8b2bb0b6a03;m=a35f9c87ac;t=63f0375dd5100;x=4ccb2771207ce73f-0-0@a1\",\n"
            + "  \"resource\": {\n"
            + "    \"type\": \"alloydb.googleapis.com/Instance\",\n"
            + "    \"labels\": {\n"
            + "      \"resource_container\": \"projects/485533885456\",\n"
            + "      \"instance_id\": \"glenn-alloydb-cluster-primary\",\n"
            + "      \"cluster_id\": \"glenn-alloydb-cluster\",\n"
            + "      \"location\": \"us-east4\"\n"
            + "    }\n"
            + "  },\n"
            + "  \"timestamp\": \"2025-09-17T18:32:35.154176Z\",\n"
            + "  \"severity\": \"ERROR\",\n"
            + "  \"labels\": {\n"
            + "    \"CONSUMER_PROJECT_NUMBER\": \"485533885456\",\n"
            + "    \"CONSUMER_PROJECT\": \"charged-mind-281913\",\n"
            + "    \"DATABASE_VERSION\": \"POSTGRES_16\",\n"
            + "    \"NODE_ID\": \"k351\"\n"
            + "  },\n"
            + "  \"logName\": \"projects/charged-mind-281913/logs/alloydb.googleapis.com%2Fpostgres.log\",\n"
            + "  \"receiveTimestamp\": \"2025-09-17T18:33:06.249293611Z\"\n"
            + "}";
    Record record = parser.parseRecord(payload);
    assertNotNull(record);
    assertNotNull(record.getException());
    assertEquals("select * from employs;", record.getException().getSqlString());
  }
}
