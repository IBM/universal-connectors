# IBM Cloud MongoDB-Guardium Logstash filter plug-in
This is a [Logstash](https://github.com/elastic/logstash) filter plug-in for the universal connector that is featured in IBM Security Guardium. It parses events and messages from the IBM Cloud MongoDB audit log into a [Guardium record](https://github.com/IBM/universal-connectors/blob/main/common/src/main/java/com/ibm/guardium/universalconnector/commons/structures/Record.java) instance (which is a standard structure made out of several parts). The information is then sent over to Guardium. Guardium records include the accessor (the person who tried to access the data), the session, data, and exceptions. If there are no errors, the 
data contains details about the query "construct". The construct details the main action (verb) and collections (objects) involved.

The plug-in is free and open-source (Apache 2.0). It can be used as a starting point to develop additional filter plug-ins for the Guardium universal connector.

## 1. Configuring the IBM Cloud MongoDB service
### Procedure:
1. Go to https://cloud.ibm.com/login.
2. On the home page, search and select **Databases for MongoDB**.
3. Then,  create a database by following the below steps-
      - In the **Platform** field, select **IBM Cloud**.
      - In **Service Details**,  define your service name (e.g.: MongoDB-test1). 
      - Make the relevant selection in the **Resource Group** drop-down menu. 
      - Choose your location in the **Location** field.
      - In **Resource allocation**, select **custom option**:
			RAM - 14 GB,
			Dedicated Core – 6 cores,
			Disk Usage – 20 GB.
	  -	You can customize the above deployment resources as per your needs.	
	  - In **Database Edition**, Select **Enterprise edition**, as audit log capability is available only on this edition.
      - Click **Create**.
4. Navigate to the **Resource List** and click **Databases**.
5. Select the database you have created to see the details (e.g., MongoDB-test1) .

**Note** - It will take around 60-80 minutes to be active.

### Changing the database admin password
1. Select the database you created above and go to **Settings**.
2. In the **Change Database Admin Password** section, provide the new password and  click **Change Password**.
You will receive a prompt asking "Are you sure you want to continue?".  Select **Change**.

### Connecting to the database through MongoDB Shell
1. In this example, we created an AWS EC2 instance with Amazon Linux 2 AMI for database connectivity.
2. After the EC2 instance is created, 
      - Create the repository file for MongoDB by running the below command  
         ```echo -e "[mongodb-org-4.4] \nname=MongoDB Repository\nbaseurl=https://repo.mongodb.org/yum/amazon/2013.03/mongodb-org/4.4/x86_64/\ngpgcheck=1 \nenabled=1 \ngpgkey=https://www.mongodb.org/static/pgp/server-4.4.asc" | sudo tee /etc/yum.repos.d/mongodb-org-4.4.repo```
      -  Install MongoDB Shell by running the below command    
         ```sudo yum install -y mongodb-org-shell```
3. On your deployment's Overview page, there is an Endpoints panel with all the relevant connection information.
4. Copy the CLI endpoint URL.
5. Connect to the AWS EC2 instance that you previously created and paste the URL by replacing username (default: admin) and password credentials. The database will be connected.

## 2. Viewing the Audit logs
### Creating an instance of IBM log analysis on IBM Cloud.
1. On the home page, search and select **IBM Log Analysis** to create an instance.
2. Choose your location in the **Location** field.
3. In the **Pricing Plan** field, select **7 days Log Search**.
4. In the **Service Name** field, type a name for the service (e.g., IBM Log Analysis Test-1).
5. Choose **Resource Group**.
6. Click the **T&C** checkbox.
7. Click **Create**.  

### Configuring platform logs
1. Navigate to the resource list and click **Logging and monitoring**.
2. Select the IBM log analysis instance created in the step above.
3. Click **Configure platform logs**. 
4. Select the region and instance name you specified previously.
5. Click **Select**.  

### Viewing the logs entries on the IBM Log Analysis Instance
1. Navigate to the created instance and Click **Open Dashboard** to view the logs.

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
1. On the home page, search and select **Event Streams** to create the instance.
2. In the **Platform** field, select **IBM Cloud**.
3. Choose your location in the **Location** field
4. In the **Pricing plan** field, select **Standard**.
5. Type a name in the **Service Name** field (e.g., IBM Log Analysis Test-1).
6. Make the relevant selection in the **Resource Group** drop-down menu 
7. Click **Create**.

### Creating a topic
1. Navigate to the created instance, select the Topics tab, and click **Create Topic**.
2. In the **Topic Name** field, give your topic a name (e.g., xyz).
3. Keep the defaults set in the rest of the Topic tab fields, click **Next**, and then click **Create topic**.

### Creating service credentials
1. Navigate to the created instance and go to **Service credentials** in the navigation pane.
2. Click **New credential**.
3. Name the credential so that you can identify its purpose later. You can use the default value.
4. Assign the credential the **Writer** Role.
5. Click **Add**. The new credential is listed in the table in Service credentials.
6. Click the drop-down button before Service credentials to see the `api_key` and `kafka_brokers_sasl` values.

###  Configuring the connection in log analysis to event streams
1. Navigate to the created IBM log analysis instance and click **Open Dashboard**.
2. Click the **Settings** icon.
3. select **Streaming** > **Configuration**.
4. Select **Kafka** as the streaming type. Then, enter the following information:
5. In the **Username** field, enter the value token.
6. In the **Password** field, enter the API key that is associated with the service credential that you want to use to authenticate log analysis with event streams.
7. In the **Bootstrap Server URL** section, enter the `kafka_brokers_sasl` values that are listed in the service credential.
8. Enter the topic name you defined earlier and click **Save**.   
  **Note**- You must enter each URL as individual entries. If you need to add additional URLs, click **Add another URL** to add each additional URL.
9. After you click **Save**, you have to verify by clicking the **yes** button and then **Start**. 
10. Streaming gets an active status after you click **Start**.

## 4. Limitations
1. The analysis is based on IBM Cloud Database for MongoDB 4.4.
2. Logs for SQL errors do not get generated from the data source.
3. You will get 3 separate logs for createIndex events which have IP values of NONE and port values of 0.
   - The `ServerIp` and `ClientIP :` fields are populated with `0.0.0.0`, as this information is not embedded in the log messages.
   - The `Client HostName` : field is set to `0.0.0.0` , as the IP value is `NONE`.
4. IBM Cloud Databases for MongoDB only supports 22 events. See [here](https://cloud.ibm.com/docs/databases-for-mongodb?topic=databases-for-mongodb-auditlogging) for more information.
5. In this example, we used both CLI and UI queries to run the analysis.
6. Using CLI for the third-party tool MongoDB Compass supports all 22 events supported by IBM Cloud Databases for MongoDB. But using the UI for MongoDB Compass supports only the following 6 events:
   -  `createDatabase`
   -  `dropDatabase`
   -  `createCollection`
   -  `dropCollection`
   -  `createIndex`
   -  `dropIndex`
7. IBM Cloud Platform doesn't retain the source IP addresses of the connection. Instead, an internal IP addresses (local and remote) are shown in audit logs for each connection.
8. The following important fields cannot be mapped with MongoDB logs:
   - Source program 
   - Client HostName 

## 5. Configuring the IBM Cloud MongoDB filter in Guardium
The Guardium universal connector is the Guardium entry point for native audit logs. The Guardium universal connector identifies and parses the received events, and converts them to a standard Guardium format. The output of the Guardium universal connector is forwarded to the Guardium sniffer on the collector, for policy and auditing enforcements. Configure Guardium to read the native audit logs by customizing the MongoDB template.

### Before you begin
* Configure the policies you require. See [policies](/../../#policies) for more information.
* You must have permission for the S-Tap Management role. The admin user includes this role by default.
* Download the [guardium_logstash-offline-plugins-mongo.zip](https://github.com/IBM/universal-connectors/raw/release-v1.2.0/filter-plugin/logstash-filter-mongodb-guardium/MongodbOverFilebeatPackage/MongoDB/guardium_logstash-offline-plugins-mongo.zip)) plug-in. (Do not unzip the offline-package file throughout the procedure). This step is not necessary for Guardium Data Protection v12.0 and later.
* Download the plug-in filter configuration file [IBMCloudMongoDB.conf](IBMCloudMongoDB.conf).

### Procedure
1. On the collector, go to **Setup** > **Tools and Views** > **Configure Universal Connector**.
2. Before you upload the universal connector, enable the connector if it is disabled.
3. Click **Upload File** and select the offline [guardium_logstash-offline-plugins-mongo.zip](https://github.com/IBM/universal-connectors/raw/release-v1.2.0/filter-plugin/logstash-filter-mongodb-guardium/MongodbOverFilebeatPackage/MongoDB/guardium_logstash-offline-plugins-mongo.zip)) plug-in. After it is uploaded, click **OK**. This step is not necessary for Guardium Data Protection v12.0 and later.
4. Click the Plus sign to open the Connector Configuration dialog box.
5. Type a name in the **Connector name** field.
6. Update the input section to add the details from the [IBMCloudMongoDB.conf](IBMCloudMongoDB.conf) file's input part, omitting the keyword "input{" at the beginning and its corresponding "}" at the end.
7. Update the filter section to add the details from the [IBMCloudMongoDB.conf](IBMCloudMongoDB.conf) file's filter part, omitting the keyword "filter{" at the beginning and its corresponding "}" at the end.
8. The **type** fields should match in the input and filter configuration sections. This field should be unique for every individual connector added.
9. Click **Save**. Guardium validates the new connector, and enables the universal connector if it was disabled. After it is validated, it appears in the Configure Universal Connector page.
 
