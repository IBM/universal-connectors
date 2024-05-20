# Aurora Postgres

## Enabling the PGAudit extension

There are different ways of auditing and logging in postgres. For this exercise, we will use PGAudit, the open
source audit logging extension for PostgreSQL 9.5+. This extension supports logging for Sessions or Objects.
Configure either session auditing or object auditing. You cannot enable both at the same time.

### Steps to enable PGAudit

1. Create the database parameter group
2. Enable auditing using either one of the following:

    a. Enabling PGAudit Session Auditing

    b. Enabling PGAudit Object Auditing

3. Associating the DB Parameter Group with the database Instance

#### Creating the database parameter group

When you create a database instance, it is associated with the default parameter group. Follow these
steps to create a new parameter group:

##### Procedure
1. Go to **Services** > **Database** > **Parameter groups**.
2. Click **Create Parameter Group** in the left pane.
3. Enter the parameter group details.

	• Select the parameter group family. For example, aurora-postgres12. This version should match the version of the database that is created and with which this parameter group is to be associated.

	• Enter the DB parameter group name.

	• Enter the DB parameter group description.
4. Click **Save**. The new group appears in the **Parameter Groups** section.

#### Enabling PGAudit Session Auditing

Session Auditing allows you to log activities that are selected in the pgaudit.log for logging. Be cautious when you select which activities will be logged, as logged activities can affect the performance of the database instance.


##### Procedure
1. In the Amazon RDS panel, select **Parameter Groups**.
2. Select the parameter group that you created.
3. Click **Edit parameters** and add these settings:

	• `pgaudit.log = all, -misc`

	(Select the options from the **Allowed values** list. You can specify multiple values, and separate them with “,”. The values that are marked with “-” are excluded while logging.)

	• `pgaudit.log_catalog = 0`

	• `pgaudit.log_parameter = 0`

	• `shared_preload_libraries = pgaudit`

	• `log_error_verbosity = default`


#### Enabling PGAudit Object Auditing

Object auditing affects the performance less than session auditing, due to the fine-grained criteria of tables and columns that you can choose for auditing.

##### Procedure
1. Set these parameters:

	• `pgaudit.log = none` (since this is not needed for extensive SESSION logging)

	• `pgaudit.role = rds_pgaudit`

	• `pgaudit.log_catalog = 0`

	• `pgaudit.log_parameter = 0`

	• `shared_preload_libraries = pgaudit`

	• `log_error_verbosity = default`
	
2. Provide the required permissions to the rds_pgaudit role while associating it with the table that is audited. For example, grant `ALL` on `<relation_name>` to `rds_pgaudit` (this grant enables full `SELECT`, `INSERT`, `UPDATE`, and `DELETE	 logging on the `relation_name`).

#### Associating the DB parameter group with the database instance

##### Procedure
1. Go to **Services** > **Database** > **RDS** > **Databases**.
2. Click the Aurora Postgres database instance to be updated.
3. Click **Modify**.
4. Go to the **Additional Configuration** section > **database options** > **DB Parameter Group** menu and select the newly-created group.
5. Click **Continue**.
6. Select the database instance in its configuration section. The state of the DB Parameter Group is pending-reboot.
7. Reboot the database instance for the changes to take effect.


## 3. Viewing the PGAudit logs

The PGAudit logs (both session and object logs) can be seen in log files in RDS, and also on CloudWatch:


### Viewing the auditing details in RDS log files

The RDS log files can be viewed, watched, and downloaded. The name of the RDS log file is modifiable and is controlled by parameter log_filename.

#### Procedure
1. Go to **Services** > **Database** > **RDS** > **Databases**.
2. Select the database instance.
3. Select the **Logs & Events** section.
4. The end of the Logs section lists the files that contain the auditing details. The newest file is the last page.

### Viewing the logs entries on CloudWatch

By default, each database instance has an associated log group with a name in this format: /aws/rds/cluster/<instance_name>/postgresql. You can use this log group, or you can create a new one and associate it with the database instance.

#### Procedure
1. On the AWS Console page, open the **Services** menu.
2. Enter the CloudWatch string in the search box.
3. Click CloudWatch to redirect to the CloudWatch dashboard.
4. In the left panel, select **Logs**.
5. Click **Log Groups**.


#### Notes
* Guardium Data Protection requires installation of the [json_encode](https://www.elastic.co/guide/en/logstash-versioned-plugins/current/v3.0.3-plugins-filters-json_encode.html) filter plug-in.

## 4. Configuring the Postgres filters in Guardium

The Guardium universal connector is the Guardium entry point for native audit logs. The universal connector identifies and parses the received events, and converts them to a standard Guardium format. The output of the universal connector is forwarded to the Guardium sniffer on the collector, for policy and auditing enforcements. Configure Guardium to read the native audit logs by customizing the Postgres template.

### Authorizing outgoing traffic from AWS to Guardium

#### Procedure
1. Log in to the Guardium Collector's API.
2. Issue these commands:
   
   • `grdapi add_domain_to_universal_connector_allowed_domains domain=amazonaws.com`

   • `grdapi add_domain_to_universal_connector_allowed_domains domain=amazon.com`

#### Before you begin
•  Configure the policies you require. See [policies](/docs/#policies) for more information.

• You must have permission for the S-Tap Management role. The admin user includes this role by default.

• This plug-in is automatically available with Guardium Data Protection versions 12.x, 11.4 with appliance bundle 11.0p490 or later or Guardium Data Protection version 11.5 with appliance bundle 11.0p540 or later releases.

• For Guardium Data Protection version 11.0p540 and/or 11.0p6505 and/or 12p15 download the [cloudwatch_logs plug-in](../../input-plugin/logstash-input-cloudwatch-logs/CloudwatchLogsInputPackage/offline-logstash-input-cloudwatch_log_1_0_5.zip)

**Note**: For Guardium Data Protection version 11.4 without appliance bundle 11.0p490 or prior or Guardium Data Protection version 11.5 without appliance bundle 11.0p540 or prior, download the [postgres-offline-plugins-7.5.2.zip](https://github.com/IBM/universal-connectors/raw/release-v1.2.0/filter-plugin/logstash-filter-postgres-guardium/PostgresOverCloudWatchPackage/Postgres/postgres-offline-plugins-7.5.2.zip) plug-in. (Do not unzip the offline-package file throughout the procedure). 

#### Procedure

1. On the collector, go to **Setup** > **Tools and Views** > **Configure Universal Connector**.
2. Enable the universal connector if it is disabled.
3. Click **Upload File** and 
	*  Select the [offline postgres-offline-plugins-7.5.2.zip](https://github.com/IBM/universal-connectors/raw/release-v1.2.0/filter-plugin/logstash-filter-postgres-guardium/PostgresOverCloudWatchPackage/Postgres/postgres-offline-plugins-7.5.2.zip) plug-in. After it is uploaded, click **OK**. This is not necessary for Guardium Data Protection v11.0p490 or later, v11.0p540 or later, v12.0 or later.
	*  If you have installed Guardium Data Protection version 11.0p540 and/or 11.0p6505 and/or 12p15, select the offline [cloudwatch_logs plug-in](../../input-plugin/logstash-input-cloudwatch-logs/CloudwatchLogsInputPackage/offline-logstash-input-cloudwatch_log_1_0_5.zip). After it is uploaded, click **OK**.
4. Click the Plus sign to open the Connector Configuration dialog box.
5. Type a name in the **Connector name** field.
6. Update the input section to add the details from the [postgresCloudwatch.conf](https://github.com/IBM/universal-connectors/blob/main/filter-plugin/logstash-filter-postgres-guardium/PostgresOverCloudWatchPackage/postgresCloudwatch.conf)  file's input part, omitting the keyword "input{" at the beginning and its corresponding "}" at the end.
7. Update the filter section to add the details from the [postgresCloudwatch.conf](https://github.com/IBM/universal-connectors/blob/main/filter-plugin/logstash-filter-postgres-guardium/PostgresOverCloudWatchPackage/postgresCloudwatch.conf) file's filter part, omitting the keyword "filter{" at the beginning and its corresponding "}" at the end.
8. The "type" fields should match in the input and the filter configuration sections. This field should be unique for  every individual connector added.
9. Click **Save**. Guardium validates the new connector and displays it in the Configure Universal Connector page.
