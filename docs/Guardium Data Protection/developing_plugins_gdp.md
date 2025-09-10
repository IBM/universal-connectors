# Developing new plug-ins for Guardium Data Protection

To support a new data source, a few steps are required. Document each stage you do, even creating scripts, that can be sent to clients for easier configuration and installation.

1. Configure your DB server.

Enable the audit log. Enable the native DB audit log or use alternative ways of auditing the data source.
Send the log to Guardium collector (or pull it): Configure sending the audit messages/events to the Guardium collector by using Syslog or FileBeat, or another method.

2. Create a data parser plug-in for the Guardium universal connector (a Logstash filter plug-in, de facto) that parses the audit log data and prepare it for Guardium. Plug-in development and testing can be done on your dev server, without requiring a Guardium system.

3. Enable Guardium Universal connector on your Guardium system, install your plug-in on the Guardium Universal connector, and configure a Connector to test that data is flowing from your data source into Guardium.

## Procedure

1. **Configuring the native audit on the data source**.

Configure your database server to enable native auditing for your data source, and to send the messages to Guardium. For example, via Syslog, or by installing and configuring Elastic Filebeat.

a.  **Configuring the audit logs on the data source**

The first thing to do is to enable the log of your data source. See the examples for MongoDB (Configuring audit logs on MongoDB and forwarding to Guardium via Filebeat), and Amazon S3 (Configuring Amazon S3 auditing with CloudWatch), but refer mainly to the documentation of your data source.

Activating audit logs might be different for every data source type. Give yourself some time to research the data source and activate the correct log. Then, familiarize yourself with the log configuration options, running commands, and the resulting messages and events that you see in the audit log.

Points to consider:

* Which log messages are triggered by your actions, and which are triggered sporadically or internally by the data source? Consider filtering out things that do not seem to be important.
* Consider the impact of activating the audit log, and consider filtering its events. Or show clients how to filter events to their needs, like logging only certain users or commands.

* If your log is minimal, either look for another log or try to use an enterprise-grade product which might contain more detailed logs.

* If you see an empty or localhost client IP or host, try performing actions on your data source from a remote location, instead of working directly on the DB server. For example, write a short python script from your local dev to connect and perform a few commands.
Other fields, like server IP or host, can be populated into the Logstash Event that reaches your Logstash filter plug-in by configuring FileBeat, Syslog, or the Logstash configuration file itself. (You can apply the same configuration in the Guardium UI, later). Refer to MongoDB auditing by using Filebeat connector template to see how a sample configured server host and IP.

b. **Send or get the data from the data source**

If you want your data source server to send the log events to your Elastic Logstash (used for testing) or Guardium universal connector, configure Elastic Filebeat or Syslog to forward the events. If your data source solely exists on the cloud, you might need to find other solutions to send the audit log events or files to your Logstash or Guardium universal connector. For example, CloudWatch and CloudTrail services on Amazon AWS are another solution.

Temporarily, stop configuring the data source, and start developing something based on a few log examples you can copy from the log. That's your main focus after all. Return to this step when you're ready to install your parser on Logstash or the Guardium universal connector, and test it.

Not all data sources allow the data to be sent to another server easily, and sometimes you need to develop a Logstash filter plug-in that connects and fetches the log in chunks. For example, with Amazon S3 you can use Amazon CloudWatch, to allow fetching of the log by the Guardium Universal Connector. Other data sources (like Snowflake and Teradata) might require you to fetch activity data by using a Logstash JDBC input plugin.

i. **Configuring Syslog**

The easiest way to get started is to use Syslog, since it comes preinstalled in Linux distributions. Consider switching to Filebeat later on though, as Syslog might truncate long log messages. In addition, Filebeat has other advantages like load balancing and better performance.

Not all DBs allow sending their logs to Syslog, like MysQL, so you might need to configure Syslog to read the log from a file, by using ```$InputFileName /....```.

 The Syslog configuration file is usually located in``` /etc/rsyslog.conf.```.

To configure Syslog to send the audit logs to the Guardium Universal Connector:

 1. Under the Modules section, enter a rule that notifies Syslog where to send the logs to. For example, in MongoDB a rule looks like:

   ```programname, isequal, "mongod" @<Guardium-Collector-IP>:<port>```

 Use the prefix ```@<IP:port>``` to send the logs via UDP and``` @@<IP:port>``` to send the logs via TCP. You need to verify that the same port is set in Guardium, later, when you configure the Guardium universal connector.
 2. If you want to temporarily skip sending data to a remote testing Logstash or Guardium server, configure Syslog to write the messages to a local log file:

  ```:programname, isequal, "mongod" /var/log/syslog-messages-mongodb.log```

 Then you can see what a typical Syslog “message” looks like when it reaches Logstash or Guardium Universal Connector.

 3. Restart Syslog to apply your changes: ```service rsyslog restart```

ii. **Configuring Filebeat**

Filebeat is the preferred option when you want to be production-ready. You might need to install Elastic Filebeat on the DB server. The Filebeat configuration file is usually located in ```/etc/filebeat/filebeat.yml.```

To configure Filebeat to send the audit logs to the Guardium universal connector:

-Configure the ```filebeat.input``` section in the ```filebeat.yaml.``` Set ```enabled: true``` and make sure that there is at least one path in the paths list. Unlike Syslog, here you need to make sure that the input is correlated with data source auditLog output.

-If you intend to use Filebeat to send different data sources from the same server on (same port), attach a different tag to each input log. The tags help later on to identify and filter or parse only the correct events that match that data source, when you configure a connection in Guardium Universal Connector (see Adding connectors and plug-ins in Guardium.) For example,
```
# ============================== Filebeat inputs ===============================
filebeat.inputs:
# Each -is an input. Most options can be set at the input level, so
# you can use different inputs for various configurations.
# Below are the input specific configurations.
-type: filestream  
- id: <ID>
# Change to true to enable this input configuration.
enabled: true  
# Paths that should be crawled and fetched. Glob based paths.paths:
-/var/lib/mongo/auditLog.json
tags: ["mongodb"]
```

-Configure the ```output.logstash``` section. Make sure that this part is not commented out, and enter your testing Logstash or Guardium universal connector as a host.For example,
```
output.logstash:
# The Logstash hosts
hosts: ["<Universal-Connector-IP>:<port>"]
```
Then. restart Filebeat to apply your changes:

```service filebeat restart```

Points to consider:

* Not all incoming events are necessarily relevant. You need to find a way to filter them out at the data source server, or if not possible, then in your Filter plug-in. If you use Syslog to send data to your Logstash or Guardium Universal connector, other events might come in from other services that use the Syslog service, for example, kernel, telnet.

* If possible, think about performance, failover, and persistency of data. For example, Filebeat supports load balancing and persistency. But if you can’t use them, how would you take care to manage them? What if you pull data into Guardium and the collector is overloaded? Or down? Do you need to audit all the events of your data source, or can you filter the activity log to contain fewer events?

Recommendation:

* If possible, prepare scripts for your configuration changes related to native auditing, Syslog, or Filebeat. These scripts can be used later by others to use your plug-in much faster.

2. **Developing a filter plug-in**

The Guardium universal connector internal engine is Logstash. The activity log you develop goes through an input stage, where you configure how the data gets into the event pipeline. The next stage is the filter stage, where your plug-in gets the data, and prepares it. The final stage is the output stage, where the Guardium Universal connector takes the data you prepared and pushes it into Guardium.

For an introduction to developing a Logstash filter plug-in, see the Elastic Logstash tutorial on [How to write a Java filter plugin](https://www.elastic.co/guide/en/logstash/7.16/java-filter-plugin.html). It describes how to download Logstash source code and compile logstash-coreclasses. (You need to reference this classes folder from your plug-in project.) It also describes how to install a plug-in onto a local Logstash installation, and how to run Logstash minimally.

The new plug-in can be implemented in any language Logstash supports for plug-ins, like Ruby and Java. The Guardium universal connectors were developed in Java, since it is more flexible and better supports more complex projects.

To develop a plug-in for Guardium universal connector, Develop a Logstash Filter plug-in that parses the events you get from Logstash input stage, and adds a Guardium record field to them. Guardium record is a data structure that the Guardium universal connector can digest. For example, an event that is processed by your filter plug-in can look like this:
```
"GuardRecord" =>
"{"sessionId":"mV20eHvvRha2ELTeqJxQJg\u003d\u003d","dbName":"admin","appUserName":"","time":{"timstamp":1591883051070,"minOffsetFromGMT":-240,"minDst":0},"sessionLocator":{"clientIp":"9.144.222.99","clientPort":60185,"serverIp":"9.77.144.59","serverPort":27017,"isIpv6":false,"clientIpv6":"","serverIpv6":""},"accessor":{"dbUser":"realAdmin ","serverType":"MongoDB","serverOs":"","clientOs":"","clientHostName":"","serverHostName":"","commProtocol":"","dbProtocol":"MongoDB native audit","dbProtocolVersion":"","osUser":"","sourceProgram":"","client_mac":"","serverDescription":"","serviceName":"admin","language":"FREE_TEXT","dataType":"CONSTRUCT"},"data":{"construct":{"sentences":[{"verb":"find","objects":[{"name":"USERS","type":"collection","fields":[],"schema":""}],"descendants":[],"fields":[]}],"fullSql":"{\"atype\":\"authCheck\",\"ts\":{\"$date\":\"2020-06-11T09:44:11.070-0400\"},\"local\":{\"ip\":\"9.70.147.59\",\"port\":27017},\"remote\":{\"ip\":\"9.148.202.94\",\"port\":60185},\"users\":[{\"user\":\"realAdmin\",\"db\":\"admin\"}],\"roles\":[{\"role\":\"readWriteAnyDatabase\",\"db\":\"admin\"},{\"role\":\"userAdminAnyDatabase\",\"db\":\"admin\"}],\"param\":{\"command\":\"find\",\"ns\":\"admin.USERS\",\"args\":{\"find\":\"USERS\",\"filter\":{},\"lsid\":{\"id\":{\"$binary\":\"mV20eHvvRha2ELTeqJxQJg\u003d\u003d\",\"$type\":\"04\"}},\"$db\":\"admin\",\"$readPreference\":{\"mode\":\"primaryPreferred\"}}},\"result\":0}","redactedSensitiveDataSql":"{\"atype\":\"authCheck\",\"ts\":{\"$date\":\"2020-06-11T09:44:11.070-0400\"},\"local\":{\"ip\":\"9.70.147.59\",\"port\":27017},\"remote\":{\"ip\":\"9.148.202.94\",\"port\":60185},\"users\":[{\"user\":\"realAdmin\",\"db\":\"admin\"}],\"roles\":[{\"role\":\"readWriteAnyDatabase\",\"db\":\"admin\"},{\"role\":\"userAdminAnyDatabase\",\"db\":\"admin\"}],\"param\":{\"command\":\"find\",\"ns\":\"admin.USERS\",\"args\":{\"filter\":{},\"lsid\":{\"id\":{\"$binary\":\"?\",\"$type\":\"?\"}},\"$readPreference\":{\"mode\":\"?\"},\"find\":\"USERS\",\"$db\":\"admin\"}},\"result\":0}"},"originalSqlCommand":""},"exception":null}",
    "@version" => "1",
    "type" => "syslog",
    "timestamp" => "2020-01-26T10:47:41.225-0500"
}
```
The Guardium record, which is added to Logstash Event after the filter, is examined and handled by Guardium Universal connector (in an output stage) and inserted into Guardium.

Clone the [Guardium Code](https://github.com/IBM/universal-connectors/tree/main) from GitHub and build the [Guardium Universal-Connector Commons](https://github.com/IBM/universal-connectors/tree/main/common) code to get helper classes for creating a Guardium record.

For a production-ready example, see the [MongoDB-Guardium filter plug-in project on GitHub](https://github.com/IBM/universal-connectors/tree/main/filter-plugin/logstash-filter-mongodb-guardium). It transforms MongoDB activity audit log messages into a record that contains details about who performed the action, from where, and information about the command that was run. You can use it to learn how events are parsed and tested, and more. The ```README.md``` contains a brief introduction as to how the filter works, what it sends to Guardium in the end (a Record POJO), how to build and install it as a Logstash filter plug-in.

If you need to create and store files from your plug-in’s code, use the environment variable ```$THIRD_PARTY_PATH``` from within your plug-in, to create a persistent storage location for you to work with. For example, to manage a queue and track the latest fetched log entry. For other usages of this folder, see [Installing and testing the filter or input plug-in on a staging Guardium system].

***Tips:***

* To get started, start coding the filter plug-in by using just a handful of events you see in the data source log. Test it using unit tests, and only later try to install your plug-in on a separate Logstash instance before you test it on Guardium server.

* Events sent to a Logstash can differ a bit from what you see in the logs. The best way to see an incoming Logstash event message is to direct traffic from your data source to a Logstash service (either local or remote).

* To improve your filter plug-in performance, test that the messages match your filter as fast soon as possible. For example, the MongoDB parser/filter uses mongod:" and mongos: (based on the Syslog template) to recognize MongoDB audit log events, instead of parsing the whole JSON structure.

* Use a specific package name for your plug-in, since Logstash loads classes by using the same class loader at the moment. This prevents clashes with other plug-ins that might have the same Class name as yours.

* Harness log4j2 logger to log events. Later, when you install your filter plug-in on a Guardium server, you can examine the logs locally (error level and higher, though) by running a MustGather command.

***Notes:***

* Do not try to inject multiple words into SentenceObjects. The Guardium investigation dashboard does not display it clearly. Instead, consider multi-word-objects.

* Do not remove or drop events from the batch of Events that passes through your filter section, unless:

 * They are related to your data source.
 * You are positive that no other connector configuration expects the events.

See Notes on connections in the [Installing and testing the filter or input plug-in on a staging Guardium system].

* Remember to parse errors and authentication errors, and send them to Guardium as exceptions, by using ```Record.setException()```. For successful commands, use ```Record.setData()```.

3. **Testing the filter in a dev environment**

While you develop your filter plug-in, you need to test it. Test it locally on your dev environment first, using unit tests. Then, test it on a local Logstash, by installing the plug-in and running Logstash with simulated events. To test on Logstash, you need to install Elastic Logstash (executable) v7.5.x and JRuby. v9.2.11.0 is verified, though other versions should work.


To run Logstash with your plug-in, create a simple Logstash configuration file that generates or simulates an audit message of your data source, then passes it to your filter.

Here's a test Logstash configuration that simulates an incoming Syslog event of a MongoDB server, and passes it to a filter:
```
input {
      generator {
      type => "syslogMongoDB"
      lines => [
          "<14>Feb 18 08:53:31 qa-db51 mongod: { 'atype': 'authCheck', 'ts': { '$date': '2020-01-26T08:25:10.527-0500' }, 'local': { 'ip': '127.0.0.1', 'port': 27017 }, 'remote': { 'ip': '127.0.0.1', 'port': 56470 }, 'users': [ { 'user' : 'realAdmin', 'db' : 'admin' } ], 'roles' : [ { 'role' : 'readWriteAnyDatabase', 'db' : 'admin' }, { 'role' : 'userAdminAnyDatabase', 'db' : 'admin' } ], 'param': { 'command': 'delete', 'ns': 'test.posts', 'args': { 'delete': 'posts', 'ordered': true, 'lsid': { 'id': { '$binary': '1P3A98W7QbqeDMqMdP2trA==', '$type': '04' } }, '$db': 'test', 'deletes': [ { 'q': { 'owner_id': '12345' }, 'limit': 1 } ] } }, 'result': 0 }"
      ]
      count => 1
  }
}

filter {

  if [type] == "syslogMongoDB" {
        grok { match => { "message" => "%{SYSLOGTIMESTAMP:syslog_timestamp} %{SYSLOGHOST:server_hostname} %{SYSLOGPROG:source_program}(?:\[%{POSINT:syslog_pid}\])?: %{GREEDYDATA:syslog_message}" }
        }
        date {
          match => [ "timestamp", "MMM  d HH:mm:ss", "MMM dd HH:mm:ss" ]
        }
        logstash-filter-mongodb-guardium {}
    }

    mutate { remove_field => [
        "syslog_timestamp",
        "source_program", "program", "syslog_pid",
        "syslog_message",
        "server_hostname", "host"
        ]
    }
}

output {
        stdout { codec => rubydebug }
}
```
See the [MongoDB-Guardium filter plugin project](https://github.com/IBM/universal-connectors/tree/main/filter-plugin/logstash-filter-mongodb-guardium) on GitHub for more examples and instructions.

Run your test configuration by entering:
```logstash -f ./test.conf```.

The product of your plug-in prints to the standard output of your console, as configured in the output section. After your unit tests pass, and the Logstash filter installs and works fine, continue with the next step, installing your plug-in on a staging Guardium server.

4.**Installing and testing the filter or input plug-in on a staging Guardium system**

After you test your filter locally, follow these instructions to test it on a staging Guardium system.

1. Prepare your plug-ins as a Logstash offline plug-in pack.

Since your Guardium system might not have access to the internet, your plug-in must be created with offline installation in mind. To ensure success, follow the [Logstash documentation](https://www.elastic.co/guide/en/logstash/7.5/offline-plugins.html) . The output is a .zip file with your plug-ins.

  2. Install your offline plug-in pack (or other file type that is referenced by the plug-in) on Guardium.


  * On the collector, go to ```Setup > Tools and Views > Configure Universal Connector```.

* Click ```Upload and Install plug-ins```, and upload the offline plug-ins pack. Use only the packages that are supplied by IBM. Do not use extra spaces in the title.

* Repeat for any other file type used by your plug-ins.

Plug-in verification and installation might take a few minutes to complete.
To view the plug-ins that are currently installed, run the API:

```grdapi show_universal_connector_plugins.```

If your plug-in did not install successfully, you can get more details by running the command

```support must_gather universal_connector_issues```

and examining the log

```uc_container_log/uc_handler.log.```

If you upload file types other than compressed offline-plugin-packs, like ```JAR``` dependencies that are used in a Logstash jdbc input plug-in, you can refer to them by using the environment variable ```THIRD_PARTY_PATH``` as the uploads folder. For example, after you upload a dependency ```JAR``` file, you can later refer to it from within a Connector’s input configuration in a similar way to this example:
```
jdbc {
   jdbc_driver_library => "${THIRD_PARTY_PATH}/mysql-connector-java-5.1.49-bin.jar"
   jdbc_driver_class => "com.mysql.jdbc.Driver"
   ...
}
```
You can also create and store files in the folder ```$THIRD_PARTY_PATH```, if your plug-in’s code requires it. For example, to support persistency by keeping track of the last fetched log entry.

  3. Configure the Guardium universal connector to be aware of your data source and use your plug-in, by adding a connection. In the Configure Universal Connector page create a Connector configuration. Paste the input and filter sections contents from your Logstash configuration file into the Input and Filter textboxes.

***CAUTION:***

***All user-defined connections are part of a single Logstash pipeline. Any input data that you define in a connection configuration flows to all other connectors’ filter configurations. Be careful to filter or parse only those events that are within your filter plug-in's responsibility.***

* ***In general, set a unique identifier (type or tag) for each data source type and make sure your filter configuration addresses this type only. For example, use ```type``` to filter only events that come from a MongoDB:***
```
if [type]== syslogMongoDB{...
```

* ***Be extra careful before you remove events or event fields since they might belong to other data source types. If you decide to remove them, first verify the event identifier (type or tag).***

* ***If you send multiple, different data sources that use Filebeat from the same server:***

  * ***Configure Filebeat to use a different tag for each data source***

  * ***Use the tag as the identifier in the Filter configuration:***
 ```if [type] == "filebeat" and "mongodb" in [tags] {  ```

4. Set the policies you require. See the [Policies](/docs/Guardium%20Data%20Protection/uc_policies_gdp.md) topic for Guardium Data Protection. 

5. Enable the Guardium universal connector.

When the Guardium universal connector runs, it starts a docker container with Logstash in the background. Start the Guardium Universal connector by using a API, adding the debug flag to collect more data in the log folder. (By default, only errors are written to the log.) The debug level does not affect the plug-in logger. It shows more details about the data that is passed to the Guardium universal connector (output stage), just before it is pushed into Guardium.

* Run: ```grdapi run_universal_connector uc_debug_level=debug debug=3```

The ```debug=3 ```flag activates the debug log, which you can also view after you run a MustGather command.

6. To get further insight about how your plug-in handles the input data, examine the debug logs by running

```support must_gather universal_connector_issues```

and download the MustGather file ```(.tgz)``` from the UI: ```Maintenance > Support Information Results```

More commands that you might need:

* To change the debug level while the Guardium universal connector is running, run

```grdapi set_universal_connector_log_level uc_debug_level=debug```

* To stop the Guardium Universal connector, use

```grdapi stop_universal_connector```

* To check status, use

```grdapi get_universal_connector_status```

Check the connector status and data flow. See Monitoring connector and data flow status.
If you developed a plug-in that others can benefit from, consider contributing it to the open source community.


5. **Publishing your plug-in**

When your plug-in is fully tested and ready for deployment, make sure you have these files ready, so your client can add your plug-in to Guardium.

-Input or Filter plug-in, packed as a Logstash offline plug-in pack (compressed file).

-Example ```readme``` file and scripts that are related to configuring native audit on the data source server.

-Example Logstash configuration that works with your input or filter.

Optionally, prepare three ```.CSV``` files with lists of commands that are relevant to your data source. These CSV files can be then imported into Guardium existing Groups (lists), and used by clients as part of a Guardium policy, which uses them to trigger alerts and audit processes.

```Administrative-commands.csv``` (like ```createCollection```)

```DML-commands.csv```

```DDL-commands.csv```

If you developed a plug-in that others can benefit from, consider contributing it to the open source community.
