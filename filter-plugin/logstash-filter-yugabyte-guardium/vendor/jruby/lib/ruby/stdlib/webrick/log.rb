# frozen_string_literal: false
#--
# log.rb -- Log Class
#
# Author: IPR -- Internet Programming with Ruby -- writers
# Copyright (c) 2000, 2001 TAKAHASHI Ma***REMOVED***yoshi, GOTOU Yuuzou
# Copyright (c) 2002 Internet Programming with Ruby writers. All rights
# reserved.
#
# $IPR: log.rb,v 1.26 2002/10/06 17:06:10 gotoyuzo Exp $

module WEBrick

  ##
  # A generic logging class

  class BasicLog

    # Fatal log level which indicates a server crash

    FATAL = 1

    # Error log level which indicates a recoverable error

    ERROR = 2

    # Warning log level which indicates a possible problem

    WARN  = 3

    # Information log level which indicates possibly useful information

    INFO  = 4

    # Debugging error level for mes***REMOVED***ges used in server development or
    # debugging

    DEBUG = 5

    # log-level, mes***REMOVED***ges above this level will be logged
    attr_accessor :level

    ##
    # Initializes a new logger for +log_file+ that outputs mes***REMOVED***ges at +level+
    # or higher.  +log_file+ can be a filename, an IO-like object that
    # responds to #<< or nil which outputs to $stderr.
    #
    # If no level is given INFO is chosen by default

    def initialize(log_file=nil, level=nil)
      @level = level || INFO
      case log_file
      when String
        @log = File.open(log_file, "a+")
        @log.sync = true
        @opened = true
      when NilClass
        @log = $stderr
      else
        @log = log_file  # requires "<<". (see BasicLog#log)
      end
    end

    ##
    # Closes the logger (also closes the log device associated to the logger)
    def close
      @log.close if @opened
      @log = nil
    end

    ##
    # Logs +data+ at +level+ if the given level is above the current log
    # level.

    def log(level, data)
      if @log && level <= @level
        data += "\n" if /\n\Z/ !~ data
        @log << data
      end
    end

    ##
    # Synonym for log(INFO, obj.to_s)
    def <<(obj)
      log(INFO, obj.to_s)
    end

    # Shortcut for logging a FATAL mes***REMOVED***ge
    def fatal(msg) log(FATAL, "FATAL " << format(msg)); end
    # Shortcut for logging an ERROR mes***REMOVED***ge
    def error(msg) log(ERROR, "ERROR " << format(msg)); end
    # Shortcut for logging a WARN mes***REMOVED***ge
    def warn(msg)  log(WARN,  "WARN  " << format(msg)); end
    # Shortcut for logging an INFO mes***REMOVED***ge
    def info(msg)  log(INFO,  "INFO  " << format(msg)); end
    # Shortcut for logging a DEBUG mes***REMOVED***ge
    def debug(msg) log(DEBUG, "DEBUG " << format(msg)); end

    # Will the logger output FATAL mes***REMOVED***ges?
    def fatal?; @level >= FATAL; end
    # Will the logger output ERROR mes***REMOVED***ges?
    def error?; @level >= ERROR; end
    # Will the logger output WARN mes***REMOVED***ges?
    def warn?;  @level >= WARN; end
    # Will the logger output INFO mes***REMOVED***ges?
    def info?;  @level >= INFO; end
    # Will the logger output DEBUG mes***REMOVED***ges?
    def debug?; @level >= DEBUG; end

    private

    ##
    # Formats +arg+ for the logger
    #
    # * If +arg+ is an Exception, it will format the error mes***REMOVED***ge and
    #   the back trace.
    # * If +arg+ responds to #to_str, it will return it.
    # * Otherwise it will return +arg+.inspect.
    def format(arg)
      if arg.is_a?(Exception)
        "#{arg.class}: #{AccessLog.escape(arg.mes***REMOVED***ge)}\n\t" <<
        arg.backtrace.join("\n\t") << "\n"
      elsif arg.respond_to?(:to_str)
        AccessLog.escape(arg.to_str)
      else
        arg.inspect
      end
    end
  end

  ##
  # A logging class that prepends a timestamp to each mes***REMOVED***ge.

  class Log < BasicLog
    # Format of the timestamp which is applied to each logged line.  The
    # default is <tt>"[%Y-%m-%d %H:%M:%S]"</tt>
    attr_accessor :time_format

    ##
    # Same as BasicLog#initialize
    #
    # You can set the timestamp format through #time_format
    def initialize(log_file=nil, level=nil)
      super(log_file, level)
      @time_format = "[%Y-%m-%d %H:%M:%S]"
    end

    ##
    # Same as BasicLog#log
    def log(level, data)
      tmp = Time.now.strftime(@time_format)
      tmp << " " << data
      super(level, tmp)
    end
  end
end
