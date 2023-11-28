#
# Copyright 2022-2023 IBM Inc. All rights reserved
# SPDX-License-Identifier: Apache2.0
#

import boto3
import os
from datetime import datetime, timedelta
GROUP_NAME = os.environ['GROUP_NAME']
QUEUE_NAME = os.environ['QUEUE_NAME']

def lambda_handler(event, context):

    currentTime = datetime.now()
    StartDate = currentTime  - timedelta(minutes = 2)
    EndDate = currentTime


    fromDate = int(StartDate.timestamp() * 1000)
    toDate = int(EndDate.timestamp() * 1000)


    client = boto3.client('logs')
    sqs = boto3.client('sqs')

    logGroupDetails = client.describe_log_streams(
        logGroupName = GROUP_NAME,
        orderBy = 'LastEventTime',
        descending = True
    )

    for logStream in logGroupDetails['logStreams'] :
        logStreamNameObt =  logStream['logStreamName']
        response = client.get_log_events(
            logGroupName=GROUP_NAME,
            logStreamName=logStreamNameObt,
            startTime=fromDate,
            endTime=toDate,
            startFromHead=True,
        )

        for event in response['events'] :
            sqs.send_message(
                QueueUrl = QUEUE_NAME,
                MessageBody=str(event)
            )

        print("New response",response)