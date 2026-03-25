# Configuring Oracle RDS datasource profiles for Kafka Connect Plug-ins

Create and configure datasource profiles through Central Manager for **Oracle RDS over CloudWatch Kafka Connect** plug-ins.

### Meet Oracle RDS over CloudWatch Connect

* Environments: AWS
* Supported inputs: Kafka Input (pull)
* Supported Guardium versions:
   * Guardium Data Protection: Appliance bundle 12.2.2 or later

Kafka-connect is a framework for streaming data between Apache Kafka and other systems. This connector enables monitoring of Oracle RDS audit logs through CloudWatch.

## Configuring AWS RDS Oracle

For detailed instructions on creating and configuring an AWS RDS Oracle database instance, see [Creating an Oracle DB instance and connecting to a database on an Oracle DB instance](https://docs.aws.amazon.com/AmazonRDS/latest/UserGuide/CHAP_GettingStarted.CreatingConnecting.Oracle.html#CHAP_GettingStarted.Creating.Oracle).

### Additional configuration requirements

After creating your Oracle RDS instance following the Amazon documentation, make sure you complete the following steps.

1. **Enable CloudWatch Log Exports:**
   - In the Additional Configuration section, under Log exports, select the log type '**Audit**' from Amazon CloudWatch log options.
   - Click on **Add Rule** and **Save** changes.
   - **Note:** You might need to restart the database for changes to take effect.

2. **Configure Security Group:**
   - Edit the VPC security group associated with your database instance to allow traffic on port **1521**.
   - In the **Inbound Rules** section, add a rule with:
     - Type: Oracle-RDS
     - Protocol: TCP
     - Port Range: 1521
     - Source: Configure based on your security requirements (specific IP address or IP range)

## Enabling Auditing

### Configuring parameter group

1. Enable auditing by setting up parameters on the parameter group and associating them with the database instance.
   a. Select **Parameter Groups** from the left pane on Amazon RDS.
   b. Select the newly created parameter group.
   c. Click **Edit parameters** on the right corner.
   d. Add the following setting:

   ```
   audit_trail = XML, EXTENDED
   ```

### Associating DB parameter group to database instance

1. Click **RDS** > **Databases** from the left panel.
2. Select the **Oracle database** instance to be updated. Then click **Modify**.
4. In the **Additional Configuration** section, under database options, select the newly created group from the **DB Parameter Group** drop-down.
5. Click **Continue**.
6. Select the database instance that, in its configuration section, shows the status for the DB Parameter Group as **pending-reboot**.
7. Reboot the Database instance for the changes to take effect.

### Applying Audit Policies

To allow the connector to parse and analyze your database queries, you must enable **Traditional Auditing** using the `AUDIT` command. Traditional Auditing records are exported to CloudWatch, making them accessible to this connector.

**Important:** Do **not** use `CREATE AUDIT POLICY` (Unified Auditing). Unified Auditing records are stored inside the database and cannot be exported to CloudWatch. You must use the Traditional Auditing commands shown below.

Connect to your Oracle RDS database using your master user (e.g., `ADMIN`) via a SQL client such as SQL*Plus, SQL Developer, or any JDBC-based tool. Depending on your security and compliance requirements, run the appropriate commands below.

For more information about Traditional Auditing, see:
- [AWS Blog: Security Auditing in Amazon RDS for Oracle](https://aws.amazon.com/blogs/database/part-1-security-auditing-in-amazon-rds-for-oracle/)
- [Oracle Documentation: AUDIT (Traditional Auditing)](https://docs.oracle.com/en/database/oracle/oracle-database/19/sqlrf/AUDIT-Traditional-Auditing.html)

#### Option 1: Audit all data modifications globally (All Users, All Tables)

**Note:** This generates high log volume. It captures reads/writes on any table by any user.

```sql
-- Capture all reads
AUDIT SELECT ANY TABLE BY ACCESS;

-- Capture all writes
AUDIT INSERT ANY TABLE BY ACCESS;
AUDIT UPDATE ANY TABLE BY ACCESS;
AUDIT DELETE ANY TABLE BY ACCESS;

-- Capture stored procedure executions
AUDIT EXECUTE ANY PROCEDURE BY ACCESS;
```

#### Option 2: Audit specific schema operations

To audit operations on a specific schema (e.g., `MYSCHEMA`):

```sql
-- Audit all operations on tables in MYSCHEMA
AUDIT SELECT TABLE, INSERT TABLE, UPDATE TABLE, DELETE TABLE BY MYSCHEMA BY ACCESS;

-- Audit procedure executions in MYSCHEMA
AUDIT EXECUTE PROCEDURE BY MYSCHEMA BY ACCESS;
```

#### Option 3: Audit specific tables

To audit operations on specific tables:

```sql
-- Audit a specific table
AUDIT SELECT, INSERT, UPDATE, DELETE ON MYSCHEMA.MYTABLE BY ACCESS;

-- Audit multiple specific tables
AUDIT SELECT, INSERT, UPDATE, DELETE ON MYSCHEMA.CUSTOMERS BY ACCESS;
AUDIT SELECT, INSERT, UPDATE, DELETE ON MYSCHEMA.ORDERS BY ACCESS;
```

#### Option 4: Audit session events (Login/Logout)

To capture login and logout events:

```sql
-- Audit all session connections
AUDIT SESSION BY ACCESS;

-- Audit only failed login attempts
AUDIT SESSION WHENEVER NOT SUCCESSFUL;
```

#### Option 5: Audit DDL statements

To capture Data Definition Language (DDL) operations:

```sql
-- Audit all DDL statements
AUDIT TABLE BY ACCESS;
AUDIT VIEW BY ACCESS;
AUDIT PROCEDURE BY ACCESS;
```

#### To disable auditing

To stop auditing specific operations:

```sql
-- Disable global auditing
NOAUDIT SELECT ANY TABLE;
NOAUDIT INSERT ANY TABLE;
NOAUDIT UPDATE ANY TABLE;
NOAUDIT DELETE ANY TABLE;

-- Stop auditing procedure executions
NOAUDIT EXECUTE ANY PROCEDURE;

-- Disable auditing on specific tables
NOAUDIT SELECT, INSERT, UPDATE, DELETE ON MYSCHEMA.MYTABLE;

-- Disable session auditing
NOAUDIT SESSION;
```

#### To check current audit settings

To view which audit options are currently enabled:

```sql
-- View global statement audit options
SELECT * FROM DBA_STMT_AUDIT_OPTS;

-- View object-specific audit options
SELECT * FROM DBA_OBJ_AUDIT_OPTS;
```

#### To view audit trail records

To query the audit trail (note: these records are also exported to CloudWatch):

```sql
-- View recent audit records
SELECT EXTENDED_TIMESTAMP, DB_USER, ACTION, OBJECT_NAME, SQL_TEXT
FROM V$XML_AUDIT_TRAIL
WHERE DB_USER NOT IN ('SYS', 'SYSTEM', 'RDSADMIN')
  AND DB_USER != '/'  -- Exclude internal user
  AND DB_USER IS NOT NULL
ORDER BY EXTENDED_TIMESTAMP DESC
```


## Limitations

1. **Data Ingestion Delay**: There will be a delay in data being observed for reports due to limitations of the Oracle RDS DB instance and CloudWatch log availability.

2. **Filtered System Users and Operations**: To avoid unnecessary logging and reduce noise, the connector automatically filters out audit records from the following system users and operations:
   - **System Users**: Records where `Object_Schema`, `Current_User`, or `DB_User` is `SYS`, `AUDSYS`, or `RDSADMIN`
   - **DBMS_OUTPUT Operations**: Any SQL statements containing `DBMS_OUTPUT` calls
   - **Empty or Invalid SQL**: Records with empty SQL text or containing only `/`
   - **Records without Content**: Records missing both `Comment_Text` and `Sql_Text`
   
   These filters help focus on application-level database activities and reduce the volume of system-generated audit logs.

## Creating datasource profiles

You can create a new datasource profile from the **Datasource Profile Management** page.

### Procedure

1. Go to **Manage > Universal Connector > Datasource Profile Management**
2. Click the **➕ (Add)** button.
3. You can create a profile by using one of the following methods:

    - To **Create a new profile manually**, go to the **"Add Profile"** tab and provide values for the following fields.
        - **Name** and **Description**.
        - Select a **Plug-in Type** from the dropdown. For example, `Amazon RDS Oracle Over Cloudwatch Connect 2.0`.

    - To **Upload from CSV**, go to the **"Upload from CSV"** tab and upload an exported or manually created CSV file containing one or more profiles.  
      You can also choose from the following options:
        - **Update existing profiles on name match** — Updates profiles with the same name if they already exist.
        - **Test connection for imported profiles** — Automatically tests connections after profiles are created.
        - **Use ELB** — Enables ELB support for imported profiles. You must provide the number of MUs to be used in the ELB process.

**Note:** Configuration options vary based on the selected plug-in.

## Configuring Oracle RDS Over CloudWatch Kafka Connect 2.0

The following table describes the fields that are specific to Oracle RDS over CloudWatch Kafka Connect 2.0 plugin.

| Field                    | Description                                                                                                                                                                                                                                                         |
|--------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Name**                 | Unique name of the profile.                                                                                                                                                                                                                                         |
| **Description**          | Description of the profile.                                                                                                                                                                                                                                         |
| **Plug-in**              | Plug-in type for this profile. Select `Amazon RDS Oracle Over Cloudwatch Connect 2.0`. A full list of available plug-ins are available on the **Package Management** page.                                                                                         |
| **Credential**           | Select AWS Credentials or AWS Role ARN. The credential to authenticate with AWS. Must be created in **Credential Management**, or click **➕** to create one. For more information, see [Creating Credentials](https://www.ibm.com/docs/en/SSMPHH_12.x/com.ibm.guardium.doc.stap/guc/guc_credential_management.html). |
| **Kafka Cluster**        | Select the appropriate Kafka cluster from the available Kafka cluster list or create a new Kafka cluster. For more information, see [Managing Kafka clusters](https://www.ibm.com/docs/en/SSMPHH_12.x/com.ibm.guardium.doc.stap/guc/guc_kafka_cluster_management.html). |
| **Label**                | Grouping label. For example, customer name or ID.                                                                                                                                                                                                                   |
| **AWS account region**   | Specifies the AWS region where your RDS Oracle instance is located (e.g., us-east-1, eu-west-1).                                                                                                                                                                   |
| **Log groups**           | List of CloudWatch log groups to monitor. These are the log groups where Oracle audit logs are exported.                                                                                                                                                           |
| **Filter pattern**       | CloudWatch Logs filter pattern to apply. Use "None" to retrieve all logs, or specify a pattern to filter specific log events.                                                                                                                                      |
| **Account ID**           | Your AWS account ID (12-digit number). This identifies your AWS account.                                                                                                                                                                                           |
| **Cluster name**         | The name of your RDS Oracle cluster or instance identifier.                                                                                                                                                                                                         |
| **Ingestion delay (seconds)** | Default value is 900 seconds (15 minutes). This delay accounts for the time it takes for logs to be available in CloudWatch after being generated.                                                                                                            |
| **No-traffic threshold (minutes)** | Default value is 60. If there is no incoming traffic for an hour, S-TAP displays a red status. Once incoming traffic resumes, the status returns to green.                                                                                                    |
| **Unmask sensitive value** | Optional boolean flag. When enabled, sensitive values in the audit logs will not be masked.                                                                                                                                                                       |
| **Use Enterprise Load Balancing (ELB)** | Enable this if ELB support is required.                                                                                                                                                                                                                             |
| **Managed Unit Count**   | Number of Managed Units (MUs) to allocate for ELB.                                                                                                                                                                                                                  |

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

