# Cassandra-Guardium Logstash filter plug-in
### Meet Cassandra
* Tested versions: 4.0.1
* Environment: On-premise
* Supported inputs: Filebeat (push)
* Supported Guardium versions:
	* Guardium Data Protection: 11.4 and above

This is a [Logstash](https://github.com/elastic/logstash) filter plug-in for the universal connector that is featured in IBM Security Guardium. It parses events and messages from the Cassandra audit log into a [Guardium record](https://github.com/IBM/universal-connectors/blob/main/common/src/main/java/com/ibm/guardium/universalconnector/commons/structures/Record.java) instance (which is a standard structure made out of several parts). The information is then sent over to Guardium. Guardium records include the accessor (the person who tried to access the data), the session, data, and exceptions. If there are no errors, the data contains details about the query "construct". The construct details the main action (verb) and collections (objects) involved. 

The plug-in is free and open-source (Apache 2.0). It can be used as a starting point to develop additional filter plug-ins for Guardium universal connector.

## 1. Configuring the Cassandra server

There are multiple ways to install a Cassandra server. For this example, we will assume that we already have a working Cassandra setup.

## 2. Enabling Auditing
    1. Edit /etc/cassandra/conf/cassandra.yaml.
		• In 'audit_logging_options' Change enabled: false to true
		• Set logger: class_name: FileAuditLogger
    2. Edit /etc/cassandra/conf/logback.xml. 
		• Uncomment below section
       <appender name="AUDIT" class="ch.qos.logback.core.rolling.RollingFileAppender">
	   <file>${cassandra.logdir}/audit/audit.log</file>
	   <rollingPolicy class="ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy">
       <!-- rollover daily -->
       <fileNamePattern>${cassandra.logdir}/audit/audit.log.%d{yyyy-MM-dd}.%i.zip</fileNamePattern>
       <!-- each file should be at most 50MB, keep 30 days worth of history, but at most 5GB -->
       <maxFileSize>50MB</maxFileSize>
       <maxHistory>30</maxHistory>
       <totalSizeCap>5GB</totalSizeCap>
	   </rollingPolicy>
       <encoder>
       <pattern>%-5level [%thread] %date{ISO8601} %F:%L - %msg%n</pattern>
       </encoder>
      </appender>
      
	  <!-- Audit Logging additivity to redirect audit logging events to audit/audit.log -->
      <logger name="org.apache.cassandra.audit" additivity="false" level="INFO">
      <appender-ref ref="AUDIT"/>
      </logger>
	  
    After saving files, restart the Cassandra service
	
## 3. Viewing the audit logs

The audit logs can be viewed under the parameter "${cassandra.logdir}/audit/" under the file name "audit.log" in the /etc/cassandra/conf/logback.xml file.

## 4. Configuring Filebeat to push logs to Guardium

## a. Filebeat installation

### Procedure:

1. To install Filebeat on your system, follow the steps in this topic:
    https://www.elastic.co/guide/en/beats/filebeat/current/filebeat-installation-configuration.html#installation

## b. Filebeat configuration:

To use Logstash to perform additional processing on the data collected by Filebeat, we need to configure Filebeat to use Logstash. To do this, modify the filebeat.yml file which you can find inside the folder where filebeat is installed. Follow these instructions for finding the installation directory:
https://www.elastic.co/guide/en/beats/filebeat/current/directory-layout.html

### Procedure:

1. Configuring the input section :-

    • Locate "filebeat.inputs" in the filebeat.yml file, then add the following parameters.

		filebeat.inputs:
			- type: log   
			- enabled: true
		paths:
			- <path_of_log_file as specified in /etc/cassandra/conf/logback.xml file>
			- exclude_lines: ['AuditLogManager']

	where path_of_log_file is the same path that was used when enabling Cassandra auditing (${cassandra.logdir}/audit/audit.log). For example, /var/log/cassandra/audit/audit.log.
	
	• To process multi-line audit events, add the settings in same inputs section.
	
			multiline.type: pattern
			multiline.pattern: '^INFO'
			multiline.negate: true
			multiline.match: after
			
	• Add the tags to uniquely identify the Cassandra events from the rest.
			tags: ["guc_cassandra_param"]
	
2. Configuring the output section:

		• Locate "output" in the filebeat.yml file, then add the following parameters.

		• Disable Elasticsearch output by commenting it out.

		• Enable Logstash output by uncommenting the Logstash section. For more information, see https://www.elastic.co/guide/en/beats/filebeat/current/logstash-output.html#logstash-output

    For example:

		output.logstash:
			hosts: ["127.0.0.1:5001"]
		
		• The hosts option specifies the Logstash server and the port (5001) where Logstash is configured to listen for incoming Beats connections.

		• You can set any port number except 5044, 5141, and 5000 (as these are currently reserved in Guardium v11.3 and v11.4 ).

3. To learn how to start FileBeat, see https://www.elastic.co/guide/en/beats/filebeat/current/filebeat-installation-configuration.html#start

### Limitations

Cassandra audit logs do not provide 'Source program' value, hence it is kept blank.	

## 5. Configuring the Cassandra filters in Guardium

The Guardium universal connector is the Guardium entry point for native audit logs. The universal connector identifies and parses received events, and converts them to a standard Guardium format. The output of the Guardium universal connector is forwarded to the Guardium sniffer on the collector, for policy and auditing enforcements. Configure Guardium to read the native audit logs by customizing the Cassandra template.

### Before you begin

•  Configure the policies you require. See [policies](/../../#policies) for more information.

• You must have permission for the S-Tap Management role. The admin user includes this role, by default.

• Download the [cassandra-offline-plugins-7.5.2.zip plug-in.](https://github.com/IBM/universal-connectors/blob/main/filter-plugin/logstash-filter-cassandra-guardium/CassandraOverFilebeatPackage/Cassandra/cassandra-offline-plugins-7.5.2.zip)																	
### Procedure

1. On the collector, go to Setup > Tools and Views > Configure Universal Connector.
2. First enable the Universal Guardium connector, if it is disabled already.
3. Click Upload File and select the offline [cassandra-offline-plugins-7.5.2.zip plug-in.](https://github.com/IBM/universal-connectors/blob/main/filter-plugin/logstash-filter-cassandra-guardium/CassandraOverFilebeatPackage/Cassandra/cassandra-offline-plugins-7.5.2.zip) plug-in. After it is uploaded, click OK.
4. Click the Plus sign to open the Connector Configuration dialog box.
5. Type a name in the Connector name field.
6. Update the input section to add the details from the [filter-test-beats.conf](https://github.com/IBM/universal-connectors/blob/main/filter-plugin/logstash-filter-cassandra-guardium/filter-test-beats.conf) file's input part, omitting the keyword "input{" at the beginning and its corresponding "}" at the end.
7. Update the filter section to add the details from the [filter-test-beats.conf](https://github.com/IBM/universal-connectors/blob/main/filter-plugin/logstash-filter-cassandra-guardium/filter-test-beats.conf) file's filter part, omitting the keyword "filter{" at the beginning and its corresponding "}" at the end.
8. The "type" fields should match in the input and the filter configuration sections. This field should be unique for  every individual connector added.
9. Click Save. Guardium validates the new connector, and enables the universal connector if it was disabled. After it is validated, it appears in the Configure Universal Connector page.
