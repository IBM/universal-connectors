# Oracle Unified Audit Universal Connector

## Meet Oracle Unified Audit

* Tested versions: 18,19
* Environments: On-prem, RDS in AWS, Oracle Autonomous Database in OCI

   **Note**: 
     * Autonomous Database in OCI is supported only by Guardium Data Protection  SqlGuard-12.0p7015_Bundle_May_20_2024 and SqlGuard-11.0p545_Bundle_Jul_09_2024.
     * For Oracle Autonomous Database, TCP is the only supported protocol, which does not provide built-in encryption.
* Supported inputs: Oracle Unified Audit (pull)
* Supported Oracle versions: 18, 19, and 21
* Supported Guardium versions:
    * Guardium Data Protection: 11.4 and above 

## Requirements

1. Unified auditing must be enabled in an Oracle database that will be monitored by this method
2. Download the Basic Instant client package from Oracle.
   **Note:** : In this release only specific Instant client will be supported from v21.1.0.0.0 Download [here](https://download.oracle.com/otn_software/linux/instantclient/211000/oracle-instantclient-basic-21.1.0.0.0-1.x86_64.rpm)
3. Download the OUA universal connector plug-in  `guardium-oua-uc.zip` from [here](./guardium-oua-uc.zip)
4. A designated user for OUA UC should be created for Oracle database access. An existing user with sysdba privileges can also be used
5. A secret containing the user’s password for OUA universal connector must be created
    - Example: `grdapi universal_connector_keystore_add key=OUA_USER_PASS password=<PASSWORD>` where `<PASSWORD>` is the OUA universal connector user’s password for the database. `OUA_USER_PASS` will be used in the plug-in configuration as a variable for password secret

Currently, this plug-in will work only on IBM Security Guardium Data Protection, not Guardium Insights

## Building

Update the variables in Makefile for your environment's Java home and Logstash location

## Setup

1. Create a designated Database User for OUA UC to retrieve audit data with minimal privileges (using DBA help) as follows:
    - Assuming the name for the designated Oracle Unified Audit user with minimal permissions will be "guardium" with password "password"
    - Connect to Oracle using sysdba account and execute the following commands:

        ```
        CREATE USER guardium IDENTIFIED BY password;
        GRANT CONNECT, RESOURCE to guardium;
        GRANT SELECT ANY DICTIONARY TO guardium;
        exec DBMS_NETWORK_ACL_ADMIN.APPEND_HOST_ACE(host => 'localhost',
        ace => xs$ace_type(privilege_list => xs$name_list('connect',
        'resolve'), principal_name => 'guardium', principal_type => xs_acl.ptype_db));
        ```

    - To verify your new user's privileges, connect to the Oracle instance that you planning to monitor using the name and credentials for your designated user and run the following statements:

        ```
        select count(*) from AUDSYS.AUD$UNIFIED;
        SELECT UTL_INADDR.get_host_address FROM DUAL;
        ```

    - If there are no errors that means you can use this new user for this UC method

2. Enable the universal collector feature on the designated Guardium collectors or the stand-alone system. See [here](https://www.ibm.com/docs/en/guardium/11.4?topic=connector-enabling-guardium-universal-collectors)

3. On the collector, go to Setup > Tools and Views > Configure Universal Connector

4. Click on the "UPLOAD” button and upload downloaded `oracle-instantclient-basic-21.1.0.0.0-1.x86_64.rpm` and then `guardium-oua-uc.zip`

    5. Click on "+". The Connector Configuration dialog opens

        - Type any unique name in the "Connector name" field

            - Paste content of "[ouaPipe.conf](https://github.ibm.com/Activity-Insights/universal-connectors/blob/master/filter-plugin/logstash-filter-oua-guardium/OracleUnifiedAuditPackageOverPipe/ouaPipe.conf)" in the **Input configuration** field

            - Paste content of "[ouaPipe.conf](https://github.ibm.com/Activity-Insights/universal-connectors/blob/master/filter-plugin/logstash-filter-oua-guardium/OracleUnifiedAuditPackageOverPipe/ouaPipe.conf)" in the **Filter configuration** field

    
        - **NOTE**: The type specified for the filters must be unique among Universal Connectors and be identical in the input and filter configurations

    6. Click **Save**. Guardium validates the new connector and enables plugin. After it is validated, it appears in the **Configure Universal Connector** page.

**Note**: The following arguments are used in configuration for the OUA plug-in
- -c path : path to instantclient libraries
- -a path : path to tnsnames.ora (directory, not the filename)
- -r rows : number of rows to fetch at a single time (default 100)
- -t timeout : timeout for all operations (connect, execute, fetch, etc) specified in milliseconds (default 300000)
- -p period : period of time between passes specified in seconds (default 300)
- -s path : path to directory where state should be saved
- -j : output audits in JSON format

## Limitations
    	Normally, the "Statement Type" attribute for the "FULL SQL" entity in Guardium reports shows us whether a full SQL statement is a prepared statement. However, because OUA doesn't give us information about whether a statement is a prepared statement or not, the "Statement type" attribute is not applicable for the OUA universal connector plug-in.

## Troubleshooting

### Issue: Oracle DB has too many audit logs

If you are encountering issues related to excessive audit logs in the Oracle database, it might be causing performance issues or S-TAP not coming up and no traffic in collector. The following steps can help resolve this issue:

1. **Clean up Oracle DB audit logs:**

   It's possible that the Oracle database has accumulated too many audit logs. You can clean them up using the following command:

   ```sql
   exec dbms_audit_mgmt.clean_audit_trail(audit_trail_type => dbms_audit_mgmt.audit_trail_unified, use_last_arch_timestamp => FALSE);
   ```

   **Note:** This command should be executed with a user that has admin privileges.

2. **Restart the UC from GUI:**

   After cleaning up the audit logs, restart the UC (Universal Connector) for the changes to take effect. You can restart the UC by disabling and then enabling it through the GUI.

   - Go to Configure Universal Connector page. Click on the Disable button if it is enabled already.
   - Wait for a few seconds till it loads.
   - Enable the Universal Guardium connector by clicking enable button.

This should resolve any issues related to the audit log overload.

### Issue: Change the user password (OUA_USER_PASS) in Keystore

Complete the following steps to change the OUA user password in Keystore.

1. Remove the existing password from the **OUA_USER_PASS** parameter.

   ```grdapi universal_connector_keystore_remove key=OUA_USER_PASS```  

3. Add new user password.  

   ```grdapi universal_connector_keystore_add key=OUA_USER_PASS password=password```   

4. Restart the Universal Connector. 

    ```grdapi stop_universal_connector``` 

    ```grdapi run_universal_connector```   

5. Verify the password update status by using the following grdapis.  

  a. Verify if **OUA_USER_PASS** is defined in the Keystore.  

    grdapi universal_connector_keystore_list   

  b. Verify the Universal Connector status.

    grdapi get_universal_connector_status 

### Issue: UC OUA Connection Failure Caused by Special Character in Oracle Password

#### Root Cause:
The issue occurs when a special character, specifically `@`, is used in the Oracle database password. This interferes with the connection string and leads to connection failures.

#### Symptoms:
- No audit traffic is seen in the Guardium collector.
- UC fails to connect to the database.
- S-TAP not showing in the S-TAP status monitor page. 

#### Example Logs:
ORA-12154: TNS:could not resolve the connect identifier specified
can't connect to: rdxxxxx1d@infdbrds001awsd...:1521/DBMINFD1

#### Resolution:
1. **Update the Oracle Password**  
   - Remove or replace the `@` character from password for the database.
   - Update the password in the Universal Connector configuration.

2. **Restart the UC Connector**  
   - Go to *Configure Universal Connector* in the GUI.
   - Click **Disable**, wait a few seconds, then click **Enable** to restart the connector.

#### Note:
Special characters like `@` are reserved in connection strings. Avoid using them in database passwords when configuring UC with Guardium.
