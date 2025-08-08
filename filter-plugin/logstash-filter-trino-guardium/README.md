# TrinoDB - Guardium Logstash filter plug-in

### Meet TrinoDB

* Tested versions: v1
* Environment: Trino DB
* Supported inputs: http (pull)
* Supported Guardium versions:
    * Guardium Data Protection 11.4 and above

This is a [Logstash](https://github.com/elastic/logstash) filter plug-in for the universal connector that is featured in
IBM Security Guardium. It parses events and messages from the Trino audit log into a Guardium Record.

The plug-in is free and open-source (Apache 2.0). It can be used as a starting point to develop additional filter
plug-ins for Guardium universal connector.

## 1. Configuring ONPREM MSSQL

### Procedure

	1. Create a database instance:

		a.Here, We will consider that we have already installed TrinoDB ONPREM setup. 

See: [TrinoDB Install Offical Documentation](https://trino.io/docs/current/installation/deployment.html)

### Enabling Audit Logs

#### requirements

1. Provide an HTTP/S service that accepts POST events with a JSON body.
2. Configure http-event-listener.connect-ingest-uri in the event listener properties file with the URI of the service.
3. Detail the events to send in the Configuration section.

#### Procedure

1. Log into Trino server
2. Trino application logging is optional and configured in the `log.properties` file in your Trino installation `etc`
   configuration directory as set by
   the [launcher](https://trino.io/docs/current/installation/deployment.html#running-trino).
   See: [logging](https://trino.io/docs/current/admin/logging.html)

   etc/log.properties

   io.trino=INFO
   io.trino.plugin.hive=DEBUG
   io.trino.event.QueryMonitor=DEBUG
   io.trino.execution.SqlTask=DEBUG

2. set `etc/http-event-listener.properties`
   See: [http-event-listener](https://trino.io/docs/current/admin/event-listeners-http.html)

   etc/http-event-listener.properties

   event-listener.name=http
   http-event-listener.connect-ingest-uri=http://guardium_IP:5060
   http-event-listener.log-created=true
   http-event-listener.log-completed=true
   http-event-listener.log-split=false

2. add `etc/http-event-listener.properties` to `event-listener.config-files` on config.properties file
   refer [Config properties](https://trino.io/docs/current/installation/deployment.html#config-properties)

   etc/config.properties

   event-listener.config-files=etc/http-event-listener.properties

## Guardium Data Protection

The Guardium universal connector is the Guardium entry point for native audit/data_access logs. The Guardium universal
connector identifies and parses the received events, and converts them to a standard Guardium format. The output of the
Guardium universal connector is forwarded to the Guardium sniffer on the collector, for policy and auditing
enforcements.

### Before you begin

* Configure the policies you require. See [policies](/docs/#policies) for more information.
* You must have permission for the S-Tap Management role. The admin user includes this role by default
* Download
  the [logstash-filter-trino_guardium_filter](./logstash-filter-trino_guardium_plugin_filter.zip)
  plug-in.
* Verify that the http input plugin is available on the GDP system. 

### Procedure

1. On the collector, go to ```Setup``` > ```Tools and Views``` > ```Configure Universal Connector```.
2. Enable the universal connector if it is disabled.
3. Click ```Upload File``` and select the
   offline  [logstash-filter-trino_guardium_filter](./logstash-filter-trino_guardium_plugin_filter.zip)
   plug-in. After it is uploaded, click ```OK```.
4. Click ```Upload File``` and select the key.json file. After it is uploaded, click ```OK```.
5. Click the Plus sign to open the Connector Configuration dialog box.
6. Type a name in the Connector name field.
7. Update the input section to add the details from the [TrinoSyslog.conf](./TrinoOverSyslogPackage/TrinoSyslog.conf)
   file's input part, omitting the keyword "input{" at the beginning and its corresponding "}" at the end.
8. Update the filter section to add the details from the [TrinoSyslog.conf](./TrinoOverSyslogPackage/TrinoSyslog.conf)
   file's filter part, omitting the keyword "filter{" at the beginning and its corresponding "}" at the end.
9. The 'type' fields should match in the input and filter configuration sections. This field should be unique for every
   individual connector added.
10. Click ```Save```. Guardium validates the new connector and displays it in the Configure Universal Connector page.
11. After the offline plug-in is installed and the configuration is uploaded and saved in the Guardium machine, restart
    the Universal Connector using the ```Disable/Enable``` button.
