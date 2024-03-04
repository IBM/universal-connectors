# Configuring a universal connector in Guardium Insights


## Before you begin

Each data source type has a specific plug-in that filters and parses the events from the data source, and converts them to a standard Guardium format. You can create a connection for any plug-in listed in Guardium Insights. Go to **Settings** > **Connections** >  **Manage** > **Universal Connector plug-ins**  to see the full list and create a connection.

## About this task


The universal connector configuration has a few parts, all described in this task:

*   Configuring the connection between Guardium Insights and the data source
*   Downloading the certificate (when using Filebeat input type or TCP input type for Syslog connection)

## Procedure


1. Click **Connections** in the **Settings** menu.

2.  Click **Add connection.**  The Connect to new data source page opens, with a card for each available data source type.

3.  Select the data source type. This opens a panel that aids you in initiating the connection.

    a **Select data source environment**: Select the environment that hosts your data source.

    b. **Select connection method**: Select **Universal Connector.**

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

## JDBC input plug-in configuration

In the **Additional info** page, specify the following details for the connection you want to create:

*   **Connection string** - the JDBC connection string, for example:
   ```
   jdbc:sqlserver://<server_name>:<port_number>;databaseName=<database_name>
   ```
**NOTE** Do not enter the database user name and password in the connection string, as they are supplied in the **JDBC user** and **Password** fields mentioned in previous steps.

*   **Statement** - the statement setting determines which audit tables the SELECT query calls for the audit logs. In the Guardium UI, the **Statement*** is divided into three parts to enhance clarity and ease of use: `SELECT` for choosing columns, `FROM` for specifying tables, and `WHERE` for adding filter conditions.

*   **JDBC user** - input the user name that you want to connect to the database with access to the audit tables to be queried.

*   **Password** -  password for the above JDBC user.

#### Additional parameters that are specific to certain data sources:
* **Account ID** - your AWS account ID. (For more information, click [here](https://docs.aws.amazon.com/IAM/latest/UserGuide/console_account-alias.html#FindingYourAWSId)).
* **Enrollment ID** - used for an AzureSQL connection. (For more information, click [here](https://github.com/IBM/universal-connectors/tree/main/filter-plugin/logstash-filter-azure-sql-guardium#5-finding-enrollment-id)).
* **Server name** - enter the host name of the database server.

## Filebeat input plug-in configuration
### Prerequisite:

On the data source server, create a **certificate authority** and **certificate** for Filebeat.
These certificates will be used later to establish secured connections between the data source server and universal connector.
1. Download the [create_certificates.sh](create_certificates.sh) script to the datasource server.
2. Change the file permissions so the script can run:
   ```
   chmod +x create_certificates.sh
   ```
3. Run the script with 2 arguments:
   The first argument should be the path where the certificates will be stored and the host name.
   ```
   ./create_certificates.sh <PATH TO STORE> <DATASOURCE SERVER DNS>
   ```
   for example:
   ```
   ./create_certificates.sh /path/to/store datasource.server.dns.com
   ```

4. Copy `ucCA.crt` to your local system.

### Configuring a Filebeat connection:

1. Exectue steps 1-4 from the [main procedure](../SaaS_1.0/UC_Configuration_GI.md#procedure) . For the data source, select **MongoDB**. For the data source environment type, select **On-premises**. For the input plug-in, choose **Filebeat**.

3. In the Additional info page, enter a **Data source tag**: This tag uniquely identifies the incoming Filebeat stream. This tag will be added later to the Filebeat configuration so Filebeat will tag every event with this tag. For example, specify `any-mongodb` in this field.

5. Click the **upload certificates authorities** button and select the authority created in the prerequisites: `filebeatCA.crt`. You can specify multiple authorities from your local system if needed. The universal connector will process an event only if its certificate is signed by one the specified authorities.

3. Click **Configure**.

4.  In the Configuration notes page, click **Download certificate** to download the universal connector certificate authority to your local system. Copy the certificate to the data source (it will be added to the Filebeat configuration later). All data sources of any one specific type use the same certificate.

5.  Click **Done**. It may take up to 15 minutes to create the connection.

6. To configure the data source to communicate with Guardium Insights, follow the instructions in the last section on this page: [Configuring Filebeat to forward audit logs to Guardium.](../SaaS_1.0/UC_Configuration_GI.md#configuring-filebeat-to-forward-audit-logs-to-guardium)  Copy the hostname in the Configuration Notes to configure the host in the filebeat.yml file on your datasource.
7. Persistent queue is disabled by default in the universal connector and must be enabled manually. Persistent queue can only be enabled for Filebeat and it can cause the universal connector to work more slowly.To enable it, go to **Settings** > **Global settings** > **Connection settings**. Click **Universal connector: enable persistent queue**.

## TCP Input Plug-in Configuration (for Connection with Syslog)

### Prerequisites:

To enable a secure connection with Syslog on the data source server, you need to create a **certificate authority** and **certificate**. Follow the steps below:

1. **Download the Script:**
    - Download the [create_certificates.sh](create_certificates.sh) script to the data source server.

2. **Set Script Permissions:**
    - Change the file permissions to make the script executable:
      ```bash
      chmod +x create_certificates.sh
      ```

3. **Run the Script:**
    - Execute the script with the following 2 arguments:
        - The first argument specifies the path where the certificates will be stored.
        - The second argument is the hostname of the data source server.

      For example:
      ```bash
      ./create_certificates.sh /path/to/store datasource.server.dns.com
      ```

4. **Copy `ucCA.crt` to your local system.**

### Configuring a Syslog Connection:

1. **Initiate Configuration:**
    - For this input type, simply click on configure without entering additional details.

2. **Retrieve Universal Connector Certificate Authority:**
    - Access the Configuration notes page.
    - Download the certificate authority for the universal connector by clicking **Download certificate**. Save it to your local system.
    - Copy this certificate to the respective data source directory (it will be incorporated into the Syslog configuration later).
    - Note: All data sources of a specific type share the same certificate.

3. **Complete Configuration:**
    - Click on **Done**.
    - Note: Allow up to 15 minutes for the connection setup.

4. **Finalize Configuration:**
    - Click on **Done** again.
    - Note: Allow up to 15 minutes for the connection setup.

5. **Configure Datasource Source for Guardium Insights:**
    - Follow the instructions in the readme file of the filter plug-in to configure the datasource source for effective communication with Guardium Insights. Pay attention to the configuration steps on the rsyslog side. Refer to the Syslog configuration section for specific details.
    - Copy the hostname in the Configuration Notes to configure the host in the rsyslog.conf file on your datasource.
    - The configured port should be 443. Guardium Insights will map this to an internal port.

6. **Configure mTLS:**
    - **Universal Connector to the Data Source Server:**
        1. Download the SSL certificate (`UC certificate authority`) from Guardium Insights and upload it to the machine where Syslog is installed.
        2. Copy the location of the downloaded certificate and use it as the certificate authority in the `rsyslog.conf` file:
        ```conf
        # Path to the Certificate Authority (CA)
        $DefaultNetstreamDriverCAFile <PATH TO>/GuardiumInsightsCA.pem
        ```

    - **Data Source Server to the Universal Connector:**
        - Add entries for the SSL certificate in the `rsyslog.conf` file:
        ```conf
        $DefaultNetstreamDriverCertFile <PATH TO>/ucCA.crt
        $DefaultNetstreamDriverKeyFile <PATH TO>/ucCA-pkcs8.key
        ```

      Example:
         ```conf
        $DefaultNetstreamDriverCAFile /root/uc/cert/GuardiumInsightsCA.pem
        #certs for mTls connection
        $DefaultNetstreamDriverCertFile <PATH TO>/ucCA.crt
        $DefaultNetstreamDriverKeyFile <PATH TO>/ucCA-pkcs8.key

        module(load="imfile")
        input(type="imfile"
        ## To configure this section, follow the filter plug-in's readme 
        Tag="Syslog"
        ruleset="imfile_to_GI")
       
        ruleset(name="imfile_to_GI") {
        action(type="omfwd"
        protocol="tcp"
        StreamDriver="gtls"
        StreamDriverMode="1"
        StreamDriverAuthMode="x509/certvalid"
        target="<hostname>"
        port="443")
        ```
7. **Restart rsyslog to Apply Changes:**
    - **Linux:**
      Run the command:
      ```bash
      sudo service rsyslog restart
      ```

    - **Windows:**
      Restart the rsyslog service through the Services window.


## CloudWatch input plug-in configuration

1. In the Additional info page, specify the details of the connection you want to create:


*   **AWS access key ID** and **AWS secret access key** - your AWS user account access key and the secret access key (for more information, click [here](https://docs.aws.amazon.com/powershell/latest/userguide/pstools-appendix-sign-up.html)). These parameters are mandatory if you are using the CloudWatch or SQS input plug-ins in SaaS 1.0. Note that unlike other Guardium deployments, in SaaS configuring only role_arn without access_key_id and secret_access_key is not a valid option.


*   **AWS account region** - for example, "us-east-1".

*   **Event filter** (optional) - specify the filters to apply when fetching resources. For example, for filtering an S3 events based on bucket name: '{$ .eventSource = "s3.amazonaws.com" && $ .requestParameters.bucketName = "```give bucket name```"}'.

*   **Account ID** (Mandatory) - your AWS account ID (For more information, click [here](https://docs.aws.amazon.com/IAM/latest/UserGuide/console_account-alias.html#FindingYourAWSId)).

*   **CloudWatch Log Group name** - specify the log group that is created for your data instance.

    For example "/aws/rds/instance/any\_instance/any\_log\_group".

    ***NOTE: Due to the possibility of CloudWatch reporting events multiple times for plug-ins configured for Guardium Insights, it is recommended to use SQS instead***

## Azure Event Hubs input plug-in configuration

In the **Additional info** page, specify the details of the connection you want to create:

* **Event hub connections** - specify the list of connection strings that identifies the Event Hubs to be read. Connection strings include the `EntityPath` for the Event Hub.

* **Enrollment ID** - Azure enrollment ID: Unique subscription identifier for billing and resource management.
## Configuring Filebeat to forward audit logs to Guardium


## Procedure
prerequisites:
* On the data source server there is a **certificate authority** (filebeatCA key and certificate).
* A connection was added by the steps in the section: [Filebeat input plug-in configuration](UC_Configuration_GI.md#filebeat-input-plug-in-configuration).
  The `data source tag`, `host` and `UC certificate authority` from the configuration notes are used in these steps.


1.  Open the Filebeat configuration (filebeat.yml) usually located in `/etc/filebeat/filebeat.yml`.

2.  Locate the `tags` section and enter the data source tag. For example, `tags: ["any-mongodb"]`.

3.  Locate the `output.logstash` section and add an entry for Guardium Insights:
    ```
    # The Logstash hosts
    hosts: ["<hostname-URL>:443"]
    ```
**NOTE**: In Guardium Insights, whenever using plug-ins that are based on Filebeat/Syslog as a data shipper, the configured port should be 443. Guardium Insights will map this to an internal port

4. Configure mTLS - universal connector to data source server:
    1. Download the SSL certificate (`UC certificate authority`) from Guardium Insights and upload it to the machine where Filebeat is.
    2. Copy the location of the downloaded certificate and enter it as the certificate authority.
    ```
    # List of root certificates for HTTPS server verifications
    ssl.certificate_authorities: ["<PATH TO>/GuardiumInsightsCA.pem"]
    ```
5. Configure mTLS - data source server to universal connector:

   Locate the `output.logstash` section and add an entry for the certificate:
      ```
       ssl.certificate: "<PATH TO>/ucCA.crt"
       ssl.key: "<PATH TO>/ucCA-pkcs8.key"
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
     ssl.certificate: "/etc/pki/certs/ucCA.crt"
     # Certificate key for SSL client authentication
     ssl.key: "/etc/pki/certs/ucCA-pkcs8.key"
   ```

6.  Restart Filebeat to effect these changes

    Linux: Run the command: `sudo service filebeat restart`

7. Windows: Restart in the Services window