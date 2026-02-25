# Configuring DocumentDB on AWS datasource profiles for Kafka Connect Plug-ins

Create and configure datasource profiles through Central Manager for **DocumentDB over Cloudwatch Kafka Connect** plug-ins.

## Meet DocumentDB over Cloudwatch Kafka Connect

* **Environment:** AWS
* **Supported inputs:** Kafka Input (pull) 
* **Supported Guardium versions:**
    * Guardium Data Protection: Appliance bundle 12.2.2 or later

Kafka-connect is a framework for streaming data between Apache Kafka and other systems. This connector enables monitoring of DocumentDB audit logs through CloudWatch.

## Configuring Amazon DocumentDB

In the AWS web interface, configure the service for DocumentDB.

### Procedure

1. Go to https://console.aws.amazon.com/.
2. Click **Services** in the top left menu.
3. Underneath **All services**, click on **Database**.
4. On the right panel, click **Amazon DocumentDB**.
5. At the top right, click on the dropdown menu and select your region.
6. Click the orange **Create** button.
7. Enter a Cluster identifier.
8. Scroll down and click **Create cluster**.

## Enabling Audit Logs

There are different methods for auditing and logging. CloudTrail is used for this example as it supports all required parameters. The following events are supported for auditing in AWS.

### Procedure

1. From the AWS console, open the **Amazon DocumentDB** service
2. In the left navigation pane, click **Parameter groups**.
3. Check which cluster parameter group is currently associated with the DocumentDB cluster.
4. If the cluster is using the default parameter group, click Create (or Create parameter group) to create a custom cluster parameter group.
5. For the new parameter group, choose the **family** version that matches the DocumentDB cluster (for example, docdb4.0 if the DocumentDB cluster is running 4.0.0).
6. Enter a name and description for the parameter group, then click **Create**.
7. Select the newly created parameter group and click Edit (or Edit parameters).
8. In the search box, type **audit_logs** to locate the audit_logs parameter.
9. Change the value of **audit_logs** from disabled to one of the following, depending on the requirements:
10.    * **enabled** / **all**
11.    * or other supported value to log only specific event types (for example, only DDL/DML).
12. Save the changes to the parameter group.
13. In the left navigation pane, click **Clusters**, then select the DocumentDB cluster.
14. Click **Modify**.
15. In the Cluster parameter group (or Additional configuration) section, choose the custom parameter group which just edited.
16. Under Scheduling of modifications, select Apply immediately (or choose the appropriate maintenance window, if required).
17. Review the changes and click **Modify** cluster to apply the new parameter group with audit logging enabled.
18. After the cluster modification is complete, Amazon DocumentDB automatically creates a new CloudWatch Logs log group named:
    * /aws/docdb/<cluster-identifier>/audit

### Viewing DocumentDB log entries on CloudWatch

By default, each CloudTrail trail has an associated log group with a name in the format specified during trail creation. You can use this log group, or you can create a new one and associate it with the trail.

1. On the AWS Console page, open the **Services** menu.
2. Enter `CloudWatch` in the search box.
3. Click **CloudWatch** to redirect to the CloudWatch dashboard.
4. In the left panel, select **Logs**.
5. Click **Log Groups**.

## Exporting CloudWatch Logs to SQS Using Lambda Function (Optional)

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

Create the IAM role that will be used in the Lambda function setup. The AWS Lambda service requires permission to log events and write to the SQS created. Create the IAM Role **Export-DocumentDB-CloudWatch-to-SQS-Lambda** with **AmazonSQSFullAccess**, **CloudWatchLogsFullAccess**, and **CloudWatchEventsFullAccess** policies.

1. Go to https://console.aws.amazon.com/.
2. Go to **IAM** > **Roles** > **Create Role**.
4. Under **Use case**, select **Lambda** and click **Next**.
5. Search for ``AmazonSQSFullAccess`` and select it.
6. Search for ``CloudWatchLogsFullAccess`` and select it.
7. Search for ``CloudWatchEventsFullAccess`` and select it.
8. Set the **Role Name**. For example, **Export-DocumentDB-CloudWatch-to-SQS-Lambda**. Then click **Create role**.

### Create the Lambda Function

1. Go to https://console.aws.amazon.com/.
2. Go to **Services**. Search for Lambda function.
3. Click **Functions** > **Create Function**
5. Keep **Author from Scratch** selected.
6. Set **Function name**. For example, **Export-DocumentDB-CloudWatch-Logs-To-SQS**.
7. Under **Runtime**, select **Python 3.x**.
8. Under **Permissions**, select **Use an existing role** and select the IAM role created in the previous step (Export-DocumentDB-CloudWatch-to-SQS-Lambda).
9. Click **Create function** and navigate to **Code view**.
10. Add the function code from the DocumentDB Lambda function file (available in the plugin package).
11. Click **Configuration** > **Environment Variables**.
12. Create the following two variables.
    - Key = `GROUP_NAME`, value = `<Name of the log group in CloudWatch whose logs are to be exported>` e.g., `/aws/cloudtrail/DocumentDB-trail`
    - Key = `QUEUE_NAME`, value = `<Queue URL where logs are to be sent>` e.g., `https://sqs.us-east-1.amazonaws.com/1111111111/DocumentDB`
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
10. Select the lambda function created in the previous step (Export-DocumentDB-CloudWatch-Logs-To-SQS).
11. Add the tag if needed. Then click **Create Rule**.

**Note:** Before making any changes to the lambda function code, first disable the rule you created. Deploy the change and then re-enable the rule.

## Limitations

- The DocumentDB plug-in does not support IPV6.
- You may need to disable management events in order to avoid heavy traffic and data loss in Guardium. Disabling management events disables logging of the following events: CreateTable, DeleteTable, ListTable, UpdateTable, DescribeTable events.
- The following important fields can not be mapped with DocumentDB audit logs:
    - **Client HostName**: Not available with audit logs, so set as N.A.

## Creating datasource profiles

You can create a new datasource profile from the **Datasource Profile Management** page.

### Procedure

1. Go to **Manage > Universal Connector > Datasource Profile Management**
2. Click the **➕ (Add)** button.
3. You can create a profile by using one of the following methods:

    - To **Create a new profile manually**, go to the **"Add Profile"** tab and provide values for the following fields.
        - **Name** and **Description**.
        - Select a **Plug-in Type** from the dropdown. For example, `DocumentDB over Cloudwatch Connect 2.0` 

    - To **Upload from CSV**, go to the **"Upload from CSV"** tab and upload an exported or manually created CSV file containing one or more profiles.  
      You can also choose from the following options:
        - **Update existing profiles on name match** — Updates profiles with the same name if they already exist.
        - **Test connection for imported profiles** — Automatically tests connections after profiles are created.
        - **Use ELB** — Enables ELB support for imported profiles. You must provide the number of MUs to be used in the ELB process.

**Note:** Configuration options vary based on the selected plug-in.

## Configuring DocumentDB Over CloudWatch Kafka Connect 2.0

The following table describes the fields that are specific to DocumentDB over CloudWatch Kafka Connect 2.0 plugin.

| Field                    | Description                                                                                                                                                                                                                                                         |
|--------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Name**                 | Unique name of the profile.                                                                                                                                                                                                                                         |
| **Description**          | Description of the profile.                                                                                                                                                                                                                                         |
| **Plug-in**              | Plug-in type for this profile. Select `DocumentDB Over Cloudwatch Connect 2.0`. A full list of available plug-ins are available on the **Package Management** page.                                                                                        |
| **Credential**           | Select AWS Credentials or AWS Role ARN. The credential to authenticate with AWS. Must be created in **Credential Management**, or click **➕** to create one. For more information, see [Creating Credentials](https://www.ibm.com/docs/en/SSMPHH_12.x/com.ibm.guardium.doc.stap/guc/guc_credential_management.html). |
| **Kafka Cluster**        | Select the appropriate Kafka cluster from the available Kafka cluster list or create a new Kafka cluster. For more information, see [Managing Kafka clusters](https://www.ibm.com/docs/en/SSMPHH_12.x/com.ibm.guardium.doc.stap/guc/guc_kafka_cluster_management.html). |
| **Label**                | Grouping label. For example, customer name or ID.                                                                                                                                                                                                                   |
| **AWS account region**   | Specifies the AWS region where your DocumentDB tables are located (e.g., us-east-1, eu-west-1).                                                                                                                                                                  |
| **Log groups**           | List of CloudWatch log groups to monitor. These are the log groups where DocumentDB audit logs (via CloudTrail) are exported. Format: `/aws/cloudtrail/<trail_name>`                                                                                                        |
| **Filter pattern**       | CloudWatch Logs filter pattern to apply. Use "None" to retrieve all logs, or specify a pattern to filter specific log events.                                                                                                                                      |
| **Account ID**           | Your AWS account ID (12-digit number). This identifies your AWS account.                                                                                                                                                                                           |
| **Cluster name**         | The name of your DocumentDB cluster or table identifier.                                                                                                                                                                                                        |
| **Ingestion delay (seconds)** | Default value is 900 seconds (15 minutes). This delay accounts for the time it takes for logs to be available in CloudWatch after being generated.                                                                                                            |
| **No-traffic threshold (minutes)** | Default value is 60. If there is no incoming traffic for an hour, S-TAP displays a red status. Once incoming traffic resumes, the status returns to green.                                                                                                    |
| **Unmask sensitive value** | Optional boolean flag. When enabled, sensitive values in the audit logs will not be masked.                                                                                                                                                                       |
| **Use Enterprise Load Balancing (ELB)** | Enable this if ELB support is required.                                                                                                                                                                                                                             |
| **Managed Unit Count**   | Number of Managed Units (MUs) to allocate for ELB.                                                                                                                                                                                                                  |

**Note:**
- Ensure that the **profile name** is unique.
- Required credentials must be created before or during profile creation.
- The AWS credentials must have appropriate permissions to read CloudWatch logs and CloudTrail events.

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
