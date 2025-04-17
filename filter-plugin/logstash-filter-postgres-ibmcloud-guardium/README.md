# IBM Cloud PostgresSQL-Guardium Logstash filter plug-in

### Meet IBM PostgreSQL
* Tested versions: v15
* Environment: IBM CLOUD
* Supported inputs: Kafka (pull)
* Supported Guardium versions:
  * Guardium Data Protection: 11.4 and later
  
This [Logstash](https://github.com/elastic/logstash) filter plug-in for IBM Security Guardium the universal connector parses events and messages from the IBM Cloud PostgresSQL audit log into a [Guardium record](https://github.com/IBM/universal-connectors/blob/main/common/src/main/java/com/ibm/guardium/universalconnector/commons/structures/Record.java) instance (which is a standard structure made out of several parts). The information is then sent over to Guardium. Guardium records the accessor (the person who tried to access the data), the session, data, and exceptions. If there are no errors, the data contains SQL commands are not parsed by this plug-in but rather forwarded to Guardium to do the SQL parsing.

The plug-in is free and open-source (Apache 2.0). You can use it as a starting point to develop additional filter plug-ins for the Guardium universal connector.

## 1. Configuring the IBM Cloud PostgresSQL service

### Procedure:
1. Browse to https://cloud.ibm.com/login.
2. On the home page search and select **Databases for PostgreSQL** option.
3. Create a PostgreSQL database as follows:
   - Select Platform name - *IBM Cloud*.
   - In **Service Details** define the Service Name (e.g.- *PostgreSQL-test1*).
   - Choose a **Resource Group**.
   - Choose a **Location** (e.g.- `Dallas (us-south)`).
   - Under **Resource allocation**, select an initial resource allocation preset, or customize your deployments resources custom option (e.g.- RAM - 1 GB Dedicated Core – 3 cores, Disk Usage – 5 GB).
      - Under **Service Configuration**, select *Public Network* in *Endpoints*.
   - Click **Create**.
4. Navigate to the Resource List and click **Databases**.
5. Select the database (PostgreSQL-test1) that you created to see the details.
   **Note:** The database may take  15-20 minutes to become Active.

### Changing the Database Admin Password
1. Select your newly created database, and go to **Settings**.
2. In the **Change Database Admin Password** section, provide the new password and  click **Change Password**. YAt the "Are you sure you want to continue?" prompt, click **Change**.

### Connecting to Database through IBM Cloud Shell
1. Select your new database and go to the Overview page. Browse to the **Endpoints** pane, which contains all of the relevant connection information.
2. Under Quick start, copy the `Connect to your deployment` command.
3. Open IBM Cloud shell console, paste the command, and click **Enter**.
4. After you run the command, provide the database password (that you changed in the previous step).

## 2. Enabling the audit logs
To enable pgAaudit session logging, connect as the admin user and call the set_pgaudit_session_logging function with the appropriate event parameters specified. Session logging is enabled directly in the database and no API or CLI access is provided.
To enable various event types, connect to the cloud shell and then run the following commands:
```
- SELECT public.set_pgaudit_session_logging('{ddl, role}');
- SELECT public.set_pgaudit_session_logging('{misc}');
- SELECT public.set_pgaudit_session_logging('{FUNCTION}');
- SELECT public.set_pgaudit_session_logging('{MISC_SET}');
```
To enable all event types at once, user first need to connect to the cloud shell and then call following command:​
```
- SELECT public.set_pgaudit_session_logging('{ddl, role, misc, FUNCTION, MISC_SET}');
```
**Note:** Any subsequent calls replace the existing configuration; that is, they are not additive. For example, a subsequent call to SELECT public.set_pgaudit_session_logging('{misc}'); logs only misc but disables ddl and role.
Session logging is configured per event type. The supported event types across all versions are:
- FUNCTION : Function calls and DO blocks
- ROLE : Statements related to roles and privileges- GRANT, REVOKE, CREATE/ALTER/DROP ROLE.
- DDL : All DDL that is not included in the ROLE class.
- MISC : Miscellaneous commands, e.g. DISCARD, FETCH, CHECKPOINT, VACUUM, SET.
- MISC_SET : Miscellaneous SET commands, e.g. SET ROLE. (This additional type is only supported in PostgreSQL 12 or greater).

## Note :

If the instance of IBM Log Analysis and Event Stream is already configured in the region of IBM Cloud Account, then skip Step - 3 and Step - 4.

## 3. Viewing the Audit logs

### Creating an instance of IBM Cloud Log
1. On the home page, search and select **IBM Cloud Log** option to create an instance.
2. Choose Location (e.g.-`Dallas (us-south)`).
3. In the Pricing Plan field, select **7 days Log Search**.
4. Enter the Service Name (e.g.- *IBM Log Analysis Test-1*).
5. Choose the Resource Group same that you selected for the server.
6. Click the T&C checkbox.
7. Click **Create**.

### Configuring Platform Logs
1. Navigate to the Resource List and select **Logging and Monitoring**.
2. Select the IBM cloud log instance created in the step above.
3. Click Manage of Logs Routing in Integrations section.
4. Select the region and set the target as the instance name you specified previously.
5. Click **Save**.

### Viewing the logs entries on IBM Cloud Log Instance
1. Navigate to the created instance and click **Open Dashboard**.
2. After you run some queries on cloud shell (after enabling audit logging), the respective logs are reflected in the dashboard.

## 4. Creating an instance of Event Streams on IBM Cloud

### Procedure:
1. On the home page, search and select **Event Streams** to create the instance.
2. Choose the Platform name - *Public Cloud*.
3. Choose Location (e.g.- `Dallas`).
4. In the Pricing plan field, select *Standard*.
5. Enter Service Name (e.g. *Event streams-1*).
6. Select the Resource Group that you selected for server.
7. Click create.

#### Creating the Topic
1. Navigate to the Resource List and click on **Integration**.
2. Select the Event Stream instance that you created in the previous step.
3. Select the Topics tab and then select **Create Topic**.
4. Enter a Topic Name.
5. Keep the current defaults, click **Next**, and then click **Create topic**.
   **Note:** For load balancing, make sure that the number of partitions is set to two or more when you create the topic. Use the same number in the input configuration *consumer_threads* property.

#### Creating Service Credentials
1. Navigate to the newly created event stream and select **Service credentials** in the navigation panel.
2. Click **New credential**.
3. Enter a Credential Name.
4. Give the credential the Writer Role.
5. Click **Add**. The new credential is added to the Service credentials table.
6. To see the api_key, username, password and kafka_brokers_sasl values, click the dropdown menu by Service credentials.

###  Configuring the connection in Cloud Log to Event Streams
Note : Verify [here](https://ondeck.console.cloud.ibm.com/docs/cloud-logs?topic=cloud-logs-iam-service-auth-es&interface=ui ) whether required permission are present.
1. Navigate to the IBM Cloud Log instance and click **Open Dashboard**.
2. Click the Data Pipeline icon.
3. Select **Streams > Add Stream**.
4. Enter the following information:
   - In the Stream name field, enter the Stream name.
   - In the Stream Url section, enter the `kafka_brokers_sasl` values that are listed in the Service credential.
   - Enter the `topic` name that you  created earlier.
   - Verify all of the information and click **Create stream**.

## 5. Limitations
-  The Audit log does not contain a server IP. The default value for *server IP* is `0.0.0.0`.
-  The following important fields cannot be mapped with PostgreSql logs:
   - Client Hostname
-  Success Audit logs for SELECT, INSERT, UPDATE, DELETE, DECLARE, TRUNCATE queries are not generated, but if those queries fail for some reason, then the appropriate Failure Log is captured in audit logs and in the Guardium Error Report.
-  Create database instances in different regions. This is an IBM platform limitation where we can't create two Kafka streams from a single IBM Log Analysis.
- Queries containing single line comments will not be parsed.
- Multiline queries will not be parsed.

## 6. Configuring the IBM Cloud PostgresSQL filter in Guardium
The Guardium universal connector is the Guardium entry point for native audit logs. The Guardium universal connector identifies and parses the received events, and converts them to a standard Guardium format. The output of the Guardium universal connector is forwarded to the Guardium sniffer on the collector, for policy and auditing enforcements. Configure Guardium to read the native audit logs by customizing the PostgreSQL template.

**Important**:

• Starting with Guardium Data Protection version 12.1, you can configuring the Universal Connectors in 2 ways. You can either use the legacy flow or the new flow.

• To configure Universal Connector by using the new flow, see [Managing universal connector configuration](https://www.ibm.com/docs/en/gdp/12.x?topic=connector-managing-universal-configuration) topic. 

• To configure the Universal Connector by using the legacy flow, use the procedure from this section.

### Before you begin
- Configure the policies you require. See [policies](https://github.com/IBM/universal-connectors/#policies) for more information.
- You must have permission for the S-Tap Management role. The admin user includes this role by default.
- Download the [guardium_logstash-offline-plugin-icd-postgresql.zip](https://github.com/IBM/universal-connectors/raw/main/filter-plugin/logstash-filter-postgres-ibmcloud-guardium/IcdPostgresOverKafkaEvent/guardium_logstash-offline-plugin-icd-postgresql.zip) plug-in.

### Procedure
1. On the collector, go to **Setup > Tools and Views > Configure Universal Connector**.
2. Before you upload the universal connector, enable the connector if it is disabled.
3. Click **Upload File** and select the offline [guardium_logstash-offline-plugin-icd-postgresql.zip](https://github.com/IBM/universal-connectors/raw/main/filter-plugin/logstash-filter-postgres-ibmcloud-guardium/IcdPostgresOverKafkaEvent/guardium_logstash-offline-plugin-icd-postgresql.zip) plug-in. After it is uploaded, click **OK**.
4. Click the plus sign **(+)** to open **Connector Configuration**.
5. Enter a name in the Connector name field.
6. Update the input section to add the details from [Postgres.conf](Postgres.conf) file's input part, omitting the keyword *input{* at the beginning and its corresponding curly brace *(})* at the end.
7. Update the filter section to add the details from [Postgres.conf](Postgres.conf) file's filter part, omitting the keyword *filter{* at the beginning and its corresponding curly brace *(})* at the end.
8. The "type" fields should match in the input and the filter configuration sections. This field should be unique for every individual connector added. This is no longer required starting v12p20 and v12.1.
9. Click **Save**. Guardium validates the new connector, and enables the universal connector if it was disabled. After it is validated, it appears in the Configure Universal Connector page.
