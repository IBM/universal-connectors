# AWS MySQL S3SQS Setup

Please follow the below link to setup S3SQS for AWS MySQL using kinesis data fire hose.
[S3SQSWithFirehose](../../input-plugin/logstash-input-s3sqs/S3SQSWithFirehose.md) guide for setup and configuration details.

## Configuring the AWS MySQL filter in Guardium

The Guardium universal connector is the Guardium entry point for native audit logs. The Guardium universal connector identifies and parses the received events, and converts them to a standard Guardium format. The output of the Guardium universal connector is forwarded to the Guardium sniffer on the collector for policy and auditing enforcements.

### Before you begin

* Configure the policies you need. For more information, see [Policies](/docs/#policies).
* You must have permissions for the S-Tap Management role. By default, the admin user is assigned the S-Tap Management role.
* Download the [logstash-filter-mysql_guardium_plugin_filter](https://github.com/IBM/universal-connectors/releases) plug-in.
* Download the [logstash-input-s3_sqs](https://github.com/IBM/universal-connectors/releases) plug-in.

### Procedure

1. On the collector, go to **Setup** > **Tools and Views** > **Configure Universal Connector**.
2. Enable the universal connector if it is disabled.
3. Click **Upload File** and select the offline [logstash-filter-mysql_guardium_plugin_filter](https://github.com/IBM/universal-connectors/releases) plug-in. After it is uploaded, click **OK**. This step is not necessary for Guardium Data Protection v11.0p490 or later, v11.0p540 or later, v12.0 or later.
4. Click **Upload File** and select the offline [logstash-input-s3_sqs](https://github.com/IBM/universal-connectors/releases) plug-in. After it is uploaded, click **OK**. This step is not necessary for Guardium Data Protection v11.0p490 or later, v11.0p540 or later, v12.0 or later.
5. Click the **Plus** sign to open the Connector Configuration dialog box.
6. In the **Connector name** field, enter a name.
7. Update the input section to add the details from the [MySQLOverS3SQS.conf](./MySQLOverS3SQS/MySQLOverS3SQS.conf) file's `input` section, omitting the keyword `input{` at the beginning and its corresponding `}` at the end. More details on how to configure the relevant input plugin can be found [here](../../input-plugin/logstash-input-s3sqs/README.md).
8. Update the filter section to add the details from the [MySQLOverS3SQS.conf](./MySQLOverS3SQS/MySQLOverS3SQS.conf) file's `filter` section, omitting the keyword `filter{` at the beginning and its corresponding `}` at the end.
9. Make sure that the `type` fields in the `input` and `filter` configuration sections align. This field must be unique for each connector added to the system. This is no longer required starting v12p20 and v12.1.
10. Click **Save**. Guardium validates the new connector and displays it in the Configure Universal Connector page.
11. When the offline plug-in is installed and the configuration is uploaded and saved in the Guardium machine, restart the universal connector by using the **Disable/Enable** button.

## Limitations

- When a login attempt fails, the MySQL audit log does not capture the database name. As a result, a new S-TAP entry may be created with the host displayed as `<ACCOUNT_ID>:unknown`.