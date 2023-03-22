# Configuring SSL with syslog

Syslog for universal connector is based on Logstash's tcp input plug-in. In order to enable TCP with SSL, first use the API to generate an SSL certificate. Then, modify the rsyslog configuration file on the database server (MySQL or MongoDB) with the certificate details and update the connector configuration on the collector.

## Procedure
1.	Generate the certificate. On the collector, run the API

          grdapi generate_ssl_key_universal_connector hostname=<specific hostname or a wildcard>

       If you want to overwrite the previous key certificate, include the ```overwrite``` parameter:

         grdapi generate_ssl_key_universal_connector hostname=<Guardium system name> overwrite=1

       The public certificate prints to the console.

2.	Copy the certificate from the console output in step 1 (including ```Begin``` and ```End```) to a new certificate file.

       Create the file with a name, for example ```logstash.crt```, and save it in the database server where rsyslog is installed, for example, in ```/usr/local/etc/``` (depends on the operating system).

3.	Add the certificate file path to the rsyslog configuration file rsyslog.conf (```/usr/local/etc/rsyslog.conf``` for example) on the database server (example below is with MongoDB):

                      ```
                      # make gtls driver the default and set certificate files
                      global(
                      DefaultNetstreamDriver="gtls"
                      # DefaultNetstreamDriverCAFile="/path/to/rootCA.crt"
                      DefaultNetstreamDriverCertFile="/path/to/rsyslog.crt"
                      DefaultNetstreamDriverKeyFile="/path/to/rsyslog.key"
                      )

                      # load TCP listener
                      module(
                      load="imtcp"
                      StreamDriver.Name="gtls"
                      StreamDriver.Mode="1"
                      StreamDriver.Authmode="anon"
                      )
                      # choose a method to transmit data into rsyslog (via socket or straight from a file)

                      # For socket method uncomment lines below
                      # start up listener at port <PORT>
                      # input(
                      # type="imtcp"
                      # port="<PORT>"
                      # )

                      # For file method uncomment lines below
                      # $FileCreateMode 0640


                      # module(load="imfile")

                      # input(type="imfile" Tag="mongod: " File="/var/lib/mongo/auditLog.json" ruleset="pRuleset")


                      # ruleset(name="pRuleset") {
                      #	action(type="omfwd"
                      # 		keepalive="on"
                      #			protocol="tcp"
                      #			target="127.0.0.1"
                      #			port="5142")
                      #}

                      # ### begin forwarding rule ###

                      # Forward to Guardium collector
                      authpriv.* @@<COLLECTOR_IP>:<COL_PORT>
                      ```

     Where ```/path/to``` is the path to the certificate file that was copied to the database server in step 2.

4. Enable UC and add a connector with MongoDB (MySQL) with Syslog template from dropdown menu
5. Configure the input section to receive MongoDB (MySQL) events over Syslog for example with the same port configured in the rsyslog config file above. Example with MongoDB:

  ```
       tcp {
            port => 5001 type => "syslog-mongodb" dns_reverse_lookup_enabled => false
            ssl_enable => true
            ssl_cert => "${SSL_DIR}/app.crt"
            ssl_key => "${SSL_DIR}/app.key"
            ssl_verify => true
       }
   ```


4.	Create a connector in Guardium:

     a.	On the collector, go to ```Setup``` > ```Tools and Views``` > ```Configure Universal Connector```

     b.	Click the plus icon, type a name, and select the ```MongoDB using Syslog``` or  ```MySQL using Syslog``` connector template.

     c.	Under input configuration, locate the line:

       # For SSL over syslog, uncomment the following lines after generating a SSL key and a certificate using GuardAPI (see documentation), copy the public certificate to your data source and adjust the rsyslog configuration:

    Uncomment the next three lines, and add the certificate location:
```
    ssl => true
	ssl_certificate => "${SSL_DIR}/syslog.crt"
	ssl_key => "${SSL_DIR}/syslog.key"
  ```
  d.	Click ```Save```.

5. Optional: To overwrite or refresh the certificate after you save the configuration, modify something in the GUI configuration and save it. Guardium overwrites the connector image with a new certificate. Repeat steps 2 and 3 to copy the refreshed certificate to the database side for rsyslog.
