# Installing Apache Solr

Create a Linux virtual machine in the Azure portal.

You can learn more about Virtual Machine Creation [here](https://docs.microsoft.com/en-us/azure/virtual-machines/linux/quick-create-portal)

## Procedure

1.Install Java by running the following command:
        $ sudo apt install default-jre
        
2.Run the below command to check the java version:
        $ java –version
        
3.In order for Solr to work as expected, the user needs to have the lsof command installed as well.
        The lsof command stands for "list open files".  Run the following command for lsof installation:
        $ sudo apt install lsof
        
4.Run the following commands to download the Solr installation files:
        $ cd /usr/src
        $ sudo apt install wget
        $ sudo apt-get install wget
        $ sudo wget https://archive.apache.org/dist/lucene/solr/8.6.0/solr-8.6.0.tgz
        $ sudo tar -xzvf solr-8.6.0.tgz
        
5.Run the Solr installation script
        $ cd solr-8.6.0/bin
        $ sudo ./install_solr_service.sh ../../solr-8.6.0.tgz

### Launching Apache Solr

1.Once the script completes, Solr will be installed as a service and run in the background on the user's server (on port 8983). To verify, run:
       $ sudo service solr status
       
2.The Solr can be run in 2 different modes, You can only use one mode at a time:

2.1.Standalone mode :An index is stored on a single computer and the setup is called a core.There can be multiple cores or indexes here.
To Launch Solr in Standalone Mode:
       $ cd /opt/solr-8.6.0
       $ sudo bin/solr start -force 
       
2.2.SolrCloud mode:  An index is distributed across multiple computers or even multiple server instances on one computer. Groups of documents here are called collections.
 To Launch Solr in SolrCloud Mode:
       $ cd /opt/solr-8.6.0
       $ sudo bin/solr start -e cloud -force


### Adding an inbound port rule
1. Go to the virtual machine.
2. In the left-nav, select Networking.
3. The rules will be displayed on right side of the portal.
4. Select Inbound port rules ,then select Add.
5. Add your port(on which solr is running, default is 8983) under Destination Port ranges.
6. Click Add to create the rule.

### Login to the Solr Dashboard
To access the Solr admin panel, visit the hostname or IP address on the port (on which Solr is running):
    http://ip_address:port/solr/

### Core Creation in Standalone mode

1.Create a new Solr core with the following command:

  $ sudo bin/solr create -c core_name -force
  For example: Core Created named as new_core:
  $ sudo bin/solr create -c new_core -force
  
2.The created core will reflect in the core drop-down menu on the Solr admin console.

### Collection Creation in SolrCloud mode

1.Create a new Solr collection with the following command with the default shard and replica count:

  $ sudo bin/solr create -c collection_name -force
  
To create a collection with a customized shard and replica count. 

  $ sudo bin/solr create -c collection_name -s <count> -rf <count> -force
  For example: Collection Created named as new_collection respectively.
  $ sudo bin/solr create -c new_collection -force
  $ sudo bin/solr create -c new_collection -s 1 -rf 2 -force
  
2.The created collection will reflect in the collection drop-down menu on the Solr admin console.
  
