# Dynamodb-Guardium Logstash filter plug-in

### Meet DynamoDB
* Environment: AWS
* Supported Guardium versions:
    * Guardium Data Protection: 11.4 and later
        * Supported inputs:
            * S3 (pull)
            * CloudWatch (pull)
            * SQS (Pull)
    * Guardium Insights: 3.3
        * Supported inputs:
            * CloudWatch (pull)
    * Guardium Insights SaaS: 1.0
        * Supported inputs:
            * CloudWatch (pull)

This is a [Logstash](https://github.com/elastic/logstash) filter plug-in for the universal connector that is featured in IBM Security Guardium. It parses events and messages from the Amazon DynamoDB audit log into a [Guardium record](https://github.com/IBM/universal-connectors/blob/main/common/src/main/java/com/ibm/guardium/universalconnector/commons/structures/Record.java) instance (which is a standard structure made out of several parts). The information is then sent over to Guardium. Guardium records include the accessor (the person who tried to access the data), the session, data, and exceptions. If there are no errors, the data contains details about the query "construct". The construct details the main action (verb) and collections (objects) involved.

The plug-in is free and open-source (Apache 2.0). It can be used as a starting point to develop additional filter plug-ins for Guardium universal connector.


## 1. Configuring Amazon DynamoDB

In the AWS web interface, configure the service for Dynamodb.

### Procedure

1. Go to https://console.aws.amazon.com/
2. Click **Services** in the top left menu.
3. Underneath **All services**, click on **Database**.
4. On the right panel, click **DynamoDB**.
5. At the top right, click on the dropdown menu and select your region.
6. Click the orange **Create Table** button.
7. Enter a table name.
8. Enter a partition key.
9. Scroll down and click the orange **Create table** button.

## 2. Enabling audit logs 

There are different methods for auditing and logging. We will use CloudTrail for this example since it supports all required parameters. The following events are supported for auditing in AWS.


### Procedure

1. Click **Services** in the top left menu.
2. Underneath **All services**, click on **Management & Governance**.
3. On the right panel, click **Cloud Trail**.
4. Click **Create trail** button.
5. Provide a trail name under **Trail name**.
6. Under **Storage location**, verify that **Create new S3 bucket** is selected.
7. Under **Log file SSE-KMS encryption**, clear the Enabled box.
8. If the logs are to be monitored through CloudWatch, then forward them to Cloudwatch using steps 9 to 13. (If not, skip those steps).
9. Under **CloudWatch Logs**, check the **Enabled** box.
10. Verify **New** is selected for **Log group**.
11. Under **Log group name**, provide a new log group name.
12. Verify **New** is selected for **IAM Role**.
13. For **Role name**, provide a new role name.
14. Click **Next**.
15. For **Event type**, select **Management events** and **Data events**.
16. Verify that **Read** and **Write** are selected for **API Activity**.
17. In the **Data Events** section, click **Switch to basic event selectors**.
18. Click **Continue** to confirm.
19. Click **Add data event type**.
20. Click **Data event source** and select **DynamoDB**.
21. Click **NEXT**.
22. Verify that all parameters shown are correct.
23. Click **Create trail**.

## Follow the below link if DynamoDB is to be monitored using Cloudwatch

[DynamoDB Over Cloudwatch](https://github.com/IBM/universal-connectors/blob/main/filter-plugin/logstash-filter-dynamodb-guardium/DynamodbOverCloudwatch/README.md)

## Follow the below link if DynamoDB is to be monitored using Cloudtrail

[DynamoDB Over Cloudtrail](https://github.com/IBM/universal-connectors/blob/main/filter-plugin/logstash-filter-dynamodb-guardium/DynamodbOverCloudtrail/README.md)

### Limitations

1. The Dynamo DB plug-in does not support IPV6.
2. You may need to disable management events in order to avoid heavy traffic and data loss in Guardium. Disabling management events disables logging of the following events: 
CreateTable, DeleteTable, ListTable, UpdateTable, DescribeTable events.
3. The following fields couldn't be mapped with the Dynamo audit logs,
   1. Client HostName : Not available with audit logs so set as NA.
