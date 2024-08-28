#!/bin/bash

if [ "$TRAVIS_BRANCH" == "build-gdp-zip-on-travis" ] && $(git diff --name-only $TRAVIS_COMMIT_RANGE | grep -q "gdp-packages"); then
  echo "zipping GDP package templates";
  ./gdp-packages/zipPackagesForGDP.sh;
  export TAG=$(cat version);
  export UPLOAD_URL=$(curl -s "https://api.github.com/repos/${TRAVIS_REPO_SLUG}/releases/tags/$TAG" | jq -r '.upload_url' | sed -e "s/{?name,label}//");
  curl -X POST --data-binary "@gdp_plugins_templates.zip" -H "Authorization: token $GITHUB_TOKEN" -H "Content-Type: application/zip" "$UPLOAD_URL?name=gdp_plugins_templates.zip";
else
  echo "There was no change in gdp-packages on main branch."
fi