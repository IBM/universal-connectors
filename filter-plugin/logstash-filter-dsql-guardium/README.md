## DSQL-Guardium Logstash filter plug-in

### Meet DSQL
* Tested versions: 
* Environment: AWS
* Supported inputs: SQS (pull)
* Supported Guardium versions:
    * Guardium Data Protection: 

This is a [Logstash](https://github.com/elastic/logstash) filter plug-in for the universal connector that is featured in IBM Security Guardium. It parses events and messages from the AWS DSQL Database Activity Streams into a [Guardium record](https://github.com/IBM/universal-connectors/blob/main/common/src/main/java/com/ibm/guardium/universalconnector/commons/structures/Record.java) instance (which is a standard structure made out of several parts). The information is then sent over to Guardium. Guardium records include the accessor (the person who tried to access the data), the session, data, and exceptions. If there are no errors, the data contains details about the query "construct". The construct details the main action (verb) and collections (objects) involved.

The plug-in is free and open-source (Apache 2.0). It can be used as a starting point to develop additional filter plug-ins for the Guardium universal connector.

## 1. Overview

AWS DSQL (Database SQL) is a distributed SQL database service that provides PostgreSQL-compatible database capabilities. This plugin processes audit logs from DSQL Database Activity Streams, which are delivered via Amazon SQS (Simple Queue Service).

### Key Features
- Parses DSQL Database Activity Stream events in PostgreSQL format
- Supports both successful queries and error/exception events
- Captures detailed session information including client IP, port, and user details
- Handles authentication failures and SQL errors
- Compatible with AWS SQS input plugin

## 2. Configuring DSQL Database Activity Streams

### 2.1 Enable Database Activity Streams

1. Navigate to the AWS RDS console
2. Select your DSQL cluster
3. Enable Database Activity Streams:
   - Choose **Actions** → **Start activity stream**
   - Select **Asynchronous** mode (recommended for production)
   - Choose or create a KMS key for encryption
   - Note the Kinesis Data Stream name created

### 2.2 Configure SQS Queue

1. Create an SQS queue to receive the activity stream events:
   ```bash
   aws sqs create-queue --queue-name dsql-activity-stream-queue
   ```

2. Configure the Kinesis Data Stream to send events to SQS:
   - Create a Lambda function to forward Kinesis events to SQS
   - Or use Kinesis Data Firehose to deliver to SQS

3. Set appropriate IAM permissions for the SQS queue

### 2.3 Required IAM Permissions

The AWS credentials used by Logstash must have the following permissions:

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [
        "sqs:ReceiveMessage",
        "sqs:DeleteMessage",
        "sqs:GetQueueAttributes"
      ],
      "Resource": "arn:aws:sqs:REGION:ACCOUNT_ID:dsql-activity-stream-queue"
    }
  ]
}
```

## 3. Configuring the DSQL Filter in Guardium

### 3.1 Logstash Configuration

The Guardium universal connector is the Logstash runtime environment that comes with a Logstash service. The DSQL filter plug-in is a configuration file that defines the settings for the Logstash service.

#### Procedure

1. On the collector, go to **Setup** → **Tools and Views** → **Configure Universal Connector**.

2. Enable the universal connector if it is disabled.

3. Click **Upload File** and select the offline [dsql-offline-plugins-7.5.2.zip](./DSQLOverSQSPackage/DSQL/dsql-offline-plugins-7.5.2.zip) plug-in. After it is uploaded, click **OK**.

4. Click the Plus sign to open the Connector Configuration dialog box.

5. Type a name in the **Connector name** field.

6. Update the input section to add details from your SQS queue:

```
input {
    sqs {
        access_key_id => "<ACCESS_KEY>"
        secret_access_key => "<SECRET_KEY>"
        region => "<REGION>"
        queue => "<QUEUE_NAME>"
        codec => "json"
        type => "dsql"
        add_field => {"account_id" => "<ACCOUNT_ID>"}
        add_field => {"instance_name" => "<INSTANCE_NAME>"}
    }
}
```

7. The filter section is provided in the [filter.conf](./DSQLOverSQSPackage/DSQL/filter.conf) file. Include this section in your configuration.

8. The output section should point to your Guardium collector:

```
output {
    if [GuardRecord] {
        guardium_connector {
            guardium_ip => "<GUARDIUM_IP>"
            guardium_port => <GUARDIUM_PORT>
        }
    }
}
```

## 4. DSQL Audit Log Format

The DSQL Database Activity Streams provide audit logs in JSON format. The plugin supports two formats: flat format and nested DatabaseActivityMonitoringRecord format.

### 4.1 Flat Format

#### 4.1.1 Successful Query Event
```json
{
  "type": "record",
  "databaseName": "mydb",
  "dbUserName": "postgres",
  "remoteHost": "10.0.1.100",
  "remotePort": 54321,
  "sessionId": "session-12345",
  "statementText": "SELECT * FROM users WHERE id = 1;",
  "commandTag": "SELECT",
  "exitCode": 0,
  "logTime": "2023-11-10T10:15:30.123Z",
  "clientApplication": "psql",
  "statementId": "stmt-001"
}
```

#### 4.1.2 Error Event
```json
{
  "type": "record",
  "databaseName": "mydb",
  "dbUserName": "postgres",
  "remoteHost": "10.0.1.100",
  "remotePort": 54321,
  "sessionId": "session-12345",
  "statementText": "SELECT * FROM nonexistent_table;",
  "exitCode": 1,
  "errorMessage": "relation \"nonexistent_table\" does not exist",
  "logTime": "2023-11-10T10:15:30.123Z"
}
```

#### 4.1.3 Authentication Failure Event
```json
{
  "type": "record",
  "databaseName": "mydb",
  "dbUserName": "baduser",
  "remoteHost": "10.0.1.100",
  "remotePort": 54321,
  "exitCode": 1,
  "errorMessage": "password authentication failed for user \"baduser\"",
  "logTime": "2023-11-10T10:15:30.123Z"
}
```

### 4.2 Nested DatabaseActivityMonitoringRecord Format

The plugin also supports a nested format where events are wrapped in a `DatabaseActivityMonitoringRecord` structure with a `databaseActivityEventList` array.

#### 4.2.1 Nested Format - Successful Query Event
```json
{
  "type": "DatabaseActivityMonitoringRecord",
  "clusterId": "cluster-abc123",
  "instanceId": "db-INSTANCE123",
  "databaseActivityEventList": [
    {
      "type": "record",
      "class": "READ",
      "command": "SELECT",
      "commandText": "SELECT * FROM users WHERE id = 1;",
      "databaseName": "mydb",
      "dbProtocol": "POSTGRESQL",
      "dbUserName": "postgres",
      "exitCode": 0,
      "logTime": "2023-11-10T10:15:30.123Z",
      "remoteHost": "10.0.1.100",
      "remotePort": 5432,
      "serverHost": "172.31.30.159",
      "serverType": "POSTGRESQL",
      "sessionId": "session-456",
      "clientApplication": "psql"
    }
  ]
}
```

#### 4.2.2 Nested Format - Login Failure Event
```json
{
  "type": "DatabaseActivityMonitoringRecord",
  "clusterId": "cluster-abc123",
  "instanceId": "db-INSTANCE123",
  "databaseActivityEventList": [
    {
      "type": "record",
      "class": "LOGIN",
      "command": "LOGIN FAILED",
      "commandText": "Login attempt failed",
      "databaseName": "postgres",
      "dbProtocol": "POSTGRESQL",
      "dbUserName": "baduser",
      "errorMessage": "password authentication failed for user \"baduser\"",
      "exitCode": 1,
      "logTime": "2023-11-10T10:15:30.123Z",
      "remoteHost": "10.0.1.100",
      "remotePort": 5432,
      "serverHost": "172.31.30.159",
      "serverType": "POSTGRESQL",
      "sessionId": "session-123",
      "clientApplication": "psql"
    }
  ]
}
```

#### 4.2.3 Nested Format - DDL Statement
```json
{
  "type": "DatabaseActivityMonitoringRecord",
  "clusterId": "cluster-abc123",
  "instanceId": "db-INSTANCE123",
  "databaseActivityEventList": [
    {
      "type": "record",
      "class": "SCHEMA",
      "command": "CREATE",
      "commandText": "CREATE TABLE users (id serial PRIMARY KEY, name varchar(100));",
      "databaseName": "mydb",
      "dbProtocol": "POSTGRESQL",
      "dbUserName": "postgres",
      "exitCode": 0,
      "logTime": "2023-11-10T10:15:30.123Z",
      "remoteHost": "10.0.1.100",
      "remotePort": 5432,
      "serverHost": "172.31.30.159",
      "serverType": "POSTGRESQL",
      "sessionId": "session-789",
      "objectName": "users",
      "objectType": "TABLE",
      "clientApplication": "psql"
    }
  ]
}
```

### 4.3 Field Mapping

The parser automatically detects the format (flat or nested) and extracts events accordingly. For nested format, the parser processes the first event in the `databaseActivityEventList` array.

**Key differences between formats:**
- **Flat format**: Uses `statementText` for SQL commands
- **Nested format**: Uses `commandText` for SQL commands (parser handles both)
- **Nested format**: Includes additional fields like `class`, `command`, `objectName`, `objectType`
- **Nested format**: Parent-level fields (`instanceId`, `clusterId`) are preserved

**Error detection:** The parser determines if an event is an error based on the presence of a non-empty `errorMessage` field, not the `exitCode` value.

## 5. Supported Fields

The plugin extracts and maps the following fields from DSQL audit logs:

| DSQL Field | Guardium Field | Description |
|------------|----------------|-------------|
| databaseName | dbName | Database name |
| dbUserName | accessor.dbUser | Database user |
| remoteHost | sessionLocator.clientIp | Client IP address |
| remotePort | sessionLocator.clientPort | Client port |
| sessionId | sessionId | Session identifier |
| statementText | data.originalSqlCommand | SQL statement |
| exitCode | - | Exit code (0=success, non-zero=error) |
| errorMessage | exception.description | Error description |
| logTime | time.timestamp | Event timestamp |
| clientApplication | accessor.sourceProgram | Client application name |

## 6. Limitations

- The DSQL plug-in does not support IPv6
- Client hostname and OS user fields are not available in DSQL audit logs and are set as empty
- The plugin uses PostgreSQL as the server type and protocol since DSQL is PostgreSQL-compatible

## 7. Troubleshooting

### 7.1 No Events Received

**Problem**: Logstash is not receiving events from SQS.

**Solution**:
- Verify SQS queue name and region are correct
- Check AWS credentials have proper permissions
- Ensure Database Activity Streams are enabled on DSQL cluster
- Verify the Kinesis-to-SQS forwarding is configured correctly

### 7.2 Parsing Errors

**Problem**: Events are received but not parsed correctly.

**Solution**:
- Check the Logstash logs for parsing errors
- Verify the JSON format matches the expected DSQL audit log format
- Ensure the `codec => "json"` is set in the SQS input configuration

### 7.3 Missing Fields

**Problem**: Some fields are not appearing in Guardium reports.

**Solution**:
- Verify all required fields are present in the DSQL audit logs
- Check that the filter configuration includes all field mappings
- Review the Constants.java file for field name mappings

## 8. Additional Resources

- [AWS DSQL Documentation](https://docs.aws.amazon.com/dsql/)
- [AWS RDS Database Activity Streams](https://docs.aws.amazon.com/AmazonRDS/latest/UserGuide/DBActivityStreams.html)
- [Guardium Universal Connector Documentation](https://github.com/IBM/universal-connectors)
- [Logstash SQS Input Plugin](https://www.elastic.co/guide/en/logstash/current/plugins-inputs-sqs.html)

## 9. License

This plugin is licensed under the Apache License 2.0. See the [LICENSE](./LICENSE) file for details.
