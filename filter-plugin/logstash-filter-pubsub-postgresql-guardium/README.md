# Logstash Filter Pub/Sub PostgreSQL Plugin
### Meet Pub/Sub PostgreSQL
* Tested versions: 13.0, 14.0
* Environment: Google Cloud Platform (GCP)
* Supported inputs: Pub/Sub (pull)
* Supported Guardium versions:
    * Guardium Data Protection: 11.4 and above

This is a Logstash filter plug-in for the universal connector that is featured in IBM Security Guardium. It parses a GCP (Google Cloud Platform) audit event into a Guardium record instance, which standardizes the event into several parts before it is sent over to Guardium.
Generated with Logstash v7.15.0.

### Note
This plug-in contains a runtime dependency of Logstash Google PubSub input plug-in (version ~> 1.2.1, i.e. at least 1.2.1). To install, refer to the [Prerequisites](#Prerequisites) section of the Documentation below.

The plug-in is free and open-source (Apache 2.0). It can be used as a starting point to develop additional filter plug-ins for Guardium universal connector.

## Documentation

#### Note
This version is compliant with GDP v11.4 and above.
Please refer to the [input plugin's repository](../../input-plugin/logstash-input-google-pubsub) for more information.

### Configuring Logging

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
- The _SQL string that caused the exception_ column in the report is blank, since this information is not embedded in the error messages pulled from Google Cloud.
- `sourceProgram` field is left blank since this information is not embedded in the messages pulled from Google Cloud. Queries can be run either from Cloud Shell with `gcloud` or from a locally installed PostgreSQL Server
- `clientIP` and `serverIP` fields are populated with `0.0.0.0`, as this information is not embedded in the messages pulled from Google Cloud.

## Installation
Logstash Filter Pub/Sub PostgreSQL Plugin is automatically available with Guardium Data Protection versions 12.x, 11.4 with appliance bundle 11.0p490 or later or Guardium Data Protection version 11.5 with appliance bundle 11.0p540 or later releases.

Note: For Guardium Data Protection version 11.4 without appliance bundle 11.0p490 or later or Guardium Data Protection version 11.5 without appliance bundle 11.0p540 or later, download the [Logstash_Offline_package_7.x](https://github.com/IBM/universal-connectors/raw/release-v1.2.0/filter-plugin/logstash-filter-pubsub-postgresql-guardium/PubSubPostgreSQLPackage/logstash-offline-plugins-filter-pubsub-postgresql-guardium.zip) plug-in. (Do not unzip the offline-package file throughout the procedure).


After you install the plug-in's offline package and upload and save the configuration to your Guardium machine, restart the Universal Connector using the Disable/Enable button or CLI.

### Sample Configuration

  Below is a copy of the filter scope included in `postgresqlGooglePubsub.conf` [file](PubSubPostgreSQLPackage/postgresqlGooglePubsub.conf) that shows a basic configuration for this plugin.
  **Note** The "type" fields should match in the input and the filter configuration sections. This field should be unique for every individual connector added.

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
