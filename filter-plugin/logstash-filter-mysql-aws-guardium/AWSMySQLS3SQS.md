# AWS MySQL S3SQS Setup
Please follow the below link to setup S3SQS for AWS MySQL using kinesis data fire hose.
[S3SQSWithFirehose](../../input-plugin/logstash-input-s3sqs/S3SQSWithFirehose.md) guide for setup and configuration details.


## Configuration
1. On the collector, go to ```Setup``` > ```Tools and Views``` > ```Configure Universal Connector```.
2. Enable the universal connector if it is disabled.
3. Click ```Upload File``` and select the offline [logstash-filter-mysql_guardium_plugin_filter.zip](./MySQLS3SQS/logstash-filter-mysql_guardium_plugin_filter.zip) plugin. After it is uploaded, click ```OK```. This step is not necessary for Guardium Data Protection v11.0p490 or later, v11.0p540 or later, v12.0 or later.
4. Click ```Upload File``` and select the offline [logstash-input-s3_sqs.zip](../../input-plugin/logstash-input-s3sqs/InputS3SQSPackage/S3SQS/logstash-input-s3_sqs.zip) plugin. After it is uploaded, click ```OK```. This step is not necessary for Guardium Data Protection v11.0p490 or later, v11.0p540 or later, v12.0 or later.
4. Click the Plus sign to open the Connector Configuration dialog box.
5. Type a name in the ```Connector name``` field.
6. If the audit logs are to be fetched from S3SQS directly, use the details from the [MySQLS3SQS.conf](./MySQLS3SQS/MySQLS3SQS.conf) file. Update the input section to add the details from the corresponding file's input part, omitting the keyword "input{" at the beginning and its corresponding "}" at the end. More details on how to configure the relevant input plugin can be found [here](../../input-plugin/logstash-input-s3sqs/README.md)
7. If the audit logs are to be fetched from S3SQS directly, use the details from the [MySQLS3SQS.conf](./MySQLS3SQS/MySQLS3SQS.conf) file. Update the filter section to add the details from the corresponding file's filter part, omitting the keyword "filter{" at the beginning and its corresponding "}" at the end.
8. The "type" fields should match in the input and the filter configuration sections. This field should be unique for every individual connector added. This is no longer required starting v12p20 and v12.1.
9. Click ```Save```. Guardium validates the new connector and displays it in the Configure Universal Connector page.
10. After the offline plug-in is installed and the configuration is uploaded and saved in the Guardium machine, restart the Universal Connector using the ```Disable/Enable``` button.

#### Limitation
When a login attempt fails, the MySQL audit log does not capture the database name. As a result, a new S-TAP entry may be created with the host displayed as `<ACCOUNT_ID>:unknown`.