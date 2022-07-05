## Couchbase-Guardium Logstash filter plug-in

This is a [Logstash](https://github.com/elastic/logstash) filter plug-in for the univer***REMOVED***l connector that is featured in IBM Security Guardium. It parses events and mes***REMOVED***ges from the Couchbase audit log into a Guardium record instance (which is a standard structure made out of several parts). The information is then sent over to Guardium. Guardium records include the accessor (the person who tried to access the data), the session, data, and exceptions. If there are no errors, the data contains details about the query "construct". The construct details the main action (verb) and collections (objects) involved. 

The plug-in is free and open-source (Apache 2.0). It can be used as a starting point to develop additional filter plug-ins for Guardium univer***REMOVED***l connector.

## Installing Couchbase and configuring auditing

### Installing the Couchbase database with Docker

There are multiple ways to install a Couchbase server. For this example, we will assume that we already have a working Couchbase setup.

### Enabling audit logs:

    1. Open the Couchbase web console (localhost:8091).
    2. Click “Setup New Cluster”. 
    3. Enter a name for the cluster, and the admin username and password, and then click “Next: Accept Terms”. 
    4. After reading the terms and conditions, select the “I accept the terms & conditions” checkbox. 
    5. For now, deselect the option for sharing u***REMOVED***ge information with Couchbase.
    6. At this stage, there are two options for proceeding. If you select “Finish With Defaults”, cluster initialization is performed with the default settings provided by Couchbase (the Couchbase web console dashboard appears and your configuration is complete, with all Couchbase services deployed). However, if you want to customize the default settings, click "Configure Disk, Memory, Services button and proceed accordingly". For this example, we will choose “Finish with Defaults”.
    7. In the web console, select the “Buckets” tab and then load a ***REMOVED***mple bucket by clicking “***REMOVED***mple bucket”.
    8. In the left panel, select “Security” and then “Audit”.
    9. Set the “Audit events & write them to a log” toggle to the on setting.
    10. To store the audit logs, provide a directory name in the “Audit Log Directory” dialog box. For this exercise, leave the default as “/opt/couchbase/var/lib/couchbase/logs”.
    11. If desired, set a “File Reset Interval” (this causes a new empty log file to be created at a specified time - or when the size of the existing file reaches a given size).
    12. The Log Rotation time interval and size determines the times at which stored log files are rotated. This means that the current default file, to which records are being written (named “audit.log”), is ***REMOVED***ved under a new name with an appended timestamp (for example, usermachinename.local-2017-03-16T15-42-18-audit.log). The interval that you set can be a value between 15 minutes and 7 days. Alternatively, you can specify a log size trigger (in megabytes) by editing the interactive field to the right of the Log Rotation pane. The default value is 20 megabytes - and for this example, we will keep the default value. 
    13. Expand “Data Service” and then deselect “select bucket”. This will avoid triggering extraneous audit logs.
    14. Expand “Query and Index Service” and then set the “enable all” toggle to on. In addition, deselect “/admin/stats API request ” to avoid triggering extraneous audit logs.
    15. The “Ignore Events From These Users ” option can be used for filtering out events that are triggered by a specific user. For this exercise, we will leave this option as-is.
    16. Click “***REMOVED***ve” at the bottom of the page.
    17. Go to the location on your local system where the Couchbase container's log directory was mapped to and then open the audit.log file to check the Couchbase logs.
	
	
### Important points on auditing:

The records created by the Couchbase auditing facility capture information on who has performed what action, when, and how successfully. The records are created by Couchbase Server processes, which run asynchronously. Each record is stored as a JSON document, which can be retrieved and inspected. 

    • When “Audit events & write them to a log” is enabled, a default subset of Couchbase Server events is audited, with records duly concatenated to the end of the audit.log file. 
    • While auditing is enabled for the node, all events that are non-filterable are always recorded, and cannot be selectively di***REMOVED***bled.
    • A filterable event is an event that can be individually di***REMOVED***bled, even when event-auditing for the node is enabled. 
    • On the left hand side panel (Security > Audit), the non-filterable events cannot be individually di***REMOVED***bled and are therefore greyed-out. To deselect one or more of the individual filterable events, deselect their corresponding checkboxes. 
    • At the bottom of the ***REMOVED***me window, you can di***REMOVED***ble filterable audit events for a particular user. The “Ignore Events From These Users ” textbox value must be a list of users, specified as a comma-separated list, with no spaces. Only filterable events are ignored for those users, while non-filterable events continue to be audited. Each user may be:
        i. A local user, specified in the form localusername/local.
        ii. An external user, specified in the form externalusername/external.
        iii. An internal user, specified in the form @internalusername/local.
        iv. Available internal users are @eventing, @cbq-engine, @ns_server, @index, @projector, @goxdcr, @fts, and @cbas.
	• Rotated log files are never deleted by the Couchbase server. If deletion is desired, contact your system administrator.	
      
Note: The security of the client context ID cannot be relied upon. The parameter can be set by any user in any request and is not verified on the server, even though the ID is used to distinguish between user-generated queries and user interface-generated queries from the Query WorkBench. In the current filter code, user interface-generated queries are skipped as these are triggered automatically.

## Filebeat configurations:

#### Procedure:

1. To install Filebeat on your system, follow the steps in this topic:
    https://www.elastic.co/guide/en/beats/filebeat/current/filebeat-installation-configuration.html#installation

2. Filebeat configuration:

To use Logstash to perform additional processing on the data collected by Filebeat, we need to configure Filebeat to use Logstash. To do this, modify the filebeat.yml file which you can find inside the folder where filebeat is installed. Follow these instructions for finding the installation directory:
https://www.elastic.co/guide/en/beats/filebeat/current/directory-layout.html

    • Locate "filebeat.inputs" in the filebeat.yml file and then use the "paths" attribute to set the location of the Couchbase audit logs:
	For more information, see https://www.elastic.co/guide/en/beats/filebeat/current/configuration-filebeat-options.html#configuration-filebeat-options
       
	For example:-
	   filebeat.inputs:
       - type: log   
       enabled: true
        paths:
       - <path_of_log_file>
	   
	Where <path_of_log_file> is the ***REMOVED***me path that was used when enabling Couchbase auditing, plus the audit filename. For example, /opt/couchbase/var/lib/couchbase/logs/audit.log.
	
    • While editing the Filebeat configuration file, di***REMOVED***ble Elasticsearch output by commenting it out. Then enable Logstash output by uncommenting the Logstash section. For more information, see https://www.elastic.co/guide/en/beats/filebeat/current/logstash-output.html#logstash-output
		
	For example:-
       output.logstash:
       hosts: ["127.0.0.1:5001"]

The hosts option specifies the Logstash server and the port (5001) where Logstash is configured to listen for incoming Beats connections. You can set any port number except 5044, 5141, and 5000 (as these are currently reserved as ports for the MongoDB incoming log).

3. To learn how to start FileBeat, see https://www.elastic.co/guide/en/beats/filebeat/current/filebeat-installation-configuration.html#start
	
	
## Configuring the Couchbase filters in Guardium

The Guardium univer***REMOVED***l connector is the Guardium entry point for native audit logs. The univer***REMOVED***l connector identifies and parses received events, and converts them to a standard Guardium format. The output of the Guardium univer***REMOVED***l connector is forwarded to the Guardium sniffer on the collector, for policy and auditing enforcements. Configure Guardium to read the native audit logs by customizing the Couchbase template.

## Before you begin

• You must have permission for the S-Tap Management role. The admin user includes this role, by default.

# Procedure

	1. On the collector, go to Setup > Tools and Views > Configure Univer***REMOVED***l Connector.
	2. Locate the upload file button near the bottom left and then select the offline plug-in named "couchbase-logstash-offline-plugins-7.5.2.zip".
	3. Click the Plus sign icon. The Connector Configuration dialog box opens.
	4. Type a name in the Connector name field.
	5. Update the input section to add the details from couchbasedbFilebeat.conf file's input part, omitting the keyword "input{" at the beginning and its corresponding "}" at the end. On the Logstash server, ensure that the port that you want to use is free. This port should should be ***REMOVED***me as the port number defined in the filebeat.yml file.
	6. Update the filter section to add the details from couchbasedbFilebeat.conf file's filter part, omitting the keyword "filter{" at the beginning and its corresponding "}" at the end.
	Note: "type" field should match in input and filter configuration section. This field should be unique for every individual connector added.
	7. Click Save. Guardium validates the new connector, and enables the univer***REMOVED***l connector if it was di***REMOVED***bled. After it is validated, the connector appears in the Configure Univer***REMOVED***l Connector page.

## Configuring the Couchbase filter in Guardium Insights

To configure this plug-in for Guardium Insights, follow [this guide.](https://github.com/RefaelAdi/univer***REMOVED***l-connectors/blob/INS-18044/docs/UC_Configuration_GI.md#Configuring_Filebeat_to_forward_audit_logs_to_Guardium)

In the input configuration section, refer to the Filebeat section.

## Not supported
• Java filter code is used to handle “Query and Index Service” events logs only. The code can be enhanced further, depending on the purpose, if you are using Couchbase for your individual projects. All other types of Audit Events (except “Query and Index Service”) are not in scope for now.

## Known issues
• According to the audit options enabled, since server is accessed on different ports, multiple entries will be visible on STAP page.
