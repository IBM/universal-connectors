# Azure-Cosmos-Guardium Logstash filter plug-in
### Meet Azure Cosmos
* Tested versions: 4.48.0
* Environment: Azure
* Supported inputs: Azure Event Hub (pull)
* Supported Guardium versions:
  * Guardium Data Protection: 11.4 and above
  * Guardium Insights: 3.3.1
  
This is a [Logstash](https://github.com/elastic/logstash) filter plug-in for the universal connector that is featured in IBM Security Guardium. It parses events and messages from the Azure-Cosmos audit log into a [Guardium record](https://github.com/IBM/universal-connectors/blob/main/common/src/main/java/com/ibm/guardium/universalconnector/commons/structures/Record.java) instance (which is a standard structure made out of several parts). The information is then sent over to Guardium. Guardium records include the accessor (the person who tried to access the data), the session, data, and exceptions. If there are no errors, the 
data contains details about the query "construct". The construct details the main action (verb) and collections (objects) involved.

The plug-in is free and open-source (Apache 2.0). It can be used as a starting point to develop additional filter plug-ins for the Guardium universal connector.

## 1. Configuring the Azure-Cosmos service
### You can get the Azure Cosmos audit data from the following two locations:
1. Azure Event Hub
2. Log Analytics Workspace

In this example, we used Azure Event Hub.

### Procedure:
1. Login to https://portal.azure.com.
2. From the Azure portal menu or the home page, create an Azure Cosmos DB account by following these steps-
      - If you are on the home page, select **Create a resource**.
	  - On the new page, search for and select **Azure Cosmos DB**.
	  - On the Select API option page, click **Create option** in NoSQL.
3. If you are on the **Create Azure Cosmos DB Account** page, provide the following details.
      - Subscription: Select a valid subscription to create an Azure Cosmos DB account.​
      - Resource group: Choose the existing resource group or create a new one by clicking **Create New**.​ 
      - Account name: Provide a name for your Azure Cosmos DB account.​
      - Region: Select the region or location.​
      - Capacity Mode: Select **Provisioned Throughput** to create an account in provisioned throughput mode. Select **Serverless** to create  an account in serverless mode.
	  -	Apply Azure Cosmos DB free tier discount: Click **Apply** or **Do not apply**.	
4. Keep the other tab values as is. Finally, click on **Review+Create**.
5. Review the settings you provided, and then select **Create**. It takes a few minutes to create the account. Wait for the portal page to display "Your deployment is complete" before moving on.
6. Select **Go to resource** to go to the Azure Cosmos DB account page.

## 2. Enabling Auditing
### Enable full-text query for logging query text
1. By enabling full-text query, you will be able to view the deobfuscated query for all requests within your Azure Cosmos DB account.
	  - To enable this feature, navigate to the Features page in the Azure Cosmos DB Account.
	  - Click on **Diagnostics full-text query**. A full-text query panel appears.
	  - Select **Enable**. It can take a few minutes before your selection is applied. 
	
***Note: By default, the query text and its parameters are obfuscated to avoid logging personal data.***

### Monitoring Azure Cosmos DB data by using diagnostic settings in Azure
1. Login to https://portal.azure.com.
2. Navigate to your Azure Cosmos DB account. Open the Diagnostic settings pane under the Monitoring section.
3. After the page opens, you will be prompted to enable or disable the full-text query
according to your preference. You can choose to do so, or click **'Not
now** and then select **Add diagnostic setting**.
4. In the Diagnostic settings pane, fill in the form with your preferred categories. Here is a list of log categories.
	  - DataPlaneRequests: Logs back-end requests as data plane operations, which are requests executed to create, update, delete, or retrieve data within the account.
	  - QueryRuntimeStatistics: This table details query operations executed against an API for a NoSQL account. By default, the query text and its parameters are obfuscated to avoid logging personal data with full text query logging available by request.
	  - ControlPlaneRequests: Logs details on control plane operations, which include creating an account, adding or removing a region, updating account replication settings, etc.
5. Select your categories details, and then send your logs to your preferred destination.

## 3. Viewing the Audit logs
### Azure Event Hub Connection:
    1. Search "event hub" in the search bar.
    2. Select **create event hubs namespace**.

    3. To create a namespace:
           - Select the subscription in which you want to create the namespace.
	       - Select the resource group you created in the previous step.
	       - Enter a unique name for the namespace.
	       - Select a location for the namespace.
	       - Choose the appropriate pricing tier. (In this example, we selected basic).
	       - Leave the throughput units (or processing units for standard and premium tier) settings as is.
	       - Select **Review + Create**. Review the settings and select **Create**.
	       - Your recently created namespace appears in **resource group**.
	
	4. To create an event hub:
	       - Go to the Event Hubs Namespace page.
           - Click **+ Event Hub**.
           - Enter a unique name for the event hub.
           - Choose at least the maximum number of partitions that you expect to require during peak usage for this event hub.
    	 For example, if you want to generate traffic from 2 DB instances, then choose at least 2 partitions if not more.
           - Click **Review+create**.
           - Review the settings and click **Create**.  
	  
    5. Connection string for an event hub:
	       - In the list of event hubs, select your event hub.
	       - On the Event Hubs instance page, go to **Settings** > **Shared access policies** > **Add**.
	       - Name the policy, click **manage** to provide permissions, and create the policy.  
	       - Select Connection string–primary key string from policy (it would be required in input plugin).
           
	6. Azure Storage Accounts Creation:
	         - Login to https://portal.azure.com.
		 - Search Storage accounts in search bar.
		 - Click **Create**.
		 - Basic Tab:
		        - Select the subscription in which you want to create the storage account.
	                - Select an existing resource group or create a new one.
	                - Enter a unique name for the storage account.
	                - Select the same region for the storage account that you selected for the server.
	                - Choose any performance type.
	                - Select **Geo-redundant(GRS) Redundancy configuration**.
	                - Select **Make read access to data**.
	                - Click **Next:Advance**.	
		 - Advanced tab:
		        - **Require secure transfer** should already be selected.
	                - **Allow enabling public access** should already be selected.
	                - **Enable storage account key access** should already be selected.
	                - Select the latest TLS version.
	                - Permitted scope should display the default value (from any storage account).
	                - The remaining parameters (Hierarchical Namespace, Access protocols, Blob storage, and Azure Files) should display the default values provided by Azure.
		        - Click **Next:Networking**.
		 - Networking tab:
	                - Enable public access from all networks for **Network access**.
	                - Select **Microsoft network routing** for **Routing preference**.
	                - Click **Next:Data protection**.	
		 - Data protection tab:
		            - Keep the default values provided by Azure.
			    - Click **Next:Encryption**.
		 - Encryption tab:
		            - **Encryption type** should already be set to **Microsoft-managed key(MMK)**.
			    - **Enable support for customer-managed keys** should be set to the default value (**blobs and files**).
			    - By default, **Infrastructure encryption** should not be enabled.
			    - Click **Next:Tags**.
		 - For the Tags tab, make no changes and click **Next:Review**.
		 - Click **Create** after you review all the parameters.
	7. Stream logs to an event hub:
            - Login to https://portal.azure.com.
            - Navigate to your Azure Cosmos DB account. From **Monitoring**, select **Diagnostics settings** and do either of the following:
		       - To change existing settings, select **Edit setting**.
		       - To add new settings, select **Add diagnostics setting**.
            - To add new settings, do the following:
		       - Name the setting.	
		       - In **Categories**, select **DataPlaneRequests**, **QueryRuntimeStatistics**, and **ControlPlaneRequests**.
		       - In **Destination details**, choose **Archive to a storage account**.
	               - In **Archive to a storage account**, select the storage account you created previously.
		       - In **Destination details**, choose **Stream to an event hub**.
		       - In **Stream to event hub**, select the namespace name and event hub name that you created previously. Keep the event hub policy name as is.		
		       - Click **Save**.	 						   
    8. After about 15 minutes, verify that events are displayed in your event hub.
	9. You need the following configurations to monitor traffic from a single event hub when the universal connector is configured on single collectors: 
	        - For a single machine- use the name $Default in the input section of the configuration file in the consumer group field.
	10. You need the following configurations to monitor traffic from a single event hub when universal connectors are configured on two separate collectors:
		Procedure:
		
			- Create a namespace in Azure event hub with the details provided previously, but select the standard pricing tier instead of basic in the pricing tier configuration.
			- After you create a namespace, create an event hub and connection strings as described previously.
			- Then, add a consumer group to the event hub as follows:
				- Select your event hub from the list of event hubs.
				- on the Event Hubs instance page, select **consumer group** from **entities**.
				- In **consumer group**, click **Add**.
				- Name and create the consumer group.
			- Stream logs to the event hub as described previously.
			- For the gmachine , use these consumer group names in the input section of the configuration file as follows:
				- For one machine, use the name $Default.
				- For the other machine, use the name of the other consumer group.
				- Keep all other configurations as is.

## 4. Connecting to the Azure Cosmos Database
### Insert/Update data through Data Explorer
1. Login to https://portal.azure.com.
2. Navigate to your Azure Cosmos DB account. Open the Data Explorer pane, and then select **New Container** to create a container.
3. After the container is created, use the Data Structures browser to find and open it. 
4. In  the container you created, click **Items** > **New Item**. Use this feature to create the JSON items.

### Query Execution through Data Explorer
1. Select **New SQL Query**.
2. Write the SQL query and click **Execute Query**.

## 5. Limitations
1. The following important fields couldn't be mapped with Azure-Cosmos audit logs:
   - Source program : this field is left blank, as this information is not embedded in the messages pulled from Azure.
   - ServerIP : this field is populated with 0.0.0.0, as this information is not embedded in the messages pulled from Azure.
   - Client HostName : Not available with logs.
   - dbUser : this field is populated with NA, as it is not available in ControlPlaneRequest, QueryRuntimeStatistics logs.
   - Exception > sqlString : this field is populated with NA, as it is not available in Error logs.
2. Logs are not captured for failed login attempts.
3. For DataPlaneRequest, dbUser is NA when AADPrincipalId is empty.
4. Data Explorer and CLI don't support creation, updating, and deletion for user and permission operations; only the REST API does.
5. REST API doesn't support creation, updating, and deletion for role definition and role assignment operations; But the Data Explorer and CLI does.
6. Data Explorer, CLI and REST API don't support trigger execution; It can only be called via programs.
7. In case of REST API, ControlPlaneRequest logs will not generate.
8. To get an Azure Active Directory(AAD) token, you need to give a few permissions that disable read-write keys and only allow read-only keys.
9. With the AAD token, you cannot use any Azure Cosmos DB data plane SDK to authenticate management operations (create/delete database, container, triggers, stored procedures, user-defined functions) with an Azure AD identity. Instead, use Azure role-based access control through one of the following options:
   - Azure Resource Manager templates (ARM templates)
   - Azure PowerShell scripts
   - Azure CLI scripts
10. Application is the only option to work with Azure CosmosDB SDK using Azure Active Directory(AAD) token and it  gives only ApplicationID as AAD PrincipalID which we use for DBUser.    

## 6. Configuring the Azure-Cosmos filter in Guardium
The Guardium universal connector is the Guardium entry point for native audit logs. The Guardium universal connector identifies and parses the received events, and converts them to a standard Guardium format. The output of the Guardium universal connector is forwarded to the Guardium sniffer on the collector, for policy and auditing enforcements. Configure Guardium to read the native audit logs by customizing the Azure-Cosmos template.

### Before you begin
* Configure the policies you require. See [policies](https://github.com/IBM/universal-connectors/#policies) for more information.
* You must have permission for the S-Tap Management role. The admin user includes this role by default.
* Download the [guardium_logstash-offline-plugin-azure-cosmos.zip](https://github.com/IBM/universal-connectors/releases/download/v1.6.0/logstash-filter-azure_cosmos_guardium_filter.zip) plug-in. (Do not unzip the offline-package file throughout the procedure).
* Download the plug-in filter configuration file [azure_cosmos.conf](azure_cosmos.conf).

### Note
* While upgrading from v11.5 p535 or SP6505 to p540, upload [Azure Cosmos updated plugin](https://github.com/IBM/universal-connectors/releases/download/v1.5.6/logstash-filter-azure_cosmos_guardium_filter.zip).

### Procedure
1. On the collector, go to Setup > Tools and Views > Configure Universal Connector.
2. Enable the connector if it is disabled.
3. Click Upload File and select the offline [guardium_logstash-offline-plugin-azure-cosmos.zip](https://github.com/IBM/universal-connectors/releases/download/v1.6.0/logstash-filter-azure_cosmos_guardium_filter.zip) plug-in. After it is uploaded, click OK.
4. Click the Plus sign to open the Connector Configuration dialog box.
5. Type a name in the Connector name field.
6. Update the input section to add the details from [azure_cosmos.conf](azure_cosmos.conf) file's input part, omitting the keyword "input{" at the beginning and its corresponding "}" at the end.
    - Insert the Connection string in storage_connection from the Access Keys present in the Storage account from the Azure portal.
7. Update the filter section to add the details from [azure_cosmos.conf](azure_cosmos.conf) file's filter part, omitting the keyword "filter{" at the beginning and its corresponding "}" at the end.
8. The "type" fields should match in the input and filter configuration sections. This field should be unique for every individual connector added.
9. Click Save. Guardium validates the new connector, and enables the universal connector if it was disabled. After it is validated, it appears in the Configure Universal Connector page.


