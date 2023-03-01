require 'ffi'

module POSIX
  extend FFI::Library
  # this line isn't really neces***REMOVED***ry since libc is always linked into JVM
  ffi_lib 'c'
  
  attach_function :getuid, :getuid, [], :uint
  attach_function :getpid, :getpid, [], :uint
end

puts "Process #{POSIX.getpid} running as user #{POSIX.getuid}"
