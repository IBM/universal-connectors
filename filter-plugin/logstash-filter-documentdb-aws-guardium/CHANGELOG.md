# Changelog
Notable changes will be documented in this file.

## [0.1.22]
- SQL query will not appear in Object and Verb column.
- Service name and Database name should be identical.

## [0.1.15]
- added support for insert, update, drop and delete queries
- changed the object value to be a compound of <database>.<collection> and not just the collection name
- fixed cases where the object field was populated by a JSON structure and not the correct object of the command

## [0.1.8]
- Initial Java filter plugin to parse AWS DocumentDB audit log messages into Construct JSON.
  - Using google/gson
