# FAQs and Troubleshooting for Guardium Data Protection

Find answers to commonly asked questions about the Guardium universal connector.

-   **How many connectors can one collector have?**

    It depends on the load.

-   **A connector is fully configured to connect with the Guardium universal connector but it does not show in the S-TAP or central manager pages.**

    Check that the database has activity. The universal connector displays in the pages only when it has active traffic.

-   **The connector status is red. How to troubleshoot?**

    In the Universal Connector page, click **Enable**.

    Alternatively:

    1.  Run the API: grdapi get\_universal\_connector\_status
    2.  If the status is ok, run the API: grdapi stop\_universal\_connector
    3.  Run the API: grdapi run\_universal\_connector
-   **Does MustGather support the Guardium universal connector?**

    Yes, every MustGather option includes logs for the Guardium universal connector. See [Basic information for IBM Support](https://www.ibm.com/docs/en/guardium/11.4?topic=problems-basic-information-support).

-   **Data is missing from reports**

    1.  Go to the Universal Connector page and see whether it is enabled.
    2.  If enabled, go to the S-TAP Status page to see whether your data source connector status is red or green.
        -   Status in the UI is red. There is no active connection from the Guardium universal connector. It might be that the universal connector stopped working.
            1.  Disable and enable in the GUI \(or by using the APIs grdapi get\_universal\_connector\_status and grdapi run\_universal\_connector\).
            2.  If the status is still **Disabled**, run the MustGather.
        -   Status in UI is green. The universal connector is OK.
            1.  Verify that policy that is installed on managed unit allows saving data in Guardium \(see [Configuring policies for the universal connector](https://www.ibm.com/docs/en/guardium/11.4?topic=connector-configuring-policies-universal))
            2.  Check the MustGather logs that data is coming in from the data source for the universal connector to capture.
            3.  Check that communication between the database server and the managed unit is not blocked \(managed unit is accessible from the database server\).
    3.  User plug-ins.
        -   Verify the event path, from DB server to Guardium database.
        -   If you're using Syslog to send the data, verify that it's configured properly to send to the correct Guardium IP and port.
        -   Verify `rsyslog service` is running. Run service rsyslog status.
        -   Check the Connector configuration, on Configure Universal connector page, and verify the port.
    4.  Run MustGather, collect the information, and send to Guardium support.
-   **The Universal connector started but Guardium is not showing any events.**

    Verify that a Policy with a **Log full details** rule is installed, and that no other policy blocks it.

    Create a MustGather and look for the universal connector log \(uc-logstash.log\), or Logstash log \(logstash\_stdout\_stderr.log\), after you perform a MustGather command \(see [Developing a filter plug-in](https://www.ibm.com/docs/en/guardium/11.4?topic=ins-developing-filter-plug-in)). By default, the log level reports only errors. Raise the log level, if needed, by entering: grdapi run\_universal\_connector uc\_debug\_level="debug"If the log seems normal, try to restart the inspection core by entering: restart inspection-core


## Plug-in questions

-   **Universal connector does not start with your plug-in or configuration**

    See [Installing and testing the filter or input plug-in on a staging Guardium system](https://www.ibm.com/docs/en/guardium/11.4?topic=dpi-installing-testing-filter-input-plug-in-staging-guardium-system) for details on how to collect diagnostic data by running a `must gather` command.

-   **Is Java required to create a plug-in?**

    No, you can develop by using Ruby, especially when Guardium knows how to parse your data source commands. However, if you need to parse the commands for a new data source type, Java is the best choice.

-   **How much time does it take to develop a plug-in?**

    Give yourself 4 - 5 weeks, maybe a bit more to perfect it.
