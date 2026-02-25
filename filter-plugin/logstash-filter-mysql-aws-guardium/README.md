# AWS MySQL Guardium Logstash filter configuration

### Meet AWS MySQL 
* Tested versions: 5.7
* Environment: AWS
* Supported inputs: CloudWatch (pull)
* Supported Guardium versions:
    * Guardium Data Protection: 11.4 and above
    * Guardium Data Security Center SaaS: 1.0

This is a Logstash filter configuration. This filter receives CloudWatch audit logs of AWS MySQL instances, filters those events, and parses them into a Guardium record instance. The information is then sent over to Guardium as a JSON GuardRecord.
This filter is a script written in Ruby. It should be copied directly into the Guardium universal connector configuration. There is no need to modify the filter section (changes in the filter section may affect proper filtering).


## Create and modify a new parameter group
To publish logs to CloudWatch, create a new parameter group and set the `log_output` parameter to `FILE`. When you create a database instance, it is associated with the default parameter group and cannot be modified. To create a new parameter group follow these steps:

### Procedure
1. Open the Amazon RDS console (https://console.aws.amazon.com/rds).
2. In the navigation pane, choose **Parameter groups**.
3. Choose **Create parameter group** to open the Create parameter group dialog box.
4. In the **Parameter group family** list, choose your engine version.
5. In the **Group name** box, enter the name of the new database parameter group.
6. In the **Description** box, enter a description for the new database parameter group.
7. Select **Create**.
8. Go back to **Parameter groups** from the navigation pane.
9. In the **Parameter groups** list, choose the parameter group that you just created.
10. Choose **Parameter group actions**, and then choose **Edit**.
11. Use the **Filter parameters** field to search for the `log_output` parameter.
12. Set the value of the `log_output` parameter to `FILE`.
13. Choose **Save changes**.

## Enable audit logs using the MariaDB plug-in
To add the MariaDB plug-in to a MySQL instance, follow the instructions described [here](https://docs.aws.amazon.com/AmazonRDS/latest/UserGuide/Appendix.MySQL.Options.AuditPlugin.html).

***Note: The 'rdsadmin' user queries the database every second to check its health. This activity may cause your log file to grow quickly to a very large size, which could result in unnecessary data proccessing in the filter. If recording this activity is not required, add the rdsadmin user to the SERVER_AUDIT_EXCL_USERS list.***


## Configuring the AWS MySQL filters in Guardium

#### Before you begin
* Configure the policies you require. See [policies](https://github.com/IBM/universal-connectors/tree/main/docs#policies) for more information.
* You must have permissions for the S-TAP Management role. The admin user includes this role by default.
* This filter plug-in is automatically available with Guardium Data Protection versions 12.x, 11.4 with appliance bundle 11.0p490 or later or Guardium Data Protection version 11.5 with appliance bundle 11.0p540 or later releases.
* For Guardium Data Protection version 11.0p540 and/or 11.0p6505 and or 12.0 and/or 12p15 download the [cloudwatch_logs plug-in](../../input-plugin/logstash-input-cloudwatch-logs/CloudwatchLogsInputPackage/offline-logstash-input-cloudwatch_log_1_0_5.zip)

**Note**: For Guardium Data Protection version 11.4 without appliance bundle 11.0p490 or prior or Guardium Data Protection version 11.5 without appliance bundle 11.0p540 or prior, download the [offline json-encode-offline-plugin.zip](https://github.com/IBM/universal-connectors/raw/main/filter-plugin/logstash-filter-mysql-aws-guardium/json-encode-offline-plugin.zip) plug-in. (Do not unzip the offline-package file throughout the procedure).

#### Authorizing outgoing traffic from AWS to Guardium

1. Log in to the Guardium API.
2. Issue these commands:
		• `grdapi add_domain_to_universal_connector_allowed_domains domain=amazonaws.com`
		• `grdapi add_domain_to_universal_connector_allowed_domains domain=amazon.com`
### Procedure
1. On the collector, go to **Setup** > **Tools and Views** > **Configure Universal Connector**.
2. Enable the universal connector if it is disabled.
3. Click **Upload File**
	*  Select the [offline json-encode-offline-plugin.zip](https://github.com/IBM/universal-connectors/raw/main/filter-plugin/logstash-filter-mysql-aws-guardium/json-encode-offline-plugin.zip) plug-in. After it is uploaded, click **OK**. This is not necessary for Guardium Data Protection v11.0p490 or later, v11.0p540 or later, v12.0 or later.
	*  If you have installed Guardium Data Protection version 11.0p540 and/or 11.0p6505 and/or 12.0 and.or 12p15, select the offline [cloudwatch_logs plug-in](../../input-plugin/logstash-input-cloudwatch-logs/CloudwatchLogsInputPackage/offline-logstash-input-cloudwatch_log_1_0_5.zip). After it is uploaded, click **OK**.
4. Click the Plus sign to open the Connector Configuration dialog box.
5. Type a name in the **Connector name** field.
6. Update the input section to add the details from the [mysqlCloudwatch.conf](https://github.com/IBM/universal-connectors/raw/main/filter-plugin/logstash-filter-mysql-aws-guardium/mysqlCloudwatch.conf) file input section, omitting the keyword "input{" at the beginning and its corresponding "}" at the end.
7. Update the filter section to add the details from the [mysqlCloudwatch.conf](https://github.com/IBM/universal-connectors/raw/main/filter-plugin/logstash-filter-mysql-aws-guardium/mysqlCloudwatch.conf) file filter section, omitting the keyword "filter{" at the beginning and its corresponding "}" at the end.
8. The "type" fields should match in the input and the filter configuration sections. This field should be unique for every individual connector added.
9. Click Save. Guardium validates the new connector and displays it in the Configure Universal Connector page.

## Limitations
* For CloudWatch to monitor your RDS instance, the MariaDB audit plug-in must be running on the instance. For information about this plug-in and version compatibilty, refer to [MariaDB Audit Plugin support](https://docs.aws.amazon.com/AmazonRDS/latest/UserGuide/Appendix.MySQL.Options.AuditPlugin.html).
* The client source program is not available in messages sent by MySQL. This data is sent only in the first audit log message upon database connection - and the filter plug-in doesn't aggregate data from different messages.
* Currently, this plugin supports only audit logs and "Login Failed" error logs.
* Guardium Data Protection requires installation of the [json_encode](https://www.elastic.co/guide/en/logstash-versioned-plugins/current/v3.0.3-plugins-filters-json_encode.html) filter plug-in.
* The `use` statement does not display the account ID in the 'Database Name' column on the reports page.
* The RDS MYSQL over CloudWatch plug-in does not classify SQL errors as error-level events in the database logs.
## Configuring the AWS MySQL Guardium Logstash filters in Guardium Data Security Center
To configure this plug-in for Guardium Data Security Center, follow [this guide.](/docs/Guardium%20Insights/3.2.x/UC_Configuration_GI.md)
For the input configuration step, refer to the [CloudWatch_logs section](/docs/Guardium%20Insights/3.2.x/UC_Configuration_GI.md#configuring-a-CloudWatch-input-plug-in).
