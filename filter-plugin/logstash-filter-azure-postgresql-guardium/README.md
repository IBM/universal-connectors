##	Azure PostgreSQL-Guardium Logstash filter plug-in

This is a [Logstash](https://github.com/elastic/logstash) filter plug-in for the univer***REMOVED***l connector that is featured in IBM Security Guardium. It parses events and mes***REMOVED***ges from the azure postgreSQL audit log into a Guardium record instance (which is a standard structure made out of several parts). The information is then sent over to Guardium. Guardium records include the accessor (the person who tried to access the data), the session, data, and exceptions. If there are no errors, the data contains details about the query "construct". The construct details the main action (verb) and collections (objects) involved.

Currently, this plug-in will work only on IBM Security Guardium Data Protection, not Guardium Insights.

The plug-in is free and open-source (Apache 2.0). It can be used as a starting point to develop additional filter plug-ins for Guardium univer***REMOVED***l connector.

##	Limitations
	• The azure postgreSQL plug-in does not support IPV6.
	• For sql errors and login failed,sql string is not available.
	• For primary or foreign key constraints violation, entry would be added to sql error report as well as full sql report.
	• Source program will be seen as blank in report for some clients(here for psql and pgadmin we get value but for visual studio it is blank).


##	Configuring the Azure PostgreSQL service

There are following ways to get Azure PostgreSQL audit data:

	1. Azure Event Hub
	2. Azure Storage
	3. Log Analytics Workspace
	4. Azure Partner Solution

In this plugin we have used Azure Event Hub.


##	Procedure:
	1. Go to https://portal.azure.com/.
	2. Click search bar.
	3. Search for azure database for postgreSQL servers.
	4. Click on 'Create' button.
	5. Select server type.(here we use single server.)
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
	
	
## Enabling Auditing

	1.	From settings,select server parameter.
	2.	Search for shared_preload_libraries in server parameter.
	3.	Select shared_preload_libraries as PGAUDIT and ***REMOVED***ve.
	4.	Go to overview and restart the server to apply the changes.
	5.	After installation of pgAudit, you can configure its parameters to start logging.
			•	Under Settings, select server parameters and set server parameters as follows:
					•	log_checkpoints = off
					•	log_connections = off
					•	log_disconnections = off
					•	log_duration = off
					•	log_error_verbosity = VERBOSE
					•	log_line_prefix = specify as per requirement but should include timestamp,client ip,client port,database username,database name,process id application name,sql state. Refer from this link https://www.postgresql.org/docs/current/runtime-config-logging.html#GUC-LOG-LINE-PREFIX 
							(eg:- %t:%r:%u@%d:[%p]:%a:%e)
					•	pgaudit.log = DDL,FUNCTION,READ,WRITE,ROLE
					•	pgaudit.log_catalog = off
					•	pgaudit.log_client = off
					•	pgaudit.log_parameter = off
	6.	Click ***REMOVED***ve.
			
		
## Azure Event Hub Connection:

		1.	Search event hub in search bar.
		2.	Select create event hubs namespace button.
		
		3.	To create namespace:
				1.	Select the subscription in which you want to create the namespace.
				2.	Select the resource group you created in the previous step.
				3.	Enter a unique name for the namespace. 
				4.	Select a location for the namespace.
				5.	Choose appropriate pricing tier.(here selected basic)
				6.	Leave the throughput units (or processing units for standard and premium tier) settings as it is.
				7.	Select Review + Create at the botton of the page.
				8.	Review the settings and select create.
				9.	After successful creation recently created namespace will appear in resource group.
				
		4.	To create event hub :
				1.	Go to the Event Hubs Namespace page.
				2.	Click on '+ Event Hub' to add event hub.
				3.	Enter unique name for event hub, then select create.
				
		5.	Connection string for a eventhub:
				1.	In the list of event hubs, select your event hub.
				2.	On the Event Hubs instance page, from settings select Shared access policies on the left menu.
				3.	In shared access policies click on add button from top.
				4.	Give the name to policy and provide permission 'manage' and create the policy. 
				5.	Select primary key string from policy (it would be required in input plugin).
				
				
		6.	Stream logs to an event hub:
				1.	Go to server in your azure portal.
				2.	From Monitoring, select Diagnostics settings option and do either of the following:
						a.	To change existing settings, select Edit setting.
						b.	To add new settings, select Add diagnostics setting.
				3.	For adding new settings do following:
						a.	Give name to setting.	
						b.	Select PostgreSQLLogs from categories.
						c.	In Destination details choose stream to an event hub.
						d.	In stream to event hub, select namespace name and event hub name as created above.Keep event hub policy name as it is.
						e.	Select Save to ***REMOVED***ve the setting.				 
		7.	After about 15 minutes, verify that events are displayed in your event hub.
		
		8.	Configurations needed to monitor traffic from single Event Hub, when UCs are configred on two 	separate Collectors
			Procedure :-
			We need to do the following steps:
				1. We can create namespace in azure event hub as per given above but select standard pricing tier instead of basic in pricing tier configuration.
				2. After creation of namespace we can create eventhub and connection string as per mention above.
				3. After successful creation of eventhub we can add a consumer group to event hub as follows:
					1. In the list of event hubs, select your event hub.
					2. On the Event Hubs instance page, from entities select consumer group.
					3. In consumer group click on add button from top.
					4. Give the name to consumer group and create the consumer group.
				4. Stream logs to event hub as mentioned as above.
				5. For gmachine , We have to use these consumer group name in input section of configuration file as follows:
					1. On one machine in input section of configuration file in consumer group field give           name as $Default.
					2. On other machine in input section of configuration file in consumer group field give name of other consumer group.
					3. Keep all other configurations as it is.
		

## Connecting to Azure postgreSQL Database:

		1. Start the psql and provide connection details.
		2. Enter the 'server name' (from overview window of server in azure portal)
		3. Enter 'database name' as 'postgres'.
		4. Enter 'port' as 5432.
        5. Provide the 'username' (you will get it from azure portal,click on overview and copy admin username) and ‘password’ that we had set while creating the database.
		6. Click enter.
		7. Enter command to give access to pgaudit.
        		GRANT pg_read_all_settings TO <admin-username>;(use admin-username which is given at the time of creation of database)
				    eg:-GRANT pg_read_all_settings TO postgres; 


## Configuring the Azure PostgreSQL filters in Guardium

The Guardium univer***REMOVED***l connector is the Guardium entry point for native audit logs. The Guardium univer***REMOVED***l connector identifies and parses the received events, and converts them to a standard Guardium format. The output of the Guardium univer***REMOVED***l connector is forwarded to the Guardium sniffer on the collector, for policy and auditing enforcements. Configure Guardium to read the native audit logs by customizing the azure postgreSQL template.


## Before you begin
	• You must have permission for the S-Tap Management role. The admin user includes this role by default.
	• Download the azure-postgresql-offline-plugins-7.5.2.zip plug-in.


## Procedure : 

  1.	On the collector, go to Setup > Tools and Views > Configure Univer***REMOVED***l Connector.
  2.	First Enable the Univer***REMOVED***l Guardium connector, if it is Di***REMOVED***bled already.
  3.	Click Upload File and select the offline azure-postgresql-offline-plugins-7.5.2.zip plugin. After it is uploaded,click OK.
  4.	Click the Plus sign to open the Connector Configuration dialog box.
  5.	Type a name in the Connector name field.
  6.	Update the input section to add the details from azurepostgresql.conf file's input part, omitting the keyword "input{" at the beginning and its corresponding "}" at the end.
  7.	"type" field should match in input and filter configuration section. This field should be unique for every individual connector added.
  8.	Update the filter section to add the details from azurepostgresql.conf file's filter part, omitting the keyword "filter{" at the beginning and its corresponding "}" at the end.
  9.	Click **Save**. Guardium validates the new connector, and enables the univer***REMOVED***l connector if it was di***REMOVED***bled. After it is validated, the connector appears in the Configure Univer***REMOVED***l Connector page.
