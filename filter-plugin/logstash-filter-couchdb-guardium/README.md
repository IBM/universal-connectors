# CouchDB-Guardium Logstash filter plug-in
### Meet CouchDB
* Tested versions: 3.2.2
* Environment: On-premise, Iaas
* Supported inputs: Filebeat (push)
* Supported Guardium versions:
    * Guardium Data Protection: 11.4 and above

This is a Logstash filter plug-in for the univer***REMOVED***l connector that is featured in IBM Security Guardium. It parses events and mes***REMOVED***ges from the CouchDB log into a Guardium record instance (which is a standard structure made out of several parts). The information is then sent over to Guardium. Guardium records include the accessor (the person who tried to access the data), the session, data, and exceptions. If there are no errors, the data contains details about the query and Guardium sniffer parse the CouchDB queries.This plug-in prepares the Guardium Record object and relies on Guardium internal CouchDB parser to parse the database command. The CouchDB plugin supports only Guardium Data Protection as of now.

# Limitations
1. A delay in generating logs is observed for SQL error exceptions in Guardium.
2. We have setup a DbUser value to NA instead of undefined for login failed exceptions.
3. The Following important fields couldn't be mapped with CouchDB logs    
    - Source program : Not available with logs 
    - OS User : Not available with logs    
    - Client port : Not available with logs
    - Client HostName : Not available with logs  

## CouchDB Configuration on Linux
## Procedure:
1. Enabling the Apache CouchDB package repository
```
sudo apt update && sudo apt install -y curl apt-transport-https gnupg
curl https://couchdb.apache.org/repo/keys.asc | gpg --dearmor | sudo tee /usr/share/keyrings/couchdb-archive-keyring.gpg >/dev/null 2>&1
source /etc/os-release
echo "deb [signed-by=/usr/share/keyrings/couchdb-archive-keyring.gpg] https://apache.jfrog.io/artifactory/couchdb-deb/ ${VERSION_CODENAME} main" \
    | sudo tee /etc/apt/sources.list.d/couchdb.list >/dev/null
```
2. Install CouchDB on the server with the following command
```
sudo apt update
sudo apt install -y couchdb
```
3. After this,in that prompt,select options to configure CouchDB.configure either in standalone or clustered mode. Since we are installing on a single server, we will opt for the single-server standalone option.
4. In the next prompt, the user is supposed to configure the network interface which the CouchDB will bind to. In standalone server mode, the default is 127.0.0.1 
5. If you opt for clustered configuration, you are prompted to enter the Erlang Node Name of your server.
6. The user is supposed to enter this private DNS name in the form, by keeping the prefix couchdb@.
7. Set the Erlang Magic Cookie. This is a unique identifier to authenticate for your cluster. All nodes must have the ***REMOVED***me cookie.
8. Configure the network interfaces on which CouchDB will be bound. To run a cluster, it is important to bind it to 0.0.0.0.
9. Next, set the admin password.
10. To verify that whether the installation was successful and the service is running, run the below curl command:
```
$ curl http://127.0.0.1:5984/
```
  [Click Here](https://docs.couchdb.org/en/stable/config/intro.html) to learn more about CouchDB configuration

# Filebeat Setup And Configuration
## Procedure
```
Install filebeat using the following command:
$ curl -L -O https://artifacts.elastic.co/downloads/beats/filebeat/filebeat-oss-7.15.1-amd64.deb
$ sudo dpkg -i filebeat-oss-7.15.1-amd64.deb
```
You can learn more about Filebeat Setup [here](https://www.elastic.co/guide/en/beats/filebeat/current/filebeat-installation-configuration.html#:~:text=%20Filebeat%20quick%20start:%20installation%20and%20configuration%20edit,predefined%20assets%20for%20parsing%2C%20indexing%2C%20and...%20More)

## Filebeat Configuration

Filebeat must be configured to send the output to the chosen Logstash host and port. In addition, events are filtered out with the exclude_lines. To do this, modify the filebeat.yml file which you can find inside the folder where filebeat is installed.You can learn more about Filebeat Configuration [here](https://www.elastic.co/guide/en/beats/filebeat/current/filebeat-input-log.html#:~:text=include_lines%20edit%20A%20list%20of%20regular%20expressions%20to,the%20list.%20By%20default%2C%20all%20lines%20are%20exported.).

```
- Locate "filebeat.inputs" in the filebeat.yml file and then use the "paths" attribute to set the location of the couchdb logs:

filebeat.inputs:
- type: log
  enabled: true
  paths:
    - /var/log/couchdb/*.log 
    tags: ["CouchDB_On_Premise"]
    exclude_lines: ['--------']

-  While editing the Filebeat configuration file, di***REMOVED***ble Elasticsearch output by commenting it out. Then enable Logstash output by uncommenting the Logstash section. For more information, see https://www.elastic.co/guide/en/beats/filebeat/current/logstash-output.html#logstash-output

output.logstash:
  hosts: ["localhost:8541"]

```
Note:-The hosts option specifies the Logstash server and the port (5044) where Logstash is configured to listen for incoming Beats connections. You can set any port number except 5044, 5141, and 5000 (as these are currently reserved as ports for the MongoDB incoming log).


## Assumptions
* Logs are successfully pushed to the logstash from filebeat to the respective services.
* Logstash will pull logs only from filebeat.

## Configuring the CouchDB filter in Guardium
The Guardium univer***REMOVED***l connector is the Guardium entry point for native audit logs. The Guardium univer***REMOVED***l connector identifies and parses the received events, and converts them to a standard Guardium format. The output of the Guardium univer***REMOVED***l connector is forwarded to the Guardium sniffer on the collector, for policy and auditing enforcements. Configure Guardium to read the native audit logs by customizing the CouchDB template.

## Before you begin
* You must have permission for the S-Tap Management role.The admin user includes this role by default.
* Download the [guardium_logstash-offline-plugin-couchDB.zip](CouchdbOverFilebeatPackage/guardium_logstash-offline-plugins-couchDB.zip) plug-in.
* Download the plugin filter configuration file [couchdb.conf](couchdb.conf).

## Procedure
1. On the collector, go to Setup > Tools and Views > Configure Univer***REMOVED***l Connector.
2. Enable the connector if it is di***REMOVED***bled before uploading the UC plug-in.
3. Click Upload File and select the offline guardium_logstash-offline-plugin-couchdb.zip plug-in. After it is uploaded, click OK.
4. Click the Plus sign to open the Connector Configuration dialog box.
5. Type a name in the Connector name field.
6. Update the input section to add the details from couchdb.conf file's input part, omitting the keyword "input{" at the beginning and its corresponding "}" at the end.
7. Update the filter section to add the details from couchdb.conf file's filter part, omitting the keyword "filter{" at the beginning and its corresponding "}" at the end.
8. The "type" fields should match in input and filter configuration sections. This field should be unique for every individual connector added.
9. The "tags" parameter in the filter configuration should match the value of the attribute tags configured in the Filebeat configuration for a connector.
10. Click Save. Guardium validates the new connector, and enables the univer***REMOVED***l connector if it was
di***REMOVED***bled. After it is validated, it appears in the Configure Univer***REMOVED***l Connector page.

### Set-up dev environment
Before you can build & create an updated GEM of this filter plugin, set up your environment as follows: .
1. Clone the Logstash codebase & build its libraries as as specified in [How to write a Java filter plugin](https://github.com/logstash-plugins/logstash-filter-java_filter_example). Use branch 7.x (this filter was developed alongside 7.14 branch).  
2. Create _gradle.properties_ and add LOGSTASH_CORE_PATH variable with the path to the logstash-core folder you created in the previous step. For example: 

    ```LOGSTASH_CORE_PATH=/Users/taldan/logstash76/logstash-core```
	
3. Clone the [github-uc-commons](https://github.com/IBM/guardium-univer***REMOVED***lconnector-commons) project and build a JAR from it according to instructions specified there. The project contains Guardium Record structure you need to adjust, so Guardium univer***REMOVED***l connector can eventually feed your filter's output into Guardium. 
4. Edit _gradle.properties_ and add a GUARDIUM_UNIVERSALCONNECTOR_COMMONS_PATH variable with the path to the built JAR. For example:
    ```GUARDIUM_UNIVERSALCONNECTOR_COMMONS_PATH=../guardium-univer***REMOVED***lconnector-commons/build/libs```
If you'd like to start with the most simple filter plugin, we recommend following all the steps in [How to write a Java filter plugin](https://www.elastic.co/guide/en/logstash/7.16/java-filter-plugin.html) tutorial.
### Build plugin GEM
To build this filter project into a GEM that can be installed onto Logstash, run 
    $ ./gradlew.unix gem --info
Sometimes, especially after major changes, clean the artifacts before you run the build gem task:
    $ ./gradlew.unix clean
### Install
To install this plugin on your local developer machine with Logstash installed, run:
    
    $ ~/Downloads/logstash-7.14/bin/logstash-plugin install ./logstash-filter-couchdb_guardium_filter-?.?.?.gem
**Notes:** 
* Replace "?" with this plugin version
* The logstash-plugin may not handle relative paths well, so try to install the gem from a simple path, as in the example above. 
### Run on local Logstash
To test your filter using your local Logstash installation, run 
    $ ~/Downloads/logstash-7.14/bin/logstash -f couchdb-test.conf
    
This configuration file generates an Event and sends it through the installed filter plugin.


