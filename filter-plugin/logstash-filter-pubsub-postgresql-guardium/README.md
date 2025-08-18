# Logstash Filter Pub/Sub PostgreSQL Plugin
### Overview
* Tested versions: 13.0, 14.0
* Environment: Google Cloud Platform (GCP)
* Supported inputs: Pub/Sub (pull)
* Supported Guardium versions:
    * Guardium Data Protection: 11.4 and later

This is a [Logstash](https://github.com/elastic/logstash) filter plug-in for the universal connector that is featured in IBM Security Guardium. It parses events and messages from BigTable audit/activity logs into a [Guardium record](https://github.com/IBM/universal-connectors/raw/main/common/src/main/java/com/ibm/guardium/universalconnector/commons/structures/Record.java) instance. The information is then pushed into Guardium. Guardium records include the accessor (the person who tried to access the data), the session, data, and exceptions. If there are no errors, the data contains details about the query "construct". The construct details the main action (Verb) and collections (Objects) involved.
The PostgreSQL filter plug-in supports Guardium Data Protection and Guardium Data Security Center. The plug-in is free and open-source (Apache 2.0).


### Note
- This plug-in contains a runtime dependency of Logstash Google PubSub input plug-in (version ~> 1.2.1, i.e. at least 1.2.1).
- This version is compliant with GDP v11.4 or later and Guardium Data Security Center version 3.3.x or later. To create an input plugin, refer to the [input plug-in's repository](https://github.com/IBM/universal-connectors/tree/main/input-plugin/logstash-input-google-pubsub).
- If GCP postgres is configured prior to upgrade to 12.2 then udpate the filter conf after upgrade.

## Configuration


### Configuring the PostgreSQL on GCP
1. [Prerequisites](https://cloud.google.com/sql/docs/postgres/create-instance#before_you_begin)
2. [Creating a PostgreSQL instance](https://cloud.google.com/sql/docs/postgres/create-instance#create-2nd-gen)
3. Set and save the admin user `postgres`'s password
4. Enable message ordering option

### Configure Logging
1. Login to the instance with `gcloud` in *SQL Instance > connect using Cloud Shell* (password of admin user "postgres" is defined upon instance creation)
2. *SQL Instances > Select & Edit instance >* add the following flags:
    - **cloudsql.enable_pgaudit**: on
    - **pgaudit.log**: all
3. Run the following once logged-in in the Cloud Shell terminal
   `CREATE EXTENSION pgaudit;`

### Configuring GCP for the input plug-in
1. [Create a topic in Pub/Sub](https://cloud.google.com/pubsub/docs/create-topic#create_a_topic_2).
2. [Create a subscription in Pub/Sub](https://cloud.google.com/pubsub/docs/create-subscription#create_a_pull_subscription)
3. [Create service account credentials](https://developers.google.com/workspace/guides/create-credentials#create_a_service_account):
    - To provide subscription access to the service account, select the **Pub/Sub Subscriber** role from the role selection list during the service account creation process.
    - You do not need to grant users access to this service account.
4. [Create credentials for a service account](https://developers.google.com/workspace/guides/create-credentials#create_credentials_for_a_service_account). The key is used by the Logstash input plug-in configuration file.
5. [Create a log sink in Pub/Sub](https://cloud.google.com/logging/docs/export/configure_export_v2#creating_sink)
    * Use the following inclusion filter for ```Choose logs to include in sink``` during log sink creation to specify which logs to route.  The following filter captures relevant logs based on ```data access``` and ```activity``` logs:
   
      #### Inclusion Filter
          (resource.type="cloudsql_database" resource.labels.database_id="<PROJECT_ID>:<SQL_INSTANCE_ID>"
          logName=("projects/<PROJECT_ID>/logs/cloudaudit.googleapis.com%2Fdata_access")
          protoPayload.request.@type="type.googleapis.com/google.cloud.sql.audit.v1.PgAuditEntry")
          OR
          (resource.type="cloudsql_database" resource.labels.database_id="<PROJECT_ID>:<SQL_INSTANCE_ID>"
          logName="projects/<PROJECT_ID>/logs/cloudsql.googleapis.com%2Fpostgres.log"
          severity=(EMERGENCY OR ALERT OR CRITICAL OR ERROR OR WARNING OR NOTICE OR DEBUG OR DEFAULT))
       

## Enabling audit logs
The [inclusion filter](#Inclusion-Filter) ensures that only relevant logs are routed.

### Permissions and Roles Details - Required for Viewing/Downloading Logs
To view or download the generated logs, ensure the appropriate Identity and Access Management (IAM) roles are assigned. These roles control access to logs in GCP.
* **View logs**:
  - roles/logging.viewer (Logs Viewer)
  - roles/logging.privateLogViewer (Private Logs Viewer)
* **Download logs**:
    - roles/logging.admin (Logging Admin)
    - roles/logging.viewAccessor (Logs View Accessor)

For more details on IAM roles and access control, refer to [Access Control with IAM.](https://cloud.google.com/logging/docs/access-control)

### Set destination permissions
To route audit logs to a specific destination, such as Pub/Sub topic and subscription, follow these steps:

1. [Get sink writer's identity.](https://cloud.google.com/logging/docs/export/configure_export_v2#dest-auth)
2. If you have owner access to the destination, [set access controls](https://cloud.google.com/pubsub/docs/access-control#console). Use the sink writer's identity and paste it in ```New Principals``` policy for topics and subscriptions.
    - For **topics**:
        * Assign the ```Pub/Sub Publisher``` and ```Pub/Sub Subscriber``` role.
    - For **subscriptions**:
        - Assign the ```Pub/Sub Publisher``` role.


### Supported audit logs
  1. PgAudit - `INFO` logs
  2. postgres.log - `EMERGENCY`, `ALERT`, `CRITICAL`, `ERROR`, `WARNING`, `NOTICE`, `DEBUG`, `DEFAULT` logs

### Notes
- `serverHostName` field is populated with the name of the MySQL instance connection
- `serviceName` field is populated with Cloud SQL Service. See [docs](https://cloud.google.com/sql/docs/postgres)

## Guardium Data Protection
The Guardium universal connector is the Guardium entry point for native audit/data_access logs. The Guardium universal connector identifies and parses the received events, and converts them to a standard Guardium format. The output of the Guardium universal connector is forwarded to the Guardium sniffer on the collector, for policy and auditing enforcements. Configure Guardium to read the native audit/data_access logs by customizing the PostgreSQL template.

### Before you begin
* Configure the policies you require. See [policies](/docs/#policies) for more information.
* You must have permission for the S-Tap Management role. The admin user includes this role by default

### Configuration
1. On the collector, go to **Setup** > **Tools and Views** > **Configure Universal Connector**.
2. Enable the connector if it is disabled before uploading the universal connector plug-in.
3. Click ```Upload File``` and select [logstash-filter-pubsub-postgresql-guardium.zip](gi-pubsub-postgresql-package/PostgreSQL/logstash-filter-pubsub-postgresql-guardium.zip) plug-in. After it is uploaded, click OK. This is not necessary for Guardium Data Protection v12.0 and later.
4. Click ```Upload File``` and select the key.json file. After it is uploaded, click **OK**.
5. Click the Plus sign to open the Connector Configuration dialog box.
6. Type a name in the ```Connector name``` field.
7. Update the input section to add the details from the [postgresqlGooglePubsub.conf](postgresqlGooglePubsub.conf) file's input part, omitting the keyword "input{" at the beginning and its corresponding "}" at the end.
8. Update the filter section to add the details from the [postgresqlGooglePubsub.conf](postgresqlGooglePubsub.conf) file's filter part, omitting the keyword "filter{" at the beginning and its corresponding "}" at the end.
9. The "type" fields should match in the input and the filter configuration sections. This field should be unique for every individual connector added. This is no longer required starting v12p20 and v12.1.
10. Click ```Save```. Guardium validates the new connector, and enables the universal connector if it was disabled. After it is validated, it appears in the Configure Universal Connector page.
11. After the offline plug-in is installed and the configuration is uploaded and saved in the Guardium machine, restart the universal connector using the Disable/Enable button.

## Guardium Data Security Center

### Configuration
For 3.6.2 and Saas, refer to [Connecting to data source](https://www.ibm.com/docs/en/gdsc/saas?topic=connector-connecting-data-source-by-using-universal).

Complete the following steps for 3.6.1 and prior:
1. In the main menu, click **Configurations** > **Connections** > **Monitored Data Source**.
2. On the Connections page, click **Manage** > **Universal Connector Plugins**.
3. Click **Add Plugin**, upload the [zip package](GI-PubSubPostgreSQLPackage.zip) file.
4. To connect to a new data source, click **Connections** > **Add connection**.
5. Search for **PostgreSQL** and click **Configure**
6. Enter  Name and Description, click **Next**.
7. In the **Build pipeline**, select the input plugin and filter plugin.
8. Enter the Additional information from the Google Pub/Sub json file.
9. Click **Configure** and then **Done**.


## Limitations
- The _SQL string that caused the exception_ column in the report is blank, since this information is not embedded in the error messages pulled from Google Cloud.
- `sourceProgram` field is left blank since this information is not embedded in the messages pulled from Google Cloud. Queries can be run either from Cloud Shell with `gcloud` or from a locally installed PostgreSQL Server
- `clientIP` and `serverIP` fields are populated with `0.0.0.0`, as this information is not embedded in the messages pulled from Google Cloud.