# Oracle Unified Audit Univer***REMOVED***l Connector

## Meet Oracle Unified Audit

* Tested versions: 18,19
* Environment: On-prem, RDS
* Supported inputs: Oracle Unified Audit (pull)
* Supported Guardium versions: Guardium Data Protection: 11.4 and above

## Requirements

1. Unified auditing must be enabled in an Oracle database that will be monitored by this method
2. Download the Basic Instant client package from Oracle.
   **Note:** : In this release only specific Instant client will be supported from v21.1.0.0.0 Download [here](https://download.oracle.com/otn_software/linux/instantclient/211000/oracle-instantclient-basic-21.1.0.0.0-1.x86_64.rpm)
3. Download the OUA univer***REMOVED***l connector plug-in  `guardium-oua-uc.zip` from [here](https://github.com/IBM/univer***REMOVED***l-connectors/raw/main/filter-plugin/logstash-filter-oua-guardium/OracleUnifiedAuditPackage/OracleUnifiedAudit/guardium-oua-uc.zip)
4. A designated user for OUA UC should be created for Oracle database access. An existing user with sysdba privileges can also be used
5. A secret containing the user’s password for OUA univer***REMOVED***l connector must be created
    - Example: `grdapi univer***REMOVED***l_connector_keystore_add key=OUA_USER_PASS password=<PASSWORD>` where `<PASSWORD>` is the OUA univer***REMOVED***l connector user’s password for the database. `OUA_USER_PASS` will be used in the plug-in configuration as a variable for password secret

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

2. Enable the univer***REMOVED***l collector feature on the designated Guardium collectors or the stand-alone system. See [here](https://www.ibm.com/docs/en/guardium/11.4?topic=connector-enabling-guardium-univer***REMOVED***l-collectors)

3. On the collector, go to Setup > Tools and Views > Configure Univer***REMOVED***l Connector

4. Click on the "UPLOAD” button and upload downloaded `oracle-instantclient-basic-21.1.0.0.0-1.x86_64.rpm` and then `guardium-oua-uc.zip`

   5. Click on "+". The Connector Configuration dialog opens 

       - Type any unique name in the "Connector name" field

         - Paste content of "[ouaPipe.conf](https://github.com/IBM/univer***REMOVED***l-connectors/blob/main/filter-plugin/logstash-filter-oua-guardium/ouaPipe.conf)" in the **Input configuration** field

             ```
           pipe {
             type => "oua"
             command => "${OUA_BINARY_PATH} -c ${THIRD_PARTY_PATH} -s ${THIRD_PARTY_PATH} -r 1 -t 1000 -p 10 -j <USER>/${OUA_USER_PASS}@<SERVER_ADDRESS>:<SERVER_PORT>/<INSTANCE_NAME>"
                         add_field => {"SERVER_ADDRESS" => "<Enter_Server_Address>"}
                             add_field => {"SERVER_PORT" => "<Enter_Server_Port>"} }
           ```

       - Paste content of "[ouaPipe.conf](https://github.com/IBM/univer***REMOVED***l-connectors/blob/main/filter-plugin/logstash-filter-oua-guardium/ouaPipe.conf)" in the **Filter configuration** field

           ```
              if [type] == "oua" {
           json {
               source => "mes***REMOVED***ge"
           }
               mutate {
                       add_field => {"[HostName]" => "%{SERVER_ADDRESS}" }
                       add_field => {"[PortNumber]" => "%{SERVER_PORT}" }
                       }
           if "_jsonparsefailure" not in [tags] {
               oua_filter {}
           }
         }

           ```

       - **NOTE**: The type specified for the filters must be unique among Univer***REMOVED***l Connectors and be identical in the input and filter configurations

   6. Click **Save**. Guardium validates the new connector and enables plugin. After it is validated, it appears in the **Configure Univer***REMOVED***l Connector** page.

**Note**: The following arguments are used in configuration for the OUA plug-in
- -c path : path to instantclient libraries
- -a path : path to tnsnames.ora (directory, not the filename)
- -r rows : number of rows to fetch at a single time (default 100)
- -t timeout : timeout for all operations (connect, execute, fetch, etc) specified in milliseconds (default 300000)
- -p period : period of time between passes specified in seconds (default 300)
- -s path : path to directory where state should be ***REMOVED***ved
- -j : output audits in JSON format

## Limitation

Normally, the "Statement Type" attribute for the "FULL SQL" entity in Guardium reports shows us whether a full SQL statement is a prepared statement. However, because OUA doesn't give us information about whether a statement is a prepared statement or not, the "Statement type" attribute is not applicable for the OUA univer***REMOVED***l connector plug-in.
