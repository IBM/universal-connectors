# Configuring Capella data source profiles for Capella Kafka Connect plug-ins

Create and configure data source profiles through central manager for Capella Kafka Connect plug-ins.

## Meet Couchbase Capella Over Capella Kafka Connect
* Environments: On-prem
* Supported inputs: Capella Kafka connect 2.0 (pull)
* Supported Guardium versions:
   * Guardium Data Protection: Appliance bundle 12.2.3 or later

Kafka-connect is a framework for streaming data between Apache Kafka and other systems.

## Configuring the Couchbase Capella database

Install and setup a Couchbase server before you proceed. 

**Note:** 
- To manage Couchbase Capella Operational audits,
see [Manage Audits](https://docs.couchbase.com/cloud/security/audit-management.html).

- To work with audit logging for App Services, you must use the Capella Operational Management API.<br>
           <ul><li>For an overview of the Management API,
  see [Manage Deployments with the Management API](https://docs.couchbase.com/cloud/management-api-guide/management-api-intro.html).</li>
               <li>To get started with the Management API,
  see [Get Started with the Management API](https://docs.couchbase.com/cloud/management-api-guide/management-api-start.html).</li>
                 <li>To make an API call,
  see [Make an API Call with the Management API](https://docs.couchbase.com/cloud/management-api-guide/management-api-use.html).</li>
                 <li>For a full reference guide,
  see [Management API Reference](https://docs.couchbase.com/cloud/management-api-reference/index.html).</li></ul>

## Enabling audit logs

To configure and manage audit logging for App Services and App Endpoints, see [Manage Audit Logs](https://docs.couchbase.com/cloud/app-services/monitoring/manage-audit-logs.html).

1. Use ```PUT /appservices/{appServiceId}/auditLog```.

2. Pass the App Service ID as a path parameter.

3. Pass ``"auditEnabled": true`` as the request body.

### Viewing the Audit logs status

To view the current status of audit logging for a specified App Service, see [View Log Status](https://docs.couchbase.com/cloud/app-services/monitoring/manage-audit-logs.html#view-log-status).

1. Use ```GET /appservices/{appServiceId}/auditLog```.

2. Pass the App Service ID as a path parameter.

### Exporting Audit Logs

To create an export job to gather and prepare the audit log files for export, see [Export Audit Logs](https://docs.couchbase.com/cloud/app-services/monitoring/manage-audit-logs.html#export-app-services-audit-logs).

When the export job is completed, you can download the compressed file from S3 by using the provided download URL. Export requests expire after 72 hours. The download URL is valid for one hour when the export request is created.

1. Create an App Services audit log export job. <br>
      a. Use ```POST /appservices/{appServiceId}/auditLogExports```. <br>
      b. Pass the App Service ID as a path parameter. <br>
      c. Pass the start time and end time for the audit log export job in the request body. <br>

      If successful, the request returns an audit log Export ID. 

2. Get an App Services audit log export job. <br>
      a. Use ```GET /appservices/{appServiceId}/auditLogExports/{auditLogExportId}``` and enter the export ID that you obtained in Step 1 in the **auditLogExportId** field.<br>
      b. Pass the App Service ID and the Audit Log Export ID as path parameters.

3. List App Services audit log export jobs. <br>
      a. Use ```GET /appservices/{appServiceId}/auditLogExports```. <br>
      b. Pass the App Service ID as a path parameter.

      If successful, the request returns an array of all the audit log export jobs for the specified App Service.

For each audit log export job, when the export is ready, the **download_id** field gives a URL that you can use to download
the exported audit log.

## Creating datasource profiles

You can create a new datasource profile from the **Datasource Profile Management** page.

### Procedure

1. Go to **Manage > Universal Connector > Datasource Profile Management**
2. Click the **➕ (Add)** button.
3. You can create a profile by using one of the following methods:

   * To create a new profile manually, go to the **"Add Profile"** tab and provide values for the following fields.
      * **Name** and **Description**.
      * Select a **Plug-in Type** from the dropdown. For example, **Couchbase Capella over Capella Kafka Connect 2.0**.

   * To upload from CSV, go to the **Upload from CSV** tab and upload an exported or manually created CSV file
     containing one or more profiles. You can also choose from the following options:
      * **Update existing profiles on name match** — Updates profiles with the same name if they already exist.
      * **Test connection for imported profiles** — Automatically tests connections after profiles are created.
      * **Use ELB** — Enables ELB support for imported profiles. You must provide the number of MUs to be used in the
        ELB process.

**Note:** Configuration options vary based on the selected plug-in.

## Limitations

* Capella endpoint limitation doc (https://docs.couchbase.com/cloud/security/auditing.html#limitations)
* The original Capella audit log contains no values for the following fields: Database Name, Service Name.

## Configuring JDBC Kafka Connect 2.0-based plugins

Below is a description of the fields specific to Capella Kafka Connect 2.0 and:

| Field                                         | Description                                                                                                                                | Value/Example                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                         |
|-----------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Name**                                      | Unique name of the profile                                                                                                                 | Couchbase_Capella_KAFKA_CONNECT                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          |
| **Description**                               | Description of the profile                                                                                                                 | Profile for Couchbase Capella over Capella Kafka connect 2.0 plug-in                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                   |
| **Plug-in**                                   | Plug-in type for this profile. A full list of available plug-ins is available on the **Package Management** page                           | Couchbase Capella over Capella Kafka connect 2.0                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                       |
| **Credential**                                | The credential to authenticate with the datasource. Create the credential in **Credential Management**, or click **➕** to create a new one |                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                       |
| **Kafka Cluster**                             | Kafka cluster to deploy the universal connector                                                                                            | Select from existing Kafka clusters attached to central management                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                    |
| **Label**                                     | Grouping label (e.g., **customer name** or **ID**)                                                                                         |                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                       |
| **Capella API Base URL**                                  | Capella API base URL                                                                                                                   | https://cloudapi.cloud.couchbase.com/v4                     |
| **Organization ID**                | Organization ID                                                                              |  |
| **Project ID**                   | Project ID                                                                                         |                                                                                                                                                                                                                                                                                       |
| **Cluster ID** | Cluster ID                                                                                    | Numeric                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                               |
| **Query interval (seconds)**      | The time length in a poll                                                                                                      |                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                 |
| **Query time window (seconds)**    | Query time window                                                                                 | Numeric                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                               |
| **Initial start time in epoch seconds (e.g., 1712419200 for 2024-04-06T16:00:00Z)**         | Customized start time for first                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                |
| ** No traffic threshold (minutes)**                         | No traffic threshold                                                                                     |                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                     |

## Testing a Connection

After creating a profile, you must test the connection to make sure that the provided configuration is valid.

### Procedure

1. Select the new profile.
2. From the top menu, click **Test Connection**.
3. If the test is successful, you can proceed to installing the profile.

---

## Installing a Profile

After the connection test succeeds, you can install the profile on **Managed Units (MUs)** or **Edges**. The parsed
audit logs are sent to the selected Managed Unit or Edge for to be consumed by the **Sniffer**.

### Procedure

1. Select the profile.
2. From the **Install** menu, click **Install**.
3. From the displayed list of available MUs and Edges, select the ones to which you want to deploy the profile.

---

## Uninstalling or reinstalling profiles

An installed profile can be uninstalled or reinstalled if needed.

### Procedure
1. Select the profile.
2. From the list of available actions, select the desired option **Uninstall** or **Reinstall**.

---



