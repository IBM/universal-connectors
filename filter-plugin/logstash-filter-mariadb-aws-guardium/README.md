# MariaDB on Amazon RDS-Guardium Logstash filter plug-in
### Meet MariaDB on Amazon RDS
* Tested versions: 10.6.10, 10.5.17
* Environment: AWS
* Supported inputs: CloudWatch (pull)
* Supported Guardium versions:
   * Guardium Data Protection: 11.4 and above
   * Guardium Data Security Center SaaS: 1.0

This is a [Logstash](https://github.com/elastic/logstash) filter plug-in for the universal connector that is featured in IBM Security Guardium. It parses events and messages from the MariaDB audit log into a [Guardium record](https://github.com/IBM/universal-connectors/blob/main/common/src/main/java/com/ibm/guardium/universalconnector/commons/structures/Record.java) instance (which is a standard structure made out of several parts). The information is then sent over to Guardium. Guardium records include the accessor (the person who tried to access the data), the session, data, and exceptions. If there are no errors, the data contains details about the query and Guardium sniffer parses the MariaDB queries. The MariaDB on Amazon RDS plugin only supports Guardium Data Protection as of now.

The plug-in is free and open-source (Apache 2.0). It can be used as a starting point to develop additional filter plug-ins for the Guardium universal connector.


## Enabling the MariaDB Server Audit Logs

### Steps to enable MariaDB Server Audit Logs
1. Edit Inbound port rule
2. Create a new parameter group
3. Create a new option group and add MARIADB_AUDIT_PLUGIN 
4. Modify Parameter group and Option groups in DB Instance 	
   
#### Edit Inbound port rule

1. Select the MariaDB insatnce.
2. In **Connectivity & security**, click **VPC security group**.
3. Select **Inbound rules**, and then click **edit inbound rules** and set the custom value to 0.0.0.0/0.
4. Click **Add rules**, and then click **save rule**.

#### Create a new parameter group
To publish logs to CloudWatch, create a new parameter group and set the `log_output` parameter to `FILE`. When you create a database instance, it is associated with the default parameter group and cannot be modified. To create a new parameter group, follow these steps:

##### Procedure:

1. Open the Amazon RDS console (https://console.aws.amazon.com/rds).
2. In the navigation pane, choose **Parameter groups**.
3. Choose **Create parameter group** to open the Create parameter group dialog box.
4. In the **Parameter group family** list, choose your engine version.
5. In the **Group name** box, enter the name of the new DB parameter group.
6. In the **Description** box, enter a description for the new database parameter group.
7. Click **Create**.

##### Configure log_output:

1. Select **parameter group**, click **parameter group action** from the drop-down menu, and click **edit**.
2. In the **parameters filter** search box, filter by the `log_output`. Using the drop-down menu, set the `log_output` parameter to `FILE`.
3. click save changes.

	
#### Create a new Option groups and add MARIADB_AUDIT_PLUGIN
To add `MARIADB_AUDIT_PLUGIN` which will enable Server Audit Logs.

##### Procedure:

1. In the RDS dashboard, select **option groups** and then click **create group**.
2. In the **Create option group** window, do the following:
   - For **Name**, type a name for the option group.
   - For **Description**, type a brief description of the option group. The description is used for display purposes.
   - For **Engine**, choose whichever database engine you want to use.
   - For **Major engine version**, choose the major version of the database engine that you want to use.
   - To continue, choose **Create**.
3. To add `MARIADB_AUDIT_PLUGIN`, do the following:
   - Select the created Option groups and then click **Add options**.
   - Set the **option name** to MARIADB_AUDIT_PLUGIN, and then keep option setting parameters with the default values
   - Change the `SERVER_AUDIT_EXCL_USERS` value to rdsadmin
   - Set the value for `SERVER_AUDIT_EVENTS` to `QUERY, CONNECT` in order to see query and connection logs.
   - To enable the option immediately, choose **Yes** for **Apply Immediately**. (By default, **No** is selected instead.) Keep this default selection if you want the option enabled for each associated database instance during its next maintenance window.
   - Click **Add option**.

To add the MariaDB plug-in to a MySQL instance, follow the instructions described [here](https://docs.aws.amazon.com/AmazonRDS/latest/UserGuide/Appendix.MySQL.Options.AuditPlugin.html).

### Note
The 'rdsadmin' user queries the database every second to check its health. This activity may cause the log file to grow quickly to a very large size, which could result in unnecessary data proccessing in the filter. If recording this activity is not required, add the rdsadmin user to the `SERVER_AUDIT_EXCL_USERS` list.
	
##### Modify parameter and option groups in the database instance

1. Choose the database instance hyperlink and then choose **modify**.
2. In the **Settings** sections, confirm the password. Then, under **Additional configuration**, use the drop-down menu to modify the database parameter group and option group. 
3. For the last section, keep the default settings and click **Continue**.

## Connect to the MariaDB instance

#### Procedure

1. Download and Install MySQL Workbench.
2. Copy the endpoint and port from the MariaDB instance.
3. Open MySQL Workbench, choose a database connection, and specify an endpoint, port, and master credentials. Then, click **ok**.
4. Open the MySQL Workbench query editor with instance connection then execute some queries.
   
## Viewing the MariaDB audit logs 

### Viewing the logs entries on CloudWatch

By default, each database instance has an associated log group with a name in this format: /aws/rds/instance/<Instance_name>/audit. You can use this log group, or you can create a new one and associate it with the database instance.

#### Procedure

 1. On the AWS Console page, open the **Services** menu.
 2. Enter the CloudWatch string in the search box.
 3. Click **CloudWatch** to redirect to the CloudWatch dashboard.
 4. Select **Logs**.
 5. Click **Log Groups**.
	 
## 5.Configuring the MariaDB filter in Guardium

The Guardium universal connector is the Guardium entry point for native audit logs. The Guardium universal connector identifies and parses the received events, and converts them to a standard Guardium format. The output of the Guardium universal connector is forwarded to the Guardium sniffer on the collector, for policy and auditing enforcements. Configure Guardium to read the native audit logs by customizing the MariaDB template.

### Authorizing outgoing traffic from AWS to Guardium

#### Procedure

 1. Log in to the Guardium API.
 2. Issue these commands:
    
    `grdapi add_domain_to_universal_connector_allowed_domains domain=amazonaws.com`
	 
### Before you begin

*  Configure the policies you require. See [policies](/docs/#policies) for more information.
* You must have permission for the S-Tap Management role. The admin user includes this role by default.
* MariaDB on Amazon RDS-Guardium Logstash filter plug-in is automatically available with Guardium Data Protection versions 12.x, 11.4 with appliance bundle 11.0p490 or later or Guardium Data Protection version 11.5 with appliance bundle 11.0p540 or later releases.
* Download the plug-in filter configuration file [MariaDBCloudWatch.conf](https://github.com/IBM/universal-connectors/raw/main/filter-plugin/logstash-filter-mariadb-aws-guardium/MariaDBCloudWatch.conf).
* For Guardium Data Protection version 11.0p540 and/or 11.0p6505 and/or 12.0 and/or 12p15 download the [cloudwatch_logs plug-in](../../input-plugin/logstash-input-cloudwatch-logs/CloudwatchLogsInputPackage/offline-logstash-input-cloudwatch_log_1_0_5.zip)

**Note**: For Guardium Data Protection version 11.4 without appliance bundle 11.0p490 or prior or Guardium Data Protection version 11.5 without appliance bundle 11.0p540 or prior, download the [logstash-filter-awsmariadb_guardium_filter.zip](https://github.com/IBM/universal-connectors/releases/download/v1.5.6/logstash-filter-awsmariadb_guardium_filter.zip) plug-in. (Do not unzip the offline-package file throughout the procedure).

#### Procedure

1. On the collector, go to **Setup** > **Tools and Views** > **Configure Universal Connector**.
2. Enable the universal connector if it is disabled.
3. Click **Upload File** 
	*  Select [logstash-filter-awsmariadb_guardium_filter.zip](https://github.com/IBM/universal-connectors/releases/download/v1.5.6/logstash-filter-awsmariadb_guardium_filter.zip) plug-in. After it is uploaded, click **OK**. This is not necessary for Guardium Data Protection v12.0 and later.v11.0p490 or later, v11.0p540 or later, v12.0 or later.
	*  If you have installed Guardium Data Protection version 11.0p540 and/or 11.0p6505 and/or 12.0 and/or 12p15, select the offline [cloudwatch_logs plug-in](../../input-plugin/logstash-input-cloudwatch-logs/CloudwatchLogsInputPackage/offline-logstash-input-cloudwatch_log_1_0_5.zip). After it is uploaded, click **OK**.
4. Click the Plus sign to open the Connector Configuration dialog box.
5. Type a name in the **Connector name** field.
6. Update the input section to add the details from [MariaDBCloudWatch.conf](https://github.com/IBM/universal-connectors/raw/main/filter-plugin/logstash-filter-mariadb-aws-guardium/MariaDBCloudWatch.conf) file's input part, omitting the keyword "input{" at the beginning and its corresponding "}" at the end.

   **Note**: If you want to configure Cloudwatch with role_arn instead of access_key and secret_key then refer to the [Configuration for role_arn parameter in the cloudwatch_logs input plug-in](https://github.com/IBM/universal-connectors/blob/main/input-plugin/logstash-input-cloudwatch-logs/SettingsForRoleArn.md#configuration-for-role_arn-parameter-in-the-cloudwatch_logs-input-plug-in) topic.
7. Update the filter section to add the details from [MariaDBCloudWatch.conf](https://github.com/IBM/universal-connectors/raw/main/filter-plugin/logstash-filter-mariadb-aws-guardium/MariaDBCloudWatch.conf) file's filter part, omitting the keyword "filter{" at the beginning and its corresponding "}" at the end.
8. In the "type" fields should match in the input and filter configuration sections. This field should be unique for every individual connector added.
9. Click **Save**.  Guardium validates the new connector and displays it in the Configure Universal Connector page.

## 6. Limitations

 - The following important fields cannot be mappped with MariaDB audit logs:   
     - Source program : This field is left blank since this information is not embedded in the messages pulled from AWS Cloudwatch.
     - OS User : Not available with audit logs    
     - Client HostName : Not available with audit logs when we connect to the MariaDB instance through SQL standard and third party tools.
	 - serverIP : This field is populated with 0.0.0.0, as this information is not embedded in the messages pulled from AWS Cloudwatch.
     - clientPort and serverPort : Not available with audit logs
 - For system generated LOGIN_FAILED logs, the Dbuser value not available,so we set it as "NA".
 - Currently, S‑TAP registration is restricted to one primary MU, meaning the S‑TAP and its logs appear only on the initial primary MU even when multiple primary MUs are present.
 
## 7. Configuring the AWS MariaDB Guardium Logstash filters in Guardium Data Security Center

To configure this plug-in for Guardium Data Security Center, follow [this guide.](/docs/Guardium%20Insights/3.2.x/UC_Configuration_GI.md)

For the input configuration step, refer to the [CloudWatch_logs section](/docs/Guardium%20Insights/3.2.x/UC_Configuration_GI.md#configuring-a-CloudWatch-input-plug-in).

