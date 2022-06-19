# Configuring a univer***REMOVED***l connector
##Before you begin
Each data source type has a specific plug-in that filters and parses the events from the data source, and converts them to a standard Guardium format.
The first time that you add a connector of a specific data source type, you need to upload its plug-in. On the [available_plugins.md](https://github.com/IBM/univer***REMOVED***l-connectors/blob/main/docs/available_plugins.md) page, refer to the "Downloads" column for the list of plug-ins available for GI. Click the "GI" hyperlink for the plug-in you wish to download.

##About this task
The univer***REMOVED***l connector configuration has a few parts, all described in this task:
- Uploading a plug-in that configures the filter or parser to convert the data source events to a standard Guardium format.
- Configuring the connection between Guardium Insights and the data source.
- Downloading the certificate (when using Filebeat input type).

##Procedure
1. Click **Manage > UC plug-ins**.
2. If the plug-in is already installed, proceed to step 3. If the plug-in for the specific data source type is not installed, click **Add plug-in**. Go to the directory with the plug-ins you already downloaded, select the .zip file for this data source type, and click **Open**.
3. Navigate back to the Connections page (**Settings > Connections**).
4. Click **Add connection**. <br />
   The Connect to new data source page opens, with a card for each data source type whose plug-in is already installed.
5. The data source type. This opens a panel that aids you in initiating the connection:
   1. **Select data source environment**: Select the environment that hosts your data source.
   2. **Select connection method**: Select **Univer***REMOVED***l Connector**.
   3. The remainder of the panel provides **Additional information** about the connection type that you are creating.
   4. After reading the **Addition information**, click **Configure**.
6. Enter the details for this connection.
   1. In the Name and description page, enter this information:
      1. **Name**: A unique name for the connection. This name distinguishes this connection from all other Guardium Insights connections.
      2. **Description**: Enter a description for the connection.
   2. Click **Next**.
   3. In the Build pipeline page, use the **Choose input plug-in** menu to select your input plug-in. Then select a filter plug-in by using the **Choose a filter plug-in** menu.
   4. Click **Next**.
   5. Follow the instructions according to the input plug-in type you selected on step 3:
   ###<br />**Filebeat**<br />
      1. In the Additional info page, enter a **Data source tag**: This tag identifies the plug-in that is associated with this connector.
      Use this tag in the filebeat.yml configuration of the data sources whose type matches this plug-in (step [2]() in [Configuring Filebeat to forward audit logs to Guardium]()).
      The data source sends the tag with every event. For example, specify any-mongodb in this field, and configure Filebeat with the ***REMOVED***me tag for MongoDB activity logs coming from your MongoDB data source.
      2. Click **Configure**.
      3. In the Configuration notes page, click Download certificate to **download the certificate** to your local system. Copy the certificate to the data source (step [4]() in [Configuring Filebeat to forward audit logs to Guardium]()). All data sources of any one specific type use the ***REMOVED***me certificate.
      4. Click **Done** 
      5. Follow the instructions to configure the data source to communicate with Guardium Insights. Copy the hostname in the Configuration Notes to configure the host in the filebeat.yml file on your datasource ([Configuring Filebeat to forward audit logs to Guardium]()).

    ### <br />**Cloudwatch_logs**<br />
In the Additional info page, specify the details of the connection you want to create:
* **AWS Role ARN (optional)** - this is used to generate temporary credentials, typically for cross-account access. See the [AssumeRole API documentation](https://docs.aws.amazon.com/STS/latest/APIReference/API_AssumeRole.html) for more information.
* **AWS access key id** and **AWS secret access key** - your AWS user account access key and the secret access key (for more information, click [here](https://docs.aws.amazon.com/powershell/latest/userguide/pstools-appendix-sign-up.html)).
* **AWS account region** - for example, "us-east-1"
* **Event filter** (optional) - specify the filters to apply when fetching resources. For example, for filtering an S3 events based on bucket name: '{$.eventSource="s3.amazonaws.com" && $.requestParameters.bucketName= "<BUCKET_NAME>"}'
* **Account id** (optional) - your AWS account ID (For more information, click [here](https://docs.aws.amazon.com/IAM/latest/UserGuide/console_account-alias.html#FindingYourAWSId))
* **Cloudwatch Log Group name** - specify the log group that is created for your data instance. For example /aws/rds/instance/<instance_name>/<log_group_name>

##Configuring Filebeat to forward audit logs to Guardium
##Procedure
1. Open the file filebeat.yml, usually located in /etc/filebeat/filebeat.yml.
2. In the tags section, enter the value of Data source tag that you defined when you added a connection [in this step](6.5.1). <br />For example,<br />
    ``` tags: ["any-tag-name"]```
3. In the Logstash Output section, enter the hostname URL from the Configuration Notes popup (Configuring a univer***REMOVED***l connector, step [6.5.7]()).
   <br />For example,<br />
```  hosts: ["<hostname-URL>:5044"]```
  <br />**NOTE**: In GI, whenever using plug-ins that are based on Filebeat as a data shipper, the configured port should be 5044. Guardium Insights will map this to an internal port.     <br />
4. Copy the location of the downloaded certificate, in the ssl.certificate_authorities row (Configuring a univer***REMOVED***l connector, step 6.g). <br />For example,<br />
```  ssl.certificate_authorities: ["/etc/pki/ca-trust/GuardiumInsightsCA.pem"]```
5. Restart Filebeat to effect these changes.
   <br />Linux: Enter the command:<br />
   ``` sudo service filebeat restart ```
6. Windows: Restart in the Services window
