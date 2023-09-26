# SAP HANA-Guardium Logstash filter plug-in
### Meet SAP HANA
* Tested versions: 2.00.033.00.1535711040
* Environment: On-premise, Iaas
* Supported Guardium versions:
	* Guardium Data Protection: 11.4 and above
      * Supported inputs:
        * Filebeat (push)
        * JDBC (pull)
	* Guardium Insights: 3.2
      * Supported inputs:
        * Filebeat (push)
    * Guardium Insights SaaS: 1.0
      * Supported inputs:
        * Filebeat (push)
        * JDBC (pull)

This is a [Logstash](https://github.com/elastic/logstash) filter plug-in for the universal connector that is featured in IBM Security Guardium. It parses events and messages from the SAP HANA audit log into a [Guardium record](https://github.com/IBM/universal-connectors/blob/main/common/src/main/java/com/ibm/guardium/universalconnector/commons/structures/Record.java) instance (which is a standard structure made out of several parts). The information is then sent over to Guardium. Guardium records include the accessor (the person who tried to access the data), the session, data, and exceptions. If there are no errors, the data contains details about the query "construct". The construct details the main action (verb) and collections (objects) involved.

The plug-in is free and open-source (Apache 2.0). It can be used as a starting point to develop additional filter plug-ins for Guardium universal connector.

## 1. Configuring the SAP HANA

There are multiple ways to install a SAP HANA server. For this example, we will assume that we already have a working SAP HANA setup.

## 2. Enabling the audit logs:
### Procedure
In the SAP HANA Studio, expand the system on which you would like to enable auditing.
1. Expand the Security folder.
2. Double click on the ‘Security option’.
3. Click on the auditing status drop-down menu, by default it will be disabled.
4. Select "Enabled".
5. Click "Deploy" or press F8 to save the changes.
6. Restart database instance to reflect new changes.


There are multiple ways to enable auditing in SAP HANA, You can choose as per your requirement.
* CSTABLE base auditing - Audit-trail target is a table, requires JDBC input plug-in.
* CSVTEXTFILE base auditing - Audit-trail target is a file, requires Beat input plugin.

### Enabling CSTABLE base auditing logs:
To perform the below steps, open SAP HANA studio (Eclipse) Select SYSTEMDB user, right-click,  select SQL Console, and then run these Commands:

1. For multiple container tenant database you can enable auditing for CSV File target by using the following command
    1. SAP HANA Command For Enable Auditing:
    ```
    ALTER SYSTEM ALTER CONFIGURATION ('global.ini', 'system') set ('auditing configuration', 'global_auditing_state') = 'true';
    ``` 
    2. To select the target as a table, use the following command
   ```
   ALTER SYSTEM ALTER CONFIGURATION ('global.ini', 'system') set ('auditing configuration', 'default_audit_trail_type') = 'CSTABLE'; 
   ```
    3. To avoid unwanted system logs use below command to store all system logs in table.
   ```
   ALTER SYSTEM ALTER CONFIGURATION ('global.ini', 'system') set ('auditing configuration', 'critical_audit_trail_type') = 'CSVTEXTFILE';
   ```

2. After running the previous command, restart the container and refresh the added systems.

### Enabling CSVTEXTFILE base auditing logs:

To perform the below steps, open SAP HANA studio (Eclipse) Select SYSTEMDB user, right-click,  select SQL Console, and then run these Commands:

1. For multiple container tenant database you can enable auditing for CSV File target by using the following command
    1. SAP HANA Command For Enable Auditing:
    ```
    ALTER SYSTEM ALTER CONFIGURATION ('global.ini', 'system') set ('auditing configuration', 'global_auditing_state') = 'true';
    ``` 
    2. To select the target as a CSV text file, use the following command
   ```
   ALTER SYSTEM ALTER CONFIGURATION ('global.ini', 'system') set ('auditing configuration', 'default_audit_trail_type') = 'CSVTEXTFILE'; 
   ```
    3. To avoid unwanted system logs use below command to store all system logs in table.
   ```
   ALTER SYSTEM ALTER CONFIGURATION ('global.ini', 'system') set ('auditing configuration', 'critical_audit_trail_type') = 'CSTABLE';
   ```

2. After running the previous command, restart the container and refresh the added systems.


## 3. Common steps for either auditing type

### Creating an audit policy

An audit policy defines the actions to be audited. In order to create an audit policy, the user must have AUDIT ADMIN system rights.
Creating an audit policy is a common step for both types of auditing.


#### Procedure

	1. In the SAP HANA Studio, expand the database:
	2. Expand the ‘Security’ folder.
	3. Double click on the ‘Security option’.
	4. Click the green plus sign under the ‘Audit Polices’ panel.
	5. Enter your policy name.
	6. Click in the Audited actions field and then press the ‘…’
	7. Select the actions you would like to audit.
	8. Select when an audit record should be created in the ‘Audited actions status’ column.

		a) SUCCESSFUL – When an action is successfully executed, it is logged

		b) UNSUCCESSFUL	- When an action is unsuccessfully executed, it is logged

		c) ALL	 - Both of the above situations are logged.
	9. Select the audit level:

		a) EMERGENCY

		b) CRITICAL

		c) ALERT

		d) WARNING

		e) INFO (default)

	10. If needed, you can filter the users you would like to audit. Under the users column, you can press the ‘…’ button and then add users.

	11. You can also specify the target object(s) to be audited. This option is valid if the actions to be audited involve SELECT, INSERT, UPDATE, DELETE.

	12. Once done press the deploy button or press F8

		a) Reboot the database instance for the changes to take place.

### Sap Hana Auditing Policy

1. For Audit session-related logs (Connect, Disconnect, Validation Of User), select the following audited actions:
   a) Check the “Connect” checkbox from session management and the  system configuration menu in the audited action tab.

   **Note: For this policy, select "Audited Action Status=Unsuccessful".**

2. For audit DML logs, select the following audited action:

   	a) Check the Data Query and Manipulation checkbox from the audited action tab.

   **Note: For this policy, select "Audited Action  Status=Successful".**

   **Note: It is mandatory to provide a “Target Object” for this policy.**

3. For audit DDL Logs, select the following audited action:

   	a) Check the  “Create Table, Create Function, Create Procedure, Drop Function, Drop Procedure, Drop Table” checkbox from the data definition menu in the audited action table.

   **Note: For this policy, select "Audited Action Status=Successful".**



## 4.  Viewing the audit logs

### View the SAP HANA audit logs for CSTABLE-based auditing.

#### Procedure
```
    1. Connect to the database.
    2. Right-click on it.
    3. Open the SQL console and run the following command:
         Select * from AUDIT_LOG;
```

### View the SAP HANA audit logs for CSVTEXTFILE-based auditing.

#### Procedure
```
    1. To view the audited Logs:

        a.  GOTO Location /usr/sap/host-server/instance/*.audit_trail.csv
        
	2. To view the logs entries on Studio:

        a. Click on HXE system.

        b. GOTO administrative Console.

        c. Click on the diagnostic file tab.

        d. Click on filter and Search for “*.audit_trail.csv”
```
## 4. Configuring Filebeat to push logs to Guardium

This configuration is required to pull logs when CSVTEXTFILE-based auditing is enabled.

## a. Filebeat installation

### Procedure:

1. To install Filebeat on your system, follow the steps in this topic:
   https://www.elastic.co/guide/en/beats/filebeat/current/filebeat-installation-configuration.html#installation

## b. Filebeat configuration:

To use Logstash to perform additional processing on the data collected by Filebeat, we need to configure Filebeat to use Logstash. To do this, modify the filebeat.yml file which you can find inside the folder where Filebeat is installed. Follow these instructions for finding the installation directory:
https://www.elastic.co/guide/en/beats/filebeat/current/directory-layout.html

### Procedure:

1. Configuring the input section :-
    * Locate "filebeat.inputs" in the filebeat.yml file, then add the following parameters.
   ```
   filebeat.inputs:
     - type: log
       enabled: true
       paths:
         - <host_name/trace/DB_<DB_Name>/*.audit_trail.csv>
   ```
    * Add the tags to uniquely identify the SAP HANA events from the rest.
   ```
   tags : ["sapHana"]
   ```

2. Configuring the output section:

    * Locate "output" in the filebeat.yml file, then add the following parameters.
    * Disable Elasticsearch output by commenting it out.
    * Enable Logstash output by uncommenting the Logstash section. For more information, see https://www.elastic.co/guide/en/beats/filebeat/current/logstash-output.html#logstash-output
    * For example:
   ```
   output.logstash:
     hosts: ["<host>:<port>"]
   ```
    * The hosts option specifies the Logstash server and the port (5001) where Logstash is configured to listen for incoming Beats connections.

    * You can set any port number except 5044, 5141, and 5000 (as these are currently reserved in Guardium v11.3 and v11.4 ).

    * Locate "Processors" in the filebeat.yml file and then add the below attribute to get the server's time zone.For more information, see https://www.elastic.co/guide/en/beats/filebeat/current/add-locale.html

    * For example:
    ```
     processors:
       - add_locale: ~
	```
3. To learn how to start FileBeat, see https://www.elastic.co/guide/en/beats/filebeat/current/filebeat-installation-configuration.html#start

### Limitations:

	1. SAP HANA auditing only supports error logs for authentication failures.
	2. SAP HANA does not audit multiple line query properly.
    3. Single line and multi-line comments in between the query are not supported.
	4. The SAP HANA CSVTEXTFILE(audit target) does not audit the DB_Name.
	5. SAP HANA with JDBC shows server ip as 0.0.0.0
	6. Duplicate records will be seen in load balancing.

## 5. Configuring the SAP HANA filters in Guardium

The Guardium universal connector is the Guardium entry point for native audit logs. The universal connector identifies and parses received events, and converts them to a standard Guardium format. The output of the Guardium universal connector is forwarded to the Guardium sniffer on the collector, for policy and auditing enforcements. Configure Guardium to read the native audit logs by customizing the SAP HANA template.

### Before you begin

* You must have permissions for the S-Tap Management role. The admin user includes this role, by default.

* Download the required (ngdbc)jars as per your database version from URL https://tools.hana.ondemand.com/#hanatools .

* For CSVTEXTFILE-based auditing, refer to this [package](https://github.com/IBM/universal-connectors/tree/main/filter-plugin/logstash-filter-saphana-guardium/SaphanaOverFilebeatPackage) and download the [SAPHANA-offline-plugin.zip](https://github.com/IBM/universal-connectors/raw/release-v1.2.0/filter-plugin/logstash-filter-saphana-guardium/SaphanaOverFilebeatPackage/SAPHANA/SAPHANA-offline-plugin.zip) plug-in.(Do not unzip the offline-package file throughout the procedure).This step is not necessary for Guardium Data Protection v12.0 and later.


* For CSTABLE based auditing, refer to this [package](https://github.com/IBM/universal-connectors/tree/main/filter-plugin/logstash-filter-saphana-guardium/SaphanaOverJdbcPackage) and download the [SAPHANA-offline-plugin.zip](https://github.com/IBM/universal-connectors/raw/release-v1.2.0/filter-plugin/logstash-filter-saphana-guardium/SaphanaOverJdbcPackage/SAPHANA-offline-plugin.zip) plug-in.(Do not unzip the offline-package file throughout the procedure).This step is not necessary for Guardium Data Protection v12.0 and later.


# Procedure
1. On the collector, go to Setup > Tools and Views > Configure Universal Connector.
2. First enable the Universal Guardium connector, if it is disabled already.
3. For CSVTEXTFILE-based auditing, follow these steps:-
    1. Click "Upload File" and select the [SAPHANA-offline-plugin.zip](https://github.com/IBM/universal-connectors/raw/release-v1.2.0/filter-plugin/logstash-filter-saphana-guardium/SaphanaOverFilebeatPackage/SAPHANA/SAPHANA-offline-plugin.zip) plug-in as per specific audit. After it is uploaded, click "OK". This is not necessary for Guardium Data Protection v12.0 and later.
    2. Click the Plus sign to open the Connector Configuration dialog box.
    3. Type a name in the Connector name field.
    4. Update the input section. Use the [saphanaFilebeat.conf](https://github.com/IBM/universal-connectors/raw/main/filter-plugin/logstash-filter-saphana-guardium/saphanaFilebeat.conf) file's input part, omitting the keyword "input{" at the beginning and its corresponding "}" at the end.
    5. Update the filter section. Use the [saphanaFilebeat.conf](https://github.com/IBM/universal-connectors/raw/main/filter-plugin/logstash-filter-saphana-guardium/saphanaFilebeat.conf) file's filter part, omitting the keyword "filter{" at the beginning and its corresponding "}" at the end.
    6. The "type" field should match in the input and filter configuration section. This field should be unique for every individual connector added.
    7. Click "Save". Guardium validates the new connector, and enables the universal connector if it was disabled. After it is validated, it appears in the Configure Universal Connector page.

4. For CSTABLE-based auditing, follow these steps:
    1. Click "Upload File" and select the offline [SAPHANA-offline-plugin.zip](https://github.com/IBM/universal-connectors/raw/release-v1.2.0/filter-plugin/logstash-filter-saphana-guardium/SaphanaOverJdbcPackage/SAPHANA-offline-plugin.zip) plug-in as per specific audit. After it is uploaded, click "OK". This step is not necessary for Guardium Data Protection v12.0 and later.
    2. Click "Upload File" again and select the ngdbc-2.9.12 jar file. After it is uploaded, click "OK".
    3. Click the Plus sign to open the Connector Configuration dialog box.
    4. Type a name in the Connector name field.
    5. Update the input section . Use the [saphanaJDBC.conf](https://github.com/IBM/universal-connectors/raw/main/filter-plugin/logstash-filter-saphana-guardium/saphanaJDBC.conf) file's input part, omitting the keyword "input{" at the beginning and its corresponding "}" at the end.
    6. Update the filter section for JDBC Plugin. Use the [saphanaJDBC.conf](https://github.com/IBM/universal-connectors/raw/main/filter-plugin/logstash-filter-saphana-guardium/saphanaJDBC.conf) file's filter part, omitting the keyword "filter{" at the beginning and its corresponding "}" at the end.
    7. The "type" fields should match in the input and the filter configuration sections. This field should be unique for every individual connector added.
    8. If using two jdbc plug-ins on same machine , the last_run_metadata_path file name should be different.

       **Note: For moderate to large amounts of data, include pagination to facilitate the audit and to avoid out of memory errors.  Use the parameters below in the input section when using a JDBC connector, and remove the concluding semicolon ';' from the jdbc statement:**
        ```
       jdbc_paging_enabled => true 
       jdbc_page_size => 1000
       ```
    9. Click Save. Guardium validates the new connector, and enables the universal connector if it was disabled. After it is validated, it appears in the Configure Universal Connector page.

## 6. JDBC Load Balancing Configuration

In SAP HANA JDBC input plug-ins , we distribute load between two machines based on even and odd "sessionIds"

### Procedure
1. On the first G Machine, in the input section for JDBC Plug-in, update the "statement" field as follows:
    ```
    select audit_log.event_status,audit_log.client_ip,audit_log.connection_id,audit_log.client_port,audit_log.timestamp,audit_log.event_action,audit_log.user_name,audit_log.port,audit_log.client_host,audit_log.service_name,audit_log.statement_string,audit_log.application_name,audit_log.host,audit_log.application_user_name,M_DATABASE.database_name from M_DATABASE,audit_log where M_DATABASE.HOST = audit_log.HOST and audit_policy_name not in ('MandatoryAuditPolicy') and mod(connection_id,2) = 0 and timestamp > :sql_last_value;
    ```
2. On the second G machine, in the input section for the JDBC Plug-in, update the  "statement" field as follows:
    ```
    select audit_log.event_status,audit_log.client_ip,audit_log.connection_id,audit_log.client_port,audit_log.timestamp,audit_log.event_action,audit_log.user_name,audit_log.port,audit_log.client_host,audit_log.service_name,audit_log.statement_string,audit_log.application_name,audit_log.host,audit_log.application_user_name,M_DATABASE.database_name from M_DATABASE,audit_log where M_DATABASE.HOST = audit_log.HOST and audit_policy_name not in ('MandatoryAuditPolicy') and mod(connection_id,2) = 1 and timestamp > :sql_last_value;
    ```

## 5. Configuring the SAP HANA filters in Guardium Insights
To configure this plug-in for Guardium Insights, follow [this guide.](/docs/Guardium%20Insights/3.2.x/UC_Configuration_GI.md)
For the input configuration step, refer to the [Filebeat section](/docs/Guardium%20Insights/3.2.x/UC_Configuration_GI.md#Filebeat-input-plug-in-configuration).
