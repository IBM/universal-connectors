# Configuring AWS Athena Iceberg datasource profiles for Kafka Connect plug-ins

Create and configure datasource profiles through Central Manager for **AWS Athena Iceberg over CloudWatch Connect 2.0** plug-in.

## Meet AWS Athena Iceberg over CloudWatch Connect

* **Environment:** AWS
* **Supported inputs:** Kafka Connect CloudWatch 2.0 (pull)
* **Supported Guardium versions:**
    * Guardium Data Protection: Appliance bundle 12.2.4 or later

Kafka Connect is a framework for streaming data between Apache Kafka and other systems. This connector enables monitoring of AWS Athena query activity on Iceberg tables through CloudWatch. Because CloudTrail omits the full query string from Athena events by default, a Lambda enricher function is required to retrieve the complete SQL and write it to a separate enriched log group before the connector reads it.

**Note:** This connector requires an Iceberg-format table. Regular Hive tables will not exercise the Iceberg code path. The `table_type = 'ICEBERG'` property must be set when creating the table.

## Configuring AWS

### 1. Enabling CloudTrail Management Events

Navigate to **CloudTrail** and enable **Management Events** on your trail.

### 2. Creating an S3 Bucket

Navigate to **S3** and create a new bucket. This bucket is used for Athena query results and the Iceberg table data.

### 3. Configuring the Athena Query Result Location

Navigate to **Amazon Athena → Query Settings**.
Set **Query result location** to the bucket you created in step 2.

### 4. Creating an Iceberg Test Table

In the Athena Query Editor, create a database and an Iceberg-format table. Replace `your-bucket` with the bucket name from step 2.

```sql
CREATE DATABASE IF NOT EXISTS iceberg_db;

CREATE TABLE iceberg_db.orders (
  order_id  INT,
  customer  STRING,
  amount    DOUBLE
)
LOCATION 's3://your-bucket/iceberg/orders/'
TBLPROPERTIES ('table_type' = 'ICEBERG');
```

Confirm the table was created:

```sql
SHOW TABLES IN iceberg_db;
```

**Note:** The `table_type = 'ICEBERG'` property is required. Regular Hive tables will not exercise the Iceberg code path.

### 5. Creating the Lambda Enricher Function

Navigate to **Lambda** and create a new function (for example, `athena-audit-enricher`).

Paste the contents of [`lambda.py`](lambda.py) into the function code editor and click **Deploy**.

After deploying, set the following **environment variables** on the Lambda (**Configuration → Environment variables → Edit**):

| Variable | Example value | Description |
|---|---|---|
| `REGION` | `us-east-1` | The AWS region where the CloudTrail trail is configured. |
| `ENRICHED_LOG_GROUP` | `/aws/cloudtrail/athena-audit-enriched` | The CloudWatch log group where the Lambda writes enriched events. Use a unique name to avoid duplication. |
| `ENRICHED_LOG_STREAM` | `enriched-stream` | The log stream within the enriched log group. |

### 6. Increasing the Lambda Timeout

In the Lambda console, go to **Configuration → General configuration → Edit**.
Set **Timeout** to at least **30 seconds** and click **Save**.

**Note:** The default timeout is 3 seconds. If Athena's `GetQueryExecution` call is slow due to a cold start or throttling, the Lambda will time out and write nothing to the enriched log group.

### 7. Granting Lambda IAM Permissions

Navigate to **IAM → Roles** and open the execution role attached to your Lambda (visible on the Lambda's **Configuration → Permissions** tab).

Click **Add permissions → Create inline policy**, paste the following JSON, name it `AthenaIcebergLambdaPolicy`, and click **Create policy**:

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": "athena:GetQueryExecution",
      "Resource": "*"
    },
    {
      "Effect": "Allow",
      "Action": [
        "logs:CreateLogGroup",
        "logs:CreateLogStream",
        "logs:PutLogEvents"
      ],
      "Resource": "arn:aws:logs:*:*:log-group:*:*"
    }
  ]
}
```

**Note:** Without `athena:GetQueryExecution`, the Lambda cannot retrieve the real query string and will write `UNAVAILABLE` instead.

### 8. Wiring CloudWatch Logs to the Lambda (Subscription Filter)

Navigate to **CloudWatch → Log groups** and open the CloudTrail log group associated with your trail (for example, `/aws/cloudtrail`).

1. Click the **Subscription filters** tab → **Create Lambda subscription filter**.
2. Under **Lambda function**, select the Lambda you created in step 5.
3. Set a **Filter pattern** of `{ $.eventSource = "athena.amazonaws.com" }` to reduce noise.
4. Give the filter a name (for example, `athena-cloudtrail-to-lambda`) and click **Start streaming**.

**Note:** Without this step, the Lambda is never invoked. Athena events will arrive in CloudTrail but the enriched log group will remain empty.

### 9. Configuring the UC Profile Log Group

Point the UC connector at the **enriched** log group (the Lambda's output), not the raw CloudTrail log group. Use the same value you set in the `ENRICHED_LOG_GROUP` environment variable on the Lambda.

Ensure that the AWS credentials supplied to the connector have the following permissions on the enriched log group:
- `logs:GetLogEvents`
- `logs:DescribeLogStreams`

**Note:** If the connector points at the raw CloudTrail log group, it will receive events where `queryString` is `***OMITTED***` and the SQL will not be visible in Guardium.

## Viewing the Audit Logs

Athena audit events are captured by CloudTrail and forwarded to CloudWatch via the subscription filter and Lambda enricher. The enriched log group contains one event per query with the following key fields:

| Field | Description |
|---|---|
| `requestParameters.queryString` | The full SQL statement that was executed. |
| `requestParameters.queryStatus` | The query result: `SUCCEEDED` or `FAILED`. |
| `requestParameters.queryExecutionContext.database` | The Athena database against which the query ran. |
| `userIdentity` | Details of the IAM principal that ran the query. |
| `sourceIPAddress` | The IP address of the client that submitted the query. |
| `eventTime` | The time the event was recorded in CloudTrail. |

For the full CloudTrail event structure for Athena, see the [AWS CloudTrail documentation for Athena](https://docs.aws.amazon.com/athena/latest/ug/logging-using-cloudtrail.html).

## Limitations

- The connector requires a Lambda enricher function. Without it, `queryString` is `***OMITTED***` in CloudTrail events and the SQL cannot be parsed.
- Lambda cold starts or throttling can delay enriched log entries. If entries are missing, check the Lambda logs in CloudWatch and verify that the timeout is at least 30 seconds.
- The `table_type = 'ICEBERG'` property is required on the Athena table. Queries against standard Hive tables will not be captured on the Iceberg code path.

## Creating datasource profiles

You can create a new datasource profile from the **Datasource Profile Management** page.

### Procedure

1. Go to **Manage > Universal Connector > Datasource Profile Management**.
2. Click the **➕ (Add)** button.
3. You can create a profile by using one of the following methods:

    - To **create a new profile manually**, go to the **Add Profile** tab and provide values for the following fields:
        - **Name** and **Description**.
        - Select a **Plug-in Type** from the dropdown. For example, `AWS Athena Iceberg Over Cloudwatch Connect 2.0`.

    - To **upload from CSV**, go to the **Upload from CSV** tab and upload an exported or manually created CSV file containing one or more profiles. You can also choose from the following options:
        - **Update existing profiles on name match** — Updates profiles with the same name if they already exist.
        - **Test connection for imported profiles** — Automatically tests connections after profiles are created.
        - **Use ELB** — Enables ELB support for imported profiles. You must provide the number of MUs to be used in the ELB process.

**Note:** Configuration options vary based on the selected plug-in.

## Configuring AWS Athena Iceberg Over CloudWatch Connect 2.0

The following table describes the fields that are specific to the AWS Athena Iceberg Over CloudWatch Connect 2.0 plug-in.

| Field | Description |
|---|---|
| **Name** | Unique name of the profile. |
| **Description** | Description of the profile. |
| **Plug-in** | Plug-in type for this profile. Select `AWS Athena Iceberg Over Cloudwatch Connect 2.0`. A full list of available plug-ins is available on the **Package Management** page. |
| **Credential** | Select AWS Credentials or AWS Role ARN. The credential to authenticate with AWS. Must be created in **Credential Management**, or click **➕** to create one. For more information, see [Creating Credentials](https://www.ibm.com/docs/en/SSMPHH_12.x/com.ibm.guardium.doc.stap/guc/guc_credential_management.html). |
| **Kafka Cluster** | Select the appropriate Kafka cluster from the available Kafka cluster list or create a new Kafka cluster. For more information, see [Managing Kafka clusters](https://www.ibm.com/docs/en/SSMPHH_12.x/com.ibm.guardium.doc.stap/guc/guc_kafka_cluster_management.html). |
| **Label** | Grouping label. For example, customer name or ID. |
| **AWS account region** | The AWS region where your Athena workgroup and CloudTrail trail are located (for example, `us-east-1`). |
| **Log groups** | The enriched CloudWatch log group to monitor — the output of the Lambda enricher, not the raw CloudTrail log group. Use the same value as the `ENRICHED_LOG_GROUP` Lambda environment variable. |
| **Filter pattern** | CloudWatch Logs filter pattern to apply. Use `None` to retrieve all logs, or specify a pattern to filter specific log events. |
| **Account ID** | Your AWS account ID (12-digit number). |
| **Cluster name** | The Athena workgroup or table identifier. |
| **Ingestion delay (seconds)** | Default value is 900 seconds (15 minutes). This delay accounts for the time it takes for logs to be available in CloudWatch after being generated. |
| **No-traffic threshold (minutes)** | Default value is 60. If there is no incoming traffic for an hour, S-TAP displays a red status. When incoming traffic resumes, the status returns to green. |
| **Unmask sensitive value** | Optional boolean flag. When enabled, sensitive values in the audit logs will not be masked. |
| **Use Enterprise Load Balancing (ELB)** | Enable this if ELB support is required. |
| **Managed Unit Count** | Number of Managed Units (MUs) to allocate for ELB. |

**Notes:**
- Ensure that the **profile name** is unique.
- Required credentials must be created before or during profile creation.
- The AWS credentials must have `logs:GetLogEvents` and `logs:DescribeLogStreams` permissions on the enriched log group.

---

## Testing a Connection

After you create a profile, test the connection to ensure that the configuration is valid.

### Procedure

1. Select the new profile.
2. From the top menu, click **Test Connection**.
3. If the test is successful, proceed to installing the profile.
4. If the test fails, verify the following items:
   - The AWS credentials have the required CloudWatch permissions on the enriched log group.
   - The enriched log group name matches the `ENRICHED_LOG_GROUP` Lambda environment variable.
   - The Kafka cluster is accessible.

---

## Installing a Profile

Once the connection test is successful, you can install the profile on **Managed Units (MUs)** or **Edges**. The parsed audit logs are sent to the selected Managed Unit or Edge to be consumed by the Sniffer.

### Procedure

1. Select the profile.
2. From the **Install** menu, click **Install**.
3. From the list of available MUs and Edges, select the ones where you want to deploy the profile.

---

## Uninstalling or reinstalling profiles

You can uninstall or reinstall an installed profile.

### Procedure

1. Select the profile.
2. From the list of available actions, select **Uninstall** or **Reinstall**.

---

## Troubleshooting

### The enriched log group is empty

1. Confirm that the CloudWatch subscription filter is attached to the raw CloudTrail log group and targets the Lambda function.
2. Run an Athena query and wait 30–60 seconds, then check the enriched log group.
3. Check the Lambda function's own CloudWatch logs for errors:
   - Navigate to **CloudWatch → Log groups** and open `/aws/lambda/<function-name>`.
   - Look for timeout errors (increase the Lambda timeout to at least 30 seconds) or permission errors (verify the IAM policy includes `athena:GetQueryExecution`).

### The `queryString` field shows `UNAVAILABLE`

The Lambda failed to call `GetQueryExecution`. Verify that the Lambda execution role includes the `athena:GetQueryExecution` IAM permission.

### Logs are not appearing in Guardium

1. Confirm that the UC profile **Log groups** field points to the enriched log group, not the raw CloudTrail log group.
2. Verify that the enriched log group contains events with `queryString` showing the real SQL (not `***OMITTED***`).
3. Confirm that the AWS credentials attached to the profile have `logs:GetLogEvents` and `logs:DescribeLogStreams` on the enriched log group.
4. Check the Kafka Connect logs for connector errors.

### Connection test fails

1. Verify that the AWS credentials are valid and have not expired.
2. Confirm that the log group name is correct and exists in the specified region.
3. Verify that the Kafka cluster is accessible.

---
