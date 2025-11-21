## SingleStore-Guardium Logstash filter plug-in
## Meet SingleStore
* Tested versions: 8.7.1
* Environment: On-premise, Iaas
* Supported inputs: Filebeat (push)
* Supported Guardium versions:
	* Guardium Data Protection: 12.2.1 and above

This is a [Logstash](https://github.com/elastic/logstash) filter plug-in for the universal connector that is featured in IBM Security Guardium. It parses events and messages from the SingleStore audit log into a [Guardium record](https://github.com/IBM/universal-connectors/blob/main/common/src/main/java/com/ibm/guardium/universalconnector/commons/structures/Record.java) instance (which is a standard structure made out of several parts). The information is then sent over to Guardium. Guardium records include the accessor (the person who tried to access the data), the session, data, and exceptions. If there are no errors, the data contains details about the query "construct". The construct details the main action (verb) and collections (objects) involved.

Currently, this plug-in will work only with IBM Security Guardium Data Protection, and not Guardium Insights.

The plug-in is free and open-source (Apache 2.0). It can be used as a starting point to develop additional filter plug-ins for Guardium universal connector.

## 1. Enabling the audit logs

### Procedure
1. Enable the audit logs.  
	```text
	sdb-admin update-config --all --key "auditlog_level" --value "ALL-QUERIES-PLAINTEXT"
	```
	
2. Restart the nodes.  
	```text
	sdb-admin restart-node --all
	```

3. Verify if the configuration is saved and enabled.  
	```text
	SHOW GLOBAL VARIABLES LIKE 'audit%';
	```

## 2. Viewing the audit logs configuration 
Use the following command to retrieve the log files that are stored in the auditlogsdir variable.  
   ```text
   SHOW GLOBAL VARIABLES LIKE 'audit%';
   ```

**Note:** If you are using docker deployment, the logs directory (i.e. /var/lib/memsql) must be present outside the container.

## 3. Configuring Filebeat to push logs to Guardium  
1.	To install Filebeat on your system, refer to the [Filebeat quick start: installation and configuration](https://www.elastic.co/guide/en/beats/filebeat/current/filebeat-installation-configuration.html#installation) topic.


2. Configuring Filebeat  
	To use Logstash to process additional data collected by Filebeat, configure Filebeat to use Logstash. To do so, modify the `filebeat.yml` file.  
		**Note:** Search for the `filebeat.yml` file in the filebeat installation directory. You can also refer [Directory layout](https://www.elastic.co/guide/en/beats/filebeat/current/directory-layout.html) to search the `filebeat.yml` file.  
		
	a. Update the filebeat.inputs section with the following parameters.
	```text
	filebeat.inputs:
		- type: filestream   
        - id: <ID>
		enabled: true
		paths :  - /path/to/query.log
		parsers:
		- multiline:
			type: pattern
			pattern: '^\d+,'
			negate: true
			match: after
		tags : ["singlestore"] 
	```
     ```text
    # If filestream is not supported, use the log input as shown below:
    filebeat.inputs:
      type: log
      # Unique ID among all inputs, an ID is required.
      id: <ID>
 
      # Change to true to enable this input configuration.
      enabled: true
      allow_deprecated_use: true
      # Paths that should be crawled and fetched. Glob based paths.
      paths:
        - /singlestoredata/*/auditlogs/*.log
 
      multiline.type: pattern
      multiline.pattern: '^\d+,'
      multiline.negate: true
      multiline.match: after
 
      tags: ["singlestore"]
     ```
	**Note:** Add the tags to uniquely identify the SingleStore events from the rest.  

	b. Configuring the output section.  
		&nbsp;&nbsp;1.In the output section, disable the Elasticsearch output by commenting it out.  
		&nbsp;&nbsp;2. Enable Logstash output by uncommenting the Logstash section.  For more information, see [Configure the Logstash output
](https://www.elastic.co/guide/en/beats/filebeat/current/logstash-output.html#logstash-output).  <br><br>
		**Note:** The hosts option specifies the Logstash server and the port (5001) where Logstash is configured to listen for incoming Beats connections. You can set any port number except 5044, 5141, and 5000 (as these are currently reserved in Guardium v11.3 and v11.4).  
		For example:  
	```text
	output.logstash:  
		hosts: ["<host>:<port>"]  
	```
		
### Limitations  
• Source Program is not part of the SingleStore logs.  
• Client IP can only be retrieved in login / logout actions.  
• Queries with SQL errors are included in the `Full SQL` report and are not displayed in `SQL Errors`.

**Note:** For details on configuring Filebeat connection over SSL, refer [Configuring Filebeat to push logs to Guardium](https://github.com/IBM/universal-connectors/blob/main/input-plugin/logstash-input-beats/README.md#configuring-filebeat-to-push-logs-to-guardium).


## 4. Configuring the SingleStore filters in Guardium

The Guardium universal connector is the Guardium entry point for native audit logs. The universal connector identifies and parses received events, and then converts them to a standard Guardium format. The output of the universal connector is forwarded to the Guardium sniffer on the collector, for policy and auditing enforcements. Configure Guardium to read the native audit logs by customizing the SingleStore template.

### Before you begin

• Configure the [policies](/docs/#policies).

• You must have permission for the S-Tap Management role. The admin user includes this role, by default.

# Procedure

1. On the collector, go to Setup > Tools and Views > Configure Universal Connector.
2. First enable the Universal Guardium connector, if it is disabled already.
3. Click Upload File and select the offline [logstash-filter-singlestoredb_guardium_filter.zip](https://github.com/IBM/universal-connectors/releases/download/v1.7.0/logstash-filter-singlestoredb_guardium_filter.zip) plug-in. After it is uploaded, click OK. This step is not necessary for Guardium Data Protection v11.0p490 or later, v11.0p540 or later, v12.0 or later.
4. Click the Plus sign to open the Connector Configuration dialog box.
5. Type a name in the Connector name field.
6. Update the input section to add the details from the [singlestoreFilebeat.conf](./singleStoreFilebeat.conf) file input section, omitting the keyword "input{" at the beginning and its corresponding "}" at the end.
7. Update the filter section to add the details from the [singlestoreFilebeat.conf](./singleStoreFilebeat.conf)  file filter section, omitting the keyword "filter{" at the beginning and its corresponding "}" at the end.
8. The "type" fields should match in the input and the filter configuration section. This field should be unique for  every individual connector added
9. Click Save. Guardium validates the new connector and displays it in the Configure Universal Connector page.
