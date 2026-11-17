# Guardium Universal Connector for Google Cloud (Pub/Sub): Troubleshooting Guide

> **Note:** This guide is for reference only. Google Cloud commands, IAM roles, console options and behavior can change. Always check the official Google Cloud documentation, such as [Pub/Sub](https://cloud.google.com/pubsub/docs), [Cloud Logging log sinks](https://cloud.google.com/logging/docs/export/configure_export_v2) and [IAM](https://cloud.google.com/iam/docs), before you run any command or change your Google Cloud configuration.

## Overview

This guide applies to any Google Cloud data source that Guardium monitors through the Universal Connector (UC) over Pub/Sub, for example Cloud SQL for PostgreSQL or MySQL, AlloyDB, BigQuery, Bigtable, Spanner or Firestore. It covers both connector types:

- **Pub/Sub (Logstash)** profiles, such as *PostgreSQL Over Pub Sub*, which use the `google_pubsub` input.
- **UC 2.0 (Kafka Connect)** profiles, such as *GCP Postgres Over PubSub Connect 2.0*, which use the *Kafka connect Pubsub 2.0* input.

Steps [1](#step-1-uc-input-configuration)–[6](#step-6-audit-entries-in-cloud-logging) and [8](#step-8-guardium-collector) are the same for every data source. [Step 7](#step-7-data-source-audit-configuration) and the [Data source reference](#data-source-reference) cover the settings that differ.

Guardium receives audit events through the following path:

1. **Data source** writes audit records to Cloud Logging, typically as Data Access audit logs.
2. **Log sink** (organization, folder or project level) selects these entries with its inclusion filter and routes them to the sink's destination topic.
3. **Pub/Sub topic** receives the entries as messages. The sink's writer identity must hold `roles/pubsub.publisher` on the topic. Each message is delivered only to the subscriptions that are attached to the topic when it is published.
4. **Pub/Sub subscription** attached to the topic queues the messages until they are acknowledged or the retention period ends.
5. **Guardium Universal Connector** pulls from the subscription with a service account key and parses the events with the plugin for that data source. The data source appears in S-TAP Status after the first event is parsed.

A misconfiguration at any point results in no data source in S-TAP Status and no events in Guardium. Each value below must be consistent across all components.

**Note:** Use a dedicated Pub/Sub topic, subscription, and log sink for each database type. Make sure that they are not used by any other database type or application. Messages from another source can cause parsing errors or incorrect audit records.

| Value | Where it is set |
| --- | --- |
| Data source project | Sink inclusion filter |
| Topic project | Sink destination; UC project ID |
| Topic | Sink destination; UC topic |
| Subscription | UC subscription; must be attached to the topic |
| Sink writer identity | Topic IAM policy (`roles/pubsub.publisher`) |
| UC service account | Subscription IAM policy (`roles/pubsub.subscriber`; for UC 2.0 also `pubsub.subscriptions.get`) |

## Before you begin

Set these variables in your shell. All commands in this guide use them. Take `RESOURCE_TYPE` and `SERVICE_NAME` from the [Data source reference](#data-source-reference).

```
export ORG_ID="<organization-id>"
export DB_PROJECT="<project-id-of-the-data-source>"
export RESOURCE_TYPE="<monitored-resource-type>"        # for example cloudsql_database
export SERVICE_NAME="<google-cloud-service-name>"       # for example cloudsql.googleapis.com
export TOPIC_PROJECT="<project-id-of-the-pubsub-topic>"
export TOPIC="<topic-name>"
export SUBSCRIPTION="<subscription-name-used-by-guardium>"
export SINK_NAME="<log-sink-name>"
export KEY_FILE="<path-to-guardium-service-account-key.json>"
```

The commands assume an organization-level sink. For a folder-level or project-level sink, replace `--organization=$ORG_ID` with `--folder=<folder-id>` or `--project=<project-id>`.

Work through the steps in order. Stop at the first result that does not match **Expected** and correct it before continuing.

## Common causes

| Cause | Step |
| --- | --- |
| UC topic or subscription does not match the sink's destination topic | [1](#step-1-uc-input-configuration), [2](#step-2-pubsub-subscription) |
| UC topic or subscription entered as a `projects/...` path instead of the name | [1](#step-1-uc-input-configuration) |
| Subscription is attached to a different topic than the sink writes to | [2](#step-2-pubsub-subscription) |
| UC service account lacks `roles/pubsub.subscriber`, or its key is disabled or expired | [3](#step-3-uc-service-account-access) |
| UC 2.0 service account lacks `pubsub.subscriptions.get` on the subscription | [3](#step-3-uc-service-account-access) |
| Topic, subscription or sink shared with another database type or application | [2](#step-2-pubsub-subscription), [5](#step-5-log-sink) |
| Sink filter restricts `logName` to a project that does not contain the data source | [5](#step-5-log-sink) |
| Sink filter does not match the data source's resource type or log | [5](#step-5-log-sink), [6](#step-6-audit-entries-in-cloud-logging) |
| Organization- or folder-level sink created without `includeChildren` | [5](#step-5-log-sink) |
| Sink writer identity lacks `roles/pubsub.publisher` on the topic | [5](#step-5-log-sink) |
| VPC Service Controls perimeter blocks delivery | [5](#step-5-log-sink) |
| Audit logging not enabled on the data source, or Data Access audit logs disabled for the service | [6](#step-6-audit-entries-in-cloud-logging), [7](#step-7-data-source-audit-configuration) |
| Guardium plugin does not match the data source or log format | [8](#step-8-guardium-collector) |

## Step 1: UC input configuration

Open the connector on the Universal Connector configuration page in Guardium and check the plugin selected for it. Then use the section that matches the connector type.

| Plugin name contains | Connector type | Section |
| --- | --- | --- |
| `Connect 2.0` | UC 2.0 (Kafka Connect) | [Step 1a](#step-1a-uc-20-kafka-connect-profile) |
| `Over Pub Sub`, `Over PubSub` or `Over Google PubSub`, without `Connect 2.0` | Pub/Sub (Logstash) | [Step 1b](#step-1b-pubsub-logstash-profile) |

Check also that the plugin matches the data source (see the [Data source reference](#data-source-reference)). A plugin for a different data source pulls messages but parses none.

### Step 1a: UC 2.0 (Kafka Connect) profile

| Field | Requirement |
| --- | --- |
| GCP project ID | Project that holds the topic and subscription, not the data source project. The topic and the subscription must both be in this project |
| Pub/Sub topic | Name of the sink destination topic only, for example `guardium-audit-topic` |
| Pub/Sub subscription | Name of a pull subscription attached to that topic ([Step 2](#step-2-pubsub-subscription)), for example `guardium-audit-sub` |
| Credential | A *Google Cloud Credentials* entry whose *Service Account JSON key file* is the key verified in [Step 3](#step-3-uc-service-account-access). If the collector reaches Google Cloud through a proxy, set *Enable HTTP proxy*, *HTTP proxy URL*, *HTTP proxy user name* and *HTTP proxy password* in the same credential. When the proxy is enabled, all three proxy values are required |
| Initial polling time | Optional, empty by default: the connector starts from the subscription's current position, that is, the oldest unacknowledged message. A value must be an ISO 8601 UTC time in the past, for example `2024-01-15T08:30:00Z`; a future or invalid value is rejected at validation. A valid value moves the subscription to that time on the connector's first start only; later restarts continue from the stored position. Messages older than the subscription's retention period are not available |
| Poll interval (seconds), Poll interval (minutes) | Defaults `30` and `0`. The two values are added together; if the total is `0`, the connector uses 30 seconds. These affect latency, not whether events arrive |
| Max poll records, Events per second | Defaults `1000` and `10000`. These affect throughput, not whether events arrive |
| No traffic threshold | Default `60` (minutes). A no-traffic alert is raised after this period without events |

The connector combines the project ID with the topic and subscription names. If either field contains a full path such as `projects/<TOPIC_PROJECT>/subscriptions/<SUBSCRIPTION>`, the subscription is not found or the topic check fails. Enter the names only, even though the field help text shows the full path as an example.

Some 2.0 profiles also show an **Advanced** section (connection timeout, parallel pull streams, flow control limits, acknowledgement deadline, batch acknowledgement, circuit breaker timeout). These fields do not affect whether events arrive; leave them at their defaults. The default acknowledgement deadline is `600` seconds, which the subscription must match ([Step 2](#step-2-pubsub-subscription)).

**If it does not match:** correct the values, save, and restart the connector.

### Step 1b: Pub/Sub (Logstash) profile

Compare the input block with the following.

```
google_pubsub {
  project_id => "<TOPIC_PROJECT>"
  topic => "<TOPIC>"
  subscription => "<SUBSCRIPTION>"
  json_key_file => "${THIRD_PARTY_PATH}/<key-file-name>.json"
  include_metadata => true
  codec => "json"
  # max_messages => 100
}
```

| Setting | Requirement |
| --- | --- |
| `project_id` | Project that holds the topic and subscription, not the data source project |
| `topic` | Same as the sink destination topic |
| `subscription` | Short name of an existing subscription attached to `topic` ([Step 2](#step-2-pubsub-subscription)) |
| `json_key_file` | Key for the service account verified in [Step 3](#step-3-uc-service-account-access) |

In the Guardium profile form, these values appear as *GCP project id*, *Pub/Sub topic*, *Pub/Sub subscription* and *Maximum messages to pull* (default `100`).

Every value must be quoted, every comment line must start with `#`, and the block must end with `}`.

**If it does not match:** correct the values, save, and restart the connector.

## Step 2: Pub/Sub subscription

**Command 1: topic of the configured subscription.**

```
gcloud pubsub subscriptions describe $SUBSCRIPTION --project=$TOPIC_PROJECT
```

**Expected:**

- `topic: projects/<TOPIC_PROJECT>/topics/<TOPIC>`
- `state: ACTIVE`
- No `pushConfig.pushEndpoint` (the UC requires a pull subscription)
- No `filter:`, or a filter that matches the data source's audit entries
- No `detached: true` and no topic of `_deleted-topic_`
- UC 2.0 only: `ackDeadlineSeconds: 600` and `enableExactlyOnceDelivery: true`. Without these, messages are redelivered and the audit records are duplicated
- The subscription and its topic are used only by this data source (see the note in the Overview)

If the UC reports `Subscription <SUBSCRIPTION> is attached to topic <A>, but expected topic <B>`, the topic in the UC input does not match the subscription's topic in this output. Correct the UC input ([Step 1](#step-1-uc-input-configuration)) or use a subscription that Command 2 lists.

**Command 2: subscriptions attached to the sink topic.**

```
gcloud pubsub topics list-subscriptions $TOPIC --project=$TOPIC_PROJECT
```

**Expected:** the subscription configured in the UC input is listed.

**If it does not match:** a subscription cannot be reattached to another topic. Configure the UC input with a subscription that Command 2 lists. If none is listed, create one.

For a Pub/Sub (Logstash) profile:

```
gcloud pubsub subscriptions create <SUBSCRIPTION_NAME> \
  --topic=$TOPIC \
  --project=$TOPIC_PROJECT \
  --ack-deadline=60 \
  --message-retention-duration=7d
```

For a UC 2.0 (Kafka Connect) profile, enable exactly-once delivery and set the acknowledgement deadline to 600 seconds:

```
gcloud pubsub subscriptions create <SUBSCRIPTION_NAME> \
  --topic=$TOPIC \
  --project=$TOPIC_PROJECT \
  --enable-exactly-once-delivery \
  --ack-deadline=600 \
  --message-retention-duration=7d
```

To correct an existing UC 2.0 subscription:

```
gcloud pubsub subscriptions update $SUBSCRIPTION \
  --project=$TOPIC_PROJECT \
  --enable-exactly-once-delivery \
  --ack-deadline=600
```

A new subscription receives only messages published after its creation. Grant access as described in [Step 3](#step-3-uc-service-account-access), then update the UC input ([Step 1](#step-1-uc-input-configuration)).

## Step 3: UC service account access

Run these commands on a workstation with `gcloud`, `jq` and a copy of the key file uploaded to the Guardium collector.

**Command 1: service account and key ID.**

```
jq -r '.client_email, .private_key_id' $KEY_FILE
export UC_SA_EMAIL="<client_email from the output>"
```

**Command 2: key status.**

```
gcloud iam service-accounts keys list --iam-account=$UC_SA_EMAIL \
  --format="table(name.basename(), validAfterTime, validBeforeTime, disabled, keyType)"
```

**Expected:** the key ID from Command 1 is listed, `disabled` is empty or `False`, and `validBeforeTime` is in the future.

**Command 3: subscriber role.**

```
gcloud pubsub subscriptions get-iam-policy $SUBSCRIPTION --project=$TOPIC_PROJECT

gcloud projects get-iam-policy $TOPIC_PROJECT \
  --flatten="bindings[].members" \
  --filter="bindings.members:serviceAccount:$UC_SA_EMAIL" \
  --format="table(bindings.role)"
```

**Expected:** `roles/pubsub.subscriber` for `serviceAccount:$UC_SA_EMAIL` in either output.

**If it does not match:**

```
gcloud pubsub subscriptions add-iam-policy-binding $SUBSCRIPTION \
  --project=$TOPIC_PROJECT \
  --member="serviceAccount:$UC_SA_EMAIL" \
  --role="roles/pubsub.subscriber"
```

For UC 2.0, the service account also needs `pubsub.subscriptions.get` on the subscription, because the connector reads the subscription's configuration at startup. `roles/pubsub.subscriber` does not include this permission. Grant `roles/pubsub.viewer` on the subscription, or a custom role that includes it:

```
gcloud pubsub subscriptions add-iam-policy-binding $SUBSCRIPTION \
  --project=$TOPIC_PROJECT \
  --member="serviceAccount:$UC_SA_EMAIL" \
  --role="roles/pubsub.viewer"
```

**Command 4: access test with the UC key.** Authenticates as the UC service account and verifies that it can reach the subscription. Do not pass `--auto-ack`; the pulled message is redelivered to the UC.

```
export CLOUDSDK_CONFIG=$(mktemp -d)
gcloud auth activate-service-account --key-file=$KEY_FILE
gcloud pubsub subscriptions describe $SUBSCRIPTION --project=$TOPIC_PROJECT
gcloud pubsub subscriptions pull $SUBSCRIPTION --project=$TOPIC_PROJECT --limit=1
unset CLOUDSDK_CONFIG
```

**Expected:**

- `describe` returns the subscription details. (For UC 2.0, the service account must also have `pubsub.subscriptions.get`; a failure here indicates missing `roles/pubsub.viewer`.)
- `pull` returns a message or `Listed 0 items.`

`PERMISSION_DENIED`, `NOT_FOUND` or a key error indicates an IAM, subscription name or key problem.

## Step 4: Message flow

Generate activity on the data source that its audit configuration records (for example queries, inserts or schema changes), wait 2–3 minutes, then open **Monitoring > Metrics Explorer** in the topic project.

| Resource | Metric | Expected | If not |
| --- | --- | --- | --- |
| Pub/Sub Topic `<TOPIC>` | Publish requests (`topic/send_request_count`) | Greater than zero | The sink is not delivering. Go to [Step 5](#step-5-log-sink) |
| Pub/Sub Subscription `<SUBSCRIPTION>` | Acknowledged messages (`subscription/ack_message_count`) | Greater than zero | The UC is not receiving messages. Go to Steps [1](#step-1-uc-input-configuration), [3](#step-3-uc-service-account-access) and [8](#step-8-guardium-collector) |
| Pub/Sub Subscription `<SUBSCRIPTION>` | Unacked messages (`subscription/num_undelivered_messages`) | Low and not increasing | Messages arrive but the UC does not consume them. Go to [Step 8](#step-8-guardium-collector) |
| Pub/Sub Subscription `<SUBSCRIPTION>` | Oldest unacked message age (`subscription/oldest_unacked_message_age`) | Not increasing | The UC pulls but does not acknowledge. For UC 2.0, the connector acknowledges a message only after Kafka confirms the write, so check the connector and task status, the Kafka brokers, and the exactly-once settings in the Kafka Connect worker. A parsing failure does not cause this, because UC 2.0 parses events downstream of the acknowledgement. For Pub/Sub (Logstash), check the pipeline logs. Go to [Step 8](#step-8-guardium-collector) |

## Step 5: Log sink

**Command 1: sink configuration.**

```
gcloud logging sinks describe $SINK_NAME --organization=$ORG_ID
```

**Expected:**

| Field | Expected value |
| --- | --- |
| `destination` | `pubsub.googleapis.com/projects/<TOPIC_PROJECT>/topics/<TOPIC>` |
| `includeChildren` | `true` (organization- and folder-level sinks only) |
| `disabled` | absent or `false` |
| `filter` | Matches the data source's audit entries ([Step 6](#step-6-audit-entries-in-cloud-logging)) |
| `exclusions` | None that match the data source's entries |
| `writerIdentity` | Record this value for Command 3 |

```
export WRITER_IDENTITY="<writerIdentity from the output, including the serviceAccount: prefix>"
```

If `includeChildren` is not `true` on an organization- or folder-level sink:

```
gcloud logging sinks update $SINK_NAME --organization=$ORG_ID --include-children
```

**Command 2: data source location.** Confirm that the monitored resource is in `$DB_PROJECT` using the command for the data source, for example:

```
gcloud sql instances list --project=$DB_PROJECT        # Cloud SQL
bq ls --project_id=$DB_PROJECT                         # BigQuery
gcloud spanner instances list --project=$DB_PROJECT    # Spanner
```

**Expected:** the monitored resource is listed. If the filter restricts `logName`, it must reference this project ID, not the project name or number.

**If the data source is in a different project, or the filter does not match its entries:** update the filter. The general form is:

```
gcloud logging sinks update $SINK_NAME --organization=$ORG_ID \
  --log-filter='resource.type="<RESOURCE_TYPE>" AND LOG_ID("<LOG_ID>") <ADDITIONAL_CONDITIONS>'
```

Take `<RESOURCE_TYPE>`, `<LOG_ID>` and any additional conditions from the [Data source reference](#data-source-reference) and the Guardium plugin documentation for the data source. Without a `logName` restriction, an organization-level sink with `includeChildren` matches entries from all child projects. To restrict it to specific projects, add `AND logName:("projects/<PROJECT_A>/logs/" OR "projects/<PROJECT_B>/logs/")`.

Each sink must route only one database type to its own topic. If a topic receives entries from more than one data source, log type or application, the Guardium plugin cannot parse the entries it does not support. This results in parsing errors or incorrect audit records. Split the sink, so that each database type has its own sink, topic and subscription.

**Command 3: writer identity permission.**

```
gcloud pubsub topics get-iam-policy $TOPIC --project=$TOPIC_PROJECT
```

**Expected:** `$WRITER_IDENTITY` holds `roles/pubsub.publisher`. Organization-level sinks use an identity of the form `service-org-<ORG_ID>@gcp-sa-logging.iam.gserviceaccount.com`; a grant to a project-level logging identity (`service-<PROJECT_NUMBER>@gcp-sa-logging...`) does not apply to an organization-level sink.

**If it does not match:**

```
gcloud pubsub topics add-iam-policy-binding $TOPIC \
  --project=$TOPIC_PROJECT \
  --member="$WRITER_IDENTITY" \
  --role="roles/pubsub.publisher"
```

If the grant is rejected, review the `iam.allowedPolicyMemberDomains` organization policy on the topic project.

**Command 4: VPC Service Controls violations** (only if VPC Service Controls is in use).

```
gcloud logging read \
  'protoPayload.metadata.@type="type.googleapis.com/google.cloud.audit.VpcServiceControlAuditMetadata"' \
  --project=$TOPIC_PROJECT --freshness=1d --limit=10
```

**Expected:** no results. Violations that reference Pub/Sub require an ingress rule for `$WRITER_IDENTITY`, and for `$UC_SA_EMAIL` if the Guardium collector is outside the perimeter.

Sink delivery errors are also shown under **Logging > Log Router** at the level where the sink is defined.

## Step 6: Audit entries in Cloud Logging

Generate activity on the data source, then query the data source project:

```
gcloud logging read "
  resource.type=\"$RESOURCE_TYPE\"
  AND protoPayload.serviceName=\"$SERVICE_NAME\"
  AND logName=\"projects/$DB_PROJECT/logs/cloudaudit.googleapis.com%2Fdata_access\"
" --project=$DB_PROJECT --freshness=1h --limit=5
```

If the data source writes its audit records to a log other than Data Access audit logs, replace the `logName` condition with the log listed in the [Data source reference](#data-source-reference). If the entries are not Cloud Audit Logs (AlloyDB and Apache Solr), also remove the `protoPayload.serviceName` condition.

**Expected:** recent entries for the monitored resource, with the user, database or dataset, and statement or method of the activity generated above.

Retrieve the sink filter and run it unchanged against the same project:

```
gcloud logging sinks describe $SINK_NAME --organization=$ORG_ID --format="value(filter)"

gcloud logging read '<SINK_FILTER>' --project=$DB_PROJECT --freshness=1h --limit=5
```

**Expected:** the same entries as the first query.

| Result | Cause |
| --- | --- |
| First query returns entries; sink filter query does not | Sink filter ([Step 5](#step-5-log-sink)) |
| First query returns nothing | Data source audit configuration ([Step 7](#step-7-data-source-audit-configuration)) |

## Step 7: Data source audit configuration

**Command 1: Data Access audit logs.** Most data sources require Data Access audit logs to be enabled for their service. AlloyDB and Apache Solr do not use Cloud Audit Logs; skip Command 1 for them and go to Command 2. The project policy does not include settings inherited from a folder or the organization, so check each level:

```
gcloud projects get-iam-policy $DB_PROJECT --format=json | jq '.auditConfigs'
gcloud resource-manager folders get-iam-policy <FOLDER_ID> --format=json | jq '.auditConfigs'
gcloud organizations get-iam-policy $ORG_ID --format=json | jq '.auditConfigs'
```

**Expected:** an entry for `$SERVICE_NAME` or `allServices` that includes the log types the data source requires (usually `DATA_READ` and `DATA_WRITE`):

```
{
  "service": "<SERVICE_NAME>",
  "auditLogConfigs": [
    { "logType": "DATA_READ" },
    { "logType": "DATA_WRITE" }
  ]
}
```

Check also that the service account or user generating the activity is not listed under `exemptedMembers`.

**If it does not match:** enable Data Read and Data Write for the service under **IAM & Admin > Audit Logs**, then repeat [Step 6](#step-6-audit-entries-in-cloud-logging).

**Command 2: data source audit settings.** Some data sources require audit logging to be enabled on the resource itself. Check the settings listed for the data source in the [Data source reference](#data-source-reference), correct them if required, and repeat [Step 6](#step-6-audit-entries-in-cloud-logging).

## Step 8: Guardium collector

| Check | Expected | Action |
| --- | --- | --- |
| Connector status | Enabled and running | Correct the configuration ([Step 1](#step-1-uc-input-configuration)) and restart the connector |
| Plugin | Plugin for the data source over Pub/Sub, supported by the Guardium version (see [Data source reference](#data-source-reference)) | A mismatched plugin pulls messages but parses none |
| Outbound connectivity | HTTPS (443) to `pubsub.googleapis.com`, `oauth2.googleapis.com`, `www.googleapis.com` | Open the firewall, or configure the proxy (for UC 2.0, in the *Google Cloud Credentials* entry) |
| System clock | Synchronized with NTP | Clock skew causes OAuth token failures |
| Key file | Same key verified in [Step 3](#step-3-uc-service-account-access) | Upload the verified key and restart the connector |
| UC logs | No `PERMISSION_DENIED`, `UNAUTHENTICATED`, `NOT_FOUND`, timeout or parse errors. For UC 2.0, also no `SUBSCRIPTION_INIT_ERROR` or `SUBSCRIPTION_SEEK_ERROR` events | Resolve according to the corresponding step |

A UC data source is listed in S-TAP Status only after its first event is parsed.

## Data source reference

The values below are common starting points. Consult the plugin documentation linked in the table for the authoritative log, filter and audit settings for each data source.

**Universal Connectors plugins.**

| Data source | Pub/Sub (Logstash) plugin | UC 2.0 (Kafka Connect) plugin | Pub/Sub (Logstash) documentation | UC 2.0 (Kafka Connect) documentation |
| --- | --- | --- | --- | --- |
| Cloud SQL for PostgreSQL | PostgreSQL Over Pub Sub | GCP Postgres Over PubSub Connect 2.0 | [logstash-filter-pubsub-postgresql-guardium](../../filter-plugin/logstash-filter-pubsub-postgresql-guardium/README.md) | [PostgresqlPubsubKafkaConnect](../KafkaBasedUCs/PostgresqlPubsubKafkaConnect.md) |
| Cloud SQL for MySQL | MySQL Over Pub Sub | GCP MySQL Over PubSub Connect 2.0 | [logstash-filter-pubsub-mysql-guardium](../../filter-plugin/logstash-filter-pubsub-mysql-guardium/README.md) | Same as the Pub/Sub (Logstash) documentation. There is no separate UC 2.0 guide |
| AlloyDB | GCP AlloyDB Over PubSub | GCP AlloyDB Over PubSub Connect 2.0 | [logstash-filter-alloydb-guardium](../../filter-plugin/logstash-filter-alloydb-guardium/README.md) | [AlloyDBPubsubKafkaConnect](../KafkaBasedUCs/AlloyDBPubsubKafkaConnect.md) |
| BigQuery | GCP Big Query Over PubSub | GCP BigQuery Over PubSub Connect 2.0 | [logstash-filter-pubsub-bigquery-guardium](../../filter-plugin/logstash-filter-pubsub-bigquery-guardium/README.md) | [BigqueryPubsubKafkaConnect](../KafkaBasedUCs/BigqueryPubsubKafkaConnect.md) |
| Bigtable | GCP BigTable Over PubSub | GCP BigTable Over PubSub Connect 2.0 | [logstash-filter-pubsub-bigtable-guardium](../../filter-plugin/logstash-filter-pubsub-bigtable-guardium/README.md) | [BigtablePubsubKafkaConnect](../KafkaBasedUCs/BigtablePubsubKafkaConnect.md) |
| Spanner | Spanner Over Pub Sub | GCP Spanner Over PubSub Connect 2.0 | [logstash-filter-pubsub-spanner-guardium](../../filter-plugin/logstash-filter-pubsub-spanner-guardium/README.md) | [SpannerPubsubKafkaConnect](../KafkaBasedUCs/SpannerPubsubKafkaConnect.md) |
| Apache Solr | Apache solr Over Pub Sub | GCP ApacheSolr Over PubSub Connect 2.0 | [logstash-filter-pubsub-apachesolr-guardium](../../filter-plugin/logstash-filter-pubsub-apachesolr-guardium/README.md) | [ApachesolrPubsubkafkaConnect](../KafkaBasedUCs/ApachesolrPubsubkafkaConnect.md) |
| Firestore | FireStore Over Pub Sub | Not available | [logstash-filter-pubsub-firestore-guardium](../../filter-plugin/logstash-filter-pubsub-firestore-guardium/README.md) | Not available |
| Firebase | Firebase Over Google PubSub | Not available | [logstash-filter-pubsub-firebase-realtime-guardium](../../filter-plugin/logstash-filter-pubsub-firebase-realtime-guardium/README.md) | Not available |

**Log and audit settings.** Values are taken from the plugin documentation linked above.

| Data source | `RESOURCE_TYPE` | `SERVICE_NAME` | Log | Additional sink filter condition | Resource-level audit settings |
| --- | --- | --- | --- | --- | --- |
| Cloud SQL for PostgreSQL | `cloudsql_database` | `cloudsql.googleapis.com` | `cloudaudit.googleapis.com/data_access` and `cloudsql.googleapis.com/postgres.log` | For the Data Access log: `protoPayload.request.@type="type.googleapis.com/google.cloud.sql.audit.v1.PgAuditEntry"` | Flags `cloudsql.enable_pgaudit=on` and `pgaudit.log=all`; `CREATE EXTENSION pgaudit;` in each audited database |
| Cloud SQL for MySQL | `cloudsql_database` | `cloudsql.googleapis.com` | `cloudaudit.googleapis.com/data_access` and `cloudsql.googleapis.com/mysql.err` | For the Data Access log: `protoPayload.methodName="cloudsql.instances.query"` | Flags `cloudsql_mysql_audit=ON` and `audit_log=ON`; audit rule created with `CALL mysql.cloudsql_create_audit_rule(...)` |
| AlloyDB | `alloydb.googleapis.com/Instance` | Not applicable. The entries are database logs, not Cloud Audit Logs | `alloydb.googleapis.com/postgres.log` | None | Flag `log_statement: all` |
| BigQuery | `bigquery_project` | `bigquery.googleapis.com` | `cloudaudit.googleapis.com/data_access` | Job status, dataset and table conditions as in the plugin documentation | None. Data Access audit logs are enabled by default for BigQuery |
| Bigtable | Not restricted by the plugin filter | `bigtableadmin.googleapis.com` and `bigtable.googleapis.com` | `cloudaudit.googleapis.com/data_access` and `cloudaudit.googleapis.com/activity` | The filter matches on `protoPayload.serviceName` only | Data Access audit logs enabled for the service ([Step 7](#step-7-data-source-audit-configuration)) |
| Spanner | `spanner_instance` | `spanner.googleapis.com` | `cloudaudit.googleapis.com/data_access` and `cloudaudit.googleapis.com/activity` | Request type and query mode conditions as in the plugin documentation | Data Access audit logs enabled for the service ([Step 7](#step-7-data-source-audit-configuration)) |
| Apache Solr | `gce_instance` | Not applicable. The entries are Solr logs, not Cloud Audit Logs | `projects/<PROJECT_ID>/logs/files` | `jsonPayload.message` conditions as in the plugin documentation | Ops Agent configured to send `solr.log` to Cloud Logging |
| Firestore | `datastore_database`, `audited_resource` and `datastore_index` | `firestore.googleapis.com` | See plugin documentation | See plugin documentation | See plugin documentation |
| Firebase | `datastore_database` and `audited_resource` | `firebasedatabase.googleapis.com` | See plugin documentation | See plugin documentation | See plugin documentation |

To confirm the resource type and log name of entries already in Cloud Logging, open **Logging > Logs Explorer** in the data source project, generate activity, and inspect `resource.type`, `logName` and `protoPayload.serviceName` on a matching entry.