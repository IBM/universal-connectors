# encoding: utf-8

# Author: Eric Johnson <erjohnso@google.com>
# Date: 2016-06-01
#
# Copyright 2016 Google Inc.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
require "logstash/inputs/base"
require "logstash/namespace"

require 'java'
require 'logstash-input-google_pubsub_jars.rb'

# This is a https://github.com/elastic/logstash[Logstash] input plugin for 
# https://cloud.google.com/pubsub/[Google Pub/Sub]. The plugin can subscribe 
# to a topic and ingest messages.
#
# The main motivation behind the development of this plugin was to ingest 
# https://cloud.google.com/logging/[Stackdriver Logging] messages via the 
# https://cloud.google.com/logging/docs/export/using_exported_logs[Exported Logs] 
# feature of Stackdriver Logging.
#
# ==== Prerequisites
#
# You must first create a Google Cloud Platform project and enable the the 
# Google Pub/Sub API. If you intend to use the plugin ingest Stackdriver Logging 
# messages, you must also enable the Stackdriver Logging API and configure log 
# exporting to Pub/Sub. There is plentiful information on 
# https://cloud.google.com/ to get started: 
#
# - Google Cloud Platform Projects and https://cloud.google.com/docs/overview/[Overview]
# - Google Cloud Pub/Sub https://cloud.google.com/pubsub/[documentation]
# - Stackdriver Logging https://cloud.google.com/logging/[documentation]
#
# ==== Cloud Pub/Sub
#
# Currently, this module requires you to create a `topic` manually and specify 
# it in the logstash config file. You must also specify a `subscription`, but 
# the plugin will attempt to create the pull-based `subscription` on its own. 
#
# All messages received from Pub/Sub will be converted to a logstash `event` 
# and added to the processing pipeline queue. All Pub/Sub messages will be 
# `acknowledged` and removed from the Pub/Sub `topic` (please see more about 
# https://cloud.google.com/pubsub/overview#concepts)[Pub/Sub concepts]. 
#
# It is generally assumed that incoming messages will be in JSON and added to 
# the logstash `event` as-is. However, if a plain text message is received, the 
# plugin will return the raw text in as `raw_message` in the logstash `event`. 
#
# ==== Authentication
#
# You have two options for authentication depending on where you run Logstash. 
#
# 1. If you are running Logstash outside of Google Cloud Platform, then you will 
# need to create a Google Cloud Platform Service Account and specify the full 
# path to the JSON private key file in your config. You must assign sufficient 
# roles to the Service Account to create a subscription and to pull messages 
# from the subscription. Learn more about GCP Service Accounts and IAM roles 
# here:
#
#   - Google Cloud Platform IAM https://cloud.google.com/iam/[overview]
#   - Creating Service Accounts https://cloud.google.com/iam/docs/creating-managing-service-accounts[overview]
#   - Granting Roles https://cloud.google.com/iam/docs/granting-roles-to-service-accounts[overview]
#
# 1. If you are running Logstash on a Google Compute Engine instance, you may opt 
# to use Application Default Credentials. In this case, you will not need to 
# specify a JSON private key file in your config.
#
# ==== Stackdriver Logging (optional)
#
# If you intend to use the logstash plugin for Stackdriver Logging message 
# ingestion, you must first manually set up the Export option to Cloud Pub/Sub and 
# the manually create the `topic`. Please see the more detailed instructions at, 
# https://cloud.google.com/logging/docs/export/using_exported_logs [Exported Logs] 
# and ensure that the https://cloud.google.com/logging/docs/export/configure_export#manual-access-pubsub[necessary permissions] 
# have also been manually configured.
#
# Logging messages from Stackdriver Logging exported to Pub/Sub are received as 
# JSON and converted to a logstash `event` as-is in 
# https://cloud.google.com/logging/docs/export/using_exported_logs#log_entries_in_google_pubsub_topics[this format].
#
# ==== Sample Configuration
#
# Below is a copy of the included `example.conf-tmpl` file that shows a basic 
# configuration for this plugin.
#
# [source,ruby]
# ----------------------------------
# input {
#     google_pubsub {
#         # Your GCP project id (name)
#         project_id => "my-project-1234"
#
#         # The topic name below is currently hard-coded in the plugin. You
#         # must first create this topic by hand and ensure you are exporting
#         # logging to this pubsub topic.
#         topic => "logstash-input-dev"
#
#         # The subscription name is customizeable. The plugin will attempt to
#         # create the subscription (but use the hard-coded topic name above).
#         subscription => "logstash-sub"
#
#         # If you are running logstash within GCE, it will use
#         # Application Default Credentials and use GCE's metadata
#         # service to fetch tokens.  However, if you are running logstash
#         # outside of GCE, you will need to specify the service account's
#         # JSON key file below.
#         #json_key_file_content => "TODO"
#     }
# }
# output { stdout { codec => rubydebug } }
# ----------------------------------
#
# ==== Metadata and Attributes
#
# The original Pub/Sub message is preserved in the special Logstash
# `[@metadata][pubsub_message]` field so you can fetch:
#
# * Message attributes
# * The origiginal base64 data
# * Pub/Sub message ID for de-duplication
# * Publish time
#
# You MUST extract any fields you want in a filter prior to the data being sent
# to an output because Logstash deletes `@metadata` fields otherwise.
#
# See the PubsubMessage
# https://cloud.google.com/pubsub/docs/reference/rest/v1/PubsubMessage[documentation]
# for a full description of the fields.
#
# Example to get the message ID:
#
# [source,ruby]
# ----------------------------------
# input {google_pubsub {...}}
#
# filter {
#   mutate {
#     add_field => { "messageId" => "%{[@metadata][pubsub_message][messageId]}" }
#   }
# }
#
# output {...}
# ----------------------------------
#

class LogStash::Inputs::GooglePubSub < LogStash::Inputs::Base
  class MessageReceiver
    include com.google.cloud.pubsub.v1.MessageReceiver

    def initialize(&blk)
      @block = blk
    end

    def receiveMessage(message, consumer)
      @block.call(message)
      consumer.ack()
    end
  end

  java_import 'com.google.api.core.ApiService$Listener'
  class SubscriberListener < Listener
    def initialize(&blk)
      @block = blk
    end

    def failed(from, failure)
      @block.call(from, failure)
    end
  end

  include_package 'com.google.api.gax.batching'
  include_package 'com.google.api.gax.core'
  include_package 'com.google.auth.oauth2'
  include_package 'com.google.common.util.concurrent'
  include_package 'com.google.cloud.pubsub.v1'
  include_package 'com.google.pubsub.v1'
  include_package 'com.google.protobuf.util'
  config_name "google_pubsub"

  # Google Cloud Project ID (name, not number)
  config :project_id, :validate => :string, :required => true

  # Google Cloud Pub/Sub Topic and Subscription.
  # Note that the topic must be created manually with Cloud Logging
  # pre-configured export to PubSub configured to use the defined topic.
  # The subscription will be created automatically by the plugin.
  config :topic, :validate => :string, :required => true
  config :subscription, :validate => :string, :required => true
  config :max_messages, :validate => :number, :required => true, :default => 5

  # If logstash is running within Google Compute Engine, the plugin will use
  # GCE's Application Default Credentials. Outside of GCE, you will need to
  # specify a Service Account JSON key file.
  config :json_key_file_content, :validate => :string, :required => true

  # If set true, will include the full message data in the `[@metadata][pubsub_message]` field.
  config :include_metadata, :validate => :boolean, :required => false, :default => false

  # If true, the plugin will try to create the subscription before publishing.
  # Note: this requires additional permissions to be granted to the client and is _not_
  # recommended for most use-cases.
  config :create_subscription, :validate => :boolean, :required => false, :default => false

  # If undefined, Logstash will complain, even if codec is unused.
  default :codec, "plain"

  public
  def register
    @logger.debug("Registering Google PubSub Input: project_id=#{@project_id}, topic=#{@topic}, subscription=#{@subscription}")
    @subscription_id = "projects/#{@project_id}/subscriptions/#{@subscription}"

    if @json_key_file_content
      @credentialsProvider = FixedCredentialsProvider.create(
        ServiceAccountCredentials.fromStream(java.io.ByteArrayInputStream.new((@json_key_file_content).to_java_bytes))
      )
    end
    @topic_name = ProjectTopicName.of(@project_id, @topic)
    @subscription_name = ProjectSubscriptionName.of(@project_id, @subscription)
  end

  def stop
    @subscriber.stopAsync().awaitTerminated() if @subscriber != nil
  end

  def run(queue)
    # Attempt to create the subscription
    if @create_subscription
      @logger.debug("Creating subscription #{@subscription_id}")
      subscriptionAdminClient = SubscriptionAdminClient.create
      begin
        subscriptionAdminClient.createSubscription(@subscription_name, @topic_name, PushConfig.getDefaultInstance(), 0)
      rescue
        @logger.info("Subscription already exists")
      end
    end

    @logger.debug("Pulling messages from sub '#{@subscription_id}'")
    handler = MessageReceiver.new do |message|
      # handle incoming message, then ack/nack the received message
      data = message.getData().toStringUtf8()
      @codec.decode(data) do |event|
        event.set("host", event.get("host") || @host)
        event.set("[@metadata][pubsub_message]", extract_metadata(message)) if @include_metadata
        decorate(event)
        queue << event
      end
    end
    listener = SubscriberListener.new do |from, failure|
      @logger.error("#{failure}")
      raise failure
    end
    flowControlSettings = FlowControlSettings.newBuilder().setMaxOutstandingElementCount(@max_messages).build()
    executorProvider = InstantiatingExecutorProvider.newBuilder().setExecutorThreadCount(1).build()
    subscriberBuilder = Subscriber.newBuilder(@subscription_name, handler)
      .setFlowControlSettings(flowControlSettings)
      .setExecutorProvider(executorProvider)
      .setParallelPullCount(1)

    if @credentialsProvider
      subscriberBuilder.setCredentialsProvider(@credentialsProvider)
    end
    @subscriber = subscriberBuilder.build()
    @subscriber.addListener(listener, MoreExecutors.directExecutor())
    @subscriber.startAsync()
    @subscriber.awaitTerminated()
  end

  def extract_metadata(java_message)
    {
      data: java_message.getData().toStringUtf8(),
      attributes: java_message.getAttributesMap(),
      messageId: java_message.getMessageId(),
      publishTime: Timestamps.toString(java_message.getPublishTime())
    }
  end
end
