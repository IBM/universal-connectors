# Configuring RDS MySql datasource profiles for Kafka Connect Plug-ins

Create and configure datasource profiles through Central Manager for **RDS MySql
over CloudWatch Kafka Connect** plug-ins.

### Meet RDS MySql over CloudWatch Connect

* Environments: AWS
* Supported inputs: Kafka Input (pull)
* Supported Guardium versions:
    * Guardium Data Protection: Appliance bundle 12.2.2 or later

Kafka-connect is a framework for streaming data between Apache Kafka and other systems. This connector enables
monitoring of Redshift audit logs through CloudWatch.

***Note:***
* The client source program is not available in messages sent by MySQL. This data is sent only in the first audit log message upon database connection and the filter plug-in doesn't aggregate data from different messages.
* The **type** field should be the same in both the input and filter sections in the Logstash configuration file. This field should be unique for every individual connector added.
* GDP requires installation of [json_encode](https://www.elastic.co/guide/en/logstash-versioned-plugins/current/v3.0.3-plugins-filters-json_encode.html) filter plug-in. If you are on Guardium Data Protection v12.0 and later, you can skip this step.
* On the reports page, the `use` statement does not display the account ID in the **Database Name** column.

## Creating and configuring a MySQL database instance

### Creating a MySQL database instance

To create a new MySQL instance, see [Create and Connect to a MySQL Database with Amazon RDS](https://aws.amazon.com/getting-started/hands-on/create-mysql-db/). When setting the properties under **Additional Configuration**, in the Log exports section select **Audit log** and **Error log** as the log types to publish to Amazon.


### Creating a new parameter group

1. Open the Amazon RDS console (https://console.aws.amazon.com/rds).
2. In the navigation pane, choose **Parameter groups**.
3. Choose **Create parameter group** to open the Create parameter group dialog box.
4. In the **Parameter group family** list, choose your engine version.
5. In the **Group name** box, enter the name of the new DB parameter group.
6. In the **Description** box, enter a description for the new DB parameter group.
7. Click **Create**.
8. Go back to **Parameter groups** from the navigation pane.
9. In the **Parameter groups list**, choose the parameter group that you just created.
10. Click **Parameter group actions** > **Edit**.
11. Use the **Filter parameters** field to search for the **log_output** parameter.
12. Set the value of the **log_output** parameter to `FILE`.
13. Click **Save changes**.

## Enabling Auditing

1. Click **Parameter Groups** > **Create Parameter Groups**.
2. Provide the following details:
   • Parameter group family: Provide aurora-mysql version
   • Type: DB cluster parameter group
   • Group name: Name of Group
   • Description: Privide description
3. Click **Create**.
4. Select **DB Parameter** > **Parameter group actions** > **Edit**.
5. Update the value of the parameters and add the following settings:
   • **server_audit_events** = `CONNECT,QUERY_DCL,QUERY_DDL,QUERY_DML`
   • **server_audit_excl_users** =	`rdsadmin`
   • **server_audit_logging**	= `1`
   • **server_audit_logs_upload**	= `1`
   • **log_output** = `FILE`
6. Click **Save changes**.
7. Go to **Database Clustor** > **Modify** > **Additional Configuration** > **Database options**.
8. Change the DB clustor parameter group.
8. Click **Continue** > **Apply immediately**.
10. Click **Modify Cluster**.
11. Reboot the DB Cluster for the changes to take effect.

## Viewing the Audit logs

The audit logs can be seen in log files in RDS, and also on CloudWatch.

 ### Viewing the auditing details in RDS log files

The RDS log files can be viewed, watched, and downloaded. The name of the RDS log file is modifiable and is controlled
by the **log_filename** parameter.

1. Go to **Services** > **Database** > **RDS** > **Databases**.
2. Select the database instance.
3. Select the **Logs & Events** section.
4. The end of the Logs section lists the files that contain the auditing details. The newest file is on the last page.

### Viewing the logs entries on CloudWatch

By default, each database instance has an associated log group. You can use this log group, or you can create a new one and associate it with the database
instance.

1. On the AWS Console page, open the **Services** menu.
2. Enter the CloudWatch string in the search box.
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
        - Select a **Plug-in Type** from the dropdown. For example, `AWS RDS MySql Over Cloudwatch Connect 2.0`.

    - To **Upload from CSV**, go to the **"Upload from CSV"** tab and upload an exported or manually created CSV file
      containing one or more profiles.  
      You can also choose from the following options:
        - **Update existing profiles on name match** — Updates profiles with the same name if they already exist.
        - **Test connection for imported profiles** — Automatically tests connections after profiles are created.
        - **Use ELB** — Enables ELB support for imported profiles. You must provide the number of MUs to be used in the
          ELB process.

**Note:** Configuration options vary based on the selected plug-in.

## Configuring AWS RDS MySql Over CloudWatch Kafka Connect 2.0

The following table describes the fields that are specific to Redshift over CloudWatch Kafka Connect 2.0 plugin.

| Field                                   | Description                                                                                                                                                                                                                                                                                                          |
|-----------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Name**                                | Unique name of the profile.                                                                                                                                                                                                                                                                                          |
| **Description**                         | Description of the profile.                                                                                                                                                                                                                                                                                          |
| **Plug-in**                             | Plug-in type for this profile. Select `AWS RDS MySql Over Cloudwatch Connect 2.0`. A full list of available plug-ins are available on the **Package Management** page.                                                                                                                                               |
| **Credential**                          | Select AWS Credentials or AWS Role ARN. The credential to authenticate with AWS. Must be created in **Credential Management**, or click **➕** to create one. For more information, see [Creating Credentials](https://www.ibm.com/docs/en/SSMPHH_12.x/com.ibm.guardium.doc.stap/guc/guc_credential_management.html). |
| **Kafka Cluster**                       | Select the appropriate Kafka cluster from the available Kafka cluster list or create a new Kafka cluster. For more information, see [Managing Kafka clusters](https://www.ibm.com/docs/en/SSMPHH_12.x/com.ibm.guardium.doc.stap/guc/guc_kafka_cluster_management.html).                                              |
| **Label**                               | Grouping label. For example, customer name or ID.                                                                                                                                                                                                                                                                    |
| **AWS account region**                  | Specifies the AWS region where your Redshift instance is located (e.g., us-east-1, eu-west-1).                                                                                                                                                                                                                       |
| **Log groups**                          | List of CloudWatch log groups to monitor. These are the log groups where Redshift audit logs are exported.                                                                                                                                                                                                           |
| **Filter pattern**                      | CloudWatch Logs filter pattern to apply. Use "None" to retrieve all logs, or specify a pattern to filter specific log events.                                                                                                                                                                                        |
| **Account ID**                          | Your AWS account ID (12-digit number). This identifies your AWS account.                                                                                                                                                                                                                                             |
| **Cluster name**                        | The name of your Redshift cluster or instance identifier.                                                                                                                                                                                                                                                            |
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

