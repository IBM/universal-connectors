## 1. Configuring the Aurora Postgres service

1. Go to https://console.aws.amazon.com/
2. Click ```Services```
3. In the Database section, click ```RDS```
4. Select the region in the top right corner
5. In the central panel of the Amazon RDS Dashboard, click ```Create database```
6. Choose a database creation method
7. In the Engine options, select ```PostgreSQL```, and then select the appropropriate version
8. Select an appropriate template (Production, Dev/Test, or Free Tier)
9. In the Settings section, type the database instance name and create the master account with the username and password to log in to the database
10. Select the database instance size according to your requirements
11. Select appropriate storage options (for example, you may want to enable auto-scaling)
12. Select the availability and durability options
13. Select the connectivity settings that are appropriate for your environment. To make the database accessible, set the Public access option to Publicly Accessible within Additional Configuration
14. Select the type of Authentication for the database (choose from Password Authentication, Password and IAM database authentication, and Password and Kerberos authentication)
15. Expand the Additional Configuration options:

	a. Configure the database options

	b. Select options for Backup

	c. If desired, enable Encryption on the database instances

	d. In Log exports, select the Postgresql log type to publish to Amazon CloudWatch

	e. Select the options for Deletion protection

16. Click ```Create Database```
17. To view the database, click ```Databases``` under Amazon RDS in the left panel
18. To authorize inbound traffic, edit the security group:

	a. In the database summary page, select the Connectivity and Security tab. Under Security, click VPC security group

	b. Click the group name that you selected while creating a database (each database has one active group)

	c. In the Inbound rule section, choose to edit the inbound rules

	d. Set this rule:

		• Type: PostgreSQL

		• Protocol: TCP

		• Port Range: 5432

		Notes: Depending on your requirements, the source can be set to a specific IP address or it can be opened to all hosts.

	e. Click ```Add Rule ```and then click ```Save changes```. The database may need to be restarted

## 2. Enabling the PGAudit extension

There are different ways of auditing and logging in postgres. For this exercise, we will use PGAudit, the open
source audit logging extension for PostgreSQL 9.5+. This extension supports logging for Sessions or Objects.
Configure either Session Auditing or Object Auditing. You cannot enable both at the same time.

### Steps to enable PGAudit

1. Creating the database parameter group
2. Enabling Auditing using **either one** of the following:

    a. Enabling PGAudit Session Auditing

    b. Enabling PGAudit Object Auditing

3. Associating the DB Parameter Group with the database Instance

#### Creating the database parameter group

When you create a database instance, it is associated with the default parameter group. Follow these
steps to create a new parameter group:

1. Go to ```Services``` > ```Database``` > ```Parameter groups```
2. Click ```Create Parameter Group``` in the left pane
3. Enter the parameter group details

	• Select the parameter group family. For example, aurora-postgres12. This version should match the version of the database that is created and with which this parameter group is to be associated

	• Enter the DB parameter group name

	• Enter the DB parameter group description
		
4. Click ```Save```. The new group appears in the Parameter Groups section

#### Enabling PGAudit Session Auditing

Session Auditing allows you to log activities that are selected in the pgaudit.log for logging. Be cautious when you select which activities will be logged, as logged activities can affect the performance of the database instance.

1. In the left-hand Amazon RDS panel, select Parameter Groups
2. Select the parameter group that you created
3. Click Edit parameters and add these settings:

	• pgaudit.log = all, -misc
		(Select the options from the Allowed values list. You can specify multiple values, and separate them with “,”. The values that are marked with “-” are excluded while logging.)

	• pgaudit.log_catalog = 0

	• pgaudit.log_parameter = 0

	• shared_preload_libraries = pgaudit

	• log_error_verbosity = default

#### Enabling PGAudit Object Auditing

Object auditing affects the performance less than session auditing, due to the fine-grained criteria of tables and columns that you can choose for auditing.

1. Set these parameters:

	• pgaudit.log = none (since this is not needed for extensive SESSION logging)

	• pgaudit.role = rds_pgaudit

	• pgaudit.log_catalog = 0

	• pgaudit.log_parameter = 0

	• shared_preload_libraries = pgaudit

	• log_error_verbosity = default
	
2. Provide the required permissions to the rds_pgaudit role while associating it with the table that is audited. For example, grant ALL on <relation_name> to rds_pgaudit (this grant enables full SELECT, INSERT, UPDATE, and DELETE logging on the relation_name).

#### Associating the DB Parameter Group with the database Instance

1. Go to ```Services``` > ```Database``` > ```RDS``` > ```Databases```
2. Click the Aurora Postgres database instance to be updated
3. Click ```Modify```
4. Go to the Additional Configuration ```section``` > ```database options``` > ```DB Parameter Group menu ```and select the ```newly-created group```
5. Click ```Continue```
6. Select the database instance in its configuration section. The state of the DB Parameter Group is pending-reboot
7. Reboot the database instance for the changes to take effect


## 3. Viewing the PGAudit logs

The PGAudit logs (both Session and Object logs) can be seen in log files in RDS, and also on CloudWatch:


### Viewing the auditing details in RDS log files
The RDS log files can be viewed, watched, and downloaded. The name of the RDS log file is modifiable and is controlled by parameter log_filename.

1. Go to ```Services``` > ```Database``` > ```RDS``` > ```Databases```
2. Select the database instance
3. Select the Logs & Events section
4. The end of the Logs section lists the files that contain the auditing details. The newest file is the last page

### Viewing the logs entries on CloudWatch
By default, each database instance has an associated log group with a name in this format: /aws/rds/cluster/<instance_name>/postgresql. You can use this log group, or you can create a new one and associate it with the database instance.

1. On the AWS Console page, open the Services menu
2. Enter the CloudWatch string in the search box
3. Click CloudWatch to redirect to the CloudWatch dashboard
4. In the left panel, select Logs
5. Click ```Log Groups```


#### Notes
* GDP: requires installation of [json_encode](https://www.elastic.co/guide/en/logstash-versioned-plugins/current/v3.0.3-plugins-filters-json_encode.html) filter plug-in

## 4. Exporting Cloudwatch Logs to SQS using lambda function
In order to achieve load balancing of audit logs between different collectors, the audit logs must exported from Cloudwatch to SQS

### Creating the SQS
1. Go to https://console.aws.amazon.com/
2. Click ```Services```
3. Search for SQS and click on ```Simple Queue Services```
4. Click on ```Create Queue```
5. Select the type as Standard
6. Enter the name for the queue
7. Keep the rest of the default settings

#### Create Policy for the relevant IAM User

1. For the IAM User using which the SQS logs are to be accessed in Guardium, perform the below steps
2. Go to https://console.aws.amazon.com/
3. Go to ```IAM service``` > ```Policies``` > ```Create Policy```
4. Select ```service as SQS```
5. Select the check boxes having actions as: ```ListQueues```, ```DeleteMessage```, ```DeleteMessageBatch```, ```GetQueueAttributes```, ```GetQueueUrl```, ```ReceiveMessage```, ```ChangeMessageVisibility```, ```ChangeMessageVisibilityBatch```
6. In the resources, specify the ARN of the queue created in the above step
7. Click ```Review policy``` and specify the policy name
8. Click ```Create policy```
9. Assign the policy to the user
		
	a.	Log in to the IAM console as IAM user (https://console.aws.amazon.com/iam/) 

	b.	Go to ```Users``` on the console and select the relevant IAM user to whom you want to give permissions. Click the user name link

	c.	In the ```Permissions``` tab, click ```Add permissions```

	d.	Click ```Attach existing policies directly```

	e.	Search for the policy created and check the checkbox next to it

	f.	Click ```Next: Review```

	g.	Click ```Add permissions```
	

#### Create IAM Role

Create the IAM role that will be used in the Lambda function set up. The AWS lambda service will require permission to log events and write to the SQS created. Create the IAM Role “Export-RDS-CloudWatch-to-SQS-Lambda” with “AmazonSQSFullAccess”, “CloudWatchLogsFullAccess”, and “CloudWatchEventsFullAccess” policies.

1. Go to https://console.aws.amazon.com/
2. Go to ```IAM``` -> ```Roles```
3. Click ```Create Role```
4. Under ```use case``` select ```Lambda``` and click ```Next```
5. Search for “AmazonSQSFullAccess” and select it
6. Search for “CloudWatchLogsFullAccess” and select it
7. Search for “CloudWatchEventsFullAccess” and select it
8. Set the ```Role Name```: e.g., “Export-RDS-CloudWatch-to-SQS-Lambda” and click ```Create role```

#### Create the lambda function

1. Go to https://console.aws.amazon.com/
2. Go to ```Services```. Search for lambda function
3. Click ```Functions```
4. Click ```Create Function```
5. Keep ```Author for Scratch``` selected
6. Set ```Function name``` e.g., Export-RDS-CloudWatch-Logs-To-SQS
7. Under ```Runtime```, select ```Python 3.x```
8. Under ```Permissions```, select ```Use an existing role``` and select the IAM role that you created in the previous step (Export-RDS-CloudWatch-to-SQS-Lambda)
9. Click ```Create function``` and navigate to ```Code view```
10. Add the function code from [lambdaFunction](./PostgresOverSQSPackage/postgresLambdaSQS.py)
11. Click ```Configuration``` -> ```Environment Variables```
12. Create 2 variables:
		1. Key = GROUP_NAME  value = <Name of the log group in Cloudwatch whose logs are to be exported> e.g., /aws/rds/cluster/aurora-postgres/postgresql
		2. Key = QUEUE_NAME  value = <Queue URL where logs are to be sent> e.g., https://sqs.ap-south-1.amazonaws.com/1111111111/PostgresQueue
13. Save the function
14. Click on the **Deploy** button

#### Automating the lambda function
1. Go to the CloudWatch dashboard
2. Go to ```Events``` -> ```Rules``` on the left pane
3. Click ```Create Rule```
4. Enter the name for the rule e.g., cloudwatchToSqs
5. Under ```Rule Type```, select ```Schedule```
6. Define the schedule. In ```schedule pattern``` select a schedule that runs at a regular rate, such as every 10 minutes
7. Enter the rate expression, meaning the rate at which the function should execute. This value must match the time specified in the lambda function code that calculates the time delta. (If the function code it is set to 2 minutes, set the rate to 2 minutes unless changed in the code). Click ```Next```
8. Select the ```Target1```. Select the ```Target Type``` as ```AWS Service```
9. Select ```Target``` as ```Lambda Function```
10. Select the lambda function created in the above step. e.g., Export-RDS-CloudWatch-Logs-To-SQS
11. Add the tag if needed
12. Click ```Create Rule```

#### Note
1. Before making any changes to the lambda function code, first disable the above rule. Deploy the change and then re-enable the rule. 
2. When large amount of events are to be pushed from Cloudwatch to SQS, it is possible that Lambda function may get timed out. In order to avoid data loss, 'Timeout' value needs to be updated. However, this value should be less than the value configured in scheduling of the Event Bridge Rule. We can even adjust the scheduling of the Event Bridge to suit the load requirements. The steps to change the timeout parameter :-
	1. Edit the created Lambda function.
	2. ```Click Configuration``` -> ```General configuration```
	3. Click Edit
	4. Set ```Timeout```,  eg : 1 min 45 sec

## 5. Configuring the postgres filters in Guardium
The Guardium universal connector is the Guardium entry point for native audit logs. The Guardium universal connector identifies and parses the received events, and converts them to a standard Guardium format. The output of the Guardium universal connector is forwarded to the Guardium sniffer on the collector, for policy and auditing enforcements. Configure Guardium to read the native audit logs by customizing the postgres template.
	
**Important**:

• Starting with Guardium Data Protection version 12.1, you can configuring the Universal Connectors in 2 ways. You can either use the legacy flow or the new flow.

• To configure Universal Connector by using the new flow, see [Managing universal connector configuration](https://www.ibm.com/docs/en/gdp/12.x?topic=connector-managing-universal-configuration) topic. 

• To configure the Universal Connector by using the legacy flow, use the procedure in the Authorizing outgoing traffic from AWS to Guardium topic.

### Authorizing outgoing traffic from AWS to Guardium
1. Log in to the Guardium collector's API.
2. Issue these commands:

		• `grdapi add_domain_to_universal_connector_allowed_domains domain=amazonaws.com`

		• `grdapi add_domain_to_universal_connector_allowed_domains domain=amazon.com`

#### Before you begin

* Configure the policies you require. See [policies](/../../#policies) for more information.
* You must have permission for the S-Tap Management role. The admin user includes this role by default.
* Download the [postgres-offline-plugins-7.5.2.zip.](https://github.com/IBM/universal-connectors/blob/main/filter-plugin/logstash-filter-postgres-guardium/PostgresOverCloudWatchPackage/Postgres/postgres-offline-plugins-7.5.2.zip)
* For Guardium Data Protection version 11.0p540 and/or 11.0p6505 and/or 12p15 download the [cloudwatch_logs plug-in](../../input-plugin/logstash-input-cloudwatch-logs/CloudwatchLogsInputPackage/offline-logstash-input-cloudwatch_log_1_0_5.zip)

#### Configuration

1. On the collector, go to ```Setup``` > ```Tools and Views``` > ```Configure Universal Connector```.
2. Enable the universal connector if it is disabled.
3. Click ```Upload File``` and select the offline [postgres-offline-plugins-7.5.2.zip](https://github.com/IBM/universal-connectors/blob/main/filter-plugin/logstash-filter-postgres-guardium/postgres-offline-plugins-7.5.2.zip) plugin. After it is uploaded, click ```OK```. This step is not necessary for Guardium Data Protection v11.0p490 or later, v11.0p540 or later, v12.0 or later.
4. Click the Plus sign to open the Connector Configuration dialog box.
5. Type a name in the ```Connector name``` field.
6. If the audit logs are to be fetched from CloudWatch directly, use the details from the [postgresCloudwatch.conf](./PostgresOverCloudWatchPackage/postgresCloudwatch.conf) file. But if the audit logs are to be fetched from SQS,  use the details from the [postgreSQS.conf](./PostgresOverSQSPackage/postgreSQS.conf) file. Update the input section to add the details from the corresponding file's input part, omitting the keyword "input{" at the beginning and its corresponding "}" at the end. More details on how to configure the relevant input plugin can be found [here](../../input-plugin/logstash-input-cloudwatch-logs/README.md)
7. If the audit logs are to be fetched from CloudWatch directly, use the details from the [postgresCloudwatch.conf](./PostgresOverCloudWatchPackage/postgresCloudwatch.conf) file. But if the audit logs are to be fetched from SQS,  use the details from the [postgreSQS.conf](./PostgresOverSQSPackage/postgreSQS.conf) file. Update the filter section to add the details from the corresponding file's filter part, omitting the keyword "filter{" at the beginning and its corresponding "}" at the end.
8. The "type" fields should match in the input and the filter configuration sections. This field should be unique for every individual connector added. This is no longer required starting v12p20 and v12.1.
9. Click ```Save```. Guardium validates the new connector and displays it in the Configure Universal Connector page.
10. After the offline plug-in is installed and the configuration is uploaded and saved in the Guardium machine, restart the Universal Connector using the ```Disable/Enable``` button.
   
**Note**: If you want to configure Cloudwatch with role_arn instead of access_key and secret_key then refer to the [Configuration for role_arn parameter in the cloudwatch_logs input plug-in](https://github.com/IBM/universal-connectors/blob/main/input-plugin/logstash-input-cloudwatch-logs/SettingsForRoleArn.md#configuration-for-role_arn-parameter-in-the-cloudwatch_logs-input-plug-in) topic.

