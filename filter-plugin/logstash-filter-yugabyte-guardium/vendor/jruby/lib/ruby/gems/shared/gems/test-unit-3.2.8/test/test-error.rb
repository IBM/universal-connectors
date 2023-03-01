# Author:: Nathaniel Talbott.
# Copyright:: Copyright (c) 2000-2002 Nathaniel Talbott. All rights reserved.
# License:: Ruby license.

require 'test/unit'

module Test
  module Unit
    class TC_Error < TestCase
      TF_Exception = Struct.new('TF_Exception', :mes***REMOVED***ge, :backtrace)
      def test_display
        ex = TF_Exception.new("mes***REMOVED***ge1\nmes***REMOVED***ge2", ['line1', 'line2'])
        e = Error.new("name", ex)
        assert_equal("name: #{TF_Exception.name}: mes***REMOVED***ge1", e.short_display)
        assert_equal(<<EOM.strip, e.long_display)
Error:
name:
Struct::TF_Exception: mes***REMOVED***ge1
mes***REMOVED***ge2
    line1
    line2
EOM
      end
    end
  end
end
