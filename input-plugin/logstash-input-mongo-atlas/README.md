# Mongo Atlas-Guardium Logstash input plug-in
### Meet Mongo Atlas
* Tested versions: 1.0.1
* Developed by IBM
* Configuration instructions can be found on [Guardium Mongo Atlas documentation](https://github.com/IBM/universal-connectors/blob/main/input-plugin/logstash-input-mongo-atlas/README.md)
* Supported Guardium versions:
    * Guardium Data Protection: 11.4 and above
    * Guardium Insights: coming soon

This is a [Logstash](https://github.com/elastic/logstash) input plug-in for the 
universal connector that is featured in IBM Security Guardium. It reads events 
and messages from the Mongo Atlas audit log 
into a [Guardium record](https://github.com/IBM/universal-connectors/blob/main/common/src/main/java/com/ibm/guardium/universalconnector/commons/structures/Record.java) instance 
(which is a standard structure made out of several parts). The information is then 
sent over to Guardium. Guardium records include the accessor (the person who tried 
to access the data), the session, data, and exceptions. If there are no errors, the 
data contains details about the query "construct". The construct details the main 
action (verb) and collections (objects) involved.

In order to support a few features one zip has to be added with the name "guardium_logstash-offline-plugins-mongo-atlas.zip".

##  Steps for cluster creation in Mongo Atlas.
1. Login to Atlas using https://cloud.mongodb.com/.
2. Click 'Build a cluster'.
3. If 'Build a cluster' option is unavailable,
   Select **Database** from the menu at left and
   Select the **Create** option in the top right corner.
4. Select Dedicated Cluster.
5. Select your preferred Cloud Provider & Region
6. Select your preferred Cluster Tier.
7. Enter a name for your cluster in the Cluster Name field.
8. Click Create Cluster to deploy the cluster.
   Now that your cluster is provisioned.
   For more information https://www.mongodb.com/docs/atlas/tutorial/create-new-cluster/.


##  Steps to create API user.
1. Click on **Database Access** option from **Security** menu.
2. Click on **Add New Database User** option in the top right corner.
3. Create username/password for Authentication And provide built-in role for user from drop-down list.
4. Click on Add user.
   Your API user is created successfully.


##  Create an API Key And Provide Network access.
1. Navigate to the Access Manager page for your organization.
2. Select **Projects** from the menu at left.
3. Select a Project from the list.
4. Click on three dots adjacent to the Project selection menu
   at top left corner just below to the Organization menu.
5. Select the **Project Settings**.
6. Select **Access Manager** from the menu at left side.
7. Click **Create API Key**.
8. Enter the API Key Information.
    1. Enter a Description.
    2. In the Project Permissions menu, select the role for the API key.
9. Click **Next**.
10. Copy and save the **Public Key**.
11. Copy and save the **Private Key**.
12. Click **Add Access list Entry**.
    1. Enter an IP address from which you want Atlas to
       accept API requests for this API Key. You can add the current IP address
       just by clicking **Use Current IP address**.
    2. Click **Save**.
13. Click **Done**.
14. Select the project from the top left corner
    list below to the Organizations list.
15. In the Security section of the left navigation, click on **Network Access**.
16. Click on **Add IP Address** button.
17. Add IP address and click on **Confirm**.

For more information, https://www.mongodb.com/docs/atlas/configure-api-access/#add-an-api-access-list-entry. 


##  Setup Database Auditing.
1. In the **Security** section of the left navigation, click **Advanced**.
2. Toggle the button next to **Database Auditing** to On.
3. Click **Save**.
   For more information, https://www.mongodb.com/docs/atlas/database-auditing/.

##  Audit filter criteria on MongoDB
1. In the **Security** section of the left navigation, click **Advanced**.
2. Click **Edit** just below to **Database Auditing** button.
3. Paste below text and click **Save**.
```json
 {
  "$and": [
    {
      "atype": {
        "$nin": [
          "clientMetadata"
        ]
      }
    },
    {
      "users.user": {
        "$nin": [
          "mms-automation",
          "mms-monitoring-agent",
          "__system"
        ]
      }
    },
    {
      "param.initialUsers.user": {
        "$nin": [
          "mms-automation",
          "mms-monitoring-agent",
          "__system"
        ]
      }
    },
    {
      "param.ns": {
        "$not": {
          "$regex": "^admin"
        }
      }
    },
    {
      "param.ns": {
        "$not": {
          "$regex": "^local"
        }
      }
    }
  ]
}
```

##  Configuring the Input Mongo Atlas plugin in Guardium
### Before you begin
  • You must have permissions for the S-TAP Management role. The admin user includes this role by default.
## Authorizing outgoing traffic from Mongo atlas to Guardium
  1. Log in to the Guardium API.
  2. Issue these command:
    • grdapi add_domain_to_universal_connector_allowed_domains domain=cloud.mongodb.com
## Procedure
  1. On the collector, go to Setup > Tools and Views > Configure Universal Connector.
  2. Enable the Guardium Universal Connector if is in disabled state before uploading the UC plug-in.
  3. Click Upload File and select the offline [logstash-input-mongo_atlas_input.zip](https://github.com/IBM/universal-connectors/releases/download/v1.5.0/logstash-input-mongo_atlas_input.zip) plug-in. After it is uploaded, click OK.This is not necessary for Guardium Data Protection v12.0 and later.
  4. Click the Plus sign to open the Connector Configuration dialog box.
  5. Type a name in the Connector name field.
  6. Update the input section to add the details from the https://github.com/IBM/universal-connectors/blob/main/input-plugin/logstash-input-mongo-atlas/input-mongo-atlas.conf file input section, omitting the keyword "input{" at the beginning and its corresponding "}" at the end.
  7. Update the filter section to add the details from the https://github.com/IBM/universal-connectors/blob/main/input-plugin/logstash-input-mongo-atlas/input-mongo-atlas.conf file filter section, omitting the keyword "filter{" at the beginning and its corresponding "}" at the end.
  8. 'type' field should match in input and filter configuration section.This field should be unique for every individual connector added.
  9. Click Save. Guardium validates the new connector, and enables the universal connector if it was disabled. After it is validated, it appears in the Configure Universal Connector page.

## Usage


### Parameters
| Parameter   | Input Type | Required | Default                |
|-------------|------------|----------|------------------------|
| interval    | number     | Yes      | 300                    | 
| public-key  | string     | Yes      |                        |
| private-key | string     | Yes      |                        |
| group-id    | string     | Yes      |                        |
| hostname    | string     | Yes      |                        |
| filename    | string     | No       | `mongodb-audit-log.gz` |

* **group-id**: GroupId is equivalent to the project ID. To find it,
    1. Go to your Organization.
    2. Select **Projects** from the left menu.
    3. Select the required project.
    4. Click on the three dots right by the name of the selected project in the
       top left corner.
    5. Select **Project Settings**.
    6. Copy the **Project ID** displayed on the page.

* **hostname**: Hostname is equivalent to the cluster hostname. To find it,
    1. Click **All Clusters** in the top right corner.
    2. Select the cluster from the list.
    3. There will be a panel named **Region**.
    4. Select any shard among the listed inside the region panel.
    5. You will land to the **Status** page.
    6. The label of the title of the page will be in the format of <hostname>:<port>.
    7. Take the host name. It will be in the format of,
       `<cluster-name>-<shard-seq-seq>.<identifier>.mongodb.net`. For example,
       `cluster2-shard-00-01.i2jq9.mongodb.net`

### Example
#### Mongo  event
```json
  {
  "atype": "authCheck",
  "ts": {
    "$date": "2022-07-03T10:05:49.906+00:00"
  },
  "uuid": {
    "$binary": "Y2etnPUqSgayglUyJEIhAg==",
    "$type": "04"
  },
  "local": {
    "ip": "192.168.240.160",
    "port": 27017
  },
  "remote": {
    "ip": "192.168.240.160",
    "port": 35154
  },
  "users": [
    {
      "user": "mms-automation",
      "db": "admin"
    }
  ],
  "roles": [
    {
      "role": "restore",
      "db": "admin"
    },
    {
      "role": "userAdminAnyDatabase",
      "db": "admin"
    },
    {
      "role": "dbAdminAnyDatabase",
      "db": "admin"
    },
    {
      "role": "backup",
      "db": "admin"
    },
    {
      "role": "readWriteAnyDatabase",
      "db": "admin"
    },
    {
      "role": "clusterAdmin",
      "db": "admin"
    }
  ],
  "param": {
    "command": "find",
    "ns": "local.clustermanager",
    "args": {
      "find": "clustermanager",
      "filter": {},
      "limit": {
        "$numberLong": "1"
      },
      "singleBatch": true,
      "sort": {},
      "lsid": {
        "id": {
          "$binary": "ij8ekCWaRSmm4kJmrXMVrA==",
          "$type": "04"
        }
      },
      "$clusterTime": {
        "clusterTime": {
          "$timestamp": {
            "t": 1656842749,
            "i": 1
          }
        },
        "signature": {
          "hash": {
            "$binary": "DO6s7IXc/4pDtTLFqcVl58O/uaI=",
            "$type": "00"
          },
          "keyId": {
            "$numberLong": "7109460176817618948"
          }
        }
      },
      "$db": "local",
      "$readPreference": {
        "mode": "primaryPreferred"
      }
    }
  },
  "result": 0
}
```

## Supported audit messages & commands
* authCheck: 
    * find, insert, delete, update, create, drop, etc.
    * aggregate with $lookup(s) or $graphLookup(s)
    * applyOps: An internal command that can be triggered manually to create or drop collection. The command object is written as "\[json-object\]" in Guardium. Details are included in the Guardium Full SQL field, if available. 
* authenticate (with error only) 

Notes: 
* For the events to be handled propertly: 
    * MongoDB access control must be set, as messages without users are removed. 
    * authCheck and authenticate events should not be filtered out of the MongoDB audit log messages.
* Other MongoDB events and messages are removed from the pipeline, since their data is already parsed in the authCheck message.
* Non-MongoDB events are skipped, but not removed from the pipeline, since they may be used by other filter plug-ins.

##  Supported errors

* Authentication error (18) – A failed login error.
* Authorization error (13) - To see the "Unauthorized ..." description in Guardium, you must extend the report and add the "Exception description" field.

The filter plug-in also supports sending errors. For this, MongoDB access control must be configured before the events will be logged. For example, edit _/etc/mongod.conf_ so that it includes:

    security:  
        authorization: enabled

##  Limitations
* There is no support for client hostname
* IPv6 addresses are typically supported by the MongoDB and filter plug-ins, however this is not fully supported by the Guardium pipeline.
* 'Source Program' is left blank.
* For system generated queries, 'Server Host Name' & 'Client Host Name' are seen same.
* Mentioned 'Audit filter criteria on MongoDB' captures all the events. Customer should set it as per their need to avoid unnecessary logs.
