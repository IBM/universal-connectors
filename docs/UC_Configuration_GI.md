# Configuring a universal connector in Guardium Insights


## Before you begin

Each data source type has a specific plug-in that filters and parses the events from the data source, and converts them to a standard Guardium format. The first time that you add a connector of a specific data source type, you need to upload its plug-in. On the [available\_plugins.md](https://github.com/IBM/universal-connectors/blob/main/docs/available_plugins.md) page, refer to the "Downloads" column for the list of plug-ins available for GI. Click the "GI" hyperlink for the plug-in you wish to download.

## About this task


The universal connector configuration has a few parts, all described in this task:

*   Uploading a plug-in that configures the filter or parser to convert the data source events to a standard Guardium format
*   Configuring the connection between Guardium Insights and the data source
*   Downloading the certificate (when using Filebeat input type)

## Procedure


1. Click **Connections** in the **Settings** menu.
    
2. Click **Manage > UC plug-ins**.
    
3. If the plug-in is already installed, proceed to <em>**step 4**</em>. If the plug-in for the specific data source type is not installed, click **Add plug-in**. Go to the directory with the plug-ins you already downloaded, select the .zip file for this data source type, and click **Open**.
    
4. Navigate back to the Connections page (**Settings > Connections**).

    
5.  Click **Add connection.**  The Connect to new data source page opens, with a card for each data source type whose plug-in is already installed.
    
6.  Select the data source type. This opens a panel that aids you in initiating the connection.
    
    a **Select data source environment**: Select the environment that hosts your data source.
    
    b. **Select connection method**: Select **Universal Connector.**
    
    c. The remainder of the panel provides **Additional information** about the connection type that you are creating.
    
    d. After reading the **Additional information**, click **Configure**.
    
7.  Enter the details for this connection.
    
    a. In the Name and description page, enter this information:
    
      i. **Name**: A unique name for the connection. This name distinguishes this connection from all other Guardium Insights connections.
    
      ii. **Description**: Enter a description for the connection.
    
    b. Click **Next**.
    
    c. In the Build pipeline page, use the **Choose input plug-in** menu to select your input plug-in. Then select a filter plug-in by using the **Choose a filter plug-in** menu.
    
    d. Click **Next**.
    
    e. Follow the instructions according to the input plug-in type you selected in <em>**step c**</em>.
    
 ## Filebeat   
1.  In the Additional info page, enter a **Data source tag**: This tag identifies the plug-in that is associated with this connector. Use this tag in the filebeat.yml    configuration of the data sources whose type matches this plug-in step 2 of the last section on this page: Configuring Filebeat to forward audit logs to Guardium.  The data source sends the tag with every event. For example, specify any-mongodb in this field, and configure Filebeat with the same tag for MongoDB activity logs coming from your MongoDB data source.
      
2.  Click **Configure**.
        
3.  In the Configuration notes page, click Download certificate to **download the certificate** to your local system. Copy the certificate to the data source (refer to step 4 in the last section on this page: Configuring Filebeat to forward audit logs to Guardium.) All data sources of any one specific type use the same certificate.
        
4.  Click **Done**.
       
5. To configure the data source to communicate with Guardium Insights, follow the instructions in the last section on this page: Configuring Filebeat to forward audit logs to Guardium.  Copy the hostname in the Configuration Notes to configure the host in the filebeat.yml file on your datasource.
        
 ## Cloudwatch\_logs
        
1. In the Additional info page, specify the details of the connection you want to create:

*   **AWS Role ARN (optional)** - this is used to generate temporary credentials, typically for cross-account access. See the [AssumeRole API documentation](https://docs.aws.amazon.com/STS/latest/APIReference/API_AssumeRole.html) for more information.
    
*   **AWS access key id** and **AWS secret access key** - your AWS user account access key and the secret access key (for more information, click [here](https://docs.aws.amazon.com/powershell/latest/userguide/pstools-appendix-sign-up.html)).
    
*   **AWS account region** - for example, "us-east-1".
    
*   **Event filter** (optional) - specify the filters to apply when fetching resources. For example, for filtering an S3 events based on bucket name: '{$ .eventSource = "s3.amazonaws.com" && $ .requestParameters.bucketName = "```give bucket name```"}'.
    
*   **Account id** (optional) - your AWS account ID (For more information, click [here](https://docs.aws.amazon.com/IAM/latest/UserGuide/console_account-alias.html#FindingYourAWSId)).
    
*   **Cloudwatch Log Group name** - specify the log group that is created for your data instance.
    
    For example "/aws/rds/instance/any\_instance/any\_log\_group" .
    
 ## Configuring Filebeat to forward audit logs to Guardium
    
    
  ## Procedure
    
1.  Open the file filebeat.yml, usually located in /etc/filebeat/filebeat.yml.
    
2.  In the tags section, enter the value of the Data source tag that you defined when you added a connection in the Filebeat section on this page. For example, `tags: ["<tag-name>"]`.
    
3.  In the Logstash Output section, enter the hostname URL from the Configuration Notes popup from when you configured the universal connector in the first procedure on this page. For example, `hosts: ["<hostname-URL>:443"]`

**NOTE**: In GI, whenever using plug-ins that are based on Filebeat as a data shipper, the configured port should be 443. Guardium Insights will map this to an internal port

4.  Copy the location of the downloaded certificate, in the ssl.certificate\_authorities row in step 3 of the Filebeat section on this page. For example, `ssl.certificate_authorities: ["/etc/pki/ca-trust/GuardiumInsightsCA.pem"]`
    
5.  Restart Filebeat to effect these changes
    
    Linux: Enter the command: `sudo service filebeat restart`
    
6.  Windows: Restart in the Services window
