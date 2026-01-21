# Template Summary - Logstash Filter Plugin for Guardium

## Overview

This template provides a complete starting point for creating custom Logstash filter plugins for IBM Security Guardium Universal Connector. It was created by analyzing successful implementations from:
- Azure Cosmos DB filter plugin
- Google Cloud BigTable filter plugin

## Template Structure

```
template-logstash-filter-guardium/
├── GETTING_STARTED.md              # Comprehensive guide for using this template
├── README.md                        # Template README with placeholders
├── TEMPLATE_SUMMARY.md             # This file
├── LICENSE                          # Apache 2.0 License
├── VERSION                          # Version file (starts at 0.0.1)
├── CHANGELOG.md                     # Changelog template
├── build.gradle                     # Gradle build configuration
├── settings.gradle                  # Gradle settings
├── datasource_sample.conf          # Sample Logstash configuration
├── gradle/
│   └── wrapper/                    # Gradle wrapper files (copy from samples)
├── src/
│   ├── main/
│   │   └── java/
│   │       └── com/
│   │           └── ibm/
│   │               └── guardium/
│   │                   └── DATASOURCE_PLACEHOLDER/
│   │                       ├── DataSourceGuardiumFilter.java    # Main filter class
│   │                       ├── Parser.java                      # Parser for both JSON and text logs
│   │                       └── ApplicationConstants.java        # Constants
│   └── test/
│       ├── java/
│       │   └── com/
│       │       └── ibm/
│       │           └── guardium/
│       │               └── DATASOURCE_PLACEHOLDER/
│       │                   └── DataSourceGuardiumFilterTest.java  # Unit tests
│       └── resources/                                             # Test resources (add sample logs here)
```

## Key Features

### 1. Dual Format Support
The template supports both JSON and text-based log formats:
- **JSON logs**: Direct parsing using Gson
- **Text logs**: Regex-based extraction
- Flexible architecture allows mixing both formats

### 2. Complete Guardium Record Mapping
Implements all required Guardium structures:
- **Record**: Top-level container
- **Accessor**: User and system information
- **SessionLocator**: Network details (IPv4/IPv6 support)
- **Data/Construct**: Query and operation details
- **ExceptionRecord**: Error handling
- **Time**: Timestamp with timezone support

### 3. Comprehensive Documentation
- **GETTING_STARTED.md**: Step-by-step guide (368 lines)
- **README.md**: Template with all standard sections
- **Inline comments**: Extensive TODO markers and examples
- **Configuration examples**: Multiple input/output scenarios

### 4. Testing Framework
- Unit test template with multiple test cases
- Success and error scenarios
- Edge case handling
- Test helper classes included

### 5. Build System
- Gradle-based build system
- Shadow JAR for dependencies
- JaCoCo for code coverage
- Ruby gem generation for Logstash

## Placeholders to Replace

When using this template, replace the following placeholders throughout all files:

| Placeholder | Description | Example |
|-------------|-------------|---------|
| `DATASOURCE_PLACEHOLDER` | Lowercase data source identifier | `mongodb`, `postgresql` |
| `DATASOURCE_NAME` | Display name of data source | `MongoDB`, `PostgreSQL` |
| `DataSourceGuardiumFilter` | Main filter class name | `MongoDbGuardiumFilter` |
| `your_filter_name` | Logstash plugin name | `mongodb_guardium_filter` |
| `[DATASOURCE_NAME]` | Bracketed display name in docs | `[MongoDB]` |
| `YOUR_PACKAGE_NAME` | Java package name | `mongodb` |

## Quick Start Checklist

- [ ] Copy template to new directory: `logstash-filter-YOURDATASOURCE-guardium`
- [ ] Replace all placeholders (use find & replace)
- [ ] Rename directory: `DATASOURCE_PLACEHOLDER` → your data source name
- [ ] Update `settings.gradle` with correct project name
- [ ] Update `build.gradle` with correct group, plugin info
- [ ] Collect sample audit logs from your data source
- [ ] Implement parsing logic in `Parser.java`
- [ ] Update `ApplicationConstants.java` with your field names
- [ ] Implement validation in `DataSourceGuardiumFilter.java`
- [ ] Write unit tests with real log samples
- [ ] Update `README.md` with data source-specific information
- [ ] Test build: `./gradlew gem`
- [ ] Test locally with Logstash
- [ ] Create package for Guardium deployment

## Implementation Patterns

### Pattern 1: JSON-Only Data Source
If your data source only produces JSON logs:
1. Remove text parsing methods from `Parser.java`
2. Simplify `parseRecord()` to only handle JsonObject
3. Focus on JSON field extraction

### Pattern 2: Text-Only Data Source
If your data source only produces text logs:
1. Remove JSON parsing methods from `Parser.java`
2. Define regex patterns in `ApplicationConstants.java`
3. Implement comprehensive regex extraction

### Pattern 3: Mixed Format Data Source
If your data source produces both formats:
1. Keep both parsing methods
2. Add format detection logic in filter
3. Route to appropriate parser method

### Pattern 4: Complex Query Parsing
If you need to parse SQL/NoSQL queries:
1. Consider using parser libraries (Parboiled, JSqlParser)
2. Add dependencies to `build.gradle`
3. Implement query parsing in separate class
4. Extract verbs and objects for Sentence/SentenceObject

## Common Customizations

### Adding Custom Fields
```java
// In ApplicationConstants.java
public static final String CUSTOM_FIELD = "customField";

// In Parser.java
String customValue = inputJSON.get(ApplicationConstants.CUSTOM_FIELD).getAsString();
```

### Adding Error Codes
```java
// Create ErrorCodes.java
public enum ErrorCodes {
    SUCCESS(0),
    AUTH_FAILED(401),
    NOT_FOUND(404);
    
    private final int code;
    // ... implementation
}
```

### Adding Utility Methods
```java
// Create CommonUtils.java
public class CommonUtils {
    public static boolean isValidJSON(String str) {
        // ... implementation
    }
}
```

## Testing Strategy

### Unit Tests
- Test each operation type (SELECT, INSERT, UPDATE, DELETE, etc.)
- Test success and failure scenarios
- Test edge cases (null, empty, malformed)
- Test different user types
- Test IPv4 and IPv6 addresses

### Integration Tests
- Test with actual Logstash
- Test with sample log files
- Test with Guardium collector
- Verify reports in Guardium UI

### Performance Tests
- Test with high volume logs
- Monitor memory usage
- Check CPU utilization
- Measure throughput

## Deployment

### Building the Plugin
```bash
./gradlew clean gem
```

### Installing Locally
```bash
/path/to/logstash/bin/logstash-plugin install /path/to/plugin.gem
```

### Creating Guardium Package
```
your-datasource-package/
├── logstash-filter-your_datasource_guardium_filter.zip
├── your_datasource.conf
└── gi_templates.json (optional, for Guardium Insights)
```

## Support and Resources

- **Universal Connectors Repo**: https://github.com/IBM/universal-connectors
- **Guardium Record Structure**: Check common module in repo
- **Sample Plugins**: Review existing filter plugins for patterns
- **Logstash Plugin Development**: https://www.elastic.co/guide/en/logstash/current/contributing-to-logstash.html

## Version History

- **0.0.1** (Template Creation): Initial template with dual format support

## Contributing

When improving this template:
1. Test changes with multiple data source types
2. Update documentation
3. Add examples for new patterns
4. Maintain backward compatibility
5. Update this summary document

## License

Apache 2.0 License - See LICENSE file for details

---

**Note**: This template is designed to be flexible and comprehensive. Not all features may be needed for your specific data source. Feel free to remove unused code and simplify where appropriate.