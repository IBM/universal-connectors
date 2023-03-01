# Author:: Nathaniel Talbott.
# Copyright:: Copyright (c) 2003 Nathaniel Talbott. All rights reserved.
# License:: Ruby license.

require 'test/unit'
require 'test/unit/failure'

module Test::Unit
  class TestFailure < TestCase
    def test_display
      f = Failure.new("name", [%q{location:1 in 'l'}], "mes***REMOVED***ge1\nmes***REMOVED***ge2")
      assert_equal("name: mes***REMOVED***ge1", f.short_display)
      assert_equal(<<EOM.strip, f.long_display)
Failure:
name [location:1]:
mes***REMOVED***ge1
mes***REMOVED***ge2
EOM

      f = Failure.new("name", [%q{location1:2 in 'l1'}, 'location2:1', %q{location3:3 in 'l3'}], "mes***REMOVED***ge1\nmes***REMOVED***ge2")
      assert_equal("name: mes***REMOVED***ge1", f.short_display)
      assert_equal(<<EOM.strip, f.long_display)
Failure:
name
    [location1:2 in 'l1'
     location2:1
     location3:3 in 'l3']:
mes***REMOVED***ge1
mes***REMOVED***ge2
EOM
    end
  end
end
