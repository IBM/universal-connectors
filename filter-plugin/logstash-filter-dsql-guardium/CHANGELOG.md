# Changelog

All notable changes to the DSQL Guardium filter plugin will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.0.0] - 2024-01-01

### Added
- Initial release of DSQL Guardium filter plugin
- Support for AWS DSQL Database Activity Streams
- PostgreSQL-compatible SQL parsing (DSQL uses PostgreSQL protocol)
- SQS input integration for receiving audit events
- Parsing of successful SQL statements
- Parsing of SQL errors and exceptions
- Authentication failure detection
- Session tracking with client IP and port
- Database user and database name extraction
- Timestamp parsing from audit logs
- Client application identification
- Comprehensive test coverage
- Documentation and configuration examples

### Features
- **Server Type**: POSTGRESQL
- **Data Protocol**: POSTGRESQL  
- **Language**: PGRS (PostgreSQL)
- **Input Method**: AWS SQS
- **Supported Events**: 
  - Successful queries
  - SQL errors
  - Authentication failures
  - Connection events

### Known Limitations
- IPv6 is not supported
- Client hostname and OS user fields are not available in DSQL audit logs
- Multiline characters in queries may not be preserved when using SQS
- Single line comments in queries are not fully supported

## [Unreleased]

### Planned
- Support for additional DSQL-specific features as they become available
- Enhanced error handling and logging
- Performance optimizations for high-volume environments