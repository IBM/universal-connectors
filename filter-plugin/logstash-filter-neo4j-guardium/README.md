## Neo4j-Guardium Logstash filter plug-in

This is a [Logstash](https://github.com/elastic/logstash) filter plug-in for the univer***REMOVED***l connector that is featured in IBM Security Guardium. It parses events and mes***REMOVED***ges from the Neo4j audit log into a Guardium record instance (which is a standard structure made out of several parts). The information is then sent over to Guardium. Guardium records include the accessor (the person who tried to access the data), the session, data, and exceptions. If there are no errors, the data contains details about the query "construct". The contstruct details the main action (verb) and collections (objects) involved.

Currently, this plug-in will work only with IBM Security Guardium Data Protection, and not Guardium Insights.

The plug-in is free and open-source (Apache 2.0). It can be used as a starting point to develop additional filter plug-ins for Guardium univer***REMOVED***l connector.

# Creating the Neo4j database and configuring logs

## Procedure

	1. Download “neo4j-community-4.2.1.zip”. Extract the archive file and then install Neo4j.
	2. Add a database from the console.
	3. To view logs, go to the Neo4j console and select DB > manage > Open Folder > logs.
	4. These log types are supported:
		a. Debug.log: Information useful when debugging problems with Neo4j.
		b. neo4j.log: The standard log, where general information about Neo4j is written.
		c. query.log: Log of executed queries that takes longer than a specified threshold.
		d. security.log: Log of security events.
		e. http.log: Request log for the HTTP API.
		f. gc.log: Garbage collection logging provided by the Java Virtual Machine (JVM).
		g. service-error.log: (Windows) Log of errors encountered when installing or running the Windows service.
	5. These instructions will use query.log for the Guardium filter, as it contains all database-related log events.
	6. Log configuration: Create this configuration in the neo4j.conf file:
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
	7. Cleanup for log files: You can set a maximum number of history files to be kept on the system for the query log. To enable this, include this configuration in the neo4j.conf file:
				dbms.logs.query.rotation.keep_number=7

# Filebeat installation and configuration

## Procedure

	1. Filebeat installation :
		a. Download the Filebeat Windows zip file from the downloads page at <https://www.elastic.co/downloads/beats/filebeat>.
		b. Extract the contents of the zip file to C:\Program Files.
		c. Rename the filebeat-<version>-windows directory to Filebeat.
		d. Open a PowerShell prompt as an Administrator.
		e. From the PowerShell prompt, run these commands to install Filebeat as a Windows service:
			I. PS > cd 'C:\Program Files\Filebeat'
			II. PS C:\Program Files\Filebeat> .\install-service-filebeat.ps1 [Note: If PSSecurityException: UnauthorizedAccess error occurs, use  command: #PowerShell.exe -ExecutionPolicy UnRestricted -File .\install-service-filebeat.ps1]
		f. To run Filebeat, issue this command:
			I. PS C:\Program Files\Filebeat>./filebeat -e -c filebeat.yml -d "publish"

	2. Filebeat configuration:
		Add these settings to the filebeat.yaml file:

		a. Under "filebeat.inputs", provide the path from which Filebeat can read Neo4j audit logs and add tag :
			paths :  - /path/to/query.log
			tags : ["Neo4j"]		#Name of database
		b. Under the output.logstash section, specify the host and port to which you want to send log events:
			hosts: ["<host>:<port>"]
		c. Some events are logged on multiple lines in the log file - but Filebeats considers each line as a new event. To avoid this, add these settings “filebeat.inputs”. These settings tell Filebeat that, when the pattern matches ^[0-9]{4}-[0-9]{2}-[0-9]{2} (the timestamp in the event log), the line should be consider as a new event. When the pattern is not matched, the line will be appended to the previous event log:
				multiline.type: pattern
				multiline.pattern: '^[0-9]{4}-[0-9]{2}-[0-9]{2}'
				multiline.negate: true
				multiline.match: after


## Configuring Neo4j filters in Guardium

The Guardium univer***REMOVED***l connector is the Guardium entry point for native audit logs. The univer***REMOVED***l connector identifies and parses received events, and then converts them to a standard Guardium format. The output of the univer***REMOVED***l connector is forwarded to the Guardium sniffer on the collector, for policy and auditing enforcements. Configure Guardium to read the native audit logs by customizing the Neo4j template.

## Before you begin

	• You must have permission for the S-Tap Management role. The admin user has this role by default.
	• Download the neo4j-logstash-offline-plugins-7.5.2.zip plug-in.

# Procedure

	1. On the collector, go to Setup > Tools and Views > Configure Univer***REMOVED***l Connector.
	2. Click Upload File and select the offline neo4j-logstash-offline-plugins-7.5.2.zip plug-in. After it is uploaded, click OK.
	3. Click the Plus sign to open the Connector Configuration dialog box.
	4. Type a name in the Connector name field.
	5. Update the input section to add the details from the https://github.com/IBM/univer***REMOVED***l-connectors/tree/main/filter-plugin/logstash-filter-neo4j-guardium/NeodbOverFilebeatPackage/config.json file input section, omitting the keyword "input{" at the beginning and its corresponding "}" at the end.
	6. Update the filter section to add the details from the https://github.com/IBM/univer***REMOVED***l-connectors/tree/main/filter-plugin/logstash-filter-neo4j-guardium/NeodbOverFilebeatPackage/config.json  file filter section, omitting the keyword "filter{" at the beginning and its corresponding "}" at the end.
	7. Click Save. Guardium validates the new connector, and enables the univer***REMOVED***l connector if it was di***REMOVED***bled. After it is validated, it appears in the Configure Univer***REMOVED***l Connector page.

## Limitations

	If your Guardium version is 11.4 and below, the port should not be 5000, 5141 or 5044, as Guardium Univer***REMOVED***l Connector reserves these ports for MongoDB events. To check the available ports on Windows, issue this command:
		netstat -a
