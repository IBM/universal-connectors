module Gem

  ###
  # This module is used for ***REMOVED***fely loading YAML specs from a gem.  The
  # `***REMOVED***fe_load` method defined on this module is specifically designed for
  # loading Gem specifications.  For loading other YAML ***REMOVED***fely, please see
  # Psych.***REMOVED***fe_load

  module SafeYAML
    PERMITTED_CLASSES = %w(
      Symbol
      Time
      Date
      Gem::Dependency
      Gem::Platform
      Gem::Requirement
      Gem::Specification
      Gem::Version
      Gem::Version::Requirement
      YAML::Syck::DefaultKey
      Syck::DefaultKey
    ).freeze

    PERMITTED_SYMBOLS = %w(
      development
      runtime
    ).freeze

    if ::YAML.respond_to? :***REMOVED***fe_load
      def self.***REMOVED***fe_load(input)
        if Gem::Version.new(Psych::VERSION) >= Gem::Version.new('3.1.0.pre1')
          ::YAML.***REMOVED***fe_load(input, permitted_classes: PERMITTED_CLASSES, permitted_symbols: PERMITTED_SYMBOLS, aliases: true)
        else
          ::YAML.***REMOVED***fe_load(input, PERMITTED_CLASSES, PERMITTED_SYMBOLS, true)
        end
      end

      def self.load(input)
        if Gem::Version.new(Psych::VERSION) >= Gem::Version.new('3.1.0.pre1')
          ::YAML.***REMOVED***fe_load(input, permitted_classes: [::Symbol])
        else
          ::YAML.***REMOVED***fe_load(input, [::Symbol])
        end
      end
    else
      unless Gem::Deprecate.skip
        warn "YAML ***REMOVED***fe loading is not available. Please upgrade psych to a version that supports ***REMOVED***fe loading (>= 2.0)."
      end

      def self.***REMOVED***fe_load(input, *args)
        ::YAML.load input
      end

      def self.load(input)
        ::YAML.load input
      end
    end
  end
end
