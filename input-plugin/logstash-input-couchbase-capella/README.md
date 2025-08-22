# Couchbase Capella-Guardium Logstash input plug-in

### Meet Couchbase Capella
* Tested versions: 1.0.0
* Developed by IBM
* Configuration instructions can be found on [Guardium Couchbase Capella documentation](../../input-plugin/logstash-input-couchbase-capella/README.md#setup-couchbase-capella-cluster)
* Supported Guardium versions:
    * Guardium Data Protection: 12.0 and above

This is a java [Logstash](https://github.com/elastic/logstash) input plug-in for the universal connector that is featured in IBM Security Guardium. It reads events and messages from the Mongo Atlas audit log into a [Guardium record](https://github.com/IBM/universal-connectors/blob/main/common/src/main/java/com/ibm/guardium/universalconnector/commons/structures/Record.java) instance (which is a standard structure made out of several parts). The information is then sent over to Guardium. Guardium records include the accessor (the person who tried to access the data), the session, data, and exceptions. If there are no errors, the data contains details about the query "construct". The construct details the main action (verb) and collections (objects) involved.

# Setup Couchbase Capella Cluster
1. Login to Capella using https://cloud.couchbase.com.
2. Click ```Create Cluster```.
3. Select My First Project as the project for your cluster.
4. Select one of the ```Cluster Option```.
5. In the Name field, enter a name for your cluster or accept the default option.
6. Select one of the available cloud service providers: ```AWS```, ```Google Cloud```, ```Azure```.
7. Select an available geographic region for your cluster.
8. Enter a CIDR Block for your cluster, or accept the default. For more information about how to configure a CIDR block, see https://docs.couchbase.com/cloud/clusters/databases.html#cloud-provider
9. Click ```Create Cluster``` to deploy your free tier operational cluster with Capella.

# Get Access Bearer Token
1. Login to Capella using https://cloud.couchbase.com.
2. Select ```Settings```
3. Select ```API Keys```
4. Select ```Generate Key``` on the upper left side.
5. Enter ```Key Name```, select one of the Organization Roles
6. Click ```Generate key```
Note: You can also create API Key through the endpoint, the detail information see https://docs.couchbase.com/cloud/management-api-reference/index.html

# Setup Couchbase Capella Cluster Auditing
1. Open the Audit tab of the Security settings by selecting Security -> Audit.
2. Turn on the Audit events & write them to a log toggle.
3. For more information, 
https://docs.couchbase.com/server/current/manage/manage-security/manage-auditing.html

# Configuring the Input Capella plugin in Guardium
### Before you begin
* Configure the policies you require. See [policies](/docs/#policies) for more information.
* You must have permission for the S-Tap Management role. The admin user includes this role by default
* Download the [logstash-input-couchbase_capella_input](logstash-input-couchbase_capella_input.zip) plug-in.

### Procedure
1. On the collector, go to ```Setup``` > ```Tools and Views``` > ```Configure Universal Connector```.
2. Enable the universal connector if it is disabled.
3. Click ```Upload File``` and select the offline  [logstash-input-couchbase_capella_input](logstash-input-couchbase_capella_input.zip) plug-in. After it is uploaded, click ```OK```.
4. Click the Plus sign to open the Connector Configuration dialog box.
5. Type a name in the Connector name field.
6. Update the input section to add the details from the [capellaCouchbase.conf](../../filter-plugin/logstash-filter-capella-guardium/CapellaCouchbaseOverCapellaPackage/capellaCouchbase.conf) file's input part, omitting the keyword "input{" at the beginning and its corresponding "}" at the end.
7. Update the filter section to add the details from the [capellaCouchbase.conf](../../filter-plugin/logstash-filter-capella-guardium/CapellaCouchbaseOverCapellaPackage/capellaCouchbase.conf) file's filter part, omitting the keyword "filter{" at the beginning and its corresponding "}" at the end.
8. The 'type' fields should match in the input and filter configuration sections. This field should be unique for every individual connector added.
9. Click ```Save```. Guardium validates the new connector and displays it in the Configure Universal Connector page.
10. After the offline plug-in is installed and the configuration is uploaded and saved in the Guardium machine, restart the Universal Connector using the ```Disable/Enable``` button.

##  Limitations
* No more than three historical export requests are permitted over 24-hour period.

Notes:
* It may take approximately 30 minutes for data to appear in the Full SQL report.
* The original Capella audit log contains no values for the following fields: Database Name, Service Name.

# Usage
### Parameters
| Parameter      | Input Type | Required | Default      |
|----------------|------------|----------|--------------|
| query-interval | number     | Yes      | 8*3600       |
| query_length    | number     | Yes      |   3600       |
| api_base_url    | string     | Yes      |`https://cloudapi.cloud.couchbase.com/v4`  |
| organization_id | string     | Yes      |              |
| project_id       | string     | Yes      |              |
| cluster_id       | string     | Yes      |  |
| auth_token       | string     | Yes      |  |

# Couchbase Capella Cluster Audit Log Event
## Sample log:
{
"description": "Successful login to couchbase cluster",
"id": 8192,
"local": {
"ip": "10.144.210.101",
"port": 8091
},
"name": "login success",
"real_userid": {
"domain": "local",
"user": "testUser"
},
"remote": {
"ip": "10.144.210.1",
"port": 53322
},
"roles": [
"admin"
],
"sessionid": "ba2760cee506d0293a8b4a0bf83687b807329667",
"timestamp": "2021-02-09T14:44:17.938Z"
}

## Supported audit messages
For more information, reference here https://docs.couchbase.com/server/current/audit-event-reference/audit-event-reference.html

**Tip:** This plugin queries Capella audit logs based on two parameters: query_interval (how often to query) and length (time span of audit logs to fetch per query). If these values are too short, audit logs may not exist yet, resulting in 404 errors. If too long, audit files can become large and delay the job cycle. A recommended default is setting both to 1 hour to balance reliability and performance.