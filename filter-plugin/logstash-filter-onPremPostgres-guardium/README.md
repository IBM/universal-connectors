## Postgres-Guardium Logstash filter plug-in

This is a [Logstash](https://github.com/elastic/logstash) filter plug-in for the univer***REMOVED***l connector that is featured in IBM Security Guardium. It parses events and mes***REMOVED***ges from the EDB and Fujitsu Enterprise Postgres audit log into a [Guardium record](https://github.com/IBM/univer***REMOVED***l-connectors/blob/main/common/src/main/java/com/ibm/guardium/univer***REMOVED***lconnector/commons/structures/Record.java) instance (which is a standard structure made out of several parts). The information is then sent over to Guardium. Guardium records include the accessor (the person who tried to access the data), the session, data, and exceptions. If there are no errors, the data contains details about the query "construct". The construct details the main action (verb) and collections (objects) involved.

Currently, this plug-in will work only on IBM Security Guardium Data Protection, not Guardium Insights.

This plugin is written in Ruby and so is a script that can be directly copied into Guardium configuration of Univer***REMOVED***l Connectors. There is no need to upload the plugin code. However, in order to support few features one zip has to be added named, postgres-offline-plugins-7.5.2.zip

The plug-in is free and open-source (Apache 2.0). It can be used as a starting point to develop additional filter plug-ins for Guardium univer***REMOVED***l connector.

## Follow the below link to setup and use EDB Postgres

[EDB Postgres README](./EDBPostgres_README.md) 

## Follow the below link to setup and use Fujitsu Postgres

[FEP Postgres README](./FEPostgres_README.md) 

## Limitations
	• Pgaudit logs Failed queries twice, one as success and one with actual Failure reason, so expect Failed queries to be seen in Full Sql Report
	• The log entries which are generated even before the user is logged in or before the session starts, are currently not seen in SQL Error report
	• When the database is created using UI option from pgadmin, currently sniffer fails to parse that audit log, so the entry cannot be seen in the Full Sql report