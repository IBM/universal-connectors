# Spanner-Guardium Logstash filter plug-in 
### Meet Spanner
* Tested versions: 1.0.39300
* Environment: Google Cloud
* Supported inputs: Pub/Sub (pull)
* Supported versions:
    * GDP: 11.4 and above

This is a [Logstash](https://github.com/elastic/logstash) filter plug-in for the universal connector that is featured in IBM Security Guardium. It parses events and messages from Spanner audit/activity logs into a [Guardium record](https://github.com/IBM/universal-connectors/blob/main/common/src/main/java/com/ibm/guardium/universalconnector/commons/structures/Record.java) instance (which is a standard structure made out of several parts). The information is then pushed into Guardium. Guardium records include the accessor (the person who tried to access the data), the session, data, and exceptions. If there are no errors, the data contains details about the query "construct". The construct details the main action (verb) and collections (objects) involved.
As of now, the Spanner filter plug-in only supports Guardium Data Protection.

The plug-in is free and open-source (Apache 2.0). It can be used as a starting point to develop additional filter plug-ins for the Guardium universal connector.

### Note
This plug-in contains a runtime dependency of Logstash Google PubSub input plug-in (version ~> 1.2.1, i.e. at least 1.2.1). 

This version is compliant with GDP v11.4 and above. Please refer to the [input plug-in's repository](https://github.com/IBM/universal-connectors/tree/main/input-plugin/logstash-input-google-pubsub) for more information.

## 1. Configuring the Spanner on GCP

#### Spanner Instance Setup
1. [Prerequisites](https://cloud.google.com/spanner/docs/quickstart-console#before-you-begin)
2. Enable the Cloud Spanner API for Spanner. Please refer to [Enable the Cloud Spanner API](https://console.cloud.google.com/flows/enableapi?apiid=spanner.googleapis.com&_ga=2.69819094.793489055.1640153775-1810232467.1637833560&_gac=1.255824122.1639012036.Cj0KCQiAzMGNBhCyARIsANpUkzMIfXsi2xH-E172b_5yWbsy04K8iAVkti5E0AX18B4JbIVhDgn3S58aAl3NEALw_wcB) for more information
3. Creating a Spanner instance. Please refer to [Create an instance](https://cloud.google.com/spanner/docs/quickstart-console#create_an_instance) for more information 
4. Creating a Spanner database. Please refer to [Create a database](https://cloud.google.com/spanner/docs/quickstart-console#create_a_database) for more information 
5. Creating a schema for your database. Please refer to [Create a database schema](https://cloud.google.com/spanner/docs/quickstart-console#create_a_schema_for_your_database) for more information
6. Insert and modify data. Please refer to [Insert and modify data](https://cloud.google.com/spanner/docs/quickstart-console#insert_and_modify_data) for more information 
7. Run a query. Please refer to [Run a query](https://cloud.google.com/spanner/docs/quickstart-console#run_a_query) for more information 


#### Permissions and Roles Details

##### Grant Permission Required for log view / download
You can view and download the generated logs. 
The following identity and access management roles are required to view and download logs:
* Logs view:
roles/logging.viewer (Logs Viewer) 
roles/logging.privateLogViewer (Private Logs Viewer)
* Download logs:
roles/logging.admin (Logging Admin)
roles/logging.viewAccessor (Logs View Accessor)


## 2. Configuration on GCP for the input plug-in

##### Creating a topic in Pub/Sub
* Go to the Pub/Sub topics page in the Cloud Console. 
* Click ```Create a topic```
* In the Topic ID field, provide a unique topic name, for example, MyTopic.
* Click the Create Topic button.

##### Creating a subscription in Pub/Sub
* Display the menu for the topic created in the previous step and click ```New subscription```.
* Type a name for the subscription, such as MySub.
* Leave the delivery type as Pull.
* Click the Create button

##### Creating a log sink in Pub/Sub
* In the Cloud Console, go to the Logging > Log Router page. 
* Click the Create sink button.
* In the Sink details panel, enter the following details:
* Sink name: Provide an identifier for the sink. Note that after you create the sink, you cannot rename it. However, you can delete the sink and create a new one.
* Sink description (optional): Describe the purpose or use case for the sink.
* In the Sink destination panel, select the Pub/Sub topic as sink service and destination.
* Choose logs to include in the sink in the Build inclusion filter panel. 
* You can filter the logs by log name, resource, and severity.
* In cases of multiple regions, use the same set of configurations for all regions. Use different configuration files for the input plug-in based on the region.

##### Setting destination permissions (topic and subscription)
To set permissions for the log sink to route to its destination, do the following:
* Obtain the sink's writer identity from the new sink (it should be an email address). 
   1. Go to the Log Router page, and select menu  > View sink details. 
   2. The writer identity appears in the Sink details panel.
* If you have owner access to the destination,
   1. add the sink's writer identity to the topic and give it the Pub/Sub Publisher and Subscriber role.
   2. add the sink's writer identity to the subscription and give it the Pub/Sub Subscriber role.


##### Creating service account credentials
* Go to the Service accounts section of the IAM & Admin console. 
* Select project and click Create Service Account.
* Enter a Service account name, such as spanner-pubsub.
* Click Create.
* The service account needs access to a subscription that was created earlier. Use the Select a role drop-down menu to add the Pub/Sub Subscriber role. ​ 
* Click Continue. You do not need to grant users access to this service account.
* Click the Create Key. The key is used by the Logstash input plug-in configuration file.
* Select JSON and click Create. 

##### Inclusion Filter
Edit the Sink via *Logs Router > Sink Inclusion Filter*:
##### Description
The purpose of this inclusion filter is to include the required logs and exclude the unnecessary ones, on the basis of some parameter from data_access and activity logs.
 ```
resource.type="spanner_instance" resource.labels.instance_id="<instance_id>"
(logName="projects/<project-id>/logs/cloudaudit.googleapis.com%2Fdata_access" 
AND (protoPayload.request.queryMode="PROFILE" 
OR protoPayload.request.@type="type.googleapis.com/google.spanner.v1.ExecuteSqlRequest")
AND -protoPayload.request.queryMode="PLAN"
AND -protoPayload.request.requestOptions.requestTag:*
)
OR 
(logName="projects/<project-id>/logs/cloudaudit.googleapis.com%2Factivity" 
AND (type.googleapis.com/google.spanner.admin.database.v1.UpdateDatabaseDdlRequest 
OR type.googleapis.com/google.spanner.admin.database.v1.CreateDatabaseRequest)
AND operation.producer="spanner.googleapis.com"
)
AND -protoPayload.request.sql="SELECT 1"

```
## 3. Viewing the Audit logs

The inclusion filter mentioned above will be used to view the Audit logs in the GCP Logs Explorer.

### Supported audit logs
* spanner-general.log - `INFO`, `DEFAULT`, `ALERT`,`NOTICE`,`DEBUG`,`WARNING`

## 4. Limitations
1. Error Logs are not generated in GCP for spanner and this plug-in does not support errors traffic in Guardium.
2. The Audit/Data access log doesn't contain a server IP. The default value is set to 0.0.0.0 for the server IP.
3. Some fields cannot be mapped, as there is no information about them in the logs. The following important fields cannot be mapped:
    - Source program : Not available with logs
    - OS User : Not available with logs
    - Client HostName : Not available with logs
4. Spanner does not require a DDL query for Drop Database, as the Spanner UI only gives an option to delete and if you delete the database you cannot get query parameters in audit logs. Please refer to [Spanner DDL documentation](https://cloud.google.com/spanner/docs/reference/standard-sql/data-definition-language) for more information. Henceforth,this is not captured in the full SQL report.
5. The parser does not support queries in which a keyword is used as a table name or column name, nor in scenarios of nested parameters inside functions.

## 5. Configuring the Spanner filter in Guardium
The Guardium universal connector is the Guardium entry point for native audit/data_access logs. The Guardium universal connector identifies and parses the received events, and converts them to a standard Guardium format. The output of the Guardium universal connector is forwarded to the Guardium sniffer on the collector, for policy and auditing enforcements. Configure Guardium to read the native audit/data_access logs by customizing the Spanner template.

### Before you begin
* You must have Log Full Details policy enabled on the collector. The detailed steps can be found in step 4 under section Installing and testing the filter or input plug-in on a staging Guardium system on [this page](https://github.com/IBM/universal-connectors/blob/main/docs/developing_plugins_gdp.md).
* You must have permission for the S-Tap Management role. The admin user includes this role by default.
* Download the [guardium_logstash-offline-plugins-gcp-pubsub-spanner.zip](SpannerOverPubSubPackage/guardium_logstash-offline-plugins-gcp-pubsub-spanner.zip) plug-in.

### Procedure
1. On the collector, go to Setup > Tools and Views > Configure Universal Connector.
2. Enable the connector if it is already disabled, before uploading the universal connector.
3. Click upload File and select the [guardium_logstash-offline-plugins-gcp-pubsub-spanner.zip](SpannerOverPubSubPackage/guardium_logstash-offline-plugins-gcp-pubsub-spanner.zip) plug-in. After it is uploaded, click OK.
4. Click Upload File and select the key.json file. After it is uploaded, click OK.
5. Click the Plus sign to open the Connector Configuration dialog box.
6. Type a name in the Connector name field.
7. Update the input section to add the details from [spanner_with_pubsub.conf](spanner_with_pubsub.conf) file's input part, omitting the keyword "input{" at the beginning and its corresponding "}" at the end.
8. Update the filter section to add the details from [spanner_with_pubsub.conf](spanner_with_pubsub.conf) file's filter part, omitting the keyword "filter{" at the beginning and its corresponding "}" at the end.
9.  The 'type' fields should match in input and filter configuration sections. This field should be unique for every individual connector added.
10. Click Save. Guardium validates the new connector, and enables the universal connector if it was disabled. After it is validated, it appears in the Configure Universal Connector page.
11. After installing the plugins offline package and once the configuration is uploaded and saved to your Guardium machine, restart the universal connector using the Disable/Enable button.

