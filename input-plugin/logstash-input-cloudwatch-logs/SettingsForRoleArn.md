## Configuration for role_arn parameter in the input plug-in

For input plug-ins like cloudwatch_logs and SQS, the AWS credentials are required to access the AWS endpoint either cloudwatch_logs log_group or SQS queue. However, instead of using the AWS access_key and secret_key, using the role_arn setting can be preferable.

### Note:

These settings can be used only when Guardium Data Protection is hosted on AWS.

## Configuration for IAM Role when the Guardium Data Protection and the Database to be monitored are in the same AWS account

### Procedure :

1.  Log in to your IAM console (https://console.aws.amazon.com/iam/).
2.  Click the **Roles** tab under **Access Management**.
3.  Click **Create Role**.
4.  For **Trusted Entity Type**, select **AWS Service**.
5.  For **Use case**, select **EC2**.
6.  Click **Next**.
7.  Steps to set the Permissions Policies
    1. When the input plug-in is cloudwatch_logs:
        1. Search CloudWatchLogsReadOnlyAccess and select it.
    2. When the input plug-in is SQS:
        1. Search CloudWatchLogsReadOnlyAccess and select it.
        2. Click **Create Policy**.
        3. In **Service**, select **SQS**.
        4. In **Actions**, select **Read and Write Action levels**.
        5. In **Resources**, add the role ARN of the queue that is to be monitored.
        6. Click **Review policy** and specify the policy name.
        7. Click **Create policy**.
        8. Attach this new policy to the role.
8.  Click **Next**.
9.  Enter the role name.
10. Click **Create Role**.
11. Search for the created role and open it.
12.	In the **Permissions** tab, click **Add Permissions** and select **Create Inline Policy**.
13.	On the **Create Policy** page, select JSON editor and add the following policy:

```
{
	"Version": "2012-10-17",
	"Statement": [
	{
    		"Sid": "VisualEditor0",
   	 	"Effect": "Allow",
    		"Action": "sts:AssumeRole",
    		"Resource": [
        		"arn:aws:iam::<AWS Account>:role/<Role Name>/*",
        		"arn:aws:iam::<AWS Account>:role/<Role Name>",
        		"arn:aws:sts::<AWS Account>:assumed-role/<Role Name>/*",
        		"arn:aws:sts::<AWS Account>:assumed-role/<Role Name>/<EC2 Instance Id>"
    		]
	}]
}
```

14. Click **Review Policy**.
15. Enter the policy name and click **Create Policy**.
16. Select the role created previously.
17. Click the **Trust relationships** tab and click **Edit trust policy**.
18. Add the below statement in the trust policy and click **Update Policy**.

```
{
	"Effect": "Allow",
        "Principal": {
            "AWS": "arn:aws:sts::<AWS Account>:role/<Role Name>/"
        },
        "Action": "sts:AssumeRole"
    }
    ```

19. Set the role to the EC2 machine hosting Guardium.
20. Go to the EC2 machine hosting Guardium.
21. Right-click on the EC2 instance, select the **Security** option, and modify the IAM role.
22. Set the role that was created previously.

## Configuration for IAM Role when Guardium Data Protection and the database to be monitored are in different AWS accounts

### Prerequisites :

1.  An AWS Account1 where IBM Guardium is hosted on an EC2 instance. In this example, Account ID is 111111.
2.  An AWS Account2 where RDS is present. In this example, Account ID is 222222.
3.  A Log group in the Account2 where RDS logs are logged. In this example, the test-log-group is the log group that is logging the logs in region us-east-1.

### Steps to create roles:

1. Log in to your IAM console (https://console.aws.amazon.com/iam/) of the first AWS Account where IBM Guardium is hosted (in this example, with Account ID 111111).
2. Click the **Roles** tab under **Access Management**.
3. Click **Create Role**.
4. For **Trusted Entity Type**, **select AWS Service**.
5. For **Use case**, select **EC2**.
6. Click **Next**.
7. Enter the role name (in this example, role_on_111111).
8. Click **Create Role**.
9. Repeat steps 1-10 on the second AWS Account (in this example, with Account ID 222222) and create a role with a name (in this example, role_on_222222).

### Steps to add permissions and policies to the role created on the account that has Guardium EC2:

1.  Log back in to the first AWS account.
2.  Search for the created role (in this example, role_on_111111) and open it.
3.	In the **Permissions** tab, click **Add Permissions** and select **Create Inline Policy**.
4.	On the **Create Policy** page, select JSON editor and add this policy:

```
{
           "Version": "2012-10-17",
           "Statement": [
           {
               "Sid": "VisualEditor0",
               "Effect": "Allow",
               "Action": "sts:AssumeRole",
               "Resource": [
               "arn:aws:iam::<AccountID_of_RDS>:role/<Role_In_Second_Account>"
               ]
           }
           ]
}
```
   
   e.g.,
   
```
{
	"Version": "2012-10-17",
        "Statement": [
        {
        	"Sid": "VisualEditor0",
                "Effect": "Allow",
                "Action": "sts:AssumeRole",
                "Resource": [
                	"arn:aws:iam::222222:role/role_on_222222"
                ]
         }]
}
```

5.  Click **Review Policy**.
6.  Enter the policy name and click **Create Policy**.
7.  The role named role_on_111111 is edited with the above inline policy.

### Adding permissions and policies to the role created on the account that has the RDS:

1. Log in to the second AWS account with account ID 222222.
2. Search for the created role (in this example, role_on_222222) and open it.
3. Steps to set the permissions policies to allow read permissions to CloudWatchLogs and/or SQS queue.
    i. When the input plug-in is cloudwatch_logs search CloudWatchLogsReadOnlyAccess and select it.
    ii. When the input plug-in is SQS 
		1. Search CloudWatchLogsReadOnlyAccess and select it.
		2. Click **Create Policy**.
		3. In **Service**, choose **SQS**.
		4. In **Actions**, select **Read and Write Action levels**.
		5. In **Resources**, add the role ARN of the queue that is to be monitored.
		6. Click **Review Policy** and specify the policy name.
		7. Click **Create policy**.
		8. Attach this new policy to the role.
4. In the **Permissions** tab, click **Add Permissions** and select **Create Inline Policy**.
5. Here you can add a policy to eliminate access from all but one log group (e.g., test-log-group).
6. On the **Create Policy** page, select JSON editor and add this policy:

Inline policy –
```
{
      "Version": "2012-10-17",
      "Statement": {
      		"Effect": "Deny",
         	"Action": "*",
         	"NotResource": [
            		"arn:aws:logs:<Region_of_Database>:<Account_Id_Of_RDS>:log-group:<log_group_to_be_monitored>:*" ]
      }
}
```

e.g.,
Inline policy –
```
{
	"Version": "2012-10-17",
	"Statement": {
		"Effect": "Deny",
		"Action": "*",
		"NotResource": [
			"arn:aws:logs:us-east-1:222222:log-group:test-log-group:*"
		]
	}
}
```

7. Select the role created above.
8. Click the **Trust relationships** tab and click **Edit trust policy**.
9. Add the below statement in the trust policy and click **Update Policy**.

```
{
       "Version": "2012-10-17",
       "Statement": [
       {
           "Effect": "Allow",
           "Principal": {
               "AWS": "arn:aws:iam::<Account_Id_of_Guardium_EC2>:role/<Role_In_First_Account>"
           },
           "Action": "sts:AssumeRole"
       }]
}
```
e.g.,

 ```
{
	"Version": "2012-10-17",
	"Statement": [
	{
		"Effect": "Allow",
		"Principal": {
				"AWS": "arn:aws:iam::111111:role/role_on_111111"
		},
		"Action": "sts:AssumeRole"
	}]
}
```

10. Set the role created on the first AWS account (in this example, role_on_111111 to the EC2 machine hosting Guardium).
11. Go to the EC2 machine hosting Guardium.
12. Right-click on the EC2 instance, select **Security**, and modify the IAM role.
13. Set the role that was created previously.


## Configuring an input plug-in on Guardium:

### Procedure :

Update the input section to add the details from the corresponding file's input part, omitting the keyword "input{" at the beginning and its corresponding "}" at the end.

The following is a sample configuration:

```
input {
	cloudwatch_logs {
	#Mandatory arguments:
	#Insert the log group that is created for the data instance 
	#Example of log group for AWS postgres RDS, /aws/rds/instance/<instance_name>/postgresql i.e., ["/aws/rds/instance/database-1/postgresql"]
	#Example of log group for Aurora Postgres, /aws/rds/cluster/<instance_name>/postgresql i.e., ["/aws/rds/cluster/aurorapostgres/postgresql"]
	log_group => ["<LOG_GROUP>"]  #e.g., ["/aws/rds/instance/database-1/postgresql"]
	start_position => "end"
	#Insert the role_arn of the role that is associated with the Guardium EC2 instance.
	role_arn => "<ROLE_ARN_ON_GUARDIUM_EC2_INSTANCE>"   #e.g., "arn:aws:iam::111111:role/role_on_111111" 
	region => "<REGION>" # Region that has the DB, Default value : us-east-1
	interval => 2
	event_filter => ""
	type => "<TYPE>"  #e.g., Postgres
	#Insert the account id of the AWS account
	add_field => {"account_id" => "<ACCOUNT_ID>"}
	}
}
```
