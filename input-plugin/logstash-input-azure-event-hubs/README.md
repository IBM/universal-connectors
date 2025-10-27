## azure_event_hubs input plug-in

### Meet Azure event hubs

* Tested versions: 1.4.3
* Developed by Elastic
* Configuration instructions can be found on every relevant filter plugin readme page. For
  example: [Azure PostgresSQL](../../filter-plugin/logstash-filter-azure-postgresql-guardium/README.md#procedure)
* Supported Guardium versions:
  * Guardium Data Protection: 11.4 and above

This is a [Logstash](https://github.com/elastic/logstash) input plug-in for the universal connector that is featured in
IBM Security Guardium. It pulls events from the Azure Event Hub. The events are then sent over to corresponding filter
plugin which transforms these audit logs into
a [Guardium record](https://github.com/IBM/universal-connectors/blob/main/common/src/main/java/com/ibm/guardium/universalconnector/commons/structures/Record.java)
instance (which is a standard structure made out of several parts). The information is then sent over to Guardium.
Guardium records include the accessor (the person who tried to access the data), the session, data, and exceptions. If
there are no errors, the data contains details about the query "construct". The construct details the main action (verb)
and collections (objects) involved.

## Purpose:

This plugin consumes events from Azure Event Hubs, a highly scalable data streaming platform and event ingestion
service. Event producers send events to the Azure Event Hub, and this plugin consumes those events for use with
Logstash.

## Usage:

### Parameters:

| Parameter | Input Type | Required | Default |
|-----------|------------|----------|---------|
| config_mode | String (basic or advanced) |  | Basic |
| event_hub_connections | Array | Yes, when config_mode => basic |  |
| initial_position | String, (beginning, end, or look_back) | No | `beginning` |
| threads | number | No | 16 |
| decorate_events | Boolean | No | |
| consumer_group | string | No | $Default |

#### `config_mode`

The `config_mode` setting allows specifying configuration to
either [Basic](https://www.elastic.co/guide/en/logstash/current/plugins-inputs-azure_event_hubs.html#plugins-inputs-azure_event_hubs-eh_basic_config)
configuration (default)
or [Advanced](https://www.elastic.co/guide/en/logstash/current/plugins-inputs-azure_event_hubs.html#plugins-inputs-azure_event_hubs-eh_advanced_config)
configuration.

#### `event_hub_connections`

The `event_hub_connections` setting allows specifying the list of connection strings that identifies the Event Hubs to
be read.

Each connection string must include the following mandatory components: Endpoint, SharedAccessKeyName, SharedAccessKey,
EntityPath (for the Event Hub)

The event_hub_connections option is defined per Event Hub. All other configuration options are shared among Event Hubs.

#### `initial_position`

The `initial_position` setting allows specifying when first reading from an Event Hub, start from this position:

Valid options for `start_position` are:

* `beginning` - reads all pre-existing events in the Event Hub (default)
* `end` - does not read any pre-existing events in the Event Hub
* look_back reads end minus a number of seconds worth of pre-existing events. You control the number of seconds using
  the initial_position_look_back option.

#### `threads`

The `threads` setting allows setting total number of threads used to process events. The value you set here applies to
all Event Hubs. Even with advanced configuration, this value is a global setting, and can’t be set per event hub.

#### `decorate_events`

The `decorate_events` setting allows adding metadata about the Event Hub, including Event Hub name, consumer_group,
processor_host, partition, offset, sequence, timestamp, and event_size.

#### `consumer_group`

The `consumer_group` setting allows specifying the Consumer group used to read the Event Hub(s). Create a consumer group
specifically for Logstash. Then ensure that all instances of Logstash use that consumer group so that they can work
together properly.

#### Logstash Default config params

Other standard logstash parameters are available such as:

* `add_field`
* `type`
* `tags`

### Example

	input {
		azure_event_hubs 
			{
				config_mode => "basic"
				event_hub_connections => ["Endpoint=<Endpoint>;SharedAccessKeyName=<SharedAccessKeyName>;SharedAccessKey=<SharedAccessKey>;EntityPath=<EntityPath>"]
				initial_position => "end"
				threads => 8
				decorate_events => true
				consumer_group => "$Default" 
				type => "azure_event_hub"
				add_field => {"enrollmentId" => <enrollmentId>}	
			}
	}

