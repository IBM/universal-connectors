# DocumentDB-Guardium Logstash filter plug-in
### Meet DocumentDB
* Tested versions: 4.0
* Environment: AWS
* Supported inputs: CloudWatch (pull)
* Supported versions:
    * GDP: 11.3 and above

This is a [Logstash](https://github.com/elastic/logstash) filter plug-in for the univer***REMOVED***l connector that is featured in IBM Security Guardium. It parses events and mes***REMOVED***ges from the DocumentDB audit and profiler logs into a [Guardium record](https://github.com/IBM/univer***REMOVED***l-connectors/blob/main/common/src/main/java/com/ibm/guardium/univer***REMOVED***lconnector/commons/structures/Record.java) instance (which is a standard structure made out of several parts). The information is then sent over to Guardium. Guardium records include the accessor (the person who tried to access the data), the session, data, and exceptions. If there are no errors, the data contains details about the query "construct". The construct details the main action (verb) and collections (objects) involved. 
The DocumentDB plugin supports only Guardium Data Protection as of now. 

## 1. Creating AWS Cloud9 Environment

### Procedure:
1. From the AWS Management Console navigate to the AWS Cloud9 console and choose Create environment
2. Enter a name,for example GuardiumCloud9
3. Choose Next step
4. In the Configure settings section, choose Next step.
5. In the Review section, choose Create environment.

## 2. Configuring the AWS DocumentDB service/cluster
### Procedure:
1. Go to https://console.aws.amazon.com/.
2. Search for and navigate to the AWS DocumentDB management Console. Click on Launch Amazon DocumentDB, and under Clusters, choose Create.​
3. Choose a name for the new cluster, or go for the default name. Optional step: Choosing "1" for Number of instances helps minimize cost.
4. In the Authentication section, choose a username and password.
5. Turn On "Show advance settings" and under "Log exports" section, enable both "Audit logs" and "Profiler logs". Amazon DocumentDB will now provision the new cluster, and this process can take up to a few minutes to finish. User can connect to this cluster when both the cluster and instance status show as Available

### Configuring the AWS DocumentDB Security Group
### Procedure:
1. Once the cluster is available, click on the newly created cluster.
2. In security groups section, under Connection and Security tab, click on the tagged security group.
3.  Click on Inbound Rules tab and then on Edit Inbound rules to add new rule. 
4. For Type, choose Custom TCP Rule. For Port Range , enter 27017​. Note: Port 27017 is the default port for Amazon DocumentDB.​
5. In the Source feild, choose Custom and specify the public IPv4 address of the computer (that will connect to DocumentDB instance) or network in CIDR notation. For example, if your IPv4 address is 203.0.113.25, specify 203.0.113.25/32 to list this single IPv4 address in CIDR notation.
6. In our case we will add newly created cloud9 environment's security group here. The source will be the security group for the AWS Cloud9 environment we just created. To see a list of available security groups, enter cloud9 in the destination field. Choose the security group with the name aws-cloud9-<environment name>.
7. Save security group settings.

## 3. Enabling Auditing

### Creating the database parameter group
## Procedure:
1. On Amazon DocumentDB console, choose Parameter groups on left panel. And click on "Create" on landing page. Give a name and description to new Parameter group and Click on Create.
2. Click on newly created Parameter Group from Cluster Parameter Groups list.
3. Enable "audit_logs", "profiler" and set profiler_threshold_ms to lowest value possible (50 in case of AWS)

### Associating the DB Parameter Group with the database Instance
### Procedure: 
1. Go back to the cluster list from console's left panel, and choose the newly created cluster's checkbox and from Actions dropdown choose Modify.
2. Under cluster options, for Cluster parameter group, choose newly created parameter group and continue with ***REMOVED***ving these settings.
3. Reboot the cluster for the cluster settings to come into effect.

### 4. Viewing the logs entries on CloudWatch
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

## 5. Configuring the DocumentDB filter in Guardium
The Guardium univer***REMOVED***l connector is the Guardium entry point for native audit/profiler logs. The Guardium univer***REMOVED***l connector identifies and parses the received events, and converts them to a standard Guardium format. The output of the Guardium univer***REMOVED***l connector is forwarded to the Guardium sniffer on the collector, for policy and auditing enforcements. Configure Guardium to read the native audit/profiler logs by customizing the DocumentDB template.

### Authorizing outgoing traffic from AWS to Guardium
1. Log in to the Guardium API.
2. Issue these commands: 
     * grdapi add_domain_to_univer***REMOVED***l_connector_allowed_domains domain=amazonaws.com

### Before you begin
* Configure the policies you require. See [policies](/../../#policies) for more information.
* You must have permission for the S-Tap Management role.The admin user includes this role by     default.
* Download the [guardium_logstash-offline-plugin-documentdb.zip](DocumentDBOverCloudwatchPackage/guardium_logstash-offline-plugin-documentdb.zip) plug-in.
* Download the plugin filter configuration file [ documentDBCloudwatch.conf](DocumentDBOverCloudwatchPackage/documentDBCloudwatch.conf).

### Procedure
1. On the collector, go to Setup > Tools and Views > Configure Univer***REMOVED***l Connector.
2. First Enable the Univer***REMOVED***l Guardium connector, if it is di***REMOVED***bled already.
3. Click Upload File and select the [offline guardium_logstash-offline-plugin-documentdb.zip](DocumentDBOverCloudwatchPackage/guardium_logstash-offline-plugin-documentdb.zip) plug-in. After it is uploaded, click OK.
4. Click the Plus sign to open the Connector Configuration dialog box.
5. Type a name in the Connector name field.
6. Update the input section to add the details from [documentDBCloudwatch.conf](DocumentDBOverCloudwatchPackage/documentDBCloudwatch.conf) file's input part, omitting the keyword "input{" at the beginning and its corresponding "}" at the end.
7. Update the filter section to add the details from [documentDBCloudwatch.conf](DocumentDBOverCloudwatchPackage/documentDBCloudwatch.conf) file's filter part, omitting the keyword "filter{" at the beginning and its corresponding "}" at the end.
8. The "type" field should match in the input and filter configuration sections. This field should be unique for  every individual connector added.
9. Click Save. Guardium validates the new connector, and enables the univer***REMOVED***l connector if it was
di***REMOVED***bled. After it is validated, it appears in the Configure Univer***REMOVED***l Connector page.

## 6. Limitations
- DocumentDB Profiler logs capture any database operations that take longer than some period of time(e. g. 100 ms). If the threshold value is not configurable and set value is too high, then profiler logs may not get captured for every database operation. 
- The Following important fields couldn't be mapped with DocumentDB audit/profiler logs
     - Source program : Only available in case of "aggregate" query
     - OS User : Not available with Audit/Profier logs
     - Client HostName : Not available with Audit/Profier logs
- Server IPs are also not reported because they are not part of the audit stream. That ***REMOVED***id, the "add_field" clause in the configuration adds a user defined Server Host Name that can be used in reports and policies if desired.
- Because Sniffer ***REMOVED***ves the DB name once when a new session is created, and not with every event, DB name will be updated and populated correctly in Guardium only when everytime a new database connection is established with database name. If Database connection is established without database name, then the database on which the first query for that session runs, will be retained in Guardium. Even if user switches between the databases for the ***REMOVED***me session.     
