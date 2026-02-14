## Meet AWS Postgres
* Environment: AWS
* Supported inputs: S3 (pull), SQS (pull)
* Supported Guardium versions:
    * Guardium Data Protection: 11.4 and above
    * Guardium Data Security Center: 3.2 and above
#### Notes
This is a [Logstash](https://github.com/elastic/logstash) filter plug-in for the universal connector that is featured in IBM Security Guardium. The filter supports events sent through Cloudwatch OR SQS.

## 1. Configuring the AWS Postgres service

1. Go to https://console.aws.amazon.com/
2. Click ```Services```
3. In the Database section, click ```RDS```
4. Select the region in the top right corner
5. In the central panel of the Amazon RDS Dashboard, click ```Create database```
6. Choose a database creation method
7. In the Engine options, select ```PostgreSQL```, and then select the appropropriate version
8. Select an appropriate template (Production, Dev/Test, or Free Tier)
9. In the Settings section, type the database instance name and create the master account with the username and password to log in to the database
10. Select the database instance size according to your requirements
11. Select appropriate storage options (for example, you may want to enable auto-scaling)
12. Select the availability and durability options
13. Select the connectivity settings that are appropriate for your environment. To make the database accessible, set the Public access option to Publicly Accessible within Additional Configuration
14. Select the type of Authentication for the database (choose from Password Authentication, Password and IAM database authentication, and Password and Kerberos authentication)
15. Expand the Additional Configuration options:

    a. Configure the database options

    b. Select options for Backup

    c. If desired, enable Encryption on the database instances

    d. In Log exports, select the Postgresql log type to publish to Amazon CloudWatch

    e. Select the options for Deletion protection

16. Click ```Create Database```
17. To view the database, click ```Databases``` under Amazon RDS in the left panel
18. To authorize inbound traffic, edit the security group:

    a. In the database summary page, select the Connectivity and Security tab. Under Security, click VPC security group

    b. Click the group name that you selected while creating a database (each database has one active group)

    c. In the Inbound rule section, choose to edit the inbound rules

    d. Set this rule:

    	• Type: PostgreSQL

    	• Protocol: TCP

    	• Port Range: 5432

    	Notes: Depending on your requirements, the source can be set to a specific IP address or it can be opened to all hosts.

    e. Click ```Add Rule ```and then click ```Save changes```. The database may need to be restarted

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

1. Go to ```Services``` > ```Database``` > ```Parameter groups```
2. Click Create Parameter Group in the left pane
3. Enter the parameter group details

   • Select the parameter group family. For example, aurora-postgres12. This version should match the version of the database that is created and with which this parameter group is to be associated

   • Enter the DB parameter group name

   • Enter the DB parameter group description

4. Click ```Save```. The new group appears in the Parameter Groups section

#### Enabling PGAudit Auditing

Session Auditing allows you to log activities that are selected in the pgaudit.log for logging. Be cautious when you select which activities will be logged, as logged activities can affect the performance of the database instance.

1. In the left-hand Amazon RDS panel, select Parameter Groups.
2. Select the parameter group that you created.
3. Click Edit parameters and add these settings:

   • pgaudit.log = all
   (Select the options from the Allowed values list. You can specify multiple values, and separate them with ",". The values that are marked with "-" are excluded while logging.)

   • pgaudit.log_catalog = 0

   • pgaudit.log_parameter = 0

   • shared_preload_libraries = pgaudit,pg_cron

   • log_error_verbosity = default

   • pgaudit.role = rds_pgaudit

   • log_destination = csvlog

   • cron.database_name = ```<database_name>```

#### Associating the DB Parameter Group with the database Instance

1. Go to ```Services``` > ```Database``` > ```RDS``` > ```Databases```
2. Click the Postgres database instance to be updated
3. Click ```Modify```
4. Go to the Additional Configuration ```section``` > ```database options``` > ```DB Parameter Group menu``` and select the ```newly-created group```
5. Click ```Continue```
6. Select the database instance in its configuration section. The state of the DB Parameter Group is pending-reboot
7. Reboot the database instance for the changes to take effect

## 3. Viewing the PGAudit logs

The PGAudit logs (both Session and Object logs) can be seen in log files in RDS, and also on CloudWatch:

### Viewing the auditing details in RDS log files

The RDS log files can be viewed, watched, and downloaded. The name of the RDS log file is modifiable and is controlled by parameter log_filename.

1. Go to Services > Database > RDS > Databases
2. Select the database instance
3. Select the Logs & Events section
4. The end of the Logs section lists the files that contain the auditing details. The newest file is the last page

### Viewing the logs entries on CloudWatch

By default, each database instance has an associated log group with a name in this format: /aws/rds/instance/<instance_name>/postgresql. You can use this log group, or you can create a new one and associate it with the database instance.

1. On the AWS Console page, open the Services menu
2. Enter the CloudWatch string in the search box
3. Click CloudWatch to redirect to the CloudWatch dashboard
4. In the left panel, select Logs
5. Click Log Groups

### Configuration
1. On the collector, go to ```Setup``` > ```Tools and Views``` > ```Configure Universal Connector```.
2. Enable the universal connector if it is disabled.
3. Click ```Upload File``` and select the offline [logstash-filter-s3sqs_postgresql_guardium_plugin_filter.zip](../../logstash-filter-postgres-guardium/PostgresOverS3SQS/logstash-filter-s3sqs_postgresql_guardium_plugin_filter.zip) plugin. After it is uploaded, click ```OK```. This step is not necessary for Guardium Data Protection v11.0p490 or later, v11.0p540 or later, v12.0 or later.
4. Click ```Upload File``` and select the offline [logstash-input-s3_sqs.zip](../../../input-plugin/logstash-input-s3sqs/InputS3SQSPackage/S3SQS/logstash-input-s3_sqs.zip) plugin. After it is uploaded, click ```OK```. This step is not necessary for Guardium Data Protection v11.0p490 or later, v11.0p540 or later, v12.0 or later.
4. Click the Plus sign to open the Connector Configuration dialog box.
5. Type a name in the ```Connector name``` field.
6. The audit logs are to be fetched from S3SQS directly, use the details from the [AWSS3SQSProstgre.conf](../../logstash-filter-postgres-guardium/PostgresOverS3SQSPackage/AWSS3SQSProstgre.conf) file. Update the input section to add the details from the corresponding file's input part, omitting the keyword "input{" at the beginning and its corresponding "}" at the end. More details on how to configure the relevant input plugin can be found [here](../../../input-plugin/logstash-input-s3sqs/README.md)
7. The audit logs are to be fetched from S3SQS directly, use the details from the [AWSS3SQSProstgre.conf](../../logstash-filter-postgres-guardium/PostgresOverS3SQSPackage/AWSS3SQSProstgre.conf) file. Update the filter section to add the details from the corresponding file's filter part, omitting the keyword "filter{" at the beginning and its corresponding "}" at the end.
8. The "type" fields should match in the input and the filter configuration sections. This field should be unique for every individual connector added. This is no longer required starting v12p20 and v12.1.
9. Click ```Save```. Guardium validates the new connector and displays it in the Configure Universal Connector page.
10. After the offline plug-in is installed and the configuration is uploaded and saved in the Guardium machine, restart the Universal Connector using the ```Disable/Enable``` button.

## Note:

### Exporting PostgreSQL or Aurora PostgreSQL Audit Logs to S3

You can export PostgreSQL or Aurora PostgreSQL audit logs to an S3 bucket using the following methods:

1. **Using Extensions (`log_fdw`, `aws_s3`, and `pg_cron`)**  
   Refer to the [PostgresExtLogsExport](../PostgresOverS3SQSPackage/PostgresExtLogsExport.md) guide for detailed instructions.

### Limitations:

System-generated queries may appear in the Full SQL Report when using SQL client tools (e.g., DBeaver, DBVisualizer, pgAdmin), which can result in duplicate query entries.
Role‑based authentication using AWS IAM Role ARNs is not supported for Postgres over S3SQL at this time.