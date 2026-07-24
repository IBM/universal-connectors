# AlloyDB Omni Source Connector

This connector enables IBM Guardium Data Protection (GDP) to monitor and collect audit logs from AlloyDB Omni databases
through rsyslog forwarding by using Kafka Connect.

## Meet AlloyDB Omni over Syslog Connect

* Environments: On-prem
* Supported inputs: Kafka connect Syslog 2.0
* Supported Guardium versions:
    * Guardium Data Protection: Appliance bundle 12.2 or later
* Tested DB version: 18.1.0

Kafka-connect is a framework for streaming data between Apache Kafka and other systems.

## Configuring AlloyDB Omni

AlloyDB Omni is a downloadable edition of AlloyDB that you can run anywhere. It provides the same capabilities as AlloyDB on Google Cloud Platform but can be deployed on-premises or in any cloud environment.

### Prerequisites

1. AlloyDB Omni instance installed and running (based on PostgreSQL 17)
2. Network connectivity between the database server and Kafka cluster
3. rsyslog installed and configured on the database server (for Docker and Standard installations)
4. For OpenShift: OpenShift cluster with appropriate permissions to create pods, configmaps, and secrets. Enable the `anyuid` Security Context Constraint (SCC) on the namespace where AlloyDB Omni will run:

   ```bash
   oc adm policy add-scc-to-user anyuid -z default -n <your-namespace>
   ```

## Configuring logging for PostgreSQL

To enable comprehensive audit logging for AlloyDB Omni (which is based on PostgreSQL), you need to configure the PostgreSQL logging parameters.

### Procedure

#### For Docker Deployments

1. Connect to your AlloyDB Omni container:

   ```bash
   docker exec -it <container-name> bash
   ```

2. Add the following configuration to the PostgreSQL configuration file:

   ```bash
   cat >> /var/lib/postgresql/data/postgresql.conf <<'EOF'
   logging_collector = on
   log_destination = 'jsonlog'
   log_statement = 'all'
   log_min_duration_statement = 0
   log_directory = '/var/log/postgresql'
   log_filename = 'postgresql.json'
   EOF
   ```

3. Verify the configuration was added:

   ```bash
   tail -20 /var/lib/postgresql/data/postgresql.conf
   ```

4. Reload the PostgreSQL configuration:

   ```bash
   psql -U postgres -c "SELECT pg_reload_conf();"
   ```

5. Restart PostgreSQL to apply changes:

   ```bash
   su - postgres -c "/usr/lib/postgresql/17/bin/pg_ctl restart -D /var/lib/postgresql/data"
   ```

   **Note:** This will stop the container and you will exit the shell.

6. Wait for the container to restart:

   ```bash
   docker ps -a | grep <container-name>
   ```

7. Verify that logging is working by checking the log directory:

   ```bash
   docker exec -it <container-name> ls -la /var/log/postgresql/
   ```

#### For OpenShift Deployments

1. Connect to your AlloyDB Omni pod:

   ```bash
   oc exec -it alloydb-omni -- bash
   ```

2. Add the following configuration to the PostgreSQL configuration file:

   ```bash
   cat >> /var/lib/postgresql/data/postgresql.conf <<'EOF'
   logging_collector = on
   log_destination = 'jsonlog'
   log_statement = 'all'
   log_min_duration_statement = 0
   log_directory = '/var/log/postgresql'
   log_filename = 'postgresql.json'
   EOF
   ```

3. Verify the configuration was added:

   ```bash
   tail -20 /var/lib/postgresql/data/postgresql.conf
   ```

4. Reload the PostgreSQL configuration:

   ```bash
   psql -U postgres -c "SELECT pg_reload_conf();"
   ```

5. Restart PostgreSQL to apply changes:

   ```bash
   su - postgres -c "/usr/lib/postgresql/17/bin/pg_ctl restart -D /var/lib/postgresql/data"
   ```

   **Note:** This will stop the container and you will exit the shell.

6. Wait for the pod to restart and become ready:

   ```bash
   oc get pod alloydb-omni -w
   ```

   Wait until it shows `Running 2/2`.

7. Verify that logging is working by reconnecting and checking the log directory:

   ```bash
   oc exec -it alloydb-omni -- ls -la /var/log/postgresql/
   ```

**Configuration parameters explained:**
- `logging_collector = on` - Enables the logging collector background process
- `log_destination = 'jsonlog'` - Outputs logs in JSON format for easier parsing
- `log_statement = 'all'` - Logs all SQL statements (DDL, DML, and queries)
- `log_min_duration_statement = 0` - Logs all statements regardless of duration
- `log_directory = '/var/log/postgresql'` - Directory where log files are stored
- `log_filename = 'postgresql.json'` - Name of the log file

## Configuring rsyslog forwarding

To forward the PostgreSQL audit logs to Guardium through Kafka, you need to configure rsyslog on the database server.

### Procedure for Docker Deployments

1. Connect to your AlloyDB Omni container:

   ```bash
   docker exec -it <container-name> bash
   ```

2. Install rsyslog if not already installed:

   ```bash
   apt-get update && apt-get install -y rsyslog
   ```

3. Create the rsyslog configuration file for AlloyDB Omni:

   ```bash
   cat > /etc/rsyslog.d/alloydb-guardium.conf <<'EOF'
   global(workDirectory="/tmp/rsyslog")
   module(load="imfile")

   template(name="RFC5424JsonTemplate" type="string"
            string="<133>1 %timegenerated:::date-rfc3339% %hostname% alloydb - - - %msg%\n")

   ruleset(name="alloydb_ruleset") {
       if ($msg startswith "{") then {
           action(type="omfwd"
              target="<kafka-connect-hostname-1>"
              port="5142"
              protocol="tcp"
              template="RFC5424JsonTemplate"
              action.resumeRetryCount="-1"
              queue.type="LinkedList"
              queue.size="10000")
           action(type="omfwd"
              target="<kafka-connect-hostname-2>"
              port="5142"
              protocol="tcp"
              template="RFC5424JsonTemplate"
              action.resumeRetryCount="-1"
              queue.type="LinkedList"
              queue.size="10000")
           action(type="omfwd"
              target="<kafka-connect-hostname-3>"
              port="5142"
              protocol="tcp"
              template="RFC5424JsonTemplate"
              action.resumeRetryCount="-1"
              queue.type="LinkedList"
              queue.size="10000")
       }
       stop
   }

   input(type="imfile"
         File="/var/log/postgresql/postgresql.json*"
         Tag="alloydb"
         addMetadata="off"
         ruleset="alloydb_ruleset"
         reopenOnTruncate="on")
   EOF
   ```

   Replace `<kafka-connect-hostname-1>`, `<kafka-connect-hostname-2>`, and `<kafka-connect-hostname-3>` with your Kafka Connect server hostnames or IP addresses. If you have fewer Kafka nodes, remove the extra action blocks.

4. Start rsyslog service:

   ```bash
   service rsyslog start
   ```

6. Verify that rsyslog is running and forwarding logs:

   ```bash
   service rsyslog status
   tail -f /var/log/syslog | grep alloydb
   ```

### Procedure for OpenShift Deployments

For OpenShift, use a sidecar container approach with rsyslog running alongside the AlloyDB Omni container. This ensures rsyslog persists and automatically forwards logs.

1. First, create the PostgreSQL configuration ConfigMap (this contains the logging settings):

   ```bash
   cat > alloydb-postgresql-conf.yaml <<'EOF'
   apiVersion: v1
   kind: ConfigMap
   metadata:
     name: alloydb-postgresql-conf
     namespace: <your-namespace>
   data:
     postgresql.conf: |
       logging_collector = on
       log_destination = 'jsonlog'
       log_statement = 'all'
       log_min_duration_statement = 0
       log_directory = '/var/log/postgresql'
       log_filename = 'postgresql.json'
   EOF
   ```

2. Create the PostgreSQL password secret:

   ```bash
   oc create secret generic alloydb-secret \
     --from-literal=POSTGRES_PASSWORD='<your-password>' \
     -n <your-namespace>
   ```

3. Create the complete Pod YAML file with the AlloyDB Omni container and a log-forwarder sidecar:

   ```yaml
   apiVersion: v1
   kind: Pod
   metadata:
     labels:
       app: alloydb-omni
     name: alloydb-omni
     namespace: <your-namespace>
   spec:
     containers:
     # Main AlloyDB Omni container
     - name: alloydb
       image: gcr.io/alloydb-omni/alloydbomni:latest
       env:
       - name: POSTGRES_PASSWORD
         valueFrom:
           secretKeyRef:
             key: POSTGRES_PASSWORD
             name: alloydb-secret
       ports:
       - containerPort: 5432
         protocol: TCP
       volumeMounts:
       - mountPath: /var/lib/postgresql/data
         name: data-volume
       - mountPath: /var/log/postgresql
         name: log-volume
       - mountPath: /etc/custom-postgresql
         name: generated-config
     
     # Rsyslog sidecar container for log forwarding
     - name: log-forwarder
       image: registry.access.redhat.com/ubi9/ubi-minimal
       command:
       - /bin/sh
       - -c
       args:
       - |
         microdnf install -y rsyslog && \
         mkdir -p /tmp/rsyslog && \
         cat >/etc/rsyslog.conf <<'EOF'
         global(workDirectory="/tmp/rsyslog")
         module(load="imfile")

         template(name="RFC5424JsonTemplate" type="string"
                  string="<133>1 %timegenerated:::date-rfc3339% %hostname% alloydb - - - %msg%\n")

         ruleset(name="alloydb_ruleset") {
             if ($msg startswith "{") then {
                 action(type="omfwd"
                    target="<kafka-connect-hostname-1>"
                    port="5142"
                    protocol="tcp"
                    template="RFC5424JsonTemplate"
                    action.resumeRetryCount="-1"
                    queue.type="LinkedList"
                    queue.size="10000")
                 action(type="omfwd"
                    target="<kafka-connect-hostname-2>"
                    port="5142"
                    protocol="tcp"
                    template="RFC5424JsonTemplate"
                    action.resumeRetryCount="-1"
                    queue.type="LinkedList"
                    queue.size="10000")
                 action(type="omfwd"
                    target="<kafka-connect-hostname-3>"
                    port="5142"
                    protocol="tcp"
                    template="RFC5424JsonTemplate"
                    action.resumeRetryCount="-1"
                    queue.type="LinkedList"
                    queue.size="10000")
             }
             stop
         }

         input(type="imfile"
               File="/var/log/postgresql/postgresql.json*"
               Tag="alloydb"
               addMetadata="off"
               ruleset="alloydb_ruleset"
               reopenOnTruncate="on")
         EOF

         rsyslogd -n
       volumeMounts:
       - mountPath: /var/log/postgresql
         name: log-volume
     
     # Init container to prepare PostgreSQL configuration
     initContainers:
     - name: prepare-postgresql-conf
       image: registry.access.redhat.com/ubi9/ubi-minimal
       command:
       - /bin/sh
       - -c
       args:
       - |
         mkdir -p /config-out
         cp /config-in/postgresql.conf /config-out/postgresql.conf
       volumeMounts:
       - mountPath: /config-in
         name: postgres-config
       - mountPath: /config-out
         name: generated-config
     
     # Volumes
     volumes:
     - name: data-volume
       persistentVolumeClaim:
         claimName: alloydb-omni-data-pvc
     - name: log-volume
       emptyDir: {}
     - name: postgres-config
       configMap:
         name: alloydb-postgresql-conf
     - name: generated-config
       emptyDir: {}
     
     securityContext:
       fsGroup: 26
   ```

   **Important:** Replace the following placeholders:
   - `<your-namespace>` with your OpenShift namespace
   - `<kafka-connect-hostname-1>`, `<kafka-connect-hostname-2>`, and `<kafka-connect-hostname-3>` with your Kafka Connect server hostnames or IP addresses
   - If you have fewer Kafka nodes, remove the extra action blocks

4. Create the Persistent Volume Claim (PVC) for durable database storage:

   ```bash
   cat > alloydb-omni-pvc.yaml <<'EOF'
   apiVersion: v1
   kind: PersistentVolumeClaim
   metadata:
     name: alloydb-omni-data-pvc
     namespace: <your-namespace>
   spec:
     accessModes:
       - ReadWriteOnce
     resources:
       requests:
         storage: 10Gi
   EOF
   ```

   Adjust `storage` to the size required for your workload.

5. Apply the ConfigMap, Secret, PVC, and Pod:

   ```bash
   oc apply -f alloydb-postgresql-conf.yaml
   oc apply -f alloydb-omni-pvc.yaml
   oc apply -f alloydb-omni-pod.yaml
   ```

6. Monitor the pod until it's running:

   ```bash
   oc get pod alloydb-omni -w
   ```

   Wait until it shows `Running 2/2` (both containers running).

7. Verify that rsyslog is running and forwarding logs:

   ```bash
   oc logs alloydb-omni -c log-forwarder
   oc exec -it alloydb-omni -c alloydb -- tail -f /var/log/postgresql/postgresql.json
   ```


**Configuration parameters explained:**
- `global(workDirectory="/tmp/rsyslog")` - Sets the working directory for rsyslog
- `module(load="imfile")` - Loads the file input module
- `template(name="RFC5424JsonTemplate")` - Defines a custom template for RFC5424 format with JSON message
- `ruleset(name="alloydb_ruleset")` - Defines a named ruleset for AlloyDB logs
- `if ($msg startswith "{")` - Filters only JSON messages (PostgreSQL JSON logs)
- `action(type="omfwd")` - Specifies the forward action to Kafka Connect servers
- `target` - The hostname or IP address of the Kafka Connect server
- `port` - The port number where Kafka Connect is listening (default: 5142)
- `protocol="tcp"` - Uses TCP protocol for reliable delivery
- `template="RFC5424JsonTemplate"` - Uses the custom RFC5424 template for log forwarding
- `action.resumeRetryCount="-1"` - Retries indefinitely on connection failure
- `queue.type="LinkedList"` - Uses a linked list queue for buffering
- `queue.size="10000"` - Sets queue size to 10,000 messages
- `File="/var/log/postgresql/postgresql.json*"` - Monitors all PostgreSQL JSON log files (including rotated files)
- `addMetadata="off"` - Disables adding file metadata to messages
- `reopenOnTruncate="on"` - Reopens file if truncated (useful for log rotation)
- `stop` - Stops processing after this action

## Enabling audit logs

The PostgreSQL configuration ensures that all SQL statements are logged. The rsyslog configuration forwards these logs to the Kafka Connect server.


## Limitations

1. The universal connector can be installed on multiple Managed Units (MUs) for high availability, but all traffic will be displayed to a single MU.

2. See the [Testing a Connection](#testing-a-connection) section for test connection limitations and expected behaviour.

## Configuring Guardium

The Guardium universal connector is the Guardium entry point for native audit and data access logs. The Guardium
universal connector identifies and parses the received events, and converts them to a standard Guardium format. The
output of the Guardium universal connector is forwarded to the Guardium sniffer on the collector for policy and auditing
enforcements. You can configure Guardium to read the native audit and data access logs by customizing the AlloyDB Omni
template.

### Before you begin

* Configure the policies that you need. For more information, see [Policies](/docs/#policies).
* You must have permissions for the S-Tap Management role. By default, the admin user is assigned the S-Tap Management
  role.
* Ensure that the Kafka Connect server is configured to receive logs on port 5142.

## Creating datasource profiles

You can create a new datasource profile from the **Datasource Profile Management** page.

### Procedure

1. Go to **Manage > Universal Connector > Datasource Profile Management**
2. Click the **➕ (Add)** button.
3. You can create a profile by using one of the following methods:

    * To create a new profile manually, go to the **"Add Profile"** tab and provide values for the following fields.
        * **Name** and **Description**.
        * Select a **Plug-in Type** from the dropdown. For example, **AlloyDB Omni Over Syslog Connect 2.0**.

    * To upload from CSV, go to the **Upload from CSV** tab and upload an exported or manually created CSV file
      containing one or more profiles. You can also choose from the following options:
        * **Update existing profiles on name match** — Updates profiles with the same name if they already exist.
        * **Test connection for imported profiles** — Automatically tests connections after profiles are created.
        * **Use ELB** — Enables ELB support for imported profiles. You must provide the number of MUs to be used in the
          ELB process.

**Note:** Configuration options vary based on the selected plug-in.

## Configuration: Kafka Connect-based Plugins

The following table describes the fields that are specific to Kafka Connect and similar plugins.

| Field                              | Description                                                                                                       |
|------------------------------------|-------------------------------------------------------------------------------------------------------------------|
| **Name**                           | Unique name of the profile.                                                                                       |
| **Description**                    | Description of the profile.                                                                                       |
| **Plug-in**                        | Plug-in type for this profile. A full list of available plug-ins is available on the **Package Management** page. |
| **Syslog Credentials**             | Select or create Syslog Credentials. The credential type must be **Syslog Credentials** with a username field (the username can be any value as it is not used for authentication). |
| **Kafka Cluster**                  | Kafka cluster to deploy the universal connector.                                                                  |
| **Label**                          | Grouping label (e.g., **customer name** or **ID**).                                                               |
| **Syslog Port**                    | Port number for rsyslog forwarding (default: 6514).                                                               |
| **No traffic threshold (minutes)** | The time period after which the system detects inactivity.                                                        |

## Testing a Connection

After creating a profile, you must test the connection to ensure that the provided configuration is valid.

**Note:**
- Only one syslog profile can use a specific port at a time across all datasource profiles in your Guardium environment. If multiple syslog profiles are configured to use the same port, connection conflicts occur.
- You must test the connection immediately before you deploy the profile. The test connection validates that the port is available.
- If you test a connection and then wait before deployment, another syslog profile might claim the port and cause the deployment to fail.
- If a test connection is successful and the profile is deployed, other profiles using the same port will also succeed in testing unless the port is actually occupied by the deployed profile.
- Test connection will fail for a profile that is already deployed. When a profile is deployed, it occupies the port defined in its configuration. Since the port is already in use, any subsequent connection test will fail with a port conflict error.

### Procedure

1. Select the new profile.
2. From the top menu, click **Test Connection**.
3. If the test is successful, you can proceed to installing the profile.

---

## Installing a Profile

Once the connection test is successful, you can install the profile on **Managed Units (MUs)** or **Edges**. The parsed
audit logs are sent to the selected Managed Unit or Edge to be consumed by the Sniffer.

### Procedure

1. Select the profile.
2. From the **Install** menu, click **Install**.
3. From the list of available MUs and Edges that is displayed, select the ones that you want to deploy the profile to.

---

## Uninstalling or reinstalling profiles

An installed profile can be uninstalled or reinstalled if needed.

### Procedure

1. Select the profile.
2. From the list of available actions, select the desired option: **Uninstall** or **Reinstall**.

---


## Troubleshooting

#### Logs are not being forwarded

1. Verify that rsyslog is running:
   ```bash
   sudo systemctl status rsyslog
   ```

2. Check rsyslog configuration for syntax errors:
   ```bash
   sudo rsyslogd -N1
   ```

3. Verify that the log file exists and has correct permissions:
   ```bash
   ls -la /var/log/postgresql/postgresql.json
   ```

4. Check rsyslog logs for errors:
   ```bash
   sudo tail -f /var/log/syslog | grep rsyslog
   ```

#### PostgreSQL logs are not being generated

1. Verify that logging is enabled:
   ```sql
   SHOW logging_collector;
   SHOW log_destination;
   SHOW log_statement;
   ```

2. Check PostgreSQL logs for errors:
   ```bash
   sudo tail -f /var/log/postgresql/postgresql.json
   ```

3. Ensure the log directory exists and has correct permissions:
   ```bash
   sudo mkdir -p /var/log/postgresql
   sudo chown postgres:postgres /var/log/postgresql
   sudo chmod 755 /var/log/postgresql
   ```

#### Connection test fails

1.  Verify that the Kafka Connect server is listening on the specified port:
   ```bash
   netstat -tuln | grep 5142
   ```

#### High disk usage due to logs

1. Implement log rotation as described in the production configuration section.

2. Adjust logging parameters to reduce log volume:
   ```sql
   ALTER SYSTEM SET log_min_duration_statement = 5000;  -- Only log slow queries
   ALTER SYSTEM SET log_statement = 'ddl';  -- Only log DDL statements
   SELECT pg_reload_conf();
   ```

3. Monitor disk space regularly:
   ```bash
   df -h /var/log/postgresql