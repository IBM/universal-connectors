# Logstash Filter PubSub MySQL Plugin

This is a Logstash filter plug-in for the univer***REMOVED***l connector that is featured in IBM Security Guardium. It parses a GCP (Google Cloud Platform) audit event into a Guardium record instance, which standardizes the event into several parts before it is sent over to Guardium.
Generated with Logstash v7.15.0.

### Note
This plug-in contains a runtime dependency of Logstash Google PubSub input plug-in (version ~> 1.2.1, i.e. at least 1.2.1). To install, refer to the [Prerequisites](#Prerequisites) section of the Documentation below.

The plug-in is free and open-source (Apache 2.0). It can be used as a starting point to develop additional filter plug-ins for Guardium univer***REMOVED***l connector.


## Documentation

### Supported Client Applications:
* DBeaver
* DataGrip
* Google Cloud Shell (`gcloud`)
* MySQL Client
* MySQL Shell
* [Cloud Run](https://cloud.google.com/sql/docs/mysql/connect-run) for `JDBC` and `.NET`

###### Note: all of the above comply with Cloud SQL Proxy authentication, except for `gcloud`
###### Note: `.NET` wasn't tested, but is assumed to have the ***REMOVED***me event structure as `JDBC`


### Prerequisites
Download the [Logstash Offline package](PubSubMySQLPackage/logstash-offline-plugins-filter-pubsub-mysql-guardium.zip) that includes both the Logstash Google PubSub input plugin and the MySQL PubSub filter plugin, and upload it to the gmachine.
#### Note
This version is compliant with GDP v11.4 and above. Please refer to the
[input plugin's repository](../../input-plugin/logstash-input-google-pubsub) for more information.

### Create the SQL instance and Configure Logging

#### Create the SQL Instance

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
   ###### Note: running this will automatically bind your computer's IP to the MySQL port. In case the bind failed due to addres***REMOVED***lready in use - use `tcp:0.0.0.0:3306` instead.


6. Setup your SQL client application with **DBeaver** as an example:
    1. Click _Database > New Database Connection > MySQL > Next_
    2. You’ll then get to a connection form where you specify host, port, database, user, and password. Leave the
       defaults and add the user and password from GCP. Also click Show all databases
    3. Then you can click the Test Connection button, and you should see a success pop-up mes***REMOVED***ge
    4. Click OK twice, and you are up and running

#### Configure Logging
##### Inclusion Filter
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

##### Exclusion Filter
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
###### Note: the purpose of this exclusion filter is to filter out system and admin logs that are the result of internal packets in Google Cloud.
### Supported audit mes***REMOVED***ges types
* data_access - `INFO`, `DEFAULT`
* mysql.err

### Notes
- `serverHostName` field is populated with the name of the MySQL instance connection
- `serviceName` field is populated with Cloud SQL Service. See [docs](https://cloud.google.com/sql/docs/mysql)
- `dbUser` and `exception.sqlString` fields are populated with "Undisclosed" for `mysql.err` events, as this information is not embedded in the mes***REMOVED***ges pulled from Google Cloud.
- `appUserName` is populated with your IAM Cloud SQL service account ID


## Installation
To install this plug-in, you need to download the [offline pack](https://github.ibm.com/Activity-Insights/univer***REMOVED***l-connectors/blob/master/filter-plugin/logstash-filter-pubsub-mysql-guardium/PubSubMySQLPackage/logstash-offline-plugins-filter-pubsub-mysql-guardium.zip).

### Note
To install on your local machine that is running Logstash, execute:
`bin/logstash-plugin install file:///path/to/logstash-offline-plugin-input-google_pubsub.zip
`

### Sample Configuration

Below is a copy of the filter scope included `mysqlGooglePubsub.conf` [file](PubSubMySQLPackage/mysqlGooglePubsub.conf) that shows a basic
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
   1. **Note:** For security reasons, it is highly recommended that you configure `cloudsqlproxy_enabled` to true for all client applications other than Google Cloud Shell (`gcloud`). Otherwise, `Source Program` is populated with _Google Cloud Shell (gcloud)_.

## Troubleshooting
Refer to the input plugin's [Troubleshooting](../../input-plugin/logstash-input-google-pubsub#troubleshooting) section.

## Contributing
You can enhance this filter and open a pull request with suggested changes - or you can use the project to create a different filter plug-in for Guardium that supports other data sources.
