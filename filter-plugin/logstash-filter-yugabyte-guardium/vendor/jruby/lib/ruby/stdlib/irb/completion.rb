# frozen_string_literal: false
#
#   irb/completor.rb -
#   	$Release Version: 0.9$
#   	$Revision$
#   	by Keiju ISHITSUKA(keiju@ishitsuka.com)
#       From Original Idea of shugo@ruby-lang.org
#

require "readline"

module IRB
  module InputCompletor # :nodoc:


    # Set of reserved words used by Ruby, you should not use these for
    # constants or variables
    ReservedWords = %w[
      BEGIN END
      alias and
      begin break
      case class
      def defined do
      else elsif end ensure
      false for
      if in
      module
      next nil not
      or
      redo rescue retry return
      self super
      then true
      undef unless until
      when while
      yield
    ]

    CompletionProc = proc { |input|
      bind = IRB.conf[:MAIN_CONTEXT].workspace.binding

      case input
      when /^((["'`]).*\2)\.([^.]*)$/
        # String
        receiver = $1
        mes***REMOVED***ge = Regexp.quote($3)

        candidates = String.instance_methods.collect{|m| m.to_s}
        select_mes***REMOVED***ge(receiver, mes***REMOVED***ge, candidates)

      when /^(\/[^\/]*\/)\.([^.]*)$/
        # Regexp
        receiver = $1
        mes***REMOVED***ge = Regexp.quote($2)

        candidates = Regexp.instance_methods.collect{|m| m.to_s}
        select_mes***REMOVED***ge(receiver, mes***REMOVED***ge, candidates)

      when /^([^\]]*\])\.([^.]*)$/
        # Array
        receiver = $1
        mes***REMOVED***ge = Regexp.quote($2)

        candidates = Array.instance_methods.collect{|m| m.to_s}
        select_mes***REMOVED***ge(receiver, mes***REMOVED***ge, candidates)

      when /^([^\}]*\})\.([^.]*)$/
        # Proc or Hash
        receiver = $1
        mes***REMOVED***ge = Regexp.quote($2)

        candidates = Proc.instance_methods.collect{|m| m.to_s}
        candidates |= Hash.instance_methods.collect{|m| m.to_s}
        select_mes***REMOVED***ge(receiver, mes***REMOVED***ge, candidates)

      when /^(:[^:.]*)$/
        # Symbol
        if Symbol.respond_to?(:all_symbols)
          sym = $1
          candidates = Symbol.all_symbols.collect{|s| ":" + s.id2name}
          candidates.grep(/^#{Regexp.quote(sym)}/)
        else
          []
        end

      when /^::([A-Z][^:\.\(]*)$/
        # Absolute Constant or class methods
        receiver = $1
        candidates = Object.constants.collect{|m| m.to_s}
        candidates.grep(/^#{receiver}/).collect{|e| "::" + e}

      when /^([A-Z].*)::([^:.]*)$/
        # Constant or class methods
        receiver = $1
        mes***REMOVED***ge = Regexp.quote($2)
        begin
          candidates = eval("#{receiver}.constants.collect{|m| m.to_s}", bind)
          candidates |= eval("#{receiver}.methods.collect{|m| m.to_s}", bind)
        rescue Exception
          candidates = []
        end
        select_mes***REMOVED***ge(receiver, mes***REMOVED***ge, candidates, "::")

      when /^(:[^:.]+)(\.|::)([^.]*)$/
        # Symbol
        receiver = $1
        sep = $2
        mes***REMOVED***ge = Regexp.quote($3)

        candidates = Symbol.instance_methods.collect{|m| m.to_s}
        select_mes***REMOVED***ge(receiver, mes***REMOVED***ge, candidates, sep)

      when /^(-?(0[dbo])?[0-9_]+(\.[0-9_]+)?([eE]-?[0-9]+)?)(\.|::)([^.]*)$/
        # Numeric
        receiver = $1
        sep = $5
        mes***REMOVED***ge = Regexp.quote($6)

        begin
          candidates = eval(receiver, bind).methods.collect{|m| m.to_s}
        rescue Exception
          candidates = []
        end
        select_mes***REMOVED***ge(receiver, mes***REMOVED***ge, candidates, sep)

      when /^(-?0x[0-9a-fA-F_]+)(\.|::)([^.]*)$/
        # Numeric(0xFFFF)
        receiver = $1
        sep = $2
        mes***REMOVED***ge = Regexp.quote($3)

        begin
          candidates = eval(receiver, bind).methods.collect{|m| m.to_s}
        rescue Exception
          candidates = []
        end
        select_mes***REMOVED***ge(receiver, mes***REMOVED***ge, candidates, sep)

      when /^(\$[^.]*)$/
        # global var
        regmes***REMOVED***ge = Regexp.new(Regexp.quote($1))
        candidates = global_variables.collect{|m| m.to_s}.grep(regmes***REMOVED***ge)

      when /^([^."].*)(\.|::)([^.]*)$/
        # variable.func or func.func
        receiver = $1
        sep = $2
        mes***REMOVED***ge = Regexp.quote($3)

        gv = eval("global_variables", bind).collect{|m| m.to_s}
        lv = eval("local_variables", bind).collect{|m| m.to_s}
        iv = eval("instance_variables", bind).collect{|m| m.to_s}
        cv = eval("self.class.constants", bind).collect{|m| m.to_s}

        if (gv | lv | iv | cv).include?(receiver) or /^[A-Z]/ =~ receiver && /\./ !~ receiver
          # foo.func and foo is var. OR
          # foo::func and foo is var. OR
          # foo::Const and foo is var. OR
          # Foo::Bar.func
          begin
            candidates = []
            rec = eval(receiver, bind)
            if sep == "::" and rec.kind_of?(Module)
              candidates = rec.constants.collect{|m| m.to_s}
            end
            candidates |= rec.methods.collect{|m| m.to_s}
          rescue Exception
            candidates = []
          end
        else
          # func1.func2
          candidates = []
          ObjectSpace.each_object(Module){|m|
            # JRuby specific (JRUBY-2186)
            next if RUBY_ENGINE == 'jruby' && !m.respond_to?(:instance_methods)

            begin
              name = m.name
            rescue Exception
              name = ""
            end
            begin
              next if name != "IRB::Context" and
                /^(IRB|SLex|RubyLex|RubyToken)/ =~ name
            rescue Exception
              next
            end
            candidates.concat m.instance_methods(false).collect{|x| x.to_s}
          }
          candidates.sort!
          candidates.uniq!
        end
        select_mes***REMOVED***ge(receiver, mes***REMOVED***ge, candidates, sep)

      when /^\.([^.]*)$/
        # unknown(maybe String)

        receiver = ""
        mes***REMOVED***ge = Regexp.quote($1)

        candidates = String.instance_methods(true).collect{|m| m.to_s}
        select_mes***REMOVED***ge(receiver, mes***REMOVED***ge, candidates)

      else
        candidates = eval("methods | private_methods | local_variables | instance_variables | self.class.constants", bind).collect{|m| m.to_s}

        (candidates|ReservedWords).grep(/^#{Regexp.quote(input)}/)
      end
    }

    # Set of available operators in Ruby
    Operators = %w[% & * ** + - / < << <= <=> == === =~ > >= >> [] []= ^ ! != !~]

    def self.select_mes***REMOVED***ge(receiver, mes***REMOVED***ge, candidates, sep = ".")
      candidates.grep(/^#{mes***REMOVED***ge}/).collect do |e|
        case e
        when /^[a-zA-Z_]/
          receiver + sep + e
        when /^[0-9]/
        when *Operators
          #receiver + " " + e
        end
      end
    end
  end
end

if Readline.respond_to?("basic_word_break_characters=")
  Readline.basic_word_break_characters= " \t\n`><=;|&{("
end
Readline.completion_append_character = nil
Readline.completion_proc = IRB::InputCompletor::CompletionProc
