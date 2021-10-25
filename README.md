<details open="open">
  <summary>Table of Contents</summary>

  - [Overview](#overview)
  - [How it works](#how-it-works)
  - [Available Universal Connector Plugins](#universal-connector-plugins)
  - [Using Universal Connector Plugins](#using-universal-connector-plugins)
  - [Packaging Guardium Insights Universal Connector Plugins](#packaging-universal-connector-plugins)
  - [Creating custom Universal Connector Plugins](#creating-custom-universal-connector-plugins)
  - [Contributing](#contributing)
  - [Contact us](#contact-us)
  - [Licensing](#licensing)

</details>

## Overview

The Universal Connector framework assists data security teams by providing a method to agentlessly collect activity and audit log data from a variety cloud and on-premise data sources.
- Easy to setup and configure
- Simple to maintain
- Normalizes collected data - making it consumable for security applications

An important note: only the plug-ins which appear in verifiedUCPlugins.txt are supported in Guardium Insights. The rest are only supported in Guardium GDP.

## How it works
Universal Connectors consist of series of three plugins within a [Logstash pipeline](https://www.elastic.co/guide/en/logstash/current/pipeline.html) that ingest, filter, and output events in a normalized, common format:

1) **Input Plugin**: Settings to pull events from APIs or receive push of events.
2) **Filter Plugin**: Parses, filters, and modifies events into a common format.
3) **Output Plugin**: Sends normalized events to locations to be consumed by security applications.

![Universal Connector - Logstash Pipeline](/docs/images/uc_overview.png)

Universal Connector plugins are packaged and deployed in a Docker container environment.

[Technical Demo](https://youtu.be/LAYhVoYMb28)

## Universal Connector Plugins

[View all available plugins](/docs/available_plugins.md)

## Using Universal Connector Plugins
- [In Guardium Data Protection](https://www.ibm.com/support/knowledgecenter/SSMPHH_11.3.0/com.ibm.guardium.doc.stap/guc/cfg_overview.html)
- [In Guardium Insights](https://www.ibm.com/docs/en/guardium-insights/3.0.x?topic=connector-configuring-universal)

## Packaging Universal Connector Plugins for use in Guardium Insights
Note: Pre-packaged plugins can be downloaded from [here](https://github.com/IBM/universal-connectors/releases)
1) **Clone universal-connectors project**: git clone https://github.com/IBM/universal-connectors.git
2) **Enter universal-connector project folder**: cd /path/to/universal-connectors
3) **Run packagePluginsForGuardiumInsights.sh script**: sh packagePluginsForGuardiumInsights.sh
4) **Find required plugins in packagedPlugins**: ls packagedPlugins

## Creating custom Universal Connector Plugins
- [Configuring native audit on the data source](https://www.ibm.com/support/knowledgecenter/SSMPHH_11.3.0/com.ibm.guardium.doc.stap/guc/cfg_native_audit_data_source.html)
- [Developing a filter plugin](https://www.ibm.com/support/knowledgecenter/SSMPHH_11.3.0/com.ibm.guardium.doc.stap/guc/develop_filter_plugin.html)
- [Create](https://www.elastic.co/guide/en/logstash/current/input-new-plugin.html) or use an [existing (recommended)](https://www.elastic.co/guide/en/logstash/current/input-plugins.html) input plugin
- [Testing a filter plugin in a development environment](https://www.ibm.com/support/knowledgecenter/SSMPHH_11.3.0/com.ibm.guardium.doc.stap/guc/test_filter_dev_env.html)
- [Installing and testing the filter or input plugin on a staging Guardium system](https://www.ibm.com/support/knowledgecenter/SSMPHH_11.3.0/com.ibm.guardium.doc.stap/guc/test_filter_guardium.html)
- [Publishing your plugin](https://www.ibm.com/support/knowledgecenter/SSMPHH_11.3.0/com.ibm.guardium.doc.stap/guc/publish_plugin.html)

Note: To package a plug-in for Guardium Insights, you need to include a few additional files, in this structure: 

    datasourceManifest.json
    config.json
    lang_en.json
    /YourPluginNamePackage
      |_  filter.conf
      |_  your-filter-offline-plugin.zip
      |_ manifest.json # details ...

* _datasourceManifest.json_ specifices the input and filter names, when installed on Logstash, as well as the supported platforms (one of "on-premise", "AWS", "Azure", "GCP", or a new platform of your choice).
* config.json contains parameters that should be filled-in by Guardium Insights users
* lang_en.json describes the parameters, for humans
* YourPluginNamePackage folder contains:
  * filter.conf - a validated Logstash filter configuration (could also be an input, but usually a filter), with parameters names as placeholders. These values will be replaced by user input, according to the input fields you specified in config.json
  *  your-filter-offline-plugin.zip - a Logstash offline plugin package. This is the plug-in you created for GDP (see links above for instructions)
  * manifest.json describes the plugin type ("filter" or "input"), its mechanism ("push" or "pull"), as well as the id/name of the plugin after installed on Logstash, supported data source(s), and other valuable meta-data.


## Contributing
To make your connector plugin available to the community, submit your connector for IBM Certification.
- [Guidelines for contributing](CONTRIBUTING.md)
- Benefits include:
  - Free, comprehensive testing and certification
  - Extending the reach of a product, feature, or function
  - Driving usage of a product or solution
- We also accept currency updates or bug fixes from the community for any existing connector plugins.

## Contact Us
If you have find any problems or want to make suggestions for future features please create [issues and suggestions on Github](https://github.com/IBM/universal-connectors/issues).


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
