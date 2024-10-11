<details closed="closed">
  <summary>Table of contents</summary>

  - [Guardium universal connector](#guardium-universal-connector)
  - [Product versions](#product-versions)
  - [Supported data sources](#supported-data-sources)
  - [Contributing](#contributing)
  - [Contact us](#contact-us)
  - [Licensing](#licensing)

</details>

# Guardium universal connector

The Guardium universal connector enables Guardium Data Protection and Guardium Insights to get data from potentially any data source's native activity logs without using software taps (S-TAPs- the software agent usually used in Guardium). The universal connector includes support for various plug-in packages, requiring minimal configuration. You can easily develop plug-ins for other data sources and install them in Guardium.

## Product versions
The process for working with the universal connector differs for Guardium Data Protection, Guardium Insights, and Guardium Insights SaaS. We provide documentation for each, adapted to our latest product version. 

See documentation specific to [Guardium Data Protection](docs/Guardium%20Data%20Protection).

See documentation specific to [Guardium Insights 3.x](https://www.ibm.com/docs/en/guardium-insights/3.x?topic=connecting-data-sources).

See documentation specific to [Guardium Insights 3.2.x](https://github.com/IBM/universal-connectors/tree/main/docs/Guardium%20Insights/3.2.x).

See documentation specific to [Guardium Insights SaaS](https://www.ibm.com/docs/en/guardium-insights/saas?topic=connecting-data-sources)

See general documentation about the universal connector for all versions [here](/docs/readme.md)

## Supported data sources

Guardium can collect events using a variety of input and filter plug-in combinations.

Please refer to [available plugins](docs/available_plugins.md) to see the full list.


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

In order to generate gdp_plugins_templates.zip 
run: 
./gdp-packages/zipPackagesForGDP.sh
the zip will be under gdp-packages
