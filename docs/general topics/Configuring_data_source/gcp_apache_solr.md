### GCP instance Creation 

1. In the Google Cloud Console, go to the VM instances page.
2. Select your project and click Continue.
3. Click Create instance.
4. Specify a Name for your VM.
5. Optional: Change the Zone for this VM. Compute Engine randomizes the list of zones within each region to encourage use across multiple zones.
6. Select a Machine configuration for your VM.
7. In the Firewall section, to permit HTTP or HTTPS traffic to the VM, select Allow HTTP traffic or Allow HTTPS traffic.(add the port on which solr is running, default is 8983)
To create and start the VM, click Create.

You can learn more about VM Creation [here](https://cloud.google.com/compute/docs/instances/create-start-instance#expandable-2)


### Configuring the Apache Solr on GCP
#### Apache Solr Setup 
```
1.Install java by running the following command:
        $ sudo apt install default-jre
2.Run the below command to check the java version:
        $ java –version
3.In order for Solr to work as expected, the user needs to have the lsof command installed as well.
        The lsof command stands for "list open files".  Run the following command for lsof Installation:
        $ sudo apt install lsof
4.Run the following commands to download Solr installation files:
        $ cd /usr/src
        $ sudo apt install wget
        $ sudo apt-get install wget
        $ sudo wget https://archive.apache.org/dist/lucene/solr/8.6.0/solr-8.6.0.tgz
        $ sudo tar -xzvf solr-8.6.0.tgz
5.Run the Solr installation script
        $ cd solr-8.6.0/bin
        $ sudo ./install_solr_service.sh ../../solr-8.6.0.tgz
```
#### Launching Apache Solr
```
1.Once the script completes, Solr will be installed as a service and run in the background on the user's server (on port 8983). To verify, run:
       $ sudo service solr status
2.Solr facilitates to run it in 2 modes:
2.1.Standalone mode :An index is stored on a single computer and the setup is called a core.There can be multiple cores or indexes here.
To Launch Solr in Standalone Mode:
       $ cd /opt/solr-8.6.0
       $ sudo bin/solr start -force 
2.2.SolrCloud mode:  An index is distributed across multiple computers or even multiple server instances on one computer. Groups of documents here are called collections.
 To Launch Solr in SolrCloud Mode:
       $ cd /opt/solr-8.6.0
       $ sudo bin/solr start -e cloud -force
```

You can learn more about Apache Solr setup [here](https://solr.apache.org/guide/8_8/installing-solr.html)
Once the Apache Solr setup is done, Ops Agent needs to be installed and configured on the system.

## Login to the Solr Dashboard
To access the Solr admin panel, visit the hostname or IP address on port (solr is running):
    http://ip_address:port/solr/

### Core Creation in Standalone mode
```
1.Create a new Solr core with the following command:
  $ sudo bin/solr create -c core_name -force
  For example: Core Created named as new_core:
  $ sudo bin/solr create -c new_core -force
2.Created core will reflect in the core drop-down menu on the solr admin console.
```
### Collection Creation in SolrCloud mode
```
1.Create a new Solr collection with the following command(having default shard and replica count):
  $ sudo bin/solr create -c collection_name -force
To create a collection having specified shard and replica count. 
  $ sudo bin/solr create -c collection_name -s <count> -rf <count> -force
For example: Collection Created named as new_collection respectively.
  $ sudo bin/solr create -c new_collection -force
  $ sudo bin/solr create -c new_collection -s 1 -rf 2 -force
2.Created collection will reflect in the collection drop-down menu on the solr admin console.

```
### Configure Ops Agent
The Ops Agent is the primary agent for collecting telemetry from your Compute Engine instances.
```
Run the following commands to install ops agent
sudo curl -sSO https://dl.google.com/cloudagents/add-google-cloud-ops-agent-repo.sh
sudo bash add-google-cloud-ops-agent-repo.sh --also-install

To verify that the agent is working as expected, run:
sudo systemctl status google-cloud-ops-agent
```
