#!/bin/bash
BASE_DIR=$(pwd)

function buildUCCommons() {
  cd "${BASE_DIR}/common" || exit
  ./gradlew test >/dev/null 2>&1
  if [ $? -eq 0 ]; then
    echo "Successfully tested uc-commons"
  else
    echo "Failed to test uc-commons"
    exit 1
  fi
  ./gradlew jar >/dev/null 2>&1
  if [ $? -eq 0 ]; then
    echo "Successfully built jar uc-commons"
  else
    echo "Failed to build jar uc-commons"
    exit 2
  fi
  cp ./build/libs/common-1.0.0.jar ./build/libs/guardium-universalconnector-commons-1.0.0.jar
  cd "${BASE_DIR}" || exit
}

buildUCCommons

# Build Java plugins in parallel
grep -v '^#' "${BASE_DIR}/build/javaPluginsToBuild.txt" | xargs -P 4 -I {} bash -c '
BASE_DIR="'${BASE_DIR}'"

function adjustToLogstash8() {
  sed -i "s/logstash-core-*.*.*.jar/logstash-core.jar/" build.gradle
}

function buildUCPluginGem() {
  echo "================ Building $1 gem file ================="
  cd "${BASE_DIR}/$1" || exit
  adjustToLogstash8
  cp "${BASE_DIR}/build/gradle.properties" .
  ./gradlew --no-daemon test >/dev/null 2>&1
  if [ $? -eq 0 ]; then
    echo "Successfully tested $1"
    ./gradlew --no-daemon gem >/dev/null 2>&1
    if [ $? -eq 0 ]; then
      echo "Successfully built gem $1"
    else
      echo "Failed to build gem $1"
    fi
  else
    echo "Failed to test $1"
  fi
}

buildUCPluginGem "{}"
'

# Build Ruby plugins in parallel
grep -v '^#' "${BASE_DIR}/build/rubyPluginsToBuild.txt" | xargs -P 2 -I {} bash -c '
BASE_DIR="'${BASE_DIR}'"

function buildRubyPlugin() {
  cd "${BASE_DIR}/$1" || exit
  bundle install >/dev/null 2>&1
  gem build "$2"
}

buildRubyPlugin "{}" "${}/*.gemspec"
'

exit 0
