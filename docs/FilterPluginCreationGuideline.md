# Setting up Logstash Development Environment: A Complete Guide

## Introduction

Setting up a Logstash development environment can be complex, especially when working with custom plugins and multiple
versions. This guide walks you through the complete process of setting up Logstash from source, building custom filter
and input plugins, and installing them for use.

## Version Compatibility

Before starting, it's crucial to understand the version dependencies:

- **GDP 11.x** → Logstash 7.x → Java 8
- **GDP 12.x** → Logstash 8.x → Java 11

Make sure you're using the correct Java version for your target Logstash version.

## Prerequisites

### 1. Java SDK Installation

Download and install the IBM Semeru Runtime from:

```
https://developer.ibm.com/languages/java/semeru-runtimes/downloads/?license=IBM
```

### 2. Clone Required Repositories

You'll need to clone three repositories:

**Logstash Core:**

```bash
git clone https://github.com/elastic/logstash.git
```

For a specific older version:

```bash
git clone --branch 7.5 --single-branch https://github.com/elastic/logstash.git
```

**Universal Connectors (Public):**

```bash
git clone https://github.com/IBM/universal-connectors
```

**Universal Connectors (Internal):**
Note: Only for IBM's developers

```bash
git clone https://github.ibm.com/Activity-Insights/universal-connectors
```

### 3. Build Common Project

Navigate to the common project inside the public universal-connectors repo and build it:

1. Clean the project
2. Run assemble
3. Run build


## Building Logstash from Source

### Step 1: Install Ruby Version Manager (RVM)

Follow the official Logstash development guide, then execute these commands:

```bash
# Install specific JRuby version
rvm install "jruby-9.3.10.0"

# Install required gems
gem install rake
gem install bundler -v 2.3.27

# Import GPG keys
gpg --keyserver hkp://keys.gnupg.net --recv-keys 409B6B1796C275462A1703113804BB82D39DC0E3

# Install RVM with stable Ruby
\curl -sSL https://get.rvm.io | bash -s stable --ruby=$(cat .ruby-version)
```

### Step 2: Verify Ruby Version

Ensure your Ruby version matches the required version:

```bash
ruby -v
cat .ruby-version
```

These two commands should output the same version.

### Step 3: Set Environment Variables

Set these critical environment variables:

```bash
export OSS=false
export LOGSTASH_SOURCE=1
export LOGSTASH_PATH=pathToLogstashDirectory/logstash
```

**Important Note:** The `OSS=false` export is crucial. Without it, you may encounter this error when testing:

```
Logstash stopped processing because of an error: (SystemExit) exit
```

### Step 4: Test Your Logstash Installation

Run a simple test to verify Logstash is working:

```bash
bin/logstash -e 'input { stdin { } } output { stdout {} }'
```

## Building a Filter Plugin

### Step 1: Create gradle.properties

In your filter plugin project root, create a `gradle.properties` file with these paths (adjust to your local setup):

```properties
LOGSTASH_CORE_PATH=pathToLogstashDirectory/logstash/logstash-core
GUARDIUM_UNIVERSALCONNECTOR_COMMONS_PATH=pathToPublicUniversalConnectorDirectory/guardium-universalconnector-commons/build/libs
```

### Step 2: Modify build.gradle

Add to the build script section:

```gradle
ext {
    snakeYamlVersion = '2.2'
}
```

Change the implementation line from:

```gradle
implementation fileTree(dir: LOGSTASH_CORE_PATH, include: "build/libs/logstash-core-*.*.*.jar")
```

To:

```gradle
implementation fileTree(dir: LOGSTASH_CORE_PATH, include: "build/libs/logstash-core.jar")
```

**Note:** You may need to re-import the Record type after these changes.

### Step 3: Build the Plugin

```bash
chmod +x gradlew
./gradlew clean build gem
```

Run tests to verify everything works correctly.

## Building an Input Plugin

The process for input plugins is similar to filter plugins with slight variations:

### Step 1: Configure gradle.properties

```properties
LOGSTASH_CORE_PATH=pathToLogstashDirectory/logstash/logstash-core
```

If there are conflicting paths, comment them out:

```properties
#LOGSTASH_CORE_PATH=pathToLogstashDirectory/universal-connector-master/logstash-master/logstash-core
```

### Step 2: Modify build.gradle

Add the same build script modifications as the filter plugin:

```gradle
ext {
    snakeYamlVersion = '2.2'
}
```

Update the implementation line:

```gradle
implementation fileTree(dir: LOGSTASH_CORE_PATH, include: "build/libs/logstash-core.jar")
```

### Step 3: Build

```bash
./gradlew clean build
```

## Installing a Filter Plugin

### Step 1: Prepare Offline Pack

From your plugin project directory:

```bash
bin/logstash-plugin prepare-offline-pack \
  --output logstash-filter-auroramysqlguardiumpluginfilter.zip \
  --overwrite logstash-filter-auroramysqlguardiumpluginfilter
```

### Step 2: Install the Plugin

Navigate to your Logstash project directory and install:

```bash
bin/logstash-plugin install \
  --no-verify \
  --local logstash-filter-auroramysqlguardiumpluginfilter-1.0.1.gem
```

## Common Issues and Solutions

### Issue 1: SystemExit Error

**Problem:** `Logstash stopped processing because of an error: (SystemExit) exit`

**Solution:** Ensure you've set `export OSS=false` before running Logstash.

### Issue 2: Ruby Version Mismatch

**Problem:** Ruby version doesn't match requirements

**Solution:** Use RVM to install the exact version specified in `.ruby-version`:

```bash
rvm install "jruby-9.3.10.0"
rvm use jruby-9.3.10.0
```

### Issue 3: Build Failures

**Problem:** Gradle build fails with dependency issues

**Solution:**

- Verify all paths in `gradle.properties` are correct
- Ensure the common project is built first
- Check that `snakeYamlVersion` is set to `2.2`

## Best Practices

1. **Version Control**: Keep track of which Logstash version you're building against
2. **Path Management**: Use absolute paths in `gradle.properties` to avoid confusion
3. **Test Incrementally**: Test after each major step rather than waiting until the end
4. **Documentation**: Keep notes on any custom modifications you make

## Conclusion

Setting up a Logstash development environment requires careful attention to version compatibility, proper configuration
of build files, and correct environment variables. By following this guide, you should be able to:

- Build Logstash from source
- Create custom filter and input plugins
- Install and test your plugins locally

This setup enables you to develop, test, and deploy custom Logstash plugins for your specific data processing needs.

## Additional Resources

- [Logstash Official Documentation](https://github.com/elastic/logstash?tab=readme-ov-file#developing-logstash-core)
- [IBM Universal Connectors](https://github.com/IBM/universal-connectors)
- [IBM Semeru Runtimes](https://developer.ibm.com/languages/java/semeru-runtimes/downloads/?license=IBM)

---

*Last Updated: January 2026*