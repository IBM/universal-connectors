# MySql-Guardium Logstash filter plug-in

This is a [Logstash](https://github.com/elastic/logstash) filter plug-in for the universal connector that is featured in IBM Security Guardium. It parses events and messages from the MySQL audit into a Guardium record instance (which is a standard structure made out of several parts). The information is then sent over to Guardium. Guardium records include the accessor (the person who tried to access the data), the session, data, and exceptions. If there are no errors, the data contains details about the query "construct". The contstruct details the main action (verb) and collections (objects) involved.

The plug-in is free and open-source (Apache 2.0). It can be used as a starting point to develop addition filter plug-ins for Guardium universal connector.

## Documentation
### Supported audit messages class: 
* connection: 
    * connect event
* general:
    * command
        Init DB, query

Notes: 
* For the events to be handled propertly: 
    * connection and general class events should not be filtered out of the MySQL audit log messages.
* Other MySQL events and messages in the pipeline are ignored.
* Non-MySQL events are skipped, but not removed from pipeline (since they may be used by other filters in the connector configuration pipeline).

### Supported errors:  

* LOGIN_FAILED
* SQL_ERROR

*IPv6* addresses are typically supported by the MySQL and filter plug-ins, however this is not fully supported by the Guardium pipeline.

## Filter notes
* The filter supports events sent through Syslog or Filebeat. It relies on the "mysql_audit_log:" prefix in the event message for the JSON portion of the audit to be parsed.
* Field _server_hostname_ (required) - Server hostname is expected (extracted from the second field of the syslog message).
* Field _server_ip_ - States the IP address of the MySQL server, if it is available to the filter plug-in. The filter will use this IP address instead of localhost IP addresses that are reported by MySQL, if actions were performed directly on the database server.
* The client "Source program" is not available in messages sent by MySQL. This is because this data is sent only in the first audit log message upon database connection - and the filter plug-in doesn't aggregate data from different messages.
* If events with "(NONE)" local/remote IP addresses are not filtered, the filter plug-in will convert the IP to "0.0.0.0", as a valid format for IP is needed. However, this is atypical, since as messages without users are filtered out.
* Events in the filter are not removed, but tagged if not parsed (see [Filter result](#filter-result), below).


## Example 
### syslog input

    Aug 26 13:31:27 rh7u4x64t mysql_audit_log: { "timestamp": "2020-08-26 17:31:22", "id": 1, "class": "general", "event": "status", "connection_id": 42, "account": { "user": "guardium_qa", "host": "" }, "login": { "user": "guardium_qa", "os": "", "ip": "1.1.1.1", "proxy": "" }, "general_data": { "command": "Query", "sql_command": "select", "query": "\/* ApplicationName=DBeaver 7.1.4 - SQLEditor <Script.sql> *\/ select * from pet\nLIMIT 0, 200", "status": 0 } },

## Filter result
The filter tweaks the event by adding a _GuardRecord_ field to it with a JSON representation of a Guardium record object. As the filter takes the responsiblity of breaking the database command into its atomic parts, it details the construct object with the parsed command structure: 
{
             "program" => "mysql_audit_log",
         "GuardRecord" => "{\"sessionId\":\"42\",\"dbName\":\"\",\"appUserName\":\"\",\"time\":{\"timstamp\":1598477482000,\"minOffsetFromGMT\":0,\"minDst\":0},\"sessionLocator\":{\"clientIp\":\"1.1.1.1\",\"clientPort\":-1,\"serverIp\":\"0.0.0.0\",\"serverPort\":-1,\"isIpv6\":false,\"clientIpv6\":\"\",\"serverIpv6\":\"\"},\"accessor\":{\"dbUser\":\"guardium_qa\",\"serverType\":\"MySql\",\"serverOs\":\"\",\"clientOs\":\"\",\"clientHostName\":\"\",\"serverHostName\":\"rh7u4x64t\",\"commProtocol\":\"\",\"dbProtocol\":\"MySQL native audit\",\"dbProtocolVersion\":\"\",\"osUser\":\"\",\"sourceProgram\":\"\",\"client_mac\":\"\",\"serverDescription\":\"\",\"serviceName\":\"\",\"language\":\"MYSQL\",\"dataType\":\"TEXT\"},\"data\":{\"construct\":null,\"originalSqlCommand\":\"/* ApplicationName\\u003dDBeaver 7.1.4 - SQLEditor \\u003cScript.sql\\u003e */ select * from pet\\nLIMIT 0, 200\"},\"exception\":null}",
          "@timestamp" => 2020-10-13T12:49:47.842Z,
    "syslog_timestamp" => "Aug 26 13:31:27",
      "source_program" => "mysql_audit_log",
            "@version" => "1",
     "server_hostname" => "rh7u4x64t",
            "sequence" => 0,
                "type" => "syslog"
}

This Guardium record, which is added to Logstash event after the filter, is examined and handled by Guardium universal connector (in an output stage) and inserted into Guardium. 

If the event message is not related to MySQL, the event is tagged with  "_mysqlguardium_ignore" (not removed from the pipeline). If it is an event from MySQL but JSON parsing fails, the event is tagged with "_mysqlguardium_parse_error" but not removed (this may happen if the syslog message is too long and was truncated). These tags can be useful for debugging purposes. 


To build and create an updated GEM of this filter plug-in which can be installed onto Logstash: 
1. Build Logstash from the repository source.
2. Create or edit _gradle.properties_ and add the LOGSTASH_CORE_PATH variable with the path to the logstash-core folder. For example: 
    
    ```LOGSTASH_CORE_PATH=/Users/taldan/logstash76/logstash-core```

3. Run ```$ ./gradlew.unix gem --info``` to create the GEM. 

## Install
To install this plug-in on your local developer machine with Logstash installed, clone or download the GEM file and then issue this command from your Logstash installation (replace "?" with this plug-in version):
    
    $ logstash-plugin install --local ./logstash-filter-mysql_filter_guardium-?.?.?.gem

Note: The logstash-plugin may not handle relative paths well. It is recommended that you install the GEM from a simple path, as in the above example. 

To test filter installation on your development Logstash:
1. Install Logstash (using Brew, for example).
2. Install the filter plug-in (see above).
2. Run this command:

    ```$ logstash -f ./filter-test.conf --config.reload.automatic```


### Not supported
1. Support fields.
2. Embedded documents as inner objects.


## Contribute
You can enhance this filter and open a pull request with suggested changes - or you can use the project to create a different filter plug-in for Guardium that supports other data sources.

## References
See [documentation for Logstash Java plug-ins](https://www.elastic.co/guide/en/logstash/current/contributing-java-plugin.html).

See [Guardium Universal connector commons](https://www.github.com/IBM/guardium-universalconnector-commons) library for more details regarding the standard Guardium Record object.

