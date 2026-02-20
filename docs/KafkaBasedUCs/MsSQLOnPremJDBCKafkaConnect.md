# Configuring MSSQL datasource profiles for JDBC Kafka Connect Plug-ins

Create and configure datasource profiles through through Central Manager for MSSQL OnPrem JDBC Kafka Connect plug-ins.

## Meet MSSQL Over JDBC Connect
* Environments: On-prem
* Supported inputs: Kafka Input (pull)
* Supported Guardium versions:
    * Guardium Data Protection: Appliance bundle 12.2.2 or later

Kafka-connect is a framework for streaming data between Apache Kafka and other systems.

## Before you begin

Download MSSQL JDBC driver from [mssql-jdbc-7.4.1.jre8](https://github.ibm.com/Activity-Insights/universal-connectors/blob/master/filter-plugin/logstash-filter-mssql-guardium/MssqlOverJdbcPackage/mssql-jdbc-7.4.1.jre8.jar).

## Configuring the MSSQL database
    
Create a database instance. This procedure requires an existing Microsoft SQL Server on-premises installation.

## Enabling auditing
     
1. Connect to the database. </br>
	a. Launch SQL Server Management Studio and provide the following connection details: </br>
		   &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;i. In the **Server Name** field, enter the **HostName**.</br>
   		&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;ii. Enter the username and master password that you set while creating the database.</br>
	b. Create database.</br>
   
2. Audit specifications.</br>
	a. SQL Server audit allows you to create server audits that can contain the following specifications:</br>
		&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;i. Server audit specifications for server-level events.</br>
		&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;ii. Database audit specifications for database-level events.</br>
	b. When you define an audit, you specify the output location for the results (the audit destination). The audit is created in a disabled state and does not automatically audit any actions. After the audit is enabled, the audit destination receives data from the audit.</br>

3. Create an audit.</br>
	a. Create an audit in Management Studio by going to **Security > Audits > New Audit**.</br>
	b. Enter the filepath.</br>
	c. In the **Maximum file size** field, deselect the **Unlimited** checkbox and enter a specific value.</br>
	d. Keep the remaining configurations as default.</br>
	e. Click **OK**.</br>
	f. Right-click the audit you created and select **Enable**.</br>
		
4. Create server or database audit specifications.
  
   - Create a server audit specification.</br>
	a. In **Management**, navigate to **Security** and expand it.</br>
	b. Right-click **Server Audit Specifications** and select **New Audit Specification**.</br>
	c. Select the audit that you created in the previous step.</br>
	d. Configure the audit log groups based on your requirements. For more information on audit log groups, see the Microsoft documentation website.</br>
	e. Click **OK**.</br>
	f. Right-click the database audit specification you created and select **Enable**.</br>

   - Create a database audit specification.</br>
	a. In **Management**, navigate to **Databases** and expand it. Then expand **Security** under the database.</br>
	b. Right-click **Database Audit Specifications** and select **New Audit Specification**.</br>
	c. Select the audit that you created in the previous step.</br>
	d. Configure the audit log groups based on your requirements. For more information on audit log groups, see the Microsoft documentation website.</br>
	e. Click **OK**.</br>
	f. Right-click the database audit specification you created and select **Enable**.</br>
   
   
5. Create audit specifications for capturing errors. </br>
   Excute the following TSQL to capture error events. 
   
   			CREATE EVENT SESSION [<event_name>] 
   			ON SERVER 
   			ADD EVENT sqlserver.error_reported
   			(    
   				ACTION    
   				(	       
   					sqlserver.client_hostname,
   					sqlserver.database_id,
   					sqlserver.sql_text,
   					sqlserver.username,
   					sqlserver.database_name,
   					sqlserver.session_id,
   					sqlserver.server_instance_name
   				)
   				WHERE
   				(
   					[severity] >= (11)
   				)
   			)
   			ADD TARGET package0.asynchronous_file_target
   			(
   				SET filename=N'<path to the xel file>'
   			)
   			WITH
   				(
   					MAX_MEMORY=4096 KB,
   					EVENT_RETENTION_MODE=ALLOW_SINGLE_EVENT_LOSS,
   					MAX_DISPATCH_LATENCY=30 SECONDS,
   					MAX_EVENT_SIZE=0 KB,
   					MEMORY_PARTITION_MODE=NONE,
   					TRACK_CAUSALITY=OFF,
   					STARTUP_STATE=ON
   				);
   			GO
   
   			ALTER EVENT SESSION [<event_name mentioned in create event>]ON SERVER
   			STATE = START;
   			GO
   
   	**Note:** The event name must be the same for **TSQL Create** and **Alter event**.
   	The xel file path name mentioned in TSQL must match the SQL statement mentioned in input plugin for the 'failure' tag. 


6. Create a non-admin user to access the audit table without exposing admin credentials. </br>
	a. Log in to the database using admin credentials and run the following queries:  </br>

  	```sql
 	CREATE LOGIN <login_name> WITH PASSWORD = '<password>';
   CREATE USER <user_name> FOR LOGIN <login_name>;
   GRANT SELECT ON sys.fn_get_audit_file TO <user_name>;
   GRANT CONTROL SERVER TO <login_name>;
	```
	
	b.  In the **Input** section, set the database name as **msdb**.  </br>

  	```properties
 	 jdbc_connection_string => "jdbc:sqlserver://<SERVER_NAME>:<PORT>;databaseName=msdb;"
 	 ```

   c. Use the login credentials created in the previous step for the JDBC connection.  </br>
	
   	```properties
  	jdbc_user => "<login_name>"
  	jdbc_password => "<password>"
 	 ```
    
## Creating datasource profiles

You can create a new datasource profile from the **Datasource Profile Management** page.

### Procedure

1. Go to **Manage > Universal Connector > Datasource Profile Management**
2. Click the **➕ (Add)** button.
3. You can create a profile by using one of the following methods:

    * To **Create a new profile manually**, go to the **"Add Profile"** tab and provide values for the following fields.
        * **Name** and **Description**.
        * Select a **Plug-in Type** from the dropdown. For example, `MSSQL OnPrem Over JDBC Kafka Connect 2.0`.

    * To **Upload from CSV**, go to the **"Upload from CSV"** tab and upload an exported or manually created CSV file
      containing one or more profiles. You can also choose from the following options:
        * **Update existing profiles on name match** — Updates profiles with the same name if they already exist.
        * **Test connection for imported profiles** — Automatically tests connections after profiles are created.
        * **Use ELB** — Enables ELB support for imported profiles. You must provide the number of MUs to be used in the
          ELB process.

**Note:** Configuration options vary based on the selected plug-in.

## Configuration: JDBC Kafka Connect 2.0-based Plugins

The following table describes the fields that are specific to JDBC Kafka Connect 2.0 and similar plugins.

| Field                    | Description                                                                                                                                                      |Value/Example                          |
|--------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------|----------------------------------|
| **Name**                 | Unique name of the profile.                                                                                                                                       | MSSQL_ONPREM_JDBC_KAFKA_CONNECT            |
| **Description**          | Description of the profile.                                                                                                                                      | Profile for MSSQL OnPrem over JDBC connect 2.0 plug-in |
| **Plug-in**              | Plug-in type for this profile. A full list of available plug-ins is found in the **Package Management** page.                                                     | MSSQL OnPrem over JDBC connect 2.0            |
| **Credential**           | The credential to authenticate with the datasource. Must be created in **Credential Management**, or click **➕** to create one.                              | |
| **Kafka Cluster**        | Kafka cluster to deploy the Universal Connector.                                                                                                                  |Select from existing Kafka clusters attached to central management|
| **Label**                | Grouping label (e.g., customer name or ID).                                                                                                                       | |
| **JDBC Driver Library**  | JDBC driver for the database.                                                                                                                                     |Download from here [mssql-jdbc-7.4.1.jre8](https://github.ibm.com/Activity-Insights/universal-connectors/blob/master/filter-plugin/logstash-filter-mssql-guardium/MssqlOverJdbcPackage/mssql-jdbc-7.4.1.jre8.jar) |
| **Initial time (milliseconds)**                 | Initial polling time for audit logs.                                                                                                                              | 0                    |
| **No traffic threshold (minutes)**             | Threshold setting for inactivity detection.                                                                                                                                        |60 (default)               |
| **Query for MSSQL Audit Log**                | SQL query used to extract audit logs.                                                                                                                             | SELECT * FROM (SELECT CAST(event_time AS DATETIME) AS event_time, CAST(succeeded AS INT) AS succeeded, session_id, CAST(database_name AS NVARCHAR(128)) AS database_name, CAST(client_ip AS NVARCHAR(50)) AS client_ip, CAST(server_principal_name AS NVARCHAR(128)) AS server_principal_name, CAST(application_name AS NVARCHAR(128)) AS application_name, CAST(statement AS NVARCHAR(4000)) AS statement, CAST(server_instance_name AS NVARCHAR(128)) AS server_instance_name, '' AS host_name, DATEDIFF_BIG(ns, '1970-01-01 00:00:00.00000', event_time) AS updatedeventtime, additional_information FROM (SELECT * FROM sys.fn_get_audit_file('/var/opt/mssql/audit/*.sqlaudit', DEFAULT, DEFAULT)) AS audit_data WHERE schema_name NOT IN ('sys') AND object_name NOT IN ('dbo', 'syssubsystems', 'fn_sysdac_is_currentuser_sa', 'backupmediafamily', 'backupset', 'syspolicy_configuration', 'syspolicy_configuration_internal', 'syspolicy_system_health_state', 'syspolicy_system_health_state_internal', 'fn_syspolicy_is_automation_enabled', 'spt_values', 'sysdac_instances_internal', 'sysdac_instances') AND database_principal_name NOT IN ('public') AND ((succeeded = 1) OR (succeeded = 0 AND statement LIKE '%Login failed%')) AND statement != '') AS KAFKA_AUDIT_VIEW
| **Query for SQL failed**                | SQL query used to extract audit logs.                                                                                                                             | SELECT * FROM (SELECT CAST(timestamp_utc AS DATETIME) as timestamp_utc,event_data,DATEDIFF_BIG(ns, '1970-01-01 00:00:00.00000', timestamp_utc) AS updated_timestamp FROM sys.fn_xe_file_target_read_file('/var/opt/mssql/log/*.xel',null,null,null)) AS MSSQL_SQL_FAILED_VIEW |
| **Tracking column type for MSSQL Audit Log**   | Tracking column type for MSSQL Audit Log.                                                                                                                         | timestamp         |
| **Tracking column for MSSQL Audit Log**         | Tracking column.                                                                                                                              | event_time  |
| **Tracking column type for SQLFailedLog** | Tracking column type for SQLFailedLog.                                                                                                                        | timestamp |
| **Tracking column for SQLFailedLog** |            Tracking column.                                  | event_time                                                                     |
| **Connection String**              | Connection string connect to MSSQL database.               |                    jdbc:sqlserver://;serverName=<host>;databaseName=<service_name>                                                                                     |

**Note:**

- Depending on the plugin type, the configuration may require either:
    - A **Connection URL**, or
    - Separate fields for **Hostname**, **Port**, and **Service Name / SID**
- Ensure that the **profile name** is unique.
- Required credentials must be created before or during profile creation.

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

## Limitations

- The MSSQL On‑Prem JDBC Kafka Connect plug‑in multiple S‑TAP entries may appear in the S‑TAP report.