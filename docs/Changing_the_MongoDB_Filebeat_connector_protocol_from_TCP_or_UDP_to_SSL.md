# Changing the MongoDB Filebeat connector protocol from TCP or UDP to SSL

The default Filebeat internet protocol is TCP. To change the protocol FROM TCP/UDP to SSL, use the API to generate an SSL certificate. Then, modify the Filebeat configuration file on the MongoDB server with the certificate details and update the connector configuration on the collector.

To revert to TCP/UDP, revert the configuration changes described here.

## Procedure
1.	Generate the certificate. On the collector, run the API

          grdapi generate_ssl_key_universal_connector hostname=<specific hostname or a wildcard>

       If you want to overwrite the previous key certificate, include the ```overwrite``` parameter:

         grdapi generate_ssl_key_universal_connector hostname=<Guardium system name> overwrite=1
	 
       The public certificate prints to the console.

2.	Copy the certificate from the console output in step 1 (including ```Begin``` and ```End```) to a new certificate file. 

       Create the file with a name, for example ```logstash.crt```, and save it in the database server where Filebeat is installed, for example, in ```/root/tmp folder```.
   
3.	Add the certificate file path to the Filebeat configuration file filebeat.yml on the database server, in the output.logstash: section. It looks like this:
	                 
                      output.logstash:
	                  hosts: 
	                  hosts: ["<ipaddress1>:<port>","<ipaddress2>:<port>,"<ipaddress3>:<port>"...]
	                  #ssl.certificate_authorities: ["/etc/pki/root/ca.pem"]
                     ssl.certificate_authorities: ["<path to certificate>"]
                    
     Where ```<path to certificate>``` is the path to the certificate file that was copied to the database server in step 2.
4.	Create a connector in Guardium:

     a.	On the collector, go to ```Setup``` > ```Tools and Views``` > ```Configure Universal Connector```

     b.	Click the plus icon, type a name, and select the ```MongoDB using Filebeat``` connector template.

     c.	Under input configuration, locate the line:
 
      	# For SSL over Filebeat, uncomment the following lines after generating a SSL key and a certificate using GuardAPI (see documentation), copy the public certificate to your data source and adjust Filebeat configuration: 

    Uncomment the next three lines, and add the certificate location:
```
    ssl => true
	ssl_certificate => "${SSL_DIR}/logstash-beats.crt" 
	ssl_key => "${SSL_DIR}/logstash-beats.key"
  ```
  d.	Click ```Save```.
  
5. Optional: To overwrite or refresh the certificate after you save the configuration, modify something in the GUI configuration and save it. Guardium overwrites the connector image with a new certificate. Repeat steps 2 and 3 to copy the refreshed certificate to the database side for Filebeat.
