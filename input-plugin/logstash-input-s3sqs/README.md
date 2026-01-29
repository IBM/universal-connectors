## s3sqs input plug-in
### Meet s3sqs
* Tested versions: 1.0.0
* Developed by [IBM](https://github.ibm.com/Activity-Insights/universal-connectors/tree/master/input-plugin/logstash-input-s3sqs)
* Supported Guardium versions:
    * Guardium Data Protection: 11.4 and above

This is a [Logstash](https://github.com/elastic/logstash) input plug-in for the universal connector that is featured in IBM Security Guardium. The more details of this plugin can be found [here](./README.md). It retrieves messages from Amazon SQS, each containing the detailed path to an audit log file stored in an S3 bucket. The file is then read and transformed into events. The events are then sent over to corresponding filter plugin which transforms these audit logs into a [Guardium record](https://github.com/IBM/universal-connectors/blob/main/common/src/main/java/com/ibm/guardium/universalconnector/commons/structures/Record.java) instance (which is a standard structure made out of several parts). The information is then sent over to Guardium. Guardium records include the accessor (the person who tried to access the data), the session, data, and exceptions. If there are no errors, the data contains details about the query "construct". The construct details the main action (verb) and collections (objects) involved.

## Purpose:

Specify the Queue url from where the file name in S3 is to be read.

## Usage:

### Parameters:
| Parameter         | Input Type | Required | Default |
|-------------------|------------|----------|--------|
| queue_url         | string     | Yes      | |
| region            | string     | Yes      | |
| max_messages      | Integer    | Yes      | |
| wait_time         | Integer    | Yes      | |
| polling_frequency | Integer    | Yes      | |
| access_key_id     | string     | No       | |
| secret_access_key | string     | No       | |
| role_arn          | string     | No       | |
| type              | string     | No       | |
| account_id         | string     | No       | |

#### `queue_url`
The `queue_url` setting specifies the SQS queue URL created to receive notifications from S3.

#### `region`
The `region` setting allows specify the region in which the Amazon SQS Queue exists.

#### `max_messages`
The `max_messages` setting defines the maximum number of messages to retrieve in a single SQS call.

#### `wait_time`
The `wait_time` specifies the duration (in seconds) the call will wait for a message to appear in the queue before returning. If a message is available sooner, the call returns immediately. Ensure that `wait_time` is always less than the `polling_frequency`.

#### `polling_frequency`
The `polling_frequency` setting specifies the delay between consecutive SQS calls.

#### `access_key_id`
The `access_key_id` setting allows specifying the access key id of the IAM user having cloudwatch access to read the logs.

#### `secret_access_key`
The `secret_access_key` setting allows specifying the access secret key id of the IAM user having cloudwatch access to read the logs.

#### `role_arn`
The role_arn setting allows you to specify which AWS IAM Role to assume, if any. This is used to generate temporary credentials, typically for cross-account access. Click [here](S3SQS_RoleARN.md) to configure the RoleARN.

#### `type`
The type you can use to specify the plugin type in the input section.

#### `account_id`
You can use the account_id to specify the AWS account ID

## Note:
For detailed instructions on collecting RDS audit logs with Amazon Kinesis Data Firehose, please refer "[S3SQSWithFireHose](S3SQSWithFirehose.md)".

## Limitations:
Duplicate Event Delivery: Amazon S3 does not support sending event notifications to FIFO SQS queues. Only Standard queues are compatible with S3 event notifications.
The plugin relies on Amazon SQS Standard queues, which guarantee at-least-once delivery. As a result, the same S3 event may be delivered multiple times, leading to duplicate processing of S3 objects.