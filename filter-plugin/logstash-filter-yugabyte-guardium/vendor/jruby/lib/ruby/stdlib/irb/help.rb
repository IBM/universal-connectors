# frozen_string_literal: false
#
#   irb/help.rb - print u***REMOVED***ge module
#   	$Release Version: 0.9.6$
#   	$Revision$
#   	by Keiju ISHITSUKA(keiju@ishitsuka.com)
#
# --
#
#
#

require 'irb/magic-file'

module IRB
  # Outputs the irb help mes***REMOVED***ge, see IRB@Command+line+options.
  def IRB.print_u***REMOVED***ge
    lc = IRB.conf[:LC_MESSAGES]
    path = lc.find("irb/help-mes***REMOVED***ge")
    space_line = false
    IRB::MagicFile.open(path){|f|
      f.each_line do |l|
        if /^\s*$/ =~ l
          lc.puts l unless space_line
          space_line = true
          next
        end
        space_line = false

        l.sub!(/#.*$/, "")
        next if /^\s*$/ =~ l
        lc.puts l
      end
    }
  end
end

