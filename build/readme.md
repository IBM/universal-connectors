# Build and Package Scripts

This repository contains a set of scripts for building and packaging plugins for a Logstash-based project. The scripts automate the process of building UC plugins and preparing offline packages. This README provides an overview of each script, details on their execution order, and guidance on determining which plugins to build.

## Main Script

### `buildUCDefaultOfflinePackage.sh`

**Purpose**: This is the main script that coordinates the build and packaging process. It performs the following tasks:

**Functionality**:
1. **Create Docker Image**: Builds a Docker image to act as the build platform.
2. **Run Plugin Build**: Executes `buildUCPluginGems.sh` inside the Docker container to build UC plugins.
3. **Prepare Offline Package**: Calls `prepareUCDefaultOfflinePackage.sh` inside the Docker container to prepare the default offline package which is installed on UC image.

**Usage**:
- Execute `buildUCDefaultOfflinePackage.sh` to start the build and packaging process. This script handles creating the Docker image, running the plugin build, and preparing the offline package.

   ```bash
   ./buildUCDefaultOfflinePackage.sh

## Additional Scripts

### `buildUCDefaultOfflinePackage.sh`
**Purpose**: This script is responsible for building Ruby gems and Java plugins. It performs the following steps:
**Functionality**:
1. **Build UC Commons**: Builds the UC Commons project, which is used by the Java plugins.
2. **Build Java Plugins**: Reads from javaPluginsToBuild.txt to build Java plugins.
3. **Build Ruby Plugins**: Builds Ruby plugins from directories listed in rubyPluginsToBuild.txt.

### `prepareUCDefaultOfflinePackage.sh`
**Purpose**: This script prepares an offline package for UC plugins.
**Functionality**: Packages the UC components and plugins for offline installation or deployment.

## Determining Which Plugins to Build

To determine which plugins to build, you need to configure two text files: `javaPluginsToBuild.txt` and `rubyPluginsToBuild.txt`.

### 1. **Java Plugins**:
- List the Java plugin directories in `javaPluginsToBuild.txt`.
- Each line should be a relative path to a directory containing a Gradle project for a Java plugin.
- Lines that are empty or start with `#` will be ignored.

**Example `javaPluginsToBuild.txt`**:
```plaintext
filter-plugin/logstash-filter-mongodb-guardium
filter-plugin/logstash-filter-dynamodb-guardium
```

### 2. **Ruby Plugins**:
- List the Ruby plugin directories in `rubyPluginsToBuild.txt`.
- Each line should be a relative path to a directory containing a Gradle project for a Ruby plugin.
- Lines that are empty or start with `#` will be ignored.

**Example `javaPluginsToBuild.txt`**:
```plaintext
filter-plugin/logstash-filter-pubsub-mysql-guardium
filter-plugin/logstash-filter-pubsub-postgresql-guardium
```

