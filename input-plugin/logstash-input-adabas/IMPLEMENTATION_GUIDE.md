# Adabas Universal Connector Implementation Guide for Guardium Data Protection

## Table of Contents
1. [Overview](#overview)
2. [Architecture](#architecture)
3. [Prerequisites](#prerequisites)
4. [Implementation Steps](#implementation-steps)
   - [Step 1: Configure Adabas Auditing Server](#step-1-configure-adabas-auditing-server)
   - [Step 2: Obtain or Build the Plugins](#step-2-obtain-or-build-the-plugins)
   - [Step 3: Install Plugins on Guardium](#step-3-install-plugins-on-guardium)
   - [Step 4: Configure Universal Connector](#step-4-configure-universal-connector)
   - [Step 5: Enable Universal Connector](#step-5-enable-universal-connector)
   - [Step 6: Verify Data Flow](#step-6-verify-data-flow)
5. [Configuration Reference](#configuration-reference)
6. [Troubleshooting](#troubleshooting)
7. [Appendix](#appendix)

---

## Overview

This guide provides step-by-step instructions for implementing the Adabas Universal Connector with IBM Guardium Data Protection. The Adabas connector enables Guardium to monitor and audit Adabas database activity without requiring traditional S-TAP agents.

### What is the Universal Connector?

The Guardium Universal Connector is a framework that allows Guardium to receive and process audit data from various data sources through their native audit logs. It uses a Logstash-based pipeline with three components:

1. **Input Plugin** - Receives audit data from the data source
2. **Filter Plugin** - Parses and transforms the data into Guardium format
3. **Output Plugin** - Sends processed data to Guardium (internal component)

### Adabas-Specific Architecture

The Adabas implementation uses a **custom input plugin** that connects directly to the Adabas Auditing Server via EntireX Broker, rather than using standard log forwarding methods like Syslog or Filebeat.

**Key Components:**
- **Adabas Auditing Server** - Generates audit events from Adabas database activity
- **EntireX Broker** - Messaging middleware that facilitates communication
- **Adabas Input Plugin** (`logstash-input-adabas_auditing_input`) - Connects to the broker and retrieves audit messages
- **Adabas Filter Plugin** (`logstash-filter-adabas_guardium_filter`) - Parses audit data into Guardium format
- **Guardium Universal Connector** - Hosts the plugins and forwards data to Guardium

---

## Architecture

```
┌─────────────────────────┐
│   Adabas Database       │
│                         │
└───────────┬─────────────┘
            │
            │ Audit Events
            ▼
┌─────────────────────────┐
│ Adabas Auditing Server  │
│                         │
└───────────┬─────────────┘
            │
            │ EntireX Protocol
            ▼
┌─────────────────────────┐
│   EntireX Broker        │
│   (Messaging Layer)     │
└───────────┬─────────────┘
            │
            │ Broker Messages
            ▼
┌─────────────────────────────────────────────┐
│   Guardium Universal Connector              │
│                                             │
│   ┌─────────────────────────────────────┐  │
│   │  Input Plugin                       │  │
│   │  (adabas_auditing_input)            │  │
│   │  - Connects to EntireX Broker       │  │
│   │  - Retrieves audit messages         │  │
│   │  - Parses binary format             │  │
│   └──────────────┬──────────────────────┘  │
│                  │                          │
│                  ▼                          │
│   ┌─────────────────────────────────────┐  │
│   │  Filter Plugin                      │  │
│   │  (adabas_guardium_filter)           │  │
│   │  - Transforms to Guardium format    │  │
│   │  - Extracts metadata                │  │
│   │  - Handles errors                   │  │
│   └──────────────┬──────────────────────┘  │
│                  │                          │
│                  ▼                          │
│   ┌─────────────────────────────────────┐  │
│   │  Output Plugin (Internal)           │  │
│   │  - Sends to Guardium Sniffer        │  │
│   └─────────────────────────────────────┘  │
└─────────────────┬───────────────────────────┘
                  │
                  ▼
┌─────────────────────────────────────────────┐
│   Guardium Data Protection                  │
│   - Policy Enforcement                      │
│   - Reporting & Analytics                   │
│   - Alerting                                │
└─────────────────────────────────────────────┘
```

---

## Prerequisites

### System Requirements

#### Guardium System
- **Guardium Data Protection Version:** 12.0 or later
- **Deployment Type:** Standalone system or Collector
- **User Permissions:** S-TAP Management Application role
- **Network Access:** Connectivity to Adabas Auditing Server's EntireX Broker

#### Adabas Environment
- **Adabas Version:** Compatible version with Auditing Server support
- **Adabas Auditing Server:** Installed and configured
- **EntireX Broker:** Running and accessible
- **Network Configuration:** Firewall rules allowing Guardium to connect to broker port (default: 3000)

### Software Requirements

#### For Using Pre-Built Plugins (Recommended)
- Pre-built plugin files (`.gem` files) provided by IBM or Software AG:
  - `logstash-input-adabas_auditing_input-<version>-java.gem`
  - `logstash-filter-adabas_guardium_filter-<version>-java.gem`

#### For Building Plugins from Source (Optional)
- **Java Development Kit (JDK):** Version 11 or later
- **Gradle:** Version 6.x or later (included via wrapper)
- **Logstash:** Version 7.5.2 or compatible
- **Git:** For cloning repositories
- **Build Dependencies:**
  - Guardium Universal Connector Commons library
  - Adabas SDK libraries (provided by Software AG)
  - EntireX libraries (provided by Software AG)

### Required Information

Before starting, gather the following information:

| Information | Description | Example |
|------------|-------------|---------|
| **Broker Host** | Hostname or IP of EntireX Broker | `adabas-broker.company.com` |
| **Broker Port** | Port number for broker connection | `3000` |
| **Broker Class** | Broker class identifier | `ADABAS-AUDIT` |
| **Broker Server** | Broker server name | `AUDIT-SERVER` |
| **Broker Service** | Service name for audit data | `AUDIT-SERVICE` |
| **User** | Authentication user for broker | `guardium_user` |
| **Token** | Authentication token/password | `<secure_token>` |
| **Guardium Collector IP** | IP address of Guardium system | `10.0.1.100` |

### Network Requirements

Ensure the following network connectivity:

```
Guardium Collector → EntireX Broker
- Protocol: TCP
- Port: 3000 (default, may vary)
- Direction: Outbound from Guardium
```

---

## Implementation Steps

### Step 1: Configure Adabas Auditing Server

The Adabas Auditing Server must be properly configured to generate and publish audit events.

#### 1.1 Enable Adabas Auditing

**Note:** This step is typically performed by your Adabas administrator. Consult Software AG documentation for detailed Adabas configuration.

1. **Enable Auditing on Adabas Database:**
   - Configure the Adabas database to generate audit records
   - Specify which operations to audit (reads, writes, DDL, etc.)
   - Set audit detail level

2. **Configure Adabas Auditing Server:**
   - Install the Adabas Auditing Server component
   - Configure connection to the Adabas database
   - Set up audit event collection parameters

#### 1.2 Configure EntireX Broker

The EntireX Broker acts as the messaging middleware between Adabas and Guardium.

1. **Verify Broker is Running:**
   ```bash
   # Check broker status (command may vary by platform)
   etbinfo -b <broker_id>
   ```

2. **Configure Broker Service:**
   - Create or identify the service that will publish audit events
   - Note the broker class, server, and service names
   - Configure authentication if required

3. **Test Broker Connectivity:**
   ```bash
   # Test connection from Guardium server
   telnet <broker_host> <broker_port>
   ```

#### 1.3 Verify Audit Data Flow

Before proceeding, verify that audit data is being generated:

1. Perform some database operations on Adabas
2. Check that audit events are being published to the broker
3. Use EntireX tools to verify message flow

---

### Step 2: Obtain or Build the Plugins

You have two options: use pre-built plugins or build from source.

#### Option A: Using Pre-Built Plugins (Recommended)

If IBM or Software AG has provided pre-built plugin files:

1. **Obtain the Plugin Files:**
   - `logstash-input-adabas_auditing_input-<version>-java.gem`
   - `logstash-filter-adabas_guardium_filter-<version>-java.gem`

2. **Transfer to Guardium System:**
   ```bash
   # Copy files to Guardium (from your local machine)
   scp logstash-input-adabas_auditing_input-*.gem guardium@<guardium_ip>:/tmp/
   scp logstash-filter-adabas_guardium_filter-*.gem guardium@<guardium_ip>:/tmp/
   ```

3. **Skip to Step 3** (Installation)

#### Option B: Building Plugins from Source

If you need to build the plugins yourself:

##### 2.1 Prepare Build Environment

1. **Install Java 11:**
   ```bash
   # Verify Java installation
   java -version
   # Should show Java 11 or later
   ```

2. **Download Logstash:**
   ```bash
   # Download Logstash 7.5.2 or compatible version
   wget https://artifacts.elastic.co/downloads/logstash/logstash-7.5.2.tar.gz
   tar -xzf logstash-7.5.2.tar.gz
   export LOGSTASH_HOME=/path/to/logstash-7.5.2
   ```

3. **Obtain Required Files:**
   - Download `rubyUtils.gradle` and `versions.yml` from Logstash GitHub
   - Copy to your Logstash installation directory

4. **Fix rubyUtils.gradle Issues:**

   Edit `rubyUtils.gradle` and make these changes:

   **Issue 1 - Fix JRuby Version:**
   ```gradle
   // Find this line (around line 20):
   classpath "org.jruby:jruby-core:${gradle.ext.versions.jruby.version}"
   
   // Replace with actual version from versions.yml:
   classpath "org.jruby:jruby-core:9.4.13.0"
   ```

   **Issue 2 - Add YAML Parsing:**
   ```gradle
   // Add this code after the Ruby variables section:
   
   // Ruby variables
   def versionsPath = project.hasProperty("LOGSTASH_CORE_PATH") ? LOGSTASH_CORE_PATH + "/../versions.yml" : "${projectDir}/versions.yml"
   
   // Add YAML parsing code below:
   def versionsFile = new File(versionsPath)
   if (!versionsFile.exists()) {
       throw new GradleException("versions.yml file not found at: ${versionsPath}")
   }
   
   def versionsData = [:]
   def currentSection = null
   versionsFile.eachLine { line ->
       def trimmed = line.trim()
       if (trimmed && !trimmed.startsWith('#')) {
           if (!trimmed.startsWith(' ') && trimmed.endsWith(':')) {
               currentSection = trimmed.replaceAll(':', '')
               versionsData[currentSection] = [:]
           } else if (trimmed.startsWith('version:') || trimmed.startsWith('sha256:')) {
               def parts = trimmed.split(':', 2)
               if (parts.length == 2 && currentSection) {
                   versionsData[currentSection][parts[0].trim()] = parts[1].trim()
               }
           }
       }
   }
   
   gradle.ext.versions = versionsData
   versionMap = gradle.ext.versions
   ```

##### 2.2 Build Guardium Commons Library

1. **Clone the Commons Repository:**
   ```bash
   git clone https://github.com/IBM/guardium-universalconnector-commons.git
   cd guardium-universalconnector-commons
   ```

2. **Build the Commons JARs:**
   ```bash
   # Follow the README instructions to build
   ./gradlew build
   # Note the location of generated JAR files
   ```

##### 2.3 Build Input Plugin

1. **Clone or Navigate to Input Plugin:**
   ```bash
   cd /path/to/universal-connectors/input-plugin/logstash-input-adabas
   ```

2. **Create gradle.properties:**
   ```bash
   cat > gradle.properties << EOF
   LOGSTASH_CORE_PATH=/path/to/logstash-7.5.2/logstash-core
   EOF
   ```

3. **Build the Plugin:**
   ```bash
   ./gradlew assemble gem
   ```

4. **Verify Build:**
   ```bash
   # Check for generated .gem file
   ls -l logstash-input-adabas_auditing_input-*.gem
   ```

##### 2.4 Build Filter Plugin

1. **Navigate to Filter Plugin:**
   ```bash
   cd /path/to/universal-connectors/filter-plugin/logstash-filter-adabas-guardium
   ```

2. **Create gradle.properties:**
   ```bash
   cat > gradle.properties << EOF
   LOGSTASH_CORE_PATH=/path/to/logstash-7.5.2/logstash-core
   GUARDIUM_UNIVERSALCONNECTOR_COMMONS_PATH=/path/to/guardium-universalconnector-commons/build/libs
   EOF
   ```

3. **Build the Plugin:**
   ```bash
   ./gradlew assemble gem
   ```

4. **Verify Build:**
   ```bash
   # Check for generated .gem file
   ls -l logstash-filter-adabas_guardium_filter-*.gem
   ```

##### 2.5 Transfer Built Plugins to Guardium

```bash
# Copy both .gem files to Guardium
scp logstash-input-adabas_auditing_input-*.gem guardium@<guardium_ip>:/tmp/
scp logstash-filter-adabas_guardium_filter-*.gem guardium@<guardium_ip>:/tmp/
```

---

### Step 3: Install Plugins on Guardium

Now install the plugins on your Guardium system.

#### 3.1 Access Guardium CLI

1. **SSH to Guardium:**
   ```bash
   ssh guardium@<guardium_ip>
   ```

2. **Switch to CLI Mode:**
   ```bash
   # If prompted, enter CLI mode
   cli
   ```

#### 3.2 Install Input Plugin

1. **Install the Input Plugin:**
   ```bash
   grdapi install_universal_connector_plugin \
     plugin_file=/tmp/logstash-input-adabas_auditing_input-<version>-java.gem
   ```

2. **Verify Installation:**
   ```bash
   # List installed plugins
   grdapi list_universal_connector_plugins
   ```

   Expected output should include:
   ```
   adabas_auditing_input
   ```

#### 3.3 Install Filter Plugin

1. **Install the Filter Plugin:**
   ```bash
   grdapi install_universal_connector_plugin \
     plugin_file=/tmp/logstash-filter-adabas_guardium_filter-<version>-java.gem
   ```

2. **Verify Installation:**
   ```bash
   # List installed plugins
   grdapi list_universal_connector_plugins
   ```

   Expected output should include:
   ```
   adabas_auditing_input
   adabas_guardium_filter
   ```

#### 3.4 Clean Up

```bash
# Remove temporary .gem files
rm /tmp/logstash-input-adabas_auditing_input-*.gem
rm /tmp/logstash-filter-adabas_guardium_filter-*.gem
```

---

### Step 4: Configure Universal Connector

Configure the Universal Connector to use the Adabas plugins.

#### 4.1 Access Universal Connector Configuration

1. **Log in to Guardium Web UI:**
   - Open browser: `https://<guardium_ip>:8443`
   - Enter credentials

2. **Navigate to Universal Connector:**
   - Go to: **Setup** → **Tools and Views** → **Configure Universal Connector**

#### 4.2 Create Connector Configuration

1. **Click "Add" to Create New Connector**

2. **Enter Connector Name:**
   - Name: `Adabas_Production` (or your preferred name)
   - Description: `Adabas database auditing via EntireX Broker`

3. **Configure Input Section:**

   Click on the **Input** tab and enter:

   ```ruby
   input {
     adabas_auditing_input {
       # EntireX Broker connection details
       host => "adabas-broker.company.com"
       port => 3000
       
       # Broker service identification
       brokerClass => "ADABAS-AUDIT"
       brokerServer => "AUDIT-SERVER"
       brokerService => "AUDIT-SERVICE"
       
       # Authentication
       user => "guardium_user"
       token => "your_secure_token"
       
       # Optional: Connection parameters
       retryInterval => 5
       retryCount => 10
       waitTime => 30
       receiveLength => 32767
       compression => 0
       
       # Optional: Metadata REST server URL
       # restURL => "http://metadata-server:8080"
     }
   }
   ```

   **Parameter Descriptions:**

   | Parameter | Required | Description | Default |
   |-----------|----------|-------------|---------|
   | `host` | Yes | EntireX Broker hostname or IP | `localhost` |
   | `port` | Yes | EntireX Broker port | `3000` |
   | `brokerClass` | Yes | Broker class identifier | `class` |
   | `brokerServer` | Yes | Broker server name | `server` |
   | `brokerService` | Yes | Service name for audit data | `service` |
   | `user` | Yes | Authentication username | `user` |
   | `token` | Yes | Authentication token/password | `token` |
   | `retryInterval` | No | Retry interval in seconds | `5` |
   | `retryCount` | No | Number of retry attempts | `10` |
   | `waitTime` | No | Wait time in seconds | `30` |
   | `receiveLength` | No | Maximum message receive length | `32767` |
   | `compression` | No | Compression level (0=none) | `0` |
   | `restURL` | No | Metadata REST server URL | `""` |

4. **Configure Filter Section:**

   Click on the **Filter** tab and enter:

   ```ruby
   filter {
     adabas_guardium_filter {
       # Source field containing audit data
       source => "adabas-auditing"
     }
   }
   ```

   **Note:** The `source` parameter should match the field name used by the input plugin (default: `adabas-auditing`).

5. **Review Configuration:**
   - Verify all parameters are correct
   - Check for syntax errors (Guardium will validate)

6. **Save Configuration:**
   - Click **Save**
   - Guardium will validate the configuration
   - If validation fails, review error messages and correct

#### 4.3 Using Secrets for Sensitive Data (Recommended)

For production environments, use Guardium's keystore for sensitive information:

1. **Create Secrets via CLI:**
   ```bash
   # SSH to Guardium
   ssh guardium@<guardium_ip>
   
   # Add broker credentials to keystore
   grdapi universal_connector_keystore_add \
     key=ADABAS_BROKER_USER \
     password=guardium_user
   
   grdapi universal_connector_keystore_add \
     key=ADABAS_BROKER_TOKEN \
     password=your_secure_token
   ```

2. **Verify Secrets:**
   ```bash
   grdapi universal_connector_keystore_list
   ```

3. **Update Input Configuration to Use Secrets:**
   ```ruby
   input {
     adabas_auditing_input {
       host => "adabas-broker.company.com"
       port => 3000
       brokerClass => "ADABAS-AUDIT"
       brokerServer => "AUDIT-SERVER"
       brokerService => "AUDIT-SERVICE"
       
       # Use environment variables from keystore
       user => "${ADABAS_BROKER_USER}"
       token => "${ADABAS_BROKER_TOKEN}"
       
       retryInterval => 5
       retryCount => 10
       waitTime => 30
     }
   }
   ```

4. **Save Updated Configuration**

---

### Step 5: Enable Universal Connector

#### 5.1 Enable via Web UI

1. **In Configure Universal Connector Page:**
   - Verify your connector configuration is listed
   - Click **Enable** button

2. **Wait for Startup:**
   - Universal Connector will start (takes 1-2 minutes)
   - Status will change from "Disabled" to "Enabled"

#### 5.2 Enable via CLI (Alternative)

```bash
# SSH to Guardium
ssh guardium@<guardium_ip>

# Enable Universal Connector
grdapi run_universal_connector
```

#### 5.3 Enable with Debug Logging (For Troubleshooting)

```bash
# Enable with debug level logging
grdapi run_universal_connector debug_level=2
```

**Debug Levels:**
- `0` - Errors only (default)
- `1` - Warnings and errors
- `2` - Info, warnings, and errors
- `3` - Debug (verbose)

#### 5.4 Verify Universal Connector Status

1. **Check Status in Web UI:**
   - The **Enable** button should change to **Disable**
   - Status indicator should be green

2. **Check Status via CLI:**
   ```bash
   grdapi get_universal_connector_status
   ```

   Expected output:
   ```
   Status: Running
   Connectors: 1
   ```

---

### Step 6: Verify Data Flow

#### 6.1 Generate Test Activity

1. **Perform Operations on Adabas:**
   - Connect to your Adabas database
   - Execute some queries (SELECT, INSERT, UPDATE)
   - Perform administrative operations if applicable

2. **Wait for Processing:**
   - Allow 1-2 minutes for data to flow through the pipeline

#### 6.2 Check S-TAP Status Page

1. **Navigate to S-TAP Status:**
   - Go to: **Monitor** → **S-TAP Status**

2. **Look for Adabas Connector:**
   - S-TAP Host format: `<adabas_host>:<broker_port>:UC<n>`
   - S-TAP Version: `Universal connector V<version>`
   - Status should be **Green** (active)

3. **Verify Data Flow:**
   - Check "Last Contact" timestamp (should be recent)
   - Check "Messages" count (should be increasing)

#### 6.3 Check Guardium Logs

1. **Access Universal Connector Logs:**
   ```bash
   # SSH to Guardium
   ssh guardium@<guardium_ip>
   
   # View recent log entries
   grdapi tail_universal_connector_log lines=100
   ```

2. **Look for Success Indicators:**
   - Connection to broker established
   - Messages being received
   - No error messages

3. **Check for Errors:**
   ```bash
   # Search for errors in logs
   grdapi tail_universal_connector_log lines=500 | grep -i error
   ```

#### 6.4 Verify Data in Guardium Reports

1. **Run Activity Report:**
   - Go to: **Reports** → **Activity** → **Activity Report**
   - Set time range to last hour
   - Filter by database type: Adabas

2. **Check for Audit Records:**
   - Verify records are appearing
   - Check that user names are populated
   - Verify SQL/commands are captured
   - Confirm timestamps are correct

3. **Review Session Details:**
   - Go to: **Monitor** → **Sessions**
   - Look for Adabas sessions
   - Verify session information is complete

#### 6.5 Test Policy Enforcement

1. **Create Test Policy:**
   - Go to: **Policy** → **Policy Builder**
   - Create a simple policy (e.g., alert on SELECT statements)
   - Apply to Adabas data source

2. **Trigger Policy:**
   - Execute operations that match the policy
   - Wait for policy evaluation

3. **Check Alerts:**
   - Go to: **Monitor** → **Alerts**
   - Verify alerts are generated for Adabas activity

---

## Configuration Reference

### Complete Configuration Example

Here's a complete, production-ready configuration:

```ruby
# ============================================
# INPUT SECTION
# ============================================
input {
  adabas_auditing_input {
    # Broker Connection
    host => "adabas-broker.company.com"
    port => 3000
    
    # Service Identification
    brokerClass => "ADABAS-AUDIT"
    brokerServer => "AUDIT-SERVER"
    brokerService => "AUDIT-SERVICE"
    
    # Authentication (using keystore)
    user => "${ADABAS_BROKER_USER}"
    token => "${ADABAS_BROKER_TOKEN}"
    
    # Connection Tuning
    retryInterval => 5      # Retry every 5 seconds on failure
    retryCount => 10        # Retry up to 10 times
    waitTime => 30          # Wait 30 seconds for messages
    receiveLength => 32767  # Maximum message size
    compression => 0        # No compression
    
    # Optional: Metadata Server
    # restURL => "http://metadata-server:8080"
  }
}

# ============================================
# FILTER SECTION
# ============================================
filter {
  adabas_guardium_filter {
    source => "adabas-auditing"
  }
  
  # Optional: Add custom fields
  mutate {
    add_field => {
      "environment" => "production"
      "data_center" => "DC1"
    }
  }
}
```

### Environment Variables Reference

When using the keystore, these environment variables are available:

| Variable Name | Purpose | Example Value |
|--------------|---------|---------------|
| `ADABAS_BROKER_USER` | Broker authentication username | `guardium_user` |
| `ADABAS_BROKER_TOKEN` | Broker authentication token | `<secure_token>` |
| `ADABAS_BROKER_HOST` | Broker hostname (optional) | `adabas-broker.company.com` |
| `ADABAS_METADATA_URL` | Metadata REST server URL (optional) | `http://metadata:8080` |

### Guardium Record Structure

The filter plugin transforms Adabas audit data into this Guardium structure:

```json
{
  "sessionId": "mV20eHvvRha2ELTeqJxQJg==",
  "dbName": "ADABAS-DB-001",
  "appUserName": "APP_USER",
  "time": {
    "timestamp": 1705751051070,
    "minOffsetFromGMT": -240,
    "minDst": 0
  },
  "accessor": {
    "dbUser": "NATUID_VALUE",
    "serverType": "Adabas",
    "serverHostName": "LPAR_NAME",
    "sourceProgram": "NATPROG_NAME",
    "language": "FREE_TEXT",
    "dataType": "CONSTRUCT",
    "dbProtocol": "Adabas native audit"
  },
  "data": {
    "construct": {
      "sentences": [
        {
          "verb": "READ",
          "fields": ["FIELD1", "FIELD2", "FIELD3"]
        }
      ]
    }
  },
  "exception": {
    "exceptionTypeId": "SQL_ERROR",
    "description": "Response Code 3 (0) received.",
    "sqlString": "READ with ISN 12345"
  }
}
```

---

## Troubleshooting

### Common Issues and Solutions

#### Issue 1: Universal Connector Won't Start

**Symptoms:**
- Status remains "Disabled" after clicking Enable
- Error in logs: "Failed to start Universal Connector"

**Solutions:**

1. **Check Plugin Installation:**
   ```bash
   grdapi list_universal_connector_plugins
   ```
   Verify both `adabas_auditing_input` and `adabas_guardium_filter` are listed.

2. **Check Configuration Syntax:**
   - Review input and filter sections for typos
   - Ensure all required parameters are present
   - Verify quotes and brackets are balanced

3. **Check Logs for Details:**
   ```bash
   grdapi tail_universal_connector_log lines=200
   ```

4. **Restart with Overwrite:**
   ```bash
   grdapi run_universal_connector overwrite_old_instance="true"
   ```

#### Issue 2: Cannot Connect to EntireX Broker

**Symptoms:**
- Log message: "Failed to connect to broker"
- Log message: "Connection refused"
- No data flowing to Guardium

**Solutions:**

1. **Verify Network Connectivity:**
   ```bash
   # From Guardium CLI
   telnet <broker_host> <broker_port>
   ```

2. **Check Firewall Rules:**
   - Ensure Guardium can reach broker port (default: 3000)
   - Check both outbound (Guardium) and inbound (broker) rules

3. **Verify Broker is Running:**
   ```bash
   # On broker server
   etbinfo -b <broker_id>
   ```

4. **Check Broker Configuration:**
   - Verify broker class, server, and service names
   - Ensure service is registered and active

5. **Test with Broker Tools:**
   - Use EntireX tools to verify broker accessibility
   - Test authentication credentials

#### Issue 3: Authentication Failures

**Symptoms:**
- Log message: "Authentication failed"
- Log message: "Invalid credentials"
- Connection established but no data received

**Solutions:**

1. **Verify Credentials:**
   - Check username and token are correct
   - Ensure no extra spaces or special characters

2. **Check Keystore Values:**
   ```bash
   grdapi universal_connector_keystore_list
   ```
   Verify keys exist and are spelled correctly.

3. **Test Credentials Manually:**
   - Use EntireX tools to test authentication
   - Verify user has permissions to access audit service

4. **Update Credentials:**
   ```bash
   # Remove old key
   grdapi universal_connector_keystore_remove key=ADABAS_BROKER_TOKEN
   
   # Add new key
   grdapi universal_connector_keystore_add \
     key=ADABAS_BROKER_TOKEN \
     password=new_token
   
   # Restart UC
   grdapi run_universal_connector overwrite_old_instance="true"
   ```

#### Issue 4: No Audit Data Appearing

**Symptoms:**
- Universal Connector is running
- Connection to broker successful
- No records in Guardium reports

**Solutions:**

1. **Verify Audit Data is Being Generated:**
   - Check Adabas Auditing Server is running
   - Perform database operations
   - Verify audit events are published to broker

2. **Check Filter Processing:**
   ```bash
   # Enable debug logging
   grdapi run_universal_connector debug_level=3
   
   # Check logs for filter activity
   grdapi tail_universal_connector_log lines=500 | grep -i filter
   ```

3. **Look for Skipped Events:**
   ```bash
   # Check for skip tags
   grdapi tail_universal_connector_log lines=500 | grep -i skip
   ```

4. **Verify Source Field:**
   - Ensure filter `source` parameter matches input plugin output
   - Default is `adabas-auditing`

5. **Check for Parsing Errors:**
   ```bash
   # Look for parsing errors
   grdapi tail_universal_connector_log lines=500 | grep -i "parse\|error"
   ```

#### Issue 5: Incomplete or Missing Data Fields

**Symptoms:**
- Records appear in Guardium but missing information
- User names showing as empty
- Database names not populated

**Solutions:**

1. **Check Adabas Audit Configuration:**
   - Verify audit level captures required fields
   - Ensure user context is included in audit events

2. **Review Audit Event Structure:**
   - Check that CLNT (client) data is present
   - Verify ACBX (control block) data is included

3. **Check Metadata Configuration:**
   - If using metadata REST server, verify it's accessible
   - Check `restURL` parameter is correct

4. **Review Parser Logic:**
   - Check filter plugin logs for warnings
   - Verify all expected fields are being extracted

#### Issue 6: Performance Issues

**Symptoms:**
- High CPU usage on Guardium
- Slow report generation
- Delayed data processing

**Solutions:**

1. **Adjust Connection Parameters:**
   ```ruby
   input {
     adabas_auditing_input {
       # Increase wait time to reduce polling frequency
       waitTime => 60
       
       # Increase receive length for batch processing
       receiveLength => 65535
     }
   }
   ```

2. **Enable Compression:**
   ```ruby
   input {
     adabas_auditing_input {
       compression => 1  # Enable compression
     }
   }
   ```

3. **Filter Audit Events at Source:**
   - Configure Adabas to audit only necessary operations
   - Reduce audit detail level if appropriate

4. **Check Guardium Resources:**
   - Monitor CPU and memory usage
   - Consider adding more collectors for load balancing

#### Issue 7: Universal Connector Stops After Reboot

**Symptoms:**
- Universal Connector not running after Guardium restart
- Must manually enable after reboot

**Solution:**

After each Guardium reboot, restart the Universal Connector:

```bash
# SSH to Guardium
ssh guardium@<guardium_ip>

# Start Universal Connector
grdapi run_universal_connector
```

**Note:** This is expected behavior. Universal Connector must be manually started after system reboots.

### Diagnostic Commands

#### Check Universal Connector Status
```bash
grdapi get_universal_connector_status
```

#### View Recent Logs
```bash
# Last 100 lines
grdapi tail_universal_connector_log lines=100

# Last 500 lines with timestamps
grdapi tail_universal_connector_log lines=500
```

#### List Installed Plugins
```bash
grdapi list_universal_connector_plugins
```

#### List Connector Configurations
```bash
# Via Web UI: Setup → Tools and Views → Configure Universal Connector
```

#### Check Keystore Contents
```bash
grdapi universal_connector_keystore_list
```

#### Restart Universal Connector
```bash
# Normal restart
grdapi run_universal_connector

# Force restart (overwrite existing instance)
grdapi run_universal_connector overwrite_old_instance="true"

# Restart with debug logging
grdapi run_universal_connector debug_level=3
```

#### Stop Universal Connector
```bash
grdapi stop_universal_connector
```

### Log File Locations

When creating a MustGather, these log files are included:

- **Universal Connector Log:** `uc-logstash.log`
- **Logstash Standard Output:** `logstash_stdout_stderr.log`
- **Guardium System Logs:** Various system logs

### Getting Help

If you continue to experience issues:

1. **Create a MustGather:**
   - Go to: **Setup** → **Tools and Views** → **MustGather**
   - Select all relevant options
   - Include Universal Connector logs

2. **Contact IBM Support:**
   - Provide MustGather output
   - Include configuration details (sanitize sensitive data)
   - Describe the issue and steps to reproduce

3. **Check Documentation:**
   - [IBM Guardium Documentation](https://www.ibm.com/docs/en/guardium)
   - [Universal Connectors GitHub](https://github.com/IBM/universal-connectors)
   - Software AG Adabas documentation

---

## Appendix

### A. Glossary

| Term | Definition |
|------|------------|
| **Adabas** | A high-performance database management system by Software AG |
| **Adabas Auditing Server** | Component that captures and publishes Adabas audit events |
| **EntireX Broker** | Messaging middleware for communication between Adabas and Guardium |
| **Universal Connector** | Guardium framework for ingesting data from various sources |
| **Input Plugin** | Component that receives data from external sources |
| **Filter Plugin** | Component that parses and transforms data |
| **S-TAP** | Software Tap - traditional Guardium monitoring agent |
| **Guardium Record** | Standardized data structure for audit events in Guardium |
| **ACBX** | Adabas Control Block Extended - contains command details |
| **CLNT** | Client information block in Adabas audit data |

### B. Port Reference

| Port | Protocol | Purpose | Direction |
|------|----------|---------|-----------|
| 3000 | TCP | EntireX Broker (default) | Guardium → Broker |
| 8443 | HTTPS | Guardium Web UI | Admin → Guardium |
| 22 | SSH | Guardium CLI access | Admin → Guardium |

### C. File Locations

| File/Directory | Purpose |
|----------------|---------|
| `/tmp/` | Temporary location for plugin .gem files |
| Guardium internal | Universal Connector logs (accessed via grdapi) |
| Guardium internal | Plugin installation directory (managed by Guardium) |

### D. Related Documentation

- [Guardium Data Protection Documentation](https://www.ibm.com/docs/en/guardium)
- [Universal Connectors GitHub Repository](https://github.com/IBM/universal-connectors)
- [Developing Plugins for Guardium](https://github.com/IBM/universal-connectors/blob/main/docs/Guardium%20Data%20Protection/developing_plugins_gdp.md)
- [Configuring Universal Connector](https://github.com/IBM/universal-connectors/blob/main/docs/Guardium%20Data%20Protection/uc_config_gdp.md)
- Software AG Adabas Documentation
- Software AG EntireX Documentation

### E. Quick Reference Card

#### Essential Commands

```bash
# Enable Universal Connector
grdapi run_universal_connector

# Check status
grdapi get_universal_connector_status

# View logs
grdapi tail_universal_connector_log lines=100

# List plugins
grdapi list_universal_connector_plugins

# Stop Universal Connector
grdapi stop_universal_connector

# Restart with overwrite
grdapi run_universal_connector overwrite_old_instance="true"
```

#### Configuration Template

```ruby
input {
  adabas_auditing_input {
    host => "BROKER_HOST"
    port => BROKER_PORT
    brokerClass => "BROKER_CLASS"
    brokerServer => "BROKER_SERVER"
    brokerService => "BROKER_SERVICE"
    user => "${ADABAS_BROKER_USER}"
    token => "${ADABAS_BROKER_TOKEN}"
  }
}

filter {
  adabas_guardium_filter {
    source => "adabas-auditing"
  }
}
```

### F. Version History

| Version | Date | Changes |
|---------|------|---------|
| 1.0 | 2025-01 | Initial implementation guide |

---

## Support and Feedback

For questions, issues, or feedback regarding this implementation guide:

- **IBM Support:** Contact your IBM support representative
- **GitHub Issues:** [Universal Connectors Issues](https://github.com/IBM/universal-connectors/issues)
- **Documentation Updates:** Submit pull requests to improve this guide

---

**Document Information:**
- **Title:** Adabas Universal Connector Implementation Guide
- **Audience:** Guardium administrators, Database administrators
- **Prerequisites:** Basic knowledge of Guardium, Adabas, and networking
- **Estimated Implementation Time:** 2-4 hours (excluding Adabas configuration)

---

*This guide is provided as-is and may be updated as new versions of Guardium or the Adabas plugins are released. Always refer to the latest official IBM and Software AG documentation for the most current information.*