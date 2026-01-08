# AWS postgres

## Meet AWS Postgres

* Environment: AWS
* Supported inputs: CloudWatch (pull), SQS (pull)
* Supported Guardium versions: 
    * Guardium Data Protection: 11.4 and above
    * Guardium Data Security Center: 3.2 and above

## Configuring native logging

 If desired, enable encryption on the database instances. In **Additional configuration** > **Log exports**, select the Postgresql log type to publish to Amazon CloudWatch.

## Enabling the PGAudit extension

There are different ways of auditing and logging in Postgres. For this exercise, we will use PGAudit, the open
source audit logging extension for PostgreSQL 9.5+. This extension supports logging for Sessions or Objects.
Configure either Session Auditing or Object Auditing. You cannot enable both at the same time.

### Procedure

1. Creating the database parameter group
2. Enabling Auditing using **either one** of the following:

    a. Enabling PGAudit Session Auditing

    b. Enabling PGAudit Object Auditing

3. Associating the DB Parameter Group with the database Instance

#### Creating the database parameter group

When you create a database instance, it is associated with the default parameter group. Follow these
steps to create a new parameter group:

#### Procedure
1. Go to **Services** > **Database** > **Parameter groups**.
2. Click **Create Parameter Group**.
3. Enter the parameter group details.

	• Select the parameter group family. For example, postgres 12. This version should match the version of the database that is created and with which this parameter group is to be associated.

	• Enter the DB parameter group name.

	• Enter the DB parameter group description.
4. Click **Save**. The new group appears in the **Parameter Groups** section.

### Enabling PGAudit session auditing

Session auditing allows you to log activities that are selected in the pgaudit.log for logging. Be cautious when you select which activities will be logged, as logged activities can affect the performance of the database instance.


#### Procedure
1. In the Amazon RDS panel, select **Parameter Groups**.
2. Select the parameter group that you created.
3. Click **Edit parameters** and add these settings:

	• `pgaudit.log = all, -misc`

	(Select the options from the **Allowed values** list. You can specify multiple values and separate them with “,”. The values that are marked with “-” are excluded while logging.)

	• `pgaudit.log_catalog = 0`

	• `pgaudit.log_parameter = 0`

	• `shared_preload_libraries = pgaudit`

	• `log_error_verbosity = default`


### Enabling PGAudit Object Auditing

Object auditing affects the performance less than session auditing, due to the fine-grained criteria of tables and columns that you can choose for auditing.

#### Procedure
1. Set these parameters:

	• `pgaudit.log = none` (since this is not needed for extensive SESSION logging)

	• `pgaudit.role = rds_pgaudit`

	• `pgaudit.log_catalog = 0`

 	• `pgaudit.log_parameter = 0`

 	• `shared_preload_libraries = pgaudit`

	• `log_error_verbosity = default`
	
2. Provide the required permissions to the rds_pgaudit role while associating it with the table that is audited. For example, grant `ALL` on `<relation_name>` to `rds_pgaudit` (this grant enables full `SELECT`, `INSERT`, `UPDATE`, and `DELETE` logging on the `relation_name`).

### Associating the DB Parameter Group with the database instance

#### Procedure
1. Go to **Services** > **Database** > **RDS** > **Databases**.
2. Click the Postgres database instance to be updated.
3. Click **Modify**.
4. Go to the **Additional Configuration** section > **database options** > **DB Parameter Group** menu and select the newly-created group.
5. Click **Continue**.
6. Select the database instance in its configuration section. The state of the DB Parameter Group is pending-reboot.
7. Reboot the database instance for the changes to take effect.


## Viewing the PGAudit logs

The PGAudit logs (both Session and Object logs) can be seen in log files in RDS, and also on CloudWatch:


### Viewing the auditing details in RDS log files

The RDS log files can be viewed, watched, and downloaded. The name of the RDS log file is modifiable and is controlled by parameter log_filename.

#### Procedure
1. Go to **Services** > **Database** > **RDS** > **Databases**.
2. Select the database instance.
3. Select the **Logs & Events** section.
4. The end of the Logs section lists the files that contain the auditing details. The newest file is the last page.

### Viewing the logs entries on CloudWatch

By default, each database instance has an associated log group with a name in this format: /aws/rds/instance/<instance_name>/postgresql. You can use this log group, or you can create a new one and associate it with the database instance.

#### Procedure
1. On the AWS Console page, open the **Services** menu.
2. Enter the CloudWatch string in the search box.
3. Click **CloudWatch** to redirect to the CloudWatch dashboard.
4. Select **Logs**.
5. Click **Log Groups**.


#### Notes
* Guardium Data Protection requires installation of the [json_encode](https://www.elastic.co/guide/en/logstash-versioned-plugins/current/v3.0.3-plugins-filters-json_encode.html) filter plug-in


## Exporting CloudWatch Logs to SQS using lambda function

In order to achieve load balancing of audit logs between different collectors, the audit logs must exported from CloudWatch to SQS

### Creating the SQS

#### Procedure
1. Go to https://console.aws.amazon.com/
2. Click **Services**
3. Search for SQS and click on Simple Queue Services
4. Click on **Create Queue**
5. Select the type as **Standard**
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
1. Go to https://console.aws.amazon.com/.
2. Go to **Services** and search for ``lambda function``.
3. Click **Functions > Create Function**.
4. Make sure **Author for Scratch** is selected.
5. Enter a **Function name**. For example, ``Export-RDS-CloudWatch-Logs-To-SQS``.
6. Under **Runtime**, select **Python 3.x**.
7. Under **Permissions**, select **Use an existing role**. Then select the IAM role that you created in the previous step (Export-RDS-CloudWatch-to-SQS-Lambda).
8. Click **Create function** and navigate to **Code view**.
9. Add the function code from [lambdaFunction](./PostgresOverSQSPackage/postgresLambda.py).
10. Click **Configuration > Environment Variables**.
11. Create two variables: </br>
	a. Key = GROUP_NAME value = Name of the log group in CloudWatch from where logs are to be exported. For example, /aws/rds/instance/database-1/postgresql. </br>
	b. Key = QUEUE_NAME value = Queue URL where logs are to be sent. For example, https://sqs.ap-south-1.amazonaws.com/11111111/PostgresQueue. </br>
	c. Key = PARAMETER_NAME value = Name of the parameter store in the System Manager Parameter Store. For example, "LastExecutionTimestamp". </br>
	d. Key = ENABLE_DEBUG value = <True/False>. This setting controls the debugging statements. </br>
12. Click **Save** to save the environment variables.
13. From the **Code** tab, deploy the function by using one of the following methods:
	* **New Lambda Editor**: From the left sidebar, go to **EXPLORER > DEPLOY** and click **Deploy**.
	* **Classic Lambda Editor**: Click **Deploy** in the top-right area of the code editor.
14. Wait for the deployment to complete. A message is displayed when the deployment is successful.

#### Automating the lambda function

**Note**: AWS has migrated CloudWatch Events to Amazon EventBridge. Use the EventBridge service to create scheduling rules for Lambda functions.

1. Go to the AWS Console and search for ``EventBridge``.
2. To open the EventBridge dashboard, click **Amazon EventBridge**.
3. In the left navigation pane, click **Rules** under **Events**.
4. Click **Create rule**, and enter the rule details. </br>
	a. **Name**: Enter a name for the rule. For example, `cloudwatchToSqs`. </br>
	b. **Description**: (Optional) Add a description. </br>
	c. **Event bus**: Select **default**. </br>
5. In the **Rule type** field, select **Schedule** and click **Next**.
6. Define the schedule pattern. </br>
	a. Select **A schedule that runs at a regular rate, such as every 10 minutes**. </br>
	b. Enter the rate expression (e.g., ``2`` minutes). This value must match the time specified in the lambda function code that calculates the time delta. If the function code is set to 2 minutes, set the rate to 2 minutes unless it is changed in the code. </br>
	c. Click **Next**.
7. Select the target. </br>
	a. In the **Target types** field, select **AWS service**. </br>
	b. In the **Select a target** field, select **Lambda function**. </br>
   	c. In the **Function** field, select the Lambda function that you created in the previous step. For example, **Export-RDS-CloudWatch-Logs-To-SQS**. </br>
   	d. Click **Next**. </br>
8. (Optional) Add tags if needed, then click **Next**.
9. Review the rule configuration and click **Create rule**.

**Note:** Before making any changes to the Lambda function code, you must disable this rule. Once you deploy the change, you can re-enable the rule.

## Configuring the Postgres filters in Guardium

The Guardium universal connector is the Guardium entry point for native audit logs. The universal connector identifies and parses the received events, and converts them to a standard Guardium format. The output of the universal connector is forwarded to the Guardium sniffer on the collector, for policy and auditing enforcements. Configure Guardium to read the native audit logs by customizing the Postgres template.

### Authorizing outgoing traffic from AWS to Guardium

#### Procedure
1. Log in to the Guardium Collector's API.
2. Issue these commands:
   
    • `grdapi add_domain_to_universal_connector_allowed_domains domain=amazonaws.com`

    • `grdapi add_domain_to_universal_connector_allowed_domains domain=amazon.com`

#### Before you begin
• Configure the policies you require. See [policies](/docs/#policies) for more information.

• You must have permission for the S-Tap Management role. The admin user includes this role by default.

• This plug-in is automatically available with Guardium Data Protection versions 12.x, 11.4 with appliance bundle 11.0p490 or later or Guardium Data Protection version 11.5 with appliance bundle 11.0p540 or later releases.

• For Guardium Data Protection versions 11.0p540, 11.0p6505, 12.0 and 12p15 download the [cloudwatch_logs plug-in](../../input-plugin/logstash-input-cloudwatch-logs/CloudwatchLogsInputPackage/offline-logstash-input-cloudwatch_log_1_0_5.zip).

**Note**: For Guardium Data Protection version 11.4 without appliance bundle 11.0p490 or prior or Guardium Data Protection version 11.5 without appliance bundle 11.0p540 or prior, download the [postgres-offline-plugins-7.5.2.zip plug-in](https://github.com/IBM/universal-connectors/raw/release-v1.2.0/filter-plugin/logstash-filter-postgres-guardium/PostgresOverCloudWatchPackage/Postgres/postgres-offline-plugins-7.5.2.zip) plug-in. (Do not unzip the offline-package file throughout the procedure). 

#### Procedure

1. On the collector, go to **Setup** > **Tools and Views** > **Configure Universal Connector**.
2. Enable the universal connector if it is disabled.
3. Click **Upload File** and 
	*  Select the [offline postgres-offline-plugins-7.5.2.zip](https://github.com/IBM/universal-connectors/raw/release-v1.2.0/filter-plugin/logstash-filter-postgres-guardium/PostgresOverCloudWatchPackage/Postgres/postgres-offline-plugins-7.5.2.zip) plug-in. After it uploads, click **OK**. This is not necessary for Guardium Data Protection v11.0p490 or later, v11.0p540 or later, v12.0 or later.
	*  If you have installed Guardium Data Protection version 11.0p540 and/or 11.0p6505, 12.0 and/or 12p15, select the offline [cloudwatch_logs plug-in](../../input-plugin/logstash-input-cloudwatch-logs/CloudwatchLogsInputPackage/offline-logstash-input-cloudwatch_log_1_0_5.zip). After it is uploaded, click **OK**.
4. Click the Plus sign to open the Connector Configuration dialog box.
5. Type a name in the **Connector name** field.
6. If the audit logs are to be fetched from CloudWatch directly, use the details from the [postgresCloudwatch.conf](./PostgresOverCloudWatchPackage/postgresCloudwatch.conf) file. But if the audit logs are to be fetched from SQS,  use the details from the [postgreSQS.conf](./PostgresOverSQSPackage/postgreSQS.conf) file. Update the input section to add the details from the corresponding file's input part, omitting the keyword "input{" at the beginning and its corresponding "}" at the end. More details on how to configure the relevant input plugin, see [Cloudwatch_logs input plug-in](../../input-plugin/logstash-input-cloudwatch-logs/README.md). 

   **Note**:If you want to configure Cloudwatch with role_arn instead of access_key and secret_key then refer to the [Configuration for role_arn parameter in the cloudwatch_logs input plug-in](https://github.com/IBM/universal-connectors/blob/main/input-plugin/logstash-input-cloudwatch-logs/SettingsForRoleArn.md#configuration-for-role_arn-parameter-in-the-cloudwatch_logs-input-plug-in) topic.

7. If the audit logs are to be fetched from CloudWatch directly, use the details from the [postgresCloudwatch.conf](./PostgresOverCloudWatchPackage/postgresCloudwatch.conf) file. But if the audit logs are to be fetched from SQS,  use the details from the [postgreSQS.conf](./PostgresOverSQSPackage/postgreSQS.conf) file. Update the filter section to add the details from the corresponding file's filter part, omitting the keyword "filter{" at the beginning and its corresponding "}" at the end
8. The "type" fields should match in the input and the filter configuration sections. This field should be unique for  every individual connector added.
9. Click **Save**. Guardium validates the new connector and displays it in the Configure Universal Connector page.

## Configuring the Postgres AWS Guardium Logstash filters in Guardium Data Security Center

To configure this plug-in for Guardium Data Security Center, follow [this guide.](/docs/Guardium%20Insights/3.2.x/UC_Configuration_GI.md)

For the input configuration step, refer to the [CloudWatch_logs section](/docs/Guardium%20Insights/3.2.x/UC_Configuration_GI.md#configuring-a-CloudWatch-input-plug-in).

# Troubleshooting

## 1. Troubleshooting for Seahorse Networking errors

If you encounter one of the following errors:

```
Exception: Seahorse::Client::NetworkingError
```

or

```
Exception: Seahorse::Client::NetworkingError: Socket closed
```

These errors typically indicate an issue with the AWS credentials or network configuration inside your Docker container. Follow the steps below to verify the issue:

### 1. Run `aws configure`
Ensure that your AWS credentials are correctly configured inside the Docker container(Klaus) within Collector. Run the following command to set up your AWS CLI configuration:

```bash
aws configure
```

This will prompt you to enter your AWS Access Key ID, Secret Access Key, default region, and output format. Make sure to provide valid credentials with the necessary permissions to access the required AWS services.

### 2. Verify AWS Identity with `aws sts get-caller-identity`
After configuring your AWS CLI credentials, verify that your AWS setup is correct by running the following command:

```bash
aws sts get-caller-identity
```

This command returns the IAM user or role associated with the AWS credentials being used. If the command fails, it may indicate that the credentials are misconfigured or missing required permissions.

### Additional Notes

* If you encounter the `Seahorse::Client::NetworkingError`, it suggests a network or connectivity issue, typically indicating that the AWS CLI (in our case Plugin) cannot establish a connection to AWS services. Ensure that the container has proper network access and can reach the AWS endpoints.
* If the error is `Seahorse::Client::NetworkingError: Socket closed`, it might indicate that the AWS CLI (in our case Plugin) connection was unexpectedly closed or terminated. This could be caused by network interruptions, firewall issues, or invalid credentials.
* If the network issue is resolved, you can re-establish the connection between UC and AWS using the following CLI command:
```
grdapi restart_universal_connector overwrite_old_instance="true"
```
## 2. Configuring event filters

Note: This is an optional step

To improve data processing efficiency and avoid delays, you can configure **event filtering** to collect specific types of events from AWS into Guardium. This is done using the `event_filter` parameter under the input filter configuration.

For example

```text
event_filter => '?delete ?DELETE ?insert ?INSERT'
```
This query filters DELETE and INSERT operations (case-insensitive) and reduces unnecessary event processing.

You can customize the filter based on specific events that are relevant to your use case. To update the filter, modify the event types such as delete and insert that are listed in the event_filter string.