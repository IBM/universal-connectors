#!/bin/bash

LOGSTASH_DIR=/usr/share/logstash

# Update the "defaultOfflinePackagePlugins.txt" by adding the latest version to each plugin file name
function verify_plugins_version() {
  if [ -e build/defaultOfflinePackagePlugins.txt ]
  then
      echo "Updating plugin file names with the latest version in the defaultOfflinePackagePlugins.txt file"
      grep -v '^#' build/defaultOfflinePackagePlugins.txt | while read -r line; do
      plugin_name=$(basename "${line}")
      plugin_location=$(dirname "${line}")

      # Read the current version from the VERSION file and store it in a variable
      version_file_path="$plugin_location/VERSION"
      version=$(cat "${version_file_path}")

      new_line="$plugin_location/$plugin_name-$version.gem"
      if [[ $OSTYPE == 'darwin'* ]]; then
        sed -i '' "s|$line|$new_line|g" build/defaultOfflinePackagePlugins.txt
      else
        sed -i "s|$line|$new_line|g" build/defaultOfflinePackagePlugins.txt
      fi

  done
  fi
}

cd build
docker build --build-arg DOCKER_REPO=${DOCKER_REPO} --build-arg IMAGE_TAG=${IMAGE_TAG} -qt guc_dit:latest .
cd ..

verify_plugins_version

echo "Final list in defaultOfflinePackagePlugins.txt:"
cat build/defaultOfflinePackagePlugins.txt

# Run UC build container
docker run --name="Alan" -v $(pwd)/:${LOGSTASH_DIR}/universal-connectors/ -dit guc_dit:latest bash

# Build plugin gems
docker exec Alan bash -c "cd universal-connectors && ./build/buildUCPluginGems.sh"
if [ $? -eq 0 ]
then
  echo "Successfully tested and built"
else
  echo "Failed to test and build plugins"
  exit 1
fi


# Prepare default offline package
grep -v '^#' build/defaultOfflinePackagePlugins.txt | while read -r line; do docker cp "${line}" Alan:/${LOGSTASH_DIR}/.; done
docker exec Alan bash -c "cd universal-connectors && ./build/prepareUCDefaultOfflinePackage.sh"

find dist -type f