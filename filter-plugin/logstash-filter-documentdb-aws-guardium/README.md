# DocumentDB-Guardium Logstash filter plug-in

This is a Logstash filter plug-in for the universal connector that is featured in IBM Security Guardium. It parses events and messages from the DocumentDB audit and profiler logs into a Guardium record instance (which is a standard structure made out of several parts). The information is then sent over to Guardium. Guardium records include the accessor (the person who tried to access the data), the session, data, and exceptions. If there are no errors, the data contains details about the query "construct". The construct details the main action (verb) and collections (objects) involved. 
The DocumentDB plugin supports only Guardium Data Protection as of now. 


# Limitations
1. DocumentDB Profiler logs capture any database operations that take longer than some period of time(e. g. 100 ms). If the threshold value is not configurable and set value is too high, then profiler logs may not get captured for every database operation. 
2. The Following important fields couldn't be mapped with DocumentDB audit/profiler logs
     - Source program : Only available in case of "aggregate" query
     - OS User : Not available with Audit/Profier logs
     - Client HostName : Not available with Audit/Profier logs
## Creating AWS Cloud9 Environment

## Procedure:
1. From the AWS Management Console navigate to the AWS Cloud9 console and choose Create environment
2. Enter a name,for example GuardiumCloud9
3. Choose Next step
4. In the Configure settings section, choose Next step.
5. In the Review section, choose Create environment.

## Configuring the AWS DocumentDB service/cluster
## Procedure:
1. Go to https://console.aws.amazon.com/.
2. Search for and navigate to the AWS DocumentDB management Console. Click on Launch Amazon DocumentDB, and under Clusters, choose Create.​
3. Choose a name for the new cluster, or go for the default name. Optional step: Choosing "1" for Number of instances helps minimize cost.
4. In the Authentication section, choose a username and password.
5. Turn On "Show advance settings" and under "Log exports" section, enable both "Audit logs" and "Profiler logs". Amazon DocumentDB will now provision the new cluster, and this process can take up to a few minutes to finish. User can connect to this cluster when both the cluster and instance status show as Available

## Configuring the AWS DocumentDB Security Group
## Procedure:
1. Once the cluster is available, click on the newly created cluster.
2. In security groups section, under Connection and Security tab, click on the tagged security group.
3.  Click on Inbound Rules tab and then on Edit Inbound rules to add new rule. 
4. For Type, choose Custom TCP Rule. For Port Range , enter 27017​. Note: Port 27017 is the default port for Amazon DocumentDB.​
5. In the Source feild, choose Custom and specify the public IPv4 address of the computer (that will connect to DocumentDB instance) or network in CIDR notation. For example, if your IPv4 address is 203.0.113.25, specify 203.0.113.25/32 to list this single IPv4 address in CIDR notation.
6. In our case we will add newly created cloud9 environment's security group here. The source will be the security group for the AWS Cloud9 environment we just created. To see a list of available security groups, enter cloud9 in the destination field. Choose the security group with the name aws-cloud9-<environment name>.
7. Save security group settings.

# Enabling Auditing

## Creating the database parameter group
## Procedure:
1. On Amazon DocumentDB console, choose Parameter groups on left panel. And click on "Create" on landing page. Give a name and description to new Parameter group and Click on Create.
2. Click on newly created Parameter Group from Cluster Parameter Groups list.
3. Enable "audit_logs", "profiler" and set profiler_threshold_ms to lowest value possible (50 in case of AWS)

## Associating the DB Parameter Group with the database Instance
## Procedure: 
1. Go back to the cluster list from console's left panel, and choose the newly created cluster's checkbox and from Actions dropdown choose Modify.
2. Under cluster options, for Cluster parameter group, choose newly created parameter group and continue with saving these settings.
3. Reboot the cluster for the cluster settings to come into effect.

## Viewing the logs entries on CloudWatch
By default, each database instance has an associated log group with a name in this format: /aws/docdb/<instance_name>/audit and /aws/docdb/<instance_name>/profiler. You can use this log group, or you can create a new one and associate it with the database instance.

## Supported audit events
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

## Supported Profiler events
* aggregate
* count
* delete
* distinct
* find (OP_QUERY and command)
* findAndModify
* insert
* update

## Authorizing outgoing traffic from AWS to Guardium
1. Log in to the Guardium API.
2. Issue these commands: 
     * grdapi add_domain_to_universal_connector_allowed_domains domain=amazonaws.com

## Configuring the DocumentDB filter in Guardium
The Guardium universal connector is the Guardium entry point for native audit/profiler logs. The Guardium universal connector identifies and parses the received events, and converts them to a standard Guardium format. The output of the Guardium universal connector is forwarded to the Guardium sniffer on the collector, for policy and auditing enforcements. Configure Guardium to read the native audit/profiler logs by customizing the DocumentDB template.

## Logstash configuration
## Input Parameters
* log_group - string or Array of strings - Mandatory
* log_group_prefix - boolean - Non-mandatory
* access_key_id - string
* secret_access_key - string
* region - string - Default (us-east-1)
* type - string - used mainly for filter activation. No default value.

## Filter Parameters
* mutate filter - The mutate filter allows you to perform general mutations on fields. You can rename, replace, and modify fields in your events. More information around mutate filter can be found at https://www.elastic.co/guide/en/logstash/current/plugins-filters-mutate.html#plugins-filters-mutate-replace 
For DocumentDB, "replace" is used as mutate filter configuration. It is used to Replace the value of an event field with a new value, or add the field if it doesn’t already exist. Replace mutate filter is used to give a unique value of serverHostname for each configuration being added in Guardium. For every Guardium configuration, the serverHostname value will be used as the differentiating factor in STAP status table.

* Server Hostname is made up of 2 values.
	- AccountID - The AWS account id, user needs to provide the value for it.
	- [cloudwatch_logs][log_stream] - This value will be replaced with DocumentDB cluster name by Guardium.

	  example :- 92216548523_docdbcluster.aws.com

## Sample configuration
Below is a copy of the filter scope included `docdb.conf` [file](DocumentDBOverCloudwatchPackage/docdbCloudwatch.conf) that shows a basic
configuration for this plugin.
#### Input part:
```
input{
     cloudwatch_logs {
          log_group =>  ["/aws/docdb/"]
          log_group_prefix => true 
          access_key_id => "AKIA9IRPS6RR2NHG4X"
          region => "ap-south-1"
          codec => plain #Non-Mandatory
          sincedb_path => "NUL"  #Non-Mandatory
          secret_access_key => "2THpRymnRPFu2SYHrpoC7OWeR3434UYHI1dcHB5"
          event_filter => ''
          type => "docdb"
     }
}
```
#### Filter part:
```
filter {
     if [type] == "docdb" {
          mutate {
               replace => { "serverHostname" => "9856325925_%{[cloudwatch_logs][log_stream]}" } 
          }
          documentdb_guardium_filter {}
     }
}
```

## Before you begin
* You must have permission for the S-Tap Management role.The admin user includes this role by     default.
* Download the [guardium_logstash-offline-plugin-documentdb.zip](DocumentDBOverCloudwatchPackage/guardium_logstash-offline-plugin-documentdb.zip) plug-in.
* Download the plugin filter configuration file [docdb.conf](DocumentDBOverCloudwatchPackage/docdbCloudwatch.conf).

## Procedure
1. On the collector, go to Setup > Tools and Views > Configure Universal Connector.
2. Click Upload File and select the offline guardium_logstash-offline-plugin-documentdb.zip plug-in. After it is uploaded, click OK.
3. Click the Plus sign to open the Connector Configuration dialog box.
4. Type a name in the Connector name field.
5. Update the input section to add the details from docdb.conf file's input part, omitting the keyword "input{" at the beginning and its corresponding "}" at the end.
6. Update the filter section to add the details from docdb.conf file's filter part, omitting the keyword "filter{" at the beginning and its corresponding "}" at the end.
7. Click Save. Guardium validates the new connector, and enables the universal connector if it was
disabled. After it is validated, it appears in the Configure Universal Connector page.

### Set-up dev environment
Before you can build & create an updated GEM of this filter plugin, set up your environment as follows: .
1. Clone Logstash codebase & build its libraries as as specified in [How to write a Java filter plugin](https://github.com/logstash-plugins/logstash-filter-java_filter_example). Use branch 7.x (this filter was developed alongside 7.14 branch).  
2. Create _gradle.properties_ and add LOGSTASH_CORE_PATH variable with the path to the logstash-core folder you created in the previous step. For example: 

    ```LOGSTASH_CORE_PATH=/Users/taldan/logstash76/logstash-core```
	
3. Clone the [github-uc-commons](https://github.com/IBM/guardium-universalconnector-commons) project and build a JAR from it according to instructions specified there. The project contains Guardium Record structure you need to adjust, so Guardium universal connector can eventually feed your filter's output into Guardium. 
4. Edit _gradle.properties_ and add a GUARDIUM_UNIVERSALCONNECTOR_COMMONS_PATH variable with the path to the built JAR. For example:

    ```GUARDIUM_UNIVERSALCONNECTOR_COMMONS_PATH=../guardium-universalconnector-commons/build/libs```

If you'd like to start with the most simple filter plugin, we recommend to follow all the steps in [How to write a Java filter plugin][logstash-java-plugin-dev] tutorial.

### Build plugin GEM
To build this filter project into a GEM that can be installed onto Logstash, run 

    $ ./gradlew.unix gem --info

Sometimes, especially after major changes, clean the artifacts before you run the build gem task:

    $ ./gradlew.unix clean

### Install
To install this plugin on your local developer machine with Logstash installed, run:
    
    $ ~/Downloads/logstash-7.14/bin/logstash-plugin install ./logstash-filter-documentdb_guardium_filter-?.?.?.gem

**Notes:** 
* Replace "?" with this plugin version
* logstash-plugin may not handle relative paths well, so try to install the gem from a simple path, as in the example above. 

### Run on local Logstash
To test your filter using your local Logstash installation, run 

    $ ~/Downloads/logstash-7.14/bin/logstash -f docdb-test.conf
    
This configuration file generates an Event and send it thru the installed filter plugin.