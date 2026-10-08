# Configuring MongoDB Atlas datasource profiles for Kafka Connect plug-ins

Create and configure datasource profiles through Central Manager for **MongoDB Atlas over Mongo Atlas Kafka Connect** plug-ins.

## Meet MongoDB Atlas over MongoDB Atlas Kafka Connect

* **Tested versions**: 8.0.29
* **Environment:** MongoDB Atlas (cloud)
* **Supported inputs:** Kafka connect Mongo Atlas 2.0 (pull)
* **Supported Guardium versions:**
    * Guardium Data Protection: Appliance bundle 12.2.4 or later

Kafka connect is a framework for streaming data between Apache Kafka and other systems. This connector enables monitoring of MongoDB Atlas audit logs by pulling them through the MongoDB Atlas API.

## Configuring the MongoDB Atlas service

Configure your MongoDB Atlas environment by completing the following steps to set up a cluster, create an API user, and enable audit logging.

###  Creating a cluster in MongoDB Atlas

1. Log in to [Atlas](https://cloud.mongodb.com/).
2. Click **Build a cluster**. If the **Build a cluster** option is unavailable, click **Create** in the upper-right corner.
3. Select **Dedicated Cluster**.
4. Select your preferred cloud provider and region.
5. Select your preferred cluster tier.
6. In the **Cluster Name** field, enter a name for the cluster.
7. Click **Create Cluster** to deploy the cluster.
   The cluster is now provisioned.

For more information, see [Create an Atlas cluster](https://www.mongodb.com/docs/atlas/tutorial/create-new-cluster/).

### Creating an API user

1. From the **SECURITY** menu, select **Database Access**.
2. Click **ADD NEW DATABASE USER** in the upper-right corner.
3. Enter a username and password for authentication and select a built-in role for the user from the drop-down list.
4. Click **Add user**.
   The API user is created.

### Creating an API Key and configuring network access

1. Go to the **Access Manager** page for your organization.
2. Click **Create API Key**.
3. Enter the API key details.
   a. Enter a Description.
   b. From the **Organization Permissions** menu, select the roles for the API key.
   The minimum required permission is **Project Data Access Read Only**. For more information, see [Atlas User Roles](https://www.mongodb.com/docs/atlas/reference/user-roles/#mongodb-authrole-Project-Data-Access-Read-Only).
4. Click **Next**.
5. Copy and save the public key.
6. Copy and save the private key.
7. Add an entry to the API access list.
   a. Click **Add Access list Entry**.
   b. Enter an IP address from which Atlas can accept API requests for this key.
   c. Click **Save**.
8. Click **Done**.
9. In the navigation pane, expand the **Security** section and select **Network Access**.
10. Click **ADD IP ADDRESS**.
11. Enter the IP address and click **Confirm**.
    For more information, see [Add an API Access List Entry](https://www.mongodb.com/docs/atlas/configure-api-access/#add-an-api-access-list-entry).

**Note**: If no traffic is detected and the API key is configured properly, remove and re-add the IP address in the access list to revalidate it, and then recreate the universal connector (UC) connection.

###  Setting up database auditing

1. In the navigation pane, expand the **Security** section and select **Advanced**.
2. Set **Database Auditing** to **On**.
3. Click **Save**.
   For more information, see [Database Auditing](https://www.mongodb.com/docs/atlas/database-auditing/).

###  Configuring audit filter criteria in MongoDB

1. In the navigation pane, expand the **Security** section and select **Advanced**.
2. Click **Audit Filter Settings** next to **Database Auditing**.
3. Paste the following configuration in the field and click **Save**.
   
```
{ "atype": { "$in": [ "authCheck", "authenticate" ] } }
```

### Example Mongo Event
```
{ "atype" : "authCheck", "ts" : { "$date" : "2022-01-01T00:00:00.000+00:00" }, "uuid" : { "$binary" : "AAAAAAAAAAAAAAAAAAAAAA==", "$type" : "04" }, "local" : { "ip" : "1.2.3.4", "port" : 27017 }, "remote" : { "ip" : "1.2.3.4", "port" : 12345 }, "users" : [ { "user" : "myUser", "db" : "admin" } ], "roles" : [ { "role" : "restore", "db" : "admin" }, { "role" : "userAdminAnyDatabase", "db" : "admin" }, { "role" : "dbAdminAnyDatabase", "db" : "admin" }, { "role" : "backup", "db" : "admin" }, { "role" : "readWriteAnyDatabase", "db" : "admin" }, { "role" : "clusterAdmin", "db" : "admin" } ], "param" : { "command" : "find", "ns" : "myDatabase.myCollection", "args" : { "find" : "myCollection", "filter" : {}, "limit" : { "$numberLong" : "1" }, "singleBatch" : true, "sort" : {}, "lsid" : { "id" : { "$binary" : "AAAAAAAAAAAAAAAAAAAAAA==", "$type" : "04" } }, "$clusterTime" : { "clusterTime" : { "$timestamp" : { "t" : 1000000000, "i" : 1 } }, "signature" : { "hash" : { "$binary" : "AAAAAAAAAAAAAAAAAAAAAA==", "$type" : "00" }, "keyId" : { "$numberLong" : "0" } } }, "$db" : "myDatabase", "$readPreference" : { "mode" : "primaryPreferred" } } }, "result" : 0 }
```

## Supported audit messages and commands

* `authCheck`:
    * Database operations, such as `find`, `insert`, `delete`, `update`, `create`, `drop`, etc.
    * `aggregate` operations with `$lookup` or `$graphLookup`.
    * `applyOps`: An internal command that can be triggered manually to create or drop a collection. The command object is recorded as `\[json-object\]` in Guardium. Details are included in the Guardium **Full SQL** field, if available.
* `authenticate` (errors only).

**Note:**
* To make sure that events are handled properly, complete the following steps:
    * Configure MongoDB access control as messages with no associated users are removed.
    * Do not filter `authCheck` and `authenticate` events out of the MongoDB audit log messages.
* Other MongoDB events and messages are removed from the pipeline as their data is already parsed in the `authCheck` message.
* Non-MongoDB events are skipped, but not removed from the pipeline, as they may be used by other filter plug-ins.

##  Supported errors

* **Authentication error (18)** – A failed login attempt.
* **Authorization error (13)** – To see the `Unauthorized ...` description in Guardium, extend the report and add the **Exception description** field.

The filter plug-in also supports forwarding errors. To log these events, you must configure MongoDB access control. For example, edit `_/etc/mongod.conf_` to include the following lines:

```
    security:
        authorization: enabled
```

## Limitations

* **Client Host Name** is not supported. For system-generated queries, **Server Host Name** and **Client Host Name** are the same.
* IPv6 addresses are typically supported by MongoDB and the filter plug-ins. However, IPv6 is not fully supported across the Guardium pipeline.
* **Source Program** is not available in the MongoDB Atlas audit log stream; the value is hardcoded to `mongod`.
* The filter criteria described in [Configuring audit filter criteria in MongoDB](#configuring-audit-filter-criteria-in-mongodb) captures all matching events. Adjust the audit filter criteria as needed to avoid logging unnecessary events.


## Creating datasource profiles

You can create a new datasource profile from the **Datasource Profile Management** page.

### Procedure

1. Go to **Manage > Universal Connector > Datasource Profile Management**.
2. Click the **➕ (Add)** button.
3. Create a profile by using one of the following methods:

    * To **Create a new profile manually**, go to the **Add Profile** tab and provide values for the following fields:
        * **Name** and **Description** — Enter a name and description.
        * **Plug-in Type** — Select a plug-in type from the dropdown. For example, `MongoDB Over Mongo Atlas Connect 2.0`.

    * To **Upload from CSV**, go to the **Upload from CSV** tab and upload an exported or manually created CSV file containing one or more profiles. You can also choose from the following options:
        * **Update existing profiles on name match** — Updates profiles with the same name if they already exist.
        * **Test connection for imported profiles** — Automatically tests connections after profiles are created.
        * **Use ELB** — Enables ELB support for imported profiles. You must provide the number of MUs to use in the ELB process.

**Note:** Configuration options vary based on the selected plug-in.

## Configuring MongoDB Atlas over MongoDB Atlas Kafka Connect 2.0

The following table describes the fields that are specific to the MongoDB Atlas Kafka Connect 2.0 plug-in.

| Field | Description |
|---|---|
| **Name** | Unique name of the profile. |
| **Description** | Description of the profile. |
| **Plug-in** | Plug-in type for this profile. Select **MongoDB Over Mongo Atlas Connect 2.0**. To view a full list of available plug-ins, go to the **Package Management** page. |
| **Credential** | The credential to authenticate with the MongoDB Atlas API. Select an existing credential in **Credential Management**, or click **➕ (Add)** to create a new credential. For more information, see [Creating credentials](https://www.ibm.com/docs/en/SSMPHH_12.x/com.ibm.guardium.doc.stap/guc/guc_credential_management.html). |
| **Kafka Cluster** | Select a Kafka cluster from the list of available Kafka clusters, or create a new Kafka cluster. For more information, see [Managing Kafka clusters](https://www.ibm.com/docs/en/SSMPHH_12.x/com.ibm.guardium.doc.stap/guc/guc_kafka_cluster_management.html). |
| **Label** | Grouping label. For example, customer name or ID. |
| **Group ID** | The MongoDB Atlas project (group) ID. |
| **Hostname** | The host name of one of your MongoDB Atlas cluster nodes. |
| **MongoDB Atlas API Base URL** | The base URL for the MongoDB Atlas API. Default: `https://cloud.mongodb.com/api/atlas/v1.0/groups/`. For deployments on custom domains or private cloud installations, specify the appropriate URL. |
| **Poll interval (seconds)** | The interval at which the connector polls the Atlas API for new audit log entries. Default: `300`. |
| **No-traffic threshold (minutes)** | The inactivity threshold, in minutes, before an alert state is triggered. Default: `60`. If no incoming traffic is detected for an hour, S-TAP displays a red status indicator. When incoming traffic resumes, the status returns to green. |
| **Use Enterprise Load Balancing (ELB)** | Enable ELB support, if required. |
| **Managed Unit Count** | Number of managed units (MUs) to allocate for ELB. |

**Note:**

- Ensure that the **Profile name** is unique.
- Required credentials must be created before or during profile creation.
- The Mongo Atlas API key must have at minimum **Project Data Access Read Only** permissions.

---

## MongoDB Atlas credential configuration

When you create credentials for the MongoDB Atlas API, specify the following information.

| Field name | Description |
|---|---|
| **Name** | A unique name for the credential. |
| **Description** | A description for the credential. |
| **Credential Type** | Mongo Atlas API Key. |
| **Public Key** | The public key generated during API key creation in MongoDB Atlas. |
| **Private Key** | The private key generated during API key creation in MongoDB Atlas. |

---

## Testing a connection

After you create a profile, test the connection to ensure that the configuration is valid.


### Procedure

1. Select the new profile.
2. From the top menu, click **Test Connection**.
3. If the test is successful, you can proceed to install the profile.

---

## Installing a profile

After the connection test is successful, you can install the profile on managed units (MUs) or Edge units. The parsed audit logs are sent to the selected managed unit or Edge units to be consumed by the **Sniffer**.

### Procedure

1. Select the profile.
2. From the **Install** menu, select **Install**.
3. From the list of available MUs and Edges displayed, select the ones where you want to deploy the profile.

---

## Uninstalling or reinstalling a profile

You can uninstall or reinstall an installed profile as needed.

### Procedure

1. Select the profile.
2. From the available actions, select **Uninstall** or **Reinstall**.

---
