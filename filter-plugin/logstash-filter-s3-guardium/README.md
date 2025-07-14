# S3-Guardium Logstash filter plug-in
### Meet S3
* Environment: AWS
* Supported inputs: CloudWatch (pull), SQS (pull)
* Supported Guardium versions:
  * Guardium Data Protection: 11.4 and above
    * Supported inputs:
      * Cloudwatch logs (pull)
      * SQS (pull)
  * Guardium Data Security Center SaaS: 1.0
    * Supported inputs:
      * Cloudwatch logs (pull)
      * SQS (pull)

This is a [Logstash](https://github.com/elastic/logstash) filter plug-in for the universal connector that is featured in IBM Security Guardium. It parses S3 database events into a Guardium record instance (which is a standard structure made out of several parts). The information is then sent over to Guardium. Guardium records include the accessor (the person who tried to access the data), the session, data, and exceptions. If there are no errors, the data contains details about the query "construct". The construct details the main action (verb) and collections (objects) involved. 

The plug-in is free and open-source (Apache 2.0). It can be used as a starting point to develop additional filter plug-ins for Guardium universal connector.

## Filter notes
* The filter supports events sent through Cloudwatch or SQS.

# Universal connector for CloudWatch with S3 in a single account

## 1. Configuring Amazon AWS CloudTrail to send log files to CloudWatch

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

## 2. Configuring an IAM role for CloudWatch integration

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

## 3. Exporting the Logs originating from S3 to SQS using Event Rule

In order to pull the logs from SQS, we need to push the logs originating from S3 to SQS.

**_Procedure_**
1. Go to https://console.aws.amazon.com/
2. Click **Services**.
3. Search for Amazon EventBridge and click on **Rules**.
4. Click **Create Rule**.
5. Enter the name for the rule.
6. Enter the description for the rule.
7. In the Rule Type select **Rule with an Event Pattern**.
8. Keep the rest of the default settings.
9. Click on **Next**.
10. In Event Source select **AWS Events or EventBridge Partner Events**.
11. Skip the **Sample Event**.
12. Keep Default settings for **Creation Method**.
13. In the Event Pattern Select **Edit Pattern** and enter the below pattern
```
}
  "source": [
    "aws.s3"
  ]
}
```
14. Click on **Next**.
15. In the Target1, select **Target Types** as **AWS Service**.
16. Select the target as **SQS Queue**.
17. Select the **Queue**.
18. Click on **Next**.
19. Add the **Tags** if required.
20. Review the settings and click on **Create Rule**.


## 4. Exporting Cloudwatch Logs to SQS using lambda function
In order to achieve load balancing of audit logs between different collectors, the audit logs must be exported
from Cloudwatch to SQS.

### Creating the SQS queue
The SQS created in these steps will contain the messages to be filled up by the lambda function
(created in next section) in the queue by reading the CloudWatch logs. The messages inside the SQS will
contain content from CloudWatch logs.

**_Procedure_**
1. Go to https://console.aws.amazon.com/
2. Click **Services**
3. Search for SQS and click on **Simple Queue Services**
4. Click **Create Queue**.
5. Select the type as **Standard**.
6. Enter the name for the queue
7. Keep the rest of the default settings

### Creating a policy for the relevant IAM User
Perform the below steps for the IAM user who is accessing the SQS logs in Guardium:

**_Procedure_**
1. Go to https://console.aws.amazon.com/
2. Go to **IAM service** > **Policies** > **Create Policy**.
3. Select **service as SQS**.
4. Check the following checkboxes:  **ListQueues**, **DeleteMessage**, **DeleteMessageBatch**, **GetQueueAttributes**,
   **GetQueueUrl**, **ReceiveMessage**, **ChangeMessageVisibility**, **ChangeMessageVisibilityBatch**.
5. In the resources, specify the ARN of the queue created in the above step.
6. Click **Review policy** and specify the policy name.
7. Click **Create policy**.
8. Assign the policy to the user
    1. Log in to the IAM console as an IAM user (https://console.aws.amazon.com/iam/).
    2. Go to **Users** on the console and select the relevant IAM user to whom you want to give permissions.
       Click the **username**.
    3. In the **Permissions tab**, click **Add permissions**.
    4. Click **Attach existing policies directly**.
    5. Search for the policy created and check the checkbox next to it.
    6. Click **Next: Review**
    7. Click **Add permissions**

### Creating the Lambda function
The Lambda function will read the CloudWatch Logs and send the events into the SQS queue.
Follow the steps below to configure the Lambda function.

#### Creating IAM Role
Create the IAM role that will be used in the Lambda function setup. The AWS Lambda service will require permission to
log events and write to the SQS created. Create the IAM Role **Export-Redshift-CloudWatch-to-SQS-Lambda** with
"AmazonSQSFullAccess", "CloudWatchLogsFullAccess", and "CloudWatchEventsFullAccess" policies.

__*Procedure*__
1. Go to https://console.aws.amazon.com/
2. Go to **IAM** -> **Roles**
3. Click **Create Role**
4. Under use case select **Lambda** and click **Next**
5. Search for **AmazonSQSFullAccess** and select it
6. Search for **CloudWatchLogsFullAccess** and select it
7. Search for **CloudWatchEventsFullAccess** and select it
8. Set the Role Name: e.g., "Export-Redshift-CloudWatch-Logs-To-SQS" and click **Create role**.

### Create the lambda function

__*Procedure*__
1. Go to https://console.aws.amazon.com/
2. Go to **Services**. Search for **lambda function**.
3. Click **Functions**
4. Click **Create Function**
5. Keep **Author for Scratch** selected
6. Set Function name e.g., Export-S3-CloudWatch-Logs-To-SQS
7. Under **Runtime**, **select Python 3.x**
8. Under **Permissions**, select **Use an existing role** and select the IAM role that you created in
   the previous step (Export-S3-CloudWatch-to-SQS-Lambda)
9. Click **Create function** and navigate to **Code view**
10. Add the function code from the file [s3Lambda.py](S3OverSQSUsingLambdaPackage/s3Lambda.py)
11. Click **Configuration** > **Environment Variables**
12. Create 2 variables:
    1. Key = GROUP_NAME value = e.g., uc-s3-plugin-cloudwatch-group
    2. Key = QUEUE_NAME value = e.g., https://sqs.ap-south-1.amazonaws.com/1111111111/np-s3-sqs
13. Save the function
14. Click on the **Deploy** button

#### Automating the lambda function
The Lambda will be called by a scheduler configured inside event rules in CloudWatch.

**_Procedure_**
1. Go to the CloudWatch dashboard.
2. Go to **Events** > **Rules**.
3. Click **Create Rule**.
4. Enter the name for the rule e.g., cloudwatchToSqs
5. Under **Rule Type**, select **Schedule**.
6. Define the schedule. In **schedule pattern**, select a schedule that runs at a regular rate,
   such as every 10 minutes.
7. Enter the rate expression, meaning the rate at which the function should execute.
   This value must match the time specified in the lambda function code that calculates the time delta.
   (If the function code it is set to 2 minutes, set the rate to 2 minutes unless changed in the code). Click **Next**.
8. Select **Target1**. Select the **Target Type** as **AWS Service**.
9. Select **Target** as **Lambda Function**.
10. Select the lambda function created in the above step. e.g., Export-S3-CloudWatch-to-SQS-Lambda.
11. Add the tag if needed.
12. Click Create Rule.

#### Note
Before making any changes to the lambda function code, first disable the above rule.
Deploy the change and then re-enable the rule.

To authorize outgoing traffic from Amazon Web Services (AWS) to Guardium, run these APIs:
```
grdapi add_domain_to_universal_connector_allowed_domains domain=amazonaws.com
grdapi add_domain_to_universal_connector_allowed_domains domain=amazon.com
```

## 5. Configuring the universal connector in Guardium

### Before you begin

* For Guardium Data Protection version 11.0p540 and/or 11.0p6505 and/or 12.0 and/or 12p15 download the [cloudwatch_logs plug-in](../../input-plugin/logstash-input-cloudwatch-logs/CloudwatchLogsInputPackage/offline-logstash-input-cloudwatch_log_1_0_5.zip)

1. Log in to Guardium

2. Go to `Configure Universal Connector`

3. If the audit logs are to be fetched from Cloudwatch directly,

    1. Click **Upload File**, If you have installed Guardium Data Protection version 11.0p540 and/or 11.0p6505 and/or 12.0 and/or 12p15, select the offline [cloudwatch_logs plug-in](../../input-plugin/logstash-input-cloudwatch-logs/CloudwatchLogsInputPackage/offline-logstash-input-cloudwatch_log_1_0_5.zip). After it is uploaded, click OK.

    2. Select Connector template as Amazon S3 using CloudWatch

       ![Connector configuration 1](/docs/images/cloudwatch/connector_configuration_1.png)

    3. Fill in the log group and the role_arn that were assigned to the ec2

       ![Connector configuration 2](/docs/images/cloudwatch/connector_configuration_2.png)

4.  Note :To configure SQS on AWS, follow the steps mentioned in the [SQS input plug-in](/input-plugin/logstash-input-sqs/README.md) readme file.</br>

    If the audit logs are to be fetched from S3 directly,
    1. Select **Amazon S3 using SQS** in **Connector template**.

       ![Connector configuration 3](/docs/images/cloudwatch/connector_configuration_3.png)

    2. Fill in the queue name and relevant details

       ![Connector configuration 4](/docs/images/cloudwatch/connector_configuration_4.png)

5. If the audit logs are to be fetched from SQS LAMBDA,
    1. Use the details from the [s3-over-sqs.conf](S3OverSQSPackage/S3/S3OverSQS.conf) file.
       Update the input section to add the details from the corresponding file's input part, omitting the
       keyword "input{" at the beginning and its corresponding "}" at the end. More details on how to configure the
       relevant input plugin can be found [here](../../input-plugin/logstash-input-cloudwatch-logs/README.md).
    2. Use the details
       from the [s3-over-sqs.conf](S3OverSQSPackage/S3/S3OverSQS.conf) file. Update the filter section to add the details
       from the corresponding file's filter part, omitting the keyword "filter{" at the beginning and its corresponding "}"
       at the end. More details on how to configure the relevant input plugin can be
       found [here](../../input-plugin/logstash-input-cloudwatch-logs/README.md).

6. The "type" fields should match in the input and the filter configuration sections. This field should be unique for every individual connector added. This is no longer required starting v12p20 and v12.1.
7. Click **Save**. Guardium validates the new connector, and enables the universal connector if it was
   disabled. After it is validated, it appears in the Configure Universal Connector page.


## Configuring the Amazon S3 over Cloudwatch_logs in Guardium Data Security Center

To configure this plug-in for Guardium Data Security Center, follow [this guide.](/docs/Guardium%20Insights/3.2.x/UC_Configuration_GI.md)

For the input configuration step, refer to the [CloudWatch_logs section](/docs/Guardium%20Insights/3.2.x/UC_Configuration_GI.md#configuring-a-CloudWatch-input-plug-in).
