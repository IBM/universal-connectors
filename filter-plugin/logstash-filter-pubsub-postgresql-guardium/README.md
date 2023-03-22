# Logstash Filter Pub/Sub PostgreSQL Plugin
### Meet Pub/Sub PostgreSQL
* Tested versions: 13.0, 14.0
* Environment: Google Cloud Platform (GCP)
* Supported inputs: Pub/Sub (pull)
* Supported Guardium versions:
    * Guardium Data Protection: 11.4 and above

This is a Logstash filter plug-in for the univer***REMOVED***l connector that is featured in IBM Security Guardium. It parses a GCP (Google Cloud Platform) audit event into a Guardium record instance, which standardizes the event into several parts before it is sent over to Guardium.
Generated with Logstash v7.15.0.

### Note
This plug-in contains a runtime dependency of Logstash Google PubSub input plug-in (version ~> 1.2.1, i.e. at least 1.2.1). To install, refer to the [Prerequisites](#Prerequisites) section of the Documentation below.

The plug-in is free and open-source (Apache 2.0). It can be used as a starting point to develop additional filter plug-ins for Guardium univer***REMOVED***l connector.

## Documentation

### Prerequisites
Download the [Logstash Offline package](../../filter-plugin/logstash-filter-pubsub-postgresql-guardium/PubSubPostgreSQLPackage/logstash-offline-plugins-filter-pubsub-postgresql-guardium.zip) that includes both the Logstash Google PubSub input plugin and the PostgreSQL PubSub filter plugin, and upload it to the gmachine.
#### Note
This version is compliant with GDP v11.4 and above.
Please refer to the [input plugin's repository](../../input-plugin/logstash-input-google-pubsub) for more information.

###  Create the SQL instance and Configure Logging
#### Create a SQL instance
1. [Prerequisites](https://cloud.google.com/sql/docs/postgres/create-instance#before_you_begin)
2. [Creating a PostgreSQL instance](https://cloud.google.com/sql/docs/postgres/create-instance#create-2nd-gen)
3. Set and ***REMOVED***ve the admin user `postgres`'s password
4. Enable mes***REMOVED***ge ordering option
#### Configure Logging
1. Login to the instance with `gcloud` in *SQL Instance > connect using Cloud Shell* (password of admin user "postgres" is defined upon instance creation)
2. *SQL Instances > Select & Edit instance >* add the following flags:
   - **cloudsql.enable_pgaudit**: on
   - **pgaudit.log**: all
3. Run the following once logged-in in the Cloud Shell terminal
      `CREATE EXTENSION pgaudit;`
4. Add an inclusion filter by editing the Sink via *Logs Router > Edit Sink > Sink Inclusion Filter*:
      ```
    (resource.type="cloudsql_database" resource.labels.database_id="<PROJECT_ID>:<SQL_INSTANCE_ID>"
    logName=("projects/<PROJECT_ID>/logs/cloudaudit.googleapis.com%2Fdata_access")
    protoPayload.request.@type="type.googleapis.com/google.cloud.sql.audit.v1.PgAuditEntry")
    OR
    (resource.type="cloudsql_database" resource.labels.database_id="<PROJECT_ID>:<SQL_INSTANCE_ID>"
    logName="projects/<PROJECT_ID>/logs/cloudsql.googleapis.com%2Fpostgres.log"
    severity=(EMERGENCY OR ALERT OR CRITICAL OR ERROR OR WARNING OR NOTICE OR DEBUG OR DEFAULT))
    ```
### Supported audit logs
  1. PgAudit - `INFO` logs
  2. postgres.log - `EMERGENCY`, `ALERT`, `CRITICAL`, `ERROR`, `WARNING`, `NOTICE`, `DEBUG`, `DEFAULT` logs

### Notes
- `serverHostName` field is populated with the name of the MySQL instance connection
- `serviceName` field is populated with Cloud SQL Service. See [docs](https://cloud.google.com/sql/docs/postgres)


### Limitations
- The _SQL string that caused the exception_ column in the report is blank, since this information is not embedded in the error mes***REMOVED***ges pulled from Google Cloud.
- `sourceProgram` field is left blank since this information is not embedded in the mes***REMOVED***ges pulled from Google Cloud. Queries can be run either from Cloud Shell with `gcloud` or from a locally installed PostgreSQL Server
- `clientIP` and `serverIP` fields are populated with `0.0.0.0`, as this information is not embedded in the mes***REMOVED***ges pulled from Google Cloud.

## Installation
To install this plug-in, you need to download the [offline pack](PubSubPostgreSQLPackage/logstash-offline-plugins-filter-pubsub-postgresql-guardium.zip) and upload the file to the gmachine

After you install the plug-in's offline package and upload and ***REMOVED***ve the configuration to your Guardium machine, restart the Univer***REMOVED***l Connector using the Di***REMOVED***ble/Enable button or CLI.

### Sample Configuration

  Below is a copy of the filter scope included in `postgresqlGooglePubsub.conf` [file](PubSubPostgreSQLPackage/postgresqlGooglePubsub.conf) that shows a basic configuration for this plugin.
#### Filter part:
  ```
  filter {
  	pubsub-postgresql-guardium{}
    }
  ```

## Troubleshooting
Refer to the input plugin's [Troubleshooting](../../input-plugin/logstash-input-google-pubsub/README.md#troubleshooting) section.

## Contributing

  You can enhance this filter and open a pull request with suggested changes - or you can use the project to create a different filter plug-in for Guardium that supports other data sources.
