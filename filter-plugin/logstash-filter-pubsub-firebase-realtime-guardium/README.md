# Firebase-Realtime-Guardium Logstash filter plug-in
### Meet Firebase-Realtime
* Tested versions: 11.2.1
* Environment: Google Cloud Platform (GCP)
* Supported inputs: Pub/Sub (pull)
* Supported Guardium versions:
    * Guardium Data Protection: 11.4 and above

This is a [Logstash](https://github.com/elastic/logstash) filter plug-in for the universal connector that is featured in IBM Security Guardium. It parses GCP (Google Cloud Platform) event logs into a [Guardium record](https://github.com/IBM/universal-connectors/blob/main/common/src/main/java/com/ibm/guardium/universalconnector/commons/structures/Record.java) instance (which is a standard structure made out of several parts). The information is then sent over to Guardium. Guardium records include the accessor (the person who tried to access the data), the session, data, and exceptions. If there are no errors, the data contains details about the query "construct". The construct details the main action (verb) and collections (objects) involved.

The plug-in is free and open-source (Apache 2.0). It can be used as a starting point to develop additional filter plug-ins for the Guardium universal connector.


### Note
This plug-in contains a runtime dependency of Logstash Google PubSub input plug-in (version ~> 1.2.1, i.e. at least 1.2.1).

This version is for GDP v11.4, i.e., stable version. Please refer to the [input plug-in repository](https://github.com/IBM/universal-connectors/tree/main/input-plugin/logstash-input-google-pubsub) for more information.

For a single query execution two error logs will get generated using Firebase Realtime Database UI(https://console.firebase.google.com).

## 1. Configuring the Firebase Realtime Database on GCP

### Firebase-Realtime Setup 
1. [Prerequisites](https://firebase.google.com/docs/projects/api/workflow_set-up-and-manage-project?authuser=0#before-you-begin)
2. To activate  Firebase-Realtime, enable the  Firebase Management API.  Please refer to [Enable the  Firebase Management API](https://firebase.google.com/docs/projects/api/workflow_set-up-and-manage-project?authuser=0#enable-api) for more information.
3. Add Firebase services to your project (Optional). Please refer to [ Add Firebase to Project](https://firebase.google.com/docs/projects/api/workflow_set-up-and-manage-project?authuser=0#add-firebase) for more information.
4. Add Firebase Apps to your Firebase-Realtime project (Optional). Please refer to [Add Firebase Apps to Firebase project](https://firebase.google.com/docs/projects/api/workflow_set-up-and-manage-project?authuser=0#enable-api) for more information.
5. Link your Firebase-Realtime project to a Google Analytics account (Optional). Please refer to [Firebase project to Google Analytics account](https://firebase.google.com/docs/projects/api/workflow_set-up-and-manage-project?authuser=0#link-ga-account) for more information.
6. Finalize your project's default location (Optional). Please refer to [Finalize project's default location](https://firebase.google.com/docs/projects/api/workflow_set-up-and-manage-project?authuser=0#finalize-default-location) for more information.
7. Go to the search Box and search “Firebase"
        7.1) Click on the URL(https://firebase.google.com/) to see the Firebase console.
        7.2) Go to the console and select the GCP project.
        7.3) To create a Realtime Database, go to Build > Realtime Database. 
        7.4) Click Create Database.
        7.5) Click Save.

### Access Permission for Firebase-Realtime
Ensure you have Firebase Realtime Database Admin permissions set up in your profile to access Firebase:
   1. Go to IAM > PRINCIPALS > Firebase Realtime Database Admin
   2. Click Save.  

### Configure Logging & Filter the logs using resources, logs, and severity levels to display
1. Select a Google Cloud project. Go to the Google Cloud navigation menu and select Logging > Logs Explorer.
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

#### Steps to Set destination(TOPIC & SUBSCRIPTION) permissions
To set permissions for log sink to route to its destination, do the following:
* Copy the sink's writer identity—an email address—from the new sink.
1. Go to the Log Router page, and select menu  > View sink details. 
2. The writer identity appears in the Sink details panel.
* If you have Owner access to the destination
   1. Add the sink's writer identity to the topic and give it the Pub/Sub Publisher role and subscriber role.
   2. Add the sink's writer identity to the subscription and give it the Pub/Sub subscriber role.

 ## 2. Configuration on GCP for Input Plugin

### Steps to create Topic in Pub/Sub
* Go to the Pub/Sub topics page in the Cloud Console. 
* Click Create a topic
* In the Topic ID field, provide a unique topic name. For example, MyTopic.
* Click Create Topic.

### Steps to create a subscription in Pub/Sub
* Display the menu for the topic created in the previous step and click New subscription.
* Type a name for the subscription, such as MySub.
* Leave the delivery type as Pull.
* Click Create.

### Steps to create log sink in Pub/Sub
* In the Cloud Console, go to the Logging > Log Router page. 
* Click on Create sink.
* In the Sink details panel, enter the following details:
* Sink name: Provide an identifier for the sink; note that after you create the sink, you can't rename it. However, you can delete it and create a new sink.
* Sink description (optional): Describe the purpose or use case for the sink.
* In the Sink destination panel, select the Pub/Sub topic as sink service and destination.
* Choose logs to include in the sink in the Build inclusion filter panel. 
* The user can filter the logs using log name, resource, and severity.
* In cases of multiple regions, use the same set of configurations for all regions.
Use different configuration files for the input plug-in based on the region.

### Steps to Create service account credentials
* Go to the Service accounts section of IAM & Admin console. 
* Select project and click Create Service Account.
* Enter a Service account name, such as firebase-pubsub.
* Click Create.
* The service account needs access to a subscription that was created earlier. Use the Select a role drop-down menu to add the Owner role.  ​ 
* Click Continue. You do not need to grant users access to this service account.
* Click Create Key. The key is used by the Logstash input plug-in configuration file.
* Select JSON and click Create. 

### Inclusion Filter
Edit the Sink via *Logs Router > Sink Inclusion Filter*:
#### Description 
The purpose of this inclusion filter is to exclude unnecessary logs and include required logs with resource types datastore_database, audited_resource ,severity error and, service firebasedatabase.googleapis.com.

 ```
resource.type=("datastore_database" OR "audited_resource") AND protoPayload.serviceName="firebasedatabase.googleapis.com" AND NOT protoPayload.request.validateOnly=true OR (severity= "ERROR")


```
## 3. Viewing the Audit logs

The inclusion filter mentioned above will be used to view the logs in the GCP Logs Explorer.


### Supported audit logs
  1. Firebase-Realtime Audit Logs - `ACTIVITY`, `DATA_ACCESS` logs
  2. Firebase-Realtime other Logs - `EMERGENCY`, `ALERT`, `CRITICAL`, `ERROR`, `WARNING`, `NOTICE`, `DEBUG`, `DEFAULT`

### Supported Data-access/activity events
* CreateDatabaseInstance
* DeleteDatabaseInstance
* DisableDatabaseInstance
* ReenableDatabaseInstance
* ListDatabaseInstances
* GetDatabaseInstance(Getting this event only while using Firebase CLI)


## 4. Configuring the Firebase-Realtime filter in Guardium
The Guardium universal connector is the Guardium entry point for native audit/data_access logs. The Guardium universal connector identifies and parses the received events, and converts them to a standard Guardium format. The output of the Guardium universal connector is forwarded to the Guardium sniffer on the collector, for policy and auditing enforcements. Configure Guardium to read the native audit/data_access logs by customizing the Firebase template.

### Before you begin
*  Configure the policies you require. See [policies](/../../#policies) for more information.
* You must have permission for the S-Tap Management role. The admin user includes this role by default.
* Firebase-Realtime-Guardium Logstash filter plug-in is automatically available with Guardium Data Protection versions 12.x, 11.4 with appliance bundle 11.0p490 or later or Guardium Data Protection version 11.5 with appliance bundle 11.0p540 or later releases.

**Note**: For Guardium Data Protection version 11.4 without appliance bundle 11.0p490 or prior or Guardium Data Protection version 11.5 without appliance bundle 11.0p540 or prior, download the [Logstash_Offline_package_7.x](https://github.com/IBM/universal-connectors/raw/release-v1.2.0/filter-plugin/logstash-filter-pubsub-firebase-realtime-guardium/PubSubFirebasePackage/guardium_logstash-offline-plugin-Firebase.zip) plug-in. (Do not unzip the offline-package file throughout the procedure).

### Procedure
1. On the collector, go to Setup > Tools and Views > Configure Universal Connector.
2. First, enable the Universal Guardium Connector, if it is disabled.
3. Click Upload File and select the offline [guardium_logstash-offline-plugin-Firebase.zip](https://github.com/IBM/universal-connectors/raw/release-v1.2.0/filter-plugin/logstash-filter-pubsub-firebase-realtime-guardium/PubSubFirebasePackage/guardium_logstash-offline-plugin-Firebase.zip) plug-in. After it is uploaded, click OK. This step is not necessary for Guardium Data Protection v11.0p490 or later, v11.0p540 or later, v12.0 or later.
4. Click Upload File and select the key.json file(which was generated above for Service Account). After it is uploaded, click OK.
5. Click the plus icon to open the Connector Configuration dialog box.
6. Type a name in the Connector name field.
7. Update the input section to add the details from the [firebase_with_pubsub.conf](./firebase_with_pubsub.conf) file's input part, omitting the keyword "input{" at the beginning and its corresponding "}" at the end.
8. Update the filter section to add the details from the [firebase_with_pubsub.conf](./firebase_with_pubsub.conf) file's filter part, omitting the keyword "filter {" at the beginning and its corresponding "}" at the end.
9. The 'type' fields should match in the input and filter configuration sections.This field should be unique for every individual connector added.
10. Click Save. Guardium validates the new connector, and enables the universal connector if it was disabled. After it is validated, it appears in the Configure Universal Connector page.
11. After offline plug-in is installed and the configuration is uploaded and saved in the Guardium machine, restart the Universal Connector using the Disable/Enable button.

## 5. Limitations
- Error Logs(LOGIN FAILED) are not generated in GCP for Firebase and this plugin does not support error traffic in Guardium.
- The audit/data access log doesn't have a server IP. The default value for server IPs is as 0.0.0.0.
- Some fields could not be mapped as no such field were found in the logs. The following important fields couldn't be mapped-
   - Source program : Not available with logs<br>
   - OS User : Not available with logs<br>
   - Client HostName : Not available with logs
- For ListDatabaseInstances events, the object field is left blank.
- Limitations while using the Firebase Realtime Database UI(https://console.firebase.google.com):<br> 
   - Firebase does not generate logs for Export JSON, Import JSON, Updating JSON and Show legend.<br>
   - Firebase does not generate logs for GetDatabaseInstance event.
- While using Firebase CLI(third-party tool), the GetDatabaseInstance event populates all data operations (GET/PUSH/UPDATE/REMOVE) inside the logs.
- The "logging.googleapis.com" service is a general logging service that logs various activities, such as switching between databases or clicking on different tabs. The service is available on the Firebase UI, and when updating JSON files using the UI. Logs for the Firebase UI and the JSON file updates on the UI will generate the same logs with no new information.
- Error queries are not supported in GCP for Firebase.
