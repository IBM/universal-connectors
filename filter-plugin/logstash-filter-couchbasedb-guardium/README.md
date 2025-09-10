## Couchbase-Guardium Logstash filter plug-in
### Meet Couchbase
* Tested versions: 6.6.2-9600, 7.2.5-7596, 7.6.1-3200, 7.6.3-4200, 7.6.4-5146
* Environment: On-premise, Iaas
* Supported inputs: Filebeat (push)
* Supported Guardium versions:
	* Guardium Data Protection: 11.4 and above
    * Guardium Data Security Center SaaS: 1.0

This is a [Logstash](https://github.com/elastic/logstash) filter plug-in for the universal connector that is featured in IBM Security Guardium. It parses events and messages from the Couchbase audit log into a [Guardium record](https://github.com/IBM/universal-connectors/blob/main/common/src/main/java/com/ibm/guardium/universalconnector/commons/structures/Record.java) instance (which is a standard structure made out of several parts). The information is then sent over to Guardium. Guardium records include the accessor (the person who tried to access the data), the session, data, and exceptions. If there are no errors, the data contains details about the query "construct". The construct details the main action (verb) and collections (objects) involved. 

The plug-in is free and open-source (Apache 2.0). It can be used as a starting point to develop additional filter plug-ins for Guardium universal connector.

## 1. Configuring the Couchbase database

There are multiple ways to install a Couchbase server. For this example, we will assume that we already have a working Couchbase setup.

## 2. Enabling audit logs:

    1. Open the Couchbase web console (localhost:8091).
    2. Click “Setup New Cluster”. 
    3. Enter a name for the cluster, and the admin username and password, and then click “Next: Accept Terms”. 
    4. After reading the terms and conditions, select the “I accept the terms & conditions” checkbox. 
    5. For now, deselect the option for sharing usage information with Couchbase.
    6. At this stage, there are two options for proceeding. If you select “Finish With Defaults”, cluster initialization is performed with the default settings provided by Couchbase (the Couchbase web console dashboard appears and your configuration is complete, with all Couchbase services deployed). However, if you want to customize the default settings, click "Configure Disk, Memory, Services button and proceed accordingly". For this example, we will choose “Finish with Defaults”.
    7. In the web console, select the “Buckets” tab and then load a sample bucket by clicking “sample bucket”.
    8. In the left panel, select “Security” and then “Audit”.
    9. Set the “Audit events & write them to a log” toggle to the on setting.
    10. To store the audit logs, provide a directory name in the “Audit Log Directory” dialog box. For this exercise, leave the default as “/opt/couchbase/var/lib/couchbase/logs”.
    11. If desired, set a “File Reset Interval” (this causes a new empty log file to be created at a specified time - or when the size of the existing file reaches a given size).
    12. The Log Rotation time interval and size determines the times at which stored log files are rotated. This means that the current default file, to which records are being written (named “audit.log”), is saved under a new name with an appended timestamp (for example, usermachinename.local-2017-03-16T15-42-18-audit.log). The interval that you set can be a value between 15 minutes and 7 days. Alternatively, you can specify a log size trigger (in megabytes) by editing the interactive field to the right of the Log Rotation pane. The default value is 20 megabytes - and for this example, we will keep the default value. 
    13. Expand “Data Service” and then deselect “select bucket”. This will avoid triggering extraneous audit logs.
    14. Expand “Query and Index Service” and then set the “enable all” toggle to on. In addition, deselect “/admin/stats API request ” to avoid triggering extraneous audit logs.
    15. The “Ignore Events From These Users ” option can be used for filtering out events that are triggered by a specific user. For this exercise, we will leave this option as-is.
    16. Click “save” at the bottom of the page.
    17. Go to the location on your local system where the Couchbase container's log directory was mapped to and then open the audit.log file to check the Couchbase logs.
	
	
### Note:

The records created by the Couchbase auditing facility capture information on who has performed what action, when, and how successfully. The records are created by Couchbase Server processes, which run asynchronously. Each record is stored as a JSON document, which can be retrieved and inspected. 

    • When “Audit events & write them to a log” is enabled, a default subset of Couchbase Server events is audited, with records duly concatenated to the end of the audit.log file. 
    • While auditing is enabled for the node, all events that are non-filterable are always recorded, and cannot be selectively disabled.
    • A filterable event is an event that can be individually disabled, even when event-auditing for the node is enabled. 
    • On the left hand side panel (Security > Audit), the non-filterable events cannot be individually disabled and are therefore greyed-out. To deselect one or more of the individual filterable events, deselect their corresponding checkboxes. 
    • At the bottom of the same window, you can disable filterable audit events for a particular user. The “Ignore Events From These Users ” textbox value must be a list of users, specified as a comma-separated list, with no spaces. Only filterable events are ignored for those users, while non-filterable events continue to be audited. Each user may be:
        i. A local user, specified in the form localusername/local.
        ii. An external user, specified in the form externalusername/external.
        iii. An internal user, specified in the form @internalusername/local.
        iv. Available internal users are @eventing, @cbq-engine, @ns_server, @index, @projector, @goxdcr, @fts, and @cbas.
	• Rotated log files are never deleted by the Couchbase server. If deletion is desired, contact your system administrator.	
      
	• The security of the client context ID cannot be relied upon. The parameter can be set by any user in any request and is not verified on the server, even though the ID is used to distinguish between user-generated queries and user interface-generated queries from the Query WorkBench. In the current filter code, user interface-generated queries are skipped as these are triggered automatically.

## 3. Configuring Filebeat to push logs to Guardium

## a. Filebeat installation

### Procedure:
1. To install Filebeat on your system, follow the steps in this topic:
    https://www.elastic.co/guide/en/beats/filebeat/current/filebeat-installation-configuration.html#installation

## b. Filebeat configuration:

To use Logstash to perform additional processing on the data collected by Filebeat, we need to configure Filebeat to use Logstash. To do this, modify the filebeat.yml file which you can find inside the folder where filebeat is installed. Follow these instructions for finding the installation directory:
https://www.elastic.co/guide/en/beats/filebeat/current/directory-layout.html

### Procedure:

1. Configuring the input section :

    • Locate "filebeat.inputs" in the filebeat.yml file, then add the following parameters.
		  
		filebeat.inputs:
		- type: filestream   
        - id: <ID>
   		- enabled: true
		paths:
			- <path_of_log_file>
		
	where path_of_log_file is the same path that was used when enabling Couchbase auditing, plus the audit filename. For example, /opt/couchbase/var/lib/couchbase/logs/audit.log.
	
	• Add the tags to uniquely identify the Couchbase events from the rest.
		tags : ["couchbase"]
	
	
2. Configuring the output section:

		• Locate "output" in the filebeat.yml file, then add the following parameters.

		• Disable Elasticsearch output by commenting it out.

		• Enable Logstash output by uncommenting the Logstash section. For more information, see https://www.elastic.co/guide/en/beats/filebeat/current/logstash-output.html#logstash-output

    For example:

		output.logstash:
			hosts: ["127.0.0.1:5001"]
		
		• The hosts option specifies the Logstash server and the port (5001) where Logstash is configured to listen for incoming Beats connections.
		
		• You can set any port number except 5044, 5141, and 5000 (as these are currently reserved in Guardium v11.3 and v11.4 ).

		• Locate "Processors" in the filebeat.yml file and then add below attribute to get timezone of Server:
   		For more information, see https://www.elastic.co/guide/en/beats/filebeat/current/add-locale.html
   		For example:-
       		processors:
    		- add_locale: ~


3. To learn how to start FileBeat, see https://www.elastic.co/guide/en/beats/filebeat/current/filebeat-installation-configuration.html#start

#### For details on configuring Filebeat connection over SSL, refer [Configuring Filebeat to push logs to Guardium](https://github.com/IBM/universal-connectors/blob/main/input-plugin/logstash-input-beats/README.md#configuring-filebeat-to-push-logs-to-guardium).


### Limitations

	• DBName is set to 'NA' since it is not present in audit log.
	• Java filter code is used to handle “Query and Index Service” events logs only. The code can be enhanced further, depending on the purpose, if you are using Couchbase for your individual projects. All other types of Audit Events (except “Query and Index Service”) are not in scope for now.

## 4. Configuring the Couchbase filter in Guardium

The Guardium universal connector is the Guardium entry point for native audit logs. The universal connector identifies and parses received events, and converts them to a standard Guardium format. The output of the Guardium universal connector is forwarded to the Guardium sniffer on the collector, for policy and auditing enforcements. Configure Guardium to read the native audit logs by customizing the Couchbase template.

#### Before you begin

• Configure the policies you require. See [policies](/docs/#policies) for more information.

• You must have permission for the S-Tap Management role. The admin user includes this role, by default.

• Couchbase-Guardium Logstash filter plug-in is automatically available with Guardium Data Protection versions 12.x, 11.4 with appliance bundle 11.0p490 or later or Guardium Data Protection version 11.5 with appliance bundle 11.0p540 or later releases.

**Note**: For Guardium Data Protection version 11.4 without appliance bundle 11.0p490 or prior or Guardium Data Protection version 11.5 without appliance bundle 11.0p540 or prior, download the [couchbase-offline-pack.zip](logstash-filter-couchbasedb_guardium_plugin_filter.zip) plug-in. (Do not unzip the offline-package file throughout the procedure).


#### Configuration
1. On the collector, go to ```Setup``` > ```Tools and Views``` > ```Configure Universal Connector```.
2. Enable the universal connector if it is disabled.
3. Click ```Upload File``` and select the offline [couchbase-offline-pack.zip](logstash-filter-couchbasedb_guardium_plugin_filter.zip) plug-in. After it is uploaded, click ```OK```. This step is not necessary for Guardium Data Protection v11.0p490 or later, v11.0p540 or later, v12.0 or later.
4. Click the Plus sign to open the Connector Configuration dialog box.
5. Type a name in the ```Connector name``` field.
6. Update the input section to add the details from the [couchbasedbFilebeat.conf](https://github.com/IBM/universal-connectors/raw/main/filter-plugin/logstash-filter-couchbasedb-guardium/couchbasedbFilebeat.conf) file's input part, omitting the keyword "input{" at the beginning and its corresponding "}" at the end. On the Logstash server, ensure that the port that you want to use is free. This port should be same as the port number defined in the filebeat.yml file.
7. Update the filter section to add the details from the [couchbasedbFilebeat.conf](https://github.com/IBM/universal-connectors/raw/main/filter-plugin/logstash-filter-couchbasedb-guardium/couchbasedbFilebeat.conf) file's filter part, omitting the keyword "filter{" at the beginning and its corresponding "}" at the end.
8. The 'type' fields should match in the input and filter configuration sections. This field should be unique for every individual connector added.
9. Click ```Save```. Guardium validates the new connector and displays it in the Configure Universal Connector page.
10. After the offline plug-in is installed and the configuration is uploaded and saved in the Guardium machine, restart the Universal Connector using the ```Disable/Enable``` button.


## 5. Configuring the Couchbase filter in Guardium Data Security Center

To configure this plug-in for Guardium Data Security Center, follow [this guide.](/docs/Guardium%20Insights/3.2.x/UC_Configuration_GI.md)

In the input configuration section, refer to the Filebeat section.

#### Known issues
• According to the audit options enabled, since the server is accessed on different ports, multiple entries will be visible on the STAP page.
