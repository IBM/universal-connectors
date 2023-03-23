
## Monitoring connector and data flow status for Guardium Insights 

The connectors appear in the standard Guardium reports and UI pages. You can check the general status of the connector by using the API. Learn where you can view the status.


### Check univer***REMOVED***l connector status in real time
 * In Guardium Insights, select  **Settings**>
**Connections**

In the table that appears you can view name, type, data source, connectivity status and status information for all your connections. You can also use the filter to view univer***REMOVED***l connector connections only.  

The connectivity status options are **healthy**, **unhealthy** or **starting up**. 
In the case of a healthy connection, the status information may also include the latest traffic that was captured in the univer***REMOVED***l connector. 

### Monitor administrative actions and traffic of univer***REMOVED***l connectors over time

* In Guardium Insights, select the menu icon >**reports** > **Connection events**
* Search for and select the Connection events report

In this report you can view administrative and traffic events that occurred on a connection over time. 
You can also use the filter to view univer***REMOVED***l connector connections only. 

The Connection events report can also be filtered by event type. The event types are: 

* ```ADMIN_CREATE```
* ```ADMIN_UPDATE```
* ```ADMIN_DELETE```
* ```STATUS```
* ```DATA``` 
* ```ERROR``` 
	
```DATA``` events, for instance, describe how many events occurred on a specific database via a specific
connection over the last minute. Here is an example of a ```DATA``` event mes***REMOVED***ge: 

```1000 events occurred on db test_123 during the last minute.```
