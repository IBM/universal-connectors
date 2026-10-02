# Configuring Neo4j datasource profiles for Kafka Connect plug-ins

Create and configure datasource profiles through Central Manager for **Neo4j over Syslog Kafka Connect** plug-ins.

## Meet Neo4j over Syslog Connect

* **Tested versions:** 5.26 (Enterprise Edition)
* **Environment:** On-premises
* **Supported inputs:** Kafka Connect Syslog 2.0 (push)
* **Supported Guardium versions:**
    * Guardium Data Protection: Appliance bundle 12.2.4 or later

Kafka Connect is a framework for streaming data between Apache Kafka and other systems. This connector enables monitoring of Neo4j query, security, and HTTP logs through syslog.

**Note:** Neo4j query logs are only available in the **Enterprise Edition**. The Community Edition does not expose the query log, so query-level visibility in Guardium requires a Neo4j Enterprise license.

## 1. Enabling query logs

### Procedure

1. Edit the Neo4j configuration file at `/etc/neo4j/neo4j.conf`.

2. Add or confirm the following settings. These use the Neo4j 5.x syntax — the older `dbms.logs.query.*` prefix is not valid in Neo4j 5.x:

   ```ini
   # Enable query logging (Neo4j 5.x syntax)
   db.logs.query.enabled=INFO
   db.logs.query.threshold=0
   db.logs.query.parameter_logging_enabled=true
   db.logs.query.time_logging_enabled=true
   db.logs.query.allocation_logging_enabled=true
   db.logs.query.page_logging_enabled=true
   ```

3. Restart Neo4j to apply the changes:

   ```bash
   systemctl restart neo4j
   ```

4. Confirm that query log entries are appearing:

   ```bash
   tail -f /opt/neo4j/logs/query.log
   ```

## 2. Viewing the audit logs

Neo4j writes log events to separate files under `/opt/neo4j/logs/`. The key files are:

| Log file | Description |
|---|---|
| `query.log` | Records every Cypher query that completes, including the query text, execution time, database, and user. Requires Enterprise Edition with `db.logs.query.enabled=INFO`. |
| `security.log` | Records authentication and authorisation events such as login successes, login failures, and role changes. |
| `http.log` | Records HTTP API requests to the Neo4j browser and Bolt endpoints. |

Each `query.log` entry is a single line containing space-separated fields that include the timestamp, elapsed time, planning time, CPU time, waiting time, allocated bytes, page hits, page faults, query source, username, database, transaction id, query text, and parameters.

## Configuring syslog to push logs to Kafka

Configure rsyslog to tail the Neo4j log files and forward them to the Kafka Connect broker. This example uses rsyslog, which is available in most Linux distributions.

The plug-in uses the Confluent Syslog Source Connector to receive syslog messages from rsyslog.

### Procedure

1. Install rsyslog on the Neo4j server if it is not already installed:

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
   vi /etc/rsyslog.d/neo4j.conf
   ```

4. Add the following configuration. Replace the `<UC_NODE_*>` placeholders with the addresses of your Kafka broker nodes (for UC 2.0) or your MU/Collector node (for UC 1.0):

   ```
   # Neo4j query.log
   input(type="imfile"
         File="/opt/neo4j/logs/query.log"
         Tag="neo4j-query"
         Severity="info"
         Facility="local0"
         reopenOnTruncate="on"
         startmsg.regex="^[0-9]{4}-[0-9]{2}-[0-9]{2} [0-9]{2}:[0-9]{2}:[0-9]{2}")

   # Neo4j security.log
   input(type="imfile"
         File="/opt/neo4j/logs/security.log"
         Tag="neo4j-security"
         Severity="warning"
         Facility="local2"
         reopenOnTruncate="on"
         startmsg.regex="^[0-9]{4}-[0-9]{2}-[0-9]{2} [0-9]{2}:[0-9]{2}:[0-9]{2}")

   # Neo4j http.log
   input(type="imfile"
         File="/opt/neo4j/logs/http.log"
         Tag="neo4j-http"
         Severity="info"
         Facility="local3"
         reopenOnTruncate="on"
         startmsg.regex="^[0-9]{4}-[0-9]{2}-[0-9]{2} [0-9]{2}:[0-9]{2}:[0-9]{2}")

   # Forward Neo4j logs to UC nodes
   if $programname contains 'neo4j' then {
       action(type="omfwd"
              target="<UC_NODE_1>"
              port="5000"
              protocol="tcp"
              template="RSYSLOG_SyslogProtocol23Format"
              action.resumeRetryCount="-1"
              queue.type="LinkedList"
              queue.size="10000")
       action(type="omfwd"
              target="<UC_NODE_2>"
              port="5000"
              protocol="tcp"
              template="RSYSLOG_SyslogProtocol23Format"
              action.resumeRetryCount="-1"
              queue.type="LinkedList"
              queue.size="10000")
       action(type="omfwd"
              target="<UC_NODE_3>"
              port="5000"
              protocol="tcp"
              template="RSYSLOG_SyslogProtocol23Format"
              action.resumeRetryCount="-1"
              queue.type="LinkedList"
              queue.size="10000")
   }
   ```

   **Notes:**
   - For **UC 2.0 (Kafka Connect)**, set the `target` values to your Kafka broker node addresses.
   - For **UC 1.0**, set the `target` to your MU or Collector address.
   - Add or remove `action(type="omfwd" ...)` blocks to match the number of nodes in your environment.
   - The `startmsg.regex` patterns match the ISO-format timestamp prefix on each log line, enabling rsyslog to correctly reassemble multi-line entries.

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

1. Neo4j query logs require **Enterprise Edition**. Community Edition does not expose the query log.
2. See the [Testing a Connection](#testing-a-connection) section for test connection limitations and expected behavior.

## Creating datasource profiles

You can create a new datasource profile from the **Datasource Profile Management** page.

### Procedure

1. Go to **Manage > Universal Connector > Datasource Profile Management**.
2. Click the **➕ (Add)** button.
3. You can create a profile by using one of the following methods:

    - To **create a new profile manually**, go to the **Add Profile** tab and provide values for the following fields:
        - **Name** and **Description**.
        - Select a **Plug-in Type** from the dropdown. For example, `Neo4j Over Syslog Connect 2.0`.

    - To **upload from CSV**, go to the **Upload from CSV** tab and upload an exported or manually created CSV file containing one or more profiles. You can also choose from the following options:
        - **Update existing profiles on name match** — Updates profiles with the same name if they already exist.
        - **Test connection for imported profiles** — Automatically tests connections after profiles are created.
        - **Use ELB** — Enables ELB support for imported profiles. You must provide the number of MUs to be used in the ELB process.

**Note:** Configuration options vary based on the selected plug-in.

## Configuring Neo4j Over Syslog Connect 2.0

The following table describes the fields that are specific to the Neo4j Over Syslog Connect 2.0 plug-in.

| Field | Description |
|---|---|
| **Name** | Unique name of the profile. |
| **Description** | Description of the profile. |
| **Plug-in** | Plug-in type for this profile. Select `Neo4j Over Syslog Connect 2.0`. A full list of available plug-ins is available on the **Package Management** page. |
| **Syslog Credentials** | Select or create Syslog Credentials. The credential type must be **Syslog Credentials** with a username field (the username can be any value as it is not used for authentication). |
| **Kafka Cluster** | Select the appropriate Kafka cluster from the available Kafka cluster list or create a new Kafka cluster. For more information, see [Managing Kafka clusters](https://www.ibm.com/docs/en/SSMPHH_12.x/com.ibm.guardium.doc.stap/guc/guc_kafka_cluster_management.html). |
| **Label** | Grouping label. For example, customer name or ID. |
| **Syslog port** | The port number on which Kafka Connect listens for incoming syslog messages. Default is `5000`. |
| **Syslog listener (TCP or TCPSSL)** | Network listener type used to receive Neo4j syslog events. Select **TCP** for plain syslog over TCP. Select **TCPSSL** only when the syslog sender is configured to use TLS/SSL with the required certificates. The listener type must match the sender configuration. |
| **Database hostname** | The hostname of the Neo4j server. |
| **No traffic threshold (minutes)** | Default value is 60. If there is no incoming traffic for an hour, S-TAP displays a red status. When incoming traffic resumes, the status returns to green. |
| **Use Enterprise Load Balancing (ELB)** | Enable this if ELB support is required. |

**Notes:**
- Ensure that the **profile name** is unique.
- The Kafka cluster must be configured and accessible before creating the profile.
- Ensure that rsyslog is configured to send logs to the correct Kafka broker and port.

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
   - Network connectivity exists between the Neo4j server and the Kafka broker.

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

---

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

3. Verify that the Neo4j log files exist and are being written to:

   ```bash
   tail -f /opt/neo4j/logs/query.log
   tail -f /opt/neo4j/logs/security.log
   ```

4. Check the rsyslog logs for errors:

   ```bash
   sudo journalctl -u rsyslog -f
   ```

### Neo4j query logs are not being generated

1. Confirm that you are running Neo4j Enterprise Edition:

   ```bash
   neo4j --version
   ```

2. Verify that query logging is enabled in `/etc/neo4j/neo4j.conf`:

   ```bash
   grep db.logs.query /etc/neo4j/neo4j.conf
   ```

3. Confirm that `db.logs.query.enabled` is set to `INFO` and that `db.logs.query.threshold` is set to `0` (log all queries regardless of duration).

4. After editing the configuration, restart Neo4j and confirm entries appear in the query log:

   ```bash
   systemctl restart neo4j
   tail -f /opt/neo4j/logs/query.log
   ```

### Connection test fails

1. Verify that the Kafka Connect server is listening on the specified port:

   ```bash
   netstat -tuln | grep <PORT>
   ```

2. Check network connectivity between the Neo4j server and the Kafka Connect broker:

   ```bash
   telnet <KAFKA_BROKER> <PORT>
   ```

3. Verify that rsyslog is configured with the correct Kafka broker address and port.

---
