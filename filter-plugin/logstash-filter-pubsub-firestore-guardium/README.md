# FireStore-Guardium Logstash filter plug-in

This is a [Logstash](https://github.com/elastic/logstash) filter plug-in for the univer***REMOVED***l connector that is featured in IBM Security Guardium. It parses GCP (Google Cloud Platform) event logs into a [Guardium record](https://github.com/IBM/univer***REMOVED***l-connectors/blob/main/common/src/main/java/com/ibm/guardium/univer***REMOVED***lconnector/commons/structures/Record.java) instance (which is a standard structure made out of several parts). The information is then sent over to Guardium. Guardium records include the accessor (the person who tried to access the data), the session, data, and exceptions. If there are no errors, the data contains details about the query "construct". The construct details the main action (verb) and collections (objects) involved.The Firestore plugin only supports Guardium Data Protection as of now. 

The plug-in is free and open-source (Apache 2.0). It can be used as a starting point to develop additional filter plug-ins for the Guardium univer***REMOVED***l connector.

### Note
This plug-in contains a runtime dependency of Logstash Google PubSub input plug-in (version ~> 1.2.1, i.e. at least 1.2.1).

This version is for GDP v11.4, i.e. a stable version.Please refer to the [input plug-in repository](https://github.com/IBM/univer***REMOVED***l-connectors/tree/main/input-plugin/logstash-input-google-pubsub) for more information.

## 1. Configuring the FireStore on GCP
### FireStore Setup 
1. [Prerequisites](https://firebase.google.com/docs/projects/api/workflow_set-up-and-manage-project?authuser=0#before-you-begin)
2. To activate  Firebase, enable the  Firebase Management API. Please refer to [Enable the  Firebase Management API](https://firebase.google.com/docs/projects/api/workflow_set-up-and-manage-project?authuser=0#enable-api) for more information.
3. Add Firebase services to your project(Optional). Please refer to [ Add Firebase to Project](https://firebase.google.com/docs/projects/api/workflow_set-up-and-manage-project?authuser=0#add-firebase) for more information.
4. Add Firebase Apps to your Firebase project(Optional). Please refer to [Add Firebase Apps to Firebase project](https://firebase.google.com/docs/projects/api/workflow_set-up-and-manage-project?authuser=0#enable-api) for more information.
5. Link your Firebase project to a Google Analytics account (Optional). Please refer to [Firebase project to Google Analytics account](https://firebase.google.com/docs/projects/api/workflow_set-up-and-manage-project?authuser=0#link-ga-account) for more information.
6. Finalize your project's default location (Optional). Please refer to [Finalize project's default location](https://firebase.google.com/docs/projects/api/workflow_set-up-and-manage-project?authuser=0#finalize-default-location) for more information.
7. Access the GCP console either by searching for "Firestore" via the search box, or by using this URL: https://console.cloud.google.com/firestore/data?referrer=search&project="<project-Id>"
       
        7.1) In the GCP console, click on "Firestore" to see the Firestore console.
        7.2) In the Firebase console, go to Build>Firestore Database to create a Firestore collection. 
        7.3) Enter the collection_id, document_id, field_name, field_type and field_value.
        7.4. Click on Save.

### Access Permission for Firestore  
Ensure you have Firebase Admin permissions set up in your profile to access Firestore:
   1. Go to IAM > PRINCIPALS > Firebase Admin 
   2. Click on Save.


### Configure logging & filter the logs using resources, logs, and severity levels to display
1. Select a Google Cloud project.Go to the Google Cloud navigation menu and select Logging > Logs Explorer.
2. Filter the logs in logs explorer
    - Resources: The resources available in your current project.
    - Logs: The log types available for the current resources in your project.
    - Log severity: The log severity levels.

	  
### Permissions and role details

#### Grant Permission Required for log view/ download
User can view and download the generated logs. 
The following Identity and Access Management roles are required to view and download logs:
* Logs view:
roles/logging.viewer (Logs Viewer) 
roles/logging.privateLogViewer (Private Logs Viewer)
* Download logs:
Logging Admin (roles/logging.admin)
Logs View Accessor (roles/logging.viewAccessor)

#### Steps to set destination(TOPIC & SUBSCRIPTION) permissions
To set permissions for log sink to route to its destination, do the following:
* Copy the sink's writer identity—an email address—from the new sink.
1. Go to the Log Router page, and select menu  > View sink details. 
2. The writer identity appears in the Sink details panel.
* If the user has owner access to the destination:
   1. add the sink's writer identity to the topic and give it the Pub/Sub Publisher role and subscriber role.
   2. add the sink's writer identity to the subscription and give it the Pub/Sub subscriber role.

## 2. Configuration on GCP for Input Plugin

### Steps to create Topic in Pub/Sub
* Go to the Pub/Sub topics page in the Cloud Console. 
* Click on Create a topic
* In the Topic ID field, provide a unique topic name. For example, MyTopic.
* Click on Create Topic.

### Steps to create subscription in Pub/Sub
* Display the menu for the topic created in the previous step and click New subscription.
* Type a name for the subscription, such as MySub.
* Leave the delivery type as Pull.
* Click Create.

### Steps to create log sink in Pub/Sub
* In the Cloud Console, go to the Logging > Log Router page. 
* Click on Create sink button.
* In the Sink details panel, enter the following details:
* Sink name: Provide an identifier for the sink; note that after you create the sink, you cannot rename it. However, you can delete it and create a new sink.
* Sink description (optional): Describe the purpose or use case for the sink.
* In the Sink destination panel, select the Pub/Sub topic as sink service and destination.
* Choose logs to include in the sink in the Build inclusion filter panel. 
* The user can filter the logs using log name, resource, and severity.
* In cases of multiple regions, use the ***REMOVED***me set of configurations for all regions.
Use different configuration files for the input plug-in based on the region.

### Steps to Create service account credentials
* Go to the Service accounts section of IAM & Admin console. 
* Select project and click on Create Service Account.
* Enter a Service account name, such as firestore-pubsub.
* Click Create.
* The service account needs access to a subscription that was created earlier. Use the Select a role drop-down menu to add the Owner role. 
* Click Continue. The user does not need to grant users access to this service account.
* Click the Create Key. The key is used by the logstash input plug-in configuration file.
* Select JSON and click Create. 
* Rename the key file to key.json.

### Inclusion Filter.

Edit the Sink via Logs Router > Sink Inclusion Filter:
#### Description  
The purpose of this inclusion filter is to exclude unneces***REMOVED***ry logs and include required logs with resource types datastore_database, audited_resource, datastore_index and service firestore.googleapis.com.
 
      resource.type=("datastore_database" OR "audited_resource" OR "datastore_index") AND protoPayload.serviceName="firestore.googleapis.com" AND -(protoPayload.methodName=~"Listen" OR protoPayload.methodName=~"ListCollectionIds" OR protoPayload.methodName=~"GetDatabase" OR protoPayload.methodName=~"GetIndex" OR protoPayload.methodName=~"ListFields" OR protoPayload.methodName=~"GetField" OR protoPayload.methodName=~"ListIndexes" OR protoPayload.request.mask.fieldPaths="__name__" OR protoPayload.request.structuredQuery.select.fields.fieldPath="__name__" )

## 3. Viewing the Audit logs

The inclusion filter mentioned above will be used to view the Audit logs in the GCP Logs Explorer.

### Supported audit logs
  1. Firestore Audit Logs - `ACTIVITY`, `DATA_ACCESS` logs
  2. Firestore other Logs - `EMERGENCY`, `ALERT`, `CRITICAL`, `NOTICE`, `DEBUG`, `DEFAULT`

### Supported Data-access/activity events
* ListDocumentsRequest 
* GetDocumentRequest
* StructuredQuery
* Write
* Index(Create and Delete)

## 4. Configuring the Firestore filter in Guardium
The Guardium univer***REMOVED***l connector is the Guardium entry point for native audit/data_access logs. The Guardium univer***REMOVED***l connector identifies and parses the received events, and converts them to a standard Guardium format. The output of the Guardium univer***REMOVED***l connector is forwarded to the Guardium sniffer on the collector, for policy and auditing enforcements. Configure Guardium to read the native audit/data_access logs by customizing the Firestore template.

### Before you begin
   * Configure the policies you require. See [policies](/../../#policies) for more information.
   * You must have permission for the S-Tap Management role. The admin user includes this role by default.
   * Download the [guardium_logstash-offline-plugins-ps-firestore.zip](PubSubFireStorePackage/guardium_logstash-offline-plugins-ps-firestore.zip) plug-in.

### Procedure
1. On the collector, go to Setup > Tools and Views > Configure Univer***REMOVED***l Connector.
2. First, enable the Univer***REMOVED***l Guardium connector if it is di***REMOVED***bled.
3. Click Upload File and select the offline [guardium_logstash-offline-plugins-ps-firestore.zip](PubSubFireStorePackage/guardium_logstash-offline-plugins-ps-firestore.zip) plug-in. After it is uploaded, click OK.
3. Click the plus icon to open the Connector Configuration dialog box.
4. Type a name in the Connector name field.
5. Update the input section to add the details from the [firestore_pubsub_run.conf](PubSubFireStorePackage/firestore_pubsub_run.conf) file's input part, omitting the keyword "input{" at the beginning and its corresponding "}" at the end.
6. Update the filter section to add the details from the [firestore_pubsub_run.conf](PubSubFireStorePackage/firestore_pubsub_run.conf) file's filter part, omitting the keyword "filter {" at the beginning and its corresponding "}" at the end.
7. The "type" fields should match in the input and filter configuration sections. This field should be unique for  every individual connector added.
8. Click Save. Guardium validates the new connector, and enables the univer***REMOVED***l connector if it was
di***REMOVED***bled. After it is validated, it appears in the Configure Univer***REMOVED***l Connector page.
9. After the offline plug-in is installed and the configuration is uploaded and ***REMOVED***ved in the Guardium machine, restart the Univer***REMOVED***l Connector using the Di***REMOVED***ble/Enable button.

## 5. Limitations 
- LOGIN FAILED logs are not generated in GCP for Firestore.
- The audit/data access log doesn't have a server IP. The default value for server IPs is set as 0.0.0.0.
- Some fields could not be mapped as no such fields were found in the logs. The following important fields couldn't be mapped-
   - Source program : Not available with logs<br>
   - OS User : Not available with logs<br>
   - Client HostName : Not available with logs
