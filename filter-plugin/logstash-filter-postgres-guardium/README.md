## Postgres-Guardium Logstash filter plug-in
### Meet Postgres
* Tested versions: 13.x
* Environment: AWS
* Supported inputs: CloudWatch (pull)
* Supported versions:
    * GDP: 11.3 and above
    * GI: 3.2 and above

This is a [Logstash](https://github.com/elastic/logstash) filter plug-in for the universal connector that is featured in IBM Security Guardium. It parses events and messages from the Postgres audit log into a [Guardium record](https://github.com/IBM/universal-connectors/blob/main/common/src/main/java/com/ibm/guardium/universalconnector/commons/structures/Record.java) instance (which is a standard structure made out of several parts). The information is then sent over to Guardium. Guardium records include the accessor (the person who tried to access the data), the session, data, and exceptions. If there are no errors, the data contains details about the query "construct". The construct details the main action (verb) and collections (objects) involved.

This plugin is written in Ruby, and so is a script that can be directly copied into the Guardium configuration for Universal Connectors. There is no need to upload the plugin code. However, in order to support a few features one zip has to be added with the name "postgres-offline-plugins-7.5.2.zip".

The plug-in is free and open-source (Apache 2.0). It can be used as a starting point to develop additional filter plug-ins for the Guardium universal connector.

## Follow the below link to setup and use AWS Postgres

[AWS Postgres README](./AwsPostgres_README.md) 

## Follow the below link to setup and use Aurora Postgres

[Aurora Postgres README](./AuroraPostgres_README.md) 

## Limitations
	• The postgres plug-in does not support IPV6.
	• PGAudit logs the batch queries multiple times, so the report will show multiple entries for the same item.