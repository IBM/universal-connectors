# Configuring MariaDB on AWS datasource profiles for Kafka Connect Plug-ins

Create and configure datasource profiles through Central Manager for **MariaDB over Cloudwatch Kafka Connect** plug-ins.

## Meet MariaDB over Cloudwatch Kafka Connect

* **Environment:** AWS
* **Supported inputs:** Kafka Input (pull) 
* **Supported Guardium versions:**
    * Guardium Data Protection: Appliance bundle 12.2.2 or later

Kafka-connect is a framework for streaming data between Apache Kafka and other systems. This connector enables monitoring of MariaDB audit logs through CloudWatch.

## Creating and configuring a MariaDB database instance

To create a new MariaDB instance, complete the steps in the [AWS Getting Started Guide](https://aws.amazon.com/getting-started/hands-on/create-mariadb-db/).

**Note:** When setting the properties under **Additional Configuration**, in the **Log exports** section, select **Audit log** as the log type to publish to Amazon CloudWatch logs.

## Enabling the MariaDB server audit Logs

1. Edit **Inbound port** rule.
2. Create a new parameter group.
3. Create a new option group and add **MARIADB_AUDIT_PLUGIN**.
4. Modify **Parameter group** and **Option groups** in DB Instance.

### Editing an inbound port rule

1. Select your MariaDB instance.
2. Under **Connectivity & security**, click on the VPC security group.
3. Select **Inbound rules**, then click **Edit inbound rules**.
4. Set custom value to `0.0.0.0/0`.
5. Click **Add rules**, then click **Save rule**.

### Creating a new parameter group

To publish logs to CloudWatch, create a new parameter group and set the **log_output** parameter to `FILE`. When you create a database instance, it is associated with the default parameter group and cannot be modified.

1. Open the Amazon RDS console (https://console.aws.amazon.com/rds).
2. In the navigation pane, choose **Parameter groups**.
3. Choose **Create parameter group** to open the Create parameter group dialog box.
4. In the **Parameter group family** list, choose your engine version.
5. In the **Group name** box, enter the name of the new DB parameter group.
6. In the **Description** box, enter a description for the new DB parameter group.
7. Click **Create**.

### Configuring log_output.

1. Select the parameter group, click on **Parameter group action** from the drop-down menu, then click **Edit**.
2. In the parameters filter search box, filter by `log_output`.
3. Using the drop-down menu, set the **log_output** parameter to `FILE`.
4. Click **Save changes**.

### Creating a new option group and adding MARIADB_AUDIT_PLUGIN.

You must add **MARIADB_AUDIT_PLUGIN** to enable Server Audit Logs.

1. In the RDS dashboard, select **Option groups** and then click **Create group**.
2. In the **Create option group** window, complete the following steps.</br>
    a. For **Name**, type a name for the option group.</br>
    b. For **Description**, type a brief description of the option group.</br>
    c. For **Engine**, choose the MariaDB DB engine.</br>
    d. For **Major engine version**, choose the major version of the DB engine.</br>
    e. Click **Create**.</br>
3. To add MARIADB_AUDIT_PLUGIN, complete the following steps.</br>
    a. Select the created Option group, then click **Add options**.</br>
    b. Set **Option name** to `MARIADB_AUDIT_PLUGIN`.</br>
    c. Keep option setting parameters with default values.</br>
    d. Pass **SERVER_AUDIT_EXCL_USERS** value to `rdsadmin`.</br>
    e. Set the value for `SERVER_AUDIT_EVENTS` to `QUERY, CONNECT` to see query and connection logs.</br>
    f. To enable the option immediately, choose **Yes** for **Apply Immediately**.</br>
    g. Click **Add option**.</br>

For more information about adding the MariaDB plug-in to a MySQL instance, see [AWS documentation](https://docs.aws.amazon.com/AmazonRDS/latest/UserGuide/Appendix.MySQL.Options.AuditPlugin.html).

**Note:** The **rdsadmin** user queries the database every second to check its health. This activity may cause the log file to grow quickly to a very large size, which could result in unnecessary data processing in the filter. If recording this activity is not required, add the `rdsadmin` user to the `SERVER_AUDIT_EXCL_USERS` list.

### Modifying parameter and option groups in DB instance

1. Choose the DB instance hyperlink, then choose **Modify**.
2. In the **Settings** section, confirm the password.
3. Under **Additional configuration**, use the drop-down menu to modify **DB parameter group** and **Option group**.
4. For the last section, keep the default settings and click **Continue**.

## Connecting to MariaDB Instance

1. Download and install MySQL Workbench.
2. Copy the endpoint and port of your MariaDB instance.
3. Open MySQL Workbench, choose a database connection, specify endpoint, port, and master credentials, then click **OK**.
4. Open MySQL Workbench query editor with the instance connection, then execute some queries.

### Viewing MariaDB log entries on CloudWatch

By default, each database instance has an associated log group with a name in this format: `/aws/rds/instance/<Instance_name>/audit`. You can use this log group, or you can create a new one and associate it with the database instance.

1. On the AWS Console page, open the **Services** menu.
2. Enter `CloudWatch` in the search box.
3. Click **CloudWatch** to redirect to the CloudWatch dashboard.
4. In the left panel, select **Logs**.
5. Click **Log Groups**.

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

### Create Policy for the Relevant IAM User

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
2. Go to **Services**. Search for Lambda function.
3. Click **Functions** > **Create Function**
5. Keep **Author from Scratch** selected.
6. Set **Function name**. For example, **Export-RDS-CloudWatch-Logs-To-SQS**.
7. Under **Runtime**, select **Python 3.x**.
8. Under **Permissions**, select **Use an existing role** and select the IAM role created in the previous step (Export-RDS-CloudWatch-Logs-To-SQS).
9. Click **Create function** and navigate to **Code view**.
10. Add the function code from the DynamoDB Lambda function file (available in the plugin package).
11. Click **Configuration** > **Environment Variables**.
12. Create the following two variables.
    - Key = `GROUP_NAME`, value = `<Name of the log group in CloudWatch whose logs are to be exported>` e.g., `/aws/rds/instance/mariadbsqs/audit`
    - Key = `QUEUE_NAME`, value = `<Queue URL where logs are to be sent>` e.g., `https://sqs.us-east-1.amazonaws.com/1111111111/mariadb`
13. Save the function.
14. Click **Deploy**.


### Automating the Lambda Function

1. Go to the CloudWatch dashboard.
2. Go to **Events** > **Rules** on the left pane.
3. Click **Create Rule**.
4. Enter the name for the rule. For example, **cloudwatchToSqs**.
5. Under **Rule Type**, select **Schedule**.
6. Define the schedule. In **schedule pattern**, select a schedule that runs at a regular rate, such as every 10 minutes.
7. Enter the rate expression, meaning the rate at which the function should execute. This value must match the time specified in the lambda function code that calculates the time delta. For instance, if the function code is set to 2 minutes, set the rate to 2 minutes unless changed in the code. Then click **Next**.
8. Select the **Target1**. Select the **Target Type** as **AWS Service**.
9. Select **Target** as **Lambda Function**.
10. Select the lambda function created in the previous step (Export-RDS-CloudWatch-Logs-To-SQS).
11. Add the tag if needed. Then click **Create Rule**.


##### Important Note

Before making any changes to the lambda function code, first disable the rule you created. Deploy the change and then re-enable the rule.

## Limitations

- The following important fields could not be mapped with MariaDB audit logs:
    - **Source program**: This field is left blank since this information is not embedded in the messages pulled from AWS CloudWatch
    - **OS User**: Not available with audit logs
    - **Client HostName**: Not available with audit logs when connecting to MariaDB instance through SQL standard and third-party tools
    - **serverIP**: This field is populated with 0.0.0.0, as this information is not embedded in the messages pulled from AWS CloudWatch
    - **clientPort and serverPort**: Not available with audit logs
- For system-generated LOGIN_FAILED logs, the Dbuser value is not available, so it is set to `N.A`.
- Large SQL statements are truncated by AWS by default, which can cause a GuardUCInvalidRecordException as the event is no longer valid.
- Currently while using ELB, S-TAP registration is restricted to one primary MU, meaning the S-TAP and its logs appear only on the initial primary MU even when multiple primary MUs are present.

## Creating datasource profiles

You can create a new datasource profile from the **Datasource Profile Management** page.

### Procedure

1. Go to **Manage > Universal Connector > Datasource Profile Management**
2. Click the **➕ (Add)** button.
3. You can create a profile by using one of the following methods:

    - To **Create a new profile manually**, go to the **"Add Profile"** tab and provide values for the following fields.
        - **Name** and **Description**.
        - Select a **Plug-in Type** from the dropdown. For example, `MariaDB over Cloudwatch Kafka Connect 2.0` 

    - To **Upload from CSV**, go to the **"Upload from CSV"** tab and upload an exported or manually created CSV file containing one or more profiles.  
      You can also choose from the following options:
        - **Update existing profiles on name match** — Updates profiles with the same name if they already exist.
        - **Test connection for imported profiles** — Automatically tests connections after profiles are created.
        - **Use ELB** — Enables ELB support for imported profiles. You must provide the number of MUs to be used in the ELB process.

**Note:** Configuration options vary based on the selected plug-in.

## Configuring MariaDB Over CloudWatch Kafka Connect 2.0

The following table describes the fields that are specific to MariaDB over CloudWatch Kafka Connect 2.0 plugin.

| Field                    | Description                                                                                                                                                                                                                                                         |
|--------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Name**                 | Unique name of the profile.                                                                                                                                                                                                                                         |
| **Description**          | Description of the profile.                                                                                                                                                                                                                                         |
| **Plug-in**              | Plug-in type for this profile. Select `AWS MariaDB Over Cloudwatch Connect 2.0`. A full list of available plug-ins are available on the **Package Management** page.                                                                                        |
| **Credential**           | Select AWS Credentials or AWS Role ARN. The credential to authenticate with AWS. Must be created in **Credential Management**, or click **➕** to create one. For more information, see [Creating Credentials](https://www.ibm.com/docs/en/SSMPHH_12.x/com.ibm.guardium.doc.stap/guc/guc_credential_management.html). |
| **Kafka Cluster**        | Select the appropriate Kafka cluster from the available Kafka cluster list or create a new Kafka cluster. For more information, see [Managing Kafka clusters](https://www.ibm.com/docs/en/SSMPHH_12.x/com.ibm.guardium.doc.stap/guc/guc_kafka_cluster_management.html). |
| **Label**                | Grouping label. For example, customer name or ID.                                                                                                                                                                                                                   |
| **AWS account region**   | Specifies the AWS region where your RDS MariaDB instance is located (e.g., us-east-1, eu-west-1).                                                                                                                                                                  |
| **Log groups**           | List of CloudWatch log groups to monitor. These are the log groups where MariaDB audit logs are exported. Format: `/aws/rds/instance/<Instance_name>/audit`                                                                                                        |
| **Filter pattern**       | CloudWatch Logs filter pattern to apply. Use "None" to retrieve all logs, or specify a pattern to filter specific log events.                                                                                                                                      |
| **Account ID**           | Your AWS account ID (12-digit number). This identifies your AWS account.                                                                                                                                                                                           |
| **Cluster name**         | The name of your RDS MariaDB cluster or instance identifier.                                                                                                                                                                                                        |
| **Ingestion delay (seconds)** | Default value is 900 seconds (15 minutes). This delay accounts for the time it takes for logs to be available in CloudWatch after being generated.                                                                                                            |
| **No-traffic threshold (minutes)** | Default value is 60. If there is no incoming traffic for an hour, S-TAP displays a red status. Once incoming traffic resumes, the status returns to green.                                                                                                    |
| **Unmask sensitive value** | Optional boolean flag. When enabled, sensitive values in the audit logs will not be masked.                                                                                                                                                                       |
| **Use Enterprise Load Balancing (ELB)** | Enable this if ELB support is required.                                                                                                                                                                                                                             |
| **Managed Unit Count**   | Number of Managed Units (MUs) to allocate for ELB.                                                                                                                                                                                                                  |

**Note:**
- Ensure that the **profile name** is unique.
- Required credentials must be created before or during profile creation.
- The AWS credentials must have appropriate permissions to read CloudWatch logs.

## Testing a Connection

After creating a profile, you must test the connection to ensure the provided configuration is valid.

### Procedure

1. Select the new profile.
2. From the top menu, click **Test Connection**.
3. If the test is successful, you can proceed to installing the profile.

---

## Installing a Profile

Once the connection test is successful, you can install the profile on **Managed Units (MUs)** or **Edges**. The parsed audit logs are sent to the selected Managed Unit or Edge to be consumed by the **Sniffer**.

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

