## 2.0.0
- Substitute the file path within the key file content with the JSON key and designate it as a mandatory field.

## 1.3.0
 - Updated plugins dependencies [#62](https://github.com/logstash-plugins/logstash-input-google_pubsub/pull/62)

## 1.2.2
 - Updated dependencies forcing this plugin to be compatible only with Logstash >= 8.2.0 [#58](https://github.com/logstash-plugins/logstash-input-google_pubsub/pull/58)

## 1.2.1
 - Fixed dependency conflicts with logstash-output-google_pubsub by using the same client library [logstash-output-google_pubsub#7](https://github.com/logstash-plugins/logstash-output-google_pubsub/issues/7)

## 1.2.0
 - Change to Java client
 - Add `create_subscription` setting. Fixes [#9](https://github.com/logstash-plugins/logstash-input-google_pubsub/issues/9)

## 1.1.0
  - Add additional attributes in the `[@metadata][pubsub_message]` field. Fixes [#7](https://github.com/logstash-plugins/logstash-input-google_pubsub/issues/7)

## 1.0.6
  - Ignore acknowledge requests with an empty array of IDs. Fixes [#14](https://github.com/logstash-plugins/logstash-input-google_pubsub/issues/14)

## 1.0.5
  - Docs: Set the default_codec doc attribute.

## 1.0.4
  - Update gemspec summary

## 1.0.3
  - Fix some documentation issues

## 1.0.2
 - Docs: Fix list numbering

## 1.0.1
 - Docs: Bump patch level for doc build

## 1.0.0
 - Update to use 5.0 API

## 0.9.0
 - Initial release
