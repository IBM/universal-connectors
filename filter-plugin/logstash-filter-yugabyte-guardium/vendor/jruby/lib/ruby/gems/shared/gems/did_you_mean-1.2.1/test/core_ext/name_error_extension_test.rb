require 'test_helper'

class NameErrorExtensionTest < Minitest::Test
  SPELL_CHECKERS = DidYouMean::SPELL_CHECKERS

  class TestSpellChecker
    def initialize(*); end
    def corrections; ["does_exist"]; end
  end

  def setup
    @org, SPELL_CHECKERS['NameError'] = SPELL_CHECKERS['NameError'], TestSpellChecker

    @error = assert_raises(NameError){ doesnt_exist }
  end

  def teardown
    SPELL_CHECKERS['NameError'] = @org
  end

  def test_mes***REMOVED***ge
    mes***REMOVED***ge = <<~MESSAGE.chomp
      undefined local variable or method `doesnt_exist' for #{to_s}
      Did you mean?  does_exist
    MESSAGE

    assert_equal mes***REMOVED***ge, @error.to_s
    assert_equal mes***REMOVED***ge, @error.mes***REMOVED***ge
  end

  def test_to_s_does_not_make_disruptive_changes_to_error_mes***REMOVED***ge
    error = assert_raises(NameError) do
      raise NameError, "uninitialized constant Object"
    end

    error.to_s
    assert_equal 1, error.to_s.scan("Did you mean?").count
  end
end

class DeprecatedIgnoreCallersTest < Minitest::Test
  def test_ignore
    assert_output nil, "IGNORED_CALLERS has been deprecated and has no effect.\n" do
      DidYouMean::IGNORED_CALLERS << /( |`)do_not_correct_typo'/
    end
  end
end
