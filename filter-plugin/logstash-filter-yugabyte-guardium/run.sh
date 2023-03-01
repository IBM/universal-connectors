if [ -n "$LOGSTASH_HOME" ]; then
  echo "building the plugin......"
  ./gradlew gem
  echo "installing the plugin...."
  VERSION=`cat ./VERSION`
  $LOGSTASH_HOME/bin/logstash-plugin install --no-verify --local ./logstash-filter-yugabytedb_guardium_filter-$VERSION.gem
else
  echo "LOGSTASH_HOME is not defined."
fi
