Gem::Specification.new do |s|
  s.name = 'logstash-input-google_pubsub'
  s.version         = '2.0.0'
  s.licenses = ['Apache-2.0']
  s.summary = "Consume events from a Google Cloud PubSub service"
  s.description = "This gem is a Logstash input plugin required to be installed on top of the Logstash core pipeline using $LS_HOME/bin/logstash-plugin install gemname. This gem is not a stand-alone program."
  s.authors = ["Eric Johnson"]
  s.email = 'erjohnso@google.com'
  s.homepage = "https://cloud.google.com/pubsub/overview"
  s.require_paths = ["lib", "vendor/jar-dependencies"]

  # Files
  s.files = Dir["lib/**/*","spec/**/*","*.gemspec","*.md","CONTRIBUTORS","Gemfile","LICENSE","NOTICE.TXT", "vendor/jar-dependencies/**/*.jar", "vendor/jar-dependencies/**/*.rb", "VERSION", "docs/**/*"]
   # Tests
  s.test_files = s.files.grep(%r{^(test|spec|features)/})

  # Special flag to let us know this is actually a logstash plugin
  s.metadata = { "logstash_plugin" => "true", "logstash_group" => "input" }

  # Gem dependencies
  s.add_runtime_dependency 'logstash-core', '>= 8.2.0'
  s.add_runtime_dependency "logstash-core-plugin-api", ">= 1.60", "<= 2.99"
  s.add_runtime_dependency 'logstash-codec-plain'
  s.add_runtime_dependency 'stud', '>= 0.0.22'
  # Google dependencies
  # with JRuby
  s.requirements << "jar 'com.google.cloud:google-cloud-pubsub', '0.28.0-beta'"
  s.requirements << "jar 'com.google.api.grpc:proto-google-cloud-pubsub-v1', '0.1.24'"
  s.requirements << "jar 'com.google.api:gax', '1.14.0'"
  s.requirements << "jar 'com.google.guava:guava', '20.0'"
  s.requirements << "jar 'com.google.api:api-common', '1.2.0'"
  s.requirements << "jar 'com.google.auth:google-auth-library-oauth2-http', '0.9.0'"
  s.add_development_dependency 'logstash-devutils'
  s.add_development_dependency 'jar-dependencies', '~> 0.4'
end
