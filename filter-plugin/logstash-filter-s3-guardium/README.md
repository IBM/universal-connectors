# S3-Guardium Logstash filter plug-in
### Meet S3
* Environment: AWS
* Supported inputs: CloudWatch (pull), SQS (pull)
* Supported Guardium versions:
  * Guardium Data Protection: 11.4 and above
    * Supported inputs:
      * Cloudwatch logs (pull)
      * SQS (pull)
  * Guardium Insights: 3.2
    * Supported inputs:
      * Cloudwatch logs (pull)
      * SQS (pull)
  * Guardium Insights SaaS: 1.0
    * Supported inputs:
      * Cloudwatch logs (pull)
      * SQS (pull)

This is a [Logstash](https://github.com/elastic/logstash) filter plug-in for the universal connector that is featured in IBM Security Guardium. It parses S3 database events into a Guardium record instance (which is a standard structure made out of several parts). The information is then sent over to Guardium. Guardium records include the accessor (the person who tried to access the data), the session, data, and exceptions. If there are no errors, the data contains details about the query "construct". The construct details the main action (verb) and collections (objects) involved. 

The plug-in is free and open-source (Apache 2.0). It can be used as a starting point to develop additional filter plug-ins for Guardium universal connector.

## Filter notes
* The filter supports events sent through Cloudwatch or SQS.

# Universal connector for CloudWatch with S3 in a single account

## Configuring Amazon AWS CloudTrail to send log files to CloudWatch

The full AWS documentation is in
https://docs.aws.amazon.com/awscloudtrail/latest/userguide/send-cloudtrail-events-to-cloudwatch-logs.html

### Procedure

1. Go to https://console.aws.amazon.com/cloudtrail

    a.	Click Trails in the left menu

    b.	Click Create trail and enter the trail name

    c.	Fill in the details

![General details](/docs/images/cloudwatch/general_details.png)

2. Enable the CloudWatch logs, select the target log group, and click Next

![CloudWatch logs](/docs/images/cloudwatch/CloudWatch_logs.png)

3. Select both Management events and Data events

![Log events](/docs/images/cloudwatch/log_events.png)

4. Select S3 as the data event source and the buckets that you want to monitor and click Next (Here we switched to basic event selectors)

![Data events](/docs/images/cloudwatch/data_events.png)



5. In the `Summary` screen, validate that the data is accurate and click Create

 ![Summary](/docs/images/cloudwatch/summary.png)

## Configuring an IAM role for CloudWatch integration

1.	Log in to your IAM console (https://console.aws.amazon.com/iam/).

   a. Create a role

2.	Select ```AWS service``` as ```Trusted entity``` type and ```EC2``` as a ```use case```. Click ```Next```

![use case](/docs/images/cloudwatch/use_case.png)

3.	Search ```“CloudWatchLogsReadOnlyAccess“``` in ```policy filter``` and select it. Click ```Next```

![policy filter](/docs/images/cloudwatch/policy_filter.png)

4.	Enter ```RoleName```

![role name](/docs/images/cloudwatch/role_name.png)

5.	Click ```Create Role```

![create role](/docs/images/cloudwatch/create_role.png)

6.	Search for the created role and open it.

7.	In the ```Permissions``` tab, click the ```Add Permissions``` button and select ```Create Inline Policy```

8.	On the ```Create Policy``` page, select JSON editor and add the below policy.
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
        }
    ]
}
 ```
 ![create policy](/docs/images/cloudwatch/create_policy.png)

9.	Click ```Review Policy```

10.	Enter the policy name and click ```Create Policy```

![create policy 2](/docs/images/cloudwatch/create_policy_2.png)

11.	On ```Role```, click the ```Trust relationships``` tab, click ```Edit trust policy```

12.	Add the below statement in ```trust policy``` and click  ```Update Policy```:

```
{
            "Effect": "Allow",
            "Principal": {
                "AWS": "arn:aws:sts::<AWS Account>:assumed-role/<Role Name>/<EC2 Instance Id>"
            },
            "Action": "sts:AssumeRole"
        }
```
![update policy](/docs/images/cloudwatch/update_policy.png)

13.	Set the role to the ec2 machine hosting Guardium

 a.	Go to the ec2 machine hosting Guardium and modify the IAM role to the one you created
![iam role](/docs/images/cloudwatch/iam_role.png)
![iam role 2](/docs/images/cloudwatch/iam_role_2.png)


14.	VPC endpoint- In cases where Cloudwatch Logs is outside the VPC of the ec2 machine hosting Guardium, you can create a VPC endpoint that will establish a private connection between your VPC and CloudWatch Logs by following the instructions in: https://docs.aws.amazon.com/AmazonCloudWatch/latest/logs/cloudwatch-logs-and-interface-VPC.html In case arn role based authentication is used in input - in addition to connection to Cloudwatch, VPC connection to STS should also be established between ec2 machine and Cloudwatch accout

To authorize outgoing traffic from Amazon Web Services (AWS) to Guardium, run these APIs:
```
grdapi add_domain_to_universal_connector_allowed_domains domain=amazonaws.com
grdapi add_domain_to_universal_connector_allowed_domain
```


## 3. Configuring the universal connector in Guardium

1. Log in to Guardium

2. Go to `Configure Universal Connector`

3. If the audit logs are to be fetched from s3 directly,
    1. Select Connector template as Amazon S3 using CloudWatch

       ![Connector configuration 1](/docs/images/cloudwatch/connector_configuration_1.png)

    2. Fill in the log group and the role_arn that were assigned to the ec2

       ![Connector configuration 2](/docs/images/cloudwatch/connector_configuration_2.png)

4.  Note :To configure SQS on AWS, follow the steps mentioned in the [SQS input plug-in](/input-plugin/logstash-input-sqs/README.md) readme file.</br>
    If the audit logs are to be fetched from S3 directly,
    1. Select **Amazon S3 using SQS** in **Connector template**.

       ![Connector configuration 3](/docs/images/cloudwatch/connector_configuration_3.png)

    2. Fill in the queue name and relevant details

       ![Connector configuration 4](/docs/images/cloudwatch/connector_configuration_4.png)

5. The "type" fields should match in input and filter configuration sections. This field should be unique for every individual connector added.
6. Click **Save**. Guardium validates the new connector, and enables the universal connector if it was
   disabled. After it is validated, it appears in the Configure Universal Connector page.


## Configuring the Amazon S3 over Cloudwatch_logs in Guardium Insights

To configure this plug-in for Guardium Insights, follow [this guide.](/docs/Guardium%20Insights/3.2.x/UC_Configuration_GI.md)

For the input configuration step, refer to the [CloudWatch_logs section](/docs/Guardium%20Insights/3.2.x/UC_Configuration_GI.md#configuring-a-CloudWatch-input-plug-in).