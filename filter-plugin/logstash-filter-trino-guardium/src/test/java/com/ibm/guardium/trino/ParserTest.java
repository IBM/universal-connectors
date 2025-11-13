package com.ibm.guardium.trino;

import com.google.gson.Gson;
import com.ibm.guardium.universalconnector.commons.structures.Accessor;
import com.ibm.guardium.universalconnector.commons.structures.Record;
import com.ibm.guardium.universalconnector.commons.structures.SessionLocator;
import org.junit.jupiter.api.Test;
import com.google.gson.JsonObject;
import org.w3c.dom.UserDataHandler;

import static org.junit.jupiter.api.Assertions.*;

class ParserTest {

  @Test
  void testRecord() {
    String payload =
            "{\"createTime\":\"2025-06-09T18:26:41.470Z\",\"context\":{\"user\":\"trino\",\"originalUser\":\"trino\",\"principal\":\"trino\",\"schema\":\"my_database1\",\"enabledRoles\":[],\"groups\":[],\"remoteClientAddress\":\"127.0.0.1\",\"userAgent\":\"trino-cli\",\"clientTags\":[],\"clientCapabilities\":[\"PATH\",\"PARAMETRIC_DATETIME\",\"SESSION_AUTHORIZATION\"],\"source\":\"trino-cli\",\"timezone\":\"UTC\",\"resourceGroupId\":[\"global\"],\"sessionProperties\":{},\"resourceEstimates\":{},\"serverAddress\":\"172.19.0.6\",\"serverVersion\":\"466\",\"environment\":\"production\",\"queryType\":\"SELECT\",\"retryPolicy\":\"NONE\"},\"metadata\":{\"queryId\":\"20250609_182641_00011_mtb2e\",\"transactionId\":\"03463dad-f1e8-4915-885c-d3a1c2bdedd6\",\"query\":\"SELECT * FROM hive.test_db.test_table LIMIT 10\",\"queryState\":\"QUEUED\",\"tables\":[{\"catalog\":\"hive\",\"schema\":\"my_database1\",\"table\":\"test_table\",\"authorization\":\"trino\",\"filters\":[],\"columns\":[{\"column\":\"city\"},{\"column\":\"name\"},{\"column\":\"id\"},{\"column\":\"age\"}],\"directlyReferenced\":true,\"referenceChain\":[]}],\"routines\":[],\"uri\":\"http://172.19.0.6:8080/v1/query/20250609_182641_00011_mtb2e\"}}";

    final JsonObject data = new Gson().fromJson(payload, JsonObject.class);
    Record record = Parser.parseRecord(data);

    assertNotNull(record);

    assertEquals("my_database1", record.getDbName());
    assertEquals(-1, record.getSessionLocator().getClientPort());
    assertEquals("127.0.0.1", record.getSessionLocator().getClientIp());
    assertEquals("Trino", record.getAccessor().getDbProtocol());
    assertEquals("172.19.0.6", record.getSessionLocator().getServerIp());
    assertEquals(-1, record.getSessionLocator().getServerPort());
    assertEquals("trino", record.getAccessor().getDbUser());
    assertEquals("Trino", record.getAccessor().getServerType());
    assertEquals(
            "SELECT * FROM hive.test_db.test_table LIMIT 10", record.getData().getOriginalSqlCommand());
    assertEquals(1749493601470L, record.getTime().getTimstamp());
    assertEquals("my_database1", record.getAccessor().getServiceName());
    assertEquals(record.getDbName(), record.getAccessor().getServiceName());
  }

  @Test
  void testSessionLocatorEmpty_case1() {
    String payload =
            "{\"context\":{\"user\":\"trino\",\"originalUser\":\"trino\",\"principal\":\"trino\",\"enabledRoles\":[],\"groups\":[],\"userAgent\":\"trino-cli\",\"clientTags\":[],\"clientCapabilities\":[\"PATH\",\"PARAMETRIC_DATETIME\",\"SESSION_AUTHORIZATION\"],\"source\":\"trino-cli\",\"timezone\":\"UTC\",\"resourceGroupId\":[\"global\"],\"sessionProperties\":{},\"resourceEstimates\":{},\"serverVersion\":\"466\",\"environment\":\"production\",\"queryType\":\"SELECT\",\"retryPolicy\":\"NONE\"},\"url\":{\"path\":\"/\", \"domain\":\"host.docker.internal\"},\"metadata\":{\"queryId\":\"20250605_195009_00010_34b3y\",\"transactionId\":\"b69e0f30-de9a-4dd3-b05f-f75566f1d6d1\",\"query\":\"SELECT * FROM hive.test_db.test_table LIMIT 10\",\"queryState\":\"FINISHED\",\"tables\":[],\"routines\":[]}}";

    final JsonObject data = new Gson().fromJson(payload, JsonObject.class);
    SessionLocator record = Parser.getSessionLocator(data);
    assertEquals("0.0.0.0", record.getClientIp());
    assertEquals("0.0.0.0", record.getServerIp());
    assertEquals(-1, record.getClientPort());
    assertEquals(-1, record.getServerPort());
  }

  @Test
  void testSessionLocatorEmpty_case2() {
    String payload = "{\"DatabaseName\":\"db_name\",\"createTime\":\"2025-06-05T19:50:09.402Z\"}";
    final JsonObject data = new Gson().fromJson(payload, JsonObject.class);
    SessionLocator record = Parser.getSessionLocator(data);
    assertEquals("0.0.0.0", record.getClientIp());
    assertEquals("0.0.0.0", record.getServerIp());
    assertEquals(-1, record.getClientPort());
    assertEquals(-1, record.getServerPort());
  }

  @Test
  void testDBUser() {
    String payload = "{\"DatabaseName\":\"db_name\",\"createTime\":\"2025-06-05T19:50:09.402Z\"}";
    final JsonObject data = new Gson().fromJson(payload, JsonObject.class);
    Accessor record = Parser.getAccessor(data, "N.A.");
    assertEquals("N.A.", record.getDbUser());
  }

  @Test
  void testGetException() {
    String payload =
            "{\"createTime\":\"2025-06-09T18:26:41.470Z\",\"context\":{\"user\":\"trino\",\"originalUser\":\"trino\",\"principal\":\"trino\",\"enabledRoles\":[],\"groups\":[],\"remoteClientAddress\":\"127.0.0.1\",\"userAgent\":\"trino-cli\",\"clientTags\":[],\"clientCapabilities\":[\"PATH\",\"PARAMETRIC_DATETIME\",\"SESSION_AUTHORIZATION\"],\"source\":\"trino-cli\",\"timezone\":\"UTC\",\"resourceGroupId\":[\"global\"],\"sessionProperties\":{},\"resourceEstimates\":{},\"serverAddress\":\"172.19.0.6\",\"serverVersion\":\"466\",\"environment\":\"production\",\"queryType\":\"SELECT\",\"retryPolicy\":\"NONE\"},\"metadata\":{\"queryId\":\"20250609_182641_00011_mtb2e\",\"transactionId\":\"03463dad-f1e8-4915-885c-d3a1c2bdedd6\",\"query\":\"INSERT INTO hive.test_db.test_table VALUES (17, 'John Doe', 30, 'Engineer')\",\"queryState\":\"QUEUED\",\"tables\":[{\"catalog\":\"hive\",\"schema\":\"test_db\",\"table\":\"test_table\",\"authorization\":\"trino\",\"filters\":[],\"columns\":[{\"column\":\"city\"},{\"column\":\"name\"},{\"column\":\"id\"},{\"column\":\"age\"}],\"directlyReferenced\":true,\"referenceChain\":[]}],\"routines\":[],\"uri\":\"http://172.19.0.6:8080/v1/query/20250609_182641_00011_mtb2e\"},\"failureInfo\":{\"errorCode\":{\"code\":16777232,\"name\":\"HIVE_FILESYSTEM_ERROR\",\"type\":\"EXTERNAL\",\"fatal\":false},\"failureType\":\"io.trino.spi.TrinoException\",\"failureMessage\":\"Error moving data files from hdfs://hadoop-hive:9000/tmp/presto-trino/eb708b7a-5685-4a07-9d72-26d1760567b5/20250610_022922_00018_mtb2e_9e3c81b8-10b3-45fd-8103-6a45610ee8ee.gz to final location hdfs://hadoop-hive:9000/user/hive/warehouse/test_db.db/test_table/20250610_022922_00018_mtb2e_9e3c81b8-10b3-45fd-8103-6a45610ee8ee.gz\"}}";
    final JsonObject data = new Gson().fromJson(payload, JsonObject.class);
    Record record = Parser.parseRecord(data);

    assertNotNull(record.getException());
    assertEquals(
            "INSERT INTO hive.test_db.test_table VALUES (17, 'John Doe', 30, 'Engineer')",
            record.getException().getSqlString());
    assertEquals(
            "Error moving data files from hdfs://hadoop-hive:9000/tmp/presto-trino/eb708b7a-5685-4a07-9d72-26d1760567b5/20250610_022922_00018_mtb2e_9e3c81b8-10b3-45fd-8103-6a45610ee8ee.gz to final location hdfs://hadoop-hive:9000/user/hive/warehouse/test_db.db/test_table/20250610_022922_00018_mtb2e_9e3c81b8-10b3-45fd-8103-6a45610ee8ee.gz",
            record.getException().getDescription());
    assertEquals("SQL_ERROR", record.getException().getExceptionTypeId());
  }
}
