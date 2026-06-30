# Configuring CockroachDB datasource profiles for Kafka Connect plug-ins

Create and configure datasource profiles through Central Manager for **CockroachDB over Syslog Kafka Connect** plug-ins.

## Meet CockroachDB over Syslog Kafka Connect

* Tested versions: 23.2.x, 25.4.1
* Environment: On-premise
* Supported inputs: Kafka connect Syslog 2.0 (push)
* Supported Guardium versions:
    * Guardium Data Protection: 12.1 UC patch 5008 and later
    * Guardium Data Protection: Appliance bundle 12.2.3 or later

Kafka-connect is a framework for streaming data between Apache Kafka and other systems. This connector enables monitoring of CockroachDB audit logs through syslog.

## 1. Enabling the audit logs

### Procedure

1. Connect to your CockroachDB cluster using the SQL client.

2. Enable audit logging by running the following commands:
   ```sql
   SET CLUSTER SETTING server.auth_log.sql_connections.enabled = true;
   SET CLUSTER SETTING server.auth_log.sql_sessions.enabled = true;
   SET CLUSTER SETTING sql.log.all_statements.enabled = true;
   SET CLUSTER SETTING sql.log.admin_audit.enabled = true;
   ```

3. Verify the configuration:
   ```sql
   SHOW CLUSTER SETTING server.auth_log.sql_connections.enabled;
   SHOW CLUSTER SETTING server.auth_log.sql_sessions.enabled;
   SHOW CLUSTER SETTING sql.log.all_statements.enabled;
   SHOW CLUSTER SETTING sql.log.admin_audit.enabled;
   ```

4. Create a logging configuration file on your CockroachDB server:
   ```bash
   mkdir -p /path/to/logs/directory/scripts
   vi /path/to/logs/directory/scripts/log-config.yaml
   ```

5. Add the following YAML configuration to enable structured JSON logging for audit events:

   ```bash
   vi /path/to/logs/directory/scripts/log-config.yaml
   ```
   
	```yaml
	file-defaults:
	  format: json
	  redact: false
	  redactable: true

	sinks:
	  file-groups:
	    # In v23: cockroach-sensitive-access.log
	    # In v25: cockroach-sql-audit.log
	    sensitive-access:
	      channels: [SENSITIVE_ACCESS]
	      dir: /path/to/logs/directory/

	    sql-exec:
	      channels: [SQL_EXEC]
	      dir: /path/to/logs/directory/

	    sql-schema:
	      channels: [SQL_SCHEMA]
	      dir: /path/to/logs/directory/

	    # In v23: cockroach-sessions.log
	    # In v25: cockroach-sql-auth.log
	    sessions: 
	      channels: [SESSIONS]
	      dir: /path/to/logs/directory/
	```
   
6. Update your CockroachDB systemd service file to use the logging configuration:
   ```bash
   sudo vi /etc/systemd/system/cockroachdb.service
   ```

7. Add the `--log-config-file` flag to the ExecStart line and **remove** any `--log-dir` flag if present (these flags are incompatible):
   ```
   ExecStart=/usr/local/bin/cockroach start \
         ...
     --log-config-file=/path/to/logs/directory/log-config.yaml
   ```

8. Reload systemd and restart CockroachDB:
   ```bash
   sudo systemctl daemon-reload
   sudo systemctl restart cockroachdb
   ```

9. Verify the service started successfully:
   ```bash
   sudo systemctl status cockroachdb
   ```

10. Verify the structured JSON logs are being created:
   ```bash
   ls -la /path/to/logs/directory/
   ```
   
### Important Notes:
- The `file-defaults` section must be at the top level of the YAML file, not nested inside sinks
- You cannot use both `--log-dir` and `--log-config-file` flags together
- The log directory is specified in the YAML configuration under each sink's `dir` field

## 2. Viewing the audit logs

The audit logs are stored in the directory specified in your YAML configuration (`/path/to/logs/directory/` by default). Each file-group creates a separate log file:

- **cockroach-sensitive-access.log** (v23) or **cockroach-sql-audit.log** (v25) - Administrative and privileged operations
  - EventType: `admin_query`
  - Examples: User management, permission changes, privilege grants/revokes
  
- **cockroach-sql-exec.log** - Regular SQL query execution
  - EventType: `query_execute`
  - Examples: SELECT, INSERT, UPDATE, DELETE statements
  
- **cockroach-sessions.log** (v23) or **cockroach-sql-auth.log** (v25) - Authentication and session events
  - EventTypes: `client_authentication_failed`
  - Examples: Login attempts (success/failure), session terminations

- **cockroach-sql-schema.log** - Schema change operations (DDL statements)
  - EventType: DDL operations
  - Examples: CREATE TABLE, DROP TABLE, ALTER TABLE, CREATE INDEX, DROP INDEX

**Note:** All log files use structured JSON format when configured with the YAML file. Each log entry includes a `"channel"` field indicating its source (e.g., `"channel":"SQL_EXEC"`, `"channel":"SQL_SCHEMA"`), and an `"event"` object containing the audit data. For more information about logging channels and files depending on your cockroachDB version, see [CockroachDB Logging Channels](https://www.cockroachlabs.com/docs/v25.4/logging-overview#logging-channels).


## Configuring syslog to push logs to Kafka

Configure a syslog utility to enable Kafka to process the data collected by syslog. This example uses rsyslog, which is available in most Linux distributions.

The plug-in uses the Confluent Syslog Source Connector to receive syslog messages from rsyslog.

### Procedure

1. Install rsyslog on the CockroachDB server if its not already installed:
	```bash
	# For Ubuntu/Debian
	sudo apt-get install rsyslog
	
	# For RHEL/CentOS
	sudo yum install rsyslog
	```
   For more information about installing rsyslog, see [Ubuntu](https://www.rsyslog.com/ubuntu-repository) or [RHEL](https://www.rsyslog.com/rhelcentos-rpms).


2. To verify that the service is active and running, run the following command:
    ```bash
    systemctl status rsyslog
    ```

3. Create the rsyslog configuration file `cockroachdb-kafka.conf`:
	```bash
	vi /etc/rsyslog.d/cockroachdb-kafka.conf
	```

4. Add the configuration to read logs from the CockroachDB log directory and send syslog messages to the Kafka broker.
   
   **For a TLS connection, add the following configuration:**
    
   You may need to restart the Kafka cluster to create the required UC SSL certificates on Kafka nodes.
   ```
    module(load="imfile" PollingInterval="10")
    $MaxMessageSize 64k
 
   ruleset(name="imfile_to_gdp") {
       action(type="omfwd"
           Protocol="tcp"
           StreamDriver="gtls"
           StreamDriverMode="1"
           StreamDriverAuthMode="anon"
           Template="RSYSLOG_SyslogProtocol23Format"
           Target=["<KAFKA_BROKER_1>", "<KAFKA_BROKER_2>", "<KAFKA_BROKER_3>", ...]
           Port="<TARGET_PORT>"
   
           # High traffic configuration (required for >11,400 eps per CockroachDB)
           # These settings prevent data drops at extremely high traffic volumes
           action.resumeRetryCount="-1"
           action.resumeInterval="10"
           queue.type="LinkedList"
           queue.size="100000"
           queue.dequeueBatchSize="2000"
           queue.workerThreads="4"
           queue.timeoutEnqueue="0"
           queue.saveOnShutdown="on"
   
           # Persistent queue configuration (optional)
           # Enables data persistence in case of Kafka Connect issues
           # Note: May result in slightly reduced throughput at high traffic (latency at high traffic)
           queue.fileName="cockroachdb_queue"
           queue.maxDiskSpace="2g"
           queue.highWatermark="80000"
           queue.lowWatermark="40000"
           queue.discardMark="95000"
           queue.discardSeverity="7")
   }

   # Monitor CockroachDB SQL query execution logs
   input(type="imfile"
         File="/path/to/logs/directory/cockroach-sql-exec.log"
         Tag="cockroachdb-sql-exec"
         Ruleset="imfile_to_gdp"
         reopenOnTruncate="on")

   # Monitor CockroachDB admin/sensitive access logs
   # For v23: cockroach-sensitive-access.log
   # For v25: cockroach-sql-audit.log
   input(type="imfile"
         File="/path/to/logs/directory/cockroach-sensitive-access.log"
         Tag="cockroachdb-sensitive-access"
         Ruleset="imfile_to_gdp"
         reopenOnTruncate="on")

   # Monitor CockroachDB schema change logs
   input(type="imfile"
         File="/path/to/logs/directory/cockroach-sql-schema.log"
         Tag="cockroachdb-sql-schema"
         Ruleset="imfile_to_gdp"
         reopenOnTruncate="on")

   # Monitor CockroachDB authentication/session logs
   # For v23: use cockroach-sessions.log
   # For v25: use cockroach-sql-auth.log
   input(type="imfile"
         File="/path/to/logs/directory/cockroach-sessions.log"
         Tag="cockroachdb-sessions"
         Ruleset="imfile_to_gdp"
         reopenOnTruncate="on")
   ```
   
   **Note:**
   - Replace `<KAFKA_BROKER>` with your Kafka broker hostname or IP address and `/path/to/logs/directory/` with the actual path to your CockroachDB log files.
   - Use the high traffic configuration parameters for handling extremely high traffic (more than 11,400 events per second per CockroachDB).
   - The persistent queue configuration is optional. It provides data persistence if Kafka Connect issues occur, but it might reduce throughput at high traffic volumes.
   - When multiple Kafka brokers are configured, rsyslog connects to only one broker for the source connector. Connection errors may appear in the rsyslog service status for the other Kafka nodes, which is expected behavior.

5. Restart the rsyslog service:
 ```bash
 systemctl restart rsyslog
 ```

6. Verify that rsyslog is running:
 ```bash
 systemctl status rsyslog
 ```

## Limitations

1. CockroachDB wraps query values in Unicode characters `‹` (U+2039) and `›` (U+203A) in audit logs (for example, `UPDATE table SET id = ‹2› WHERE id > ‹1›`). 
   The plug-in automatically removes these characters to restore the original query format.

2. CockroachDB automatically logs `SHOW database` queries (along with query executions), which are sent to Guardium.

3. The plug-in automatically filters out the following system-generated queries, which are not sent to Guardium:

  - Internal execution queries (`intExec=`)
  - Automatic job queries (`job=AUTO`)
  - User: "node" (internal CockroachDB operations)
  - Application Name starting with "$ internal"
  - ExecMode: "exec-internal"
  - Queries against system schemas:
    - `crdb_internal.*`
    - `pg_catalog.*`
    - `information_schema.*`
    - System tables: `system.jobs`, `system.lease`, `system.sql_instances`, `system.job_info`, `system.statement_statistics`, `system.transaction_statistics`, `system.job_progress_history`, `system.reports_meta`

4. The following fields are not found in CockroachDB audit logs (applies to queries and failed logins):

    - Database Name
    - Service Name
    - Client Host Name
    - Server Port and Server IP (set to default value `0.0.0.0`)
    - Source Program (might be missing in some audit logs)

5. For failed login attempts, CockroachDB returns different errors depending on the failure type (has duplicate events):
    - `USER_NOT_FOUND` error - When the username does not exist.
    - `PRE_HOOK_ERROR` error - When the username exists but the password is incorrect.

6. When using `cockroach sql` CLI with password authentication, a `client_authentication_failed` event is logged before successful login. The failed event is reported to Guardium as a LOGIN_FAILED exception.

7. The audit logs capture SQL errors for syntactically correct queries (which appear in both the Full SQL report and the Exception report) but do not capture syntactically incorrect queries.

8. Some operations may appear in multiple log files. For example, DDL operations (CREATE, ALTER, DROP, etc.) may appear in `cockroach-sql-exec.log`, `cockroach-sensitive-access.log`, and/or `cockroach-sql-schema.log`.

9. The universal connector can be installed on multiple Managed Units (MUs) for high availability, but all traffic will be displayed to a single MU.

10. See the [Testing a Connection](#testing-a-connection) section for test connection limitations and expected behaviour.

## Creating datasource profiles

You can create a new datasource profile from the **Datasource Profile Management** page.

### Procedure

1. Go to **Manage > Universal Connector > Datasource Profile Management**
2. Click the **➕ (Add)** button.
3. You can create a profile by using one of the following methods:

    - To **Create a new profile manually**, go to the **"Add Profile"** tab and provide values for the following fields.
        - **Name** and **Description**.
        - Select a **Plug-in Type** from the dropdown. For example, `CockroachDB Over Syslog Connect 2.0`.

    - To **Upload from CSV**, go to the **"Upload from CSV"** tab and upload an exported or manually created CSV file containing one or more profiles.  
      You can also choose from the following options:
        - **Update existing profiles on name match** — Updates profiles with the same name if they already exist.
        - **Test connection for imported profiles** — Automatically tests connections after profiles are created.
        - **Use ELB** — Enables ELB support for imported profiles. You must provide the number of MUs to be used in the ELB process.

**Note:** Configuration options vary based on the selected plug-in.

## Configuring CockroachDB Over Syslog Connect 2.0

The following table describes the fields that are specific to CockroachDB Over Syslog Connect 2.0 plugin.

| Field                                   | Description                                                                                                                                                                                                                                                                  |
|-----------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Name**                                | Unique name of the profile.                                                                                                                                                                                                                                                  |
| **Description**                         | Description of the profile.                                                                                                                                                                                                                                                  |
| **Plug-in**                             | Plug-in type for this profile. Select `CockroachDB Over Syslog Connect 2.0`. A full list of available plug-ins is available on the **Package Management** page.                                                                                                              |
| **Syslog Credentials**                  | Select or create Syslog Credentials. The credential type must be **Syslog Credentials** with a username field (the username can be any value as it is not used for authentication). |
| **Kafka Cluster**                       | Select the appropriate Kafka cluster from the available Kafka cluster list or create a new Kafka cluster. For more information, see [Managing Kafka clusters](https://www.ibm.com/docs/en/SSMPHH_12.x/com.ibm.guardium.doc.stap/guc/guc_kafka_cluster_management.html).      |
| **Label**                               | Grouping label. For example, customer name or ID.                                                                                                                                                                                                                            |
| **Syslog port**                         | The port number of the CockroachDB server. Default is `6514`.                                                                                                                                                                                                                |
| **Syslog listener (TCP or TCPSSL)**     | Network listener type used to receive CockroachDB syslog events. Select **TCP** for plain syslog over TCP. Select **TCPSSL** only when the syslog sender is configured to use TLS/SSL with the required certificates. The listener type must match the sender configuration. |
| **Database hostname**                   | The hostname of the CockroachDB server.                                                                                                                                                                                                                                      |
| **No traffic threshold (minutes)**      | Default value is 60. If there is no incoming traffic for an hour, S-TAP displays a red status. When incoming traffic resumes, the status returns to green.                                                                                                                   |
| **Use Enterprise Load Balancing (ELB)** | Enable this if ELB support is required.                                                                                                                                                                                                                                      |

**Note:**

- Ensure that the **profile name** is unique.
- The Kafka cluster must be configured and accessible before creating the profile.
- Ensure that the Kafka topic exists and rsyslog is configured to send logs to it.
---

## Testing a Connection

After you create a profile, test the connection to ensure that the configuration is valid.

**Note:**
- Only one syslog profile can use a specific port at a time across all datasource profiles in your Guardium environment. If multiple syslog profiles are configured to use the same port, connection conflicts occur.
- You must test the connection immediately before you deploy the profile. The test connection validates that the port is available.
- If you test a connection and then wait before deployment, another syslog profile might claim the port and cause the deployment to fail.
- If a test connection is successful and the profile is deployed, other profiles using the same port will also succeed in testing unless the port is actually occupied by the deployed profile.
- Test connection will fail for a profile that is already deployed. When a profile is deployed, it occupies the port defined in its configuration. Since the port is already in use, any subsequent connection test will fail with a port conflict error.

### Procedure

1. Select the new profile.
2. From the top menu, click **Test Connection**.
3. If the test is successful, proceed immediately to installing the profile.
4. If the test fails, verify the following items:
   - The port is not already in use by another profile.
   - The Kafka cluster is accessible.
   - Network connectivity exists between the data source and Kafka broker.

---

## Installing a Profile

Once the connection test is successful, you can install the profile on **Managed Units (MUs)** or **Edges**. The parsed audit logs are sent to the selected Managed Unit or Edge to be consumed by the **Sniffer**.

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

3. Verify that the log files exist and have correct permissions:
   ```bash
   ls -la /path/to/logs/directory/cockroach-sql-exec.log
   ls -la /path/to/logs/directory/cockroach-sql-auth.log
   ls -la /path/to/logs/directory/cockroach-sql-schema.log
   ```

4. Check the rsyslog logs for errors:
   ```bash
   sudo tail -f /var/log/syslog | grep rsyslog
   ```

### CockroachDB logs are not being generated

1. Verify that audit logging is enabled:
   ```sql
   SHOW CLUSTER SETTING server.auth_log.sql_connections.enabled;
   SHOW CLUSTER SETTING server.auth_log.sql_sessions.enabled;
   SHOW CLUSTER SETTING sql.log.all_statements.enabled;
   SHOW CLUSTER SETTING sql.log.admin_audit.enabled;
   ```

2. Check the CockroachDB logs for errors:
   ```bash
   tail -f /path/to/logs/directory/cockroach-sql-exec.log
   ```

3. Ensure that the log directory exists and has correct permissions:
   ```bash
   sudo mkdir -p /path/to/logs/directory
   sudo chown cockroach:cockroach /path/to/logs/directory
   sudo chmod 755 /path/to/logs/directory
   ```

### Connection test fails

1. Verify that the Kafka Connect server is listening on the specified port:
   ```bash
   netstat -tuln | grep <PORT>
   ```

2. Check the network connectivity between the CockroachDB server and Kafka Connect:
   ```bash
   telnet <KAFKA_BROKER> <PORT>
   ```

3. Verify that rsyslog is configured with the correct Kafka broker addresses and port.
---
