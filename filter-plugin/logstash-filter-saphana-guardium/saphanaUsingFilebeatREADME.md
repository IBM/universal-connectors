## Configuring the SAP HANA Logstash filter plug-in using Filebeat input

There are multiple ways to install a SAP HANA server. For this example, we will assume that we already have a working
SAP HANA setup.

## Enabling the audit logs:
Here we will check about CSVTEXTFILE base auditing.
* CSVTEXTFILE base auditing - Audit-trail target is a file, requires Beat input plugin.

### Enabling CSVTEXTFILE base auditing logs:

To perform the below steps, open SAP HANA studio (Eclipse) Select SYSTEMDB user, right-click,  select SQL Console,
and then run these Commands:

1. For multiple container tenant database you can enable auditing for CSV File target by using the following command
    1. SAP HANA Command For Enable Auditing:
       ```
       ALTER SYSTEM ALTER CONFIGURATION ('global.ini', 'system') 
       set ('auditing configuration', 'global_auditing_state') = 'true';
       ``` 
    2. To select the target as a CSV text file, use the following command
       ```
       ALTER SYSTEM ALTER CONFIGURATION ('global.ini', 'system') 
       set ('auditing configuration', 'default_audit_trail_type') = 'CSVTEXTFILE'; 
       ```
    3. To avoid unwanted system logs use below command to store all system logs in table.
       ```
       ALTER SYSTEM ALTER CONFIGURATION ('global.ini', 'system') 
       set ('auditing configuration', 'critical_audit_trail_type') = 'CSTABLE';
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

      **Note: For this policy, select "Audited Action Status=Unsuccessful".**

2. For audit DML logs, select the following audited action:
    - Check the Data Query and Manipulation checkbox from the audited action tab.

      **Note: For this policy, select "Audited Action  Status=Successful".**

      **Note: It is mandatory to provide a “Target Object” for this policy.**

3. For audit DDL Logs, select the following audited action:
    - Check the  “Create Table, Create Function, Create Procedure, Drop Function, Drop Procedure, Drop Table” checkbox
      from the data definition menu in the audited action table.

      **Note: For this policy, select "Audited Action Status=Successful".**

## Viewing the audit logs

### View the SAP HANA audit logs for CSVTEXTFILE-based auditing.

#### Procedure
1. To view the audited Logs:
    1. GOTO Location /usr/sap/host-server/instance/*.audit_trail.csv
2. To view the logs entries on Studio:
    1. Click on HXE system.
    2. GOTO administrative Console.
    3. Click on the diagnostic file tab.
    4. Click on filter and Search for “*.audit_trail.csv”

## Configuring Filebeat to push logs to Guardium

This configuration is required to pull logs when CSVTEXTFILE-based auditing is enabled.

## Filebeat installation

### Procedure:

1. To install Filebeat on your system, follow the steps in this topic:
   https://www.elastic.co/guide/en/beats/filebeat/current/filebeat-installation-configuration.html#installation

## Filebeat configuration:

To use Logstash to perform additional processing on the data collected by Filebeat, we need
to configure Filebeat to use Logstash. To do this, modify the filebeat.yml file which you can find inside the
folder where Filebeat is installed. Follow these instructions for finding the installation directory:
https://www.elastic.co/guide/en/beats/filebeat/current/directory-layout.html

### Procedure:
1. Configuring the input section :-
    * Locate "filebeat.inputs" in the filebeat.yml file, then add the following parameters.
       ```
       filebeat.inputs:
         - type: filestream
         - id: <ID>
           enabled: true
           paths:
             - <host_name/trace/DB_<DB_Name>/*.audit_trail.csv>
       ```
    * Add the tags to uniquely identify the SAP HANA events from the rest.
       ```
       tags : ["sapHana"]
       ```

2. Configuring the output section:
    1. Locate "output" in the filebeat.yml file, then add the following parameters.
    2. Disable Elasticsearch output by commenting it out.
    3. Enable Logstash output by uncommenting the Logstash section. For more information,
       see https://www.elastic.co/guide/en/beats/filebeat/current/logstash-output.html#logstash-output
    4. For example:
       ```
       output.logstash:
       hosts: ["<host>:<port>"]
       ```
    5. The hosts option specifies the Logstash server and the port (5001) where Logstash is configured
       to listen for incoming Beats connections.
    6. You can set any port number except 5044, 5141, and 5000 (as these are currently
       reserved in Guardium v11.3 and v11.4 ).
    7. Locate "Processors" in the filebeat.yml file and then add the below attribute to get the server's
       time zone.For more information, see https://www.elastic.co/guide/en/beats/filebeat/current/add-locale.html
    8. For example:
       ```
       processors:
         - add_locale: ~
       ```
    9. To learn how to start FileBeat,
       see https://www.elastic.co/guide/en/beats/filebeat/current/filebeat-installation-configuration.html#start

## Configuring the SAP HANA filters in Guardium

The Guardium universal connector is the Guardium entry point for native audit logs. The universal connector
identifies and parses received events, and converts them to a standard Guardium format. The output of the
Guardium universal connector is forwarded to the Guardium sniffer on the collector, for policy and auditing
enforcements. Configure Guardium to read the native audit logs by customizing the SAP HANA template.

### Before you begin

* You must have permissions for the S-Tap Management role. The admin user includes this role, by default.
* For CSVTEXTFILE-based auditing, refer to this [package](SaphanaOverFilebeatPackage) and download the [SAPHANA-offline-plugin.zip](SaphanaOverFilebeatPackage/SAPHANA/SAPHANA-offline-plugin.zip) plug-in.

# Procedure
1. On the collector, go to Setup > Tools and Views > Configure Universal Connector.
2. First enable the Universal Guardium connector, if it is disabled already.
3. Click "Upload File" and select the [SAPHANA-offline-plugin.zip](SaphanaOverFilebeatPackage/SAPHANA/SAPHANA-offline-plugin.zip) plug-in as per specific audit. After it is uploaded, click "OK".
4. Click the Plus sign to open the Connector Configuration dialog box.
5. Type a name in the Connector name field.
6. Update the input section. Use the [saphanaFilebeat.conf](saphanaFilebeat.conf) file's input part, omitting the keyword "input{" at the beginning and its corresponding "}" at the end.
7. Update the filter section. Use the [saphanaFilebeat.conf](saphanaFilebeat.conf) file's filter part, omitting the keyword "filter{" at the beginning and its corresponding "}" at the end.
8. The "type" fields should match in the input and the filter configuration sections. This field should be unique for every individual connector added. This is no longer required starting v12p20 and v12.1.
9. Click "Save". Guardium validates the new connector, and enables the universal connector if it was
   disabled. After it is validated, it appears in the Configure Universal Connector page.