# Oracle Unified Audit Univer***REMOVED***l Connector

## Requirements

1. An Oracle database configured to use Oracle Unified Auditing (OUA) as well as a user for Guardium to read the audit tables in the Oracle database.
2. A Guardium collector with Univer***REMOVED***l Connector (UC) functionality as well as network connectivity to the Oracle database.

Currently, this plug-in will work only on IBM Security Guardium Data Protection, not Guardium Insights.

## Building

Update the variables in Makefile for your environment's Java home and Logstash location.

## Setup

1. Enable the Univer***REMOVED***l Connector feature on the designated Guardium collectors or stand-alone system.
2. Download the Oracle Instant Client version 21.1.0.0.0-1 RPM package (only this version is supported) from Oracle. https://download.oracle.com/otn_software/linux/instantclient/211000/oracle-instantclient-basic-21.1.0.0.0-1.x86_64.rpm
3. Download the Oracle Unified Audit Univer***REMOVED***l Connector plugin (guardium-oua-uc.zip).
4. Create the UC secret to store the Oracle user's password using the grdapi `univer***REMOVED***l_connector_keystore_add`. e.g. `grdapi univer***REMOVED***l_connector_keystore_add key=OUA_USER_PASS password=<PASSWORD>`
5. If UC is already running, you should restart UC to apply a new or updated key, using grdapi run_univer***REMOVED***l_connector overwrite_old_instance="true"
6. On the chosen collector, go to Setup > Tools and Views > Configure Univer***REMOVED***l Connector.
7. Click on the `Upload File` button and first upload the Oracle Instant Client RPM from step 2 and then the OUA UC plugin from step 3.
8. Click on the button to add a connector.
    1. Type any unique name in the `Connector Name` field.
	2. Paste the following into the `Input Configuration` field and update the placeholders for user, server address, server port, and instance name.
	```
	pipe {
		type => "oua"
		command => "${OUA_BINARY_PATH} -c ${THIRD_PARTY_PATH} -s ${THIRD_PARTY_PATH} -r 1 -t 1000 -p 10 -j <USER>/${OUA_USER_PASS}@<SERVER_ADDRESS>:<SERVER_PORT>/<INSTANCE_NAME>"
	}
	```
	3. Paste the following into the `Filter Configuration` field.
	```
	if [type] == "oua" {
		json {
			source => "mes***REMOVED***ge"
		}
		if "_jsonparsefailure" not in [tags] {
			oua_filter {}
		}
	}
	```
	4. Click ***REMOVED***ve. Guardium validates the new connector and enables the plugin. After it is validated, it appears in the Configure Univer***REMOVED***l Connector page.
