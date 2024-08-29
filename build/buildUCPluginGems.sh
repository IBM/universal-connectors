#!/bin/bash

BASE_DIR=$(pwd)
IS_MAC_M1_BUILD=false  # Set to true for Mac M1 builds
export GRADLE_OPTS="-Dorg.gradle.daemon=true"

# Updates build.gradle to use logstash-core.jar instead of versioned logstash-core JARs.
adjustToLogstash8() {
  local sed_cmd

  # Determine the sed command based on the platform
  if [ "$IS_MAC_M1_BUILD" = false ]; then
    sed_cmd='s/logstash-core-.*\.jar/logstash-core.jar/'
    sed "$sed_cmd" build.gradle > tmp && mv tmp build.gradle
  else
    sed_cmd='s/logstash-core-*.*.*.jar/logstash-core.jar/'
    sed -i "$sed_cmd" build.gradle
  fi
}

# Builds a gem from the specified plugin directory.
buildUCPluginGem() {
  local plugin_dir="$1"

  echo "================ Building $plugin_dir gem file ================="
  cd "${BASE_DIR}/${plugin_dir}" || { echo "Failed to enter directory ${BASE_DIR}/${plugin_dir}"; exit 1; }

  adjustToLogstash8

  cp "${BASE_DIR}/build/gradle.properties" .

  ./gradlew --no-daemon test </dev/null >/dev/null 2>&1
  TEST_STATUS=$?

  ./gradlew --no-daemon gem </dev/null >/dev/null 2>&1
  GEM_STATUS=$?

  if [ $TEST_STATUS -eq 0 ] && [ $GEM_STATUS -eq 0 ]; then
    echo "Successfully tested and built gem $plugin_dir"
  else
    echo "Failed to test or build gem $plugin_dir"
    exit 1
  fi
}

# Builds the UC Commons project.
buildUCCommons() {
  echo "================ Building UC Commons ================="
  cd "${BASE_DIR}/common" || { echo "Failed to enter directory ${BASE_DIR}/common"; exit 1; }

  if ./gradlew test </dev/null >/dev/null 2>&1; then
    echo "Successfully tested uc-commons"
  else
    echo "Failed to test uc-commons"
    exit 1
  fi

  if ./gradlew jar </dev/null >/dev/null 2>&1; then
    echo "Successfully built jar uc-commons"
  else
    echo "Failed to build jar uc-commons"
    exit 2
  fi

  cp ./build/libs/common-1.0.0.jar ./build/libs/guardium-universalconnector-commons-1.0.0.jar
  cd "${BASE_DIR}" || exit
}

# Builds Java plugins specified in the javaPluginsToBuild.txt file in parallel.
buildJavaPlugins() {
  echo "================ Building Java Plugins in Parallel ================="
  local pids=()
  while IFS= read -r plugin; do
    [[ -z "$plugin" || "$plugin" =~ ^# ]] && continue  # Skip comments or empty lines
    buildUCPluginGem "$plugin" &
    pids+=($!)  # Capture the PID of the background process
  done < "${BASE_DIR}/build/javaPluginsToBuild.txt"

  # Wait for all background jobs to finish
  for pid in "${pids[@]}"; do
    wait "$pid" || { echo "Build failed for one or more Java plugins"; exit 1; }
  done
}

# Builds Ruby plugins specified in the rubyPluginsToBuild.txt file in parallel.
buildRubyPlugins() {
  echo "================ Building Ruby Plugins in Parallel ================="
  local pids=()
  while IFS= read -r plugin; do
    [[ -z "$plugin" || "$plugin" =~ ^# ]] && continue  # Skip comments or empty lines
    buildRubyPlugin "$plugin" "${plugin##*/}.gemspec" &
    pids+=($!)  # Capture the PID of the background process
  done < "${BASE_DIR}/build/rubyPluginsToBuild.txt"

  # Wait for all background jobs to finish
  for pid in "${pids[@]}"; do
    wait "$pid" || { echo "Build failed for one or more Ruby plugins"; exit 1; }
  done
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

buildUCCommons

# Run Java and Ruby plugin builds in parallel
buildJavaPlugins &
buildRubyPlugins &
wait  # Wait for all parallel builds to complete

echo "================ Build Process Complete ================="

exit 0
