### Meet HDFS
* Tested versions: Hadoop 3.1.x
* HDFS Versions- Cloudera 7.1
* Environment: On-premise, Iaas
* Supported inputs: Filebeat (push)
* Supported Guardium versions:
  * Guardium Data Protection: 11.4 and above
  * Guardium Data Security Center SaaS : 1.0

This is a [Logstash](https://github.com/elastic/logstash) filter plug-in for the universal connector that is featured in IBM Security Guardium. It parses an HDFS audit event into a Guardium record instance, which standardizes the event into several parts before it is sent over to Guardium.

## 1. Configuring the HDFS

HDFS needs to be configured to write HDFS audits to a file on the system. In most HDFS installations, this is enabled and configured by default. All NameNodes will write audits to a log on their hosts.

## 2. Installing and enabling auditing

Once the HDFS audit log is enabled and configured properly, Filebeat will need to be installed and configured on the system.

## 3. Configuring Filebeat to push logs to Guardium

### Procedure:

1. To install Filebeat on your system, follow the steps in this topic:
   https://www.elastic.co/guide/en/beats/filebeat/current/filebeat-installation-configuration.html#installation

## b. Filebeat configuration:

To use Logstash to perform additional processing on the data collected by Filebeat, we need to configure Filebeat to use Logstash. To do this, modify the filebeat.yml file which you can find inside the folder where Filebeat is installed. Follow these instructions for finding the installation directory:
https://www.elastic.co/guide/en/beats/filebeat/current/directory-layout.html

### Procedure:

1. Configuring the input section :

   • Locate "filebeat.inputs" in the filebeat.yml file, then add the following parameters.

    ```
    filebeat.inputs:
   - type: filestream
   - id: <ID>
     enabled: true
     paths:
       - /var/log/hadoop-hdfs/hdfs-audit.log*

    filebeat.config.modules:
    path: ${path.config}/modules.d/*.yml
    reload.enabled: false

    setup.template.settings:
    index.number_of_shards: 1
  
    ```

2. Configuring the output section:

   • Locate "output" in the filebeat.yml file, then add the following parameters.

   • Disable Elasticsearch output by commenting it out.

   • Enable Logstash output by uncommenting the Logstash section. For more information, see https://www.elastic.co/guide/en/beats/filebeat/current/logstash-output.html#logstash-output

   For example:

    ```
    output.logstash:
    hosts: ["universal-connector-host:5046"]

    processors:
     - add_host_metadata: ~
     - add_locale: ~
     - add_tags:
    tags: [hdfs]

    ```
   In addition, events are configured with the add_locale, add_host_metadata, and add_tags processors (to add an "hdfs" tag).


3. To learn more about Filebeat processors, click [here](https://www.elastic.co/guide/en/beats/filebeat/current/filtering-and-enhancing-data.html#using-processors).

#### For details on configuring Filebeat connection over SSL, refer [Configuring Filebeat to push logs to Guardium](https://github.com/IBM/universal-connectors/blob/main/input-plugin/logstash-input-beats/README.md#configuring-filebeat-to-push-logs-to-guardium).


## 4. Configuring the HDFS filters in Guardium Data Protection (GDP)

The Guardium universal connector is the Guardium entry point for native audit logs.
The universal connector identifies and parses received events, and converts them to a standard Guardium format.
The output of the Guardium universal connector is forwarded to the Guardium sniffer on the collector, for policy and auditing enforcements.

## Limitations:

1. Database name is seen as blank.

### Before you begin
•Configure the policies you require. See [policies](/docs/#policies) for more information.

• You must have permission for the S-Tap Management role. The admin user includes this role by default.

• HDFS Logstash filter plug-in is automatically available with Guardium Data Protection versions 12.x, 11.4 with appliance bundle 11.0p490 or later or Guardium Data Protection version 11.5 with appliance bundle 11.0p540 or later releases.

**Note**: For Guardium Data Protection version 11.4 without appliance bundle 11.0p490 or prior or Guardium Data Protection version 11.5 without appliance bundle 11.0p540 or prior, download the [guardium-hdfs-uc.zip](https://github.com/IBM/universal-connectors/releases/download/v1.5.6/logstash-filter-hdfs_guardium_filter.zip) plug-in. (Do not unzip the offline-package file throughout the procedure). 

### Procedure

1. On the collector, go to Setup > Tools and Views > Configure Universal Connector.

2. First enable the Universal Guardium connector, if it is disabled already.

4. Click **Upload File** and select the offline [guardium-hdfs-uc.zip](https://github.com/IBM/universal-connectors/releases/download/v1.5.6/logstash-filter-hdfs_guardium_filter.zip) file. After it is uploaded, click OK.

5. Click the Plus sign to open the Connector Configuration dialog box.

6. Type a name in the Connector name field.

7. Update the input section to add the details from the [hdfsFilebeat.conf](./hdfsFilebeat.conf) file's input part, omitting the keyword "input{" at the beginning and its corresponding "}" at the end. Provide required details for DB server name, username and password for making JDBC connectivity.

8. Update the filter section to add the details from the [hdfsFilebeat.conf](./hdfsFilebeat.conf) file's filter part, omitting the keyword "filter{" at the beginning and its corresponding "}" at the end. Provide the same DB server name as in above step against the Server_Hostname attribute in the filter section.

9. Click Save. Guardium validates the new connector, and enables the universal connector if it was disabled. After it is validated, it appears in the Configure Universal Connector page.


## Configuring the HDFS filters in Guardium Data Security Center

To configure this plug-in for Guardium Data Security Center, follow [this guide.](https://github.com/IBM/universal-connectors/blob/main/docs/Guardium%20Insights/3.2.x/UC_Configuration_GI.md)

In the input configuration section, refer to the Filebeat section.

