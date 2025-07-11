package com.ibm.guardium.couchbase.capella;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockserver.configuration.ConfigurationProperties;
import org.mockserver.integration.ClientAndServer;

import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.stop.Stop.stopQuietly;

public class DownloadAuditLogFileTest {

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
    public void testDownloadAuditLogFileSuccessfully() {

        CapellaAuditLogApiManager mgr = new CapellaAuditLogApiManager();

        var baseUrl = String.format("http://%s:%d", mockServerHost, mockServerPort);
        var downloadUrlPath = "/audit-log-files/good/file/cluster-audit-logs-91c27e7a";
        var downloadUrl = baseUrl + downloadUrlPath;

        try {
            loader.downloadAuditLogFile_LoadSuccessfulResponse("src/test/resources/mocks/mock-audit-log.tar.gz");
        } catch (Exception e) {
            Assert.assertNull(e);
        }

        var auditLog = mgr.downloadAuditLogFile(downloadUrl);
        Assert.assertNotNull(auditLog);
        Assert.assertNotEquals("", auditLog);
    }

    @Test
    public void testDownloadAuditLogFileNotFound() {

        CapellaAuditLogApiManager mgr = new CapellaAuditLogApiManager();

        var baseUrl = String.format("http://%s:%d", mockServerHost, mockServerPort);
        var downloadUrlPath = "/audit-log-files/unknown-file";
        var downloadUrl = baseUrl + downloadUrlPath;

        var auditLog = mgr.downloadAuditLogFileWithRetry(downloadUrl, 0);
        Assert.assertNull(auditLog);
    }
}
