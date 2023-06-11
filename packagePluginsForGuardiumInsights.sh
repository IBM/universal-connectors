#!/bin/bash
#--------------------------------------------------------------------------------
# Copyright 2020-2021 IBM Inc. All rights reserved
# SPDX-License-Identifier: Apache2.0
#---------------------------------------------------------------------------------
#---------------------------------------------------------------------------------

VERIFIED_UC_PLUGINS_FILE="verifiedUCPlugins.txt"
PACKAGED_PLUGINS_DIR=dist/plugins
originalPath=$(pwd)

function zipPackage {
  zipName="$(echo $1 | awk -F"\/" '{print $NF}')"
  packageFolder=$(dirname $1)

  cd $packageFolder
  echo "location to folder to package: $(pwd)"
  echo "folder to package: ${zipName}"
  zip -r "${originalPath}/${PACKAGED_PLUGINS_DIR}/${zipName}.zip" $zipName -x "*.zip"
  cd $originalPath
}

mkdir -p ${PACKAGED_PLUGINS_DIR};
grep -v '^#' $VERIFIED_UC_PLUGINS_FILE | while read -r line ; do zipPackage "$line" ; done

# list all plugins into a file
cd ${PACKAGED_PLUGINS_DIR}
ls | grep -v '/$' | grep -v 'plugins_list.txt' > "plugins_list.txt"

# zip all plugins zips and the names list into one zip
cd $originalPath
zip -j -r "plugins.zip" "${PACKAGED_PLUGINS_DIR}"