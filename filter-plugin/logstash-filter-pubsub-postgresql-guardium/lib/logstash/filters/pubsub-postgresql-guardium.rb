# Copyright 2020-2022 IBM Inc. All rights reserved
# SPDX-License-Identifier: Apache-2.0

# encoding: utf-8
require 'logstash/filters/base'
require 'logstash/namespace'
require 'json'
require_relative 'timestamp_formatter'

# This  filter will replace the contents of the default
# message field with whatever you specify in the configuration.
#
# It is only intended to be used as an .
class LogStash::Filters::PubsubPostgresqlGuardium < LogStash::Filters::Base

  config_name 'pubsub-postgresql-guardium'


  public
  def register
    # Add instance variables
    @logger.debug('Registering Google PubSub PostgreSQL filter')
  end # def register

  # auxilary functions for parsing
  def parsePostgresLog(event)
    begin

      a = event.get('textPayload')
      @logger.debug("Start parsing postgres log. payload: #{a}")
      message = event.get('textPayload').match(/(?<ts>(\d*-){2}(\d*)\s(\d*:){2}(\d*.\d*))(\s)UTC(\s)\[(?<session_id>\d*)\].*db=(?<db_name>\S*),user=(?<uname>\S*)\s(?<severity>[A-Z]*):(?<msg>.*)/)

      msg = message['msg']
      severity = message['severity']
      db_name = message['db_name'].to_s.empty? ? "N.A." : message['db_name']
      session_id = message['session_id'].to_s.empty? ? "" : message['session_id']
      uname = message['uname'].to_s.empty? ? "N.A." : message['uname']
      timestamp = message['ts']

      event.set('[GuardRecord][dbName]', db_name)
      event.set('[GuardRecord][accessor][serviceName]', db_name)
      event.set('[GuardRecord][sessionId]', session_id)
      event.set('[GuardRecord][accessor][dbUser]', uname)

      login_failed_substr = 'password authentication failed'
      aborted_connection = 'Aborted connection'
      wait_timeout_exceeded = 'The wait_timeout'

      if %w[EMERGENCY ALERT CRITICAL ERROR FATAL].include?(severity)
        exception_type = case msg
                         when /#{login_failed_substr}.*/
                           'LOGIN_FAILED'
                         when /#{aborted_connection}.*/
                           'PREMATURE_CLOSE'
                         when /#{wait_timeout_exceeded}.*/
                           'SESSION_ERROR'
                         else
                           'SQL_ERROR'
                         end
        event.set('[GuardRecord][exception][exceptionTypeId]', exception_type)
        event.set('[GuardRecord][exception][description]', msg)
        event.set('[GuardRecord][exception][sqlString]', 'N.A.')
        # GRD-71899: data or exception must be null
        event.set('[GuardRecord][data]', nil)

      else
        event.set('[GuardRecord][exception]', nil)
        event.set('[GuardRecord][data][originalSqlCommand]', msg)
        event.set('[GuardRecord][data][construct]', nil)
      end

      ts_epoch = TimestampFormatter.parse(timestamp)
      event.set('[GuardRecord][time][timstamp]', ts_epoch)

      event.remove('textPayload')


    rescue Exception => e
      @logger.error('in parsePostgresLog: ', e)
      raise Exception, 'An error occurred while trying to parse postgres log'
    end
  end # def parsePostgresLog

  def parsePgAudit(event)
    begin


       a = event.get('protoPayload')
       @logger.debug("Start parsing pg audit log. payload: #{a}")
       protoPayload = event.get('protoPayload')

      request = protoPayload['request']
      original_sql = request['statement']
      session_id = request['databaseSessionId'].to_s.empty? ? "" : request['databaseSessionId']
      db_name = request['database'].to_s.empty? ? "N.A." : request['database']
      uname = request['user'].to_s.empty? ? "N.A." : request['user']
      timestamp = event.get('timestamp')
      #timestamp = parse["timestamp"]

      event.set('[GuardRecord][data][originalSqlCommand]', original_sql)
      event.set('[GuardRecord][data][construct]', nil)
      event.set('[GuardRecord][dbName]', db_name)
      event.set('[GuardRecord][accessor][serviceName]', db_name)
      event.set('[GuardRecord][sessionId]', session_id)
      event.set('[GuardRecord][accessor][dbUser]', uname)
      event.set('[GuardRecord][exception]', nil)
      ts_epoch = TimestampFormatter.parse(timestamp)
      event.set('[GuardRecord][time][timstamp]', ts_epoch)

      event.remove('protoPayload')

    rescue Exception => e
      @logger.error('in parsePgAudit: ', e.message)
      raise Exception, 'An error occurred while trying to parse PgAudit log'
    end
  end # def parsePgAudit

  public
  def filter(event)
    matched = false

    begin

    pmessage = {
      "resource" => event.get("resource"),
      "logName" => event.get("logName"),
      "severity" => event.get("severity"),
      "host" => event.get("host")
     }

      @logger.debug("Start processing new event: #{pmessage}")

      @logger.debug(">>> message class: #{pmessage.class}")

      @logger.debug(" Debug Message :  #{pmessage}")

      log_name_for_debug = pmessage['logName']
      @logger.debug("log name: #{log_name_for_debug}")
      log_name = pmessage['logName'].match(/.*%2F(?<log_type>.*)/)
      log_type = log_name['log_type']
      @logger.debug("log type: #{log_type}")
      resource = pmessage['resource']
      labels = resource['labels']
      database_id = labels['database_id']
      server_hostname = "#{labels["region"]}:#{database_id}"
      severity = pmessage['severity']
      client_hostname = pmessage['host']


      @logger.debug('Parsing by log type')
      case log_type
      when 'postgres.log'
        parsePostgresLog(event)
      when 'data_access'
        parsePgAudit(event)
      else
        raise Exception, "Log type doesn't match any of the valid logNames"
      end

      matched = true

      @logger.debug('Setting GuardRecord')

      event.set('[GuardRecord][time][minOffsetFromGMT]', 0)
      event.set('[GuardRecord][time][minDst]', 0)

      event.set('[GuardRecord][sessionLocator][clientIp]', '0.0.0.0')
      event.set('[GuardRecord][sessionLocator][clientPort]', -1)
      event.set('[GuardRecord][sessionLocator][clientIpv6]', nil)
      event.set('[GuardRecord][sessionLocator][serverIpv6]', nil)


      event.set('[GuardRecord][appUserName]', 'cloudSQL_service')


      event.set('[GuardRecord][sessionLocator][serverIp]', '0.0.0.0')
      event.set('[GuardRecord][sessionLocator][serverPort]', -1)
      event.set('[GuardRecord][sessionLocator][isIpv6]', false)

      event.set('[GuardRecord][accessor][serverType]', 'POSTGRESQL')
      event.set('[GuardRecord][accessor][serverOS]', '')
      event.set('[GuardRecord][accessor][clientOs]', '')
      event.set('[GuardRecord][accessor][clientHostName]', client_hostname)
      event.set('[GuardRecord][accessor][serverHostName]', server_hostname)
      event.set('[GuardRecord][accessor][commProtocol]', '')
      event.set('[GuardRecord][accessor][dbProtocol]', 'POSTGRESQL')
      event.set('[GuardRecord][accessor][dbProtocolVersion]', '')
      event.set('[GuardRecord][accessor][osUser]', '')
      event.set('[GuardRecord][accessor][sourceProgram]', '')
      event.set('[GuardRecord][accessor][clientMac]', '')
      event.set('[GuardRecord][accessor][serverDescription]', '')
      event.set('[GuardRecord][accessor][language]', 'PGRS')
      event.set('[GuardRecord][accessor][dataType]', 'TEXT')

      event.remove('@timestamp')
      event.remove('host')
      event.remove('receiveTimestamp')
      event.remove('timestamp')
      event.remove('@version')
      event.remove('logName')
      event.remove('resource')
      event.remove('insertId')
      event.remove('severity')
      event.remove('labels')
      event.remove('pmessage')


      event.set('GuardRecord', event.get('GuardRecord').to_json)

    rescue Exception => e
      event.cancel
    else
      @logger.debug('Sending record to Guardium output plugin')
      @logger.debug(event.to_json)
      filter_matched(event) if matched
    end
  end # def filter
end # class LogStash::Filters::PubsubPostgresqlGuardium