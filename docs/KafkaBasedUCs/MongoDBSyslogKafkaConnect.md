# Configuring MongoDB On-Premises datasource profiles for Kafka Connect plug-ins

Create and configure datasource profiles through Central Manager for **MongoDB On-Premises over Syslog Kafka Connect** plug-ins.

## Meet MongoDB On-Premises over Syslog Connect

* Tested versions: MongoDB v5.0.34
* Environment: On-premises
* Supported inputs: Kafka Connect Syslog 2.0 (push)
* Supported Guardium versions:
    * Guardium Data Protection: Appliance bundle 12.2 or later

Kafka Connect is a framework for streaming data between Apache Kafka and other systems. This connector enables monitoring of MongoDB audit logs through syslog.

**Note:** The MongoDB Enterprise Audit plugin is required. This plugin is only available in MongoDB Enterprise Edition.

## 1. Enabling audit logs

### Procedure

1. Edit `/etc/mongod.conf` and add or confirm the following settings in the `auditLog` section:

   ```yaml
   # /etc/mongod.conf

   systemLog:
     destination: file
     path: /var/log/mongodb/mongod.log
     logAppend: true

   auditLog:
     destination: file
     format: JSON
     path: /var/log/mongodb/auditLog.json
     filter: '{ atype: { $in: ["authenticate","authCheck","createCollection","dropCollection","insert","update","delete","find","createIndex","dropDatabase","logout"] } }'
   ```

   **Configuration parameters explained:**
   - `auditLog.destination: file` — Writes audit events to a file.
   - `auditLog.format: JSON` — Outputs each audit event as a JSON object on its own line.
   - `auditLog.path` — Path to the audit log file read by rsyslog.
   - `auditLog.filter` — Restricts which operation types are audited. Expand or restrict the `atype` list to match your auditing policy.

2. Restart MongoDB to apply the changes:

   ```bash
   systemctl restart mongod
   ```

3. Verify that audit log entries are being written:

   ```bash
   tail -f /var/log/mongodb/auditLog.json
   ```

## 2. Viewing the audit logs

MongoDB audit logs in JSON format are written one event per line to the file specified by `auditLog.path` (default: `/var/log/mongodb/auditLog.json`). Each line is a JSON object containing fields such as:

| Field | Description |
|-------|-------------|
| `atype` | The operation type (for example, `authenticate`, `find`, `insert`) |
| `ts` | Timestamp of the event |
| `local` | Server address and port |
| `remote` | Client address and port |
| `users` | Array of authenticated users |
| `roles` | Array of roles associated with the user |
| `param` | Operation-specific parameters (for example, namespace, command) |
| `result` | Result code (0 = success) |

For the full audit log field reference, see the [MongoDB Audit Log Messages documentation](https://www.mongodb.com/docs/manual/reference/audit-message/).

## Configuring syslog to push logs to Kafka

Configure rsyslog to tail the MongoDB audit log and forward it to the Kafka Connect broker. This example uses rsyslog, which is available in most Linux distributions.

The plug-in uses the Confluent Syslog Source Connector to receive syslog messages from rsyslog.

### Before you begin

The rsyslog template **must** include `serverHostname=` and `serverPort=` values in the message. These values are used by the connector to:
- Route each message to the correct per-source Kafka topic (`serverHostname=` must match the **Database hostname** field in the datasource profile)
- Populate the server hostname and port fields in Guardium records

If either value is missing, messages are dropped and do not appear in Guardium.

### Procedure

1. Install rsyslog on the MongoDB server if it is not already installed:

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
   vi /etc/rsyslog.d/mongo-audit.conf
   ```

4. Add the following configuration. Replace the placeholders with values for your environment:

   ```
   module(load="imfile" PollingInterval="1")
   $MaxMessageSize 64k

   template(name="UcMessageFormat" type="string"
       string="<%PRI%>%TIMESTAMP:::date-rfc3164% %HOSTNAME% %APP-NAME%[%PROCID%]: serverHostname=<SERVER_HOSTNAME> serverPort=<SERVER_PORT> %rawmsg%\n"
   )

   ruleset(name="forwardToGUC") {
       action(type="omfwd"
              target="<KAFKA_BROKER_1>"
              port="<TARGET_PORT>"
              protocol="tcp"
              template="UcMessageFormat"
              queue.type="LinkedList"
              queue.size="10000"
              queue.saveOnShutdown="on"
              action.resumeRetryCount="-1"
              action.resumeInterval="10")
       # Add additional action blocks for each Kafka broker node
   }

   input(type="imfile"
         File="/var/log/mongodb/auditLog.json"
         Tag="syslog-mongodb"
         Severity="info"
         Facility="local2"
         Ruleset="forwardToGUC"
         reopenOnTruncate="on")
   ```

   **Notes:**
   - Replace `<SERVER_HOSTNAME>` with the MongoDB server hostname as it should appear in Guardium. This value must exactly match the **Database hostname** field in the datasource profile.
   - Replace `<SERVER_PORT>` with the MongoDB listener port (default: `27017`).
   - Replace `<KAFKA_BROKER_1>` and `<TARGET_PORT>` with your Kafka broker hostname and port.
   - The `Tag` value `syslog-mongodb` is required. The parser uses this tag to identify MongoDB audit messages.
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

### Enabling TLS (TCPSSL)

For encrypted forwarding, the GUC connector is the TLS **server** and rsyslog is the TLS **client**.

**Prerequisites on the DB host:**

1. Install the GnuTLS driver:

   ```bash
   # RHEL / CentOS / Rocky
   dnf install rsyslog-gnutls

   # Debian / Ubuntu
   apt-get install rsyslog-gnutls
   ```

2. Export the Guardium CA certificate.

3. Copy the CA cert to the DB host.

4. Update `/etc/rsyslog.d/mongo-audit.conf` to use TLS:

   ```
   # Required for x509/certvalid — omit this line for "anon" mode
   global(DefaultNetstreamDriverCAFile="/etc/rsyslog.d/certs/guc-ca.crt")

   ruleset(name="forwardToGUC") {
       action(type="omfwd"
              target="<KAFKA_BROKER_1>"
              port="<TARGET_PORT>"
              protocol="tcp"
              StreamDriver="gtls"
              StreamDriverMode="1"
              StreamDriverAuthMode="x509/certvalid"
              template="UcMessageFormat"
              queue.type="LinkedList"
              queue.size="10000"
              queue.saveOnShutdown="on"
              action.resumeRetryCount="-1"
              action.resumeInterval="10")
   }
   ```

   **Note:**
   - The `serverHostname=` value in the template must exactly match the **Database hostname** field configured in the datasource profile. If they differ, messages are dropped.
   - Ensure `DefaultNetstreamDriver="gtls"` is **not** set in the rsyslog `global()` block.

## Limitations

1. The MongoDB Enterprise Audit plugin is required. MongoDB Community Edition does not include this plugin.

2. The universal connector can be installed on multiple Managed Units (MUs) for high availability, but all traffic will be displayed to a single MU.

3. See the [Testing a Connection](#testing-a-connection) section for test connection limitations and expected behavior.

## Creating datasource profiles

You can create a new datasource profile from the **Datasource Profile Management** page.

### Procedure

1. Go to **Manage > Universal Connector > Datasource Profile Management**.
2. Click the **➕ (Add)** button.
3. You can create a profile by using one of the following methods:

    - To **create a new profile manually**, go to the **Add Profile** tab and provide values for the following fields:
        - **Name** and **Description**.
        - Select a **Plug-in Type** from the dropdown. For example, `MongoDB Over Syslog Connect 2.0`.

    - To **upload from CSV**, go to the **Upload from CSV** tab and upload an exported or manually created CSV file containing one or more profiles. You can also choose from the following options:
        - **Update existing profiles on name match** — Updates profiles with the same name if they already exist.
        - **Test connection for imported profiles** — Automatically tests connections after profiles are created.
        - **Use ELB** — Enables ELB support for imported profiles. You must provide the number of MUs to be used in the ELB process.

**Note:** Configuration options vary based on the selected plug-in.

## Configuring MongoDB Over Syslog Connect 2.0

The following table describes the fields that are specific to the MongoDB Over Syslog Connect 2.0 plug-in.

| Field | Description |
|-------|-------------|
| **Name** | Unique name of the profile. |
| **Description** | Description of the profile. |
| **Plug-in** | Plug-in type for this profile. Select `MongoDB Over Syslog Connect 2.0`. A full list of available plug-ins is available on the **Package Management** page. |
| **Syslog Credentials** | Select or create Syslog Credentials. The credential type must be **Syslog Credentials** with a username field (the username can be any value as it is not used for authentication). |
| **Kafka Cluster** | Select the appropriate Kafka cluster from the available Kafka cluster list or create a new Kafka cluster. For more information, see [Managing Kafka clusters](https://www.ibm.com/docs/en/SSMPHH_12.x/com.ibm.guardium.doc.stap/guc/guc_kafka_cluster_management.html). |
| **Label** | Grouping label. For example, customer name or ID. |
| **Syslog port** | The port number on which Kafka Connect listens for incoming syslog messages. Default is `6514`. |
| **Syslog listener (TCP or TCPSSL)** | Network listener type used to receive MongoDB syslog events. Select **TCP** for plain syslog over TCP. Select **TCPSSL** only when the syslog sender is configured to use TLS/SSL with the required certificates. The listener type must match the sender configuration. |
| **Database hostname** | The hostname of the MongoDB server. Must match the `serverHostname=` value in the rsyslog template. |
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
   ls -la /var/log/mongodb/auditLog.json
   ```

4. Check the rsyslog logs for errors:

   ```bash
   sudo journalctl -u rsyslog -f
   ```

### MongoDB audit logs are not being generated

1. Verify that audit logging is active:

   ```bash
   tail -f /var/log/mongodb/auditLog.json
   ```

2. Confirm that the `auditLog` section is present and correct in `/etc/mongod.conf`:

   ```bash
   grep -A5 'auditLog' /etc/mongod.conf
   ```

3. Ensure that the log directory exists and has correct permissions:

   ```bash
   sudo mkdir -p /var/log/mongodb
   sudo chown mongod:mongod /var/log/mongodb
   sudo chmod 755 /var/log/mongodb
   ```

### No events appear in Guardium

1. Verify that `serverHostname=` in the rsyslog template exactly matches the **Database hostname** field in the datasource profile.

2. Verify that the `Tag` in the rsyslog `imfile` input block is exactly `syslog-mongodb`.

### Connection test fails

1. Verify that the Kafka Connect server is listening on the configured port:

   ```bash
   netstat -tuln | grep <PORT>
   ```

2. Check network connectivity between the MongoDB server and the Kafka Connect broker:

   ```bash
   telnet <KAFKA_BROKER> <PORT>
   ```

3. Verify that rsyslog is configured with the correct Kafka broker address and port.

### TLS handshake fails

1. Verify the CA cert file is present and readable:

   ```bash
   ls -la /etc/rsyslog.d/certs/guc-ca.crt
   ```

2. Re-export and copy the CA cert as described in the TLS prerequisites above.

3. Ensure `DefaultNetstreamDriver="gtls"` is **not** set in the rsyslog `global()` block.

---
