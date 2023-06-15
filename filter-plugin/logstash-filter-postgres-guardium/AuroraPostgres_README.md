## 1. Configuring the Aurora Postgres service

### Procedure:

1. Go to https://console.aws.amazon.com/.
2. Click Services.
3. In the Database section, click RDS.
4. Select the region in the top right corner.
5. In the central panel of the Amazon RDS Dashboard, click Create database.
6. Choose a database creation method.
7. In the Engine options, select Amazon Aurora
8. Select the Edtion as Amazon Aurora PostgreSQL-Compatible Edition and then select the appropropriate version.
8. Select an appropriate template (Production, Dev/Test, or Free Tier).
9. In the Settings section, type the database instance name and create the master account with the username and password to log in to the database.
10. Select the database instance size according to your requirements.
11. Select appropriate storage options (for example, you may want to enable auto-scaling).
12. Select the availability and durability options.
13. Select the connectivity settings that are appropriate for your environment. To make the database accessible, set the Public access option to Publicly Accessible within Additional Configuration.
14. Select the type of Authentication for the database (choose from Password Authentication, Password and IAM database authentication, and Password and Kerberos authentication).
15. Expand the Additional Configuration options:

	a. Configure the database options.

	b. Select options for Backup.

	c. If desired, enable Encryption on the database instances.

	d. In Log exports, select the Postgresql log type to publish to Amazon CloudWatch.

	e. Select the options for Deletion protection.
16. Click Create Database.
17. To view the database, click Databases under Amazon RDS in the left panel.
18. To authorize inbound traffic, edit the security group:

	a. In the database summary page, select the Connectivity and Security tab. Under Security, click VPC security group.

	b. Click the group name that you selected while creating a database (each database has one active group).

	c. In the Inbound rule section, choose to edit the inbound rules.

	d. Set this rule:

	• Type: PostgreSQL

	• Protocol: TCP

	• Port Range: 5432
	(depending on your requirements, the source can be set to a specific IP address or it can be opened to all hosts)

	e. Click Add Rule and then click Save changes.

	The database may need to be restarted.

## 2. Enabling the PGAudit extension

There are different ways of auditing and logging in postgres. For this exercise, we will use PGAudit, the open
source audit logging extension for PostgreSQL 9.5+. This extension supports logging for Sessions or Objects.
Configure either Session Auditing or Object Auditing. You cannot enable both at the same time.

### Steps to enable PGAudit

1. Creating the database parameter group
2. Enabling Auditing using **either one** of the following:

    a. Enabling PGAudit Session Auditing

    b. Enabling PGAudit Object Auditing

3. Associating the DB Parameter Group with the database Instance

#### Creating the database parameter group

When you create a database instance, it is associated with the default parameter group. Follow these
steps to create a new parameter group:

##### Procedure
1. Go to Services > Database > Parameter groups.
2. Click Create Parameter Group in the left pane.
3. Enter the parameter group details.

	• Select the parameter group family. For example, aurora-postgres12. This version should match the version of the database that is created and with which this parameter group is to be associated.

	• Enter the DB parameter group name.

	• Enter the DB parameter group description.
4. Click Save. The new group appears in the Parameter Groups section.

#### Enabling PGAudit Session Auditing

Session Auditing allows you to log activities that are selected in the pgaudit.log for logging. Be cautious when you select which activities will be logged, as logged activities can affect the performance of the database instance.


##### Procedure
1. In the left-hand Amazon RDS panel, select Parameter Groups.
2. Select the parameter group that you created.
3. Click Edit parameters and add these settings:

	• pgaudit.log = all, -misc

	(Select the options from the Allowed values list. You can specify multiple values, and separate them with “,”. The values that are marked with “-” are excluded while logging.)

	• pgaudit.log_catalog = 0

	• pgaudit.log_parameter = 0

	• shared_preload_libraries = pgaudit

	• log_error_verbosity = default


#### Enabling PGAudit Object Auditing

Object auditing affects the performance less than session auditing, due to the fine-grained criteria of tables and columns that you can choose for auditing.

##### Procedure
1. Set these parameters:

	• pgaudit.log = none (since this is not needed for extensive SESSION logging)

	• pgaudit.role = rds_pgaudit

	• pgaudit.log_catalog = 0

	• pgaudit.log_parameter = 0

	• shared_preload_libraries = pgaudit

	• log_error_verbosity = default
	
2. Provide the required permissions to the rds_pgaudit role while associating it with the table that is audited. For example, grant ALL on <relation_name> to rds_pgaudit (this grant enables full SELECT, INSERT, UPDATE, and DELETE logging on the relation_name).

#### Associating the DB Parameter Group with the database Instance

##### Procedure
1. Go to Services > Database > RDS > Databases.
2. Click the Aurora Postgres database instance to be updated.
3. Click Modify.
4. Go to the Additional Configuration section > database options > DB Parameter Group menu and select the newly-created group.
5. Click Continue.
6. Select the database instance in its configuration section. The state of the DB Parameter Group is pending-reboot.
7. Reboot the database instance for the changes to take effect.


## 3. Viewing the PGAudit logs

The PGAudit logs (both Session and Object logs) can be seen in log files in RDS, and also on CloudWatch:


### Viewing the auditing details in RDS log files

The RDS log files can be viewed, watched, and downloaded. The name of the RDS log file is modifiable and is controlled by parameter log_filename.

#### Procedure
1. Go to Services > Database > RDS > Databases.
2. Select the database instance.
3. Select the Logs & Events section.
4. The end of the Logs section lists the files that contain the auditing details. The newest file is the last page.

### Viewing the logs entries on CloudWatch

By default, each database instance has an associated log group with a name in this format: /aws/rds/cluster/<instance_name>/postgresql. You can use this log group, or you can create a new one and associate it with the database instance.

#### Procedure
1. On the AWS Console page, open the Services menu.
2. Enter the CloudWatch string in the search box.
3. Click CloudWatch to redirect to the CloudWatch dashboard.
4. In the left panel, select Logs.
5. Click Log Groups.


#### Notes
* GDP: requires installation of [json_encode](https://www.elastic.co/guide/en/logstash-versioned-plugins/current/v3.0.3-plugins-filters-json_encode.html) filter plug-in

## 4. Configuring the postgres filters in Guardium

The Guardium universal connector is the Guardium entry point for native audit logs. The Guardium universal connector identifies and parses the received events, and converts them to a standard Guardium format. The output of the Guardium universal connector is forwarded to the Guardium sniffer on the collector, for policy and auditing enforcements. Configure Guardium to read the native audit logs by customizing the postgres template.

### Authorizing outgoing traffic from AWS to Guardium

#### Procedure
1. Log in to the Guardium Collector's API.
2. Issue these commands:
		• grdapi add_domain_to_universal_connector_allowed_domains domain=amazonaws.com
		• grdapi add_domain_to_universal_connector_allowed_domains domain=amazon.com

#### Before you begin
•  Configure the policies you require. See [policies](/../../#policies) for more information.

• You must have permission for the S-Tap Management role. The admin user includes this role by default.

• Download the [postgres-offline-plugins-7.5.2.zip plug-in.](https://github.com/IBM/universal-connectors/raw/main/filter-plugin/logstash-filter-postgres-guardium/PostgresOverCloudWatchPackage/Postgres/postgres-offline-plugins-7.5.2.zip)

#### Procedure

1. On the collector, go to Setup > Tools and Views > Configure Universal Connector.
2. First enable the Universal Guardium connector, if it is disabled already.
3. Click Upload File and select the [offline postgres-offline-plugins-7.5.2.zip](https://github.com/IBM/universal-connectors/raw/main/filter-plugin/logstash-filter-postgres-guardium/PostgresOverCloudWatchPackage/Postgres/postgres-offline-plugins-7.5.2.zip) plug-in. After it is uploaded, click OK.
4. Click the Plus sign to open the Connector Configuration dialog box.
5. Type a name in the Connector name field.
6. Update the input section to add the details from the [postgresCloudwatch.conf](https://github.com/IBM/universal-connectors/raw/main/filter-plugin/logstash-filter-postgres-guardium/postgresCloudwatch.conf)  file's input part, omitting the keyword "input{" at the beginning and its corresponding "}" at the end.
7. Update the filter section to add the details from the [postgresCloudwatch.conf](https://github.com/IBM/universal-connectors/raw/main/filter-plugin/logstash-filter-postgres-guardium/postgresCloudwatch.conf) file's filter part, omitting the keyword "filter{" at the beginning and its corresponding "}" at the end.
8. The "type" fields should match in the input and the filter configuration sections. This field should be unique for  every individual connector added.
9. Click Save. Guardium validates the new connector, and enables the universal connector if it was
	disabled. After it is validated, it appears in the Configure Universal Connector page.
