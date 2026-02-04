## CockroachDB-Guardium Logstash filter plug-in

### Meet CockroachDB
* Tested versions: 25.4.1
* Environment: On-premise, IaaS
* Supported inputs: Syslog (push)
* Supported Guardium versions:
	* Guardium Data Protection: 12.2 and above

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

## 2. Viewing the audit logs
The audit logs are stored in files with the following naming patterns:
- SQL query execution logs - `cockroachdb-sql-exec.cockroachdb.*.log`
- Authentication logs (i.e., failed and successful logins) - `cockroachdb-sql-auth.cockroachdb.*.log`

## 3. Configuring Syslog to push logs to Guardium

### Procedure
To make Logstash able to process the data collected by syslog, configure available
syslog utility. The example is based on rsyslog utility available in many
versions of the Linux distributions.

#### Rsyslog installation guide:
* [Ubuntu](https://www.rsyslog.com/ubuntu-repository)
* [RHEL](https://www.rsyslog.com/rhelcentos-rpms)

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
   * **Guardium Data Protection** <br/>
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
   the syslog messages to the provided host `TARGET_HOST` at the provided port `TARGET_PORT`. Add the following configuration:

   **For TLS connection:**
   ```
   global(
   DefaultNetstreamDriverCAFile="/path/to/certs/ca.pem"
   # DefaultNetstreamDriverCertFile="/path/to/certs/tls-client-cert.crt"
   # DefaultNetstreamDriverKeyFile="/path/to/certs/tls-client-key.key"
   )

   module(load="imfile")
   ruleset(name="imfile_to_gdp") {
           action(type="omfwd"
           protocol="tcp"
           StreamDriver="gtls"
           StreamDriverMode="1"
           StreamDriverAuthMode="x509/certvalid"
           template="RSYSLOG_SyslogProtocol23Format"
           target="<TARGET_HOST>"
           port="<TARGET_PORT>")
   }

   # Monitor CockroachDB SQL query execution logs
   input(type="imfile"
         File="/path/to/logs/directory/cockroach-sql-exec.log"
         Tag="cockroachdb-sql-exec"
         Ruleset="imfile_to_gdp")

   # Monitor CockroachDB authentication logs
   input(type="imfile"
         File="/path/to/logs/directory/cockroach-sql-auth.log"
         Tag="cockroachdb-auth"
         Ruleset="imfile_to_gdp")

   # Monitor CockroachDB schema logs
   input(type="imfile"
         File="/path/to/logs/directory/cockroach-sql-schema.log"
         Tag="cockroach-schema"
         Ruleset="imfile_to_gdp")
   ```

   **For non-TLS connection:**
   ```
   module(load="imfile")
   ruleset(name="imfile_to_gdp") {
           action(type="omfwd"
           TARGET="<TARGET_HOST>"
           Port="<TARGET_PORT>"
           Protocol="tcp"
           template="RSYSLOG_SyslogProtocol23Format")
   }

   # Monitor CockroachDB SQL query execution logs
   input(type="imfile"
         File="/path/to/logs/directory/cockroach-sql-exec.log"
         Tag="cockroachdb-sql-exec"
         Ruleset="imfile_to_gdp")

   # Monitor CockroachDB authentication logs
   input(type="imfile"
         File="/path/to/logs/directory/cockroach-sql-auth.log"
         Tag="cockroachdb-auth"
         Ruleset="imfile_to_gdp")

   # Monitor CockroachDB schema logs
   input(type="imfile"
         File="/path/to/logs/directory/cockroach-sql-schema.log"
         Tag="cockroach-schema"
         Ruleset="imfile_to_gdp")
   ```

    **For multiple managed units (Guardium collectors):**
    ```
    # Ruleset to forward to multiple managed units
    ruleset(name="imfile_to_gdp") {
        # Forward to first managed unit
        action(type="omfwd"
               TARGET="<TARGET_HOST_1>"
               Port="<TARGET_PORT>"
               Protocol="tcp"
               Template="RSYSLOG_SyslogProtocol23Format")
	    
        # Forward to second managed unit
        action(type="omfwd"
               TARGET="<TARGET_HOST_2>"
               Port="<TARGET_PORT>"
               Protocol="tcp"
               Template="RSYSLOG_SyslogProtocol23Format")
	    
        # Add more action blocks for additional managed units as needed
    }
    ```

5. Restart Rsyslog service:
	```bash
	systemctl restart rsyslog
	```

6. Verify Rsyslog is running:
	```bash
	systemctl status rsyslog
	```

## 4. Limitations
- CockroachDB wraps query values in Unicode characters `‹` (U+2039) and `›` (U+203A) in audit logs (e.g., `UPDATE table SET id = ‹2› WHERE id > ‹1›`). The plugin automatically removes these characters to restore the original query format.
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
    - Server Port and Server IP - it is set to default value `0.0.0.0`
    - Source Program (might be missing in some audit logs)
- For failed login attempts, CockroachDB returns different errors depending on the failure type:
  - When the username does not exist: `USER_NOT_FOUND` error
  - When the username exists but the password is incorrect: `PRE_HOOK_ERROR` error
- When using `cockroach sql` CLI with password authentication, a `client_authentication_failed` event is logged before successful login. The failed event is reported to Guardium as a LOGIN_FAILED exception.
- The audit logs captures sql errors for syntactically correct queries (appears in both Full SQL report and Exception report) and does not capture syntactically incorrect queries.
- DDL operations (CREATE, ALTER, DROP, etc.) appear twice in FULL SQL reports. This occurs because CockroachDB logs DDL statements in both `cockroach-sql-exec.log` and `cockroach-sql-schema.log`.

## 5. Configuring the CockroachDB filter in Guardium

The Guardium universal connector is the Guardium entry point for native audit logs. The universal connector identifies and parses received events, and then converts them to a standard Guardium format. The output of the universal connector is forwarded to the Guardium sniffer on the collector, for policy and auditing enforcements. Configure Guardium to read the native audit logs by customizing the CockroachDB template.

### Before you begin

• Configure the policies to match the CockroachDB events.

• You must have permission for the S-Tap Management role. The admin user includes this role, by default.

• Download the [logstash-filter-cockroachdb_guardium_filter.zip](CockroachDBOverSyslogPackage/logstash-filter-cockroachdb_guardium_filter.zip) plug-in.

### Procedure

1. On the collector, go to **Setup** > **Tools and Views** > **Configure Universal Connector**.
2. First enable the Universal Guardium connector, if it is disabled.
3. Click **Upload File** and select the offline [logstash-filter-cockroachdb_guardium_filter.zip](CockroachDBOverSyslogPackage/logstash-filter-cockroachdb_guardium_filter.zip) plug-in. After it is uploaded, click **OK**. 
4. Click the **Plus sign** to open the Connector Configuration dialog box.
5. Type a name in the **Connector name** field.
6. Update the input section to add the details from the [CockroachDBOverSyslog.conf](CockroachDBOverSyslogPackage/CockroachDBOverSyslog.conf) file input section, omitting the keyword "input{" at the beginning and its corresponding "}" at the end.
7. Update the filter section to add the details from the [CockroachDBOverSyslog.conf](CockroachDBOverSyslogPackage/CockroachDBOverSyslog.conf) file filter section, omitting the keyword "filter{" at the beginning and its corresponding "}" at the end.
8. The "type" fields should match in the input and the filter configuration section. This field should be unique for every individual connector added.
9. Click **Save**. Guardium validates the new connector and displays it in the Configure Universal Connector page.