# Apache Solr Azure-Guardium Logstash filter plug-in

This is a [Logstash](https://github.com/elastic/logstash) filter plug-in for the universal connector that is featured in IBM Security Guardium. It parses events and messages from the Apache Solr logs into a [Guardium record](https://github.com/IBM/universal-connectors/blob/main/common/src/main/java/com/ibm/guardium/universalconnector/commons/structures/Record.java) instance (which is a standard structure made out of several parts). The information is then sent over to Guardium. Guardium records include the accessor (the person who tried to access the data), the session, data, and exceptions. If there are no errors, the data contains details about the query "construct". The construct details the main action (verb) and collections (objects) involved.The Apache Solr Azure plugin only supports Guardium Data Protection as of now.

The plug-in is free and open-source (Apache 2.0). It can be used as a starting point to develop additional filter plug-ins for Guardium universal connector.

## 1. Installing Apache Solr

Create a Linux virtual machine in the Azure portal.

You can learn more about Virtual Machine Creation [here](https://docs.microsoft.com/en-us/azure/virtual-machines/linux/quick-create-portal)

### Procedure
```
1.Install Java by running the following command:
        $ sudo apt install default-jre
2.Run the below command to check the java version:
        $ java –version
3.In order for Solr to work as expected, the user needs to have the lsof command installed as well.
        The lsof command stands for "list open files".  Run the following command for lsof installation:
        $ sudo apt install lsof
4.Run the following commands to download the Solr installation files:
        $ cd /usr/src
        $ sudo apt install wget
        $ sudo apt-get install wget
        $ sudo wget https://archive.apache.org/dist/lucene/solr/8.6.0/solr-8.6.0.tgz
        $ sudo tar -xzvf solr-8.6.0.tgz
5.Run the Solr installation script
        $ cd solr-8.6.0/bin
        $ sudo ./install_solr_service.sh ../../solr-8.6.0.tgz
```
### Launching Apache Solr
```
1.Once the script completes, Solr will be installed as a service and run in the background on the user's server (on port 8983). To verify, run:
       $ sudo service solr status
2.The Solr can be run in 2 different modes, You can only use one mode at a time:
2.1.Standalone mode :An index is stored on a single computer and the setup is called a core.There can be multiple cores or indexes here.
To Launch Solr in Standalone Mode:
       $ cd /opt/solr-8.6.0
       $ sudo bin/solr start -force 
2.2.SolrCloud mode:  An index is distributed across multiple computers or even multiple server instances on one computer. Groups of documents here are called collections.
 To Launch Solr in SolrCloud Mode:
       $ cd /opt/solr-8.6.0
       $ sudo bin/solr start -e cloud -force
```

### Adding an inbound port rule
1. Go to the virtual machine.
2. In the left-nav, select Networking.
3. The rules will be displayed on right side of the portal.
4. Select Inbound port rules ,then select Add.
5. Add your port(on which solr is running, default is 8983) under Destination Port ranges.
6. Click Add to create the rule.

### Login to the Solr Dashboard
To access the Solr admin panel, visit the hostname or IP address on the port (on which Solr is running):
    http://ip_address:port/solr/

### Core Creation in Standalone mode
```
1.Create a new Solr core with the following command:
  $ sudo bin/solr create -c core_name -force
  For example: Core Created named as new_core:
  $ sudo bin/solr create -c new_core -force
2.The created core will reflect in the core drop-down menu on the Solr admin console.
```
### Collection Creation in SolrCloud mode
```
1.Create a new Solr collection with the following command with the default shard and replica count:
  $ sudo bin/solr create -c collection_name -force
To create a collection with a customized shard and replica count. 
  $ sudo bin/solr create -c collection_name -s <count> -rf <count> -force
  For example: Collection Created named as new_collection respectively.
  $ sudo bin/solr create -c new_collection -force
  $ sudo bin/solr create -c new_collection -s 1 -rf 2 -force
2.The created collection will reflect in the collection drop-down menu on the Solr admin console.

```    

## 2. Viewing the Apache Solr logs

Go to the Solr dashboard. Under Java Properties, search for solr.​log.​dir<br>
To view logs, go to Location:<br>
* When Solr runs in Standalone mode - /var/solr/logs/solr.log<br>
* When Solr runs in SolrCloud mode<br>
 -/opt/solr-8.6.0/example/cloud/node1/logs/solr.log<br>
 -/opt/solr-8.6.0/example/cloud/node2/logs/solr.log<br>
 -/opt/solr-8.6.0/example/cloud/node3/logs/solr.log<br>
 -/opt/solr-8.6.0/example/cloud/node4/logs/solr.log<br>

## 3. Configuring Filebeat to push logs to Guardium

## a. Filebeat installation

### Procedure:

To install Filebeat on your system, follow the steps in this topic:
    https://www.elastic.co/guide/en/beats/filebeat/current/filebeat-installation-configuration.html#installation

## b. Filebeat configuration:

To use Logstash to perform additional processing on the data collected by Filebeat, we need to configure Filebeat to use Logstash. To do this, modify the filebeat.yml file which you can find inside the folder where filebeat is installed. Follow these instructions for finding the installation directory:
    https://www.elastic.co/guide/en/beats/filebeat/current/directory-layout.html

In addition, events are configured with the include_lines, exclude_lines, and add_tags. You can learn more about Filebeat Configuration [here](https://www.elastic.co/guide/en/beats/filebeat/current/filebeat-input-log.html#:~:text=include_lines%20edit%20A%20list%20of%20regular%20expressions%20to,the%20list.%20By%20default%2C%20all%20lines%20are%20exported.).

### Procedure:

1. Configuring the input section :-
```
    • Locate "filebeat.inputs" in the filebeat.yml file, then add the following parameters.

            filebeat.inputs:
            - type: log
            enabled: true
            paths:
              - /var/solr/logs/solr.log
              - /opt/solr-8.6.0/example/cloud/node1/logs/solr.log
              - /opt/solr-8.6.0/example/cloud/node2/logs/solr.log
              - /opt/solr-8.6.0/example/cloud/node3/logs/solr.log
              - /opt/solr-8.6.0/example/cloud/node4/logs/solr.log
			  
	• To include logs that are the result of RequestHandler, LogUpdateProcessorFactory, HttpSolrCall, and Exceptions in Solr.
	
            include_lines: ['o.a.s.u.p.LogUpdateProcessorFactory','o.a.s.c.S.Request','o.a.s.h.RequestHandlerBase org.apache.solr.common.SolrException','o.a.s.s.HttpSolrCall','o.a.s.c.a.c.OverseerCollectionMessageHandler','o.a.s.c.s.i.s.ExceptionStream','o.a.s.h.SQLHandler','o.a.s.h.e.ExportWriter','o.a.s.h.StreamHandler']
            exclude_lines: ['status=400']

    • To process multi-line audit events, add the settings in the same inputs section.

            multiline.type: pattern
            multiline.pattern: '^[0-9]{4}-[0-9]{2}-[0-9]{2}'
            multiline.negate: true
            multiline.match: after
  
    • Add the tags to uniquely identify the ApacheSolr events from the rest.    
      
            tags: ["apache_solr_on_azure"]
```
2. Configuring the output section:-
```
    • Locate "output" in the filebeat.yml file, then add the following parameters.

    • Disable Elasticsearch output by commenting it out.

    • Enable Logstash output by uncommenting the Logstash section. For more information, see https://www.elastic.co/guide/en/beats/filebeat/current/logstash-output.html#logstash-output

    For example:
            output.logstash:
	      hosts: ["20.115.120.146:5085"]

    • The hosts option specifies the Logstash server and the port (5001) where Logstash is configured to listen for incoming Beats connections.

    • You can set any port number except 5044, 5141, and 5000 (as these are currently reserved in Guardium v11.3 and v11.4 ).
```

3. To learn how to start FileBeat, see https://www.elastic.co/guide/en/beats/filebeat/current/filebeat-installation-configuration.html#start


## 4. Configuring the ApacheSolr filter in Guardium

The Guardium universal connector is the Guardium entry point for native audit logs. The Guardium universal connector identifies and parses the received events, and converts them to a standard Guardium format. The output of the Guardium universal connector is forwarded to the Guardium sniffer on the collector, for policy and auditing enforcements. Configure Guardium to read the native audit logs by customizing the Solr template.

### Before you begin

* You must have the Log Full Details policy enabled on the collector. The detailed steps can be found in step #4 on [this page](https://www.ibm.com/docs/en/guardium/11.4?topic=dpi-installing-testing-filter-input-plug-in-staging-guardium-system).
* You must have permission for the S-Tap Management role. The admin user includes this role by default.
* Download the [guardium_logstash-offline-plugin-apache-solr-azure.zip](ApacheSolrOverFilebeatPackage/guardium_logstash-offline-plugin-apache-solr-azure.zip) plug-in.

### Procedure

1. On the collector, go to Setup > Tools and Views > Configure Universal Connector.
2. First, enable the Universal Guardium connector, if it is currently disabled.
3. Click Upload File and select the offline [guardium_logstash-offline-plugin-apache-solr-azure.zip](ApacheSolrOverFilebeatPackage/guardium_logstash-offline-plugin-apache-solr-azure.zip) plug-in. After it is uploaded, click OK.
4. Click the Plus icon to open the Connector Configuration dialog box.
5. Type a name in the Connector name field.
6. Update the input section to add the details from the [solrazure.conf](solrazure.conf) file's input part, omitting the keyword "input{" at the beginning and its corresponding "}" at the end.
7. Update the filter section to add the details from the [solrazure.conf](solrazure.conf) file's filter part, omitting the keyword "filter{" at the beginning and its corresponding "}" at the end.
8. The 'type' fields should match in the input and filter configuration sections. This field should be unique for every individual connector added.
9. The tag added in the filebeat.yml file should match the "[tags]" specified in the filter part.
10. Click Save. Guardium validates the new connector, and enables the universal connector if it was disabled. After it is validated, it appears on the Configure Universal Connector page.

## 5. Limitations    
- The following important fields couldn't be mapped with ApacheSolr qtp logs:
  - SourceProgram : field is left blank, as this information is not embedded in the messages pulled from Azure.<br>
  - clientIP : field is populated with 0.0.0.0, as this information is not embedded in the messages pulled from Azure.<br>
  - OS User         : Not available with logs<br>
  - Client HostName : Not available with logs<br>
  - dbUser          : Not available with logs<br>
  - LOGIN_FAILED    : Not available with logs

- While launching Solr in SolrCloud mode, multiple logs will be generated for single query execution as a call to shard(In SolrCloud, a logical partition of a single Collection) and replica(A core that acts as a physical copy of a shard in a SolrCloud Collection). 
