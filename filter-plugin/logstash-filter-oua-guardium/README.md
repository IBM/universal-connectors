# Oracle Unified Audit Universal Connector

## 1. Configuring the Database
Procedure:
 For this example, we will assume that we already have a working Oracle database. 

Note: Here, we can opt for either a single server or a PostgreSQL Flexible server. The Single Server option is on the retirement path. For more information on the retirement schedule, please refer to the official Microsoft documentation.

## 2. Enabling Auditing
Unified auditing must be enabled in an Oracle database. Required policies needs to be applied.

### Some Recommended Predefined Unified Audit policies in OUA :

a. Capturing Database Failed Logins:
    
    ORA_LOGON_FAILURES;

b. Capturing Changes to System Parameters (including runtime) and Database StartUp Activity::
   
    ORA_DATABASE_PARAMETER;
   
c. Capturing Account and Role Management Activities which includes
   Create user & mask Password, Drop user,Alter user,Create role,Update role,Drop role,Grant privileges to user,Capture password change event,
   Revoke privileges from user,Grant role to user,Revoke role from user,Grant privileges to role,Revoke privileges from role,Grant role to role,Revoke role from role:

    ORA_ACCOUNT_MGMT;

d. To capture events like Create database and Drop database :

    ORA_SECURECONFIG;

### Manually Created policies for auditing in OUA :

1. Capturing Successful Logins and Logoffs:
   ```
    CREATE AUDIT POLICY login_audit_policy ACTIONS LOGON, LOGOFF;
   ```
2. Capturing DDL Activities this includes: Create index, Drop index,Create View,Drop View and Create,Drop and Alter table:
   ```
    CREATE AUDIT POLICY MY_AUDITED_ACTION 
    ACTIONS CREATE TABLE, DROP TABLE, ALTER TABLE, 
    CREATE INDEX, DROP INDEX, 
    CREATE VIEW, DROP VIEW;
   ```

Note:

Certain events, such as ALTER AUDIT, ENABLE AUDIT, DISABLE AUDIT, backup activities, and restore activities, are always audited.
SQL errors (with error codes) are always captured in UNIFIED_AUDIT_TRAIL.RETURN_CODE.



## Follow this link to set up and use Oracle Unified Audit Universal Connector over Pipe

[OuaOverPipeReadme](./OuaOverPipeReadme.md)

## Follow this link to set up and use Oracle Unified Audit Universal Connector over JDBC Connect

[OuaOverConnectJdbc](./OuaOverConnectJdbcReadme.md)