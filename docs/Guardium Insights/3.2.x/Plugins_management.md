# Managing and Installing Plugins
The universal connector plug-ins are shown on the **Universal Connector plug-ins** page. 
1. Open the **Settings** menu.
2. Click **Connections**.
3. Click the **Manage** drop-down button and select the **universal connector plug-ins** option.


## Built-in plug-ins

Guardium Insights supplies the following input plug-ins: 
* [Filebeat](../../../input-plugin/logstash-input-beats/README.md)
* [SQS](../../../input-plugin/logstash-input-sqs/README.md)
* [CloudWatch](../../../input-plugin/logstash-input-cloudwatch-logs/README.md)

## Adding a new plug-in

1. Download the plug-in for the requested data source from the attached table to your local system.

2. Click **Connections** in the **Settings** menu.

3. Click the **Manage** drop-down button and select the **universal connector plug-ins** option.

4. Click the **Upload plug-in** button and select the zip file from your local system.

5. Verify that the new plug-in is shown in the **universal connector plug-ins** page.

## Available plugins
|                                                    Data source                                                    | Versions tested        | Environments      | Developer | Supported inputs              |                                                    Download                                                    |
|:-----------------------------------------------------------------------------------------------------------------:|------------------------|-------------------|-----------|-------------------------------|:--------------------------------------------------------------------------------------------------------------:|
|               [Amazon DynamoDB](../../../filter-plugin/logstash-filter-dynamodb-guardium/README.md)               | 2019.11.21             | AWS               | IBM       | CloudWatch (pull)             |  [GI](https://github.com/IBM/universal-connectors/releases/download/v1.2.0/DynamodbOverCloudwatchPackage.zip)  |
|            [Amazon RDS for MySQL](../../../filter-plugin/logstash-filter-mysql-aws-guardium/README.md)            | 5.7                    | AWS               | IBM       | CloudWatch (pull)             | [GI](https://github.com/IBM/universal-connectors/releases/download/v1.2.0/MysqlOverCloudwatchLogsPackage.zip)  |
| [Amazon RDS for Postgres and Aurora Postgres](../../../filter-plugin/logstash-filter-postgres-guardium/README.md) | 13.x                   | AWS               | IBM       | CloudWatch (pull)             |  [GI](https://github.com/IBM/universal-connectors/releases/download/v1.2.0/PostgresOverCloudWatchPackage.zip)  |                                                  
|                     [Amazon S3](../../../filter-plugin/logstash-filter-s3-guardium/README.md)                     | -                      | AWS               | IBM       | CloudWatch (pull), SQS (pull) |   [GI](https://github.com/IBM/universal-connectors/releases/download/v1.2.0/S3OverCloudwatchLogsPackage.zip)   |
|                [Couchbase](../../../filter-plugin/logstash-filter-couchbasedb-guardium/README.md)                 | 6.6.2-9600             | On-premise / Iaas | IBM       | Filebeat (push)               | [GI](https://github.com/IBM/universal-connectors/releases/download/v1.2.0/CouchbasedbOverFilebeatPackage.zip)  |
|                      [HDFS](../../../filter-plugin/logstash-filter-hdfs-guardium/README.md)                       | Hadoop 3.1.x           | On-premise / Iaas | IBM       | Filebeat (push)               |     [GI](https://github.com/IBM/universal-connectors/releases/download/v1.2.0/HDFSOverFilebeatPackage.zip)     |
|  [MariaDB-AWS](../../../filter-plugin/logstash-filter-mariadb-aws-guardium/README.md)                        		   | 10.6.10, 10.5.17       | AWS               | IBM       | CloudWatch (pull)             |  [GI](https://github.com/IBM/universal-connectors/releases/download/v1.2.0/MariaDBOverCloudWatchPackage.zip)   |
|                   [MongoDB](../../../filter-plugin/logstash-filter-mongodb-guardium/README.md)                    | 4.2, 4.4               | On-premise / Iaas | IBM       | Filebeat (push)               |   [GI](https://github.com/IBM/universal-connectors/releases/download/v1.2.0/MongodbOverFilebeatPackage.zip)    |
|                     [MySQL](../../../filter-plugin/logstash-filter-mysql-guardium/README.md)                      | 5.x                    | On-premise / Iaas | IBM       | Filebeat (push)               |    [GI](https://github.com/IBM/universal-connectors/releases/download/v1.2.0/MysqlOverFilebeatPackage.zip)     |
|             [MySQL-Percona](../../../filter-plugin/logstash-filter-mysql-percona-guardium/README.md)              | 5.7.31-34              | On-premise / Iaas | IBM       | Filebeat (push)               | [GI](https://github.com/IBM/universal-connectors/releases/download/v1.2.0/MysqlPerconaOverFilebeatPackage.zip) |
|                 [Neptune](../../../filter-plugin/logstash-filter-neptune-aws-guardium/README.md)                  | 1.1                    | AWS               | IBM       | CloudWatch (pull)             |  [GI](https://github.com/IBM/universal-connectors/releases/download/v1.2.0/NeptuneOverCloudWatchPackage.zip)   |
|             [Greenplum](../../../filter-plugin/logstash-filter-onPremGreenplumdb-guardium/README.md)              | 6.21.0                 | On-premise        | IBM       | Filebeat (push)               | [GI](https://github.com/IBM/universal-connectors/releases/download/v1.2.0/GreenplumdbOverFilebeatPackage.zip)  |
|                   [SAP HANA](../../../filter-plugin/logstash-filter-saphana-guardium/README.md)                   | 2.00.033.00.1535711040 | On-premise / Iaas | IBM       | Filebeat (push)               |   [GI](https://github.com/IBM/universal-connectors/releases/download/v1.2.0/SaphanaOverFilebeatPackage.zip)    |
