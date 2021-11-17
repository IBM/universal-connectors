# S3-Guardium Logstash filter plug-in

This is a [Logstash](https://github.com/elastic/logstash) filter plug-in for the universal connector that is featured in IBM Security Guardium. It parses S3 database events into a Guardium record instance (which is a standard structure made out of several parts). The information is then sent over to Guardium. Guardium records include the accessor (the person who tried to access the data), the session, data, and exceptions. If there are no errors, the data contains details about the query "construct". The construct details the main action (verb) and collections (objects) involved. 

The plug-in is free and open-source (Apache 2.0). It can be used as a starting point to develop additional filter plug-ins for Guardium universal connector.

## Filter notes
* The filter supports events sent through Cloudwatch or SQS.

## Contribute

The documentation for the Logstash Java plug-ins is available [here](https://www.elastic.co/guide/en/logstash/current/contributing-java-plugin.html).

You can enhance this filter and open a pull request with suggested changes - or you can use the project to create a different filter plug-in for Guardium that supports other data sources.

To build and create an updated GEM of this filter plug-in which can be installed onto Logstash: 
1. Build Logstash from the repository source.
2. Create or edit _gradle.properties_ and add the LOGSTASH_CORE_PATH variable with the path to the logstash-core folder. For example: 
    
    ```LOGSTASH_CORE_PATH=/Users/taldan/logstash76/logstash-core```

3. Run ```$ ./gradlew.unix gem --info``` to create the GEM (ensure you have JRuby installed beforehand, as described [here](https://www.ibm.com/docs/en/guardium/11.3?topic=connector-developing-plug-ins)).

To test installation on your development Logstash
1. Install Logstash (using Brew, for example).
2. Install the filter plug-in (see above).
2. Run this command:

    ```$ logstash -f ./filter-test.conf --config.reload.automatic```
