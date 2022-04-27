## SAP HANA-Guardium Logstash filter plug-in

This is a [Logstash](https://github.com/elastic/logstash) filter plug-in for the univer***REMOVED***l connector that is featured in IBM Security Guardium. It parses events and mes***REMOVED***ges from the SAP HANA audit log into a Guardium record instance (which is a standard structure made out of several parts). The information is then sent over to Guardium. Guardium records include the accessor (the person who tried to access the data), the session, data, and exceptions. If there are no errors, the data contains details about the query "construct". The construct details the main action (verb) and collections (objects) involved. 

The plug-in is free and open-source (Apache 2.0). It can be used as a starting point to develop additional filter plug-ins for Guardium univer***REMOVED***l connector.

## Installing SAP HANA and configuring auditing

### Installing the SAP HANA database with Docker

There are multiple ways to install a SAP HANA server. For this example, we will assume that we already have a working SAP HANA setup.

### Enabling CSTABLE base auditing logs:
	
	1. In the SAP HANA Studio expand the system on which you would like to enable auditing.
        a) Expand the ‘Security’ folder.
        b) Double click on the ‘Security option’.
        c) Click on the auditing status drop down menu, by default it will be ‘Di***REMOVED***bled’.
        d) Select ‘Enabled’ option.
        e) Click on the deploy button or press F8 to ***REMOVED***ve the changes.
        f) Restart database instance to reflect new changes.

### Enabling CSVTEXTFILE base auditing logs:

	To perform below steps open SAP HANA studio(Eclipse)
	Select SYSTEMDB user ->right-click on it->Select SQL Console then fire below Commands.

    1. For multiple container tenant database you can enable auditing for CSV File target by using below command
        a)  SAP HANA Command For Enable Auditing:-
           ALTER SYSTEM ALTER CONFIGURATION ('global.ini', 'system') set ('auditing configuration', 'global_auditing_state') = 'true';
        b) To select target as CSV text file use below command
           ALTER SYSTEM ALTER CONFIGURATION ('global.ini', 'system') set ('auditing configuration', 'default_audit_trail_type') = 'CSVTEXTFILE';
        c) To avoid unwanted system logs use below command to store all system logs in table.
           ALTER SYSTEM ALTER CONFIGURATION ('global.ini', 'system') set ('auditing configuration', 'critical_audit_trail_type') = 'CSTABLE';
    2. After hit above command restart the container and refresh added systems.  
    
	
### CREATING AN AUDIT POLICY
	
	An audit policy defines the actions to be audited, in order to create an audit policy the user must have AUDIT ADMIN system rights.

## Procedure
   
   1. In the SAP HANA Studio expand the “Database”.
        a) Expand the ‘Security’ folder.
        b) Double click on the ‘Security option’.
    2. Click the green plus sign under the ‘Audit Polices’ panel. 
    3. Enter in your policy name.
    4. Click in the Audited actions field and then press the ‘…’
    5. Select what actions you would like to audit.
    6. Select when an audit record should be created in the ‘Audited actions status’ column.
        a) SUCCESSFUL – When an action is successfully executed it is logged
        b) UNSUCCESSFUL	- When an action is unsuccessfully executed it is logged
        c) ALL	 - Both of the above situations are logged.
    7. Select the audit level:
        a) EMERGENCY
        b) CRITICAL
        c) ALERT
        d) WARNING
        e) INFO (default)
    8. If needed, you can filter the users you would like to audit. Under the users column, you can press the ‘…’ button and the add user. 
    9. You can also specify the target object(s) to be audited. This option is valid if the actions to be audited involve SELECT, INSERT, UPDATE, DELETE.
    10. Once done press the deploy button or press F8
        a) Reboot the database instance for the changes to take place.
           
## Sap Hana Auditing Policy
      
    1. For Audit session related logs (Connect, Disconnect, Validation Of User) select below audited actions.
        a) Select “Connect” checkbox from session management And system configuration menu in audited action tab.
           Note: For this policy select Audited Action Status=Unsuccessful. 
    2. For audit DML logs select below audited action.
        a) Select Data Query and Manipulation checkbox from audited action tab.
           Note: 
        ◦ For this policy select Audited Action Status=Successful. 
        ◦ And it is mandatory to provide “Target Object” for this policy.  

    3. For audit DDL Logs select below audited action.
        a) Select “Create Table, Create Function, Create Procedure, Drop Function, Drop Procedure, Drop Table” checkbox from Data definition menu in audited action table.
           Note: For this policy select Audited Action Status=Successful. 

### View the Sap Hana audit logs for CSTABLE based auditing.

## Procedure
    1. Connect to Database.
    2. Right Click on it. Open SQL console and hit below command.
       Select * from AUDIT_LOG;

### View the Sap Hana audit logs for CSV based auditing. 

## Procedure

    1. To view the audited Logs:-
        a) GOTO Location /usr/***REMOVED***p/host-server/instance/*.csv 
    2. To view the logs entries on Studio
        a) Click on HXE system 
        b) GOTO administrative Console. 
        c) Click on diagnostic file tab.
        d) Click on filter and Search for “.CSV”		
	
## Filebeat configurations:

#### Procedure:

1. To install Filebeat on your system, follow the steps in this topic:
    https://www.elastic.co/guide/en/beats/filebeat/current/filebeat-installation-configuration.html#installation

2. Filebeat configuration:

To use Logstash to perform additional processing on the data collected by Filebeat, we need to configure Filebeat to use Logstash. To do this, modify the filebeat.yml file which you can find inside the folder where filebeat is installed. Follow these instructions for finding the installation directory:
https://www.elastic.co/guide/en/beats/filebeat/current/directory-layout.html

    • Locate "filebeat.inputs" in the filebeat.yml file and then use the "paths" attribute to set the location of the ***REMOVED***phana audit logs:
	For more information, see https://www.elastic.co/guide/en/beats/filebeat/current/configuration-filebeat-options.html#configuration-filebeat-options
       
	For example:-
	   filebeat.inputs:
       - type: log   
       enabled: true
        paths:
       - <host_name/trace/DB_<DB_Name>/*.csv>
	
    • While editing the Filebeat configuration file, di***REMOVED***ble Elasticsearch output by commenting it out. Then enable Logstash output by uncommenting the Logstash section. For more information, see https://www.elastic.co/guide/en/beats/filebeat/current/logstash-output.html#logstash-output
		
	For example:-
       output.logstash:
       hosts: ["127.0.0.1:5001"]

The hosts option specifies the Logstash server and the port (5001) where Logstash is configured to listen for incoming Beats connections. You can set any port number except 5044, 5141, and 5000 (as these are currently reserved as ports for the MongoDB incoming log).
	
	• Locate "Processors" in the filebeat.yml file and then add below attribute to get timezone of Server:
	For more information, see https://www.elastic.co/guide/en/beats/filebeat/current/add-locale.html
	For example:-
       processors:
	   - add_locale: ~

3. To learn how to start FileBeat, see https://www.elastic.co/guide/en/beats/filebeat/current/filebeat-installation-configuration.html#start
	
	
## Configuring the SAP HANA filters in Guardium

The Guardium univer***REMOVED***l connector is the Guardium entry point for native audit logs. The univer***REMOVED***l connector identifies and parses received events, and converts them to a standard Guardium format. The output of the Guardium univer***REMOVED***l connector is forwarded to the Guardium sniffer on the collector, for policy and auditing enforcements. Configure Guardium to read the native audit logs by customizing the SAP HANA template.

## Before you begin

• You must have permission for the S-Tap Management role. The admin user includes this role, by default.
• Download the required (ngdbc)jars as per your database version from URL:- https://tools.hana.ondemand.com/#hanatools


# Procedure
	1. On the collector, go to Setup > Tools and Views > Configure Univer***REMOVED***l Connector.
	2. Click Upload File and select the offline ***REMOVED***pHana-offline-plugin.zip plug-in. After it is uploaded, click OK.
	3. Click Upload File again and select the ngdbc-2.9.12 jar file. After it is uploaded, click OK.   
	4. Click the Plus sign to open the Connector Configuration dialog box.
	5. Type a name in the Connector name field.
	6. Update the input section for JDBC Plugin. Use ***REMOVED***pHANA-JDBC.conf file's input part, omitting the keyword "input{" at the beginning and its corresponding "}" at the end and for FileBeat use filter-test-beats.conf file's input part.
	7. Update the filter section for JDBC Plugin. Use ***REMOVED***pHANA-JDBC.conf file's filter part, omitting the keyword "filter{" at the beginning and its corresponding "}" at the end and for FileBeat use filter-test-beats.conf file's filter part.
	Note: The "type" field should match in the input and filter configuration section. This field should be unique for every individual connector added.
	Note: For moderate to large amounts of data, include pagination to facilitate the audit and to avoid out of memory errors. Use the parameters below in the input section when using a JDBC connector, and remove the concluding semicolon ';' from the jdbc statement :
jdbc_paging_enabled => true
jdbc_page_size => 1000 
	8. Click Save. Guardium validates the new connector, and enables the univer***REMOVED***l connector if it was
	di***REMOVED***bled. After it is validated, it appears in the Configure Univer***REMOVED***l Connector page.
	
	
## Limitations :

	1. SAP Hana auditing only supports error logs for Authentication Failure.
	2. SAP HANA does not audit multiple line query properly.
	3. SAP HANA CSVTEXTFILE(audit target) does not audit the DB_Name.
	4. SAP HANA with JDBC shows server ip as 0.0.0.0

## JDBC Load Balancing Configuration

	In SAP HANA JDBC input plug-in , we distribute load between two machines based on Even and Odd "sessionId"
	
# Procedure

	On First G Machine,in input section for JDBC Plugin update "statement" field like below:

		select audit_log.event_status,audit_log.client_ip,audit_log.connection_id,audit_log.client_port,audit_log.timestamp,audit_log.event_action,audit_log.user_name,audit_log.port,audit_log.client_host,audit_log.service_name,audit_log.statement_string,audit_log.application_name,audit_log.host,audit_log.application_user_name,M_DATABASE.database_name from M_DATABASE,audit_log where M_DATABASE.HOST = audit_log.HOST and audit_policy_name not in ('MandatoryAuditPolicy') and mod(connection_id,2) = 0 and timestamp > :sql_last_value;
		
	On Second G machine ,in input section for JDBC Plugin update "statement" field like below:
	
		select audit_log.event_status,audit_log.client_ip,audit_log.connection_id,audit_log.client_port,audit_log.timestamp,audit_log.event_action,audit_log.user_name,audit_log.port,audit_log.client_host,audit_log.service_name,audit_log.statement_string,audit_log.application_name,audit_log.host,audit_log.application_user_name,M_DATABASE.database_name from M_DATABASE,audit_log where M_DATABASE.HOST = audit_log.HOST and audit_policy_name not in ('MandatoryAuditPolicy') and mod(connection_id,2) = 1 and timestamp > :sql_last_value;
		
		
		
		
		
	
	
