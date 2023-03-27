# FAQs and Troubleshooting for Guardium Data Protection

Find answers to commonly asked questions about the Guardium univer***REMOVED***l connector.

-   **How many connectors can one collector have?**

    It depends on the load.

-   **A connector is fully configured to connect with the Guardium univer***REMOVED***l connector but it does not show in the S-TAP or central manager pages.**

    Check that the database has activity. The univer***REMOVED***l connector displays in the pages only when it has active traffic.

-   **The connector status is red. How to troubleshoot?**

    In the Univer***REMOVED***l Connector page, click **Enable**.

    Alternatively:

    1.  Run the API: grdapi get\_univer***REMOVED***l\_connector\_status
    2.  If the status is ok, run the API: grdapi stop\_univer***REMOVED***l\_connector
    3.  Run the API: grdapi run\_univer***REMOVED***l\_connector
    
-   **Does MustGather support the Guardium univer***REMOVED***l connector?**

    Yes, every MustGather option includes logs for the Guardium univer***REMOVED***l connector. See [Basic information for IBM Support](https://www.ibm.com/docs/en/guardium/11.4?topic=problems-basic-information-support).

-   **Data is missing from reports**

    1.  Go to the Univer***REMOVED***l Connector page and see whether it is enabled.
    2.  If enabled, go to the S-TAP Status page to see whether your data source connector status is red or green.
        -   Status in the UI is red. There is no active connection from the Guardium univer***REMOVED***l connector. It might be that the univer***REMOVED***l connector stopped working.
            1.  Di***REMOVED***ble and enable in the GUI \(or by using the APIs grdapi get\_univer***REMOVED***l\_connector\_status and grdapi run\_univer***REMOVED***l\_connector\).
            2.  If the status is still **Di***REMOVED***bled**, run the MustGather.
        -   Status in UI is green. The univer***REMOVED***l connector is OK.
            1.  Verify that policy that is installed on managed unit allows ***REMOVED***ving data in Guardium \(see [Configuring policies for the univer***REMOVED***l connector](/docs/Guardium%20Data%20Protection/uc_policies_gdp.md))
            2.  Check the MustGather logs that data is coming in from the data source for the univer***REMOVED***l connector to capture.
            3.  Check that communication between the database server and the managed unit is not blocked \(managed unit is accessible from the database server\).
    3.  User plug-ins.
        -   Verify the event path, from DB server to Guardium database.
        -   If you're using Syslog to send the data, verify that it's configured properly to send to the correct Guardium IP and port.
        -   Verify `rsyslog service` is running. Run service rsyslog status.
        -   Check the Connector configuration, on Configure Univer***REMOVED***l connector page, and verify the port.
    4.  Run MustGather, collect the information, and send to Guardium support.
-   **The Univer***REMOVED***l connector started but Guardium is not showing any events.**

    Verify that a Policy with a **log full details** rule is installed, and that no other policy blocks it.

    Create a MustGather and look for the univer***REMOVED***l connector log \(uc-logstash.log\), or Logstash log \(logstash\_stdout\_stderr.log\), after you perform a MustGather command. See the section for developing a filter plug-in in [Developing new plug-ins for Guardium Data Protection](/docs/Guardium%20Data%20Protection/developing_plugins_gdp.md). 
    
    By default, the log level reports only errors. Raise the log level, if needed, by entering: grdapi run\_univer***REMOVED***l\_connector uc\_debug\_level="debug"If the log seems normal, try to restart the inspection core by entering: restart inspection-core


## Plug-in questions

1.   **The univer***REMOVED***l connector does not start with your plug-in or configuration**

See the section [here](/docs/Guardium%20Data%20Protection/developing_plugins_gdp.md) about installing and testing the filter or input plug-in on a staging Guardium system.  for details on how to collect diagnostic data by running a `must gather` command.

2.    **Is Java required to create a plug-in?**

No, you can develop by using Ruby, especially when Guardium knows how to parse your data source commands. However, if you need to parse the commands for a new data source type, Java is the best choice.

3.    **How much time does it take to develop a plug-in?**

Give yourself 4 - 5 weeks, maybe a bit more to perfect it.
