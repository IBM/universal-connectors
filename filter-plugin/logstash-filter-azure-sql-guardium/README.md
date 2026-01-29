# AzureSQL-Guardium Logstash filter plug-in

### Meet AzureSQL
* Tested versions: 12.0.2000.8
* Environment: Azure, Azure SQL Managed Instance, MS SQL deployed on VMs in Azure
* Supported inputs: JDBC (pull)
* Supported Guardium versions:
   * Guardium Data Protection: 11.4 and above
   * Guardium Data Security Center SaaS: 1.0

This is a [Logstash](https://github.com/elastic/logstash) filter plug-in for the universal connector that is featured in IBM Security Guardium. It parses events and messages from the azureSQL audit log into a [Guardium record](https://github.com/IBM/universal-connectors/blob/main/common/src/main/java/com/ibm/guardium/universalconnector/commons/structures/Record.java) instance (which is a standard structure made out of several parts). The information is then sent over to Guardium. Guardium records include the accessor (the person who tried to access the data), the session, data, and exceptions. If there are no errors, the data contains details about the query "construct". The construct details the main action (verb) and collections (objects) involved.
The plug-in is free and open-source (Apache 2.0). It can be used as a starting point to develop additional filter plug-ins for Guardium universal connector.



## Enabling Auditing

1. Click on Tap left Corner ```Menu``` button.

2. Go to "Resource groups" tab.

3. Click on ```Resource Group```:

   a. Select SQL Server you have created

   b. Click on ```Show Firewall setting``` options.

   c. Add Client IPaddress by clicking ```Add Client IP``` Button.

   d. Add Public IPaddress of gmachine which is required to capture traffic.

         Use the below command for getting public IPaddress of gmachine:

          `curl ipinfo.io/ip`

   e. Click on ```Save``` Button.

5. To enable auditing:

   a. Select your SQL Database .

   b. In search bar search for 'auditing'.

   c. In Auditing, Click on ```Enable Azure SQL Auditing``` Button.

   d. Select ```Storage``` Audit log destination Check box.

   e. Select Storage Account which you have created.

   f. Click on ```Advanced properties```, And select "Retention (Days)".

   g. Then, click on ```Save``` Button.

**Note:** Since auditing configuration differs between Azure SQL Database and Azure SQL Managed Instance, please follow the given link to [enable auditing for Azure SQL Managed Instance](https://learn.microsoft.com/en-us/azure/azure-sql/managed-instance/auditing-configure?view=azuresql)

## Connecting to AzureSQL Database

		a. Start the SQL Server Management Studio and provide connection details. Enter the ‘server name’ and Provide the ‘username’ and the master ‘password’ that we had set while creating the database.
  
		b. Click on Connect.


## Viewing the Audit logs

	Use below query and fill your storage-account-name,server_instance_name,DB-NAME in Query for viewing audit logs.
	
	SELECT event_time,succeeded,session_id,database_name,client_ip,server_principal_name,application_name,statement,server_instance_name,host_name,DATEDIFF_BIG(ns, '1970-01-01 00:00:00.00000', event_time) AS updatedeventtime,additional_information  FROM sys.fn_get_audit_file('https://<storage-account-name>.blob.core.windows.net/sqldbauditlogs/<server_instance_name>/<DB-NAME>', DEFAULT, DEFAULT) where action_id='BCM' and statement not like '%xproc%' and statement not like '%SPID%' and statement not like '%DEADLOCK_PRIORITY%' and application_name not like '%Microsoft SQL Server Management Studio - Transact-SQL IntelliSense%' and DATEDIFF_BIG(ns, '1970-01-01 00:00:00.00000', event_time) > 0;


## Finding Enrollment ID

	Follow the below steps to get Enrollment number:

	1.Log in to your Azure Enterprise account at https://ea.azure.com.
	2.Top left corner you can see your "Enrollment ID".
	3.Copy the "Enrollment ID" and save it for later use.
	Note: You need to be an Enterprise Administrator to access this.	

## Getting the JDBC Connection String

		a. Click on Database.
		b. Search for "connection string" Inside Search Bar.
		c. Click on JDBC,Copy that.
		d. Add this Connection String value inside JDBC Input plugin "jdbc_connection_string" Parameter.


#### Limitations
	• The azureSQL plug-in does not support IPV6.
	• The azureSQL auditing does not audit authentication failure(Login Failed) operations.
	• AzureSQL audit-records does not audit serverIP,so that serverIp is hardcoded to "0.0.0.0".
	• The following important field couldn't mapped with AzureSQL audit logs. 
		- OS USER : Not Available with audit logs.
	• "create user with password" operation is currently not audited by this plug-in.
	• AzureSQL auditing does not audit operations perform by "Beekeeper Studio Tools".


## Configuring the AzureSQL filters in Guardium

The Guardium universal connector is the Guardium entry point for native audit logs. The Guardium universal connector identifies and parses the received events, and converts them to a standard Guardium format. The output of the Guardium universal connector is forwarded to the Guardium sniffer on the collector, for policy and auditing enforcements. Configure Guardium to read the native audit logs by customizing the azureSQL template.


#### Before you begin

• Configure the policies you require. See [policies](/docs/#policies) for more information..

• You must have permission for the S-Tap Management role. The admin user includes this role by default.

• AzureSQL-Guardium Logstash filter plug-in is automatically available with Guardium Data Protection versions 12.x, 11.4 with appliance bundle 11.0p490 or later or Guardium Data Protection version 11.5 with appliance bundle 11.0p540 or later releases.

**Note**: For Guardium Data Protection version 11.4 without appliance bundle 11.0p490 or prior or Guardium Data Protection version 11.5 without appliance bundle 11.0p540 or prior, download the [Azure-SQL-Offline-Package.zip](https://github.com/IBM/universal-connectors/blob/main/filter-plugin/logstash-filter-azure-sql-guardium/AzureSQLOverJdbcPackage/AzureSQL/logstash-filter-azuresql_guardium_filter.zip) plug-in. (Do not unzip the offline-package file throughout the procedure).

• Download the mssql-jdbc-7.4.1.jre8 from [here](https://repo1.maven.org/maven2/com/microsoft/sqlserver/mssql-jdbc/7.4.1.jre8/mssql-jdbc-7.4.1.jre8.jar)

#### Configuration

1. On the collector, go to ```Setup``` > ```Tools and Views``` > ```Configure Universal Connector```.
2. Enable the universal connector if it is disabled.
3. Click ```Upload File``` and select the offline [Azure-SQL-Offline-Package.zip](https://github.com/IBM/universal-connectors/blob/main/filter-plugin/logstash-filter-azure-sql-guardium/AzureSQLOverJdbcPackage/AzureSQL/logstash-filter-azuresql_guardium_filter.zip) This step is not necessary for Guardium Data Protection v11.0p490 or later, v11.0p540 or later, v12.0 or later.
   a. Again click ```Upload File``` and select the offline mssql-jdbc-7.4.1.jre8 file. After it is uploaded, click ```OK```.
4. Click the Plus sign to open the Connector Configuration dialog box.
5. Type a name in the ```Connector name``` field.
6. Update the input section to add the details from the [azureSQLJDBC.conf](https://github.com/IBM/universal-connectors/raw/main/filter-plugin/logstash-filter-azure-sql-guardium/azureSQLJDBC.conf) file's input part, omitting the keyword "input{" at the beginning and its corresponding "}" at the end.

Note:

	• For Guardium Data Protection version 11.3, add the following line to the input section:
       'jdbc_driver_library => "${THIRD_PARTY_PATH}/mssql-jdbc-7.4.1.jre8.jar"'
	• If auditing was configured a while before the UC, the UC will still process all previous records, since they were already audited by the database.
	• For moderate to large amounts of data, include pagination to facilitate the audit and to avoid out-of-memory errors. Use the parameters in the input section below when using a JDBC connector
               jdbc_paging_enabled => true
               jdbc_page_size => <size>

7. Update the filter section to add the details from the [azureSQLJDBC.conf](https://github.com/IBM/universal-connectors/raw/main/filter-plugin/logstash-filter-azure-sql-guardium/azureSQLJDBC.conf) file's filter part, omitting the keyword "filter{" at the beginning and its corresponding "}" at the end.
8. The 'type' fields should match in the input and filter configuration sections. This field should be unique for every individual connector added.

Note:

    If using two jdbc plug-ins on the same machine, the last_run_metadata_path file name should be different.

9. Click ```Save```. Guardium validates the new connector and displays it in the Configure Universal Connector page.
10. After the offline plug-in is installed and the configuration is uploaded and saved in the Guardium machine, restart the Universal Connector using the ```Disable/Enable``` button.


## JDBC Load Balancing Configuration

	In AzureSQL JDBC input plug-in , we distribute load between two machines based on Even and Odd "session_id"

### Configuration

	On First G Machine,in input section for JDBC Plugin update "statement" field like below:
	
	SELECT event_time,succeeded,session_id,database_name,client_ip,server_principal_name,application_name,statement,server_instance_name,host_name,DATEDIFF_BIG(ns, '1970-01-01 00:00:00.00000', event_time) AS updatedeventtime,additional_information  FROM sys.fn_get_audit_file('https://<storage-account-name>.blob.core.windows.net/sqldbauditlogs/<server_instance_name>/<DB-NAME>', DEFAULT, DEFAULT) where action_id='BCM' and statement not like '%xproc%' and statement not like '%SPID%' and statement not like '%DEADLOCK_PRIORITY%' and application_name not like '%Microsoft SQL Server Management Studio - Transact-SQL IntelliSense%' and session_id%2 = 0 and DATEDIFF_BIG(ns, '1970-01-01 00:00:00.00000', event_time) > :sql_last_value order by event_time;

		
	On Second G machine ,in input section for JDBC Plugin update "statement" field like below:
	
	SELECT event_time,succeeded,session_id,database_name,client_ip,server_principal_name,application_name,statement,server_instance_name,host_name,DATEDIFF_BIG(ns, '1970-01-01 00:00:00.00000', event_time) AS updatedeventtime,additional_information  FROM sys.fn_get_audit_file('https://<storage-account-name>.blob.core.windows.net/sqldbauditlogs/<server_instance_name>/<DB-NAME>', DEFAULT, DEFAULT) where action_id='BCM' and statement not like '%xproc%' and statement not like '%SPID%' and statement not like '%DEADLOCK_PRIORITY%' and application_name not like '%Microsoft SQL Server Management Studio - Transact-SQL IntelliSense%' and session_id%2 = 1 and DATEDIFF_BIG(ns, '1970-01-01 00:00:00.00000', event_time) > :sql_last_value order by event_time;

## Configuring the Azure SQL filters in Guardium Data Security Center
To configure this plug-in for Guardium Data Security Center, follow [this guide.](/docs/Guardium%20Insights/3.2.x/UC_Configuration_GI.md)	
		
