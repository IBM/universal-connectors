# DoucumentDB

## Creating AWS Cloud9 Environment

### Procedure:
1. From the AWS Management Console navigate to the AWS Cloud9 console and choose Create environment
2. Enter a name,for example GuardiumCloud9
3. Choose Next step
4. In the Configure settings section, choose Next step.
5. In the Review section, choose Create environment.

## Configuring the AWS DocumentDB service/cluster

### Procedure:
1. Go to https://console.aws.amazon.com/.
2. Search for and navigate to the AWS DocumentDB management Console. Click on Launch Amazon DocumentDB, and under Clusters, choose Create.​
3. Choose a name for the new cluster, or go for the default name. Optional step: Choosing "1" for Number of instances helps minimize cost.
4. In the Authentication section, choose a username and password.
5. Turn On "Show advance settings" and under "Log exports" section, enable both "Audit logs" and "Profiler logs". Amazon DocumentDB will now provision the new cluster, and this process can take up to a few minutes to finish. User can connect to this cluster when both the cluster and instance status show as Available

### Configuring the AWS DocumentDB Security Group

### Procedure:
1. Once the cluster is available, click on the newly created cluster.
2. In security groups section, under Connection and Security tab, click on the tagged security group.
3.  Click on Inbound Rules tab and then on Edit Inbound rules to add new rule. 
4. For Type, choose Custom TCP Rule. For Port Range , enter 27017​. Note: Port 27017 is the default port for Amazon DocumentDB.​
5. In the Source feild, choose Custom and specify the public IPv4 address of the computer (that will connect to DocumentDB instance) or network in CIDR notation. For example, if your IPv4 address is 203.0.113.25, specify 203.0.113.25/32 to list this single IPv4 address in CIDR notation.
6. In our case we will add newly created cloud9 environment's security group here. The source will be the security group for the AWS Cloud9 environment we just created. To see a list of available security groups, enter cloud9 in the destination field. Choose the security group with the name aws-cloud9-<environment name>.
7. Save security group settings.
