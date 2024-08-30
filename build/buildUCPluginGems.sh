#!/bin/bash
BASE_DIR=$(pwd)

function adjustToLogstash8() {
  sed -i 's/logstash-core-*.*.*.jar/logstash-core.jar/' build.gradle
}
function buildUCPluginGem() {
  echo "================ Building $1 gem file================"
  cd ${BASE_DIR}/$1
  adjustToLogstash8
  cp ${BASE_DIR}/build/gradle.properties .
  ./gradlew --no-daemon test </dev/null >/dev/null 2>&1
  if [ $? -eq 0 ]; then
    echo "Successfully test $1"
      ./gradlew --no-daemon gem </dev/null >/dev/null 2>&1
      if [ $? -eq 0 ]; then
        echo "Successfully build gem $1"
      else
        echo "Failed build gem $1"
      fi
  else
    echo "Failed test $1"
  fi
}

function buildUCCommons() {
  cd ${BASE_DIR}/common
  ./gradlew test >/dev/null 2>&1
  if [ $? -eq 0 ]; then
    echo "Successfully test uc-commons"
  else
    echo "Failed test uc-commons"
    exit 1
  fi
  #check if succeed
  ./gradlew jar >/dev/null 2>&1
  if [ $? -eq 0 ]; then
    echo "Successfully build jar uc-commons"
  else
    echo "Failed build jar uc-commons"
    exit 2
  fi
  cp ./build/libs/common-1.0.0.jar ./build/libs/guardium-universalconnector-commons-1.0.0.jar
  cd ../../
}

function buildRubyPlugin(){
  cd ${BASE_DIR}/$1
  bundle install >/dev/null 2>&1
  gem build $2
}

buildUCCommons

# Build the rest of the plugins from javaPluginsToBuild.txt
grep -v '^#' ${BASE_DIR}/build/javaPluginsToBuild.txt | while read -r line; do buildUCPluginGem "$line";done
grep -v '^#' ${BASE_DIR}/build/rubyPluginsToBuild.txt | while read -r line; do buildRubyPlugin "${line}" "${line##*/}.gemspec"; done

exit 0