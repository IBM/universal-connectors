#!/bin/bash

LOGSTASH_PLUGIN_CMD=${LOGSTASH_DIR}/bin/logstash-plugin

function addPluginToInstallationList() {
  line=$1
  if [[ $1 =~ ^#.* ]]; then
    echo "Ignoring the commented out following line: $1"
  else
    pluginName=${1##*/}
    packages_list+=" ${pluginName%-*}"
  fi
}

function removeUninstalledPluginFromPackagingList() {
  installedPlugins=$(ls | grep gem)
  for value in $packages_list
  do
    if [[ ${installedPlugins} != *"${value}"* ]];then
      echo "${value} gem file was not found in the plugins directory. Removing it from default offline-package for UC"
      packages_list=${packages_list//$value/}
    fi
  done
}

function removePluginsFromGIPackagingList() {
  while read value; do
    packages_list=${packages_list//$value/}
  done < build/pluginsToRemoveFromGIOfflinePackage.txt
}

function prepareReleaseOfflinePackages() {
  echo "Packages included in UC default offline package for GDP: $packages_list"
  ${LOGSTASH_PLUGIN_CMD} prepare-offline-pack --output ./uc_gdp_offline_package.zip --overwrite $packages_list

  echo "Preparing separated offline packages for every built plugin..."
  for value in $packages_list
  do
    ${LOGSTASH_PLUGIN_CMD} prepare-offline-pack --output ./${value}.zip --overwrite ${value}
  done

  removePluginsFromGIPackagingList
  echo "Packages included in UC default offline package for GI: $packages_list"
  ${LOGSTASH_PLUGIN_CMD} prepare-offline-pack --output ./uc_gi_offline_package.zip --overwrite $packages_list
}

while read line; do addPluginToInstallationList $line; done <build/defaultOfflinePackagePlugins.txt

echo "Installing packages on Logstash..."
cd ${LOGSTASH_DIR}
${LOGSTASH_PLUGIN_CMD} install *.gem

removeUninstalledPluginFromPackagingList

prepareReleaseOfflinePackages

mkdir -p universal-connectors/dist
mv *.zip universal-connectors/dist/
