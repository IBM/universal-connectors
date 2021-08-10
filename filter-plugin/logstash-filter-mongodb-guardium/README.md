# MongoDB-Guardium Logstash filter plug-in

This is a [Logstash](https://github.com/elastic/logstash) filter plug-in for the universal connector that is featured in IBM Security Guardium. It parses events and messages from MongoDB audit/activity logs into a Guardium record instance (which is a standard structure made out of several parts). The information is then pushed into Guardium. Guardium records include the accessor (the person who tried to access the data), the session, data, and exceptions. If there are no errors, the data contains details about the query "construct". The construct details the main action (verb) and collections (objects) involved.  

The plug-in is free and open-source (Apache 2.0). It can be used as a starting point to develop additional filter plug-ins for Guardium universal connector.

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
* Field _server_hostname_ (required) - Server hostname is expected (extracted from the second field of the syslog message).
* Field _server_ip_ - States the IP address of the MongoDB server, if it is available to the filter plug-in. The filter will use this IP address instead of localhost IP addresses that are reported by MongoDB, if actions were performed directly on the database server. 
* The client "Source program" is not available in messages sent by MongoDB. This is because this data is sent only in the first audit log message upon database connection - and the filter plug-in doesn't aggregate data from different messages.  
* If events with "(NONE)" local/remote IP addresses are not filtered, the filter plug-in will convert the IP to "0.0.0.0", as a valid format for IP is needed. However, this is atypical, since as messages without users are filtered out.
* Events in the filter are not removed, but tagged if not parsed (see [Filter result](#filter-result), below).
* The filter also masks the audit messages of type MongoDB authCheck: Currently, most field values are replaced with "?" in a naïve process, where most command arguments are redacted, apart from the _command_, _$db_, and _$lookup_ & _$graphLookup_ required arguments (_from_, _localField_, _foreignField_, _as_, _connectFromField_, _connectToField_).

## Example 
### syslog input

    2020-01-26T10:47:41.225272-05:00 test-server05 mongod: { "atype" : "authCheck", "ts" : { "$date" : "2020-06-11T09:44:11.070-0400" }, "local" : { "ip" : "9.70.147.59", "port" : 27017 }, "remote" : { "ip" : "9.148.202.94", "port" : 60185 }, "users" : [ { "user" : "realAdmin", "db" : "admin" } ], "roles" : [ { "role" : "readWriteAnyDatabase", "db" : "admin" }, { "role" : "userAdminAnyDatabase", "db" : "admin" } ], "param" : { "command" : "find", "ns" : "admin.USERS", "args" : { "find" : "USERS", "filter" : {}, "lsid" : { "id" : { "$binary" : "mV20eHvvRha2ELTeqJxQJg==", "$type" : "04" } }, "$db" : "admin", "$readPreference" : { "mode" : "primaryPreferred" } } }, "result" : 0 }

## Filter result
The filter tweaks the event by adding a _GuardRecord_ field to it with a JSON representation of a Guardium record object. As the filter takes the responsiblity of breaking the database command into its atomic parts, it details the construct object with the parsed command structure: 
    {

      "sequence" => 0,
        "GuardRecord" => "{"sessionId":"mV20eHvvRha2ELTeqJxQJg\u003d\u003d","dbName":"admin","appUserName":"","time":{"timstamp":1591883051070,"minOffsetFromGMT":-240,"minDst":0},"sessionLocator":{"clientIp":"9.148.202.94","clientPort":60185,"serverIp":"9.70.147.59","serverPort":27017,"isIpv6":false,"clientIpv6":"","serverIpv6":""},"accessor":{"dbUser":"realAdmin ","serverType":"MongoDB","serverOs":"","clientOs":"","clientHostName":"","serverHostName":"","commProtocol":"","dbProtocol":"MongoDB native audit","dbProtocolVersion":"","osUser":"","sourceProgram":"","client_mac":"","serverDescription":"","serviceName":"admin","language":"FREE_TEXT","dataType":"CONSTRUCT"},"data":{"construct":{"sentences":[{"verb":"find","objects":[{"name":"USERS","type":"collection","fields":[],"schema":""}],"descendants":[],"fields":[]}],"fullSql":"{\"atype\":\"authCheck\",\"ts\":{\"$date\":\"2020-06-11T09:44:11.070-0400\"},\"local\":{\"ip\":\"9.70.147.59\",\"port\":27017},\"remote\":{\"ip\":\"9.148.202.94\",\"port\":60185},\"users\":[{\"user\":\"realAdmin\",\"db\":\"admin\"}],\"roles\":[{\"role\":\"readWriteAnyDatabase\",\"db\":\"admin\"},{\"role\":\"userAdminAnyDatabase\",\"db\":\"admin\"}],\"param\":{\"command\":\"find\",\"ns\":\"admin.USERS\",\"args\":{\"find\":\"USERS\",\"filter\":{},\"lsid\":{\"id\":{\"$binary\":\"mV20eHvvRha2ELTeqJxQJg\u003d\u003d\",\"$type\":\"04\"}},\"$db\":\"admin\",\"$readPreference\":{\"mode\":\"primaryPreferred\"}}},\"result\":0}","redactedSensitiveDataSql":"{\"atype\":\"authCheck\",\"ts\":{\"$date\":\"2020-06-11T09:44:11.070-0400\"},\"local\":{\"ip\":\"9.70.147.59\",\"port\":27017},\"remote\":{\"ip\":\"9.148.202.94\",\"port\":60185},\"users\":[{\"user\":\"realAdmin\",\"db\":\"admin\"}],\"roles\":[{\"role\":\"readWriteAnyDatabase\",\"db\":\"admin\"},{\"role\":\"userAdminAnyDatabase\",\"db\":\"admin\"}],\"param\":{\"command\":\"find\",\"ns\":\"admin.USERS\",\"args\":{\"filter\":{},\"lsid\":{\"id\":{\"$binary\":\"?\",\"$type\":\"?\"}},\"$readPreference\":{\"mode\":\"?\"},\"find\":\"USERS\",\"$db\":\"admin\"}},\"result\":0}"},"originalSqlCommand":""},"exception":null}",
        "@version" => "1",
        "@timestamp" => 2020-02-25T12:32:16.314Z,
          "type" => "syslog",
        "timestamp" => "2020-01-26T10:47:41.225-0500"
    }

This Guardium record, which is added to Logstash event after the filter, is examined and handled by Guardium universal connector (in an output stage) and inserted into Guardium. 

If the event message is not related to MongoDB, the event is tagged with  "_mongoguardium_skip_not_mongodb" (not removed from the pipeline). If it is an event from MongoDB but JSON parsing fails, the event is tagged with "_mongoguardium_json_parse_error" but not removed (this may happen if the syslog message is too long and was truncated). These tags can be useful for debugging purposes. 


To build and create an updated GEM of this filter plug-in which can be installed onto Logstash: 
1. Build Logstash from the repository source.
2. Create or edit _gradle.properties_ and add the LOGSTASH_CORE_PATH variable with the path to the logstash-core folder. For example: 
    
    ```LOGSTASH_CORE_PATH=/Users/taldan/logstash76/logstash-core```

3. Run ```$ ./gradlew.unix gem --info``` to create the GEM (ensure you have JRuby installed beforehand, as described [here](https://www.ibm.com/docs/en/guardium/11.3?topic=connector-developing-plug-ins)).

## Install
To install this plug-in on your local developer machine with Logstash installed, issue this command:
    
    $ ~/Downloads/logstash-7.5.2/bin/logstash-plugin install ./logstash-filter-mongodb_guardium_filter-?.?.?.gem

Notes: 
* Replace "?" with this plug-in version.
* The logstash-plugin may not handle relative paths well. It is recommended that you install the GEM from a simple path, as in the above example. 

To test the filter using your local Logstash installation, run this command:

    ```$ logstash -f ./filter-test-generator.conf --config.reload.automatic```

### Not yet supported
1. Support fields.
2. Embedded documents as inner objects.


## Contribute
You can enhance this filter and open a pull request with suggested changes - or you can use the project to create a different filter plug-in for Guardium that supports other data sources.


## References
See [documentation for Logstash Java plug-ins](https://www.elastic.co/guide/en/logstash/current/contributing-java-plugin.html).

See [Guardium Universal connector commons](https://www.github.com/IBM/guardium-universalconnector-commons) library for more details regarding the standard Guardium Record object.

