# Universal Connector Documentation
This is the documentation for the Universal Connector. Get started right away by viewing available plug-ins and documentation per product version, or read more about the Universal Connector and how it works.

<details open="open">

<summary><b>Getting started</b></summary>

## Available plug-ins
See available plug-ins [here](/docs/available_plugins.md). 

## Guardium Data Protection
See our Guardium Data Protection documentation [here](/docs/Guardium%20Data%20Protection/).

## Guardium Insights
See Guardium Insights documentation for [3.2.x](https://github.com/IBM/universal-connectors/tree/main/docs/Guardium%20Insights/3.2.x) 

See Guardium Insights documentation for [3.x](https://www.ibm.com/docs/en/guardium-insights/3.x?topic=connecting-data-sources)

## Guardium Insights SaaS

See our Guardium Insights documentation for [SaaS](https://www.ibm.com/docs/en/guardium-insights/saas?topic=connecting-data-sources)


</details>

<details open="open">
  
  <summary><b>Learn more</b></summary>
  
# Universal Connector Documentation

<details open="open">
<summary>Overview</summary>

# Overview

The Guardium Universal Connector enables Guardium Data Protection and Guardium Insights to get data from potentially any data source's native activity logs without using S-TAPs. It includes support for various plug-in packages, requiring minimal configuration. You can easily develop plug-ins for other data sources and install them in Guardium.

The captured events embed messages of any type that is supported by the configured data source. That includes: information and administrative system logs (e.g., login logs, various data lake platform native plug-in related data), DDLs and DMLs, errors of varying subtypes, etc. The incoming events received by the Universal Connector can be configured to arrive either encrypted or as plain text.

Figure 1. Guardium Universal Connector architecture

![Universal Connector](/docs/images/guc.jpg)

<sub>Data flow from input plugin to Guardium sniffer in Guardium Data Protection</sub>

The Guardium Universal Connector supports many platforms and connectivity options. It supports pull and push modes, multi-protocols, on-premises, and cloud platforms. For the data sources with pre-defined plug-ins, you configure Guardium to accept audit logs from the data source.

For data sources that do not have pre-defined plug-ins, you can customize the filtering and parsing components of audit trails and log formats. The open architecture enables reuse of prebuilt filters and parsers, and creation of a shared library for the Guardium community.

The Guardium Universal Connector identifies and parses the received events, and converts them to a standard Guardium format. The output of the Guardium Universal Connector is forwarded to the Guardium sniffer on the collector, for policy and auditing enforcements. The Guardium policy, as usual, determines whether the activities are legitimate or not, when to alert, and the auditing level per activity.

The Guardium Universal Connector is scalable. It provides load-balancing and fail-over mechanisms among a deployment of Universal Connector instances, that either conform to Guardium Data Protection as a set of Guardium Collectors, or to Guardium Insights as a set of Universal Connector pods. The load-balancing mechanism distributes the events sent from the data source among a collection of Universal Connector instances installed on the Guardium endpoints (i.e., Guardium Data Protection collectors or Guardium Insights pods). For more information, see [Enabling Load-Balancing and Fail-Over](/docs/readme.md/#enabling-load-balancing-and-fail-over).

Connections to databases that are configured with the Guardium Universal Connector are handled the same as all other data sources in Guardium. You can apply policies, view reports, monitor connections, for example.

</details>

<details open="open">
<summary> How it works</summary>

# How it works
Under the hood, the Universal Connector is a Logstash pipeline comprised of a series of three plug-ins:

1. Input plug-in. This plug-in ingests events. Depending on the type of plug-in, there are settings to either pull events from APIs or receive a push of events.

2. Filter plug-in. This plug-in filters the events captured by the input plug-in. The filter plug-in parses, filters, and modifies event logs into a Guardium-digestible format.

3. Output plug-in. This plug-in receives the formatted event logs from the filter plug-in and transmits them to IBM Guardium (either Guardium Data Protection or Guardium Insights).

***Note: The output plug-in is presented here as an internal component of the Universal Connector pipeline and is not to be accessed or modified by the user.***

![Universal Connector - Logstash pipeline](/docs/images/uc_overview.png)

Universal Connector plug-ins are packaged and deployed in a Docker container environment.

## Enabling audit log collection on Guardium
There are a couple of approaches for enabling audit log forwarding into Guardium for various data sources, comprised of either a cloud or on-premise data lake platform, of a database type that is supported by the Guardium sniffer[^1]:
1. Utilize the out-of-the-box, pre-installed plug-in packages[^2] that require minimal configuration on the client's end by either plugging suitable values into their respective template configuration files in the input and filter sections, or by adding a Ruby code subsection to the filter section in case a more complex parsing method is necessary as a pre-processing stage to be executed prior to the execution of the respective filter plug-in. See each plug-in's user manual via [Available Plug-ins](/docs/available_plugins.md).

2. For data sources that are not yet supported, you can either upload an IBM-approved filter plug-in or [develop your own](/docs/readme.md/#developing-plug-ins) and add it to our plug-in repository. You can also clone and modify the existing plug-ins as a template for your convenience (either in Ruby or Java)[^3]. You can optionally either let the parsing operations be executed by your filter plug-in, or assign this task to the Guardium Sniffer by transferring the event to the output plug-in in a designated structure as part of the filter plug-in development, as instructed in the links in the [Developers Guide](/docs/readme.md/#developing-plug-ins).



## Keep In Mind:

  1. The pre-defined and pre-installed plug-ins do not require any manual uploads or other such prerequisites on the user's end, as opposed to user-made plug-ins or other available Logstash plug-ins. You can simply use a ready-made template for plugging in values to the input and filter sections of their respective configuration files, expand these sections by using online pre-installed Logstash plug-ins, or write your own Ruby code parser using the [Ruby filter plug-in](/docs/readme.md/#use-a-logstash-ruby-filter-plug-in) as a pre-processing stage prior to executing the filter plug-ins.
  2. It is recommended to use one of the input plug-ins already in the repository and modify its config file input section. But if the input plug-ins already in the repository are insufficient for your needs, you can add a new one.
  3. You can choose to configure either pull or push methods via the messaging middleware service installed on the data lake platform that is used by the input plug-in. Messages can be received with pull or push delivery. In pull mode, the Universal Connector instance initiates requests to the remote service to retrieve messages. In push mode, the remote service initiates requests to the Universal Connector instance to deliver messages.
  4. The specific audit log types transmitted into the Universal Connector from the data source are configurable via the SQL instance settings installed on the data lake platform. This can vary depending on the installed data lake platform native plug-ins and the utilized messaging middleware service[^4].
  5. For some data lake platforms, you can define inclusion and exclusion filters for the events routed to the Universal Connector to be ingested by the input plug-in. This can result in a more efficient filtering implemented either as part of the filter scope in the connector's configuration file, or in the developed filter plug-in.



## Enabling Load Balancing and Fail-Over
When you use the built-in features of both Guardium Data Protection and Guardium Insights, you might inadvertently distribute the entire set of received events to each Guardium instance, which can result in duplicated and redundant event processing.

To prevent this default behavior and ensure efficient operation, it is recommended to configure these mechanisms as part of the input scope in the configuration file of the installed connector. You can achieve this configuration through both pull (Pub/Sub[^5.1], JDBC[^5.2], SQS[^5.3]) and push (Filebeat[^5.4]) methods.

It's important to note that when using the push method in Guardium Data Protection, configuring the entire set of collectors as part of the input scope is necessary.

For more detailed information about each plug-in, please refer to the [Available Plug-ins](/docs/available_plugins.md) page.

</details>

<details open="open">
<summary>Deploying the Universal Connector</summary>

# Deploying the Universal Connector

In Guardium Data Protection, the overall workflow for deploying the Universal Connector is as follows:

1. Installing desired policies as instructed in [Policies](/docs/readme.md/#policies)

2. Install and configure a plugin[^6].

3. Configuring native auditing[^7] on the data source

4. Sending native audit logs to the Universal Connector, using either a push or pull workflow.

5. Configuring the Universal Connector to read the native audit logs.


More detailed information about the workflow for Guardium Data Protection can be found [here](/docs/Guardium%20Data%20Protection/uc_config_gdp.md).

In Guardium Insights, the workflow for deploying the Universal Connector is slightly different, and can be found [here](/docs/Guardium%20Insights/3.2.x/UC_Configuration_GI.md)


**Note that the specific steps for each workflow may differ slightly per different data sources. See our [list of available plugins](https://github.com/IBM/universal-connectors/blob/main/docs/available_plugins.md) to view detailed, step-by-step instructions for each supported data source/plug-in**.

***
### **Useful links:**
 - You can optionally use a Guardium client installed on a database running on your local host for forwarding native audit logs into Universal Connector via Filebeat or Syslog[^8]. See [Using GIM](/docs/general%20topics/GIM.md) for more information.
 - On how to configure Universal Connector for various data sources via AWS, see [Using AWS](/docs/general%20topics/aws.md)
 - On how to configure sample data sources and forward the generated audit log events into Universal Connector via Syslog or Filebeat, see [Sample data sources Configurations via Filebeat and Syslog](/docs/general%20topics/sample_data_sources_configurations.md)
 - To see a sample configuration of MySQL with Filebeat, see [Configuring SSL with Syslog](/docs/general%20topics/configure_syslog_ssl_failover.md)
 - To see suggested configurations for optimized database performance, see [here](/docs/general%20topics/suggested_configurations_to_optimize_database_performance.md)
 - To see a sample for changing connector protocol from TCP/UDP to SSL, see [Changing the MongoDB Filebeat connector protocol from TCP or UDP to SSL](/docs/general%20topics/Changing_the_MongoDB_Filebeat_connector_protocol_from_TCP_or_UDP_to_SSL.md)

   </details>

   <details open="open">
   <summary>Monitoring Universal Connector Connections</summary>
   # Monitoring Universal Connector Connections
   The Universal Connector is monitored via tools that are already familiar to Guardium Data Protection and Guardium Insights users, as well as some unique tools that can be found in the following links:

 [Monitoring UC connections in Guardium Data Protection](/docs/Guardium%20Data%20Protection/monitoring_GDP.md)

 [Monitoring UC connections in Guardium Insights](/docs/Guardium%20Insights/3.2.x/monitoring_GI.MD)

   </details>
   
   <details open="open">
     <summary>Policies</summary>
     
# Policies
     
With a few exceptions, using data from the Universal Connector is no different from using data from any other source in Guardium Data Protection or Guardium Insights. For using the Universal Connector in Guardium Data Protection, there are a few unique policies that can be found in this link:

[Configuring Policies for the Universal Connector](/docs/Guardium%20Data%20Protection/uc_policies_gdp.md)

For more general information about policies, refer to our  [Guardium Data Protection](https://www.ibm.com/docs/en/SSMPHH_latest/com.ibm.guardium.doc.admin/tshoot/policies.html) and [Guardium Insights](https://www.ibm.com/docs/en/SSWSZ5_latest/policies.html) policy documentation.

</details>

<details open="open">

  <summary>Known limitations</summary>

# Known limitations

  ***Please note: Limitations associated with specific data sources are described in the Universal Connector plug-in readme files for each data source. See [Available Plug-ins](/docs/available_plugins.md) for more information.***


### Guardium Data Protection

 * When configuring Universal Connectors, only use port numbers higher than 5000. Use a new port for each future connection.

 * Use only the packages that are supplied by IBM. Do not use extra spaces in the title.

* IPv6 support
  - S3 SQS and S3 CloudWatch plug-ins are not supported on IPv6 Guardium systems.
  - The DynamoDB plug-in does not support IPv6.

* Native MySQL plug-in[^9]:
  - Do not send the database name to Guardium if the database commands are performed by using MySQL native client.
  - When connected with this plug-in, queries for non-existent tables are not logged to GDM_CONSTRUCT.

* MongoDB plug-ins do not send the client source program to Guardium.

* Guardium recommends that you do not configure more than 10 Universal Connectors on a single Guardium collector.

</details>

<details open="open">

  <summary>FAQs and troubleshooting</summary>
  
  # FAQs 
  
[Here](/docs/Guardium%20Data%20Protection/faqs_troubleshooting_gdp.md) is a list of frequently asked questions and troubleshooting sections for Guardium Data Protection.

[Here](/docs/Guardium%20Insights/3.2.x/faqs_troubleshooting_gi.md) is a list of frequently asked questions and troubleshooting sections for Guardium Insights.

**Note:**
For further plug-in designated troubleshooting, see the "Troubleshooting" section in the plug-in's documentation linked at [Available Plug-ins](/docs/available_plugins.md)
  </details>
  
<details open= "open">
  <summary>Developing plug-ins</summary>

  # Developing plug-ins
  
Users can develop their own Universal Connector plugins, if needed, and contribute them back to the open source project, if desired.

(In order to overwrite old plug-ins, you can upload a new version from the official IBM GitHub page. Please make sure that the new plug-in has the exact same name as the old version.)

[Here](/docs/Guardium%20Data%20Protection/developing_plugins_gdp.md) is a guide for developing new plug-ins for Guardium Data Protection.

[Here](/docs/Guardium%20Insights/3.2.x/developing_plugins_gi.md) is a guide for developing new plug-ins for Guardium Insights.


### Use a Logstash Ruby filter plug-in 
For adding a parser to the filter section of the configuration file as a pre-processing stage prior to executing the filter plug-in, use the [Ruby filter plugin](https://www.elastic.co/guide/en/logstash/current/plugins-filters-ruby.html).

### Develop a filter plug-in 
* For developing a Ruby filter plug-in, use [How to write a Logstash filter plugin](https://www.elastic.co/guide/en/logstash/current/filter-new-plugin.html)
* For developing a Java filter plug-in, use [How to write a Java filter plugin](https://www.elastic.co/guide/en/logstash/current/java-filter-plugin.html)

### Develop an input plug-in
* For developing a Ruby input plug-in, use [How to write a Logstash input plugin](https://www.elastic.co/guide/en/logstash/current/input-new-plugin.html)
* For developing a Java input plug-in, use [How to write a Java filter plugin](https://www.elastic.co/guide/en/logstash/current/java-input-plugin.html)
*
***Note:***
It is the developer's responsibility to maintain and update the database's supported versions.

***
**Useful links:**
 - [Integrate Code Coverage tool into Universal Connector Plug-ins](/docs/integrate_code_coverage_into_plug-ins.md)
</details>

<details open= "open">
  <summary>Contributing</summary>
  
  # Contributing
  
  To make your connector plug-in available to the community, submit your connector to this repository for IBM Certification. We also accept updates or bug fixes to existing plug-ins, to keep them current:

- [Guidelines for contributing](CONTRIBUTING.md)
- Benefits include:

  - Free, comprehensive testing and certification.

  - Expanding the reach of product APIs.

  - Driving usage of a product or solution.
</details>

<details open= "open">
  <summary>Contact us</summary>
  
  # Contact us 
  
  If you find any problems or want to make suggestions for future features, please create [issues and suggestions on GitHub](https://github.com/IBM/universal-connectors/issues).

</details>

<details open= "open">
  <summary>Licensing</summary>

  # Licensing
  
  Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0
    
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
</details>

<details open="open">
  
  <summary>References</summary>
  
  # References
  
[^1]: See [IBM Guardium System Requirements and Supported Platforms](https://www.ibm.com/support/pages/ibm-guardium-system-requirements-and-supported-platforms)
[^2]: In GI 3.3.0, SaaS, and GDP 12.0.0 all the plug-ins listed in [Available Plug-ins](https://github.com/IBM/universal-connectors/blob/main/docs/available_plugins.md) are pre-installed upon startup.
[^3]: except GI SaaS 1.0.0, where no manual uploads by the customer are allowed.
[^4]: See GCP MySQL [Create the SQL Instance and Configure Logging](https://github.com/IBM/universal-connectors/blob/main/filter-plugin/logstash-filter-pubsub-mysql-guardium/README.md#create-the-sql-instance-and-configure-logging) section as an example of configuring audit log types via the cloud SQL Instance.
[^5.1]: See GCP Pub/Sub input plug-in [load-balancing configuration](https://github.com/IBM/universal-connectors/tree/main/input-plugin/logstash-input-google-pubsub#note-2) as an example of a pull method plug-in.
[^5.2]: See JDBC input plug-in [load-balancing configuration](https://github.com/IBM/universal-connectors/blob/main/input-plugin/logstash-input-jdbc/README.md#jdbc-load-balancing-and-failover-configuration) as an example of a pull method plug-in.
[^5.3]: See SQS input plug-in [load-balancing configuration](https://github.com/IBM/universal-connectors/blob/main/docs/general%20topics/aws.md#configuring-amazon-s3-auditing-via-sqs) as an example of a pull method plug-in.
[^5.4]: See Filebeat input plug-in [load-balancing configuration](https://www.elastic.co/guide/en/beats/filebeat/7.17/load-balancing.html) as an example of a push method plug-in.
[^6]: Check [Available Plug-ins](https://github.com/IBM/universal-connectors/blob/main/docs/available_plugins.md) for the list of plug-ins that are pre-installed and do not require any manual uploads.
[^7]: For some data sources, you can configure either real-time or historic audit logging  via the input plug-in's configuration file in its input scope (e.g., [JDBC Snowflake](https://github.com/IBM/universal-connectors/tree/main/filter-plugin/logstash-filter-snowflake-guardium#snowflake-guardium-logstash-filter-plug-in)).
[^8]: GIM is currently supported only for [Filebeat and Syslog on MongoDB](https://github.com/IBM/universal-connectors/blob/main/docs/general%20topics/GIM.md#configuring-gim-to-handle-filebeat-and-syslog-on-mongodb).
[^9]: See [MySQL filter plug-in page](https://github.com/IBM/universal-connectors/blob/main/filter-plugin/logstash-filter-mysql-guardium/README.md#mysql-guardium-logstash-filter-plug-in)

</details>




