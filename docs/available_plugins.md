Pre-packaged plugins can be downloaded from here: https://github.com/IBM/universal-connectors/releases

## Filter plugins

| Data source                                                               | Versions tested | Environments             | Developer            | Supported inputs               | Notes                         |
| ------------------------------------------------------------------------- | --------------- | ------------------------ | -------------------- | ------------------------------ | ----------------------------- |
| [AWS S3](../filter-plugin/logstash-filter-s3-guardium/README.md)          |                 | AWS                      | IBM                  | Cloudwatch (pull), SQS (pull)  |                               |
| [HDFS](../filter-plugin/logstash-filter-hdfs-guardium/README.md)          | Hadoop 3.1.x    | On-premise / Iaas        | IBM                  | Filebeat (push)                | Guardium Data Protection only |
| [MySQL](../filter-plugin/logstash-filter-mysql-guardium/README.md)        | 5.x             | On-premise / Iaas        | IBM                  | Syslog (push), Filebeat (push) |                               |
| [MongoDB](../filter-plugin/logstash-filter-mongodb-guardium/README.md)    | 4.2, 4.4        | On-premise / Iaas        | IBM                  | Syslog (push), Filebeat (push) |                               |
| [Snowflake](https://github.com/infoinsights/guardium-snowflake-uc-filter) | 5.x             | AWS, Azure, Google Cloud | Information Insights | JDBC (pull)                    |                               |
