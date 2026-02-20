# Configuring Azure PostgreSQL datasource profiles for Kafka Connect Plug-ins

reate and configure datasource profiles through central manager for **Azure
PostgreSQL over Event Hub Kafka Connect** plug-ins.

### Meet Azure PostgreSQL over Event Hub Connect

* Environment: Azure
* Supported inputs: Kafka Input (pull)
* Supported Guardium versions:
    * Guardium Data Protection: Appliance bundle 12.2.2 or later

Kafka-connect is a framework for streaming data between Apache Kafka and other systems. This connector enables
monitoring of Azure PostgreSQL audit logs through Azure Event Hub.

## Configuring the Azure PostgreSQL service

You can retrieve Azure PostgreSQL audit data in the following ways:

1. Azure Event Hub
2. Azure Storage
3. Log Analytics Workspace
4. Azure Partner Solution

This plug-in uses Azure Event Hub as the data streaming service.

### Procedure

There are multiple ways to install a Postgres server. For this example, we assume that you already have a working Azure PostgreSQL setup. For more information about the Azure Postgres setup, see [quickstart-create-server-portal](https://learn.microsoft.com/en-us/azure/postgresql/flexible-server/quickstart-create-server-portal).

**Note:** You can choose between a Single Server or a PostgreSQL Flexible Server deployment. The Single Server option is on the retirement path and will be deprecated in the future. For more information about the retirement schedule, see [Microsoft documentation](https://learn.microsoft.com/en-us/azure/postgresql/migrate/whats-happening-to-postgresql-single-server?wt.mc_id=searchAPI_azureportal_inproduct_rmskilling&sessionId=d1a1e6c6a39842e1bc0191329167d1c3).

## Enabling Auditing

1. On the Database auditing page, go to **Settings** and select **server** parameter.
2. Search for **shared_preload_libraries** in server parameter.
3. Select **shared_preload_libraries** as PGAUDIT and save.
4. Go to overview and restart the server to apply the changes.
5. After installation of pgAudit, you can configure its parameters to start logging.
6. On the Database auditing page, go to **Settings** > **server parameters** and set the following parameters.
        a. **log_checkpoints** = ``off``
        b. **log_error_verbosity** = ``VERBOSE``
        c. **log_line_prefix** = Specify as based on your requirement but should include timestamp, client ip, client port, database
          username, database name, process id, application name, sql state. For more information,
          see [Error Reporting and Logging](https://www.postgresql.org/docs/current/runtime-config-logging.html#GUC-LOG-LINE-PREFIX).
        d. **pgaudit.log** = ``DDL,FUNCTION,READ,WRITE,ROLE``
        e. **pgaudit.log_catalog** = ``off``
        f. **pgaudit.log_client** = ``off``
        g. **pgaudit.log_parameter** = ``off``
7. Click **Save**.

## Configuring Azure Event Hub

### Azure Event Hub Connection

1. Search for Storage Accounts in the search bar of the Azure portal. </br>
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
    f. Set the throughput units (or processing units for standard and premium tiers) as ``10`` to prevent data loss. You can
      update the throughput units as per your requirement (some pricing tiers provide Auto-inflate feature). In Azure, 1
      throughput unit handles incoming data of up to 1 MB/second/1000 events and outgoing data of up to 2MB/second/4096
      events.</br>
    g. Select **Review + Create**.</br>
    h. Review the settings and select **create**. </br>
    i. After successful creation, the recently created namespace appears in the resource group.</br>

5. To create an event hub, complete the following steps.
    1. Go to the Event Hubs Namespace page.
    2. Click **+ Event Hub** to add event hub.
    3. Enter a unique name for the event hub, then select **create**.

6. Connection string for an eventhub, complete the following stpes.
    a. From the list of event hubs, select your event hub.</br>
    b. On the Event Hubs instance page, go to **Settings** > **Shared access policies** > **Add**.</br>
    c. In shared access policies, click **Add**.</br>
    d. Enter a policy name and select **Manage** from the permission. Then create the policy.</br>
    e. Take the value of the primary key - connection string from the policy (this is required for the connector
       configuration).</br>
    f. The value of the primary key - connection string will be used in the connector configuration.</br>

7 Stream logs to an Event Hub</br>
   a. Login to https://portal.azure.com.</br>
   b. Go to server in your Azure Portal.</br>
   c. From **Monitoring**, select **Diagnostics settings**.</br>
   d. To change existing settings, select **Edit setting**.</br>
   e. To add new settings, select **Add diagnostics setting**. </br>
     &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;i. Enter a name for the setting.</br>
     &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;ii. Select **PostgreSQL Server Logs** from categories.</br>
     &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;iii. In **Destination details**, choose **Stream to an event hub**.</br>
     &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;iv. In **Stream to event hub**, select the **namespace name** and **event hub name** as created above*. Keep the event hub policy name as is.</br>
     &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;v. Click **Save**.</br>

8. After about 15 minutes, verify that the events are displayed in your event hub.

  
### Configuring multiple collectors

When UCs are configured on two separate Collectors, additional configurations are needed to monitor traffic from a single Event Hub.

1. Create a namespace in Azure Event Hub following the standard procedure, and select the **Standard pricing tier** instead of Basic in the pricing tier configuration.
2. After the namespace is created, create an Event Hub and generate a connection string as described in the previous steps.
3. After successfully creating the Event Hub, add a consumer group to the event hub. </br>
    a. From the list of Event Hubs, select your Event Hub. </br>
    b. On the Event Hub instance page, go to **Entities** > **Consumer group**. Then click **Add**. </br>
    c. Enter a name for the consumer group and create it. </br>
4. Configure log streaming to the Event Hub as described in the previous steps.
5. For gmachine, use the following consumer group names in the connector configurations. </br>
    a. Machine 1: Use the consumer group name ``$Default``.  </br>
    b. Machine 2: Use the name of the consumer group created in step 3. </br>
    c. Keep all other configuration settings as is. </br>

**Note:**

The following recommendations apply when configuring the plug-in to read from multiple event hubs. These guidelines are relevant in two scenarios:
- When multiple connection strings are specified in event_hub_connections.
- When multiple universal connector configurations exist on a single Guardium machine.

* When there is more than one connection string provided in the **event_hub_connections** parameter, define both
  **storage_connection** and **consumer_group** parameters. This helps to differentiate the files where timestamp offsets are
  written. The **azure_event_hubs** input does not store the offset locally. For more information, see [Best practices](https://www.elastic.co/guide/en/logstash/current/plugins-inputs-azure_event_hubs.html#plugins-inputs-azure_event_hubs-best-practices).

Not following this recommendation may result in data loss.

## Connecting to Azure PostgreSQL Database

1. Start the psql and provide connection details.
2. Enter the **server name** from the overview window of the server in the Azure portal.
3. Enter the database name as ``postgres``.
4. In the **Port** field, enter ``5432``.
5. Provide the **username**. From the Azure portal, click **overview** and copy the admin username and '
   password that you set when you created the database.
6. Click **Enter**.
7. Run the following command to give access to pgaudit.
   ```sql
   GRANT pg_read_all_settings TO <admin-username>;
   -- (use admin-username which is given at the time of creation of database)
   -- eg: GRANT pg_read_all_settings TO postgres;
   ```

## Limitations

- The Azure PostgreSQL plug-in does not support IPV6.
- The audit log does not contain a server IP. The **Server IP** is set to default value `0.0.0.0`.
- For failed logins, **SQL string** and **Source Program** is not available.
- Multiple records related to session timeout with `SESSION_ERROR` are seen in the exception log report.
- For primary or foreign key constraints violations, entries are added to both the SQL error report and the full SQL
  report.
- The source program appears blank in the report for some clients (in this case, values appear for psql and
  pgadmin but visual studio is blank).
- **Client Host Name** and **Server Port** is not available in the audit logs.
- SQL string that caused the Exception is not available in exception log report for sql errors.

## Creating datasource profiles

You can create a new datasource profile from the **Datasource Profile Management** page.

### Procedure

1. Go to **Manage > Universal Connector > Datasource Profile Management**
2. Click the **➕ (Add)** button.
3. You can create a profile by using one of the following methods:

    * To **Create a new profile manually**, go to the **"Add Profile"** tab and provide values for the following fields.
        * **Name** and **Description**.
        * Select a **Plug-in Type** from the dropdown. For example, `Azure PostgreSQL Over Event Hub Connect 2.0`.

    * To **Upload from CSV**, go to the **"Upload from CSV"** tab and upload an exported or manually created CSV file
      containing one or more profiles. You can also choose from the following options:
        * **Update existing profiles on name match** — Updates profiles with the same name if they already exist.
        * **Test connection for imported profiles** — Automatically tests connections after profiles are created.
        * **Use ELB** — Enables ELB support for imported profiles. You must provide the number of MUs to be used in the
          ELB process.

**Note:** Configuration options vary based on the selected plug-in.

## Configuring Azure PostgreSQL Over Event Hub Kafka Connect 2.0

The following table describes the fields that are specific to Azure Event Hub Kafka Connect 2.0 plugin.

| Field                                   | Description                                                                                                                                                                                                                                                                             |
|-----------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Name**                                | Unique name of the profile.                                                                                                                                                                                                                                                              |
| **Description**                         | Description of the profile.                                                                                                                                                                                                                                                              |
| **Plug-in**                             | Plug-in type for this profile. Select `Azure PostgreSQL Over Event Hub Connect 2.0`. A full list of available plug-ins are available on the **Package Management** page.                                                                                                                 |
| **Credential**                          | The credential to authenticate with Azure Event Hub. Must be created in **Credential Management**, or click **➕** to create one. For more information, see [Creating Credentials](https://www.ibm.com/docs/en/SSMPHH_12.x/com.ibm.guardium.doc.stap/guc/guc_credential_management.html). |
| **Kafka Cluster**                       | Select the appropriate Kafka cluster from the available Kafka cluster list or create a new Kafka cluster. For more information, see [Managing Kafka clusters](https://www.ibm.com/docs/en/SSMPHH_12.x/com.ibm.guardium.doc.stap/guc/guc_kafka_cluster_management.html).                  |
| **Label**                               | Grouping label. For example, customer name or ID.                                                                                                                                                                                                                                       |
| **Event Hub Connection String**         | Primary connection string from the Event Hub shared access policy. This connection string is obtained from the Azure Event Hub configuration.                                                                                                                              |
| **Storage Connection String**           | Connection string for the Azure Storage Account. Required when reading from multiple Event Hubs to track offsets.                                                                                                                                                                        |
| **Consumer Group**                      | Consumer group name for the Event Hub. Use `$Default` for single collector or create custom consumer groups for multiple collectors.                                                                                                                                                     |
| **Event Hub Partition Count**           | Number of partitions in the Event Hub. This value must match or exceed the number of database instances generating traffic.                                                                                                                                                                       |
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
