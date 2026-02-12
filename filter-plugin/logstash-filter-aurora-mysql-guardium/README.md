# Aurora-MySQL-Guardium Logstash filter plug-in

### Meet Aurora-MySQL
* Tested versions: 2.07.2
* Environment: AWS
* Supported inputs: CloudWatch (pull)
* Supported Guardium versions:
  * Guardium Data Protection: 11.4 and above
  * Guardium Data Security Center SaaS: 1.0

This is a [Logstash](https://github.com/elastic/logstash) filter plug-in for the universal connector that is featured in IBM Security Guardium. It parses events and messages from the aurora-mysql audit log into a [Guardium record](https://github.com/IBM/universal-connectors/blob/main/common/src/main/java/com/ibm/guardium/universalconnector/commons/structures/Record.java) instance (which is a standard structure made out of several parts). The information is then sent over to Guardium. Guardium records include the accessor (the person who tried to access the data), the session, data, and exceptions. If there are no errors, the data contains details about the query "construct". The construct details the main action (verb) and collections (objects) involved.

The plug-in is free and open-source (Apache 2.0). It can be used as a starting point to develop additional filter plug-ins for Guardium universal connector.


## Enabling auditing

1. In Aurora for MySQL, click **Parameter Groups**.
2. Click **Create Parameter Groups**.
3. Provide the following details:
		• Parameter group family : Provide aurora-mysql version
		• Type : DB cluster parameter group
		• Group name : Name of Group
		• Description : Privide description
4. Click **create**.
5. Select **DB Parameter** > **Parameter group actions** > **Edit**.
6. Change the value of parameter by adding these settings:
		• server_audit_events = CONNECT,QUERY_DCL,QUERY_DDL,QUERY_DML	
		• server_audit_excl_users =	rdsadmin
		• server_audit_logging	= 1
		• server_audit_logs_upload	= 1
		• log_output = FILE
7. Click **Save changes**.
8. Go to **Database Clustor** > **modify**.
9. Go to **Additional Configuration** > **Database options**.
10. Change the DB clustor parameter group.
11. Click **continue** and select **Apply immediately**.
12. Click **Modify Cluster**.
13. Reboot the DB Cluster for the changes to take effect.
		
## Viewing the Audit logs

The Audit logs can be seen in log files in RDS, and also on CloudWatch.
	
### Viewing the auditing details in RDS log files

The RDS log files can be viewed, watched, and downloaded. The name of the RDS log file is modifiable and is controlled by parameter log_filename.

#### Procedure
	1. Go to **Services** > **Database** > **RDS** > **Databases**.
	2. Select the database instance.
	3. Select the Logs & Events section.
	4. The end of the Logs section lists the files that contain the auditing details. The newest file is the last page.

### Viewing the logs entries on CloudWatch

By default, each database instance has an associated log group with a name in this format: /aws/rds/instance/<instance_name>/aurora-mysqlql. You can use this log group, or you can create a new one and associate it with the database instance.

#### Procedure
	1. On the AWS Console page, open the **Services** menu.
	2. Enter the CloudWatch string in the search box.
	3. Click **CloudWatch** to redirect to the CloudWatch dashboard.
	4. In the left panel, select **Log**s.
	5. Click **Log Groups**.
	

#### Limitations
	• The aurora-mysql plug-in does not support IPV6.
	• The aurora-mysql auditing does not audit Procedure, Function, and Show tables operations.
	• The source program will be seen as blank in the report.
	• Syntactically incorrect queries are not captured in audit logs.

## 4. Configuring the aurora-mysql filters in Guardium

The Guardium universal connector is the Guardium entry point for native audit logs. The Guardium universal connector identifies and parses the received events, and converts them to a standard Guardium format. The output of the Guardium universal connector is forwarded to the Guardium sniffer on the collector, for policy and auditing enforcements. Configure Guardium to read the native audit logs by customizing the aurora-mysql template.

### Authorizing outgoing traffic from AWS to Guardium

#### Procedure
	1. Log in to the Guardium API.
	2. Issue these commands:
		• `grdapi add_domain_to_universal_connector_allowed_domains domain=amazonaws.com`
		• `grdapi add_domain_to_universal_connector_allowed_domains domain=amazon.com`

#### Before you begin

•  Configure the policies you require. See [policies](/docs/#policies) for more information.

• You must have permission for the S-Tap Management role. The admin user includes this role by default.

• Aurora-MySQL-Guardium Logstash filter plug-in is automatically available with Guardium Data Protection versions 12.x, 11.4 with appliance bundle 11.0p490 or later or Guardium Data Protection version 11.5 with appliance bundle 11.0p540 or later releases.

• For Guardium Data Protection version 11.0p540 and/or 11.0p6505 and/or 12.0 and/or 12p15 download the [cloudwatch_logs plug-in](../../input-plugin/logstash-input-cloudwatch-logs/CloudwatchLogsInputPackage/offline-logstash-input-cloudwatch_log_1_0_5.zip)

**Note**: For Guardium Data Protection version 11.4 without appliance bundle 11.0p490 or prior or Guardium Data Protection version 11.5 without appliance bundle 11.0p540 or prior, download the [Aurora-Mysql-offlinePlugin.zip](https://github.com/IBM/universal-connectors/raw/main/filter-plugin/logstash-filter-aurora-mysql-guardium/AuroraMysqlOverCloudwatchPackage/AuroraMysql/Aurora-Mysql-offlinePlugin.zip). (Do not unzip the offline-package file throughout the procedure). 

#### Procedure
1. On the collector, go to Setup > Tools and Views > Configure Universal Connector.
2. First enable the Universal Guardium connector, if it is disabled already.
3. Click Upload File 
	* Select the offline [Aurora-Mysql-offlinePlugin.zip](https://github.com/IBM/universal-connectors/raw/main/filter-plugin/logstash-filter-aurora-mysql-guardium/AuroraMysqlOverCloudwatchPackage/AuroraMysql/Aurora-Mysql-offlinePlugin.zip) plug-in. After it is uploaded, click **OK**. This is not necessary for Guardium Data Protection v11.0p490 or later, v11.0p540 or later, v12.0 or later.		
	*  If you have installed Guardium Data Protection version 11.0p540 and/or 11.0p6505 and/or 12..0 and/or 12p15, select the offline [cloudwatch_logs plug-in](../../input-plugin/logstash-input-cloudwatch-logs/CloudwatchLogsInputPackage/offline-logstash-input-cloudwatch_log_1_0_5.zip). After it is uploaded, click **OK**.			 
4. Click the Plus sign to open the Connector Configuration dialog box.
5. Type a name in the **Connector name** field.
6. Update the input section to add the details from [auroraMysqlCloudwatch.conf](https://github.com/IBM/universal-connectors/raw/main/filter-plugin/logstash-filter-aurora-mysql-guardium/auroraMysqlCloudwatch.conf) file's input part, omitting the keyword "input{" at the beginning and its corresponding "}" at the end. 

**Note**: If you want to configure Cloudwatch with role_arn instead of access_key and secret_key then refer to the [Configuration for role_arn parameter in the cloudwatch_logs input plug-in](https://github.com/IBM/universal-connectors/blob/main/input-plugin/logstash-input-cloudwatch-logs/SettingsForRoleArn.md#configuration-for-role_arn-parameter-in-the-cloudwatch_logs-input-plug-in) topic.

7. Update the filter section to add the details from [auroraMysqlCloudwatch.conf](https://github.com/IBM/universal-connectors/raw/main/filter-plugin/logstash-filter-aurora-mysql-guardium/auroraMysqlCloudwatch.conf)  file's filter part, omitting the keyword "filter{" at the beginning and its corresponding "}" at the end.
8. The "type" fields should match in the input and the filter configuration sections. This field should be unique for every individual connector added.
9. Click **Save**. Guardium validates the new connector, and displays it in the Configure Universal Connector page.

## Configuring the Aurora-MySQL Guardium Logstash filters in Guardium Data Security Center.

To configure this plug-in for Guardium Data Security Center, follow [this guide.](/docs/Guardium%20Insights/3.2.x/UC_Configuration_GI.md)

For the input configuration step, refer to the [CloudWatch_logs section](/docs/Guardium%20Insights/3.2.x/UC_Configuration_GI.md#configuring-a-CloudWatch-input-plug-in).
