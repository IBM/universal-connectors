# Changelog
Notable changes will be documented in this file.

## [Unreleased]
## [0.6.5]
### Changed 
- Changed imports to guardium-universalconnector-commons, whatever version.

## [0.6.2]
### Fixed
- removed TODO/FIXME comments, and redundunt code.

## [0.6.1]
### Changed
- SessionLocator.clientPort set to default port, to mark Guardium Universal connector to fetch session from SessionID, if nothing written to client port.

## [0.6.0]
### Changed
- Renamed filter name to mongodb-guardium-filter (instead of java-filter-example) and moved package to com.ibm.guardium.mongodb

## [0.5.1]
### Fixed 
- Added support for MongoDB v4.4 timeformat change in ts.$date (with unit tests for createCollection & mapReduce).

## [0.4.9]
### Fixed 
- Send "\[json-object\]" when object is not a JSON primitive, as two word objects are not supported in Investigation dashboard. 
### Changed
- time formate changed to include timezone, to better represent time of datasource server.
- change Record field to GuardRecord.

## [0.4.6]
### Fixed
- Remove source program ("mongod" is not \[client\] source program) 

## [0.4.5]
### Added
- Support commands with 2 words, like "mapReduce", "resetErrors", if logged case-insensitive.
- Send [compound object] when object is not a JSON primitive

## [0.4.2]
### Changed
- override localhost remote/local IPs coming from MongoDB logs, if Logstash event contains "server_ip" field.

## [0.4.1]
### Changed
- DB protocol changed from Logstash to "MongoDB native audit"
- Server type changed from "MONGODB" to "MongoDB"

### Fixed
- N/A in various fields was changed to an empty string, to better resemble how MongoDB STAP reflects unavailable data.

## [0.4.0]
### Changed
- $lookup and $graphLookup required arguments are not redacted (from, localField, foreignField, as, connectFromField, connectToField).

## [0.3.3]
### Fixed
- Record & Data timestamps are now sent as Long, to allow millisecond precision.

## [0.3.2]
### Changed
- Removed mongoDB messages with empty users[], as they are either internal or occur during authentication.

## [0.3.1]
### Added 
- Filter tags messages that are not related to MongoDB as "_mongoguardium_skip_not_mongodb" (not removed from pipeline).

### Fixed
- Filter plugin now skips and removes messages/events that are not authCheck and authenticate from logstash pipeline, to prevent unnecessary JSON parse errors.

## [0.3.0]
###  Added
- Support for IPv6

### Changed
- Edited README.md to better specify required fields for Logstash Filter
- Classes Construct, Sentence, SentenceObject moved into com.ibm.guardium.connector.structures package. 


## [0.2.0]
- Add support for Authentication and Authorization errors.

## [0.0.1]
- Initial Java filter plugin to parse mongodb syslog messages into Construct JSON. 
  - Using google/gson