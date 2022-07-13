# MySql-Percona-Guardium Logstash filter plug-in 

This is a [Logstash](https://github.com/elastic/logstash) filter plug-in for the universal connector that is featured in IBM Security Guardium. It is an extension of MySql-Guardium Logstash filter plug-in. See [MySql-Guardium Logstash filter plug-in](https://github.ibm.com/Activity-Insights/universal-connectors/blob/master/filter-plugin/logstash-filter-mysql-guardium/README.md)

## Filebeat configuration


1.  On the database, configure the Filebeat data shipper to forward the audit logs to the Guardium universal connector. In the file filebeat.yml, usually located in /etc/filebeat/filebeat.yml, modify the `filebeat.inputs` section.

  a.  Change the `enabled` field to `true`, update the Logstash host, and add the path of the audit logs. 
  
  For example:
        
            #‌#‌#‌#‌#‌#‌#‌#‌#‌#‌#‌#‌#‌#‌#‌#‌#‌#‌#‌#‌#‌#‌#‌#‌#‌#‌#‌#‌#‌#‌#‌#‌#‌#‌#‌#‌#‌#‌#‌#‌#‌#‌#‌#‌#‌#‌#‌#‌#‌#‌#‌#‌
	    filebeat.inputs:
                type: log
                #‌ Change to true to enable this input configuration.
                enabled: true 
                paths:
                #‌- c:\programdata\elasticsearch\logs*
                C:\downloads\docker_volumes*
                output.logstash:
                #‌ The Logstash hosts
                hosts: ["<Guardium IP>:5045"] #‌ just to ip/host name of the gmachine
                # Paths that should be crawled and fetched. Glob based paths.
                paths: 
                - /var/lib/mysql/audit.log
                tags: ["mysqlpercona"] 
                #‌#‌#‌#‌#‌#‌#‌#‌#‌#‌#‌#‌#‌#‌#‌#‌#‌#‌#‌#‌#‌#‌#‌#‌#‌#‌#‌#‌#‌#‌#‌#‌#‌#‌#‌#‌#‌#‌#‌#‌#‌#‌#‌#‌#‌#
            
        
  Copied!
        
  b. Filebeat communicates with the Guardium universal connector via port 5045. Verify that port 5045 is open.

2.  Restart the filebeat service by entering:
    
        sudo service filebeat restart
    

Percona configuration
---------------------

1.  On the database, update the file: /etc/percona-server.conf.d/mysqld.cnf
    
        symbolic-links=0
            bind_address=0.0.0.0
            log-error=/var/log/mysqld.log
            pid-file=/var/run/mysqld/mysqld.pid
            audit_log_format=JSON
            audit_log_handler=FILE
            audit_log_file=/var/lib/mysql/audit.log
    
2.  Restart the mysql service.

## Guardium configuration
1. On the collector, go to Setup > Tools and Views > Configure Universal Connector.

2. Click Upload and Install plug-ins, and upload the file logstash-filter-mysql_percona_guardium_filter.

3. Type a name in the Connector name field.
4. In the Input configuration field, enter:

         ##   Change the port to match the Filebeat configuration on your data source. Port should not be 5000, 5141 or 5044, as Guardium Universal Connector reserves these ports for MongoDB events. If this port appears in other Connector configurations, on this Guardium system, make sure it flags events as type "filebeat":
       beats { port => 5045 type => "filebeat" } 

where the TCP or UDP port is the same one you configured in the Filebeat configuration.

5. In the Filter configuration field, enter:
       
         # For this to work, the Filebeat configuration on your data source should tag the events it is sending.  
       if [type] == "filebeat" and "mysqlpercona" in [tags] {
       mutate { add_field => { "source_program" => "percona-audit" } }
       mutate { add_field => { "client_hostname" => "%{[agent][hostname]}" } }
       mutate { add_field => { "server_hostname" => "%{[host][hostname]}" } }
       mutate { add_field => { "server_ip" => "%{[host][ip][0]}" } }
       mutate { replace => { "message" => "%{source_program}: %{message}" } }

	   mysql_percona_guardium_filter {}
	
   	     # keep original event fields, for debugging
   	     if "_mysqlguardium_parse_error" not in [tags] {
			mutate { remove_field => [
					"message", "syslog_timestamp", "source_program", "program",
					"syslog_pid", "syslog_message",
					"server_hostname", "client_hostname", "host",
					"ecs", "log", "agent", "input"]
			}   	}
        }

6. Click Save. Guardium validates the new connector, and enables the universal connector if it was disabled. After it is validated,the connector appears in the Configure Universal Connector page.

## Configuring the MySQL for Percona filters in Guardium Insights

To configure this plug-in for Guardium Insights, follow [this guide.](https://github.com/RefaelAdi/universal-connectors/blob/INS-18044/docs/UC_Configuration_GI.md#Configuring_Filebeat_to_forward_audit_logs_to_Guardium)

In the input configuration section, refer to the Filebeat section.


 ## Contribute
  You can enhance this filter and open a pull request with suggested changes - or you can use the project to create a different filter plug-in for Guardium that supports other data sources. ## References See \[documentation for Logstash Java plug-ins\](https://www.elastic.co/guide/en/logstash/current/contributing-java-plugin.html). See \[Guardium Universal connector commons\](https://www.github.com/IBM/guardium-universalconnector-commons) library for more details regarding the standard Guardium Record object.
