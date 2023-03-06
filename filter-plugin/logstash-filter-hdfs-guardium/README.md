# HDFS-Guardium Logstash Filter Plug-in
### Meet HDFS
* Tested versions: Hadoop 3.1.x
* Environment: On-premise, Iaas
* Supported inputs: Filebeat (push)
* Supported versions:
    * Guardium Data Protection: 11.4 and above
    * Guardium Insights: 3.2 and above

This is a [Logstash](https://github.com/elastic/logstash) filter plug-in for the univer***REMOVED***l connector that is featured in IBM Security Guardium. It parses an HDFS audit event into a Guardium record instance, which standardizes the event into several parts before it is sent over to Guardium.


## HDFS Configuration

HDFS needs to be configured to write HDFS audits to a file on the system. In most HDFS installations, this is enabled and configured by default. All NameNodes will write audits to a log on their hosts.

Once the HDFS audit log is enabled and configured properly, Filebeat will need to be installed and configured on the system.

## Filebeat Configuration

Filebeat must be configured to send the output to the chosen Logstash host and port. In addition, events are configured with the add_locale, add_host_metadata, and add_tags processors (to add an "hdfs" tag). You can learn more about Filebeat processors [here](https://www.elastic.co/guide/en/beats/filebeat/current/filtering-and-enhancing-data.html#using-processors).

```
filebeat.inputs:
- type: log
  enabled: true
  paths:
    - /var/log/hadoop-hdfs/hdfs-audit.log*

filebeat.config.modules:
  path: ${path.config}/modules.d/*.yml
  reload.enabled: false

setup.template.settings:
  index.number_of_shards: 1

output.logstash:
  hosts: ["univer***REMOVED***l-connector-host:5046"]

processors:
  - add_host_metadata: ~
  - add_locale: ~
  - add_tags:
      tags: [hdfs]
```

## Univer***REMOVED***l Connector Configuration

### Input Configuration

```
### Change the port to match the Filebeat configuration of your data source. The port should not be 5000, 5141, or 5044 - as Guardium univer***REMOVED***l connector reserves these ports for MongoDB events. If the port appears in other connector configurations on this Guardium system, make sure it flags events as type "filebeat":

beats {
    port => 5046
    type => "filebeat"
    # For SSL over Filebeat, uncomment the following lines after generating a SSL key and a certificate using GuardAPI (see documentation), copy the public certificate to your data source and adjust Filebeat    configuration:
    #ssl => true
    #ssl_certificate => "\${SSL_DIR}/logstash-beats.crt"
    #ssl_key => "\${SSL_DIR}/logstash-beats.key"
}
```

### Filter Configuration

```
# For this to work, the Filebeat configuration on your data source should tag the events it is sending.
if [type] == "filebeat" and "hdfs" in [tags] {
    if "callerContext=" in [mes***REMOVED***ge] and "trackingId=" in [mes***REMOVED***ge] {
        dissect {
            mapping => {
                "mes***REMOVED***ge" => "%{timestamp} INFO FSNamesystem.audit: allowed=%{allowed}	ugi=%{ugi}	ip=%{hostname}/%{ip}	cmd=%{cmd}	src=%{src}	dst=%{dst}	perm=%{perm}	trackingId=%{tracking_id}	proto=%{proto}	callerContext=%{call_ctx}"
            }
        }
    } else if "trackingId=" in [mes***REMOVED***ge] {
        dissect {
            mapping => {
                "mes***REMOVED***ge" => "%{timestamp} INFO FSNamesystem.audit: allowed=%{allowed}	ugi=%{ugi}	ip=%{hostname}/%{ip	cmd=%{cmd}	src=%{src}	dst=%{dst}	perm=%{perm}	trackingId=%{tracking_id}	proto=%{proto}"
            }
        }
    } else if "callerContext=" in [mes***REMOVED***ge] {
        dissect {
            mapping => {
                "mes***REMOVED***ge" => "%{timestamp} INFO FSNamesystem.audit: allowed=%{allowed}	ugi=%{ugi}	ip=%{hostname}/%{ip}	cmd=%{cmd}	src=%{src}	dst=%{dst}	perm=%{perm}	proto=%{proto}	callerContext=%{call_ctx}"
            }
        }
    } else {
        dissect {
            mapping => {
                "mes***REMOVED***ge" => "%{timestamp} INFO FSNamesystem.audit: allowed=%{allowed}	ugi=%{ugi}	ip=%{hostname}/%{ip}	cmd=%{cmd}	src=%{src}	dst=%{dst}	perm=%{perm}	proto=%{proto}"
            }
        }
    }
}
if [type] == "filebeat" and "hdfs" in [tags] and "_dissectfailure" not in [tags] {
        hdfs_guardium_filter {}
}

# uncomment to test events/sec
#       metrics {
#               meter => "events"
#               add_tag => "metric"
#       }
```

## Configuring the HDFS filters in Guardium Insights

To configure this plug-in for Guardium Insights, follow [this guide.](https://github.com/IBM/univer***REMOVED***l-connectors/blob/main/docs/UC_Configuration_GI.md)

In the input configuration section, refer to the Filebeat section.

## Tested HDFS Versions

- Cloudera 7.1

## Tested Guardium Versions

- v11.3

## Univer***REMOVED***l Connector Commons Version

- 1.0.0

## Logstash Core Version

- 7.5.3

## HDFS Example Audits

```
2020-11-29 10:44:07,035 INFO FSNamesystem.audit: allowed=true	ugi=hbase (auth:SIMPLE)	ip=/x.x.x.x	cmd=getfileinfo	src=/hbase/WALs/cdh713-a-2.x.com,16020,1606673870978/cdh713-a-2.x.com%2C16020%2C1606673870978.cdh713-a-2.x.com%2C16020%2C1606673870978.regiongroup-0.1606675447034	dst=null	perm=null	proto=rpc	callerContext=CLI
2020-11-29 10:44:07,037 INFO FSNamesystem.audit: allowed=true	ugi=hbase (auth:SIMPLE)	ip=/x.x.x.x	cmd=create	src=/hbase/WALs/cdh713-a-2.x.com,16020,1606673870978/cdh713-a-2.x.com%2C16020%2C1606673870978.cdh713-a-2.x.com%2C16020%2C1606673870978.regiongroup-0.1606675447034	dst=null	perm=hbase:hbase:rw-r--r--	proto=rpc	callerContext=CLI
2020-11-29 10:44:07,060 INFO FSNamesystem.audit: allowed=true	ugi=hbase (auth:SIMPLE)	ip=/x.x.x.x	cmd=setTimes	src=/hbase/WALs/cdh713-a-2.x.com,16020,1606673870978/cdh713-a-2.x.com%2C16020%2C1606673870978.cdh713-a-2.x.com%2C16020%2C1606673870978.regiongroup-0.1606673886016	dst=null	perm=hbase:hbase:rw-r--r--	proto=rpc	callerContext=CLI
2020-11-29 10:44:07,062 INFO FSNamesystem.audit: allowed=true	ugi=hbase (auth:SIMPLE)	ip=/x.x.x.x	cmd=rename	src=/hbase/WALs/cdh713-a-2.x.com,16020,1606673870978/cdh713-a-2.x.com%2C16020%2C1606673870978.cdh713-a-2.x.com%2C16020%2C1606673870978.regiongroup-0.1606673886016	dst=/hbase/oldWALs/cdh713-a-2.x.com%2C16020%2C1606673870978.cdh713-a-2.x.com%2C16020%2C1606673870978.regiongroup-0.1606673886016	perm=hbase:hbase:rw-r--r--	proto=rpc	callerContext=CLI
2020-11-29 10:44:07,065 INFO FSNamesystem.audit: allowed=true	ugi=hbase (auth:SIMPLE)	ip=/x.x.x.x	cmd=setTimes	src=/hbase/WALs/cdh713-a-2.x.com,16020,1606673870978/cdh713-a-2.x.com%2C16020%2C1606673870978.cdh713-a-2.x.com%2C16020%2C1606673870978.regiongroup-0.1606673948128	dst=null	perm=hbase:hbase:rw-r--r--	proto=rpc	callerContext=CLI
2020-11-29 10:44:07,068 INFO FSNamesystem.audit: allowed=true	ugi=hbase (auth:SIMPLE)	ip=/x.x.x.x	cmd=rename	src=/hbase/WALs/cdh713-a-2.x.com,16020,1606673870978/cdh713-a-2.x.com%2C16020%2C1606673870978.cdh713-a-2.x.com%2C16020%2C1606673870978.regiongroup-0.1606673948128	dst=/hbase/oldWALs/cdh713-a-2.x.com%2C16020%2C1606673870978.cdh713-a-2.x.com%2C16020%2C1606673870978.regiongroup-0.1606673948128	perm=hbase:hbase:rw-r--r--	proto=rpc	callerContext=CLI
2020-11-29 10:44:07,090 INFO FSNamesystem.audit: allowed=true	ugi=hbase (auth:SIMPLE)	ip=/x.x.x.x	cmd=getfileinfo	src=/hbase/WALs/cdh713-a-2.x.com,16020,1606673870978/cdh713-a-2.x.com%2C16020%2C1606673870978.meta.1606675447089.meta	dst=null	perm=null	proto=rpc	callerContext=CLI
2020-11-29 10:44:07,092 INFO FSNamesystem.audit: allowed=true	ugi=hbase (auth:SIMPLE)	ip=/x.x.x.x	cmd=create	src=/hbase/WALs/cdh713-a-2.x.com,16020,1606673870978/cdh713-a-2.x.com%2C16020%2C1606673870978.meta.1606675447089.meta	dst=null	perm=hbase:hbase:rw-r--r--	proto=rpc	callerContext=CLI
2020-11-29 10:44:07,137 INFO FSNamesystem.audit: allowed=true	ugi=hbase (auth:SIMPLE)	ip=/x.x.x.x	cmd=setTimes	src=/hbase/WALs/cdh713-a-2.x.com,16020,1606673870978/cdh713-a-2.x.com%2C16020%2C1606673870978.meta.1606675446889.meta	dst=null	perm=hbase:hbase:rw-r--r--	proto=rpc	callerContext=CLI
2020-11-29 10:44:07,139 INFO FSNamesystem.audit: allowed=true	ugi=hbase (auth:SIMPLE)	ip=/x.x.x.x	cmd=rename	src=/hbase/WALs/cdh713-a-2.x.com,16020,1606673870978/cdh713-a-2.x.com%2C16020%2C1606673870978.meta.1606675446889.meta	dst=/hbase/oldWALs/cdh713-a-2.x.com%2C16020%2C1606673870978.meta.1606675446889.meta	perm=hbase:hbase:rw-r--r--	proto=rpc	callerContext=CLI
2020-11-29 10:44:07,142 INFO FSNamesystem.audit: allowed=true	ugi=hbase (auth:SIMPLE)	ip=/x.x.x.x	cmd=getfileinfo	src=/hbase/WALs/cdh713-a-2.x.com,16020,1606673870978/cdh713-a-2.x.com%2C16020%2C1606673870978.cdh713-a-2.x.com%2C16020%2C1606673870978.regiongroup-0.1606675447142	dst=null	perm=null	proto=rpc	callerContext=CLI
2020-11-29 10:44:07,146 INFO FSNamesystem.audit: allowed=true	ugi=hbase (auth:SIMPLE)	ip=/x.x.x.x	cmd=create	src=/hbase/WALs/cdh713-a-2.x.com,16020,1606673870978/cdh713-a-2.x.com%2C16020%2C1606673870978.cdh713-a-2.x.com%2C16020%2C1606673870978.regiongroup-0.1606675447142	dst=null	perm=hbase:hbase:rw-r--r--	proto=rpc	callerContext=CLI
2020-11-29 10:44:07,167 INFO FSNamesystem.audit: allowed=true	ugi=hbase (auth:SIMPLE)	ip=/x.x.x.x	cmd=setTimes	src=/hbase/WALs/cdh713-a-2.x.com,16020,1606673870978/cdh713-a-2.x.com%2C16020%2C1606673870978.cdh713-a-2.x.com%2C16020%2C1606673870978.regiongroup-0.1606675447034	dst=null	perm=hbase:hbase:rw-r--r--	proto=rpc	callerContext=CLI
2020-11-29 10:44:07,170 INFO FSNamesystem.audit: allowed=true	ugi=hbase (auth:SIMPLE)	ip=/x.x.x.x	cmd=rename	src=/hbase/WALs/cdh713-a-2.x.com,16020,1606673870978/cdh713-a-2.x.com%2C16020%2C1606673870978.cdh713-a-2.x.com%2C16020%2C1606673870978.regiongroup-0.1606675447034	dst=/hbase/oldWALs/cdh713-a-2.x.com%2C16020%2C1606673870978.cdh713-a-2.x.com%2C16020%2C1606673870978.regiongroup-0.1606675447034	perm=hbase:hbase:rw-r--r--	proto=rpc	callerContext=CLI
2020-11-29 10:44:07,489 INFO FSNamesystem.audit: allowed=true	ugi=hbase (auth:SIMPLE)	ip=/10.11.18.117	cmd=create	src=/hbase/MasterProcWALs/pv2-00000000000000000003.log	dst=null	perm=hbase:hbase:rw-r--r--	proto=rpc	callerContext=CLI
2020-11-29 10:44:07,497 INFO FSNamesystem.audit: allowed=true	ugi=hbase (auth:SIMPLE)	ip=/10.11.18.117	cmd=listStatus	src=/hbase/MasterProcWALs	dst=null	perm=null	proto=rpc	callerContext=CLI
2020-11-29 10:44:07,519 INFO FSNamesystem.audit: allowed=true	ugi=hbase (auth:SIMPLE)	ip=/10.11.18.117	cmd=create	src=/hbase/MasterProcWALs/pv2-00000000000000000004.log	dst=null	perm=hbase:hbase:rw-r--r--	proto=rpc	callerContext=CLI
2020-11-29 10:44:07,523 INFO FSNamesystem.audit: allowed=true	ugi=hbase (auth:SIMPLE)	ip=/10.11.18.117	cmd=listStatus	src=/hbase/MasterProcWALs	dst=null	perm=null	proto=rpc	callerContext=CLI
2020-11-29 10:44:07,544 INFO FSNamesystem.audit: allowed=true	ugi=hbase (auth:SIMPLE)	ip=/10.11.18.117	cmd=create	src=/hbase/MasterProcWALs/pv2-00000000000000000005.log	dst=null	perm=hbase:hbase:rw-r--r--	proto=rpc	callerContext=CLI
2020-11-29 10:44:07,550 INFO FSNamesystem.audit: allowed=true	ugi=hbase (auth:SIMPLE)	ip=/10.11.18.117	cmd=listStatus	src=/hbase/MasterProcWALs	dst=null	perm=null	proto=rpc	callerContext=CLI
```

## License

The license is Apache 2.0. Please refer to the included LICENSE file for more information.
