## S3 input plug-in

This is a [Logstash](https://github.com/elastic/logstash) input plug-in for the universal connector that is featured in IBM Security Guardium. It pulls events from the Amazon Web Services CloudWatch API. The events are then sent over to corresponding filter plugin which transforms these audit logs into a [Guardium record](https://github.com/IBM/universal-connectors/blob/main/common/src/main/java/com/ibm/guardium/univer***REMOVED***lconnector/commons/structures/Record.java)  instance (which is a standard structure made out of several parts). The information is then sent over to Guardium. Guardium records include the accessor (the person who tried to access the data), the session, data, and exceptions. If there are no errors, the data contains details about the query "construct". The construct details the main action (verb) and collections (objects) involved.

## Purpose:

Stream events from files from a S3 bucket.Each line from each file generates an event. Files ending in .gz are handled as gzip’ed files.
Files that are archived to AWS Glacier will be skipped

**Note** :The S3 input plugin only supports AWS S3. Other S3 compatible storage solutions are not supported.

### Parameters:

| Parameter         | Input Type | Required | Default     |
|-------------------|------------|----------|-------------|
| bucket            | String     | Yes      | NA          |
| access_key_id     | String     | NO       | NA          |
| secret_access_key | String     | NO       | NA          |
| region            | String     | NO       | `us-east-1` |
| Prefix            | String     | NO       | NA          |
| Codec             | String     | No       | `plain`     |
| role_arn          | String     | No       |  |

#### `Bucket`
The `Bucket` is the name of the S3 bucket.

#### `access_key_id`
This plugin uses the AWS SDK and supports several ways to get credentials, one of the way is Static configuration, using `access_key_id` in logstash plugin config.

#### `secret_access_key`
This plugin uses the AWS SDK and supports several ways to get credentials, one of the way is Static configuration, using `secret_access_key` in logstash plugin config.

#### `region`
The `region` setting allows to specify the region in which the Cloudwatch log group exists.

#### `Prefix`
If specified, the `prefix` of filenames in the bucket must match (not a regexp).

#### `codec`
The `codec` setting allows specify, the codec used for input data. Input codecs are a convenient method for decoding the data before it enters the input, without needing a separate filter in the Logstash pipeline.

#### `role_arn`
The role_arn setting allows you to specify which AWS IAM Role to assume, if any. This is used to generate temporary credentials, typically for cross-account access. To understand more about the settings to be followed while using this parameter, click [here]( ./SettingsForRoleArn.md )

#### Logstash Default config params
Other standard logstash parameters are available such as:
* `add_field`
* `type`
* `tags`

### Example

	input {
    s3 {
        bucket =>"<Enter bucket name>"
        access_key_id => "<Enter the access key id>"
		secret_access_key => "<<Enter the secret access key id>>"
		region => "ap-south-1" #Default value: us-east-1
        prefix =>"<Enter bucket prefix>"
		codec => multiline {
          pattern => ""
          negate => false
          what => "previous"
        }
		type => "test"
		add_field => {"AccountID" => "<AccountID>"}
	}
