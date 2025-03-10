## Postgres-Guardium Logstash filter plug-in
### Meet Postgres
* Tested versions: EDB 12, 14 and FEP 14
* Environment: On-premise
* Supported inputs: Filebeat (push)
* Supported Guardium versions:
    * Guardium Data Protection: 11.4 and above
    * Guardium Data Security Center: 3.3
    * Guardium Data Security Center SaaS: 1.0

This is a [Logstash](https://github.com/elastic/logstash) filter plug-in for the universal connector that is featured in IBM Security Guardium. It parses events and messages from the EDB and Fujitsu Enterprise Postgres audit log into a [Guardium record](https://github.com/IBM/universal-connectors/blob/main/common/src/main/java/com/ibm/guardium/universalconnector/commons/structures/Record.java) instance (which is a standard structure made out of several parts). The information is then sent over to Guardium. Guardium records include the accessor (the person who tried to access the data), the session, data, and exceptions. If there are no errors, the data contains details about the query "construct". The construct details the main action (verb) and collections (objects) involved.

This plugin is written in Ruby and so is a script that can be directly copied into Guardium configuration of Universal Connectors. There is no need to upload the plugin code. However, in order to support few features one zip has to be added named, postgres-offline-plugins-7.5.2.zip

The plug-in is free and open-source (Apache 2.0). It can be used as a starting point to develop additional filter plug-ins for Guardium universal connector.

## Follow the below link to setup and use EDB Postgres

[EDB Postgres README](./EDBPostgres_README.md) 

## Follow the below link to setup and use Fujitsu Postgres

[FEP Postgres README](./FEPostgres_README.md) 

## Limitations
	• Pgaudit logs Failed queries twice, one as success and one with actual Failure reason, so expect Failed queries to be seen twice in Full Sql Report
	• The log entries which are generated even before the user is logged in or before the session starts, are currently not seen in SQL Error report
	• When the database is created using UI option from pgadmin, currently sniffer fails to parse that audit log, so the entry cannot be seen in the Full Sql report

## 5. Configuring the Postgres filters in Guardium Data Security Center
To configure this plug-in for Guardium Data Security Center, follow [this guide.](/docs/Guardium%20Insights/3.2.x/UC_Configuration_GI.md)
For the input configuration step, refer to the [Filebeat section](/docs/Guardium%20Insights/3.2.x/UC_Configuration_GI.md#Filebeat-input-plug-in-configuration).