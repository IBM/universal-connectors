# Configuring WatsonX data source profiles for Kafka Connect plug-ins

You can create and configure datasource profiles through Central Manager for **WatsonX Kafka Connect** plug-ins.

## About WatsonX Kafka Connect

* **Environment:** IBM Cloud
* **Supported inputs:** Kafka Input (pull)
* **Supported Guardium versions:**
    * Guardium Data Protection: Appliance bundle 12.2.2 or later

Kafka Connect is a framework for streaming data between Apache Kafka and other systems. You can use this connector to monitor WatsonX audit logs through Kafka.

## Configuring WatsonX on IBM Cloud

### Setting up WatsonX

1. Log in to the WatsonX portal.
2. From the menu in the upper right, select **IBM Cloud Pak for Data**.
3. Under **Instances**, click **View all**.
4. Create a WatsonX instance or select an existing instance.
5. From **Access information**, note the access token.
6. From **Configuration**, note the instance ID.
7. In the upper right, click **Admin** > **Profile and settings**.
8. Click **API key**, and then generate a new API key.
9. Return to your instance, then click **Open** in the upper right to open the WatsonX dashboard.
10. From the menu on the left, click **Configurations** > **IBM Guardium** > **Enable** to enable audit logging. </br>
    **Note:** You must complete this step after you install the profile to avoid installation failure. 
11. From the menu on the left, click **Data Manager**.
12. Run queries.
13. Retrieve the SSL certificate by completing the following steps. </br>
    a. Log in to the CPD cluster from the terminal and run the following command: </br>
       ``` oc get secret ibm-lh-tls-secret -n cpd-instance -o jsonpath='{.data.ca\.crt}' | base64 --decode > ca.crt``` </br>
    
    b. Transfer the file to your local machine by running the following command outside your cluster: </br>
       ``` scp <user>@<fyre_ip>:ca.crt <local_system_path> ``` </br>

## Limitations

The WatsonX Kafka Connect plug-in has the following limitations:

* **Duplicate log entries:** Audit logs might contain duplicate entries due to the nature of the data streaming process.
* **Query operation support:** WatsonX does not support `UPDATE` and `DELETE` query operations for audit logging.
* **Authentication model:** The query engine does not require separate login authentication. Therefore `LOGIN FAILED` exceptions are not captured in the audit logs.
* **Access token expiration:** Access tokens expire after 20 minutes. While this limitation does not affect deployed connectors during normal operation, you must manually refresh tokens if the connector is redeployed after an extended period of inactivity.
* **ELB:** Currently, ELB is not supported because token expiration prevents reliable failover.
* **Single Subscriber Per Instance**: Only one connector can be deployed per WatsonX instance ID at a time, even across different Guardium systems. Running multiple connectors with the same instance ID or testing a connection while a connector is running causes subscription conflicts and failures.

## Configuring credentials

The following table describes the credential fields required for WatsonX authentication.

| Field                    | Description                                                                                                                                                                                                                                                         |
|--------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Credential Type**      | Select `WatsonX Credentials` as the credential type.                                                                                                                                                                                                                            |
| **API Key**              | The API key generated in step 8 of the WatsonX setup.                                                                       |
| **Access Token**         | The access token obtained from **Access information** in step 5. This token expires every 20 minutes. You can regenerate the token from the portal as needed.                                                     |
| **SSL Truststore**       | Upload the SSL certificate.
                                              

### Creating WatsonX credentials

### Procedure

1. Click **Manage > Universal Connector > Credential Management**.
2. Click the **➕ (Add)** button to create a new credential.
3. Provide the following information:

---

## Creating Datasource Profiles

You can create a new datasource profile from the **Datasource Profile Management** page.

### Procedure

1. Go to **Manage > Universal Connector > Datasource Profile Management**.
2. Click the **➕ (Add)** button.
3. You can create a profile by using one of the following methods:

    - To **Create a new profile manually**, go to the **"Add Profile"** tab and provide values for the following fields.
        - **Name** and **Description**.
        - From the dropdown, select a **Plug-in Type** . For example, `WatsonX over Kafka Connect 2.0`

    - To **Upload from CSV**, go to the **"Upload from CSV"** tab and upload an exported or manually created CSV file containing one or more profiles.
      You can also choose from the following options:
        - **Update existing profiles on name match** — Updates profiles with the same name if they already exist.
        - **Test connection for imported profiles** — Automatically tests connections after profiles are created.
        - **Use ELB** — Enables ELB support for imported profiles. You must provide the number of MUs to be used in the ELB process.

**Note:** Configuration options vary based on the selected plug-in.

## Configuring WatsonX Kafka Connect 2.0

The following table describes the fields that are specific to WatsonX Kafka Connect 2.0 plugin.

| Field                    | Description                                                                                                                                                                                                                                                         |
|--------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Name**                 | Unique name of the profile.                                                                                                                                                                                                                                         |
| **Description**          | Description of the profile.                                                                                                                                                                                                                                         |
| **Plug-in**              | Plug-in type for this profile. Select **WatsonX over Kafka Connect 2.0**. A complete list of available plug-ins is available on the **Package Management** page.                                                                                        |
| **Credential**           | The credential to authenticate with WatsonX. The credential must be created in **Credential Management**, or click **➕** to create one. For more information, see [Creating Credentials](https://www.ibm.com/docs/en/SSMPHH_12.x/com.ibm.guardium.doc.stap/guc/guc_credential_management.html). |
| **Kafka Cluster**        | Select the appropriate Kafka cluster from the available Kafka cluster list or create a new Kafka cluster. For more information, see [Managing Kafka clusters](https://www.ibm.com/docs/en/SSMPHH_12.x/com.ibm.guardium.doc.stap/guc/guc_kafka_cluster_management.html). |
| **Label**                | Grouping label. For example, customer name or ID.                                                                                                                                                                                                                   |
| **Username**     | The username that you use to log in to WatsonX.                                                                                                                                               |
| **WatsonX URL**  | The URL of your WatsonX cluster.                                                                                                                                                                                     |
| **Instance ID**          | The instance ID that you obtained from step 6.                                                                                                         |
| **No-traffic threshold (minutes)** | The default value is 60. If no incoming traffic is received for an hour, S-TAP displays a red status. When incoming traffic resumes, the status returns to green.                                                                                                    |
| **Use Enterprise Load Balancing (ELB)** | Enable this option if ELB support is required.                                                                                                                                                                                                                             |
| **Managed Unit Count**   | Number of Managed Units (MUs) to allocate for ELB.                                                                                                                                                                                                                  |

**Note:**
- Make sure that the **profile name** is unique.

## Testing a connection

After you create a profile, test the connection to make sure that the configuration is valid.

### Procedure

1. Select the new profile.
2. From the menu, click **Test Connection**.
3. If the test is successful, you can install the profile.

---

## Installing a profile

After the connection test is successful, you can install the profile on Managed Units (MUs) or Edges. The parsed audit logs are sent to the selected Managed Unit or Edge to be consumed by the Sniffer.

### Procedure

1. Select the profile.
2. From the **Install** menu, click **Install**.
3. From the list of available MUs and Edges, select the ones where you want to deploy the profile.
4. After the profile is installed, complete step 10 from the WatsonX setup section to enable audit logging.

---

## Uninstalling or reinstalling profiles

You can uninstall or reinstall an installed profile if needed.

### Procedure

1. Select the profile.
2. From the list of available actions, select **Uninstall** or **Reinstall**.