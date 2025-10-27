# BigQuery-Guardium Logstash filter plug-in
### Meet BigQuery
* Tested versions: V2
* Environment: Google Cloud Platform (GCP)
* Supported inputs: Pub/Sub (pull)
* Supported Guardium versions:
    * Guardium Data Protection: 11.4 and above

This is a [Logstash](https://github.com/elastic/logstash) filter plug-in for the universal connector that is featured in IBM Security Guardium. It parses a GCP (Google Cloud Platform) event logs into a [Guardium record](https://github.com/IBM/universal-connectors/blob/main/common/src/main/java/com/ibm/guardium/universalconnector/commons/structures/Record.java) instance (which is a standard structure made out of several parts). The information is then sent over to Guardium. Guardium records include the accessor (the person who tried to access the data), the session, data, and exceptions. If there are no errors, the data contains details about the query "construct". The construct details the main action (verb) and collections (objects) involved. As of now, the BigQuery plug-in only supports Guardium Data Protection.

### Note
This plug-in contains a runtime dependency of Logstash Google PubSub input plug-in (version ~> 1.2.1, i.e. at least 1.2.1).

This version is compliant with Guardium Data Protection v11.4 and above. Please refer to the [input plug-in repository](https://github.com/IBM/universal-connectors/tree/main/input-plugin/logstash-input-google-pubsub) for more information.

## Configuring BigQuery on GCP
### BigQuery Setup
1. [Prerequisites](https://cloud.google.com/bigquery/docs/quickstarts/quickstart-cloud-console)
2. BigQuery is automatically enabled in new projects. To activate BigQuery in an existing project, enable the BigQuery API.  Please refer to [Enable the BigQuery API](https://cloud.google.com/bigquery-transfer/docs/enable-transfer-service#creating_a_project_and_enabling_the_api) for more information.
3. Create a BigQuery dataset that stores the data.
  Please refer to [Create a Dataset](https://cloud.google.com/bigquery/docs/datasets) for more information
4. Create a table
    - Expand the: View actions option and click ```Open```.
    - In the details panel, click on (+) Create table.
    - On the Create table page, do the following:
Enter Table Name. e.g., “user”.
You can add more fields to the table by clicking  ```+Add Field```.
    - Click ```Create Table```.
Please refer to [Create Table](https://cloud.google.com/bigquery/docs/tables) for more information
5. Query table data. Please refer to [Query Table Data](https://cloud.google.com/bigquery/docs/quickstarts/quickstart-cloud-console#query_table_data) for more information

### Permissions and Roles Details

##### Grant Permission Required for log view/ download

User can view and download the generated logs.
The following Identity and Access Management roles are required to view and download logs:

* To view logs:
roles/logging.viewer (Logs Viewer)
roles/logging.privateLogViewer (Private Logs Viewer)
* To download logs:
roles/logging.admin (Logging Admin)
roles/logging.viewAccessor (Logs View Accessor)

### Create a topic in Pub/Sub
* Go to the Pub/Sub topics page in the Cloud Console. 
* Click ```Create a topic```
* In the Topic ID field, provide a unique topic name, for example, MyTopic.
* Click ```Create Topic```.

### Create a subscription in Pub/Sub
* Display the menu for the topic created in the previous step and click ```New subscription```.
* Type a name for the subscription, such as MySub.
* Leave the delivery type as Pull.
* Click ```Create```

### Create a log sink in Pub/Sub
* In the Cloud Console, go to the Logging > Log Router page.
* Click ```Create sink```.
* In the Sink details panel, enter the following details:
* Sink name: Provide an identifier for the sink. Note that after you create the sink you cannot rename it. However, you can delete a sink and create a new one.
* Sink description (optional): Describe the purpose or use case for the sink.
* In the Sink destination panel, select the Cloud Pub/Sub topic as sink service and select the topic created in previous steps.
* Choose logs to include in the sink in the Build inclusion filter panel.
* You can filter the logs by log name, resource, and  severity.
Multi-region
* In cases of multiple regions, you need to do the same set of configurations per each region.
Based on the region, different configuration files will be used for the input plug-in

#### Set destination (TOPIC & SUBSCRIPTION) permissions

To set permissions for the log sink to route to its destination, do the following:
* Obtain the sink's writer identity—an email address—from the new sink.
   1. Go to the Log Router page, and select ```menu```  > ```View sink details```.
   2. The writer identity appears in the Sink details panel.
* If you have owner access to the destination:
   1. Add the sink's writer identity to topic >>>>
      - Navigate to the Topic created in the earlier steps
      - Click on SHOW INFO panel
      - Click on ADD PRINCIPAL
      - Paste writer identity in the New Principals
      - Give it the Pub/Sub Publisher role and subscriber role

   2. Add the sink's writer identity to subscription >>>>
      - Navigate to the Subscription
      - Click on SHOW INFO panel
      - Click on ADD PRINCIPAL
      - Paste writer identity in the New Principals
      - Give it the subscriber role

### Create service account credentials
* Go to the Service accounts section of the IAM & Admin console.
* Select ```project``` and click ```Create Service Account```.
* Enter a Service account name, such as Bigquery-pubsub.
* Click ```Create```.
* The owner role is required for the service account. Select the owner role from the drop-down menu
* Click ```Continue```. You do not need to grant users access to this service account.
* Click ```Create Key```. The key is used by the Logstash input plug-in configuration file.
* Select JSON and click ```Create```.

### Inclusion Filter
Edit the Sink via *Logs Router > Sink Inclusion Filter*:
#### Description
The purpose of this inclusion filter is to exclude unnecessary logs and include required logs with resource types and metadata reason as DELETE,TABLE_INSERT_REQUEST,TABLE_DELETE_REQUEST or CREATE and metadtata jobStatus.
```    
(resource.type=("bigquery_project") AND protoPayload.authenticationInfo.principalEmail:* AND
(protoPayload.metadata.jobChange.job.jobStatus.jobState = DONE AND -protoPayload.metadata.jobChange.job.jobConfig.queryConfig.statementType = "SCRIPT"))
OR
(protoPayload.metadata.datasetDeletion.reason = "DELETE") OR (protoPayload.metadata.tableCreation.reason = "TABLE_INSERT_REQUEST") OR (protoPayload.metadata.tableDeletion.reason = "TABLE_DELETE_REQUEST") OR (protoPayload.metadata.datasetCreation.reason = "CREATE")
```

## Viewing the Audit logs

The inclusion filter mentioned above will be used to view the Audit logs in the GCP Logs Explorer.

### Supported audit logs
  1. BigQueryAudit - `ACTIVITY`, `DATA_ACCESS` logs
  2. BigQuery Log - `EMERGENCY`, `ALERT`, `CRITICAL`, `ERROR`, `WARNING`, `NOTICE`, `DEBUG`, `DEFAULT`

## 4. Limitations
1. If no information regarding certain fields is available in the logs, those fields will not be mapped. 
2. Exception object will be prepared based on severity of the logs.
3. The data model size is limited to 10 GB per table. If you have a 100 GB reservation per project per location, BigQuery BI Engine limits the reservation per table to 10 GB. The rest of the available reservation is used for other tables in the project.
4. BigQuery cannot read the data in parallel if you use gzip compression. Loading compressed JSON data into BigQuery is slower than loading uncompressed data.
5. You cannot include both compressed and uncompressed files in the same load job.
6. JSON data must be newline delimited. Each JSON object must be on a separate line in the file.
7. The maximum size for a gzip file is 4 GB.
8. Log messages have a size limit of 100K bytes
9. The Audit/Data access log doesn't contain a server IP. The default value is set 0.0.0.0 for the server IP.
10. The following important fields cannot be mapped, as there is no information regarding these fields in the logs:
    - Source program 
    - OS User
    - Client HostName 
11.  `serverHostName` pattern for BigQuery GCP :
project-id_bigquery.googleapis.com.
12. When you try to create or delete a data set or table using BigQuery UI options, fields like the FULL SQL & Objects and Verbs column appear blank, because these actions don't receive any query from GCP logs. You can ignore these actions, by updating the inculsion filter:
"(resource.type=("bigquery_project") AND protoPayload.authenticationInfo.principalEmail:* AND
(protoPayload.metadata.jobChange.job.jobStatus.jobState = DONE AND -protoPayload.metadata.jobChange.job.jobConfig.queryConfig.statementType = "SCRIPT"))"
13. The parser does not support queries in which a keyword is used as a table name or column name, or in scenarios of nested parameters inside functions.
14. The BigQuery audit log doesn’t include login failed logs, so these will not appear in the guardium LOGIN_FAILED report.
15. Syntactically correct SQL queries that fail on Database will be captured only in SQL_Error report.

## Configuring the BigQuery filter in Guardium
The Guardium universal connector is the Guardium entry point for native audit/data_access logs. The Guardium universal connector identifies and parses the received events, and converts them to a standard Guardium format. The output of the Guardium universal connector is forwarded to the Guardium sniffer on the collector, for policy and auditing enforcements. Configure Guardium to read the native audit/data_access logs by customizing the BigQuery template.

### Before you begin
* Configure the policies you require. See [policies](/docs/#policies) for more information.
* You must have permission for the S-Tap Management role. The admin user includes this role by default
* BigQuery-Guardium Logstash filter plug-in is automatically available with Guardium Data Protection versions 12.x, 11.4 with appliance bundle 11.0p490 or later or Guardium Data Protection version 11.5 with appliance bundle 11.0p540 or later releases.

**Note:** For Guardium Data Protection version 11.4 without appliance bundle 11.0p490 or prior or Guardium Data Protection version 11.5 without appliance bundle 11.0p540 or prior, download the [Logstash_Offline_package_7.x](./guardium_logstash-offline-plugins-ps-bigQuery.zip) plug-in. (Do not unzip the offline-package file throughout the procedure).

### Procedure
1. On the collector, go to Setup > Tools and Views > Configure Universal Connector.
2. Enable the universal connector if it is disabled.
3. Click Upload File and select the relevant plugin based on the version of the Guardium. After it is uploaded, click ```OK```. 
   * For the Guardium 11.x, download the [Logstash_Offline_package_7.x](./guardium_logstash-offline-plugins-ps-bigQuery.zip)
   * For the Guardium 12.x, download the [Logstash_Offline_package_8.x](./logstash-filter-big_query_guardium_filter.zip)
4. Click ```Upload File``` and select the key.json file. After it is uploaded, click ```OK```.
5. Click the Plus sign to open the Connector Configuration dialog box.
6. Type a name in the Connector name field.
7. Update the input section to add the details from the [pubsub_big_query.conf](pubsub_big_query.conf) file's input part, omitting the keyword "input{" at the beginning and its corresponding "}" at the end.
8. Update the filter section to add the details from the [pubsub_big_query.conf](pubsub_big_query.conf) file's filter part, omitting the keyword "filter{" at the beginning and its corresponding "}" at the end.
9. The 'type' fields should match in the input and filter configuration sections. This field should be unique for every individual connector added.
10. Click ```Save```. Guardium validates the new connector and displays it in the Configure Universal Connector page.
11. After the offline plug-in is installed and the configuration is uploaded and saved in the Guardium machine, restart the Universal Connector using the Disable/Enable button.

