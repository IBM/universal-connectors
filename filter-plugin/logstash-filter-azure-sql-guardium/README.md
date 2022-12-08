# AzureSQL-Guardium Logstash filter plug-in

This is a [Logstash](https://github.com/elastic/logstash) filter plug-in for the univer***REMOVED***l connector that is featured in IBM Security Guardium. It parses events and mes***REMOVED***ges from the azureSQL audit log into a [Guardium record](https://github.com/IBM/univer***REMOVED***l-connectors/blob/main/common/src/main/java/com/ibm/guardium/univer***REMOVED***lconnector/commons/structures/Record.java) instance (which is a standard structure made out of several parts). The information is then sent over to Guardium. Guardium records include the accessor (the person who tried to access the data), the session, data, and exceptions. If there are no errors, the data contains details about the query "construct". The construct details the main action (verb) and collections (objects) involved.

Currently, this plug-in will work only on IBM Security Guardium Data Protection, not Guardium Insights.
The plug-in is free and open-source (Apache 2.0). It can be used as a starting point to develop additional filter plug-ins for Guardium univer***REMOVED***l connector.


## 1. Configuring the AzureSQL service

There are two ways to get AzureSQL audit data:

1. Univer***REMOVED***l Connector using Azure object storage plus a JDBC feed or
2. Guardium Streams using a Azure Event Hub

In this plugin we have used Object Storage
If the customer wants to get more insights into these options, they can reach out to Guardium Offering Managers.

### Procedure:
	1. Go to https://portal.azure.com/.
	2. Click Search Bar.
	3. Search for AzureSQL.
	4. Click on "Create" Button.
		<The button name is 'Create'>
	5. Select SQL databases option.
	6. Choose Resource Type depending on requirements(Single database,Elastic pool).
	7. Click on "Create" Button.
	8. Select Existing Resource group or Create New one.
	9. Provide Database Name.
	10. Click on "Create New" Server Button.
		<The button name is 'Create new' under server field>
	11. Provide Server Name and Select appropriate Location .
	12. Select appropriate Authentication method as per requirement.
	13. Provide "Server admin login name and Password.
	14. Click on "OK" Button.
	15. Select the "Compute + storage" Configuration depending on requirement.
	16. Click "Review + Create" Button.
	17. Verify the Configuration and then Click on "Create" Button.
	18. Again Click on Search Bar:
			a. Search for Storage Account
			b. Click on "create" to create new storage account
			c. Select resource group which you have created.
			d. Provide Storage Account Name.
			e. Select appropriate location.
			f. Choose additional configuration as per requirement.
			g. Click on "Review + create" Button.
			h. After Review Click on "Create" Button	

## 2. Enabling Auditing

	1. Click on Tap left Corner Menu button.
	2. Go to "Resource groups" tab.
	3. Click on Resource Group:
			a. Select SQL Server you have created
			b. Click on "Show Firewall setting" options.
			c. Add Client IPaddress by clicking "Add Client IP" Button.
			d. Add Public IPaddress of gmachine which is required to capture traffic.
				Use below command for getting public IPaddress of gmachine:
				curl ipinfo.io/ip
			e. Click on "Save" Button.
	4. To enable auditing:
			a. Select your SQL Database .
			b. In search bar search for 'auditing'.
			c. In Auditing, Click on "Enable Azure SQL Auditing" Button.
			d. Select "Storage" Audit log destination Check box.
			e. Select Storage Account which you have created.
			f. Click on "Advanced properties", And select " Retention (Days)"
	5. Then ,Click on "Save" Button.
	
		
## 3. Connecting to AzureSQL Database:
		
		a. Start the SQL Server Management Studio and provide connection details. Enter the ‘server name’ and Provide the ‘username’ and the master ‘password’ that we had set while creating the database.
		b. Click on Connect.
		
## 4. Viewing the Audit logs

	Use below query and fill your storage-account-name,server_instance_name,DB-NAME in Query for viewing audit logs.
	
	SELECT event_time,succeeded,session_id,database_name,client_ip,server_principal_name,application_name,statement,server_instance_name,host_name,DATEDIFF_BIG(ns, '1970-01-01 00:00:00.00000', event_time) AS updatedeventtime,additional_information  FROM sys.fn_get_audit_file('https://<storage-account-name>.blob.core.windows.net/sqldbauditlogs/<server_instance_name>/<DB-NAME>', DEFAULT, DEFAULT) where action_id='BCM' and statement not like '%xproc%' and statement not like '%SPID%' and statement not like '%DEADLOCK_PRIORITY%' and application_name not like '%Microsoft SQL Server Management Studio - Tran***REMOVED***ct-SQL IntelliSense%' and DATEDIFF_BIG(ns, '1970-01-01 00:00:00.00000', event_time) > 0;


## 5. Finding Enrollment ID

	Follow the below steps to get Enrollment number:

	1.Log in to your Azure Enterprise account at https://ea.azure.com.
	2.Top left corner you can see your "Enrollment ID".
	3.Copy the "Enrollment ID" and ***REMOVED***ve it for later use.
	Note: You need to be an Enterprise Administrator to access this.	
		
## 6. Getting the JDBC Connection String.

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
	
	
## 7. Configuring the AzureSQL filters in Guardium

The Guardium univer***REMOVED***l connector is the Guardium entry point for native audit logs. The Guardium univer***REMOVED***l connector identifies and parses the received events, and converts them to a standard Guardium format. The output of the Guardium univer***REMOVED***l connector is forwarded to the Guardium sniffer on the collector, for policy and auditing enforcements. Configure Guardium to read the native audit logs by customizing the azureSQL template.


#### Before you begin
• You must have LFD policy enabled on the collector. The detailed steps can be found in step 4 on [this page](https://www.ibm.com/docs/en/guardium/11.4?topic=dpi-installing-testing-filter-input-plug-in-staging-guardium-system).

• You must have permission for the S-Tap Management role. The admin user includes this role by default.

• Download the [Azure-SQL-Offline-Package.zip](https://github.com/IBM/univer***REMOVED***l-connectors/blob/main/filter-plugin/logstash-filter-azure-sql-guardium/AzureSQLOverJdbcPackage/AzureSQL/Azure-SQL-Offline-Package.zip) plug-in.

• Download the mssql-jdbc-7.4.1.jre8 from [here](https://jar-download.com/artifacts/com.microsoft.sqlserver/mssql-jdbc/7.4.1.jre8)

#### Procedure : 

1. On the collector, go to Setup > Tools and Views > Configure Univer***REMOVED***l Connector.
2. First enable the Univer***REMOVED***l Guardium connector, if it is di***REMOVED***bled already.
3. Click Upload File and select the offline [Azure-SQL-Offline-Package.zip](https://github.com/IBM/univer***REMOVED***l-connectors/blob/main/filter-plugin/logstash-filter-azure-sql-guardium/AzureSQLOverJdbcPackage/AzureSQL/Azure-SQL-Offline-Package.zip) plug-in. After it is uploaded, click OK.
4. Again click Upload File and select the offline mssql-jdbc-7.4.1.jre8 file. After it is uploaded, click OK. . 
5. Click the Plus sign to open the Connector Configuration dialog box.
6. Type a name in the Connector name field.
7. Update the input section to add the details from [azureSQLJDBC.conf](https://github.com/IBM/univer***REMOVED***l-connectors/blob/main/filter-plugin/logstash-filter-azure-sql-guardium/azureSQLJDBC.conf) file's input part, omitting the keyword "input{" at the beginning and its corresponding "}" at the end.
	Note : • For Guardium Data Protection version 11.3, add the following line to the input section:
	'jdbc_driver_library => "${THIRD_PARTY_PATH}/mssql-jdbc-7.4.1.jre8.jar"'
		• If auditing was configured a while before the UC, the UC will still process all previous records, since they were already audited by the database.
		• For moderate to large amounts of data, include pagination to facilitate the audit and to avoid out-of-memory errors. Use the parameters in the input section below when using a JDBC connector
			jdbc_paging_enabled => true
			jdbc_page_size => <size> 
8. The "type" fields should match in the input and the filter configuration sections. This field should be unique for every individual connector added.
9. If using two jdbc plug-ins on the ***REMOVED***me machine, the last_run_metadata_path file name should be different.
10. Update the filter section to add the details from [azureSQLJDBC.conf](https://github.com/IBM/univer***REMOVED***l-connectors/blob/main/filter-plugin/logstash-filter-azure-sql-guardium/azureSQLJDBC.conf) file's filter part, omitting the keyword "filter{" at the beginning and its corresponding "}" at the end.
11. Click Save. Guardium validates the new connector, and enables the univer***REMOVED***l connector if it was di***REMOVED***bled. After it is validated, the connector appears in the Configure Univer***REMOVED***l Connector page.


## 8. JDBC Load Balancing Configuration

	In AzureSQL JDBC input plug-in , we distribute load between two machines based on Even and Odd "session_id"
	
### Procedure

	On First G Machine,in input section for JDBC Plugin update "statement" field like below:
	
	SELECT event_time,succeeded,session_id,database_name,client_ip,server_principal_name,application_name,statement,server_instance_name,host_name,DATEDIFF_BIG(ns, '1970-01-01 00:00:00.00000', event_time) AS updatedeventtime,additional_information  FROM sys.fn_get_audit_file('https://<storage-account-name>.blob.core.windows.net/sqldbauditlogs/<server_instance_name>/<DB-NAME>', DEFAULT, DEFAULT) where action_id='BCM' and statement not like '%xproc%' and statement not like '%SPID%' and statement not like '%DEADLOCK_PRIORITY%' and application_name not like '%Microsoft SQL Server Management Studio - Tran***REMOVED***ct-SQL IntelliSense%' and session_id%2 = 0 and DATEDIFF_BIG(ns, '1970-01-01 00:00:00.00000', event_time) > :sql_last_value order by event_time;

		
		
	On Second G machine ,in input section for JDBC Plugin update "statement" field like below:
	
	SELECT event_time,succeeded,session_id,database_name,client_ip,server_principal_name,application_name,statement,server_instance_name,host_name,DATEDIFF_BIG(ns, '1970-01-01 00:00:00.00000', event_time) AS updatedeventtime,additional_information  FROM sys.fn_get_audit_file('https://<storage-account-name>.blob.core.windows.net/sqldbauditlogs/<server_instance_name>/<DB-NAME>', DEFAULT, DEFAULT) where action_id='BCM' and statement not like '%xproc%' and statement not like '%SPID%' and statement not like '%DEADLOCK_PRIORITY%' and application_name not like '%Microsoft SQL Server Management Studio - Tran***REMOVED***ct-SQL IntelliSense%' and session_id%2 = 1 and DATEDIFF_BIG(ns, '1970-01-01 00:00:00.00000', event_time) > :sql_last_value order by event_time;
	
		