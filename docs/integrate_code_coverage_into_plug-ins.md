# Integrate Code Coverage tool into Universal Connector Plug-ins
## Introduction
As a Universal Connector plug-in developer, you should be familiarized with Code Coverage tools targeted at monitoring the plug-in's unit tests folder for percentage coverage that corresponds to our conventions.

<details open="open">
  <summary>Table of contents</summary>

  - [Integrate code coverage into Java plug-ins](#integrate-code-coverage-into-java-plug-ins)
    * [Prerequisites](#prerequisites)
    * [Keep In Mind](#keep-in-mind)
    * [Template build.gradle](#template-build.gradle)
    * [Template Makefile](#template-makefile)
  - [Integrate code coverage into Ruby plug-ins](#integrate-code-coverage-into-ruby-plug-ins)

</details>

## Integrate code coverage into Java plug-ins
### Prerequisites
- Jacoco code coverage tool for Java
- A developed Universal Connector Java plug-in that includes unit tests in the path designated by Logstash for Java plug-ins (`<TYPE>-plugin/logstash-<TYPE>-<PLUGIN_NAME>/src/test`)

### Keep In Mind
- Make sure you install all the necessary Jacoco plug-ins and set the minimum coverage as detailed in the template build.gradle bellow.

  - Plug-ins used: jacoco, org.barfuin.gradle.jacocolog

  - Note that to be able to install the jacocolog plug-in, you must include this classpath in the dependencies of the buildscript: org.barfuin.gradle.jacocolog:gradle-jacoco-log:3.0.0-RC2

- Executing “./gradlew build” executes the Jacoco percentage coverage and the reports output path in your plug-in’s directory.

  - You can add a Makefile with a task that executes the commands as an alternative (see template Makefile bellow)

- You can add files to the exclusion list in order to exclude them from the Jacoco reports task jacocoTestReport and the Jacoco percentage coverage task jacocoTestCoverageVerification

- minimumCoverage is adjustable. The recommended value is `80.0%`

- Note: this is a template, therefore any other Logstash plug-in specific dependencies or tasks need to be added

### Template build.gradle
```
import java.nio.file.Files
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING

apply plugin: 'java'
apply from: LOGSTASH_CORE_PATH + "/../rubyUtils.gradle"

// ===========================================================================
// plugin info
// ===========================================================================
group                      '' // must match the package of the main plugin class
version                    "${file("VERSION").text.trim()}" // read from required VERSION file
description                = ""
pluginInfo.licenses        = ['Apache-2.0'] // list of SPDX license IDs
pluginInfo.longDescription = ""
pluginInfo.authors         = ['IBM']
pluginInfo.email           = ['']
pluginInfo.homepage        = ""
pluginInfo.pluginType      = ""
pluginInfo.pluginClass     = ""
pluginInfo.pluginName      = "" // must match the @LogstashPlugin annotation in the main plugin class
// ===========================================================================

sourceCompatibility = 1.8
targetCompatibility = 1.8

def jacocoVersion = '0.8.4'

// minimumCoverage can be set by Travis ENV
def minimumCoverageStr = System.getenv("MINIMUM_COVERAGE") ?: "50.0%"
if (minimumCoverageStr.endsWith("%")) {
    minimumCoverageStr = minimumCoverageStr.substring(0, minimumCoverageStr.length() - 1)
}
def minimumCoverage = Float.valueOf(minimumCoverageStr) / 100


buildscript {
    repositories {
        maven {
            url "https://plugins.gradle.org/m2/"
        }
        mavenCentral()
        jcenter()
    }

    dependencies {
        classpath 'com.github.jengelman.gradle.plugins:shadow:4.0.4'
        classpath "org.barfuin.gradle.jacocolog:gradle-jacoco-log:3.0.0-RC2"

    }
}

repositories {
    mavenCentral()
}

tasks.register("vendor"){
    dependsOn shadowJar
    doLast {
        String vendorPathPrefix = "vendor/jar-dependencies"
        String projectGroupPath = project.group.replaceAll('\\.', '/')
        File projectJarFile = file("${vendorPathPrefix}/${projectGroupPath}/${pluginInfo.pluginFullName()}/${project.version}/${pluginInfo.pluginFullName()}-${project.version}.jar")
        projectJarFile.mkdirs()
        Files.copy(file("$buildDir/libs/${project.name}-${project.version}.jar").toPath(), projectJarFile.toPath(), REPLACE_EXISTING)
        validatePluginJar(projectJarFile, project.group)
    }
}

apply plugin: 'com.github.johnrengelman.shadow'

shadowJar {
    classifier = null
}

dependencies
 {
    implementation group: 'commons-validator', name: 'commons-validator', version: '1.7'
    implementation group: 'org.apache.logging.log4j', name: 'log4j-core', version: '2.17.1'
    implementation 'org.apache.commons:commons-lang3:3.7'
    implementation 'com.google.code.gson:gson:2.8.9'
    implementation fileTree(dir: LOGSTASH_CORE_PATH, include: "build/libs/logstash-core.jar")
    implementation fileTree(dir: GUARDIUM_UNIVERSALCONNECTOR_COMMONS_PATH, include: "common-*.*.*.jar")
    //implementation 'com.google.code.gson:gson:2.8.9'


    testImplementation 'junit:junit:4.12'
    testImplementation 'org.jruby:jruby-complete:9.2.7.0'

    testImplementation fileTree(dir: GUARDIUM_UNIVERSALCONNECTOR_COMMONS_PATH, include: "guardium-universalconnector-commons-?.?.?.jar")
}


clean {
    delete "${projectDir}/Gemfile"
    delete "${projectDir}/" + pluginInfo.pluginFullName() + ".gemspec"
    delete "${projectDir}/lib/"
    delete "${projectDir}/vendor/"
    new FileNameFinder().getFileNames(projectDir.toString(), pluginInfo.pluginFullName() + "-*.*.*.gem").each { filename ->
        delete filename
    }
}

tasks.withType(JavaCompile) {
    options.encoding = 'UTF-8'
}



tasks.register("generateRubySupportFiles") {
    doLast {
        generateRubySupportFilesForPlugin(project.description, project.group, version)
    }
}

tasks.register("removeObsoleteJars") {
    doLast {
        new FileNameFinder().getFileNames(
                projectDir.toString(),
                "vendor/**/" + pluginInfo.pluginFullName() + "*.jar",
                "vendor/**/" + pluginInfo.pluginFullName() + "-" + version + ".jar").each { f ->
            delete f
        }
    }
}

tasks.register("gem"){
    dependsOn = [downloadAndInstallJRuby, removeObsoleteJars, vendor, generateRubySupportFiles]
    doLast {
        buildGem(projectDir, buildDir, pluginInfo.pluginFullName() + ".gemspec")
    }
}

apply plugin: 'jacoco'
apply plugin: "org.barfuin.gradle.jacocolog"

// ------------------------------------
// JaCoCo is a code coverage tool
// ------------------------------------

jacoco {
    toolVersion = "${jacocoVersion}"
    reportsDir = file("$buildDir/reports/jacoco")
}

jacocoTestReport {
    // You will see "Report -> file://...." at the end of a JaCoCo build
    // If no output, run this first:   ./gradlew test
    reports {
        html.enabled true
        xml.enabled true
        csv.enabled true
        html.destination file("${buildDir}/reports/jacoco")
        csv.destination file("${buildDir}/reports/jacoco/all.csv")
    }
    executionData.from fileTree(dir: "${buildDir}/jacoco/", includes: [
            '**/*.exec'
    ])

    afterEvaluate {
        // objective is to test TicketingService class
        classDirectories.setFrom(files(classDirectories.files.collect {
            fileTree(dir: it, exclude: [])
        }))
    }

    doLast {
        println "Report -> file://${buildDir}/reports/jacoco/index.html"
    }
}

test.finalizedBy jacocoTestReport

jacocoTestCoverageVerification {
    violationRules {
        rule {
            limit {
                minimum = minimumCoverage
            }
        }
    }
    executionData.from fileTree(dir: "${buildDir}/jacoco/", includes: [
            '**/*.exec'
    ])
    afterEvaluate {
        // objective is to test TicketingService class
        classDirectories.setFrom(files(classDirectories.files.collect {
            fileTree(dir: it, exclude: [])
        }))
    }
}

project.tasks.check.dependsOn(jacocoTestCoverageVerification, jacocoTestReport)

```

### Template Makefile
```
# **************************************************************
#
# IBM Confidential
#
# OCO Source Materials
#
# 5737-L66
#
# (C) Copyright IBM Corp. 2019, 2022
#
# The source code for this program is not published or otherwise
# divested of its trade secrets, irrespective of what has been
# deposited with the U.S. Copyright Office.
#
# **************************************************************
# To run unit-test and JaCoCo code coverage script:
check:
	./gradlew check

# To run just JaCoCo code coverage script, make test will be executed first.
report:  test
	./gradlew jacocoTestReport

```

## Integrate code coverage into Ruby plug-ins
**TBD**
