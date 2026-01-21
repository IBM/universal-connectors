# [DATASOURCE_NAME]-Guardium Logstash Filter Plugin Template

> **Important Note**: This template is located in the `docs/` directory for reference. To use this template and build your plugin, you must copy it to the `filter-plugin/` directory:
> ```bash
> cp -r docs/template-logstash-filter-guardium filter-plugin/logstash-filter-YOURDATASOURCE-guardium
> cd filter-plugin/logstash-filter-YOURDATASOURCE-guardium
> ```
> The build system requires the plugin to be under `filter-plugin/` to access shared resources and dependencies.

# [DATASOURCE_NAME]-Guardium Logstash Filter Plugin Template

### Meet [DATASOURCE_NAME]
* Tested versions: [VERSION_NUMBER]
* Environment: [ENVIRONMENT] (e.g., On-Premise, AWS, Azure, GCP)
* Supported inputs: [INPUT_TYPE] (e.g., Filebeat, Kafka, HTTP, Pub/Sub)
* Supported Guardium versions:
    * Guardium Data Protection: [MIN_VERSION] and above
    * Guardium Insights: [SUPPORTED_VERSION] (if applicable)

This is a [Logstash](https://github.com/elastic/logstash) filter plug-in for the universal connector that is featured in IBM Security Guardium. It parses events and messages from the [DATASOURCE_NAME] audit log into a [Guardium record](https://github.com/IBM/universal-connectors/blob/main/common/src/main/java/com/ibm/guardium/universalconnector/commons/structures/Record.java) instance (which is a standard structure made out of several parts). The information is then sent over to Guardium. Guardium records include the accessor (the person who tried to access the data), the session, data, and exceptions. If there are no errors, the data contains details about the query "construct". The construct details the main action (verb) and collections (objects) involved.

The plug-in is free and open-source (Apache 2.0). It can be used as a starting point to develop additional filter plug-ins for the Guardium universal connector.

## 1. Configuring the [DATASOURCE_NAME] Service

### Prerequisites
[List any prerequisites needed before configuration]

### Procedure:
1. [Step-by-step instructions for setting up your data source]
2. [Include screenshots or code examples where helpful]
3. [Be specific about versions, settings, and configurations]

## 2. Enabling Audit Logging

### Enable Audit Logs
1. [Detailed steps to enable audit logging in your data source]
2. [Specify what types of operations are logged]
3. [Include any performance considerations]

**Note:** [Any important notes about audit logging behavior]

### Audit Log Configuration
[Describe the audit log configuration options]
- **Option 1**: [Description]
- **Option 2**: [Description]

Example configuration:
```
[Configuration example in appropriate format - JSON, YAML, SQL, etc.]
```

## 3. Viewing the Audit Logs

### Accessing Logs
[Describe how to access and view the audit logs]

1. [Step 1]
2. [Step 2]
3. [Step 3]

### Log Format
The audit logs are in [FORMAT] format. Example:
```json
{
  "timestamp": "2024-01-01T12:00:00Z",
  "user": "admin@example.com",
  "operation": "SELECT",
  "database": "mydb",
  "table": "users",
  "query": "SELECT * FROM users WHERE id = 1",
  "status": "SUCCESS"
}
```

### Supported Audit Log Types
* [LOG_TYPE_1] - [Description]
* [LOG_TYPE_2] - [Description]
* [LOG_TYPE_3] - [Description]

## 6. Limitations

1. The following important fields couldn't be mapped with [DATASOURCE_NAME] audit logs:
    - **Source Program**: [Explanation - e.g., "Not available in audit logs, left blank"]
    - **Server IP**: [Explanation - e.g., "Not provided, defaults to 0.0.0.0"]
    - **Client HostName**: [Explanation]
    - **OS User**: [Explanation]
    - [Add other unmapped fields]

2. [Specific limitation related to your data source]
3. [Another limitation]

## 4. Query Parsing and Sniffer Parsers

Parsing query statements is one of the most complicated parts of parsing which could hurt performance. There are a list of available high-performance sniffer parsers that can be used in universal connectors to improve performance. Here is a list of the available ones. This list could get updated if more sniffer parsers get created.

### Available Sniffer Parser Languages:

| Language Mark | Database/Technology |
|---------------|---------------------|
| -tg | TigerGraph |
| -mi | Milvus |
| -b | BigQuery |
| -B | BigQuery SQL |
| -ca | Cassandra |
| -C | Couchbase |
| -c | Cypher |
| -d | DB2 |
| -e | ElsSql |
| -hi | Hive |
| -ha | Hana |
| -i | Informix |
| -im | Impala |
| -m | MySql |
| -x | MySql X |
| -M | MemSql |
| -mo | MongoDB |
| -N | N1ql |
| -n | Neo4j |
| -o | Oracle |
| -os | OpenSearch |
| -p | Postgres |
| -r | Redis |
| -s | Sybase |
| -S | Snowflake |
| -t | TSql/MsSql |
| -td | Teradata |
| -ad | AWS DynamoDB |
| -ae | AWS Elastic Search |
| -as | AWS S3 |
| -ck | Cockroach |
| -nz | Netezza |
| -xq | Xquery |
| -cd | CouchDB |
| -ns | NoSQL |

### Using Sniffer Parsers

If the queries in your database are compatible with any of the above parsers, you can use the corresponding parser in your universal connector.

#### With Sniffer Parser:

In the **Accessor** field:
- Set `language` to the database mark (e.g., `-m` for MySQL, `-p` for Postgres)
- Set `dataType` to `"TEXT"`

In the **Data** field:
- Populate `originalSqlCommand` with the query text

Example:
```java
accessor.setLanguage("-p");  // For PostgreSQL
accessor.setDataType(Accessor.DATA_TYPE_GUARDIUM_SHOULD_PARSE_SQL);
data.setOriginalSqlCommand(queryText);
```

#### Without Sniffer Parser:

In the **Accessor** field:
- Set `language` to `FREE_TEXT`
- Set `dataType` to `"CONSTRUCT"`
- Set `serverType` to the database type

In the **Data** field:
- Populate the `construct` object:
  - **Verb**: The action (for non-SQL DBs) or SQL operation (SELECT, INSERT, DELETE, UPDATE, etc.)
  - **Object**: May have more than one object (DB, table, index, etc.). Set the type for each one.

Example:
```java
accessor.setLanguage(Accessor.LANGUAGE_FREE_TEXT_STRING);
accessor.setDataType(Accessor.DATA_TYPE_GUARDIUM_SHOULD_NOT_PARSE_SQL);
accessor.setServerType("YourDatabaseType");

Construct construct = new Construct();
Sentence sentence = new Sentence("SELECT");
SentenceObject tableObject = new SentenceObject("users");
tableObject.setType("table");
sentence.getObjects().add(tableObject);
construct.sentences.add(sentence);
data.setConstruct(construct);
```

4. [Performance considerations]
5. [Known issues or workarounds]

## 5. Configuring the [DATASOURCE_NAME] Filter in Guardium

The Guardium universal connector is the Guardium entry point for native audit logs. The Guardium universal connector identifies and parses the received events, and converts them to a standard Guardium format. The output of the Guardium universal connector is forwarded to the Guardium sniffer on the collector, for policy and auditing enforcements. Configure Guardium to read the native audit logs by customizing the [DATASOURCE_NAME] template.

### Before you begin
* Configure the policies you require. See [policies](https://github.com/IBM/universal-connectors/#policies) for more information.
* You must have permission for the S-Tap Management role. The admin user includes this role by default.
* Download the [guardium_logstash-offline-plugin-[DATASOURCE_PLACEHOLDER].zip](./package/guardium_logstash-offline-plugin-[DATASOURCE_PLACEHOLDER].zip) plug-in. (Do not unzip the offline-package file throughout the procedure).

## 5. Important Implementation Notes

### Critical Guidelines

1. **Missing Fields in Audit Logs**
   - If a field is not available in the audit logs and you leave it empty, you **MUST** document this in the Limitations section of this README.
   - Example: "Client HostName: Not available in audit logs, left blank"

2. **Session ID Usage**
   - `session_id` in Universal Connectors has a specific meaning. It creates a grouping based on user name, database name, and other fields.
   - Setting this from a wrong value could result in incorrect database names or user names in reports.
   - **Solution**: Do not set `session_id` at all if you are not sure about the correct value.

3. **Guard Record Requirements**
   - **NEVER** return `NULL` as a Guard Record
   - In Guard Record, you must have **either** Data **or** Exception (never both, never neither)

4. **Data and Construct Requirements**
   - If you have Data and you **DO NOT** use sniffer for parsing SQL statements:
     - Construct **CANNOT** be NULL (will cause NullPointerException)
     - You must create the construct object with verb and objects
   
   - If sniffer parses the SQL statement (language is database mark and dataType is TEXT):
     - You only need to set `originalSqlCommand` on the Data field
     - Sniffer will handle construct creation
   
   - Otherwise, it is your responsibility to create the construct object (verb and objects)

5. **S-TAP Identification**
   - S-TAP is identified by:
     - If `serverHostName` is available: `serverHostName:serverPort`
     - Otherwise: `serverIp:serverPort`
   - **Note**: Default values are still being standardized. Use either `""` or `"N.A."` for serverHostName and `"0.0.0.0"` for serverIp

6. **Unit Test Security**
   - **Always** check your unit tests to ensure they do not contain any private or sensitive information
   - Remove any real usernames, passwords, IP addresses, or company-specific data
   - Use placeholder values like "testuser", "testdb", "192.168.1.1", etc.

### Implementation Checklist

Before submitting your plugin, verify:

- [ ] All missing fields are documented in Limitations section
- [ ] Session ID is only set when you have a reliable value
- [ ] Parser never returns NULL as Guard Record
- [ ] Data always has either Construct (if not using sniffer) or originalSqlCommand (if using sniffer)
- [ ] Exception is set for error cases
- [ ] S-TAP identification uses correct serverHostName/serverIp and port
- [ ] Unit tests contain no sensitive information
- [ ] All TODO comments in template code have been addressed
- [ ] README is updated with data source-specific information

### Example: Proper Data/Exception Handling

```java
public static Record parseRecord(JsonObject input) {
    Record record = new Record();
    
    // ... set other fields ...
    
    // Check for errors
    if (isError(input)) {
        // Set exception for error cases
        ExceptionRecord exception = new ExceptionRecord();
        exception.setExceptionTypeId("SQL_ERROR");
        exception.setDescription(getErrorMessage(input));
        exception.setSqlString(getQueryIfAvailable(input));
        record.setException(exception);
    } else {
        // Set data for success cases
        Data data = new Data();
        
        if (useSnifferParser()) {
            // Using sniffer - only set originalSqlCommand
            data.setOriginalSqlCommand(getQuery(input));
        } else {
            // Not using sniffer - must create construct
            Construct construct = new Construct();
            Sentence sentence = new Sentence(getVerb(input));
            // Add objects to sentence
            sentence.getObjects().add(createSentenceObject(input));
            construct.sentences.add(sentence);
            construct.setFullSql(getQuery(input));
            data.setConstruct(construct);  // MUST NOT be null
        }
        
        record.setData(data);
    }
    
    return record;  // NEVER return null
}
```

* Download the plug-in filter configuration file [[DATASOURCE_PLACEHOLDER].conf](.//[DATASOURCE_PLACEHOLDER].conf).

### Procedure
1. On the collector, go to **Setup** > **Tools and Views** > **Configure Universal Connector**.
2. Enable the universal connector if it is disabled.
3. Click **Upload File** and select the offline [guardium_logstash-offline-plugin-[DATASOURCE_PLACEHOLDER].zip](./package/guardium_logstash-offline-plugin-[DATASOURCE_PLACEHOLDER].zip) plug-in. After it is uploaded, click **OK**.
4. Click the Plus sign to open the Connector Configuration dialog box.
5. Type a name in the **Connector name** field.
6. Update the input section to add the details from the [[DATASOURCE_PLACEHOLDER].conf](.//[DATASOURCE_PLACEHOLDER].conf) file's input part, omitting the keyword "input{" at the beginning and its corresponding "}" at the end.
7. Update the filter section to add the details from the [[DATASOURCE_PLACEHOLDER].conf](.//[DATASOURCE_PLACEHOLDER].conf) file's filter part, omitting the keyword "filter{" at the beginning and its corresponding "}" at the end.
8. The **type** fields should match in the input and filter configuration sections. This field should be unique for every individual connector added.
9. Click **Save**. Guardium validates the new connector and displays it in the Configure Universal Connector page.
10. After the offline plug-in is installed and the configuration is uploaded and saved in the Guardium machine, restart the Universal Connector using the **Disable/Enable** button.

## 8. Supported Operations

The following operations are supported by this filter plugin:

### Data Manipulation
- **SELECT/READ**: Query operations
- **INSERT/CREATE**: Insert operations
- **UPDATE/MODIFY**: Update operations
- **DELETE/DROP**: Delete operations

### Data Definition
- **CREATE DATABASE**: Database creation
- **CREATE TABLE**: Table/Collection creation
- **ALTER TABLE**: Schema modifications
- **DROP TABLE**: Table/Collection deletion

### Access Control
- **GRANT**: Permission grants
- **REVOKE**: Permission revocations
- **CREATE USER**: User creation
- **DROP USER**: User deletion

[Customize this list based on your data source's capabilities]

## 9. Input Plugin Configuration

The input plugin determines how audit logs are collected and sent to Logstash. Choose the appropriate input plugin based on your data source's log delivery method.

### Common Input Plugin Options

#### Option 1: File Input (Reading from Log Files)
Use when audit logs are written to files on disk.

```ruby
input {
  file {
    path => "/var/log/[datasource]/audit*.log"
    start_position => "beginning"
    sincedb_path => "/var/lib/logstash/sincedb_[datasource]"
    type => "[DATASOURCE_PLACEHOLDER]"
    codec => json  # Use "plain" for text logs
    tags => ["[DATASOURCE_NAME]"]
  }
}
```

**Key Parameters:**
- `path`: Location of audit log files (supports wildcards)
- `start_position`: "beginning" or "end" (where to start reading)
- `sincedb_path`: Tracks file reading position (for resuming after restart)
- `codec`: "json" for JSON logs, "plain" for text logs

#### Option 2: Kafka Input (Message Queue)
Use when audit logs are published to Kafka topics.

```ruby
input {
  kafka {
    bootstrap_servers => "kafka1:9092,kafka2:9092"
    topics => ["[datasource]-audit-logs"]
    group_id => "logstash-[datasource]-consumer"
    consumer_threads => 4
    type => "[DATASOURCE_PLACEHOLDER]"
    codec => json
    auto_offset_reset => "earliest"
  }
}
```

**Key Parameters:**
- `bootstrap_servers`: Kafka broker addresses
- `topics`: Kafka topics to consume from
- `group_id`: Consumer group identifier
- `consumer_threads`: Number of parallel consumers

#### Option 3: HTTP Input (Webhook/API)
Use when audit logs are pushed via HTTP/HTTPS.

```ruby
input {
  http {
    port => 8080
    type => "[DATASOURCE_PLACEHOLDER]"
    codec => json
    ssl => true
    ssl_certificate => "/path/to/cert.pem"
    ssl_key => "/path/to/key.pem"
    additional_codecs => { "application/json" => "json" }
  }
}
```

**Key Parameters:**
- `port`: Port to listen on
- `ssl`: Enable HTTPS
- `ssl_certificate` / `ssl_key`: SSL/TLS certificates

#### Option 4: Beats Input (Filebeat/Metricbeat)
Use when using Elastic Beats to ship logs.

```ruby
input {
  beats {
    port => 5044
    type => "[DATASOURCE_PLACEHOLDER]"
    ssl => true
    ssl_certificate => "/path/to/cert.pem"
    ssl_key => "/path/to/key.pem"
  }
}
```

**Key Parameters:**
- `port`: Port for Beats to connect to (default: 5044)
- `ssl`: Enable encrypted communication

#### Option 5: Cloud-Specific Inputs

**AWS CloudWatch Logs:**
```ruby
input {
  cloudwatch_logs {
    log_group => "/aws/[datasource]/audit"
    region => "us-east-1"
    type => "[DATASOURCE_PLACEHOLDER]"
    access_key_id => "${AWS_ACCESS_KEY}"
    secret_access_key => "${AWS_SECRET_KEY}"
  }
}
```

**Azure Event Hub:**
```ruby
input {
  azure_event_hubs {
    event_hub_connections => ["Endpoint=sb://...;EntityPath=..."]
    threads => 4
    decorate_events => true
    consumer_group => "$Default"
    storage_connection => "DefaultEndpointsProtocol=https;..."
    type => "[DATASOURCE_PLACEHOLDER]"
  }
}
```

**GCP Pub/Sub:**
```ruby
input {
  google_pubsub {
    project_id => "your-project-id"
    topic => "[datasource]-audit-logs"
    subscription => "logstash-subscription"
    json_key_file => "/path/to/service-account-key.json"
    type => "[DATASOURCE_PLACEHOLDER]"
    codec => json
  }
}
```

### Filter Plugin Configuration

The filter plugin processes the audit logs and converts them to Guardium format.

**Basic Configuration:**
```ruby
filter {
  if [type] == "[DATASOURCE_PLACEHOLDER]" {
    [DATASOURCE_PLACEHOLDER]_guardium_filter {
      source => "message"
    }
  }
}
```

**With Pre-processing:**
```ruby
filter {
  if [type] == "[DATASOURCE_PLACEHOLDER]" {
    # Optional: Parse timestamp if needed
    date {
      match => ["timestamp", "ISO8601", "yyyy-MM-dd HH:mm:ss"]
      target => "@timestamp"
    }
    
    # Optional: Add custom fields
    mutate {
      add_field => {
        "environment" => "production"
        "data_center" => "dc1"
      }
    }
    
    # Main filter plugin
    [DATASOURCE_PLACEHOLDER]_guardium_filter {
      source => "message"
    }
  }
}
```

### Complete Configuration Example

See [datasource_sample.conf](datasource_sample.conf) for a complete configuration file with:
- Multiple input plugin options (commented)
- Filter configuration with pre/post-processing
- Output configuration for Guardium
- Error handling examples
- Performance tuning options

### Configuration Best Practices

1. **Type Field**: Always set a unique `type` field in the input to identify logs from this data source
2. **Codec Selection**: Use `json` codec for JSON logs, `plain` for text logs
3. **Error Handling**: Add error tags and separate outputs for failed events
4. **Performance**:
   - Adjust `pipeline.workers` for parallel processing
   - Set appropriate `pipeline.batch.size` (default: 125)
   - Configure persistent queues for reliability
5. **Security**:
   - Use SSL/TLS for network inputs
   - Store credentials in environment variables or keystore
   - Implement proper access controls
6. **Monitoring**:
   - Enable Logstash monitoring
   - Set up alerts for processing failures
   - Monitor queue sizes and throughput

### Testing Your Configuration

```bash
# Test configuration syntax
/path/to/logstash/bin/logstash -f your-config.conf --config.test_and_exit

# Run with debug output
/path/to/logstash/bin/logstash -f your-config.conf --log.level=debug

# Run in foreground for testing
/path/to/logstash/bin/logstash -f your-config.conf
```

## 10. Troubleshooting

### Common Issues

#### Issue 1: Events not being processed
**Symptoms**: No Guardium records are generated
**Solution**: 
- Check that the `type` field matches in input and filter sections
- Verify audit logs are in the expected format
- Check Logstash logs for parsing errors

#### Issue 2: Missing fields in Guardium
**Symptoms**: Some fields appear as "UNKNOWN" or empty
**Solution**:
- Verify audit logging is configured correctly
- Check if the data source provides the missing information
- Review the limitations section

#### Issue 3: Performance issues
**Symptoms**: High CPU or memory usage
**Solution**:
- Adjust Logstash heap size
- Consider filtering logs before processing
- Review batch size settings

### Debug Mode
To enable debug logging, add this to your Logstash configuration:
```
filter {
  if [type] == "[DATASOURCE_PLACEHOLDER]" {
    mutate {
      add_field => { "[@metadata][debug]" => "true" }
    }
    [DATASOURCE_PLACEHOLDER]_guardium_filter {}
  }
}
```

## 11. Development and Testing

### Building from Source
```bash
# Clone the repository
git clone https://github.com/IBM/universal-connectors.git
cd universal-connectors/filter-plugin/logstash-filter-[DATASOURCE_PLACEHOLDER]-guardium

# Build the plugin
./gradlew gem

# The gem file will be created in the project root
```

### Running Tests
```bash
# Run all tests
./gradlew test

# Generate coverage report
./gradlew jacocoTestReport

# View coverage report
open build/reports/jacoco/index.html
```

### Local Testing
```bash
# Install the plugin locally
/path/to/logstash/bin/logstash-plugin install /path/to/logstash-filter-[DATASOURCE_PLACEHOLDER]_guardium_filter-X.X.X.gem

# Run Logstash with your configuration
/path/to/logstash/bin/logstash -f [DATASOURCE_PLACEHOLDER].conf
```

## 12. Contributing

Contributions are welcome! Please:
1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests for new functionality
5. Ensure all tests pass
6. Submit a pull request

## 13. License

This plugin is licensed under the Apache 2.0 License. See [LICENSE](LICENSE) for details.

## 14. Support

For issues, questions, or contributions:
- GitHub Issues: [Link to issues page]
- Documentation: [Link to additional documentation]
- Universal Connectors: https://github.com/IBM/universal-connectors

## 15. Version History

See [CHANGELOG.md](CHANGELOG.md) for version history and release notes.

---

**Note**: This is a template. Replace all placeholders (marked with [BRACKETS] or DATASOURCE_PLACEHOLDER) with your actual data source information.