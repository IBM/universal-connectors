# MSSQL-Guardium Logstash filter plug-in
### Meet MSSQL
* Tested versions: 14.0.x
* Environment: AWS, On-premises, GCP
* Supported inputs: JDBC (pull)
* Supported Guardium versions:
    * Guardium Data Protection: 11.4 and above
    * Guardium Insights SaaS: 1.0

## MSSQL-Guardium Logstash filter plug-in

This is a [Logstash](https://github.com/elastic/logstash) filter plug-in for the universal connector that is featured in IBM Security Guardium. It parses events and messages from the MSSQL audit log into a [Guardium record](https://github.com/IBM/universal-connectors/blob/main/common/src/main/java/com/ibm/guardium/universalconnector/commons/structures/Record.java)  instance (which is a standard structure made out of several parts). The information is then sent over to Guardium. Guardium records include the accessor (the person who tried to access the data), the session, data, and exceptions. If there are no errors, the data contains details about the query "construct". The construct details the main action (verb) and collections (objects) involved.

This plugin is written in Ruby and so is a script that can be directly copied into Guardium configuration of Universal Connectors. There is no need to upload the plugin code. However, in order to support few features one zip has to be added named, mssql-offline-plugins-7.5.2.zip and a jar file for mssql has to be uploaded named, mssql-jdbc-7.4.1.jre8.

The plug-in is free and open-source (Apache 2.0). It can be used as a starting point to develop additional filter plug-ins for Guardium universal connector.

## Follow this link to set up and use AWS MSSQL

[AWS MSSQL README](./AWSMSSQL_README.md)

## Follow this link to set up and use GCP MSSSQL

[GCP MSSQL README](./GCPMSSQL_README.md)

#### Limitations :

	• MSSQL auditing only supports error logs for following two groups:
		a. FAILED_DATABASE_AUTHENTICATION_GROUP: Indicates that a principal tried to log on to a contained database and failed. Events in this class are raised by new connections or by connections that are reused from a connection pool.
		b. FAILED_LOGIN_GROUP: Indicates that a principal tried to log on to SQL Server and failed. Events in this class are raised by new connections or by connections that are reused from a connection pool.
	• The mssql plug-in does not support IPV6.
	• For guardium version 11.4 and below, incorrect value of session start time in report is known issue.
	• MSSQL error capturing is not supported for AWS MSSQL and GCP MSSQL.
    • Single line comments are getting omitted in GCP MSSQL.
    • The server IP is set to the default value, which is "0.0.0.0".

    

