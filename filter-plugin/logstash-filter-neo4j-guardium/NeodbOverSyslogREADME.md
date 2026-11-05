# Neo4j-Guardium Logstash filter plug-in (Syslog)

## Enabling the audit logs

### Procedure

1. Neo4j supports the below log types:
   * Debug.log: Information useful when debugging problems with Neo4j.
   * neo4j.log: The standard log, where general information about Neo4j is written.
   * query.log: Log of executed queries that takes longer than a specified threshold.
   * security.log: Log of security events.
   * http.log: Request log for the HTTP API.
   * gc.log: Garbage collection logging provided by the Java Virtual Machine (JVM).
   * service-error.log: (Windows) Log of errors encountered when installing or running the Windows service.

2. These instructions will use query.log for the Guardium filter, as it contains all database-related log events.

3. Log configuration: Create this configuration in the neo4j.conf file located at {NeoInstallationDir}/neo4jDatabases/{DB_Name}/installation-4.1.0/conf: [For version 4.x and below]
   * Provide database name:
     ```
     dbms.default_database=<DB_Name>
     ```
   * Enable audit logs:
     ```
     dbms.logs.query.enabled=true
     ```
   * Include parameters for the executed queries being logged:
     ```
     dbms.logs.query.parameter_logging_enabled=true
     ```
   * Include detailed time information for the executed queries being logged:
     ```
     dbms.logs.query.time_logging_enabled=true
     ```
   * Include bytes allocated by the executed queries being logged:
     ```
     dbms.logs.query.allocation_logging_enabled=true
     ```
   * Include page hit and page fault information for the executed queries being logged:
     ```
     dbms.logs.query.page_logging_enabled=true
     ```

4. Cleanup for log files: You can set a maximum number of history files to be kept on the system for the query log. To enable this, include this configuration in the neo4j.conf file: [For version 4.x and below]
   ```
   dbms.logs.query.rotation.keep_number=7
   ```

5. Log configuration: Create this configuration in the neo4j.conf file located at {NeoInstallationDir}/neo4jDatabases/{DB_Name}/installation-5.x/conf: [For version 5.x and above]
   * Provide database name:
     ```
     dbms.default_database=<DB_Name>
     ```

## Viewing the audit logs

To view logs, go to the Neo4j console and select DB > manage > Open Folder > logs.

## Configuring rsyslog to push logs to Guardium

### rsyslog installation

rsyslog is typically pre-installed on most Linux distributions. To verify:

```bash
rsyslogd -v
```

If not installed, install it using your package manager:
* Ubuntu/Debian: `sudo apt-get install rsyslog`
* RHEL/CentOS: `sudo yum install rsyslog`

### rsyslog configuration

To configure rsyslog to forward Neo4j logs to Guardium Universal Connector using RFC5424 format:

1. **Check if imfile module is already loaded** in `/etc/rsyslog.conf`:

```bash
grep "imfile" /etc/rsyslog.conf
```

If not present, add to `/etc/rsyslog.conf`:
```conf
module(load="imfile" PollingInterval="10")
```

2. **Create a new rsyslog configuration file for Neo4j:**

```bash
sudo vi /etc/rsyslog.d/10-neo4j.conf
```

3. **Add the following configuration:**

```conf
# Neo4j query.log
input(type="imfile"
      File="/var/log/neo4j/query.log"
      Tag="neo4j-query"
      Severity="info"
      Facility="local0"
      reopenOnTruncate="on")

# Neo4j debug.log
input(type="imfile"
      File="/var/log/neo4j/debug.log"
      Tag="neo4j-debug"
      Severity="debug"
      Facility="local1"
      reopenOnTruncate="on")

# Neo4j security.log
input(type="imfile"
      File="/var/log/neo4j/security.log"
      Tag="neo4j-security"
      Severity="warning"
      Facility="local2"
      reopenOnTruncate="on")

# Template for structured logging (RFC5424 format)
template(name="Neo4jLogFormat" type="string"
         string="<%pri%>%timestamp:::date-rfc3339% %hostname% %syslogtag%%msg:::sp-if-no-1st-sp%%msg:::drop-last-lf%\n")

# Forward Neo4j logs to Guardium UC
if $syslogtag contains 'neo4j' then {
    action(type="omfwd" target="<GUARDIUM_UC_HOST_IP>" port="5000" protocol="tcp" template="Neo4jLogFormat")
}
```

4. **Replace the following placeholders:**
   * `/var/log/neo4j/` - Path to your Neo4j logs directory
   * `<GUARDIUM_UC_HOST_IP>` - IP address or hostname of your Guardium Universal Connector
   * `5000` - Port number (must match the TCP input port in Logstash configuration)

5. **Set proper permissions** (for RHEL/CentOS, rsyslog runs as root by default):

```bash
sudo chmod -R 755 /var/log/neo4j
```

6. **Validate rsyslog configuration:**

```bash
sudo rsyslogd -N1
```

7. **Restart rsyslog service:**

```bash
sudo systemctl restart rsyslog
sudo systemctl status rsyslog
```

### Testing the configuration

1. **Check rsyslog logs for any errors:**

```bash
sudo journalctl -u rsyslog -n 50 --no-pager
```

2. **Verify rsyslog is forwarding:**

```bash
# Check network connection
sudo netstat -an | grep 5000
# or
sudo ss -tan | grep 5000
```

3. **Generate Neo4j queries to verify logs are being forwarded:**

```bash
cypher-shell -u neo4j -p your_password
MATCH (n) RETURN count(n);
:exit
```

4. **Check if logs appear in Neo4j log file:**

```bash
sudo tail -5 /var/log/neo4j/query.log
```

5. **On the Guardium/Logstash side, verify that events are being received.**

### Troubleshooting

* **Permission issues**: Ensure rsyslog has read permissions on the Neo4j log files:
  ```bash
  sudo chmod -R 755 /var/log/neo4j
  ```

* **Module not loading**: Check if imfile module is available:
  ```bash
  rsyslogd -v | grep imfile
  ```

* **Logs not being forwarded**: Check rsyslog state files:
  ```bash
  sudo ls -la /var/lib/rsyslog/
  ```

* **Force reading from beginning of file**: Add `readMode="2"` to input configuration:
  ```conf
  input(type="imfile"
        File="/var/log/neo4j/query.log"
        Tag="neo4j-query"
        readMode="2"
        ...)
  ```

* **SELinux issues** (RHEL/CentOS): If SELinux is enabled, you may need to adjust policies:
  ```bash
  sudo setsebool -P rsyslogd_can_network_connect on
  ```

* **Firewall**: Ensure the port (e.g., 5000) is open on the Guardium UC host:
  ```bash
  sudo firewall-cmd --add-port=5000/tcp --permanent
  sudo firewall-cmd --reload
  ```

* **Debug mode**: Run rsyslog in debug mode to see what's happening:
  ```bash
  sudo systemctl stop rsyslog
  sudo rsyslogd -dn 2>&1 | grep -i "neo4j\|imfile"
  ```
