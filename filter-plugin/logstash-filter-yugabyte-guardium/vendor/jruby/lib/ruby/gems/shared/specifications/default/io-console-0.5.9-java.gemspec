# -*- encoding: utf-8 -*-
# stub: io-console 0.5.9 java lib

Gem::Specification.new do |s|
  s.name = "io-console".freeze
  s.version = "0.5.9"
  s.platform = "java".freeze

  s.required_rubygems_version = Gem::Requirement.new(">= 0".freeze) if s.respond_to? :required_rubygems_version=
  s.metadata = { "source_code_url" => "https://github.com/ruby/io-console" } if s.respond_to? :metadata=
  s.require_paths = ["lib".freeze]
  s.authors = ["Nobu Nakada".freeze]
  s.date = "2021-03-10"
  s.description = "add console capabilities to IO instances.".freeze
  s.email = "nobu@ruby-lang.org".freeze
  s.files = ["LICENSE.txt".freeze, "README.md".freeze, "lib/io/console.rb".freeze, "lib/io/console/ffi/bsd_console.rb".freeze, "lib/io/console/ffi/common.rb".freeze, "lib/io/console/ffi/console.rb".freeze, "lib/io/console/ffi/linux_console.rb".freeze, "lib/io/console/ffi/native_console.rb".freeze, "lib/io/console/ffi/stty_console.rb".freeze, "lib/io/console/ffi/stub_console.rb".freeze, "lib/io/console/size.rb".freeze]
  s.homepage = "https://github.com/ruby/io-console".freeze
  s.licenses = ["Ruby".freeze, "BSD-2-Clause".freeze]
  s.required_ruby_version = Gem::Requirement.new(">= 2.4.0".freeze)
  s.rubygems_version = "2.6.14.1".freeze
  s.summary = "Console interface".freeze
end
