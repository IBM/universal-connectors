## 1. Configuring SAP HANA Logstash filter plug-in using JDBC input

SAP HANA server can be installed in various ways. For this example, you must have a functional SAP HANA configuration.
### Audit Log Configuration with a new User
To create a user for auditing the log configurations, complete the following steps.

**Note**: You can also use system user to audit log configurations.

1. To create a new user, run the following command.
   ```sql
   CREATE USER your_username PASSWORD 'your_password';
   ```
2. Assign the required audit privileges to the user.
   ```sql
   GRANT AUDIT ADMIN TO your_username;
   ```
3. Commit the changes to the database.
   ```sql
   COMMIT;
   ```
Ensure that all the changes are committed to the database.

## 2. Enabling the audit logs:
### Procedure
In the SAP HANA Studio, expand the system on which you would like to enable auditing.
1. Expand the Security folder.
2. Double click on the ‘Security option’.
3. Click on the auditing status drop-down menu, by default it will be disabled.
4. Select "Enabled".
5. Click "Deploy" or press F8 to save the changes.
6. Restart database instance to reflect new changes.


Here we will check about CSTABLE base auditing.
* CSTABLE base auditing - Audit-trail target is a table, requires JDBC input plug-in.

### Enabling CSTABLE base auditing logs:
To perform the below steps, open SAP HANA studio (Eclipse) Select SYSTEMDB user, right-click,  select SQL Console,
and then run these Commands:

1. For multiple container tenant database you can enable auditing for CSV File target by using the following command
    1. SAP HANA Command For Enable Auditing:
       ```
       ALTER SYSTEM ALTER CONFIGURATION ('global.ini', 'system')
       set ('auditing configuration', 'global_auditing_state') = 'true';
       ```
    2. To select the target as a table, use the following command
       ```
       ALTER SYSTEM ALTER CONFIGURATION ('global.ini', 'system')
       set ('auditing configuration', 'default_audit_trail_type') = 'CSTABLE';
       ```
    3. To avoid unwanted system logs use below command to store all system logs in table.
       ```
       ALTER SYSTEM ALTER CONFIGURATION ('global.ini', 'system')
       set ('auditing configuration', 'critical_audit_trail_type') = 'CSVTEXTFILE';
       ```

2. After running the previous command, restart the container and refresh the added systems.

## 3. Common steps for either auditing type

### Creating an audit policy

An audit policy defines the actions to be audited. In order to create an audit policy, the user must have
AUDIT ADMIN system rights. Creating an audit policy is a common step for both types of auditing.


#### Procedure

1. In the SAP HANA Studio, expand the database:
2. Expand the ‘Security’ folder.
3. Double-click on the ‘Security option’.
4. Click the green plus sign under the ‘Audit Polices’ panel.
5. Enter your policy name.
6. Click in the Audited actions field and then press the ‘…’
7. Select the actions you would like to audit.
8. Select when an audit record should be created in the ‘Audited actions status’ column.
    1. SUCCESSFUL – When an action is successfully executed, it is logged
    2. UNSUCCESSFUL	- When an action is unsuccessfully executed, it is logged
    3. ALL	 - Both of the above situations are logged.
9. Select the audit level:
    * EMERGENCY
    * CRITICAL
    * ALERT
    * WARNING
    * INFO (default)

10. If needed, you can filter the users you would like to audit. Under the users column, you can press the ‘…’ button
    and then add users.

11. You can also specify the target object(s) to be audited. This option is valid if the actions to be audited
    involve SELECT, INSERT, UPDATE, DELETE.

12. Once done press the deploy button or press F8
    * Reboot the database instance for the changes to take place.

### Sap Hana Auditing Policy

1. For Audit session-related logs (Connect, Disconnect, Validation Of User), select the following audited actions:
    - Check the “Connect” checkbox from session management and the  system configuration menu in the audited action tab.

      **Note: For this policy, select "Audited Action Status=Unsuccessful".**

2. For audit DML logs, select the following audited action:
    - Check the Data Query and Manipulation checkbox from the audited action tab.

      **Note: For this policy, select "Audited Action  Status=Successful".**

      **Note: It is mandatory to provide a “Target Object” for this policy.**

3. For audit DDL Logs, select the following audited action:
    - Check the  “Create Table, Create Function, Create Procedure, Drop Function, Drop Procedure, Drop Table” checkbox
      from the data definition menu in the audited action table.

      **Note: For this policy, select "Audited Action Status=Successful".**

## 4.  Viewing the audit logs

### View the SAP HANA audit logs for CSTABLE-based auditing.

#### Procedure

1. Connect to the database.
2. Right-click on it.
3. Open the SQL console and run the following command:
   ```sql
     select * from AUDIT_LOG;
   ```

## 5. Configuring the SAP HANA filters in Guardium

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
7. Update the input section . Use the [saphanaJDBC.conf](SaphanaOverJdbcPackage/saphanaJDBC.conf) file's input part, omitting the keyword "input{" at the beginning and its corresponding "}" at the end.
8. Update the filter section for JDBC Plugin. Use the [saphanaJDBC.conf](SaphanaOverJdbcPackage/saphanaJDBC.conf) file's filter part, omitting the keyword "filter{" at the beginning and its corresponding "}" at the end.
7. The "type" fields should match in the input and the filter configuration sections. This field should be unique for every individual connector added. This is no longer required starting v12p20 and v12.1.
8. If using two jdbc plug-ins on same machine , the last_run_metadata_path file name should be different.

   **Note: For moderate to large amounts of data, include pagination to facilitate the audit and to avoid out
   of memory errors.  Use the parameters below in the input section when using a JDBC connector, and remove the
   concluding semicolon ';' from the jdbc statement:**
    ```
   jdbc_paging_enabled => true
   jdbc_page_size => 1000
   ```
9. Click Save. Guardium validates the new connector, and enables the universal connector if it was disabled.
   After it is validated, it appears in the Configure Universal Connector page.

## 6. JDBC Load Balancing Configuration

In SAP HANA JDBC input plug-ins , we distribute load between two machines based on even and odd "sessionIds"

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