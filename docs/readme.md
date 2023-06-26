<details open="open">
  <summary>Table of contents</summary>

  - [Available plug-ins](#available-plug-ins)
-   [Guardium Data Protection](#guardium-data-protection)
-   [Guardium Insights](#guardium-insights)
-   [Guardium Insights SaaS](#guardium-insights-saas)
-   [General topics](#general-topics)
-   [A little bit more about the universal connector](#a-little-bit-more-about-the-universal-connector)
-   [How it works](#how-it-works)
    * [The Workflows](#the-workflows)
    * [Keep In Mind](#keep-in-mind)
    * [Enabling Load-balancing and Fail-Over](#enabling-load-balancing-and-fail-over)
  - [Deploying universal connector](#deploying-universal-connector)
  - [Monitoring universal connector  connections](#monitoring-uc-connections)
  - [Policies](#policies)
  - [Known limitations](#known-limitations)
  - [FAQs and Troubleshooting](#faqs-and-troubleshooting)

</details>

# Universal Connector documentation

## Available plug-ins
See available plug-ins [here](/docs/available_plugins.md). 

## Guardium Data Protection
See our Guardium Data Protection documentation [here](/docs/Guardium%20Data%20Protection/).
## Guardium Insights
See our Guardium Insights documentation [here](/docs/Guardium%20Insights/).
## Guardium Insights SaaS
See our Guardium Insights SaaS documentation [here](/docs/Guardium%20Insights/SaaS_1.0/)
## General topics
[AWS](/docs/aws.md)

[Changing the MongoDB Filebeat connector protocol from TCP or UDP to SSL](/docs/Changing_the_MongoDB_Filebeat_connector_protocol_from_TCP_or_UDP_to_SSL.md)

[Configuring GIM to handle Filebeat and Syslog on MongoDB](/docs/GIM.md)

[Configuring SSL with Syslog](/docs/configure_syslog_ssl_failover.md)

[Integrating the Code Coverage tool into universal connector Plug-ins](/docs/integrate_code_coverage_into_plug-ins.md)

[Sample configuration: MySQL with Filebeat](/docs/cfg_mysql_filebeat.md)

[Sample data source configurations](/docs/sample_data_sources_configurations.md)

[Suggested configurations to optimize database performance](/docs/suggested_configurations_to_optimize_database_performance.md)

## A little bit more about the Universal Connector

The Guardium universal connector enables Guardium Data Protection and Guardium Insights to get data from potentially any data source's native activity logs without using S-TAPs. It includes support for various plug-in packages, requiring minimal configuration. You can easily develop plug-ins for other data sources and install them in Guardium.

The captured events embed messages of any type that is supported by the configured data source. That includes: information and administrative system logs (e.g.: login logs, various data lake platform native plug-in related data), DDLs and DMLs, errors of varying subtypes, etc. The incoming events received by the universal connector can be configured to arrive either encrypted or as plain text.

Figure 1. Guardium universal connector architecture

![Universal Connector](/docs/images/guc.jpg)

<sub> Data flow from input plugin to guardium sniffer in Guardium Data Protection </sub>

The Guardium universal connector supports many platforms and connectivity options. It supports pull and push modes, multi-protocols, on-premises, and cloud platforms. For the data sources with pre-defined plug-ins, you configure Guardium to accept audit logs from the data source.

For data sources that do not have pre-defined plug-ins, you can customize the filtering and parsing components of audit trails and log formats. The open architecture enables reuse of prebuilt filters and parsers, and creation of shared library for the Guardium community.

The Guardium universal connector identifies and parses the received events, and converts them to a standard Guardium format. The output of the Guardium universal connector is forwarded to the Guardium sniffer on the collector, for policy and auditing enforcements. The Guardium policy, as usual, determines whether the activities are legitimate or not, when to alert, and the auditing level per activity.

The Guardium universal connector is scalable. It provides load-balancing and fail-over mechanisms among a deployment of universal connector instances, that either conform to Guardium Data Protection as a set of Guardium Collectors, or to Guardium Insights as a set of universal connector pods. The load-balancing mechanism distributes the events sent from the data source among a collection of universal connector instances installed on the Guardium endpoints (i.e., Guardium Data Protection collectors or Guardium Insights pods). For more information, see [Enabling Load-Balancing and Fail-Over](#enabling-load-balancing-and-fail-over).

Connections to databases that are configured with the Guardium universal connector are handled the same as all other datasources in Guardium. You can apply policies, view reports, monitor connections, for example.

## How it works

Under the hood, the universal connector is a Logstash pipeline comprised of a series of three plug-ins:

1. Input plug-in. This plug-in ingests events. Depending on the type of plug-in, there are settings to either pull events from APIs or receive a push of events.

2. Filter plug-in. This plug-in filters the events captured by the input plug-in. The filter plug-in parses, filters, and modifies event logs into a Guardium-digestible format.

3. Output plug-in. This plug-in receives the formatted event logs from the filter plug-in and transmits them to IBM Guardium (either Guardium Data Protection or Guardium Insights).

***Note: the output plug-in is presented here as an internal component of the universal connector pipeline and is not to be accessed or modified by the user.***

![Universal Connector - Logstash pipeline](/docs/images/uc_overview.png)

Universal Connector plug-ins are packaged and deployed in a Docker container environment.

### The Workflows
There are a couple of flavors aimed at enabling audit log forwarding into Guardium for various data sources, comprised of either a cloud or on-premise data lake platform, of a database type that is supported by the Guardium sniffer[^1]:

  1. Utilize the out-of-the-box, pre-installed plug-in packages[^2] that require minimal configuration on the client's end by either plugging suited values into their respective template configuration files in the input and filter sections, or by adding a Ruby code subsection to the said filter section in case a more complex parsing method is necessary as a pre-processing stage to be executed prior to the execution of the respective filter plug-in. See each plug-in's user manual via [Available Plug-ins](/docs/available_plugins.md).

  2. For data sources that are not yet supported, you can either upload an IBM-approved filter plug-in or [develop your own](#developing-plug-ins) and add it to our plug-in repository. You can also clone and modify the existing plug-ins as a template for your convenience (either in Ruby or Java)[^3]. You can optionally either let the parsing operations be executed by your filter plug-in, or assign this task to the Guardium Sniffer by transferring the event to the output plug-in in a designated structure as part of the filter plug-in development, as instructed in the links in the [Developers Guide](#developing-plug-ins).


### Keep In Mind:

  1. The pre-defined and pre-installed plug-ins do not require any manual uploads or other such prerequisites on the user's end, as opposed to user-made plug-ins or other available Logstash plug-ins. You can simply use a ready-made template for plugging in values to the input and filter sections of their respective configuration files, expand these sections by using online pre-installed Logstash plug-ins, or write your own Ruby code parser using the [Ruby filter plug-in](#use-logstash-ruby-filter-plug-in) as a pre-processing stage prior to executing the filter plug-ins.
  2.  It is recommended to use one of the input plug-ins already in the repository and modify its config file input section. But if the input plug-ins already in the repository are insufficient for your needs, you can add a new one.
  3. You can choose to configure either pull or push methods via the messaging middleware service installed on the data lake platform that is used by the input plug-in. Messages can be received with pull or push delivery. In pull mode, the universal connector instance initiates requests to the remote service to retrieve messages. In push mode, the remote service initiates requests to the universal connector instance to deliver messages.
  4. The specific audit log types transmitted into the universal connector from the data source are configurable via the SQL instance settings installed on the data lake platform. This can vary depending on the installed data lake platform native plug-ins and the utilized messaging middleware service[^4].
  5. For some data lake platforms, you can define inclusion and exclusion filters for the events routed to the universal connector to be ingested by the input plug-in. This can result in a more efficient filtering implemented either as part of the filter scope in the connector's configuration file, or in the developed filter plug-in.




### Enabling Load Balancing and Fail-over
Using the given out-of-the-box mechanisms in both Guardium Data Protection and Guardium Insights might implicate distribution of the whole set of ingested events to each of the Guardium instances (i.e., Guardium collectors and universal connector pods respectively) in the set. This could cause duplications and redundant event processing. To properly avoid this fallback default behavior, configure these mechanisms as part of the input scope of the installed connector's configuration file. This is configurable via both pull[^5.1] and push[^5.2] methods. Note that the push method on Guardium Data Protection requires configuring the full set of collectors as part of the said input scope. For detailed information regarding each plug-in, check the [Available Plug-ins](/docs/available_plugins.md) page.




## Deploying the Universal Connector

In Guardium Data Protection, the overall workflow for deploying the universal connector is as follows:

1. Installing desired policies as instructed in [Policies](#policies)

2. Install and configure a plugin[^6]. [Guardium Insights plugins](docs/Guardium%20Insights/3.2.x/Plugins_management.md).

3. Configuring native auditing[^7] on the data source

4. Sending native audit logs to the universal connector, using either a push or pull workflow.

5. Configuring the universal connector to read the native audit logs.


More detailed information about the workflow for GDP can be found [here](docs/Guardium%20Data%20Protection/uc_config_gdp.md).

In Guardium Insights, the workflow for deploying the universal connector is slightly different, and can be found [here](docs/Guardium%20Insights/3.2.x/UC_Configuration_GI.md)


**Note that the specific steps for each workflow may differ slightly per different data sources. See our [list of available plugins](https://github.com/IBM/universal-connectors/blob/main/docs/available_plugins.md) to view detailed, step-by-step instructions for each supported data source/plug-in**.

***
**Useful links:**
 - You can optionally use a Guardium client installed on a database running on your local host for forwarding native audit logs into Universal Connector via Filebeat or Syslog[^8]. See [Using GIM](docs/GIM.md) for more information.
 - On how to configure Universal Connector for various data sources via AWS, see [Using AWS](docs/aws.md)
 - On how to configure sample data sources and forward the generated audit log events into Universal Connector via Syslog or Filebeat, see [Sample data sources Configurations via Filebeat and Syslog](docs/sample_data_sources_configurations.md)
 - To see suggested configurations for optimized database performance. see [here](docs/suggested_configurations_to_optimize_database_performance.md)
***

## Monitoring UC connections

The Universal connector is monitored via tools that are already familiar to Guardium Data Protection and Guardium Insights users. As well as some unique tools that can be found in the following links.

 [Monitoring UC connections in Guardium Data Protection](docs/Guardium%20Data%20Protection/monitoring_GDP.md)

 [Monitoring UC connections in Guardium Insights](/docs/Guardium%20Insights/3.2.x/monitoring_GI.MD)

## Policies

With a few exceptions,  using data from the universal connector is no different from using data from any other source in Guardium Data Protection or Guardium Insights. For using the universal connector in Guardium Data Protection, there are a few unique policies that can be found in this link:

[Configuring Policies for the universal connector](docs/Guardium%20Data%20Protection/uc_policies_gdp.md)

For more general information about policies, refer to our  [Guardium Data Protection](https://www.ibm.com/docs/en/SSMPHH_latest/com.ibm.guardium.doc.admin/tshoot/policies.html) and [Guardium Insights](https://www.ibm.com/docs/en/SSWSZ5_latest/policies.html) policy documentation.

## Known limitations

***Please note: limitations associated with specific datasources are described in the UC plugin readme files for each datasource.***

**See [Available Plug-ins](/docs/available_plugins.md) for more information.**

### Guardium Data Protection

 * When configuring universal connectors, only use port numbers higher than 5000. Use a new port for each future connection.

 * Use only the packages that are supplied by IBM. Do not use extra spaces in the title.

* IPV6 support
  - S3 SQS and S3 Cloudwatch plug-ins are not supported on IPV6 Guardium systems.
  - The DynamoDB plug-in does not support IPV6.

* Native MySQL plug-in[^9]:
  - do not send the database name to Guardium if the database commands are performed by using MySQL native client.
  - When connected with this plug-in, queries for non-existent tables are not logged to GDM_CONSTRUCT.

* MongoDB plug-ins do not send the client source program to Guardium.



## FAQs and Troubleshooting

[Here](docs/Guardium%20Data%20Protection/faqs_troubleshooting_gdp.md) is a list of frequently asked questions and troubleshooting sections for Guardium Data Protection.

[Here](docs/Guardium%20Insights/3.2.x/faqs_troubleshooting_gi.md) is a list of frequently asked questions and troubleshooting sections for Guardium Insights.

**Note:**
For further plug-in designated troubleshooting, see "troubleshooting" section in the plug-in's documentation linked at [Available Plug-ins](/docs/available_plugins.md)


