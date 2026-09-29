# Configuring Claude datasource profiles for Kafka Connect plug-ins

Create and configure datasource profiles through Central Manager for **Claude Over Claude Kafka Connect** plug-ins.

## Meet Claude Over Claude Kafka Connect

* **Environment:** Anthropic Claude AI
* **Supported inputs:** Kafka connect Claude 2.0 (pull)
* **Supported Guardium versions:**
    * Guardium Data Protection: Appliance bundle 12.2.4 or later

Kafka Connect is a framework for streaming data between Apache Kafka and other systems. This connector monitors Claude activity by pulling compliance events from Anthropic's Compliance API and publishing them to Apache Kafka.

## Configuring Anthropic Claude for Compliance

### Prerequisites

To use this connector you need:
- An Anthropic organization with access to the **Compliance API**.
- A **Compliance API key** with compliance scope. See [Compliance API](https://platform.claude.com/docs/en/manage-claude/compliance-api).
- Optionally, an **Admin API key** for enriching `admin_api_key_created`, `admin_api_key_deleted`, and `compliance_api_accessed` activities with key name and status.

| Key type | Prefix | Created in |
|---|---|---|
| Compliance Access Key | `sk-ant-api01-...` | claude.ai > Organization settings > API |
| Admin API Key | `sk-ant-admin01-...` | Claude Console > Settings > Admin keys |


Source: [Set up the Compliance API](https://platform.claude.com/docs/en/manage-claude/compliance-api-access).

### How the Compliance API works

Before configuring the connector, review how the Compliance API works to understand what data is accessible and which key type is required. It covers endpoint authentication, key scopes, organization types, and rate limits. For full details, see [How the Compliance API works](https://platform.claude.com/docs/en/manage-claude/compliance-api#how-the-compliance-api-works).

### Enabling audit logging

Compliance activity collection is enabled at the organization level through the Anthropic admin console.

1. Log in to [console.anthropic.com](https://console.anthropic.com) as an organization admin.
2. Navigate to **Settings > Compliance**.
3. Confirm that the **Compliance Activity Feed** is enabled for your organization.
4. Generate or retrieve a **Compliance API access key**.

For more information, see the [Anthropic Compliance API documentation](https://platform.claude.com/docs/en/manage-claude/compliance-api).

### Supported activity types

The connector collects the following activity types from the Anthropic Compliance API.

| Category | Activity Types |
|---|---|
| Chat | `claude_chat_created`, `claude_chat_updated`, `claude_chat_viewed`, `claude_chat_deleted`, `claude_chat_access_failed`, `claude_chat_deletion_failed` |
| File | `claude_file_uploaded`, `claude_file_viewed`, `claude_file_deleted` |
| Project | `claude_project_created`, `claude_project_viewed`, `claude_project_deleted`, `claude_project_document_uploaded`, `claude_project_document_deleted` |
| Admin API Key | `admin_api_key_created`, `admin_api_key_deleted` |
| Compliance API | `compliance_api_accessed` |
| Settings | `claude_chat_settings_updated`, `claude_organization_settings_updated` |
| Auth | `sso_login_failed`, `step_up_authentication_failed`, `trusted_device_revoked` |
| Other | `abuse_decision_received`, `account_deleted`, `claude_artifact_created`, `org_user_invite_accepted`, `claude_user_role_updated` |

## Kafka Connect Claude 2.0 connector

On each poll, the connector calls `GET /v1/compliance/activities` with the stored cursor (`after_id`) and the configured activity types. On the first poll (no cursor), `created_at.gte` is used instead. Each activity is then enriched with metadata from related API endpoints before being published to Kafka.

If **Start Time** (`created_at.gte`) is changed after the connector has been running, the stored activity cursor is discarded on the next restart and polling begins from the new time. The per-chat message cursors are independent and are not cleared — so `claude_chat_viewed` activities encountered after the reset still resume from the last fetched message for that chat, not from the beginning. When a profile is deleted from **Datasource Profile Management** page, its per-chat message cursors are removed as part of the deletion.

### Enrichment

| Activity type(s) | Enrichment endpoint | Fields added |
|---|---|---|
| All (with `organization_uuid`) | `GET /v1/compliance/organizations` (cached) | `organization_name` |
| `claude_chat_created` (full history), `claude_chat_viewed` (new messages since last cursor) | `GET /v1/compliance/apps/chats/{id}/messages` | `claude_chat` object (name, model, messages) |
| `claude_chat_updated`, `claude_chat_deleted` | `GET /v1/compliance/apps/chats/{id}/messages` (limit=1) | `claude_chat` object (name, model only) |
| `claude_project_*` | `GET /v1/compliance/apps/projects/{id}` (cached) | `name`, `is_private` |
| `claude_file_*` | `GET /v1/compliance/apps/chats/files/{id}` | `mime_type`, `size_bytes`, `md5` |
| `claude_artifact_created` | `GET /v1/compliance/apps/artifacts/{id}` | `title`, `md5`, `size_bytes`, `artifact_type`, `version_id` |
| `admin_api_key_created`, `admin_api_key_deleted` | `GET /v1/organizations/api_keys` (cached); falls back to `GET /v1/organizations/api_keys/{id}` if not in cache | `name`, `status`, `expires_at` |
| Any activity where the actor has `api_key_id` or `admin_api_key_id` | `GET /v1/organizations/api_keys` (cached); falls back to `GET /v1/organizations/api_keys/{id}` if not in cache | `actor.api_key_name` |

> **Note:** The `GET /v1/organizations/api_keys` endpoint is called with the **Admin API key** (`sk-ant-admin01-...`) when configured, and falls back to the compliance key otherwise.

### Kafka record output

For `claude_chat_created` and `claude_chat_viewed`, the connector emits **one Kafka record per chat message**. All other activity types produce a single record.

### Actor identity and dbUser mapping

Each activity includes an `actor` object. The connector resolves the best available identity from the actor and uses it as `dbUser` in Guardium:

| Actor type | Identity used |
|---|---|
| `UserActor` / `AnthropicActor` | `email_address` |
| `UnauthenticatedUserActor` | `unauthenticated_email_address` |
| `APIActor` | `api_key_name` (enriched) or `api_key_id` |
| `AdminAPIKeyActor` | `api_key_name` (enriched) or `admin_api_key_id` |
| `ServiceAccountActor` | `service_account_id` |
| `FederatedIdentityActor` / `FederatedActor` | `subject` |
| `AttestedDeviceActor` | `external_client_id` |
| `SystemActor` | `service` |
| `ScimDirectorySyncActor` | `directory_id` |

For `APIActor` and `AdminAPIKeyActor`, the key name is resolved via `GET /v1/organizations/api_keys` using the Admin API key if configured, or the compliance key otherwise. 

---

## Limitations

1. The connector uses a **cursor-based offset** to track progress. If the cursor expires or is pruned by Anthropic, the connector resets automatically and resumes from the current time, which may result in a gap in coverage.

2. For `claude_chat_created` and `claude_chat_viewed` activities, the connector emits **one record per chat message**. For all other activity types, a single record is emitted per activity.

3. The `Start time` parameter is only applied on the **first poll** (when no cursor is stored). Once a cursor is established, it is ignored.

4. The following fields are not available in the Anthropic Compliance API and are not populated in Guardium records:
    - Server Port / Server IP
    - Client Host Name

5. Only one connector instance should be deployed per Anthropic organization at a time. Running multiple connectors against the same organization and activity types may cause duplicate records.

6. Chat messages that produce a Guardium record exceeding **1 MB** are dropped and not forwarded to Guardium. Chats with very long message histories may be affected.

7. Depending on the activity type, one or more of the following fields may be absent from the Guardium record: **source program**, **service name**, **database name**, **dbUser**, **server IP**, or **client IP**.

## Configuring credentials

The following table describes the credential fields required for Claude Compliance authentication.

| Field | Description |
|---|---|
| **Credential Type** | Select `Claude Compliance API Key` as the credential type. |
| **API Key** | The Anthropic Compliance API access key. Used for all compliance endpoints. |
| **Admin API Key** | *(Optional)* An Anthropic Admin API key. Required only to enrich `admin_api_key_created` and `admin_api_key_deleted` activities with key name and status. If not provided, those activities are still collected but without the enriched details. |

### Creating Claude credentials

#### Procedure

1. Click **Manage > Universal Connector > Credential Management**.
2. Click the **➕ (Add)** button to create a new credential.
3. Select `Claude Compliance API Key` as the **Credential Type**.
4. Enter the **API Key** obtained from the Anthropic admin console.
5. If you want `admin_api_key_created` and `admin_api_key_deleted` activities to include enriched key details, enter the **Admin API Key**. Otherwise, leave it blank.
6. Click **Save**.

---

## Creating datasource profiles

You can create a new datasource profile from the **Datasource Profile Management** page.

### Procedure

1. Go to **Manage > Universal Connector > Datasource Profile Management**.
2. Click the **➕ (Add)** button.
3. Create a profile by using one of the following methods:

    * To **Create a new profile manually**, go to the **"Add Profile"** tab and provide values for the following fields:
        * **Name** and **Description**.
        * From the dropdown, select a **Plug-in Type**. For example, `Claude Over Claude Connect 2.0`.

    * To **Upload from CSV**, go to the **"Upload from CSV"** tab and upload an exported or manually created CSV file containing one or more profiles. You can also choose from the following options:
        * **Update existing profiles on name match** — Updates profiles with the same name if they already exist.
        * **Test connection for imported profiles** — Automatically tests connections after profiles are created.
        * **Use ELB** — Enables ELB support for imported profiles. You must provide the number of MUs to be used in the ELB process.

**Note:** Configuration options vary based on the selected plug-in.

---

## Configuring Claude Over Claude Connect 2.0

The following table describes the fields that are specific to the Claude Over Claude Connect 2.0 plug-in.

| Field | Description |
|---|---|
| **Name** | Unique name of the profile. |
| **Description** | Description of the profile. |
| **Plug-in** | Plug-in type for this profile. Select `Claude Over Claude Connect 2.0`. A full list of available plug-ins is available on the **Package Management** page. |
| **Credential** | The credential to authenticate with Anthropic. The credential must be created in **Credential Management**, or click **➕** to create one. For more information, see [Creating Credentials](https://www.ibm.com/docs/en/SSMPHH_12.x/com.ibm.guardium.doc.stap/guc/guc_credential_management.html). |
| **Kafka Cluster** | Select the appropriate Kafka cluster from the available Kafka cluster list or create a new Kafka cluster. For more information, see [Managing Kafka clusters](https://www.ibm.com/docs/en/SSMPHH_12.x/com.ibm.guardium.doc.stap/guc/guc_kafka_cluster_management.html). |
| **Label** | Grouping label. For example, customer name or ID. |
| **Poll Interval (ms)** | How frequently the connector polls the Anthropic API, in milliseconds. Default: `60000` (1 minute). |
| **Max Events per Request** | Maximum number of events to fetch per API request (1–5000). Default: `1000`. |
| **Start Time** | ISO 8601 timestamp for the initial fetch window. Only used on the first poll when no cursor is stored. If not set, defaults to the current UTC time. Example: `2026-05-01T00:00:00Z`. |
| **No-traffic threshold (minutes)** | If no incoming traffic is received for the configured period, S-TAP displays a red status. When incoming traffic resumes, the status returns to green. Default: `60`. |
| **Chat Fetch Delay (ms)** | *(Advanced)* Delay in milliseconds between successive chat message API calls. Increase this value if Anthropic rate limits are being hit. Default: `200`. |
| **Organization Cache TTL (ms)** | *(Advanced)* How long to cache the organization list before re-fetching. Default: `3600000` (60 minutes). |
| **Project Cache TTL (ms)** | *(Advanced)* How long to cache the project list before re-fetching. Default: `900000` (15 minutes). |
| **Admin API Key Cache TTL (ms)** | *(Advanced)* How long to cache the admin API key list before re-fetching. Default: `300000` (5 minutes). |

**Note:**
- Ensure that the **profile name** is unique.
- The Kafka cluster must be configured and accessible before creating the profile.

---

## Testing a connection

After you create a profile, test the connection to ensure that the configuration is valid.

### Procedure

1. Select the new profile.
2. From the top menu, click **Test Connection**.
3. If the test fails, verify the following:
   - The **API Key** begins with `sk-ant-api`. Keys with any other prefix are rejected. 
   - The **Admin API Key**, if provided, begins with `sk-ant-admin`. See [Compliance Access Key and Admin API Key](https://platform.claude.com/docs/en/manage-claude/compliance-api-access).
4. If the test is successful, proceed to installing the profile.

---

## Installing a profile

After the connection test is successful, you can install the profile on **Managed Units (MUs)** or **Edges**. The parsed audit data is sent to the selected Managed Unit or Edge to be consumed by the **Sniffer**.

### Procedure

1. Select the profile.
2. From the **Install** menu, click **Install**.
3. From the list of available MUs and Edges, select the ones where you want to deploy the profile.

---

## Uninstalling or reinstalling profiles

You can uninstall or reinstall an installed profile.

### Procedure

1. Select the profile.
2. From the list of available actions, select **Uninstall** or **Reinstall**.

---

## Troubleshooting

### No events are appearing in Guardium

1. Verify that the Anthropic Compliance API is accessible from the Kafka Connect node.
2. Check the Kafka Connect logs for errors related to the connector.
3. Verify that the activity types configured in the profile are types that are actually being generated by your organization. Some types (for example, `sso_login_failed`) may only appear if your organization uses SSO.
4. If the **Start Time** was set to a future date by mistake, no historical events will appear until the connector cursor advances past that point. Remove the value or reset it to an appropriate past timestamp.

### Connector task fails on the first poll

An HTTP 400 response from the Anthropic API on the first poll indicates that one or more values in the activity types configuration are not recognized. Review the [Supported activity types](#supported-activity-types) table and correct the configuration.

### Connector task fails with an authentication error

The API key is missing, invalid, or does not have compliance API access. Generate a new key from the Anthropic admin console and update the credential in **Credential Management**.

### Connector resets and skips events

If the stored cursor expires or is no longer found, the connector automatically resets to the current time and logs a warning. This is expected behaviour when a cursor is pruned by Anthropic. To minimize gaps, keep the poll interval short enough that the cursor is refreshed before it expires.

### Chat enrichment is slow or causing rate-limit errors

Increase the **Chat Fetch Delay (ms)** value in the Advanced section of the profile to add more time between successive chat message API calls. A value of `500`–`1000` ms is recommended for organizations with high chat activity.
