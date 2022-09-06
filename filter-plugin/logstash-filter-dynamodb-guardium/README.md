## Dynamodb-Guardium Logstash filter plug-in

This is a [Logstash](https://github.com/elastic/logstash) filter plug-in for the universal connector that is featured in IBM Security Guardium. It parses events and messages from the Amazon DynamoDB audit log into a Guardium record instance (which is a standard structure made out of several parts). The information is then sent over to Guardium. Guardium records include the accessor (the person who tried to access the data), the session, data, and exceptions. If there are no errors, the data contains details about the query "construct". The contstruct details the main action (verb) and collections (objects) involved. 

The plug-in is free and open-source (Apache 2.0). It can be used as a starting point to develop additional filter plug-ins for Guardium universal connector.

## Supported Events

    1. UpdateTable event
    2. CreateTable event
    3. DescribeTable event
    4. ListTables event
    5. DeleteTable event
    6. Error event

## Configuring Amazon DynamoDB and sending logs to CloudWatch

# To authorize outgoing traffic from Amazon Web Services (AWS) to Guardium, run these API:

	grdapi add_domain_to_universal_connector_allowed_domains domain=amazonaws.com
	grdapi add_domain_to_universal_connector_allowed_domains domain=amazon.com


In the AWS web interface, configure the service for Dynamodb.

## Procedure

	1. Go to https://console.aws.amazon.com/:

		a) Click <Services> in the top left menu.
		b) Underneath <All services>, click on <Database>
		c) On the right panel, click <DynamoDB>
		d) At the top right, click on the drop down menu and select your region (??)
		e) Click the orange <Create Table> button.
		f) Enter a table name
		g) Enter a partition key
		h) Scroll down and click the orange <Create table> button

## Enable logging through CloudTrail

There are different ways for auditing and logging. We will use CloudTrail for this example since it supports all required parameters. 

## Procedure

    1. Click Services in the top left menu.
	2. Underneath <All services>, click on <Management & Governance>
	3. On the right panel, click <Cloud Trail>
	4. Click <Create trail> button
	5. Provide a trail name under <Trail name>
	6. Under <Storage location>, verify "Create new S3 bucket" is selected
	7. Under <Log file SSE-KMS encryption>, uncheck the Enabled box
	8. Under <CloudWatch Logs>, check the Enabled box
	9. Verify (x)New is selected under <Log group>
	10. Under <Log group name>, provide a new log group name
	11. Verify (x)New is selected under <IAM Role>
	12 Under <Role name>, provide a new role name
	13. Click the orange [Next] button at the lower right
	14. Under Event type, select [x] Management events and [x] Data events
	15. Verify [x]Read and [x]Write is selected under API Activity
	16. In the <Data Events> section, click the [Switch to basic event selectors] button
	17. Click [Continue] to confirm button.
	18. Verify <Data event source> is S3
	19. Uncheck the []Read and []Write boxes next to <All current and future S3 buckets>
	20. Next to the [Browse] button under <Individual bucket selection>, ensure that both Read and Write boxes are selected
	21. Click [Add data event type] button
	22. Click the <Data event source> dropdown and select "DynamoDB"
	23. Click the orange [NEXT] button
	24. Verify all parameters shown are correct.
	25. Click the orange [Create trail] button

## View the logs entries on CloudWatch

## Procedure

	1. Click the Service drop down.
	2. On right <Recently visited> panel, click on CloudWatch
	3. On the left panel, click on Logs
	4. Click on <Log groups> under Logs
	5. In the search box, enter the name of the log group created in the previous STEP 9.
	6. Click on the log group that appeared from the search
	7. All logs will appear under log streams in the format: <account_i>_CloudTrail_<region>


## Configuring the dynamodb filters in Guardium

The Guardium universal connector is the Guardium entry point for native audit logs. The universal connector identifies and parses received events, and then converts them to a standard Guardium format. The output of the universal connector is forwarded to the Guardium sniffer on the collector, for policy and auditing enforcements. Configure Guardium to read the native audit logs by customizing the dynamodb template.

## Before you begin

	• You must have permission for the S-Tap Management role. The admin user has this role by default.
	• Download the dynamodb-offline-plugins-7.5.2.zip plug-in.
	• Verify whether you have cloudwatch_logs input plugin available using following command : 
		su - cli
		grdapi show_universal_connector_plugins

# Procedure

	1. On the collector, navigate to Setup > Tools and Views > Configure Universal Connector
	2. First Enable the Universal Guardium connector, if it is Disabled already.
	3. Click the [Upload File] button
	4. Select the downloaded file "dynamodb-offline-plugins-7.5.2.zip"
	5. Click [Upload/install]
	6. Click [OK] on the upload confirmation
	7. Click the Plus sign to open the Connector Configuration dialog box.
	8. Type a name in the Connector name field.
	9. Update the input section to add the details from dynamodbCloudwatch.conf file's input part, omitting the keyword "input{" at the beginning and its corresponding "}" at the end.
	10. Update the filter section to add the details from dynamodbCloudwatch.conf file's filter part, omitting the keyword "filter{" at the beginning and its corresponding "}" at the end.
	11. "type" field should match in input and filter configuration section. This field should be unique for every individual connector added.
	12. Click Save. Guardium validates the new connector, and enables the universal connector if it was disabled. After it is validated, it appears in the Configure Universal Connector page.
	
## Limitations

	The dynamodb plug-in does not support IPV6.

## Configuring the dynamodb filters in Guardium Insights

To configure this plug-in for Guardium Insights, follow [this guide.](https://github.com/IBM/universal-connectors/blob/main/docs/UC_Configuration_GI.md)

In the input configuration section, refer to the CloudWatch_logs section.