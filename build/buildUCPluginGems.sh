#!/bin/bash

BASE_DIR=$(pwd)
IS_MAC_M1_BUILD=true  # Set to true for Mac M1 builds

# Updates build.gradle to use logstash-core.jar instead of versioned logstash-core JARs.
adjustToLogstash8() {
  local sed_cmd

  if [ "$IS_MAC_M1_BUILD" = true ]; then
    sed_cmd='s/logstash-core-.*\.jar/logstash-core.jar/'
    sed "$sed_cmd" build.gradle > tmp && mv tmp build.gradle
    # Uncomment and modify as needed:
    # sed '/ext { snakeYamlVersion.*/d' build.gradle > tmp && mv tmp build.gradle
    # sed "/^buildscript.*/a ext { snakeYamlVersion = \"$snakeYamlVersion\" }" build.gradle > tmp && mv tmp build.gradle
  else
    sed_cmd='s/logstash-core-*.*.*.jar/logstash-core.jar/'
    sed -i "$sed_cmd" build.gradle
    # Uncomment and modify as needed:
    # sed -i '/ext { snakeYamlVersion.*/d' build.gradle
    # sed -i '/^buildscript.*/a ext { snakeYamlVersion = '$snakeYamlVersion' }' build.gradle
  fi
}

# Builds a gem from the specified plugin directory.
buildUCPluginGem() {
  local plugin_dir="$1"

  echo "================ Building $plugin_dir gem file ================="
  cd "${BASE_DIR}/${plugin_dir}" || { echo "Failed to enter directory ${BASE_DIR}/${plugin_dir}"; exit 1; }

  adjustToLogstash8

  cp "${BASE_DIR}/build/gradle.properties" .

  if ./gradlew --no-daemon test </dev/null; then
    echo "Successfully tested $plugin_dir"
    if ./gradlew --no-daemon gem </dev/null; then
      echo "Successfully built gem $plugin_dir"
    else
      echo "Failed to build gem $plugin_dir"
      exit 1
    fi
  else
    echo "Failed to test $plugin_dir"
    exit 1
  fi
}

# Builds the UC Commons project.
buildUCCommons() {
  echo "================ Building UC Commons ================="
  cd "${BASE_DIR}/common" || { echo "Failed to enter directory ${BASE_DIR}/common"; exit 1; }

  if ./gradlew test; then
    echo "Successfully tested uc-commons"
  else
    echo "Failed to test uc-commons"
    exit 1
  fi

  if ./gradlew jar; then
    echo "Successfully built jar uc-commons"
  else
    echo "Failed to build jar uc-commons"
    exit 2
  fi

  cp ./build/libs/common-1.0.0.jar ./build/libs/guardium-universalconnector-commons-1.0.0.jar
  cd "${BASE_DIR}" || exit
}

# Builds Java plugins specified in the javaPluginsToBuild.txt file.
buildJavaPlugins() {
  echo "================ Building Java Plugins ================="
  while IFS= read -r plugin; do
    # Skip lines that are comments or empty
    [[ -z "$plugin" || "$plugin" =~ ^# ]] && continue
    buildUCPluginGem "$plugin"
  done < "${BASE_DIR}/build/javaPluginsToBuild.txt"
}

# Builds Ruby plugins specified in the rubyPluginsToBuild.txt file.
buildRubyPlugins() {
  echo "================ Building Ruby Plugins ================="
  while IFS= read -r plugin; do
    # Skip lines that are comments or empty
    [[ -z "$plugin" || "$plugin" =~ ^# ]] && continue
    buildRubyPlugin "$plugin" "${plugin##*/}.gemspec"
  done < "${BASE_DIR}/build/rubyPluginsToBuild.txt"
}

# Builds a Ruby plugin from the specified directory and gemspec.
buildRubyPlugin() {
  local plugin_dir="$1"
  local gemspec="$2"

  echo "================ Building Ruby Plugin in $plugin_dir ================="
  cd "${BASE_DIR}/${plugin_dir}" || { echo "Failed to enter directory ${BASE_DIR}/${plugin_dir}"; exit 1; }

  bundle install >/dev/null 2>&1
  if gem build "$gemspec"; then
    echo "Successfully built Ruby plugin with gemspec $gemspec"
  else
    echo "Failed to build Ruby plugin with gemspec $gemspec"
    exit 1
  fi
}

# Main script execution
echo "================ Starting Build Process ================="

ls -ld /usr/share/logstash
ls -ld /usr/share/logstash/universal-connectors
ls -ld /usr/share/logstash/universal-connectors/common

buildUCCommons
buildJavaPlugins
buildRubyPlugins

echo "================ Build Process Complete ================="

exit 0