# Configuring AWS MSSQL datasource profile for JDBC Kafka Connect plug-ins

You can create and configure datasource profiles through central manager for **AWS MSSQL JDBC Kafka Connect** plug-ins.

## Meet MSSQL over JDBC Connect

* Environments: On-prem
* Supported inputs: Kafka Input (pull)
* Supported Guardium versions:
    * Guardium Data Protection: Appliance bundle 12.2.2 or later

Kafka-connect is a framework for streaming data between Apache Kafka and other systems.

## Configuring AWS MSSQL RDS server 
Create and configure an AWS RDS Microsoft SQL Server Enterprise Edition instance with external access, audit logging, and S3 integration for storing audit trails.

### Procedure

1. Create a database instance. </br>
	a. Go to https://console.aws.amazon.com/. </br>
	b. Click on **Services**. </br>
	c. In the **Database** section, click on **RDS**. </br>
	d. From the **Region** dropdown menu, select your region where you want to create the databse instance.</br>
	e. From the **Amazon RDS Dashboard**, click **Create database**. </br>
	f. In **Choose a database creation method**, select **Standard Create**. </br>
	g. In the **Engine options** field, select **Microsoft SQL Server and ‘SQL Server Enterprise Edition**. </br>
	h. In the **Version** field, select **SQL Server 2017 14.00.3281.6.v1**. </br>
	i. Select the **Dev/Test** template. </br>
	j. Enter the **Database name**, **Master username** and **Password**. This username and password is used as an input in jdbc connection details for universal connector. </br>
	k. Optional: In the **Export logs** field, select error logs. </br>
	l. To access the database from outside, go to the **Connectivity** section and set **Public access** to **Yes**. </br>
	m. Select **Create database**. </br>
	
2. To access the database instance from outside, you must add an inbound rule to the database. </br>
	a. Click the database that you created in the previous step. </br>
	b. Go to the **Connectivity & security** tab. </br>
	c. Under the **Security** section, select **VPC security group**, which is the default option you select when creating the database. </br>
	d. Go to selected default security group.</br>
	e. Under the **Inbound rule** section, click **Edit inbound rules**. </br>
	f. Click on the **Add rule** button and add the following rules for MSSQL. </br>
		&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;i. From the **type** dropdown, select **MSSQL**. In the **Source** column, keep the custom as default. Then click the **Search** icon to select the **0.0.0.0/0** rule. </br>
		&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;ii. From the **type** dropdown, select **MSSQL**. In the **Source** column, keep the custom as default. Then click the **Search** icon to select the **::/0** rule.  </br>
	g. **Microsoft MSSQL Management Studio** is required to connect with the database and perform database operations. To connect with the database, use the endpoint and port values from the  **Connectivity & security** tab in the RDS instance. </br>

3. Assign a parameter group to the database instance. </br>
	a. You can assign a default parameter group to your database. Use the parameter group family ``sqlserver-ee-14.0`` and set the **rds.sqlserver_audit** parameter to ``true``.</br>
	b. In the **Navigation** panel, choose **Databases**.</br>
	c. Select the **mssql** database that you created, then click **Modify**.</br>
	d. Go to **Advance configurations**.</br>
	e. Under **Database options**, select the parameter group from drop down.</br>
	f. Click **Continue**. On next window, select **Apply Immediately** and click **Modify DB Instance**.</br>

4. Create an S3 bucket.</br>
   	a. Click **Services**.</br>
	b. Select **S3** from the services.</br>
	c. Choose **Create bucket**.</br>
	d. Enter the **Bucket name** and **AWS region**, then click **Create bucket**.</br>

5. Create a custom option group for your database instance.</br>
	a. Click **Services**.</br>
	b. In the **Database** section, click **RDS**.</br>
	c. In the **Navigation** panel, choose **Option groups**. Then select **Create group**.</br>
 	d. In the **Create option group** window, complete the following steps.  </br>
   		&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;i. In the **Name** field, enter a unique name for the option group within your AWS account. The name can contain only letters, digits, and hyphens.  </br>
   		&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;ii. In the **Description** field, enter a brief description of the option group.  </br>
   		&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;iii. For the **Engine** field, select **sqlserver-ee**.  </br>
   		&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;iv. For the **Major engine version** field, select the major version of the DB engine that you want to use.  </br>
	e. Select the created option group and click **Add option**.</br>
	f. Select **SQLSERVER_AUDIT** as an option name.</br>
	g. Set the S3 bucket that you created in the previous step.</br>
	h. Create a new IAM role. Then create a policy to attach to the IAM role. Use the following JSON: </br>

		{
				"Version": "2012-10-17",
				"Statement": [
					{
						"Effect": "Allow",
						"Action": "s3:ListAllMyBuckets",
						"Resource": "*"
					},
					{
						"Effect": "Allow",
						"Action": [
							"s3:ListBucket",
							"s3:GetBucketACL",
							"s3:GetBucketLocation"
						],
						"Resource": "arn:aws:s3:::<BUCKET_NAME>"
					},
					{
						"Effect": "Allow",
						"Action": [
							"s3:PutObject",
							"s3:ListMultipartUploadParts",
							"s3:AbortMultipartUpload"
						],
						"Resource": "arn:aws:s3:::<BUCKET_NAME>/*"
					}
				]
			}   </br>

	i. In the **Scheduling** field, select **Apply Immediately**, then click **Add option**.</br>


7. Associate the option group with the DB instance.</br>
	a. In the **Navigation** panel, choose **Databases**.</br>
	b. Select the **mssql** database that you created, then click **Modify**.</br>
	c. Go to **Advance configurations**.</br>
	d. Under **Database options**, select the custom option group that you created in the previous stes.</br>
	e. Click **Continue**. On the next window, select **Apply Immediately** and then click **Modify DB Instance**.</br>


## Enabling auditing

1. Connect to the database. </br>
	a. Launch SQL Server Management Studio and provide the following connection details: </br>
		&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;i. In the **Server Name** field, enter the endpoint (available in the AWS RDS console).</br>
   		&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;ii. Enter the username and master password that you set when creating the database.</br>
	b. Create database.</br>

2. Audit specifications.</br>
	a. SQL Server audit allows you to create server audits that can contain the following specifications:</br>
		&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;i. Server audit specifications for server-level events.</br>
		&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;ii. Database audit specifications for database-level events.</br>
	b. When you define an audit, you specify the output location for the results (the audit destination). The audit is created in a disabled state and does not automatically audit any actions. After the audit is enabled, the audit destination receives data from the audit.</br>

3. Create an audit.</br>
	a. Create an audit in Management Studio by going to **Security > Audits > New Audit**.</br>
	b. Enter the ``D:\rdsdbdata\SQLAudit\`` filepath, which is the default path for AWS RDS instances.</br>
	c. In the **Maximum file size** field, deselect the **Unlimited** checkbox and enter a specific value.</br>
	d. Keep the remaining configurations as default.</br>
	e. Click **OK**.</br>
	f. Right-click the audit you created and select **Enable**.</br>
		
4. Create a server audit specification.</br>
	a. In **Management**, navigate to **Security** and expand it.</br>
	b. Right-click **Server Audit Specifications** and select **New Audit Specification**.</br>
	c. Select the audit that you created in the previous step.</br>
	d. Configure the audit log groups based on your requirements. For more information on audit log groups, see the Microsoft documentation website.</br>
	e. Click **OK**.</br>
	f. Right-click the database audit specification you created and select **Enable**.</br>

5. Create a database audit specification.</br>
	a. In **Management**, navigate to **Databases** and expand it. Then expand **Security** under the database.</br>
	b. Right-click **Database Audit Specifications** and select **New Audit Specification**.</br>
	c. Select the audit that you created in the previous step.</br>
	d. Configure the audit log groups based on your requirements. For more information on audit log groups, see the Microsoft documentation website.</br>
	e. Click **OK**.</br>
	f. Right-click the database audit specification you created and select **Enable**.</br>

6. Create a non-admin user to access the audit table without exposing admin credentials. </br>
	a. Log in to the database using admin credentials and run the following queries:  </br>

  	```sql
 	CREATE LOGIN <login_name> WITH PASSWORD = '<password>';
 	USE msdb;
  	CREATE USER <user_name> FOR LOGIN <login_name>;
   	GRANT SELECT ON msdb.dbo.rds_fn_get_audit_file TO <user_name>;
	```
	
	b.  In the **Input** section, set the database name as **msdb**.  </br>

  	```properties
 	 jdbc_connection_string => "jdbc:sqlserver://<SERVER_NAME>:<PORT>;databaseName=msdb;"
 	 ```

   	c. Use the login credentials created in the previous step for the JDBC connection.  </br>
	
   	```properties
  	jdbc_user => "<login_name>"
  	jdbc_password => "<password>"
 	 ```

## Creating datasource profiles

You can create a new datasource profile from the **Datasource Profile Management** page.

### Procedure

1. Go to **Manage > Universal Connector > Datasource Profile Management**
2. Click the **➕ (Add)** button.
3. You can create a profile by using one of the following methods:

    * To **Create a new profile manually**, go to the **"Add Profile"** tab and provide values for the following fields.
        * **Name** and **Description**.
        * Select a **Plug-in Type** from the dropdown. For example, `AWS MsSQL Over JDBC Kafka Connect 2.0`.

    * To **Upload from CSV**, go to the **"Upload from CSV"** tab and upload an exported or manually created CSV file
      containing one or more profiles. You can also choose from the following options:
        * **Update existing profiles on name match** — Updates profiles with the same name if they already exist.
        * **Test connection for imported profiles** — Automatically tests connections after profiles are created.
        * **Use ELB** — Enables ELB support for imported profiles. You must provide the number of MUs to be used in the
          ELB process.

**Note:** Configuration options vary based on the selected plug-in.

## Configuration: JDBC Kafka Connect 2.0-based Plugins

The following table describes the fields that are specific to JDBC Kafka Connect 2.0 and similar plugins.

| Field                         | Description                                                                                                                                                     |
|-------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Name**                      | Unique name of the profile.                                                                                                                                     |
| **Description**               | Description of the profile.                                                                                                                                     |
| **Plug-in**                   | Plug-in type for this profile. A full list of available plug-ins are available on the **Package Management** page.                                              |
| **Credential**                | The credential to authenticate with the datasource. Must be created in **Credential Management**, or click **➕** to create one.                                 |
| **Kafka Cluster**             | Kafka cluster to deploy the universal connector.                                                                                                                |
| **Label**                     | Grouping label. For example, customer name or ID.                                                                                                               |
| **JDBC Driver Library**       | JDBC driver for the database.                                                                                                                                   |
| **Port**                      | Port that is used to connect to the database.                                                                                                                   |
| **Hostname**                  | Hostname of the database.                                                                                                                                       |
| **Query**                     | SQL query that is used to extract audit logs.                                                                                                                   |
| **Service Name / SID**        | The database **service name** or **SID**.                                                                                                                       |
| **Initial Time**              | Initial polling time for audit logs.                                                                                                                            |
| **No Traffic Threshold**      | Threshold setting for inactivity detection.                                                                                                                     |
| **Connection URL**            | Full JDBC connection string. Format varies by database type. <br/> For example, `jdbc:postgresql://mydb.abc123.us-east-1.rds.amazonaws.com:5432/mydb?ssl=true`. |
| **Use Enterprise Load Balancing (ELB)** | Enable this if ELB support is required.                                                                                                                         |
| **Managed Unit Count**        | Number of Managed Units (MUs) to allocate for ELB.                                                                                                              |

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
