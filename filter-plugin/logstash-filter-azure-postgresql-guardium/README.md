# Azure PostgreSQL-Guardium Logstash filter plug-in

This is a [Logstash](https://github.com/elastic/logstash) filter plug-in for the universal connector that is featured in IBM Security Guardium. It parses events and messages from the azure postgreSQL audit log into a [Guardium record](https://github.com/IBM/universal-connectors/blob/main/common/src/main/java/com/ibm/guardium/universalconnector/commons/structures/Record.java) instance (which is a standard structure made out of several parts). The information is then sent over to Guardium. Guardium records include the accessor (the person who tried to access the data), the session, data, and exceptions. If there are no errors, the data contains details about the query "construct". The construct details the main action (verb) and collections (objects) involved.

Currently, this plug-in will work only on IBM Security Guardium Data Protection, not Guardium Insights.

The plug-in is free and open-source (Apache 2.0). It can be used as a starting point to develop additional filter plug-ins for Guardium universal connector.


## 1.	Configuring the Azure PostgreSQL service

There are following ways to get Azure PostgreSQL audit data:
1. Azure Event Hub
2. Azure Storage
3. Log Analytics Workspace
4. Azure Partner Solution

In this plugin we have used Azure Event Hub.


###	Procedure:
1. Go to https://portal.azure.com/.
2. Click search bar.
3. Search for azure database for postgreSQL servers.
4. Click on 'Create' button.
5. Select resource type.(here we use single server.)
6. Select Single server and click on create button and fill details in basic option.
7. In Basic option:
   1. Select your subscription name.
   2. Select existing resource group or create new one.
   3. For new resource group click on create button  and enter name for resource group.
   4. Provide 'Server Name'. Keep 'Data source' as none ,select appropriate location and set 'Version=11'.
   5. Keep compute + storage as 'General Purpose, 4 vCore(s), 100 GB'.
   6. Provide server admin username and password.
8.	Click 'Review + Create' Button.
9.	Verify the configuration and then click on 'Create' Button.
10.	After successful creation of resource group, select resource button.
11.	From settings,select connection security.
12.	In firewall rules,select allow access to azure services to 'Yes'.
13. Click on add current client IP address, and then click on Save.
	
	
## 2. Enabling Auditing

1.	From settings,select server parameter.
2.	Search for shared_preload_libraries in server parameter.
3.	Select shared_preload_libraries as PGAUDIT and save.
4.	Go to overview and restart the server to apply the changes.
5.	After installation of pgAudit, you can configure its parameters to start logging.
      1. Under Settings, select server parameters and set server parameters as follows:
         * log_checkpoints = off
         * log_connections = off
         * log_disconnections = off
         * log_duration = off
         * log_error_verbosity = VERBOSE 
         * log_line_prefix = specify as per requirement but should include 
         timestamp, client ip, client port, database username, database name, process id, application name,sql state. 
         Refer from this link https://www.postgresql.org/docs/current/runtime-config-logging.html#GUC-LOG-LINE-PREFIX
         (eg:- %t:%r:%u@%d:[%p]:%a:%e)
         * pgaudit.log = DDL,FUNCTION,READ,WRITE,ROLE 
         * pgaudit.log_catalog = off 
         * pgaudit.log_client = off 
         * pgaudit.log_parameter = off 
6. Click save.
			

## 3. Viewing the Audit logs
		
### Azure Event Hub Connection:

1. Search Storage Accounts in the search bar of the Azure portal.
   (If you need your plug-in to read events from multiple Event Hubs, you need a storage account.)
    1. Click **Create**.
    2. Enter the required details in the presented form.
    3. Once the details are reviewed, create a storage account.
    4. Go into the created storage account.
    5. In the menu, go to **Security + networking** > **Access keys**.
    6. Choose any key (though key1 is preferable), and click **Show** in **Connection String**.
    7. Copy the presented connection string and save it somewhere.
2. Search "event hub" in search bar.
3. Select **Create** to **create event hubs namespace**.
4. To create a namespace:
    1. Select the subscription in which you want to create the namespace.
    2. Select the resource group you created in the previous step.
    3. Enter a unique name for the namespace.
    4. Select a location for the namespace.
    5. Choose the appropriate pricing tier.(In this case, we selected basic.)
    6. Leave the throughput units settings (or processing units settings for standard and premium tiers) as it is.
    7. Select **Review + Create**.
    8. Review the settings and select **create**.
    9. After successful creation, the recently created namespace appears in the resource group.

5. To create an event hub:
    1. Go to the Event Hubs Namespace page.
    2. Click **+ Event Hub** to add event hub.
    3. Enter a unique name for the event hub, then select **create**.

6. Connection string for a eventhub:
    1. In the list of event hubs, select your event hub.
    2. On the Event Hubs instance page, go to **settings**  >  **shared access policies** > **Add**.
    3. In shared access policies click on add button from top.
    4. Provide the name to policy and choose **manage** from the permissions and create the policy.
    5. Take the value of the primary key - connection string from the policy (this is required for an input plug-in).
    6. The value of the Primary Key - Connection String will be used in the parameter **event_hub_connections** in the input of the plugin.

7. Stream logs to an event hub:
    1. Go to the server in your Azure portal.
    2. From **Monitoring**, select **Diagnostics settings** and do either of the following:
        1. Change existing settings by selecting **Edit setting**.
        2. Add new settings by selecting **Add diagnostics setting**.
        3. To add new settings, do following:
            1. Give the setting a name.
            2. Select **PostgreSQLLogs** from **categories**.
            3. In **Destination details**, choose **stream to an event hub**.
            4. In **stream to event hub**, select the namespace name and event hub name that were above. Keep the event 
               hub policy name as is.
            5. Select **Save**.
8. After about 15 minutes, verify that the events are displayed in your event hub.

9. You also need the correct configurations to monitor traffic from a single event hub, when universal connectors are 
configured on two separate collectors. <br />
Procedure: 
   1. Create a namespace in Azure event hub according to the procedure described previously, and then 
select **standard**  instead of **basic** in the pricing tier configuration.
   2. Then create an event hub and connection string according to the procedure described previously.
   3. Add a consumer group to the event hub as follows:
      1. In the list of event hubs, select your event hub.
      2. On the Event Hubs instance page, go to **entities** >  **consumer group**.
      3. In **consumer group**, click **Add**.
      4. Assign the consumer group a name and create the consumer group.
   4. Stream logs to the event hub according to the procedure described previously.
   5. For the Guardium machine, use this consumer group name in the input section of the configuration file as follows:
      1. For one machine, go to the consumer group field in the input section of the configuration 
file and give the name `$Default`.
      2. For the other machine, put the name of the other consumer group in the same location.
      3. Keep all other configurations as is.

#### Note
There are some recommendations while configuring the plug-in to read from multiple event hubs. These recommendations 
apply to both scenarios; when there is more than one connection string in `event_hub_connections` as well as when 
there are multiple universal connector configurations in one Guardium machine.

* When there is more than one connection string provided in the `event_hub_connections` parameter, 
define both `storage_connection` and `consumer_group` parameters. They help to differentiate the files where timestamp 
offsets are written. The `azure_event_hubs` input does not store the offset locally. 
More details are here in the Logstash [documentation](https://www.elastic.co/guide/en/logstash/current/plugins-inputs-azure_event_hubs.html#plugins-inputs-azure_event_hubs-best-practices).

Not following this recommendation may result into data loss.

## 4. Connecting to Azure postgreSQL Database:

1. Start the psql and provide connection details.
2. Enter the **server name** (from overview window of server in azure portal)
3. Enter the database name as 'postgres'.
4. Enter 'port' as 5432.
5. Provide the 'username' (you will get it from azure portal,click on overview and copy admin username) and ‘password’ that we had set while creating the database.
6. Click enter.
7. Enter command to give access to pgaudit.
    ```text
    GRANT pg_read_all_settings TO <admin-username>;
    (use admin-username which is given at the time of creation of database)
    eg:-GRANT pg_read_all_settings TO postgres; 
    ```

#### Limitations
* The azure postgreSQL plug-in does not support IPV6.
* For sql errors and login failed,sql string is not available.
* For primary or foreign key constraints violation, entry would be added to sql error report as well as full sql report.
* Source program will be seen as blank in report for some clients(here for psql and pgadmin we get value but for visual studio it is blank).

## 5. Configuring the Azure PostgreSQL filters in Guardium

The Guardium universal connector is the Guardium entry point for native audit logs. The Guardium universal connector identifies and parses the received events, and converts them to a standard Guardium format. The output of the Guardium universal connector is forwarded to the Guardium sniffer on the collector, for policy and auditing enforcements. Configure Guardium to read the native audit logs by customizing the azure postgreSQL template.


#### Before you begin
* You must have LFD policy enabled on the collector. The detailed steps can be found in step 4 on [this page](https://www.ibm.com/docs/en/guardium/11.4?topic=dpi-installing-testing-filter-input-plug-in-staging-guardium-system).
* You must have permission for the S-Tap Management role. The admin user includes this role by default. 
* Download the [azure-postgresql-offline-plugins-7.5.2.zip](https://github.ibm.com/Activity-Insights/universal-connectors/blob/master/filter-plugin/logstash-filter-azure-postgresql-guardium/AzurePostgresqlOverAzureEventHub/azurepostgresql/azure-postgresql-offline-plugins-7.5.2.zip) plug-in.


#### Procedure : 

1.	On the collector, go to Setup > Tools and Views > Configure Universal Connector.
2.	First enable the Universal Guardium connector, if it is disabled already.
3.	Click Upload File and select the offline [azure-postgresql-offline-plugins-7.5.2.zip](https://github.ibm.com/Activity-Insights/universal-connectors/blob/master/filter-plugin/logstash-filter-azure-postgresql-guardium/AzurePostgresqlOverAzureEventHub/azurepostgresql/azure-postgresql-offline-plugins-7.5.2.zip) plugin. After it is uploaded,click OK.
4.	Click the Plus sign to open the Connector Configuration dialog box.
5.	Type a name in the Connector name field.
6.	Update the input section to add the details from [azurepostgresql.conf](https://github.ibm.com/Activity-Insights/universal-connectors/blob/master/filter-plugin/logstash-filter-azure-postgresql-guardium/AzurePostgresqlOverAzureEventHub/azurepostgresql/azurepostgresql.conf) file's input part, omitting the keyword "input{" at the beginning and its corresponding "}" at the end.
7.	The "type" fields should match in the input and the filter configuration sections. This field should be unique for every individual connector added.
8.	Update the filter section to add the details from [azurepostgresql.conf](https://github.ibm.com/Activity-Insights/universal-connectors/blob/master/filter-plugin/logstash-filter-azure-postgresql-guardium/AzurePostgresqlOverAzureEventHub/azurepostgresql/azurepostgresql.conf) file's filter part, omitting the keyword "filter{" at the beginning and its corresponding "}" at the end.
9.	Click **Save**. Guardium validates the new connector, and enables the universal connector if it was disabled. After it is validated, the connector appears in the Configure Universal Connector page.
