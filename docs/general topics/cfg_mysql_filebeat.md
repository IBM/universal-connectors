# MySQL auditing by using Filebeat connector template

Learn about a typical MySQL connector that uses Filebeat.

**Note:**

-   MySQL plug-ins do not send the DB name to Guardium, if the DB commands are performed by using MySQL native client.
-   When connected with a MySQL plug-in, queries for non-existent tables are not logged to `GDM_CONSTRUCT`.

Input configuration:

```
### Change the port to match the Filebeat configuration on your data source. Port should not be 5000, 5141 or 5044, as Guardium Universal Connector reserves these ports for MongoDB events. If this port appears in other Connector configurations, on this Guardium system, make sure it flags events as type "filebeat":

beats { port => 5045 type => "filebeat" }
```

Filter configuration:

```
# For this to work, the Filebeat configuration on your data source should tag the events it is sending.  
if [type] == "filebeat" and "mysql" in [tags] {
    mutate { add_field => { "source_program" => "mysql_audit_log" } }
    mutate { add_field => { "client_hostname" => "%{[agent][hostname]}" } }
    mutate { add_field => { "server_hostname" => "%{[host][hostname]}" } }
    mutate { add_field => { "server_ip" => "%{[host][ip][0]}" } }
    mutate { replace => { "message" => "%{source_program}: %{message}" } }

	mysql_filter_guardium {}
	
	# keep original event fields, for debugging
	if "_mysqlguardium_parse_error" not in [tags] {
			mutate { remove_field => [
					"message", "syslog_timestamp", "source_program", "program",
					"syslog_pid", "syslog_message",
					"server_hostname", "client_hostname", "host",
					"ecs", "log", "agent", "input"]
			}
	}
}

# uncomment to test events/sec
#       metrics {
#               meter => "events"
#               add_tag => "metric"
#       }
```
