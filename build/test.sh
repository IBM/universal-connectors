#!/bin/bash

UC_IMAGE_NAME="guc_dit"
UC_TEST_CONTAINER_NAME="uc_test_container"
LOGSTASH_PLUGIN_LIST_FILE=logstash-plugin-list.txt

LOGSTASH_DIR=/usr/share/logstash
PACKAGED_PLUGINS_DIR=dist
UC_PLUGINS_BUILD_FOLDER=/build

function testPluginExistence() {
  pluginName=${1##*/}
  pluginName=${pluginName%-*} # removed version

  installedVersion=$(grep "${pluginName}" ${LOGSTASH_PLUGIN_LIST_FILE} | cut -d ")" -f1 | cut -d "(" -f2 | xargs)

  parentDir="$(dirname "${UC_OPENSOURCE_ROOT_DIR}/$1")"
  expectedVersion=$(cat ${parentDir}/VERSION | xargs)

  if [ "$expectedVersion" = "$installedVersion" ]; then
    echo "Plugin $pluginName exists in UC image. Version installed: $expectedVersion"
  elif [ -z "$installedVersion" ]; then
    echo "Warning: Plugin ${pluginName} is not installed in UC image"
  else
    echo "Warning: installed version of plugin $pluginName is $installedVersion and the version stated in Github ${parentDir}/VERSION file is $expectedVersion"
  fi
}

function printNumOfInstalledPlugin() {
  numOfInstalledGems=$(docker exec -it ${UC_TEST_CONTAINER_NAME} bash -c "unzip -l $1 | grep -c gem")
  echo "Num of installed plugins in $1: $numOfInstalledGems"
  numOfDeps=$(docker exec -it ${UC_TEST_CONTAINER_NAME} bash -c "unzip -l $1 | grep dependencies | grep -c gem")
  echo "Of of them, num of dependencies: $numOfDeps"
}

echo "test uc_gdp_offline_package"
echo "running ${UC_TEST_CONTAINER_NAME} from ${UC_IMAGE_NAME}"
docker run -d --name=${UC_TEST_CONTAINER_NAME} -it ${UC_IMAGE_NAME} bash

docker cp ${PACKAGED_PLUGINS_DIR}/uc_gdp_offline_package.zip ${UC_TEST_CONTAINER_NAME}:${LOGSTASH_DIR}/.
docker cp ${PACKAGED_PLUGINS_DIR}/uc_gi_offline_package.zip ${UC_TEST_CONTAINER_NAME}:${LOGSTASH_DIR}/.
docker exec -it ${UC_TEST_CONTAINER_NAME} bash -c "logstash-plugin install file:///${LOGSTASH_DIR}/uc_gdp_offline_package.zip"

# Print logstash-plugin list
docker exec -it ${UC_TEST_CONTAINER_NAME} bash -c "logstash-plugin list --verbose &> ${LOGSTASH_PLUGIN_LIST_FILE}"
docker cp ${UC_TEST_CONTAINER_NAME}:${LOGSTASH_DIR}/${LOGSTASH_PLUGIN_LIST_FILE} .

printNumOfInstalledPlugin uc_gdp_offline_package.zip
printNumOfInstalledPlugin uc_gi_offline_package.zip

# Check plugin existence and version in UC image
grep -v '^#' build/defaultOfflinePackagePlugins.txt | while read -r line; do testPluginExistence "${line}"; done

# Check content of uc_gi_offline_package
echo "uc_gi_offline_package content:"
docker exec -it  ${UC_TEST_CONTAINER_NAME} bash -c "unzip -l uc_gi_offline_package.zip"

# remove test container
docker rm -f ${UC_TEST_CONTAINER_NAME}

echo "content of ${PACKAGED_PLUGINS_DIR}:"
ls -ltrh ${PACKAGED_PLUGINS_DIR}