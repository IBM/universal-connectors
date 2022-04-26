## Postgres-Guardium Logstash filter plug-in

This is a [Logstash](https://github.com/elastic/logstash) filter plug-in for the universal connector that is featured in IBM Security Guardium. It parses events and messages from the Postgres audit log into a Guardium record instance (which is a standard structure made out of several parts). The information is then sent over to Guardium. Guardium records include the accessor (the person who tried to access the data), the session, data, and exceptions. If there are no errors, the data contains details about the query "construct". The construct details the main action (verb) and collections (objects) involved.

Currently, this plug-in will work only on IBM Security Guardium Data Protection, not Guardium Insights.

This plugin is written in Ruby and so is a script that can be directly copied into Guardium configuration of Universal Connectors. There is no need to upload the plugin code. However, in order to support few features one zip has to be added named, postgres-offline-plugins-7.5.2.zip

The plug-in is free and open-source (Apache 2.0). It can be used as a starting point to develop additional filter plug-ins for Guardium universal connector.

### NOTES
* GDP: requires installation of [json_encode](https://www.elastic.co/guide/en/logstash-versioned-plugins/current/v3.0.3-plugins-filters-json_encode.html) filter plug-in

## Limitations
	• The postgres plug-in does not support IPV6.
	• PGAudit logs the batch queries multiple times, so report will show multiple entries for the same.

## Configuring the AWS Postgres service

### Procedure:
	1. Go to https://console.aws.amazon.com/.
	2. Click Services.
	3. In the Database section, click RDS.
	4. Select the region in the top right corner.
	5. In the central panel of the Amazon RDS Dashboard, click Create database.
	6. Choose a database creation method.
	7. In the Engine options, select PostgreSQL, and then select the appropropriate version.
	8. Select an appropriate template (Production, Dev/Test, or Free Tier).
	9. In the Settings section, type the database instance name and create the master account with the username and password to log in to the database.
	10. Select the database instance size according to your requirements.
	11. Select appropriate storage options (for example, you may want to enable auto scaling).
	12. Select the Availability and durability options.
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
		b. Click the group name that you selected while creating database (each database has one active group).
		c. In the Inbound rule section, choose to edit the inbound rules.
		d. Set this rule:
			• Type: PostgreSQL
			• Protocol: TCP
			• Port Range: 5432
			(depending on your requirements, the Source can be set to a specific IP address or it can be opened to all hosts)
		e. Click Add Rule and then click Save changes.
		The database may need to be restarted.

## Enabling the PGAudit extension

There are different ways of auditing and logging in postgres. For this exercise, we will use PGAudit, the open
source audit logging extension for PostgreSQL 9.5+. This extension supports logging for Sessions and Objects.

	1. “Creating the database parameter group”
	2. “Enabling PGAudit Session Auditing”
	3. “Enabling PGAudit Object Auditing”
	4. “Associating the DB Parameter Group with the database Instance”

## Creating the database parameter group

When you create a database instance, it is associated with the default parameter group. Follow these
steps to create a new parameter group:

### Procedure
	1. Go to Services > Database > Parameter groups.
	2. Click Create Parameter Group in the left pane.
	3. Enter the parameter group details.
		• Select the parameter group family, for example, postgres 12, this version should match the version of the database that is created and with which this parameter group is to be associated.
		• Enter the DB parameter group name.
		• Enter the DB parameter group description.
	4. Click Save. The new group appears in the Parameter Groups section.

## Enabling PGAudit Session Auditing

Session Auditing allows you to log activities that are selected in the pgaudit.log for logging. Be cautious when you select which activities will be logged, as logged activities can affect the performance of the database instance.


### Procedure
	1. In the lefthand Amazon RDS panel, select Parameter Groups.
	2. Select the parameter group that you created.
	3. Click Edit parameters and add these settings:
		• pgaudit.log = all, -misc (Select the options from the Allowed values list. You can specify multiple values, and separate them with “,”. The values that are marked with “-” are excluded while logging.)
		• pgaudit.log_catalog = 0
		• pgaudit.log_parameter = 0
		• shared_preload_libraries = pgaudit
		• log_error_verbosity = default
	4. Provide the required permissions to this role while associating it with the table that is audited. For example, grant ALL on <relation_name> to rds_pgaudit (this grant enables full SELECT, INSERT, UPDATE, and DELETE logging on the relation_name).

## Enabling PGAudit Object Auditing

Object auditing affects the performance less than session auditing, due to the fine-grained criteria of tables and column that you can choose for auditing.

### Procedure
	1. Set these parameters:
		• pgaudit.log = none (since this is not needed for extensive SESSION logging)
		• pgaudit.role = rds_pgaudit
		• pgaudit.log_catalog = 0
		• pgaudit.log_parameter = 0
		• shared_preload_libraries = pgaudit
		• log_error_verbosity = default
	2. Provide the required permissions to the rds_pgaudit role while associating it with the table that is audited. For example, grant ALL on <relation_name> to rds_pgaudit (this grant enables full SELECT, INSERT, UPDATE, and DELETE logging on the relation_name).

## Associating the DB Parameter Group with the database Instance

### Procedure
	1. Go to Services > Database > RDS > Databases.
	2. Click the Postgres database instance to be updated.
	3. Click Modify.
	4. Go to the Additional Configuration section > database options > DB Parameter Group menu and select the newly-created group.
	5. Click Continue.
	6. Select the database instance in its configuration section. The state of the DB Parameter Group is pending-reboot.
	7. Reboot the Database instance for the changes to take effect.


## Viewing the pgaudit logs

The PGAudit logs (both Session and Object logs) can be seen in log files in RDS, and also on CloudWatch.
	• “Viewing the auditing details in RDS log files”
	• “Viewing the logs entries on CloudWatch”

## Viewing the auditing details in RDS log files

The RDS log files can be viewed, watched, and downloaded. The name of the RDS log file is modifiable and is controlled by parameter log_filename.

### Procedure
	1. Go to Services > Database > RDS > Databases.
	2. Select the database instance.
	3. Select the Logs & Events section.
	4. The end of the Logs section lists the files that contain the auditing details. The newest file is the last page.

## Viewing the logs entries on CloudWatch

By default, each database instance has an associated log group with a name in this format: /aws/rds/instance/<instance_name>/postgresql. You can use this log group, or you can create a new one and associate it with the database instance.

### Procedure
	1. On the AWS Console page, open the Services menu.
	2. Enter the CloudWatch string in the search box.
	3. Click CloudWatch to redirect to the CloudWatch dashboard.
	4. In the left panel, select Logs.
	5. Click Log Groups.

## Authorizing outgoing traffic from AWS to Guardium

### Procedure
	1. Log in to the Guardium API.
	2. Issue these commands:
		• grdapi add_domain_to_universal_connector_allowed_domains domain=amazonaws.com
		• grdapi add_domain_to_universal_connector_allowed_domains domain=amazon.com


## Configuring the postgres filters in Guardium

The Guardium universal connector is the Guardium entry point for native audit logs. The Guardium universal connector identifies and parses the received events, and converts them to a standard Guardium format. The output of the Guardium universal connector is forwarded to the Guardium sniffer on the collector, for policy and auditing enforcements. Configure Guardium to read the native audit logs by customizing the postgres template.

## Before you begin
	• You must have permission for the S-Tap Management role. The admin user includes this role by default.
	• Download the postgres-offline-plugins-7.5.2.zip plug-in.

# Procedure
	1. On the collector, go to Setup > Tools and Views > Configure Universal Connector.
	2. First Enable the Universal Guardium connector, if it is Disabled already.
	3. Click Upload File and select the offline postgres-offline-plugins-7.5.2.zip plug-in. After it is uploaded, click OK.
	4. Click the Plus sign to open the Connector Configuration dialog box.
	5. Type a name in the Connector name field.
	6. Update the input section to add the details from postgresCloudwatch.conf file's input part, omitting the keyword "input{" at the beginning and its corresponding "}" at the end.
	7. Update the filter section to add the details from postgresCloudwatch.conf file's filter part, omitting the keyword "filter{" at the beginning and its corresponding "}" at the end.
	8. "type" field should match in input and filter configuration section. This field should be unique for  every individual connector added.
	9. Click Save. Guardium validates the new connector, and enables the universal connector if it was
	disabled. After it is validated, it appears in the Configure Universal Connector page.

