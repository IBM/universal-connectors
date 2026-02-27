# Configuring Aurora Postgres datasource profile for Kafka Connect plug-ins

Create and configure datasource profiles through Central Manager for **Aurora Postgres
over CloudWatch Kafka Connect** plug-ins.

### Meet Aurora Postgres over CloudWatch Connect

* Environments: AWS
* Supported inputs: Kafka Input (pull)
* Supported Guardium versions:
    * Guardium Data Protection: Appliance bundle 12.2.2 or later

Kafka-connect is a framework for streaming data between Apache Kafka and other systems. This connector enables
monitoring of Aurora Postgres audit logs through CloudWatch.

## Enabling auditing for Aurora Postgres

### Creating a database

1. Go to https://console.aws.amazon.com/.
2. Click **Services**.
3. In the **Database** section, click **RDS**.
4. From the **Region** dropdown menu, select your region where you want to create the databse instance.
5. In the central panel of the Amazon RDS dashboard, click **Create database**.
6. Choose a database creation method.
7. In the **Engine** field, select **PostgreSQL**, and then select the appropriate version.
8. Select a template (Production, Dev/Test, or Free Tier).
9. In the **Settings** section, enter the database instance name and create the master account with a username and password to log in to the database.
10. Select the database instance size according to your requirements.
11. Select appropriate storage options. For example, you can enable auto scaling.
12. Select the **Availability** and **Durability** options.
13. Select the connectivity settings that are appropriate for your environment. To make the database accessible, set the **Public access** option to **Publicly Accessible within Additional Configuration**. 
14. Select the **Authentication type** for the database (choose from Password Authentication, Password and IAM database authentication, or Password and Kerberos authentication).
15. Expand the **Additional Configuration** options and complete the following steps. </br>
      a. Configure the database options.  </br>
      b. Select a DB cluster parameter group.  </br>
      c. Select options for Backup. </br>
      d. Optional: Enable **Encryption** on the database instances. </br>
      e. In **Log exports**, select the **Postgresql** log type to publish to Amazon CloudWatch. </br>
      f. Select the options for **Deletion protection**.  </br>
16. Click **Create Database**.
17. To view the database, click **Databases** under Amazon RDS in the left panel. 
18. To authorize inbound traffic, edit the security group.  </br>
      a. In the database summary page, select the **Connectivity and Security** tab. Under **Security**, click **VPC security group**.  </br>
      b. Click the group name you selected while creating the database (each database has one active group).  </br>
      c. In the **Inbound rules** section, choose to edit the inbound rules and set the following rule. </br>
      &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;- **Type**: PostgreSQL </br>
      &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;- **Protocol**: TCP </br>
      &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;- **Port Range**: 5432 </br>
         &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;**Note:** Depending on your requirements, the source can be set to a specific IP address or it can be opened to all hosts.</br>
      d. Click **Add Rule** and then click **Save changes**. You may need to restart the database. </br>


## Enabling the PGAudit extension

There are different ways to audit and log in PostgreSQL. In this procedure, we will use **PGAudit**, the open-source audit logging extension for PostgreSQL 9.5+. This extension supports logging for sessions or objects.

**Note:** Configure either **Session Auditing** or **Object Auditing**. You cannot enable both at the same time.

### 1. Create a database parameter group. 
   
When you create a database instance, it is associated with the default parameter group. To create a new database parameter group, complete the following steps. 

1. Go to **Services** > **Database** > **Parameter groups** 
2. From the left panel, click **Create Parameter Group**.
3. Enter the parameter group details.</br>
      a. Select the parameter group family. For example, **aurora-postgres12**. This version should match the version of the database you created and with which this parameter group will be associated. </br>
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
   
## Viewing the logs entries on Cloudwatch

1. On the AWS Console page, open the **Services** menu.
2. Enter the CloudWatch string in the search box.
3. Click **CloudWatch** to redirect to the CloudWatch dashboard.
4. In the left panel, select **Logs**.
5. Click **Log Groups**.

Go to Cloudwatch from the search box and find the details of the generated logs (UserActivity/Connection) in the `/aws/rds/cluster/<cluster_name>/postgresql` log group.


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

## Configuring Aurora Postgres Over CloudWatch Kafka Connect 2.0

The following table describes the fields that are specific to Aurora Postgres over CloudWatch Kafka Connect 2.0 plugin.

| Field                                   | Description                                                                                                                                                                                                                                                                                                          |
|-----------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Name**                                | Unique name of the profile.                                                                                                                                                                                                                                                                                          |
| **Description**                         | Description of the profile.                                                                                                                                                                                                                                                                                          |
| **Plug-in**                             | Plug-in type for this profile. Select `AWS Postgres Over Cloudwatch Connect 2.0`. A full list of available plug-ins are available on the **Package Management** page.                                                                                                                                                    |
| **Credential**                          | Select AWS Credentials or AWS Role ARN. The credential to authenticate with AWS. Must be created in **Credential Management**, or click **➕** to create one. For more information, see [Creating Credentials](https://www.ibm.com/docs/en/SSMPHH_12.x/com.ibm.guardium.doc.stap/guc/guc_credential_management.html). |
| **Kafka Cluster**                       | Select the appropriate Kafka cluster from the available Kafka cluster list or create a new Kafka cluster. For more information, see [Managing Kafka clusters](https://www.ibm.com/docs/en/SSMPHH_12.x/com.ibm.guardium.doc.stap/guc/guc_kafka_cluster_management.html).                                              |
| **Label**                               | Grouping label. For example, customer name or ID.                                                                                                                                                                                                                                                                    |
| **AWS account region**                  | Specifies the AWS region where your Aurora Postgres instance is located (e.g., us-east-1, eu-west-1).                                                                                                                                                                                                                       |
| **Log groups**                          | List of CloudWatch log groups to monitor. These are the log groups where Aurora Postgres audit logs are exported.                                                                                                                                                                                                           |
| **Filter pattern**                      | CloudWatch Logs filter pattern to apply. Use "None" to retrieve all logs, or specify a pattern to filter specific log events.                                                                                                                                                                                        |
| **Account ID**                          | Your AWS account ID (12-digit number). This identifies your AWS account.                                                                                                                                                                                                                                             |
| **Cluster name**                        | The name of your Aurora Postgres cluster or instance identifier.                                                                                                                                                                                                                                                            |
| **Ingestion delay (seconds)**           | Default value is 900 seconds (15 minutes). This delay accounts for the time it takes for logs to be available in CloudWatch after being generated.                                                                                                                                                                   |
| **No-traffic threshold (minutes)**      | Default value is 60. If there is no incoming traffic for an hour, S-TAP displays a red status. Once incoming traffic resumes, the status returns to green.                                                                                                                                                           |
| **Unmask sensitive value**              | Optional boolean flag. When enabled, sensitive values in the audit logs are not masked.                                                                                                                                                                                                                          |
| **Use Enterprise Load Balancing (ELB)** | Enable this if ELB support is required.                                                                                                                                                                                                                                                                              |
| **Managed Unit Count**                  | Number of Managed Units (MUs) to allocate for ELB.                                                                                                                                                                                                                                                                   |

**Note:**

- Ensure that the **profile name** is unique.
- Required credentials must be created before or during profile creation.
- The AWS credentials must have appropriate permissions to read CloudWatch logs.

---

## Testing a Connection

After creating a profile, you must test the connection to make sure that the provided configuration is valid.

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

