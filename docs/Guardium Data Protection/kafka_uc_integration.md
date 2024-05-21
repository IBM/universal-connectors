:# Universal Connector and Kafka Integration on Guardium - Configuration Guide

Welcome to the configuration guide for integrating a Universal Connector (UC) with Kafka on Guardium. This guide provides step-by-step instructions to set up the necessary configurations and installations for seamless communication between Guardium and Kafka.


## Table of Contents

1. [Set up UC Configurations on Guardium](#Configuring-UC-on-Guardium-Data-Protection )
    - 1.1 [Creating Kafka Cluster on Guardium (once per deployment)](#11-Creating-Kafka-Cluster-on-Guardium-once-per-deployment)
   	- 1.1.1 [Creating new Kafka clusters ](#Creating-new-Kafka-clusters )
   	- 1.1.2 [Downloading server (Kafka cluster) CA certificate](#Downloading-server-(Kafka-cluster)-CA-certificate)
    - 1.2 [Configuring Universal Connector (UC) on Guardium](#12-Configuring-Universal-Connector-UC-on-Guardium)

2. [Configuring native audit and rsyslog on Datasource Server](#2-Configuring-native-audit-and-rsyslog-on-Datasource-Server)
    - 2.1 [Prerequisites: Install Rsyslog on the Database Server](#21-Prerequisites-install-rsyslog-on-the-database-server)
    - 2.2 [Prerequisites: Install Kafka Module for rsyslog on Database Server](#22-Prerequisites-Install-Kafka-Module-for-rsyslog-on-Database-Server)
    - 2.3 [Enabling logging on the database](#23-Enabling-logging-on-the-database)
    - 2.4 [Configuring rsyslog to send native audit data to Guardium via kafka](#24-Configuring-rsyslog-to-send-native-audit-data-to-Guardium-via-kafka)

3. [Troubleshooting](#3-Troubleshooting)
4. [Known limitations](#4-Limitations)  
   
---

##  Configuring UC on Guardium Data Protection 
### 1.1 Creating a Kafka Cluster (once per deployment)

1. Log in to **Guardium central manager** by using your login credentials.

2. Go to **Manage** > **Central Management** > **Kafka cluster management** page.

   Use the following information to complete various tasks. 

#### 1.1.1 Creating new Kafka clusters 

1. Click **Add** icon to create a new Kafka cluster.

2. In the **Name** field, enter unique cluster name.

3. In the **Cluster member** grid, click add icon to create a member cluster.

4. From the **Select units to add** list, select at least one or maximum three Kakfa nodes and click **OK**.

5. Optionally, if you want to authenticate users before connecting to the Kafka cluster, select enable **Client Authentication**.

   Users need to upload valid client certificates to authenticate themselves to the Kafka cluster. 

7. Select one or more Kafka nodes from the **Cluster member** grid to create a Kafka cluster.

8. Use the **Add** icon before the Kafka cluster name to expand the cluster and view the individual node **Status** and **Details** in the grid.

9. Use the **Start**, **Stop** and **Restart** options to start, stop and restart the individual clusters, respectively. 

**Result**: Kafka cluster is created successfully. 

#### 1.1.2 Downloading server (Kafka cluster) CA certificate
You need to download the server (Kafka cluster) CA certificate to enure that syslog accepts the communication with Kafka cluster. 

1. In the **Kafka cluster management** page, select a cluster from the grid and click **Download server CA**.

2. Copy the certificate to th database server machine and note the location.
For more information, see [Installing an appliance certificate to avoid a browser SSL certificate challenge](https://www.ibm.com/docs/en/guardium/12.x?topic=certificates-installing-appliance-certificate-avoid-browser-ssl-certificate-challenge).

### 1.2 Configuring Universal Connector (UC) on Guardium

1. On the **Guadium Managed Unit** machine, go to **Setup** > **Tools and Views** > **Configure Universal Connector** page.

2. Click **Add** icon to add a new configuration.

3. Select the **EDB PostgreSQL using kafka (high volume)** connector template. 

4. In the **Connector Name** field, enter a unique connector name. 
   Ensure the name does not contain spaces and special characters.

5. Click **Save**. 
   Your UC is now configured and ready to receive new events from datasource.

**Note**: To ensure data failover and loadbalancing, define same configuration on another Kafka node.


## 2. Configuring native audit and rsyslog on Datasource Server
### 2.1 Prerequisites: Install rsyslog on the database server
Verify if rsyslog is installed:

```bash
service rsyslog status
```

If not installed, use one of the following commands based on your system:

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

Start rsyslog and check its status:

```bash
sudo systemctl start rsyslog
sudo systemctl enable rsyslog
service rsyslog status
```

If issues persist, refer to the official rsyslog documentation: [Rsyslog Documentation](https://www.rsyslog.com/doc/master/installation/index.html)
### 2.2 Prerequisites: Install Kafka module for rsyslog on database server
Verify that rsyslog-kafka package is installed:
- For yum-based systems:

```bash
yum list installed | grep rsyslog-kafka
```

or

- For apt-based systems:

```bash
apt-get list --installed | grep rsyslog-kafka
```

If rsyslog-kafka is not installed, install it:

- For yum-based systems:

```bash
sudo yum install rsyslog-kafka
```

or

- For apt-based systems:

```bash
sudo apt-get install rsyslog-kafka
```

### 2.3 Enabling logging on the database
### 2.3.1 Enabling native audit on Postgres

PostgreSQL can be configured to send logs to rsyslog. Open the PostgreSQL configuration file and make the following changes: 

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
(usually located at `/etc/postgresql/{version}/main/postgresql.conf` on Linux systems, but can be different)
* Ensure that timezone specified in this file is the actual timezone of db server machine
Restart PostgreSQL to apply the configuration modifications.
For example, the enterprise db postgres service: 

```bash
sudo service edb-as-15 restart
sudo service edb-as-15 status
```

- The exact file paths and configurations may vary depending on your operating system and PostgreSQL version.

### 2.3.2 Enabling audit on Yugabyte
To configure auditing in Yugabyte DB, follow the instructions in [here](../filter-plugin/logstash-filter-yugabyte-guardium/README.md#2-enabling-the-audit-logs)

### 2.4 Configuring rsyslog to send native audit data to Guardium via kafka 

a. Edit your rsyslog configuration file (typically located at `/etc/rsyslog.conf` or `/etc/rsyslog.d/50-default.conf`).
Add the following section:

Ensure that you substitute the designated placeholders with the appropriate values:

- **Message format:** Replace the `<SERVER_IP>` with the server IP for example: `1.11.11.11`.

- **Input:** Replace `<PATH_TO_DIRECTORY>` with the actual path to the datasource logs directory, such as `/var/lib/edb/as15/data`.

- **Action:** Replace the `<TOPIC_NAME>` with your database server name (without domain).

- **Action:** Replace the brokers (`<MANAGED_UNIT_#>`) with the hosts of the managed units in the Kafka cluster established earlier. Note that the port remains constant at `9093`, as the Kafka cluster is configured to listen on this port.

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
           broker=["<KAFKA_NODE_1>:9093", "<KAFKA_NODE_2>:9093", "<KAFKA_NODE_3>:9093"]
           topic="<_ENTER_CONNECTOR_NAME_FROM_TOPIC_1.2_STEP_4_>"
           dynakey = "on"
           key = "UcSessionID"
           queue.filename="omkafkaq"
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
           confParam=[ "compression.codec=snappy",
               	"socket.timeout.ms=1000",
               	"socket.keepalive.enable=true",
		"security.protocol=ssl",
		"debug=all",
                "ssl.ca.location=<_ENTER_CERTIFICATE_PATH_FROM_TOPIC_1.1.2_STEP_2_>"
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
        broker=["<KAFKA_NODE_1>:9093", "<KAFKA_NODE_2>:9093", "<KAFKA_NODE_3>:9093"]
        topic="<ENTER_CONNECTOR_NAME_FROM_TOPIC_1.2>"
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
            "ssl.ca.location=<Enter_CERTIFICATE_PATH_FROM_DOWLOADING_SERVER_CA_STEP_2>"
        ]
    )
}

# Kafka ruleset for Yugabyte logs
ruleset(name="kafkaRuleset_yugabyte") {
    if $msg contains "source" and $msg contains "port" then {
        action(type="omkafka"
            template="UcMessageFormat"
            broker=["<MANAGED_UNIT_1>:9093", "<MANAGED_UNIT_2>:9093", "<MANAGED_UNIT_3>:9093"]
            topic="<ENTER_CONNECTOR_NAME_FROM_TOPIC_1.2>"
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
		"ssl.ca.location=<Enter_CERTIFICATE_PATH_FROM_DOWLOADING_SERVER_CA_STEP_2>"
            ]
        )
    }
}
```

b. Verify the configuration:

```bash
rsyslogd -N1
```

Verify successful response: `rsyslogd: End of config validation run. Bye.`

After making changes to the syslog configuration, restart the syslog daemon to apply the changes:
Run:

```bash
sudo service rsyslog restart
```

Verify by running:

```bash
sudo tail -f /var/log/syslog
```

Search for any error messages related to omkafka. If everything is set up correctly, rsyslog should send the log messages to your Kafka broker.

**Important Note** The steps may vary depending on your operating system and rsyslog version. For more accurate information, see the rsyslog documentation and the documentation specific to your distribution.

## 3. Troubleshooting 
<TBD>

## 4. Limitations

1. Client hostname is not captured by EDB PostgresSQL native audit on traffic of some db client tools, therefore can not be reported in Guardium.

2. Source program is not captured by EDB PostgresSQL native audit on failed login attempt, therefore can not be reported in Guardium.

3. Operating system user is not captured by EDB PostgresSQL native audit, therefore can not be reported in Guardium.
