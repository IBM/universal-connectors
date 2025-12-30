# Sybase Datasource Profile Configuration Guide for JDBC Kafka Connect Plug-ins

This guide provides instructions for creating and configuring datasource profiles through Central Manager for **Sybase
JDBC Kafka Connect** plug-ins.

## Meet Sybase Over JDBC Connect

* Environments: On-prem
* Supported inputs: Kafka Input (pull)
* Supported Guardium versions:
    * Guardium Data Protection: Appliance bundle 12.2.1 or later.

Kafka-connect is a framework for streaming data between Apache Kafka and other systems.

## Configuring the Sybase Adaptive Server Enterprise (ASE) server

### Procedure
1. Install Sybase ASE version 16.0 on your system based on your operating system.
2. Set up your system administrator (SA) credentials.
3. Install the `isql` utility. This command-line SQL interface is required to connect to and interact with Sybase ASE. 
4. Download the `JTDS` JDBC driver by completing the following steps. </br>
    a. Go to the official jTDS website [https://jtds.sourceforge.net/](https://jtds.sourceforge.net/). </br>
    b. From the navigation menu, click on the **Download** link. </br>
    c. Click on the `jtds` folder, and select the desired version from the available releases (e.g., `1.3.1`). </br>
    d. Download the distribution package (e.g., `jtds-1.3.1-dist.zip`). </br>
    e. Extract the archive and locate the driver file `jtds-1.3.1.jar` in the extracted folder. </br>

## Connecting to the Sybase server

### Procedure
1. Connect to your Sybase ASE instance by using the `isql` command-line utility with your system administrator credentials.

    ```isql -U sa -P <password> -S <server_name>```
   
    Parameters:
* `-U sa`: Connect as the system administrator (SA). <br/>
* `-P <your_sa_password>`: Your system administrator account password, <br/>
* `-S <your_server_name>`: The name of your Sybase server instance.

## Setting server timezone to UTC

### Procedure 

1. To check the current time and date settings on your Sybase database server, run the `timedatectl` command.
      
   ```
   [sybase16@sybase16-sysqa ~]$ timedatectl
                  Local time: Thu 2025-10-23 14:21:13 EDT
              Universal time: Thu 2025-10-23 18:21:13 UTC
                    RTC time: Thu 2025-10-23 18:21:13
                   Time zone: America/New_York (EDT, -0400)
   System clock synchronized: yes
                 NTP service: active
             RTC in local TZ: no
   ```

2. If needed, change timezone by running the `timedatectl set-timezone UTC` command.

3. After connecting to your Sybase ASE instance by using the `isql` utility, verify that the local timezone is set to UTC.

   ```
   1> SELECT getdate() AS LocalTime, getutcdate() AS UTCTime
   2> go
    LocalTime                       UTCTime
    ------------------------------- -------------------------------
                Oct 23 2025  6:22PM             Oct 23 2025  6:22PM
   
   (1 row affected)
   ```

## Creating database devices

### Before you begin

A directory is required to store the database devices that you create. If this directory doesn't exist already, create it by using the following command. 

```
sudo mkdir -p $SYBASE/$SYBASE_ASE/data
``` 

### Procedure

1. Create a device to store table data.

   ```
   1> disk init
      name = "auditdev",
      physname = "$SYBASE/$SYBASE_ASE/data/audit_device.dat",
      size = "500M"
   2> go
   ```

2. Create a device to store audit logs.

   ```
   1> disk init
      name = "auditlogdev",
      physname = "$SYBASE/$SYBASE_ASE/data/audit_log_device.dat",
      size = "500M"
   2> go
   ```

Parameters:

* `name`: Logical name for the device. <br/>
* `physname`: Physical file path on disk where the device is created. <br/>
* `size`: Megabytes allocated for this device (change the size based on your needs).


## Creating `sybsecurity` databases

1. To store and manage audit information, create a `sybsecurity` database and allocate storage space based on your requirements.

   ```
   1> CREATE DATABASE sybsecurity
      ON auditdev = '500M'
      LOG ON auditlogdev = '250M'
   2> go
   ```

2. Configure the database by running the following security installation script.

   ```
   isql -U sa -P password -S server_name -i $SYBASE/$SYBASE_ASE/scripts/installsecurity
   ```

3. Verify that the database is successfully created.

   ```
   1> sp_helpdb sybsecurity
   2> go
   ```

4. Restart the Sybase server.

## Enabling auditing

1. Access the security database and configure the audit parameters.

   ```
   1> use sybsecurity
   2> go
   ```

2. Enable the auditing feature.

   ```
   1> sp_configure "auditing", 1
   2> go
   ```

3. Allow the configuration updates.

   ```
   1> sp_configure "allow updates", 1
   2> go
   ```

4. Set the audit policies.
   To ensure that the server remains stable when the audit log device runs out of space, configure Sybase ASE to suspend auditing gracefully instead of crashing. This prevents system failure by halting audit operations when the audit device is full.

   ```
   1> sp_configure "suspend audit when device full", 1
   2> go
   ```

5. Enable comprehensive auditing for the system administrative role (change this based on your needs).

   ```
   1> sp_audit "all", "sa_role", "all", "on"
   2> go
   ```

6. Customize the audit settings.

   The `sp_audit` command accepts the following four parameters:
   * the option type ('insert'),
   * the user scope ('all' for all users),
   * the object to monitor ('sybasetable'),
   * the setting ('on' to enable).
  
   For example, the following commands enable auditing for `sybasetable` insert operations.

   ```
   1> create database sybasedb
   2> go
   1> use sybasedb
   2> go
   1> create table sybasetable (id INT)
   2> go
   1> sp_audit 'insert', 'all', 'sybasetable', 'on'
   2> go
   Audit option has been changed and has taken effect immediately.
   (return status = 0)
   ```

   For more information on customizing audit settings, see [sp_audit](https://help.sap.com/docs/SAP_ASE/29a04b8081884fb5b715fe4aa1ab4ad2/ab54050ebc2b1014b5d9ca93507f4a1d.html).

7. Verify the audit configurations.

   ```
   -- Check audit status
   1> sp_displayaudit
   2> go

   -- View current audit records
   1> select count(*) from sybsecurity..sysaudits_01
   2> go
   ```

## Limitations

#### 1. Failed Login Attempt Logging

Sybase ASE generates multiple audit log entries for failed login attempts, depending on the failure type.

   * **Invalid Username**: When a login attempt fails due to an invalid username, Sybase ASE generates four audit log entries.

      For example, the `extrainfo` values in the following Sybase audit table:

      | ExtraInfo Column                                                  |
      |------------------------------------------------------------------------------------------------|
      | `; ; ; ; sybase16-hostname, 9.00.100.100, network password encryption not set, 16106.14.1; ; ;` |
      | `; ; ; ; 4002.14.1; ; ;`                                                                       |
      | `; ; ; ; sybase16-hostname, 9.00.100.100, network password encryption not set, 16106.14.1; ; ;` |
      | `; ; ; ; 4002.14.1; ; ;`                                                                       |

   * **Invalid Password**: When a valid username is provided with an incorrect password, Sybase ASE generates two audit log entries.

      For example, the `extrainfo` values (for username `sa`) in the following Sybase audit table:

      | ExtraInfo Column                                                                                                                                              |
      |---------------------------------------------------------------------------------------------------------------------------------------------------------------|
      | `sa_role sso_role oper_role sybase_ts_role mon_role; ; ; ; sybase16-hostname, 9.00.100.100, network password rsa encryption with nonce, 4067.14.1; ; sa/ase;` |
      | `sa_role sso_role oper_role sybase_ts_role mon_role; ; ; ; 4002.14.1; ; sa/ase;`                                                                              |

      **Note:** Failed login attempts are tracked and reported in the **SQL Errors** and **Failed Login** reports.

#### 2. Missing Audit Record Fields

The following fields are not available in the Sybase ASE audit records and are populated with default values:

   * **Server IP**: Default value `"0.0.0.0"`
   * **Server Hostname**: Default value `"N.A."`
   * **Server Port**: Derived from the JDBC connection string

#### 3. Field Truncation

`Client Hostname` and `Source Program` fields may be truncated due to character length limitations in the Sybase ASE system tables.

#### 4. Query Length Limitations

Sybase ASE splits long queries across multiple audit records in audit table. When processing these records, queries may appear incomplete and may be dropped if they exceed length limits. 
This affects long queries or complex statements.

## Creating datasource profiles

You can create a new datasource profile from the **Datasource Profile Management** page.

### Procedure

1. Go to **Manage > Universal Connector > Datasource Profile Management**
2. Click the **➕ (Add)** button.
3. You can create a profile by using one of the following methods:

   * To **Create a new profile manually**, go to the **"Add Profile"** tab and provide values for the following fields.
     * **Name** and **Description**.
     * Select a **Plug-in Type** from the dropdown. For example, `Sybase Over JDBC Kafka Connect 2.0`.

   * To **Upload from CSV**, go to the **"Upload from CSV"** tab and upload an exported or manually created CSV file containing one or more profiles. You can also choose from the following options:
     * **Update existing profiles on name match** — Updates profiles with the same name if they already exist.
     * **Test connection for imported profiles** — Automatically tests connections after profiles are created.
     * **Use ELB** — Enables ELB support for imported profiles. You must provide the number of MUs to be used in the ELB process.

**Note:** Configuration options vary based on the selected plug-in.

## Configuration: JDBC Kafka Connect 2.0-based Plugins

The following table describes the fields that are specific to JDBC Kafka Connect 2.0 and similar plugins.

| Field                         | Description                                                                                                                               |
|-------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------|
| **Name**                      | Unique name of the profile.                                                                                                                |
| **Description**               | Description of the profile.                                                                                                                |
| **Plug-in**                   | Plug-in type for this profile. A full list of available plug-ins are available on the **Package Management** page.                              |
| **Credential**                | The credential to authenticate with the datasource. Must be created in **Credential Management**, or click **➕** to create one.       |
| **Kafka Cluster**             | Kafka cluster to deploy the universal connector.                                                                                           |
| **Label**                     | Grouping label. For example, customer name or ID.                                                                                                |
| **JDBC Driver Library**       | JDBC driver for the database.                                                                                                              |
| **Port**                      | Port that is used to connect to the database.                                                                                                      |
| **Hostname**                  | Hostname of the database.                                                                                                                  |
| **Query**                     | SQL query that is used to extract audit logs.                                                                                                      |
| **Service Name / SID**        | The database **service name** or **SID**.                                                                                                  |
| **Initial Time**              | Initial polling time for audit logs.                                                                                                       |
| **No Traffic Threshold**      | Threshold setting for inactivity detection.                                                                                                |
| **Connection URL**            | Full JDBC connection string. Format varies by database type. <br/> For example, `jdbc:jtds:sybase://sybase-db.dev.fyre.ibm.com:5000/sybsecurity`. |
| **Enterprise Load Balancing** |                                                                                                                                           |
| **Use ELB**                   | Enable this if ELB support is required.                                                                                                    |
| **Managed Unit Count**        | Number of Managed Units (MUs) to allocate for ELB.                                                                                         |


**Note:**
- Depending on the plugin type, the configuration may require either:
    - A **Connection URL**, or
    - Separate fields for **Hostname**, **Port**, and **Service Name / SID**
- Ensure that the **profile name** is unique.
- Required credentials must be created before or during profile creation.

---

## Testing a Connection

After creating a profile, you must **test the connection** to ensure the provided configuration is valid.

### Procedure

1. Select the new profile.
2. From the top menu, click **Test Connection**.
3. If the test is successful, you can proceed to installing the profile.

---

## Installing a Profile

Once the connection test is successful, you can install the profile on **Managed Units (MUs)** or **Edges**. The parsed audit logs are sent to the selected Managed Unit or Edge to be consumed by the **Sniffer**.

### Procedure

1. Select the profile.
2. From the **Install** menu, click **Install**.
3. From the list of available MUs and Edges that is displayed, select the ones that you want to deploy the profile to.

---

## Uninstalling or reinstalling profiles

An installed profile can be **uninstalled** or **reinstalled** if needed.

### Procedure

1. Select the profile.
2. From the list of available actions, select the desired option: **Uninstall** or **Reinstall**.

---
