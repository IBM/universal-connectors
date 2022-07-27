# MariaDB-Guardium Logstash filter plug-in

This is a Logstash filter plug-in for the universal connector that is featured in IBM Security Guardium. It parses events and messages from the MariaDB audit log into a Guardium record instance (which is a standard structure made out of several parts). The information is then sent over to Guardium. Guardium records include the accessor (the person who tried to access the data), the session, data, and exceptions. If there are no errors, the data contains details about the query and Guardium sniffer parse the MariaDB queries. The MariaDB plugin supports only Guardium Data Protection as of now.

## MariaDB Configuration on Linux
## Procedure:
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

## MariaDB Audit Plugin - Installation and Configuration
## Procedure:
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
## Create MariaDB Database and Table​
## Procedure
1. create database
2. create table and insert data in table

[Click Here](https://mariadb.com/kb/en/create-database/) for learn more about create database

## View the MariaDB audit logs
To view audit logs goto Location -/var/lib/mysql/*.log

## MariaDB Server Audit Log Sample
```
"20220314 08:29:51,ip-172-31-44-208.ap-south-1.compute.internal,root,localhost,122,1779,QUERY,testDB,'SELECT DATABASE()',0"
"20220314 08:29:51,ip-172-31-44-208.ap-south-1.compute.internal,root,localhost,122,1781,QUERY,testDB,'show databases',0"
"20220314 08:29:51,ip-172-31-44-208.ap-south-1.compute.internal,root,localhost,122,1782,QUERY,testDB,'show tables',0"

```

[Click Here](https://mariadb.com/kb/en/mariadb-audit-plugin-log-format/) for learn more about audit logs.

## Supported audit messages types
Default event types configure in MariaDB are Connect,Query and Table. User can update  these value by executing "SET GLOBAL server_audit_events" command. [MariaDB LogSettings](https://mariadb.com/kb/en/mariadb-audit-plugin-log-settings/)

```

mariadb> SET GLOBAL server_audit_events= 'CONNECT,QUERY,TABLE';	
mariadb> SHOW GLOBAL VARIABLES LIKE 'server_aduit%';

+-------------------------------+-----------------------+
| Variable_name                 | Value                 |
+-------------------------------+-----------------------+
| server_audit_events           | CONNECT,QUERY,TABLE   |
```
## Logstash_Input_Filebeat Configuration:
## Procedure:
1. To install Filebeat on your system, follow the steps in this topic:
    https://www.elastic.co/guide/en/beats/filebeat/current/filebeat-installation-configuration.html#installation

2. Filebeat configuration:
To use Logstash to perform additional processing on the data collected by Filebeat, we need to configure Filebeat to use Logstash. To do this, modify the filebeat.yml file which you can find inside the folder where filebeat is installed. Follow these instructions for finding the installation directory:
https://www.elastic.co/guide/en/beats/filebeat/current/directory-layout.html
```
    • Locate "filebeat.inputs" in the filebeat.yml file and then use the "paths" attribute to set the location of the mariadb audit logs:
	For more information, see https://www.elastic.co/guide/en/beats/filebeat/current/configuration-filebeat-options.html#configuration-filebeat-options
       
	For example:-
	   filebeat.inputs:
       - type: log   
       enabled: true
        paths:
       - <host_name/trace/DB_<DB_Name>/*.log>
       #exclude_lines: ['^DBG']
       tags: ["MariaDB_On_Premise"]
	
    • While editing the Filebeat configuration file, disable Elasticsearch output by commenting it out. Then enable Logstash output by uncommenting the Logstash section. For more information, see https://www.elastic.co/guide/en/beats/filebeat/current/logstash-output.html#logstash-output
		
	For example:-
       output.logstash:
       hosts: ["127.0.0.1:5044"]
```
The hosts option specifies the Logstash server and the port (5044) where Logstash is configured to listen for incoming Beats connections. You can set any port number except 5044, 5141, and 5000 (as these are currently reserved as ports for the MongoDB incoming log).

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
The problem appears when Filebeat send an event to Guardium but don't get the acknowledgement back from Guardium due to connection failure/break because of Network or Firewall problem, in that scenario Filebeat treats it as failure and try to send the same event again. 

The best solution for this problem is to fix the Network/Firewall issues, but Filebeat has also provided a solution to fix this problem and that in context of Elasticsearch. [Click here](https://www.elastic.co/guide/en/beats/filebeat/current/filebeat-deduplication.html) for more details.

User can also try to implement the same by adding below mention lines in Filebeat and Guardium filter configuration.

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

## Limitation
1. The following important fields could not be mappped with MariaDB audit logs.
    - OS User  - Not available in Audit logs
	- ClientIP - Not avaiable in Audit Logs
	
	
## Sample Configuration
Below is a copy of the filter scope included [MariaDB.conf](MariaDB.conf) that shows a basic
configuration for this plugin.
#### Input part:
```
input 
{
  beats 
  {
    port => 8541
    type => "mariadb"
   }
}
```
#### Filter part:
```
filter{
if [type] == "mariadb" and "MariaDB_On_Premise" in [tags][0]{
mutate {
add_field => {"server_Ip" => "%{[host][ip][0]}"} 
}
mutate {add_field => { "TZ" => "%{[event][timezone]}" }}

# In case of duplicate records enable add_id feature in Filebeat configuration and uncomment below mentioned lines replacing event_id and _id.
#mutate {
# replace => { "event_id" => "%{[@metadata][_id]}"}
# replace => { "_id" => "%{[@metadata][_id]}"}
#}

grok { match => { "TZ" => "(?<minutes>[^:]*):(?<seconds>[^,]*)" } }
grok { match => { "minutes" => "(?<offset_diff>[^[0-9]]*)%{GREEDYDATA:actual_minutes}" } }ruby { code => "event.set('minutes1', event.get('actual_minutes').to_i * 60)" }
ruby { code => "event.set('offset1', event.get('minutes1') +  event.get('seconds').to_i)" }mutate { add_field => { "totalOffset" => "%{offset_diff}%{offset1}" } }
grok
{
match => {"message" => "(?<timestamp>[^[A-Z][a-z]]*),(?<serverhost>[^\s]*),(?<username>[^\s]*),(?<hostname>[^\s]*),(?<connectionid>[^\s]*),(?<queryid>[^\s]*),(?<operation>[^\s]*),(?<database>[^\s]*),(?:%{GREEDYDATA:object}),(?<retcode>[^\s]*)"}
}
mariadb_guardium_filter{}
}
}

```
## Assumptions
* Logs are successfully pushed to the logstash from filebeat to the respective services.
* Logstash will pull log only from filebeat.


## Configuring the MariaDB filter in Guardium
The Guardium universal connector is the Guardium entry point for native audit logs. The Guardium universal connector identifies and parses the received events, and converts them to a standard Guardium format. The output of the Guardium universal connector is forwarded to the Guardium sniffer on the collector, for policy and auditing enforcements. Configure Guardium to read the native audit logs by customizing the MariaDB template.
 
## Before you begin
* You must have permission for the S-Tap Management role. The admin user includes this role by default.
* Download the [logstash-filter_offline_plugin_mariadb.zip](MariaDBOverFilebeatPackage/logstash-filter-mariadb_guardium_filter.zip) plug-in.
* Download the plugin filter configuration file [MariaDB.conf](MariaDB.conf).


## Procedure
1. On the collector, go to Setup > Tools and Views > Configure Universal Connector.
2. Enable the connector if it is disabled before uploading the UC plug-in.	
3. Click Upload File and select the offline logstash-filter_offline_plugin_mariadb.zip plug-in. After it is uploaded, click OK.
4. Click the Plus sign to open the Connector Configuration dialog box.
5. Type a name in the Connector name field.
6. Update the input section to add the details from mariadb.conf file's input part, omitting the keyword "input{" at the beginning and its corresponding "}" at the end.
7. Update the filter section to add the details from mariadb.conf file's filter part, omitting the keyword "filter{" at the beginning and its corresponding "}" at the end.
8. The "type" field should match in input and filter configuration section. This field should be unique for every individual connector added.
9. The "tags" parameter in the filter configuration should match the value of the attribute tags configured in the Filebeat configuration for a connector.
10. Click Save. Guardium validates the new connector, and enables the universal connector if it was
disabled. After it is validated, it appears in the Configure Universal Connector page.

### Set-up dev environment
Before you can build & create an updated GEM of this filter plugin, set up your environment as follows: .
1. Clone Logstash codebase & build its libraries as as specified in [How to write a Java filter plugin](https://github.com/logstash-plugins/logstash-filter-java_filter_example). Use branch 7.x (this filter was developed alongside 7.14 branch).  
2. Create _gradle.properties_ and add LOGSTASH_CORE_PATH variable with the path to the logstash-core folder you created in the previous step. For example: 

    ```LOGSTASH_CORE_PATH=/Users/taldan/logstash76/logstash-core```
	
3. Clone the [github-uc-commons](https://github.com/IBM/guardium-universalconnector-commons) project and build a JAR from it according to instructions specified there. The project contains Guardium Record structure you need to adjust, so Guardium universal connector can eventually feed your filter's output into Guardium. 
4. Edit _gradle.properties_ and add a GUARDIUM_UNIVERSALCONNECTOR_COMMONS_PATH variable with the path to the built JAR. For example:
    ```GUARDIUM_UNIVERSALCONNECTOR_COMMONS_PATH=../guardium-universalconnector-commons/build/libs```
If you'd like to start with the most simple filter plugin, we recommend to follow all the steps in [How to write a Java filter plugin](https://www.elastic.co/guide/en/logstash/7.16/java-filter-plugin.html) tutorial.

### Build plugin GEM
To build this filter project into a GEM that can be installed onto Logstash, run 
    $ ./gradlew.unix gem --info
Sometimes, especially after major changes, clean the artifacts before you run the build gem task:
    $ ./gradlew.unix clean
### Install
To install this plugin on your local developer machine with Logstash installed, run:
    
    $ ~/Downloads/logstash-7.14/bin/logstash-plugin install ./logstash-filter-mariadb_guardium_filter-?.?.?.gem
**Notes:** 
* Replace "?" with this plugin version
* logstash-plugin may not handle relative paths well, so try to install the gem from a simple path, as in the example above. 
### Run on local Logstash
To test your filter using your local Logstash installation, run 
    $ ~/Downloads/logstash-7.14/bin/logstash -f mariadb-test.conf
    
This configuration file generates an Event and send it through the installed filter plugin.
