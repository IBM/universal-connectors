# Available Plug-ins
Universal Connector uses plug-ins to connect the different datasources to Guardium. 
Below all the available plug-ins which are pre-developed and ready for use.

Our latest product version for Guardium Data Protection is [11.5](../docs/Guardium%20Data%20Protection)

Our latest product version for Guardium Insights is [3.2.x](../docs/Guardium%20Insights/3.2.x/Plugins_management.md)
## Supported Datasources
The following list is supported by the latest versions.
In case of other version, please refer the supported versions inside the plug-in.
* [Amazon DynamoDB](../filter-plugin/logstash-filter-dynamodb-guardium/README.md)
* [Amazon Redshift](../filter-plugin/logstash-filter-redshift-aws-guardium/README.md) (Coming soon on Guardium Insights)
* [Amazon RDS for MySQL](../filter-plugin/logstash-filter-mysql-aws-guardium/README.md)
* [Amazon RDS for Postgres and Aurora Postgres](../filter-plugin/logstash-filter-postgres-guardium/README.md)
* [Amazon S3](../filter-plugin/logstash-filter-s3-guardium/README.md)
* [Aurora-MySQL](../filter-plugin/logstash-filter-aurora-mysql-guardium/README.md) (Coming soon on Guardium Insights)
* [Azure Apache Solr](../filter-plugin/logstash-filter-azure-apachesolr-guardium/README.md) (Coming soon on Guardium Insights)
* [Azure Postgres](../filter-plugin/logstash-filter-azure-postgresql-guardium/README.md) (Coming soon on Guardium Insights)
* [Azure SQL](../filter-plugin/logstash-filter-azure-sql-guardium/README.md) (Coming soon on Guardium Insights)
* [Cassandra](../filter-plugin/logstash-filter-cassandra-guardium/README.md) (Coming soon on Guardium Insights)
* [Couchbase](../filter-plugin/logstash-filter-couchbasedb-guardium/README.md)
* [CouchDB](../filter-plugin/logstash-filter-couchdb-guardium/README.md) (Coming soon on Guardium Insights)
* [DocumentDB](../filter-plugin/logstash-filter-documentdb-aws-guardium/README.md) (Coming soon on Guardium Insights)
* [Google Cloud Apache Solr](../filter-plugin/logstash-filter-pubsub-apachesolr-guardium/README.md) (Coming soon on Guardium Insights)
* [Google Cloud BigQuery](../filter-plugin/logstash-filter-pubsub-bigquery-guardium/README.md) (Coming soon on Guardium Insights)
* [Google Cloud Firestore](../filter-plugin/logstash-filter-pubsub-firestore-guardium/README.md) (Coming soon on Guardium Insights)
* [Google Cloud PostgreSQL](../filter-plugin/logstash-filter-pubsub-postgresql-guardium/README.md) (Coming soon on Guardium Insights)
* [Google Cloud Spanner](../filter-plugin/logstash-filter-pubsub-spanner-guardium/README.md) (Coming soon on Guardium Insights)
* [Google Cloud MySQL](../filter-plugin/logstash-filter-pubsub-mysql-guardium/README.md) (Coming soon on Guardium Insights)
* [HDFS](../filter-plugin/logstash-filter-hdfs-guardium/README.md)
* [MariaDB](../filter-plugin/logstash-filter-mariadb-guardium/README.md) (Coming soon on Guardium Insights)
* [Amazon RDS for MariaDB](../filter-plugin/logstash-filter-mariadb-aws-guardium/README.md) (Coming soon on Guardium Insights)
* [MongoDB](../filter-plugin/logstash-filter-mongodb-guardium/README.md)
* [MySQL](../filter-plugin/logstash-filter-mysql-guardium/README.md)
* [MySQL-Percona](../filter-plugin/logstash-filter-mysql-percona-guardium/README.md)
* [MSSQL](../filter-plugin/logstash-filter-mssql-guardium/README.md) (Coming soon on Guardium Insights)
* [Neo4j](../filter-plugin/logstash-filter-neo4j-guardium/README.md) (Coming soon on Guardium Insights)
* [Neptune](../filter-plugin/logstash-filter-neptune-aws-guardium/README.md) 
* [Greenplum](../filter-plugin/logstash-filter-onPremGreenplumdb-guardium/README.md)
* [On Prem PostgreSQL EDB and FEP](../filter-plugin/logstash-filter-onPremPostgres-guardium/README.md) (Coming soon on Guardium Insights)
* [Oracle Unified Audit](../filter-plugin/logstash-filter-oua-guardium/README.md) (Coming soon on Guardium Insights)
* [SAP HANA](../filter-plugin/logstash-filter-saphana-guardium/README.md)
* [Snowflake](https://github.com/infoinsights/guardium-snowflake-uc-filter) (Coming soon on Guardium Insights)
* [Teradata](../filter-plugin/logstash-filter-teradatadb-guardium/README.md) (Coming soon on Guardium Insights)
* [Yugabyte](../filter-plugin/logstash-filter-yugabyte-guardium/README.md) (Coming soon on Guardium Insights)

## Developing Plug-ins
Users can develop their own universal connector plug-ins, if needed, and contribute them back to the open source project, if desired.

[Here](../docs/Guardium%20Data%20Protection/developing_plugins_gdp.md) is a guide for developing new plug-ins for Guardium Data Protection.

[Here](../docs/Guardium%20Insights/3.2.x/developing_plugins_gi.md) is a guide for developing new plug-ins for Guardium Insights.
