# Mysql-Guardium Logstash filter plug-in
This is a [Logstash](https://github.com/elastic/logstash) filter plug-in for the universal connector that is featured in IBM Security Guardium. It parses events and messages from the MySQL audit into a Guardium record instance (which is a standard structure made out of several parts). The information is then sent over to Guardium. [Guardium records](https://github.com/IBM/universal-connectors/blob/main/common/src/main/java/com/ibm/guardium/universalconnector/commons/structures/Record.java) include the accessor (the person who tried to access the data), the session, data, and exceptions. If there are no errors, the data contains details about the query "construct". The contstruct details the main action (verb) and collections (objects) involved.

The plug-in is free and open-source (Apache 2.0). It can be used as a starting point to develop addition filter plug-ins for Guardium universal connector.

## 1. Configuring the Mysql server
There are multiple ways to install a MySQL on-premise server. For this example, we will assume that we already have a working MySQL setup.
## 2. Installing and enabling auditing
[Install the audit log plug-in](https://dev.mysql.com/doc/mysql-secure-deployment-guide/5.7/en/secure-deployment-audit.html), and verify the following two lines in the my.cnf file:
####
      plugin-load = audit_log.so
      audit_log_format=JSON
####

The log file is:
####
      /home/<os_user>/mysql/data/audit.log
####
Restart the mysql daemon. <br />
Run the following two SQLs to install the default filter to get every log:
####
      SELECT audit_log_filter_set_filter('log_all', '{ "filter": { "log": true }}');
      SELECT audit_log_filter_set_user('%', 'log_all');
####

## 3. Configuring Filebeat to push logs to Guardium

## a. Filebeat installation

### Procedure:

1. To install Filebeat on your system, follow the steps in this topic:
   https://www.elastic.co/guide/en/beats/filebeat/current/filebeat-installation-configuration.html#installation

## b. Filebeat configuration:

To use Logstash to perform additional processing on the data collected by Filebeat, we need to configure Filebeat to use Logstash. To do this, modify the filebeat.yml file which you can find inside the folder where Filebeat is installed. Follow these instructions for finding the installation directory:
https://www.elastic.co/guide/en/beats/filebeat/current/directory-layout.html

### Procedure:

1. Configuring the input section :-

   • Locate "filebeat.inputs" in the filebeat.yml file, then add the following parameters.

       filebeat.inputs:
   		   - type: log   
   		   - enabled: true
   	   paths:
   		   - <path_of_log_file as specified in /path/to/audit.log file>
   		   - exclude_lines: ['AuditLogManager']

   where path_of_log_file is the same path that was used when enabling MySQL auditing. For example, /home/mysql8_ent/mysql/data/audit.log .

   • Add the tags to uniquely identify the MySQL on-prem events from the rest.
   tags: ["<tag_name>>"]

2. Configuring the output section:

   • Locate "output" in the filebeat.yml file, then add the following parameters.

   • Disable Elasticsearch output by commenting it out.

   • Enable Logstash output by uncommenting the Logstash section. For more information, see https://www.elastic.co/guide/en/beats/filebeat/current/logstash-output.html#logstash-output

   For example:
   ### 
   	   output.logstash:
   		   hosts: ["127.0.0.1:5001"]
   	
   	 • The hosts option specifies the Logstash server and the port (5001) where Logstash is configured to listen for incoming Beats connections.

   	 • You can set any port number except 5044, 5141, and 5000 (as these are currently reserved in Guardium v11.3 and v11.4 ).

3. To learn how to start FileBeat, see https://www.elastic.co/guide/en/beats/filebeat/current/filebeat-installation-configuration.html#start


## 4. Configuring the MySQL filters in Guardium Data Protection (GDP)

The Guardium universal connector is the Guardium entry point for native audit logs.
The universal connector identifies and parses received events, and converts them to a standard Guardium format.
The output of the Guardium universal connector is forwarded to the Guardium sniffer on the collector, for policy and auditing enforcements.

### Before you begin
• You must have permission for the S-Tap Management role. The admin user includes this role, by default.

### Procedure

1. On the collector, go to ```Setup``` > ```Tools and Views``` > ```Configure Universal Connector```.
2. First, enable the Guardium universal connector, if it is currently disabled.
3. Click the Plus sign to open the ```Connector Configuration``` dialog box.
4. Type a name in the ```Connector name``` field.
5. Select ```MySQL using Filebeat``` or ```MySQL using Syslog``` for the ```Connector template```.
6. Follow the notes in the ```Input configuration``` section.
7. The "type" fields should match in the input and the filter configuration sections. This field should be unique for every individual connector added.
8. Click ```Save```. Guardium validates the new connector, and enables the universal connector if it was disabled. After it is validated, it appears in the ```Configure Universal Connector``` page.

## 5. Configuring the MySQL filters in Guardium Insights
To configure this plug-in for Guardium Insights, follow [this guide.](https://github.com/IBM/universal-connectors/blob/main/docs/UC_Configuration_GI.md)
In the ```Input configuration``` section, refer to the Filebeat section.


## Notes
* The filter supports events sent through Syslog or Filebeat. It relies on the "mysql_audit_log:" prefix in the event message for the JSON portion of the audit to be parsed.
* Field _server_hostname_ (required) - Server hostname is expected (extracted from the second field of the syslog message).
* Field _server_ip_ - States the IP address of the MySQL server, if it is available to the filter plug-in. The filter will use this IP address instead of localhost IP addresses that are reported by MySQL, if actions were performed directly on the database server.
* The client "Source program" is not available in messages sent by MySQL. This is because this data is sent only in the first audit log message upon database connection - and the filter plug-in doesn't aggregate data from different messages.
* If events with "(NONE)" local/remote IP addresses are not filtered, the filter plug-in will convert the IP to "0.0.0.0", as a valid format for IP is needed. However, this is atypical, since messages without users are filtered out.
* Events in the filter are not removed, but tagged if not parsed (see [Filter result](#filter-result), below).
*  If the dbname is not coming from the command line, it will not get populated. If you want to see the dbname, either  send a use statement or send it on command line.
* *IPv6* addresses are typically supported by the MySQL and filter plug-ins, however this is not fully supported by the Guardium pipeline.
* It is supported on Enterprise version only
* Use JSON format for native logging (configurable in the database server). XML is not supported as of now.
