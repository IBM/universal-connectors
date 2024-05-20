# Logstash Filter PubSub MySQL Plugin

## Meet PubSub MySQL

This is a Logstash filter plug-in for the universal connector that is featured in IBM Security Guardium. It parses a GCP (Google Cloud Platform) audit event into a Guardium record instance, which standardizes the event into several parts before it is sent over to Guardium.
Generated with Logstash v7.15.0.

**Notes**:
* This plug-in contains a runtime dependency of Logstash Google PubSub input plug-in (version ~> 1.2.1, i.e. at least 1.2.1). To install, refer to the [Prerequisites](https://cloud.google.com/sql/docs/mysql/create-instance#before_you_begin).

* The plug-in is free and open-source (Apache 2.0). It can be used as a starting point to develop additional filter plug-ins for Guardium universal connector.


## Supported Client Applications:
* DBeaver
* DataGrip
* Google Cloud Shell (`gcloud`)
* MySQL Client
* MySQL Shell
* [Cloud Run](https://cloud.google.com/sql/docs/mysql/connect-run) for `JDBC` and `.NET`

**Notes**: 
* All of the above comply with Cloud SQL Proxy authentication, except for `gcloud`.
* `.NET` wasn't tested, but is assumed to have the same event structure as `JDBC`.
* This version is compliant with GDP v11.4 and above. Please refer to the
[input plugin's repository](../../input-plugin/logstash-input-google-pubsub) for more information.

## Creating SQL instance and Configure Logging

### Creating SQL Instance

1. [Prerequisites](https://cloud.google.com/sql/docs/mysql/create-instance#before_you_begin)
2. [Creating a MySQL instance](https://cloud.google.com/sql/docs/mysql/create-instance#create-2nd-gen)
3. *SQL Instances > Select & Edit instance >* add the following flags:
    - **cloudsql_mysql_audit**: ON
    - **audit_log**: ON
4. Open _Cloud Shell_ terminal and connect to your project using:
`gcloud config set project [PROJECT_ID]`, and click _Authorize_ in the pop-up window
5. Go to _SQL Instance_ > _OPEN CLOUD SHELL_ or use the following command: ```gcloud sql connect <INSTANCE_NAME> --user=<USER_NAME> --quiet``` and enter the database user's password
6. Enter the following in order to enable audit logging:
   ```
   CALL mysql.cloudsql_create_audit_rule('*','*','*','*','B',1,@outval,@outmsg);
   ```

In case you wish to use **Cloud SQL proxy**, use the following steps:
1. [Prerequisites](https://cloud.google.com/sql/docs/mysql/connect-admin-proxy#before_you_begin)
2. Download the Cloud SQL Auth proxy compatible with your OS according to the steps done [here](https://cloud.google.com/sql/docs/mysql/connect-admin-proxy#install)
3. Copy the SQL instance name from the instance's overview page, or run this in the GCP terminal:
    ```      
    gcloud sql instances describe INSTANCE_NAME
    ```   
4. Generate a json key file of the service account created in (2) by going to _Service Accounts > your Cloud SQL service
   account page > Keys > Add key > Create new key > JSON > Create_ this will download the key file to your downloads folder
5. [Start the cloud sql auth proxy](https://cloud.google.com/sql/docs/mysql/connect-admin-proxy#start-proxy) by running:
   1. In PowerShell on Windows:
   ```
   .\cloud_sql_proxy.exe -instances=INSTANCE_CONNECTION_NAME=tcp:3306 -credential_file=/path/to/<CLOUD_SQL_KEYFILE_ID>.json
   ```
   2. For Mac and Linux OSs:
   ```
   ./cloud_sql_proxy -instances=INSTANCE_CONNECTION_NAM=tcp:3306 \
                  -credential_file=PATH_TO_KEY_FILE &
   ```
**Note**: Running this will automatically bind your computer's IP to the MySQL port. In case the bind failed due to addressalready in use - use `tcp:0.0.0.0:3306` instead.


6. Setup your SQL client application with **DBeaver** as an example:
    1. Click _Database > New Database Connection > MySQL > Next_
    2. You’ll then get to a connection form where you specify host, port, database, user, and password. Leave the 
       defaults and add the user and password from GCP. Also click Show all databases
    3. Then you can click the Test Connection button, and you should see a success pop-up message
    4. Click OK twice, and you are up and running

### Creating a topic in Pub/Sub

1. Go to the Pub/Sub topics page in the Cloud Console and click Create a topic.
2. In the Topic ID field, provide a unique topic name. For example, MyTopic.
3.  Click Create Topic.

### Creating subscription in Pub/Sub
1. Display the menu for the topic created in the previous step and click New subscription.
2. Enter the subscription name. For example, MySub.
3. Leave the delivery type as Pull and click Create.

### Creating log sink in Pub/Sub
1. In the Cloud Console, go to the Logging > Log Router page.
2. Click Create sink.
3. In the Sink details panel, enter the following details:
   * Sink name: Provide an identifier for the sink. 
     Note that once you create the sink you cannot rename it. However, you can delete a sink and create a new one.
4. Sink description (optional): Describe the purpose or use case for the sink.
5. In the Sink destination panel, select the Cloud Pub/Sub topic as sink service and select the topic created in previous steps.
6. Choose logs to include in the sink in the Build inclusion filter panel.
   
**Notes**:
* You can filter the logs by log name, resource, and severit multi-region.
* In cases of multiple regions, you need to do the same set of configurations per each region. Based on the region, different configuration files will be used for the input plug-in.

### Setting destination (TOPIC & SUBSCRIPTION) permissions

To set permissions for the log sink to route to its destination, do the following:

1. Obtain the sink's writer identity—an email address—from the new sink.
    * Go to the Log Router page, and select menu  > View sink details.
    * The writer identity appears in the Sink details panel.
2. If you have owner access to the destination:
    * Add the sink's writer identity to topic >>>>
        * Navigate to the Topic created in the earlier steps
        * Click on SHOW INFO panel
        * Click on ADD PRINCIPAL
        * Paste writer identity in the New Principals
        * Give it the Pub/Sub Publisher role and subscriber role
    * Add the sink's writer identity to subscription >>>>
        * Navigate to the Subscription
        * Click on SHOW INFO panel
        * Click on ADD PRINCIPAL
        * Paste writer identity in the New Principals
        * Give it the subscriber role
     
### Creating service account credentials
1. Go to the Service accounts section of the IAM & Admin console.
2. Select project and click Create Service Account.
3. Enter a Service account name, such as MySQL-pubsub.
4. Click Create.
5. The owner role is required for the service account. Select the owner role from the drop-down menu
6. Click Continue. You do not need to grant users access to this service account.
7. Click Create Key. The key is used by the Logstash input plug-in configuration file.
8. Select JSON and click Create.

### Configure Logging
#### Inclusion Filter
Edit the Sink via *Logs Router > Sink Inclusion Filter*:

For **Cloud SQL proxy**:

**DBeaver:**
 ```
resource.type="cloudsql_database" resource.labels.database_id="<PROJECT_ID>:<SQL_Instance_name>"
((logName="projects/<PROJECT_ID>/logs/cloudaudit.googleapis.com%2Fdata_access" AND protoPayload.methodName="cloudsql.instances.query" AND protoPayload.request.query:"/*" AND
protoPayload.request.query:"SQLEditor")
OR 
logName="projects/<PROJECT_ID>/logs/cloudsql.googleapis.com%2Fmysql.err")
```
**DataGrip:**
```
resource.type="cloudsql_database" resource.labels.database_id="<PROJECT_ID>:<SQL_Instance_name>"
((logName="projects/<PROJECT_ID>/logs/cloudaudit.googleapis.com%2Fdata_access" AND protoPayload.methodName="cloudsql.instances.query" AND protoPayload.request.query:"/*" AND
 protoPayload.request.query:"DataGrip")
OR 
logName="projects/<PROJECT_ID>/logs/cloudsql.googleapis.com%2Fmysql.err")
```
**All client applications:**
```
resource.type="cloudsql_database" resource.labels.database_id="<PROJECT_ID>:<SQL_Instance_name>"
((logName="projects/<PROJECT_ID>/logs/cloudaudit.googleapis.com%2Fdata_access" AND protoPayload.methodName="cloudsql.instances.query")
OR 
logName="projects/<PROJECT_ID>/logs/cloudsql.googleapis.com%2Fmysql.err")
```

#### Exclusion Filter
   - Edit the Sink via *Logs Router > Build an exclusion filter*
     - Set the exclusion filter name to `internal_logs`
     - Set the filter
       ```
       protoPayload.request.query:"mysql.heartbeat" OR protoPayload.request.query="Select 1" OR protoPayload.request.ip="127.0.0.1" OR protoPayload.request.cmd:"connect" OR protoPayload.request.query:"information_schema" OR protoPayload.request.query:"@@" OR protoPayload.request.query:"performance_schema"  OR  protoPayload.request.query:"mysql." OR protoPayload.request.query:"select @@" OR protoPayload.request.query:"`mysql`."
       ```
     - To exclude DBeaver **and** DataGrip, concatenate the following to the previous exclusion filter:
       ```
       OR protoPayload.request.query:"DataGrip" OR protoPayload.request.query:"DBeaver"     
       ```
**Note**: The purpose of this exclusion filter is to filter out system and admin logs that are the result of internal packets in Google Cloud.
### Supported audit messages types
* data_access - `INFO`, `DEFAULT`
* mysql.err

**Notes**:
* `serverHostName` field is populated with the name of the MySQL instance connection
* `serviceName` field is populated with Cloud SQL Service. See [docs](https://cloud.google.com/sql/docs/mysql)
* `dbUser` and `exception.sqlString` fields are populated with "Undisclosed" for `mysql.err` events, as this information is not embedded in the messages pulled from Google Cloud.
* `appUserName` is populated with your IAM Cloud SQL service account ID


## Installation

Logstash Filter PubSub MySQL Plugin is automatically available with Guardium Data Protection versions 12.x, 11.4 with appliance bundle 11.0p490 or later or Guardium Data Protection version 11.5 with appliance bundle 11.0p540 or later releases.

**Note**: For Guardium Data Protection version 11.4 without appliance bundle 11.0p490 or prior or Guardium Data Protection version 11.5 without appliance bundle 11.0p540 or prior, download the [logstash-filter-pubsub-mysql-guardium-7.16.3.zip](https://github.com/IBM/universal-connectors/raw/main/filter-plugin/logstash-filter-pubsub-mysql-guardium/PubSubMySQLPackage/logstash-filter-pubsub-mysql-guardium-7.16.3.zip) plug-in. (Do not unzip the offline-package file throughout the procedure).

### Note
To install on your local machine that is running Logstash, execute:
`bin/logstash-plugin install file:///path/to/logstash-offline-plugin-input-google_pubsub.zip
`

### Sample Configuration

Below is a copy of the filter scope included `googlepubsub.conf` [file](PubSubMySQLPackage/googlepubsub.conf) that shows a basic
configuration for this plugin.
#### Filter part:
```
pubsub-mysql-guardium{}
```
For enabling Cloud SQL Proxy, use:
```
pubsub-mysql-guardium{ cloudsqlproxy_enabled => true }
```
**Note:** `cloudsqlproxy_enabled` will default to `false` if not explicitly used in filter scope.

## Limitations
1. Some internal system queries for client apps other than DBeaver might be ingested by the plug-in. You could easily filter them out by using the 
   GCP logs routing sink's exclusion filter, as seen in the Exclusion filter section above (simply append to it an `OR` followed by your expression). For more information, check 
   [Exclusion filters](https://cloud.google.com/logging/docs/routing/overview#exclusions) and 
   [Logging query language](https://cloud.google.com/logging/docs/view/logging-query-language).
2. mysql-slow.log logs aren't supported in this version
3. `SQLEditor` is the event type identifying user queries on DBeaver. For DataGrip, there's no type embedded in the event's comment, meaning some internal system queries will be ingested by this plug-in.
4. `DESCRIBE table_name` and `SHOW TABLES` queries result in an internal `SELECT DATABASE()` query for some of the client applications
5. For some of the client applications (detected for DBeaver and MySQL Client) the events are expected to be suffixed by a `LIMIT`
   1. Example: `SELECT * FROM my_table WHERE name = 'Smith AND age < 30' LIMIT 0, 200`
6. `dbName` will be populated with `Undisclosed` for some of the queries as their respective GCP events do not embed this information
7. Due to a known issue, the objects and verbs column for `SHOW TRIGGERS` query is blank
8. For applications other than DataGrip, DBeaver and Google Cloud Shell (`gcloud`), `Source Program` column in the reports will be populated with `MySQL Client Application` as this information isn't embedded in the applications' GCP events
**Note:** For security reasons, it is highly recommended that you configure `cloudsqlproxy_enabled` to true for all client applications other than Google Cloud Shell (`gcloud`). Otherwise, `Source Program` is populated with _Google Cloud Shell (gcloud)_.

## Installation
### Before you begin 
To install this plug-in, you need to download the relevant plugin based on the version of the Guardium.
1. For the Guardium 11.x, download [logstash-filter-pubsub-mysql-guardium-7.16.3.zip](https://github.com/IBM/universal-connectors/raw/main/filter-plugin/logstash-filter-pubsub-mysql-guardium/PubSubMySQLPackage/logstash-filter-pubsub-mysql-guardium-7.16.3.zip)
2. For the Guardium 12.x, download [logstash-filter-pubsub-mysql-guardium-8.3.3.zip](https://github.com/IBM/universal-connectors/raw/main/filter-plugin/logstash-filter-pubsub-mysql-guardium/PubSubMySQLPackage/logstash-filter-pubsub-mysql-guardium-8.3.3.zip)


### Procedure
1. On the collector, go to Setup > Tools and Views > Configure Universal Connector.
2. Ensure that the universal connector is enabled. 
3. Click Upload File and select the relevant plugin based on the version of the Guardium and after it is uploads, click OK.
    * For Guardium 11.x, download the [logstash-filter-pubsub-mysql-guardium-7.16.3.zip](https://github.com/IBM/universal-connectors/raw/main/filter-plugin/logstash-filter-pubsub-mysql-guardium/PubSubMySQLPackage/logstash-filter-pubsub-mysql-guardium-7.16.3.zip)
    * For Guardium 12.x, download the [logstash-filter-pubsub-mysql-guardium-8.3.3.zip](https://github.com/IBM/universal-connectors/raw/main/filter-plugin/logstash-filter-pubsub-mysql-guardium/PubSubMySQLPackage/logstash-filter-pubsub-mysql-guardium-8.3.3.zip)
4. Click Upload File and select the key.json file and click OK.
5. Click the plus sign to open the Connector Configuration dialog box.
6. In the Connector name field, enter a name. 
7. Update the input section to add the details from the `googlepubsub.conf` [file](PubSubMySQLPackage/googlepubsub.conf) input part, omitting the keyword "input{" at the beginning and its corresponding "}" at the end.
8. Update the filter section to add the details from the `googlepubsub.conf` [file](PubSubMySQLPackage/googlepubsub.conf) filter part, omitting the keyword "filter{" at the beginning and its corresponding "}" at the end.
9. Ensure that the type fields match in the input and filter configuration. The field must be unique for every individual connector. 
10. Click Save.
    Guardium validates the new connector and displays it in the Configure Universal Connector page.
12. Once the offline plug-in is installed and the configuration is uploaded and saved in the Guardium machine, restart the Universal Connector using the Disable/Enable button.

**Note**:
To install on your local machine that is running Logstash, execute: `bin/logstash-plugin install file:///path/to/logstash-offline-plugin-input-google_pubsub.zip`

### Sample Configuration

Below is a copy of the filter scope included `googlepubsub.conf` [file](PubSubMySQLPackage/googlepubsub.conf) that shows a basic
configuration for this plugin.
**Filter part**
```
pubsub-mysql-guardium{}
```
For enabling Cloud SQL Proxy, use:
```
pubsub-mysql-guardium{ cloudsqlproxy_enabled => true }
```
**Note**:
`cloudsqlproxy_enabled` will default to `false` if not explicitly used in filter scope.

## Troubleshooting
Refer to the input plugin's [Troubleshooting](../../input-plugin/logstash-input-google-pubsub#troubleshooting) section.

## Contributing
You can enhance this filter and open a pull request with suggested changes - or you can use the project to create a different filter plug-in for Guardium that supports other data sources.
