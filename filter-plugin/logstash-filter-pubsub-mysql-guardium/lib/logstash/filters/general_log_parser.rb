# Copyright 2020-2022 IBM Inc. All rights reserved
# SPDX-License-Identifier: Apache-2.0

module GeneralLogParser
  # general log parser type superclass
  class GeneralLogParserType
    attr_accessor :src_program

    def self.create(is_proxy)
      case is_proxy
      when true
        GeneralLogProxy.new
      else
        GeneralLog.new
      end
    end

    # general log parser subclass
    class GeneralLog < GeneralLogParserType
      def parse(event)
        begin
          message_no_tabs = event.get('textPayload').gsub /\t/, ' '
          message = message_no_tabs.match(/(?<ts>(\d*-){2}(\d*)T(\d*:){2}(\d*.\d*)Z)([\sa-zA-Z]*)\[(?<db_user>[A-Za-z]*)\].*@.*\[(?<client_ip>(\d)*.(\d)*.(\d)*.(\d)*)\]\s*(?<session_id>\d*)\s(?<connection_id>\d*)\s*Query(\s)(?<original_sql>.*)/)
          session_id = message["session_id"]
          original_sql = message["original_sql"]
          client_ip = message["client_ip"]
          db_user = message["db_user"]
          self.src_program = "Google Cloud Platform"

        rescue StandardError
          raise FilterException::GeneralLogParserErr
        end
        event.set('[GuardRecord][exception]', nil)
        event.set('[GuardRecord][data][originalSqlCommand]', original_sql)
        event.set('[GuardRecord][sessionId]', session_id)
        event.set('[GuardRecord][sessionLocator][clientIp]', client_ip)
        event.set('[GuardRecord][accessor][dbUser]', db_user)

      end
    end

    # general log proxy parser subclass
    class GeneralLogProxy < GeneralLogParserType
      def parse(event)
        message_no_tabs = event.get('textPayload').gsub /\t/, ' '
        raise FilterException::GeneralLogProxyParserSQLErr if message_no_tabs =~ /SHOW WARNINGS/

        raise FilterException::GeneralLogProxyParserComment if message_no_tabs !~ /\/*/

        begin
          message = message_no_tabs.match(/(?<ts>(\d*-){2}(\d*)T(\d*:){2}(\d*.\d*)Z)[^\[]*\[(?<db_user>[A-Za-z]*)\](\s*)@(\s*)\[cloudsqlproxy~(?<client_ip>(\d)*.(\d)*.(\d)*.(\d)*)\](\s*)(?<session_id>\d*)(\s*)(?<connection_id>(\d*))(\s*)Query(\s)((?<comment>\/*(.*)*\/)(?<query>.*))/)

          msg_comment = message['comment'].match(/ApplicationName=(?<app_name>[^(\s)]*)(\s)[^-]*-(\s)(?<type>[a-zA-Z]*)/)

        rescue StandardError
          raise FilterException::GeneralLogProxyParserErr
        end

        # exclude Main and Metadata logs (i.e. internal GCP logs) and unsupported SQL client apps
        app_name = msg_comment["app_name"]
        if app_name !~ /DBeaver/ || msg_comment['type'] !~ /SQLEditor/
          raise FilterException::GeneralLogProxyParserBadType
        end

        begin
          session_id = message["session_id"]
          original_sql = message["query"]
          client_ip = message["client_ip"]
          db_user = message["db_user"]

          self.src_program = app_name
        rescue StandardError
          raise FilterException::GeneralLogProxyParserErr
        end


        event.set('[GuardRecord][exception]', nil)
        event.set('[GuardRecord][data][originalSqlCommand]', original_sql)
        event.set('[GuardRecord][sessionId]', session_id)
        event.set('[GuardRecord][sessionLocator][clientIp]', client_ip)
        event.set('[GuardRecord][accessor][dbUser]', db_user)
      end
    end
  end
end
