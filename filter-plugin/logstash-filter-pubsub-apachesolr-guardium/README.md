# Apache Solr GCP-Guardium Logstash filter plug-in

This is a Logstash filter plug-in for the universal connector that is featured in IBM Security Guardium. It parses a GCP (Google Cloud Platform) event logs into a Guardium record instance (which is a standard structure made out of several parts). The information is then sent over to Guardium. Guardium records include the accessor (the person who tried to access the data), the session, data, and exceptions. If there are no errors, the data contains details about the query "construct". The construct details the main action (verb) and collections (objects) involved.
The Apache Solr GCP plugin supports only Guardium Data Protection as of now.

### Note
This plug-in contains a runtime dependency of Logstash Google PubSub input plug-in (version ~> 1.2.1, i.e. at least 1.2.1). To install, refer to the [Prerequisites](#Prerequisites) section of the Documentation below.

The plug-in is free and open-source (Apache 2.0). It can be used as a starting point to develop additional filter plug-ins for Guardium universal connector.

# Documentation

### Prerequisites

Download the [Logstash Offline package](PubSubApacheSolrPackage/guardium_logstash-offline-plugin-pubsub-apache-solr-gcp.zip) for the ApacheSolrGCP PubSub filter plugin, and upload it to the gmachine.

#### Note
This version is for GDP v11.4, i.e., stable version. Please refer to the
[input plugin's repository](https://github.com/IBM/universal-connectors/tree/main/input-plugin/logstash-input-google-pubsub) for more information.

#### Permissions and Roles Details

##### Grant Permission Required for log view/ download
User can view and download the generated logs. 
Following Identity and Access Management roles required for view and download logs:
* Logs view:
roles/logging.viewer (Logs Viewer) 
roles/logging.privateLogViewer (Private Logs Viewer)
* Download logs:
Logging Admin (roles/logging.admin)
Logs View Accessor (roles/logging.viewAccessor)

##### Steps to Set destination(TOPIC & SUBSCRIPTION) permissions
To set permissions for log sink to route to its destination, do the following:
* Obtain the sink's writer identity—an email address—from the new sink.
   1. Go to the Log Router page, and select menu  > View sink details. 
   2. The writer identity appears in the Sink details panel.
* If user have Owner access to the destination, 
   1. add the sink's writer identity to topic and give it the Pub/Sub Publisher and Subscriber role.
   2. add the sink's writer identity to subscription and give it the Pub/Sub Subscriber role.

### GCP instance Creation 

1. In the Google Cloud Console, go to the VM instances page..
2. Select your project and click Continue.
3. Click Create instance.
4. Specify a Name for your VM.
5. Optional: Change the Zone for this VM. Compute Engine randomizes the list of zones within each region to encourage use across multiple zones.
6. Select a Machine configuration for your VM.
7. In the Firewall section, to permit HTTP or HTTPS traffic to the VM, select Allow HTTP traffic or Allow HTTPS traffic.(add the port on which solr is running, default is 8983)
To create and start the VM, click Create.

You can learn more about VM Creation [here](https://cloud.google.com/compute/docs/instances/create-start-instance#expandable-2)


### Configuring the Apache Solr on GCP
#### Apache Solr Setup 
```
1.Install java by running the following command:
        $ sudo apt install default-jre
2.Run the below command to check the java version:
        $ java –version
3.In order for Solr to work as expected, user need to have the lsof command installed as well.
        The lsof command stands for "list open files".  Run the following command for lsof Installation:
        $ sudo apt install lsof
4.Run the following commands to download Solr installation files:
        $ cd /usr/src
        $ sudo apt install wget
        $ sudo apt-get install wget
        $ sudo wget https://archive.apache.org/dist/lucene/solr/8.6.0/solr-8.6.0.tgz
        $ sudo tar -xzvf solr-8.6.0.tgz
5.Run the Solr installation script
        $ cd solr-8.6.0/bin
        $ sudo ./install_solr_service.sh ../../solr-8.6.0.tgz
```
#### Launching Apache Solr
```
1.Once the script completes, Solr will be installed as a service and running in the background on users server (on port 8983). To verify, run:
       $ sudo service solr status
2.Solr facilitates to run it in 2 modes:
2.1.Standalone mode :An index is stored on a single computer and the setup is called a core.There can be multiple cores or indexes here.
To Launch Solr in Standalone Mode:
       $ cd /opt/solr-8.6.0
       $ sudo bin/solr start -force 
2.2.SolrCloud mode:  An index is distributed across multiple computers or even multiple server instances on one computer. Groups of documents here are called collections.
 To Launch Solr in SolrCloud Mode:
       $ cd /opt/solr-8.6.0
       $ sudo bin/solr start -e cloud -force
```

You can learn more about ApacheSolr Setup [here](https://solr.apache.org/guide/8_8/installing-solr.html)
Once the Apache Solr set up is done, Ops Agent needs to be installed and configured on the system.

## Login to the Solr Dashboard
In order to access the Solr admin panel. Visit the hostname or IP address on port(solr is running):
    http://ip_address:port/solr/

### Core Creation in Standalone mode
```
1.Create a new Solr core with the following command:
  $ sudo bin/solr create -c core_name -force
  For eg: Core Created named as new_core:
  $ sudo bin/solr create -c new_core -force
2.Created core will reflect in the core drop down on solr admin console.
```
### Collection Creation in SolrCloud mode
```
1.Create a new Solr collection with the following command(having default shard and replica count):
  $ sudo bin/solr create -c collection_name -force
To create collection having specified shard and replica count. 
  $ sudo bin/solr create -c collection_name -s <count> -rf <count> -force
For eg: Collection Created named as new_collection respectively.
  $ sudo bin/solr create -c new_collection -force
  $ sudo bin/solr create -c new_collection -s 1 -rf 2 -force
2.Created collection will reflect in the collection drop down on solr admin console.

```
### Configure Ops Agent
The Ops Agent is the primary agent for collecting telemetry from your Compute Engine instances.
```
Run Following commands to install ops agent
sudo curl -sSO https://dl.google.com/cloudagents/add-google-cloud-ops-agent-repo.sh
sudo bash add-google-cloud-ops-agent-repo.sh --also-install

To verify that the agent is working as expected, run:
sudo systemctl status google-cloud-ops-agent
```
### Customize the Ops Agent configuration
Put your configuration for the Ops Agent in the following path:

<b>/etc/google-cloud-ops-agent/config.yaml<b>

To configure logging , Visit Solr Dashboard, under Java Properties , search for solr.​log.​dir

**When Solr running in Standalone mode**
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
**When Solr running in SolrCloud mode**
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
Then,restart agent using below command:<br>
<b>$ sudo service google-cloud-ops-agent restart<b>

You can learn more about [Ops Agent installation](https://cloud.google.com/monitoring/agent/ops-agent/installation) and [configuration](https://cloud.google.com/monitoring/agent/ops-agent/configuration).


#### Configure Logging & Filter the logs using resources, logs and severity levels to display
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
* Click on Create button

##### Steps to create log sink in Pub/Sub
* In the Cloud Console, go to the Logging > Log Router page. 
* Click on Create sink button.
* In the Sink details panel, enter the following details:
* Sink name: Provide an identifier for the sink; note that after User create the sink, user can't rename the sink, but can delete it and create a new sink.
* Sink description (optional): Describe the purpose or use case for the sink.
* In the Sink destination panel, select the Pub/Sub topic as sink service and destination.
* Choose logs to include in the sink in the Build inclusion filter panel. 
* User can filter the logs using log name, resource and severity.
* In case of multi-region one need to do the same set of configurations on that region.
Based on the region different configuration file will be used for  Input Plugin

##### Steps to Create service account credentials
* Go to the Service accounts section of IAM & Admin console. 
* Select project and click on Create Service Account.
* Enter a Service account name, such as apachesolrgcp-pubsub.
* Click on Create.
* The service account needs access on subscription that was created earlier. Use the Select a role dropdown to add the Pub/Sub Subscriber role. 
* Click Continue. user does not need to grant users access to this service account.
* Click Create Key. The key is used by logstash input plugin configuration file.
* Select JSON and click Create. 
* Rename the key file to ~/Downloads/key.json.	  
	  
##### Inclusion Filter
Edit the Sink via *Logs Router > Sink Inclusion Filter*:

 ```
resource.type="gce_instance" resource.labels.instance_id="<instance_id>"
logName="projects/project-sccd/logs/files"
jsonPayload.message=~(("o.a.s.c.S.Request" AND "status=0") OR "o.a.s.u.p.LogUpdateProcessorFactory" OR "o.a.s.h.RequestHandlerBase org.apache.solr.common.SolrException" OR ("o.a.s.s.HttpSolrCall" AND "status=0" ) OR "o.a.s.c.a.c.OverseerCollectionMessageHandler" OR "o.a.s.c.s.i.s.ExceptionStream")
```
  

### Supported Log event types
* solr.log - `INFO`, `ERROR`

 
   
## Configuring the Apache Solr filter in Guardium
The Guardium universal connector is the Guardium entry point for native audit/data_access logs. The Guardium universal connector identifies and parses the received events, and converts them to a standard Guardium format. The output of the Guardium universal connector is forwarded to the Guardium sniffer on the collector, for policy and auditing enforcements. Configure Guardium to read the native audit/data_access logs by customizing the Apache Solr template.

## Before you begin
* You must have Log Full Details policy enabled on the collector. The detailed steps can be found in step 4 on [this page](https://www.ibm.com/docs/en/guardium/11.4?topic=dpi-installing-testing-filter-input-plug-in-staging-guardium-system).
* You must have permission for the S-Tap Management role. The admin user includes this role by default.
* Download the [guardium_logstash-offline-plugin-pubsub-apache-solr-gcp.zip](PubSubApacheSolrPackage/guardium_logstash-offline-plugin-pubsub-apache-solr-gcp.zip) plug-in.

## Procedure
1. On the collector, go to Setup > Tools and Views > Configure Universal Connector.
2. Enable the connector if is in disabled state before uploading the UC plug-in.
3. Click Upload File and select the offline [guardium_logstash-offline-plugin-pubsub-apache-solr-gcp.zip](PubSubApacheSolrPackage/guardium_logstash-offline-plugin-pubsub-apache-solr-gcp.zip) plug-in. After it is uploaded, click OK.
4. Click Upload File and select the key.json file(which was generated above for Service Account). After it is uploaded, click OK.
5. Click the Plus sign to open the Connector Configuration dialog box.
6. Type a name in the Connector name field.
7. Update the input section to add the details from [solrgcp.conf](PubSubApacheSolrPackage/solrgcp.conf) file's input part, omitting the keyword "input{" at the beginning and its corresponding "}" at the end.
8. Update the filter section to add the details from [solrgcp.conf](PubSubApacheSolrPackage/solrgcp.conf) file's filter part, omitting the keyword "filter{" at the beginning and its corresponding "}" at the end.
9. 'type' field should match in input and filter configuration section.This field should be unique for every individual connector added.
10. Click Save. Guardium validates the new connector, and enables the universal connector if it was disabled. After it is validated, it appears in the Configure Universal Connector page.
11. After offline plugin is installed and configuration is uploaded and saved in Guardium machine, restart Universal Connector using Disable/Enable button.

## Limitations  
1. Following important fields couldn't be mapped with ApacheSolr qtp logs.
     - SourceProgram : field is left blank since this information is not embedded in the messages pulled from Google Cloud.
     - clientIP and serverIP : fields are populated with 0.0.0.0, as this information is not embedded in the messages pulled from Google Cloud.
     - OS User         : Not available with logs
     - Client HostName : Not available with logs

2. As qtp(QueuedThreadPool)logs are taken into consideration because it have more information.
   Below fields are not available
     - dbUser         : Not available with logs
     - LOGIN_FAILED : Not available with logs

3. While launching Solr in SolrCloud mode, multiple logs will be generated for single query execution as a call to shard(In SolrCloud, a logical partition of a single Collection) and replica(A Core that acts as a physical copy of a Shard in a SolrCloud Collection).

4. On executing error queries in multiline from third-party tool, GCP is not capturing log as a single event.So, partial query will be displayed in Sql Error Report.
