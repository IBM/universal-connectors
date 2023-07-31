#!/bin/bash
#--------------------------------------------------------------------------------
# Copyright 2020-2021 IBM Inc. All rights reserved
# SPDX-License-Identifier: Apache2.0
#---------------------------------------------------------------------------------
#---------------------------------------------------------------------------------

VERIFIED_UC_GDP_PLUGINS_FILE="verifiedUCPlugins_gdp.txt"
VERIFIED_UC_GI_PLUGINS_FILE="verifiedUCPlugins_gi.txt"

#The full list with the offline packages names
VERIFIED_FULL_PLUGINS_LIST="verified_UC_plugins_full_list.txt"

PACKAGED_PLUGINS_DIR=dist/plugins
GI_PLUGINS_DIR=dist/temp/GI
GDP_PLUGINS_DIR=dist/temp/GDP

PLUGINS_LISTS_DIR=build/


GI_PLUGINS_TEMPLATES="gi_plugins_templates.zip"
GDP_PLUGINS_TEMPLATES="gdp_plugins_templates.zip"
originalPath=$(pwd)

function zipPackage {
  IFS=';' read -r package_folder offline_plugin <<< "$1"

  echo "package_folder is: $package_folder"
  if [ -n "$offline_plugin" ]; then
    echo "offline_plugin is: $offline_plugin"
  fi


  zipName="$(echo $package_folder | awk -F'/' '{print $NF}')"
  packageFolder=$(dirname $package_folder)
  targetDirectory=$2

  cd "${originalPath}/${packageFolder}"
  echo "location of folder to package: $(pwd)"
  echo "folder to package: ${zipName}"
  zip -r "${originalPath}/${targetDirectory}/${zipName}.zip" $zipName -x "*.zip"

  # if there is also offline plugins, enter it to the zip:
  if [ -e "$originalPath/build/offline_packages/zips/$offline_plugin" ]; then
      echo "The file $offline_plugin exists."

      zip -uj "${originalPath}/${targetDirectory}/${zipName}.zip" $originalPath/build/offline_packages/zips/$offline_plugin
  fi

  cd "$originalPath"
}

mkdir -p ${PACKAGED_PLUGINS_DIR};
mkdir -p ${GI_PLUGINS_DIR}
mkdir -p ${GDP_PLUGINS_DIR}


# go to the build direction and zip all the plug-ins packages includes the offline-package plugin (if exist)
cd ${PLUGINS_LISTS_DIR}
grep -v '^#' ${VERIFIED_FULL_PLUGINS_LIST} | while read -r line ; do zipPackage "$line" "${PACKAGED_PLUGINS_DIR}" ; done


#zip the GDP plugins templates to a temp location
grep -v '^#' ${VERIFIED_UC_GDP_PLUGINS_FILE} | while read -r line ; do zipPackage "$line" "${GDP_PLUGINS_DIR}" ; done

#zip the GI plugins templates to a temp location
grep -v '^#' ${VERIFIED_UC_GI_PLUGINS_FILE} | while read -r line ; do zipPackage "$line" "${GI_PLUGINS_DIR}" ; done

# list all GI plugins into a file
cd ${originalPath}/${GI_PLUGINS_DIR}
ls | grep -v '/$' | grep -v 'plugins_list.txt' > "plugins_list.txt"


#zip all plugins zips (the templates only, not the offline plugin zip) and the names list into one zip
cd ${originalPath}
zip -j -r "${GDP_PLUGINS_TEMPLATES}" "${GDP_PLUGINS_DIR}"
zip -j -r "${GI_PLUGINS_TEMPLATES}" "${GI_PLUGINS_DIR}"


# remove the temporary directions
rm -r ${originalPath}/${GI_PLUGINS_DIR}
rm -r ${originalPath}/${GDP_PLUGINS_DIR}