# Redshift-Guardium Logstash filter plug-in
### Meet Redshift
* Tested versions: 1.0.40182
* Environment: AWS
* Supported inputs: S3 (pull), Cloudwatch(pull)
* Supported Guardium versions:
    * Guardium Data Protection: 11.4 and above

This is a [Logstash](https://github.com/elastic/logstash) filter plug-in for the universal connector that is featured in IBM Security Guardium. It parses events and messages from the AWS Redshift audit log into a [Guardium record](https://github.com/IBM/universal-connectors/blob/main/common/src/main/java/com/ibm/guardium/universalconnector/commons/structures/Record.java) instance (which is a standard structure made out of several parts). The information is then sent over to Guardium. Guardium records include the accessor (the person who tried to access the data), the session, data, and exceptions. If there are no errors, the data contains details about the query "construct". The construct details the main action (verb) and collections (objects) involved. In this connector we are using "PGRS" Guardium parser as well as custom parser for some scenarios. As of now, the Redshift plug-in only supports Guardium Data Protection.

This plug-in uses two parsers. It relies on Guardium SQL parser most of the time, while parsing some queries unique to Redshift by itself.

The plug-in is free and open-source (Apache 2.0). It can be used as a starting point to develop additional filter plug-ins for the Guardium universal connector.

 ***Note:***
 While connecting a third-party tool (sqlworkbench/j), we need to add an inbound rule to the security group inside the Redshift cluster:
 
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

### Enabling and configuring audit logging

### Procedure
1. Navigate to the Redshift Console and click on the cluster that you just created and go to properties tab.
3. After opening the **properties** tab, click **Edit** and select **Edit Audit Logging**.
4. In the **configure enable audit logging** option, choose **enable**.
5. Select **Create new bucket**.
6. Enter a bucket name with an S3 key prefix and then click **Save changes**.

## Creating a parameter group

### Procedure

1. Go to the **CONFIG** tab.
2. Select **Workload management** and choose the default parameter group. Editing is disabled for the default parameter group in the **Parameters** tab. No provision is available to edit the default parameter group, so you need to create a new parameter group.
3. Click **Create**.
4. Choose a name and description for the Parameter group and click **Create**.
   
## Configuring and modifying a parameter group

### Procedure
After creating a new parameter group, you can modify the parameters
1. Open the newly created parameter group and select the **Parameters** tab.
2. Click **Edit Parameters**.
2. In **enable_user_activity_logging**, change the value from **false** to **true**.
3. Click **Save**.

## Add a new Parameter Group to a cluster

### Procedure
1. Navigate to the Redshift Console, click on the cluster that you just created, and go to the **properties** tab.
3. Click **Edit** and select **Edit parameter group**.
4. Select the parameter group you have created and modified.
5. Click **Save changes**.
6. 
## Connecting to the database

### Procedure

Once the cluster has been created, go to the **properties** tab

In the **Database configurations** section, you can see that the default database name is dev, the default port AWS Redshift listens to is 5439, and the default username is awsuser.
1. Go to the **Editor** tab and click **Connect to database**. A new page opens.
2. Choose the created cluster name.
3. Add a database name and database user. 
4. Click **Connect**.

## Running the query

### Procedure

1. Create a table with details and append the row with the created table.
2. Run a query with the run button.

## Viewing the logs entries on S3 

### Procedure
Go to the S3 buckets from the search box and find the details of the generated logs (UserActivity/Connection) in below format:

`s3`/<`bucket`>/<`prefix/`>/`AWSLogs/`/<`Account ID`>`redshift/`/<`region/`>/`<Year/>`/`<Month/>`/`<Day/>`/ See the generated UserActivity/Connection logs here.

## Viewing the logs entries on Cloudwatch

### Procedure
Go to Cloudwatch from the search box and find the details of the generated logs (UserActivity/Connection) in the following log groups:
`/aws/redshift/cluster/ds-redshift-cluster/connectionlog`
`/aws/redshift/cluster/ds-redshift-cluster/useractivitylog`
Note : We are not capturing logs from cloudwatch `/aws/redshift/cluster/ds-redshift-cluster/userlog` group.

## Configuring the Redshift filter in Guardium
The Guardium universal connector is the Guardium entry point for native audit logs. The Guardium universal connector identifies and parses the received events, and converts them to a standard Guardium format. The output of the Guardium universal connector is forwarded to the Guardium sniffer on the collector, for policy and auditing enforcements. Configure Guardium to read the native audit logs by customizing the Redshift template.

## Authorizing outgoing traffic from AWS to Guardium
1. Log in to the Guardium API.
2. Issue this command:
   
`grdapi add_domain_to_universal_connector_allowed_domains domain=amazonaws.com`

## Before you begin

*  Configure the policies you require. See [policies](/docs/#policies) for more information.
* You must have permission for the S-Tap Management role. The admin user includes this role by default.

* Redshift-Guardium Logstash filter plug-in is automatically available with Guardium Data Protection versions 12.x, 11.4 with appliance bundle 11.0p490 or later or Guardium Data Protection version 11.5 with appliance bundle 11.0p540 or later releases.

**Note:** For Guardium Data Protection version 11.4 without appliance bundle 11.0p490 or prior or Guardium Data Protection version 11.5 without appliance bundle 11.0p540 or prior, download the [logstash-filter-redshift_guardium_connector.zip](https://github.com/IBM/universal-connectors/raw/release-v1.2.0/filter-plugin/logstash-filter-redshift-aws-guardium/S3OverRedshiftPackage/logstash-filter-redshift_guardium_connector.zip) plug-in. (Do not unzip the offline-package file throughout the procedure). 
* Download the plugin filter configuration file [redshift-over-s3.conf](https://github.com/IBM/universal-connectors/raw/main/filter-plugin/logstash-filter-redshift-aws-guardium/redshift-over-s3.conf) or [redshift-over-cloudwatch.conf](https://github.com/IBM/universal-connectors/raw/main/filter-plugin/logstash-filter-redshift-aws-guardium/redshift-over-cloudwatch.conf) based on input used.


## Procedure
1. On the collector, go to **Setup** > **Tools and Views** > **Configure Universal Connector**.
2. Enable the connector if it is disabled.
3. Click **Upload File** and select the [logstash-filter-redshift_guardium_connector.zip](https://github.com/IBM/universal-connectors/raw/release-v1.2.0/filter-plugin/logstash-filter-redshift-aws-guardium/S3OverRedshiftPackage/logstash-filter-redshift_guardium_connector.zip) plug-in. After it is uploaded, click **OK**. This step is not necessary for Guardium Data Protection v11.0p490 or later, v11.0p540 or later, v12.0 or later.
4. Click the Plus sign to open the Connector Configuration dialog box.
5. Type a name in the **Connector name** field.
6. To fetch the audit logs from s3 directly, use the details from the [redshift-over-s3.conf](redshift-over-s3.conf) file. To fetch the audit logs from Cloudwatch, use the details from the [redshift-over-cloudwatch.conf](redshift-over-cloudwatch.conf) file. Update the input section to add the details
    from the corresponding file's input part, omitting the keyword "input{" at the beginning and its corresponding "}" at the end.
7. To fetch the audit logs from s3 directly, use the details from the [redshift-over-s3.conf](redshift-over-s3.conf) file. To fetch the audit logs from Cloudwatch, use the details from the [redshift-over-cloudwatch.conf](redshift-over-cloudwatch.conf) file. Update the filter section to add the details
    from the corresponding file's filter part, omitting the keyword "filter{" at the beginning and its corresponding "}" at the end.
8. The "type" fields should match in input and filter configuration sections. This field should be unique for every individual connector added.
9. Click **Save**. Guardium validates the new connector. After it is validated, it appears in the Configure Universal Connector page.

## 5. Limitations
1. The log files appear in the s3 bucket in hourly batches, and sometimes even later. A typical delay is 30-120 minutes.

2. The following important fields couldn't be mapped with Redshift audit logs:
     - Source program : Not available with User Activity/Connection logs
     - Server IP : Not available with User Activity/Connection logs
     - Client HostName : Not available with User Activity/Connection logs
     - Client IP : Not available with User Activity logs (mentioned in AWS documentation) and in case of Connection logs, it only appears in the Guardium quickSearch (QS) page.
3. Error Logs : As mentioned in the AWS documentation for the Redshift Connector, UserActivity logs do not capture any error logs related with Syntax errors or Authentication errors. That's why we capture connection logs only for Authentication Failure Logs (It appears on Guardium the Guardium quicksearch screen as "LOGIN_FAILED" only if failed log-in is attempted). 
4. Due to parser limitations, the following are the details of the changes and limitations-
    - Queries having `MINUS` operator will appear with `EXCEPT` operator in Guardium.
    - The keyword `TOP<NUMBER>` will be removed from the Select Queries.
    - Select queries with `PIVOT/UNPIVOT` will not be parsed by the Connector.
5. CREATE MATERIALIZED VIEW commands appear multiple times on the S3 bucket, the Guardium full SQL report, and sniffer logs.
6. Any query having key constraints (primary key, foreign key, unique key) may create duplication in logs because they are not enforced by Amazon Redshift, as mentioned in AWS document. (https://docs.aws.amazon.com/redshift/latest/dg/t_Defining_constraints.html)
