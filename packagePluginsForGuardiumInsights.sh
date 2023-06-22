#!/bin/bash
#--------------------------------------------------------------------------------
# Copyright 2020-2021 IBM Inc. All rights reserved
# SPDX-License-Identifier: Apache2.0
#---------------------------------------------------------------------------------
#---------------------------------------------------------------------------------

VERIFIED_UC_GDP_PLUGINS_FILE="verifiedUCPlugins_gdp.txt"
VERIFIED_UC_GI_PLUGINS_FILE="verifiedUCPlugins_gi.txt"

PACKAGED_PLUGINS_DIR=dist/plugins
GI_PLUGINS_DIR=dist/temp
PLUGINS_LISTS_DIR=build/

GI_PLUGINS_TEMPLATES="gi_plugins_templates.zip"
GDP_PLUGINS_TEMPLATES="gdp_plugins_templates.zip"
originalPath=$(pwd)

function zipPackage {
  zipName="$(echo $1 | awk -F'/' '{print $NF}')"
  packageFolder=$(dirname $1)
  targetDirectory=$2

  cd "$packageFolder"
  echo "location of folder to package: $(pwd)"
  echo "folder to package: ${zipName}"
  zip -r "${originalPath}/${targetDirectory}/${zipName}.zip" $zipName -x "*.zip"
  cd "$originalPath"
}

mkdir -p ${PACKAGED_PLUGINS_DIR};
mkdir -p ${GI_PLUGINS_DIR}

# go to the build direction and zip all the plug-ins packages (the GDP plugins list includes the both GDP+GI plugins)
cd ${PLUGINS_LISTS_DIR}
grep -v '^#' ${VERIFIED_UC_GDP_PLUGINS_FILE} | while read -r line ; do zipPackage "$line" "${PACKAGED_PLUGINS_DIR}"; done

# zip the GI plugins to a temp location
grep -v '^#' ${VERIFIED_UC_GI_PLUGINS_FILE} | while read -r line ; do zipPackage "$line" "${GI_PLUGINS_DIR}"; done

# list all GI plugins into a file
cd ${originalPath}/${GI_PLUGINS_DIR}
ls | grep -v '/$' | grep -v 'plugins_list.txt' > "plugins_list.txt"


# zip all plugins zips and the names list into one zip
cd ${originalPath}
zip -j -r "${GDP_PLUGINS_TEMPLATES}" "${PACKAGED_PLUGINS_DIR}"
zip -j -r "${GI_PLUGINS_TEMPLATES}" "${GI_PLUGINS_DIR}"

# remove temporary direction
rm -r ${originalPath}/${GI_PLUGINS_DIR}

