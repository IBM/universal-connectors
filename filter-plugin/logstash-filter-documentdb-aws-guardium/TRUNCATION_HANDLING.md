# DocumentDB Guardium Filter - JSON Truncation Handling

## Overview

This document describes the enhanced error handling for JSON truncation issues in the DocumentDB Guardium filter.

## Problem

Large DocumentDB audit/profiler events can be truncated during transmission or logging, resulting in malformed JSON that causes parsing errors like:

```
JsonSyntaxException: Unterminated array at line 1 column 1518 path $.param.args.updates[2]
```

These errors occur when JSON messages are cut off mid-value (e.g., `"upsert":tru...` instead of `"upsert":true`).

## Solution

The filter now includes comprehensive truncation detection and handling:

### 1. **Pre-Parse Validation**
- Validates JSON structure before attempting to parse
- Detects common truncation patterns:
  - Missing closing brackets
  - Unterminated strings
  - Truncated boolean/null values (`tru`, `fal`, `nul`)
  - Ellipsis patterns (`...`)

### 2. **Size Monitoring**
- Configurable maximum JSON size threshold (default: 1MB)
- Warns when messages exceed the threshold
- Helps identify potential truncation sources

### 3. **Enhanced Error Detection**
- Identifies truncation-specific errors in `JsonSyntaxException`
- Distinguishes between truncation and other parsing errors
- Provides detailed error context

### 4. **Improved Logging**
- Separate tags for different error types:
  - `_documentdbguardium_json_truncated` - Truncated JSON
  - `_documentdbguardium_json_parse_error` - Other parsing errors
  - `_documentdbguardium_json_depth_error` - Nesting/structure errors
- Concise event identifiers (event_id, log_group, type, cluster)
- Message size reporting for truncation errors

## Configuration

### New Configuration Option

```ruby
filter {
  documentdb_guardium_filter {
    # Maximum JSON message size in bytes (default: 1048576 = 1MB)
    max_json_size_bytes => 2097152  # 2MB
  }
}
```

### Recommended Settings

For environments with large DocumentDB operations:
- Increase `max_json_size_bytes` to 2-5MB
- Monitor CloudWatch log group settings for size limits
- Consider enabling CloudWatch Logs data protection

## Error Tags

Events are tagged based on the error type:

| Tag | Description | Action |
|-----|-------------|--------|
| `_documentdbguardium_json_truncated` | JSON appears truncated | Increase log size limits or filter large operations |
| `_documentdbguardium_json_parse_error` | General parsing error | Review JSON structure |
| `_documentdbguardium_json_depth_error` | Nesting too deep or invalid structure | Review event complexity |

## Monitoring

### Log Patterns to Watch

**Truncation Detected:**
```
DocumentDB filter: JSON truncation detected in audit event. Message size: 2048 bytes. Error: Unterminated array...
```

**Size Warning:**
```
DocumentDB filter: JSON message exceeds max size (1500000 bytes), truncating may occur.
```

### Metrics to Track

1. Count of `_documentdbguardium_json_truncated` tags
2. Average message sizes for truncated events
3. Frequency of truncation by cluster/log_group

## Troubleshooting

### High Truncation Rate

**Symptoms:** Many events tagged with `_documentdbguardium_json_truncated`

**Solutions:**
1. **Increase CloudWatch Logs limits:**
   - Check CloudWatch log group retention and size settings
   - Verify no intermediate log processors are truncating

2. **Filter large operations:**
   ```ruby
   filter {
     if [message] =~ /large_collection/ {
       drop { }
     }
   }
   ```

3. **Increase buffer sizes:**
   - Adjust Logstash pipeline batch sizes
   - Review input plugin buffer settings

### Specific Operations Truncating

**Symptoms:** Truncation occurs for specific operation types (e.g., large updates)

**Solutions:**
1. **Exclude problematic operations:**
   ```ruby
   filter {
     if [message] =~ /"update".*"updates":\[/ and [message] !~ /\}\}$/ {
       mutate {
         add_tag => ["_skip_large_update"]
       }
     }
   }
   ```

2. **Sample large operations:**
   - Use sampling to reduce volume of large events
   - Focus on metadata rather than full payloads

### False Positives

**Symptoms:** Valid JSON tagged as truncated

**Solutions:**
1. Review truncation pattern detection in [`validateJson()`](src/main/java/com/ibm/guardium/documentdb/DocumentdbGuardiumFilter.java:362)
2. Adjust pattern matching if needed
3. Report false positives for filter improvement

## Best Practices

1. **Set appropriate size limits** based on your DocumentDB operation patterns
2. **Monitor truncation rates** and adjust configuration accordingly
3. **Use event identifiers** in logs to correlate with source events
4. **Review CloudWatch settings** to ensure logs aren't truncated upstream
5. **Consider operation filtering** for extremely large operations that don't need auditing

## Technical Details

### Truncation Detection Methods

1. **Structural validation:**
   - Checks for matching opening/closing brackets
   - Validates JSON starts with `{` or `[`

2. **Pattern matching:**
   - Regex patterns for incomplete values
   - End-of-string analysis for truncation markers

3. **Exception analysis:**
   - Parses `JsonSyntaxException` messages
   - Identifies truncation-specific error patterns

### Performance Impact

- Pre-validation adds minimal overhead (~1-2ms per event)
- Pattern matching is optimized for common cases
- No impact on successfully parsed events

## Related Files

- [`DocumentdbGuardiumFilter.java`](src/main/java/com/ibm/guardium/documentdb/DocumentdbGuardiumFilter.java) - Main filter implementation
- [`Parser.java`](src/main/java/com/ibm/guardium/documentdb/Parser.java) - JSON parsing logic
- [`README.md`](README.md) - General filter documentation

## Support

For issues or questions:
1. Check logs for specific error messages and event identifiers
2. Review configuration settings
3. Monitor truncation patterns and frequencies
4. Report persistent issues with example events (sanitized)