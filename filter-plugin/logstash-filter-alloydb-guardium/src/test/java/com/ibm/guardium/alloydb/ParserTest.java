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
            + "        \"CONSUMER_PROJECT\": \"project-name\",\n"
            + "        \"NODE_ID\": \"kvqd\"\n"
            + "    },\n"
            + "    \"logName\": \"projects/project-name/logs/alloydb.googleapis.com%2Fpostgres.log\",\n"
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
    assertEquals("alloydb_postgresql", record.getAccessor().getServiceName());
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
            + "        \"CONSUMER_PROJECT\": \"project-name\",\n"
            + "        \"DATABASE_VERSION\": \"POSTGRES_15\",\n"
            + "        \"NODE_ID\": \"kvqd\"\n"
            + "    },\n"
            + "    \"logName\": \"projects/project-name/logs/alloydb.googleapis.com%2Fpostgres.log\",\n"
            + "    \"receiveTimestamp\": \"2025-05-05T16:38:53.360493534Z\"\n"
            + "}";
    Record record = parser.parseRecord(payload);
    assertNotNull(record);
    assertEquals("alloydb_postgresql", record.getDbName());
    assertEquals(-1, record.getSessionLocator().getClientPort());
    assertEquals("0.0.0.0", record.getSessionLocator().getClientIp());
    assertEquals(-1, record.getSessionLocator().getServerPort());
    assertFalse(record.getSessionLocator().isIpv6());
    assertEquals("0.0.0.0", record.getSessionLocator().getServerIp());
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
    assertEquals("alloydb_postgresql", record.getAccessor().getServiceName());
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
            + "        \"CONSUMER_PROJECT\": \"project-name\",\n"
            + "        \"NODE_ID\": \"m3df\",\n"
            + "        \"CONSUMER_PROJECT_NUMBER\": \"485533885456\",\n"
            + "        \"DATABASE_VERSION\": \"POSTGRES_16\"\n"
            + "    },\n"
            + "    \"logName\": \"projects/project-name/logs/alloydb.googleapis.com%2Fpostgres.log\",\n"
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
            + "    \"CONSUMER_PROJECT\": \"project-name\",\n"
            + "    \"DATABASE_VERSION\": \"POSTGRES_16\",\n"
            + "    \"NODE_ID\": \"k351\"\n"
            + "  },\n"
            + "  \"logName\": \"projects/project-name/logs/alloydb.googleapis.com%2Fpostgres.log\",\n"
            + "  \"receiveTimestamp\": \"2025-09-17T18:33:06.249293611Z\"\n"
            + "}";
    Record record = parser.parseRecord(payload);
    assertNotNull(record);
    assertNotNull(record.getException());
    assertEquals("select * from employs;", record.getException().getSqlString());
  }

  @Test
  void test2() {
    String payload =
        "{\n"
            + "  \"textPayload\": \"2025-10-16 09:39:40.843 UTC [408063]: [1-1] db=postgres,user=postgres LOG:  [postgres.c:1214]  statement: CREATE TABLE employees (\\n    id SERIAL PRIMARY KEY,\\n    name VARCHAR(50),\\n    department VARCHAR(50),\\n    salary NUMERIC(10,2)\\n);\\n\\n\\nINSERT INTO employees (name, department, salary) VALUES\\n('Alice', 'Engineering', 90000),\\n('Bob', 'Marketing', 75000),\\n('Charlie', 'Finance', 82000),\\n('Diana', 'Engineering', 95000);\\n\\n\\nSELECT * FROM employees;\\n\\n\\nUPDATE employees\\nSET salary = 98000\\nWHERE name = 'Diana';\\n\\n\\nSELECT * FROM employees WHERE name = 'Diana';\\n\\n\\nDELETE FROM employees\\nWHERE name = 'Bob';\\n\\n\\nSELECT * FROM employees;\\n\\nDROP TABLE employees;\\n\",\n"
            + "  \"insertId\": \"s=935ad8a1f4a1460aa2d252313f784084;i=b224ca;b=39deecf7c2a1461893339287af93c848;m=169d8a1ad7;t=641436571f066;x=f13b8397a707becc-0-0@a1\",\n"
            + "  \"resource\": {\n"
            + "    \"type\": \"alloydb.googleapis.com/Instance\",\n"
            + "    \"labels\": {\n"
            + "      \"location\": \"us-central1\",\n"
            + "      \"cluster_id\": \"my-cluster\",\n"
            + "      \"instance_id\": \"my-cluster-primary\",\n"
            + "      \"resource_container\": \"projects/485533885456\"\n"
            + "    }\n"
            + "  },\n"
            + "  \"timestamp\": \"2025-10-16T09:39:40.844134Z\",\n"
            + "  \"severity\": \"INFO\",\n"
            + "  \"labels\": {\n"
            + "    \"NODE_ID\": \"xq84\",\n"
            + "    \"CONSUMER_PROJECT\": \"project-name\",\n"
            + "    \"DATABASE_VERSION\": \"POSTGRES_16\",\n"
            + "    \"CONSUMER_PROJECT_NUMBER\": \"485533885456\"\n"
            + "  },\n"
            + "  \"logName\": \"projects/project-name/logs/alloydb.googleapis.com%2Fpostgres.log\",\n"
            + "  \"receiveTimestamp\": \"2025-10-16T09:39:41.849130289Z\"\n"
            + "}";

    Record record = parser.parseRecord(payload);
    assertNotNull(record);
    assertEquals("postgres", record.getDbName());

    assertEquals("postgres", record.getAccessor().getDbUser());
    assertEquals("ALLOYDB", record.getAccessor().getServerType());
    assertEquals("postgres", record.getAccessor().getServiceName());

    assertNull(record.getException());
    assertEquals(
        "CREATE TABLE employees (id SERIAL PRIMARY KEY, name VARCHAR(50), department VARCHAR(50), salary NUMERIC(10,2)); INSERT INTO employees (name, department, salary) VALUES ('Alice', 'Engineering', 90000), ('Bob', 'Marketing', 75000), ('Charlie', 'Finance', 82000), ('Diana', 'Engineering', 95000); SELECT * FROM employees; UPDATE employees SET salary = 98000 WHERE name = 'Diana'; SELECT * FROM employees WHERE name = 'Diana'; DELETE FROM employees WHERE name = 'Bob'; SELECT * FROM employees; DROP TABLE employees;",
        record.getData().getOriginalSqlCommand());
    assertEquals("PGRS", record.getAccessor().getLanguage());
  }

  @Test
  void test3() {
    String payload =
        "{\n"
            + "  \"textPayload\": \"2025-10-17 10:48:39.823 UTC [780051]: [347-1] db=postgres,user=postgres LOG:  [postgres.c:2408]  execute <unnamed>: SELECT COUNT(*) AS \\\"RECORDS\\\" FROM PANY\",\n"
            + "  \"insertId\": \"s=935ad8a1f4a1460aa2d252313f784084;i=15403ce;b=39deecf7c2a1461893339287af93c848;m=2bb2154cbb;t=6415879fd2249;x=8840df928313b684-0-0@a1\",\n"
            + "  \"resource\": {\n"
            + "    \"type\": \"alloydb.googleapis.com/Instance\",\n"
            + "    \"labels\": {\n"
            + "      \"cluster_id\": \"my-cluster\",\n"
            + "      \"resource_container\": \"projects/485533885456\",\n"
            + "      \"instance_id\": \"my-cluster-primary\",\n"
            + "      \"location\": \"us-central1\"\n"
            + "    }\n"
            + "  },\n"
            + "  \"timestamp\": \"2025-10-17T10:48:39.823945Z\",\n"
            + "  \"severity\": \"INFO\",\n"
            + "  \"labels\": {\n"
            + "    \"NODE_ID\": \"xq84\",\n"
            + "    \"DATABASE_VERSION\": \"POSTGRES_16\",\n"
            + "    \"CONSUMER_PROJECT\": \"project-name\",\n"
            + "    \"CONSUMER_PROJECT_NUMBER\": \"485533885456\"\n"
            + "  },\n"
            + "  \"logName\": \"projects/project-name/logs/alloydb.googleapis.com%2Fpostgres.log\",\n"
            + "  \"receiveTimestamp\": \"2025-10-17T10:48:40.901838976Z\"\n"
            + "}";

    Record record = parser.parseRecord(payload);
    assertNotNull(record);
    assertEquals("postgres", record.getDbName());

    assertEquals("postgres", record.getAccessor().getDbUser());
    assertEquals("ALLOYDB", record.getAccessor().getServerType());
    assertEquals("postgres", record.getAccessor().getServiceName());

    assertNull(record.getException());
    assertEquals(
        "SELECT COUNT(*) AS \"RECORDS\" FROM PANY", record.getData().getOriginalSqlCommand());
  }

  @Test
  void test4() {
    String payload =
        "{\n"
            + "  \"textPayload\": \"2025-10-16 04:22:05.937 UTC [328339]: [1-1] db=guestbook,user=postgres LOG:  [postgres.c:1214]  statement: CREATE TABLE PANY(ID INT PRIMARY KEY NOT NULL,NAME TEXT NOT NULL,AGE INT NOT NULL,ADDRESS CHAR(50),SALARY REAL);\\nCREATE TABLE DPT(ID INT PRIMARY KEY NOT NULL,DEPT CHAR(50) NOT NULL,EMP_ID INT NOT NULL);\\nINSERT INTO PANY (ID,NAME,AGE,ADDRESS,SALARY) VALUES (1, 'Paul', 32, 'California', 20000.00);\\nINSERT INTO PANY (ID,NAME,AGE,ADDRESS) VALUES (2, 'Allen', 25, 'Texas');\\nINSERT INTO PANY (ID,NAME,AGE,ADDRESS,SALARY) VALUES (3, 'Teddy', 23, 'Norway', 20000.00 );\\nINSERT INTO PANY (ID,NAME,AGE,ADDRESS,SALARY) VALUES (4, 'Mark', 25, 'Rich-Mond ', 65000.00 ), (5, 'David', 27, 'Texas', 85000.00);\\nSELECT ID, NAME, SALARY FROM PANY;\\nSELECT * FROM PANY WHERE SALARY = 10000;\\nSELECT COUNT(*) AS \\\"RECORDS\\\" FROM PANY;\\nSELECT * FROM PANY WHERE AGE >= 25 AND SALARY >= 65000;\\nSELECT * FROM PANY WHERE AGE >= 25 OR SALARY >= 65000;\\nSELECT * FROM PANY WHERE AGE IS NOT NULL;\\nSELECT * FROM PANY WHERE NAME LIKE 'Pa%';\\nSELECT * FROM PANY WHERE AGE IN ( 25, 27 );\\nSELECT * FROM PANY WHERE AGE NOT IN ( 25, 27 );\\nSELECT * FROM PANY WHERE AGE BETWEEN 25 AND 27;\\nUPDATE PANY SET SALARY = 15000 WHERE ID = 3;\\nUPDATE PANY SET ADDRESS = 'Texas', SALARY=20000;\\nDELETE FROM PANY WHERE ID = 2;\\n\\ndrop table PANY;\\ndrop table DPT;\",\n"
            + "  \"insertId\": \"s=935ad8a1f4a1460aa2d252313f784084;i=8ff679;b=39deecf7c2a1461893339287af93c848;m=122dc74f75;t=6413ef5af2504;x=1cfa30b9194c0d1d-0-0@a1\",\n"
            + "  \"resource\": {\n"
            + "    \"type\": \"alloydb.googleapis.com/Instance\",\n"
            + "    \"labels\": {\n"
            + "      \"cluster_id\": \"my-cluster\",\n"
            + "      \"location\": \"us-central1\",\n"
            + "      \"instance_id\": \"my-cluster-primary\",\n"
            + "      \"resource_container\": \"projects/485533885456\"\n"
            + "    }\n"
            + "  },\n"
            + "  \"timestamp\": \"2025-10-16T04:22:05.937924Z\",\n"
            + "  \"severity\": \"INFO\",\n"
            + "  \"labels\": {\n"
            + "    \"CONSUMER_PROJECT_NUMBER\": \"485533885456\",\n"
            + "    \"DATABASE_VERSION\": \"POSTGRES_16\",\n"
            + "    \"CONSUMER_PROJECT\": \"project-name\",\n"
            + "    \"NODE_ID\": \"xq84\"\n"
            + "  },\n"
            + "  \"logName\": \"projects/project-name/logs/alloydb.googleapis.com%2Fpostgres.log\",\n"
            + "  \"receiveTimestamp\": \"2025-10-16T04:22:06.439003426Z\"\n"
            + "}";

    Record record = parser.parseRecord(payload);
    assertNotNull(record);
    assertEquals("guestbook", record.getDbName());
    assertEquals("guestbook", record.getAccessor().getServiceName());
    assertEquals("postgres", record.getAccessor().getDbUser());
    assertEquals("ALLOYDB", record.getAccessor().getServerType());

    assertNull(record.getException());
    assertEquals(
        "CREATE TABLE PANY(ID INT PRIMARY KEY NOT NULL,NAME TEXT NOT NULL,AGE INT NOT NULL,ADDRESS CHAR(50),SALARY REAL); CREATE TABLE DPT(ID INT PRIMARY KEY NOT NULL,DEPT CHAR(50) NOT NULL,EMP_ID INT NOT NULL); INSERT INTO PANY (ID,NAME,AGE,ADDRESS,SALARY) VALUES (1, 'Paul', 32, 'California', 20000.00); INSERT INTO PANY (ID,NAME,AGE,ADDRESS) VALUES (2, 'Allen', 25, 'Texas'); INSERT INTO PANY (ID,NAME,AGE,ADDRESS,SALARY) VALUES (3, 'Teddy', 23, 'Norway', 20000.00); INSERT INTO PANY (ID,NAME,AGE,ADDRESS,SALARY) VALUES (4, 'Mark', 25, 'Rich-Mond ', 65000.00), (5, 'David', 27, 'Texas', 85000.00); SELECT ID, NAME, SALARY FROM PANY; SELECT * FROM PANY WHERE SALARY = 10000; SELECT COUNT(*) AS \"RECORDS\" FROM PANY; SELECT * FROM PANY WHERE AGE >= 25 AND SALARY >= 65000; SELECT * FROM PANY WHERE AGE >= 25 OR SALARY >= 65000; SELECT * FROM PANY WHERE AGE IS NOT NULL; SELECT * FROM PANY WHERE NAME LIKE 'Pa%'; SELECT * FROM PANY WHERE AGE IN (25, 27); SELECT * FROM PANY WHERE AGE NOT IN (25, 27); SELECT * FROM PANY WHERE AGE BETWEEN 25 AND 27; UPDATE PANY SET SALARY = 15000 WHERE ID = 3; UPDATE PANY SET ADDRESS = 'Texas', SALARY=20000; DELETE FROM PANY WHERE ID = 2; drop table PANY; drop table DPT;",
        record.getData().getOriginalSqlCommand());
  }
}
