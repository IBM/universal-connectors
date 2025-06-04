# Oracle Unified Audit Universal Connector Over JDBC Connect

## Meet Oracle Unified Audit Over JDBC Connect
* Tested versions: 19,21
* Environments: On-prem, RDS in AWS
* Supported inputs: Kafka Input (pull)
* Supported Oracle versions: 19,21
* Supported Guardium versions:
    * Guardium Data Protection: appliance bundle 12.1p105 or later.
 
**Note**: This readme is also applicable for **OUA over JDBC connect 2.0** and **OUA Multitenant over JDBC connect 2.0** plug-ins.

Kafka-connect is framework for streaming data between Apache Kafka and other systems.

Detailed breakdown:
1. Kafka-connect JDBC Connector: used to pull data from `UNIFIED_AUDIT_TRAIL`.
2. Kafka-connect JDBC Connector for OUA Multitenant over JDBC connect 2.0: used to pull data from `CDB_UNIFIED_AUDIT_TRAIL`.
3. Consume with UC: The data in the Kafka topic is consumed by kafka-input plugin and process by the 'guardium-oua-uc' filter plug-in,
   a specific Unified Connector designed for your use case.

**Tip**:IBM recommends creating a Kafka cluster only after your environment is patched with appliance bundle p120+ uc p5002 for Guardium Data Protection version 12.1, as using a Kafka cluster before appliance bundle p120 + uc p5002 may provide undesirable results and does not support disaster recovery scenarios. This ensures that profiles using the Kafka cluster are applied correctly.

### GDP versions available with OUA over JDBC credential support
| Credential types                     | Patch details for availability                                                                      |
|--------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **JDBC**           | GDP version 12.1 + Appliance bundle patch 105 + Universal connector patch 1006 and later                                                                                                       |
| **JDBC & Kerbseros**  | GDP version 12.1 + Appliance bundle patch 115 + Universal connector patch 1006 and later |
| **JDBC & Kerbseros for OUA 2.0** | GDP version 12.1 + Appliance bundle patch 120 + Universal connector patch p5002 |



### Requirements
1. This feature currently only supported in environment with CM management 12.1 and kafka cluster
2. Unified auditing must be enabled in an Oracle database that will be monitored by this method
3. Download the Oracle JDBC driver from the **Configuring Universal Connector Profile** topic. 

## Setup

1.  **Create a designated Database User for OUA UC to retrieve audit data with minimal privileges (using DBA help) as follows:**
    - Assuming the name for the designated Oracle Unified Audit user with minimal permissions will be "guardium" with password "password"
    - Connect to Oracle using sysdba account and execute the following commands:

        ```
        CREATE USER guardium IDENTIFIED BY password;
        GRANT CONNECT, RESOURCE to guardium;
        GRANT SELECT ANY DICTIONARY TO guardium;
        ```

    - To verify your new user's privileges, connect to the Oracle instance that you planning to monitor using the name and credentials for your designated user and run the following statements:

        ```
        select count(*) from AUDSYS.AUD$UNIFIED;
        ```

    - If there are no errors that means you can use this new user for this UC method
   
    - Apply the following policy to capture changes to system parameters:
        ```
        CREATE AUDIT POLICY system_param_changes ACTIONS ALTER SYSTEM;
        AUDIT POLICY system_param_changes;
        ```
      
   2. **Exclude Auditing for the DB User Performing JDBC Queries
      To avoid 'self-monitoring' of the database user executing JDBC queries, follow these steps to exclude the user from being audited:**
        ```
        # Connect as SYSDBA
        sqlplus / AS SYSDBA
   
        # make sure audit_trail is set to none
        SHOW PARAMETER audit_trail;
   
        # log to PDB
        ALTER SESSION SET CONTAINER = <PDB_name>;
   
        # Create a new user with connect and dictionary privilege (for example: AUDITUSER)

        CREATE USER  AUDITUSER IDENTIFIED BY guardium;
        GRANT CONNECT, RESOURCE to AUDITUSER;
        GRANT SELECT ANY DICTIONARY TO AUDITUSER;

        # Check which policy enabled
        select distinct * from AUDIT_UNIFIED_ENABLED_POLICIES;

        # Remove audit policies like ALL_ACTIONS that include
        NOAUDIT POLICY ALL_ACTIONS;

        # Create ALL_ACTIONS policy if not exists

        CREATE AUDIT POLICY ALL_ACTIONS ACTIONS INSERT, SELECT, UPDATE, DELETE;
        SELECT * FROM AUDIT_UNIFIED_POLICIES WHERE POLICY_NAME = ' ALL_ACTIONS';

        #  Add ALL_ACTIONS policy to all user except AUDITUSER
        AUDIT POLICY ALL_ACTIONS EXCEPT "AUDITUSER";
        ```
**For further details about configuring audit policies, see [official Oracle documentation](https://docs.oracle.com/en/database/oracle/oracle-database/19/dbseg/configuring-audit-policies.html).**
## Configuring Universal Connector on Guardium Data Protection

### Before you begin 
* Configure the policies you require. See [policies](/docs/#policies) for more information.

### Configuring Universal Connector Profile
1. See [Creating data source profile topic](https://www.ibm.com/docs/en/gdp/12.x?topic=configuration-creating-data-source-profiles) to create a datasource profile.
2. Select '**OUA over JDBC connect**' in the plug-ins list
3. Update the parameters as follows:

| Field                    | Description                                                                                                                                                                                                                                                         |
|--------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Credential**           | Create JDBC credentials. For more information, see [Creating Credentials](https://www.ibm.com/docs/en/gdp/12.x?topic=configuration-creating-credentials).                                                                                                         |
| **Kafka cluster**        | Select the appropriate Kafka cluster from the available Kafka cluster list or create a new Kafka cluster. For more information, see [Managing Kafka clusters](https://www.ibm.com/docs/en/gdp/12.x?topic=flow-creating-kafka-clusters).                   |
| **Maximum poll records** | By default, the value is 1000. To get more efficient results, you can increase the Kafka cluster partition by setting this value to 2000. **Restriction**: Once partitions are increased, they cannot be decreased. After updating this value, reinstall the profile. |
| **Poll timeout (ms)**    | By default, the value is 500.                                                                                                                                                                                                                                       |
| **Initial Time (ms)**    | The timestamp from which the connector starts polling for changes in the database. Setting this to 0 means the connector starts from the earliest available data. For incremental data fetching, this ensures only new data (after the initial time) is retrieved.  |
| **Hostname**             | Specifies the hostname or IP address of the Oracle database server. It is the address where the Oracle instance can be accessed for establishing a JDBC connection.                                                                                                |
| **JDBC driver library**  | The Oracle JDBC driver JAR file (e.g., `ojdbc8.jar`) is required for the connector to communicate with the Oracle database. Download the [Oracle JDBC driver JAR file](https://download.oracle.com/otn-pub/otn_software/jdbc/234/ojdbc8.jar) and upload it to the Kafka Connect environment. |
| **Port**                 | Specifies the port number used to connect to the Oracle database. The default port number is 1521, but it can vary depending on the Oracle configuration. Port 1521 must be open and accessible for the connection.                                                 |
| **Service Name / SID**   | Specifies the Oracle service name (or SID if it's an older configuration) for the Kafka connector to connect. The service name uniquely identifies a database service within an Oracle environment and is provided by the database administrator. For OUA over JDBC data is retrived from the service itself: unified_audit_trail .                  |
| **CDB Service Name / SID**   | OUA over JDBC Connect 2.0 and OUA multitenant over JDBC Connect, data is retrived from CDB service audit log: cdb_unified_audit_trail. |


4. Continue from step 3 of [Creating data source profile topic](https://www.ibm.com/docs/en/gdp/12.x?topic=configuration-creating-data-source-profiles) to complete creating a datasource profile. 
