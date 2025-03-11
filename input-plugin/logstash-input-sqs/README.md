## SQS input plug-in
### Meet SQS
* Tested versions: 3.1.3
* Developed by Elastic
* Supported Guardium versions:
    * Guardium Data Protection: 11.4 and above
    * Guardium Insights: 3.2

This is a [Logstash](https://github.com/elastic/logstash) input plug-in for the universal connector that is featured in IBM Security Guardium. It pulls events from the SQS from the Amazon Web Services. The events are then sent over to corresponding filter plugin which transforms these audit logs into a [Guardium record](https://github.com/IBM/universal-connectors/blob/main/common/src/main/java/com/ibm/guardium/universalconnector/commons/structures/Record.java)  instance (which is a standard structure made out of several parts). The information is then sent over to Guardium. Guardium records include the accessor (the person who tried to access the data), the session, data, and exceptions. If there are no errors, the data contains details about the query "construct". The construct details the main action (verb) and collections (objects) involved.


## Purpose:

This plug-in pulls events from an Amazon Web Services Simple Queue Service (SQS) queue.

SQS is a simple, scalable queue system that is part of the Amazon Web Services suite of tools.

### Creating the SQS queue
**_Procedure_**
1. Go to https://console.aws.amazon.com/
2. Click **Services**
3. Search for SQS and click on **Simple Queue Services**
4. Click **Create Queue**.
5. Select the type as **Standard**.
6. Enter the name for the queue.
7. Keep the rest of the default settings.


## Usage:

### a. Prerequisites:

1. Have an AWS account.

2. Set up an SQS queue as mentioned previously.

3. Create an identity that has access to consume messages from the queue.

4. The "consumer" identity must have the following permissions on the queue:

	sqs:ChangeMessageVisibility
	
	sqs:ChangeMessageVisibilityBatch
	
	sqs:DeleteMessage
	
	sqs:DeleteMessageBatch
	
	sqs:GetQueueAttributes
	
	sqs:GetQueueUrl
	
	sqs:ListQueues
	
	sqs:ReceiveMessage

5. Create a user and apply the below IAM Policy  to the user.

```
	{
      "Statement": [
        {
          "Action": [
            "sqs:ChangeMessageVisibility",
            "sqs:ChangeMessageVisibilityBatch",
            "sqs:DeleteMessage",
            "sqs:DeleteMessageBatch",
            "sqs:GetQueueAttributes",
            "sqs:GetQueueUrl",
            "sqs:ListQueues",
            "sqs:ReceiveMessage"
          ],
          "Effect": "Allow",
          "Resource": [
            "arn:aws:sqs:us-east-1:123456789012:Logstash"
          ]
        }
      ]
    }
```


### b. Parameters:
	
| Parameter | Input Type | Required | Default |
|-----------|------------|----------|---------|
| access_key_id | String  | No |  |
| secret_access_key | String  | No |  |
| polling_frequency | Number | No | 20 |
| queue | String | Yes |  |
| region | String | No |  |
| role_arn | string | No |  |




#### `access_key_id`
The `access_key_id` setting allows to set the access key ID for the user that has access to SQS. This plugin uses the AWS SDK and supports several ways to get credentials, which will be tried in this order:

	1. Static configuration, using access_key_id and secret_access_key params in logstash plugin config.
	
	2. External credentials file specified by an aws_credentials_file.

	3. Environment variables AWS_ACCESS_KEY_ID and AWS_SECRET_ACCESS_KEY.
	
	4. Environment variables AMAZON_ACCESS_KEY_ID and AMAZON_SECRET_ACCESS_KEY.

	5. IAM Instance Profile (available when running inside EC2).

#### `secret_access_key`
The `secret_access_key` setting defines the AWS Secret Access Key.

#### `polling_frequency`
The `polling_frequency` setting defines the frequency for the queue to be polled.

#### `queue`
The `queue` setting specifies the name of the SQS queue to pull messages from. Note that this is just the name of the queue, not the URL or ARN.

#### `region`
The `region` setting defines the region where the SQS is present.

#### `role_arn`
The role_arn setting allows you to specify which AWS IAM Role to assume, if any. This is used to generate temporary credentials, typically for cross-account access. To understand more about the settings to be followed while using this parameter, click [here]( ./SettingsForRoleArn.md )


#### Logstash Default configuration parameters
Other standard logstash parameters are available, such as:
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
1. Log in to the Guardium collector's API.
2. Issue these commands:
		• grdapi add_domain_to_universal_connector_allowed_domains domain=amazonaws.com
		• grdapi add_domain_to_universal_connector_allowed_domains domain=amazon.com

### Troubleshooting
#### To adjust the time on your Guardium machine based on your location, complete the following steps. 

1. Set the correct time on the Guardium machine by using the following CLI commands.
```
store syst ntp server ntp.rtp.raleigh.ibm.com
store syst ntp state on
store sync_timezone
```
2. Restart the Sniffer service by using the following command.

   ``` systemctl restart guard-snif ```
   
3. On the Guardium machine, go to **Setup > Tools and Views > Configure Universal Connector** page and restart the Universal Connector by disabling and then enabling it.
