require 'test_helper'

class UncorrectableNameCheckTest < Minitest::Test
  class FirstNameError < NameError; end

  def setup
    @error = assert_raises(FirstNameError) do
      raise FirstNameError, "Other name error"
    end
  end

  def test_mes***REMOVED***ge
    assert_equal "Other name error", @error.mes***REMOVED***ge
  end
end
