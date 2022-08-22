# FAQs for Guardium Insights


1.	**How many connections can one Univer***REMOVED***l Connector have?**

It depends on the Open Shift cluster resources. No limit defined.

2.	**Create connection started, connection is in starting up status a long time.**

It may take time, up to 15 minutes. Wait and refresh the connection status.

3.	**Upload plugin failed.**

Copy the plugin package from the official location and retry. Link to official location:
https://github.com/IBM/univer***REMOVED***l-connectors/blob/main/docs/available_plugins.md

4.	**Connection creation fails.**

•	Connect with CLI tools to your OpenShift cluster.

Example:

    cloudctl login -a https://cp-console.apps.lions-king.cp.fyre.ibm.com --skip-ssl-validation -u
    admin -p
    AHippopotamusPlaysHopscotchWithAnElephant -n
    staging

•	Find the UC-manager pod and check pod status.
The status should be 1/1 running as in example:

Example:

    oc get pods | grep univer***REMOVED***l

    staging-univer***REMOVED***l-connector-manager-794c5bb44c-

    cdp9g              1/1     Running     

    0               2d15h

•	Check the UC-manager logs for errors.

Example:

    oc logs staging-univer***REMOVED***l-connector-manager-

    794c5bb44c-cdp9g

5.	**Connection is in red or yellow and has an unhealthy Connectivity status on the Connections page.**

•	Connect with CLI tools to your openshift cluster.

Example:

     cloudctl login -a https://cp-console.apps.lions-refaelnew.cp.fyre.ibm.com --skip-ssl-validation -
     u admin -p

     AHippopotamusPlaysHopscotchWithAnElephant -n
     staging

•	Find UC pods and check that the pods' statuses are running 1/1 as in example. If status of uc pods is not running or not 1/1, perform steps from paragraph 4 above: check uc-manager logs.

Example:

      oc get pods | grep univer***REMOVED***l
      staging-univer***REMOVED***l-connector-manager-7cc4c6849b-
      s72tc              1/1     Running     0             15d

     stagingksultnyrrzbspmyqfppnd61-univer***REMOVED***l-
     connector-0              1/1     Running     0             15d

    stagingksultnyrrzbspmyqfppnd63-univer***REMOVED***l-
    connector-0              1/1     Running     
    0             13d

•	Enter into UC container and check ```UC  logstash_stdout_stderr.log``` for errors and exceptions:

Example:

    oc exec -it stagingksultnyrrzbspmyqfppnd61-univer***REMOVED***l-connector-0 bash
    cd /var/log/uc
    ls  -ltr
    cat logstash_stdout_stderr.log

•	Enable GI reconciler, then delete the old connection and create a new connection. To enable reconciler: connect with CLI to your cluster and run as in example.

Example:

    oc scale $( oc get deploy -oname | grep
      guardiuminsights-controller-manager | awk

      '{print $1}') --replicas=1

6.	**Does MustGather support the Guardium univer***REMOVED***l connector?**

*Yes, every MustGather option includes logs for the Guardium univer***REMOVED***l connector. (TBD)*

7.	**Data is missing from reports.**

Check the following steps:

a.	Go to the GI ```Connections``` page and see whether the connection is in green and healthy connectivity status.

* If the status is red, perform the item from question #3 above (Copy the plugin package from the official location and retry. )

* *Status in UI is green.*

b.	Check if traffic is running on the datasource.

c.	Check if auditing is set correctly on the datasource.

d.	Check that the auditing logs are created in the expected location on datasource

e.	Check that the connection settings are set correctly on the datasource side.

Example:

For mongo over Filebeat configuration, check on the datasource that:

•	The Mongo database auditing is configured in a mongod.conf file, mongo service is running , mongo audit log file is created.

•	In the filebeat.yml , in filebeat.inputs: check the path to audit logs. It should be ***REMOVED***me path as the one configured in the MongoDB configuration file in the auditing section.

•	In the filebeat.yml,  check that the tags are identical to the ones in the Connection configuration details in on the GI connection page (on the right side of the page).

To check datasource tag on GI :Go to the connection page, select your connection, click on connection name, check the datasource tag in Univer***REMOVED***l Connector Configuration (on the right side of the page)

•	Check that the server certificate is downloaded from the connection page and uploaded to the datasource. Check that the path to the certificate is set in the filebeat.yml and uncommented.

Example: ```(fragment from filebeat.yml)
ssl.certificate_authorities: ["/etc/pki/client/lions-dev7/GuardiumInsightsCA.pem"]```

•	In the filebeat.yml, check that the  ```output.logstash```  section is uncommented and that the ```output.elasticsearch``` is commented

•	In the filebeat.yml , in the ```output.logstash```   section, check the connection hosts and port. They should be identical to the ones in the connection details on the GI Connections page. (Port is 443 by default).

Example:

    hosts: ["usuj7iib4hsg2yutp9i3ca3-univer***REMOVED***l-
    connector.perf-ded-wdc06-
    0a94651246c65639d6ebe7da606c2479-0000.us-
    east.containers.appdomain.cloud:443"]

     #hosts: ["ukwh5r5mhhmipecfe44s6f1-univer***REMOVED***l-
     connector.apps.lions-master.cp.fyre.ibm.com:443"]

•	Restart filebeat service after editing the filebeat.yml file and checking status after restart

Example:

    service filebeat restart
    service filebeat status

•	Check the Filebeat log files. Enable the logging section in the filebeat.yml, restart Filebeat , go to filebeat logs located by path , configured in filebeat.yml logging section, and check the Filebeat logs for errors and exceptions.


f.	Check that communication between the database server and the managed unit is not blocked (GI  is accessible from the database server).

8.	**The Univer***REMOVED***l connector started but Guardium is not showing any events.**

•	Verify that a Policy with a Log full details rule is created and installed, and that no other policy blocks it.

•	Check the steps from paragraph 6 above.

•	Set the debug level in the uc_logstahs log and check “Guardium Events” in the log. If you found events in uc_logstahs.log, the problem may be in the minisnif.

To setup UC debug logs level:
connect with CLI to your cluster

Enter the uc pod

Run the command: ```${UC_SCRIPTS}/set_uc_log_level.sh "debug"```

•	*Create a MustGather and look for the univer***REMOVED***l connector log (TBD)*

*Plug-in questions*

*Univer***REMOVED***l connector does not start with your plug-in or configuration*

*See Installing and testing the filter or input plug-in on a staging Guardium system for details on how to collect diagnostic data by running a must gather command.*

*Is Java required to create a plug-in?*

*No, you can develop by using Ruby, especially when Guardium knows how to parse your data source commands. However, if you need to parse the commands for a new data source type, Java is the best choice.
How much time does it take to develop a plug-in?
Give yourself 4 - 5 weeks, maybe a bit more to perfect it.*
