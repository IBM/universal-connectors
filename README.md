<details open="open">
  <summary>Table of contents</summary>

  - [Overview](#overview)
  - [How it works](#how-it-works)
  - [Deploying univer***REMOVED***l connector](#deploying-univer***REMOVED***l-connector)
  - [Monitoring univer***REMOVED***l connector connections](#monitoring-univer***REMOVED***l-connector-connections)
  - [Policies](#policies)
  - [Known limitations](#known-limitations)
  - [FAQs](#faqs)
  - [Developing plug-ins] (developing-plug-ins)
  - [Packaging Guardium Insights univer***REMOVED***l connector plug-ins] (#packaging-guardium-insights-univer***REMOVED***l-connector-plug-ins)
  - [Creating custom univer***REMOVED***l connector plug-ins] (#creating-custom-univer***REMOVED***l-connector-plug-ins)
  - [Contributing](#contributing)
  - [Contact us](#contact-us)
  - [Licensing](#licensing)

</details>

## Overview

he Guardium univer***REMOVED***l connector enables Guardium Data Protection and Guardium Insights to get data from potentially any data source's native activity logs without using S-TAPs. The Guardium Univer***REMOVED***l Connector includes support for MongoDB, MySQL, and Amazon S3, requiring minimal configuration. Users can easily develop plug-ins for other data sources, and install them in Guardium.

Figure 1. Guardium univer***REMOVED***l connector architecture

![Univer***REMOVED***l Connector](guc.jpg)

data flow from input plugin to guardium sniffer

The Guardium univer***REMOVED***l connector supports many platforms and connectivity options. It supports pull and push modes, multi-protocols, on-premises, and cloud platforms. For the data sources with pre-defined plug-ins, you configure Guardium to accept audit logs from the data source.

For data sources that do not have pre-defined plug-ins, you can customize the filtering and parsing components of audit trails and log formats. The open architecture enables reuse of prebuilt filters and parsers, and creation of shared library for the Guardium community.

The Guardium univer***REMOVED***l connector identifies and parses the received events, and converts them to a standard Guardium format. The output of the Guardium univer***REMOVED***l connector is forwarded to the Guardium sniffer on the collector, for policy and auditing enforcements. The Guardium policy, as usual, determines whether the activities are legitimate or not, when to alert, and the auditing level per activity.

The Guardium univer***REMOVED***l connector scales by adding Guardium collectors. It has a load-balancing and fail-over mechanism among multiple Guardium collectors.

Connections to databases that are configured with the Guardium univer***REMOVED***l connector are handled the ***REMOVED***me as all other datasources in Guardium. You can apply policies, view reports, monitor connections, for example.

#### Limitations
 * When configuring univer***REMOVED***l connectors, only use port numbers higher than 5000. Use a new port for each future connection.

* S3 SQS and S3 Cloudwatch plug-ins are not supported on IPV6 Guardium systems.

* The DynamoDB plug-in does not support IPV6.

* MySQL plug-ins do not send the DB name to Guardium, if the DB commands are performed by using MySQL native client.

* When connected with a MySQL plug-in, queries for non-existent tables are not logged to GDM_CONSTRUCT.

* MongoDB plug-ins do not send the client source program to Guardium.

* Use only the packages that are supplied by IBM. Do not use extra spaces in the title.



## How it works

The Univer***REMOVED***l Connectors consist of a series of three plug-ins within a Logstash pipeline that ingest, filter, and output events in a normalized, common format:

1. Input plug-in: Settings to pull events from APIs or receive push of events.

2. Filter plug-in: Parses, filters, and modifies events into a common format.

3. Output plug-in: Sends normalized events to locations to be consumed by security applications.

![Univer***REMOVED***l Connector - Logstash pipeline](/docs/images/uc_overview.png)

Univer***REMOVED***l Connector plug-ins are packaged and deployed in a Docker container environment.

[Technical demo](https://youtu.be/LAYhVoYMb28)

## Deploying Univer***REMOVED***l Connector

Overall, deploying the univer***REMOVED***l connector involves the following workflow:

a. uploading and installing a plugin

b. configuring native auditing on the data source

c. sending native audit logs to the univer***REMOVED***l connector (not all plugins require this)

d. configuring the univer***REMOVED***l connector to read the native audit logs

However, the specific steps for each workflow may differ slightly per different data sources. See our [list of of available plugins](https://github.com/IBM/univer***REMOVED***l-connectors/blob/main/docs/available_plugins.md) to view detailed, step-by-step instructions for each supported data source/plug-in.


## Monitoring UC connections

The Univer***REMOVED***l connector is monitored via tools that are already familiar to Guardium Data Protection and Guardium Insights users, for example [this page](https://www.ibm.com/docs/en/guardium/11.4?topic=started-data-activity-monitoring) in GDP and [this page](xxx) in GI. 

a. [separate readme for GDP UC monitoring, based off existing GDP doc]

b. [seperate readme for GI UC monitoring, based off existing GI doc]

## Policies

With a few exceptions, using data from the univer***REMOVED***l connector is no different than using data from any other source in Guardium Data Protection or Guardium Insights:

a. [separate readme for  GDP UC policies, based off existing GDP doc]

b. [separate readme for  GI UC policies, based off existing GI doc]


***NOTE: if policy-related restrictions/limitations are identical for GDP +GI, let's just list them here instead***

## Known limitations

The univer***REMOVED***l connector has the following known limitations:

### Guardium Data Protection

### Guardium Insights

(GI list, if different)

***Limitations associated with specific datasources are described in the UC plugin readme files for each datasource***

## FAQs

link to seperate readme [here](docs/faqs_gdp.md)

***Need to decide if it's 2 seperate readmes for GDP and GI, or one readme with 2 different sections- GDP and GI. Possibly a 3rd section- univer***REMOVED***l, overlapping FAQs for GDP AND GI.***

## Developing plug-ins

Users can develop their own univer***REMOVED***l connector plugins, if needed, and contribute them back to the open source project, if desired. 

 [link to documentation about developing UC plugins]. OR JUST DROP IN HERE?

***NOTE: any GI/GDP specific content around plugin development should be handled in the plugin development doc itself and not here: it's mostly going to be down to how the plugin receives its configuration setting from the end users, and that's just one part of plugin development.***


## Packaging Guardium Insights Univer***REMOVED***l Connector plug-ins
Note: Pre-packaged plug-ins can be downloaded from [here](https://github.com/IBM/univer***REMOVED***l-connectors/releases)
1) **Clone univer***REMOVED***l-connectors project**: git clone https://github.com/IBM/univer***REMOVED***l-connectors.git
2) **Enter univer***REMOVED***l-connector project folder**: cd /path/to/univer***REMOVED***l-connectors
3) **Run packagePluginsForGuardiumInsights.sh script**: sh packagePluginsForGuardiumInsights.sh
4) **Find required plug-ins in packagedPlugins**: ls packagedPlugins

## Creating custom Univer***REMOVED***l Connector plug-ins
- [Configuring native audit on the data source](https://www.ibm.com/docs/en/guardium/11.4?topic=ins-configuring-native-audit-data-source)
- [Developing a filter plug-in](https://www.ibm.com/docs/en/guardium/11.4?topic=ins-developing-filter-plug-in)
- [Create](https://www.elastic.co/guide/en/logstash/current/input-new-plugin.html) or use an [existing (recommended)](https://www.elastic.co/guide/en/logstash/current/input-plugins.html) input plugin
- [Testing a filter plug-in in a development environment](https://www.ibm.com/docs/en/guardium/11.4?topic=ins-testing-filter-in-dev-environment)
- [Installing and testing the filter or input plug-in on a staging Guardium system](https://www.ibm.com/docs/en/guardium/11.4?topic=dpi-installing-testing-filter-input-plug-in-staging-guardium-system)
- [Publishing your plug-in](https://www.ibm.com/docs/en/guardium/11.4?topic=ins-publishing-your-plug-in); see also [Contributing](#Contributing) section, below

Note: To package a plug-in for Guardium Insights, you need to include additional files, in this structure: 

    datasourceManifest.json
    config.json
    lang_en.json
    /YourPluginNamePackage
      |_  filter.conf
      |_  your-filter-offline-plugin.zip
      |_ manifest.json # details ...

* _datasourceManifest.json_: Specifies the input and filter names (when installed on Logstash), as well as the supported platforms ("on-premise", "AWS", "Azure", "GCP", or a new platform of your choice).
* config.json: Contains parameters that are completed by Guardium Insights users.
* lang_en.json: Describes the parameters.
* YourPluginNamePackage folder contains:
  * filter.conf: A validated Logstash filter configuration (could also be an input, but usually a filter), with parameters names as placeholders. These values will be replaced by user input, according to the input fields you specified in config.json.
  * your-filter-offline-plugin.zip: A Logstash offline plug-in package. This is the plug-in you created for GDP (see links above for instructions).
  * manifest.json: Describes the plug-in type ("filter" or "input"), its mechanism ("push" or "pull"), as well as the id/name of the plug-in after it is installed on Logstash, supported data source(s), and other valuable meta-data.


## Contributing
To make your connector plug-in available to the community, submit your connector to this repository for IBM Certification. We also accept updates or bug fixes to existing plug-ins, to keep them current:

- [Guidelines for contributing](CONTRIBUTING.md)
- Benefits include:
  - Free, comprehensive testing and certification.
  - Expanding the reach of product APIs.
  - Driving u***REMOVED***ge of a product or solution.


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
