# Available Plug-ins
Univer***REMOVED***l Connector uses plug-ins to connect the different datasources to Guardium. 
Below are all the available plug-ins, which are pre-developed and ready for use.

Our latest product version for Guardium Data Protection is [11.5](../docs/Guardium%20Data%20Protection)

Our latest product version for Guardium Insights is [3.2.x](../docs/Guardium%20Insights/3.2.x/Plugins_management.md)
## Supported Datasources
The following list is supported by the latest versions.
To see if a particular plug-in is supported by older versions, please refer to the "supported versions" section inside each plug-in page.
* [Amazon DynamoDB](../filter-plugin/logstash-filter-dynamodb-guardium/README.md)
* [Amazon Redshift](../filter-plugin/logstash-filter-redshift-aws-guardium/README.md) (soon: GI 3.3/Saas)
* [Amazon RDS for MySQL](../filter-plugin/logstash-filter-mysql-aws-guardium/README.md)
* [Amazon RDS for Postgres and Aurora Postgres](../filter-plugin/logstash-filter-postgres-guardium/README.md)
* [Amazon S3](../filter-plugin/logstash-filter-s3-guardium/README.md)
* [Aurora-MySQL](../filter-plugin/logstash-filter-aurora-mysql-guardium/README.md) (soon: GI 3.3/Saas)
* [Azure Apache Solr](../filter-plugin/logstash-filter-azure-apachesolr-guardium/README.md) (soon: GI 3.3/Saas)
* [Azure Postgres](../filter-plugin/logstash-filter-azure-postgresql-guardium/README.md) (soon: GI 3.3/Saas)
* [Azure SQL](../filter-plugin/logstash-filter-azure-sql-guardium/README.md) (soon: GI 3.3/Saas)
* [Cas***REMOVED***ndra](../filter-plugin/logstash-filter-cas***REMOVED***ndra-guardium/README.md) (soon: GI 3.3/Saas)
* [Couchbase](../filter-plugin/logstash-filter-couchbasedb-guardium/README.md)
* [CouchDB](../filter-plugin/logstash-filter-couchdb-guardium/README.md) (soon: GI 3.3/Saas)
* [DocumentDB](../filter-plugin/logstash-filter-documentdb-aws-guardium/README.md) (soon: GI 3.3/Saas)
* [Google Cloud Apache Solr](../filter-plugin/logstash-filter-pubsub-apachesolr-guardium/README.md) (Future GI releases)
* [Google Cloud BigQuery](../filter-plugin/logstash-filter-pubsub-bigquery-guardium/README.md) (Future GI releases)
* [Google Cloud Firestore](../filter-plugin/logstash-filter-pubsub-firestore-guardium/README.md) (Future GI releases)
* [Google Cloud PostgreSQL](../filter-plugin/logstash-filter-pubsub-postgresql-guardium/README.md) (Future GI releases)
* [Google Cloud Spanner](../filter-plugin/logstash-filter-pubsub-spanner-guardium/README.md) (Future GI releases)
* [Google Cloud MySQL](../filter-plugin/logstash-filter-pubsub-mysql-guardium/README.md) (Future GI releases)
* [HDFS](../filter-plugin/logstash-filter-hdfs-guardium/README.md)
* [MariaDB](../filter-plugin/logstash-filter-mariadb-guardium/README.md) (soon: GI 3.3/Saas)
* [Amazon RDS for MariaDB](../filter-plugin/logstash-filter-mariadb-aws-guardium/README.md) (soon: GI 3.3/Saas)
* [MongoDB](../filter-plugin/logstash-filter-mongodb-guardium/README.md)
* [MySQL](../filter-plugin/logstash-filter-mysql-guardium/README.md)
* [MySQL-Percona](../filter-plugin/logstash-filter-mysql-percona-guardium/README.md)
* [MSSQL](../filter-plugin/logstash-filter-mssql-guardium/README.md) (soon: GI 3.3/Saas)
* [Neo4j](../filter-plugin/logstash-filter-neo4j-guardium/README.md) (soon: GI 3.3/Saas)
* [Neptune](../filter-plugin/logstash-filter-neptune-aws-guardium/README.md) 
* [Greenplum](../filter-plugin/logstash-filter-onPremGreenplumdb-guardium/README.md)
* [On Prem PostgreSQL EDB and FEP](../filter-plugin/logstash-filter-onPremPostgres-guardium/README.md) (soon: GI 3.3/Saas)
* [Oracle Unified Audit](../filter-plugin/logstash-filter-oua-guardium/README.md) (Future GI releases)
* [SAP HANA](../filter-plugin/logstash-filter-***REMOVED***phana-guardium/README.md)
* [Snowflake](https://github.com/infoinsights/guardium-snowflake-uc-filter) (soon: GI 3.3/Saas)
* [Teradata](../filter-plugin/logstash-filter-teradatadb-guardium/README.md) (Future GI releases)
* [Yugabyte](../filter-plugin/logstash-filter-yugabyte-guardium/README.md) (soon: GI 3.3/Saas)

## Developing Plug-ins
Users can develop their own univer***REMOVED***l connector plug-ins, if needed, and contribute them back to the open source project, if desired.

[Here](../docs/Guardium%20Data%20Protection/developing_plugins_gdp.md) is a guide for developing new plug-ins for Guardium Data Protection.

[Here](../docs/Guardium%20Insights/3.2.x/developing_plugins_gi.md) is a guide for developing new plug-ins for Guardium Insights.
