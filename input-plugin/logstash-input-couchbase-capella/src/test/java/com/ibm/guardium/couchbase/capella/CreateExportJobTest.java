package com.ibm.guardium.couchbase.capella;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockserver.configuration.ConfigurationProperties;
import org.mockserver.integration.ClientAndServer;

import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.stop.Stop.stopQuietly;

public class CreateExportJobTest {


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
    public void shouldCreateExportJob() {
        var expectedExportJobID = "ef53edc8-cb96-457a-afcc-39e005c0d522";
        loader.createExportJob_LoadsSuccessfulResponses(expectedExportJobID);

        var baseUrl = String.format("http://%s:%d/%s", mockServerHost, mockServerPort, mockServerApiBasePath);
        var orgID = "success-org";
        var projID = "success-project";
        var clusterID = "success-cluster";
        var token = "Bearer good_token";
        var startTime = "2025-03-10T04:56:07.000+00:00";
        var endTime = "2025-03-12T10:05:07.000+00:00";
        CapellaAuditLogApiManager mgr = new CapellaAuditLogApiManager();
        var actualExportJobID = mgr.createExportJob(baseUrl, orgID, projID, clusterID, token, startTime, endTime);

        Assert.assertEquals(expectedExportJobID, actualExportJobID);

    }

    @Test
    public void invalidUrl() {
        CapellaAuditLogApiManager mgr = new CapellaAuditLogApiManager();
        var baseUrl = String.format("I_AM_INVALID://%s:%d/%s", mockServerHost, mockServerPort, mockServerApiBasePath);
        var orgID = "success-org";
        var projID = "success-project";
        var clusterID = "success-cluster";
        var token = "Bearer fake_token_abc";
        var startTime = "2025-03-10T04:56:07.000+00:00";
        var endTime = "2025-03-12T10:05:07.000+00:00";
        var actualExportJobID = mgr.createExportJob(baseUrl, orgID, projID, clusterID, token, startTime, endTime);
        Assert.assertNull(actualExportJobID);
    }

    @Test
    public void invalidAuthorization() {
        loader.createExportJob_LoadsBadAuthToken();

        var baseUrl = String.format("http://%s:%d/%s", mockServerHost, mockServerPort, mockServerApiBasePath);
        var orgID = "success-org";
        var projID = "success-project";
        var clusterID = "success-cluster";
        var token = "bad_token";
        var startTime = "2025-03-10T04:56:07.000+00:00";
        var endTime = "2025-03-12T10:05:07.000+00:00";
        CapellaAuditLogApiManager mgr = new CapellaAuditLogApiManager();
        var actualExportJobID = mgr.createExportJob(baseUrl, orgID, projID, clusterID, token, startTime, endTime);
        Assert.assertNull(actualExportJobID);
    }

}
