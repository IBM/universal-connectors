## google_pubsub input plug-in
### Meet Google Pubsub
* Tested versions: 1.2.1 for GDP and 1.4.0 for GI
* Developed by Elastic
* Configuration instructions can be found in [Guardium Google Pub/Sub documentation](#installation).
* Supported Guardium versions:
    * Guardium Data Protection: 11.4 and above
    * GI 3.6 and above

This is a [Logstash](https://github.com/elastic/logstash) input plugin for
[Google Pub/Sub](https://cloud.google.com/pubsub/). The plugin can subscribe
to a topic and ingest messages. The events are then sent over to corresponding filter plugin which transforms these audit logs into a [Guardium record](https://github.com/IBM/universal-connectors/blob/main/common/src/main/java/com/ibm/guardium/universalconnector/commons/structures/Record.java)  instance (which is a standard structure made out of several parts).


## Purpose:

The plugin ingests [Stackdriver Logging](https://cloud.google.com/logging/) messages via the
[Exported Logs](https://cloud.google.com/logging/docs/export/using_exported_logs)
feature of Stackdriver Logging.

It is fully free and fully open source. The license is Apache 2.0, meaning you
are pretty much free to use it however you want in whatever way.

## Usage:

### Prerequisites:

#### Procedure:

1. Access to Google Cloud Platform project

2. Enable the Google Pub/Sub API.

3. If the plugin is to be used to ingest Stackdriver Logging messages, then the Stackdriver Logging API should be enabled and configure log exporting to Pub/Sub.

4. Below links can be used to gather more information on the same
	
	* Google Cloud Platform Projects and [Overview](https://cloud.google.com/docs/overview/)
	* Google Cloud Pub/Sub [documentation](https://cloud.google.com/pubsub/)
	* Stackdriver Logging [documentation](https://cloud.google.com/logging/)


### Audit Logs Streaming

#### Procedure:

1. Create a project attached to billing info 
2. Set up your Google Cloud project and Pub/Sub topic and subscriptions:
   - Grant Pub/Sub Editor, Pub/Sub Publisher, Pub/Sub Subscriber roles to your account in IAM to enable editing Pub/Sub and viewing all audited logs. See [Pub/Sub Roles](https://cloud.google.com/pubsub/docs/access-control#roles) for the full list.
   - Create the topic and subscription as instructed [here](https://cloud.google.com/pubsub/docs/building-pubsub-messaging-system#set_up_your_project_and_topic_and_subscriptions)
3. Create service account and credentials and add IAM roles
   - Go to *Service accounts > IAM service accounts*, select your project and click Create Service Account
   - Grant Pub/Sub Admin and Cloud Pub/Sub Service Agent roles to the service account in *Service account permissions or IAM & Admin > IAM*
   - *Service accounts > Keys > Create Key > JSON > Create*. The key is sent to your downloads folder
   - Use Upload file in the Guardium machine to upload the generated json key file
4. Turn on audit logging for all services, as instructed [here](https://cloud.google.com/architecture/exporting-stackdriver-logging-for-security-and-access-analytics#turn_on_audit_logging_for_all_services)
6. Configure a Sink:
   - [Prerequisites](https://cloud.google.com/logging/docs/export/configure_export_v2#before-you-begin)
   - in Logs Router in Logging side panel create a Sink associated with the Pub/Sub topic 
   - Create a Sink [how-to guide](https://cloud.google.com/logging/docs/export/configure_export_v2#creating_sink)
   - Go to *View Sink details > copy the Writer identity*
   - Go to your topic's *page > Permissions > grant Pub/Sub Publisher role* to the Writer identity
7. Create the SQL instance and Configure Logging
   - MySQL [how-to guide](../../filter-plugin/logstash-filter-pubsub-mysql-guardium/README.md#prerequisites)
   - PostgreSQL [how-to guide](../../filter-plugin/logstash-filter-pubsub-postgresql-guardium/README.md#prerequisites)
8. After installing the plug-in's offline package and once the configuration is uploaded and saved to your Guardium machine, restart Universal Connector using the Disable/Enable button
9. Connect to the SQL instance and run queries
   - Add your IP to the Authorized networks section in *SQL > Connections > Add Network*
   - Create SQL instance users in *SQL > Instances > `instance_name` > Users*
   - Run queries from Cloud Shell:  *SQL > Instances > `instance_name` > Overview page > Connect using `gcloud`*

### Installation for GDP

To install this plug-in, you need to download the [offline pack](https://github.ibm.com/Activity-Insights/universal-connectors/blob/master/input-plugin/logstash-input-google-pubsub/GooglePubSubPackage/logstash-offline-plugin-input-google_pubsub.zip), and use Upload file in the Guardium machine.

#### Note
To install on your local machine that is running Logstash, execute:
`bin/logstash-plugin install file:///path/to/logstash-offline-plugin-input-google_pubsub.zip`

### Parameters for GDP:
	
| Parameter | Input Type | Required | Default |
|-----------|------------|----------|---------|
| project_id | String  | Yes |  |
| topic | String | Yes |  |
| subscription | String | No |  |
| json_key_file | a valid filesystem path | No |  |
| include_metadata | Boolean | No | false|
| create_subscription | Boolean | No | false|
| max_messages | Number | Yes | 5|





#### `project_id`
The `project_id` setting allows to set the Google Cloud Project ID (name, not number).

#### `topic`
The `topic` setting allows specifying the Google Cloud Pub/Sub Topic and Subscription. Note that the topic must be created manually with Cloud Logging pre-configured export to PubSub configured to use the defined topic. The subscription will be created automatically by the plugin.

#### `subscription`
The `subscription` setting allows to specify teh subscription

#### `json_key_file`
The `json_key_file` allows setting authentication details, if logstash is running within Google Compute Engine, the plugin will use GCE’s Application Default Credentials. Outside of GCE, you will need to specify a Service Account JSON key file.

#### `include_metadata`
The `include_metadata` setting, if set true, will include the full message data in the [@metadata][pubsub_message] field

#### `create_subscription`
The `create_subscription` setting, if set true, will have the input plugin create a new subscription on startup instead of manually creating it on GCP
##### Note
    - This requires additional permissions to be granted to the client (i.e. the Service Account) and is not recommended for most use-cases. If you still need to use it, grant the Service Account the "Cloud Pub/Sub Service Agent" Role in *IAM & Admin > Service Accounts > Grant Access*

#### `max_messages`
The `max_messages` setting, helps to mitigate the issues caused due to subscriber client processing and acknowledging the messages more slowly than Pub/Sub sending them to the client. This option helps to control the rate at which the subscriber receives messages. The value is adjusted according to the traffic load. 


#### Logstash Default config params
Other standard logstash parameters are available such as:
* `add_field`
* `type`
* `tags`

### Parameters for GI:
	
| Parameter | Input Type | Required | Default |
|-----------|------------|----------|---------|
| project_id | String  | Yes |  |
| topic | String | Yes |  |
| subscription | String | No |  |
| json_key_file_content | a valid JSON key file content | No |  |
| include_metadata | Boolean | No | false|
| create_subscription | Boolean | No | false|
| max_messages | Number | Yes | 5|

#### `json_key_file_content`
The `json_key_file_content` allows setting authentication details, if logstash is running within Google Compute Engine, the plugin will use GCE’s Application Default Credentials. Outside of GCE, you will need to specify a Service Account JSON key file content.

Note: `json_key_file_content` is replacement of `json_key_file` in GI, all other settings are same

### Sample Configuration

Below is a copy of the included `googlepubsub.conf` [file](https://github.ibm.com/Activity-Insights/universal-connectors/blob/master/input-plugin/logstash-input-google-pubsub/GooglePubSubPackage/googlepubsub.conf) that shows a basic
configuration for this plugin.


#### Input part:
```
input {
   google_pubsub {
        # Your GCP project id (name)
       project_id => "<PROJECT_ID>"

        # The topic name below is currently hard-coded in the plugin. You
        # must first create this topic by hand and ensure you are exporting
        # logging to this pubsub topic.
       topic => "<TOPIC_NAME>"

        # The subscription name is customizeable. The plugin will attempt to
        # create the subscription (but use the hard-coded topic name above).
       subscription => "<SUB_NAME>"

        # If you are running logstash within GCE, it will use
        # Application Default Credentials and use GCE's metadata
        # service to fetch tokens.  However, if you are running logstash
        # outside of GCE, you will need to specify the service account's
        # JSON key file below.
       json_key_file => "${THIRD_PARTY_PATH}/<KEY_FILE_NAME>.json"

        include_metadata => true
       codec => "json"
}
}
```
##### Note
Setting a different `subscription` for each Logstash pipeline running on a server, enables receiving the full set of messages published to the `topic`.
Setting the same `subscription` for multiple Logstash pipelines running on different servers, can be used for **Load Balancing** the processing of the messages over those servers.


## Troubleshooting

* Logs aren't showing:
   * Check that messages are being sent to your gmachine by going to your *Pub/Sub topic > Publish Messages*, publish a message and see if it's logged in `logstash_stdout_err.log`
   * Make sure you can see messages when going to *Pub/Sub topic > Select Subscription > Pull messages*
   * Check the inclusion/exclusion filters are valid and legal by going to *Edit Sink > Preview Logs* button next to the filters edit window.
   * If you're still not seeing logs, double check that you don't have another configuration running using the same subscription name.
