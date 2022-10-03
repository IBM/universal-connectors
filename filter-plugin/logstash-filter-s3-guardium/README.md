# S3-Guardium Logstash filter plug-in

This is a [Logstash](https://github.com/elastic/logstash) filter plug-in for the univer***REMOVED***l connector that is featured in IBM Security Guardium. It parses S3 database events into a Guardium record instance (which is a standard structure made out of several parts). The information is then sent over to Guardium. Guardium records include the accessor (the person who tried to access the data), the session, data, and exceptions. If there are no errors, the data contains details about the query "construct". The construct details the main action (verb) and collections (objects) involved. 

The plug-in is free and open-source (Apache 2.0). It can be used as a starting point to develop additional filter plug-ins for Guardium univer***REMOVED***l connector.

## Filter notes
* The filter supports events sent through Cloudwatch or SQS.

# Univer***REMOVED***l connector for CloudWatch with S3 in a single account

## Configuring Amazon AWS CloudTrail to send log files to CloudWatch

The full AWS documentation is in
https://docs.aws.amazon.com/awscloudtrail/latest/userguide/send-cloudtrail-events-to-cloudwatch-logs.html

### Procedure

1. Go to https://console.aws.amazon.com/cloudtrail

a.	Click Trails in the left menu

b.	Click Create trail and enter the trail name

c.	Fill in the details

![General details]()

2. Enable the CloudWatch logs, select the target log group, and click Next

![CloudWatch logs]()

3. Select both Management events and Data events

![Log events]()

4. Select S3 as the data event source and the buckets that you want to monitor and click Next (Here we switched to basic event selectors)

![Data events]()



5. In the `Summary` screen, validate that the data is accurate and click Create

 ![Summary]()

## Configuring an IAM role for CloudWatch integration

1. Log in to your IAM console (https://console.aws.amazon.com/iam/).

   a. Create a role

2. Select Roles in the left-hand navigation and then create a new IAM role that has `CloudWatchLogsReadOnlyAccess` and `sts:AssumeRole` permissions

(For both the role and every instance that will use it: ``"arn:aws:iam::346824953529:role/ec2_single_account_cloudwatch_logs/*"``, ``"arn:aws:iam::346824953529:role/ec2_single_account_cloudwatch_logs"``.)

 ![Roles]()

  ![IAM Roles]()

3. Select AWS service as Trusted entity type and EC2 as a use case.

 ![AWS + EC2]()



4. Set the role to the ec2 machine hosting Guardium

   a. Go to the ec2 machine hosting Guardium and modify the IAM role to the one you created

   ![ec2 Roles]()

    ![IAM ec2 Roles]()

5. VPC endpoint- In cases where Cloudwatch Logs is outside the VPC of the ec2 machine hosting Guardium, you can create a VPC endpoint that will establish a private connection between your VPC and CloudWatch Logs by following the instructions in:
https://docs.aws.amazon.com/AmazonCloudWatch/latest/logs/cloudwatch-logs-and-interface-VPC.html

## 3. Configuring the univer***REMOVED***l connector in Guardium

1. Log in to Guardium

2. Go to `Configure Univer***REMOVED***l Connector`

3. Select Connector template as Amazon S3 using CloudWatch

 ![Connector configuration 1]()

4. Fill in the log group and the role_arn that were assigned to the ec2

 ![Connector configuration 2]()

# Univer***REMOVED***l connector for CloudWatch with S3 in a single account

## Configuring Amazon AWS CloudTrail to send log files to CloudWatch

The full AWS documentation is in
https://docs.aws.amazon.com/awscloudtrail/latest/userguide/send-cloudtrail-events-to-cloudwatch-logs.html

### Procedure

1. Go to https://console.aws.amazon.com/cloudtrail

a.	Click Trails in the left menu

b.	Click Create trail and enter the trail name

c.	Fill in the details

![General details]()

2. Enable the CloudWatch logs, select the target log group, and click Next

![CloudWatch logs]()

3. Select both Management events and Data events

![Log events]()

4. Select S3 as the data event source and the buckets that you want to monitor and click Next (Here we switched to basic event selectors)

![Data events]()



5. In the `Summary` screen, validate that the data is accurate and click Create

 ![Summary]()

## Configuring an IAM role for CloudWatch integration

1. Log in to your IAM console (https://console.aws.amazon.com/iam/).

   a. Create a role

2. Select Roles in the left-hand navigation and then create a new IAM role that has `CloudWatchLogsReadOnlyAccess` and `sts:AssumeRole` permissions

(For both the role and every instance that will use it: ``"arn:aws:iam::346824953529:role/ec2_single_account_cloudwatch_logs/*"``, ``"arn:aws:iam::346824953529:role/ec2_single_account_cloudwatch_logs"``.)

 ![Roles]()

  ![IAM Roles]()

3. Select AWS service as Trusted entity type and EC2 as a use case.

 ![AWS + EC2]()



4. Set the role to the ec2 machine hosting Guardium

   a. Go to the ec2 machine hosting Guardium and modify the IAM role to the one you created

   ![ec2 Roles]()

    ![IAM ec2 Roles]()

5. VPC endpoint- In cases where Cloudwatch Logs is outside the VPC of the ec2 machine hosting Guardium, you can create a VPC endpoint that will establish a private connection between your VPC and CloudWatch Logs by following the instructions in:
https://docs.aws.amazon.com/AmazonCloudWatch/latest/logs/cloudwatch-logs-and-interface-VPC.html

## Configuring the univer***REMOVED***l connector in Guardium

1. Log in to Guardium

2. Go to `Configure Univer***REMOVED***l Connector`

3. Select Connector template as Amazon S3 using CloudWatch

 ![Connector configuration 1]()

4. Fill in the log group and the role_arn that were assigned to the ec2

 ![Connector configuration 2]()
## Configuring the Amazon S3 over Cloudwatch_logs in Guardium Insights

To configure this plug-in for Guardium Insights, follow [this guide.](https://github.com/IBM/univer***REMOVED***l-connectors/blob/main/docs/UC_Configuration_GI.md)

In the input configuration section, refer to the CloudWatch_logs section.

## Contribute

The documentation for the Logstash Java plug-ins is available [here](https://www.elastic.co/guide/en/logstash/current/contributing-java-plugin.html).

You can enhance this filter and open a pull request with suggested changes - or you can use the project to create a different filter plug-in for Guardium that supports other data sources.

To build and create an updated GEM of this filter plug-in which can be installed onto Logstash: 
1. Build Logstash from the repository source.
2. Create or edit _gradle.properties_ and add the LOGSTASH_CORE_PATH variable with the path to the logstash-core folder. For example: 
    
    ```LOGSTASH_CORE_PATH=/Users/taldan/logstash76/logstash-core```

3. Run ```$ ./gradlew.unix gem --info``` to create the GEM (ensure you have JRuby installed beforehand, as described [here](https://www.ibm.com/docs/en/guardium/11.3?topic=connector-developing-plug-ins)).
