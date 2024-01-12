# IBM Cloud PostgresSQL-Guardium Logstash filter plug-in
This is a [Logstash](https://github.com/elastic/logstash) filter plug-in for the universal connector that is featured in IBM Security Guardium. It parses events and messages from the IBM Cloud PostgresSQL audit log into a [Guardium record](https://github.com/IBM/universal-connectors/blob/main/common/src/main/java/com/ibm/guardium/universalconnector/commons/structures/Record.java) instance (which is a standard structure made out of several parts). The information is then sent over to Guardium. Guardium records include the accessor (the person who tried to access the data), the session, data, and exceptions. If there are no errors, the data contains SQL commands are not parsed by this plug-in but rather forwarded as it is to Guardium to do the SQL parsing.

The plug-in is free and open-source (Apache 2.0). It can be used as a starting point to develop additional filter plug-ins for the Guardium universal connector.

## 1. Configuring the IBM Cloud PostgresSQL service
### Procedure:
1. Go to https://cloud.ibm.com/login.
2. On the home page search and select Databases for PostgreSQL option to create database.
3. User can create Database by following the below steps:
      - Choose Platform name- IBM Cloud.
      - In Service Details Define Service Name (e.g.- PostgreSQL-test1).
      - Choose Resource Group. 
      - Choose Location (e.g.- `Dallas (us-south)`).
      - In Resource allocation select an initial resource allocation preset, or customize your deployments resources custom option (e.g.- RAM - 1 GB Dedicated Core – 3 cores, Disk Usage – 5 GB). 
      - Click Create.
4. Navigate to the Resource List, click on Databases.
5. Select the database (PostgreSQL-test1) user have created to see the details.

Note - It will take around 15-20 minutes to be Active.

### Changing the Database Admin Password
1. Select the above created database, go to Settings.
2. In the Change Database Admin Password section, provide the new password and  click Change Password. You will receive a prompt asking "Are you sure you want to continue?". Select Change.

### Connecting to Database through IBM Cloud Shell
1. Select the above created database and go to Overview page, below there is an Endpoints panel with all the relevant connection information.
2. In the Quick start, Copy the `Connect to your deployment` command.
3. Open IBM Cloud shell console and paste the command and click enter.
4. After executing the command, user needs to provide the Database Password which has been changed in above step.

## 2. Enabling the audit logs
To enable pgAudit session logging, connect as the admin user and call the set_pgaudit_session_logging function with the appropriate event parameters specified. Session logging is enabled directly in the database and no API or CLI access is provided.

To enable various event types, user first need to connect to the cloud shell and then call following commands:
- SELECT public.set_pgaudit_session_logging('{ddl, role}');
- SELECT public.set_pgaudit_session_logging('{misc}');
- SELECT public.set_pgaudit_session_logging('{FUNCTION}');
- SELECT public.set_pgaudit_session_logging('{MISC_SET}');

To enable all event types at once, user first need to connect to the cloud shell and then call following command:​
- SELECT public.set_pgaudit_session_logging('{ddl, role, misc, FUNCTION, MISC_SET}');

Note: Any subsequent calls replace the existing configuration, they are not additive. For example, a subsequent call to SELECT public.set_pgaudit_session_logging('{misc}'); would log only misc but disable ddl and role.

Session logging is configured per event type. The supported event types across all versions are:
1. FUNCTION : Function calls and DO blocks
2. ROLE : Statements related to roles and privileges- GRANT, REVOKE, CREATE/ALTER/DROP ROLE.
3. DDL : All DDL that is not included in the ROLE class.
4. MISC : Miscellaneous commands, e.g. DISCARD, FETCH, CHECKPOINT, VACUUM, SET.
5. MISC_SET : Miscellaneous SET commands, e.g. SET ROLE. (This additional type is only supported in PostgreSQL 12 or greater).

## 3. Viewing the Audit logs
### Creating an instance of IBM Log Analysis on IBM Cloud
1. On the home page, search and select IBM Log Analysis option to create instance.
2. Choose Location (e.g.-`Dallas (us-south)`).
3. In the Pricing Plan field, select 7 days Log Search.
4. Enter Service Name (e.g.- IBM Log Analysis Test-1).
5. Choose Resource Group same which you selected for server.
6. Click the T&C checkbox.
7. Click create.

### Configuring Platform Logs
1. Navigate to the Resource List, click on Logging and Monitoring option.
2. Select the IBM log analysis instance created in above step.
3. Click on configure platform logs.
4. Select region and respective instance name.
5. Click on Select.  

### Viewing the logs entries on IBM Log Analysis Instance
1. Navigate to the created instance and Click open Dashboard.
2. After executing some queries on cloud shell (after enabling audit logging), respective logs will be reflected on Dashboard. 

## 4. Creating an instance of Event Streams on IBM Cloud
### Procedure:
1. On the home page, search and select Event Streams option to create instance
2. Choose Platform name- Public Cloud.
3. Choose Location (e.g.- `Dallas`).
4. In the Pricing plan field, select Standard.
5. Enter Service Name (e.g.- Event streams-1).
6. Choose Resource Group same which you selected for server.
7. Click create.

#### Creating Topic
1. Navigate to the Resource List, click on Integration option.
2. Select the Event Stream instance created in above step.
3. Select Topics Tab and click on Create Topic.
4. Enter Topic Name.
5. Keep the defaults set in the rest of the topic creation, click Next, and then Create topic.

Note - For Load balancing number of partitions should be at least two or more while creating the topic. Same count should be updated in "consumer_threads" property of input configuration.

#### Creating Service Credentials
1. Navigate to the created event stream and go to Service credentials in the navigation panel.
2. Click New credential.
3. Enter a Credential Name.
4. Give the credential the Writer Role.
5. Click Add. The new credential is listed in the table in Service credentials.
6. Click the dropdown button before to Service credentials to see the api_key, username, password and kafka_brokers_sasl values.

###  Configuring the connection in Log Analysis to Event Streams
1. Navigate to the created IBM Log Analysis instance and Click open Dashboard.
2. Click the Settings icon.
3. select Streaming > Configuration.
4. Select kafka as the streaming type. Then, enter the following information:
      - In the Username field, enter the `user` value that is given in Service credential.
      - In the Password field, enter the `API key` or `Password` that is given 
      in Service credential.
      - In the Bootstrap Server URL section, enter the `kafka_brokers_sasl` values that are listed in the service credential.
      Note- You must enter each URL as individual entries. If you need to add additional URLs, click on Add another URL.
      - Enter the `topic` name which you have created above and click on Save button
      - Verify all the information which you have given and click on yes and after that click on Start stream.

## 5. Limitations
1. The Audit log doesn't contain a server IP. The default value is set to `0.0.0.0` for the `server IP`.
2. The following important fields cannot be mapped with PostgreSql logs:
      - Source program
      - Client Hostname
3. Success Audit log for SELECT, INSERT, UPDATE, DELETE, DECLARE, TRUNCATE queries are not generated, but if those Queries fail for some reason, then the appropriate Failure Log is captured in audit logs and so in the Error Report of Guardium.

## 6. Configuring the IBM Cloud PostgresSQL filter in Guardium
The Guardium universal connector is the Guardium entry point for native audit logs. The Guardium universal connector identifies and parses the received events, and converts them to a standard Guardium format. The output of the Guardium universal connector is forwarded to the Guardium sniffer on the collector, for policy and auditing enforcements. Configure Guardium to read the native audit logs by customizing the PostgreSQL template.

### Before you begin
* Configure the policies you require. See [policies](https://github.com/IBM/universal-connectors/#policies) for more information.
* You must have permission for the S-Tap Management role. The admin user includes this role by default.
* Download the [guardium_logstash-offline-plugin-icd-postgresql.zip](https://github.com/IBM/universal-connectors/raw/main/filter-plugin/logstash-filter-postgres-ibmcloud-guardium/IcdPostgresOverKafkaEvent/guardium_logstash-offline-plugin-icd-postgresql.zip) plug-in.

### Procedure
1. On the collector, go to Setup > Tools and Views > Configure Universal Connector.
2. Before you upload the universal connector, enable the connector if it is disabled.
3. Click Upload File and select the offline [guardium_logstash-offline-plugin-icd-postgresql.zip](https://github.com/IBM/universal-connectors/raw/main/filter-plugin/logstash-filter-postgres-ibmcloud-guardium/IcdPostgresOverKafkaEvent/guardium_logstash-offline-plugin-icd-postgresql.zip) plug-in. After it is uploaded, click OK.
4. Click the Plus sign to open the Connector Configuration dialog box.
5. Type a name in the Connector name field.
6. Update the input section to add the details from [Postgres.conf](Postgres.conf) file's input part, omitting the keyword "input{" at the beginning and its corresponding "}" at the end.
7. Update the filter section to add the details from [Postgres.conf](Postgres.conf) file's filter part, omitting the keyword "filter{" at the beginning and its corresponding "}" at the end.
8. The "type" field should match in the input and filter configuration section. This field should be unique for every individual connector added.
9. Click Save. Guardium validates the new connector, and enables the universal connector if it was disabled. After it is validated, it appears in the Configure Universal Connector page.