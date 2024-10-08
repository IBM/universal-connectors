# SAP HANA-Guardium Logstash filter plug-in

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

  
* CSVTEXTFILE base auditing - Audit-trail target is a file, requires Beat input plugin.

  [SAPHANA Using FILEBEAT Input](./saphanaUsingFilebeatREADME.md)

  
### Limitations:

1. SAP HANA auditing only supports error logs for authentication failures.
2. SAP HANA does not audit multiple line query properly.
3. Single line and multi-line comments in between the query are not supported.
4. The SAP HANA CSVTEXTFILE(audit target) does not audit the DB_Name.
5. SAP HANA with JDBC shows server ip as 0.0.0.0
6. Duplicate records will be seen in load balancing.