package org.logstashplugins;



import org.junit.Test;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageResponse;
import software.amazon.awssdk.services.sqs.model.Message;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class S3SQSLogstashPluginTest {

    @Test
    public void testRetrieveMessageFromSQS() {
        Message message = Message.builder().body("{\"Records\":[{\"s3\":{\"bucket\":{\"name\":\"cloudwatchlogspostgres1\"},\"object\":{\"key\":\"2025/02/27/11/CloudWatchLogstoS3-5-2025-02-27-11-03-48-05c21749-c976-4f0a-9df9-abec5111d9d4\"}}}]}" ).build();
        ReceiveMessageResponse response = ReceiveMessageResponse.builder().messages(Collections.singletonList(message)).build();

        List<Message> messages = Collections.singletonList(message);

        assertFalse(messages.isEmpty());
        assertEquals("{\"Records\":[{\"s3\":{\"bucket\":{\"name\":\"cloudwatchlogspostgres1\"},\"object\":{\"key\":\"2025/02/27/11/CloudWatchLogstoS3-5-2025-02-27-11-03-48-05c21749-c976-4f0a-9df9-abec5111d9d4\"}}}]}", messages.get(0).body());
    }

    @Test
    public void testRetrieveFileFromS3() {
        String fileContent = "{\"timestamp\":\"2025-02-27T11:17:53.777587Z\",\"bucketName\":\"cloudwatchlogspostgres1\",\"Name\":\"Piyush.Desai@ibm.com\",\"type\":\"S3SQS\",\"message\":\"{\"messageType\":\"DATA_MESSAGE\",\"owner\":\"346824953529\",\"logGroup\":\"aws/rds/cluster/auroramysql/audit\",\"logStream\":\"auroramysql-instance-1.audit.log.1\",\"subscriptionFilters\":[\"AuroraMySQLCloudwatchToS3\"],\"logEvents\":[{\"id\":\"38817886306025407784629710918078008262425635349182414848\",\"timestamp\":1740654223007,\"message\":\"CREATE TABLE log_20250227_110342 ( id INT AUTO_INCREMENT PRIMARY KEY, data VARCHAR(255) NOT NULL, created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ) ENGINE=InnoDB\"}]}\"}\",\"@version\":\"1\",\"@timestamp\":\"2025-02-27T11:17:53.777677Z\",\"fileKey\":\"2025/02/27/11/CloudWatchLogstoS3-5-2025-02-27-11-03-48-05c21749-c976-4f0a-9df9-abec5111d9d4\"}";
        ByteArrayInputStream inputStream = new ByteArrayInputStream(fileContent.getBytes(StandardCharsets.UTF_8));

        GetObjectRequest request = GetObjectRequest.builder().bucket("cloudwatchlogspostgres1").key("2025/02/27/11/CloudWatchLogstoS3-5-2025-02-27-11-03-48-05c21749-c976-4f0a-9df9-abec5111d9d4").build();
        GetObjectResponse response = GetObjectResponse.builder().build();

        String content = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);

        assertEquals(fileContent, content);
    }

}
