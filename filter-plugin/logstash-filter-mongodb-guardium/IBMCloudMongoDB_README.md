# IBM Cloud MongoDB-Guardium Logstash filter plug-in
This is a [Logstash](https://github.com/elastic/logstash) filter plug-in for the universal connector that is featured in IBM Security Guardium. It parses events and messages from the IBM Cloud MongoDB audit log into a [Guardium record](https://github.com/IBM/universal-connectors/blob/main/common/src/main/java/com/ibm/guardium/universalconnector/commons/structures/Record.java) instance (which is a standard structure made out of several parts). The information is then sent over to Guardium. Guardium records include the accessor (the person who tried to access the data), the session, data, and exceptions. If there are no errors, the 
data contains details about the query "construct". The construct details the main action (verb) and collections (objects) involved.

The plug-in is free and open-source (Apache 2.0). It can be used as a starting point to develop additional filter plug-ins for the Guardium universal connector.

## 1. Configuring the IBM Cloud MongoDB service
1. Go to https://cloud.ibm.com/login.
2. On the home page, search and select ```Databases for MongoDB```.
3. Then,  create a database by following the below steps-
      - In the ```Platform``` field, select ```IBM Cloud```.
      - In ```Service Details```,  define your service name (e.g.: MongoDB-test1). 
      - Make the relevant selection in the ```Resource Group``` drop-down menu. 
      - Choose your location in the ```Location``` field.
      - Make the relevant selection in the ```Resource allocation```.
	  -	You can customize the above deployment resources as per your needs.	
	  - In ```Database Edition```, Select ```Enterprise edition```, as audit log capability is available only on this edition.
      - In ```Endpoints```, select ```Public Network```.
      - Click ```Create```.
4. Navigate to the ```Resource List``` and click ```Databases```.
5. Select the database you have created to see the details (e.g., MongoDB-test1) .

**Note** - It will take around 60-80 minutes to be active.

### Changing the database admin password
1. Select the database you created above and go to ```Settings```.
2. In the ```Change Database Admin Password``` section, provide the new password and  click ```Change Password```.
You will receive a prompt asking "Are you sure you want to continue?".  Select ```Change```.


# Connecting to the database through MongoDB Shell

## Prerequisites

- **MongoDB Shell**: Ensure you have the MongoDB Shell installed. You can download it from the [MongoDB Download Center](https://www.mongodb.com/try/download/community).
- **MongoDB Server**: Make sure you have access to a MongoDB server (either local or cloud-based).

## Connecting to MongoDB

### 1. Open MongoDB Shell

Launch the MongoDB Shell by entering the following command in your terminal or command prompt:

```
mongo
```

### 2. Connection String Format

To connect to a MongoDB instance, use the following syntax:

```
mongo <connection_string>
```

### 3. Examples of Connection Strings

#### Local MongoDB Instance

For a local MongoDB instance running on the default port (27017):

```
mongo localhost:27017
```

#### MongoDB Atlas (Cloud)

To connect to a MongoDB Atlas cluster, your connection string will look like this:

```
mongo "mongodb+srv://<username>:<password>@cluster0.mongodb.net/myDatabase"
```

Make sure to replace `<username>`, `<password>`, and `myDatabase` with your actual credentials and database name.

On your deployment's Overview page, there is an Endpoints panel with all the relevant connection information.Copy the CLI endpoint URL.

### 4. Switch to Desired Database

After connecting, switch to your desired database using:

```
use myDatabase
```

### 5. Common Commands

Once connected, you can execute various commands:

- View all databases:
  ```
  show dbs
  ```

- View collections in the current database:
  ```
  show collections
  ```

### 6. Exiting the Shell

To exit the MongoDB Shell, type:

```
exit
```

## Troubleshooting

- **MongoDB Server Not Running**: Ensure that the MongoDB server is running on your machine.
- **Firewall Issues**: Check that your firewall allows connections on the MongoDB port (default: 27017).
- **Incorrect Credentials**: Verify that your username, password, and connection string are correct.

## Note :

If the Instance of IBM Log Analysis and Event Stream is already configured in the region of IBM Cloud Account, then skip Step - 2 and Step - 3.

## 2. Viewing the Audit logs
### Creating an instance of IBM Cloud Logs.
1. On the home page, search and select **IBM Cloud Log ** to create an instance.
2. Choose your location in the ```Location``` field.
3. In the ```Pricing Plan``` field, select ```7 days Log Search```.
4. In the ```Service Name``` field, type a name for the service (e.g., IBM Log Analysis Test-1).
5. Choose ```Resource Group```.
6. Click the ```T&C``` checkbox.
7. Click ```Create```.  

### Configuring platform logs
1. Navigate to the resource list and click ```Logging and monitoring```
2. Select the IBM cloud log instance created in the step above.
3. Click Manage of ```Logs Routing``` in Integrations section.
4. Select the region and set the target as the instance name you specified previously.
5. Click ```Save```.  

### Viewing the logs entries on the IBM Cloud Log Instance
1. Navigate to the created instance and Click ```Open Dashboard``` to view the logs.

### Supported event types
* `authenticate`
* `createCollection`
* `createDatabase`
* `createIndex`
* `renameCollection`
* `dropCollection`
* `dropDatabase`
* `dropIndex`
* `createUser`
* `dropUser`
* `dropAllUsersFromDatabase`
* `updateUser`
* `grantRolesToUser`
* `revokeRolesFromUser`
* `createRole`
* `updateRole`
* `dropRole`
* `dropAllRolesFromDatabase`
* `grantRolesToRole`
* `revokeRolesFromRole`
* `grantPrivilegesToRole`
* `revokePrivilegesFromRole`

## 3. Creating an instance of event streams on IBM Cloud
### Procedure:
1. On the home page, search and select ```Event Streams``` to create the instance.
2. Choose the Platform name - ```Public Cloud```.
3. Choose Location (e.g.- `Dallas`).
4. In the Pricing plan field, select ```Standard```.
5. Enter Service Name (e.g. *Event streams-1*).
6. Select the Resource Group that you selected for server.
7. Click create.

### Creating a topic
1. Navigate to the created instance, select the Topics tab, and click ```Create Topic```.
2. In the ```Topic Name``` field, give your topic a name (e.g., xyz).
3. Keep the defaults set in the rest of the Topic tab fields, click ```Next```, and then click ```Create topic```.

### Creating service credentials
1. Navigate to the created instance and go to ```Service credentials``` in the navigation pane.
2. Click ```New credential```.
3. Name the credential so that you can identify its purpose later. You can use the default value.
4. Assign the credential the ```Writer``` Role.
5. Click ```Add```. The new credential is listed in the table in Service credentials.
6. Click the drop-down button before Service credentials to see the `api_key` and `kafka_brokers_sasl` values.

###  Configuring the connection in log cloud to streams

Verify [here](https://ondeck.console.cloud.ibm.com/docs/cloud-logs?topic=cloud-logs-iam-service-auth-es&interface=ui ) whether required permission are present.
1. Navigate to the created IBM cloud log instance and click ```Open Dashboard```.
2. Click the Data Pipeline icon.
3. Select ```Streams``` > ```Add Stream```.
4. Enter the following information:
    - In the Stream name field, enter the Stream name.
    - In the Stream Url section, enter the `kafka_brokers_sasl` values that are listed in the Service credential.
    - Enter the `topic` name that you  created earlier.
    - Verify all of the information and click ```Create stream```.

## 4. Limitations
1. The analysis is based on IBM Cloud Database for MongoDB 7.0.
2. Logs for SQL errors do not get generated from the data source.
3. IBM Cloud Databases for MongoDB only supports 22 events. See [here](https://cloud.ibm.com/docs/databases-for-mongodb?topic=databases-for-mongodb-auditlogging) for more information.
4. In this example, we used both CLI and UI queries to run the analysis.
5. Using CLI for the third-party tool MongoDB Compass supports all 22 events supported by IBM Cloud Databases for MongoDB. But using the UI for MongoDB Compass supports only the following 6 events:
   -  `createDatabase`
   -  `dropDatabase`
   -  `createCollection`
   -  `dropCollection`
   -  `createIndex`
   -  `dropIndex`
   -  `renameCollection`

6. IBM Cloud Platform doesn't retain the source IP addresses of the connection. Instead, an internal IP addresses (local and remote) are shown in audit logs for each connection.
7. The following important fields cannot be mapped with MongoDB logs:
   - Client HostName 
8. For admin DB, Failed Login is not supported for admin user.

## 5. Configuring the IBM Cloud MongoDB filter in Guardium
The Guardium universal connector is the Guardium entry point for native audit logs. The Guardium universal connector identifies and parses the received events, and converts them to a standard Guardium format. The output of the Guardium universal connector is forwarded to the Guardium sniffer on the collector, for policy and auditing enforcements. Configure Guardium to read the native audit logs by customizing the MongoDB template.

### Before you begin
* Configure the policies you require. See [policies](/../../#policies) for more information.
* You must have permission for the S-Tap Management role. The admin user includes this role by default.
* Download the [guardium_logstash-offline-plugins-mongo.zip](https://github.com/IBM/universal-connectors/releases/download/v1.5.1/logstash-filter-mongodb_guardium_filter.zip) plug-in. (Do not unzip the offline-package file throughout the procedure). This step is not necessary for Guardium Data Protection v12.0 and later.
* Download the plug-in filter configuration file [IBMCloudMongoDB.conf](IBMCloudMongoDB.conf).

### Configuration
1. On the collector, go to ```Setup``` > ```Tools and Views``` > ```Configure Universal Connector```.
2. Enable the universal connector if it is disabled.
3. Click ```Upload File``` and select the offline [guardium_logstash-offline-plugins-mongo.zip](https://github.com/IBM/universal-connectors/releases/download/v1.5.1/logstash-filter-mongodb_guardium_filter.zip) plug-in. After it is uploaded, click ```OK```. This step is not necessary for Guardium Data Protection v12.0 and later.
4. Click the Plus sign to open the Connector Configuration dialog box.
5. Type a name in the ```Connector name``` field.
6. Update the input section to add the details from the [IBMCloudMongoDB.conf](IBMCloudMongoDB.conf) file's input part, omitting the keyword "input{" at the beginning and its corresponding "}" at the end.
7. Update the filter section to add the details from the [IBMCloudMongoDB.conf](IBMCloudMongoDB.conf) file's filter part, omitting the keyword "filter{" at the beginning and its corresponding "}" at the end.
8. The 'type' fields should match in the input and filter configuration sections. This field should be unique for every individual connector added.
9. Click ```Save```. Guardium validates the new connector and displays it in the Configure Universal Connector page.
10. After the offline plug-in is installed and the configuration is uploaded and saved in the Guardium machine, restart the Universal Connector using the ```Disable/Enable``` button.

