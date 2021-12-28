Pre-packaged plugins can be downloaded from here: https://github.com/IBM/universal-connectors/releases

## Filter plugins

| Data source                                                               | Versions tested | Environments             | Developer            | Supported inputs               | Notes                         |
| ------------------------------------------------------------------------- | --------------- | ------------------------ | -------------------- | ------------------------------ | ----------------------------- |
| [Amazon S3](../filter-plugin/logstash-filter-s3-guardium/README.md)          |                 | AWS                      | IBM                  | CloudWatch (pull), SQS (pull)  |                               |
| [HDFS](../filter-plugin/logstash-filter-hdfs-guardium/README.md)          | Hadoop 3.1.x    | On-premise / Iaas        | IBM                  | Filebeat (push)                | **Guardium Data Protection only** |
| [MySQL](../filter-plugin/logstash-filter-mysql-guardium/README.md)        | 5.x             | On-premise / Iaas        | IBM                  | Syslog (push), Filebeat (push) |                               |
| [MySQL-Percona](../filter-plugin/logstash-filter-mysql-percona-guardium/README.md)        | 5.7.31-34             | On-premise / Iaas        | IBM                  | Filebeat (push) |         **Guardium Data Protection only**             |
| [MySQL (Cloud SQL)](../filter-plugin/logstash-filter-pubsub-mysql-guardium/README.md)| 8.0             | Google Cloud        | IBM                  | Pub/Sub (pull) |         **Guardium Data Protection only**             |
| [MongoDB](../filter-plugin/logstash-filter-mongodb-guardium/README.md)    | 4.2, 4.4        | On-premise / Iaas        | IBM                  | Syslog (push), Filebeat (push) |                               |
| [Snowflake](https://github.com/infoinsights/guardium-snowflake-uc-filter) | 5.x             | AWS, Azure, Google Cloud | Information Insights | JDBC (pull)                    | **Guardium Data Protection only** |
| [PostgreSQL](../filter-plugin/logstash-filter-postgres-guardium/PostgresOverCloudWatchPackage/README.md) | 12.x | AWS | IBM | CloudWatch (pull)   | **Guardium Data Protection only**.<br />Installs required [json_encode](https://www.elastic.co/guide/en/logstash-versioned-plugins/current/v3.0.3-plugins-filters-json_encode.html) filter plug-in. |
|  [PostgreSQL (Cloud SQL)](../filter-plugin/logstash-filter-pubsub-postgresql-guardium/README.md)| 13.0             | Google Cloud        | IBM                  | Pub/Sub (pull) |         **Guardium Data Protection only**             |
| [Amazon DynamoDB](../filter-plugin/logstash-filter-dynamodb-guardium/README.md) | 2019.11.21 | AWS | IBM | CloudWatch (pull)   | **Guardium Data Protection only** |
| [Oracle Unified Audit](../filter-plugin/logstash-filter-oua-guardium/README.md)    | 18, 19 | On-prem, RDS                 | IBM      | Oracle Unified Audit (pull) | **Guardium Data Protection only** |
| [Couchbase](../filter-plugin/logstash-filter-couchbasedb-guardium/README.md)    | 6.6.2-9600        | On-premise / Iaas        | IBM    		| Filebeat (push) | **Guardium Data Protection only** |
| [Neo4j](../filter-plugin/logstash-filter-couchbasedb-guardium/README.md)    | 4.2.11        | On-premise / Iaas        | IBM    		| Filebeat (push) | **Guardium Data Protection only** |
| [SAP HANA](../filter-plugin/logstash-filter-saphana-guardium/README.md)    | 2.00.033.00.1535711040        | On-premise / Iaas        | IBM    		| Filebeat (push), JDBC (pull) | **Guardium Data Protection only** |

