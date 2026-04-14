#!/bin/bash

# Script to add test blocks with JVM args to plugins that don't have them
# Based on commit 30b637e5 from public GDSC branch

set -e

REPO_DIR="/Users/jiseok/Documents/workspace/public/universal-connectors"
cd "$REPO_DIR"

echo "=== Adding Missing Test Blocks with JVM Args ==="
echo ""

# List of plugins that need test blocks added
PLUGINS_NEEDING_TEST_BLOCKS=(
    "filter-plugin/logstash-filter-aurora-mysql-guardium"
    "filter-plugin/logstash-filter-azure-apachesolr-guardium"
    "filter-plugin/logstash-filter-azure-postgresql-guardium"
    "filter-plugin/logstash-filter-azure-sql-guardium"
    "filter-plugin/logstash-filter-cassandra-guardium"
    "filter-plugin/logstash-filter-couchbasedb-guardium"
    "filter-plugin/logstash-filter-dynamodb-guardium"
    "filter-plugin/logstash-filter-elasticsearch-guardium"
    "filter-plugin/logstash-filter-generic-guardium"
    "filter-plugin/logstash-filter-hdfs-guardium"
    "filter-plugin/logstash-filter-mariadb-aws-guardium"
    "filter-plugin/logstash-filter-mariadb-guardium"
    "filter-plugin/logstash-filter-mysql-azure-guardium"
    "filter-plugin/logstash-filter-mysql-guardium"
    "filter-plugin/logstash-filter-mysql-percona-guardium"
    "filter-plugin/logstash-filter-neo4j-guardium"
    "filter-plugin/logstash-filter-onPremGreenplumdb-guardium"
    "filter-plugin/logstash-filter-oua-guardium"
    "filter-plugin/logstash-filter-progressdb-guardium"
    "filter-plugin/logstash-filter-pubsub-apachesolr-guardium"
    "filter-plugin/logstash-filter-pubsub-firebase-realtime-guardium"
    "filter-plugin/logstash-filter-redshift-aws-guardium"
    "filter-plugin/logstash-filter-s3-guardium"
    "filter-plugin/logstash-filter-saphana-guardium"
    "filter-plugin/logstash-filter-singlestore-guardium"
    "filter-plugin/logstash-filter-snowflake-guardium"
    "filter-plugin/logstash-filter-teradatadb-guardium"
    "filter-plugin/logstash-filter-yugabyte-guardium"
)

# Test block content to add
TEST_BLOCK='
test {
    jvmArgs = [
        '\''--add-opens=java.base/java.lang=ALL-UNNAMED'\'',
        '\''--add-opens=java.base/java.util=ALL-UNNAMED'\'',
        '\''--add-opens=java.base/java.text=ALL-UNNAMED'\'',
        '\''--add-opens=java.base/java.lang.reflect=ALL-UNNAMED'\'',
        '\''--add-opens=java.base/sun.util.resources=ALL-UNNAMED'\'',
        '\''--add-opens=java.base/sun.util.cldr=ALL-UNNAMED'\''
    ]
}
'

count=0
for plugin in "${PLUGINS_NEEDING_TEST_BLOCKS[@]}"; do
    BUILD_FILE="$plugin/build.gradle"
    if [ -f "$BUILD_FILE" ]; then
        # Check if test block already exists
        if grep -q "^test {" "$BUILD_FILE"; then
            echo "  ⊙ $plugin already has test block"
            continue
        fi
        
        # Find the line with "tasks.withType(JavaCompile)" and add test block after its closing brace
        if grep -q "tasks.withType(JavaCompile)" "$BUILD_FILE"; then
            # Use awk to insert after the closing brace of tasks.withType(JavaCompile)
            awk '
                /tasks\.withType\(JavaCompile\)/ { in_block=1 }
                in_block && /^}$/ { 
                    print
                    print ""
                    print "test {"
                    print "    jvmArgs = ["
                    print "        '\''--add-opens=java.base/java.lang=ALL-UNNAMED'\'',"
                    print "        '\''--add-opens=java.base/java.util=ALL-UNNAMED'\'',"
                    print "        '\''--add-opens=java.base/java.text=ALL-UNNAMED'\'',"
                    print "        '\''--add-opens=java.base/java.lang.reflect=ALL-UNNAMED'\'',"
                    print "        '\''--add-opens=java.base/sun.util.resources=ALL-UNNAMED'\'',"
                    print "        '\''--add-opens=java.base/sun.util.cldr=ALL-UNNAMED'\''"
                    print "    ]"
                    print "}"
                    in_block=0
                    next
                }
                { print }
            ' "$BUILD_FILE" > "$BUILD_FILE.tmp" && mv "$BUILD_FILE.tmp" "$BUILD_FILE"
            echo "  ✓ Added test block to $plugin"
            ((count++))
        else
            echo "  ⚠ $plugin doesn't have tasks.withType(JavaCompile) block"
        fi
    fi
done

echo ""
echo "=== Summary ==="
echo "✓ Added test blocks to $count plugins"
echo ""
echo "Next: Review changes with 'git diff'"

# Made with Bob
