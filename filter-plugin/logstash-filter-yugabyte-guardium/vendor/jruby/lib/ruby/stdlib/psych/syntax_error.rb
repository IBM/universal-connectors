# frozen_string_literal: true
require 'psych/exception'

module Psych
  class SyntaxError < Psych::Exception
    attr_reader :file, :line, :column, :offset, :problem, :context

    def initialize file, line, col, offset, problem, context
      err      = [problem, context].compact.join ' '
      filename = file || '<unknown>'
      mes***REMOVED***ge  = "(%s): %s at line %d column %d" % [filename, err, line, col]

      @file    = file
      @line    = line
      @column  = col
      @offset  = offset
      @problem = problem
      @context = context
      super(mes***REMOVED***ge)
    end
  end
end
