# Snowflake-Guardium Logstash filter plug-in

## Meet Snowflake
* Tested versions: 7.x
* Environment: On-premise, Iaas
* Supported inputs: JDBC (pull)
* Supported Guardium versions:
    * Guardium Data Protection: 11.4 and above
    * Guardium Insights SaaS: 1.0

This is a [Logstash](https://github.com/elastic/logstash) filter plug-in for the universal connector that is featured 
in IBM Security Guardium. It parses events and messages from the Snowflake database audit log into a 
[Guardium record](https://github.com/IBM/universal-connectors/blob/main/common/src/main/java/com/ibm/guardium/universalconnector/commons/structures/Record.java) 
instance (which is a standard structure made out of several parts). The information is then sent over to Guardium. 
Guardium records include the accessor (the person who tried to access the data), the session, data, and exceptions. 
If there are no errors, the data contains details about the query "construct". The construct details the main action 
(verb) and collections (objects) involved.

The plug-in is free and open-source (Apache 2.0). It can be used as a starting point to develop additional 
filter plug-ins for Guardium universal connector.

The connector helps to monitor SQL occurring in their Snowflake environments by providing a feed of events to
the Guardium using the Universal Connector functionality.
### Credits
Converge Technology Solutions (formerly Information Insights) provided the original version of this plug-in. 
Maintenance of the plug-in has been taken over by IBM to provide improvements, such as integration with Guardium 
Insights. See the original plug-in [here](https://github.com/infoinsights/guardium-snowflake-uc-filter).


## Enabling audit logs
Audit logs are enabled by default. Some data views that Snowflake manages for audit data might 
simply be from `select` queries.
### Providing permissions to a JDBC user
The user you define below in the jdbc_user parameter must have enough permissions
to execute the SQL in the statement area. You are encouraged to test this first by replacing `:sql_last_value` with an
epoch time value and running this against Snowflake with the user in question. In particular, make sure the
user has access to the [Snowflake database](https://docs.snowflake.com/en/sql-reference/account-usage.html#enabling-snowflake-database-usage-for-other-roles).
Below are the reference queries to execute to provide access to a particular role.
```sql
  use role accountadmin;

  grant imported privileges on database snowflake to role sysadmin;
  grant imported privileges on database snowflake to role customrole1;

  use role customrole1;

  select database_name, database_owner from snowflake.account_usage.databases;
``` 

## Viewing the audit logs
To view the audit logs, query the tables `SNOWFLAKE.ACCOUNT_USAGE.QUERY_HISTORY` , `SNOWFLAKE.ACCOUNT_USAGE.SESSIONS` 
and `SNOWFLAKE.ACCOUNT_USAGE.LOGIN_HISTORY`. 
### Limitations

1. Since `SNOWFLAKE.ACCOUNT_USAGE` is a data view and the plug-in is dependent on its tables for the logs, There may be a slight delay in refreshing the data view which may result in a slight delay in retrieving the audit data.
2. The server portal must remain at 443, as configuration support is deprecated. For more information, see [here](https://docs.snowflake.com/en/user-guide/snowsql-start#p-port-deprecated).

## Configuring the Snowflake filter in Guardium
The Guardium universal connector is the Guardium entry point for native audit logs. The universal connector
identifies and parses received events, and then converts them to a standard Guardium format. The output of the
universal connector is forwarded to the Guardium sniffer on the collector, for policy and auditing enforcements.
Configure Guardium to read the native audit logs by customizing the Snowflake template.

### Authorizing outgoing traffic from AWS to Guardium

**Procedure**
1. Log in to the Guardium API.
2. Issue these commands:
   ```text
   • grdapi add_domain_to_universal_connector_allowed_domains domain=amazonaws.com
   • grdapi add_domain_to_universal_connector_allowed_domains domain=amazon.com
    ```
**NOTE**
Snowflake auditing will also capture the query executed to retrieve audit logs. Audit tables will have many entries
for the audit select statement (the query mentioned in the `statement` of JDBC input). If you don't need to track this specific select statement, 
you can turn it off. To do this, set the value to `false` for the following two fields in the Filter configurations section: 
1. `skip_logging_audit_query` - setting the value of this field to `"false"` will stop capturing the particular
`select` query that fetches the query logs.
2. `skip_logging_auth_audit_query` - setting the value of this field to `"false"` will stop capturing the particular
`select` query that fetches the authentication logs.

### Before you begin
1. Configure the policies you require. See [policies](/docs/#policies) for more information. 
2. You must have permission for the S-Tap Management role. The admin user includes this role by default.
3. Download the [logstash-filter-guardium_snowflake_filter.zip](https://github.com/IBM/universal-connectors/releases/download/v1.5.0/logstash-filter-guardium_snowflake_filter.zip)
plug-in. (Do not unzip the offline-package file throughout the procedure). This step is not necessary for Guardium Data Protection v12.0 and later.
4. The plugin is tested with Snowflake JDBC driver v3.13.30.
Download the jdbc driver `jar` file from the maven repository [here](https://repo1.maven.org/maven2/net/snowflake/snowflake-jdbc/).

### Procedure

1. On the collector, go to `Setup` > `Tools and Views` > `Configure Universal Connector`.
2. Enable the universal connector if it is disabled.
3. Click **Upload File** and select the downloaded .jar jdbc driver file. After it is uploaded, click **OK**. 
4. Click **Upload File** and select the offline [logstash-filter-guardium_snowflake_filter.zip](https://github.com/IBM/universal-connectors/releases/download/v1.5.0/logstash-filter-guardium_snowflake_filter.zip) plug-in. After it is uploaded, click **OK**. This step is not necessary for Guardium Data Protection v12.0 and later.
5. Click the Plus sign to open the Connector Configuration dialog box.
6. Type a name in the `Connector name` field.
7. Update the input section to add the details from the [snowflakeJDBC.conf](snowflakeJDBC.conf) file input section, 
omitting the keyword "input{" at the beginning and its corresponding "}" at the end.
8. Update the filter section to add the details from the [snowflakeJDBC.conf](snowflakeJDBC.conf)  file filter section, 
omitting the keyword "filter{" at the beginning and its corresponding "}" at the end.
9. The "type" fields should match in the input and the filter configuration section. This field should be unique for  
every individual connector added.
10. If you are using two JDBC plug-ins on same machine, the `last_run_metadata_path` file name should be different.
    **NOTE: For moderate to large amounts of data, include pagination to facilitate the audit and to avoid out of 
memory errors. Use the parameters below in the input section when using a JDBC connector, and remove the concluding 
semicolon ';' from the JDBC statement:**
    ```text
    jdbc_paging_enabled => true 
    jdbc_page_size => 1000
    ```

11. Click `Save`. Guardium validates the new connector and displays it in the Configure Universal Connector page.
 

## 5. JDBC load-balancing configuration,
1. For Query auditing,
   * the load can be distributed between two machines based on the even and the odd values of `EXECUTION_TIME`.
   * **Procedure**,
     * In the input section for the JDBC plug-in on the first G machine,  update the `statement` field in the first JDBC block where
       `"event_type" => "login_success"` is found inside the `add_field` section, as below.
       ```text
        SELECT
              QH.DATABASE_NAME as DATABASE_NAME,QH.SESSION_ID as SESSION_ID,
              TO_TIMESTAMP_LTZ(QH.END_TIME) as QUERY_TIMESTAMP,
              LH.CLIENT_IP as CLIENT_IP, CURRENT_IP_ADDRESS() as SERVER_IP,
              QH.USER_NAME as USER_NAME, S.CLIENT_ENVIRONMENT,
              QH.QUERY_ID, QH.QUERY_TEXT, QH.QUERY_TYPE, QH.QUERY_TAG ,
              QH.ROLE_NAME, S.CLIENT_APPLICATION_ID, QH.WAREHOUSE_NAME,
              QH.ERROR_CODE AS QUERY_ERROR_CODE, QH.ERROR_MESSAGE AS QUERY_ERROR_MESSAGE,
              QH.EXECUTION_STATUS AS QUERY_EXECUTION_STATUS,
              DATE_PART(epoch_millisecond, QH.END_TIME) AS QUERY_TIMESTAMP_EPOCH,
              QH.EXECUTION_TIME as QUERY_EXECUTION_TIME
          FROM
              SNOWFLAKE.ACCOUNT_USAGE.QUERY_HISTORY QH
          LEFT JOIN SNOWFLAKE.ACCOUNT_USAGE.SESSIONS S
              ON S.SESSION_ID = QH.SESSION_ID
          LEFT JOIN SNOWFLAKE.ACCOUNT_USAGE.LOGIN_HISTORY LH
              ON S.LOGIN_EVENT_ID = LH.EVENT_ID
          WHERE (QH.EXECUTION_STATUS <> :execution_status)
              AND DATE_PART(epoch_millisecond, QH.END_TIME) > :sql_last_value
              AND QH.EXECUTION_TIME%2=0
          ORDER BY QH.END_TIME
       ``` 
     * In the input section for the JDBC plug-in on the second G machine,  update the `statement` field in the first JDBC block where
       `"event_type" => "login_success"` is found inside the `add_field` section, as below.
       ```text
       SELECT
            QH.DATABASE_NAME as DATABASE_NAME,QH.SESSION_ID as SESSION_ID,
            TO_TIMESTAMP_LTZ(QH.END_TIME) as QUERY_TIMESTAMP,
            LH.CLIENT_IP as CLIENT_IP, CURRENT_IP_ADDRESS() as SERVER_IP,
            QH.USER_NAME as USER_NAME, S.CLIENT_ENVIRONMENT,
            QH.QUERY_ID, QH.QUERY_TEXT, QH.QUERY_TYPE, QH.QUERY_TAG ,
            QH.ROLE_NAME, S.CLIENT_APPLICATION_ID, QH.WAREHOUSE_NAME,
            QH.ERROR_CODE AS QUERY_ERROR_CODE, QH.ERROR_MESSAGE AS QUERY_ERROR_MESSAGE,
            QH.EXECUTION_STATUS AS QUERY_EXECUTION_STATUS,
            DATE_PART(epoch_millisecond, QH.END_TIME) AS QUERY_TIMESTAMP_EPOCH,
            QH.EXECUTION_TIME as QUERY_EXECUTION_TIME
        FROM
            SNOWFLAKE.ACCOUNT_USAGE.QUERY_HISTORY QH
        LEFT JOIN SNOWFLAKE.ACCOUNT_USAGE.SESSIONS S
            ON S.SESSION_ID = QH.SESSION_ID
        LEFT JOIN SNOWFLAKE.ACCOUNT_USAGE.LOGIN_HISTORY LH
            ON S.LOGIN_EVENT_ID = LH.EVENT_ID
        WHERE (QH.EXECUTION_STATUS <> :execution_status)
            AND DATE_PART(epoch_millisecond, QH.END_TIME) > :sql_last_value
            AND QH.EXECUTION_TIME%2=1
        ORDER BY QH.END_TIME
       ``` 
2. For Authentication/Login specific auditing.
   * The load can be distributed between two machines based on the even and odd values of Login `EVENT_TIMESTAMP`.
   * **Procedure**,
     * In the input section for the JDBC plug-in on the first G machine,  update the `statement` field in the first JDBC block where
       `"event_type" => "login_failed"` is found inside the `add_field` section, as below.
       ```text
        SELECT
          LH.USER_NAME AS USER_NAME,
          LH.CLIENT_IP AS CLIENT_IP,
          CURRENT_IP_ADDRESS() as SERVER_IP,
          (LH.REPORTED_CLIENT_TYPE || ' ' || LH.REPORTED_CLIENT_VERSION) AS CLIENT_APPLICATION_ID,
          LH.IS_SUCCESS AS LOGIN_SUCCESS,
          LH.ERROR_CODE AS LOGIN_ERROR_CODE,
          LH.ERROR_MESSAGE AS LOGIN_ERROR_MESSAGE,
          TO_TIMESTAMP_LTZ(LH.EVENT_TIMESTAMP) AS LOGIN_TIMESTAMP,
          DATE_PART(epoch_millisecond, LH.EVENT_TIMESTAMP) AS LOGIN_TIMESTAMP_EPOCH
        FROM SNOWFLAKE.ACCOUNT_USAGE.LOGIN_HISTORY LH
         WHERE LH.IS_SUCCESS LIKE :login_success
           AND DATE_PART(epoch_millisecond, LH.EVENT_TIMESTAMP) > :sql_last_value
           AND (DATE_PART(epoch_millisecond, LH.EVENT_TIMESTAMP))%2 = 0
        ORDER BY LH.EVENT_TIMESTAMP
       ``` 
     * In the input section for the JDBC plug-in on the second G machine,  update the `statement` field in the first JDBC block where
       `"event_type" => "login_failed"` is found inside the `add_field` section, as below.
       ```text
       SELECT
          LH.USER_NAME AS USER_NAME,
          LH.CLIENT_IP AS CLIENT_IP,
          CURRENT_IP_ADDRESS() as SERVER_IP,
          (LH.REPORTED_CLIENT_TYPE || ' ' || LH.REPORTED_CLIENT_VERSION) AS CLIENT_APPLICATION_ID,
          LH.IS_SUCCESS AS LOGIN_SUCCESS,
          LH.ERROR_CODE AS LOGIN_ERROR_CODE,
          LH.ERROR_MESSAGE AS LOGIN_ERROR_MESSAGE,
          TO_TIMESTAMP_LTZ(LH.EVENT_TIMESTAMP) AS LOGIN_TIMESTAMP,
          DATE_PART(epoch_millisecond, LH.EVENT_TIMESTAMP) AS LOGIN_TIMESTAMP_EPOCH
       FROM SNOWFLAKE.ACCOUNT_USAGE.LOGIN_HISTORY LH
         WHERE LH.IS_SUCCESS LIKE :login_success
           AND DATE_PART(epoch_millisecond, LH.EVENT_TIMESTAMP) > :sql_last_value
           AND (DATE_PART(epoch_millisecond, LH.EVENT_TIMESTAMP))%2 = 1
       ORDER BY LH.EVENT_TIMESTAMP
       
**NOTE: To support the Authentication/Login specific auditing on Guardium Insights/SaaS, create a new Snowflake connection and enter the mentioned input details in the connection settings.**

## FAQ
### Does this work with AWS, Azure, and GCP instances of Snowflake?

Yes. The schema and connection behaviour for Snowflake are the same across all those cloud service
providers. You may wish to deploy a Guardium collector in the same region as your Snowflake instance to
reduce items such as egress costs.



