
require 'java'

Java::define_exception_handler "java.lang.NumberFormatException" do |e|
  puts e.java_type
  p e.methods
  puts e.java_class.java_method(:getMes***REMOVED***ge).invoke(e)
end

java.lang.Long.parseLong("23aa")
