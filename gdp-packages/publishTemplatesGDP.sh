#!/bin/bash

if [ "$TRAVIS_BRANCH" == "build-gdp-zip-on-travis" ] && $(git diff --name-only $TRAVIS_COMMIT_RANGE | grep -q "gdp-packages"); then
  echo "zipping GDP package templates";
  ./gdp-packages/zipPackagesForGDP.sh;
  export TAG=$(cat "./gdp-packages/version");
  export UPLOAD_URL=$(curl -s "https://api.github.com/repos/${TRAVIS_REPO_SLUG}/releases/tags/$TAG" | jq -r '.upload_url' | sed -e "s/{?name,label}//");
  response=$(curl -s -o response.json -w "%{http_code}" -X POST --data-binary "@gdp-packages/gdp_plugins_templates.zip" -H "Authorization: token $GITHUB_TOKEN" -H "Content-Type: application/zip" "$UPLOAD_URL?name=gdp_plugins_templates.zip")
  # Parse the response JSON file to check for errors
  if [[ "$response" -ne 200 ]]; then
      error_message=$(jq -r '.message' response.json)
      echo "Failed to publish gdp templates zip to release: $TAG due to error: $error_message"
      exit 1
  fi

  echo "Uploaded gdp templates zip to release: $TAG successfully"
  exit 0
else
  echo "There was no change in gdp-packages on main branch."
fi