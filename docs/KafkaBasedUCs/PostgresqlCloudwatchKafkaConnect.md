# Configuring Postgres datasource profiles for Kafka Connect Plug-ins

Create and configure datasource profiles datasource profiles through Central Manager for **AWS
Postgres over CloudWatch Kafka Connect** plug-ins.

### Meet Postgres over CloudWatch Connect

* Environments: AWS
* Supported inputs: Kafka Input (pull)
* Supported Guardium versions:
    * Guardium Data Protection: Appliance bundle 12.2.2 or later

Kafka-connect is a framework for streaming data between Apache Kafka and other systems. This connector enables
monitoring of Postgres audit logs through CloudWatch.

## Optional: Configuring native logging

Enable encryption on the database instances. In **Additional configuration** > **Log exports**, select the
Postgresql log type to publish to Amazon CloudWatch.

## Enabling the PGAudit extension

There are different ways of auditing and logging in Postgres. This procedure used PGAudit, the open
source audit logging extension for PostgreSQL 9.5+. This extension supports logging for Sessions or Objects.

**Note:** Configure either **Session Auditing** or **Object Auditing**. You cannot enable both at the same time.

### 1. Creating a database parameter group. 
   
When you create a database instance, it is associated with the default parameter group. To create a new database parameter group, complete the following steps. 

1. Go to **Services** > **Database** > **Parameter groups** 
2. From the left panel, click **Create Parameter Group**.
3. Enter the parameter group details.</br>
      a. Select the parameter group family. For example, **postgres12**. This version should match the version of the database you created and with which this parameter group will be associated. </br>
      b. Enter the **DB parameter group name**.</br>
      c. Enter the **DB parameter group description**.</br>
4. Click **Save**. The new group appears in the **Parameter Groups** section.

### 2a. Enabling PGAudit Session auditing

Session Auditing allows you to log activities that are selected in the **pgaudit.log** parameter for logging. Be cautious when selecting which activities to log, as logged activities can affect database instance performance.

1. From the Amazon RDS left panel, select **Parameter Groups**.
2. Select the parameter group you created.
3. Click **Edit parameters** and add the following settings.</br>
      a. **pgaudit.log** = ``all, -misc`` (Select options from the **Allowed values** list. You can specify multiple values separated by commas. Values that are marked with "**-**" are excluded from logging.)</br>
      b. **pgaudit.log_catalog** = ``0`` </br>
      c. **pgaudit.log_parameter** = ``0`` </br>
      d. **shared_preload_libraries** = ``pgaudit`` </br>
      e. **log_error_verbosity** = ``default``</br>

### 2b. Enabling PGAudit Object Auditing

Object auditing affects performance less than session auditing due to the fine-grained criteria of tables and columns that you can select for auditing.

1. Set the following parameters. </br>
      a. **pgaudit.log** = ``none`` (since this is not needed for extensive SESSION logging)</br>
      b. **pgaudit.role** = ``rds_pgaudit``</br>
      c. **pgaudit.log_catalog** = ``0``</br>
      d. **pgaudit.log_parameter** = ``0``</br>
      e. **shared_preload_libraries** = ``pgaudit``</br>
      f. **log_error_verbosity** = ``default``</br>

2. Provide the required permissions to the **rds_pgaudit** role when associating it with the table to be audited. For example, ```GRANT ALL ON <relation_name> TO rds_pgaudit```.
   This grant enables full **SELECT**, **INSERT**, **UPDATE**, and **DELETE** logging on the relation.

### 3. Associating the DB parameter group with the database instance

1. Go to **Services** > **Database** > **RDS** > **Databases**.
2. Click the Aurora Postgres database instance that you want to update.
3. Click **Modify**.
4. Go to **Additional Configurations** > **Database Options** > **DB Parameter Group menu**, and select the newly-created group.
5. Click **Continue**.
6. Select the database instance in its configuration section. The state of the DB Parameter Group is pending-reboot.
7. Reboot the database instance for the changes to take effect.


## Viewing the PGAudit logs

The PGAudit logs (both Session and Object logs) can be seen in log files in RDS, and also on CloudWatch.

### Viewing the auditing details in RDS log files

The RDS log files can be viewed, watched, and downloaded. The name of the RDS log file is modifiable and is controlled
by the **log_filename** parameter.

1. Go to **Services** > **Database** > **RDS** > **Databases**.
2. Select the database instance.
3. Select the **Logs & Events** section.
4. The end of the Logs section lists the files that contain the auditing details. The newest file is on the last page.

### Viewing the logs entries on CloudWatch

By default, each database instance has an associated log group with a name in this format: `/aws/rds/instance/<
instance_name>/postgresql`. You can use this log group, or you can create a new one and associate it with the database
instance.

1. On the AWS Console page, open the **Services** menu.
2. Enter the CloudWatch string in the search box.
3. Click **CloudWatch** to redirect to the CloudWatch dashboard.
4. In the left panel, select **Logs**.
5. Click **Log Groups**.


**Notes:** Guardium Data Protection requires installation of
  the [json_encode](https://www.elastic.co/guide/en/logstash-versioned-plugins/current/v3.0.3-plugins-filters-json_encode.html)
  filter plug-in. 


## Exporting CloudWatch logs to SQS Using Lambda Function (Optional)

To achieve load balancing of audit logs between different collectors, the audit logs can be exported from CloudWatch to SQS.

### Creating the SQS Queue

1. Go to https://console.aws.amazon.com/.
2. Click **Services**.
3. Search for SQS and click on **Simple Queue Services**.
4. Click **Create Queue**.
5. Select the type as **Standard**.
6. Enter the name for the queue.
7. Keep the rest of the default settings.

### Creating policies for the relevant IAM User

1. For the IAM User using which the SQS logs are to be accessed in Guardium, complete the following steps.
2. Go to https://console.aws.amazon.com/.
3. Go to **IAM service** > **Policies** > **Create Policy**.
4. Select **service as SQS**.
5. Select the following checkboxes: **ListQueues**, **DeleteMessage**, **DeleteMessageBatch**, **GetQueueAttributes**, **GetQueueUrl**, **ReceiveMessage**, **ChangeMessageVisibility**, **ChangeMessageVisibilityBatch**.
6. In the resources, specify the ARN of the queue created in the previous step.
7. Click **Review policy** and specify the policy name.
8. Click **Create policy**.
9. Assign the policy to the user. </br>
    a. Log in to the IAM console as IAM user (https://console.aws.amazon.com/iam/).  </br>
    b. Go to **Users** on the console and select the relevant IAM user to whom you want to give permissions. </br>
    c. In the **Permissions** tab, click **Add permissions**. </br>
    d. Click **Attach existing policies directly**.</br> 
    e. Search for the policy created and check the checkbox next to it.</br>
    f. Click **Next: Review** > **Add permissions**.</br>


## Creating the Lambda Function

### Create IAM Role

Create the IAM role that will be used in the Lambda function setup. The AWS Lambda service requires permission to log events and write to the SQS created. Create the IAM Role **Export-DynamoDB-CloudWatch-to-SQS-Lambda** with **AmazonSQSFullAccess**, **CloudWatchLogsFullAccess**, and **CloudWatchEventsFullAccess** policies.

1. Go to https://console.aws.amazon.com/.
2. Go to **IAM** > **Roles** > **Create Role**.
4. Under **Use case**, select **Lambda** and click **Next**.
5. Search for ``AmazonSQSFullAccess`` and select it.
6. Search for ``CloudWatchLogsFullAccess`` and select it.
7. Search for ``CloudWatchEventsFullAccess`` and select it.
8. Set the **Role Name**. For example, **Export-RDS-CloudWatch-to-SQS-Lambda**. Then click **Create role**.

### Creating the Lambda Function

1. Go to https://console.aws.amazon.com/.
2. Go to **Services**. Search for ``Lambda function``.
3. Click **Functions** > **Create Function**
5. Keep **Author from Scratch** selected.
6. Set **Function name**. For example, **Export-RDS-CloudWatch-Logs-To-SQS**.
7. Under **Runtime**, select **Python 3.x**.
8. Under **Permissions**, select **Use an existing role** and select the IAM role created in the previous step (Export-RDS-CloudWatch-Logs-To-SQS).
9. Click **Create function** and navigate to **Code view**.
10. Add the function code from [lambdaFunction](./PostgresOverSQSPackage/postgresLambda.py).
11. Click **Configuration** > **Environment Variables**.
12. Create the following two variables.
    - Key = `GROUP_NAME`, value = `<Name of the log group in CloudWatch whose logs are to be exported>` e.g., `/aws/rds/instance/mariadbsqs/audit`
    - Key = `QUEUE_NAME`, value = `<Queue URL where logs are to be sent>` e.g., `https://sqs.us-east-1.amazonaws.com/1111111111/mariadb`
13. Save the function.
14. Click **Deploy**.

### Automating the lambda function

**Note**: AWS has migrated CloudWatch Events to Amazon EventBridge. Use the EventBridge service to create scheduling
rules for Lambda functions.

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
   b. Enter the rate expression (e.g., ``2`` minutes). This value must match the time specified in the lambda function
   code that calculates the time delta. If the function code is set to 2 minutes, set the rate to 2 minutes unless it is
   changed in the code. </br>
   c. Click **Next**.
7. Select the target. </br>
   a. In the **Target types** field, select **AWS service**. </br>
   b. In the **Select a target** field, select **Lambda function**. </br>
   c. In the **Function** field, select the Lambda function that you created in the previous step. For example, *
   *Export-RDS-CloudWatch-Logs-To-SQS**. </br>
   d. Click **Next**. </br>
8. (Optional) Add tags if needed, then click **Next**.
9. Review the rule configuration and click **Create rule**.

**Note:** Before making any changes to the Lambda function code, you must disable this rule. Once you deploy the change,
you can re-enable the rule.

## Limitations

1. The postgres plug-in does not support IPV6
2. When Postgres UC is configured to be used with SQS, the multiline characters in the query are not preserved in FullSql Reports
3. When Postgres UC is configured to be used with SQS, the queries containing single line comments will not be supported
4. PGAudit logs the batch queries multiple times, so the report will show multiple entries for the same item
5. Client HostName and OS User fields couldn't be mapped with the logs, so set as empty.

## Creating datasource profiles

You can create a new datasource profile from the **Datasource Profile Management** page.

### Procedure

1. Go to **Manage > Universal Connector > Datasource Profile Management**
2. Click the **➕ (Add)** button.
3. You can create a profile by using one of the following methods:

    - To **Create a new profile manually**, go to the **"Add Profile"** tab and provide values for the following fields.
        - **Name** and **Description**.
        - Select a **Plug-in Type** from the dropdown. For example, `AWS Postgres Over Cloudwatch Connect 2.0`.

    - To **Upload from CSV**, go to the **"Upload from CSV"** tab and upload an exported or manually created CSV file
      containing one or more profiles.  
      You can also choose from the following options:
        - **Update existing profiles on name match** — Updates profiles with the same name if they already exist.
        - **Test connection for imported profiles** — Automatically tests connections after profiles are created.
        - **Use ELB** — Enables ELB support for imported profiles. You must provide the number of MUs to be used in the
          ELB process.

**Note:** Configuration options vary based on the selected plug-in.

## Configuring Postgres Over CloudWatch Kafka Connect 2.0

The following table describes the fields that are specific to Postgres over CloudWatch Kafka Connect 2.0 plugin.

| Field                                   | Description                                                                                                                                                                                                                                                                                                          |
|-----------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Name**                                | Unique name of the profile.                                                                                                                                                                                                                                                                                          |
| **Description**                         | Description of the profile.                                                                                                                                                                                                                                                                                          |
| **Plug-in**                             | Plug-in type for this profile. Select `AWS Postgres Over Cloudwatch Connect 2.0`. A full list of available plug-ins are available on the **Package Management** page.                                                                                                                                                |
| **Credential**                          | Select AWS Credentials or AWS Role ARN. The credential to authenticate with AWS. Must be created in **Credential Management**, or click **➕** to create one. For more information, see [Creating Credentials](https://www.ibm.com/docs/en/SSMPHH_12.x/com.ibm.guardium.doc.stap/guc/guc_credential_management.html). |
| **Kafka Cluster**                       | Select the appropriate Kafka cluster from the available Kafka cluster list or create a new Kafka cluster. For more information, see [Managing Kafka clusters](https://www.ibm.com/docs/en/SSMPHH_12.x/com.ibm.guardium.doc.stap/guc/guc_kafka_cluster_management.html).                                              |
| **Label**                               | Grouping label. For example, customer name or ID.                                                                                                                                                                                                                                                                    |
| **AWS account region**                  | Specifies the AWS region where your Postgres instance is located (e.g., us-east-1, eu-west-1).                                                                                                                                                                                                                       |
| **Log groups**                          | List of CloudWatch log groups to monitor. These are the log groups where Postgres audit logs are exported.                                                                                                                                                                                                           |
| **Filter pattern**                      | CloudWatch Logs filter pattern to apply. Use "None" to retrieve all logs, or specify a pattern to filter specific log events.                                                                                                                                                                                        |
| **Account ID**                          | Your AWS account ID (12-digit number). This identifies your AWS account.                                                                                                                                                                                                                                             |
| **Cluster name**                        | The name of your Postgres cluster or instance identifier.                                                                                                                                                                                                                                                            |
| **Ingestion delay (seconds)**           | Default value is 900 seconds (15 minutes). This delay accounts for the time it takes for logs to be available in CloudWatch after being generated.                                                                                                                                                                   |
| **No-traffic threshold (minutes)**      | Default value is 60. If there is no incoming traffic for an hour, S-TAP displays a red status. Once incoming traffic resumes, the status returns to green.                                                                                                                                                           |
| **Unmask sensitive value**              | Optional boolean flag. When enabled, sensitive values in the audit logs will not be masked.                                                                                                                                                                                                                          |
| **Use Enterprise Load Balancing (ELB)** | Enable this if ELB support is required.                                                                                                                                                                                                                                                                              |
| **Managed Unit Count**                  | Number of Managed Units (MUs) to allocate for ELB.                                                                                                                                                                                                                                                                   |

**Note:**

- Ensure that the **profile name** is unique.
- Required credentials must be created before or during profile creation.
- The AWS credentials must have appropriate permissions to read CloudWatch logs.

---

## Testing a Connection

After creating a profile, you must test the connection to ensure the provided configuration is valid.

### Procedure

1. Select the new profile.
2. From the top menu, click **Test Connection**.
3. If the test is successful, you can proceed to installing the profile.

---

## Installing a Profile

Once the connection test is successful, you can install the profile on **Managed Units (MUs)** or **Edges**. The parsed
audit logs are sent to the selected Managed Unit or Edge to be consumed by the **Sniffer**.

### Procedure

1. Select the profile.
2. From the **Install** menu, click **Install**.
3. From the list of available MUs and Edges that is displayed, select the ones that you want to deploy the profile to.

---

## Uninstalling or reinstalling profiles

An installed profile can be uninstalled or reinstalled if needed.

### Procedure

1. Select the profile.
2. From the list of available actions, select the desired option: **Uninstall** or **Reinstall**.

---
