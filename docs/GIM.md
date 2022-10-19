# Configuring GIM to handle Filebeat and Syslog on MongoDB

You can use the GIM client, which is installed on your database,
to manage native log audit forwarding by using Filebeat and Syslog.

•	**Configuring the GIM client to handle Filebeat and Syslog on MongoDB**

You can configure the Filebeat and Syslog native log audit on MongoDB data sources
by installing the GIM client on the MongoDB, and configuring the GIM parameters in the Setup by Client page.

•	**Installing the GIM client by using shell on Unix, and configuring Filebeat and Syslog on MongoDB by using GIM**

You can install the ```GIM BUNDLE-GUC``` by script on the Unix database server,
and then continue by configuring the Guardium universal connector in the Set up by Client page.

•	**Creating a filter for MongoDB activity audit log by using the API**

The groups in the API `schedule_generate_mongo_filter_job` are used to continuously update
the value of the GIM parameter ```GUC_AUDIT_LOG_FILTER```. The GIM parameter specifies which events to include
in the data that is forwarded to the Guardium universal connector. This task describes creating and
populating the groups, and running the API.

## Configuring the GIM client to handle Filebeat and Syslog on MongoDB

You can configure the Filebeat and Syslog native log audit on MongoDB data sources by installing the
 GIM client on the MongoDB, and configuring the GIM parameters in the Setup by Client page.
Before you begin

•	Obtain the GIM client from either [Fix Central](https://www.ibm.com/support/fixcentral/), or your
Guardium representative. The bundle name has the format BUNDLE-GUC.

•	The GIM bundles must be uploaded and imported to the Guardium system. See [Uploading and
importing GIM modules](https://www.ibm.com/docs/en/guardium/11.5?topic=gim-uploading-importing-modules).

About this task

You can either update the configuration immediately and restart the database, or save the configuration to a
temporary mongod.conf file on the database. The parameter ```GUC_RESTART_DB``` controls the restart.
See the [parameter description](https://www.ibm.com/docs/en/guardium/11.4?topic=mongodb-configuring-gim-client-handle-filebeat-syslog#task_btt_z11_2nb__GUC_RESTART_DB).
(You can also install the GIM bundle on Linux servers by using the shell.
See Installing the GIM client by using shell on Unix, and configuring Filebeat and Syslog on MongoDB by using GIM below.)

Procedure

1. Install the GIM client on your data sources.
For UNIX, use GUC GIM package guard-bundle-GUC-11.4.0.0_r111103_v11_4_1-rhel-8-linux-x86_64.gim.
 For Windows, the package name is guard-GUC-11.4_r110400183_1-x86_x64.gim
 (These package names are examples for version 11.4. More package names can be found on [Fix Central](https://www.ibm.com/support/fixcentral/)). For more information, see [Installing the GIM client on a Windows server](https://www.ibm.com/docs/en/guardium/11.5?topic=servers-installing-gim-client-windows-server), and [Installing the GIM client on a UNIX server](https://www.ibm.com/docs/en/guardium/11.5?topic=servers-installing-gim-client-unix-server).

2. In the Setup by Client page, install the Guardium universal connector bundle on the data sources.
(See the description of the Setup by Client page options in [Set up by Client](https://www.ibm.com/docs/en/guardium/11.5?topic=gim-set-up-by-client).)

a.	In the ```Choose bundle``` section of the Setup by Client page,
select the bundle: ```BUNDLE-GUC (<release number>)```. To see bundles for Windows, leave the ```“show only bundles”``` checkbox cleared.

b.	Set the ```GUC_GUARD_HOSTS``` parameter, which is required for installation.

  **Note: If you want to install Filebeat on your data source, set ```GUC_INSTALL_FILEBEAT``` to ```1```
  during installation, but not during the update.**

  **Note: You can update optional configuration parameters from
  paragraph section ```GIM GUC Client configuration update``` during GUC client installation or later,
   GUC client during configuration update.**

  **Note: If you installed Filebeat using ```PARAMETER GUC_INSTALL_FILEBEAT=1```,
  all changes of Filebeat-related parameters, marked in the table as “filebeat only” causes the Filebeat service to restart.
  If you don’t install Filebeat, all changes of Filebeat-related parameters require manual Filebeat service restart.**


  **Table 1. GIM parameters for Guardium universal connector bundle installation**


| Parameter                 | Description                                                                                                                               |
| ----------                |  -----------                                                                                                                              |
|GUC_GUARD_HOSTS            | IPv4: Comma-delimited string of ```host_port```. For Syslog, only one pair is allowed.IPv6: Comma-delimited string of ```[host]_port```.  |
|                           |     To connect to a Guardium system configured with IPv6, configure the rsyslog on the MongoDB server to send by using a TCP port.            |
|                           |   For example, ```programname, isequal, "mongod" @@[2620:1f7:807:a080:956:9300:0:1c]:5000``` is a required parameter for installation.      |
|GUC_INSTALL_FILEBEAT       |	Determines whether the GIM bundle needs to install Filebeat on your database or not. Valid values:                                        |
|                                	0: Do not install Filebeat. This is the default value.
|                                	Install Filebeat on your database.
|                                Optional. Can be set only during installation, not during update.

  c.	Click Install.

**Note: You must click Install before configuring the parameters in the next step.**

d. In the ```Choose parameters``` section, modify the optional client parameters by clicking the ```+```.

**Table 2. GIM Client parameters Guardium universal connector**


| Parameter               | Description                                                |
| ----------              |  -----------                                               |
|GUC_AUDIT_LOG_PATH       | Filebeat only. Optional. Path to the database audit logs.<br> Default value: /var/lib/mongo/auditLog.json|
|GUC_AUDIT_LOG_FILTER     |Optional. Filters specific events in the database. See [schedule_generate_mongo_filter_job](https://www.ibm.com/docs/en/guardium/11.5?topic=reference-schedule-generate-mongo-filter-job).To use this parameter to filter events, ```GUC_RESTART_DB``` must be set to ```1```. |
|GUC_DATA_SHIPPER         | Data shipper that passes the data to the Guardium universal connector. Valid values:<br> 	0: Filebeat <br> 	1: Syslog<br><br> Default: 0 |
|GUC_DB_TYPE              |  Optional. The database type.                              |
|GUC_DEBUG                |    Enables the GIM debug. Valid values:<br> 0: Off<br>1: On<br><br>Default: 0 |
|GUC_ENABLED	            |   Controlling GUC model.<br>Valid values:<br><br>0: Off<br><br>1: On<br><br>                             2:Restart GUC<br><br>Default: 1|
| GUC_ENABLE_LOADBALANCE  |   	Filebeat only. Enables load balancing.<br> Valid values:<br><br>0: Traffic load is balanced between all the defined hosts.<br><br> 1: Data is sent randomly to one of the defined hosts. The other hosts are used as a failover hosts.<br><br>  Default: 0|
|GUC_EXTRA_DETAILS	      |      Optional. Comments that you can add to the bundle description, which is viewed in the GIM GUI|        |                
|GUC_GUARD_HOSTS	        |     IPv4: Comma-delimited string of host_port. For Syslog, only one pair  is allowed. <br><br> IPv6: Comma-delimited string of [host]_port. To connect to a Guardium system configured with IPv6, configure the rsyslog on the MongoDB server to send by using a TCP port. For example,<br> ```programname, isequal, "mongod" @@[2620:1f7:807:a080:956:9300:0:1c]:5000```|
| GUC_RESTART_DB          | Whether the database is restarted during the GUC-Bundle installation or not. <br> Valid values:<br> 0: Save the GIM configuration that you just entered to an example file /etc/mongod.conf.guardium_example. <timestamp> (by default in this directory),           without restarting the database.<br> 1: Update the working mongod.conf, and then restart the database.  Saves the previous working file as mongod.conf. <timestamp> <br><br>  Default: 0|
|GUC_SYSLOG_PROTOCOL      | Syslog only. Specifies the communication protocol to send the data. Valid values:<br> 0: UDP<br> 1: TCP<br><br> Default: 0 |                                  
| GUC_DATASOURCE_TAG      |Filebeat only. This tag is added to every event that is sent from Filebeat, and it identifies the event's data source. This should be the same tag that is defined in the Universal Connector configuration page. |



e.	Click Install.

After completing this procedure, see enabling the Guardium universal connector on collectors

## Installing the GIM client by using shell on Unix, and configuring Filebeat and Syslog on MongoDB by using GIM

You can install the ```GIM BUNDLE-GUC``` by script on the Unix database server, and then continue by configuring the Guardium universal connector in the Set up by Client page.
Before you begin
•	Obtain the correct Guardium universal connector installer script, from either [Fix Central](https://www.ibm.com/support/fixcentral/), or your Guardium representative. The script name identifies the database server operating system. The GUC shell installation package name is in the format: ```guard-bundle-GUC-11.3.0.0_r109603_v11_3_1-rhel-7-linux-x86_64.gim.sh```, where the first three numbers are the release number, followed by the revision number, in this example r109603.

### Procedure

1.	Log in to the database server as root user and copy the GIM bundle for Guardium universal connector to ```/tmp```.
2.	Change the directory to ```/tmp``` folder.
Run this command, for example:
```
# ../guard-bundle-GUC-11.4.0.0_r111103_v11_4_1-rhel-8-linux-x86_64.gim.sh -- --dir /usr/local/IBM --tapip  9.46.90.173 --sqlguardip 9.55.247.248 --perl /usr/bin --guc_guard_hosts 9.55.247.248_5044 -q
Verifying archive integrity... All good.
Uncompressing Guard BUNDLE-GUC Installer....
Installing modules ....
NOTE : The following modules stopped running after installation:
GUC   DOWN
Installation completed successfully
```
3.	In the Guardium UI, go to ```Manage``` > ```Module Installation``` > ```Set up by Client```, and configure the connector as relevant:

**Table 1. GIM Client parameters Guardium universal connector**

| Parameter               | Description                                                |
| ----------              |  -----------                                               |
|GUC_AUDIT_LOG_PATH       | Filebeat only. Optional. Path to the database audit logs.<br> Default value: /var/lib/mongo/auditLog.json|
|GUC_AUDIT_LOG_FILTER     |Optional. Filters specific events in the database. See [schedule_generate_mongo_filter_job](https://www.ibm.com/docs/en/guardium/11.5?topic=reference-schedule-generate-mongo-filter-job).To use this parameter to filter events, ```GUC_RESTART_DB``` must be set to ```1```. |
|GUC_DATA_SHIPPER         | Data shipper that passes the data to the Guardium universal connector. Valid values:<br> 	0: Filebeat <br> 	1: Syslog<br><br> Default: 0 |
|GUC_DB_TYPE              |  Optional. The database type.                              |
|GUC_DEBUG                |    Enables the GIM debug. Valid values:<br> 0: Off<br>1: On<br><br>Default: 0 |
|GUC_ENABLED	            |   Controlling GUC model.<br>Valid values:<br><br>0: Off<br><br>1: On<br><br>                             2:Restart GUC<br><br>Default: 1|
| GUC_ENABLE_LOADBALANCE  |   	Filebeat only. Enables load balancing.<br> Valid values:<br><br>0: Traffic load is balanced between all the defined hosts.<br><br> 1: Data is sent randomly to one of the defined hosts. The other hosts are used as a failover hosts.<br><br>  Default: 0|
|GUC_EXTRA_DETAILS	      |      Optional. Comments that you can add to the bundle description, which is viewed in the GIM GUI|        |                
|GUC_GUARD_HOSTS	        |     IPv4: Comma-delimited string of host_port. For Syslog, only one pair  is allowed. <br><br> IPv6: Comma-delimited string of [host]_port. To connect to a Guardium system configured with IPv6, configure the rsyslog on the MongoDB server to send by using a TCP port. For example,<br> ```programname, isequal, "mongod" @@[2620:1f7:807:a080:956:9300:0:1c]:5000```|
| GUC_RESTART_DB          | Whether the database is restarted during the GUC-Bundle installation or not. <br> Valid values:<br> 0: Save the GIM configuration that you just entered to an example file /etc/mongod.conf.guardium_example. < timestamp >   (remove extra spaces in timestamp brackets. By default in this directory),           without restarting the database.<br> 1: Update the working mongod.conf, and then restart the database.  Saves the previous working file as mongod.conf. < timestamp >(remove extra spaces in timestamp brackets) <br><br>  Default: 0|
|GUC_SYSLOG_PROTOCOL      | Syslog only. Specifies the communication protocol to send the data. Valid values:<br> 0: UDP<br> 1: TCP<br><br> Default: 0 |                                  
| GUC_DATASOURCE_TAG      |Filebeat only. This tag is added to every event that is sent from Filebeat, and it identifies the event's data source. This should be the same tag that is defined in the Universal Connector configuration page. |

4.	Repeat step 3 to configure more connectors.


## Creating a filter for MongoDB activity audit log by using the API

The groups in the API ```schedule_generate_mongo_filter_job``` are used to continuously update the value of the GIM parameter ```GUC_AUDIT_LOG_FILTER```.

The GIM parameter specifies which events to include in the data that is forwarded to the Guardium universal connector. This task describes creating and populating the groups, and running the API.

### About this task

Each group specifies either a command, user group, or object. Each activity on the MongoDB is compared to these groups. If an activity matches a definition in any one of the groups, the specific activity is forwarded to the Guardium universal connector. Each group is evaluated independently. A match to any one group means that the activity is forwarded.

**Note: To apply an updated ```GUC_AUDIT_LOG_FILTER``` parameter to a working mongo configuration, you need to restart the mongo database. Set GIM GUC bundle configuration parameter ```GUC_RESTART_DB=1``` and run an install update via the GIM GUI.**

### Procedure

1.	Create groups for the API parameter ```usersGroupId```.

a.	Go to ```Setup``` > ```Tools and Views``` > ```Group Builder```.

b.	Click the plus icon to open the Create new group dialog.

c.	Give the group an identifiable group name, like API_UsersGroup-1.

d.	For Application type, select Classifier.

e.	For Group type, select a group type with tuple of two fields in the format ```<field-1>/<field-2>```. For example, ```Server IP/DB User```. You can choose any Group type that has the format
``` <field-1>/<field-2>```.
2.	Add members to the group.

a.	In the Members tab, click the plus icon  to open the Add member dialog.

b.	Enter the database name in the first field, and the DB username in the second field, and click ```OK```. Repeat for more members.

3.	Click Save.

4.	Repeat steps 1, 2, and 3 to create more user groups.

5.	Identify the group IDs. From the CLI command line, run the command ```grdapi list_group_by_desc desc="<group name>"``` once for each of the groups you created in step 1-3.

6.	Create groups for the API parameter ```commandGroupId```.

a.	Repeat step 1 using names that indicate command groups, for example, ```API_commands_1```, ```API_commands_2```.

b.	Add members to the new groups. Enter the database name in the first field, and the conman name in the second field.

7.	Identify the group IDs. From the CLI command line, run the command ```grdapi list_group_by_desc desc="<group name>"``` once for each of the groups you created in step 6.

8.	Create groups for the API parameter ```objectsGroupId```.

a.	Repeat step 1 using names that indicate command groups, for example, ```API_objects_1```, ```API_objects_2```.

b.	Add members to the new groups. Enter the database name in the first field, and the object name in the second field.

9.	Identify the group IDs. From the CLI command line, run the command ```grdapi list_group_by_desc desc="<group name>"``` once for each of the groups you created in step 8.

10.	Run the command ```grdapi schedule_generate_mongo_filter_job```. For example, with the groups IDs

```
grdapi schedule_generate_mongo_filter_job clientIp="n.n.n.n"
usersGroupId="100,101" objectsGroupId="104,105"
commandGroupId="201,202" cronStr="0 15 0 * * ? *"
```

11.	In the Guardium GUI, open the ```Scheduled Jobs``` page. The new job MongoFilterGeneratorJob appears in waiting status.

### Results
The audit log is filtered for anything that does not match the group definitions, by default, starting at 00:15 the following night.

See also: [Using the group builder](https://www.ibm.com/docs/en/guardium/11.5?topic=groups-using-group-builder)

Related references:

[list_group_members_by_desc](https://www.ibm.com/docs/en/guardium/11.5?topic=reference-list-group-members-by-desc)

[schedule_generate_mongo_filter_job](https://www.ibm.com/docs/en/guardium/11.5?topic=reference-list-group-members-by-desc)
