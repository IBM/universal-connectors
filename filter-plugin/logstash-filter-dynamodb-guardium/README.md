# Dynamodb-Guardium Logstash filter plug-in

This is a [Logstash](https://github.com/elastic/logstash) filter plug-in for the universal connector that is featured in IBM Security Guardium. It parses events and messages from the Amazon DynamoDB audit log into a Guardium record instance (which is a standard structure made out of several parts). The information is then sent over to Guardium. Guardium records include the accessor (the person who tried to access the data), the session, data, and exceptions. If there are no errors, the data contains details about the query "construct". The contstruct details the main action (verb) and collections (objects) involved. 

The plug-in is free and open-source (Apache 2.0). It can be used as a starting point to develop additional filter plug-ins for Guardium universal connector.

##Configuring Amazon DynamoDB and sending logs to CloudWatch

In the AWS web interface, configure the service for Dynamodb:

#Procedure

	1. Go to  https://console.aws.amazon.com/:
		a) Click Services in the top left menu.
		b) In the Database section, click DynamoDB.
		c) Select the required region in the top right corner.
		d) Click Create Table.
		e) Provide the Table name and Primary key.
		f) Click Create.


##Enable logging through CloudTrail

There are different ways for auditing and logging. We will use CloudTrail for this example since it supports all required parameters. 

#Procedure

    1. Click Services in the top left menu.
    2. In the Storage section, select S3.
    3. Create an S3 bucket.
    4. Select the appropriate region.
    5. In Management & Governance, select CloudTrail.
    6. Click Create Trail.
    7. Enter the trail name.
    8. For Storage location, choose Create new S3 bucket to create a bucket.
    9. Deselect the check box to disable Log file SSE-KMS encryption.
    10. Enable CloudWatch logs.
    11. Create a new Log group and IAM role.
    12. Click Next.
    13. For Choose log event, select Data events - and then deselect Management events.
    14. Deselect the Read and Write option for All current and future S3 buckets.
    15. Browse the created S3 bucket - and then enable read and write options.
    16. Preview the details provided and then click Create Trail.


##View the logs entries on CloudWatch

#Procedure

	1. Click the Services drop down.
    2. Enter the CloudWatch string in the search box.
    3. Click CloudWatch to redirect to the CloudWatch dashboard.
    4. In the left pane, select Logs.
    5. Click Log Groups.
    6. Search for the log group that you created in the previous step.
    7. In the log group, locate the <account_id>CloudTrail_<region> file. All events are logged to this file.


**Notes:**```
These events are supported :
    1. UpdateTable event
    2. CreateTable event
    3. DescribeTable event
    4. ListTables event
    5. DeleteTable event
    6. Error event
```
