Pre-packaged plugins can be downloaded from here: https://github.com/IBM/universal-connectors/releases

## Filter plugins

| Data source                                                               | Versions tested | Environments             | Developer            | Supported inputs               | Notes                         |
| ------------------------------------------------------------------------- | --------------- | ------------------------ | -------------------- | ------------------------------ | ----------------------------- |
| [AWS S3](../filter-plugin/logstash-filter-s3-guardium/README.md)          |                 | AWS                      | IBM                  | Cloudwatch (pull), SQS (pull)  |                               |
| [HDFS](../filter-plugin/logstash-filter-hdfs-guardium/README.md)          | Hadoop 3.1.x    | On-premise / Iaas        | IBM                  | Filebeat (push)                | **Guardium Data Protection only** |
| [MySQL](../filter-plugin/logstash-filter-mysql-guardium/README.md)        | 5.x             | On-premise / Iaas        | IBM                  | Syslog (push), Filebeat (push) |                               |
| [MongoDB](../filter-plugin/logstash-filter-mongodb-guardium/README.md)    | 4.2, 4.4        | On-premise / Iaas        | IBM                  | Syslog (push), Filebeat (push) |                               |
| [Snowflake](https://github.com/infoinsights/guardium-snowflake-uc-filter) | 5.x             | AWS, Azure, Google Cloud | Information Insights | JDBC (pull)                    |                               |
| [PostgreSQL](../filter-plugin/logstash-filter-postgres-guardium/PostgresOverCloudWatchPackage/README.md) | 12.x | Amazon AWS | IBM | Cloudwatch (pull)   | **Guardium Data Protection only**.<br />Installs required [json_encode](https://www.elastic.co/guide/en/logstash-versioned-plugins/current/v3.0.3-plugins-filters-json_encode.html) filter plug-in. |

