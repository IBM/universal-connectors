# SAP HANA-Guardium Logstash filter plug-in
### Meet SAP HANA
* Tested versions: 2.00.033.00.1535711040
* Environment: On-premise, Saas
* Supported Guardium versions:
    * Guardium Data Protection: 11.4 and above
        * Supported inputs:
            * Filebeat (push)
            * JDBC (pull)
    * Guardium Insights: 3.3
        * Supported inputs:
            * Filebeat (push)
    * Guardium Insights SaaS: 1.0
        * Supported inputs:
            * Filebeat (push)
            * JDBC (pull)

This is a [Logstash](https://github.com/elastic/logstash) filter plug-in for the universal connector that is featured in IBM Security Guardium.
It parses events and messages from the SAP HANA audit log into a [Guardium record](https://github.com/IBM/universal-connectors/blob/main/common/src/main/java/com/ibm/guardium/universalconnector/commons/structures/Record.java) instance (which is a standard
structure made out of several parts). The information is then sent over to Guardium. Guardium records include the
accessor (the person who tried to access the data), the session, data, and exceptions. If there are no errors, the data
contains details about the query "construct". The construct details the main action (verb) and
collections (objects) involved.

The plug-in is free and open-source (Apache 2.0). It can be used as a starting point to develop additional filter
plug-ins for Guardium universal connector.

## Configuring the SAP HANA

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


There are multiple ways to enable auditing in SAP HANA, You can choose as per your requirement.
* CSTABLE base auditing - Audit-trail target is a table, requires JDBC input plug-in.

  [SAPHANA Using JDBC Input](./saphanaUsingJDBCREADME.md)

  [SAPHANA Cloud Using JDBC Input](./saphanaCloudUsingJDBCREADME.md)

* CSVTEXTFILE base auditing - Audit-trail target is a file, requires Beat input plugin.

  [SAPHANA Using FILEBEAT Input](./saphanaUsingFilebeatREADME.md)

* SYSLOG base auditing -Audit-trail target is a syslog, requires syslog input plug-in.

  [SAPHANA Using SYSLOG Input](./saphanaUsingSyslogREADME.md)



## Troubleshooting

If you encounter an error like the following when trying to connect to the database:

```
2024-07-04 21:15:48 ERROR jdbc:127 - Unable to connect to database. Tried 1 times {:message=>"Java::ComSapDbJdbcExceptions::SQLInvalidAuthorizationSpecExceptionSapDB: [10]: authentication failed", :exception=>Sequel::DatabaseConnectionError, :cause=>#<Java::ComSapDbJdbcExceptions::SQLInvalidAuthorizationSpecExceptionSapDB: [10]: authentication failed>, :backtrace=>["com.sap.db.jdbc.exceptions.SQLExceptionSapDB._newInstance(com/sap/db/jdbc/exceptions/SQLExceptionSapDB.java:183)", "com.sap.db.jdbc.exceptions.SQLExceptionSapDB.newInstance(com/sap/db/jdbc/exceptions/SQLExceptionSapDB.java:42)", 
```

This error indicates an **authentication failure** when connecting to the database. It is often caused by issues with the provided credentials (username or password) or incorrect database configuration.

## Steps to resolve:

1. **Verify Database Credentials**  
   Check the **username** and **password** provided in your database connection configuration. Ensure that:
    - The username and password are correct.
    - The username has sufficient privileges to access the database.
    - The password is correctly formatted, without any trailing spaces or special characters that may cause issues.



3. **Test Credentials Manually**  
   Try connecting to the database manually using a database client or command line tool (e.g., SAP HANA Studio, `hdbsql`, or another SQL client). This can help verify that the credentials are valid and that the database is accessible.

4. **Check for Locked or Expired Accounts**  
   If you have confirmed the credentials are correct, check if the account is locked or if the password has expired. Some databases automatically lock accounts after multiple failed login attempts.


### Limitations:

1. SAP HANA auditing only supports error logs for authentication failures.
2. SAP HANA does not audit multiple line query properly.
3. Single line and multi-line comments in between the query are not supported.
4. The SAP HANA CSVTEXTFILE(audit target) does not audit the DB_Name.
5. SAP HANA with JDBC shows server ip as 0.0.0.0
6. Duplicate records will be seen in load balancing.
7. SAPHANA SYSLOG does not natively support load balancing.

## 5. Configuring the SAP HANA filters in Guardium

The Guardium universal connector is the Guardium entry point for native audit logs. The universal connector identifies and parses received events, and converts them to a standard Guardium format. The output of the Guardium universal connector is forwarded to the Guardium sniffer on the collector, for policy and auditing enforcements. Configure Guardium to read the native audit logs by customizing the SAP HANA template.

### Before you begin

* Configure the policies you require. See [policies](https://github.com/IBM/universal-connectors/tree/main/docs#policies) for more information.
* You must have permissions for the S-Tap Management role. The admin user includes this role, by default.

* Download the required (ngdbc)jars as per your database version from URL https://tools.hana.ondemand.com/#hanatools.

* This plug-in is automatically available with Guardium Data Protection. versions 12.x, 11.4 with appliance bundle 11.0p490 or later or Guardium Data Protection version 11.5 with appliance bundle 11.0p540 or later releases.

**Note:** For Guardium Data Protection version 11.4 without appliance bundle 11.0p490 or prior or Guardium Data Protection version 11.5 without appliance bundle 11.0p540 or prior, download the following plug-in. (Do not unzip the offline-package file throughout the procedure).
* For CSVTEXTFILE-based auditing, refer to this [package](SaphanaOverFilebeatPackage) and download the [logstash-filter-saphana_guardium_plugin_filter.zip](SaphanaOverFilebeatPackage/SAPHANA/SAPHANA-offline-plugin.zip) plug-in. (Do not unzip the offline-package file throughout the procedure).This step is not necessary for Guardium Data Protection v12.0 and later.
* For CSTABLE based auditing, refer to this [package](SaphanaOverJdbcPackage) and download the [logstash-filter-saphana_guardium_plugin_filter.zip](SaphanaOverJdbcPackage/SAPHANA/SAPHANA-offline-plugin.zip) plug-in. (Do not unzip the offline-package file throughout the procedure).This step is not necessary for Guardium Data Protection v12.0 and later.


# Procedure
1. On the collector, go to Setup > Tools and Views > Configure Universal Connector.
2. First enable the Universal Guardium connector, if it is disabled already.
3. For CSVTEXTFILE-based auditing, follow these steps:-
    1. Click "Upload File" and select the [logstash-filter-saphana_guardium_plugin_filter.zip](SaphanaOverFilebeatPackage/SAPHANA/SAPHANA-offline-plugin.zip) plug-in as per specific audit. After it is uploaded, click "OK". This step is not necessary for Guardium Data Protection v11.0p490 or later, v11.0p540 or later, v12.0 or later.
    2. Click the Plus sign to open the Connector Configuration dialog box.
    3. Type a name in the Connector name field.
    4. Update the input section. Use the [saphanaFilebeat.conf](https://github.com/IBM/universal-connectors/raw/main/filter-plugin/logstash-filter-saphana-guardium/saphanaFilebeat.conf) file's input part, omitting the keyword "input{" at the beginning and its corresponding "}" at the end.
    5. Update the filter section. Use the [saphanaFilebeat.conf](https://github.com/IBM/universal-connectors/raw/main/filter-plugin/logstash-filter-saphana-guardium/saphanaFilebeat.conf) file's filter part, omitting the keyword "filter{" at the beginning and its corresponding "}" at the end.
    6. The "type" field should match in the input and filter configuration section. This field should be unique for every individual connector added.
    7. Click "Save". Guardium validates the new connector, and enables the universal connector if it was disabled. After it is validated, it appears in the Configure Universal Connector page.

4. For CSTABLE-based auditing, follow these steps:
    1. Click "Upload File" and select the offline [SAPHANA-offline-plugin.zip](SaphanaOverJdbcPackage/SAPHANA/SAPHANA-offline-plugin.zip) plug-in as per specific audit. After it is uploaded, click "OK". This step is not necessary for Guardium Data Protection v12.0 and later.
    2. Click "Upload File" again and select the ngdbc-2.9.12 jar file. After it is uploaded, click "OK".
    3. Click the Plus sign to open the Connector Configuration dialog box.
    4. Type a name in the Connector name field.
    5. Update the input section . Use the [saphanaJDBC.conf](https://github.com/IBM/universal-connectors/raw/main/filter-plugin/logstash-filter-saphana-guardium/saphanaJDBC.conf) file's input part, omitting the keyword "input{" at the beginning and its corresponding "}" at the end.
    6. Update the filter section for JDBC Plugin. Use the [saphanaJDBC.conf](https://github.com/IBM/universal-connectors/raw/main/filter-plugin/logstash-filter-saphana-guardium/saphanaJDBC.conf) file's filter part, omitting the keyword "filter{" at the beginning and its corresponding "}" at the end.
    7. The "type" fields should match in the input and the filter configuration sections. This field should be unique for every individual connector added.
    8. If using two jdbc plug-ins on same machine , the last_run_metadata_path file name should be different.

       **Note: For moderate to large amounts of data, include pagination to facilitate the audit and to avoid out of memory errors.  Use the parameters below in the input section when using a JDBC connector, and remove the concluding semicolon ';' from the jdbc statement:**
        ```
       jdbc_paging_enabled => true 
       jdbc_page_size => 1000
       ```
    9. Click Save. Guardium validates the new connector, and enables the universal connector if it was disabled. After it is validated, it appears in the Configure Universal Connector page.

## 6. JDBC Load Balancing Configuration

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

## 5. Configuring the SAP HANA filters in Guardium Data Security Center
To configure this plug-in for Guardium Data Security Center, follow [this guide.](/docs/Guardium%20Insights/3.2.x/UC_Configuration_GI.md)
For the input configuration step, refer to the [Filebeat section](/docs/Guardium%20Insights/3.2.x/UC_Configuration_GI.md#Filebeat-input-plug-in-configuration).
