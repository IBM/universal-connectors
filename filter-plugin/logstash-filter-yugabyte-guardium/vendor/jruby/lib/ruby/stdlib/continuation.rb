# This is a partial implementation of Continuation that only supports
# in-scope, one-shot restore using catch/throw.
class Continuation
  class << self
    undef new if defined?(Continuation.new)
  end

  def initialize
    @di***REMOVED***bled = false
  end

  def call
    throw @tag
  end

  def yield_once
    if @di***REMOVED***bled
      throw ThreadError, "continuations can not be called from outside their scope"
    end

    begin
      catch(@tag = Object.new) do
        yield
      end
    ensure
      @di***REMOVED***bled = true
    end
  end

  alias [] call
end

module Kernel
  module_function def callcc(&block)
    cont = Continuation.alloc
    cont.initialize

    cont.yield_once(&block)
  end
end