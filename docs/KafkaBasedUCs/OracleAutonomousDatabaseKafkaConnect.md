# Configuring Oracle Autonomous Database datasource profiles for JDBC Kafka connect plug-ins

Create and configure datasource profiles through Central Manager for **Oracle Autonomous Database JDBC Kafka Connect** plug-ins.

## Meet Oracle Autonomous Database Over JDBC Connect

* Environments: Oracle Cloud Infrastructure (OCI)
* Tested versions: 19c
* Supported inputs: Kafka Input (pull)
* Supported Guardium versions:
   * Guardium Data Protection: Appliance bundle 12.2.1 or later

Kafka-connect is a framework for streaming data between Apache Kafka and other systems. This connector enables monitoring of Oracle Autonomous Database audit logs through JDBC connectivity.

## Prerequisites

Before you begin, make sure that you have the following prerequisites:
- An Oracle Cloud Infrastructure (OCI) account
- Access to create and manage Oracle Autonomous Database instances
- Oracle JDBC driver (`ojdbc8.jar` or later)

## Creating and configuring Oracle Autonomous Database

### Procedure

1. Log in to the (Oracle Cloud Infrastructure Console)[https://cloud.oracle.com/].
2. From the navigation menu, click **Oracle Database** > **Autonomous Database**.
3. Click **Create Autonomous Database**.
4. Configure the following settings:
   - **Compartment**: Select the compartment in which you want to create the database.
   - **Display name**: Enter a descriptive name for the database.
   - **Database name**: Enter a unique database name. Use alphanumeric characters only.
   - **Workload type**: Select the workload type that matches your use case:
      - **Transaction Processing** (ATP) for online transaction processing (OLTP) workloads.
      - **Data Warehouse** (ADW) for analytics workloads.
   - **Deployment type**: Select **Shared Infrastructure** or **Dedicated Infrastructure**.
5. Configure the database settings:
   - **Database version**: Select the Oracle Database version. Oracle Database 19c or later is recommended.
   - **OCPU count**: Select the number of OCPUs.
   - **Storage**: Specify the storage capacity in TB.
   - **Auto scaling**: Enable this option to automatically scale resources.
6. Set the administrator credentials:
   - **Username**: The default administrator username is `ADMIN`.
   - **Password**: Enter a strong password that meets Oracle's complexity requirements.
   - **Confirm password**: Re-enter the password.
7. Configure network access:
   - **Access Type**: Select **Secure access from everywhere** or **Private endpoint access only** based on your security requirements.
   - **Mutual TLS (mTLS) authentication**: Keep this option enabled for enhanced security (recommended).
8. Click **Create Autonomous Database**.
9. Wait for the database to be provisioned. The status changes from **PROVISIONING** to **AVAILABLE**.


## Downloading the wallet

Oracle Autonomous Database requires a wallet file for secure connections. The wallet contains credentials and connection information.

### Procedure

1. Open the OCI Console and navigate to your Autonomous Database instance.
2. On the Autonomous Database details page, click **Database connection**.
3. In the **Database Connection** dialog, click **Download wallet**.
4. In the **Download wallet** dialog, configure the following settings:
   - **Wallet type**: Select **Instance Wallet** (recommended) or **Regional Wallet**.
      - **Instance Wallet**: Contains connection information for a single database instance.
      - **Regional Wallet**: Contains connection information for all Autonomous Databases in a region.
   - **Password**: Enter a password to protect the wallet file. You must provide this password when you configure the JDBC connection.
   - **Confirm password**: Re-enter the password.
5. Click **Download**.
6. Save the wallet file (e.g., `Wallet_DatabaseName.zip`) to a secure location.
7. Extract the wallet ZIP file to a directory on the system on which Guardium Universal Connector runs.
8. Save the path of the extracted wallet directory. You need this path when you configure the datasource profile.

### Wallet contents

The wallet ZIP file contains the following files:
- **cwallet.sso**: Oracle wallet file (auto-login format)
- **ewallet.p12**: Oracle wallet file (PKCS#12 format)
- **tnsnames.ora**: Network service names and connection strings
- **sqlnet.ora**: SQL*Net configuration
- **ojdbc.properties**: JDBC connection properties
- **keystore.jks**: Java keystore (if applicable)
- **truststore.jks**: Java truststore (if applicable)

**Note:**
- Keep the wallet file secure. It contains credentials that provide access to your database.
- The wallet password is separate from the database ADMIN password.
- If you regenerate the wallet, you must update all applications that use the old wallet.
- For production environments, consider using Instance Wallet for better security isolation.

## Enabling auditing

Oracle Autonomous Database supports both traditional auditing and unified auditing. For this connector, use traditional auditing as it provides audit records in a format that can be queried by using JDBC.

### Connecting to the database

1. Use an SQL client that supports Oracle connections, such as SQL*Plus, SQL Developer, or another JDBC-based tool.
2. Configure the connection by using the wallet:
   - **Connection Type**: TNS
   - **Network Alias**: Select a service name from `tnsnames.ora`, such as `databasename_high`, `databasename_medium`, or `databasename_low`.
   - **Wallet Location**: Specify the directory in which you extracted the wallet files.
   - **Username**: `ADMIN` (or another user with appropriate privileges)
   - **Password**: The ADMIN password set when you created the database

### Applying audit policies

Connect to your Oracle Autonomous Database using the `ADMIN` user and run the SQL commands for your auditing requirements.

**Important:** Use traditional auditing commands (`AUDIT` statement), not unified auditing (`CREATE AUDIT POLICY`). Traditional auditing records can be queried from standard audit views that are accessible through JDBC.

For more information about traditional auditing, see:
- [Oracle Documentation: AUDIT (Traditional Auditing)](https://docs.oracle.com/en/database/oracle/oracle-database/19/sqlrf/AUDIT-Traditional-Auditing.html)


## Creating datasource profiles

You can create a new datasource profile from the **Datasource Profile Management** page.

### Procedure

1. Go to **Manage > Universal Connector > Datasource Profile Management**
2. Click the **➕ (Add)** button.
3. You can create a profile by using one of the following methods:

   * To **Create a new profile manually**, go to the **"Add Profile"** tab and provide values for the following fields.
      * **Name** and **Description**.
      * Select a **Plug-in Type** from the dropdown. For example, `Oracle Autonomous Database Over JDBC Kafka Connect 2.0`.

   * To **Upload from CSV**, go to the **"Upload from CSV"** tab and upload an exported or manually created CSV file containing one or more profiles. You can also choose from the following options:
      * **Update existing profiles on name match** — Updates profiles with the same name if they already exist.
      * **Test connection for imported profiles** — Automatically tests connections after profiles are created.
      * **Use ELB** — Enables ELB support for imported profiles. You must provide the number of MUs to be used in the ELB process.

**Note:** Configuration options vary based on the selected plug-in.

## Configuration: Oracle Autonomous Database JDBC Kafka Connect 2.0

The following table describes the fields that are specific to Oracle Autonomous Database JDBC Kafka Connect 2.0 plugin.

| Field                         | Description                                                                                                                                                     |
|-------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Name**                      | Unique name of the profile.                                                                                                                                     |
| **Description**               | Description of the profile.                                                                                                                                     |
| **Plug-in**                   | Plug-in type for this profile. Select `Oracle Autonomous Database Over JDBC Kafka Connect 2.0`. A list of available plug-ins is available on the **Package Management** page. |
| **Credential**                | The credential to authenticate with the datasource. You can create credentials in **Credential Management**, or click **➕** to create one. Use the `audit_reader` user credentials or ADMIN credentials. |
| **Kafka Cluster**             | Select the appropriate Kafka cluster from the available Kafka cluster list or create a new Kafka cluster. For more information, see [Managing Kafka clusters](https://www.ibm.com/docs/en/SSMPHH_12.x/com.ibm.guardium.doc.stap/guc/guc_kafka_cluster_management.html). |
| **Label**                     | Grouping label. For example, customer name or ID.                                                                                                               |
| **JDBC Driver Library**       | Upload the Oracle JDBC driver (ojdbc8.jar or later). Download from [Oracle JDBC Downloads](https://www.oracle.com/database/technologies/appdev/jdbc-downloads.html). |
| **Connection URL**            | Full JDBC connection string for Oracle Autonomous Database. Format: `jdbc:oracle:thin:@<service_name>` <br/> The service name can be found in the `tnsnames.ora` file within the wallet. |
| **Query**                     | SQL query to extract audit logs. Example: `SELECT * FROM DBA_AUDIT_TRAIL WHERE TIMESTAMP > ? ORDER BY TIMESTAMP` |
| **Initial Time**              | Initial polling time for audit logs. Format: `YYYY-MM-DD HH:MM:SS` or use relative time like `-1h` for one hour ago. |
| **No Traffic Threshold**      | Threshold setting for inactivity detection (in minutes). Default: 60. If there is no incoming traffic for this duration, S-TAP displays a red status. |
| **Use Enterprise Load Balancing (ELB)** | Enable this if ELB support is required.                                                                                                                         |
| **Managed Unit Count**        | Number of Managed Units (MUs) to allocate for ELB.                                                                                                              |

**Note:**
- Make sure that the **profile name** is unique.
- Required credentials must be created before or during profile creation.
- The wallet directory path must be accessible from the system on which the Universal Connector runs.
- The JDBC driver must be compatible with your Oracle Autonomous Database version.
- The connection URL must include the `TNS_ADMIN` parameter that points to the wallet directory.

### Example connection URL

```
jdbc:oracle:thin:@myatp_high?TNS_ADMIN=/opt/guardium/wallets/myatp_wallet
```

In this example:
- `myatp_high` is the service name from `tnsnames.ora`.
- `/opt/guardium/wallets/myatp_wallet` is the directory that contains the extracted wallet files.

---

## Testing a connection

After creating a profile, test the connection to make sure that the provided configuration is valid.

### Procedure

1. Select the new profile.
2. From the top menu, click **Test Connection**.
3. If the test is successful, you can proceed to installing the profile.
4. If the test fails, verify the following items:
   - The wallet files are in the correct location and are accessible.
   - The wallet password is correct (if required).
   - The service name in the connection URL matches an entry in `tnsnames.ora`.
   - The JDBC driver is compatible with your database version.
   - Network connectivity to Oracle Cloud Infrastructure.

---

## Installing a profile

Once the connection test is successful, you can install the profile on Managed Units (MUs) or Edges. The parsed audit logs are sent to the selected Managed Unit or Edge to be consumed by the Sniffer.

### Procedure

1. Select the profile.
2. From the **Install** menu, click **Install**.
3. From the list of available MUs and Edges that is displayed, select the ones to which you want to deploy the profile.

---

## Uninstalling or reinstalling profiles

You can uninstall or reinstall an installed profile, if needed.

### Procedure

1. Select the profile.
2. From the list of available actions, select **Uninstall** or **Reinstall**.



## Limitations

1. **Wallet Management**: The wallet files must be manually distributed to all systems on which the Universal Connector runs. If the wallet is regenerated, all connectors must be updated.

2. **Connection Pooling**: JDBC connection pooling behavior can vary depending on the connector configuration and database workload.

