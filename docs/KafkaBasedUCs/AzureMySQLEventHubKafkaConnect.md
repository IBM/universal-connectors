# Configuring Azure MySQL datasource profile for Kafka Connect Plug-ins

Create and configure datasource profiles through central manager for **Azure
MySQL over Event Hub Kafka Connect** plug-ins.

### Meet Azure MySQL over Event Hub Connect

* Environment: Azure
* Supported inputs: Kafka Input (pull)
* Supported Guardium versions:
    * Guardium Data Protection: Appliance bundle 12.2.2 or later

Kafka-connect is a framework for streaming data between Apache Kafka and other systems. This connector enables
monitoring of Azure MySQL audit logs through Azure Event Hub.

## Configuring the Azure MySQL Service

You can use the following methods to obtain Azure MySQL audit data:

1. Azure Event Hub
2. Azure Storage
3. Log Analytics Workspace

For this procedure, we are using Azure Event Hub.

### Procedure

1. Login to https://portal.azure.com.
2. In the search bar, search for and select **Azure Database for MySQL servers**.
3. Click **Create**.
4. On the Select Azure Database for MySQL deployment option page, select **Flexible server**.
5. On the **Basics** tab, provide the following details.
    - **Subscription**: Select your subscription name.
    - **Resource group**: Select existing resource group or create new one.
    - **Server name**: User need to provide a unique name that identifies your flexible server.
    - **Region**: Select the region or location.
    - **MySQL version**: Choose latest Version (here 8.0).
    - **Workload type**: User can choose size as per the requirement.
    - **Compute + storage**: Server configuration can be changed as per the requirement.
    - **Availability zone**: No preference, can be specified as per your requirement.
    - **Authentication method**: MySQL authentication only.
    - **Admin username**: Provide Username.
    - **Password**: Provide Password.
6. Under the **Networking** tab, for **Connectivity method**, select **Public access**.
7. For configuring Firewall rules, select **Add current client IP address** and check the checkbox to allow public access
   from any azure service.
8. Click **Review + create** to review your flexible server configuration.
9. Verify the configuration and then click **Create**.
10. When the deployment is completed, the server is ready for use.

## Enabling Audit Logs

1. Select your Azure Database for MySQL server.
2. Under **Settings**, select the **Server** parameter.
3. Update the **audit_log_enabled** parameter to ``ON``.
4. Select the event types to be logged by updating the **audit_log_events** parameter as shown below.
    - **CONNECTION** includes:
        - Connection initiation (successful or unsuccessful).
        - User reauthentication with different user/password during session.
        - Connection termination.
    - **GENERAL** includes:
        - DML_SELECT, DML_NONSELECT, DML, DDL, DCL, and ADMIN.

## Creating and connecting the Azure Event Hub namespace 

### Creating Azure Event Hub namespace

1. Login to https://portal.azure.com.
2. Search for ``event hub`` in search bar.
3. Click **Create event hubs namespace**.
4. To create a namespace, complete the following steps.</br>
    a. Select the **Subscription** in which you want to create the namespace.</br>
    b. Select the **Resource group** that you created in the previous step.</br>
    c. Enter a unique name for the namespace.</br>
    d. Select the same location for the namespace that you selected for the server.</br>
    e. Choose the pricing tier based on your requirement.</br>
    f. Set the throughput units (or processing units for standard and premium tiers) as ``10`` to prevent data loss. You can
      update the throughput units as per your requirement (some pricing tiers provide Auto-inflate feature). In Azure, 1
      throughput unit handles incoming data of up to 1 MB/second/1000 events and outgoing data of up to 2MB/second/4096
      events.</br>
    g. Click **Review + Create** at the bottom of the page.</br>
    h. Review the settings and select **Create**.</br>
    i. After the namespace is created, it appears in **Resource group**.</br>

### Azure Event Hub Connection

1. To create an event hub, complete the following steps.</br>
    a. Go to the Event Hubs Namespace page.</br>
    b. To add an event hub, click **+ Event Hub**.</br>
    c. Enter unique name for event hub.</br>
    d. Choose at least as many partitions as required during the peak load of your application for that particular event hub. For example, if you want to generate traffic from 2 database instances, the partition count should be at least 2 or more.</br>
    e. Click **Review+create**.</br>
    f. Review the settings and click **Create**.</br>

## Creating Azure Storage Accounts

1. Login to https://portal.azure.com.
2. Search for ``Storage accounts`` in the search bar.
3. Click **Create**.
4. **Basic** Tab:
    - Select the Subscription in which you want to create the Storage account.
    - Select or create new Resource group.
    - Enter a unique name for Storage account.
    - Select same region for the storage account which you selected for server.
    - Choose any Performance type.
    - Select Geo-redundant(GRS) Redundancy configuration.
    - Select Make read access to data option.
    - Click **Next:Advance**.
5. **Advanced** tab:
    - Require secure transfer option should be selected.
    - Allow enabling public access option should be selected.
    - Enable storage account key access option should be selected.
    - Select latest TLS version.
    - Permitted scope should be the default value(From any storage account).
    - Other parameters Hierarchical Namespace, Access protocols, Blob storage and Azure Files should be default value
      provided by azure.
    - Click **Next:Networking**.
6. **Networking** tab:
    - Enable public access from all networks for Network access.
    - Select the **Microsoft network routing** option for Routing preference.
    - Click **Next:Data protection**.
7. **Data protection** tab:
    - Use the default values provided by Azure.
    - Click **Next:Encryption**.
8. **Encryption** tab:
    - **Encryption type** is the Microsoft-managed key(MMK).
    - Enable support for customer-managed keys option should be by default value (blobs and files).
    - By default, infrastructure encryption should not be enabled.
    - Click **Next:Tags**.
9. On the **Tags** tab no need to select anything and click **Next:Review**.
10. Click on Create button after review all the parameters.

## Stream logs to an Event Hub

1. Login to https://portal.azure.com.
2. Go to server in your Azure Portal.
3. From **Monitoring**, select **Diagnostics settings**.
4. To change existing settings, select **Edit setting**.
5. To add new settings, select **Add diagnostics setting**. </br>
     a. Enter a name for the setting.</br>
     b. Select **MySQL Audit Logs** from categories.</br>
     c. In **Destination details**, select **Archive to a storage account**.</br>
     d. In **Archive to a storage account**, select **Storage account as created above**.</br>
     e. In **Destination details**, choose **Stream to an event hub**.</br>
     f. In **Stream to event hub**, select **namespace name and event hub name as created above**. Keep the event hub policy name as is.</br>
     g. Click **Save** to save the setting.</br>
6. After about 15 minutes, verify that the events are displayed in your event hub.

### Configuring multiple collectors

When UCs are configured on two separate Collectors, additional configurations are needed to monitor traffic from a single Event Hub.

1. Create a namespace in Azure Event Hub following the standard procedure, but select the **Standard pricing tier** instead of Basic in the pricing tier configuration.
2. After the namespace is created, create an Event Hub and generate a connection string as described in the previous steps.
3. After successfully creating the Event Hub, add a consumer group to the event hub. </br>
    a. From the list of Event Hubs, select your Event Hub. </br>
    b. On the Event Hub instance page, select **Consumer Groups** from the **Entities** section. Then click **Add**. </br>
    c. Enter a name for the consumer group and create it. </br>
4. Configure log streaming to the Event Hub as described in the previous steps.
5. For gmachine, use the following consumer group names in the connector configurations. </br>
    a. Machine 1: Use the consumer group name ``$Default``.  </br>
    b. Machine 2: Use the name of the consumer group created in step 3. </br>
    c. Keep all other configuration settings as is. </br>

## Connecting to Azure MySQL Database

1. Go to the server and click **Connect** in the Overview page.
2. Enter the password that you have set while creating the server and click **Enter**.
3. You can successfully connect Mysql and execute queries.

## Limitations

- The audit log does not contain a server IP. The **Server IP** is set to default value `0.0.0.0`.
- Error events will cause a duplicated success events in Guardium due to duplicate events in the Azure EventHub audit
   log.
- Azure EventHub is not capturing **Syntactical error** queries logs, **Login Failed** logs, and logs (i.e., when using
   **az commands** (example below) and **REST API**).
    - Example:
      `az mysql flexible-server db create --resource-group <testresgroup> --server-name <testserver> --database-name <db>`
- There are certain [limited privileges](https://learn.microsoft.com/en-us/azure/mysql/how-to-create-users) given by
   Azure MYSQL to users.
- We are getting below extra logs while executing `USE` command:
    - show tables
    - show databases
- The following important fields cannot be mapped with Azure MySQL logs:
    - Source Program
    - Client Host Name
    - Server Port
- Database name is not available in **General logs**, it only available at the time of **Disconnect** and **Connect**
(We must use **database name** at the time of connection to get the database name).
- Eventhub capturing identical duplicate logs for each query and same has been carrying to Guardium reports.
- **Database name** and **Service name** are not identical when user execute queries using Third party tool (DB
   Visualizer/ MySQL Workbench).

## Creating datasource profiles

You can create a new datasource profile from the **Datasource Profile Management** page.

### Procedure

1. Go to **Manage > Universal Connector > Datasource Profile Management**
2. Click the **➕ (Add)** button.
3. You can create a profile by using one of the following methods:

    * To **Create a new profile manually**, go to the **"Add Profile"** tab and provide values for the following fields.
        * **Name** and **Description**.
        * Select a **Plug-in Type** from the dropdown. For example, `Azure MySQL Over Event Hub Connect 2.0`.

    * To **Upload from CSV**, go to the **"Upload from CSV"** tab and upload an exported or manually created CSV file
      containing one or more profiles. You can also choose from the following options:
        * **Update existing profiles on name match** — Updates profiles with the same name if they already exist.
        * **Test connection for imported profiles** — Automatically tests connections after profiles are created.
        * **Use ELB** — Enables ELB support for imported profiles. You must provide the number of MUs to be used in the
          ELB process.

**Note:** Configuration options vary based on the selected plug-in.

## Configuring Azure MySQL Over Event Hub Kafka Connect 2.0

The following table describes the fields that are specific to Azure Event Hub Kafka Connect 2.0 plugin.

| Field                              | Description                                                                                                                                                                                                                                                                             |
|------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Name**                           | Unique name of the profile.                                                                                                                                                                                                                                                              |
| **Description**                    | Description of the profile.                                                                                                                                                                                                                                                              |
| **Plug-in**                        | Plug-in type for this profile. **Select Azure MySQL Over Event Hub Connect 2.0**. A full list of available plug-ins are available on the **Package Management** page.                                                                                                                      |
| **Credential**                     | The credential to authenticate with Azure Event Hub. Must be created in **Credential Management**, or click **➕** to create one. For more information, see [Creating Credentials](https://www.ibm.com/docs/en/SSMPHH_12.x/com.ibm.guardium.doc.stap/guc/guc_credential_management.html). |
| **Kafka Cluster**                  | Select the appropriate Kafka cluster from the available Kafka cluster list or create a new Kafka cluster. For more information, see [Managing Kafka clusters](https://www.ibm.com/docs/en/SSMPHH_12.x/com.ibm.guardium.doc.stap/guc/guc_kafka_cluster_management.html).                  |
| **Label**                          | Grouping label. For example, customer name or ID.                                                                                                                                                                                                                                        |
| **Event Hub Connection String**    | Primary connection string from the Event Hub shared access policy. This connection string is obtained from the Event Hub namespace.                                                                                                                                                  |
| **Storage Connection String**      | Connection string for the Azure Storage Account. Required when reading from multiple Event Hubs to track offsets.                                                                                                                                                                        |
| **Consumer Group**                 | Consumer group name for the Event Hub. Use `$Default` for single collector or create custom consumer groups for multiple collectors.                                                                                                                                                     |
| **Event Hub Partition Count**      | Number of partitions in the Event Hub. This value should match or exceed the number of database instances generating traffic.                                                                                                                                                                       |
| **Enrollment ID**                  | Unique identifier for the Azure resource enrollment.                                                                                                                                                                                                                                    |
| **No-traffic threshold (minutes)** | Default value is 60. If there is no incoming traffic for an hour, S-TAP displays a red status. Once incoming traffic resumes, the status returns to green.                                                                                              |
| **Use Enterprise Load Balancing (ELB)** | Enable this if ELB support is required.                                                                                                                                                                                                                                                  |
| **Managed Unit Count**             | Number of Managed Units (MUs) to allocate for ELB.                                                                                                                                                                                                                                        |

  **Note:**

- Ensure that the **profile name** is unique.
- Required credentials must be created before or during profile creation.
- The Azure credentials must have appropriate permissions to read from Event Hub.
- The partition count must be configured based on expected traffic volume.

---

## Azure Credential Configuration

When creating credentials for Azure Event Hub, provide the following information:

| Field name            | Description                                                     |
|-----------------------|-----------------------------------------------------------------|
| **Name**              | A unique credential name                                        |
| **Description**       | A description for your credential                               |
| **Credential Type**   | Azure Event Hub Credentials `Azure Event Hub Connection String` |
| **Connection String** | Event Hub connection string with manage permissions             |

---

---

## Testing a Connection

After creating a profile, you must test the connection to ensure the provided configuration is valid.

### Procedure

1. Select the new profile.
2. From the top menu, click **Test Connection**.
3. If the test is successful, you can proceed to installing the profile.

---

## Installing a Profile

Once the connection test is successful, you can install the profile on **Managed Units (MUs)** or **Edges**. The parsed
audit logs are sent to the selected Managed Unit or Edge to be consumed by the **Sniffer**.

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
