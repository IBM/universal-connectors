# Dynamodb-Guardium Logstash filter plug-in

### Meet DynamoDB
* Environment: AWS
* Supported Guardium versions:
	* Guardium Data Protection: 11.4 and above
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

1. Go to https://console.aws.amazon.com/.
2. Click **Services** in the top left menu.
3. Underneath **All services**, click on **Database**.
4. On the right panel, click **DynamoDB**.
5. Click the drop-down menu and select your region.
6. Click **Create Table** button.
7. Enter a table name.
8. Enter a partition key.
9. Scroll down and click **Create table**.

## 2. Enabling audit logs 

There are different ways to audit and log events. We will use CloudTrail for this example since it supports all required parameters. The following events are supported for auditing in AWS:

### Procedure

1. Click **Services**.
2. Go to **All services** > **Management & Governance**.
3. Click **Cloud trail**.
4. Click **Create trail** button.
5. Provide a trail name in the **Trail name** field.
6. For **Storage location**, verify that **Create new S3 bucket** is selected.
	 The logs can be read directly from this bucket using the S3 input of the logstash.
7. In **Log file SSE-KMS encryption**, uncheck **Enabled**. 
8. If the logs are to be monitored through CloudWatch, then forward them to CloudWatch using steps 9 to 13. (If not, skip those steps).
9. In **CloudWatch Logs**, check **Enabled**.
10. Verify **New** is selected for **Log group**.
11. Under **Log group name**, provide a new log group name.
12. Verify **New** is selected for **IAM Role**.
13. For **Role name**, provide a new role name.
14. Click **Next**.
15. For **Event type**, select **Management events** and **Data events**.
16. Verify that **Read** and **Write** are selected for **API Activity**.
17. In the **Data Events** section, click **Switch to basic event selectors**.
18. Click **Continue** to confirm.
19. Click **Add data event type**.
20. Click **Data event source** and select **DynamoDB**.
21. Click **NEXT**.
22. Verify that all parameters shown are correct.
23. Click **Create trail**.


## 3. Viewing the logs on CloudWatch

### Procedure
1. Click the Service drop-down.
2. In the **Recently visited** panel, click on **CloudWatch**.
3. Click **Logs** > **Log groups**.
4. In the search box, enter the name of the log group created previously in step 9.
5. Click on the log group that appears from the search.
6. All logs appear under log streams in this format: <account_id>_CloudTrail_<region>

## 4. Exporting Cloudwatch Logs to SQS using lambda function
In order to achieve load balancing of audit logs between different collectors, 
the audit logs must be exported from Cloudwatch to SQS.

### Creating the SQS queue
The SQS created in these steps will contain the messages to be filled up by the
lambda function (created in next section) in the queue by reading the CloudWatch logs.
The messages inside the SQS will contain content from CloudWatch logs.
#### Procedure
1. Go to https://console.aws.amazon.com/
2. Click **Services**
3. Search for SQS and click on **Simple Queue Services**
4. Click **Create Queue**.
5. Select the type as **Standard**.
6. Enter the name for the queue
7. Keep the rest of the default settings

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

#### Creating IAM Role
Create the IAM role that will be used in the Lambda function setup. The AWS Lambda service will require permission to
log events and write to the SQS created. Create the IAM Role **Export-Dynamo-CloudWatch-to-SQS-Lambda** with
"AmazonSQSFullAccess", "CloudWatchLogsFullAccess", and "CloudWatchEventsFullAccess" policies.

__*Procedure*__
1. Go to https://console.aws.amazon.com/
2. Go to **IAM** -> **Roles**
3. Click **Create Role**
4. Under use case select **Lambda** and click **Next**
5. Search for **AmazonSQSFullAccess** and select it
6. Search for **CloudWatchLogsFullAccess** and select it
7. Search for **CloudWatchEventsFullAccess** and select it
8. Set the Role Name: e.g., "Export-Dynamo-CloudWatch-to-SQS-Lambda" and click **Create role**.


#### Create the lambda function
__*Procedure*__
1. Go to https://console.aws.amazon.com/
2. Go to **Services**. Search for **lambda function**.
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

#### Automating the lambda function
The Lambda will be called by a scheduler configured inside event rules in CloudWatch.

**_Procedure_**
1. Go to the CloudWatch dashboard.
2. Go to **Events** > **Rules**.
3. Click **Create Rule**.
4. Enter the name for the rule e.g., cloudwatchToSqs
5. Under **Rule Type**, select **Schedule**.
6. Define the schedule. In **schedule pattern**, select a schedule that runs at a regular rate,
   such as every 10 minutes.
7. Enter the rate expression, meaning the rate at which the function should execute.
   This value must match the time specified in the lambda function code that calculates the time delta.
   (If the function code it is set to 2 minutes, set the rate to 2 minutes unless changed in the code). Click **Next**.
8. Select **Target1**. Select the **Target Type** as **AWS Service**.
9. Select **Target** as **Lambda Function**.
10. Select the lambda function created in the above step. e.g., Export-Dynamo-CloudWatch-Logs-To-SQS.
11. Add the tag if needed.
12. Click **Create Rule**.

#### Note
* Before making any changes to the lambda function code, first disable the above rule.
  Deploy the change and then re-enable the rule.
* If the **Management events** are enabled while configuring the **CloudTrail**, it is possible
  that the data being read by the Lambda Function would be large. In such cases the `timeout` parameter
  and the `memory` parameters may have to be tuned properly.
  Below are the steps to take to configure those parameters,
    * Go to the created Lambda Function.
    * Go to **General Configurations**.
    * Click **Edit**.
    * Update the value of **Memory** as required.
    * Update the value of **Timeout** as required.

### Limitations

1. The Dynamo DB plug-in does not support IPV6.
2. You may need to disable management events in order to avoid heavy traffic and data loss in Guardium. Disabling management events disables logging of the following events:
   CreateTable, DeleteTable, ListTable, UpdateTable, DescribeTable events.
3. The following fields couldn't be mapped with the Dynamo audit logs,
    1. Client HostName : Not available with audit logs so set as NA.


## 4. Configuring the Dynamodb filters in Guardium

The Guardium universal connector is the Guardium entry point for native 
audit logs. The universal connector identifies and parses received events, 
and then converts them to a standard Guardium format. The output of the universal 
connector is forwarded to the Guardium sniffer on the collector, for 
policy and auditing enforcements. Configure Guardium to read the native 
audit logs by customizing the dynamodb template.

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
2. Enable the connector if it is already disabled, before proceeding to upload the UC.
3. Click **Upload File**,
    * If the audit logs are to be fetched from CloudTrail, select the
        1. [cloudtrail_codec_plugin.zip](./DynamodbOverCloudtrailPackage/DynamoDB/cloudtrail_codec_plugin.zip) plug-in. After it is uploaded, click **OK**. This is specifically for CloudTrail only. Other types of inputs do not require this file to be uploaded.
    * Select [logstash-filter-dynamodb_guardium_plugin_filter.zip](logstash-filter-dynamodb_guardium_plugin_filter.zip) plug-in. After it is uploaded, click **OK**.
4. Click the Plus sign to open the Connector Configuration dialog box.
5. Type a name in the **Connector name** field.
6. If the audit logs are to be fetched from CloudWatch, use the details from the [dynamodbCloudwatch.conf](DynamodbOverCloudwatchPackage/DynamoDB/dynamodbCloudwatch.conf)
   file. If the audit logs are to be fetched from SQS, use the details
   from the [dynamodb_over_sqs.conf](DynamodbOverSQSPackage/DynamoDB/dynamodbOverSqs.conf) file. If the audit logs are to be fetched from CloudTrail, use the details from the [dynamodbCloudtrail.conf](DynamodbOverCloudtrailPackage/DynamoDB/dynamodbCloudtrail.conf) file. Update the input section to add the details from the corresponding file's input part, omitting the keyword "input{" at the beginning and its corresponding "}" at the end. More details on how to configure the relevant input plug-in can be found [here](../../input-plugin/logstash-input-cloudwatch-logs/README.md).
7. If the audit logs are to be fetched from CloudWatch, use the details from the [dynamodbCloudwatch.conf](DynamodbOverCloudwatchPackage/DynamoDB/dynamodbCloudwatch.conf)
   file. But if the audit logs are to be fetched from SQS, use the details
   from the [dynamodbOverSqs.conf](DynamodbOverSQSPackage/DynamoDB/dynamodbOverSqs.conf) file. If the audit logs are to be fetched from CloudTrail, use the details from the [dynamodbCloudtrail.conf](DynamodbOverCloudtrailPackage/DynamoDB/dynamodbCloudtrail.conf) file. Update the filter section to add the details
   from the corresponding file's input part, omitting the keyword "filter{" at the beginning and its corresponding "}"
   at the end.
8. The "type" fields should match in input and filter configuration sections. This field should be unique for every individual connector added.
9. Click **Save**. Guardium validates the new connector, and enables the universal connector if it was
   disabled. After it is validated, it appears in the Configure Universal Connector page.

## Configuring the DynamoDB filters in Guardium Insights

To configure this plug-in for Guardium Insights, follow [this guide.](https://github.com/IBM/universal-connectors/blob/main/docs/UC_Configuration_GI.md)

In the input configuration section, refer to the CloudWatch_logs section.
