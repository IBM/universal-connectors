# MariaDB-Guardium Logstash filter plug-in
### Meet MariaDB
* Tested versions: 10.6
* Environment: On-premise, Iaas
* Supported inputs: Filebeat (push)
* Supported versions:
    * GDP: 11.3 and above

This is a [Logstash](https://github.com/elastic/logstash) filter plug-in for the univer***REMOVED***l connector that is featured in IBM Security Guardium. It parses events and mes***REMOVED***ges from the MariaDB audit log into a [Guardium record](https://github.com/IBM/univer***REMOVED***l-connectors/blob/main/common/src/main/java/com/ibm/guardium/univer***REMOVED***lconnector/commons/structures/Record.java) instance (which is a standard structure made out of several parts). The information is then sent over to Guardium. Guardium records include the accessor (the person who tried to access the data), the session, data, and exceptions. If there are no errors, the data contains details about the query and Guardium sniffer parse the MariaDB queries. The MariaDB plugin supports only Guardium Data Protection as of now.

The plug-in is free and open-source (Apache 2.0). It can be used as a starting point to develop additional filter plug-ins for Guardium univer***REMOVED***l connector.

## 1. Installing MariaDB on Linux

### Procedure:
1. Install MariaDB on the server with the yum command
```
Yum install mariadb* -y
```
2. Start the MariaDB service on the server with the systemctl command.
```
systemctl start mariadb
```
3. Enable the MariaDB service to start at boot on the server.
```
systemctl enable mariadb
```
4. Generate Password​ by executing "mysql_secure_installation"​ command After providing the new password user will be asked for following,  
- Remove test Database and access to it? [y/n].​User need to type ‘y’​,
- Reload privilege tables now?[y/n]​.User need to type ‘y’​.
- Enter password for connecting to MariaDB server

  [Click Here](https://computingforgeeks.com/how-to-install-mariadb-on-kali-linux/) for learn more about MariaDB configuration

## 2. Enabling the audit logs:

### Procedure:
1. To install MariaDB Audit Plugin execute the INSTALL command
``` 
INSTALL SONAME 'server_audit’;
```
[Click Here](https://mariadb.com/kb/en/mariadb-audit-plugin-installation/) for learn more about audit plugin

2. To check the list of audit plugin-related variables on the server and their values, execute the command while connected to the server
```
 SHOW GLOBAL VARIABLES LIKE 'server_audit%';
 ```
3. Once the plugin installed, set server_audit_logging=ON​ in vim /etc/my.cnf file to enable the audit log in MariaDB.

## 3. Viewing the audit logs
To view audit logs goto Location -/var/lib/mysql/*.log

### Supported audit mes***REMOVED***ges types
Default event types configure in MariaDB are Connect,Query and Table. User can update  these value by executing "SET GLOBAL server_audit_events" command. [MariaDB LogSettings](https://mariadb.com/kb/en/mariadb-audit-plugin-log-settings/)

```

mariadb> SET GLOBAL server_audit_events= 'CONNECT,QUERY,TABLE';	
mariadb> SHOW GLOBAL VARIABLES LIKE 'server_aduit%';

+-------------------------------+-----------------------+
| Variable_name                 | Value                 |
+-------------------------------+-----------------------+
| server_audit_events           | CONNECT,QUERY,TABLE   |
```
## 4. Configuring Filebeat to push logs to Guardium

## a. Filebeat installation

### Procedure:
 
  To install Filebeat on your system, follow the steps in this topic:
    https://www.elastic.co/guide/en/beats/filebeat/current/filebeat-installation-configuration.html#installation

## b. Filebeat configuration:

To use Logstash to perform additional processing on the data collected by Filebeat, we need to configure Filebeat to use Logstash. To do this, modify the filebeat.yml file which you can find inside the folder where filebeat is installed. Follow these instructions for finding the installation directory:
https://www.elastic.co/guide/en/beats/filebeat/current/directory-layout.html

### Procedure:

1. Configuring the input section :-

    • Locate "filebeat.inputs" in the filebeat.yml file, then add the following parameters. 
```
	For example:-
	   filebeat.inputs:
       - type: log   
       enabled: true
        paths:
       - <host_name/trace/DB_<DB_Name>/*.log>
       #exclude_lines: ['^DBG']
       tags: ["MariaDB_On_Premise"]
```
2. Configuring the output section:
```
    • Locate "output" in the filebeat.yml file, then add the following parameters.

    • Di***REMOVED***ble Elasticsearch output by commenting it out.

    • Enable Logstash output by uncommenting the Logstash section. For more information, see https://www.elastic.co/guide/en/beats/filebeat/current/logstash-output.html#logstash-output
```
For example:
  ```
       output.logstash:
       hosts: ["127.0.0.1:8541"]

    • The hosts option specifies the Logstash server and the port (8541) where Logstash is configured to listen for incoming Beats connections.

    •You can set any port number except 5044, 5141, and 5000 (as these are currently reserved in Guardium v11.3 and v11.4 ).
```
```
• Locate "Processors" in the filebeat.yml file and then add below attribute to get timezone of Server:
	For more information, see https://www.elastic.co/guide/en/beats/filebeat/current/add-locale.html
	For example:-
       processors:
	 - add_locale: ~
 ```

3. To learn how to start FileBeat, see https://www.elastic.co/guide/en/beats/filebeat/current/filebeat-installation-configuration.html#start
### Known issues and Solution.

#### Duplicate Records in Guardium for a single event.

There is a known issue when Guardium start getting multiple entries from Filebeat for a single record because of either Network or Firewall issue. 
The problem appears when Filebeat send an event to Guardium but don't get the acknowledgement back from Guardium due to connection failure/break because of Network or Firewall problem, in that scenario Filebeat treats it as failure and try to send the ***REMOVED***me event again. 

The best solution for this problem is to fix the Network/Firewall issues, but Filebeat has also provided a solution to fix this problem and that in context of Elasticsearch. [Click here](https://www.elastic.co/guide/en/beats/filebeat/current/filebeat-deduplication.html) for more details.

User can also try to implement the ***REMOVED***me by adding below mention lines in Filebeat and Guardium filter configuration.

Filebeat Configuration:- 

```
* Locate "Processors" in the filebeat.yml file and then add below attribute to get unique id for each event:
	For more information, see https://www.elastic.co/guide/en/beats/filebeat/current/add-id.html
	For example:-
        processors:
           - add_id: ~

 ```
Filter Configuration :-

```
* Add the below mentioned lines inside the filter configuration, which will replace the event_id & _id values with the unique id created by 
  Filebeat using add_id feature in Filebeat configuration.

mutate {
 replace => { "event_id" => "%{[@metadata][_id]}"}
 replace => { "_id" => "%{[@metadata][_id]}"}
}
	
 ```
 
#### Ignoring system logs using Filebeat.
There are cases when MariaDB generates few system logs which are either not required or having less information for Guardium. Such logs should be filtered
at Filebeat level only.

Filebeat provides a solution which can be implemented to ignore such logs, below are the steps
1. Open filebeat.yml in notepad or any OS specific text editor.
2. Search "filebeat.inputs" property.
3. Under the "filebeat.inputs" property, uncomment the exculde_lines property and place the query user wants to ignore at filebeat level
	
```
exclude_lines: ["set autocommit=0","set autocommit=1","SELECT @@tx_isolation"]
		
 ```

## 5. Configuring the MariaDB filter in Guardium
The Guardium univer***REMOVED***l connector is the Guardium entry point for native audit logs. The Guardium univer***REMOVED***l connector identifies and parses the received events, and converts them to a standard Guardium format. The output of the Guardium univer***REMOVED***l connector is forwarded to the Guardium sniffer on the collector, for policy and auditing enforcements. Configure Guardium to read the native audit logs by customizing the MariaDB template.
 
## Before you begin
* You must have permission for the S-Tap Management role. The admin user includes this role by default.
* Download the [logstash-filter_offline_plugin_mariadb.zip](MariaDBOverFilebeatPackage/logstash-filter-mariadb_guardium_filter.zip) plug-in.
* Download the plugin filter configuration file [MariaDB.conf](MariaDB.conf).

## Procedure
1. On the collector, go to Setup > Tools and Views > Configure Univer***REMOVED***l Connector.
2. Enable the connector if it is di***REMOVED***bled before uploading the UC plug-in.	
3. Click Upload File and select the offline [logstash-filter_offline_plugin_mariadb.zip](MariaDBOverFilebeatPackage/logstash-filter-mariadb_guardium_filter.zip) plug-in. After it is uploaded, click OK.
4. Click the Plus sign to open the Connector Configuration dialog box.
5. Type a name in the Connector name field.
6. Update the input section to add the details from [mariadb.conf](MariaDB.conf) file's input part, omitting the keyword "input{" at the beginning and its corresponding "}" at the end.
7. Update the filter section to add the details from [mariadb.conf](MariaDB.conf) file's filter part, omitting the keyword "filter{" at the beginning and its corresponding "}" at the end.
8. The "type" field should match in input and filter configuration section. This field should be unique for every individual connector added.
9. The "tags" parameter in the filter configuration should match the value of the attribute tags configured in the Filebeat configuration for a connector.
10. Click Save. Guardium validates the new connector, and enables the univer***REMOVED***l connector if it was
di***REMOVED***bled. After it is validated, it appears in the Configure Univer***REMOVED***l Connector page.

## 6. Limitations
 - The following important fields could not be mappped with MariaDB audit logs.
    - OS User  - Not available in Audit logs
    - ClientIP - Not avaiable in Audit Logs
    - Source Program - Not available in Audit Logs
