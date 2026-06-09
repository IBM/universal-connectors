# Oracle Unified Audit universal connector with Wallet authentication

## Meet Oracle Unified Audit over Pipe with Wallet authentication
* Tested versions: 19C
* Environments: Oracle Cloud Infrastructure (OCI), Oracle Autonomous Database
* Supported inputs: Pipe input
* Supported Oracle versions: 19C
* Supported Guardium versions:
   * Guardium Data Protection: 12.2.2 or later

### Wallet ZIP:
1. **cwallet.sso** - Auto-login wallet (no password required)
2. **ewallet.p12** - Password-protected encrypted wallet
3. **tnsnames.ora** - Network service names configuration
4. **sqlnet.ora** - SQL*Net configuration with wallet location


## Before you begin

1. Download the Oracle Wallet files from OCI or create them on-premises.
2. Enable unified auditing in the Oracle database.
3. Configure network connectivity to the Oracle database (typically port 1522 for TCPS).

## Setting up the Wallet
### For Oracle Cloud Infrastructure (OCI):

1. Download the Wallet from the OCI console.</br>
     a. Navigate to your **Autonomous Database** or **Database Service**.</br>
     b. Click **DB Connection**. </br>
     c. Download the wallet ZIP file.</br>

2. The Wallet contains the following files.
   ```
   wallet/
   ├── cwallet.sso          # Auto-login wallet
   ├── ewallet.p12          # Encrypted wallet
   ├── tnsnames.ora         # Service names
   ├── sqlnet.ora           # Network configuration
   ├── ojdbc.properties     # JDBC properties
   └── keystore.jks         # Java keystore (optional)
   ```


3. Configure the `sqlnet.ora` file.
   ```properties
   WALLET_LOCATION = (SOURCE = (METHOD = file) (METHOD_DATA = (DIRECTORY=/usr/share/logstash/third_party/wallet/your_wallet_zip_name)))
   SSL_SERVER_DN_MATCH=yes
   SQLNET.AUTHENTICATION_SERVICES= (TCPS,NTS)
   ```

## Configuring the universal connector

### Step 1: Preparing the Wallet files

Upload the Wallet files to Guardium system by using GRDAPI endpoint.

1. Login to the CLI by using the following command.
   This command returns a **client_secret** that you can use to generate the token.

   ```
   grdapi register_oauth_client client_id="testWallet"
   ```

2. Generate the token. Exit the CLI, then run the following command on your MU.
      ```
         curl -k -X POST -d
         "client_id=testWallet&grant_type=password&client_secret=your_clien_secret&username=admin" -d password='your_password'
      ```
      
3. Upload the Wallet ZIP by using the following command.
   
      ```
      curl -k -X POST --header "Authorization: Bearer token" -F
      "walletFile=@local_file_path/your_wallet_zip_name.zip" \
      https://hostname:8443/restAPI/uploadOracleWalletZipFile
      ```
**Note:** For Guardium 12.2.1 with patch 5007, you must manually upload and extract the wallet ZIP file from the command line by completing the following steps.

  1. Navigate to the collector at `/var/IBM/Guardium/uc/third_party`. Then create a folder named `wallet`, and upload the wallet ZIP file to this folder by using the following command:
     
      ```
       scp -O your_local_wallet_zip_location/your_wallet_zip_name.zip root@host:/var/IBM/Guardium/uc/third_party/wallet/
      ```

  2. Extract the wallet file by using the following command:
     ```
     unzip your_wallet_zip_name.zip -d /var/IBM/Guardium/uc/third_party/wallet/
     ```
   
### Step 2: Configuring the Logstash legacy flow

1. Configure the Logstash legacy flow by using the following configuration template.

   ```
   input {
        pipe {
            type => "oua-wallet"
            command =>"${OUA_BINARY_PATH} -c ${THIRD_PARTY_PATH} -a
            ${THIRD_PARTY_PATH}/wallet/<your_wallet_zip_name> -s ${THIRD_PARTY_PATH} -r 100 -t
            1000 -p 20 -j username/password@SERVICE_NAME"
            # Server configuration
            add_field => {"SERVER_ADDRESS" =>
            "<Enter_Server_Address>"}
            add_field => {"SERVER_PORT" => "<Enter_Server_Port>"}
            # Wallet configuration - path on Guardium server where wallet is stored
            add_field => {"WALLET_ZIP_PATH" =>
            "${THIRD_PARTY_PATH}/wallet/<your_wallet_zip_name.zip>"}
            # Service name from tnsnames.ora in wallet (e.g., mydb_high,
            mydb_medium, mydb_low)
            add_field => {"SERVICE_NAME" => "<Enter_Service_Name>"}
        }
    }

    filter {
        if [type] == "oua" {
        	    ruby {
        		  code => "
        		    if event.get('message')
        		      event.set('message', event.get('message').gsub('\\\\\\', '\\\\\\\\'))
        		    end
        		  "
                }
        	    mutate {
                        gsub => [
                            "command", "\w+/[^\s@]+@", "*****@"
                        ]
                    }
        		json {
        			source => "message"
        		}
       		 if [obj_owner] == "SYS" or [obj_owner] == "AUDSYS" or [obj_owner] == "RDSADMIN"
                                   or [current_user] == "RDSADMIN" or [current_user] == "SYS" or [sql_text] =~ "DBMS_OUTPUT.GET_LINE"

                                 {
                                 drop{}
                                 }
        			mutate {
                        	add_field => {"[HostName]" => "%{SERVER_ADDRESS}" }
                        	add_field => {"[PortNumber]" => "%{SERVER_PORT}" }
                        	add_field => {"[accountId]" => "%{ACCOUNT_ID}" }
                            }
        		if "_jsonparsefailure" not in [tags] {
        			oua_filter {}
        		}
        	}
    }
   ```

### Step 3: Configuring the Logstash CM flow

1. Login to Guardium CM. Then navigate to the **Datasource Profile Management** page. 
2. Create a **Profile**, select **Oracle over Pipe**.
3. In the **Credential** field, select **Oracle Autonomous Database Wallet**, and complete the required fields.
    - Upload the Wallet configuration file: the `wallet.zip` file.
    - Enter a **Database Username** (e.g., admin).
    - Enter a **Database Password**.
4. Click **OK** to save the credential.
5. In the **Datasource Profile**, complete the required fields.
    - **Server Address**: IP or hostname of the Oracle database
    - **Server Port**: Port number for TCPS connection (the default is 1522)
    - **Account ID**: (Optional) Account identifier for Guardium
    - **Basic Instant Client package**: Download the [RPM file](https://download.oracle.com/otn_software/linux/instantclient/211000/oracle-instantclient-basic-21.1.0.0.0-1.x86_64.rpm). This release supports only Instant Client version 21.1.0.0.0 and later.
   - **Wallet Zip Path**: Path on the Guardium server in which the wallet ZIP is stored (e.g., `${THIRD_PARTY_PATH}/wallet/<your_wallet_zip_name.zip>`). </br>
     **Note:** This field is only required for Wallet authentication method. If you are using other authentication methods, leave this field blank.
   - **Command**: Use the same command as in the legacy flow. Replace the wallet path and service name variables with your specific values.
     ```
     ${OUA_BINARY_PATH} -c ${THIRD_PARTY_PATH} -a ${THIRD_PARTY_PATH}/wallet/<your_wallet_zip_name> -s ${THIRD_PARTY_PATH} -r 1 -t 1000 -p 10 -j username/${password}@SERVICE_NAME
     ```
6. Click **OK** to save the datasource profile.
7. Select the new datasource profile in the Oracle over Pipe configuration. Then, deploy it to the collectors.
8. A secret containing the user’s password for OUA universal connector must be created - Example: grdapi universal_connector_keystore_add key=OUA_USER_PASS password=<PASSWORD> where <PASSWORD> is the OUA universal connector user’s password for the database. OUA_USER_PASS will be used in the plug-in configuration as a variable for password secret.

 Note: For CM flow, the password secret also need to add in MU by using the grdapi command.

## Connection string format

When you use the Wallet authentication, use the service name from `tnsnames.ora`.

```
jdbc:oracle:thin:@<service_name>
```

Example service names from OCI wallet:
- `mydb_high` - High priority connection
- `mydb_medium` - Medium priority connection
- `mydb_low` - Low priority connection

## Troubleshooting common issues

1. **Wallet location does not exist** </br>
    a. Verify that the wallet path is correct.</br>
    b. Check file permissions.</br>
    c. Ensure that the wallet files are extracted from the ZIP file.</br>

2. **Wallet directory must contain either cwallet.sso or ewallet.p12** </br>
    a. Verify that the wallet files are present. </br>
    b. Check if the ZIP extraction was successful. </br>
    c. Ensure that the wallet was downloaded correctly from OCI. </br>

3. **SSL handshake failed** </br>
    a. Verify that the SSL certificates in the wallet are valid. </br>
    b. Check network connectivity to database. </br>
    c. Ensure that the port is correct (1522 for TCPS). </br>

4. **Service name not found** </br>
    a. Check that the `tnsnames.ora` file contains the service name. </br>
    b. Verify that `TNS_ADMIN` is set correctly. </br>
    c. Ensure that the service name matches the OCI configuration. </br>
