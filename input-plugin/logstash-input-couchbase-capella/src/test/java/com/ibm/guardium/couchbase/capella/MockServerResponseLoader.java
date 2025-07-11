package com.ibm.guardium.couchbase.capella;

import com.google.gson.Gson;
import org.apache.http.HttpStatus;
import org.junit.Assert;
import org.mockserver.integration.ClientAndServer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.mockserver.model.Header.header;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.model.Parameter.param;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;

public class MockServerResponseLoader {

    private final ClientAndServer server;
    private final String apiHost;

    public MockServerResponseLoader(ClientAndServer server, String apiHost) {
        this.server = server;
        this.apiHost = apiHost;
        Assert.assertNotNull(this.server);
        Assert.assertNotNull(apiHost);
        Assert.assertFalse(apiHost.isBlank());
    }

    public void createExportJob_LoadsSuccessfulResponses(String exportJobID) {
        String path = String.format("/%s/organizations/{orgId}/projects/{projID}/clusters/{clusterID}/auditLogExports",
                this.apiHost);
        String body = String.format("{\"exportId\":\"%s\"}", exportJobID);
        this.server.when(
                        request()
                                .withMethod("POST")
                                .withPath(path)
                                .withPathParameters(
                                        // TODO write some conditions
                                        param("orgId", ".*"),
                                        param("projID", ".*"),
                                        param("clusterID", ".*")
                                )
                                .withHeader(
                                        header("Authorization", ".*good.*")
                                )
                )
                .respond(
                        response()
                                .withStatusCode(HttpStatus.SC_ACCEPTED)
                                .withBody(body)
                )
        ;
    }

    public void createExportJob_LoadsBadAuthToken() {
        String path = String.format("/%s/organizations/{orgId}/projects/{projID}/clusters/{clusterID}/auditLogExports",
                this.apiHost);
        String body = "{\n" +
                "    \"code\": 1001,\n" +
                "    \"hint\": \"The request is unauthorized. Please ensure you have provided appropriate credentials " +
                                "in the request header. Please make sure the client IP that is trying to access the " +
                "                 resource using the API key is in the API key allowlist.\",\n" +
                "    \"httpStatusCode\": 401,\n" +
                "    \"message\": \"Unauthorized\"\n" +
                "}";
        this.server.when(
                        request()
                                .withMethod("POST")
                                .withPath(path)
                                .withPathParameters(
                                        // TODO write some conditions
                                        param("orgId", ".*"),
                                        param("projID", ".*"),
                                        param("clusterID", ".*")
                                )
                                .withHeader(
                                        header("Authorization", "bad.*")
                                )
                )
                .respond(
                        response()
                                .withStatusCode(HttpStatus.SC_UNAUTHORIZED)
                                .withBody(body)
                )
        ;
    }


    // Status is Enum: "In Progress" "Completed" "Queued" "Failed"
    public void getExportJobStatus_LoadSuccessfulResponse(String jobId, String downloadURL, String status) {
        String path = String.format("/%s/organizations/{orgId}/projects/{projID}/clusters/{clusterID}/auditLogExports/{jobId}",
                this.apiHost);

        Gson gson = new Gson();
        var res = new GetClusterAuditLogExportResponse();
        res.auditLogDownloadURL = downloadURL;
        res.status = status;
        String body = gson.toJson(res);

        this.server.when(
                        request()
                                .withMethod("GET")
                                .withPath(path)
                                .withPathParameters(
                                        param("orgId", "success.*"),
                                        param("projID", "success.*"),
                                        param("clusterID", "success.*"),
                                        param("jobId", jobId)
                                )
                )
                .respond(
                        response()
                                .withStatusCode(HttpStatus.SC_OK)
                                .withBody(body)
                )
        ;
    }

    public void getExportJobStatus_LoadErrorResponse(String jobId, ErrorResponse errorResponse) {
        String path = String.format("/%s/organizations/{orgId}/projects/{projID}/clusters/{clusterID}/auditLogExports/{jobId}",
                this.apiHost);

        Gson gson = new Gson();
        String body = gson.toJson(errorResponse);

        this.server.when(
                        request()
                                .withMethod("GET")
                                .withPath(path)
                                .withPathParameters(
                                        param("orgId", "bad.*"),
                                        param("projID", "bad.*"),
                                        param("clusterID", "bad.*"),
                                        param("jobId", jobId)
                                )
                )
                .respond(
                        response()
                                .withStatusCode(errorResponse.httpStatusCode)
                                .withBody(body)
                )
        ;
    }

    public void downloadAuditLogFile_LoadSuccessfulResponse(String mockPayloadFilePath) throws IOException {

        Path currentRelativePath = Paths.get("");
        String s = currentRelativePath.toAbsolutePath().toString();
        System.out.println("Current absolute path is: " + s);

        var allBytes = Files.readAllBytes(Paths.get(mockPayloadFilePath));

        this.server.when(
                        request()
                                .withMethod("GET")
                                .withPath(".*/good/file.*")
                )
                .respond(
                        response()
                                .withStatusCode(HttpStatus.SC_OK)
                                .withBody(allBytes)
                )
        ;
    }

}
