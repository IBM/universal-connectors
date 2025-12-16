import boto3
import os
import re
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
            match = re.search(":ERROR:", event['message'])
            match1 = re.search(":STATEMENT:", event['message'])
            match2 = re.search(":DETAIL:", event['message'])
            match3 = re.search(":HINT:", event['message'])
            match4 = re.search(":LOCATION:", event['message'])
            
            if match:
                newMessage = event['message'] + '|||'
                continue
            elif match2:
                continue
            elif match3:
                continue
            elif match4:
                continue
            elif match1:
                finalMessage = newMessage + event['message']
                finalMessage = finalMessage.replace("\r", ' ')
                finalMessage = finalMessage.replace("\t", ' ')
                finalMessage = finalMessage.replace("\n", ' ')
                event['message'] = finalMessage + '++'
                sqs.send_message(
                    QueueUrl = QUEUE_NAME,
                    MessageBody=str(event)
                )
            else:
               newMessage = event['message'].replace("\r", ' ')
               newMessage = newMessage.replace("\t", ' ')
               newMessage = newMessage.replace("\n", ' ')
               event['message'] = newMessage + '++'
               sqs.send_message(
                    QueueUrl = QUEUE_NAME,
                    MessageBody=str(event)
                 )
                
        print("New response",response)