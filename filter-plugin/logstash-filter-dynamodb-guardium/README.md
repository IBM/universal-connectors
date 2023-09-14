# Amazon DynamoDB-Guardium Logstash filter plug-in
### Meet DynamoDB
* Tested versions: 2019.11.21
* Environment: AWS
* Supported inputs: CloudWatch (pull)
* Supported Guardium versions:
    * Guardium Data Protection: 11.4 and above
    * Guardium Insights: 3.2
    * Guardium Insights SaaS: 1.0

This is a [Logstash](https://github.com/elastic/logstash) filter plug-in for the universal connector that is featured in IBM Security Guardium. It parses events and messages from the Amazon DynamoDB audit log into a [Guardium record](https://github.com/IBM/universal-connectors/blob/main/common/src/main/java/com/ibm/guardium/universalconnector/commons/structures/Record.java) instance (which is a standard structure made out of several parts). The information is then sent over to Guardium. Guardium records include the accessor (the person who tried to access the data), the session, data, and exceptions. If there are no errors, the data contains details about the query "construct". The construct details the main action (verb) and collections (objects) involved.

The plug-in is free and open-source (Apache 2.0). It can be used as a starting point to develop additional filter plug-ins for Guardium universal connector.



## Enabling audit logs 

There are different ways for auditing and logging. We will use CloudTrail for this example since it supports all required parameters. The following events are supported for auditing in AWS.

### Supported Events

1. UpdateTable event
2. CreateTable event
3. DescribeTable event
4. ListTables event
5. DeleteTable event
6. Error event

### Procedure

1. In Amazon DynamoDB, click **Services**.
2. Go to **All services** > **Management & Governance**.
3. Click **Cloud Trail**.
4. Click **Create trail**.
5. Provide a trail name for **Trail name**.
6. For **Storage location**, verify that **Create new S3 bucket** is selected.
7. For **Log file SSE-KMS encryption**, uncheck **Enabled**.
8. For  **CloudWatch Logs**, check **Enabled**.
9. Verify **New** is selected for **Log group**.
10. For **Log group name**, provide a new log group name.
11. Verify **New** is selected for **IAM Role**.
12. For **Role name**, provide a new role name.
13. Click **Next**.
14. For **Event type**, select **Management events** and **Data events**.
15. Verify that **Read** and **Write** is selected for **API Activity**.
16. In the Data Events section, click **Switch to basic event selectors**.
17. Click **Continue**.
18. Verify that the **Data event source** is **S3**.
19. Uncheck the **Read** and **Write** boxes for **All current and future S3 buckets**.
20. In **Individual bucket selection** > **Browse**, ensure that both **Read** and **Write** boxes are selected.
21. Click **Add data event type**.
22. Click the **data event source** drop-down and select **DynamoDB**.
23. Click **NEXT**.
24. Verify all parameters shown are correct.
25. Click **Create trail**.

## Viewing the logs on CloudWatch

### Procedure

1. Click the **Service** drop-down.
2. In the Recently visited panel, click **CloudWatch**.
3. Click **Logs** > **Log groups**
5. In the search box, enter the name of the log group created in the previous step.
6. Click on the log group that appears in the search.
7. All logs appear under log streams in the format:` < account_i>_CloudTrail_<region >`

### Limitations

1. The DynamoDB plug-in does not support IPV6.

2. You may need to disable management events in order to avoid heavy traffic and data loss in Guardium. Disabling management events disables logging of the following events: CreateTable, DeleteTable, ListTable, UpdateTable, DescribeTable, All Error events.


## 4. Configuring the DynamoDB filters in Guardium

The Guardium universal connector is the Guardium entry point for native audit logs. The universal connector identifies and parses received events, and then converts them to a standard Guardium format. The output of the universal connector is forwarded to the Guardium sniffer on the collector, for policy and auditing enforcements. Configure Guardium to read the native audit logs by customizing the dynamodb template.

### Authorizing outgoing traffic from AWS to Guardium

#### Procedure

1. Log in to the Guardium Collector's APIs.
2. Issue these commands:
		• `grdapi add_domain_to_universal_connector_allowed_domains domain=amazonaws.com`
		• `grdapi add_domain_to_universal_connector_allowed_domains domain=amazon.com`

#### Before you begin

•  Configure the policies you require. See ([policies](/docs/#policies)) for more information.

• You must have permission for the S-Tap Management role. The admin user includes this role by default.

• Download the [dynamodb-offline-plugins-7.5.2.zip](https://github.com/IBM/universal-connectors/raw/release-v1.2.0/filter-plugin/logstash-filter-dynamodb-guardium/DynamodbOverCloudwatchPackage/DynamoDB/dynamodb-offline-plugins-7.5.2.zip) plug-in.This is not necessary for Guardium Data Protection v12.0 and later.



### Procedure

1. On the collector, navigate to **Setup** > **Tools and Views** > **Configure Universal Connector**.
2. Enable the universal collector if it is disabled.
3. Click **Upload File** and select the offline plug-in named [dynamodb-offline-plugins-7.5.2.zip](https://github.com/IBM/universal-connectors/raw/release-v1.2.0/filter-plugin/logstash-filter-dynamodb-guardium/DynamodbOverCloudwatchPackage/DynamoDB/dynamodb-offline-plugins-7.5.2.zip). After it uploads, click **OK**. This is not necessary for Guardium Data Protection v12.0 and later.
4. Click the Plus sign to open the Connector Configuration dialog box
5. Type a name in the **Connector name** field.
6. Update the input section to add the details from the [dynamodbCloudwatch.conf](https://github.com/IBM/universal-connectors/raw/main/filter-plugin/logstash-filter-dynamodb-guardium/dynamodbCloudwatch.conf) file's input part, omitting the keyword "input{" at the beginning and its corresponding "}" at the end
7. Update the filter section to add the details from the [dynamodbCloudwatch.conf](https://github.com/IBM/universal-connectors/raw/main/filter-plugin/logstash-filter-dynamodb-guardium/dynamodbCloudwatch.conf) file's filter part, omitting the keyword "filter{" at the beginning and its corresponding "}" at the end
8. The "type" fields should match in the input and the filter configuration sections. This field should be unique for every individual connector added.
9. Click **Save**. Guardium validates the new connector and displays it in the Configure Universal Connector page. 

## Configuring the dynamoDB filters in Guardium Insights

To configure this plug-in for Guardium Insights, follow [this guide.](/docs/Guardium%20Insights/3.2.x/UC_Configuration_GI.md)

For the input configuration step, refer to the [CloudWatch_logs section](/docs/Guardium%20Insights/3.2.x/UC_Configuration_GI.md#configuring-a-CloudWatch-input-plug-in).
