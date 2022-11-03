# Changelog
Notable changes will be documented in this file.

## [0.1.15]
- added support for insert, update, drop and delete queries
- changed the object value to be a compound of <database>.<collection> and not just the collection name
- fixed cases where the object field was populated by a JSON structure and not the correct object of the command

## [0.1.8]
- Initial Java filter plugin to parse AWS DocumentDB audit log mes***REMOVED***ges into Construct JSON.
  - Using google/gson
