# BigTable-Guardium Logstash filter plug-in
### Meet BigTable
* Tested versions: V2
* Environment: Google Cloud Platform (GCP)
* Supported inputs: Pub/Sub (pull)
* Supported Guardium versions:
    * Guardium Data Protection: 12.1 and above

This is a [Logstash](https://github.com/elastic/logstash) filter plug-in for the universal connector that is featured in IBM Security Guardium. It parses GCP (Google Cloud Platform) event logs into a [Guardium record](https://github.com/IBM/universal-connectors/blob/main/common/src/main/java/com/ibm/guardium/universalconnector/commons/structures/Record.java) instance (which is a standard structure made out of several parts). The information is then sent over to Guardium. Guardium records include the accessor (the person who tried to access the data), the session, data, and exceptions. If there are no errors, the data contains details about the query "construct". The construct details the main action (verb) and collections (objects) involved. The Bigtable Logstash filter plug-in supports Guardium Data Protection and Guardium Insights.

### Note
This plug-in contains a runtime dependency of Logstash Google PubSub input plug-in (version ~> 1.2.1, i.e. at least 1.2.1).

This version is compliant with Guardium Data Protection v12.1 and above. For more information, refer to [input plug-in repository](https://github.com/IBM/universal-connectors/tree/main/input-plugin/logstash-input-google-pubsub).

## Configuring BigTable on GCP
### BigTable Setup
1. [Prerequisites](https://cloud.google.com/bigtable/docs/quickstarts/quickstart-cloud-console)
2. BigTable is automatically enabled in new projects. To activate BigTable in an existing project, refer to [Enable the BigTable API](https://console.cloud.google.com/marketplace/product/google/bigtable.googleapis.com).
3. Create a BigTable instance that stores the data, refer to [Create a Dataset](https://cloud.google.com/bigtable/docs/creating-instance).
4. Create a table. For more information, refer to [Create Table](https://cloud.google.com/bigtable/docs/samples/bigtable-hw-create-table).
5. Query table data. For more information, refer to [Query Table Data](https://cloud.google.com/bigquery/docs/external-data-bigtable).

### Permissions and Roles Details

##### Grant Permission Required for log view/ download

You can view and download the generated logs.
You need the following Identity and Access Management (IAM) roles to view and download the generated logs:

* View logs:
  * roles/logging.viewer (Logs Viewer)
  * roles/logging.privateLogViewer (Private Logs Viewer)
* Download logs:
  * roles/logging.admin (Logging Admin)
  * roles/logging.viewAccessor (Logs View Accessor)

### Creating a topic in Pub/Sub
* Go to the Pub/Sub topics page in the Cloud Console.
* Click ```Create a topic```
* In the Topic ID field, provide a unique topic name, for example, ```MyTopic```.
* Click ```Create Topic```.

### Creating a subscription in Pub/Sub
* Display the menu for the topic created in the previous step and click```New subscription```.
* Type a name for the subscription, such as```MySub```.
* Leave the delivery type as```Pull```.
* Click```Create```

### Creating a log sink in Pub/Sub
* In the Cloud Console, go to the ```Logging > Log Router``` page.
* Click ```Create sink```.
* In the Sink details panel, enter the following details:
  * ```Sink name```: Provide an identifier for the sink. Note that after you create the sink you cannot rename it. However, you can delete a sink and create a new one.
  * ```Sink description (optional)```: Describe the purpose or use case for the sink.
  * In the ```Sink destination``` panel, select the Cloud ```Pub/Sub topic``` as sink service and select the topic created in previous steps.
  * Choose logs to include in the sink in the Build inclusion filter panel. You can filter the logs by log name, resource, and  severity. 
  * Multi-region 
    * In cases of multiple regions, you need to do the same set of configurations per each region.
Based on the region, different configuration files will be used for the input plug-in

#### Setting permissions for the destination (TOPIC & SUBSCRIPTION)

To set permissions for the log sink to route to its destination, do the following:
* Obtain the sink's writer identity—an email address—from the new sink. 
  * Go to the```Log Router```page, and select```menu``` >```View sink details```. 
  * The writer identity appears in the Sink details panel.
* If you have owner access to the destination:
   1. Add the sink's writer identity to topic >>>>
      - Navigate to the Topic created in the earlier steps.
      - Click SHOW INFO panel.
      - Click ADD PRINCIPAL.
      - Paste writer identity in the New Principals.
      - Give it the Pub/Sub Publisher role and subscriber role.

   2. Add the sink's writer identity to subscription >>>>
      - Navigate to the Subscription.
      - Click SHOW INFO panel.
      - Click ADD PRINCIPAL.
      - Paste writer identity in the New Principals.
      - Give it the subscriber role.

### Creating service account credentials
1. Go to the Service accounts section of the IAM & Admin console.
2. Select ```project``` and click ```Create Service Account```.
3. Enter a Service account name, such as Bigtable-pubsub.
4. Click ```Create```.
5. The owner role is required for the service account. Select the owner role from the drop-down list.
6. Click ```Continue```. You do not need to grant users access to this service account.
7. Click ```Create Key```. The key is used by the Logstash input plug-in configuration file.
8. Select JSON and click ```Create```.

### Inclusion Filter
Edit the Sink via *Logs Router > Sink Inclusion Filter*:
#### Description
The purpose of this inclusion filter is to exclude unnecessary logs and include required logs with resource types and metadata only from BigTable.
```    
protoPayload.serviceName="bigtableadmin.googleapis.com" OR protoPayload.serviceName="bigtable.googleapis.com"
```

## Viewing the Audit logs

The inclusion filter mentioned above is used to view the Audit logs in the GCP Logs Explorer.

### Supported audit logs
* BigTableAudit - `ACTIVITY`, `DATA_ACCESS` logs
* BigTable Log - `CREATEINSTANCE`, `DELETEINSTANCE`, `UPDATEINSTANCE`, `CREATECLUSTER`, `DELETECLUSTER`, `UPDATECLUSTER`, `CREATETABLE`, `DELETETABLE`, `MODIFYCOLUMNFAMILIES`, `EXECUTEQUERY`, `LISTCLUSTERS`, `LISTINSTANCES`. 

## Limitations
* Exception object is prepared based on severity of the logs.
* The data model size is limited to 10 GB per table. If you have a 100 GB reservation per project per location, BigTable BI Engine limits the reservation per table to 10 GB. The rest of the available reservation is used for other tables in the project.
* BigTable cannot read the data in parallel if you use gzip compression. Loading compressed JSON data into BigTable is slower than loading uncompressed data.
* You cannot include both compressed and uncompressed files in the same load job.
* JSON data must be newline delimited. Each JSON object must be on a separate line in the file.
* The maximum size for a gzip file is 4 GB.
* Log messages have a size limit of 100K bytes.
* The Audit/Data access log doesn't contain a server IP. The default value is set to 0.0.0.0 and can be in IPV4 or IPV6 format.
* The following important fields cannot be mapped, as there is no information regarding these fields in the logs:
    - Source program 
    - OS User
    - Client HostName
* While using GCP, duplicate entries may appear in both the reports and audit logs.
* Bigtable uses two different service names (`bigtable.googleapis.com` & `bigtableadmin.googleapis.com`) depending on the tasks being performed. This results in two distinct S-TAP host entries.
* Multiple sessions from the same Bigtable instance may result in multiple S-TAP entries.
* The BigTable audit log doesn’t include login failed logs. So, these logs do not appear in the guardium LOGIN_FAILED report.

## Configuring the BigTable filter in Guardium
The Guardium universal connector is the Guardium entry point for native audit/data_access logs. The Guardium universal connector identifies and parses the received events, and converts them to a standard Guardium format. The output of the Guardium universal connector is forwarded to the Guardium sniffer on the collector, for policy and auditing enforcements. Configure Guardium to read the native audit/data_access logs by customizing the BigTable template.

### Before you begin
* Configure the policies you require. See [policies](/docs/#policies) for more information.
* You must have permission for the S-Tap Management role. The admin user includes this role by default
* Download the [logstash-filter-big_table_guardium_filter](gdp-pubsub-bigtable-package/logstash-filter-big_table_guardium_filter.zip) plug-in.

### Procedure
1. On the collector, go to ```Setup``` > ```Tools and Views``` > ```Configure Universal Connector```.
2. Enable the universal connector if it is disabled.
3. Click ```Upload File``` and select the offline  [logstash-filter-big_table_guardium_filter](gdp-pubsub-bigtable-package/logstash-filter-big_table_guardium_filter.zip) plug-in. After it is uploaded, click ```OK```.
4. Click ```Upload File``` and select the key.json file. After it is uploaded, click ```OK```.
5. Click the Plus sign to open the Connector Configuration dialog box.
6. Type a name in the Connector name field.
7. Update the input section to add the details from the [pubsub_big_table.conf](gdp-pubsub-bigtable-package/pubsub_big_table.conf) file's input part, omitting the keyword "input{" at the beginning and its corresponding "}" at the end.
8. Update the filter section to add the details from the [pubsub_big_table.conf](gdp-pubsub-bigtable-package/pubsub_big_table.conf) file's filter part, omitting the keyword "filter{" at the beginning and its corresponding "}" at the end.
9. The 'type' fields should match in the input and filter configuration sections. This field should be unique for every individual connector added.
10. Click ```Save```. Guardium validates the new connector and displays it in the Configure Universal Connector page.
11. After the offline plug-in is installed and the configuration is uploaded and saved in the Guardium machine, restart the Universal Connector using the ```Disable/Enable``` button.

