package com.ibm.guardium.couchbase.capella;

import org.apache.http.HttpStatus;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockserver.configuration.ConfigurationProperties;
import org.mockserver.integration.ClientAndServer;

import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.stop.Stop.stopQuietly;

public class GetExportJobStatusTest {

    private static ClientAndServer mockServer;
    private static final int mockServerPort=1080;
    private static final String mockServerHost="localhost";
    private static final String mockServerApiBasePath="mockcapella";
    private static MockServerResponseLoader loader;

    @BeforeClass
    public static void startMockServer() {
        ConfigurationProperties.logLevel("OFF");

        mockServer = startClientAndServer(mockServerPort);
        loader = new MockServerResponseLoader(mockServer, mockServerApiBasePath);
    }
    @AfterClass
    public static void stopMockServer() {
        stopQuietly(mockServer);
    }

    @Test
    public void testGetExportJobStatusComplete() {
        CapellaAuditLogApiManager mgr = new CapellaAuditLogApiManager();

        var baseUrl = String.format("http://%s:%d/%s", mockServerHost, mockServerPort, mockServerApiBasePath);
        var orgID = "success-org";
        var projID = "success-project";
        var clusterID = "success-cluster";
        var token = "Bearer good_token";
        var jobID = "i-am-a-complete-job";
        var expectedDownloadURL = "http://localhost:1080/i/am/a/good/dowload-url";
        loader.getExportJobStatus_LoadSuccessfulResponse(jobID, expectedDownloadURL, "Completed");

        var actualDownloadUrl = mgr.getExportJobStatus(baseUrl, orgID, projID, clusterID, token, jobID);
        Assert.assertEquals(expectedDownloadURL, actualDownloadUrl);
    }

    @Test
    public void testGetExportJobStatusAlwaysInProgress() {
        CapellaAuditLogApiManager mgr = new CapellaAuditLogApiManager();

        var baseUrl = String.format("http://%s:%d/%s", mockServerHost, mockServerPort, mockServerApiBasePath);
        var orgID = "success-org";
        var projID = "success-project";
        var clusterID = "success-cluster";
        var token = "Bearer good_token";
        var jobID = "i-am-always-in-progress";
        var expectedDownloadURL = "http://localhost:1080/i/am/a/good/dowload-url";
        loader.getExportJobStatus_LoadSuccessfulResponse(jobID, expectedDownloadURL, "In Progress");

        var actualDownloadUrl = mgr.getExportJobStatusWithRetry(baseUrl, orgID, projID, clusterID, token, jobID, 0);
        Assert.assertNull(actualDownloadUrl);
    }

    @Test
    public void testGetExportJobStatusWithErrorResponse() {
        CapellaAuditLogApiManager mgr = new CapellaAuditLogApiManager();

        var errorRes = new ErrorResponse();
        errorRes.httpStatusCode = HttpStatus.SC_UNAUTHORIZED;
        errorRes.code = 1001;
        errorRes.message = "Unauthorized";
        errorRes.hint = "The request is unauthorized. Please ensure you have provided appropriate credentials in the request header. " +
                "Please make sure the client IP that is trying to access the resource using the API key is in the API key allowlist.";

        var baseUrl = String.format("http://%s:%d/%s", mockServerHost, mockServerPort, mockServerApiBasePath);
        var orgID = "bad-org";
        var projID = "bad-project";
        var clusterID = "bad-cluster";
        var token = "Bearer bad_token";
        var jobID = "i-am-error-job"+errorRes.httpStatusCode+errorRes.message;

        loader.getExportJobStatus_LoadErrorResponse(jobID, errorRes);

        var actualDownloadUrl = mgr.getExportJobStatusWithRetry(baseUrl, orgID, projID, clusterID, token, jobID, 0);
        Assert.assertNull(actualDownloadUrl);
    }

    @Test
    public void testGetExportJobStatusInvalidUrl() {
        CapellaAuditLogApiManager mgr = new CapellaAuditLogApiManager();

        var errorRes = new ErrorResponse();
        errorRes.httpStatusCode = HttpStatus.SC_UNAUTHORIZED;
        errorRes.code = 1001;
        errorRes.message = "Unauthorized";
        errorRes.hint = "The request is unauthorized. Please ensure you have provided appropriate credentials in the request header. " +
                "Please make sure the client IP that is trying to access the resource using the API key is in the API key allowlist.";

        var baseUrl = String.format("htp:/invalid-url");
        var orgID = "bad-org";
        var projID = "bad-project";
        var clusterID = "bad-cluster";
        var token = "Bearer bad_token";
        var jobID = "i-am-error-job"+errorRes.httpStatusCode+errorRes.message;

        loader.getExportJobStatus_LoadErrorResponse(jobID, errorRes);

        var actualDownloadUrl = mgr.getExportJobStatusWithRetry(baseUrl, orgID, projID, clusterID, token, jobID, 0);

        Assert.assertNull(actualDownloadUrl);
    }
}
