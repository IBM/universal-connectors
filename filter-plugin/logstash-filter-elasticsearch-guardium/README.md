# logstash-filter-elasticsearch-guardium

### Meet Elasticsearch
* Tested versions: v9
* Environment: On-premise
* Supported inputs: Filebeat (push)
* Supported Guardium versions:
  * Guardium Data Protection: 12.1 and later
  
This is a [Logstash](https://github.com/elastic/logstash)  filter plug-in for the universal connector that is featured in IBM Security Guardium. It parses events and messages from the Elasticsearch audit log into a [Guardium record](https://github.com/IBM/universal-connectors/blob/main/common/src/main/java/com/ibm/guardium/universalconnector/commons/structures/Record.java) instance (which is a standard structure made out of several parts). The information is then sent over to Guardium. Guardium records include the accessor (the person who tried to access the data), the session, data, and exceptions. If there are no errors, the data contains details about the query and Guardium sniffer parse the Elasticsearch queries.

The plug-in is free and open-source (Apache 2.0). It can be used as a starting point to develop additional filter plug-ins for Guardium universal connector.


## 1. Elasticsearch Configuration on RPM machine
### Procedure:
1. Install wget on RPM machine
  
   sudo yum install wget

2. Run below command for downloading the  Elasticsearch on RPM machine.
  
   wget https://artifacts.elastic.co/downloads/elasticsearch/elasticsearch-8.9.1-x86_64.rpm

3. Run below commands for installing the Elasticsearch
   
   sudo rpm --install elasticsearch-8.9.1-x86_64.rpm

- Password will be generated for elastic built-in superuser while executing above command.
- This password will be required while executing the queries or calling the APIs.


 4. To configure Elasticsearch to start automatically when the system boots up, run the following commands

     sudo /bin/systemctl daemon-reload

     sudo /bin/systemctl enable elasticsearch.service

 5. To start the Elasticsearch run the below command
  
    sudo systemctl start elasticsearch.service

 6. To Check that Elasticsearch is running use the below commands

    curl -XGET -u elastic:_AD-LeuMuM6WzKE0zRdx "https://localhost:9200/?pretty" -k 

   **Note:** In above command we have used below credential
 
       username-> elastic 
       password-> _AD-LeuMuM6WzKE0zRdx

 7. For trial license run below api 
  
    curl -XPOST -u elastic:_AD-LeuMuM6WzKE0zRdx " https://localhost:9200/_license/start_trial?acknowledge=true&pretty" -k

 8. To check the status of license run below api

    curl -XGET -u elastic:_AD-LeuMuM6WzKE0zRdx "https://localhost:9200/_license?pretty" -k

  9. After updating the license as trial restart the Elasticsearch using below command

     sudo systemctl restart elasticsearch.service
 
 10. To configure the Elasticsearch, we need to modify the elasticsearch.yml  file, for that run the below command
 
     sudo vi /etc/elasticsearch/elasticsearch.yml

11. User have to uncomment and update below default properties values in configuration file      (elasticsearch.yml).
 
-   #cluster.name : my-application
-   #node.name : node1
- 	#http.port : 9200
-   #network.host: 192.168.0.1(provide the host name of the machine where elasticsearch is installed)

 12. After modifying the above configuration   file (elasticsearch.yml),restart the Elasticsearch using the below command

      sudo systemctl restart elasticsearch.service




## 2. Enabling Auditing

 1.   To enable Elasticsearch audit log you have to add following properties in elasticsearch.yml 
file.

- xpack.security.audit.enabled : true   
- xpack.security.audit.logfile.events.include:["authentication_success","authentication_failed"]
- xpack.security.audit.logfile.events.emit_request_body : true
- xpack.security.audit.logfile.emit_node_name : true
- xpack.security.audit.logfile.emit_node_host_address : true
- xpack.security.audit.logfile.emit_node_host_name: true
- xpack.security.audit.logfile.emit_node_id: true
- xpack.license.self_generated.type: trial
- xpack.security.audit.logfile.events.ignore_filters:​
    systemlogs:​   
      users: ["kibana_system", "_system"]

Note: To exclude unwanted logs for specific user, keep that user.name value to users list.
      
For example:-

    users:["kibana_system","_system","<user.name value>"]

Note: List of value contains for **xpack.security.audit.logfile.events.include** 

    access_denied,access_granted,anonymous_access_denied,authentication _failed,_all,connection_denied,tampered_request,run_as_denied,run_as_granted,security_config_change      

 2. After enabling audit log restart the Elasticsearch
             
    sudo systemctl restart elasticsearch.service


## 3. Query Execution using SQL CLI
1. While using SQL CLI  we are getting SSL certificate error, To resolve this error we are
changing below highlighted configuration in elasticsearch.yml file

        xpack.security.http.ssl:
          enabled: false  
          keystore.path: certs/http.p12
        xpack.security.transport.ssl:
          enabled: false
          verification_mode: certificate
          keystore.path: certs/transport.p12
          truststore.path: certs/transport.p12

 2. After configuration changes we need to restart the elasticsearch using below command.

    sudo systemctl restart elasticsearch.service

3. We need to run below command for going to elasticsearch directory

    cd  /usr/share/elasticsearch

4. To run the elasticsearch sql cli, we need to run below command.

    sudo ./bin/elasticsearch-sql-cli http://elastic:_AD- LeuMuM6WzKE0zRdx@localhost:9200

5. Below are few sample sql queries example executing through sql cli .

   Query 1:
   show tables;

   Query 2:
   select  * from hcltestingteam;

   Query 3:
   describe hcltestingteam;

  Note: Using  sql cli  user can execute only sql queries.

## 4. Query Execution using CURL command
User can execute any type of elasticsearch supported queries using curl commands. Below are  the few sample queries executed through curl command.


  Query 1:
    
    curl -XPOST  -u elastic:_AD-LeuMuM6WzKE0zRdx  
    "http://localhost:9200/hcltestingteam/_doc/1?pretty" -H 'Content-Type: application/json' -d'
    {
      "firstname": "Ravi",
      "lastname": "Shukla"
     }
      ‘ –k

  Query 2:

    curl -XPOST -u elastic:_AD-LeuMuM6WzKE0zRdx  
    "http://localhost:9200/_sql?format=txt&pretty" -H 'Content-Type: application/json' -d'
    {
     "query": "SELECT * FROM hcltestingteam"
    }
    ' -k

Query 3:

    curl -XPOST -u elastic:_AD-LeuMuM6WzKE0zRdx "http://localhost:9200/_sql?format=txt&pretty" -H 'Content-Type: application/json' -d'
    {
     "query": "describe hcltestingteam"
    }
    ‘ –k

Query 4:

    curl -XPUT -u elastic:_AD-LeuMuM6WzKE0zRdx"http://localhost:9200/_ingest/pipeline/my-pipeline-id?pretty" -H 'Content-Type: application/json' -d'
    {
    "description" : "My optional pipeline description",
    "processors" : [
      {
      "set" : {
        "description" : "My optional processor description",
        "field": "my-keyword-field",
        "value": "foo"
          }
       }
     ]
    }
    ‘ –k

Query 5:

    curl -XGET -u elastic:_AD-LeuMuM6WzKE0zRdx
    "http://localhost:9200/_ingest/pipeline/my-pipeline-id?pretty" -k


## 5. Viewing the Audit logs
 1. User can see Audit log file in log directory with name 
<<clustername>clustername>_audit.json

2.  If the log directory is configured in elasticsearch.yml file is /var/log/elasticsearch then sample  audit log file location would be

    
    /var/log/elasticsearch/<<clustername>clustername>_audit.json

 3. If cluster name is  my-application then sample audit log file location would be 

    /var/log/elasticsearch/my-application_audit.json

## 6. Logstash_Input_Filebeat Configuration
### Procedure:
1. To install Filebeat on your system, follow the steps in this topic:
    https://www.elastic.co/guide/en/beats/filebeat/current/filebeat-installation-configuration.html#installation

2. Filebeat configuration:
To use Logstash to perform additional processing on the data collected by Filebeat, we need to configure Filebeat to use Logstash. To do this, modify the filebeat.yml file which you can find inside the folder where filebeat is installed. Follow these instructions for finding the installation directory:
https://www.elastic.co/guide/en/beats/filebeat/current/directory-layout.html
```
    • Locate "filebeat.inputs" in the filebeat.yml file and then use the "paths" attribute to set the location of the elasticsearch audit logs:
	For more information, see https://www.elastic.co/guide/en/beats/filebeat/current/configuration-filebeat-options.html#configuration-filebeat-options
       
   For example:-
    filebeat.inputs:
    - type: log   
      enabled: true
      paths:
       - /var/log/elasticsearch/<clustername>_audit.json
      #exclude_lines: ['^DBG']
      tags: ["Elasticsearch"]
	
    • While editing the Filebeat configuration file, disable Elasticsearch output by commenting it out. Then enable Logstash output by uncommenting the Logstash section. For more information, see https://www.elastic.co/guide/en/beats/filebeat/current/logstash-output.html#logstash-output
		
    For example:-
       output.logstash:
       hosts: ["hcl-test7.isslab.usga.ibm.com:8684"]
```
The hosts option specifies the Logstash server and the port (8684) where Logstash is configured to listen for incoming Beats connections. You can set any port number except 5044, 5141, and 5000 (as these are currently reserved in Guardium v11.3 and v11.4).

```
• Locate "Processors" in the filebeat.yml file and then add below attribute to get timezone of Server:
	For more information, see https://www.elastic.co/guide/en/beats/filebeat/current/add-locale.html
	For example:-
       processors:
	 - add_locale: ~
 ```

3. To learn how to start FileBeat, see https://www.elastic.co/guide/en/beats/filebeat/current/filebeat-installation-configuration.html#start


## 7. Limitations
1.	The following important fields couldn't be mapped with Elasticsearch audit logs:

- Accessor>sourceProgram : Not available with logs.
- dbName : Not available in audit logs.
- Accessor >clientHostName : Not available in audit logs
- SQL Syntax Error logs are not available in audit logs.

2.	SQL queries supports only below commands 

     DESCRIBE TABLE

     SELECT

     SHOW CATALOGS

     SHOW COLUMNS

     SHOW FUNCTIONS

     SHOW TABLES


## 8. Configuring the Elasticsearch filter in Guardium
The Guardium universal connector is the Guardium entry point for native audit logs. The Guardium universal connector identifies and parses the received events, and converts them to a standard Guardium format. The output of the Guardium universal connector is forwarded to the Guardium sniffer on the collector, for policy and auditing enforcements. Configure Guardium to read the native audit logs by customizing the Elasticsearch template.

### Before you begin
* Configure the policies you require. See [policies](https://github.com/IBM/universal-connectors/#policies) for more information.
* You must have permission for the S-Tap Management role. The admin user includes this role by default.
* Download the [guardium_logstash-offline-plugin-elasticsearch.zip](ElasticsearchOverFilebeatPackage/guardium_logstash-offline-plugins-elasticsearch.zip) plug-in.
* Download the plugin filter configuration file [elasticsearch.conf](elasticsearch.conf).

### Procedure
1. On the collector, go to Setup > Tools and Views > Configure Universal Connector.
2. Enable the connector if it is already disabled, before proceeding uploading of the UC.
3. Click Upload File and select the offline [guardium_logstash-offline-plugin-elasticsearch.zip](ElasticsearchOverFilebeatPackage/guardium_logstash-offline-plugins-elasticsearch.zip) plug-in. After it is uploaded, click OK.
4. Click the Plus sign to open the Connector Configuration dialog box.
5. Type a name in the Connector name field.
6. Update the input section to add the details from [elasticsearch.conf](elasticsearch.conf) file's input part, omitting the keyword "input{" at the beginning and its corresponding "}" at the end.
7. Update the filter section to add the details from [elasticsearch.conf](elasticsearch.conf) file's filter part, omitting the keyword "filter{" at the beginning and its corresponding "}" at the end.
8. The "type" field should match in the input and filter configuration section. This field should be unique for every individual connector added.
9. Click Save. Guardium validates the new connector, and enables the universal connector if it was disabled. After it is validated, it appears in the Configure Universal Connector page.
