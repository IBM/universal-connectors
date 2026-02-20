# Bigtable over Pub/Sub Source Connector

This connector enables IBM Guardium Data Protection (GDP) to monitor and collect audit logs from Bigtable databases
through Google Cloud Pub/Sub using Kafka Connect.

## Meet BigTable over Pub/Sub Connect

* Environments: On-prem
* Supported inputs: Kafka Input (pull)
* Supported Guardium versions:
    * Guardium Data Protection: Appliance bundle 12.2.2 or later

Kafka-connect is a framework for streaming data between Apache Kafka and other systems.

## Configuring BigTable on GCP

### BigTable Setup
1. Complete the BigTable [Prerequisites](https://cloud.google.com/bigtable/docs/quickstarts/quickstart-cloud-console).

2. Enable the BigTable API. </br>
BigQuery is automatically enabled in new projects. For existing projects, you must enable the BigTable API. For more information, see [Enable the BigTable API](https://console.cloud.google.com/marketplace/product/google/bigtable.googleapis.com).
3. Create a BigTable dataset to store your data. For more information, see [Create a Dataset](https://cloud.google.com/bigtable/docs/creating-instance).

4. Create a table. For more information, see [Create Table](https://cloud.google.com/bigtable/docs/samples/bigtable-hw-create-table).</br>

5. Query table data. For more information, see see [Query Table Data](https://cloud.google.com/bigquery/docs/external-data-bigtable).

### Permissions and Roles for log viewing and downloading

To view or download the generated logs, make sure that the appropriate Identity and Access Management (IAM) roles are
assigned.

* To View logs:
     - roles/logging.viewer (Logs Viewer)
     - roles/logging.privateLogViewer (Private Logs Viewer)
* To download logs:
     - roles/logging.admin (Logging Admin)
     - roles/logging.viewAccessor (Logs View Accessor)

### Creating a topic in Pub/Sub

1. Go to the Pub/Sub topics page in the Cloud Console.
2. Click **Create a topic**.
3. In the **Topic ID** field, provide a unique topic name. For example, ``MyTopic``.
4. Click **Create Topic**.

### Creating a subscription in Pub/Sub

1. Display the menu for the topic created in the previous step and click **New subscription**.
2. Type a name for the subscription, such as MySub.
3. Leave the delivery type as **Pull**.
4. Click **Create**.

### Creating a log sink in Pub/Sub

1. In the Cloud Console, go to the **Logging** > **Log Router page**.
2. Click **Create sink**.
3. In the **Sink details** panel, enter the following details:</br>
      a. Sink name: Provide an identifier for the sink. Once you create the sink, you cannot rename it. However, you can delete a sink and create a new one.</br>
      b. Sink description (optional): Describe the purpose or use case for the sink.</br>
4. In the **Sink destination** panel, select the Cloud Pub/Sub topic as sink service and select the topic that you created in the previous steps.
5. Choose logs to include in the sink in the Build inclusion filter panel.
6. You can filter the logs by log name, resource, and severity.
7. In cases of multiple regions, you need to do the same set of configurations for each region.
  Based on the region, different configuration files are used for the input plug-in.          

### Setting permissions for the destination (TOPIC & SUBSCRIPTION)

1. Obtain the sink's writer identity from the new sink. For example, an email address.</br> 
    a. Go to the Log Router page, and select **menu** > **View sink details**.</br>
    b. The writer identity appears in the Sink details panel.</br>
2. If you have owner access to the destination:</br>
    a. Add the sink's writer identity to topic.</br>
        - Navigate to the Topic you created.</br>
        - Click on the **SHOW INFO** panel.</br>
        - Click **ADD PRINCIPAL**.</br>
        - Paste writer identity in the New Principals.</br>
        - Give it the Pub/Sub Publisher role and subscriber role.</br>

    b. Add the sink's writer identity to subscription. </br>
        - Navigate to the Subscription.</br>
        - Click **SHOW INFO** panel.</br>
        - Click **ADD PRINCIPAL**.</br>
        - Paste writer identity in the New Principals<./br>
        - Give it the subscriber role.</br>

### Creating service account credentials

1. Go to the **Service accounts** section of the IAM & Admin console.
2. Select **project** and click **Create Service Account**.
3. Enter a **Service account name**, such as Bigtable-Pub/Sub.
4. Click **Create**.
5. The owner role is required for the service account. Select the owner role from the drop-down menu.
6. Click **Continue**. You do not need to grant users access to this service account.
7. Click **Create Key**. This key is used by the Logstash input plug-in configuration file.
8. Select **JSON** and click **Create**.

### Inclusion Filter

To edit the Sink, go to **Logs Router** > **Sink Inclusion Filter**.

This inclusion filter excludes unnecessary logs and includes required logs with resource types and metadata only from BigTable.

```    
protoPayload.serviceName="bigtableadmin.googleapis.com" OR protoPayload.serviceName="bigtable.googleapis.com"
```

## Viewing the Audit logs

The above inclusion filter is used to view the audit logs in the GCP Logs Explorer.

### Supported audit logs

1. BigTableAudit - `ACTIVITY`, `DATA_ACCESS` logs
2. BigTable Log - `CREATEINSTANCE`, `DELETEINSTANCE`, `UPDATEINSTANCE`, `CREATECLUSTER`, `DELETECLUSTER`, `UPDATECLUSTER`, `CREATETABLE`, `DELETETABLE`, `MODIFYCOLUMNFAMILIES`, `EXECUTEQUERY`, `LISTCLUSTERS`, `LISTINSTANCES`.

## Limitations
* Exception object is prepared based on severity of the logs.
* The data model size is limited to 10 GB per table. If you have a 100 GB reservation per project per location, BigTable BI Engine limits the reservation per table to 10 GB. The rest of the available reservation is used for other tables in the project.
* BigTable cannot read the data in parallel if you use gzip compression. Loading compressed JSON data into BigTable is slower than loading uncompressed data.
* You cannot include both compressed and uncompressed files in the same load job.
* JSON data must be newline delimited. Each JSON object must be on a separate line in the file.
* The maximum size for a gzip file is 4 GB.
* Log messages have a size limit of 100K bytes.
* The Audit/Data access log doesn't contain a server IP. The default value is set to `0.0.0.0` and can be in IPV4 or IPV6 format.
* The following important fields cannot be mapped, as there is no information regarding these fields in the logs:
    - Source program
    - OS User
    - Client HostName
* While using GCP, duplicate entries may appear in both the reports and audit logs.
* Bigtable uses two different service names (`bigtable.googleapis.com` & `bigtableadmin.googleapis.com`) depending on the tasks being performed. This results in two distinct S-TAP host entries.
* Multiple sessions from the same Bigtable instance may result in multiple S-TAP entries.
* The BigTable audit log doesn’t include login failed logs. So, these logs do not appear in the guardium LOGIN_FAILED report.

## Creating datasource profiles

You can create a new datasource profile from the **Datasource Profile Management** page.

### Procedure

1. Go to **Manage > Universal Connector > Datasource Profile Management**
2. Click the **➕ (Add)** button.
3. You can create a profile by using one of the following methods:

    * To create a new profile manually, go to the **"Add Profile"** tab and provide values for the following fields.
        * **Name** and **Description**.
      * Select a **Plug-in Type** from the dropdown. For example, **BigTable Over PubSub Kafka Connect 2.0**.

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
