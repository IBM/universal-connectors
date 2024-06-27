# Plug-in Development

These notes supplement [universal-connector-commons java](https://github.com/IBM/universal-connectors/tree/main/common) docs and the [universal connector docs](https://github.com/IBM/universal-connectors/tree/main/docs). Use them as a guideline that emphasizes a few points that we learned to repeat while developing plug-ins.

## Guardium Record fields within filter plug-in
1. **SessionId (required)**: if there's a good session ID (connection ID to the data source), use it. If you don't have a session ID, set sessionId to empty string, server port and client ports to -1. 

2. **Server/client** :
   * **No serverIP in cloud?** – set to 0.0.0.0 if it's a cloud environments.
   * **No clientIP (required)**: set to 0.0.0.0, as required not to get an exception
   * **isIPv6 flag (required)**: will cause Guardium to look at serverIPv6 and clientIPv6 fields first; if one of them is missing (dual mode) the ip4v will be translated to IPv6 format. In other words, if isIPv6 is true:  clientIPv6 will be used, serverIPv4 will be translated to ipv6 address. 
   * **No serverPort/clientPort?** – set to -1 (for cloud environments, usually)

3. **Type & language (required)**: set type to "TEXT" if Guardium should parse the command (use constant in commons Java lib), and language should specify the language code (see Appendix A: Language codes, below). If your connector parses the data, not Guardium, then specify type to "CONSTRUCT" and language to "FREE_TEXT" (use constants). 

4. **DB protocol**: For Databases that have built-in error text available (usually has, when you specify Language code to TEXT, as above), set DB protocol in Exception to the value in the DB Protocol column in Appendix A: Language codes, below, to take advantage of the error text.

5. **Record (AKA Guardium Record)**: Send either record.Data or record.Exception (error log event), not both. For getting errors in log, try to login with wrong password, or not allowed operation for user role. 
   * If Exception: ExceptionTypeId should contain some known ID for Guardium (LOGIN_FAILED or  SQL_ERROR, see constants used in MongoDB plug-in). If your plug-in parses the commands,  add in description field the error code and message that describes the error in your DB, for example a decription could be: “user not authorized (13)”  
   * If Exception: Description of the exception can also let Guardium show the predefined error texts (if Guardium is familiar with the DB,  but in most cases, you will need to write the sentence yourself for unfamiliar DBs. If you want to use the error text feature, specify error code only in DESCRIPTION, like "12380". Guardium will show the matching error text. To see the error text, change the SQL Errors report and add a column to the report. If you want to write the description by yourself, fill DESCRIPTION with proper message like "<error-type>: description of the error (<error-code>)"
   * To see the error in Guardium reports: If you provide a custom description, you will be able to see it under the Exception entitiy in Guardium reports (Exception.Description, Exception.ExceptionTypeId and Exception.SqlthatCausedException). However, if you rely on Sniffer to provide the description according to the known error ID, you will see it under the Database Error Text entity (Error.Text and Error_code).
   * If your plug-in parses the language syntax, make sure to populate Construct.redactedSensitiveDataSql and do not populate Data.originalSqlCommand. 
   
6. **DB user (required)**: try to get after applying roles and authentication restrictions on Db. If not available, please set to “NA”. 

7. **Timestamps (required)**: You need to send Guardium the time in UTC (long) in microseconds (based on protobuf Timestamp), as timezone offset (minOffsetFromGMT) and daylight saving time (mindset) is not taken into account (known issue). Verify that the time format in the audit log contains the timezone offset, so you'll be able to translate it well. For example, in Mongo plug-in, the audit log is "2020-10-08T13:58:00.222+0300", where the latter part is the timezone offset.  

8. **server hostname** :
   * If you use Filebeat, server hostname can be obtained from FileBeat input property [host][name]. See other plug-ins for examples. 
   * on cloud environments, hard-coding the server hostname can become a problem for customers that want to see that traffic is flowing from each and every UC connector they configured on Guardium. A better approach is to add a prefix for the server hostname with instance, account, or subscription on the cloud that your filter gets the events from, so customers will still be able to edit the pattern, if they would like to, in order to create further separation in STAP View page or similar pages. For example, on AWS, this could be "<AccountID>_<InstanceID>.aws.com", or "<dbtype>.amazonaws.com". Since we want to allow customers the flexibility to create the differentiation between datasources, filter plug-ins on cloud services should support stating server hostname as another field within the Logstash filter configuration stage, like 
    ````
     mutate {add_field => "server_hostname_pattern" => "%{[accountID]}_%{[instanceID]}"}  AWS 
     mutate {add_field => "server_hostname_pattern" => "%{[connection_name]}"}  GoogleCloud 
     mutate {add_field => "server_hostname_pattern" => "%{[subscriptionID]}_%{[instanceID]}"}  Azure 
     mutate { add_field =>  “server_hostname_pattern” => “%{accountId}-%{instanceId}” } } ICD
   
   ````
Make sure that your plug-in parser has a default server hostname, for cases where there are no Instance ID or Account ID, for example “postgres.aws.com”; do not send an empty string or “NA”. 
This prefix/pattern will be used by the filter in the serverHostname text String. This way customers will be able choose whether and how to differentiate the connectors, while the developer can recommend to use a default pattern, that will be populated dynamically with available fields like instance id, connection name, etc. Make sure the terms within the brackets match existing fields that arrive with each event, or its @metadata. If you are using CloudWatch input, use accountId_instanceId in your pattern. on Azure plug-ins, use subscriptionId_instanceId if possible. On GCP, use connection_name. 

9. **service name:** specify same as the DB name. 

10. **DB name**: the datasource name. on cloud environments, specify a better  hierarchy than just mention the data source name,  In short, try to include in service name some hierarchy + the datasource name. Example: instanceID:project:dbname. Including the DB name in service_name will help you see it in Full SQL report, as well, even if you switch to other DB during the same connection.
    * Commands that mention 2 or more databases or data source names should try to mention other objects involved in the command with their fully qualified name. For example if a graph database deletes entity person from both bookstore and addresses graph databases, aim to send a single event to Guardium as well, which mentions that person object from addresses database was also deleted. An expected result should have these values: DB name: bookstore;  
    * Sentence verb: delete 
    * Sentence object: person, addresses.personNote: in some cases, like MariaDB, we noticed that USE statement does not parses colons (“:”), so start the database name with an alphabet or underscore, and avoid using colons: use _979326520502_mariadb-aws-database_dbtest1 
    
11. **Object:** should represent an SQL Table or its equivalent. **Objects** in nested DB collections should show the full collection(s) path to them, without the specific documents. For example, for the path /rooms/docidxj34/messages/docid4ab2/likes, rooms is a collection of documents within a noSQL DB, and messages is a nested collection of documents within rooms, and so forth, so Object in guardium should be: rooms.message.likes

    * **Construct.fullSql** should contain the original command only: Object, verbs and other command or request parameters, if available. There’s no need to pass meta-data not directly relevant to the command itself. Note that this field should be populated when developing a plug-in that parses the command. If your plug-in only passes the command and Guardium does the parsing, populate Data.originalSqlCommand instead.
    * **Construct.redactedSensitiveDataSql** should contain a redacted version of the command in fullSql field, while masking parameter values as question marks, as these could be sensitive (like “INSERT into … VALUES (?, ?)”, or “… WHERE city=?”). There is no need to mask objects and verbs. This allows users in Guardium to see reports without seeing sensitive data. In guardium Report, you can add “sql” column to see the result of this masked field.
    * **optional fields:** UNKNOWN_STRING or "": empty string ("") is OK for optional fields. For required, use "NA" or 0.0.0.0 for serverIP in cloud environments. Please specify these fields briefly in README, limitations section.  
    * Explore the **tag** option in Filebeat configuration, to tag the data activity logs coming from multiple datasources and activate your plug-in only on these Logstash events. IBM GitHub repository have connectors built using Filebeat and can be used as reference. 
    * When using Filebeat, send to Guardium using a different port than 5044, as this port and a few others are already used by Guardium.  
    * For On-Prem data Sources, input type is usually Filebeat and Syslog (Syslog configuration example will be specified later in project).

12. **sourceProgram** - one word. two words may not show in Guardium reports. Prefer using-hyphen over unitingwords to improve legibility. 

13. If you have line feed/return characters ("\r", "\n") within Events, it's OK to remove them from final full SQL to improve legibility. remove events from your batch of Logstash Events only if you are certain their source is the data source that your filter handles, and there's no need for them to be inserted into Guardium. 

14. It is strongly recommended for to run commands on data source from a remote client, and not develop/test solely by directly issuing commands thru the data-source console. Running from a remote tool may populate other fields and improve their understanding of values. 

15. Refer to the Full SQL Report in Guardium as the point of truth (extend it by creating an extended Full SQL Report to see more columns); Investigation dashboard (QS) have known issues, like showing service name in "database" column. known issue. It is also recommended edit the Report and add an “sql” field to the report, to see the masked/redacted version of the full SQL. 

16. FYI, service name and other session fields are saved once per session in Guardium (the 1st command logged with same other accessor parameters and command construct), so that can explain some oddities you might encounter when viewing a Report, like seeing an older timestamp or data source name in a report row for a command you just performed. To solve such oddities, run a command again on the datasource, but try to use a  unique/different command structure (not only values). 

17. **DB protocol** should contain some identification how the data reached Guardium, like "Redshift (AWS)". If you let guardium parse the event, use the language as written in the agreement between UC and sniffer doc, like "MARIADB". 

18. **Server, DB Type, DB/Database name**: Known issue: If you instruct Guardium to parse the query (in Language parameter), Guardium will specify the language/parser it used (like MARIADB), instead of what was passed by plug-in developer, in Investigation dashboard Activity results. For Errors, DB type will show as specified by developer.  


# General dev guidlines

## When publishing a PR:    
    * Sign off all your commits with appropriate message.
    * Verify all code files have a copyright notice, and if they are not originally written by you, add copyright in NOTICE
    * Update CHANGELOG.md with what was added, changed, removed, especially if the PR changes something in the plug-in.
    * Add sample audit logs
    * Make sure unit tests test correct parsing of commands too, not only that parsing did not fail
    * Verify you code deals with null field values, as this could happen in a real setting.
    * Add conditional debug statements where needed, like printing the log event before parsing, as it may help others when debugging the plug-in in production environments. Make sure your plug-in does not print unnecessary data by default, in production, though.
    * Try to reuse already approved libraries/packages, like gson, instead of adding new alternative libraries that do the same funct

## In README.md,
    * Specify whether the plug-in manually parses the language grammar, or whether it passes the parsing to be done by Guardium (see other plug-ins for examples).
    * Specify the exact DB or data source software type and version used while developing, like “ElasticSearch Enterprise v8.9”, to help customers know regarding compatibility.
    * Specify the DB client tool(s) that were used .
    * Document how to to configure the audit log, and add info about filtering the audit log messages at the data source, if possible, like users, type of commands. Try to give examples of how to filter commands that are not needed, like system commands, as it creates redundant traffic, and it’s better to filter out messages/events as early as possible in the data pipeline.  


# Input plug-in configuration
In general, look for recent similar plug-in configuration examples on github, and check whether something changed, and follow the used format.

## Amazon CloudWatch input:
Mention start_position should be “end” in CloudWatch input, to prevent too much traffic sent over to Guardium, when the connector starts.  

## Google Cloud (GCP) input: 
    * multiline queries are sent as multiple events. To prevent that, it is recommended to configure Google Cloud to use cloudsql_mysql_audit plug-in, or similar, before sending the events to a sink (see README of GCP MySQL plug-in, for example). Another, less recommended direction is to try to run queries in single line, without line feed characters, or find a why to trim those characters before sending the command using a script (for example, refer to pubsub package in the repository), though this may introduce problems when parsing multiline queries with single line comments.
    * Be sure to mention any problem in README.md, and if you find a quick fix for it, refer readers to it.  
    * pubsub: create 2 projects usually: 1 for datasource, 1 for publishing events  

## JDBC input

To support load balancing, differentiate the query statements used in the input configuration on the basis of even and odd logic using unique value column from the table. This allows to send different data to two different Guardium collectors. Emphasize this in the README.md and as a comment in the input configuration section, so customers would be able to configure the input plug-in correctly. See an example from MSSQL plug-in. Here’s an example that uses session_id, but you can use any field, if it fits for this task:
````
jdbc{
statement = SELECT [...] AND session_id%2= 0 # on other collector: session_ID%2=1
````

## Repository requirements (not final): 
* .conf files (GDP, GI, generator input; all with dummy output)
* sample.log - contains example events from the data source your plug-in is intended for. 
* README with configuration of datasource and Guardium. Mention also what the parser supports, as well as any known limitations, like unsupported commands, log message size, ... We defined a template for the README, but you can view recent plug-ins for examples. 
* Pull requests on github.com should not include bin, .gradle, and other IDE folders, though leaving gradleWrapper.jar is fine included 
* CHANGELOG.md - after released on github.com, specify main additions, changes, and fixes,  on each commit 
* packge plug-in for Guardium Insights, as well

## Plug-in compilation and packaging
* Build your plug-in using Grade 6.5.1; this will save building plug-ins on your end, in the near future, as all plug-ins are going through a standardization.
* Verify all tests are passing on other timezone, as well, that are different from your timezone as well (so plugins' build script will not break)
* Plug-in package for Guardium Insights is a bit different, and follows some certain guidelines, as detailed below

## Packaging for Guardium Insights (GI)
* full sql report 
* param for traffic 
* package reference(s)

## Performance testing

* Try to use the tools and methods that Ora and Gene referred you to, if possible and helping. If not, develop a script that will replicate the process (insert events to data source then send to Guardium, or only send events to Guardium from static log file – skipping the datasource – if you feel there's a performance bottleneck when inserting data into the datasource).  
* Use the Logstash metrics plugin to sample performance; aim for 5K events/sec on normal Guardium machine (Atlanta). This threshold may change in the future as a result of changes in UC infrastructure. If you do not reach this level, advise with Gene (gmaystro@us.ibm.com; CC Tal) regarding performance tuning suggestions. 
* Send report with performance test details that include the experiment(s) steps, repetitions, settings, and results. 
* See also Filebeat and database settings that were used by Guardium testing department, in https://ibm.ent.box.com/folder/154711504977 (access to box folder required).  


## Automation guidelines 

### Not complete guidelines/advice:

* No hardcoding of values, cerificates, passwords, … in repo 
* add dependencies into gradle.build, not JARS in libs 
* change UI part, mostly. traffic utils (sql folder) should be used for unique datasources that do not have SQL syntax. 
* connection to cloud - certificate ….   
* create Jenkins job to test the automation 
* demo when automation test is ready - add README 


# Appendix A: Language codes
These language codes should be used when Guardium is familiar with the data source (DB) and should parse the command, otherwise, specify "FREE_TEXT" and in type, specify "CONSTRUCT", and send a parsed command in construct.
**DB protocol:** For Databases that have built-in error text available (usually has, when you specify Language code), set DB protocol in Exception to the value in the DB Protocol column below to take advantage of the error text.

| Language code | DB Protocol (on error) | -                                  | -      |
|---------------|----------------------|------------------------------------|--------|
| MSSQL         | MS SQL SERVER        |                                    |        |
| SYB           | SYBASE               | SYBASE ANYWHERE                    |        |
| ORACLE        | ORACLE               |                                    |        |
| DB2           | DB2                  | DB2 I?                             | DB2/Z? |
| INFX          | INFORMIX             | IBM INFORMIX?                      |        |
| MYSQL         | MYSQL                |                                    |        |
| TRD           | TERADATA             |                                    |        |
| HADOOP        | HADOOP               |                                    |        |
| PGRS          | POSTGRESQL           |                                    |        |
| MSSP          | SAP HANA             |                                    |        |
| OPTIM_AUDIT   | N/A                  |                                    |        |
| OPTIM_REDACT  | N/A                  |                                    |        |
| BIG_INSIGHTS  | N/A                  |                                    |        |
| MONGO         | MONGODB              |                                    |        |
| CASS          | CASSANDRA            |                                    |        |
| EL_SEARCH     | ELASTIC SEARCH       |                                    |        |
| ASTER         | N/A                  |                                    |        |
| GPLUM         | GREENPLUMDB          |                                    |        |
| COUCH         | N/A                  |                                    |        |
| MARIADB       | MARIADB              |                                    |        |
| FSM           | N/A                  |                                    |        |
| MAGEN         | N/A                  |                                    |        |
| HIVE          | HIVE                 |                                    |        |
| ACCUMULO      | N/A                  |                                    |        |
| IMPALA        | N/A                  |                                    |        |
| VRTC          | VERTICA              |                                    |        |
| MEMSQL        | N/A                  |                                    |        |
| MYSQL_X       | N/A                  |                                    |        |
| COUCHB        | COUCHBASE            |                                    |        |
| REDIS         | REDIS                | No support for built-in error text |        |
| COCKROACH     | SNOWFLAKE            |                                    |        |
| ?             | AMAZON DYNAMODB      |                                    |        |
| ?             | NEO4J                |                                    |        |
| ?             | WGPB                 |                                    |        |
| ?             | IBM ISERIES          |                                    |        |
| ?             | FTP                  |                                    |        |
| ?             | GDM                  |                                    |        |
| ?             | CIFS                |                                    |        |



