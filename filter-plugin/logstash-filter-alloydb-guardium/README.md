# AlloyDB-Guardium Logstash filter plug-in

### Meet AlloyDB

* Tested versions: V1
* Environment: Google Cloud Platform (GCP)
* Supported inputs: Google Pubsub input plugin
* Supported Guardium versions: Guardium Data Protection: 12.1 and later

This is a [Logstash](https://github.com/elastic/logstash) filter plug-in for the universal connector that is featured in
IBM Security Guardium. It parses AlloyDB event logs into
a [Guardium record](https://github.com/IBM/universal-connectors/blob/main/common/src/main/java/com/ibm/guardium/universalconnector/commons/structures/Record.java)
instance, which is a standard structure made out of several parts. Then the information is sent to Guardium.
Guardium records include the accessor (the person who tries to access the data), the session, data, and exceptions. If
there are no errors, the data contains details about the query `construct`. The construct details the main action (verb)
and collections (objects) involved. The AlloyDB Logstash filter plug-in supports Guardium Data Protection.

<b>Note:</b> This version is compliant with Guardium Data Protection v12.1 and later. For more information, see [input plug-in repository](https://github.com/IBM/universal-connectors/tree/main/input-plugin/logstash-input-google-pubsub).

## Configuring AlloyDB on GCP

1. [Create a cluster and its
  primary instance](https://cloud.google.com/alloydb/docs/quickstart/create-and-connect?hl=en#create-cluster).
2. [Connect to your instance and create
  a database](https://cloud.google.com/alloydb/docs/quickstart/create-and-connect?hl=en#run).
3. [Connect to the database that you created](https://cloud.google.com/alloydb/docs/quickstart/create-and-connect?hl=en#connect-to-guestbook).
4. [Verify your database
  connection](https://cloud.google.com/alloydb/docs/quickstart/create-and-connect?hl=en#verify-connection).
5. [Create a log sink in Pub/Sub](https://cloud.google.com/logging/docs/export/configure_export_v2#creating_sink).
    * Use the following inclusion filter for ```Choose logs to include in sink``` during log sink creation to specify which logs to route. The following filter captures relevant logs based on data access and activity logs:

              ((resource.type="alloydb.googleapis.com/Instance" logName="projects/charged-mind-281913/logs/alloydb.googleapis.com%2Fpostgres.log" )) 

## Enabling audit logs:

1. To view the detailed audit logs, enable the following flags on your database instance:

* `log_statement: all` - View executed SQL statements in audit logs.

2. To reduce the volume of audit logs, you can turn off the following flags, as they do not contain any details about the run queries:

* `autovacuum: off`
* `log_checkpoints: off`
* `log_connections: off`
* `log_disconnections: off` 

## Configuring the AlloyDB filter in Guardium

The Guardium universal connector is the Guardium entry point for native audit and data access logs. The Guardium universal
connector identifies and parses the received events, and converts them to a standard Guardium format. The output of the
Guardium universal connector is forwarded to the Guardium sniffer on the collector for policy and auditing
enforcements. Configure Guardium to read the native audit and data access logs by customizing the Capella template.

### Before you begin

* Configure the policies you need. For more information, see [Policies](/docs/#policies).
* You must have permissions for the S-Tap Management role. By default, the admin user is assigned the S-Tap Management role.
* Download
  the [logstash-filter-alloydb_guardium_filter](AlloyDBoverPubSubPackage/logstash-filter-alloydb_guardium_filter.zip) plug-in.

### Procedure

1. On the collector, go to **Setup** > **Tools and Views** > **Configure Universal Connector**.
2. Enable the universal connector if it is disabled.
3. Click **Upload File** and select the offline [logstash-filter-alloydb_guardium_filter](AlloyDBoverPubSubPackage/logstash-filter-alloydb_guardium_filter.zip) plug-in. After it is uploaded, click **OK**.
4. Click the **Plus** sign to open the Connector Configuration dialog.
5. In the **Connector name** field, enter a name.
6. Update the input section to add the details from
   the [alloydb.conf](AlloyDBoverPubSubPackage/alloydb.conf) file's ``input`` section, omitting the keyword ``input{`` at the beginning and its corresponding ``}`` at the end.
7. Update the filter section to add the details from
   the [alloydb.conf](AlloyDBoverPubSubPackage/alloydb.conf) file's ``filter`` section, omitting the keyword ``filter{`` at the beginning and its corresponding ``}`` at the end.
8. Make sure that the ``type`` fields in the ``input`` and ``filter`` configuration sections align. This field must be unique for each connector added to the system.
9. Click **Save**. Guardium validates the new connector and displays it in the Configure Universal Connector page.
10. When the offline plug-in is installed and the configuration is uploaded and saved in the Guardium machine, restart the universal connector by using the **Disable/Enable** button.

## Limitations

- Audit logs that contain SQL queries do not contain port and host information, so they are mapped to the default values.
- When you use GCP, duplicate entries can appear in both the reports and audit logs.


