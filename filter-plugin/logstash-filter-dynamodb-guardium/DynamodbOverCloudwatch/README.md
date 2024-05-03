# DynamodbOverCloudwatch-Guardium Logstash filter plug-in

## 1. Viewing the logs on CloudWatch

### Procedure

1. Click the Service drop down.
2. On the `Recently visited` panel, click **CloudWatch**.
3. On the left panel, click **Log**`.
4. Click on **Log groups** under Logs.
5. In the search box, enter the name of the log group that you created in the previous STEP 9.
6. Click on the log group that appears in the search.
7. All logs display under log streams in the format: <account_id>_CloudTrail_<region>

## NOTE

In order to achieve load balancing of audit logs between different collectors, the audit logs must be exported from
Cloudwatch to SQS. Follow step #2 to publish logs to SQS else move to step #3.

## 2. Exporting Cloudwatch Logs to SQS using lambda function

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
Perform the following steps for the IAM user who is accessing the SQS logs in Guardium:
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
10. Add the function code from the file [lambda.py](DynamodbCloudwatchOverSQSPackage/lambda.py).
11. Click **Configuration** > **Environment Variables**.
12. Create 2 variables:
    1. `Key = GROUP_NAME value = <value>` e.g., `uc-dynamo-plugin-cloudwatch-group`
    2. `Key = QUEUE_NAME value = <value>` e.g., `https://sqs.ap-south-1.amazonaws.com/1111111111/np-dynamo-sqs`
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

## 3. Configuring the Dynamodb filters in Guardium

The Guardium universal connector is the Guardium entry point for native audit logs. The universal connector identifies and parses received events, and then converts them to a standard Guardium format. The output of the universal connector is forwarded to the Guardium sniffer on the collector, for policy and auditing enforcements. Configure Guardium to read the native audit logs by customizing the dynamodb template.

### Authorizing outgoing traffic from AWS to Guardium

#### Procedure

1. Log in to the Guardium Collector's APIs.
2. Issue the following commands:
```
grdapi add_domain_to_universal_connector_allowed_domains domain=amazonaws.com
grdapi add_domain_to_universal_connector_allowed_domains domain=amazon.com
```

#### Before you begin
• Configure the policies you require. See [policies](https://github.com/IBM/universal-connectors/tree/main/docs#policies) for more information.

• You must have permission for the S-Tap Management role. The admin user includes this role by default.

• DynamodbOverCloudwatch-Guardium Logstash filter plug-in is automatically available with Guardium Data Protection versions 12.x, 11.4 with appliance bundle 11.0p490 or later or Guardium Data Protection version 11.5 with appliance bundle 11.0p540 or later releases.

• For Guardium Data Protection version 11.0p540 and/or 11.0p6505 download the [cloudwatch_logs plug-in](../../../input-plugin/logstash-input-cloudwatch-logs/CloudwatchLogsInputPackage/offline-logstash-input-cloudwatch_log_1_0_5.zip)

**Note:** For Guardium Data Protection version 11.4 without appliance bundle 11.0p490 or prior or Guardium Data Protection version 11.5 without appliance bundle 11.0p540 or prior,download the [logstash-filter-dynamodb_guardium_plugin_filter.zip](../logstash-filter-dynamodb_guardium_plugin_filter.zip) plug-in.

### Procedure
1. On the collector, go to **Setup** > **Tools and Views** > **Configure Universal Connector**.
2. Enable the connector if it is already disabled, before proceeding to upload the UC.
3. Click **Upload File**, 
	* Select [logstash-filter-dynamodb_guardium_plugin_filter.zip](../logstash-filter-dynamodb_guardium_plugin_filter.zip) plug-in. After it is uploaded, click **OK**. This is not necessary for Guardium Data Protection v11.0p490 or later, v11.0p540 or later, v12.0 or later.
   * If you have installed Guardium Data Protection version 11.0p540 and/or 11.0p6505,  select the offline [cloudwatch_logs plug-in](../../../input-plugin/logstash-input-cloudwatch-logs/CloudwatchLogsInputPackage/offline-logstash-input-cloudwatch_log_1_0_5.zip). After it is uploaded, click **OK**.
4. Click the Plus sign to open the Connector Configuration dialog box.
5. Type a name in the **Connector name** field.
6. If the audit logs are to be fetched from CloudWatch, use the details from the [dynamodbCloudwatch.conf](DynamodbOverCloudwatchPackage/DynamoDB/dynamodbCloudwatch.conf)
   file. If the audit logs are to be fetched from SQS, use the details
   from the [dynamodb_over_sqs.conf](DynamodbCloudwatchOverSQSPackage/DynamoDB/dynamodbOverSqs.conf) file. Update the input section to add the details from the corresponding file's input part, omitting the keyword "input{" at the beginning and its corresponding "}" at the end. More details on how to configure the relevant input plug-in can be found [cloudwatch_logs](../../../input-plugin/logstash-input-cloudwatch-logs/README.md) and [sqs](../../../input-plugin/logstash-input-sqs/README.md)
7. If the audit logs are to be fetched from CloudWatch, use the details from the [dynamodbCloudwatch.conf](DynamodbOverCloudwatchPackage/DynamoDB/dynamodbCloudwatch.conf)
   file. But if the audit logs are to be fetched from SQS, use the details
   from the [dynamodbOverSqs.conf](DynamodbCloudwatchOverSQSPackage/DynamoDB/dynamodbOverSqs.conf) file. Update the filter section to add the details
   from the corresponding file's input part, omitting the keyword "filter{" at the beginning and its corresponding "}"
   at the end.
8. The "type" fields should match in input and filter configuration sections. This field should be unique for every individual connector added.
9. Click **Save**. Guardium validates the new connector, and enables the universal connector if it was
   disabled. After it is validated, it appears in the Configure Universal Connector page.


## Configuring the dynamodb filters in Guardium Insights

Depending on your environment, see the instructions for configuring the DynamoDB filters in one of the following 
locations,

* Guardium Insights SaaS, follow [this guide](https://github.com/IBM/universal-connectors/blob/main/docs/Guardium%20Insights/SaaS_1.0/UC_Configuration_GI.md).
* Guardium Insights on-premises, follow [this guide](https://github.com/IBM/universal-connectors/blob/main/docs/Guardium%20Insights/3.2.x/UC_Configuration_GI.md).

In the input configuration section, refer to the CloudWatch_logs section.
