## Configure GCP for the input plug-in

### Create a topic in Pub/Sub
* Go to the Pub/Sub topics page in the Cloud Console. 
* Click ```Create a topic```
* In the Topic ID field, provide a unique topic name, for example, MyTopic.
* Click ```Create Topic```.

### Create a subscription in Pub/Sub
* Display the menu for the topic created in the previous step and click ```New subscription```.
* Type a name for the subscription, such as MySub.
* Leave the delivery type as Pull.
* Click ```Create```

### Create a log sink in Pub/Sub
* In the Cloud Console, go to the Logging > Log Router page.
* Click ```Create sink```.
* In the Sink details panel, enter the following details:
* Sink name: Provide an identifier for the sink. Note that after you create the sink you cannot rename it. However, you can delete a sink and create a new one.
* Sink description (optional): Describe the purpose or use case for the sink.
* In the Sink destination panel, select the Pub/Sub topic as sink service and destination.
* Choose logs to include in the sink in the Build inclusion filter panel.
* You can filter the logs by log name, resource, and  severity.
Multi-region
* In cases of multiple regions, you need to do the same set of configurations per each region.
Based on the region, different configuration files will be used for the input plug-in

#### Set destination (TOPIC & SUBSCRIPTION) permissions

To set permissions for the log sink to route to its destination, do the following:
* Obtain the sink's writer identity—an email address—from the new sink.
   1. Go to the Log Router page, and select ```menu```  > ```View sink details```.
   2. The writer identity appears in the Sink details panel.
* If you have owner access to the destination:
   1. add the sink's writer identity to topic and give it the Pub/Sub Publisher role and subscriber role.
   2. add the sink's writer identity to subscription and give it the Pub/Sub subscriber role.

### Create service account credentials
* Go to the Service accounts section of the IAM & Admin console.
* Select ```project``` and click ```Create Service Account```.
* Enter a Service account name, such as Bigquery-pubsub.
* Click ```Create```.
* The owner role is required for the service account. Select the owner role from the drop-down menu
* Click ```Continue```. You do not need to grant users access to this service account.
* Click ```Create Key```. The key is used by the Logstash input plug-in configuration file.
* Select JSON and click ```Create```.

### Inclusion Filter
Edit the Sink via *Logs Router > Sink Inclusion Filter*:
#### Description  
The purpose of this inclusion filter is to exclude unnecessary logs and include required logs with resource types and metadata reason as DELETE,TABLE_INSERT_REQUEST,TABLE_DELETE_REQUEST or CREATE and metadtata jobStatus.
```    
(resource.type=("bigquery_project") AND protoPayload.authenticationInfo.principalEmail:* AND
(protoPayload.metadata.jobChange.job.jobStatus.jobState = DONE AND -protoPayload.metadata.jobChange.job.jobConfig.queryConfig.statementType = "SCRIPT"))
OR
(protoPayload.metadata.datasetDeletion.reason = "DELETE") OR (protoPayload.metadata.tableCreation.reason = "TABLE_INSERT_REQUEST") OR (protoPayload.metadata.tableDeletion.reason = "TABLE_DELETE_REQUEST") OR (protoPayload.metadata.datasetCreation.reason = "CREATE")

```
