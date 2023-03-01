# frozen-string-literal: true

module DidYouMean
  class VerboseFormatter
    def mes***REMOVED***ge_for(corrections)
      return "" if corrections.empty?

      output = "\n\n    Did you mean? ".dup
      output << corrections.join("\n                  ")
      output << "\n "
    end
  end
end
