# Copyright 2020-2022 IBM Inc. All rights reserved
# SPDX-License-Identifier: Apache-2.0

module FilterException
    class FilterError < StandardError
      def initialize(msg = nil)
        super(msg || 'An error occurred while trying to parse an event:')
      end
    end

    class GeneralLogParserErr < FilterError
      def initialize(msg = nil)
        super(msg || 'An error occurred while trying to parse a mysql-general.log event')
      end
    end

    class GeneralLogProxyParserErr < FilterError
      def initialize(msg = nil)
        super(msg || 'An error occurred while trying to parse a mysql-general.log event using cloudsqlproxy')
      end
    end

    class GeneralLogProxyParserBadType < GeneralLogProxyParserErr
      def initialize(msg = nil)
        super(msg || 'Parser caught a bad or missing query type or SQL client application')
      end
    end

    class GeneralLogProxyParserSQLErr < GeneralLogProxyParserErr
      def initialize(msg = nil)
        super(msg || 'SQL syntax error. Check your SQL client application for more information')
      end
    end

    class GeneralLogProxyParserComment < GeneralLogProxyParserErr
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
    end
