#!/bin/bash
#--------------------------------------------------------------------------------
# Copyright 2020-2021 IBM Inc. All rights reserved
# SPDX-License-Identifier: Apache2.0
#---------------------------------------------------------------------------------
#---------------------------------------------------------------------------------

VERIFIED_UC_PLUGINS_FILE="verifiedUCPlugins.txt"
PACKAGED_PLUGINS_DIR=dist/plugins

function zipPackage {
  zipName="$(echo $1 | awk -F"\/" '{print $NF}')"
  packageFolder=$(dirname $1)
  originalPath=$(pwd)

  cd $packageFolder
  echo "location to folder to package: $(pwd)"
  echo "folder to package: ${zipName}"
  zip -r "${originalPath}/${PACKAGED_PLUGINS_DIR}/${zipName}.zip" $zipName -x ".*" -x "__MACOSX"
  cd $originalPath
}

mkdir -p ${PACKAGED_PLUGINS_DIR};
grep -v '^#' $VERIFIED_UC_PLUGINS_FILE | while read -r line ; do zipPackage "$line" ; done