# Universal Connector for AWS

## Configuring AWS DynamoDB and Sending Logs to CloudWatch

### Before You Begin:

To authorize outgoing traffic from Amazon Web Services (AWS) to Guardium, run these APIs:
```
grdapi add_domain_to_universal_connector_allowed_domains domain=amazonaws.com
grdapi add_domain_to_universal_connector_allowed_domains domain=amazon.com
```
**Note: The DynamoDB plug-in does not support IPv6.**

### Procedure:

To configure DynamoDB and send logs to CloudWatch, follow these steps:

 - Configure the service for DynamoDB in AWS

 - Enable logging through CloudTrail

 - View the log entries on CloudWatch

 - Configure the DynamoDB filters in Guardium

#### Configure the Service for DynamoDB in AWS

1.	Go to https://console.aws.amazon.com/.
2.	Click ```Services``` in the top left.
3.	In the ```Database``` section, click ```DynamoDB```.
4.	Select the appropriate region in the top right corner.
5.	Click ```Create Table```.
6.	Provide the Table name and Primary key.
7.	Click ```Create```.

#### Enable Logging Through CloudTrail

There are different methods for auditing and logging. We will use CloudTrail for these instructions, since it supports all required parameters.

1.	Click ```Services``` in the top left.
2.	In the Storage container, select S3.
3.	Create an S3 bucket.
4.	Select the appropriate region.
5.	In ```Management & Governance```, select ```CloudTrail```.
6.	Click ```Create Trail```.
7.	Enter the trail name.
8.	For Storage location, choose ```Create new S3 bucket```.
9.	Deselect the checkbox to disable Log file SSE-KMS encryption.
10.	Enable CloudWatch logs.
11.	Create a new Log group and IAM role.
12.	Click Next.
13.	For ```Choose log event```, select ```Data events``` and ```Management events```.
14.	In the ```Management events``` section, ensure that read and write are selected.
15.	Deselect the Read and Write options for All current and future S3 buckets.
16.	Browse the created S3 bucket - and then enable read and write options.
17.	Preview the details provided and then click Create Trail.

#### View the Log Entries on CloudWatch

1.	Click the ```Services``` drop-down menu.
2.	Enter CloudWatch in the search box.
3.	Click ```CloudWatch``` to redirect to the CloudWatch dashboard.
4.	In the left pane, select ```Logs```.
5.	Click ```Log Groups```.
6.	Search for the log group that you created in the previous step.
7.	In the log group, locate the <account id>CloudTrail_<region> file. All events are logged to this file.

#### Configure the DynamoDB Filters in Guardium

##### Before You Begin

You must have permission for the S-Tap Management role. The admin user has this role by default.

#### About This Task

The Guardium Universal Connector is the Guardium entry point for native audit logs. The Universal Connector identifies and parses received events, and then converts them to a standard Guardium format. The output of the Universal Connector is forwarded to the Guardium sniffer on the collector, for policy and auditing enforcements. Configure Guardium to read the native audit logs by customizing the DynamoDB template.

Supported Events:

- UpdateTable event
- CreateTable event
- DescribeTable event
- ListTables event
- DeleteTable event
- Error event

##### Procedure

1.	On the collector, go to ```Setup``` > ```Tools and Views``` > ```Configure Universal Connector``` to open the Connector Configuration dialog box.
2.	From the Connector template menu, select the offline logstash-offline-plugins-7.5.2.zip plug-in.
3.	Type a name in the Connector name field.
4.	Modify the input section to look like this :

	      cloudwatch_logs {
				#Mandatory arguments:
				log_group => ["aws-dynamodb-logs"]
				access_key_id => "<insert_access_key>"
				secret_access_key => "<insert_access_secret"
			region => "ap-south-1" #Default value: us-east-1
				start_position => "end"
				interval => 5
				event_filter => ""
				type => "Dynamodb"
		      }
		
where log_group is the log group that is created for the data instance (for example, ```"aws-dynamodb-logs"```).

5.	Update the filter section to look like this:
					
       if [type] == "Dynamodb" {
			json {
					source => "message"
					target => "parsed_json"
				}
				mutate {
					add_field => {
						"new_event_source" => "%{[parsed_json][eventSource]}"
					}
				}
				if [new_event_source] {
					if[new_event_source] =~ "dynamodb.amazonaws.com" {
						dynamodb_guardium_plugin_filter {}
					}
					else {
 					drop {}
					}
				}

				mutate { remove_field => [ "parsed_json", "new_event_source", "message", "cloudwatch_logs", "@timestamp", "@version", "type", "host", "sequence" ] }
			}

	IMPORTANT: Delete the lines:  
			output {
				stdout { codec => rubydebug }
		}


6. Click Save. Guardium validates the new connector, and enables the universal connector if it was disabled. After it is validated, the connector appears in the Configure Universal Connector page.
 
## Configuring Amazon S3 Auditing with CloudWatch

CloudTrail monitors S3 activity in your Amazon account. Configure CloudTrail with CloudWatch so that Guardium can pull the information from CloudWatch into the Guardium collector and analyze it.

### About This Task
Configuring Amazon S3 auditing to send files to CloudWatch (and then on to the Guardium Universal Connector) using CloudTrail links your Amazon S3 or AWS account with Guardium. Begin by turning on your Universal Connector, then complete the steps in this procedure.

#### Procedure

1.	Complete the procedure below for **Configuring Amazon AWS CloudTrail to Send Log Files to CloudWatch**. This procedure is for all users, so Guardium can monitor events from their Amazon account.

2.	Then, follow one of the procedures listed below. Choose the one that describes your current account setup.

- If you want to connect to CloudWatch directly from Guardium outside AWS, follow this procedure: **Configuring Security Credentials for Your AWS User Account**.

- If you access CloudWatch from the same AWS account as the EC2 hosting Guardium, follow this procedure: **Configuring IAM Role for CloudWatch Integration**.

- If you access CloudWatch from a separate AWS account, follow this procedure: **Configuring AWS Security Credentials for Cross-Account CloudWatch Integration That Uses a Configuration with role_arn**.
 
### Configuring Amazon AWS CloudTrail to Send Log Files to CloudWatch

#### Before You Begin

In the Amazon UI, configure CloudTrail to pull the native audit logs, and to create JSON files in CloudWatch.

### Procedure
1.	Go to https://console.aws.amazon.com/cloudtrail

a.	Click ```Trails``` in the left menu.

b.	Click ``Create trail``` and enter the trail name.

c.	In the ```Data events``` section, select the S3 tab and specify the names of the buckets you want to audit.

d.	In the ```Storage location``` section, select the s3 bucket name that hosts the logs, and click ```Create```.

2.	Send CloudTrail Events to CloudWatch.

a.	Click the trail.

b.	Go to the CloudWatch Logs section and click ```Configure```.

c.	Select the name of the log group, either new or existing, and click ```Continue```.

d.	On the next page, click ```Allow```.

For more details, see [Creating a Trail](https://docs.aws.amazon.com/awscloudtrail/latest/userguide/cloudtrail-create-a-trail-using-the-console-first-time.html) and [Logs](https://docs.aws.amazon.com/awscloudtrail/latest/userguide/send-cloudtrail-events-to-cloudwatch-logs.html).

[Related information](https://docs.aws.amazon.com/awscloudtrail/latest/userguide/cloudtrail-create-a-trail-using-the-console-first-time.html)

### Configuring Security Credentials for Your AWS User Account

#### Before You Begin

You must have your AWS user account access key and the secret access key values before you can configure a log source in Universal Connector.

#### Procedure
1.	Log in to your IAM console
2.	Select ```Users``` from the left navigation panel and then select your user name from the list.
3.	Click the ```Security Credentials``` tab.
4.	In the ```Access Keys``` section, click the ```Create access``` key.
5.	From the window that displays after the access key and corresponding secret access key are created, download the .csv file that contains the keys or copy and save the keys.

***Note: Save the Access key ID and Secret access key and use them when you configure a log source in Universal Connector.***

***Note: You can view the Secret access key only after it is created.***
 
### Configuring IAM Role for CloudWatch Integration

#### Before You Begin

In the Amazon UI, create and save the IAM role. You use it when you configure a Universal Connector.

#### Procedure
1.	Log in to your IAM console (https://console.aws.amazon.com/iam/).
2.	Select ```AWS service``` as ```Trusted entity type``` and ```EC2``` as a use case.
3.	Select ```Roles``` in the left-hand navigation and then create a new IAM role that has CloudWatchLogsFullAccess and STS:full access permissions.
4.	Go to the Elastic Computing (EC2) instance page that hosts the collector and assign to it the IAM role that you created.

#### Results
That IAM role that you created will be used to configure the connector in role_arn (no access key ID or secret access key will be needed).

### Configuring AWS Security Credentials for Cross-Account CloudWatch Integration That Uses a Configuration with role_arn

When you are using two AWS accounts, use the following configuration: a Guardium hosted on an EC2 account and a CloudWatch account.

#### Before You Begin
This procedure assumes that:
• The CloudWatch account number is 2222.
• The Guardium on EC2 account number is 1111 and the EC2 instance ID is i-01111.
• The Amazon Resource Name (ARN) role assigned to the EC2 account is:
```arn:aws:iam::1111:role/ec2_only_assign_role_to2222```

***Note: If that role does not exist, create it and assign it to the EC2 instance.***

#### Procedure
1.	Create a role named ```ec2_read_loggorups_from_1111``` for the CloudWatch account (2222).

- Restrict access by using one of the following methods:
 - Add the policy ```CloudWatchLogsReadOnlyAccess``` to that role and an inline policy that eliminates access to all groups except ```npDemoGroup```.

	       {
                  "Version": "2012-10-17",
	             "Statement": {
	                  "Effect": "Deny",
	                  "Action": "*",
	                  "NotResource": [
	                      "arn:aws:logs:us-east-1:2222:log-group:npDemoGroup:*"
	                     ]

	          }
            }

 - Use an inline policy so the role can read logs only on the resource arn:```aws:logs:us-east-1:2222:log-group:npDemoGroup:*```.


	     {
	           "Version": "2012-10-17",
	           "Statement": [
	               {
	                  "Action": [
	                     "logs:Describe*",
	                     "logs:Get*",
	                     "logs:List*",
	                     "logs:StartQuery",
	                     "logs:StopQuery",
	                     "logs:TestMetricFilter",
	                     "logs:FilterLogEvents"
	                  ],
	                  "Effect": "Allow",
	                  "Resource": "arn:aws:logs:us-east-1:2222:log-group:npDemoGroup:*"	                  }
	              ]
	     }

2.	Add a trust policy to ```ec2_read_loggorups_from_1111``` to trust the role on the other account (1111) that is assigned to the ec2 that hosts Guardium.



	     {
	               "Version": "2012-10-17",
	"Statement": [
	{
	"Effect": "Allow",
	"Principal": {
	     "AWS”:“arn:aws:sts:: 1111:assumed-role/ ec2_only_assign_role_to2222/i-01111"
	},
	  "Action": "sts:AssumeRole"
	},
	        {
	            "Sid": "Allow",
	            "Effect": "Allow",
	            "Principal": {
	                "AWS": "arn:aws:sts:: 1111:assumed-role/ ec2_only_assign_role_to2222/i-
	01111"
	            },
    "Action": "sts:AssumeRole"
	        }
	]
	     }


In this example, the ARN of the created role is: ```arn:aws:iam::2222:role/ec2_read_loggorups_from_1111```

3. 	Add a trust policy to the role in the Guardium on EC2 account (1111) that is assigned to EC2. In this example, the role is: ```arn:aws:iam::1111:role/ec2_only_assign_role_to2222```.

	       {
	       {
       	"Version": "2012-10-17",
	       "Statement": [
       	{
       	"Effect": "Allow",
       	"Principal": {
	         "Service": "ec2.amazonaws.com"
       	},
         	"Action": "sts:AssumeRole"
	        }
     	  ]
       	}

And add this inline policy:


       {
           "Version": "2012-10-17",
           "Statement": [
             {
                "Sid": "VisualEditor0",
                "Effect": "Allow",
                "Action": "sts:AssumeRole",
                "Resource": [
                     "arn:aws:iam::2222:role/ec2_read_loggorups_from_1111"
                   ]
                 }
            ]
     }

4 .	Set the input configuration for CloudWatch in the universal connector where role_arn is ```arn:aws:iam::2222:role/ec2_read_loggorups_from_1111``` and log_group is ```npDemoGroup```. The log group is from the CloudWatch account (2222).

	cloudwatch_logs {
	                  start_position => "end"
	                  interval => 5
	                  log_group => [ "npDemoGroup" ]



	                  role_arn => "arn:aws:iam::2222:role/ec2_read_loggorups_from_1111"
	                      # like "AK7VU3RZIA6LUS2AOLSU"

	                region => "us-east-1"
	                   # like "us-east-1"

	               event_filter => '{$.eventSource="s3.amazonaws.com"}'
	                   # for filtering also based on bucket name use event_filter =>
	                   '{$.eventSource="s3.amazonaws.com" && $.requestParameters.bucketName= "<BUCKET_NAME>"}'
	                     type => "S3"
    }

## Configuring Amazon S3 Auditing via SQS

In this mode, events are moved to the SQS message queuing service, and then to the Guardium Universal Connector. It provides load balancing for multiple connectors from one database to the Guardium Universal Connector.

### Moving Events from S3 to SQS
Guardium uses input from the SQS message queuing service. Learn how to move events from S3 to SQS.

#### Before You Begin
Complete all tasks in Configuring Amazon S3 Auditing with CloudWatch.

#### Procedure
1.	Create a queue. See [Creating an Amazon SQS queue (console)](https://docs.aws.amazon.com/AWSSimpleQueueService/latest/SQSDeveloperGuide/step-create-queue.html).
	
2.	Create a rule in CloudWatch to move events from S3 to SQS.

a.	Go to https://console.aws.amazon.com/cloudwatch.

b.	In the left menu under ```Events```, click ```Rules```, then click ```Create rule```.

c.	In the Event Pattern Preview, click the Edit.

d.	In the text area, paste this text:

  	{
	"source": [
	"aws.s3"
	],
	"detail-type": [
	"AWS API Call via CloudTrail"
		],
	"detail": {
	"eventSource": [
	"s3.amazonaws.com"
	]
	}
     }

e.	Click ```Save```.

f.	Click ```Add target``` in the right ```Targets``` pane.

g.	In the ```combination``` box, select ```SQS queue```.

h.	In the ```queue combination``` box, select the name of the queue you created.

i.	Validate that the Enabled checkbox is checked.

j.	Click ```Create rule```.

### Configuring AWS User Account Security Credentials for SQS Integration

Create the security credentials that you use when you enable the connector on your collector.
	
#### Before You Begin
	
Complete all tasks in Configuring Amazon S3 Auditing with CloudWatch.

#### Procedure
	
1.	Create a policy for the relevant IAM user.

a.	Log in to IAM console as IAM user (https://console.aws.amazon.com/iam/).

b.	Go to ```IAM service``` > ```Policies``` > ```Create Policy```.

c.	Select ```SQS``` as the service.

d.	Select the next actions check boxes: ```ListQueues```, ```DeleteMessage```, ```DeleteMessageBatch```, ```GetQueueAttributes```, ```GetQueueUrl```, ```ReceiveMessage```, ```ChangeMessageVisibility```, ```ChangeMessageVisibilityBatch```.

e.	In the resources, specify the ARN of the queue you created.

f.	Click ```Review policy``` and specify the policy name.

g.	Click ```Create policy```.

2.	Assign the policy to the user.

a.	Log in to IAM console as IAM user (https://console.aws.amazon.com/iam/).

b.	Go to ```Users``` on the console and select the relevant IAM user you want to give permissions to. Click the username link.

c.	In the ```Permissions``` tab, click ```Add permissions```.

d.	Click ```Attach existing policies directly```.

e.	Find the policy you created, and select the checkbox next to it.

f.	Click ```Next: Review```.

g.	Click ```Add permissions```.
