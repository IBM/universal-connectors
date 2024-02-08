# Generic-Guardium Logstash Filter plugin

This is a [Logstash](https://github.com/elastic/logstash) filter plug-in for the universal connector that is featured in IBM Security Guardium. It parses events and messages from any database audit log into a Guardium record instance (a standard structure made of several parts).

(If the audit log uses a syntax unfamilar to Guardium, parse the syntax to extract more fields. If you are not using a configuration based on one of our working configuration examples, consider contributing it as a pull request so it may used as a template in the future.)

The information is then sent over to Guardium. Guardium records include the accessor (the person who tried to access the data), session, data, and exceptions. If there are no errors, the data contains details about the query "construct". The construct details the main action (verb) and collections (objects) involved.

Currently, this plug-in will work only on IBM Security Guardium Data Protection, not Guardium Insights.

This plug-in is a tool that helps developers write other plug-ins. It is written in Java, so it can be used as a template for creating new connectors for any datasource.

You may directly copy it into a Guardium configuration for Universal Connectors. There is no need to upload the plugin code. 

However, in order to support a few features one zip has to be added with the name: generic-offline-plugins-7.5.2.zip
This plug-in is free and open-source (Apache 2.0). It can be used as a starting point to develop additional filter plug-ins for Guardium universal connector.

## Limitations
	• The generic plug-in does not support IPV6.
	• The generic plugin requires the timestamp of the audit event to be in format of yyyy-MM-dd HH:mm:ss zzz

## Configuring the Data Source

Before starting the actual plugin writing, set up the Data source, whose traffic is to be monitored. The data source can be a on premise setup or any cloud setup of the database. This plugin can support all those databases, whose datatype is of type TEXT and not CONSTRUCT.

## Enabling Auditing

In order to capture the exact sequence of events performed on the database, one must enable the native auditing that is supported for that particular datasource.

## How to write a plugin
To use this filter plugin, here's an overview of the process:
1. Set-up your dev environment
2. Build this filter into a GEM file
3. Install GEM on a local Logstash distributation
4. Run Logstash with a test configuration to see how this filter behaves.

Follow the steps below: 

### Set-up dev environment
Before you can build & create an updated GEM of this filter plugin, set up your environment as follows: 
1. Clone Logstash codebase & build its libraries as as specified in [How to write a Java filter plugin][logstash-java-plugin-dev]. Use branch 7.x (this filter was developed alongside 7.5 branch).  
2. Create _gradle.properties_ and add LOGSTASH_CORE_PATH variable with the path to the logstash-core folder you created in the previous step. For example: 

    ```LOGSTASH_CORE_PATH=/Users/taldan/logstash76/logstash-core```

3. Clone the [guardium-universalconnector-commons][github-uc-commons] project and build a JAR from it according to instructions specified there. The project contains Guardium Record structure you need to adjust, so Guardium universal connector can eventually feed your filter's output into Guardium. 
4. Edit _gradle.properties_ and add a GUARDIUM_UNIVERSALCONNECTOR_COMMONS_PATH variable with the path to the built JAR. For example:

    ```GUARDIUM_UNIVERSALCONNECTOR_COMMONS_PATH=../guardium-universalconnector-commons/build/libs```

If you'd like to start with the most simple filter plugin, we recommend to follow all the steps in [How to write a Java filter plugin][logstash-java-plugin-dev] tutorial.

### Build plugin GEM
To build this filter project into a GEM that can be installed onto Logstash, run 

    $ ./gradlew.unix gem --info

Sometimes, especially after major changes, clean the artifacts before you run the build gem task:

    $ ./gradlew.unix clean

### Install
To install this plugin on your local developer machine with Logstash installed, run:
    
    $ ~/Downloads/logstash-7.5.2/bin/logstash-plugin install ./logstash-filter-rds_guardium_filter-?.?.?.gem

**Notes:** 
* Replace "?" with this plugin version
* logstash-plugin may not handle relative paths well, so try to install the gem from a simple path, as in the example above. 

### Run on local Logstash
To test your filter using your local Logstash installation, run 

    $ ~/Downloads/logstash-7.5.2/bin/logstash -f filter-test-generator-postgres.conf
    
This configuration file generates an Event and send it thru the installed filter plugin. 

## Configuring the generic filter in Guardium

The Guardium universal connector is the Guardium entry point for native audit logs. The Guardium universal connector identifies and parses the received events, and converts them to a standard Guardium format. The output of the Guardium universal connector is forwarded to the Guardium sniffer on the collector, for policy and auditing enforcements. Configure Guardium to read the native audit logs by customizing the aurora-mysql template.

## Before you begin
• You must have permission for the S-Tap Management role. The admin user includes this role by default.
• Download the [logstash-filter-generic_guardium_filter.zip](https://github.com/IBM/universal-connectors/releases/download/v1.5.1/logstash-filter-generic_guardium_filter.zip). plug-in.

## Procedure
	1. On the collector, go to Setup > Tools and Views > Configure Universal Connector.
	2. Click Upload File and select the logstash-filter-generic_guardium_filter.zip plug-in. After it is uploaded, click OK.
	3. Click the Plus sign to open the Connector Configuration dialog box.
	4. Type a name in the Connector name field.
	5. Update the input section to add the details that can help fetch the audit events from the datasource. Depending upon the auditing feature that is selected, the audit logs can be stored either in audit-files, database tables or on Cloudwatch like cloud service. To fetch the data from these respective sources, one can make use of Filebeat, JDBC input plugin or cloudwatch_logs plugin respectively. Accordingly one must add otehr required parameters as well to connect to that datasource and fetch the events.
	6. Update the filter section to parse the fetched audit event. Here one must make use of Logstash internal functions and extract the different parts of the audit event into variables.
    7. The "type" fields should match in the input and the filter configuration sections. This field should be unique for every individual connector added.  
	8. Click Save. Guardium validates the new connector, and enables the universal connector if it was
	disabled. After it is validated, it appears in the Configure Universal Connector page.

## Further learning
Further learning on how to build and test universal connector filter plug-ins can be found on the Security Learning Academy:\
[Tech Day: Build and use universal connector plug-ins to agentlessly monitor databases](https://www.securitylearningacademy.com/course/view.php?id=6361)
