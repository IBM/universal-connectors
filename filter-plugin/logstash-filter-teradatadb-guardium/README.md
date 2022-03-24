# Teradata-Guardium Logstash filter plug-in

	This is a [Logstash](https://github.com/elastic/logstash) filter plug-in for the univer***REMOVED***l connector that is featured in IBM Security Guardium. It parses events and mes***REMOVED***ges from the Teradata audit log into a Guardium record instance (which is a standard structure comprised of several parts). The information is then sent over to Guardium. Guardium records include the accessor (the person who tried to access the data), session, data, and exceptions. If there are no errors, the data contains details about the query "construct". The construct details the main action (verb) and collections (objects) involved.

	The plug-in is free and open-source (Apache 2.0). It can be used as a starting point to develop additional filter plug-ins for Guardium univer***REMOVED***l connector.


# Audit Log Configurations

## Procedure

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
	
# Steps to di***REMOVED***ble Auditing

	Auditing can be di***REMOVED***bled similarly to how we enabled auditing by logging in with dbc user or any other user that has access to DBQLAccessMacro.
	
	To di***REMOVED***ble query logging, execute the below command:-
		
	END QUERY LOGGING WITH SQL LIMIT SQLTEXT=0 ON ALL;
	

# Archiving and Deleting DBQL Logs	
	There are many ways to archive and delete DBQL logs. One of the ways is depicted below:-
	
	Use the following steps to delete old log data from system tables manually:
	
	It is recommended, though not neces***REMOVED***ry, to di***REMOVED***ble DBQL logging before you perform clean up activities on the logs. Otherwise, the delete process locks the DBQL table and if DBQL needs to flush a cache to the ***REMOVED***me table to continue logging queries, the whole system could experience a slow-down.
	
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


# Limitations :
	
	• Teradata sniffer parser does not parse below listed operations properly,Hence this plug-in does not support below operations:-
		1] User Management 
		2] DBQL Queries.
		3] Timestamp configuration.
		4] Cast operations.
		5] Stored Procedure and User Defined Functions.
	• This plug-in is not tested on cloud instance.
	• This plug-in supports queries that are approximately 32,000 characters long. When the count of characters in a query exceed the given count, the remaining part of the query is stored in other rows. This is why the SQLTextInfo column of the table DBC.DBQLSqlTbl has more than one row per QueryID.
	• serverIp is hardcoded to "0.0.0.0" in this plugin, as tables referred in configuration file do not have an attribute that directly holds actual serverIp value but that can be checked from column LogonSource(from DBC.DBQLOGTBL ) or sourceProgram  attribute. For more information on how to check the serverIp from LogonSource, please refer this doc:-
	https://docs.teradata.com/r/ANYCOtbX9Q1iyd~Uiok8gA/VPQKKhAyOf6hzUc4sfciIQ

# Configuring the Teradata filters in Guardium

	The Guardium univer***REMOVED***l connector is the Guardium entry point for native audit logs. The univer***REMOVED***l connector identifies and parses received events, and then converts them to a standard Guardium format. The output of the univer***REMOVED***l connector is forwarded to the Guardium sniffer on the collector, for policy and auditing enforcements. Configure Guardium to read the native audit logs by customizing the Teradata template.

# Before you begin

	• You must have permission for the S-Tap Management role. The admin user includes this role by default.
	• Download the logstash-offline-Teradata-plugins-7.5.2 plug-in.
	• Download the required jars as per your database version from URL:- https://downloads.teradata.com/download/connectivity/jdbc-driver

## Procedure : 

    1. On the collector, go to Setup > Tools and Views > Configure Univer***REMOVED***l Connector.
	2. Click Upload File and upload the jar/jars which you have downloaded from teradata website. 
    3. Click Upload File and select the offline logstash-offline-Teradata-plugins-7.5.2 plug-in. After it is uploaded, click OK.
	4. Click the Plus sign to open the Connector Configuration dialog box.
    5. Type a name in the Connector name field.
    6. Update the input section to add the details from Teradata.conf file's input part, omitting the keyword "input{" at the beginning and its corresponding "}" at the end. Provide required details for DB server name, username and password for making JDBC connectivity.
    7. Update the filter section to add the details from Teradata.conf file's filter part, omitting the keyword "filter{" at the beginning and its corresponding "}" at the end. Provide the ***REMOVED***me DB server name as in above step against the Server_Hostname attribute in the filter section.
	Note: "type" field should match in input and filter configuration section. This field should be unique for every individual connector added.
    8. Click Save. Guardium validates the new connector, and enables the univer***REMOVED***l connector if it was di***REMOVED***bled. After it is validated, the connector appears in the Configure Univer***REMOVED***l Connector page.
