import boto3
import base64
import gzip
import json
import os

REGION              = os.environ.get('REGION', 'us-east-1')
ENRICHED_LOG_GROUP  = os.environ['ENRICHED_LOG_GROUP']
ENRICHED_LOG_STREAM = os.environ.get('ENRICHED_LOG_STREAM', 'enriched-stream')

athena = boto3.client('athena', region_name=REGION)
logs   = boto3.client('logs',   region_name=REGION)

def lambda_handler(event, context):
    # CloudWatch delivers log data as base64-encoded gzip
    print("Received event:", json.dumps(event))
    raw       = event['awslogs']['data']
    decoded   = base64.b64decode(raw)
    log_data  = json.loads(gzip.decompress(decoded))

    log_events_out = []

    for log_event in log_data['logEvents']:
        try:
            record = json.loads(log_event['message'])
        except Exception:
            continue

        # Only process Athena StartQueryExecution events
        if record.get('eventName') != 'StartQueryExecution':
            continue
        if record.get('eventSource') != 'athena.amazonaws.com':
            continue

        query_execution_id = (
            record
            .get('responseElements', {})
            .get('queryExecutionId')
        )

        if not query_execution_id:
            continue

        # Fetch the full query string from Athena
        try:
            resp            = athena.get_query_execution(
                                  QueryExecutionId=query_execution_id
                              )
            query_string    = resp['QueryExecution']['Query']
            # NOTE: status is often RUNNING at this point because CloudTrail
            # fires on StartQueryExecution, before the query finishes.
            query_status    = resp['QueryExecution']['Status']['State']
            database        = (resp['QueryExecution']
                               .get('QueryExecutionContext', {})
                               .get('Database', 'unknown'))
        except Exception as e:
            print(f"Failed to get query execution {query_execution_id}: {e}")
            query_string = 'UNAVAILABLE'
            query_status = 'UNKNOWN'
            database     = 'unknown'

        # Build enriched event — inject queryString back in
        enriched = dict(record)
        enriched['requestParameters'] = dict(
            record.get('requestParameters', {})
        )
        enriched['requestParameters']['queryString'] = query_string
        enriched['requestParameters']['queryStatus'] = query_status
        enriched['requestParameters']['queryExecutionContext'] = {
            'database': database,
            'catalog': 'AwsDataCatalog'
        }

        log_events_out.append({
            'timestamp': log_event['timestamp'],
            'message':   json.dumps(enriched)
        })

    if not log_events_out:
        return {'statusCode': 200, 'body': 'No Athena events to enrich'}

    # Ensure log group and stream exist
    ensure_log_stream()

    # Write enriched events to the new log group
    logs.put_log_events(
        logGroupName=ENRICHED_LOG_GROUP,
        logStreamName=ENRICHED_LOG_STREAM,
        logEvents=sorted(log_events_out, key=lambda e: e['timestamp'])
    )

    print(f"Wrote {len(log_events_out)} enriched events")
    return {'statusCode': 200, 'body': f'Enriched {len(log_events_out)} events'}


def ensure_log_stream():
    # Create log group if it doesn't exist
    try:
        logs.create_log_group(logGroupName=ENRICHED_LOG_GROUP)
    except logs.exceptions.ResourceAlreadyExistsException:
        pass

    # Create log stream if it doesn't exist
    try:
        logs.create_log_stream(
            logGroupName=ENRICHED_LOG_GROUP,
            logStreamName=ENRICHED_LOG_STREAM
        )
    except logs.exceptions.ResourceAlreadyExistsException:
        pass