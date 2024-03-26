# Apache Solr GCP-Guardium Logstash filter plug-in
### Meet Apache Solr GCP
* Tested versions: 8.6.0
* Environment: Google Cloud Platform (GCP)
* Supported inputs: Pub/Sub (pull)
* Supported Guardium versions:
    * Guardium Data Protection: 11.4 and above

This is a Logstash filter plug-in for the universal connector that is featured in IBM Security Guardium. It parses GCP (Google Cloud Platform) event logs into a Guardium record instance (which is a standard structure made out of several parts). The information is then sent over to Guardium. Guardium records include the accessor (the person who tried to access the data), the session, data, and exceptions. If there are no errors, the data contains details about the query "construct". The construct details the main action (verb) and collections (objects) involved.
The Apache Solr GCP plugin supports only Guardium Data Protection as of now.

### Note
This plug-in contains a runtime dependency of Logstash Google PubSub input plug-in (version ~> 1.2.1, i.e. at least 1.2.1). To install, refer to the [Prerequisites](#Prerequisites) section of the Documentation below.

The plug-in is free and open-source (Apache 2.0). It can be used as a starting point to develop additional filter plug-ins for Guardium universal connector.

# Documentation

[input plugin's repository](https://github.com/IBM/universal-connectors/tree/main/input-plugin/logstash-input-google-pubsub) for more information.

#### Permissions and Roles Details

##### Grant Permission Required for log view/ download
User can view and download the generated logs. 
Following Identity and Access Management roles required to view and download logs:
* Logs view:
roles/logging.viewer (Logs Viewer) 
roles/logging.privateLogViewer (Private Logs Viewer)
* Download logs:
Logging Admin (roles/logging.admin)
Logs View Accessor (roles/logging.viewAccessor)

##### Steps to set destination(TOPIC & SUBSCRIPTION) permissions
To set permissions for log sink to route to its destination, do the following:
* Obtain the sink's writer identity—an email address—from the new sink.
   1. Go to the Log Router page, and select menu  > View sink details. 
   2. The writer identity appears in the Sink details panel.
* If the user has owner access to the destination, 
   1. add the sink's writer identity to topic and give it the Pub/Sub Publisher and Subscriber role.
   2. add the sink's writer identity to subscription and give it the Pub/Sub Subscriber role.


### Customize the Ops Agent configuration
Put your configuration for the Ops Agent in the following path:

<b>/etc/google-cloud-ops-agent/config.yaml<b>

To configure logging, visit Solr Dashboard. Under Java Properties , search for solr.​log.​dir

**When Solr runs in Standalone mode**
```
logging:
  receivers:
    files:
      type: files
      include_paths:
      - /var/solr/logs/solr.log
  service:
    pipelines:
      default_pipeline:
        receivers: [files]
```
**When Solr runs in SolrCloud mode**
```
logging:
  receivers:
    files:
      type: files
      include_paths:
      - /opt/solr-8.6.0/example/cloud/node1/logs/solr.log
      - /opt/solr-8.6.0/example/cloud/node2/logs/solr.log
      - /opt/solr-8.6.0/example/cloud/node3/logs/solr.log
      - /opt/solr-8.6.0/example/cloud/node4/logs/solr.log
  service:
    pipelines:
      default_pipeline:
        receivers: [files]        
```
Then,restart the agent using the below command:<br>
<b>$ sudo service google-cloud-ops-agent restart<b>

You can learn more about [Ops Agent installation](https://cloud.google.com/monitoring/agent/ops-agent/installation) and [configuration](https://cloud.google.com/monitoring/agent/ops-agent/configuration).


#### Configure logging and filter the logs using resources, logs, and severity levels to display
1. Select a Google Cloud project.Go to the Google Cloud navigation menu and select Logging > Logs Explorer.
2. Filter the logs in logs explorer
    - Resources: The resources available in your current project.
    - Logs: The log types available for the current resources in your project.
    - Log severity: The log severity levels.

#### Configuration on GCP for Input Plugin

##### Steps to create Topic in Pub/Sub
* Go to the Pub/Sub topics page in the Cloud Console. 
* Click on Create a topic
* In the Topic ID field, provide a unique topic name, for example, MyTopic.
* Click on Create Topic button.

##### Steps to create subscription in Pub/Sub
* Display the menu for the topic created in previous step and click on New subscription.
* Type a name for the subscription, such as MySub.
* Leave the delivery type as Pull.
* Click Create button

##### Steps to create log sink in Pub/Sub
* In the Cloud Console, go to the Logging > Log Router page. 
* Click on Create sink button.
* In the Sink details panel, enter the following details:
* Sink name: Provide an identifier for the sink; note that after the user creates the sink, the user can't rename the sink, but can delete it and create a new sink.
* Sink description (optional): Describe the purpose or use case for the sink.
* In the Sink destination panel, select the Pub/Sub topic as sink service and destination.
* Choose logs to include in the sink in the Build inclusion filter panel. 
* The user can filter the logs using log name, resource, and severity.
* In cases of multiple regions, use the same set of configurations for all regions.
Use different configuration files for the input plug-in based on the region.

##### Steps to Create service account credentials
* Go to the Service accounts section of IAM & Admin console. 
* Select project and click on Create Service Account.
* Enter a Service account name, such as apachesolrgcp-pubsub.
* Click Create.
* The service account needs access to a subscription that was created earlier. Use the Select a role drop-down menu to add the Pub/Sub Subscriber role. 
* Click Continue. The user does not need to grant users access to this service account.
* Click the Create key. The key is used by the logstash input plug-in configuration file.
* Select JSON and click Create. 
* Rename the key file to ~/Downloads/key.json.	  
	  
##### Inclusion Filter
Edit the Sink via *Logs Router > Sink Inclusion Filter*:
##### Description
The purpose of this inclusion filter is to include logs that are the result of RequestHandler, LogUpdateProcessorFactory, HttpSolrCall, and Exceptions in Solr.

 ```
resource.type="gce_instance" resource.labels.instance_id="<instance_id>"
logName="projects/<project-id>/logs/files"
jsonPayload.message=~(("o.a.s.c.S.Request" AND "status=0" AND ("path=/select" OR "path=/spell" OR "path=/query" OR "path=/get" OR "path=/terms" OR "path=/export")) OR "o.a.s.u.p.LogUpdateProcessorFactory" OR "o.a.s.h.RequestHandlerBase org.apache.solr.common.SolrException" OR ("o.a.s.s.HttpSolrCall" AND "status=0" ) OR "o.a.s.c.a.c.OverseerCollectionMessageHandler" OR "o.a.s.c.s.i.s.ExceptionStream" OR "o.a.s.h.SQLHandler" OR "o.a.s.h.e.ExportWriter" OR "o.a.s.h.StreamHandler")

```
  

### Supported Log event types
* solr.log - `INFO`, `ERROR`

 
   
## Configuring the Apache Solr filter in Guardium
The Guardium universal connector is the Guardium entry point for native audit/data_access logs. The Guardium universal connector identifies and parses the received events, and converts them to a standard Guardium format. The output of the Guardium universal connector is forwarded to the Guardium sniffer on the collector, for policy and auditing enforcements. Configure Guardium to read the native audit/data_access logs by customizing the Apache Solr template.

## Before you begin
* Configure the policies you require. See [policies](/docs/#policies) for more information.
* You must have permission for the S-Tap Management role. The admin user includes this role by default.
* Apache Solr GCP-Guardium Logstash filter plug-in is automatically available with Guardium Data Protection versions 12.x, 11.4 with appliance bundle 11.0p490 or later or Guardium Data Protection version 11.5 with appliance bundle 11.0p540 or later releases.

**Note**: For Guardium Data Protection version 11.4 without appliance bundle 11.0p490 or later or Guardium Data Protection version 11.5 without appliance bundle 11.0p540 or later, download the [Logstash_Offline_package_7.x](https://github.com/IBM/universal-connectors/raw/release-v1.2.0/filter-plugin/logstash-filter-pubsub-apachesolr-guardium/PubSubApacheSolrPackage/guardium_logstash-offline-plugin-pubsub-apache-solr-gcp.zip) plug-in. (Do not unzip the offline-package file throughout the procedure).

 
## Procedure
1. On the collector, go to Setup > Tools and Views > Configure Universal Connector.
2. Enable the universal connector if it is disabled.
3. Click Upload File and select the offline [guardium_logstash-offline-plugin-pubsub-apache-solr-gcp.zip](https://github.com/IBM/universal-connectors/raw/release-v1.2.0/filter-plugin/logstash-filter-pubsub-apachesolr-guardium/PubSubApacheSolrPackage/guardium_logstash-offline-plugin-pubsub-apache-solr-gcp.zip) plug-in. After it is uploaded, click OK. This step is not necessary for Guardium Data Protection v12.0 and later.
4. Click Upload File and select the key.json file(which was generated above for the service account). After it is uploaded, click OK.
5. Click the Plus sign to open the Connector Configuration dialog box.
6. Type a name in the Connector name field.
7. Update the input section to add the details from the [solrgcp.conf](PubSubApacheSolrPackage/solrgcp.conf) file's input part, omitting the keyword "input{" at the beginning and its corresponding "}" at the end.
8. Update the filter section to add the details from the [solrgcp.conf](PubSubApacheSolrPackage/solrgcp.conf) file's filter part, omitting the keyword "filter{" at the beginning and its corresponding "}" at the end.
9. The 'type' fields should match in input and filter configuration sections.This field should be unique for every individual connector added.
10. Click Save. Guardium validates the new connector and displays it in the Configure Universal Connector page.
11. After the offline plug-in is installed and configuration is uploaded and saved in the Guardium machine, restart the Universal Connector using the Disable/Enable button.

## Limitations  
1. The following important fields couldn't be mapped with ApacheSolr qtp logs.
     - SourceProgram : field is left blank since this information is not embedded in the messages pulled from Google Cloud.
     - clientIP and serverIP : fields are populated with 0.0.0.0, as this information is not embedded in the messages pulled from Google Cloud.
     - OS User         : Not available with logs
     - Client HostName : Not available with logs
	 - dbUser          : Not available with logs
     - LOGIN_FAILED    : Not available with logs

2. While launching Solr in SolrCloud mode, multiple logs will be generated for single query execution as a call to shard(In SolrCloud, a logical partition of a single Collection) and replica(A Core that acts as a physical copy of a Shard in a SolrCloud Collection).

3. On executing error queries in multiline from third-party tool, GCP is not capturing log as a single event.So, partial query will be displayed in the SQL Error Report.
