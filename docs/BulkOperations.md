# Bulk Profile Management APIs

This set of API endpoints enables bulk operations on data source profiles including creation, updating, deletion, export, and template retrieval. These operations streamline large-scale profile management for customers.

## Table of Contents

- [Bulk Profile Creation](#bulk-profile-creation)
- [Bulk Delete Profiles](#bulk-delete-profiles)
- [Bulk Install Profiles](#bulk-install-profiles)
- [Bulk Uninstall Profiles](#bulk-uninstall-profiles)
- [Bulk Reinstall Profiles](#bulk-reinstall-profiles)
- [Export Profiles](#export-profiles)
- [Get Profile Template](#get-profile-template)

---

## Bulk Profile Creation

**Endpoint:** `universal_connector_import_profiles`  
**Method:** POST  
**Use Case:** Create multiple profiles in bulk using an uploaded file.

### Input Parameters

| Name | Type | Description |
|------|------|-------------|
| `path` | String | Path to the uploaded CSV file |
| `update_mode` | Boolean | If true, existing profiles will be updated instead of created |

### Example Usage

```bash
grdapi universal_connector_import_profiles path=/path/to/file.csv update_mode=true
```

> **Note:** Updating existing profiles is handled by setting `update_mode=true`

---

## Bulk Delete Profiles

**Endpoint:** `universal_connector_bulk_delete`  
**Method:** POST  
**Use Case:** Delete multiple profiles at once.

### Input Parameters

| Name | Type | Description |
|------|------|-------------|
| `profile_names` | List<String> | List of profile names to delete |

### Example Usage

```bash
grdapi universal_connector_bulk_delete profileNames=profile_a,profile_b,profile_c
```

---

## Bulk Install Profiles

**Endpoint:** `universal_connector_bulk_install`  
**Method:** POST  
**Use Case:** Install specified profiles on given hosts.

### Input Parameters

| Name | Type | Description |
|------|------|-------------|
| `profile_names` | List<String> | List of profile names to install |
| `hosts` | List<String> | List of hostnames |

> **Note:** If there is no host specified, the profiles will be installed on all of the deployed hosts.

### Example Usage

```bash
grdapi universal_connector_bulk_install profileNames=profile_a,profile_b hosts=host1,host2
```

---

## Bulk Uninstall Profiles

**Endpoint:** `universal_connector_bulk_uninstall`  
**Method:** POST  
**Use Case:** Uninstall multiple profiles from specified hosts.

### Input Parameters

| Name | Type | Description |
|------|------|-------------|
| `profile_names` | List<String> | List of profile names to uninstall |
| `hosts` | List<String> | List of hostnames |

> **Note:** If there is no host specified, the profiles will be uninstalled from all of the deployed hosts.

### Example Usage

```bash
grdapi universal_connector_bulk_uninstall profileNames=profile_a,profile_b hosts=host1,host2
```

---

## Bulk Reinstall Profiles

**Endpoint:** `universal_connector_reinstall_profiles`
**Method:** POST
**Use Case:** Reinstall all installed UC profiles on the collectors they are already installed on.

### Input Parameters

This endpoint does not require any input parameters. It automatically reinstalls all profiles on their respective collectors.

### Example Usage

```bash
grdapi universal_connector_reinstall_profiles
```

---

## Export Profiles

**Endpoint:** `universal_connector_export_profiles`  
**Method:** POST  
**Use Case:** Export one or more profile configurations.

### Input Parameters

| Name | Type | Description |
|------|------|-------------|
| `profile_names` | List<String> | List of profile names to export |

### Example Usage

```bash
grdapi universal_connector_export_profiles profileNames=profile_a,profile_b
```

---

## Get Profile Template

**Endpoint:** `universal_connector_get_profile_template`  
**Method:** POST  
**Use Case:** Retrieve a profile creation template.

### Input Parameters

| Name | Type | Description |
|------|------|-------------|
| `profile_names` | String | The profile name to create |
| `credential` | String | Credentials |
| `cluster_name` | String | The cluster name |
| `upload_name` | String | Name of the template |

### Example Usage

```bash
grdapi universal_connector_get_profile_template profile_names=my_profile credential=my_creds cluster_name=my_cluster upload_name=my_template