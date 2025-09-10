# Greenplum-Guardium Logstash filter plug-in
### Meet Greenplum
* Tested versions: 6.21.0
* Environment: On-premise
* Supported inputs: Filebeat (push)
* Supported Guardium versions:
  * Guardium Data Protection: 11.4 and above
  * Guardium Data Security Center SaaS: 1.0

This is a [Logstash](https://github.com/elastic/logstash) filter plug-in for the universal connector that is featured in IBM Security Guardium. It parses events and messages from the GreenplumDB log into a [Guardium record](https://github.com/IBM/universal-connectors/blob/main/common/src/main/java/com/ibm/guardium/universalconnector/commons/structures/Record.java) instance (which is a standard structure made out of several parts). Information is then sent over to the Guardium. Guardium records include the accessor (the person who tried to access the data), the session, data, and exceptions. If there are no errors, the data contains details about the query and the Guardium sniffer parses the Greenplum queries. As of now,the Greenplum plug-in only supports Guardium Data Protection.

The plug-in is free and open-source (Apache 2.0). It can be used as a starting point to develop additional filter plug-ins for Guardium universal connector.

## Enabling the audit Logs:

### Procedure 
1. Run the following command to start the database server.
```
$ gpstart
```
2. Modify the postressql.conf file to enable the audit logs, and execute the following command to open the file.

##### Edit
``` 
  sudo nano  ~/greenplum-db-node/gpmaster/gpsne-1/postgresql.conf
```
##### Uncomment this section:
```
  log_error_verbosity = default
  log_statement = 'all‘
```
* Note: After enabling audit logging, stop the database server using $ gpstop, start the database server using $ gpstart, and then verify that audit logging is now enabled.

3. Run the following command to stop the database server.
```
$ gpstop
```
* Note: Remember to stop the database server before turning the Greenplum database instance off. Failure to do so may corrupt the instance.

4. Run the below query to generate some audit logs. For example:
```
$ psql -d template1 -c "INSERT INTO products5 VALUES ('Cheese',10, 11);" -h localhost -p 5432
```
## Viewing the audit logs
The audit logs can be viewed in this location:-
```
$ cat ~/greenplum-db-node/gpmaster/gpsne-1/pg_log/<file-name.csv>
```
After viewing the audit logs , user can download the logs from a remote machine to a local machine using the scp command:- 
```
scp -i <private key pair> <username>@<public IP>:<file source on EC2> <file destination on local>
```

## Configuring Filebeat to push logs to Guardium

### a. Filebeat Installation

#### Procedure

To install Filebeat on your system, follow the steps in this topic: https://gryzli.info/2019/02/15/installing-and-configuring-filebeat-on-centos-rhel/.

### b. Filebeat Configuration

Filebeat must be configured to send the output to the chosen Logstash host and port. In addition, events are filtered out with the exclude_lines. To do this, modify the filebeat.yml file which you can find inside the folder where Filebeat is installed.You can learn more about Filebeat Configuration [here](https://www.elastic.co/guide/en/beats/filebeat/current/filebeat-input-log.html#:~:text=include_lines%20edit%20A%20list%20of%20regular%20expressions%20to,the%20list.%20By%20default%2C%20all%20lines%20are%20exported.).

### Procedure

1. Configure the input section:

```
• Locate "filebeat.inputs" in the filebeat.yml file, then add the following parameters.
    filebeat.inputs:
    - type: filestream
    - id: <ID>
    enabled: true
    paths:
    - /home/ec2-user/greenplum-db-node/gpmaster/gpsne-1/pg_log/*.csv

• To process multi-line audit events, add the settings the same inputs section.

   multiline.pattern: '^[\d]{4}[-][\d]{1,2}[-][\d]{1,2}'
   multiline.negate: true
   multiline.match: after

• Add the tags to uniquely identify the GreenplumDB events from the rest.
   tags: ["GreenplumDB_On_Premise"]

```

2. Configure the output section:
```
• Locate "output" in the filebeat.yml file, then add the following parameters.

• Disable Elasticsearch output by commenting it out.

• Enable Logstash output by uncommenting the Logstash section. 
For example:
  output.logstash:
    hosts: ["20.115.120.146:5085"]

• The hosts option specifies the Logstash server and the port (5001) where Logstash is configured to listen for incoming Beats connections.

•You can set any port number except 5044, 5141, and 5000 (as these are currently reserved in Guardium v11.3 and v11.4 ).
```
```
• Locate "Processors" in the filebeat.yml file and then add below attribute to get timezone of Server:
	For more information, see https://www.elastic.co/guide/en/beats/filebeat/current/add-locale.html
	For example:-
       processors:
	 - add_locale: ~
 ```

  
_For more information, see https://www.elastic.co/guide/en/beats/filebeat/current/logstash-output.html#logstash-output_


3. To learn how to start Filebeat, see https://www.elastic.co/guide/en/beats/filebeat/current/filebeat-installation-configuration.html#start

#### For details on configuring Filebeat connection over SSL, refer [Configuring Filebeat to push logs to Guardium](https://github.com/IBM/universal-connectors/blob/main/input-plugin/logstash-input-beats/README.md#configuring-filebeat-to-push-logs-to-guardium).


## Configuring the Greenplum filter in Guardium
The Guardium universal connector is the Guardium entry point for native audit logs. The Guardium universal connector identifies and parses the received events, and converts them to a standard Guardium format. The output of the Guardium universal connector is forwarded to the Guardium sniffer on the collector, for policy and auditing enforcements. Configure Guardium to read the native audit logs by customizing the Greenplumdb template.

### Before you begin

*  Configure the policies you require. See [policies](/docs/#policies) for more information.
* You must have permission for the S-Tap Management role.The admin user includes this role by default.
* Greenplum-Guardium Logstash filter plug-in is automatically available with Guardium Data Protection versions 12.x, 11.4 with appliance bundle 11.0p490 or later or Guardium Data Protection version 11.5 with appliance bundle 11.0p540 or later releases.

**Note**: For Guardium Data Protection version 11.4 without appliance bundle 11.0p490 or prior or Guardium Data Protection version 11.5 without appliance bundle 11.0p540 or prior, download the [logstash-filter-greenplumdb_guardium_filter.zip](https://github.com/IBM/universal-connectors/releases/download/v1.5.6/logstash-filter-greenplumdb_guardium_filter.zip) plug-in. (Do not unzip the offline-package file throughout the procedure).



### Procedure
1. On the collector, go to Setup > Tools and Views > Configure Universal Connector.
2. Enable the universal connector if it is disabled.
3. Click Upload File and select the offline [logstash-filter-greenplumdb_guardium_filter.zip](https://github.com/IBM/universal-connectors/releases/download/v1.5.6/logstash-filter-greenplumdb_guardium_filter.zip) plug-in. After it is uploaded, click OK. This step is not necessary for Guardium Data Protection v11.0p490 or later, v11.0p540 or later, v12.0 or later.
4. Click the Plus icon to open the Connector Configuration dialog box.
5. Type a name in the Connector name field.
6. Update the input section to add the details from the [greenplumdb.conf](https://github.com/IBM/universal-connectors/raw/main/filter-plugin/logstash-filter-onPremGreenplumdb-guardium/greenplumdb.conf) file's input part, omitting the keyword "input{" at the beginning and its corresponding "}" at the end.
7. Update the filter section to add the details from the [greenplumdb.conf](https://github.com/IBM/universal-connectors/raw/main/filter-plugin/logstash-filter-onPremGreenplumdb-guardium/greenplumdb.conf) file's filter part, omitting the keyword "filter{" at the beginning and its corresponding "}" at the end.
8. The "type" fields should match in input and filter configuration sections. This field should be unique for every individual connector added.
9. The "tags" parameter in filter configuration should match the value of attribute tags configured in filebeat configuration for a connector.
10. Click Save. Guardium validates the new connector and displays it in the Configure Universal Connector page.

## 6. Limitations

- The following important fields can't be mapped with Greenplumdb audit logs:
  - SourceProgram: Not Available with audit logs.
  - OsUser: Not Available with audit logs.
  - ClientHostName: Not Available with audit logs.

## 7. Configuring the Greenplum filter in Guardium Data Security Center

To configure this plug-in for Guardium Data Security Center, follow [this guide.](/docs/Guardium%20Insights/3.2.x/UC_Configuration_GI.md)

In the input configuration section, refer to the Filebeat section.
