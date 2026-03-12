package org.logstashplugins;

import co.elastic.logstash.api.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.*;
import software.amazon.awssdk.services.sts.StsClient;
import software.amazon.awssdk.services.sts.auth.StsAssumeRoleCredentialsProvider;
import software.amazon.awssdk.services.sts.model.AssumeRoleRequest;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;

/**
 * Custom SQS Input Plugin
 *
 * This plugin reads messages from AWS SQS with graceful error handling.
 *
 * @author IBM Guardium Universal Connectors Team
 * @version 1.0.0
 */
@LogstashPlugin(name = "custom_sqs")
public class CustomSQS implements Input {
    
    private static final Logger logger = LogManager.getLogger(CustomSQS.class);
    
    // Plugin configuration parameters
    protected static final PluginConfigSpec<String> QUEUE_NAME = PluginConfigSpec.requiredStringSetting("queue");
    protected static final PluginConfigSpec<String> REGION = PluginConfigSpec.stringSetting("region", "us-east-1");
    protected static final PluginConfigSpec<String> ACCESS_KEY = PluginConfigSpec.stringSetting("access_key_id");
    protected static final PluginConfigSpec<String> SECRET_KEY = PluginConfigSpec.stringSetting("secret_access_key");
    protected static final PluginConfigSpec<String> ROLE_ARN = PluginConfigSpec.stringSetting("role_arn");
    protected static final PluginConfigSpec<String> ENDPOINT = PluginConfigSpec.stringSetting("endpoint");
    protected static final PluginConfigSpec<Long> POLLING_FREQUENCY = PluginConfigSpec.numSetting("polling_frequency", 20);
    protected static final PluginConfigSpec<Long> MAX_MESSAGES = PluginConfigSpec.numSetting("max_messages", 10);
    protected static final PluginConfigSpec<String> TYPE = PluginConfigSpec.stringSetting("type");
    protected static final PluginConfigSpec<Map<String, Object>> ADD_FIELD = PluginConfigSpec.hashSetting("add_field", Collections.emptyMap(), false, false);
    protected static final PluginConfigSpec<String> CODEC = PluginConfigSpec.stringSetting("codec", "plain");
    protected static final PluginConfigSpec<Boolean> USE_AWS_BUNDLED_CA = PluginConfigSpec.booleanSetting("use_aws_bundled_ca", true);
    protected static final PluginConfigSpec<Map<String, Object>> ADDITIONAL_SETTINGS = PluginConfigSpec.hashSetting("additional_settings", Collections.emptyMap(), false, false);
    
    private final String id;
    private final String queueName;
    private final String region;
    private final String accessKeyId;
    private final String secretAccessKey;
    private final String roleArn;
    private final String endpoint;
    private final long pollingFrequency;
    private final int maxMessages;
    private final String type;
    private final Map<String, Object> addField;
    private final boolean useAwsBundledCa;
    private final Map<String, Object> additionalSettings;
    
    private SqsClient sqsClient;
    private String queueUrl;
    private volatile boolean stopped = false;
    private final CountDownLatch done = new CountDownLatch(1);
    private boolean connectionValid = false;
    private Consumer<Map<String, Object>> consumer;
    
    /**
     * Constructor called by Logstash
     */
    public CustomSQS(String id, Configuration config, Context context) {
        this.id = id;
        this.queueName = config.get(QUEUE_NAME);
        this.region = config.get(REGION);
        this.accessKeyId = config.get(ACCESS_KEY);
        this.secretAccessKey = config.get(SECRET_KEY);
        this.roleArn = config.get(ROLE_ARN);
        this.endpoint = config.get(ENDPOINT);
        this.pollingFrequency = config.get(POLLING_FREQUENCY);
        this.maxMessages = Math.toIntExact(config.get(MAX_MESSAGES));
        this.type = config.get(TYPE);
        this.addField = config.get(ADD_FIELD);
        this.useAwsBundledCa = config.get(USE_AWS_BUNDLED_CA);
        this.additionalSettings = config.get(ADDITIONAL_SETTINGS);
        
        logger.info("Custom SQS Guardium input plugin initialized",
                   "queue", queueName,
                   "region", region);
    }
    
    @Override
    public void start(Consumer<Map<String, Object>> consumer) {
        this.consumer = consumer;
        
        // Try to connect with retry for transient errors
        if (connectWithRetry()) {
            // Successfully connected, start polling messages
            pollMessages();
        } else {
            // Failed after retries (credential error or max retries exceeded)
            sleepIndefinitely();
        }
    }
    
    /**
     * Attempt to connect to SQS with retry logic for transient errors
     * Credential errors are not retried
     *
     * @return true if connection successful, false otherwise
     */
    private boolean connectWithRetry() {
        int maxRetries = 5;
        long sleepMs = 1000; // Start with 1 second
        long maxSleepMs = 60000; // Max 60 seconds
        
        for (int attempt = 1; attempt <= maxRetries && !stopped; attempt++) {
            try {
                logger.info("Attempting to connect to SQS queue: {} (attempt {}/{})",
                           queueName, attempt, maxRetries);
                
                initializeSqsClient();
                connectionValid = true;
                logger.info("Successfully connected to SQS queue: {}", queueName);
                return true;
                
            } catch (SqsException e) {
                connectionValid = false;
                String errorCode = e.awsErrorDetails() != null ? e.awsErrorDetails().errorCode() : "UNKNOWN";
                String errorMessage = e.awsErrorDetails() != null ? e.awsErrorDetails().errorMessage() : e.getMessage();
                
                // Extract only the first sentence/line of the error message
                if (errorMessage != null && errorMessage.contains("\n")) {
                    errorMessage = errorMessage.substring(0, errorMessage.indexOf("\n")).trim();
                }
                
                // Check if it's a credential error (permanent - don't retry)
                if (isCredentialError(errorCode)) {
                    logger.error("Credential error detected - Queue: " + queueName +
                                ", Region: " + region +
                                ", Error Code: " + errorCode +
                                ", Message: " + errorMessage +
                                " - Not retrying, requires configuration fix");
                    return false;
                }
                
                // Network or transient error - retry with backoff
                logger.warn("Connection attempt {} failed - Queue: {}, Error Code: {}, Message: {} - Retrying in {}ms",
                           attempt, queueName, errorCode, errorMessage, sleepMs);
                
                if (attempt < maxRetries) {
                    sleep(sleepMs);
                    sleepMs = Math.min(sleepMs * 2, maxSleepMs); // Exponential backoff
                }
                
            } catch (Exception e) {
                // Unknown error - retry with backoff
                connectionValid = false;
                String errorMessage = e.getMessage();
                
                if (errorMessage != null && errorMessage.contains("\n")) {
                    errorMessage = errorMessage.substring(0, errorMessage.indexOf("\n")).trim();
                }
                
                logger.warn("Unexpected error on attempt {} - Queue: {}, Error: {}, Type: {} - Retrying in {}ms",
                           attempt, queueName, errorMessage, e.getClass().getName(), sleepMs);
                
                if (attempt < maxRetries) {
                    sleep(sleepMs);
                    sleepMs = Math.min(sleepMs * 2, maxSleepMs); // Exponential backoff
                }
            }
        }
        
        // Failed after all retries
        logger.error("Failed to connect to SQS after {} attempts - Queue: {}, Region: {}",
                    maxRetries, queueName, region);
        return false;
    }
    
    /**
     * Check if the error code indicates a credential/authentication error
     * These errors are permanent and should not be retried
     *
     * @param errorCode AWS error code
     * @return true if credential error, false otherwise
     */
    private boolean isCredentialError(String errorCode) {
        return "SignatureDoesNotMatch".equals(errorCode) ||
               "InvalidClientTokenId".equals(errorCode) ||
               "InvalidAccessKeyId".equals(errorCode) ||
               "AccessDenied".equals(errorCode) ||
               "UnrecognizedClientException".equals(errorCode) ||
               "InvalidSignatureException".equals(errorCode);
    }
    
    /**
     * Initialize SQS client with credentials
     * This is where credential validation happens
     */
    private void initializeSqsClient() {
        logger.info("Initializing SQS client for queue: {}", queueName);
        
        AwsCredentialsProvider credentialsProvider;
        
        // Setup credentials provider
        if (roleArn != null && !roleArn.isEmpty() && !roleArn.equals("guc_input_param_role-arn")) {
            // Use role ARN for cross-account access
            logger.info("Using role ARN for authentication: {}", roleArn);
            
            StsClient stsClient = StsClient.builder()
                    .region(Region.of(region))
                    .credentialsProvider(StaticCredentialsProvider.create(
                            AwsBasicCredentials.create(accessKeyId, secretAccessKey)))
                    .build();
            
            credentialsProvider = StsAssumeRoleCredentialsProvider.builder()
                    .refreshRequest(AssumeRoleRequest.builder()
                            .roleArn(roleArn)
                            .roleSessionName("sqs-guardium-" + System.currentTimeMillis())
                            .build())
                    .stsClient(stsClient)
                    .build();
                    
        } else if (accessKeyId != null && !accessKeyId.isEmpty()) {
            // Use static credentials
            logger.info("Using static credentials for authentication");
            credentialsProvider = StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(accessKeyId, secretAccessKey));
        } else {
            // Use default credentials chain
            logger.info("Using default credentials provider");
            credentialsProvider = DefaultCredentialsProvider.create();
        }
        
        // Create SQS client builder
        software.amazon.awssdk.services.sqs.SqsClientBuilder clientBuilder = SqsClient.builder()
                .region(Region.of(region))
                .credentialsProvider(credentialsProvider);
        
        // Apply custom endpoint if provided (for VPC endpoints or proxies)
        if (endpoint != null && !endpoint.isEmpty()) {
            try {
                clientBuilder.endpointOverride(java.net.URI.create(endpoint));
                logger.info("Using custom SQS endpoint: {}", endpoint);
            } catch (Exception e) {
                logger.error("Invalid endpoint URL: {}", endpoint, e);
            }
        }
        
        // Apply additional settings if provided
        if (additionalSettings != null && !additionalSettings.isEmpty()) {
            applyAdditionalSettings(clientBuilder);
        }
        
        sqsClient = clientBuilder.build();
        
        // Get queue URL - this validates credentials
        GetQueueUrlRequest getQueueUrlRequest = GetQueueUrlRequest.builder()
                .queueName(queueName)
                .build();
        
        GetQueueUrlResponse response = sqsClient.getQueueUrl(getQueueUrlRequest);
        queueUrl = response.queueUrl();
        
        logger.info("Successfully retrieved queue URL: {}", queueUrl);
    }
    
    /**
     * Poll messages from SQS queue
     */
    private void pollMessages() {
        logger.info("Starting SQS message polling", "queue", queueName, "interval", pollingFrequency);
        
        while (!stopped) {
            try {
                // Check if client is still valid before using it
                if (sqsClient == null || stopped) {
                    break;
                }
                
                // Receive messages from queue
                ReceiveMessageRequest receiveRequest = ReceiveMessageRequest.builder()
                        .queueUrl(queueUrl)
                        .maxNumberOfMessages(maxMessages)
                        .waitTimeSeconds((int) pollingFrequency)
                        .build();
                
                ReceiveMessageResponse receiveResponse = sqsClient.receiveMessage(receiveRequest);
                List<Message> messages = receiveResponse.messages();
                
                // Process each message
                for (Message message : messages) {
                    if (stopped) break;  // Check stopped flag during message processing
                    processMessage(message);
                }
                
            } catch (SqsException e) {
                if (stopped) break;  // Exit if stopping
                
                logger.error("Error receiving SQS messages - Queue: " + queueName +
                            ", Error: " + e.getMessage() +
                            ", Code: " + (e.awsErrorDetails() != null ? e.awsErrorDetails().errorCode() : "UNKNOWN"));
                
                // Sleep before retry
                sleep(pollingFrequency * 1000);
                
            } catch (IllegalStateException e) {
                // Connection pool shut down - plugin is stopping
                if (stopped) {
                    logger.info("SQS client closed during shutdown - Queue: " + queueName);
                    break;
                }
                logger.error("Unexpected IllegalStateException in SQS polling - Queue: " + queueName +
                            ", Error: " + e.getMessage());
                sleep(pollingFrequency * 1000);
                
            } catch (Exception e) {
                if (stopped) break;  // Exit if stopping
                
                logger.error("Unexpected error in SQS polling - Queue: " + queueName +
                            ", Error: " + e.getMessage() +
                            ", Type: " + e.getClass().getName());
                
                // Sleep before retry
                sleep(pollingFrequency * 1000);
            }
        }
        
        done.countDown();
    }
    
    /**
     * Process a single SQS message
     */
    private void processMessage(Message message) {
        try {
            // Create event map
            Map<String, Object> event = new HashMap<>();
            event.put("message", message.body());
            event.put("sqs_message_id", message.messageId());
            event.put("sqs_receipt_handle", message.receiptHandle());
            
            // Add type if specified
            if (type != null && !type.isEmpty()) {
                event.put("type", type);
            }
            
            // Send to Logstash pipeline
            consumer.accept(event);
            
            // Delete message from queue after successful processing
            DeleteMessageRequest deleteRequest = DeleteMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .receiptHandle(message.receiptHandle())
                    .build();
            
            sqsClient.deleteMessage(deleteRequest);
            
            logger.debug("Successfully processed and deleted message: {}", message.messageId());
            
        } catch (Exception e) {
            logger.error("Error processing SQS message",
                        "messageId", message.messageId(),
                        "error", e.getMessage());
        }
    }
    
    /**
     * Sleep indefinitely when connection is invalid
     * This keeps the plugin alive without processing messages
     */
    private void sleepIndefinitely() {
        logger.error("SQS input not running due to connection failure. Fix credentials and reload pipeline.");
        
        while (!stopped) {
            sleep(pollingFrequency * 1000);
            logger.error("SQS input sleeping due to connection failure",
                        "queue", queueName,
                        "interval", pollingFrequency);
        }
        
        done.countDown();
    }
    
    /**
     * Apply additional settings to the SQS client builder
     *
     * This method processes all settings in the additional_settings map and applies
     * them to the client builder. Each setting is handled individually with proper
     * error handling to ensure one failing setting doesn't prevent others from being applied.
     *
     * Supported settings:
     * - ssl_ca_bundle: Path to custom SSL certificate file
     * - (Add more settings as needed)
     */
    private void applyAdditionalSettings(software.amazon.awssdk.services.sqs.SqsClientBuilder clientBuilder) {
        logger.info("Applying {} additional setting(s) to SQS client", additionalSettings.size());
        
        additionalSettings.forEach((key, value) -> {
            if (value == null) {
                logger.warn("Skipping additional setting '{}' with null value", key);
                return;
            }
            
            try {
                logger.debug("Processing additional setting: {} = {}", key, value);
                
                // Handle each setting type
                if ("ssl_ca_bundle".equals(key)) {
                    configureSslCertificate(clientBuilder, value.toString());
                } else {
                    // Warn about unrecognized settings to help catch typos
                    logger.warn("Unrecognized additional setting '{}' = {} (will be ignored). Valid settings: ssl_ca_bundle", key, value);
                }
                
            } catch (Exception e) {
                logger.error("Failed to apply additional setting '{}': {}", key, e.getMessage(), e);
                // Continue processing other settings even if one fails
            }
        });
    }
    
    /**
     * Configure custom SSL certificate for the SQS client
     *
     * @param clientBuilder The SQS client builder
     * @param certPath Path to the SSL certificate file
     */
    private void configureSslCertificate(software.amazon.awssdk.services.sqs.SqsClientBuilder clientBuilder, String certPath) {
        logger.info("Configuring custom SSL certificate from: {}", certPath);
        
        java.io.File certFile = new java.io.File(certPath);
        if (!certFile.exists()) {
            logger.error("Custom SSL certificate file not found at path: {}. Please verify the path exists and is accessible.", certPath);
            return;
        }
        
        try {
            // Set the custom CA bundle as a system property
            // This will be used by the AWS SDK for SSL/TLS connections
            System.setProperty("aws.caBundlePath", certPath);
            logger.info("Successfully configured custom SSL certificate path: {}", certPath);
        } catch (Exception e) {
            logger.error("Failed to configure SSL certificate from '{}': {}", certPath, e.getMessage(), e);
            throw new RuntimeException("SSL certificate configuration failed", e);
        }
    }
    
    /**
     * Sleep for specified milliseconds
     */
    private void sleep(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    @Override
    public void stop() {
        stopped = true;
        logger.info("Stopping SQS input plugin", "queue", queueName);
        
        // Close SQS client safely
        SqsClient clientToClose = sqsClient;
        sqsClient = null;  // Set to null first to prevent further use
        
        if (clientToClose != null) {
            try {
                clientToClose.close();
                logger.info("SQS client closed successfully", "queue", queueName);
            } catch (Exception e) {
                logger.warn("Error closing SQS client (this is normal during shutdown)", "error", e.getMessage());
            }
        }
    }
    
    @Override
    public void awaitStop() throws InterruptedException {
        done.await();
    }
    
    @Override
    public Collection<PluginConfigSpec<?>> configSchema() {
        return Arrays.asList(
                QUEUE_NAME,
                REGION,
                ACCESS_KEY,
                SECRET_KEY,
                ROLE_ARN,
                ENDPOINT,
                POLLING_FREQUENCY,
                MAX_MESSAGES,
                TYPE,
                ADD_FIELD,
                CODEC,
                USE_AWS_BUNDLED_CA,
                ADDITIONAL_SETTINGS
        );
    }
    
    @Override
    public String getId() {
        return id;
    }
}
