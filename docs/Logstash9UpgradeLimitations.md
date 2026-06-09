# Logstash 9.3.3 Upgrade Guide

## Overview

This document provides guidance for applying patch 5008, which upgrades the Universal Connector from Logstash 8.3.3 to Logstash 9.3.3.

**Important Considerations:**
- Logstash 9.3.3 introduces changes to SSL/TLS configuration parameters
- Configuration updates are required for Universal Connector deployments
- Both Legacy Flow and Central Manager Flow are affected

**Impact by Deployment Type:**
- **Legacy Flow:** Requires manual updates to Logstash configuration files with new parameter names
- **Central Manager Flow:** Requires Universal Connector profile reinstallation after applying patch 5008

## Configuration Parameter Changes

Logstash 9.3.3 has updated SSL/TLS configuration parameters in the TCP input plugin. The following parameter names have changed:

### Parameter Name Changes

| Old Parameter (Logstash 8.3.3) | New Parameter (Logstash 9.3.3) | Description |
|---------------------------------|--------------------------------|-------------|
| `ssl_enable` | `ssl_enabled` | Enable/disable SSL |
| `ssl_cert` | `ssl_certificate` | Path to SSL certificate |
| `ssl_verify` | **Deprecated/Removed** | SSL verification (replaced by `ssl_client_authentication`) |

**Note:** The following parameters remain unchanged:
- `ssl_key`
- `ssl_key_passphrase`
- `ssl_client_authentication`

The `ssl_verify` parameter has been deprecated in Logstash 9.3.3 and replaced with `ssl_client_authentication`.

**Current Support:**
- Guardium Universal Connectors support `ssl_client_authentication => "none"` (equivalent to `ssl_verify => false`)
- The values `"optional"` and `"required"` are not currently supported

**Deployment-Specific Impact:**

**Legacy Flow:**
- Configuration files must be manually updated with the new parameter names
- This document provides the complete migration guide

**Central Manager Flow:**
- After applying patch 5008, the Universal Connector profiles must be reinstalled through the Central Manager UI
- The SSL verification UI option has been updated in GDP 12.2.3
- Fresh installations of GDP 12.2.3 use `ssl_client_authentication => "none"` for SSL connections
- When applying patch 5008, the `ssl_verify` checkbox is replaced with a textbox that can be left empty or set to `none`

## Affected Connectors

The following Universal Connectors are affected by these changes:

### 1. MongoDB over Syslog
- **Configuration File:** `filter-plugin/logstash-filter-mongodb-guardium/MongoDBOverSyslogPackage/mongodbSyslog.conf`
- **Impact:** TCP input configuration requires parameter updates

### 2. PostgreSQL over Syslog (EDB)
- **Configuration File:** `filter-plugin/logstash-filter-onPremPostgres-guardium/PostgresOverSyslogPackage/PostgresEDB/filter.conf`
- **Impact:** TCP input configuration requires parameter updates

### 3. CockroachDB over Syslog
- **Configuration File:** `filter-plugin/logstash-filter-cockroachdb-guardium/CockroachDBOverSyslogPackage/CockroachDBOverSyslog.conf`
- **Impact:** TCP input configuration requires parameter updates

### 4. Milvus over Filebeat
- **Configuration File:** `filter-plugin/logstash-filter-milvus-guardium/milvusOverFilebeat.conf`
- **Impact:** Beats input configuration requires parameter updates

### 5. MySQL Percona over Filebeat
- **Configuration File:** `filter-plugin/logstash-filter-mysql-percona-guardium/perconaFilebeat.conf`
- **Impact:** Beats input configuration requires parameter updates

### 6. TCP Syslog Input Plugin
- **Documentation:** `input-plugin/logstash-input-tcp-syslog/README.md`
- **Impact:** Documentation and examples updated

### 7. Beats Input Plugin
- **Configuration File:** `input-plugin/logstash-input-beats/FilebeatInputPackage/Filebeat/input.conf`
- **Documentation:** `input-plugin/logstash-input-beats/README.md`
- **Impact:** Configuration and documentation updated

## Migration Guide

### For Legacy Flow Deployments

When applying **patch 5008** or performing a **fresh install of GDP 12.2.3**, the Universal Connector will use Logstash 9.3.3. Configuration files must be manually updated with the new parameter names after the upgrade.


#### Step 1: Identify Affected Configurations

Review all your Universal Connector configurations that use TCP or Beats input plugins with SSL/TLS enabled.

#### Step 2: Update Configuration Parameters

Replace the deprecated parameters with their new equivalents:

**Before (Logstash 8.3.3):**
```conf
input {
  tcp {
    port => 6514
    ssl_enable => true
    ssl_cert => "${SSL_DIR}/tls.crt"
    ssl_key => "${SSL_DIR}/tls.key"
    ssl_key_passphrase => "${ssl_key_passphrase}"
    ssl_verify => false
  }
}
```

**After (Logstash 9.3.3):**
```conf
input {
  tcp {
    port => 6514
    ssl_enabled => true
    ssl_certificate => "${SSL_DIR}/tls.crt"
    ssl_key => "${SSL_DIR}/tls.key"
    ssl_key_passphrase => "${ssl_key_passphrase}"
    ssl_client_authentication => "none"
  }
}
```

#### Step 3: Test Configuration

Before deploying to production:

1. Validate your installed UC configuration:

2. Test with sample data in a non-production environment

3. Verify SSL/TLS connections are working correctly


### For Central Manager (CM Flow) Deployments

After applying **patch 5008**, all datasource profiles must be reinstalled to ensure they are using the updated Logstash 9.3.3 configuration:

1. Navigate to **Manage > Universal Connector > Datasource Profile Management**
2. Select all the profiles
3. Follow the profile reinstallation process through the Central Manager UI


**Important Note for Syslog Profiles:**
For syslog-based profiles, you **must** edit and save the profile before reinstalling it. This ensures that the profile configuration is properly updated with the new Logstash 9.3.3 parameter names. Simply reinstalling without editing and saving first may not apply the necessary configuration updates.

**Note:** Profile reinstallation is necessary to ensure Universal Connector configurations are properly updated to work with Logstash 9.3.3 after applying patch 5008.

## Compatibility Notes

Configuration changes introduced in Logstash 9.3.3 are not backward compatible:

- Old parameter names (`ssl_enable`, `ssl_cert`, `ssl_verify`) are not supported in Logstash 9.3.3
- Configurations must be updated appropriately for GDP version 12.2.3+ or Patch 5008

## Post-Upgrade Validation

After applying patch 5008, verify the following:

- [ ] TCP input configurations use updated parameter names
- [ ] Beats input configurations use updated parameter names
- [ ] SSL/TLS connections are functioning correctly
- [ ] (Central Manager only) Universal Connector profiles reinstalled

## Troubleshooting

### Common Issues

#### Issue 1: Configuration Validation Fails
**Symptom:** Logstash fails to start with configuration errors related to SSL parameters

**Solution:**
- Verify all `ssl_enable` instances are changed to `ssl_enabled`
- Verify all `ssl_cert` instances are changed to `ssl_certificate`
- Remove or replace `ssl_verify` with `ssl_client_authentication => "none"`
  - Note: Only `"none"` is currently supported by Guardium Universal Connectors

#### Issue 2: SSL/TLS Connection Failures
**Symptom:** Data sources cannot connect to Logstash

**Solution:**
- Verify certificate paths are correct
- Ensure `ssl_enabled => true` is set
- Verify `ssl_client_authentication => "none"` is configured (currently the only supported value)
- For Central Manager deployments, ensure all profiles have been reinstalled
- For patch 5008, the `ssl_verify` textbox can be left empty or set to `none`

#### Issue 3: No Data Flowing After Upgrade
**Symptom:** Logstash starts but no events are received

**Solution:**
- Verify the Universal Connector is up and running
- For Central Manager deployments, ensure all profiles have been reinstalled through the UI

**Applies To:**
- Guardium Data Protection 12.2.3 and above (includes Logstash 9.3.3)
- Patch 5008 (Logstash 9.3.3)
- Logstash 9.3.3 and above (standalone installations)
**Previous Versions:**
- GDP < 12.2.3 (Logstash 8.3.3)