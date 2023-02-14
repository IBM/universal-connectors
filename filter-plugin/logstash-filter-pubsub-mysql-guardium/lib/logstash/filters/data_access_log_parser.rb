# Copyright 2020-2022 IBM Inc. All rights reserved
# SPDX-License-Identifier: Apache-2.0

module DataAccessLogParser
  # data_access log parser type superclass
  class DataAccessLogParserType

    def self.parse(event, is_proxy)
      proto_payload = event.get('protoPayload')
      svc_name = proto_payload['serviceName']
      request = proto_payload['request']

      if request.include? 'objects'
        objects = request['objects']
        if not objects.empty?
          first_object = objects[0]
          db_name = first_object['db']
        else
          db_name = 'Undisclosed'
        end
      else
        db_name = 'Undisclosed'
      end

      app_uname = request['auditId']
      if app_uname == nil
        app_uname = 'Undisclosed'
      end

      query = request['query']
      begin

        if query.start_with? "/*"
          index = query.index("*/") + 2
          query_element = query[index..-1]
          comment_element = query[0..index-1]
          starts_with_comment = true
        else
          query_element = query
        end

        original_sql = query_element
        # remove redundant ws if any
        if original_sql.start_with? " "
          original_sql = original_sql[1..-1]
        end

        if  starts_with_comment
          msg_comment = comment_element.match(/ApplicationName=(?<app_name>[a-zA-Z]*)(\s)(?<version>\d*.\d*.\d*)?((\s)-(\s)(?<type>[a-zA-Z]*))?/)
          app_name = "#{msg_comment['app_name']} #{msg_comment['version']}"
        elsif not is_proxy
          app_name = "Google Cloud Shell (gcloud)"
        else
          app_name = "MySQL Client Application"
        end
      rescue StandardError
        raise FilterException::DataAccessLogProxyParserErr
      end

      begin
        if is_proxy
         ip_prefix = 'cloudsqlproxy~'
        else
          ip_prefix = ''
        end

        server_ip_array = request['ip'].match(/#{ip_prefix}(?<ip_address>\d*.\d*.\d*.\d*)/)
        server_ip = server_ip_array['ip_address']
      rescue StandardError
        raise FilterException::BadIPPrefixCloudSQL
      end

      # generate a consistent hash key for db_name
      db_name_key= (Digest::SHA1.hexdigest db_name).to_i(16)
      # avoiding arbitrary size longer than 64 bits
      db_name_key = db_name_key.to_s[0..9]

      # session_id is comprised of the concatenation of the session id and the hash key generated above
      # this resolves Sniffer not being able to parse varying dbNames and assign them to separate sessions
      # with no prior "use db_name" command, which isn't audited in GCP for the used plug-in (cloudsql_mysql_audit)
      session_id = "#{request["threadId"]}#{db_name_key}"

      db_user = request["user"]

      if request['status'] =~ /unsuccessful/
        event.set('[GuardRecord][exception][exceptionTypeId]', 'SQL_ERROR')
        event.set('[GuardRecord][exception][description]', 'Undisclosed')
        event.set('[GuardRecord][exception][sqlString]', original_sql)
        event.set('[GuardRecord][data][originalSqlCommand]', nil)
      else
        event.set('[GuardRecord][exception]', nil)
        event.set('[GuardRecord][data][originalSqlCommand]', original_sql)

      end
      event.set('[GuardRecord][sessionId]', session_id)
      event.set('[GuardRecord][sessionLocator][serverIp]', server_ip)
      event.set('[GuardRecord][accessor][dbUser]', db_user)
      event.set('[GuardRecord][dbName]', db_name)
      event.set('[GuardRecord][accessor][serviceName]', svc_name)
      event.set('[GuardRecord][accessor][sourceProgram]', app_name)
      event.set('[GuardRecord][appUserName]', app_uname)


    end

    def self.create(is_proxy)
      case is_proxy
      when true
        DataAccessLogProxy.new
      else
        DataAccessLog.new
      end
    end

    # data_access log parser subclass
    class DataAccessLog < DataAccessLogParserType
      def parse(event)
        DataAccessLogParser::DataAccessLogParserType.parse(event, false)
      end
    end

    # data_access log proxy parser subclass
    class DataAccessLogProxy < DataAccessLogParserType
      def parse(event)
        DataAccessLogParser::DataAccessLogParserType.parse(event, true)
      end
    end
  end
end
