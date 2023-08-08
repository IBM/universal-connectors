## Progress-Guardium Logstash filter plug-in

**Meet Progress**

- Tested versions: 12.6

- Environment: On-premise

- Supported inputs: JDBC (Pull)

- Supported Guardium versions:
   - Guardium Data Protection: 11.4 and above

**Note:** Licensed database users get access to openEdge jar directly from the Progress team.  

**Note:** To capture DML operations, you need to explicitly apply a policy on each and every table, in addition to the imported default policy.

This is a [Logstash](https://github.com/elastic/logstash) filter plug-in for the universal connector that is featured in IBM Security Guardium. It parses events and messages from the Progress audit log into a [Guardium record](https://github.com/IBM/universal-connectors/blob/main/common/src/main/java/com/ibm/guardium/universalconnector/commons/structures/Record.java) instance (which is a standard structure made out of several parts). The information is then sent over to Guardium. Guardium records include the accessor (the person who tried to access the data), the session, data, and exceptions. If there are no errors, the data contains details about the query "construct". The construct details the main action (verb) and collections (objects) involved.

The plug-in is free and open-source (Apache 2.0). It can be used as a starting point to develop additional filter plug-ins for Guardium universal connector.


## 1. Installing Progress and configuring auditing

For this example, we will assume that we already have a working Progress Database setup.

## 2. Enabling Auditing :
 
   1. Create a structure file that defines the database structure. It contains all of the information required by the PROSTRCT CREATE utility to create a database control area and the database extents.
   [This](https://docs.progress.com/bundle/openedge-database-management-117/page/Creating-a-structure-description-file.html) link can be used to create struture file for database. 
   
   Below are the details of a sample structure file.

    d "AuditData":20,64;512 . f 2000000

    d "AuditData":20,64;512 .

    d "AuditIndex":21,1;64 . f 1000000

    d "AuditIndex":21,1;64

Where:

-  ‘d’ indicates a type which is ‘Schema and application data areas.

-  'AuditData' indicates a name of storage area.

- ‘20,64’ co-ordinates indicate number of storage area.

-  ‘512’ indicates recsPerBlock, which tells us the number of database records in each database block.

- ‘.’ points to current working directory. We can mention an absolute or relative pathname of each extent.

- ‘f’ indicates ‘Fixed’ extent type. If the extent type token is not specified, the extent is variable.

- ‘2000000’ is the size of an extent in kilobytes. This value must be a multiple of 16 times your database block size.


 2. Once the database is installed, Proenv command line application will be added. Open the application and execute the below commands.

       a. `proenv> prostrct add <Database_name> <"location of structure of file created in step1">`.

       Example:    `prostrct add guardium-db "C:\Users\Administrator\Downloads\ProgressDocs\audit.st.txt"`.

       b.         `proenv> proutil <Database_name> -C enableauditing area  <"Areaname which we have mentioned in st file"> indexarea <"Indexname which we have mentioned in st file">`.

       Example:   `proutil guardium-db -C enableauditing area "AuditData" indexarea "AuditIndex"`.


## 3. Adding an audit policy for a specific database
 
   1. Connect to the database using multi-user mode and login with the audit admin user to enable audit policy.

   2. To allow multiuser mode for the database, deploy it on a specific server with the below commands using Proenv application.

      `proenv> proserve -db <Database_name> -H <Host_name> -S <Port_Number>`

      Example: `proserve -db guardium-db -H DataBase1 -S 5555`

   3. Connect to the database using multi-user mode and login with the audit admin user to enable audit policy.

   4. Under the **Admin** tab, click **Security** and select **Disallow blank user id access**.(Optional)


### Importing the audit policy using the APM tool

Connect to the new audit-enabled database using the Data Administrator tool.

  1. Under **Tools**, select **Audit Policy Maintaince** and click **Import Policy**.

  2. Import the Policy File. A predefined audit policy [policies.xml](https://github.ibm.com/Activity-Insights/universal-connectors/raw/master/filter-plugin/logstash-filter-progressdb-guardium/ProgressOverJdbcPackage/policies.xml.zip),which is provided by Progress, can be imported to any database which is enabled for auditing.

  3. Extract the zip file. After extracting the downloaded zip, there will be a file named "policies.xml". Select the policies.xml.

  4. Click **OK** to import the Policies from the .xml file.

  5. All audit events mentioned in file will be imported on UI in Audit Policy Maintaince.

  6. Policies.xml capture all audit events. If you don't want to capture any type of event, then those can be deleted via the UI. 

  7. To capture DML operations, you need to explicitly apply a policy for each and every table, in addition to the imported default policy.

  8. To delete a particular record, click the - icon.

  9. Once all changes are done, activate the policy.

  10. Commit the changes.

For more information, refer to, [How To Import Audit Policies using APMT](https://community.progress.com/s/article/P137619).


## 4. Viewing audit logs

### View the Progress audit tables from the OpenEdge tool using 4GL language.

#### Procedure

   1. Open the **Database Administrator** component.

   2. Connect to the database using **Audit Administrator** credentials.

   3. Click **Procedure Editor** from **Tools**.

   4. Run the below queries to check the audit table:

	      FOR EACH _aud-audit-data: Display _aud-audit-data.

	      FOR EACH _aud-event: Display _aud-event.

	      FOR EACH _client-session: Display _client-session.

	      FOR EACH _aud-audit-data-value: Display _aud-audit-data-value.

 
### View the Progress audit tables from any third party tool using SQL.

#### Procedure
1. To view the audited Logs:-

    a) Connect to the database with a user who has 'Audit Administrator' and 'DBA' rights.

    2. The below shows information about audit logs:

			  select * from PUB."_aud-audit-data";

			  select * from PUB."_aud-audit-data-value";

			  select * from PUB."_aud-event";

			  select * from PUB."_client-session";

			  select * from PUB."_db-detail";

			  select * from PUB."_db";
	  

## 5. Archiving audit tables:

1. Extracting Audit data to output file.

		OUTPUT TO "myFile.txt".
		    FOR EACH _aud-audit-data:
		    Display _aud-audit-data.
		    OUTPUT CLOSE.
     
  2. Deleting audit data.
       
	       FOR EACH _aud-audit-data EXCLUSIVE-LOCK :
		    DELETE _aud-audit-data.
		    END.

## Limitations:

1. Progress auditing only supports error logs for Login Failed.
2. Client IP and Server IP is set to 0.0.0.0.
3. SQL query will not be visible.
4. Supported only for Windows OS.
5. Object name has special characters due to a Progress audit table limitation.
6. The source program will not be available in reports.
7. The DQL command[Select] is not captured in audit logs.



## 6. Configuring the Progress filters in Guardium.

The Guardium universal connector is the Guardium entry point for native audit logs. The universal connector identifies
and parses received events, and then converts them to a standard Guardium format. The output of the universal connector
is forwarded to the Guardium sniffer on the collector, for policy and auditing enforcements. Configure Guardium to read
the native audit logs by customizing the Progress template.

### Before you begin

• Configure the policies you require. See [policies](/docs/#policies) for more information.

• You must have permission for the S-Tap Management role. The admin user includes this role by default.

• Download the [logstash-filter-progress_guardium_plugin_filter.zip](https://github.ibm.com/Activity-Insights/universal-connectors/raw/master/filter-plugin/logstash-filter-progressdb-guardium/ProgressOverJdbcPackage/logstash-filter-progress_guardium_plugin_filter.zip) file. This is not necessary for Guardium Data Protection v12.0 and later.

• Download the `openedge.jar` file based on your platform and database version.


### Procedure

1. On the collector, go to **Setup** > **Tools and Views** > **Configure Universal Connector**.

2. First, enable the universal connector, if it is currently disabled.

3. Click **Upload File** and upload the `openedge.jar` file that is included in the enterprise version. 

4. Click **Upload File** and select the offline [logstash-filter-progress_guardium_plugin_filter.zip](https://github.ibm.com/Activity-Insights/universal-connectors/raw/master/filter-plugin/logstash-filter-progressdb-guardium/ProgressOverJdbcPackage/logstash-filter-progress_guardium_plugin_filter.zip) file. After it is uploaded, click **OK**. This is not necessary for Guardium Data Protection v12.0 and later.

5. Click the Plus icon to open the Connector Configuration dialog box.
    
6. Type a name in the Connector name field.
    
7. Update the input section to add the details from the [Progress-JDBC.conf](https://github.ibm.com/Activity-Insights/universal-connectors/raw/master/filter-plugin/logstash-filter-progressdb-guardium/ProgressOverJdbcPackage/Progress-JDBC.conf) file's input part, omitting the keyword "input{" at the beginning and its corresponding "}" at the end. Provide the required details for database server name, username, and password for JDBC connectivity.

8. Update the filter section to add the details from the [Progress-JDBC.conf](https://github.ibm.com/Activity-Insights/universal-connectors/raw/master/filter-plugin/logstash-filter-progressdb-guardium/ProgressOverJdbcPackage/Progress-JDBC.conf) file's filter part, omitting the keyword "filter{" at the beginning and its corresponding "}" at the end. Provide the same database server name as in the above step for the Server_Hostname attribute in the filter section.

9. The "type" fields should match in the input and the filter configuration sections. This field should be unique for every individual connector added.
    
10. If using two JDBC plug-ins on same machine, the `last_run_metadata_path` file name should be different.

**Note**: For moderate to large amounts of data, include pagination to facilitate the audit and to avoid out of memory errors. Use the parameters below in the input section when using a JDBC connector, and remove the concluding semicolon ';' from the jdbc statement: `jdbc_paging_enabled => true jdbc_page_size => 1000`.

11. Click **Save**. Guardium validates the new connector, and enables the universal connector if it was disabled. After it is validated, it appears in the Configure Universal Connector page.


## 7. JDBC load balancing configuration

In Progress JDBC input plug-ins, we distribute load between two machines based on an even and odd "transaction ID".

### Procedure

1. On the first G Machine, in the input section for the JDBC Plug-in, update the **statement** field as follows:

		SELECT
                         PUB."_aud-audit-data"."_User-id",
                         PUB."_aud-audit-data"."_Audit-date-time",
                         TIMESTAMPDIFF(SQL_TSI_FRAC_SECOND, TO_TIMESTAMP(TO_CHAR(:epoch_start_from)), TO_TIMESTAMP(TO_CHAR(PUB."_aud-audit-data"."_Audit-date-time"))) as audit_timestamp,
                         PUB."_aud-audit-data"."_Client-session-uuid",
                         PUB."_aud-audit-data"."_Event-context",
                         PUB."_aud-audit-data"."_Transaction-id",
                         PUB."_db-detail"."_db-description",
                         PUB."_aud-event"."_Event-name"
                 FROM  PUB."_db-detail", PUB."_aud-audit-data" inner join PUB."_aud-event"
                 on PUB."_aud-event"."_Event-id" = PUB."_aud-audit-data"."_Event-id"
                 where PUB."_aud-audit-data"."_Db-guid" = PUB."_db-detail"."_Db-guid"
                 and mod(PUB."_aud-audit-data"."_Transaction-id",2) = 0
                 and TIMESTAMPDIFF(SQL_TSI_FRAC_SECOND, TO_TIMESTAMP(TO_CHAR(:epoch_start_from)), TO_TIMESTAMP(TO_CHAR(PUB."_aud-audit-data"."_Audit-date-time"))) > :sql_last_value
                 order by PUB."_aud-audit-data"."_Audit-date-time" asc

2. On the second G machine, in the input section for the JDBC plug-in, update the **statement** field as follows:

				SELECT
                         PUB."_aud-audit-data"."_User-id",
                         PUB."_aud-audit-data"."_Audit-date-time",
                         TIMESTAMPDIFF(SQL_TSI_FRAC_SECOND, TO_TIMESTAMP(TO_CHAR(:epoch_start_from)), TO_TIMESTAMP(TO_CHAR(PUB."_aud-audit-data"."_Audit-date-time"))) as audit_timestamp,
                         PUB."_aud-audit-data"."_Client-session-uuid",
                         PUB."_aud-audit-data"."_Event-context",
                         PUB."_aud-audit-data"."_Transaction-id",
                         PUB."_db-detail"."_db-description",
                         PUB."_aud-event"."_Event-name"
                 FROM  PUB."_db-detail", PUB."_aud-audit-data" inner join PUB."_aud-event"
                 on PUB."_aud-event"."_Event-id" = PUB."_aud-audit-data"."_Event-id"
                 where PUB."_aud-audit-data"."_Db-guid" = PUB."_db-detail"."_Db-guid"
                 and mod(PUB."_aud-audit-data"."_Transaction-id",2) = 1
                 and TIMESTAMPDIFF(SQL_TSI_FRAC_SECOND, TO_TIMESTAMP(TO_CHAR(:epoch_start_from)), TO_TIMESTAMP(TO_CHAR(PUB."_aud-audit-data"."_Audit-date-time"))) > :sql_last_value
                 order by PUB."_aud-audit-data"."_Audit-date-time" asc
