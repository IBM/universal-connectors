# Teradata-Guardium Logstash filter plug-in
### Meet Teradata
* Tested versions: 16.20, 17.5
* Environment: Cloud, On-premise
* Supported inputs: JDBC (pull)
* Supported versions:
    * Guardium Data Protection: 11.4 and above

This is a [Logstash](https://github.com/elastic/logstash) filter plug-in for the universal connector that is featured in IBM Security Guardium. It parses events and messages from the Teradata audit log into a [Guardium record](https://github.com/IBM/universal-connectors/blob/main/common/src/main/java/com/ibm/guardium/universalconnector/commons/structures/Record.java) instance (which is a standard structure comprised of several parts). The information is then sent over to Guardium. Guardium records include the accessor (the person who tried to access the data), session, data, and exceptions. If there are no errors, the data contains details about the query "construct". The construct details the main action (verb) and collections (objects) involved.

The plug-in is free and open-source (Apache 2.0). It can be used as a starting point to develop additional filter plug-ins for Guardium universal connector.


## 1. Configuring the Teradata server

There are multiple ways to install a Teradata server. For this example, we will assume that we already have a working Teradata server setup.

## 2. Enabling Auditing

	• Connect to the Teradata server using SSH.
  
	• Login with the dbc user (or any other user) of teradata that has access to DBQLAccessMacro using bteq. The commands to log in with dbc user commands are:-
		bteq .logon <server-name>/dbc,<password>
		
		in the above command, give the password for the dbc user.	
		
	• Create a user to read logs from audit tables through the logstash JDBC input plug-in.

		CREATE USER <username> AS  PERMANENT = 100000000 BYTES   PASSWORD = "<password>"	 
		
	• To grant read access of objects inside dbc user to above created user, execute the below command:-
				
		GRANT SELECT ON "dbc" TO "<username>";
	
	• There are mutiple ways to enable DBQL Query Logging. Query logging includes a variety of table/view combinations in the DBC database. Logging for users, accounts, and applications should be used when it is really required. Query logging can be enabled for all users or for specific users.  
	
	To enable Auditing here we are using command i.e.  
	
	BEGIN QUERY LOGGING WITH SQL LIMIT SQLTEXT=0 ON ALL;
		
	DBQL Query Logging can be explored further in this document:- https://docs.teradata.com/r/qOek~PvFMDdCF0yyBN6zkA/f7yJJ4siIiBUpoQVvvAwpQ
		
	• Type "exit;" to get out of bteq terminal. 
	
	• Set the database time zone with two dbscontrol fields:
	
	"18. System TimeZone String" must be set to the timezone which we want to configure for our database.
	"57. TimeDateWZControl " must be set to 2.
	
	• Close the terminal.	
		
	• We can verify that a logging rule is created in a table. Log in with dbc user. You can choose to trigger the below query through any client utility.
	In this case we are using Teradata Studio Express.
	
		select * from DBC.DBQLRulesV;	
	
## 3. Steps to disable Auditing
	Auditing can be disabled similarly to how we enabled auditing by logging in with dbc user or any other user that has access to DBQLAccessMacro.
	
	To disable query logging, execute the below command:-
		
	END QUERY LOGGING WITH SQL LIMIT SQLTEXT=0 ON ALL;
	

## 4. Archiving and Deleting DBQL Logs	

	There are many ways to archive and delete DBQL logs. One of the ways is depicted below:-
	
	Use the following steps to delete old log data from system tables manually:
	
	It is recommended, though not necessary, to disable DBQL logging before you perform clean up activities on the logs. Otherwise, the delete process locks the DBQL table and if DBQL needs to flush a cache to the same table to continue logging queries, the whole system could experience a slow-down.
	
	Note: You cannot delete data that is less than 30 days old.
	
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
	
	• Teradata sniffer parser does not parse below listed operations properly,Hence this plug-in does not support below operations:-
		1] User Management 
		2] DBQL Queries.
		3] Timestamp configuration.
		4] Cast operations.
		5] Stored Procedure and User Defined Functions.
	• The Teradata auditing does not audit authentication failure(Login Failed) operations.
	• Following important field couldn't mapped with TeradataDB audit logs. 
		- OS USER : Not Available with audit logs.
		- Client HostName : Not Available with audit logs.
		- Database Name	: Not Available with audit logs.
	• In case of EC2 guardium instance, Teradata traffic took more time (25-30 min) to populate data in full sql Report. 
	• This plug-in supports queries that are approximately 32,000 characters long. When the count of characters in a query exceed the given count, the remaining part of the query is stored in other rows. This is why the SQLTextInfo column of the table DBC.DBQLSqlTbl has more than one row per QueryID.
	• serverIp is hardcoded to "0.0.0.0" in this plugin, as tables referred in configuration file do not have an attribute that directly holds actual serverIp value but that can be checked from column LogonSource(from DBC.DBQLOGTBL ) or sourceProgram  attribute.
For more information on how to check the serverIp from LogonSource, please refer this [doc](https://docs.teradata.com/r/ANYCOtbX9Q1iyd~Uiok8gA/VPQKKhAyOf6hzUc4sfciIQ)

## 5. Configuring the Teradata filters in Guardium

	The Guardium universal connector is the Guardium entry point for native audit logs. The universal connector identifies and parses received events, and then converts them to a standard Guardium format. The output of the universal connector is forwarded to the Guardium sniffer on the collector, for policy and auditing enforcements. Configure Guardium to read the native audit logs by customizing the Teradata template.

#### Before you begin

•  Configure the policies you require. See [policies](/../../#policies) for more information.

• You must have permission for the S-Tap Management role. The admin user includes this role by default.

• Download the [Teradata-Offline-Plugin.zip](https://github.com/IBM/universal-connectors/blob/main/filter-plugin/logstash-filter-teradatadb-guardium/TeradataOverJdbcPackage/Teradata-Offline-Plugin.zip) plug-in.

• Download the required jars as per your database version from URL:- https://downloads.teradata.com/download/connectivity/jdbc-driver

• Download "tdgssconfig.jar" - Go to the URL https://downloads.teradata.com/download/connectivity/jdbc-driver and download the zip/tar for version 16.10.00.07. After extracting the downloaded zip/tar, there will be a file named "tdgssconfig.jar".

• The file "tdgssconfig.jar" is meant for the Teradata database version 16.10.00.07 but it should be compatible with the later database versions also.


#### Procedure: 

1. On the collector, go to Setup > Tools and Views > Configure Universal Connector.
2. First enable the Universal Guardium connector, if it is disabled already.
3. Click ```Upload File``` and upload the jar/jars which you downloaded from the teradata website.
4. Click ```Upload File``` and upload "tdgssconfig.jar".
5. Click ```Upload File``` and select the offline [Teradata-Offline-Plugin.zip](https://github.com/IBM/universal-connectors/blob/main/filter-plugin/logstash-filter-teradatadb-guardium/TeradataOverJdbcPackage/Teradata-Offline-Plugin.zip) plug-in. After it is uploaded, click ```OK```.
6. Click the Plus sign to open the ```Connector Configuration``` dialog box. 
7. Type a name in the Connector name field.
8. Update the input section to add the details from the [teradataJDBC.conf](https://github.com/IBM/universal-connectors/blob/main/filter-plugin/logstash-filter-teradatadb-guardium/TeradataOverJdbcPackage/teradataJDBC.conf) file's input part, omitting the keyword "input{" at the beginning and its corresponding "}" at the end. Provide the details for database server name, username, and password that are required for connecting with JDBC.
9. Update the filter section to add the details from the [teradataJDBC.conf](https://github.com/IBM/universal-connectors/blob/main/filter-plugin/logstash-filter-teradatadb-guardium/TeradataOverJdbcPackage/teradataJDBC.conf) file's filter part, omitting the keyword "filter{" at the beginning and its corresponding "}" at the end. Provide the same database server name that you gave in the above step against the Server_Hostname attribute in the filter section.
10. The "type" fields should match in the input and the filter configuration sections. This field should be unique for every individual connector added.
11. If you are using two JDBC plug-ins on the same machine, the last_run_metadata_path file name should be different.
12. Click ```Save```. Guardium validates the new connector, and enables the universal connector if it was disabled. After it is validated, the connector appears in the ```Configure Universal Connector``` page.
