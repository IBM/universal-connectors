# Logstash Filter PubSub MySQL Plugin

This is a Logstash filter plug-in for the universal connector that is featured in IBM Security Guardium. It parses a GCP (Google Cloud Platform) audit event into a Guardium record instance, which standardizes the event into several parts before it is sent over to Guardium.

### Note
This plug-in assumes the Logstash Google PubSub input plug-in is installed (version ~> 1.2.1, i.e. at least 1.2.1).

The plug-in is free and open-source (Apache 2.0). It can be used as a starting point to develop addition filter plug-ins for Guardium universal connector.

## Documentation

### Supported audit messages types
* Fmysql-general.log
* Fmysql.err

## Installation

You can install by editing the plugin's `Gemfile` and pointing the `:path` to your local plugin development directory or you can build the gem and install it using:

- Build your plugin gem
```sh
gem build logstash-filter-awesome.gemspec
```
- Install the plugin from the Logstash home
```sh
bin/logstash-plugin install /your/local/plugin/logstash-filter-awesome.gem
```
- Start Logstash and proceed to test the plugin

## Contributing

You can enhance this filter and open a pull request with suggested changes - or you can use the project to create a different filter plug-in for Guardium that supports other data sources.
