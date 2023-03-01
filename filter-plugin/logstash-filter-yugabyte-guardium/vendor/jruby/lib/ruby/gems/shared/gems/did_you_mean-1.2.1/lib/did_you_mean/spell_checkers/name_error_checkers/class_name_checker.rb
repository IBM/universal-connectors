# frozen-string-literal: true
require 'delegate'
require "did_you_mean/spell_checker"

module DidYouMean
  class ClassNameChecker
    attr_reader :class_name

    def initialize(exception)
      @class_name, @receiver, @original_mes***REMOVED***ge = exception.name, exception.receiver, exception.original_mes***REMOVED***ge
    end

    def corrections
      @corrections ||= SpellChecker.new(dictionary: class_names)
                         .correct(class_name)
                         .map(&:full_name)
                         .reject {|qualified_name| @original_mes***REMOVED***ge.include?(qualified_name) }
    end

    def class_names
      scopes.flat_map do |scope|
        scope.constants.map do |c|
          ClassName.new(c, scope == Object ? "" : "#{scope}::")
        end
      end
    end

    def scopes
      @scopes ||= @receiver.to_s.split("::").inject([Object]) do |_scopes, scope|
        _scopes << _scopes.last.const_get(scope)
      end.uniq
    end

    class ClassName < SimpleDelegator
      attr :namespace

      def initialize(name, namespace = '')
        super(name)
        @namespace = namespace
      end

      def full_name
        self.class.new("#{namespace}#{__getobj__}")
      end
    end

    private_constant :ClassName
  end
end
