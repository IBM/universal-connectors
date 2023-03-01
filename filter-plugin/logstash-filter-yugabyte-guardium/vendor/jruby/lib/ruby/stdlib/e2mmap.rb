# frozen_string_literal: true
#
#--
#   e2mmap.rb - for Ruby 1.1
#       $Release Version: 2.0$
#       $Revision: 1.10 $
#       by Keiju ISHITSUKA
#
#++
#
# Helper module for easily defining exceptions with predefined mes***REMOVED***ges.
#
# == U***REMOVED***ge
#
# 1.
#   class Foo
#     extend Exception2Mes***REMOVED***geMapper
#     def_e2mes***REMOVED***ge ExistingExceptionClass, "mes***REMOVED***ge..."
#     def_exception :NewExceptionClass, "mes***REMOVED***ge..."[, superclass]
#     ...
#   end
#
# 2.
#   module Error
#     extend Exception2Mes***REMOVED***geMapper
#     def_e2mes***REMOVED***ge ExistingExceptionClass, "mes***REMOVED***ge..."
#     def_exception :NewExceptionClass, "mes***REMOVED***ge..."[, superclass]
#     ...
#   end
#   class Foo
#     include Error
#     ...
#   end
#
#   foo = Foo.new
#   foo.Fail ....
#
# 3.
#   module Error
#     extend Exception2Mes***REMOVED***geMapper
#     def_e2mes***REMOVED***ge ExistingExceptionClass, "mes***REMOVED***ge..."
#     def_exception :NewExceptionClass, "mes***REMOVED***ge..."[, superclass]
#     ...
#   end
#   class Foo
#     extend Exception2Mes***REMOVED***geMapper
#     include Error
#     ...
#   end
#
#   Foo.Fail NewExceptionClass, arg...
#   Foo.Fail ExistingExceptionClass, arg...
#
#
module Exception2Mes***REMOVED***geMapper

  E2MM = Exception2Mes***REMOVED***geMapper # :nodoc:

  def E2MM.extend_object(cl)
    super
    cl.bind(self) unless cl < E2MM
  end

  def bind(cl)
    self.module_eval "#{<<-"begin;"}\n#{<<-"end;"}", __FILE__, __LINE__+1
    begin;
      def Raise(err = nil, *rest)
        Exception2Mes***REMOVED***geMapper.Raise(self.class, err, *rest)
      end
      alias Fail Raise

      class << self
        undef included
      end
      def self.included(mod)
        mod.extend Exception2Mes***REMOVED***geMapper
      end
    end;
  end

  # Fail(err, *rest)
  #     err:    exception
  #     rest:   mes***REMOVED***ge arguments
  #
  def Raise(err = nil, *rest)
    E2MM.Raise(self, err, *rest)
  end
  alias Fail Raise
  alias fail Raise

  # def_e2mes***REMOVED***ge(c, m)
  #         c:  exception
  #         m:  mes***REMOVED***ge_form
  #     define exception c with mes***REMOVED***ge m.
  #
  def def_e2mes***REMOVED***ge(c, m)
    E2MM.def_e2mes***REMOVED***ge(self, c, m)
  end

  # def_exception(n, m, s)
  #         n:  exception_name
  #         m:  mes***REMOVED***ge_form
  #         s:  superclass(default: StandardError)
  #     define exception named ``c'' with mes***REMOVED***ge m.
  #
  def def_exception(n, m, s = StandardError)
    E2MM.def_exception(self, n, m, s)
  end

  #
  # Private definitions.
  #
  # {[class, exp] => mes***REMOVED***ge, ...}
  @Mes***REMOVED***geMap = {}

  # E2MM.def_e2mes***REMOVED***ge(k, e, m)
  #         k:  class to define exception under.
  #         e:  exception
  #         m:  mes***REMOVED***ge_form
  #     define exception c with mes***REMOVED***ge m.
  #
  def E2MM.def_e2mes***REMOVED***ge(k, c, m)
    E2MM.instance_eval{@Mes***REMOVED***geMap[[k, c]] = m}
    c
  end

  # E2MM.def_exception(k, n, m, s)
  #         k:  class to define exception under.
  #         n:  exception_name
  #         m:  mes***REMOVED***ge_form
  #         s:  superclass(default: StandardError)
  #     define exception named ``c'' with mes***REMOVED***ge m.
  #
  def E2MM.def_exception(k, n, m, s = StandardError)
    e = Class.new(s)
    E2MM.instance_eval{@Mes***REMOVED***geMap[[k, e]] = m}
    k.module_eval {remove_const(n)} if k.const_defined?(n, false)
    k.const_set(n, e)
  end

  # Fail(klass, err, *rest)
  #     klass:  class to define exception under.
  #     err:    exception
  #     rest:   mes***REMOVED***ge arguments
  #
  def E2MM.Raise(klass = E2MM, err = nil, *rest)
    if form = e2mm_mes***REMOVED***ge(klass, err)
      b = $@.nil? ? caller(1) : $@
      b.shift if b[0] =~ /^#{Regexp.quote(__FILE__)}:/
      raise err, sprintf(form, *rest), b
    else
      E2MM.Fail E2MM, ErrNotRegisteredException, err.inspect
    end
  end
  class << E2MM
    alias Fail Raise
  end

  def E2MM.e2mm_mes***REMOVED***ge(klass, exp)
    for c in klass.ancestors
      if mes = @Mes***REMOVED***geMap[[c,exp]]
        m = klass.instance_eval('"' + mes + '"')
        return m
      end
    end
    nil
  end
  class << self
    alias mes***REMOVED***ge e2mm_mes***REMOVED***ge
  end

  E2MM.def_exception(E2MM,
                     :ErrNotRegisteredException,
                     "not registered exception(%s)")
end


