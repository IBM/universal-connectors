package org.logstashplugins;

import co.elastic.logstash.api.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.json.JSONArray;
import org.json.JSONObject;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;
import software.amazon.awssdk.services.sts.StsClient;
import software.amazon.awssdk.services.sts.auth.StsAssumeRoleCredentialsProvider;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.zip.GZIPInputStream;

@LogstashPlugin(name = "s3_sqs")
public class S3SQS implements Input, AutoCloseable {
    private final String queueUrl;
    private final int maxMessages;
    private final int waitTime;
    private final Context context;
    private volatile boolean stopped = false;
    private final CountDownLatch done = new CountDownLatch(1);
    private ExecutorService executorService;
    private Consumer<Map<String, Object>> consumer;
    private final S3Client s3Client;
    private final SqsClient sqsClient;
    private final String type;
    private final Long pollingFrequency;
    private static final String LOG_EVENTS = "logEvents";
    private static final String RECORDS = "Records";
    private Map<String, Object> addField = new HashMap<>();

    private static final PluginConfigSpec<String> QUEUE_URL = PluginConfigSpec.stringSetting("queue_url");
    private static final PluginConfigSpec<String> REGION = PluginConfigSpec.stringSetting("region");
    private static final PluginConfigSpec<String> ACCESS_KEY = PluginConfigSpec.stringSetting("access_key_id");
    private static final PluginConfigSpec<String> SECRET_KEY = PluginConfigSpec.stringSetting("secret_access_key");
    private static final PluginConfigSpec<String> ARN = PluginConfigSpec.stringSetting("role_arn");
    private static final PluginConfigSpec<Long> MAX_MESSAGES = PluginConfigSpec.numSetting("max_messages", 10);
    private static final PluginConfigSpec<Long> WAIT_TIME = PluginConfigSpec.numSetting("wait_time", 20);
    private static final PluginConfigSpec<String> TYPE = PluginConfigSpec.stringSetting("type");
    private static final PluginConfigSpec<Long> SQS_POLLING_FREQUENCY = PluginConfigSpec.numSetting("polling_frequency", 10);
    private static final PluginConfigSpec<Map<String, Object>> ADD_FIELD = PluginConfigSpec.hashSetting("add_field", Collections.emptyMap(), false, false);

    private static final ObjectMapper mapper = new ObjectMapper();



    public S3SQS(String id, Configuration config, Context context) {
        this.context = context;
        this.queueUrl = config.get(QUEUE_URL);
        this.maxMessages = Math.toIntExact(config.get(MAX_MESSAGES));
        this.waitTime = Math.toIntExact(config.get(WAIT_TIME));
        String region = config.get(REGION);
        String accessKeyId = config.get(ACCESS_KEY);
        String secretAccessKey = config.get(SECRET_KEY);
        String arn = config.get(ARN);
        this.type = config.get(TYPE);
        this.addField = config.get(ADD_FIELD);


        // convert seconds to milliseconds
        this.pollingFrequency = (1000 * config.get(SQS_POLLING_FREQUENCY));

        AwsCredentialsProvider credentialsProvider;
        if (arn != null && !arn.isEmpty()) {
            credentialsProvider = StsAssumeRoleCredentialsProvider.builder()
                    .refreshRequest(b -> b
                            .roleArn(arn)
                            .roleSessionName((type != null && !type.isEmpty())? type : "S3SQS-session"))
                    .stsClient(software.amazon.awssdk.services.sts.StsClient.builder()
                            .region(Region.of(region))
                            .credentialsProvider(DefaultCredentialsProvider.create())
                            .build())
                    .build();

        } else {
            credentialsProvider = StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKeyId, secretAccessKey));
        }

        this.s3Client = S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(credentialsProvider)
                .overrideConfiguration(ClientOverrideConfiguration.builder()
                        .apiCallTimeout(Duration.ofMinutes(2))
                        .build())
                .build();

        this.sqsClient = SqsClient.builder()
                .region(Region.of(region))
                .credentialsProvider(credentialsProvider)
                .build();
    }

    @Override
    public void start(Consumer<Map<String, Object>> consumer) {
        this.consumer = consumer;
        this.executorService = Executors.newFixedThreadPool(1);
        while (!stopped) {
            executorService.submit(this::processMessages);
            try {
                Thread.sleep(this.pollingFrequency);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // Restore the interrupted status
                context.getLogger(this).error("Thread interrupted: {}", e.getMessage());
            }
        }
    }


    private void processMessages() {
        try {
            context.getLogger(this).debug("Polling messages from SQS...");
            ReceiveMessageRequest receiveRequest = ReceiveMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .maxNumberOfMessages(maxMessages)
                    .waitTimeSeconds(waitTime)
                    .build();

            List<Message> messages = sqsClient.receiveMessage(receiveRequest).messages();
            if (!messages.isEmpty()) {
                messages.forEach(this::processSQSMessage);
            }
        } catch (Exception e) {
            context.getLogger(this).error("Error processing messages", e);
        }
    }

    private void processSQSMessage(Message message) {
        try {
            JSONObject jsonMessage = new JSONObject(message.body());
            JSONArray records = jsonMessage.optJSONArray("Records");

            if (records != null) {
                for (int i = 0; i < records.length(); i++) {
                    JSONObject record = records.getJSONObject(i);
                    JSONObject s3Info = record.getJSONObject("s3");
                    String bucketName = s3Info.getJSONObject("bucket").getString("name");
                    String fileKey = s3Info.getJSONObject("object").getString("key");
                    fetchAndProcessFile(bucketName, fileKey);
                }
            }
        } catch (Exception e) {
            context.getLogger(this).error("Error processing SQS message", e);
        } finally {
            deleteMessageFromQueue(message);
        }
    }

    private void fetchAndProcessFile(String bucketName, String fileKey) {
        context.getLogger(this).debug("Fetching file from S3 - Bucket: {} | FileKey: {}", bucketName, fileKey);

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(fileKey)
                .build();

        try {
            InputStream inputStream = s3Client.getObject(getObjectRequest);
            InputStream effectiveStream = fileKey.contains(".gz")
                    ? new GZIPInputStream(inputStream)
                    : inputStream;

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(effectiveStream, StandardCharsets.UTF_8))) {
                if(fileKey.contains(".gz")){
                    reader.lines().forEach(line -> generateEvent(bucketName, fileKey, line));
                } else if (fileKey.contains(".csv")) {
                    List<Map<String, String>> jsonData = processCSVFile(reader);
                    for (Map jsonDataMap : jsonData){
                        context.getLogger(this).debug("jsonDataMap {} ", jsonDataMap);
                        processEventString(bucketName,fileKey,mapper.writeValueAsString(jsonDataMap));
                    }
                }
            } catch (IOException ioException){
                generateErrorEventMap(bucketName, fileKey, ioException, "IOException Failed to fetch file from S3");
            }

        } catch (Exception e) {
            if (e instanceof  JsonProcessingException )
                generateErrorEventMap(bucketName, fileKey, e, "JsonProcessingException");
            else
                generateErrorEventMap(bucketName, fileKey, e, "Failed to fetch file from S3");
        }
    }

    private  List<Map<String, String>> processCSVFile(BufferedReader reader) {

        List<Map<String, String>> jsonData = new ArrayList<>();
        try (
                CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {
                for (CSVRecord record : csvParser) {
                    Map<String, String> jsonObject = new LinkedHashMap<>();
                    for (String header : csvParser.getHeaderMap().keySet()) {
                        jsonObject.put(header, record.get(header));
                    }
                    jsonData.add(jsonObject);

                    context.getLogger(this).debug("processCSVFile jsonObject {} ", mapper.writeValueAsString(jsonObject));
                }

        } catch (IOException e) {
            context.getLogger(this).error("processCSVFile cause {} | message {}", e.getCause(), e.getMessage());
        }
        return jsonData;
    }

    private void generateEvent(String bucketName, String fileKey, String line) {
        context.getLogger(this).debug("line : {}", line);
        if (isValidaJSON(line)) {
            try {
                JsonNode rootNode = getJSON(line);
                if (rootNode.isArray()) {
                    rootNode.forEach(node -> {
                        processEventArray(bucketName, fileKey, node);
                    });
                } else {
                    processEventString(bucketName, fileKey, line);
                }
            } catch (JsonProcessingException jsonProcessingException) {
                context.getLogger(this).error("JsonProcessingException", jsonProcessingException);
                generateErrorEventMap(bucketName, fileKey, jsonProcessingException,"JsonProcessingException");
            }
        }
    }

    private void processEventString(String bucketName, String fileKey, String jsonString) {
        //ObjectMapper mapper = new ObjectMapper();
        //Map<String, Object> eventMap = mapper.convertValue(jsonString, Map.class);
        Map<String, Object> eventMap = new HashMap<>();
        eventMap.put("message",jsonString);
        eventMap.put("bucketName", bucketName);
        eventMap.put("fileKey", fileKey);
        eventMap.put("timestamp", Instant.now().toString());
        eventMap.put("type", this.type);
        eventMap.putAll(addField);
        consumer.accept(eventMap);
    }

    private void processEventArray(String bucketName, String fileKey, JsonNode rootNode) {
        //ObjectMapper objMapper = new ObjectMapper();
        rootNode.forEach(node -> {
            //Map<String, Object> eventMap = objMapper.convertValue(node, Map.class);
            Map<String, Object> eventMap = new HashMap<>();
            eventMap.put("message",node);
            eventMap.put("bucketName", bucketName);
            eventMap.put("fileKey", fileKey);
            eventMap.put("timestamp", Instant.now().toString());
            eventMap.put("type", this.type);
            eventMap.putAll(addField);
            consumer.accept(eventMap);
        });
    }

    private void generateErrorEventMap(String bucketName, String fileKey, Exception e, String exceptionDetail) {
        Map<String, Object> errorEvent = new HashMap<>();
        errorEvent.put("error", exceptionDetail);
        errorEvent.put("bucketName", bucketName);
        errorEvent.put("fileKey", fileKey);
        errorEvent.put("timestamp", Instant.now().toString());
        errorEvent.put("type", this.type);
        errorEvent.putAll(addField);
        consumer.accept(errorEvent);
        context.getLogger(this).error("Error fetching or processing file from S3", e);
    }

    private boolean isValidaJSON(String json) {
        try {
            ObjectMapper objMapper = new ObjectMapper();
            objMapper.readTree(json);
            return true;
        } catch (Exception e) {
            context.getLogger(this).error("Not valid JSON {}", json);
            return false;
        }
    }

    private JsonNode getJSON(String json) throws JsonProcessingException {
        ObjectMapper objMapper = new ObjectMapper();
        return objMapper.readTree(json);
    }

    private void deleteMessageFromQueue(Message message) {
        try {
            sqsClient.deleteMessage(DeleteMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .receiptHandle(message.receiptHandle())
                    .build());
        } catch (Exception e) {
            context.getLogger(this).error("Error deleting message from SQS", e);
        }
    }


    @Override
    public void stop() {
        stopped = true;
        if (executorService != null) {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(20, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                executorService.shutdownNow();
            }
        }
        close();
    }

    @Override
    public void awaitStop() throws InterruptedException {
        done.await();
    }

    @Override
    public void close() {
        try {
            if (sqsClient != null) {
                sqsClient.close();
            }
            if (s3Client != null) {
                s3Client.close();
            }
        } catch (Exception e) {
            context.getLogger(this).error("Error closing AWS clients", e);
        }
    }

    @Override
    public Collection<PluginConfigSpec<?>> configSchema() {
        return List.of(QUEUE_URL, REGION, ACCESS_KEY, SECRET_KEY, ARN, MAX_MESSAGES, WAIT_TIME, TYPE, SQS_POLLING_FREQUENCY, ADD_FIELD);
    }

    @Override
    public String getName() {
        return "s3_sqs_input";
    }

    @Override
    public String getId() {
        return UUID.randomUUID().toString();
    }
}
