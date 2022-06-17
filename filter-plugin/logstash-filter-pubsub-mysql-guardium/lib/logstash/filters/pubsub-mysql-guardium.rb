# Copyright 2020-2022 IBM Inc. All rights reserved
# SPDX-License-Identifier: Apache-2.0

# encoding: utf-8

require 'logstash/filters/base'
require 'logstash/namespace'
require 'digest/sha1'
require 'json'
require_relative 'filter_exception'
require_relative 'timestamp_formatter'
require_relative 'data_access_log_parser'

# This  filter will replace the contents of the default
# message field with whatever you specify in the configuration.
class LogStash::Filters::PubsubMysql < LogStash::Filters::Base

  config_name 'pubsub-mysql-guardium'
  config :cloudsqlproxy_enabled, :validate => :boolean, :default => false

  public

  def register
    # Add instance variables
    @logger.debug('Registering Google PubSub MySQL filter')
    @parser = DataAccessLogParser::DataAccessLogParserType.create(@cloudsqlproxy_enabled)
  end
  # def register

  def parse_err_log(event)
    begin
      message = event.get('textPayload').match(/(?<ts>(\d*-){2}(\d*)T(\d*:){2}(\d*.\d*)Z)(\s)(?<session_id>\d*)(\s)\[(?<severity_type>[A-Za-z]*)\](\s)\[(?<type_db>[A-Za-z]*-\d*)\](\s)\[(?<type_host>.*)\](\s)(?<msg>.*)/)

      session_id = message['session_id']
      event.set('[GuardRecord][sessionId]', session_id)

      login_failed_substr = 'Access denied for user'
      aborted_connection = 'Aborted connection'
      wait_timeout_exceeded = 'wait_timeout'
      msg = message['msg']

    rescue FilterException::FilterError
      raise FilterException::ErrorLogParserErr
    end

    exception_type = case msg
                     when /#{login_failed_substr}/
                       'LOGIN_FAILED'
                     when /#{aborted_connection}/
                       'PREMATURE_CLOSE'
                     when /#{wait_timeout_exceeded}/
                       'SESSION_ERROR'
                     else
                       'SQL_ERROR'
                     end

    svc_name = "cloudsql.googleapis.com"
    event.set('[GuardRecord][exception][exceptionTypeId]', exception_type)
    event.set('[GuardRecord][exception][description]', msg)
    event.set('[GuardRecord][exception][sqlString]', 'Undisclosed')
    event.set('[GuardRecord][data][originalSqlCommand]', nil)
    event.set('[GuardRecord][accessor][dbUser]', 'Undisclosed')
    event.set('[GuardRecord][sessionLocator][clientIp]', '0.0.0.0')
    event.set('[GuardRecord][accessor][serviceName]', svc_name)
    event.set('[GuardRecord][sessionLocator][serverIp]', '0.0.0.0')

  end
  # def parseErrLog

  public

  def filter(event)
    @logger.debug("Incoming Google Cloud Platform event: #{event.to_json}")
    begin
      log_name = event.get('logName').match(/.*%2F(?<log_type>.*)/)
      log_type = log_name['log_type']
      resource = event.get('resource')
      labels = resource['labels']
      database_id = labels['database_id']
      server_hostname = "#{labels["region"]}:#{database_id}"
      timestamp = event.get('timestamp')
      ts_epoch_u = TimestampFormatter.parse(timestamp)


      @logger.debug('Parsing by log type')
      case log_type
      when 'data_access'
        @parser.parse(event)
      when 'mysql.err'
        parse_err_log(event)
      else
        raise FilterException::UnsupportedLogType
      end

      matched = true

      event.set('[GuardRecord][time][timstamp]', ts_epoch_u)
      event.set('[GuardRecord][time][minOffsetFromGMT]', 0)
      event.set('[GuardRecord][time][minDst]', 0)


      event.set('[GuardRecord][data][construct]', nil)

      event.set('[GuardRecord][sessionLocator][clientPort]', nil)
      event.set('[GuardRecord][sessionLocator][clientIpv6]', nil)
      event.set('[GuardRecord][sessionLocator][serverIpv6]', nil)


      
      event.set('[GuardRecord][sessionLocator][clientIp]', '127.0.0.1')
      event.set('[GuardRecord][sessionLocator][serverPort]', 3306)
      event.set('[GuardRecord][sessionLocator][isIpv6]', false)

      event.set('[GuardRecord][accessor][serverType]', 'MySQL')
      event.set('[GuardRecord][accessor][serverOS]', '')
      event.set('[GuardRecord][accessor][clientOs]', '')
      event.set('[GuardRecord][accessor][clientHostName]', '')
      event.set('[GuardRecord][accessor][serverHostName]', server_hostname)
      event.set('[GuardRecord][accessor][commProtocol]', '')
      event.set('[GuardRecord][accessor][dbProtocol]', 'MYSQL')
      event.set('[GuardRecord][accessor][dbProtocolVersion]', '')
      event.set('[GuardRecord][accessor][osUser]', '')
      event.set('[GuardRecord][accessor][clientMac]', '')
      event.set('[GuardRecord][accessor][serverDescription]', '')
      event.set('[GuardRecord][accessor][language]', 'MYSQL')
      event.set('[GuardRecord][accessor][dataType]', 'TEXT')


      remove_redundant_fields(event)

      event.set('GuardRecord', event.get('GuardRecord').to_json)

    rescue StandardError => e
      @logger.error("#{e.class.name}: #{e.message}")
      event.cancel
    else
      @logger.debug("Sending record to Guardium output plugin: #{event.to_json}")
      filter_matched(event) if matched
    end
  end
  # def filter

  private

  def remove_redundant_fields(event)
    event.remove('insertId')
    event.remove('labels')
    event.remove('logName')
    event.remove('protoPayload')
    event.remove('receiveTimestamp')
    event.remove('resource')
    event.remove('severity')
    event.remove('timestamp')
    event.remove('host')
    event.remove('@timestamp')
    event.remove('@version')
    event.remove('textPayload')
  end
end
# class LogStash::Filters::PubsubMysql
