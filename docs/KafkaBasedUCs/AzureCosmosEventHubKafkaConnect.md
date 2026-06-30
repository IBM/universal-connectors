# Configuring Azure Cosmos datasource profiles for Kafka Connect Plug-ins

Create and configure datasource profiles through central manager for **Azure
Cosmos over Event Hub Kafka Connect** plug-ins.

### Meet Azure Cosmos over Event Hub Connect

* Environment: Azure
* Supported inputs: Kafka connect Azure EventHub 2.0
* Supported Guardium versions:
    * Guardium Data Protection: 12.1 UC patch 5008 and above
    * Guardium Data Protection: Appliance bundle 12.2.3 or later

Kafka-connect is a framework for streaming data between Apache Kafka and other systems. This connector enables
monitoring of Azure Cosmos DB audit logs through Azure Event Hub.

## Configuring the Azure Cosmos Service

You can retrieve Azure Cosmos audit data from the following locations:

1. Azure Event Hub
2. Log Analytics Workspace

This plug-in uses Azure Event Hub as the data streaming service.

### Procedure

1. Login to https://portal.azure.com.
2. From the Azure portal menu or the home page, create an Azure Cosmos DB account by following these steps:
    - If you are on the home page, select **Create a resource**.
    - On the new page, search for and select **Azure Cosmos DB**.
    - On the Select API option page, click **Create option** in NoSQL.
3. If you are on the **Create Azure Cosmos DB Account** page, provide the following details:
    - **Subscription**: Select a valid subscription to create an Azure Cosmos DB account.
    - **Resource group**: Choose the existing resource group or create a new one by clicking **Create New**.
    - **Account name**: Provide a name for your Azure Cosmos DB account.
    - **Region**: Select the region or location.
    - **Capacity Mode**: Select **Provisioned Throughput** to create an account in provisioned throughput mode. Select **Serverless** to create an account in serverless mode.
    - **Apply Azure Cosmos DB free tier discount**: Click **Apply** or **Do not apply**.
4. Keep the other tab values as is. Finally, click on **Review+Create**.
5. Review the settings you provided, and then select **Create**. It takes a few minutes to create the account. Wait for the portal page to display "Your deployment is complete" before moving on.
6. Select **Go to resource** to go to the Azure Cosmos DB account page.

## Enabling Auditing

### Enable full-text query for logging query text

1. By enabling full-text query, you will be able to view the deobfuscated query for all requests within your Azure Cosmos DB account.
    - To enable this feature, navigate to the Features page in the Azure Cosmos DB Account.
    - Click on **Diagnostics full-text query**. A full-text query panel appears.
    - Select **Enable**. It can take a few minutes before your selection is applied.

**Note:** By default, the query text and its parameters are obfuscated to avoid logging personal data.

### Monitoring Azure Cosmos DB data by using diagnostic settings in Azure

1. Login to https://portal.azure.com.
2. Navigate to your Azure Cosmos DB account. Open the Diagnostic settings pane under the Monitoring section.
3. After the page opens, you will be prompted to enable or disable the full-text query according to your preference. You can choose to do so, or click **'Not now'** and then select **Add diagnostic setting**.
4. In the Diagnostic settings pane, fill in the form with your preferred categories. Here is a list of log categories:
    - **DataPlaneRequests**: Logs back-end requests as data plane operations, which are requests executed to create, update, delete, or retrieve data within the account.
    - **QueryRuntimeStatistics**: This table details query operations executed against an API for a NoSQL account. By default, the query text and its parameters are obfuscated to avoid logging personal data with full text query logging available by request.
    - **ControlPlaneRequests**: Logs details on control plane operations, which include creating an account, adding or removing a region, updating account replication settings, etc.
5. Select your categories details, and then send your logs to your preferred destination.

## Configuring Azure Event Hub

### Azure Event Hub Connection

1. Search for Storage Accounts in the search bar of the Azure portal.</br>
   (If you need your plug-in to read events from multiple Event Hubs, you need a storage account.)</br>
    a. Click **Create**.</br>
    b. Enter the required details in the presented form.</br>
    c. Review the details, and then create the storage account.</br>
    d. Go into the newly created storage account.</br>
    e. In the menu, go to **Security + networking** > **Access keys**.</br>
    f. Choose any key (though key1 is preferable), and click **Show** in **Connection String**.</br>
    g. Copy the presented connection string and save it somewhere.</br>

2. In the search bar, enter and select ``event hub``.
3. Select **Create** to create event hubs namespace.
4. To create a namespace, complete the following steps.</br>
    a. Select the **Subscription** in which you want to create the namespace.</br>
    b. Select the **Resource group** that you created in the previous step.</br>
    c. Enter a unique name for the namespace.</br>
    d. Select a location for the namespace.</br>
    e. Choose the pricing tier based on your requirement.</br>
    f. Set the throughput units (or processing units for standard and premium tiers) as ``10`` to prevent data loss. You can update the throughput units as per your requirement (some pricing tiers provide Auto-inflate feature). In Azure, 1 throughput unit handles incoming data of up to 1 MB/second/1000 events and outgoing data of up to 2MB/second/4096 events.</br>
    g. Select **Review + Create**.</br>
    h. Review the settings and select **create**.</br>
    i. After successful creation, the recently created namespace appears in the resource group.</br>

5. To create an event hub, complete the following steps.
    a. Go to the Event Hubs Namespace page.
    b. Click **+ Event Hub** to add event hub.
    c. Enter a unique name for the event hub.
    d. Choose at least as many partitions as required during the peak load of your application for that particular event hub. For example, if you want to generate traffic from 2 database instances, the partition count should be at least 2 or more.
    e. Click **Review+create**.
    f. Review the settings and click **Create**.

6. Connection string for an event hub, complete the following steps.</br>
    a. From the list of event hubs, select your event hub.</br>
    b. On the Event Hubs instance page, go to **Settings** > **Shared access policies** > **Add**.</br>
    c. In shared access policies, click **Add**.</br>
    d. Enter a policy name and select **Manage** from the permission. Then create the policy.</br>
    e. Take the value of the primary key - connection string from the policy (this is required for the connector configuration).</br>
    f. The value of the primary key - connection string will be used in the connector configuration.</br>

## Creating Azure Storage Accounts

1. Login to https://portal.azure.com.
2. Search for ``Storage accounts`` in the search bar.
3. Click **Create**.
4. **Basic** Tab:
    - Select the Subscription in which you want to create the Storage account.
    - Select or create new Resource group.
    - Enter a unique name for Storage account.
    - Select same region for the storage account which you selected for the Cosmos DB account.
    - Choose any Performance type.
    - Select **Geo-redundant(GRS) Redundancy** configuration.
    - Select **Make read access to data** option.
    - Click **Next:Advance**.
5. **Advanced** tab:
    - **Require secure transfer** option should be selected.
    - **Allow enabling public access** option should be selected.
    - **Enable storage account key access** option should be selected.
    - Select latest TLS version.
    - Permitted scope should be the default value (From any storage account).
    - Other parameters Hierarchical Namespace, Access protocols, Blob storage and Azure Files should be default value provided by Azure.
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
10. Click on **Create** button after review all the parameters.

## Stream logs to an Event Hub

1. Login to https://portal.azure.com.
2. Navigate to your Azure Cosmos DB account. From **Monitoring**, select **Diagnostics settings** and do either of the following:
    - To change existing settings, select **Edit setting**.
    - To add new settings, select **Add diagnostics setting**.
3. To add new settings, do the following:</br>
    a. Name the setting.</br>
    b. In **Categories**, select **DataPlaneRequests**, **QueryRuntimeStatistics**, and **ControlPlaneRequests**.</br>
    c. In **Destination details**, choose **Archive to a storage account**.</br>
    d. In **Archive to a storage account**, select the storage account you created previously.</br>
    e. In **Destination details**, choose **Stream to an event hub**.</br>
    f. In **Stream to event hub**, select the namespace name and event hub name that you created previously. Keep the event hub policy name as is.</br>
    g. Click **Save**.</br>
4. After about 15 minutes, verify that events are displayed in your event hub.

### Configuring multiple collectors

When UCs are configured on two separate Collectors, additional configurations are needed to monitor traffic from a single Event Hub.

1. Create a namespace in Azure Event Hub following the standard procedure, but select the **Standard pricing tier** instead of Basic in the pricing tier configuration.
2. After the namespace is created, create an Event Hub and generate a connection string as described in the previous steps.
3. After successfully creating the Event Hub, add a consumer group to the event hub.</br>
    a. From the list of Event Hubs, select your Event Hub.</br>
    b. On the Event Hub instance page, select **Consumer Groups** from the **Entities** section. Then click **Add**.</br>
    c. Enter a name for the consumer group and create it.</br>
4. Configure log streaming to the Event Hub as described in the previous steps.
5. For gmachine, use the following consumer group names in the connector configurations.</br>
    a. Machine 1: Use the consumer group name ``$Default``.</br>
    b. Machine 2: Use the name of the consumer group created in step 3.</br>
    c. Keep all other configuration settings as is.</br>

**Note:**

The following recommendations apply when configuring the plug-in to read from multiple event hubs. These guidelines are relevant in two scenarios:
- When multiple connection strings are specified in event_hub_connections.
- When multiple universal connector configurations exist on a single Guardium machine.

* When there is more than one connection string provided in the **event_hub_connections** parameter, define both **storage_connection** and **consumer_group** parameters. This helps to differentiate the files where timestamp offsets are written. The **azure_event_hubs** input does not store the offset locally.

Not following this recommendation may result in data loss.

## Connecting to the Azure Cosmos Database

### Insert/Update data through Data Explorer

1. Login to https://portal.azure.com.
2. Navigate to your Azure Cosmos DB account. Open the Data Explorer pane, and then select **New Container** to create a container.
3. After the container is created, use the Data Structures browser to find and open it.
4. In the container you created, click **Items** > **New Item**. Use this feature to create the JSON items.

### Query Execution through Data Explorer

1. Select **New SQL Query**.
2. Write the SQL query and click **Execute Query**.

### Configuring Throughput and Performance

You can adjust the Azure Cosmos DB Request Units (RUs).

**Adjust throughput at the account level**

1. Navigate to your Azure Cosmos DB account in the Azure portal.
2. In the left navigation menu under **Settings**, Adjust the **Throughput (RU/s)** for the account. This sets the total throughput that can be allocated to databases and containers.
3. Click **Save** to apply the changes.

**Adjust throughput at the database or container level**

1. Navigate to your Azure Cosmos DB account in the Azure portal.
2. Open **Data Explorer**.
3. Select your database or container.
4. Click **Scale & Settings**.
5. Adjust the **Throughput (RU/s)** based on your expected event volume. Higher traffic requires higher RU provisioning to avoid throttling and missed or delayed audit events.
6. Click **Save** to apply the changes.

## Limitations

1. The following important fields couldn't be mapped with Azure Cosmos audit logs:
    - **Source program**: This field is left blank, as this information is not embedded in the messages pulled from Azure.
    - **ServerIP**: This field is populated with `0.0.0.0`, as this information is not embedded in the messages pulled from Azure.
    - **Client HostName**: Not available with logs.
    - **dbUser**: This field is populated with `N.A.`, as it is not available in ControlPlaneRequest, QueryRuntimeStatistics logs.
    - **Exception > sqlString**: This field is populated with NA, as it is not available in Error logs.
2. Logs are not captured for failed login attempts.
3. For DataPlaneRequest, dbUser is `N.A.` when AADPrincipalId is empty.
4. Data Explorer and CLI don't support creation, updating, and deletion for user and permission operations; only the REST API does.
5. REST API doesn't support creation, updating, and deletion for role definition and role assignment operations; But the Data Explorer and CLI does.
6. Data Explorer, CLI and REST API don't support trigger execution; It can only be called via programs.
7. In case of REST API, ControlPlaneRequest logs will not generate.
8. To get an Azure Active Directory(AAD) token, you need to give a few permissions that disable read-write keys and only allow read-only keys.
9. With the AAD token, you cannot use any Azure Cosmos DB data plane SDK to authenticate management operations (create/delete database, container, triggers, stored procedures, user-defined functions) with an Azure AD identity. Instead, use Azure role-based access control through one of the following options:
    - Azure Resource Manager templates (ARM templates)
    - Azure PowerShell scripts
    - Azure CLI scripts
10. Application is the only option to work with Azure CosmosDB SDK using Azure Active Directory(AAD) token and it gives only ApplicationID as AAD PrincipalID which we use for DBUser.

## Creating datasource profiles

You can create a new datasource profile from the **Datasource Profile Management** page.

### Procedure

1. Go to **Manage > Universal Connector > Datasource Profile Management**
2. Click the **➕ (Add)** button.
3. You can create a profile by using one of the following methods:

    * To **Create a new profile manually**, go to the **"Add Profile"** tab and provide values for the following fields.
        * **Name** and **Description**.
        * Select a **Plug-in Type** from the dropdown. For example, `Azure Cosmos Over Event Hub Connect 2.0`.

    * To **Upload from CSV**, go to the **"Upload from CSV"** tab and upload an exported or manually created CSV file containing one or more profiles. You can also choose from the following options:
        * **Update existing profiles on name match** — Updates profiles with the same name if they already exist.
        * **Test connection for imported profiles** — Automatically tests connections after profiles are created.
        * **Use ELB** — Enables ELB support for imported profiles. You must provide the number of MUs to be used in the ELB process.

**Note:** Configuration options vary based on the selected plug-in.

## Configuring Azure Cosmos Over Event Hub Kafka Connect 2.0

The following table describes the fields that are specific to Azure Event Hub Kafka Connect 2.0 plugin.

| Field                                   | Description                                                                                                                                                                                                                                                                             |
|-----------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Name**                                | Unique name of the profile.                                                                                                                                                                                                                                                              |
| **Description**                         | Description of the profile.                                                                                                                                                                                                                                                              |
| **Plug-in**                             | Plug-in type for this profile. Select `Azure Cosmos Over Event Hub Connect 2.0`. A full list of available plug-ins are available on the **Package Management** page.                                                                                                                     |
| **Credential**                          | The credential to authenticate with Azure Event Hub. Must be created in **Credential Management**, or click **➕** to create one. For more information, see [Creating Credentials](https://www.ibm.com/docs/en/SSMPHH_12.x/com.ibm.guardium.doc.stap/guc/guc_credential_management.html). |
| **Kafka Cluster**                       | Select the appropriate Kafka cluster from the available Kafka cluster list or create a new Kafka cluster. For more information, see [Managing Kafka clusters](https://www.ibm.com/docs/en/SSMPHH_12.x/com.ibm.guardium.doc.stap/guc/guc_kafka_cluster_management.html).                  |
| **Label**                               | Grouping label. For example, customer name or ID.                                                                                                                                                                                                                                       |
| **Event Hub Connection String**         | Primary connection string from the Event Hub shared access policy. This connection string is obtained from the Azure Event Hub configuration.                                                                                                                              |
| **Storage Connection String**           | Connection string for the Azure Storage Account. Required when reading from multiple Event Hubs to track offsets.                                                                                                                                                                        |
| **Consumer Group**                      | Consumer group name for the Event Hub. Use `$Default` for single collector or create custom consumer groups for multiple collectors.                                                                                                                                                     |
| **Event Hub Partition Count**           | Number of partitions in the Event Hub. This value must match or exceed the number of Cosmos DB instances generating traffic.                                                                                                                                                                       |
| **Enrollment ID**                       | Unique identifier for the Azure resource enrollment.                                                                                                                                                                                                                                    |
| **No-traffic threshold (minutes)**      | Default value is 60. If there is no incoming traffic for an hour, S-TAP displays a red status. Once incoming traffic resumes, the status returns to green.                                                                                                                               |
| **Use Enterprise Load Balancing (ELB)** | Enable this if ELB support is required.                                                                                                                                                                                                                                                  |
| **Managed Unit Count**                  | Number of Managed Units (MUs) to allocate for ELB.                                                                                                                                                                                                                                       |

**Note:**

- Ensure that the **profile name** is unique.
- Required credentials must be created before or during profile creation.
- The Azure credentials must have appropriate permissions to read from Event Hub.

---

## Azure Credential Configuration

When creating credentials for Azure Event Hub, provide the following information.

| Field name            | Description                                                     |
|-----------------------|-----------------------------------------------------------------|
| **Name**              | A unique credential name                                        |
| **Description**       | A description for your credential                               |
| **Credential Type**   | Azure Event Hub Credentials `Azure Event Hub Connection String` |
| **Connection String** | Event Hub connection string with manage permissions             |

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