# Getting Started - Creating Your Custom Logstash Filter Plugin for Guardium

This template provides a starting point for creating your own custom Logstash filter plugin for IBM Security Guardium Universal Connector.

## Overview

This template is based on successful implementations like:
- Azure Cosmos DB filter plugin
- Google Cloud BigTable filter plugin

It includes all the necessary structure, build configuration, and example code to help you create a filter plugin for your specific data source.

## Prerequisites

Before you begin, ensure you have:
- Java Development Kit (JDK) 8 or 11
- Gradle (will be downloaded automatically via wrapper)
- Understanding of your data source's audit log format
- Access to the universal-connectors repository structure

## Step-by-Step Guide

### 1. Copy and Rename the Template

```bash
# Navigate to the filter-plugin directory
cd /path/to/universal-connectors/filter-plugin

# Copy the template
cp -r template-logstash-filter-guardium logstash-filter-YOURDATASOURCE-guardium

# Replace YOURDATASOURCE with your actual data source name (e.g., mongodb, postgresql, etc.)
```

### 2. Update Project Names and Identifiers

Replace all placeholders throughout the project:

**Placeholders to replace:**
- `DATASOURCE_PLACEHOLDER` → Your data source name in lowercase (e.g., `mongodb`, `postgresql`)
- `DATASOURCE_NAME` → Your data source display name (e.g., `MongoDB`, `PostgreSQL`)
- `YOUR_PACKAGE_NAME` → Your Java package name (e.g., `mongodb`, `postgresql`)
- `YourFilterClass` → Your main filter class name (e.g., `MongoDbGuardiumFilter`)
- `your_filter_name` → Logstash plugin name (e.g., `mongodb_guardium_filter`)

**Files to update:**
1. `settings.gradle` - Update `rootProject.name`
2. `build.gradle` - Update group, description, pluginInfo fields
3. All Java source files - Update package names and class names
4. `README.md` - Update all documentation
5. Directory names under `src/main/java/com/ibm/guardium/`

### 3. Understand the Core Components

#### A. Main Filter Class (`YourDataSourceGuardiumFilter.java`)

This is the entry point for your plugin. It:
- Receives log events from Logstash
- Validates and filters relevant events
- Calls the Parser to convert events to Guardium records
- Returns processed events

**Key responsibilities:**
```java
@LogstashPlugin(name = "your_datasource_guardium_filter")
public class YourDataSourceGuardiumFilter implements Filter {
    // 1. Validate incoming events
    // 2. Parse JSON/log format
    // 3. Call Parser.parseRecord()
    // 4. Handle exceptions
    // 5. Tag and return events
}
```

#### B. Parser Class (`Parser.java`)

Converts your data source's audit logs into Guardium Record format. It creates:
- **Record**: Top-level Guardium object
- **Accessor**: Who accessed the data (user, client info)
- **SessionLocator**: Network information (IPs, ports)
- **Data/Construct**: What was accessed (SQL/NoSQL queries, objects)
- **ExceptionRecord**: Error information (if applicable)
- **Time**: Timestamp information

#### C. ApplicationConstants Class

Define all constants used in your plugin:
```java
public class ApplicationConstants {
    // Field names from your audit logs
    public static final String TIMESTAMP = "timestamp";
    public static final String USER_NAME = "userName";
    
    // Default values
    public static final String DEFAULT_IP = "0.0.0.0";
    public static final String UNKNOWN_STRING = "";
    
    // Error tags
    public static final String LOGSTASH_TAG_JSON_PARSE_ERROR = "your_datasource_json_parse_error";
}
```

### 4. Implement Your Parser Logic

#### Step 4.1: Analyze Your Audit Logs

Collect sample audit logs from your data source and identify:
- Timestamp format
- User/principal information
- Client IP address
- Database/schema/table names
- Query or operation performed
- Success/failure indicators
- Error messages

#### Step 4.2: Map to Guardium Record Structure

Create a mapping document:

```
Your Audit Log Field → Guardium Field
----------------------------------------
timestamp            → Time (millis, offset)
user_email           → Accessor.dbUser
client_ip            → SessionLocator.clientIp
database_name        → Record.dbName
query_text           → Data.Construct.fullSql
operation_type       → Sentence.verb
table_name           → SentenceObject.name
error_code           → ExceptionRecord.exceptionTypeId
error_message        → ExceptionRecord.description
```

#### Step 4.3: Implement parseRecord() Method

```java
public static Record parseRecord(JsonObject input) {
    Record record = new Record();
    
    // 1. Extract and set time
    record.setTime(parseTime(input));
    
    // 2. Extract database information
    record.setDbName(extractDatabaseName(input));
    
    // 3. Parse accessor (user info)
    record.setAccessor(parseAccessor(input));
    
    // 4. Parse session locator (network info)
    record.setSessionLocator(parseSessionLocator(input));
    
    // 5. Check for errors
    if (hasError(input)) {
        record.setException(parseException(input));
    } else {
        record.setData(parseData(input));
    }
    
    // 6. Set session ID and app user
    record.setSessionId(extractSessionId(input));
    record.setAppUserName(extractAppUserName(input));
    
    return record;
}
```

### 5. Write Unit Tests

Create test cases for:
- Successful operations
- Failed operations
- Different operation types (SELECT, INSERT, UPDATE, DELETE, etc.)
- Edge cases (missing fields, malformed JSON)
- Different user types

Example test structure:
```java
@Test
public void testSuccessfulQuery() {
    String auditLog = "{ /* your sample log */ }";
    Event event = new org.logstash.Event();
    event.setField("message", auditLog);
    
    Collection<Event> results = filter.filter(
        Collections.singletonList(event), 
        matchListener
    );
    
    assertEquals(1, results.size());
    assertNotNull(event.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME));
}
```

### 6. Update Configuration Files

#### build.gradle
- Update `group` to match your package
- Update `description`
- Update `pluginInfo.pluginName`
- Update `pluginInfo.pluginClass`
- Add any specific dependencies your plugin needs

#### settings.gradle
- Update `rootProject.name`

#### VERSION
- Start with version `0.0.1`

### 7. Create Sample Configuration File

Create a `.conf` file showing how to use your plugin:

```
input {
  # Your input plugin configuration
  # e.g., file, kafka, http, etc.
}

filter {
  if [type] == "your_datasource" {
    your_datasource_guardium_filter {}
  }
}

output {
  # Guardium output configuration
}
```

### 8. Write Documentation (README.md)

Your README should include:
1. **Overview**: What data source and what it does
2. **Prerequisites**: Versions tested, environment requirements
3. **Data Source Configuration**: How to enable audit logging
4. **Viewing Audit Logs**: Where to find logs
5. **Limitations**: Known limitations and unsupported features
6. **Guardium Configuration**: How to install and configure in Guardium
7. **Supported Operations**: List of supported audit log types

### 9. Build Your Plugin

```bash
# Build the plugin
./gradlew gem

# The gem file will be created in the project root
# logstash-filter-your_datasource_guardium_filter-X.X.X.gem
```

### 10. Test Your Plugin

#### Local Testing
```bash
# Install in local Logstash
/path/to/logstash/bin/logstash-plugin install /path/to/your-plugin.gem

# Run with your config
/path/to/logstash/bin/logstash -f your-config.conf
```

#### Unit Testing
```bash
./gradlew test
./gradlew jacocoTestReport  # Generate coverage report
```

### 11. Package for Guardium

Create the offline package structure:
```
your-datasource-package/
├── logstash-filter-your_datasource_guardium_filter.zip
├── your_datasource.conf
└── gi_templates.json (if using Guardium Insights)
```

## Common Patterns and Best Practices

### 1. Error Handling
```java
try {
    JsonObject inputJSON = new Gson().fromJson(messageString, JsonObject.class);
    Record record = Parser.parseRecord(inputJSON);
    // ... process record
} catch (Exception exception) {
    log.error("Error parsing event", exception);
    event.tag(LOGSTASH_TAG_JSON_PARSE_ERROR);
}
```

### 2. IP Address Handling
```java
SessionLocator sessionLocator = new SessionLocator();
if (Util.isIPv6(clientIp)) {
    sessionLocator.setIpv6(true);
    sessionLocator.setClientIpv6(clientIp);
    sessionLocator.setServerIpv6(DEFAULT_IPV6);
} else {
    sessionLocator.setIpv6(false);
    sessionLocator.setClientIp(clientIp);
    sessionLocator.setServerIp(DEFAULT_IP);
}
```

### 3. Time Parsing
```java
public static Time parseTime(String dateString) {
    ZonedDateTime date = ZonedDateTime.parse(dateString);
    long millis = date.toInstant().toEpochMilli();
    int minOffset = date.getOffset().getTotalSeconds() / 60;
    return new Time(millis, minOffset, 0);
}
```

### 4. Null Safety
```java
String value = jsonObject.has("field") && jsonObject.get("field") != null
    ? jsonObject.get("field").getAsString()
    : DEFAULT_VALUE;
```

### 5. Logging
```java
private static Logger log = LogManager.getLogger(YourClass.class);

if (log.isDebugEnabled()) {
    log.debug("Processing event: {}", event);
}
log.error("Error occurred", exception);
```

## Troubleshooting

### Plugin Not Loading
- Check `@LogstashPlugin` annotation name matches `pluginInfo.pluginName` in build.gradle
- Verify package structure matches `group` in build.gradle
- Ensure all required dependencies are included

### Events Not Being Processed
- Add debug logging to see what events are received
- Check event tags for error indicators
- Verify your filter condition in Logstash config

### Build Failures
- Ensure `LOGSTASH_CORE_PATH` and `GUARDIUM_UNIVERSALCONNECTOR_COMMONS_PATH` are set
- Check Java version compatibility
- Review dependency versions in `versions.yml`

### Test Failures
- Verify test data matches your actual audit log format
- Check for timezone issues in time parsing
- Ensure all required fields are present in test data

## Additional Resources

- [Guardium Universal Connector Documentation](https://github.com/IBM/universal-connectors)
- [Logstash Plugin Development](https://www.elastic.co/guide/en/logstash/current/contributing-to-logstash.html)
- [Guardium Record Structure](https://github.com/IBM/universal-connectors/blob/main/common/src/main/java/com/ibm/guardium/universalconnector/commons/structures/Record.java)

## Support

For questions or issues:
1. Check existing filter plugins for similar implementations
2. Review the universal-connectors repository documentation
3. Contact the Guardium Universal Connector team

## License

This template is provided under the Apache 2.0 License.