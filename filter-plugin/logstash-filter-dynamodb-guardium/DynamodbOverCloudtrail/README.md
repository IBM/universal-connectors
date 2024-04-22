# DynamodbOverCloudtrail-Guardium Logstash filter plug-in

## 1. Viewing the logs on CloudTrail

### Procedure

1. Click the Service drop down.
2. On the `Recently visited` panel, click **CloudTrail**.
3. On the left panel, click **Trails**.
4. Click on **Bucket Name** under the Trail that was created above.
5. Traverse through the folders of the S3 bucket.
7. Log files will be displayed as .json.gz files.
8. The log file can be then opened in the browser in readable format or downloaded as per the requirement.

## NOTE

In order to achieve load balancing of audit logs between different collectors, the audit logs must be exported from CloudTrail to SQS by using the EventBridge Rule. Follow step #2 to publish logs to SQS else move to step #3.

## 2. Exporting CloudTrail Logs to SQS

### Creating the SQS queue
The SQS created in these steps will contain the messages to be filled up by the
EventBridge Rule (created in next section) in the queue by reading the CloudTrail logs.
The messages inside the SQS will contain content from CloudTrail logs.

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


### Creating the EventBridge Rule to forward logs from CloudTrail to SQS

The creation of a new event in a CloudTrail will trigger the EventBridge Rule to activate. It will only send the events related to DynamoDB to the SQS queue after filtering out AWS API Activity Events.

Follow the steps below to configure the EventBridge Rule

#### Create the EventBridge Rule
__*Procedure*__
1. Go to https://console.aws.amazon.com/
2. Go to **Services** and search for **Amazon EventBridge**.
3. Click **Rules** under the **Buses** option in the left panel.
4. Click **Create Rule**.
5. Enter **Rule name** and **Rule Description**.
6. In the **Rule Type** field, select **Rule with an Event Pattern**.
7. Click **Next**.
8. In the **Event Source** fiel, select **Other**.
9. In the **Creation Method** field, select **Custom Pattern Json Editor**.
10. In the **Event Pattern** field, add the following pattern.

```
{
  "detail-type": ["AWS API Call via CloudTrail"],
  "detail": {
    "eventSource": ["dynamodb.amazonaws.com"]
  }
}
```
12. Click **Next**.
13. In the **Select Targets** field, select **Target Types** as **AWS Service**.
14. In the **Select Target** list, select **SQS Queue**.
15. In the **Queue** field, select the queue name from the list. This is the same queue that you created in the previous steps.
16. Click **Create Rule**.
17. If the rule is currently disabled, enable it.

### NOTE 

Create an EventBridge Rule and Queue for each region where you wish to monitor the Dynamo DB tables, as they may be present in various regions. Additionally, configure one Universal Connector in Guardium for every Queue.

### Limitations

1. If Dynamo DB is monitored directly through CloudTrail using S3 input plugin, then it will pull all the existing older logs that are present in the bucket. There is no provision to set index to pull only the latest logs


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

• Download the [cloudtrail_codec_plugin.zip](DynamodbOverCloudtrailPackage/DynamoDB/cloudtrail_codec_plugin.zip) plug-in, if the logs are to be pulled directly from CloudTrail.

• Download the [logstash-filter-dynamodb_guardium_plugin_filter.zip plug-in.](../logstash-filter-dynamodb_guardium_plugin_filter.zip)

### Procedure
1. On the collector, go to **Setup** > **Tools and Views** > **Configure Universal Connector**.
2. Enable the connector if it is already disabled, before proceeding to upload the UC.
3. Click **Upload File**, 
	* If the audit logs are to be fetched from CloudTrail, select the
      1. [cloudtrail_codec_plugin.zip](./DynamodbOverCloudtrailPackage/DynamoDB/cloudtrail_codec_plugin.zip) plug-in. After it is uploaded, click **OK**. This is specifically for CloudTrail only.
	* Select [logstash-filter-dynamodb_guardium_plugin_filter.zip](../logstash-filter-dynamodb_guardium_plugin_filter.zip) plug-in. After it is uploaded, click **OK**.
4. Click the Plus sign to open the Connector Configuration dialog box.
5. Type a name in the **Connector name** field.
6. If the audit logs are to be fetched from CloudTrail, use the details from the [dynamodbCloudtrail.conf](./DynamodbOverCloudtrailPackage/DynamoDB/dynamodbCloudtrail.conf) file. If the audit logs are to be fetched from SQS, use the details
   from the [dynamodb_over_sqs.conf](./DynamodbCloudtrailOverSQSPackage/DynamoDB/dynamodbOverSqs.conf) file. Update the input section to add the details from the corresponding file's input part, omitting the keyword "input{" at the beginning and its corresponding "}" at the end. More details on how to configure the relevant input plug-in can be found [cloudTrail_input](../../../input-plugin/logstash-input-s3/README.md) or [sqs](../../../input-plugin/logstash-input-sqs/README.md)
7. If the audit logs are to be fetched from CloudTrail, use the details from the [dynamodbCloudtrail.conf](./DynamodbOverCloudtrailPackage/DynamoDB/dynamodbCloudtrail.conf) file. If the audit logs are to be fetched from SQS, use the details
   from the [dynamodb_over_sqs.conf](./DynamodbCloudtrailOverSQSPackage/DynamoDB/dynamodbOverSqs.conf) file. Update the filter section to add the details
   from the corresponding file's input part, omitting the keyword "filter{" at the beginning and its corresponding "}"
   at the end.
8. The "type" fields should match in input and filter configuration sections. This field should be unique for every individual connector added.
9. Click **Save**. Guardium validates the new connector, and enables the universal connector if it was
   disabled. After it is validated, it appears in the Configure Universal Connector page.