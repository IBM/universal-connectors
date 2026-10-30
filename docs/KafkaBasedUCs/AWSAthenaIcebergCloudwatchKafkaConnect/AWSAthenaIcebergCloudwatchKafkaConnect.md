# Configuring AWS Athena Iceberg datasource profiles for Kafka Connect plug-ins

Create and configure datasource profiles by using central manager for **AWS Athena Iceberg over CloudWatch Connect 2.0** plug-in.

## Meet AWS Athena Iceberg over CloudWatch Connect

* **Environment:** AWS
* **Supported inputs:** Kafka Connect CloudWatch 2.0 (pull)
* **Supported Guardium versions:**
    * Guardium Data Protection: Appliance bundle 12.2.4 or later

Kafka Connect is a framework for streaming data between Apache Kafka and other systems. This connector enables monitoring of AWS Athena query activity on Iceberg tables through CloudWatch. Because CloudTrail omits the full query string from Athena events by default, a Lambda enricher function is required to retrieve the complete SQL and write it to a separate enriched log group before the connector reads it.

**Note:** This connector requires an Apache Iceberg table. Standard Hive tables do not use the Iceberg code path. You must set the `table_type = 'ICEBERG'` table property when you create the table.

## Configuring AWS

1. Go to **CloudTrail** and enable **Management events** on your trail.

2. Go to **S3** and create a new bucket. This bucket stores the Athena query results and the Iceberg table data.

3. Configure the Athena query result location.</br>
      a. Go to **Amazon Athena > Query Settings**. </br>
      b. In the **Query result location** field, specify the S3 bucket that you created in step 2. </br>

4. In the **Athena query editor**, create a database and an Apache Iceberg table. 
In the following example, replace `your-bucket` with the S3 bucket name that you created in step 2:

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

      To verify that the table is created, run the following command:

      ```sql
      SHOW TABLES IN iceberg_db;
      ```

      **Note:** The `table_type = 'ICEBERG'` table property is required. Regular Hive tables do not use the Iceberg code path.

5. Create the Lambda Enricher function. </br>
      a. In the AWS Lambda console, create a new function (for example, `athena-audit-enricher`). </br>
      b. Paste the contents of [`lambda.py`](lambda.py) into the function code editor, and then click **Deploy**. </br>
      c. On the Configuration tab, click **Environment variables > Edit**, and configure the following variables: </br>

   | Variable | Example value | Description |
   |---|---|---|
   | `REGION` | `us-east-1` | The AWS region where the CloudTrail trail is configured. |
   | `ENRICHED_LOG_GROUP` | `/aws/cloudtrail/athena-audit-enriched` | The CloudWatch log group where the Lambda function writes enriched events. Enter a unique name to avoid duplicate entries. |
   | `ENRICHED_LOG_STREAM` | `enriched-stream` | The log stream name within the enriched log group. |

6. Increase the Lambda function timeout. </br>
      a. In the AWS Lambda console, go to **Configuration > General configuration > Edit**.</br>
      b. In the **Timeout** field, specify at least `30` seconds, and then click **Save**.</br>
      
      **Note:** The default timeout is 3 seconds. If the Athena `GetQueryExecution` call is delayed by a cold start or throttling, the Lambda function times out without writing events to the enriched log group.

7. Grant IAM permissions to the Lambda function. </br>
      a. In the AWS IAM console, go to **Roles**, and open the execution role that is attached to your Lambda function (listed on the **Configuration** > **Permissions** tab in the Lambda console). </br>
      b. Click **Add permissions > Create inline policy**.</br>
      c. Select the **JSON** tab, paste the following policy, enter `AthenaIcebergLambdaPolicy` for the policy name, and then click **Create policy**. </br>

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

      **Note:** Without the `athena:GetQueryExecution` permission, the Lambda function cannot retrieve the actual query string and records `UNAVAILABLE` instead.

8. Configure a CloudWatch Logs subscription filter for the Lambda function. </br>
      a. In the AWS CloudWatch console, go to **Log groups**, and open the CloudWatch log group that is associated with your CloudTrail trail (for example, `/aws/cloudtrail`). </br>
      b. Go to the **Subscription filters** tab, then click **Create Lambda subscription filter**. </br> 
      c. Under **Lambda function**, select the Lambda function that you created in step 5. </br>
      d. In the **Filter pattern** field, enter `{ $.eventSource = "athena.amazonaws.com" }` to capture only Athena events. </br>
      e. Enter a filter name (for example, `athena-cloudtrail-to-lambda`), then click **Start streaming**.

      **Note:** If you do not configure this subscription filter, the Lambda function is not invoked. CloudTrail captures Athena events, but the enriched log group remains empty.

9. Configure the universal connector profile log group. </br>
         a. Configure the universal connector to read from the **enriched** CloudWatch log group (the Lambda function output), not the raw CloudTrail log group. Specify the same log group name that you set in the `ENRICHED_LOG_GROUP` environment variable on the Lambda function in step 5. </br>
         b. Verify that the AWS credentials configured for the universal connector have the following permissions on the enriched log group: </br>
&emsp;                  - `logs:GetLogEvents`</br>
&emsp;                  - `logs:DescribeLogStreams`</br>

    **Note:** If the universal connector is configured to read from the raw CloudTrail log group, the connector receives events where `queryString` is `***OMITTED***`, and the SQL statements are not displayed in Guardium.
   

## Viewing the audit Logs

AWS CloudTrail captures Athena audit events and forwards them to CloudWatch through the subscription filter and the Lambda enricher function. The enriched log group contains one event for each query with the following fields:

| Field | Description |
|---|---|
| `requestParameters.queryString` | The full SQL statement that was executed. |
| `requestParameters.queryStatus` | The query result: `SUCCEEDED` or `FAILED`. |
| `requestParameters.queryExecutionContext.database` | The Athena database where the query was run. |
| `userIdentity` | Details of the IAM principal that ran the query. |
| `sourceIPAddress` | The IP address of the client that submitted the query. |
| `eventTime` | The timestamp when the event was recorded in CloudTrail. |

For more information about the CloudTrail event structure for Athena, see [AWS CloudTrail documentation for Athena](https://docs.aws.amazon.com/athena/latest/ug/logging-using-cloudtrail.html).

## Limitations

- The connector requires a Lambda enricher function. Without this function, the `queryString` field is set to `***OMITTED***` in CloudTrail events, and the SQL statements cannot be parsed.
- Lambda function cold starts or throttling can delay enriched log entries. If log entries are missing, check the Lambda function logs in CloudWatch and verify that the function timeout is set to at least 30 seconds.
- The `table_type = 'ICEBERG'` table property is required on the Athena table. Queries against standard Hive tables are not captured on the Iceberg code path.

## Creating datasource profiles

You can create a new datasource profile from the **Datasource Profile Management** page.

### Procedure

1. Go to **Manage > Universal Connector > Datasource Profile Management**.
2. Click the **➕ (Add)** button.
3. Select one of the following methods to create a profile:

    - To **create a new profile manually**, go to the **Add Profile** tab and provide values for the following fields:
        - **Name** and **Description**.
        - Select a **Plug-in Type** from the dropdown. For example, `AWS Athena Iceberg Over Cloudwatch Connect 2.0`.

    - To **upload from CSV**, go to the **Upload from CSV** tab and upload an exported or manually created CSV file containing one or more profiles. You can also choose from the following options:
        - **Update existing profiles on name match** — Updates profiles with the same name if they already exist.
        - **Test connection for imported profiles** — Automatically tests connections after profiles are created.
        - **Use ELB** — Enables ELB support for imported profiles. You must provide the number of MUs to be used in the ELB process.

**Note:** Configuration options vary based on the selected plug-in.

## Configuring AWS Athena Iceberg over CloudWatch Connect 2.0

The following table describes the configuration fields for the AWS Athena Iceberg Over CloudWatch Connect 2.0 plug-in.

| Field | Description |
|---|---|
| **Name** | Unique name of the profile. |
| **Description** | Description of the profile. |
| **Plug-in** | Plug-in type for this profile. Select `AWS Athena Iceberg Over Cloudwatch Connect 2.0`. A complete list of available plug-ins is displayed on the **Package Management** page. |
| **Credential** | Credential used to authenticate with AWS. Select **AWS Credentials** or **AWS Role ARN**. You must create credentials in **Credential Management**, or click the **Add (+)** icon to create new credentials. For more information, see [Creating Credentials](https://www.ibm.com/docs/en/SSMPHH_12.x/com.ibm.guardium.doc.stap/guc/guc_credential_management.html). |
| **Kafka Cluster** | Select a Kafka cluster from the available Kafka cluster list or create a new Kafka cluster. For more information, see [Managing Kafka clusters](https://www.ibm.com/docs/en/SSMPHH_12.x/com.ibm.guardium.doc.stap/guc/guc_kafka_cluster_management.html). |
| **Label** | Grouping label for the profile. For example, customer name or ID. |
| **AWS account region** | The AWS region where your Athena workgroup and CloudTrail trail are located (for example, `us-east-1`). |
| **Log groups** | The enriched CloudWatch log group to monitor — the output of the Lambda enricher, not the raw CloudTrail log group. Specify the same value that you set in the `ENRICHED_LOG_GROUP` environment variable on the Lambda function. |
| **Filter pattern** | CloudWatch Logs filter pattern to apply. Specify `None` to retrieve all logs, or specify a pattern to filter for specific log events. |
| **Account ID** | The 12-digit AWS account ID. |
| **Cluster name** | The Athena workgroup or table identifier. |
| **Ingestion delay (seconds)** | Ingestion delay in seconds. Default value is 900 seconds (15 minutes). This delay accounts for the latency between event generation and log availability in CloudWatch. |
| **No-traffic threshold (minutes)** | Inactivity threshold in minutes. The default value is 60. If no incoming traffic is detected for 60 minutes, the S-TAP status changes to red. When incoming traffic resumes, the status returns to green. |
| **Unmask sensitive value** | Optional boolean flag. Select this option to display sensitive values in audit logs without masking. |
| **Use Enterprise Load Balancing (ELB)** | Select this option to enable Enterprise Load Balancing (ELB) support. |
| **Managed Unit Count** | The number of managed units (MUs) to allocate for ELB. |

**Notes:**
- Ensure that the **profile name** is unique.
- Required credentials must be created before or during profile creation.
- The AWS credentials must have `logs:GetLogEvents` and `logs:DescribeLogStreams` permissions on the enriched log group.

---

## Testing a connection

After you create a profile, test the connection to ensure that the configuration is valid.

### Procedure

1. Select the new profile.
2. From the top menu, click **Test Connection**.
3. If the connection test succeeds, proceed to install the profile.
4. If the connection test fails, verify the following items:
   - The AWS credentials have the required CloudWatch permissions (`logs:GetLogEvents` and `logs:DescribeLogStreams`) on the enriched log group.
   - The enriched log group name matches the `ENRICHED_LOG_GROUP` environment variable on the Lambda function.
   - The Kafka cluster is running and accessible.

---

## Installing a profile

After the connection test succeeds, install the profile on managed units (MUs) or edge units. The parsed audit logs are sent to the selected managed unit or edge unit for the sniffer to process.

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

1. Verify that the CloudWatch subscription filter is attached to the raw CloudTrail log group and targets the Lambda function.
2. Run a test query in Athena, wait 30–60 seconds, and then check the enriched log group for events.
3. Check the Lambda function CloudWatch logs for execution errors:
   - Go to **CloudWatch > Log groups**, and open `/aws/lambda/<function-name>`.
   - If timeout errors occur, increase the Lambda function timeout to at least 30 seconds.
   - If permission errors occur, verify that the IAM policy attached to the Lambda execution role includes the `athena:GetQueryExecution` permission.

### The queryString field shows UNAVAILABLE

The Lambda function failed to call the `GetQueryExecution` API. Verify that the Lambda execution role includes the `athena:GetQueryExecution` IAM permission.

### Logs do not appear in Guardium

1. Verify that the **Log groups** field in the universal connector profile specifies the enriched log group, not the raw CloudTrail log group.
2. Verify that the enriched log group contains events where the `queryString` field displays the actual SQL statements (not `***OMITTED***`).
3. Verify that the AWS credentials configured for the profile have `logs:GetLogEvents` and `logs:DescribeLogStreams` permissions on the enriched log group.
4. Check the Kafka Connect logs for connector errors.

### Connection test fails

1. Verify that the AWS credentials are valid and have not expired.
2. Verify that the log group name is correct and exists in the specified AWS region.
3. Verify that the Kafka cluster is running and accessible.

---
