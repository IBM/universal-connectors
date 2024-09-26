#!/bin/bash

# Constants
LOGSTASH_PLUGIN_CMD="${LOGSTASH_DIR}/bin/logstash-plugin"
PACKAGES_LIST=""

# Adds a plugin to the installation list if it is not commented out.
add_plugin_to_installation_list() {
  local line=$1

  if [[ $line =~ ^# ]]; then
    echo "Ignoring commented line: $line"
  else
    local plugin_name=${line##*/}
    PACKAGES_LIST+=" ${plugin_name%-*}"
  fi
}

# Removes plugins from the packaging list that are not found in the directory.
remove_uninstalled_plugins() {
  local installed_plugins
  installed_plugins=$(ls | grep -E '\.gem$')

  for plugin in $PACKAGES_LIST; do
    if [[ ! $installed_plugins == *"${plugin}"* ]]; then
      echo "${plugin} gem file not found. Removing from packaging list."
      PACKAGES_LIST=${PACKAGES_LIST// $plugin/}
    fi
  done
}

# Removes plugins listed in build/pluginsToRemoveFromGIOfflinePackage.txt from the packaging list.
remove_plugins_from_gi_list() {
  while read -r plugin; do
    PACKAGES_LIST=${PACKAGES_LIST// $plugin/}
  done < build/pluginsToRemoveFromGIOfflinePackage.txt
}

# Prepares offline packages for UC and GI.
prepare_offline_packages() {
  echo "Creating UC default offline package for GDP with packages: $PACKAGES_LIST"
  ${LOGSTASH_PLUGIN_CMD} prepare-offline-pack --output ./uc_gdp_offline_package.zip --overwrite $PACKAGES_LIST

  echo "Creating separated offline packages for each plugin..."
  for plugin in $PACKAGES_LIST; do
    ${LOGSTASH_PLUGIN_CMD} prepare-offline-pack --output ./${plugin}.zip --overwrite ${plugin}
  done

  remove_plugins_from_gi_list
  echo "Creating UC default offline package for GI with packages: $PACKAGES_LIST"
  ${LOGSTASH_PLUGIN_CMD} prepare-offline-pack --output ./uc_gi_offline_package.zip --overwrite $PACKAGES_LIST
}

# Main script execution
# Build the list of plugins to install
while read -r line; do
  add_plugin_to_installation_list "$line"
done < build/defaultOfflinePackagePlugins.txt

echo "Installing packages on Logstash..."
cd "${LOGSTASH_DIR}" || exit
${LOGSTASH_PLUGIN_CMD} install *.gem

remove_uninstalled_plugins
prepare_offline_packages

# Move zip files to the dist directory
mkdir -p universal-connectors/dist
mv *.zip universal-connectors/dist/

