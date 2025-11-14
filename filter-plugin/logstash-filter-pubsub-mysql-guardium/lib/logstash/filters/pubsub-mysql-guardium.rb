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

      errMsg = event.get('message')
      parse = JSON.parse(errMsg)
      tax_payload = parse["textPayload"]
       pattern = /(?<ts>(\d*-){2}(\d*)T(\d*:){2}(\d*.\d*)Z)(\s)(?<session_id>\d*)(\s)\[(?<severity_type>[A-Za-z]*)\](\s)\[(?<type_db>[A-Za-z]*-\d*)\](\s)\[(?<type_host>.*)\](\s)(?<msg>.*)/

         match_data = tax_payload.match(pattern)

         # default value for session_id and message if match data = nil
         session_id = "" 
         message = tax_payload

         if match_data
           timestamp = match_data["ts"]
           session_id = match_data["session_id"].to_s.empty? ? "" : match_data['session_id']
           severity_type = match_data["severity_type"]
           type_db = match_data["type_db"]
           type_host = match_data["type_host"]
           message = match_data["msg"]
         else
           puts "No match found."
         end 

      event.set('[GuardRecord][sessionId]', session_id)

      login_failed = 'Access denied for user'
      aborted_connection = 'Aborted connection'
      wait_timeout_exceeded = 'wait_timeout'
      reading_communication = 'Got an error reading communication'
      packet_out_of_order = 'Got packets out of order'

    rescue FilterException::FilterError
      raise FilterException::ErrorLogParserErr
    end

      exception_type = case message
                        when /#{Regexp.escape(login_failed)}/i
                          'LOGIN_FAILED'
                        when /#{Regexp.escape(aborted_connection)}|#{Regexp.escape(reading_communication)}/i
                          'PREMATURE_CLOSE'
                        when /#{Regexp.escape(wait_timeout_exceeded)}/i
                          'SESSION_ERROR'
                        when /#{Regexp.escape(packet_out_of_order)}/i
                           'PACKET_OUT_OF_ORDER'
                        else
                           'SQL_ERROR'
                        end

    svc_name = "cloudsql.googleapis.com"
    event.set('[GuardRecord][exception][exceptionTypeId]', exception_type)
    event.set('[GuardRecord][exception][description]', tax_payload)
    event.set('[GuardRecord][exception][sqlString]', 'Undisclosed')
    event.set('[GuardRecord][data][originalSqlCommand]', '')
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
        message1 = event.get('message')
        parse = JSON.parse(message1)

        log_name = parse["logName"]
        logtype = log_name.match(/.*%2F(?<log_type>.*)/)
        log_type = ''
        if logtype
           log_type = logtype["log_type"]
        else
            puts "No match found."
        end

        resource = parse["resource"]
        labels = resource['labels']
        database_id = labels['database_id']
        server_hostname = "#{labels["region"]}:#{database_id}"
        timestamp = parse["timestamp"]
        ts_epoch_u = TimestampFormatter.parse(timestamp)

        @logger.debug("Parsing by log type: #{log_type}")
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

        event.set('[GuardRecord][sessionLocator][clientPort]', -1)
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
      @logger.error("Error: #{e.message}")
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
