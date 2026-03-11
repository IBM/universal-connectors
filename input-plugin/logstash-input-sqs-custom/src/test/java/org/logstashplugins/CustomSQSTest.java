package org.logstashplugins;

import co.elastic.logstash.api.Configuration;
import org.junit.Assert;
import org.junit.Test;
import org.logstash.plugins.ConfigurationImpl;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

/**
 * Unit tests for SQSInput plugin
 */
public class CustomSQSTest {

    @Test
    public void testSQSInputCreation_WithValidConfig() {
        Map<String, Object> configValues = new HashMap<>();
        configValues.put(CustomSQS.QUEUE_NAME.name(), "test-queue");
        configValues.put(CustomSQS.REGION.name(), "us-east-1");
        configValues.put(CustomSQS.ACCESS_KEY.name(), "test-access-key");
        configValues.put(CustomSQS.SECRET_KEY.name(), "test-secret-key");
        configValues.put(CustomSQS.POLLING_FREQUENCY.name(), 20L);
        configValues.put(CustomSQS.MAX_MESSAGES.name(), 10L);

        Configuration config = new ConfigurationImpl(configValues);
        CustomSQS input = new CustomSQS("test-id", config, null);

        Assert.assertNotNull("SQS Input should be created", input);
        Assert.assertEquals("Plugin ID should match", "test-id", input.getId());
    }

    @Test
    public void testSQSInputCreation_WithRoleArn() {
        Map<String, Object> configValues = new HashMap<>();
        configValues.put(CustomSQS.QUEUE_NAME.name(), "test-queue");
        configValues.put(CustomSQS.REGION.name(), "us-west-2");
        configValues.put(CustomSQS.ACCESS_KEY.name(), "test-access-key");
        configValues.put(CustomSQS.SECRET_KEY.name(), "test-secret-key");
        configValues.put(CustomSQS.ROLE_ARN.name(), "arn:aws:iam::123456789012:role/test-role");
        configValues.put(CustomSQS.POLLING_FREQUENCY.name(), 30L);
        configValues.put(CustomSQS.MAX_MESSAGES.name(), 5L);
        configValues.put(CustomSQS.TYPE.name(), "sqs-input");

        Configuration config = new ConfigurationImpl(configValues);
        CustomSQS input = new CustomSQS("test-id-2", config, null);

        Assert.assertNotNull("SQS Input with role ARN should be created", input);
        Assert.assertEquals("Plugin ID should match", "test-id-2", input.getId());
    }

    @Test
    public void testSQSInputCreation_WithDefaultValues() {
        Map<String, Object> configValues = new HashMap<>();
        configValues.put(CustomSQS.QUEUE_NAME.name(), "default-queue");
        configValues.put(CustomSQS.ACCESS_KEY.name(), "test-key");
        configValues.put(CustomSQS.SECRET_KEY.name(), "test-secret");

        Configuration config = new ConfigurationImpl(configValues);
        CustomSQS input = new CustomSQS("test-id-3", config, null);

        Assert.assertNotNull("SQS Input with defaults should be created", input);
        Assert.assertEquals("Plugin ID should match", "test-id-3", input.getId());
    }

    @Test
    public void testConfigSchema_NotNull() {
        Map<String, Object> configValues = new HashMap<>();
        configValues.put(CustomSQS.QUEUE_NAME.name(), "test-queue");
        configValues.put(CustomSQS.ACCESS_KEY.name(), "test-key");
        configValues.put(CustomSQS.SECRET_KEY.name(), "test-secret");

        Configuration config = new ConfigurationImpl(configValues);
        CustomSQS input = new CustomSQS("test-id", config, null);

        Assert.assertNotNull("Config schema should not be null", input.configSchema());
        Assert.assertTrue("Config schema should have multiple fields", input.configSchema().size() > 0);
    }

    @Test
    public void testStop_DoesNotThrowException() {
        Map<String, Object> configValues = new HashMap<>();
        configValues.put(CustomSQS.QUEUE_NAME.name(), "test-queue");
        configValues.put(CustomSQS.ACCESS_KEY.name(), "test-key");
        configValues.put(CustomSQS.SECRET_KEY.name(), "test-secret");

        Configuration config = new ConfigurationImpl(configValues);
        CustomSQS input = new CustomSQS("test-id", config, null);

        // Stop should not throw exception
        input.stop();
        
        Assert.assertTrue("Stop completed successfully", true);
    }

    @Test
    public void testSQSInputCreation_WithInvalidCredentials() {
        // Test that plugin can be created even with invalid credentials
        // The actual connection failure will happen when start() is called
        Map<String, Object> configValues = new HashMap<>();
        configValues.put(CustomSQS.QUEUE_NAME.name(), "test-queue");
        configValues.put(CustomSQS.REGION.name(), "us-east-1");
        configValues.put(CustomSQS.ACCESS_KEY.name(), "INVALID_ACCESS_KEY");
        configValues.put(CustomSQS.SECRET_KEY.name(), "INVALID_SECRET_KEY");
        configValues.put(CustomSQS.POLLING_FREQUENCY.name(), 20L);
        configValues.put(CustomSQS.MAX_MESSAGES.name(), 10L);

        Configuration config = new ConfigurationImpl(configValues);
        CustomSQS input = new CustomSQS("test-invalid-creds", config, null);

        // Plugin should be created successfully
        // Credential validation happens during start(), not during construction
        Assert.assertNotNull("SQS Input should be created even with invalid credentials", input);
        Assert.assertEquals("Plugin ID should match", "test-invalid-creds", input.getId());
        
        // Note: We don't call start() here because it would attempt to connect to AWS
        // The plugin's error handling will catch credential errors during start()
        // and enter sleep mode without crashing the pipeline
    }
    
    @Test
    public void testSQSInputCreation_WithCustomEndpoint() {
        Map<String, Object> configValues = new HashMap<>();
        configValues.put(CustomSQS.QUEUE_NAME.name(), "test-queue");
        configValues.put(CustomSQS.REGION.name(), "us-east-1");
        configValues.put(CustomSQS.ACCESS_KEY.name(), "test-key");
        configValues.put(CustomSQS.SECRET_KEY.name(), "test-secret");
        configValues.put(CustomSQS.ENDPOINT.name(), "https://vpce-xxxxx.sqs.us-east-1.vpce.amazonaws.com");

        Configuration config = new ConfigurationImpl(configValues);
        CustomSQS input = new CustomSQS("test-endpoint", config, null);

        Assert.assertNotNull("SQS Input with custom endpoint should be created", input);
        Assert.assertEquals("Plugin ID should match", "test-endpoint", input.getId());
    }

    @Test
    public void testSQSInputCreation_WithAwsBundledCA() {
        Map<String, Object> configValues = new HashMap<>();
        configValues.put(CustomSQS.QUEUE_NAME.name(), "test-queue");
        configValues.put(CustomSQS.REGION.name(), "us-east-1");
        configValues.put(CustomSQS.ACCESS_KEY.name(), "test-key");
        configValues.put(CustomSQS.SECRET_KEY.name(), "test-secret");
        configValues.put(CustomSQS.USE_AWS_BUNDLED_CA.name(), true);

        Configuration config = new ConfigurationImpl(configValues);
        CustomSQS input = new CustomSQS("test-bundled-ca", config, null);

        Assert.assertNotNull("SQS Input with AWS bundled CA should be created", input);
        Assert.assertEquals("Plugin ID should match", "test-bundled-ca", input.getId());
    }

    @Test
    public void testSQSInputCreation_WithCustomSslCertificate() throws IOException {
        // Create a temporary test certificate file
        File tempCert = createTestCertificateFile();
        
        try {
            Map<String, Object> configValues = new HashMap<>();
            configValues.put(CustomSQS.QUEUE_NAME.name(), "test-queue");
            configValues.put(CustomSQS.REGION.name(), "us-east-1");
            configValues.put(CustomSQS.ACCESS_KEY.name(), "test-key");
            configValues.put(CustomSQS.SECRET_KEY.name(), "test-secret");
            
            // Configure additional_settings with ssl_ca_bundle
            Map<String, Object> additionalSettings = new HashMap<>();
            additionalSettings.put("ssl_ca_bundle", tempCert.getAbsolutePath());
            configValues.put(CustomSQS.ADDITIONAL_SETTINGS.name(), additionalSettings);

            Configuration config = new ConfigurationImpl(configValues);
            CustomSQS input = new CustomSQS("test-ssl-cert", config, null);

            Assert.assertNotNull("SQS Input with custom SSL certificate should be created", input);
            Assert.assertEquals("Plugin ID should match", "test-ssl-cert", input.getId());
            
            // Note: The actual SSL configuration happens during start() when the SQS client is built
            // This test verifies that the configuration is accepted without errors
        } finally {
            // Clean up
            if (tempCert != null && tempCert.exists()) {
                tempCert.delete();
            }
        }
    }

    @Test
    public void testSQSInputCreation_WithNonExistentSslCertificate() {
        // Test that plugin can be created even with non-existent certificate path
        // The actual validation happens during start()
        Map<String, Object> configValues = new HashMap<>();
        configValues.put(CustomSQS.QUEUE_NAME.name(), "test-queue");
        configValues.put(CustomSQS.REGION.name(), "us-east-1");
        configValues.put(CustomSQS.ACCESS_KEY.name(), "test-key");
        configValues.put(CustomSQS.SECRET_KEY.name(), "test-secret");
        
        Map<String, Object> additionalSettings = new HashMap<>();
        additionalSettings.put("ssl_ca_bundle", "/non/existent/path/ca-bundle.pem");
        configValues.put(CustomSQS.ADDITIONAL_SETTINGS.name(), additionalSettings);

        Configuration config = new ConfigurationImpl(configValues);
        CustomSQS input = new CustomSQS("test-missing-cert", config, null);

        Assert.assertNotNull("SQS Input should be created even with non-existent cert path", input);
        Assert.assertEquals("Plugin ID should match", "test-missing-cert", input.getId());
    }

    @Test
    public void testSQSInputCreation_WithAllSslOptions() throws IOException {
        File tempCert = createTestCertificateFile();
        
        try {
            Map<String, Object> configValues = new HashMap<>();
            configValues.put(CustomSQS.QUEUE_NAME.name(), "test-queue");
            configValues.put(CustomSQS.REGION.name(), "us-east-1");
            configValues.put(CustomSQS.ACCESS_KEY.name(), "test-key");
            configValues.put(CustomSQS.SECRET_KEY.name(), "test-secret");
            configValues.put(CustomSQS.ENDPOINT.name(), "https://vpce-xxxxx.sqs.us-east-1.vpce.amazonaws.com");
            configValues.put(CustomSQS.USE_AWS_BUNDLED_CA.name(), false);
            
            Map<String, Object> additionalSettings = new HashMap<>();
            additionalSettings.put("ssl_ca_bundle", tempCert.getAbsolutePath());
            configValues.put(CustomSQS.ADDITIONAL_SETTINGS.name(), additionalSettings);

            Configuration config = new ConfigurationImpl(configValues);
            CustomSQS input = new CustomSQS("test-all-ssl", config, null);

            Assert.assertNotNull("SQS Input with all SSL options should be created", input);
            Assert.assertEquals("Plugin ID should match", "test-all-ssl", input.getId());
        } finally {
            if (tempCert != null && tempCert.exists()) {
                tempCert.delete();
            }
        }
    }

    /**
     * Helper method to create a DUMMY test certificate file
     *
     * @return File containing a dummy PEM certificate for testing
     * @throws IOException if file creation fails
     */
    private File createTestCertificateFile() throws IOException {
        File tempFile = File.createTempFile("test-ca-", ".pem");
        
        // DUMMY/FAKE certificate - Only for testing configuration parsing
        String dummyCert = "-----BEGIN CERTIFICATE-----\n" +
                          "TESTTESTTESTTESTTESTESTTESTTESTTESTTESTTESTESTTESTTESTTESTTESTTESTEST\n" +
                          "TESTTESTTESTTESTTESTESTTESTTESTTESTTESTTESTESTTESTTESTTESTTESTTESTEST\n" +
                          "TESTTESTTESTTESTTESTESTTESTTESTTESTTESTTESTESTTESTTESTTESTTESTTESTEST\n" +
                          "TESTTESTTESTTESTTESTESTTESTTESTTESTTESTTESTESTTESTTESTTESTTESTTESTEST\n" +
                          "TESTTESTTESTTESTTESTESTTESTTESTTESTTESTTESTESTTESTTESTTESTTESTTESTEST\n" +
                          "TESTTESTTESTTESTTESTESTTESTTESTTESTTESTTESTESTTESTTESTTESTTESTTESTEST\n" +
                          "TESTTESTTESTTESTTESTESTTESTTESTTESTTESTTESTESTTESTTESTTESTTESTTESTEST\n" +
                          "TESTTESTTESTTESTTESTESTTESTTESTTESTTESTTESTESTTESTTESTTESTTESTTESTEST\n" +
                          "TESTTESTTESTTESTTESTESTTESTTESTTESTTESTTESTESTTESTTESTTESTTESTTESTEST\n" +
                          "TESTTESTTESTTESTTESTESTTESTTESTTESTTESTTESTESTTESTTESTTESTTESTTESTEST\n" +
                          "-----END CERTIFICATE-----\n";
        
        try (FileWriter writer = new FileWriter(tempFile)) {
            writer.write(dummyCert);
        }
        
        return tempFile;
    }
}
