# Configuring SapHana data source profiles for JDBC Kafka Connect plug-ins

Create and configure data source profiles through central manager for SAP HANA JDBC Kafka Connect plug-ins.

## Meet SAP HANA Over JDBC Connect
* Environments: On-prem
* Supported inputs: Kafka Input (pull)
* Supported Guardium versions:
    * Guardium Data Protection: Appliance bundle 12.2.1 or later

Kafka-connect is a framework for streaming data between Apache Kafka and other systems.

### Before you begin

Download the [SAP HANA JDBC driver](https://tools.hana.ondemand.com/#hanatools).

## Creating a user for audit log configuration

Create a dedicated user with audit privileges to manage and monitor audit log configurations in SAP HANA.

**Note**: You can also use system user to audit log configurations.

### Procedure

1. Create a new user by running the following command.
   ```sql
   CREATE USER your_username PASSWORD 'your_password';
   ```
2. Assign the required audit privileges to the user.
   ```sql
   GRANT AUDIT ADMIN TO your_username;
   ```
3. Commit the changes to the database.
   ```sql
   COMMIT;
   ```

Verify that all the changes are committed to the database.

### Procedure

1. In SAP HANA Studio, expand the system on which you want to enable auditing.
2. Expand the **Security** folder.
3. Double-click the **Security option**.
4. From the **Auditing status** drop-down menu, select **Enabled**. By default, the auditing status is disabled.
5. To save the changes, click **Deploy** or press **F8**.
6. Restart the database instance to apply the changes.

### Enabling CSTABLE-based auditing

Configure CSTABLE-based auditing in SAP HANA to store audit trails in database tables by using the JDBC input plug-in.

**Note:** CSTABLE-based auditing stores audit trails in a database table and requires the JDBC input plug-in.

### Procedure

1. In SAP HANA Studio, select the **SYSTEMDB** user. Then, open **SQL Console**.
2. Enable auditing for a multiple container tenant database.
   ```
   ALTER SYSTEM ALTER CONFIGURATION ('global.ini', 'system')
   set ('auditing configuration', 'global_auditing_state') = 'true';
   ```
3. Set the audit trail target to a table (``CSTABLE``).
   ```
   ALTER SYSTEM ALTER CONFIGURATION ('global.ini', 'system')
   set ('auditing configuration', 'default_audit_trail_type') = 'CSTABLE';
   ```
4. To prevent storing unwanted system logs in the audit table, store them in CSV files using the following command
   ```
   ALTER SYSTEM ALTER CONFIGURATION ('global.ini', 'system')
   set ('auditing configuration', 'critical_audit_trail_type') = 'CSVTEXTFILE';
   ```
5. Restart the container and refresh the added systems.

## Creating an audit policy

Here we will check about CSTABLE base auditing.
* CSTABLE base auditing - Audit-trail target is a table, requires JDBC input plug-in.

### Enabling CSTABLE base auditing logs:

An audit policy defines the actions to be audited. To create an audit policy, the user must have AUDIT ADMIN system
privileges.
You need to create an audit policy for both types of auditing.

To perform the below steps, open SAP HANA studio (Eclipse) Select SYSTEMDB user, right-click,  select SQL Console,
and then run these Commands:

1. For multiple container tenant database you can enable auditing for CSV File target by using the following command
    1. SAP HANA Command For Enable Auditing:
       ```
       ALTER SYSTEM ALTER CONFIGURATION ('global.ini', 'system')
       set ('auditing configuration', 'global_auditing_state') = 'true';
       ```
    2. To select the target as a table, use the following command
       ```
       ALTER SYSTEM ALTER CONFIGURATION ('global.ini', 'system')
       set ('auditing configuration', 'default_audit_trail_type') = 'CSTABLE';
       ```
    3. To avoid unwanted system logs use below command to store all system logs in table.
       ```
       ALTER SYSTEM ALTER CONFIGURATION ('global.ini', 'system')
       set ('auditing configuration', 'critical_audit_trail_type') = 'CSVTEXTFILE';
       ```

2. After running the previous command, restart the container and refresh the added systems.

## Common steps for either auditing type

### Creating an audit policy

An audit policy defines the actions to be audited. In order to create an audit policy, the user must have
AUDIT ADMIN system rights. Creating an audit policy is a common step for both types of auditing.

You need to create an audit policy for both types of auditing.
#### Procedure

1. In the SAP HANA Studio, expand the database.
2. Expand the **Security** folder.
3. Double-click the **Security option**.
4. Under the **Audit Policies** panel, click the **green plus** icon.
5. Enter your policy name.
6. In the **Audited actions** field, click **…** (ellipsis). Then select the actions to audit.
7. In the **Audited actions status** column, select when to create an audit record.
    - SUCCESSFUL - Logs successfully executed actions
    - UNSUCCESSFUL - Logs unsuccessfully executed actions
    - ALL - Logs both successful and unsuccessful actions
8. Select the audit level.
    - EMERGENCY
    - CRITICAL
    - ALERT
    - WARNING
    - INFO (default)
9. (Optional) Filter the users you would like to audit. From the **Users** column, click ***…*** (ellipsis) and add
   users.
10. (Optional) Specify the target object(s) to be audited. This option is available only when auditing `SELECT`,
    `INSERT`, `UPDATE`, or `DELETE` actions.
11. To save the changes, click **Deploy** or press **F8**.
12. Restart the database instance to apply the changes.

## Configuring audit policies in SAP HANA

Configure audit policies in SAP HANA to monitor session activities, DML operations, and DDL changes.

1. For audit session-related logs (Connect, Disconnect, User Validation), complete the following steps. <br>
   a. Go to the tab **Audited action** tab. From the **Session Management** and **System Configuration** menus, select
   the **Connect** checkbox. <br>
   b. Set **Audited Action Status** to ``Unsuccessful``. <br>

2. For audit DML logs, complete the following steps. <br>
   a. From the tab **Audited action** tab, select the **Data Query** and **Manipulation** checkbox. <br>
   b. Set **Audited Action Status** to ``Successful``.<br>
   c. Specify a **Target Object** for this policy.

3. For audit DDL Logs, complete the following steps. <br>
   a. Go to the tab **Audited action** tab. From the **Data Definition** menu, select the **Create Table**, **Create
   Function**, **Create Procedure**, **Drop Function**, **Drop Procedure**, and **Drop Table** checkboxes. <br>
   b. Set **Audited Action Status** to ``Successful``. <br>

## Viewing SAP HANA audit logs for CSTABLE-based auditing

### Procedure

1. Connect to the database.
2. Right-click on it and click **SQL console**. Then run the following command.
   ```sql
     select * from AUDIT_LOG;
   ```

## Creating datasource profiles

You can create a new datasource profile from the **Datasource Profile Management** page.

### Procedure

1. Go to **Manage > Universal Connector > Datasource Profile Management**
2. Click the **➕ (Add)** button.
3. You can create a profile by using one of the following methods:

    * To create a new profile manually, go to the **"Add Profile"** tab and provide values for the following fields.
        * **Name** and **Description**.
        * Select a **Plug-in Type** from the dropdown. For example, **SAP HANA Over JDBC Kafka Connect 2.0**.

    * To upload from CSV, go to the **Upload from CSV** tab and upload an exported or manually created CSV file
      containing one or more profiles. You can also choose from the following options:
        * **Update existing profiles on name match** — Updates profiles with the same name if they already exist.
        * **Test connection for imported profiles** — Automatically tests connections after profiles are created.
        * **Use ELB** — Enables ELB support for imported profiles. You must provide the number of MUs to be used in the
          ELB process.

**Note:** Configuration options vary based on the selected plug-in.

---

## Configuring JDBC Kafka Connect 2.0-based plugins

The following table describes the fields that are specific to JDBC Kafka Connect 2.0 and similar plugins.

| Field                                   | Description                                                                                                                                 |
|-----------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------|
| **Name**                                | Unique name of the profile.                                                                                                                 |
| **Description**                         | Description of the profile.                                                                                                                 |
| **Plug-in**                             | Plug-in type for this profile. A full list of available plug-ins are available on the **Package Management** page.                          |
| **Credential**                          | The credential to authenticate with the datasource. Create the credential in **Credential Management**, or click **➕** to create a new one. |
| **Kafka Cluster**                       | Kafka cluster to deploy the universal connector.                                                                                            |
| **Label**                               | Grouping label (e.g, customer name or ID).                                                                                                  |
| **JDBC Driver Library**                 | JDBC driver for the database.                                                                                                               |
| **Port**                                | Port that is used to connect to the database.                                                                                               |
| **Hostname**                            | Hostname of the database.                                                                                                                   |
| **Query**                               | SQL query that is used to extract audit logs.                                                                                               |
| **Service Name / SID**                  | The database **service name** or **SID**.                                                                                                   |
| **Initial Time**                        | Initial polling time for audit logs.                                                                                                        |
| **No Traffic Threshold**                | Threshold setting for inactivity detection.                                                                                                 |
| **Use Enterprise Load Balancing (ELB)** | Enable this if ELB support is required.                                                                                                     |
| **Managed Unit Count**                  | Number of Managed Units (MUs) to allocate for ELB.                                                                                          |

**Note:**
- Depending on the plugin type, the configuration may require either:
    - A **Connection URL**, or
    - Separate fields for **Hostname**, **Port**, and **Service Name / SID**
- Make sure that the **Profile name** is unique.
- Required credentials must be created before or during profile creation.

---

## Testing a Connection

After creating a profile, you must test the connection to make sure that the provided configuration is valid.

### Procedure

1. Select the new profile.
2. From the top menu, click **Test Connection**.
3. If the test is successful, you can proceed to installing the profile.

---

## Installing a Profile

After the connection test succeeds, you can install the profile on **Managed Units (MUs)** or **Edges**. The parsed
audit logs are sent to the selected Managed Unit or Edge for to be consumed by the **Sniffer**.

### Procedure

1. Select the profile.
2. From the list of available actions, select the desired option **Uninstall** or **Reinstall**.
3. From the list of available MUs and Edges that is displayed, select the ones that you want to deploy the profile to.

---

## Uninstalling or reinstalling profiles

An installed profile can be **uninstalled** or **reinstalled** if needed.

### Procedure

1. Select the profile.
2. From the list of available actions, select the desired option: **Uninstall** or **Reinstall**.

---

