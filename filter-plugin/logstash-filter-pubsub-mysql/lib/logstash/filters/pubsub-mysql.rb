# encoding: utf-8
require "logstash/filters/base"
require "logstash/namespace"
require "json"

# This  filter will replace the contents of the default
# message field with whatever you specify in the configuration.
#
# It is only intended to be used as an .
class LogStash::Filters::PubsubMysql < LogStash::Filters::Base

  # Setting the config_name here is required. This is how you
  # configure this filter from your Logstash config.
  #
  # filter {
  #    {
  #     message => "My message..."
  #   }
  # }
  #
  config_name "pubsub-mysql"

  # Replace the message with this value.
  # config :message, :validate => :string, :default => "Hello World!"
  # The target field to place all the data
  config :target, :validate => :string, :default => "GuardRecord"

  public
  def register
    # Add instance variables
    @logger.debug("Registering Google PubSub MySQL filter")
  end # def register

  public
  def filter(event)
    matched = false
    message = nil

  	begin
  		log_name = event.get("logName").match(/.*%2F(?<log_type>.*)/)
      log_type = log_name["log_type"]

      sessionId = 0

  		if log_type == "mysql-general.log"
  			message_no_tabs = event.get("textPayload").gsub /\t/, ' '
  			message = message_no_tabs.match(/(?<ts>(\d*-){2}(\d*)T(\d*:){2}(\d*.\d*)Z)(\s)?(?<db_user>[A-Za-z]*).*@.*\[(?<client_ip>(\d)*.(\d)*.(\d)*.(\d)*)](?<session_id>\d*)\s(?<connection_id>\d*)\s*Query(\s)(?<original_sql>.*)/)
        sessionId = message["session_id"]
        event.set("[GuardRecord][exception]", nil)
      end
  		if log_type == "mysql.err"
  			message = event.match(/(?<ts>(\d*-){2}(\d*)T(\d*:){2}(\d*.\d*)Z)(\s)(?<session_id>\d*)(\s)\[(?<severity_type>[A-Za-z]*)](\s)\[(?<type_db>[A-Za-z]*-\d*)](\s)\[(?<type_host>.*)](\s)(?<msg>.*)/)
        sessionId = message["session_id"] if (!message == nil && !message == "")

        message = event.match(/(?<msg>.*)/) if (message == nil || message == "")
  			event.set("[GuardRecord][exception][exceptionTypeId]", "SQL_ERROR")
  			desc = message["msg"]
  			event.set("[GuardRecord][exception][description]",  desc)
  			event.set("[GuardRecord][exception][sqlString]", nil)

  		else
  			event.set("[GuardRecord][exception]", nil)
  		end


      matched = true

  		event.set('[GuardRecord][time][timestamp]', event.get('timestamp'))
  		event.set('[GuardRecord][time][minOffsetFromGMT]', 0)
  		event.set('[GuardRecord][time][minDst]', 0)

  		event.set('[GuardRecord][data][originalSqlCommand]', message["original_sql"])
  		event.set("[GuardRecord][data][construct]", nil)

  		event.set("[GuardRecord][sessionLocator][clientIp]", message["client_ip"])
  		event.set("[GuardRecord][sessionLocator][clientPort]", 0)
  		event.set("[GuardRecord][sessionLocator][clientIpv6]", nil)
  		event.set("[GuardRecord][sessionLocator][serverIpv6]", nil)

  		event.set("[GuardRecord][dbName]", event.get("resource")["labels"]["database_id"])

  		event.set("[GuardRecord][appUserName]", "cloudSQL_service")

  		event.set("[GuardRecord][sessionId]", sessionId)

  		event.set("[GuardRecord][sessionLocator][serverIp]", "0.0.0.0")
  		event.set("[GuardRecord][sessionLocator][serverPort]", "0")
  		event.set("[GuardRecord][sessionLocator][isIpv6]", false)

  		event.set("[GuardRecord][accessor][dbUser]", message["db_user"])
  		event.set("[GuardRecord][accessor][serverType]", "MySQL")
  		event.set("[GuardRecord][accessor][serverOS]", "")
  		event.set("[GuardRecord][accessor][clientOs]", "")
  		event.set("[GuardRecord][accessor][clientHostName]", "")
  		event.set("[GuardRecord][accessor][serverHostName]", "charged-mind-281913:us-central1:mysql8-google-ritu")
  		event.set("[GuardRecord][accessor][commProtocol]", "")
  		event.set("[GuardRecord][accessor][dbProtocol]", "MYSQL")
  		event.set("[GuardRecord][accessor][dbProtocolVersion]", "")
  		event.set("[GuardRecord][accessor][osUser]", "")
  		event.set("[GuardRecord][accessor][sourceProgram]", "")
  		event.set("[GuardRecord][accessor][clientMac]", "")
  		event.set("[GuardRecord][accessor][serverDescription]", "")
  		event.set("[GuardRecord][accessor][serviceName]", "cloudsql_database")
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
  		event.remove("messageId")
  		event.remove("insertId")


  		event.set(@target, event.get("GuardRecord").to_json)

  	rescue Exception => e
  		event.cancel
  	else
      @logger.debug("Sending record to Guardium output plugin")
      @logger.debug(event.to_json)
  		filter_matched(event) if matched
  	end
  end # def filter
end # class LogStash::Filters::PubsubMysql
