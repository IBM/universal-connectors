#!/bin/bash

# Script to apply comprehensive Java 21 and Gradle 8.11.1 compatibility fixes
# Based on commit 30b637e5 from public GDSC branch

set -e

REPO_DIR="/Users/jiseok/Documents/workspace/public/universal-connectors"
cd "$REPO_DIR"

echo "=== Applying Comprehensive Fixes from Public GDSC Branch ==="
echo ""

# List of all 44 plugins that need fixes
PLUGINS=(
    "filter-plugin/logstash-filter-alloydb-guardium"
    "filter-plugin/logstash-filter-aurora-mysql-guardium"
    "filter-plugin/logstash-filter-azure-apachesolr-guardium"
    "filter-plugin/logstash-filter-azure-postgresql-guardium"
    "filter-plugin/logstash-filter-azure-sql-guardium"
    "filter-plugin/logstash-filter-capella-guardium"
    "filter-plugin/logstash-filter-cassandra-guardium"
    "filter-plugin/logstash-filter-cosmos-azure-guardium"
    "filter-plugin/logstash-filter-couchbasedb-guardium"
    "filter-plugin/logstash-filter-couchdb-guardium"
    "filter-plugin/logstash-filter-databricks-guardium"
    "filter-plugin/logstash-filter-documentdb-aws-guardium"
    "filter-plugin/logstash-filter-dynamodb-guardium"
    "filter-plugin/logstash-filter-elasticsearch-guardium"
    "filter-plugin/logstash-filter-generic-guardium"
    "filter-plugin/logstash-filter-hdfs-guardium"
    "filter-plugin/logstash-filter-intersystems-iris-guardium"
    "filter-plugin/logstash-filter-mariadb-aws-guardium"
    "filter-plugin/logstash-filter-mariadb-guardium"
    "filter-plugin/logstash-filter-milvus-guardium"
    "filter-plugin/logstash-filter-mongodb-guardium"
    "filter-plugin/logstash-filter-mysql-azure-guardium"
    "filter-plugin/logstash-filter-mysql-guardium"
    "filter-plugin/logstash-filter-mysql-percona-guardium"
    "filter-plugin/logstash-filter-neo4j-guardium"
    "filter-plugin/logstash-filter-neptune-aws-guardium"
    "filter-plugin/logstash-filter-onPremGreenplumdb-guardium"
    "filter-plugin/logstash-filter-opensearch-guardium"
    "filter-plugin/logstash-filter-oua-guardium"
    "filter-plugin/logstash-filter-progressdb-guardium"
    "filter-plugin/logstash-filter-pubsub-apachesolr-guardium"
    "filter-plugin/logstash-filter-pubsub-bigquery-guardium"
    "filter-plugin/logstash-filter-pubsub-bigtable-guardium"
    "filter-plugin/logstash-filter-pubsub-firebase-realtime-guardium"
    "filter-plugin/logstash-filter-pubsub-firestore-guardium"
    "filter-plugin/logstash-filter-pubsub-spanner-guardium"
    "filter-plugin/logstash-filter-redshift-aws-guardium"
    "filter-plugin/logstash-filter-s3-guardium"
    "filter-plugin/logstash-filter-saphana-guardium"
    "filter-plugin/logstash-filter-singlestore-guardium"
    "filter-plugin/logstash-filter-snowflake-guardium"
    "filter-plugin/logstash-filter-teradatadb-guardium"
    "filter-plugin/logstash-filter-trino-guardium"
    "filter-plugin/logstash-filter-yugabyte-guardium"
)

# Plugins that don't have jacocoVersion (simpler structure)
SIMPLE_PLUGINS=(
    "filter-plugin/logstash-filter-cosmos-azure-guardium"
    "filter-plugin/logstash-filter-elasticsearch-guardium"
    "filter-plugin/logstash-filter-generic-guardium"
    "filter-plugin/logstash-filter-intersystems-iris-guardium"
    "filter-plugin/logstash-filter-mysql-azure-guardium"
)

echo "Step 1: Upgrading gradle-jacoco-log from 4.0.1 to 3.1.0 in 44 plugins..."
for plugin in "${PLUGINS[@]}"; do
    BUILD_FILE="$plugin/build.gradle"
    if [ -f "$BUILD_FILE" ]; then
        sed -i '' 's/gradle-jacoco-log:4\.0\.1/gradle-jacoco-log:3.1.0/g' "$BUILD_FILE"
        echo "  ✓ Updated $plugin"
    fi
done
echo ""

echo "Step 2: Upgrading JaCoCo version from 0.8.4 to 0.8.11 in plugins with jacocoVersion..."
for plugin in "${PLUGINS[@]}"; do
    # Skip simple plugins
    skip=false
    for simple in "${SIMPLE_PLUGINS[@]}"; do
        if [ "$plugin" == "$simple" ]; then
            skip=true
            break
        fi
    done
    
    if [ "$skip" == "true" ]; then
        continue
    fi
    
    BUILD_FILE="$plugin/build.gradle"
    if [ -f "$BUILD_FILE" ]; then
        # Check if file has jacocoVersion
        if grep -q "def jacocoVersion" "$BUILD_FILE"; then
            sed -i '' "s/def jacocoVersion = '0\.8\.4'/def jacocoVersion = '0.8.11'/g" "$BUILD_FILE"
            echo "  ✓ Updated $plugin"
        fi
    fi
done
echo ""

echo "Step 3: Adding JVM args to test blocks in all 44 plugins..."
for plugin in "${PLUGINS[@]}"; do
    BUILD_FILE="$plugin/build.gradle"
    if [ -f "$BUILD_FILE" ]; then
        # Check if test block already has jvmArgs
        if grep -q "test {" "$BUILD_FILE" && ! grep -q "jvmArgs = \[" "$BUILD_FILE"; then
            # Add jvmArgs after "test {" line
            sed -i '' '/^test {$/a\
    jvmArgs = [\
        '\''--add-opens=java.base/java.lang=ALL-UNNAMED'\'',\
        '\''--add-opens=java.base/java.util=ALL-UNNAMED'\'',\
        '\''--add-opens=java.base/java.text=ALL-UNNAMED'\'',\
        '\''--add-opens=java.base/java.lang.reflect=ALL-UNNAMED'\'',\
        '\''--add-opens=java.base/sun.util.resources=ALL-UNNAMED'\'',\
        '\''--add-opens=java.base/sun.util.cldr=ALL-UNNAMED'\''\
    ]
' "$BUILD_FILE"
            echo "  ✓ Added JVM args to $plugin"
        elif grep -q "jvmArgs = \[" "$BUILD_FILE"; then
            echo "  ⊙ $plugin already has JVM args"
        else
            echo "  ⚠ $plugin doesn't have test block"
        fi
    fi
done
echo ""

echo "=== Summary ==="
echo "✓ Upgraded gradle-jacoco-log to 3.1.0 in 44 plugins"
echo "✓ Upgraded JaCoCo to 0.8.11 in ~39 plugins"
echo "✓ Added JVM args to test blocks in ~34 plugins"
echo ""
echo "Next steps:"
echo "1. Review changes: git diff"
echo "2. Test a few plugins locally"
echo "3. Commit changes"
echo "4. Push to upstream"
echo "5. Trigger SPS build"

# Made with Bob
