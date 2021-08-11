# Guardium Universal-Connector Commons
This project contains helper classes & utilities for creating a Guardium Record – a required object while developing a plugin for Guardium Universal connector (a feature within IBM Security Guardium). 

## How to use it
1. Clone the project
2. Build the JARs by running `./gradlew.unix assemble` or `./gradlew.bat assemble` on Windows. This will create 3 JAR files containins the compiled code, javaDocs, and sources.
3. Clone or download the [example plugin project][logstash-filter-mongodb-guardium] 
2. Within your plugin project, create _gradle.properties_ and add a reference to the folder with the built JAR files. For example: 

    GUARDIUM_UNIVERSALCONNECTOR_COMMONS_PATH=../guardium-universalconnector-commons/build/libs
    
The GUARDIUM_UNIVERSALCONNECTOR_COMMONS_PATH variable is used in _build.gradle_, when compiling and building the code in your plugin project.
5. Link the javadoc and sources JAR files from within your preferred IDE, to assist in your development. 

## Contribute
We want your feedback: Let us know of any errors, or if you have an improvement suggestion, by creating a pull request, or sending us a message. 

<!-- references -->
[logstash-filter-mongodb-guardium]: https://github.com/IBM/logstash-filter-mongodb-guardium




