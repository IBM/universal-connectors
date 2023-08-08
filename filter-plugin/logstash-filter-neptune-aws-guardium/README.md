# Neptune-Guardium Logstash filter plug-in
### Meet Neptune
* Tested versions: 1.1
* Environment: AWS
* Supported inputs: CloudWatch (pull)
* Supported Guardium versions:
    * Guardium Data Protection: 11.4 and above
    * Guardium Insights: 3.2
    * Guardium Insights SaaS: 1.0

This is a [Logstash](https://github.com/elastic/logstash) filter plug-in for the universal connector that is featured in IBM Security Guardium. It parses events and messages from the Neptune audit logs into a [Guardium record](https://github.com/IBM/universal-connectors/blob/main/common/src/main/java/com/ibm/guardium/universalconnector/commons/structures/Record.java) instance (which is a standard structure made out of several parts). The information is then sent over to Guardium. Guardium records include the accessor (the person who tried to access the data), the sessionLocator, data, and exceptions. If there are no errors, the data contains details about the query "construct". The construct details the main action (verb) and collections (objects) involved. The Neptune plug-in only supports Guardium Data Protection as of now.

Neptune plug-in supports the Apache TinkerPop Gremlin and W3C's SPARQL queries.

The plug-in is free and open-source (Apache 2.0). It can be used as a starting point to develop additional filter plug-ins for the Guardium universal connector.


## 1. Configuring the AWS Neptune service/cluster

### Procedure:

 1. Go to https://console.aws.amazon.com/.
 2. Search for and navigate to the AWS Neptune management Console. Click on _Launch Amazon  Neptune_.​

## 2. Enabling the Neptune Audit extension

The only way to enable the audit logs is by setting the value from 0 to 1 in the parameter confuguration. After that, the audit logs will be enabled.

### Steps to enable Neptune Audit

 1. Creating the database parameter group.
 2. Enabling Neptune Auditing by setting the value from 0 to 1 in parameter configuration.
 3. Creating the database and associating parameter group.

#### Creating the database parameter group

When you create a database instance, it is associated with the default parameter group. Follow these steps to create a new parameter group:

##### Procedure:

 1. Go to Services > Database > Parameter groups.
 2. Click Create Parameter Group in the left pane.
 3. Enter the parameter group details.
 
    * Select "DB Cluster Parameter Group" from the Type drop-down menu.

    * Enter the DB parameter group name.

    * Enter the DB parameter group description.

4. Click Save. The new group appears in the Parameter Groups section.

#### Enabling Neptune Audit Logs

##### Procedure:


 1. Click on the newly created Parameter Group from the Cluster Parameter Groups list.
 2. Click on the "edit parameters" for configuration changes.
 3. Select "1" instead of "0" from the value drop-down menu for neptune_enable_audit_log. Neptune audit logs will now be allowed. 
 4. Finally, click on the save changes button to save the changes.  

#### Creating the database and associating parameter group

##### Procedure:

 1. The Amazon Neptune console, choose Databases from the left panel. And click on "Create database" in the landing page.
 2. Choose the default version of Neptune, which will be auto-populated.
 3. Provide "DB cluster identifier" and select the Development and Testing option.   
 4. Click on the checkbox for "Create notebook" under the  Notebook configuration and provide the  Notebook name.
 5. Select the existing IAM Role option and provide it.
 6. Under the additional configuration, the following things need to be provided.

    * Provide the DB instance identifier(optional).

    * Select the created DB parameter group name from the drop-down menu for "DB cluster parameter group" and select "Audit log" of Log exports.

 7. Finally, click on the Create database button. It will take a few minutes to finish the process. You can connect to this cluster when both the cluster and instance status show as Available.

## 3. Viewing the Neptune Audit logs

The Neptune Audit can be seen via CloudWatch only.

### Viewing the log entries on CloudWatch
By default, each database instance has an associated log group with a name in this format: /aws/neptune/<db_cluster_name>/audit . You can use this log group, or you can create a new one and associate it with the database instance.

#### Procedure

 1. On the AWS Console page, open the Services menu.
 2. Enter the CloudWatch string in the search box.
 3. Click CloudWatch to redirect to the CloudWatch dashboard.
 4. In the left panel, select Logs.
 5. Click Log Groups.

### Note
_If user is facing connectivity issue while executing queries on notebook, please ensure that the necessary permissions have been granted on "security group" for the port on which the Neptune instance is running_. 

## 4. Configuring the Neptune filter in Guardium
The Guardium universal connector is the Guardium entry point for native audit logs. The Guardium universal connector identifies and parses the received events, and converts them to a standard Guardium format. The output of the Guardium universal connector is forwarded to the Guardium sniffer on the collector, for policy and auditing enforcements. Configure Guardium to read the native audit logs by customizing the Neptune template.

### Authorizing outgoing traffic from AWS to Guardium

#### Procedure

 1. Log in to the Guardium API.
 2. Issue these commands:<br>
      • grdapi add_domain_to_universal_connector_allowed_domains domain=amazonaws.com <br>
		• grdapi add_domain_to_universal_connector_allowed_domains domain=amazon.com
    

#### Before you begin

*  Configure the policies you require. See [policies](/docs/#policies) for more information.

* You must have permission for the S-Tap Management role. The admin user includes this role by  default.

* Download the [guardium_logstash-offline-plugin-neptune.zip plug-in](https://github.com/IBM/universal-connectors/raw/main/filter-plugin/logstash-filter-neptune-aws-guardium/NeptuneOverCloudWatchPackage/Neptune/guardium_logstash-offline-plugin-neptune.zip). This is not necessary for Guardium Data Protection v12.0 and later.


#### Procedure

 1. On the collector, go to Setup > Tools and Views > Configure Universal Connector.
 2. Enable the connector if it is already disabled, before uploading the UC.
 3. Click Upload File and select the offline [guardium_logstash-offline-plugin-neptune.zip](https://github.com/IBM/universal-connectors/raw/main/filter-plugin/logstash-filter-neptune-aws-guardium/NeptuneOverCloudWatchPackage/Neptune/guardium_logstash-offline-plugin-neptune.zip) plug-in. After it is uploaded, click OK. This is not necessary for Guardium Data Protection v12.0 and later.
 4. Click the Plus icon to open the Connector Configuration dialog box.
 5. Type a name in the Connector name field.
 6. Update the input section to add the details from [Neptune.conf](https://github.com/IBM/universal-connectors/raw/main/filter-plugin/logstash-filter-neptune-aws-guardium/neptune.conf) file's input  part, omitting the keyword "input{" at the beginning and its corresponding "}" at the end.
 7. Update the filter section to add the details from [Neptune.conf](https://github.com/IBM/universal-connectors/raw/main/filter-plugin/logstash-filter-neptune-aws-guardium/neptune.conf) file's filter part, omitting the keyword "filter{" at the beginning and its corresponding "}" at the end.
 8. The _"type"_ field should match in the input and filter configuration sections. This field should be unique for every individual connector added.
 9. Click Save. Guardium validates the new connector, and enables the universal connector if it was disabled. After it is validated, it appears in the Configure Universal Connector page.

## 5. Limitations

1. The following important fields couldn't be mapped with Neptune audit logs
     - SourceProgram  : field is left blank since this information is not embedded in the  messages pulled from AWS Cloudwatch. 
     - OS User         : Not available with Audit logs
     - Client HostName : Not available with Audit logs
     - The Neptune audit log doesn’t include error logs, so in Guardium we will not be able to show this in the in SQL_ERROR & LOGIN_FAILED report.In cases of invalid queries, an error message will appear in the Guardium logs instead of records.

	
## 6. Configuring the AWS Neptune Guardium Logstash filters in Guardium Insights

To configure this plug-in for Guardium Insights, follow [this guide.](/docs/Guardium%20Insights/3.2.x/UC_Configuration_GI.md)

For the input configuration step, refer to the [CloudWatch_logs section](/docs/Guardium%20Insights/3.2.x/UC_Configuration_GI.md#configuring-a-CloudWatch-input-plug-in).

