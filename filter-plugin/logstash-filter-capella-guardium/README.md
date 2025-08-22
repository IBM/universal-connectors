# Capella-Guardium Logstash filter plug-in

### Meet Capella

* Tested versions: V1
* Environment: Couchbase Capella Cloud
* Supported inputs: Capella Input plugin
* Supported Guardium versions:
    * Guardium Data Protection: 12.0 and above

This is a [Logstash](https://github.com/elastic/logstash) filter plug-in for the universal connector that is featured in
IBM Security Guardium. It parses Couchbase Capella event logs into
a [Guardium record](https://github.com/IBM/universal-connectors/blob/main/common/src/main/java/com/ibm/guardium/universalconnector/commons/structures/Record.java)
instance (which is a standard structure made out of several parts). The information is then sent over to Guardium.
Guardium records include the accessor (the person who tried to access the data), the session, data, and exceptions. If
there are no errors, the data contains details about the query "construct". The construct details the main action (verb)
and collections (objects) involved. The Capella Logstash filter plug-in supports Guardium Data Protection.

### Note

This version is compliant with Guardium Data Protection v12.1 and above. For more information, refer
to [input plug-in repository](../../input-plugin/logstash-input-couchbase-capella).

## Configuring Capella database on Couchbase Capella

There are multiple ways to install a Couchbase server. For this example, we will assume that we already have a working
Couchbase setup.

### Note

To manage Couchbase Capella Operational audits,
see [Manage Audits](https://docs.couchbase.com/cloud/security/audit-management.html).

To work with audit logging for App Services, you must use the Capella Operational Management API.

* For an overview of the Management API,
  see [Manage Deployments with the Management API](https://docs.couchbase.com/cloud/management-api-guide/management-api-intro.html).

* To get started with the Management API,
  see [Get Started with the Management API](https://docs.couchbase.com/cloud/management-api-guide/management-api-start.html).

* To make an API call,
  see [Make an API Call with the Management API](https://docs.couchbase.com/cloud/management-api-guide/management-api-use.html).

* For a full reference guide,
  see [Management API Reference](https://docs.couchbase.com/cloud/management-api-reference/index.html).

### Enabling audit logs:

* How to configure and manage audit logging for App Services and App Endpoints,
  see [Manage Audit Logs](https://docs.couchbase.com/cloud/app-services/monitoring/manage-audit-logs.html).

1. Use PUT ```/appservices/{appServiceId}/auditLog```.

2. Pass the App Service ID as a path parameter.

3. Pass "auditEnabled": true as the request body.

### Viewing the Audit logs status

To view the current status of audit logging for a specified App Service,
see [View Log Status](https://docs.couchbase.com/cloud/app-services/monitoring/manage-audit-logs.html#view-log-status):

1. Use GET ```/appservices/{appServiceId}/auditLog```.

2. Pass the App Service ID as a path parameter.

### Export Audit Logs

Create an export job to gather and prepare the audit log files for export,
see [Export Audit Logs](https://docs.couchbase.com/cloud/app-services/monitoring/manage-audit-logs.html#export-app-services-audit-logs).

When the export job has finished, you can download the compressed file from S3, using the supplied download URL. Export
requests expire after 72 hours. The download URL is valid for one hour when the export request is created.

1. Create an App Services Audit Log Export Job

* Use POST ```/appservices/{appServiceId}/auditLogExports```.
* Pass the App Service ID as a path parameter.
* Pass the start time and end time for the audit log export job in the request body.
* If successful, the request returns an audit log Export ID.

2. Get an App Services Audit Log Export Job

* You need the Export ID that was returned when you created the audit log export job to get the status of an App
  Services audit log export job.
  Use GET ```/appservices/{appServiceId}/auditLogExports/{auditLogExportId}```.

Note: Pass the App Service ID and the Audit Log Export ID as path parameters.

3. List App Services Audit Log Export Jobs

* Use GET ```/appservices/{appServiceId}/auditLogExports```.

* Pass the App Service ID as a path parameter.

Note: If successful, the request returns an array of all the audit log export jobs for the specified App Service.

For each audit log export job, when the export is ready, the download_id field gives a URL that you can use to download
the exported audit log.

## Configuring the Capella filter in Guardium

The Guardium universal connector is the Guardium entry point for native audit/data_access logs. The Guardium universal
connector identifies and parses the received events, and converts them to a standard Guardium format. The output of the
Guardium universal connector is forwarded to the Guardium sniffer on the collector, for policy and auditing
enforcements. Configure Guardium to read the native audit/data_access logs by customizing the Capella template.

### Before you begin

* Configure the policies you require. See [policies](/docs/#policies) for more information.
* You must have permission for the S-Tap Management role. The admin user includes this role by default
* Verify that the Capella input plugin is available on the GDP system. If the plugin is missing, download and install
  the  [logstash-input-couchbase_capella_input](../../input-plugin/logstash-input-couchbase-capella/logstash-input-couchbase_capella_input.zip)
  plug-in.
* Download
  the [logstash-filter-capella_guardium_filter](logstash-filter-capella_guardium_filter.zip)
  plug-in.
* Capella-Guardium Logstash filter plug-in is automatically available with Guardium Data Protection versions 12.x, 11.4
  with appliance bundle 11.0p490 or later or Guardium Data Protection version 11.5 with appliance bundle 11.0p540 or
  later releases. After each patch, there is no need to download and deploy the packages.

### Procedure

1. On the collector, go to ```Setup``` > ```Tools and Views``` > ```Configure Universal Connector```.
2. Enable the universal connector if it is disabled.
3. Click ```Upload File``` and select the
   offline  [logstash-filter-capella_guardium_filter](logstash-filter-capella_guardium_filter.zip)
   plug-in. After it is uploaded, click ```OK```.
4. Click the Plus sign to open the Connector Configuration dialog box.
5. Type a name in the Connector name field.
6. Update the input section to add the details from
   the [capellaCouchbase.conf](CapellaCouchbaseOverCapellaPackage/capellaCouchbase.conf) file's input part,
   omitting the keyword "input{" at the beginning and its corresponding "}" at the end.
7. Update the filter section to add the details from
   the [capellaCouchbase.conf](CapellaCouchbaseOverCapellaPackage/capellaCouchbase.conf) file's filter part,
   omitting the keyword "filter{" at the beginning and its corresponding "}" at the end.
8. The 'type' fields should match in the input and filter configuration sections. This field should be unique for every
   individual connector added.
9. Click ```Save```. Guardium validates the new connector and displays it in the Configure Universal Connector page.
10. After the offline plug-in is installed and the configuration is uploaded and saved in the Guardium machine, restart
    the Universal Connector using the ```Disable/Enable``` button.

##  Limitations
* No more than three historical export requests are permitted over 24-hour period.
* The original Capella audit log contains no values for the following fields: Database Name, Service Name.

Notes:
* It may take approximately 30 minutes for data to appear in the Full SQL report.
