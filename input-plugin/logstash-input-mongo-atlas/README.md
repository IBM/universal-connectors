# Mongo Atlas-Guardium Logstash input plug-in
### Meet Mongo Atlas
* Tested versions: 1.0.1
* Developed by IBM
* Configuration instructions can be found on [Guardium Mongo Atlas documentation](https://github.com/IBM/universal-connectors/blob/main/input-plugin/logstash-input-mongo-atlas/README.md)
* Supported Guardium versions:
    * Guardium Data Protection: 11.4 and above
    * Guardium Data Security Center: coming soon

This is a [Logstash](https://github.com/elastic/logstash) input plug-in for the universal connector that is featured in IBM Security Guardium. It reads events and messages from the Mongo Atlas audit log into a [Guardium record](https://github.com/IBM/universal-connectors/blob/main/common/src/main/java/com/ibm/guardium/universalconnector/commons/structures/Record.java) instance (which is a standard structure made out of several parts). The information is then sent over to Guardium. Guardium records include the accessor (the person who tried to access the data), the session, data, and exceptions. If there are no errors, the data contains details about the query "construct". The construct details the main action (verb) and collections (objects) involved.

In order to support a few features one zip has to be added with the name "guardium_logstash-offline-plugins-mongo-atlas.zip".

##  Steps for cluster creation in Mongo Atlas.
1. Login to Atlas using https://cloud.mongodb.com/.
2. Click ```Build a cluster```.
3. If ```Build a cluster``` option is unavailable, Select the ```create``` option in the top right corner.
4. Select Dedicated Cluster.
5. Select your preferred Cloud Provider & Region
6. Select your preferred Cluster Tier.
7. Enter a name for your cluster in the Cluster Name field.
8. Click ```Create Cluster``` to deploy the cluster.
   Now that your cluster is provisioned.
   For more information https://www.mongodb.com/docs/atlas/tutorial/create-new-cluster/.


##  Steps to create API user.
1. Click on ```Database Access``` option from ```SECURITY``` menu.
2. Click on ```ADD NEW DATABASE USER``` option in the top right corner.
3. Create username/password for Authentication And provide built-in role for user from drop-down list.
4. Click on ```Add user```.
   Your API user is created successfully.


##  Create an API Key And Provide Network access.
1. Navigate to the ```Access Manager``` page for your organization.
2. Click ```Create API Key```.
3. Enter the API Key Information.
   a.Enter a Description.
   b.In the Organization Permissions menu, select the new role or roles for the API key.
4. Click ```Next```.
5. Copy and save the Public Key.
6. Copy and save the Private Key.
7. Add an API Access List Entry.
   a.Click ```Add Access list Entry```.
   b.Enter an IP address from which you want Atlas to accept API requests for this API Key.
   c.Click ```Save```.
8. Click ```Done```.
9. In the Security section of the left navigation, click on Network Access.
10. Click on ```ADD IP ADDRESS``` button.
11. Add IP address and and click on ```Confirm```.
    For more information, https://www.mongodb.com/docs/atlas/configure-api-access/#add-an-api-access-list-entry.

**Note**:
If no traffic is observed and the API key configured properly, revalidate the IP in the allowed access list by removing and adding it again, and recreate the UC connection.


##  Setup Database Auditing.
1. In the ```Security``` section of the left navigation, click ```Advanced```.
2. Toggle the button next to Database Auditing to On.
3. Click ```Save```.
   For more information, https://www.mongodb.com/docs/atlas/database-auditing/.

##  Audit filter criteria on MongoDB
1. In the Security section of the left navigation, click ```Advanced```.
2. Click ```Audit Filter Settings``` next to Database Auditing.
3. Paste this text and click ```Save```
```
{ "atype": { "$in": [ "authCheck", "authenticate" ] } }
```

##  Configuring the Input Mongo Atlas plugin in Guardium
### Before you begin
• You must have permissions for the S-TAP Management role. The admin user includes this role by default.
## Authorizing outgoing traffic from Mongo atlas to Guardium
1. Log in to the Guardium API.
2. Issue the following command:
```
grdapi add_domain_to_universal_connector_allowed_domains domain=cloud.mongodb.com
```

## Procedure
1. On the collector, go to ```Setup``` > ```Tools and Views``` > ```Configure Universal Connector```.
2. Enable the Guardium Universal Connector if is in disabled state before uploading the UC plug-in.
3. Click ```Upload File``` and select the offline [logstash-input-mongo_atlas_input.zip](https://github.com/IBM/universal-connectors/releases/tag/main_dev/logstash-input-mongo_atlas_input.zip) plug-in. After it is uploaded, click ```OK```.
4. Click the Plus sign to open the Connector Configuration dialog box.
5. Type a name in the ```Connector name``` field.
6. Update the input section to add the details from the [input-mongo-atlas.conf](https://github.com/IBM/universal-connectors/blob/main/input-plugin/logstash-input-mongo-atlas/input-mongo-atlas.conf) file input section, omitting the keyword "input{" at the beginning and its corresponding "}" at the end.
7. Update the filter section to add the details from the [input-mongo-atlas.conf](https://github.com/IBM/universal-connectors/blob/main/input-plugin/logstash-input-mongo-atlas/input-mongo-atlas.conf) file filter section, omitting the keyword "filter{" at the beginning and its corresponding "}" at the end.
8. Make sure that the ```type``` filed matches in both the input and filter configuration section. This field must be unique for every individual connector that you add.
9. Click ```Save```. Guardium validates the new connector, and enables the universal connector if it was disabled. After it is validated, it appears in the Configure Universal Connector page.

## Usage


### Parameters
| Parameter | Input Type | Required | Default |
|-----------|------------|----------|---------|
| interval | number | Yes | | 300
| public-key | string | No | |
| private-key | string | No | |
| group-id | string | No | |
| hostname | string | No | |
| filename | string | No | `mongodb-audit-log.gz` |

### Example
### mongo  event
```
{ "atype" : "authCheck", "ts" : { "$date" : "2022-07-03T10:05:49.906+00:00" }, "uuid" : { "$binary" : "Y2etnPUqSgayglUyJEIhAg==", "$type" : "04" }, "local" : { "ip" : "192.168.240.160", "port" : 27017 }, "remote" : { "ip" : "192.168.240.160", "port" : 35154 }, "users" : [ { "user" : "mms-automation", "db" : "admin" } ], "roles" : [ { "role" : "restore", "db" : "admin" }, { "role" : "userAdminAnyDatabase", "db" : "admin" }, { "role" : "dbAdminAnyDatabase", "db" : "admin" }, { "role" : "backup", "db" : "admin" }, { "role" : "readWriteAnyDatabase", "db" : "admin" }, { "role" : "clusterAdmin", "db" : "admin" } ], "param" : { "command" : "find", "ns" : "local.clustermanager", "args" : { "find" : "clustermanager", "filter" : {}, "limit" : { "$numberLong" : "1" }, "singleBatch" : true, "sort" : {}, "lsid" : { "id" : { "$binary" : "ij8ekCWaRSmm4kJmrXMVrA==", "$type" : "04" } }, "$clusterTime" : { "clusterTime" : { "$timestamp" : { "t" : 1656842749, "i" : 1 } }, "signature" : { "hash" : { "$binary" : "DO6s7IXc/4pDtTLFqcVl58O/uaI=", "$type" : "00" }, "keyId" : { "$numberLong" : "7109460176817618948" } } }, "$db" : "local", "$readPreference" : { "mode" : "primaryPreferred" } } }, "result" : 0 }
```

## Supported audit messages & commands
* authCheck:
    * find, insert, delete, update, create, drop, etc.
    * aggregate with $lookup(s) or $graphLookup(s)
    * applyOps: An internal command that can be triggered manually to create or drop collection. The command object is written as "\[json-object\]" in Guardium. Details are included in the Guardium Full SQL field, if available.
* authenticate (with error only)

Notes:
* To make sure that events are handled properly, take the following steps:
    * Set MongoDB access control, because messages with no users are removed.
    * Do not filter `authcheck` and `authenticate` events out of the MongoDB audit log messages.
* Other MongoDB events and messages are removed from the pipeline, since their data is already parsed in the authCheck message.
* Non-MongoDB events are skipped, but not removed from the pipeline, since they may be used by other filter plug-ins.

##  Supported errors

* Authentication error (18) – A failed login error.
* Authorization error (13) - To see the "Unauthorized ..." description in Guardium, you must extend the report and add the "Exception description" field.

The filter plug-in also supports sending errors. For this, MongoDB access control must be configured before the events will be logged. For example, edit _/etc/mongod.conf_ so that it includes:

    security:  
        authorization: enabled

##  Limitations
* `Client Host Name` is not supported. For system-generated queries, 'Server Host Name' and 'Client Host Name' are the same.
* IPv6 addresses are typically supported by the MongoDB and filter plug-ins. However, IPV6 is not fully supported by the Guardium pipeline.
* `Source Program` is left blank.
* Mentioning 'Audit filter criteria on MongoDB' captures all of the events. Set the audit filter criteria as needed to avoid unnecessary logs.
