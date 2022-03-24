# Logstash Filter PubSub MySQL Plugin

This is a Logstash filter plug-in for the univer***REMOVED***l connector that is featured in IBM Security Guardium. It parses a GCP (Google Cloud Platform) audit event into a Guardium record instance, which standardizes the event into several parts before it is sent over to Guardium.
Generated with Logstash v7.15.0.

### Note
This plug-in contains a runtime dependency of Logstash Google PubSub input plug-in (version ~> 1.2.1, i.e. at least 1.2.1). To install, refer to the [Prerequisites](#Prerequisites) section of the Documentation below.

The plug-in is free and open-source (Apache 2.0). It can be used as a starting point to develop additional filter plug-ins for Guardium univer***REMOVED***l connector.

## Documentation

### Prerequisites
Download the [Logstash Offline package](PubSubMySQLPackage/logstash-offline-plugins-filter-pubsub-mysql-guardium.zip) that includes both the Logstash Google PubSub input plugin and the MySQL PubSub filter plugin, and upload it to the gmachine.
#### Note
This version is for GDP v11.4, i.e., stable version. Please refer to the
[input plugin's repository](../../input-plugin/logstash-input-google-pubsub) for more information.

### Create the SQL instance and Configure Logging

#### Create the SQL Instance

1. [Prerequisites](https://cloud.google.com/sql/docs/mysql/create-instance#before_you_begin)
2. [Creating a MySQL instance](https://cloud.google.com/sql/docs/mysql/create-instance#create-2nd-gen)
3. *SQL Instances > Select & Edit instance >* add the following flags:
    - **general_log**: on
    - **log_output**: FILE
    - **slow_query_log**: on
    
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
   ###### Note: 
   running this will automatically bind your computer's IP to the MySQL port. In case the bind failed due to address
   already in use - use `tcp:0.0.0.0:3306` instead.


6. Setup your SQL client application with **DBeaver**:
    1. Click _Database > New Database Connection > MySQL > Next_
    2. You’ll then get to a connection form where you specify host, port, database, user, and password. Leave the 
       defaults and add the user and password from GCP. Also click Show all databases
    3. Then you can click the Test Connection button, and you should see a success pop-up mes***REMOVED***ge
    4. Click OK twice, and you are up and running

#### Configure Logging
##### Inclusion Filter
Edit the Sink via *Logs Router > Sink Inclusion Filter*:

For **Cloud SQL proxy** + DBeaver setup, use:
 ```
resource.type="cloudsql_database" resource.labels.database_id="<project_name>:<SQL_Instance_name>" (logName="projects/<project_name>/logs/cloudsql.googleapis.com%2Fmysql-general.log" AND textPayload:"Query" AND textPayload:"/*" AND textPayload:"SQLEditor") OR logName=("projects/<project_name>/logs/cloudsql.googleapis.com%2Fmysql.err")
```

For the basic setup (without Cloud SQL proxy), use:
 ```
resource.type="cloudsql_database" resource.labels.database_id="<project_name>:<SQL_Instance_name>" (logName="projects/<project_name>/logs/cloudsql.googleapis.com%2Fmysql-general.log" AND textPayload:"Query") OR logName=("projects/<project_name>/logs/cloudsql.googleapis.com%2Fmysql.err")
```
##### Exclusion Filter
   - Edit the Sink via *Logs Router > Build an exclusion filter*
   - Set the exclusion filter name to `internal_logs`
   - Set the filter
       `textPayload:"[127.0.0.1]"`
###### Note
The purpose of this exclusion filter is to filter out system and admin logs that are the result of internal packets in Google Cloud.
### Supported audit mes***REMOVED***ges types
* mysql-general.log - `INFO`, `DEFAULT`
* mysql.err

### Notes
- `serverHostName` field is populated with the name of the MySQL instance connection
- `serviceName` field is populated with Cloud SQL Service. See [docs](https://cloud.google.com/sql/docs/mysql)
- `dbUser` and `exception.sqlString` fields are populated with "Undisclosed" for `mysql.err` events, as this information is not embedded in the mes***REMOVED***ges pulled from Google Cloud.



## Installation
To install this plug-in, you need to download the [offline pack](https://github.ibm.com/Activity-Insights/univer***REMOVED***l-connectors/blob/master/filter-plugin/logstash-filter-pubsub-mysql-guardium/PubSubMySQLPackage/logstash-offline-plugins-filter-pubsub-mysql-guardium.zip).

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
1. Since GCP truncates multi-line queries, meaning inserting new lines in your user query would generate multiple logs on 
Google Cloud, this plug-in would then only ingest the first line of that query. In order to trim-off line feeds via your SQL Editor on DBeaver and have these type of queries fully digested, please refer to [SQL Formatter](PubSubMySQLPackage/sqlFormatter)
   
2. Some internal system queries from DBeaver are ingested by the plug-in. You could easily filter them out by using the 
   GCP logs routing sink's exclusion filter, as seen in the Exclusion filter section above. For more information, check 
   [Exclusion filters](https://cloud.google.com/logging/docs/routing/overview#exclusions) and 
   [Logging query language](https://cloud.google.com/logging/docs/view/logging-query-language).
3. For Cloud SQL proxy, this plug-in currently supports only DBeaver as a SQL client application
4. mysql-slow.log logs aren't supported in this version
5. For a setup configured with DBeaver and Cloud SQL proxy: note that SQL errors generate a 
   `SHOW WARNINGS` query execution on DBeaver, and the information regarding the error isn't embedded in its event or
   logged as a mysql.err on GCP. It will however be logged on DBeaver in the Output tab with the full error mes***REMOVED***ge and 
   details.
   
   

## Troubleshooting
Refer to the input plugin's [Troubleshooting](../../input-plugin/logstash-input-google-pubsub#troubleshooting) section.

## Contributing

You can enhance this filter and open a pull request with suggested changes - or you can use the project to create a different filter plug-in for Guardium that supports other data sources.
