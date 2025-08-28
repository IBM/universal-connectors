# DocumentDB-Guardium Logstash filter plug-in
### Meet DocumentDB
* Tested versions: 4.0
* Environment: AWS
* Supported inputs: CloudWatch (pull)
* Supported Guardium versions:
    * Guardium Data Protection: 11.4 and above
    * Guardium Data Security Center SaaS: 1.0

This is a [Logstash](https://github.com/elastic/logstash) filter plug-in for the universal connector that is featured in IBM Security Guardium. It parses events and messages from the DocumentDB audit and profiler logs into a [Guardium record](https://github.com/IBM/universal-connectors/blob/main/common/src/main/java/com/ibm/guardium/universalconnector/commons/structures/Record.java) instance (which is a standard structure made out of several parts). The information is then sent over to Guardium. Guardium records include the accessor (the person who tried to access the data), the session, data, and exceptions. If there are no errors, the data contains details about the query "construct". The construct details the main action (verb) and collections (objects) involved. 
The DocumentDB plugin supports only Guardium Data Protection as of now. 


## Enabling Auditing

### Creating the database parameter group
#### Procedure:
1. On Amazon DocumentDB console, choose Parameter groups on left panel. And click on "Create" on landing page. Give a name and description to new Parameter group and Click on Create.
2. Click on newly created Parameter Group from Cluster Parameter Groups list.
3. Enable "audit_logs", "profiler" and set profiler_threshold_ms to lowest value possible (50 in case of AWS)

### Associating the DB Parameter Group with the database Instance
#### Procedure: 
1. Go back to the cluster list from console's left panel, and choose the newly created cluster's checkbox and from Actions dropdown choose Modify.
2. Under cluster options, for Cluster parameter group, choose newly created parameter group and continue with saving these settings.
3. Reboot the cluster for the cluster settings to come into effect.

## Viewing the logs entries on CloudWatch
By default, each database instance has an associated log group with a name in this format: /aws/docdb/<instance_name>/audit and /aws/docdb/<instance_name>/profiler. You can use this log group, or you can create a new one and associate it with the database instance.

### Procedure
1. On the AWS Console page, open the Services menu.
2. Enter the CloudWatch string in the search box.
3. Click CloudWatch to redirect to the CloudWatch dashboard.
4. In the left panel, select Logs.
5. Click Log Groups.

### Supported audit events
* authCheck - Unauthorized attempts to perform an operation.
* createDatabase - Creation of a new database.
* createCollection - Creation of a new collection within a database.
* createIndex - Creation of a new index within a collection.
* dropCollection - Dropping of a collection within a database.
* dropDatabase - Dropping of a database.
* dropIndex - Dropping of an index within a collection.
* createUser - Creation of a new user.
* dropAllUsersFromDatabase - Dropping of all users within a database.
* dropUser - Dropping of an existing user.
* grantRolesToUser - Granting roles to a user.
* revokeRolesFromUser - Revoking roles from a user.
* updateUser - Updating of an existing user.

### Supported Profiler events
* aggregate
* count
* delete
* distinct
* find (OP_QUERY and command)
* findAndModify
* insert
* update

### Notes
1. ServerHostname Pattern for DocumentDB: accountID_ClusterName.aws.com
2. Database name Pattern for DocumentDB: accountID_ClusterName:DatabaseName

## Configuring the DocumentDB filter in Guardium
The Guardium universal connector is the Guardium entry point for native audit/profiler logs. The Guardium universal connector identifies and parses the received events, and converts them to a standard Guardium format. The output of the Guardium universal connector is forwarded to the Guardium sniffer on the collector, for policy and auditing enforcements. Configure Guardium to read the native audit/profiler logs by customizing the DocumentDB template.

### Authorizing outgoing traffic from AWS to Guardium
1. Log in to the Guardium API.
2. Issue these commands:
```
    grdapi add_domain_to_universal_connector_allowed_domains domain=amazonaws.com
```
### Before you begin
* Configure the policies you require. See [policies](/docs/#policies) for more information.
* You must have permission for the S-Tap Management role.The admin user includes this role by     default.
* DocumentDB-Guardium Logstash filter plug-in is automatically available with Guardium Data Protection versions 12.x, 11.4 with appliance bundle 11.0p490 or later or Guardium Data Protection version 11.5 with appliance bundle 11.0p540 or later releases.
*  Download the plugin filter configuration file [documentDBCloudwatch.conf](https://github.com/IBM/universal-connectors/raw/main/filter-plugin/logstash-filter-documentdb-aws-guardium/documentDBCloudwatch.conf).
*  For Guardium Data Protection version 11.0p540 and/or 11.0p6505 and/or 12.0 and/or 12p15 download the [cloudwatch_logs plug-in](../../input-plugin/logstash-input-cloudwatch-logs/CloudwatchLogsInputPackage/offline-logstash-input-cloudwatch_log_1_0_5.zip)

**Note**: For Guardium Data Protection version 11.4 without appliance bundle 11.0p490 or prior or Guardium Data Protection version 11.5 without appliance bundle 11.0p540 or prior, download the [logstash-filter-documentdb_guardium_filter.zip](https://github.com/IBM/universal-connectors/releases/download/v1.5.6/logstash-filter-documentdb_guardium_filter.zip) plug-in. (Do not unzip the offline-package file throughout the procedure).


### Configuration
1. On the collector, go to ```Setup``` > ```Tools and Views``` > ```Configure Universal Connector```.
2. Enable the universal connector if it is disabled.
3. Click ```Upload File``` and select the offline [logstash-filter-documentdb_guardium_filter.zip](https://github.com/IBM/universal-connectors/releases/download/v1.5.6/logstash-filter-documentdb_guardium_filter.zip) plug-in. After it is uploaded, click ```OK```. This step is not necessary for Guardium Data Protection v11.0p490 or later, v11.0p540 or later, v12.0 or later.
    *  If you have installed Guardium Data Protection version 11.0p540 and/or 11.0p6505 and/or 12.0 and/or 12p15, select the offline [cloudwatch_logs plug-in](../../input-plugin/logstash-input-cloudwatch-logs/CloudwatchLogsInputPackage/offline-logstash-input-cloudwatch_log_1_0_5.zip). After it is uploaded, click ```OK```.
4. Click the Plus sign to open the Connector Configuration dialog box.
5. Type a name in the ```Connector name``` field.
6. Update the input section to add the details from the [documentDBCloudwatch.conf](https://github.com/IBM/universal-connectors/raw/main/filter-plugin/logstash-filter-documentdb-aws-guardium/documentDBCloudwatch.conf) file's input part, omitting the keyword "input{" at the beginning and its corresponding "}" at the end.

   **Note**: If you want to configure Cloudwatch with role_arn instead of access_key and secret_key then refer to the [Configuration for role_arn parameter in the cloudwatch_logs input plug-in](https://github.com/IBM/universal-connectors/blob/main/input-plugin/logstash-input-cloudwatch-logs/SettingsForRoleArn.md#configuration-for-role_arn-parameter-in-the-cloudwatch_logs-input-plug-in) topic.

7. Update the filter section to add the details from the [documentDBCloudwatch.conf](https://github.com/IBM/universal-connectors/raw/main/filter-plugin/logstash-filter-documentdb-aws-guardium/documentDBCloudwatch.conf) file's filter part, omitting the keyword "filter{" at the beginning and its corresponding "}" at the end.
8. The 'type' fields should match in the input and filter configuration sections. This field should be unique for every individual connector added.
9. Click ```Save```. Guardium validates the new connector and displays it in the Configure Universal Connector page.
10. After the offline plug-in is installed and the configuration is uploaded and saved in the Guardium machine, restart the Universal Connector using the ```Disable/Enable``` button.

## 6. Limitations
- DocumentDB Profiler logs capture any database operations that take longer than some period of time(e. g. 100 ms). If the threshold value is not configurable and set value is too high, then profiler logs may not get captured for every database operation.
- The Following important fields couldn't be mapped with DocumentDB audit/profiler logs
    - Source program : Only available in case of "aggregate" query
    - OS User : Not available with Audit/Profier logs
    - Client HostName : Not available with Audit/Profier logs
- Server IPs are also not reported because they are not part of the audit stream. That said, the "add_field" clause in the configuration adds a user defined Server Host Name that can be used in reports and policies if desired.
- Because Sniffer saves the DB name once when a new session is created, and not with every event, DB name will be updated and populated correctly in Guardium only when everytime a new database connection is established with database name. If Database connection is established without database name, then the database on which the first query for that session runs, will be retained in Guardium. Even if user switches between the databases for the same session.
- Sql Errors are not supported.

## Configuring the DocumentDB Guardium Logstash filters in Guardium Data Security Center

To configure this plug-in for Guardium Data Security Center, follow [this guide.](/docs/Guardium%20Insights/3.2.x/UC_Configuration_GI.md)

For the input configuration step, refer to the [CloudWatch_logs section](/docs/Guardium%20Insights/3.2.x/UC_Configuration_GI.md#configuring-a-CloudWatch-input-plug-in).
