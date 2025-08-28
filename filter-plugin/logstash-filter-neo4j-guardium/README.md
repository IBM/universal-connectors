# Neo4j-Guardium Logstash filter plug-in
### Meet Neo4j
* Tested versions: 4.4.3, 5.26.0
* Environment: On-premise, Iaas
* Supported inputs: Filebeat (push)
* Supported Guardium versions:
   * Guardium Data Protection: 11.4 and above
   * Guardium Data Security Center: SaaS 1.0

This is a [Logstash](https://github.com/elastic/logstash) filter plug-in for the universal connector that is featured in IBM Security Guardium. It parses events and messages from the Neo4j audit log into a [Guardium record](https://github.com/IBM/universal-connectors/blob/main/common/src/main/java/com/ibm/guardium/universalconnector/commons/structures/Record.java) instance (which is a standard structure made out of several parts). The information is then sent over to Guardium. Guardium records include the accessor (the person who tried to access the data), the session, data, and exceptions. If there are no errors, the data contains details about the query "construct". The construct details the main action (verb) and collections (objects) involved.

Currently, this plug-in will work only with IBM Security Guardium Data Protection, and not Guardium Insights.

The plug-in is free and open-source (Apache 2.0). It can be used as a starting point to develop additional filter plug-ins for Guardium universal connector.



## Enabling the audit logs:

### Procedure

	1. Neo4j supports the below log types:
		a. Debug.log: Information useful when debugging problems with Neo4j.
		b. neo4j.log: The standard log, where general information about Neo4j is written.
		c. query.log: Log of executed queries that takes longer than a specified threshold.
		d. security.log: Log of security events.
		e. http.log: Request log for the HTTP API.
		f. gc.log: Garbage collection logging provided by the Java Virtual Machine (JVM).
		g. service-error.log: (Windows) Log of errors encountered when installing or running the Windows service.
	2. These instructions will use query.log for the Guardium filter, as it contains all database-related log events.
	3. Log configuration: Create this configuration in the neo4j.conf file located at {NeoInstallationDir}/neo4jDatabases/{DB_Name}/installation-4.1.0/conf:[For version 4.x and below]
		a. Provide database name:
				dbms.default_database=<DB_Name>
		b. Enable audit logs:
				dbms.logs.query.enabled=true
		c. Include parameters for the executed queries being logged:
				dbms.logs.query.parameter_logging_enabled=true
		d. Include detailed time information for the executed queries being logged:
				dbms.logs.query.time_logging_enabled=true
		e. Include bytes allocated by the executed queries being logged:
				dbms.logs.query.allocation_logging_enabled=true
		f. Include page hit and page fault information for the executed queries being logged:
				dbms.logs.query.page_logging_enabled=true
	4. Cleanup for log files: You can set a maximum number of history files to be kept on the system for the query log. To enable this, include this configuration in the neo4j.conf file:[For version 4.x and below]
				dbms.logs.query.rotation.keep_number=7
    5. Log configuration: Create this configuration in the neo4j.conf file located at {NeoInstallationDir}/neo4jDatabases/{DB_Name}/installation-4.1.0/conf:[For version 5.x and above]
         a. Provide database name:
                dbms.default_database=<DB_Name>

## Viewing the audit logs

    To view logs, go to the Neo4j console and select DB > manage > Open Folder > logs.

## Configuring Filebeat to push logs to Guardium

## Filebeat installation

### Procedure:

To install Filebeat on your system, follow the steps in this topic:
https://www.elastic.co/guide/en/beats/filebeat/current/filebeat-installation-configuration.html#installation

## Filebeat configuration:

To use Logstash to perform additional processing on the data collected by Filebeat, we need to configure Filebeat to use Logstash. To do this, modify the filebeat.yml file which you can find inside the folder where filebeat is installed. Follow these instructions for finding the installation directory: https://www.elastic.co/guide/en/beats/filebeat/current/directory-layout.html

### Procedure:

1. Configuring the input section:

   • Locate "filebeat.inputs" in the filebeat.yml file, then add the following parameters.

        filebeat.inputs:
        - type: log
        enabled: true
        paths :  - /path/to/query.log

   • To process multi-line audit events, add the settings in same inputs section.

   	    multiline.type: pattern
   	    multiline.pattern: '^[0-9]{4}-[0-9]{2}-[0-9]{2}'
   	    multiline.negate: true
   	    multiline.match: after

   • Add the tags to uniquely identify the Neo4j events from the rest.

   	    tags : ["Neo4j"]

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

   • For example:

       processors:
           add_locale: ~

### Limitations

1. Queries containing a semi-colon in a batch query, causes skipping of the entire batch query
2. If your Guardium version is 11.4 and below, the port should not be 5000, 5141 or 5044, as Guardium Universal Connector reserves these ports for MongoDB events. To check the available ports on Windows, issue this command:

       stat -a

3. Neo4j logs some queries multiple times, since they are executed in pipeline, so the same will be reflected in the Reports
4. Multiple system related queries are logged, which cannot be skipped, so will be seen in the Reports
5. Neo4j does not support Failed Login.
6. Syntactically incorrect queries are executed as success queries and not as Sql Error.


#### For details on configuring Filebeat connection over SSL, refer [Configuring Filebeat to push logs to Guardium](https://github.com/IBM/universal-connectors/blob/main/input-plugin/logstash-input-beats/README.md#configuring-filebeat-to-push-logs-to-guardium).


## Configuring the Neo4j filters in Guardium

The Guardium universal connector is the Guardium entry point for native audit logs. The universal connector identifies and parses received events, and then converts them to a standard Guardium format. The output of the universal connector is forwarded to the Guardium sniffer on the collector, for policy and auditing enforcements. Configure Guardium to read the native audit logs by customizing the Neo4j template.

### Before you begin

• Configure the policies you require. See [policies](/docs/#policies) for more information.

• You must have permission for the S-Tap Management role. The admin user includes this role, by default.

• Neo4j-Guardium Logstash filter plug-in is automatically available with Guardium Data Protection versions 12.x, 11.4 with appliance bundle 11.0p490 or later or Guardium Data Protection version 11.5 with appliance bundle 11.0p540 or later releases.

**Note**: For Guardium Data Protection version 11.4 without appliance bundle 11.0p490 or prior or Guardium Data Protection version 11.5 without appliance bundle 11.0p540 or prior, download the [logstash-filter-neodb_guardium_filter.zip](https://github.com/IBM/universal-connectors/releases/download/v1.5.6/logstash-filter-neodb_guardium_filter.zip) plug-in. (Do not unzip the offline-package file throughout the procedure).

### Configuration

1. On the collector, go to ```Setup``` > ```Tools and Views``` > ```Configure Universal Connector```.
2. Enable the universal connector if it is disabled.
3. Click ```Upload File``` and select the offline [logstash-filter-neodb_guardium_filter.zip](https://github.com/IBM/universal-connectors/releases/download/v1.5.6/logstash-filter-neodb_guardium_filter.zip) plug-in. After it is uploaded, click ```OK```. This step is not necessary for Guardium Data Protection v11.0p490 or later, v11.0p540 or later, v12.0 or later.
4. Click the Plus sign to open the Connector Configuration dialog box.
5. Type a name in the ```Connector name``` field.
6. Update the input section to add the details from the [neo4jFilebeat.conf](https://github.com/IBM/universal-connectors/raw/main/filter-plugin/logstash-filter-neo4j-guardium/neo4jFilebeat.conf) file's input part, omitting the keyword "input{" at the beginning and its corresponding "}" at the end.
7. Update the filter section to add the details from the [neo4jFilebeat.conf](https://github.com/IBM/universal-connectors/raw/main/filter-plugin/logstash-filter-neo4j-guardium/neo4jFilebeat.conf)  file's filter part, omitting the keyword "filter{" at the beginning and its corresponding "}" at the end.
8. The 'type' fields should match in the input and filter configuration sections. This field should be unique for every individual connector added.
9. Click ```Save```. Guardium validates the new connector and displays it in the Configure Universal Connector page.
10. After the offline plug-in is installed and the configuration is uploaded and saved in the Guardium machine, restart the Universal Connector using the ```Disable/Enable``` button.

## Configuring the Neo4j filters in Guardium Data Security Center
To configure this plug-in for Guardium Data Security Center, follow [this guide.](/docs/Guardium%20Insights/3.2.x/UC_Configuration_GI.md)
For the input configuration step, refer to the [Filebeat section](/docs/Guardium%20Insights/3.2.x/UC_Configuration_GI.md#Filebeat-input-plug-in-configuration).
