# Oracle Unified Audit Universal Connector

## Requirements

1. An Oracle database configured to use Oracle Unified Auditing (OUA) as well as a user for Guardium to read the audit tables in the Oracle database.
2. A Guardium collector with Universal Connector (UC) functionality as well as network connectivity to the Oracle database.

## Setup

1. Enable the Universal Connector feature on the designated Guardium collectors or stand-alone system.
2. Download the Oracle Instant Client version 21 RPM package from Oracle.
3. Download the Oracle Unified Audit Universal Connector plugin (guardium-oua-uc.zip).
4. Create the UC secret to store the Oracle user's password using the grdapi `universal_connector_keystore_add`. e.g. `grdapi universal_connector_keystore_add key=OUA_USER_PASS password=<PASSWORD>`
5. On the chosen collector, go to Setup > Tools and Views > Configure Universal Connector.
6. Click on the `Upload File` button and first upload the Oracle Instant Client RPM from step 2 and then the OUA UC plugin from step 3.
7. Click on the button to add a connector.
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
			source => "message"
		}
		if "_jsonparsefailure" not in [tags] {
			oua_filter {}
		}
	}
	```
	4. Click save. Guardium validates the new connector and enables the plugin. After it is validated, it appears in the Configure Universal Connector page.
