package org.logstashplugins;

import co.elastic.logstash.api.Configuration;
import co.elastic.logstash.api.PluginConfigSpec;
import com.ibm.guardium.couchbase.capella.MockServerResponseLoader;
import org.apache.commons.lang3.StringUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.logstash.plugins.ConfigurationImpl;
import org.mockserver.configuration.ConfigurationProperties;
import org.mockserver.integration.ClientAndServer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.stop.Stop.stopQuietly;

public class CouchbaseCapellaInputTest {

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
    public void testCouchbaseCapellaInputTest() {

        String exportedJobID = "ef53edc8-cb96-457a-afcc-39e005c0d522";
        loader.createExportJob_LoadsSuccessfulResponses(exportedJobID);
        var expectedDownloadURL = "http://localhost:1080/i/am/a/good/file/dowload-url";
        loader.getExportJobStatus_LoadSuccessfulResponse(exportedJobID, expectedDownloadURL, "Completed");
        try {
            loader.downloadAuditLogFile_LoadSuccessfulResponse("src/test/resources/mocks/mock-audit-log.tar.gz");
        } catch (Exception e) {
            Assert.assertNull(e);
        }


        var baseUrl = String.format("http://%s:%d/%s", mockServerHost, mockServerPort, mockServerApiBasePath);
        Map<String, Object> configValues = new HashMap<>();
        configValues.put(CouchbaseCapellaInput.INTERVAL_CONFIG.name(), 2L);
        configValues.put(CouchbaseCapellaInput.QUERY_LENGTH_CONFIG.name(), 20 * 60L);
        configValues.put(CouchbaseCapellaInput.API_BASE_URL_CONFIG.name(), baseUrl);
        configValues.put(CouchbaseCapellaInput.ORG_ID_CONFIG.name(), "success-org");
        configValues.put(CouchbaseCapellaInput.PROJECT_ID_CONFIG.name(), "success-project");
        configValues.put(CouchbaseCapellaInput.CLUSTER_ID_CONFIG.name(), "success-cluster");
        configValues.put(CouchbaseCapellaInput.AUTH_TOKEN_CONFIG.name(), "Bearer good_token");

        Configuration config = new ConfigurationImpl(configValues);
        CouchbaseCapellaInput input = new CouchbaseCapellaInput("test-id", config, null);


        CouchbaseCapellaInputTest.TestConsumer testConsumer = new CouchbaseCapellaInputTest.TestConsumer();

        // Thread 2: Wait for a while and then stop Thread 1
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    TimeUnit.SECONDS.sleep(3);
                } catch (InterruptedException e) {
                    Assert.assertNotNull(e);
                }
                input.stop();
            }
        });
        thread.start();


        input.start(testConsumer);

        List<Map<String, Object>> events = testConsumer.getEvents();
        for (int k = 1; k <= events.size(); k++) {
            var actualMessage = events.get(k - 1).get("message");
            Assert.assertNotNull(actualMessage);
            Assert.assertFalse(actualMessage.toString().isEmpty());
            var actualPluginType = events.get(k - 1).get("type");
            Assert.assertEquals(input.pluginType, actualPluginType);
            var orgID = events.get(k - 1).get("organizationID");
            Assert.assertEquals("success-org", orgID);

        }
    }

    @Test
    public void testCouchbaseCapellaInput_EpochToISO8601() {

        TimeZone originalTimeZone = TimeZone.getDefault();

        try {
            TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
            var baseUrl = String.format("http://%s:%d/%s", mockServerHost, mockServerPort, mockServerApiBasePath);
            Map<String, Object> configValues = new HashMap<>();
            configValues.put(CouchbaseCapellaInput.INTERVAL_CONFIG.name(), 2L);
            configValues.put(CouchbaseCapellaInput.QUERY_LENGTH_CONFIG.name(), 20 * 60L);
            configValues.put(CouchbaseCapellaInput.API_BASE_URL_CONFIG.name(), baseUrl);
            configValues.put(CouchbaseCapellaInput.ORG_ID_CONFIG.name(), "success-org");
            configValues.put(CouchbaseCapellaInput.PROJECT_ID_CONFIG.name(), "success-project");
            configValues.put(CouchbaseCapellaInput.CLUSTER_ID_CONFIG.name(), "success-cluster");
            configValues.put(CouchbaseCapellaInput.AUTH_TOKEN_CONFIG.name(), "Bearer good_token");

            Configuration config = new ConfigurationImpl(configValues);
            CouchbaseCapellaInput input = new CouchbaseCapellaInput("test-id", config, null);

            var dateTimeStr = input.epochSecToISO8601DateTimeString(1747702429L);
            Assert.assertEquals("2025-05-20T00:53:49Z", dateTimeStr);
        } finally {
            TimeZone.setDefault(originalTimeZone); // Restore after test
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCouchbaseCapellaInput_EmptyBaseUrl() {

        Map<String, Object> configValues = new HashMap<>();
        configValues.put(CouchbaseCapellaInput.INTERVAL_CONFIG.name(), 2L);
        configValues.put(CouchbaseCapellaInput.QUERY_LENGTH_CONFIG.name(), 20 * 60L);
        configValues.put(CouchbaseCapellaInput.API_BASE_URL_CONFIG.name(), "");
        configValues.put(CouchbaseCapellaInput.ORG_ID_CONFIG.name(), "success-org");
        configValues.put(CouchbaseCapellaInput.PROJECT_ID_CONFIG.name(), "success-project");
        configValues.put(CouchbaseCapellaInput.CLUSTER_ID_CONFIG.name(), "success-cluster");
        configValues.put(CouchbaseCapellaInput.AUTH_TOKEN_CONFIG.name(), "Bearer good_token");

        Configuration config = new ConfigurationImpl(configValues);
        CouchbaseCapellaInput input = new CouchbaseCapellaInput("test-id", config, null);

    }

    @Test(expected = IllegalArgumentException.class)
    public void testCouchbaseCapellaInput_EmptyOrgID() {


        var baseUrl = String.format("http://%s:%d/%s", mockServerHost, mockServerPort, mockServerApiBasePath);
        Map<String, Object> configValues = new HashMap<>();
        configValues.put(CouchbaseCapellaInput.INTERVAL_CONFIG.name(), 2L);
        configValues.put(CouchbaseCapellaInput.QUERY_LENGTH_CONFIG.name(), 20 * 60L);
        configValues.put(CouchbaseCapellaInput.API_BASE_URL_CONFIG.name(), baseUrl);
        configValues.put(CouchbaseCapellaInput.ORG_ID_CONFIG.name(), "");
        configValues.put(CouchbaseCapellaInput.PROJECT_ID_CONFIG.name(), "success-project");
        configValues.put(CouchbaseCapellaInput.CLUSTER_ID_CONFIG.name(), "success-cluster");
        configValues.put(CouchbaseCapellaInput.AUTH_TOKEN_CONFIG.name(), "Bearer good_token");

        Configuration config = new ConfigurationImpl(configValues);
        CouchbaseCapellaInput input = new CouchbaseCapellaInput("test-id", config, null);

    }

    @Test(expected = IllegalArgumentException.class)
    public void testCouchbaseCapellaInput_EmptyProjectID() {


        var baseUrl = String.format("http://%s:%d/%s", mockServerHost, mockServerPort, mockServerApiBasePath);
        Map<String, Object> configValues = new HashMap<>();
        configValues.put(CouchbaseCapellaInput.INTERVAL_CONFIG.name(), 2L);
        configValues.put(CouchbaseCapellaInput.QUERY_LENGTH_CONFIG.name(), 20 * 60L);
        configValues.put(CouchbaseCapellaInput.API_BASE_URL_CONFIG.name(), baseUrl);
        configValues.put(CouchbaseCapellaInput.ORG_ID_CONFIG.name(), "success-org");
        configValues.put(CouchbaseCapellaInput.PROJECT_ID_CONFIG.name(), "");
        configValues.put(CouchbaseCapellaInput.CLUSTER_ID_CONFIG.name(), "success-cluster");
        configValues.put(CouchbaseCapellaInput.AUTH_TOKEN_CONFIG.name(), "Bearer good_token");

        Configuration config = new ConfigurationImpl(configValues);
        CouchbaseCapellaInput input = new CouchbaseCapellaInput("test-id", config, null);

    }

    @Test(expected = IllegalArgumentException.class)
    public void testCouchbaseCapellaInput_EmptyClusterID() {


        var baseUrl = String.format("http://%s:%d/%s", mockServerHost, mockServerPort, mockServerApiBasePath);
        Map<String, Object> configValues = new HashMap<>();
        configValues.put(CouchbaseCapellaInput.INTERVAL_CONFIG.name(), 2L);
        configValues.put(CouchbaseCapellaInput.QUERY_LENGTH_CONFIG.name(), 20 * 60L);
        configValues.put(CouchbaseCapellaInput.API_BASE_URL_CONFIG.name(), baseUrl);
        configValues.put(CouchbaseCapellaInput.ORG_ID_CONFIG.name(), "success-org");
        configValues.put(CouchbaseCapellaInput.PROJECT_ID_CONFIG.name(), "success-project");
        configValues.put(CouchbaseCapellaInput.CLUSTER_ID_CONFIG.name(), "");
        configValues.put(CouchbaseCapellaInput.AUTH_TOKEN_CONFIG.name(), "Bearer good_token");

        Configuration config = new ConfigurationImpl(configValues);
        CouchbaseCapellaInput input = new CouchbaseCapellaInput("test-id", config, null);

    }

    @Test(expected = IllegalArgumentException.class)
    public void testCouchbaseCapellaInput_EmptyAuthToken() {


        var baseUrl = String.format("http://%s:%d/%s", mockServerHost, mockServerPort, mockServerApiBasePath);
        Map<String, Object> configValues = new HashMap<>();
        configValues.put(CouchbaseCapellaInput.INTERVAL_CONFIG.name(), 2L);
        configValues.put(CouchbaseCapellaInput.QUERY_LENGTH_CONFIG.name(), 20 * 60L);
        configValues.put(CouchbaseCapellaInput.API_BASE_URL_CONFIG.name(), baseUrl);
        configValues.put(CouchbaseCapellaInput.ORG_ID_CONFIG.name(), "success-org");
        configValues.put(CouchbaseCapellaInput.PROJECT_ID_CONFIG.name(), "success-project");
        configValues.put(CouchbaseCapellaInput.CLUSTER_ID_CONFIG.name(), "success-cluster");
        configValues.put(CouchbaseCapellaInput.AUTH_TOKEN_CONFIG.name(), "");

        Configuration config = new ConfigurationImpl(configValues);
        CouchbaseCapellaInput input = new CouchbaseCapellaInput("test-id", config, null);

    }

    private static class TestConsumer implements Consumer<Map<String, Object>> {

        private List<Map<String, Object>> events = new ArrayList<>();

        @Override
        public void accept(Map<String, Object> event) {
            synchronized (this) {
                events.add(event);
            }
        }

        public List<Map<String, Object>> getEvents() {
            return events;
        }
    }
}
