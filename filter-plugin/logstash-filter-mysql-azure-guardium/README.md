# Azure MySQL-Guardium Logstash filter plug-in
This is a [Logstash](https://github.com/elastic/logstash) filter plug-in for the universal connector that is featured in IBM Security Guardium. It parses events and messages from the Azure MySQL audit log into a [Guardium record](https://github.com/IBM/universal-connectors/blob/main/common/src/main/java/com/ibm/guardium/universalconnector/commons/structures/Record.java) instance (which is a standard structure made out of several parts). The information is then sent over to Guardium. Guardium records include the accessor (the person who tried to access the data), the session, data, and exceptions. If there are no errors, the 
data that contains SQL commands are not parsed by this plug-in but rather forwarded as it is to Guardium to do the SQL parsing.

The plug-in is free and open-source (Apache 2.0). It can be used as a starting point to develop additional filter plug-ins for the Guardium universal connector.

## 1. Configuring the Azure MySQL service
### There are following ways to get Azure MySQL audit data:
1. Azure Event Hub
2. Azure Storage
3. Log Analytics Workspace

In this plugin we have used Azure Event Hub.

### Procedure:
1. Login to https://portal.azure.com.
2. Search for and select Azure Database for MySQL servers in the search bar.
3. Click on 'Create' button.
4. On the Select Azure Database for MySQL deployment option page, select Flexible server option.
5. On the Basics tab, provide the below details:
      - Subscription: Select your subscription name.
      - Resource group: Select existing resource group or create new one.
      - Server name: User need to provide a unique name that identifies your flexible server. 
	  -	Region: Select the region or location.
	  -	MySQL version: Choose latest Version (here 8.0).
	  -	Workload type: User can choose size as per the requirement.
	  -	Compute + storage: Server configuration can be changed as per the requirement.
	  -	Availability zone: No preference, can be specified as per your requirement.
	  -	Authentication method: MySQL authentication only.
	  -	Admin username: Provide Username.
	  -	Password: Provide Password.
6. Under the Networking tab, for Connectivity method select Public access.
7. For configuring Firewall rules, select Add current client IP address and check the checkbox to allow public access from any azure service.
8. Click on Review + create to review your flexible server configuration.
9. Verify the configuration and then click on 'Create' Button.
10. When deployment is done then server is ready for use.

## 2. Enabling Audit logs
1. Select your Azure Database for MySQL server.
2. Under settings, select server parameter.
3. Update the audit_log_enabled parameter to ON.
4. Select the event types to be logged by updating the below audit_log_events parameter:
      - CONNECTION includes, 
           - Connection initiation (successful or unsuccessful).
		   - User reauthentication with different user/password during session.
		   - Connection termination.
	  - GENERAL includes, 
		   - DML_SELECT, DML_NONSELECT, DML, DDL, DCL, and ADMIN.
		   
## 3. Azure Event Hub Namespace Creation and connection
### Azure Event Hub Namespace Creation
1. Login to https://portal.azure.com. 
2. Search event hub in search bar.
3. Click on create event hubs namespace button.
4. To create namespace:
	  - Select the Subscription in which you want to create the namespace.
	  -	Select the Resource group you created in the previous step.
	  -	Enter a unique name for the namespace.
	  -	Select same location for the namespace which you selected for server.
	  -	Choose appropriate pricing tier.
	  -	Leave the throughput units (or processing units for standard and premium tier) settings as it is.
	  -	Click on Review + Create at the bottom of the page.
	  -	Review the settings and select Create.
	  -	After successful creation recently created namespace will appear in resource group.

### Azure Event Hub Connection
1. To create event hub :
	  -	Go to the Event Hubs Namespace page.
	  -	Click on '+ Event Hub' to add event hub.
	  -	Enter unique name for event hub.
	  - Choose at least as many partitions as you expect that are required during the peak load of your application for that particular event hub.
	  	- Ex: If user wants to generate traffic from 2 DB instances then partition count should be at least 2 OR more than that.
	  - Click on Review+create.
	  -	Review the settings and click on Create button.

## 4. Azure Storage Accounts Creation
1. Login to https://portal.azure.com. 
2. Search Storage accounts in search bar.
3. Click on Create button.
4. Basic Tab:
	  - Select the Subscription in which you want to create the Storage account.
	  -	Select or create new Resource group.
	  -	Enter a unique name for Storage account.
	  -	Select same region for the storage account which you selected for server.
	  -	Choose any Performance type.
	  -	Select Geo-redundant(GRS) Redundancy configuration.
	  -	Select Make read access to data option.
	  - Click on Next:Advance button.
5. Advanced tab:
	  - Require secure transfer option should be selected.
	  - Allow enabling public access option should be selected.
	  - Enable storage account key access option should be selected.
	  - Select latest TLS version.
	  - Permitted scope should be the default value(From any storage account).
	  - Other parameters Hierarchical Namespace, Access protocols, Blob storage and Azure Files should be default value provided by azure.
	  - Click on Next:Networking button.
6. Networking tab:
	  - Enable public access from all networks for Network access.
	  -	Select Microsoft network routing option for Routing preference.
	  -	Click on Next:Data protection button.
7. Data protection tab:
	  - Using by default values provided by azure.
	  - Click on Next:Encryption button.
8. Encryption tab:
	  - Encryption type should be Microsoft-managed key(MMK).
	  - Enable support for customer-managed keys option should be by default value(blobs and files).
	  - By default, infrastructure encryption should not be enabled.
	  - Click on Next:Tags button.
9.  On the Tags tab no need to select anything and click Next:Review button.
10. Click on Create button after review all the parameters.

## 5. Stream Logs to an Event Hub
1. Login to https://portal.azure.com.
2. Go to server in your azure portal.
3. From Monitoring, select Diagnostics settings option and do either of the following:
4. To change existing settings, select Edit setting.
5. To add new settings, select Add diagnostics setting.
6. For adding new settings do following:
	  - Give name to setting.
	  - Select MySQL Audit Logs from categories.
	  - In Destination details choose Archive to a storage account.
	  - In Archive to a storage account, select Storage account as created above.
	  - In Destination details choose Stream to an event hub.
	  - In stream to event hub, select namespace name and and event hub name as created above. Keep event hub policy name as it is.		
      - Select Save to save the setting.
7. After about 15 minutes, verify that events are displayed in your event hub.
8. Configurations needed to monitor traffic from single Event Hub, when UCs are configred on two separate Collectors Procedure, we need to do the following steps:
	  -  We can create namespace in azure event hub as per given above but select standard pricing tier instead of basic in pricing tier configuration.
	  - After creation of namespace we can create eventhub and connection string as per mention above.
	  - After successful creation of eventhub we can add a consumer group to event hub as follows:
	  	  - In the list of event hubs, select your event hub.
		  - On the Event Hubs instance page, from entities select consumer group.
		  - In consumer group click on add button from top.
		  - Give the name to consumer group and create the consumer group.
	  - Stream logs to event hub as mentioned as above.
	  - For gmachine , We have to use these consumer group name in input section of configuration file as follows:
	  	  - On one machine in input section of configuration file in consumer group field give name as `$Default`.
		  - On other machine in input section of configuration file in consumer group field give name of other consumer group.
		  - Keep all other configurations as it is.

## 6. Connecting to Azure MySQL Database:
1. Go to the server and click on Connect button in Overview page.
2. An interface will open and then provide the password which you have given while creating the server and click enter.
3. You will successfully able to connect Mysql and execute queries.

## 7. Limitations 
1. The Audit log doesn't contain a server IP. The default value is set to __0.0.0.0__ for the __Server IP__.
2. Error events will cause a duplicated success events in Guardium due to duplicate events in the Azure EventHub audit log.
3. Azure EventHub is not capturing __Syntactical error__ queries logs, __Login Failed__ logs, and logs using __az commands__ (Ex below) and __REST API__.
      - Ex: az mysql flexible-server db create --resource-group <testresgroup> --server-name <testserver> --database-name <db>  
4. There are certain [limited privilages](https://learn.microsoft.com/en-us/azure/mysql/how-to-create-users) given by Azure MYSQL to users.
5. We are getting below extra logs while executing __USE <databasename>__ command:
	  - show tables
	  - show databses
6. The following important fields cannot be mapped with Azure Mysql logs:
	  - Source program
	  - Client HostName
7. Database name is not available in __General logs__, it only avialable at the time of __Disconnect__ and __Connect__(We must use __database name__ at the time of connection to get the database name).
8. Eventhub capturing identical duplicate logs for each query and same has been carrying to Guardium reports.
9. __Database name__ and __Service name__ are not identical when user execute queries using Third party tool (DB Visualizer/ MySQL Workbench).

## 7. Configuring the Azure MySQL filter in Guardium
The Guardium universal connector is the Guardium entry point for native audit logs. The Guardium universal connector identifies and parses the received events, and converts them to a standard Guardium format. The output of the Guardium universal connector is forwarded to the Guardium sniffer on the collector, for policy and auditing enforcements. Configure Guardium to read the native audit logs by customizing the Azure MySQL template.

### Before you begin
* Configure the policies you require. See [policies](https://github.com/IBM/universal-connectors/#policies) for more information.
* You must have permission for the S-Tap Management role. The admin user includes this role by default.
* Azure MySQL-Guardium Logstash filter plug-in is automatically available with Guardium Data Protection versions 12.x, 11.4 with appliance bundle 11.0p490 or later or Guardium Data Protection version 11.5 with appliance bundle 11.0p540 or later releases.

**Note**: For Guardium Data Protection version 11.4 without appliance bundle 11.0p490 or later or Guardium Data Protection version 11.5 without appliance bundle 11.0p540 or later, download the [guardium_logstash-offline-plugins-azure-mysql.zip](https://github.com/IBM/universal-connectors/raw/main/filter-plugin/logstash-filter-mysql-azure-guardium/AzureMySQLOverAzureEventHub/guardium_logstash-offline-plugins-azure-mysql.zip) plug-in. (Do not unzip the offline-package file throughout the procedure).

### Procedure
1. On the collector, go to Setup > Tools and Views > Configure Universal Connector.
2. Before you upload the universal connector, enable the connector if it is disabled.
3. Click Upload File and select the offline [guardium_logstash-offline-plugins-azure-mysql.zip](https://github.com/IBM/universal-connectors/raw/main/filter-plugin/logstash-filter-mysql-azure-guardium/AzureMySQLOverAzureEventHub/guardium_logstash-offline-plugins-azure-mysql.zip) plug-in. After it is uploaded, click OK.
4. Click the Plus sign to open the Connector Configuration dialog box.
5. Type a name in the Connector name field.
6. Update the input section to add the details from [azure_mysql.conf](azure_mysql.conf) file's input part, omitting the keyword "input{" at the beginning and its corresponding "}" at the end.
	 - Insert connection string primary key from shared access policies present in event hub namespace from azure portal.
	 - Insert the eventhub name in the EntityPath.
	 - Insert the Connection string in storage_connection from the Access Keys present in the Storage account from azure portal.
7. Update the filter section to add the details from [azure_mysql.conf](azure_mysql.conf) file's filter part, omitting the keyword "filter{" at the beginning and its corresponding "}" at the end.
8. The "type" field should match in the input and filter configuration section. This field should be unique for every individual connector added.
9. Click Save. Guardium validates the new connector, and enables the universal connector if it was disabled. After it is validated, it appears in the Configure Universal Connector page.
