# BigQuery-Guardium Logstash filter plug-in
### Meet BigQuery
* Tested versions: V2
* Environment: Google Cloud Platform (GCP)
* Supported inputs: Pub/Sub (pull)
* Supported Guardium versions:
    * Guardium Data Protection: 11.4 or later

This is a [Logstash](https://github.com/elastic/logstash) filter plug-in for the universal connector that is featured in IBM Security Guardium. It parses a GCP (Google Cloud Platform) event logs into a [Guardium record](https://github.com/IBM/universal-connectors/blob/main/common/src/main/java/com/ibm/guardium/universalconnector/commons/structures/Record.java) instance (which is a standard structure made out of several parts). The information is then sent over to Guardium. Guardium records include the accessor (the person who tried to access the data), the session, data, and exceptions. If there are no errors, the data contains details about the query "construct". The construct details the main action (verb) and collections (objects) involved. The BigQuery filter plugin supports Guardium Data Protection and Guardium Data Security Center.

### Note
This plug-in contains a runtime dependency of Logstash Google PubSub input plug-in (version ~> 1.2.1, i.e. at least 1.2.1).

This version is compliant with GDP v11.4 or later and Guardium Data Security Center v3.3.x or later. Please refer to the [input plug-in repository](https://github.com/IBM/universal-connectors/tree/main/input-plugin/logstash-input-google-pubsub) for more information.

## Configuring BigQuery on GCP

### BigQuery Setup
1. [Prerequisites](https://cloud.google.com/bigquery/docs/quickstarts/quickstart-cloud-console)
2. BigQuery is automatically enabled in new projects. To activate BigQuery in an existing project, enable the BigQuery API.  Please refer to [Enable the BigQuery API](https://cloud.google.com/bigquery-transfer/docs/enable-transfer-service#creating_a_project_and_enabling_the_api) for more information.
3. [Create a BigQuery Dataset](https://cloud.google.com/bigquery/docs/datasets).
4. [Create a Table](https://cloud.google.com/bigquery/docs/tables).
5. [Query Table Data](https://cloud.google.com/bigquery/docs/quickstarts/quickstart-cloud-console#query_table_data).

### Configuring GCP for the input plug-in

1. [Create a topic in Pub/Sub](https://cloud.google.com/pubsub/docs/create-topic#create_a_topic_2).
2. [Create a subscription in Pub/Sub](https://cloud.google.com/pubsub/docs/create-subscription#create_a_pull_subscription)
3. [Create a service account](https://developers.google.com/workspace/guides/create-credentials#create_a_service_account):
    - To provide subscription access to the service account, select the **Pub/Sub Subscriber** role from the role selection list during the service account creation process.
    - You do not need to grant users access to this service account.
4. [Create credentials for a service account](https://developers.google.com/workspace/guides/create-credentials#create_credentials_for_a_service_account). The key is used by the Logstash input plug-in configuration file.
5. [Create a log sink in Pub/Sub](https://cloud.google.com/logging/docs/export/configure_export_v2#creating_sink)
    * Use the following inclusion filter for ```Choose logs to include in sink``` during log sink creation to specify which logs to route.  The following filter captures relevant logs based on ```data access``` and ```activity``` logs:
    #### Inclusion Filter
    ```    
    (resource.type=("bigquery_project") AND protoPayload.authenticationInfo.principalEmail:* AND
    (protoPayload.metadata.jobChange.job.jobStatus.jobState = DONE AND -protoPayload.metadata.jobChange.job.jobConfig.queryConfig.statementType = "SCRIPT"))
    OR
    (protoPayload.metadata.datasetDeletion.reason = "DELETE") OR (protoPayload.metadata.tableCreation.reason = "TABLE_INSERT_REQUEST") OR (protoPayload.metadata.tableDeletion.reason = "TABLE_DELETE_REQUEST") OR (protoPayload.metadata.datasetCreation.reason = "CREATE")
    ```

### Permissions and Roles Details - Required for viewing/downloading logs

User can view and download the generated logs.
The following Identity and Access Management roles are required to view and download logs:

* To view logs:
  - roles/logging.viewer (Logs Viewer)
  - roles/logging.privateLogViewer (Private Logs Viewer)
* To download logs:
  - roles/logging.admin (Logging Admin)
  - roles/logging.viewAccessor (Logs View Accessor)

For more details on IAM roles and access control, refer to [Access Control with IAM.](https://cloud.google.com/logging/docs/access-control)

#### Set destination permissions

To route audit logs to a specific destination, such as Pub/Sub topic and subscription, follow these steps:

1. [Get sink writer's identity.](https://cloud.google.com/logging/docs/export/configure_export_v2#dest-auth)
2. If you have owner access to the destination, [set access controls](https://cloud.google.com/pubsub/docs/access-control#console). Use the sink writer's identity and paste it in ```New Principals``` policy for topics and subscriptions.
    - For **topics**:
        * Assign the ```Pub/Sub Publisher``` and ```Pub/Sub Subscriber``` role.
    - For **subscriptions**:
        - Assign the ```Pub/Sub Publisher``` role.

## Viewing the Audit logs

The [inclusion filter](#Inclusion-Filter) mentioned above will be used to view the Audit logs in the GCP Logs Explorer.

### Supported audit logs
  1. BigQueryAudit - `ACTIVITY`, `DATA_ACCESS` logs
  2. BigQuery Log - `EMERGENCY`, `ALERT`, `CRITICAL`, `ERROR`, `WARNING`, `NOTICE`, `DEBUG`, `DEFAULT`

## Configuring the BigQuery filter in Guardium
The Guardium universal connector is the Guardium entry point for native audit/data_access logs. The Guardium universal connector identifies and parses the received events, and converts them to a standard Guardium format. The output of the Guardium universal connector is forwarded to the Guardium sniffer on the collector, for policy and auditing enforcements. Configure Guardium to read the native audit/data_access logs by customizing the BigQuery template.

### Before you begin
* Configure the policies you require. See [policies](/docs/#policies) for more information.
* You must have permission for the S-Tap Management role. The admin user includes this role by default

### Procedure in Guardium Data Protection
1. On the collector, go to Setup > Tools and Views > Configure Universal Connector.
2. Enable the universal connector if it is disabled.
3. Click Upload File and select the relevant plugin based on the version of the Guardium. After it is uploaded, click ```OK```. 
   * For the Guardium 11.x, download the [Logstash_Offline_package_7.x](./guardium_logstash-offline-plugins-ps-bigQuery.zip)
   * For the Guardium 12.x, download the [Logstash_Offline_package_8.x](./logstash-filter-big_query_guardium_filter.zip)
4. Click ```Upload File``` and select the key.json file. After it is uploaded, click ```OK```.
5. Click the Plus sign to open the Connector Configuration dialog box.
6. Type a name in the Connector name field.
7. Update the input section to add the details from the [pubsub_big_query.conf](https://github.ibm.com/Activity-Insights/universal-connectors/blob/master/filter-plugin/logstash-filter-pubsub-bigquery-guardium/pubsub_big_query.conf) file's input part, omitting the keyword "input{" at the beginning and its corresponding "}" at the end.
8. Update the filter section to add the details from the [pubsub_big_query.conf](https://github.ibm.com/Activity-Insights/universal-connectors/blob/master/filter-plugin/logstash-filter-pubsub-bigquery-guardium/pubsub_big_query.conf) file's filter part, omitting the keyword "filter{" at the beginning and its corresponding "}" at the end.
9. The "type" fields should match in the input and the filter configuration sections. This field should be unique for every individual connector added. This is no longer required starting v12p20 and v12.1.
10. Click ```Save```. Guardium validates the new connector and displays it in the Configure Universal Connector page.
11. After the offline plug-in is installed and the configuration is uploaded and saved in the Guardium machine, restart the Universal Connector using the Disable/Enable button.

### Procedure in Guardium Data Security Center
1. In the main menu, click **Configurations** > **Connections** > **Monitored Data Stores**.
2. On the **Connections** page, click **Manage** > **Universal Connector Plugins**.
3. Click **Add Plugin**, upload the zip package file as [gi-pubsub-bigquery-package.zip](https://github.com/IBM/universal-connectors/releases/download/v1.6.4/gi-pubsub-bigquery-package.zip).
4. To connect a new data source, click **Connections** > **Create connection**.
5. Search for **bigquery** and click **Configure**.
6. Enter Name, Description and click **Next**.
7. In the **Build pipeline**, select the input and the filter plugin.
8. Enter the additional information from the Google Pub/Sub json file.
9. Click **Configure** and then **Done**.

## Limitations
1. If no information regarding certain fields is available in the logs, those fields will not be mapped. 
2. Exception object will be prepared based on severity of the logs.
3. The data model size is limited to 10 GB per table. If you have a 100 GB reservation per project per location, BigQuery BI Engine limits the reservation per table to 10 GB. The rest of the available reservation is used for other tables in the project.
4. BigQuery cannot read the data in parallel if you use gzip compression. Loading compressed JSON data into BigQuery is slower than loading uncompressed data.
5. You cannot include both compressed and uncompressed files in the same load job.
6. JSON data must be newline delimited. Each JSON object must be on a separate line in the file.
7. The maximum size for a zip file is 4 GB.
8. Log messages have a size limit of 100K bytes
9. The Audit/Data access log doesn't contain a server IP. The default value is set 0.0.0.0 for the server IP.
10. The following important fields cannot be mapped, as there is no information regarding these fields in the logs:
    - Source program 
    - OS User
    - Client HostName 
11.  `serverHostName` pattern for BigQuery GCP :
project-id_bigquery.googleapis.com.
12. When you try to create or delete a data set or table using BigQuery UI options, fields like the FULL SQL & Objects and Verbs column appear blank, because these actions don't receive any query from GCP logs. You can ignore these actions, by updating the inculsion filter:
```
"(resource.type=("bigquery_project") AND protoPayload.authenticationInfo.principalEmail:* AND
(protoPayload.metadata.jobChange.job.jobStatus.jobState = DONE AND -protoPayload.metadata.jobChange.job.jobConfig.queryConfig.statementType = "SCRIPT"))"
```
13. The parser does not support queries in which a keyword is used as a table name or column name, or in scenarios of nested parameters inside functions.
14. The BigQuery audit log doesn’t include login failed logs, so these will not appear in the guardium LOGIN_FAILED report.
15. While using GCP, duplicate entries may appear in both the reports and audit logs.