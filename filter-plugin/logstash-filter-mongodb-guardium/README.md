# MongoDB-Guardium Logstash filter plug-in
### Meet MongoDB
* Tested versions: 4.2, 4.4
* Environment: On-premise(Only Enterprise version is supported), Iaas, IBM Cloud
* Supported Guardium versions:
    * Guardium Data Protection: 11.3 and above
      * Supported inputs:
        * Syslog (push)
        * Filebeat (push)
        * [MongoDB Atlas](../../input-plugin/logstash-input-mongo-atlas/README.md) (pull)
		* IBM Cloud KafkaStreams (pull)
    * Guardium Data Security Center SaaS: 1.0
      * Supported inputs:
        * Filebeat (push)
        * MongoDB Atlas (pull)

This is a [Logstash](https://github.com/elastic/logstash) filter plug-in for the universal connector that is featured in IBM Security Guardium. It parses events and messages from MongoDB audit/activity logs into a Guardium record instance (which is a standard structure made out of several parts). The information is then pushed into Guardium. Guardium records include the accessor (the person who tried to access the data), the session, data, and exceptions. If there are no errors, the data contains details about the query "construct". The construct details the main action (verb) and collections (objects) involved.  

The plug-in is free and open-source (Apache 2.0). It can be used as a starting point to develop additional filter plug-ins for Guardium universal connector.

## Follow the below link to set up and use IBM Cloud MongoDB

[IBM Cloud MongoDB README](./IBMCloudMongoDB_README.md)

## Documentation
### Supported audit messages & commands: 
* authCheck: 
    * find, insert, delete, update, create, drop, etc.
    * aggregate with $lookup(s) or $graphLookup(s)
    * applyOps: An internal command that can be triggered manually to create or drop collection. The command object is written as "\[json-object\]" in Guardium. Details are included in the Guardium Full SQL field, if available. 
* authenticate (with error only) 

Notes: 
* For the events to be handled propertly: 
    * MongoDB access control must be set, as messages without users are removed. 
    * authCheck and authenticate events should not be filtered out of the MongoDB audit log messages.
* Other MongoDB events and messages are removed from the pipeline, since their data is already parsed in the authCheck message.
* Non-MongoDB events are skipped, but not removed from the pipeline, since they may be used by other filter plug-ins.

### Supported errors:  

* Authentication error (18) – A failed login error.
* Authorization error (13) - To see the "Unauthorized ..." description in Guardium, you must extend the report and add the "Exception description" field.

The filter plug-in also supports sending errors. For this, MongoDB access control must be configured before the events will be logged. For example, edit _/etc/mongod.conf_ so that it includes:

    security:  
        authorization: enabled

*IPv6* addresses are typically supported by the MongoDB and filter plug-ins, however this is not fully supported by the Guardium pipeline. 

## Filter notes
* The filter supports events sent through Syslog or Filebeat. It relies on the "mongod:" or "mongos:" prefixes in the event message for the JSON portion of the audit to be parsed. 
* Field _server_hostname_ (required) - Server hostname is expected (extracted from the nested field "name" inside the host object of the Filebeat message).
* Field _server_ip_ - States the IP address of the MongoDB server, if it is available to the filter plug-in. The filter will use this IP address instead of localhost IP addresses that are reported by MongoDB, if actions were performed directly on the database server. 
* The client "Source program" is not available in messages sent by MongoDB. This is because this data is sent only in the first audit log message upon database connection - and the filter plug-in doesn't aggregate data from different messages.  
* If events with "(NONE)" local/remote IP addresses are not filtered, the filter plug-in will convert the IP to "0.0.0.0", as a valid format for IP is needed. However, this is atypical, since as messages without users are filtered out.
* Events in the filter are not removed, but tagged if not parsed (see [Filter result](#filter-result), below).
* The filter also masks the audit messages of type MongoDB authCheck: Currently, most field values are replaced with "?" in a naïve process, where most command arguments are redacted, apart from the _command_, _$db_, and _$lookup_ & _$graphLookup_ required arguments (_from_, _localField_, _foreignField_, _as_, _connectFromField_, _connectToField_).
* The "type" fields should match in the input and the filter configuration sections. This field should be unique for every individual connector added.

## Example 
### Filebeat input

A typical original log file looks like:

```
{ "atype" : "authCheck", "ts" : { "$date" : "2020-02-16T03:21:58.185-0500" }, "local" : { "ip" : "127.0.30.1", "port" : 0 }, "remote" : { "ip" : "127.0.20.1", "port" : 0 }, "users" : [], "roles" : [], "param" : { "command" : "find", "ns" : "config.transactions", "args" : { "find" : "transactions", "filter" : { "lastWriteDate" : { "$lt" : { "$date" : "2020-02-16T02:51:58.185-0500" } } }, "projection" : { "_id" : 1 }, "sort" : { "_id" : 1 }, "$db" : "config" } }, "result" : 0 }
```
The Filebeat version of the same file looks like:
```
{
 "@version" => "1",
 "input" => { "type" => "log"},
 "tags" => [[0] "beats_input_codec_plain_applied"],
 "@timestamp" => 2020-06-11T13:46:20.663Z,
 "log" => {"offset" => 1997890,"file" => { "path" =>"C:\\Users\\Name\\Desktop\\p1.log" }},
 "ecs" => {"version" => "1.4.0"},
 "type" => "filebeat",
 "agent" => {
  "ephemeral_id" => 
  "b7d849f9-dfa9-4d27-be8c-20061b1facdf",
  "id" => 
  "a54b2184-0bb5-4683-a039-7e1c70f1a57c",
  "version" => "7.6.2",
  "type" => "filebeat",
  "hostname" => "<name>"
 },
 "message" =>"{ \"atype\" : \"authCheck\", \"ts\" : { \"$date\" : \"2020-02-16T03:21:58.185-0500\" }, \"local\" : { \"ip\" : \"127.0.30.1\", \"port\" : 0 }, \"remote\" : { \"ip\" : \"127.0.20.1\", \"port\" : 0 }, \"users\" : [], \"roles\" : [], \"param\" : { \"command\" : \"find\", \"ns\" : \"config.transactions\", \"args\" : { \"find\" : \"transactions\", \"filter\" : { \"lastWriteDate\" : { \"$lt\" : { \"$date\" : \"2020-02-16T02:51:58.185-0500\" } } }, \"projection\" : { \"_id\" : 1 }, \"sort\" : { \"_id\" : 1 }, \"$db\" : \"config\" } }, \"result\" : 0 }",
 "host" => {
  "architecture" => 
  "x86_64",
  "id" => "d4e2c297-47bf-443a-8af8-e921715ed047",
  "os" => {
   "version" => "10.0",
   "kernel" => "10.0.18362.836 (WinBuild.160101.0800)",
   "build" => "18363.836",
   "name" => "Windows 10 Enterprise",
   "platform" => "windows",
   "family" => "windows"
  },
  "name" => "<name>",
  "hostname" => "<name>"
 }
}
```

## Configuring audit logs on MongoDB and forwarding to Guardium via Filebeat

First, configure the MongoDB native audit logs so that they can be parsed by Guardium. Then, configure Filebeat to forward the audit logs to the Guardium universal connector. This implementation supports Linux and Windows database servers.

### Before you begin

-   Use Filebeat whenever possible. It is the natural solution for integration with Logstash. It supports load balancing, and it has fewer limitations than Syslog for integration with the Guardium universal connector.
-   Filebeat must be installed on your database server. For more information on installation, see [https://www.elastic.co/guide/en/beats/filebeat/current/setup-repositories.html\#\_yum](https://www.elastic.co/guide/en/beats/filebeat/current/setup-repositories.html#_yum). The recommended Filebeat version is 7.5.0 and higher.
-   Native audit configuration is performed by the database admin.
-   Filebeat cannot handle messages over approximately 1 GB. Make sure the MongoDB does not save files larger than this limit \(by using `logRotate`\). File messages that exceed the limit are dropped.
-   You can configure multiple collectors simultaneously by using GIM [Configuring the GIM client to handle Filebeat and Syslog on MongoDB](https://github.com/IBM/universal-connectors/blob/main/docs/general%20topics/GIM.md). If you configure collectors manually, you need to configure them individually.
-   For more information about MongoDB native audit, see [https://docs.mongodb.com/manual/core/auditing/](https://docs.mongodb.com/manual/core/auditing/).

### Procedure

1.  Configure the MongoDB audit logs in the file mongod.conf on a Linux server, or mongod.cfg on a Windows server.

 a.  Configure the AuditLog section in the mongod.conf file.

      -   destination: file
      -   format: JSON
      -   path: /var/log/mongodb/<filename\>.json for example /var/log/mongodb/auditLog.json


   b. Add the following field to audit the `auditAuthorizationSuccess` messages:
```
   setParameter: {auditAuthorizationSuccess: true}
```

  c. Add or uncomment the security section and edit the following parameter:
 ```
    authorization: enabled
 ```

  d.  `filter`: For the Guardium universal connector MongoDB filter to handle events properly, a few conditions must exist:  
   * MongoDB access control must be set. (Messages without users are removed.)
   * `authCheck` and `authenticate` events are not filtered out from the MongoDB audit log messages. 
   Verify that the filter section contains at least the following commands:
     ```
       '{ atype: { $in: ["authCheck", "authenticate"] }'
     ```
     To narrow down the events, you can tweak the filter.
     For example, To audit only the delete actions made in MongoDB, add the following suffix to the filter section:
     ```
       '{ atype: { $in: ["authCheck", "authenticate"] } '
       "param.command": { $in: ["
       delete"] } }'
     ```
   * Auditing all commands can lead to excessive records. To prevent performance issues, make sure you have `authCheck` and `authenticate` log types, and any other commands you want to see. The filter parameters are an allowed list. They define what you see in the logs, not what is filtered from the logs. For more information about the MongoDB filter, see [Configuring Audit Filters](https://docs.mongodb.com/manual/tutorial/configure-audit-filters/) and [Configuring Filebeat](https://www.elastic.co/guide/en/beats/filebeat/current/filebeat-installation-configuration.html#installation).
   
   **Note:** The spaces in the configuration file are important, and must be located in the file as presented here.

  e. After configuration, the file has these lines:
```
      ...
      auditLog:
      destination: file
      format: JSON
      path: /var/lib/mongo/auditLog.json
      filter: '{"$or": [{ atype: { $ne: ["authCheck"] }, "param.command": { $in: [ "find", "insert", "delete", "update", "findandmodify", "create", "drop", "mapReduce", "applyOps", "eval", "resetError","renameCollection","adminCommand"] } },{ atype: "authCheck", "param.command": { $in: ["aggregate"]}}]}'
      setParameter: {auditAuthorizationSuccess: true}
      ...
      security:
        authorization: enabled
```

    **Important:** The MongoDB needs to be restarted for the configuration changes to take effect.


2.  Configure the Filebeat data shipper to forward the audit logs to the Guardium universal connector. In the file filebeat.yml, usually located in /etc/filebeat/filebeat.yml, modify the Filebeat inputs section.

    a.  Select a template from the Universal Connector page and enter your desired port in the port line, beginning at port 5001. \(Use a new port for each new future connection.\) Save the configuration.

    b.  Change the `enabled` field to `true`, and add the path of the audit logs. For example:

        
        filebeat.inputs
        - type: log
          enabled: true
          paths:
            - /var/log/mongodb/auditLog.json
            #- c:\programdata\elasticsearch\logs\*
            tags: ["mongodb"]
        

    c.  If you send multiple, different data sources from the same server on the same port:

- Attach a different tag to each input log. Then, use the tags when you configure the connector
- Use the ```tags``` parameter from the following code while configuring the connector:

            
            # ============================== Filebeat inputs ===============================
            filebeat.inputs:
            # Each -is an input. Most options can be set at the input level, so
            # you can use different inputs for various configurations.
            # Below are the input specific configurations.
            -type: log  
            # Change to true to enable this input configuration.
              enabled: true  
              # Paths that should be crawled and fetched. Glob based paths.
              paths: /var/lib/mongo/auditLog.json
              tags: ["mongodb"]
            

    d.  In the Outputs section:

- Make sure that Elasticsearch output is commented out.
  - Add or uncomment the Logstash output and edit the following parameters:
  - Add all the Guardium Universal Connector IPs and ports:

            hosts: [“<ipaddress1>:<port>”,”<ipaddress2>:<port>,”<ipaddress3>:<port>”...]
                

  - Use the same port you selected when configuring the Universal Connector.
  - Enable load balancing:

                
            loadbalance: true
                
   - For more information on Elastic's Filebeat load-balancing, see: [https://www.elastic.co/guide/en/beats/filebeat/current/load-balancing.html](https://www.elastic.co/guide/en/beats/filebeat/7.17/load-balancing.html)

  - More optional parameters are described in the Elastic official documentation: [https://www.elastic.co/guide/en/beats/filebeat/current/logstash-output.html](https://www.elastic.co/guide/en/beats/filebeat/current/logstash-output.html)

      A typical original log file looks like:

  ```
        { "atype" : "authCheck", "ts" : { "$date" : "2020-02-16T03:21:58.185-0500" }, "local" : { "ip" : "127.0.30.1", "port" : 0 }, "remote" : { "ip" : "127.0.20.1", "port" : 0 }, "users" : [], "roles" : [], "param" : { "command" : "find", "ns" : "config.transactions", "args" : { "find" : "transactions", "filter" : { "lastWriteDate" : { "$lt" : { "$date" : "2020-02-16T02:51:58.185-0500" } } }, "projection" : { "_id" : 1 }, "sort" : { "_id" : 1 }, "$db" : "config" } }, "result" : 0 }
  ```

      The Filebeat version of the same file looks like:

        
        {
         "@version" => "1",
         "input" => { "type" => "log"},
         "tags" => [[0] "beats_input_codec_plain_applied"],
         "@timestamp" => 2020-06-11T13:46:20.663Z,
         "log" => {"offset" => 1997890,"file" => { "path" =>"C:\\Users\\Name\\Desktop\\p1.log" }},
         "ecs" => {"version" => "1.4.0"},
         "type" => "filebeat",
         "agent" => {
          "ephemeral_id" =>
          "b7d849f9-dfa9-4d27-be8c-20061b1facdf",
          "id" =>
          "a54b2184-0bb5-4683-a039-7e1c70f1a57c",
          "version" => "7.6.2",
          "type" => "filebeat",
          "hostname" => "<name>"
         },
         "message" =>"{ \"atype\" : \"authCheck\", \"ts\" : { \"$date\" : \"2020-02-16T03:21:58.185-0500\" }, \"local\" : { \"ip\" : \"127.0.30.1\", \"port\" : 0 }, \"remote\" : { \"ip\" : \"127.0.20.1\", \"port\" : 0 }, \"users\" : [], \"roles\" : [], \"param\" : { \"command\" : \"find\", \"ns\" : \"config.transactions\", \"args\" : { \"find\" : \"transactions\", \"filter\" : { \"lastWriteDate\" : { \"$lt\" : { \"$date\" : \"2020-02-16T02:51:58.185-0500\" } } }, \"projection\" : { \"_id\" : 1 }, \"sort\" : { \"_id\" : 1 }, \"$db\" : \"config\" } }, \"result\" : 0 }",
         "host" => {
          "architecture" =>
          "x86_64",
          "id" => "d4e2c297-47bf-443a-8af8-e921715ed047",
          "os" => {
           "version" => "10.0",
           "kernel" => "10.0.18362.836 (WinBuild.160101.0800)",
           "build" => "18363.836",
           "name" => "Windows 10 Enterprise",
           "platform" => "windows",
           "family" => "windows"
          },
          "name" => "<name>",
          "hostname" => "<name>"
         }
        }
        

3.  Restart Filebeat to effect these changes.

-  Linux: Enter the command:

        
        sudo service filebeat restart
        

- Windows: Restart in the Services window

#### For details on configuring Filebeat connection over SSL, refer [Configuring Filebeat to push logs to Guardium](https://github.com/IBM/universal-connectors/blob/main/input-plugin/logstash-input-beats/README.md#configuring-filebeat-to-push-logs-to-guardium).


### What to do next

Enable the universal connector on your collector. [Enabling the Guardium universal connector on collectors](https://www.ibm.com/docs/en/SSMPHH_11.4.0/com.ibm.guardium.doc.stap/guc/cfg_guc_input_filters.html)


## Configuring the MongoDB filters in Guardium Data Security Center
To configure this plug-in for Guardium Data Security Center, follow [this guide.](/docs/Guardium%20Insights/3.2.x/UC_Configuration_GI.md)
In the input configuration section, refer to the Filebeat section.

### Not yet supported
1. Support fields.
2. Embedded documents as inner objects.
