# Configuring ClickHouse data source profiles for JDBC Kafka Connect plug-ins

Create and configure data source profiles through central manager for ClickHouse JDBC Kafka Connect plug-ins.

## Meet ClickHouse Over JDBC Connect
* Environments: On-prem
* Supported inputs: Kafka connect JDBC 2.0 (pull)
* Supported Guardium versions:
   * Guardium Data Protection: Appliance bundle 12.2.5 and above

Kafka-connect is a framework for streaming data between Apache Kafka and other systems.

The plug-in runs two queries over JDBC (Kafka Connect JDBC 2.0 Multi Query):

| Query | Table                | What is sent to Guardium                                                                                  |
|-------|----------------------|-----------------------------------------------------------------------------------------------------------|
| 1     | `system.query_log`   | Successful queries (`QueryFinish`) and failed queries (`ExceptionBeforeStart`, `ExceptionWhileProcessing`). |
| 2     | `system.session_log` | Logins (`LoginSuccess`), logouts (`Logout`) and failed logins (`LoginFailure`).                            |

### Limitations

* Only the node that the JDBC URL points to is audited. `system.query_log` and `system.session_log` are local to
  each node, so in a cluster create one profile per node.
* Both tables are flushed to disk in batches (every 7.5 seconds by default). The default **Delay interval** of
  10000 ms accounts for this. Do not lower it below the `flush_interval_milliseconds` of the `query_log` and
  `session_log` sections in the ClickHouse server configuration.
* The queries that this plug-in runs against `system.query_log` and `system.session_log` are recognized and are not
  reported to Guardium.
* The plug-in logs in over HTTP on every poll, which produces a `LoginSuccess` and a `Logout` row in
  `system.session_log` each time. The logins and logouts of the user that the plug-in connects with (the credential
  of the profile) over the HTTP interface are not reported. Use a dedicated user for the plug-in. Failed logins are
  always reported, also for that user.
* A failed login is recorded in `system.session_log` only. It has no database, so the database name is `N.A.`.
* When collecting from `system.query_log`, the plug-in cannot capture inserted row values for `INSERT ... VALUES` statements because ClickHouse does not retain those values in the query log.

### Before you begin

Download the ClickHouse JDBC driver. Use the `clickhouse-jdbc-<version>-all.jar` (shaded) file from the
[clickhouse-java releases](https://github.com/ClickHouse/clickhouse-java/releases).

## Enabling the query log

`system.query_log` is enabled by default. Verify that it is not disabled in the server configuration:

```sql
SELECT count() FROM system.query_log;
```

If the `query_log` section is missing or the table does not exist, enable it in the server configuration
(`config.xml`, or a file in `config.d`):

```xml
<clickhouse>
    <query_log>
        <database>system</database>
        <table>query_log</table>
        <flush_interval_milliseconds>7500</flush_interval_milliseconds>
    </query_log>
</clickhouse>
```

By default ClickHouse logs every query. Verify that the `log_queries` setting is `1` for the users that you want to
audit:

```sql
SELECT name, value FROM system.settings WHERE name = 'log_queries';
```

## Enabling the session log

`system.session_log` is not enabled in every ClickHouse installation. Verify that the table exists:

```sql
SELECT count() FROM system.session_log;
```

If the table does not exist, enable it in the server configuration (`config.xml`, or a file in `config.d`) and
restart the server:

```xml
<clickhouse>
    <session_log>
        <database>system</database>
        <table>session_log</table>
        <flush_interval_milliseconds>7500</flush_interval_milliseconds>
    </session_log>
</clickhouse>
```

## Creating a user for audit log reading

Create a dedicated user that can read `system.query_log` and `system.session_log`.

```sql
CREATE USER guardium IDENTIFIED BY 'your_password';
GRANT SELECT ON system.query_log TO guardium;
GRANT SELECT ON system.session_log TO guardium;
```

## Field mapping

### system.query_log

| `system.query_log` column | Guardium field                                                                                     |
|---------------------------|----------------------------------------------------------------------------------------------------|
| `current_database`        | Database name                                                                                      |
| `address`, `port`         | Client IP and client port (`::ffff:a.b.c.d` is reported as `a.b.c.d`). Session ID is `ip:port`.     |
| `user`                    | DB user name                                                                                       |
| `client_hostname`         | Client host name                                                                                   |
| `hostname`                | Server host name                                                                                   |
| `interface`               | Communication and database protocol: 1 TCP/Native, 2 HTTP, 3 gRPC, 4 MySQL, 5 PostgreSQL           |
| `os_user`                 | OS user                                                                                            |
| `client_name`             | Source program                                                                                     |
| `query_kind`              | SQL verb (for example `SELECT`)                                                                    |
| `tables`                  | Objects (`database.table`). If empty, the objects are extracted from `query`.                      |
| `query`                   | Full SQL                                                                                           |
| `exception_code`          | Exception type ID (failed queries only)                                                            |
| `exception`               | Exception description. The ClickHouse error name (for example `UNKNOWN_IDENTIFIER`) when present.  |

### system.session_log

| `system.session_log` column | Guardium field                                                                                   |
|-----------------------------|--------------------------------------------------------------------------------------------------|
| `type`                      | `LoginSuccess`: `SET @event = 'USER_LOGIN'`. `Logout`: `SET @event = 'USER_LOGOUT'`. `LoginFailure`: `LOGIN_FAILED` exception. |
| `client_address`, `client_port` | Client IP and client port (`::ffff:a.b.c.d` is reported as `a.b.c.d`). Session ID is `ip:port`. |
| `user`                      | DB user name                                                                                     |
| `client_hostname`           | Client host name                                                                                 |
| `client_name`               | Source program                                                                                   |
| `hostname`                  | Server host name                                                                                 |
| `interface`                 | Communication and database protocol: 1 TCP/Native, 2 HTTP, 3 gRPC, 4 MySQL, 5 PostgreSQL         |
| `failure_reason`            | Exception description of a failed login                                                          |

The server IP and port come from the **Connection URL** of the profile.

## Creating datasource profiles

You can create a new datasource profile from the **Datasource Profile Management** page.

### Procedure

1. Go to **Manage > Universal Connector > Datasource Profile Management**
2. Click the **➕ (Add)** button.
3. You can create a profile by using one of the following methods:

   - To create a new profile manually, go to the **"Add Profile"** tab and provide values for the following fields.
     - **Name** and **Description**.
     - Select a **Plug-in Type** from the dropdown. For example, **ClickHouse Over JDBC Connect 2.0**.

   - To upload from CSV, go to the **Upload from CSV** tab and upload an exported or manually created CSV file
     containing one or more profiles. You can also choose from the following options:
     - **Update existing profiles on name match** — Updates profiles with the same name if they already exist.
     - **Test connection for imported profiles** — Automatically tests connections after profiles are created.
     - **Use ELB** — Enables ELB support for imported profiles. You must provide the number of MUs to be used in the
       ELB process.

**Note:** Configuration options vary based on the selected plug-in.

---

## Configuring JDBC Kafka Connect 2.0-based plugins

The following table describes the fields that are specific to JDBC Kafka Connect 2.0 and similar plugins.

| Field                                   | Description                                                                                                                                         |
| --------------------------------------- |-----------------------------------------------------------------------------------------------------------------------------------------------------|
| **Name**                                | Unique name of the profile.                                                                                                                         |
| **Description**                         | Description of the profile.                                                                                                                         |
| **Plug-in**                             | Plug-in type for this profile. A full list of available plug-ins are available on the **Package Management** page.                                  |
| **Credential**                          | The credential to authenticate with the datasource. Create the credential in **Credential Management**, or click **➕** to create a new one.         |
| **Kafka Cluster**                       | Kafka cluster to deploy the universal connector.                                                                                                    |
| **Label**                               | Grouping label (e.g, customer name or ID).                                                                                                          |
| **JDBC Driver Library**                 | The ClickHouse JDBC driver (`clickhouse-jdbc-<version>-all.jar`).                                                                                   |
| **Connection URL**                      | JDBC URL of the ClickHouse server, for example `jdbc:clickhouse://<host>:8123/system`. The default port is 8123 (HTTP). Keep the `system` database. |
| **Query for query_log**                 | SQL query that is used to extract queries from `system.query_log`. Keep the column aliases of the default query.                                    |
| **Query for session_log**               | SQL query that is used to extract logins, logouts and failed logins from `system.session_log`. Keep the column aliases of the default query.        |
| **Delay interval**                      | Wait time before a row is read. Must be larger than the `query_log` and `session_log` flush intervals. The default is 10000 ms.                     |
| **No Traffic Threshold**                | Threshold setting for inactivity detection.                                                                                                         |
| **Use Enterprise Load Balancing (ELB)** | Enable this if ELB support is required.                                                                                                             |
| **Managed Unit Count**                  | Number of Managed Units (MUs) to allocate for ELB.                                                                                                  |

**Note:**

- Make sure that the **Profile name** is unique.
- Required credentials must be created before or during profile creation.

---

## Testing a Connection

After creating a profile, you must test the connection to make sure that the provided configuration is valid.

### Procedure

1. Select the new profile.
2. From the top menu, click **Test Connection**.
3. If the test is successful, you can proceed to installing the profile.

---

## Installing a Profile

After the connection test succeeds, you can install the profile on **Managed Units (MUs)** or **Edges**. The parsed
audit logs are sent to the selected Managed Unit or Edge to be consumed by the **Sniffer**.

### Procedure

1. Select the profile.
2. From the list of available actions, select the desired option **Install** or **Reinstall**.
3. From the list of available MUs and Edges that is displayed, select the ones that you want to deploy the profile to.

---

## Uninstalling or reinstalling profiles

An installed profile can be **uninstalled** or **reinstalled** if needed.

### Procedure

1. Select the profile.
2. From the list of available actions, select the desired option: **Uninstall** or **Reinstall**.
