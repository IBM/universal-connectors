# Redis Enterprise Source Connector

This connector enables IBM Guardium Data Protection (GDP) to monitor and collect audit logs from Redis Enterprise
databases through rsyslog forwarding by using Kafka Connect.

## Meet Redis Enterprise over Syslog Connect

* Environments: On-prem
* Supported inputs: Kafka connect Syslog 2.0
* Supported Guardium versions:
    * Guardium Data Protection: Appliance bundle 12.2 or later
* Tested DB version: Redis on-prem 8.2.1, Containerized Redis Enterprise 8.2.0-78

Kafka-connect is a framework for streaming data between Apache Kafka and other systems.

## Configuring Redis Enterprise

Redis Enterprise is an enterprise-grade in-memory database platform that extends open-source Redis with high
availability, linear scalability, and active-active geo-distribution. It can be deployed on-premises or in any
cloud environment.

### Prerequisites

1. Redis Enterprise cluster installed and running
2. Network connectivity between the Redis Enterprise nodes and the Kafka cluster
3. rsyslog installed and configured on each Redis Enterprise node
4. Redis Enterprise admin console or `rladmin` CLI access with sufficient privileges to configure audit logging

## Configuring audit logging for Redis Enterprise

Redis Enterprise supports audit logging at both the cluster and database level. Audit events are written to the
Redis Enterprise system log, which can then be forwarded to Guardium through rsyslog.

### Procedure

#### Enabling audit logging via rladmin CLI

1. Connect to a Redis Enterprise node:

   ```bash
   ssh <redis-node-hostname>
   ```

2. Enable audit logging at the cluster level:

   ```bash
   /opt/redislabs/bin/rladmin
   
   rladmin cluster config auditing db_conns audit_protocol TCP audit_address <host> audit_port <port>
   ```

3. Exit from Shell
4. Turn it on for your database, DB-level audit configuration
    ```bash
   /opt/redislabs/bin/rladmin tune db <DB_NAME> db_conns_auditing enabled
   ```

3. Verify the audit log configuration using RESTAPI:

   ```bash
    curl -k -u '<USERNAME>:<PASSWORD>' https://localhost:9443/v1/bdbs
   ```
    Make sure in the audit_settings fields, audit_settings: {"audit_mode":"connection_and_crud", ...}

#### Configuring the audit log destination

Redis Enterprise writes audit logs to `/var/log/redis_audit.log` by default. Verify the file exists
and is being populated:

```bash
tail -f /var/log/redis_audit.log
```

A typical audit log entry looks like this:

```
<13>1 2024-11-15T10:23:45.123Z redis-node-1 redis_mgmt - - - {"time":"2024-11-15T10:23:45.123Z","type":"auth","status":"success","username":"admin@example.com","remote_ip":"xxx.xxx.xxx","db":"db_name","cmd":"AUTH"}
```

## Configuring rsyslog forwarding

To forward Redis Enterprise audit logs to Guardium through Kafka, configure rsyslog on each Redis Enterprise node.

### Procedure for Standard (Non-containerised) Deployments

1. Connect to the Redis Enterprise node:

   ```bash
   ssh <redis-node-hostname>
   ```

2. Install rsyslog if not already installed:

   ```bash
   # For RHEL/CentOS:
   sudo yum install -y rsyslog

   # For Ubuntu/Debian:
   sudo apt-get update && sudo apt-get install -y rsyslog
   ```

3. Create the rsyslog configuration file for Redis Enterprise on-prem:

   ```bash
   cat /etc/rsyslog.conf
   global(workDirectory="/tmp/rsyslog")
   module(load="imtcp") # needs to be done just once
   input(type="imtcp" port="514")

   # RFC5424-compliant JSON template that injects your host details cleanly
   template(name="RedisAuditJsonTemplate" type="string"
            string="<133>1 %timegenerated:::date-rfc3339% %hostname% redis-audit - - - serverHostname=<HOSTNAME> %rawmsg%\n")
   
   # Intercept direct Redis TCP streams from port 514
   if ($rawmsg contains "<DB NAME>") then {
   
       # 1. Keep a secure backup copy locally on disk (Zero Data Loss Buffer)
       action(type="omfile"
              file="/var/log/redis_audit.log"
              template="RedisAuditJsonTemplate")
   
       # 2. Forward concurrently to Kafka Connect Node 1
       action(type="omfwd"
              target="<kafka-connect-hostname-1>"
              port="6514"
              protocol="tcp"
              template="RedisAuditJsonTemplate"
              action.resumeRetryCount="-1"
              queue.type="LinkedList"
              queue.size="10000")
   
       # 3. Forward concurrently to Kafka Connect Node 2
       action(type="omfwd"
              target="<kafka-connect-hostname-2>"
              port="6514"
              protocol="tcp"
              template="RedisAuditJsonTemplate"
              action.resumeRetryCount="-1"
              queue.type="LinkedList"
              queue.size="10000")
   
       # 4. Forward concurrently to Kafka Connect Node 3
       action(type="omfwd"
              target="<kafka-connect-hostname-3>"
              port="6514"
              protocol="tcp"
              template="RedisAuditJsonTemplate"
              action.resumeRetryCount="-1"
              queue.type="LinkedList"
              queue.size="10000")
   
       # Prevent these logs from cluttering your standard /var/log/messages
       stop
   ```
   
    For containerized Redis Enterprise 8.2.0-78
    ```bash
    module(load="imtcp")
    input(type="imtcp" port="514")
   
    template(name="RedisAuditJsonTemplate" type="string"
      string="<%PRI%>1 %TIMESTAMP:::date-rfc3339% %HOSTNAME% redis-audit - - - serverHostname=<HOSTNAME> serverPort=<PORT> %rawmsg%\n"
    )
    
    if ($rawmsg contains "<DB NAME>") then {
    
        action(type="omfile"
               file="/var/log/redis_audit.log"
               template="RedisAuditJsonTemplate")
    
        action(type="omfwd"
               Target="<kafka-connect-hostname-1>"
               Port="6514"
               Protocol="tcp"
               template="RedisAuditJsonTemplate"
               queue.type="LinkedList"
               queue.size="10000"
               action.resumeRetryCount="-1"
               action.resumeInterval="10")
    
        action(type="omfwd"
               Target="<kafka-connect-hostname-2>"
               Port="6514"
               Protocol="tcp"
               template="RedisAuditJsonTemplate"
               queue.type="LinkedList"
               queue.size="10000"
               action.resumeRetryCount="-1"
               action.resumeInterval="10")
    
        action(type="omfwd"
               Target="<kafka-connect-hostname-3>"
               Port="6514"
               Protocol="tcp"
               template="RedisAuditJsonTemplate"
               queue.type="LinkedList"
               queue.size="10000"
               action.resumeRetryCount="-1"
               action.resumeInterval="10")
    
        stop
    ```
   Replace `<kafka-connect-hostname-1>`, `<kafka-connect-hostname-2>`, and `<kafka-connect-hostname-3>` with your
   Kafka Connect server hostnames or IP addresses. If you have fewer Kafka nodes, remove the extra action blocks.

4. Restart the rsyslog service to apply the configuration:

   ```bash
   systemctl restart rsyslog
   ```

5. Verify that rsyslog is running:

   ```bash
   systemctl status rsyslog
   ```

## Limitations

1. The universal connector can be installed on multiple Managed Units (MUs) for high availability, but all traffic will be displayed to a single MU.

2. See the [Testing a Connection](#testing-a-connection) section for test connection limitations and expected behaviour.

## Configuring Guardium

The Guardium universal connector is the Guardium entry point for native audit and data access logs. The Guardium
universal connector identifies and parses the received events, and converts them to a standard Guardium format. The
output of the Guardium universal connector is forwarded to the Guardium sniffer on the collector for policy and auditing
enforcements. You can configure Guardium to read the native audit and data access logs by customising the Redis
Enterprise template.

### Before you begin

* Configure the policies that you need. For more information, see [Policies](/docs/#policies).
* You must have permissions for the S-Tap Management role. By default, the admin user is assigned the S-Tap Management
  role.
* Ensure that the Kafka Connect server is configured to receive logs on port 5142.

## Creating datasource profiles

You can create a new datasource profile from the **Datasource Profile Management** page.

### Procedure

1. Go to **Manage > Universal Connector > Datasource Profile Management**
2. Click the **➕ (Add)** button.
3. You can create a profile by using one of the following methods:

    * To create a new profile manually, go to the **"Add Profile"** tab and provide values for the following fields.
        * **Name** and **Description**.
        * Select a **Plug-in Type** from the dropdown. For example, **Redis Enterprise Over Syslog Connect 2.0**.

    * To upload from CSV, go to the **Upload from CSV** tab and upload an exported or manually created CSV file
      containing one or more profiles. You can also choose from the following options:
        * **Update existing profiles on name match** — Updates profiles with the same name if they already exist.
        * **Test connection for imported profiles** — Automatically tests connections after profiles are created.
        * **Use ELB** — Enables ELB support for imported profiles. You must provide the number of MUs to be used in the
          ELB process.

**Note:** Configuration options vary based on the selected plug-in.

## Configuration: Kafka Connect-based Plugins

The following table describes the fields that are specific to Kafka Connect and similar plugins.

| Field                              | Description                                                                                                       |
|------------------------------------|-------------------------------------------------------------------------------------------------------------------|
| **Name**                           | Unique name of the profile.                                                                                       |
| **Description**                    | Description of the profile.                                                                                       |
| **Plug-in**                        | Plug-in type for this profile. A full list of available plug-ins is available on the **Package Management** page. |
| **Syslog Credentials**             | Select or create Syslog Credentials. The credential type must be **Syslog Credentials** with a username field (the username can be any value as it is not used for authentication). |
| **Kafka Cluster**                  | Kafka cluster to deploy the universal connector.                                                                  |
| **Label**                          | Grouping label (e.g., **customer name** or **ID**).                                                               |
| **Syslog Port**                    | Port number for rsyslog forwarding (default: 6514).                                                               |
|Syslog listener                     | Default: TCP |
| Database hostname                  | Database hostname |
| **No traffic threshold (minutes)** | The time period after which the system detects inactivity.                                                        |

## Testing a Connection

After creating a profile, you must test the connection to ensure that the provided configuration is valid.

**Note:**
- Only one syslog profile can use a specific port at a time across all datasource profiles in your Guardium environment. If multiple syslog profiles are configured to use the same port, connection conflicts occur.
- You must test the connection immediately before you deploy the profile. The test connection validates that the port is available.
- If you test a connection and then wait before deployment, another syslog profile might claim the port and cause the deployment to fail.
- If a test connection is successful and the profile is deployed, other profiles using the same port will also succeed in testing unless the port is actually occupied by the deployed profile.
- Test connection will fail for a profile that is already deployed. When a profile is deployed, it occupies the port defined in its configuration. Since the port is already in use, any subsequent connection test will fail with a port conflict error.

### Procedure

1. Select the new profile.
2. From the top menu, click **Test Connection**.
3. If the test is successful, you can proceed to installing the profile.

---

## Installing a Profile

Once the connection test is successful, you can install the profile on **Managed Units (MUs)** or **Edges**. The parsed
audit logs are sent to the selected Managed Unit or Edge to be consumed by the Sniffer.

### Procedure

1. Select the profile.
2. From the **Manage** menu, click **Install**.
3. From the list of available MUs and Edges that is displayed, select the ones that you want to deploy the profile to.

---

## Uninstalling or reinstalling profiles

An installed profile can be uninstalled or reinstalled if needed.

### Procedure

1. Select the profile.
2. From the list of available actions, select the desired option: **Uninstall** or **Reinstall**.

---


## Troubleshooting

#### Logs are not being forwarded

1. Verify that rsyslog is running:
   ```bash
    systemctl status rsyslog
   ```

2. Check rsyslog configuration for syntax errors:
   ```bash
   rsyslogd -N1
   ```

3. Verify that the Redis Enterprise audit log file exists and has correct permissions:
   ```bash
   ls -la /var/log/redis_audit.log
   ```

#### Redis Enterprise audit logs are not being generated

1. Verify that audit logging is enabled:
   ```bash
   rladmin info cluster | grep audit_log
   ```
   
2. Check the Redis Enterprise log for errors:
   ```bash
   sudo tail -f /var/log/redis_audit.log
   ```

#### Connection test fails

1. Verify that the Kafka Connect server is listening on the specified port(depends on how configured in rladmin cluster config auditing):
   ```bash
   netstat -tuln | grep <port number>
   ```
