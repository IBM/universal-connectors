# Teradata Datasource Profile Configuration Guide for JDBC Kafka Connect Plug-ins

This guide provides instructions for creating and configuring datasource profiles through Central Manager for **Teradata JDBC Kafka Connect** plug-ins.

### Meet Teradata over JDBC Connect

* Environments: On-prem
* Supported inputs: Kafka Input (pull)
* Supported Guardium versions:
    * Guardium Data Protection: Appliance bundle 12.2.1 or later.

Kafka-connect is a framework for streaming data between Apache Kafka and other systems.

## Configuring the Teradata server

### Before you begin
There are multiple ways to install a Teradata server. For this example, we assume that we already have a working Teradata server setup.

Download the [Teradata JDBC Driver](https://downloads.teradata.com/download/connectivity/jdbc-driver).

## Enabling auditing

### Procedure 
1. Connect to the Teradata server by using SSH.

2. Log in to the Teradata system by using BTEQ with the credentials of a user (such as DBC), that has the permissions to execute ``DBQLAccessMacro``. In the following command, enter the password for the DBC user. </br>

	```
	bteq .logon <server-name>/dbc,<password>
	```

3. Create a user to read logs from audit tables through the logstash JDBC input plug-in by using the following command.

   	```
   	CREATE USER <username> AS  PERMANENT = 100000000 BYTES   PASSWORD = "<password>"
4. Grant read access of objects within the DBC user to newly created user by using the following command.

    	GRANT SELECT ON "dbc" TO "<username>";

5. Enable Database Query Logging. 

	**Note:** Query logging involves a range of table/view combinations within the DBC database. Enable query logging for users, accounts, or applications only when necessary.

	You can enable query logging for all users or for specific users. In the following example command, query logging is enabled for all users.

		BEGIN QUERY LOGGING WITH SQL LIMIT SQLTEXT=0 ON ALL;

	For more information about Database Query Logging, see [DBQL](https://docs.teradata.com/r/qOek~PvFMDdCF0yyBN6zkA/f7yJJ4siIiBUpoQVvvAwpQ).

6. To exit the BTEQ terminal, type `exit;`.
7. Set the database time zone by configuring the following ``dbscontrol`` fields.

   a. Set the `"18. System TimeZone String"` parameter to the timezone that you want to configure for your database.<br/>
   b. Set the `"57. TimeDateWZControl "` parameter to 2.

8. Close the terminal.

9. Verify that a logging rule is created in a table. Log in with DBC user. Run the following query by using any client utility.
   In the following example, the Teradata Studio Express is used.

   ```
   select * from DBC.DBQLRulesV;
   ```

### Notes:
For **Teradata VCE on Azure**, enable audit logging by using the following command.

    BEGIN QUERY LOGGING WITH SQL LIMIT SQLTEXT=0 ON ALL;

Verify that the audit log is enabled by running the following command.

    select * from DBC.DBQLRulesV;


## Disabling auditing
You can disable auditing can be disabled by using the credentials of a user (such as DBC) that has the permissions to execute ``DBQLAccessMacro``.

To disable query logging, run the following command.

    END QUERY LOGGING WITH SQL LIMIT SQLTEXT=0 ON ALL;


## Archiving and deleting DBQL logs

There are many ways to archive and delete DBQL logs. To delete old log data from system tables manually, complete the following steps.

### Before you begin
Before you perform cleanup activities on DBQL logs, it is recommended to disable DBQL logging to prevent potential performance issues. Disabling logging makes sure that the delete process does not lock the DBQL tables, which could slowdown the system if DBQL needs to flush cache to the same table for query logging.

**Note:** You cannot delete data that is less than 30 days old.

### Procedure

1. Back up log data. <br/>
   a. Create a duplicate log table in another database by using the ``Copy Table`` syntax for the `CREATE TABLE` statement. </br>
   ```
   CT DBC.tablename AS databasename.tablename`
   ```
   b. Back up the table to tape storage in accordance with your site backup policy.  <br>
   c. Drop the duplicate table using a ``DROP TABLE`` statement.
3. Log on to Teradata Studio as DBADMIN or another administrative user with DELETE privileges on the DBC database.
4. In the **Query** window, enter an SQL statement to purge old log entries. For example,

   `DELETE FROM DBC.object_name WHERE (Date - LogDate) > number_of_days ;`

	Examples for using the above query.
	```
	DELETE FROM DBC.DBQLOGTBL WHERE (DATE '2021-12-16' - cast(starttime as DATE)) > 30 ;
	DELETE FROM DBC.DBQLSqlTbl WHERE (DATE '2021-12-16' - cast(collecttimestamp as DATE)) > 30 ;
	```

## Limitations

1. The Teradata sniffer parser may not parse certain operations accurately. This plug-in does not support the following operations: <br>
   	a. User Management <br>
    	b. DBQL Queries <br>
   	c. Timestamp configuration <br>
   	d. Cast operations <br>
   	e. Stored Procedure and User Defined Functions <br>
2. The following fields are not found in TeradataDB audit logs: <br>
   	a. ``Client HostName`` : Not Available with audit logs. <br>
   	b. ``Database Name`` : Not Available with audit logs. <br>
3. The Teradata auditing does not audit authentication failure (Login Failed) operations.
4. In case of the EC2 guardium instance, Teradata traffic takes longer (25-30 min) to populate data in the full SQL report.
5. This plug-in supports queries that are approximately 32,000 characters long. When the count of characters in a query exceeds the given count, the remaining part of the query is stored in other rows and the **SQLTextInfo** column of the `DBC.DBQLSqlTbl` table has more than one row per ``QueryID``.
6. Client IP and Server IP are retrieved from DBC.QryLogClientAttrV view using ClientIPAddrByClient and ServerIPAddrByServer fields respectively, as recommended by Teradata support. The deprecated logonsource field is no longer used for IP address retrieval. 

    For more information on DBC.QryLogClientAttrV, please refer to this [documentation](https://docs.teradata.com/r/Enterprise_IntelliFlex_VMware/Data-Dictionary/Views-Reference/QryLogClientAttrV).

## Creating datasource profiles
You can create a new datasource profile from the **Datasource Profile Management** page.

### Procedure

1. Go to **Manage > Universal Connector > Datasource Profile Management**
2. Click the **➕ (Add)** button.
3. You can create a profile by using one of the following methods:

    - To **Create a new profile manually**, go to the **"Add Profile"** tab and provide values for the following fields.
        - **Name** and **Description**.
        - Select a **Plug-in Type** from the dropdown. For example, `Teradata Over JDBC Kafka Connect 2.0`.

    - To **Upload from CSV**, go to the **"Upload from CSV"** tab and upload an exported or manually created CSV file containing one or more profiles.  
      You can also choose from the following options:
        - **Update existing profiles on name match** — Updates profiles with the same name if they already exist.
        - **Test connection for imported profiles** — Automatically tests connections after profiles are created.
        - **Use ELB** — Enables ELB support for imported profiles. You must provide the number of MUs to be used in the ELB process.

**Note:** Configuration options vary based on the selected plug-in.

## Configuring JDBC Kafka Connect 2.0-based plugins

The following table dercribes the fields that are specific to JDBC Kafka Connect 2.0 and similar plugins.

| Field                    | Description                                                                                                                                                                              |
|--------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Name**                 | Unique name of the profile.                                                                                                                                                               |
| **Description**          | Description of the profile.                                                                                                                                                               |
| **Plug-in**              | Plug-in type for this profile. A full list of available plug-ins are available on the **Package Management** page.                                                                             |
| **Credential**           | The credential to authenticate with the datasource. Must be created in **Credential Management**, or click **➕** to create one.                                                      |
| **Kafka Cluster**        | Kafka cluster to deploy the Universal Connector.                                                                                                                                          |
| **Label**                | Grouping label. For example, customer name or ID.                                                                                                                                               |
| **JDBC Driver Library**  | JDBC driver for the database.                                                                                                                                                             |
| **Port**                 | Port used to connect to the database.                                                                                                                                                     |
| **Hostname**             | Hostname of the database.                                                                                                                                                                 |
| **Query**                | SQL query that is used to extract audit logs.                                                                                                                                                     |
| **Service Name / SID**   | The database **service name** or **SID**.                                                                                                                                                 |
| **Initial Time**         | Initial polling time for audit logs.                                                                                                                                                      |
| **No Traffic Threshold** | Threshold setting for inactivity detection.                                                                                                                                               |
| **Connection URL**       | Full JDBC connection string. Format varies by database type. <br/> For example, `jdbc:teradata://teradata-db.env.clearscape.teradata.com/DATABASE=audituser,DBS_PORT=1025`). |
| **Enterprise Load Balancing** |                                                                                                                                                                                          |
| **Use ELB**              | Enable this if ELB support is required.                                                                                                                                                   |
| **Managed Unit Count**   | Number of Managed Units (MUs) to allocate for ELB.                                                                                                                                        |


**Note:**
- Depending on the plugin type, the configuration may require either:
    - A **Connection URL**, or
    - Separate fields for **Hostname**, **Port**, and **Service Name / SID**
- Ensure that the **profile name** is unique.
- Required credentials must be created before or during profile creation.

---

## Testing connections

After creating a profile, you must **test the connection** to ensure that the provided configuration is valid.

### Procedure

1. Select the new profile.
2. From the top menu, click **Test Connection**.
3. If the test is successful, you can proceed to installing the profile.

---

## Installing profiles

Once the connection test is successful, you can install the profile on **Managed Units (MUs)** or **Edges**. The parsed audit logs are sent to the selected Managed Unit or Edge to be consumed by the **Sniffer**.

### Procedure

1. Select the profile.
2. From the **Install** menu, click **Install**.
3. A list of available MUs and Edges are displayed. Choose the specific MUs and Edges to which you want to apply the new profile.

---

## Uninstalling or reinstalling profiles

An installed profile can be **uninstalled** or **reinstalled** if needed.

### Procedure

1. Select the profile.
2. From the list of available actions, select the desired option **Uninstall** or **Reinstall**.

---
