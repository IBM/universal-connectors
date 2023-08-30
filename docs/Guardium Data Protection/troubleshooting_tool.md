# Troubleshooting tool

The troubleshooting tool helps you identify and resolve issues in UC connections. The troubleshooting tool scans your Logstash log files and searches for known errors. Once it finds errors, it notifies you along with a description of the problem. . Additionally, the troubleshooting tool checks the status of Sniffer and Squid services and informs you if either of them is inactive.

## How it works

To avoid  old or irrelevent messages, the tool scans for errors in the Logstash logs file starting from the last time you ran troubleshooting.For example, if you last ran troubleshooting 3 hours ago, the troubleshooting tool will now only scan for errors from the past 3 hours. If this is your first time running the troubleshooting tool,  it will scan from the beginning of the Logstash logs file. 



## Procedure

You can run the troubleshooting tool  in 2 ways:

1. Run the troubleshooting tool through this grdapi:

`grdapi universal_connector_troubleshooting`


2. Run the toubleshooting tool through the Guardium Data Protection UI:

   A new Run troubleshooting button is now available in GDP 12.0.
 Go to the configure universal connector page and click **Run troubleshooting**.

<img width="1338" alt="Screenshot 2023-08-03 at 13 56 57" src="https://github.com/AlexaNahum/universal-connectors/assets/94283854/df93cb28-73e1-403d-851d-3b2e8d6e4e17">

  
   <img width="1132" alt="Screenshot 2023-08-03 at 14 05 12" src="https://github.com/AlexaNahum/universal-connectors/assets/94283854/b7ed7d0e-389f-4452-a9da-f2d12b300dab">


## Notes & Limitations

- The troubleshooting tool searches for errors starting from the last time you ran it. If you have not run the tool in a long time, it can take some time until new errors are observed and caught by the troubleshooting tool. 

- The troubleshooting tool does not solve the issues. Rather, it gives you the relevant information about the root cause of the error so that you can fix it. 
