## 3.0.2
  - Bug fix for `dbName` column values mismatch when executing commands that operate against varying databases
  - Bug fix for `sourceProgram` column values mismatch when using the inclusion filter with all of the client apps
## 3.0.1
  - Supported SQL Instance GCP plug-in changed from `general-log` to `cloudsql_mysql_audit`
    - Includes support of multi-line queries containing newline characters
  - Added support for various MySQL Client Applications
## 2.0.1
  - Bug fix for Cloud SQL Proxy general logs comment regexp
## 2.0.0
  - Added support for integrating Cloud SQL Proxy connectivity setup with DBeaver
  - Performance improvements
## 1.0.0
- Stable version
## 0.1.0
  - Plugin created with the logstash plugin generator
