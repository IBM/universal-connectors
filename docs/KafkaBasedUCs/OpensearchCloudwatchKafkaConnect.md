# Configuring Amazon OpenSearch datasource profiles for Kafka Connect Plug-ins

Create and configure datasource profiles through Central Manager for **Amazon OpenSearch over CloudWatch Kafka Connect** plug-ins.

### Meet Amazon OpenSearch over CloudWatch Connect

* Tested versions: v1
* Environments: AWS
* Supported inputs: Kafka Input (pull)
* Supported Guardium versions:
   * Guardium Data Protection: Appliance bundle 12.2.2 or later

Kafka-connect is a framework for streaming data between Apache Kafka and other systems. This connector enables monitoring of Amazon OpenSearch audit logs through CloudWatch.

## Configuring Amazon OpenSearch Service

### Before you begin

* [Prerequisites](https://docs.aws.amazon.com/opensearch-service/latest/developerguide/setting-up.html)

### Procedure

1. Go to https://console.aws.amazon.com/.
2. Search and navigate to ```Amazon OpenSearch Service```.
3. To create an OpenSearch domain, see [Getting started with Amazon OpenSearch Service guide](https://docs.aws.amazon.com/opensearch-service/latest/developerguide/gsg.html).

## Enabling Audit Logs

1. To Enable audit logs for **CloudWatch Logs** and **OpenSearch Dashboard**, see [Enabling Audit logs](https://docs.aws.amazon.com/opensearch-service/latest/developerguide/audit-logs.html#audit-log-enabling).

### Viewing Audit Logs on CloudWatch

By default, each OpenSearch domain has associated log groups with names in this format: 
- `/aws/OpenSearchService/<domain_name>/audit` 
- `/aws/OpenSearchService/<domain_name>/profiler`

1. Open the CloudWatch console https://console.aws.amazon.com/cloudwatch/.
2. In the navigation pane, choose **Log groups**.
3. Choose the **log group** that you specified while enabling audit logs. Within the log group, OpenSearch Service creates a log stream for each node in your domain.
4. In the **Log streams**, select **Search all**.
5. For the read and write events, see the corresponding logs. This process may take several seconds.

### Supported Audit Log Types

Cluster communication occurs over two separate layers: **REST layer** and **Transport layer**. The following is the list of **Audit log Categorie**s, with their availability determined by the communication layers:

* FAILED_LOGIN
* MISSING_PRIVILEGES
* BAD_HEADERS
* SSL_EXCEPTION
* GRANTED_PRIVILEGES
* OPENSEARCH_SECURITY_INDEX_ATTEMPT
* AUTHENTICATED
* INDEX_EVENT
* COMPLIANCE_DOC_READ
* COMPLIANCE_DOC_WRITE
* COMPLIANCE_INTERNAL_CONFIG_READ
* COMPLIANCE_INTERNAL_CONFIG_WRITE

For more information about the audit logging category and layers, see [Audit log layers and categories](https://docs.aws.amazon.com/opensearch-service/latest/developerguide/audit-logs.html#audit-log-layers).

For more information about the audit logging fields, see [Audit log field reference](https://docs.opensearch.org/docs/latest/security/audit-logs/field-reference/).

**Note:** OpenSearch generates a large volume of background audit logs by default. Configure the audit settings appropriately to limit unnecessary entries in the audit logs.

## Limitations

- Audit logging in OpenSearch can be accessed in two different ways – via the OpenSearch Dashboards or through CloudWatch Logs. However, this filter plugin only parses and processes audit logs that are streamed to CloudWatch. Audit logs stored directly in OpenSearch indices or viewed in the Dashboards are not supported for parsing.
- FAILED_LOGIN REST messages will appear in **Full SQL** and **Failed Logins** report.
- Certain reserved keywords (template, mappings, get, aliases, user) are automatically prefixed with an underscore (_) during sanitization to prevent OpenSearch URI parsing errors or endpoint conflicts.
- **ClientHostName** is not available in the audit logs for OpenSearch.

## Creating datasource profiles

You can create a new datasource profile from the **Datasource Profile Management** page.

### Procedure

1. Go to **Manage > Universal Connector > Datasource Profile Management**
2. Click the **➕ (Add)** button.
3. You can create a profile by using one of the following methods:

    - To **Create a new profile manually**, go to the **"Add Profile"** tab and provide values for the following fields.
        - **Name** and **Description**.
        - Select a **Plug-in Type** from the dropdown. For example, `Amazon OpenSearch Over Cloudwatch Connect 2.0`.

    - To **Upload from CSV**, go to the **"Upload from CSV"** tab and upload an exported or manually created CSV file containing one or more profiles.  
      You can also choose from the following options:
        - **Update existing profiles on name match** — Updates profiles with the same name if they already exist.
        - **Test connection for imported profiles** — Automatically tests connections after profiles are created.
        - **Use ELB** — Enables ELB support for imported profiles. You must provide the number of MUs to be used in the ELB process.

**Note:** Configuration options vary based on the selected plug-in.

## Configuring Amazon OpenSearch Over CloudWatch Kafka Connect 2.0

The following table describes the fields that are specific to Amazon OpenSearch over CloudWatch Kafka Connect 2.0 plugin.

| Field                    | Description                                                                                                                                                                                                                                                         |
|--------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Name**                 | Unique name of the profile.                                                                                                                                                                                                                                         |
| **Description**          | Description of the profile.                                                                                                                                                                                                                                         |
| **Plug-in**              | Plug-in type for this profile. Select `Amazon OpenSearch Over Cloudwatch Connect 2.0`. A full list of available plug-ins are available on the **Package Management** page.                                                                                         |
| **Credential**           | Select AWS Credentials or AWS Role ARN. The credential to authenticate with AWS. Must be created in **Credential Management**, or click **➕** to create one. For more information, see [Creating Credentials](https://www.ibm.com/docs/en/SSMPHH_12.x/com.ibm.guardium.doc.stap/guc/guc_credential_management.html). |
| **Kafka Cluster**        | Select the appropriate Kafka cluster from the available Kafka cluster list or create a new Kafka cluster. For more information, see [Managing Kafka clusters](https://www.ibm.com/docs/en/SSMPHH_12.x/com.ibm.guardium.doc.stap/guc/guc_kafka_cluster_management.html). |
| **Label**                | Grouping label. For example, customer name or ID.                                                                                                                                                                                                                   |
| **AWS account region**   | Specifies the AWS region where your OpenSearch domain is located (e.g., us-east-1, eu-west-1).                                                                                                                                                                     |
| **Log groups**           | List of CloudWatch log groups to monitor. These are the log groups where OpenSearch audit logs are exported. Format: `/aws/OpenSearchService/<domain_name>/audit`                                                                                                  |
| **Filter pattern**       | CloudWatch Logs filter pattern to apply. Use "None" to retrieve all logs, or specify a pattern to filter specific log events.                                                                                                                                      |
| **Account ID**           | Your AWS account ID (12-digit number). This identifies your AWS account.                                                                                                                                                                                           |
| **Cluster name**         | The name of your OpenSearch domain identifier.                                                                                                                                                                                                                      |
| **Ingestion delay (seconds)** | Default value is 900 seconds (15 minutes). This delay accounts for the time it takes for logs to be available in CloudWatch after being generated.                                                                                                            |
| **No-traffic threshold (minutes)** | Default value is 60. If there is no incoming traffic for an hour, S-TAP displays a red status. Once incoming traffic resumes, the status returns to green.                                                                                                    |
| **Use Enterprise Load Balancing (ELB)** | Enable this if ELB support is required.                                                                                                                                                                                                                             |
| **Managed Unit Count**   | Number of Managed Units (MUs) to allocate for ELB.                                                                                                                                                                                                                  |

**Note:**
- Ensure that the **profile name** is unique.
- Required credentials must be created before or during profile creation.
- The AWS credentials must have appropriate permissions to read CloudWatch logs.

---

## Testing connections

After creating a profile, you must test the connection to ensure that the provided configuration is valid.

### Procedure

1. Select the new profile.
2. From the top menu, click **Test Connection**.
3. If the test is successful, you can proceed to installing the profile.

---

## Installing profiles

Once the connection test is successful, you can install the profile on **Managed Units (MUs)** or **Edges**. The parsed audit logs are sent to the selected Managed Unit or Edge to be consumed by the **Sniffer**.

### Procedure

1. Select the profile.
2. From the **Install** menu, click **Install**.
3. A list of available MUs and Edges are displayed. Choose the specific MUs and Edges to which you want to apply the new profile.

---

## Uninstalling or reinstalling profiles

An installed profile can be uninstalled or reinstalled if needed.

### Procedure

1. Select the profile.
2. From the list of available actions, select the desired option **Uninstall** or **Reinstall**.

---
