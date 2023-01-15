## 1. Configuring the the EDB Postgres on Linux environment

### Procedure:
	1. Go to https://www.enterprisedb.com/user/login and create an EDB Account.
	2. Go to https://www.enterprisedb.com/software-downloads-postgres, to generate credentials for accessing the repository
	3. In the EDB Postgres Advanced Server section, select the appropriate version of the database
	4. Depending upon the server requirement, click on the Access repo link of the required OS. Here we have used Linux x86-64
    RPM.
	5. To access this repo, sign in using the EDB account created in Step 1.
	6. Generate a password for the repo by clicking on generate password.
	7. Get the actual password by clicking on reveal password and copy the ***REMOVED***me.
	8. Follow the steps given in https://www.enterprisedb.com/docs/epas/latest/epas_qs_linux_7/ to create a EDB Postgres Advanced Server.
	
## 2. Enabling audit logs:

There are different ways of auditing and logging in EDB postgres. For this exercise, we will use edb_audit, the open
source audit logging extension for PostgreSQL 

### Procedure
	1. Open a session to the DB server.
	2. Edit the postgresql.conf
	3. In the edb_audit section, add the required parameters. The details of all the parameters can be found https://www.enterprisedb.com/docs/epas/latest/epas_guide/03_database_administration/01_configuration_parameters/03_configuration_parameters_by_functionality/07_auditing_settings/
	4. The ones tried in this excercise includes below ones
		• edb_audit = 'csv', mandatory one since UC code works on this setting.
		• edb_audit_directory = '/var/lib/edb_audit' ,can be set as per the requirement but should be specified in filebeat.yml
		• edb_audit_filename = 'audit-%Y-%m-%d_%H%M%S' ,can be set as per the requirement
		• edb_audit_connect = 'failed'
		• edb_audit_statement = 'ddl, dml, error', can be set as per the requirement
		• edb_audit_destination = 'file'
	5. Save and close the file.
	6. Restart the DB server so as to get the changes reflected.
	

## 3. Viewing the audit logs

The audit logs can be viewed in the location that is specified by the parameter "edb_audit_directory" in the file name that is specified by "edb_audit_filename".
	
## 4. Configuring Filebeat to push logs to Guardium

## a. Filebeat installation

### Procedure:

To install Filebeat on your system, follow the steps in this topic:
    https://www.elastic.co/guide/en/beats/filebeat/current/filebeat-installation-configuration.html#installation

## b. Filebeat configuration:

To use Logstash to perform additional processing on the data collected by Filebeat, we need to configure Filebeat to use Logstash. To do this, modify the filebeat.yml file which you can find inside the folder where filebeat is installed. Follow these instructions for finding the installation directory:
https://www.elastic.co/guide/en/beats/filebeat/current/directory-layout.html

### Procedure:

1. Configuring the input section :-

	• Locate "filebeat.inputs" in the filebeat.yml file, then add the following parameters.
       
	For example:-
	   filebeat.inputs:
       - type: log   
       enabled: true
        paths:
       - <directory specified in postgresql.conf under "edb_audit_directory" parameter/*.csv>
	
	• To process multi-line audit events, add the settings in the ***REMOVED***me inputs section.
	
		multiline.type: pattern
		multiline.pattern: '^[0-9]{4}-[0-9]{2}-[0-9]{2}'
		multiline.negate: true
		multiline.match: after
	
	• Add the tags to uniquely identify the Neo4j events from the rest.
	
		tags: ["guc_postgres_param"]
	
2. Configuring the output section:

	• Locate "output" in the filebeat.yml file, then add the following parameters.

    • Di***REMOVED***ble Elasticsearch output by commenting it out.

	• Enable Logstash output by uncommenting the Logstash section. For more information, see https://www.elastic.co/guide/en/beats/filebeat/current/logstash-output.html#logstash-output

	• For example:

		output.logstash:
		hosts: ["<host>:<port>"]
	• The hosts option specifies the Logstash server and the port (5001) where Logstash is configured to listen for incoming Beats connections.

	• You can set any port number except 5044, 5141, and 5000 (as these are currently reserved in Guardium v11.3 and v11.4 ).

	• Locate "Processors" in the filebeat.yml file and then add below attribute to get timezone of Server:
	For more information, see https://www.elastic.co/guide/en/beats/filebeat/current/add-locale.html
	For example:-
       processors:
		- add_locale: ~
		- add_host_metadata:
			when.not.contains.tags: forwarded
		- add_cloud_metadata: ~
		- add_docker_metadata: ~
		- add_kubernetes_metadata: ~


3. To learn how to start FileBeat, see https://www.elastic.co/guide/en/beats/filebeat/current/filebeat-installation-configuration.html#start

## Configuring the EDB Postgres filters in Guardium

The Guardium univer***REMOVED***l connector is the Guardium entry point for native audit logs. The Guardium univer***REMOVED***l connector identifies and parses the received events, and converts them to a standard Guardium format. The output of the Guardium univer***REMOVED***l connector is forwarded to the Guardium sniffer on the collector, for policy and auditing enforcements. Configure Guardium to read the native audit logs by customizing the postgres template.

###  Before you begin

•  You must have the log full details policy enabled on the collector. The detailed steps can be found in step 4 under the section about Installing and testing the filter or input plug-in on a staging Guardium system on [this page](https://github.com/IBM/univer***REMOVED***l-connectors/blob/main/docs/developing_plugins_gdp.md).

• You must have permission for the S-Tap Management role. The admin user includes this role, by default.

• Download the [postgres-offline-plugins-7.5.2.zip](https://github.com/IBM/univer***REMOVED***l-connectors/blob/main/filter-plugin/logstash-filter-onPremPostgres-guardium/PostgresOverFilebeatPackage/postgres-offline-plugins-7.5.2.zip) plug-in

### Procedure
1. On the collector, go to Setup > Tools and Views > Configure Univer***REMOVED***l Connector.
2. First enable the Univer***REMOVED***l Guardium connector, if it is di***REMOVED***bled already.
3. Click Upload File and select the offline  [postgres-offline-plugins-7.5.2.zip](https://github.com/IBM/univer***REMOVED***l-connectors/blob/main/filter-plugin/logstash-filter-onPremPostgres-guardium/PostgresOverFilebeatPackage/postgres-offline-plugins-7.5.2.zip)  plug-in. After it is uploaded, click OK.
4. Click the Plus sign to open the Connector Configuration dialog box.
5. Type a name in the Connector name field.
6. Update the input section to add the details from the [PostgresFilebeat.conf](https://github.com/IBM/univer***REMOVED***l-connectors/blob/main/filter-plugin/logstash-filter-onPremPostgres-guardium/PostgresFilebeat.conf) file's input part, omitting the keyword "input{" at the beginning and its corresponding "}" at the end.
7. Update the filter section to add the details from the [PostgresFilebeat.conf](https://github.com/IBM/univer***REMOVED***l-connectors/blob/main/filter-plugin/logstash-filter-onPremPostgres-guardium/PostgresFilebeat.conf) file's filter part, omitting the keyword "filter{" at the beginning and its corresponding "}" at the end.
8. The "type" fields should match in the input and the filter configuration section. This field should be unique for  every individual connector added.
9. The tag added in the filebeat.yml file should match the "[tags]" specified in the filter part.
10. Click Save. Guardium validates the new connector, and enables the univer***REMOVED***l connector if it was	di***REMOVED***bled. After it is validated, it appears in the Configure Univer***REMOVED***l Connector page.
