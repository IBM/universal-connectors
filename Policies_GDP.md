# Data activity monitoring

Information about key security concepts used in Guardium data activity monitoring.

## Policies and rules

A security policy contains an ordered set of rules to be applied to the observed traffic between database clients and servers. Each rule can apply to a request from a client, or to a response from a server. Multiple policies can be defined and multiple policies can be installed on a Guardium system at the ***REMOVED***me time.

Each rule in a policy defines a conditional action. The condition can be a simple test, for example a check for any access from a client IP address not found in an Authorized Client IPs group, or the condition can be a complex test that evaluates multiple mes***REMOVED***ge and session attributes such as database user, source program, command type, time of day, etc. Rules can also be sensitive to the number of times a condition is met within a specified timeframe.

The action triggered by the rule can be a notification action (e-mail to one or more recipients, for example), a blocking action (the client session might be disconnected), or the event might simply be logged as a policy violation. Custom actions can be developed to perform any tasks neces***REMOVED***ry for conditions that may be unique to a given environment or application.

## Workflows

Workflows consolidate several database activity monitoring tasks, including asset discovery, vulnerability assessment and hardening, database activity monitoring and audit reporting, report distribution, sign-off by key stakeholders, and escalations.

Workflows are intended to transform database security management from a time-consuming manual activity performed periodically to a continuously automated process that supports company privacy and governance requirements, such as PCI-DSS, SOX, Data Privacy and HIPAA. In addition, workflows support the exporting of audit results to external repositories for additional forensic analysis via Syslog, CSV/CEF files, and external feeds.

For example, a compliance workflow automation process might address the following questions: what type of report, assessment, audit trail, or classification is needed, who should receive this information and how sign-offs are handled, and what is the schedule for delivery?

## Auditing

Guardium provides value change auditing features for tracking changes to values in database tables.

For each table in which changes are to be tracked, you can select which SQL value-change commands to monitor (insert, update, delete). Before and after values are captured each time a value-change command is executed against a monitored table. This change activity is uploaded to Guardium on a scheduled basis, after which all of Guardium‘s reporting and alerting functions can be used.

You can view value-change data from the default Values Changed report, or you can create custom reports using the Value Change Tracking domain. 

## Classification

Guardium supports the discovery and classification of sensitive data to allow the creation and enforcement of effective access policies.
