# Configuring DocumentDB on AWS datasource profiles for Kafka Connect plug-ins

Create and configure datasource profiles through Central Manager for DocumentDB over CloudWatch Kafka Connect plug-ins.

## Meet DocumentDB over Cloudwatch Kafka Connect

* **Environment:** AWS
* **Supported inputs:** Kafka Input (pull) 
* **Supported Guardium versions:**
    * Guardium Data Protection: Appliance bundle 12.2.2 or later

Kafka-connect is a framework for streaming data between Apache Kafka and other systems. This connector enables monitoring of DocumentDB audit logs through CloudWatch.

## Configuring Amazon DocumentDB

In the AWS web interface, configure the service for DocumentDB.

### Procedure

1. Go to the [AWS Console](https://console.aws.amazon.com/).
2. From the navigation menu, click **Services**.
3. Click **All services** > **Database**.
4. On the right panel, click **Amazon DocumentDB**.
5. From the dropdown menu, select your region.
6. Click **Create**.
7. Enter a cluster identifier. Then click **Create cluster**.

## Enabling Audit Logs

You can use different methods for auditing and logging. This example uses CloudTrail because it supports all required parameters. The following events are supported for auditing in AWS.

### Procedure

1. From the AWS console, open the **Amazon DocumentDB** service.
2. In the left navigation pane, click **Parameter groups**.
3. Check which cluster parameter group is currently associated with the DocumentDB cluster.
4. If the cluster uses the default parameter group, click **Create** (or **Create parameter group**) to create a custom cluster parameter group.
5. For the new parameter group, select the family version that matches your DocumentDB cluster (for example, docdb4.0 for version 4.0.0)
6. Enter a name and description for the parameter group, then click **Create**.
7. Select the newly created parameter group and click **Edit** (or **Edit parameters**).
8. In the search box, enter `audit_logs` to locate the **audit_logs** parameter.
9. Change the **audit_logs** field value from disabled to one of the following options.
       * **enabled** / **all**: Logs all events
       * Specific event type: Enter a supported value to log only certain events (for example, `DDL` or `DML`).
10. Save the changes to the parameter group.
11. In the left navigation pane, click **Clusters**, then select the DocumentDB cluster.
12. Click **Modify**.
13. In the Cluster parameter group or Additional configuration section, select the custom parameter group that you edited.
14. Under the Scheduling of modifications section, select **Apply immediately** (or choose the appropriate maintenance window, if required).
15. Review your changes. Then click **Modify cluster** to apply the new parameter group with audit logging enabled.
16. After the cluster modification completes, Amazon DocumentDB automatically creates a CloudWatch Logs log group named: `/aws/docdb/<cluster-identifier>/audit`.

### Viewing DocumentDB log entries on CloudWatch

By default, each CloudTrail trail has an associated log group with a name in the format specified during trail creation. You can use this log group, or you can create a new one and associate it with the trail.

1. On the AWS Console page, open the **Services** menu.
2. In the search box, enter `CloudWatch`.
3. Click **CloudWatch** to open the CloudWatch dashboard.
4. In the left panel, click **Logs**.
5. Click **Log Groups**.

## Exporting CloudWatch Logs to SQS by using a Lambda function (Optional)

To achieve load balancing of audit logs between different collectors, you can export the audit logs from CloudWatch to SQS.

### Creating the SQS queue

1. Go to the [AWS Console](https://console.aws.amazon.com/).
2. Click **Services**.
3. In the search box, enter `SQS` and click **Simple Queue Services**.
4. Click **Create Queue**.
5. In the **Type** field, select **Standard**.
6. Enter a name for the queue.
7. Keep the default settings for the remaining fields.

### Creating a policy for the IAM User

Complete the following steps for the IAM user that will access the SQS logs in Guardium.

1. Go to [AWS Console](https://console.aws.amazon.com/).
2. Go to **IAM service** > **Policies** > **Create Policy**.
3. For the **Service** field, select **SQS**.
4. Select the following checkboxes: **ListQueues**, **DeleteMessage**, **DeleteMessageBatch**, **GetQueueAttributes**, **GetQueueUrl**, **ReceiveMessage**, **ChangeMessageVisibility**, and **ChangeMessageVisibilityBatch**.
5. In the **Resources** section, specify the ARN of the queue that you created in the previous step.
6. Click **Review policy** and enter the policy name.
7. Click **Create policy**.
8. Assign the policy to the user. </br>
    a. Log in to the [IAM console](https://console.aws.amazon.com/iam/).  </br>
    b. Go to **Users** on the console, and select the IAM user that you want to grant permissions. </br>
    c. In the **Permissions** tab, click **Add permissions** > **Attach existing policies directly**. </br>
    d. Select the checkbox for the policy that you created.</br>
    e. Click **Next: Review** > **Add permissions**.</br>

### Creating an IAM Role

Create an IAM role for the Lambda function. The AWS Lambda service requires permission to log events and write to the SQS queue. Create the IAM Role named **Export-DocumentDB-CloudWatch-to-SQS-Lambda** with the following policies: **AmazonSQSFullAccess**, **CloudWatchLogsFullAccess**, and **CloudWatchEventsFullAccess**.

1. Go to the [AWS Console](https://console.aws.amazon.com/).
2. Go to **IAM** > **Roles** > **Create role**.
3. For **Use case**, select **Lambda** and click **Next**.
4. Search for ``AmazonSQSFullAccess`` and select it.
5. Search for ``CloudWatchLogsFullAccess`` and select it.
6. Search for ``CloudWatchEventsFullAccess`` and select it.
7. Enter a **Role Name**. For example, `Export-DocumentDB-CloudWatch-to-SQS-Lambda`. 
8. Click **Create role**.

### Creating the Lambda Function

1. Go to the [AWS Console](https://console.aws.amazon.com/).
2. Go to **Services** and search for `Lambda function`.
3. Click **Functions** > **Create Function**
4. Keep **Author from Scratch** selected.
5. For **Function name**, enter a name. For example, **Export-DocumentDB-CloudWatch-Logs-To-SQS**.
6. For **Runtime**, select **Python 3.x**.
7. For **Permissions**, select **Use an existing role** and select the IAM role that you created in the previous step (`Export-DocumentDB-CloudWatch-to-SQS-Lambda`).
8. Click **Create function**.
9. In the **Code view**, add the function code from the DocumentDB Lambda function file (available in the plugin package).
10. Click **Configuration** > **Environment Variables**.
11. Create the following variables.
    - **Key** = `GROUP_NAME`, **Value** = The name of the CloudWatch log group whose logs you want to export (for example, `/aws/cloudtrail/DocumentDB-trail`)
    - **Key** = `QUEUE_NAME`, **Value** = The queue URL where logs are sent (for example, `https://sqs.us-east-1.amazonaws.com/1111111111/DocumentDB`)
12. Click **Save** > **Deploy**.

### Automating the Lambda function

1. Go to the CloudWatch dashboard.
2. In the navigation panel, click **Events** > **Rules**.
3. Click **Create Rule**.
4. For **Rule name**, enter a name (for example, `cloudwatchToSqs`).
5. For **Rule Type**, select **Schedule**.
6. Define the schedule. For **Schedule pattern**, select a schedule that runs at a regular rate, such as every 10 minutes.
7. Enter the rate expression (the rate at which the function is executed). This value must match the time that is specified in the lambda function code that calculates the time delta. For instance, if the function code is set to 2 minutes, set the rate to 2 minutes unless changed in the code.
8. Click **Next**.H
9. Select **Target1**. For **Target Type**, select **AWS Service**.
10. For **Target**, select **Lambda Function**.
11. Select the Lambda function that you created in the previous step (`Export-DocumentDB-CloudWatch-Logs-To-SQS`).
12. Optional: Add tags. 
13. Click **Create Rule**.

**Note:** Before you make any changes to the Lambda function code, you must disable the rule that you created. Deploy the changes and then re-enable the rule.

## Limitations

- DocumentDB Profiler logs capture database operations that take longer than some period of time (for example, 100 ms). If the threshold value is not configurable and the set value is too high, profiler logs may not be captured for every database operation.
- The following important fields cannot be mapped with DocumentDB audit or profiler logs:
    - **Source program**: Available only for aggregate queries
    - **OS User**: Not available with audit or profiler logs
    - **Client HostName**: Not available with audit or profiler logs
- Server IPs are not reported because they are not part of the audit stream. However, the `add_field` clause in the configuration adds a user-defined server host name that can be used in reports and policies.
- The sniffer saves the database name when a new session is created, not with every event. The database name is updated and populated correctly in Guardium only when a new database connection is established with a database name. If a database connection is established without a database name, the database on which the first query for that session runs is retained in Guardium, even if the user switches between databases for the same session.
- SQL errors are not supported.
- For DocumentDB over CloudWatch Kafka, logs take 15-20 minutes to appear in Guardium after they are generated by the database.

## Creating datasource profiles

You can create a new datasource profile from the Datasource Profile Management page.

### Procedure

1. Go to **Manage > Universal Connector > Datasource Profile Management**
2. Click the **➕ (Add)** button.
3. Create a profile by using one of the following methods:

    - To **Create a new profile manually**, go to the **"Add Profile"** tab and provide values for the following fields:
        - **Name** and **Description**.
        - **Plug-in Type** — Select a plug-in type from the dropdown (for example, `DocumentDB over Cloudwatch Connect 2.0`).

    - To **Upload from CSV**, go to the **"Upload from CSV"** tab and upload an exported or manually created CSV file containing one or more profiles.  
      You can also choose from the following options:
        - **Update existing profiles on name match** — Updates profiles with the same name if they already exist.
        - **Test connection for imported profiles** — Automatically tests connections after profiles are created.
        - **Use ELB** — Enables ELB support for imported profiles. You must provide the number of MUs to use in the ELB process.

**Note:** Configuration options vary based on the selected plug-in.

## Configuring DocumentDB Over CloudWatch Kafka Connect 2.0

The following table describes the fields that are specific to DocumentDB over CloudWatch Kafka Connect 2.0 plugin.

| Field                    | Description                                                                                                                                                                                                                                                         |
|--------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Name**                 | Unique name of the profile.                                                                                                                                                                                                                                         |
| **Description**          | Description of the profile.                                                                                                                                                                                                                                         |
| **Plug-in**              | Plug-in type for this profile. Select `DocumentDB Over Cloudwatch Connect 2.0`. A full list of available plug-ins are available on the Package Management page.                                                                                        |
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

## Testing a connection

After you create a profile, test the connection to ensure that the configuration is valid.

### Procedure

1. Select the profile.
2. From the top menu, click **Test Connection**.
3. If the test is successful, you can install the profile.

---

## Installing a profile

After the connection test is successful, you can install the profile on Managed Units (MUs) or Edges. The parsed audit logs are sent to the selected Managed Unit or Edge to be consumed by the Sniffer.

### Procedure

1. Select the profile.
2. From the **Install** menu, click **Install**.
3. From the list of available MUs and Edges, select the ones where you want to deploy the profile.

---

## Uninstalling or reinstalling profiles

You can uninstall or reinstall an installed profile.

### Procedure

1. Select the profile.
2. Click **Uninstall** or **Reinstall**.

---
