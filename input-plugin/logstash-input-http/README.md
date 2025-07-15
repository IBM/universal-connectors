## http input plug-in
### Meet http
* Tested versions: 3.10.2
* Developed by Elastic
* Configuration instructions can be found on [HTTP](https://github.com/IBM/universal-connectors/blob/main/filter-plugin/logstash-filter-trino-guardium)
* Supported Guardium versions:
    * Guardium Data Protection: 11.3 and above

This is a [Logstash](https://github.com/elastic/logstash) input plug-in for the universal connector that is featured in IBM Security Guardium. It  enables Logstash to receive events from the http framework. The events are then sent over to corresponding filter plugin which transforms these audit logs into a [Guardium record](https://github.com/IBM/universal-connectors/blob/main/common/src/main/java/com/ibm/guardium/universalconnector/commons/structures/Record.java)  instance (which is a standard structure made out of several parts). The information is then sent over to Guardium. Guardium records include the accessor (the person who tried to access the data), the session, data, and exceptions. If there are no errors, the data contains details about the query "construct". The construct details the main action (verb) and collections (objects) involved.

## Purpose:

Specify a port, and this plugin will poll the same port on the Logstash host for any new log events.


## Usage:

### Parameters:

| Parameter | Input Type | Required | Default |
|-----------|------------|----------|---------|
| port  | number | Yes | |

#### `port`
The `port` setting allows specifying a port on which the Logstash host listens to and pull the log events written there.


#### Logstash Default config params
Other standard logstash parameters are available such as:
* `add_field`
* `type`
* `tags`

### Example

	input {
		http {
			port => 5060
            type => "http"
		}
	}
