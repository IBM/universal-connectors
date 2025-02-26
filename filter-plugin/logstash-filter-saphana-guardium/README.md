# SAP HANA-Guardium Logstash filter plug-in
### Meet SAP HANA
* Tested versions: 2.00.033.00.1535711040
* Environment: On-premise, Iaas
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

## 1. Configuring the SAP HANA

There are multiple ways to install a SAP HANA server. For this example, we will assume that we already have a working
SAP HANA setup.

## 2. Enabling the audit logs:
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

### Steps to resolve:

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