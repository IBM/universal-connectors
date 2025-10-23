logstash_dir=${HOME}/tools/logstash_8.13.4
java_home_dir=/Library/Java/JavaVirtualMachines/ibm-jdk-8//Contents/Home/
version=$(shell cat VERSION)

all: jar gem

jar:
	JAVA_HOME=${java_home_dir} JAVA_TOOL_OPTIONS=-Dhttps.protocols=TLSv1.2 ./gradlew assemble

gem: jar
	./gradlew gem

clean:
	./gradlew clean

test:
	JAVA_HOME=${java_home_dir} JAVA_TOOL_OPTIONS=-Dhttps.protocols=TLSv1.2 ./gradlew test

package:
	  ${logstash_dir}/bin/logstash-plugin install logstash-filter-oua_filter-${version}.gem
	  ${logstash_dir}/bin/logstash-plugin prepare-offline-pack --output guardium-oua-uc.zip --overwrite logstash-filter-oua_filter

