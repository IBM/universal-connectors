# Configuring Redshift datasource profiles for Kafka Connect Plug-ins

Create and configure datasource profiles through Central Manager for **Redshift
over CloudWatch Kafka Connect** plug-ins.

### Meet Redshift over CloudWatch Connect

* Environments: AWS
* Supported inputs: Kafka Input (pull)
* Supported Guardium versions:
    * Guardium Data Protection: Appliance bundle 12.2.1 or later.

Kafka-connect is a framework for streaming data between Apache Kafka and other systems. This connector enables
monitoring of Redshift audit logs through CloudWatch.

**Note:** While connecting a third-party tool (sqlworkbench/j), you need to add an inbound rule to the security group inside the
Redshift cluster:

- Navigate to the Redshift Console and click on the cluster which is created and go to the **properties** tab.

- In **Network and security settings**, select the security group inside **VPC security group**.

- Once the Security Groups page opens, go to the **Inbound rules** section and click **Edit inbound rules**.

- Click **Add rule** and provide the following details:

    1. Type : Redshift

    2. Protocol : TCP (default)

    3. Port range: 5439 (default)

    4. Source : Custom (0.0.0.0/0)

- Click **Save rules**.

## Enabling auditing for Redshift

1. Navigate to the Redshift Console and click on the cluster that you just created and go to **Properties** tab.
2. From the **Properties** tab, click **Edit** > **Edit Audit Logging**.
3. In the **Configure enable audit logging** option, choose **Enable**.
4. Select **Create new bucket**.
5. Enter a bucket name with an S3 key prefix and then click **Save changes**.

## Creating a parameter group

1. Go to the **CONFIG** tab.
2. Select **Workload management** and choose the default parameter group. Editing is disabled for the default parameter
   group in the **Parameters** tab. No provision is available to edit the default parameter group, so you need to create
   a new parameter group.
3. Click **Create**.
4. Choose a name and description for the Parameter group and click **Create**.

## Configuring and modifying a parameter group

After creating a new parameter group, you can modify the parameters.

1. Open the newly created parameter group and select the **Parameters** tab.
2. Click **Edit Parameters**.
2. In **enable_user_activity_logging**, change the value from **false** to **true**.
3. Click **Save**.

## Add a new Parameter Group to a cluster

### Procedure

1. Navigate to the Redshift Console, click on the cluster that you just created, and go to the **Properties** tab.
2. Click **Edit** and select **Edit parameter group**.
3. Select the parameter group you have created and modified.
4. Click **Save changes**.


## Connecting to the database

Once the cluster has been created, go to the **Properties** tab

In the **Database configurations** section, you can see that the default database name is `dev`, the default port AWS
Redshift listens to is `5439`, and the default username is `awsuser`.

1. Go to the **Editor** tab and click **Connect to database**. 
2. Choose the created cluster name.
3. Add a database name and database user.
4. Click **Connect**.

## Running the query

1. Create a table with details and append the row with the created table.
2. Run a query with the run button.

## Viewing the logs entries on S3

Go to the S3 buckets from the search box and find the details of the generated logs (UserActivity/Connection) as shown in the following example.

`s3`/<`bucket`>/<`prefix/`>/`AWSLogs/`/<`Account ID`>`redshift/`/<`region/`>/`<Year/>`/`<Month/>`/`<Day/>`/ See the
generated UserActivity/Connection logs here.

## Viewing the logs entries on Cloudwatch

Go to Cloudwatch from the search box and find the details of the generated logs (UserActivity/Connection) in the following log groups:
`/aws/redshift/cluster/ds-redshift-cluster/connectionlog`
`/aws/redshift/cluster/ds-redshift-cluster/useractivitylog`

**Note** : Logs are not captured from `/aws/redshift/cluster/ds-redshift-cluster/userlog` cloudwatch group.

## Limitations

1. The log files appear in the s3 bucket in hourly batches, and sometimes even later. A typical delay is 30-120 minutes.

2. The following important fields can not be mapped with Redshift audit logs:
    - Source program : Not available with User Activity/Connection logs
    - Server IP : Not available with User Activity/Connection logs
    - Client HostName : Not available with User Activity/Connection logs
    - Client IP : Not available with User Activity logs (mentioned in AWS documentation) and in case of Connection logs,
      it only appears in the Guardium quickSearch (QS) page.
3. Error Logs : UserActivity logs do not capture any error logs related with Syntax errors or Authentication errors. That's why we capture connection logs only for
   Authentication Failure Logs (It appears on Guardium the Guardium quicksearch screen as "LOGIN_FAILED" only if failed
   log-in is attempted).
4. Due to parser limitations, the following are the details of the changes and limitations:
    - Queries having `MINUS` operator appeara with `EXCEPT` operator in Guardium.
    - The keyword `TOP<NUMBER>` is removed from the Select Queries.
    - Select queries with `PIVOT/UNPIVOT` is not parsed by the Connector.
5. CREATE MATERIALIZED VIEW commands appear multiple times on the S3 bucket, the Guardium full SQL report, and sniffer
   logs.
6. Any query having key constraints (primary key, foreign key, unique key) may create duplication in logs because they
   are not enforced by Amazon Redshift. For more information, [AWS: Table constraints](https://docs.aws.amazon.com/redshift/latest/dg/t_Defining_constraints.html).

## Creating datasource profiles

You can create a new datasource profile from the **Datasource Profile Management** page.

### Procedure

1. Go to **Manage > Universal Connector > Datasource Profile Management**
2. Click the **➕ (Add)** button.
3. You can create a profile by using one of the following methods:

    - To **Create a new profile manually**, go to the **"Add Profile"** tab and provide values for the following fields.
        - **Name** and **Description**.
        - Select a **Plug-in Type** from the dropdown. For example, `Redshift Over Cloudwatch Connect 2.0`.

    - To **Upload from CSV**, go to the **"Upload from CSV"** tab and upload an exported or manually created CSV file
      containing one or more profiles.  
      You can also choose from the following options:
        - **Update existing profiles on name match** — Updates profiles with the same name if they already exist.
        - **Test connection for imported profiles** — Automatically tests connections after profiles are created.
        - **Use ELB** — Enables ELB support for imported profiles. You must provide the number of MUs to be used in the
          ELB process.

**Note:** Configuration options vary based on the selected plug-in.

## Configuring Redshift Over CloudWatch Kafka Connect 2.0

The following table describes the fields that are specific to Redshift over CloudWatch Kafka Connect 2.0 plugin.

| Field                                   | Description                                                                                                                                                                                                                                                                                                          |
|-----------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Name**                                | Unique name of the profile.                                                                                                                                                                                                                                                                                          |
| **Description**                         | Description of the profile.                                                                                                                                                                                                                                                                                          |
| **Plug-in**                             | Plug-in type for this profile. Select `Redshift Over Cloudwatch Connect 2.0`. A full list of available plug-ins are available on the **Package Management** page.                                                                                                                                                    |
| **Credential**                          | Select AWS Credentials or AWS Role ARN. The credential to authenticate with AWS. Must be created in **Credential Management**, or click **➕** to create one. For more information, see [Creating Credentials](https://www.ibm.com/docs/en/SSMPHH_12.x/com.ibm.guardium.doc.stap/guc/guc_credential_management.html). |
| **Kafka Cluster**                       | Select the appropriate Kafka cluster from the available Kafka cluster list or create a new Kafka cluster. For more information, see [Managing Kafka clusters](https://www.ibm.com/docs/en/SSMPHH_12.x/com.ibm.guardium.doc.stap/guc/guc_kafka_cluster_management.html).                                              |
| **Label**                               | Grouping label. For example, customer name or ID.                                                                                                                                                                                                                                                                    |
| **AWS account region**                  | Specifies the AWS region where your Redshift instance is located (e.g., us-east-1, eu-west-1).                                                                                                                                                                                                                       |
| **Log groups**                          | List of CloudWatch log groups to monitor. These are the log groups where Redshift audit logs are exported.                                                                                                                                                                                                           |
| **Filter pattern**                      | CloudWatch Logs filter pattern to apply. Use "None" to retrieve all logs, or specify a pattern to filter specific log events.                                                                                                                                                                                        |
| **Account ID**                          | Your AWS account ID (12-digit number). This identifies your AWS account.                                                                                                                                                                                                                                             |
| **Cluster name**                        | The name of your Redshift cluster or instance identifier.                                                                                                                                                                                                                                                            |
| **Ingestion delay (seconds)**           | Default value is 900 seconds (15 minutes). This delay accounts for the time it takes for logs to be available in CloudWatch after being generated.                                                                                                                                                                   |
| **No-traffic threshold (minutes)**      | Default value is 60. If there is no incoming traffic for an hour, S-TAP displays a red status. Once incoming traffic resumes, the status returns to green.                                                                                                                                                           |
| **Unmask sensitive value**              | Optional boolean flag. When enabled, sensitive values in the audit logs will not be masked.                                                                                                                                                                                                                          |
| **Use Enterprise Load Balancing (ELB)** | Enable this if ELB support is required.                                                                                                                                                                                                                                                                              |
| **Managed Unit Count**                  | Number of Managed Units (MUs) to allocate for ELB.                                                                                                                                                                                                                                                                   |

**Note:**

- Ensure that the **profile name** is unique.
- Required credentials must be created before or during profile creation.
- The AWS credentials must have appropriate permissions to read CloudWatch logs.

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

