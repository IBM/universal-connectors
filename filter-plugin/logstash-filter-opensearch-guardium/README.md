# Amazon OpenSearch - Guardium Logstash filter plug-in

### Meet OpenSearch

* Tested versions: v1
* Environment: AWS
* Supported inputs: CloudWatch (pull)
* Supported Guardium versions:
    * Guardium Data Protection 12.2 and later

This is a [Logstash](https://github.com/elastic/logstash) filter plug-in for the universal connector that is featured in
IBM Security Guardium. It parses events and messages from the Amazon OpenSearch audit log into
a Guardium Record.

The plug-in is free and open-source (Apache 2.0). It can be used as a starting point to develop additional filter
plug-ins for Guardium universal connector.

## Configuration

### OpenSearch Setup

1. [Prerequisites](https://docs.aws.amazon.com/opensearch-service/latest/developerguide/setting-up.html)
2. Go to https://console.aws.amazon.com/.
3. Search and navigate to ```Amazon OpenSearch Service```. 
4. To create an OpenSearch domain, refer to the [Getting started with Amazon OpenSearch Service guide](https://docs.aws.amazon.com/opensearch-service/latest/developerguide/gsg.html).

### Enabling Audit Logs

1. Enable audit logs for **CloudWatch Logs** and **OpenSearch Dashboard**, refer to the [Enabling Audit logs](https://docs.aws.amazon.com/opensearch-service/latest/developerguide/audit-logs.html#audit-log-enabling).

### Viewing Audit Logs on CloudWatch

By default, each database instance has an associated log group with a name in this format: `/aws/OpenSearchService/<instance_name>/audit` and `/aws/OpenSearchService/<instance_name>/profiler`.

#### Procedure

1. Open the CloudWatch console https://console.aws.amazon.com/cloudwatch/.
2. In the navigation pane, choose ```Log groups```.
3. Choose the ```log group``` that you specified while enabling audit logs. Within the log group, OpenSearch Service creates a log stream for each node in your domain. 
4. In the ```Log streams```, select ```Search all```.
5. For the read and write events, see the corresponding logs. This process may take several seconds.

#### Supported Audit Log Types

Cluster communication occurs over two separate layers: **REST layer** and **Transport layer**. The following is the list of Audit log Categories, with their availability determined by the communication layers.

* FAILED_LOGIN 
* MISSING_PRIVILEGES 
* BAD_HEADERS
* SSL_EXCEPTION
* GRANTED_PRIVILEGES
* OPENSEARCH_SECURITY_INDEX_ATTEMPT
* AUTHENTICATED 
* INDEX_EVENT
* COMPLIANCE_DOC_READ 
* COMPLIANCE_DOC_WRITE 
* COMPLIANCE_INTERNAL_CONFIG_READ
* COMPLIANCE_INTERNAL_CONFIG_WRITE
 


For more information about the audit logging category and layers, refer to the [Audit log layers and categories](https://docs.aws.amazon.com/opensearch-service/latest/developerguide/audit-logs.html#audit-log-layers).

For more information about the audit logging fields, refer to the [Audit log field reference](https://docs.opensearch.org/docs/latest/security/audit-logs/field-reference/).

**Note:** OpenSearch generates a large volume of background audit logs by default. We recommend configuring the audit settings appropriately to limit unnecessary entries in the audit logs.

### Limitations
- Audit logging in OpenSearch can be accessed in two different ways – via the OpenSearch Dashboards or through CloudWatch Logs. However, this filter plugin only parses and processes audit logs that are streamed to CloudWatch. Audit logs stored directly in OpenSearch indices or viewed in the Dashboards are not supported for parsing.
- FAILED_LOGIN REST messages will appear in 'Full SQL' and 'Failed Logins' report.
- Certain reserved keywords (template, mappings, get, aliases, user) are automatically prefixed with an underscore (_) during sanitization to prevent OpenSearch URI parsing errors or endpoint conflicts.
- ClientHostName is not available in the audit logs for OpenSearch.

## Guardium Data Protection

The Guardium universal connector is the Guardium entry point for native audit/data_access logs. The Guardium universal connector identifies and parses the received events, and converts them to a standard Guardium format. The output of the Guardium universal connector is forwarded to the Guardium sniffer on the collector, for policy and auditing enforcements.

### Before you begin
* Configure the policies you require. See [policies](/docs/#policies) for more information.
* You must have permission for the S-Tap Management role. The admin user includes this role by default
* Download the [logstash-filter-aws_opensearch_guardium_filter](logstash-filter-opensearch_guardium_filter.zip) plug-in.

### Procedure
1. On the collector, go to ```Setup``` > ```Tools and Views``` > ```Configure Universal Connector```.
2. Enable the universal connector if it is disabled.
3. Click ```Upload File``` and select the offline  [logstash-filter-aws_opensearch_guardium_filter](logstash-filter-opensearch_guardium_filter.zip) plug-in. After it is uploaded, click ```OK```.
4. Click ```Upload File``` and select the key.json file. After it is uploaded, click ```OK```.
5. Click the Plus sign to open the Connector Configuration dialog box.
6. Type a name in the Connector name field.
7. Update the input section to add the details from the [opensearch.conf](OpenSearchOverCloudwatchPackage/opensearch.conf) file's input part, omitting the keyword "input{" at the beginning and its corresponding "}" at the end.
8. Update the filter section to add the details from the [opensearch.conf](OpenSearchOverCloudwatchPackage/opensearch.conf) file's filter part, omitting the keyword "filter{" at the beginning and its corresponding "}" at the end.
9. The 'type' fields should match in the input and filter configuration sections. This field should be unique for every individual connector added.
10. Click ```Save```. Guardium validates the new connector and displays it in the Configure Universal Connector page.
11. After the offline plug-in is installed and the configuration is uploaded and saved in the Guardium machine, restart the Universal Connector using the ```Disable/Enable``` button.


