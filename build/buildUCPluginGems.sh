#!/bin/bash
BASE_DIR=$(pwd)

function adjustToLogstash8() {
  IS_MAC_M1_BUILD=true # change to true to build on Mac M1 build
  if [ "$IS_MAC_M1_BUILD" = true ]; then
    sed 's/logstash-core-.*\.jar/logstash-core.jar/' build.gradle > tmp && mv tmp build.gradle
    #sed '/ext { snakeYamlVersion.*/d' build.gradle > tmp && mv tmp build.gradle
    #sed  "/^buildscript.*/a ext { snakeYamlVersion = \"$snakeYamlVersion\" }" build.gradle > tmp && mv tmp build.gradle
  else
    sed -i 's/logstash-core-*.*.*.jar/logstash-core.jar/' build.gradle
    #sed -i '/ext { snakeYamlVersion.*/d' build.gradle
    #sed -i '/^buildscript.*/a ext { snakeYamlVersion = '$snakeYamlVersion' }' build.gradle
  fi
}
function buildUCPluginGem() {
  echo "================ Building $1 gem file================"
  cd ${BASE_DIR}/$1
  adjustToLogstash8
  cp ${BASE_DIR}/build/gradle.properties .
  chmod 755 gradlew
  ./gradlew --no-daemon test </dev/null
  if [ $? -eq 0 ]; then
    echo "Successfully test $1"
      ./gradlew --no-daemon gem </dev/null
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
  chmod -R 775 ${BASE_DIR}/common
  cd ${BASE_DIR}/common
  ./gradlew test
  if [ $? -eq 0 ]; then
    echo "Successfully test uc-commons"
  else
    echo "Failed test uc-commons"
    exit 1
  fi
  #check if succeed
  ./gradlew jar
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

#snakeYamlVersion=$(grep -E "snakeYamlVersion =" /usr/share/logstashSRC/logstash/build.gradle | cut -d \' -f 2)

# Build the rest of the plugins from javaPluginsToBuild.txt
grep -v '^#' ${BASE_DIR}/build/javaPluginsToBuild.txt | while read -r line; do buildUCPluginGem "$line";done
grep -v '^#' ${BASE_DIR}/build/rubyPluginsToBuild.txt | while read -r line; do buildRubyPlugin "${line}" "${line##*/}.gemspec"; done

exit 0