# Configuring Azure Databricks datasource profiles for JDBC Kafka Connect Plug-ins

Create and configure datasource profiles through Central Manager for **Azure Databricks JDBC Kafka Connect** plug-ins.

## Meet Azure Databricks Over JDBC Connect

* Environments: Azure
* Supported inputs: Azure Event Hub (pull)
* Supported Guardium versions:
    * Guardium Data Protection: Appliance bundle 12.2.2 or later.

Kafka-connect is a framework for streaming data between Apache Kafka and other systems.

## Configuring the Azure Databricks

1. Login to https://portal.azure.com.
2. From the **Azure portal** menu or the home page, create an Azure Databricks DB account.
    - If you are on the home page, select **Create a resource**.
    - On the new page, search for and select **Azure Databricks**.
3. Select **Subscription** and **Resource group**. Keep the other tab values as is. Then click **Review + Create**.
4. Review the settings, and then select **Create**. It takes a few minutes to create the account. Wait for the portal page to display ``Your deployment is complete`` before proceeding to the next step.
5. Go to the Azure Databricks page by clicking **Go to resource**.

## Azure Event Hub connection

1. In the search bar, enter ``Event hub``.
2. Select **Create event hubs namespace**. 
3. To create a namespace, complete the following steps.</br>
    a. Select the subscription in which you want to create the namespace.</br>
    b. Select the resource group that you created in the previous step.</br>
    c. Enter a unique name for the namespace.</br>
    d. Select a location for the namespace.</br>
    e. Choose the appropriate pricing tier. (In this example, we selected **basic**).</br>
    f. Leave the throughput units (or processing units for standard and premium tier) settings as is.</br>
    g. Select **Review + Create**. Review the settings and select **Create**.</br>
    h. Your recently created namespace appears in **resource group**.</br>
4. To create an event hub, complete the following steps.</br>
    a. Go to the Event Hubs Namespace page. </br>
    b. Click **+ Event Hub**.</br>
    c. Enter a unique name for the event hub.</br>
    d. Choose the maximum number of partitions that you expect to require during peak usage for this event hub.</br>
      For example, if you want to generate traffic from 2 DB instances, then choose at least 2 partitions if not more.</br>
    e. Click **Review + create**.</br>
    f. Review the settings and click **Create**.</br>
5. Connection string for an event hub.</br>
    a. In the list of event hubs, select your event hub. </br>
    b. On the Event Hubs instance page, go to **Settings** > **Shared access policies** > **Add**.</br> 
    c. Name the policy, click **Manage** to provide permissions, and create the policy.</br>
    d. Select **Connection string–primary key** from policy (this string is required in the input plugin).</br>

6. Azure Storage Accounts Creation:</br>
    a. Login to https://portal.azure.com.</br>
    b. Search Storage accounts in search bar.</br>
    c. Click **Create**.</br>
    d. **Basic** Tab:</br>
        &nbsp;&nbsp;&nbsp;&nbsp;- Select the subscription in which you want to create the storage account.</br>
        &nbsp;&nbsp;&nbsp;&nbsp;- Select an existing resource group or create a new one.</br>
        &nbsp;&nbsp;&nbsp;&nbsp;- Enter a unique name for the storage account.</br>
        &nbsp;&nbsp;&nbsp;&nbsp;- Select the same region for the storage account that you selected for the server.</br>
       &nbsp;&nbsp;&nbsp;&nbsp; - Choose any performance type.</br>
        &nbsp;&nbsp;&nbsp;&nbsp;- Select **Geo-redundant(GRS) Redundancy configuration**.</br>
        &nbsp;&nbsp;&nbsp;&nbsp;- Select **Make read access to data**.</br>
        &nbsp;&nbsp;&nbsp;&nbsp;- Click **Next:Advance**.</br>
    e. **Advanced** tab:</br>
        &nbsp;&nbsp;&nbsp;&nbsp;- **Require secure transfer** should already be selected.</br>
        &nbsp;&nbsp;&nbsp;&nbsp;- **Allow enabling public access** should already be selected.</br>
        &nbsp;&nbsp;&nbsp;&nbsp;- **Enable storage account key access** should already be selected.</br>
        &nbsp;&nbsp;&nbsp;&nbsp;- Select the latest TLS version.</br>
        &nbsp;&nbsp;&nbsp;&nbsp;- Permitted scope should display the default value (from any storage account).</br>
        &nbsp;&nbsp;&nbsp;&nbsp;- The remaining parameters (Hierarchical Namespace, Access protocols, Blob storage, and Azure Files) should display the default values provided by Azure.</br>
        &nbsp;&nbsp;&nbsp;&nbsp;- Click **Next:Networking**.</br>
    f. **Networking** tab:</br>
        &nbsp;&nbsp;&nbsp;&nbsp;- Enable public access from all networks for **Network access**.</br>
        &nbsp;&nbsp;&nbsp;&nbsp;- Select **Microsoft network routing** for **Routing preference**.</br>
        &nbsp;&nbsp;&nbsp;&nbsp;- Click **Next:Data protection**.</br>
    g. **Data protection** tab:</br>
        &nbsp;&nbsp;&nbsp;&nbsp;- Keep the default values provided by Azure.</br>
        &nbsp;&nbsp;&nbsp;&nbsp;- Click **Next:Encryption**.</br>
    h. **Encryption** tab:</br>
        &nbsp;&nbsp;&nbsp;&nbsp;- **Encryption type** should already be set to **Microsoft-managed key(MMK)**.</br>
        &nbsp;&nbsp;&nbsp;&nbsp;- **Enable support for customer-managed keys** should be set to the default value (**blobs and files**).</br>
        &nbsp;&nbsp;&nbsp;&nbsp;- By default, **Infrastructure encryption** should not be enabled.</br>
        &nbsp;&nbsp;&nbsp;&nbsp;- Click **Next:Tags**.</br>
    i. For the **Tags** tab, make no changes and click **Next:Review**.</br>
    j. Click **Create** after you review all the parameters.</br>



##  Link event hub to Databricks

1. Login to https://portal.azure.com.
2. Navigate to your Azure Databricks. Open the **Diagnostic settings** pane under the **Monitoring** section to create a new diagnostic setting.
3. In the **Diagnostic settings** pane, fill in the form with your preferred categories.
4. Select your categories details, and then send your logs to your preferred destination. In this example, **Stream to an event hub** is selected. Then put your prefered event hub information in.
5. Launch your Databricks Workspace and go to **Profile** at top right corner.
6. Click **Settings** > **Advanced**, then search for **Verbose Audit Logs** and turn it on.

## Connecting to the Azure Databricks

### Insert/Update data through Data Explorer

1. Login to https://portal.azure.com.
2. Navigate to your Azure Databricks. Launch Workspace.
3. Under **SQL Editor**,  you can run sql command by creating new quert scripts.


#### Limitations
1. The following fields are not available in the audit logs from Azure Databricks: **Database name**, **ProtocolVersion**, **AppUserName**, **Client mac**, **Common Protocol**, **Os User**, **ClientOs**, **ServerOs**.
2. The log with sql execution does not display **Client IP**, but it can be found in another log with the **commandFinish** action.
3. Eventhub takes 10~30 minutes to receive raw logs from Databricks. The same delay time for Guardium is expected.
4. The Databricks auditing does not audit authentication failure (Login Failed) operations.
   
## Creating datasource profiles

You can create a new datasource profile from the **Datasource Profile Management** page.

### Procedure

1. Go to **Manage > Universal Connector > Datasource Profile Management**
2. Click the **➕ (Add)** button.
3. You can create a profile by using one of the following methods:

    * To **Create a new profile manually**, go to the **"Add Profile"** tab and provide values for the following fields.
        * **Name** and **Description**.
        * Select a **Plug-in Type** from the dropdown. For example, **Azure Databricks Over JDBC Kafka Connect 2.0**.

    * To **Upload from CSV**, go to the **"Upload from CSV"** tab and upload an exported or manually created CSV file
      containing one or more profiles. You can also choose from the following options:
        * **Update existing profiles on name match** — Updates profiles with the same name if they already exist.
        * **Test connection for imported profiles** — Automatically tests connections after profiles are created.
        * **Use ELB** — Enables ELB support for imported profiles. You must provide the number of MUs to be used in the
          ELB process.

**Note:** Configuration options vary based on the selected plug-in.

## Configuration: JDBC Kafka Connect 2.0-based Plugins

The following table describes the fields that are specific to JDBC Kafka Connect 2.0 and similar plugins.

| Field                         | Description                                                                                                                                                        |
|-------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Name**                      | Unique name of the profile.                                                                                                                                        |
| **Description**               | Description of the profile.                                                                                                                                        |
| **Plug-in**                   | Plug-in type for this profile. A full list of available plug-ins are available on the **Package Management** page.                                                 |
| **Credential**                | The credential to authenticate with the datasource. Must be created in **Credential Management**, or click **➕** to create one.                                    |
| **Kafka Cluster**             | Kafka cluster to deploy the universal connector.                                                                                                                   |
| **Label**                     | Grouping label. For example, customer name or ID.                                                                                                                  |
| **JDBC Driver Library**       | JDBC driver for the database.                                                                                                                                      |
| **Port**                      | Port that is used to connect to the database.                                                                                                                      |
| **Hostname**                  | Hostname of the database.                                                                                                                                          |
| **Query**                     | SQL query that is used to extract audit logs.                                                                                                                      |
| **Service Name / SID**        | The database **service name** or **SID**.                                                                                                                          |
| **Initial Time**              | Initial polling time for audit logs.                                                                                                                               |
| **No Traffic Threshold**      | Threshold setting for inactivity detection.                                                                                                                        |
| **Connection URL**            | Full JDBC connection string. Format varies by database type. <br/> For example, `jdbc:sqlserver://myserver.database.windows.net:1433;database=mydb;encrypt=true;`. |
| **Use Enterprise Load Balancing (ELB)** | Enable this if ELB support is required.                                                                                                                            |
| **Managed Unit Count**        | Number of Managed Units (MUs) to allocate for ELB.                                                                                                                 |

**Note:**
- Depending on the plugin type, the configuration may require either:
    - A **Connection URL**, or
    - Separate fields for **Hostname**, **Port**, and **Service Name / SID**
- Ensure that the **profile name** is unique.
- Required credentials must be created before or during profile creation.

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
