# SingleStore-Guardium Logstash filter plug-in
### Meet SingleStore
* Tested versions: 8.7.1
* Environment: On-premise, Iaas
* Supported inputs: Filebeat (push)
* Supported Guardium versions:
	* Guardium Data Protection: 12 and above

This is a [Logstash](https://github.com/elastic/logstash) filter plug-in for the universal connector that is featured in IBM Security Guardium. It parses events and messages from the SingleStore audit log into a [Guardium record](https://github.com/IBM/universal-connectors/blob/main/common/src/main/java/com/ibm/guardium/universalconnector/commons/structures/Record.java) instance (which is a standard structure made out of several parts). The information is then sent over to Guardium. Guardium records include the accessor (the person who tried to access the data), the session, data, and exceptions. If there are no errors, the data contains details about the query "construct". The construct details the main action (verb) and collections (objects) involved.

Currently, this plug-in will work only with IBM Security Guardium Data Protection, and not Guardium Insights.

The plug-in is free and open-source (Apache 2.0). It can be used as a starting point to develop additional filter plug-ins for Guardium universal connector.



## Enabling the audit logs:

### Procedure
	Start enabling audit logs in SingleStore
		sdb-admin update-config --all --key "auditlog_level" --value "ALL-QUERIES-PLAINTEXT"
	
	Restart the nodes
		sdb-admin restart-node --all

	Check is configuration is saved and enabled
		SHOW GLOBAL VARIABLES LIKE 'audit%';

## 3. Viewing the audit logs configuration 

    logs file are stored in auditlogsdir variable that can be retrieved using the following command
		SHOW GLOBAL VARIABLES LIKE 'audit%';

	If using docker deployment, the logs directory need to be persisted outside the container

## 4. Configuring Filebeat to push logs to Guardium

## a. Filebeat installation

### Procedure:

To install Filebeat on your system, follow the steps in this topic:
    https://www.elastic.co/guide/en/beats/filebeat/current/filebeat-installation-configuration.html#installation

## b. Filebeat configuration:

To use Logstash to perform additional processing on the data collected by Filebeat, we need to configure Filebeat to use Logstash. To do this, modify the filebeat.yml file which you can find inside the folder where filebeat is installed. Follow these instructions for finding the installation directory: https://www.elastic.co/guide/en/beats/filebeat/current/directory-layout.html

### Procedure:

1. Configuring the input section :-

	• Locate "filebeat.inputs" in the filebeat.yml file, then add the following parameters.

		filebeat.inputs:
		- type: log
          enabled: true
		  paths :  - /path/to/query.log
		  parsers:
  		  - multiline:
              type: pattern
              pattern: '^\d+,'
              negate: true
              match: after

	• Add the tags to uniquely identify the SingleStore events from the rest.

		tags : ["SingleStore"]

2. Configuring the output section:

		• Locate "output" in the filebeat.yml file, then add the following parameters.

		• Disable Elasticsearch output by commenting it out.

		• Enable Logstash output by uncommenting the Logstash section. For more information, see https://www.elastic.co/guide/en/beats/filebeat/current/logstash-output.html#logstash-output

		• For example:

				output.logstash:
					hosts: ["<host>:<port>"]
		• The hosts option specifies the Logstash server and the port (5001) where Logstash is configured to listen for incoming Beats connections.

		•You can set any port number except 5044, 5141, and 5000 (as these are currently reserved in Guardium v11.3 and v11.4 ).
### Limitations

	• Source Program is not part of the SingleStore logs
	• Client IP can only be retrieved in login / logout actions 
	• SQL Errors are not logged by SingleStore		   


#### For details on configuring Filebeat connection over SSL, refer [Configuring Filebeat to push logs to Guardium](https://github.com/IBM/universal-connectors/blob/main/input-plugin/logstash-input-beats/README.md#configuring-filebeat-to-push-logs-to-guardium).


## 5. Configuring the SingleStore filters in Guardium

The Guardium universal connector is the Guardium entry point for native audit logs. The universal connector identifies and parses received events, and then converts them to a standard Guardium format. The output of the universal connector is forwarded to the Guardium sniffer on the collector, for policy and auditing enforcements. Configure Guardium to read the native audit logs by customizing the SingleStore template.

### Before you begin

• Configure the policies you require. See [policies](/docs/#policies) for more information.

• You must have permission for the S-Tap Management role. The admin user includes this role, by default.

# Procedure

1. On the collector, go to Setup > Tools and Views > Configure Universal Connector.
2. First enable the Universal Guardium connector, if it is disabled already.
3. Click Upload File and select the offline [logstash-filter-singlestore_guardium_filter.zip](https://github.com/IBM/universal-connectors/releases/download/v1.5.1/logstash-filter-neodb_guardium_filter.zip) plug-in. After it is uploaded, click OK. This step is not necessary for Guardium Data Protection v11.0p490 or later, v11.0p540 or later, v12.0 or later.
4. Click the Plus sign to open the Connector Configuration dialog box.
5. Type a name in the Connector name field.
6. Update the input section to add the details from the [singlestoreFilebeat.conf](https://github.com/IBM/universal-connectors/raw/main/filter-plugin/logstash-filter-SingleStore-guardium/SingleStoreFilebeat.conf) file input section, omitting the keyword "input{" at the beginning and its corresponding "}" at the end.
7. Update the filter section to add the details from the [singlestoreFilebeat.conf](https://github.com/IBM/universal-connectors/raw/main/filter-plugin/logstash-filter-SingleStore-guardium/SingleStoreFilebeat.conf)  file filter section, omitting the keyword "filter{" at the beginning and its corresponding "}" at the end.
8. The "type" fields should match in the input and the filter configuration section. This field should be unique for  every individual connector added
9. Click Save. Guardium validates the new connector and displays it in the Configure Universal Connector page.