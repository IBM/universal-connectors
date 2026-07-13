# MongoDB-Guardium Logstash filter plug-in
### Meet MongoDB
* Tested versions: 4.2, 4.4, 8.0
* Environment: On-premise, Iaas, IBM Cloud
* Supported Guardium versions:
    * Guardium Data Protection: 11.3 and above
        * Supported inputs:
            * Syslog (push)
            * Filebeat (push)
            * [MongoDB Atlas](https://github.com/IBM/universal-connectors/blob/main/input-plugin/logstash-input-mongo-atlas/README.md) (pull)
            * IBM Cloud KafkaStreams (pull)
    * Guardium Data Security Center: 3.2 and above
        * Supported inputs:
            * Filebeat (push)
    * Guardium Data Security Center SaaS: 1.0
        * Supported inputs:
            * Filebeat (push)
            * MongoDB Atlas (pull)

This is a [Logstash](https://github.com/elastic/logstash) filter plug-in for the universal connector that is featured in IBM Security
Guardium. It parses events and messages from MongoDB audit/activity logs into a Guardium record instance
(which is a standard structure made out of several parts). The information is then pushed into
Guardium. Guardium records include the accessor (the person who tried to access the data), the session,
data, and exceptions. If there are no errors, the data contains details about the query "construct".
The construct details the main action (verb) and collections (objects) involved.

The plug-in is free, open-source (Apache 2.0) and is written in Java.

## Follow the below link to set up and use IBM Cloud MongoDB

[IBM Cloud MongoDB README](./IBMCloudMongoDB_README.md)

## 1. Configure the mongodb database

1. Install the flavor of MongoDB according to the environment by following the instructions in
   the documentation [here](https://www.mongodb.com/docs/manual/installation/).
2. The MongoDB can be managed using a provided utility named MongoDB Shell(mongosh).
   The documentation of the utility is available [here](https://www.mongodb.com/docs/mongodb-shell/).

## 2. Enabling the audit logs:
MongoDB native audit configuration is performed by the database admin. So the following procedure
must be performed by the user who is administrator.

1. Configure the MongoDB audit logs in the file `mongod.conf` on a Linux server,
   or `mongod.cfg` on a Windows server.
2. Configure the AuditLog section in the mongod config file.
    ```text
    -   destination: file
    -   format: JSON
    -   path: /var/log/mongodb/<filename\>.json, for example /var/log/mongodb/auditLog.json
    ```
3. Add the following field to audit the `auditAuthorizationSuccess` messages:
   ```text
   setParameter: {auditAuthorizationSuccess: true}
   ```
4. Add or uncomment the security section and edit the following parameter:
    ```text
    authorization: enabled
    ```
5. Following conditions must be met for the Guardium universal connector MongoDB filter to correctly handle the events:
    * MongoDB access control must be set. (Messages without users are removed.)
    * `authCheck` and `authenticate` events are not filtered out from the MongoDB audit log messages.
      Verify that the filter section contains at least the following commands:
      ```text
        '{ atype: { $in: ["authCheck", "authenticate"] }'
      ```
      To narrow down the events, you can tweak the filter.
      For example, To audit only the delete actions made in MongoDB, add the following suffix to the filter section:
      ```text
        '{ atype: { $in: ["authCheck", "authenticate"] } '
        "param.command": { $in: ["
        delete"] } }'
      ```
    * Auditing all commands can lead to excessive records. To prevent performance issues, make sure
      you have `authCheck` and `authenticate` log types, and any other commands you want to see. The
      filter parameters are an allowed list. They define what you see in the logs, not what is filtered
      from the logs. For more information about the MongoDB filter,
      see https://docs.mongodb.com/manual/tutorial/configure-audit-filters/ <br/><br/>
      **Note**: The spaces in the configuration file are important, and must be located in the file as presented here.

6. After configuration, the file should have these lines:
     ```text
      ...
      auditLog:
      destination: file
      format: JSON
      path: /var/lib/mongo/auditLog.json
      filter: '{"$or": [{ atype: { $ne: ["authCheck"] }, "param.command": { $in: [ "find", "insert", "delete", "update", "findandmodify", "create", "drop", "mapReduce", "applyOps", "eval", "resetError","renameCollection","adminCommand"] } },{ atype: "authCheck", "param.command": { $in: ["aggregate"]}},{atype:"authenticate", result:{ $ne: 0 }}]}'
      setParameter: {auditAuthorizationSuccess: true}
      ...
      security:
        authorization: enabled
     ```
7. Restart MongoDB to apply the configuration changes.

## 3. Viewing the audit logs:
To view the logs, go to the path configured in the audit configuration file in the previous steps.

* A typical log line is seen as below,
    ```json
    {
        "atype": "authCheck",
        "ts": {
            "$date": "2020-02-16T03:21:58.185-0500"
        },
        "local": {
            "ip": "127.0.30.1",
            "port": 0
        },
        "remote": {
            "ip": "127.0.20.1",
            "port": 0
        },
        "users": [],
        "roles": [],
        "param": {
            "command": "find",
            "ns": "config.transactions",
            "args": {
                "find": "transactions",
                "filter": {
                    "lastWriteDate": {
                        "$lt": {
                            "$date": "2020-02-16T02:51:58.185-0500"
                        }
                    }
                },
                "projection": {
                    "_id": 1
                },
                "sort": {
                    "_id": 1
                },
                "$db": "config"
            }
        },
        "result": 0
    }
    ```
## 4. Configuring Filebeat to push logs to Guardium
### a. Filebeat installation
To install Filebeat on your system, follow the steps in this
topic: https://www.elastic.co/guide/en/beats/filebeat/current/filebeat-installation-configuration.html#installation

### b. Filebeat configuration
To use Logstash to perform additional processing on the data collected by Filebeat,
configure Filebeat to use Logstash. To do this, modify the filebeat.yml file which you can find
inside the folder where Filebeat is installed. Follow these instructions to find the installation
directory: https://www.elastic.co/guide/en/beats/filebeat/current/directory-layout.html


1. Configuring the input section:
    1. Locate "filebeat.inputs" in the `filebeat.yml` file, then add the following parameters. Change the
       `enabled` field to `true`, and add the `path` of the audit logs. For example:
         ```text
         filebeat.inputs
             - type: filestream   
             - id: <ID>
               enabled: true
               paths:
                 - /var/log/mongodb/auditLog.json
                 #- c:\programdata\elasticsearch\logs\*
               tags: ["mongodb"]
         ```
    2. If you send multiple, different data sources from the same server on the same port
        1. Attach a different tag to each input log. Then, use the tags when you configure the connector.
2. Configuring the output section:
    1. Locate "output" in the filebeat.yml file, then add the following parameters.
    2. Disable Elasticsearch output by commenting it out.
    3. Enable Logstash output by uncommenting the Logstash section. For more information,
       see https://www.elastic.co/guide/en/beats/filebeat/current/logstash-output.html#logstash-output
    4. For example:
       ```text
       output.logstash:
         hosts: [<host>:<port>]
       ```
    5. To enable load balancing:
       ```text
       loadbalance: true 
       ```
    6. The hosts option specifies the Logstash server and the port (5001) where Logstash is configured
       to listen for incoming Beats connections.
    7. You can set any port number except 5044, 5141, and 5000
       (as these are currently reserved in Guardium v11.3 and v11.4 ).
3. Restart Filebeat to effect these changes.
4. For details on configuring Filebeat connection over SSL, refer [Configuring Filebeat to push
   logs to Guardium](https://github.com/IBM/universal-connectors/blob/main/input-plugin/logstash-input-beats/README.md#configuring-filebeat-to-push-logs-to-guardium).

## 5. Configuring Syslog to push logs to Guardium
### Syslogs configuration:
To make the Logstash able to process the data collected by syslogs, configure available
syslog utility. The example is based on rsyslog utility available in many
versions of the Linux distributions. To check the service is active and running, execute the below
command:

```text
systemctl status rsyslog
```

#### Rsyslog installation guide:
* [Ubuntu](https://www.rsyslog.com/ubuntu-repository)
* [RHEL](https://www.rsyslog.com/rhelcentos-rpms)

1. Generate Certificate Authority (CA):
    * **Guardium Data Protection** <br/>
      To obtain the Certificate Authority content on the Collector, run the following API command:
      ```text
      grdapi generate_ssl_key_universal_connector
      ```
      This API command will display the content of the public Certificate Authority. Copy this certificate authority content to your database source and save it as a file named 'ca.pem' .

    * **Guardium Data Security Center - SaaS** <br/>
      Refer to the instructions provided [here](https://www.ibm.com/docs/en/gdsc/saas?topic=connector-connecting-data-source-by-using-universal#plugin_connection_configuration__title__15) to obtain the Certificate Authority
      and connection details for Guardium Insights-SaaS.
2. Create a file with name `mongo_syslog.conf` in the /etc/rsyslog.d/ directory with the content below in the
   snippet and change the values of target and port,
   ```text
    global(DefaultNetstreamDriverCAFile="/path/to/ca_file/ca.pem")
    # The template for message formatting
    $template UcMessageFormat,"%TIMESTAMP% %HOSTNAME% mongod: %msg%"

    module(load="imfile")
    ruleset(name="imfile_to_gdp") {
            action(type="omfwd"
            protocol="tcp"
            StreamDriver="gtls"
            StreamDriverMode="1"
            StreamDriverAuthMode="x509/certvalid"
            template="UcMessageFormat"
            target="<target_host>"
            port="<target_port>")
    }   

    input(
        type="imfile"
        file="/path/to/logs/directory/auditLog.json"
        # Keep the value of tag below as same as here,
        tag="syslog"
        ruleset="imfile_to_gdp"
    )
    ```
   This configuration reads the logs from the MongoDB log directory path and sends
   the syslog messages to the provided host (target_host) at the provided port (target_port).<br/> <br/>

   **NOTE**: For further configuration requirements that are specific to Guardium Insights - SaaS
   environment, please follow the instructions provided [here](https://github.com/IBM/universal-connectors/blob/main/docs/Guardium%20Insights/SaaS_1.0/UC_Configuration_GI.md#tcp-input-plug-in-configuration-for-connection-with-syslog).
   <br/><br/>

3. Include this file in the main rsyslog configurations file.
    1. Open the file `/etc/rsyslog.conf`.
    2. Append the below line at the end.
       ```text
       $IncludeConfig /etc/rsyslog.d/mongo_syslog.conf 
       ```
4. Restart the rsyslog utility.
   ```text
    systemctl restart rsyslog
   ```

## 6. Configuring the MongoDB filters in Guardium
The Guardium universal connector is the Guardium entry point for native audit logs. The universal
connector identifies and parses received events, and then converts them to a standard
Guardium format. The output of the universal connector is forwarded to the Guardium sniffer on
the collector, for policy and auditing enforcements. Configure Guardium to read the native
audit logs by customizing the MongoDB template.

**Important**

• Starting with Guardium Data Protection version 12.1, you can configuring the Universal Connectors in 2 ways. You can either use the legacy flow or the new flow.

• To configure Universal Connector by using the new flow, see [Managing universal connector configuration](https://www.ibm.com/docs/en/gdp/12.x?topic=connector-managing-universal-configuration) on the Guardium Universal Connector page.

• To configure the Universal Connector by using the legacy flow, use the procedure in this topic.

### Limitations
* The filter supports events sent through Syslog or Filebeat. It relies on the "mongod:" or "mongos:" prefixes in
  the event message for the JSON portion of the audit to be parsed.
* Field **server_hostname** (required) - Server hostname is expected (extracted from the nested field "name"
  inside the host object of the Filebeat message).
* Field **server_ip** - States the IP address of the MongoDB server, if it is available to the
  filter plug-in. The filter will use this IP address instead of localhost IP addresses
  that are reported by MongoDB, if actions were performed directly on the database server.
* The client "Source program" is not available in messages sent by MongoDB. This is because
  this data is sent only in the first audit log message upon database connection - and the
  filter plug-in doesn't aggregate data from different messages.


### Before You Begin
* Configure the policies you require. See [policies](https://github.ibm.com/chirag-soni/universal-connectors/blob/GRD-78729/docs/#policies) for more information.
* You must have permission for the S-Tap Management role. The admin user includes this role by default.
* Download the [logstash-filter-mongodb_guardium_filter.zip](./logstash-filter-mongodb_guardium_filter.zip) plug-in.
  This is not necessary for Guardium Data Protection v12.0 and later.

### Configuration
1. On the collector, go to ```Setup``` > ```Tools and Views``` > ```Configure Universal Connector```.
2. Enable the universal connector if it is disabled.
3. Click ```Upload File``` and select the
   offline [logstash-filter-mongodb_guardium_filter.zip](./logstash-filter-mongodb_guardium_filter.zip)
   plug-in. After it is uploaded, click OK.
4. Click the plus sign to open the Connector Configuration dialog box.
5. Type a name in the ```Connector name``` field.
6. Update the input section,
    1. To collect data over Filebeat, add the details from [mongoDBFilebeat.conf](./MongodbOverFilebeatPackage/mongodbFilebeat.conf)
       file input section, omitting the keyword "input{" at the beginning and its corresponding "}"
       at the end.
    2. To collect data over Syslogs, add the details from [mongoDBSyslog.conf](./MongoDBOverSyslogPackage/mongodbSyslog.conf) file input section,
       omitting the keyword "input{" at the beginning and its corresponding "}" at the end.
    3. To collect data over Mongo Atlas API, add the details from [mongoAtlas.conf](./MongodbOverMongoAtlasPackage/mongodbAtlas.conf) file input section,
       omitting the keyword "input{" at the beginning and its corresponding "}" at the end.
7. Update the filter section,
    1. To filter the data collected from the Filebeat, add the details from the
       [mongoDBFilebeat.conf](./MongodbOverFilebeatPackage/mongodbFilebeat.conf) file filter section, omitting the keyword
       "filter{" at the beginning and its corresponding "}" at the end.
    2. To filter the data collected from the Syslogs, add the details from the
       [mongoDBSyslog.conf](MongoDBOverSyslogPackage/mongodbSyslog.conf) file filter section,
       omitting the keyword "filter{" at the beginning and its corresponding "}" at the end.
    3. To filter the data collected from the Mongo Atlas API, add the details from the [mongoAtlas.conf](./MongodbOverMongoAtlasPackage/mongodbAtlas.conf)       file filter section, omitting the keyword "filter{" at the beginning and its corresponding "}" at the end.
8. The "type" fields should match in the input and the filter configuration sections. This field should be unique for every individual connector added. This is no longer required starting v12p20 and v12.1.
9. Click ```Save```. Guardium validates the new connector, and enables the universal connector if it was disabled. After it is validated, it appears in the Configure Universal Connector page.

## 7. Configuring the MongoDB filters in Guardium Data Security Center
To configure this plug-in for Guardium Insights, follow [this guide](https://github.com/IBM/universal-connectors/blob/main/docs/Guardium%20Insights/3.2.x/UC_Configuration_GI.md). For the input configuration step, refer
to the [Filebeat section](https://github.com/IBM/universal-connectors/blob/main/docs/Guardium%20Insights/3.2.x/UC_Configuration_GI.md#Filebeat-input-plug-in-configuration).