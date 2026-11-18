# Configuring Dell PowerScale OneFS (formerly Isilon) datasource profiles for Kafka Connect plug-ins

You can create and configure datasource profiles for the **Dell PowerScale OneFS Over Syslog Connect 2.0** plug-in through Central Manager.

> **Note:** The PowerScale OneFS configuration steps in the following procedures are for reference only. Command syntax, available parameters, and default values vary by OneFS release. For version-specific procedures, see the [OneFS 9.12.0.0 Documentation - PowerScale Info Hub](https://www.dell.com/support/kbdoc/en-gb/000355502/onefs-9-12-0-0-documentation-powerscale-info-hub).

## Meet Dell PowerScale OneFS (formerly Isilon) over Kafka Connect

* Tested versions: OneFS 9.12
* Environment: On-premises
* Supported inputs: Syslog over TLS (Kafka Connect Syslog 2.0, `TCPSSL` listener)
* Supported Guardium versions:
    * Guardium Data Protection: 12.2.3 and later

This plug-in collects NAS file access protocol audit events from Dell PowerScale OneFS (formerly Isilon) clusters over syslog. OneFS sends the events over TLS directly to the syslog listener on the Guardium Kafka nodes.

**What this plug-in captures:** OneFS protocol audit events (`audit_protocol`) for SMB/CIFS and NFS clients, including file open, create, read, write, close, rename, delete, set security (`SET_SECURITY`), get security (`GET_SECURITY`), user logon, and share connect (`TREE_CONNECT`).

**What this plug-in does not capture:** OneFS administrative and cluster configuration audit events (`audit_config`).

---

**Pipeline considerations:**

- Each OneFS node opens its own TLS connection and sends its events with its own node name as the syslog host name.
- The syslog listener runs on one Guardium Kafka node at a time. List every Kafka node in the OneFS protocol syslog servers, so that events keep arriving when the listener moves to another node.
- OneFS sends protocol audit events over TCP only when TLS is enabled. With TLS disabled, OneFS sends UDP, which the Guardium listener does not accept.

---

## Before you begin

### Requirements checklist

| # | Requirement | How to verify |
|---|---|---|
| 1 | TCP port 5514 (or the port set in the profile) open from every PowerScale node to every Guardium Kafka node | From a OneFS node, `openssl s_client -connect <kafka-node-fqdn>:5514 </dev/null` returns a certificate from the Kafka node that runs the listener |
| 2 | The OneFS nodes can resolve the Guardium Kafka node host names | From a OneFS node, `ping -c 1 <kafka-node-fqdn>` prints the node address (the ping itself may be blocked) |
| 3 | Protocol auditing enabled cluster-wide | `isi audit settings global view` shows `Protocol Auditing Enabled: Yes` |
| 4 | The required operations are audited and sent to syslog in each audited zone | `isi audit settings view --zone <zone_name>` (see [1.3](#13-verify-the-auditing-configuration)) |
| 5 | The Guardium CA certificate is imported on OneFS | `isi certificate authority list` (see [1.4](#14-forward-to-guardium-over-tls)) |
| 6 | The Guardium profile **Cluster name** matches the PowerScale cluster name | Compare with the cluster name shown by `isi status` |

---

## 1. Configuring PowerScale OneFS auditing

Run the following commands as a cluster administrator from the OneFS CLI.

### 1.1 Enable protocol auditing cluster-wide

```bash
isi audit settings global modify \
  --protocol-auditing-enabled true \
  --audited-zones System,<zone1>,<zone2>
```

### 1.2 Configure per-zone audit events

For each audited access zone, set the operations to audit, set the operations to send over syslog, and turn on syslog forwarding:

```bash
isi audit settings modify --zone <zone_name> \
  --audit-success create,delete,rename,set_security,open,close,get_security,logon,logoff,tree_connect \
  --audit-failure create,delete,rename,set_security,open,close,get_security,logon,logoff,tree_connect \
  --syslog-audit-events create,delete,rename,set_security,open,close,get_security,logon,logoff,tree_connect \
  --syslog-forwarding-enabled true
```

* `--audit-success` and `--audit-failure` set which successful and failed operations OneFS audits.
* `--syslog-audit-events` filters the audited operations that are sent over syslog. Check that it contains every operation that you want to see in Guardium.
* `logon` and `tree_connect` are needed for logon activity and for the Guardium **Failed Login** report.
* OneFS 9.12 accepts `logoff` as an audit event name but does not generate logoff events, so no logoff activity is reported in Guardium (see [Limitations](#limitations)).

> **Note:** Enabling `read` and `write` auditing on high-throughput shares generates significant event volume. Include them only if required by your compliance policy.

### 1.3 Verify the auditing configuration

```bash
isi audit settings global view
isi audit settings view --zone <zone_name>
```

For each zone, check that `Syslog Forwarding Enabled` is `Yes` and that `Syslog Audit Events` lists the operations that you need.

### 1.4 Forward to Guardium over TLS

PowerScale OneFS sends TLS-encrypted syslog directly to the Guardium syslog listener. The Guardium datasource profile uses **Syslog listener = TCPSSL**.

OneFS strictly enforces TLS certificate verification. The Guardium server CA certificate must be downloaded from the **Central Manager** and imported into PowerScale OneFS for the connection and event forwarding to work.

**Step 1. Download the Guardium server CA certificate from Central Manager.**
1. In the Central Manager UI, navigate to **Manage > Universal Connector > Datasource Profile Management**.
2. Select **Actions > Download server CA** (or export the Kafka cluster root/intermediate CA certificate).
3. Save the downloaded PEM file (for example, `guard_uc_CA.pem`).

**Step 2. Verify that the CA certificate validates the Kafka listener.**
From a system that can reach the Guardium Kafka nodes, test the connection using the downloaded CA certificate:

```bash
openssl s_client -connect <kafka-node-fqdn>:5514 \
  -CAfile guard_kafka_cluster_CA.pem -verify_hostname <kafka-node-fqdn> </dev/null 2>&1 | grep "Verify return code"
```

The output must show `Verify return code: 0 (ok)`.

**Step 3. Copy the CA certificate to PowerScale OneFS and import it.**
Copy the certificate to a directory under `/ifs` (import will fail if placed outside `/ifs`, such as `/tmp`):

```bash
# Copy to PowerScale
scp guard_kafka_cluster_CA.pem root@<powerscale-ip>:/ifs/data/certs/

# On the PowerScale CLI, import the authority certificate
isi certificate authority import /ifs/data/certs/guard_kafka_cluster_CA.pem \
  --name guardium-uc --description "Guardium Universal Connector Server CA"

# Verify the certificate is imported
isi certificate authority list | grep guardium-uc
```

**Step 4. Configure PowerScale OneFS with all Guardium Kafka nodes and enable TLS.**

In a Guardium Kafka cluster, the active syslog listener task runs on one Kafka node at a time and may dynamically rebalance or fail over to another Kafka node. **You must add all Guardium Kafka nodes** to the PowerScale syslog server configuration so forwarding continues uninterrupted during failover or rebalancing.

```bash
# Clear any existing syslog servers
isi audit settings global modify \
  --clear-protocol-syslog-servers

# Add ALL Guardium Kafka nodes by FQDN with port 5514
isi audit settings global modify \
  --protocol-syslog-servers kf01.example.com:5514 \
  --protocol-syslog-servers kf02.example.com:5514 \
  --protocol-syslog-servers kf03.example.com:5514 \
  --protocol-syslog-tls-enabled true

# Confirm the configuration
isi audit settings global view | grep -E "Protocol Syslog"
```

* **Add all Kafka nodes:** List every Kafka node in your Guardium cluster (e.g. `kf01.example.com:5514`, `kf02.example.com:5514`, `kf03.example.com:5514`).
* **Use host names (FQDNs), not IP addresses:** OneFS validates the server certificate's Subject Alternative Names (SAN). Using an IP address will cause `IP address mismatch` errors.
* **Include the port:** Append `:5514` (or your configured profile port) to each host name.
* **Set the profile Cluster name:** Set the **Cluster name** field in Guardium to match your PowerScale cluster name (as shown by `isi status`). All nodes matching `<cluster>-<N>` are automatically collected under the single profile.

**Step 5. Verify.** Generate some SMB or NFS activity and check the OneFS log for the connection result:

```bash
grep isi_audit_syslog /var/log/messages | tail
```

Common messages:

| Message in `/var/log/messages` | Cause | Action |
|---|---|---|
| `CERTIFICATE_VERIFY_FAILED ... unable to get local issuer certificate` | The imported CA did not sign the certificate presented by the listener | Repeat step 2 and import the matching CA |
| `CERTIFICATE_VERIFY_FAILED ... Hostname mismatch` or `IP address mismatch` | The server name in `--protocol-syslog-servers` is not in the certificate SAN | Use a name from the SAN (step 4) |
| `Failed to connect to:<node>:5514, err:[Errno 61] Connection refused` | That Kafka node is not running the syslog listener | Expected for the other nodes, see [Limitations](#limitations) |
| `The handshake operation timed out` | The listener on that port does not use TLS, for example because a profile of another plug-in on the same port uses `TCP` | Use a port on which every profile uses `TCPSSL` |

---

## Creating datasource profiles

### Procedure

1. Go to **Manage > Universal Connector > Datasource Profile Management**.
2. Click the **➕ (Add)** button.
3. Create a profile by using one of the following methods:
    * To create a profile manually, go to the **Add Profile** tab and provide values for the following fields:
        * **Name** and **Description**.
        * Select **Plug-in Type**: `Dell PowerScale OneFS Over Syslog Connect 2.0`.
        * Select or create **Syslog Credentials**.
    * To upload from CSV, go to the **Upload from CSV** tab and upload a CSV file with one or more profiles. The following options are available:
        * **Update existing profiles on name match** — Updates profiles with the same name if they already exist.
        * **Test connection for imported profiles** — Tests connections automatically after profiles are created.
        * **Use ELB** — Enables ELB support. Provide the number of Managed Units to include.

**Note:** Configuration options vary based on the selected plug-in.

---

## Configuring Dell PowerScale OneFS Over Syslog Connect 2.0

### Profile fields

| Field | Description |
|---|---|
| **Name** | Unique name for the profile. |
| **Description** | Description of the profile. |
| **Plug-in** | Select `Dell PowerScale OneFS Over Syslog Connect 2.0`. |
| **Kafka Cluster** | Kafka cluster used to deploy the universal connector. For more information, see [Managing Kafka clusters](https://www.ibm.com/docs/en/SSMPHH_12.x/com.ibm.guardium.doc.stap/guc/guc_kafka_cluster_management.html). |
| **Syslog Credentials** | Select or create **Syslog Credentials**. The credential type must be **Syslog Credentials** with a username field (the username can be any value as it is not used for authentication). |
| **Label** | Grouping label. For example, a customer name or datacenter. |
| **Syslog port** | TCP port the connector listens on. Default is `5514`. |
| **Syslog listener** | `TCPSSL`. This is the only option, because OneFS sends protocol audit events over TCP only when TLS is enabled. |
| **Cluster name** | The PowerScale cluster name, as shown by `isi status`. Each OneFS node sends its own node name, `<cluster name>-<node number>`, sometimes followed by `(idN)` or a domain name. This one profile collects events from every node in the cluster, including nodes added later. Each node is reported as its own server host name. To collect from one node only, enter that node's name. A profile with a node name takes precedence over the cluster profile on the same port. |
| **No traffic threshold (minutes)** | Minutes without events before a no-traffic alert is triggered. Default is `60`. |
| **Use Enterprise Load Balancing (ELB)** | Enable for Enterprise Load Balancing deployments. |

**Notes:**
- The profile name must be unique across the Guardium environment.
- Profiles of other plug-ins that share the same syslog port must also use `TCPSSL`.

---

## Testing a connection

**Test Connection** validates that:
1. The `Syslog port` value is a valid port number (`1–65535`).
2. The port is available and not in conflict with an existing listener.

### Procedure

1. Select the profile.
2. Click **Test Connection**.
3. If the test fails:
    * Verify that port 5514 is not in use by another service on Guardium.
    * If another Guardium profile uses port 5514, confirm that it also uses `TCPSSL`.

---

## Installing a profile

### Procedure

1. Select the profile.
2. From the **Install** menu, click **Install**.
3. Select the Managed Units and Edges to deploy to.

---

## Uninstalling or reinstalling profiles

### Procedure

1. Select the profile.
2. Select **Uninstall** or **Reinstall** from the available actions.

---

## Limitations

* **Syslog transport:** PowerScale OneFS forwards protocol audit events over TCP only when TLS is enabled. Plain TCP and UDP syslog forwarding are not supported by this plug-in. The datasource profile requires the `TCPSSL` listener.
* **Syslog connection notices for inactive Kafka nodes:** In a multi-node Guardium Kafka Connect cluster, the active syslog listener task runs on one Kafka node at a time. PowerScale OneFS continuously attempts to deliver to all servers listed in `--protocol-syslog-servers` and records connection refused notices (`[Errno 61] Connection refused`) in `/var/log/messages` for the inactive listener nodes. These notices are expected and do not indicate data loss while the active listener node is operational.
* Dell PowerScale OneFS audit logs do not include plaintext usernames (such as DOMAIN\username); they only contain the user's Active Directory SID (userSID) or UNIX ID (userID). Therefore, Guardium populates DB User Name and Application User with the raw SID (for example, S-1-5-21-...-1000) or mapped UNIX ID (uid:0, uid:2000). The plug-in does not perform real-time AD or LDAP lookups to resolve SIDs to account names.
* **High-volume operations:** Intensive file operations (such as large-scale file copying or directory traversal) generate large volumes of `OPEN` events. Enabling `read` and `write` audit event types on high-throughput shares will significantly increase event volume; configure audit event filters to align with your organization's monitoring and compliance policies.
* **Duplicate message suppression:** If the OneFS syslog daemon suppresses rapid repeated events (`syslogd: last message repeated N times`), the suppression notices are filtered out and individual suppressed events are not forwarded to Guardium.
* **Failed Login reporting:** Guardium **Failed Login** reports capture failed share connection (`TREE_CONNECT`) events.
* **Logoff events:** OneFS 9.12 does not generate `logoff` events in the protocol audit log, even when `logoff` is enabled for auditing and syslog forwarding. Guardium therefore reports logon (`Logon`) and share connection (`Connect Share`) activity, but no logoff activity.
* **Database Error Text:** For exception records, the error description (including NTSTATUS text) is provided in the **Exception Description** field. The **Database Error Text** field is populated with `N/A`.
* **Server port:** OneFS protocol audit events do not contain the server port, so the server port field is not populated.
* **High availability reporting:** When the universal connector is deployed across multiple Managed Units for high availability, traffic is attributed to a single Managed Unit.

---

## Troubleshooting

### No events appear in Guardium

1. **Verify OneFS auditing:**
   ```bash
   isi audit settings global view
   isi audit settings view --zone <zone_name>
   ```
   Confirm `Protocol Auditing Enabled: Yes`, that every Guardium Kafka nodes are listed as `<fqdn>:<port>` under `Protocol Syslog Servers`, and that protocol syslog TLS is enabled. For each zone, confirm `Syslog Forwarding Enabled: Yes` and the `Syslog Audit Events` list.

2. **Check the OneFS connection messages:**
   ```bash
   grep isi_audit_syslog /var/log/messages | tail
   ```
   For the meaning of each message, see the table in [1.4](#14-forward-to-guardium-over-tls), step 5.

3. **Check which Kafka node runs the listener:** On each Guardium Kafka node, run:
   ```bash
   ss -ltn | grep 5514
   ```
   Exactly one node is expected to show a listening socket. That node must be in the OneFS `Protocol Syslog Servers` list.

4. **Verify hostname matching:**
   Check that the **Cluster name** in the Guardium profile is the node name without the `-<node number>` part (node `powerscale-cluster-3` → cluster `powerscale-cluster`), or the exact node name for a single-node profile. On a OneFS node, `isi status` shows the cluster name and `hostname` shows the node name.

5. **Review connector log messages:**
   Check the Kafka Connect log on the Guardium Kafka nodes for the following messages:

   | Log message | Level | Cause | Action |
   |---|---|---|---|
   | `SyslogSourceTask: could not bind port 5514 — will retry in <n> ms: ...` | WARN | Another process or listener holds port 5514 on that Kafka node | Check for conflicting profiles or processes on that node |
   | `SyslogPortRegistry: no route for host '<name>' (remote: <ip>), dropping` | DEBUG | The syslog host name matches neither the profile **Cluster name** nor one of its node names | Update the profile **Cluster name** (see step 4) |
   | `Filtered UC guard record, discard record: ...` with `Syslog duplicate suppression line` | DEBUG | OneFS is suppressing rapid duplicate events | Reduce the audited event types in OneFS |

---

### Test Connection fails with a port conflict

1. Verify that all datasource profiles sharing port 5514 use `TCPSSL`.
2. Verify that no other service is bound to port 5514 on Guardium.

---

### After a OneFS upgrade

The plug-in depends on the OneFS audit event layout, which Dell does not document. After you upgrade OneFS, generate some SMB and NFS activity and check the following:

1. **Parser errors:** Look in the Guardium exception reports for exceptions of type `UC_PARSER_ERROR` whose description starts with `PowerScaleOneFS parser error:`. These exceptions mean that an event has a layout the plug-in does not recognize. The original event text is kept with the exception.
2. **Unknown error codes:** Look in **Exception Description** for text such as `OPEN failed: NTSTATUS 3221225999`. This text means that the plug-in has no name for that error code. The event is still reported, with the numeric code in place of the name.
3. **Operations without a Guardium verb:** Look in **Full SQL** for statements that contain `raw="`, for example `FUTURE_OP /ifs/data/a.txt | result=SUCCESS type=FILE fileId=12345 raw="FILE|12345|/ifs/data/a.txt"`. Such a statement means that OneFS sent an operation that the plug-in does not map to a Guardium verb. The event is still reported: the verb is the OneFS operation name, and `raw=` holds all of the event's operation fields exactly as sent.