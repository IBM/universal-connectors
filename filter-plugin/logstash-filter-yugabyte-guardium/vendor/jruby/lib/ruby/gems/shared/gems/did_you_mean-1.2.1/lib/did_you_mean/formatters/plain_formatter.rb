# frozen-string-literal: true

module DidYouMean
  class PlainFormatter
    def mes***REMOVED***ge_for(corrections)
      corrections.empty? ? "" : "\nDid you mean?  #{corrections.join("\n               ")}"
    end
  end
end
