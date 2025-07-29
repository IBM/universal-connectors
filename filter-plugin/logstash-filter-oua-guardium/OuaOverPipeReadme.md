# Oracle Unified Audit Universal Connector
 
## Meet Oracle Unified Audit

* Tested versions: 18,19
* Environments: On-prem, RDS in AWS, Oracle Autonomous Database in OCI

   **Note**: Autonomous Database in OCI is supported only by Guardium Data Protection  SqlGuard-12.0p7015_Bundle_May_20_2024 and SqlGuard-11.0p545_Bundle_Jul_09_2024. 
* Supported inputs: Oracle Unified Audit (pull)
* Supported Oracle versions: 18, 19, and 21
* Supported Guardium versions:
    * Guardium Data Protection: 11.4 and above 
  

## Requirements

1. Unified auditing must be enabled in an Oracle database that will be monitored by this method
2. Download the Basic Instant client package from Oracle.
   **Note:** : In this release,only specific instance clients will be supported from starting v21.1.0.0.0 Download [here](https://download.oracle.com/otn_software/linux/instantclient/211000/oracle-instantclient-basic-21.1.0.0.0-1.x86_64.rpm)
3. Oracle Unified Audit Universal Connector is automatically available with Guardium Data Protection versions 12.x, 11.4 with appliance bundle 11.0p490 or later or Guardium Data Protection version 11.5 with appliance bundle 11.0p540 or later releases.
**Note**: For Guardium Data Protection version 11.4 without appliance bundle 11.0p490 or prior or Guardium Data Protection version 11.5 without appliance bundle 11.0p540 or prior, download the [OUA](https://github.com/IBM/universal-connectors/raw/release-v1.2.0/filter-plugin/logstash-filter-oua-guardium/OracleUnifiedAuditPackage/OracleUnifiedAudit/guardium-oua-uc.zip) plug-in. (Do not unzip the offline-package file throughout the procedure).

4. A designated user for OUA UC should be created for Oracle database access. An existing user with sysdba privileges can also be used

5. You must create a secret containing your OUA universal connector password.
   - Example: `grdapi universal_connector_keystore_add key=OUA_USER_PASS password=<PASSWORD>` where `<PASSWORD>` is the OUA universal connector user’s password for the database. `OUA_USER_PASS` will be used in the plug-in configuration as a variable for the password secret.

6. Configure the policies you require. See [policies](https://github.com/IBM/universal-connectors/tree/main/docs#policies) for more information.

Currently, this plug-in will work only on IBM Security Guardium Data Protection, not Guardium Insights

## Building

Update the variables in Makefile for your environment's Java home and Logstash location

## Setup

1. Create a designated Database User for OUA UC to retrieve audit data with minimal privileges (using DBA help) as follows:
   - Assuming the name for the designated Oracle Unified Audit user with minimal permissions will be "guardium" with password "password"
   - Connect to Oracle using sysdba account and execute the following commands:

      - For Oracle Autonomous Database in OCI, run the following commands to create a user with the required grants:
         ```
         CREATE USER guardium IDENTIFIED BY password;
         GRANT CONNECT to guardium;
         GRANT SELECT ANY DICTIONARY to guardium;
         GRANT AUDIT_VIEWER to guardium;
         GRANT SELECT ON v$INSTANCE to guardium;
         GRANT SELECT ON v$DATABASE to guardium;
         GRANT SELECT ON v$MYSTAT to guardium;
         ```

       - For other environments including RDS in AWS and Oracle Databases On-Premises run the following commands:
         ```
         CREATE USER <guardium_user> IDENTIFIED BY <guardium_password>; 
         GRANT CONNECT to <guardium_user>; 
         GRANT AUDIT_VIEWER to <guardium_user>; 
         GRANT SELECT ON v_$INSTANCE to <guardium_user>; 
         GRANT SELECT ON v_$DATABASE to <guardium_user>; 
         GRANT SELECT ON v_$MYSTAT to <guardium_user>;
         ```

   - To verify your new user's privileges, connect to the Oracle instance that you planning to monitor using the name and credentials for your designated user and run the following statements:

       ```
       select count(*) from AUDSYS.AUD$UNIFIED;
       SELECT UTL_INADDR.get_host_address FROM DUAL;
       ```

   - If there are no errors that means you can use this new user for this UC method

2. Enable the universal collector feature on the designated Guardium collectors or the stand-alone system. See [here](/docs/Guardium%20Data%20Protection/uc_config_gdp.md).

3. On the collector, go to Setup > Tools and Views > Configure Universal Connector

4. Click on the "UPLOAD” button and upload the downloaded `oracle-instantclient-basic-21.1.0.0.0-1.x86_64.rpm`.
   
6.  Then, upload the `guardium-oua-uc.zip` file. This step is not necessary for Guardium Data Protection v12.0 and later.

   5. Click on the "+". The Connector Configuration dialog opens.

      - Type any unique name in the **Connector name** field.

         - Paste the content from the  "[ouaPipe.conf](https://github.com/IBM/universal-connectors/raw/main/filter-plugin/logstash-filter-oua-guardium/ouaPipe.conf)" in the **Input configuration** field

         - Paste the content from the  "[ouaPipe.conf](https://github.com/IBM/universal-connectors/raw/main/filter-plugin/logstash-filter-oua-guardium/ouaPipe.conf)" in the **Filter configuration** field

      - **NOTE**: The type specified for the filters must be unique among Universal Connectors and be identical in the input and filter configurations

   6. Click **Save**. Guardium validates the new connector and enables the plug-in. After it is validated, it appears on the **Configure Universal Connector** page.

**Note**: The following arguments are used in the OUA plug-in configuration.
- -c path : path to instantclient libraries
- -a path : path to tnsnames.ora (directory, not the filename)
- -r rows : number of rows to fetch at a single time (default 100)
- -t timeout : timeout for all operations (connect, execute, fetch, etc) specified in milliseconds (default 300000)
- -p period : period of time between passes specified in seconds (default 300)
- -s path : path to directory where state should be saved
- -j : output audits in JSON format

## Limitation
- Normally, the "statement type" attribute for the "FULL SQL" entity in Guardium reports shows us whether a full SQL statement is a prepared statement. However, because OUA doesn't give us information about whether a statement is a prepared statement or not, the "Statement type" attribute is not applicable for the OUA universal connector plug-in.
- The "record affected" field is not supported when using Oracle with the universal connector.
- If you restart the database, then any Universal Connector that uses the OUA plugin must also be restarted.