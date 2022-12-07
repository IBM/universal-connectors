# Configuring a univer***REMOVED***l connector in Guardium Insights


## Before you begin

Each data source type has a specific plug-in that filters and parses the events from the data source, and converts them to a standard Guardium format. The first time that you add a connector of a specific data source type, you need to upload its plug-in. On the [available\_plugins.md](https://github.com/IBM/univer***REMOVED***l-connectors/blob/main/docs/available_plugins.md) page, refer to the "Downloads" column for the list of plug-ins available for GI. Click the "GI" hyperlink for the plug-in you wish to download.

## About this task


The univer***REMOVED***l connector configuration has a few parts, all described in this task:

*   Uploading a plug-in that configures the filter or parser to convert the data source events to a standard Guardium format
*   Configuring the connection between Guardium Insights and the data source
*   Downloading the certificate (when using Filebeat input type)

## Procedure


1. Click **Connections** in the **Settings** menu.
    
2.  Click **Add connection.**  The Connect to new data source page opens, with a card for each available data source type.
    
3.  Select the data source type. This opens a panel that aids you in initiating the connection.
    
    a **Select data source environment**: Select the environment that hosts your data source.
    
    b. **Select connection method**: Select **Univer***REMOVED***l Connector.**
    
    c. The remainder of the panel provides **Additional information** about the connection type that you are creating.
    
    d. After reading the **Additional information**, click **Configure**.
    
4.  Enter the details for this connection.
    
    a. In the Name and description page, enter this information:
    
      i. **Name**: A unique name for the connection. This name distinguishes this connection from all other Guardium Insights connections.
    
      ii. **Description**: Enter a description for the connection.
    
    b. Click **Next**.
    
    c. In the Build pipeline page, use the **Choose input plug-in** menu to select your input plug-in. Then select a filter plug-in by using the **Choose a filter plug-in** menu.
    
    d. Click **Next**.
    
    e. Follow the instructions according to the input plug-in type you selected in <em>**step c**</em>.
    
 ## Filebeat input plug-in configuration
Prerequisite: 

On the data source server, create a **certificate authority** for Filebeat.
This CA will be used later to create a certificate for signing the events from Filebeat to the univer***REMOVED***l connector.

Run this command
```openssl req -x509 -sha256 -days 356 -nodes -newkey r***REMOVED***:2048 -subj "/CN=filebeat.lan/C=IL" -keyout filebeatCA.key -out filebeatCA.crt``` 

Then, copy `filebeatCA.crt` to your local system.

1. In the Additional info page, enter a **Data source tag**: This tag uniquely identifies the incoming Filebeat stream. This tag will be added later to filebeat configuration so filebeat will tag every event with this tag. For example, specify `any-mongodb` in this field.
      
2. Click on **upload certificates authorities** button and select the authority created in the prerequisites: `filebeatCA.crt`. You can specify multiple authorities from your local system if needed. The UC will process an event only if its certificate is signed by one the specified authorities. 

3. Click **Configure**.
        
4.  In the Configuration notes page, click **Download certificate** to download the UC certificate authority to your local system. Copy the certificate to the data source (will be added to filebeat configuration later). All data sources of any one specific type use the ***REMOVED***me certificate.
        
5.  Click **Done**.
       
6. To configure the data source to communicate with Guardium Insights, follow the instructions in the last section on this page: Configuring Filebeat to forward audit logs to Guardium.  Copy the hostname in the Configuration Notes to configure the host in the filebeat.yml file on your datasource.
        
 ## CloudWatch input plug-in configuration
        
1. In the Additional info page, specify the details of the connection you want to create:

*   **AWS Role ARN (optional)** - this is used to generate temporary credentials, typically for cross-account access. See the [AssumeRole API documentation](https://docs.aws.amazon.com/STS/latest/APIReference/API_AssumeRole.html) for more information.
    
*   **AWS access key id** and **AWS secret access key** - your AWS user account access key and the secret access key (for more information, click [here](https://docs.aws.amazon.com/powershell/latest/userguide/pstools-appendix-sign-up.html)).
    
*   **AWS account region** - for example, "us-east-1".
    
*   **Event filter** (optional) - specify the filters to apply when fetching resources. For example, for filtering an S3 events based on bucket name: '{$ .eventSource = "s3.amazonaws.com" && $ .requestParameters.bucketName = "```give bucket name```"}'.
    
*   **Account id** (Mandatory) - your AWS account ID (For more information, click [here](https://docs.aws.amazon.com/IAM/latest/UserGuide/console_account-alias.html#FindingYourAWSId)).
    
*   **Cloudwatch Log Group name** - specify the log group that is created for your data instance.
    
    For example "/aws/rds/instance/any\_instance/any\_log\_group" .
    
 ## Configuring Filebeat to forward audit logs to Guardium
    
    
  ## Procedure
prerequisites:
* On the data source server there is a **certificate authority** (filebeatCA key and certificate).
* A connection was added by the steps in the section: [Filebeat input plug-in configuration](https://github.com/IBM/univer***REMOVED***l-connectors/blob/main/docs/UC_Configuration_GI.md#filebeat-input-plug-in-configuration). 
In the configuration notes there are `data source tag`, `host` and `UC certificate authority` that are used in these steps. 


1.  Open the Filebeat Configuration. (filebeat.yml). usually located in `/etc/filebeat/filebeat.yml`.
    
2.  Locate the `tags` section, enter the Data source tag. For example, `tags: ["any-mongodb"]`.
    
3.  Locate `output.logstash` section and add an entry for Guardium Insights:
    ```
    # The Logstash hosts
    hosts: ["<hostname-URL>:443"]
    ```
**NOTE**: In GI, whenever using plug-ins that are based on Filebeat as a data shipper, the configured port should be 443. Guardium Insights will map this to an internal port

4. Configure mTLS - Logstash to Filebeat:
   1. Download the SSL certificate (`UC certificate authority`) from Guardium Insights and upload it to the machine where Filebeat is.
   2. Copy the location of the downloaded certificate and enter it as the certificate authority.
    ```
    # List of root certificates for HTTPS server verifications
    ssl.certificate_authorities: ["/etc/pki/ca-trust/GuardiumInsightsCA.pem"]
    ```
5. Configure mTLS - Filebeat to Logstash:
   1. Create a filebeat certificate:
   
       a. Create a private key: 
       ```
       openssl genr***REMOVED*** -out filebeat.key 2048 
       ```
       b. convert private key to pkc8
       ```
       openssl pkcs8 -in filebeat.key -topk8 -out filebeat-pkcs8.key -nocrypt
       ```
       c. Creating certificate request file
      First create filebeat-csr.conf file with
       ```
       [ req ]
       default_bits = 2048
       prompt = no
       default_md = sha256
       req_extensions = req_ext
       distinguished_name = dn
    
       [ dn ]
       C = IL
       CN = filebeat.lan
    
       [ req_ext ]
       subjectAltName = @alt_names
    
       [ alt_names ]
       DNS.1 = server.dns.com
       ```
       d. Now generate the certificate request file:
       ```
       openssl req -new -key filebeat.key -out filebeat.csr -config filebeat-csr.conf
       ```
       e. Generate certificate from csr file and CA 
          Create certificate configuration file (filebeat-cert.conf) like
       ```
       authorityKeyIdentifier=keyid,issuer
       basicConstraints=CA:FALSE
       keyU***REMOVED***ge = digitalSignature, nonRepudiation, keyEncipherment, dataEncipherment
       subjectAltName = @alt_names
    
       [alt_names]
       DNS.1 = server.dns.com
       ```
       f. Now generate the certificate
       ```
       openssl x509 -req -in filebeat.csr -CA filebeatCA.crt -CAkey filebeatCA.key -CAcreateserial -out filebeat.crt -days 365 -sha256 -extfile filebeat-cert.conf
       ```
   2. Locate `output.logstash` section and add an entry of the certificate:
      ```
       ssl.certificate: "<PATH TO>/filebeat.crt"
       ssl.key: "<PATH TO>/filebeat-pkcs8.key"
       ```

Summary: 

   ```
   tags: ["any-mongodb"]
   
   output.logstash:
     # The Logstash hosts
     hosts: ["<hostname-URL>:443"]
     # List of root certificates for HTTPS server verifications
     ssl.certificate_authorities: ["/etc/pki/ca-trust/GuardiumInsightsCA.pem"]
     # Certificate for SSL client authentication
     ssl.certificate: "/etc/pki/certs/filebeat.crt"
     # Certificate key for SSL client authentication
     ssl.key: "/etc/pki/certs/filebeat-pkcs8.key"
   ```
    
6.  Restart Filebeat to effect these changes
    
    Linux: Run the command: `sudo service filebeat restart`
    
7. Windows: Restart in the Services window
