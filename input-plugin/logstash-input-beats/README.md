## beats input plug-in
### Meet Beats
* Tested versions: 6.2.5
* Developed by Elastic
* Configuration instructions can be found on every relevant filter plugin readme page. For example: [MongoDB](https://github.com/IBM/universal-connectors/blob/main/filter-plugin/logstash-filter-mongodb-guardium#configuring-audit-logs-on-mongodb-and-forwarding-to-guardium-via-filebeat)
* Supported Guardium versions:
  * Guardium Data Protection: 11.3 and above
  * Guardium Insights: 3.2 and above

This is a [Logstash](https://github.com/elastic/logstash) input plug-in for the universal connector that is featured in IBM Security Guardium. It  enables Logstash to receive events from the Beats framework. The events are then sent over to corresponding filter plugin which transforms these audit logs into a [Guardium record](https://github.com/IBM/universal-connectors/blob/main/common/src/main/java/com/ibm/guardium/universalconnector/commons/structures/Record.java)  instance (which is a standard structure made out of several parts). The information is then sent over to Guardium. Guardium records include the accessor (the person who tried to access the data), the session, data, and exceptions. If there are no errors, the data contains details about the query "construct". The construct details the main action (verb) and collections (objects) involved.


## Purpose:

Specify a port, and this plugin will poll the same port on the Logstash host for any new log events.


## Usage:

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

### Configure Filebeat connection with SSL on GDP
1. Generate Certificate Authority (CA)
	1. On the Collector, run the following API to get the Certificate Authority content:

	    ```bash
        grdapi generate_ssl_key_universal_connector
        ```

	2. This API will print the content of the public Certificate Authority. Copy this certificate authority to your database source and save it as a `ca.pem` file.

2. Configure the Filebeat logstash input with the following:
```txt
input {
  beats { 
	port => 5045 
	# For SSL over Filebeat, uncomment the following lines after generating an SSL key and a certificate authority (CA) using GuardAPI (see documentation), copy the public certificate authority (CA) to your data source and adjust Filebeat configuration:
	ssl => true
	ssl_certificate => "${SSL_DIR}/cert.pem"
	ssl_key => "${SSL_DIR}/key.pem"
	type => "<datasource-type>" 
	}
}
```
Set the *datasource-type* value according to the specific filter plug-in configuration.

For detailed instructions on how to configure Filebeat connection on GI, follow the instructions [here](https://github.com/IBM/universal-connectors/blob/main/docs/Guardium%20Insights/SaaS_1.0/UC_Configuration_GI.md#filebeat-input-plug-in-configuration).

## Configuring Filebeat to push logs to Guardium

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
			- type: filestream
            - id: <ID>
			- enabled: true
		paths:
			- <path_of_log_file>
			- exclude_lines: [<if any particular event needs to be skipped>]

	where path_of_log_file is the same path that was used when enabling Database auditing. For example, /var/log/cassandra/audit/audit.log for Cassandra database
	
	• To process multi-line audit events, add the settings in same inputs section.
	
			multiline.type: pattern
			multiline.pattern: '<pattern_to_match>'
			multiline.negate: true
			multiline.match: after
	
	where pattern_to_match is the pattern, which determines the begining of every Event, so that other lines that do not begin with the same are concatenated as a single event.
			
	• Add the tags to uniquely identify the events from the rest.
			tags: ["sample_db_param"]
	
2. Configuring the output section:

		• Locate "output" in the filebeat.yml file, then add the following parameters.

		• Disable Elasticsearch output by commenting it out.

		• Enable Logstash output by uncommenting the Logstash section. For more information, see https://www.elastic.co/guide/en/beats/filebeat/current/logstash-output.html#logstash-output

    For example:

		output.logstash:
			hosts: ["127.0.0.1:5001"]
		
		• The hosts option specifies the Logstash server and the port (5001) where Logstash is configured to listen for incoming Beats connections.

		• You can set any port number except 5044, 5141, and 5000 (as these are currently reserved in Guardium v11.3 and v11.4 ).

3. Filebeat configuration for sending data with SSL


   Copy the location of the downloaded certificate authority and enter it as a pem file:
```txt
  # List of root certificates for HTTPS server verifications
  ssl.certificate_authorities: ["<PATH TO>/ca.pem.pem"]
 ```

After making changes to the filebeat configuration, restart the filebeat daemon to apply the changes:
Run:

```bash
sudo service filebeat restart
```

4. To learn how to start FileBeat, see https://www.elastic.co/guide/en/beats/filebeat/current/filebeat-installation-configuration.html#start