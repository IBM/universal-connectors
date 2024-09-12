# logstash-filter-intersystems-iris-guardium

### Meet IntersystemsIRIS
* Tested versions: 2023.1
* Environment: On-premise
* Supported inputs: JDBC (pull)
* Supported Guardium versions:
  * Guardium Data Protection: 11.4 and above
  * Guardium Insights: 3.4.1
  
This is a [Logstash](https://github.com/elastic/logstash) filter plug-in for the universal connector that is featured in IBM Security Guardium. It parses events and messages from the intersystems-iris audit log into a [Guardium record](https://github.com/IBM/universal-connectors/blob/main/common/src/main/java/com/ibm/guardium/universalconnector/commons/structures/Record.java) instance (which is a standard structure made out of several parts). The information is then sent over to Guardium. Guardium records include the accessor (the person who tried to access the data), the session, data, and exceptions. If there are no errors, the 
data contains details about the query "construct". The construct details the main action (verb) and collections (objects) involved.

The plug-in is free and open-source (Apache 2.0). It can be used as a starting point to develop additional filter plug-ins for the Guardium universal connector.

## 1. IRIS Environment Setup and Installation
This plug-in was developed and tested against an Ubuntu instance on AWS EC2, but any instance should work.
### Create ubuntu Instance using AWS EC2 instance

#### Procedure:
1. Click Launch Instances button at top right corner
2. Give the name for the instance.
3. Choose Ubuntu.
4. Click on new pair -- choose .pem radio button
5. Click on create the new key pair (.pem)
6. Now Launch the instance.

### Downloading the IRIS Community Edition

### Procedure:
1. The InterSystems IRIS Community Edition is a free version, from the InterSystems development community. Download the IRIS installation file from the [link](https://evaluation.intersystems.com/Eval/community-download).
2. If you are not a existing user, use the Register link to Register as a new user. 
3. Provide the required details for new user registration.
4. Select the community edition.
5. Select the below options in Download page
    - Choose a Product - InterSystems IRIS Community
    - Choose a Platform - Ubuntu
    - Choose a Version - 2023.1 
6. Select I agree to Accept the policy.
7. Click on the Download InterSystems IRIS button to download.

### Transferring the IRIS Installation file using Winscp

#### Procedure:
1. Start a new session from Winscp.
2. Set File Protocal to SFTP. 
3. Provide the username and password (Enabled Password Authentication).
4. Go to Advanced settings. Select SSH > Authentication and upload the private key ​and then. 
5. Click on "ok" button.
6. Click on login button.
7. Now drag and drop the intersystem IRIS community edition file from our local to instance. 
    
### Extracting the Installation Kit

#### Procedure:

Execute the below command.

```
# mkdir /tmp/iriskit 
# chmod og+rx /tmp/iriskit 
# umask 022 
# gunzip -c <username>/<downloaded_iris_filename>| ( cd /tmp/iriskit ; tar xf - )
Ex : # gunzip -c /home/ubuntu/IRIS_Community-2023.1.0.229.0-lnxubuntu2204x64.tar.gz | ( cd /tmp/iriskit ; tar xf - )

```
### Installation and prompts during installation

#### Procedure:
```
1. Go to your IRIS_Community directory and run `sudo ./irisinstall` command to install iris. 
2. Installation Prompts :
   Enter a destination directory for the new instance.
        Directory: /etc/iris
        Directory '/etc/iris' does not exist.
        Do you want to create it <Yes>?  Yes
3. Select installation type.
    1) Development - Install InterSystems IRIS server and all language bindings
    2) Server only - Install InterSystems IRIS server
    3) Custom
   Setup type <1>? 1
4. How restrictive do you want the initial Security settings to be?
"Minimal" is the least restrictive, "Locked Down" is the most secure.
    1) Minimal
    2) Normal
    3) Locked Down
   Initial Security settings <1>? 1
5. What group should be allowed to start and stop this instance? root
6. Do you want to install IRIS Unicode support <Yes>?  yes
7. InterSystems IRIS did not detect a license key file
   Do you want to enter a license key <No>?  No
8. After the installation it will prompt to review the installation options and confirm  to install
```
### Web based InterSystems IRIS Management Portal

#### Procedure:

1. InterSystems IRIS® enables you to perform system administration and management tasks via a web application, the InterSystems Management Portal.

2. Access the Management Portal: http://machine:port/csp/sys/UtilHome.csp
    - Where machine is the IP address of your system (such as localhost) and port is the port number of the web server configured for use by InterSystems IRIS. 
    - For example, the default location on the local server of a single installation of InterSystems IRIS is `http://<ec2 instance ip>:52773/csp/sys/UtilHome.csp`(where 52773 is the default InterSystems IRIS web service server port number).

Note: If we are not able to access the portal, Edit the inbound rules in EC2 instance by adding port 52773.

3. There are two conditions depends on the authentication settings :
    - Unauthenticated Access Only : Neither requires nor accepts a username and password.
    - Authenticated Access : Requires a username and password
4. User definition and granting privileges
```
1. Go to the (System Administration > Security > Users)
2. Choose an Admin user account 
3. Enter new password and confirm 
4. Tick the user enabled.
5. Select any Namespace from the Start up Namespace.
6. For user Admin  alter values for the properties and give all the persmissions under account Properties section : Roles, SQL Privileges, SQL Tables , SQL Views, SQL Procedures
7. In Admin user's details page, We can edit the  name, password and edit the required permissions needed and Assign.
``` 
5. Enabling this login page with Username and Password :
    - You can look up the Authentication allowed settings on the Web Applications page (System Administration > Security > Applications > Web Applications) by clicking Edit in the `/csp/sys` application row and tick password check box.
    - Enable Password in same manner for `/csp/sys/sec`(for Audit logs) , `/csp/sys/exp` (Executing Queries)  

## 2. Enabling audit logs

1. To turn on auditing, on the Auditing menu (System Administration > Security > Auditing > Enable Auditing), select Enable Auditing.
2. Click on Perform Action Now button.
3. Configure audit events
    - System audit events are predefined events that are available for auditing by default. General information about them appears in the table on the System Audit Events page (System Administration > Security > Auditing > Configure System Events
   - Enable required Event name by change status: 
       - Dynamic statement SQL :  yes 
       - Login failure :  yes
       - XDBCStatement : yes

## 3. Executing the SQL queries in management portal
    
   1. Go to System Explorer>SQL>Go 
   2. In the query panel Write the query. For example: SELECT * FROM Sample.Employee.
   3. Click on the execute to execute the written query.

## 4. Viewing the audit

   1. Select View Audit Database from the Auditing menu, which displays the View Audit Database page (System Administration > Security > Auditing > View Audit Database).
   2. To refine the search, use the fields in this page’s left pane and select the Search button at the bottom of the pane. (Select Reset Values at the bottom of the pane to restore the default values.

## 5. Connecting to the InterSystems IRIS  using the Terminal

    1. From Management portal Enable the password to login with Username , go to System   Administration> Security>Services click  on %Service_Terminal   and tick password check box and click on save button to save the changes.
    2. To interacting with InterSystems IRIS shell,  we need to execute the following command : iris session IRIS
    3. It prompts node with Host Name and Instance Name EX: Node: ip-172-31-33-219, Instance: IRIS with Username and Password.
    4. Enter the Username and Password to enter in to the USER namespace.
    5. we can use ZN  command to switch to another name space ex: ZN "Samples”
    6. To enter the command shell to SQL queries, we use the following command : do $system.SQL.Shell()
    7. After entering the SQL Shell, we can now execute the SQL Queries
    8. To view Audit logs using Terminal, switch to %SYS database(USE %SYS) and execute below query.
      - SELECT SystemID,UTCTimeStamp,EventType,Event,Username FROM %SYS.Audit

## 6. Limitation:

 1. The following important fields couldn't be mapped with intersystems-iris audit logs:
    - Source program : field is left blank, as this information is not embedded in the messages pulled from Intersystems-Iris.
    - Client HostName : Not available with logs. 
 2. IP addresses are blank while executing queries using Management Portal.
 3. SQL Syntax Error logs  not available in audit logs.
 4. For the SQL Error(not syntactical) queries,  success audit logs are capturing.
 5. User is expected to give Server Ip address according to the format of Client Ip address in the input configuration.
 6. We have seen the error(Communication link failure: Connection refused) using AWS EC2 instance Ip inside UC input configuration, a restart may be required for UC to bypass a connection refused issue.

## 7. Configuring the intersystems-iris filter in Guardium
The Guardium universal connector is the Guardium entry point for native audit logs. The Guardium universal connector identifies and parses the received events, and converts them to a standard Guardium format. The output of the Guardium universal connector is forwarded to the Guardium sniffer on the collector, for policy and auditing enforcements. Configure Guardium to read the native audit logs by customizing the intersystems-iris template.

### Before you begin
1. Configure the policies you require. See [policies](https://github.com/IBM/universal-connectors/#policies) for more information.
2. You must have permission for the S-Tap Management role. The admin user includes this role by default.
3. Download the [guardium_logstash-offline-plugin-intersystemsiris.zip](https://github.com/IBM/universal-connectors/releases/download/v1.6.0/logstash-filter-intersystems_iris_guardium_filter.zip) plug-in.
4. Download the plugin filter configuration file [intersystems_iris.conf](intersystems_iris.conf).
5. Download the intersystems-jdbc-3.7.1.jar from [here](IntersystemsIrisoverJDBC/intersystems-jdbc-3.7.1.jar) ([External Link](https://github.com/intersystems-community/iris-driver-distribution/blob/main/JDBC/JDK18/intersystems-jdbc-3.7.1.jar)).

### Procedure
1. On the collector, go to Setup > Tools and Views > Configure Universal Connector.
2. Enable the connector if it is already disabled, before proceeding uploading of the UC.
3. Click Upload File and select the offline [guardium_logstash-offline-plugin-intersystemsiris.zip](https://github.com/IBM/universal-connectors/releases/download/v1.6.0/logstash-filter-intersystems_iris_guardium_filter.zip) plug-in. After it is uploaded, click OK.
4. Again click Upload File and select the offline intersystems-jdbc-3.7.1.jar file. After it is uploaded, click OK.
5. Click the Plus sign to open the Connector Configuration dialog box.
6. Type a name in the Connector name field.
7. Update the input section to add the details from [intersystems_iris.conf](intersystems_iris.conf) file's input part, omitting the keyword "input{" at the beginning and its corresponding "}" at the end.
8. Update the filter section to add the details from [intersystems_iris.conf](intersystems_iris.conf) file's filter part, omitting the keyword "filter{" at the beginning and its corresponding "}" at the end.
9. The "type" field should match in the input and filter configuration section. This field should be unique for every individual connector added.
10. Click Save. Guardium validates the new connector, and enables the universal connector if it was disabled. After it is validated, it appears in the Configure Universal Connector page.

## 8. JDBC Load Balancing Configuration
In Intersystems IRIS JDBC input plug-in, we distribute load between two machines based on Even and Odd "AuditIndex".

### Procedure

    1. On First G Machine, in input section for JDBC Plugin update "statement" field like below:

    select UTCTimestamp as mytimestamp,Event,Username,ClientIPAddress,StartupClientIPAddress,SystemID,CSPSessionID,AuditIndex,Namespace,Description,OSUsername,EventData from %SYS.Audit where mod(AuditIndex,2) = 0 and UTCTimestamp > :sql_last_value order by UTCTimestamp asc

    2. On Second G Machine, in input section for JDBC Plugin update "statement" field like below:

    select UTCTimestamp as mytimestamp,Event,Username,ClientIPAddress,StartupClientIPAddress,SystemID,CSPSessionID,AuditIndex,Namespace,Description,OSUsername,EventData from %SYS.Audit where mod(AuditIndex,2) = 1 and UTCTimestamp > :sql_last_value order by UTCTimestamp asc