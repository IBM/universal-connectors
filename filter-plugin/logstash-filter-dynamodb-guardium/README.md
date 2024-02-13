# Dynamodb-Guardium Logstash filter plug-in

### Meet DynamoDB
* Environment: AWS
* Supported Guardium versions:
    * Guardium Data Protection: 11.4 and later
        * Supported inputs:
            * S3 (pull)
            * CloudWatch (pull)
            * SQS (Pull)
    * Guardium Insights: 3.2
        * Supported inputs:
            * CloudWatch (pull)
    * Guardium Insights SaaS: 1.0
        * Supported inputs:
            * CloudWatch (pull)

This is a [Logstash](https://github.com/elastic/logstash) filter plug-in
for the universal connector that is featured in IBM Security Guardium.
It parses events and messages from the Amazon DynamoDB audit log
into a [Guardium record](https://github.com/IBM/universal-connectors/blob/main/common/src/main/java/com/ibm/guardium/universalconnector/commons/structures/Record.java)
instance (which is a standard structure made out of several parts).
The information is then sent over to Guardium. Guardium records include the accessor
(the person who tried to access the data), the session, data, and exceptions.
If there are no errors, the data contains details about the query "construct".
The construct details the main action (verb) and collections (objects) involved.

The plug-in is free and open-source (Apache 2.0). It can be used as a starting
point to develop additional filter plug-ins for Guardium universal connector.


## 1. Configuring Amazon DynamoDB

In the AWS web interface, configure the service for DynamoDB.

### Procedure

1. From https://console.aws.amazon.com/, click **Services**.
2. Underneath **All services**, click on **Database**.
3. On the right panel, click **DynamoDB**.
4. Click the drop-down menu and select your region.
5. Click **Create Table** button.
6. Enter a table name.
7. Enter a partition key.
8. Scroll down and click **Create table**.

## 2. Enabling audit logs

There are different ways to audit and log events. We will use CloudTrail for this 
example since it supports all required parameters.

### Procedure

1. Click **Services**.
2. Go to **All services** > **Management & Governance**.
3. Click **Cloud trail**.
4. Click **Create trail** button.
5. Provide a trail name in the **Trail name** field.
6. For **Storage location**, verify that **Create new S3 bucket** is selected.
   The logs can be read directly from this bucket using the S3 input of the logstash.
7. In **Log file SSE-KMS encryption**, uncheck **Enabled**.
8. To monitor the logs through CloudWatch, take the following steps to forward them to Cloudwatch:
   1. Under **CloudWatch Logs**, check **Enabled**.
   2. Verify that **New** is selected for **Log group**.
   3. Under **Log group name**, provide a new log group name.
   4. Verify that **New** is selected for **IAM Role**.
   5. For **Role name**, provide a new role name.
9. Click **Next**.
10. For **Event type**, select **Management events** and **Data events**.
11. Verify that **Read** and **Write** are selected for **API Activity**.
12. In the **Data Events** section, click **Switch to basic event selectors**.
13. Click **Continue** to confirm.
14. Click **Add data event type**.
15. Click **Data event source** and select **DynamoDB**.
16. Click **NEXT**.
17. Verify that all parameters shown are correct.
18. Click **Create trail**.


## 3. Viewing the logs on CloudWatch

### Procedure
1. Click the Service drop-down.
2. In the **Recently visited** panel, click on **CloudWatch**.
3. Click **Logs** > **Log groups**.
4. In the search box, enter the name of the log group created previously in step 9.
5. Click on the log group that appears from the search.
6. All logs appear under log streams in this format: <account_id>_CloudTrail_<region>

## 4. Exporting Cloudwatch Logs to SQS using Lambda function
In order to achieve load balancing of audit logs between different collectors,
the audit logs must be exported from Cloudwatch to SQS.

### Creating the SQS queue
The SQS that you create contains messages that will be filled by the Lambda 
function (see [Creating the Lambda function](#creating-the-lambda-function)) by reading the 
CloudWatch logs.
#### Procedure
1. From https://console.aws.amazon.com/, click **Services**.
2. Search for SQS and click on **Simple Queue Services**.
3. Click **Create Queue**.
4. Select the type as **Standard**.
5. Enter the name for the queue.
6. Keep the rest of the default settings.

### Creating a policy for the relevant IAM User
Perform the following steps for the IAM user who is accessing the SQS logs in
Guardium:
#### Procedure
1. Go to https://console.aws.amazon.com/
2. Go to **IAM service** > **Policies** > **Create Policy**.
3. Select **service as SQS**.
4. Check the following checkboxes:
    * **ListQueues**
    * **DeleteMessage**
    * **DeleteMessageBatch**
    * **GetQueueAttributes**
    *  **GetQueueUrl**
    * **ReceiveMessage**
    * **ChangeMessageVisibility**
    * **ChangeMessageVisibilityBatch**
5. In the resources, specify the ARN of the queue created in the above step.
6. Click **Review policy** and specify the policy name.
7. Click **Create policy**.
8. Assign the policy to the user
    1. Log in to the IAM console as an IAM user (https://console.aws.amazon.com/iam/).
    2. Go to **Users** on the console and select the relevant IAM user to whom you want to give permissions.
       Click the **username**.
    3. In the **Permissions tab**, click **Add permissions**.
    4. Click **Attach existing policies directly**.
    5. Search for the policy created and check the checkbox next to it.
    6. Click **Next: Review**
    7. Click **Add permissions**

### Creating the Lambda function
The Lambda function will read the CloudWatch Logs and send the events into the SQS queue.
Follow the steps below to configure the Lambda function.

#### Create IAM Role
Create the IAM role that will be used in the Lambda function setup. The AWS Lambda service requires permission to log 
events and write to the SQS queue. Create the IAM Role **Export-Dynamo-CloudWatch-to-SQS-Lambda** with
"AmazonSQSFullAccess", "CloudWatchLogsFullAccess", and "CloudWatchEventsFullAccess" policies.

__*Procedure*__
1. From https://console.aws.amazon.com/, browse to **IAM** -> **Roles**.
2. Click **Create Role**
3. Under use case select **Lambda** and click **Next**
4. Search for **AmazonSQSFullAccess** and select it
5. Search for **CloudWatchLogsFullAccess** and select it
6. Search for **CloudWatchEventsFullAccess** and select it
7. Set the Role Name: e.g., "Export-Dynamo-CloudWatch-to-SQS-Lambda" and click **Create role**.


#### Create the Lambda function
__*Procedure*__
1. Go to https://console.aws.amazon.com/, click **Services**.
2. Search for **Lambda function**.
3. Click **Functions**
4. Click **Create Function**
5. Keep **Author for Scratch** selected
6. Set a function name e.g., Export-Dynamo-CloudWatch-Logs-To-SQS.
7. Under **Runtime**, select **Python 3.x**.
8. Under **Permissions**, select **Use an existing role** and select the IAM role that you created in
   the previous step (Export-Dynamo-CloudWatch-Logs-To-SQS).
9. Click **Create function** and navigate to **Code view**.
10. Add the function code from the file [lambda.py](DynamodbOverSQSPackage/lambda.py).
11. Click **Configuration** > **Environment Variables**.
12. Create 2 variables:
    1. `Key = GROUP_NAME value = <value>` e.g., `uc-dynamo-plugin-cloudwatch-group`
    2. `Key = QUEUE_NAME value = <value>` e.g., `https://sqs.ap-south-1.amazonaws.com/346824951129/np-dynamo-sqs`
13. Save the function.
14. Click **Deploy**.

#### Automate the Lambda function
The Lambda will be called by a scheduler configured inside event rules in CloudWatch.

**_Procedure_**
1. From the CloudWatch dashboard, browse to **Events** > **Rules**.
2. Click **Create Rule**.
3. Enter the name for the rule e.g., cloudwatchToSqs
4. Under **Rule Type**, select **Schedule**.
5. Define the schedule. In **schedule pattern**, select a schedule that runs at a regular rate,
   such as every 10 minutes.
6. Enter the rate expression, meaning the rate at which the function should execute.
   **Note**: This value must match the time that is specified in the Lambda function code 
   that calculates the time delta. (For example, if the function code is 
   set to 2 minutes, set the rate to 2 minutes). 
7. Click **Next**.
8. Select **Target1**. Select the **Target Type** as **AWS Service**.
9. Select **Target** as **Lambda Function**.
10. Select the Lambda function created in the above step. e.g., Export-Dynamo-CloudWatch-Logs-To-SQS.
11. Add the tag if needed.
12. Click **Create Rule**.

#### Note
* Before you make changes to the Lambda function code, disable the rule that you just created.
  Deploy the change and then re-enable the rule.
* If the **Management events** are enabled while configuring the **CloudTrail**, it is possible
  that a large amount of data will be read by the Lambda function. In this case 
  you might need to tune the `timeout` and `memory` parameters.
  Below are the steps to take to configure those parameters,
    * Go to the created Lambda Function.
    * Go to **General Configurations**.
    * Click **Edit**.
    * Update the value of **Memory** as required.
    * Update the value of **Timeout** as required.

### Limitations

1. The DynamoDB plug-in does not support IPV6.
2. You may need to disable management events in order to avoid heavy traffic and data loss in Guardium. Disabling management events disables logging of the following events:
   CreateTable, DeleteTable, ListTable, UpdateTable, DescribeTable events.
3. The `Client HostName` field cannot be mapped with the DynamoDB audit logs, Set this field to _NA_.


## 4. Configuring the Dynamodb filters in Guardium

The Guardium universal connector is the Guardium entry point for native
audit logs. The universal connector identifies and parses received events,
and then converts them to a standard Guardium format. The output of the universal
connector is forwarded to the Guardium sniffer on the collector, for
policy and auditing enforcements. Configure Guardium to read the native
audit logs by customizing the DynamoDB template.

### Authorizing outgoing traffic from AWS to Guardium

#### Procedure

1. Log in to the Guardium Collector's APIs.
2. Issue these commands:
```
grdapi add_domain_to_universal_connector_allowed_domains domain=amazonaws.com
grdapi add_domain_to_universal_connector_allowed_domains domain=amazon.com
```

#### Before you begin
• Configure the policies you require. See ([policies](/docs/#policies)) for more information.
• You must have permission for the S-Tap Management role. The admin user includes this role by default.

• Download the [cloudtrail_codec_plugin.zip](DynamodbOverCloudtrailPackage/DynamoDB/cloudtrail_codec_plugin.zip) plug-in, if the logs are to be pulled directly from CloudTrail. (Do not unzip the offline-package file throughout the procedure). This step is not necessary for Guardium Data Protection v12.0 and later.

• Download the [logstash-filter-dynamodb_guardium_plugin_filter.zip plug-in.](./logstash-filter-dynamodb_guardium_plugin_filter.zip)

### Procedure
1. On the collector, go to **Setup** > **Tools and Views** > **Configure Universal Connector**.
2. Enable the connector if it is already disabled, before uploading the 
Universal Connector, make sure that it is enabled.
3. Click **Upload File**,
    * If the audit logs are to be fetched from CloudWatch directly, 
      select the [logstash-filter-dynamodb_guardium_plugin_filter.zip](https://github.com/IBM/universal-connectors/releases/download/v1.5.1/logstash-filter-dynamodb_guardium_plugin_filter.zip) plug-in. After it is uploaded, click **OK**.
    * If the audit logs are to be fetched from CloudTrail, select the
      1. [cloudtrail_codec_plugin.zip](./DynamodbOverCloudtrailPackage/DynamoDB/cloudtrail_codec_plugin.zip) plug-in. After it is uploaded, click **OK**. 
       **Note:** This step is not necessary for Guardium Data Protection 12.0 and later. 
      2. Select [logstash-filter-dynamodb_guardium_plugin_filter.zip](https://github.com/IBM/universal-connectors/releases/download/v1.5.1/logstash-filter-dynamodb_guardium_plugin_filter.zip) plug-in. After it is uploaded, click **OK**.
    * If the audit logs are to be fetched from SQS, select the [logstash-filter-dynamodb_guardium_plugin_filter.zip](https://github.com/IBM/universal-connectors/releases/download/v1.5.1/logstash-filter-dynamodb_guardium_plugin_filter.zip) plug-in. After it is uploaded, click **OK**.
4. Click the Plus sign to open the Connector Configuration dialog box.
5. Type a name in the **Connector name** field.
6. Update the input section to add the details from the corresponding file's input part, omitting the keyword "input{" at the beginning and its corresponding "}"
   at the end.
    1. If the audit logs are to be fetched from CloudWatch, use the details from the [dynamodbCloudwatch.conf](./dynamodbCloudwatch.conf)
       file. For more details about configuring the relevent input plugin, see [here](../../input-plugin/logstash-input-cloudwatch-logs/README.md).
    2. If the audit logs are to be fetched from CloudTrail, use the details from the [dynamodbCloudtrail.conf](./dynamodbCloudtrail.conf)
       file.
    3. If the audit logs are to be fetched from SQS, use the details from the [dynamodbSQS.conf](./dynamodbSqs.conf)
       file.
7. Update the filter section to add the details from the corresponding file's input part, omitting the keyword "filter{" at the beginning and its corresponding "}"
   at the end.
    1. If the audit logs are to be fetched from CloudWatch, use the 
       details from the [dynamodbCloudwatch.conf](./dynamodbCloudwatch.conf) file.
    2. If the audit logs are to be fetched from CloudTrail,
       use the details from the [dynamodbCloudtrail.conf](./dynamodbCloudtrail.conf) file.
    3. If the audit logs are to be fetched from SQS,
       use the details from the [dynamodbSQS.conf](./dynamodbSqs.conf) file.
8. The "type" fields should match in input and filter configuration sections. This field should be unique for every individual connector added.
9. Click **Save**. Guardium validates the new connector, and enables the universal connector if it was
   disabled. After it is validated, it appears in the Configure Universal Connector page.

## Configuring the DynamoDB filters in Guardium Insights

To configure this plug-in for Guardium Insights, follow [this guide.](https://github.com/IBM/universal-connectors/blob/main/docs/UC_Configuration_GI.md)

In the input configuration section, refer to the CloudWatch_logs section.
