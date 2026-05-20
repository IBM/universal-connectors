# Logstash 9.3.4 Upgrade Limitations and Breaking Changes

## Overview

This document outlines the limitations and breaking changes that customers need to be aware of when upgrading to **Guardium Data Protection (GDP) version 12.2.3**.

**IMPORTANT:** GDP 12.2.3 includes an upgrade from Logstash 8.3.3 to Logstash 9.3.4. This Logstash upgrade introduces breaking changes in SSL/TLS configuration parameters that will impact Universal Connector configurations.

**Who is Impacted:**
- **Legacy Flow Customers:** Must manually update their Logstash configuration files with the new parameter names
- **Central Manager Flow Customers (Syslog):** The SSL verification UI option will be removed in GDP 12.2.3. SSL connections will default to `ssl_client_authentication => "none"` (no client authentication required)

## Breaking Changes in TCP Input Plugin

Logstash 9.3.4 has deprecated certain SSL/TLS configuration parameters in the TCP input plugin. The following parameter names have changed:

### Parameter Name Changes

| Old Parameter (Logstash 8.3.3) | New Parameter (Logstash 9.3.4) | Description |
|---------------------------------|--------------------------------|-------------|
| `ssl_enable` | `ssl_enabled` | Enable/disable SSL |
| `ssl_cert` | `ssl_certificate` | Path to SSL certificate |
| `ssl_verify` | **Deprecated/Removed** | SSL verification (replaced by `ssl_client_authentication`) |

**Note:** The following parameters remain unchanged:
- `ssl_key`
- `ssl_key_passphrase`
- `ssl_client_authentication`

**Important:** The `ssl_verify` parameter has been deprecated in Logstash 9.3.4.

**Guardium Universal Connectors currently only support:**
- `ssl_client_authentication => "none"` (equivalent to the old `ssl_verify => false`)

**Note:** The values `"optional"` and `"required"` for `ssl_client_authentication` are not currently supported by Guardium Universal Connectors.

**Deployment-Specific Impact:**

**Legacy Flow:**
- Customers must manually update their configuration files with the new parameter names
- This document provides the complete migration guide for these updates

**Central Manager Flow (Syslog):**
- **Breaking Change:** The SSL verification option in the Central Manager UI will be **removed** in GDP 12.2.3
- Previously, Central Manager provided a UI option to configure SSL verification for Syslog connectors
- After upgrade, SSL connections will automatically use `ssl_client_authentication => "none"` (no client authentication)
- This change is due to Guardium Universal Connectors currently only supporting `ssl_client_authentication => "none"` (the `"optional"` and `"required"` values are not supported)

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

### For Legacy Flow Customers Upgrading to GDP 12.2.3 (Logstash 8.3.3 → 9.3.4)

The Logstash version will be automatically upgraded as part of the GDP upgrade process. **You must manually update your configuration files** with the new parameter names.


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

**After (Logstash 9.3.4):**
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

#### Step 4: Update Documentation

Update any internal documentation or runbooks that reference the old parameter names.

## Backward Compatibility

**Important:** These configuration changes are **NOT backward compatible**.

- Configurations using old parameter names (`ssl_enable`, `ssl_cert`, `ssl_verify`) will **fail** on Logstash 9.3.4
- Configurations using new parameter names (`ssl_enabled`, `ssl_certificate`) will **fail** on Logstash 8.3.3 and earlier versions
- The `ssl_verify` parameter is no longer supported in Logstash 9.3.4 and must be replaced with `ssl_client_authentication`

### Recommendation for Mixed Environments

If you have a mixed environment with different Logstash versions:

1. **Option 1 (Recommended):** Upgrade all GDP instances to version 12.2.3 (which includes Logstash 9.3.4) before updating configurations
2. **Option 2:** Maintain separate configuration files for different GDP/Logstash versions
3. **Option 3:** Use environment-specific configuration management tools to deploy appropriate configurations

**Note:** For GDP 12.2.3, the Logstash upgrade is automatic and mandatory. Plan your configuration updates accordingly.

## Validation Checklist

Before completing your upgrade to GDP 12.2.3, verify:

- [ ] All TCP input configurations updated with new parameter names
- [ ] All Beats input configurations updated with new parameter names
- [ ] SSL/TLS connections tested and working
- [ ] Configuration validation passes (`--config.test_and_exit`)
- [ ] Data flow verified in test environment
- [ ] Guardium receiving and processing events correctly
- [ ] Internal documentation updated
- [ ] Team members notified of changes
- [ ] Backup of existing configurations created before upgrade

## Troubleshooting

### Common Issues

#### Issue 1: Configuration Validation Fails
**Symptom:** Logstash fails to start with configuration errors related to SSL parameters
**Solution:**
- Verify all `ssl_enable` changed to `ssl_enabled`
- Verify all `ssl_cert` changed to `ssl_certificate`
- Remove or replace `ssl_verify` with `ssl_client_authentication => "none"`
  - Note: Only `"none"` is currently supported by Guardium Universal Connectors

#### Issue 2: SSL/TLS Connection Failures
**Symptom:** Data sources cannot connect to Logstash
**Solution:**
- Verify certificate paths are correct
- Check file permissions on certificate files
- Ensure `ssl_enabled => true` (not `false`)
- Verify `ssl_client_authentication => "none"` is set (this is the only supported value)
- **Note:** This applies to legacy flow deployments. 
-  For Central Manager deployments, SSL verification options are now not configurable through the UI

#### Issue 3: No Data Flowing After Upgrade
**Symptom:** Logstash starts but no events are received
**Solution:**
- Check Logstash logs for connection errors
- Verify data source is sending to correct port
- Test connectivity: `telnet <logstash-host> <port>`
- For SSL: Verify certificate chain is valid

## Support

For additional assistance:
- Review the [Logstash 9.3.4 Release Notes](https://www.elastic.co/guide/en/logstash/9.3/releasenotes.html)
- Consult the [Universal Connectors Documentation](https://github.com/IBM/universal-connectors)
- Contact IBM Security Guardium Support

## Version History

| Version | Date | Changes |
|---------|------|---------|
| 1.0 | 2026-05-20 | Initial documentation for Logstash 9.3.4 upgrade |

---

**Applies To:**
- Guardium Data Protection 12.2.3 and above (includes Logstash 9.3.4)
- Logstash 9.3.4 and above (standalone installations)
**Previous Versions:**
- GDP < 12.2.3 (Logstash 8.3.3)