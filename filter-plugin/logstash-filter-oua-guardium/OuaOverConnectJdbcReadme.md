# Oracle Unified Audit Universal Connector Over JDBC Connect

## Meet Oracle Unified Audit Over JDBC Connect
* Tested versions: 19
* Environments: On-prem, RDS in AWS
* Supported inputs: Kafka Input (pull)
* Supported Oracle versions: 18, 19, and 21
* Supported Guardium versions:
    * Guardium Data Protection: 12.1 and above

kafka-connect is framework for streaming data between Apache Kafka and other systems.
Detailed breakdown:
1. Kafka-connect JDBC Connector: used to pull data from `UNIFIED_AUDIT_TRAIL`.
2. Produce to Kafka: The queried data is then sent (produced) to a Kafka topic.
3. Consume with UC: The data in the Kafka topic is consumed by kafka-input plugin and process by the 'guardium-oua-uc' filter plug-in,
   a specific Unified Connector designed for your use case.

### Requirements
1. This feature currently only supported in environment with CM management 12.1 and kafka cluster
2. Unified auditing must be enabled in an Oracle database that will be monitored by this method
3. Download the Oracle JDBC driver. Download here:
   https://download.oracle.com/otn-pub/otn_software/jdbc/234/ojdbc8.jar
4. Currently, this plug-in will work only on IBM Security Guardium Data Protection, not in Guardium Insights

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
      
### Configuring Universal Connector on Guardium Data Protection
1. ### Creating a Kafka Cluster on Guardium
   For information on creating Kafka Clusters, see the [Managing Kafka clusters](https://www.ibm.com/docs/en/gdp/12.x?topic=configuration-managing-kafka-clusters) topic.
2. ### Configuring Universal Connector
   For information on configuring the Universal Connector on Guardium using the new flow, see [Managing universal connector configuration](https://www.ibm.com/docs/en/gdp/12.x?topic=connector-managing-universal-configuration) topic.

### Limitations 
- Traffic is not getting captured on the Guardium report after the Oracle DB server reboot - as a temporary workaround, uninstalling and then reinstalling the profile will work in this case.
- Currently, the following activities are not being captured in the Guardian reports:
  - Logon/Logoff
  - Startup/Shutdown
  - backup/restore
  We are aware of this limitation and are actively working on a resolution, which will be included in the upcoming UC version.

