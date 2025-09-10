## 1. Configuring the Fujitsu Enterprise Postgres
For this example, we will assume that we already have a working Fujitsu Enterprise Postgres setup.

## 2. Enabling pgaudit extension logs:

There are different ways of auditing and logging in Fujitsu postgres. For this exercise, we will use pgaudit, the open
source audit logging extension for PostgreSQL 

### Procedure
	1. Open a session to the DB server.
	2. Copy the pgaudit files, by running below command as a superuser. In the command <x> denotes the product version.
	       cp -r /opt/fsepv<x>server64/OSS/pgaudit/* /opt/fsepv<x>server64
	3. Create pgaudit configuration file pgaudit.conf, which describes the information required for pgaudit actions.
	4. Set write permissions for the Database administrator only, so that policies related to the audit log are not viewed by unintended users.
	5. Configure postgresql.conf. Configure the parameters below in postgresql.conf to use audit logs:
		• shared_preload_libraries = pgaudit
		• pgaudit.config_file = Specify the deployment destination path of the pgaudit configuration file
		• log_replication_commands = on
		• log_min_messages = WARNING
	6. Start the Instance
	7. Create the pgaudit extension, using executing below query, by connecting to the DB server through client.
			CREATE EXTENSION pgaudit;
	8. Configure the parameters in pgaudit.conf file
		• [output]
			logger = 'auditlog'
		• [option] 
			role = 'auditor'
		• [rule]
			class = 'WRITE, DDL, ERROR, FUNCTION, ROLE'
	9. Save and close the file.
	10. Restart the DB server so as to get the changes reflected. Command to restart the server  pg_ctl restart -D <dbInstanceName>

## 3. Viewing the audit logs

When logger parameter is set to 'auditLog', one can also add settings for 'log_directory' and 'log_filename', where the log files can be found. If not specified it is in default location <path_of_pgaudit.conf>/pgaudit_log/
	
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
       - type: filestream
       - id: <ID>
       enabled: true
        paths:
       - <directory specified in pgaudit.conf under "log_directory" parameter, if not specified it is pgaudit_log directory>
	
	• To process multi-line audit events, add the settings in the same inputs section.
	
		multiline.type: pattern
		multiline.pattern: 'AUDIT:'
		multiline.negate: true
		multiline.match: after
	
	• Add the tags to uniquely identify the Neo4j events from the rest.
	
		tags: ["guc_postgres_param"]
	
 2. Configuring the output section:
	
	• Locate "output" in the filebeat.yml file, then add the following parameters.
					   
								

    • Disable Elasticsearch output by commenting it out.

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

#### For details on configuring Filebeat connection over SSL, refer [Configuring Filebeat to push logs to Guardium](https://github.com/IBM/universal-connectors/blob/main/input-plugin/logstash-input-beats/README.md#configuring-filebeat-to-push-logs-to-guardium).

	
### Limitations
	• Here, the exact query that caused exception is not logged, so in reports it is set as NA (Not Available)

	
## 5. Configuring the Fujitsu Enterprise Postgres filters in Guardium

The Guardium universal connector is the Guardium entry point for native audit logs. The Guardium universal connector identifies and parses the received events, and converts them to a standard Guardium format. The output of the Guardium universal connector is forwarded to the Guardium sniffer on the collector, for policy and auditing enforcements. Configure Guardium to read the native audit logs by customizing the postgres template.

## Before you begin
•  Configure the policies you require. See [policies](/docs/#policies) for more information.

• You must have permission for the S-Tap Management role. The admin user includes this role, by default.
					 
• This plug-in is automatically available with Guardium Data Protection versions 12.x, 11.4 with appliance bundle 11.0p490 or later or Guardium Data Protection version 11.5 with appliance bundle 11.0p540 or later releases.

**Note**: For Guardium Data Protection version 11.4 without appliance bundle 11.0p490 or prior or Guardium Data Protection version 11.5 without appliance bundle 11.0p540 or prior, download the [postgres-offline-plugins-7.5.2.zip](https://github.com/IBM/universal-connectors/raw/release-v1.2.0/filter-plugin/logstash-filter-onPremPostgres-guardium/PostgresOverFilebeatPackage/postgres-offline-plugins-7.5.2.zip) plug-in. (Do not unzip the offline-package file throughout the procedure). 


# Procedure
1. On the collector, go to Setup > Tools and Views > Configure Universal Connector.
2. First enable the Universal Guardium connector, if it is disabled already.
3. Click Upload File and select the offline [postgres-offline-plugins-7.5.2.zip](https://github.com/IBM/universal-connectors/raw/release-v1.2.0/filter-plugin/logstash-filter-onPremPostgres-guardium/PostgresOverFilebeatPackage/postgres-offline-plugins-7.5.2.zip)  plug-in. After it is uploaded, click OK. This step is not necessary for Guardium Data Protection v11.0p490 or later, v11.0p540 or later, v12.0 or later.
4. Click the Plus sign to open the Connector Configuration dialog box.
5. Type a name in the Connector name field.
6. Update the input section to add the details from the [PostgresFilebeat.conf](https://github.com/IBM/universal-connectors/raw/main/filter-plugin/logstash-filter-onPremPostgres-guardium/PostgresFilebeat.conf) file's input part, omitting the keyword "input{" at the beginning and its corresponding "}" at the end.
7. Update the filter section to add the details from the [PostgresFilebeat.conf](https://github.com/IBM/universal-connectors/raw/main/filter-plugin/logstash-filter-onPremPostgres-guardium/PostgresFilebeat.conf) file's filter part, omitting the keyword "filter{" at the beginning and its corresponding "}" at the end.
8. The "type" fields should match in the input and the filter configuration section. This field should be unique for  every individual connector added.
9. The tag added in the filebeat.yml file should match the "[tags]" specified in the filter part.
10. Click Save. Guardium validates the new connector, and enables the universal connector if it was	disabled. After it is validated, it appears in the Configure Universal Connector page.
