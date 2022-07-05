## Dynamodb-Guardium Logstash filter plug-in

This is a [Logstash](https://github.com/elastic/logstash) filter plug-in for the univer***REMOVED***l connector that is featured in IBM Security Guardium. It parses events and mes***REMOVED***ges from the Amazon DynamoDB audit log into a Guardium record instance (which is a standard structure made out of several parts). The information is then sent over to Guardium. Guardium records include the accessor (the person who tried to access the data), the session, data, and exceptions. If there are no errors, the data contains details about the query "construct". The contstruct details the main action (verb) and collections (objects) involved. 

The plug-in is free and open-source (Apache 2.0). It can be used as a starting point to develop additional filter plug-ins for Guardium univer***REMOVED***l connector.

## Supported Events

    1. UpdateTable event
    2. CreateTable event
    3. DescribeTable event
    4. ListTables event
    5. DeleteTable event
    6. Error event

## Limitations

	The dynamodb plug-in does not support IPV6.


## Configuring Amazon DynamoDB and sending logs to CloudWatch

# To authorize outgoing traffic from Amazone Web Services (AWS) to Guardium, run these API:

	grdapi add_domain_to_univer***REMOVED***l_connector_allowed_domains domain=amazonaws.com
	grdapi add_domain_to_univer***REMOVED***l_connector_allowed_domains domain=amazon.com


In the AWS web interface, configure the service for Dynamodb.

## Procedure

	1. Go to https://console.aws.amazon.com/:

		a) Click Services in the top left menu.
		b) In the Database section, click DynamoDB.
		c) Select the required region in the top right corner.
		d) Click Create Table.
		e) Provide the Table name and Primary key.
		f) Click Create.

## Enable logging through CloudTrail

There are different ways for auditing and logging. We will use CloudTrail for this example since it supports all required parameters. 

## Procedure

    1. Click Services in the top left menu.
    2. In the Storage section, select S3.
    3. Create an S3 bucket.
    4. Select the appropriate region.
    5. In Management & Governance, select CloudTrail.
    6. Click Create Trail.
    7. Enter the trail name.
    8. For Storage location, choose Create new S3 bucket to create a bucket.
    9. Deselect the check box to di***REMOVED***ble Log file SSE-KMS encryption.
    10. Enable CloudWatch logs.
    11. Create a new Log group and IAM role.
    12. Click Next.
    13. For Choose log event, select Data events and Management events.
    14. In the Management events section, ensure that Read and Write are selected.
    15. Deselect the Read and Write option for All current and future S3 buckets.
    16. Browse the created S3 bucket - and then enable Read and Write options.
    17. Preview the details provided and then click Create Trail.


## View the logs entries on CloudWatch

## Procedure

	1. Click the Services drop down.
    2. Enter the CloudWatch string in the search box.
    3. Click CloudWatch to redirect to the CloudWatch dashboard.
    4. In the left pane, select Logs.
    5. Click Log Groups.
    6. Search for the log group that you created in the previous step.
    7. In the log group, locate the <account_id>CloudTrail_<region> file. All events are logged to this file.


## Configuring the dynamodb filters in Guardium

The Guardium univer***REMOVED***l connector is the Guardium entry point for native audit logs. The univer***REMOVED***l connector identifies and parses received events, and then converts them to a standard Guardium format. The output of the univer***REMOVED***l connector is forwarded to the Guardium sniffer on the collector, for policy and auditing enforcements. Configure Guardium to read the native audit logs by customizing the dynamodb template.

## Before you begin

	• You must have permission for the S-Tap Management role. The admin user has this role by default.
	• Download the dynamodb-offline-plugins-7.5.2.zip plug-in.
	• Verify whether you have cloudwatch_logs input plugin available using following command : 
		su - cli
		grdapi show_univer***REMOVED***l_connector_plugins

# Procedure

	1. On the collector, go to Setup > Tools and Views > Configure Univer***REMOVED***l Connector.
	2. Click Upload File and select the offline dynamodb-offline-plugins-7.5.2.zip plug-in. After it is uploaded, click OK.
	3. Click the Plus sign to open the Connector Configuration dialog box.
	4. Type a name in the Connector name field.
	5. Update the input section to add the details from dynamodbCloudwatch.conf file's input part, omitting the keyword "input{" at the beginning and its corresponding "}" at the end.
	6. Update the filter section to add the details from dynamodbCloudwatch.conf file's filter part, omitting the keyword "filter{" at the beginning and its corresponding "}" at the end.
	7. Click Save. Guardium validates the new connector, and enables the univer***REMOVED***l connector if it was di***REMOVED***bled. After it is validated, it appears in the Configure Univer***REMOVED***l Connector page.

## Configuring the dynamodb filters in Guardium Insights

To configure this plug-in for Guardium Insights, follow [this guide.](https://github.com/RefaelAdi/univer***REMOVED***l-connectors/blob/INS-18044/docs/UC_Configuration_GI.md#Configuring_Filebeat_to_forward_audit_logs_to_Guardium)

In the input configuration section, refer to the CloudWatch_logs section.