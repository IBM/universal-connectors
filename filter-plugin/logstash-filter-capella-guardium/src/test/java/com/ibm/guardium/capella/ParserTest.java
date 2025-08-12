package com.ibm.guardium.capella;

import com.ibm.guardium.universalconnector.commons.custom_parsing.ParserFactory;
import com.ibm.guardium.universalconnector.commons.structures.Record;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ParserTest {
  Parser parser = new Parser(ParserFactory.ParserType.json);

  /**
   * testAuthenticationSuccessPayload tests that we can parse the "authentication succeeded" events.
   */
  @Test
  void testAuthenticationSuccessPayload() {
    String payload =
        "{\"description\":\"Authentication to the cluster succeeded\",\"id\":20485,\"local\":{\"ip\":\"10.0.0.11\",\"port\":11207},\"name\":\"authentication succeeded\",\"real_userid\":{\"domain\":\"local\",\"user\":\"@index\"},\"remote\":{\"ip\":\"10.0.0.23\",\"port\":51606},\"timestamp\":\"2025-01-30T21:12:48.099382Z\"}";
    Record record = parser.parseRecord(payload);
    assertNotNull(record);

    assertEquals("N.A.", record.getDbName());
    assertEquals("authentication succeeded", record.getAccessor().getServiceName());
    assertEquals(51606, record.getSessionLocator().getClientPort());
    assertEquals("10.0.0.23", record.getSessionLocator().getClientIp());
    assertEquals(-1, record.getSessionLocator().getServerPort());
    assertEquals("10.0.0.11", record.getSessionLocator().getServerIp());
    assertEquals("CAPELLA", record.getAccessor().getDbProtocol());
    assertEquals("@index@local", record.getAccessor().getDbUser());
    assertEquals("CAPELLA", record.getAccessor().getServerType());
    assertEquals(
        "Time{timstamp=1738271568099, minOffsetFromGMT=0, minDst=0}", record.getTime().toString());
    assertNull(record.getException());
    assertEquals(
        "Authentication to the cluster succeeded", record.getData().getOriginalSqlCommand());
    assertEquals(
        "Authentication to the cluster succeeded", record.getData().getConstruct().fullSql);
    assertEquals(
        "authentication succeeded",
        record.getData().getConstruct().getSentences().get(0).getVerb());
    assertEquals("FREE_TEXT", record.getAccessor().getLanguage());
  }

  /** testBucketSelectionPayload tests that we can parse the "select bucket" events. */
  @Test
  void testBucketSelectionPayload() {
    String payload =
        "{\"bucket\":\"ZeeshanTestBucket\",\"description\":\"The specified bucket was selected\",\"id\":20492,\"local\":{\"ip\":\"127.0.0.1\",\"port\":11209},\"name\":\"select bucket\",\"real_userid\":{\"domain\":\"local\",\"user\":\"@ns_server\"},\"remote\":{\"ip\":\"127.0.0.1\",\"port\":44518},\"timestamp\":\"2025-01-30T21:16:10.986107Z\"}";
    Record record = parser.parseRecord(payload);
    assertNotNull(record);

    assertEquals("ZeeshanTestBucket", record.getDbName());
    assertEquals(44518, record.getSessionLocator().getClientPort());
    assertEquals("127.0.0.1", record.getSessionLocator().getClientIp());
    assertEquals(-1, record.getSessionLocator().getServerPort());
    assertEquals("127.0.0.1", record.getSessionLocator().getServerIp());
    assertEquals("CAPELLA", record.getAccessor().getDbProtocol());
    assertEquals("@ns_server@local", record.getAccessor().getDbUser());
    assertEquals("CAPELLA", record.getAccessor().getServerType());
    assertEquals(
        "Time{timstamp=1738271770986, minOffsetFromGMT=0, minDst=0}", record.getTime().toString());
    assertNull(record.getException());
    assertEquals("The specified bucket was selected", record.getData().getOriginalSqlCommand());
    assertEquals("The specified bucket was selected", record.getData().getConstruct().fullSql);
    assertEquals("select bucket", record.getData().getConstruct().getSentences().get(0).getVerb());
  }

  /**
   * testSuccessfulLoginPayload tests that we can parse the "login success" events. An example can
   * be found at <a href="https://docs.couchbase.com/cloud/security/auditing.html#login">here</a>
   */
  @Test
  void testSuccessfulLoginPayload() {
    String payload =
        "{\n"
            + "  \"description\": \"Successful login to couchbase cluster\",\n"
            + "  \"id\": 8192,\n"
            + "  \"local\": {\n"
            + "    \"ip\": \"10.144.210.101\",\n"
            + "    \"port\": 8091\n"
            + "  },\n"
            + "  \"name\": \"login success\",\n"
            + "  \"real_userid\": {\n"
            + "    \"domain\": \"local\",\n"
            + "    \"user\": \"testUser\"\n"
            + "  },\n"
            + "  \"remote\": {\n"
            + "    \"ip\": \"10.144.210.1\",\n"
            + "    \"port\": 53322\n"
            + "  },\n"
            + "  \"roles\": [\n"
            + "    \"admin\"\n"
            + "  ],\n"
            + "  \"sessionid\": \"ba2760cee506d0293a8b4a0bf83687b807329667\",\n"
            + "  \"timestamp\": \"2021-02-09T14:44:17.938Z\"\n"
            + "}";
    Record record = parser.parseRecord(payload);
    assertNotNull(record);

    assertEquals("N.A.", record.getDbName());
    assertEquals("ba2760cee506d0293a8b4a0bf83687b807329667", record.getSessionId());
    assertEquals(53322, record.getSessionLocator().getClientPort());
    assertEquals("10.144.210.1", record.getSessionLocator().getClientIp());
    assertEquals(-1, record.getSessionLocator().getServerPort());
    assertEquals("10.144.210.101", record.getSessionLocator().getServerIp());
    assertEquals("CAPELLA", record.getAccessor().getDbProtocol());
    assertEquals("testUser@local", record.getAccessor().getDbUser());
    assertEquals("CAPELLA", record.getAccessor().getServerType());
    assertEquals(
        "Time{timstamp=1612881857938, minOffsetFromGMT=0, minDst=0}", record.getTime().toString());
    assertNull(record.getException());
    assertEquals("Successful login to couchbase cluster", record.getData().getOriginalSqlCommand());
    assertEquals("Successful login to couchbase cluster", record.getData().getConstruct().fullSql);
    assertEquals("login success", record.getData().getConstruct().getSentences().get(0).getVerb());
  }

  /**
   * testFailedLoginPayload tests that we can parse the "login failure" events. An example can be
   * found at <a
   * href="https://docs.couchbase.com/cloud/security/auditing.html#login-failure">here</a>
   */
  @Test
  void testFailedLoginPayload() {
    String payload =
        "{\n"
            + "  \"description\": \"Unsuccessful attempt to login to couchbase cluster\",\n"
            + "  \"id\": 8193,\n"
            + "  \"local\": {\n"
            + "    \"ip\": \"10.144.210.101\",\n"
            + "    \"port\": 8091\n"
            + "  },\n"
            + "  \"name\": \"login failure\",\n"
            + "  \"real_userid\": {\n"
            + "    \"domain\": \"rejected\",\n"
            + "    \"user\": \"newUser\"\n"
            + "  },\n"
            + "  \"remote\": {\n"
            + "    \"ip\": \"10.144.210.1\",\n"
            + "    \"port\": 53348\n"
            + "  },\n"
            + "  \"timestamp\": \"2021-02-09T14:45:34.934Z\"\n"
            + "}";
    Record record = parser.parseRecord(payload);
    assertNotNull(record);

    assertEquals("N.A.", record.getDbName());
    assertEquals("", record.getSessionId());
    assertEquals(53348, record.getSessionLocator().getClientPort());
    assertEquals("10.144.210.1", record.getSessionLocator().getClientIp());
    assertEquals(-1, record.getSessionLocator().getServerPort());
    assertEquals("10.144.210.101", record.getSessionLocator().getServerIp());
    assertEquals("CAPELLA", record.getAccessor().getDbProtocol());
    assertEquals("newUser@rejected", record.getAccessor().getDbUser());
    assertEquals("CAPELLA", record.getAccessor().getServerType());
    assertEquals(
        "Time{timstamp=1612881934934, minOffsetFromGMT=0, minDst=0}", record.getTime().toString());
    assertNull(record.getException());
    assertEquals(
        "Unsuccessful attempt to login to couchbase cluster",
        record.getData().getOriginalSqlCommand());
    assertEquals(
        "Unsuccessful attempt to login to couchbase cluster",
        record.getData().getConstruct().fullSql);
    assertEquals("login failure", record.getData().getConstruct().getSentences().get(0).getVerb());
  }

  /**
   * testCreateBucketPayload tests that we can parse the "create bucket" events. An example can be
   * found at <a
   * href="https://docs.couchbase.com/cloud/security/auditing.html#bucket-creation">here</a>
   */
  @Test
  void testCreateBucketPayload() {
    String payload =
        "{\n"
            + "  \"bucket_name\": \"testBucket\",\n"
            + "  \"description\": \"Bucket was created\",\n"
            + "  \"id\": 8201,\n"
            + "  \"local\": {\n"
            + "    \"ip\": \"10.144.231.102\",\n"
            + "    \"port\": 8091\n"
            + "  },\n"
            + "  \"name\": \"create bucket\",\n"
            + "  \"props\": {\n"
            + "    \"compression_mode\": \"passive\",\n"
            + "    \"conflict_resolution_type\": \"seqno\",\n"
            + "    \"durability_min_level\": \"none\",\n"
            + "    \"eviction_policy\": \"value_only\",\n"
            + "    \"flush_enabled\": false,\n"
            + "    \"max_ttl\": 0,\n"
            + "    \"num_replicas\": 1,\n"
            + "    \"num_threads\": 3,\n"
            + "    \"purge_interval\": \"undefined\",\n"
            + "    \"ram_quota\": 268435456,\n"
            + "    \"replica_index\": false,\n"
            + "    \"storage_mode\": \"couchstore\"\n"
            + "  },\n"
            + "  \"real_userid\": {\n"
            + "    \"domain\": \"builtin\",\n"
            + "    \"user\": \"Administrator\"\n"
            + "  },\n"
            + "  \"remote\": {\n"
            + "    \"ip\": \"10.144.231.1\",\n"
            + "    \"port\": 53837\n"
            + "  },\n"
            + "  \"sessionid\": \"3f8472056c30014d32f19aca0bb22b10d5cefbee\",\n"
            + "  \"timestamp\": \"2022-08-23T10:05:34.489Z\",\n"
            + "  \"type\": \"membase\"\n"
            + "}";
    Record record = parser.parseRecord(payload);
    assertNotNull(record);

    assertEquals("testBucket", record.getDbName());
    assertEquals("3f8472056c30014d32f19aca0bb22b10d5cefbee", record.getSessionId());
    assertEquals(53837, record.getSessionLocator().getClientPort());
    assertEquals("10.144.231.1", record.getSessionLocator().getClientIp());
    assertEquals(-1, record.getSessionLocator().getServerPort());
    assertEquals("10.144.231.102", record.getSessionLocator().getServerIp());
    assertEquals("CAPELLA", record.getAccessor().getDbProtocol());
    assertEquals("Administrator@builtin", record.getAccessor().getDbUser());
    assertEquals("CAPELLA", record.getAccessor().getServerType());
    assertEquals(
        "Time{timstamp=1661249134489, minOffsetFromGMT=0, minDst=0}", record.getTime().toString());
    assertNull(record.getException());
    assertEquals("Bucket was created", record.getData().getOriginalSqlCommand());
    assertEquals("Bucket was created", record.getData().getConstruct().fullSql);
    assertEquals("create bucket", record.getData().getConstruct().getSentences().get(0).getVerb());
    assertEquals(
        "testBucket",
        record.getData().getConstruct().getSentences().get(0).getObjects().get(0).name);
    assertEquals(
        "object", record.getData().getConstruct().getSentences().get(0).getObjects().get(0).type);
  }

  /**
   * testCreateUpdateIndexPayload tests that we can parse the "Create/Update index" events. An
   * example can be found at <a
   * href="https://docs.couchbase.com/cloud/security/auditing.html#index-creation">here</a>
   */
  @Test
  void testCreateUpdateIndexPayload() {
    String payload =
        "{\n"
            + "  \"description\": \"FTS index was created/Updated\",\n"
            + "  \"id\": 24577,\n"
            + "  \"index_name\": \"testIndex\",\n"
            + "  \"local\": {\n"
            + "    \"ip\": \"127.0.0.1\",\n"
            + "    \"port\": \"8094\"\n"
            + "  },\n"
            + "  \"name\": \"Create/Update index\",\n"
            + "  \"real_userid\": {\n"
            + "    \"domain\": \"builtin\",\n"
            + "    \"user\": \"Administrator\"\n"
            + "  },\n"
            + "  \"remote\": {\n"
            + "    \"ip\": \"127.0.0.1\",\n"
            + "    \"port\": \"39575\"\n"
            + "  },\n"
            + "  \"timestamp\": \"2021-02-09T15:20:49.953Z\"\n"
            + "}";
    Record record = parser.parseRecord(payload);
    assertNotNull(record);

    assertEquals("N.A.", record.getDbName());
    assertEquals("", record.getSessionId());
    assertEquals(39575, record.getSessionLocator().getClientPort());
    assertEquals("127.0.0.1", record.getSessionLocator().getClientIp());
    assertEquals(-1, record.getSessionLocator().getServerPort());
    assertEquals("127.0.0.1", record.getSessionLocator().getServerIp());
    assertEquals("CAPELLA", record.getAccessor().getDbProtocol());
    assertEquals("Administrator@builtin", record.getAccessor().getDbUser());
    assertEquals("CAPELLA", record.getAccessor().getServerType());
    assertEquals(
        "Time{timstamp=1612884049953, minOffsetFromGMT=0, minDst=0}", record.getTime().toString());
    assertNull(record.getException());
    assertEquals("FTS index was created/Updated", record.getData().getOriginalSqlCommand());
    assertEquals("FTS index was created/Updated", record.getData().getConstruct().fullSql);
    assertEquals(
        "Create/Update index", record.getData().getConstruct().getSentences().get(0).getVerb());
    assertEquals(
        "testIndex",
        record.getData().getConstruct().getSentences().get(0).getObjects().get(0).name);
    assertEquals(
        "index", record.getData().getConstruct().getSentences().get(0).getObjects().get(0).type);
  }

  /** testBucketTtlModificationPayload tests that we can parse the "modify bucket" events. */
  @Test
  void testBucketTtlModificationPayload() {
    String payload =
        "{\n"
            + "  \"bucket_name\": \"testBucket\",\n"
            + "  \"description\": \"Bucket was modified\",\n"
            + "  \"id\": 8202,\n"
            + "  \"local\": {\n"
            + "    \"ip\": \"10.144.210.101\",\n"
            + "    \"port\": 8091\n"
            + "  },\n"
            + "  \"name\": \"modify bucket\",\n"
            + "  \"props\": {\n"
            + "    \"compression_mode\": \"passive\",\n"
            + "    \"durability_min_level\": \"none\",\n"
            + "    \"eviction_policy\": \"value_only\",\n"
            + "    \"flush_enabled\": false,\n"
            + "    \"max_ttl\": 100000,\n"
            + "    \"num_replicas\": 1,\n"
            + "    \"num_threads\": 3,\n"
            + "    \"purge_interval\": \"undefined\",\n"
            + "    \"ram_quota\": 268435456,\n"
            + "    \"storage_mode\": \"couchstore\"\n"
            + "  },\n"
            + "  \"real_userid\": {\n"
            + "    \"domain\": \"builtin\",\n"
            + "    \"user\": \"Administrator\"\n"
            + "  },\n"
            + "  \"remote\": {\n"
            + "    \"ip\": \"10.144.210.1\",\n"
            + "    \"port\": 53397\n"
            + "  },\n"
            + "  \"sessionid\": \"eb1411eaa5eb041ea07fb86ffe93a94a59f8e8e2\",\n"
            + "  \"timestamp\": \"2021-02-09T14:48:14.653Z\",\n"
            + "  \"type\": \"membase\"\n"
            + "}";

    Record record = parser.parseRecord(payload);
    assertNotNull(record);

    assertEquals("testBucket", record.getDbName());
    assertEquals("eb1411eaa5eb041ea07fb86ffe93a94a59f8e8e2", record.getSessionId());
    assertEquals(53397, record.getSessionLocator().getClientPort());
    assertEquals("10.144.210.1", record.getSessionLocator().getClientIp());
    assertEquals(-1, record.getSessionLocator().getServerPort());
    assertEquals("10.144.210.101", record.getSessionLocator().getServerIp());
    assertEquals("CAPELLA", record.getAccessor().getDbProtocol());
    assertEquals("Administrator@builtin", record.getAccessor().getDbUser());
    assertEquals("CAPELLA", record.getAccessor().getServerType());
    assertEquals(
        "Time{timstamp=1612882094653, minOffsetFromGMT=0, minDst=0}", record.getTime().toString());
    assertNull(record.getException());
    assertEquals("Bucket was modified", record.getData().getOriginalSqlCommand());
    assertEquals("Bucket was modified", record.getData().getConstruct().fullSql);
    assertEquals("modify bucket", record.getData().getConstruct().getSentences().get(0).getVerb());
    assertEquals(
        "testBucket",
        record.getData().getConstruct().getSentences().get(0).getObjects().get(0).name);
    assertEquals(
        "object", record.getData().getConstruct().getSentences().get(0).getObjects().get(0).type);
  }

  /**
   * testUserCreationPayload tests that we can parse the "set user" events. An example can be found
   * at <a href="https://docs.couchbase.com/cloud/security/auditing.html#user-creation">here</a>
   */
  @Test
  void testUserCreationPayload() {
    String payload =
        "{\n"
            + "  \"description\": \"User was added or updated\",\n"
            + "  \"full_name\": \"\",\n"
            + "  \"groups\": [],\n"
            + "  \"id\": 8232,\n"
            + "  \"identity\": {\n"
            + "    \"domain\": \"local\",\n"
            + "    \"user\": \"clusterUser\"\n"
            + "  },\n"
            + "  \"local\": {\n"
            + "    \"ip\": \"10.144.210.101\",\n"
            + "    \"port\": 8091\n"
            + "  },\n"
            + "  \"name\": \"set user\",\n"
            + "  \"real_userid\": {\n"
            + "    \"domain\": \"builtin\",\n"
            + "    \"user\": \"Administrator\"\n"
            + "  },\n"
            + "  \"reason\": \"added\",\n"
            + "  \"remote\": {\n"
            + "    \"ip\": \"10.144.210.1\",\n"
            + "    \"port\": 53444\n"
            + "  },\n"
            + "  \"roles\": [\n"
            + "    \"cluster_admin\"\n"
            + "  ],\n"
            + "  \"sessionid\": \"eb1411eaa5eb041ea07fb86ffe93a94a59f8e8e2\",\n"
            + "  \"timestamp\": \"2021-02-09T14:50:38.256Z\"\n"
            + "}";

    Record record = parser.parseRecord(payload);
    assertNotNull(record);

    assertEquals("N.A.", record.getDbName());
    assertEquals("eb1411eaa5eb041ea07fb86ffe93a94a59f8e8e2", record.getSessionId());
    assertEquals(53444, record.getSessionLocator().getClientPort());
    assertEquals("10.144.210.1", record.getSessionLocator().getClientIp());
    assertEquals(-1, record.getSessionLocator().getServerPort());
    assertEquals("10.144.210.101", record.getSessionLocator().getServerIp());
    assertEquals("CAPELLA", record.getAccessor().getDbProtocol());
    assertEquals("Administrator@builtin", record.getAccessor().getDbUser());
    assertEquals("CAPELLA", record.getAccessor().getServerType());
    assertEquals(
        "Time{timstamp=1612882238256, minOffsetFromGMT=0, minDst=0}", record.getTime().toString());
    assertNull(record.getException());
    assertEquals("User was added or updated", record.getData().getOriginalSqlCommand());
    assertEquals("User was added or updated", record.getData().getConstruct().fullSql);
    assertEquals("set user", record.getData().getConstruct().getSentences().get(0).getVerb());
    assertEquals(0, record.getData().getConstruct().getSentences().get(0).getObjects().size());
  }

  @Test
  void testDeleteBucket() {
    String payload =
        "{\"bucket\":\"deleteMeBucket\",\"description\":\"The specified bucket was selected\",\"id\":20492,\"local\":{\"ip\":\"127.0.0.2\",\"port\":11209},\"name\":\"select bucket\",\"real_userid\":{\"domain\":\"local\",\"user\":\"@ns_server\"},\"remote\":{\"ip\":\"127.0.0.1\",\"port\":35264},\"timestamp\":\"2025-04-24T00:32:27.462571Z\"}";
    Record record = parser.parseRecord(payload);
    assertNotNull(record);

    assertEquals("deleteMeBucket", record.getDbName());
    assertEquals(35264, record.getSessionLocator().getClientPort());
    assertEquals("127.0.0.1", record.getSessionLocator().getClientIp());
    assertEquals(-1, record.getSessionLocator().getServerPort());
    assertEquals("127.0.0.2", record.getSessionLocator().getServerIp());
    assertEquals("CAPELLA", record.getAccessor().getDbProtocol());
    assertEquals("@ns_server@local", record.getAccessor().getDbUser());
    assertEquals("CAPELLA", record.getAccessor().getServerType());
    assertEquals(
        "Time{timstamp=1745454747462, minOffsetFromGMT=0, minDst=0}", record.getTime().toString());
    assertNull(record.getException());
    assertEquals("The specified bucket was selected", record.getData().getOriginalSqlCommand());
    assertEquals("The specified bucket was selected", record.getData().getConstruct().fullSql);
    assertEquals("select bucket", record.getData().getConstruct().getSentences().get(0).getVerb());
  }

  @Test
  void testSqlString() {
    String payload =
        "{\n"
            + "  \"description\": \"A N1QL SELECT statement was executed\",\n"
            + "  \"errors\": null,\n"
            + "  \"id\": 28672,\n"
            + "  \"isAdHoc\": true,\n"
            + "  \"local\": {\n"
            + "    \"ip\": \"127.0.0.1\",\n"
            + "    \"port\": 18093\n"
            + "  },\n"
            + "  \"metrics\": {\n"
            + "    \"elapsedTime\": \"0.047955155\",\n"
            + "    \"executionTime\": \"0.047860929\",\n"
            + "    \"resultCount\": 38,\n"
            + "    \"resultSize\": 2405\n"
            + "  },\n"
            + "  \"name\": \"SELECT statement\",\n"
            + "  \"node\": \"svc-dqi-node-003.vam24ep86horsjja.cloud.couchbase.com:8091\",\n"
            + "  \"real_userid\": {\n"
            + "    \"domain\": \"local\",\n"
            + "    \"user\": \"2209d939-681d-44ed-a47c-4757d13344a2\"\n"
            + "  },\n"
            + "  \"remote\": {\n"
            + "    \"ip\": \"127.0.0.1\",\n"
            + "    \"port\": 56928\n"
            + "  },\n"
            + "  \"requestId\": \"dc6c5e46-ad6b-4bc6-a0a0-ac0418b7ec51\",\n"
            + "  \"statement\": \"select * from system:all_keyspaces as keyspaces union select * from system:indexes where `using`=\\\"gsi\\\" union select * from system:all_scopes\",\n"
            + "  \"status\": \"success\",\n"
            + "  \"timestamp\": \"2025-06-03T22:38:49.615Z\",\n"
            + "  \"userAgent\": \"couchbase-cloud-proxy\"\n"
            + "}";
    Record record = parser.parseRecord(payload);
    assertNotNull(record);

    assertEquals("N.A.", record.getDbName());
    assertEquals(56928, record.getSessionLocator().getClientPort());
    assertEquals("127.0.0.1", record.getSessionLocator().getClientIp());
    assertEquals(-1, record.getSessionLocator().getServerPort());
    assertEquals("127.0.0.1", record.getSessionLocator().getServerIp());
    assertEquals("CAPELLA", record.getAccessor().getDbProtocol());
    assertEquals("2209d939-681d-44ed-a47c-4757d13344a2@local", record.getAccessor().getDbUser());
    assertEquals("CAPELLA", record.getAccessor().getServerType());
    assertEquals(
        "Time{timstamp=1748990329615, minOffsetFromGMT=0, minDst=0}", record.getTime().toString());
    assertNull(record.getException());
    assertEquals(
        "__CB POST /#statement=select * from system:all_keyspaces as keyspaces union select * from system:indexes where `using`=\"gsi\" union select * from system:all_scopes",
        record.getData().getOriginalSqlCommand());
  }

  @Test
  void testServiceName() {
    String payload =
        "{\n"
            + "  \"cluster_name\": \"\",\n"
            + "  \"description\": \"Cluster settings were changed\",\n"
            + "  \"id\": 8209,\n"
            + "  \"local\": {\n"
            + "    \"ip\": \"10.0.0.44\",\n"
            + "    \"port\": 18091\n"
            + "  },\n"
            + "  \"name\": \"change cluster settings\",\n"
            + "  \"quotas\": {\n"
            + "    \"cbas\": 2189,\n"
            + "    \"eventing\": 256,\n"
            + "    \"fts\": 512,\n"
            + "    \"index\": 4139,\n"
            + "    \"kv\": 4139,\n"
            + "    \"n1ql\": 3311\n"
            + "  },\n"
            + "  \"real_userid\": {\n"
            + "    \"domain\": \"builtin\",\n"
            + "    \"user\": \"couchbase-cloud-admin\"\n"
            + "  },\n"
            + "  \"remote\": {\n"
            + "    \"ip\": \"3.230.238.38\",\n"
            + "    \"port\": 14501\n"
            + "  },\n"
            + "  \"timestamp\": \"2025-06-04T22:04:21.561Z\"\n"
            + "}";
    Record record = parser.parseRecord(payload);
    assertNotNull(record);
    assertEquals("change cluster settings", record.getAccessor().getServiceName());
  }

  @Test
  void testDeleteUser() {
    String payload =
        "{\n"
            + "  \"description\": \"User was deleted\",\n"
            + "  \"id\": 8194,\n"
            + "  \"identity\": {\n"
            + "    \"domain\": \"local\",\n"
            + "    \"user\": \"e1028d45-5814-44de-b299-af8302f64629\"\n"
            + "  },\n"
            + "  \"local\": {\n"
            + "    \"ip\": \"10.0.0.44\",\n"
            + "    \"port\": 18091\n"
            + "  },\n"
            + "  \"name\": \"delete user\",\n"
            + "  \"real_userid\": {\n"
            + "    \"domain\": \"builtin\",\n"
            + "    \"user\": \"couchbase-cloud-admin\"\n"
            + "  },\n"
            + "  \"remote\": {\n"
            + "    \"ip\": \"3.230.238.38\",\n"
            + "    \"port\": 12694\n"
            + "  },\n"
            + "  \"timestamp\": \"2025-06-04T23:00:00.664Z\"\n"
            + "}";
    Record record = parser.parseRecord(payload);
    assertNotNull(record);

    assertEquals("User was deleted", record.getData().getOriginalSqlCommand());
    assertEquals("User was deleted", record.getData().getConstruct().fullSql);
    assertEquals("delete user", record.getAccessor().getServiceName());
  }

  @Test
  void testINFERStatment() {
    String payload =
        "{\n"
            + "  \"description\": \"A N1QL INFER statement was executed\",\n"
            + "  \"errors\": null,\n"
            + "  \"id\": 28675,\n"
            + "  \"isAdHoc\": true,\n"
            + "  \"local\": {\n"
            + "    \"ip\": \"127.0.0.1\",\n"
            + "    \"port\": 18093\n"
            + "  },\n"
            + "  \"metrics\": {\n"
            + "    \"elapsedTime\": \"0.016768862\",\n"
            + "    \"executionTime\": \"0.016718729\",\n"
            + "    \"resultCount\": 1,\n"
            + "    \"resultSize\": 1678\n"
            + "  },\n"
            + "  \"name\": \"INFER statement\",\n"
            + "  \"node\": \"svc-dqi-node-003.vam24ep86horsjja.cloud.couchbase.com:8091\",\n"
            + "  \"real_userid\": {\n"
            + "    \"domain\": \"local\",\n"
            + "    \"user\": \"e1028d45-5814-44de-b299-af8302f64629\"\n"
            + "  },\n"
            + "  \"remote\": {\n"
            + "    \"ip\": \"127.0.0.1\",\n"
            + "    \"port\": 54908\n"
            + "  },\n"
            + "  \"requestId\": \"9e60a8e7-98be-400f-9fef-1b562deb88cc\",\n"
            + "  \"statement\": \"infer `MtBucket`.`new`.`capella_engine_events`\",\n"
            + "  \"status\": \"success\",\n"
            + "  \"timestamp\": \"2025-06-03T23:25:34.231Z\",\n"
            + "  \"userAgent\": \"couchbase-cloud-proxy\"\n"
            + "}";
    Record record = parser.parseRecord(payload);
    assertNotNull(record);
    assertNotNull(record.getData());

    assertEquals("N.A.", record.getDbName());
    assertEquals(54908, record.getSessionLocator().getClientPort());
    assertEquals("127.0.0.1", record.getSessionLocator().getClientIp());
    assertEquals(-1, record.getSessionLocator().getServerPort());
    assertEquals("127.0.0.1", record.getSessionLocator().getServerIp());
    assertEquals("CAPELLA", record.getAccessor().getDbProtocol());
    assertEquals("e1028d45-5814-44de-b299-af8302f64629@local", record.getAccessor().getDbUser());
    assertEquals("CAPELLA", record.getAccessor().getServerType());
    assertEquals(
        "__CB POST /#statement=infer `MtBucket`.`new`.`capella_engine_events`",
        record.getData().getOriginalSqlCommand());
    assertEquals("INFER statement", record.getAccessor().getServiceName());
    assertEquals("TEXT", record.getAccessor().getDataType());
    assertEquals("COUCHB", record.getAccessor().getLanguage());
  }

  @Test
  void testDeleteStatement() {
    String payload =
        "{\n"
            + "  \"description\": \"A N1QL DELETE statement was executed\",\n"
            + "  \"errors\": null,\n"
            + "  \"id\": 28678,\n"
            + "  \"isAdHoc\": true,\n"
            + "  \"local\": {\n"
            + "    \"ip\": \"10.0.0.123\",\n"
            + "    \"port\": 18093\n"
            + "  },\n"
            + "  \"metrics\": {\n"
            + "    \"elapsedTime\": \"0.002459825\",\n"
            + "    \"executionTime\": \"0.002391854\",\n"
            + "    \"mutationCount\": 1,\n"
            + "    \"resultCount\": 0,\n"
            + "    \"resultSize\": 0\n"
            + "  },\n"
            + "  \"name\": \"DELETE statement\",\n"
            + "  \"node\": \"svc-dqi-node-002.vam24ep86horsjja.cloud.couchbase.com:8091\",\n"
            + "  \"real_userid\": {\n"
            + "    \"domain\": \"local\",\n"
            + "    \"user\": \"admin\"\n"
            + "  },\n"
            + "  \"remote\": {\n"
            + "    \"ip\": \"99.233.169.176\",\n"
            + "    \"port\": 53221\n"
            + "  },\n"
            + "  \"requestId\": \"85476769-4fcc-4a63-b7a6-f399fb92b0f7\",\n"
            + "  \"statement\": \"DELETE FROM `MtBucket`.`CreatedByMe`.`TestScope` WHERE id = \\\"user4\\\";\",\n"
            + "  \"status\": \"success\",\n"
            + "  \"timestamp\": \"2025-06-04T15:15:36.432Z\",\n"
            + "  \"userAgent\": \"Go-http-client/1.1\"\n"
            + "}";
    Record record = parser.parseRecord(payload);
    assertNotNull(record);
    assertNotNull(record.getData());

    assertEquals("N.A.", record.getDbName());
    assertEquals(53221, record.getSessionLocator().getClientPort());
    assertEquals("99.233.169.176", record.getSessionLocator().getClientIp());
    assertEquals(-1, record.getSessionLocator().getServerPort());
    assertEquals("10.0.0.123", record.getSessionLocator().getServerIp());
    assertEquals("CAPELLA", record.getAccessor().getDbProtocol());
    assertEquals("admin@local", record.getAccessor().getDbUser());
    assertEquals("CAPELLA", record.getAccessor().getServerType());
    assertEquals(
        "__CB POST /#statement=DELETE FROM `MtBucket`.`CreatedByMe`.`TestScope` WHERE id = \"user4\";",
        record.getData().getOriginalSqlCommand());
    assertEquals("DELETE statement", record.getAccessor().getServiceName());
  }

  @Test
  void testComplexSQL() {
    String payload =
        "{\n"
            + "  \"description\": \"A N1QL SELECT statement was executed\",\n"
            + "  \"errors\": null,\n"
            + "  \"id\": 28672,\n"
            + "  \"isAdHoc\": true,\n"
            + "  \"local\": {\n"
            + "    \"ip\": \"127.0.0.1\",\n"
            + "    \"port\": 18093\n"
            + "  },\n"
            + "  \"metrics\": {\n"
            + "    \"elapsedTime\": \"0.041295408\",\n"
            + "    \"executionTime\": \"0.041239119\",\n"
            + "    \"resultCount\": 112,\n"
            + "    \"resultSize\": 30859,\n"
            + "    \"sortCount\": 112\n"
            + "  },\n"
            + "  \"name\": \"SELECT statement\",\n"
            + "  \"node\": \"svc-dqi-node-003.vam24ep86horsjja.cloud.couchbase.com:8091\",\n"
            + "  \"real_userid\": {\n"
            + "    \"domain\": \"local\",\n"
            + "    \"user\": \"2209d939-681d-44ed-a47c-4757d13344a2\"\n"
            + "  },\n"
            + "  \"remote\": {\n"
            + "    \"ip\": \"127.0.0.1\",\n"
            + "    \"port\": 43060\n"
            + "  },\n"
            + "  \"requestId\": \"aefbc581-8187-4672-8d4b-91826a024cf4\",\n"
            + "  \"statement\": \"SELECT a.* FROM\\n           (SELECT all_keyspaces.*, 'keyspace' as `type` FROM SYSTEM:all_keyspaces where datastore_id != \\\"system\\\" UNION\\n            SELECT all_indexes.*, 'index' as `type` FROM system:all_indexes WHERE `using`=\\\"gsi\\\" UNION\\n            SELECT all_scopes.*, 'scope' as `type` FROM SYSTEM:all_scopes order by `type` desc) a\\n          order by `type` desc, `path`;\",\n"
            + "  \"status\": \"success\",\n"
            + "  \"timestamp\": \"2025-06-06T17:32:22.441Z\",\n"
            + "  \"userAgent\": \"couchbase-cloud-proxy\"\n"
            + "}";
    Record record = parser.parseRecord(payload);
    assertNotNull(record);

    assertEquals("N.A.", record.getDbName());
    assertEquals(43060, record.getSessionLocator().getClientPort());
    assertEquals("127.0.0.1", record.getSessionLocator().getClientIp());
    assertEquals(-1, record.getSessionLocator().getServerPort());
    assertEquals("127.0.0.1", record.getSessionLocator().getServerIp());
    assertEquals("CAPELLA", record.getAccessor().getDbProtocol());
    assertEquals("2209d939-681d-44ed-a47c-4757d13344a2@local", record.getAccessor().getDbUser());
    assertEquals("CAPELLA", record.getAccessor().getServerType());
    assertEquals("COUCHB", record.getAccessor().getLanguage());
    assertEquals(
        "__CB POST /#statement=SELECT a.* FROM\n"
            + "           (SELECT all_keyspaces.*, 'keyspace' as `type` FROM SYSTEM:all_keyspaces where datastore_id != \"system\" UNION\n"
            + "            SELECT all_indexes.*, 'index' as `type` FROM system:all_indexes WHERE `using`=\"gsi\" UNION\n"
            + "            SELECT all_scopes.*, 'scope' as `type` FROM SYSTEM:all_scopes order by `type` desc) a\n"
            + "          order by `type` desc, `path`;",
        record.getData().getOriginalSqlCommand());
  }

  @Test
  void testException() {
    String payload =
        "{\"description\":\"REST operation failed due to authentication failure\",\"id\":20485,\"local\":{\"ip\":\"10.0.0.11\",\"port\":11207},\"name\":\"authentication failure\",\"raw_url\": \"/pools\",\"real_userid\":{\"domain\":\"local\",\"user\":\"@index\"},\"remote\":{\"ip\":\"10.0.0.23\",\"port\":51606},\"timestamp\":\"2025-01-30T21:12:48.099382Z\"}";
    Record record = parser.parseRecord(payload);
    assertEquals("N.A.", record.getDbName());
    assertEquals("authentication failure", record.getAccessor().getServiceName());
    assertEquals(51606, record.getSessionLocator().getClientPort());
    assertEquals("10.0.0.23", record.getSessionLocator().getClientIp());
    assertEquals(-1, record.getSessionLocator().getServerPort());
    assertEquals("10.0.0.11", record.getSessionLocator().getServerIp());
    assertEquals("CAPELLA", record.getAccessor().getDbProtocol());
    assertEquals("@index@local", record.getAccessor().getDbUser());
    assertEquals("CAPELLA", record.getAccessor().getServerType());
    assertEquals(
        "Time{timstamp=1738271568099, minOffsetFromGMT=0, minDst=0}", record.getTime().toString());
    assertEquals("FREE_TEXT", record.getAccessor().getLanguage());
    assertEquals("authentication failure", record.getException().getDescription());
    assertEquals("LOGIN_FAILED", record.getException().getExceptionTypeId());
  }

  @Test
  void testSQLError() {
    String payload =
        "{\"description\":\"An unrecognized statement was received by the N1QL query engine\",\"errors\":null,\"id\":28687,\"isAdHoc\":true,\"local\":{\"ip\":\"10.0.0.102\",\"port\":18093},\"metrics\":{\"elapsedTime\":\"0.00037393\",\"errorCount\":1,\"executionTime\":\"0.000311174\",\"resultCount\":0,\"resultSize\":0},\"name\":\"UNRECOGNIZED statement\",\"node\":\"svc-dqi-node-001.vam24ep86horsjja.cloud.couchbase.com:8091\",\"real_userid\":{\"domain\":\"local\",\"user\":\"@index\"},\"remote\":{\"ip\":\"99.233.169.176\",\"port\":58674},\"requestId\":\"d0707282-7b1a-453e-82df-76ea3456e6ca\",\"statement\":\"select * fro test;\",\"status\":\"fatal\",\"timestamp\":\"2025-06-25T15:58:08.215Z\",\"userAgent\":\"Go-http-client/1.1\"}";
    Record record = parser.parseRecord(payload);
    assertEquals("N.A.", record.getDbName());
    assertEquals("UNRECOGNIZED statement", record.getAccessor().getServiceName());
    assertEquals(58674, record.getSessionLocator().getClientPort());
    assertEquals("99.233.169.176", record.getSessionLocator().getClientIp());
    assertEquals(-1, record.getSessionLocator().getServerPort());
    assertEquals("10.0.0.102", record.getSessionLocator().getServerIp());
    assertEquals("CAPELLA", record.getAccessor().getDbProtocol());
    assertEquals("@index@local", record.getAccessor().getDbUser());
    assertEquals("CAPELLA", record.getAccessor().getServerType());
    assertEquals(
        "Time{timstamp=1750867088215, minOffsetFromGMT=0, minDst=0}", record.getTime().toString());
    assertEquals("COUCHB", record.getAccessor().getLanguage());
    assertEquals("UNRECOGNIZED statement", record.getException().getDescription());
    assertEquals("SQL_ERROR", record.getException().getExceptionTypeId());
    assertEquals("select * fro test;", record.getException().getSqlString());
  }
}
