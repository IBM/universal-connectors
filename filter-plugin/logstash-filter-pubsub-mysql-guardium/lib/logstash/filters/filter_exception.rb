# Copyright 2020-2022 IBM Inc. All rights reserved
# SPDX-License-Identifier: Apache-2.0

module FilterException
    class FilterError < StandardError
      def initialize(msg = nil)
        super(msg || 'An error occurred while trying to parse an event:')
      end
    end

    class DataAccessLogParserErr < FilterError
      def initialize(msg = nil)
        super(msg || 'An error occurred while trying to parse a data_access log event')
      end
    end

    class DataAccessLogProxyParserErr < FilterError
      def initialize(msg = nil)
        super(msg || 'An error occurred while trying to parse a data_access log event using cloudsqlproxy')
      end
    end

    class DataAccessLogProxyParserBadType < DataAccessLogProxyParserErr
      def initialize(msg = nil)
        super(msg || 'Parser caught a bad or missing query type or SQL client application')
      end
    end

    class DataAccessLogProxyParserSQLErr < DataAccessLogProxyParserErr
      def initialize(msg = nil)
        super(msg || 'SQL syntax error. Check your SQL client application for more information')
      end
    end

    class DataAccessLogProxyParserComment < DataAccessLogProxyParserErr
      def initialize(msg = nil)
        super(msg || "Illegal log format. Make sure your GCP Sink's filters exclude events without a comment")
      end
    end

    class ErrorLogParserErr < FilterError
      def initialize(msg = nil)
        super(msg || 'An error occurred while trying to parse a mysql.err log event')
      end
    end

    class UnsupportedLogType < FilterError
      def initialize(msg = nil)
        super(msg || 'Unsupported log type')
      end
    end
    class BadIPPrefixCloudSQL < FilterError
      def initialize(msg = nil)
        super(msg || "Client IP type mismatched. Please check that the filter's configuration variable cloudsqlproxy_enabled is enabled for Cloud SQL proxy authentication, and vice versa")

      end
    end
    end
