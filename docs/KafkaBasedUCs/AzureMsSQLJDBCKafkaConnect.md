# Configuring Azure SQL datasource profiles for JDBC Kafka Connect Plug-ins

Create and configure datasource profiles through central manager for **Azure MSSQL JDBC Kafka Connect** plug-ins.

## Meet Azure SQL Over JDBC Connect

* Environments: On-prem
* Supported inputs: Kafka Input (pull)
* Supported Guardium versions:
    * Guardium Data Protection: Appliance bundle 12.2.2 or later

Kafka-connect is a framework for streaming data between Apache Kafka and other systems.

## Configuring the AzureSQL service

There are two ways to get Azure SQL audit data:

1. Universal Connector using Azure object storage and a JDBC feed
2. Guardium Streams using an Azure Event Hub
This plugin uses object storage.

If you want more insights into these options, contact the Guardium Offering Managers.

### Procedure 
1. Go to https://portal.azure.com/.
2. In the search bar, enter **AzureSQL**.
3. Click **Create**.
4. Select the **SQL databases** option.
5. Choose the **Resource Type** depending on your requirements (**Single database** or **Elastic pool**). Then click **Create**.
6. Select **Existing Resource group** or **Create New one**.
7. Provide a **Database Name**.
8. Click the **Create New** option under the **Server** field.
9. Provide a **Server Name** and select the appropriate **Location**.
10. Select **Authentication method** based on your requirement.
11. Enter a **Server admin login name** and **Password**. Then click **OK**.
12. If needed, select the **Compute + storage** configuration.
13. Click **Review + Create**.
14. Verify the configuration and then click **Create**.
15. From the search bar, navigate to **Storage Account** and create a new storage account. </br>
        a. Select the resource group that you created.</br>
        b. Enter a **Storage Account Name**.</br>
        c. Select the appropriate location.</br>
        d. Choose additional configurations, if needed.</br>
        e. Click **Review + create** > **Create**.</br>

## Enabling Auditing

1. Click on the **Menu** button and go to the **Resource groups** tab.
2. In the **Resource groups** tab, click **Resource Group**.</br>
  	a. Select the SQL Server that you created.</br>
   	b. Expand the **Show Firewall setting** options and add **Client IPaddress** and **Public IPaddress** of the gmachine that is required to capture traffic. </br>
   	c. Add **Client IPaddress** by clicking ```Add Client IP``` Button.</br>
   	   Use the `curl ipinfo.io/ip` command to obtain the public IPaddress of your gmachine.</br>
	d. Click **Save**.</br>
5. To enable auditing, complete the following steps.</br>
	a. Select your SQL Database.</br>
   	b. From the search bar, navigate to **Auditing**.</br>
   	c. In **Auditing**, click **Enable Azure SQL Auditing**.</br>
  	d. Select the **Storage Audit log destination** check box.</br>
   	e. Select the **Storage Account** that you created.</br>
   	f. Go to **Advanced properties**, and select **Retention (Days)**.</br>
   	g. Click **Save**.</br>


## Connecting to AzureSQL Database

1. Start the SQL Server Management Studio and provide connection details. Enter the **server name**, **username**, and the **master password** that you set while creating the database.
2. Click **Connect**.


## Viewing audit logs

Use the following query and enter your **storage-account-name**, **server_instance_name**, **DB-NAME** values.

	```
	SELECT event_time,succeeded,session_id,database_name,client_ip,server_principal_name,application_name,statement,server_instance_name,host_name,DATEDIFF_BIG(ns, '1970-01-01 00:00:00.00000', event_time) AS updatedeventtime,additional_information  FROM sys.fn_get_audit_file('https://<storage-account-name>.blob.core.windows.net/sqldbauditlogs/<server_instance_name>/<DB-NAME>', DEFAULT, DEFAULT) where action_id='BCM' and statement not like '%xproc%' and statement not like '%SPID%' and statement not like '%DEADLOCK_PRIORITY%' and application_name not like '%Microsoft SQL Server Management Studio - Transact-SQL IntelliSense%' and DATEDIFF_BIG(ns, '1970-01-01 00:00:00.00000', event_time) > 0;```

**Note:** Create a non-admin user to access the audit table without exposing admin credentials. Create the non-admin user in the Azure Portal directly. 

1. Log in to the database using admin credentials and run the following queries.
	

```
sql
CREATE USER <user_name> WITH PASSWORD = '<password>';
ALTER ROLE db_datareader ADD MEMBER <user_name>;
GRANT VIEW DATABASE STATE TO <user_name>;
GRANT VIEW DATABASE SECURITY AUDIT TO <user_name>;
  ```

2. Use the user credentials that you created in the previous step for the JDBC connection.

```
properties
jdbc_user => "<user_name>"
jdbc_password => "<password>"
  ```


## Finding the Enrollment ID

**Note:** You need to be an Enterprise Administrator to access the Enrollment ID.

1. Log in to your Azure Enterprise account at https://ea.azure.com.

2. In the top left corner, you can see your **Enrollment ID**.

3. Copy the Enrollment ID and save it for later use.

## Getting the JDBC Connection string

1. Click on **Database**.

2. In the search bar, enter **connection string**.

3. Select **JDBC** and copy the string.

4. Enter this connection string value to the JDBC Input plugin **jdbc_connection_string** parameter.


## Limitations
• The azureSQL plug-in does not support IPV6.

• The azureSQL auditing does not audit authentication failure (Login Failed) operations.

• AzureSQL audit-records does not audit **serverIP**. The **serverIp** value is hardcoded to ``0.0.0.0``.

• The following important fields are not mapped with AzureSQL audit logs: **create user with password** operation, **OS USER** field.
   
• AzureSQL auditing does not audit operations perform by **Beekeeper Studio Tools**.

## Creating datasource profiles

You can create a new datasource profile from the **Datasource Profile Management** page.

### Procedure

1. Go to **Manage > Universal Connector > Datasource Profile Management**
2. Click the **➕ (Add)** button.
3. You can create a profile by using one of the following methods:

    * To **Create a new profile manually**, go to the **"Add Profile"** tab and provide values for the following fields.
        * **Name** and **Description**.
        * Select a **Plug-in Type** from the dropdown. For example, `Azure SQL Over JDBC Kafka Connect 2.0`.

    * To **Upload from CSV**, go to the **"Upload from CSV"** tab and upload an exported or manually created CSV file
      containing one or more profiles. You can also choose from the following options:
        * **Update existing profiles on name match** — Updates profiles with the same name if they already exist.
        * **Test connection for imported profiles** — Automatically tests connections after profiles are created.
        * **Use ELB** — Enables ELB support for imported profiles. You must provide the number of MUs to be used in the
          ELB process.

**Note:** Configuration options vary based on the selected plug-in.

## Configuration: JDBC Kafka Connect 2.0-based Plugins

The following table describes the fields that are specific to JDBC Kafka Connect 2.0 and similar plugins.

| Field                         | Description                                                                                                                                                        |
|-------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Name**                      | Unique name of the profile.                                                                                                                                        |
| **Description**               | Description of the profile.                                                                                                                                        |
| **Plug-in**                   | Plug-in type for this profile. A full list of available plug-ins are available on the **Package Management** page.                                                 |
| **Credential**                | The credential to authenticate with the datasource. Must be created in **Credential Management**, or click **➕** to create one.                                    |
| **Kafka Cluster**             | Kafka cluster to deploy the universal connector.                                                                                                                   |
| **Label**                     | Grouping label. For example, customer name or ID.                                                                                                                  |
| **JDBC Driver Library**       | JDBC driver for the database.                                                                                                                                      |
| **Port**                      | Port that is used to connect to the database.                                                                                                                      |
| **Hostname**                  | Hostname of the database.                                                                                                                                          |
| **Query**                     | SQL query that is used to extract audit logs.                                                                                                                      |
| **Service Name / SID**        | The database **service name** or **SID**.                                                                                                                          |
| **Initial Time**              | Initial polling time for audit logs.                                                                                                                               |
| **No Traffic Threshold**      | Threshold setting for inactivity detection.                                                                                                                        |
| **Connection URL**            | Full JDBC connection string. Format varies by database type. <br/> For example, `jdbc:sqlserver://myserver.database.windows.net:1433;database=mydb;encrypt=true;`. |
| **Use Enterprise Load Balancing (ELB)** | Enable this if ELB support is required.                                                                                                                            |
| **Managed Unit Count**        | Number of Managed Units (MUs) to allocate for ELB.                                                                                                                 |

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
