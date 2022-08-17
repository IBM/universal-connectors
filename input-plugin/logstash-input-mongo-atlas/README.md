# Logstash Java Plugin

[![Travis Build Status](https://travis-ci.com/logstash-plugins/logstash-filter-java_filter_example.svg)](https://travis-ci.com/logstash-plugins/logstash-filter-java_filter_example)

This is a Java plugin for [Logstash](https://github.com/elastic/logstash).

It is fully free and fully open source. The license is Apache 2.0, meaning you are free to use it however you want.

The documentation for Logstash Java plugins is available [here](https://www.elastic.co/guide/en/logstash/6.7/contributing-java-plugin.html).

## Usage

### Parameters
| Parameter | Input Type | Required | Default |
|-----------|------------|----------|---------|
| interval | number | Yes | | 300
| public-key | string | No | |
| private-key | string | No | |
| group-id | string | No | |
| hostname | `beginning`, `end`, or an Integer | No | `beginning` |
| filename | string | No | `mongodb-audit-log.gz` |

### Example

    input {
        mongo_atlas_input{
            interval => 300
            public-key => "utwklfnh"
            private-key => "e2f542e2-c065-76bc-acdc-62d105b5da88"
            group-id => "61f8b9021d9dcc63i8fbfcf1"
            hostname => "cluster0-shard-00-02.i2kko8.mongodb.net"
            type => "mongodbatlas"
        }
    }
