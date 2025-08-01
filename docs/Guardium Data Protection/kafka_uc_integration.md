# Universal Connector and Kafka Integration on Guardium - Configuration Guide

Welcome to the configuration guide for integrating a Universal Connector (UC) with Kafka on Guardium. This guide provides step-by-step instructions to set up the necessary configurations and installations for seamless communication between Guardium and Kafka.

**Note**: Do not install Sniffer patches on Kafka nodes. 

## Table of Contents

1. [Set up UC Configurations on Guardium](#1-Configuring-UC-on-Guardium-Data-Protection)
    - 1.1 [Creating a Kafka Cluster on Guardium](#11-Creating-a-Kafka-Cluster-on-Guardium)
    - 1.2 [Configuring Universal Connector](#12-Configuring-Universal-Connector)
      	- 1.2.1 [Configuring UC on Guardium by using the Central Manager](#121-Configuring-UC-on-Guardium-by-using-the-Central-Manager)
      	- 1.2.2 [Configuring UC on Guardium by using the legacy workflow](#122-Configuring-UC-on-Guardium-by-using-the-legacy-workflow)

2. [Configuring native audit and rsyslog on Datasource Server](#2-Configuring-native-audit-and-rsyslog-on-Datasource-Server)
    - 2.1 [Prerequisite ](#21-prerequisite)
    - 2.2 [Installing Rsyslog on the Database Server](#22-installing-rsyslog-on-the-database-server)
    - 2.3 [Installing Kafka Module for rsyslog on Database Server](#23-Installing-Kafka-Module-for-rsyslog-on-Database-Server)
3. [Enabling logging on the database](#3-Enabling-logging-on-the-database)
    - 3.1 [Enabling native audit on Postgres](#31-enabling-native-audit-on-postgres)
    - 3.2 [Enabling audit on Yugabyte](#32-enabling-audit-on-yugabyte)
4. [Configuring rsyslog to send native audit data to Guardium via kafka](#4-Configuring-rsyslog-to-send-native-audit-data-to-Guardium-via-kafka)
5. [Known limitations](#5-Limitations)  
   
---

## 1 Configuring Universal Connector on Guardium Data Protection 
### 1.1 Creating a Kafka Cluster on Guardium
For information on creating Kafka Clusters, see the [Creating Kafka clusters](https://www.ibm.com/docs/en/gdp/12.x?topic=manager-creating-kafka-clusters) topic.

### 1.2 Configuring Universal Connector 

#### 1.2.1 Configuring UC on Guardium by using the Central Manager 
For information on configuring the Universal Connector on Guardium using the new workflow, see [Configuring a universal connector by using Central Manager](https://www.ibm.com/docs/en/gdp/12.x?topic=connector-configuring-universal-by-using-central-manager) topic.

#### 1.2.2 Configuring UC on Guardium by using the legacy workflow

1. On the **Guadium Managed Unit** machine, go to **Setup** > **Tools and Views** > **Configure Universal Connector** page.

    **Important Note**: Ensure that **Guardium Universal Connector** is enabled.

3. Click the **Add** icon to add a new configuration.

4. Select the **EDB PostgreSQL using kafka (high volume)** connector template. 

5. In the **Connector Name** field, enter a unique connector name. 

   Ensure the Connector name does not contain special characters other than underscore (_) and hyphen (-).

6. Click **Save**. 

Your UC is now configured and ready to receive new events from the data source.

**Note**: To ensure data failover and load-balancing, define the same configuration name on the Managed Unit for all Universal Connectors in the same Kafka Cluster. 


## 2. Configuring native audit and rsyslog on Datasource Server

### 2.1 Prerequisite 
 Verify if rsyslog is installed on your system by using the following command.

```bash
service rsyslog status
```

In some systems, rsyslog may have a different name. Run the following command to verify if rsyslog is installed on your system.

```
sudo yum list --available | grep rsyslog | grep kafka
```


### 2.2 Installing rsyslog on the database server

1. Install rsyslog by using one of the following commands based on your system:

	- For yum-based systems:

	```bash
	sudo yum install rsyslog
	```

	- For apt-based systems:

	```bash
	sudo apt-get install rsyslog
	```

	- For apk-based systems:
	
	```bash
	sudo apk add rsyslog
	```

2. Start rsyslog and check its status:

```bash
sudo systemctl start rsyslog
sudo systemctl enable rsyslog
service rsyslog status
```
For more information on rsyslog, see [Rsyslog Documentation](https://www.rsyslog.com/doc/master/installation/index.html)

### 2.3 Installing Kafka module for rsyslog on database server
1. Verify that rsyslog-kafka package is installed:
	- For yum-based systems:

	```bash
	yum list installed | grep rsyslog-kafka
	```

	or

	- For apt-based systems:

	```bash
	apt-get list --installed | grep rsyslog-kafka
	```

2. Install rsyslog-kafka by using the following command. 

	- For yum-based systems:

	```bash
	sudo yum install rsyslog-kafka
	```

	or

	- For apt-based systems:

	```bash
	sudo apt-get install rsyslog-kafka
	```

## 3 Enabling logging on the database
### 3.1 Enabling native audit on Postgres

1. You can configure PostgreSQL so that it can send logs to rsyslog. Open the PostgreSQL configuration file and make the following changes.

**Note**: The PostgreSQL configuration file is usually located at `/etc/postgresql/{version}/main/postgresql.conf` on Linux systems. 

	```conf
	# Enable logging
	logging_collector = on
	log_destination = 'csvlog'			
	
	log_directory = '<PATH_TO_DIRECTORY>'            # directory where log files are written,
	log_filename = 'edb-%Y-%m-%d_%H%M%S.log'         # log file name pattern,
	log_file_mode = 0600			         # creation mode for log files
	log_rotation_age = 1d			     
	log_rotation_size = 10MB
			     
	log_min_messages = error		         # this enables logging error, log, fatal and panic events
	
	log_connections = on                             # enable logon
	log_disconnections = on                          # enable logoff
	
	log_error_verbosity = default		         # terse, default, or verbose messages
	log_line_prefix = ' %t '			 # Use '%t ' to enable log-reading
	
	log_statement = 'all'			         # none, ddl, mod, all
	    
	log_timezone = 'America/New_York'                # this timezone should be the same timezone as dbserver machine timezone   
	```

2. Ensure that timezone specified in this file is the actual timezone of db server machine.
 
3. Restart PostgreSQL to apply the configuration modifications. For example, see the following enterprise db postgres service: 

	```bash
	sudo service edb-as-15 restart
	sudo service edb-as-15 status
	```

The exact file paths and configurations may vary depending on your operating system and PostgreSQL version.

### 3.2 Enabling audit on Yugabyte
To configure audit logs for Yugabyte DB, see [Enabling the audit logs](https://github.com/IBM/universal-connectors/blob/main/filter-plugin/logstash-filter-yugabyte-guardium/README.md#enabling-the-audit-logs)

## 4 Configuring rsyslog to send native audit data to Guardium via kafka 

1. Edit your rsyslog configuration file (typically located at `/etc/rsyslog.conf` or `/etc/rsyslog.d/50-default.conf`) and add the following section. Ensure that you substitute the designated placeholders with the appropriate values:

	- **Message format:** Replace the `<SERVER_IP>` with the server IP for example: `1.11.11.11`.
	
	- **Input:** Replace `<PATH_TO_DIRECTORY>` with the actual path to the datasource logs directory, such as `/var/lib/edb/as15/data`.
	
	- **Action:** Replace the `<TOPIC_NAME>` with your database server name (without domain).
	
	- **Action:** Replace the brokers (`<MANAGED_UNIT_#>`) with the hosts of the managed units in the Kafka cluster established earlier. Note that the port remains constant at `9093`, as the Kafka 	cluster is configured to listen on this port.
	- **Topic:** Replace '<UC_Connector_Name>_<Connector_ID>' with the UC connection name followed by an underscore and then the Connector ID. You can find the Connector ID after underscore on the         **Kafka cluster Management page > Topic details** column. For example, if the Univeral Connector Connection name is 'EDB_PG' then **Topic** name is 'EDB_PG_2', where 2 is the collector ID.

	**Note:** 
	* For Guardium Data Protection version 12.1 with appliance bundle p115, **Topic Name** is same as the Connector Name.
	
 	* For Guardium Data Protection version 12.1 appliance patch p115 and later, **Topic Name** is <UC_Connector_Name>_<Connector_ID>.     
	
 
 **Syslog configuration for PostgreSQL**:
	```conf
	module(load="imfile")
	# Select input configuration by datasource: 
	# Use the following input for Postgres
	input(type="imfile" Tag="postgres" startmsg.regex="^[[:digit:]]{4}-[[:digit:]]{2}-[[:digit:]]{2}" File="<PATH_TO_DIRECTORY>/*.csv" ruleset="kafkaRuleset")
	
	# Template for audit message that will be sent to Kafka topic of UC. 
	$template UcMessageFormat,"%timegenerated:::date-rfc3339%,%HOSTNAME%,<SERVER_IP>,%msg%"
	
	# Template for message key ID that will be sent to kafka topic of UC for Medium-Large database types 
	$template UcKeyHostname, "%HOSTNAME%" 
	
	# Template for message key ID that will be sent to kafka topic of UC for Extra Large database type    
	$template UcSessionID,"%msg:F,44:6%"
	    
	module(load="omkafka")
	ruleset(name="kafkaRuleset") {
	       action(type="omkafka"
	           template="UcMessageFormat"
	           broker=["<KAFKA_NODE_1>:9093, <KAFKA_NODE_2>:9093, <KAFKA_NODE_3>:9093"]
	           topic="<UC_Connector_Name>_<Connector ID>"
	           dynakey = "on"
	           key = "UcSessionID"
	           queue.filename="omkafkaq"
	           queue.spoolDirectory="/var/lib/rsyslog"
	           queue.size="500000"
	           queue.maxdiskspace="536870912"
	           queue.lowwatermark="20000"
	           queue.highwatermark="400000"
	           queue.discardmark="250000"
	           queue.type="LinkedList"
	           queue.discardseverity="8"
	           queue.saveonshutdown="on"
	           queue.dequeuebatchsize="1024"
	           partitions.auto="on"
	           action.resumeRetryCount="-1"
	           action.resumeInterval="10"
	           action.reportSuspension="on"
	           errorFile="/var/log/rsyslog.err"
	           confParam=[ "compression.codec=snappy",
	           		"queue.buffering.max.messages=10000000",
                	"socket.timeout.ms=1000",
               		"socket.keepalive.enable=true","security.protocol=ssl",
               		"debug=all",
	                "ssl.ca.location=<_ENTER_CERTIFICATE>"
	           ]
		)
	}
	```
	
	
	**Syslog configuration for YugabyteDB**:
	```conf
	module(load="imfile")
	
	# Input configuration for PostgreSQL logs
	input(
	    type="imfile"
	    File="<PATH_TO_DIRECTORY>/postgresql-*"
	    tag="syslog"
	    ruleset="kafkaRuleset_postgresql"
	    startmsg.regex="^([0-9.]+).*(LOG|ERROR|FATAL):.*"
	)
	
	# Input configuration for Yugabyte logs
	input(
	    type="imfile"
	    File="<PATH_TO_DIRECTORY>/yb-tserver.*.*.log.*.*"
	    tag="syslog"
	    ruleset="kafkaRuleset_yugabyte"
	    startmsg.regex="^(((I|W|E|F)[0-9]+\\s[0-9:.]+).*)$|^([0-9.]+).*(LOG|ERROR|FATAL):.*"
	)
	
	# The template for message formatting
	$template UcMessageFormat,"%HOSTNAME%,<SERVER_IP>,%msg%"
	
	# The Key for PostgreSQL events: Session ID from the audit message
	$template UcSessionID_PostgreSQL,"%msg:F,32:7%"
	
	# The Key for Yugabyte events: Client IP and Port combination, identifying the client-to-database session
	$template UcSessionID_Yugabyte,"%msg:F,124:3%,%msg:F,124:4%"
	
	# Load omkafka module for Kafka output
	module(load="omkafka")
	# Kafka ruleset for PostgreSQL logs
	ruleset(name="kafkaRuleset_postgresql") {
	    action(type="omkafka"
	        template="UcMessageFormat"
	        broker=["<KAFKA_NODE_1>:9093, <KAFKA_NODE_2>:9093, <KAFKA_NODE_3>:9093"]
	        topic="<UC_Connector_Name>_<Connector_ID>"
	        dynakey="on"
	        key="UcSessionID_PostgreSQL"
	        queue.filename="omkafkaq_postgres"
	        queue.spoolDirectory="/var/lib/rsyslog"
	        queue.size="300000"
	        queue.maxdiskspace="536870912"
	        queue.lowwatermark="20000"
	        queue.highwatermark="200000"
	        queue.discardmark="250000"
	        queue.type="LinkedList"
	        queue.discardseverity="4"
	        queue.saveonshutdown="on"
	        queue.dequeuebatchsize="4"
	        partitions.auto="on"
	        errorFile="/var/log/rsyslog.err"
	        confParam=[
	            "compression.codec=snappy",
	            "socket.timeout.ms=1000",
	            "socket.keepalive.enable=true",
	            "security.protocol=ssl",
	            "debug=all",
	            "ssl.ca.location=<ENTER_CERTIFICATE_PATH>"
	        ]
	    )
	}
	
	# Kafka ruleset for Yugabyte logs
	ruleset(name="kafkaRuleset_yugabyte") {
	    if $msg contains "source" and $msg contains "port" then {
	        action(type="omkafka"
	            template="UcMessageFormat"
	            broker=["<MANAGED_UNIT_1>:9093, <MANAGED_UNIT_2>:9093, <MANAGED_UNIT_3>:9093"]
	            topic="<UC_Connection_Name>_<Connector_ID>"
	            dynakey="on"
	            key="UcSessionID_Yugabyte"
	            queue.filename="omkafkaq_yugabyte"
	            queue.spoolDirectory="/var/lib/rsyslog"
	            queue.size="300000"
	            queue.maxdiskspace="536870912"
	            queue.lowwatermark="20000"
	            queue.highwatermark="200000"
	            queue.discardmark="250000"
	            queue.type="LinkedList"
	            queue.discardseverity="4"
	            queue.saveonshutdown="on"
	            queue.dequeuebatchsize="4"
	            partitions.auto="on"
	            errorFile="/var/log/rsyslog.err"
	            confParam=[
	                "compression.codec=snappy",
	                "socket.timeout.ms=1000",
	                "socket.keepalive.enable=true",
	            	"security.protocol=ssl",
			"debug=all",
			"ssl.ca.location=<ENTER_CERTIFICATE_PATH>"
	            ]
	        )
	    }
	}
	```

2. Run the following command to verify the configuration.

	```bash
	rsyslogd -N1
	```

	Verify successful response: `rsyslogd: End of config validation run. Bye.`

3. After making changes to the syslog configuration, run the following command to restart the syslog daemon to apply the changes.
	Run:

	```bash
	sudo service rsyslog restart
	```

4. Verify by running the following command. 

	```bash
	sudo tail -f /var/log/syslog
	```

**Important Notes** 
- Search for error messages related to omkafka. If everything is set up correctly, rsyslog should send the log messages to your Kafka broker
- The steps may vary depending on your operating system and rsyslog version. For more accurate information, see the rsyslog documentation and the documentation specific to your distribution.
- You can also use Command Line Interface (CLI) for various Kafka related functionality, such as create, edit, delete, get Kafka cluster, etc. For more information, see [GuardAPI and REST API commands](https://www.ibm.com/docs/en/guardium/12.x?topic=guardapi-rest-api-commands).


## 5. Limitations

* Client hostname is not captured by EDB PostgresSQL native audit on traffic of some db client tools, therefore can not be reported in Guardium.

* Source program is not captured by EDB PostgresSQL native audit on failed login attempt, therefore can not be reported in Guardium.

* Operating system user is not captured by EDB PostgresSQL native audit, therefore can not be reported in Guardium.

* In any Kafka cluster that is deployed with 3 or more Kafka nodes, if only 1 node is up and running, then there is loss of data.
