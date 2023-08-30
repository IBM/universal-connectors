# Troubleshooting tool

The troubleshooting tool helps you identify and resolve issues in UC connections. The troubleshooting tool scans your Logstash log files and searches for known errors. Once it finds errors, it notifies you along with a description of the problem. . Additionally, the troubleshooting tool checks the status of Sniffer and Squid services and informs you if either of them is inactive.

## How it works

To avoid  old or irrelevent messages, the tool scans for errors in the Logstash logs file starting from the last time you ran troubleshooting.For example, if you last ran troubleshooting 3 hours ago, the troubleshooting tool will now only scan for errors from the past 3 hours. If this is your first time running the troubleshooting tool,  it will scan from the beginning of the Logstash logs file. 

## Supported errors

Currently, only the following error patterns are supported:

```
Lines format:
ERROR_PATTERN;ERROR_CODE;ERROR_MESSAGE
InvalidURIError: bad URI;1;InvalidURIError. Please verify credentials correctness and network access.
ERROR GuardConnection;2;The connection to Guardium Sniffer is down. Please verify Sniffer is up.
Your free trial has ended;3;The database free trial has ended.
Check your AWS Secret Access Key and signing method.;4;Check your AWS Secret Access Key and signing method.
The specified log group does not exist.;5;The specified log group does not exist.
HTTPServerException;6;AWS domain not added in allowed domain list.
The security token included in the request is invalid.;7;Check your AWS Secret Access Key and signing method.
ERROR: file oracle_unified_audit_oci.cc;8;The rpm file is not uploaded or uploaded before the oua zip, make sure to first upload rpm and then upload the relevant oua zip.
```

## Procedure

You can run the troubleshooting tool  in 2 ways:

1. Run the troubleshooting tool through this grdapi:

`grdapi universal_connector_troubleshooting`

Example:

2. Run the toubleshooting tool through the Guardium Data Protection UI:A new Run troubleshooting button is now available in GDP 12.0.
   Go to the configure universal connector page and click **Run troubleshooting**.

   Example:

## Notes & Limitations

- Currently, only the listed errors are supported. Additional cases will be supported in the future.

- The troubleshooting tool searches for errors starting from the last time you ran it. If you have not run the tool in a long time, it can take some time until new errors are observed and caught by the troubleshooting tool. 

- The troubleshooting tool does not solve the issues. Rather, it gives you the relevant information about the root cause of the error so that you can fix it. 
