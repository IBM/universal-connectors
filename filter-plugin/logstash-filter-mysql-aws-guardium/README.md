# AWS MySQL Guardium Logstash filter configuration
### Meet AWS MySQL
* Tested versions: 5.7
* Environment: AWS
* Supported inputs: CloudWatch (pull)
* Supported Guardium versions:
	* Guardium Data Protection: 11.4 and above
	* Guardium Insights: 3.2 and above

This is a Logstash filter configuration. This filter receives CloudWatch audit logs of AWS MySQL instances, and filters those events and parses them into a Guardium record instance. The information is then sent over to Guardium as a JSON GuardRecord.
This filter is a script written in Ruby. It should be copied directly into the Guardium universal connector configuration. There is no need to modify the filter section (changes in the filter section may affect proper filtering).

### NOTES
* For CloudWatch to monitor your RDS instance, the MariaDB audit plug-in must be running on the instance. For information about this plug-in and version compatibilty, refer to [MariaDB Audit Plugin support](https://docs.aws.amazon.com/AmazonRDS/latest/UserGuide/Appendix.MySQL.Options.AuditPlugin.html).
* The client source program is not available in messages sent by MySQL. This data is sent only in the first audit log message upon database connection - and the filter plug-in doesn't aggregate data from different messages.
* The ‘type’ field should be the same in both the input and filter sections in the Logstash configuration file. This field should be unique for every individual connector added.
* GDP: requires installation of [json_encode](https://www.elastic.co/guide/en/logstash-versioned-plugins/current/v3.0.3-plugins-filters-json_encode.html) filter plug-in. This step is not necessary for Guardium Data Protection v12.0 and later.
* The 'use' statement does not display the account ID in the 'Database Name' column on the reports page.
## 1. Create and configure a MySQL database instance
### Create a MySQL database instance
To create a new MySQL instance, follow the instructions described [here](https://aws.amazon.com/getting-started/hands-on/create-mysql-db/). When setting the properties under Additional Configuration, in the Log exports section select Audit log and Error log as the log types to publish to Amazon.

## 2. Create and modify a new parameter group
To publish logs to CloudWatch, create a new parameter group and set the log_output parameter to FILE. When you create a database instance, it is associated with the default parameter group and cannot be modified. To create a new parameter group follow these steps:
### Procedure
1. Open the Amazon RDS console (https://console.aws.amazon.com/rds).
2. In the navigation pane, choose Parameter groups.
3. Choose Create parameter group to open the Create parameter group dialog box.
4. In the Parameter group family list, choose your engine version.
5. In the Group name box, enter the name of the new DB parameter group.
6. In the Description box, enter a description for the new DB parameter group.
7. Select Create.
8. Go back to Parameter groups from the navigation pane.
9. In the Parameter groups list, choose the parameter group that you just created.
10. Choose Parameter group actions, and then choose Edit.
11. Use the Filter parameters field to search for the log_output parameter.
12. Set the value of the log_output parameter to FILE.
13. Choose Save changes.

## 3. Enable audit logs using the MariaDB plugin
To add the MariaDB plug-in to a MySQL instance, follow the instructions described [here](https://docs.aws.amazon.com/AmazonRDS/latest/UserGuide/Appendix.MySQL.Options.AuditPlugin.html).
### NOTE
The 'rdsadmin' user queries the database every second to check its health. This activity may cause your log file to grow quickly to a very large size, which could result in unnecessary data proccessing in the filter. If recording this activity is not required, add the rdsadmin user to the SERVER_AUDIT_EXCL_USERS list.

## 4. Exporting Cloudwatch Logs to SQS using lambda function

In order to achieve load balancing of audit logs between different collectors, the audit logs must exported from Cloudwatch to SQS

### Creating the SQS

#### Procedure
1. Go to https://console.aws.amazon.com/
2. Click Services
3. Search for SQS and click on Simple Queue Services
4. Click on Create Queue
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

### Creating the lambda function

#### Create IAM Role

Create the IAM role that will be used in the Lambda function set up. The AWS lambda service will require permission to log events and write to the SQS created. Create the IAM Role “Export-RDS-CloudWatch-to-SQS-Lambda” with “AmazonSQSFullAccess”, “CloudWatchLogsFullAccess”, and “CloudWatchEventsFullAccess” policies.

##### Procedure
1. Go to https://console.aws.amazon.com/
2. Go to ```IAM``` -> ```Roles```
3. Click ```Create Role```
4. Under ```use case``` select ```Lambda``` and click ```Next```
5. Search for “AmazonSQSFullAccess” and select it
6. Search for “CloudWatchLogsFullAccess” and select it
7. Search for “CloudWatchEventsFullAccess” and select it
8. Set the ```Role Name```: e.g., “Export-RDS-CloudWatch-to-SQS-Lambda” and click ```Create role```

#### Create the lambda function

#### Procedure
1. Go to https://console.aws.amazon.com/
2. Go to ```Services```. Search for lambda function
3. Click ```Functions```
4. Click ```Create Function```
5. Keep ```Author for Scratch``` selected
6. Set ```Function name``` e.g., Export-RDS-CloudWatch-Logs-To-SQS
7. Under ```Runtime```, select ```Python 3.x```
8. Under ```Permissions```, select ```Use an existing role``` and select the IAM role that you created in the previous step (Export-RDS-CloudWatch-to-SQS-Lambda)
9. Click ```Create function``` and navigate to ```Code view```
10. Add the function code from [lambdaFunction](./MysqlOverSQSPackage/mysqlLambda.py)
11. Click ```Configuration``` -> ```Environment Variables```
12. Create 2 variables:
	1. Key = GROUP_NAME  value = <Name of the log group in Cloudwatch where Success logs of mysql are captured> e.g., /aws/rds/instance/mysql/audit
	2. Key = QUEUE_NAME  value = <Queue URL where logs are to be sent> e.g., https://sqs.ap-south-1.amazonaws.com/1111111111/MysqlQueue
13. Save the function
14. Click on the **Deploy** button

#### Automating the lambda function

#### Procedure
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
Before making any changes to the lambda function code, first disable the above rule. Deploy the change and then re-enable the rule.


## 5. Configuring the AWS MySQL filters in Guardium

The Guardium universal connector is the Guardium entry point for native audit logs. The Guardium universal connector identifies and parses the received events, and converts them to a standard Guardium format. The output of the Guardium universal connector is forwarded to the Guardium sniffer on the collector, for policy and auditing enforcements. Configure Guardium to read the native audit logs by customizing the postgres template.

### Authorizing outgoing traffic from AWS to Guardium

#### Procedure
1. Log in to the Guardium collector's API
2. Issue these commands:

   	• `grdapi add_domain_to_universal_connector_allowed_domains domain=amazonaws.com`

   	• `grdapi add_domain_to_universal_connector_allowed_domains domain=amazon.com`

#### Before you begin
• Configure the policies you require. See [policies](/../../#policies) for more information

• You must have permission for the S-Tap Management role. The admin user includes this role by default

• Download the [json-encode-offline-plugin.zip plug-in.](./json-encode-offline-plugin.zip)

• For Guardium Data Protection version 11.0p540 and/or 11.0p6505 and/or 12p15 download the [cloudwatch_logs plug-in](../../input-plugin/logstash-input-cloudwatch-logs/CloudwatchLogsInputPackage/offline-logstash-input-cloudwatch_log_1_0_5.zip)

#### Procedure

1. On the collector, go to Setup > Tools and Views > Configure Universal Connector.
2. First enable the Universal Guardium connector, if it is disabled already.
3. Click **Upload File**,
	* Select the [json-encode-offline-plugin.zip plug-in.](./json-encode-offline-plugin.zip) plug-in. After it is uploaded, click ```OK```
	* If you have installed Guardium Data Protection version 11.0p540 and/or 11.0p6505 and/or 12p15,  select the offline [cloudwatch_logs plug-in](../../input-plugin/logstash-input-cloudwatch-logs/CloudwatchLogsInputPackage/offline-logstash-input-cloudwatch_log_1_0_5.zip). After it is uploaded, click **OK**.
4. Click the Plus sign to open the Connector Configuration dialog box.
5. Type a name in the Connector name field.
6. If the audit logs are to be fetched from CloudWatch directly, use the details from the [mysqlCloudwatch.conf](./mysqlCloudwatch.conf) file. But if the audit logs are to be fetched from SQS, use the details from the [mysqlSQS.conf](./mysqlSQS.conf) file. Update the input section to add the details from the corresponding file's input section, omitting the keyword "input{" at the beginning and its corresponding "}" at the end. More details on how to configure the relevant input plugin can be found [here](../../input-plugin/logstash-input-cloudwatch-logs/README.md)
7. If the audit logs are to be fetched from CloudWatch directly, use the details from the [mysqlCloudwatch.conf](./mysqlCloudwatch.conf) file. But if the audit logs are to be fetched from SQS, use the details from the [mysqlSQS.conf](./mysqlSQS.conf) file. Update the filter section to add the details from the corresponding file's filter section, omitting the keyword "filter{" at the beginning and its corresponding "}" at the end.
8. The "type" fields should match in the input and the filter configuration sections. This field should be unique for every individual connector added. This is no longer required starting v12p20 and v12.1.
9. Click ```Save```. Guardium validates the new connector, and enables the universal connector if it was disabled. After it is validated, it appears in the ```Configure Universal Connector``` page

## Configuring the AWS MySQL Guardium Logstash filters in Guardium Insights

To configure this plug-in for Guardium Insights, follow [this guide.](/docs/Guardium%20Insights/3.2.x/UC_Configuration_GI.md)

In the input configuration section, refer to the CloudWatch_logs section.
