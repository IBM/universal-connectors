import boto3
import os
import re
from datetime import datetime, timedelta

GROUP_NAME = os.environ.get('GROUP_NAME')
QUEUE_NAME = os.environ.get('QUEUE_NAME')
PARAMETER_NAME = os.environ.get('PARAMETER_NAME', 'LastExecutionTimestamp')
IS_DEBUG = bool(os.environ.get('ENABLE_DEBUG', "")) #String with blank value always returns false.


def get_last_execution_timestamp(ssm_client):
    try:
        response = ssm_client.get_parameter(Name=PARAMETER_NAME)
        return int(response['Parameter']['Value'])
    except ssm_client.exceptions.ParameterNotFound:
        print("LastExecutionTimestamp not found in the Paramterstore, starting from the beginning: ",0)
        return 0

def set_last_execution_timestamp(ssm_client, timestamp):
    ssm_client.put_parameter(
        Name=PARAMETER_NAME,
        Value=str(timestamp),
        Type='String',
        Overwrite=True
    )

def lambda_handler(event, context):
    ssm_client = boto3.client('ssm')
    logs_client = boto3.client('logs')
    sqs_client = boto3.client('sqs')
    
    
    init_time_sec = context.get_remaining_time_in_millis()/1000;
    # Validate environment variables
    if not GROUP_NAME or not QUEUE_NAME:
        raise ValueError("GROUP_NAME and QUEUE_NAME environment variables must be set.")

    current_time = datetime.now()
    current_time_epoch = int(current_time.timestamp() * 1000)

    # Retrieve the last execution timestamp from SSM
    last_execution_timestamp = get_last_execution_timestamp(ssm_client)

    
    from_date = last_execution_timestamp + 1
    to_date = current_time_epoch

    # Fetch log streams
    log_group_details = logs_client.describe_log_streams(
        logGroupName=GROUP_NAME,
        orderBy='LastEventTime',
        descending=True
    )

    for log_stream in log_group_details['logStreams']:
        log_stream_name = log_stream['logStreamName']
        next_token = None

        while True:
            
            #Exiting the Loop just before the lambda timeout
            remaining_time_sec = context.get_remaining_time_in_millis()/1000
            if IS_DEBUG:
                print("Remaining time of exit for the function: ",remaining_time_sec)
            
            if int(remaining_time_sec) <= 5:
                return
        
            if next_token:
                response = logs_client.get_log_events(
                    logGroupName=GROUP_NAME,
                    logStreamName=log_stream_name,
                    startTime=from_date,
                    endTime=to_date,
                    startFromHead=True,
                    nextToken=next_token,
                    limit = 1000
                )
            else:
                response = logs_client.get_log_events(
                    logGroupName=GROUP_NAME,
                    logStreamName=log_stream_name,
                    startTime=from_date,
                    endTime=to_date,
                    startFromHead=True,
                    limit = 1000
                )
            
            if IS_DEBUG:
                print("response: ", response)
             # Check if there are more events to fetch
            next_token = response.get('nextForwardToken')
            
            # comparing the next token and the backward token by skipping the first characters of both.
            if not next_token or next_token[1:] == response.get('nextBackwardToken')[1:]:
                break
            
            
            for event in response['events']:
                message = event['message']
                
                match_error = re.search(":ERROR:", message)
                match_statement = re.search(":STATEMENT:", message)
                match_detail = re.search(":DETAIL:", message)
                match_hint = re.search(":HINT:", message)
                match_wal_stmt_start = re.search("checkpoint starting:", message)
                match_wal_stmt_complete = re.search("checkpoint complete:", message)
                current_user_stmt = re.search("current_schema(),session_user", message)

                if match_error:
                    new_message = message + '|||'
                elif match_statement:
                    final_message = new_message + message
                    final_message = re.sub(r'[\r\t\n]', ' ', final_message)
                    event['message'] = final_message + '++'
                    sqs_client.send_message(
                        QueueUrl=QUEUE_NAME,
                        MessageBody=str(event)
                    )
                elif match_detail or match_hint or match_wal_stmt_start or match_wal_stmt_complete or current_user_stmt:
                    continue
                else:
                    event['message'] = re.sub(r'[\r\t\n]', ' ', message) + '++'
                    sqs_client.send_message(
                        QueueUrl=QUEUE_NAME,
                        MessageBody=str(event)
                    )
            
            # Store the current execution timestamp in SSM
            events = response['events']
            if events:
                last_event = events[-1]
                last_timestamp = last_event['timestamp']
                if IS_DEBUG:
                    print("last_timestamp", last_timestamp)
                set_last_execution_timestamp(ssm_client, last_timestamp)

