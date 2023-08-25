# Yugabyte-Guardium Logstash filter plug-in

### Meet Yugabyte
* Tested versions: 2.14
* Environment: On-premise
* Supported inputs: Filebeat (push), Syslogs (push)
* Supported Guardium versions:
    * Guardium Data Protection: 11.4 and above
    * Guardium Insights SaaS: 1.0
  
This is a [Logstash](https://github.com/elastic/logstash) filter plug-in for the universal connector that is featured in IBM Security Guardium. It parses events and messages from the Yugabyte audit log into a [Guardium record](https://github.com/IBM/universal-connectors/blob/main/common/src/main/java/com/ibm/guardium/universalconnector/commons/structures/Record.java) instance (which is a standard structure made out of several parts). The information is then sent over to Guardium. Guardium records include the accessor (the person who tried to access the data), the session, data, and exceptions. If there are no errors, the data contains details about the query "construct". The construct details the main action (verb) and collections (objects) involved.

The plug-in is free and open-source (Apache 2.0). The plug-in is written in Java. There is no need for plug-in code, only a jar archive.

Yugabyte DB re-uses Postgres Query language (PSQL) and Cassandra Query Language (CQL) just for database interactions. PSQL is renamed as `ysql` and CQL is renamed as `ycql`. However, the YB database engine is not built on Postgres or Cassandra. 
Hence, a developer can take advantage of SQL syntax, NoSQL syntax, or both. Yugabyte is a distributed SQL database that supports distributed transactions.

## 1. Configuring the Yugabyte database

### Procedure

1. Install the flavor of Yugabyte database according to the environment by following the instructions in the documentation.(https://docs.yugabyte.com/stable/quick-start/).
2. The Yugabyte DB can be managed using a provided utility named yb-ctl(https://docs.yugabyte.com/preview/admin/yb-ctl/).
3. To connect with the Database there are two types of APIs available, `ysql` and `ycql`.

## 2. Enabling the audit logs:

### Procedure

1. Yugabyte supports the below log types:
   1. `postgres-*.log` : The logs related to the operations performed using the API `ysql`.
   2. `yb-tserver.*.*.log.(WARNING|INFO|ERROR|FATAL).*-*.*` : The logs related to the operations performed 
   using the API `ycql`.
2. These instructions will use both types of files for the Guardium filter, as it 
contains all database-related log events.
3. Log configuration:
   1. YugabyteDB YSQL - uses PostgreSQL Audit Extension (pgAudit)
      1. By using and providing value to the flag `--ysql_pg_conf_csv`, log output can be configured.
      2. This flag should be passed while starting the YB server using the utility `ybctl`. <br/><br/>
   2. Yugabyte DB YCQL -
      1. Logs can be enabled by setting the flag `--ycql_enable_audit_log=true` for `ycql` APIs. <br/><br/>
   3. The minimum settings are mentioned here so that Guardium can collect minimum data to serve useful information.
      ```
      --ysql_pg_conf_csv=pgaudit.log='ALL',pgaudit.log_level=INFO,pgaudit.log_parameter=true,pgaudit.log_relation=on,log_line_prefix='%n %r [%p] %a %u %d %c %x '
      --ycql_enable_audit_log=true
      --ycql_audit_included_categories=QUERY,DML,DDL,DCL,AUTH,PREPARE,ERROR,OTHER
      --ycql_audit_log_level=INFO
      ```
   4. Create a configuration file with the content in the step 3(iii) and start the Yugabyte server as follows:
      ```text
       bin/yugabyted start --tserver_flags=flagfile=path/to/created/conf/file 
      ```
   5. A detailed documentation can be 
   found [here](https://docs.yugabyte.com/preview/secure/audit-logging/audit-logging-ysql/#enable-audit-logging). <br/><br/>

## 3. Viewing the audit logs

To view the logs, go to the path `yb_data/logs` relative to Yugabyte installation directory.

## 4. Configuring Filebeat to push logs to Guardium

## a. Filebeat installation

### Procedure:

To install Filebeat on your system, follow the steps in this topic:
    https://www.elastic.co/guide/en/beats/filebeat/current/filebeat-installation-configuration.html#installation

## b. Filebeat configuration:

To use Logstash to perform additional processing on the data collected by Filebeat, we need to configure Filebeat to use Logstash. To do this, modify the filebeat.yml file which you can find inside the folder where Filebeat is installed. Follow these instructions for finding the installation directory: https://www.elastic.co/guide/en/beats/filebeat/current/directory-layout.html

### Procedure:

1. Configuring the input section :

* Locate "filebeat.inputs" in the filebeat.yml file, then add the following parameters.
```
    filebeat.inputs:
       - type: filestream
         enabled: true
         paths : 
            - /path/to/logs/directory/postgresql-*.log
            - /path/to/logs/directory/yb-tserver.*.*.log.INFO.*
            - /path/to/logs/directory/yb-tserver.*.*.log.FATAL.*
            - /path/to/logs/directory/yb-tserver.*.*.log.WARNING.*
            - /path/to/logs/directory/yb-tserver.*.*.log.ERROR.*
```
* To process multi-line audit events, add the settings in same inputs section.
```
Parsers:
    - multiline:
        type: pattern
        pattern: '^(((I|W|E|F)[0-9]+\s[0-9:.]+).*)$|^([0-9.]+).*(LOG|ERROR|FATAL):.*'
        match: after
        negate: true
```
* Add the tags to uniquely identify the Yugabyte events from the rest. For Example,
```
tags : ["Yugabyte"]
```

2. Configuring the output section:
   1. Locate "output" in the filebeat.yml file, then add the following parameters. 
   2. Disable Elasticsearch output by commenting it out. 
   3. Enable Logstash output by uncommenting the Logstash section. For more information, see https://www.elastic.co/guide/en/beats/filebeat/current/logstash-output.html#logstash-output
   4. For example:
	```
	output.logstash:
      hosts: [<host>:<port>]
	```
   5. The hosts option specifies the Logstash server and the port (5001) where Logstash is configured to listen for incoming Beats connections. 
   6. You can set any port number except 5044, 5141, and 5000 (as these are currently reserved in Guardium v11.3 and v11.4 ).

## 5. Configuring Syslogs to push logs to Guardium
## a. Syslogs configuration:
To make the Logstash able to process the data collected by syslogs, we need to configure available syslog utility.
The example is based on `rsyslog` utility available in many versions of the Linux distributions.

### Procedure: 
1. Create a file with name `yugabyte_syslog.conf` in the `/etc/rsyslog.d/` directory with the 
content below in the snippet and change give the values of `target` and `port`,
    ```
    module(load="imfile")
    ruleset(name="imfile_to_gdp") {
        action(type="omfwd"
            protocol="tcp"
            target="<target_host>"
            port="<target_port>")
        stop
    }
    
    input(
        type="imfile"
        file="/path/to/logs/directory/postgresql-*.log"
        # Keep the value of tag below as same as here,
        tag="syslog"
        ruleset="imfile_to_gdp"
        startmsg.regex="^([0-9.]+).*(LOG|ERROR|FATAL):.*"
    )
    
    input(
        type="imfile"
        file="/path/to/logs/directory/yb-tserver.*.*.log.*.*"
        # Keep the value of tag below as same as here,
        tag="syslog" 
        ruleset="imfile_to_gdp"
    )
    ```
    this is the configurations that will read the logs from the Yugabyte log directory path and send the
    syslogs to the provided host (target_port) at the provided port (target_port). This will also process
    the multiline audit logs. <br/><br/>

2. Include this file in the main `rsyslog` configurations file.
   1. Open the file `/etc/rsyslog.conf`.
   2. Append the below line at the end.
      ```
      $IncludeConfig /etc/rsyslog.d/yugabyte_syslog.conf.conf
      ```
3. Restart the `rsyslog` utility.
    ```text
    systemctl restart rsyslog
    ```

## 6. Configuring the Yugabyte filters in Guardium

The Guardium universal connector is the Guardium entry point for native audit logs. The universal connector identifies and parses received events, and then converts them to a standard Guardium format. The output of the universal connector is forwarded to the Guardium sniffer on the collector, for policy and auditing enforcements. Configure Guardium to read the native audit logs by customizing the Yugabyte template.

### Limitations
When UC will start collecting data, It may show two STAP statuses in the pattern "postgres_<server-host-name>" and "cassandra_<server-host-name>" based on what type of logs it is collecting.

### Before you begin
* Configure the policies you require. See [policies](/docs/#policies) for more information.
* You must have permission for the S-Tap Management role. The admin user includes this role by default.
* Download the [yugabyte-logstash-offline-plugins-1.0.0.zip](YugabytedbOverFilebeatPackage/YugabyteDB/yugabytedb-logstash-offline-plugins-1.0.0.zip) 
plug-in. This is not necessary for Guardium Data Protection v12.0 and later.

# Procedure

1. On the collector, go to Setup > Tools and Views > Configure Universal Connector.
2. First enable the Universal Guardium connector if it is disabled.
3. Click Upload File and select the offline [yugabyte-logstash-offline-plugins-1.0.0.zip](YugabytedbOverFilebeatPackage/YugabyteDB/yugabytedb-logstash-offline-plugins-1.0.0.zip) plug-in. After it is uploaded, click OK.
4. Click the Plus sign to open the Connector Configuration dialog box.
5. Type a name in the Connector name field.
6. Update the input section,
   1. To collect data over Filebeat add the details from [yugabyteFilebeat.conf](yugabyteFilebeat.conf) 
      file input section, omitting the keyword "input{" at the beginning and its corresponding "}" at the end.
   2. To collect data over Syslogs add the details from [yugabyteSyslog.conf](yugabyteSyslog.conf)
      file input section, omitting the keyword "input{" at the beginning and its corresponding "}" at the end.
7. Update the filter section,
   1. To filter the data collected from the Filebeat, add the details from the [yugabyteFilebeat.conf](yugabyteFilebeat.conf)  
      file filter section, omitting the keyword "filter{" at the beginning and its corresponding "}" at the end.
   2. To filter the data collected from the Syslogs, add the details from the [yugabyteSyslog.conf](yugabyteSyslog.conf)  
      file filter section, omitting the keyword "filter{" at the beginning and its corresponding "}" at the end.
8. The "type" fields should match in the input and the filter configuration section. This field should be unique for
every individual connector added.
9. Click Save. Guardium validates the new connector, and enables the universal connector if it was disabled. 
After it is validated, it appears in the Configure Universal Connector page.

## 5. Configuring the Yugabyte filters in Guardium Insights
To configure this plug-in for Guardium Insights, follow [this guide.](/docs/Guardium%20Insights/3.2.x/UC_Configuration_GI.md)
For the input configuration step, refer to the [Filebeat section](/docs/Guardium%20Insights/3.2.x/UC_Configuration_GI.md#Filebeat-input-plug-in-configuration).
