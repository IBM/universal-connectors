## Configuring the SAP HANA Logstash filter plug-in using Syslog input

There are multiple ways to install a SAP HANA server. For this example, we will assume that we already have a working
SAP HANA setup.

## Enabling the audit logs:
### Procedure
In the SAP HANA Studio, expand the system on which you would like to enable auditing.
1. Expand the Security folder.
2. Double click on the ‘Security option’.
3. Click on the auditing status drop-down menu, by default it will be disabled.
4. Select "Enabled".
5. Click "Deploy" or press F8 to save the changes.
6. Restart database instance to reflect new changes.


Here we will check about Syslog base auditing.
* SYSLOG base auditing - Audit-trail target is a syslog, requires syslog input plug-in.

### Enabling SYSLOG base auditing logs:
Configure syslogprotocol audit trail on Saphana instance, on T1 database:
1. In SAPAHANA Studio open the configuration and set the System and Database audit trail to “SYSLOGPROTOCOL”.
2. If Syslog protocol configuration cannot be applied on SapHANA tenant database via SapHana studio GUI, follow below steps:
3. For multiple container tenant database you can enable auditing for CSV File target by using the following command.
   *. Connect with ssh to saphana dbserver.
   *. Open /hana/shared/H05/global/hdb/custom/config/DB_T1/global.ini for editing.
   *. Set like in example below:
     ```
   * # global.ini last modified 2022-09-29 11:07:44.524763 by hdbindexserver -port 30040
   [auditing configuration]
   global_auditing_state = true
   default_audit_trail_type = SYSLOGPROTOCOL
   sr_audit_trail_type_cstable_override = SYSLOGPROTOCOL
   
     ```

2. After running the previous command, restart the container and refresh the added systems.

## Common steps for either auditing type

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

      **Note: For this policy, select "Audited Action Status=ALL".**

2. For audit DML logs, select the following audited action:
    - Check the Data Query and Manipulation checkbox from the audited action tab.

      **Note: For this policy, select "Audited Action  Status=Successful".**

      **Note: It is mandatory to provide a “Target Object” for this policy.**

3. For audit DDL Logs, select the following audited action:
    - Check the  “Create Table, Create Function, Create Procedure, Drop Function, Drop Procedure, Drop Table” checkbox
      from the data definition menu in the audited action table.

      **Note: For this policy, select "Audited Action Status=Successful".**

## Viewing the audit logs

### View the SAP HANA audit logs for SYSLOG auditing.

#### Procedure

1. Connect to the machine where the database is.
2. Run below command.
   ```
   cat /var/log/secure
   
   ```
## Configuring Syslogs to push logs to Guardium
## Syslogs configuration:
To make the Logstash able to process the data collected by syslogs, we need to configure available syslog utility.

The example is based on `rsyslog` utility available in many versions of the Linux distributions.

1. To check the service is active and running,Run below command:
   `systemctl status rsyslog.service`

2. Open rsyslog file using command `nano /etc/rsyslog.conf` add the values of `ipaddress` and `port` of Guardium machine where data needs to be sent.

3. Restart the `rsyslog` utility once above file is updated and make sure status is running.
   `systemctl restart rsyslog.service`

## Configuring the SAP HANA filters in Guardium

The Guardium universal connector is the Guardium entry point for native audit logs. The universal connector
identifies and parses received events, and converts them to a standard Guardium format. The output of the
Guardium universal connector is forwarded to the Guardium sniffer on the collector, for policy and auditing
enforcements. Configure Guardium to read the native audit logs by customizing the SAP HANA template.

### Before you begin

* You must have permissions for the S-Tap Management role. The admin user includes this role, by default.
* Download the required (ngdbc)jars as per your database version from URL https://tools.hana.ondemand.com/#hanatools.
* For SYSLOG based auditing, refer to this [Package](./SaphanaOverSyslogPackage) and download the [SAPHANA-offline-plugin.zip](SaphanaOverJdbcPackage/SAPHANA-offline-plugin.zip) plug-in.


# Procedure
1. On the collector, go to Setup > Tools and Views > Configure Universal Connector.
2. First enable the Universal Guardium connector, if it is disabled already.
3. Click "Upload File" and select the offline [SAPHANA-offline-plugin.zip](SaphanaOverJdbcPackage/SAPHANA-offline-plugin.zip) plug-in as per specific audit. After it is uploaded, click "OK".
4. Click "Upload File" again and select the ngdbc-2.9.12 jar file. After it is uploaded, click "OK".
5. Click the Plus sign to open the Connector Configuration dialog box.
6. Type a name in the Connector name field.
7. Update the input section . Use the [saphanaSyslog.conf](SaphanaOverSyslogPackage/saphanaSyslog.conf) file's input part, omitting the keyword "input{" at the beginning and its corresponding "}" at the end.
8. Update the filter section for JDBC Plugin. Use the [saphanaSyslog.conf](SaphanaOverSyslogPackage/saphanaSyslog.conf) file's filter part, omitting the keyword "filter{" at the beginning and its corresponding "}" at the end.
7. The "type" fields should match in the input and the filter configuration sections. This field should be unique for every individual connector added. This is no longer required starting v12p20 and v12.1.
8. Click Save. Guardium validates the new connector, and enables the universal connector if it was disabled.
   After it is validated, it appears in the Configure Universal Connector page.