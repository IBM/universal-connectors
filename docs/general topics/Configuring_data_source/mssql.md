# Configuring AWS MSSQL RDS

### Procedure
1. Create database instance
   1. Go to https://console.aws.amazon.com/
   2. Click on Services.
   3. In the Database section, click on RDS.
   4. Select the region in top right corner.
   5. On Amazon RDS Dashboard in central panel, click on Create database.
   6. In Choose a database creation method, select Standard Create.
   7. In Engine options, choose the engine type: Microsoft SQL Server and ‘SQL Server Enterprise Edition’.
   8. Chose version ‘SQL Server 2017 14.00.3281.6.v1’.
   9. Choose Dev/Test template
   10. Provide database name, master username and password. (this username and password will be used as an input in jdbc connection details for universal connector)
   11. Export logs: error logs can be selected.
   12. To access DB from outside, select public access to yes under Connectivity section.
   13. Select create database.
   
2. Accessing database instance from outside - To access DB instance from outside we need to add inbound rule to database.
   1. Click on database that we created in previous step.
   2. Go to ‘Connectivity & security’ tab.
   3. Under security, click on ‘VPC security group’(which is default we selected while creating database)
   4. Go to selected default security group
   5. Under ‘Inbound rule’ section, click on edit inbound rules.
   6. Click on Add rule button and add following two rules for MSSQL.
   7. Select type MSSQL from first drop down, in source column keep custom as default, click on search icon and select rule ‘0.0.0.0/0’.
   8. Select type MSSQL from first drop down, in source column keep custom as default, click on search icon and select rule ‘::/0’.
   9. We will be requiring “Microsoft MSSQL Management Studio” to connect with the database and do DB operations. To connect with DB, use endpoint and port which we will get under ‘Connectivity & security’ tab in rds instance.
 
3. Assign parameter group to database instance.
   1. We can assign default parameter group to our database. Parameter group family should be ‘sqlserver-ee-14.0’ and parameter ‘rds.sqlserver_audit’ parameter should be set to true.
   2. In Navigation panel choose Databases.
   3. Select mssql database that we created. Click on Modify button.
   4. Go to Advance configurations.
   5. Under Database options, select parameter group from drop down.
   6. Click on Continue. On next window select Apply Immediately and click on Modify DB Instance.
   
4. Create S3 bucket.
   1. Click on Services.
   2. Select S3 from services.
   3. Choose Create bucket.
   4. Provide Bucket name and AWS region. Click on Create bucket.
   
5. Create custom option group to database instance.
   1. Click on Services.
   2. In the Database section, click on RDS.
   3. In the navigation pane, choose Option groups.
   4. Choose Create group.
   5. In the Create option group window, do the following:
      1. For Name, type a name for the option group that is unique within your AWS account. The name can contain only letters, digits, and hyphens.
      2. For Description, type a brief description of the option group.
      3. For Engine, choose the DB engine <sqlserver-ee>.
      4. For Major engine version, choose the major version of the DB engine that you want.
   6. Select created option group and click on Add option.
   7. Select SQLSERVER_AUDIT as an option name.
   8. Set S3 bucket that we created in previous step.
   9. Create new IAM role.
   10. Need to create policy which we will be attaching to the IAM role. Use following JSON for same.
   11. ``` 
       {
          "Version": "2012-10-17",
          "Statement": [{
             "Effect": "Allow",
             "Action": "s3:ListAllMyBuckets",
             "Resource": "*"
          },{
             "Effect": "Allow",
             "Action": [
                "s3:ListBucket",
                "s3:GetBucketACL",
                "s3:GetBucketLocation"
             ],
             "Resource": "arn:aws:s3:::<BUCKET_NAME>"
          },{
             "Effect": "Allow",
             "Action": [
                "s3:PutObject",
                "s3:ListMultipartUploadParts",
                "s3:AbortMultipartUpload"
             ],
             "Resource": "arn:aws:s3:::<BUCKET_NAME>/*"
          }]
       }
       ```
   12. Select Apply Immediately in Scheduling option and click on Add option.

6. Associate the option group with the DB instance
   1. In Navigation panel choose Databases.
   2. Select mssql database that we created. Click on Modify button
   3. Go to Advance configurations
   4. Under Database options. Select custom option group from drop down (which we have created in earlier step.)
   5. Click Continue. In the next window, select Apply Immediately and click Modify DB Instance.

