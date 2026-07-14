# Configuring PostgreSQL audit logging for AWS RDS with Guardium

## Meet AWS Postgres

- Environment: AWS
- Supported inputs: CloudWatch (pull), SQS (pull)
- Supported Guardium versions:
  - Guardium Data Protection: 11.4 and later
  - Guardium Data Security Center: 3.2 and later
* Supported database versions: 13, 14

## Configuring native logging (optional)

You can enable encryption on the database instances by completing the following step.

Click **Additional configuration** > **Log exports**. Then select the **Postgresql** log type to publish to Amazon CloudWatch.

## Enabling the PGAudit extension

There are different ways of auditing and logging in PostgreSQL. This procedure uses PGAudit, the open
source audit logging extension for PostgreSQL 9.5 and later.

This extension supports logging for sessions or objects. You can configure either session auditing or object auditing, but not both at the same time.

1. Create the database parameter group.
2. Enable auditing by using one of the following methods:

   a. Enable PGAudit session auditing.

   b. Enable PGAudit object auditing.

3. Associate the DB parameter group with the database instance.

### Creating the database parameter group

When you create a database instance, it is associated with the default parameter group. To create a new parameter group, complete the following steps.

1.  From the AWS console, go to **Services** > **Database** > **Parameter groups**.
2.  Click **Create Parameter Group**.
3.  Configure the following parameter group fields:

        a.  Select the parameter group family. </br>

    &ensp;&ensp;For example, **postgres 12**. This version must match the version of the database that is created and with which this parameter group is to be associated. </br>
    b. Enter the database parameter group name. </br>
    c. Enter the database parameter group description. </br>

4.  Click **Save**. The new group appears in the **Parameter Groups** section.

### Enabling PGAudit session auditing

Session auditing logs the activities that you specify in the pgaudit.log parameter.

**Note:** When you select which activities to log, as extensive logging can affect database instance performance.

1. In the Amazon RDS panel, select **Parameter Groups**.
2. Select the parameter group that you created.
3. Click **Edit parameters** and add the following settings:

   a. `pgaudit.log = all, -misc` </br>

   **Note:** Select the options from the **Allowed values** list. You can specify multiple values and separate them with commas `,`. The values that are marked with a hyphen `-` are excluded from logging. </br>

   b. `pgaudit.log_catalog = 0` </br>

   c. `pgaudit.log_parameter = 0` </br>

   d. `shared_preload_libraries = pgaudit` </br>

   e. `log_error_verbosity = default` </br>

### Enabling PGAudit object auditing

Object auditing affects the performance less than session auditing, due to the fine-grained criteria of tables and columns that you can choose for auditing.

1. Configure the following parameters:

   a. `pgaudit.log = none` </br>

   b. `pgaudit.role = rds_pgaudit` </br>

   c. `pgaudit.log_catalog = 0` </br>

   d. `pgaudit.log_parameter = 0` </br>

   e. `shared_preload_libraries = pgaudit` </br>

   f. `log_error_verbosity = default` </br>

2. Provide the required permissions to the rds_pgaudit role while associating it with the table that is audited. </br>
   For example, grant `ALL` on `<relation_name>` to `rds_pgaudit`. This grant enables full `SELECT`, `INSERT`, `UPDATE`, and `DELETE` logging on `relation_name`.

### Associating the DB parameter group with the database instance

1. From the AWS console, go to **Services** > **Database** > **RDS** > **Databases**.
2. Select the Postgres database instance that you want to update and click **Modify**.
3. In **Additional Configuration** > **Database options**, select the newly created group from the **DB Parameter Group** menu.
4. Click **Continue**. </br>
   When you view the database instance in its configuration section, the state of the DB Parameter Group is `pending-reboot`.
5. Reboot the database instance for the changes to take effect.

### Logging Enabling query duration logging (optional)

Query duration logging tracks and logs queries based on their execution time. Use this feature for performance monitoring and identifying slow queries.

1. From the RDS console, go to **Parameter Groups** and select your parameter group.
2. Edit the following parameters: </br>

   a. To log all queries with duration, set `log_min_duration_statement` to `0`. </br>
   &ensp;&ensp;&ensp;_ `0` - logs all queries with duration </br>
   &ensp;&ensp;&ensp;_ `-1` - disables duration-based logging </br>
   &ensp;&ensp;&ensp;\* `5000` - logs queries that run longer than 5 seconds </br>
   </br>
   b. To avoid duplicate entries in CloudWatch, set `pgaudit.log` to `none`. </br>

3. Save the changes. </br>
   Changes apply immediately without requiring a database reboot.

## Viewing the PGAudit logs

You can view PGAudit logs (both session and object logs) in RDS log files and in CloudWatch.

### Viewing audit details in RDS log files

You can view, watch, and download RDS log files. The `log_filename` parameter specifies the log file name, which you can modify.

1. From the AWS console, go to **Services** > **Database** > **RDS** > **Databases**.
2. Select the database instance.
3. Select the **Logs & Events** tab.
4. In the **Logs** section, you can view the files that contain audit details. The newest file appears on the last page.

### Viewing log entries in CloudWatch

By default, each database instance has an associated log group with the name `/aws/rds/instance/<instance_name>/postgresql`. You can use this log group or create a new one and associate it with the database instance.

1. From the AWS console, open the **Services** menu.
2. Search for and select **CloudWatch**.
3. Click **Logs** > **Log Groups**.

**Note:** Guardium Data Protection requires installation of the [json_encode](https://www.elastic.co/guide/en/logstash-versioned-plugins/current/v3.0.3-plugins-filters-json_encode.html) filter plug-in.

## Exporting CloudWatch logs to SQS by using a Lambda function

To load balance audit logs between different collectors, export the audit logs from CloudWatch to SQS.

### Creating the SQS queue

1. From the [AWS console](https://console.aws.amazon.com), click **Services**.
2. Search for and select **Simple Queue Service**.
3. Click **Create Queue**.
4. Select **Standard** as the queue type.
5. Enter the name for the queue.
6. Keep the remaining settings at their default values and create the queue.

### Creating a policy for the IAM user

Create a policy for the IAM user that accesses the SQS logs in Guardium.

1. From the [AWS console](https://console.aws.amazon.com/), go to **IAM service** > **Policies** > **Create Policy**.
2. Select **SQS** as the service.
3. Select the following actions: **ListQueues**, **DeleteMessage**, **DeleteMessageBatch**, **GetQueueAttributes**, **GetQueueUrl**, **ReceiveMessage**, **ChangeMessageVisibility**, **ChangeMessageVisibilityBatch**.
4. In **Resources**, specify the ARN of the queue that you created.
5. Click **Review policy**, specify a policy name, and click **Create policy**.

### Assigning the policy to the user

1. From the [IAM console](https://console.aws.amazon.com/iam/), go to **Users** and select the IAM user to whom you want to assign permissions.
2. In the **Permissions** tab, click **Add permissions**.
3. Click **Attach existing policies directly**.
4. Search for and select the policy that you created.
5. Click **Next: Review** and then click **Add permissions**.

### Creating an IAM role

Create an IAM role for the Lambda function. The AWS Lambda service requires permission to log events and write to the SQS queue. Create the IAM role with the following policies: **AmazonSQSFullAccess**, **CloudWatchLogsFullAccess**, and **CloudWatchEventsFullAccess**.

1. From the [AWS console](https://console.aws.amazon.com/), go to **IAM** > **Roles**.
2. Click **Create Role**.
3. Under **Use case**, select **Lambda** and click **Next**.
4. Search for and select the following policies:
   - **AmazonSQSFullAccess**
   - **CloudWatchLogsFullAccess**
   - **CloudWatchEventsFullAccess**
5. Enter a **Role Name** (for example, `Export-RDS-CloudWatch-to-SQS-Lambda`) and click **Create role**.

### Creating the Lambda function

1. From the [AWS console](https://console.aws.amazon.com/), go to **Services** and search for `Lambda function`.
2. Click **Functions** > **Create Function**.
3. Ensure that **Author for Scratch** is selected.
4. Enter a **Function name**. For example, `Export-RDS-CloudWatch-Logs-To-SQS`.
5. Under **Runtime**, select **Python 3.x**.
6. Under **Permissions**, select **Use an existing role**. Then select the IAM role that you created (for example, `Export-RDS-CloudWatch-to-SQS-Lambda`).
7. Click **Create function**.
8. In the **Code** tab, add the function code from [lambdaFunction](./PostgresOverSQSPackage/postgresLambda.py).
9. Click **Configuration > Environment Variables** and create the following variables. </br>
   a. `GROUP_NAME` - Name of the log group in CloudWatch from where logs are exported. For example, `/aws/rds/instance/database-1/postgresql`. </br>
   b. `QUEUE_NAME` - Queue URL where logs are sent. For example, `https://sqs.ap-south-1.amazonaws.com/11111111/PostgresQueue`. </br>
   c. `PARAMETER_NAME` - Name of the parameter store in the System Manager Parameter Store. For example, `LastExecutionTimestamp`. </br>
   d. `ENABLE_DEBUG` - Set to `True` or `False` to control debugging statements. </br>
10. Click **Save**.
11. Deploy the function by using one of the following methods.
    - **New Lambda Editor**: From the left sidebar, go to **EXPLORER > DEPLOY** and click **Deploy**.
    - **Classic Lambda Editor**: Click **Deploy** in the top-right corner of the code editor.
12. Wait for the deployment to complete. A message is displayed when the deployment is successful.

### Automating the Lambda function

AWS migrated CloudWatch Events to Amazon EventBridge. Use EventBridge to create scheduling rules for Lambda functions.

1. From the AWS console, search for and select **Amazon EventBridge**.
2. In the left navigation pane, click **Events** > **Rules**.
3. Click **Create rule**, and configure the following fields. </br>
   a. **Name**: Enter a name for the rule. For example, `cloudwatchToSqs`. </br>
   b. **Description**: (Optional) Add a description. </br>
   c. **Event bus**: Select **default**. </br>
4. In the **Rule type** field, select **Schedule** and click **Next**.
5. Define the schedule pattern. </br>
   a. Select **A schedule that runs at a regular rate, such as every 10 minutes**. </br>
   b. Enter the rate expression (for example, `2` minutes). This value must match the time delta in the Lambda function code. If the function code uses 2 minutes, set the rate to 2 minutes.</br>
   c. Click **Next**.
6. Select the target, and configure the following fields. </br>
   a. **Target types**: Select **AWS service**. </br>
   b. **Select a target**: Select **Lambda function**. </br>
   c. **Function**: Select the Lambda function that you created. For example, **Export-RDS-CloudWatch-Logs-To-SQS**. </br>
   d. Click **Next**. </br>
7. (Optional) Add tags and click **Next**.
8. Review the rule configuration and click **Create rule**.

**Note:** Before you modify the Lambda function code, disable this rule. After you deploy the changes, re-enable the rule.

## Configuring the Postgres filters in Guardium

The Guardium universal connector identifies and parses native audit log events, and converts them to standard Guardium format. The output is forwarded to the Guardium sniffer on the collector for policy and auditing enforcement.

To configure Guardium to read native audit logs, customize the Postgres template.

## Authorizing outgoing traffic from AWS to Guardium

### Before you begin

1. Configure the required policies. For more information, see [policies](/docs/#policies).
2. You must have permission for the S-Tap Management role. The admin user includes this role by default.

### Procedure

1. Log in to the Guardium collector's API.
2. Run the following commands:
   ```
    grdapi add_domain_to_universal_connector_allowed_domains domain=amazonaws.com
    grdapi add_domain_to_universal_connector_allowed_domains domain=amazon.com
   ```
3. On the collector, go to **Setup** > **Tools and Views** > **Configure Universal Connector**.
4. If the universal connector is disabled, enable it.
5. Click the **plus sign (+)** to open the Connector Configuration dialog box.
6. In the **Connector name** field, enter a name for the connector.
7. Configure the input section by copying the content from the appropriate file, omitting `input{` at the beginning and the closing `}` at the end:
   - To fetch audit logs from CloudWatch, use the input section from [postgresCloudwatch.conf](./PostgresOverCloudWatchPackage/postgresCloudwatch.conf)..
   - To fetch audit logs from SQS, use the input section from [postgreSQS.conf](./PostgresOverSQSPackage/postgreSQS.conf).

   For more information about configuring the input plug-in, see [Cloudwatch_logs input plug-in](../../input-plugin/logstash-input-cloudwatch-logs/README.md).

   **Note**: To configure CloudWatch with `role_arn` instead of `access_key` and `secret_key`, see [Configuration for role_arn parameter in the cloudwatch_logs input plug-in](https://github.com/IBM/universal-connectors/blob/main/input-plugin/logstash-input-cloudwatch-logs/SettingsForRoleArn.md#configuration-for-role_arn-parameter-in-the-cloudwatch_logs-input-plug-in).

8. Configure the filter section by copying the content from the appropriate file, omitting `filter{` at the beginning and the closing `}` at the end:
   - To fetch audit logs from CloudWatch, use the filter section from [postgresCloudwatch.conf](./PostgresOverCloudWatchPackage/postgresCloudwatch.conf).
   - To fetch audit logs from SQS, use the filter section from [postgreSQS.conf](./PostgresOverSQSPackage/postgreSQS.conf).
9. Ensure that the `type` field values match in both the input and filter sections. This field must be unique for each connector.
10. Click **Save**. Guardium validates the new connector and displays it on the Configure Universal Connector page.

## Configuring the Postgres AWS Guardium Logstash filters in Guardium Data Security Center

To configure this plug-in for Guardium Data Security Center, see [Configuring universal connectors.](/docs/Guardium%20Insights/3.2.x/UC_Configuration_GI.md)

For more information on the input configuration step, see [CloudWatch_logs section](/docs/Guardium%20Insights/3.2.x/UC_Configuration_GI.md#configuring-a-CloudWatch-input-plug-in).

## Configuring event filters (optional)

To improve data processing efficiency and avoid delays, configure event filtering to collect specific event types from AWS. Use the `event_filter` parameter in the input filter configuration.

For example, to filter DELETE and INSERT operations (case-insensitive) and reduce unnecessary event processing, run the following command:

```
event_filter => '?delete ?DELETE ?insert ?INSERT'
```

You can customize the filter based on the events that are relevant to your use case. To update the filter, modify the event types in the `event_filter` string.

## Troubleshooting seahorse networking errors

### Symptoms

If you encounter the following errors, the issue is typically related to AWS credentials or network configuration in your Docker container:

```
Exception: Seahorse::Client::NetworkingError
```

or

```
Exception: Seahorse::Client::NetworkingError: Socket closed
```

### Resolving the problem

1. Verify your AWS credentials. </br>
   a. In the Docker container (Klaus) within the collector, configure your AWS CLI credentials by running the following command: </br>
   ```
   aws configure
   ```
   b. Enter your AWS Access Key ID, Secret Access Key, default region, and output format. Ensure that you provide valid credentials with the necessary permissions. </br>
   </br>
   c. Verify your AWS setup by running the following command. This command returns the IAM user or role associated with your AWS credentials. If the command fails, the credentials might be misconfigured or missing required permissions. </br>
   ```
   	aws sts get-caller-identity
   ```
2. Review the error type:
   - `Seahorse::Client::NetworkingError` indicates a network or connectivity issue. The plug-in cannot establish a connection to AWS services. Ensure that the container has network access and can reach AWS endpoints.
   - `Seahorse::Client::NetworkingError:` Socket closed indicates that the connection was unexpectedly closed or terminated. This error can be caused by network interruptions, firewall issues, or invalid credentials.

3. After you resolve the network issue, restart the connection between the universal connector and AWS by running the following command:
   ```
   grdapi restart_universal_connector overwrite_old_instance="true"
   ```
