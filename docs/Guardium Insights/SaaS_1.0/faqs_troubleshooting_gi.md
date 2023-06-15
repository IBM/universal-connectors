# FAQs and Troubleshooting for Guardium Insights

-   **How many universal connections can be configured at once?**

    It depends on the OpenShift cluster resources. No limit is defined.

-   **The connection is taking more than 15 minutes to start up.**

    It may take time, up to 15 minutes. Wait and refresh the connection status.

-   **Connection creation fails.**

    •	Connect via CLI to your OpenShift cluster.

    Example:
    
        cloudctl login -a <CLUSTER_URL> --skip-ssl-validation -u <USER> -p <PASSWORD> -n staging

    •	Find the universal connector manager pod and check the pod's status. The status should be 1/1 running.

    Example:
    
        oc get pods | grep universal
    
        staging-universal-connector-manager-794c5bb44c-
    
        cdp9g              1/1     Running     
    
        0               2d15h

    •	Check the UC-manager logs for errors.
        
    Example:
        
        oc logs staging-universal-connector-manager- 794c5bb44c-cdp9g

-   **The connection is in red or yellow and has an unhealthy Connectivity status on the Connections page.**

    •	Connect via CLI to your OpenShift cluster.
    
    Example:
    
         cloudctl login -a <CLUSTER_URL> --skip-ssl-validation -u <USER> -p <PASSWORD> -n staging
    
    •	Find the universal connector pods and check that the pods' statuses are running 1/1. If the status of the UC pods is not running or not 1/1, perform the steps from the above question and check the uc-manager logs.
    
    Example:
    
          oc get pods | grep universal
          staging-universal-connector-manager-7cc4c6849b-
          s72tc              1/1     Running     0             15d
    
         stagingksultnyrrzbspmyqfppnd61-universal-
         connector-0              1/1     Running     0             15d
    
        stagingksultnyrrzbspmyqfppnd63-universal-
        connector-0              1/1     Running     
        0             13d
    
    •	Enter the universal connector container pod and check ```UC  logstash_stdout_stderr.log``` for errors and exceptions:
    
    Example:
    
        oc exec -it stagingksultnyrrzbspmyqfppnd61-universal-connector-0 bash
        cd /var/log/uc
        ls  -ltr
        cat logstash_stdout_stderr.log
    
    •	Enable the Guardium Insights operator, delete the old connection, and create a new connection. To enable the Guardium Insights operator: connect via CLI to your cluster and run the command in the following example.
    
    Example:
    
        oc scale $( oc get deploy -oname | grep
          guardiuminsights-controller-manager | awk
    
          '{print $1}') --replicas=1

-   **Does MustGather support the Guardium universal connector?**

    Yes, every MustGather option includes logs for the Guardium universal connector.

-   **Data is missing from reports.**

    Check the following steps:
    
    +	Go to the Guardium Insights Connections page and see whether the connection is in green and healthy connectivity status.
    
    +	Check if traffic is running on the data source.
    
    +	Check if auditing is set correctly on the data source.
    
    +	Check that the auditing logs are created in the expected location on data source
    
    +	Check that the connection settings are set correctly on the data source side.
    
    +  Check that the required policy is installed.

        Example- Mongo Over Filbeat configuration:
        
        Check on the data source end that:
        + The Mongo database auditing is configured in a mongod.conf file, mongo service is running , mongo audit log file is created.
        + In filebeat.yml file , in filebeat.inputs: check the path to the audit logs. It should be same path as the one configured in the MongoDB configuration file in the auditing section.
        + In filebeat.yml file, check that the tags are identical to the ones in the Connection configuration details on the Guardium Insights connection page.
            
            To check the data source tag on Guardium Insights: Go to the connection page, select your connection, click on connection name, check the data source tag in Universal Connector Configuration 
            
        + Check that the server certificate is downloaded from the connection page and uploaded to the data source. Check that the path to the certificate is set in the filebeat.yml and uncommented.
            
            Example: ```(fragment from filebeat.yml)
            ssl.certificate_authorities: ["/etc/pki/client/lions-dev7/GuardiumInsightsCA.pem"]```
            
            •	In filebeat.yml, check that the  ```output.logstash```  section is uncommented and that the ```output.elasticsearch``` is commented
            
            •	In filebeat.yml, in ```output.logstash```   section, check the connection hosts and port. They should be identical to the ones in the connection details on the Guardium Insights Connections page. (Port is 443 by default).
            
            Example:
            
                hosts: ["usuj7iib4hsg2yutp9i3ca3-universal-
                connector.perf-ded-wdc06-
                0a94651246c65639d6ebe7da606c2479-0000.us-
                east.containers.appdomain.cloud:443"]
    
            •	Restart Filebeat service after editing the filebeat.yml file and checking status after restart
            
            Example:
            
                service filebeat restart
                service filebeat status
            
            •	Check the Filebeat log files. Enable the logging section in the filebeat.yml, restart Filebeat, go to filebeat logs located by path, configured in filebeat.yml logging section, and check the Filebeat logs for errors and exceptions.
    
    +	Check that communication between the database server, and verify that the managed unit is not blocked (Guardium Insights is accessible from the database server).

-   **Guardium is not showing any events.**
    
    •	Verify that the required policy is installed. 
    + For tests, we recommend to start with a log full details rule, and verify no other policy blocks it.
    
    •	Check the steps from question 6 above.
    
    •	Set the debug level in the uc_logstash log and check “Guardium Events” in the log. 
    + If you found events in `uc_logstash.log`, the problem may be in the mini-snif microservice.
    
    To set up universal connector debug logs level:
    Connect with CLI to your cluster,enter the universal connector pod and run the command: ```${UC_SCRIPTS}/set_uc_log_level.sh "debug"```
    
    •	Create a MustGather and look for the universal connector log