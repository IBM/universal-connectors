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

## A little bit more about the Universal Connector

The Guardium universal connector enables Guardium Data Protection and Guardium Insights to get data from potentially any data source's native activity logs without using S-TAPs. It includes support for various plug-in packages, requiring minimal configuration. You can easily develop plug-ins for other data sources and install them in Guardium.

The captured events embed messages of any type that is supported by the configured data source. That includes: information and administrative system logs (e.g.: login logs, various data lake platform native plug-in related data), DDLs and DMLs, errors of varying subtypes, etc. The incoming events received by the universal connector can be configured to arrive either encrypted or as plain text.

Figure 1. Guardium universal connector architecture

![Universal Connector](/docs/images/guc.jpg)

<sub> Data flow from input plugin to guardium sniffer </sub>

The Guardium universal connector supports many platforms and connectivity options. It supports pull and push modes, multi-protocols, on-premises, and cloud platforms. For the data sources with pre-defined plug-ins, you configure Guardium to accept audit logs from the data source.

For data sources that do not have pre-defined plug-ins, you can customize the filtering and parsing components of audit trails and log formats. The open architecture enables reuse of prebuilt filters and parsers, and creation of shared library for the Guardium community.

The Guardium universal connector identifies and parses the received events, and converts them to a standard Guardium format. The output of the Guardium universal connector is forwarded to the Guardium sniffer on the collector, for policy and auditing enforcements. The Guardium policy, as usual, determines whether the activities are legitimate or not, when to alert, and the auditing level per activity.

The Guardium universal connector is scalable. It provides load-balancing and fail-over mechanisms among a deployment of universal connector instances, that either conform to Guardium Data Protection as a set of Guardium Collectors, or to Guardium Insights as a set of universal connector pods. The load-balancing mechanism distributes the events sent from the data source among a collection of universal connector instances installed on the Guardium endpoints (i.e., Guardium Data Protection collectors or Guardium Insights pods). For more information, see [Enabling Load-Balancing and Fail-Over](#enabling-load-balancing-and-fail-over).

Connections to databases that are configured with the Guardium universal connector are handled the same as all other datasources in Guardium. You can apply policies, view reports, monitor connections, for example.

[Suggested configurations to optimize database performance](/docs/suggested_configurations_to_optimize_database_performance.md)
