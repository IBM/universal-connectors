
# Configuring the AzureSQL service

There are two ways to get AzureSQL audit data:

1. Universal Connector using Azure object storage plus a JDBC feed or
2. Guardium Streams using a Azure Event Hub

In this plugin we have used Object Storage
If the customer wants to get more insights into these options, they can reach out to Guardium Offering Managers.

### Procedure:
	1. Go to https://portal.azure.com/.
	2. Click Search Bar.
	3. Search for AzureSQL.
	4. Click on "Create" Button.
		<The button name is 'Create'>
	5. Select SQL databases option.
	6. Choose Resource Type depending on requirements(Single database,Elastic pool).
	7. Click on "Create" Button.
	8. Select Existing Resource group or Create New one.
	9. Provide Database Name.
	10. Click on "Create New" Server Button.
		<The button name is 'Create new' under server field>
	11. Provide Server Name and Select appropriate Location .
	12. Select appropriate Authentication method as per requirement.
	13. Provide "Server admin login name and Password.
	14. Click on "OK" Button.
	15. Select the "Compute + storage" Configuration depending on requirement.
	16. Click "Review + Create" Button.
	17. Verify the Configuration and then Click on "Create" Button.
	18. Again Click on Search Bar:
			a. Search for Storage Account
			b. Click on "create" to create new storage account
			c. Select resource group which you have created.
			d. Provide Storage Account Name.
			e. Select appropriate location.
			f. Choose additional configuration as per requirement.
			g. Click on "Review + create" Button.
			h. After Review Click on "Create" Button	
