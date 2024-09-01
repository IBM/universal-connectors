#!/bin/bash

# Constants
UC_IMAGE_NAME="guc_dit"
UC_TEST_CONTAINER_NAME="uc_test_container"
LOGSTASH_PLUGIN_LIST_FILE="logstash-plugin-list.txt"

LOGSTASH_DIR="/usr/share/logstash"
PACKAGED_PLUGINS_DIR="dist"
LOGSTASH_PLUGIN_CMD="${LOGSTASH_DIR}/bin/logstash-plugin"

MINIMUM_AMOUNT_OF_PLUGINS=20


# Function to test if a plugin exists and has the expected version
testPluginExistence() {
  local plugin_path="$1"
  local plugin_name="${plugin_path##*/}"
  plugin_name="${plugin_name%-*}"  # Remove version suffix

  local installed_version
  installed_version=$(grep "${plugin_name}" "${LOGSTASH_PLUGIN_LIST_FILE}" | cut -d ")" -f1 | cut -d "(" -f2 | xargs)

  local parent_dir
  parent_dir="$(dirname "$plugin_path")"
  local expected_version
  expected_version=$(cat "${parent_dir}/VERSION" | xargs)

  if [ "$expected_version" == "$installed_version" ]; then
    echo "Plugin $plugin_name exists in UC image. Version installed: $expected_version"
  elif [ -z "$installed_version" ]; then
    echo "Warning: Plugin ${plugin_name} is not installed in UC image"
  else
    echo "Warning: Installed version of plugin $plugin_name is $installed_version, expected version is $expected_version"
  fi
}

# Function to print the number of installed plugins and dependencies in a package
printNumOfInstalledPlugins() {
  local package_file="$1"

  local num_of_installed_plugins
  num_of_installed_plugins=$(docker exec -it "${UC_TEST_CONTAINER_NAME}" bash -c "unzip -l ${package_file} | grep -c gem")
  echo "Number of installed plugins in ${package_file}: ${num_of_installed_plugins}"

  if [[ "$num_of_installed_plugins" -lt ${MINIMUM_AMOUNT_OF_PLUGINS} ]]; then
    echo "Missing Guardium plugins. Please check if UC plugins were installed."
    exit 1
  fi

  local num_of_dependencies
  num_of_dependencies=$(docker exec -it "${UC_TEST_CONTAINER_NAME}" bash -c "unzip -l ${package_file} | grep dependencies | grep -c gem")
  echo "Of these, number of dependencies: ${num_of_dependencies}"
}

# Function to start a Docker container for testing
startTestContainer() {
  echo "Starting ${UC_TEST_CONTAINER_NAME} from image ${UC_IMAGE_NAME}"
  docker run -d --name="${UC_TEST_CONTAINER_NAME}" -it "${UC_IMAGE_NAME}" bash
}

# Function to stop and remove the test Docker container
removeTestContainer() {
  echo "Removing test container: ${UC_TEST_CONTAINER_NAME}"
  docker rm -f "${UC_TEST_CONTAINER_NAME}"
}

# Function to copy offline packages to the test container
copyPackagesToContainer() {
  local package_files=("$@")
  for package_file in "${package_files[@]}"; do
    echo "Copying ${package_file} to container..."
    docker cp "${PACKAGED_PLUGINS_DIR}/${package_file}" "${UC_TEST_CONTAINER_NAME}:${LOGSTASH_DIR}/."
  done
}

# Function to install a plugin package in the Logstash instance within the container
installPluginPackage() {
  local package_file="$1"
  echo "Installing plugin package ${package_file}..."
  docker exec -it "${UC_TEST_CONTAINER_NAME}" bash -c "${LOGSTASH_PLUGIN_CMD} install file:///${LOGSTASH_DIR}/${package_file}"
}

# Function to list installed plugins and save to a file
listInstalledPlugins() {
  echo "Listing installed plugins..."
  docker exec -it "${UC_TEST_CONTAINER_NAME}" bash -c "${LOGSTASH_PLUGIN_CMD} list --verbose &> ${LOGSTASH_PLUGIN_LIST_FILE}"
  docker cp "${UC_TEST_CONTAINER_NAME}:${LOGSTASH_DIR}/${LOGSTASH_PLUGIN_LIST_FILE}" .
}

# Function to check the contents of a package file
checkPackageContents() {
  local package_file="$1"
  echo "Contents of ${package_file}:"
  docker exec -it "${UC_TEST_CONTAINER_NAME}" bash -c "unzip -l ${package_file}"
}

# Main script logic

# Start the test container
startTestContainer

# Copy offline packages to the container
copyPackagesToContainer "uc_gdp_offline_package.zip" "uc_gi_offline_package.zip"

# Install and test the plugin packages
installPluginPackage "uc_gdp_offline_package.zip"
listInstalledPlugins

# Print the number of installed plugins
printNumOfInstalledPlugins "uc_gdp_offline_package.zip"
printNumOfInstalledPlugins "uc_gi_offline_package.zip"

# Verify plugin existence and versions
echo "Checking plugin versions..."
grep -v '^#' build/defaultOfflinePackagePlugins.txt | while read -r line; do testPluginExistence "${line}"; done

# Check contents of the offline package
checkPackageContents "uc_gi_offline_package.zip"

# Remove the test container
removeTestContainer

# Display the contents of the packaged plugins directory
echo "Contents of ${PACKAGED_PLUGINS_DIR}:"
ls -ltrh "${PACKAGED_PLUGINS_DIR}"
