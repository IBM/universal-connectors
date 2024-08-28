#!/bin/bash

if [ "$TRAVIS_BRANCH" == "main" ] && $(git diff --name-only $TRAVIS_COMMIT_RANGE | grep -q "gdp-packages"); then
  echo "zipping GDP package templates"
  ./gdp-packages/zipPackagesForGDP.sh

  export TAG=$(cat "./gdp-packages/version")

  # Delete the previous version of the release
  # Step 1: get the asset id for gdp_plugins_templates.zip
  RELEASE_INFO=$(curl -s -H "Authorization: token $GITHUB_TOKEN" \
    "https://api.github.com/repos/${TRAVIS_REPO_SLUG}/releases/tags/${TAG}")
  RELEASE_ID=$(echo "$RELEASE_INFO" | jq -r '.id')
  ASSET_LIST=$(curl -s -H "Authorization: token $GITHUB_TOKEN" \
      "https://api.github.com/repos/${TRAVIS_REPO_SLUG}/releases/${RELEASE_ID}/assets")
  ASSET_ID=$(echo "$ASSET_LIST" | jq -r '.[] | select(.name == "gdp_plugins_templates.zip") | .id')

  # Step 2: Delete the existing asset gdp_plugins_templates.zip by asset id
  if [ -n "$ASSET_ID" ]; then
    curl -s -X DELETE -H "Authorization: token $GITHUB_TOKEN" \
         "https://api.github.com/repos/${TRAVIS_REPO_SLUG}/releases/assets/$ASSET_ID"
  fi

  # Publish the new gdp_plugins_templates.zip to release
  export UPLOAD_URL=$(curl -s "https://api.github.com/repos/${TRAVIS_REPO_SLUG}/releases/tags/$TAG" | jq -r '.upload_url' | sed -e "s/{?name,label}//")
  echo "Uploading gdp-packages/gdp_plugins_templates.zip into release: $TAG, url: $UPLOAD_URL"
  response=$(curl -s -o response.json -w "%{http_code}" -X POST --data-binary "@gdp-packages/gdp_plugins_templates.zip" -H "Authorization: token $GITHUB_TOKEN" -H "Content-Type: application/zip" "$UPLOAD_URL?name=gdp_plugins_templates.zip")
  # Parse the response JSON file to check for errors
  if [[ "$response" -ne 200 && "$response" -ne 201 ]]; then
      error_message=$(jq -r '.message' response.json)
      echo "Failed to publish gdp templates zip to release: $TAG due to error: $error_message"
      exit 1
  fi
  echo "Uploaded gdp templates zip to release: $TAG successfully"
  exit 0
else
  echo "There was no change in gdp-packages on main branch."
fi