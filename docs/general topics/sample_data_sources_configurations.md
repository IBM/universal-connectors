# Configuring audit logs on MongoDB and forwarding to Guardium via Syslog

First, configure the MongoDB audit logs so that they can be parsed by Guardium. Then, configure Syslog to forward the audit logs to the Guardium universal connector. This implementation supports Linux database servers.

## Before you begin

-   Service rsyslog is installed. Preferred version: rsyslogd 8.24.0-52.el7.
-   Native audit configuration is performed by the database admin.
-   You can configure multiple collectors simultaneously using GIM \([Configuring the GIM to handle Filebeat and Syslog on MongoDB](https://www.ibm.com/docs/en/guardium/11.4?topic=connector-configuring-gim-handle-filebeat-syslog-mongodb)\). If you configure collectors manually, you need to configure them individually.

## About this task
You can filter out any native audit events that are irrelelvant before inputting the logs to Guardium. Filter them out at the datasource server, or with a filter plugin.

## Procedure

1.  Configure the MongoDB audit logs in the mongod.conf file.

     a. Configure the AuditLog section in the mongod.conf file.

      -   `destination`:syslog
      -   `format`:delete or comment out
      -   `path`:delete or comment out

    b. Add the following field to audit the `auditAuthorizationSuccess` messages:

        ```
        setParameter: {auditAuthorizationSuccess: true}
        ```

    c. Add or uncomment the security section and edit the following parameter:

        ```
        authorization: enabled
        ```

    d. Filter: For the Guardium universal connector MongoDB filter to handle events properly, a few conditions must exist:

      -MongoDB access control must be set. \(Messages without users are removed.\)

      -`authCheck` and `authenticate events` are not filtered out from the MongoDB audit log messages. Verify the filter section contains at least the following commands:

  ```
            '{ atype: { $in: ["authCheck", "authenticate"] }
            }'
  ```

      In order to narrow down the events, you can tweak the filter. For example, to audit only delete actions made in MongoDB, add the following suffix to the filter section:

            ```
            '{ atype: { $in: ["authCheck", "authenticate"] } ,
            "param.command": { $in: ["
            delete"] } }'
            ```

      Auditing all commands can lead to excessive records. To prevent performance issues, make sure you have `authCheck` and `authenticate` log types, and additionally the commands you want to see. The filter parameters are aa allowed list. They define what you see in the logs, not what is filtered from the logs. For more information about the MongoDB filter, see [https://docs.mongodb.com/manual/tutorial/configure-audit-filters/](https://docs.mongodb.com/manual/tutorial/configure-audit-filters/).

    **Note:** The spaces in the configuration file are important, and must be located in the file as presented here.

    After configuration, the file has these lines:


        ```
        ...
        auditLog:
          destination: syslog
          ...
          filter:  '{ atype: { $in: ["authCheck", "authenticate"] } '
        setParameter: {auditAuthorizationSuccess: true}
        ...
        security:
          authorization: enabled
        ```

  **Important:** **The MongoDB needs to be restarted for the configuration changes to take effect.**

2. The Guardium Universal Connector listens to port 5141 when using UDP, and 5000 when using TCP. Verify that the relevant port is open.

3.  Configure the Syslog data shipper to forward the audit logs into Guardium Universal Connector. In the Modules section of the Syslog configuration file rsyslog.conf \(usually located in /etc/rsyslog.conf\), enter a rule that defines the target destination for the Syslog logs. The rule looks like:

    ```
    :programname, isequal, "mongod" @<Universal-Connector-IP>:<port>
    ```

    Use `@@` for logs sent by TCP, and `@` for logs sent by UDP.

    **Note:** Syslog does not support load balancing. Do not define multiple hosts, since that would duplicate the events.

4.  Restart Syslog by entering the command:

    ```
    sudo service rsyslog restart
    ```


# Configuring audit logs on MongoDB and forwarding to Guardium via Filebeat

First, configure the MongoDB native audit logs so that they can be parsed by Guardium. Then, configure Filebeat to forward the audit logs to the Guardium universal connector. This implementation supports Linux and Windows database servers.

## Before you begin

-   Use Filebeat whenever possible. It is the natural solution for integration with Logstash. It supports load balancing, and it has fewer limitations than Syslog for integration with the Guardium universal connector.
-   Filebeat must be installed on your database server. For more information on installation, see [https://www.elastic.co/guide/en/beats/filebeat/current/setup-repositories.html\#\_yum](https://www.elastic.co/guide/en/beats/filebeat/current/setup-repositories.html#_yum). The recommended Filebeat version is 7.5.0 and higher.
-   Native audit configuration is performed by the database admin.
-   Filebeat cannot handle messages over approximately 1 GB. Make sure the MongoDB does not save files larger than this limit \(by using `logRotate`\). File messages that exceed the limit are dropped.
-   You can configure multiple collectors simultaneously by using GIM \([Configuring the GIM client to handle Filebeat and Syslog on MongoDB](https://www.ibm.com/docs/en/guardium/11.4?topic=connector-configuring-gim-handle-filebeat-syslog-mongodb)\). If you configure collectors manually, you need to configure them individually.
-   For more information about MongoDB native audit, see [https://docs.mongodb.com/manual/core/auditing/](https://docs.mongodb.com/manual/core/auditing/).

## Procedure

1.  Configure the MongoDB audit logs in the file mongod.conf on a Linux server, or mongod.cfg on a Windows server.

 a.  Configure the AuditLog section in the mongod.conf file.

      -   `destination`: file
      -   `format`: JSON
      -   `path`: /var/log/mongodb/<filename\>.json, for example /var/log/mongodb/auditLog.json


   b. Add the following field to audit the `auditAuthorizationSuccess` messages:

        ```
        setParameter: {auditAuthorizationSuccess: **true**}
         ```

  c. Add or uncomment the security section and edit the following parameter:

        ```authorization: **enabled**```

  d.  `filter`: For the Guardium universal connector MongoDB filter to handle events properly, a few conditions must exist:  
  -   MongoDB access control must be set. \(Messages without users are removed.\)

  - `authCheck` and `authenticate events` are not filtered out from the MongoDB audit log messages. Verify that the filter section contains at least the following commands:


            ```
            '{ atype: { $in: ["authCheck", "authenticate"] }'
            ```

  To narrow down the events, you can tweak the filter.

  - To audit only the delete actions made in MongoDB, for example, add the following suffix to the filter section:

            ```
            '{ atype: { $in: ["authCheck", "authenticate"] } '
            "param.command": { $in: ["
            delete"] } }'
            ```

  - Auditing all commands can lead to excessive records. To prevent performance issues, make sure you have `authCheck` and `authenticate` log types, and any other commands you want to see. The filter parameters are an allowed list. They define what you see in the logs, not what is filtered from the logs. For more information about the MongoDB filter, see [https://docs.mongodb.com/manual/tutorial/configure-audit-filters/](https://docs.mongodb.com/manual/tutorial/configure-audit-filters/) and [Configuring Filebeat](https://www.ibm.com/docs/en/guardium/11.4?topic=source-send-get-data-from-data).

      **Note:** The spaces in the configuration file are important, and must be located in the file as presented here.

      After configuration, the file has these lines:

        ```
        ...
        auditLog:
          destination: file
          format: JSON
          path: /var/lib/mongo/auditLog.json
          filter: '{ atype: { $in: ["authCheck", "authenticate"] } , "param.command": { $in: ["delete"] } }'
        setParameter: {auditAuthorizationSuccess: true}
        ...
        security:
          authorization: enabled
        ```

    **Important:** The MongoDB needs to be restarted for the configuration changes to take effect.

2.  Configure the Filebeat data shipper to forward the audit logs to the Guardium universal connector. In the file filebeat.yml, usually located in /etc/filebeat/filebeat.yml, modify the Filebeat inputs section.

    a.  Select a template from the Universal Connector page and enter your desired port in the port line, beginning at port 5001. \(Use a new port for each new future connection.\) Save the configuration.

    b.  Change the `enabled` field to `true`, and add the path of the audit logs. For example:

        ```
        filebeat.inputs
        - type: filestream
        - id: <ID>
          enabled: **true**
          paths:
            - **/var/log/mongodb/auditLog.json**
            #- c:\programdata\elasticsearch\logs\*
            tags: ["mongodb"]
        ```

    c.  If you send multiple, different data sources from the same server on the same port:

- Attach a different tag to each input log. Then, use the tags when you configure the connector
- Use the tags when you configure the connector \([MongoDB auditing by using Filebeat connector template](https://www.ibm.com/docs/en/guardium/11.4?topic=guardium-mongodb-auditing-by-using-filebeat-connector-template)\)

            ```
            # ============================== Filebeat inputs ===============================
            filebeat.inputs:
            # Each -is an input. Most options can be set at the input level, so
            # you can use different inputs for various configurations.
            # Below are the input specific configurations.
            -type: filestream  
            -id: <ID>
            # Change to true to enable this input configuration.
              enabled: true  
              # Paths that should be crawled and fetched. Glob based paths.
              paths:-/var/lib/mongo/auditLog.json
              tags: ["mongodb"]
            ```

    d.  In the Outputs section:

- Make sure that Elasticsearch output is commented out.
  - Add or uncomment the Logstash output and edit the following parameters:
  - For Guardium Data Protection, add all the Guardium Universal Connector IPs and ports:

                ```
                hosts: **hosts: \[“<ipaddress1\>:<port\>”,”<ipaddress2\>:<port\>,”<ipaddress3\>:<port\>”...\]**
                ```
  -  For Guardium Insights, add all universal connector hosts and ports.  Copy the hostname and port (443) from the Configuration Notes to configure the host in the filebeat.yml file on your datasource.
  - For Guardium Data Protection, use the same port you selected when configuring the Universal Connector. For Guardium Insights, the configured port should be 443. Guardium Insights will map this to an internal port and then copy the path to the certificate (see step 4 and 5 in the Procedure on [this page](universal-connectors/docs/UC_Configuration_GI.md)).
  - Enable load balancing:

                ```
                loadbalance: **true**
                ```
   - For more information on Elastic's Filebeat load-balancing, see: [https://www.elastic.co/guide/en/beats/filebeat/current/load-balancing.html](https://www.elastic.co/guide/en/beats/filebeat/current/load-balancing.html)

  - More optional parameters are described in the Elastic official documentation: [https://www.elastic.co/guide/en/beats/filebeat/current/logstash-output.html](https://www.elastic.co/guide/en/beats/filebeat/current/logstash-output.html)

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

3.  Restart Filebeat to effect these changes.

-  Linux: Enter the command:

        ```
        sudo service filebeat restart
        ```

- Windows: Restart in the Services window


# Configuring audit logs on MySQL and forwarding to Guardium via Syslog

This configuration uses the audit log plug-in, and Syslog, to transfer logs to Guardium.

## Before you begin

This feature requires the Enterprise version of MySQL.

## Procedure

1.  Log in to the MySQL database.

2.  Install the audit log plug-in and verify the following two lines in the my.cnf file:

    ```
    plugin-load = audit_log.so
    audit_log_format=JSON
    ```

    The log file is: /home/<os\_user\>/mysql/data/audit.log

3.  Restart the mysql daemon.

4.  Run the following two SQLs to install the default filter to get every log.

    ```
    SELECT audit_log_filter_set_filter('log_all', '{ "filter":   { "log": true }
                    }');
    SELECT audit_log_filter_set_user('%', 'log_all');
    ```

5.  Configure the remote syslog. Add the following lines to /etc/rsyslog.conf:

    ```
    #audit log
    $ModLoad imfile
    $InputFileName /home/mysql8_ent/mysql/data/audit.log
    $InputFileTag mysql_audit_log:
    $InputFileStateFile audit_log
    $InputFileSeverity info
    $InputFileFacility local6
    $InputRunFileMonitor
    ```

    where:

-   The rsyslog server IP must follow `local6`, for example: `local6.* @@<Guardium IP>:10514`. Do not use port 5141 since this conflicts with the MongoDB default configuration.
-   Use `@<Guardium IP>:port_num` for UDP. Use `@@<Guardium IP>:port_num` for TCP. For example:`local6.* @@<gmachine_ip>:5000 ### To send to logstash using tcp local6.* @<gmachine_ip>:5142 ### To send to logstash using UDP`

6. Restart Syslog by entering the command:
`sudo service rsyslog restart`




# Configuring audit logs on MySQL and forwarding to Guardium via Filebeat

This configuration uses the audit log plug-in, and Filebeat to transfer logs to Guardium.

## Before you begin

This feature requires the Enterprise version of MySQL.

## Procedure

1.  Log in to the MySQL database.

2.  Install the audit log plug-in and verify the following two lines in the my.cnf file:

    ```
    plugin-load = audit_log.so
    audit_log_format=JSON
    ```

    The log file is: /home/<os\_user\>/mysql/data/audit.log

3.  Restart the mysql daemon.

4.  Run the following two SQLs to install the default filter to get every log.

    ```
    SELECT audit_log_filter_set_filter('log_all', '{ "filter":   { "log": true }
                    }');
    SELECT audit_log_filter_set_user('%', 'log_all');
    ```

5.  Configure filebeat.yml

    ```
    filebeat.inputs:

      paths:
        - /home/mysql8_ent/mysql/data/audit.log  # Path to mysql audit log
        #- c:\programdata\elasticsearch\logs\*
      tags: ["mysql"]

    output.logstash:
      # The Logstash hosts
      hosts: ["<Guardium IP>:5045"] # just to ip/host name of the gmachine
    ```

    For IPv6, you can use either of these formats:

    -   `hosts: ["[2620:1f7:807:a080:946:9600:0:d]:5045"]` \(the IPV6 address of this collector\)
    -   `hosts: ["<IPV6 collector hostname>:5045"]`


# Using Filebeat as a non-root user

## Before you begin

Change /etc/filebeat/filebeat.yml to log all Filebeat logs to a file specified in the config:

```

# ================================== Logging ===================================# Sets log level. The default log level is info.
# Available log levels are: error, warning, info, debug
logging.level: info
logging.to_files: true
logging.files:
  path: /var/log/filebeat
  name: filebeat.log
  keepfiles: 7
  permissions: 0640# At debug level, you can selectively enable logging only for some components.
# To enable all selectors use ["*"]. Examples of other selectors are "beat",
# "publisher", "service".
logging.selectors: ["*"] 

 ```
 
 ## Procedure

1. Change the /usr/lib/systemd/system/filebeat.service file to add a non-root user and group to the file:

```
[Service]
User=non-root-user
Group=non-root-user-group
Environment="GODEBUG='madvdontneed=1'"
....
```

2. Change the following file's owner to a non-root-user:non-root-user-group

```
/etc/filebeat/filebeat.yml
/var/lib/filebeat (all files and directories under filebeat, include /var/lib/filebeat directory)
/var/log/filebeat (all files and directories under filebeat, include /var/log/filebeat directory) 
```

3. Restart the Filebeat service

```
root>systemctl restart filebeat
```

Note that that restarting the Filebeat service requires a root user or sudo

4. Make sure that Filebeat is starting ok

```
root>systemctl status filebeat
```

5. To capture the traffic, you also need to provide the executable permission to the audit.log file (Ex: MariaDB: server_audit.log)

If earlier permissions looked like this:

```
-rw-rw---- 1 mariadb10 mariadb    942156 Feb  3 02:08 server_audit.log
```

Then new permissions should look like this: 

```
[root@sys-uctest11 data]# chmod 777 server_audit.log 
[root@sys-uctest11 data]# ls -lrt server_audit.log 
-rwxrwxrwx 1 mariadb10 mariadb 950180 Feb  3 02:18 server_audit.log 
[root@sys-uctest11 data]#
```
 
