# Logstash Google PubSub Input Plugin

This is a [Logstash](https://github.com/elastic/logstash) input plugin for
[Google Pub/Sub](https://cloud.google.com/pubsub/). The plugin can subscribe
to a topic and ingest mes***REMOVED***ges.

The plugin ingests [Stackdriver Logging](https://cloud.google.com/logging/) mes***REMOVED***ges via the
[Exported Logs](https://cloud.google.com/logging/docs/export/using_exported_logs)
feature of Stackdriver Logging.

It is fully free and fully open source. The license is Apache 2.0, meaning you
are pretty much free to use it however you want in whatever way.

## Documentation

### Step-by-Step Guide for starting Audit Logs Streaming
1. Create a project attached to billing info 
2. Set up your Google Cloud project and Pub/Sub topic and subscriptions, as instructed [here](https://cloud.google.com/pubsub/docs/building-pubsub-mes***REMOVED***ging-system#set_up_your_project_and_topic_and_subscriptions)
   - Grant Pub/Sub Editor, Pub/Sub Publisher, Pub/Sub Subscriber roles to your account in IAM to enable editing Pub/Sub and viewing all audited logs. See [Pub/Sub Roles](https://cloud.google.com/pubsub/docs/access-control#roles) for the full list.
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
   - Go to View Sink and copy the Writer address
   - Go to your topic's *page > Permissions > grant Pub/Sub Publisher role* to the Writer address
7. Create the SQL instance and Configure Logging
   - MySQL [how-to guide](https://github.ibm.com/Activity-Insights/univer***REMOVED***l-connectors/blob/master/filter-plugin/logstash-filter-pubsub-mysql-guardium/README.md#Create-the-SQL-instance-and-Configure-Logging)
8. Connect to the SQL instance and run queries
   - Add your IP to the Authorized networks section in *SQL > Connections > Add Network*
   - Create SQL instance users in *SQL > Instances > `instance_name` > Users*
   - Run queries from Cloud Shell:  *SQL > Instances > `instance_name` > Overview page > Connect using `gcloud`*

### Note
For more information on Prerequisites, Cloud Pub/Sub, Authentication and
Stackdriver logging - refer to [Google Pub/Sub](https://www.elastic.co/guide/en/logstash/current/plugins-inputs-google_pubsub.html).

## Installation

To install this plug-in, you need to download the [offline pack](https://github.ibm.com/Activity-Insights/univer***REMOVED***l-connectors/blob/master/input-plugin/logstash-input-google-pubsub/GooglePubSubPackage/logstash-offline-plugin-input-google_pubsub.zip), and use Upload file in the Guardium machine.

### Note
To install on your local machine that is running Logstash, execute:
`bin/logstash-plugin install file:///path/to/logstash-offline-plugin-input-google_pubsub.zip
`

### Sample Configuration

Below is a copy of the included `googlepubsub.conf` [file](https://github.ibm.com/Activity-Insights/univer***REMOVED***l-connectors/blob/master/input-plugin/logstash-input-google-pubsub/GooglePubSubPackage/googlepubsub.conf) that shows a basic
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
       json_key_file => "${LOGSTASH_DIR}/third_party/<KEY_FILE_NAME>.json"

        include_metadata => true
       codec => "json"
}
}
```
##### Note
Setting a different `subscription` for each Logstash pipeline running on a server, enables receiving the full set of mes***REMOVED***ges published to the `topic`.
Setting the ***REMOVED***me `subscription` for multiple Logstash pipelines running on different servers, can be used for **Load Balancing** the processing of the mes***REMOVED***ges over those servers.

### More configuration options
#### Subscription Creation on startup
To have the input plugin create a new subscription on startup instead of manually creating it on GCP, use:
    `create_subscription => true`
##### Note
   - Value defaults to `false` in case the feature is not used in the configuration
   - This requires additional permissions to be granted to the client (i.e. the Service Account) and is not recommended for most use-cases. If you still need to use it, grant the Service Account the "Cloud Pub/Sub Service Agent" Role in *IAM & Admin > Service Accounts > Grant Access*

#### Mes***REMOVED***ge Flow Control
Your subscriber client might process and acknowledge mes***REMOVED***ges more slowly than Pub/Sub sends them to the client. In this case, to mitigate the issues that might occur, use `max_mes***REMOVED***ges` configuration option to control the rate at which the subscriber receives mes***REMOVED***ges.
`max_mes***REMOVED***ges => <NUM>`

##### Note
   - Value defaults to 5 in case the feature is not used in the configuration
   - The maximum number of mes***REMOVED***ges returned per request. The Pub/Sub system may return fewer than the number specified.

More on creation and management of Pub/Sub topics and subscriptions, see [GCP docs](https://cloud.google.com/pubsub/docs/admin)

## Troubleshooting

* Logs aren't showing:
   * Check that mes***REMOVED***ges are being sent to your gmachine by going to your *Pub/Sub topic > Publish Mes***REMOVED***ges*, publish a mes***REMOVED***ge and see if it's logged in `logstash_stdout_err.log`
   * Make sure you can see mes***REMOVED***ges when going to *Pub/Sub topic > Select Subscription > Pull mes***REMOVED***ges*
   * Check the inclusion/exclusion filters are valid and legal by going to *Edit Sink > Preview Logs* button next to the filters edit window.
   * If you're still not seeing logs, double check that you don't have another configuration running using the ***REMOVED***me subscription name.


## Contributing

You can enhance this input and open a pull request with suggested changes - or you can use the project to create a different input plug-in for Guardium that supports other data sources.
