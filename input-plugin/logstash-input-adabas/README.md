# Logstash Adabas Auditing Input Plugin

This is a Java plugin for [Logstash](https://github.com/elastic/logstash).

## Build
The build of this plugin requires the access to an installation of Logstash.

1. Download Logstash from https://www.elastic.co/downloads/logstash
2. Copy the files **rubyUtils.gradle** and **versions.yml** from Github repository https://github.com/elastic/logstash to directory where you installed Logstash

    **Note:** We've identified issues with the `rubyUtils.gradle` file from the Logstash GitHub repository that may cause build failures for this project. Please make the following modifications to the `rubyUtils.gradle` file:

    -  Issue 1: JRuby Version Resolution
       
        **Problem:** Dynamic version reference fails during build
        ```gradle
        // Original (causes build failure)
        classpath "org.jruby:jruby-core:${gradle.ext.versions.jruby.version}"
        ```
        **Solution:** Use the actual version number of jruby from versions.yml, for example:
        ```gradle
        // Fixed version
        classpath "org.jruby:jruby-core:9.4.13.0"
        ```
    
    -  Issue 2: YAML Parsing
    
        **Problem:** Missing YAML parsing logic causes version resolution to fail

        **Solution:** Add the following YAML parsing code after the Ruby variables section:

        ```gradle
        // Ruby variables
        def versionsPath = project.hasProperty("LOGSTASH_CORE_PATH") ? LOGSTASH_CORE_PATH + "/../versions.yml" : "${projectDir}/versions.yml"
        
        // ⚠️Add this YAML parsing code below:
        // Read and parse versions.yml without external dependencies
        def versionsFile = new File(versionsPath)
        if (!versionsFile.exists()) {
            throw new GradleException("versions.yml file not found at: ${versionsPath}")
        }
        
        // Simple YAML parsing for versions.yml structure
        def versionsData = [:]
        def currentSection = null
        versionsFile.eachLine { line ->
            def trimmed = line.trim()
            if (trimmed && !trimmed.startsWith('#')) {
                if (!trimmed.startsWith(' ') && trimmed.endsWith(':')) {
                    // Top level section
                    currentSection = trimmed.replaceAll(':', '')
                    versionsData[currentSection] = [:]
                } else if (trimmed.startsWith('version:') || trimmed.startsWith('sha256:')) {
                    // Property in current section
                    def parts = trimmed.split(':', 2)
                    if (parts.length == 2 && currentSection) {
                        versionsData[currentSection][parts[0].trim()] = parts[1].trim()
                    }
                }
            }
        }
        
        // Set gradle.ext.versions
        gradle.ext.versions = versionsData
        versionMap = gradle.ext.versions
        ```
3. Clone this repository
4. Set the property variable **LOGSTASH_CORE_PATH**. This could be done in gradle.properties file
5. Assemble plugin with the command `./gradlew assemble gem`

After that successful build a file **logstash-input-adabas_auditing_input-<version>-java.gem** is created in the root directory of the project.

See also [How to write a Java input plugin](https://www.elastic.co/guide/en/logstash/current/java-input-plugin.html).

## Install Plugin
To install the plugin use the command 
```
logstash-plugin install --no-verify --local <full-path>/logstash-input-adabas_auditing_input-<version>-java.gem
```

## Run Logstash
Execute the command `logstash -f <file>` where `<file>`is your Logstash configuration file. An example is below.

## Plugin Configuration Example
This configuration reads the data from the Adabas Auditing Server and write the data to `elasticsearch` and `stdout`.

```
input {
  adabas_auditing_input { 
    brokerClass => "class" 
    brokerServer => "server" 
    brokerService => "service" 
    host => "host" 
    port => 3000 
    token => "token" 
    user => "user" 
  }
}
output {
  stdout { 
    codec => rubydebug
  }
}
```

## Plugin Parameter
| Parameter     | Description                 | Type   | Default Value    |
| ------------- | --------------------------- | ------ | ---------------- |
| host          | Broker host                 | String | "localhost"      |
| port          | Broker port                 | Number | 3000             |
| brokerClass   | Broker class name           | String | "class"          |
| brokerServer  | Broker server name          | String | "server"         |
| brokerService | Broker service name         | String | "service"        |
| user          | User                        | String | "user"           |
| token         | Token                       | String | "token"          |
| retryInterval | Retry interval in seconds   | Number | 5                |
| retryCount    | Retry count                 | Number | 10               |
| waitTime      | Wait time in seconds        | Number | 30               |
| receiveLength | Receive length              | Number | 32767            |
| compression   | Compression                 | Number | 0                |
| restURL       | URL of metadata REST server | String | ""               |
| Hosts         | Elasticsearch host          | String | "localhost:9200" |

## Environment Variable
Use the environment variable `REST_PATH` set the directory for the metadata outside of Logstash.
