# MSSQL-Guardium Logstash filter plug-in
### Meet MSSQL
* Tested versions: 14.0.x
* Environment: AWS, On-premise
* Supported inputs: JDBC (pull)
* Supported Guardium versions:
	* Guardium Data Protection: 11.4 and above
    * Guardium Insights SaaS: 1.0

This is a [Logstash](https://github.com/elastic/logstash) filter plug-in for the universal connector that is featured in IBM Security Guardium. It parses events and messages from the MSSQL audit log into a [Guardium record](https://github.com/IBM/universal-connectors/blob/main/common/src/main/java/com/ibm/guardium/universalconnector/commons/structures/Record.java)  instance (which is a standard structure made out of several parts). The information is then sent over to Guardium. Guardium records include the accessor (the person who tried to access the data), the session, data, and exceptions. If there are no errors, the data contains details about the query "construct". The construct details the main action (verb) and collections (objects) involved.

This plugin is written in Ruby and so is a script that can be directly copied into Guardium configuration of Universal Connectors. There is no need to upload the plugin code. However, in order to support few features one zip has to be added named, mssql-offline-plugins-7.5.2.zip and a jar file for mssql has to be uploaded named, mssql-jdbc-7.4.1.jre8.

The plug-in is free and open-source (Apache 2.0). It can be used as a starting point to develop additional filter plug-ins for Guardium universal connector.

## 1. Configuring AWS MSSQL RDS

### Procedure
1. Create database instance
   1. Go to https://console.aws.amazon.com/
   2. Click on Services.
   3. In the Database section, click on RDS.
   4. Select the region in top right corner.
   5. On Amazon RDS Dashboard in central panel, click on Create database.
   6. In Choose a database creation method, select Standard Create.
   7. In Engine options, choose the engine type: Microsoft SQL Server and ‘SQL Server Enterprise Edition’.
   8. Chose version ‘SQL Server 2017 14.00.3281.6.v1’.
   9. Choose Dev/Test template
   10. Provide database name, master username and password. (this username and password will be used as an input in jdbc connection details for universal connector)
   11. Export logs: error logs can be selected.
   12. To access DB from outside, select public access to yes under Connectivity section.
   13. Select create database.
   
2. Accessing database instance from outside - To access DB instance from outside we need to add inbound rule to database.
   1. Click on database that we created in previous step.
   2. Go to ‘Connectivity & security’ tab.
   3. Under security, click on ‘VPC security group’(which is default we selected while creating database)
   4. Go to selected default security group
   5. Under ‘Inbound rule’ section, click on edit inbound rules.
   6. Click on Add rule button and add following two rules for MSSQL.
   7. Select type MSSQL from first drop down, in source column keep custom as default, click on search icon and select rule ‘0.0.0.0/0’.
   8. Select type MSSQL from first drop down, in source column keep custom as default, click on search icon and select rule ‘::/0’.
   9. We will be requiring “Microsoft MSSQL Management Studio” to connect with the database and do DB operations. To connect with DB, use endpoint and port which we will get under ‘Connectivity & security’ tab in rds instance.
 
3. Assign parameter group to database instance.
   1. We can assign default parameter group to our database. Parameter group family should be ‘sqlserver-ee-14.0’ and parameter ‘rds.sqlserver_audit’ parameter should be set to true.
   2. In Navigation panel choose Databases.
   3. Select mssql database that we created. Click on Modify button.
   4. Go to Advance configurations.
   5. Under Database options, select parameter group from drop down.
   6. Click on Continue. On next window select Apply Immediately and click on Modify DB Instance.
   
4. Create S3 bucket.
   1. Click on Services.
   2. Select S3 from services.
   3. Choose Create bucket.
   4. Provide Bucket name and AWS region. Click on Create bucket.
   
5. Create custom option group to database instance.
   1. Click on Services.
   2. In the Database section, click on RDS.
   3. In the navigation pane, choose Option groups.
   4. Choose Create group.
   5. In the Create option group window, do the following:
      1. For Name, type a name for the option group that is unique within your AWS account. The name can contain only letters, digits, and hyphens.
      2. For Description, type a brief description of the option group.
      3. For Engine, choose the DB engine <sqlserver-ee>.
      4. For Major engine version, choose the major version of the DB engine that you want.
   6. Select created option group and click on Add option.
   7. Select SQLSERVER_AUDIT as an option name.
   8. Set S3 bucket that we created in previous step.
   9. Create new IAM role.
   10. Need to create policy which we will be attaching to the IAM role. Use following JSON for same.
   11. ``` 
       {
          "Version": "2012-10-17",
          "Statement": [{
             "Effect": "Allow",
             "Action": "s3:ListAllMyBuckets",
             "Resource": "*"
          },{
             "Effect": "Allow",
             "Action": [
                "s3:ListBucket",
                "s3:GetBucketACL",
                "s3:GetBucketLocation"
             ],
             "Resource": "arn:aws:s3:::<BUCKET_NAME>"
          },{
             "Effect": "Allow",
             "Action": [
                "s3:PutObject",
                "s3:ListMultipartUploadParts",
                "s3:AbortMultipartUpload"
             ],
             "Resource": "arn:aws:s3:::<BUCKET_NAME>/*"
          }]
       }
       ```
   12. Select Apply Immediately in Scheduling option and click on Add option.

6. Associate the option group with the DB instance
   1. In Navigation panel choose Databases.
   2. Select mssql database that we created. Click on Modify button
   3. Go to Advance configurations
   4. Under Database options. Select custom option group from drop down (which we have created in earlier step.)
   5. Click Continue. In the next window, select Apply Immediately and click Modify DB Instance.


## 2. Enabling Auditing
1. Connecting to database:
   1. Start the SQL Server Management Studio and provide connection details. Enter the ‘endpoint’ (for AWS, you will get this on AWS RDS console) as the Server Name. Provide the ‘username’ and the master ‘password’ that we had set while creating the database.
   2. Create database.
2. About Audits:
   1. SQL Server audit lets you create server audits, which can contain server audit specifications for server level events, and database audit specifications for database level events.
   2. When you define an audit, you specify the location for the output of the results. This is the audit destination. The audit is created in a disabled state and does not automatically audit any actions. After the audit is enabled, the audit destination receives data from the audit.
3. Creating audit:
   1. Create an audit (In management studio: Security -> Audits -> New Audit)
   2. Provide file path ([D:\rdsdbdata\SQLAudit\] default for AWS RDS instance).
   3. In Maximum file size, deselect Unlimited check box and provide a value.
   4. Keep remaining configurations as is.
   5. Click on Ok button.
   6. Right click on audit that we have created and select enable to enable it. 
4. Create audit specifications:
   1. Create a server audit specification:
      1. In management go to Security, expand it.
      2. Right click on Server audit specification option and select New audit specification.
      3. Select Audit that we created in earlier step.
      4. Configure Audit log groups as per requirement. (Detailed audit log groups can be found on Microsoft documentation site)
      5. Click on Ok button.
      6. Right click on server audit specification that we have created and select enable to enable it.
			
   2. Create database audit specification:
      1. In management go to Database, expand it.
      2. Expand security under it.
      3. Right click on Database audit specification option and select New audit specification.
      4. Select Audit that we created in earlier step.
      5. Configure Audit log groups as per requirement. (Detailed audit log groups can be found on Microsoft documentation site)
      6. Click on Ok button.
      7. Right click on database audit specification that we have created and select enable to enable it.

5. Create audit specifications for error capture [This step is only required for on-prem configuration]:
   1. Execute the below TSQL to capture error events.
   ```
        CREATE EVENT SESSION [<event_name>] 
        ON SERVER 
        ADD EVENT sqlserver.error_reported
        (
            ACTION    
            (      
                sqlserver.client_hostname,
                sqlserver.database_id,
                sqlserver.sql_text,
                sqlserver.username,
                sqlserver.database_name,
                sqlserver.session_id,
                sqlserver.server_instance_name
            )
            WHERE
            (
                [severity] >= (11)
            )
        )
        ADD TARGET package0.asynchronous_file_target
        (
            SET filename=N'<path to the xel file>'
        )
        WITH
        (
				MAX_MEMORY=4096 KB,
				EVENT_RETENTION_MODE=ALLOW_SINGLE_EVENT_LOSS,
				MAX_DISPATCH_LATENCY=30 SECONDS,
				MAX_EVENT_SIZE=0 KB,
				MEMORY_PARTITION_MODE=NONE,
				TRACK_CAUSALITY=OFF,
				STARTUP_STATE=ON
        );
        GO

        ALTER EVENT SESSION [<event_name mentioned in create event>]ON SERVER
        STATE = START;
        GO

        ** Note that the event name should be the same for TSQL Create and Alter event.**
        ** Note that the xel path name mentioned in TSQL should be matching the one in the SQL statement mentioned in the input plug-in for 'failure' tag. **
   ``` 


#### Limitations

* MSSQL auditing only supports error logs for following two groups:
  * FAILED_DATABASE_AUTHENTICATION_GROUP: Indicates that a principal tried to log on to a contained database and failed. Events in this class are raised by new connections or by connections that are reused from a connection pool.
  * FAILED_LOGIN_GROUP: Indicates that a principal tried to log on to SQL Server and failed. Events in this class are raised by new connections or by connections that are reused from a connection pool.
* The mssql plug-in does not support IPV6.
* For guardium version 11.4 and below, incorrect value of session start time in report is known issue.
* Currently, load balancing is not supported.
* MSSQL error capturing is not supported for AWS MSSQL.


## 3. Configuring the MSSQL filters in Guardium

The Guardium universal connector is the Guardium entry point for native audit logs. The universal connector identifies and parses received events, and then converts them to a standard Guardium format. The output of the universal connector is forwarded to the Guardium sniffer on the collector, for policy and auditing enforcements. Configure Guardium to read the native audit logs by customizing the MSSQL template.

### Authorizing outgoing traffic from AWS to Guardium

#### Procedure
1. Log in to the Guardium API.
2. Issue these commands:
   * grdapi add_domain_to_universal_connector_allowed_domains domain=amazonaws.com
   * grdapi add_domain_to_universal_connector_allowed_domains domain=amazon.com

#### Before you begin

* Configure the policies you require. See [policies](/docs/#policies) for more information.
* You must have permission for the S-Tap Management role. The admin user includes this role by default.
* Download the [mssql-offline-plugins-7.5.2.zip](https://github.com/IBM/universal-connectors/raw/main/filter-plugin/logstash-filter-mssql-guardium/MssqlOverJdbcPackage/mssql-offline-plugins-7.5.2.zip) plug-in.
* Download the [logstash-filter-xml.zip](https://github.com/IBM/universal-connectors/raw/main/filter-plugin/logstash-filter-mssql-guardium/MssqlOverJdbcPackage/logstash-filter-xml.zip) plug-in.[This zip is not required for AWS MSSQL]
* Download the [mssql-jdbc-7.4.1.jre8](https://jar-download.com/artifacts/com.microsoft.sqlserver/mssql-jdbc/7.4.1.jre8) jar.

#### Procedure:

1. On the collector, go to Setup > Tools and Views > Configure Universal Connector.
2. First Enable the Universal Guardium connector, if it is Disabled already.
3. Click Upload File and select the offline [mssql-offline-plugins-7.5.2.zip](https://github.com/IBM/universal-connectors/raw/main/filter-plugin/logstash-filter-mssql-guardium/MssqlOverJdbcPackage/mssql-offline-plugins-7.5.2.zip) plug-in. After it is uploaded, click OK.
4. Click Upload File and select the offline [logstash-filter-xml.zip](https://github.com/IBM/universal-connectors/raw/main/filter-plugin/logstash-filter-mssql-guardium/MssqlOverJdbcPackage/logstash-filter-xml.zip) plug-in. After it is uploaded, click OK.
5. Click Upload File and select the [mssql-jdbc-7.4.1.jre8](https://jar-download.com/artifacts/com.microsoft.sqlserver/mssql-jdbc/7.4.1.jre8) jar. After it is uploaded, click OK.

6. Click the Plus sign to open the Connector Configuration dialog box.
7. Type a name in the Connector name field.
8. Update the input section to add the details from [awsMssqlJDBC.conf](https://github.com/IBM/universal-connectors/raw/main/filter-plugin/logstash-filter-mssql-guardium/MssqlOverJdbcPackage/awsMssqlJDBC.conf) for AWS MSSQL or [onPremMSSQL.conf](https://github.com/IBM/universal-connectors/raw/main/filter-plugin/logstash-filter-mssql-guardium/MssqlOverJdbcPackage/on-premMssqlJDBC.conf) for on prem MSSQL setup file's input part, omitting the keyword "input{" at the beginning and its corresponding "}" at the end.
   Note :
   • For Guardium Data Protection version 11.3, add the following line to the input section:
   'jdbc_driver_library => "${THIRD_PARTY_PATH}/mssql-jdbc-7.4.1.jre8.jar"'
   • Even if the universal connector was configured a while after auditing was configured, the universal connector  will still process all the previous records as well, since they were already audited by the database.
9. Update the filter section to add the details from [awsMssqlJDBC.conf](https://github.com/IBM/universal-connectors/raw/main/filter-plugin/logstash-filter-mssql-guardium/MssqlOverJdbcPackage/awsMssqlJDBC.conf) for AWS MSSQL or [onPremMSSQL.conf](https://github.com/IBM/universal-connectors/raw/main/filter-plugin/logstash-filter-mssql-guardium/MssqlOverJdbcPackage/on-premMssqlJDBC.conf) for on prem MSSQL setup file's filter part, omitting the keyword "filter{" at the beginning and its corresponding "}" at the end.
10. The "type" fields should match in the input and filter configuration sections. This field should be unique for every individual connector added.
11. If you are using two JDBC plug-ins on the same machine, the last_run_metadata_path file name should be different for each.
12. Click Save. Guardium validates the new connector, and enables the universal connector if it was
	disabled. After it is validated, it appears in the Configure Universal Connector page.

## 4. JDBC Load Balancing Configuration

1. In the MSSQL JDBC input plug-in,we distribute load between two machines based on Even and Odd "session_id" for "Success" events.
   * #### Procedure:
     On the first G Machine, in the input section for the JDBC plug-in, update the "statement" field in the first JDBC block where tags => ["Success"] like below:
     ```
     SELECT server_instance_name,event_time, session_id, database_name, client_ip, server_principal_name, application_name, statement, succeeded, DATEDIFF_BIG(ns, ‘1970-01-01 00:00:00.00000’, event_time) AS updatedeventtime FROM sys.fn_get_audit_file(‘E:\Deploy\SQLAudit*.sqlaudit’, DEFAULT, DEFAULT) Where schema_name not in (‘sys’) and object_name NOT IN (‘dbo’,‘syssubsystems’,‘fn_sysdac_is_currentuser_sa’,‘backupmediafamily’,‘backupset’,‘syspolicy_configuration’,‘syspolicy_configuration_internal’,‘syspolicy_system_health_state’,‘syspolicy_system_health_state_internal’,‘fn_syspolicy_is_automation_enabled’,‘spt_values’,‘sysdac_instances_internal’,‘sysdac_instances’) and database_principal_name not in(‘public’) and ((succeeded =1) or (succeeded =0 and statement like ‘%Login failed%’ )) and statement != ‘’ and session_id%2= 0 and DATEDIFF_BIG(ns, ‘1970-01-01 00:00:00.00000’, event_time) > :sql_last_value order by event_time
     ```
     On the second G Machine, in the input section for the JDBC plug-in,  update the "statement" field in the first JDBC block where tags => ["Success"] like below:
     ```
     SELECT server_instance_name,event_time, session_id, database_name, client_ip, server_principal_name, application_name, statement, succeeded, DATEDIFF_BIG(ns, ‘1970-01-01 00:00:00.00000’, event_time) AS updatedeventtime FROM sys.fn_get_audit_file(‘E:\Deploy\SQLAudit*.sqlaudit’, DEFAULT, DEFAULT) Where schema_name not in (‘sys’) and object_name NOT IN (‘dbo’,‘syssubsystems’,‘fn_sysdac_is_currentuser_sa’,‘backupmediafamily’,‘backupset’,‘syspolicy_configuration’,‘syspolicy_configuration_internal’,‘syspolicy_system_health_state’,‘syspolicy_system_health_state_internal’,‘fn_syspolicy_is_automation_enabled’,‘spt_values’,‘sysdac_instances_internal’,‘sysdac_instances’) and database_principal_name not in(‘public’) and ((succeeded =1) or (succeeded =0 and statement like ‘%Login failed%’ )) and statement != ‘’ and session_id%2= 1 and DATEDIFF_BIG(ns, ‘1970-01-01 00:00:00.00000’, event_time) > :sql_last_value order by event_time
     ```
2. In the MSSQL JDBC input plug-in , we distribute load between two machines based on Even and Odd "timestamp" for "Failure" events.
   * #### Procedure:
       On the first G Machine, in the input section for the JDBC plug-in, update the "statement" field in the second JDBC block where tags => ["Failure"] like below:
       ```
       SELECT timestamp_utc,event_data,DATEDIFF_BIG(ns, ‘1970-01-01 00:00:00.00000’, timestamp_utc) AS updated_timestamp FROM sys.fn_xe_file_target_read_file(‘C:\temp\ErrorCapture*.xel’,null,null,null) where DATEDIFF_BIG(ss, ‘1970-01-01 00:00:00.00000’, timestamp_utc)%2 = 1 and DATEDIFF_BIG(ns, ‘1970-01-01 00:00:00.00000’, timestamp_utc) > :sql_last_value order by timestamp_utc
       ```
       On the second G Machine, in the input section for the JDBC plug-in, update the "statement" field in the second JDBC block where tags => ["Failure"] like below:
       ```
       SELECT timestamp_utc,event_data,DATEDIFF_BIG(ns, ‘1970-01-01 00:00:00.00000’, timestamp_utc) AS updated_timestamp FROM sys.fn_xe_file_target_read_file(‘C:\temp\ErrorCapture*.xel’,null,null,null) where DATEDIFF_BIG(ss, ‘1970-01-01 00:00:00.00000’, timestamp_utc)%2 = 0 and DATEDIFF_BIG(ns, ‘1970-01-01 00:00:00.00000’, timestamp_utc) > :sql_last_value order by timestamp_utc
       ```
