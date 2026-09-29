# Configuring MongoDB Atlas datasource profiles for Kafka Connect plug-ins

Create and configure datasource profiles through Central Manager for **MongoDB Atlas over Mongo Atlas Kafka Connect** plug-ins.

## Meet MongoDB Atlas over MongoDB Atlas Kafka Connect

* Tested versions: 8.0.29
* **Environment:** MongoDB Atlas (cloud)
* **Supported inputs:** Kafka connect Mongo Atlas 2.0 (pull)
* **Supported Guardium versions:**
    * Guardium Data Protection: Appliance bundle 12.2.4 or later

Kafka-connect is a framework for streaming data between Apache Kafka and other systems. This connector enables monitoring of MongoDB Atlas audit logs by pulling them through the MongoDB Atlas API.

## Configuring the MongoDB Atlas Service

Configure your MongoDB Atlas environment by completing the steps below to set up a cluster, create an API user, and enable audit logging.

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
   b.In the Organization Permissions menu, select the new role or roles for the API key. Minimum permission: ```Project Data Access Read Only``` (For more information, https://www.mongodb.com/docs/atlas/reference/user-roles/#mongodb-authrole-Project-Data-Access-Read-Only).
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

## Example
### Mongo Event
```
{ "atype" : "authCheck", "ts" : { "$date" : "2022-01-01T00:00:00.000+00:00" }, "uuid" : { "$binary" : "AAAAAAAAAAAAAAAAAAAAAA==", "$type" : "04" }, "local" : { "ip" : "1.2.3.4", "port" : 27017 }, "remote" : { "ip" : "1.2.3.4", "port" : 12345 }, "users" : [ { "user" : "myUser", "db" : "admin" } ], "roles" : [ { "role" : "restore", "db" : "admin" }, { "role" : "userAdminAnyDatabase", "db" : "admin" }, { "role" : "dbAdminAnyDatabase", "db" : "admin" }, { "role" : "backup", "db" : "admin" }, { "role" : "readWriteAnyDatabase", "db" : "admin" }, { "role" : "clusterAdmin", "db" : "admin" } ], "param" : { "command" : "find", "ns" : "myDatabase.myCollection", "args" : { "find" : "myCollection", "filter" : {}, "limit" : { "$numberLong" : "1" }, "singleBatch" : true, "sort" : {}, "lsid" : { "id" : { "$binary" : "AAAAAAAAAAAAAAAAAAAAAA==", "$type" : "04" } }, "$clusterTime" : { "clusterTime" : { "$timestamp" : { "t" : 1000000000, "i" : 1 } }, "signature" : { "hash" : { "$binary" : "AAAAAAAAAAAAAAAAAAAAAA==", "$type" : "00" }, "keyId" : { "$numberLong" : "0" } } }, "$db" : "myDatabase", "$readPreference" : { "mode" : "primaryPreferred" } } }, "result" : 0 }
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

## Limitations

* `Client Host Name` is not supported. For system-generated queries, 'Server Host Name' and 'Client Host Name' are the same.
* IPv6 addresses are typically supported by the MongoDB and filter plug-ins. However, IPV6 is not fully supported by the Guardium pipeline.
* `Source Program` is not available in the Atlas audit log stream; it is hardcoded to `mongod`.
* Mentioning 'Audit filter criteria on MongoDB' captures all of the events. Set the audit filter criteria as needed to avoid unnecessary logs.

## Creating datasource profiles

You can create a new datasource profile from the **Datasource Profile Management** page.

### Procedure

1. Go to **Manage > Universal Connector > Datasource Profile Management**.
2. Click the **➕ (Add)** button.
3. Create a profile by using one of the following methods:

    * To **Create a new profile manually**, go to the **"Add Profile"** tab and provide values for the following fields:
        * **Name** and **Description**.
        * Select a **Plug-in Type** from the dropdown. For example, `MongoDB Over Mongo Atlas Connect 2.0`.

    * To **Upload from CSV**, go to the **"Upload from CSV"** tab and upload an exported or manually created CSV file containing one or more profiles. You can also choose from the following options:
        * **Update existing profiles on name match** — Updates profiles with the same name if they already exist.
        * **Test connection for imported profiles** — Automatically tests connections after profiles are created.
        * **Use ELB** — Enables ELB support for imported profiles. You must provide the number of MUs to use in the ELB process.

**Note:** Configuration options vary based on the selected plug-in.

## Configuring MongoDB Atlas over Mongo Atlas Kafka Connect 2.0

The following table describes the fields that are specific to the MongoDB Atlas Kafka Connect 2.0 plug-in.

| Field | Description |
|---|---|
| **Name** | Unique name of the profile. |
| **Description** | Description of the profile. |
| **Plug-in** | Plug-in type for this profile. Select `MongoDB Over Mongo Atlas Connect 2.0`. A full list of available plug-ins is available on the **Package Management** page. |
| **Credential** | The credential to authenticate with the MongoDB Atlas API. Must be created in **Credential Management**, or click **➕** to create one. For more information, see [Creating Credentials](https://www.ibm.com/docs/en/SSMPHH_12.x/com.ibm.guardium.doc.stap/guc/guc_credential_management.html). |
| **Kafka Cluster** | Select the appropriate Kafka cluster from the available Kafka cluster list, or create a new Kafka cluster. For more information, see [Managing Kafka clusters](https://www.ibm.com/docs/en/SSMPHH_12.x/com.ibm.guardium.doc.stap/guc/guc_kafka_cluster_management.html). |
| **Label** | Grouping label. For example, customer name or ID. |
| **Group ID** | The MongoDB Atlas project (group) ID. |
| **Hostname** | The hostname of one of your Atlas cluster nodes. |
| **MongoDB Atlas API Base URL** | Base URL for the MongoDB Atlas API. Default: `https://cloud.mongodb.com/api/atlas/v1.0/groups/`. For deployments on custom domains or private cloud installations, specify the appropriate URL. |
| **Poll interval (seconds)** | The interval at which the connector polls the Atlas API for new audit log entries. Default: `300`. |
| **No-traffic threshold (minutes)** | Default value is 60. If there is no incoming traffic for an hour, S-TAP displays a red status. Once incoming traffic resumes, the status returns to green. |
| **Use Enterprise Load Balancing (ELB)** | Enable this if ELB support is required. |
| **Managed Unit Count** | Number of Managed Units (MUs) to allocate for ELB. |

**Note:**

- Ensure that the **profile name** is unique.
- Required credentials must be created before or during profile creation.
- The Mongo Atlas API key must have at minimum **Project Data Access Read Only** permissions.

---

## MongoDB Atlas Credential Configuration

When creating credentials for the MongoDB Atlas API, provide the following information.

| Field name | Description |
|---|---|
| **Name** | A unique credential name. |
| **Description** | A description for your credential. |
| **Credential Type** | `Mongo Atlas API Key` |
| **Public Key** | The public key obtained from the Mongo Atlas API Key creation page. |
| **Private Key** | The private key obtained from the Mongo Atlas API Key creation page. |

---

## Testing a Connection

After creating a profile, test the connection to ensure that the provided configuration is valid.

### Procedure

1. Select the new profile.
2. From the top menu, click **Test Connection**.
3. If the test is successful, you can proceed to installing the profile.

---

## Installing a Profile

After the connection test is successful, you can install the profile on **Managed Units (MUs)** or **Edges**. The parsed audit logs are sent to the selected Managed Unit or Edge to be consumed by the **Sniffer**.

### Procedure

1. Select the profile.
2. From the **Install** menu, click **Install**.
3. From the list of available MUs and Edges that is displayed, select the ones that you want to deploy the profile to.

---

## Uninstalling or reinstalling profiles

An installed profile can be uninstalled or reinstalled if needed.

### Procedure

1. Select the profile.
2. From the list of available actions, select the desired option: **Uninstall** or **Reinstall**.

---
