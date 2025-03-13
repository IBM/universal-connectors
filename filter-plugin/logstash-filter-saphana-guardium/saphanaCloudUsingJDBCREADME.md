### Meet SAP HANA
* Tested versions: 2.00.033.00.1535711040
* Environment: On-premise, Saas
* Supported Guardium versions:
    * Guardium Data Protection: 12.1 and above
        * Supported inputs:
            * Filebeat (push)
            * JDBC (pull)
    * Guardium Data Security Center: 3.3 and above
        * Supported inputs:
            * Filebeat (push)
    * Guardium Data Security Center SaaS: 1.0
        * Supported inputs:
            * Filebeat (push)
            * JDBC (pull)

## 1. Configuring the SAP HANA Cloud Logstash filter plug-in using JDBC input

There are multiple ways to install a SAP HANA Cloud. For this example we are using SAP BTP Cockpit, we will assume that we already have a working SAP HANA Cloud setup.

## 2. Enabling the audit logs:
### Procedure
In the SAP HANA Cloud Central, expand the system for which you would like to enable auditing.
Following this link to enable Audit logs: https://help.sap.com/docs/SAP_HANA_COCKPIT/afa922439b204e9caf22c78b6b69e4f2/db8cca116f1d45e085a68ffdc0dfb92b.html

Here we will review CSTABLE base auditing.
* CSTABLE base auditing - Audit-trail target is a table, requires JDBC input plug-in.

For SAP Hana Cloud, CSTABLE-based auditing (where the audit trail is stored in a table) is enabled by default, so no additional steps are required.

## 3. Common steps for either auditing type

### Creating an audit policy (Optional)

An audit policy defines the actions to be audited. In order to create an audit policy, the user must have
AUDIT ADMIN system rights. Creating an audit policy is a common step for both types of auditing.
In SAP HANA Cloud, default audit policies already exist. If you want to create a custom policy, follow these steps:

#### Procedure

1. On the Database Overview page, with the Security and User Management or All view selected, navigate to the Auditing card, then click the card title.
2. On the Audit Policies tab of the Auditing page, click the Create Audit Policy button and follow the wizard steps.
3. After reviewing the configuration of the new audit policy, choose Save.

* Detailed steps can be found here: https://help.sap.com/docs/SAP_HANA_COCKPIT/afa922439b204e9caf22c78b6b69e4f2/c5f5344403cb40d3a2ed72912a46beb3.html

## 4.  Viewing the audit logs

### View the SAP HANA audit logs for CSTABLE-based auditing.

#### Procedure
There are two ways to view audit logs

1. Open Audit tab under Database Overview page in SAP HANA Cloud Central.
2. Open the SQL console and run the following command:
   ```sql
     select * from AUDIT_LOG;
   ```

## 5. Configuring the SAP HANA Cloud filters in Guardium

The Guardium universal connector is the Guardium entry point for native audit logs. The universal connector
identifies and parses received events, and converts them to a standard Guardium format. The output of the
Guardium universal connector is forwarded to the Guardium sniffer on the collector, for policy and auditing
enforcements. Configure Guardium to read the native audit logs by customizing the SAP HANA template.

### Before you begin

* You must have permissions for the S-Tap Management role. The admin user includes this role, by default.
* Download the required (ngdbc)jars as per your database version from URL https://tools.hana.ondemand.com/#hanatools.
* For CSTABLE based auditing, refer to this [package](SaphanaOverJdbcPackage) and download the [SAPHANA-offline-plugin.zip](SaphanaOverJdbcPackage/SAPHANA-offline-plugin.zip) plug-in.


# Procedure
1. On the collector, go to Setup > Tools and Views > Configure Universal Connector.
2. First enable the Universal Guardium connector, if it is disabled already.
3. Click "Upload File" and select the offline [SAPHANA-offline-plugin.zip](SaphanaOverJdbcPackage/SAPHANA-offline-plugin.zip) plug-in as per specific audit. After it is uploaded, click "OK".
4. Click "Upload File" again and select the ngdbc-2.9.12 jar file. After it is uploaded, click "OK".
5. Click the Plus sign to open the Connector Configuration dialog box.
6. Type a name in the Connector name field.
7. Update the input section . Use the [saphanaJDBC.conf](SaphanaOverJdbcPackage/saphanaJDBC.conf) file's input part, update `jdbc_connection_string => "jdbc:sap://<server-name>:<db-port-number>`,omitting the keyword "input{" at the beginning and its corresponding "}" at the end.
8. Update the filter section for JDBC Plugin. Use the [saphanaJDBC.conf](SaphanaOverJdbcPackage/saphanaJDBC.conf) file's filter part, omitting the keyword "filter{" at the beginning and its corresponding "}" at the end.
9. The "type" fields should match in the input and the filter configuration sections. This field should be unique for every individual connector added. This is no longer required starting v12p20 and v12.1.
10. If using two jdbc plug-ins on same machine, the last_run_metadata_path file name should be different.

**Note: For moderate to large amounts of data, include pagination to facilitate the audit and to avoid out
of memory errors. Use the parameters below in the input section when using a JDBC connector, and remove the
concluding semicolon ';' from the jdbc statement:**
  ```
   jdbc_paging_enabled => true 
   jdbc_page_size => 1000
  ```
11. Click Save. Guardium validates the new connector, and enables the universal connector if it was disabled.
    Once validated, it appears in the Configure Universal Connector page.

## 6. Configuring the SAP HANA Cloud filters in Guardium Data Security Center
1. In the main menu, click **Configurations** > **Connections** > **Monitored Data Stores**.
2. On the **Connections** page, click **Manage** > **Universal Connector Plugins**.
3. Click **Add Plugin**, upload the zip package file as SAPHANA-offline-plugin.zip.
4. To connect a new data source, click **Connections** > **Create connection**.
5. Search for **Sap Hana** and click **Configure**.
6. Enter Name, Description and click **Next**.
7. In the **Build pipeline**, select the input and the filter plugin.
8. Enter the additional information from the JDBC connection string .conf file.
9. Click **Configure** and then **Done**.

## 7. JDBC Load Balancing Configuration

In SAP HANA JDBC input plug-ins, we distribute load between two machines based on even and odd "sessionIds"

### Procedure
1. On the first G Machine, in the input section for JDBC Plug-in, update the "statement" field as follows:
   ```sql
   select 
      audit_log.event_status,audit_log.client_ip,
      audit_log.connection_id,audit_log.client_port,
      SECONDS_BETWEEN('1970-01-01 00:00:00.00000',localtoutc(audit_log.timestamp)) as new_timestamp,
      audit_log.event_action,audit_log.user_name,
      audit_log.port,audit_log.client_host,
      audit_log.service_name,audit_log.statement_string,
      audit_log.application_name,audit_log.host,
      audit_log.application_user_name,
      M_DATABASE.database_name,M_DATABASE.system_id
   from
      M_DATABASE, audit_log
   where
      M_DATABASE.HOST = audit_log.HOST and audit_policy_name not in ('MandatoryAuditPolicy')
      and SECONDS_BETWEEN ('1970-01-01 00:00:00.00000', localtoutc(audit_log.timestamp)) > :sql_last_value
      and mod(connection_id, 2) = 0;
    ```
2. On the second G machine, in the input section for the JDBC Plug-in, update the  "statement" field as follows:
   ```sql
   select
     audit_log.event_status,audit_log.client_ip,
     audit_log.connection_id,audit_log.client_port,
     SECONDS_BETWEEN('1970-01-01 00:00:00.00000',localtoutc(audit_log.timestamp)) as new_timestamp,
     audit_log.event_action,audit_log.user_name,
     audit_log.port,audit_log.client_host,
     audit_log.service_name,audit_log.statement_string,
     audit_log.application_name,audit_log.host,
     audit_log.application_user_name,
     M_DATABASE.database_name,M_DATABASE.system_id
   from
     M_DATABASE, audit_log
   where
     M_DATABASE.HOST = audit_log.HOST and audit_policy_name not in ('MandatoryAuditPolicy')
     and SECONDS_BETWEEN ('1970-01-01 00:00:00.00000', localtoutc(audit_log.timestamp)) > :sql_last_value
     and mod(connection_id, 2) = 1;
   ```
