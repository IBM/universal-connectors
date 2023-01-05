<details open="open">
  <summary>Table of contents</summary>

  - [Overview](#overview)
  - [How it works](#how-it-works)
    * [The Workflows](#the-workflows)
    * [Keep In Mind](#keep-in-mind)
    * [Enabling Load-balancing and Fail-Over](#enabling-load-balancing-and-fail-over)
  - [Deploying univer***REMOVED***l connector](#deploying-univer***REMOVED***l-connector)
  - [Monitoring univer***REMOVED***l connector  connections](#monitoring-uc-connections)
  - [Policies](#policies)
  - [Known limitations](#known-limitations)
  - [FAQs](#faqs)
  - [Developing plug-ins](#developing-plug-ins)
    * [Use Logstash's Ruby filter plug-in](#-use-logstashs-ruby-filter-plug-in)
    * [Develop a filter plug-in](#-develop-a-filter-plug-in)
    * [Develop an input plug-in](#-develop-an-input-plug-in)
  - [Contributing](#contributing)
  - [Contact us](#contact-us)
  - [Licensing](#licensing)

</details>

## Overview

The Guardium univer***REMOVED***l connector enables Guardium Data Protection and Guardium Insights to get data from potentially any data source's native activity logs without using S-TAPs. It includes support for various plug-in packages, requiring minimal configuration. You can easily develop plug-ins for other data sources and install them in Guardium.

The captured events embed mes***REMOVED***ges of any type that is supported by the configured data source. That includes: information and administrative system logs (e.g.: login logs, various data lake platform native plug-in related data), DDLs and DMLs, errors of varying sub-types, etc. The incoming events received by the univer***REMOVED***l connector can be configured to arrive either encrypted or as plaintext.

Figure 1. Guardium univer***REMOVED***l connector architecture

![Univer***REMOVED***l Connector](/docs/images/guc.jpg)

<sub> Data flow from input plugin to guardium sniffer </sub>

The Guardium univer***REMOVED***l connector supports many platforms and connectivity options. It supports pull and push modes, multi-protocols, on-premises, and cloud platforms. For the data sources with pre-defined plug-ins, you configure Guardium to accept audit logs from the data source.

For data sources that do not have pre-defined plug-ins, you can customize the filtering and parsing components of audit trails and log formats. The open architecture enables reuse of prebuilt filters and parsers, and creation of shared library for the Guardium community.

The Guardium univer***REMOVED***l connector identifies and parses the received events, and converts them to a standard Guardium format. The output of the Guardium univer***REMOVED***l connector is forwarded to the Guardium sniffer on the collector, for policy and auditing enforcements. The Guardium policy, as usual, determines whether the activities are legitimate or not, when to alert, and the auditing level per activity.

The Guardium univer***REMOVED***l connector is scalable. It provides load-balancing and fail-over mechanisms among a deployment of Univer***REMOVED***l Connector instances, that either conform to Guardium Data Protection as a set of Guardium Collectors, or to Guardium Insights as a set of univer***REMOVED***l connector pods. For more information, see [Enabling Load-Balancing and Fail-Over](#enabling-load-balancing-and-fail-over).

Connections to databases that are configured with the Guardium univer***REMOVED***l connector are handled the ***REMOVED***me as all other datasources in Guardium. You can apply policies, view reports, monitor connections, for example.



## How it works

The Univer***REMOVED***l Connector under-the-hood is a Logstash pipeline comprising of a series of three plug-ins:

1. Input plug-in. This plug-in ingests events. Depending on the type of plug-in, there are settings to either pull events from APIs or receive a push of events.

2. Filter plug-in. This plug-in filters the events captured by the input plug-in. The filter plug-in parses, filters, and modifies event logs into a Guardium-digestible format.

3. Output plug-in. This plug-in receives the formatted event logs from the filter plug-in and transmits them to IBM Guardium (either Guardium Data Protection or Guardium Insights).

***Note: the Output plug-in is presented here as an internal component of the univer***REMOVED***l connector pipeline and is not to be accessed or modified by the user.***

![Univer***REMOVED***l Connector - Logstash pipeline](/docs/images/uc_overview.png)

Univer***REMOVED***l Connector plug-ins are packaged and deployed in a Docker container environment.

### The Workflows
There are a couple of flavors aimed at enabling audit log forwarding into Guardium for various data sources, comprised of either a cloud or on-premise data lake platform, of a database type that is supported by the Guardium sniffer[^1]:

  1. Utilize the out-of-the-box, pre-installed plug-in packages[^2] that require minimal configuration on the client's end by either plugging suited values into their respective template configuration files in the input and filter sections, or by adding a Ruby code sub-section to the ***REMOVED***id filter section in case a more complex parsing method is neces***REMOVED***ry as a pre-processing stage to be executed prior to the execution of the respective filter plug-in. See each plug-in's user manual via [Available Plug-ins](/docs/available_plugins.md).

  2. For data sources that are not yet supported, you can either upload an IBM-approved filter plug-in or [develop your own](#developing-plug-ins) and add it to our plug-in repository. You can also clone and modify the existing plug-ins as a template for your convenience (either in Ruby or Java)[^3]. You can optionally either let the parsing operations be executed by your filter plug-in, or assign this task to the Guardium Sniffer by transferring the event to the Output plug-in in a designated structure as part of the filter plug-in development, as instructed in the links in the [Developers Guide](#developing-plug-ins).

  3. For some data lake platforms it's possible to define inclusion and exclusion filters for the events routed to Univer***REMOVED***l Connector to be ingested by the input plug-in which can result in a more efficient filtering implemented either as part of the filter scope in the connector's configuration file, or the developed filter plug-in

### Keep In Mind:

  1. The pre-defined and pre-installed plug-ins do not require any manual uploads or other such pre-requisites on the user's end, as opposed to user made plug-ins or other available Logstash plug-ins. The user needs to simply use a ready-made template for plugging in values to the input and filter sections of their respective configuration files, or expand these sections by using online pre-installed Logstash plug-ins, or write their own Ruby code parser using the [Ruby filter plug-in](#-use-logstashs-ruby-filter-plug-in) as a pre-processing stage prior to executing the filter plug-ins.
  2. It's optional to add an input plug-in to the repository in case the existing ones are insufficient for your needs, although it's recommended to use one of the existing pre-installed input plug-ins and modify their respective config files' input section according to their documentation[^4].
  3. The user can choose to configure either pull or push methods via the mas***REMOVED***ging middleware service installed on the Data Lake Platform that is used by the input plug-in. Mes***REMOVED***ges can be received with pull or push delivery. In pull mode, the Univer***REMOVED***l Connector instance initiates requests to the remote service to retrieve mes***REMOVED***ges. In push mode, the remote service initiates requests to the Univer***REMOVED***l Connector instance to deliver mes***REMOVED***ges.
  4. The specific audit log types transmitted into Univer***REMOVED***l Connector from the Data Source are configurable via the SQL Instance settings installed on the Data Lake Platform. This can vary depending on the installed Data Lake Platform native plug-ins and the utilized mes***REMOVED***ging middleware service[^5].




### Enabling Load Balancing and Fail-over
Using the given out-of-the-box mechanisms in both GDP and GI might implicate distribution of the whole set of ingested events to each of the Guardium instances (i.e., Guardium Collectors and Univer***REMOVED***l Connector Pods respectively) in the set, causing duplications and redundant event processing. To properly avoid this fallback default behavior, these mechanisms are to be configured as part of the input scope of the installed Connector's configuration file[^6]. This is configurable via both pull and push methods. Note that the push method on GDP requires configuring the full set of Collectors as part of the ***REMOVED***id input scope. For detailed information regarding each plug-in, check the [Available Plug-ins](/docs/available_plugins.md) page.




## Deploying Univer***REMOVED***l Connector

In Guardium Data Protection, the overall workflow for deploying the univer***REMOVED***l connector is as follows:

1. Install desired policies as instructed in [Policies](#policies)

2. Uploading and installing a plugin

3. Configuring native auditing[^7] on the data source

4. Sending native audit logs to the univer***REMOVED***l connector, using either a push or pull workflow.

5. Configuring the univer***REMOVED***l connector to read the native audit logs


More detailed information about the workflow for GDP can be found [here](docs/uc_config_gdp.md).

In Guardium Insights, the workflow for deploying the univer***REMOVED***l connector is slightly different, and can be found [here](docs/UC_Configuration_GI.md)


**Note that the specific steps for each workflow may differ slightly per different data sources. See our [list of of available plugins](https://github.com/IBM/univer***REMOVED***l-connectors/blob/main/docs/available_plugins.md) to view detailed, step-by-step instructions for each supported data source/plug-in**.

***
**Miscellaneous: [Using GIM](docs/GIM.md)[^8], [Using AWS](docs/aws.md), [Configuring with MongoDB, Filebeat, Syslog, and MYSQL](docs/Migrated_pages.md)**
***

## Monitoring UC connections

The Univer***REMOVED***l connector is monitored via tools that are already familiar to Guardium Data Protection and Guardium Insights users. As well as some unique tools that can be found in the following links.

 [Monitoring UC connections in Guardium Data Protection](docs/monitoring_GDP.md)

 [Monitoring UC connections in Guardium Insights](/docs/monitoring_GI.MD)

## Policies

With a few Exceptions,  using data from the univer***REMOVED***l connector is no different than using data from any other source in Guardium Data Protection or Guardium Insights. For using the Univer***REMOVED***l Connector in Guardium Data Protection, there are a few unique policies that can be found in this link:

[Configuring Policies for the univer***REMOVED***l connector](docs/uc_policies_gdp.md)


## Known limitations

The univer***REMOVED***l connector has the following known limitations:

### Guardium Data Protection

 * When configuring univer***REMOVED***l connectors, only use port numbers higher than 5000. Use a new port for each future connection.

 * Use only the packages that are supplied by IBM. Do not use extra spaces in the title.

* IPV6 support
  - S3 SQS and S3 Cloudwatch plug-ins are not supported on IPV6 Guardium systems.
  - The DynamoDB plug-in does not support IPV6.

* MySQL plug-ins:
  - do not send the DB name to Guardium, if the DB commands are performed by using MySQL native client.
  - When connected with this plug-in, queries for non-existent tables are not logged to GDM_CONSTRUCT.

* MongoDB plug-ins do not send the client source program to Guardium.


### Guardium Insights

Known limitations for Guardium insights can be found in the UC plugin readme files for each datasource
***
***Limitations associated with specific datasources are described in the UC plugin readme files for each datasource.***

**See [Available Plug-ins](/docs/available_plugins.md) for more information.**
***

## FAQs

[Here](docs/faqs_gdp.md) is a list of frequently asked questions for Guardium Data Protection.

[Here](docs/faqs_gi.md) is a list of frequently asked questions for Guardium Insights.


## Developing plug-ins

Users can develop their own univer***REMOVED***l connector plugins, if needed, and contribute them back to the open source project, if desired.

(In order to overwrite old plug-ins, you can upload a new version from the official IBM Github page. Please make sure that the new plug-in has the exact ***REMOVED***me name as the old version.)

[Here](docs/developing_plugins_gdp.md) is a guide for developing new plug-ins for Guardium Data Protection.

[Here](docs/developing_plugins_gi.md) is a guide for developing new plug-ins for Guardium Insights.


### <a name="Ruby-filter"></a> Use Logstash's Ruby filter plug-in
For adding a parser to the filter section of the configuration file as a pre-processing stage prior to executing the filter plug-in, use the [Ruby filter plugin](https://www.elastic.co/guide/en/logstash/current/plugins-filters-ruby.html).

### <a name="filter-plug-in"></a> Develop a filter plug-in
* For developing a Ruby filter plug-in, use [How to write a Logstash filter plugin](https://www.elastic.co/guide/en/logstash/current/filter-new-plugin.html)
* For developing a Java filter plug-in, use [How to write a Java filter plugin](https://www.elastic.co/guide/en/logstash/current/java-filter-plugin.html)

### <a name="input-plug-in"></a> Develop an input plug-in
* For developing a Ruby input plug-in, use [How to write a Logstash input plugin](https://www.elastic.co/guide/en/logstash/current/input-new-plugin.html)
* For developing a Java input plug-in, use [How to write a Java filter plugin](https://www.elastic.co/guide/en/logstash/current/java-input-plugin.html)

**Note:**
It is the developer's responsibility to maintain and update the database's supported versions


***
## Contributing
**To make your connector plug-in available to the community, submit your connector to this repository for IBM Certification. We also accept updates or bug fixes to existing plug-ins, to keep them current:**

- **[Guidelines for contributing](CONTRIBUTING.md)**
- **Benefits include:**

  **- Free, comprehensive testing and certification.**

  **- Expanding the reach of product APIs.**

  **- Driving u***REMOVED***ge of a product or solution.**
***

## Contact Us
If you find any problems or want to make suggestions for future features, please create [issues and suggestions on Github](https://github.com/IBM/univer***REMOVED***l-connectors/issues).


## Licensing

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

[^1]: See [IBM Guardium System Requirements and Supported Platforms](https://www.ibm.com/support/pages/ibm-guardium-system-requirements-and-supported-platforms)
[^2]: In GI 3.3.0, SaaS, and GDP 12.0.0 all of the plug-ins listed in [Available Plug-ins](/docs/available_plugins.md) are pre-installed upon startup.
[^3]: with the exception of GI SaaS 1.0.0 where no manual uploads by the customer are allowed.
[^4]: See [Input plugins](docs/available_plugins.md#input-plugins) for the full list and documentation.
[^5]: See GCP MySQL's [Create the SQL Instance and Configure Logging](filter-plugin/logstash-filter-pubsub-mysql-guardium#create-the-sql-instance-and-configure-logging) section as an example of configuring audit log types via the cloud SQL Instance.
[^6]: See GCP's Pub/Sub input plug-in [load-balancing configuration](input-plugin/logstash-input-google-pubsub#note-2) as an example of a pull method plug-in.
[^7]: For some data sources, you can configure either real-time or historic audit logging  via the input plug-in's configuration file in its input scope (e.g., [JDBC Snowflake](https://github.com/infoinsights/guardium-snowflake-uc-filter#3-configure-the-input-and-filter-plugins))
[^8]: GIM is currently supported only for [Filebeat and Syslog on MongoDB](docs/GIM.md#configuring-gim-to-handle-filebeat-and-syslog-on-mongodb).
