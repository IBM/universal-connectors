# Guardium Snowflake filter plug-in
A Guardium Univer***REMOVED***l Connector filter plugin for Snowflake. The connector allows people to
monitor SQL occuring in their Snowflake environments by providing a feed of events to
Guardium using the new [V11.3 Univer***REMOVED***l Connector functionality](https://www.ibm.com/support/knowledgecenter/SSMPHH_11.3.0/com.ibm.guardium.doc.stap/guc/g_univer***REMOVED***l_connector.html).

## Credits
Converge Technology Solutions (formerly Information Insights) provided the original version of this plug-in. Maintenance of the plug-in has been taken over by IBM to provide improvements, such as integration with Guardium Insights. See the original plug-in [here](https://github.com/infoinsights/guardium-snowflake-uc-filter).

## Building the Plugin
A good resource for compiling and packaging this plugin is the documentation outlining 
[IBM's MongoDB filter](https://github.com/IBM/logstash-filter-mongodb-guardium), which 
this project is based on.

## Guardium Version
You should use this plugin with Guardium V11.4. While it technically worked in V11.3 there were
issues around both parsing and configuring the plugin that have been greatly improved in V11.4.

## Installing the Plugin
This section of the readme outlines the installation process.

### 1. Install the Plugin
The first step is to install a plug-in pack that includes this plugin. The plugin package can be downloaded from this repository [here](https://github.com/infoinsights/guardium-snowflake-uc-filter/raw/main/logstash-offline-plugins-7.12.1.zip). Instructions on how
to do so can be found [here](https://www.ibm.com/support/knowledgecenter/SSMPHH_11.3.0/com.ibm.guardium.doc.stap/guc/test_filter_guardium.html).

### 2. Upload JDBC Driver and Initialize :sql_last_value
Once installed, upload a Snowflake JDBC driver as described [here](https://www.ibm.com/support/knowledgecenter/SSMPHH_11.3.0/com.ibm.guardium.doc.stap/guc/test_filter_guardium.html).

We have tested with Snowflake JDBC driver V3.9.2. You can file the JDCB driver [here](https://repo1.maven.org/maven2/net/snowflake/snowflake-jdbc/3.9.2/).

Next, to prevent Snowflake errors from occurring when querying data that is for the entire previous year, create 
a file called "metadata" and initialize it to a time that is at a date sooner than a week ago.
Use the following format as an example:
```
--- 2020-12-22 11:23:20.085000000 -00:00
```
That should be the only contents of the "metadata" file. Upload it the ***REMOVED***me way you uploaded
the JDBC driver. It is used later to keep track of the :sql_last_value parameter

### 3. Configure the Input and Filter Plugins

Configure a JDBC Logstash **input** source using this as a template. Replace all 
values located in angle brackets.

**NOTE**: The user you define below in the jdbc_user parameter must have enough permissions to execute the SQL in statement area.
You are encouraged to test this first by replacing :sql_last_value with a string literal and running
this against Snowflake with the user in question. In particular, make sure the user in question [has access
to the "SNOWFLAKE" database](https://docs.snowflake.com/en/sql-reference/account-u***REMOVED***ge.html#enabling-snowflake-database-u***REMOVED***ge-for-other-roles).

```sql
use role accountadmin;

grant imported privileges on database snowflake to role sy***REMOVED***dmin;
grant imported privileges on database snowflake to role customrole1;

use role customrole1;

select database_name, database_owner from snowflake.account_u***REMOVED***ge.databases;
```

The “type” fields should match in the input and the filter configuration sections. This field should be unique for every individual connector added.

```ruby
jdbc {
    type => "snowflake"
    jdbc_connection_string => "jdbc:snowflake://<id>.<region>.<provider>.snowflakecomputing.com/?warehouse=<warehouse>&db=<database>"
    jdbc_user => "<username>"
    jdbc_password => "<password>"
    jdbc_validate_connection => true
    jdbc_driver_class => "Java::net.snowflake.client.jdbc.SnowflakeDriver"
    jdbc_driver_library => "/usr/share/logstash/logstash-core/lib/jars/snowflake-jdbc-3.9.2.jar"
    use_column_value  => true
    tracking_column_type => "timestamp"
    last_run_metadata_path => "/usr/share/logstash/third_party/metadata"
    record_last_run => true
    schedule =>  "* * * * *" 
    tracking_column => "end_time"
    plugin_timezone => "local"
    add_field => {"server_host_name" => "<id>.<region>.<provider>.snowflakecomputing.com"}
    statement => "
    SELECT * FROM
      SNOWFLAKE.ACCOUNT_USAGE.query_history QH,
	    SNOWFLAKE.ACCOUNT_USAGE.LOGIN_HISTORY LH,
	    SNOWFLAKE.ACCOUNT_USAGE.SESSIONS S
	    WHERE QH.SESSION_ID = S.SESSION_ID 
       AND LH.EVENT_ID = S.LOGIN_EVENT_ID
       AND QH.execution_status <> 'RUNNING'
       AND QH.end_time > :sql_last_value  || ' -0000'
       AND QH.end_time < DATEADD(HOUR, -1, CURRENT_TIMESTAMP)
       ORDER BY QH.END_TIME
    "
}
```

The configuration for the Snowflake filter plugin is simpler:
```ruby
if [type] == "snowflake" {
   guardium_snowflake_filter{
   }
}
```


## Known Issues and Limitations
This is a list of known issues and limitations. Not all issue are resolvable as the data the connector
can provide is limitted by what Snowflake keeps track of in its audit logs.

1. Server IPs are also not reported because they are not part of the audit stream. That ***REMOVED***id, the "add_field" clause in the example shown above adds a user defined Server Host Name that can be used in reports and policies if desired.
2. "OS User" is not provided by the Snowflake audit stream, but instead we made the decision to populate "OS User" with the current user's role in Snowflake as that might be useful information.
3. We are using the tables in SNOWFLAKE.ACCOUNT_USAGE above. We do that because it is the only way we know of to join SQL data to informaton on client IPs and source programs. Note though because we are using those tables and not the ones in information_schema, there is a 
delay of 1 hour between SQL execution and the data being reflected in the tables and in Guardium as a result.

## FAQ
### Will my policies and reports work the ***REMOVED***me way?

Yes, with a few exceptions. As with any univer***REMOVED***l connector plugin, policy rules related to logging 
behave the ***REMOVED***me as if the data was coming from an STAP. The ***REMOVED***me goes with reporting. That ***REMOVED***id, extrusion
rules do not work as there is no returned data to inspect. Blocking rules (S-GATE/S-TAP terminate) also do 
not work.


### Does this work with AWS, Azure, and GCP instances of Snowflake?

Yes. The schema and connection behaviour for Snowflake are the ***REMOVED***me across all those cloud service
providers. You may wish to deploy a Guardium collector in the ***REMOVED***me region as your snowflake instance to
reduce items such as egress costs.

Have another question? [Open an issue](https://github.com/infoinsights/guardium-snowflake-uc-filter/issues) in this repo and we'll answer and post it here.


## Screenshot
Here's a screenshot of a report showing Snowflake data in Guardium data protection:

![Viewing Snowflake data in Guardium Data Protection](Screenshot%202022-12-08%20160128.png)

Author: John Haldeman



