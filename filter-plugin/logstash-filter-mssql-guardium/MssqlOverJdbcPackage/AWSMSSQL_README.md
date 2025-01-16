# AWS-MSSQL-Guardium Logstash filter plug-in

## Meet AWS MSSQL
* Tested versions: 14.00.3281.6 Enterprise Version and above
* Environment: AWS
* Supported inputs: JDBC (pull)
* Supported Guardium versions:
	* Guardium Data Protection: 11.4 and above
    * Guardium Insights :3.3
	* Guardium Insights SaaS: 1.0

## 1. Configuring AWS MSSQL RDS

### Procedure

	1. Create a database instance.
	
		a. Go to https://console.aws.amazon.com/
		b. Click on Services.
		c. In the Database section, click on RDS.
		d. Select the region in top right corner.
		e. On Amazon RDS Dashboard in central panel, click on Create database.
		f. In Choose a database creation method, select Standard Create.
		g. In Engine options, choose the engine type: Microsoft SQL Server and ‘SQL Server Enterprise Edition’.
		h. Chose version ‘SQL Server 2017 14.00.3281.6.v1’.
		i. Choose Dev/Test template
		j. Provide database name, master username and password. (this username and password will be used as an input in jdbc connection details for universal connector)
		k. Export logs: error logs can be selected.
		l. To access DB from outside, select public access to yes under Connectivity section.
		m. Select create database.
	
	2. Accessing database instance from outside
	
		To access DB instance from outside we need to add inbound rule to database.
			
			a. Click on database that we created in previous step.
			b. Go to ‘Connectivity & security’ tab.
			c. Under security, click on ‘VPC security group’(which is default we selected while creating database)
			d. Go to selected default security group
			e. Under ‘Inbound rule’ section, click on edit inbound rules.
			f. Click on Add rule button and add following two rules for MSSQL.
				I. Select type MSSQL from first drop down, in source column keep custom as default, click on search icon and select rule ‘0.0.0.0/0’.
				II. Select type MSSQL from first drop down, in source column keep custom as default, click on search icon and select rule ‘::/0’.
			g. We will be requiring “Microsoft MSSQL Management Studio” to connect with the database and do DB operations. To connect with DB, use endpoint and port which we will get under ‘Connectivity & security’ tab in rds instance.

	3. Assign parameter group to database instance.
	
		a. We can assign default parameter group to our database. Parameter group family should be ‘sqlserver-ee-14.0’ and parameter ‘rds.sqlserver_audit’ parameter should be set to true.
		b. In Navigation panel choose Databases.
		c. Select mssql database that we created. Click on Modify button.
		d. Go to Advance configurations.
		e. Under Database options, select parameter group from drop down.
		f. Click on Continue. On next window select Apply Immediately and click on Modify DB Instance.

	4. Create S3 bucket.

		a. Click on Services.
		b. Select S3 from services.
		c. Choose Create bucket.
		d. Provide Bucket name and AWS region. Click on Create bucket.

	5. Create custom option group to database instance.
	
		a. Click on Services.
		b. In the Database section, click on RDS.
		c. In the navigation pane, choose Option groups.
		d. Choose Create group.
		e. In the Create option group window, do the following:
			I. For Name, type a name for the option group that is unique within your AWS account. The name can contain only letters, digits, and hyphens.
			II. For Description, type a brief description of the option group.
			III. For Engine, choose the DB engine <sqlserver-ee>.
			IV. For Major engine version, choose the major version of the DB engine that you want.
		f. Select created option group and click on Add option.
		g. Select SQLSERVER_AUDIT as an option name.
		h. Set S3 bucket that we created in previous step.
		i. Create new IAM role.
		j. Need to create policy which we will be attaching to the IAM role. Use following JSON for same.
			{
				"Version": "2012-10-17",
				"Statement": [
					{
						"Effect": "Allow",
						"Action": "s3:ListAllMyBuckets",
						"Resource": "*"
					},
					{
						"Effect": "Allow",
						"Action": [
							"s3:ListBucket",
							"s3:GetBucketACL",
							"s3:GetBucketLocation"
						],
						"Resource": "arn:aws:s3:::<BUCKET_NAME>"
					},
					{
						"Effect": "Allow",
						"Action": [
							"s3:PutObject",
							"s3:ListMultipartUploadParts",
							"s3:AbortMultipartUpload"
						],
						"Resource": "arn:aws:s3:::<BUCKET_NAME>/*"
					}
				]
			}
		o. Select Apply Immediately in Scheduling option and click on Add option.


	6. Associate the option group with the DB instance
	
		a. In Navigation panel choose Databases.
		b. Select mssql database that we created. Click on Modify button
		c. Go to Advance configurations
		d. Under Database options. Select custom option group from drop down (which we have created in earlier step.)
		e. Click on Continue. On next window select Apply Immediately and click on Modify DB Instance.


## 2. Enabling auditing

	1. Connecting to database:
		
		a. Start the SQL Server Management Studio and provide connection details. Enter the ‘endpoint’ (for AWS, you will get this on AWS RDS console) as the Server Name. Provide the ‘username’ and the master ‘password’ that we had set while creating the database.
		b. Create database.

	2. About Audits:
		
		a. SQL Server audit lets you create server audits, which can contain server audit specifications for server level events, and database audit specifications for database level events.
		b. When you define an audit, you specify the location for the output of the results. This is the audit destination. The audit is created in a disabled state and does not automatically audit any actions. After the audit is enabled, the audit destination receives data from the audit.

	3. Creating audit:
	
		a. Create an audit (In management studio: Security -> Audits -> New Audit)
		b. Provide file path ([D:\rdsdbdata\SQLAudit\] default for AWS RDS instance).
		c. In Maximum file size, deselect Unlimited check box and provide a value.
		d. Keep remaining configurations as is.
		e. Click on Ok button.
		f. Right click on audit that we have created and select enable to enable it.
		
	4. Create audit specifications:
	
		a. Create a server audit specification:
		
			I. In management go to Security, expand it.
			II. Right click on Server audit specification option and select New audit specification.
			III. Select Audit that we created in earlier step.
			IV. Configure Audit log groups as per requirement. (Detailed audit log groups can be found on Microsoft documentation site)
			V. Click on Ok button.
			VI. Right click on server audit specification that we have created and select enable to enable it.
			
		b. Create database audit specification:
		
			I. In management go to Database, expand it.
			II. Expand security under it.
			III. Right click on Database audit specification option and select New audit specification.
			IV. Select Audit that we created in earlier step.
			V. Configure Audit log groups as per requirement. (Detailed audit log groups can be found on Microsoft documentation site)
			VI. Click on Ok button.
			VII. Right click on database audit specification that we have created and select enable to enable it.

### **Note: Create non-admin user to access audit table**

To access the audit table without exposing admin credentials, create a non-admin user with specific permissions:

- Log in to the database using admin credentials and run the following queries:
  ```sql
  CREATE LOGIN <login_name> WITH PASSWORD = '<password>';
  USE msdb;
  CREATE USER <user_name> FOR LOGIN <login_name>;
  GRANT SELECT ON msdb.dbo.rds_fn_get_audit_file TO <user_name>;
  ```

- In the input section, set the database name as **msdb**.
  ```properties
  jdbc_connection_string => "jdbc:sqlserver://<SERVER_NAME>:<PORT>;databaseName=msdb;"
  ```

- Use the login credentials created in the previous step for the JDBC connection:
  ```properties
  jdbc_user => "<login_name>"
  jdbc_password => "<password>"
  ```

- Update the input section by adding the details from the [awsNonAdminMSSQL.conf](./awsNonAdminMSSQL.conf) AWS MSSQL setup file, omitting the `input {` at the beginning and its corresponding `}` at the end.

## 3. Configuring the MSSQL filters in Guardium

The Guardium universal connector is the Guardium entry point for native audit logs. The universal connector identifies and parses received events, and then converts them to a standard Guardium format. The output of the universal connector is forwarded to the Guardium sniffer on the collector, for policy and auditing enforcements. Configure Guardium to read the native audit logs by customizing the MSSQL template.

### Authorizing outgoing traffic from AWS to Guardium

#### Procedure
	1. Log in to the Guardium API.
	2. Issue these commands:
		• grdapi add_domain_to_universal_connector_allowed_domains domain=amazonaws.com
		• grdapi add_domain_to_universal_connector_allowed_domains domain=amazon.com

#### Before you begin

• You must have LFD policy enabled on the collector. The detailed steps can be found in step 4 on [this page](https://www.ibm.com/docs/en/guardium/11.4?topic=dpi-installing-testing-filter-input-plug-in-staging-guardium-system).

• You must have permission for the S-Tap Management role. The admin user includes this role by default.

• Download the [mssql-offline-plugins-7.5.2.zip](./mssql-offline-plugins-7.5.2.zip) plug-in.This is not necessary for Guardium Data Protection v12.0 and later.

• Download the [mssql-jdbc-7.4.1.jre8](https://repo1.maven.org/maven2/com/microsoft/sqlserver/mssql-jdbc/7.4.1.jre8/mssql-jdbc-7.4.1.jre8.jar) jar.

#### Procedure: 

1. On the collector, go to Setup > Tools and Views > Configure Universal Connector.
2. First Enable the Universal Guardium connector, if it is Disabled already.
3. Click Upload File and select the offline [mssql-offline-plugins-7.5.2.zip](./mssql-offline-plugins-7.5.2.zip) plug-in. After it is uploaded, click OK.This is not necessary for Guardium Data Protection v12.0 and later.
4. Click Upload File and select the [mssql-jdbc-7.4.1.jre8](https://repo1.maven.org/maven2/com/microsoft/sqlserver/mssql-jdbc/7.4.1.jre8/mssql-jdbc-7.4.1.jre8.jar) jar. After it is uploaded, click OK.
5. Click the Plus sign to open the Connector Configuration dialog box.
6. Type a name in the Connector name field.
7. Update the input section to add the details from [awsMSSQL.conf](./awsMSSQL.conf) for AWS MSSQL setup file's input part, omitting the keyword "input{" at the beginning and its corresponding "}" at the end.

Note :

	• For Guardium Data Protection version 11.3, add the following line to the input section:
       'jdbc_driver_library => "${THIRD_PARTY_PATH}/mssql-jdbc-7.4.1.jre8.jar"'
	• If auditing was configured a while before the UC, the UC will still process all previous records, since they were already audited by the database.
8. Update the filter section to add the details from [awsMSSQL.conf](./awsMSSQL.conf) for AWS MSSQL setup file's filter part, omitting the keyword "filter{" at the beginning and its corresponding "}" at the end.
9. "type" field should match in input and filter configuration section. This field should be unique for  every individual connector added. 
10. If using two jdbc plug-ins on the same machine, the last_run_metadata_path file name should be different.
11. Click Save. Guardium validates the new connector, and enables the universal connector if it was
	disabled. After it is validated, it appears in the Configure Universal Connector page.

## 4. JDBC Load Balancing Configuration
	
a. In MSSQL JDBC input plug-in,we distribute load between two machines based on Even and Odd "session_id".

#### Procedure:

On the first G machine, in the input section for the JDBC plug-in, update the **statement** field in the JDBC block:

	SELECT event_time, session_id, database_name, client_ip, server_principal_name, application_name, statement, succeeded, DATEDIFF_BIG(ns, '1970-01-01 00:00:00.00000', event_time) AS updatedeventtime FROM msdb.dbo.rds_fn_get_audit_file('D:\rdsdbdata\SQLAudit\*.sqlaudit', default, default ) Where schema_name not in ('sys') and object_name NOT IN ('dbo','syssubsystems','fn_sysdac_is_currentuser_sa','backupmediafamily','backupset','syspolicy_configuration','syspolicy_configuration_internal','syspolicy_system_health_state','syspolicy_system_health_state_internal','fn_syspolicy_is_automation_enabled','spt_values','sysdac_instances_internal','sysdac_instances') and database_principal_name not in('public') and ((succeeded =1) or (succeeded =0 and statement like '%Login failed%')) and statement != '' and session_id%2= 0 and DATEDIFF_BIG(ns, '1970-01-01 00:00:00.00000', event_time) > :sql_last_value order by event_time;

On the second G machine, in the input section for the JDBC plug-in,  update the **statement** field in the JDBC block:

	SELECT event_time, session_id, database_name, client_ip, server_principal_name, application_name, statement, succeeded, DATEDIFF_BIG(ns, '1970-01-01 00:00:00.00000', event_time) AS updatedeventtime FROM msdb.dbo.rds_fn_get_audit_file('D:\rdsdbdata\SQLAudit\*.sqlaudit', default, default ) Where schema_name not in ('sys') and object_name NOT IN ('dbo','syssubsystems','fn_sysdac_is_currentuser_sa','backupmediafamily','backupset','syspolicy_configuration','syspolicy_configuration_internal','syspolicy_system_health_state','syspolicy_system_health_state_internal','fn_syspolicy_is_automation_enabled','spt_values','sysdac_instances_internal','sysdac_instances') and database_principal_name not in('public') and ((succeeded =1) or (succeeded =0 and statement like '%Login failed%')) and statement != '' and session_id%2= 1 and DATEDIFF_BIG(ns, '1970-01-01 00:00:00.00000', event_time) > :sql_last_value order by event_time;

## 5. Configuring the AWS MSSQL Guardium Logstash filters in Guardium Insights

To configure this plug-in for Guardium Insights, follow [this guide.](/docs/Guardium%20Insights/3.2.x/UC_Configuration_GI.md)