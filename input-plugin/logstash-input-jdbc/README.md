## jdbc input plug-in

This is a [Logstash](https://github.com/elastic/logstash) input plug-in for the universal connector that is featured in IBM Security Guardium. It connects to the Database using driver library and pull the events from the audit log tables. The events are then sent over to corresponding filter plugin which transforms these audit logs into a [Guardium record](https://github.com/IBM/universal-connectors/blob/main/common/src/main/java/com/ibm/guardium/universalconnector/commons/structures/Record.java)  instance (which is a standard structure made out of several parts). The information is then sent over to Guardium. Guardium records include the accessor (the person who tried to access the data), the session, data, and exceptions. If there are no errors, the data contains details about the query "construct". The construct details the main action (verb) and collections (objects) involved.


## Purpose:

Specify the "Select" query with the required parameters, to pull the Events from the audit tables, and this plugin will pull the events into Guardium.


## Usage:

### Parameters:
	
| Parameter | Input Type | Required | Default |
|-----------|------------|----------|---------|
| jdbc_driver_class  | String | Yes |   |
| jdbc_connection_string  | String | Yes |   |
| jdbc_user  | String | Yes |   |
| jdbc_password  | String | Yes |   |
| statement  | String | Yes |   |
| use_column_value  | String | Yes |   |
| tracking_column_type  | String | Yes | numeric  |
| tracking_column  | String | Yes |   |
| last_run_metadata_path  | String | Yes |  "$HOME/.logstash_jdbc_last_run" |
| schedule  | String | Yes |   |
| jdbc_paging_enabled  | Boolean | No |  false |
| jdbc_page_size  | Number | No | 100000  |


#### `jdbc_driver_class`
The `jdbc_driver_class` setting allows to set JDBC driver class to load, for example, "org.apache.derby.jdbc.ClientDriver"

#### `jdbc_connection_string`
The `jdbc_connection_string` setting allows to set the JDBC connection string, for example, "jdbc:sqlserver://<server_name>:<port>;databaseName=<database_name>"

#### `jdbc_user`
The `jdbc_user` setting allows to set the username used to connect to the database that has access to the Audit tables to be queried.

#### `jdbc_password`
The `jdbc_password` setting allows to set the password for the above jdbc_user.

#### `statement`
The `statement` setting allows to set the SELECT query using which the Audit tables are queried for the audit logs.

#### `use_column_value`
The `use_column_value` setting when set to true, uses the defined tracking_column value as the :sql_last_value. When set to false, :sql_last_value reflects the last time the query was executed.

#### `tracking_column_type`
The `tracking_column_type` setting allows to set the type of tracking column. Currently only "numeric" and "timestamp"

#### `tracking_column`
The `tracking_column` setting allows to set the column whose value is to be tracked if use_column_value is set to true

#### `last_run_metadata_path`
The `last_run_metadata_path` setting allows to set the path to file with last run time

#### `schedule`
The `schedule` setting allows to set the schedule of when to periodically run statement, in Cron format for example: "* * * * *" (execute query every minute, on the minute). There is no schedule by default. If no schedule is given, then the statement is run exactly once.

#### `jdbc_paging_enabled`
The `jdbc_paging_enabled` setting will cause a sql statement to be broken up into multiple queries. Each query will use limits and offsets to collectively retrieve the full result-set. The limit size is set with jdbc_page_size.

#### `jdbc_page_size`
The `jdbc_page_size` setting will set the JDBC page size

**Note: For moderate to large amounts of data, include pagination to facilitate the audit and to avoid out of memory errors.  Use the parameters below in the input section when using a JDBC connector, and remove the concluding semicolon ';' from the jdbc statement:**
			jdbc_paging_enabled => true
			jdbc_page_size => 1000

#### Logstash Default config params
Other standard logstash parameters are available such as:
* `add_field`
* `type`
* `tags`

### Example

	input {
		jdbc {
			jdbc_driver_class => "com.sap.db.jdbc.Driver"
			jdbc_connection_string => "jdbc:sap://<server-name>:<db-port-number>/?databaseName=<db-name>&user=<user_name>&password=<password>"
			jdbc_user => "<user_name>"
			jdbc_password => "<password>"
			statement => "SELECT <parameters> FROM <Audit_tables> WHERE <tracking_column> > :sql_last_value;"
			use_column_value => true
			tracking_column_type => "numeric"
			tracking_column => "new_timestamp"
			last_run_metadata_path => "./.example_logstash_jdbc_last_run"
			type => "test"
			schedule => "*/2 * * * *"
		}
	}

## JDBC Load Balancing Configuration

In JDBC input plug-ins , we distribute load between two machines based on even and odd sessionIds or any other Primary Key of the Audit Table.

### Procedure

1. On the first G Machine, in the input section for JDBC Plug-in, update the "statement" field as follows:

		SELECT <parameters> FROM <Audit_tables> WHERE mod(PK_column,2) = 0 and <tracking_column> > :sql_last_value;

2. On the second G machine, in the input section for the JDBC Plug-in, update the  "statement" field as follows:

		SELECT <parameters> FROM <Audit_tables> WHERE mod(PK_column,2) = 1 and <tracking_column> > :sql_last_value;
