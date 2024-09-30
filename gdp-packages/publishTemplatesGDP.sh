#!/bin/bash

if [ "$TRAVIS_BRANCH" == "main" ] && $(git diff --name-only $TRAVIS_COMMIT_RANGE | grep -q "gdp-packages"); then
  echo "zipping GDP package templates"
  ./gdp-packages/zipPackagesForGDP.sh
  export COMMIT_SHA=${TRAVIS_COMMIT}
  # Get current timestamp
  TIMESTAMP=$(date +%Y-%m-%d-%H-%M)
  # Create the tag name by combining timestamp and commit hash
  export TAG="packages-v${TIMESTAMP}_${COMMIT_SHA:0:7}"

  # Create the tag using the GitHub API
  curl -X POST -H "Authorization: token $GITHUB_TOKEN" \
  -H "Accept: application/vnd.github.v3+json" \
  https://api.github.com/repos/${TRAVIS_REPO_SLUG}/git/refs \
  -d '{
    "ref": "refs/tags/'"$TAG"'",
    "sha": "'"$COMMIT_SHA"'"
  }'

  echo "Tag $TAG created for commit $COMMIT_SHA"

  # Create a release from the tag using the GitHub API
  curl -X POST -H "Authorization: token $GITHUB_TOKEN" \
  -H "Accept: application/vnd.github.v3+json" \
  https://api.github.com/repos/${TRAVIS_REPO_SLUG}/releases \
  -d '{
    "tag_name": "'"$TAG"'",
    "name": "'"$TAG"'",
    "body": "gdp packages for CM management",
    "draft": false,
    "prerelease":true
  }'
  echo "Draft release $TAG created"

  # Publish the new gdp_plugins_templates.zip to release
  export UPLOAD_URL=$(curl -H "Authorization: token $GITHUB_TOKEN" -s "https://api.github.com/repos/${TRAVIS_REPO_SLUG}/releases/tags/$TAG" | jq -r '.upload_url' | sed -e "s/{?name,label}//")
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