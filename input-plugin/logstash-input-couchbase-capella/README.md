# Couchbase Capella-Guardium Logstash input plug-in

### Meet Couchbase Capella
* Tested versions: 1.0.0
* Developed by IBM
* Configuration instructions can be found on [Guardium Couchbase Capella documentation](../../input-plugin/logstash-input-couchbase-capella/README.md#setup-couchbase-capella-cluster)
* Supported Guardium versions: Guardium Data Protection: 12.0 or later

This is a java [Logstash](https://github.com/elastic/logstash) input plug-in for the universal connector that is featured in IBM Security Guardium. It reads events and messages from the Mongo Atlas audit log into a [Guardium record](https://github.com/IBM/universal-connectors/blob/main/common/src/main/java/com/ibm/guardium/universalconnector/commons/structures/Record.java) instance, which is a standard structure made out of several parts. Then the information is sent to Guardium. Guardium records include the accessor (the person who tries to access the data), the session, data, and exceptions. If there are no errors, the data contains details about the query `construct`. The construct details the main action (verb) and collections (objects) involved.

## Setting up a Couchbase Capella Cluster
1. Login to Capella by using https://cloud.couchbase.com.
2. Click **Create Cluster**.
3. Select **My First Project** as the project for your cluster.
4. Select a **Cluster Option**.
5. In the **Name** field, enter a name for your cluster or accept the default option.
6. Select one of the available cloud service providers: **AWS**, **Google Cloud**, or **Azure**.
7. Select an available geographic region for your cluster.
8. Enter a CIDR block for your cluster, or accept the default. For more information about configuring a CIDR block, see https://docs.couchbase.com/cloud/clusters/databases.html#cloud-provider.
9. Click **Create Cluster** to deploy your free tier operational cluster with Capella.

## Obtaining an access bearer token
1. Login to Capella by using https://cloud.couchbase.com.
2. Click **Settings** > **API Keys** > **Generate Key**.
3. Enter a **Key Name**. Then select one of the organization roles.
4. Click **Generate key**.

   **Note:** You can also create an API Key through the endpoint. For more information, see [Create API Key](https://docs.couchbase.com/cloud/management-api-reference/index.html#tag/Api-Keys/operation/postOrganizationAPIKeys).

## Setting up Couchbase Capella cluster auditing
1. Go to **Security** > **Audit** tab.
2. Turn on the Audit events & write them to a log toggle.
   For more information, [Managing Auditing](https://docs.couchbase.com/server/current/manage/manage-security/manage-auditing.html).

## Configuring the input Capella plugin in Guardium

### Before you begin
* Configure the policies you need. For more information, see [Policies](/docs/#policies).
* You must have permissions for the S-Tap Management role. By default, the admin user is assigned the S-Tap Management role.
* Download the [logstash-input-couchbase_capella_input](logstash-input-couchbase_capella_input.zip) plug-in.

### Procedure
1. On the collector, go to **Setup** > **Tools and Views** > **Configure Universal Connector**.
2. Enable the universal connector if it is disabled.
3. Click **Upload File** and select the offline [logstash-input-couchbase_capella_input](logstash-input-couchbase_capella_input.zip) plug-in. After it is uploaded, click **OK**.
4. Click the **Plus** icon to open the Connector Configuration dialog box.
5. In the **Connector name** field, enter a name.
6. Update the input section to add the details from the [capellaCouchbase.conf](../../filter-plugin/logstash-filter-capella-guardium/capellaCouchbaseOverCapellaPackage/capellaCouchbase.conf) file's ``input`` section, omitting the keyword ``input{`` at the beginning and its corresponding ``}`` at the end.
7. Update the filter section to add the details from the [capellaCouchbase.conf](../../filter-plugin/logstash-filter-capella-guardium/capellaCouchbaseOverCapellaPackage/capellaCouchbase.conf) file's ``filter`` section, omitting the keyword ``filter{`` at the beginning and its corresponding ``}`` at the end.
8. Make sure that the ``type`` fields in the ``input`` and ``filter`` configuration sections align. This field must be unique for each connector added to the system.
9. Click **Save**. Guardium validates the new connector and displays it in the Configure Universal Connector page.
10. After the offline plug-in is installed and the configuration is uploaded and saved in the Guardium machine, restart the universal connector by using the **Disable/Enable** button.

##  Limitations
* No more than three historical export requests are permitted over 24-hour period.
* * The following fields are not found in original audit log from Capella: Database name, Service Name.

Notes:
* It may take approximately 30 minutes for data to appear in the Full SQL report.

## Usage

### Parameters
| Parameter      | Input Type | Required | Default      |
|----------------|------------|----------|--------------|
| query-interval | number     | Yes      | 8*3600       |
| query_length    | number     | Yes      |   3600       |
| api_base_url    | string     | Yes      |https://cloudapi.cloud.couchbase.com/v4          |
| organization_id | string     | Yes      |              |
| project_id       | string     | Yes      |              |
| cluster_id       | string     | Yes      |  |
| auth_token       | string     | Yes      |  |

## Couchbase Capella cluster audit log events

### Sample log

```{
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
```

For more information about supported audit messages, see [Audit Event Reference](https://docs.couchbase.com/server/current/audit-event-reference/audit-event-reference.html). </p>

**Tip:** In the configuration file, `query_interval` and `query_length` have no restrictions, and both fields are set to **1 hour** by default. To improve resource efficiency, use shorter intervals as larger intervals may result in unnecessary waiting time before the next cycle.
