# Configuring Yugabyte datasource profile for Syslog Kafka Connect plug-ins

You can create and configure datasource profiles through Central Manager for **YugabyteDB over Syslog Kafka Connect** plug-ins.

## Meet Yugabyte over Syslog Connect

* **Tested versions:** YugabyteDB 2.25.1.0
* **Environment:** On-premise
* **Supported inputs:** Syslog push through a TCP or TCPSSL listener
* **Supported Guardium versions:** **TODO / Confirm exact support matrix for the Kafka Connect profile**
  * The existing Yugabyte over syslog README lists Guardium Data Protection 12.1 patch 5008 and above, 12.2 patch 5008 and above, and 12.3 and above.
  * The existing AWS MSSQL Kafka Connect profile README lists Guardium Data Protection appliance bundle 12.2.2 or later for that specific JDBC Kafka Connect plug-in.

Kafka Connect is a framework for streaming data between Apache Kafka and other systems. In this Yugabyte profile, Yugabyte audit events are pushed to the syslog listener and then processed by the universal connector.

YugabyteDB reuses the PostgreSQL query language through **YSQL** and the Cassandra query language through **YCQL** for database interactions. The audit log files generated for these APIs are collected and forwarded to Guardium.

## Important implementation note

The existing Yugabyte README describes **syslog push** collection from Yugabyte audit log files. Therefore, this README is written for a **Syslog Kafka Connect** profile, not a JDBC polling profile.

Before publishing or testing this README, confirm the following with the Yugabyte Kafka Connect plug-in implementation:

1. The exact **Plug-in Type** name displayed in **Datasource Profile Management**.
2. The exact supported **Guardium version / appliance bundle**.
3. The default or recommended **Syslog port**, if the plug-in provides one.
4. Whether the profile supports both **TCP** and **TCPSSL**.
5. The required TCPSSL certificate or truststore configuration, if TCPSSL is supported.
6. Whether the profile supports only **YSQL** audit logs or both **YSQL** and **YCQL** audit logs.

## Configuring YugabyteDB server

Enable YugabyteDB audit logging before creating the Guardium datasource profile.

### Procedure

1. YugabyteDB supports two types of audit-related logs:

   a. `postgres-*.log`: logs related to operations performed through the `ysql` API.

   b. `yb-tserver.*.*.log.(WARNING|INFO|ERROR|FATAL).*-*.*`: logs related to operations performed through the `ycql` API.

2. Configure YSQL audit logging.

   YugabyteDB YSQL uses the PostgreSQL Audit Extension (`pgaudit`). Audit logging can be configured by providing `pgaudit` settings through the `--ysql_pg_conf_csv` YB-TServer flag.

3. Configure YCQL audit logging if the deployment requires YCQL audit records.

   YCQL audit logging can be enabled with `--ycql_enable_audit_log=true`.

4. Use the following minimum settings as a starting point:

   ```bash
   --ysql_pg_conf_csv=pgaudit.log='ALL',pgaudit.log_level=INFO,pgaudit.log_parameter=true,pgaudit.log_relation=on,log_line_prefix='%n %r [%p] %a %u %d %c %x '
   --ycql_enable_audit_log=true
   --ycql_audit_included_categories=QUERY,DML,DDL,DCL,AUTH,PREPARE,ERROR,OTHER
   --ycql_audit_log_level=INFO
   --use_cassandra_authentication=true
   ```

5. Create a configuration file that contains the required flags and start YugabyteDB with the flag file:

   ```bash
   bin/yugabyted start --tserver_flags=flagfile=<path_to_flag_file>
   ```

6. After configuring YSQL audit logging, create the `pgaudit` extension from `ysqlsh`:

   ```sql
   CREATE EXTENSION IF NOT EXISTS pgaudit;
   ```

7. Validate that audit logs are being generated.

   The existing Yugabyte over syslog README states that logs can be viewed under `yb_data/logs` relative to the Yugabyte installation directory.

## Configuring syslog forwarding

Configure syslog forwarding so that Yugabyte audit log events are pushed to the syslog listener configured in the datasource profile.

### Procedure

1. Configure rsyslog `imfile` to read the Yugabyte audit log file.

   Example for YSQL audit logs:

   ```conf
   input(type="imfile"
     File="/root/var/data/yb-data/tserver/logs/postgresql-*.log"
     Tag="yugabyte:"
     Severity="info"
     Facility="local0"
     startmsg.regex="^[0-9]{10}[.][0-9]{3} "
     readTimeout="2"
     escapeLF="on"
     escapeLF.replacement=" ")
   ```

2. Add a template that prepends the hostname and source IP address before the original message.

   ```conf
   template(name="YugabyteCsv"
            type="string"
            string="%hostname%,%fromhost-ip%,%msg%\n")
   ```

3. Forward the events to the syslog listener host and port.

   For plain TCP syslog forwarding:

   ```conf
   local0.* @@<target_host_ip>:<syslog_port>;YugabyteCsv
   ```

   The `<target_host_ip>` must be the host that receives syslog events for the Kafka Connect profile. The `<syslog_port>` must match the **Syslog port** field in the datasource profile.

4. If **TCPSSL** is used, configure the syslog sender with the required TLS settings and make sure the datasource profile uses **TCPSSL** as the listener type. Do not mix TCP on one side and TCPSSL on the other side.

## Creating a datasource profile

You can create a new datasource profile from the **Datasource Profile Management** page.

### Procedure

1. Go to **Manage > Universal Connector > Datasource Profile Management**.
2. Click the **➕ (Add)** button.
3. Create the profile by using one of the following methods:

   * To **Create a new profile manually**, go to the **Add Profile** tab and provide the required fields.

4. In the **Plug-in Type** field, select the YugabyteDB over Syslog Kafka Connect plug-in. Example: `YugabyteDB Over Syslog Connect 2.0`. **TODO / Confirm the exact display name from Package Management.**

## Configuration: Yugabyte over Syslog Kafka Connect 2.0-based profile

The following table follows the datasource-profile structure used by the existing AWS MSSQL README and adapts it for the Yugabyte syslog-based profile. Fields may vary depending on the actual Yugabyte plug-in package.

| Field | Description |
|---|---|
| **Name** | Unique name of the profile. |
| **Description** | Description of the profile. |
| **Plug-in** | Plug-in type for this profile. Confirm the exact YugabyteDB over Syslog Kafka Connect display name from **Package Management**. |
| **Kafka Cluster** | Kafka cluster where the universal connector is deployed. |
| **Label** | Grouping label, such as customer name, environment, or database group. |
| **Syslog port** | Port on which the Yugabyte syslog listener receives audit events. Configure the same port on the syslog sender, for example `local0.* @@<target_host_ip>:<syslog_port>;YugabyteCsv`. The existing Yugabyte syslog example uses port `5514`; use the actual port configured for this profile and avoid ports reserved by Guardium. |
| **Syslog listener (TCP or TCPSSL)** | Network listener type used to receive Yugabyte syslog events. Select **TCP** for plain syslog over TCP. Select **TCPSSL** only when the syslog sender is configured to use TLS/SSL with the required certificates. The listener type must match the sender configuration. |
| **Database hostname** | Hostname or IP address of the YugabyteDB server, node, or cluster endpoint whose audit logs are being collected. This identifies the database source represented by the incoming audit events; do not use the Kafka, Guardium, or syslog relay host unless that host is also the YugabyteDB source being documented. |
| **No traffic threshold (minutes)** | Number of minutes the profile can go without receiving Yugabyte audit events before it is treated as no-traffic or inactive. Set this value based on the expected audit-event frequency. Use a lower value for active test systems and a higher value for low-traffic environments to avoid false no-traffic alerts. |

## Testing a connection

After creating a profile, test the configuration to ensure that the provided values are valid.

### Procedure

1. Select the new profile.
2. From the top menu, click **Test Connection**, if the plug-in provides this action.
3. If the test is successful, proceed to installing the profile.

If the connection test or traffic validation fails, verify:

* The syslog sender is forwarding Yugabyte audit events to the correct target host and **Syslog port**.
* The datasource profile uses the same listener type as the sender: **TCP** or **TCPSSL**.
* Firewall rules allow traffic from the Yugabyte/syslog sender host to the syslog listener port.
* The rsyslog/Filebeat configuration is reading the correct Yugabyte log path.
* Multi-line audit events are handled with `escapeLF="on"` and `escapeLF.replacement=" "` when using rsyslog `imfile`.
* Yugabyte audit logging is enabled and audit log files are being generated under the expected `yb_data/logs` path.
* The database hostname in the profile represents the correct YugabyteDB source.
* The no-traffic threshold is appropriate for the expected event volume.

## Installing a profile

After the connection test is successful, install the profile on the required **Managed Units (MUs)** or **Edges**. Parsed audit logs are sent to the selected Managed Unit or Edge to be consumed by the **Sniffer**.

### Procedure

1. Select the profile.
2. From the **Install** menu, click **Install**.
3. Select the MUs or Edges where the profile should be deployed.

## Uninstalling or reinstalling profiles

An installed profile can be uninstalled or reinstalled if needed.

### Procedure

1. Select the profile.
2. From the list of available actions, select **Uninstall** or **Reinstall**.

## Limitations and open questions

