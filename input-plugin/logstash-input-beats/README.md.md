## 1. beats input plug-in

This is a [Logstash](https://github.com/elastic/logstash) input plug-in for the univer***REMOVED***l connector that is featured in IBM Security Guardium. It  enables Logstash to receive events from the Beats framework. The events are then sent over to corresponding filter plugin which transforms these audit logs into a [Guardium record](https://github.com/IBM/univer***REMOVED***l-connectors/blob/main/common/src/main/java/com/ibm/guardium/univer***REMOVED***lconnector/commons/structures/Record.java)  instance (which is a standard structure made out of several parts). The information is then sent over to Guardium. Guardium records include the accessor (the person who tried to access the data), the session, data, and exceptions. If there are no errors, the data contains details about the query "construct". The construct details the main action (verb) and collections (objects) involved.


## 2. Purpose:

Specify a port, and this plugin will poll the ***REMOVED***me port on the Logstash host for any new log events.


## 3. U***REMOVED***ge:

### Parameters:
	
| Parameter | Input Type | Required | Default |
|-----------|------------|----------|---------|
| port  | number | Yes | |



#### `port`
The `port` setting allows specifying a port on which the Logstash host listens to and pull the log events written there.


#### Logstash Default config params
Other standard logstash parameters are available such as:
* `add_field`
* `type`
* `tags`

### Example

	input {
		beats {
			port => 5044
		}
	}


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
			- <path_of_log_file>
			- exclude_lines: [<if any particular event needs to be skipped>]

	where path_of_log_file is the ***REMOVED***me path that was used when enabling Database auditing. For example, /var/log/cas***REMOVED***ndra/audit/audit.log for Cas***REMOVED***ndra database
	
	• To process multi-line audit events, add the settings in ***REMOVED***me inputs section.
	
			multiline.type: pattern
			multiline.pattern: '<pattern_to_match>'
			multiline.negate: true
			multiline.match: after
	
	where pattern_to_match is the pattern, which determines the begining of every Event, so that other lines that do not begin with the ***REMOVED***me are concatenated as a single event.
			
	• Add the tags to uniquely identify the events from the rest.
			tags: ["***REMOVED***mple_db_param"]
	
2. Configuring the output section:

		• Locate "output" in the filebeat.yml file, then add the following parameters.

		• Di***REMOVED***ble Elasticsearch output by commenting it out.

		• Enable Logstash output by uncommenting the Logstash section. For more information, see https://www.elastic.co/guide/en/beats/filebeat/current/logstash-output.html#logstash-output

    For example:

		output.logstash:
			hosts: ["127.0.0.1:5001"]
		
		• The hosts option specifies the Logstash server and the port (5001) where Logstash is configured to listen for incoming Beats connections.

		• You can set any port number except 5044, 5141, and 5000 (as these are currently reserved in Guardium v11.3 and v11.4 ).

3. To learn how to start FileBeat, see https://www.elastic.co/guide/en/beats/filebeat/current/filebeat-installation-configuration.html#start