# Troubleshooting tool

The troubleshooting tool helps you identify and resolve issues in UC connections. The troubleshooting tool scans your Logstash log files and searches for known errors. Once it finds errors, it notifies you along with a description of the problem. . Additionally, the troubleshooting tool checks the status of Sniffer and Squid services and informs you if either of them is inactive.

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
<img width="1338" alt="Screenshot 2023-08-03 at 13 56 57" src="https://github.com/AlexaNahum/universal-connectors/assets/94283854/df93cb28-73e1-403d-851d-3b2e8d6e4e17">

   Example:
   <img width="1132" alt="Screenshot 2023-08-03 at 14 05 12" src="https://github.com/AlexaNahum/universal-connectors/assets/94283854/b7ed7d0e-389f-4452-a9da-f2d12b300dab">


## Notes & Limitations

- Currently, only the listed errors are supported. Additional cases will be supported in the future.

- The troubleshooting tool searches for errors starting from the last time you ran it. If you have not run the tool in a long time, it can take some time until new errors are observed and caught by the troubleshooting tool. 

- The troubleshooting tool does not solve the issues. Rather, it gives you the relevant information about the root cause of the error so that you can fix it. 
