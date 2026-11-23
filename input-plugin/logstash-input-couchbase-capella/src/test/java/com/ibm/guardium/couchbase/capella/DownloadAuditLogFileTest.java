
package com.ibm.guardium.couchbase.capella;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockserver.configuration.ConfigurationProperties;
import org.mockserver.integration.ClientAndServer;

import java.util.ArrayList;
import java.util.List;

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

        List<String> lines = new ArrayList<>();
        boolean success = mgr.downloadAuditLogFile(downloadUrl, lines::add);
        Assert.assertTrue(success);
        Assert.assertFalse(lines.isEmpty());
    }

    @Test
    public void testDownloadAuditLogFileNotFound() {

        CapellaAuditLogApiManager mgr = new CapellaAuditLogApiManager();

        var baseUrl = String.format("http://%s:%d", mockServerHost, mockServerPort);
        var downloadUrlPath = "/audit-log-files/unknown-file";
        var downloadUrl = baseUrl + downloadUrlPath;

        List<String> lines = new ArrayList<>();
        boolean success = mgr.downloadAuditLogFileWithRetry(downloadUrl, lines::add, 0);
        Assert.assertFalse(success);
        Assert.assertTrue(lines.isEmpty());
    }
}
