# AWS MySQL Guardium Logstash filter configuration
This is a Logstash filter configuration. This filter receives CloudWatch audit logs of AWS MySQL instances, and filters those events and parses them into a Guardium record instance. The information is then sent over to Guardium as a JSON GuardRecord.
This filter is a script written in Ruby. It should be copied directly into the Guardium universal connector configuration. There is no need to modify the filter section (changes in the filter section may affect proper filtering).

### NOTES
* For CloudWatch to monitor your RDS instance, the MariaDB audit plug-in must be running on the instance. For information about this plug-in and version compatibilty, refer to [MariaDB Audit Plugin support](https://docs.aws.amazon.com/AmazonRDS/latest/UserGuide/Appendix.MySQL.Options.AuditPlugin.html).
* The client source program is not available in messages sent by MySQL. This data is sent only in the first audit log message upon database connection - and the filter plug-in doesn't aggregate data from different messages.
* Currently, this plugin supports only audit logs and "Login Failed" error logs.
* The ‘type’ field should be the same in both the input and filter sections in the Logstash configuration file. This field should be unique for every individual connector added.
* GDP: requires installation of [json_encode](https://www.elastic.co/guide/en/logstash-versioned-plugins/current/v3.0.3-plugins-filters-json_encode.html) filter plug-in
* The 'use' statement does not display the account ID in the 'Database Name' column on the reports page.
## Create and configure a MySQL database instance
### Create a MySQL database instance
To create a new MySQL instance, follow the instructions described [here](https://aws.amazon.com/getting-started/hands-on/create-mysql-db/). When setting the properties under Additional Configuration, in the Log exports section select Audit log and Error log as the log types to publish to Amazon.

### Create and modify a new parameter group
To publish logs to CloudWatch, create a new parameter group and set the log_output parameter to FILE. When you create a database instance, it is associated with the default parameter group and cannot be modified. To create a new parameter group follow these steps:
### Procedure
	1. Open the Amazon RDS console (https://console.aws.amazon.com/rds).
	2. In the navigation pane, choose Parameter groups.
	3. Choose Create parameter group to open the Create parameter group dialog box.
	4. In the Parameter group family list, choose your engine version.
	5. In the Group name box, enter the name of the new DB parameter group.
	6. In the Description box, enter a description for the new DB parameter group.
	7. Select Create.
	8. Go back to Parameter groups from the navigation pane.
	9. In the Parameter groups list, choose the parameter group that you just created.
	10. Choose Parameter group actions, and then choose Edit.
	11. Use the Filter parameters field to search for the log_output parameter.
	12. Set the value of the log_output parameter to FILE.
	13. Choose Save changes.

### Enable audit logs using the MariaDB plugin
To add the MariaDB plug-in to a MySQL instance, follow the instructions described [here](https://docs.aws.amazon.com/AmazonRDS/latest/UserGuide/Appendix.MySQL.Options.AuditPlugin.html).
### NOTE
The 'rdsadmin' user queries the database every second to check its health. This activity may cause your log file to grow quickly to a very large size, which could result in unnecessary data proccessing in the filter. If recording this activity is not required, add the rdsadmin user to the SERVER_AUDIT_EXCL_USERS list.

## Configuring the AWS MySQL filters in Guardium
### Before you begin
	• You must have permissions for the S-TAP Management role. The admin user includes this role by default.
	• Download the json-encode-offline-plugin.zip plug-in.
## Authorizing outgoing traffic from AWS to Guardium
	1. Log in to the Guardium API.
	2. Issue these commands:
		• grdapi add_domain_to_universal_connector_allowed_domains domain=amazonaws.com
		• grdapi add_domain_to_universal_connector_allowed_domains domain=amazon.com
## Procedure
	1. On the collector, go to Setup > Tools and Views > Configure Universal Connector.
	2. Click Upload File and select the offline json-encode-offline-plugin.zip plug-in. After it is uploaded, click OK.
	3. Click the Plus sign to open the Connector Configuration dialog box.
	4. Type a name in the Connector name field.
	5. Update the input section to add the details from the mysqlCloudwatch.conf file input section, omitting the keyword "input{" at the beginning and its corresponding "}" at the end.
	6. Update the filter section to add the details from the mysqlCloudwatch.conf file filter section, omitting the keyword "filter{" at the beginning and its corresponding "}" at the end.
	7. Click Save. Guardium validates the new connector, and enables the universal connector if it was disabled. After it is validated, it appears in the Configure Universal Connector page.
