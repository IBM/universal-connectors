# Builds a gem from the specified plugin directory.
buildUCPluginGem() {
  local plugin_dir="$1"

  # Remove any quotes from plugin_dir to prevent path errors
  plugin_dir="${plugin_dir//\"/}"

  echo "================ Building $plugin_dir gem file ================="
  cd "${BASE_DIR}/${plugin_dir}" || { echo "Failed to enter directory ${BASE_DIR}/${plugin_dir}"; exit 1; }

  adjustToLogstash8

  cp "${BASE_DIR}/build/gradle.properties" .

  # Adjust Gradle options for low resource environments
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
