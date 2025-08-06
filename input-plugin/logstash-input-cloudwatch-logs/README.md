## Cloudwatch_logs input plug-in
### Meet Cloudwatch
* Tested versions: 1.0.3
* Developed by [Luke Waite](https://github.com/lukewaite)
* Configuration instructions can be found on every relevant filter plugin readme page. For example: [AWS PostgresSQL](../../filter-plugin/logstash-filter-azure-postgresql-guardium/README.md#procedure)
* Supported Guardium versions:
	* Guardium Data Protection: 11.3 and above
	* Guardium Data Security Center: 3.2

This is a [Logstash](https://github.com/elastic/logstash) input plug-in for the universal connector that is featured in IBM Security Guardium. It pulls events from the Amazon Web Services CloudWatch API. The events are then sent over to corresponding filter plugin which transforms these audit logs into a [Guardium record](https://github.com/IBM/universal-connectors/blob/main/common/src/main/java/com/ibm/guardium/universalconnector/commons/structures/Record.java)  instance (which is a standard structure made out of several parts). The information is then sent over to Guardium. Guardium records include the accessor (the person who tried to access the data), the session, data, and exceptions. If there are no errors, the data contains details about the query "construct". The construct details the main action (verb) and collections (objects) involved.


## Purpose:

Specify an individual log group or array of groups, and this plugin will scan all log streams in that group, and pull in any new log events.

Optionally, you may set the log_group_prefix parameter to true which will scan for all log groups matching the specified prefix(s) and ingest all logs available in all of the matching groups.

## Usage:

### Parameters:

| Parameter | Input Type | Required | Default |
|-----------|------------|----------|---------|
| log_group | string or Array of strings | Yes | |
| log_group_prefix | boolean | No | `false` |
| start_position | `beginning`, `end`, or an Integer | No | `beginning` |
| interval | number | No | 60 |
| aws_credentials_file | string | No | |
| access_key_id | string | No | |
| secret_access_key | string | No | |
| session_token | string | No | |
| region | string | No | `us-east-1` |
| codec | string | No | `plain` |
| role_arn | string | No |  |


#### `log_group`
The `log_group` setting allows specifying either a single or array of log groups, that are to be monitored for audit logs.

#### `log_group_prefix`
The `log_group_prefix` setting allows specifying a prefix that can be matched to all the log groups whose name contains the specified prefix string and logs are pulled over from all groups.

#### `start_position`
The `start_position` setting allows specifying where to begin processing of a newly encountered log group on plugin boot. Whether the group is 'new' is determined by whether or not the log group has a previously existing entry in the sincedb file.

Valid options for `start_position` are:
* `beginning` - Reads from the beginning of the group (default)
* `end` - Sets the sincedb to now, and reads any new messages going forward
* Integer - Number of seconds in the past to begin reading at

#### `interval`
The `interval` setting allows specifying after how many seconds the log groups should be polled for new logs.

#### `aws_credentials_file`
The `aws_credentials_file` setting allows specifying a aws credentials file, which contains the acces key and secret to connect to AWS cloudwatch instance

#### `access_key_id`
The `access_key_id` setting allows specifying the access key id of the IAM user having cloudwatch access to read the logs.

#### `secret_access_key`
The `secret_access_key` setting allows specifying the access secret key id of the IAM user having cloudwatch access to read the logs.

#### `session_token`
The `session_token` setting allows specifying a AWS Session token for temporary credential

#### `region`
The `region` setting allows specify the region in which the Cloudwatch log group exists.

#### `codec pattern`
The `codec pattern` setting allows specify, the codec used for input data. Input codecs are a convenient method for decoding the data before it enters the input, without needing a separate filter in the Logstash pipeline.
For the Redshift and Postgres plug-ins, update the value of the pattern parameter from the inputs section as specified in the codec pattern.
For Redshift, add pattern from [here] ( https://github.com/IBM/universal-connectors/blob/main/filter-plugin/logstash-filter-redshift-aws-guardium/redshift-over-cloudwatch.conf )
For Postgres, add pattern from [here] ( https://github.com/IBM/universal-connectors/blob/main/filter-plugin/logstash-filter-postgres-guardium/PostgresOverCloudWatchPackage/postgresCloudwatch.conf )

#### `role_arn`
The role_arn setting allows you to specify which AWS IAM Role to assume, if any. This is used to generate temporary credentials, typically for cross-account access. To understand more about the settings to be followed while using this parameter, click [here]( ./SettingsForRoleArn.md )


#### Logstash Default config params
Other standard logstash parameters are available such as:
* `add_field`
* `type`
* `tags`

### Example

	input {
    cloudwatch_logs {
		log_group => [ "/aws/rds/instance/test/postgresql"]
		start_position => "end"
		access_key_id => "<Enter the access key id>"
		secret_access_key => "<<Enter the secret access key id>>"
		region => "ap-south-1" #Default value: us-east-1
		interval => 1
		codec => multiline {
          pattern => ""
          negate => false
          what => "previous"
        }
		type => "test"
		add_field => {"account_id" => "<Enter the account id>"}
		add_field => {"abc" => "value"}
	}

### Authorizing outgoing traffic from AWS to Guardium

#### Procedure
1. Log in to the Guardium Collector's API.
2. Issue these commands:
```
	grdapi add_domain_to_universal_connector_allowed_domains domain=amazonaws.com
	grdapi add_domain_to_universal_connector_allowed_domains domain=amazon.com
```