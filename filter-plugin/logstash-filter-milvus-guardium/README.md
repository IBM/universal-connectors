# Zilliz Milvus - Guardium Logstash filter plug-in

### Meet Milvus

* Tested versions: 2.4.4 or later
* Environment: Milvus Standalone (Docker Linux), Milvus Distributed (Milvus Operator)
* Supported inputs: Filebeat (push)
* Supported Guardium versions: Guardium Data Protection 12.0 and later

This is a [Logstash](https://github.com/elastic/logstash) filter plug-in for the universal connector that is featured in IBM Security Guardium. It parses events and messages from the Zilliz Milvus access log into a Guardium Record.

The plug-in is free and open-source (Apache 2.0). It can be used as a starting point to develop additional filter plug-ins for Guardium universal connector.

### Limitations

1. Milvus access logs do not include the server IP address.
2. Milvus access logs do not specify the source program but do provide the SDK version.

## Configuring access logs for Milvus

### Before you begin

Install Milvus. For more information, see [Milvus](https://milvus.io/docs).

### Procedure

1. Configure access logs for **Milvus**.

      In the ``milvus.yaml`` file, find the ``proxy | accessLog`` section and configure the following parameters:

      - Set ``accessLog | enable`` to ``true``.
      - In the ``localPath`` parameter, enter the directory where the access log file is located.
      - In the ``filename`` parameter, enter the name of your access log file.
        <br></br> 
      ```
      proxy:
            accessLog:
              enable: true
              localPath: /tmp/milvus_access
              filename: access.log
      ```  
      For more information, see [Configure Access logs](https://milvus.io/docs/configure_access_logs.md).

2. The Milvus filter requires IBM Log Event Extended Format (LEEF) for the access log entry. For more information, see [LEEF overview](https://www.ibm.com/docs/en/dsm?topic=leef-overview).
3. Update the ``formatters`` section in your Milvus configuration file to use LEEF.
   
   ```
    formatters:
      base:
        format:"LEEF:1.0|Zilliz|Milvus|1.0|$method_name-$method_status|devTime=$time_now\tdevTimeFormat=yyyy/MM/dd HH:mm:ss.SSS xxx\tuserName=$user_name\tuserAddress=$user_addr\tdatabaseName=$database_name\tcollectionName=$collection_name\tpartitionName=$partition_name\tqueryExpression=$method_expr\terrorCode=$error_code\terrorMessage=$error_msg\ttraceId=$trace_Id\tresponseSize=$response_size\ttimeCost=$time_cost\ttimeStart=$time_start\ttimeEnd=$time_end\tsdkVersion=$sdk_version\tmethodName=$method_name\tmethodStatus=$method_status"
   ```

## Installing and configuring Filebeat

Guardium uses the Filebeat input plugin to ingest access logs from Milvus. For more information, see [Filebeat](https://www.elastic.co/docs/reference/beats/filebeat).


### Procedure
1. Install Filebeat on your system. For more information, see [Install Filebeat](https://www.elastic.co/guide/en/beats/filebeat/current/filebeat-installation-configuration.html#installation).
2. Configure Filebeat to use Logstash for additional data processing by updating the ``filebeat.yml`` configuration file located in the Filebeat installation directory. For more information about locating the installation directory, see [Directory layout](https://www.elastic.co/guide/en/beats/filebeat/current/directory-layout.html).
3. In the `filebeat.yml` file, navigate to the `filebeat.inputs` section and add the following parameters. Make sure to add the ``milvus`` tag to identify the Milvus events from other data.
   ```
   filebeat.inputs:
        - type: filestream   
        - id: <ID>
     enabled: true
     paths:
       - <directory path to access log file>
     fields:
       service: milvus
     fields_under_root: true
     tags: ["milvus"]
   ```
  
4. Configure the output section in the ``filebeat.yml`` file by completing the following steps.

   a. Disable Elasticsearch output by commenting it out.
   
   b. Enable Logstash output by uncommenting the Logstash section. For more information, see [Configure the Logstash output](https://www.elastic.co/guide/en/beats/filebeat/current/logstash-output.html#logstash-output).
   
   For example:
   
   ```
   output.logstash:
     hosts: ["<host>:<port>"]
   ```

   Note: The ``hosts`` specifies the Logstash server and the ``port`` specifies where Logstash is configured to listen for incoming Beats connections. You can set any port number except ``5044``, ``5141``, and ``5000`` as these ports are currently reserved in Guardium v11.3 and v11.4.

5. Navigate to the ``processors`` section and add the following attribute to get the server's time zone. For more information, see [Add the local time zone](https://www.elastic.co/guide/en/beats/filebeat/current/add-locale.html).

   In the following example, the processor is enabled with the default settings.
   ```
   processors:
     - add_locale: ~
   ```
 
6. Start FileBeat. For more information, see [Start filebeat](https://www.elastic.co/guide/en/beats/filebeat/current/filebeat-installation-configuration.html#start).

## Configuring Milvus filters in Guardium

The Guardium universal connector is the Guardium entry point for native access logs. The Guardium universal connector identifies and parses the received events, and converts them to a standard Guardium format. The output of the Guardium universal connector is forwarded to the Guardium sniffer on the collector for policy and auditing enforcements.

### Before you begin
* Configure the policies you need. For more information, see [Policies](/docs/#policies).
* You must have permissions for the S-Tap Management role. By default, the admin user is assigned the S-Tap Management role.
* Download the [logstash-filter-milvus-guardium](https://github.com/IBM/universal-connectors/releases/download/v1.7.0/logstash-filter-milvus_guardium_filter.zip) plug-in.

### Procedure
1. On the collector, go to **Setup** > **Tools and Views** > **Configure Universal Connector**.
2. Enable the universal connector if it is disabled.
3. Click **Upload File** and select the offline [logstash-filter-milvus-guardium](https://github.com/IBM/universal-connectors/releases/download/v1.7.0/logstash-filter-milvus_guardium_filter.zip) plug-in. After it is uploaded, click **OK**.
4. Click **Upload File** and select the ``key.json`` file. After it is uploaded, click **OK**.
5. Click the **Plus** sign to open the Connector Configuration dialog box.
6. In the **Connector name** field, enter a name.
7. Update the input section to add the details from the [``milvusOverFilebeat.conf``](milvusOverFilebeat.conf) file's ``input`` section, omitting the keyword ``input{`` at the beginning and its corresponding ``}`` at the end.
8. Update the filter section to add the details from the [``milvusOverFilebeat.conf``](milvusOverFilebeat.conf) file's ``filter`` section, omitting the keyword ``filter{`` at the beginning and its corresponding ``}`` at the end.
9. Make sure that the ``type`` fields in the ``input`` and ``filter`` configuration sections align. This field must be unique for each connector added to the system.
10. Click **Save**. Guardium validates the new connector and displays it in the Configure Universal Connector page.
11. After the offline plug-in is installed and the configuration is uploaded and saved in the Guardium machine, restart the universal connector by using the **Disable/Enable** button.
