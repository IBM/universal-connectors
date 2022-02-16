# Copyright 2020-2021 IBM Inc. All rights reserved
# SPDX-License-Identifier: Apache-2.0

# encoding: utf-8
require "logstash/filters/base"
require "logstash/namespace"
require "json"
require "time"

# This  filter will replace the contents of the default
# mes***REMOVED***ge field with whatever you specify in the configuration.
#
# It is only intended to be used as an .
class LogStash::Filters::PubsubMysql < LogStash::Filters::Base

  # Setting the config_name here is required. This is how you
  # configure this filter from your Logstash config.
  #
  # filter {
  #    {
  #     mes***REMOVED***ge => "My mes***REMOVED***ge..."
  #   }
  # }
  #
  config_name "pubsub-mysql-guardium"

  # Replace the mes***REMOVED***ge with this value.
  # config :mes***REMOVED***ge, :validate => :string, :default => "Hello World!"
  # The target field to place all the data
  config :target, :validate => :string, :default => "GuardRecord"


  public
  def register
    # Add instance variables
    @logger.debug("Registering Google PubSub MySQL filter")
  end # def register

  # auxilary functions for parsing
  def parseGeneralLog(event)
    begin
      mes***REMOVED***ge_no_tabs = event.get("textPayload").gsub /\t/, ' '
      mes***REMOVED***ge = mes***REMOVED***ge_no_tabs.match(/(?<ts>(\d*-){2}(\d*)T(\d*:){2}(\d*.\d*)Z)([\***REMOVED***-zA-Z]*)\[(?<db_user>[A-Za-z]*)\].*@.*\[(?<client_ip>(\d)*.(\d)*.(\d)*.(\d)*)]\s*(?<session_id>\d*)\s(?<connection_id>\d*)\s*Query(\s)(?<original_sql>.*)/)
      sessionId = mes***REMOVED***ge["session_id"]
      original_sql = mes***REMOVED***ge["original_sql"]
      clientIp = mes***REMOVED***ge["client_ip"]
      dbUser = mes***REMOVED***ge["db_user"]
      timestamp = mes***REMOVED***ge["ts"]
      event.set("[GuardRecord][exception]", nil)
      event.set('[GuardRecord][data][originalSqlCommand]', original_sql)
      event.set("[GuardRecord][sessionId]", sessionId)
      event.set("[GuardRecord][sessionLocator][clientIp]", clientIp)
      event.set("[GuardRecord][accessor][dbUser]", dbUser)
      ts_epoch = Time.parse(timestamp).to_i
      ts_epoch_u = ts_epoch * (10**3)
      event.set('[GuardRecord][time][timstamp]', ts_epoch_u)
    rescue Exception => e
      @logger.error("in parseGeneralLog: ", e.mes***REMOVED***ge)
      raise Exception.new "An error occured while trying to parse general log"
    end
  end # def parseGeneralLog

  def parseErrLog(event)
    begin
      mes***REMOVED***ge = event.get("textPayload").match(/(?<ts>(\d*-){2}(\d*)T(\d*:){2}(\d*.\d*)Z)(\s)(?<session_id>\d*)(\s)\[(?<severity_type>[A-Za-z]*)](\s)\[(?<type_db>[A-Za-z]*-\d*)](\s)\[(?<type_host>.*)](\s)(?<msg>.*)/)
      timestamp = mes***REMOVED***ge["ts"]
      if mes***REMOVED***ge == nil || mes***REMOVED***ge == ""
        mes***REMOVED***ge = event.match(/(?<msg>.*)/)
        timestamp = event.get("timestamp")
        sessionId = 0
      else
        sessionId = mes***REMOVED***ge["session_id"]
        event.set("[GuardRecord][sessionId]", sessionId)
      end
      login_failed_substr = "Access denied for user"
      aborted_connection = "Aborted connection"
      wait_timeout_exceeded = "wait_timeout"
      msg = mes***REMOVED***ge["msg"]

        exceptionType =  case msg
          when /#{login_failed_substr}.*/
            "LOGIN_FAILED"
          when /#{aborted_connection}.*/
            "PREMATURE_CLOSE"
          when /#{wait_timeout_exceeded}.*/
            "PREMATURE_CLOSE"
          else
            "SQL_ERROR"
          end

      event.set("[GuardRecord][exception][exceptionTypeId]", exceptionType)
      event.set("[GuardRecord][exception][description]",  msg)
      event.set("[GuardRecord][exception][sqlString]", "Undisclosed")
      event.set('[GuardRecord][data][originalSqlCommand]', nil)
      event.set("[GuardRecord][accessor][dbUser]", "Undisclosed")
      event.set("[GuardRecord][sessionLocator][clientIp]", "0.0.0.0")
      ts_epoch = Time.parse(timestamp).to_i
      ts_epoch_u = ts_epoch * (10**3)
      event.set('[GuardRecord][time][timstamp]', ts_epoch_u)


    rescue Exception => e
      @logger.error("in parseErrLog: ", e.mes***REMOVED***ge)
      raise Exception.new "An error occured while trying to parse error log"
    end
  end # def parseErrLog

  public
  def filter(event)
    matched = false
    mes***REMOVED***ge = nil

  	begin
  		log_name = event.get("logName").match(/.*%2F(?<log_type>.*)/)
      log_type = log_name["log_type"]

      sessionId = 0
      original_sql = nil
      clientIp = nil

      @logger.debug("Parsing by log type")
  		if log_type == "mysql-general.log"
        parseGeneralLog(event)
      elsif log_type == "mysql.err"
        parseErrLog(event)
      else
        @logger.debug("Unsupported log type")
        event.cancel
      end

      matched = true

      resource = event.get("resource")
      labels = resource["labels"]
      database_id = labels["database_id"]
      server_hostname = labels["region"] + ":" + database_id
      type_service = resource["type"]

  		event.set('[GuardRecord][time][minOffsetFromGMT]', 0)
  		event.set('[GuardRecord][time][minDst]', 0)

  		event.set("[GuardRecord][data][construct]", nil)

  		event.set("[GuardRecord][sessionLocator][clientPort]", 0)
  		event.set("[GuardRecord][sessionLocator][clientIpv6]", nil)
  		event.set("[GuardRecord][sessionLocator][serverIpv6]", nil)

  		event.set("[GuardRecord][dbName]", database_id)

  		event.set("[GuardRecord][appUserName]", "cloudSQL_service")

  		event.set("[GuardRecord][sessionLocator][serverIp]", "0.0.0.0")
  		event.set("[GuardRecord][sessionLocator][serverPort]", 0)
  		event.set("[GuardRecord][sessionLocator][isIpv6]", false)

  		event.set("[GuardRecord][accessor][serverType]", "MySQL")
  		event.set("[GuardRecord][accessor][serverOS]", "")
  		event.set("[GuardRecord][accessor][clientOs]", "")
  		event.set("[GuardRecord][accessor][clientHostName]", "")
  		event.set("[GuardRecord][accessor][serverHostName]", server_hostname)
  		event.set("[GuardRecord][accessor][commProtocol]", "")
  		event.set("[GuardRecord][accessor][dbProtocol]", "MYSQL")
  		event.set("[GuardRecord][accessor][dbProtocolVersion]", "")
  		event.set("[GuardRecord][accessor][osUser]", "")
  		event.set("[GuardRecord][accessor][sourceProgram]", "")
  		event.set("[GuardRecord][accessor][clientMac]", "")
  		event.set("[GuardRecord][accessor][serverDescription]", "")
  		event.set("[GuardRecord][accessor][serviceName]", type_service)
  		event.set("[GuardRecord][accessor][language]", "MYSQL")
  		event.set("[GuardRecord][accessor][dataType]", "TEXT")

  		event.remove("attributes")
  		event.remove("textPayload")
  		event.remove("@timestamp")
  		event.remove("host")
  		event.remove("receiveTimestamp")
  		event.remove("timestamp")
  		event.remove("@version")
  		event.remove("logName")
  		event.remove("resource")
  		event.remove("mes***REMOVED***geId")
  		event.remove("insertId")
      event.remove("severity")

  		event.set(@target, event.get("GuardRecord").to_json)

  	rescue Exception => e
      @logger.error(e.mes***REMOVED***ge)
  		event.cancel
  	else
      @logger.debug("Sending record to Guardium output plugin")
      @logger.debug(event.to_json)
  		filter_matched(event) if matched
  	end
  end # def filter
end # class LogStash::Filters::PubsubMysql
