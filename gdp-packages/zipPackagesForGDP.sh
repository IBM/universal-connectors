#!/bin/bash

cd "gdp-packages"
TEMPLATES_DIR="gdp_plugins_templates"

zip_directories() {
    local base_dir="$1"
    cd "$base_dir"
    for dir in ./*/; do
        dir_name=$(basename "$dir")
        zip -r "${dir_name}.zip" "$dir" -x ".*"
    done
    cd ..
}

zip_profile() {
    cd "profile"
    local dir_name="$1"
    zip -r "${dir_name}.zip" "${dir_name}" -x ".*"
    cd ..
}

mkdir $TEMPLATES_DIR

zip_directories credentials
zip_directories input
while IFS= read -r line; do
    zip_profile "$line"
done < "preinstalled.txt"

mv credentials/*.zip "$TEMPLATES_DIR"
mv input/*.zip "$TEMPLATES_DIR"
mv profile/*.zip "$TEMPLATES_DIR"
cd "$TEMPLATES_DIR"
zip -r "$TEMPLATES_DIR" . -x ".*"
mv gdp_plugins_templates.zip ../.
cd ..

# publish zip into pre release
if [ "$TRAVIS_BRANCH" == "build-gdp-zip-on-travis" ]; then
  export TAG=$(cat version);
  export UPLOAD_URL=$(curl -s "https://api.github.com/repos/${TRAVIS_REPO_SLUG}/releases/tags/$TAG" | jq -r '.upload_url' | sed -e "s/{?name,label}//");
  curl -X POST --data-binary "@gdp_plugins_templates.zip" -H "Authorization: token $GITHUB_TOKEN" -H "Content-Type: application/zip" "$UPLOAD_URL?name=gdp_plugins_templates.zip";
fi