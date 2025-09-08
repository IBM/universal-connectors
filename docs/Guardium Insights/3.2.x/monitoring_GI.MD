# Monitoring Connector and Data Flow Status for Guardium Insights

The connectors appear in the standard Guardium reports and UI pages. You can check the general status of the connector by using the API. Learn where you can view the status.


## Check Universal Connector Status in Real Time
* In Guardium Insights, select **Settings** > **Connections**

In the table that appears, you can view name, type, data source, connectivity status, and status information for all your connections. You can also use the filter to view Universal Connector connections only.

The connectivity status options are **healthy**, **unhealthy**, or **starting up**.
In the case of a healthy connection, the status information may also include the latest traffic that was captured in the Universal Connector.

## Monitor Administrative Actions and Traffic of Universal Connectors Over Time

* In Guardium Insights, select the menu icon > **Reports** > **Connection events**
* Search for and select the Connection events report

In this report, you can view administrative and traffic events that occurred on a connection over time.
You can also use the filter to view Universal Connector connections only.

The Connection events report can also be filtered by event type. The event types are:

* ```ADMIN_CREATE```
* ```ADMIN_UPDATE```
* ```ADMIN_DELETE```
* ```STATUS```
* ```DATA```
* ```ERROR```
	
```DATA``` events, for instance, describe how many events occurred on a specific database via a specific
connection over the last minute. Here is an example of a ```DATA``` event message:

```1000 events occurred on db test_123 during the last minute.```
