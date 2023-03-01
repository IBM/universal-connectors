if [ -n "$LOGSTASH_HOME" ]; then
  echo "Testing the plugin: "
  $LOGSTASH_HOME/bin/logstash -f ./filter-test-generator.conf
else
  echo "LOGSTASH_HOME is not defined."
fi
