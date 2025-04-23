## Configuration for role_arn parameter in the cloudwatch_logs input plug-in

For input plug-ins like cloudwatch_logs, the AWS credentials are required to access the AWS endpoint for cloudwatch_logs log_group. However, instead of using the AWS access_key and secret_key, using the role_arn setting can be preferable.

### Note:

These settings can be used only when the Guardium Data Protection is hosted on AWS.

## Configuration for IAM Role when the Guardium Data Protection and the Database to be monitored are in the same AWS account

### Configuration:

1.  Log in to your IAM console (https://console.aws.amazon.com/iam/)
2.  Click the ```Roles``` tab under ```Access Management```
3.  Click the ```Create Role``` button
4.  For ```Trusted Entity Type```, select AWS Service
5.  For ```Use case```, select EC2
6.  Click ```Next```
7.  Steps to set the Permissions Policies
	1. Search CloudWatchLogsReadOnlyAccess and select it
8.  Click ```Next```
9.  Enter the role name
10. Click ```Create Role```
11. Search for the created role and open it
12.	In the ```Permissions``` tab, click the ```Add Permissions``` button and select ```Create Inline Policy```
13.	On the ```Create Policy``` page, select JSON editor and add the below policy

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

14. Click ```Review Policy```
15. Enter the policy Name and click ```Create Policy```
16. In order to restrict access to a particular log group only, perform below steps else move to Step 21
17.	In the ```Permissions``` tab, click the ```Add Permissions``` button and select ```Create Inline Policy```
18.	On the ```Create Policy``` page, select JSON editor and add the below policy

```
{
    "Version": "2012-10-17",
    "Statement": {
        "Effect": "Deny",
        "NotAction": "logs:DescribeLogGroups",
        "NotResource": [
            "arn:aws:logs:<AWS log group Region>:<AWS Account>:log-group:<log_group name>:*"
        ]
    }
}
```

19. Click ```Review Policy```
20. Enter the policy Name and click ```Create Policy```
21. Select the role created above
22. Click the ```Trust relationships``` tab and click ```Edit trust policy```
23. Add the below statement in the trust policy and click ```Update Policy```

```
{
        "Effect": "Allow",
        "Principal": {
            "AWS": "arn:aws:iam::<AWS Account>:role/<Role Name>"
        },
        "Action": "sts:AssumeRole"
}
```

24. Set the role to the EC2 machine hosting Guardium
25. Go to the EC2 machine hosting Guardium
26. Right click on the EC2 instance, select the Security Option, and modify the IAM role
27. Set the role that was created above

## Configuration for IAM Role when the Guardium Data Protection and the Database to be monitored are in different AWS accounts

### Prerequisites:

1.  AWS Account1 where IBM Guardium is hosted on EC2 instance. For simplicity consider this as Account with Account ID 111111
2.  AWS Account2 where RDS is present. For simplicity consider this as Account with Account ID 222222
3.  Log group in the Account2 where RDS logs are logged. For simplicity consider test-log-group is the log group that is logging the logs in the region us-east-1

### Steps to create Roles:

1.  Log in to your IAM console (https://console.aws.amazon.com/iam/) of first AWS Account where IBM Guardium is hosted for e.g., with Account ID 111111
2.  Click the ```Roles``` tab under ```Access Management```
3.  Click the ```Create Role``` button
4.  For ```Trusted Entity Type```, select AWS Service
5.  For ```Use case```, select EC2
6.  Click ```Next```
9.  Enter the role name e.g., role_on_111111
10. Click ```Create Role```
11. Repeat steps fom 1 to 10 on second AWS Account i.e., with Account ID 222222 and create a role with name for e.g., role_on_222222

### Steps to add Permissions and Policies to the Role created on the Account that has Guardium EC2:

1.  Log in back to the first AWS Account.
2.  Search for the created role i.e., role_on_111111 and open it
3.	In the ```Permissions``` tab, click the ```Add Permissions``` button and select ```Create Inline Policy```
4.	On the ```Create Policy``` page, select JSON editor and add the below policy

```
	{
		"Version": "2012-10-17",
		"Statement": [
		{
			"Sid": "VisualEditor0",
			"Effect": "Allow",
			"Action": "sts:AssumeRole",
			"Resource": ["arn:aws:iam::<AccountID_of_RDS>:role/<Role_In_Second_Account>"]
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

5.  Click ```Review Policy```
6.  Enter the policy Name and click ```Create Policy```
7.  The role named role_on_111111 is edited with the above inline policy.

### Steps to add Permissions and Policies to the Role created on the Account that has the RDS:

1.  Log in to the Second AWS Account with account id 222222
2.  Search for the created role i.e., role_on_222222 and open it
3.  Steps to set the Permissions Policies, to allow read permissions to CloudWatchLogs
	1. Search CloudWatchLogsReadOnlyAccess and select it
4.	In the ```Permissions``` tab, click the ```Add Permissions``` button and select ```Create Inline Policy```
5.  Here you can add policy to eliminate access except for the one log group e.g., test-log-group.
5.	On the ```Create Policy``` page, select JSON editor and add the below policy

Inline policy –
```
{
	"Version": "2012-10-17",
	"Statement": {
		"Effect": "Deny",
		"NotAction": "logs:DescribeLogGroups",
		"NotResource": [
				"arn:aws:logs:<Region_of_Database>:<Account_Id_Of_RDS>:log-group:<log_group_to_be_monitored>:*"]
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
		"NotAction": "logs:DescribeLogGroups",
		"NotResource": [
			"arn:aws:logs:us-east-1:222222:log-group:test-log-group:*"
		]
	}
}
```

6. Select the role created above
7. Click the ```Trust relationships``` tab and click ```Edit trust policy```
8. Add the below statement in the trust policy and click ```Update Policy```

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

9. Set the role created on first AWS Account i.e., role_on_111111 to the EC2 machine hosting Guardium
10. Go to the EC2 machine hosting Guardium
11. Right click on the EC2 instance, select the Security Option, and modify the IAM role
12. Set the role that was created above


## Configuring input plugin on Guardium:

### Configuration:

Update the input section to add the details from the corresponding file's input part, omitting the keyword "input{" at the beginning and its corresponding "}" at the end.

The sample configuration looks like:

```
input {
		cloudwatch_logs {
			#Mandatory arguments:
			#Insert the log group that is created for the data instance 
			#Example of log group for AWS postgres RDS, /aws/rds/instance/<instance_name>/postgresql i.e., ["/aws/rds/instance/database-1/postgresql"]
			#Example of log group for Aurora Postgres, /aws/rds/cluster/<instance_name>/postgresql i.e., ["/aws/rds/cluster/aurorapostgres/postgresql"]
			log_group => ["<LOG_GROUP>"]  #e.g., ["/aws/rds/instance/database-1/postgresql"]
			start_position => "end"
			#Insert the role_arn of the role that is created in RDS account.
			role_arn => "<ROLE_ARN_ON_RDS_ACCOUNT>"   #e.g., "arn:aws:iam::222222:role/role_on_222222"
			region => "<REGION>" #Region that has the DB, Default value: us-east-1
			interval => 2
			event_filter => ""
			type => "<TYPE>"  #e.g., Postgres
			#Insert the account id of the AWS account
			add_field => {"account_id" => "<ACCOUNT_ID>"}
			#Add the below parameter with default value logstash
			role_session_name => "logstash"
		}
	}
```
