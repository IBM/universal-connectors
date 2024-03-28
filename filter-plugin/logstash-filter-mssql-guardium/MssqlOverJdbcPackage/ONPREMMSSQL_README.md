## 1. Configuring ONPREM MSSQL

### Procedure

	1. Create a database instance:

		a.Here, We will consider that we have already installed MSSQL ONPREM setup. 

## 2. Enabling auditing

	1. Connecting to database:
		
		a. Start the SQL Server Management Studio and provide connection details. 
			Enter the ‘HostName’as the Server Name. 
			Provide the ‘username’ and the master ‘password’ that we had set while creating the database.
		b. Create database.

	2. About Audits:
		
		a. SQL Server audit lets you create server audits, which can contain server audit specifications for server level events, and database audit specifications for database level events.
		b. When you define an audit, you specify the location for the output of the results. This is the audit destination. The audit is created in a disabled state and does not automatically audit any actions. After the audit is enabled, the audit destination receives data from the audit.

	3. Creating audit:
	
		a. Create an audit (In management studio: Security -> Audits -> New Audit)
		b. Provide file path as per choice([D:\SQLAudit\]).
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

	5. Create audit specifications for error capture:

			I. Excute below TSQL to capture error events. 

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

	Kindly note event name should be same for TSQL Create and Alter event.
	And xel path name mentioned in TSQL should be matching to SQL statement mentioned in input plugin for 'failure' tag. 


## 3. Configuring the MSSQL filters in Guardium

The Guardium universal connector is the Guardium entry point for native audit logs. The universal connector identifies and parses received events, and then converts them to a standard Guardium format. The output of the universal connector is forwarded to the Guardium sniffer on the collector, for policy and auditing enforcements. Configure Guardium to read the native audit logs by customizing the MSSQL template.

### Authorizing outgoing traffic from AWS to Guardium

#### Procedure

#### Before you begin

• You must have LFD policy enabled on the collector. The detailed steps can be found in step 4 on [this page](https://www.ibm.com/docs/en/guardium/11.4?topic=dpi-installing-testing-filter-input-plug-in-staging-guardium-system).

• You must have permission for the S-Tap Management role. The admin user includes this role by default.

• Download the [mssql-offline-plugins-7.5.2.zip](https://github.ibm.com/Activity-Insights/universal-connectors/blob/master/filter-plugin/logstash-filter-mssql-guardium/MssqlOverJdbcPackage/mssql-offline-plugins-7.5.2.zip) plug-in.This is not necessary for Guardium Data Protection v12.0 and later.

•  Choose the appropriate plugin download based on your Guardium version. <br>
	I. If you are using Guardium 11.4 with patch p485 or earlier,
		Download [logstash-filter-xml-4.1.3-1.zip](https://github.ibm.com/Activity-Insights/universal-connectors/blob/master/filter-plugin/logstash-filter-mssql-guardium/MssqlOverJdbcPackage/logstash-filter-xml-4.1.3-1.zip).<br>
	II. If you are using Guardium 11.5 with patch p535 or earlier,
		Download [logstash-filter-xml-4.1.3-1.zip](https://github.ibm.com/Activity-Insights/universal-connectors/blob/master/filter-plugin/logstash-filter-mssql-guardium/MssqlOverJdbcPackage/logstash-filter-xml-4.1.3-1.zip).<br>
	III. If you are using Guardium 12.0 with patch p5 or earlier,
		Download [logstash-filter-xml-4.2.0-1.zip](https://github.ibm.com/Activity-Insights/universal-connectors/blob/master/filter-plugin/logstash-filter-mssql-guardium/MssqlOverJdbcPackage/logstash-filter-xml-4.2.0-1.zip).<br>
	IV. For the Guardium 11.4(p490 or later),11.5(p540 or later) and 12.0(p10 or later),
		Download [logstash-filter-xml-4.2.0-2.zip](https://github.ibm.com/Activity-Insights/universal-connectors/blob/master/filter-plugin/logstash-filter-mssql-guardium/MssqlOverJdbcPackage/logstash-filter-xml-4.2.0-2.zip).<br>
	V. For the Guardium 12.0(p15 or later), logstash-filter-xml.zip upload is not required.

• Download the [mssql-jdbc-7.4.1.jre8](https://github.ibm.com/Activity-Insights/universal-connectors/blob/master/filter-plugin/logstash-filter-mssql-guardium/MssqlOverJdbcPackage/mssql-jdbc-7.4.1.jre8.jar) jar.

#### Procedure: 

1. On the collector, go to Setup > Tools and Views > Configure Universal Connector.
2. First Enable the Universal Guardium connector, if it is Disabled already.
3. Click Upload File and select the offline [mssql-offline-plugins-7.5.2.zip](https://github.ibm.com/Activity-Insights/universal-connectors/blob/master/filter-plugin/logstash-filter-mssql-guardium/MssqlOverJdbcPackage/mssql-offline-plugins-7.5.2.zip) plug-in. After it is uploaded, click OK.This is not necessary for Guardium Data Protection v12.0 and later.
   4. Upload the relevant plugin based on the version of the Guardium.<br>
      I. If you are using Guardium 11.4 with patch p485 or earlier,
      Upload [logstash-filter-xml-4.1.3-1.zip](https://github.ibm.com/Activity-Insights/universal-connectors/blob/master/filter-plugin/logstash-filter-mssql-guardium/MssqlOverJdbcPackage/logstash-filter-xml-4.1.3-1.zip).<br>
      II. If you are using Guardium 11.5 with patch p535 or earlier,
      Upload [logstash-filter-xml-4.1.3-1.zip](https://github.ibm.com/Activity-Insights/universal-connectors/blob/master/filter-plugin/logstash-filter-mssql-guardium/MssqlOverJdbcPackage/logstash-filter-xml-4.1.3-1.zip).<br>
      III. If you are using Guardium 12.0 with patch p5 or earlier,
      Upload [logstash-filter-xml-4.2.0-1.zip](https://github.ibm.com/Activity-Insights/universal-connectors/blob/master/filter-plugin/logstash-filter-mssql-guardium/MssqlOverJdbcPackage/logstash-filter-xml-4.2.0-1.zip).<br>
      IV. For the Guardium 11.4(p490 or later),11.5(p540 or later) and 12.0(p10 or later),
      Upload [logstash-filter-xml-4.2.0-2.zip](https://github.ibm.com/Activity-Insights/universal-connectors/blob/master/filter-plugin/logstash-filter-mssql-guardium/MssqlOverJdbcPackage/logstash-filter-xml-4.2.0-2.zip).<br>
   	  V. For the Guardium 12.0(p15 or later), logstash-filter-xml.zip upload is not required.
5. Click Upload File and select the [mssql-jdbc-7.4.1.jre8](https://github.ibm.com/Activity-Insights/universal-connectors/blob/master/filter-plugin/logstash-filter-mssql-guardium/MssqlOverJdbcPackage/mssql-jdbc-7.4.1.jre8.jar) jar. After it is uploaded, click OK.
6. Click the Plus sign to open the Connector Configuration dialog box.
7. Type a name in the Connector name field.
8. Update the input section to add the details from [onPremMSSQL.conf](https://github.ibm.com/Activity-Insights/universal-connectors/blob/master/filter-plugin/logstash-filter-mssql-guardium/MssqlOverJdbcPackage/onpremMSSQLPlugin.conf) for on prem MSSQL setup file's input part, omitting the keyword "input{" at the beginning and its corresponding "}" at the end.
	Note : 
		• For Guardium Data Protection version 11.3, add the following line to the input section:
			'jdbc_driver_library => "${THIRD_PARTY_PATH}/mssql-jdbc-7.4.1.jre8.jar"'
		• If auditing is configured way long back and UC is configured at later point of time, still UC will process all the previous older records as well till date, since it is already audited by the DB.
9.  Update the filter section to add the details from [onPremMSSQL.conf](https://github.ibm.com/Activity-Insights/universal-connectors/blob/master/filter-plugin/logstash-filter-mssql-guardium/MssqlOverJdbcPackage/onpremMSSQLPlugin.conf) for on prem MSSQL setup file's filter part, omitting the keyword "filter{" at the beginning and its corresponding "}" at the end.
10. "type" field should match in input and filter configuration section. This field should be unique for  every individual connector added.
11. If using two jdbc plug-ins on the same machine, the last_run_metadata_path file name should be different.
12. Click Save. Guardium validates the new connector, and enables the universal connector if it was
	disabled. After it is validated, it appears in the Configure Universal Connector page.

## 4. JDBC Load Balancing Configuration
	
a. In MSSQL JDBC input plug-in,we distribute load between two machines based on Even and Odd "session_id" for "Success" events.

#### Procedure: 

On First G Machine, in input section for JDBC Plugin update "statement" field in the first jdbc block where tags => ["Success"] like below:

	SELECT server_instance_name,event_time, session_id, database_name, client_ip, server_principal_name, application_name, statement, succeeded, DATEDIFF_BIG(ns, ‘1970-01-01 00:00:00.00000’, event_time) AS updatedeventtime FROM sys.fn_get_audit_file(‘E:\Deploy\SQLAudit*.sqlaudit’, DEFAULT, DEFAULT) Where schema_name not in (‘sys’) and object_name NOT IN (‘dbo’,‘syssubsystems’,‘fn_sysdac_is_currentuser_sa’,‘backupmediafamily’,‘backupset’,‘syspolicy_configuration’,‘syspolicy_configuration_internal’,‘syspolicy_system_health_state’,‘syspolicy_system_health_state_internal’,‘fn_syspolicy_is_automation_enabled’,‘spt_values’,‘sysdac_instances_internal’,‘sysdac_instances’) and database_principal_name not in(‘public’) and ((succeeded =1) or (succeeded =0 and statement like ‘%Login failed%’ )) and statement != ‘’ and session_id%2= 0 and DATEDIFF_BIG(ns, ‘1970-01-01 00:00:00.00000’, event_time) > :sql_last_value order by event_time

On Second G Machine, in input section for JDBC Plugin update "statement" field in the first jdbc block where tags => ["Success"] like below:

	SELECT server_instance_name,event_time, session_id, database_name, client_ip, server_principal_name, application_name, statement, succeeded, DATEDIFF_BIG(ns, ‘1970-01-01 00:00:00.00000’, event_time) AS updatedeventtime FROM sys.fn_get_audit_file(‘E:\Deploy\SQLAudit*.sqlaudit’, DEFAULT, DEFAULT) Where schema_name not in (‘sys’) and object_name NOT IN (‘dbo’,‘syssubsystems’,‘fn_sysdac_is_currentuser_sa’,‘backupmediafamily’,‘backupset’,‘syspolicy_configuration’,‘syspolicy_configuration_internal’,‘syspolicy_system_health_state’,‘syspolicy_system_health_state_internal’,‘fn_syspolicy_is_automation_enabled’,‘spt_values’,‘sysdac_instances_internal’,‘sysdac_instances’) and database_principal_name not in(‘public’) and ((succeeded =1) or (succeeded =0 and statement like ‘%Login failed%’ )) and statement != ‘’ and session_id%2= 1 and DATEDIFF_BIG(ns, ‘1970-01-01 00:00:00.00000’, event_time) > :sql_last_value order by event_time

b. In MSSQL JDBC input plug-in , we distribute load between two machines based on Even and Odd "timestamp" for "Failure" events.

#### Procedure: 

On the first G machine, in the input section for the JDBC plug-in, update the "statement" field in the second JDBC block where tags => ["Failure"], as follows:

	SELECT timestamp_utc,event_data,DATEDIFF_BIG(ns, ‘1970-01-01 00:00:00.00000’, timestamp_utc) AS updated_timestamp FROM sys.fn_xe_file_target_read_file(‘C:\temp\ErrorCapture*.xel’,null,null,null) where DATEDIFF_BIG(ss, ‘1970-01-01 00:00:00.00000’, timestamp_utc)%2 = 1 and DATEDIFF_BIG(ns, ‘1970-01-01 00:00:00.00000’, timestamp_utc) > :sql_last_value order by timestamp_utc

On the second G machine, in the input section for the JDBC plug-in, update the "statement" field in the second JDBC block where tags => ["Failure"], as follows:

	SELECT timestamp_utc,event_data,DATEDIFF_BIG(ns, ‘1970-01-01 00:00:00.00000’, timestamp_utc) AS updated_timestamp FROM sys.fn_xe_file_target_read_file(‘C:\temp\ErrorCapture*.xel’,null,null,null) where DATEDIFF_BIG(ss, ‘1970-01-01 00:00:00.00000’, timestamp_utc)%2 = 0 and DATEDIFF_BIG(ns, ‘1970-01-01 00:00:00.00000’, timestamp_utc) > :sql_last_value order by timestamp_utc