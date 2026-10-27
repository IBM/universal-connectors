# SingleStore-Guardium Logstash filter plug-in: SingleStore over Syslog

### Meet SingleStore over Syslog

- Tested versions: 8.7.12 (memsql version)
- Environment: On-premise, Iaas
- Supported inputs: Syslog (push)
- Supported Guardium versions:
  - Guardium Data Protection: 12.0 and above

This page describes how to send SingleStore audit logs to Guardium over Syslog. The same `singlestoredb_guardium_filter` plug-in is used as for Filebeat. For the plug-in description and for enabling the audit logs, see the [main README](README.md). For the Filebeat option, see section 3 of the [main README](README.md).

## 1. Enabling the audit logs

Follow [Enabling the audit logs](README.md#1-enabling-the-audit-logs) in the main README.

## 2. Configuring rsyslog to push logs to Guardium

### Procedure

1. Install rsyslog on the SingleStore node, and make sure that the `imfile` module is available.

2. Add the following configuration, for example in `/etc/rsyslog.d/singlestore-guardium.conf`.

   ```text
   module(load="imfile" PollingInterval="10")
   $MaxMessageSize 64k

   # The message carries the syslog header, the database hostname and the raw SingleStore audit line.
   # Replace singlestore10.dev.fyre.ibm.com with the hostname of the SingleStore node.
   template(name="SingleStoreAuditFormat" type="string"
     string="<%PRI%>1 %TIMESTAMP:::date-rfc3339% singlestore10.dev.fyre.ibm.com %APP-NAME% - - - serverHostname=singlestore10.dev.fyre.ibm.com serverPort=6514 %rawmsg%\n"
   )

   ruleset(name="forwardToGUC") {
       action(type="omfwd"
              target="<GUARDIUM_COLLECTOR_HOST>"
              Port="<PORT_NUMBER>"
              Protocol="tcp"
              # StreamDriver="gtls"
              # StreamDriverMode="1"
              # StreamDriverAuthMode="anon"
              template="SingleStoreAuditFormat"
              action.resumeRetryCount="-1"
              action.resumeInterval="10"
              queue.type="LinkedList"
              queue.size="100000"
              queue.dequeueBatchSize="2000"
              queue.workerThreads="4"
              queue.timeoutEnqueue="0"
              queue.saveOnShutdown="on"
              queue.fileName="singlestore_queue2"
              queue.maxDiskSpace="2g"
              queue.highWatermark="80000"
              queue.lowWatermark="40000"
              queue.discardMark="95000"
              queue.discardSeverity="7")
   }

   # SingleStore audit log input.
   # Adjust the path to match the auditlogsdir of your SingleStore deployment.
   input(type="imfile"
         File="/var/lib/memsql/auditlogs/*.log"
         Tag="singlestore-audit"
         Ruleset="forwardToGUC"
         reopenOnTruncate="on"
         startmsg.regex="^[0-9]+,")
   ```

   **Note:**
   - `Tag` becomes the `APP-NAME` field of the syslog header. Use the same value as the **Datasource Tag** when you configure the connector in Guardium.
   - `startmsg.regex="^[0-9]+,"` keeps a multi-line SQL statement together in one event.
   - `reopenOnTruncate="on"` makes rsyslog read the file again when it is truncated or rotated.
   - `serverHostname=` is optional. If it is removed, the server hostname is taken from the SingleStore audit line.
   - `serverPort=` in the template is not used by the filter. The database port is taken from the SingleStore audit line.
   - To use TLS, uncomment the `StreamDriver` lines, and enable SSL in the input section of the connector.

3. Restart rsyslog.

   ```text
   systemctl restart rsyslog
   ```

### Sample message received by Guardium

```text
<133>1 2026-10-27T12:09:30.724-07:00 singlestore10.dev.fyre.ibm.com singlestore-audit - - - serverHostname=singlestore10.dev.fyre.ibm.com serverPort=6514 263075,2026-10-27 12:09:30.724,PDT,singlestore10.dev.fyre.ibm.com:3306,agg,1,99999,root,uc_audit_test,,11966432271814971918,CREATE ROWSTORE TABLE IF NOT EXISTS audit_test (\\n   id INT NOT NULL\,\\n    username VARCHAR(100)\,\\n    department VARCHAR(100)\,\\n    salary DECIMAL(10\,2)\,\\n    created_at DATETIME\,\\n    PRIMARY KEY (id)\,\\n    SHARD KEY (id)\\n)
```

### Limitations

• Source Program is not part of the SingleStore logs.  
• Client IP can only be retrieved in login / logout actions.  
• Queries with SQL errors are included in the `Full SQL` report and are not displayed in `SQL Errors`.  
• The server IP is the address of the host that sends the syslog message over TCP. If the message passes through NAT or a load balancer, the server IP is not the address of the database. If it cannot be determined, the server IP is reported as `0.0.0.0`. The TCP input must have `dns_reverse_lookup_enabled => false`, as in [singleStoreSyslog.conf](./singleStoreSyslog.conf). Otherwise the sender is reported by its DNS name, which is not a valid IP address, and the server IP is reported as `0.0.0.0`.  
• rsyslog escapes the new lines of a multi-line SQL statement, and the SingleStore audit log ends each wrapped line with a backslash, so the new line arrives as `\\n` (or `#012`). The filter replaces these sequences with a space. A backslash followed by `n` that is written inside the SQL statement itself looks the same after the escaping, so it is also replaced with a space.  
• `$MaxMessageSize 64k` limits the size of a message. A longer SQL statement is truncated by rsyslog.

## 3. Configuring the SingleStore filters in Guardium

The Guardium universal connector is the Guardium entry point for native audit logs. The universal connector identifies and parses received events, and then converts them to a standard Guardium format. The output of the universal connector is forwarded to the Guardium sniffer on the collector, for policy and auditing enforcements. Configure Guardium to read the native audit logs by customizing the SingleStore template.

### Before you begin

• Configure the [policies](/docs/#policies).

• You must have permission for the S-Tap Management role. The admin user includes this role, by default.

### Procedure

1. On the collector, go to Setup > Tools and Views > Configure Universal Connector.
2. First enable the Universal Guardium connector, if it is disabled already.
3. Click Upload File and select the offline [logstash-filter-singlestoredb_guardium_filter.zip](SingleStoreOverFilebeatPackage/logstash-filter-singlestoredb_guardium_filter.zip) plug-in. After it is uploaded, click OK.
4. Click the Plus sign to open the Connector Configuration dialog box.
5. Type a name in the Connector name field.
6. Update the input section to add the details from the [singleStoreSyslog.conf](./singleStoreSyslog.conf) file input section, omitting the keyword "input{" at the beginning and its corresponding "}" at the end.
7. Update the filter section to add the details from the [singleStoreSyslog.conf](./singleStoreSyslog.conf) file filter section, omitting the keyword "filter{" at the beginning and its corresponding "}" at the end.
8. The "type" fields should match in the input and the filter configuration section. This field should be unique for every individual connector added.
9. The tag in the input section (`tags => ["singlestore-audit"]`) and in the filter section (`"singlestore-audit" in [tags]`) must match the `Tag` of the rsyslog `imfile` input.
10. Click Save. Guardium validates the new connector and displays it in the Configure Universal Connector page.
