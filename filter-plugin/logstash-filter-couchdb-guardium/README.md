# CouchDB-Guardium Logstash filter plug-in
### Meet CouchDB
* Tested versions: 3.2.2
* Environment: On-premise, Iaas
* Supported inputs: Filebeat (push)
* Supported Guardium versions:
  * Guardium Data Protection: 11.4 and above
  * Guardium Data Security Center SaaS: 1.0

This is a [Logstash](https://github.com/elastic/logstash) filter plug-in for the universal connector that is featured in IBM Security Guardium. It parses events and messages from the CouchDB log into a [Guardium record](https://github.com/IBM/universal-connectors/blob/main/common/src/main/java/com/ibm/guardium/universalconnector/commons/structures/Record.java) instance (which is a standard structure made out of several parts). The information is then sent over to Guardium. Guardium records include the accessor (the person who tried to access the data), the session, data, and exceptions. If there are no errors, the data contains details about the query and Guardium sniffer parse the CouchDB queries.This plug-in prepares the Guardium Record object and relies on Guardium internal CouchDB parser to parse the database command. The CouchDB plugin supports only Guardium Data Protection as of now.

The plug-in is free and open-source (Apache 2.0). It can be used as a starting point to develop additional filter plug-ins for Guardium universal connector.

## 1. Installing CouchDB on Linux
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
6. The user is supposed to enter this private DNS name in the form, by keeping the prefix couchdb@.
7. Set the Erlang Magic Cookie. This is a unique identifier to authenticate for your cluster. All nodes must have the same cookie.
8. Configure the network interfaces on which CouchDB will be bound. To run a cluster, it is important to bind it to 0.0.0.0.
9. Next, set the admin password.
10. To verify that whether the installation was successful and the service is running, run the below curl command:
```
$ curl http://127.0.0.1:5984/
```
[Click Here](https://docs.couchdb.org/en/stable/config/intro.html) to learn more about CouchDB configuration

## 2. Viewing the audit logs

To view the audit logs, go to the Location: -/var/log/couchdb/*.log

## 3. Configuring Filebeat to push logs to Guardium

## a. Filebeat installation

### Procedure
```
Install filebeat using the following command:
$ curl -L -O https://artifacts.elastic.co/downloads/beats/filebeat/filebeat-oss-7.15.1-amd64.deb
$ sudo dpkg -i filebeat-oss-7.15.1-amd64.deb
```
You can learn more about Filebeat Setup [here](https://www.elastic.co/guide/en/beats/filebeat/current/filebeat-installation-configuration.html#:~:text=%20Filebeat%20quick%20start:%20installation%20and%20configuration%20edit,predefined%20assets%20for%20parsing%2C%20indexing%2C%20and...%20More)

## b. Filebeat configuration:

Filebeat must be configured to send the output to the chosen Logstash host and port. In addition, events are filtered out with the exclude_lines. To do this, modify the filebeat.yml file which you can find inside the folder where filebeat is installed.You can learn more about Filebeat Configuration [here](https://www.elastic.co/guide/en/beats/filebeat/current/filebeat-input-log.html#:~:text=include_lines%20edit%20A%20list%20of%20regular%20expressions%20to,the%20list.%20By%20default%2C%20all%20lines%20are%20exported.).

### Procedure:

1. Configuring the input section :-

   •  Locate "filebeat.inputs" in the filebeat.yml file and then use the "paths" attribute to set the location of the couchdb logs:
```
    filebeat.inputs:
   - type: filestream
   - id: <ID>
  enabled: true
    paths:
    - /var/log/couchdb/*.log 
    tags: ["CouchDB_On_Premise"]
    exclude_lines: ['--------']
```
2. Configuring the output section :
```
• Locate "output" in the filebeat.yml file, then add the following parameters.

• Disable Elasticsearch output by commenting it out.

• Enable Logstash output by uncommenting the Logstash section. For more information, see https://www.elastic.co/guide/en/beats/filebeat/current/logstash-output.html#logstash-output
```
For example:
```
                output.logstash:
                   hosts: ["localhost:8541"]

• The hosts option specifies the Logstash server and the port (8541) where Logstash is configured to listen for incoming Beats connections.

•You can set any port number except 5044, 5141, and 5000 (as these are currently reserved in Guardium v11.3 and v11.4 ).
```
```
• Locate "Processors" in the filebeat.yml file and then add below attribute to get timezone of Server:
	For more information, see https://www.elastic.co/guide/en/beats/filebeat/current/add-locale.html
	For example:-
       processors:
	 - add_locale: ~
 ```

3. To learn how to start FileBeat, see https://www.elastic.co/guide/en/beats/filebeat/current/filebeat-installation-configuration.html#start

#### For details on configuring Filebeat connection over SSL, refer [Configuring Filebeat to push logs to Guardium](https://github.com/IBM/universal-connectors/blob/main/input-plugin/logstash-input-beats/README.md#configuring-filebeat-to-push-logs-to-guardium).


## 4. Configuring the CouchDB filter in Guardium
The Guardium universal connector is the Guardium entry point for native audit logs. The Guardium universal connector identifies and parses the received events, and converts them to a standard Guardium format. The output of the Guardium universal connector is forwarded to the Guardium sniffer on the collector, for policy and auditing enforcements. Configure Guardium to read the native audit logs by customizing the CouchDB template.

### Before you begin
* Configure the policies you require. See [policies](/docs/#policies) for more information.
* You must have permission for the S-Tap Management role.The admin user includes this role by default.
* CouchDB-Guardium Logstash filter plug-in is automatically available with Guardium Data Protection. versions 12.x, 11.4 with appliance bundle 11.0p490 or later or Guardium Data Protection version 11.5 with appliance bundle 11.0p540 or later releases.

**Note**: For Guardium Data Protection version 11.4 without appliance bundle 11.0p490 or prior or Guardium Data Protection version 11.5 without appliance bundle 11.0p540 or prior, download the [logstash-filter-couchdb_guardium_filter.zip](https://github.com/IBM/universal-connectors/releases/download/v1.5.6/logstash-filter-couchdb_guardium_filter.zip) plug-in. (Do not unzip the offline-package file throughout the procedure).

* Download the plugin filter configuration file [couchdb.conf](couchdb.conf).

### Configuration
1. On the collector, go to ```Setup``` > ```Tools and Views``` > ```Configure Universal Connector```.
2. Enable the universal connector if it is disabled.
3. Click ```Upload File``` and select the offline [logstash-filter-couchdb_guardium_filter.zip](https://github.com/IBM/universal-connectors/releases/download/v1.5.6/logstash-filter-couchdb_guardium_filter.zip) plug-in. After it is uploaded, click ```OK```. This step is not necessary for Guardium Data Protection v11.0p490 or later, v11.0p540 or later, v12.0 or later.
4. Click the Plus sign to open the Connector Configuration dialog box.
5. Type a name in the ```Connector name``` field.
6. Update the input section to add the details from the [couchdb.conf](couchdb.conf) file's input part, omitting the keyword "input{" at the beginning and its corresponding "}" at the end.
7. Update the filter section to add the details from the [couchdb.conf](couchdb.conf) file's filter part, omitting the keyword "filter{" at the beginning and its corresponding "}" at the end.
8. The 'type' fields should match in the input and filter configuration sections. This field should be unique for every individual connector added.
9. Click ```Save```. Guardium validates the new connector and displays it in the Configure Universal Connector page.
10. After the offline plug-in is installed and the configuration is uploaded and saved in the Guardium machine, restart the Universal Connector using the ```Disable/Enable``` button.


## 5. Limitations
- A delay in generating logs is observed for SQL error exceptions in Guardium.
- We have setup a DbUser value to NA instead of undefined for login failed exceptions.
- The Following important fields couldn't be mapped with CouchDB logs
  - Source program : Not available with logs
  - OS User : Not available with logs
  - Client port : Not available with logs
  - Client HostName : Not available with logs

## 6. Configuring the Couchdb filters in Guardium Data Security Center
To configure this plug-in for Guardium Data Security Center, follow [this guide.](/docs/Guardium%20Insights/3.2.x/UC_Configuration_GI.md)
For the input configuration step, refer to the [Filebeat section](/docs/Guardium%20Insights/3.2.x/UC_Configuration_GI.md#Filebeat-input-plug-in-configuration).
