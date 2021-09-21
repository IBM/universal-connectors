# Logstash Plugin

[![Travis Build Status](https://travis-ci.com/logstash-plugins/logstash-input-google_pubsub.svg)](https://travis-ci.com/logstash-plugins/logstash-input-google_pubsub)

This is a [Logstash](https://github.com/elastic/logstash) input plugin for
[Google Pub/Sub](https://cloud.google.com/pubsub/). The plugin can subscribe
to a topic and ingest messages.

The main motivation behind the development of this plugin was to ingest
[Stackdriver Logging](https://cloud.google.com/logging/) messages via the
[Exported Logs](https://cloud.google.com/logging/docs/export/using_exported_logs)
feature of Stackdriver Logging.

It is fully free and fully open source. The license is Apache 2.0, meaning you
are pretty much free to use it however you want in whatever way.

## Documentation

### Prerequisites

You must first create a Google Cloud Platform project and enable the the
Google Pub/Sub API. If you intend to use the plugin ingest Stackdriver Logging
messages, you must also enable the Stackdriver Logging API and configure log
exporting to Pub/Sub. There is plentiful information on
https://cloud.google.com/ to get started,

- Google Cloud Platform Projects and [Overview](https://cloud.google.com/docs/overview/)
- Goolge Cloud Pub/Sub [documentation](https://cloud.google.com/pubsub/)
- Stackdriver Logging [documentation](https://cloud.google.com/logging/)

### Cloud Pub/Sub

Currently, this module requires you to create a `topic` manually and specify
it in the logstash config file. You must also specify a `subscription`, but
the plugin will attempt to create the pull-based `subscription` on its own.

All messages received from Pub/Sub will be converted to a logstash `event`
and added to the processing pipeline queue. All Pub/Sub messages will be
`acknowledged` and removed from the Pub/Sub `topic` (please see more about
[Pub/Sub concepts](https://cloud.google.com/pubsub/overview#concepts)).

It is generally assumed that incoming messages will be in JSON and added to
the logstash `event` as-is. However, if a plain text message is received, the
plugin will return the raw text in as `raw_message` in the logstash `event`.

#### Authentication

You have two options for authentication depending on where you run Logstash.

1. If you are running Logstash outside of Google Cloud Platform, then you will
need to create a Google Cloud Platform Service Account and specify the full
path to the JSON private key file in your config. You must assign sufficient
roles to the Service Account to create a subscription and to pull messages
from the subscription. Learn more about GCP Service Accounts and IAM roles
here:

  - Google Cloud Platform IAM [overview](https://cloud.google.com/iam/)
  - Creating Service Accounts [overview](https://cloud.google.com/iam/docs/creating-managing-service-accounts)
  - Granting Roles [overview](https://cloud.google.com/iam/docs/granting-roles-to-service-accounts)

1. If you are running Logstash on a Google Compute Engine instance, you may opt
to use Application Default Credentials. In this case, you will not need to
specify a JSON private key file in your config.

### Stackdriver Logging (optional)

If you intend to use the logstash plugin for Stackdriver Logging message
ingestion, you must first manually set up the Export option to Coud Pub/Sub and
the manually create the `topic`. Please see the more detailed instructions at,
[Exported Logs](https://cloud.google.com/logging/docs/export/using_exported_logs)
and ensure that the [necessary permissions](https://cloud.google.com/logging/docs/export/configure_export#manual-access-pubsub)
have also been manually configured.

Logging messages from Stackdriver Logging exported to Pub/Sub are received as
JSON and converted to a logstash `event` as-is in
[this format](https://cloud.google.com/logging/docs/export/using_exported_logs#log_entries_in_google_pubsub_topics).

### Sample Configuration

Below is a copy of the included `example.conf-tmpl` file that shows a basic
configuration for this plugin.

```
input {
    google_pubsub {
        # Your GCP project id (name)
        project_id => "my-project-1234"

        # The topic name below is currently hard-coded in the plugin. You
        # must first create this topic by hand and ensure you are exporting
        # logging to this pubsub topic.
        topic => "logstash-input-dev"

        # The subscription name is customizeable. The plugin will attempt to
        # create the subscription (but use the hard-coded topic name above).
        subscription => "logstash-sub"

        # If you are running logstash within GCE, it will use
        # Application Default Credentials and use GCE's metadata
        # service to fetch tokens.  However, if you are running logstash
        # outside of GCE, you will need to specify the service account's
        # JSON key file below.
        #json_key_file => "/home/erjohnso/pkey.json"
    }
}

output { stdout { codec => rubydebug } }
```

## (stock) Documentation

Logstash provides infrastructure to automatically generate documentation for this plugin. We use the asciidoc format to write documentation so any comments in the source code will be first converted into asciidoc and then into html. All plugin documentation are placed under one [central location](http://www.elastic.co/guide/en/logstash/current/).

- For formatting code or config example, you can use the asciidoc `[source,ruby]` directive
- For more asciidoc formatting tips, see the excellent reference here https://github.com/elastic/docs#asciidoc-guide

## Need Help?

Need help? Try #logstash on freenode IRC or the https://discuss.elastic.co/c/logstash discussion forum.

## Developing

### 1. Plugin Developement and Testing

#### Code
- To get started, you'll need JRuby with the Bundler gem installed.

- Create a new plugin or clone and existing from the GitHub [logstash-plugins](https://github.com/logstash-plugins) organization. We also provide [example plugins](https://github.com/logstash-plugins?query=example).

- Install dependencies
```sh
bundle install
rake install_jars
```

#### Test

- Update your dependencies

```sh
bundle install
```

- Run tests

```sh
bundle exec rspec
```

### 2. Running your unpublished Plugin in Logstash

#### 2.1 Run in a local Logstash clone

- Edit Logstash `Gemfile` and add the local plugin path, for example:
```ruby
gem "logstash-filter-awesome", :path => "/your/local/logstash-filter-awesome"
```
- Install plugin
```sh
# Logstash 2.3 and higher
bin/logstash-plugin install --no-verify

# Prior to Logstash 2.3
bin/plugin install --no-verify

```
- Run Logstash with your plugin
```sh
bin/logstash -e 'filter {awesome {}}'
```
At this point any modifications to the plugin code will be applied to this local Logstash setup. After modifying the plugin, simply rerun Logstash.

#### 2.2 Run in an installed Logstash

You can use the same **2.1** method to run your plugin in an installed Logstash by editing its `Gemfile` and pointing the `:path` to your local plugin development directory or you can build the gem and install it using:

- Build your plugin gem
```sh
gem build logstash-filter-awesome.gemspec
```
- Install the plugin from the Logstash home
```sh
# Logstash 2.3 and higher
bin/logstash-plugin install --no-verify

# Prior to Logstash 2.3
bin/plugin install --no-verify

```
- Start Logstash and proceed to test the plugin

## Contributing

All contributions are welcome: ideas, patches, documentation, bug reports, complaints, and even something you drew up on a napkin.

Programming is not a required skill. Whatever you've seen about open source and maintainers or community members  saying "send patches or die" - you will not see that here.

It is more important to the community that you are able to contribute.

For more information about contributing, see the [CONTRIBUTING](https://github.com/elastic/logstash/blob/master/CONTRIBUTING.md) file
