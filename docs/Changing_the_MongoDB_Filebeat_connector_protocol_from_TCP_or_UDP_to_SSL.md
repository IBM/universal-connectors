# Changing the MongoDB Filebeat connector protocol from TCP or UDP to SSL
Last Updated: 2022-10-03
The default Filebeat internet protocol is TCP. To change the protocol, use the API to generate a certificate. Then, modify the Filebeat configuration file on the MongoDB server with the certificate details and update the connector configuration on the collector.
About this task
This task describes changing the protocol from TCP/UDP to SSL. To revert to TCP/UDP, revert the configuration changes described here.
Procedure
1.	On the collector, run the API
grdapi generate_ssl_key_univer***REMOVED***l_connector hostname=<specific hostname or a wildcard>
If you want to overwrite the previous key certificate, include the overwrite parameter.
grdapi generate_ssl_key_univer***REMOVED***l_connector hostname=<Guardium system name> overwrite=1
The public certificate prints to the console.
2.	Copy the certificate from the console output in step 1 (including Begin and End) to a new certificate file. Create the file with a name, for example logstash.crt, and ***REMOVED***ve it in the database server where Filebeat is installed, for example, in /root/tmp folder.
3.	Add the path to the certificate file to the Filebeat configuration file filebeat.yml on the database server, in the output.logstash: section. It looks like:
4.	output.logstash:
5.	  hosts:
6.	  hosts: ["<ipaddress1>:<port>","<ipaddress2>:<port>,"<ipaddress3>:<port>"...]
7.	  #ssl.certificate_authorities: ["/etc/pki/root/ca.pem"]
  ssl.certificate_authorities: ["<path to certificate>"]
where <path to certificate> is the path to the certificate file that was copied to the database server in step 2.
8.	Create a connector in Guardium:
a.	On the collector, go to Setup > Tools and Views > Configure Univer***REMOVED***l Connector
b.	Click  , type a name, and select the MongoDB using Filebeat connector template.
c.	Under input configuration, locate the line:
d.	# For SSL over Filebeat, uncomment the following lines after generating a SSL key and a certificate using GuardAPI (see documentation), copy the public certificate to your data source and adjust Filebeat configuration:
Uncomment the next three lines, and add the certificate location:
ssl => true
	ssl_certificate => "${SSL_DIR}/logstash-beats.crt"
	ssl_key => "${SSL_DIR}/logstash-beats.key"
e.	Click Save.
9.	Optional: To overwrite or refresh the certificate after you ***REMOVED***ve the configuration, modify something in the GUI configuration ***REMOVED***ve it. Guardium overwrites the connector image with new certificate. Repeat steps 2 and 3 to copy the refreshed certificate to the database side for Filebeat.
Parent topic:
Guardium univer***REMOVED***l connector
Related reference
•	generate_ssl_key_univer***REMOVED***l_connector


https://www.ibm.com/docs/en/guardium/11.4?topic=guc-configuring-audit-logs-mysql-forwarding-guardium-via-filebeat
IBM Documentation 1.0 - SSMPHH_11.4.0/com.ibm.guardium.doc.stap/guc/cfg_filebeat_mongodb.html'
