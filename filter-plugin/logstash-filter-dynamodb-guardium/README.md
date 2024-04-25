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

This is a [Logstash](https://github.com/elastic/logstash) filter plug-in for the universal connector that is featured in IBM Security Guardium. It parses events and messages from the Amazon DynamoDB audit log into a [Guardium record](https://github.com/IBM/universal-connectors/blob/main/common/src/main/java/com/ibm/guardium/universalconnector/commons/structures/Record.java) instance (which is a standard structure made out of several parts). The information is then sent over to Guardium. Guardium records include the accessor (the person who tried to access the data), the session, data, and exceptions. If there are no errors, the data contains details about the query "construct". The construct details the main action (verb) and collections (objects) involved.

The plug-in is free and open-source (Apache 2.0). It can be used as a starting point to develop additional filter plug-ins for Guardium universal connector.


## 1. Configuring Amazon DynamoDB

In the AWS web interface, configure the service for Dynamodb.

### Procedure

1. Go to https://console.aws.amazon.com/
2. Click **Services** in the top left menu.
3. Underneath **All services**, click on **Database**.
4. On the right panel, click **DynamoDB**.
5. At the top right, click on the dropdown menu and select your region.
6. Click the orange **Create Table** button.
7. Enter a table name.
8. Enter a partition key.
9. Scroll down and click the orange **Create table** button.

## 2. Enabling audit logs 

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

• Dynamodb-Guardium Logstash filter plug-in is automatically available with Guardium Data Protection versions 12.x, 11.4 with appliance bundle 11.0p490 or later or Guardium Data Protection version 11.5 with appliance bundle 11.0p540 or later releases.

**Note**: For Guardium Data Protection version 11.4 without appliance bundle 11.0p490 or prior or Guardium Data Protection version 11.5 without appliance bundle 11.0p540 or prior, download the [logstash-filter-dynamodb_guardium_plugin_filter.zip](./logstash-filter-dynamodb_guardium_plugin_filter.zip) plug-in. (Do not unzip the offline-package file throughout the procedure). 


### Procedure
1. On the collector, go to **Setup** > **Tools and Views** > **Configure Universal Connector**.
2. Enable the connector if it is already disabled, before uploading the 
Universal Connector, make sure that it is enabled.
3. Click **Upload File**,
    * If the audit logs are to be fetched from CloudWatch directly, 
      select the [logstash-filter-dynamodb_guardium_plugin_filter.zip](https://github.com/IBM/universal-connectors/releases/download/v1.5.1/logstash-filter-dynamodb_guardium_plugin_filter.zip) plug-in. After it is uploaded, click **OK**.
    * If the audit logs are to be fetched from CloudTrail, select the
      1. [cloudtrail_codec_plugin.zip](./DynamodbOverCloudtrailPackage/DynamoDB/cloudtrail_codec_plugin.zip) plug-in. After it is uploaded, click **OK**. 
       **Note:** This step is not necessary for Guardium Data Protection v11.0p490 or later, v11.0p540 or later, v12.0 or later. 
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
There are different methods for auditing and logging. We will use CloudTrail for this example since it supports all required parameters. The following events are supported for auditing in AWS.


### Procedure

1. Click **Services** in the top left menu.
2. Underneath **All services**, click on **Management & Governance**.
3. On the right panel, click **Cloud Trail**.
4. Click **Create trail** button.
5. Provide a trail name under **Trail name**.
6. Under **Storage location**, verify that **Create new S3 bucket** is selected.
7. Under **Log file SSE-KMS encryption**, clear the Enabled box.
8. If the logs are to be monitored through CloudWatch, then forward them to Cloudwatch using steps 9 to 13. (If not, skip those steps).
9. Under **CloudWatch Logs**, check the **Enabled** box.
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

## Follow the below link if DynamoDB is to be monitored using Cloudwatch

[DynamoDB Over Cloudwatch](DynamodbOverCloudwatch/README.md)

## Follow the below link if DynamoDB is to be monitored using Cloudtrail

[DynamoDB Over Cloudtrail](DynamodbOverCloudtrail/README.md)

### Limitations

1. The Dynamo DB plug-in does not support IPV6.
2. You may need to disable management events in order to avoid heavy traffic and data loss in Guardium. Disabling management events disables logging of the following events: 
CreateTable, DeleteTable, ListTable, UpdateTable, DescribeTable events.
3. The following fields couldn't be mapped with the Dynamo audit logs,
   1. Client HostName : Not available with audit logs so set as NA.
