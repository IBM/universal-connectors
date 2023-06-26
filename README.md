<details open="open">
  <summary>Table of contents</summary>

  - [Overview](#overview)
    * [Releases](#latest-releases)
  - [Supported Data sources](#supported-data-sources)
  - [How it works](#how-it-works)
    * [The Workflows](#the-workflows)
    * [Keep In Mind](#keep-in-mind)
    * [Enabling Load-balancing and Fail-Over](#enabling-load-balancing-and-fail-over)
  - [Deploying universal connector](#deploying-universal-connector)
  - [Monitoring universal connector  connections](#monitoring-uc-connections)
  - [Policies](#policies)
  - [Known limitations](#known-limitations)
  - [FAQs and Troubleshooting](#faqs-and-troubleshooting)
  - [Developing plug-ins](#developing-plug-ins)
    * [Use Logstash Ruby filter plug-in](#use-logstash-ruby-filter-plug-in)
    * [Develop a filter plug-in](#develop-a-filter-plug-in)
    * [Develop an input plug-in](#develop-an-input-plug-in)
  - [Contributing](#contributing)
  - [Contact us](#contact-us)
  - [Licensing](#licensing)

</details>

## Overview

The Guardium universal connector enables Guardium Data Protection and Guardium Insights to get data from potentially any data source's native activity logs without using S-TAPs. It includes support for various plug-in packages, requiring minimal configuration. You can easily develop plug-ins for other data sources and install them in Guardium.

## Product versions
Workflows differ for Guardium Data Protection and Guardium Insights. It is recommended to use our latest product versions. 

Our latest product version for Guardium Data Protection is [11.5](docs/Guardium%20Data%20Protection).

Our latest product version for Guardium Insights is [3.2](docs/Guardium%20Insights/3.2.x/README.md)

Our latest product version for Guardium Insights SaaS is [1.0](docs/Guardium%20Insights/SaaS_1.0)

## Supported data sources
Connecting a data source to Guardium requires a designated plug-in. 

Please refer [available plugins](docs/available_plugins.md) to see the full list. 

## Developing plug-ins

Users can develop their own universal connector plugins, if needed, and contribute them back to the open source project, if desired.

(In order to overwrite old plug-ins, you can upload a new version from the official IBM GitHub page. Please make sure that the new plug-in has the exact same name as the old version.)

[Here](docs/Guardium%20Data%20Protection/developing_plugins_gdp.md) is a guide for developing new plug-ins for Guardium Data Protection.

[Here](docs/Guardium%20Insights/3.2.x/developing_plugins_gi.md) is a guide for developing new plug-ins for Guardium Insights.


### Use Logstash Ruby filter plug-in 
For adding a parser to the filter section of the configuration file as a pre-processing stage prior to executing the filter plug-in, use the [Ruby filter plugin](https://www.elastic.co/guide/en/logstash/current/plugins-filters-ruby.html).

### Develop a filter plug-in 
* For developing a Ruby filter plug-in, use [How to write a Logstash filter plugin](https://www.elastic.co/guide/en/logstash/current/filter-new-plugin.html)
* For developing a Java filter plug-in, use [How to write a Java filter plugin](https://www.elastic.co/guide/en/logstash/current/java-filter-plugin.html)

### Develop an input plug-in
* For developing a Ruby input plug-in, use [How to write a Logstash input plugin](https://www.elastic.co/guide/en/logstash/current/input-new-plugin.html)
* For developing a Java input plug-in, use [How to write a Java filter plugin](https://www.elastic.co/guide/en/logstash/current/java-input-plugin.html)

**Note:**
It is the developer's responsibility to maintain and update the database's supported versions

***
**Useful links:**
 - [Integrate Code Coverage tool into Universal Connector Plug-ins](/docs/integrate_code_coverage_into_plug-ins.md)


***
## Contributing
**To make your connector plug-in available to the community, submit your connector to this repository for IBM Certification. We also accept updates or bug fixes to existing plug-ins, to keep them current:**

- **[Guidelines for contributing](CONTRIBUTING.md)**
- **Benefits include:**

  **- Free, comprehensive testing and certification.**

  **- Expanding the reach of product APIs.**

  **- Driving usage of a product or solution.**
***

## Contact Us
If you find any problems or want to make suggestions for future features, please create [issues and suggestions on GitHub](https://github.com/IBM/universal-connectors/issues).


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

[^1] See [IBM Guardium System Requirements and Supported Platforms](https://www.ibm.com/support/pages/ibm-guardium-system-requirements-and-supported-platforms)
[^2] In GI 3.3.0, SaaS, and GDP 12.0.0 all the plug-ins listed in [Available Plug-ins](/docs/available_plugins.md) are pre-installed upon startup.
[^3] except GI SaaS 1.0.0, where no manual uploads by the customer are allowed.
[^4] See GCP MySQL [Create the SQL Instance and Configure Logging](filter-plugin/logstash-filter-pubsub-mysql-guardium#create-the-sql-instance-and-configure-logging) section as an example of configuring audit log types via the cloud SQL Instance.
[^5.1] See GCP Pub/Sub input plug-in [load-balancing configuration](input-plugin/logstash-input-google-pubsub#note-2) as an example of a pull method plug-in.
[^5.2] See Filebeat input plug-in [load-balancing configuration](https://www.elastic.co/guide/en/beats/filebeat/master/load-balancing.html) as an example of a push method plug-in.
[^6] Check [Available Plug-ins](/docs/available_plugins.md) for the list of plug-ins that are pre-installed and do not require any manual uploads.
[^7] For some data sources, you can configure either real-time or historic audit logging  via the input plug-in's configuration file in its input scope (e.g., [JDBC Snowflake](https://github.com/infoinsights/guardium-snowflake-uc-filter#3-configure-the-input-and-filter-plugins)).
[^8] GIM is currently supported only for [Filebeat and Syslog on MongoDB](docs/GIM.md#configuring-gim-to-handle-filebeat-and-syslog-on-mongodb).
[^9] See [MySQL filter plug-in page](filter-plugin/logstash-filter-mysql-guardium/README.md#mysql-guardium-logstash-filter-plug-in)
