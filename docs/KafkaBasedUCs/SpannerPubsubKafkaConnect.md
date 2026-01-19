# Spanner over over Pub/Sub Source Connector

This connector enables IBM Guardium Data Protection (GDP) to monitor and collect audit logs from Spanner databases
through Google Cloud Pub/Sub using Kafka Connect.

## Meet Spanner over PubSub Connect

* Environments: On-prem
* Supported inputs: Kafka Input (pull)
* Supported Guardium versions:
    * Guardium Data Protection: Appliance bundle 12.2.2 or later

Kafka-connect is a framework for streaming data between Apache Kafka and other systems.

## Configuring Spanner on GCP

1. [Create a Spanner instance](https://cloud.google.com/spanner/docs/create-manage-instances).
2. [Create a database](https://cloud.google.com/spanner/docs/create-manage-databases).
3. [Create a log sink in Pub/Sub](https://cloud.google.com/logging/docs/export/configure_export_v2#creating_sink). </br>
   To specify which logs to route, use the following inclusion filter in the Choose logs to include in sink field during
   log sink creation. This filter captures relevant data access and activity logs.

  ```
   resource.type="spanner_instance" resource.labels.instance_id="<instance_id>"
   (logName="projects/<project-id>/logs/cloudaudit.googleapis.com%2Fdata_access" 
   AND (protoPayload.request.queryMode="PROFILE" 
   OR protoPayload.request.@type="type.googleapis.com/google.spanner.v1.ExecuteSqlRequest")
   AND -protoPayload.request.queryMode="PLAN"
   AND -protoPayload.request.requestOptions.requestTag:*
   )
   OR 
   (logName="projects/<project-id>/logs/cloudaudit.googleapis.com%2Factivity" 
   AND (type.googleapis.com/google.spanner.admin.database.v1.UpdateDatabaseDdlRequest 
   OR type.googleapis.com/google.spanner.admin.database.v1.CreateDatabaseRequest)
   AND operation.producer="spanner.googleapis.com"
   )
   AND -protoPayload.request.sql="SELECT 1"
   ```

## Configuring GCP for the input plug-in

1. [Create a topic in Pub/Sub](https://cloud.google.com/pubsub/docs/create-topic#create_a_topic_2).
2. [Create a subscription in Pub/Sub](https://cloud.google.com/pubsub/docs/create-subscription#create_a_pull_subscription)
3. [Create service account credentials](https://developers.google.com/workspace/guides/create-credentials#create_a_service_account):
    - To grant subscription access to the service account, select the **Pub/Sub Subscriber** role from the role
      selection list during the service account creation process.
    - You do not need to grant users access to this service account.
4. [Create credentials for a service account](https://developers.google.com/workspace/guides/create-credentials#create_credentials_for_a_service_account).
   This key is used in the Kafka Connect connector configuration.

## Enabling audit logs

The inclusion filter that is used during log sink creation makes sure that only relevant logs are routed.

### Viewing or downloading logs

To view or download the generated logs, make sure that the appropriate Identity and Access Management (IAM) roles are
assigned.
These roles control access to logs in GCP.

* **View logs**:
    - roles/logging.viewer (Logs Viewer)
    - roles/logging.privateLogViewer (Private Logs Viewer)
* **Download logs**:
    - roles/logging.admin (Logging Admin)
    - roles/logging.viewAccessor (Logs View Accessor)

For more information on IAM roles and access control,
see [Access Control with IAM](https://cloud.google.com/logging/docs/access-control).

### Setting destination permissions

To route audit logs to a specific destination, such as Pub/Sub topic and subscription, follow these steps:

1. [Get sink writer's identity](https://cloud.google.com/logging/docs/export/configure_export_v2#dest-auth).
2. If you have owner access to the
   destination, [set access controls](https://cloud.google.com/pubsub/docs/access-control#console). Copy the sink
   writer's identity and enter it in the **New Principals** field when you configure access policies for topics and
   subscriptions. </br>
    * For **topics**, assign the **Pub/Sub Publisher** and **Pub/Sub Subscriber** role. </br>
    * For **subscriptions**, assign the **Pub/Sub Publisher** role.

## Limitations

1. Error logs are not generated in GCP for Spanner and this connector does not support error traffic in Guardium.
2. The audit/data access log doesn't contain a server IP. The default value for the server IP is set to `0.0.0.0`.
3. The following important fields cannot be mapped:
    - Source program
    - OS User
    - Client HostName
4. Spanner does not require a DDL query for Drop Database operations because databases can only be deleted through the
   Spanner UI. When a database is deleted through the UI, query parameters are not captured in audit logs. Therefore,
   these operations do not appear in the full SQL report.
5. When you use GCP, duplicate entries can appear in both the reports and audit logs.

## Configuring Guardium

The Guardium universal connector is the Guardium entry point for native audit and data access logs. The Guardium
universal connector identifies and parses the received events, and converts them to a standard Guardium format. The
output of the Guardium universal connector is forwarded to the Guardium sniffer on the collector for policy and auditing
enforcements. You can configure Guardium to read the native audit and data access logs by customizing the Spanner
template.

### Before you begin

* Configure the policies that you need. For more information, see [Policies](/docs/#policies).
* You must have permissions for the S-Tap Management role. By default, the admin user is assigned the S-Tap Management
  role.

## Creating datasource profiles

You can create a new datasource profile from the **Datasource Profile Management** page.

### Procedure

1. Go to **Manage > Universal Connector > Datasource Profile Management**
2. Click the **➕ (Add)** button.
3. You can create a profile by using one of the following methods:

    * To create a new profile manually, go to the **"Add Profile"** tab and provide values for the following fields.
        * **Name** and **Description**.
        * Select a **Plug-in Type** from the dropdown. For example, **Spanner Over PubSub Kafka Connect 2.0**.

    * To upload from CSV, go to the **Upload from CSV** tab and upload an exported or manually created CSV file
      containing one or more profiles. You can also choose from the following options:
        * **Update existing profiles on name match** — Updates profiles with the same name if they already exist.
        * **Test connection for imported profiles** — Automatically tests connections after profiles are created.
        * **Use ELB** — Enables ELB support for imported profiles. You must provide the number of MUs to be used in the
          ELB process.

**Note:** Configuration options vary based on the selected plug-in.

## Configuration: Pub/Sub Kafka Connect 2.0-based Plugins

The following table describes the fields that are specific to Pub/Sub Kafka Connect 2.0 and similar plugins.

| Field                               | Description                                                                                                                                                                      |
|-------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Name**                            | Unique name of the profile.                                                                                                                                                      |
| **Description**                     | Description of the profile.                                                                                                                                                      |
| **Plug-in**                         | Plug-in type for this profile. A full list of available plug-ins are available on the **Package Management** page.                                                               |
| **Credential**                      | The credential to authenticate with the datasource. Must be created in **Credential Management**, or click **➕** to create one.                                                  |
| **Kafka Cluster**                   | Kafka cluster to deploy the universal connector.                                                                                                                                 |
| **Label**                           | Grouping label. For example, **customer name** or **ID**.                                                                                                                        |
| **GCP project id**                  | Google Cloud project ID that contains the Pub/Sub subscription.                                                                                                                  |
| **Pub/Sub Subscription ID**         | Pub/Sub subscription ID from which messages are consumed.                                                                                                                        |
| **GCP Topic**                       | Pub/Sub topic name.                                                                                                                                                              |
| **Maximum poll records**            | The maximum number of records returned in a single poll                                                                                                                          |
| **Expected events per second**      | Expected events per second. This value is used to automatically calculate the **parallel.pull.count** parameter when it not set. Calculation formula: ceil(expected.eps / 1000). |                                                                                                                                 |
| **Number of parallel pull streams** | Number of parallel pull streams to use. If not specified, this value is automatically calculated based on **expected.eps** (1 subscriber per 1000 EPS).                          |
| **No traffic threshold (minutes)**  | The time period after which the system detects inactivity.                                                                                                                       |

## Testing a Connection

After creating a profile, you must test the connection to ensure that the provided configuration is valid.

### Procedure

1. Select the new profile.
2. From the top menu, click **Test Connection**.
3. If the test is successful, you can proceed to installing the profile.

---

## Installing a Profile

Once the connection test is successful, you can install the profile on **Managed Units (MUs)** or **Edges**. The parsed
audit logs are sent to the selected Managed Unit or Edge to be consumed by the Sniffer.

### Procedure

1. Select the profile.
2. From the **Install** menu, click **Install**.
3. From the list of available MUs and Edges that is displayed, select the ones that you want to deploy the profile to.

---

## Uninstalling or reinstalling profiles

An installed profile can be **uninstalled** or **reinstalled** if needed.

### Procedure

1. Select the profile.
2. From the list of available actions, select the desired option: **Uninstall** or **Reinstall**.

---

## Required Configuration Changes for Production

## Configuring GCP for production

You must configure **exactly-once** delivery on your Pub/Sub subscription to prevent duplicate audit log entries at the
Pub/Sub level before the messages reach the connector.

### Procedure

1. To enable **exactly-once** delivery on your Pub/Sub subscription, use only one of the following commands based on
   your scenario.
    * For an existing subscription:
      ```bash
       gcloud pubsub subscriptions update <subscription-name> \
      --enable-exactly-once-delivery
      ```   
    * For a new subscription:
       ```bash
       gcloud pubsub subscriptions create <subscription-name> \
         --topic=<topic-name> \
         --enable-exactly-once-delivery \
         --ack-deadline=600
       ```

## Troubleshooting

#### Messages are not being processed

1. Verify that all Kafka worker settings are added.
2. Make sure the worker is restarted after configuration changes.
3. Restart the connector after the worker restart.

#### "Quota exceeded" errors in GCP

1. Check the current quota usage in the GCP console.
2. Request a quota increase.
3. Temporarily reduce the number of tasks until the quota is increased.

#### High number of unacknowledged messages in Pub/Sub

1. Verify that the connector is running by using the following command. </br>
   ```
   curl http://localhost:8083/connectors/<connector-name>/status
   ```
2. Check for errors in logs.
3. Verify that exactly-once delivery is enabled on the subscription.