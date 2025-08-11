# Azure-Databricks-Guardium Logstash filter plug-in
### Meet Azure Databricks
* Tested versions: Databricks Runtime version 11.2 and above
* Environment: Azure
* Supported inputs: Azure Event Hub (pull)
* Supported Guardium versions:
  * Guardium Data Protection: 11.4 and above

This is a [Logstash](https://github.com/elastic/logstash) filter plug-in for the universal connector that is featured in IBM Security Guardium. It parses events and messages from the Azure-Databricks audit log into a [Guardium record](https://github.com/IBM/universal-connectors/blob/main/common/src/main/java/com/ibm/guardium/universalconnector/commons/structures/Record.java) instance (which is a standard structure made out of several parts). The information is then sent over to Guardium. Guardium records include the accessor (the person who tried to access the data), the session, data, and exceptions. If there are no errors, the
data contains details about the query "construct". The construct details the main action (verb) and collections (objects) involved. 



The plug-in is free and open-source (Apache 2.0). It can be used as a starting point to develop additional filter plug-ins for the Guardium universal connector.

## 1. Configuring the Azure-Databricks service
### You can get the Azure Databricks audit data from Azure Event Hub

### Databricks Resource creation on Azure:
1. Login to https://portal.azure.com.
2. From the Azure portal menu or the home page, create an Azure Databricks DB account by following these steps-
    - If you are on the home page, select **Create a resource**.
    - On the new page, search for and select **Azure Databricks**.
4. Select **Subscription** and **Resource group**. Keep the other tab values as is. Finally, click on **Review+Create**.
5. Review the settings you provided, and then select **Create**. It takes a few minutes to create the account. Wait for the portal page to display "Your deployment is complete" before moving on.
6. Select **Go to resource** to go to the Azure Databricks page.


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


### Link event hub to Databricks

1. Login to https://portal.azure.com.
2. Navigate to your Azure Databricks. Open the Diagnostic settings pane under the Monitoring section.
3. After the page opens, you will need to create a new diagnostic setting.
4. In the Diagnostic settings pane, fill in the form with your preferred categories.
5. Select your categories details, and then send your logs to your preferred destination, in this case, we check **Stream to an event hub**, and put prefered event hub information in.
6. Launch your Databricks Workspace and go to profile at top right corner. 
7. click ```Settings```, go to ```Advanced```, search for ```Verbose Audit Logs``` and turn it on.



## 2. Connecting to the Azure Databricks
### Insert/Update data through Data Explorer
1. Login to https://portal.azure.com.
2. Navigate to your Azure Databricks. Launch Workspace.
3. Under **SQL Editor**,  you can run sql command by creating new quert scripts.  

## 3. Limitations
1. The following important fields couldn't be mapped with Databricks audit logs:
    The following fields are not found in original audit log from Azure Databricks: Database name, ProtocolVersion, AppUserName, Client mac, Common Protocol, Os User, ClientOs, ServerOs.
2. The log with sql execution will not have client ip, but it will come with another log with action name of "commandFinish". 
3. The eventhub takes 10~30 minutes to receive raw logs from Databricks, the same delay time for Guardium is expected.
4. If queries are submitted as part of a notebook cell, job, or script, Databricks may log the entire execution context (e.g., the notebook run or job task) rather than each individual SQL query. In this case, Guardium will not be able to form separate records and only parse the first statement.

## 4. Configuring the Azure-Databricks filter in Guardium
The Guardium universal connector is the Guardium entry point for native audit logs. The Guardium universal connector identifies and parses the received events, and converts them to a standard Guardium format. The output of the Guardium universal connector is forwarded to the Guardium sniffer on the collector, for policy and auditing enforcements. Configure Guardium to read the native audit logs by customizing the Azure-Databricks template.

### Before you begin
Azure-Databricks-Guardium Logstash filter plug-in is automatically available with Guardium Data Protection versions 12.x, 11.4 with appliance bundle 11.0p490 or later or Guardium Data Protection version 11.5 with appliance bundle 11.0p540 or later releases.

* Configure the policies you require. See [policies](https://github.com/IBM/universal-connectors/#policies) for more information.
* You must have permission for the S-Tap Management role. The admin user includes this role by default.
* Download the  plug-in. (Do not unzip the offline-package file throughout the procedure).
* Download the plug-in filter configuration file .

### Configuration
1. On the collector, go to ```Setup``` > ```Tools and Views``` > ```Configure Universal Connector```.
2. Enable the universal connector if it is disabled.
3. Click ```Upload File``` and select the offline  [logstash-filter-databricks_guardium_filter.zip](../../filter-plugin/logstash-filter-databricks-guardium/logstash-filter-databricks_guardium_filter.zip)
 plug-in. After it is uploaded, click ```OK```.
4. Click the Plus sign to open the Connector Configuration dialog box.
5. Type a name in the ```Connector name``` field.
6. Update the input section to add the details from the [databricks.conf](../../filter-plugin/logstash-filter-databricks-guardium/AzureDatabricksOverAzureEventHub/databricks.conf)  file's input part, omitting the keyword "input{" at the beginning and its corresponding "}" at the end.
    - Insert the Connection string in storage_connection from the Access Keys present in the Storage account from the Azure portal.
7. Update the filter section to add the details from the [databricks.conf](../../filter-plugin/logstash-filter-databricks-guardium/AzureDatabricksOverAzureEventHub/databricks.conf) file's filter part, omitting the keyword "filter{" at the beginning and its corresponding "}" at the end.
8. The 'type' fields should match in the input and filter configuration sections. This field should be unique for every individual connector added.
9. Click ```Save```. Guardium validates the new connector and displays it in the Configure Universal Connector page.
10. After the offline plug-in is installed and the configuration is uploaded and saved in the Guardium machine, restart the Universal Connector using the ```Disable/Enable``` button.
