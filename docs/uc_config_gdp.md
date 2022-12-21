# Configuring the Guardium universal connector

Review the options and the end-to-end flow for configuring the Guardium universal connector.

**Your role must include S-TAP Management Application role permission.**

## Procedure

1.	Allocate Guardium collectors to receive the audit files.

2.	For the data source types supported by Guardium:

      a.	Configure the native audit logs on the data source so that they can be parsed by Guardium, then configure the data shipper to forward the audit logs to the Guardium universal connector.

      b.	Configure the Guardium universal connector to read the native audit logs. See the section about adding connectors and plug-ins below.

***Note: if you are using secrets or sensitive information in your configuration, see the Creating and Managing Secrets section below before you configure a new connector***

3.	For a data source that does not have off-the-shelf support by Guardium, [upload a plug-in](/docs/available_plugins.md)

4.	Enable the universal collector feature on the designated Guardium collectors or the stand-alone system. See the section about enabling the Guardium universal connector on collectors below.

# Creating and Managing Secrets

It is more secure to store secrets in the universal connector keystore, instead of writing the passwords in plain text within a connector configuration. Store secrets before you add a connector configuration that is using a secret. You also need to restart the universal connector to make the new or updated secrets available for universal connector configurations.

## Procedure

1.	Create a secret. Log in to the Guardium CLI and create the key by using the grdapi command:
```
 grdapi universal_connector_keystore_add key=<key_name> password=<key_value>
For example, add these two keys:
grdapi universal_connector_keystore_add key=MYSQL_USERX_NAME
password=Guardium_qa      
grdapi universal_connector_keystore_add key=MYSQL_USERX_PASSWORD
password=guardium
```
Where:

- If ```MYSQL_USERX_NAME``` is the key name, ```guardium_qa``` is the key value.

- If ```MYSQL_USERX_PASSWORD``` is the key name, ```guardium``` is the key value.

***Note: Spaces are not allowed after and before “=” in this grdapi command.***

2.	Check that your keys were entered successfully.
Run the following command from the Guardium CLI:
```
grdapi universal_connector_keystore_list For example:
grdapi universal_connector_keystore_list
ID=0
Using JAVA_HOME defined java: /opt/java/openjdk
mysql_userx_name
mysql_userx_password
ok
```

3.	Add a key as an environment variable in the connector configuration.

      a. Log in to Guardium and then go to the ```Configure Universal Connector``` page.

      b. Upload jdbc driver (JAR file)

      c. Add or edit a connector configuration to use a secret. Instead of writing the secret in plain text, type the key that you created as an environment variable.
For example,
           ```
  jdbc {   
...  
jdbc_connection_string => "jdbc:..."  
jdbc_user => "${MYSQL_USERX_NAME}"   
jdbc_password => "${MYSQL_USERX_PASSWORD}"  
 ... }     
jdbc_password =>        
"${MYSQL_USERX_PASSWORD}"
         ```

    d. Save the configuration.

***Note: To use the JDBC input plug-in, you need to upload a driver (JAR file), then add the configuration.***

4.	Update or remove a secret. To update a secret, you need to remove the key, add it again, and then restart the universal connector with overwriting old instance option.

- To remove the key, run this command from the Guardium CLI:
```
grdapi universal_connector_keystore_remove key=<key_name>
grdapi universal_connector_keystore_remove key=MYSQL_USERX_NAME
ID=0
Using JAVA_HOME defined java: /opt/java/openjdk
Removed 'mysql_userx_name' from the Logstash keystore.
Ok
```

- To make sure that the key is no longer available to configurations, force the universal connector to fully restart by running this command:
```
grdapi run_universal_connector overwrite_old_instance="true"
```
- Listing the secret keys. To retrieve your updated list of the secrets after adding, removing, and updating keys, run this command:
```
grdapi universal_connector_keystore_list
```
# Enabling the Guardium universal connector on collectors

After you configure the database server native audit, and the file forwarding to Guardium, enable the Guardium universal connector on your collectors, in the UI or with an API.

**Tip: To simplify managing multiple collectors, create a managed unit group and use the API. See [Creating managed unit groups](https://www.ibm.com/docs/en/guardium/11.5?topic=functions-creating-managed-unit-groups).**

**Attention: When you reboot your Guardium system, restart the Guardium universal connector by running the API:**

```
grdapi run_local_universal_connector
```

Procedure
1.	On each collector that has connectors, enter the API command ```grdapi run_universal_connector```, or go to ```Setup``` > ```Tools and Views``` > ```Configure Universal Connector``` and click ```Enable```.

2.	To enable on multiple collectors by using the API, on the central manager enter:
```
grdapi run_universal_connector api_target_host=group:<managed unit group name>
```

3.	Check the status by:

- Checking that the ```Disabled``` button is active in the Configure Universal Connector page, which indicates that the Guardium Universal Connector is enabled.

- Entering the API command on the managed unit (not on the central manager): ```grdapi get_universal_connector_status```


# Adding connectors and plug-ins in Guardium

The Guardium universal connector is the Guardium entry point for native audit logs. The Guardium universal connector identifies and parses the received events, and converts them to a standard Guardium format.

The output of the Guardium universal connector is forwarded to the Guardium sniffer on the collector, for policy and auditing enforcements.

Configure Guardium to read the native audit logs by customizing a pre-defined template for data sources that have pre-defined plug-ins (Amazon S3, MongoDB, and MySQL), or with your own plug-in.

##  Before you begin
**You must have permission for the role S-Tap Management. The admin user has this role by default.**

## About this task

Pre-defined plug-ins: Guardium has a few pre-defined plug-ins for specific data sources: Amazon S3, MongoDB, and MySQL. In this scenario, you do not need to upload a plug-in. Instead, you can use the corresponding template to help you to configure the input and the filter. The templates include all required fields. The input and filter sections conform to the sections in an Elastic Logstash configuration file, described [here](https://www.elastic.co/guide/en/logstash/7.5/configuration-file-structure.html).

•	The default MongoDB connector is the preferred method of ingesting data. It does not require any additional configuration on Guardium if you use the default configuration. By default, the Guardium universal connector listens for MongoDB audit log events that are sent over Syslog (TCP port 5000, UDP port 5141) and Filebeat (port 5044). If you cannot use these ports, or if a parameter does not display in the reports as you expect, update the MongoDB connector configuration to match your system.

**Important: Each connector requires unique ports. Do not use the default ports for a customized connector configuration.**

•	Each MongDB connector on any one collector must have a unique type, and the filter configuration must use that type. For example, if the input configuration includes:
```
udp { port => 5141 type => "syslogMongoDB" }
```
then the filter must match it:
```
if [type] == "syslogMongoDB" {
```
*** Tip: When you save a connector configuration, Guardium stops the universal connector, verifies the new connection syntax, and initiates the new connection. Then, it restarts the universal connector. To prevent unnecessary loss of data during this stop period (usually about 1 minute), verify new configurations on a test Guardium system before you add them to your live system.***

## Procedure

1.	On the collector, go to ```Setup``` > ```Tools and Views``` > ```Configure Universal Connector```.

2.	Click the plus icon. The Connector Configuration dialog opens.

3.	For pre-defined plug-ins:

a.	Type a name in the Connector name field.

b.	From the Connector template drop-down list, select the template that most closely matches your system and follow the instructions in the sections that describe each template.

c.	Click Save. Guardium validates the new connector, and enables the universal connector if it was disabled. After it is validated, it appears in the Configure Universal Connector page.

4.	For offline plug-in packs and related files, [upload a plug-in here](docs/available_plugins.md).
