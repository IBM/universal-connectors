# Configuring Aurora-MySQL datasource profile for Kafka Connect plug-ins

Create and configure datasource profiles through central manager for **Aurora-MySQL
over CloudWatch Kafka Connect** plug-ins.

### Meet Aurora-MySQL over CloudWatch Connect

* Environments: AWS
* Supported inputs: Kafka Input (pull)
* Supported Guardium versions:
    * Guardium Data Protection: Appliance bundle 12.2.2 or later

Kafka-connect is a framework for streaming data between Apache Kafka and other systems. This connector enables
monitoring of Aurora-MySQL audit logs through CloudWatch.

## Enabling auditing for Aurora-MySQL

### Creating a database

1. Go to https://console.aws.amazon.com/.
2. Click **Services**.
3. In the **Database** section, click **RDS**.
4. From the **Region** dropdown menu, select your region where you want to create the databse instance.
5. In the central panel of the Amazon RDS dashboard, click **Create database**.
6. Choose a database creation method.
7. In the **Engine** field, select **Amazon Aurora**, then select **Amazon Aurora MySQL-Compatible Edition**.
8. Select a capacity type (Provisioned).
9. Select a template (Production or Dev/Test).
10. In the **Settings** section, enter the database instance name and create the master account with a username and password to log in to the database.
11. Select the database instance size according to your requirements.
12. Select appropriate storage options. For example, you can enable auto scaling.
13. Select the **Availability** and **Durability** options.
14. Select the connectivity settings that are appropriate for your environment. To make the database accessible, set the **Public access** option to **Publicly Accessible within Additional Configuration**. 
15. Select the **Authentication type** for the database (choose from Password Authentication, Password and IAM database authentication, or Password and Kerberos authentication).
16. Expand the **Additional Configuration** options and complete the following steps. </br>
      a. Configure the database options.  </br>
      b. Select a DB cluster parameter group.  </br>
      c. Select options for Backup. </br>
      d. If desired, enable **Encryption** on the database instances. </br>
      e. In **Log exports**, select the log types to publish to Amazon CloudWatch (Audit log). </br>
      f. Select the options for **Deletion protection**.  </br>
17. Click **Create Database**.
18. To view the database, click **Databases** under Amazon RDS in the left panel. 
19. To authorize inbound traffic, edit the security group.  </br>
      a. In the database summary page, select the **Connectivity and Security** tab. Under **Security**, click **VPC security group**.  </br>
      b. Click the group name you selected while creating the database (each database has one active group).  </br>
      c. In the **Inbound rules** section, choose to edit the inbound rules and set the following rule. </br>
      &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;- **Type**: MYSQL/Aurora </br>
      &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;- **Protocol**: TCP </br>
      &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;- **Port Range**: 3306 (depending on your requirements, the source can be set to a specific IP address or opened to all hosts)  </br>
      d. Click **Add Rule** and then click **Save changes**. You may need to restart the database. </br>

### Creating a parameter group

1. Click **Parameter Groups**, then click **Create Parameter Groups**.
2. Provide the following details. </br>
      a. **Parameter group family**: Select the Aurora MySQL version</br>
      b. **Type**: DB cluster parameter group</br>
      c. **Group name**: Enter a name for the group</br>
      d. **Description**: Enter a description</br>
    
3. Click **Create**.
4. Click **DB Parameter > Parameter group actions > Edit**.
5. Change the parameter values by adding the following settings.</br>
      a. **server_audit_events** = ``CONNECT,QUERY_DCL,QUERY_DDL,QUERY_DML``</br>
      b. **server_audit_excl_users** = ``rdsadmin``</br>
      c. **server_audit_logging** = ``1``</br>
      d. **server_audit_logs_upload** = ``1``</br>
      e. **log_output** = ``FILE``</br>
6. Click **Save changes**.
7. Go to **Database Clusters**, then click **Modify**.
8. Go to **Additional Configurations > Database options**.
9. Change the DB clustor parameter group.
10. Click **Continue**, then select **Apply immediately**.
11. Click **Modify Cluster**.
12. Reboot the DB cluster for the changes to take effect.

## Viewing the logs entries on Cloudwatch

By default, each database instance has an associated log group with a name in this format: ``/aws/rds/instance/<instance_name>/aurora-mysqlql``. You can use this log group, or you can create a new one and associate it with the database instance.

### Procedure

1. On the AWS Console page, open the **Services** menu.
2. Enter the CloudWatch string in the search box.
3. Click **CloudWatch** to redirect to the CloudWatch dashboard.
4. In the left panel, select **Logs**.
5. Click **Log Groups**.

Go to Cloudwatch from the search box and find the details of the generated logs (UserActivity/Connection) in the following log groups:
   - `/aws/rds/cluster/<cluster_name>/audit`
   - `/aws/rds/cluster/<cluster_name>/error`

## Limitations

1. The aurora-mysql plug-in does not support IPV6.
2. The aurora-mysql auditing does not audit **Procedure**, **Function**, and **Show** table operations.
3. **Source Program** appears blank in report.
4. Syntactically incorrect queries are not captured in audit logs.


## Creating datasource profiles

You can create a new datasource profile from the **Datasource Profile Management** page.

### Procedure

1. Go to **Manage > Universal Connector > Datasource Profile Management**
2. Click the **➕ (Add)** button.
3. You can create a profile by using one of the following methods:

    - To **Create a new profile manually**, go to the **"Add Profile"** tab and provide values for the following fields.
        - **Name** and **Description**.
        - Select a **Plug-in Type** from the dropdown. For example, `AWS Aurora MySQL Over Cloudwatch Connect 2.0`.

    - To **Upload from CSV**, go to the **"Upload from CSV"** tab and upload an exported or manually created CSV file
      containing one or more profiles.  
      You can also choose from the following options:
        - **Update existing profiles on name match** — Updates profiles with the same name if they already exist.
        - **Test connection for imported profiles** — Automatically tests connections after profiles are created.
        - **Use ELB** — Enables ELB support for imported profiles. You must provide the number of MUs to be used in the
          ELB process.

**Note:** Configuration options vary based on the selected plug-in.

## Configuring Aurora-MySQL Over CloudWatch Kafka Connect 2.0

The following table describes the fields that are specific to Aurora-MySQL over CloudWatch Kafka Connect 2.0 plugin.

| Field                                   | Description                                                                                                                                                                                                                                                                                                          |
|-----------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Name**                                | Unique name of the profile.                                                                                                                                                                                                                                                                                          |
| **Description**                         | Description of the profile.                                                                                                                                                                                                                                                                                          |
| **Plug-in**                             | Plug-in type for this profile. Select **AWS Aurora MySQL Over Cloudwatch Connect 2.0**. A full list of available plug-ins are available on the **Package Management** page.                                                                                                                                                    |
| **Credential**                          | Select AWS Credentials or AWS Role ARN. The credential to authenticate with AWS. Must be created in **Credential Management**, or click **➕** to create one. For more information, see [Creating Credentials](https://www.ibm.com/docs/en/SSMPHH_12.x/com.ibm.guardium.doc.stap/guc/guc_credential_management.html). |
| **Kafka Cluster**                       | Select the appropriate Kafka cluster from the available Kafka cluster list or create a new Kafka cluster. For more information, see [Managing Kafka clusters](https://www.ibm.com/docs/en/SSMPHH_12.x/com.ibm.guardium.doc.stap/guc/guc_kafka_cluster_management.html).                                              |
| **Label**                               | Grouping label. For example, customer name or ID.                                                                                                                                                                                                                                                                    |
| **AWS account region**                  | Specifies the AWS region where your Aurora-MySQL instance is located (e.g., us-east-1, eu-west-1).                                                                                                                                                                                                                       |
| **Log groups**                          | List of CloudWatch log groups to monitor. These are the log groups where Aurora-MySQL audit logs are exported.                                                                                                                                                                                                           |
| **Filter pattern**                      | CloudWatch Logs filter pattern to apply. Use "None" to retrieve all logs, or specify a pattern to filter specific log events.                                                                                                                                                                                        |
| **Account ID**                          | Your AWS account ID (12-digit number). This identifies your AWS account.                                                                                                                                                                                                                                             |
| **Cluster name**                        | The name of your Aurora-MySQL cluster or instance identifier.                                                                                                                                                                                                                                                            |
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

