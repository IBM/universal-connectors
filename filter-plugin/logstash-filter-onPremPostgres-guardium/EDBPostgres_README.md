#  EDB Postgres
	
## Enabling audit logs:

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
	

## Viewing the audit logs

The audit logs can be viewed in the location that is specified by the parameter "edb_audit_directory" in the file name that is specified by "edb_audit_filename".
	
## Configuring Filebeat to push logs to Guardium

### Filebeat installation

#### Procedure:

To install Filebeat on your system, follow the steps in this topic:
    https://www.elastic.co/guide/en/beats/filebeat/current/filebeat-installation-configuration.html#installation

### Filebeat configuration:

To use Logstash to perform additional processing on the data collected by Filebeat, we need to configure Filebeat to use Logstash. To do this, modify the filebeat.yml file which you can find inside the folder where filebeat is installed. Follow these instructions for finding the installation directory:
https://www.elastic.co/guide/en/beats/filebeat/current/directory-layout.html

#### Procedure:

1. Configuring the input section :-

	• Locate "filebeat.inputs" in the filebeat.yml file, then add the following parameters.
       
		For example:-
		filebeat.inputs:
    	- type: filestream
        - id: <ID>
    	enabled: true
    	paths: - <directory specified in postgresql.conf under "edb_audit_directory" parameter/audit*.csv>

		Note : In the above path, ensure that the name of the .csv matches the filename specified for the "edb_audit_filename" parameter in the postgresql.conf file.
	
	• To process multi-line audit events, add the settings in the same inputs section.
	
		multiline.type: pattern
		multiline.pattern: '^[0-9]{4}-[0-9]{2}-[0-9]{2}'
		multiline.negate: true
		multiline.match: after
	
	• Add the tags to uniquely identify the EDB Postgres events from the rest.
	
		tags: ["guc_postgres_param"]
	
   2. Configuring the output section:

       • Locate "Outputs" in the filebeat.yml file.

       • Disable "Elasticsearch output" by commenting it out.

       • Enable "Logstash Output" by uncommenting the Logstash section.
      	
  		 For more information, see https://www.elastic.co/guide/en/beats/filebeat/current/logstash-output.html#logstash-output

              For example:-
              output.logstash:
              hosts: ["<host>:<port>"]
          • The hosts option specifies the Logstash server and the port where Logstash is configured to listen for incoming Beats connections.

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


## Configuring the EDB Postgres filters in Guardium

The Guardium universal connector is the Guardium entry point for native audit logs. The Guardium universal connector identifies and parses the received events, and converts them to a standard Guardium format. The output of the Guardium universal connector is forwarded to the Guardium sniffer on the collector, for policy and auditing enforcements. Configure Guardium to read the native audit logs by customizing the postgres template.

###  Before you begin

•  Configure the policies you require. See [policies](/docs/#policies) for more information.

• You must have permission for the S-Tap Management role. The admin user includes this role, by default.

• This plug-in is automatically available with Guardium Data Protection versions 12.x, 11.4 with appliance bundle 11.0p490 or later or Guardium Data Protection version 11.5 with appliance bundle 11.0p540 or later releases.

**Note**: For Guardium Data Protection version 11.4 without appliance bundle 11.0p490 or prior or Guardium Data Protection version 11.5 without appliance bundle 11.0p540 or prior, download the [postgres-offline-plugins-7.5.2.zip](https://github.com/IBM/universal-connectors/raw/release-v1.2.0/filter-plugin/logstash-filter-onPremPostgres-guardium/PostgresOverFilebeatPackage/postgres-offline-plugins-7.5.2.zip) plug-in. (Do not unzip the offline-package file throughout the procedure). 

### Procedure
1. On the collector, go to Setup > Tools and Views > Configure Universal Connector.
2. Enable the universal connector if it is disabled.
3. Click Upload File and select the offline  [postgres-offline-plugins-7.5.2.zip](https://github.com/IBM/universal-connectors/raw/release-v1.2.0/filter-plugin/logstash-filter-onPremPostgres-guardium/PostgresOverFilebeatPackage/postgres-offline-plugins-7.5.2.zip)  plug-in. After it is uploaded, click OK. This step is not necessary for Guardium Data Protection v11.0p490 or later, v11.0p540 or later, v12.0 or later.
4. Click the Plus sign to open the Connector Configuration dialog box.
5. Type a name in the Connector name field.
6. Update the input section to add the details from the [PostgresFilebeat.conf](https://github.com/IBM/universal-connectors/raw/main/filter-plugin/logstash-filter-onPremPostgres-guardium/PostgresFilebeat.conf) file's input part, omitting the keyword "input{" at the beginning and its corresponding "}" at the end.
7. Update the filter section to add the details from the [PostgresFilebeat.conf](https://github.com/IBM/universal-connectors/raw/main/filter-plugin/logstash-filter-onPremPostgres-guardium/PostgresFilebeat.conf) file's filter part, omitting the keyword "filter{" at the beginning and its corresponding "}" at the end.
8. The "type" fields should match in the input and the filter configuration section. This field should be unique for  every individual connector added.
9. The tag added in the filebeat.yml file should match the "[tags]" specified in the filter part.
10. Click Save. Guardium validates the new connector and displays it in the Configure Universal Connector page.
