# Configuring S3 datasource profile for Kafka Connect plug-ins

Create and configure datasource profiles through central manager for **AWS S3
over CloudWatch Kafka Connect** plug-ins.

### Meet S3 over CloudWatch Connect

* Environments: AWS
* Supported inputs: Kafka Input (pull)
* Supported Guardium versions:
    * Guardium Data Protection: Appliance bundle 12.2.2 or later

Kafka-connect is a framework for streaming data between Apache Kafka and other systems. This connector enables
monitoring of S3 audit logs through CloudWatch.

## Configuring Amazon AWS CloudTrail to send S3 log files to CloudWatch

There are different methods for auditing and logging. CloudTrail is used for this example as it supports all required parameters.

### Procedure

1. Go to https://console.aws.amazon.com/cloudtrail.
2. Click **Trails** in the left menu.
3. Click **Create trail** and enter the trail name.
4. Under **Storage location**, verify that **Create new S3 bucket** is selected.
5. Under **Log file SSE-KMS encryption**, clear the **Enabled** box.
6. Under **CloudWatch Logs**, check the **Enabled** box.
7. Verify **New** is selected for **Log group**.
8. Under **Log group name**, provide a new log group name.
9. Verify **New** is selected for **IAM Role**.
10. For **Role name**, provide a new role name.
11. Click **Next**.
12. For **Event type**, select **Management events** and **Data events**.
13. Verify that **Read** and **Write** are selected for **API Activity**.
14. In the **Data Events** section, click **Switch to basic event selectors**.
15. Click **Add data event type** > **Data event source** and then select **S3**.
16. Select the S3 buckets that you want to monitor.
17. Click **Next**.
18. Verify that all parameters shown are correct. Then click **Create trail**.

### Viewing S3 log entries on CloudWatch

By default, each CloudTrail trail has an associated log group with a name in the format specified during trail creation. You can use this log group, or you can create a new one and associate it with the trail.

1. On the AWS Console page, open the **Services** menu.
2. Enter `CloudWatch` in the search box.
3. Click **CloudWatch** to redirect to the CloudWatch dashboard.
4. In the left panel, select **Logs**.
5. Click **Log Groups**.


## Creating datasource profiles

You can create a new datasource profile from the **Datasource Profile Management** page.

### Procedure

1. Go to **Manage > Universal Connector > Datasource Profile Management**
2. Click the **➕ (Add)** button.
3. You can create a profile by using one of the following methods:

    - To **Create a new profile manually**, go to the **"Add Profile"** tab and provide values for the following fields.
        - **Name** and **Description**.
        - Select a **Plug-in Type** from the dropdown. For example, `AWS S3 Over Cloudwatch Connect 2.0`.

    - To **Upload from CSV**, go to the **"Upload from CSV"** tab and upload an exported or manually created CSV file
      containing one or more profiles.  
      You can also choose from the following options:
        - **Update existing profiles on name match** — Updates profiles with the same name if they already exist.
        - **Test connection for imported profiles** — Automatically tests connections after profiles are created.
        - **Use ELB** — Enables ELB support for imported profiles. You must provide the number of MUs to be used in the
          ELB process.

**Note:** Configuration options vary based on the selected plug-in.

## Configuring S3 Over CloudWatch Kafka Connect 2.0

The following table describes the fields that are specific to S3 over CloudWatch Kafka Connect 2.0 plugin.

| Field                                   | Description                                                                                                                                                                                                                                                                                                          |
|-----------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Name**                                | Unique name of the profile.                                                                                                                                                                                                                                                                                          |
| **Description**                         | Description of the profile.                                                                                                                                                                                                                                                                                          |
| **Plug-in**                             | Plug-in type for this profile. Select **AWS S3 Over Cloudwatch Connect 2.0**. A full list of available plug-ins are available on the **Package Management** page.                                                                                                                                                    |
| **Credential**                          | Select AWS Credentials or AWS Role ARN. The credential to authenticate with AWS. Must be created in **Credential Management**, or click **➕** to create one. For more information, see [Creating Credentials](https://www.ibm.com/docs/en/SSMPHH_12.x/com.ibm.guardium.doc.stap/guc/guc_credential_management.html). |
| **Kafka Cluster**                       | Select the appropriate Kafka cluster from the available Kafka cluster list or create a new Kafka cluster. For more information, see [Managing Kafka clusters](https://www.ibm.com/docs/en/SSMPHH_12.x/com.ibm.guardium.doc.stap/guc/guc_kafka_cluster_management.html).                                              |
| **Label**                               | Grouping label. For example, customer name or ID.                                                                                                                                                                                                                                                                    |
| **AWS account region**                  | Specifies the AWS region where your S3 buckets are located (e.g., us-east-1, eu-west-1).                                                                                                                                                                                                                            |
| **Log groups**                          | List of CloudWatch log groups to monitor. These are the log groups where S3 audit logs (via CloudTrail) are exported. Format: `/aws/cloudtrail/<trail_name>`                                                                                                                                                         |
| **Filter pattern**                      | CloudWatch Logs filter pattern to apply. Use "None" to retrieve all logs, or specify a pattern to filter specific log events.                                                                                                                                                                                        |
| **Account ID**                          | Your AWS account ID (12-digit number). This identifies your AWS account.                                                                                                                                                                                                                                             |
| **Cluster name**                        | The name of your S3 bucket or identifier used to distinguish this data source.                                                                                                                                                                                                                                       |
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
