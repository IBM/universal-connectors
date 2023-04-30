# Amazon DynamoDB-Guardium Logstash filter plug-in
### Meet DynamoDB
* Tested versions: 2019.11.21
* Environment: AWS
* Supported inputs: CloudWatch (pull)
* Supported Guardium versions:
    * Guardium Data Protection: 11.4 and above
    * Guardium Insights: 3.2 and above

This is a [Logstash](https://github.com/elastic/logstash) filter plug-in for the universal connector that is featured in IBM Security Guardium. It parses events and messages from the Amazon DynamoDB audit log into a [Guardium record](https://github.com/IBM/universal-connectors/blob/main/common/src/main/java/com/ibm/guardium/universalconnector/commons/structures/Record.java) instance (which is a standard structure made out of several parts). The information is then sent over to Guardium. Guardium records include the accessor (the person who tried to access the data), the session, data, and exceptions. If there are no errors, the data contains details about the query "construct". The construct details the main action (verb) and collections (objects) involved.

The plug-in is free and open-source (Apache 2.0). It can be used as a starting point to develop additional filter plug-ins for Guardium universal connector.

## 1. Configuring Amazon DynamoDB

In the AWS web interface, configure the service for Dynamodb.

### Procedure

1. Go to https://console.aws.amazon.com/:

	a) Click <Services> in the top left menu.
	
	b) Underneath <All services>, click on <Database>
	
	c) On the right panel, click <DynamoDB>
	
	d) At the top right, click on the drop down menu and select your region 
	
	e) Click the orange <Create Table> button.
	
	f) Enter a table name
	
	g) Enter a partition key
	
	h) Scroll down and click the orange <Create table> button

## 2. Enabling audit logs 

There are different ways for auditing and logging. We will use CloudTrail for this example since it supports all required parameters. The below events are supported for auditing in AWS

### Supported Events

1. UpdateTable event
2. CreateTable event
3. DescribeTable event
4. ListTables event
5. DeleteTable event
6. Error event

### Procedure

1. Click Services in the top left menu.
2. Underneath All services, click on <Management & Governance>
3. On the right panel, click Cloud Trail
4. Click Create trail button
5. Provide a trail name under Trail name
6. Under Storage location, verify "Create new S3 bucket" is selected
7. Under Log file SSE-KMS encryption, uncheck the Enabled box
8. Under CloudWatch Logs, check the Enabled box
9. Verify (x)New is selected under <Log group>
10. Under Log group name, provide a new log group name
11. Verify (x)New is selected under <IAM Role>
12 Under Role name, provide a new role name
13. Click the orange [Next] button at the lower right
14. Under Event type, select [x] Management events and [x] Data events
15. Verify [x]Read and [x]Write is selected under API Activity
16. In the Data Events section, click the [Switch to basic event selectors] button
17. Click [Continue] to confirm button.
18. Verify Data event source is S3
19. Uncheck the []Read and []Write boxes next to <All current and future S3 buckets>
20. Next to the [Browse] button under <Individual bucket selection>, ensure that both Read and Write boxes are selected
21. Click [Add data event type] button
22. Click the data event source dropdown and select "DynamoDB"
23. Click the orange [NEXT] button
24. Verify all parameters shown are correct.
25. Click the orange [Create trail] button

## 3. Viewing the logs on CloudWatch

### Procedure

1. Click the Service drop down.
2. On right Recently visited panel, click on CloudWatch
3. On the left panel, click on Logs
4. Click on Log groups under Logs
5. In the search box, enter the name of the log group created in the previous STEP 9.
6. Click on the log group that appeared from the search
7. All logs will appear under log streams in the format: < account_i>_CloudTrail_<region >

### Limitations

The dynamodb plug-in does not support IPV6.

## 4. Configuring the Dynamodb filters in Guardium

The Guardium universal connector is the Guardium entry point for native audit logs. The universal connector identifies and parses received events, and then converts them to a standard Guardium format. The output of the universal connector is forwarded to the Guardium sniffer on the collector, for policy and auditing enforcements. Configure Guardium to read the native audit logs by customizing the dynamodb template.

### Authorizing outgoing traffic from AWS to Guardium

#### Procedure

1. Log in to the Guardium Collector's APIs.
2. Issue these commands:
		• grdapi add_domain_to_universal_connector_allowed_domains domain=amazonaws.com
		• grdapi add_domain_to_universal_connector_allowed_domains domain=amazon.com
#### Before you begin

•  Configure the policies you require. See ([policies](/../../#policies)) for more information.


• You must have permission for the S-Tap Management role. The admin user includes this role by default.

• Download the [dynamodb-offline-plugins-7.5.2.zip plug-in.](../../filter-plugin/logstash-filter-dynamodb-guardium/DynamodbOverCloudwatchPackage/DynamoDB/dynamodb-offline-plugins-7.5.2.zip)


### Procedure

1. On the collector, navigate to Setup > Tools and Views > Configure Universal Connector
2. First enable the Universal Guardium connector, if it is disabled already
3. Click Upload File and select the offline plug-in named [dynamodb-offline-plugins-7.5.2.zip plug-in.](https://github.com/IBM/universal-connectors/blob/main/filter-plugin/logstash-filter-dynamodb-guardium/DynamodbOverCloudwatchPackage/DynamoDB/dynamodb-offline-plugins-7.5.2.zip). After it is uploaded, click OK.
4. Click the Plus sign to open the Connector Configuration dialog box
5. Type a name in the Connector name field.
6. Update the input section to add the details from the [dynamodbCloudwatch.conf](https://github.com/IBM/universal-connectors/blob/main/filter-plugin/logstash-filter-dynamodb-guardium/dynamodbCloudwatch.conf) file's input part, omitting the keyword "input{" at the beginning and its corresponding "}" at the end
7. Update the filter section to add the details from the [dynamodbCloudwatch.conf](https://github.com/IBM/universal-connectors/blob/main/filter-plugin/logstash-filter-dynamodb-guardium/dynamodbCloudwatch.conf) file's filter part, omitting the keyword "filter{" at the beginning and its corresponding "}" at the end
8. The "type" fields should match in the input and the filter configuration sections. This field should be unique for every individual connector added.
9. Click Save. Guardium validates the new connector, and enables the universal connector if it was disabled. After it is validated, it appears in the Configure Universal Connector page

## Configuring the dynamodb filters in Guardium Insights

To configure this plug-in for Guardium Insights, follow [this guide.](/docs/Guardium%20Insights/3.2.x/UC_Configuration_GI.m)

For the input configuration step, refer to the [CloudWatch_logs section](/docs/Guardium%20Insights/3.2.x/UC_Configuration_GI.md#configuring-a-CloudWatch-input-plug-in).