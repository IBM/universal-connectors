# Teradata-Guardium Logstash filter plug-in
### Meet Teradata

* Tested versions: 16.2, 17.2 and 20.0
* Environment: On-premises, VCL on AWS and Azure, VCE on Azure, AWS and GCP.
* Supported inputs: JDBC (pull)
* Supported Guardium versions:
	* Guardium Data Protection: 11.4 and later

This is a [Logstash](https://github.com/elastic/logstash) filter plug-in for the universal connector that is featured in IBM Security Guardium. It parses events and messages from the Teradata audit log into a [Guardium record](https://github.com/IBM/universal-connectors/raw/main/common/src/main/java/com/ibm/guardium/universalconnector/commons/structures/Record.java) instance (which is a standard structure comprised of several parts). The information is then sent over to Guardium. Guardium records include the accessor (the person who tried to access the data), session, data, and exceptions. If there are no errors, the data contains details about the query "construct". The construct details the main action (verb) and collections (objects) involved.

The plug-in is free and open-source (Apache 2.0). It can be used as a starting point to develop additional filter plug-ins for Guardium universal connector.


## 1. Configuring the Teradata server

There are multiple ways to install a Teradata server. For this example, we will assume that we already have a working Teradata server setup.

## 2. Enabling Auditing
1. Connect to the Teradata server using SSH.

2. Login with the dbc user (or any other user) of teradata that has access to DBQLAccessMacro using bteq. The commands to log in with dbc user commands are:
   ```bteq .logon <server-name>/dbc,<password>```

In the above command, give the password for the dbc user.

4. Create a user to read logs from audit tables through the logstash JDBC input plug-in.

   	CREATE USER <username> AS  PERMANENT = 100000000 BYTES   PASSWORD = "<password>"	 

5.  To grant read access of objects inside dbc user to above created user, execute the below command:-

		GRANT SELECT ON "dbc" TO "<username>";

6.  There are mutiple ways to enable DBQL Query Logging. Query logging includes a variety of table/view combinations in the DBC database. Logging for users, accounts, and applications should be used when it is really required. Query logging can be enabled for all users or for specific users.

To enable Auditing, use a command like this one:

	BEGIN QUERY LOGGING WITH SQL LIMIT SQLTEXT=0 ON ALL;

DBQL Query Logging can be explored further in this [document](https://docs.teradata.com/r/qOek~PvFMDdCF0yyBN6zkA/f7yJJ4siIiBUpoQVvvAwpQ).

6.  Type "exit;" to get out of bteq terminal.

7.  Set the database time zone with two dbscontrol fields:

	"18. System TimeZone String" must be set to the timezone which we want to configure for our database.
	"57. TimeDateWZControl " must be set to 2.

8.  Close the terminal.

9.  We can verify that a logging rule is created in a table. Log in with dbc user. You can choose to trigger the below query through any client utility.
	In this case we are using Teradata Studio Express.

		select * from DBC.DBQLRulesV;	
### Notes:
For **Teradata VCE on Azure** or **Teradata VCL on AWS**, run the following query to turn the audit log on.
```
BEGIN QUERY LOGGING WITH SQL LIMIT SQLTEXT=0 ON ALL;
```
To check if the audit log is on/off, run:
```
select * from DBC.DBQLRulesV;
```
## 3. Steps to disable Auditing
Auditing can be disabled similarly to how we enabled auditing by logging in with dbc user or any other user that has access to DBQLAccessMacro.

To disable query logging, execute the below command:-

	END QUERY LOGGING WITH SQL LIMIT SQLTEXT=0 ON ALL;


## 4. Archiving and Deleting DBQL Logs

There are many ways to archive and delete DBQL logs. One of the ways is depicted below:-

Use the following steps to delete old log data from system tables manually:

It is recommended, though not necessary, to disable DBQL logging before you perform clean up activities on the logs. Otherwise, the delete process locks the DBQL table and if DBQL needs to flush a cache to the same table to continue logging queries, the whole system could experience a slow-down.

***Note: You cannot delete data that is less than 30 days old.***

1. To back up log data
   a. Create a duplicate log table in another database using the Copy Table syntax for the CREATE TABLE statement.
   CT DBC.tablename AS databasename.tablename
   b. Back up the table to tape storage in accordance with your site backup policy.
   c. Drop the duplicate table using a DROP TABLE statement.
2. Log on to Teradata Studio as DBADMIN or another administrative user with DELETE privileges on database DBC.
3. In the Query window, enter an SQL statement to purge old log entries. For example:

   DELETE FROM DBC.object_name WHERE (Date - LogDate) > number_of_days ;

Examples for using above query:-
DELETE FROM DBC.DBQLOGTBL WHERE (DATE '2021-12-16' - cast(starttime as DATE)) > 30 ;
DELETE FROM DBC.DBQLSqlTbl WHERE (DATE '2021-12-16' - cast(collecttimestamp as DATE)) > 30 ;


#### Limitations:

• Teradata sniffer parser does not parse below listed operations properly. Hence this plug-in does not support these operations:

1] User Management

2] DBQL Queries

3] Timestamp configuration

4] Cast operations

5] Stored Procedure and User Defined Functions

6] DBQL tables do not capture 100% of Teradata workload due to configurable filters, aggregation options, memory caching with periodic writes (e.g., every 10 minutes via DBQLFlushRate), and potential data loss during system restarts before cache flush. Reference: https://www.dwhpro.com/teradata-query-logging-dbql/

• The Teradata auditing does not audit authentication failure(Login Failed) operations.

• Following important field couldn't mapped with TeradataDB audit logs.

1] Client HostName : Not Available with audit logs.

2] Database Name : Not Available with audit logs.

• In case of EC2 guardium instance, Teradata traffic took more time (25-30 min) to populate data in full sql Report.

• This plug-in supports queries that are approximately 32,000 characters long. When the count of characters in a query exceed the given count, the remaining part of the query is stored in other rows. This is why the SQLTextInfo column of the table DBC.DBQLSqlTbl has more than one row per QueryID.

• Client IP and Server IP are retrieved from DBC.QryLogClientAttrV view using ClientIPAddrByClient and ServerIPAddrByServer fields respectively, as recommended by Teradata support. The deprecated logonsource field is no longer used for IP address retrieval.

For more information on DBC.QryLogClientAttrV, please refer to this [documentation](https://docs.teradata.com/r/Enterprise_IntelliFlex_VMware/Data-Dictionary/Views-Reference/QryLogClientAttrV).

## 5. Configuring the Teradata filters in Guardium

The Guardium universal connector is the Guardium entry point for native audit logs. The universal connector identifies and parses received events, and then converts them to a standard Guardium format. The output of the universal connector is forwarded to the Guardium sniffer on the collector, for policy and auditing enforcements. Configure Guardium to read the native audit logs by customizing the Teradata template.

#### Before you begin

•  Configure the policies you require. See [policies](/docs/#policies) for more information.

• You must have permission for the S-Tap Management role. The admin user includes this role by default.


• Download the [logstash-filter-teradatadb_guardium_plugin_filter.zip](./TeradataOverJdbcPackage/logstash-filter-teradatadb_guardium_plugin_filter.zip) plug-in. (Do not unzip the offline-package file throughout the procedure).

• Download driver jar - Go to the URL https://downloads.teradata.com/download/connectivity/jdbc-driver and download the zip/tar for required version. After extracting the downloaded zip/tar, there will be a jar file.


#### Procedure:

1. On the collector, go to Setup > Tools and Views > Configure Universal Connector.
2. First enable the Universal Guardium connector, if it is disabled already.
3. Click ```Upload File``` and upload the jar/jars which you downloaded from the teradata website.
4. Click ```Upload File``` and select the offline [logstash-filter-teradatadb_guardium_plugin_filter.zip](./TeradataOverJdbcPackage/logstash-filter-teradatadb_guardium_plugin_filter.zip) plug-in. After it is uploaded, click ```OK```. This is not necessary for Guardium Data Protection v11.0p490 or later, v11.0p540 or later, v12.0 or later.
5. Click the Plus sign to open the ```Connector Configuration``` dialog box.
6. Type a name in the Connector name field.
7. Update the input section to add the details from the [teradataJDBC.conf](./TeradataOverJdbcPackage/teradataJDBC.conf) file's input part, omitting the keyword "input{" at the beginning and its corresponding "}" at the end. Provide the details for database server name, username, and password that are required for connecting with JDBC.
8. Update the filter section to add the details from the [teradataJDBC.conf](./TeradataOverJdbcPackage/teradataJDBC.conf) file's filter part, omitting the keyword "filter{" at the beginning and its corresponding "}" at the end. Provide the same database server name that you gave in the above step against the Server_Hostname attribute in the filter section.
9. The "type" fields should match in the input and the filter configuration sections. This field should be unique for every individual connector added.
10. If you are using two JDBC plug-ins on the same machine, the last_run_metadata_path file name should be different.
11. Click ```Save```. Guardium validates the new connector, and enables the universal connector if it was disabled. After it is validated, the connector appears in the ```Configure Universal Connector``` page.

#### Note:

•  ```Records Affected``` column has a valid value for the Select Queries only, for other type of queries it is set to -1.

•  If there is a requirement of having the number of Records Affected from a Select Query in a FULL SQL report, follow the below steps :

#### Procedure:
1. On the collector, go to Activity Monitoring > Inspection Engines.
2. Check Default capture value, Log Records Affected and Inspect Returned data checkboxes, if unchecked already.
3. Click on Apply.
4. Click on Restart Inspection Engines.
5. To add the column in the Full SQL Report, select the Attribute ```Records Affected``` listed under the Entity  ```FULL SQL```.
