# Configuring the Azure PostgreSQL service

There are following ways to get Azure PostgreSQL audit data:

	1. Azure Event Hub
	2. Azure Storage
	3. Log Analytics Workspace
	4. Azure Partner Solution

In this plugin we have used Azure Event Hub.


###	Procedure:

1. Go to https://portal.azure.com/.
   
3. Click search bar.
   
5. Search for azure database for postgreSQL servers.
   
7. Click on 'Create' button.
   
9. Select server type.(here we use single server.)
    
11. Select Single server and click on create button and fill details in basic option.
    
13. In Basic option:
			1. Select your subscription name.
    
			2. Select existing resource group or create new one.
    
			3. For new resource group click on create button  and enter name for resource group.
    
			4. Provide 'Server Name'. Keep 'Data source' as none ,select appropriate location and set 'Version=11'.
    
			5. Keep compute + storage as 'General Purpose, 4 vCore(s), 100 GB'.
    
			6. Provide server admin username and password.
    
8.	Click 'Review + Create' Button.
   
10.	Verify the configuration and then click on 'Create' Button.
    
12.	After successful creation of resource group, select resource button.
    	
13.	From settings,select connection security.

14.	In firewall rules,select allow access to azure services to 'Yes'.

15. Click on add current client IP address, and then click on Save.
	
	
