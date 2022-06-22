## SAP HANA-Guardium Logstash filter plug-in

This is a [Logstash](https://github.com/elastic/logstash) filter plug-in for the universal connector that is featured in IBM Security Guardium. It parses events and messages from the SAP HANA audit log into a Guardium record instance (which is a standard structure made out of several parts). The information is then sent over to Guardium. Guardium records include the accessor (the person who tried to access the data), the session, data, and exceptions. If there are no errors, the data contains details about the query "construct". The construct details the main action (verb) and collections (objects) involved.

The plug-in is free and open-source (Apache 2.0). It can be used as a starting point to develop additional filter plug-ins for Guardium universal connector.

## Installing SAP HANA and configuring auditing

There are multiple ways to install a SAP HANA server. For this example, we will assume that we already have a working SAP HANA setup.

## Auditing

There are multiple ways to enable auditing in SAP HANA, You can choose as per your requirement.
* CSTABLE base auditing :- Audit-trail target is a table, requires JDBC input plug-in.
* CSVTEXTFILE base auditing :- Audit-trail target is a file, requires Beat input plugin.

### Enabling CSTABLE base auditing logs:

1. In the SAP HANA Studio, expand the system on which you would like to enable auditing.

	 a) Expand the Security folder.

	 b) Double click on the ‘Security option’.

	c) Click on the auditing status drop-down menu, by default it will be disabled.

	d) Select "Enabled".

  e) Click "Deploy" or press F8 to save the changes.

      f) Restart database instance to reflect new changes.

### Enabling CSVTEXTFILE base auditing logs:

To perform the below steps, open SAP HANA studio (Eclipse)
	Select SYSTEMDB user, right-click,  select SQL Console, and then run these Commands:

  1. For multiple container tenant database you can enable auditing for CSV File target by using the following command

		a)  SAP HANA Command For Enable Auditing:-

			 ALTER SYSTEM ALTER CONFIGURATION ('global.ini', 'system') set ('auditing configuration', 'global_auditing_state') = 'true';
      b) To select the target as a CSV text file, use the following command
           ALTER SYSTEM ALTER CONFIGURATION ('global.ini', 'system') set ('auditing configuration', 'default_audit_trail_type') = 'CSVTEXTFILE';
        c) To avoid unwanted system logs use below command to store all system logs in table.
           ALTER SYSTEM ALTER CONFIGURATION ('global.ini', 'system') set ('auditing configuration', 'critical_audit_trail_type') = 'CSTABLE';
    2. After running the previous command, restart the container and refresh the added systems.


## Common steps for either auditing type

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
   7. Select the audit level:

		a) EMERGENCY

		b) CRITICAL

		c) ALERT

		d) WARNING

		e) INFO (default)

   8. If needed, you can filter the users you would like to audit. Under the users column, you can press the ‘…’ button and then add users.

   9. You can also specify the target object(s) to be audited. This option is valid if the actions to be audited involve SELECT, INSERT, UPDATE, DELETE.

10. Once done press the deploy button or press F8

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



## VIEWING AUDIT LOGS

### View the SAP HANA audit logs for CSTABLE-based auditing.

#### Procedure
  1. Connect to the database.
  2. Right-click on it.
  3. Open the SQL console and run the following command:

         Select * from AUDIT_LOG;

### View the SAP HANA audit logs for CSVTEXTFILE-based auditing.

#### Procedure

  1. To view the audited Logs:-

        a) GOTO Location /usr/sap/host-server/instance/*.audit_trail.csv

  2. To view the logs entries on Studio:

        a) Click on HXE system.

        b) GOTO administrative Console.

        c) Click on the diagnostic file tab.

        d) Click on filter and Search for “*.audit_trail.csv”

## Filebeat configurations:

This configuration is required to pull logs when CSVTEXTFILE-based auditing is enabled.

#### Procedure:

1. To install Filebeat on your system, follow the steps in this topic:
    https://www.elastic.co/guide/en/beats/filebeat/current/filebeat-installation-configuration.html#installation

2. Filebeat configuration:

 a) To use Logstash to perform additional processing on the data collected by Filebeat, we need to configure Filebeat to use Logstash. To do this, modify the filebeat.yml file which you can find inside the folder where Filebeat is installed. Follow these instructions for finding the installation directory:
https://www.elastic.co/guide/en/beats/filebeat/current/directory-layout.html

  b) Locate "filebeat.inputs" in the filebeat.yml file and then use the "paths" attribute to set the location of the SAP HANA audit logs:
	For more information, see https://www.elastic.co/guide/en/beats/filebeat/current/configuration-filebeat-options.html#configuration-filebeat-options

For example:-

	   filebeat.inputs:
       - type: log
       enabled: true
        paths:
       - <host_name/trace/DB_<DB_Name>/*.audit_trail.csv>


c) While editing the Filebeat configuration file, disable Elasticsearch output by commenting it out. Then enable Logstash output by uncommenting the Logstash section. For more information, see https://www.elastic.co/guide/en/beats/filebeat/current/logstash-output.html#logstash-output

For example:-

        output.logstash:
        hosts: ["127.0.0.1:5001"]

d) The hosts option specifies the Logstash server and the port (5001) where Logstash is configured to listen for incoming Beats connections. You can set any port number except 5044, 5141, and 5000 (as these are currently reserved as ports for the MongoDB incoming log).

e) Locate "Processors" in the filebeat.yml file and then add the below attribute to get the server's time zone.For more information, see https://www.elastic.co/guide/en/beats/filebeat/current/add-locale.html

For example:-

       processors:
	   - add_locale: ~

3. To learn how to start FileBeat, see https://www.elastic.co/guide/en/beats/filebeat/current/filebeat-installation-configuration.html#start


## Configuring the SAP HANA filters in Guardium

The Guardium universal connector is the Guardium entry point for native audit logs. The universal connector identifies and parses received events, and converts them to a standard Guardium format. The output of the Guardium universal connector is forwarded to the Guardium sniffer on the collector, for policy and auditing enforcements. Configure Guardium to read the native audit logs by customizing the SAP HANA template.

## Before you begin

* You must have permissions for the S-Tap Management role. The admin user includes this role, by default.
* Download the required (ngdbc)jars as per your database version from URL https://tools.hana.ondemand.com/#hanatools .
* For CSVTEXTFILE-based auditing, refer to this package:

	../filter-plugin/logstash-filter-saphana-guardium/SaphanaOverFilebeatPackage/SAPHANA/

   and download the  SAPHANA-OFFLINE-PACK.zip plug-in.
* For CSTABLE based auditing , refer to this package:

 ../filter-plugin/logstash-filter-saphana-guardium/SaphanaOverJdbcPackage/

	and download the SAPHANA-OFFLINE-PACK.zip plug-in.



# Procedure
1. On the collector, go to Setup > Tools and Views > Configure Universal Connector.
2. First Enable the Universal Guardium connector, if it is Disabled already.
3. For CSVTEXTFILE-based auditing, follow these steps:-

	a) Click "Upload File" and select the SAPHANA-OFFLINE-PACK.zip plug-in as per specific audit. After it is uploaded, click "OK".

	b) Click the Plus sign to open the Connector Configuration dialog box.

	c) Type a name in the Connector name field.

	d) Update the input section. Use filter-test-beats.conf file's input part, omitting the keyword "input{" at the beginning and its corresponding "}" at the end.

	e) Update the filter section. Use filter-test-beats.conf file's filter part, omitting the keyword "filter{" at the beginning and its corresponding "}" at the end.

	f) The "type" field should match in the input and filter configuration section. This field should be unique for every individual connector added.

	g) Click "Save". Guardium validates the new connector, and enables the universal connector if it was disabled. After it is validated, it appears in the Configure Universal Connector page.

4. For CSTABLE-based auditing, follow these steps:

	a) Click "Upload File" and select the offline SAPHANA-OFFLINE-PACK.zip plug-in as per specific audit. After it is uploaded, click "OK".

	b) Click "Upload File" again and select the ngdbc-2.9.12 jar file. After it is uploaded, click "OK".

	c) Click the Plus sign to open the Connector Configuration dialog box.

	d) Type a name in the Connector name field.

	e) Update the input section . Use sapHANA-JDBC.conf file's input part, omitting the keyword "input{" at the beginning and its corresponding "}" at the end.

	f) Update the filter section for JDBC Plugin. Use sapHANA-JDBC.conf file's filter part, omitting the keyword "filter{" at the beginning and its corresponding "}" at the end.

	g) The "type" field should match in the input and filter configuration section. This field should be unique for every individual connector added.

	h) If using two jdbc plug-ins on same machine , the last_run_metadata_path file name should be different.

	**Note: For moderate to large amounts of data, include pagination to facilitate the audit and to avoid out of memory errors.  Use the parameters below in the input section when using a JDBC connector, and remove the concluding semicolon ';' from the jdbc statement:**
			jdbc_paging_enabled => true
			jdbc_page_size => 1000

	I) Click Save. Guardium validates the new connector, and enables the universal connector if it was
	disabled. After it is validated, it appears in the Configure Universal Connector page.

## Configuring the SAP HANA over Filebeat in Guardium Insights

To configure this plug-in for Guardium Insights, follow [this guide.](https://github.com/RefaelAdi/universal-connectors/blob/INS-18044/docs/UC_Configuration_GI.md#Configuring_Filebeat_to_forward_audit_logs_to_Guardium)

In the input configuration section, refer to the Filebeat section.

## Limitations :

1. SAP HANA auditing only supports error logs for authentication failures.
2. SAP HANA does not audit multiple line query properly.
3. SAP HANA CSVTEXTFILE(audit target) does not audit the DB_Name.
4. SAP HANA with JDBC shows server ip as 0.0.0.0

## JDBC Load Balancing Configuration

In SAP HANA JDBC input plug-ins , we distribute load between two machines based on even and odd "sessionIds"

### Procedure

1. On the first G Machine, in the input section for JDBC Plug-in, update the "statement" field as follows:

		select audit_log.event_status,audit_log.client_ip,audit_log.connection_id,audit_log.client_port,audit_log.timestamp,audit_log.event_action,audit_log.user_name,audit_log.port,audit_log.client_host,audit_log.service_name,audit_log.statement_string,audit_log.application_name,audit_log.host,audit_log.application_user_name,M_DATABASE.database_name from M_DATABASE,audit_log where M_DATABASE.HOST = audit_log.HOST and audit_policy_name not in ('MandatoryAuditPolicy') and mod(connection_id,2) = 0 and timestamp > :sql_last_value;

2. On the second G machine, in the input section for the JDBC Plug-in, update the  "statement" field as follows:

		select audit_log.event_status,audit_log.client_ip,audit_log.connection_id,audit_log.client_port,audit_log.timestamp,audit_log.event_action,audit_log.user_name,audit_log.port,audit_log.client_host,audit_log.service_name,audit_log.statement_string,audit_log.application_name,audit_log.host,audit_log.application_user_name,M_DATABASE.database_name from M_DATABASE,audit_log where M_DATABASE.HOST = audit_log.HOST and audit_policy_name not in ('MandatoryAuditPolicy') and mod(connection_id,2) = 1 and timestamp > :sql_last_value;