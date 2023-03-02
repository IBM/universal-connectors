## sqs input plug-in
### Meet SQS
* Tested versions: 3.1.3
* Developed by Elastic
* Supported versions:
    * GDP: 11.3 and above
    * GI: 3.2

This is a [Logstash](https://github.com/elastic/logstash) input plug-in for the univer***REMOVED***l connector that is featured in IBM Security Guardium. It pulls events from the SQS from the Amazon Web Services. The events are then sent over to corresponding filter plugin which transforms these audit logs into a [Guardium record](https://github.com/IBM/univer***REMOVED***l-connectors/blob/main/common/src/main/java/com/ibm/guardium/univer***REMOVED***lconnector/commons/structures/Record.java)  instance (which is a standard structure made out of several parts). The information is then sent over to Guardium. Guardium records include the accessor (the person who tried to access the data), the session, data, and exceptions. If there are no errors, the data contains details about the query "construct". The construct details the main action (verb) and collections (objects) involved.


## Purpose:

This plugin pulls events from an Amazon Web Services Simple Queue Service (SQS) queue.

SQS is a simple, scalable queue system that is part of the Amazon Web Services suite of tools.

## U***REMOVED***ge:

### a. Prerequisites:

1. Have an AWS account

2. Setup an SQS queue

3. Create an identity that has access to consume mes***REMOVED***ges from the queue.

4. The "consumer" identity must have the following permissions on the queue:

	sqs:ChangeMes***REMOVED***geVisibility
	
	sqs:ChangeMes***REMOVED***geVisibilityBatch
	
	sqs:DeleteMes***REMOVED***ge
	
	sqs:DeleteMes***REMOVED***geBatch
	
	sqs:GetQueueAttributes
	
	sqs:GetQueueUrl
	
	sqs:ListQueues
	
	sqs:ReceiveMes***REMOVED***ge

5. Create a user and apply the below IAM Policy  to the user

	{
      "Statement": [
        {
          "Action": [
            "sqs:ChangeMes***REMOVED***geVisibility",
            "sqs:ChangeMes***REMOVED***geVisibilityBatch",
            "sqs:DeleteMes***REMOVED***ge",
            "sqs:DeleteMes***REMOVED***geBatch",
            "sqs:GetQueueAttributes",
            "sqs:GetQueueUrl",
            "sqs:ListQueues",
            "sqs:ReceiveMes***REMOVED***ge"
          ],
          "Effect": "Allow",
          "Resource": [
            "arn:aws:sqs:us-east-1:123456789012:Logstash"
          ]
        }
      ]
    }

### b. Parameters:
	
| Parameter | Input Type | Required | Default |
|-----------|------------|----------|---------|
| access_key_id | String  | No |  |
| secret_access_key | String  | No |  |
| polling_frequency | Number | No | 20 |
| queue | String | Yes |  |
| region | String | No |  |



#### `access_key_id`
The `access_key_id` setting allows to set the access key id for the user having access to the SQS. This plugin uses the AWS SDK and supports several ways to get credentials, which will be tried in this order:

	1. Static configuration, using access_key_id and secret_access_key params in logstash plugin config
	
	2. External credentials file specified by aws_credentials_file

	3. Environment variables AWS_ACCESS_KEY_ID and AWS_SECRET_ACCESS_KEY
	
	4. Environment variables AMAZON_ACCESS_KEY_ID and AMAZON_SECRET_ACCESS_KEY

	5. IAM Instance Profile (available when running inside EC2)

#### `secret_access_key`
The `secret_access_key` setting allows to set The AWS Secret Access Key.

#### `polling_frequency`
The `polling_frequency` setting allows specify the frequency after which the Queue is to be polled

#### `queue`
The `queue` setting allows to specify the Name of the SQS Queue name to pull mes***REMOVED***ges from. Note that this is just the name of the queue, not the URL or ARN.

#### `region`
The `region` allows setting the region where the SQS is present.


#### Logstash Default config params
Other standard logstash parameters are available such as:
* `add_field`
* `type`
* `tags`

### Example

	input {
		sqs {
			access_key_id => "<access key id>"
			secret_access_key => "<access secret key>"
			queue => "<queue name>"
			region => "<region where SQS is created>"
		}
	}

### Authorizing outgoing traffic from AWS to Guardium

#### Procedure
1. Log in to the Guardium Collector's API.
2. Issue these commands:
		• grdapi add_domain_to_univer***REMOVED***l_connector_allowed_domains domain=amazonaws.com
		• grdapi add_domain_to_univer***REMOVED***l_connector_allowed_domains domain=amazon.com