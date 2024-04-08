## 1. Configuring GCP MSSQL 

### Procedure

	1. Create a database instance
	
		a. Go to https://console.cloud.google.com/sql/instances
		b. Click **CREATE INSTANCE**.
		c. Choose your database engine : SQL Server.
		d. Provide InstanceID and password. (This password will be used as an input in the JDBC connection details for the universal connector).
		e. Select Database version : SQL Server 2019 Enterprise.
		f. Choose a configuration to start with from Deleveopment/Production option. Choose region and zonal availability.  
		g. Customize your instance by selecting **Machine Configuration** and **Storage Information** based on your requirements.	
		h. From the **Connection** menu, choose how you want your source to connect to this instance. Then, define which networks are authorized to connect.
            (Here you need to add details of the Guardium machine where the SQL Server Management tool will be installed.)  
		j. Click **Flags and parameters** and Check the checkbox next to **Enable SQL Server audit**. Click **Save** to apply your changes. Choose the bucket in which audit logs will be stored.
        k. Click **CREATE INSTANCE**.


## 2. Enabling auditing

	1. Connecting to the database:
		
		a. In the Google Cloud console, go to the Cloud SQL Instances page.
        b. To open the Overview page of an instance, click the instance name.
        c. Click **Edit**.
        d. In the Customize your instance section, click **Flags and parameters**.
        e. Check the checkbox next to **Enable SQL Server audit**.
        f. Click **Save** to apply your changes.

	2. About Audits:
		
		a. SQL Server audit lets you create server audits, which can contain server audit specifications for server-level events, and database audit specifications for database-level events.
		b. When you define an audit, you specify a default location for the output of the results. This is the audit destination. The audit is created in a disabled state and does not automatically audit any actions. After the audit is enabled, the audit destination receives data from the audit.

	3. Creating an audit:
	
		a. Create an audit (In management studio: **Security** -> **Audits** -> **New Audit**)
		b. For the GCP instance, set the default file path as `[/var/opt/mssql/audit/]`. (It will not accept any other path).
		c. In **Maximum file size**, deselect **Unlimited** and provide a value.
		d. Keep the other configurations as is.
		e. Click **Ok**.
		f. Right-click on the audit that you created and select **enable** to enable it.
		
	4. Create audit specifications:
	
		a. Create a server audit specification:
		
			I. In **Management**, go to **Security** and expand it.
			II. Right-click on **Server audit specification** and select **New audit specification**.
			III. Select the audit that you created in an earlier step.
			IV. Configure the audit log groups as per your requirements. (Detailed audit log groups can be found on the Microsoft documentation site).
			V. Click **Ok**.
			VI. Right-click on the server audit specification that you created and select **enable** to enable it.
			
		b. Create database audit specifications:
		
			I. In **Management**, go to **Database** and expand it.
			II. Expand **security**.
			III. Right-click **Database audit specification** and select **New audit specification**.
			IV. Select the audit that you created in an earlier step.
			V. Configure audit log groups as per your requirements. (Detailed audit log groups can be found on the Microsoft documentation site)
			VI. Click **Ok**.
			VII. Right-click on the database audit specification that you created and select **enable** to enable it.


## 3. Configuring the MSSQL filters in Guardium

The Guardium universal connector is the Guardium entry point for native audit logs. The universal connector identifies and parses received events, and then converts them to a standard Guardium format. The output of the universal connector is forwarded to the Guardium sniffer on the collector for policy and auditing enforcements. Configure Guardium to read the native audit logs by customizing the MSSQL template.

### Obtaining the public IP for the Guardium machine

#### Procedure
	1. Log in to the Guardium CLI.
	2. Issue these commands:
		• 'host myip.opendns.com resolver1.opendns.com'
		• `copy received ip in mentioned step 1 point H.` 

#### Before you begin

• Configure the policies you require. See [policies](/docs/#policies) for more information.

• You must have permission for the S-Tap Management role. The admin user includes this role by default.

• Download the [mssql-offline-plugins-7.5.2.zip](./mssql-offline-plugins-7.5.2.zip) plug-in. This is not necessary for Guardium Data Protection v12.0 and later.

• Download the [mssql-jdbc-7.4.1.jre8](./mssql-jdbc-7.4.1.jre8.jar) jar.

#### Procedure:

1. On the collector, go to **Setup** > **Tools and Views** > **Configure Universal Connector**.
2. Enable the universal connector if it is currently disabled.
3. Click **Upload File** and select the offline [mssql-offline-plugins-7.5.2.zip](./mssql-offline-plugins-7.5.2.zip) plug-in. After it uploads, click **OK**.This is not necessary for Guardium Data Protection v12.0 and later.
4. Click **Upload File** and select the [mssql-jdbc-7.4.1.jre8](https://github.ibm.com/prasona/universal-connectors/blob/master/filter-plugin/logstash-filter-mssql-guardium/MssqlOverJdbcPackage/mssql-jdbc-7.4.1.jre8.jar) jar. After it uploads, click **OK**.

5. Click the Plus sign to open the Connector Configuration dialog box.
6. Type a name in the **Connector name** field.
7. Update the input section to add the details from [gcpMSSQL.conf](./gcpMSSQL.conf) for GCP MSSQL.
   ***Note :***
   • For Guardium Data Protection version 11.3, add the following line to the input section:
    'jdbc_driver_library => "${THIRD_PARTY_PATH}/mssql-jdbc-7.4.1.jre8.jar"'
   • Even if auditing was configured long before the universal connector, the universal connector  will still process all the older records, since they were already audited by the database. 
9. Update the filter section to add the details from [gcpMSSQL.conf](./gcpMSSQL.conf) for GCP MSSQL. 
10. The**type** fields should match in the input and filter configuration sections. This field should be unique for  every individual connector added.
11. If you are using two JDBC plug-ins on the same machine, the `last_run_metadata_path` file name should be different.
12. Click **Save**. Guardium validates the new connector and enables the universal connector if it was
    disabled. After it is validated, it appears in the Configure Universal Connector page.

## 4. JDBC load-balancing configuration


In the MSSQL JDBC input plug-in,we distribute load between two machines based on even and odd `"session_id"`s for  events.

#### Procedure:

On the first G machine, in the input section for the JDBC plug-in, update the **statement** field in the JDBC block:

	select connection_id,event_time,database_name,server_instance_name,client_ip, application_name, host_name, server_principal_name,object_name,statement,succeeded,session_id,DATEDIFF_BIG(ns, '1970-01-01 00:00:00.00000', event_time) AS updatedeventtime FROM msdb.dbo.gcloudsql_fn_get_audit_file('/var/opt/mssql/audit/*', NULL, NULL) where  ((succeeded =1) or (succeeded =0 and statement like '%Login failed%' )and application_name not like 'SQLServerCEIP') and statement != '' and session_id%2= 0 and schema_name not in ('sys') and database_principal_name not in('public') and object_name NOT IN ('dbo','syssubsystems','change_tables','lsn_time_mapping') and DATEDIFF_BIG(ns, '1970-01-01 00:00:00.00000', event_time) > :sql_last_value order by event_time;

On the second G machine, in the input section for the JDBC plug-in,  update the **statement** field in the JDBC block:

	select connection_id,event_time,database_name,server_instance_name,client_ip, application_name, host_name, server_principal_name,object_name,statement,succeeded,session_id,DATEDIFF_BIG(ns, '1970-01-01 00:00:00.00000', event_time) AS updatedeventtime FROM msdb.dbo.gcloudsql_fn_get_audit_file('/var/opt/mssql/audit/*', NULL, NULL) where  ((succeeded =1) or (succeeded =0 and statement like '%Login failed%' )and application_name not like 'SQLServerCEIP') and statement != '' and session_id%2= 1 and schema_name not in ('sys') and database_principal_name not in('public') and object_name NOT IN ('dbo','syssubsystems','change_tables','lsn_time_mapping') and DATEDIFF_BIG(ns, '1970-01-01 00:00:00.00000', event_time) > :sql_last_value order by event_time;


