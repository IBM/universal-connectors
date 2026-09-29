# Configuring NetApp ONTAP NAS datasource profiles for Kafka Connect plug-ins

You can create and configure datasource profiles for the **NetApp ONTAP NAS Over Kafka Connect 2.0** plug-in through central manager.

> **Note:** The ONTAP configuration steps in the following procedures are for reference only. Command syntax, available parameters, and default values vary by ONTAP release. For version-specific procedures, see the [NetApp ONTAP documentation](https://docs.netapp.com/us-en/ontap/). 

## Meet NetApp ONTAP NAS over Kafka Connect

* Tested versions: ONTAP 9.10 and later
* Environment: On-premises
* Supported inputs: Kafka Connect 2.0 (agentless REST pull)
* Supported Guardium versions:
* Guardium Data Protection: 12.2.4 and later

NAS file access audit events are collected from the NetApp ONTAP system through the ONTAP REST API (HTTPS, port 443). One raw XML `<Event>` record is published per Kafka message. No software is installed on the ONTAP system, and no NFS, SMB mount, or log-shipping agent is required.

**What this plug-in captures:** ONTAP `vserver audit` NAS file access events for SMB/CIFS and NFS clients, including file open, close, read, write, delete, hard link, and permission change operations. ONTAP emits these using a Windows Security Event schema (event IDs 4656, 4658, 4659, 4660, 4663, 4664, 4670, and related).

**What this plug-in does not capture:** ONTAP management and administrative audit log entries (`security audit log`). This plug-in reads data-plane end-user file activity only.

---

## Audit event ingestion pipeline

```
NAS client (SMB/CIFS or NFS)
  |  file operations that match an audit policy trigger audit records
  v
ONTAP per-node staging files (binary)
  |  consolidated and converted when a rotation schedule fires
  |  or the rotation size limit is reached
  v
audit_<svm>_D<timestamp>_<seq>.xml  (in the audit destination volume)
  |  one <Event> XML element per audited file operation
  v
Guardium connector polls every poll.interval.seconds
  |  lists completed files over the ONTAP REST API
  |  downloads them over HTTPS and splits each into <Event> records
  v
Kafka topic  ->  Sink connector  ->  Guardium MU (parsed by NetappOntapParser)
```

The newest file in the destination volume is skipped at every poll because ONTAP might still be writing to it. Only completed (rotated) files are ingested. Kafka Connect offsets track the exact position (`last_file`, `event_index`), which provides at-least-once delivery: uncommitted events may be re-emitted after a crash, but none are skipped.

For more information about the staging, consolidation, and rotation pipeline, see [Learn about the functioning of the ONTAP auditing process](https://docs.netapp.com/us-en/ontap/nas-audit/auditing-process-concept.html).

---

## Before you begin

Command syntax, default values, and available parameters vary by ONTAP release. Environments such as MetroCluster, SVM disaster recovery, multi-node clusters, Active Directory joined CIFS servers, and non-default RBAC configurations might require different steps. For version-specific procedures, see [NetApp ONTAP documentation](https://docs.netapp.com/us-en/ontap/) or [NetApp reference documentation](#netapp-reference-documentation).

**Note:** If NAS auditing is already configured in your ONTAP environment, no changes might be required. The connector depends only on the resulting SVM state. Use the following checklist to confirm whether any action is required.

### Requirements checklist

A datasource profile works with any SVM that meets the following conditions.

| # | Condition | Verify with |
|---|---|---|
| 1 | The cluster management LIF is reachable from the Kafka Connect host on TCP port 443 | `nc -w 5 <management_lif> 443` |
| 2 | An ONTAP account exists with `http` application access and read-only rights to `/api/cluster`, `/api/protocols/audit`, `/api/storage/volumes`, and `/api/storage/volumes/*/files` | `curl -sk -u '<user>:<password>' https://<management_lif>/api/cluster` |
| 3 | NAS auditing is configured and enabled on the SVM | `vserver audit show -vserver <svm_name>` returns `Auditing State: true` |
| 4 | The audit log format is `xml`. The ONTAP default, `evtx`, is not supported | `vserver audit show -vserver <svm_name>` returns `Log Format: xml` |
| 5 | Completed audit files are written to the volume named by the audit `log_path`, using the ONTAP standard `audit_<svm>_D<timestamp>_<seq>.xml` naming convention | `volume show -vserver <svm_name> -volume <log_path_volume>` |
| 6 | The audited data has an audit policy that its security style supports: an NTFS SACL on `ntfs` or `mixed` volumes, or an NFSv4.x audit ACE on `unix` volumes | `vserver security file-directory show -vserver <svm_name> -path /<data_volume>` |
| 7 | A rotation schedule is configured so that events reach completed files predictably | `vserver audit show -vserver <svm_name>` shows values under `Log Rotation Schedule` |

Conditions 1 to 5 are validated by **Test Connection** and enforced at connector startup. Conditions 6 and 7 cannot be validated remotely and are the most common cause of a connector that is working but reports no events.

Everything else, including the destination path, the audit event category list, the rotation cadence, and the retention limit, can be optionally configured based on your requirements. Rotation cadence directly affects Guardium because it determines ingestion latency. For more information, see [Step 3](#3-configuring-an-audit-log-rotation-schedule).

No steps in the following procedures require a reboot or service interruption.

---

## 1. Creating an ONTAP service account

The connector authenticates to the ONTAP REST API by using HTTP basic authentication over HTTPS. A dedicated, least-privilege, read-only service account is required. Do not use the cluster `admin` account.

The account requires read-only access to the following four REST endpoints, which are the only endpoints the connector uses:

| Endpoint | Used for |
|---|---|
| `/api/cluster` | Connectivity and credential validation |
| `/api/protocols/audit` | Reading the audit configuration and resolving the destination volume |
| `/api/storage/volumes` | Resolving the destination volume UUID |
| `/api/storage/volumes/*/files` | Listing and downloading completed audit files |

Choose one of the following methods based on your ONTAP release.

### Method A: REST role by using the CLI (ONTAP 9.11.1 and later, recommended)

Run the following commands as a cluster administrator on the cluster management LIF.

```
security login rest-role create -role guardium-rest -api /api/cluster -access readonly
security login rest-role create -role guardium-rest -api /api/protocols/audit -access readonly
security login rest-role create -role guardium-rest -api /api/storage/volumes -access readonly

security login create -user-or-group-name guardium_svc \
  -application http \
  -authmethod password \
  -role guardium-rest
```

For more information, see the following **NetApp documentation**. 
* [security login rest-role create](https://docs.netapp.com/us-en/ontap-cli/security-login-rest-role-create.html)
* [Work with roles and users in the ONTAP REST API](https://docs.netapp.com/us-en/ontap-automation/rest/rbac_roles_users.html)
* [Define custom roles for ONTAP administrators](https://docs.netapp.com/us-en/ontap/authentication/define-custom-roles-task.html)

### Method B: REST role using the REST API

Use this method when the account is provisioned from an automation pipeline rather than the CLI.

```bash
# Get the cluster owner UUID
OWNER_UUID=$(curl -sk -u 'admin:<password>' https://<management_lif>/api/cluster \
  | python3 -c "import sys,json; print(json.load(sys.stdin)['uuid'])")

# Create the role
curl -sk -u 'admin:<password>' -X POST \
  "https://<management_lif>/api/security/roles" \
  -H 'Content-Type: application/json' \
  -d '{
    "name": "guardium-rest",
    "owner": {"uuid": "'"$OWNER_UUID"'"},
    "privileges": [
      {"path": "/api/cluster",                 "access": "readonly"},
      {"path": "/api/protocols/audit",         "access": "readonly"},
      {"path": "/api/storage/volumes",         "access": "readonly"},
      {"path": "/api/storage/volumes/*/files", "access": "readonly"}
    ]
  }'

# Create the service account
curl -sk -u 'admin:<password>' -X POST \
  "https://<management_lif>/api/security/accounts" \
  -H 'Content-Type: application/json' \
  -d '{
    "owner": {"uuid": "'"$OWNER_UUID"'"},
    "name": "guardium_svc",
    "role": {"name": "guardium-rest"},
    "applications": [{"application": "http", "authentication_methods": ["password"]}],
    "password": "<service_account_password>"
  }'
```

For more information, see [Manage security roles (REST API)](https://docs.netapp.com/us-en/ontap-restapi/security_roles_endpoint_overview.html) in the NetApp documentation.

### Method C: Traditional CLI-mapped role (ONTAP 9.10 and 9.11)

Use this option only on releases that do not support REST roles.

```
security login role create -vserver <cluster_name> -role guardium-readonly \
  -cmddirname "DEFAULT" -access none
security login role create -vserver <cluster_name> -role guardium-readonly \
  -cmddirname "cluster identity" -access readonly
security login role create -vserver <cluster_name> -role guardium-readonly \
  -cmddirname "vserver audit" -access readonly
security login role create -vserver <cluster_name> -role guardium-readonly \
  -cmddirname "volume" -access readonly

security login create -vserver <cluster_name> \
  -user-or-group-name guardium_svc \
  -application http \
  -authmethod password \
  -role guardium-readonly
```

**Note:** On ONTAP 9.12 and later, the REST authorization layer might return HTTP 403 for `/api/cluster` when the account uses a traditional CLI-mapped role. If the **Test Connection** fails with an authentication error after this role is created, use method A or B instead.

For more information, see [security login role create](https://docs.netapp.com/us-en/ontap-cli/security-login-role-create.html) in the NetApp documentation.

### Verifying the account

Run the following command from the host that runs Kafka Connect.

```bash
curl -sk -u 'guardium_svc:<password>' https://<management_lif>/api/cluster \
  | python3 -m json.tool
```

The response must include `"name": "<cluster_name>"`. An empty response or a connection error indicates that port 443 is not reachable from the Kafka Connect host.

---

## 2. Enabling NAS auditing on the SVM in XML format

The connector reads ONTAP's converted XML audit event logs. ONTAP supports two output formats, `evtx` and `xml`, and **the default is `evtx`, which this connector does not support.** Auditing must be configured with `-format xml`.

1. Verify whether auditing is already configured by running the following command.

```
vserver audit show -vserver <svm_name>
```

2. If auditing is not configured, create it by using the following reference example. Choose the destination volume and event category list to suit your environment.

```
vserver audit create \
  -vserver <svm_name> \
  -destination /vol_audit_log \
  -format xml \
  -events file-ops,cifs-logon-logoff,security-group,user-account,authorization-policy-change

vserver audit enable -vserver <svm_name>
```
**Note:**

* You can specify any valid destination path. The connector reads the audit `log_path` from the ONTAP REST API and resolves the volume automatically. The volume name is not configured in the datasource profile.
* Of the event categories shown, `file-ops` produces file access records. The remaining categories add logon, account, and policy change events, which the connector also forwards. Include only the categories that your audit requirements call for.
* The destination directory must already exist, must not contain symbolic links, and must be given as an absolute path.
  
3. If auditing is already configured but uses the `evtx` format, change the format by running the following command.

```
vserver audit modify -vserver <svm_name> -format xml
```

4. Verify the result by running the following command.

```
vserver audit show -vserver <svm_name>
```

The output must show `Auditing State: true` and `Log Format: xml`.


For more information, see the following NetApp documentation:
* [Create a file and directory auditing configuration](https://docs.netapp.com/us-en/ontap/nas-audit/create-auditing-config-task.html) 
* [Enable or disable auditing on an SVM](https://docs.netapp.com/us-en/ontap/nas-audit/enable-disable-auditing-svms-task.html) 
* [Supported audit event log formats](https://docs.netapp.com/us-en/ontap/nas-audit/supported-audit-event-log-formats-concept.html)
* [Prerequisites for ONTAP auditing](https://docs.netapp.com/us-en/ontap/nas-audit/requirements-considerations-concept.html) 
* [vserver audit create](https://docs.netapp.com/us-en/ontap-cli/vserver-audit-create.html)

---

## 3. Configuring an audit log rotation schedule

ONTAP does not write audit events to completed log files continuously. Records accumulate in per-node staging files and are consolidated into a completed file only when the rotation size limit is reached or a rotation schedule fires. By default, ONTAP rotates on size by using a 100 MB limit. On a lightly loaded SVM, this can leave events in staging for hours or days before the connector can read them.

**Rotation cadence is the primary control over Guardium ingestion latency.**

| Rotation schedule | Worst-case delay before an event reaches Guardium |
|---|---|
| Every 1 minute | Approximately 2 minutes |
| Every 5 minutes | Approximately 6 minutes |
| Every 15 minutes | Approximately 16 minutes |
| Size-based only, no schedule | Hours to days. Not suitable for real-time monitoring |

1. Configure the rotation schedule as shown in the following example.
The following reference example configures rotation every 5 minutes by setting `-rotate-schedule-minute` to fire at every 5-minute interval within the hour.

```
vserver audit modify -vserver <svm_name> \
  -rotate-schedule-minute 0,5,10,15,20,25,30,35,40,45,50,55
```
**Note:**

* `-rotate-schedule-minute` is mandatory when time-based rotation is used. The other schedule parameters (`-rotate-schedule-month`, `-rotate-schedule-dayofweek`, `-rotate-schedule-day`, `-rotate-schedule-hour`) are optional and can be combined.
* Size-based rotation with `-rotate-size` remains active alongside a schedule. A busy SVM might rotate more often than the schedule specifies, which is expected.
* Set -rotate-limit so that retained files outlast any planned Guardium outage. If ONTAP deletes files before the connector reads them, the connector logs a retention gap warning and those events are lost. For more information, see [Troubleshooting](#connector-reports-a-retention-gap-warning).

2. Set a retention limit to prevent the audit destination volume from filling up.

```
# Retain the last 48 completed log files
vserver audit modify -vserver <svm_name> -rotate-limit 48
```

For more information, see the following NetApp documentation:
* [Plan the auditing configuration](https://docs.netapp.com/us-en/ontap/nas-audit/plan-auditing-config-concept.html) 
* [vserver audit modify](https://docs.netapp.com/us-en/ontap-cli/vserver-audit-modify.html) 
* [Manually rotate the audit event logs](https://docs.netapp.com/us-en/ontap/nas-audit/manual-rotate-audit-event-logs-task.html)

---

## 4. Sizing the audit destination volume

Auditing depends on available space in both the per-node staging volumes and the volume that holds the converted event logs. If the destination volume fills, ONTAP stops writing audit events when the volume has `guarantee=true`, and might drop them silently when it has `guarantee=false`.

To check the available space, run the following command.

```
volume show -vserver <svm_name> -volume <audit_volume> \
  -fields size,available,percent-used
```

Size the volume to accommodate the chosen rotation cadence and retention limit, with additional space for activity spikes.

For more information, see the following NetApp documentation:
* [Prerequisites for ONTAP auditing](https://docs.netapp.com/us-en/ontap/nas-audit/requirements-considerations-concept.html) 
* [Limitations on the size of audit records in staging files](https://docs.netapp.com/us-en/ontap/nas-audit/auditing-limitations-size-audit-records-concept.html)

---

## 5. Applying an audit policy to the data being audited

Enabling auditing on the SVM does not by itself cause any event to be recorded. ONTAP records an event only when the file or directory being accessed has an audit policy that matches the operation. This is the most common reason for a correctly configured connector that reports no events.

The mechanism depends on the security style of the data volume.

| Volume security style | Audit policy mechanism | Applied with |
|---|---|---|
| `ntfs` | NTFS SACL (system access control list) | Windows Explorer Security tab, or `vserver security file-directory` |
| `mixed` | NTFS SACL for NTFS effective style, NFSv4.x audit ACE for UNIX effective style | Either mechanism, depending on effective style |
| `unix` | NFSv4.x audit ACE | `nfs4_setfacl` from an NFSv4 client |

1. Most production environments already have this configuration in place, applied through the normal permissions workflow. Verify the existing configuration before making any changes by running the following command.

```
vserver security file-directory show -vserver <svm_name> -path /<data_volume>
```

2. Look for a SACL - ACEs section that contains AUDIT- entries. If the section is absent, no auditing occurs on that path regardless of the SVM audit configuration.

3. The following reference example applies a broad success and failure SACL to an `ntfs` or `mixed` style volume from the ONTAP CLI. In production, restrict this policy to the specific accounts and access rights based on your audit requirements, because auditing `Everyone` with `full-control` generates a high event volume.

```
vserver security file-directory ntfs create \
  -vserver <svm_name> -ntfs-sd audit_sacl_sd

vserver security file-directory ntfs dacl add \
  -vserver <svm_name> -ntfs-sd audit_sacl_sd \
  -access-type allow -account Everyone -rights full-control

vserver security file-directory ntfs sacl add \
  -vserver <svm_name> -ntfs-sd audit_sacl_sd \
  -access-type success -account Everyone -rights full-control \
  -apply-to this-folder,sub-folders,files

vserver security file-directory ntfs sacl add \
  -vserver <svm_name> -ntfs-sd audit_sacl_sd \
  -access-type failure -account Everyone -rights full-control \
  -apply-to this-folder,sub-folders,files

vserver security file-directory policy create \
  -vserver <svm_name> -policy-name audit_sacl_policy

vserver security file-directory policy task add \
  -vserver <svm_name> -policy-name audit_sacl_policy \
  -path /<data_volume> -ntfs-mode propagate -ntfs-sd audit_sacl_sd

vserver security file-directory apply \
  -vserver <svm_name> -policy-name audit_sacl_policy
```

**Note:**

* NTFS SACLs can be configured only through the CLI or through Windows. The `vserver security file-directory` command family does not configure NFSv4 SACLs.
* For `unix` security style data, audit ACEs are added to the NFSv4.x ACL by using `nfs4_getfacl` and `nfs4_setfacl` from an NFSv4 client. In NFSv4.x, discretionary and system ACEs share a single ACL, so the existing ACL must be retrieved and extended rather than replaced.
* NFSv3 has no ACL mechanism that can carry an audit ACE. A `unix` security style volume accessed only over NFSv3 produces no audit records. Where NFSv3 clients access an `ntfs` or `mixed` style volume, auditing follows the NTFS SACL and the UNIX user's mapped Windows identity.
* CIFS/SMB auditing requires a CIFS server on the SVM so that ONTAP can resolve SIDs. For more information, see [Verifying the pipeline](#verifying-the-pipeline).

For more information, see the following NetApp documentation:
* [Configure audit policies on NTFS security style files and directories](https://docs.netapp.com/us-en/ontap/nas-audit/configur-policies-ntfs-security-concept.html) 
* [Configure auditing for UNIX security style files and directories](https://docs.netapp.com/us-en/ontap/nas-audit/configure-auditing-unix-security-files-directories-task.html) 
* [Learn about ONTAP NAS security styles](https://docs.netapp.com/us-en/ontap/nfs-admin/security-styles-their-effects-concept.html) 
* [NAS file system auditing (security hardening technical report)](https://docs.netapp.com/us-en/ontap-technical-reports/ontap-security-hardening/filesystem-auditing.html)

---

## Creating datasource profiles

A new datasource profile is created from the **Datasource Profile Management** page.

### Procedure

1. Go to **Manage > Universal Connector > Datasource Profile Management**.
2. Click the **➕ (Add)** button.
3. A profile can be created by using one of the following methods.

    * To create a profile manually, go to the **Add Profile** tab and provide values for the following fields:
        * **Name** and **Description**.
        * Select a **Plug-in Type** from the dropdown. For example, `NetApp ONTAP NAS Over Kafka Connect 2.0`.

    * To upload from CSV, go to the **Upload from CSV** tab and upload an exported or manually created CSV file containing one or more profiles. The following options are also available:
        * **Update existing profiles on name match** — Updates profiles with the same name if they already exist.
        * **Test connection for imported profiles** — Automatically tests connections after profiles are created.
        * **Use ELB** — Enables ELB support for imported profiles. You must provide the number of Managed Units to be used in the ELB process.

**Note:** Configuration options vary based on the selected plug-in.

---

## Configuring NetApp ONTAP NAS Over Kafka Connect 2.0

### Profile fields

The following table describes the fields for the NetApp ONTAP NAS Over Kafka Connect 2.0 plug-in.

| Field | Description |
|---|---|
| **Name** | Unique name for the profile. |
| **Description** | Description of the profile. |
| **Plug-in** | Plug-in type for this profile. Select `NetApp ONTAP NAS Over Kafka Connect 2.0`. A full list of available plug-ins is on the **Package Management** page. |
| **Kafka Cluster** | Kafka cluster used to deploy the universal connector. For more information, see [Managing Kafka clusters](https://www.ibm.com/docs/en/SSMPHH_12.x/com.ibm.guardium.doc.stap/guc/guc_kafka_cluster_management.html). |
| **Label** | Grouping label. For example, a customer name or ID. |
| **Hostname** | ONTAP cluster management LIF IP address or hostname. Must be the cluster management LIF (`ontap.host`), not a data LIF or a node management LIF. |
| **SVM name(s)** | Comma-separated list of SVM names to monitor (`ontap.svm.names`). For example, `svm_nas` or `svm_nas,svm_finance`. One connector task is created per SVM, up to the configured task maximum. |
| **Cluster name** | Optional. Cluster name added to Kafka record headers and used in Guardium reports (`ontap.cluster.name`). |
| **Poll interval (seconds)** | How often the connector checks for new completed audit files (`poll.interval.seconds`). The minimum value is 10. Default is 60. |
| **Max events per poll** | Maximum number of audit events emitted per poll cycle (`max.events.per.poll`). Default is 5000. |
| **Start time (epoch ms)** | Applied on the first run only, before any offset is stored. Files with a timestamp earlier than this value are skipped (`start.from.time`). Leave at the default of `0` to start from the current time. For more information, see [Limitations](#limitations). |
| **No traffic threshold (minutes)** | Default is 60. The S-TAP status indicator turns red when no traffic is received for this duration. The status returns to green when traffic resumes. |
| **Use Enterprise Load Balancing (ELB)** | Enable if ELB support is required. |


**Notes:**

* The profile name must be unique within the Guardium environment.
* The Kafka cluster must be configured and accessible before the profile is created.
* The audit destination volume is resolved automatically from the ONTAP audit configuration and is not entered in the profile.

---

## Testing a connection

After a profile is created, test the connection to verify that the configuration is valid.

**Test Connection** performs the following checks in order:

1. Sends a `GET /api/cluster` request to confirm that the Kafka Connect host can reach the management LIF over HTTPS and that the credentials are valid.
2. For each configured SVM, sends a `GET /api/protocols/audit` request to confirm that an audit configuration exists, that auditing is enabled, and that the log format is `xml`.
3. Resolves the audit destination volume and confirms that it exists in the SVM.

A successful test confirms requirements 1 through 5 of the [requirements checklist](#requirements-checklist). It does not confirm that an audit policy is applied to your data or that a rotation schedule is configured. To verify the audit policy and rotation schedule, see [Verifying the pipeline](#verifying-the-pipeline).

### Procedure

1. Select the new profile.
2. From the top menu, click **Test Connection**.
3. If the test succeeds, continue to installing the profile.
4. If the test fails, verify the following:
   * The ONTAP management LIF is reachable from the Kafka Connect host on port 443.
   * The service account credentials are correct.
   * The service account role grants `readonly` access to the required API endpoints. For more information, see [Step 1](#1-create-an-ontap-service-account).
   * NAS auditing is enabled on every configured SVM and uses the `xml` format. For more information, see [Step 2](#2-enable-nas-auditing-on-the-svm-in-xml-format).

---

## Installing a profile

After the connection test succeeds, install the profile on Managed Units or Edges. Parsed audit records are forwarded to the selected Managed Unit or Edge for policy evaluation and auditing.

### Procedure

1. Select the profile.
2. From the **Install** menu, click **Install**.
3. From the list of available Managed Units and Edges, select the ones to deploy the profile to.

---

## Uninstalling or reinstalling profiles

### Procedure

1. Select the profile.
2. From the list of available actions, select **Uninstall** or **Reinstall**.

---

## Verifying the pipeline

Use the following steps to confirm end-to-end operation after installation, particularly in a lab or proof-of-concept environment. In a production environment with existing NAS activity, events normally appear in Guardium within one rotation interval plus one poll interval, and no additional steps are required.

### Why audit log files may be empty

A rotation schedule fires on time regardless of client activity. When there has been no auditable activity, ONTAP produces a valid but empty `<Events/>` file. Empty files are not a fault. They indicate that no NAS operation matched an audit policy during the interval.

### Which access methods produce audit records

| Access method | Produces audit records | Reason |
|---|---|---|
| SMB/CIFS against an `ntfs` or `mixed` volume with an NTFS SACL | Yes | Carries a Windows identity, and the SACL defines what is audited |
| NFSv4.x against a `unix` volume with an audit ACE in the NFSv4 ACL | Yes | The audit ACE defines what is audited |
| NFSv4.x with Kerberos against an `ntfs` or `mixed` volume with an NTFS SACL | Yes | The Kerberos principal maps to a Windows SID |
| NFSv3 against a `unix` volume | No | NFSv3 has no ACL mechanism that can carry an audit ACE |
| Any protocol against a volume with no SACL or audit ACE | No | Nothing defines what should be audited |

SMB/CIFS is typically the simplest method for generating test events.

### Preparing a CIFS server for testing

CIFS auditing requires a CIFS server on the SVM so that ONTAP can resolve SIDs. For lab validation the server can run in workgroup mode, and Active Directory is not required.

```
vserver cifs create -vserver <svm_name> -cifs-server <NETBIOS_NAME> -workgroup WORKGROUP

vserver cifs share create -vserver <svm_name> -share-name data -path /<data_volume>
```

**Note:** On a newly created workgroup CIFS server, the local `Administrator` account is disabled by default. Enable the account and set a password before attempting SMB access.

### Generating test events

1. Run the following commands from any Linux host that can reach the ONTAP data LIF.

```bash
# Install smbclient if it is not already present
dnf install -y samba-client   # RHEL and CentOS
apt install -y smbclient      # Debian and Ubuntu

# Generate file operation events: create, read, delete
TS=$(date +%s)
smbclient //<data_lif>/data -U '<NETBIOS_NAME>\Administrator%<password>' -m SMB3 \
  -c "put /etc/hostname test_${TS}.txt; \
      get test_${TS}.txt /tmp/out.txt; \
      del test_${TS}.txt"
```

2. Then force two rotations to flush the staging buffer to a completed file.

```
vserver audit rotate-log -vserver <svm_name>
# wait 5 seconds
vserver audit rotate-log -vserver <svm_name>
```

**Note:** Two rotations are required. The first rotation closes the current staging file, which is often empty, and starts a new one. Events generated after that point land in the new staging file. The second rotation closes that file and produces the completed XML file that the connector reads.

After the second rotation, the connector picks up the file on its next poll cycle, within the configured poll interval. The events then appear in the Guardium **Full SQL** report.

For more information, see the following NetApp documentation:
* [Manually rotate the audit event logs](https://docs.netapp.com/us-en/ontap/nas-audit/manual-rotate-audit-event-logs-task.html) 
* [View and process audit event logs](https://docs.netapp.com/us-en/ontap/nas-audit/view-audit-event-logs-concept.html) 
* [vserver audit rotate-log](https://docs.netapp.com/us-en/ontap-cli/vserver-audit-rotate-log.html)

---

## Limitations

1. The ONTAP `evtx` audit format is not supported. Auditing must be configured with `-format xml`.

2. Events become available to the connector only after ONTAP rotates the active log file. The rotation schedule is the primary control over ingestion latency. For more information, see [Step 3](#3-configuring-an-audit-log-rotation-schedule).

3. The connector reads only completed, rotated audit files. Events held in the active staging file are not read until the next rotation.

4. Auditing records an operation only when the target file or directory carries a matching audit policy. NFSv3 access to a `unix` security style volume produces no audit records because NFSv3 cannot carry an audit ACE. For more information, see [Step 5](#5-applying-an-audit-policy-to-the-data-being-audited).

5. On the first poll after installation, the connector ingests every completed audit file retained in the destination volume. To limit the initial backfill, set `-rotate-limit` before installation or install the profile shortly after a rotation.

6. One connector task is created per SVM. Multiple SVMs are supported in a single profile through the **SVM name(s)** field.

7. The universal connector can be installed on multiple Managed Units for high availability, but all traffic is attributed to a single Managed Unit.

8. **Test Connection** cannot verify that an audit policy is applied to your data or that a rotation schedule is configured. For more information, see [Testing a connection](#testing-a-connection).

---

## Troubleshooting

### No events appear in Guardium

Complete the following checks in order.

1. Confirm that auditing is enabled and the format is correct.
   ```
   vserver audit show -vserver <svm_name>
   ```
   `Auditing State` must be `true` and `Log Format` must be `xml`.

2. Confirm that a rotation schedule is configured. In the same output, check `Log Rotation Schedule`. If every schedule field shows `-`, rotation is size-based only, and events might not reach a completed file for hours or days. For more information, see [Step 3](#3-configuring-an-audit-log-rotation-schedule).

3. Confirm that an audit policy is applied to the data.
   ```
   vserver security file-directory show -vserver <svm_name> -path /<data_volume>
   ```
   Look for a `SACL - ACEs` section that contains `AUDIT-` entries. The absence of this section is the most common cause of empty audit files. For more information, see [Step 5](#5-applying-an-audit-policy-to-the-data-being-audited).

4. Confirm that the audit volume is online and has free space.
   ```
   volume show -vserver <svm_name> -volume <audit_volume>
   ```

5. Check the connector log on the Kafka host.
   ```bash
   tail -f /var/IBM/Guardium/log/kafka-uc/connect.log \
     | grep -iE "ontap|netapp|ERROR|Emitting|Processing"
   ```

   | Log message | Meaning |
   |---|---|
   | `listAuditFiles found N XML files` on every poll | The connector is running normally |
   | `No <Event> elements found` on every file | Auditing is enabled, but no client activity matched an audit policy. For more information, see [Applying an audit policy to the data being audited](#applying-an-audit-policy-to-the-data-being-audited). |
   | `ONTAP authentication failed` | Credentials are incorrect or the account lacks REST API access. For more information, see [ONTAP authentication failed](#ontap-authentication-failed-http-401-or-403) |
   | `No audit configuration found for SVM` | NAS auditing is not configured on the SVM. For more information, see [Step 2](#2-enabling-nas-auditing-on-the-svm-in-xml-format) |
   | `ONTAP audit log format ... must be 'xml'` | Auditing uses `evtx`. For more information, see [Step 2](#2-enabling-nas-auditing-on-the-svm-in-xml-format) |

6. Force two rotations to test the pipeline end to end. For more information, see [Generating test events](#generating-test-events).

---

### Audit log files are all empty (`<Events/>` only)

The following are the most likely causes:

1. No audit policy on the data - No SACL on an `ntfs` or `mixed` volume, or no audit ACE on a `unix` volume. Nothing defines what ONTAP should audit. For more information, see [Step 5](#5-applying-an-audit-policy-to-the-data-being-audited).
2. No auditable client activity - Rotations fire on schedule regardless of activity. The files are valid and there is nothing to record.
3. NFSv3 access to a `unix` security style volume - NFSv3 cannot carry an audit ACE. Use SMB/CIFS or NFSv4.x, or change the volume to `mixed` or `ntfs` security style and apply an NTFS SACL.
4. No CIFS server on the SVM - SMB auditing requires a CIFS server for SID resolution. Without one, events are not recorded.

---

### ONTAP authentication failed (HTTP 401 or 403)

The connector issues a `GET /api/cluster` request to validate credentials. On ONTAP 9.12 and later, a traditional CLI-mapped role might return HTTP 403 for this endpoint.

**Resolution:** Create a REST role. For more information, see [Method A: REST role by using the CLI](#method-a-rest-role-by-using-the-cli-ontap-9111-and-later-recommended).

---

### Connector reports a retention gap warning

```
WARN ONTAP audit retention gap detected on SVM '<svm_name>':
     stored offset='audit_...' but oldest available file='audit_...'
```

ONTAP deleted completed audit files under its `-rotate-limit` policy before the connector could read them, and those events are lost. Increase `-rotate-limit`, reduce the poll interval, or both so that the connector keeps pace with the rotation schedule.

---

### Events are stale by hours

The rotation schedule is too infrequent, or no schedule is configured and rotation is size-based only. For more information, see [Configuring an audit log roatation schedule](#3-configuring-an-audit-log-rotation-schedule).

---

### Test Connection fails with a port or network error

1. Verify that port 443 is open between the Kafka Connect host and the ONTAP management LIF.
   ```bash
   nc -w 5 <management_lif> 443 </dev/null && echo "443 OPEN" || echo "443 CLOSED"
   ```

2. If port 443 is closed but port 22 is open, the management LIF might be on a restricted VLAN. Network access on port 443 from the Kafka Connect host to the ONTAP management LIF is required.

3. Confirm that the address is the **cluster management LIF**, not a data LIF or a node management LIF.

---

### VMware and VSIM environments: ARP conflict on the data LIF

In VMware-based lab environments, the ONTAP data LIF IP address might conflict with another virtual machine on the same subnet. The symptom is that `smbclient` returns `Connection refused` even though `ping` succeeds.

1. Check for an ARP conflict by running the following command.

```bash
arping -c 3 -I <interface> <data_lif_ip>
```

2. Two different MAC addresses in the response indicate a conflict. To resolve it, obtain the correct ONTAP MAC address from `network port show -node <node> -port <port>` and add a static ARP entry on the Linux client.

```bash
ip neigh replace <data_lif_ip> lladdr <ontap_mac_address> dev <interface>
```