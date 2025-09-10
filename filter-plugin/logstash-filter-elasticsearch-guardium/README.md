# logstash-filter-elasticsearch-guardium

### Meet Elasticsearch
* Tested versions: v8.9
* Environment: On-premise
* Supported inputs: Filebeat (push)
* Supported Guardium versions:
  * Guardium 11.4 with patch p490 and above having sniffer patch p4076
  * Guardium 11.5 with patch p540 and above having sniffer patch p4076
  * Guardium 12.0 with patch p10 and above having sniffer patch p4001
  
This is a [Logstash](https://github.com/elastic/logstash) filter plug-in for the universal connector that is featured in IBM Security Guardium.It parses events and messages from the Elasticsearch audit log into a [Guardium record](https://github.com/IBM/universal-connectors/blob/main/common/src/main/java/com/ibm/guardium/universalconnector/commons/structures/Record.java) instance(which is a standard structure made out of several parts).The information is then sent over to Guardium. Guardium records include the accessor(the person who tried to access the data),the session,data and exceptions.If there are no errors,the data contains details about the query and Guardium sniffer parse the Elasticsearch queries.

The plug-in is free and open-source (Apache 2.0).It can be used as a starting point to develop additional filter plug-ins for Guardium universal connector.


## 1. Elasticsearch Configuration on RPM machine
### Procedure:
Run the following commands to configure Elasticsearch on RPM machine.
1. Install wget on RPM machine.
  
   ```sudo yum install wget```

2. Download the Elasticsearch on RPM machine.
  
   ```wget https://artifacts.elastic.co/downloads/elasticsearch/elasticsearch-8.9.1-x86_64.rpm```

3. Install Elasticseach.
   
   ```sudo rpm --install elasticsearch-8.9.1-x86_64.rpm```

**Note:** Upon running the above command,a password is generated for the elastic built-in superuser.Use this password to run queries and call APIs.


 4. Configure Elasticsearch to start automatically as the system starts.

     ```sudo /bin/systemctl daemon-reload```

     ```sudo /bin/systemctl enable elasticsearch.service```

 5. Start the Elasticseach.
  
    ```sudo systemctl start elasticsearch.service```

 6. Verify if the Elasticseach is up and running.

    ```curl -XGET -u <username:password> "https://localhost:9200/?pretty" -k```

 7. To get the Elasticsearch trial license,run the following command. 
  
    ```curl -XPOST -u <username:password> " https://localhost:9200/_license/start_trial?acknowledge=true&pretty" -k```

 8. To verify the license status, run the following command.

    ```curl -XGET -u <username:password> "https://localhost:9200/_license?pretty" -k```

**Note:** Enter the username and password that you have generated in step 3.

  9. After updating the license as trial, restart the Elasticsearch.

     ```sudo systemctl restart elasticsearch.service```
 
 10. To configure the Elasticsearch, modify the elasticsearch.yml file.
 
     ```sudo vi /etc/elasticsearch/elasticsearch.yml```

11. Uncomment and update the default values in the following code block.
 ```
-   cluster.name : my-application
-   node.name : node1
-   http.port : 9200
-   network.host: 192.168.0.1<Enter the hostname of the machine on which the Elasticsearch is installed>
```
 12. Restart the Elasticsearch.

      ```sudo systemctl restart elasticsearch.service```



## 2. Enabling Audit

 1.   To enable the Elasticsearch audit log, add the following properties in the elasticsearch.yml file.
```
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
``` 

**Note:** To exclude unwanted logs for specific user, keep that user.name value to users list.
      
For example:-

    users:["kibana_system","_system","<user.name value>"]

**Note:** List of value contains for **xpack.security.audit.logfile.events.include** 

    access_denied,access_granted,anonymous_access_denied,authentication _failed,_all,connection_denied,tampered_request,run_as_denied,run_as_granted,security_config_change      

 2. After enabling the audit log, restart the Elasticsearch.
             
    ```sudo systemctl restart elasticsearch.service```


## 3. Query Execution using SQL CLI

While using SQL CLI, you may get the SSL certificate error. Complete the following steps to resolve this error.
1. Update the highlighted configurations in elasticsearch.yml file as follows:

        xpack.security.http.ssl:
          enabled: false  
          keystore.path: certs/http.p12
        xpack.security.transport.ssl:
          enabled: false
          verification_mode: certificate
          keystore.path: certs/transport.p12
          truststore.path: certs/transport.p12

 2. Restart the Elasticsearch.

    ```sudo systemctl restart elasticsearch.service```

3. Go to the elasticsearch directory.

    ```cd /usr/share/elasticsearch```

4. Run the elasticsearch SQL CLI.

    ```sudo ./bin/elasticsearch-sql-cli http://<username:password>@localhost:9200```

5. A few sample SQL queries that can be run with the SQL CLI are provided below.

   ```show tables;```

   ```select * from hcltestingteam;```

   ```describe hcltestingteam;```

  **Note:** Using SQL CLI, user can only run the SQL type of queries.

## 4. Running queries by using CURL command.
User can execute any type of elasticsearch supported queries using curl commands.Below are the few sample queries executed through curl command.


  Query 1:
    
    curl -XPOST  -u <username:password>  
    "http://localhost:9200/hcltestingteam/_doc/1?pretty" -H 'Content-Type: application/json' -d'
    {
      "firstname": "Jennifer",
      "lastname": "Walters"
     }
      ‘ –k

  Query 2:

    curl -XPOST -u <username:password> 
    "http://localhost:9200/_sql?format=txt&pretty" -H 'Content-Type: application/json' -d'
    {
     "query": "SELECT * FROM hcltestingteam"
    }
    ' -k

Query 3:

    curl -XPOST -u <username:password> "http://localhost:9200/_sql?format=txt&pretty" -H 'Content-Type: application/json' -d'
    {
     "query": "describe hcltestingteam"
    }
    ‘ –k

Query 4:

    curl -XPUT -u <username:password> "http://localhost:9200/_ingest/pipeline/my-pipeline-id?pretty" -H 'Content-Type: application/json' -d'
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

    curl -XGET -u <username:password>
    "http://localhost:9200/_ingest/pipeline/my-pipeline-id?pretty" -k

**Note:** username and password needs to be updated in above curl commands.

## 5. Viewing the audit logs
 • User can see Audit log file in log directory with name

` 
<clustername>_audit.json
`

 • If the log directory is configured in elasticsearch.yml file is /var/log/elasticsearch then sample  audit log file location would be

  `
    /var/log/elasticsearch/<clustername>_audit.json
`

 • If cluster name is  my-application then sample audit log file location would be

`
    /var/log/elasticsearch/my-application_audit.json
`

## 6. Logstash_Input_Filebeat Configuration
### Procedure:
1. To install Filebeat on your system, follow the steps in this topic:
    https://www.elastic.co/guide/en/beats/filebeat/current/filebeat-installation-configuration.html#installation

2. Filebeat configuration:
To use Logstash to perform additional processing on the data collected by Filebeat,configure Filebeat to use Logstash. To do this, modify the filebeat.yml file which you can find inside the folder where filebeat is installed. Follow these instructions for finding the installation directory:
https://www.elastic.co/guide/en/beats/filebeat/current/directory-layout.html
```
    • Locate "filebeat.inputs" in the filebeat.yml file and then use the "paths" attribute to set the location of the elasticsearch audit logs:
	For more information, see https://www.elastic.co/guide/en/beats/filebeat/current/configuration-filebeat-options.html#configuration-filebeat-options
       
   For example:-
    filebeat.inputs:
    - type: filestream
    - id: <ID>   
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
1. The following important fields are not mapped with the Elasticsearch audit logs

- Accessor>sourceProgram : Not available with logs.
- dbName : Not available in audit logs.
- Accessor >clientHostName : Not available in audit logs
- SQL Syntax Error logs are not available in audit logs.

2. User can perform crud operation only through rest api,It can not be able to perform with sql cli. 

## 8. Configuring the Elasticsearch filter in Guardium
The Guardium universal connector is the Guardium entry point for native audit logs. The Guardium universal connector identifies and parses the received events, and converts them to a standard Guardium format. The output of the Guardium universal connector is forwarded to the Guardium sniffer on the collector, for policy and auditing enforcements. Configure Guardium to read the native audit logs by customizing the Elasticsearch template.

### Before you begin
* Configure the policies you require. See [policies](https://github.com/IBM/universal-connectors/#policies) for more information.
* You must have permission for the S-Tap Management role. The admin user includes this role by default.
* Download the [guardium_logstash-offline-plugins-elasticsearch.zip](ElasticsearchOverFilebeatPackage/guardium_logstash-offline-plugins-elasticsearch.zip) plug-in.
* Download the plugin filter configuration file [elasticsearch.conf](elasticsearch.conf).

### Procedure
1. On the collector, go to Setup > Tools and Views > Configure Universal Connector.
2. Enable the connector if it is already disabled, before proceeding uploading of the UC.
3. Click Upload File and select the offline [guardium_logstash-offline-plugins-elasticsearch.zip](ElasticsearchOverFilebeatPackage/guardium_logstash-offline-plugins-elasticsearch.zip) plug-in. After it is uploaded, click OK.
4. Click the Plus sign to open the Connector Configuration dialog box.
5. Type a name in the Connector name field.
6. Update the input section to add the details from [elasticsearch.conf](elasticsearch.conf) file's input part, omitting the keyword "input{" at the beginning and its corresponding "}" at the end.
7. Update the filter section to add the details from [elasticsearch.conf](elasticsearch.conf) file's filter part, omitting the keyword "filter{" at the beginning and its corresponding "}" at the end.
8. The "type" field should match in the input and filter configuration section. This field should be unique for every individual connector added.
9. Click Save. Guardium validates the new connector, and enables the universal connector if it was disabled. After it is validated, it appears in the Configure Universal Connector page.
