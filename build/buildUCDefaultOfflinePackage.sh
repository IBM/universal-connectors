#!/bin/bash

# Constants
LOGSTASH_DIR=/usr/share/logstash
PLUGIN_FILE="build/defaultOfflinePackagePlugins.txt"
DOCKER_CONTAINER_NAME="Alan"
DOCKER_IMAGE_NAME="guc_dit:latest"

# Function to update the "defaultOfflinePackagePlugins.txt" with the latest version for each plugin
function verify_plugins_version() {
  if [ -e "$PLUGIN_FILE" ]; then
    echo "Updating plugin file names with the latest version in the defaultOfflinePackagePlugins.txt file"

    # Read each line in the file and process it
    grep -v '^#' "$PLUGIN_FILE" | while read -r line; do
      plugin_name=$(basename "${line}")
      plugin_location=$(dirname "${line}")
      version_file_path="$plugin_location/VERSION"

      # Read the current version from the VERSION file
      if [ -f "$version_file_path" ]; then
        version=$(cat "${version_file_path}")
        new_line="$plugin_location/$plugin_name-$version.gem"

        # Update the plugin file with the new version using `sed`
        if [[ $OSTYPE == 'darwin'* ]]; then
          sed -i '' "s|$line|$new_line|g" "$PLUGIN_FILE"
        else
          sed -i "s|$line|$new_line|g" "$PLUGIN_FILE"
        fi
      else
        echo "Version file not found for plugin: $plugin_name"
      fi
    done
  else
    echo "Plugin file not found: $PLUGIN_FILE"
  fi
}

# Function to build Docker image
function build_docker_image() {
  echo "Building Docker image..."
  cd build || exit
  docker build --build-arg DOCKER_REPO="${DOCKER_REPO}" --build-arg IMAGE_TAG="${IMAGE_TAG}" -qt "${DOCKER_IMAGE_NAME}" .
  cd .. || exit
}

# Function to run Docker container
function run_docker_container() {
  echo "Running Docker container..."
  docker run --name="${DOCKER_CONTAINER_NAME}" -v "$(pwd)/:${LOGSTASH_DIR}/universal-connectors/" -dit "${DOCKER_IMAGE_NAME}" bash
}

# Function to build plugin gems inside the Docker container
function build_plugin_gems() {
  echo "Building plugin gems in Docker container..."
  docker exec "${DOCKER_CONTAINER_NAME}" bash -c "cd universal-connectors && ./build/buildUCPluginGems.sh"
  if [ $? -eq 0 ]; then
    echo "Successfully tested and built plugins."
  else
    echo "Failed to test and build plugins."
    exit 1
  fi
}

# Function to prepare the default offline package
function prepare_offline_package() {
  echo "Preparing default offline package..."
  grep -v '^#' "$PLUGIN_FILE" | while read -r line; do
    docker cp "${line}" "${DOCKER_CONTAINER_NAME}:${LOGSTASH_DIR}/."
  done
  docker exec "${DOCKER_CONTAINER_NAME}" bash -c "cd universal-connectors && ./build/prepareUCDefaultOfflinePackage.sh"
}

# Function to list files in the 'dist' directory
function list_dist_files() {
  echo "Listing files in the 'dist' directory:"
  find dist -type f
}

# Main Script Execution

build_docker_image
verify_plugins_version

echo "Final list in defaultOfflinePackagePlugins.txt:"
cat "$PLUGIN_FILE"

run_docker_container
build_plugin_gems
prepare_offline_package
list_dist_files
