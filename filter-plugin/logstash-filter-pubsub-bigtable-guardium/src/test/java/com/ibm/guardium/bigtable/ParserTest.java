package com.ibm.guardium.bigtable;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.ibm.guardium.bigtable.errorcode.BigTableErrorCodes;
import com.ibm.guardium.universalconnector.commons.structures.Record;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ParserTest {

    @Test
    void parseRecordTest() throws IOException {
        var payload = readJsonFileAsJson("src/test/resources/writeRecord.json");
        Record record = Parser.parseRecord(payload);

        assertNotNull(record);

        assertEquals("test-Project:test-big-table", record.getDbName());
        assertEquals("test1@abc.com", record.getAppUserName());
        assertEquals("", record.getSessionId());

        assertEquals("pets", record.getData().getConstruct().sentences.get(0).getObjects().get(2).getName());
        assertEquals("table", record.getData().getConstruct().sentences.get(0).getObjects().get(2).getType());
        assertEquals("test-big-table", record.getData().getConstruct().sentences.get(0).getObjects().get(1).getName());
        assertEquals("instance", record.getData().getConstruct().sentences.get(0).getObjects().get(1).getType());
        assertEquals("test-Project", record.getData().getConstruct().sentences.get(0).getObjects().get(0).getName());
        assertEquals("project", record.getData().getConstruct().sentences.get(0).getObjects().get(0).getType());

        assertEquals("MutateRow ON projects/test-Project/instances/test-big-table/tables/pets", record.getData().getConstruct().fullSql);

        assertEquals(ApplicationConstants.UNKOWN_STRING, record.getAccessor().getClient_mac());
        assertEquals(ApplicationConstants.UNKOWN_STRING, record.getAccessor().getClientHostName());
        assertEquals(ApplicationConstants.UNKOWN_STRING, record.getAccessor().getClientOs());
        assertEquals(ApplicationConstants.UNKOWN_STRING, record.getAccessor().getCommProtocol());
        assertEquals("CONSTRUCT", record.getAccessor().getDataType());
        assertEquals("BigTable(GCP)", record.getAccessor().getDbProtocol());
        assertEquals(ApplicationConstants.UNKOWN_STRING, record.getAccessor().getDbProtocolVersion());
        assertEquals("test1@abc.com", record.getAccessor().getDbUser());
        assertEquals("FREE_TEXT", record.getAccessor().getLanguage());
        assertEquals(ApplicationConstants.UNKOWN_STRING, record.getAccessor().getOsUser());
        assertEquals(ApplicationConstants.UNKOWN_STRING, record.getAccessor().getServerDescription());
        assertEquals("test-Project_bigtable.googleapis.com", record.getAccessor().getServerHostName());
        assertEquals(ApplicationConstants.UNKOWN_STRING, record.getAccessor().getServerOs());
        assertEquals("BigTable", record.getAccessor().getServerType());
        assertEquals("bigtable.googleapis.com", record.getAccessor().getServiceName());
        assertEquals(ApplicationConstants.UNKOWN_STRING, record.getAccessor().getSourceProgram());
    }

    @Test
    void getExceptionTypeTest() {
        assertEquals(ApplicationConstants.STATUS_OK, BigTableErrorCodes.getStatusException(0));
        assertEquals(ApplicationConstants.STATUS_CANCELLED, BigTableErrorCodes.getStatusException(1));
        assertEquals(ApplicationConstants.STATUS_UNKNOWN, BigTableErrorCodes.getStatusException(2));
        assertEquals(ApplicationConstants.STATUS_DEADLINE_EXCEEDED, BigTableErrorCodes.getStatusException(4));
        assertEquals(ApplicationConstants.STATUS_NOT_FOUND, BigTableErrorCodes.getStatusException(5));
        assertEquals(ApplicationConstants.STATUS_ALREADY_EXISTS, BigTableErrorCodes.getStatusException(6));
        assertEquals(ApplicationConstants.STATUS_RESOURCE_EXHAUSTED, BigTableErrorCodes.getStatusException(8));
        assertEquals(ApplicationConstants.STATUS_ABORTED, BigTableErrorCodes.getStatusException(10));
        assertEquals(ApplicationConstants.STATUS_OUT_OF_RANGE, BigTableErrorCodes.getStatusException(11));
        assertEquals(ApplicationConstants.STATUS_UNIMPLEMENTED, BigTableErrorCodes.getStatusException(12));
        assertEquals(ApplicationConstants.STATUS_INTERNAL, BigTableErrorCodes.getStatusException(13));
        assertEquals(ApplicationConstants.STATUS_UNAVAILABLE, BigTableErrorCodes.getStatusException(14));
        assertEquals(ApplicationConstants.STATUS_INVALID_ARGUMENT, BigTableErrorCodes.getStatusException(400));
        assertEquals(ApplicationConstants.STATUS_NOT_AUTHORIZED, BigTableErrorCodes.getStatusException(401));
        assertEquals(ApplicationConstants.STATUS_PERMISSION_DENIED, BigTableErrorCodes.getStatusException(403));
        assertEquals(ApplicationConstants.STATUS_RESOURCE_NOT_FOUND, BigTableErrorCodes.getStatusException(404));
        assertEquals(ApplicationConstants.STATUS_RESOURCE_ALREADY_EXIST, BigTableErrorCodes.getStatusException(409));
        assertEquals(ApplicationConstants.STATUS_FAILED_PRECONDITION, BigTableErrorCodes.getStatusException(412));
        assertEquals(ApplicationConstants.STATUS_RATE_LIMIT_EXCEEDED, BigTableErrorCodes.getStatusException(429));
        assertEquals(ApplicationConstants.STATUS_INTERNAL_SERVER_ERROR, BigTableErrorCodes.getStatusException(500));
        assertEquals(ApplicationConstants.STATUS_GENERAL_EXCEPTION, BigTableErrorCodes.getStatusException(200));
        assertEquals(ApplicationConstants.STATUS_GENERAL_EXCEPTION, BigTableErrorCodes.getStatusException(411));
    }

    @Test
    void parserSessionLocator_IPV4() throws IOException {
        var payload = readJsonFileAsJson("src/test/resources/writeRecord.json");
        var sessionLocator = Parser.parseSessionLocator(payload.getAsJsonObject(ApplicationConstants.PROTO_PAYLOAD));
        assertNotNull(sessionLocator);

        assertEquals("35.229.29.78", sessionLocator.getClientIp());
        assertEquals(ApplicationConstants.DEFAULT_IP, sessionLocator.getServerIp());

        assertNull(sessionLocator.getServerIpv6());
        assertNull(sessionLocator.getClientIpv6());

        assertEquals(-1, sessionLocator.getServerPort());
        assertEquals(-1, sessionLocator.getClientPort());
    }

    @Test
    void parserSessionLocator_IPV6() throws IOException {
        var payload = readJsonFileAsJson("src/test/resources/writeRecord.json");

        var protoPayload = payload.getAsJsonObject(ApplicationConstants.PROTO_PAYLOAD);
        protoPayload.get(ApplicationConstants.REQUEST_METADATA).getAsJsonObject()
                .addProperty(ApplicationConstants.CALLER_IP, "2001:db8:3333:4444:5555:6666:7777:8888");

        var sessionLocator = Parser.parseSessionLocator(payload.getAsJsonObject(ApplicationConstants.PROTO_PAYLOAD));
        assertNotNull(sessionLocator);

        assertEquals("2001:db8:3333:4444:5555:6666:7777:8888", sessionLocator.getClientIpv6());
        assertEquals(ApplicationConstants.DEFAULT_IPV6, sessionLocator.getServerIpv6());
        assertTrue(sessionLocator.isIpv6());

        assertNull(sessionLocator.getClientIp());
        assertNull(sessionLocator.getServerIp());

        assertEquals(-1, sessionLocator.getServerPort());
        assertEquals(-1, sessionLocator.getClientPort());
    }

    @Test
    void parserSessionLocator_NoMetadata() throws IOException {
        var payload = readJsonFileAsJson("src/test/resources/writeRecord.json");

        var protoPayload = payload.getAsJsonObject(ApplicationConstants.PROTO_PAYLOAD);
        protoPayload.remove(ApplicationConstants.REQUEST_METADATA);

        var sessionLocator = Parser.parseSessionLocator(protoPayload);
        assertNotNull(sessionLocator);

        assertNull(sessionLocator.getClientIpv6());
        assertFalse(sessionLocator.isIpv6());
        assertNull(sessionLocator.getServerIpv6());

        assertEquals(ApplicationConstants.DEFAULT_IP, sessionLocator.getClientIp());
        assertEquals(ApplicationConstants.DEFAULT_IP, sessionLocator.getClientIp());

        assertEquals(-1, sessionLocator.getServerPort());
        assertEquals(-1, sessionLocator.getClientPort());
    }

    @Test
    void parserSessionLocator_InvalidMetadata_InvalidIpValue() throws IOException {
        var payload = readJsonFileAsJson("src/test/resources/writeRecord.json");

        var protoPayload = payload.getAsJsonObject(ApplicationConstants.PROTO_PAYLOAD);
        protoPayload.get(ApplicationConstants.REQUEST_METADATA).getAsJsonObject()
                .addProperty(ApplicationConstants.CALLER_IP, "I am wrong");

        var sessionLocator = Parser.parseSessionLocator(protoPayload);
        assertNotNull(sessionLocator);

        assertEquals(ApplicationConstants.DEFAULT_IP, sessionLocator.getClientIp());
        assertEquals(ApplicationConstants.DEFAULT_IP, sessionLocator.getServerIp());

        assertNull(sessionLocator.getClientIpv6());
        assertNull(sessionLocator.getServerIpv6());
        assertFalse(sessionLocator.isIpv6());

        assertEquals(-1, sessionLocator.getServerPort());
        assertEquals(-1, sessionLocator.getClientPort());
    }

    @Test
    void extractDatabaseDetailsTest() throws IOException {
        var payload = readJsonFileAsJson("src/test/resources/writeRecord.json");
        var protoPayload = payload.getAsJsonObject(ApplicationConstants.PROTO_PAYLOAD);
        var resource = Parser.getChildJsonObject(payload, ApplicationConstants.RESOURCE);

        var db = Parser.extractDatabaseDetails(resource, protoPayload);
        assertEquals("test-Project", db.projectId);
        assertEquals("test-big-table", db.instanceId);
        assertEquals("pets", db.tableId);
    }
    @Test
    void getResourceNameTest() throws IOException {
        var payload = readJsonFileAsJson("src/test/resources/writeRecord.json");
        var protoPayload = payload.getAsJsonObject(ApplicationConstants.PROTO_PAYLOAD);
        var resource = Parser.getChildJsonObject(payload, ApplicationConstants.RESOURCE);

        var db = Parser.extractDatabaseDetails(resource, protoPayload);
        String resourceName = Parser.getResourceName(db.projectId, db.instanceId, db.tableId);
        assertEquals("projects/test-Projectinstances/test-big-tabletables/pets", resourceName);
    }

    @Test
    void parseVerbTest() throws IOException {
        var payload = readJsonFileAsJson("src/test/resources/writeRecord.json");
        var protoPayload = payload.getAsJsonObject(ApplicationConstants.PROTO_PAYLOAD);

        String verb = Parser.getVerb(payload);
        assertNotNull(verb);
    }

    @Test
    void parseDataTest() throws IOException {
        var payload = readJsonFileAsJson("src/test/resources/writeRecord.json");
        var protoPayload = payload.getAsJsonObject(ApplicationConstants.PROTO_PAYLOAD);
        var resource = Parser.getChildJsonObject(payload, ApplicationConstants.RESOURCE);

        var db = Parser.extractDatabaseDetails(resource, protoPayload);

        var fullSql = Parser.getFullSql(db.projectId, db.instanceId, db.tableId, protoPayload);
        Data data = Parser.parseData(protoPayload, db.projectId, db.instanceId, db.tableId, fullSql);
        assertNotNull(data);

        assertEquals("MutateRow ON projects/test-Project/instances/test-big-table/tables/pets", data.getConstruct().fullSql);
        assertEquals(1, data.getConstruct().sentences.size());
        assertEquals("pets", data.getConstruct().sentences.get(0).getObjects().get(2).getName());
        assertEquals("table", data.getConstruct().sentences.get(0).getObjects().get(2).getType());

        assertEquals("test-big-table", data.getConstruct().sentences.get(0).getObjects().get(1).getName());
        assertEquals("instance", data.getConstruct().sentences.get(0).getObjects().get(1).getType());
    }

    @Test
    void parseCreateTest() throws IOException {
        var payload = readJsonFileAsJson("src/test/resources/createTable.json");

        var data = Parser.parseRecord(payload);

        assertNotNull(data);
    }

    @Test
    void parseExecuteQueryTest() throws IOException {
        var payload = readJsonFileAsJson("src/test/resources/executeQuery.json");
        var data = Parser.parseRecord(payload);
        assertNotNull(data);
        
        assertEquals("executeQuery ON projects/test-project-1234/instances/testbigtable; readRows ON projects/test-project-1234/instances/testbigtable/tables/students", data.getData().getConstruct().getFullSql());

    }

    @Test
    void parseReadTest() throws IOException {
        var payload = readJsonFileAsJson("src/test/resources/readRecord.json");
        var data = Parser.parseRecord(payload);
        assertNotNull(data);
        assertEquals("ReadRows ON projects/test-Project/instances/test-big-table/tables/pets", data.getData().getConstruct().getFullSql());
    }

    @Test
    void parseWriteTest() throws IOException {
        var payload = readJsonFileAsJson("src/test/resources/writeRecord.json");
        var data = Parser.parseRecord(payload);
        assertNotNull(data);
        assertEquals("MutateRow ON projects/test-Project/instances/test-big-table/tables/pets", data.getData().getConstruct().getFullSql());

    }

    @Test
    void parseCreateTableTest() throws IOException {
        var payload = readJsonFileAsJson("src/test/resources/createTable.json");
        var data = Parser.parseRecord(payload);
        assertNotNull(data);
        assertEquals("CreateTable ON projects/test-Project/instances/test-bigtable/tables/employeeT", data.getData().getConstruct().getFullSql());
    }

    @Test
    void parseDeleteTableTest() throws IOException {
        var payload = readJsonFileAsJson("src/test/resources/deleteTable.json");
        var data = Parser.parseRecord(payload);
        assertNotNull(data);
        assertEquals("DeleteTable ON projects/test-project-1234/instances/test-big-table/tables/student", data.getData().getConstruct().getFullSql());

    }
    
    @Test
    void emptyResourceTest() throws IOException {
        var payload = readJsonFileAsJson("src/test/resources/CreateInstanceWithClusterRequest.json");
        var data = Parser.parseRecord(payload);
        assertNotNull(data);
    }

    @Test
    void parseUpdateTableTest() throws IOException {
        var payload = readJsonFileAsJson("src/test/resources/updateTable.json");
        var data = Parser.parseRecord(payload);
        assertNotNull(data);
        assertEquals("UpdateTable ON projects/test-project-1234/instances/test-bigtable/tables/employee", data.getData().getConstruct().getFullSql());
    }

    @Test
    void parseListClustersTest() throws IOException {
        var payload = readJsonFileAsJson("src/test/resources/listClusters.json");
        var data = Parser.parseRecord(payload);
        assertNotNull(data);
        assertEquals("ListClusters ON projects/test-project-1234/instances/testbigtable", data.getData().getConstruct().getFullSql());
    }

    @Test
    void clusterTest() throws IOException {
        var payload = readJsonFileAsJson("src/test/resources/CreateInstanceWithClusterRequest.json");
        var protoPayload = payload.getAsJsonObject(ApplicationConstants.PROTO_PAYLOAD);

        var request = Parser.getChildJsonObject(protoPayload, ApplicationConstants.REQUEST);
        assertNotNull(request);

        var clusters = Parser.getChildString(request, "clusters");
        assertNotNull(clusters);
        assertEquals("{\"createinstancetestcluster\":{\"location\":\"projects/charged-mind-281913/locations/northamerica-northeast2-a\",\"serveNodes\":1,\"defaultStorageType\":\"SSD\"},\"createinstancetest-c1\":{\"defaultStorageType\":\"SSD\",\"location\":\"projects/charged-mind-281913/locations/northamerica-northeast2-c\",\"serveNodes\":1}}",clusters);
    }
    @Test
    void parseListInstancesTest() throws IOException {
        var payload = readJsonFileAsJson("src/test/resources/listInstances.json");
        var data = Parser.parseRecord(payload);
        assertNotNull(data);
        assertEquals("ListInstances ON projects/test-project-1234", data.getData().getConstruct().getFullSql());
    }

    @Test
    void parseDeleteInstanceTest() throws IOException {
        var payload = readJsonFileAsJson("src/test/resources/deleteInstance.json");
        var data = Parser.parseRecord(payload);
        assertNotNull(data.getData());
        assertNotNull(data.getException());
    }

    @Test
    void exceptionTest_noException() throws IOException {
        var payload = readJsonFileAsJson("src/test/resources/writeRecord.json");
        var protoPayload = payload.getAsJsonObject(ApplicationConstants.PROTO_PAYLOAD);

        var verb = Parser.getVerb(protoPayload);
        var resource = Parser.getChildJsonObject(payload, ApplicationConstants.RESOURCE);
        var db = Parser.extractDatabaseDetails(resource, protoPayload);

        String fullSql = Parser.getFullSql(db.projectId,db.instanceId,db.tableId, payload);

        ExceptionRecord exceptionRecord = Parser.parseException(
                Parser.getChildString(payload, ApplicationConstants.SEVERITY),
                Parser.getChildJsonObject(protoPayload, ApplicationConstants.STATUS), fullSql);

        assertNull(exceptionRecord);
    }

    @Test
    void parseAccessorTest() throws IOException {
        var payload = readJsonFileAsJson("src/test/resources/writeRecord.json");
        var protoPayload = payload.getAsJsonObject(ApplicationConstants.PROTO_PAYLOAD);

        var username = Parser.getAppUserName(protoPayload);
        var resource = Parser.getChildJsonObject(payload, ApplicationConstants.RESOURCE);
        var db = Parser.extractDatabaseDetails(resource, protoPayload);
        var serviceName = Parser.getFieldValueByKey(protoPayload, ApplicationConstants.SERVICE_NAME);
        var accessor = Parser.parseAccessor(username, db.projectId, serviceName);

        assertNotNull(accessor);
        assertEquals(ApplicationConstants.UNKOWN_STRING, accessor.getClient_mac());
        assertEquals(ApplicationConstants.UNKOWN_STRING, accessor.getClientHostName());
        assertEquals(ApplicationConstants.UNKOWN_STRING, accessor.getClientOs());
        assertEquals(ApplicationConstants.UNKOWN_STRING, accessor.getCommProtocol());
        assertEquals("CONSTRUCT", accessor.getDataType());
        assertEquals("BigTable(GCP)", accessor.getDbProtocol());
        assertEquals(ApplicationConstants.UNKOWN_STRING, accessor.getDbProtocolVersion());
        assertEquals("test1@abc.com", accessor.getDbUser());
        assertEquals("FREE_TEXT", accessor.getLanguage());
        assertEquals(ApplicationConstants.UNKOWN_STRING, accessor.getOsUser());
        assertEquals(ApplicationConstants.UNKOWN_STRING, accessor.getServerDescription());
        assertEquals("test-Project_bigtable.googleapis.com", accessor.getServerHostName());
        assertEquals(ApplicationConstants.UNKOWN_STRING, accessor.getServerOs());
        assertEquals("BigTable", accessor.getServerType());
        assertEquals("bigtable.googleapis.com", accessor.getServiceName());
        assertEquals(ApplicationConstants.UNKOWN_STRING, accessor.getSourceProgram());
    }

    @Test
    void parseSessionLocatorTest() throws IOException {
        var payload = readJsonFileAsJson("src/test/resources/readRecord.json");
        var protoPayload = payload.getAsJsonObject(ApplicationConstants.PROTO_PAYLOAD);

        SessionLocator sessionLocator = Parser.parseSessionLocator(protoPayload);
        assertNotNull(sessionLocator);
        assertEquals("0.0.0.0", sessionLocator.getServerIp());
        assertEquals(-1, sessionLocator.getServerPort());
        assertEquals("174.119.174.177", sessionLocator.getClientIp());
        assertEquals(-1, sessionLocator.getClientPort());
    }
    @Test
    void parseIpv6LocatorTest() throws IOException {
        var payload = readJsonFileAsJson("src/test/resources/listInstances.json");
        var protoPayload = payload.getAsJsonObject(ApplicationConstants.PROTO_PAYLOAD);

        SessionLocator sessionLocator = Parser.parseSessionLocator(protoPayload);
        assertNotNull(sessionLocator);

        assertEquals("2607:fea8:8522:9e00:ac0e:5400:b6a3:6f1f", sessionLocator.getClientIpv6());
        assertEquals(-1, sessionLocator.getClientPort());
    }

    @Test
    void requestTypeTest() throws IOException {
        var payload = readJsonFileAsJson("src/test/resources/AddColumn.json");
        var protoPayload = Parser.getChildJsonObject(payload, ApplicationConstants.PROTO_PAYLOAD);
        assertNotNull(protoPayload);

        var request = Parser.getChildJsonObject(protoPayload, ApplicationConstants.REQUEST);
        var type = Parser.getChildString(request, "@type");

        assertEquals("type.googleapis.com/google.bigtable.admin.v2.ModifyColumnFamiliesRequest", type);
    }

    @Test
    void serviceAccountKeyNameTest() throws IOException {
        var payload = readJsonFileAsJson("src/test/resources/multipleReadRecord.json");
        var protoPayload = Parser.getChildJsonObject(payload, ApplicationConstants.PROTO_PAYLOAD);
        assertNotNull(protoPayload);

        var authenticationInfo = Parser.getChildJsonObject(protoPayload, ApplicationConstants.AUTHENTICATION_INFO);
        var principalEmail = Parser.getChildString(authenticationInfo, "principalEmail");
        var serviceAccountKeyName = Parser.getChildString(authenticationInfo, "serviceAccountKeyName");
        var principalSubject = Parser.getChildString(authenticationInfo, "principalSubject");

        assertEquals("apachesolrgcp-pubsub@charged-mind-281913.iam.gserviceaccount.com", principalEmail);
        assertEquals("//iam.googleapis.com/projects/charged-mind-281913/serviceAccounts/apachesolrgcp-pubsub@charged-mind-281913.iam.gserviceaccount.com/keys/960653a95002d5b9c230ad1631ac38f3aee2123f", serviceAccountKeyName);
        assertEquals("serviceAccount:apachesolrgcp-pubsub@charged-mind-281913.iam.gserviceaccount.com", principalSubject);
    }

    @Test
    void operationTest() throws IOException {
        var payload = readJsonFileAsJson("src/test/resources/CreateInstanceWithClusterRequest.json");
        var operation = Parser.getChildJsonObject(payload,ApplicationConstants.OPERATION_KEY);

        var id = Parser.getChildString(operation, "id");
        var producer = Parser.getChildString(operation, "producer");
        boolean first = Boolean.parseBoolean(Parser.getChildString(operation, "first"));

        assertEquals("operations/projects/charged-mind-281913/instances/createinstancetest/locations/northamerica-northeast2-a/operations/5003528313522199720", id);
        assertEquals("bigtableadmin.googleapis.com", producer);
        assertTrue(first);
    }

    @Test
    void exceptionTest() throws IOException {
        var payload = readJsonFileAsJson("src/test/resources/error.json");
        var protoPayload = payload.getAsJsonObject(ApplicationConstants.PROTO_PAYLOAD);

        var resource = Parser.getChildJsonObject(payload, ApplicationConstants.RESOURCE);
        var db = Parser.extractDatabaseDetails(resource, protoPayload);

        String fullSql = Parser.getFullSql(db.projectId,db.instanceId,db.tableId, payload);
        ExceptionRecord exceptionRecord = Parser.parseException(
                Parser.getChildString(payload, ApplicationConstants.SEVERITY),
                Parser.getChildJsonObject(protoPayload, ApplicationConstants.STATUS), fullSql);

        assertNotNull(exceptionRecord);
        assertEquals("SQL_ERROR", exceptionRecord.getExceptionTypeId());
        assertEquals("Status Code: 404, Description: Table not found.", exceptionRecord.getDescription());
        assertEquals(fullSql.toString(), exceptionRecord.getSqlString());
    }

    @Test
    void exceptionTestForSyntheticallyIncorrectSQLRepport() throws IOException {
        var payload = readJsonFileAsJson("src/test/resources/error2.json");
        var protoPayload = payload.getAsJsonObject(ApplicationConstants.PROTO_PAYLOAD);

        var resource = Parser.getChildJsonObject(payload, ApplicationConstants.RESOURCE);
        var db = Parser.extractDatabaseDetails(resource, protoPayload);

        String fullSql = Parser.getFullSql(db.projectId,db.instanceId,db.tableId, payload);
        ExceptionRecord exceptionRecord = Parser.parseException(
                Parser.getChildString(payload, ApplicationConstants.SEVERITY),
                Parser.getChildJsonObject(protoPayload, ApplicationConstants.STATUS), fullSql);

        assertNotNull(exceptionRecord);
        assertEquals("SQL_ERROR", exceptionRecord.getExceptionTypeId());
        assertEquals("Status Code: 3, Description: Syntax error: Expected end of input but got identifier \\\"FRO\\\" [at 1:10]", exceptionRecord.getDescription());
        assertEquals(fullSql.toString(), exceptionRecord.getSqlString());
    }

    @Test
    void exceptionTestForSyntheticallyIncorrectFulLSQlRepport() throws IOException {
        var payload = readJsonFileAsJson("src/test/resources/error2.json");
        var protoPayload = payload.getAsJsonObject(ApplicationConstants.PROTO_PAYLOAD);

        var data = Parser.parseRecord(payload);
        assertNotNull(data);
        assertEquals("executeQuery ON projects/test-project/instances/testTable", data.getData().getConstruct().getFullSql());
    }

    @Test
    void illegalDeleteTest() throws IOException {
        var payload = readJsonFileAsJson("src/test/resources/errorLogs/CannotDeleteTable.json");
        var protoPayload = payload.getAsJsonObject(ApplicationConstants.PROTO_PAYLOAD);

        var resource = Parser.getChildJsonObject(payload, ApplicationConstants.RESOURCE);
        var db = Parser.extractDatabaseDetails(resource, protoPayload);

        String fullSql = Parser.getFullSql(db.projectId,db.instanceId,db.tableId, payload);
        ExceptionRecord exceptionRecord = Parser.parseException(
                Parser.getChildString(payload, ApplicationConstants.SEVERITY),
                Parser.getChildJsonObject(protoPayload, ApplicationConstants.STATUS), fullSql);

        assertNotNull(exceptionRecord);
        assertTrue(exceptionRecord.getExceptionTypeId().contains("SQL_ERROR"));
        assertTrue(exceptionRecord.getDescription().contains("Unable"));
        assertEquals(fullSql.toString(), exceptionRecord.getSqlString());
    }

    @Test
    void notFoundTest() throws IOException {
        String[] payloadFiles = {
                "src/test/resources/errorLogs/InstanceNotFound.json",
                "src/test/resources/errorLogs/TableNotFound.json"
        };

        for (String filePath : payloadFiles) {
            var payload = readJsonFileAsJson(filePath);
            var protoPayload = payload.getAsJsonObject(ApplicationConstants.PROTO_PAYLOAD);

            var resource = Parser.getChildJsonObject(payload, ApplicationConstants.RESOURCE);
            var db = Parser.extractDatabaseDetails(resource, protoPayload);

            String fullSql = Parser.getFullSql(db.projectId,db.instanceId,db.tableId, payload);
            ExceptionRecord exceptionRecord = Parser.parseException(
                    Parser.getChildString(payload, ApplicationConstants.SEVERITY),
                    Parser.getChildJsonObject(protoPayload, ApplicationConstants.STATUS), fullSql);

            assertNotNull(exceptionRecord);
            assertTrue(exceptionRecord.getExceptionTypeId().contains("SQL_ERROR"));
            assertTrue(exceptionRecord.getDescription().contains("not found"));
            assertEquals(fullSql.toString(), exceptionRecord.getSqlString());
        }
    }

    @Test
    void parseTimeTest() throws IOException {
        var payload = readJsonFileAsJson("src/test/resources/readRecord.json");
        var timestamp = Parser.getFieldValueByKey(payload, ApplicationConstants.TIMESTAMP);

        var time = Parser.parseTime(timestamp);
        assertNotNull(time);
        assertEquals(1722360499932L, time.getTimstamp());
    }

    @Test
    void filterTestError() throws IOException {
        var payload = readJsonFileAsJson("src/test/resources/executeQueryError.json");

        var record = Parser.parseRecord(payload);
        assertNotNull(record);

        assertNotNull(record.getData());
        assertNotNull(record.getException());

        var exception = record.getException();
        assertEquals("Status Code: 3, Description: Syntax error: Unexpected \\\"<\\\" [at 4:9]", exception.getDescription());
        assertEquals("SQL_ERROR", exception.getExceptionTypeId());
        assertEquals("executeQuery ON projects/test-project-1234/instances/testbigtabletest", exception.getSqlString());
    }

    @Test
    void authenticationInfoTest() throws IOException {
        var payload = readJsonFileAsJson("src/test/resources/authenticationInfo.json");

        var record = Parser.parseRecord(payload);
        assertNotNull(record);
        assertNull(record.getException());
        assertEquals("ReadRows ON projects/test-project-1234/instances/testbigtable/tables/students", record.getData().getConstruct().redactedSensitiveDataSql);
    }

    private String readJsonFile(String fileName) throws IOException {
        return new String(Files.readAllBytes(Paths.get(fileName)));
    }

    private JsonObject readJsonFileAsJson(String fileName) throws IOException {
        return new Gson().fromJson(readJsonFile(fileName), JsonObject.class);
    }
}