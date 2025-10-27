# MySql-Percona-Guardium Logstash filter plug-in
### Meet MySql-Percona
* Tested versions: 5.7.31-34
* Environment: On-premise, Iaas
* Supported inputs: Filebeat (push)
* Supported Guardium versions:
  * Guardium Data Protection: 11.4 and above
  * Guardium Data Security Center SaaS: 1.0

This is a [Logstash](https://github.com/elastic/logstash) filter plug-in for the universal connector that is featured in IBM Security Guardium.

## 1. Configuring the Mysql server

This plug-in is an extension of the MySql-Guardium Logstash filter plug-in. For more information, see [MySql-Guardium Logstash filter plug-in](https://github.ibm.com/Activity-Insights/universal-connectors/blob/master/filter-plugin/logstash-filter-mysql-guardium/README.md).

## 2. Installing and enabling auditing

### Percona configuration

1. On the database, update the file: /etc/percona-server.conf.d/mysqld.cnf.

    ```
      symbolic-links=0
      bind_address=0.0.0.0
      log-error=/var/log/mysqld.log
      pid-file=/var/run/mysqld/mysqld.pid
      audit_log_format=JSON
      audit_log_handler=FILE
      audit_log_file=/var/lib/mysql/audit.log
  
    ```
2. Restart the mysql service.


## 3. Configuring Filebeat to push logs to Guardium

### Procedure:

1. To install Filebeat on your system, follow the steps in this topic:
   https://www.elastic.co/guide/en/beats/filebeat/current/filebeat-installation-configuration.html#installation

## b. Filebeat configuration:

To use Logstash to perform additional processing on the data collected by Filebeat, we need to configure Filebeat to use Logstash. To do this, modify the filebeat.yml file which you can find inside the folder where Filebeat is installed. Follow these instructions for finding the installation directory:
https://www.elastic.co/guide/en/beats/filebeat/current/directory-layout.html

### Procedure:

1. Configuring the input section :

   • Locate "filebeat.inputs" in the filebeat.yml file, and then add the following parameters.

    ```
    type: filestream
    id: <ID>
    #Change to true to enable this input configuration.
    enabled: true 
    paths:
    - /var/lib/mysql/audit.log
    tags: ["mysqlpercona"]
  
    ```

2. Configuring the output section:

   • Locate "output" in the filebeat.yml file, and then add the following parameters.

   • Disable Elasticsearch output by commenting it out.

   • Enable Logstash output by uncommenting the Logstash section. For more information, see https://www.elastic.co/guide/en/beats/filebeat/current/logstash-output.html#logstash-output

   For example:

    ```
    output.logstash:
    #The Logstash hosts
    hosts: ["<Guardium IP>:5045"] #just to ip/host name of the gmachine

    ```
   In addition, events are configured with the add_locale, add_host_metadata, and add_tags processors (to add an "hdfs" tag).


3. To learn more about Filebeat processors, click [here](https://www.elastic.co/guide/en/beats/filebeat/current/filtering-and-enhancing-data.html#using-processors).

#### For details on configuring Filebeat connection over SSL, refer [Configuring Filebeat to push logs to Guardium](https://github.com/IBM/universal-connectors/blob/main/input-plugin/logstash-input-beats/README.md#configuring-filebeat-to-push-logs-to-guardium).


## Configuring the MySQL filters in Guardium Data Protection (GDP)

The Guardium universal connector is the Guardium entry point for native audit logs.
The universal connector identifies and parses received events, and converts them to a standard Guardium format.
The output of the Guardium universal connector is forwarded to the Guardium sniffer on the collector, for policy and auditing enforcements.

### Before you begin

• Configure the policies you require. See [policies](/docs/#policies) for more information.

• You must have permission for the S-Tap Management role. The admin user includes this role by default.

• MySql-Percona-Guardium Logstash filter plug-in is automatically available with Guardium Data Protection versions 12.x, 11.4 with appliance bundle 11.0p490 or later or Guardium Data Protection version 11.5 with appliance bundle 11.0p540 or later releases.

**Note**: For Guardium Data Protection version 11.4 without appliance bundle 11.0p490 or prior or Guardium Data Protection version 11.5 without appliance bundle 11.0p540 or prior, download the [mysql-percona-offline-plugin.zip](./MysqlPerconaOverFilebeatPackage/MysqlPercona/logstash-filter-mysql_percona_filter.zip) plug-in. (Do not unzip the offline-package file throughout the procedure). 

### Procedure

1. On the collector, go to Setup > Tools and Views > Configure Universal Connector.

2. First enable the Universal Guardium connector, if it is disabled already.

4. Click **Upload File** and select the offline [mysql-percona-offline-plugin.zip](./MysqlPerconaOverFilebeatPackage/MysqlPercona/logstash-filter-mysql_percona_filter.zip) file. After it is uploaded, click OK.

5. Click the Plus sign to open the Connector Configuration dialog box.

6. Type a name in the Connector name field.

7. Update the input section to add the details from the [perconaFilebeat.conf](./perconaFilebeat.conf) file's input part, omitting the keyword "input{" at the beginning and its corresponding "}" at the end. Provide required details for DB server name, username and password for making JDBC connectivity.

8. Update the filter section to add the details from the [perconaFilebeat.conf](./perconaFilebeat.conf) file's filter part, omitting the keyword "filter{" at the beginning and its corresponding "}" at the end. Provide the same DB server name as in above step against the Server_Hostname attribute in the filter section.

9. Click Save. Guardium validates the new connector, and enables the universal connector if it was disabled. After it is validated, it appears in the Configure Universal Connector page.


## Configuring the MySQL filters in Guardium Insights
To configure this plug-in for Guardium Insights, follow [this guide.](/docs/Guardium%20Insights/3.2.x/UC_Configuration_GI.md).
In the input configuration section, refer to the Filebeat section.
