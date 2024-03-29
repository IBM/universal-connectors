# ScyllaDB-Guardium Logstash filter plug-in

### Meet ScyllaDB
* Tested versions: v2023.1
* Environment: On-premise
* Supported inputs: Syslog (push)
* Supported Guardium versions:
  * Guardium Data Protection: 11.4 and later

This [Logstash](https://github.com/elastic/logstash) filter plug-in for IBM Security Guardium the universal connector parses events and messages from the IBM Cloud PostgresSQL audit log into a [Guardium record](https://github.com/IBM/universal-connectors/blob/main/common/src/main/java/com/ibm/guardium/universalconnector/commons/structures/Record.java) instance (which is a standard structure made out of several parts). The information is then sent over to Guardium. Guardium records the accessor (the person who tried to access the data), the session, data, and exceptions. If there are no errors, the data contains SQL commands are not parsed by this plug-in but rather forwarded to Guardium to do the SQL parsing.

The plug-in is free and open-source (Apache 2.0). You can use it as a starting point to develop additional filter plug-ins for the Guardium universal connector.

## 1. ScyllaDB Enterprise Environment Setup and Installation
ScyllaDb Enterprise Support multiple Linux Distributed system i.e. Ubuntu, Debian, CentOS, RHEL.

`Note`:
* The recommended OS for ScyllaDB Enterprise is Ubuntu 22.04.
* The recommended instance types are i3, i3en, and i4i.

### Procedure:
#### Creating a Ubuntu 22.04 platform for ScyllaDB Enterprise:
1. Log in to AWS account, search for EC2 and then go to instances(running).
2. Click Launch Instances button at top right corner.
3. Give the name for the instance.
4. Choose Ubuntu 22.04.
5. Choose instance type. (`Example- i4i.large`).
6. Click on `Create new key pair` -- choose .pem radio button.
7. Provide keypair name and click on `Create key pair` (.pem).
8. Leave rest of the setting as it is.
9. Click on Launch instance.

#### Adding Port in Inbound Rule of EC2 Instance :
1. Once your instance is ready(`2/2 checks passed`), then open your created Instance
2. Scroll down and go to Security section and there in inbound rule section click on any one of security groups option.
3. It will go to the Security groups section of the selected group then click on `Security group ID`.
4. Security group id will open, then click on `Edit inbound rule`.
5. Click on `Add rule` button and then add`9042` port and select `0.0.0.0/0` in source column.
6. Click on `Save rules`.

### Downloading the ScyllaDB Enterprise Edition:
1. First connect to your EC2 instance by using `Connect` option mention inside your instance. 
2. Install a repo file and add the ScyllaDB APT repository to your system by using below commands.
```
sudo gpg --homedir /tmp --no-default-keyring --keyring /etc/apt/keyrings/scylladb.gpg --keyserver hkp://keyserver.ubuntu.com:80 --recv-keys d0a112e067426ab2
```
``` 
sudo curl -L --output /etc/apt/sources.list.d/scylla.list http://downloads.scylladb.com/deb/ubuntu/scylla-2023.1.list
```

### Environment Setup:
1. Install packages.
```
sudo apt-get update
```
```
sudo apt-get install -y scylla-enterprise
```
2. Run below commands to set Java to 1.8 release.
 ```
sudo apt-get update
```
```
sudo apt-get install -y openjdk-8-jre-headless
```
```
sudo update-java-alternatives --jre-headless -s java-1.8.0-openjdk-amd64
```
3. Install ScyllaDB Enterprise official image.  
```
sudo apt-get install -y scylla-enterprise-machine-image
```
4. Set ScyllaDb to Developer mode.
```
sudo scylla_dev_mode_setup --developer-mode 1
```
5. Run the scylla_setup script to tune the system settings.
```
sudo scylla_setup
```
6. `scylla_setup` command invokes a set of scripts to configure several operating system settings, answer `No` for below mentioned options. For remaining options select `Yes`.
   * Enable XFS online discard?
   The default (Yes) asks the disk to recycle SSD cells as soon as files are deleted. 4.18+ kernel recommended for this option.
      * [YES/no]`no`
      * Please select unmounted disks from the following list: /dev/nvme1n1
   type 'cancel' to cancel RAID/XFS setup.
   type 'done' to finish selection. Selected: `cancel`
   * The clocksource is the physical device that Linux uses to take time measurements. In most cases Linux chooses the fastest available clocksource device as long as it is accurate. In some situations, however, Linux errs in the side of caution and does not choose the fastest available clocksource despite it being accurate enough. If you know your hardwares fast clocksource is stable enough, choose "yes" here. The safest is the choose "no" (the default)
   Yes - enforce clocksource setting. No - keep current configuration.
      * [Yes/No]`no`
   * Do you want to enable fstrim service?
   Yes - runs fstrim on your SSD. No - skip this step.
      * [Yes/No]`no`
   * Do you want to configure rsyslog to send log to a remote repository?
   Answer yes to setup rsyslog to a remote server, Answer no to do nothing.
      * [Yes/No]`no`
      
7. Run ScyllaDB as a service.
```
sudo systemctl start scylla-server
```
8. Check Status of Scylla server and make sure it is active.
```
systemctl status scylla-server.service
```
9. Run nodetool to check status of node.
```
nodetool status
```

## 2. Connecting to CQL Shell
1. Connect to CQL shell.
``` 
cqlsh or cqlsh -u cassandra -p cassandra
```
2. To exit from CQL shell run `EXIT` command in CQL shell. 

`Note`: `cassandra` is superuser name and password.

## 3. Configuring and Enabling audit logs
1. Open `scylla.yaml` file by using below commands.
```
sudo nano /etc/scylla/scylla.yaml
```
2. Uncomment below property in scylla.yaml file.
```
data_file_directories:
   - /var/lib/scylla/data
commitlog_directory: /var/lib/scylla/commitlog
```
3. Enable Communication by editing below properties:

   *  listen_address: `<EC2_instance_Private_Ipv4_ address>`
   * rpc_address: ` 0.0.0.0`
   * Uncomment the `broadcast_rpc_address` and provide `EC2 instance Private Ipv4 address`.
4. Enable Authentication: 
   * Uncomment and Edit the below mentioned parameter in `scylla.yaml` file to change the  authenticator parameter from `AllowAllAuthenticator` to `PasswordAuthenticator`.
      * authenticator: PasswordAuthenticator
5. To save the changes press `Ctrl+x`, then press `Y` and then press `Enter`.
6. Restart the server.
```
sudo systemctl restart scylla-server
```
7. Run a repair on the system_auth keyspace on all the nodes in the cluster.
```
nodetool repair system_auth
```
8. Enable Authorization: 
   * Edit `scylla.yaml` to change the authorizer parameter from `AllowAllAuthorizer` to `CassandraAuthorizer`.
	   * authorizer: CassandraAuthorizer.
9. To save the changes press `Ctrl+x`, then press `Y` and then press `Enter`.
10. Restart the server.
``` 
sudo systemctl restart scylla-server
```
11. Enable Audit log: To enable the audit log uncomment and update the below mentioned property in `scylla.yaml` file.
```
audit: “syslog“
audit_categories: "DML,DCL,DDL,AUTH,ADMIN,QUERY“
audit_tables: "“
audit_keyspaces: "<keyspace_name1>,<keyspace_name2>…….”
```
`Note`: Only those operations will be audited which are executed in above mentioned keyspaces in `scylla.yaml` file.

12. To save the changes press `Ctrl+x`, then press `Y` and then press `Enter`.
13. Restart the server.
   ```
   sudo systemctl restart scylla-server
   ```
`Note`: Once you enable the authentication and authorization in `scylla.yaml` file then to connect to CQL shell you have to only use `cqlsh -u cassandra -p cassandra` command only. `cqlsh` command will not work now.

## 4. Configuring the Syslog
1. We set up the rsyslog service to run in server mode, to do this we need to configure rsyslog.conf.
```
sudo nano /etc/rsyslog.conf 
``` 
2. Make sure to set off the below mentioned option, by default it will be on.
```
#Filter duplicated messages
$RepeatedMsgReduction off
```
3. Comment below mentioned option.
```
#$ActionFileDefaultTemplate RSYSLOG_TraditionalFileFormat
```
4. Then add this new template just below above commented option.
```
$template RFC3339Format,"%timestamp:::date-rfc3339% %hostname% %syslogtag%%msg%"
$ActionFileDefaultTemplate RFC3339Format
```
5. Let's make a template that tells where you want to send your audit logs. Add the below template at the end of the rsyslog.conf file.  
```
#Send logs to client server
$PreserveFQDN on
*.* @<Client-ip>:5514;RFC3339Format
$ActionQueueFileName queue
$ActionQueueMaxDiskSpace 1g
$ActionQueueType LinkedList
$ActionResumeRetryCount -1
```
6. To save the changes press `Ctrl+x`, then press `Y` and then press `Enter`.
7. Then restart Rsyslog to apply the changes.
```
sudo systemctl restart rsyslog
``` 
8. Verify the Rsyslog status with the following command.
```
sudo systemctl status rsyslog
```
9. Restart the server.
```
sudo systemctl restart scylla-server
```

## 5. Limitations
1. The following important fields couldn't be mapped with Scylladb audit logs:
    * Source Program
    * Client Hostname
2. Scylladb will not capture CQL error and syntax error audit logs.
3. Authorization and Authentication failure error logs will be captured.
4. Identical duplicate logs are coming on `Successful Login` and while executing `USE <keyspacename>`, `CREATE`, `ALTER`, `DROP` commands.

## 6. Configuring the scylladb filter in Guardium
The Guardium universal connector is the Guardium entry point for native audit logs. The Guardium universal connector identifies and parses the received events, and converts them to a standard Guardium format. The output of the Guardium universal connector is forwarded to the Guardium sniffer on the collector, for policy and auditing enforcements. Configure Guardium to read the native audit logs by customizing the scylladb template.
### Before you begin:
1. Configure the policies you require. See [policies](https://github.com/IBM/universal-connectors/#policies) for more information.
2. You must have permission for the S-Tap Management role. The admin user includes this role by default.
3. Download the [guardium_logstash-offline-plugin-scylladb.zip](ScyllaDBOverSyslogPackage/guardium_logstash-offline-plugin-scylladb.zip) plug-in.
### Procedure:
1. On the collector, go to Setup > Tools and Views > Configure Universal Connector.
2. Enable the connector if it is already disabled, before proceeding uploading of the UC.
3. Click Upload File and select the offline [guardium_logstash-offline-plugin-scylladb.zip](ScyllaDBOverSyslogPackage/guardium_logstash-offline-plugin-scylladb.zip) plug-in. After it is uploaded, click OK.
4. Click the Plus sign to open the Connector Configuration dialog box.
5. Type a name in the Connector name field.
6. Update the input section to add the details from [Scylladb.conf](Scylladb.conf) file's input part, omitting the keyword "input{" at the beginning and its corresponding "}" at the end.
7. Update the filter section to add the details from [Scylladb.conf](Scylladb.conf) file's filter part, omitting the keyword "filter{" at the beginning and its corresponding "}" at the end.
8. The "type" field should match in the input and filter configuration section. This field should be unique for every individual connector added.
9. Click Save. Guardium validates the new connector, and enables the universal connector if it was disabled. After it is validated, it appears in the Configure Universal Connector page.
