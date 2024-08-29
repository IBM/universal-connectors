#!/bin/bash
#--------------------------------------------------------------------------------
# Copyright 2020-2021 IBM Inc. All rights reserved
# SPDX-License-Identifier: Apache2.0
#---------------------------------------------------------------------------------

# Constants
VERIFIED_UC_GDP_PLUGINS_FILE="verifiedUCPlugins_gdp.txt"
VERIFIED_UC_GI_PLUGINS_FILE="verifiedUCPlugins_gi.txt"
VERIFIED_FULL_PLUGINS_LIST="verified_UC_plugins_full_list.txt"

PACKAGED_PLUGINS_DIR="dist/plugins"
GI_PLUGINS_DIR="dist/temp/GI"
GDP_PLUGINS_DIR="dist/temp/GDP"
PLUGINS_LISTS_DIR="build"

GI_PLUGINS_TEMPLATES="gi_plugins_templates.zip"
GDP_PLUGINS_TEMPLATES="gdp_plugins_templates.zip"
originalPath=$(pwd)

# Function to zip a package along with optional offline plugins
zip_package() {
  IFS=';' read -r package_folder offline_plugin <<< "$1"

  echo "Packaging folder: $package_folder"
  [ -n "$offline_plugin" ] && echo "Including offline plugin: $offline_plugin"

  zip_name=$(basename "$package_folder")
  package_dir=$(dirname "$package_folder")
  target_directory="$2"

  cd "${originalPath}/${package_dir}" || { echo "Failed to enter directory: ${package_dir}"; exit 1; }
  echo "Current directory: $(pwd)"
  echo "Zipping folder: ${zip_name}"

  zip -r "${originalPath}/${target_directory}/${zip_name}.zip" "$zip_name" -x "*.zip"

  # If there is an offline plugin, include it in the zip
  offline_plugin_path="$originalPath/build/offline_packages/zips/$offline_plugin"
  if [ -e "$offline_plugin_path" ]; then
    echo "Adding offline plugin: $offline_plugin to ${zip_name}.zip"
    zip -uj "${originalPath}/${target_directory}/${zip_name}.zip" "$offline_plugin_path"
  fi

  cd "$originalPath" || { echo "Failed to return to original directory"; exit 1; }
}

# Create necessary directories
mkdir -p "${PACKAGED_PLUGINS_DIR}" "${GI_PLUGINS_DIR}" "${GDP_PLUGINS_DIR}"

# Zip all plugin packages, including offline-package plugins if they exist
cd "${PLUGINS_LISTS_DIR}" || { echo "Failed to enter directory: ${PLUGINS_LISTS_DIR}"; exit 1; }
grep -v '^#' "${VERIFIED_FULL_PLUGINS_LIST}" | while read -r line; do
  zip_package "$line" "${PACKAGED_PLUGINS_DIR}"
done

# Zip GDP plugins templates to a temporary location
grep -v '^#' "${VERIFIED_UC_GDP_PLUGINS_FILE}" | while read -r line; do
  zip_package "$line" "${GDP_PLUGINS_DIR}"
done

# Zip GI plugins templates to a temporary location
grep -v '^#' "${VERIFIED_UC_GI_PLUGINS_FILE}" | while read -r line; do
  zip_package "$line" "${GI_PLUGINS_DIR}"
done

# List all GI plugins into a file
cd "${originalPath}/${GI_PLUGINS_DIR}" || { echo "Failed to enter directory: ${GI_PLUGINS_DIR}"; exit 1; }
ls | grep -v '/$' | grep -v 'plugins_list.txt' > "plugins_list.txt"

# Zip all plugin zips (the templates only, not the offline plugin zip) and the names list into one zip
cd "${originalPath}" || { echo "Failed to return to original directory"; exit 1; }
zip -j -r "${GDP_PLUGINS_TEMPLATES}" "${GDP_PLUGINS_DIR}"
zip -j -r "${GI_PLUGINS_TEMPLATES}" "${GI_PLUGINS_DIR}"

# Remove the temporary directories
rm -r "${GI_PLUGINS_DIR}" "${GDP_PLUGINS_DIR}"
