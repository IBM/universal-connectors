# Monitoring Connector and Data Flow Status

The connectors appear in the standard Guardium reports and UI pages. You can check the general status of the connector by using the API. Learn where you can view the status.

## Connector Status

The connectors appear in the S-TAP pages of the UI only after data is received in the connector. Guardium creates the connector instance after data is received. If the connector does not have active traffic, then the connector does not display in the S-TAP Status or Events pages.

In order to verify that a new connector was configured successfully and to see the connector status on the S-TAP status, run a test command on the data source and make sure you can see the new connector status.

You can typically see the connectors and their status in the following Guardium pages:

* S-TAP Status page. The connectors are indicated by the following elements:
  * S-TAP Host - ```database host: database port: UCn```.
  * S-TAP Version - ```Universal Connector Vn.n.n```.
  * Status - Indicates data flow by ```Active``` or ```Inactive```.

* S-TAP Events
* Central manager pages:
  * In the Deployment Health Topology, click ```Expand S-TAPs```. Connectors have the icon: ![icon](/universal-connectors/docs/images/icon-guc.jpg), color-coded green, yellow, or red. For more information, see [Deployment health topology and table views](https://www.ibm.com/docs/en/guardium/11.4?topic=views-deployment-health-topology-table).
 * In the Deployment Health Table, in the S-TAPs tab, you can identify connectors by:
   * Under Hostname or IP address, the address ends in ```:UCn```.
   * Under Version, the name is ```Universal Connector Vn.n.n.```.
   
   (For a description of the Deployment Health Table, see [Deployment health topology and table views](https://www.ibm.com/docs/en/guardium/11.4?topic=views-deployment-health-topology-table).)
   
   * In the [Deployment health dashboard](https://www.ibm.com/docs/en/guardium/11.4?topic=views-deployment-health-dashboard), S-TAPs by version chart, the Guardium Universal Connectors appear as S-TAPs with version numbers. In all other charts, the connectors are treated the same as S-TAPs.
   * Enterprise S-TAP View to check S-TAP status. This report requires configuration. See [External data correlation](https://www.ibm.com/docs/en/guardium/11.4?topic=audit-external-data-correlation). Check for data flow in the Status column. The status Active indicates data flow.
   * Detailed Enterprise S-TAP View. The Status column. The status Active indicates data flow. This report requires configuration. See [External data correlation](https://www.ibm.com/docs/en/guardium/11.4?topic=audit-external-data-correlation).


***Note: If the Universal Connector appears red, it might be because:***

***It was enabled, and then disabled.***

***Data did not flow in over 1 hour.***

***An issue occurred on the Universal Connector.***

To view the connector status, run the API command ```grdapi get_universal_connector_status```.

### Data
To view data sent into Guardium, view these UI pages:
- The connectors appear in many reports, similar to S-TAPs. Reports show data for connectors only if the connectors have policies that are installed, just like Guardium collectors. Make sure you have a policy installed with the ```Log Full Detail``` rule action. Add reports to a dashboard, for example:

  * Full SQL (Clone and edit this report to test that other fields are inserted correctly into Guardium).
  * SQL Errors. You might need to add the description field to see your custom errors, if you pass a string to Guardium representing your custom error text.
  * Failed Login Attempts


- [Investigation dashboard](https://www.ibm.com/docs/en/guardium/11.4?topic=audit-investigation-dashboards). Data indexing is delayed by 2 minutes.

**Limitation: An object that is deleted from a bucket is not specified in native audit events if the deletion was made from an S3 console. The delete operation appears in reports and the investigation dashboard, but the object name is not included.**

- Set up Compliance Monitoring. See Smart assistant for [compliance monitoring](https://www.ibm.com/docs/en/guardium/11.4?topic=audit-smart-assistant-compliance-monitoring). In the Database tab, you can identify data sources for monitoring.

- The Compliance monitoring page shows DBs if Guardium supports your DB and data is flowing into Guardium. This view updates once per hour and shows DBs that had remote activity in the past hour. You do not see anything here if you run commands directly from your DB server.
