# CockroachDB-Guardium Logstash filter plug-in

### Meet CockroachDB
* Tested versions: 23.2.x, 25.4.1
* Environment: On-premise, IaaS
* Supported inputs: Syslog (push)
* Supported Guardium versions:
	* Guardium Data Protection: 12.0 and above

This is a [Logstash](https://github.com/elastic/logstash) filter plug-in for the universal connector that is featured in IBM Security Guardium. It parses events and messages from the CockroachDB audit log into a [Guardium record](https://github.com/IBM/universal-connectors/blob/main/common/src/main/java/com/ibm/guardium/universalconnector/commons/structures/Record.java) instance (which is a standard structure made out of several parts). The information is then sent over to Guardium. Guardium records include the accessor (the person who tried to access the data), the session, data, and exceptions. If there are no errors, the data contains details about the query "construct". The construct details the main action (verb) and collections (objects) involved.

The plug-in is free and open-source (Apache 2.0). It can be used as a starting point to develop additional filter plug-ins for Guardium universal connector.

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
	systemctl daemon-reload
	systemctl restart cockroachdb
	```

9. Verify the service started successfully:
	```bash
	systemctl status cockroachdb
	```

10. Verify the structured JSON logs are being created:
	```bash
	ls -la /path/to/logs/directory/
	```
	
### Important Notes:
- The `file-defaults` section must be at the top level of the YAML file, not nested inside sinks
- You cannot use both `--log-dir` and `--log-config-file` flags together
- The log directory is specified in the YAML configuration under each sink's `dir` field
- Use the [CockroachDBOverSyslog.conf](CockroachDBOverSyslogPackage/CockroachDBOverSyslog.conf) configuration file with structured JSON logging

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
  - Examples: Login attempts (failure)

- **cockroach-sql-schema.log** - Schema change operations (DDL statements)
  - EventType: DDL operations
  - Examples: CREATE TABLE, DROP TABLE, ALTER TABLE, CREATE INDEX, DROP INDEX

**Note:** All log files use structured JSON format when configured with the YAML file. Each log entry includes a `"channel"` field indicating its source (e.g., `"channel":"SQL_EXEC"`, `"channel":"SQL_SCHEMA"`), and an `"event"` object containing the audit data. For more information about logging channels and files depending on your cockroachDB version, see [CockroachDB Logging Channels](https://www.cockroachlabs.com/docs/v25.4/logging-overview#logging-channels).

## 3. Configuring Syslog to push logs to Guardium

### Procedure

To make Logstash able to process the data collected by syslog, configure available
syslog utility. The example is based on rsyslog utility available in many
versions of the Linux distributions.

#### Rsyslog installation guide:

- [Ubuntu](https://www.rsyslog.com/ubuntu-repository)
- [RHEL](https://www.rsyslog.com/rhelcentos-rpms)

1. Install Rsyslog on the CockroachDB server if not already installed:

   ```bash
   # For Ubuntu/Debian
   sudo apt-get install rsyslog

   # For RHEL/CentOS
   sudo yum install rsyslog
   ```

2. To check the service is active and running, execute the below command:

   ```bash
   systemctl status rsyslog
   ```

3. Generate Certificate Authority (CA):
   - **Guardium Data Protection** <br/>
     To obtain the Certificate Authority content on the Collector, run the following API command:
     ```text
     grdapi generate_ssl_key_universal_connector
     ```
     This API command will display the content of the public Certificate Authority. Copy this certificate authority content to your database source and save it as a file named 'ca.pem' .

4. Create the Rsyslog configuration file `cockroachdb.conf` for CockroachDB in the following directory:

   ```bash
   vi /etc/rsyslog.d/cockroachdb.conf
   ```

5. This configuration reads the logs from the CockroachDB log directory path and sends
   the syslog messages to the provided host `TARGET_HOST` at the provided port `TARGET_PORT`.
   
   **Note:** You can set any port number except 5000 when using Guardium Data Protection version 12.0 or 12.1.

   The template injects the server hostname and port into each message so Guardium can identify the source server correctly. Replace `<SERVER_HOSTNAME>` with the stable hostname of your CockroachDB node and `<SERVER_PORT>` with its port (default: `26257`).
   
   Add the following configuration:

   **For TLS connection:**

   ```
    global(
       DefaultNetstreamDriverCAFile="/path/to/certs/ca.pem"
    )

   module(load="imfile" PollingInterval="10")
   $MaxMessageSize 64k

   template(name="imfile_cockroach_t" type="list") {
     constant(value="serverHostname=<SERVER_HOSTNAME> serverPort=<SERVER_PORT>")
     property(name="rawmsg")
   }

   ruleset(name="imfile_to_gdp") {
           action(type="omfwd"
           protocol="tcp"
           StreamDriver="gtls"
           StreamDriverMode="1"
           StreamDriverAuthMode="x509/certvalid"
           template="imfile_cockroach_t"
           target="<TARGET_HOST>"
           port="<TARGET_PORT>")
   }

   # Monitor CockroachDB SQL query execution logs
   input(type="imfile"
         File="/path/to/logs/directory/cockroach-sql-exec.log"
         Tag="cockroachdb-sql-exec"
         Ruleset="imfile_to_gdp")

   # Monitor CockroachDB admin/sensitive access logs
   # For v23: cockroach-sensitive-access.log
   # For v25: cockroach-sql-audit.log
   input(type="imfile"
         File="/path/to/logs/directory/cockroach-sensitive-access.log"
         Tag="cockroachdb-sensitive-access"
         Ruleset="imfile_to_gdp")

   # Monitor CockroachDB schema change logs
   input(type="imfile"
         File="/path/to/logs/directory/cockroach-sql-schema.log"
         Tag="cockroachdb-sql-schema"
         Ruleset="imfile_to_gdp")

   # Monitor CockroachDB authentication/session logs
   # For v23: use cockroach-sessions.log
   # For v25: use cockroach-sql-auth.log
   input(type="imfile"
         File="/path/to/logs/directory/cockroach-sessions.log"
         Tag="cockroachdb-sessions"
         Ruleset="imfile_to_gdp")
   ```

6. Restart Rsyslog service:

   ```bash
   systemctl restart rsyslog
   ```

7. Verify Rsyslog is running:
   ```bash
   systemctl status rsyslog
   ```

## 4. Limitations

- CockroachDB wraps query values in Unicode characters `‹` (U+2039) and `›` (U+203A) in audit logs (e.g., `UPDATE table SET id = ‹2› WHERE id > ‹1›`). The plugin automatically removes these characters to restore the original query format.
- CockroachDB automatically logs `SHOW database` queries (along with query executions) and are sent to Guardium.
- The plugin automatically filters out the following system-generated queries and are not sent to Guardium:
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
- The following fields are not found in CockroachDB audit logs (applies to queries and failed logins):
    - Database Name
    - Service Name
    - Client Host Name
    - Server IP - it is set to default value `0.0.0.0`
    - Server Port - set via `serverPort=<SERVER_PORT>` in the rsyslog template (see section 3)
    - Source Program (might be missing in some audit logs)
- For failed login attempts, CockroachDB returns different errors depending on the failure type (has duplicate events):
  - When the username does not exist: `USER_NOT_FOUND` error
  - When the username exists but the password is incorrect: `PRE_HOOK_ERROR` error
- When using `cockroach sql` CLI with password authentication, a `client_authentication_failed` event is logged before successful login. The failed event is reported to Guardium as a LOGIN_FAILED exception.
- The audit logs captures sql errors for syntactically correct queries (appears in both Full SQL report and Exception report) and does not capture syntactically incorrect queries.
- Some operations may appear in multiple log files. For example, DDL operations (CREATE, ALTER, DROP, etc.) may appear in `cockroach-sql-exec.log`, `cockroach-sensitive-access.log`, and/or `cockroach-sql-schema.log`.

## 5. Configuring the CockroachDB filter in Guardium

The Guardium universal connector is the Guardium entry point for native audit logs. The universal connector identifies and parses received events, and then converts them to a standard Guardium format. The output of the universal connector is forwarded to the Guardium sniffer on the collector, for policy and auditing enforcements. Configure Guardium to read the native audit logs by customizing the CockroachDB template.

### Before you begin

• Configure the policies to match the CockroachDB events.

• You must have permission for the S-Tap Management role. The admin user includes this role, by default.

• Download the **logstash-filter-cockroachdb_guardium_filter.zip** package from [Universal Connector release page](https://github.com/IBM/universal-connectors/releases) under Assets.

### Procedure

1. On the collector, go to **Setup** > **Tools and Views** > **Configure Universal Connector**.
2. First enable the Universal Guardium connector, if it is disabled.
3. Click **Upload File** and select the offline **logstash-filter-cockroachdb_guardium_filter.zip** plug-in. After it is uploaded, click **OK**.
4. Click the **Plus sign** to open the Connector Configuration dialog box.
5. Type a name in the **Connector name** field.
6. Update the input section to add the details from the [CockroachDBOverSyslog.conf](CockroachDBOverSyslogPackage/CockroachDBOverSyslog.conf) file input section, omitting the keyword "input{" at the beginning and its corresponding "}" at the end.
7. Update the filter section to add the details from the [CockroachDBOverSyslog.conf](CockroachDBOverSyslogPackage/CockroachDBOverSyslog.conf) file filter section, omitting the keyword "filter{" at the beginning and its corresponding "}" at the end.
8. The "type" fields should match in the input and the filter configuration section. This field should be unique for every individual connector added.
9. Click **Save**. Guardium validates the new connector and displays it in the Configure Universal Connector page.
