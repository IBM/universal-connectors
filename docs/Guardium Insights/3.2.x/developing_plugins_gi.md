# Developing new plug-ins for Guardium Insights 

 ## Packaging Guardium Insights univer***REMOVED***l connector plug-ins

***Note: Pre-packaged plug-ins can be downloaded from [here](https://github.com/IBM/univer***REMOVED***l-connectors/releases)***

1) Clone the univer***REMOVED***l-connectors project: git clone https://github.com/IBM/univer***REMOVED***l-connectors.git

2) Enter the univer***REMOVED***l-connector project folder: cd /path/to/univer***REMOVED***l-connectors

3) Run the packagePluginsForGuardiumInsights.sh script: sh packagePluginsForGuardiumInsights.sh

4) Find the required plug-ins in packagedPlugins: ls packagedPlugins

## Creating custom univer***REMOVED***l connector plug-ins

First, follow the steps in [Developing a plug-in for Guardium Data Protection](../../Guardium%20Data%20Protection/developing_plugins_gdp.md)

Then, to package a plug-in that is suitable for Guardium Insights, you also need to include additional files, in this structure: 

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
