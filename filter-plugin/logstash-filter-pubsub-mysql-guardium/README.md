# Logstash Filter PubSub MySQL Plugin

This is a Logstash filter plug-in for the univer***REMOVED***l connector that is featured in IBM Security Guardium. It parses a GCP (Google Cloud Platform) audit event into a Guardium record instance, which standardizes the event into several parts before it is sent over to Guardium.

### Note
This plug-in contains a runtime dependency of Logstash Google PubSub input plug-in (version ~> 1.2.1, i.e. at least 1.2.1). To install, refer to the [Prerequisites](#Prerequisites) section of the Documentation below.

The plug-in is free and open-source (Apache 2.0). It can be used as a starting point to develop additional filter plug-ins for Guardium univer***REMOVED***l connector.

## Documentation

### Prerequisites
Download the [Logstash Offline package](PubSubMySQLPackage/logstash-offline-plugins-filter-pubsub-mysql-guardium.zip) that includes both the Logstash Google PubSub input plugin and the MySQL PubSub filter plugin, and upload it to the gmachine.
Please refer to the [input plugin's repository](../../input-plugin/logstash-input-google-pubsub) for more information.

### Create the SQL instance and Configure Logging

#### Create the SQL Instance
1. [Prerequisites](https://cloud.google.com/sql/docs/mysql/create-instance#before_you_begin)
2. [Creating a MySQL instance](https://cloud.google.com/sql/docs/mysql/create-instance#create-2nd-gen)
3. *SQL Instances > Select & Edit instance >* add the following flags:
   - **general_log**: on 
   - **log_output**: FILE
   - **slow_query_log**: on
#### Configure Logging
##### Inclusion Filter
Edit the Sink via *Logs Router > Sink Inclusion Filter*:
 ```
resource.type="cloudsql_database" resource.labels.database_id="<project_name>:<SQL_Instance_name>" (logName="projects/<project_name>/logs/cloudsql.googleapis.com%2Fmysql-general.log" AND textPayload:"Query") OR logName=("projects/<project_name>/logs/cloudsql.googleapis.com%2Fmysql-slow.log" OR "projects/<project_name>/logs/cloudsql.googleapis.com%2Fmysql.err")
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
- `sourceProgram` field is left blank since this information is not embedded in the mes***REMOVED***ges pulled from Google Cloud. Queries can be run either from Cloud Shell with `gcloud` or from a locally installed MySQL Server
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
filter {
	pubsub-mysql-guardium{}
  }
```

## Troubleshooting
Refer to the input plugin's [Troubleshooting](../../input-plugin/logstash-input-google-pubsub#troubleshooting) section.

## Contributing

You can enhance this filter and open a pull request with suggested changes - or you can use the project to create a different filter plug-in for Guardium that supports other data sources.
