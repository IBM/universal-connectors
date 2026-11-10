# Configuring MySQL On-Premises datasource profiles for Kafka Connect plug-ins

Create and configure datasource profiles through Central Manager for **MySQL On-Premises over Syslog Kafka Connect** plug-ins.

## Meet MySQL On-Premises over Syslog Connect

* Tested versions: 9.0.1-commercial (MySQL Enterprise Server)
* Environment: On-premises
* Supported inputs: Kafka Connect Syslog 2.0 (push)
* Supported Guardium versions:
    * Guardium Data Protection: 12.1 UC patch 5008 and later
    * Guardium Data Protection: Appliance bundle 12.2.3 or later

Kafka Connect is a framework for streaming data between Apache Kafka and other systems. This connector enables monitoring of MySQL audit logs through syslog.

**Note:** The MySQL Enterprise Audit plugin (`audit_log.so`) is required. This plugin is only available in MySQL Enterprise Edition.

## 1. Enabling audit logs

### Procedure

1. Verify that the MySQL Enterprise Audit plugin is loaded by connecting to MySQL and running:

   ```bash
   mysql -uguardium_qa -p<password> <database>
   ```

   ```sql
   SHOW PLUGINS;
   ```

   Look for a row where the `Name` is `audit_log` and the `Status` is `ACTIVE`.

2. Edit the MySQL configuration file at `/etc/my.cnf` (or `/etc/mysql/my.cnf`) and add or confirm the following settings in the `[mysqld]` section:

   ```ini
   [mysqld]
   # MySQL Enterprise Audit plugin
   plugin-load-add        = audit_log.so
   audit_log_file         = /var/log/mysql/audit.log
   audit_log_format       = NEW
   audit_log_policy       = ALL
   audit_log_rotate_on_size = 0
   ```

3. Restart MySQL to apply the changes:

   ```bash
   systemctl restart mysqld
   ```

4. Verify that the audit log file is being written to:

   ```bash
   tail -f /var/log/mysql/audit.log
   ```

5. Confirm the audit log file path from inside MySQL:

   ```sql
   SHOW VARIABLES LIKE 'audit_log_file';
   ```

## 2. Viewing the audit logs

MySQL Enterprise Audit logs in `NEW` format are written as XML to the file specified by `audit_log_file` (default: `/var/log/mysql/audit.log`). Each `<AUDIT_RECORD>` element contains fields including:

| Field | Description |
|-------|-------------|
| `TIMESTAMP` | Time the event occurred |
| `COMMAND_CLASS` | Type of SQL command (for example, `select`, `insert`, `update`) |
| `CONNECTION_ID` | Client connection identifier |
| `STATUS` | Result code (0 = success) |
| `USER` | MySQL user |
| `HOST` | Client hostname |
| `OS_LOGIN` | OS-level login name |
| `SQLTEXT` | The SQL statement that was executed |

For the full audit log field reference, see the [MySQL Enterprise Audit documentation](https://dev.mysql.com/doc/refman/8.0/en/audit-log-file-formats.html).

## Configuring syslog to push logs to Kafka

Configure rsyslog to tail the MySQL audit log and forward it to the Kafka Connect broker. This example uses rsyslog, which is available in most Linux distributions.

The plug-in uses the Confluent Syslog Source Connector to receive syslog messages from rsyslog.

### Before you begin

The rsyslog template **must** include `serverHostname=` and `serverPort=` values in the message. These values are used by the connector to:
- Route each message to the correct per-source Kafka topic (`serverHostname=` must match the **Database hostname** field in the datasource profile)
- Populate the server hostname and port fields in Guardium records

If either value is missing, messages are dropped and do not appear in Guardium.

### Procedure

1. Install rsyslog on the MySQL server if it is not already installed:

   ```bash
   # For Ubuntu/Debian
   sudo apt-get install rsyslog

   # For RHEL/CentOS
   sudo yum install rsyslog
   ```

   For more information about installing rsyslog, see [Ubuntu](https://www.rsyslog.com/ubuntu-repository) or [RHEL](https://www.rsyslog.com/rhelcentos-rpms).

2. Verify that the service is active and running:

   ```bash
   systemctl status rsyslog
   ```

3. Create the rsyslog configuration file:

   ```bash
   vi /etc/rsyslog.d/mysql-kafka.conf
   ```

4. Add the following configuration. Replace the placeholders with values for your environment:

   ```
   module(load="imfile")

   # UcMessageFormat includes standard syslog header fields followed by
   # serverHostname and serverPort literals for Guardium accessor identification,
   # then %rawmsg% = the raw MySQL audit log line.
   template(name="UcMessageFormat" type="string"
       string="<%PRI%>%TIMESTAMP:::date-rfc3164% %HOSTNAME% %APP-NAME%[%PROCID%]: serverHostname=<SERVER_HOSTNAME> serverPort=<SERVER_PORT> %rawmsg%\n"
   )

   # Tail the MySQL audit log and assign syslog facility local6
   input(
       type="imfile"
       file="/var/log/mysql/audit.log"
       tag="mysql-audit"
       facility="local6"
       ruleset="mysql_to_uc"
       startmsg.regex="^<"
   )

   ruleset(name="mysql_to_uc") {
       local6.* action(
           type="omfwd"
           protocol="tcp"
           target="<KAFKA_BROKER_1>"
           port="<TARGET_PORT>"
           template="UcMessageFormat"
           queue.type="LinkedList"
           queue.size="10000"
           queue.saveOnShutdown="on"
           action.resumeRetryCount="-1"
       )
       # Add additional action blocks for each Kafka broker node
   }
   ```

   **Notes:**
   - Replace `<SERVER_HOSTNAME>` with the MySQL server hostname as it should appear in Guardium.
   - Replace `<SERVER_PORT>` with the MySQL listen port (default: `3306`).
   - Replace `<KAFKA_BROKER_1>` and `<TARGET_PORT>` with your Kafka broker hostname and port.
   - The `startmsg.regex="^<"` matches the opening `<` of each XML `<AUDIT_RECORD>` element in `NEW` format audit logs.
   - The `facility="local6"` assignment and `local6.*` filter ensure only MySQL audit messages are forwarded.
   - When multiple Kafka brokers are configured, rsyslog connects to only one broker for the source connector. Connection errors may appear in the rsyslog service status for the other Kafka nodes, which is expected behavior.

5. Restart the rsyslog service:

   ```bash
   systemctl restart rsyslog
   ```

6. Verify that rsyslog is running with no errors:

   ```bash
   systemctl status rsyslog
   journalctl -u rsyslog -f
   ```

## Limitations

1. The MySQL Enterprise Audit plugin is required. MySQL Community Edition does not include this plugin.

2. The universal connector can be installed on multiple Managed Units (MUs) for high availability, 

3. See the [Testing a Connection](#testing-a-connection) section for test connection limitations and expected behavior.


## Creating datasource profiles

You can create a new datasource profile from the **Datasource Profile Management** page.

### Procedure

1. Go to **Manage > Universal Connector > Datasource Profile Management**.
2. Click the **➕ (Add)** button.
3. You can create a profile by using one of the following methods:

    - To **create a new profile manually**, go to the **Add Profile** tab and provide values for the following fields:
        - **Name** and **Description**.
        - Select a **Plug-in Type** from the dropdown. For example, `MySQL On-Premises Over Syslog Connect 2.0`.

    - To **upload from CSV**, go to the **Upload from CSV** tab and upload an exported or manually created CSV file containing one or more profiles. You can also choose from the following options:
        - **Update existing profiles on name match** — Updates profiles with the same name if they already exist.
        - **Test connection for imported profiles** — Automatically tests connections after profiles are created.
        - **Use ELB** — Enables ELB support for imported profiles. You must provide the number of MUs to be used in the ELB process.

**Note:** Configuration options vary based on the selected plug-in.

## Configuring MySQL On-Premises Over Syslog Connect 2.0

The following table describes the fields that are specific to the MySQL On-Premises Over Syslog Connect 2.0 plug-in.

| Field | Description |
|-------|-------------|
| **Name** | Unique name of the profile. |
| **Description** | Description of the profile. |
| **Plug-in** | Plug-in type for this profile. Select `MySQL On-Premises Over Syslog Connect 2.0`. A full list of available plug-ins is available on the **Package Management** page. |
| **Syslog Credentials** | Select or create Syslog Credentials. The credential type must be **Syslog Credentials** with a username field (the username can be any value as it is not used for authentication). |
| **Kafka Cluster** | Select the appropriate Kafka cluster from the available Kafka cluster list or create a new Kafka cluster. For more information, see [Managing Kafka clusters](https://www.ibm.com/docs/en/SSMPHH_12.x/com.ibm.guardium.doc.stap/guc/guc_kafka_cluster_management.html). |
| **Label** | Grouping label. For example, customer name or ID. |
| **Syslog port** | The port number on which Kafka Connect listens for incoming syslog messages. Default is `6514`. |
| **Syslog listener (TCP or TCPSSL)** | Network listener type used to receive MySQL syslog events. Select **TCP** for plain syslog over TCP. Select **TCPSSL** only when the syslog sender is configured to use TLS/SSL with the required certificates. The listener type must match the sender configuration. |
| **Database hostname** | The hostname of the MySQL server. |
| **No traffic threshold (minutes)** | Default value is 60. If there is no incoming traffic for an hour, S-TAP displays a red status. When incoming traffic resumes, the status returns to green. |
| **Use Enterprise Load Balancing (ELB)** | Enable this if ELB support is required. |

**Notes:**
- Ensure that the **profile name** is unique.
- The Kafka cluster must be configured and accessible before creating the profile.
- Ensure that the Kafka topic exists and rsyslog is configured to send logs to it.

---

## Testing a Connection

After you create a profile, test the connection to ensure that the configuration is valid.

**Note:**
- Multiple profiles sharing the same port and listener type all pass the connection test, whether or not any of them are already deployed.
- A test connection fails only if the port is in use by a **different listener type** (TCP vs TCPSSL) or by a **non-syslog process**.

### Procedure

1. Select the new profile.
2. From the top menu, click **Test Connection**.
3. If the test is successful, proceed immediately to installing the profile.
4. If the test fails, verify the following items:
   - The port is not already in use by another profile.
   - The Kafka cluster is accessible.
   - Network connectivity exists between the data source and the Kafka broker.

---

## Installing a Profile

Once the connection test is successful, you can install the profile on **Managed Units (MUs)** or **Edges**. The parsed audit logs are sent to the selected Managed Unit or Edge to be consumed by the Sniffer.

### Procedure

1. Select the profile.
2. From the **Install** menu, click **Install**.
3. From the list of available MUs and Edges, select the ones where you want to deploy the profile.

---

## Uninstalling or reinstalling profiles

You can uninstall or reinstall an installed profile.

### Procedure

1. Select the profile.
2. From the list of available actions, select **Uninstall** or **Reinstall**.

## Troubleshooting

### Logs are not being forwarded

1. Verify that rsyslog is running:

   ```bash
   sudo systemctl status rsyslog
   ```

2. Check the rsyslog configuration for syntax errors:

   ```bash
   sudo rsyslogd -N1
   ```

3. Verify that the audit log file exists and has correct permissions:

   ```bash
   ls -la /var/log/mysql/audit.log
   ```

4. Check the rsyslog logs for errors:

   ```bash
   sudo journalctl -u rsyslog -f
   ```

### MySQL audit logs are not being generated

1. Verify that the audit plugin is active:

   ```sql
   SHOW PLUGINS;
   -- Look for: audit_log | ACTIVE | AUDIT
   ```

2. Confirm the audit log file path:

   ```sql
   SHOW VARIABLES LIKE 'audit_log_file';
   SHOW VARIABLES LIKE 'audit_log_policy';
   ```

3. Ensure that the log directory exists and has correct permissions:

   ```bash
   sudo mkdir -p /var/log/mysql
   sudo chown mysql:mysql /var/log/mysql
   sudo chmod 755 /var/log/mysql
   ```

### Connection test fails

1. Verify that the Kafka Connect server is listening on the specified port:

   ```bash
   netstat -tuln | grep <PORT>
   ```

2. Check network connectivity between the MySQL server and the Kafka Connect broker:

   ```bash
   telnet <KAFKA_BROKER> <PORT>
   ```

3. Verify that rsyslog is configured with the correct Kafka broker address and port.

### SELinux blocks the syslog port

If SELinux is enforcing and logs are still not forwarded, allow the syslog port and restart rsyslog:

```bash
semanage port -a -t syslogd_port_t -p tcp <PORT>
semanage port -l | grep syslogd_port_t  # <PORT> should now be listed
systemctl restart rsyslog
```
---
