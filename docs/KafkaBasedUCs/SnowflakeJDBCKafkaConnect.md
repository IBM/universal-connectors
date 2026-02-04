# Configuring Snowflake data source profiles for JDBC Kafka Connect plug-ins

Create and configure data source profiles through central manager for Snowflake JDBC Kafka Connect plug-ins.

## Meet Snowflake Over JDBC Connect
* Environments: On-prem
* Supported inputs: Kafka Input (pull)
* Supported Guardium versions:
   * Guardium Data Protection: Appliance bundle 12.2.2 or later

Kafka-connect is a framework for streaming data between Apache Kafka and other systems.

## Before you begin

Download
the [Snowflake JDBC driver](https://repo1.maven.org/maven2/net/snowflake/snowflake-jdbc/3.13.30/snowflake-jdbc-3.13.30.jar). Download the jdbc driver `jar` file from the maven repository 3.13.30 from [here](https://repo1.maven.org/maven2/net/snowflake/snowflake-jdbc/3.13.30/snowflake-jdbc-3.13.30.jar), 3.16.0 from [here](https://repo1.maven.org/maven2/net/snowflake/snowflake-jdbc/3.16.0/snowflake-jdbc-3.16.0.jar) .

## Configuring the Snowflake database

Create the required type of [Snowflake account](https://signup.snowflake.com/).

Upon account creation, a Snowflake database is created and the details are provided over e-mail. Snowflake also provides
the option to choose
a cloud provider to host the database. For more information, see [Snowflake documentation](https://docs.snowflake.com/).

## Enabling audit logs

### Providing permissions to JDBC users

The user that is defined in the **jdbc_user** parameter must have permissions to execute SQL statements.

To test this, replace `:sql_last_value` with an epoch time value and run the query against Snowflake by using the
specified user. Make sure that the user has access to
the [Snowflake database](https://docs.snowflake.com/en/sql-reference/account-usage.html#enabling-snowflake-database-usage-for-other-roles).

Run the following queries to provide access to a specific role.

```sql
  use role accountadmin;
  CREATE ROLE <role-name>;
  CREATE USER <username> PASSWORD = '<password>' ;

  GRANT ROLE <role-name> TO USER <username>;
  ALTER USER <username> SET DEFAULT_ROLE = <role-name>;

  grant imported privileges on database snowflake to role sysadmin;
  grant imported privileges on database snowflake to role <role-name>;
  GRANT USAGE ON WAREHOUSE COMPUTE_WH TO ROLE <role-name>;
  GRANT SELECT ON ALL TABLES IN SCHEMA snowflake.account_usage TO ROLE <role-name>;
``` 

## Viewing the audit logs

To view the audit logs, query the following tables:

- `SNOWFLAKE.ACCOUNT_USAGE.QUERY_HISTORY` <br>
- `SNOWFLAKE.ACCOUNT_USAGE.SESSIONS` <br>
- `SNOWFLAKE.ACCOUNT_USAGE.LOGIN_HISTORY` <br>

## Creating datasource profiles

You can create a new datasource profile from the **Datasource Profile Management** page.

### Procedure

1. Go to **Manage > Universal Connector > Datasource Profile Management**
2. Click the **➕ (Add)** button.
3. You can create a profile by using one of the following methods:

   * To create a new profile manually, go to the **"Add Profile"** tab and provide values for the following fields.
      * **Name** and **Description**.
      * Select a **Plug-in Type** from the dropdown. For example, **Spanner Over PubSub Kafka Connect 2.0**.

   * To upload from CSV, go to the **Upload from CSV** tab and upload an exported or manually created CSV file
     containing one or more profiles. You can also choose from the following options:
      * **Update existing profiles on name match** — Updates profiles with the same name if they already exist.
      * **Test connection for imported profiles** — Automatically tests connections after profiles are created.
      * **Use ELB** — Enables ELB support for imported profiles. You must provide the number of MUs to be used in the
        ELB process.

**Note:** Configuration options vary based on the selected plug-in.

## Limitations

1. The `SNOWFLAKE.ACCOUNT_USAGE` data view contains tables that the plug-in uses for audit logs. Because the data view
   refreshes periodically, there can be a delay in retrieving audit data.
2. The server portal must remain at 443, as configuration support is deprecated. For more information,
   see [Connection parameters reference
   ](https://docs.snowflake.com/en/user-guide/snowsql-start#p-port-deprecated).

## Configuring the key pair authentication.

You can authenticate to the database by using key pair authentication instead of password authentication. For more
information about setting up key pair authentication, see [KeyPairAuth_README](../../filter-plugin/logstash-filter-snowflake-guardium/KeyPairAuth_README.md).

## (Optional) Configuring the proxy

To connect the database through an intermediate proxy, configure the **jdbc_connection_string** parameter as shown in
the following example.

```text
jdbc_connection_string => "jdbc:snowflake://<id>.<region>.<provider>.snowflakecomputing.com/?warehouse=<warehouse>
&db=<database>&useProxy=true&proxyHost=<proxy_hostname/proxy_ip_address>&proxyPort=<proxy_port>" 
```

## Preventing authentication token expiration

To prevent authentication token expiration, configure the [
``CLIENT_SESSION_KEEP_ALIVE``](https://docs.snowflake.com/en/sql-reference/parameters#client-session-keep-alive) and [
``CLIENT_SESSION_KEEP_ALIVE_HEARTBEAT_FREQUENCY``](https://docs.snowflake.com/en/sql-reference/parameters#client-session-keep-alive-heartbeat-frequency)
fields in the **jdbc_connection_string** parameter as shown in the following example.
   ```text  
   jdbc_connection_string => jdbc:snowflake://<id>.<region>.<provider>.snowflakecomputing.com/?warehouse=<warehouse>&db=<database>
   &CLIENT_SESSION_KEEP_ALIVE=true&CLIENT_SESSION_KEEP_ALIVE_HEARTBEAT_FREQUENCY=60
   ```

## Configuring JDBC Kafka Connect 2.0-based plugins

Below is a description of the fields specific to JDBC Kafka Connect 2.0 and similar plugins:

| Field                                         | Description                                                                                                                                | Value/Example                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                         |
|-----------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Name**                                      | Unique name of the profile                                                                                                                 | SNOWFLAKE_JDBC_KAFKA_CONNECT                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          |
| **Description**                               | Description of the profile                                                                                                                 | Profile for Snowflake over JDBC connect 2.0 plug-in                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                   |
| **Plug-in**                                   | Plug-in type for this profile. A full list of available plug-ins is available on the **Package Management** page                           | Snowflake over JDBC connect 2.0                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                       |
| **Credential**                                | The credential to authenticate with the datasource. Create the credential in **Credential Management**, or click **➕** to create a new one |                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                       |
| **Kafka Cluster**                             | Kafka cluster to deploy the universal connector                                                                                            | Select from existing Kafka clusters attached to central management                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                    |
| **Label**                                     | Grouping label (e.g., **customer name** or **ID**)                                                                                         |                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                       |
| **JDBC Driver Library**                       | JDBC driver for the database                                                                                                               | Download from here (https://repo1.maven.org/maven2/net/snowflake/snowflake-jdbc/3.13.30/snowflake-jdbc-3.13.30.jar)                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                   |
| **Port**                                      | Port used to connect to the database                                                                                                       | Snowflake port (e.g., 30041)                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          |
| **Hostname**                                  | Hostname of the database                                                                                                                   | Snowflake hostname                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                    |
| **Query for execution status**                | SQL query used to extract execution status audit logs                                                                                      | SELECT * FROM (SELECT QH.DATABASE_NAME as DATABASE_NAME,QH.SESSION_ID as SESSION_ID, TO_TIMESTAMP_LTZ(QH.END_TIME) as QUERY_TIMESTAMP, LH.CLIENT_IP as CLIENT_IP, CURRENT_IP_ADDRESS() as SERVER_IP, QH.USER_NAME as USER_NAME, S.CLIENT_ENVIRONMENT, QH.QUERY_ID, QH.QUERY_TEXT, QH.QUERY_TYPE, QH.QUERY_TAG , QH.ROLE_NAME, S.CLIENT_APPLICATION_ID, QH.WAREHOUSE_NAME, QH.ERROR_CODE AS QUERY_ERROR_CODE, QH.ERROR_MESSAGE AS QUERY_ERROR_MESSAGE, QH.EXECUTION_STATUS AS QUERY_EXECUTION_STATUS, DATE_PART(epoch_millisecond, QH.END_TIME) AS QUERY_TIMESTAMP_EPOCH FROM SNOWFLAKE.ACCOUNT_USAGE.QUERY_HISTORY QH LEFT JOIN SNOWFLAKE.ACCOUNT_USAGE.SESSIONS S ON S.SESSION_ID = QH.SESSION_ID LEFT JOIN SNOWFLAKE.ACCOUNT_USAGE.LOGIN_HISTORY LH ON S.LOGIN_EVENT_ID = LH.EVENT_ID WHERE QH.EXECUTION_STATUS <> 'RUNNING') AS QUERY_HISTORY_VIEW |
| **Query for Login success**                   | SQL query used to extract login success audit logs                                                                                         | SELECT * FROM (SELECT LH.USER_NAME AS USER_NAME, LH.CLIENT_IP AS CLIENT_IP, CURRENT_IP_ADDRESS() as SERVER_IP, (LH.REPORTED_CLIENT_TYPE \|\| ' ' \|\| LH.REPORTED_CLIENT_VERSION) AS CLIENT_APPLICATION_ID, LH.IS_SUCCESS AS LOGIN_SUCCESS, LH.ERROR_CODE AS LOGIN_ERROR_CODE, LH.ERROR_MESSAGE AS LOGIN_ERROR_MESSAGE, TO_TIMESTAMP_LTZ(LH.EVENT_TIMESTAMP) AS LOGIN_TIMESTAMP, DATE_PART(epoch_millisecond, LH.EVENT_TIMESTAMP) AS LOGIN_TIMESTAMP_EPOCH FROM SNOWFLAKE.ACCOUNT_USAGE.LOGIN_HISTORY LH WHERE LH.IS_SUCCESS LIKE '%NO%') AS QUERY_LOGIN_VIEW                                                                                                                                                                                                                                                                                         |
| **Tracking column type for execution status** | Data type of the column used to track execution status                                                                                     | Numeric                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                               |
| **Tracking column for execution status**      | Column used to track execution status                                                                                                      | query_timestamp_epoch                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                 |
| **Tracking column type for Login success**    | Data type of the column used to track login success                                                                                        | Numeric                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                               |
| **Tracking column for Login success**         | Column used to track login success                                                                                                         | query_timestamp_epoch                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                 |
| **Connection String**                         | Connection string to connect to Snowflake database                                                                                         | jdbc:snowflake://<hostname>/?user=<UserName>>&warehouse=<Warehouse>&db=<DB Name>                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                      |

## Testing a Connection

After creating a profile, you must test the connection to make sure that the provided configuration is valid.

### Procedure

1. Select the new profile.
2. From the top menu, click **Test Connection**.
3. If the test is successful, you can proceed to installing the profile.

---

## Installing a Profile

After the connection test succeeds, you can install the profile on **Managed Units (MUs)** or **Edges**. The parsed
audit logs are sent to the selected Managed Unit or Edge for to be consumed by the **Sniffer**.

### Procedure

1. Select the profile.
2. From the **Install** menu, click **Install**.
3. From the displayed list of available MUs and Edges, select the ones to which you want to deploy the profile.

---

## Uninstalling or reinstalling profiles

An installed profile can be uninstalled or reinstalled if needed.

### Procedure
1. Select the profile.
2. From the list of available actions, select the desired option **Uninstall** or **Reinstall**.

---



