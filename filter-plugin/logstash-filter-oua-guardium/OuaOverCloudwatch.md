## 1. Configuring AWS RDS Oracle

### Procedure

      1. Browse to the Amazon AWS console at https://console.aws.amazon.com/.
         a. Click on Services in the top left menu.
         b. In the Database section, click on RDS.
         c. Select the required region in the top right corner.
         d. On the Amazon RDS Dashboard in the central panel, click on Create database.
         e. Select the database creation method.
         f. In Engine Options, select Oracle and the Oracle version.
         g. Select the appropriate template (such as Production or Dev/Test).
         h. In the settings section, enter a name for this database instance and create the master account with username and password that you use to log in to the database.
         i. Select the database instance size as required.
         j. Select the storage options (enabling Storage auto scaling, and so on).
         k. Select Availability and durability options.
         l. Depending on the connectivity requirements, select the settings for connectivity.
         m. Select the type of Authentication to be enabled for database. Available option are Password Authentication, Password and IAM database authentication, and Password and Kerberos authentication.
         n. Expand Additional Configuration options, as follows:
            i. Configure the database options.
            ii. Select options for backup.
            iii. Select whether to enable encryption on the database instance.
            iv. Select the options for deletion protection.
         o. Click Create Database.
         p. To view the database, click on Databases under Amazon RDS in the left panel.
         q. To access the database instance from the remote client, take the following steps:		              
            i. After the database is created, you can see the VPC security group associated with it on the summary page in the Connectivity and Security tab, under the Security section.
            ii. Edit this security group to allow traffic on port 1521.
            iii. Click on the group name to enable editing auditing.
            iv. In the panel that appears underneath, go to Inbound Rules section, click on Edit Inbound Rules.
            v. Select type as Oracle-RDS, protocol as TCP, Port Range as 1521.  Per the required source, you can either set the range to a specific IP address or opened to all hosts.

2. In the Additional Configuration section, under Log exports, select the newly created group, and then select the log type '**Audit**'from Amazon CloudWatch log options. 
3. Click on Add Rule and Save changes.
   **Note:** You might need to restart the database.

## 2. Enabling auditing

    1. Enable auditing by setting up a few parameters on the Parameter Group and associating the same parameters on the Database instance.
       a. Select Parameter Groups from the left pane on Amazon RDS
       b. Select the newly created Parameter Group.
       c. Click on Edit parameters button on right corner.
       d. Add the following setting:

   ```
   audit_trail = XML, EXTENDED
   ```


    2. Associating the DB Parameter Group to database Instance
       a. Click on RDS and then on Databases from the left panel
       b. Click on the Oracle database instance to be updated
       c. Click on Modify button
       d. In the Additional Configuration section, under database options, in the DB Parameter Group drop-down, select the newly created group
       e. Click on continue
       f. Select the database instance that, in its configuration section, the status shown for the DB Parameter Group is pending-reboot
       g. Reboot the Database instance for the changes to take affect


    3. Applying different policies.
    Depending on the requirement, rather than enabling auditing for all the tables and operations, you can choose to audit only selected tables or operations.
You can perform all  operations from the master user which you created when you created the database.

a. To audit every action for all users:

   ```
   create audit policy MyPolicy1
   actions all;
   AUDIT policy MyPolicy1
   ```

b. To audit every action for admin users:

   ```
   create audit policy MyPolicy2
   actions all
   when q'~ sys_context('userenv', 'session_user') = 'ADMIN' ~'
   evaluate per SESSION;
   Audit policy MyPolicy2;
   ```

c. Audit policies which have mentioned explicitly to capture[user can add and delete according to requirements]:

   ```
   CREATE AUDIT POLICY MyPolicy3 ACTIONS UPDATE, INSERT, SELECT, DELETE;
   AUDIT POLICY MyPolicy3;
   ```

d. To enable login failed events in DB:

   ```
   CREATE AUDIT POLICY ORA_LOGIN_LOGOUT ACTIONS LOGON;
   AUDIT POLICY ORA_LOGIN_LOGOUT WHENEVER NOT SUCCESSFUL;
   ```

e. To remove an existing policy:

   ```
   NOAUDIT POLICY MyPolicy1;
   DROP AUDIT POLICY MyPolicy1;
   ```

f. To check audit logs below view name can be used it shows all audit operation that is performed on database.

   ```
   SELECT * FROM UNIFIED_AUDIT_TRAIL WHERE DBUSERNAME ='ADMIN'AND OBJECT_SCHEMA ='ADMIN' order by event_timestamp desc;
   ```

g. To check login failed events captured in our database:

   ```
   SELECT * FROM UNIFIED_AUDIT_TRAIL
   WHERE ACTION_NAME = 'LOGON'
   AND RETURN_CODE != 0
   ORDER BY EVENT_TIMESTAMP DESC;
   ```

h. To delete the content:

   ```
   BEGIN
   dbms_audit_mgmt.clean_audit_trail(dbms_audit_mgmt.audit_trail_unified,false);
   END;
   ```

## Limitations
    There will be delay in data being observed for reports due to limitaion of ORACLE RDS DB instance.

## 3. Configuring the Oracle Unified Auditing filters in Guardium

The Guardium universal connector is the Guardium entry point for native audit logs. The universal connector identifies and parses received events, and then converts them to a standard Guardium format. The output of the universal connector is forwarded to the Guardium sniffer on the collector for policy and auditing enforcements. Configure Guardium to read the native audit logs by customizing the MSSQL template.


#### Procedure

#### Before you begin

• Configure the policies you require. See [policies](/docs/#policies) for more information.

• You must have permission for the S-Tap Management role. The admin user includes this role by default.

• Download the [logstash-json-encode.zip](./logstash-json-encode.zip) plug-in. This is not necessary for Guardium Data Protection v12.0 and later.

•  Choose the appropriate plugin download based on your Guardium version.<br>
I. If you are using Guardium 11.4 with patch p485 or earlier,
Download [logstash-filter-xml-4.1.3-1.zip](./logstash-filter-xml-4.1.3-1.zip).<br>
II. If you are using Guardium 11.5 with patch p535 or earlier,
Download [logstash-filter-xml-4.1.3-1.zip](./logstash-filter-xml-4.1.3-1.zip).<br>
III. If you are using Guardium 12.0 with patch p5 or earlier,
Download [logstash-filter-xml-4.2.0-1.zip](./logstash-filter-xml-4.2.0-1.zip).<br>
IV. For the Guardium 11.4(p490 or later),11.5(p540 or later) and 12.0(p10 or later),
Download [logstash-filter-xml-4.2.0-2.zip](./logstash-filter-xml-4.2.0-2.zip).<br>

#### Procedure:

1. On the collector, go to **Setup** > **Tools and Views** > **Configure Universal Connector**.
2. Enable the universal connector if it is currently disabled.
3. Click **Upload File** and select the offline [logstash-json-encode.zip](./logstash-json-encode.zip) plug-in. After it uploads, click **OK**.This is not necessary for Guardium Data Protection v12.0 and later.
4. Upload the relevant plugin based on the version of the Guardium.
   I. If you are using Guardium 11.4 with patch p485 or earlier,
   Download [logstash-filter-xml-4.1.3-1.zip](./logstash-filter-xml-4.1.3-1.zip).<br>
   II. If you are using Guardium 11.5 with patch p535 or earlier,
   Download [logstash-filter-xml-4.1.3-1.zip](./logstash-filter-xml-4.1.3-1.zip).<br>
   III. If you are using Guardium 12.0 with patch p5 or earlier,
   Download [logstash-filter-xml-4.2.0-1.zip](./logstash-filter-xml-4.2.0-1.zip).<br>
   IV. For the Guardium 11.4(p490 or later),11.5(p540 or later) and 12.0(p10 or later),
   Download [logstash-filter-xml-4.2.0-2.zip](./logstash-filter-xml-4.2.0-2.zip).<br>

6. Click the Plus sign to open the Connector Configuration dialog box.
7. Type a name in the **Connector name** field.
8. Update the input section to add the details from [OuaCloudwatch.conf](OracleUnifiedAuditPackageOverCloudwatch/OuaCloudwatch.conf) for AWS OUA.
9. Update the filter section to add the details from [OuaCloudwatch.conf](OracleUnifiedAuditPackageOverCloudwatch/OuaCloudwatch.conf) for AWS OUA.
10. The "type" fields should match in the input and the filter configuration sections. This field should be unique for every individual connector added. This is no longer required starting v12p20 and v12.1.
12. Click **Save**. Guardium validates the new connector and enables the universal connector if it was
    disabled. After it is validated, it appears in the Configure Universal Connector page.



