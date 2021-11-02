<details open="open">
  <summary>Table of contents</summary>

  - [Overview](#overview)
  - [How it works](#how-it-works)
  - [Available Universal Connector plug-ins](#available-universal-connector-plug-ins)
  - [Using Universal Connector plug-ins](#using-universal-connector-plug-ins)
  - [Packaging Guardium Insights Universal Connector plug-ins](#packaging-guardium-insights-universal-connector-plug-ins)
  - [Creating custom Universal Connector plug-ins](#creating-custom-universal-connector-plug-ins)
  - [Contributing](#contributing)
  - [Contact us](#contact-us)
  - [Licensing](#licensing)

</details>

## Overview

The Universal Connector framework assists data security teams by providing an agentless method for collecting activity and auditing log data from a variety of cloud and on-premise data sources. The framework:
- Is easy to set up and configure.
- Is simple to maintain.
- Normalizes collected data, making it consumable for security applications.

Important note: Only the plug-ins that appear in verifiedUCPlugins.txt are supported in Guardium Insights. The rest are only supported in Guardium Data Protection (GDP).

## How it works
The Universal Connectors consist of a series of three plug-ins within a [Logstash pipeline](https://www.elastic.co/guide/en/logstash/current/pipeline.html) that ingest, filter, and output events in a normalized, common format:

1) **Input plug-in**: Settings to pull events from APIs or receive push of events.
2) **Filter plug-in**: Parses, filters, and modifies events into a common format.
3) **Output plug-in**: Sends normalized events to locations to be consumed by security applications.

![Universal Connector - Logstash pipeline](/docs/images/uc_overview.png)

Universal Connector plug-ins are packaged and deployed in a Docker container environment.

[Technical demo](https://youtu.be/LAYhVoYMb28)

## Available Universal Connector plug-ins

[View all available plug-ins](/docs/available_plugins.md)

## Using Universal Connector plug-ins
- [In Guardium Data Protection](https://www.ibm.com/support/knowledgecenter/SSMPHH_11.3.0/com.ibm.guardium.doc.stap/guc/cfg_overview.html).
- [In Guardium Insights](https://www.ibm.com/docs/en/guardium-insights/3.0.x?topic=connector-configuring-universal).

## Packaging Guardium Insights Universal Connector plug-ins
Note: Pre-packaged plugins can be downloaded from [here](https://github.com/IBM/universal-connectors/releases)
1) **Clone universal-connectors project**: git clone https://github.com/IBM/universal-connectors.git
2) **Enter universal-connector project folder**: cd /path/to/universal-connectors
3) **Run packagePluginsForGuardiumInsights.sh script**: sh packagePluginsForGuardiumInsights.sh
4) **Find required plugins in packagedPlugins**: ls packagedPlugins

## Creating custom Universal Connector plug-ins
- [Configuring native audit on the data source](https://www.ibm.com/support/knowledgecenter/SSMPHH_11.3.0/com.ibm.guardium.doc.stap/guc/cfg_native_audit_data_source.html)
- [Developing a filter plug-in](https://www.ibm.com/support/knowledgecenter/SSMPHH_11.3.0/com.ibm.guardium.doc.stap/guc/develop_filter_plugin.html)
- [Create](https://www.elastic.co/guide/en/logstash/current/input-new-plugin.html) or use an [existing (recommended)](https://www.elastic.co/guide/en/logstash/current/input-plugins.html) input plugin
- [Testing a filter plug-in in a development environment](https://www.ibm.com/support/knowledgecenter/SSMPHH_11.3.0/com.ibm.guardium.doc.stap/guc/test_filter_dev_env.html)
- [Installing and testing the filter or input plug-in on a staging Guardium system](https://www.ibm.com/support/knowledgecenter/SSMPHH_11.3.0/com.ibm.guardium.doc.stap/guc/test_filter_guardium.html)
- [Publishing your plug-in](https://www.ibm.com/support/knowledgecenter/SSMPHH_11.3.0/com.ibm.guardium.doc.stap/guc/publish_plugin.html)

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
To make your connector plug-in available to the community, submit your connector for IBM Certification:
- [Guidelines for contributing](CONTRIBUTING.md)
- Benefits include:
  - Free, comprehensive testing and certification.
  - Expanding the reach of product APIs.
  - Driving usage of a product or solution.
- We also accept currency updates or bug fixes from the community for any existing connector plug-ins.


## Contact Us
If you find any problems or want to make suggestions for future features, please create [issues and suggestions on Github](https://github.com/IBM/universal-connectors/issues).


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

[fork-a-repo]: https://docs.github.com/en/get-started/quickstart/fork-a-repo