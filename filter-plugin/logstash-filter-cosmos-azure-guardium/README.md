# Azure-Cosmos-Guardium Logstash filter plug-in
This is a [Logstash](https://github.com/elastic/logstash) filter plug-in for the universal connector that is featured in IBM Security Guardium. It parses events and messages from the Azure-Cosmos audit log into a [Guardium record](https://github.com/IBM/universal-connectors/blob/main/common/src/main/java/com/ibm/guardium/universalconnector/commons/structures/Record.java) instance (which is a standard structure made out of several parts). The information is then sent over to Guardium. Guardium records include the accessor (the person who tried to access the data), the session, data, and exceptions. If there are no errors, the 
data contains details about the query "construct". The construct details the main action (verb) and collections (objects) involved.

The plug-in is free and open-source (Apache 2.0). It can be used as a starting point to develop additional filter plug-ins for the Guardium universal connector.

## 1. Configuring the Azure-Cosmos service
### There are following ways to get Azure Cosmos audit data:
1. Azure Event Hub
2. Log Analytics Workspace

In this plugin we have used Azure Event Hub.

### Procedure:
1. Login to https://portal.azure.com.
2. From the Azure portal menu or the Home page, user can create Azure Cosmos DB Account by following below steps-
      - In Home page, select Create a resource.
	  - On the New page, search for and select Azure Cosmos DB.
	  - On the Select API option page, click on Create option within the NoSQL.
3. On the Create Azure Cosmos DB Account page, provide the below details.
      - Subscription: Select a valid subscription to create an Azure Cosmos DB account.​
      - Resource group: Choose the existing resource group or create a new one by clicking the “Create New” option.​ 
      - Account name: User need to provide a name for their Azure Cosmos DB account.​
      - Region: Select the region or location.​
      - Capacity Mode: Select Provisioned Throughput to create an account in provisioned throughput mode. Select Serverless to create  an account in serverless mode.
	  -	Apply Azure Cosmos DB free tier discount: Apply or Do not apply.	
4. Keep the other tab values as it is, Finally click on the Review+Create button.
5. Review the settings you provide, and then select Create. It takes a few minutes to create the account. Wait for the portal page to display Your deployment is complete before moving on.
6. Select Go to resource to go to the Azure Cosmos DB account page.

## 2. Enabling Auditing
### Enable full-text query for logging query text
1. By enabling full-text query, user will be able to view the deobfuscated query for all requests within your Azure Cosmos DB account.
	  - To enable this feature, navigate to the Features page in the Azure Cosmos DB Account.
	  - Click on Diagnostics full-text query, then it will show full-text query panel on right side.
	  - Select Enable, this setting will then be applied withing the next few minutes.
	
Note: By default, the query text and its parameters are obfuscated to avoid logging personal data.

### Monitor Azure Cosmos DB data by using diagnostic settings in Azure
1. Login to https://portal.azure.com.
2. Navigate to your Azure Cosmos DB account. Open the Diagnostic settings pane under the Monitoring section.
3. After the page opens, you will be prompted to enable or disable the full-text query
according to your preference. You can choose to do so, or simply click on the 'Not
now' option, and then select the 'Add diagnostic setting' option.
4. In the Diagnostic settings pane, fill the form with the preferred categories. Provided here's a list of log categories.
	  - DataPlaneRequests: Logs back-end requests as data plane operations, which are requests executed to create, update, delete or retrieve data within the account.
	  - QueryRuntimeStatistics: This table details query operations executed against an API for NoSQL account. By default, the query text and its parameters are obfuscated to avoid logging personal data with full text query logging available by request.
	  - ControlPlaneRequests: Logs details on control plane operations, which include, creating an account, adding or removing a region, updating account replication settings etc.
5. Once you select your Categories details, then send your Logs to your preferred destination.

## 3. Viewing the Audit logs
### Azure Event Hub Connection:
    1. Search event hub in search bar.
    2. Select create event hubs namespace button.

    3. To create namespace:
           - Select the Subscription in which you want to create the namespace.
	       - Select the Resource group you created in the previous step.
	       - Enter a unique name for the namespace.
	       - Select a location for the namespace.
	       - Choose appropriate pricing tier.(here selected basic)
	       - Leave the throughput units (or processing units for standard and premium tier) settings as it is.
	       - Select Review + Create at the bottom of the page.Review the settings and select Create.
	       - After successful creation recently created namespace will appear in resource group.
	
	4. To create event hub :
	       - Go to the Event Hubs Namespace page.
           - Click on '+ Event Hub' to add event hub.
           - Enter unique name for event hub.
           - Choose at least as many partitions as you expect that are required during the peak load  of  your application for that particular event hub.
    	 Ex: If user wants to generate traffic from 2 DB instances then partition count should be at least 2 OR more than that.
           - Click on Review+create.
           - Review the settings and click on Create button.  
	  
    5. Connection string for a event hub:
	       - In the list of event hubs, select your event hub.
	       - On the Event Hubs instance page, from Settings select Shared access policies on the left menu.
	       - In shared access policies click on Add button from top.Give the name to policy and provide permission 'manage' and create the policy.  
	       - Select Connection string–primary key string from policy (it would be required in input plugin).
           
	6. Azure Storage Accounts Creation:
	         - Login to https://portal.azure.com.
		 - Search Storage accounts in search bar.
		 - Click on Create button.
		 - Basic Tab:
		        - Select the Subscription in which you want to create the Storage account.
	                - Select or create new Resource group.
	                - Enter a unique name for Storage account.
	                - Select same region for the storage account which you selected for server.
	                - Choose any Performance type.
	                - Select Geo-redundant(GRS) Redundancy configuration.
	                - Select Make read access to data option.
	                - Click on Next:Advance button.	
		 - Advanced tab:
		        - Require secure transfer option should be selected.
	                - Allow enabling public access option should be selected.
	                - Enable storage account key access option should be selected.
	                - Select latest TLS version.
	                - Permitted scope should be the default value(From any storage account).
	                - Other parameters Hierarchical Namespace, Access protocols, Blob storage and Azure Files should be default value provided by azure.
		        - Click on Next:Networking button.
		 - Networking tab:
	                - Enable public access from all networks for Network access.
	                - Select Microsoft network routing option for Routing preference.
	                - Click on Next:Data protection button.	
		 - Data protection tab:
		            - Using by default values provided by azure.
			    - Click on Next:Encryption button.
		 - Encryption tab:
		            - Encryption type should be Microsoft-managed key(MMK).
			    - Enable support for customer-managed keys option should be by default value(blobs and files).
			    - By default, infrastructure encryption should not be enabled.
			    - Click on Next:Tags button.
		 - On the Tags tab no need to select anything and click Next:Review button.
		 - Click on Create button after review all the parameters.
	7. Stream logs to an event hub:
            - Login to https://portal.azure.com.
            - Navigate to your Azure Cosmos DB account. From Monitoring, select Diagnostics settings option and do either of the following:
		       - To change existing settings, select Edit setting.
		       - To add new settings, select Add diagnostics setting.
            - For adding new settings do following:
		       - Give name to setting.	
		       - Select DataPlaneRequests, QueryRuntimeStatistics, ControlPlaneRequests from categories.
		       - In Destination details choose Archive to a storage account.
	               - In Archive to a storage account, select Storage account as created above.
		       - In Destination details choose Stream to an event hub.
		       - In stream to event hub, select namespace name and event hub name as created above. Keep event hub policy name as it is.		
		       - Select Save to save the setting.	 						   
    8. After about 15 minutes, verify that events are displayed in your event hub.
	9. Configurations needed to monitor traffic from single Event Hub, when UC is configured on single Collectors 
	        - For a single machine, in the input section of the configuration file, in the consumer group field can be given the name as $Default.
	10. Configurations needed to monitor traffic from single Event Hub, when UCs are configred on two separate Collectors
		Procedure :-
		We need to do the following steps:
			- We can create namespace in azure event hub as per given above but select standard pricing tier instead of basic in pricing tier configuration.
			- After creation of namespace we can create eventhub and connection string as per mention above.
			- After successful creation of eventhub we can add a consumer group to event hub as follows:
				- In the list of event hubs, select your event hub.
				- On the Event Hubs instance page, from entities select consumer group.
				- In consumer group click on add button from top.
				- Give the name to consumer group and create the consumer group.
			- Stream logs to event hub as mentioned as above.
			- For gmachine , We have to use these consumer group name in input section of configuration file as follows:
				- On one machine in input section of configuration file in consumer group field give name as $Default.
				- On other machine in input section of configuration file in consumer group field give name of other consumer group.
				- Keep all other configurations as it is.

## 4. Connect to Azure Cosmos Database
### Insert/Update data through Data Explorer
1. Login to https://portal.azure.com.
2. Navigate to your Azure Cosmos DB account. Open the Data Explorer pane and then select New Container to create a container.
3. After the container is created, use the Data Structures browser to find and open it. For example, a container with the name Families has been created in the image below.
4. In  Families container, you will see the Items option right below the name of the container. Open this option and you'll see a button, in the menu bar in center of the screen, to create a 'New Item'. You will use this feature to create the JSON items.

### Query Execution through Data Explorer
1. Select New SQL Query.
2. Write the SQL query and select Execute Query.

## 5. Limitations
1. The following important fields couldn't be mapped with Azure-Cosmos audit logs:
   - Source program : field is left blank, as this information is not embedded in the messages pulled from Azure.
   - ServerIP : field is populated with 0.0.0.0, as this information is not embedded in the messages pulled from Azure.
   - Client HostName : Not available with logs
   - dbUser : field is populated with NA, as it is not available in ControlPlaneRequest, QueryRuntimeStatistics logs.
   - Exception > sqlString : field is populated with NA, as it is not available in Error logs.
2. Logs will not capture for Failed login attempts.
3. For DataPlaneRequest dbUser is NA when AADPrincipalId is Empty.
4. Data Explorer and CLI doesn't support Creation, Updation and Deletion for User and Permission Operations; only the REST API does.
5. REST API doesn't support Creation, Updation and Deletion for Role Definition and Role Assignment Operations; But the Data Explorer and CLI does.
6. Data Explorer, CLI and REST API doesn't support Trigger execution; It can only be called via programs.
7. In case of REST API, ControlPlaneRequest logs will not generate.
8. To get an Azure Active Directory(AAD) token, we need to give few permissions that disable read-write keys and only allows read-only keys.
9. Through AAD token, we cannot use any Azure Cosmos DB data plane SDK to authenticate management operations(create/delete database, container, triggers, Stored procedures, user defined functions) with an Azure AD identity.Instead, you must use Azure role-based access control through one of the following options:
   - Azure Resource Manager templates (ARM templates)
   - Azure PowerShell scripts
   - Azure CLI scripts
10. Application is the only option to work with Azure CosmosDB SDK using Azure Active Directory(AAD) token and it  gives only ApplicationID as AAD PrincipalID which we use for DBUser.    

## 6. Configuring the Azure-Cosmos filter in Guardium
The Guardium universal connector is the Guardium entry point for native audit logs. The Guardium universal connector identifies and parses the received events, and converts them to a standard Guardium format. The output of the Guardium universal connector is forwarded to the Guardium sniffer on the collector, for policy and auditing enforcements. Configure Guardium to read the native audit logs by customizing the Azure-Cosmos template.

### Before you begin
* Configure the policies you require. See [policies](https://github.com/IBM/universal-connectors/#policies) for more information.
* You must have permission for the S-Tap Management role. The admin user includes this role by default.
* Download the [guardium_logstash-offline-plugin-azure-cosmos.zip](AzureCosmosOverAzureEventHub/guardium_logstash-offline-plugin-azure-cosmos.zip) plug-in.
* Download the plugin filter configuration file [azure_cosmos.conf](azure_cosmos.conf).

### Procedure
1. On the collector, go to Setup > Tools and Views > Configure Universal Connector.
2. Enable the connector if it is already disabled, before proceeding uploading of the UC.
3. Click Upload File and select the offline [guardium_logstash-offline-plugin-azure-cosmos.zip](AzureCosmosOverAzureEventHub/guardium_logstash-offline-plugin-azure-cosmos.zip) plug-in. After it is uploaded, click OK.
4. Click the Plus sign to open the Connector Configuration dialog box.
5. Type a name in the Connector name field.
6. Update the input section to add the details from [azure_cosmos.conf](azure_cosmos.conf) file's input part, omitting the keyword "input{" at the beginning and its corresponding "}" at the end.
    - Insert the Connection string in storage_connection from the Access Keys present in the Storage account from azure portal.
7. Update the filter section to add the details from [azure_cosmos.conf](azure_cosmos.conf) file's filter part, omitting the keyword "filter{" at the beginning and its corresponding "}" at the end.
8. The "type" field should match in the input and filter configuration section. This field should be unique for every individual connector added.
9. Click Save. Guardium validates the new connector, and enables the universal connector if it was disabled. After it is validated, it appears in the Configure Universal Connector page.


